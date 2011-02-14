package de.visone.crawl.texter;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import de.visone.crawl.Settings;
import de.visone.crawl.rules.HtmlQuery;
import de.visone.crawl.rules.RuleManager;

public class TexterFactory {

	private final HtmlQuery query;

	private final Map<String, HtmlQuery> dsQuery;

	private final boolean domainSpecific;

	private final boolean xmlText;

	private final boolean doLinks;

	private final boolean doText;

	public TexterFactory(final Settings sets) {
		domainSpecific = sets.domainSpecific;
		doLinks = sets.doLinks;
		doText = sets.doText;
		xmlText = sets.xmlText;
		if (domainSpecific) {
			dsQuery = new HashMap<String, HtmlQuery>();
		} else {
			dsQuery = null;
		}
		query = sets.query;
	}

	public boolean isDomainSpecific() {
		return domainSpecific;
	}

	public Map<String, HtmlQuery> getDSQueries() {
		return dsQuery;
	}

	public Texter getInstance(final URL url) {
		HtmlQuery q = query;
		if (domainSpecific) {
			final String rule = RuleManager.getRuleForURL(url);
			if (dsQuery.containsKey(rule)) {
				q = dsQuery.get(rule);
			}
		}
		return doText ? (xmlText ? new XmlTexterImpl(url, doLinks ? q : null, q)
				: new FullTexterImpl(url, doLinks ? q : null, q))
				: new NoTextTexterImpl(url, q);
	}
}
