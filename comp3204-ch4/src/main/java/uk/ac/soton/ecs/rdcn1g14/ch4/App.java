package uk.ac.soton.ecs.rdcn1g14.ch4;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.math.statistics.distribution.MultidimensionalHistogram;

/**
 * Chapter 4. Global image features  
 * 
 * http://openimaj.org/tutorial/global-image-features.html
 * 
 * @author Reuben Ng
 * @email rdcn1g14@soton.ac.uk
 * @version 1.1 23 Nov 2016
 */

public class App {
    public static void main( String[] args ) throws IOException {
    	
    	// load 3 images
    	URL[] imageURLs = new URL[] {
    			   new URL( "http://users.ecs.soton.ac.uk/dpd/projects/openimaj/tutorial/hist1.jpg" ),
    			   new URL( "http://users.ecs.soton.ac.uk/dpd/projects/openimaj/tutorial/hist2.jpg" ), 
    			   new URL( "http://users.ecs.soton.ac.uk/dpd/projects/openimaj/tutorial/hist3.jpg" ) 
    			};

    	List<MultidimensionalHistogram> histograms = new ArrayList<MultidimensionalHistogram>();
    	// creates histogram with 64 (4 × 4 × 4) bins
    	// returns normalised histogram
    	HistogramModel model = new HistogramModel(4, 4, 4);

    	
    	// store histograms
    	for( URL u : imageURLs ) {
    	    model.estimateModel(ImageUtilities.readMBF(u));
    	    histograms.add( model.histogram.clone() );
    	}
    		
        // compare histograms with Euclidean distance
    	for( int i = 0; i < histograms.size(); i++ ) {
    		   for( int j = i; j < histograms.size(); j++ ) {
    	       double distance = histograms.get(i).compare( histograms.get(j), DoubleFVComparison.EUCLIDEAN );
    	       System.out.println("Histogram " + i + " and " + j + " distance: " + distance);
    	    }
    	}
    	
    	/*
    	 * 4.1.1. Exercise 1: Finding and displaying similar images 
    	 * 
    	 * Judging by the Eucliden distance between histograms,
    	 * histogram 0 and 1 are the most similar.
    	 * Both have high amount of red colour.
    	 */
    	
    	MBFImage image1 = ImageUtilities.readMBF(imageURLs[0]);
    	MBFImage image2 = ImageUtilities.readMBF(imageURLs[1]);
    	MBFImage image3 = ImageUtilities.readMBF(imageURLs[2]);
    	
    	MBFImage[] images = {image1, image2, image3};
    	
    	double shortest = 1;
    	int histx = 0;
    	int histy = 0;
    	// compare histograms with Euclidean distance
    	for( int i = 0; i < histograms.size(); i++ ) {
    		   for( int j = 0; j < histograms.size(); j++ ) {
    	       double distance = histograms.get(i).compare( histograms.get(j), DoubleFVComparison.EUCLIDEAN );
    	       
    	       if( i == j ){
    	    	   continue;
    	    	   }
    	       else{
    	    	   if( distance < shortest ){
    	    		   
	    	    	   shortest = distance;
	    	    	   histx = i;
	    	    	   histy = j;
    	    	   }
    	       }
    	    }
    	}
    	
    	System.out.println("Histogram " + histx + " and " + histy + " most similar");
    	
    	// display both images
    	DisplayUtilities.displayName(images[histx], "Image " + histx);
    	DisplayUtilities.displayName(images[histy], "Image " + histy);
    	
    	/*
    	 * 4.1.2. Exercise 2: Exploring comparison measures
    	 * 
    	 * Using different comparison measure
    	 * 
    	 * DoubleFVComparison.INTERSECTION gives 1 or very close to 1 (0.9999)
    	 * when images are the same, thus big number indicates similar images
    	 * 
    	 */
    	
    	// compare histograms with Euclidean distance
    	for( int i = 0; i < histograms.size(); i++ ) {
    		   for( int j = i; j < histograms.size(); j++ ) {
    	       double distance = histograms.get(i).compare( histograms.get(j), DoubleFVComparison.INTERSECTION );
    	       System.out.println("Histogram " + i + " and " + j + " distance: " + distance);
    	    }
    	}
    	
    }
}
