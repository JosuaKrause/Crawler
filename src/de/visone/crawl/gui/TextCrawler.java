package de.visone.crawl.gui;

import java.awt.Window;

import javax.swing.JCheckBox;

import de.visone.crawl.Settings;

public class TextCrawler extends CrawlerDialog {

	private static final long serialVersionUID = -3624026403379579675L;

	private final JCheckBox getLinks = new JCheckBox();

	private final JCheckBox getXml = new JCheckBox();

	public TextCrawler(final Window owner) {
		super(owner, "Crawl text...");
	}

	@Override
	protected Settings createSettings(final String url) {
		final Settings s = super.createSettings(url);
		s.maxDepth = 0;
		s.doLinks = getLinks.isSelected();
		s.xmlText = getXml.isSelected();
		return s;
	}

	@Override
	protected void addCustomizedOptions() {
		getXml.setSelected(false);
		addOption("Get XML-Content:", getXml);
		getLinks.setSelected(false);
		addOption("Find links:", getLinks);
	}

	@Override
	protected boolean wantsIntro() {
		return false;
	}

}
