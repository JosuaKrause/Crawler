package de.visone.crawl.gui.editor;

import java.awt.Color;
import java.awt.Component;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import de.visone.crawl.rules.BlacklistFilter;
import de.visone.crawl.rules.RuleManager;
import de.visone.crawl.sys.Utils;

public class BlacklistEditor extends Editor {

	public static void main(final String[] args) throws Exception {
		final URL url = Utils
				.getURL("http://de.wikipedia.org/wiki/Philip_K._Dick");
		final BlacklistFilter bf = new BlacklistFilter(null);
		RuleManager.addRule(RuleManager.getRuleForURL(url), bf, null);
		final JFrame wnd = new JFrame();
		wnd.add(new BlacklistEditor(bf.getRules(), null));
		wnd.pack();
		wnd.setLocationRelativeTo(null);
		wnd.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		wnd.setVisible(true);
	}

	private class BlacklistRenderer extends JLabel implements TableCellRenderer {

		private static final long serialVersionUID = -1286451818856189494L;

		public BlacklistRenderer() {
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(final JTable table,
				final Object r, final boolean isSelected,
				final boolean hasFocus, final int row, final int column) {
			Color newColor = table.getBackground();
			final String rule = r.toString();
			if (rule.equals(reject)) {
				newColor = CRED;
			}
			setBackground(newColor);
			setText(rule);
			if (isSelected) {
				setBackground(CBLUE);
			}
			return this;
		}
	}

	private static final long serialVersionUID = 7077088658945465285L;

	private final JTable table = new JTable(tableModel) {
		private static final long serialVersionUID = -2271301419516294235L;

		private final TableCellRenderer blr = new BlacklistRenderer();

		@Override
		public TableCellRenderer getCellRenderer(final int row, final int column) {
			return blr;
		}
	};

	private final JTextField text;

	private final Highlighter highlighter;

	private final Set<String> hosts;

	private BlacklistFilter filter;

	private String reject;

	private final List<RuleListener> listener;

	public BlacklistEditor(final List<String> r, final Set<String> hosts) {
		super(r);
		this.hosts = hosts;
		listener = new LinkedList<RuleListener>();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.addMouseListener(new PopupListener(
				addDeleteSelection(new JPopupMenu())));
		final TableModelListener tml = new TableModelListener() {

			@Override
			public void tableChanged(final TableModelEvent e) {
				recalcBlacklist();
				doHighlighting();
			}

		};
		tableModel.addTableModelListener(tml);
		highlighter = new DefaultHighlighter();
		text = new JTextField("", 35);
		text.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(final DocumentEvent e) {
				safeHighlighting();
			}

			@Override
			public void insertUpdate(final DocumentEvent e) {
				safeHighlighting();
			}

			@Override
			public void changedUpdate(final DocumentEvent e) {
				safeHighlighting();
			}
		});
		text.setHighlighter(highlighter);
		final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.add(new JScrollPane(table));
		final JPanel ver = new JPanel();
		ver.add(new JLabel("Test-URL:"));
		ver.add(text);
		split.add(ver);
		add(split);
		recalcBlacklist();
	}

	@Override
	protected JTable getTable() {
		return table;
	}

	private void setReject(final String reject) {
		if (reject == this.reject
				|| (reject != null && reject.equals(this.reject))) {
			return;
		}
		this.reject = reject;
		tableModel.fireTableDataChanged();
	}

	private volatile Thread highlightThread = null;

	private volatile boolean needsHighlight = false;

	private void safeHighlighting() {
		needsHighlight = true;
		if (highlightThread != null) {
			return;
		}
		highlightThread = new Thread() {
			@Override
			public void run() {
				while (!isInterrupted() && needsHighlight) {
					needsHighlight = false;
					doHighlighting();
				}
				highlightThread = null;
				if (needsHighlight) {
					safeHighlighting();
				}
			}
		};
		highlightThread.setDaemon(true);
		highlightThread.start();
	}

	private void doHighlighting() {
		highlighter.removeAllHighlights();
		final String txt = text.getText();
		final URL url = Utils.getURL(txt);
		if (url == null) {
			setReject(null);
			return;
		}
		setReject(filter.getRejectRule(url));
		if (reject == null) {
			try {
				highlighter.addHighlight(0, txt.length(), GREEN);
			} catch (final BadLocationException e) {
				e.printStackTrace();
			}
			return;
		}

		for (final String r : reject.split(" ")) {
			final int i = txt.indexOf(r);
			if (i < 0) {
				continue;
			}
			try {
				highlighter.addHighlight(i, i + r.length(), RED);
			} catch (final BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	public void addRuleListener(final RuleListener l) {
		listener.add(l);
		if (l != null) {
			l.changedBlacklist(filter);
		}
	}

	private void recalcBlacklist() {
		filter = new BlacklistFilter(hosts);
		RuleManager.addBlacklist(rules, filter);
		for (final RuleListener l : listener) {
			l.changedBlacklist(filter);
		}
	}

	public void addBlacklistRule(final String rule) {
		final int pos = rules.size();
		rules.add(rule);
		tableModel.fireTableRowsInserted(pos, pos);
		recalcBlacklist();
		doHighlighting();
	}
}
