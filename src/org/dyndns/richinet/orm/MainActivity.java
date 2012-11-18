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
			

		/*final Button button_update = (Button) findViewById( R.id.button_update );
		button_update.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				Log.d( TAG, "Update Button clicked" );
				SharedPreferences prefs = getSharedPreferences(
						StaticAppStuff.PREFS_NAME, 0 );
				final ProgressBar progressBar = (ProgressBar) findViewById( R.id.download_progress_bar );
				progressBar.setVisibility( ProgressBar.VISIBLE );
				button_update.setClickable( false );
				button_update.setVisibility( Button.INVISIBLE );
				progressBar.setMax( 501 );
				Context context = MainActivity.this;
				( new RecipeParserThread( context, prefs, progressBar,
						MainActivity.this ) ).start();
			}
		} );*/

		final Button button_search = (Button) findViewById( R.id.button_search );
		button_search.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				String searchTerm = ( (EditText) findViewById( R.id.searchTerm ) )
						.getText().toString();
				Intent searchIntent = new Intent( MainActivity.this,
						ResultScrollerActivity.class );
				searchIntent.putExtra( "searchTerm", searchTerm );
				searchIntent.putExtra( "includeWords", includeWords );
				searchIntent.putExtra( "limitWords", limitWords );
				searchIntent.putExtra( "excludeWords", excludeWords );
				MainActivity.this.startActivity( searchIntent );
			}
		} );

		final EditText include_words = (EditText) findViewById( R.id.include_words );
		include_words.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick( View v ) {
				Intent pickCatrgoryIntent = new Intent( MainActivity.this,
						CategoryPickActivity.class );
				pickCatrgoryIntent.putExtra( "picks", includeWords );
				MainActivity.this.startActivityForResult( pickCatrgoryIntent,
						INCLUDE_WORDS );
			}
		} );

		final EditText limit_words = (EditText) findViewById( R.id.limit_words );
		limit_words.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick( View v ) {
				Intent pickCatrgoryIntent = new Intent( MainActivity.this,
						CategoryPickActivity.class );
				pickCatrgoryIntent.putExtra( "picks", limitWords );
				MainActivity.this.startActivityForResult( pickCatrgoryIntent,
						LIMIT_WORDS );
			}
		} );

		final EditText exclude_words = (EditText) findViewById( R.id.exclude_words );
		exclude_words.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick( View v ) {
				Intent pickCatrgoryIntent = new Intent( MainActivity.this,
						CategoryPickActivity.class );
				pickCatrgoryIntent.putExtra( "picks", excludeWords );
				MainActivity.this.startActivityForResult( pickCatrgoryIntent,
						EXCLUDE_WORDS );
			}
		} );
	}

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

	private static final int INCLUDE_WORDS = 1;
	private static final int LIMIT_WORDS = INCLUDE_WORDS + 1;
	private static final int EXCLUDE_WORDS = LIMIT_WORDS + 1;

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
	        //case R.id.help:
	         //   showHelp();
	         //   return true;
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
