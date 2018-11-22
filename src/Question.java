
public class Question {

	private int id;
	private double successNo;
	private int totalAttempt;
	
	public Question(int id, double successNo, int totalAttempt)
	{
		this.id = id;
		this.successNo = successNo;
		this.totalAttempt = totalAttempt;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getSuccessNo() {
		return this.successNo;
	}

	public void setSuccessNo(double successNo) {
		this.successNo = successNo;
	}

	public int getTotalAttempt() {
		return this.totalAttempt;
	}

	public void setTotalAttempt(int totalAttempt) {
		this.totalAttempt = totalAttempt;
	}
}
