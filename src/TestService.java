

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class TestService
 * 
 * TestService?grp=IS172014Spring
 */
@WebServlet("/TestService")
public class TestService extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TestService() {
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
		String header = request.getParameter("header"); // include or not the header
		boolean incHeader = (header != null && header.equalsIgnoreCase("yes"));
		String svc = request.getParameter("svc");
		boolean incsvc = (svc != null && svc.equalsIgnoreCase("yes"));
		String removeUsers = request.getParameter("removeUsr"); // group id
		String[] remove = null;
		if (removeUsers != null) {
			remove = removeUsers.split("\\s*[,\t]+\\s*");
			Common.non_students.addAll(Arrays.asList(remove));
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

		String timeLabels = request.getParameter("timelabels");
		boolean incTimeLabel = (timeLabels != null);
		
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
			for (String group : groupIds) {
				response.setContentType("text/plain");
				response.setHeader("Content-Disposition", "attachment;filename="
						+ group + ".txt");
				String topicSource = "UNKNOWN";
				if (Common.mg_grps.contains(group))
					topicSource = "MG";
				else if (Common.progressor_grps.contains(group))
					topicSource = "PR";

				GroupActivity groupActivity = new GroupActivity(group, topicSource,
						Common.non_students, Common.non_sessions, false, cm, dateRange, queryArchive);
				
				// @@@@
				// Build sequences
				//groupActivity.sequence(0,1); // by session, only questions
				//groupActivity.sequence(2,1); // by session/question, only questions
				//groupActivity.sequence(0,3); // by session, questions and examples
				//groupActivity.sequence(0,0); // by session, all
				groupActivity.sequence(3,3); // by topic , q and e
				Labeller labeller;
				if (incTimeLabel){
					String[] labels = timeLabels.split(","); 
					if(labels == null || labels.length<2) {
						labels = new String[]{"short","long"};
					}
					labeller = new Labeller(groupActivity,labels);
					labeller.labelTime(true);
					//groupActivity.labelTime(labels);
				}else{
					labeller = new Labeller(groupActivity,null);
				}
				// label sequence attempts
				labeller.labelSequences(false,true);
				
				
				// @@@@
				
				if (delimiter == null || delimiter.equals(""))
					delimiter = cm.delimiter;
				if (incHeader && !printedHeader) {
					printedHeader = true;
					out.print("user" + delimiter + "group" + delimiter + "session"
							+ delimiter + "timebin" + delimiter);
					out.print("appid" + delimiter + "applabel" + delimiter);
					out.print("activityname" + delimiter + "targetname" + delimiter
							+ "parentname" + delimiter + "topicname" + delimiter
							+ "attemptno" + delimiter + "result" + delimiter);
					out.print("datestring" + delimiter + "durationseconds" + delimiter + "timelabel");
					out.print((incsvc ? delimiter + "svc" : "")
							+ (incallparameters ? delimiter + "allparameters" : "") + "\n");
				}

				if (!groupActivity.isThereActivity()) {
					error = true;
					out.print("no activity found");
				}
				else {
					String userName = "";
					String session = "";
					HashMap<String, User> grp_activity = groupActivity.getGrpActivity();
					for (Map.Entry<String, User> entry : grp_activity.entrySet()) {
						User user = entry.getValue();
						userName = user.getUserLogin();
						for (LoggedActivity a : user.getActivity()) {
							// System.out.println(userName + "," + a.getAllParameters());
							out.print(userName + delimiter + group + delimiter
									+ a.getSession() + delimiter + a.getSessionActNo()
									+ delimiter);
							out.print(a.getAppId() + delimiter + a.getLabel() + delimiter);
							out.print(// a.getActivityId() + delimiter +
							a.getActivityName() + delimiter + a.getTargetName() + delimiter
									+ a.getParentName() + delimiter + a.getTopicName()
									+ delimiter + a.getAttemptNo() + delimiter + a.getResult()
									+ delimiter);
							out.print(a.getDateStr().toString() + delimiter);
							out.print(Common.df.format(a.getTime()) + delimiter);
							out.print(a.getLabelTime());
							out.print((incsvc ? delimiter + a.getSvc() : "")
									+ (incallparameters ? delimiter + "\"" + a.getAllParameters()
											+ "\"" : "") + "\n");

						}
						out.print("\n\nSEQUENCES\n");
						for (Sequence s : user.getSequences()) {
							
							out.print(userName + delimiter + s.toOutput(delimiter,",") + "\n");
						}
						out.print("\n\n");
					}
				}

			}
		}		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
