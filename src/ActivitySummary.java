import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ActivityReport
 */
@WebServlet("/ActivityReport")
public class ActivitySummary extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static DecimalFormat df = new DecimalFormat("#.##");

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ActivitySummary() {
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
		StringBuilder outputBuilder = new StringBuilder();
		String groupIdParam = request.getParameter("grp"); // group id
		String[] groupIds = null;
		if (groupIdParam != null)
			groupIds = groupIdParam.split("\\s*[,\t]+\\s*");
		
		String fileName = request.getParameter("filename");
		if (fileName == null) fileName = groupIdParam.replaceAll(",", "_") + "_" + "summary.txt";
		
		response.setContentType("text/plain");
		response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

		
		String users = request.getParameter("usr");// user ids (can be many, comma separated)
		String[] userIds = null;
		if (users != null)
			userIds = users.split("\\s*[,\t]+\\s*");
		String header = request.getParameter("header"); // include or not the header
		boolean incHeader = (header != null && header.equalsIgnoreCase("yes"));

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

		long[] timeBins = null;
		String timebins = request.getParameter("timebins");
		if (timebins != null){
			String[] timeBinsStr = timebins.split(",");
			timeBins = new long[timeBinsStr.length];
			try{
				for(int i = 0;i<timeBinsStr.length;i++) timeBins[i] = Long.parseLong(timeBinsStr[i]);	
			}catch(Exception e){
				timeBins = null;
			}
			
		}
		
		
		boolean sessionate = (request.getParameter("sessionate") != null);
		int minThreshold = 90;
		if(sessionate) minThreshold = 90;
		try{minThreshold = Integer.parseInt(request.getParameter("minthreshold"));}catch(Exception e){minThreshold = 90;}
		
		
		ArrayList<String> non_students = new ArrayList<String>(Common.non_students);
		
		String output = "";
		if (groupIds == null) {
			error = true;
			errorMsg = "group identifier not provided or invalid";
		}
		else {
			
			for(String groupId:groupIds) {
				
				String topicSource = "UNKNOWN";
				if (Common.mg_grps.contains(groupId))
					topicSource = "MG";
				else if (Common.progressor_grps.contains(groupId))
					topicSource = "PR";

				// ---------------------------------------------------------------------------
				// This is the main objects, and where the main computations are made
				// ---------------------------------------------------------------------------
				GroupActivity groupActivity = new GroupActivity(groupId, topicSource,
						non_students, Common.non_sessions, true, cm, dateRange, queryArchive, sessionate, minThreshold, timeBins);
				
				String delimiter = cm.delimiter;
				if (incHeader) {
					outputBuilder.append("user" + delimiter + "group" + delimiter + "sessions_dist"
							+ delimiter + "median_sessions_act" + delimiter + "median_sessions_time" 
							+ delimiter + "median_sessions_self_assesment" + delimiter + "median_sessions_example_lines"
							+ delimiter + "topics_covered" 
							+ delimiter + "parsons_topics_covered" + delimiter + "pcex_topics_covered" + delimiter + "sqlknot_topics_covered"
							+ delimiter + "question_attempts" + delimiter + "question_attempts_success" + delimiter
							+ "questions_dist" + delimiter + "questions_dist_success"
							+ delimiter + "questions_sucess_first_attempt"
							+ delimiter + "questions_sucess_second_attempt"
							+ delimiter + "questions_sucess_third_attempt"
							+ delimiter + "sql_knot_attempts" 
							+ delimiter + "sql_lab_attempts" + delimiter
							+ "sqlknot_success_first_attempt" + delimiter
							+ "sqlknot_success_second_attempt" + delimiter
							+ "sqlknot_success_third_attempt"
							+ delimiter + "examples_dist" + delimiter + "example_lines_actions"
							+ delimiter + "animated_examples_dist" + delimiter
							+ "animated_example_lines_actions" + delimiter);
							
					outputBuilder.append("parsons_attempts" + delimiter + "parsons_attempts_success" 
							+ delimiter + "parsons_dist" + delimiter + "parsons_dist_success" + delimiter
							 + "parsons_success_first_attempt" + delimiter
							 + "parsons_success_second_attempt" + delimiter
							 + "parsons_success_third_attempt" + delimiter 
							 
							 + "parsons_success_first_attempt_first_half" + delimiter
							 + "parsons_success_second_attempt_first_half" + delimiter
							 + "parsons_success_third_attempt_first_half" + delimiter 
							
							 + "parsons_success_first_attempt_second_half" + delimiter
							 + "parsons_success_second_attempt_second_half" + delimiter
							 + "parsons_success_third_attempt_second_half" + delimiter
							
							);
					
					outputBuilder.append("lesslet_attempts" + delimiter + "lesslet_attempts_success" 
							+ delimiter + "lesslet_dist" + delimiter + "lesslet_dist_success" + delimiter 
							+"lesslet_description_seen" + delimiter + "lesslet_dist_description_seen" + delimiter
							+ "lesslet_examples_seen" + delimiter + "lesslet_dist_example_seen" + delimiter);
					
					
					outputBuilder.append("pcrs_attempts" + delimiter + "pcrs_attempts_success" 
							+ delimiter + "pcrs_dist" + delimiter + "pcrs_dist_success" 
							+ delimiter + "pcrs_success_first_attempt" + delimiter + "pcrs_success_second_attempt"
							+ delimiter + "pcrs_success_third_attempt"
							+ delimiter);
					
					outputBuilder.append("sqltutor_attempts" + delimiter + "sqltutor_attempts_success" 
							+ delimiter + "sqltutor_dist" + delimiter + "sqltutor_dist_success" + delimiter);
					
					outputBuilder.append("pcex_completed_set" + delimiter + "pcex_ex_dist_seen" + delimiter + "pcex_ch_attempts" + delimiter + "pcex_ch_attempts_success" 
							+ delimiter + "pcex_ch_dist" + delimiter + "pcex_ch_success" 
							+ delimiter + "pcex_success_first_attempt" 
							+ delimiter + "pcex_success_second_attempt"
							+ delimiter + "pcex_success_third_attempt"
							
							+ delimiter + "pcex_success_first_attempt_first_half" 
							+ delimiter + "pcex_success_second_attempt_first_half"
							+ delimiter + "pcex_success_third_attempt_first_half"
							
							+ delimiter + "pcex_success_first_attempt_second_half" 
							+ delimiter + "pcex_success_second_attempt_second_half"
							+ delimiter + "pcex_success_third_attempt_second_half"
							
							+ delimiter);
									
					outputBuilder.append("mg_total_loads" + delimiter + "mg_topic_cell_clicks"
							+ delimiter + "mg_topic_cell_clicks_me" + delimiter
							+ "mg_topic_cell_clicks_grp" + delimiter
							+ "mg_topic_cell_clicks_mevsgrp" + delimiter
							+ "mg_activity_cell_clicks" + delimiter
							+ "mg_activity_cell_clicks_me" + delimiter
							+ "mg_activity_cell_clicks_grp" + delimiter
							+ "mg_activity_cell_clicks_mevsgrp" + delimiter);
					outputBuilder.append("mg_load_rec" + delimiter + "mg_load_original" + delimiter
							+ "mg_difficulty_feedback" + delimiter
							+ "mg_change_comparison_mode" + delimiter + "mg_change_group"
							+ delimiter + "mg_change_resource_set" + delimiter
							+ "mg_load_others" + delimiter);
					outputBuilder.append("mg_grid_activity_cell_mouseover" + delimiter 
							+ "mg_grid_topic_cell_mouseover" + delimiter
							+ "mg_cm_concept_mouseover" + delimiter);
					
					// JULIO
					outputBuilder.append("mg_act_open_not_attempted" + delimiter);
					outputBuilder.append("mg_act_open_and_attempted" + delimiter);
					outputBuilder.append("mg_act_open_not_attempted_difficulty" + delimiter);
					outputBuilder.append("mg_act_open_and_attempted_difficulty" + delimiter);
					outputBuilder.append("mg_act_open_not_attempted_difficulty_early"+User.EARLY_ATT_TH + delimiter);
					outputBuilder.append("mg_act_open_and_attempted_difficulty_early"+User.EARLY_ATT_TH + delimiter);
									
					outputBuilder.append("total_durationseconds" + delimiter 
							+ "quizjet_durationseconds" + delimiter
							+ "sqlknot_durationseconds" + delimiter
							+ "sqllab_durationseconds" + delimiter
							+ "webex_durationseconds" + delimiter 
							+ "animated_example_durationseconds" + delimiter 
							+ "parsons_durationseconds" + delimiter
							+ "parsons_durationseconds_median" + delimiter 
							+ "lesslet_durationseconds" + delimiter 
							+ "lesslet_description_durationseconds" + delimiter
							+ "lesslet_example_durationseconds" + delimiter
							+ "lesslet_test_durationseconds" + delimiter
							+ "pcrs_durationseconds" + delimiter
							+ "pcrs_durationseconds_first_attempt" + delimiter
							+ "pcrs_durationseconds_second_attempt" + delimiter
							+ "pcrs_durationseconds_third_attempt" + delimiter
							+ "sqltutor_durationseconds" + delimiter
							+ "pcex_example_durationseconds" + delimiter
							+ "pcex_example_durationseconds_median" + delimiter
							+ "pcex_example_lines_durationseconds" + delimiter
							+ "pcex_challenge_durationseconds" + delimiter
							+ "pcex_challenge_durationseconds_median" + delimiter
							+ "pcex_challenge_durationseconds_first_attempt" + delimiter
							+ "pcex_challenge_durationseconds_second_attempt" + delimiter
							+ "pcex_challenge_durationseconds_third_attempt" + delimiter
							+ "pcex_control_explanations_seen" + delimiter
							+ "pcex_control_explanations_not_seen" + delimiter
							+ "mastery_grid_durationseconds");
					
					if(timeBins != null){
						//outputBuilder.append(delimiter);
						//int k = timeBins.length+1;
						for(int j = 0;j<timeBins.length+1;j++){
							outputBuilder.append(delimiter);
							outputBuilder.append("mg_bin"+j+"_act_opened_att" + delimiter 
									+ "mg_bin"+j+"_act_opened_notatt" + delimiter 
									+ "mg_bin"+j+"_act_interface" + delimiter
									+ "mg_bin"+j+"_time" + delimiter
									+ "mg_bin"+j+"_time_interface" + delimiter
									+ "mg_bin"+j+"_act_opened_att_DIFF" + delimiter
									+ "mg_bin"+j+"_act_opened_noatt_DIFF");

						}
					}
					outputBuilder.append("\n");
					
					incHeader = false; //Just add header for the first group
					
				}

				if (!groupActivity.isThereActivity()) {
					error = true;
					outputBuilder.append("no activity found");
				}
				else {
					String userName = "";
					String session = "";
					HashMap<String, User> grp_activity = groupActivity.getGrpActivity();
					String[] users_ = null;
					if (userIds != null)
						users_ = userIds;
					else
						users_ = (String[]) grp_activity.keySet().toArray(
								new String[grp_activity.keySet().size()]);
					for (String u : users_) {
						User user = null;
						if (!grp_activity.containsKey(u)) {
							outputBuilder.append(u + delimiter + groupId + delimiter + "0" + delimiter + "0"
									+ delimiter + "0" + delimiter + "0" 
									+ delimiter + "0" + delimiter + "0" + delimiter + "0" + delimiter
									+ "0" + delimiter + "0" + delimiter + "0" + delimiter + "0"
									+ delimiter + "0" + delimiter);
							
							outputBuilder.append("0" + delimiter + "0" + delimiter + "0" + delimiter + "0"
									+ delimiter);

							outputBuilder.append("0" + delimiter + "0" + delimiter + "0" + delimiter + "0"
									+ delimiter + "0" + delimiter + "0" + delimiter + "0" + delimiter
									+ "0" + delimiter + "0" + delimiter);
							outputBuilder.append("0" + delimiter + "0" + delimiter + "0" + delimiter + "0"
									+ delimiter + "0" + delimiter + "0" + delimiter + "0" + delimiter);
							outputBuilder.append("0" + delimiter + "0" + delimiter + "0" + delimiter);
							// JULIO
							outputBuilder.append("0" + delimiter + "0" + delimiter + "-1" + delimiter + "-1" + delimiter
									+ "-1" + delimiter + "-1" + delimiter);
							
							outputBuilder.append("0" + delimiter + "0" + delimiter + "0" + delimiter
									+ "0" + delimiter + "0" + delimiter + "0" + delimiter + "0");
							
							if(timeBins != null){
								//outputBuilder.append(delimiter);
								//int k = timeBins.length+1;
								for(int j = 0;j<timeBins.length+1;j++){
									outputBuilder.append(delimiter);
									outputBuilder.append("0" + delimiter 
											+ "0" + delimiter 
											+ "0" + delimiter
											+ "0" + delimiter
											+ "0" + delimiter
											+ "-1" + delimiter
											+ "-1");

								}
							}
							outputBuilder.append("\n");
							
						}else {
							user = grp_activity.get(u);
							userName = user.getUserLogin();
							outputBuilder.append(userName + delimiter + groupId + delimiter
									+ user.summary.get("sessions_dist") + delimiter
									+ user.summary.get("median_sessions_act") + delimiter
									+ user.summary.get("median_sessions_time") + delimiter
									+ user.summary.get("median_sessions_self_assesment") + delimiter
									+ user.summary.get("median_sessions_example_lines") + delimiter
									+ user.summary.get("topics_covered") + delimiter
									+ user.summary.get("parsons_topics_covered") + delimiter
									+ user.summary.get("pcex_topics_covered") + delimiter
									+ user.summary.get("sqlknot_topics_covered") + delimiter
									+ user.summary.get("question_attempts") + delimiter
									+ user.summary.get("question_attempts_success") + delimiter
									+ user.summary.get("questions_dist") + delimiter
									+ user.summary.get("questions_dist_success") + delimiter
									+ user.summary.get("questions_sucess_first_attempt") + delimiter
									+ user.summary.get("questions_sucess_second_attempt") + delimiter
									+ user.summary.get("questions_sucess_third_attempt") + delimiter
									+ user.summary.get("sql_knot_attempts") + delimiter
									+ user.summary.get("sql_lab_attempts") + delimiter
									+ user.summary.get("sqlknot_sucess_first_attempt") + delimiter
									+ user.summary.get("sqlknot_sucess_second_attempt") + delimiter
									+ user.summary.get("sqlknot_sucess_third_attempt") + delimiter
									+ user.summary.get("examples_dist") + delimiter
									+ user.summary.get("example_lines_actions") + delimiter
									+ user.summary.get("animated_examples_dist") + delimiter
									+ user.summary.get("animated_example_lines_actions") + delimiter
									+ user.summary.get("parsons_attempts") + delimiter
									+ user.summary.get("parsons_attempts_success") + delimiter
									+ user.summary.get("parsons_dist") + delimiter
									+ user.summary.get("parsons_dist_success") + delimiter
									+ user.summary.get("parsons_sucess_first_attempt") + delimiter
									+ user.summary.get("parsons_sucess_second_attempt") + delimiter
									+ user.summary.get("parsons_sucess_third_attempt") + delimiter
									
									+ user.summary.get("parsons_sucess_first_attempt_first_half") + delimiter
									+ user.summary.get("parsons_sucess_second_attempt_first_half") + delimiter
									+ user.summary.get("parsons_sucess_third_attempt_first_half") + delimiter
									
									+ user.summary.get("parsons_sucess_first_attempt_second_half") + delimiter
									+ user.summary.get("parsons_sucess_second_attempt_second_half") + delimiter
									+ user.summary.get("parsons_sucess_third_attempt_second_half") + delimiter
									
									+ user.summary.get("lesslet_attempts") + delimiter
									+ user.summary.get("lesslet_attempts_success") + delimiter
									+ user.summary.get("lesslet_dist") + delimiter
									+ user.summary.get("lesslet_dist_success") + delimiter
									+ user.summary.get("lesslet_description_seen") + delimiter
									+ user.summary.get("lesslet_dist_description_seen") + delimiter
									+ user.summary.get("lesslet_example_seen") + delimiter
									+ user.summary.get("lesslet_dist_example_seen") + delimiter
									+ user.summary.get("pcrs_attempts") + delimiter
									+ user.summary.get("pcrs_attempts_success") + delimiter
									+ user.summary.get("pcrs_dist") + delimiter
									+ user.summary.get("pcrs_dist_success") + delimiter
									+ user.summary.get("pcrs_success_first_attempt") + delimiter
									+ user.summary.get("pcrs_success_second_attempt") + delimiter
									+ user.summary.get("pcrs_success_third_attempt") + delimiter
									+ user.summary.get("sqltutor_attempts") + delimiter
									+ user.summary.get("sqltutor_attempts_success") + delimiter
									+ user.summary.get("sqltutor_dist") + delimiter
									+ user.summary.get("sqltutor_dist_success") + delimiter
									+ user.summary.get("pcex_completed_set") + delimiter
									+ user.summary.get("pcex_ex_dist_seen") + delimiter
									+ user.summary.get("pcex_ch_attempts") + delimiter
									+ user.summary.get("pcex_ch_attempts_success") + delimiter
									+ user.summary.get("pcex_ch_dist") + delimiter
									+ user.summary.get("pcex_ch_dist_success") + delimiter
									+ user.summary.get("pcex_success_first_attempt") + delimiter
									+ user.summary.get("pcex_success_second_attempt") + delimiter
									+ user.summary.get("pcex_success_third_attempt") + delimiter
									
									+ user.summary.get("pcex_sucess_first_attempt_first_half") + delimiter
									+ user.summary.get("pcex_sucess_second_attempt_first_half") + delimiter
									+ user.summary.get("pcex_sucess_third_attempt_first_half") + delimiter
									
									+ user.summary.get("pcex_sucess_first_attempt_second_half") + delimiter
									+ user.summary.get("pcex_sucess_second_attempt_second_half") + delimiter
									+ user.summary.get("pcex_sucess_third_attempt_second_half") + delimiter
									);
							
							
									
							
							outputBuilder.append(user.summary.get("mg_total_loads") + delimiter
									+ user.summary.get("mg_topic_cell_clicks") + delimiter
									+ user.summary.get("mg_topic_cell_clicks_me") + delimiter
									+ user.summary.get("mg_topic_cell_clicks_grp") + delimiter
									+ user.summary.get("mg_topic_cell_clicks_mevsgrp") + delimiter
									+ user.summary.get("mg_activity_cell_clicks") + delimiter
									+ user.summary.get("mg_activity_cell_clicks_me") + delimiter
									+ user.summary.get("mg_activity_cell_clicks_grp") + delimiter
									+ user.summary.get("mg_activity_cell_clicks_mevsgrp") + delimiter);
							outputBuilder.append(user.summary.get("mg_load_rec") + delimiter
									+ user.summary.get("mg_load_original") + delimiter
									+ user.summary.get("mg_difficulty_feedback") + delimiter
									+ user.summary.get("mg_change_comparison_mode") + delimiter
									+ user.summary.get("mg_change_group") + delimiter
									+ user.summary.get("mg_change_resource_set") + delimiter
									+ user.summary.get("mg_load_others") + delimiter);
							outputBuilder.append(user.summary.get("mg_grid_activity_cell_mouseover") + delimiter
									+ user.summary.get("mg_grid_topic_cell_mouseover") + delimiter
									+ user.summary.get("mg_cm_concept_mouseover") + delimiter);
							
							// JULIO
							outputBuilder.append(user.summary.get("mg_act_open_not_attempted") + delimiter);
							outputBuilder.append(user.summary.get("mg_act_open_and_attempted") + delimiter);
							outputBuilder.append(user.summary.get("mg_act_open_not_attempted_difficulty") + delimiter);
							outputBuilder.append(user.summary.get("mg_act_open_and_attempted_difficulty") + delimiter);
							outputBuilder.append(user.summary.get("mg_act_open_not_attempted_difficulty_early"+User.EARLY_ATT_TH) + delimiter);
							outputBuilder.append(user.summary.get("mg_act_open_and_attempted_difficulty_early"+User.EARLY_ATT_TH) + delimiter);
							
							
							
							outputBuilder.append(user.summary.get("durationseconds_total") + delimiter 
									+ user.summary.get("durationseconds_quizjet") + delimiter
									+ user.summary.get("durationseconds_sqlknot") + delimiter
									+ user.summary.get("durationseconds_sqllab") + delimiter
									+ user.summary.get("durationseconds_webex") + delimiter 
									+ user.summary.get("durationseconds_animated_example") + delimiter 
									+ user.summary.get("durationseconds_parsons") + delimiter
									+ user.summary.get("durationseconds_parsons_median") + delimiter
									+ user.summary.get("durationseconds_lesslet") + delimiter 
									+ user.summary.get("durationseconds_lesslet_description") + delimiter
									+ user.summary.get("durationseconds_lesslet_example") + delimiter
									+ user.summary.get("durationseconds_lesslet_test") + delimiter
									+ user.summary.get("durationseconds_pcrs") + delimiter
									+ user.summary.get("durationseconds_pcrs_first_attempt") + delimiter
									+ user.summary.get("durationseconds_pcrs_second_attempt") + delimiter
									+ user.summary.get("durationseconds_pcrs_third_attempt") + delimiter
									+ user.summary.get("durationseconds_sqltutor") + delimiter
									+ user.summary.get("durationseconds_pcex_ex") + delimiter
									+ user.summary.get("durationseconds_pcex_ex_median") + delimiter
									+ user.summary.get("durationseconds_pcex_ex_lines") + delimiter
									+ user.summary.get("durationseconds_pcex_ch") + delimiter
									+ user.summary.get("durationseconds_pcex_ch_median") + delimiter
									+ user.summary.get("durationseconds_pcex_ch_first_attempt") + delimiter
									+ user.summary.get("durationseconds_pcex_ch_second_attempt") + delimiter
									+ user.summary.get("durationseconds_pcex_ch_third_attempt") + delimiter
									+ user.summary.get("durationseconds_pcex_control_explanations") + delimiter
									+ user.summary.get("durationseconds_pcex_control_no_explanations") + delimiter
									+ user.summary.get("durationseconds_mastery_grid"));
							
							if(timeBins != null){
								//outputBuilder.append(delimiter);
								//int k = timeBins.length+1;
								for(int j = 0;j<timeBins.length+1;j++){
									outputBuilder.append(delimiter);
									outputBuilder.append(user.summary.get("mg_bin"+j+"_act_opened_att") + delimiter 
											+ user.summary.get("mg_bin"+j+"_act_opened_notatt") + delimiter 
											+ user.summary.get("mg_bin"+j+"_act_interface") + delimiter
											+ user.summary.get("mg_bin"+j+"_time") + delimiter
											+ user.summary.get("mg_bin"+j+"_time_interface")  + delimiter
											+ user.summary.get("mg_bin"+j+"_act_opened_att_DIFF")  + delimiter
											+ user.summary.get("mg_bin"+j+"_act_opened_notatt_DIFF"));

								}
							}
							outputBuilder.append("\n");
							
						}
					}
				}
			}
		}
		
		out.print(outputBuilder.toString());
		out.flush();
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
