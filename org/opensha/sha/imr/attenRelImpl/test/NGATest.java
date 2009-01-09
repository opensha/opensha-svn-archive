package org.opensha.sha.imr.attenRelImpl.test;

import java.util.ArrayList;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.event.ParameterChangeWarningListener;

public abstract class NGATest extends TestCase implements ParameterChangeWarningListener {
	
	public NGATest(String arg0) {
		super(arg0);
	}
	
	protected double[] loadPeriods(String line) {
		StringTokenizer tok = new StringTokenizer(line);
		
		// skip the first 9
		for (int i=0; i<9; i++) {
			tok.nextToken();
		}
		
		String col = tok.nextToken();
		
		ArrayList<Double> periodList = new ArrayList<Double>();
		
		while (!col.contains("PGA")) {
			periodList.add(Double.parseDouble(col));
			
			col = tok.nextToken();
		}
		
		double periods[] = new double[periodList.size()];
		
		String str = "";
		
		for (int i=0; i<periodList.size(); i++) {
			periods[i] = periodList.get(i);
			str += " " + periods[i];
		}
		
		System.out.println("Periods:" + str);
		
		return periods;
	}
	
	public void parameterChangeWarning(ParameterChangeWarningEvent e){
		System.err.println("Parameter change warning!");
		System.err.flush();
		return;
	}
	
}
