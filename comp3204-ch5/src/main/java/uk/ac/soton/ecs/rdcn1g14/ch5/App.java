package uk.ac.soton.ecs.rdcn1g14.ch5;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.BasicMatcher;
import org.openimaj.feature.local.matcher.BasicTwoWayMatcher;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.transforms.HomographyRefinement;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.math.geometry.transforms.estimation.RobustHomographyEstimator;
import org.openimaj.math.model.fit.RANSAC;

/**
 * Chapter 5. SIFT and feature matching 
 * 
 * http://openimaj.org/tutorial/sift-and-feature-matching.html
 * 
 * @author Reuben Ng
 * @email rdcn1g14@soton.ac.uk
 * @version 1.1 23 Nov 2016
 */

public class App {
    public static void main( String[] args ) throws MalformedURLException, IOException {
    	
    	// load target and query images
    	MBFImage query = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/query.jpg"));
    	MBFImage target = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/target.jpg"));

    	//  difference-of-Gaussian feature detector with SIFT descriptor
    	// invariant to size changes, rotation, position
    	DoGSIFTEngine engine = new DoGSIFTEngine();	
    	LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(query.flatten());
    	LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(target.flatten());
    	
    	// construct and setup matcher
    	LocalFeatureMatcher<Keypoint> matcher = new BasicMatcher<Keypoint>(80);
    	matcher.setModelFeatures(queryKeypoints);
    	matcher.findMatches(targetKeypoints);
    	
    	// draw lines between matches
    	MBFImage basicMatches = MatchingUtilities.drawMatches(query, target, matcher.getMatches(), RGBColour.RED);
    	DisplayUtilities.displayName(basicMatches, "basicMatches");
    	
    	// set up RANSAC model fitter configured to find Affine Transforms and consistent match
    	RobustAffineTransformEstimator modelFitter = new RobustAffineTransformEstimator(5.0, 1500,
    		new RANSAC.PercentageInliersStoppingCondition(0.5));
    	matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
    		new FastBasicKeypointMatcher<Keypoint>(8), modelFitter);

    	matcher.setModelFeatures(queryKeypoints);
    	matcher.findMatches(targetKeypoints);

    	MBFImage consistentMatches = MatchingUtilities.drawMatches(query, target, matcher.getMatches(), 
    		RGBColour.RED);

    	// display matches
    	DisplayUtilities.displayName(consistentMatches, "consistentMatches");
    	
    	// draw polygon around estimated location of the query within the target
    	target.drawShape(
    		query.getBounds().transform(modelFitter.getModel().getTransform().inverse()), 3,RGBColour.BLUE);
    	DisplayUtilities.displayName(target, "target"); 
    	/**
    	 * 5.1.1. Exercise 1: Different matchers 
    	 * 
    	 * Using different matchers
    	 */

    	//BasicTwoWayMatcher
    	
    	// load target and query images
    	MBFImage target1 = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/target.jpg"));

    	// construct and setup matcher
    	LocalFeatureMatcher<Keypoint> TwoWay = new  BasicTwoWayMatcher<Keypoint>();
    	TwoWay.setModelFeatures(queryKeypoints);
    	TwoWay.findMatches(targetKeypoints);

    	// set up RANSAC model fitter configured to find Affine Transforms and consistent match
    	TwoWay = new ConsistentLocalFeatureMatcher2d<Keypoint>(
    		new FastBasicKeypointMatcher<Keypoint>(8), modelFitter);

    	TwoWay.setModelFeatures(queryKeypoints);
    	TwoWay.findMatches(targetKeypoints);

    	consistentMatches = MatchingUtilities.drawMatches(query, target1, TwoWay.getMatches(), 
    		RGBColour.RED);

    	// display matches
    	DisplayUtilities.displayName(consistentMatches, "Exercise 1: BasicTwoWayMatcher consistent Matches");
    	
    	// draw polygon around estimated location of the query within the target
    	target1.drawShape(
    		query.getBounds().transform(modelFitter.getModel().getTransform().inverse()), 3,RGBColour.BLUE);
    	DisplayUtilities.displayName(target1, "Exercise 1: BasicTwoWayMatcher target"); 
    	

    	// FastBasicKeypointMatcher

    	// load target and query images
    	MBFImage target2 = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/target.jpg"));

    	// construct and setup matcher
    	LocalFeatureMatcher<Keypoint> FastBasic = new  FastBasicKeypointMatcher<Keypoint>();
    	FastBasic.setModelFeatures(queryKeypoints);
    	FastBasic.findMatches(targetKeypoints);

    	// set up RANSAC model fitter configured to find Affine Transforms and consistent match
    	FastBasic = new ConsistentLocalFeatureMatcher2d<Keypoint>(
    		new FastBasicKeypointMatcher<Keypoint>(8), modelFitter);

    	FastBasic.setModelFeatures(queryKeypoints);
    	FastBasic.findMatches(targetKeypoints);

    	consistentMatches = MatchingUtilities.drawMatches(query, target2, FastBasic.getMatches(), 
    		RGBColour.RED);

    	// display matches
    	DisplayUtilities.displayName(consistentMatches, "Exercise 1: FastBasicKeypointMatcher consistent Matches");
    	
    	// draw polygon around estimated location of the query within the target
    	target2.drawShape(
    		query.getBounds().transform(modelFitter.getModel().getTransform().inverse()), 3,RGBColour.BLUE);
    	DisplayUtilities.displayName(target2, "Exercise 1: FastBasicKeypointMatcher target"); 

    	
    	/**
    	 * 5.1.2. Exercise 2: Different models 
    	 * 
    	 * Using different matchers
    	 */

    	// load target and query images
    	MBFImage target3 = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/target.jpg"));

    	// set up RANSAC model fitter 
    	RobustHomographyEstimator modelFitter2 = new RobustHomographyEstimator(5.0, 1500,
    		new RANSAC.PercentageInliersStoppingCondition(0.5), HomographyRefinement.NONE);
    	matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
    		new FastBasicKeypointMatcher<Keypoint>(8), modelFitter2);

    	matcher.setModelFeatures(queryKeypoints);
    	matcher.findMatches(targetKeypoints);

    	consistentMatches = MatchingUtilities.drawMatches(query, target3, matcher.getMatches(), 
    		RGBColour.RED);

    	// display matches
    	DisplayUtilities.displayName(consistentMatches, "Exercise 2 consistent Matches");

    	// draw polygon around estimated location of the query within the target
    	target3.drawShape(
    		query.getBounds().transform(modelFitter2.getModel().getTransform().inverse()), 3,RGBColour.BLUE);
    	DisplayUtilities.displayName(target3, "Exercise 2 target");
    }
}
