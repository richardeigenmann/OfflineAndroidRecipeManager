package org.dyndns.richinet.orm;

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

public class MainActivity extends Activity {

	/**
	 * Tag for logging
	 */
	@SuppressWarnings( "unused" )
	private static final String TAG = "MainActivity";

	/**
	 * Initialise the widgets
	 */
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

		long recipeCount = RecipesDataSource.fetchRecipesCount( this );
		if ( recipeCount == 0 ) {
			Intent firstRunIntent = new Intent( MainActivity.this,
					FirstRunActivity.class );
			MainActivity.this.startActivity( firstRunIntent );
			finish();
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
				searchIntent.putExtra( "includeWords", new String[] {
						"3 Sterne", "4 Sterne" } );
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
				searchIntent.putExtra( "includeWords",
						new String[] { "Hauptgerichte" } );
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
				searchIntent.putExtra( "includeWords",
						new String[] { "Desserts" } );
				searchIntent.putExtra( "limitWords", new String[0] );
				searchIntent.putExtra( "excludeWords", new String[0] );
				MainActivity.this.startActivity( searchIntent );
			}
		} );

		// Use the SimpleCursorAdapter to show the
		// elements in a ListView
		/*
		 * final ArrayAdapter<Recipe> adapter = new ArrayAdapter<Recipe>( this,
		 * android.R.layout.simple_list_item_1, recipes ); ListView listView =
		 * (ListView) findViewById( R.id.resultsListView );
		 * 
		 * listView.setAdapter( adapter );
		 * 
		 * listView.setOnItemClickListener( new OnItemClickListener() {
		 * 
		 * @Override public void onItemClick( AdapterView<?> parent, View view,
		 * int position, long r_id ) { Recipe selectedRecipe = adapter.getItem(
		 * position ); selectedRecipe.dumpToLog(); SharedPreferences prefs =
		 * getSharedPreferences( StaticAppStuff.PREFS_NAME, 0 ); String url =
		 * prefs.getString( "serverurl", StaticAppStuff.DEFAULT_WEBSERVER ); url
		 * = url + selectedRecipe.getFile(); Intent browserIntent = new Intent(
		 * Intent.ACTION_VIEW, Uri .parse( url ) ); startActivity( browserIntent
		 * ); } } );
		 */

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
	 * Handle the option pick event
	 */
	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		switch ( item.getItemId() ) {
		case R.id.option_menu_item_maintenance:
			Intent gotoMaintenanceIntent = new Intent( MainActivity.this,
					MaintenanceActivity.class );
			MainActivity.this.startActivity( gotoMaintenanceIntent );
			return true;
		default:
			return super.onOptionsItemSelected( item );
		}
	}

}
