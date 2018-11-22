import java.util.ArrayList;
import java.util.HashMap;

public class Activity {
	private String activityName;
	private ArrayList<String> topics;
	private String providerId;
	private int orderInCourse;
	private int orderInTopic;
	private int topicOrderInCourse;
	
	public Activity(String name, String topic, String provider, int orderInCourse, int orderInTopic, int topicOrderInCourse){
		activityName = name;
		topics = new ArrayList<String>();
		topics.add(topic);
		providerId = provider;
		this.orderInCourse = orderInCourse; 
		this.orderInTopic = orderInTopic;
		this.topicOrderInCourse = topicOrderInCourse;
	}

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public ArrayList<String> getTopics() {
		return topics;
	}

	public void setTopics(ArrayList<String> topics) {
		this.topics = topics;
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public int getOrderInCourse() {
		return orderInCourse;
	}

	public void setOrderInCourse(int orderInCourse) {
		this.orderInCourse = orderInCourse;
	}

	public int getOrderInTopic() {
		return orderInTopic;
	}

	public void setOrderInTopic(int orderInTopic) {
		this.orderInTopic = orderInTopic;
	}

	public int getTopicOrderInCourse() {
		return topicOrderInCourse;
	}

	public void setTopicOrderInCourse(int topicOrderInCourse) {
		this.topicOrderInCourse = topicOrderInCourse;
	}
	
	public String getFirstTopic(){
		if(topics != null&& topics.size()>0) return topics.get(0);
		else return "unknown";
	}
	
	

}
