package org.richinet.dyndns.orm;

import java.io.BufferedReader;
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
	 * Connects to the url and returns the result as a StringBuilder object
	 * 
	 * @param url
	 *            The URL to read
	 * @return the response
	 */
	public static ArrayList<String> retrieveFromURL( String url ) {
		InputStream is = null;
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost( url );
			HttpResponse response = httpClient.execute( httpPost );
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		} catch ( Exception e ) {
			Log.e( TAG, "Error in http connection: " + e.toString() );
		}

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
		return lines;
	}

}
