package de.visone.crawl.gui.editor;

import java.net.URL;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.visone.crawl.Settings;
import de.visone.crawl.rules.HtmlQuery;
import de.visone.crawl.rules.RuleManager;
import de.visone.crawl.rules.TextRepresentation;
import de.visone.crawl.sys.CrawlState;
import de.visone.crawl.sys.UrlPool;
import de.visone.crawl.sys.Utils;
import de.visone.crawl.texter.TreeTexterImpl;

public class TextRuleEditor extends RuleEditor {

	public static void main(final String[] args) throws Exception {
		final URL url = Utils
				.getURL("http://de.wikipedia.org/wiki/Philip_K._Dick");
		final HtmlQuery q = new HtmlQuery("foobar");
		RuleManager.addRule(RuleManager.getRuleForURL(url), null, q);
		final UrlPool pool = new UrlPool();
		final TreeTexterImpl tti = new TreeTexterImpl(url);
		final CrawlState state = new CrawlState(url, 0, tti);
		Utils.crawl(state, pool, Settings.userAgents[0]);
		final JFrame wnd = new JFrame();
		wnd.add(new TextRuleEditor(tti.getRoot(), q.getRules()));
		wnd.pack();
		wnd.setLocationRelativeTo(null);
		wnd.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		wnd.setVisible(true);
	}

	private static final long serialVersionUID = 8948559203368893517L;

	private final JTextArea text;

	private final Highlighter highlighter;

	private final TextRepresentation curRep;

	private int autoCaret;

	private RuleListener listener;

	public TextRuleEditor(final Node root, final List<String> r) {
		super(root, r);
		autoCaret = 0;
		curRep = new TextRepresentation(root);
		highlighter = new DefaultHighlighter();
		text = new JTextArea(20, 80);
		text.setHighlighter(highlighter);
		text.setEditable(false);
		text.setLineWrap(true);
		text.setText(curRep.toString());
		text.addMouseListener(treePopup);
		refreshText();
		final CaretListener cl = new CaretListener() {

			@Override
			public void caretUpdate(final CaretEvent e) {
				updateTreeview();
			}

		};
		text.addCaretListener(cl);
		leftPane.add(new JScrollPane(text));
		final TreeSelectionListener tsl = new TreeSelectionListener() {

			@Override
			public void valueChanged(final TreeSelectionEvent e) {
				final TreeNode node = (TreeNode) tree
						.getLastSelectedPathComponent();
				if (node == null) {
					return;
				}
				highlight(node);
			}
		};
		tree.addTreeSelectionListener(tsl);
		final TableModelListener tml = new TableModelListener() {

			@Override
			public void tableChanged(final TableModelEvent e) {
				refreshText();
			}

		};
		tableModel.addTableModelListener(tml);
	}

	public void setRuleListener(final RuleListener l) {
		listener = l;
		if (l != null) {
			final HtmlQuery q = new HtmlQuery("anon");
			RuleManager.addQuery(rules, q);
			l.changedRules(q);
		}
	}

	private void refreshText() {
		final HtmlQuery q = new HtmlQuery("anon");
		RuleManager.addQuery(rules, q);
		curRep.removeHighlights(highlighter);
		curRep.highlight(highlighter, null);
		curRep.setHighlight(root, q, highlighter);
		if (listener != null) {
			listener.changedRules(q);
		}
	}

	private void highlight(final TreeNode n) {
		curRep.highlight(highlighter, n);
		final int i = curRep.getIndex(n);
		if (i >= 0) {
			autoCaret = i;
			text.setCaretPosition(i);
		}
	}

	private void updateTreeview() {
		final int pos = text.getCaretPosition();
		if (pos == autoCaret) {
			return;
		}
		autoCaret = pos;
		final TreeNode node = curRep.findNodeFor(pos);
		if (node != null) {
			final TreePath path = generatePath(node);
			tree.setSelectionPath(path);
			tree.scrollPathToVisible(path);
		}
	}

}
