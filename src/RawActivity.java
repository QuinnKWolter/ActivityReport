//http://localhost:8080/ActivityReport/RawActivity?grp=IS172013Fall&header=yes
//MG:http://adapt2.sis.pitt.edu/ActivityReport/RawActivity?header=yes&svc=no&grp=CSC1310_G1,CSC1310_G2,IS17Fall20141,IS17Fall20141,IS1022Fall2014,IS2710Fall20141,IS2710Fall20142,ASUFALL2014,IS10222014Sprg,IS172014Spring,BENG11_TAZ_2014,BENG12_TAZ_2014,WSSU_JAVAF2013,WSSU_JAVAF2013B,IS172013Fall,IS10222013Fall,IS27102013Fall,MIS333_2014_1,STUDY2013_A,STUDY2013_B,STUDY2013_C
//PG:http://adapt2.sis.pitt.edu/ActivityReport/RawActivity?header=yes&svc=no&grp=IS172013Spring,IS172012Fall,IS172012Spring,IS172011Fall,IS172011Spring,IS172010Fall
//MG+PG: http://adapt2.sis.pitt.edu/ActivityReport/RawActivity?header=yes&svc=no&grp=CSC1310_G1,CSC1310_G2,IS17Fall20141,IS17Fall20141,IS1022Fall2014,IS2710Fall20141,IS2710Fall20142,ASUFALL2014,IS10222014Sprg,IS172014Spring,BENG11_TAZ_2014,BENG12_TAZ_2014,WSSU_JAVAF2013,WSSU_JAVAF2013B,IS172013Fall,IS10222013Fall,IS27102013Fall,MIS333_2014_1,STUDY2013_A,STUDY2013_B,STUDY2013_C,IS172013Spring,IS172012Fall,IS172012Spring,IS172011Fall,IS172011Spring,IS172010Fall
//problemastic PG group: http://localhost:8080/ActivityReport/RawActivity?header=yes&svc=no&grp=IS172011Spring

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

//import java.text.SimpleDateFormat;

/**
 * Servlet implementation class ActivityReport
 */
@WebServlet("/ActivityReport")
public class RawActivity extends HttpServlet {

	private static final long serialVersionUID = 1L;
//	public static DecimalFormat df = new DecimalFormat("#.##");

//	public static String mg_grps = "CSC1310_G1, CSC1310_G2, IS17Fall20141,IS17Fall20141,"
//			+ "IS1022Fall2014, IS2710Fall20141,IS2710Fall20142,"
//			+ "ASUFALL2014, IS10222014Sprg, IS172014Spring, BENG11_TAZ_2014, BENG12_TAZ_2014,"
//			+ "WSSU_JAVAF2013, WSSU_JAVAF2013B, IS172013Fall, IS10222013Fall, IS27102013Fall,"
//			+ "MIS333_2014_1,STUDY2013_A,STUDY2013_B,STUDY2013_C"; // 21
//
//	public static String progressor_grps = "IS172013Spring,IS172012Fall,IS172012Spring,"
//			+ "IS172011Fall,IS172011Spring,IS172010Fall"; // 6 groups, mapped to
//																										// progressor_plus
//
//	public static String progressor_grps_map = "progressor_plus";
//
//	// Caution: the later query also remove people from admin group (GroupId = 68)
//	public static ArrayList<String> non_students = new ArrayList<String>(
//			Arrays.asList("anonymous_user", "fedor.bakalov", "nkresl", "maccloud",
//					"moeslein", "mliang", "pjcst19", "fseels", "r.hosseini", "ltaylor",
//					"peterb", "shoha99", "jennifer", "dguerra"));
//
//	// Caution: in the latter Query also exclude those containing TEST(test) in
//	// the String
//	public static ArrayList<String> non_sessions = new ArrayList<String>(
//			Arrays.asList("null", "undefined", "xxx", "aaaaa", "bbbbb", "fffff",
//					"XXXX", "xxxx", "XXXXX", "xxxxx", "xxxyyy", "YYYYY", "YYYY"));
//
//	public static HashMap<Integer, String> APP_MAP;
//	static {
//		APP_MAP = new HashMap<Integer, String>();
//		APP_MAP.put(2, "QUIZPACK");
//		APP_MAP.put(3, "WEBEX");
//		APP_MAP.put(5, "KNOWLEDGE_SEA");
//		APP_MAP.put(8, "KT");
//		APP_MAP.put(20, "QUIZGUIDE");
//		APP_MAP.put(23, "SQLKNOT");
//		APP_MAP.put(25, "QUIZJET");
//		APP_MAP.put(35, "ANIMATED_EXAMPLE");
//		APP_MAP.put(-1, "MASTERY_GRIDS");
//	}
//	public static HashMap<String, Integer> MG_ACTIVITYID_MAP;
//	static {
//		MG_ACTIVITYID_MAP = new HashMap<String, Integer>();
//		MG_ACTIVITYID_MAP.put("", 990000001);
//		MG_ACTIVITYID_MAP.put("app-start", 990000002);
//		MG_ACTIVITYID_MAP.put("data-load-start", 990000003);
//		MG_ACTIVITYID_MAP.put("data-load-end", 990000004);
//		MG_ACTIVITYID_MAP.put("app-ready", 990000005);
//		MG_ACTIVITYID_MAP.put("group-set", 990000006);
//		MG_ACTIVITYID_MAP.put("grid-activity-cell-select", 990000007);
//		MG_ACTIVITYID_MAP.put("activity-open", 990000008);
//		MG_ACTIVITYID_MAP.put("activity-reload", 990000009);
//		MG_ACTIVITYID_MAP.put("activity-done", 990000010);
//		MG_ACTIVITYID_MAP.put("activity-close", 990000011);
//		MG_ACTIVITYID_MAP.put("activity-load-recommended", 990000012);
//		MG_ACTIVITYID_MAP.put("activity-load-original", 990000013);
//		MG_ACTIVITYID_MAP.put("load-others-list", 990000014);
//		MG_ACTIVITYID_MAP.put("resource-set", 990000015);
//		MG_ACTIVITYID_MAP.put("activity-feedback-set-difficulty", 990000016);
//		MG_ACTIVITYID_MAP.put("grid-topic-cell-select", 990000017); // cell-topic-id
//		MG_ACTIVITYID_MAP.put("comparison-mode-set", 990000018);
//
//	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RawActivity() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		boolean error = false;

