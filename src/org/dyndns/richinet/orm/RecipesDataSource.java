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

		StringBuilder sqlStatement = new StringBuilder( "select "
				+ DBHandler.SEARCHPARAMS_SEARCH_FIELD + ", "
				+ DBHandler.SEARCHPARAMS_SEARCH_TERM + " from "
				+ DBHandler.TABLE_SEARCHPARAMS + " where "
				+ DBHandler.SEARCHPARAMS_SEARCH_ID + " = "
				+ Integer.toString( searchId ) );

		Log.d( TAG, sqlStatement.toString() );
		Cursor cursor = database.rawQuery( sqlStatement.toString(), null );

		cursor.moveToFirst();
		List<String> includeWords = new ArrayList<String>();
		List<String> limitWords = new ArrayList<String>();
		List<String> excludeWords = new ArrayList<String>();

		while ( !cursor.isAfterLast() ) {
			String type = cursor.getString( 0 );
			// wanted to use switch but it said that is only valid in Java 7
			if ( "I".equals( type ) ) {
				includeWords.add( cursor.getString( 1 ) );
			} else if ( "L".equals( type ) ) {
				limitWords.add( cursor.getString( 1 ) );
			} else {
				excludeWords.add( cursor.getString( 1 ) );
			}

			cursor.moveToNext();
		}
		cursor.close();
		close();

		return searchRecipes( includeWords.toArray( new String[0] ),
				limitWords.toArray( new String[0] ),
				excludeWords.toArray( new String[0] ) );

	}

	/**
	 * Search recipes matching the criteria.
	 * 
	 * Examples: Include: Zitrone, Rüebli Limit: Cakes, Desserts Exclude: Rahm
	 * --> Aargauer Rüeblitorte because: includes either Zitrone or Rüebli it is
	 * part of the universe of 'Cakes' and 'Desserts' it is not in the universe
	 * of 'Rahm'
	 * 
	 * @param includeWords
	 * @param limitWords
	 * @param excludeWords
	 * @return the list of matching recipes
	 */
	public List<Recipe> searchRecipes( String[] includeWords,
			String[] limitWords, String[] excludeWords ) {

		/**
		 * This becomes exists ( select 1 from classifications where recipes =
		 */
		StringBuilder inClause = new StringBuilder( "exists ( select 1 from "
				+ DBHandler.TABLE_CLASSIFICATIONS + " where "
				+ DBHandler.CLASSIFICATIONS_RECIPE_FILE + "="
				+ DBHandler.TABLE_RECIPES + "." + DBHandler.RECIPE_FILE
				+ " and " + DBHandler.CLASSIFICATIONS_MEMBER + " in (" );
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
		inClause.append( ") ) " );
		Log.d( TAG, "inClause: " + inClause.toString() );

		StringBuilder limitClause = new StringBuilder( "" );
		// only build the limit clause if there is something to exclude to
		// keep the sql short and fast
		if ( limitWords.length > 0 ) {
			limitClause.append( " and exists ( select 1 from "
					+ DBHandler.TABLE_CLASSIFICATIONS + " where "
					+ DBHandler.CLASSIFICATIONS_RECIPE_FILE + "="
					+ DBHandler.TABLE_RECIPES + "." + DBHandler.RECIPE_FILE
					+ " and " + DBHandler.CLASSIFICATIONS_MEMBER + " in (" );
			{
				boolean notFirstIteration = false;
				for ( String s : limitWords ) {
					if ( notFirstIteration ) {
						limitClause.append( ", " );
					} else {
						notFirstIteration = true;
					}
					limitClause.append( DatabaseUtils.sqlEscapeString( s ) );
				}
			}
			limitClause.append( ") ) " );
		}
		Log.d( TAG, "limitClause: " + limitClause.toString() );

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

		Log.d( TAG, "excludeClause: " + excludeClause.toString() );

		// becomes something like this:
		// select file, title, imagefilename, imagewidth, imageheight
		// from recipes r
		// where exists ( select 1 from classifications c where r.file =
		// c.recipe_file and member in ('Zitronen', 'Rüebli') )
		// and exists ( select 1 from classifications c where r.file =
		// c.recipe_file and member in ('Desserts', 'Cakes') )
		// and not exists ( select 1 from classifications c where r.file =
		// c.recipe_file and member in ('Rahm') )
		// order by title

		StringBuilder sqlStatement = new StringBuilder( "select "
				+ DBHandler.RECIPE_FILE + ", " + DBHandler.RECIPE_TITLE + ", "
				+ DBHandler.RECIPE_IMAGE_FILENAME + ", "
				+ DBHandler.RECIPE_IMAGE_WIDTH + ", "
				+ DBHandler.RECIPE_IMAGE_HEIGHT + " from "
				+ DBHandler.TABLE_RECIPES + " where " + inClause.toString()
				+ limitClause.toString() + excludeClause + " order by "
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

	/**
	 * defined here to make it obvious what the true for distinct is about.
	 */
	private static final boolean DISTINCT = true;

	/**
	 * A List of String, String pairs. The first is a constant "Category" and
	 * the second is the different categories we have
	 */
	protected List<HashMap<String, String>> categoriesList = null;

	/**
	 * A List per category of a List of String, String pairs for the items
	 */
	protected List<List<HashMap<String, String>>> categoryItemsList = null;

	/**
	 * Returns a list of String, String pairs. The first is a constant
	 * "Category" and the second is the different categories we have
	 * 
	 * @return
	 */
	public List<HashMap<String, String>> getCategoriesList() {
		if ( categoriesList == null ) {
			cacheCategoriesLists();
		}
		return categoriesList;
	}

	/**
	 * Returns a list of categories where each entry is a list of hashmap
	 * entries for each item
	 * 
	 * @return
	 */
	public List<List<HashMap<String, String>>> getCategoryItemsList() {
		if ( categoriesList == null ) {
			cacheCategoriesLists();
		}
		return categoryItemsList;
	}

	/**
	 * builds a the list of categories and list of category items
	 * 
	 * @return
	 */
	public void cacheCategoriesLists() {
		open();

		List<HashMap<String, String>> newCategoriesList = new ArrayList<HashMap<String, String>>();
		List<List<HashMap<String, String>>> newCategoryItemsList = new ArrayList<List<HashMap<String, String>>>();

		Cursor cursor = database.query( DISTINCT, // boolean distinct
				DBHandler.TABLE_CLASSIFICATIONS, // table
				new String[] { DBHandler.CLASSIFICATIONS_CATEGORY,
						DBHandler.CLASSIFICATIONS_MEMBER } // columns
				, null, null, null, null, DBHandler.CLASSIFICATIONS_CATEGORY
						+ "," + DBHandler.CLASSIFICATIONS_MEMBER // order by
				, null );

		cursor.moveToFirst();

		String category, currentCategory = "";
		List<HashMap<String, String>> itemsList = null;
		while ( !cursor.isAfterLast() ) {
			category = cursor.getString( 0 );
			String item = cursor.getString( 1 );
			if ( !( category.equals( currentCategory ) ) ) {
				// we are on a new category in the table
				HashMap<String, String> categoryMap = new HashMap<String, String>();
				categoryMap.put( "Category", category );
				newCategoriesList.add( categoryMap );

				itemsList = new ArrayList<HashMap<String, String>>();
				newCategoryItemsList.add( itemsList );
				currentCategory = category;
			}

			HashMap<String, String> child = new HashMap<String, String>();
			child.put( "Item", item );
			itemsList.add( child );

			cursor.moveToNext();
		}
		cursor.close();
		close();

		categoriesList = newCategoriesList;
		categoryItemsList = newCategoryItemsList;
	}

	/**
	 * dumps the categories list to the log.
	 */
	public void dumpCategoriesList() {
		int i = 0;
		for ( HashMap<String, String> map : categoriesList ) {
			for ( String key : map.keySet() ) {
				Log.d( TAG,
						String.format( "i: %d, key: %s, value: %s", i, key,
								map.get( key ) ) );
				i++;
			}
		}
	}

	/**
	 * dump category Items List
	 */
	public void dumpCategoryItemsList() {
		int i = 0, j = 0;
		for ( List<HashMap<String, String>> list : categoryItemsList ) {
			for ( HashMap<String, String> map : list ) {
				for ( String key : map.keySet() ) {
					Log.d( TAG, String.format(
							"list: %d, map: %d, key: %s, value: %s", i, j, key,
							map.get( key ) ) );
					j++;
				}
			}
			i++;
		}
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
	 * Returns a cursor for the save searches
	 * 
	 * @return
	 */
	public Cursor getSavedSearchesAsCursor() {
		open();
		Cursor cursor = database.query( DBHandler.TABLE_SEARCHES,
				new String[] { DBHandler.SEARCH_ID + " as _id",
						DBHandler.SEARCH_DESCRIPTION }, null, null, null, null,
				DBHandler.SEARCH_DESCRIPTION, null );
		cursor.moveToFirst();
		return cursor;
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
		Log.d( TAG, String.format(
				"deleteSavedSearch called for search Id: %d", searchId ) );
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
