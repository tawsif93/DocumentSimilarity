package single_developer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by peacefrog on 7/13/16.
 * Time 11:32 AM
 */
public class WordMatcher {


	private ArrayList<WordFrequencyContainer> frequencies ;
	private ArrayList<WordFrequencyDetails> lists ;
	private ArrayList<String> wordTracker ;
	private Map<String, WordDeveloperDetails> wordDeveloperDetailsMap = new HashMap<>();
	private boolean found ;

	private final String indexPath = Constants.INDEX;
	private final String[] fields = {Constants.SUMMARY, Constants.DESCRIPTION};

	public WordMatcher(){
		lists = new ArrayList<>();
		wordTracker = new ArrayList<>();
		frequencies = new ArrayList<>();
		found = false;
	}

	private void clearData(){
		lists.clear();
		wordTracker.clear();
		found = false;
	}

	public ArrayList<WordFrequencyContainer> searchReports(String report) throws IOException, ParseException {

		frequencies.clear();

		int hitsPerPage = Integer.MAX_VALUE;

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		IndexSearcher searcher = new IndexSearcher(reader);

		Analyzer analyzer = new StandardAnalyzer(Constants.CUSTOM_STOP_WORDS_SET);


		MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(fields , analyzer);

		while (true) {

			String line = report;

			if (line == null || line.length() == -1) {
				break;
			}

			line = line.trim();
			if (line.length() == 0) {
				break;
			}

			Query query = multiFieldQueryParser.parse(MultiFieldQueryParser.escape(line));
			System.out.println("Searching for: " + query.toString());


			doPagingSearch( searcher, query, hitsPerPage, false);

			break;
		}
		reader.close();

		return frequencies;
	}


	private void doPagingSearch(IndexSearcher searcher, Query query,
	                            int hitsPerPage, boolean interactive) throws IOException {

		// Collect enough docs to show 5 pages
		TopDocs results = searcher.search(query, hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;

		int numTotalHits = results.totalHits;
		System.out.println(numTotalHits + " total matching documents");

		int start = 0;
		int end;

		while (true) {

			end = Math.min(hits.length, start + hitsPerPage);

			for (int i = start; i < end; i++) {
				clearData();
				Document doc = searcher.doc(hits[i].doc);
				Explanation explanation = searcher.explain(query, hits[i].doc);
				getFrequencies(explanation);
				String name = doc.get(Constants.NAME);

				calculateWordDeveloperRelation(name);

				frequencies.add(new WordFrequencyContainer(name , lists));
				lists = new ArrayList<>();
			}

			if (!interactive || end == 0) {
				break;
			}

		}
	}

	private void calculateWordDeveloperRelation(String name) {
		lists.forEach(wordFrequencyDetails -> {
			String word = wordFrequencyDetails.getWord();
			if (wordDeveloperDetailsMap.containsKey(word)) {

				Map<String, float[]> devCount = wordDeveloperDetailsMap.get(word).getDevCount();
				if (devCount.containsKey(name)) {
					devCount.get(name)[0] += wordFrequencyDetails.getFrequency();
				} else {
					devCount.put(name, new float[1]);
					devCount.get(name)[0] += wordFrequencyDetails.getFrequency();
				}
			} else {
				wordDeveloperDetailsMap.put(word, new WordDeveloperDetails(word));
				Map<String, float[]> devCount = wordDeveloperDetailsMap.get(word).getDevCount();
				devCount.put(name, new float[1]);
				devCount.get(name)[0] += wordFrequencyDetails.getFrequency();
			}
		});
	}

	private void  getFrequencies(Explanation explanation) {

		if(!found && explanation.getDescription().contains("DefaultSimilarity")){

			if(!wordTracker.contains(explanation.getDescription())) {
				wordTracker.add(explanation.getDescription());
				found = true;

				String [] indexWordData  = parseExplanationDescription(explanation.getDescription());

				lists.add(new WordFrequencyDetails(indexWordData[0], indexWordData[1]));
			}
		}
		else if(found && explanation.getDescription().contains("termFreq")){

			lists.get(lists.size()-1).setFrequency(explanation.getValue());
			found = false;
		}

		Explanation[] details = explanation.getDetails();
		if (details != null) {
			for (Explanation detail : details) {
				getFrequencies(detail);
			}
		}
	}

	private String[] parseExplanationDescription(String des){
		String parsed = des.replaceAll("[()]", " ");
		String[] split = parsed.split(" ");
		split = split[1].split(":");
		return split;
	}

	public Map<String, WordDeveloperDetails> getWordDeveloperDetailsMap() {
		return wordDeveloperDetailsMap;
	}
}
