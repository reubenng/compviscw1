package uk.ac.soton.ecs.rdcn1g14.ch6;

import java.util.Map.Entry;

import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.dataset.BingImageDataset;
import org.openimaj.image.dataset.FlickrImageDataset;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.BingAPIToken;
import org.openimaj.util.api.auth.common.FlickrAPIToken;

/**
 * Chapter 6. Image Datasets 
 * 
 * http://openimaj.org/tutorial/image-datasets.html
 * 
 * @author Reuben Ng
 * @email rdcn1g14@soton.ac.uk
 * @version 1.2 26 Nov 2016
 */

public class App {
    public static void main( String[] args ) throws Exception {
        
    	// path to directory, change directory path accordingly
        VFSListDataset<FImage> images = 
        		new VFSListDataset<FImage>("/home/reuben/OpenIMAJ/cw1/ch6/data", ImageUtilities.FIMAGE_READER);
        // print number of items in dataset
        System.out.println(images.size());
        
        // display random image
        DisplayUtilities.display(images.getRandomInstance(), "A random image from the dataset");
        
        // Display the image in window
        DisplayUtilities.display("My images", images);
        
        // creates image dataset from zip file on web-server
        VFSListDataset<FImage> faces = 
        		new VFSListDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER);
        // display all faces
        DisplayUtilities.display("ATT faces", faces);
        
        // maintain associations
        VFSGroupDataset<FImage> groupedFaces = 
        		new VFSGroupDataset<FImage>( "zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER);
        
        // display images from each individual in a window
        for (final Entry<String, VFSListDataset<FImage>> entry : groupedFaces.entrySet()) {
        	DisplayUtilities.display(entry.getKey(), entry.getValue());
        }
        
        // search for cats on flickr
        //FlickrAPIToken flickrToken = new FlickrAPIToken("edab4ab3f5c40297de4ade10741934b3", "6dcab8225c10dd37e");
        FlickrAPIToken flickrToken = DefaultTokenFactory.get(FlickrAPIToken.class);
        FlickrImageDataset<FImage> cats = 
        		FlickrImageDataset.create(ImageUtilities.FIMAGE_READER, flickrToken, "cat", 10);
        DisplayUtilities.display("Cats", cats);

/**
 * 6.1.1. Exercise 1: Exploring Grouped Datasets 
 */

       // for every entry in groupedFaces
        for (final Entry<String, VFSListDataset<FImage>> entry : groupedFaces.entrySet()) {
			 // display random images fron each individual in a window
            DisplayUtilities.display(groupedFaces.getRandomInstance(), entry.getKey());
        }

/**
 * 6.1.2. Exercise 2: Find out more about VFS datasets  
 * 
 * Other supported sources for building datasets listed on the Commons VFS includes
 * HTTP and HTTPS, WebDAV, FTP and FTPS, Temporary Files, ram etc..
 * 
 * See
 * https://commons.apache.org/proper/commons-vfs/filesystems.html
 * 
 */
    	
        
/**
 * 6.1.3. Exercise 3: Try the BingImageDataset dataset 
 */
    	// api token
    	//BingAPIToken bingToken = new BingAPIToken("a11ffe90e1114c38ad0aa5f581158a37");
		BingAPIToken bingToken = DefaultTokenFactory.get(BingAPIToken.class);
        // search for Emma Watson on Bing
		BingImageDataset<FImage> emma = BingImageDataset.create(ImageUtilities.FIMAGE_READER, bingToken,
				"Emma Watson", 10);
		DisplayUtilities.display("Exercise 3: Try the BingImageDataset dataset", emma);  
        
/**
 * 6.1.4. Exercise 4: Using MapBackedDataset 
 */
        // search for Elon Musk on Bing
		BingImageDataset<FImage> musk = 
				BingImageDataset.create(ImageUtilities.FIMAGE_READER, bingToken, "Elon Musk", 10);
        
        // search for Benedict Cumberbatch on Bing
        BingImageDataset<FImage> cumberbatch = 
        		BingImageDataset.create(ImageUtilities.FIMAGE_READER, bingToken, "Benedict Cumberbatch", 10);
        
        // construct grouped dataset of images of 3 famous people
        MapBackedDataset<String, BingImageDataset<FImage>, FImage> people = MapBackedDataset.of(emma, musk, cumberbatch);
        // display random image from MapBackedDataset
		DisplayUtilities.display("Exercise 4: Using MapBackedDataset ", people.getRandomInstance()); 
		
    }
}
