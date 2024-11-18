import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class um2DBInterface extends dbInterface {
	public um2DBInterface(String connurl, String user, String pass) {
		super(connurl, user, pass);
	}

	public HashMap<String, User> getActivity(String grp,
			ArrayList<String> non_students, ArrayList<String> non_sessions,
			HashMap<String, Activity> topic_map, HashMap<String, Activity> topic_map0,
			HashMap<String, PCEXActivity> pcexActivityTopicMap, HashMap<String, String> activityname_map,
			HashMap<String, String> um2_sqlknot_url_to_activityname_map,
			String[] dateRange, boolean queryArchive) {

		String nonStudents = Common.csvFromArray(non_students);
		String nonSessions = Common.csvFromArray(non_sessions);

		try {
			HashMap<String, User> res = new HashMap<String, User>();
			stmt = conn.createStatement();
			// String query =
			// "SELECT AppId, UserId, ActivityId, Result, `Session`, DateNTime, DateNTimeNS,SVC, AllParameters FROM archive_user_activity WHERE GroupId = (select userid from ent_user where login='"+grp+"');";
			String query = "SELECT UA.AppId, UA.UserId, U.Login, UA.ActivityId, UA.Result, UA.`Session`, UA.DateNTime, UA.DateNTimeNS, UNIX_TIMESTAMP(UA.DateNTime) as utimestamp, UA.SVC, UA.AllParameters "
					// +
					// " FROM ent_user U, archive_user_activity UA left join rel_activity_activity RAA on (RAA.ChildActivityId = UA.ActivityId or RAA.ParentActivityId = UA.ActivityId) "
					+ (queryArchive ? " FROM ent_user U, archive_user_activity UA ": " FROM ent_user U, ent_user_activity UA ")
					+ " WHERE "
					+ " GroupId = (select userid from ent_user where isgroup = 1 and login='"
					+ grp
					+ "') " + " and U.UserId = UA.UserId ";
			if (nonStudents != null) {
				query += " and U.Login not in (" + nonStudents + ") ";
				query += " and U.Login not in (SELECT distinct(user_id) FROM aggregate.ent_non_student)";
			}
			if (nonSessions != null)
				query += " and UA.`Session` not in (" + nonSessions + ") ";
			// get rid of all sessions that look like 'test'
			query += " and UA.`Session` not like '%TEST%' and UA.`Session` not like '%test%'";
			// admins out of the picture
			query += " and UA.UserId not in (SELECT userId from rel_user_user WHERE GroupId = 68) ";
			query += " and UA.AllParameters not like '%usr=undefined%' and UA.AllParameters not like '%sid=undefined%' ";
			if (dateRange[0].length() > 0)
				query += " AND datentime > '" + dateRange[0] + "' ";
			if (dateRange[1].length() > 0)
				query += " AND datentime < '" + dateRange[1] + "' ";
			query += " order by UA.UserId, UA.DateNTime asc;";
			System.out.println("UM QUERY for getActivity():\n    " + query);
			rs = stmt.executeQuery(query);
			
			// String content_name = "";
			// ArrayList<String[]> c_c = null;
			User currentUser = null;
			int user = -1;
			String login = null;
			long utimestamp = -1;
			int count = 0;
			Set<String> activityWithNullTopic = new HashSet<String>();
			while (rs.next()) {
				count++;
				user = rs.getInt("UserId");
				login = rs.getString("Login");
				utimestamp = rs.getLong("utimestamp");
				// first user in the logs
				if (currentUser == null)
					currentUser = new User(user, login);
				// when detecting a new user, add the current user to 'res' and create
				// another user object
				if (currentUser.getUserId() != user) {
					res.put(currentUser.getUserLogin(), currentUser);
					currentUser = new User(user, login);
				}
				int appId = rs.getInt("AppId");
				String allParameters = rs.getString("AllParameters");
				String activityName = "";
				String parentName = "";
				String topicName = "";
				int actOrderInCourse = -1;
				int topicOrderInCourse = -1;
				// System.out.println(allParameters);
				// for SQLKNOT (23), need to use activityid in um2->url in um2-> match
				// the url
				// in aggregate->get contentname
				// map
				// for WEBEX and ANIMATED_EXAMPLES
				// (3) activityName (line clicked) and parentName (example) are the act
				// and sub in AllParameters
				// for QUIZJET (25) activityName (question) the sub parameter in
				// AllParameters. activityParent does not exist
				// 37 SALT, 38 parsons, 39 socialreader, 40 educvideos, 41 quizpet, 44 PCRS
				if (appId == 3 || appId == 25 || appId == 35 || appId == 8 || appId == 23 || appId == 19
						|| appId == 37 || appId == 38 || appId == 39 || appId == 40 || appId == 41 || appId == 44 || appId == 46 || appId == 47 || appId == 53) {
					
					int codIndex = allParameters.indexOf("cod=");
					if(codIndex>0) {
						allParameters = allParameters.substring(0,codIndex);
					}
					
					String[] all_params = allParameters.split(";");
					if (all_params != null) {
						for (String _p : all_params) {
							if (_p.trim().length() > 4) {
								//System.out.println("Param : "+_p);
								String param = _p.trim().substring(0, 3);
								String value = _p.trim().substring(4);
								if (param.equalsIgnoreCase("act")) {
									switch (appId) {
									case 3:
									case 35: // WEBEX and AE the act is the example
									case 37:
									case 39:
									case 44:
									case 40:
										parentName = value;
										if (topic_map != null && topic_map.containsKey(value)) {
											Activity a = topic_map.get(value);
											actOrderInCourse = a.getOrderInCourse();
											topicOrderInCourse = a.getTopicOrderInCourse();
											topicName = a.getFirstTopic();
										}
										//TODO: if (date < 09/26/2014), then use topic_map0
										if ((grp.equals("ASUFALL2014")) && Common.compareStringDates(rs.getString("DateNTime"), "2014-09-26 00:00:00.000") < 0){
											Activity a = topic_map0.get(value);
											actOrderInCourse = a.getOrderInCourse();
											topicOrderInCourse = a.getTopicOrderInCourse();
											topicName = a.getFirstTopic();
										}
										break;
									case 46: //PCEX_example
										parentName = value;
										if(pcexActivityTopicMap != null && pcexActivityTopicMap.containsKey(value)) {
											PCEXActivity activity = pcexActivityTopicMap.get(value);
											actOrderInCourse = activity.getOrderInCourse();
											topicOrderInCourse = activity.getTopicOrderInCourse();
											topicName = activity.getFirstTopic();
										}
										break;
									case 25: // QUIZJET has the topic in the act parameter
										if (topicName.length() == 0) {
											if (activityname_map.containsKey(value)) {
												activityName = activityname_map.get(value);
												parentName = activityName; // hy added
												if (topic_map != null && topic_map.containsKey(activityName)){
													topicName = topic_map.get(activityName).getFirstTopic();
												  
												}
												if ((grp.equals("ASUFALL2014")) && Common.compareStringDates(rs.getString("DateNTime"), "2014-09-26 00:00:00.000") < 0)
												  	topicName = topic_map0.get(value).getFirstTopic();
											}
											else
												topicName = value;
										}
										break;
									case 41: // QUIZPET has the topic in the sub parameter. Before 20181104 it was next to QuizJet, but it was picking wrong topic names. 
										break;
									case 23:// SQLKNOT act is the topic
										if (value.contains("Topic") || value.equals("sqllab")) {
											topicName = value;
										} 
										break;
									case 8: // KT has the activity in the act
										activityName = value;
										break;
									case 53:
										break;
									}

								}
								else if (param.equalsIgnoreCase("sub")) {
									// System.out.println("  sub: "+value+ " !!!!!");
									switch (appId) {
									case 3:
									case 35: // WEBEX and AE the sub is the line
									case 37:
									case 39:
									case 40:
										activityName = value;
										break;
									case 25: // QUIZJET report the activity in sub
									case 41: // QUIZ PET
									case 38: // PARSONS
									case 53: // DBQA
									case 44: //PCRS
										if (activityName.length() == 0) {
											activityName = value;
											parentName = activityName; // hy added
											if (topicName.length() == 0 && topic_map != null && topic_map.containsKey(activityName)){
												Activity a = topic_map.get(value);
												actOrderInCourse = a.getOrderInCourse();
												topicOrderInCourse = a.getTopicOrderInCourse();
												topicName = a.getFirstTopic();
											}
											//TODO: if (date < 09/26/2014), then use topic_map0
											if ((grp.equals("ASUFALL2014")) && Common.compareStringDates(rs.getString("DateNTime"), "2014-09-26 00:00:00.000") < 0){
												Activity a = topic_map0.get(value);
												actOrderInCourse = a.getOrderInCourse();
												topicOrderInCourse = a.getTopicOrderInCourse();
												topicName = a.getFirstTopic();
											}
												
										}
										break;
									case 47: //PCEX_challenge
										activityName = value;
										parentName = activityName;
										if(pcexActivityTopicMap != null && pcexActivityTopicMap.containsKey(value)) {
											PCEXActivity activity = pcexActivityTopicMap.get(value);
											actOrderInCourse = activity.getOrderInCourse();
											topicOrderInCourse = activity.getTopicOrderInCourse();
											topicName = activity.getFirstTopic();
										}
										break;
									case 23:
										if (value.contains("Template")) {
											parentName = value;
											activityName = parentName;
										}
										break;
									case 19: //SQL-Tutor
										activityName = value;
										
										if (topic_map != null && topic_map.containsKey(value)){
											Activity a = topic_map.get(value);
											actOrderInCourse = a.getOrderInCourse();
											topicOrderInCourse = a.getTopicOrderInCourse();
											topicName = a.getFirstTopic();
										}
										break;
									case 8: // KT has the activity in the act
										break;
									}
								}
							}

						}
					}
				}
				//SQLKNOT
				if (appId == 23 && topicName.contains("Topic")) {
					parentName = um2_sqlknot_url_to_activityname_map.get(topicName + "_" + parentName);
					//parentName = topicName + "_" + parentName;
					activityName = parentName;
					topicName = activityName;
					try{
						topicName = topic_map.get(activityName).getFirstTopic();
					}catch(Exception e){
						topicName = "";
					}
				}
				if (!parentName.equals("")
						&& (topicName.equals("null") || topicName.equals("") || topicName
								.length() == 0)) {
					activityWithNullTopic.add(parentName);
				}

				// TODO: check: For correcting session for progressor group webex
				// activities
				// (session_id starts with WE)
				String currentSession = rs.getString("Session");
				if (rs.getInt("AppId") == 3 && currentSession.startsWith("WE")) {
					LoggedActivity previousActivity = currentUser.getActivity().get(
							currentUser.getActivity().size() - 1);
					String previousSession = previousActivity.getSession();
					if (!previousSession.equals(currentSession))
						currentSession = previousSession;
				}
				// System.out.println(currentUser.getUserLogin() + "," + parentName +
				// ","
				// + topicName + "," + rs.getDouble("Result"));
				LoggedActivity act = new LoggedActivity(rs.getInt("AppId"),
						currentSession, rs.getInt("ActivityId"), activityName,
						activityName, parentName, topicName, rs.getDouble("Result"),
						rs.getString("DateNTime"), rs.getLong("DateNTimeNS"),
						rs.getString("SVC"), actOrderInCourse, topicOrderInCourse, utimestamp, allParameters,LogType.UM);
				// (int appId, String session, String label,
				// int activityId, int parent, double result, Date date, long dateNS,
				// String svc, String allParameters)
				//System.out.println("##### result: "+act.getResult()+" #### topic name: "+act.getTopicOrder());
				currentUser.addLoggedActivity(act);
			}
			
			if (currentUser != null)
				res.put(login, currentUser);
			this.releaseStatement(stmt, rs);
			
			if(grp.equals("AALTOPY17G0")) { //Add PCEX control group activities from another table. Free to remove this check since we will not use the control group again
				addPcexControlTrackingActivities(res, grp, non_students, non_sessions, topic_map, topic_map0, pcexActivityTopicMap, activityname_map, um2_sqlknot_url_to_activityname_map, dateRange);
			} else {
				addPcexTrackingActivities(res, grp, non_students, non_sessions, topic_map, topic_map0, pcexActivityTopicMap, activityname_map, um2_sqlknot_url_to_activityname_map, dateRange);
			}
			
			System.out.println("#activities=" + count);
			System.out.println("activities with null topic:");
			for (String s : activityWithNullTopic) {
				System.out.print(s + ",");
			}
			System.out.println();
			return res;
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			this.releaseStatement(stmt, rs);
			return null;
		}
		catch (Exception ex) {
			System.out.println("Exception while getting activities from UM2: "
					+ ex.getMessage());
			ex.printStackTrace();
			
			this.releaseStatement(stmt, rs);
			return null;
		}
	}

	private void addPcexControlTrackingActivities(HashMap<String, User> result,
			String grp, ArrayList<String> non_students,
			ArrayList<String> non_sessions,
			HashMap<String, Activity> topic_map,
			HashMap<String, Activity> topic_map0,
			HashMap<String, PCEXActivity> pcexActivityTopicMap,
			HashMap<String, String> activityname_map,
			HashMap<String, String> um2_sqlknot_url_to_activityname_map,
			String[] dateRange) throws SQLException {
		
		String nonStudents = Common.csvFromArray(non_students);
		String nonSessions = Common.csvFromArray(non_sessions);
		
		String query = "SELECT user_id, group_id, session_id, activity_set_name, goal_name, explanations_shown, datetime "
				+ " FROM pcex.ent_control_tracking tracking"
				+ " WHERE "
				+ " group_id = '" + grp + "' ";
		
		if (nonStudents != null)
			query += " and user_id not in (" + nonStudents + ") ";
		if (nonSessions != null)
			query += " and session_id not in (" + nonSessions + ") ";
		
		query += " and session_id not like '%TEST%' and session_id not like '%test%'";
		// admins out of the picture
		
		if (dateRange[0].length() > 0)
			query += " AND datetime > '" + dateRange[0] + "' ";
		if (dateRange[1].length() > 0)
			query += " AND datetime < '" + dateRange[1] + "' ";
		query += " order by user_id, datetime asc;";
		
		stmt = conn.createStatement();
		
		rs = stmt.executeQuery(query);
		System.out.println("PCEX QUERY for getActivity():\n    " + query);
		
		while (rs.next()) {
			String login = rs.getString("user_id");
			String sessionId = rs.getString("session_id");
			String activitySetName = rs.getString("activity_set_name");
			String goalName = rs.getString("goal_name").replace(".java", "").replace(".py", "");
			Timestamp date = rs.getTimestamp("datetime");
			String explanationsShown = rs.getString("explanations_shown");
			
			User currentUser = result.get(login);
			if(currentUser == null) {
				currentUser = new User(-1, login);
			}
			
			PCEXActivity pcexActivity = pcexActivityTopicMap.get(goalName);
			int appId = pcexActivity.isChallenge() ? Common.PCEX_CHALLENGE:Common.PCEX_EXAMPLE;
			
			LoggedActivity act = new LoggedActivity(appId, sessionId, -1, goalName, activitySetName, goalName, pcexActivity.getFirstTopic(), -1,
					date.toString(), date.getTime(), explanationsShown, pcexActivity.getOrderInCourse(), pcexActivity.getTopicOrderInCourse(), -1, "",LogType.PCEX_CONTROL);
			
			currentUser.addLoggedActivity(act);
		}
		
		this.releaseStatement(stmt, rs);
	}

	private void addPcexTrackingActivities(HashMap<String, User> result, String grp,
			ArrayList<String> non_students, ArrayList<String> non_sessions,
			HashMap<String, Activity> topic_map, HashMap<String, Activity> topic_map0,
			HashMap<String, PCEXActivity> pcexActivityTopicMap, HashMap<String, String> activityname_map,
			HashMap<String, String> um2_sqlknot_url_to_activityname_map,
			String[] dateRange) throws SQLException {
		
		String nonStudents = Common.csvFromArray(non_students);
		String nonSessions = Common.csvFromArray(non_sessions);
		
		String query = "SELECT user_id, group_id, session_id, tracking_id, activity_set_name, activity_type, goal_name, datetime "
				+ " FROM pcex.ent_activity_tracking tracking"
				+ " WHERE "
				+ " group_id = '" + grp + "' ";
		
		if (nonStudents != null)
			query += " and user_id not in (" + nonStudents + ") ";
		if (nonSessions != null)
			query += " and session_id not in (" + nonSessions + ") ";
		
		query += " and session_id not like '%TEST%' and session_id not like '%test%'";
		// admins out of the picture
		
		if (dateRange[0].length() > 0)
			query += " AND datetime > '" + dateRange[0] + "' ";
		if (dateRange[1].length() > 0)
			query += " AND datetime < '" + dateRange[1] + "' ";
		query += " order by user_id, datetime asc;";
		
		stmt = conn.createStatement();
		
		rs = stmt.executeQuery(query);
		System.out.println("PCEX QUERY for getActivity():\n    " + query);
		
		while (rs.next()) {
			String login = rs.getString("user_id");
			String sessionId = rs.getString("session_id");
			String trackingId = rs.getString("tracking_id");
			String activitySetName = rs.getString("activity_set_name");
			String activityType = rs.getString("activity_type");
			String goalName = rs.getString("goal_name").replace(".java", "").replace(".py", "");
			Timestamp date = rs.getTimestamp("datetime");
			
			User currentUser = result.get(login);
			if(currentUser == null) {
				currentUser = new User(-1, login);
			}
			
			int appId = activityType.equals("ex") ? Common.PCEX_EXAMPLE:Common.PCEX_CHALLENGE;
			PCEXActivity pcexActivity = pcexActivityTopicMap.get(goalName);
			try {
				LoggedActivity act = new LoggedActivity(appId, sessionId, -1, goalName, activitySetName, goalName, pcexActivity.getFirstTopic(), -1,
						date.toString(), date.getTime(), trackingId, pcexActivity.getOrderInCourse(), pcexActivity.getTopicOrderInCourse(), -1, "",LogType.PCEX);
				currentUser.addLoggedActivity(act);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			
			
		}
		
		
		this.releaseStatement(stmt, rs);
		
	}

	// returns the user information given the username
	public String[] getUsrInfo(String usr) {
		try {
			String[] res = null;
			stmt = conn.createStatement();
			String query = "select U.name, U.email from ent_user U where U.login = '"
					+ usr + "';";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				res = new String[2];
				res[0] = "";
				res[1] = "";
				res[0] = rs.getString("name").trim();
				res[1] = rs.getString("email").trim();
			}
			return res;
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			return null;
		}
		finally {
			this.releaseStatement(stmt, rs);
		}

	}

	public ArrayList<String[]> getClassList(String grp) {

		try {
			ArrayList<String[]> res = new ArrayList<String[]>();
			stmt = conn.createStatement();
			String query = "select U.userid, U.login, U.name, U.email "
					+ "from ent_user U, rel_user_user UU "
					+ "where UU.groupid = (select userid from ent_user where login='"
					+ grp + "' and isgroup=1) " + "and U.userid=UU.userid";
			// System.out.println(query);
			rs = stmt.executeQuery(query);
			int i = 0;
			while (rs.next()) {
				String[] act = new String[3];
				act[0] = rs.getString("login");
				act[1] = rs.getString("name").trim();
				act[2] = rs.getString("email").trim();
				res.add(act);
				// System.out.println(act[0]+" "+act[1]+" "+act[2]+" "+act[3]);
				i++;
			}
			this.releaseStatement(stmt, rs);
			return res;
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			return null;
		}
		finally {
			this.releaseStatement(stmt, rs);
		}
	}

	// get a list with the content and for each content item, all the concepts in
	// an array list with
	public ArrayList<String[]> getContentConcepts(String domain) {
		try {
			// HashMap<String, ArrayList<String[]>> res = new HashMap<String,
			// ArrayList<String[]>>();
			ArrayList<String[]> res = new ArrayList<String[]>();
			stmt = conn.createStatement();
			String query = "SELECT CC.content_name, "
					+ " group_concat(CC.concept_name , ',', cast(CONVERT(CC.weight,DECIMAL(10,3)) as char ), ',' , cast(CC.direction as char) order by CC.weight separator ';') as concepts "
					+ " FROM agg_content_concept CC  " + " WHERE CC.domain = '" + domain
					+ "'" + " group by CC.content_name order by CC.content_name;";
			rs = stmt.executeQuery(query);
			// System.out.println(query);
			// String content_name = "";
			// ArrayList<String[]> c_c = null;
			while (rs.next()) {
				String[] data = new String[2];
				data[0] = rs.getString("content_name");
				data[1] = rs.getString("concepts");

				res.add(data);
			}
			this.releaseStatement(stmt, rs);
			return res;
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			this.releaseStatement(stmt, rs);
			return null;
		}
	}

	/*
	 * @Return: a map with keys as examples identifiers and values as the number
	 * of clickable lines in the example
	 */
	public Map<Integer, Integer> getExampleLines() {

		try {
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			stmt = conn.createStatement();
			String query = " select A.activityid as activityid, group_concat(distinct A2.activity order by A2.activity asc separator ',') as `line` "
					+ " from ent_activity A, rel_activity_activity AA, ent_activity A2 "
					+ " where A.activityid = AA.ParentActivityID  and A2.activityid = AA.ChildActivityID"
					+ " and AA.AppID = 3 " + " group by A.activityid;";
			rs = stmt.executeQuery(query);
			String[] lines;
			int total;
			while (rs.next()) {
				lines = rs.getString("line").split(",");
				total = 0;
				for (String l : lines)
					if (l.trim().equals("0") == false)
						total++;
				map.put(rs.getInt("activityid"), total);
			}
			this.releaseStatement(stmt, rs);
			return map;
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			this.releaseStatement(stmt, rs);
			return null;
		}

	}

	/*
	 * @return a map with the keys as activityids and values as activity in
	 * ent_activity table
	 */
	public Map<Integer, String> getExIdActMap() {
		try {
			Map<Integer, String> map = new HashMap<Integer, String>();
			stmt = conn.createStatement();
			String query = " select distinct A.activityid, A.activity "
					+ " from ent_activity A";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				map.put(rs.getInt("activityid"), rs.getString("activity"));
			}
			this.releaseStatement(stmt, rs);
			return map;
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			this.releaseStatement(stmt, rs);
			return null;
		}

	}

	/*
	 * @return a map with the keys as activity (names) and values as activityid in
	 * ent_activity table
	 */
	public Map<String, Integer> getActivityIdMap() {
		try {
			Map<String, Integer> map = new HashMap<String, Integer>();
			stmt = conn.createStatement();
			String query = " select distinct A.activityid, A.activity "
					+ " from ent_activity A";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				map.put(rs.getString("activity"), rs.getInt("activityid"));
			}
			this.releaseStatement(stmt, rs);
			return map;
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			this.releaseStatement(stmt, rs);
			return null;
		}

	}

	public Map<Integer, String> getUserIdLoginMap() {
		try {
			Map<Integer, String> map = new HashMap<Integer, String>();
			stmt = conn.createStatement();
			String query = " select distinct U.userid, U.login "
					+ " from ent_user U ";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				map.put(rs.getInt("userid"), rs.getString("login"));
			}
			this.releaseStatement(stmt, rs);
			return map;
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			this.releaseStatement(stmt, rs);
			return null;
		}
	}

	public Map<String, Integer> getLoginUserIdMap() {
		try {
			Map<String, Integer> map = new HashMap<String, Integer>();
			stmt = conn.createStatement();
			String query = " select distinct U.userid, U.login "
					+ " from ent_user U ";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				map.put(rs.getString("login"), rs.getInt("userid"));
			}
			this.releaseStatement(stmt, rs);
			return map;
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			this.releaseStatement(stmt, rs);
			return null;
		}
	}

	public HashMap<String, Activity> getActivityTopicMap(String grp) {
		try {
			HashMap<String, Activity> res = new HashMap<String, Activity>();
			stmt = conn.createStatement();
			String query = "select topic_name, rdfid as content_name "
					+ "  from progressor_rel_topic_content" + "  where group_id = '"
					+ grp + "'";
			rs = stmt.executeQuery(query);
			//System.out.println("UM QUERY for getActivityTopicMap():\n   " + query);

			String topic = null;
			String content = null;
			while (rs.next()) {
				topic = rs.getString("topic_name");
				content = rs.getString("content_name");
				Activity a = new Activity(content,topic,"",0,0,0);
				// first user in the logs
				res.put(content, a);
			}
			this.releaseStatement(stmt, rs);
			return res;
		}
		catch (Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
			this.releaseStatement(stmt, rs);
			return null;
		}
	}

	/**
	 * Get the example line id (activityid) from a combination f the exmaple_line.
	 * For example: switch1_12 will retrieve the id of the 12th line of the
	 * example switch1
	 * 
	 * @return
	 */
	public Map<String, Integer> getActSubIdMap() {
		try {
			Map<String, Integer> map = new HashMap<String, Integer>();
			stmt = conn.createStatement();
			String query = " select A.activityid as activityid, A.activity as act, A2.activity as `sub` , A2.activityid as subid	"
					+ "from ent_activity A, rel_activity_activity AA, ent_activity A2 "
					+ "where A.activityid = AA.ParentActivityID  and A2.activityid = AA.ChildActivityID "
					+ "and AA.AppID in (3,6,9,23,32,35,46) order by A.activityid, `sub` asc";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				map.put(rs.getString("act") + "_" + rs.getString("sub"),
						rs.getInt("subid"));
			}
			this.releaseStatement(stmt, rs);
			return map;
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			this.releaseStatement(stmt, rs);
			return null;
		}

	}

	/**
	 * Gets the activity tracked with errors from the database (appid, activityid,
	 * userid, groupid = 1, 1, 2, 1)
	 */
	public ArrayList<String[]> getBadTrackedActivity(boolean archive) {
		try {
			ArrayList<String[]> res = new ArrayList<String[]>();
			stmt = conn.createStatement();
			String query = "SELECT id, datentime, datentimens, AllParameters FROM ent_user_activity WHERE ";
			if (archive)
				query = "SELECT id, datentime, datentimens, AllParameters FROM archive_user_activity WHERE ";
			query += "appid=1 AND activityid=1 AND groupid=1 AND userid=2 "
					+ "AND allparameters not like '%grp=anonymous%' "
					+ "AND allparameters not like '%grp=&%' "
					+ "AND allparameters not like '%grp=ensemble%' "
					+ "AND allparameters not like '%grp=meta_group%' "
					+ "AND allparameters not like '%usr=null%' "
					+ "AND allparameters not like '%grp=null%' "
					+ "AND allparameters not like '%grp=;%' ";
			// System.out.println(query);
			rs = stmt.executeQuery(query);

			while (rs.next()) {
				String[] row = new String[9];
				row[0] = rs.getString("datentime");
				row[1] = rs.getString("datentimens");
				row[2] = rs.getString("AllParameters");
				row[3] = "";
				row[4] = "";
				row[5] = "";
				row[6] = "";
				row[7] = "";
				row[8] = rs.getString("id");
				res.add(row);
			}
			this.releaseStatement(stmt, rs);
			return res;
		}
		catch (Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
			this.releaseStatement(stmt, rs);
			return null;
		}
	}

	public HashMap<String, String> getActivityName() {
		try {
			HashMap<String, String> res = new HashMap<String, String>();
			stmt = conn.createStatement();
			String query = "select ActivityID, Activity from ent_activity where appid in (3, 23, 35, 37,38,44,45,46,47,53)";
			rs = stmt.executeQuery(query);
			System.out.println("UM QUERY:\n   " + query);

			int activityid = -1;
			String activity = "";
			while (rs.next()) {
				activityid = rs.getInt("ActivityID");
				activity = rs.getString("Activity");
				res.put(activityid + "", activity);
			}
			this.releaseStatement(stmt, rs);
			return res;
		}
		catch (Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
			this.releaseStatement(stmt, rs);
			return null;
		}
	}
}
