package de.visone.crawl.gui;

import java.awt.Window;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.visone.crawl.Settings;

public class LinkCrawler extends CrawlerDialog {

	private static final long serialVersionUID = -7858840947025711432L;

	private final JCheckBox readNoFollow = new JCheckBox();

	private final JCheckBox onlySameHost = new JCheckBox();

	private final JSpinner maxDepth = new JSpinner(new SpinnerNumberModel(3, 0,
			8, 1));

	private final JCheckBox textAsWell = new JCheckBox();

	private final JSpinner meanDelay = new JSpinner(new SpinnerNumberModel(
			1000, 0, 10000, 100));

	private final JSpinner coolDown = new JSpinner(new SpinnerNumberModel(5000,
			0, 20000, 100));

	private final JSpinner limit = new JSpinner(new SpinnerNumberModel(0, 0,
			100000, 100));

	public LinkCrawler(final Window owner) {
		this(owner, "Crawl links...", true, "http://");
	}

	public LinkCrawler(final Window owner, final String title,
			final boolean showProgress, final String iniURL) {
		super(owner, title, showProgress, iniURL);
	}

	@Override
	protected Settings createSettings(final String url) {
		final Settings s = super.createSettings(url);
		s.meanDelay = (Integer) meanDelay.getModel().getValue();
		s.coolDown = (Integer) coolDown.getModel().getValue();
		s.doText = textAsWell.isSelected();
		s.onlySameHost = onlySameHost.isSelected();
		s.readNoFollow = readNoFollow.isSelected();
		s.maxDepth = (Integer) maxDepth.getModel().getValue();
		s.killLimit = (Integer) limit.getModel().getValue();
		return s;
	}

	@Override
	protected void addCustomizedOptions() {
		addOption("Mean delay:", meanDelay, new JLabel(" ms"));
		addOption("Cool down:", coolDown, new JLabel(" ms"));
		addOption("Max depth:", maxDepth);
		addOption("Page limit:", limit);
		readNoFollow.setSelected(true);
		addOption("Evaluate nofollow attributes:", readNoFollow);
		onlySameHost.setSelected(true);
		addOption("Only same host:", onlySameHost);
		textAsWell.setSelected(false);
		addOption("Crawl text as well:", textAsWell);
	}

	@Override
	protected boolean wantsIntro() {
		return true;
	}

}
