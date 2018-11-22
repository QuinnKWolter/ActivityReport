import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements a simple labeller in which questions are labeled 
 * by correctness and time, and examples are labeled as 'E'
 * @author pawslab
 *
 */
public class Labeller {
	public static HashMap<Integer,String> mapAppId;

	public static HashMap<String,String[]> mapActLabelsSML;

	static{
		mapAppId = new HashMap<Integer,String>();
		mapAppId.put(Common.WEBEX,"E");
		mapAppId.put(Common.QUIZJET,"Q");
		mapAppId.put(Common.QUIZPET,"Q");
		mapAppId.put(Common.SQLKNOT,"Q");
		mapAppId.put(Common.ANIMATED_EXAMPLE,"AE");
		mapAppId.put(Common.MASTERY_GRIDS,"MG");
		mapAppId.put(Common.KT,"KT");
		
		
		mapActLabelsSML = new HashMap<String,String[]>();
		
		// Single uppercase letters available
		// A C D E F G H I K L M N P Q R S T V W Y  
		
		// succeed questions s/m/l no extreme/extreme
		mapActLabelsSML.put("Q1s",				new String[]{"s","A"});
		mapActLabelsSML.put("Q1se",				new String[]{"s","C"});
		mapActLabelsSML.put("Q1m",				new String[]{"Sm","A"});
		mapActLabelsSML.put("Q1me",				new String[]{"Sm","C"});
		mapActLabelsSML.put("Q1l",				new String[]{"S","D"});
		mapActLabelsSML.put("Q1le",				new String[]{"S","E"});
		// failed questions s/m/l no extreme/extreme
		mapActLabelsSML.put("Q0s",				new String[]{"f","F"});
		mapActLabelsSML.put("Q0se",				new String[]{"f","G"});
		mapActLabelsSML.put("Q0m",				new String[]{"Fm","F"});
		mapActLabelsSML.put("Q0me",				new String[]{"Fm","G"});
		mapActLabelsSML.put("Q0l",				new String[]{"F","H"});
		mapActLabelsSML.put("Q0le",				new String[]{"F","I"});
		// succeed repeated questions s/m/l no extreme/extreme 
		mapActLabelsSML.put("Q1sr",				new String[]{"sr","A"});
		mapActLabelsSML.put("Q1sre",			new String[]{"sr","C"});
		mapActLabelsSML.put("Q1mr",				new String[]{"Smr","A"});
		mapActLabelsSML.put("Q1mre",			new String[]{"Smr","C"});
		mapActLabelsSML.put("Q1lr",				new String[]{"Sr","D"});
		mapActLabelsSML.put("Q1lre",			new String[]{"Sr","E"});
		// failed repeated questions s/m/l no extreme/extreme
		mapActLabelsSML.put("Q0sr",				new String[]{"fr","F"});
		mapActLabelsSML.put("Q0sre",			new String[]{"fr","G"});
		mapActLabelsSML.put("Q0mr",				new String[]{"Fmr","F"});
		mapActLabelsSML.put("Q0mre",			new String[]{"Fmr","G"});
		mapActLabelsSML.put("Q0lr",				new String[]{"Fr","H"});
		mapActLabelsSML.put("Q0lre",			new String[]{"Fr","I"});
		// example s/m/l no extreme/extreme
		mapActLabelsSML.put("E1s",				new String[]{"e","K"});
		mapActLabelsSML.put("E1se",				new String[]{"e","L"});
		mapActLabelsSML.put("E1m",				new String[]{"Em","K"});
		mapActLabelsSML.put("E1me",				new String[]{"Em","L"});
		mapActLabelsSML.put("E1l",				new String[]{"E","M"});
		mapActLabelsSML.put("E1le",				new String[]{"E","N"});
		// repeated example s/m/l no extreme/extreme
		mapActLabelsSML.put("E1sr",				new String[]{"er","K"});
		mapActLabelsSML.put("E1sre",			new String[]{"er","L"});
		mapActLabelsSML.put("E1mr",				new String[]{"Emr","K"});
		mapActLabelsSML.put("E1mre",			new String[]{"Emr","L"});
		mapActLabelsSML.put("E1lr",				new String[]{"Er","M"});
		mapActLabelsSML.put("E1lre",			new String[]{"Er","N"});
		// animated example s/m/l no extreme/extreme
		mapActLabelsSML.put("AE1s",				new String[]{"ae","P"});
		mapActLabelsSML.put("AE1se",			new String[]{"ae","Q"});
		mapActLabelsSML.put("AE1m",				new String[]{"AEm","P"});
		mapActLabelsSML.put("AE1me",			new String[]{"AEm","Q"});
		mapActLabelsSML.put("AE1l",				new String[]{"AE","R"});
		mapActLabelsSML.put("AE1le",			new String[]{"AE","S"});
		// animated repeated example s/m/l no extreme/extreme
		mapActLabelsSML.put("AE1sr",			new String[]{"aer","P"});
		mapActLabelsSML.put("AE1sre",			new String[]{"aer","Q"});
		mapActLabelsSML.put("AE1mr",			new String[]{"AEmr","P"});
		mapActLabelsSML.put("AE1mre",			new String[]{"AEmr","Q"});
		mapActLabelsSML.put("AE1lr",			new String[]{"AEr","R"});
		mapActLabelsSML.put("AE1lre",			new String[]{"AEr","S"});
		
		// mastery grids s/m/l no extreme/extreme
		mapActLabelsSML.put("MG1s",				new String[]{"mg","T"});
		mapActLabelsSML.put("MG1se",			new String[]{"mg","T"});
		mapActLabelsSML.put("MG1m",				new String[]{"MGm","T"});
		mapActLabelsSML.put("MG1me",			new String[]{"MGm","T"});
		mapActLabelsSML.put("MG1l",				new String[]{"MG","T"});
		mapActLabelsSML.put("MG1le",			new String[]{"MG","T"});
		// mastery grids repeated s/m/l no extreme/extreme
		mapActLabelsSML.put("MG1sr",			new String[]{"mgr","T"});
		mapActLabelsSML.put("MG1sre",			new String[]{"mgr","T"});
		mapActLabelsSML.put("MG1mr",			new String[]{"MGmr","T"});
		mapActLabelsSML.put("MG1mre",			new String[]{"MGmr","T"});
		mapActLabelsSML.put("MG1lr",			new String[]{"MGr","T"});
		mapActLabelsSML.put("MG1lre",			new String[]{"MGr","T"});
		
		// KT s/m/l no extreme/extreme
		mapActLabelsSML.put("KT1s",				new String[]{"kt","U"});
		mapActLabelsSML.put("KT1se",			new String[]{"kt","U"});
		mapActLabelsSML.put("KT1m",				new String[]{"KTm","U"});
		mapActLabelsSML.put("KT1me",			new String[]{"KTm","U"});
		mapActLabelsSML.put("KT1l",				new String[]{"KT","U"});
		mapActLabelsSML.put("KT1le",			new String[]{"KT","U"});
		// KT repeated s/m/l no extreme/extreme
		mapActLabelsSML.put("KT1sr",			new String[]{"ktr","U"});
		mapActLabelsSML.put("KT1sre",			new String[]{"ktr","U"});
		mapActLabelsSML.put("KT1mr",			new String[]{"KTmr","U"});
		mapActLabelsSML.put("KT1mre",			new String[]{"KTmr","U"});
		mapActLabelsSML.put("KT1lr",			new String[]{"KTr","U"});
		mapActLabelsSML.put("KT1lre",			new String[]{"KTr","U"});

		
	}
	
	
	private GroupActivity groupActivity;
	private String[] timeLabels = {"short","long"};
	
