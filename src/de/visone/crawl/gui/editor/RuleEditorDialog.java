package de.visone.crawl.gui.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.visone.crawl.Settings;
import de.visone.crawl.gui.CrawlerDialog;
import de.visone.crawl.rules.BlacklistFilter;
import de.visone.crawl.rules.HtmlQuery;
import de.visone.crawl.rules.RuleManager;
import de.visone.crawl.sys.AbstractUrlPool;
import de.visone.crawl.sys.CrawlState;
import de.visone.crawl.sys.UrlPool;
import de.visone.crawl.sys.Utils;
import de.visone.crawl.texter.TreeTexterImpl;

public class RuleEditorDialog extends JDialog implements QueryManager {

	public static void main(final String[] args) {
		showRuleEditor(null, "wikipedia.org.rule", Utils
				.getURL("http://wikipedia.org"), true);
	}

	private static final String TXT_EXAMPLE = "Example-URL:";

	private static class URLRulePanel extends JPanel {

		private static final long serialVersionUID = -4009099799776631260L;

		private final JTextField url;

		private final JLabel urlLabel;

		private final JTextField rule;

		private final JLabel hint;

		private boolean doCancel;

		public URLRulePanel(final String srule, final String example,
				final boolean urlMayChange) {
			doCancel = true;
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			final DocumentListener dl = new DocumentListener() {

				@Override
				public void removeUpdate(final DocumentEvent e) {
					setHint();
				}

				@Override
				public void insertUpdate(final DocumentEvent e) {
					setHint();
				}

				@Override
				public void changedUpdate(final DocumentEvent e) {
					setHint();
				}
			};
			if (urlMayChange) {
				url = new JTextField(example, 30);
				addOption(TXT_EXAMPLE, url);
				url.getDocument().addDocumentListener(dl);
				urlLabel = null;
			} else {
				urlLabel = new JLabel(TXT_EXAMPLE + " " + example);
				add(urlLabel);
				url = null;
			}
			rule = new JTextField(srule, 20);
			addOption("Rule:", rule);
			hint = new JLabel(" ");
			add(hint);
			rule.getDocument().addDocumentListener(dl);
		}

		public boolean isValidRule() {
			return rule.getText().endsWith(".rule");
		}

		public boolean isValidURL() {
			return url == null || getUrl() != null;
		}

		public void setHint() {
			if (!isValidRule()) {
				hint.setText("Rule must end with .rule");
				return;
			}
			if (!isValidURL()) {
				hint.setText("URL is not valid");
				return;
			}
			hint.setText(" ");
		}

		public String getRule() {
			return rule.getText();
		}

		public URL getUrl() {
			return url != null ? Utils.getURL(url.getText()) : null;
		}

		public void doOk() {
			doCancel = false;
		}

		public boolean shouldCancel() {
			return doCancel;
		}

		private void addOption(final String label, final Component... comps) {
			final JPanel pane = new JPanel();
			pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
			pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			pane.add(new JLabel(label));
			pane.add(Box.createRigidArea(CrawlerDialog.DIM));
			for (final Component comp : comps) {
				pane.add(comp);
			}
			pane.add(Box.createHorizontalGlue());
			add(pane);
		}
	}

