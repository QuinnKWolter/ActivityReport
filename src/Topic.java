public class Topic {

	private int id;
	private int atmpt;
	private double successNo;
	
	public Topic(int id,double successNo, int atmpt)
	{
		this.id = id;
		this.successNo = successNo;
		this.atmpt = atmpt;
	}
	
	public double getSuccessNo() {
		return successNo;
	}

	public void setSuccessNo(double successNo) {
		this.successNo = successNo;
	}

	public int getAtmpt() {
		return this.atmpt;
	}

	public void setAtmpt(int atmpt) {
		this.atmpt = atmpt;
	}

	public int getId() {
		return this.id;
	}
	public void setId(int id) {
		this.id = id;
	}	
}
