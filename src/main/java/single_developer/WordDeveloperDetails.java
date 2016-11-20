package single_developer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by peacefrog on 7/23/16.
 * Time 2:54 AM
 */
public class WordDeveloperDetails {
	private String word;
	private Map<String, float[]> devCount;

	public WordDeveloperDetails(String word) {
		this.word = word;

		devCount = new HashMap<>();
	}

	public Map<String, float[]> getDevCount() {
		return devCount;
	}
}
