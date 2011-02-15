package de.visone.crawl.sys;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.UIManager;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.visone.crawl.xml.SaxParser;

/**
 * Provides some useful methods.
 * 
 * @author Joschi
 * 
 */
public class Utils {

	/**
	 * The new-line-character.
	 */
	public static final String NL = System.getProperty("line.separator");

	/**
	 * Collects the garbage.
	 */
	public static void gc() {
		System.gc();
		System.runFinalization();
	}

	/**
	 * @return The current time.
	 */
	public static long now() {
		return System.currentTimeMillis();
	}

	private static long lastCall = now();

	public static void timeSinceLastCall() {
		timeSinceLastCall(null);
	}

	public static void timeSinceLastCall(String optOut) {
		if (optOut == null) {
			optOut = "";
		}
		if (!optOut.isEmpty()) {
			optOut = " " + optOut;
		}
		final long now = now();
		System.out.println((now - lastCall) + "ms" + optOut);
		lastCall = now;
	}

	/**
	 * @param urls
	 *            The URLs.
	 * @return The URL-objects or <code>null</code> at the given position if
	 *         some were malformed.
	 */
	public static URL[] getURLs(final String[] urls) {
		int i = urls.length;
		final URL[] res = new URL[i];
		while (i-- > 0) {
			res[i] = getURL(urls[i]);
		}
		return res;
	}

	/**
	 * @param url
	 *            The URL.
	 * @return The URL-object or <code>null</code> if it was malformed.
	 */
	public static URL getURL(final String url) {
		return getURL(url, (URL) null);
	}

	/**
	 * @param url
	 *            The URL.
	 * @param base
	 *            The base URL.
	 * @return The URL-object or <code>null</code> if it was malformed.
	 */
	public static URL getURL(String url, final URL base) {
		url = url.replace(" ", "%20");
		if (base != null) {
			if (url.indexOf("://") < 0) {
				String path = base.getPath();
				path = path.substring(0, path.lastIndexOf('/'));
				url = base.getProtocol() + "://" + base.getHost()
						+ (url.startsWith("/") ? url : path + "/" + url);
			}
		}
		final int i = url.indexOf('#');
		url = (i <= 0) ? url : url.substring(0, i);
		url = url.replace(" ", "%20");
		try {
			final URL u = new URL(url);
			String path = "/" + u.getPath();
			String last;
			do {
				last = path;
				path = path.replace("/./", "/");
				path = path.replaceAll("/[^/]+/+\\.\\./", "/");
				path = path.replace("//", "/");
			} while (!path.equals(last));
			// removing starting ../ with no counterpart as in
			// http://tools.ietf.org/html/rfc3986#section-5.4.2
			path = path.replaceAll("(\\.\\./)+", "/");
			final String q = u.getQuery();
			if (q != null && !q.isEmpty()) {
				path += "?" + q;
			}
			return new URL(u.getProtocol(), u.getHost(), u.getPort(), path);
		} catch (final MalformedURLException e) {
			return null;
		}
	}

	/**
	 * @param url
	 *            The URL.
	 * @param base
	 *            The base URL.
	 * @return The URL-object.
	 * @throws MalformedURLException
	 *             When the URL was malicious.
	 */
	public static URL getURLnn(final String url, final URL base)
			throws MalformedURLException {
		final URL u = getURL(url, base);
		if (u == null) {
			if (base != null) {
				throw new MalformedURLException(base + " --> '" + url + "'");
			}
			throw new MalformedURLException(url);
		}
		return u;
	}

	/**
	 * @param url
	 *            The URL.
	 * @return The URL-object.
	 * @throws MalformedURLException
	 *             When the URL was malicious.
	 */
	public static URL getURLnn(final String url) throws MalformedURLException {
		return getURLnn(url, null);
	}

	public static File toFile(final URL url, final File baseDir) {
		return toFile(url, baseDir, null);
	}

