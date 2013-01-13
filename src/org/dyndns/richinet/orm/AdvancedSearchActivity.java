package org.dyndns.richinet.orm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AdvancedSearchActivity extends Activity {

	/**
	 * Tag for logging
	 */
	private static final String TAG = "AdvancedSearchActivity";

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_advanced_search );

		final Button advanced_button_include = (Button) findViewById( R.id.advanced_button_include );
		advanced_button_include.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick( View v ) {
				Intent pickCategoryIntent = new Intent(
						AdvancedSearchActivity.this, CategoryPickActivity.class );
				pickCategoryIntent.putExtra( "picks", includeWords );
				AdvancedSearchActivity.this.startActivityForResult(
						pickCategoryIntent, INCLUDE_WORDS );
			}
		} );

		final Button advanced_button_limit = (Button) findViewById( R.id.advanced_button_limit );
		advanced_button_limit.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick( View v ) {
				Intent pickCategoryIntent = new Intent(
						AdvancedSearchActivity.this, CategoryPickActivity.class );
				pickCategoryIntent.putExtra( "picks", limitWords );
				AdvancedSearchActivity.this.startActivityForResult(
						pickCategoryIntent, LIMIT_WORDS );
			}
		} );

		final Button advanced_button_exclude = (Button) findViewById( R.id.advanced_button_exclude );
		advanced_button_exclude.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick( View v ) {
				Intent pickCategoryIntent = new Intent(
						AdvancedSearchActivity.this, CategoryPickActivity.class );
				pickCategoryIntent.putExtra( "picks", excludeWords );
				AdvancedSearchActivity.this.startActivityForResult(
						pickCategoryIntent, EXCLUDE_WORDS );
			}
		} );

		final Button advanced_button_search = (Button) findViewById( R.id.advanced_button_search );
		advanced_button_search.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				doSearchClick();
			}
		} );

		final Button advanced_button_save = (Button) findViewById( R.id.advanced_button_save );
		advanced_button_save.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				doSaveClick();
			}
		} );

	}

	private static final int INCLUDE_WORDS = 1;
	private static final int LIMIT_WORDS = 2;
	private static final int EXCLUDE_WORDS = 3;

	private String[] includeWords = new String[0];
	private String[] limitWords = new String[0];
	private String[] excludeWords = new String[0];

	@Override
	public void onActivityResult( int requestCode, int resultCode, Intent data ) {
		if ( resultCode == Activity.RESULT_OK ) {
			String[] picks = data.getStringArrayExtra( "picks" );

			StringBuffer formattedPicks = new StringBuffer( "" );
			boolean firstIteration = true;
			for ( String pick : picks ) {
				if ( !firstIteration ) {
					formattedPicks.append( ", " );
				} else {
					firstIteration = false;
				}
				formattedPicks.append( pick );
			}

			switch ( requestCode ) {
			case INCLUDE_WORDS: {
				includeWords = picks;
				final TextView include_words = (TextView) findViewById( R.id.include_words );
				include_words.setText( formattedPicks );
				break;
			}
			case LIMIT_WORDS: {
				limitWords = picks;
				final TextView limit_words = (TextView) findViewById( R.id.limit_words );
				limit_words.setText( formattedPicks );
				break;
			}
			case EXCLUDE_WORDS: {
				excludeWords = picks;
				final TextView exclude_words = (TextView) findViewById( R.id.exclude_words );
				exclude_words.setText( formattedPicks );
				break;
			}
			}
		}
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
			Intent gotoMaintenanceIntent = new Intent(
					AdvancedSearchActivity.this, MaintenanceActivity.class );
			AdvancedSearchActivity.this.startActivity( gotoMaintenanceIntent );
			return true;
		default:
			return super.onOptionsItemSelected( item );
		}
	}

	/**
	 * Handle the click on the search button
	 */
	private void doSearchClick() {
		Intent searchIntent = new Intent( AdvancedSearchActivity.this,
				ResultScrollerActivity.class );
		searchIntent.putExtra( "searchTerm", "" );
		searchIntent.putExtra( "includeWords", includeWords );
		searchIntent.putExtra( "limitWords", limitWords );
		searchIntent.putExtra( "excludeWords", excludeWords );
		AdvancedSearchActivity.this.startActivity( searchIntent );
	}

	/**
	 * Handle the click on the save button
	 */
	private void doSaveClick() {
		showDialog( SAVED_QUERY_DIALOG );

	}

	/**
	 * An id for the dialog
	 */
	private static final int SAVED_QUERY_DIALOG = 0;

	/**
	 * Create the dialog. Called only once. onPrepareDialog is called on
	 * subsequent opens
	 */
	@Override
	protected Dialog onCreateDialog( int id ) {
		Dialog dialog;
		switch ( id ) {
		case SAVED_QUERY_DIALOG:
			AlertDialog.Builder builder = new AlertDialog.Builder( this );

			// this line seems to be required even though the values are
			// replaced
			// in the onPrepareDialog method. If removed the title and message
			// are
			// not shown.
			builder.setTitle( "Save as" ).setMessage( "Enter description" );

			// Set an EditText view to get user input
			final EditText input = new EditText( this );
			builder.setView( input );

			builder.setPositiveButton( "OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick( DialogInterface dialog, int which ) {
							dismissDialog( SAVED_QUERY_DIALOG );
							String value = input.getText().toString();
							RecipesDataSource.saveSearch(
									AdvancedSearchActivity.this, value,
									includeWords, limitWords, excludeWords );
							Toast.makeText( AdvancedSearchActivity.this,
									"Saved query " + value, Toast.LENGTH_LONG )
									.show();

						}
					} );
			builder.setNegativeButton( "Cancel",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick( DialogInterface dialog, int which ) {
							dismissDialog( SAVED_QUERY_DIALOG );
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

}
