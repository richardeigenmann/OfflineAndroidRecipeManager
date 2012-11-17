package org.richinet.dyndns.orm;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ResultScrollerActivity extends Activity {

	/**
	 * Tag for logging
	 */
	@SuppressWarnings( "unused" )
	private static final String TAG = "ResultScrollerActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		setContentView( R.layout.activity_result_scroller );

		final Intent myIntent = getIntent();
		final String searchTerm = myIntent.getStringExtra( "searchTerm" );

		final String[] includeWords = myIntent
				.getStringArrayExtra( "includeWords" );
		final String[] limitWords = myIntent.getStringArrayExtra( "limitWords" );
		final String[] excludeWords = myIntent
				.getStringArrayExtra( "excludeWords" );

		List<Recipe> recipes = null;
		RecipesDataSource datasource = new RecipesDataSource( this );
		if ( includeWords.length == 0 ) {
			recipes = datasource.searchRecipes( searchTerm );
		} else {
			recipes = datasource.searchRecipes( includeWords,
					limitWords, excludeWords );
		}

		// Use the SimpleCursorAdapter to show the
		// elements in a ListView
		final ArrayAdapter<Recipe> adapter = new ArrayAdapter<Recipe>( this,
				android.R.layout.simple_list_item_1, recipes );
		ListView listView = (ListView) findViewById( R.id.resultsListView );

		listView.setAdapter( adapter );

		listView.setOnItemClickListener( new OnItemClickListener() {

			@Override
			public void onItemClick( AdapterView<?> parent, View view,
					int position, long r_id ) {
				Recipe selectedRecipe = adapter.getItem( position );
				selectedRecipe.dumpToLog();
				SharedPreferences prefs = getSharedPreferences(
						StaticAppStuff.PREFS_NAME, 0 );
				String url = prefs.getString( "serverurl",
						StaticAppStuff.DEFAULT_WEBSERVER );
				url = url + selectedRecipe.getFile();
				Intent browserIntent = new Intent( Intent.ACTION_VIEW, Uri
						.parse( url ) );
				startActivity( browserIntent );
			}
		} );

	}

}
