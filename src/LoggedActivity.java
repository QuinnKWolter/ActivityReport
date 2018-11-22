import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class LoggedActivity implements Comparable<LoggedActivity> {
	//public static HashMap<Integer, String> APP_MAP = RawActivity.APP_MAP;
	//public static HashMap<String, Integer> MG_ACTIVITYID_MAP = RawActivity.MG_ACTIVITYID_MAP;
	
	private LogType logType;
	private int appId = -1;
	private String session = "NULL";
	private int activityId = -1; // the activityId as integer (id from UM2
																// database)
	private int parent = -1; // DEPRECATED
	private int sessionActNo = 0; // number of attempts for this activity so far
	private int attemptNo = 0; // number of attempts for this activity so far
	private String activityName = "";
	private String targetName = "";
	private String parentName = "";
	private String topicName = "";
	private double result = -1;
	private Date date;
	private Calendar cal;

	private String dateStr;
	private double time = -1; // time in seconds

	private long dateNS = -1;
	private String svc;
	private String allParameters;
	
	private String labelTime;

	private String actLabel;
	private String actLabelReadable;

	private int activityCourseOrder;
	private int topicOrder;
	private long unixTimestamp; 
	
	private double difficulty;
	
	public HashMap<String,String> params;
	
	

	public LoggedActivity(int appId, String session, int activityId,
			String activityName, String targetName, String parentName,
			String topicName, double result, String dateStr, long dateNS, String svc,
			int activityCourseOrder, int topicOrder, long unixTimestamp,
			String allParameters, LogType type) {
		super();
		this.appId = appId;
		this.session = session;
		this.activityId = activityId;
		this.activityName = activityName;
		this.targetName = targetName;
		this.parentName = parentName;
		this.topicName = topicName;
		this.result = result;
		this.activityCourseOrder = activityCourseOrder;
		this.topicOrder = topicOrder;
		this.unixTimestamp = unixTimestamp;
		this.logType = type;
		try {
			cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			cal.setTime(sdf.parse(dateStr));
			this.date = cal.getTime();
			// this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
			// .parse(dateStr);
		}
		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.date = null;
		}

		this.dateStr = dateStr;
		this.dateNS = dateNS;
		this.svc = svc;
		this.allParameters = allParameters;
		processAllParameters(); // this populates a hash map with all comma separated parameters in allParameters
		
		this.labelTime = "";
		this.actLabel = "";
		this.actLabelReadable = "";
		try{
			this.difficulty = (params.get("difficulty") == null ? -1 : Double.parseDouble(params.get("difficulty")));
		}catch(Exception e){
			this.difficulty = -1;
		}

	}

	
	public void processAllParameters() {
		this.params = new HashMap<String,String>();
		String[] paramPairs = allParameters.split(",");
		for (String param : paramPairs) {
			String[] pair = param.split(":");
			if (pair.length == 3)
				pair[1] = pair[1] + ":" + pair[2];
			if (pair != null && pair.length >= 2) {
				String p = pair[0];
				String v = pair[1];
				if (v == null)
					v = "";
				this.params.put(p,v);
			}
		}
	}

	
	// public LoggedActivity(int appId, String session, String label,
	// String dateStr, String allParameters) {
	// super();
	// this.appId = appId;
	// this.session = session;
	// this.label = label;
	//
	// try {
	// cal = Calendar.getInstance();
	// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	// cal.setTime(sdf.parse(dateStr));
	// this.date = cal.getTime();
	// // this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
	// // .parse(dateStr);
	// }
	// catch (ParseException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// this.date = null;
	// }
	// this.dateStr = dateStr;
	// this.allParameters = allParameters;
	// }

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public int getParent() {
		return parent;
	}

	public void setParent(int parent) {
		this.parent = parent;
	}

	public double getResult() {
		return result;
	}

	public void setResult(double result) {
		this.result = result;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDateStr() {
		return dateStr;
	}

	public void setDateStr(String dateStr) {
		this.dateStr = dateStr;
	}

	public long getDateNS() {
		return dateNS;
	}

	public void setDateNS(long dateNS) {
		this.dateNS = dateNS;
	}

	public String getSvc() {
		return svc;
	}

	public void setSvc(String svc) {
		this.svc = svc;
	}

	public String getAllParameters() {
		return allParameters;
	}

	public void setAllParameters(String allParameters) {
		this.allParameters = allParameters;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public String getLabel() {
		String res = Common.APP_MAP.get(appId);
		if (res == null)
			res = "OTHER";
		return res;
	}

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public int getAttemptNo() {
		return attemptNo;
	}

	public void setAttemptNo(int attemptNo) {
		this.attemptNo = attemptNo;
	}

	public int getSessionActNo() {
		return sessionActNo;
	}

	public void setSessionActNo(int sessionActNo) {
		this.sessionActNo = sessionActNo;
	}
	
	public String getLabelTime() {
		return labelTime;
	}

	public void setLabelTime(String labelTime) {
		this.labelTime = labelTime;
	}
	
	public String getActLabel() {
		return actLabel;
	}

	public void setActLabel(String actLabel) {
		this.actLabel = actLabel;
	}

	public String getActLabelReadable() {
		return actLabelReadable;
	}

	public void setActLabelReadable(String actLabelReadable) {
		this.actLabelReadable = actLabelReadable;
	}

	public int compareTo(LoggedActivity a) {
		// ascending order
		if (this.cal.after(a.cal))
			return 1;
		else if (this.cal.equals(a.cal))
			return 0;
		else
			return -1;
		// return (this.cal.after(a.cal) ? 1 : -1);
		// descending order
		// return compareQuantity - this.quantity;

	}

	public int getActivityCourseOrder() {
		return activityCourseOrder;
	}

	public void setActivityCourseOrder(int activityCourseOrder) {
		this.activityCourseOrder = activityCourseOrder;
	}

	public int getTopicOrder() {
		return topicOrder;
	}

	public void setTopicOrder(int topicOrder) {
		this.topicOrder = topicOrder;
	}

	public long getUnixTimestamp() {
		return unixTimestamp;
	}

	public void setUnixTimestamp(long unixTimestamp) {
		this.unixTimestamp = unixTimestamp;
	}


	public double getDifficulty() {
		return difficulty;
	}


	public void setDifficulty(double difficulty) {
		this.difficulty = difficulty;
	}


	public LogType getLogType() {
		return logType;
	}


	public void setLogType(LogType logType) {
		this.logType = logType;
	}
	
	
}

enum LogType {
	AGGREGATE,
	UM,
	PCEX, 
	PCEX_CONTROL
}