import java.util.Arrays;
import java.util.List;


public class PcexSet {
	
	private String activitySetId;
	private int numberOfChallenges;
	private List<String> activityIDs;
	
	public PcexSet(String activitySetId, int numberOfChallenges, String activityIDs) {
		this.activitySetId = activitySetId;
		this.numberOfChallenges = numberOfChallenges;
		this.activityIDs = Arrays.asList(activityIDs.split(","));
	}
	
	public String getActivitySetId() {
		return activitySetId;
	}

	public void setActivitySetId(String activitySetId) {
		this.activitySetId = activitySetId;
	}

	public int getNumberOfChallenges() {
		return numberOfChallenges;
	}

	public void setNumberOfChallenges(int numberOfChallenges) {
		this.numberOfChallenges = numberOfChallenges;
	}

	public List<String> getActivityIDs() {
		return activityIDs;
	}

}
