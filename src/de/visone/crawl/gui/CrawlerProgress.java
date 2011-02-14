package de.visone.crawl.gui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import de.visone.crawl.xml.ProgressListener;
import de.visone.crawl.xml.ProgressProducer;

public class CrawlerProgress extends JDialog implements ProgressListener {

	private static final long serialVersionUID = 7130992595324308210L;

	private static final int GRANULARITY = 10000;

	private final JProgressBar mainProgress;

	private final JProgressBar secondaryProgress;

	private ProgressProducer crawler;

	public CrawlerProgress(final Dialog parent, final ProgressProducer crawler) {
		super(parent, "Crawling...", true);
		this.crawler = crawler;
		mainProgress = new JProgressBar();
		secondaryProgress = new JProgressBar();
		mainProgress.setMaximum(GRANULARITY);
		secondaryProgress.setMaximum(GRANULARITY);
		progressAdvanced(0, 0);
		mainProgress.setIndeterminate(true);
		secondaryProgress.setIndeterminate(true);
		final JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.add(mainProgress);
		content.add(secondaryProgress);
		final JButton cancel = new JButton(new AbstractAction("Cancel") {
			private static final long serialVersionUID = -7231649340341062449L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				CrawlerProgress.this.dispose();
			}

		});
		content.add(cancel);
		add(content);
		pack();
		setLocationRelativeTo(parent);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		toFront();
		crawler.setProgressListener(this);
		mainProgress.setIndeterminate(false);
		secondaryProgress.setIndeterminate(false);
	}

	@Override
	public void progressAdvanced(final double main, final double secondary) {
		mainProgress.setValue((int) (main * GRANULARITY));
		secondaryProgress.setValue((int) (secondary * GRANULARITY));
	}

	@Override
	public void finished(final String url, final Exception e) {
		if (e != null) {
			CrawlerDialog.printErrorDialog(url, e, this);
		}
		progressAdvanced(1.0, 1.0);
		dispose();
	}

	@Override
	public boolean isActive() {
		return crawler != null;
	}

	@Override
	public void dispose() {
		if (crawler != null) {
			crawler.cancelAction();
		}
		crawler = null;
		super.dispose();
	}
}