	public Labeller(GroupActivity groupActivity, String[] timeLabels){
		this.groupActivity = groupActivity;
		if(timeLabels != null) this.timeLabels = timeLabels;

	}
	
	public void labelSequences(boolean markSeeingBefore,boolean markSeeingBeforeSeq){
		HashMap<Integer,Integer> seenAct = null;
		for (Map.Entry<String, User> entry : groupActivity.grp_activity.entrySet()) {
			User user = entry.getValue();
			for(Sequence s : user.getSequences()){
				int c = 0;
				seenAct = new HashMap<Integer,Integer>();
				for(LoggedActivity a : s.activity){
					int id = a.getActivityId();
					boolean seenInTheSequence = true;
					if(seenAct.get(id) == null){
						seenInTheSequence = false;
						seenAct.put(id, id);
					}
					
					String[] labels = getLabels(a, (c == 0 || c == s.activity.size()-1),
							(a.getAttemptNo() != 0), seenInTheSequence, markSeeingBefore, markSeeingBeforeSeq);
					
					if(labels == null){
						a.setActLabel("X");
						a.setActLabelReadable("X");
						
					}else{
						a.setActLabel(labels[1]);
						a.setActLabelReadable(labels[0]);
					}					
					c++;		
				}
			}
			
		}

	}
	public String timeLabelToLetter(String label){
		if(label.equalsIgnoreCase(timeLabels[0])) return "s";
		if(timeLabels.length == 2){
			if(label.equalsIgnoreCase(timeLabels[1])) return "l";	
		}
		if(timeLabels.length == 3){
			if(label.equalsIgnoreCase(timeLabels[1])) return "m";			
			if(label.equalsIgnoreCase(timeLabels[2])) return "l";			
		}
		return "s";
	}
	
