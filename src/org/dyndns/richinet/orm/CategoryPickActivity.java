package org.dyndns.richinet.orm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

public class CategoryPickActivity extends ExpandableListActivity {

	/**
	 * Tag for logging
	 */
	private static final String TAG = "CategoryPickActivity";


	private List<HashMap<String, String>> categories;
	private List<List<HashMap<String, String>>> categoryItems;

	private HashSet<String> picks = new HashSet<String>();

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_category_pick );

		RecipesDataSource datasource = new RecipesDataSource( this );
		
		// categories is a list of String, String pairs. 
		categories = datasource.getCategories();
		categoryItems = datasource.getCategoryMembers();

		String[] includeWords = getIntent().getStringArrayExtra( "picks" );
		for ( String s : includeWords ) {
			// Log.d( TAG, s );
			picks.add( s );
		}

		// this = context
		// categories = the List of HashMaps with constant "Category", "Asiatisch"
		// R.layout.activity_category_pick_category_row = the layout for the category rows
		// new String[] { "Category" } = The Hashmap entry to map to the 
		// new int[] { R.id.category } = The Textview elements they map to 
		// categoryItems = the List of List of HashMap Items
		// R.layout.activity_category_pick_item_row = the layout for the item rowm
		// new String[] { "item" } = the map entries that are to be shown
		// new int[] { R.id.item } = The Textview items to map them to
		SimpleExpandableListAdapter expListAdapter = new MySimpleExpandableListAdapter( this, categories,
				R.layout.activity_category_pick_category_row, 
				new String[] { "Category" }, 
				new int[] { R.id.category }, 
				categoryItems, 
				R.layout.activity_category_pick_item_row, 
				new String[] { "item" }, 
				new int[] { R.id.item } 
		); 


		setListAdapter( expListAdapter );

		final Button button_ok = (Button) findViewById( R.id.button_ok );
		button_ok.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				Intent resultIntent = new Intent();
				resultIntent.putExtra( "picks", picks.toArray( new String[0] ) );
				setResult( RESULT_OK, resultIntent );
				finish();
			}
		} );

		final Button button_clear = (Button) findViewById( R.id.button_clear );
		button_clear.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				Log.d( TAG, "Clear Button clicked" );
				picks.clear();
			}
		} );

	}

	/**
	 * handle the click on a category
	 */
	public boolean onChildClick( ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id ) {
		String clickMember = categoryItems.get( groupPosition )
				.get( childPosition ).get( "item" );
		Log.d( TAG, String.format( "onChildClick: %s", clickMember ) );
		CheckBox cb = (CheckBox) v.findViewById( R.id.pickcheckbox );
		if ( cb != null ) {
			if ( picks.contains( clickMember ) ) {
				picks.remove( clickMember );
				cb.setChecked( false );
			} else {
				picks.add( clickMember );
				cb.setChecked( true );
			}
		}
		return false;
	}

	/**
	 * Private class to extend the SimpleExpandableListAdapter so that I can
	 * override the getChildView method where I want to fiddle with the CheckBox
	 * so that it shows ticked if the row it is on represents a value in the
	 * picked Set and unchecked otherwise.
	 * 
	 * @author Richard Eigenmann
	 * 
	 */
	private class MySimpleExpandableListAdapter extends
			SimpleExpandableListAdapter {

		/**
		 * It seems I have to have this constructor though I don't really want
		 * it.
		 */
		public MySimpleExpandableListAdapter( Context context,
				List<? extends Map<String, ?>> groupData,
				int expandedGroupLayout, int collapsedGroupLayout,
				String[] groupFrom, int[] groupTo,
				List<? extends List<? extends Map<String, ?>>> childData,
				int childLayout, int lastChildLayout, String[] childFrom,
				int[] childTo ) {
			super( context, groupData, expandedGroupLayout,
					collapsedGroupLayout, groupFrom, groupTo, childData,
					childLayout, lastChildLayout, childFrom, childTo );

		}

		/**
		 * It seems I have to have this constructor though I don't really want
		 * it.
		 */
		public MySimpleExpandableListAdapter( Context context,
				List<? extends Map<String, ?>> groupData,
				int expandedGroupLayout, int collapsedGroupLayout,
				String[] groupFrom, int[] groupTo,
				List<? extends List<? extends Map<String, ?>>> childData,
				int childLayout, String[] childFrom, int[] childTo ) {
			super( context, groupData, expandedGroupLayout,
					collapsedGroupLayout, groupFrom, groupTo, childData,
					childLayout, childFrom, childTo );
		}

		/**
		 * It seems I have to have this constructor though I don't really want
		 * it.
		 */
		public MySimpleExpandableListAdapter( Context context,
				List<? extends Map<String, ?>> groupData, int groupLayout,
				String[] groupFrom, int[] groupTo,
				List<? extends List<? extends Map<String, ?>>> childData,
				int childLayout, String[] childFrom, int[] childTo ) {
			super( context, groupData, groupLayout, groupFrom, groupTo,
					childData, childLayout, childFrom, childTo );

		}

		/**
		 * This overridden method intercepts the Checkbox and sets it to checked
		 * if the string of the row is in the picks Set.
		 * 
		 * @see android.widget.SimpleExpandableListAdapter#getChildView(int,
		 *      int, boolean, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getChildView( int categoryPosition, int itemPosition,
				boolean isLastChild, View convertView, ViewGroup parent ) {
			View view = super.getChildView( categoryPosition, itemPosition,
					isLastChild, convertView, parent );
			String item = categoryItems.get( categoryPosition )
					.get( itemPosition ).get( "item" );
			boolean picked = picks.contains( item );
			CheckBox cb = (CheckBox) view.findViewById( R.id.pickcheckbox );
			cb.setChecked( picked );
			cb.setClickable( false );
			return view;
		}

	}


}
