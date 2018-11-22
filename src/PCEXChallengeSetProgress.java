import java.util.List;


public class PCEXChallengeSetProgress {
	
	private String setID;
	private int numberOfChallenges;
	private List<String> challengeIDList;
	private boolean[] progress;
	
	public PCEXChallengeSetProgress(PcexSet pcexset) {
		this(pcexset.getActivitySetId(), pcexset.getNumberOfChallenges(), pcexset.getActivityIDs());
	}
	
	public PCEXChallengeSetProgress(String setID, int numberOfChallenges, List<String> challengeIDList) {
		this.setID = setID;
		this.numberOfChallenges = numberOfChallenges;
		this.challengeIDList = challengeIDList;
		progress = new boolean[numberOfChallenges];
	}
	
	@Override
	public boolean equals(Object obj) {
		PCEXChallengeSetProgress other = (PCEXChallengeSetProgress)obj;
		
		return other.getSetID().equals(getSetID());
	}
	
	public boolean hasActivity(Integer activityID) {
		return challengeIDList.contains(activityID.toString());
	}
	
	public void markSolved(Integer activityID) {
		progress[challengeIDList.indexOf(activityID.toString())] = true;
	}
	
	public boolean isSetCompleted() {
		 for(boolean bool : progress) {
			 if(!bool) {
				 return false;
			 }
		 }
		 
	    return true;
	}
	

	public String getSetID() {
		return setID;
	}


	public void setSetID(String setID) {
		this.setID = setID;
	}


	public int getNumberOfChallenges() {
		return numberOfChallenges;
	}


	public void setNumberOfChallenges(int numberOfChallenges) {
		this.numberOfChallenges = numberOfChallenges;
	}

	public List<String> getChallengeIDList() {
		return challengeIDList;
	}

	public void setChallengeIDList(List<String> challengeIDList) {
		this.challengeIDList = challengeIDList;
	}

}
