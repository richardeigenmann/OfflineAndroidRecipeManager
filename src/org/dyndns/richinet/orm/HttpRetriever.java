package org.dyndns.richinet.orm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class HttpRetriever {

	private static final String TAG = "HttpRetriever";

	/**
	 * Connects to the url and returns the result as an ArrayList object
	 * 
	 * @param url
	 *            The URL to read
	 * @return the response
	 */
	public static ArrayList<String> retrieveLinesFromUrl( String url )
			throws IllegalArgumentException, IOException {
		Log.i( TAG, "Retrieving data from URL: " + url );
		InputStream is = null;
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost( url );
		HttpResponse response = httpClient.execute( httpPost );
		HttpEntity entity = response.getEntity();
		is = entity.getContent();

		ArrayList<String> lines = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader(
					is, "iso-8859-1" ), 8 );

			String line = null;
			while ( ( line = reader.readLine() ) != null ) {
				lines.add( line );
			}
			is.close();
		} catch ( Exception e ) {
			Log.e( TAG, "Error converting result: " + e.toString() );
		}
		Log.i( TAG,
				String.format( "Retrieved %d lines from from URL: %s",
						lines.size(), url ) );
		return lines;
	}
	
	
	/**
	 * Connects to the url and returns the result as a String object
	 * 
	 * 
	 * @param url
	 *            The URL to read
	 * @return the response
	 */
	public static String retrieveStringFromUrl( String url )
			throws IllegalArgumentException, IOException {
		Log.i( TAG, "Retrieving data from URL: " + url );
		InputStream is = null;
		long startTime = System.currentTimeMillis();
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost( url );
		HttpResponse response = httpClient.execute( httpPost );
		HttpEntity entity = response.getEntity();
		is = entity.getContent();

		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader(
					is, "iso-8859-1" ), 8 );

			String line = null;
			while ( ( line = reader.readLine() ) != null ) {
				sb.append( line + "\n" );
			}
			is.close();
		} catch ( Exception e ) {
			Log.e( TAG, "Error converting result: " + e.toString() );
		}
		duration = System.currentTimeMillis() - startTime;
		Log.i( TAG,
				String.format( "Retrieved %d characters in %dms from from URL: %s",
						sb.length(), duration, url ) );
		return sb.toString();
	}

	/**
	 * The duration of the download in ms
	 */
	public static long duration = 0;
	
}
