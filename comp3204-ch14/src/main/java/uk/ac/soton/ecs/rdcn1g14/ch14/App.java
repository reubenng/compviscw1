package uk.ac.soton.ecs.rdcn1g14.ch14;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.time.Timer;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.util.parallel.partition.RangePartitioner;
import org.openimaj.util.function.Operation;

/**
 * Chapter 14. Parallel Processing
 * 
 * http://openimaj.org/tutorial/parallel-processing.html
 * 
 * @author Reuben Ng
 * @email rdcn1g14@soton.ac.uk
 * @version 1.0 24 Nov 2016
 */

public class App {
    public static void main( String[] args ) throws IOException {
    	
    	// equivalent to for (int i=0; i<10; i++) loop
    	Parallel.forIndex(0, 10, 1, new Operation<Integer>() {
    		public void perform(Integer i) {
    		    System.out.println(i);
    		}
    	});
    	
    	/**
    	 * Compute the normalised average of the images in a dataset
    	 * 
    	 * Non-parallel version
    	 * Run time around 22s
    	 * */
    	
    	// load image from dataset
    	VFSGroupDataset<MBFImage> allImages = Caltech101.getImages(ImageUtilities.MBFIMAGE_READER);
    	
    	// use a subset of first 8 groups
    	GroupedDataset<String, ListDataset<MBFImage>, MBFImage> images = GroupSampler.sample(allImages, 8, false);
    	
    	final List<MBFImage> output = new ArrayList<MBFImage>();
    	final ResizeProcessor resize = new ResizeProcessor(200);
    	
    	//set timer
    	Timer t1 = Timer.timer();
    	
    	// for every group
    	for (ListDataset<MBFImage> clzImages : images.values()) {
    	    MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);

    	// loop through images in the group
    	    for (MBFImage i : clzImages) {
    	    	// create white image
    	        MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);
    	        tmp.fill(RGBColour.WHITE);

    	        // normalise image
    	        MBFImage small = i.process(resize).normalise();
    	        // draw image at the centre of white image
    	        int x = (200 - small.getWidth()) / 2;
    	        int y = (200 - small.getHeight()) / 2;
    	        tmp.drawImage(small, x, y);

    	        current.addInplace(tmp);
    	    }
    	    // divide accumulated image by the number of samples used to create it
    	    current.divideInplace((float) clzImages.size());
    	    // add to accumulator
    	    output.add(current);
    	}
    	
    	// display resultant averaged images
    	//DisplayUtilities.display("Images", output);
    	// print time taken
    	System.out.println("Non-parallel Time: " + t1.duration() + "ms");

    	/**
    	 * Compute the normalised average of the images in a dataset
    	 * 
    	 * Parallel version
    	 * Run time around 12200ms
    	 * */
    	
    	Timer t2 = Timer.timer();
    	
    	for (ListDataset<MBFImage> clzImages : images.values()) {
    	    final MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);

    	    // parallelise inner loop
    	    Parallel.forEach(clzImages, new Operation<MBFImage>() {
    	        public void perform(MBFImage i) {
    	            final MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);
    	            tmp.fill(RGBColour.WHITE);

    	            final MBFImage small = i.process(resize).normalise();
    	            final int x = (200 - small.getWidth()) / 2;
    	            final int y = (200 - small.getHeight()) / 2;
    	            tmp.drawImage(small, x, y);

    	            // prevent multiple threads trying to alter the image concurrently
    	            synchronized (current) {
    	                current.addInplace(tmp);
    	            }
    	        }
    	    });
    	    current.divideInplace((float) clzImages.size());
    	    output.add(current);
    	}

    	System.out.println("Parallel Time: " + t2.duration() + "ms");

    	/**
    	 * Compute the normalised average of the images in a dataset
    	 * 
    	 * Improved parallel version
    	 * use the partitioned variant of the for-each loop in the Parallel class
    	 * the partitioned variant will feed each thread a collection of images to process
    	 * 
    	 * Run time around 12000ms
    	 * */
    	
    	Timer t3 = Timer.timer();
    	
    	for (ListDataset<MBFImage> clzImages : images.values()) {
    	    final MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);

    	    // RangePartitioner break images in clzImages into as many 
    	    // (approximately equally sized) chunks as there are available CPU cores
    	    Parallel.forEachPartitioned(new RangePartitioner<MBFImage>(clzImages), new Operation<Iterator<MBFImage>>() {
    	    	// perform method increase task granularity
    	    	public void perform(Iterator<MBFImage> it) {
    	    		// hold intermediary results
    	    	    MBFImage tmpAccum = new MBFImage(200, 200, 3);
    	    	    MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);

    	    	    while (it.hasNext()) {
    	    	        final MBFImage i = it.next();
    	    	        tmp.fill(RGBColour.WHITE);

    	    	        final MBFImage small = i.process(resize).normalise();
    	    	        final int x = (200 - small.getWidth()) / 2;
    	    	        final int y = (200 - small.getHeight()) / 2;
    	    	        tmp.drawImage(small, x, y);
    	    	        tmpAccum.addInplace(tmp);
    	    	    }
    	    	    synchronized (current) {
    	    	        current.addInplace(tmpAccum);
    	    	    }
    	    	}
    	    });
    	    current.divideInplace((float) clzImages.size());
    	    output.add(current);
    	}

    	System.out.println("Improved parallel Time: " + t3.duration() + "ms");
    	
    	//DisplayUtilities.display("Images", output);
    	

/**
 * 14.1.1. Exercise 1: Parallelise the outer loop
 * 
 * parallelise the outer loop instead of inner loop
 * 
 * Run time around 12000ms
 * Slower than parallelising innner loop due to the inner loop has more tasks to perform.
 */
    	//set timer
    	Timer t4 = Timer.timer();

    	// loop through images in the group
    	//for (ListDataset<MBFImage> clzImages : images.values()) {
    	Parallel.forEach(images.values(), new Operation<ListDataset<MBFImage>>() {
			public void perform(ListDataset<MBFImage> clzImages) {
    			
	    	    MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);
	
	    	    for (MBFImage i : clzImages) {
	    	    	// create white image
	    	        MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);
	    	        tmp.fill(RGBColour.WHITE);
	
	    	        // normalise image
	    	        MBFImage small = i.process(resize).normalise();
	    	        // draw image at the centre of white image
	    	        int x = (200 - small.getWidth()) / 2;
	    	        int y = (200 - small.getHeight()) / 2;
	    	        tmp.drawImage(small, x, y);
	
	    	        current.addInplace(tmp);
	    	    }
	    	    // divide accumulated image by the number of samples used to create it
	    	    current.divideInplace((float) clzImages.size());
	    	    
	    	    // add to accumulator
	    	    synchronized (output) {
	    	    	output.add(current);
				}
    		}
    	});
    	
    	// display resultant averaged images
    	DisplayUtilities.display("Images", output);
    	// print time taken
    	System.out.println("Outer parallel Time: " + t4.duration() + "ms");

    }
}
