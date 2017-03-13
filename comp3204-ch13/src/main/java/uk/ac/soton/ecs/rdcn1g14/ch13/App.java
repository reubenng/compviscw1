package uk.ac.soton.ecs.rdcn1g14.ch13;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.model.EigenImages;

/**
 * Chapter 13. Face recognition 101: Eigenfaces 
 * 
 * http://openimaj.org/tutorial/eigenfaces.html
 * 
 * @author Reuben Ng
 * @email rdcn1g14@soton.ac.uk
 * @version 1.0 19 Nov 2016
 */

public class App {
    public static void main( String[] args ) throws FileSystemException {

    	VFSGroupDataset<FImage> dataset = 
    		new VFSGroupDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER);
    	
    	// split data into training and testing set
    	int nTraining = 5;
    	int nTesting = 5;
    	GroupedRandomSplitter<String, FImage> splits = 
    	    new GroupedRandomSplitter<String, FImage>(dataset, nTraining, 0, nTesting);
    	GroupedDataset<String, ListDataset<FImage>, FImage> training = splits.getTrainingDataset();
    	GroupedDataset<String, ListDataset<FImage>, FImage> testing = splits.getTestDataset();
    	
    	// use training data to learn PCA basis
    	List<FImage> basisImages = DatasetAdaptors.asList(training);
    	int nEigenvectors = 100;
    	EigenImages eigen = new EigenImages(nEigenvectors);
    	eigen.train(basisImages);
    	
    	// show first 12 basis vectors
    	List<FImage> eigenFaces = new ArrayList<FImage>();
    	for (int i = 0; i < 12; i++) {
    	    eigenFaces.add(eigen.visualisePC(i));
    	}
    	DisplayUtilities.display("EigenFaces", eigenFaces);
    	
    	// build database of features from the training images
    	Map<String, DoubleFV[]> features = new HashMap<String, DoubleFV[]>();
    	for (final String person : training.getGroups()) {
    	    final DoubleFV[] fvs = new DoubleFV[nTraining];

    	    for (int i = 0; i < nTraining; i++) {
    	        final FImage face = training.get(person).get(i);
    	        fvs[i] = eigen.extractFeature(face);
    	    }
    	    features.put(person, fvs);
    	}
    	
    	// extract feature from image
    	double correct = 0, incorrect = 0;
    	for (String truePerson : testing.getGroups()) {
    	    for (FImage face : testing.get(truePerson)) {
    	        DoubleFV testFeature = eigen.extractFeature(face);
    	        
    	        /**
    	         * 13.1.3. Exercise 3: Apply a threshold 
    	         * 
    	         *  If the distance between the query face and closest database face 
    	         *  was greater than threshold, an unknown result will be returned.
    	         *  
    	         *  The difference in distance has the highest value of more than 30,
    	         *  thus threshold of 30 was chosen.
    	         *  
    	         */
    	        
    	        // find database feature with smallest Euclidean distance
    	        String bestPerson = null;
    	        double minDistance = Double.MAX_VALUE;
    	        double threshold = 30;
    	        for (final String person : features.keySet()) {
    	            for (final DoubleFV fv : features.get(person)) {
    	                double distance = fv.compare(testFeature, DoubleFVComparison.EUCLIDEAN);

    	                if (distance < minDistance) {
    	                    minDistance = distance;
    	                    bestPerson = person;
    	                }else if (distance > threshold){
    	                	System.out.println("\nDistance higher than threshold: Unknown face.");
    	                }
    	            }
    	        }

    	        System.out.println("Actual: " + truePerson + "\tguess: " + bestPerson);
    	        // find accuracy
    	        if (truePerson.equals(bestPerson))
    	            correct++;
    	        else
    	            incorrect++;
    	    }
    	}

    	System.out.println("Accuracy: " + (correct / (correct + incorrect)));

/**
 * 13.1.1. Exercise 1: Reconstructing faces 
 * 
 * Reconstruct face from extracted features
 */
    	// choose a random face from the testing set
    	FImage randomFace = testing.getRandomInstance();
    	DisplayUtilities.display(randomFace, "random face");
    	
    	// extract feature from face
        DoubleFV randomfaceFeature = eigen.extractFeature(randomFace);
        // reconstruct face using feature
    	FImage reconstructFace = eigen.reconstruct(randomfaceFeature);
    	// normalise image
    	reconstructFace = reconstructFace.normalise();
    	// display result
    	DisplayUtilities.display(reconstructFace, "Reconstructed random face");

/**
 * 13.1.2. Exercise 2: Explore the effect of training set size 
 * 
 * Testing accuracy for training set lower than 5
 * 
 * Using for loop for training set size 1 to 4 and find the accuracy.
 * 
 * The lower the training set size, the lower the accuracy.
 */
    	for ( int nTraining2 = 1 ; nTraining2 < 5 ; nTraining2++){
    		
	    	// split data into training and testing set
	    	int nTesting2 = 5;
	    	splits = new GroupedRandomSplitter<String, FImage>(dataset, nTraining2, 0, nTesting2);
	    	training = splits.getTrainingDataset();
	    	testing = splits.getTestDataset();
	
	    	// use training data to learn PCA basis
	    	basisImages = DatasetAdaptors.asList(training);
	    	nEigenvectors = 100;
	    	eigen = new EigenImages(nEigenvectors);
	    	eigen.train(basisImages);
	    	
	    	// build database of features from the training images
	    	features = new HashMap<String, DoubleFV[]>();
	    	for (final String person : training.getGroups()) {
	    	    final DoubleFV[] fvs = new DoubleFV[nTraining2];
	
	    	    for (int i = 0; i < nTraining2; i++) {
	    	        final FImage face = training.get(person).get(i);
	    	        fvs[i] = eigen.extractFeature(face);
	    	    }
	    	    features.put(person, fvs);
	    	}
	    	
	    	// extract feature from image
	    	correct = 0; 
	    	incorrect = 0;
	    	for (String truePerson : testing.getGroups()) {
	    	    for (FImage face : testing.get(truePerson)) {
	    	        DoubleFV testFeature = eigen.extractFeature(face);
	
	    	        // find database feature with smallest Euclidean distance
	    	        String bestPerson = null;
	    	        double minDistance = Double.MAX_VALUE;
	    	        for (final String person : features.keySet()) {
	    	            for (final DoubleFV fv : features.get(person)) {
	    	                double distance = fv.compare(testFeature, DoubleFVComparison.EUCLIDEAN);
	
	    	                if (distance < minDistance) {
	    	                    minDistance = distance;
	    	                    bestPerson = person;
	    	                }
	    	            }
	    	        }
	
	    	        //System.out.println("Actual: " + truePerson + "\tguess: " + bestPerson);
	
	    	        // find accuracy
	    	        if (truePerson.equals(bestPerson))
	    	            correct++;
	    	        else
	    	            incorrect++;
	    	    }
	    	}
	
	    	System.out.println("\nUsing " + nTraining2 + " training images.");
	    	System.out.println("Accuracy: " + (correct / (correct + incorrect)));
    	}


    }
}
