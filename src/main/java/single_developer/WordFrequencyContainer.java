package single_developer;

import java.util.ArrayList;

/**
 * Created by peacefrog on 7/13/16.
 * Time 10:23 PM
 */
public class WordFrequencyContainer {
	private String developerName ;
	private ArrayList<WordFrequencyDetails> details ;

	public WordFrequencyContainer(String developerName, ArrayList<WordFrequencyDetails> details) {
		this.developerName = developerName;
		this.details = details;
	}

	public String getDeveloperName() {
		return developerName;
	}

	public ArrayList<WordFrequencyDetails> getDetails() {
		return details;
	}
}
