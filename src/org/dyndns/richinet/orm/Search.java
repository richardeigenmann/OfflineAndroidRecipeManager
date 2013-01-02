package org.dyndns.richinet.orm;

import java.util.ArrayList;

import android.util.Log;

public class Search {

	/**
	 * Tag for logging
	 */
	private static final String TAG = "Search";

	private String description;

	/**
	 * @return the title
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the title to set
	 */
	public void setDescription( String description ) {
		this.description = description;
	}


	/**
	 * The id of the search
	 */
	private int searchId =-1;

	/**
	 * Returns the id of the search
	 * 
	 * @return the searchId
	 */
	public int getSearchId() {
		return searchId;
	}

	/**
	 * Sets the searchId
	 * 
	 * @param searchId
	 *            the searchId
	 */
	public void setSearchId( int searchId ) {
		this.searchId = searchId;
	}


	/**
	 * Returns the description of the search. Is used by the ArrayAdapter in the
	 * mainActivity
	 */
	@Override
	public String toString() {
		return getDescription();
	}

}
