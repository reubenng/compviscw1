package uk.ac.soton.ecs.rdcn1g14.ch7;

import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.video.xuggle.XuggleVideo;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
// import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

/**
 * Chapter 7. Processing video
 * http://openimaj.org/tutorial/processing-video.html
 * 
 * @author Reuben Ng
 * @email rdcn1g14@soton.ac.uk
 * @version 1.1
 */

public class App {
    public static void main( String[] args ) throws MalformedURLException, VideoCaptureException {
    	
    	//Create a video
    	Video<MBFImage> video;

    	// load video from url
    	video = new XuggleVideo(new URL("http://static.openimaj.org/media/tutorial/keyboardcat.flv"));
    	
    	// use camera as video input 
    	// video = new VideoCapture(320, 240);
    	
        //Display the video
        VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(video);
        
        
        /*
        // process every frame with Canny edge detector
        for (MBFImage mbfImage : video) {
        	// display video in named window "videoFrames"
            DisplayUtilities.displayName(mbfImage.process(new CannyEdgeDetector()), "videoFrames");
        }
        
        //VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(video);
        // event driven display
        display.addVideoListener(
          new VideoDisplayListener<MBFImage>() {
        	// give video frame before rendering
            public void beforeUpdate(MBFImage frame) {
                frame.processInplace(new CannyEdgeDetector());
            }
            // display video after rendering
            public void afterUpdate(VideoDisplay<MBFImage> display) {
            }
          });
        */

/**
 * 7.1.1. Exercise 1: Applying different types of image processing to the video 
 * 
 */

        // event driven display
        display.addVideoListener(
          new VideoDisplayListener<MBFImage>() {
        	// give video frame before rendering
            public void beforeUpdate(MBFImage frame) {
            	// apply Gaussian blur to each frame
                frame.processInplace(new FGaussianConvolve(3f));
            }
            // display video after rendering
            public void afterUpdate(VideoDisplay<MBFImage> display) {
            }
          });
    }
}
