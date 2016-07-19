/**
 * Created by peacefrog on 7/2/16.
 * 12:55 PM
 */

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XMLFileParser {


	private static  XMLFileParser parser = null;
	private Stemmer stemmer ;

	public XMLFileParser() {
		stemmer = new Stemmer();
	}

	public static XMLFileParser getInstance() {
		if(parser == null){
			parser = new XMLFileParser();
		}

		return parser;
	}
	public List<BugReport> parse(String path ){

		List<BugReport> reports = new ArrayList<>();

		try {
			File fXmlFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();

//			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

			NodeList nList = doc.getElementsByTagName("bug");


			for (int temp = 0; temp < nList.getLength(); temp++) {
//				System.out.println("\n----------------------------");

				Node nNode = nList.item(temp);

				BugReport report ;

//				System.out.println("\nCurrent Element :" + nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					Element desc = (Element) eElement.getElementsByTagName("long_desc").item(0);

					String name = ((Element)eElement.getElementsByTagName("reporter").item(0)).getAttribute("name");
					String summary = eElement.getElementsByTagName("short_desc").item(0).getTextContent() ;
					String description = desc.getElementsByTagName("thetext").item(0).getTextContent();

//					System.out.println("Reporter name : " + name);
//					System.out.println("Summary : " + summary);
//					System.out.println("Description :\n" + description);

					summary = stemmer.englishStemer(summary);
					description = stemmer.englishStemer(description);

					report = new BugReport(name , summary , description);

					reports.add(report);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return  reports;
	}

}