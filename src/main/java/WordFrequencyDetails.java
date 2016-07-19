/**
 * Created by peacefrog on 7/12/16.
 * Time 6:01 PM
 */
public class WordFrequencyDetails {

	private String indexName ;
	private String word ;
	private Float frequency;

	public WordFrequencyDetails(String indexName, String word, Float frequency) {
		this.indexName = indexName;
		this.word = word;
		this.frequency = frequency;
	}

	public WordFrequencyDetails(String word, float frequency) {
		this.word = word;
		this.frequency = frequency;
	}

	public WordFrequencyDetails(String indexName, String word) {
		this.indexName = indexName;
		this.word = word;
	}

	public String getWord() {
		return word;
	}

	public Float getFrequency() {
		return frequency;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public void setFrequency(Float frequency) {
		this.frequency = frequency;
	}

	@Override
	public String toString() {
		return "WordFrequencyDetails{" +
				"indexName='" + indexName + '\'' +
				", word='" + word + '\'' +
				", frequency=" + frequency +
				'}';
	}
}
