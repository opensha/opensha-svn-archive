package scratchJavaDevelopers.martinez.applications;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;

import scratchJavaDevelopers.martinez.beans.GraphingBean;


public class TestGraphApp {
	public static void main(String [] args) {
		ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
		func.setName("Sample Function");
		for(int i = 0; i < 20; ++i)
			func.set(i, i);
		
		GraphingBean gb = new GraphingBean();
		gb.setGraphFunction(func);
	}
}
