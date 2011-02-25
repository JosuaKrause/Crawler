package de.visone.crawl.gui.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import de.visone.crawl.rules.BlacklistFilter;
import de.visone.crawl.rules.HtmlQuery;
import de.visone.crawl.rules.LinkRepresentation;

public class LinkViewer extends JTable implements RuleListener {

	private static final long serialVersionUID = -8757276915918283269L;

	public static Color mix(final Color a, final Color b) {
		final float aa = a.getAlpha() / 255f;
		final float ar = a.getRed() * aa;
		final float ag = a.getGreen() * aa;
		final float ab = a.getBlue() * aa;
		final float ba = b.getAlpha() / 255f;
		final float br = ba * b.getRed() + (1f - ba) * ar;
		final float bg = ba * b.getGreen() + (1f - ba) * ag;
		final float bb = ba * b.getBlue() + (1f - ba) * ab;
		return new Color((int) br, (int) bg, (int) bb);
	}

	private class LinkRenderer extends JLabel implements TableCellRenderer {

		private static final long serialVersionUID = 6302736391576264421L;

		public LinkRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(final JTable table,
				final Object r, final boolean isSelected,
				final boolean hasFocus, final int row, final int column) {
			Color newColor = table.getBackground();
			setForeground(Color.BLACK);
			final int state = lrp.getState(row);
			if ((state & LinkRepresentation.BLACKLISTED) != 0) {
				newColor = mix(newColor, Editor.CBLACK);
				setForeground(Color.WHITE);
			}
			if ((state & LinkRepresentation.INCLUDED) != 0) {
				newColor = mix(newColor, Editor.CGREEN);
			}
			if ((state & LinkRepresentation.EXCLUDED) != 0) {
				newColor = mix(newColor, Editor.CRED);
			}
			setBackground(newColor);
			setText(lrp.getLink(row).getUrl().toString());
			if (isSelected) {
				setBackground(Editor.CBLUE);
			}
			return this;
		}
	}

	private class LinkModel extends AbstractTableModel {

		private static final long serialVersionUID = -8719337051839906766L;

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public int getRowCount() {
			return lrp.size();
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			return lrp.getLink(rowIndex);
		}

		@Override
		public String getColumnName(final int column) {
			return "";
		}

	}

	private final LinkRepresentation lrp;

	private final TableCellRenderer cr = new LinkRenderer();

	private final Node root;

	private final LinkModel model;

	private BlacklistFilter blacklist;

	private HtmlQuery query;

	public LinkViewer(final Node root, final QueryManager mng) {
		this.root = root;
		lrp = new LinkRepresentation(root);
		setModel(model = new LinkModel());
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		blacklist = null;
		query = null;
		final Action addBl = new AbstractAction("add ulr(s) to blacklist") {
			private static final long serialVersionUID = 1718604439910705493L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				final int[] sel = LinkViewer.this.getSelectedRows();
				for (final int s : sel) {
					mng.addBlacklistEntry(lrp.getLink(s).getUrl().toString());
				}
			}
		};
		final JPopupMenu menu = new JPopupMenu();
		menu.add(addBl);
		addMouseListener(new PopupListener(menu));
	}

	@Override
	public TableCellRenderer getCellRenderer(final int row, final int column) {
		return cr;
	}

	@Override
	public void changedBlacklist(final BlacklistFilter bl) {
		blacklist = bl;
		changedRule();
	}

	private void changedRule() {
		if (blacklist != null && query != null) {
			lrp.refreshStates(root, query, blacklist);
			model.fireTableDataChanged();
		}
	}

	@Override
	public void changedRules(final HtmlQuery q) {
		query = q;
		changedRule();
	}
}