	public void labelTime(boolean replaceExtremeTimesByMedian){
		if(timeLabels == null || timeLabels.length < 2) return;
		
		HashMap<String,double[]> timeDist = getActivityTimeDistributions();
		boolean usingMedian = timeLabels.length == 2;
		int timeChangeCount = 0;
		for (Map.Entry<String, User> entry : groupActivity.grp_activity.entrySet()) {
			User user = entry.getValue();
			String userLogin = user.getUserLogin();
			HashMap<String,String> viewedActs = new HashMap<String,String>(); 
			
			for(LoggedActivity a : user.getActivity()){
				String actId = a.getActivityId() + "";
				double[] perc = timeDist.get(actId);
				double[] limits = new double[3];
				if(perc != null){
					if((a.getAppId() == Common.QUIZJET || a.getAppId() == Common.QUIZPET || a.getAppId() == Common.SQLKNOT)
							&& viewedActs.get(actId) == null){
						viewedActs.put(actId,actId);
						limits[0] = perc[0];
						limits[1] = perc[1];
						limits[2] = perc[2];
					}else{
						limits[0] = perc[3];
						limits[1] = perc[4];
						limits[2] = perc[5];						
					}
					if(replaceExtremeTimesByMedian){
						if(a.getTime() > Common.MAX_ACTIVITY_TIME){
							System.out.print("Time changed for "+user.getUserLogin()+" in ACT "+a.getActivityName()+ " in APP " + a.getAppId() + " TIME: "+a.getTime());
							a.setTime(limits[1] + 1.0); // reset the time to median + 1 if it is very high
							System.out.println(" -> "+a.getTime());
							timeChangeCount++;
						}
					}
					
				}
				
				if(usingMedian){
					if(a.getTime() <= limits[1]) a.setLabelTime(timeLabels[0]);
					else a.setLabelTime(timeLabels[1]);
				}else{
					if(a.getTime() <= limits[0]) a.setLabelTime(timeLabels[0]);
					else if(a.getTime() >= limits[2])  a.setLabelTime(timeLabels[2]);
					else a.setLabelTime(timeLabels[1]);
					
				}
				
			}
			
		}
		System.out.println("Total time changes: "+timeChangeCount);
	}
	

	
	/**
	 * 
	 * @return 	a hashmap where the key is the activityId and the array list contains 6 values: 
	 * 			perc 33.3 for first attempts
	 * 			perc 50.0 for first attempts
	 * 			perc 66.6 for first attempts
	 * 			perc 33.3 for next attempts
	 * 			perc 50.0 for next attempts
	 * 			perc 66.6 for next attempts
	 * 			Only for QuizJet and SQLKnot questions consider 2 distributions (first/next).
	 * 			For other kind of activity (webex, animated examples), next attempt distribution
	 * 			contains overall distributions
	 */
	public HashMap<String,double[]> getActivityTimeDistributions(){
		HashMap<String,double[]> result = new HashMap<String,double[]>();
		HashMap<String,ArrayList<Double>> tdFirstAtt = new HashMap<String,ArrayList<Double>>();
		HashMap<String,ArrayList<Double>> tdNextAtt = new HashMap<String,ArrayList<Double>>();
		// 1. for each user, go through all activity and add first and next attempts to tdFirstAtt or tdNextAtt
		for (Map.Entry<String, User> entry : groupActivity.grp_activity.entrySet()) {
			User user = entry.getValue();
			String userLogin = user.getUserLogin();
			HashMap<String,String> viewedActs = new HashMap<String,String>(); 
			
			for(LoggedActivity a : user.getActivity()){
				// only for questions and examples
				if(a.getAppId() == Common.QUIZJET  || a.getAppId() == Common.QUIZPET || a.getAppId() == Common.WEBEX 
					|| a.getAppId() == Common.ANIMATED_EXAMPLE || a.getAppId() == Common.SQLKNOT || a.getAppId() == Common.LESSLET
					|| a.getAppId() == Common.KT || a.getAppId() == Common.MASTERY_GRIDS){
					
					
					String actId = a.getActivityId() + "";
					
					// if it is the first time the activity appears adds an array for it in 
					// both first-attempt and next-attempts hash maps
					ArrayList<Double> dist1 = tdFirstAtt.get(actId);
					ArrayList<Double> dist2 = tdNextAtt.get(actId);
					if(dist1 == null){
						dist1 = new ArrayList<Double>();
						tdFirstAtt.put(actId,dist1);
					}
					if(dist2 == null){
						dist2 = new ArrayList<Double>();
						tdNextAtt.put(actId,dist2);
					}
					// if activity has duration > 0
					if(a.getTime()>0){
						// See if user has already seen this activity
						if(viewedActs.get(actId) == null){
							viewedActs.put(actId,actId);
							if(a.getAppId() == Common.QUIZJET  || a.getAppId() == Common.QUIZPET  || a.getAppId() == Common.SQLKNOT){
								dist1.add(a.getTime());
							}else{
								dist2.add(a.getTime());
							}
							
						}else{
							if(a.getAppId() == Common.QUIZJET  || a.getAppId() == Common.QUIZPET || a.getAppId() == Common.SQLKNOT) 
								dist2.add(a.getTime());
						}
						
					}
					

				}
			}
		}
		
		// 2. For each activity set of attempt times, sort, and 
		//    fill the result hash map and its arrays
		//System.out.println();
		//System.out.println("1. Distibution of time for each activity FIRST att");
		for (Map.Entry<String, ArrayList<Double>> entry : tdFirstAtt.entrySet()) {
			ArrayList<Double> dist = entry.getValue();
			Collections.sort(dist);
			//System.out.print(entry.getKey()+" "+groupActivity.um2_activityname_map.get(entry.getKey())+" ");
			//for(Double d : dist) System.out.print(Common.df.format(d)+", ");
			//System.out.println();
			
			double[] stats = result.get(entry.getKey());
			if(stats == null){
				stats = new double[6];
				result.put(entry.getKey(),stats);
			}
			double[] perc = getPercentiles(dist);
			stats[0] = perc[0];
			stats[1] = perc[1]; // median
			stats[2] = perc[2];
			
		}
		
		//System.out.println();
		//System.out.println("2. Distibution of time for each activity NEXT att");
		for (Map.Entry<String, ArrayList<Double>> entry : tdNextAtt.entrySet()) {
			ArrayList<Double> dist = entry.getValue();
			Collections.sort(dist);
			//System.out.print(entry.getKey()+" "+groupActivity.um2_activityname_map.get(entry.getKey())+" ");
			//for(Double d : dist) System.out.print(Common.df.format(d)+", ");
			//System.out.println();
			
			double[] stats = result.get(entry.getKey());
			if(stats == null){
				stats = new double[6];
				result.put(entry.getKey(),stats);
			}
			double[] perc = getPercentiles(dist);
			stats[3] = perc[0];
			stats[4] = perc[1]; // median
			stats[5] = perc[2];
		}
		//System.out.println("3. Percentiles @@@@");
//		for (Map.Entry<String, double[]> entry : result.entrySet()) {
//			System.out.print(entry.getKey()+" "+groupActivity.um2_activityname_map.get(entry.getKey())+" ");
//			System.out.println(entry.getValue()[0]+" "+entry.getValue()[1]+" "+
//					entry.getValue()[2]+" "+entry.getValue()[3]+" "+entry.getValue()[4]+" "+
//					entry.getValue()[5]);
//		}
		
		return result;
	}
	
