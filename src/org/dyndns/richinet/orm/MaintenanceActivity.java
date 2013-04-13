package org.dyndns.richinet.orm;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MaintenanceActivity extends Activity {

	/**
	 * Tag for logging
	 */
	private static final String TAG = "MaintenanceActivity";

	/**
	 * Create the widget and wire the buttons. Call updateStatus()
	 */
	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_maintenance );

		final Button button_refresh = (Button) findViewById( R.id.button_refresh );
		button_refresh.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				updateStatus();
			}
		} );

		final Button button_clear_timestamp = (Button) findViewById( R.id.button_clear_timestamp );
		button_clear_timestamp.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				StaticAppStuff
						.wipeLastDownloadTimestamp( MaintenanceActivity.this );
				updateStatus();
			}
		} );

		final Button button_wipe_db = (Button) findViewById( R.id.button_wipe_db );
		button_wipe_db.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				doWipeDbButtonClick();
			}
		} );

		final Button button_reset_url = (Button) findViewById( R.id.button_reset_url );
		button_reset_url.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				doResetUrlButtonClick();
			}
		} );

		final Button button_go_firstrun = (Button) findViewById( R.id.button_go_firstrun );
		button_go_firstrun.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick( View v ) {
				doGotoFirstRunButtonClick();

			}
		} );

		updateStatus();
	}

	int newRecipes = -1;
	int totalRecipes = -1;
	long recipesCount = -1;
	long searchesCount = -1;
	long[] categories = new long[3];
	String Url = "undefined";

	/**
	 * Refresh the data for the status Widget
	 */
	private void updateStatus() {
		askWebServer();
		askDB();
		Url = StaticAppStuff.getRecipesDataUrl( this );
	}

	/**
	 * Updates the Status widget on a new handler thread.
	 * @param h the Handler
	 */
	private void updateStatusWidget( Handler h) {
		h.post( new Runnable() {

			@Override
			public void run() {
				updateStatusWidget();

			}
		} );
	}
	
	/**
	 * Update the status widget with the details found in the variables.
	 */
	private void updateStatusWidget() {
		final TextView statusText = (TextView) findViewById( R.id.status_text );
		statusText.setText( String.format(
				"Last download: %s\nRecipes in DB: %d\n"
						+ "New recipes since last download: %d\n"
						+ "Total Server Recipes: %d\n" + "Saved Searched: %d\n"
						+ "Categories: %d, Items: %d, Recipe-Items: %d\n"
						+ "URL: %s",
				StaticAppStuff.getLastRunTimeStamp( this ), recipesCount,
				newRecipes, totalRecipes, searchesCount, categories[0],
				categories[1], categories[2], Url ) );
	}

	/**
	 * Connect to the DB and find out how many recipes we hold.
	 */
	private void askDB() {
		recipesCount = RecipesDataSource.fetchRecipesCount( this );
		searchesCount = RecipesDataSource.fetchSearchesCount( this );
		categories = RecipesDataSource.fetchCategoriesCount( this );
	}

	/**
	 * Connect to the webserver and find out how many recipes are available.
	 */
	private void askWebServer() {
		final Handler h = new Handler();
		Thread r = new Thread() {

			@Override
			public void run() {
				String newCountUrl = StaticAppStuff
						.getNewCountUrl( MaintenanceActivity.this );
				Log.d( TAG, "NewCountUrl: " + newCountUrl );
				ArrayList<String> lines = null;
				try {
					lines = HttpRetriever.retrieveLinesFromUrl( newCountUrl );
				} catch ( IllegalArgumentException e ) {
					e.printStackTrace();
					Log.e( TAG, "IllegalArgumentException: " + e.toString() );
				} catch ( IOException e ) {
					e.printStackTrace();
					Log.e( TAG, "IOException: " + e.toString() );
				}

				String[] counts = lines.get( 0 ).split( "/" );
				newRecipes = Integer.parseInt( counts[0] );
				totalRecipes = Integer.parseInt( counts[1] );
				updateStatusWidget( h );
				/*h.post( new Runnable() {

					@Override
					public void run() {
						updateStatusWidget();

					}
				} ); */
			}
		};
		r.start();
	}

	/**
	 * handle the wipe button request by calling truncateRecipes and updating
	 * the status
	 */
	private void doWipeDbButtonClick() {
		RecipesDataSource datasource = new RecipesDataSource( getBaseContext() );
		datasource.open();
		datasource.truncateRecipes( MaintenanceActivity.this );
		datasource.close();
		updateStatus();

	}

	/**
	 * handles the reset url button
	 */
	private void doResetUrlButtonClick() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences( this );
		String URL = prefs.getString( "serverurl", "no URL" );
		Log.d(TAG, "Prefs are: " + URL);

		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences( this ).edit();
		editor.putString( "serverurl", null );
		editor.commit();
		updateStatus();
		updateStatusWidget();
	}

	/**
	 * takes the user to the First Run activity and closes this one.
	 */
	private void doGotoFirstRunButtonClick() {
		Intent gotoFirstRunIntent = new Intent( this, FirstRunActivity.class );
		startActivity( gotoFirstRunIntent );
		finish();
	}

}