	private static void addOkCancel(final JPanel pane, final Action ok,
			final Action cancel) {
		final JButton bok = new JButton(ok);
		final JButton bcancel = new JButton(cancel);
		final JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));
		bottomPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		bottomPane.add(Box.createHorizontalGlue());
		bottomPane.add(bok);
		bottomPane.add(Box.createRigidArea(CrawlerDialog.DIM));
		bottomPane.add(bcancel);
		pane.add(bottomPane);
	}

	public static String showRuleEditor(final JDialog parent, String rule,
			URL example, final boolean skipIntro) {
		if (!skipIntro || rule == null || example == null) {
			if (rule == null && example != null) {
				rule = RuleManager.getFullRuleForURL(example);
			}
			final JDialog d = new JDialog(parent, "Determine example URL", true);
			final URLRulePanel urp = new URLRulePanel(rule,
					example != null ? example.toString() : "http://", true);
			final Action ok = new AbstractAction("Ok") {

				private static final long serialVersionUID = 2085161425644687775L;

				@Override
				public void actionPerformed(final ActionEvent e) {
					if (!urp.isValidRule() || !urp.isValidURL()) {
						urp.setHint();
						return;
					}
					urp.doOk();
					d.dispose();
				}

			};
			final Action cancel = new AbstractAction("Cancel") {

				private static final long serialVersionUID = -2962626346775304277L;

				@Override
				public void actionPerformed(final ActionEvent e) {
					d.dispose();
				}
			};
			addOkCancel(urp, ok, cancel);
			d.add(urp);
			d.pack();
			d.setLocationRelativeTo(parent);
			d.toFront();
			d.setVisible(true);
			if (urp.shouldCancel()) {
				return null;
			}
			rule = urp.getRule();
			example = urp.getUrl();
		}
		File startRule = RuleManager.getFileFromRule(rule);
		startRule = startRule.exists() ? startRule : null;
		RuleEditorDialog red;
		try {
			red = new RuleEditorDialog(parent, rule, startRule, example);
			red.setLocationRelativeTo(parent);
			red.toFront();
			red.setVisible(true);
			return red.getResult();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static final long serialVersionUID = -6640590225128359797L;

	private final File startRule;

	private final URLRulePanel rulePane;

	private TextRuleEditor textRules;

	private BlacklistEditor blRules;

	private String result;

	private RuleEditorDialog(final JDialog parent, final String rule,
			final File startRule, final URL exampleUrl) throws Exception {
		super(parent, startRule == null ? "New Rule" : "Edit Rule", true);
		this.startRule = startRule;
		result = null;
		rulePane = new URLRulePanel(rule, exampleUrl.toString(), false);
		ini(rule, exampleUrl);
		final Action ok = new AbstractAction("Ok") {
			private static final long serialVersionUID = -2300894939574085490L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (saveRules()) {
					result = rulePane.getRule();
					dispose();
				}
			}
		};
		final Action cancel = new AbstractAction("Cancel") {
			private static final long serialVersionUID = 3826443728155869671L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				dispose();
			}
		};
		addOkCancel(rulePane, ok, cancel);
		add(rulePane);
		pack();
	}

	public String getResult() {
		return result;
	}

	private static String CHGR_EXT = "<html>The Rulename changed!<br>Do you want to keep the old rule?";

	private static String CHGR = "Rulename changed";

	private boolean saveRules() {
		if (!rulePane.isValidRule()) {
			rulePane.setHint();
			return false;
		}
		final File saveTo = RuleManager.getFileFromRule(rulePane.getRule());
		try {
			RuleManager.writeRules(saveTo, blRules.getRules(), textRules
					.getRules());
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}
		if (startRule != null && !saveTo.equals(startRule)) {
			final int choice = JOptionPane.showOptionDialog(this, CHGR_EXT,
					CHGR, JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, new String[] { "Keep",
							"Remove" }, 0);
			if (choice == 1) {
				startRule.delete();
			}
		}
		return true;
	}

	private void ini(final String rule, final URL example) throws Exception {
		final HtmlQuery q = new HtmlQuery(rule);
		final BlacklistFilter bf = new BlacklistFilter(null);
		if (startRule != null) {
			RuleManager.addRule(rule, bf, q);
		} else {
			HtmlQuery.addStd(q);
		}
		final AbstractUrlPool pool = new UrlPool();
		final TreeTexterImpl tti = new TreeTexterImpl(example);
		final CrawlState state = new CrawlState(example, 0, tti);
		Utils.crawl(state, pool, Settings.userAgents[0]);
		textRules = new TextRuleEditor(tti.getRoot(), q.getRules());
		blRules = new BlacklistEditor(bf.getRules(), bf.getHosts());
		final LinkViewer linkView = new LinkViewer(tti.getRoot(), this);
		textRules.addRuleListener(linkView);
		blRules.addRuleListener(linkView);
		final ImageViewer imageView = new ImageViewer(tti.getRoot(), this);
		textRules.addRuleListener(imageView);
		blRules.addRuleListener(imageView);
		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Text / Links", textRules);
		tabbedPane.addTab("Blacklist", blRules);
		tabbedPane.addTab("Links", new JScrollPane(linkView));
		tabbedPane.addTab("Images", new JScrollPane(imageView));
		rulePane.add(tabbedPane);
	}

	@Override
	public void addBlacklistEntry(final String entry) {
		blRules.addBlacklistRule(entry);
	}

	@Override
	public void addRule(final String rule, final boolean inc) {
		textRules.addRule((inc ? "+ " : "- ") + rule);
	}

}
