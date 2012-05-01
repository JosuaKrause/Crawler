package de.visone.crawl.texter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.LinkedList;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.Attributes;

import com.sun.xml.internal.ws.api.streaming.XMLStreamWriterFactory;

import de.visone.crawl.IncludeAttributeText;
import de.visone.crawl.rules.HtmlQuery;

public class XmlTexterImpl extends FullTexterImpl {

    private static String ENC = "UTF-8";

    private static String ROOT = "html";

    private XMLStreamWriter out;

    private ByteArrayOutputStream baos;

    public XmlTexterImpl(final URL url, final HtmlQuery links,
            final HtmlQuery text) {
        super(url, links, text, new LinkedList<IncludeAttributeText>());
        baos = new ByteArrayOutputStream();
        out = XMLStreamWriterFactory.create(baos);
        try {
            out.writeStartDocument(ENC, "1.0");
            out.writeStartElement(ROOT);
        } catch (final XMLStreamException e) {
            // should never happen
        }
    }

    @Override
    public void startTag(final String tag, final Attributes a) {
        ensureOpen();
        // super before processing
        super.startTag(tag, a);
        if (acceptString()) {
            if (ROOT.equals(tag)) {
                return;
            }
            try {
                out.writeStartElement(tag);
                final int len = a.getLength();
                for (int i = 0; i < len; ++i) {
                    out.writeAttribute(a.getLocalName(i), a.getValue(i));
                }
            } catch (final XMLStreamException e) {
                // should never happen
            }
        }
    }

    @Override
    public void endTag(final String tag) {
        ensureOpen();
        // super after processing
        if (acceptString()) {
            if (ROOT.equals(tag)) {
                return;
            }
            try {
                out.writeEndElement();
            } catch (final XMLStreamException e) {
                // should never happen
            }
        }
        super.endTag(tag);
    }

    @Override
    public void string(final String str) {
        ensureOpen();
        try {
            out.writeCData(str);
        } catch (final XMLStreamException e) {
            // should never happen
        }
    }

    private void ensureOpen() {
        if (out == null) {
            throw new IllegalStateException("stream already closed");
        }
    }

    @Override
    public String getText() {
        if (out != null) {
            try {
                out.writeEndDocument();
                out.close();
            } catch (final XMLStreamException e) {
                // ignore
            }
            out = null;
        }
        try {
            return baos.toString(ENC);
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (out != null) {
            try {
                out.close();
            } catch (final XMLStreamException e) {
                // nothing to do
            }
            out = null;
        }
        if (baos != null) {
            try {
                baos.close();
            } catch (final IOException e) {
                // ignore
            }
            baos = null;
        }
    }

}
