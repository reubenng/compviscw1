package uk.ac.soton.ecs.rdcn1g14.ch1;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.typography.hershey.HersheyFont;

/**
 * Chapter 1. Getting started with OpenIMAJ using Maven 
 * http://openimaj.org/tutorial/exercises.html
 * 
 * @author Reuben Ng
 * @email rdcn1g14@soton.ac.uk
 * @version 1.0
 */

public class App {
    public static void main( String[] args ) {
    	//Create an image
        MBFImage image = new MBFImage(320,70, ColourSpace.RGB);

        //Fill the image with white
        image.fill(RGBColour.WHITE);
        		        
        //Render some test into the image
        image.drawText("Hello World", 10, 60, HersheyFont.CURSIVE, 50, RGBColour.BLACK);

        //Apply a Gaussian blur
        image.processInplace(new FGaussianConvolve(2f));
        
        //Display the image
        DisplayUtilities.display(image);

/**
 * 1.2.1. Exercise 1: Playing with the sample application 
 * 
 * render words with different font and colour
 * 
 */


    	//Create an image
        image = new MBFImage(550,70, ColourSpace.RGB);

        //Fill the image with white
        image.fill(RGBColour.BLACK);
        		        
        //Render some test into the image
        image.drawText("OpenIMAJ Exercise 1", 10, 60, HersheyFont.FUTURA_LIGHT, 50, RGBColour.YELLOW);

        //Apply a Gaussian blur
        image.processInplace(new FGaussianConvolve(2f));
        
        //Display the image
        DisplayUtilities.display(image);

    }
}