package org.dyndns.richinet.orm;

/**
 * Defines a method name that can be called when downloading is finished or
 * progress has been made
 * 
 * @author Richard Eigenmann
 * 
 */
public interface DownloaderInterface {

	/**
	 * This callback gets triggered when progress has been made. This would
	 * allow a progress bar to be incremented.
	 */
	public void progressStep();

	/**
	 * The implementing class must have a method downloadDone which is used as a
	 * callback when the download is finished.
	 */
	public void downloadDone();

}
