package de.visone.crawl;

import org.xml.sax.Attributes;

public class IncludeAttributeText {

    public static final IncludeAttributeText IMG_ALT = new IncludeAttributeText(
            "img", "alt");

    public static final IncludeAttributeText META = new IncludeAttributeText(
            "meta", "content");

    public static final class ATCondition {
        public final String name;

        public final String value;

        public ATCondition(final String name, final String value) {
            this.name = name;
            this.value = value;
        }

        public boolean satisfies(final Attributes a) {
            final int i = a.getIndex(name);
            if (i < 0) {
                return value == null;
            }
            return a.getValue(i).equals(value);
        }
    }

    private final String tag;
    private final String attr;
    private final ATCondition[] conditions;

    public IncludeAttributeText(final String tag, final String attr,
            final ATCondition... conditions) {
        this.tag = tag;
        this.attr = attr;
        this.conditions = conditions;
    }

    public String getTag() {
        return tag;
    }

    public String getAttributeName() {
        return attr;
    }

    public boolean satisfies(final Attributes a) {
        for (final ATCondition c : conditions) {
            if (!c.satisfies(a)) {
                return false;
            }
        }
        return true;
    }

    public String getContent(final Attributes a) {
        return a.getValue(attr);
    }

}
