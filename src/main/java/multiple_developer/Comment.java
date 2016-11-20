package multiple_developer;

/**
 * Created by peacefrog on 11/18/16.
 * Time 11:46 PM
 */
public class Comment {
	private String commentID;
	private String commentCount;
	private String text;
	private String commentTime;
	private String developerRepositoryIndex;

	/**
	 * @param text
	 * @param developerRepositoryIndex
	 * @deprecated Developer Index is not used inside the current solution
	 */
	public Comment(String text, String developerRepositoryIndex) {
		this.text = text;
		this.developerRepositoryIndex = developerRepositoryIndex;
	}

	/**
	 * @param text
	 * @param commentTime
	 * @param developerRepositoryIndex
	 * @deprecated Developer Index is not used inside the current solution
	 */
	public Comment(String text, String commentTime, String developerRepositoryIndex) {
		this.text = text;
		this.commentTime = commentTime;
		this.developerRepositoryIndex = developerRepositoryIndex;
	}

	public Comment(String commentID, String commentCount, String commentTime, String text) {
		this.commentID = commentID;
		this.commentCount = commentCount;
		this.text = text;
		this.commentTime = commentTime;
	}

	public String getCommentID() {
		return commentID;
	}

	public void setCommentID(String commentID) {
		this.commentID = commentID;
	}

	public String getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(String commentCount) {
		this.commentCount = commentCount;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getCommentTime() {
		return commentTime;
	}

	public void setCommentTime(String commentTime) {
		this.commentTime = commentTime;
	}

	public String getDeveloperRepositoryIndex() {
		return developerRepositoryIndex;
	}

	public void setDeveloperRepositoryIndex(String developerRepositoryIndex) {
		this.developerRepositoryIndex = developerRepositoryIndex;
	}
}
