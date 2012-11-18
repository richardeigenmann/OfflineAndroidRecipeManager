package org.dyndns.richinet.orm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.richinet.dyndns.orm.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadActivity extends Activity {

	/**
	 * Tag for logging
	 */
	private static final String TAG = "DownloadActivity";

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_download );

		updateStatus();

		final Button button_update = (Button) findViewById( R.id.button_update );
		final Button button_all = (Button) findViewById( R.id.button_all );
		button_update.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				Log.d( TAG, "Update Button clicked" );
				SharedPreferences prefs = getSharedPreferences(
						StaticAppStuff.PREFS_NAME, 0 );
				final ProgressBar progressBar = (ProgressBar) findViewById( R.id.download_progress_bar );
				// progressBar.setVisibility( ProgressBar.VISIBLE );
				button_update.setClickable( false );
				button_all.setClickable( false );
				//button_update.setVisibility( Button.INVISIBLE );
				progressBar.setMax( newRecipes );
				Context context = DownloadActivity.this;
				( new RecipeParserThread( context, prefs, progressBar,
						DownloadActivity.this ) ).start();
			}
		} );

		button_all.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				Log.d( TAG, "Update Button clicked" );
				SharedPreferences prefs = getSharedPreferences(
						StaticAppStuff.PREFS_NAME, 0 );
				final ProgressBar progressBar = (ProgressBar) findViewById( R.id.download_progress_bar );
				// progressBar.setVisibility( ProgressBar.VISIBLE );
				button_all.setClickable( false );
				button_update.setClickable( false );
				//button_all.setVisibility( Button.INVISIBLE );
				progressBar.setMax( totalRecipes );
				SharedPreferences.Editor editor = prefs.edit();
				editor.remove( "lastRunTimeStamp" );
				editor.commit();
				
				Context context = DownloadActivity.this;
				( new RecipeParserThread( context, prefs, progressBar,
						DownloadActivity.this ) ).start();
			}
		} );

	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		getMenuInflater().inflate( R.menu.activity_download, menu );
		return true;
	}

	int newRecipes = 0;
	int totalRecipes = 0;
	
	public long updateStatus() {
		RecipesDataSource datasource;
		datasource = new RecipesDataSource( this );
		datasource.open();
		final TextView statusText = (TextView) findViewById( R.id.status_text );
		SharedPreferences prefs = getSharedPreferences(
				StaticAppStuff.PREFS_NAME, 0 );
		long recipesCount = datasource.fetchRecipesCount();
		askServer();
		statusText
				.setText( String
						.format(
								"Recipes in DB: %d\nRefreshed: %s\nNew Recipes: %d\nServer Recipes: %d",
								recipesCount,
								prefs.getString( "lastRunTimeStamp", "never" ),
								newRecipes, totalRecipes ) );
		datasource.close();
		return recipesCount;
	}
	
	private void askServer() {
		SharedPreferences prefs = getSharedPreferences(
				StaticAppStuff.PREFS_NAME, 0 );
		String escapedLastRunTimeStamp = RecipeParserThread.getLastRunTimeStamp( prefs );
		String url = prefs.getString( "serverurl",
				StaticAppStuff.DEFAULT_WEBSERVER );
		String fullUrl = url + StaticAppStuff.PHP_QUERY_SCRIPT + "?startfrom="
				+ escapedLastRunTimeStamp;
		Log.d( TAG, "fullUrl: " + fullUrl );
		ArrayList<String> lines = HttpRetriever.retrieveFromURL( fullUrl );
		String[] counts = lines.get( 0 ).split("/");
		newRecipes = Integer.parseInt( counts[0] );
		totalRecipes = Integer.parseInt( counts[1] );
	}
	
}
