package org.dyndns.richinet.orm;

import java.util.ArrayList;
import java.util.HashMap;
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
	public SQLiteDatabase database;
	private DBHandler dbHelper;
	private static final String[] RECPIE_ALL_COLUMNS = { DBHandler.RECIPE_FILE,
			DBHandler.RECIPE_TITLE, DBHandler.RECIPE_IMAGE_FILENAME,
			DBHandler.RECIPE_IMAGE_WIDTH, DBHandler.RECIPE_IMAGE_HEIGHT };

	private static final String[] SEARCH_ALL_COLUMNS = { DBHandler.SEARCH_ID,
			DBHandler.SEARCH_DESCRIPTION };

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

		// recipe.dumpToLog();

		ContentValues values = new ContentValues();
		values.put( DBHandler.RECIPE_FILE, recipe.getFile() );
		values.put( DBHandler.RECIPE_TITLE, recipe.getTitle() );
		values.put( DBHandler.RECIPE_IMAGE_FILENAME, recipe.getImageFilename() );
		values.put( DBHandler.RECIPE_IMAGE_WIDTH, recipe.getImageWidth() );
		values.put( DBHandler.RECIPE_IMAGE_HEIGHT, recipe.getImageHeight() );
		long insertId = database.insert( DBHandler.TABLE_RECIPES, null, values );

		for ( CategoryPair categoryPair : recipe.getClassifications() ) {
			ContentValues categoryValues = new ContentValues();
			categoryValues.put( DBHandler.CLASSIFICATIONS_CATEGORY,
					categoryPair.getCategory() );
			categoryValues.put( DBHandler.CLASSIFICATIONS_MEMBER,
					categoryPair.getMember() );
			categoryValues.put( DBHandler.CLASSIFICATIONS_RECIPE_FILE,
					recipe.getFile() );
			long categoryInsertId = database.insert(
					DBHandler.TABLE_CLASSIFICATIONS, null, categoryValues );
		}
	}

	public void deleteRecipe( String fileId ) {
		String safeFileId = DatabaseUtils.sqlEscapeString( fileId );
		database.delete( DBHandler.TABLE_RECIPES, DBHandler.RECIPE_FILE + " = "
				+ safeFileId, null );
		database.delete( DBHandler.TABLE_CLASSIFICATIONS,
				DBHandler.CLASSIFICATIONS_RECIPE_FILE + " = " + safeFileId,
				null );
	}

	public void truncateRecipes( Context context ) {
		DBHandler.truncateRecipes( database, context );
	}

	/**
	 * Search recipes matching on a string.
	 * 
	 * @param searchString
	 *            the String to match on
	 * @return the list of recipes
	 */
	public List<Recipe> searchRecipes( String searchString ) {
		open();
		List<Recipe> recipes = new ArrayList<Recipe>();
		searchString = "%" + searchString + "%";

		String likeClause = DBHandler.RECIPE_TITLE + " like ?";
		Log.d( TAG, "Searching for string: " + searchString );
		Cursor cursor = database.query( DBHandler.TABLE_RECIPES,
				RECPIE_ALL_COLUMNS, likeClause, new String[] { searchString },
				null, null, DBHandler.RECIPE_TITLE, null );

		cursor.moveToFirst();
		while ( !cursor.isAfterLast() ) {
			Recipe recipe = cursorToRecipe( cursor );
			recipes.add( recipe );
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		close();
		return recipes;
	}

	/**
	 * Search recipes matching a saved query
	 * 
	 * @param searchId
	 *            the Id of the saved search
	 * @return the list of recipes
	 */
	public List<Recipe> searchRecipes( int searchId ) {
		open();

		// breaking the query up to simplify the logic and help the optimiser
		// first query builds a distinct list of the recipes that match the
		StringBuilder sqlStatement = new StringBuilder(
				"create temporary table results as select distinct c."
						+ DBHandler.CLASSIFICATIONS_RECIPE_FILE + " from "
						+ DBHandler.TABLE_CLASSIFICATIONS + " c, "
						+ DBHandler.TABLE_SEARCHPARAMS + " sp where sp."
						+ DBHandler.SEARCHPARAMS_SEARCH_ID + " = "
						+ Integer.toString( searchId ) + " and sp."
						+ DBHandler.SEARCHPARAMS_SEARCH_FIELD + " = 'I';" );
		Log.d( TAG, sqlStatement.toString() );
		database.execSQL( sqlStatement.toString() );

		sqlStatement = new StringBuilder( "select " + DBHandler.RECIPE_FILE
				+ ", " + DBHandler.RECIPE_TITLE + ", "
				+ DBHandler.RECIPE_IMAGE_FILENAME + ", "
				+ DBHandler.RECIPE_IMAGE_WIDTH + ", "
				+ DBHandler.RECIPE_IMAGE_HEIGHT + " from "
				+ DBHandler.TABLE_RECIPES + " r, temp.results tr where r."
				+ DBHandler.RECIPE_FILE + " = tr."
				+ DBHandler.CLASSIFICATIONS_RECIPE_FILE + " order by "
				+ DBHandler.RECIPE_TITLE );

		// becomes something like this:
		// select file, title, imagefilename, imagewidth, imageheight
		// from recipes r
		// where exists ( select 1 from classifications c, search_parameters sp
		// where sp.searchId = x and sp.searchField = 'I'
		// and r.file = c.recipe_file
		// and sp.searchterm = c.member )

		// and not exists ( select 1 from classifications c where r.file =
		// c.recipe_file and member in ('Cakes') )
		// order by title

		/*
		 * sqlStatement = new StringBuilder( "select " + DBHandler.RECIPE_FILE +
		 * ", " + DBHandler.RECIPE_TITLE + ", " +
		 * DBHandler.RECIPE_IMAGE_FILENAME + ", " + DBHandler.RECIPE_IMAGE_WIDTH
		 * + ", " + DBHandler.RECIPE_IMAGE_HEIGHT + " from " +
		 * DBHandler.TABLE_RECIPES + " r where exists ( select 1 from " +
		 * DBHandler.TABLE_CLASSIFICATIONS + " c, " +
		 * DBHandler.TABLE_SEARCHPARAMS + " sp where sp." +
		 * DBHandler.SEARCHPARAMS_SEARCH_ID + "=" + Integer.toString( searchId )
		 * + " and sp." + DBHandler.SEARCHPARAMS_SEARCH_FIELD + "='I' and c." +
		 * DBHandler.CLASSIFICATIONS_RECIPE_FILE + "=" + "r." +
		 * DBHandler.RECIPE_FILE + " and c." + DBHandler.CLASSIFICATIONS_MEMBER
		 * + "= sp." + DBHandler.SEARCHPARAMS_SEARCH_TERM + ") " + " order by "
		 * + DBHandler.RECIPE_TITLE );
		 */

		Log.d( TAG, sqlStatement.toString() );
		Cursor cursor = database.rawQuery( sqlStatement.toString(), null );

		cursor.moveToFirst();
		List<Recipe> recipes = new ArrayList<Recipe>();
		while ( !cursor.isAfterLast() ) {
			Recipe recipe = cursorToRecipe( cursor );
			recipes.add( recipe );
			cursor.moveToNext();
		}
		cursor.close();

		sqlStatement = new StringBuilder( "drop table temp.results" );
		Log.d( TAG, sqlStatement.toString() );
		database.execSQL( sqlStatement.toString() );

		close();
		return recipes;

	}

	/**
	 * Search recipes matching the criteria
	 * 
	 * @param includeWords
	 * @param limitWords
	 * @param excludeWords
	 * @return the list of matching recipes
	 */
	public List<Recipe> searchRecipes( String[] includeWords,
			String[] limitWords, String[] excludeWords ) {

		StringBuilder inClause = new StringBuilder( "" );
		{
			boolean notFirstIteration = false;
			for ( String s : includeWords ) {
				if ( notFirstIteration ) {
					inClause.append( ", " );
				} else {
					notFirstIteration = true;
				}
				inClause.append( DatabaseUtils.sqlEscapeString( s ) );
			}
		}

		String excludeClause = "";
		// only build the exclude clause if there is something to exclude to
		// keep the sql short and fast
		if ( excludeWords.length > 0 ) {
			StringBuilder excludeItems = new StringBuilder( "" );
			boolean notFirstIteration = false;
			for ( String s : excludeWords ) {
				if ( notFirstIteration ) {
					excludeItems.append( ", " );
				} else {
					notFirstIteration = true;
				}
				excludeItems.append( DatabaseUtils.sqlEscapeString( s ) );
			}
			excludeClause = " and not exists ( select 1 from "
					+ DBHandler.TABLE_CLASSIFICATIONS + " where "
					+ DBHandler.CLASSIFICATIONS_RECIPE_FILE + "="
					+ DBHandler.TABLE_RECIPES + "." + DBHandler.RECIPE_FILE
					+ " and " + DBHandler.CLASSIFICATIONS_MEMBER + " in ("
					+ excludeItems.toString() + ") )";
		}

		// becomes something like this:
		// select file, title, imagefilename, imagewidth, imageheight
		// from recipes r
		// where exists ( select 1 from classifications c where r.file =
		// c.recipe_file and member in ('4 Sterne') )
		// and not exists ( select 1 from classifications c where r.file =
		// c.recipe_file and member in ('Cakes') )
		// order by title

		StringBuilder sqlStatement = new StringBuilder( "select "
				+ DBHandler.RECIPE_FILE + ", " + DBHandler.RECIPE_TITLE + ", "
				+ DBHandler.RECIPE_IMAGE_FILENAME + ", "
				+ DBHandler.RECIPE_IMAGE_WIDTH + ", "
				+ DBHandler.RECIPE_IMAGE_HEIGHT + " from "
				+ DBHandler.TABLE_RECIPES + " where exists ( select 1 from "
				+ DBHandler.TABLE_CLASSIFICATIONS + " where "
				+ DBHandler.CLASSIFICATIONS_RECIPE_FILE + "="
				+ DBHandler.TABLE_RECIPES + "." + DBHandler.RECIPE_FILE
				+ " and " + DBHandler.CLASSIFICATIONS_MEMBER + " in ("
				+ inClause.toString() + ") ) " + excludeClause + " order by "
				+ DBHandler.RECIPE_TITLE );

		open();
		Log.d( TAG, sqlStatement.toString() );
		Cursor cursor = database.rawQuery( sqlStatement.toString(), null );

		cursor.moveToFirst();
		List<Recipe> recipes = new ArrayList<Recipe>();
		while ( !cursor.isAfterLast() ) {
			Recipe recipe = cursorToRecipe( cursor );
			recipes.add( recipe );
			cursor.moveToNext();
		}
		cursor.close();
		close();
		return recipes;
	}

	/**
	 * Converts a Recipe Table cursor to a Java Recipe object
	 * 
	 * @param cursor
	 *            The cursor on the recipe row
	 * @return the Recipe object
	 */
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

	/**
	 * Returns the number of recipes in the database and handles all the
	 * connection nonsense
	 * 
	 * @return the number of recipes
	 */
	public static long fetchRecipesCount( Context context ) {
		RecipesDataSource datasource;
		datasource = new RecipesDataSource( context );
		datasource.open();
		long recipesCount = datasource.fetchRecipesCount();
		datasource.close();
		return recipesCount;
	}

	/**
	 * Returns the number of searches in the database and handles all the
	 * connection nonsense
	 * 
	 * @return the number of searches
	 */
	public static long fetchSearchesCount( Context context ) {
		RecipesDataSource datasource;
		datasource = new RecipesDataSource( context );
		datasource.open();
		long recipesCount = DatabaseUtils.queryNumEntries( datasource.database,
				DBHandler.TABLE_SEARCHES );
		datasource.close();
		return recipesCount;
	}

	private static final boolean DISTINCT = true;

	/**
	 * Returns a list of String, String pairs. The first is a constant
	 * "Category" and the second is the different categories we have
	 * 
	 * @return
	 */
	public List<HashMap<String, String>> getCategories() {
		open();
		List<HashMap<String, String>> categories = new ArrayList<HashMap<String, String>>();

		Cursor cursor = database.query( DISTINCT,
				DBHandler.TABLE_CLASSIFICATIONS,
				new String[] { DBHandler.CLASSIFICATIONS_CATEGORY }, null,
				null, null, null, DBHandler.CLASSIFICATIONS_CATEGORY, null );

		cursor.moveToFirst();
		while ( !cursor.isAfterLast() ) {
			String category = cursor.getString( 0 );
			HashMap<String, String> map = new HashMap<String, String>();
			map.put( "Category", category );
			categories.add( map );
			cursor.moveToNext();
		}
		cursor.close();
		close();
		return categories;
	}

	/**
	 * Returns a list of lists
	 * 
	 * @return
	 */
	public List<List<HashMap<String, String>>> getCategoryMembers() {
		open();
		List<List<HashMap<String, String>>> categoryMembers = new ArrayList<List<HashMap<String, String>>>();

		Cursor cursor = database.query( DISTINCT,
				DBHandler.TABLE_CLASSIFICATIONS, new String[] {
						DBHandler.CLASSIFICATIONS_CATEGORY,
						DBHandler.CLASSIFICATIONS_MEMBER }, null, null, null,
				null, DBHandler.CLASSIFICATIONS_CATEGORY + ","
						+ DBHandler.CLASSIFICATIONS_MEMBER, null );

		cursor.moveToFirst();

		String currentCategory = "";
		List<HashMap<String, String>> secList = null;
		while ( !cursor.isAfterLast() ) {
			String newCategory = cursor.getString( 0 );
			String member = cursor.getString( 1 );
			if ( !( newCategory.equals( currentCategory ) ) ) {
				// we are on a new category in the table
				if ( !( secList == null ) ) {
					// attach the secondary list to the result set
					categoryMembers.add( secList );
				}
				secList = new ArrayList<HashMap<String, String>>();
				currentCategory = newCategory;
			}

			HashMap<String, String> child = new HashMap<String, String>();
			child.put( "item", member );
			secList.add( child );

			cursor.moveToNext();
		}
		cursor.close();
		close();
		return categoryMembers;
	}

	/**
	 * Returns a list of the stored searches
	 * 
	 * @param searchString
	 * @return
	 */
	public List<Search> getSearches() {
		open();
		List<Search> searches = new ArrayList<Search>();
		Cursor cursor = database.query( DBHandler.TABLE_SEARCHES,
				SEARCH_ALL_COLUMNS, null, null, null, null,
				DBHandler.SEARCH_DESCRIPTION, null );

		cursor.moveToFirst();
		while ( !cursor.isAfterLast() ) {
			Search search = cursorToSearch( cursor );
			searches.add( search );
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		close();
		return searches;
	}

	/**
	 * Converts a Search Table cursor to a Java Search object
	 * 
	 * @param cursor
	 *            The cursor on the recipe row
	 * @return the Recipe object
	 */
	private Search cursorToSearch( Cursor cursor ) {
		Search search = new Search();
		search.setSearchId( cursor.getInt( 0 ) );
		search.setDescription( cursor.getString( 1 ) );
		return search;
	}

	/**
	 * Deletes the saved search with the specified Id
	 * 
	 * @return the number of recipes
	 */
	public static void deleteSavedSearch( Context context, int searchId ) {
		RecipesDataSource datasource;
		datasource = new RecipesDataSource( context );
		datasource.open();
		int deletedRows = datasource.database.delete(
				DBHandler.TABLE_SEARCHPARAMS, DBHandler.SEARCHPARAMS_SEARCH_ID
						+ "=?", new String[] { Integer.toString( searchId ) } );
		Log.d( TAG, String.format( "%d rows deleted from table %s",
				deletedRows, DBHandler.TABLE_SEARCHPARAMS ) );

		deletedRows = datasource.database.delete( DBHandler.TABLE_SEARCHES,
				DBHandler.SEARCH_ID + "=?",
				new String[] { Integer.toString( searchId ) } );
		Log.d( TAG, String.format( "%d rows deleted from table %s",
				deletedRows, DBHandler.TABLE_SEARCHES ) );
		datasource.close();
	}

	/**
	 * Saves the search
	 * 
	 */
	public static void saveSearch( Context context, String description,
			String[] includeWords, String[] limitWords, String[] excludeWords ) {
		RecipesDataSource datasource;
		datasource = new RecipesDataSource( context );
		datasource.open();

		ContentValues contentValues = new ContentValues();
		contentValues.put( DBHandler.SEARCH_DESCRIPTION, description );
		long searchId = datasource.database.insert( DBHandler.TABLE_SEARCHES,
				null, contentValues );
		Log.d( TAG, String.format( "row %d inserted into table %s", searchId,
				DBHandler.TABLE_SEARCHES ) );

		for ( String word : includeWords ) {
			contentValues = new ContentValues();
			contentValues.put( DBHandler.SEARCHPARAMS_SEARCH_ID, searchId );
			contentValues.put( DBHandler.SEARCHPARAMS_SEARCH_FIELD, "I" );
			contentValues.put( DBHandler.SEARCHPARAMS_SEARCH_TERM, word );
			long rowId = datasource.database.insert(
					DBHandler.TABLE_SEARCHPARAMS, null, contentValues );
			Log.d( TAG, String.format( "row %d inserted into table %s", rowId,
					DBHandler.TABLE_SEARCHPARAMS ) );
		}
		for ( String word : excludeWords ) {
			contentValues = new ContentValues();
			contentValues.put( DBHandler.SEARCHPARAMS_SEARCH_ID, searchId );
			contentValues.put( DBHandler.SEARCHPARAMS_SEARCH_FIELD, "E" );
			contentValues.put( DBHandler.SEARCHPARAMS_SEARCH_TERM, word );
			long rowId = datasource.database.insert(
					DBHandler.TABLE_SEARCHPARAMS, null, contentValues );
			Log.d( TAG, String.format( "row %d inserted into table %s", rowId,
					DBHandler.TABLE_SEARCHPARAMS ) );
		}
		for ( String word : limitWords ) {
			contentValues = new ContentValues();
			contentValues.put( DBHandler.SEARCHPARAMS_SEARCH_ID, searchId );
			contentValues.put( DBHandler.SEARCHPARAMS_SEARCH_FIELD, "L" );
			contentValues.put( DBHandler.SEARCHPARAMS_SEARCH_TERM, word );
			long rowId = datasource.database.insert(
					DBHandler.TABLE_SEARCHPARAMS, null, contentValues );
			Log.d( TAG, String.format( "row %d inserted into table %s", rowId,
					DBHandler.TABLE_SEARCHPARAMS ) );
		}

		datasource.close();
	}

	/**
	 * Returns the number of categories and number of items in the database and
	 * handles all the connection nonsense
	 * 
	 * @return and array of int where [0] is the number of categories and [1] is
	 *         the number of items and [2] is the number of recipe Items.
	 */
	public static long[] fetchCategoriesCount( Context context ) {
		RecipesDataSource datasource;
		datasource = new RecipesDataSource( context );
		datasource.open();
		long[] results = new long[3];

		Cursor cursor = datasource.database.rawQuery( "Select distinct "
				+ DBHandler.CLASSIFICATIONS_CATEGORY + " from "
				+ DBHandler.TABLE_CLASSIFICATIONS, null );
		results[0] = cursor.getCount();
		cursor.close();

		cursor = datasource.database.rawQuery( "Select distinct "
				+ DBHandler.CLASSIFICATIONS_CATEGORY + ", "
				+ DBHandler.CLASSIFICATIONS_MEMBER + " from "
				+ DBHandler.TABLE_CLASSIFICATIONS, null );
		results[1] = cursor.getCount();
		cursor.close();
		
		results[2] = DatabaseUtils.queryNumEntries( datasource.database,
				DBHandler.TABLE_CLASSIFICATIONS );

		datasource.close();
		return results;
	}

}
