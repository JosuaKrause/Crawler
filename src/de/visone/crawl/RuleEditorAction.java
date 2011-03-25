/**
 * 
 */
package de.visone.crawl;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import de.visone.crawl.gui.editor.RuleEditorDialog;
import de.visone.crawl.sys.Utils;

/**
 * @author Joschi
 * 
 */
public class RuleEditorAction extends AbstractAction {

	private static final long serialVersionUID = -8099711383825201866L;

	private final URL url;

	private final String rule;

	private final JDialog par;

	private final boolean skip;

	public RuleEditorAction() {
		this((URL) null, null);
	}

	public RuleEditorAction(final URL url, final String rule) {
		this(url, rule, false);
	}

	public RuleEditorAction(final URL url, final String rule, final boolean skip) {
		this(url, rule, skip, null);
	}

	public RuleEditorAction(final URL url, final String rule,
			final boolean skip, final JDialog par) {
		this.url = url;
		this.rule = rule;
		this.skip = skip;
		this.par = par;
	}

	public RuleEditorAction(final String url, final String rule) {
		this(url, rule, false);
	}

	public RuleEditorAction(final String url, final String rule,
			final boolean skip) {
		this(url, rule, skip, null);
	}

	public RuleEditorAction(final String url, final String rule,
			final boolean skip, final JDialog par) {
		this.url = url != null ? Utils.getURL(url) : null;
		this.rule = rule;
		this.skip = skip;
		this.par = par;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		RuleEditorDialog.showRuleEditor(par, rule, url, skip);
	}

}
