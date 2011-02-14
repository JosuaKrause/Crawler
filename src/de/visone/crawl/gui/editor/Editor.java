package de.visone.crawl.gui.editor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter.HighlightPainter;

public abstract class Editor extends JPanel {

	private static final long serialVersionUID = -296516188728866139L;

	public static final Color CRED = new Color(0xE9A3C9);

	public static final Color CGREEN = new Color(0xA1D76A);

	public static final Color CBLUE = new Color(0x707570B3, true);

	public static final Color CBLACK = new Color(0x00000000);

	public static HighlightPainter RED = new DefaultHighlighter.DefaultHighlightPainter(
			CRED);

	public static HighlightPainter GREEN = new DefaultHighlighter.DefaultHighlightPainter(
			CGREEN);

	public static HighlightPainter BLUE = new DefaultHighlighter.DefaultHighlightPainter(
			CBLUE);

	protected final AbstractTableModel tableModel = new AbstractTableModel() {

		private static final long serialVersionUID = -8910532694875882298L;

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			if (rowIndex >= rules.size()) {
				return "";
			}
			return rules.get(rowIndex);
		}

		@Override
		public void setValueAt(final Object aValue, final int rowIndex,
				final int columnIndex) {
			final String str = aValue.toString();
			if (rowIndex < 0 || rowIndex >= rules.size()) {
				if (str.isEmpty()) {
					return;
				}
				final int r = rules.size();
				rules.add(str);
				fireTableRowsInserted(r, r);
				return;
			}
			if (str.isEmpty()) {
				rules.remove(rowIndex);
				fireTableRowsDeleted(rowIndex, rowIndex);
				return;
			}
			rules.set(rowIndex, str);
			fireTableRowsUpdated(rowIndex, rowIndex);
		}

		@Override
		public String getColumnName(final int column) {
			return "";
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return true;
		}

		@Override
		public int getRowCount() {
			return rules.size() + 1;
		}

		@Override
		public int getColumnCount() {
			return 1;
		}
	};

	protected final List<String> rules;

	public Editor(final List<String> r) {
		rules = new ArrayList<String>(r);
	}

	protected abstract JTable getTable();

	protected void deleteSelection() {
		final int r[] = getTable().getSelectedRows();
		int i = r.length;
		while (i-- > 0) {
			tableModel.setValueAt("", r[i], 0);
		}
	}

	protected JPopupMenu addDeleteSelection(final JPopupMenu popup) {
		popup.add(new AbstractAction("delete rule(s)") {
			private static final long serialVersionUID = -1973549327344965278L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				deleteSelection();
			}
		});
		return popup;
	}

	public List<String> getRules() {
		return rules;
	}

}
