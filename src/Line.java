
public class Line {
	
	private int id;
	private int clicks;
	
	public Line(int id , int clicks)
	{
		this.id = id;
		this.clicks = clicks;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getClicks() {
		return this.clicks;
	}

	public void setClicks(int clicks) {
		this.clicks = clicks;
	}

}
