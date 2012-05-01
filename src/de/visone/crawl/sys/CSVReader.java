package de.visone.crawl.sys;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class CSVReader {

    private final Scanner in;

    private final char comma;

    private final char quot;

    private String[] nextRow;

    public CSVReader(final File in) throws IOException {
        this(new Scanner(in));
    }

    public CSVReader(final Scanner in) {
        this(in, ';', '"');
    }

    public CSVReader(final Scanner in, final char comma, final char quot) {
        this.in = in;
        this.comma = comma;
        this.quot = quot;
        fetchNextRow();
    }

    private static StringBuilder addCur(final List<String> list,
            final StringBuilder cur) {
        if (cur != null) {
            list.add(cur.toString());
        }
        return new StringBuilder();
    }

    private void fetchNextRow() {
        if (!in.hasNextLine()) {
            nextRow = null;
            return;
        }
        final List<String> list = new LinkedList<String>();
        final StringBuilder row = new StringBuilder();
        StringBuilder cur = null;
        int pos = 0;
        boolean inQuote = false;
        boolean replacing = false;
        boolean atStart = true;
        do {
            if ((inQuote && !replacing) || atStart) {
                row.append(in.nextLine());
            } else {
                inQuote = false;
            }
            while (pos < row.length()) {
                final char c = row.charAt(pos++);
                if (atStart) {
                    atStart = false;
                    cur = addCur(list, cur);
                    if (c == quot) {
                        inQuote = true;
                        replacing = false;
                        continue;
                    }
                    inQuote = false;
                }
                if (c == comma) {
                    if (!inQuote || replacing) {
                        atStart = true;
                        continue;
                    }
                }
                if (inQuote && c == quot) {
                    replacing = !replacing;
                    if (replacing) {
                        continue;
                    }
                }
                if (cur != null) {
                    cur.append(c);
                }
            }
            if (atStart) {
                atStart = false;
                cur = addCur(list, cur);
                inQuote = false;
            }
        } while (inQuote && in.hasNextLine());
        addCur(list, cur);
        nextRow = list.toArray(new String[list.size()]);
    }

    public boolean hasNextRow() {
        return nextRow != null;
    }

    public String[] getNextRow() {
        final String[] res = nextRow;
        fetchNextRow();
        return res;
    }

}
