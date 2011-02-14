package de.visone.crawl.accept;

import java.net.URL;

import de.visone.crawl.sys.CrawlState;

public class OnlySameHost implements LinkAccepter {

	@Override
	public boolean accept(final URL url, final CrawlState state) {
		return url.getHost().equals(state.getURL().getHost());
	}

}
