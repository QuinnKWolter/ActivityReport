import java.io.IOException;
import java.io.PrintWriter;
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


/**
 * Servlet implementation class ActivityReport
 */
@WebServlet("/ActivityReport")
public class RawActivity extends HttpServlet {

	private static final long serialVersionUID = 1L;

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
		String excludeApp = request.getParameter("excludeApp");
		if (excludeApp != null)
			excludedAppIds = Arrays.asList(excludeApp.split("\\s*[,\t]+\\s*"));
		
		ArrayList<String> non_students = new ArrayList<String>(Common.non_students);
		
		String jsonOutput = request.getParameter("jsonOutput");
		boolean isJsonOutput = (jsonOutput != null && jsonOutput.equalsIgnoreCase("yes"));
		
		String removeUsers = request.getParameter("removeUsr");
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
