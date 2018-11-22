import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * A GroupActivity object is the main object that contains Users and it is used
 * by all services to draw results
 * 
 * @author pawslab
 * 
 */
public class GroupActivity {
	public ArrayList<String> non_sessions;
	public ArrayList<String> non_students;
	private ConfigManager cm;

	public um2DBInterface um2_db;
	public aggregateDBInterface aggregate_db;

	// Data structures
	public HashMap<String, User> grp_activity;

	public HashMap<String, User> getGrpActivity() {
		return grp_activity;
	}

	private HashMap<String, User> grp_activity_aggregate;
	private HashMap<String, User> grp_activity_um2;

	public HashMap<String, Activity> topic_map;
	public HashMap<String, Activity> topic_map0; // only for ASU group before 09/26/2014
	public HashMap<String, String> um2_activityname_map;
	public HashMap<String, String> um2_sqlknot_url_to_activityname_map;

	private String groupId;

	public String getGroupId() {
		return groupId;
	}

	public boolean summary = false;
	private HashMap<String, PCEXActivity> pcexActivityTopicMap;
	private List<PcexSet> pcexActivitySet;

	
	/**
	 * 
	 * @param grp
	 *          the group mnemonic
	 * @param topicSource DEPRECATED
	 *          MG (mastery grids) or PR (progressor)
	 * @param non_students
	 *          a list of students ids to omit
	 * @param non_sessions
	 *          a list of session ids to omit
	 * @param getSummary
	 *          true for compute summary for each student
	 * @param cm
	 *          the ConfigManage object to access configuration variables
	 * @param dateRange
	 *          two date in "YYYY-MM-DD HH24:MI:SS" format representing lower and
	 *          upper bounds for the activity retrieved
	 */
	
