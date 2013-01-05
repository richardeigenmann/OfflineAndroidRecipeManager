package org.dyndns.richinet.orm;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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

		final Button button_search = (Button) findViewById( R.id.main_button_search );
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

		final Button button_advanced_search = (Button) findViewById( R.id.main_button_advanced_search );
		button_advanced_search.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				Intent advancedSearchIntent = new Intent( MainActivity.this,
						AdvancedSearchActivity.class );
				MainActivity.this.startActivity( advancedSearchIntent );
			}
		} );


		fetchSearches();

		adapter = new ArrayAdapter<Search>( this,
				android.R.layout.simple_list_item_1, searches );
		ListView listView = (ListView) findViewById( R.id.searchesListView );
		listView.setAdapter( adapter );

		listView.setOnItemClickListener( new OnItemClickListener() {

			@Override
			public void onItemClick( AdapterView<?> parent, View view,
					int position, long r_id ) {
				clickedSavedSearch = adapter.getItem( position );
				Log.i( TAG, String.format(
						"Click on item %d for searchId %d for %s", position,
						clickedSavedSearch.getSearchId(),
						clickedSavedSearch.getDescription() ) );
				showDialog( SAVED_QUERY_DIALOG );
			}
		} );

	}

	/**
	 * The list of searches
	 */
	private List<Search> searches = null;
	
	/**
	 * The array adapter to show it
	 */
	private ArrayAdapter<Search> adapter;

	/**
	 * refreshes the list of searches
	 */
	private void fetchSearches() {
		RecipesDataSource datasource = new RecipesDataSource( this );
		searches = datasource.getSearches();
	}
	
	/**
	 * Memorise the saved search that was clicked for the dialog. TODO: Is this
	 * the way to do it, holding a reference in the activity? Why ever not?
	 */
	private Search clickedSavedSearch = null;

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

	/**
	 * Constant to indicate the Saves Query Dialog
	 */
	private static final int SAVED_QUERY_DIALOG = 1;

	/**
	 * Create the dialog. Called only once. onPrepareDialog is called on
	 * subsequent opens
	 */
	@Override
	protected Dialog onCreateDialog( int id ) {
		Dialog dialog;
		switch ( id ) {
		case SAVED_QUERY_DIALOG:
			AlertDialog.Builder builder = new AlertDialog.Builder(
					MainActivity.this );

			// this line seems to be required even though the values are
			// replaced
			// in the onPrepareDialog method. If removed the title and message
			// are
			// not shown.
			builder.setTitle( " " ).setMessage( "" );

			builder.setPositiveButton( "Search",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick( DialogInterface dialog, int which ) {
							dismissDialog( SAVED_QUERY_DIALOG );
							doExecuteSavedSearch();
						}
					} );
			builder.setNeutralButton( "Delete",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick( DialogInterface dialog, int which ) {
							dismissDialog( SAVED_QUERY_DIALOG );
							doDeleteSavedSearch();
						}
					} );

			AlertDialog alert = builder.create();
			dialog = alert;
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	/**
	 * This method is called each time the dialog is displayed.
	 */
	@Override
	protected void onPrepareDialog( int id, Dialog dialog ) {
		switch ( id ) {
		case SAVED_QUERY_DIALOG:
			( (AlertDialog) dialog ).setTitle( "Saved Search" );
			( (AlertDialog) dialog ).setMessage( clickedSavedSearch
					.getDescription() );
			break;
		}
	}

	/**
	 * Execute the saved search that is referred to in the clickedSaveSearch
	 * variable.
	 */
	private void doExecuteSavedSearch() {
		Toast.makeText(
				this,
				"You want to execute saved query "
						+ clickedSavedSearch.getDescription(),
				Toast.LENGTH_LONG ).show();
		Intent searchIntent = new Intent( this, ResultScrollerActivity.class );
		searchIntent.putExtra( "searchId", clickedSavedSearch.getSearchId() );
		startActivity( searchIntent );
	}

	/**
	 * Delete the saved search that is referred to in the clickedSaveSearch
	 * variable.
	 */
	private void doDeleteSavedSearch() {
		RecipesDataSource.deleteSavedSearch( this,
				clickedSavedSearch.getSearchId() );
		fetchSearches();
		adapter.notifyDataSetChanged();
		Toast.makeText( this,
				"Deleted saved query " + clickedSavedSearch.getDescription(),
				Toast.LENGTH_SHORT ).show();
	}

}
