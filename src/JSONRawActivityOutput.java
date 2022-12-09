import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class JSONRawActivityOutput implements RawActivityOutput {
	
	private JSONObject rawActivityJSON;
	private JSONArray groupsJSONArray;
	private RawActivityRequestParameters params;
	private JSONArray groupActivityJSONArray;
	private String group;
	
	public JSONRawActivityOutput() {
		this.rawActivityJSON = new JSONObject();
		this.groupsJSONArray = new JSONArray();
	}
	

	@Override
	public void init(RawActivityRequestParameters params) {
		this.params = params;
	}
	
	@Override
	public void initGroup(GroupActivity groupActivity) {
		group = groupActivity.getGroupId();
		
		groupActivityJSONArray = new JSONArray();
	}
	
	@Override
	public void endGroup() {
		JSONObject groupJSON = new JSONObject();
		try {
			groupJSON.put(group, groupActivityJSONArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		groupsJSONArray.put(groupJSON);
	}

	@Override
	public void processRawActivity(LoggedActivity activity, User user) {
		JSONObject studentJSON = new JSONObject();
		try {
			studentJSON.put("user", user.getUserLogin());
			studentJSON.put("group", group);
			studentJSON.put("session", activity.getSession());
			studentJSON.put("timebin", activity.getSessionActNo());
			studentJSON.put("appid", activity.getAppId());
			studentJSON.put("applabel", activity.getLabel());
			studentJSON.put("activityname",activity.getActivityName());
			studentJSON.put("targetname", activity.getTargetName());
			studentJSON.put("parentname", activity.getParentName());
			studentJSON.put("topicname",  activity.getTopicName());
			studentJSON.put("courseorder", activity.getActivityCourseOrder());
			studentJSON.put("topicorder", activity.getTopicOrder());
			studentJSON.put("attemptno", activity.getAttemptNo());
			studentJSON.put("result", activity.getResult());
			studentJSON.put("datestring", activity.getDateStr().toString());
			studentJSON.put("unixtimestamp", activity.getUnixTimestamp());
			studentJSON.put("durationseconds", Common.df.format(activity.getTime()));
			
			if(params.incsvc) {
				studentJSON.put("svc", Common.replaceNewLines(activity.getSvc()));
			}
			
			if(params.incallparameters) {
				studentJSON.put("allparameters", Common.replaceNewLines(activity.getAllParameters()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.groupActivityJSONArray.put(studentJSON);
	}

	@Override
	public String getOutput() {
		String output = "";
		
		try {
			rawActivityJSON.put("groups", groupsJSONArray);
			
			output =  rawActivityJSON.toString(4);
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		
		return output;
	}
	
	@Override
	public String getHeader() {
		return "";
	}

}
