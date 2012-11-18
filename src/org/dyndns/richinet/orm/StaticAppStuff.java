package org.dyndns.richinet.orm;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;



public class StaticAppStuff extends Application {

	@SuppressWarnings( "unused" )
	private static final String TAG = "StaticAppStuff";


	public static final String PREFS_NAME = "Preferences";

	/**
	 * Returns the URL for the IO classes
	 * 
	 * @return The URL for the IO classes
	 */
	public String getURL() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences( this );
		String URL = prefs.getString( "serverurl",
				"http://richieigenmann.users.sourceforge.net/allrecipes.php" );
		return URL;
	}
	
	/**
	 * The default webserver where to find the recipes
	 */
	public static final String DEFAULT_WEBSERVER = "http://pat.lomumba.ch/richi/";
	//public static final String DEFAULT_WEBSERVER = "http://richieigenmann.users.sourceforge.net/";
	
	
	/**
	 * The php script that filters out the new recipes
	 */
	public static final String PHP_FILTER_SCRIPT = "allrecipes.php";
	
	/**
	 * The php script that tells us how many new recipes there are
	 */
	public static final String PHP_QUERY_SCRIPT = "newrecipestats.php";
	

}
