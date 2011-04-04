/**
 * 
 */
package de.visone.crawl;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import de.visone.crawl.gui.CrawlerDialog;
import de.visone.crawl.gui.LinkCrawler;
import de.visone.crawl.gui.TextCrawler;
import de.visone.crawl.out.CrawlListener;

/**
 * @author Joschi
 * 
 */
public class CrawlerAction extends AbstractAction {

	private static final long serialVersionUID = 4852089049442715177L;

	private final Window wnd;

	private final CrawlListener cl;

	private final boolean link;

	public CrawlerAction(final CrawlListener cl) {
		this(cl, true);
	}

	public CrawlerAction(final CrawlListener cl, final boolean link) {
		this(cl, link, (Window) null);
	}

	public CrawlerAction(final CrawlListener cl, final boolean link,
			final Window wnd) {
		this.cl = cl;
		this.link = link;
		this.wnd = wnd;
	}

	public CrawlerAction(final CrawlListener cl, final String name) {
		this(cl);
		setName(name);
	}

	public CrawlerAction(final CrawlListener cl, final boolean link,
			final String name) {
		this(cl, link);
		setName(name);
	}

	public CrawlerAction(final CrawlListener cl, final boolean link,
			final Window wnd, final String name) {
		this(cl, link, wnd);
		setName(name);
	}

	public CrawlerAction(final CrawlListener cl, final String name,
			final Icon icon) {
		this(cl, name);
		setIcon(icon);
	}

	public CrawlerAction(final CrawlListener cl, final boolean link,
			final String name, final Icon icon) {
		this(cl, link, name);
		setIcon(icon);
	}

	public CrawlerAction(final CrawlListener cl, final boolean link,
			final Window wnd, final String name, final Icon icon) {
		this(cl, link, wnd, name);
		setIcon(icon);
	}

	public void setName(final String name) {
		putValue(Action.NAME, name);
	}

	public void setIcon(final Icon icon) {
		putValue(Action.SMALL_ICON, icon);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final CrawlerDialog cd = link ? new LinkCrawler(wnd) : new TextCrawler(
				wnd);
		cd.setCrawlListener(cl);
		cd.packAndShow();
	}
}
