package org.richinet.dyndns.orm;

import java.util.ArrayList;
import java.util.HashMap;

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

	private String file;

	/**
	 * @return the file
	 */
	public String getFile() {
		return file;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile( String file ) {
		this.file = file;
	}

	private String imageFilename;

	/**
	 * @return the imageFilename
	 */
	public String getImageFilename() {
		return imageFilename;
	}

	/**
	 * @param imageFilename
	 *            the imageFilename to set
	 */
	public void setImageFilename( String imageFilename ) {
		this.imageFilename = imageFilename;
		setHasImage( true );
	}

	int imageWidth = 0;

	/**
	 * @return the imageWidth
	 */
	public int getImageWidth() {
		return imageWidth;
	}

	/**
	 * @param imageWidth
	 *            the imageWidth to set
	 */
	public void setImageWidth( int imageWidth ) {
		this.imageWidth = imageWidth;
	}

	/**
	 * @return the imageHeight
	 */
	public int getImageHeight() {
		return imageHeight;
	}

	/**
	 * @param imageHeight
	 *            the imageHeight to set
	 */
	public void setImageHeight( int imageHeight ) {
		this.imageHeight = imageHeight;
	}

	int imageHeight = 0;

	boolean hasImage = false;

	/**
	 * @return the image
	 */
	public boolean hasImage() {
		return hasImage;
	}

	/**
	 * @param image
	 *            the image to set
	 */
	public void setHasImage( boolean image ) {
		hasImage = image;
	}

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

	// Will be used by the ArrayAdapter in the ListView
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
