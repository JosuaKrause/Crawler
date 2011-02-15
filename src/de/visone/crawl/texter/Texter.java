package de.visone.crawl.texter;

import java.net.URL;

import org.xml.sax.Attributes;

import de.visone.crawl.out.Content;
import de.visone.crawl.sys.CrawlState;
import de.visone.crawl.sys.UrlPool;

public interface Texter extends Content {

	void startTag(String tag, Attributes a);

	void endTag(String tag);

	boolean acceptString();

	void string(String str);

	void link(URL link, String text, UrlPool pool, CrawlState state);

	void img(URL img, String text, UrlPool pool, CrawlState state);

	void dispose();

}
