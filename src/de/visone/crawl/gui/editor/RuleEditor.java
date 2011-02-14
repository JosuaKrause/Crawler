package de.visone.crawl.gui.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class RuleEditor extends Editor {

	private static final long serialVersionUID = 1708978381032572105L;

	protected static TreePath generatePath(TreeNode node) {
		final LinkedList<TreeNode> path = new LinkedList<TreeNode>();
		do {
			path.addFirst(node);
			node = node.getParent();
		} while (node != null);
		return new TreePath(path.toArray());
	}

	private static class ColorRenderer extends JLabel implements
			TableCellRenderer {

		private static final long serialVersionUID = -1664506547533656330L;

		public ColorRenderer() {
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(final JTable table,
				final Object r, final boolean isSelected,
				final boolean hasFocus, final int row, final int column) {
			Color newColor = table.getBackground();
			final String rule = r.toString();
			if (rule.startsWith("+")) {
				newColor = CGREEN;
			} else if (rule.startsWith("-")) {
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

	private final JTable table = new JTable(tableModel) {
		private static final long serialVersionUID = -2271301419516294235L;

		private final TableCellRenderer cr = new ColorRenderer();

		@Override
		public TableCellRenderer getCellRenderer(final int row, final int column) {
			return cr;
		}
	};

	protected final JTree tree;

	protected final Node root;

	protected final JSplitPane leftPane;

	protected final PopupListener treePopup;

	public RuleEditor(final Node root, final List<String> r) {
		super(r);
		// tree-view stuff
		this.root = root;
		tree = new JTree(root);
		tree.setEditable(false);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		final JPopupMenu treeMenu = new JPopupMenu();
		treeMenu.add(new AbstractAction("add as include rule") {
			private static final long serialVersionUID = -3177212640319049710L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				addRuleFromSelection(true);
			}
		});
		treeMenu.add(new AbstractAction("add as exclude rule") {
			private static final long serialVersionUID = -8448160768289986778L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				addRuleFromSelection(false);
			}
		});
		treePopup = new PopupListener(treeMenu);
		tree.addMouseListener(treePopup);
		// table stuff
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.addMouseListener(new PopupListener(
				addDeleteSelection(new JPopupMenu())));
		// arranging
		final JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		leftPane.add(new JScrollPane(tree));
		main.add(leftPane);
		main.add(new JScrollPane(table));
		add(main);
	}

	private void addRuleFromSelection(final boolean b) {
		final TreePath path = tree.getSelectionPath();
		if (path == null) {
			return;
		}
		tableModel.setValueAt((b ? "+ " : "- ")
				+ path.getLastPathComponent().toString(), -1, 0);
	}

	public void addRule(final String string) {
		tableModel.setValueAt(string, -1, 0);
	}

	@Override
	protected JTable getTable() {
		return table;
	}

}
