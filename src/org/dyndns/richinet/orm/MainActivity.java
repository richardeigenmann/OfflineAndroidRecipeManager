package org.dyndns.richinet.orm;

import org.richinet.dyndns.orm.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
		SharedPreferences prefs = getSharedPreferences(
				StaticAppStuff.PREFS_NAME, 0 );
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString( "serverurl", StaticAppStuff.DEFAULT_WEBSERVER );
		editor.commit();

		long recipeCount = updateStatus();
		if ( recipeCount == 0 ) {
			Intent downloadIntent = new Intent( MainActivity.this,
					DownloadActivity.class );
			MainActivity.this.startActivity( downloadIntent );
		}
			
		final Button button_search = (Button) findViewById( R.id.button_search );
		button_search.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				String searchTerm = ( (EditText) findViewById( R.id.searchTerm ) )
						.getText().toString();
				Intent searchIntent = new Intent( MainActivity.this,
						ResultScrollerActivity.class );
				searchIntent.putExtra( "searchTerm", searchTerm );
				searchIntent.putExtra( "includeWords", new String[0] );
				searchIntent.putExtra( "limitWords", new String[0] );
				searchIntent.putExtra( "excludeWords", new String[0] );
				MainActivity.this.startActivity( searchIntent );
			}
		} );
		
		final Button button_advanced_search = (Button) findViewById( R.id.button_advanced_search );
		button_advanced_search.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				Intent advancedSearchIntent = new Intent( MainActivity.this,
						AdvancedSearchActivity.class );
				MainActivity.this.startActivity( advancedSearchIntent );
			}
		} );
		final Button button_34sterne = (Button) findViewById( R.id.button_34sterne );
		button_34sterne.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				Intent searchIntent = new Intent( MainActivity.this,
						ResultScrollerActivity.class );
				searchIntent.putExtra( "searchTerm", "" );
				searchIntent.putExtra( "includeWords", new String[] {"3 Sterne", "4 Sterne"} );
				searchIntent.putExtra( "limitWords", new String[0] );
				searchIntent.putExtra( "excludeWords", new String[0] );
				MainActivity.this.startActivity( searchIntent );
			}
		} );
		final Button button_hauptpgerichte = (Button) findViewById( R.id.button_hauptpgerichte );
		button_hauptpgerichte.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				Intent searchIntent = new Intent( MainActivity.this,
						ResultScrollerActivity.class );
				searchIntent.putExtra( "searchTerm", "" );
				searchIntent.putExtra( "includeWords", new String[] {"Hauptgerichte"} );
				searchIntent.putExtra( "limitWords", new String[0] );
				searchIntent.putExtra( "excludeWords", new String[0] );
				MainActivity.this.startActivity( searchIntent );
			}
		} );

		
		final Button button_desserts = (Button) findViewById( R.id.button_desserts );
		button_desserts.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				Intent searchIntent = new Intent( MainActivity.this,
						ResultScrollerActivity.class );
				searchIntent.putExtra( "searchTerm", "" );
				searchIntent.putExtra( "includeWords", new String[] {"Desserts"} );
				searchIntent.putExtra( "limitWords", new String[0] );
				searchIntent.putExtra( "excludeWords", new String[0] );
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
		inflater.inflate( R.menu.activity_main, menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_download:
	        	Intent downloadIntent = new Intent( MainActivity.this,
						DownloadActivity.class );
				MainActivity.this.startActivity( downloadIntent );
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	public long updateStatus() {
		RecipesDataSource datasource;
		datasource = new RecipesDataSource( this );
		datasource.open();
		final TextView statusText = (TextView) findViewById( R.id.status_text );
		SharedPreferences prefs = getSharedPreferences(
				StaticAppStuff.PREFS_NAME, 0 );
		long recipesCount = datasource.fetchRecipesCount();
		statusText.setText( String.format( "Recipes: %d  Refreshed: %s",
				recipesCount,
				prefs.getString( "lastRunTimeStamp", "never" ) ) );
		datasource.close();
		return recipesCount;
	}

}
