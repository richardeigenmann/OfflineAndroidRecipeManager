package org.dyndns.richinet.orm;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * The FirstRunActivity gets launched when the app launches for the first time.
 * It allows the user to download the index of the recipes from the website.
 * 
 * @author Richard Eigenmann
 * 
 */
public class FirstRunActivity extends Activity implements DownloaderInterface {

	/**
	 * The tag for the logger
	 */
	private static final String TAG = "FirstRunActivity";

	/**
	 * The download button. Disabled to begin with then after the webserver
	 * responds it is enabled and then when clicked disabled again.
	 */
	private Button first_run_download_button;

	/**
	 * A text widget that tells the user what is going on in this activity.
	 */
	private TextView activity_status_text;

	/**
	 * The progress bar for the download progress
	 */
	private ProgressBar progressBar;

	/**
	 * A handler for tread problem issues
	 */
	private final Handler handler = new Handler();

	/**
	 * Set up the First Run Activity
	 */
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_first_run );

		// find the widgets AFTER setContentView has run
		first_run_download_button = (Button) findViewById( R.id.first_run_download_button );
		activity_status_text = (TextView) findViewById( R.id.activity_status_text );
		progressBar = (ProgressBar) findViewById( R.id.first_run_progressBar );

		first_run_download_button
				.setOnClickListener( new View.OnClickListener() {
					public void onClick( View v ) {
						doDownloadButtonClick();
					}
				} );

		final Button first_run_goto_search_button = (Button) findViewById( R.id.first_run_goto_search_button );
		first_run_goto_search_button
				.setOnClickListener( new View.OnClickListener() {

					@Override
					public void onClick( View v ) {
						doGotoSearchButtonClick();

					}
				} );

		askWebServer();
	}

	/**
	 * Handle the onResume event
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		askWebServer();
	}

	/**
	 * Inflate an option menu
	 */
	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate( R.menu.option_menu, menu );
		return true;
	}

	/**
	 * Handle Option menu choice
	 */
	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		switch ( item.getItemId() ) {
		case R.id.option_menu_item_maintenance:
			Intent gotoMaintenanceIntent = new Intent( this, MaintenanceActivity.class );
			startActivity( gotoMaintenanceIntent );
			return true;
		default:
			return super.onOptionsItemSelected( item );
		}
	}

	/**
	 * Calls the webserver and asks for the number of recipes available
	 */
	private void askWebServer() {
		Thread r = new Thread() {

			@Override
			public void run() {
				String url = StaticAppStuff
						.getNewCountUrl( FirstRunActivity.this );
				Log.d( TAG, "Info Url: " + url );
				ArrayList<String> lines = null;
				try {
					lines = HttpRetriever.retrieveLinesFromUrl( url );
					String[] counts = lines.get( 0 ).split( "/" );
					int newRecipes = Integer.parseInt( counts[0] );
					// int totalRecipes = Integer.parseInt( counts[1] );
					updateActivityStatusText( String.format(
							"Recipes to index: %d", newRecipes ) );
					prepareForDownload( newRecipes );
				} catch ( IllegalArgumentException e ) {
					e.printStackTrace();
					Log.e( TAG, "IllegalArgumentException: " + e.toString() );
					updateActivityStatusText( "IllegalArgumentException: "
							+ e.toString() );
				} catch ( IOException e ) {
					e.printStackTrace();
					Log.e( TAG, "IOException: " + e.toString() );
					updateActivityStatusText( "IOException: " + e.toString() );
				}
			}
		};
		r.start();
	}

	/**
	 * This method enables the Download button and prepares the progress bar.
	 * All on the correct thread.
	 */
	private void prepareForDownload( final int recipes ) {
		handler.post( new Runnable() {

			@Override
			public void run() {
				first_run_download_button.setEnabled( true );
				progressBar.setMax( recipes );
			}
		} );

	}

	/**
	 * Handles the click event on the download button. Disables the button and
	 * starts the download.
	 */
	private void doDownloadButtonClick() {
		first_run_download_button.setEnabled( false );
		first_run_download_button.setText( R.string.button_download_downloading );
		( new RecipeParserThread( FirstRunActivity.this, FirstRunActivity.this ) )
				.start();
	}

	/**
	 * Updates the status line in the activity with the supplied text. Ensures
	 * that this is done by a handler on the correct thread
	 * 
	 * @param newText
	 *            The new text to update.
	 */
	private void updateActivityStatusText( final String newText ) {
		handler.post( new Runnable() {

			@Override
			public void run() {
				activity_status_text.setText( newText );
			}
		} );

	}

	/**
	 * When the callback is fired we update the progressbar by one.
	 */
	@Override
	public void progressStep() {
		progressBar.incrementProgressBy( 1 );
	}

	/**
	 * When the download is done the status is updated to "Download done" and
	 * the "Go to Search" button is enabled
	 */
	@Override
	public void downloadDone() {
		updateActivityStatusText( "Download done" );
		final Button first_run_goto_search_button = (Button) findViewById( R.id.first_run_goto_search_button );
		first_run_goto_search_button.setEnabled( true );
	}

	/**
	 * takes the user to the Search activity and closes this one.
	 */
	private void doGotoSearchButtonClick() {
		Intent searchActivityIntent = new Intent( this, MainActivity.class );
		startActivity( searchActivityIntent );
		finish();
	}

}
