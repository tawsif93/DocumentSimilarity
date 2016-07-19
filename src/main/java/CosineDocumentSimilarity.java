
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class CosineDocumentSimilarity {

	private final Set<String> terms = new HashSet<>();
	private  RealVector indexSummaryVector;
	private  RealVector indexDescriptionVector;
	private  RealVector testSummaryVector;
	private  RealVector testDescriptionVector;

	private IndexReader testDirectoryReader;
	private IndexReader indexDirectoryReader;

	public CosineDocumentSimilarity(Directory directory) throws IOException {

		testDirectoryReader = DirectoryReader.open(directory);
		indexDirectoryReader = DirectoryReader.open(FSDirectory.open(Paths.get(Constants.INDEX)));

	}

	public List<Developer> getSimilarities(){
		try {
			return createDocumentVectors(testDirectoryReader , indexDirectoryReader);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private List<Developer> createDocumentVectors(IndexReader test , IndexReader indexed) throws IOException {
		Map<String, Integer> testSummaryFrequencies;
		Map<String, Integer> testDescriptionFrequencies;
		Map<String, Integer> indexSummaryFrequencies;
		Map<String, Integer> indexDescriptionFrequencies;


		List<Developer> similarity = new ArrayList<>();

//		System.out.println(test.document(0).get(SUMMARY));
//		System.out.println(test.document(0).get(DESCRIPTION));
//
//		System.out.println(indexed.document(0).get(SUMMARY));
//		System.out.println(indexed.document(0).get(DESCRIPTION));

		for (int i=0; i<indexed.maxDoc(); i++) {
			Document doc = indexed.document(i);

//			System.out.println("*****************----------------*****************");
			String developer = doc.get("developer");
//			System.out.println(developer);
//			System.out.println(doc.get(SUMMARY));
//			System.out.println(doc.get(DESCRIPTION));
			terms.clear();
			indexSummaryFrequencies = getTermFrequencies(indexed, i, Constants.SUMMARY);
			testSummaryFrequencies = getTermFrequencies(test, 0, Constants.SUMMARY);

			testSummaryVector = toRealVector(testSummaryFrequencies);
			indexSummaryVector = toRealVector(indexSummaryFrequencies);
			Double summarySimilarity = getSummaryCosineSimilarity();

			terms.clear();

			testDescriptionFrequencies = getTermFrequencies(test, 0, Constants.DESCRIPTION);
			indexDescriptionFrequencies = getTermFrequencies(indexed, i, Constants.DESCRIPTION);

			testDescriptionVector = toRealVector(testDescriptionFrequencies);
			indexDescriptionVector = toRealVector(indexDescriptionFrequencies);

			Double descriptionSimilarity = getDescriptionCosineSimilarity();

			similarity.add(new Developer(developer, summarySimilarity , descriptionSimilarity));
		}

		return similarity;
	}


//
//	CosineDocumentSimilarity(String s1, String s2) throws IOException {
////		Edited BY TAWSIF
//
//
////		Directory directory = createIndex(s1, s2);
////		IndexReader reader = DirectoryReader.open(directory);
//		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index")));
//
//		Map<String, Integer> f1 = getTermFrequencies(reader, 0);
//		Map<String, Integer> f2 = getTermFrequencies(reader, 1);
//		reader.close();
//
//		f1.keySet().forEach(System.out::println);
//		System.out.println();
//		f2.keySet().forEach(System.out::println);
//		indexSummaryVector = toRealVector(f1);
//		testSummaryVector = toRealVector(f2);
//	}

	Directory createIndex(String s1, String s2) throws IOException {
		Directory directory = new RAMDirectory();
		Analyzer analyzer = new SimpleAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(	analyzer);
		IndexWriter writer = new IndexWriter(directory, iwc);
		addDocument(writer, s1);
		addDocument(writer, s2);
		writer.close();
		return directory;
	}

	/* Indexed, tokenized, stored. */
	public static final FieldType TYPE_STORED = new FieldType();

	static {
		TYPE_STORED.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
		TYPE_STORED.setTokenized(true);
		TYPE_STORED.setStored(true);
		TYPE_STORED.setStoreTermVectors(true);
		TYPE_STORED.setStoreTermVectorPositions(true);
		TYPE_STORED.freeze();
	}

	void addDocument(IndexWriter writer, String content) throws IOException {
		Document doc = new Document();
		Field field = new Field("", content, TYPE_STORED);
		doc.add(field);
		writer.addDocument(doc);
	}

	private double getSummaryCosineSimilarity() {
		return (indexSummaryVector.dotProduct(testSummaryVector)) / (indexSummaryVector.getNorm() * testSummaryVector.getNorm());
	}
	private double getDescriptionCosineSimilarity() {
		return (indexDescriptionVector.dotProduct(testDescriptionVector)) / (indexDescriptionVector.getNorm() * testDescriptionVector.getNorm());
	}

//	public static double getSummaryCosineSimilarity(String s1, String s2)
//			throws IOException {
//		return new CosineDocumentSimilarity(s1, s2).getSummaryCosineSimilarity();
//	}

	private Map<String, Integer> getTermFrequencies(IndexReader reader, int docId, String indexContent)
			throws IOException {
//		System.out.println(reade);
		Terms vector = reader.getTermVector(docId, indexContent);
		TermsEnum termsEnum = null;
		termsEnum = vector.iterator(termsEnum);
		Map<String, Integer> frequencies = new HashMap<>();
		BytesRef text = null;
		while ((text = termsEnum.next()) != null) {
			String term = text.utf8ToString();
			int freq = (int) termsEnum.totalTermFreq();
			frequencies.put(term, freq);
			terms.add(term);
		}
		return frequencies;
	}

	RealVector toRealVector(Map<String, Integer> map) {
		RealVector vector = new ArrayRealVector(terms.size());
		int i = 0;
		for (String term : terms) {
			int value = map.containsKey(term) ? map.get(term) : 0;
			vector.setEntry(i++, value);
		}
		return (RealVector) vector.mapDivide(vector.getL1Norm());
	}

	public static void main(String[] args) throws IOException {

//		System.out.println(getSummaryCosineSimilarity("Software Engineering IIT search" ,"search"));
	}
}