

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class GetSequences
 */
@WebServlet("/GetSequences")
public class GetSequences extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetSequences() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
		
		String users = request.getParameter("usr");// user ids (can be many, comma separated)
		String[] userIds = null;
		if (users != null)
			userIds = users.split("\\s*[,\t]+\\s*");
		
		String fileName = request.getParameter("filename");
		if (fileName == null) fileName = groupId.replaceAll(",", "_") + "_" + "_sequences.txt";
		
		String header = request.getParameter("header"); // include or not the header
		boolean incHeader = (header != null && header.equalsIgnoreCase("yes"));
		String svc = request.getParameter("svc");
		boolean incsvc = (svc != null && svc.equalsIgnoreCase("yes"));
		
		
		ArrayList<String> non_students = new ArrayList<String>(Common.non_students);
		
		String removeUsers = request.getParameter("removeUsr"); // group id
		String[] remove = null;
		if (removeUsers != null) {
			remove = removeUsers.split("\\s*[,\t]+\\s*");
			non_students.addAll(Arrays.asList(remove));
		}
		String delimiter = request.getParameter("delimiter");
		String allparameters = request.getParameter("allparameters");
		boolean incallparameters = (allparameters != null && allparameters.equalsIgnoreCase("yes"));
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
		
		// mode = 0,1,2,3 by session, question, session/question, topic
		int mode = 0;
		try{
			mode = Integer.parseInt(request.getParameter("mode"));
			if(mode < 0 || mode > 3) mode = 0;
		}catch(Exception e){
			mode = 0;
		}
		// include = 0,1,2,3 all, only questions, only examples, questions and examples (including AE)
		int include = 0;
		try{
			include = Integer.parseInt(request.getParameter("include"));
			if(include < 0 || include > 3) include = 0;
		}catch(Exception e){
			include = 0;
		}
		
		String timeLabels = request.getParameter("timelabels");
		boolean incTimeLabel = (timeLabels != null);
		
		boolean replaceExtTimes = (request.getParameter("replaceexttimes") != null);
		
		boolean extended = (request.getParameter("extended") != null);
		boolean pexspam = (request.getParameter("pexspam") != null);
		boolean labelMap = (request.getParameter("labelmap") != null);
		
		int half = (request.getParameter("half") != null) ? Integer.parseInt(request.getParameter("half")) : 0;
		
		boolean markRepetition = (request.getParameter("markrepetition") != null);
		boolean markRepetitionSeq = (request.getParameter("markrepetitionseq") != null);
		
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
			
			response.setContentType("text/plain");
			response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
			
			boolean printedHeader = false;
			String pexSpamAllSequences = "";
			
			if (delimiter == null || delimiter.equals(""))
				delimiter = cm.delimiter;
			if (incHeader && !printedHeader && !pexspam) {
				printedHeader = true;
				if(extended){
					out.print("user" + delimiter + "group" + delimiter + "session" + delimiter);
					out.print("appid" + delimiter + "applabel" + delimiter);
					out.print("activityname" + delimiter + "targetname" + delimiter
							+ "parentname" + delimiter + "topicname" + delimiter + "result" + delimiter);
					out.print("datestring" + delimiter + "durationseconds");
					out.print((incTimeLabel ? delimiter + "timelabel" : ""));
					out.print(delimiter + "sequenceid");
					out.print(delimiter + "groupingid");
					out.print(delimiter + "actionno");
					out.print(delimiter + "label");
					out.print(delimiter + "labelreadable");
					out.print((incsvc ? delimiter + "svc" : "") + (incallparameters ? delimiter + "allparameters" : "") + "\n");
				
				}else{
					out.print("user" + delimiter + "group" + delimiter);
					out.print("sequenceid" + delimiter + "groupingid" + delimiter);
					out.print("firstdate" + delimiter + "lastdate" + delimiter);
					out.print("size" + delimiter + "totaltime" + delimiter);
					//out.print("datestring" + delimiter + "durationseconds" + delimiter);
					out.print("sequence" + delimiter + "sequencereadable");
					out.print("\n");
				}
			}else if(pexspam && labelMap){
				out.print(Labeller.getLabelSchema("\t"));
				out.print("\n");
				out.print("\n@@@@@@@@@@\n");
			}
			
			for (String group : groupIds) {
				System.out.println("GROUP:"+group);
				
				GroupActivity groupActivity = new GroupActivity(group,
						non_students, Common.non_sessions, false, cm, dateRange, queryArchive);
				
				// @@@@
				// Build sequences
				groupActivity.sequence(mode,include); //
				Labeller labeller;
				if (incTimeLabel){
					String[] labels = timeLabels.split(","); 
					if(labels == null || labels.length<2) {
						labels = new String[]{"short","long"};
					}
					labeller = new Labeller(groupActivity,labels);
					labeller.labelTime(replaceExtTimes);
					//groupActivity.labelTime(labels);
				}else{
					labeller = new Labeller(groupActivity,null);
				}
				// label sequence attempts
				labeller.labelSequences(markRepetition,markRepetitionSeq);				
				// @@@@


				if (!groupActivity.isThereActivity()) {
					error = true;
					out.print("no activity found");
				}
				else {
					String userName = "";
					String session = "";
					HashMap<String, User> grp_activity = groupActivity.getGrpActivity();
					System.out.println("Total Users:" + grp_activity.size());
					// only report for the users who were specified in the parameter usr (and in its order). 
					// if none, report all
					String[] users_ = null;
					if (userIds != null)
						users_ = userIds;
					else
						users_ = (String[]) grp_activity.keySet().toArray(new String[grp_activity.keySet().size()]);
					for (String u : users_) {
						System.out.println("USER: " + u + "\t"+grp_activity.containsKey(u));
						User user = null;
						if (grp_activity.containsKey(u)) {
							user = grp_activity.get(u);
							userName = user.getUserLogin();
							if(half > 0) user.removeSequences(3-half);
							for (Sequence s : user.getSequences()) {
								int sid = s.getSequenceId();
								String sgid = s.getGroupingId(); 
								if(extended && !pexspam){
									int c = 0;
									for (LoggedActivity a : s.activity) {
										c++;
										out.print(userName + delimiter + group + delimiter
												+ a.getSession() + delimiter);
										out.print(a.getAppId() + delimiter + a.getLabel() + delimiter);
										out.print(
												a.getActivityName() + delimiter + a.getTargetName() + delimiter
												+ a.getParentName() + delimiter + a.getTopicName() + delimiter 
												+ a.getResult() + delimiter);
										out.print(a.getDateStr().toString() + delimiter);
										out.print(Common.df.format(a.getTime()));
										out.print((incTimeLabel ? delimiter + a.getLabelTime() : "") + delimiter);
										out.print(group+"_"+userName+"_"+sid + delimiter + sgid + delimiter + c + delimiter);
										out.print(a.getActLabel() + delimiter + a.getActLabelReadable());
										out.print((incsvc ? delimiter + a.getSvc() : "")
												+ (incallparameters ? delimiter + "\"" + a.getAllParameters()
														+ "\"" : "") + "\n");
									}
								}else{
									String[] ss = s.toString("","|");
									if(!pexspam){
										out.print(userName + delimiter + group + delimiter);
										out.print(group+"_"+userName+"_"+sid + delimiter + sgid + delimiter); 
										out.print(s.getFirstDate() + delimiter + s.getLastDate() + delimiter); 
										out.print(s.size() + delimiter + Common.df.format(s.totalTime()));
										//out.print("datestring" + delimiter + "durationseconds" + delimiter);
										out.print(delimiter + ss[0] + delimiter + ss[1]);
										out.print("\n");									
									}else{
										//out.print(Labeller.getLabelSchema("\t"));
										//out.print(userName + "_"+ sid + "\t" + ss[0]);
										if(s.size()>1){
											out.print(ss[0] + "\n");
											for(int i=0;i<ss[0].length();i++){
												out.print("1");
											}
											out.print("\n\n");
											
										}
										
										pexSpamAllSequences += group+ "_" + userName + "_" + sid + "\t" + userName + "\t" + ss[0] + "\n";
									}
									
								}
								
								
							}
						}
						else {
							// if the user requested has no activity
						}
					}
					
					
//					for (Map.Entry<String, User> entry : grp_activity.entrySet()) {
//						User user = entry.getValue();
//						userName = user.getUserLogin();
//						
//					}
					
				}

			}
			if(pexspam){
				out.print("\n\n@@@@@@@@@@\n\n\n" + pexSpamAllSequences + "\n");
			}
		}	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}
