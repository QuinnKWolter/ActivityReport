import java.util.ArrayList;


public class Sequence {
	public ArrayList<LoggedActivity> activity;
	private int sequenceId;
	private String groupingId; 
	
	public int getSequenceId() {
		return sequenceId;
	}
	
	public void setSequenceId(int sequenceId) {
		this.sequenceId = sequenceId;
	}
	
	public String getGroupingId() {
		return groupingId;
	}
	public void setGroupingId(String groupingId) {
		this.groupingId = groupingId;
	}
	
	public Sequence(int sequenceId) {
		super();
		this.sequenceId = sequenceId;
		this.activity = new ArrayList<LoggedActivity>();
		groupingId = "";
	}
	
	public String[] toString(String delimiter1, String delimiter2){
		String[] r = new String[2];
		r[0] = "";
		r[1] = "";
		for(LoggedActivity a : activity){
			r[0] += a.getActLabel() + delimiter1;
			r[1] += a.getActLabelReadable() + delimiter2;
		}
		if(r[0] != null && r[0].length()>0) r[0] = r[0].substring(0,r[0].length()-delimiter1.length());
		if(r[1] != null && r[1].length()>0) r[1] = r[1].substring(0,r[1].length()-delimiter2.length());
		return r;
	}
	
	public String toOutput(String delimiter, String actDelimiter){
		String r = sequenceId + delimiter + groupingId ;
		String l1 = "";
		String l2 = "";
		for(LoggedActivity a : activity){
			l1 += a.getActLabelReadable() + actDelimiter;
			l2 += a.getActLabel() + actDelimiter;
		}
		return r + delimiter + l1 + delimiter + l2;
	}
	
	public double totalTime(){
		double t = 0.0;
		if(activity != null) for(LoggedActivity a : activity){
			t += a.getTime();
		}
		return  t;
	}
	
	public int size(){
		if(activity != null) return activity.size();
		return 0;
	}
	
	public String getFirstDate(){
		if(activity != null && activity.size()>0) return activity.get(0).getDateStr();
		else return "";
	}
	public String getLastDate(){
		if(activity != null && activity.size()>0) return activity.get(activity.size()-1).getDateStr();
		else return "";
	}
}
