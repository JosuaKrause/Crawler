package de.visone.crawl.gui.editor;

import static de.visone.crawl.gui.editor.LinkViewer.mix;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

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
import de.visone.crawl.rules.ImageRepresentation;
import de.visone.crawl.rules.LinkRepresentation;
import de.visone.crawl.sys.Img;

public class ImageViewer extends JTable implements RuleListener {

	private static final long serialVersionUID = 6990041164269398001L;

	private class ImageRenderer extends JLabel implements TableCellRenderer {

		private static final long serialVersionUID = 9010510708106208053L;

		public ImageRenderer() {
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
			setText(lrp.getImage(row).getSource().toString());
			if (isSelected) {
				setBackground(Editor.CBLUE);
			}
			return this;
		}

	}

	private class ImageModel extends AbstractTableModel {

		private static final long serialVersionUID = 936144925600723908L;

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
			return lrp.getImage(rowIndex);
		}

		@Override
		public String getColumnName(final int column) {
			return "";
		}

	}

	private final ImageRepresentation lrp;

	private final TableCellRenderer cr = new ImageRenderer();

	private final Node root;

	private final ImageModel model;

	private BlacklistFilter blacklist;

	private HtmlQuery query;

	public ImageViewer(final Node root, final QueryManager mng) {
		this.root = root;
		setToolTipText("");
		lrp = new ImageRepresentation(root);
		setModel(model = new ImageModel());
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		blacklist = null;
		query = null;
		final Action addBl = new AbstractAction("add ulr(s) to blacklist") {
			private static final long serialVersionUID = 1718604439910705493L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				final int[] sel = ImageViewer.this.getSelectedRows();
				for (final int s : sel) {
					mng.addBlacklistEntry(lrp.getImage(s).getSource()
							.toString());
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

	@Override
	public String getToolTipText(final MouseEvent event) {
		final int row = rowAtPoint(event.getPoint());
		if (lrp.size() <= row || row < 0) {
			return getToolTipText();
		}
		final Img img = lrp.getImage(row);
		return "<html><img src=\"" + img.getSource().toString() + "\">";
	}

}
