package multiple_developer;

import java.util.ArrayList;

/**
 * Created by peacefrog on 11/18/16.
 * Time 11:45 PM
 */
public class Bug {
	private String bugID;
	private int developerRepositoryIndex;
	private String shortDecription;
	private String longDecription;
	private ArrayList<Comment> comments;
	private ArrayList<Integer> connectedDevelopersRepositoryIndex;

	public Bug(String bugID, String shortDecription, String longDecription, ArrayList<Comment> comments) {
		this.bugID = bugID;
		this.shortDecription = shortDecription;
		this.longDecription = longDecription;
		this.comments = comments;
	}
}
