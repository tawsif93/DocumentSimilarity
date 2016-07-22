import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by peacefrog on 7/2/16.
 * 3:59 PM
 */
public class Main {
	public static void main(String[] args) throws IOException, ParseException {
//		List<BugReport> bugReports = XMLFileParser.getInstance().parse("test.xml");
		BugReport testReport = XMLFileParser.getInstance().parse("report.xml").get(0);
//		Indexer indexer = new Indexer(bugReports);
		Indexer indexer = new Indexer();
//		indexer.createIndex();

		CosineDocumentSimilarity similarity = new CosineDocumentSimilarity(indexer.createTestIndex(
				testReport.getSummary(), testReport.getDescription()));

		List<Developer> documentSimilarities = similarity.getSimilarities();

		Collections.sort(documentSimilarities, new Developer());
//		documentSimilarities.forEach(System.out::println);

		printAllResult(documentSimilarities);
		System.out.println();
		printSummedUpSortedResult(documentSimilarities);

		WordMatcher wordMatcher = new WordMatcher();
		ArrayList<WordFrequencyContainer> searchReports = wordMatcher.searchReports(new Stemmer().englishStemer("UI locks up toggling on the variable filters"));

		searchReports.forEach(wordFrequencyContainer ->  {
			System.out.println(wordFrequencyContainer.getDeveloperName());
			wordFrequencyContainer.getDetails().forEach((wordFrequencyDetails -> System.out.println("\t" + wordFrequencyDetails.getIndexName() +" " + wordFrequencyDetails.getWord() + " " + wordFrequencyDetails.getFrequency())));
		});

		System.out.println();
		System.out.println("***************Word Developer Details***********");
		wordMatcher.getWordDeveloperDetailsMap().forEach((s, wordDeveloperDetails) -> {
			System.out.println(s + " ( Total Developer: " + wordDeveloperDetails.getDevCount().size() + " )");
			wordDeveloperDetails.getDevCount().forEach((name, floats) -> System.out.println("\t\t" + name + " " + floats[0]));
		});
	}

	private static void printSummedUpSortedResult(List<Developer> developers){

		Collections.sort(developers, (o1, o2) -> o2.getSummedSimilarity().compareTo(o1.getSummedSimilarity()));
		System.out.printf("%-25.30s | %-20.30s \n", "Developer name" , "Similarity");

		developers.stream().filter(developer -> developer.getSummedSimilarity() > 0.0).forEachOrdered(developer -> System.out.printf("%-25.30s | %-20.30s \n", developer.getName(), developer.getSummedSimilarity()));
	}

	private static void printAllResult(List<Developer> documentSimilarities) {
		System.out.printf("%-30.30s | %-20.30s | %-20.30s\n", "Developer name" , "Summary similarity", "Description similarity");
		documentSimilarities.stream().filter(developer -> developer.getSummedSimilarity() > 0.0).forEachOrdered(developer -> System.out.printf("%-30.30s | %-20.30s | %-20.30s\n", developer.getName(), developer.getSummarySimilarity(), developer.getDescriptionSimilarity()));
	}
}
