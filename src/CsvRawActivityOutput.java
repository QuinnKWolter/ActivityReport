public class CsvRawActivityOutput implements RawActivityOutput {
	
	private RawActivityRequestParameters params;
	private String delimiter;
	private StringBuilder outputBuilder;
	private String group;
	
	public CsvRawActivityOutput() {
		this.outputBuilder = new StringBuilder();
	}

	@Override
	public void init(RawActivityRequestParameters params) {
		this.params = params;
		this.delimiter = params.delimiter;
		
	}
	
	@Override
	public void initGroup(GroupActivity groupActivity) {
		group = groupActivity.getGroupId();
	}
	
	@Override
	public void endGroup() {
	}

	@Override
	public void processRawActivity(LoggedActivity activity, User user) {
		outputBuilder.append(user.getUserLogin() + delimiter + group + delimiter
				+ activity.getSession() + delimiter + activity.getSessionActNo()
				+ delimiter);
		outputBuilder.append(activity.getAppId() + delimiter + activity.getLabel() + delimiter);
		outputBuilder.append(// a.getActivityId() + delimiter +
				activity.getActivityName() + delimiter + activity.getTargetName() + delimiter
				+ activity.getParentName() + delimiter + activity.getTopicName()
				+ delimiter + activity.getActivityCourseOrder() + delimiter + activity.getTopicOrder()
				+ delimiter + activity.getAttemptNo() + delimiter + activity.getResult()
				+ delimiter);
		outputBuilder.append(activity.getDateStr().toString() + delimiter + activity.getUnixTimestamp()  + delimiter);
		outputBuilder.append(Common.df.format(activity.getTime()));
		outputBuilder.append((params.incTimeLabel ? delimiter + activity.getLabelTime() : ""));
		outputBuilder.append((params.incsvc ? delimiter + Common.replaceNewLines(activity.getSvc()) : "")
				+ (params.incallparameters ? delimiter + "\"" + Common.replaceNewLines(activity.getAllParameters())
						+ "\"" : "") + "\n");
	}

	@Override
	public String getOutput() {
		return this.outputBuilder.toString();
	}

	@Override
	public String getHeader() {
		return "user" + delimiter + "group" + delimiter + "session" + delimiter + "timebin" + delimiter + 
				"appid" + delimiter + "applabel" + delimiter +
				"activityname" + delimiter + "targetname" + delimiter
					+ "parentname" + delimiter + "topicname" + delimiter 
					+ "courseorder" + delimiter + "topicorder" + delimiter 
					+ "attemptno" + delimiter + "result" + delimiter +
					"datestring" + delimiter + "unixtimestamp" + delimiter + "durationseconds" +
					(params.incTimeLabel ? delimiter + "timelabel" : "") + 
					(params.incsvc ? delimiter + "svc" : "") + 
					(params.incallparameters ? delimiter + "allparameters" : "") + "\n";
	}

}
