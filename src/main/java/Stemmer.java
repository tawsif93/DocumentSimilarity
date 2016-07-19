import org.tartarus.snowball.ext.EnglishStemmer;

/**
 * Created by peacefrog on 7/3/16.
 * 10:55 AM
 */
public class Stemmer {
//
//	private static Stemmer stemmer= null;
//
//	public static Stemmer getInstance() {
//
//		if(stemmer== null) stemmer = new Stemmer();
//
//		return stemmer;
//	}

	public String englishStemer(String word){
		EnglishStemmer english = new EnglishStemmer();
		word = word.replaceAll("\n", "").replaceAll("\r", "");
		String[] words = word.split(" ");
		StringBuilder  builder = new StringBuilder("");
		for(int i = 0; i < words.length; i++){
			english.setCurrent(words[i]);
			english.stem();
//			System.out.println(english.getCurrent());
			builder.append(english.getCurrent()).append(" ");
		}

		return builder.toString();
	}
}
