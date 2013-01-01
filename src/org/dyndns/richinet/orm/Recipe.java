package org.dyndns.richinet.orm;

import java.util.ArrayList;

import android.util.Log;

public class Recipe {

	/**
	 * Tag for logging
	 */
	private static final String TAG = "Recipe";

	private String title;

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle( String title ) {
		this.title = title;
	}

	/**
	 * The filename of the recipe such as Rcp123.htm
	 */
	private String file;

	/**
	 * @return the file of the recipe as a string
	 */
	public String getFile() {
		return file;
	}

	/**
	 * Sets the file of the recipe such as Rcp123.htm
	 * 
	 * @param file
	 *            the file to set
	 */
	public void setFile( String file ) {
		this.file = file;
	}

	/**
	 * The filename of the image of the recipe
	 */
	private String imageFilename;

	/**
	 * Returns the filename of the image of the recipe
	 * 
	 * @return the imageFilename
	 */
	public String getImageFilename() {
		return imageFilename;
	}

	/**
	 * Saves the filename of the image of the recipes like Rcp123.jpg
	 * 
	 * @param imageFilename
	 *            the imageFilename to set
	 */
	public void setImageFilename( String imageFilename ) {
		this.imageFilename = imageFilename;
		setHasImage( true );
	}

	/**
	 * The width of the image
	 */
	private int imageWidth = 0;

	/**
	 * Returns the width of the image
	 * 
	 * @return the imageWidth
	 */
	public int getImageWidth() {
		return imageWidth;
	}

	/**
	 * Sets the width of the image
	 * 
	 * @param imageWidth
	 *            the imageWidth to set
	 */
	public void setImageWidth( int imageWidth ) {
		this.imageWidth = imageWidth;
	}

	/**
	 * Returns the image height
	 * 
	 * @return the imageHeight
	 */
	public int getImageHeight() {
		return imageHeight;
	}

	/**
	 * Sets the image height
	 * 
	 * @param imageHeight
	 *            the imageHeight to set
	 */
	public void setImageHeight( int imageHeight ) {
		this.imageHeight = imageHeight;
	}

	/**
	 * The image height
	 */
	private int imageHeight = 0;

	/**
	 * A flag whether the recipe has an image
	 */
	private boolean hasImage = false;

	/**
	 * Returns whether the recipe has an image
	 * 
	 * @return true if the recipe has an image
	 */
	public boolean hasImage() {
		return hasImage;
	}

	/**
	 * Sets whether the recipe has an image
	 * 
	 * @param hasImage
	 *            set to true if the recipe has an image
	 * 
	 */
	public void setHasImage( boolean hasImage ) {
		this.hasImage = hasImage;
	}

	/**
	 * A list of classifications that this recipe holds
	 */
	private ArrayList<CategoryPair> classifications = new ArrayList<CategoryPair>();

	/**
	 * @return the classifications
	 */
	public ArrayList<CategoryPair> getClassifications() {
		return classifications;
	}

	/**
	 * @param
	 */
	public void addClassifications( String key, String value ) {
		classifications.add( new CategoryPair( key, value ) );
	}

	/**
	 * Returns the name of the recipe. Is used by the ArrayAdapter in the
	 * resultScroller activity
	 */
	@Override
	public String toString() {
		return getTitle();
	}

	/**
	 * This method writes the data in the recipe to the log file.
	 */
	public void dumpToLog() {
		Log.d( TAG, "Title: " + getTitle() );
		Log.d( TAG, "File: " + getFile() );
		Log.d( TAG, "ImageFilename: " + getImageFilename() );
		Log.d( TAG, "Width: " + getImageWidth() );
		Log.d( TAG, "Height: " + getImageHeight() );
		Log.d( TAG, "HasImage: " + ( hasImage() ? "true" : "false" ) );
		for ( CategoryPair classification : classifications ) {
			Log.d( TAG, String.format( "%s: %s", classification.getCategory(),
					classification.getMember() ) );
		}
	}
}
