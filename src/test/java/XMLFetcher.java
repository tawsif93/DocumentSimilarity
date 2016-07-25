import com.j2bugzilla.base.BugzillaException;
import com.j2bugzilla.rpc.BugSearch;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Created by peacefrog on 7/18/16.
 * Time 9:38 PM
 */
public class XMLFetcher {


	private String url = "https://bugs.eclipse.org/bugs/";

	public XMLFetcher() {
	}

	@Test
	public void fetchBugzillaBugs() throws BugzillaException {

		ArrayList<String> bugIds = getBugIDsFormCSV();

		ArrayList<String> urls = buildURLs(bugIds);
		assertEquals("Size of urls", urls.size(), 16);
		assertEquals("Size of the bug list", bugIds.size(), 7600);
		buildMainXML(urls);
	}

	private ArrayList<String> getBugIDsFormCSV() {
		return null;
	}

	public void buildMainXML(ArrayList<String> urls) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();

			Document doc = builder.newDocument();

			Element rootElement = doc.createElement("bugzilla");
			rootElement.setAttribute("version", "4.4.7");
			rootElement.setAttribute("urlbase", url);

			doc.appendChild(rootElement);

			urls.forEach(url -> {
				System.out.println(url);
				getXMLBugNodes(url).forEach(node -> rootElement.appendChild(doc.importNode(node, true)));
			});

			writeXMLFile(doc, "test.xml");

		} catch (ParserConfigurationException | TransformerException e) {
			e.printStackTrace();
		}
	}

	private ArrayList<Node> getXMLBugNodes(String url) {
		Document document = fetchXML(url);
		NodeList nList = document.getElementsByTagName("bug");
		ArrayList<Node> nodes = new ArrayList<>();

		for (int i = 0; i < nList.getLength(); i++) {
			nodes.add(nList.item(i));
		}
		return nodes;
	}

	private ArrayList<String> buildURLs(List<String> results) {
		ArrayList<String> urls = new ArrayList<>();

		int end = 0;
		int start;
		for (int i = 0; i < (results.size() / 500); i++) {
			start = i * 500;
			end = i * 500 + 500;
			StringBuilder tempURL = new StringBuilder(url);
			tempURL.append("show_bug.cgi?");

			for (int j = start; j < end; j++) {

				tempURL.append("id=").append(results.get(j)).append("&");
			}

			tempURL.append("ctype=xml");

			urls.add(tempURL.toString());
		}


		if (end < results.size()) {
			StringBuilder tempURL = new StringBuilder(url);
			tempURL.append("show_bug.cgi?");

			for (int i = end; i < results.size(); i++) {
				tempURL.append("id=").append(results.get(i)).append("&");
			}
			tempURL.append("ctype=xml");

			urls.add(tempURL.toString());
		}

		return urls;
	}

	/**
	 * This method fiorst collect xml stream from bugzilla
	 * Then stripped of the INVALID XML charachter which are less than 0x20 in Unicode
	 * Then convert that sting to Input Stream from XML parsing
	 *
	 * @param url url with some bug id send from {{@link #getXMLBugNodes(String)}}
	 * @return returns a xml Document
	 */
	private Document fetchXML(String url) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		Document doc = null;
		try {
			InputStream stream = new URL(url).openStream();
			String receivedXML = IOUtils.toString(stream, "UTF-8");
			receivedXML = stripNonValidXMLCharacters(receivedXML);
			stream = IOUtils.toInputStream(receivedXML, "UTF-8");

			db = dbf.newDocumentBuilder();
			doc = db.parse(stream);
		} catch (ParserConfigurationException | IOException | SAXException e) {
			e.printStackTrace();
		}

		return doc;
	}

	private BugSearch.SearchQuery[] buildQuery() {
		BugSearch.SearchQuery[] queries = new BugSearch.SearchQuery[4];

		queries[0] = new BugSearch.SearchQuery(BugSearch.SearchLimiter.PRODUCT, "jdt");
//		queries[1] = new BugSearch.SearchQuery(BugSearch.SearchLimiter.COMPONENT, "ui");
		queries[1] = new BugSearch.SearchQuery(BugSearch.SearchLimiter.STATUS, "verified");
		queries[2] = new BugSearch.SearchQuery(BugSearch.SearchLimiter.RESOLUTION, "fixed");
		queries[3] = new BugSearch.SearchQuery(BugSearch.SearchLimiter.LIMIT, "7600");

		return queries;
	}

	public void writeXMLFile(Document doc, String fileName) throws TransformerException {

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(fileName));
		configureTransformer(transformer);
		transformer.transform(source, result);

		System.out.println("File saved!");
	}

	public void printXMLtoConsole(Document doc) throws TransformerException {

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(System.out);

		configureTransformer(transformer);
		transformer.transform(source, result);
	}


	/**
	 * Configure XML transformer for indentation
	 *
	 * @param transformer
	 */
	private void configureTransformer(Transformer transformer) {
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	}

	/**
	 * @param in The String whose non-valid characters we want to remove.
	 * @return The in String, stripped of non-valid characters.
	 */
	public String stripNonValidXMLCharacters(String in) {
		StringBuilder out = new StringBuilder();
		char current;

		if (in == null || ("".equals(in))) return "";
		for (int i = 0; i < in.length(); i++) {
			current = in.charAt(i);
			if ((current == 0x9) ||
					(current == 0xA) ||
					(current == 0xD) ||
					((current >= 0x20) && (current <= 0xD7FF)) ||
					((current >= 0xE000) && (current <= 0xFFFD)) ||
					((current >= 0x10000) && (current <= 0x10FFFF)))
				out.append(current);
		}
		return out.toString();
	}
}
