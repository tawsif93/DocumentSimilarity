import com.opencsv.CSVReader;
import org.apache.commons.io.FileUtils;
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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Created by peacefrog on 7/18/16.
 * Time 9:38 PM
 */
public class XMLFetcher {

//  Use -Xmx6g as JVM argument to increase Java Heap Memory before running this program

	private String url = "https://bugs.eclipse.org/bugs/";

	public XMLFetcher() {
	}

	@Test
	public void fetchBugzillaBugs() {

		ArrayList<String> bugIds = getBugIDsFormCSV();

		ArrayList<String> urls = buildURLs(bugIds);
		buildAllBugReportXML(urls);
		assertEquals("Size of urls", urls.size(), 26);
		assertEquals("Size of the bug list", bugIds.size(), 12810);
	}

	private ArrayList<String> getBugIDsFormCSV() {
		ArrayList<String> bugIDs = new ArrayList<>();

		fetchCSV();

		for (Integer i = 2001; i < 2017; i++) {
			CSVReader csvReader;
			try {
				csvReader = new CSVReader(new FileReader("CSV/" + i.toString() + ".csv"));
				String[] nextLine;
				csvReader.readNext();
				while ((nextLine = csvReader.readNext()) != null) {

					bugIDs.add(nextLine[0]);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			FileUtils.forceDeleteOnExit(new File("CSV"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bugIDs;
	}

	private void fetchCSV() {
		for (int i = 2001; i <= 2016; i++) {
			Integer year = i;
			String baseURL = "https://bugs.eclipse.org/bugs/buglist.cgi?" +
					"bug_status=VERIFIED&" +
					"classification=Eclipse&" +
					"f1=creation_ts&f2=creation_ts&" +
					"o1=greaterthaneq&o2=lessthaneq&" +
					"limit=0&list_id=14812660&" +
					"product=JDT&" +
					"query_format=advanced&" +
					"resolution=DUPLICATE&" +
					"v1=" + year.toString() + "-1-01%20&v2=" +
					year.toString() + "-12-31%20&" +
					"ctype=csv&human=1";

			File csvFile = new File("CSV/" + year.toString() + ".csv");

			try {
				FileUtils.copyURLToFile(new URL(baseURL), csvFile);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public void buildAllBugReportXML(ArrayList<String> urls) {
		buildMainXML(urls, "resolutionDUPLICATE.xml");
	}

	@Test
	public void buildTestBugReportXML() {

		ArrayList<String> ids = (ArrayList<String>) getBugIdfromRandomTestCSV();
		ArrayList<String> urls = buildURLs(ids);
		buildMainXML(urls, "random5.xml");

		assertEquals("Size of urls ", urls.size(), 1);
		assertEquals("Size of ids ", ids.size(), 100);
	}

	private void buildMainXML(ArrayList<String> urls, String fileName) {
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

			writeXMLFile(doc, fileName);

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
		int limit = 500;
		for (int i = 0; i < (results.size() / limit); i++) {
			start = i * limit;
			end = i * limit + limit;
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

	private List<String> getBugIdfromRandomTestCSV() {
		String fileData = null;
		try {
			fileData = FileUtils.readFileToString(new File("bug_id.csv"), Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}

		assert fileData != null;
		String[] bugIDs = fileData.split(",");

		ArrayList<String> idList = new ArrayList<>(Arrays.asList(bugIDs));

		return idList;
	}

	/**
	 * This method first collect xml stream from bugzilla
	 * Then stripped of the INVALID XML character which are less than 0x20 in Unicode
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


	public void writeXMLFile(Document doc, String fileName) throws TransformerException {

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(fileName));
		configureTransformer(transformer);
		transformer.transform(source, result);

		System.out.println("File saved!");
	}

	@SuppressWarnings("unused")
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
	 * @param transformer xml Transformer from {@link #printXMLtoConsole(Document)} and {@link #writeXMLFile(Document, String)}
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
