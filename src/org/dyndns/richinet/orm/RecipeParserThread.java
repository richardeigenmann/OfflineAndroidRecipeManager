package org.dyndns.richinet.orm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class RecipeParserThread extends Thread {

	/**
	 * Tag for the logger
	 */
	private static final String TAG = "RecipeParserThread";

	//StaticAppStuff app;

	private RecipesDataSource datasource;
	private Handler handler = new Handler();
	private DownloaderInterface downloaderCallback;
	private Context context;

	public RecipeParserThread( Context context,
			DownloaderInterface downloaderCallback ) {
		this.context = context;
		this.downloaderCallback = downloaderCallback;
	}

	/**
	 * Handle to the application
	 */

	@Override
	public void run() {
		String fullUrl = StaticAppStuff.getRecipesDataUrl( context );
		ArrayList<String> lines = null;
		try {
			lines = HttpRetriever.retrieveFromURL( fullUrl );
		} catch ( IllegalArgumentException e ) {
			e.printStackTrace();
			Log.e( TAG, "IllegalArgumentException: " + e.toString() );
		} catch ( IOException e ) {
			e.printStackTrace();
			Log.e( TAG, "IOException: " + e.toString() );
		}

		datasource = new RecipesDataSource( context );
		datasource.open();
		int parsedRecipes = parse( lines );
		if ( parsedRecipes > 0 ) {
			StaticAppStuff.saveLastDownloadTimestamp( context, new Date() );
		}
		Log.d( TAG, "Finished downloading %d recipes" );
		datasource.close();
		progressDone();
	}

	private static final Pattern newRecipePattern = Pattern
			.compile( "(Rcp.*htm)" );
	private static final Pattern titlePattern = Pattern
			.compile( "<title>(.*)</title>" );
	private static final Pattern metaPattern = Pattern
			.compile( "<META name=\"RCP-(.*)\" content=\"(.*)\"" );
	private static final Pattern imgPattern = Pattern
			.compile( "<img src=\"(.*)\".*alt=.* width=\"([0-9]+)\".* height=\"([0-9]+)\".*>" );
	private static final Pattern eofPattern = Pattern.compile( "===" );

	private boolean eofFound = false;

	/**
	 * Parses the lines and creates recipe entries in the database
	 * 
	 * @param lines
	 *            the source lines from the recipe grep
	 * @return the number of recipes parsed or -1 if there was an error.
	 */
	private int parse( ArrayList<String> lines ) {
		Matcher newRecipeMatcher;
		Matcher titleMatcher;
		Matcher metaMatcher;
		Matcher imgMatcher;
		Matcher eofMatcher;

		Recipe recipe = null;
		boolean recipeHasValuation = false;

		int countRecipes = 0;
		for ( String thisLine : lines ) {
			// Log.d( TAG, thisLine );
			newRecipeMatcher = newRecipePattern.matcher( thisLine );
			if ( newRecipeMatcher.find() ) {
				// Log.d( TAG, String.format( "Found recipe: %s",
				// newRecipeMatcher.group( 1 ) ) );
				if ( recipe != null ) {
					addRecipeToCollection( recipe );
					countRecipes++;
					incrementProgress();
				}
				recipe = new Recipe();
				recipeHasValuation = false;
				recipe.setFile( newRecipeMatcher.group( 1 ) );
			} else {
				titleMatcher = titlePattern.matcher( thisLine );
				if ( titleMatcher.find() ) {
					// Log.d( TAG, String.format( "Title: %s",
					// titleMatcher.group( 1 ) ) );
					recipe.setTitle( StringEscapeUtils
							.unescapeHtml4( titleMatcher.group( 1 ) ) );
				} else {
					metaMatcher = metaPattern.matcher( thisLine );
					if ( metaMatcher.find() ) {
						// Log.d( TAG, String.format( "Meta-Tags: %s, %s  ",
						// metaMatcher.group( 1 ), metaMatcher.group( 2 ) ) );
						String grt = metaMatcher.group( 1 );
						String grp = metaMatcher.group( 2 );
						recipe.addClassifications( grt, grp );
						if ( grt.equals( "Bewertung" ) ) {
							recipeHasValuation = true;
						}
					} else {
						imgMatcher = imgPattern.matcher( thisLine );
						if ( imgMatcher.find() && ( !recipe.hasImage() ) ) {
							// pick only the first image tag found
							recipe.setImageFilename( imgMatcher.group( 1 ) );
							// hasPictureGrp.getRecipes().add( recipe );
							// recipe.getGroups().add( hasPictureGrp );

							try {
								recipe.setImageWidth( Integer
										.parseInt( imgMatcher.group( 2 ) ) );
								recipe.setImageHeight( Integer
										.parseInt( imgMatcher.group( 3 ) ) );
							} catch ( NumberFormatException e ) {
								Log.e( TAG,
										String.format(
												"Could not parse image tag %s\nNumberFormatException: %s",
												thisLine, e.getMessage() ) );
							}
							/*
							 * if ( ( recipe.getImageWidth() == 0 ) || (
							 * recipe.getImageHeight() == 0 ) ) { Log.e( TAG,
							 * String.format(
							 * "Image %s has width: %d, height, %d",
							 * recipe.getFile().toString(),
							 * recipe.getImageWidth(), recipe.getImageHeight() )
							 * ); }
							 */
						} else {
							eofMatcher = eofPattern.matcher( thisLine );
							if ( eofMatcher.find() ) {
								eofFound = true;
								if ( recipe != null ) {
									addRecipeToCollection( recipe );
									countRecipes++;
								}
							}
						}
					}
				}
			}
		}

		if ( !eofFound ) {
			Log.d( TAG,
					"EOF Marker not found - Doesn't look like download worked correcty" );
			countRecipes = -1;
		}

		return countRecipes;
	}

	public void addRecipeToCollection( Recipe recipe ) {
		datasource.insertRecipe( recipe );
	}

	private void incrementProgress() {
		handler.post( new Runnable() {

			@Override
			public void run() {
				downloaderCallback.progressStep();
			}
		} );
	}

	private void progressDone() {
		handler.post( new Runnable() {

			@Override
			public void run() {
				downloaderCallback.downloadDone();
			}
		} );

	}

}
