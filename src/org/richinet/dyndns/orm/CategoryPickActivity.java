package org.richinet.dyndns.orm;

import java.util.ArrayList;
import java.util.HashMap;
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

	private ArrayList<String> picks = new ArrayList<String>();

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_category_pick );

		RecipesDataSource datasource = new RecipesDataSource( this );
		categories = datasource.getCategories();
		categoryMembers = datasource.getCategoryMembers();

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

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		getMenuInflater().inflate( R.menu.activity_category_pick, menu );
		return true;
	}

	public void onContentChanged() {
		super.onContentChanged();
		Log.d( TAG, "onContentChanged" );
	}

	public boolean onChildClick( ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id ) {
		Log.d( TAG, "onChildClick: " + childPosition );
		CheckBox cb = (CheckBox) v.findViewById( R.id.check1 );
		if ( cb != null ) {
			cb.toggle();
			Log.d( TAG, String.format(
					"groupPosition: %d childPosition: %d id: %d",
					groupPosition, childPosition, id ) );
			String clickedString = categoryMembers.get( groupPosition )
					.get( childPosition ).get( "shadeName" );
			Log.d( TAG, String.format( "String: %s", clickedString ) );
			picks.add( clickedString );
		}
		return false;
	}

	public void onGroupExpand( int groupPosition ) {
		Log.d( TAG, "onGroupExpand: " + groupPosition );
	}

	private class MySimpleExpandableListAdapter extends
			SimpleExpandableListAdapter {

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

		public MySimpleExpandableListAdapter( Context context,
				List<? extends Map<String, ?>> groupData, int groupLayout,
				String[] groupFrom, int[] groupTo,
				List<? extends List<? extends Map<String, ?>>> childData,
				int childLayout, String[] childFrom, int[] childTo ) {
			super( context, groupData, groupLayout, groupFrom, groupTo,
					childData, childLayout, childFrom, childTo );

		}

		/* (non-Javadoc)
		 * @see android.widget.SimpleExpandableListAdapter#getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getChildView( int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent ) {
			View view = super.getChildView( groupPosition, childPosition, isLastChild,
					convertView, parent );
			Log.d(TAG, String.format("getChildView groupPosition: %d, childPosition: %d", groupPosition, childPosition));
			return view;
		}

		
		
		
	}

}
