package de.visone.crawl.accept;

import java.net.URL;

import org.xml.sax.Attributes;

import de.visone.crawl.sys.CrawlState;

public class NoFollowAccepter implements LinkAccepter {

	@Override
	public boolean accept(final URL url, final CrawlState state) {
		final Attributes a = state.getAttributes();
		if (a == null) {
			return true;
		}
		final String rel = a.getValue("rel");
		if (rel == null) {
			return true;
		}
		return !rel.equals("nofollow");
	}

}
