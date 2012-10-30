package org.richinet.dyndns.orm;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class RecipesDataSource {

	/**
	 * Tag for logging
	 */
	private static final String TAG = "RecipesDataSource";

	// Database fields
	private SQLiteDatabase database;
	private DBHandler dbHelper;
	private String[] allColumns = { DBHandler.RECIPE_FILE,
			DBHandler.RECIPE_TITLE, DBHandler.RECIPE_IMAGE_FILENAME,
			DBHandler.RECIPE_IMAGE_WIDTH, DBHandler.RECIPE_IMAGE_HEIGHT };

	public RecipesDataSource( Context context ) {
		dbHelper = new DBHandler( context );
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void insertRecipe( Recipe recipe ) {
		deleteRecipe( recipe.getFile() );
		
		//recipe.dumpToLog();

		ContentValues values = new ContentValues();
		values.put( DBHandler.RECIPE_FILE, recipe.getFile() );
		values.put( DBHandler.RECIPE_TITLE, recipe.getTitle() );
		values.put( DBHandler.RECIPE_IMAGE_FILENAME, recipe.getImageFilename() );
		values.put( DBHandler.RECIPE_IMAGE_WIDTH, recipe.getImageWidth() );
		values.put( DBHandler.RECIPE_IMAGE_HEIGHT, recipe.getImageHeight() );
		long insertId = database.insert( DBHandler.TABLE_RECIPES, null, values );
		// Cursor cursor = database.query( DBHandler.TABLE_RECIPES, allColumns,
		// DBHandler.CLASSIFICATIONS_RECIPE_ID + " = " + insertId, null,
		// null, null, null );
		// cursor.moveToFirst();
		// Recipe newRecipe = cursorToRecipe( cursor );
		// cursor.close();

		for ( CategoryPair categoryPair : recipe.getClassifications() ) {
			ContentValues categoryValues = new ContentValues();
			categoryValues.put( DBHandler.CLASSIFICATIONS_CATEGORY,
					categoryPair.getCategory() );
			categoryValues.put( DBHandler.CLASSIFICATIONS_MEMBER,
					categoryPair.getMember() );
			categoryValues.put( DBHandler.CLASSIFICATIONS_RECIPE_FILE, recipe.getFile() );
			long categoryInsertId = database.insert(
					DBHandler.TABLE_CLASSIFICATIONS, null, categoryValues );
		}
	}

	public void deleteRecipe( String fileId ) {
		String safeFileId = DatabaseUtils.sqlEscapeString( fileId );
		//System.out.println( "Recipe deleted where File = " + safeFileId );
		database.delete( DBHandler.TABLE_RECIPES, DBHandler.RECIPE_FILE + " = "
				+ safeFileId, null );
		database.delete( DBHandler.TABLE_CLASSIFICATIONS, DBHandler.CLASSIFICATIONS_RECIPE_FILE + " = "
				+ safeFileId, null );
	}

	public List<Recipe> searchRecipes( String searchString ) {
		List<Recipe> recipes = new ArrayList<Recipe>();
		searchString = "%" + searchString + "%";

		String likeClause = DBHandler.RECIPE_TITLE + " like ?";
		Log.d( TAG, "Like: " + likeClause );
		Log.d( TAG, "SearchString: " + searchString );
		Cursor cursor = database.query( DBHandler.TABLE_RECIPES, allColumns,
				likeClause, new String[] { searchString }, null, null, null,
				null );

		cursor.moveToFirst();
		while ( !cursor.isAfterLast() ) {
			Recipe recipe = cursorToRecipe( cursor );
			recipes.add( recipe );
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return recipes;
	}

	private Recipe cursorToRecipe( Cursor cursor ) {
		Recipe recipe = new Recipe();
		recipe.setFile( cursor.getString( 0 ) );
		recipe.setTitle( cursor.getString( 1 ) );
		recipe.setImageFilename( cursor.getString( 2 ) );
		recipe.setImageWidth( cursor.getInt( 3 ) );
		recipe.setImageHeight( cursor.getInt( 4 ) );
		return recipe;
	}

	/**
	 * Returns the number of recipes in the database
	 * 
	 * @return the number of recipes
	 */
	public long fetchRecipesCount() {
		return DatabaseUtils
				.queryNumEntries( database, DBHandler.TABLE_RECIPES );
	}

}
