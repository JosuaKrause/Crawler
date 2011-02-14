package de.visone.crawl.gui.editor;

import de.visone.crawl.rules.BlacklistFilter;
import de.visone.crawl.rules.HtmlQuery;

public interface RuleListener {

	void changedBlacklist(BlacklistFilter bl);

	void changedRules(HtmlQuery q);

}
