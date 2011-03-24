/**
 * 
 */
package de.visone.crawl;

import java.io.File;
import java.net.URL;

import de.visone.crawl.gui.editor.RuleEditorDialog;
import de.visone.crawl.rules.RuleManager;
import de.visone.crawl.sys.Utils;

/**
 * @author Joschi
 * 
 */
public class RuleMain {

	private static void usage() {
		System.err.println("Usage: [-b <base>] [-u <url>] [-r <rule>]");
		System.err.println("<base>: The path to the base dir of the rules.");
		System.err.println("<url>: An optional example URL.");
		System.err.println("<rule>: The rule name.");
		System.exit(1);
	}

	public static void main(final String[] args) {
		Utils.setLookAndFeel();
		String base = null;
		String u = null;
		String rule = null;
		String cur = null;
		int i = args.length;
		if ((i & 1) != 0) {
			usage();
		}
		while (i-- > 0) {
			cur = args[i--].trim();
			final String flag = args[i].trim();
			if (flag.equals("-b")) {
				base = cur;
			} else if (flag.equals("-u")) {
				u = cur;
			} else if (flag.equals("-r")) {
				rule = cur;
			} else {
				usage();
			}
		}
		if (base != null) {
			RuleManager.setBaseDir(new File(base));
		}
		final URL url = u != null ? Utils.getURL(u) : null;
		RuleEditorDialog.showRuleEditor(null, rule, url,
				!(rule == null || url == null));
	}

}
