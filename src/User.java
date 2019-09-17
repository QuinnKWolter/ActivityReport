import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class User {
	static int EARLY_ATT_TH = 15;
	private int userId;
	private String userLogin;
	private ArrayList<LoggedActivity> activity;
	
	private ArrayList<Sequence> sequences;
	
	
	private ArrayList<Example> exampleActs;
	private ArrayList<Topic> topicActs;
	private ArrayList<Question> questionActs;
	private ArrayList<Line> lineActs;

	public HashMap<String, Double> summary = new HashMap<String, Double>();
	private Map<Integer, PCEXChallengeSetProgress> pcexChallengeCompletionMap;

	public ArrayList<Question> getQuestionActs() {
		return questionActs;
	}

	public void setQuestionActs(ArrayList<Question> questionActs) {
		this.questionActs = questionActs;
	}

	public ArrayList<Line> getLineAct() {
		return lineActs;
	}

	public void setLineAct(ArrayList<Line> lineAct) {
		this.lineActs = lineAct;
	}

	public ArrayList<Example> getExampleActs() {
		return exampleActs;
	}

	public void setExampleActs(ArrayList<Example> exampleActs) {
		this.exampleActs = exampleActs;
	}

	public ArrayList<Topic> getTopicActs() {
		return topicActs;
	}

	public void setTopicActs(ArrayList<Topic> topicActs) {
		this.topicActs = topicActs;
	}

	public void setActivity(ArrayList<LoggedActivity> activity) {
		this.activity = activity;
	}

	public User(int userId, String userLogin) {
		this.userId = userId;
		this.userLogin = userLogin;
		activity = new ArrayList<LoggedActivity>();
		sequences = null;
	}

	public void addLoggedActivity(LoggedActivity act) {
		this.activity.add(act);
	}

	public ArrayList<LoggedActivity> getActivity() {
		return activity;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}
	
	
	public ArrayList<Sequence> getSequences() {
		return sequences;
	}

	public void setSequences(ArrayList<Sequence> sequences) {
		this.sequences = sequences;
	}

	public void generateSessionIds(int thresholdMins){
		if (getActivity() != null && getActivity().size() > 0) {
			int sessionId = 0;
			
			LoggedActivity currentAct;
			LoggedActivity previousAct = getActivity().get(0);
			previousAct.setSession(sessionId+"");
			for (int i = 1; i < getActivity().size(); i++) {
				currentAct = getActivity().get(i);
				Long diff = (currentAct.getDate().getTime() - previousAct.getDate().getTime());
				if(diff > thresholdMins*60*1000){
					sessionId++;					
				}
				
				currentAct.setSession(sessionId+"");
				previousAct = currentAct;	
			}
		}
	}
	
	public void computeActivityTimes() {
		if (getActivity() != null && getActivity().size() > 0) {
			double duration = 0.0;
			LoggedActivity previousAct = getActivity().get(0);
			previousAct.setTime(0.0);
			LoggedActivity currentAct;
			String session = "";
			int att = 0;
			for (int i = 1; i < getActivity().size(); i++) {
				currentAct = getActivity().get(i);
				
				if(currentAct.getAppId() == Common.MASTERY_GRIDS && currentAct.getActivityName().equals("activity-done")) {
					continue;
				}
					
				if (currentAct.getSession().equalsIgnoreCase(previousAct.getSession())) {
					if (currentAct.getAppId() == Common.WEBEX) {
						if ((i + 1) < getActivity().size()) {
							LoggedActivity nextAct = getActivity().get(i + 1);
							if (currentAct.getSession().equalsIgnoreCase(nextAct.getSession()) && (nextAct.getAppId() == Common.WEBEX || nextAct.getAppId() == -1))
								duration = (nextAct.getDate().getTime() - currentAct.getDate().getTime()) / 1000.0;
							else {
								duration = 0.0;
							}
						}
						else {
							duration = 0.0;
						}
					} else {
						duration = (currentAct.getDate().getTime() - previousAct.getDate().getTime()) / 1000.0;
					}
				}
				else {
					duration = 0.0;
				}
				
				currentAct.setTime(duration);
				previousAct = currentAct;

				if (session.equals("")) {
					att++;
					session = currentAct.getSession();
				}
				else if (session.equalsIgnoreCase(currentAct.getSession())) {
					att++;
					// session = a.getSession();
				}
				else {
					att = 1;
					session = currentAct.getSession();
				}
				currentAct.setSessionActNo(att);
			}
		}
	}

	public void computeAttemptNo() {
		if (activity == null || activity.size() == 0)
			return;
		
		Map<Integer, Integer> activityAttempNoMap = new HashMap<Integer, Integer>();
		
		activity.stream().filter(act -> act.getLogType() == LogType.UM)
			.forEach(act -> {
				activityAttempNoMap.compute(act.getActivityId(), (k, v) -> (v==null)? 1: v + 1);
				act.setAttemptNo(activityAttempNoMap.get(act.getActivityId()));
			});
	}

	// DEPRECATED
	// see the method computeAttemptNo() above that does the whole process.
	public int getAttemptNo(int activityid) {
		int atmpt = 0;
		for (LoggedActivity a : activity) {
			if (a.getActivityId() == activityid)
				atmpt++;
		}
		return atmpt;
	}

	public void calculateExampleAttemptsDEPRECATED() {
		exampleActs = new ArrayList<Example>();
		Map<String, Integer> lineMap;
		Example e;
		String act;
		for (LoggedActivity a : activity) {
			if (a.getAppId() == 3) // example activity
			{
				act = a.getActivityName();
				e = getExample(a.getParentName());
				if (e == null) {
					lineMap = new HashMap<String, Integer>();
					lineMap.put(act, 1);
					e = new Example(a.getParentName(), lineMap);
					exampleActs.add(e);
				}
				else {
					lineMap = e.getLineAct();
					if (lineMap.containsKey(act) == true)
						lineMap.put(act, lineMap.get(act) + 1);
					else
						lineMap.put(act, 1);
				}
			}
		}
	}

	public void calculateTopicAttemptsDEPRECATED() {
		topicActs = new ArrayList<Topic>();
		Topic t;
		for (LoggedActivity a : activity) {
			if (a.getAppId() == 25 || a.getAppId() == 41) // question activity
			{
				t = getTopic(a.getParent());
				if (t == null) {
					t = new Topic(a.getParent(), a.getResult(), 1);
					topicActs.add(t);
				}
				else {
					t.setAtmpt(t.getAtmpt() + 1);
					if (a.getResult() == 1.0)
						t.setSuccessNo(t.getSuccessNo() + 1);
				}
			}
		}
	}

	private Example getExample(String id) {
		for (Example e : exampleActs) {
			if (e.getId() == id) {
				return e;
			}
		}
		return null;
	}

	private Topic getTopic(int id) {
		for (Topic t : topicActs) {
			if (t.getId() == id) {
				return t;
			}
		}
		return null;
	}

	public ArrayList<Question> getQuestionAct() {
		return questionActs;
	}

	public void calculateQuestionAttempts() {
		questionActs = new ArrayList<Question>();
		Question q;
		for (LoggedActivity a : activity) {
			if (a.getAppId() == 25 || a.getAppId() == 41) // question activity
			{
				q = getQuestion(a.getActivityId());
				if (q == null) {
					q = new Question(a.getActivityId(), a.getResult(), 1);
					questionActs.add(q);
				}
				else {
					q.setTotalAttempt(q.getTotalAttempt() + 1);
					if (a.getResult() == 1.0)
						q.setSuccessNo(q.getSuccessNo() + 1);
				}
			}
		}
	}

	private Question getQuestion(int activityId) {
		for (Question q : questionActs) {
			if (q.getId() == activityId) {
				return q;
			}
		}
		return null;
	}

	public void calculateLineAttempts() {
		lineActs = new ArrayList<Line>();
		Line l;
		for (LoggedActivity a : activity) {
			if (a.getAppId() == 3) // example activity
			{
				l = getLine(a.getActivityId());
				if (l == null) {
					l = new Line(a.getActivityId(), 1);
					lineActs.add(l);
				}
				else {
					l.setClicks(l.getClicks() + 1);
				}
			}
		}

	}

	private Line getLine(int activityId) {
		for (Line l : lineActs) {
			if (l.getId() == activityId) {
				return l;
			}
		}
		return null;
	}
	
	// 0: opened but not attempted, 
	// 1: opened and attempted, 
	// 2: mean difficulty of opened but not attempted, 
	// 3: mean difficulty of open and attempted
	// 4: mean difficulty of FIRST diffCountTh opened but not attempted, 
    // 5: mean difficulty of FIRST diffCountTh open and attempted
	public double[] countOpenedNotAttempted(int diffCountTh){
		double[] r = new double[6];
		int cNotAtt = 0;
		int cAtt = 0;
		double sumDiffNotAtt = 0.0;
		double sumDiffAtt = 0.0;
		int countNotAttBfTh = 0;
		int countAttBfTh = 0;
		double sumDiffNotAttBfTh = 0.0;
		double sumDiffAttBfTh = 0.0;
		for (int i=0;i<activity.size()-1;i++) {
			LoggedActivity a = activity.get(i);
			if(a.getActivityName().equalsIgnoreCase("grid-activity-cell-select")){
				// search for an attempt following the cell select. if next attempt is another activity name, 
				// then there was no attempt to the selected cell. 
				for(int j=i+1;j<activity.size();j++){
					LoggedActivity b = activity.get(j);
					if(b.getActivityName().equalsIgnoreCase("grid-activity-cell-select")){
						i = j-1;
						cNotAtt++;
						if(a.getDifficulty() >= 0.0) sumDiffNotAtt += a.getDifficulty();
						if(countNotAttBfTh < diffCountTh){
							if(a.getDifficulty() >= 0.0) sumDiffNotAttBfTh += a.getDifficulty();
							countNotAttBfTh++;
						}
						break;
					}else if(Common.isContent(b.getAppId())){ // if it is an attempt
						if(b.getActivityName().equals(a.getTargetName())) {
							cAtt++;
							if(a.getDifficulty() >= 0.0) sumDiffAtt += a.getDifficulty();
							if(countAttBfTh < diffCountTh){
								if(a.getDifficulty() >= 0.0) sumDiffAttBfTh += a.getDifficulty();
								countAttBfTh++;
							}
						}
						else {
							cNotAtt++;
							if(a.getDifficulty() >= 0.0) sumDiffNotAtt += a.getDifficulty();
							if(countNotAttBfTh < diffCountTh){
								if(a.getDifficulty() >= 0.0) sumDiffNotAttBfTh += a.getDifficulty();
								countNotAttBfTh++;
							}
						}
						break;
					}

				}
			}
		}
		r[0] = cNotAtt;
		r[1] = cAtt;
		r[2] = (cNotAtt > 0 ? sumDiffNotAtt / cNotAtt : -1 );
		r[3] = (cAtt > 0 ? sumDiffAtt / cAtt : -1 );
		r[4] = (countNotAttBfTh > 0 ? sumDiffNotAttBfTh / countNotAttBfTh : -1 );
		r[5] = (countAttBfTh > 0 ? sumDiffAttBfTh / countAttBfTh : -1 );
		return r;
	}
	
	// receive 1 or more time points (in unixtimestamp format) and returns an array counting activity
	// performed between these time points. Returned array length = 1+timeBins.length
	// each array has (2nd dimension): 
	//   0 activity opened and attempted, 
	//   1 activity opened but not attempted, 
	//   2 interface activity, 
	//   3 time spent, 
	//   4 time in interface, 
	//   5 mean difficulty of activities opened and attempted
    //   6 mean difficulty of activities opened and NOT attempted
	//   7 number of sessions (DEVELOPING)
	public double[][] countActivityByTimeBins(long[] timeBins){
		double[][] r = new double[timeBins.length+1][8];
		
		for (int i=0;i<activity.size();i++) {
			LoggedActivity a = activity.get(i);
			long actTimeStamp = a.getUnixTimestamp();
			int bin = 0;
			
			
			while(bin < timeBins.length && actTimeStamp > timeBins[bin]){
				bin++;
			}
			
			r[bin][3] += a.getTime(); 
			
			if(a.getActivityName().equalsIgnoreCase("grid-activity-cell-select")){
				// search for an attempt following the cell select. if next attempt is another activity name, 
				// then there was no attempt to the selected cell. 
				for(int j=i+1;j<activity.size();j++){
					LoggedActivity b = activity.get(j);
					if(b.getActivityName().equalsIgnoreCase("grid-activity-cell-select")){
						//i = j-1;
						r[bin][1] += 1;
						if(a.getDifficulty() >= 0.0) r[bin][6] += a.getDifficulty();
						break;
					}else if(Common.isContent(b.getAppId())){ // if it is an attempt
						if(b.getActivityName().equals(a.getTargetName())) {
							//cAtt++;
							r[bin][0] += 1;
							if(a.getDifficulty() >= 0.0) r[bin][5] += a.getDifficulty();
						}
						else {
							//cNotAtt++;
							r[bin][1] += 1;
							if(a.getDifficulty() >= 0.0) r[bin][6] += a.getDifficulty();
						}
						break;
					}

				}
			}
			if(a.getAppId() == Common.MASTERY_GRIDS){
				if(!a.getAllParameters().contains("-mouseover") || a.getTime() > Common.MIN_MOUSEOVER_TIME){
					r[bin][4] += a.getTime();
					r[bin][2] += 1;
				}	
			}
		}
		
		for (int i=0;i<r.length;i++) {
			if (r[i][0] > 0) r[i][5] = r[i][5] / r[i][0]; else r[i][5] = -1;
			if (r[i][1] > 0) r[i][6] = r[i][6] / r[i][1]; else r[i][6] = -1;
		}

		return r;	
	}
	
	public double computeMedianOfSession(HashMap<String, SessionActivity> sessions, boolean actOrTime){
		if(sessions.size() == 0) return -1;
		double[] sessionActs= new double[sessions.size()];
		int i=0;
		for (Map.Entry<String, SessionActivity> entry : sessions.entrySet()) 
			sessionActs[i++] = (double) (actOrTime ? entry.getValue().countActivity() : entry.getValue().getTime());
	
		
		Arrays.sort(sessionActs);
		double median = sessionActs[sessions.size()/2];
		
		return median;
	}
	
	public double computeMedianOfSessionSelfAssesment(HashMap<String, SessionActivity> sessions){
		if(sessions.size() == 0) return -1;
		double[] sessionSelfAssesment= new double[sessions.size()];
		int i=0;
		for (Map.Entry<String, SessionActivity> entry : sessions.entrySet()) 
			sessionSelfAssesment[i++] = (double) entry.getValue().countSelfAssesment();
	
		
		Arrays.sort(sessionSelfAssesment);
		double median = sessionSelfAssesment[sessions.size()/2];
		
		return median;
	}
	
	public double computeMedianOfSessionExampleLines(HashMap<String, SessionActivity> sessions){
		if(sessions.size() == 0) return -1;
		double[] sessionLines= new double[sessions.size()];
		int i=0;
		for (Map.Entry<String, SessionActivity> entry : sessions.entrySet()) 
			sessionLines[i++] = (double) entry.getValue().countExampleLines();
	
		
		Arrays.sort(sessionLines);
		double median = sessionLines[sessions.size()/2];
		
		return median;
	}
	
	
	
//	APP_MAP = new HashMap<Integer, String>();
//	APP_MAP.put(2, "QUIZPACK");
//	APP_MAP.put(3, "WEBEX");
//	APP_MAP.put(5, "KNOWLEDGE_SEA");
//	APP_MAP.put(8, "KT");
//	APP_MAP.put(20, "QUIZGUIDE");
//	APP_MAP.put(23, "SQLKNOT");
//	APP_MAP.put(25, "QUIZJET");
//	APP_MAP.put(35, "ANIMATED_EXAMPLE");
//	APP_MAP.put(-1, "MASTERY_GRIDS");

	public void getSummary(long timeBins[]) {		
		HashMap<String, SessionActivity> sessions = new HashMap<String, SessionActivity>();
		Set<String> topics = new HashSet<String>();
		Set<String> pcex_topics = new HashSet<String>();
		Set<String> parson_topics = new HashSet<String>();
		Set<String> sqlknot_topics = new HashSet<String>();
		Set<String> questions = new HashSet<String>();
		Set<String> success_questions = new HashSet<String>();
		Set<String> examples = new HashSet<String>();
		Set<String> animated_examples = new HashSet<String>();
		Set<String> parsons = new HashSet<String>();
		Map<Integer, Map<Integer,AtomicInteger>> parsonsTopicCorrectAttemptMap = new HashMap<Integer, Map<Integer,AtomicInteger>>();
		Map<Integer,AtomicInteger> parsonsCorrectAttemptMap = new HashMap<Integer, AtomicInteger>();
		Set<String> success_parsons = new HashSet<String>();
		Set<String> lesslet = new HashSet<String>();
		Set<String> lesslet_description = new HashSet<String>();
		Set<String> lesslet_example = new HashSet<String>();
		Set<String> success_lesslet = new HashSet<String>();
		Set<String> pcrs = new HashSet<String>();
		Set<String> success_pcrs = new HashSet<String>();
		Set<String> sqltutor = new HashSet<String>();
		Set<String> success_sqltutor = new HashSet<String>();
		Map<Integer,AtomicInteger> pcrsCorrectAttemptMap = new HashMap<Integer, AtomicInteger>();
		Set<String> pcex_ex = new HashSet<String>();
		Set<String> pcex_ch = new HashSet<String>();
		Map<Integer, Map<Integer,AtomicInteger>> pcexTopicCorrectAttemptMap = new HashMap<Integer, Map<Integer,AtomicInteger>>();
		Map<Integer,AtomicInteger> pcexCorrectAttemptMap = new HashMap<Integer, AtomicInteger>();
		Set<String> success_pcex = new HashSet<String>();
		HashMap<String, Double> time_summary = new HashMap<String, Double>();
		time_summary.put("total", 0.0);
		time_summary.put("quizjet", 0.0);
		time_summary.put("sqlknot", 0.0);
		time_summary.put("sqllab", 0.0);
		time_summary.put("webex", 0.0);
		time_summary.put("animated_example", 0.0);
		time_summary.put("parsons", 0.0);
		time_summary.put("lesslet", 0.0);
		time_summary.put("lesslet_description", 0.0);
		time_summary.put("lesslet_example", 0.0);
		time_summary.put("lesslet_test", 0.0);
		time_summary.put("pcrs", 0.0);
		time_summary.put("pcrs_first_attempt", 0.0);
		time_summary.put("pcrs_second_attempt", 0.0);
		time_summary.put("pcrs_third_attempt", 0.0);
		time_summary.put("sqltutor", 0.0);
		time_summary.put("pcex_ex", 0.0);
		time_summary.put("pcex_ex_lines", 0.0);
		time_summary.put("pcex_ch", 0.0);
		time_summary.put("pcex_ch_first_attempt", 0.0);		
		time_summary.put("pcex_ch_second_attempt", 0.0);		
		time_summary.put("pcex_ch_third_attempt", 0.0);
		time_summary.put("pcex_control_explanations_shown", 0.0);
		time_summary.put("pcex_control_explanations_not_shown", 0.0);
		time_summary.put("mastery_grid", 0.0);
		HashMap<String, Integer> mg_summary = new HashMap<String, Integer>();
		mg_summary.put("mg_total_loads", 0);
		mg_summary.put("mg_topic_cell_clicks", 0);
		mg_summary.put("mg_topic_cell_clicks_me", 0);
		mg_summary.put("mg_topic_cell_clicks_grp", 0);
		mg_summary.put("mg_topic_cell_clicks_mevsgrp", 0);
		mg_summary.put("mg_activity_cell_clicks", 0);
		mg_summary.put("mg_activity_cell_clicks_me", 0);
		mg_summary.put("mg_activity_cell_clicks_grp", 0);
		mg_summary.put("mg_activity_cell_clicks_mevsgrp", 0);
		mg_summary.put("mg_load_rec", 0);
		mg_summary.put("mg_load_original", 0);
		mg_summary.put("mg_difficulty_feedback", 0);
		mg_summary.put("mg_change_comparison_mode", 0);
		mg_summary.put("mg_change_group", 0);
		mg_summary.put("mg_change_resource_set", 0);
		mg_summary.put("mg_load_others", 0);
		mg_summary.put("mg_grid_activity_cell_mouseover", 0);
		mg_summary.put("mg_grid_topic_cell_mouseover", 0);
		mg_summary.put("mg_cm_concept_mouseover", 0);
	
		int attempts = 0;
		int correct_attempts = 0;
		Map<Integer,AtomicInteger> questionsCorrectAttemptMap = new HashMap<Integer, AtomicInteger>();
		int parsons_attempts = 0;
		int parsons_correct_attempts = 0;
		int lesslet_attempts = 0;
		int lesslet_correct_attempts = 0;
		int lesslet_description_seen = 0;
		int lesslet_examples_seen = 0;
		int pcrs_attempts = 0;
		int pcrs_correct_attempts = 0;
		int sqltutor_attempts = 0;
		int sqltutor_correct_attempts = 0;
		int pcex_ch_attempts = 0;
		int pcex_ch_correct_attempts = 0;
		long pcex_completed_set = pcexChallengeCompletionMap.values().stream().filter(PCEXChallengeSetProgress::isSetCompleted).distinct().count();
		int example_lines = 0;
		int animated_example_lines = 0;
		int sql_knot_attempts = 0;
		int sql_lab_attempts = 0;
		Map<Integer,AtomicInteger> sqlKnotCorrectAttemptMap = new HashMap<Integer, AtomicInteger>();
		
		double max_webex_time = 0;
		double max_animated_time = 0;
		double max_lesslet_time = 0;
		double max_sql_knot_time = 0;
		double max_sql_lab_time = 0;
		
		for (LoggedActivity a : activity) {
			//System.out.println("~~~~~~ "+a.getTopicName() +"  "+a.getResult());
			if(sessions.get(a.getSession()) == null){
				sessions.put(a.getSession(),new SessionActivity());
			}
			SessionActivity sessionActivity = sessions.get(a.getSession());
			
			time_summary.put("total",time_summary.get("total") + a.getTime());
			sessionActivity.addTime(a.getTime());
			
			if (a.getAppId() == Common.QUIZJET || a.getAppId() == Common.QUIZPET || a.getAppId() == Common.SQLKNOT) {//Quizjet, quizpet or Sqlknot
				questions.add(a.getParentName());
				if (a.getAppId() == Common.QUIZJET || a.getAppId() == Common.QUIZPET) {
					time_summary.put("quizjet",time_summary.get("quizjet") + a.getTime());
					
					if (a.getResult() == 1.0) {
						topics.add(a.getTopicName());
						success_questions.add(a.getParentName());
						correct_attempts++;
						
						questionsCorrectAttemptMap.merge(a.getAttemptNo(), new AtomicInteger(1), (old, v) -> {
							old.incrementAndGet();
							return old;
						});
					}
				}
				else {
					if(a.getTopicName().equals("sqllab")) {
						time_summary.put("sqllab",time_summary.get("sqllab") + a.getTime());
						sql_lab_attempts++;
						if(a.getTime() > max_sql_lab_time) {
							max_sql_lab_time = a.getTime();
						}
						//SQL-LAB report res=1 when the query is correct in syntax. It does not mean that users solve the question right!
					} else {
						time_summary.put("sqlknot",time_summary.get("sqlknot") + a.getTime());
						sql_knot_attempts++;
						
						if(a.getTime() > max_sql_knot_time) {
							max_sql_knot_time = a.getTime();
						}
						
						if (a.getResult() == 1.0) {
							topics.add(a.getTopicName());
							sqlknot_topics.add(a.getTopicName());
							success_questions.add(a.getParentName());
							correct_attempts++;
							
							sqlKnotCorrectAttemptMap.merge(a.getAttemptNo(), new AtomicInteger(1), (old, v) -> {
								old.incrementAndGet();
								return old;
							});
						}
					}
				}
				attempts++;
				sessionActivity.addQuestion(a.getParentName());
			}
			else if(a.getAppId() == Common.PARSONS){
				parsons.add(a.getParentName());
				time_summary.put("parsons",time_summary.get("parsons") + a.getTime());
				parsons_attempts++;
				if (a.getResult() == 1) {
					topics.add(a.getTopicName());
					parson_topics.add(a.getTopicName());
					success_parsons.add(a.getParentName());
					parsons_correct_attempts++;
					
					AtomicInteger counter = parsonsCorrectAttemptMap.getOrDefault(a.getAttemptNo(), new AtomicInteger());
					counter.incrementAndGet();
					parsonsCorrectAttemptMap.putIfAbsent(a.getAttemptNo(), counter);
					
					
					parsonsTopicCorrectAttemptMap.putIfAbsent(a.getTopicOrder(), new HashMap<Integer,AtomicInteger>());
					parsonsTopicCorrectAttemptMap.compute(a.getTopicOrder(), (key, value) -> {
						value.putIfAbsent(a.getAttemptNo(), new AtomicInteger());
						value.compute(a.getAttemptNo(), (key2, value2) -> {
							value2.incrementAndGet();
							return value2;
						});
						
						return value;
					});
				}
				sessionActivity.addParson(a.getParentName());
			} else if(a.getAppId() == Common.SQLTUTOR){
				System.out.println(a.getParentName());
				
				sqltutor.add(a.getActivityName());
				sqltutor_attempts++;
				time_summary.put("sqltutor", time_summary.get("sqltutor") + a.getTime());
				
				if (a.getResult() == 1) {
					topics.add(a.getTopicName());
					success_sqltutor.add(a.getActivityName());
					sqltutor_correct_attempts++;
				}
				sessionActivity.addParson(a.getActivityName());
				
//				if (a.getResult() == 1) {
//					topics.add(a.getTopicName());
//					parson_topics.add(a.getTopicName());
//					success_parsons.add(a.getParentName());
//					parsons_correct_attempts++;
//					
//					AtomicInteger counter = parsonsCorrectAttemptMap.getOrDefault(a.getAttemptNo(), new AtomicInteger());
//					counter.incrementAndGet();
//					parsonsCorrectAttemptMap.putIfAbsent(a.getAttemptNo(), counter);
//					
//					
//					parsonsTopicCorrectAttemptMap.putIfAbsent(a.getTopicOrder(), new HashMap<Integer,AtomicInteger>());
//					parsonsTopicCorrectAttemptMap.compute(a.getTopicOrder(), (key, value) -> {
//						value.putIfAbsent(a.getAttemptNo(), new AtomicInteger());
//						value.compute(a.getAttemptNo(), (key2, value2) -> {
//							value2.incrementAndGet();
//							return value2;
//						});
//						
//						return value;
//					});
//				}
//				sessionActivity.addParson(a.getParentName());
			} 
			
			
			else if(a.getAppId() == Common.LESSLET){
				lesslet.add(a.getParentName());
				if(a.getTime() > max_lesslet_time) {
					max_lesslet_time = a.getTime();
				}
				
				if(a.getActivityName().endsWith("description")) {
					lesslet_description_seen++;
					lesslet_description.add(a.getActivityName());
					time_summary.put("lesslet_description", time_summary.get("lesslet_description") + a.getTime());
				} else if(a.getActivityName().endsWith("example")) {
					lesslet_examples_seen++;
					lesslet_example.add(a.getActivityName());
					time_summary.put("lesslet_example", time_summary.get("lesslet_example") + a.getTime());
				} else if(a.getActivityName().endsWith("test")) {
					lesslet_attempts++;
					time_summary.put("lesslet_test", time_summary.get("lesslet_test") + a.getTime());
				}
				
				time_summary.put("lesslet",time_summary.get("lesslet") + a.getTime());
				if (a.getResult() == 1) {
					success_lesslet.add(a.getParentName());
					lesslet_correct_attempts++;
				}
				sessionActivity.addLesslet(a.getParentName());
			} else if(a.getAppId() == Common.PCRS){
				pcrs.add(a.getParentName());
				time_summary.put("pcrs",time_summary.get("pcrs") + a.getTime());
				pcrs_attempts++;
				if (a.getResult() == 1) {
					topics.add(a.getTopicName());
					success_pcrs.add(a.getParentName());
					pcrs_correct_attempts++;
					
					AtomicInteger counter = pcrsCorrectAttemptMap.getOrDefault(a.getAttemptNo(), new AtomicInteger());
					counter.incrementAndGet();
					pcrsCorrectAttemptMap.putIfAbsent(a.getAttemptNo(), counter);
					
					switch(a.getAttemptNo()) {
						case 1:
							time_summary.compute("pcrs_first_attempt", (key, value) -> value + a.getTime());
							break;
						case 2:
							time_summary.compute("pcrs_second_attempt", (key, value) -> value + a.getTime());
							break;
						case 3: 
							time_summary.compute("pcrs_third_attempt", (key, value) -> value + a.getTime());
							break;
					}
					
				}
				sessionActivity.addQuestion(a.getParentName());
			} else if(a.getAppId() == Common.PCEX_EXAMPLE){ //PCEX example
				pcex_ex.add(a.getParentName());
				if(a.getLogType() == LogType.UM) { //Line clicks registered to UM
					time_summary.put("pcex_ex_lines",time_summary.get("pcex_ex_lines") + a.getTime());
					example_lines++;
					sessionActivity.addExample(a.getParentName());
				} else { //Retrieved from PCEX database
					time_summary.put("pcex_ex",time_summary.get("pcex_ex") + a.getTime());
					
					if(a.getLogType() == LogType.PCEX_CONTROL) {//Calculate the time spend with explanations shown or hidden
						//The only field to pass this information is the SVC parameter. Sorry!
						boolean explanationShown = Integer.parseInt(a.getSvc()) == 1;
						
						if(explanationShown) {
							time_summary.compute("pcex_control_explanations_shown", (key, value) -> value + a.getTime());
						} else {
							time_summary.compute("pcex_control_explanations_not_shown", (key, value) -> value + a.getTime());
						}
					}
				
				}
			} else if(a.getAppId() == Common.PCEX_CHALLENGE){ //PCEX challenge
				pcex_ch.add(a.getParentName());
				time_summary.put("pcex_ch",time_summary.get("pcex_ch") + a.getTime());
				
				//Not retrieved from PCEX tracking database. Those tracking records are just for presenting the challenges to the user. 
				//Actual attempts are stored in UM and in another table of PCEX database. So, should not increase the number of attempts.
				if(a.getLogType() == LogType.UM) { 
					pcex_ch_attempts++;
					sessionActivity.addChallenge(a.getParentName());
				} else if(a.getLogType() == LogType.PCEX_CONTROL) {//Calculate the time spend with explanations shown or hidden
					//The only field to pass this information is the SVC parameter. Sorry!
					boolean explanationShown = Integer.parseInt(a.getSvc()) == 1;
					
					if(explanationShown) {
						time_summary.compute("pcex_control_explanations_shown", (key, value) -> value + a.getTime());
					} else {
						time_summary.compute("pcex_control_explanations_not_shown", (key, value) -> value + a.getTime());
					}
					
					//If this is a record from control group, the topic covered should not be based on success (they do not have challenge to submit)
					topics.add(a.getTopicName());
					pcex_topics.add(a.getTopicName());
				}
				
				if (a.getResult() == 1) {
					topics.add(a.getTopicName());
					pcex_topics.add(a.getTopicName());
					success_pcex.add(a.getParentName());
					pcex_ch_correct_attempts++;
					
					AtomicInteger counter = pcexCorrectAttemptMap.getOrDefault(a.getAttemptNo(), new AtomicInteger());
					counter.incrementAndGet();
					pcexCorrectAttemptMap.putIfAbsent(a.getAttemptNo(), counter);
					
					pcexTopicCorrectAttemptMap.putIfAbsent(a.getTopicOrder(), new HashMap<Integer,AtomicInteger>());
					pcexTopicCorrectAttemptMap.compute(a.getTopicOrder(), (key, value) -> {
						value.putIfAbsent(a.getAttemptNo(), new AtomicInteger());
						value.compute(a.getAttemptNo(), (key2, value2) -> {
							value2.incrementAndGet();
							return value2;
						});
						
						return value;
					});
					
					switch(a.getAttemptNo()) {
					case 1:
						time_summary.compute("pcex_ch_first_attempt", (key, value) -> value + a.getTime());
						break;
					case 2:
						time_summary.compute("pcex_ch_second_attempt", (key, value) -> value + a.getTime());
						break;
					case 3: 
						time_summary.compute("pcex_ch_third_attempt", (key, value) -> value + a.getTime());
						break;
					}
				}
			} else if (a.getAppId() == Common.WEBEX) {//Webex
				examples.add(a.getParentName());
				if(a.getTime() > max_webex_time) {
					max_webex_time = a.getTime();
				}
				
				time_summary.put("webex",time_summary.get("webex") + a.getTime());
				example_lines++;
				sessionActivity.addExample(a.getParentName());
				
				topics.add(a.getTopicName()); //Just for previous year SQL courses
			}
			else if (a.getAppId() == Common.ANIMATED_EXAMPLE) {//ANIMATED_EXAMPLE
				animated_examples.add(a.getParentName());
				if(a.getTime() > max_animated_time) {
					max_animated_time = a.getTime();
				}
				
				time_summary.put("animated_example",time_summary.get("animated_example") + a.getTime());
				animated_example_lines++;
				sessionActivity.addAnimation(a.getParentName());
				
				topics.add(a.getTopicName()); //Just for previous year SQL courses
			}
			else if (a.getAppId() == Common.MASTERY_GRIDS) {//MASTERY_GRIDS
				time_summary.put("mastery_grid",time_summary.get("mastery_grid") + a.getTime());
				String action = a.getActivityName();// action
				String grid_name = a.getParentName();
				if (action.contains("data-load-end"))
					mg_summary
							.put("mg_total_loads", mg_summary.get("mg_total_loads") + 1);
				else if (action.contains("grid-topic-cell-select")) {
					mg_summary.put("mg_topic_cell_clicks",
							mg_summary.get("mg_topic_cell_clicks") + 1);
					if (grid_name.contains("me vs grp"))
						mg_summary.put("mg_topic_cell_clicks_mevsgrp",
								mg_summary.get("mg_topic_cell_clicks_mevsgrp") + 1);
					else if (grid_name.contains("me"))
						mg_summary.put("mg_topic_cell_clicks_me",
								mg_summary.get("mg_topic_cell_clicks_me") + 1);
					else if (grid_name.contains("grp"))
						mg_summary.put("mg_topic_cell_clicks_grp",
								mg_summary.get("mg_topic_cell_clicks_grp") + 1);
				}
				// HERE
				else if (action.contains("grid-activity-cell-select")) {
					mg_summary.put("mg_activity_cell_clicks",
							mg_summary.get("mg_activity_cell_clicks") + 1);
					if (grid_name.contains("me vs grp"))
						mg_summary.put("mg_activity_cell_clicks_mevsgrp",
								mg_summary.get("mg_activity_cell_clicks_mevsgrp") + 1);
					else if (grid_name.contains("me"))
						mg_summary.put("mg_activity_cell_clicks_me",
								mg_summary.get("mg_activity_cell_clicks_me") + 1);
					else if (grid_name.contains("grp"))
						mg_summary.put("mg_activity_cell_clicks_grp",
								mg_summary.get("mg_activity_cell_clicks_grp") + 1);
				}
				else if (action.contains("activity-load-recommended"))
					mg_summary.put("mg_load_rec", mg_summary.get("mg_load_rec") + 1);
				else if (action.contains("activity-load-original"))
					mg_summary.put("mg_load_original",
							mg_summary.get("mg_load_original") + 1);
				else if (action.contains("activity-feedback-set-difficulty"))
					mg_summary.put("mg_difficulty_feedback",
							mg_summary.get("mg_difficulty_feedback") + 1);
				else if (action.contains("comparison-mode-set"))
					mg_summary.put("mg_change_comparison_mode",
							mg_summary.get("mg_change_comparison_mode") + 1);
				else if (action.contains("group-set"))
					mg_summary.put("mg_change_group",
							mg_summary.get("mg_change_group") + 1);
				else if (action.contains("resource-set"))
					mg_summary.put("mg_change_resource_set",
							mg_summary.get("mg_change_resource_set") + 1);
				else if (action.contains("load-others-list"))
					mg_summary.put("mg_load_others", mg_summary.get("mg_load_others") + 1);
				else if (action.contains("grid-activity-cell-mouseover") && a.getTime() > Common.MIN_MOUSEOVER_TIME)
					mg_summary.put("mg_grid_activity_cell_mouseover", mg_summary.get("mg_grid_activity_cell_mouseover") + 1);
				else if (action.contains("grid-topic-cell-mouseover") && a.getTime() > Common.MIN_MOUSEOVER_TIME)
					mg_summary.put("mg_grid_topic_cell_mouseover", mg_summary.get("mg_grid_topic_cell_mouseover") + 1);
				else if (action.contains("cm-concept-mouseover") && a.getTime() > Common.MIN_MOUSEOVER_TIME)
					mg_summary.put("mg_cm_concept_mouseover", mg_summary.get("mg_cm_concept_mouseover") + 1);
//				
			}
		}
		
		double exampleMedian = 0;
		double challengeMedian = 0;
		double parsonMedian = 0;
		
		Map<Integer, List<LoggedActivity>> collect = getActivity().stream().collect(Collectors.groupingBy(LoggedActivity::getAppId));
		List<LoggedActivity> exampleList = collect.get(46);
		
		if(exampleList != null && exampleList.size() > 0) {
			List<Double> exampleTimes = exampleList.stream()
					.collect(Collectors.groupingBy(LoggedActivity::getParentName, Collectors.summingDouble(LoggedActivity::getTime)))
					.values().stream().sorted().collect(Collectors.toList());
			
			exampleMedian = exampleTimes.get(exampleTimes.size()/2);
			if(exampleTimes.size()%2 == 0) exampleMedian = (exampleMedian + exampleTimes.get(exampleTimes.size()/2-1)) / 2;
		}
		
		
		List<LoggedActivity> challengeList = collect.get(47);
		if(challengeList != null && challengeList.size() > 0) {
			List<Double> chTimes = challengeList.stream()
					.collect(Collectors.groupingBy(LoggedActivity::getParentName, Collectors.summingDouble(LoggedActivity::getTime)))
					.values().stream().sorted().collect(Collectors.toList());
			
			challengeMedian = chTimes.get(chTimes.size()/2);
			if(chTimes.size()%2 == 0) challengeMedian = (challengeMedian + chTimes.get(chTimes.size()/2-1)) / 2;
			
		}
		
		List<LoggedActivity> parsonsList = collect.get(38);
		if(parsonsList != null && parsonsList.size() > 0) {
			List<Double> parsonTimes = parsonsList.stream()
					.collect(Collectors.groupingBy(LoggedActivity::getParentName, Collectors.summingDouble(LoggedActivity::getTime)))
					.values().stream().sorted().collect(Collectors.toList());
			
			parsonMedian = parsonTimes.get(parsonTimes.size()/2);
			if(parsonTimes.size()%2 == 0) parsonMedian = (parsonMedian + parsonTimes.get(parsonTimes.size()/2-1)) / 2;
		}
		
		List<Map<Integer,Integer>> pcexFirstSecondHalfTopicBasedSuccess = computeFirstSecondHalfTopicBasedSuccess(pcexTopicCorrectAttemptMap);
		List<Map<Integer,Integer>> parsonsFirstSecondHalfTopicBasedSuccess = computeFirstSecondHalfTopicBasedSuccess(parsonsTopicCorrectAttemptMap);
		
		
		System.out.println(topics.size());
		System.out.println("max_webex_time: " + max_webex_time);
		System.out.println("max_animated_time: " + max_animated_time);
		System.out.println("max_lesslet_time: " + max_lesslet_time);
		System.out.println("max_sql_knot_time: " + max_sql_knot_time);
		System.out.println("max_sql_lab_time: " + max_sql_lab_time);
		
		double medianSessionAct = computeMedianOfSession(sessions,true);
		double medianSessionTime = computeMedianOfSession(sessions,false);
		
		if(userLogin.equals("pene")) {
			System.out.println();
		}
		
		double medianSessionSelfAssesment = computeMedianOfSessionSelfAssesment(sessions);
		double medianSessionExamples = computeMedianOfSessionExampleLines(sessions);
		
		summary.put("sessions_dist", (double) sessions.size());
		summary.put("median_sessions_act", medianSessionAct);
		summary.put("median_sessions_time", medianSessionTime);
		summary.put("median_sessions_self_assesment", medianSessionSelfAssesment);
		summary.put("median_sessions_example_lines", medianSessionExamples);
		summary.put("topics_covered",  (double) topics.size());
		summary.put("parsons_topics_covered",  (double) parson_topics.size());
		summary.put("pcex_topics_covered",  (double) pcex_topics.size());
		summary.put("sqlknot_topics_covered", (double)sqlknot_topics.size());
		summary.put("question_attempts",  (double) attempts);
		summary.put("question_attempts_success",  (double) correct_attempts);
		summary.put("questions_dist",  (double) questions.size());
		summary.put("questions_dist_success",  (double) success_questions.size());
		
		summary.put("questions_sucess_first_attempt",  (double) questionsCorrectAttemptMap.getOrDefault(1, new AtomicInteger()).get());
		summary.put("questions_sucess_second_attempt",  (double) questionsCorrectAttemptMap.getOrDefault(2, new AtomicInteger()).get());
		summary.put("questions_sucess_third_attempt",  (double) questionsCorrectAttemptMap.getOrDefault(3, new AtomicInteger()).get());
		
		summary.put("sql_knot_attempts",  (double) sql_knot_attempts);
		summary.put("sql_lab_attempts",  (double) sql_lab_attempts);
		
		summary.put("sqlknot_sucess_first_attempt",  (double) sqlKnotCorrectAttemptMap.getOrDefault(1, new AtomicInteger()).get());
		summary.put("sqlknot_sucess_second_attempt",  (double) sqlKnotCorrectAttemptMap.getOrDefault(2, new AtomicInteger()).get());
		summary.put("sqlknot_sucess_third_attempt",  (double) sqlKnotCorrectAttemptMap.getOrDefault(3, new AtomicInteger()).get());
				
		summary.put("examples_dist",  (double) examples.size());
		summary.put("example_lines_actions",  (double) example_lines);
		summary.put("animated_examples_dist",  (double) animated_examples.size());
		summary.put("animated_example_lines_actions",  (double) animated_example_lines);
		summary.put("parsons_attempts",  (double) parsons_attempts);
		summary.put("parsons_attempts_success",  (double) parsons_correct_attempts);
		summary.put("parsons_dist",  (double) parsons.size());
		summary.put("parsons_dist_success",  (double) success_parsons.size());
		summary.put("parsons_sucess_first_attempt",  (double) parsonsCorrectAttemptMap.getOrDefault(1, new AtomicInteger()).get());
		summary.put("parsons_sucess_second_attempt",  (double) parsonsCorrectAttemptMap.getOrDefault(2, new AtomicInteger()).get());
		summary.put("parsons_sucess_third_attempt",  (double) parsonsCorrectAttemptMap.getOrDefault(3, new AtomicInteger()).get());
		
		summary.put("parsons_sucess_first_attempt_first_half",  parsonsFirstSecondHalfTopicBasedSuccess.get(0).getOrDefault(1, 0).doubleValue());
		summary.put("parsons_sucess_second_attempt_first_half",  parsonsFirstSecondHalfTopicBasedSuccess.get(0).getOrDefault(2, 0).doubleValue());
		summary.put("parsons_sucess_third_attempt_first_half",  parsonsFirstSecondHalfTopicBasedSuccess.get(0).getOrDefault(3, 0).doubleValue());
		
		summary.put("parsons_sucess_first_attempt_second_half",  parsonsFirstSecondHalfTopicBasedSuccess.get(1).getOrDefault(1, 0).doubleValue());
		summary.put("parsons_sucess_second_attempt_second_half",  parsonsFirstSecondHalfTopicBasedSuccess.get(1).getOrDefault(2, 0).doubleValue());
		summary.put("parsons_sucess_third_attempt_second_half",  parsonsFirstSecondHalfTopicBasedSuccess.get(1).getOrDefault(3, 0).doubleValue());
		
		summary.put("lesslet_attempts",  (double) lesslet_attempts);
		summary.put("lesslet_attempts_success",  (double) lesslet_correct_attempts);
		summary.put("lesslet_dist",  (double) lesslet.size());
		summary.put("lesslet_dist_success",  (double) success_lesslet.size());
		summary.put("lesslet_description_seen",  (double) lesslet_description_seen);
		summary.put("lesslet_dist_description_seen",  (double) lesslet_description.size());
		summary.put("lesslet_example_seen",  (double) lesslet_examples_seen);
		summary.put("lesslet_dist_example_seen",  (double) lesslet_example.size());
		
		summary.put("pcrs_attempts",  (double) pcrs_attempts);
		summary.put("pcrs_attempts_success",  (double) pcrs_correct_attempts);
		summary.put("pcrs_dist",  (double) pcrs.size());
		summary.put("pcrs_dist_success",  (double) success_pcrs.size());
		summary.put("pcrs_success_first_attempt",  (double) pcrsCorrectAttemptMap.getOrDefault(1, new AtomicInteger()).get());
		summary.put("pcrs_success_second_attempt",  (double) pcrsCorrectAttemptMap.getOrDefault(2, new AtomicInteger()).get());
		summary.put("pcrs_success_third_attempt",  (double) pcrsCorrectAttemptMap.getOrDefault(3, new AtomicInteger()).get());
		
		summary.put("sqltutor_attempts",  (double) sqltutor_attempts);
		summary.put("sqltutor_attempts_success",  (double) sqltutor_correct_attempts);
		summary.put("sqltutor_dist",  (double) sqltutor.size());
		summary.put("sqltutor_dist_success",  (double) success_sqltutor.size());
		
		summary.put("pcex_completed_set", (double) pcex_completed_set);
		summary.put("pcex_ex_dist_seen", (double) pcex_ex.size());
		summary.put("pcex_ch_attempts",  (double) pcex_ch_attempts);
		summary.put("pcex_ch_attempts_success",  (double) pcex_ch_correct_attempts);
		summary.put("pcex_ch_dist",  (double) pcex_ch.size());
		summary.put("pcex_ch_dist_success",  (double) success_pcex.size());
		
		summary.put("pcex_success_first_attempt",  (double) pcexCorrectAttemptMap.getOrDefault(1, new AtomicInteger()).get());
		summary.put("pcex_success_second_attempt",  (double) pcexCorrectAttemptMap.getOrDefault(2, new AtomicInteger()).get());
		summary.put("pcex_success_third_attempt",  (double) pcexCorrectAttemptMap.getOrDefault(3, new AtomicInteger()).get());
		
		summary.put("pcex_sucess_first_attempt_first_half",  pcexFirstSecondHalfTopicBasedSuccess.get(0).getOrDefault(1, 0).doubleValue());
		summary.put("pcex_sucess_second_attempt_first_half",  pcexFirstSecondHalfTopicBasedSuccess.get(0).getOrDefault(2, 0).doubleValue());
		summary.put("pcex_sucess_third_attempt_first_half",  pcexFirstSecondHalfTopicBasedSuccess.get(0).getOrDefault(3, 0).doubleValue());
		
		summary.put("pcex_sucess_first_attempt_second_half",  pcexFirstSecondHalfTopicBasedSuccess.get(1).getOrDefault(1, 0).doubleValue());
		summary.put("pcex_sucess_second_attempt_second_half",  pcexFirstSecondHalfTopicBasedSuccess.get(1).getOrDefault(2, 0).doubleValue());
		summary.put("pcex_sucess_third_attempt_second_half",  pcexFirstSecondHalfTopicBasedSuccess.get(1).getOrDefault(3, 0).doubleValue());

		
		summary.put("durationseconds_total", time_summary.get("total"));
		summary.put("durationseconds_quizjet", time_summary.get("quizjet"));
		summary.put("durationseconds_sqlknot", time_summary.get("sqlknot"));
		summary.put("durationseconds_sqllab", time_summary.get("sqllab"));
		summary.put("durationseconds_webex", time_summary.get("webex"));
		summary.put("durationseconds_animated_example", time_summary.get("animated_example"));
		summary.put("durationseconds_parsons", time_summary.get("parsons"));
		summary.put("durationseconds_parsons_median", parsonMedian);
		summary.put("durationseconds_lesslet", time_summary.get("lesslet"));
		summary.put("durationseconds_lesslet_description", time_summary.get("lesslet_description"));
		summary.put("durationseconds_lesslet_example", time_summary.get("lesslet_example"));
		summary.put("durationseconds_lesslet_test", time_summary.get("lesslet_test"));
		summary.put("durationseconds_pcrs", time_summary.get("pcrs"));
		summary.put("durationseconds_pcrs_first_attempt", time_summary.get("pcrs_first_attempt"));
		summary.put("durationseconds_pcrs_second_attempt", time_summary.get("pcrs_second_attempt"));
		summary.put("durationseconds_pcrs_third_attempt", time_summary.get("pcrs_third_attempt"));
		summary.put("durationseconds_sqltutor", time_summary.get("sqltutor"));
		summary.put("durationseconds_pcex_ex", time_summary.get("pcex_ex"));
		summary.put("durationseconds_pcex_ex_median", exampleMedian);
		summary.put("durationseconds_pcex_ex_lines", time_summary.get("pcex_ex_lines"));
		summary.put("durationseconds_pcex_ch", time_summary.get("pcex_ch"));
		summary.put("durationseconds_pcex_ch_median", challengeMedian);
		summary.put("durationseconds_pcex_ch_first_attempt", time_summary.get("pcex_ch_first_attempt"));
		summary.put("durationseconds_pcex_ch_second_attempt", time_summary.get("pcex_ch_second_attempt"));
		summary.put("durationseconds_pcex_ch_third_attempt", time_summary.get("pcex_ch_third_attempt"));
		summary.put("durationseconds_pcex_control_explanations", time_summary.get("pcex_control_explanations_shown"));
		summary.put("durationseconds_pcex_control_no_explanations", time_summary.get("pcex_control_explanations_not_shown"));
		
		summary.put("durationseconds_mastery_grid", time_summary.get("mastery_grid"));
		summary.put("mg_total_loads", (double) mg_summary.get("mg_total_loads"));
		summary.put("mg_topic_cell_clicks", (double) mg_summary.get("mg_topic_cell_clicks"));
		summary.put("mg_topic_cell_clicks_me",(double) mg_summary.get("mg_topic_cell_clicks_me"));
		summary.put("mg_topic_cell_clicks_grp",(double) mg_summary.get("mg_topic_cell_clicks_grp"));
		summary.put("mg_topic_cell_clicks_mevsgrp",(double) mg_summary.get("mg_topic_cell_clicks_mevsgrp"));
		summary.put("mg_activity_cell_clicks",(double) mg_summary.get("mg_activity_cell_clicks"));
		summary.put("mg_activity_cell_clicks_me",(double) mg_summary.get("mg_activity_cell_clicks_me"));
		summary.put("mg_activity_cell_clicks_grp",(double) mg_summary.get("mg_activity_cell_clicks_grp"));
		summary.put("mg_activity_cell_clicks_mevsgrp",(double) mg_summary.get("mg_activity_cell_clicks_mevsgrp"));
		summary.put("mg_load_rec", (double) mg_summary.get("mg_load_rec"));
		summary.put("mg_load_original", (double) mg_summary.get("mg_load_original"));
		summary.put("mg_difficulty_feedback",(double) mg_summary.get("mg_difficulty_feedback"));
		summary.put("mg_change_comparison_mode",(double) mg_summary.get("mg_change_comparison_mode"));
		summary.put("mg_change_group", (double) mg_summary.get("mg_change_group"));
		summary.put("mg_change_resource_set",(double) mg_summary.get("mg_change_resource_set"));
		summary.put("mg_change_comparison_mode",(double) mg_summary.get("mg_change_comparison_mode"));
		summary.put("mg_load_others", (double) mg_summary.get("mg_load_others"));
		
		summary.put("mg_grid_activity_cell_mouseover", (double) mg_summary.get("mg_grid_activity_cell_mouseover"));
		summary.put("mg_grid_topic_cell_mouseover", (double) mg_summary.get("mg_grid_topic_cell_mouseover"));
		summary.put("mg_cm_concept_mouseover", (double) mg_summary.get("mg_cm_concept_mouseover"));
		double[] openedAttOpenedNotAtt = countOpenedNotAttempted(EARLY_ATT_TH);
		summary.put("mg_act_open_not_attempted", (double) openedAttOpenedNotAtt[0]);
		summary.put("mg_act_open_and_attempted", (double) openedAttOpenedNotAtt[1]);
		summary.put("mg_act_open_not_attempted_difficulty", (double) openedAttOpenedNotAtt[2]);
		summary.put("mg_act_open_and_attempted_difficulty", (double) openedAttOpenedNotAtt[3]);
		summary.put("mg_act_open_not_attempted_difficulty_early"+EARLY_ATT_TH, (double) openedAttOpenedNotAtt[4]);
		summary.put("mg_act_open_and_attempted_difficulty_early"+EARLY_ATT_TH, (double) openedAttOpenedNotAtt[5]);
		
		if(timeBins != null){
			double[][] byTimeBins = countActivityByTimeBins(timeBins);
			//int k = timeBins.length+1;
			for(int j = 0;j<byTimeBins.length;j++){
				summary.put("mg_bin"+j+"_act_opened_att", byTimeBins[j][0]);
				summary.put("mg_bin"+j+"_act_opened_notatt", byTimeBins[j][1]);
				summary.put("mg_bin"+j+"_act_interface", byTimeBins[j][2]);
				summary.put("mg_bin"+j+"_time", byTimeBins[j][3]);
				summary.put("mg_bin"+j+"_time_interface", byTimeBins[j][4]);
				
				summary.put("mg_bin"+j+"_act_opened_att_DIFF", byTimeBins[j][5]);
				summary.put("mg_bin"+j+"_act_opened_notatt_DIFF", byTimeBins[j][6]);

			}
		}
		
		
	}
	
private List<Map<Integer,Integer>> computeFirstSecondHalfTopicBasedSuccess(Map<Integer, Map<Integer, AtomicInteger>> topicCorrectAttemptMap) {
	Map<Boolean, List<Entry<Integer, Map<Integer, AtomicInteger>>>> topicPartition = 
			topicCorrectAttemptMap.entrySet().stream()
			.collect(Collectors.partitioningBy(entry -> entry.getKey() <= 5 ));
	
	Map<Integer, Integer> firstHalfAttemptSuccess = topicPartition.get(true).stream()
		.flatMap(entry -> entry.getValue().entrySet().stream())
		.collect(Collectors.groupingBy(Map.Entry::getKey, 
				Collectors.mapping(Map.Entry::getValue, Collectors.summingInt(AtomicInteger::get))));
	
	Map<Integer, Integer> secondHalfAttemptSuccess = topicPartition.get(false).stream()
			.flatMap(entry -> entry.getValue().entrySet().stream())
			.collect(Collectors.groupingBy(Map.Entry::getKey, 
					Collectors.mapping(Map.Entry::getValue, Collectors.summingInt(AtomicInteger::get))));
	
	return Arrays.asList(firstHalfAttemptSuccess, secondHalfAttemptSuccess);
}

//	public void getSummary(long timeBins[]){
//		
//	}
	
	/**
	 * 
	 * 
	 * @param mode	
	 * 				0: by session
	 * 				1: by question
	 * 				2: by session/question
	 * 				3: by topic
	 * @param include
	 * 				0: all
	 * 				1: only questions
	 * 				2: only examples
	 * 				3: questions and examples
	 * 				(examples means WEBEX and ANIMATED_EXAMPLES)
	 */	
	public void sequenceActivity(int mode, int include){
		sequences = new ArrayList<Sequence>();
		Sequence s = null;
		int sid = 0;
		String groupingId = "";
		for(LoggedActivity a : getActivity()){
			int app = a.getAppId();
			// decde if the activity should be added to the sequence
			boolean addActivity = 
							( 
							  (include == 0) ||
							  ((include == 1 || include == 3) && (app == Common.QUIZJET || app == Common.QUIZPET || app == Common.SQLKNOT)) ||
							  ((include == 2 || include == 3) && (app == Common.WEBEX || app == Common.ANIMATED_EXAMPLE))
							);
			
			// add the activity
			if(addActivity){
				// compute the groupingId of activity to see if should be create another sequence
				if(mode == 0) groupingId = a.getSession();
				if(mode == 1) {
					if(app == Common.QUIZJET || app == Common.QUIZPET || app == Common.SQLKNOT) groupingId = a.getActivityName();
				}
				if(mode == 2) {
					if(app == Common.QUIZJET || app == Common.QUIZPET || app == Common.SQLKNOT) groupingId = a.getSession()+a.getActivityName();
				}
				if(mode == 3) {
					groupingId = a.getTopicName();
					s = findSequence(groupingId);
				}
				// create a sequence is the current one (s) is null of the groupingId of the 
				// activity is different of the grouppingId of the current sequence. Sequence can have empty 
				// grouping id. In this case, does not create a new sequence
				if(s == null || (s!=null && mode != 3  
						&& s.getGroupingId().length() > 0 && !s.getGroupingId().equalsIgnoreCase(groupingId)) ){
					sid++;
					s = new Sequence(sid);
					sequences.add(s);
					
				}
				if(s.getGroupingId().length() == 0){
					s.setGroupingId(groupingId);
				}
				// add the activity
				s.activity.add(a);
				
				
			}
			
		}
		
	}
	public Sequence findSequence(String groupingId){
		if(sequences == null || sequences.size() == 0) return null; 
		for(Sequence s: sequences){
			if(s.getGroupingId().equalsIgnoreCase(groupingId)) return s;
		}
		return null;
	}
	
	
	public void removeSequences(int whichHalf){
		if(whichHalf < 1 || whichHalf > 2) return;
		int n = sequences.size();
		int c = n/2;
		if(n%2 == 1 && whichHalf == 1){
			c = c+1;
		}
		if(whichHalf == 1){
			for(int i=0;i<c;i++) sequences.remove(0);
		}
		if(whichHalf == 2){
			for(int i=0;i<c;i++) sequences.remove(sequences.size()-1);
		}
	}

	public void computePCEXSetCompletionRate(List<PcexSet> pcexActivitySet, boolean controlGroup) {
		pcexChallengeCompletionMap = new HashMap<Integer, PCEXChallengeSetProgress>();
		
		pcexActivitySet.stream().map(set -> new PCEXChallengeSetProgress(set)).forEach( progress -> {
			progress.getChallengeIDList().stream().forEach(id -> pcexChallengeCompletionMap.put(Integer.parseInt(id), progress));
		});
		
		getActivity().stream().filter(activity -> pcexActivityFilterForCompletionRate(activity, controlGroup))
			.forEach(activity -> pcexChallengeCompletionMap.get(activity.getActivityId()).markSolved(activity.getActivityId()));
		
	}
	
	/**
	 * If control group, all of the challenges are shown as examples. So, all results are recorded as 0. That is why need to seperate control group and other groups
	 * If not control group, need to check if the student successfully solved all the challenges. 
	 * @param pcexChallengeCompletionMap2 
	 */
	private boolean pcexActivityFilterForCompletionRate(LoggedActivity activity, boolean controlGroup) {
		
		boolean result = (controlGroup && activity.getAppId() == Common.PCEX_CHALLENGE && activity.getLogType() == LogType.UM && activity.getResult() == 0) ||  
				(!controlGroup && activity.getAppId() == Common.PCEX_CHALLENGE && activity.getLogType() == LogType.UM && activity.getResult() == 1);
		
		return result;
	}
	
}
