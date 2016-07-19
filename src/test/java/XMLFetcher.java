import com.j2bugzilla.base.Bug;
import com.j2bugzilla.base.BugzillaConnector;
import com.j2bugzilla.base.BugzillaException;
import com.j2bugzilla.base.ConnectionException;
import com.j2bugzilla.rpc.BugSearch;
import org.junit.Test;
import org.w3c.dom.Document;
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
import java.net.URL;

/**
 * Created by peacefrog on 7/18/16.
 * Time 9:38 PM
 */
public class XMLFetcher {


	private BugzillaConnector connector;
	private String url = "https://bugs.eclipse.org/bugs/";

	public XMLFetcher() {
		connector = new BugzillaConnector();

		try {
			connector.connectTo(url);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void fetchBugzillaBugs() throws BugzillaException {
		BugSearch.SearchQuery[] queries = buildQuery();
		BugSearch search = new BugSearch(queries);

		connector.executeMethod(search);

		StringBuilder bugIDUrlBuilder = new StringBuilder(url);
		bugIDUrlBuilder.append("show_bug.cgi?");

		for (Bug bug : search.getSearchResults()) {
			bugIDUrlBuilder.append("id=").append(bug.getID()).append("&");
		}

		bugIDUrlBuilder.append("ctype=xml");

		Document doc = null;

		try {
			doc = fetchXML(bugIDUrlBuilder.toString());

			writeXMLFile(doc, "test.xml");
		} catch (ParserConfigurationException | IOException | SAXException | TransformerException e) {
			e.printStackTrace();
		}

	}

	private Document fetchXML(String url) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new URL(url).openStream());

		return doc;
	}

	private BugSearch.SearchQuery[] buildQuery() {
		BugSearch.SearchQuery[] queries = new BugSearch.SearchQuery[1];

		queries[0] = new BugSearch.SearchQuery(BugSearch.SearchLimiter.PRODUCT, "jdt");
//		queries[1] = new BugSearch.SearchQuery(BugSearch.SearchLimiter.COMPONENT, "ui");
//		queries[2] = new BugSearch.SearchQuery(BugSearch.SearchLimiter.STATUS, "unconfirmed");

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
}
