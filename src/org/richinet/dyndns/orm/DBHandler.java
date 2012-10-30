package org.richinet.dyndns.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHandler extends SQLiteOpenHelper {

	private static final String TAG = DBHandler.class.getName();

	private static final String DATABASE_NAME = "recipes.db";
	private static final int DATABASE_VERSION = 5;
	
	public static final String TABLE_RECIPES = "recipes";
	public static final String RECIPE_TITLE = "title";
	public static final String RECIPE_FILE = "file";
	public static final String RECIPE_IMAGE_FILENAME = "imagefilename";
	public static final String RECIPE_IMAGE_WIDTH = "imagewidth";
	public static final String RECIPE_IMAGE_HEIGHT = "imageheight";
	private static final String TABLE_RECIPES_CREATE = "create table "
	      + TABLE_RECIPES + "("  
	      + RECIPE_FILE + " text primary key not null, "
	      + RECIPE_TITLE + " text not null, "
	      + RECIPE_IMAGE_FILENAME + " text not null, "
	      + RECIPE_IMAGE_WIDTH + " int not null, "
	      + RECIPE_IMAGE_HEIGHT + " int not null"
	      + ");";

	
	public static final String TABLE_CLASSIFICATIONS = "classifications";
	public static final String CLASSIFICATIONS_RECIPE_FILE = "recipe_file";
	public static final String CLASSIFICATIONS_CATEGORY = "category";
	public static final String CLASSIFICATIONS_MEMBER = "member";
	
	private static final String TABLE_CLASSIFICATIONS_CREATE = "create table "
		      + TABLE_CLASSIFICATIONS + "(" 
		      + CLASSIFICATIONS_CATEGORY + " text not null, "
		      + CLASSIFICATIONS_MEMBER + " text not null, "
			  + CLASSIFICATIONS_RECIPE_FILE + " text not null "
			  //+ ", primary key ("
			  //+ CLASSIFICATIONS_CATEGORY + ", " + CLASSIFICATIONS_MEMBER + ") "
		      + ");";		

	public DBHandler( Context context ) {
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
