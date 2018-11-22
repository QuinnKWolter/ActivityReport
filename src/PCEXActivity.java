
public class PCEXActivity extends Activity {
	
	private String setActName;
	private boolean isChallenge;

		
	public PCEXActivity(String actName, boolean isChallenge, String content, String topic, String providerId, int contentOrder, int orderInTopic, int topicOrder) {
		super(content, topic, providerId, contentOrder, orderInTopic, topicOrder);
		this.isChallenge = isChallenge;
		this.setActName = actName;
	}

	public boolean isChallenge() {
		return isChallenge;
	}

	public void setChallenge(boolean isChallenge) {
		this.isChallenge = isChallenge;
	}

	public String getSetActName() {
		return setActName;
	}

	public void setSetActName(String setActName) {
		this.setActName = setActName;
	}

}
