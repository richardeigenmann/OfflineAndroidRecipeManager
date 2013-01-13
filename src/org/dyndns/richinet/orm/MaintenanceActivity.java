package org.dyndns.richinet.orm;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

	/**
	 * Refresh the data for the status Widget
	 */
	private void updateStatus() {
		askWebServer();
		askDB();
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
						+ "Categories: %d, Items: %d, Recipe-Items: %d",
				StaticAppStuff.getLastRunTimeStamp( this ), recipesCount,
				newRecipes, totalRecipes, searchesCount, categories[0], categories[1], categories[2] ) );
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
				String fullUrl = StaticAppStuff
						.getNewCountUrl( MaintenanceActivity.this );
				Log.d( TAG, "fullUrl: " + fullUrl );
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
	 * takes the user to the First Run activity and closes this one.
	 */
	private void doGotoFirstRunButtonClick() {
		Intent gotoFirstRunIntent = new Intent( this, FirstRunActivity.class );
		startActivity( gotoFirstRunIntent );
		finish();
	}

}
