/**
 * 
 */
package de.visone.crawl;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

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
		this(cl, true, null);
	}

	public CrawlerAction(final CrawlListener cl, final boolean link) {
		this(cl, link, null);
	}

	public CrawlerAction(final CrawlListener cl, final boolean link,
			final Window wnd) {
		this.cl = cl;
		this.link = link;
		this.wnd = wnd;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final CrawlerDialog cd = link ? new LinkCrawler(wnd) : new TextCrawler(
				wnd);
		cd.setCrawlListener(cl);
		cd.packAndShow();
	}
}
