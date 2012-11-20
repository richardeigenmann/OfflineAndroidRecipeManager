package org.dyndns.richinet.orm;

import java.util.ArrayList;

import org.richinet.dyndns.orm.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
				progressBar.setMax( newRecipes );
				Context context = DownloadActivity.this;
				( new RecipeParserThread( context, prefs, progressBar,
						DownloadActivity.this ) ).start();
			}
		} );

		button_all.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				Log.d( TAG, "Download all Button clicked" );
				final ProgressBar progressBar = (ProgressBar) findViewById( R.id.download_progress_bar );
				progressBar.setMax( totalRecipes );
				clearLastDownloadDate();
				SharedPreferences prefs = getSharedPreferences(
						StaticAppStuff.PREFS_NAME, 0 );

				Context context = DownloadActivity.this;
				( new RecipeParserThread( context, prefs, progressBar,
						DownloadActivity.this ) ).start();
			}
		} );

		final Button button_refresh = (Button) findViewById( R.id.button_refresh );
		button_refresh.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				updateStatus();
			}
		} );

		

		final Button button_wipe = (Button) findViewById( R.id.button_wipe );
		button_wipe.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				Log.d( TAG, "Wipe Button clicked" );
				clearLastDownloadDate();
				RecipesDataSource datasource = new RecipesDataSource( getBaseContext() );
				datasource.open();
				datasource.wipeDatabase();
				datasource.close();
				updateStatus();
			}
		} );

		
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		getMenuInflater().inflate( R.menu.activity_download, menu );
		return true;
	}

	private void clearLastDownloadDate() {
		SharedPreferences prefs = getSharedPreferences(
				StaticAppStuff.PREFS_NAME, 0 );
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove( "lastRunTimeStamp" );
		editor.commit();
	}
	
	
	int newRecipes = -1;
	int totalRecipes = -1;
	long recipesCount = -1;

	public void updateStatus() {
		askWebServer();
		askDB();
	}

	private void updateStatusWidget() {
		final TextView statusText = (TextView) findViewById( R.id.status_text );
		SharedPreferences prefs = getSharedPreferences(
				StaticAppStuff.PREFS_NAME, 0 );
		statusText
				.setText( String
						.format(
								"Recipes in DB: %d\nRefreshed: %s\nNew Recipes: %d\nServer Recipes: %d",
								recipesCount,
								prefs.getString( "lastRunTimeStamp", "never" ),
								newRecipes, totalRecipes ) );
	}

	private void askDB() {
		RecipesDataSource datasource;
		datasource = new RecipesDataSource( this );
		datasource.open();
		recipesCount = datasource.fetchRecipesCount();
		datasource.close();
	}

	private void askWebServer() {
		final Handler h = new Handler();
		Thread r = new Thread() {

			@Override
			public void run() {
				SharedPreferences prefs = getSharedPreferences(
						StaticAppStuff.PREFS_NAME, 0 );
				String escapedLastRunTimeStamp = RecipeParserThread
						.getLastRunTimeStamp( prefs );
				String url = prefs.getString( "serverurl",
						StaticAppStuff.DEFAULT_WEBSERVER );
				String fullUrl = url + StaticAppStuff.PHP_QUERY_SCRIPT
						+ "?startfrom=" + escapedLastRunTimeStamp;
				Log.d( TAG, "fullUrl: " + fullUrl );
				ArrayList<String> lines = HttpRetriever
						.retrieveFromURL( fullUrl );
				String[] counts = lines.get( 0 ).split( "/" );
				newRecipes = Integer.parseInt( counts[0] );
				totalRecipes = Integer.parseInt( counts[1] );
				h.post( new Runnable() {

					@Override
					public void run() {
						updateStatusWidget();

					}
				} );
			}
		};
		r.start();
	}

}
