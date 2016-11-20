package multiple_developer;

/**
 * Created by peacefrog on 11/20/16.
 * Time 12:45 PM
 */
public class NetworkLink {

	NetworkNode firstNode;
	NetworkNode secondNode;
	int relationType;

	public NetworkLink(NetworkNode firstNode, NetworkNode secondNode, int relationType) {
		this.firstNode = firstNode;
		this.secondNode = secondNode;
		this.relationType = relationType;
	}
}
