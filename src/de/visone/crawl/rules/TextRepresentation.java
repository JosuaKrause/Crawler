package de.visone.crawl.rules;

import static de.visone.crawl.gui.editor.Editor.BLUE;
import static de.visone.crawl.gui.editor.Editor.GREEN;
import static de.visone.crawl.gui.editor.Editor.RED;

import java.util.HashMap;
import java.util.Map;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.tree.TreeNode;

import de.visone.crawl.gui.editor.Node;

public class TextRepresentation {

    private static class Span {
        int from;
        int to;

        public Span(final int from, final int to) {
            this.from = from;
            this.to = to;
        }

        public boolean contains(final int pos) {
            return pos >= from && pos < to;
        }
    }

    private final String str;

    private final Map<TreeNode, Span> areas;

    private final Node root;

    private Object tag;

    public TextRepresentation(final Node root) {
        this.root = root;
        areas = new HashMap<TreeNode, Span>();
        final StringBuilder sb = new StringBuilder();
        setText(sb, root);
        str = sb.toString();
        tag = null;
    }

    private static void append(final StringBuilder sb, final String text) {
        if (text.isEmpty()) {
            return;
        }
        if (sb.length() != 0) {
            sb.append(' ');
        }
        sb.append(text);
    }

    private void setText(final StringBuilder sb, final Node n) {
        final int begin = sb.length();
        append(sb, n.getText(null));
        for (final Node c : n.getChilds()) {
            setText(sb, c);
            append(sb, n.getText(c));
        }
        areas.put(n, new Span(begin, sb.length()));
    }

    public void setHighlight(final Node n, HtmlQuery q, final Highlighter h) {
        final int a = n.accepts(q);
        if (a < 0) {
            q = null;
        }
        for (final Node c : n.getChilds()) {
            setHighlight(c, q, h);
        }
        if (a != 0) {
            final Span s = areas.get(n);
            if (s != null) {
                try {
                    h.addHighlight(s.from, s.to, (a > 0) ? GREEN : RED);
                } catch (final BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void highlight(final Highlighter h, final TreeNode n) {
        Span s = areas.get(n);
        if (s == null) {
            s = new Span(0, 0);
        }
        try {
            if (tag == null) {
                tag = h.addHighlight(s.from, s.to, BLUE);
            } else {
                h.changeHighlight(tag, s.from, s.to);
            }
        } catch (final BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void removeHighlights(final Highlighter h) {
        tag = null;
        h.removeAllHighlights();
    }

    public int getIndex(final TreeNode n) {
        final Span s = areas.get(n);
        if (s == null) {
            return -1;
        }
        return s.from;
    }

    public int getEndIndex(final TreeNode n) {
        final Span s = areas.get(n);
        if (s == null) {
            return -1;
        }
        return s.to;
    }

    public TreeNode findNodeFor(final int pos) {
        return findNodeFor(root, pos);
    }

    private TreeNode findNodeFor(final Node node, final int pos) {
        final Span s = areas.get(node);
        if (s == null) {
            return null;
        }
        if (!s.contains(pos)) {
            return null;
        }
        for (final Node c : node.getChilds()) {
            final Span span = areas.get(c);
            if (span == null) {
                continue;
            }
            if (span.contains(pos)) {
                return findNodeFor(c, pos);
            }
        }
        return node;
    }

    @Override
    public String toString() {
        return str;
    }

}
