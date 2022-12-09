public interface RawActivityOutput {
	void init(RawActivityRequestParameters params);
	void initGroup(GroupActivity groupActivity);
	void endGroup();
	
	String getHeader();
	void processRawActivity(LoggedActivity activity, User user);
	String getOutput();
}