	public static File toFile(final URL url, final File baseDir,
			final String ext) {
		String path = url.getHost() + "/" + url.getFile();
		path = path.replace("%", "%25");
		path = path.replace("?", "%3F");
		path = path.replace("&", "%26");
		path = path.replace("=", "%3D");
		path = path.replace("*", "%2A");
		path = path.replace(":", "%3A");
		path = path.replace("<", "%3C");
		path = path.replace(">", "%3E");
		path = path.replace("|", "%7C");
		path = path.replace("\"", "%22");
		path = path.replace("\\", "%5C");
		while (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		if (ext != null) {
			path += "." + ext;
		}
		return new File(baseDir, path);
	}

	public static void ensureDir(final File dir) {
		if (dir == null || dir.exists()) {
			return;
		}
		ensureDir(dir.getParentFile());
		dir.mkdir();
	}

	/**
	 * The name-space property of XML. It is used to deactivate it, so that the
	 * parsers won't nag.
	 */
	private static final String NAMESPACE = "http://xml.org/sax/features/namespaces";

	/**
	 * Creates a new tag-soup-parser to parse HTML.
	 * 
	 * @return The Parser.
	 * @throws SAXException
	 *             If an error occurs.
	 */
	public static Parser createTagSoupParser() throws SAXException {
		final Parser parser = new Parser();
		parser.setFeature(NAMESPACE, false);
		return parser;
	}

	/**
	 * Creates an input stream to get the contents of an URL.
	 * 
	 * @param url
	 *            The URL.
	 * @param userAgent
	 *            The User-Agent of the connection.
	 * @return The InputStream.
	 * @throws IOException
	 *             If an error occurs.
	 */
	public static InputStream createInputStream(final URL url,
			final String userAgent) throws IOException {
		return createInputStream(getConnection(url), userAgent);
	}

	private static Map<String, String> customHeaders = new HashMap<String, String>();

	public static void clearCustomHeaders() {
		customHeaders.clear();
	}

	public static void addCustomHeader(final String name, final String value) {
		customHeaders.put(name, value);
	}

	public static InputStream createInputStream(final HttpURLConnection http,
			final String userAgent) throws IOException {
		if (userAgent != null) {
			http.addRequestProperty("User-Agent", userAgent);
		}
		for (final Entry<String, String> ch : customHeaders.entrySet()) {
			http.addRequestProperty(ch.getKey(), ch.getValue());
		}
		try {
			return http.getInputStream();
		} catch (final IOException e) {
			throw new IOException(e);
		}
	}

	private static HttpURLConnection getConnection(final URL url)
			throws IOException {
		try {
			return (HttpURLConnection) url.openConnection();
		} catch (final IOException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Creates an input source to get the contents of an URL.
	 * 
	 * @param url
	 *            The URL.
	 * @param enc
	 *            The encoding. If <code>null</code> we try to find it ourself.
	 * @param userAgent
	 *            The user agent of the connection.
	 * @return The InputSource.
	 * @throws IOException
	 *             If an error occurs.
	 */
	public static InputSource createInputSource(final URL url,
			final String enc, final String userAgent) throws IOException {
		final HttpURLConnection http = getConnection(url);
		final InputSource is = new InputSource(createInputStream(http,
				userAgent));
		if (http.getResponseCode() != 200) {
			throw new IOException("invalid response code: "
					+ http.getResponseCode());
		}
		String e = http.getContentType();
		checkContentType(e);
		if (enc != null) {
			is.setEncoding(enc);
		} else {
			if (e != null) {
				final String[] parts = e.split(";\\s*charset\\s*=");
				if (parts.length > 1) {
					e = parts[1].trim();
					if (!e.isEmpty()) {
						is.setEncoding(e);
					}
				}
			}
		}
		return is;
	}

	private static String[] assuredContentTypes = new String[] { "text/html",
			"text/plain", "text/xml", "application/x-latex",
			"application/xhtml+xml", "application/rss+xml",
			"application/atom+xml" };

	private static String[] invalidContentTypes = new String[] {
			"application/EDI", "application/json", "application/javascript",
			"text/javascript", "text/cmd", "text/css",
			"application/octet-stream", "application/ogg", "application/pdf",
			"application/postscript", "application/soap+xml",
			"application/xml-dtd", "application/zip", "application/vnd",
			"application/msword", "application/x-", "video", "image", "audio", };

	private static void checkContentType(final String e) throws IOException {
		for (final String s : assuredContentTypes) {
			if (e.contains(s)) {
				return;
			}
		}
		for (final String s : invalidContentTypes) {
			if (e.contains(s)) {
				throw new IOException("illegal content-type: " + e);
			}
		}
	}

	/**
	 * Escapes a single character for URLs.
	 * 
	 * @param str
	 *            The String to escape.
	 * @param c
	 *            The character to escape.
	 * @return The escaped String.
	 */
	public static String escape(final String str, final Character c) {
		final String hex = "00" + Integer.toHexString(c).toUpperCase();
		return str.replace(c.toString(), "%" + hex.substring(hex.length() - 2));
	}

	public static void crawl(final CrawlState state,
			final AbstractUrlPool pool, final String userAgent)
			throws SAXException, IOException {
		final Parser p = Utils.createTagSoupParser();
		final InputSource s = createInputSource(state.getURL(), null, userAgent);
		final SaxParser sxp = new SaxParser(state, pool);
		try {
			p.setContentHandler(sxp);
			p.parse(s);
		} finally {
			final InputStream is = s.getByteStream();
			if (is != null) {
				is.close();
			}
		}
	}

	public static void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus."
					+ "NimbusLookAndFeel");
		} catch (final Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (final Exception inner) {
				inner.printStackTrace();
			}
		}
	}

}
