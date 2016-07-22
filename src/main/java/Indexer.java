import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by peacefrog on 7/2/16.
 * 4:12 PM
 */
public class Indexer {

	private List< BugReport> bugReports ;
	private Stemmer stemmer ;
	public Indexer(List<BugReport> bugReports) {
		this.bugReports = bugReports;
		stemmer = new Stemmer();
	}

	public Indexer() {
		stemmer = new Stemmer();
	}

	public void createIndex(){
		index(true);
	}

	public void index(boolean isNewIndex){
		String indexPath = Constants.INDEX;
		boolean create = isNewIndex;

		Date start = new Date();
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");

			Directory dir = FSDirectory.open(Paths.get(indexPath));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

			if (create) {
				// Create a new index in the directory, removing any
				// previously indexed documents:
				iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			} else {
				// Add new documents to an existing index:
				iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			}

			// Optional: for better indexing performance, if you
			// are indexing many documents, increase the RAM
			// buffer.  But if you do this, increase the max heap
			// size to the JVM (eg add -Xmx512m or -Xmx1g):
			//
			// iwc.setRAMBufferSizeMB(256.0);

			iwc.setRAMBufferSizeMB(1024.0);

			IndexWriter writer = new IndexWriter(dir, iwc);


			indexDocs(writer, bugReports);

			// NOTE: if you want to maximize search performance,
			// you can optionally call forceMerge here.  This can be
			// a terribly costly operation, so generally it's only
			// worth it when your index is relatively static (ie
			// you're done adding documents to it):
			//
			// writer.forceMerge(1);

			writer.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() +
					"\n with message: " + e.getMessage());
		}
	}

	static void indexDocs(final IndexWriter writer, List<BugReport> reports) throws IOException {

		for (BugReport report: reports) {
			indexDoc(writer , report);
		}

	}

	/** Indexes a single document */
	static void indexDoc(IndexWriter writer, BugReport report) throws IOException {
		// make a new, empty document
		Document doc = new Document();

		// Add the path of the file as a field named "path".  Use a
		// field that is indexed (i.e. searchable), but don't tokenize
		// the field into separate words and don't index term frequency
		// or positional information:
		Field pathField = new StringField("developer", report.getDeveloper(), Field.Store.YES);
		doc.add(pathField);

		// Add the last modified date of the file a field named "modified".
		// Use a LongField that is indexed (i.e. efficiently filterable with
		// NumericRangeFilter).  This indexes to milli-second resolution, which
		// is often too fine.  You could instead create a number based on
		// year/month/day/hour/minutes/seconds, down the resolution you require.
		// For example the long value 2011021714 would mean
		// February 17, 2011, 2-3 PM.
		doc.add(new Field(Constants.SUMMARY, Objects.equals(report.getSummary(), "") ? "null" : report.getSummary(), CosineDocumentSimilarity.TYPE_STORED));

		// Add the contents of the file to a field named "contents".  Specify a Reader,
		// so that the text of the file is tokenized and indexed, but not stored.
		// Note that FileReader expects the file to be in UTF-8 encoding.
		// If that's not the case searching for special characters will fail.

		doc.add(new Field(Constants.DESCRIPTION, Objects.equals(report.getDescription(), "") ?"null": report.getDescription() , CosineDocumentSimilarity.TYPE_STORED));

		if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
			// New index, so we just add the document (no old document can be there):
			System.out.println("adding " + report.getSummary());
			writer.addDocument(doc);
		} else {
			// Existing index (an old copy of this document may have been indexed) so
			// we use updateDocument instead to replace the old one matching the exact
			// path, if present:
			System.out.println("updating " + report.getSummary());
			writer.updateDocument(new Term("summary", report.getSummary()), doc);
		}
	}

	public Directory createTestIndex(String  summary , String desc) throws IOException {
		Directory directory = new RAMDirectory();
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(	analyzer);
		iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		iwc.setRAMBufferSizeMB(1024.0);

		IndexWriter writer = new IndexWriter(directory, iwc);

		addDocument(writer, summary , desc);
		writer.close();
		return directory;
	}

	void addDocument(IndexWriter writer, String summary , String  desc) throws IOException {
		Document doc = new Document();
		doc.add(new Field(Constants.SUMMARY, summary, CosineDocumentSimilarity.TYPE_STORED));
		doc.add(new Field(Constants.DESCRIPTION, desc, CosineDocumentSimilarity.TYPE_STORED));

		writer.addDocument(doc);
	}
}

