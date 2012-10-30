package org.richinet.dyndns.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBRecipeHandler extends SQLiteOpenHelper {

	private static final String TAG = DBRecipeHandler.class.getName();

	private static final String DATABASE_NAME = "recipes.db";
	private static final int DATABASE_VERSION = 2;
	
	public static final String TABLE_RECIPES = "recipes";
	public static final String RECIPE_RECIPE_ID = "recipe_id";
	public static final String RECIPE_TITLE = "title";
	public static final String RECIPE_FILE = "file";
	public static final String RECIPE_IMAGE_FILENAME = "imagefilename";
	public static final String RECIPE_IMAGE_WIDTH = "imagewidth";
	public static final String RECIPE_IMAGE_HEIGHT = "imageheight";
	private static final String TABLE_RECIPES_CREATE = "create table "
	      + TABLE_RECIPES + "(" + RECIPE_RECIPE_ID
	      + " integer primary key autoincrement, " 
	      + RECIPE_TITLE + " text not null, "
	      + RECIPE_FILE + " text not null, "
	      + RECIPE_IMAGE_FILENAME + " text not null, "
	      + RECIPE_IMAGE_WIDTH + " int not null, "
	      + RECIPE_IMAGE_HEIGHT + " int not null"
	      + ");";

	
	public static final String TABLE_CLASSIFICATIONS = "classifications";
	public static final String CLASSIFICATIONS_CATEGORY = "category";
	public static final String CLASSIFICATIONS_MEMBER = "member";
	public static final String CLASSIFICATIONS_RECIPE_ID = "recipe_id";
	
	private static final String TABLE_CLASSIFICATIONS_CREATE = "create table "
		      + TABLE_CLASSIFICATIONS + "(" 
		      + CLASSIFICATIONS_CATEGORY + " text not null, "
		      + CLASSIFICATIONS_MEMBER + " text not null, "
			  + CLASSIFICATIONS_RECIPE_ID + " int not null "
		      + ");";		

	public DBRecipeHandler( Context context ) {
		super( context, DATABASE_NAME, null, DATABASE_VERSION );
	}

	@Override
	public void onCreate( SQLiteDatabase database ) {
		Log.d( TAG, TABLE_RECIPES_CREATE );
		database.execSQL(TABLE_RECIPES_CREATE);
		Log.d( TAG, TABLE_CLASSIFICATIONS_CREATE );
		database.execSQL(TABLE_CLASSIFICATIONS_CREATE);
	}

	@Override
	public void onUpgrade( SQLiteDatabase database, int oldVersion, int newVersion ) {
		Log.w(TAG,  "Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", which will destroy all old data");
		    database.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
		    database.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASSIFICATIONS);
		    onCreate(database);
	}

}
