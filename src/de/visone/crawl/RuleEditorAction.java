/**
 * 
 */
package de.visone.crawl;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
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
		this(url, rule, skip, (JDialog) null);
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
		this(url, rule, skip, (JDialog) null);
	}

	public RuleEditorAction(final String url, final String rule,
			final boolean skip, final JDialog par) {
		this.url = url != null ? Utils.getURL(url) : null;
		this.rule = rule;
		this.skip = skip;
		this.par = par;
	}

	public RuleEditorAction(final String name) {
		this();
		setName(name);
	}

	public RuleEditorAction(final URL url, final String rule, final String name) {
		this(url, rule);
		setName(name);
	}

	public RuleEditorAction(final URL url, final String rule,
			final boolean skip, final String name) {
		this(url, rule, skip);
		setName(name);
	}

	public RuleEditorAction(final URL url, final String rule,
			final boolean skip, final JDialog par, final String name) {
		this(url, rule, skip, par);
		setName(name);
	}

	public RuleEditorAction(final String url, final String rule,
			final String name) {
		this(url, rule);
		setName(name);
	}

	public RuleEditorAction(final String url, final String rule,
			final boolean skip, final String name) {
		this(url, rule, skip);
		setName(name);
	}

	public RuleEditorAction(final String url, final String rule,
			final boolean skip, final JDialog par, final String name) {
		this(url, rule, skip, par);
		setName(name);
	}

	public RuleEditorAction(final String name, final Icon icon) {
		this(name);
		setIcon(icon);
	}

	public RuleEditorAction(final URL url, final String rule,
			final String name, final Icon icon) {
		this(url, rule, name);
		setIcon(icon);
	}

	public RuleEditorAction(final URL url, final String rule,
			final boolean skip, final String name, final Icon icon) {
		this(url, rule, skip, name);
		setIcon(icon);
	}

	public RuleEditorAction(final URL url, final String rule,
			final boolean skip, final JDialog par, final String name,
			final Icon icon) {
		this(url, rule, skip, par, name);
		setIcon(icon);
	}

	public RuleEditorAction(final String url, final String rule,
			final String name, final Icon icon) {
		this(url, rule, name);
		setIcon(icon);
	}

	public RuleEditorAction(final String url, final String rule,
			final boolean skip, final String name, final Icon icon) {
		this(url, rule, skip, name);
		setIcon(icon);
	}

	public RuleEditorAction(final String url, final String rule,
			final boolean skip, final JDialog par, final String name,
			final Icon icon) {
		this(url, rule, skip, par, name);
		setIcon(icon);
	}

	public void setName(final String name) {
		putValue(Action.NAME, name);
	}

	public void setIcon(final Icon icon) {
		putValue(Action.SMALL_ICON, icon);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		RuleEditorDialog.showRuleEditor(par, rule, url, skip);
	}

}
