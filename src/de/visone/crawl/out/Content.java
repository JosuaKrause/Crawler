package de.visone.crawl.out;

import java.net.URL;

import de.visone.crawl.sys.Link;

/**
 * Represents the Content of a crawled page.
 * 
 * @author Joschi
 * 
 */
public interface Content {

	/**
	 * @return The text content of the crawled page, when crawling text was
	 *         activated.
	 */
	String getText();

	/**
	 * @return A list of the links that were followed.
	 */
	Link[] getAcceptedLinks();

	/**
	 * @return A list of the links that were not followed.
	 */
	Link[] getOtherLinks();

	/**
	 * @return The URL of the crawled page.
	 */
	URL getURL();

}
