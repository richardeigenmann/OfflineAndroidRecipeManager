package org.dyndns.richinet.orm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class RecipeParserThread extends Thread {

	// TODO: Re-write this class with the JASON parser

	/**
	 * Tag for the logger
	 */
	private static final String TAG = "RecipeParserThread";

	// StaticAppStuff app;

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
		// ArrayList<String> lines = null;
		String jsonData = "";
		try {
			// lines = HttpRetriever.retrieveLinesFromUrl( fullUrl );
			jsonData = HttpRetriever.retrieveStringFromUrl( fullUrl );
		} catch ( IllegalArgumentException e ) {
			e.printStackTrace();
			Log.e( TAG, "IllegalArgumentException: " + e.toString() );
		} catch ( IOException e ) {
			e.printStackTrace();
			Log.e( TAG, "IOException: " + e.toString() );
		}

		sendLongToast( String.format( "Retrieved %d characters in %dms from server",
				jsonData.length(), HttpRetriever.duration  ) );
		
		datasource = new RecipesDataSource( context );
		datasource.open();
		int parsedRecipes = jsonParse( jsonData );
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
						String grt = StringEscapeUtils
								.unescapeHtml4( metaMatcher.group( 1 ) );
						String grp = StringEscapeUtils
								.unescapeHtml4( metaMatcher.group( 2 ) );
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

	/**
	 * Parses the lines and creates recipe entries in the database
	 * 
	 * @param lines
	 *            the source lines from the recipe grep
	 * @return the number of recipes parsed or -1 if there was an error.
	 */
	private int jsonParse( String jsonString ) {
		long startTime = System.currentTimeMillis();
		int countRecipes = 0;
		try {
			JSONObject jObject = new JSONObject( jsonString );

			Iterator<?> keys = jObject.keys();

			// create variables outside the loop to avoid re-creating them over and over again.
			String key;
			Recipe recipe;
			String category;
			String escapedCategory;
			String grp;
			while ( keys.hasNext() ) {
				key = (String) keys.next();
				Object recipeObject = jObject.get( key );
				if ( recipeObject instanceof JSONObject ) {
					countRecipes++;
					if ( countRecipes % 5 == 0 ) {
						sendShortToast( String.format( "%d recipes parsed",
								countRecipes ) );
					}
					recipe = new Recipe();
					recipe.setFile( key );
					JSONObject recipeJsonObject = (JSONObject) recipeObject;
					recipe.setTitle( StringEscapeUtils
							.unescapeHtml4( recipeJsonObject.getString( "name" ) ) );
					recipe.setImageFilename( recipeJsonObject
							.getString( "imageFilename" ) );
					recipe.setImageWidth( recipeJsonObject.getInt( "width" ) );
					recipe.setImageHeight( recipeJsonObject.getInt( "height" ) );
					JSONObject recipeCategories = (JSONObject) recipeJsonObject
							.get( "categories" );

					Iterator<?> grt = recipeCategories.keys();
					while ( grt.hasNext() ) {
						category = (String) grt.next();
						escapedCategory = StringEscapeUtils
								.unescapeHtml4( category );
						JSONArray entries = recipeCategories
								.getJSONArray( category );
						// Log.d(TAG, key + " - " + category + ": "
						// +entries.length() + " entries");

						for ( int i = 0; i < entries.length(); i++ ) {
							grp = StringEscapeUtils
									.unescapeHtml4( (String) entries.get( i ) );
							recipe.addClassifications( escapedCategory, grp );
						}
					}
					// recipe.dumpToLog();
					addRecipeToCollection( recipe );
					incrementProgress();
				}
			}
		} catch ( JSONException e ) {
			Log.e( TAG, "JSONException: " + e.toString() );
			e.printStackTrace();
		}
		long duration = System.currentTimeMillis() - startTime;
		Log.d( TAG, String.format( "Parsed %d recipes in %dms", countRecipes,
				duration ) );
		sendLongToast( String.format( "Parsed %d recipes in %dms",
				countRecipes, duration ) );

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

	/**
	 * use the handler to send a toast to the UI
	 * 
	 * @param message
	 */
	private void sendLongToast( final String message ) {
		handler.post( new Runnable() {

			@Override
			public void run() {
				Toast.makeText( context, message, Toast.LENGTH_LONG ).show();
			}
		} );
	}

	/**
	 * use the handler to send a toast to the UI
	 * 
	 * @param message
	 */
	private void sendShortToast( final String message ) {
		handler.post( new Runnable() {

			@Override
			public void run() {
				Toast.makeText( context, message, Toast.LENGTH_SHORT ).show();
			}
		} );
	}

}
