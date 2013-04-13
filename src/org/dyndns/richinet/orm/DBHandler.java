package org.dyndns.richinet.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHandler extends SQLiteOpenHelper {

	private static final String TAG = DBHandler.class.getName();

	private static final String DATABASE_NAME = "recipes.db";
	private static final int DATABASE_VERSION = 12;

	public static final String TABLE_RECIPES = "recipes";
	public static final String RECIPE_TITLE = "title";
	public static final String RECIPE_FILE = "file";
	public static final String RECIPE_IMAGE_FILENAME = "imagefilename";
	public static final String RECIPE_IMAGE_WIDTH = "imagewidth";
	public static final String RECIPE_IMAGE_HEIGHT = "imageheight";
	private static final String TABLE_RECIPES_CREATE = "create table "
			+ TABLE_RECIPES + "(" + RECIPE_FILE
			+ " text primary key not null, " + RECIPE_TITLE
			+ " text not null, " + RECIPE_IMAGE_FILENAME + " text not null, "
			+ RECIPE_IMAGE_WIDTH + " int not null, " + RECIPE_IMAGE_HEIGHT
			+ " int not null" + ");";

	public static final String INDEX_RECIPE_TITLE = "recipe_title_index";
	private static final String INDEX_RECIPE_TITLE_CREATE = "create index "
			+ INDEX_RECIPE_TITLE + " on " + TABLE_RECIPES + "(" + RECIPE_TITLE
			+ " asc );";
	public static final String INDEX_RECIPE_FILE = "recipe_file_index";
	private static final String INDEX_RECIPE_FILE_CREATE = "create index "
			+ INDEX_RECIPE_FILE + " on " + TABLE_RECIPES + "(" + RECIPE_FILE
			+ " asc );";

	public static final String TABLE_CLASSIFICATIONS = "classifications";
	public static final String CLASSIFICATIONS_RECIPE_FILE = "recipe_file";
	public static final String CLASSIFICATIONS_CATEGORY = "category";
	public static final String CLASSIFICATIONS_MEMBER = "member";
	public static final String INDEX_CLASSIFICATIONS_CATEGORY = "classifications_category";
	public static final String INDEX_CLASSIFICATIONS_MEMBER = "classifications_member";
	private static final String TABLE_CLASSIFICATIONS_CREATE = "create table "
			+ TABLE_CLASSIFICATIONS + "(" + CLASSIFICATIONS_RECIPE_FILE
			+ " text not null " + CLASSIFICATIONS_CATEGORY + " text not null, "
			+ CLASSIFICATIONS_MEMBER + " text not null, " + ");";

	private static final String INDEX_CLASSIFICATIONS_CATEGORY_CREATE = "create index "
			+ INDEX_CLASSIFICATIONS_CATEGORY
			+ " on "
			+ TABLE_CLASSIFICATIONS
			+ "("
			+ CLASSIFICATIONS_CATEGORY
			+ ","
			+ CLASSIFICATIONS_MEMBER
			+ ", " + CLASSIFICATIONS_RECIPE_FILE + ");";

	private static final String INDEX_CLASSIFICATIONS_MEMBER_CREATE = "create index "
			+ INDEX_CLASSIFICATIONS_MEMBER
			+ " on "
			+ TABLE_CLASSIFICATIONS
			+ "("
			+ CLASSIFICATIONS_MEMBER
			+ ", "
			+ CLASSIFICATIONS_RECIPE_FILE
			+ ");";

	public static final String TABLE_SEARCHES = "searches";
	public static final String SEARCH_ID = "search_id";
	public static final String SEARCH_DESCRIPTION = "description";
	private static final String TABLE_SEARCHES_CREATE = "create table "
			+ TABLE_SEARCHES + "(" + SEARCH_ID
			+ " integer primary key autoincrement, " + SEARCH_DESCRIPTION
			+ " text not null " + ");";

	public static final String INDEX_SEARCHES_DESCRIPTION = "searches_description";
	private static final String INDEX_SEARCHES_CREATE = "create index "
			+ INDEX_SEARCHES_DESCRIPTION + " on " + TABLE_SEARCHES + "("
			+ SEARCH_DESCRIPTION + " asc );";

	public static final String TABLE_SEARCHPARAMS = "search_parameters";
	public static final String SEARCHPARAMS_SEARCH_ID = "search_id";
	public static final String SEARCHPARAMS_SEARCH_FIELD = "field";
	public static final String SEARCHPARAMS_SEARCH_TERM = "searchterm";
	private static final String TABLE_SEARCHPARAMS_CREATE = "create table "
			+ TABLE_SEARCHPARAMS + "(" + SEARCHPARAMS_SEARCH_ID
			+ " integer not null, " + SEARCHPARAMS_SEARCH_FIELD
			+ " text not null, " + SEARCHPARAMS_SEARCH_TERM + " text not null "
			+ ");";

	public static final String INDEX_SEARCHPARAMS = "search_parameters_index";
	private static final String INDEX_SEARCHPARAMS_CREATE = "create index "
			+ INDEX_SEARCHPARAMS + " on " + TABLE_SEARCHPARAMS + "("
			+ SEARCHPARAMS_SEARCH_ID + " asc, " + SEARCHPARAMS_SEARCH_FIELD
			+ " asc);";

	/**
	 * Remember the Context
	 */
	private Context context;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The context
	 */
	public DBHandler( Context context ) {
		super( context, DATABASE_NAME, null, DATABASE_VERSION );
		this.context = context;
	}

	/**
	 * Sets up the tables and indexes
	 */
	@Override
	public void onCreate( SQLiteDatabase database ) {
		Log.d( TAG, TABLE_RECIPES_CREATE );
		database.execSQL( TABLE_RECIPES_CREATE );
		Log.d( TAG, INDEX_RECIPE_TITLE_CREATE );
		database.execSQL( INDEX_RECIPE_TITLE_CREATE );
		Log.d( TAG, INDEX_RECIPE_FILE_CREATE );
		database.execSQL( INDEX_RECIPE_FILE_CREATE );

		Log.d( TAG, TABLE_CLASSIFICATIONS_CREATE );
		database.execSQL( TABLE_CLASSIFICATIONS_CREATE );
		Log.d( TAG, INDEX_CLASSIFICATIONS_CATEGORY_CREATE );
		database.execSQL( INDEX_CLASSIFICATIONS_CATEGORY_CREATE );
		Log.d( TAG, INDEX_CLASSIFICATIONS_MEMBER_CREATE );
		database.execSQL( INDEX_CLASSIFICATIONS_MEMBER_CREATE );

		Log.d( TAG, TABLE_SEARCHES_CREATE );
		database.execSQL( TABLE_SEARCHES_CREATE );
		Log.d( TAG, INDEX_SEARCHES_CREATE );
		database.execSQL( INDEX_SEARCHES_CREATE );

		Log.d( TAG, TABLE_SEARCHPARAMS_CREATE );
		database.execSQL( TABLE_SEARCHPARAMS_CREATE );
		Log.d( TAG, INDEX_SEARCHPARAMS_CREATE );
		database.execSQL( INDEX_SEARCHPARAMS_CREATE );

		insertPredefinedSearches( database );

		StaticAppStuff.wipeLastDownloadTimestamp( context );
	}

	/**
	 * Inserts predefined searches
	 * 
	 * @param database
	 */
	private void insertPredefinedSearches( SQLiteDatabase database ) {
		String insert = "insert into " + TABLE_SEARCHES + " ("
				+ SEARCH_DESCRIPTION + ") values ( \"3 und 4 Sterne\")";
		Log.d( TAG, insert );
		database.execSQL( insert );

		insert = "insert into " + TABLE_SEARCHPARAMS + " ("
				+ SEARCHPARAMS_SEARCH_ID + "," + SEARCHPARAMS_SEARCH_FIELD
				+ "," + SEARCHPARAMS_SEARCH_TERM
				+ ") values ( 1, \"I\", \"3 Sterne\")";
		Log.d( TAG, insert );
		database.execSQL( insert );
		insert = "insert into " + TABLE_SEARCHPARAMS + " ("
				+ SEARCHPARAMS_SEARCH_ID + "," + SEARCHPARAMS_SEARCH_FIELD
				+ "," + SEARCHPARAMS_SEARCH_TERM
				+ ") values ( 1, \"I\", \"4 Sterne\")";
		Log.d( TAG, insert );
		database.execSQL( insert );

		insert = "insert into " + TABLE_SEARCHES + " (" + SEARCH_DESCRIPTION
				+ ") values ( \"Hauptgerichte\")";
		Log.d( TAG, insert );
		database.execSQL( insert );
		insert = "insert into " + TABLE_SEARCHPARAMS + " ("
				+ SEARCHPARAMS_SEARCH_ID + "," + SEARCHPARAMS_SEARCH_FIELD
				+ "," + SEARCHPARAMS_SEARCH_TERM
				+ ") values ( 2, \"I\", \"Hauptgerichte\")";
		Log.d( TAG, insert );
		database.execSQL( insert );

		insert = "insert into " + TABLE_SEARCHES + " (" + SEARCH_DESCRIPTION
				+ ") values ( \"Desserts\")";
		Log.d( TAG, insert );
		database.execSQL( insert );
		insert = "insert into " + TABLE_SEARCHPARAMS + " ("
				+ SEARCHPARAMS_SEARCH_ID + "," + SEARCHPARAMS_SEARCH_FIELD
				+ "," + SEARCHPARAMS_SEARCH_TERM
				+ ") values ( 3, \"I\", \"Desserts\")";
		Log.d( TAG, insert );
		database.execSQL( insert );
	}

	/**
	 * Handles upgrades by dropping the old indexes and tables and creating new
	 * ones.
	 */
	@Override
	public void onUpgrade( SQLiteDatabase database, int oldVersion,
			int newVersion ) {
		Log.w( TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data" );
		database.execSQL( "DROP INDEX IF EXISTS " + INDEX_RECIPE_TITLE );
		database.execSQL( "DROP TABLE IF EXISTS " + TABLE_RECIPES );
		database.execSQL( "DROP INDEX IF EXISTS "
				+ INDEX_CLASSIFICATIONS_CATEGORY );
		database.execSQL( "DROP INDEX IF EXISTS "
				+ INDEX_CLASSIFICATIONS_MEMBER );
		database.execSQL( "DROP TABLE IF EXISTS " + TABLE_CLASSIFICATIONS );
		database.execSQL( "DROP INDEX IF EXISTS " + INDEX_SEARCHES_DESCRIPTION );
		database.execSQL( "DROP INDEX IF EXISTS " + INDEX_SEARCHES_DESCRIPTION );
		database.execSQL( "DROP TABLE IF EXISTS " + TABLE_SEARCHES );
		database.execSQL( "DROP INDEX IF EXISTS " + INDEX_SEARCHPARAMS );
		database.execSQL( "DROP TABLE IF EXISTS " + TABLE_SEARCHPARAMS );

		onCreate( database );
	}

	/**
	 * This method wipes all the recipes from the database and resets the last
	 * download timestamp
	 * 
	 * @param database
	 *            The database to wipe
	 */
	public static void truncateRecipes( SQLiteDatabase database, Context context ) {
		database.delete( TABLE_RECIPES, null, null );
		database.delete( TABLE_CLASSIFICATIONS, null, null );
		StaticAppStuff.wipeLastDownloadTimestamp( context );
	}

}
