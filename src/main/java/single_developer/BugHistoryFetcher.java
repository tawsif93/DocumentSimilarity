package single_developer;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.w3c.dom.Element;

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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by peacefrog on 11/2/16.
 * Time 1:50 PM
 */
public class BugHistoryFetcher {

	static org.w3c.dom.Document dom;

	public static void main(String[] args) throws IOException, TransformerException, ParserConfigurationException {
//		Document doc = Jsoup.connect("https://bugs.eclipse.org/bugs/show_activity.cgi?id=213001").get();
//		Elements newsHeadlines = doc.select("html body.bugs-eclipse-org-bugs.yui-skin-sam div#bugzilla-body table tbody tr");
//
//		newsHeadlines.forEach(element -> {
//			System.out.println(element.select("td").size());
//			System.out.println(element.select("td").html());
//		});

		BugHistoryFetcher test = new BugHistoryFetcher();
		try {

			test.formatXmlFile();
		} catch (UnknownHostException | SocketTimeoutException e) {
			System.out.println("ERROR");
			e.printStackTrace();
		} finally {
			test.writeXMLFile(dom, "aspectj_2006-2016_new_history_part_2.xml");
		}
	}


	public void writeXMLFile(org.w3c.dom.Document doc, String fileName) throws TransformerException {

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(fileName));
		configureTransformer(transformer);
		transformer.transform(source, result);

		System.out.println("File saved!");
	}

	/**
	 * Configure XML transformer for indentation
	 *
	 * @param transformer xml Transformer from {@link #printXMLtoConsole(org.w3c.dom.Document)} and {@link #writeXMLFile(org.w3c.dom.Document, String)}
	 */
	private void configureTransformer(Transformer transformer) {
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	}

	private ArrayList<String> readBugIDs() throws IOException {

		String bugData = FileUtils.readFileToString(new File("bugIDList.csv"), Charset.defaultCharset());
		String[] bugs = bugData.split("\n");

		ArrayList<String> idList = new ArrayList<>(Arrays.asList(bugs));
		return idList;
	}

	public void formatXmlFile() throws ParserConfigurationException, IOException {

		ArrayList<String> bugs = readBugIDs();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		dom = db.newDocument();
		Element rootEle = dom.createElement("bugs");
		dom.appendChild(rootEle);


		for (int i = 0; i < bugs.size(); i++) {
			//System.out.println(((Element) bugs.item(i)).getElementsByTagName("bug_id").item(0).getTextContent());
			System.out.println("Current bug: " + bugs.get(i));
			Document doc = Jsoup.connect("https://bugs.eclipse.org/bugs/show_activity.cgi?id=" + bugs.get(i)).timeout(10 * 1000).get();
//			Document doc = Jsoup.connect("https://netbeans.org/bugzilla/show_activity.cgi?id=" + bugs.get(i)).timeout(10 * 1000).get();
//			Elements newsHeadlines = doc.select("html body #wrapper-flex #middle table tbody tr td div div#bugzilla-body table tbody tr");//NETBEANS
			Elements newsHeadlines = doc.select("html body.bugs-eclipse-org-bugs.yui-skin-sam div#bugzilla-body table tbody tr");//ECLIPSE BUGZILLA
			Element bug = dom.createElement("bug");
			Element id = dom.createElement("bug_id");

			id.appendChild(dom.createTextNode(bugs.get(i)));
			bug.appendChild(id);

			final String[] name = new String[1];
			final String[] time = new String[1];

			newsHeadlines.forEach(element -> {
//
//				System.out.println(element.select("td").size());
//				System.out.println(element.select("td").html());

				Element newElement = dom.createElement("element");
				Element who = dom.createElement("who");
				Element when = dom.createElement("when");
				Element what = dom.createElement("what");
				Element removed = dom.createElement("removed");
				Element added = dom.createElement("added");

				if (element.select("td").size() == 5) {
					name[0] = element.select("td").get(0).html();
					time[0] = element.select("td").get(1).html();
					who.appendChild(dom.createTextNode(name[0]));
					when.appendChild(dom.createTextNode(time[0]));
					what.appendChild(dom.createTextNode(element.select("td").get(2).html()));
					removed.appendChild(dom.createTextNode(element.select("td").get(3).html()));
					added.appendChild(dom.createTextNode(element.select("td").get(4).html()));

				} else if (element.select("td").size() == 3) {
					who.appendChild(dom.createTextNode(name[0]));
					when.appendChild(dom.createTextNode(time[0]));
					what.appendChild(dom.createTextNode(element.select("td").get(0).html()));
					removed.appendChild(dom.createTextNode(element.select("td").get(1).html()));
					added.appendChild(dom.createTextNode(element.select("td").get(2).html()));
				}

				//System.out.println(((Element) bugs.item(i)).getElementsByTagName("bug_id").item(0).getNodeValue());

				newElement.appendChild(who);
				newElement.appendChild(when);
				newElement.appendChild(what);
				newElement.appendChild(removed);
				newElement.appendChild(added);

				bug.appendChild(newElement);
			});


			rootEle.appendChild(bug);

		}
	}
}
