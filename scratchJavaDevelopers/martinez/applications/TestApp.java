package scratchJavaDevelopers.martinez.applications;

import java.util.ArrayList;
import java.util.TreeMap;

import scratchJavaDevelopers.martinez.util.DBAccessor;

public class TestApp {
	public static void main(String[] args) {
		String[] gldintdb = {"oracle.jdbc.OracleDriver", "localhost", 
				"1523", "TEAMT", "hc_owner", "ham23*ret"};
		DBAccessor db = new DBAccessor(gldintdb, true);
		if(db.hasValidConnection()) {
			String sql = "SELECT * FROM HC_ANALYSIS_OPT ORDER BY DISPLAY_SEQ";
			ArrayList<TreeMap<String, String>> result = db.doQuery(sql);
			doPrint(result);
		} else {
			System.err.println("Failed to get connection!");
		}
	}
	
	private static void doPrint(ArrayList<TreeMap<String, String>> result) {
		System.out.println("Analysis Options");
		for(int i = 0; i < result.size(); ++i) {
				System.out.println(result.get(i).get("ANALYSIS_OPT_DISPLAY"));
		}
	}
}
