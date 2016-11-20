package single_developer;

import org.tartarus.snowball.ext.EnglishStemmer;

/**
 * Created by peacefrog on 7/3/16.
 * 10:55 AM
 */
public class Stemmer {
//
//	private static single_developer.Stemmer stemmer= null;
//
//	public static single_developer.Stemmer getInstance() {
//
//		if(stemmer== null) stemmer = new single_developer.Stemmer();
//
//		return stemmer;
//	}

	public String englishStemer(String word){
		EnglishStemmer english = new EnglishStemmer();
		word = word.replaceAll("\n", " ").replaceAll("\r", " ");
		String[] words = word.split(" ");
		StringBuilder  builder = new StringBuilder("");
		for(int i = 0; i < words.length; i++){
			english.setCurrent(words[i].trim());
			english.stem();
//			System.out.println(english.getCurrent());
			if (!english.getCurrent().equals("")) builder.append(english.getCurrent()).append(" ");
		}

		return builder.toString();
	}
}