	public GroupActivity(String grp, String topicSource,
			ArrayList<String> non_students, ArrayList<String> non_sessions,
			boolean getSummary, ConfigManager cm, String[] dateRange, boolean queryArchive){
		this(grp, topicSource, non_students, non_sessions, getSummary, cm, dateRange, queryArchive, false, 0, null);
	}
	public GroupActivity(String grp, String topicSource,
			ArrayList<String> non_students, ArrayList<String> non_sessions,
			boolean getSummary, ConfigManager cm, String[] dateRange, boolean queryArchive,
			boolean resetSessions, int sessionMinThreshold, long timeBins[]) {
		this.cm = cm;
		this.non_students = non_students;
		this.non_sessions = non_sessions;

		groupId = grp;
		try {
			um2_db = new um2DBInterface(cm.um2_dbstring, cm.um2_dbuser, cm.um2_dbpass);
			um2_db.openConnection();

			aggregate_db = new aggregateDBInterface(cm.aggregate_dbstring,
					cm.aggregate_dbuser, cm.aggregate_dbpass);
			aggregate_db.openConnection();

			// -------------------------------------------------------------------
			// 1. Get the topic map according to the group
			// -------------------------------------------------------------------
			topic_map = null;
			topic_map0 = null;

			topic_map = aggregate_db.getActivityTopicMap(groupId);
			if (groupId.equals("ASUFALL2014")) topic_map0 = aggregate_db.getActivityTopicMap("IS172014Spring");//has course_id =1
			if(topic_map.isEmpty()){
				topic_map = um2_db.getActivityTopicMap(Common.progressor_grps_map);
				HashMap<String, Activity> agg_topic_map = aggregate_db
						.getActivityTopicMap("IS172014Spring");// need to pick a group that
																										// has course_id = 1
				for (Map.Entry<String, Activity> e : agg_topic_map.entrySet()) {
					if (!topic_map.containsKey(e.getKey()))
						topic_map.put(e.getKey(), e.getValue());
				}
			}

			pcexActivityTopicMap = aggregate_db.getPCEXActivityTopicMap(groupId);
			pcexActivitySet = aggregate_db.getPCEXActivitySet();

			// -------------------------------------------------------------------
			// 2. GET the activity tracked in aggregate (mastery grid)
			// -------------------------------------------------------------------
			grp_activity_aggregate = aggregate_db.getActivity(groupId, non_students,
					non_sessions, dateRange);
			System.out.println("Finish getting activities from aggregate!");
			um2_sqlknot_url_to_activityname_map = aggregate_db.getUrlToActivityName();

			// -------------------------------------------------------------------
			// 3. GET all the activity tracked in UM archive_user_activity table
			// -------------------------------------------------------------------
			um2_activityname_map = um2_db.getActivityName();
			grp_activity_um2 = um2_db.getActivity(groupId, non_students,
					non_sessions, topic_map, topic_map0, pcexActivityTopicMap,um2_activityname_map,
					um2_sqlknot_url_to_activityname_map, dateRange, queryArchive);
			System.out.println("Finish getting activities from um2!");

			// -------------------------------------------------------------------
			// 4. MERGE activity tracked from UM2 and Aggregate for each user and
			// order by date asc
			// -------------------------------------------------------------------
			System.out.println("ACTIVITY FROM UM: " + grp_activity_um2);
			System.out.println("ACTIVITY FROM AGG: " + grp_activity_aggregate);
			grp_activity = mergeActivity(grp_activity_um2, grp_activity_aggregate);
			System.out.println("Finish merging!");

			// -------------------------------------------------------------------
			// 5. Computations for each user.
			// times
			// attempt/action number
			// -------------------------------------------------------------------
			for (Map.Entry<String, User> entry : grp_activity.entrySet()) {
				User user = entry.getValue();
				if(resetSessions) user.generateSessionIds(sessionMinThreshold);
				user.computeActivityTimes();
				user.computeAttemptNo();
				if(groupId.equals("AALTOPY17G0")) { //PCEX control group name, PCEX control tool used only once
					user.computePCEXSetCompletionRate(pcexActivitySet, true); //Just to PCEX set rates. If activities are separated, no need to check this
				} else {
					user.computePCEXSetCompletionRate(pcexActivitySet, false); //Just to PCEX set rates. If activities are separated, no need to check this
				}
			}
			//This part was in the if below in part 6. I want it to be done for raw activity as well!
			Labeller l = new Labeller(this,new String[]{"s","l"});
			l.labelTime(true); // this will replace extremely big times by the median
			
			// -------------------------------------------------------------------
			// 6. Summary of data, including durations of times in activity
			//    which are filtered accordingly to a maximum and replaced by 
			//    group median.
			// -------------------------------------------------------------------
			if (getSummary) {
//				Labeller l = new Labeller(this,new String[]{"s","l"});
//				l.labelTime(true); // this will replace extremely big times by the median
				for (Map.Entry<String, User> entry : grp_activity.entrySet()) {
					User user = entry.getValue();
					user.getSummary(timeBins);
				}
			}
			um2_db.closeConnection();
			aggregate_db.closeConnection();

		}
		catch (Exception e) {
			um2_db.closeConnection();
			aggregate_db.closeConnection();
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * 
	 * @param mode
	 *          0: by session 1: by question 2: by session/question 3: by topic
	 * @param include
	 *          0: all 1: only questions 2: only examples 3: questions and
	 *          examples (examples means WEBEX and ANIMATED_EXAMPLES)
	 */
	public void sequence(int mode, int include) {
		for (Map.Entry<String, User> entry : grp_activity.entrySet()) {
			User user = entry.getValue();
			user.sequenceActivity(mode, include);
		}
	}

	/**
	 * Merges activity from UM and Aggregate and order by datetime
	 * 
	 * @param grp_activity_um2
	 * @param grp_activity_aggregate
	 * @return
	 */
	public HashMap<String, User> mergeActivity(
			HashMap<String, User> grp_activity_um2,
			HashMap<String, User> grp_activity_aggregate) {
		try {
			// TODO: consider whether change to user new memory space
			HashMap<String, User> res = grp_activity_um2;
			if (res != null) {
				for (Map.Entry<String, User> entry : res.entrySet()) {
					ArrayList<LoggedActivity> activity = entry.getValue().getActivity();
					if (activity == null)
						activity = new ArrayList<LoggedActivity>();
					User u = grp_activity_aggregate.get(entry.getKey());
					if (u != null) {
						activity.addAll(grp_activity_aggregate.get(entry.getKey())
								.getActivity());
					}
					Collections.sort(activity);
				}
			}

			// add users that could be only in aggregate
			for (Map.Entry<String, User> entry : grp_activity_aggregate.entrySet()) {
				if (res.get(entry.getKey()) == null)
					res.put(entry.getKey(), entry.getValue());
			}

			// for (String s : res.keySet()) {
			// for (LoggedActivity u : res.get(s).getActivity()) {
			// System.out.println(u.getActivityName() + "," + u.getResult());
			// }
			// }

			return res;
		}
		catch (Exception ex) {
			System.out.println("Exception while merging: ");
			ex.printStackTrace();
			return null;
		}
	}

	public boolean isThereActivity() {
		if (grp_activity == null || grp_activity.size() == 0)
			return false;
		return true;
	}
}
