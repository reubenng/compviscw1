package uk.ac.soton.ecs.rdcn1g14.ch3;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.connectedcomponent.GreyscaleConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.PixelProcessor;
import org.openimaj.image.segmentation.FelzenszwalbHuttenlocherSegmenter;
import org.openimaj.image.segmentation.SegmentationUtilities;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;

/**
 * Chapter 3. Introduction to clustering, segmentation and connected components 
 * 
 * http://openimaj.org/tutorial/introduction-to-clustering-segmentation-and-connected-components.html
 * 
 * @author Reuben Ng
 * @email rdcn1g14@soton.ac.uk
 * @version 1.0 17 Nov 2016
 */

public class App {
    public static void main( String[] args ) throws MalformedURLException, IOException {
    	
    	// load image from url
    	MBFImage input = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/sinaface.jpg"));

    	// apply colour-space transform to image
    	input = ColourSpace.convert(input, ColourSpace.CIE_Lab);
    	
    	// K-Means algorithm, 2 clusters/classes, 30 iterations by default
    	FloatKMeans cluster = FloatKMeans.createExact(2);
    	
    	// flatten image pixels
    	float[][] imageData = input.getPixelVectorNative(new float[input.getWidth() * input.getHeight()][3]);
    	
    	// run K-Means algorithm
    	FloatCentroidsResult result = cluster.cluster(imageData);
    	
    	// print coordinates of centroid of each cluster
    	final float[][] centroids = result.centroids;
    	for (float[] fs : centroids) {
    	    System.out.println(Arrays.toString(fs));
    	}
    	
    	// Classification
    	// return an assigner
    	final HardAssigner<float[],?,?> assigner = result.defaultHardAssigner();
    	for (int y=0; y<input.getHeight(); y++) {
    	    for (int x=0; x<input.getWidth(); x++) {
    	    	// LAB values of pixel
    	        float[] pixel = input.getPixelNative(x, y);
    	        // return index of the pixel's cluster
    	        int centroid = assigner.assign(pixel);
    	        // set pixel to centroid of its cluster
    	        input.setPixelNative(x, y, centroids[centroid]);
    	    }
    	}
    	
    	// convert image back to RGB colour space
    	input = ColourSpace.convert(input, ColourSpace.RGB);
        // Display the image
    	DisplayUtilities.displayName(input,"Chapter 3");
    	
    	// find connected components
    	GreyscaleConnectedComponentLabeler labeler = new GreyscaleConnectedComponentLabeler();
    	List<ConnectedComponent> components = labeler.findComponents(input.flatten());
    	
    	// draw texts for component label
    	int i = 0;
    	for (ConnectedComponent comp : components) {
    		// if connected component more than 50 pixels
    	    if (comp.calculateArea() < 50) 
    	        continue;
    	    // draw text
    	    input.drawText("Point:" + (i++), comp.calculateCentroidPixel(), HersheyFont.TIMES_MEDIUM, 20);
    	}

        // Display the image
    	DisplayUtilities.displayName(input,"Chapter 3: Numbered components");
    	

/**
 * 3.1.1. Exercise 1: The PixelProcessor 
 * 
 * Re-implement to replace 2 for loops with a PixelProcessor
 * 
 * Using 2 for loops is much easier and simpler without the need to convert float
 * 
 * but once processInplace is implemented, it can be reused easily with a oneliner.
 * 
 */


    	// load image from url
    	input = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/sinaface.jpg"));

    	// apply colour-space transform to image
    	input = ColourSpace.convert(input, ColourSpace.CIE_Lab);
    	
    	// Classification
    	
    	input.processInplace(new PixelProcessor<Float[]>() {
    	    public Float[] processPixel(Float[] pixel) {
    	    	// convert to float, assigner only take float
    	    	float[] newpixel = new float[pixel.length];
    	    	// reassign all float values
    	    	for(int i=0; i < newpixel.length; i++) {
    	    		newpixel[i] = pixel[i].floatValue();
    	    	}
    	    	
    	        // return index of the pixel's cluster
    	        int centroid = assigner.assign(newpixel);

    	        // new float for new centroid
    	        float[] newcentroid = centroids[centroid];
    	        
    	        // convert back to Float
    	        Float[] processedPixel = new Float[pixel.length];
    	        
    	    	// set all pixel to centroid of its cluster
    	    	for(int i=0; i < processedPixel.length; i++) {
    	    		processedPixel[i] = newcentroid[i];
    	    	}
				return processedPixel;
    	    }
    	});
    	
    	// convert image back to RGB colour space
    	input = ColourSpace.convert(input, ColourSpace.RGB);
    	
    	// find connected components
    	components = labeler.findComponents(input.flatten());
    	
    	// draw texts for component label
    	i = 0;
    	for (ConnectedComponent comp : components) {
    		// if connected component more than 50 pixels
    	    if (comp.calculateArea() < 50) 
    	        continue;
    	    // draw text
    	    input.drawText("Point:" + (i++), comp.calculateCentroidPixel(), HersheyFont.TIMES_MEDIUM, 20);
    	}

        // Display the image
    	DisplayUtilities.displayName(input,"3.1.1. Exercise 1: The PixelProcessor");
    	

/**
 * 3.1.2. Exercise 2: A real segmentation algorithm 
 * 
 * using  FelzenszwalbHuttenlocherSegmenter
 * 
 */

    	// reload image from url
    	input = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/sinaface.jpg"));

    	// apply colour-space transform to image
    	//input = ColourSpace.convert(input, ColourSpace.CIE_Lab);
    	
    	// create new segmenter
    	FelzenszwalbHuttenlocherSegmenter<MBFImage> FHSeg = new FelzenszwalbHuttenlocherSegmenter<MBFImage>();
    	
    	// apply segmenter on image
    	components = FHSeg.segment(input);
    	// draw the connected components produced by the segmenter
    	SegmentationUtilities.renderSegments(input, components);
        // Display the image
    	DisplayUtilities.displayName(input,"Exercise 2: A real segmentation algorithm ");
    }
}
