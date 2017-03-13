package uk.ac.soton.ecs.rdcn1g14.ch12;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.data.DataSource;
import org.openimaj.data.dataset.Dataset;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.experiment.dataset.sampling.GroupedUniformRandomisedSampler;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.feature.DiskCachingFeatureExtractor;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.data.LocalFeatureListDataSource;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.io.IOUtils;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101.Record;
import org.openimaj.image.feature.dense.gradient.dsift.ByteDSIFTKeypoint;
import org.openimaj.image.feature.dense.gradient.dsift.DenseSIFT;
import org.openimaj.image.feature.dense.gradient.dsift.PyramidDenseSIFT;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
//import org.openimaj.image.feature.local.aggregate.BlockSpatialAggregator;
import org.openimaj.image.feature.local.aggregate.PyramidSpatialAggregator;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.ByteKMeans;
import org.openimaj.ml.kernel.HomogeneousKernelMap;
import org.openimaj.ml.kernel.HomogeneousKernelMap.KernelType;
import org.openimaj.ml.kernel.HomogeneousKernelMap.WindowType;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator.Mode;
import org.openimaj.util.pair.IntFloatPair;

import de.bwaldvogel.liblinear.SolverType;

/**
 * Chapter 12. Classification with Caltech 101 
 * 
 * The original code on
 * http://openimaj.org/tutorial/classification101.html
 * has accuracy of approx. 0.7
 * 
 * @author Reuben Ng
 * @email rdcn1g14@soton.ac.uk
 * @version 1.1 25 Nov 2016
 */

public class App {
    public static void main( String[] args ) throws IOException {
    	
    	// use all Caltech101 dataset
    	GroupedDataset<String, VFSListDataset<Record<FImage>>, Record<FImage>> allData = 
    			Caltech101.getData(ImageUtilities.FIMAGE_READER);
    	
    	/**
		 * 12.1.3. Exercise 3: The whole dataset 
		 * 
		 * Using the whole dataset instead of just the first 5 classes
		 * visual words of 600, extra PyramidDenseSIFT scales [4, 6, 8, 10]
		 * DenseSIFT step-size reduced to 3
		 * using PyramidSpatialAggregator instead of BlockSpatialAggregator with [2, 4] blocks.
		 */
    	
         
    	// create subset of groups in a GroupedDataset
    	// new dataset called data from the first 5 classes in the allData dataset
    	GroupedDataset<String, ListDataset<Record<FImage>>, Record<FImage>> data = 
    			GroupSampler.sample(allData, 5, false);
    	
    	
    	// training dataset with 15 images per group, 15 testing images per group
    	// zero is number of validation images
    	// using allData instead of data, change accordingly
    	GroupedRandomSplitter<String, Record<FImage>> splits = 
    			new GroupedRandomSplitter<String, Record<FImage>>(allData, 15, 0, 15);
    	
    	// construct Dense SIFT extractor 
    	DenseSIFT dsift = new DenseSIFT(3, 7); //5
    	// takes DenseSIFT and applies to 7 pixels windows
    	PyramidDenseSIFT<FImage> pdsift = new PyramidDenseSIFT<FImage>(dsift, 6f, 4); //7
    	
    	// assign SIFT features to identifiers
    	// get random 30 images across all groups of training set to train the quantiser
    	HardAssigner<byte[], float[], IntFloatPair> assigner = 
    			trainQuantiser(GroupedUniformRandomisedSampler.sample(splits.getTrainingDataset(), 30), pdsift);
    	
    	/**
		 * 12.1.2. Exercise 2: Feature caching 
		 * 
		 * cache features extracted to disk, generate and save if don't exist,
		 * read features from disk if they do exist.
		 */
    	
    	// save the HardAssigner
    	IOUtils.writeToFile(assigner, new File("/home/reuben/OpenIMAJ/cw1/ch12/HardAssigner.txt"));
    	// read the saved HardAssigner
    	// assigner = IOUtils.readFromFile(new File("/home/reuben/OpenIMAJ/cw1/ch12/HardAssigner.txt"));
    	
    	// construct an instance of PHOWExtractor
    	// extract features and save it to directory, change directory path accordingly
    	FeatureExtractor<DoubleFV, Record<FImage>> extractor = new DiskCachingFeatureExtractor<DoubleFV, Caltech101.Record<FImage>>(
				new File("/home/reuben/OpenIMAJ/cw1/ch12"), new PHOWExtractor(pdsift, assigner));

    	// use linear classifier by LiblinearAnnotator
    	LiblinearAnnotator<Record<FImage>, String> ann = new LiblinearAnnotator<Record<FImage>, String>(
	        extractor, Mode.MULTICLASS, SolverType.L2R_L2LOSS_SVC, 1.0, 0.00001);
    	// train classifier
		ann.train(splits.getTrainingDataset());
		
		// perform automated evaluation for classifier’s accuracy
		ClassificationEvaluator<CMResult<String>, String, Record<FImage>> eval = new ClassificationEvaluator<CMResult<String>, String, Record<FImage>>(
			ann, splits.getTestDataset(), new CMAnalyser<Record<FImage>, String>(CMAnalyser.Strategy.SINGLE));
			
		Map<Record<FImage>, ClassificationResult<String>> guesses = eval.evaluate();
		CMResult<String> result = eval.analyse(guesses);

		// Get a String detailing the result
		System.out.println(result.getDetailReport());
		
		// Get a String summarising the result
		System.out.println(result.getSummaryReport());
		
/**
 * 12.1.1. Exercise 1: Apply a Homogeneous Kernel Map 
 * 
 * Homogeneous Kernel Map transforms data into compact linear representation 
 * such that applying a linear classifier approximates, to a high degree of accuracy, 
 * the application of a non-linear classifier over the original data.
 * 
 * using the HomogeneousKernelMap class with a KernelType.Chi2 kernel 
 * and WindowType.Rectangular window on top of the PHOWExtractor feature extractor
 * 
 * The resulting accuracy is a lot higher, around 0.85
 */

    	// construct an instance of PHOWExtractor
		HomogeneousKernelMap HKMap = new HomogeneousKernelMap(KernelType.Chi2, WindowType.Rectangular);
		extractor = HKMap.createWrappedExtractor(new PHOWExtractor(pdsift, assigner));

    	// use linear classifier by LiblinearAnnotator
    	ann = new LiblinearAnnotator<Record<FImage>, String>(
	        extractor, Mode.MULTICLASS, SolverType.L2R_L2LOSS_SVC, 1.0, 0.00001);
    	// train classifier
		ann.train(splits.getTrainingDataset());
		
		// perform automated evaluation for classifier’s accuracy
		eval = new ClassificationEvaluator<CMResult<String>, String, Record<FImage>>(
			ann, splits.getTestDataset(), new CMAnalyser<Record<FImage>, String>(CMAnalyser.Strategy.SINGLE));
			
		guesses = eval.evaluate();
		result = eval.analyse(guesses);

		System.out.println("Result using Homogeneous Kernel Map");
		// Get a String detailing the result
		System.out.println(result.getDetailReport());
		
		// Get a String summarising the result
		System.out.println(result.getSummaryReport());
    }
    
