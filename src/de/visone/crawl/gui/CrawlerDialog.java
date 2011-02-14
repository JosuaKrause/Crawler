package de.visone.crawl.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.visone.crawl.Crawler;
import de.visone.crawl.Settings;
import de.visone.crawl.gui.editor.RuleEditorDialog;
import de.visone.crawl.out.CrawlListener;
import de.visone.crawl.rules.BlacklistFilter;
import de.visone.crawl.rules.RuleManager;
import de.visone.crawl.sys.Utils;
import de.visone.crawl.xml.ProgressAdapter;
import de.visone.crawl.xml.ProgressProducer;

public abstract class CrawlerDialog extends JDialog {

	public static Dimension DIM = new Dimension(10, 10);

	private static final long serialVersionUID = -5681299201858677468L;

	private final JTextField urlField;

	private final JTextField cookieField;

	private final JPanel midPane;

	private final JComboBox userAgent;

	private final JComboBox domainSpecific;

	private final JComboBox rules;

	private final JCheckBox haltOnError;

	private final JButton edit;

	private CrawlListener listener;

	private boolean wasPacked;

	private final boolean showProgress;

	public CrawlerDialog(final Window owner, final String title) {
		this(owner, title, true);
	}

	public CrawlerDialog(final Window owner, final String title,
			final boolean showProgress) {
		super(owner, title, ModalityType.DOCUMENT_MODAL);
		this.showProgress = showProgress;
		setLayout(new BorderLayout());
		urlField = new JTextField("http://", 50);
		urlField.setCaretPosition(urlField.getText().length());
		urlField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(final DocumentEvent e) {
				checkURL();
			}

			@Override
			public void insertUpdate(final DocumentEvent e) {
				checkURL();
			}

			@Override
			public void changedUpdate(final DocumentEvent e) {
				checkURL();
			}
		});
		cookieField = new JTextField("", 50);
		cookieField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(final DocumentEvent e) {
				setCookie();
			}

			@Override
			public void insertUpdate(final DocumentEvent e) {
				setCookie();
			}

			@Override
			public void changedUpdate(final DocumentEvent e) {
				setCookie();
			}
		});
		final JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.X_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		topPane.add(new JLabel("URL:"));
		topPane.add(Box.createRigidArea(DIM));
		topPane.add(urlField);
		add(topPane, BorderLayout.NORTH);
		edit = new JButton(new AbstractAction("Edit") {
			private static final long serialVersionUID = -8183869335496767892L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				showRuleEditor(false);
			}
		});
		midPane = new JPanel();
		midPane.setLayout(new BoxLayout(midPane, BoxLayout.Y_AXIS));
		midPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(midPane, BorderLayout.CENTER);
		final JButton ok = new JButton(new AbstractAction("Crawl") {
			private static final long serialVersionUID = -2387456434627208243L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (listener == null) {
					CrawlerDialog.this.dispose();
				}
				if (!checkURL()) {
					urlField.grabFocus();
					return;
				}
				final String url = urlField.getText();
				final Settings s = createSettings(url);
				final Crawler c = new Crawler(s, url, listener);
				delayedProgress(c);
				c.start();
				listener = null;
			}

		});
		final JButton cancel = new JButton(new AbstractAction("Cancel") {
			private static final long serialVersionUID = 82119190016049903L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				CrawlerDialog.this.dispose();
			}
		});
		final JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));
		bottomPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		bottomPane.add(Box.createHorizontalGlue());
		bottomPane.add(ok);
		bottomPane.add(Box.createRigidArea(DIM));
		bottomPane.add(cancel);
		add(bottomPane, BorderLayout.SOUTH);
		userAgent = new JComboBox(Settings.userAgents);
		userAgent.setEditable(true);
		addOption("User-Agent:", userAgent);
		rules = new JComboBox(RuleManager.getRules());
		rules.setEditable(false);
		rules.setEnabled(false);
		domainSpecific = new JComboBox(new String[] { "Standard",
				"Domain Specific", "Specified" });
		domainSpecific.setSelectedIndex(1);
		domainSpecific.setEditable(false);
		domainSpecific.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				rules.setEnabled(domainSpecific.getSelectedIndex() == 2);
				enableEdit();
			}

		});
		haltOnError = new JCheckBox();
		haltOnError.setSelected(false);
		pack();
		setLocationRelativeTo(owner);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		wasPacked = false;
	}

	private void setCookie() {
		final String cookie = cookieField.getText().trim();
		if (!cookie.isEmpty()) {
			// overwrite cookie-field
			Utils.addCustomHeader("Cookie:", cookie);
		}
	}

	private void enableEdit() {
		edit.setEnabled(domainSpecific.getSelectedIndex() != 0
				&& rules.getSelectedIndex() >= 0);
	}

	private boolean checkURL() {
		final URL url = Utils.getURL(urlField.getText());
		if (url == null) {
			urlField.setBackground(Color.RED);
		} else {
			urlField.setBackground(Color.WHITE);
			if (domainSpecific.getSelectedIndex() == 1) {
				final String rule = RuleManager.getRuleForURL(url);
				rules.setSelectedItem(rule);
				enableEdit();
			}
		}
		return url != null;
	}

	public void packAndShow() {
		if (!wasPacked) {
			addCustomizedOptions();
			final JButton create = new JButton(new AbstractAction("New") {
				private static final long serialVersionUID = 4326803581652282944L;

				@Override
				public void actionPerformed(final ActionEvent e) {
					showRuleEditor(true);
				}
			});
			rules.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(final ItemEvent e) {
					enableEdit();
				}
			});
			addOption("Crawl Rules:", domainSpecific, Box.createRigidArea(DIM),
					rules, Box.createRigidArea(DIM), edit, Box
							.createRigidArea(DIM), create);
			addOption("Halt on every error:", haltOnError);
			addOption("Cookies:", cookieField);
			pack();
			wasPacked = true;
		}
		toFront();
		setVisible(true);
	}

	private void showRuleEditor(final boolean create) {
		String t = urlField.getText();
		if (t.equals("http://")) {
			t = "";
		}
		final URL url = Utils.getURL(t);
		final String rule = create ? null : rules.getSelectedItem().toString();
		final String r = RuleEditorDialog.showRuleEditor(this, rule, url,
				!wantsIntro());
		if (r != null) {
			rules.setModel(new DefaultComboBoxModel(RuleManager.getRules()));
			rules.setSelectedItem(r);
		}
	}

	protected abstract boolean wantsIntro();

	public void setCrawlListener(final CrawlListener listener) {
		this.listener = listener;
	}

	protected Settings createSettings(final String url) {
		final Settings s = new Settings();
		s.userAgent = userAgent.getModel().getSelectedItem().toString();
		s.domainSpecific = domainSpecific.getSelectedIndex() == 1;
		if (domainSpecific.getSelectedIndex() == 2) {
			final BlacklistFilter blf = new BlacklistFilter(null);
			RuleManager.addRule(rules.getSelectedItem().toString(), blf,
					s.query);
			s.customAccepter.add(blf);
		}
		s.haltOnError = haltOnError.isSelected();
		setCookie();
		return s;
	}

	protected abstract void addCustomizedOptions();

	public void delayedProgress(final Crawler c) {
		final CrawlerDialog cd = this;
		if (!showProgress) {
			final ProgressProducer pp = c.getProgressProducer();
			pp.setProgressListener(new ProgressAdapter() {

				@Override
				public void finished(final String url, final Exception err) {
					cd.dispose();
				}

			});
			return;
		}
		final Thread t = new Thread("Delayed CrawlProgress-starter") {
			@Override
			public void run() {
				try {
					synchronized (this) {
						wait(1000);
					}
					final ProgressProducer pp = c.getProgressProducer();
					if (pp.isCrawling()) {
						final CrawlerProgress cp = new CrawlerProgress(cd, pp);
						if (cp.isActive()) {
							cp.setVisible(true);
						}
					}
				} catch (final InterruptedException e) {
					interrupt();
				}
				cd.dispose();
			}
		};
		t.setDaemon(true);
		t.start();
	}

	protected void addOption(final String label, final Component... comps) {
		final JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
		pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		pane.add(new JLabel(label));
		pane.add(Box.createRigidArea(DIM));
		for (final Component comp : comps) {
			pane.add(comp);
		}
		pane.add(Box.createHorizontalGlue());
		midPane.add(pane);
	}

	public static void printErrorDialog(final String url, final Exception e,
			final Component parent) {
		System.err.println("Exception on page: " + url);
		e.printStackTrace();
		final String msg = "<html>An Error halted the crawling process:<br />"
				+ e.getMessage() + "<br />While scanning page: " + url
				+ "<br />Details can be found in the error log.</html>";
		JOptionPane.showMessageDialog(parent, msg, "Error while crawling",
				JOptionPane.ERROR_MESSAGE);
	}

}
