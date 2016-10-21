/**
 * Created by peacefrog on 10/20/16.
 * Time 10:46 PM
 */
/*
    Author: Philip Chan

    Description:  Given a list of stop words and a plain-text document,
    output words that are removed and kept.

    Input files:
    a.  stop words each on one line
    b.  plain-text document with words and possible punctuations

    Output:

    print to the screen words that are stop words
    print words that are not stop words to RESULT_FNAME

*/

import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.util.*;
import java.util.regex.Pattern;
import java.io.*;


public class StopWords {
	// return true if word is in stopWords, which are in alphabetical order
	// use binary search
	public static Boolean isStopWord(String word, String[] stopWords) {
		// compareWords(word1, word2)
		// returns 0 if word1 and word2 are the same
		// negative if word1 is alphabetically before word2
		// positive if word1 is alphabetically before word2

		boolean found = false;   // word found in stopWords

		// ******** complete definition here *********


		return found;
	}


	// makes it easier for non-OO beginners
	public static int compareWords(String word1, String word2) {
		return word1.compareToIgnoreCase(word2);
	}


	private static String RESULT_FNAME = "result.txt";


	public static void main(String[] arg) {
//		Scanner keyboard = new Scanner(System.in);
//
//		// ask for the stop words file name and read in stop words
//		System.out.print("Please type the stop words file name: ");
//		String[] stopWords = readStopWords(keyboard.next());
//
//		// ask for the text file and remove stop words
//		System.out.print("Please type the text file name: ");
//		removeStopWords(keyboard.next(), stopWords);
		System.out.println(StandardAnalyzer.STOP_WORDS_SET);
		System.out.println(StopAnalyzer.ENGLISH_STOP_WORDS_SET);

		String s[] = {"a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours 	ourselves", "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves"};

		StringBuilder stringBuilder = new StringBuilder("");

		for (int i = 0; i < s.length; i++) {
			stringBuilder.append("\"").append(s[i]).append("\",");

			if ((i + 1) % 5 == 0) stringBuilder.append("\n");
		}

		System.out.println(stringBuilder.toString());
	}

	// read stop words from the file and return an array of stop words
	public static String[] readStopWords(String stopWordsFilename) {
		String[] stopWords = null;

		try {
			Scanner stopWordsFile = new Scanner(new File(stopWordsFilename));
			int numStopWords = stopWordsFile.nextInt();
			stopWords = new String[numStopWords];
			for (int i = 0; i < numStopWords; i++)
				stopWords[i] = stopWordsFile.next();

			stopWordsFile.close();
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		return stopWords;
	}

	// for each word in the text, check if it is a stop word
	// if it is, print it; otherwise store it in a file
	public static void removeStopWords(String textFilename, String[] stopWords) {
		String word;
		try {
			Scanner textFile = new Scanner(new File(textFilename));
			textFile.useDelimiter(Pattern.compile("[ \n\r\t,.;:?!'\"]+"));

			PrintWriter outFile = new PrintWriter(new File(RESULT_FNAME));

			System.out.println("\nRemoving:");
			while (textFile.hasNext()) {
				word = textFile.next();
				if (isStopWord(word, stopWords))
					System.out.print(word + " ");
				else
					outFile.print(word + " ");
			}
			System.out.println("\n\nText after removing stop words is in " + RESULT_FNAME);
			outFile.println();

			textFile.close();
			outFile.close();
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}

	}

}