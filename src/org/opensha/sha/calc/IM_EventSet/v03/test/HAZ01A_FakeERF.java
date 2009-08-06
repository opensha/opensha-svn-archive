package org.opensha.sha.calc.IM_EventSet.v03.test;

import java.util.ArrayList;

import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.ProbEqkSource;

public class HAZ01A_FakeERF extends EqkRupForecast {
	
	ArrayList<ProbEqkSource> sources;
	
	public EqkRupForecastAPI erf;
	
	public HAZ01A_FakeERF(EqkRupForecastAPI erf) {
		this.erf = erf;
	}

	@Override
	public int getNumSources() {
		return sources.size();
	}

	@Override
	public ProbEqkSource getSource(int source) {
		return sources.get(source);
	}

	@Override
	public ArrayList getSourceList() {
		return erf.getSourceList();
	}

	public String getName() {
		return erf.getName() + " (HAZ01A Test Stub!)";
	}

	public void updateForecast() {
		sources = new ArrayList<ProbEqkSource>();
		erf.updateForecast();
		for (int i=0; i<erf.getNumSources(); i++) {
			sources.add(new HAZ01A_FakeSource(erf.getSource(i), i));
		}
	}

}