		String errorMsg = "";
		ConfigManager cm = new ConfigManager(this); // this object gets the database
		// connections values
		PrintWriter out = response.getWriter();
		String groupId = request.getParameter("grp"); // group id
		// String groups = request.getParameter("grps");
		String[] groupIds = null;
		if (groupId != null)
			groupIds = groupId.split("\\s*[,\t]+\\s*");
		
		String fileName = request.getParameter("filename");
		if (fileName == null) fileName = groupId.replaceAll(",", "_") + "_" + "raw_activity.txt";
		
		response.setContentType("text/plain");
		response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
		
		String header = request.getParameter("header"); // include or not the header
		boolean incHeader = (header != null && header.equalsIgnoreCase("yes"));

		boolean replaceExtTimes = (request.getParameter("replaceexttimes") != null);
		
		String svc = request.getParameter("svc");
		boolean incsvc = (svc != null && svc.equalsIgnoreCase("yes"));
		
		String timeLabels = request.getParameter("timelabels");
		boolean incTimeLabel = (timeLabels != null);
		
		boolean sessionate = (request.getParameter("sessionate") != null);
		int minThreshold = 90;
		if(sessionate) minThreshold = 90;
		try{minThreshold = Integer.parseInt(request.getParameter("minthreshold"));}catch(Exception e){minThreshold = 90;}
		
		List<String> excludedAppIds = new ArrayList<String>();
		String excludeApp = request.getParameter("excludeApp"); // group id
		if (excludeApp != null)
			excludedAppIds = Arrays.asList(excludeApp.split("\\s*[,\t]+\\s*"));
		
		ArrayList<String> non_students = new ArrayList<String>(Common.non_students);
		
		String jsonOutput = request.getParameter("jsonOutput");
		boolean isJsonOutput = (jsonOutput != null && jsonOutput.equalsIgnoreCase("yes"));
		
		String removeUsers = request.getParameter("removeUsr"); // group id
		String[] remove = null;
		if (removeUsers != null) {
			remove = removeUsers.split("\\s*[,\t]+\\s*");
			non_students.addAll(Arrays.asList(remove));
		}
		String delimiter = request.getParameter("delimiter");
		String allparameters = request.getParameter("allparameters");
		boolean incallparameters = (allparameters != null && allparameters
				.equalsIgnoreCase("yes"));
		// String pattern = "yyyy-MM-dd";
		// SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		String fromDate = request.getParameter("fromDate");
		String toDate = request.getParameter("toDate");
		String[] dateRange = new String[2];
		dateRange[0] = "";
		dateRange[1] = "";
		
		if (fromDate != null && fromDate.length() > 0)
			dateRange[0] = fromDate;// formatter.format(fromDate);
		if (toDate != null && toDate.length() > 0)
			dateRange[1] = toDate;// formatter.format(toDate);
		
