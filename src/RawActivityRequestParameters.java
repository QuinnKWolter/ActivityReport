import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class RawActivityRequestParameters {
	
	public String fileName;
	public boolean incHeader;
	public boolean replaceExtTimes;
	public boolean incsvc;
	public boolean incTimeLabel;
	public String[] groupIds;
	public String timeLabels;
	public ArrayList<String> non_students;
	public String[] dateRanges;
	public boolean queryArchive;
	public boolean sessionate;
	public int minThreshold;
	public String outputModeStr;
	public RawActivityOutputMode outputMode;
	public List<String> excludedAppIds;
	public String delimiter;
	public boolean incallparameters;


	public RawActivityRequestParameters(HttpServletRequest request, ConfigManager cm) {
		String groupId = request.getParameter("grp"); // group id
		
		groupIds = null;
		if (groupId != null)
			groupIds = groupId.split("\\s*[,\t]+\\s*");
		
		fileName = request.getParameter("filename");
		if (fileName == null) fileName = groupId.replaceAll(",", "_") + "_" + "raw_activity.txt";
	
		
		String header = request.getParameter("header"); // include or not the header
		incHeader = (header != null && header.equalsIgnoreCase("yes"));

		replaceExtTimes = (request.getParameter("replaceexttimes") != null);
		
		String svc = request.getParameter("svc");
		incsvc = (svc != null && svc.equalsIgnoreCase("yes"));
		
		timeLabels = request.getParameter("timelabels");
		incTimeLabel = (timeLabels != null);
		
		sessionate = (request.getParameter("sessionate") != null);
		
		minThreshold = 90;
		if(sessionate) minThreshold = 90;
		try{minThreshold = Integer.parseInt(request.getParameter("minthreshold"));}catch(Exception e){minThreshold = 90;}
		
		excludedAppIds = new ArrayList<String>();
		String excludeApp = request.getParameter("excludeApp");
		if (excludeApp != null)
			excludedAppIds = Arrays.asList(excludeApp.split("\\s*[,\t]+\\s*"));
		
		non_students = new ArrayList<String>(Common.non_students);
		
		outputModeStr = request.getParameter("outputMode");
		outputMode = RawActivityOutputMode.CSV;
		
		if(outputModeStr != null) {
			if(outputModeStr.equals("json")) {
				outputMode = RawActivityOutputMode.JSON;
			} else if(outputModeStr.equals("datashop")) {
				outputMode = RawActivityOutputMode.DATASHOP;
			}
		}
		
		String removeUsers = request.getParameter("removeUsr");
		String[] remove = null;
		if (removeUsers != null) {
			remove = removeUsers.split("\\s*[,\t]+\\s*");
			non_students.addAll(Arrays.asList(remove));
		}
		delimiter = request.getParameter("delimiter");
		if (delimiter == null || delimiter.equals("")) {
			delimiter = cm.delimiter;
		}
			
		String allparameters = request.getParameter("allparameters");
		incallparameters = (allparameters != null && allparameters
				.equalsIgnoreCase("yes"));
		
		// String pattern = "yyyy-MM-dd";
		// SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		String fromDate = request.getParameter("fromDate");
		String toDate = request.getParameter("toDate");
		dateRanges = new String[2];
		dateRanges[0] = "";
		dateRanges[1] = "";
		
		if (fromDate != null && fromDate.length() > 0)
			dateRanges[0] = fromDate;// formatter.format(fromDate);
		if (toDate != null && toDate.length() > 0)
			dateRanges[1] = toDate;// formatter.format(toDate);
		
		String queryArchiveParam = request.getParameter("queryArchive");
		if(queryArchiveParam == null) {
			queryArchiveParam = "yes";
		}
		queryArchive = queryArchiveParam.equalsIgnoreCase("yes");
		
		
	}

}
