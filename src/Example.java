import java.util.HashMap;
import java.util.Map;

public class Example {

	private String id;
	private Map<String,Integer> lineAct;
	
	public Example(String id, Map<String,Integer> lineAct){
		this.id = id;
		this.lineAct = lineAct;
	}

	public Example(String id){
		this.id = id;
		this.lineAct = new HashMap<String,Integer>();
	}

	
	public Map<String, Integer> getLineAct() {
		return lineAct;
	}
	
	public void setLineAct(Map<String, Integer> lineAct) {
		this.lineAct = lineAct;
	}
	
	public String getId() { return this.id; }
	
	public void setId(String id) { this.id = id; }
	
	public int getNumberOpen(User u)
	{
		if (lineAct.get("0")==null)
		{
			System.out.println("NO 0 line in Example! NULL returned! "+this.lineAct+" "+u.getUserLogin()+" "+" eid: "+this.getId());
		    return getTotalClicks();
		}
		return lineAct.get("0");
	}
	
	public int getTotalClicks()
	{
		int total = 0;
		for (String l : lineAct.keySet())
			if (l.equals("0") == false)
				total += lineAct.get(l);
		return total;
	}
}