		String queryArchiveParam = request.getParameter("queryArchive");
		if(queryArchiveParam == null) {
			queryArchiveParam = "yes";
		}
		boolean queryArchive = queryArchiveParam.equalsIgnoreCase("yes");
		
	
		String output = "";
		if (groupIds == null) {// && (groupId == null || groupId.length() < 1)) {
			error = true;
			errorMsg = "group identifier not provided or invalid";
		}
		else {
			// if (groupIds == null) {
			// groupIds = new String[1];
			// groupIds[0] = groupId;
			// }
			boolean printedHeader = false;
			JSONObject rawActivityJSON = new JSONObject();
			JSONArray groupsJSONArray = new JSONArray();
			StringBuilder outputBuilder = new StringBuilder();
						
			for (String group : groupIds) {
				JSONObject groupJSON = new JSONObject();
				JSONArray groupActivityJSONArray = new JSONArray();
				
				String topicSource = "UNKNOWN";
				if (Common.mg_grps.contains(group))
					topicSource = "MG";
				else if (Common.progressor_grps.contains(group))
					topicSource = "PR";

				GroupActivity groupActivity = new GroupActivity(group, topicSource,
						non_students, Common.non_sessions, false, cm, dateRange, queryArchive, sessionate, minThreshold, null);
				if (delimiter == null || delimiter.equals(""))
					delimiter = cm.delimiter;
				// @@@@
				if (incTimeLabel){
					String[] labels = timeLabels.split(","); 
					if(labels == null || labels.length<2) {
						labels = new String[]{"short","long"};
					}
					Labeller labeller = new Labeller(groupActivity,labels);
					labeller.labelTime(replaceExtTimes);
					//groupActivity.labelTime(labels);
				}
				
				if (incHeader && !printedHeader) {
					printedHeader = true;
					outputBuilder.append("user" + delimiter + "group" + delimiter + "session" + delimiter + "timebin" + delimiter)
								 .append("appid" + delimiter + "applabel" + delimiter)
								 .append("activityname" + delimiter + "targetname" + delimiter
											+ "parentname" + delimiter + "topicname" + delimiter 
											+ "courseorder" + delimiter + "topicorder" + delimiter 
											+ "attemptno" + delimiter + "result" + delimiter)
								 .append("datestring" + delimiter + "unixtimestamp" + delimiter + "durationseconds")
								 .append((incTimeLabel ? delimiter + "timelabel" : ""))
								 .append((incsvc ? delimiter + "svc" : "") + (incallparameters ? delimiter + "allparameters" : "") + "\n");
				}

				if (!groupActivity.isThereActivity()) {
					error = true;
					outputBuilder.append("no activity found");
				}
				else {
					String userName = "";
					String session = "";
					HashMap<String, User> grp_activity = groupActivity.getGrpActivity();
					for (Map.Entry<String, User> entry : grp_activity.entrySet()) {
						User user = entry.getValue();
						userName = user.getUserLogin();
						
						for (LoggedActivity a : user.getActivity()) {
							if(excludedAppIds.contains(Integer.toString(a.getAppId())) == false) {
								// System.out.println(userName + "," + a.getAllParameters());
								outputBuilder.append(userName + delimiter + group + delimiter
										+ a.getSession() + delimiter + a.getSessionActNo()
										+ delimiter);
								outputBuilder.append(a.getAppId() + delimiter + a.getLabel() + delimiter);
								outputBuilder.append(// a.getActivityId() + delimiter +
								a.getActivityName() + delimiter + a.getTargetName() + delimiter
										+ a.getParentName() + delimiter + a.getTopicName()
										+ delimiter + a.getActivityCourseOrder() + delimiter + a.getTopicOrder()
										+ delimiter + a.getAttemptNo() + delimiter + a.getResult()
										+ delimiter);
								outputBuilder.append(a.getDateStr().toString() + delimiter + a.getUnixTimestamp()  + delimiter);
								outputBuilder.append(Common.df.format(a.getTime()));
								outputBuilder.append((incTimeLabel ? delimiter + a.getLabelTime() : ""));
								outputBuilder.append((incsvc ? delimiter + Common.replaceNewLines(a.getSvc()) : "")
										+ (incallparameters ? delimiter + "\"" + Common.replaceNewLines(a.getAllParameters())
												+ "\"" : "") + "\n");
								
								JSONObject studentJSON = new JSONObject();
								try {
									studentJSON.put("user", userName);
									studentJSON.put("group", group);
									studentJSON.put("session", a.getSession());
									studentJSON.put("timebin", a.getSessionActNo());
									studentJSON.put("appid", a.getAppId());
									studentJSON.put("applabel", a.getLabel());
									studentJSON.put("activityname", a.getActivityName());
									studentJSON.put("targetname", a.getTargetName());
									studentJSON.put("parentname", a.getParentName());
									studentJSON.put("topicname",  a.getTopicName());
									studentJSON.put("courseorder", a.getActivityCourseOrder());
									studentJSON.put("topicorder", a.getTopicOrder());
									studentJSON.put("attemptno", a.getAttemptNo());
									studentJSON.put("result", a.getResult());
									studentJSON.put("datestring", a.getDateStr().toString());
									studentJSON.put("unixtimestamp", a.getUnixTimestamp());
									studentJSON.put("durationseconds", Common.df.format(a.getTime()));
									
									if(incsvc) {
										studentJSON.put("svc", Common.replaceNewLines(a.getSvc()));
									}
									
									if(incallparameters) {
										studentJSON.put("allparameters", Common.replaceNewLines(a.getAllParameters()));
									}
								} catch (Exception e) {
									e.printStackTrace();
								}

								groupActivityJSONArray.put(studentJSON);
							}
							
						}
					}
				}
				try {
					groupJSON.put(group, groupActivityJSONArray);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				groupsJSONArray.put(groupJSON);
			}
			
			try {
				rawActivityJSON.put("groups", groupsJSONArray);
				
				if(isJsonOutput) {
					out.print(rawActivityJSON.toString(4));
				} else {
					out.print(outputBuilder.toString());
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
