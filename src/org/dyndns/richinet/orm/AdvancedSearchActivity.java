package org.dyndns.richinet.orm;

import org.richinet.dyndns.orm.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AdvancedSearchActivity extends Activity {

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_advanced_search );

		final EditText include_words = (EditText) findViewById( R.id.include_words );
		include_words.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick( View v ) {
				Intent pickCatrgoryIntent = new Intent( AdvancedSearchActivity.this,
						CategoryPickActivity.class );
				pickCatrgoryIntent.putExtra( "picks", includeWords );
				AdvancedSearchActivity.this.startActivityForResult( pickCatrgoryIntent,
						INCLUDE_WORDS );
			}
		} );

		final EditText limit_words = (EditText) findViewById( R.id.limit_words );
		limit_words.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick( View v ) {
				Intent pickCatrgoryIntent = new Intent( AdvancedSearchActivity.this,
						CategoryPickActivity.class );
				pickCatrgoryIntent.putExtra( "picks", limitWords );
				AdvancedSearchActivity.this.startActivityForResult( pickCatrgoryIntent,
						LIMIT_WORDS );
			}
		} );

		final EditText exclude_words = (EditText) findViewById( R.id.exclude_words );
		exclude_words.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick( View v ) {
				Intent pickCatrgoryIntent = new Intent( AdvancedSearchActivity.this,
						CategoryPickActivity.class );
				pickCatrgoryIntent.putExtra( "picks", excludeWords );
				AdvancedSearchActivity.this.startActivityForResult( pickCatrgoryIntent,
						EXCLUDE_WORDS );
			}
		} );
		
		final Button button_search = (Button) findViewById( R.id.button_search );
		button_search.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				//String searchTerm = ( (EditText) findViewById( R.id.searchTerm ) )
				//		.getText().toString();
				Intent searchIntent = new Intent( AdvancedSearchActivity.this,
						ResultScrollerActivity.class );
				searchIntent.putExtra( "searchTerm", "" );
				searchIntent.putExtra( "includeWords", includeWords );
				searchIntent.putExtra( "limitWords", limitWords );
				searchIntent.putExtra( "excludeWords", excludeWords );
				AdvancedSearchActivity.this.startActivity( searchIntent );
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

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.activity_advanced_search, menu );
		return true;
	}

}
