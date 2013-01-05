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
import android.view.Menu;
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

	private SimpleExpandableListAdapter expListAdapter;
	private List<HashMap<String, String>> categories;
	private List<List<HashMap<String, String>>> categoryMembers;

	private HashSet<String> picks = new HashSet<String>();

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_category_pick );

		RecipesDataSource datasource = new RecipesDataSource( this );
		categories = datasource.getCategories();
		categoryMembers = datasource.getCategoryMembers();

		String[] includeWords = getIntent().getStringArrayExtra( "picks" );
		for ( String s : includeWords ) {
			// Log.d( TAG, s );
			picks.add( s );
		}

		expListAdapter = new MySimpleExpandableListAdapter( this, categories, // groupData
																				// describes
																				// the
																				// first-level
																				// entries
				R.layout.group_row, // Layout for the first-level entries
				new String[] { "colorName" }, // Key in the groupData maps to
												// display
				new int[] { R.id.childname }, // Data under "colorName" key goes
												// into this TextView
				categoryMembers, // childData describes second-level entries
				R.layout.child_row, // Layout for second-level entries
				new String[] { "shadeName", "rgb" }, // Keys in childData maps
														// to display
				new int[] { R.id.childname, R.id.rgb } // Data under the keys
														// above go into these
														// TextViews
		);
		setListAdapter( expListAdapter );

		final Button button_ok = (Button) findViewById( R.id.button_ok );
		button_ok.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				Log.d( TAG, "OK Button clicked" );
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
			}
		} );

	}

	/**
	 * handle the click on a category
	 */
	public boolean onChildClick( ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id ) {
		String clickMember = categoryMembers.get( groupPosition )
				.get( childPosition ).get( "shadeName" );
		Log.d( TAG, String.format( "onChildClick: %s", clickMember ) );
		CheckBox cb = (CheckBox) v.findViewById( R.id.check1 );
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
		 * This overriden method intercepts the Checkbox and sets it to checked
		 * if the string of the row is in the picks Set.
		 * 
		 * @see android.widget.SimpleExpandableListAdapter#getChildView(int,
		 *      int, boolean, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getChildView( int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent ) {
			View view = super.getChildView( groupPosition, childPosition,
					isLastChild, convertView, parent );
			// String member = categoryMembers.get( groupPosition )
			// .get( childPosition ).get( "shadeName" );
			boolean picked = isPicked( groupPosition, childPosition );
			/*
			 * Log.d( TAG, String.format(
			 * "getChildView groupPosition: %d, childPosition: %d member: %s picked: %b"
			 * , groupPosition, childPosition, member, picked ) );
			 */
			CheckBox cb = (CheckBox) view.findViewById( R.id.check1 );
			cb.setChecked( picked );
			cb.setClickable( false );
			return view;
		}

	}

	/**
	 * Returns true if the group and child position refer to an item that is in
	 * the picked Set.
	 * 
	 * @param groupPosition
	 *            the group in the categoryMembers List
	 * @param childPosition
	 *            the child in the categoryMembers List
	 * @return true if this is a picked item, false if not.
	 */
	private boolean isPicked( int groupPosition, int childPosition ) {
		String member = categoryMembers.get( groupPosition )
				.get( childPosition ).get( "shadeName" );
		return picks.contains( member );
	}

}