	public static double[] getPercentiles(ArrayList<Double> dist){
		double[] r = new double[3];
		if(dist != null){
			int c = dist.size();
			if(c > 1){
				if(c % 2 == 0){
					
					r[1] = ( dist.get(c/2) + dist.get(c/2 - 1) ) / 2;
					
					
				}else{
					r[1] = dist.get(c/2);
				}
				r[0] = dist.get(((int) Math.ceil(c/3.0)) - 1);
				r[2] = dist.get(((int) Math.ceil(2*c/3.0)) - 1);
				

			}else{
				if(c > 0){
					r[0] = dist.get(0);
					r[1] = dist.get(0);
					r[2] = dist.get(0);
				}
				
			}
		}
		
		return r;
	}	
	
	
	public String[] getLabels(LoggedActivity a, boolean extreme, 
			boolean seeingBefore, boolean seeingBeforeSeq,  
			boolean markSeeingBefore, boolean markSeeingBeforeSeq){
		
		String key = mapAppId.get(a.getAppId());
		
		
		if(key == null) {
			return new String[]{"O","X"};
		}
		else{
			int res = ((int)a.getResult());
			if(res == -1) res  = 0;
			key += ((a.getAppId()==Common.QUIZJET || a.getAppId()==Common.QUIZPET || a.getAppId()==Common.SQLKNOT)?res:1);
			key += timeLabelToLetter(a.getLabelTime());
			if(markSeeingBefore) key += (seeingBefore?"r":"");
			if(markSeeingBeforeSeq) key += (seeingBeforeSeq?"r":"");
			if(extreme) key += "e"; 
			
		}
		
		
		return mapActLabelsSML.get(key);
		
	}
	
	public static String getLabelSchema(String delimiter){
		String s = "";
		for (Map.Entry<String, String[]> entry : mapActLabelsSML.entrySet()) {
			String key = entry.getKey();
			String[] labels = entry.getValue();
			s += key;
			for(int i=labels.length-1;i>=0;i--){
				s += delimiter + labels[i];
			}
			s += "\n";
		}
		return s;
	}
}
