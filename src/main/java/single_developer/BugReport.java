package single_developer;

/**
 * Created by peacefrog on 7/2/16.
 * 12:49 PM
 */
public class BugReport {
	private String developer ;
	private String summary;
	private String description;

	public BugReport(String developer, String summary, String description) {
		this.developer = developer;
		this.summary = summary;
		this.description = description;
	}

	public String getDeveloper() {
		return developer;
	}

	public void setDeveloper(String developer) {
		this.developer = developer;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
