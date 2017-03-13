package uk.ac.soton.ecs.rdcn1g14.ch2;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.image.MBFImage;
import org.openimaj.image.ImageUtilities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Chapter 2. Processing your first image
 * http://openimaj.org/tutorial/processing-your-first-image.html
 * 
 * @author Reuben Ng
 * @email rdcn1g14@soton.ac.uk
 * @version 1.0
 */

public class App {
  public static void main( String[] args ) throws MalformedURLException, IOException {
  	// load image from url
  	MBFImage image = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/sinaface.jpg"));

  	System.out.println(image.colourSpace); // RGB
  	
  	DisplayUtilities.display(image);
  	DisplayUtilities.display(image.getBand(0), "Red Channel");
  	
  	// set all blue and green pixels to black
  	MBFImage clone = image.clone();
  	for (int y=0; y<image.getHeight(); y++) {
  	    for(int x=0; x<image.getWidth(); x++) {
  	        clone.getBand(1).pixels[y][x] = 0;
  	        clone.getBand(2).pixels[y][x] = 0;
  	    }
  	}
  	DisplayUtilities.display(clone);
  	
  	// same as above
  	clone.getBand(1).fill(0f);
  	clone.getBand(2).fill(0f);
  	DisplayUtilities.display(image);
  	
  	image.processInplace(new CannyEdgeDetector());
  	DisplayUtilities.display(image);
  	
  	image.drawShapeFilled(new Ellipse(700f, 450f, 20f, 10f, 0f), RGBColour.WHITE);
  	image.drawShapeFilled(new Ellipse(650f, 425f, 25f, 12f, 0f), RGBColour.WHITE);
  	image.drawShapeFilled(new Ellipse(600f, 380f, 30f, 15f, 0f), RGBColour.WHITE);
  	image.drawShapeFilled(new Ellipse(500f, 300f, 100f, 70f, 0f), RGBColour.WHITE);
  	image.drawText("OpenIMAJ is", 425, 300, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
  	image.drawText("Awesome", 425, 330, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
  	DisplayUtilities.display(image);

/**
 * 2.1.1. Exercise 1: DisplayUtilities
 * 	http://openimaj.org/tutorial/processing-your-first-image.html
 * 
 * 	Same as above except only 1 window will open.
 * 
 */

 // load image from url
   	image = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/sinaface.jpg"));

   	System.out.println(image.colourSpace); // RGB

   	// create window named "window1", titled "Exercise 2.1.1"
   	DisplayUtilities.createNamedWindow("window1", "Exercise 2.1.1");
   	
   	// display image in "window1"
   	DisplayUtilities.displayName(image, "window1");
   	// DisplayUtilities.display(image.getBand(0), "Red Channel");
   	
   	// set all blue and green pixels to black
   	clone = image.clone();
   	for (int y=0; y<image.getHeight(); y++) {
   	    for(int x=0; x<image.getWidth(); x++) {
   	        clone.getBand(1).pixels[y][x] = 0;
   	        clone.getBand(2).pixels[y][x] = 0;
   	    }
   	}
   	DisplayUtilities.displayName(clone, "window1");
   	
   	// same as above
   	clone.getBand(1).fill(0f);
   	clone.getBand(2).fill(0f);
   	DisplayUtilities.displayName(image, "window1");
   	
   	image.processInplace(new CannyEdgeDetector());
   	DisplayUtilities.displayName(image, "window1");
   	
   	image.drawShapeFilled(new Ellipse(700f, 450f, 20f, 10f, 0f), RGBColour.WHITE);
   	image.drawShapeFilled(new Ellipse(650f, 425f, 25f, 12f, 0f), RGBColour.WHITE);
   	image.drawShapeFilled(new Ellipse(600f, 380f, 30f, 15f, 0f), RGBColour.WHITE);
   	image.drawShapeFilled(new Ellipse(500f, 300f, 100f, 70f, 0f), RGBColour.WHITE);
   	image.drawText("OpenIMAJ is", 425, 300, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
   	image.drawText("Awesome", 425, 330, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
   	DisplayUtilities.displayName(image, "window1");
   	

/**
 * 2.1.2. Exercise 2: Drawing 
 * http://openimaj.org/tutorial/processing-your-first-image.html
 * 
 * 	Give speech bubbles borders.
 * 
 */

    
  	// reload image from url
  	image = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/sinaface.jpg"));

  	// create window named "window1", titled "Exercise 2.1.1"
  	DisplayUtilities.createNamedWindow("window2", "Exercise 2.1.2");

  	// apply Canny edge detector to image
  	image.processInplace(new CannyEdgeDetector());
  	
  	// draw yellow ellipses at the bottom
  	image.drawShapeFilled(new Ellipse(700f, 450f, 25f, 15f, 0f), RGBColour.YELLOW);
  	image.drawShapeFilled(new Ellipse(650f, 425f, 30f, 17f, 0f), RGBColour.YELLOW);
  	image.drawShapeFilled(new Ellipse(600f, 380f, 35f, 20f, 0f), RGBColour.YELLOW);
  	image.drawShapeFilled(new Ellipse(500f, 300f, 105f, 75f, 0f), RGBColour.YELLOW);
  	
  	// white ellipses on top
  	image.drawShapeFilled(new Ellipse(700f, 450f, 20f, 10f, 0f), RGBColour.WHITE);
  	image.drawShapeFilled(new Ellipse(650f, 425f, 25f, 12f, 0f), RGBColour.WHITE);
  	image.drawShapeFilled(new Ellipse(600f, 380f, 30f, 15f, 0f), RGBColour.WHITE);
  	image.drawShapeFilled(new Ellipse(500f, 300f, 100f, 70f, 0f), RGBColour.WHITE);

  	// draw ellipses with red lines
	image.drawShape(new Ellipse(700f, 450f, 20f, 10f, 0f), 6, RGBColour.RED);
	image.drawShape(new Ellipse(650f, 425f, 25f, 12f, 0f), 6, RGBColour.RED);
	image.drawShape(new Ellipse(600f, 380f, 30f, 15f, 0f), 6, RGBColour.RED);
	image.drawShape(new Ellipse(500f, 300f, 100f, 70f, 0f), 6, RGBColour.RED);

  	// overlay ellipses with thin blue lines
	image.drawShape(new Ellipse(700f, 450f, 20f, 10f, 0f), 3, RGBColour.BLUE);
	image.drawShape(new Ellipse(650f, 425f, 25f, 12f, 0f), 3, RGBColour.BLUE);
	image.drawShape(new Ellipse(600f, 380f, 30f, 15f, 0f), 3, RGBColour.BLUE);
	image.drawShape(new Ellipse(500f, 300f, 100f, 70f, 0f), 3, RGBColour.BLUE);
	
  	// draw texts
  	image.drawText("OpenIMAJ is", 425, 300, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
  	image.drawText("Awesome", 425, 330, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
  	
  	// display image in window1
  	DisplayUtilities.displayName(image, "window2");
  }
}