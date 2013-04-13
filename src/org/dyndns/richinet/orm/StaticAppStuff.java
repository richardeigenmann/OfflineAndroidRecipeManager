package org.dyndns.richinet.orm;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class StaticAppStuff extends Application {

	@SuppressWarnings( "unused" )
	private static final String TAG = "StaticAppStuff";

	
	/**
	 * The default webserver where to find the recipes
	 */
	// public static final String DEFAULT_WEBSERVER =
	// "http://pat.lomumba.ch/richi/";
	public static final String DEFAULT_WEBSERVER = "http://richieigenmann.users.sourceforge.net/";

	/**
	 * The php script that filters out the new recipes
	 */
	public static final String PHP_FILTER_SCRIPT = "recipeinfo.php";

	/**
	 * The php script that tells us how many new recipes there are
	 */
	public static final String PHP_QUERY_SCRIPT = "newrecipestats.php";

	
	
	
	public static final String PREFS_NAME = "Preferences";



	/**
	 * Convenience method to return the shared preferences object
	 * 
	 * @param context
	 *            the context
	 * @return the SharedPreferences object
	 */
	public static SharedPreferences getSharedPreferences( Context context ) {
		return context.getSharedPreferences( StaticAppStuff.PREFS_NAME, 0 );
	}

	/**
	 * This method wipes the timestamp of the last download
	 * 
	 * @param context
	 *            The context to find the shared preferences.
	 */
	public static void wipeLastDownloadTimestamp( Context context ) {
		SharedPreferences.Editor editor = getSharedPreferences( context )
				.edit();
		editor.putString( "lastRunTimeStamp", null );
		editor.commit();
	}

	/**
	 * This method saves the timestamp of the last download
	 * 
	 * @param context
	 *            The context to find the shared preferences.
	 */
	public static void saveLastDownloadTimestamp( Context context,
			Date lastDownloadTimestamp ) {
		SharedPreferences.Editor editor = getSharedPreferences( context )
				.edit();
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		String rememberStartTimeStamp = sdf.format( lastDownloadTimestamp );
		editor.putString( "lastRunTimeStamp", rememberStartTimeStamp );
		editor.commit();
	}

	/**
	 * Returns the timestamp of the last download
	 * 
	 * @param prefs
	 *            The preferences object where the "lastRunTimeStamp" will be
	 *            taken or Jan 1 2000 if not found.
	 * @return A urlencoded version of the last timestamp.
	 */
	public static String getLastRunTimeStamp( Context context ) {
		SharedPreferences prefs = getSharedPreferences( context );
		String lastRunTimeStamp = prefs.getString( "lastRunTimeStamp",
				"2000-01-01 00:00:00" );
		return lastRunTimeStamp;
	}

	/**
	 * Returns the URLencoded version of the last download timestamp.
	 * 
	 * @param prefs
	 *            The preferences object where the "lastRunTimeStamp" will be
	 *            taken or Jan 1 2000 if not found.
	 * @return A urlencoded version of the last timestamp.
	 */
	public static String getLastRunTimeStampUrlencoded( Context context ) {
		String lastRunTimeStamp = getLastRunTimeStamp( context );
		String escapedLastRunTimeStamp = "";
		try {
			escapedLastRunTimeStamp = URLEncoder.encode( lastRunTimeStamp,
					"UTF-8" );
		} catch ( UnsupportedEncodingException e ) {
			Log.e( TAG, e.getMessage() );
			e.printStackTrace();
		}
		// Log.d( TAG, "escapedLastRunTimeStamp: " + escapedLastRunTimeStamp );
		return escapedLastRunTimeStamp;
	}

	/**
	 * Returns the URL to hit that tells us the number of new recipes since the
	 * last run and the total number of recipes.
	 * 
	 * @return the URL to his that tells the number of new recipes
	 */
	public static String getNewCountUrl( Context context ) {
		SharedPreferences prefs = getSharedPreferences( context );
		String escapedLastRunTimeStamp = StaticAppStuff
				.getLastRunTimeStampUrlencoded( context );
		String url = prefs.getString( "serverurl",
				StaticAppStuff.DEFAULT_WEBSERVER );
		String fullUrl = url + StaticAppStuff.PHP_QUERY_SCRIPT + "?startfrom="
				+ escapedLastRunTimeStamp;
		return fullUrl;
	}

	/**
	 * Returns the URL that gives us the updated recipes since the last run date
	 * last run and the total number of recipes.
	 */
	public static String getRecipesDataUrl( Context context ) {
		SharedPreferences prefs = getSharedPreferences( context );
		String escapedLastRunTimeStamp = StaticAppStuff
				.getLastRunTimeStampUrlencoded( context );
		String serverUrl = prefs.getString( "serverurl",
				StaticAppStuff.DEFAULT_WEBSERVER );
		String recipesDataUrl = serverUrl + StaticAppStuff.PHP_FILTER_SCRIPT
				+ "?startfrom=" + escapedLastRunTimeStamp;
		return recipesDataUrl;
	}

	/**
	 * Returns the URL on which the recipe can be retrieved
	 * 
	 * @param the
	 *            context to find the SharedPreferences
	 * @param the
	 *            filename to append to the URL
	 */
	public static String getRecipeUrl( Context context, String file ) {
		SharedPreferences prefs = getSharedPreferences( context );
		String serverUrl = prefs.getString( "serverurl", DEFAULT_WEBSERVER );
		String url = serverUrl + file;
		return url;
	}


}
