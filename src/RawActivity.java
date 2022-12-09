import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


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
		
		RawActivityRequestParameters params = new RawActivityRequestParameters(request, cm);
		
		RawActivityOutput outputHelper = null;
		
		if(params.outputMode == RawActivityOutputMode.JSON) {
			outputHelper = new JSONRawActivityOutput();
		} else if (params.outputMode == RawActivityOutputMode.CSV){
			outputHelper = new CsvRawActivityOutput();
		} else if (params.outputMode == RawActivityOutputMode.DATASHOP) {
			
		}
		
		outputHelper.init(params);
		
		response.setContentType("text/plain");
		response.setHeader("Content-Disposition", "attachment;filename=" + params.fileName);
		
		if (params.groupIds == null) {
			error = true;
			errorMsg = "group identifier not provided or invalid";
		}
		else {
			List<GroupActivity> groupActivityList = new ArrayList<GroupActivity>();
			for (String group : params.groupIds) {
				GroupActivity groupActivity = new GroupActivity(group, params, cm, null);
				
				if (params.incTimeLabel){
					String[] labels = params.timeLabels.split(","); 
					if(labels == null || labels.length<2) {
						labels = new String[]{"short","long"};
					}
					Labeller labeller = new Labeller(groupActivity,labels);
					labeller.labelTime(params.replaceExtTimes);
				}
					
				groupActivityList.add(groupActivity);
			}
			
		
			for(GroupActivity groupActivity:groupActivityList) {
				outputHelper.initGroup(groupActivity);
				
				if (!groupActivity.isThereActivity()) {
					error = true;
					errorMsg += "no activity found";
				} else {
					HashMap<String, User> grp_activity = groupActivity.getGrpActivity();
					for (Map.Entry<String, User> entry : grp_activity.entrySet()) {
						User user = entry.getValue();
						
						for (LoggedActivity act : user.getActivity()) {
							if(params.excludedAppIds.contains(Integer.toString(act.getAppId())) == false) {			
								outputHelper.processRawActivity(act, user);
							}
						}
					}
				}
				
				outputHelper.endGroup();
			}
		}
		
		if(error) {
			out.print(errorMsg);
		} else {
			String output = "";
			if (params.incHeader) {
				output = outputHelper.getHeader();
			}
			
			output += outputHelper.getOutput();
			out.print(output);
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
