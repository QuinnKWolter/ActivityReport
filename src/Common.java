import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Common {
	public static DecimalFormat df = new DecimalFormat("#.##");

	public static double MAX_ACTIVITY_TIME = 600.0; // in seconds
	public static double MIN_MOUSEOVER_TIME = 1;
	
	public static final int QUIZPACK 			= 2;
	public static final int WEBEX 				= 3;
	public static final int KNOWLEDGE_SEA 		= 5;
	public static final int KT 					= 8;
	public static final int SQLTUTOR			= 19;
	public static final int QUIZGUIDE 			= 20;
	public static final int SQLKNOT 			= 23;
	public static final int QUIZJET 			= 25;
	public static final int ANIMATED_EXAMPLE 	= 35;
	public static final int MASTERY_GRIDS 		= -1;
	public static final int QUIZPET 			= 41;
	public static final int PARSONS 			= 38;
	public static final int LESSLET 			= 37;
	public static final int PCRS	 			= 44;
	public static final int PCEX_EXAMPLE		= 46;
	public static final int PCEX_CHALLENGE		= 47;
	public static final int DBQA				= 53;
	
	public static boolean isContent(int appId){
		return appId == Common.QUIZJET || appId == Common.QUIZPET || appId == Common.PARSONS || appId == Common.QUIZPACK || 
				appId == Common.SQLKNOT ||appId == Common.SQLTUTOR || appId == Common.WEBEX || appId == Common.ANIMATED_EXAMPLE || 
				appId == LESSLET || appId == PCRS || appId == PCEX_EXAMPLE || appId == PCEX_CHALLENGE || appId == DBQA; 
				
	}

	public static String progressor_grps_map = "progressor_plus";

	// Caution: the later query also remove people from admin group (GroupId = 68)
	public static ArrayList<String> non_students = new ArrayList<String>(
			Arrays.asList("anonymous_user", "fedor.bakalov", "nkresl", "maccloud",
					"moeslein", "mliang", "pjcst19", "fseels", "r.hosseini", "ltaylor",
					"peterb", "shoha99", "jennifer", "dguerra", "demo01", "demo02", "demo03",
					"ddicheva","bcaldwell","sibelsomyurek","somyurek","jdg60","demo04","kerttupollari",
					"yuh43","alto15instructor","johnramirez","regan","sherry","billlaboon","daqing",
					"rafael.araujo","prmenon","aaltoinstructor","tmprinstructor","dmb72", "test0002","test0003",
					"experimental_test1","test0001","akhuseyinoglu", "tanja.mitrovic", "cht77", "jiangqiang", "ykortsarts","mab650",
					"arl122","arunb","rah225","ruhendrawan","moh70","ras555","qkw3"));

	
	
	// Caution: in the latter Query also exclude those containing TEST(test) in
	// the String
	public static ArrayList<String> non_sessions = new ArrayList<String>(
			Arrays.asList("null", "undefined", "xxx", "aaaaa", "bbbbb", "fffff",
					"XXXX", "xxxx", "XXXXX", "xxxxx", "xxxyyy", "YYYYY", "YYYY"));

	public static HashMap<Integer, String> APP_MAP;
	static {
		APP_MAP = new HashMap<Integer, String>();
		APP_MAP.put(2, "QUIZPACK");
		APP_MAP.put(3, "WEBEX");
		APP_MAP.put(5, "KNOWLEDGE_SEA");
		APP_MAP.put(8, "KT");
		APP_MAP.put(19, "SQLTUTOR");
		APP_MAP.put(20, "QUIZGUIDE");
		APP_MAP.put(23, "SQLKNOT");
		APP_MAP.put(25, "QUIZJET");
		APP_MAP.put(35, "ANIMATED_EXAMPLE");
		APP_MAP.put(37, "LESSLET");
		APP_MAP.put(38, "PARSONS");
		APP_MAP.put(39, "SOCIALREADER");
		APP_MAP.put(40, "EDUC_VIDEOS");
		APP_MAP.put(41, "QUIZPET");
		APP_MAP.put(44, "PCRS");
		APP_MAP.put(46, "PCEX_EXAMPLE");
		APP_MAP.put(47, "PCEX_CHALLENGE");
		APP_MAP.put(53, "DBQA");
		APP_MAP.put(-1, "MASTERY_GRIDS");
	}
	public static HashMap<String, Integer> MG_ACTIVITYID_MAP;
	static {
		MG_ACTIVITYID_MAP = new HashMap<String, Integer>();
		MG_ACTIVITYID_MAP.put("", 990000001);
		MG_ACTIVITYID_MAP.put("app-start", 990000002);
		MG_ACTIVITYID_MAP.put("data-load-start", 990000003);
		MG_ACTIVITYID_MAP.put("data-load-end", 990000004);
		MG_ACTIVITYID_MAP.put("app-ready", 990000005);
		MG_ACTIVITYID_MAP.put("group-set", 990000006);
		MG_ACTIVITYID_MAP.put("grid-activity-cell-select", 990000007);
		MG_ACTIVITYID_MAP.put("activity-open", 990000008);
		MG_ACTIVITYID_MAP.put("activity-reload", 990000009);
		MG_ACTIVITYID_MAP.put("activity-done", 990000010);
		MG_ACTIVITYID_MAP.put("activity-close", 990000011);
		MG_ACTIVITYID_MAP.put("activity-load-recommended", 990000012);
		MG_ACTIVITYID_MAP.put("activity-load-original", 990000013);
		MG_ACTIVITYID_MAP.put("load-others-list", 990000014);
		MG_ACTIVITYID_MAP.put("resource-set", 990000015);
		MG_ACTIVITYID_MAP.put("activity-feedback-set-difficulty", 990000016);
		MG_ACTIVITYID_MAP.put("grid-topic-cell-select", 990000017); // cell-topic-id
		MG_ACTIVITYID_MAP.put("comparison-mode-set", 990000018);
		// JULIO 2017
		//MG_ACTIVITYID_MAP.put("XXX", 990000019);
		//MG_ACTIVITYID_MAP.put("XXY", 990000020);
		//MG_ACTIVITYID_MAP.put("XYX", 990000021);
		//MG_ACTIVITYID_MAP.put("YXX", 990000022);
		//MG_ACTIVITYID_MAP.put("XYY", 990000023);
	}
	
	public static String csvFromArray(String[] values) {
		String res = "";
		if (values != null && values.length > 0) {
			for (String v : values) {
				res += "'" + v + "',";
			}
			res = res.substring(0, res.length() - 1);
		}
		else
			return null;
		return res;
	}

	public static String csvFromArray(ArrayList<String> values) {
		String res = "";
		if (values != null && values.size() > 0) {
			for (String v : values) {
				res += "'" + v + "',";
			}
			res = res.substring(0, res.length() - 1);
		}
		else
			return null;
		return res;
	}
	
	
	/**
	 * if date1 earlier than(<) date2: return -1; 
	 * =: return 0; 
	 * >: return 1
	 * 
	 */
	public static int compareStringDates(String date1_str, String date2_str) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		int compare = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			cal1.setTime(sdf.parse(date1_str));
			cal2.setTime(sdf.parse(date2_str));
			if (cal1.before(cal2))
				compare = -1;
			else if (cal1.equals(cal2))
				compare = 0;
			else
				compare = 1;
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		return compare;
	}
	
	public static String replaceNewLines(String s){
		return s.replaceAll("\n","\\n");
	}
}
