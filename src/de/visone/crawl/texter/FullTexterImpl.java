package de.visone.crawl.texter;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.xml.sax.Attributes;

import de.visone.crawl.rules.HtmlQuery;

public class FullTexterImpl extends NoTextTexterImpl {

	private final StringBuilder sb;

	private final HtmlQuery text;

	private final Stack<Integer> tags;

	private int includeStrings;

	private int excludeStrings;

	public FullTexterImpl(final URL url, final HtmlQuery links,
			final HtmlQuery text) {
		super(url, links);
		this.text = text;
		tags = new Stack<Integer>();
		sb = new StringBuilder();
		includeStrings = excludeStrings = 0;
	}

	@Override
	public boolean acceptString() {
		return includeStrings > 0 && excludeStrings <= 0;
	}

	@Override
	protected boolean acceptLink() {
		return acceptString();
	}

	@Override
	public void startTag(final String tag, final Attributes a) {
		if (acceptString()) {
			if (tag.equals("img")) { // img alt hack
				final String alt = a.getValue("alt");
				if (alt != null) {
					string(alt);
				}
			}
			if (isSectionTag(tag)) { // section newline
				sb.append('\n');
			}
		}
		super.startTag(tag, a);
		final int l = text.check(tag, a);
		if (l > 0) {
			++includeStrings;
		} else if (l < 0) {
			++excludeStrings;
		}
		tags.push(l);
	}

	private static final Set<String> SECTION_TAGS = new HashSet<String>();

	static {
		SECTION_TAGS.add("p");
		SECTION_TAGS.add("br");
		SECTION_TAGS.add("h1");
		SECTION_TAGS.add("h2");
		SECTION_TAGS.add("h3");
		SECTION_TAGS.add("h4");
		SECTION_TAGS.add("h5");
		SECTION_TAGS.add("h6");
		SECTION_TAGS.add("div");
		SECTION_TAGS.add("ul");
		SECTION_TAGS.add("li");
		SECTION_TAGS.add("table");
	}

	private boolean needSpace = false;

	protected boolean isSectionTag(final String tag) {
		final boolean res = SECTION_TAGS.contains(tag);
		needSpace = needSpace && !res;
		return res;
	}

	@Override
	public void endTag(final String tag) {
		if (acceptString() && isSectionTag(tag)) {
			sb.append('\n');
		}
		super.endTag(tag);
		final int l = tags.pop();
		if (l > 0) {
			--includeStrings;
		} else if (l < 0) {
			--excludeStrings;
		}
	}

	@Override
	public void string(String str) {
		str = str.trim();
		if (str.isEmpty()) {
			return;
		}
		if (needSpace) {
			sb.append(' ');
		} else {
			needSpace = true;
		}
		sb.append(str.replaceAll("\\s+", " "));
	}

	@Override
	public String getText() {
		return sb.toString().trim();
	}

	@Override
	public void dispose() {
		super.dispose();
		sb.setLength(0);
	}
}
