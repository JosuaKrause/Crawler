package de.visone.crawl;

import java.util.LinkedList;
import java.util.List;

import de.visone.crawl.accept.LinkAccepter;
import de.visone.crawl.rules.HtmlQuery;
import de.visone.crawl.rules.RuleManager;

/**
 * This class holds the settings to configure the crawler.
 * 
 * @see Crawler
 * 
 * @author Joschi
 * 
 */
public class Settings {

	/**
	 * A list of common user agents.
	 */
	public static String[] userAgents = new String[] {
			"Mozilla/5.0 (Windows; U; Windows NT 6.1; de; rv:1.9.2.12) Gecko/20101026 Firefox/3.6.12 ( .NET CLR 3.5.30729; .NET CLR 4.0.20506)",
			"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)",
			"Mozilla/2.0 (compatible; Ask Jeeves/Teoma)",
			"Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
			"Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)",
			"msnbot/1.0 (+http://search.msn.com/msnbot.htm)",
			"Lynx/2.8.4rel.1 libwww-FM/2.14 SSL-MM/1.4.1 OpenSSL/0.9.6c",
			"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; WOW64; Trident/4.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; OfficeLiveConnector.1.3; OfficeLivePatch.0.0; .NET CLR 3.5.30729; .NET CLR 3.0.30618)",
			"Mozilla/4.77 [en] (X11; I; IRIX;64 6.5 IP30)",
			"Mozilla/5.0 (compatible; Konqueror/3.2; Linux 2.6.2) (KHTML, like Gecko)",
			"Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/125.2 (KHTML, like Gecko) Safari/125.8",
			"Mozilla/5.0 (OS/2; U; Warp 4.5; de; rv:1.8.1.11) Gecko/20071129 PmWFx/2.0.0.11",
			"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.A.B.C Safari/525.13",
			"Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.9.0.10) Gecko/2009042316 Firefox/3.0.10",
			"Mozilla/5.0 (X11; U; Linux i586; en-US; rv:1.7.3) Gecko/20040924 Epiphany/1.4.4 (Ubuntu)",
			"Opera/9.80 (Macintosh; Intel Mac OS X; U; en) Presto/2.2.15 Version/10.00", };

	/**
	 * The user agent for the HTTP connection. The default value denotes a
	 * Firefox-Browser.
	 */
	public String userAgent = userAgents[0];

	/**
	 * The mean delay between two network requests in milliseconds. The actual
	 * time is chosen randomly. The default value is <code>1000</code> or 1
	 * second.
	 */
	public long meanDelay = 1000;

	// TODO: implement it
	public long forcedTimeoutAfter = 20000;

	// TODO: implement it
	public int maxRetries = 3;

	/**
	 * The standard HTML-Query to filter links and text. The default query takes
	 * everything below the <code>&lt;body&gt;</code> node and ignores script-
	 * and style-nodes.
	 */
	public HtmlQuery query = new HtmlQuery("std");

	/**
	 * The maximum crawling depth. Note that values larger than 4 can give huge
	 * results. The default value is 2.
	 */
	public int maxDepth = 2;

	/**
	 * The number of pages to crawl before the crawler stops. This can be handy
	 * when fine-tuning the crawler. If the value is zero, the crawling stops
	 * only after the maximum depth ({@link #maxDepth}) is reached. The default
	 * value is zero.
	 */
	public int killLimit = 0;

	/**
	 * Whether to ignore links to other domains. The default ignores other
	 * domains.
	 */
	public boolean onlySameHost = true;

	/**
	 * When set, the attribute <code>rel=&quot;nofollow&quot;</code> in links is
	 * interpreted. The default does so.
	 */
	public boolean readNoFollow = true;

	/**
	 * Whether to use domain specific rules / queries or just the one given by
	 * {@link #query}. The default is <code>true</code>.
	 * 
	 * @see RuleManager
	 */
	public boolean domainSpecific = true;

	/**
	 * When set, the crawling process terminates after the first error occurs
	 * while crawling. This can be used to prevent blacklisting the own IP from
	 * the crawled domain, when the crawler is configured to eager. This option
	 * can make the use of the crawler very safe, but it can be annoying when
	 * other / similar errors occur without being blacklisted. The default
	 * behavior is to never stop crawling because of errors.
	 * 
	 * @see #coolDown
	 */
	public boolean haltOnError = false;

	/**
	 * EXPERIMENTAL! Accepts cookies from the first URL.
	 */
	// TODO: on demand
	public boolean acceptCookies = false;

	/**
	 * Returns the crawled text as XML. The default is <code>false</code>.
	 */
	public boolean xmlText = false;

	/**
	 * Whether to crawl text. The default crawls text.
	 */
	public boolean doText = true;

	/**
	 * Whether to crawl links. The default crawls links.
	 */
	public boolean doLinks = true;

	/**
	 * EXPERIMENTAL! Crawls in otherwise authority restricted areas. The default
	 * does nothing.
	 * 
	 * @see #authorizationPassword
	 */
	// TODO: on demand
	public String authorizationName = null;

	/**
	 * EXPERIMENTAL! The password for the authorization.
	 * 
	 * @see #authorizationName
	 */
	// TODO: on demand
	public String authorizationPassword = null;

	/**
	 * The time needed to recover from an error in milliseconds. When an error
	 * occurs the program will halt for this amount of time and then continue
	 * again. This option is only in effect, when {@link #haltOnError} is not
	 * active.
	 * 
	 * @see #haltOnError
	 */
	public int coolDown = 5000;

	/**
	 * A list containing additional / customized {@link LinkAccepter
	 * LinkAccepters}. It is empty by default.
	 */
	public final List<LinkAccepter> customAccepter = new LinkedList<LinkAccepter>();

	/**
	 * Creates settings with default values.
	 */
	public Settings() {
		HtmlQuery.addStd(query);
	}

}
