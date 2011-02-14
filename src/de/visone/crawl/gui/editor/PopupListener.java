package de.visone.crawl.gui.editor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

public class PopupListener extends MouseAdapter {
	private final JPopupMenu popup;

	public PopupListener(final JPopupMenu popup) {
		this.popup = popup;
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		maybeShowPopup(e);
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		maybeShowPopup(e);
	}

	private void maybeShowPopup(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}
