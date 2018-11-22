import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class aggregateDBInterface extends dbInterface {
	public static DecimalFormat df4 = new DecimalFormat("#.####");

	public aggregateDBInterface(String connurl, String user, String pass) {
		super(connurl, user, pass);
	}

	public HashMap<String, User> getActivity(String grp,
			ArrayList<String> non_students, ArrayList<String> non_sessions,
			String[] dateRange) {
		String nonStudents = Common.csvFromArray(non_students);
		String nonSessions = Common.csvFromArray(non_sessions);
		try {
			HashMap<String, User> res = new HashMap<String, User>();
			stmt = conn.createStatement();
			String query = "SELECT user_id, action, session_id, datentime, UNIX_TIMESTAMP(datentime) as utimestamp FROM ent_tracking WHERE action NOT LIKE '%scroll%'";
			query += " AND session_id NOT LIKE '%test%' AND session_id NOT LIKE '%TEST%' "
					+ " AND group_id = '" + grp + "' ";
			if (nonStudents != null)
				query += " AND user_id not in (" + nonStudents + ") ";
			if (nonSessions != null)
				query += " AND session_id not in (" + nonSessions + ") ";
			if (dateRange[0].length() > 0)
				query += " AND datentime > '" + dateRange[0] + "' ";
			if (dateRange[1].length() > 0)
				query += " AND datentime < '" + dateRange[1] + "' ";
			query += "ORDER BY user_id , datentime ASC;";
			rs = stmt.executeQuery(query);
			System.out.println("AGGREGATE QUERY for getActivity():\n   " + query);
			// // String content_name = "";
			// // ArrayList<String[]> c_c = null;
			User currentUser = null;
			String login = null;
			int count = 0;
			long utimestamp = -1;
			while (rs.next()) {
				count++;
				login = rs.getString("user_id");
				utimestamp = rs.getLong("utimestamp");
				// first user in the logs
				if (currentUser == null)
					currentUser = new User(-1, login);
				// when detecting a new user, add the current user to 'res' and create
				// another user object
				if (!currentUser.getUserLogin().equals(login)) {
					res.put(currentUser.getUserLogin(), currentUser);
					currentUser = new User(-1, login);
				}

				// TODO: need to parse to get activity_id
				// LoggedActivity act = new LoggedActivity(-1,
				// rs.getString("session_id"),
				// LoggedActivity.getLabel(-1), rs.getString("datentime"),
				// rs.getString("action"));
				String allParameters = rs.getString("action");
				String[] params = processAllParameters(allParameters);
				// String action = "";
				// get the activityName from the action value in AllParameters
				String activityName = params[0];// action
				String targetName = params[1];
				String parentName = params[2];// gridname
				String topicName = params[3];
				Integer activityId = Common.MG_ACTIVITYID_MAP.get(activityName);

				// public LoggedActivity(int appId, String session, int activityId,
				// String activityName, String targetName, String parentName,
				// String topicName, double result, String dateStr, long dateNS, String
				// svc,
				// String allParameters) {

				LoggedActivity act = new LoggedActivity(-1, rs.getString("session_id"),
						(activityId == null ? -1 : activityId), activityName, targetName,
						parentName, topicName, -1.0, rs.getString("datentime"), -1, "",-1,-1, utimestamp, 
						allParameters, LogType.AGGREGATE);
				currentUser.addLoggedActivity(act);
			}
			if (currentUser != null)
				res.put(login, currentUser);
			this.releaseStatement(stmt, rs);
			System.out.println("#activities=" + count);
			return res;
		}
		// catch (SQLException ex) {
		// System.out.println("SQLException: " + ex.getMessage());
		// System.out.println("SQLState: " + ex.getSQLState());
		// System.out.println("VendorError: " + ex.getErrorCode());
		// this.releaseStatement(stmt, rs);
		// return null;
		// }
		catch (Exception ex) {
			ex.printStackTrace();
			this.releaseStatement(stmt, rs);
			return null;
		}
	}

	// Returns action, cell, grid, topic
	public static String[] processAllParameters(String allParameters) {
		String[] res = { "", "", "", "" };
		String[] params = allParameters.split(",");
		for (String param : params) {
			String[] pair = param.split(":");
			if (pair.length == 3)
				pair[1] = pair[1] + ":" + pair[2];
			if (pair != null && pair.length >= 2) {
				String p = pair[0];
				String v = pair[1];
				if (v == null)
					v = "";
				if (p.equals("action"))
					res[0] = v;
				if (p.equals("activity-id") || p.equals("cell-activity-id")
						|| p.equals("activity-recommended-id") || p.equals("cell-topic-id"))
					res[1] = v;
				if (p.equals("grid-name")) {
					// System.out.println(p+" = "+v);
					res[2] = v;
				}
				if (p.equals("activity-topic-id") || p.equals("cell-topic-id")
						|| p.equals("activity-recommended-topic-id"))
					res[3] = v;
			}
		}
		return res;
	}

	public HashMap<String, Activity> getActivityTopicMap(String grp) {
		 try
		    {
		      HashMap<String, Activity> res = new HashMap();
		      this.stmt = this.conn.createStatement();
		      String query = "select distinct(T.topic_name) as topic_name, C.content_name, C.provider_id, T.order, TC.resource_id,TC.display_order   from ent_topic T, ent_content C, rel_topic_content TC, ent_group G   where G.group_id = '" + 
		      
		        grp + "' and T.course_id = G.course_id " + 
		        "  and TC.content_id = C.content_id and TC.topic_id = T.topic_id order by T.order, TC.resource_id, TC.display_order";
		      this.rs = this.stmt.executeQuery(query);
		      System.out.println("AGGREGATE QUERY for getActivityTopicMap():\n   " + query);
		      
		      String topic = null;
		      String content = null;
		      int contentOrder = 1;
		      int topicOrder = 0;
		      String previousTopic = "";
		      while (this.rs.next())
		      {
		        topic = this.rs.getString("topic_name");
		        content = this.rs.getString("content_name");
		        if (!previousTopic.equalsIgnoreCase(topic))
		        {
		          previousTopic = topic;
		          topicOrder++;
		        }
		        Activity a = new Activity(content, topic, this.rs.getString("provider_id"), contentOrder, this.rs.getInt("display_order"), topicOrder);
		        
		        res.put(content, a);
		        contentOrder++;
		      }
		      releaseStatement(this.stmt, this.rs);
		      return res;
		    }
		    catch (Exception ex)
		    {
		      ex.printStackTrace();
		      releaseStatement(this.stmt, this.rs);
		    }
		    return null;
	}
	
	public HashMap<String, PCEXActivity> getPCEXActivityTopicMap(String grp) {
		try {
			HashMap<String, PCEXActivity> result = new HashMap<String, PCEXActivity>();
			stmt = conn.createStatement();
			String query = "select distinct(T.topic_name) as topic_name, C.content_name, C.provider_id, T.order, TC.resource_id,TC.display_order,pcex_activity.act_name,pcex_activity.AppID"
					+ "  from ent_topic T, ent_content C, rel_topic_content TC, ent_group G, "
					+ " (SELECT A1.activity AS set_name, A2.activity AS act_name, A2.AppID FROM um2.ent_activity A1, um2.ent_activity A2,um2.rel_pcex_set_component AA1 "
					+ "WHERE A1.AppID = 45 AND (A2.AppID = 46 OR A2.AppID = 47) AND AA1.ParentActivityID = A1.ActivityID AND AA1.ChildActivityID = A2.ActivityID) as pcex_activity"
					+ "  where G.group_id = '"+ grp+ "' and T.course_id = G.course_id "
					+ "  and TC.content_id = C.content_id and TC.topic_id = T.topic_id  AND pcex_activity.set_name = C.content_name order by T.order, TC.resource_id, TC.display_order";
			rs = stmt.executeQuery(query);
			
			
			System.out.println("AGGREGATE QUERY for getPCEXActivityTopicMap():\n   " + query);

			String topic = null;
			String content = null;
			int contentOrder = 1;
			int topicOrder = 0;
			String previousTopic = "";
			while (rs.next()) {
				topic = rs.getString("topic_name");
				content = rs.getString("content_name");
				String actName = rs.getString("act_name");
				boolean isChallenge = rs.getString("AppID").equals("47");
				
				if(previousTopic.equalsIgnoreCase(topic) == false) {
					previousTopic = topic;
					topicOrder++;
				}
				
				PCEXActivity contentActivity = new PCEXActivity(actName, isChallenge, content, topic, rs.getString("provider_id"), contentOrder, rs.getInt("display_order"), topicOrder);
				
				// first user in the logs
				result.put(actName, contentActivity);
				Map<String, List<Entry<String, PCEXActivity>>> collect = result.entrySet().stream().collect(Collectors.groupingBy((entry) -> entry.getValue().getFirstTopic()));
				contentOrder++;
				
			}
			this.releaseStatement(stmt, rs);
			Map<String, List<Entry<String, PCEXActivity>>> collect = result.entrySet().stream().collect(Collectors.groupingBy((entry) -> entry.getValue().getFirstTopic()));
			return result;
		}
		// catch (SQLException ex) {
		// System.out.println("SQLException: " + ex.getMessage());
		// System.out.println("SQLState: " + ex.getSQLState());
		// System.out.println("VendorError: " + ex.getErrorCode());
		// this.releaseStatement(stmt, rs);
		// return null;
		// }
		catch (Exception ex) {
			ex.printStackTrace();
			this.releaseStatement(stmt, rs);
			return null;
		}
	}
	
	public List<PcexSet> getPCEXActivitySet() {
		try {
			List<PcexSet> result = new ArrayList<PcexSet>();
			stmt = conn.createStatement();
			String query = "SELECT "+
					        "activity1.ActivityID AS ActivitySetID, "+
					            "COUNT(activity2.Activity) AS NumCh, "+
					             "group_concat(distinct pcex_set.ChildActivityID separator ',') as Activities "+
					    "FROM "+
					        "um2.rel_pcex_set_component AS pcex_set, um2.ent_activity AS activity1, um2.ent_activity AS activity2 "+
					    "WHERE "+
					        "pcex_set.ParentActivityID = activity1.ActivityID "+
					            "AND pcex_set.ChildActivityID = activity2.ActivityID "+
					            "AND activity1.AppID = 45 "+
					            "AND activity2.AppID = 47 "+
					    "GROUP BY ActivitySetID";
			rs = stmt.executeQuery(query);
			System.out.println("QUERY for getPCEXActivitySet():\n   " + query);

			while (rs.next()) {
				String ActivitySetID = rs.getString("ActivitySetID");
				int NumCh = rs.getInt("NumCh");
				String Activities = rs.getString("Activities");
				
				result.add(new PcexSet(ActivitySetID, NumCh, Activities));
			}
			this.releaseStatement(stmt, rs);
			return result;
		}
		// catch (SQLException ex) {
		// System.out.println("SQLException: " + ex.getMessage());
		// System.out.println("SQLState: " + ex.getSQLState());
		// System.out.println("VendorError: " + ex.getErrorCode());
		// this.releaseStatement(stmt, rs);
		// return null;
		// }
		catch (Exception ex) {
			ex.printStackTrace();
			this.releaseStatement(stmt, rs);
			return null;
		}
		
	
	}

	public HashMap<String, String> getUrlToActivityName() {
		try {
			HashMap<String, String> res = new HashMap<String, String>();
			stmt = conn.createStatement();
			String query = "select content_name, url from ent_content where provider_id = 'sqlknot'";
			rs = stmt.executeQuery(query);
			System.out.println("AGGREGATE QUERY:\n   " + query);

			String activity = "";
			String url = "";
			while (rs.next()) {
				url = rs.getString("url");
				if (!url.contains("cid") || !url.contains("tid"))
					System.out.println("ERROR: url doesn't contain cid or tid (" + url
							+ ")");
				else {
					// http://adapt2.sis.pitt.edu/sqlknot/QuestionGenerator?cid=11&tid=4&svc=progvis
					String suburl = url.substring(url.indexOf("cid"));
					url = "Topic"
							+ suburl.substring(4, suburl.indexOf("&tid"))
							+ "_Template"
							+ suburl.substring(suburl.indexOf("tid") + 4,
									suburl.indexOf("&",suburl.indexOf("tid")));
					
					if(suburl.contains("lang")) {
						url += "_" + suburl.substring(suburl.indexOf("lang=")+5);
					}
				}
				String topic = activity = rs.getString("content_name");
				res.put(url, activity);
			}
			this.releaseStatement(stmt, rs);
			return res;
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
}
