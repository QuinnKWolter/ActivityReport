import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class FixTracking
 * This service correct erroneous logged data from ent_user_activity table. Activity from new users and
 * groups has problems when tracked before restarting cbum, and as a result, appid, activityid, userid, 
 * groupid are with default values 1, 1, 2, 1, respectively  
 * 
 */
@WebServlet("/FixTracking")
public class FixTracking extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FixTracking() {
        super();
        
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		response.setHeader("Content-Disposition", "attachment;filename=update_bad_rows2.txt");
		
		PrintWriter out = response.getWriter();
		
		boolean archive = false;
		if(request.getParameter("archive") != null) archive = true;
		
		ConfigManager cm = new ConfigManager(this);
		um2DBInterface um2_db = new um2DBInterface(cm.um2_dbstring, cm.um2_dbuser, cm.um2_dbpass);
		um2_db.openConnection();
		Map<String,Integer> activities = um2_db.getActivityIdMap();
		Map<String, Integer> actSubIds = um2_db.getActSubIdMap(); 
		Map<String,Integer> users = um2_db.getLoginUserIdMap(); // to get the userid and groupid
		ArrayList<String[]> data = um2_db.getBadTrackedActivity(archive); // datentime, datentimens, AllParameters,"app","grp","usr","act","sub"
		
		System.out.println("Bad rows: "+data.size());
		Integer appid = -1;
		Integer groupid = -1;
		Integer userid = -1;
		Integer activityid = -1;
		
		um2_db.closeConnection();
		for(String[] row : data){
			String[] params = row[2].split(";");
			appid = -1;
			groupid = -1;
			userid = -1;
			activityid = -1;
			for(String param : params){
				String[] pair = param.split("=");
				
				if(pair != null && pair.length==2){
					if(pair[0].equals("app")){
						row[3] = pair[1];
						try{
							appid = Integer.parseInt(row[3]);
						}catch(Exception e){
							
						}
					}
					if(pair[0].equals("grp")){
						row[4] = pair[1];
					}
					if(pair[0].equals("usr")){
						row[5] = pair[1];
					}
					if(pair[0].equals("act")){
						row[6] = pair[1];
					}
					if(pair[0].equals("sub")){
						row[7] = pair[1];
					}
				}
			}
			groupid = users.get(row[4]);
			userid = users.get(row[5]);
			switch(appid){
			case 3: // examples
			case 6: // ksea
			case 9: // 
			case 23: // SQLKnot
			case 32: // progressor
			case 35:	
				activityid = actSubIds.get(row[6]+"_"+row[7]);
				break;
			case 8: // ktree
				activityid = activities.get(row[6]);
				break;
			case 25:
				activityid = activities.get(row[7]);
				break;
			case 38: // parsons
				activityid = activities.get(row[7]);
				break;
			case 53: // parsons
				activityid = activities.get(row[7]);
				break;
			default:
				break;
				
			}
			if(appid != null && groupid != null && userid != null && activityid != null){
				if(appid>0 && groupid>0 && userid>0 && activityid>0){
//					out.print(row[0]+","+row[1]+","+row[2]+","+row[3]+","+row[4]+","+row[5]+","+row[6]+","+row[7]+
//							","+appid+","+groupid+","+userid+","+activityid+"\n");
					if(!archive) out.print("update ent_user_activity ");
					else out.print("update archive_user_activity ");
					
					out.print("set appid ="+ appid +", "
								+ "groupid ="+ groupid +", "
								+ "userid ="+ userid +", "
								+ "activityid ="+ activityid
								+ " WHERE id = "+row[8]+";\n");
//														 		+ " WHERE appid=1 AND activityid=1 AND groupid=1 AND userid=2 "
//														 		+ " AND datentimens = "+row[1]+" AND AllParameters = '"+row[2]+"';\n");
				}
				
			}
			//out.print(row[0]+","+row[1]+","+row[2]+","+row[3]+","+row[4]+","+row[5]+","+row[6]+","+row[7]+"\n");
			
		}
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

}