    // perform K-Means clustering on sample of SIFT features to build 
    // HardAssigner that can assign features to identifiers
    static HardAssigner<byte[], float[], IntFloatPair> trainQuantiser(
	    Dataset<Record<FImage>> sample, PyramidDenseSIFT<FImage> pdsift)
	{
		List<LocalFeatureList<ByteDSIFTKeypoint>> allkeys = new ArrayList<LocalFeatureList<ByteDSIFTKeypoint>>();
		
		for (Record<FImage> rec : sample) {
			FImage img = rec.getImage();
			
			pdsift.analyseImage(img);
			allkeys.add(pdsift.getByteKeypoints(0.005f));
		}
			
		// extract first 10000 dense SIFT features from images in dataset
		if (allkeys.size() > 10000)
			allkeys = allkeys.subList(0, 10000);
		
		// cluster them into 600 separate classes
		ByteKMeans km = ByteKMeans.createKDTreeEnsemble(600); //300
		DataSource<byte[]> datasource = new LocalFeatureListDataSource<ByteDSIFTKeypoint, byte[]>(allkeys);
		ByteCentroidsResult result = km.cluster(datasource);
		
		// returns HardAssigner to assign SIFT features to identifiers
		return result.defaultHardAssigner();
	}
    
    // FeatureExtractor implementation to train classifier
    // FeatureExtractor for exercise 3, PyramidSpatialAggregator
    static class PHOWExtractor implements FeatureExtractor<DoubleFV, Record<FImage>> {
        PyramidDenseSIFT<FImage> pdsift;
	    HardAssigner<byte[], float[], IntFloatPair> assigner;
	
	    public PHOWExtractor(PyramidDenseSIFT<FImage> pdsift, HardAssigner<byte[], float[], IntFloatPair> assigner)
	    {
	        this.pdsift = pdsift;
	        this.assigner = assigner;
	    }
	
	    public DoubleFV extractFeature(Record<FImage> object) {
	        FImage image = object.getImage();
	        pdsift.analyseImage(image);
	
	        BagOfVisualWords<byte[]> bovw = new BagOfVisualWords<byte[]>(assigner);
	        
	        // compute 4 histograms across the image (
	        // by breaking image into 2 both horizontally and vertically
	        // assign each Dense SIFT feature to a visual word and compute histogram
	        PyramidSpatialAggregator<byte[], SparseIntFV> spatial = new PyramidSpatialAggregator<byte[], SparseIntFV>(
	            bovw, 2, 2);
	
	        // from original code
	        // BlockSpatialAggregator<byte[], SparseIntFV> spatial = new BlockSpatialAggregator<byte[], SparseIntFV>(bovw, 2, 2);
	        
	        // spatial histograms appended together and normalised then returned
	        return spatial.aggregate(pdsift.getByteKeypoints(0.015f), image.getBounds()).normaliseFV();
	    }
    }
}
