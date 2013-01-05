package org.dyndns.richinet.orm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AdvancedSearchActivity extends Activity {

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_advanced_search );

		final EditText include_words = (EditText) findViewById( R.id.include_words );
		include_words.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick( View v ) {
				Intent pickCategoryIntent = new Intent( AdvancedSearchActivity.this,
						CategoryPickActivity.class );
				pickCategoryIntent.putExtra( "picks", includeWords );
				AdvancedSearchActivity.this.startActivityForResult( pickCategoryIntent,
						INCLUDE_WORDS );
			}
		} );

		final EditText limit_words = (EditText) findViewById( R.id.limit_words );
		limit_words.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick( View v ) {
				Intent pickCategoryIntent = new Intent( AdvancedSearchActivity.this,
						CategoryPickActivity.class );
				pickCategoryIntent.putExtra( "picks", limitWords );
				AdvancedSearchActivity.this.startActivityForResult( pickCategoryIntent,
						LIMIT_WORDS );
			}
		} );

		final EditText exclude_words = (EditText) findViewById( R.id.exclude_words );
		exclude_words.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick( View v ) {
				Intent pickCategoryIntent = new Intent( AdvancedSearchActivity.this,
						CategoryPickActivity.class );
				pickCategoryIntent.putExtra( "picks", excludeWords );
				AdvancedSearchActivity.this.startActivityForResult( pickCategoryIntent,
						EXCLUDE_WORDS );
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
	private static final int LIMIT_WORDS = INCLUDE_WORDS + 1;
	private static final int EXCLUDE_WORDS = LIMIT_WORDS + 1;

	
	private String[] includeWords = new String[0];
	private String[] limitWords = new String[0];
	private String[] excludeWords = new String[0];

	@Override
	public void onActivityResult( int requestCode, int resultCode, Intent data ) {
		super.onActivityResult( requestCode, resultCode, data );
		switch ( requestCode ) {
		case ( INCLUDE_WORDS ): {
			if ( resultCode == Activity.RESULT_OK ) {
				includeWords = data.getStringArrayExtra( "picks" );
				final EditText include_words = (EditText) findViewById( R.id.include_words );
				StringBuffer sb = new StringBuffer( "" );
				for ( String s : includeWords ) {
					// Log.d(TAG, s);
					sb.append( s );
					sb.append( ", " );
				}
				include_words.setText( sb );
			}
			break;
		}
		case ( LIMIT_WORDS ): {
			if ( resultCode == Activity.RESULT_OK ) {
				limitWords = data.getStringArrayExtra( "picks" );
				final EditText limit_words = (EditText) findViewById( R.id.limit_words );
				StringBuffer sb = new StringBuffer( "" );
				for ( String s : includeWords ) {
					// Log.d(TAG, s);
					sb.append( s );
					sb.append( ", " );
				}
				limit_words.setText( sb );
			}
			break;
		}
		case ( EXCLUDE_WORDS ): {
			if ( resultCode == Activity.RESULT_OK ) {
				excludeWords = data.getStringArrayExtra( "picks" );
				final EditText exclude_words = (EditText) findViewById( R.id.exclude_words );
				StringBuffer sb = new StringBuffer( "" );
				for ( String s : excludeWords ) {
					// Log.d(TAG, s);
					sb.append( s );
					sb.append( ", " );
				}
				exclude_words.setText( sb );
			}
			break;
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
			Intent gotoMaintenanceIntent = new Intent( AdvancedSearchActivity.this,
					MaintenanceActivity.class );
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
			AlertDialog.Builder builder = new AlertDialog.Builder(
					this );

			// this line seems to be required even though the values are
			// replaced
			// in the onPrepareDialog method. If removed the title and message
			// are
			// not shown.
			builder.setTitle( "Save as" ).setMessage( "Enter description" );

			// Set an EditText view to get user input   
			 final EditText input = new EditText(this); 
			 builder.setView( input  );
			
			builder.setPositiveButton( "OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick( DialogInterface dialog, int which ) {
							dismissDialog( SAVED_QUERY_DIALOG );
							String value = input.getText().toString();
							RecipesDataSource.saveSearch( AdvancedSearchActivity.this, value, includeWords, limitWords, excludeWords );
							Toast.makeText(
									AdvancedSearchActivity.this,
									"Saved query " + value ,
									Toast.LENGTH_LONG ).show();

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
