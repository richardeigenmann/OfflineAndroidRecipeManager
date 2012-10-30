package org.richinet.dyndns.orm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

	/**
	 * Tag for logging
	 */
	private static final String TAG = "MainActivity";

	
	
	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );



		

		// Check whether preferences have been set
		SharedPreferences prefs = getSharedPreferences( StaticAppStuff.PREFS_NAME, 0 );
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString( "serverurl", StaticAppStuff.DEFAULT_WEBSERVER );
		editor.commit();

		updateStatus();
	
		
		
		final Button button_update = (Button) findViewById( R.id.button_update );
		button_update.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				Log.d( TAG, "Update Button clicked" );
				SharedPreferences prefs = getSharedPreferences( StaticAppStuff.PREFS_NAME, 0 );
				final ProgressBar progressBar = (ProgressBar) findViewById( R.id.download_progress_bar );
				progressBar.setVisibility( ProgressBar.VISIBLE );
				button_update.setClickable( false );
				button_update.setVisibility( Button.INVISIBLE );
				progressBar.setMax( 501 );
				Context context = MainActivity.this;
				( new RecipeParserThread( context, prefs, progressBar, MainActivity.this ) ).start();
			}
		} );

		
		
		
		final Button button_search = (Button) findViewById( R.id.button_search );
		button_search.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				String searchTerm = ( (EditText) findViewById( R.id.searchTerm ) )
						.getText().toString();
				Intent searchIntent = new Intent( MainActivity.this,
						ResultScrollerActivity.class );
				searchIntent.putExtra( "searchTerm", searchTerm );
				MainActivity.this.startActivity( searchIntent );
			}
		} );
	}

	/**
	 * Inflate an option menu
	 */
	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate( R.menu.mainoptions, menu );
		return true;
	}

	public void updateStatus() {
		RecipesDataSource datasource;
		datasource = new RecipesDataSource( this );
		datasource.open();
		final TextView  statusText = (TextView) findViewById( R.id.status_text );
		SharedPreferences prefs = getSharedPreferences( StaticAppStuff.PREFS_NAME, 0 );
		statusText.setText( String.format( "Recipes: %d  Refreshed: %s",
				datasource.fetchRecipesCount(), prefs.getString( "lastRunTimeStamp",
						"never" ) ) );
		datasource.close();
	}
	
}
