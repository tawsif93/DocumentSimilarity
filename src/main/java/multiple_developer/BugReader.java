package multiple_developer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import single_developer.BugReport;
import single_developer.Stemmer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by peacefrog on 11/18/16.
 * Time 11:50 PM
 */
public class BugReader {
	private Stemmer stemmer;

	private ArrayList<NetworkLink> HN;

	public BugReader() {
		HN = new ArrayList<>();
		this.stemmer = new Stemmer();
	}

	public List<BugReport> parse(String path, String bug_history) {

		List<BugReport> reports = new ArrayList<>();

		try {
			File fXmlFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();


			DocumentBuilderFactory dbFactoryHistory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilderHistory = dbFactoryHistory.newDocumentBuilder();
			Document history_doc = dBuilderHistory.parse(new File(bug_history));
			history_doc.getDocumentElement().normalize();

			NodeList bugNodeList = doc.getElementsByTagName("bug");
			NodeList historyNodeList = history_doc.getElementsByTagName("bug");

			for (int temp = 0; temp < bugNodeList.getLength(); temp++) {

				Node bugNode = bugNodeList.item(temp);
				Node history_node = historyNodeList.item(temp);

				BugReport report;

				if (bugNode.getNodeType() == Node.ELEMENT_NODE) {

					Element bugNodeElement = (Element) bugNode;
					Element history_node_element = (Element) history_node;

					String bugID = bugNodeElement.getElementsByTagName("id").item(0).getTextContent();
					String name = bugNodeElement.getElementsByTagName("developer").item(0).getTextContent();
					String username = bugNodeElement.getElementsByTagName("developer_username").item(0).getTextContent();
					String summary = bugNodeElement.getElementsByTagName("short_desc").item(0).getTextContent();
					String description = bugNodeElement.getElementsByTagName("thetext").item(0).getTextContent();
					String product = bugNodeElement.getElementsByTagName("product").item(0).getTextContent();
					String component = bugNodeElement.getElementsByTagName("component").item(0).getTextContent();
					String creationTime = bugNodeElement.getElementsByTagName("creation_time").item(0).getTextContent();

					if (!DeveloperRepository.developersUsernameOriginalNameMapping.containsKey(username))
						DeveloperRepository.developersUsernameOriginalNameMapping.put(username, name);

					NodeList commentsNode = bugNodeElement.getElementsByTagName("comment");
					NodeList historyElements = history_node_element.getElementsByTagName("element");

//					for (int i = 1; i < historyElements.getLength(); i++) {
//						Node element = historyElements.item(i);
//						System.out.println("History " + ((Element) element).getElementsByTagName("who").item(0).getTextContent());
//					}

					ArrayList<Comment> commentArrayList = new ArrayList<>();

					for (int i = 0; i < commentsNode.getLength(); i++) {
						Node comment = commentsNode.item(i);
						Element commentDetails = (Element) comment;
						String commentID = commentDetails.getElementsByTagName("comment_id").item(0).getTextContent();
						String commentCount = commentDetails.getElementsByTagName("comment_count").item(0).getTextContent();
						String developerName = commentDetails.getElementsByTagName("who").item(0).getTextContent();
						String developerUsername = commentDetails.getElementsByTagName("commenter_username").item(0).getTextContent();
						String commentTime = commentDetails.getElementsByTagName("when").item(0).getTextContent();
						String commentText = commentDetails.getElementsByTagName("comment_text").item(0).getTextContent();

						if (!DeveloperRepository.developersUsernameOriginalNameMapping.containsKey(username))
							DeveloperRepository.developersUsernameOriginalNameMapping.put(developerUsername, developerName);

						Comment newComment = new Comment(commentID, commentCount, commentTime, commentText);
						commentArrayList.add(newComment);
					}

					/*
					 *  Creating Network node using Type declaration
					 *  Adding network nodes
					 */
					Bug newBug = new Bug(bugID, summary, description, creationTime, commentArrayList);
					Developer bugDeveloper = new Developer(name);

					NetworkNode bugNetworkNode = new NetworkNode("B", newBug);
					NetworkNode developerNetworkNode = new NetworkNode("D", bugDeveloper);

					/*
					 *  add newtwork nodes inside the network Array
					 */
					NetworkLink BD = new NetworkLink(bugNetworkNode, developerNetworkNode, 1);
					NetworkLink DB = new NetworkLink(developerNetworkNode, bugNetworkNode, 2);

					HN.add(BD);
					HN.add(DB);

					/*
					 * Working with related developers with the bug
					 * Create networks and add them into the
					 *
					 */
					for (int i = 1; i < historyElements.getLength(); i++) {
						Node element = historyElements.item(i);
//						System.out.println("History "+((Element)element).getElementsByTagName("who").item(0).getTextContent());
						String developerName = ((Element) element).getElementsByTagName("who").item(0).getTextContent();

						if (DeveloperRepository.developersUsernameOriginalNameMapping.containsKey(developerName)) {
							developerName = DeveloperRepository.developersUsernameOriginalNameMapping.get(developerName);
						}
						bugDeveloper = new Developer(developerName);

						developerNetworkNode = new NetworkNode("D", bugDeveloper);

						BD = new NetworkLink(bugNetworkNode, developerNetworkNode, 1);
						DB = new NetworkLink(developerNetworkNode, bugNetworkNode, 2);

						HN.add(BD);
						HN.add(DB);
					}

					/*
					 * Working with Comments inside a bug
					 * Making relation with BUG and Developer with that bug
					 * First step make relation with bug
					 * Second step make relation with developer
					 */

					for (int i = 0; i < commentsNode.getLength(); i++) {
						Node comment = commentsNode.item(i);
						Element commentDetails = (Element) comment;
						String commentID = commentDetails.getElementsByTagName("comment_id").item(0).getTextContent();
						String commentCount = commentDetails.getElementsByTagName("comment_count").item(0).getTextContent();
						String developerName = commentDetails.getElementsByTagName("who").item(0).getTextContent();
						String commentTime = commentDetails.getElementsByTagName("when").item(0).getTextContent();
						String commentText = commentDetails.getElementsByTagName("comment_text").item(0).getTextContent();

						Comment newComment = new Comment(commentID, commentCount, commentTime, commentText);

						NetworkNode commentNetworkNode = new NetworkNode("T", newComment);
						NetworkLink BT = new NetworkLink(bugNetworkNode, commentNetworkNode, 3);
						NetworkLink TB = new NetworkLink(commentNetworkNode, bugNetworkNode, 4);

						HN.add(BT);
						HN.add(TB);

						Developer commentDeveloper = new Developer(developerName);
						developerNetworkNode = new NetworkNode("D", commentDeveloper);

						NetworkLink DT = new NetworkLink(developerNetworkNode, commentNetworkNode, 5);
						NetworkLink TD = new NetworkLink(commentNetworkNode, developerNetworkNode, 6);

						HN.add(DT);
						HN.add(TD);
					}

					/*
					 * Creating BUG Component relation
					 */

					Component bugComponent = new Component(component);
					NetworkNode componentNetworkNode = new NetworkNode("C", bugComponent);

					NetworkLink BC = new NetworkLink(bugNetworkNode, componentNetworkNode, 7);
					NetworkLink CB = new NetworkLink(componentNetworkNode, bugNetworkNode, 8);

					HN.add(BC);
					HN.add(CB);

					/*
					 * Creating Bug Product relation
					 */

					Product bugProduct = new Product(product);
					NetworkNode productNetworkNode = new NetworkNode("P", bugProduct);

					NetworkLink CP = new NetworkLink(componentNetworkNode, productNetworkNode, 9);
					NetworkLink PC = new NetworkLink(productNetworkNode, componentNetworkNode, 10);

					HN.add(CP);
					HN.add(PC);

//					System.out.println(eElement.getElementsByTagName("comment").getLength());

					summary = stemmer.englishStemer(summary).trim();
					description = stemmer.englishStemer(description).trim();
					report = new BugReport(name, summary, description);

					reports.add(report);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reports;
	}

	public ArrayList<String> find_DBD_Path(String firstDeveloperName, String secondDeveloperName) {
		ArrayList<String> paths = new ArrayList<>();

		for (int i = 0; i < HN.size(); i++) {
			NetworkLink firstLink = HN.get(i);
			if (firstLink.relationType == 2) {
				NetworkNode firstDeveloperNode = firstLink.firstNode;
				Developer firstDeveloper = (Developer) firstDeveloperNode.customNodes;
				if (firstDeveloper.getName().equals(firstDeveloperName)) {
//					System.out.println("pass");
					NetworkNode firstBugNode = firstLink.secondNode;
					Bug firstBug = (Bug) firstBugNode.customNodes;

					for (int j = 0; j < HN.size(); j++) {
						NetworkLink secondLink = HN.get(j);

						if (secondLink.relationType == 1) {
							NetworkNode secondBugNode = secondLink.firstNode;
							Bug secondBug = (Bug) secondBugNode.customNodes;

							if (secondBug.getBugID().equals(firstBug.getBugID())) {
								NetworkNode secondDeveloperNode = secondLink.secondNode;
								Developer secondDeveloper = (Developer) secondDeveloperNode.customNodes;

								if (secondDeveloper.getName().equals(secondDeveloperName)) {
									String path = firstDeveloperName + " " + secondBug.getBugID() + " " + secondDeveloperName;
									paths.add(path);
								}
							}
						}
					}
				}
			}

		}

		return paths;
	}

	public static void main(String[] args) {
		BugReader reader = new BugReader();
		reader.parse("/home/peacefrog/Desktop/data_test/finalData_2.xml", "//home/peacefrog/Desktop/data_test/test_JSoup.xml");

		ArrayList<String> paths = reader.find_DBD_Path("Dejan Glozic", "Dani Megert");

		System.out.println(paths.size());

		paths.forEach(System.out::println);

	}
}
