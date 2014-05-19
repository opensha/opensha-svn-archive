package org.opensha.sha.imr.attenRelImpl;

import java.util.ArrayList;

import org.opensha.commons.param.event.ParameterChangeWarningListener;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;

import com.google.common.collect.Lists;

public class NGAWest_2014_Averaged_AttenRel extends MultiIMR_Averaged_AttenRel {
	
	public static final String NAME = "NGAWest2 2014 Averaged Attenuation Relationship (unverified!)";
	public static final String SHORT_NAME = "NGAWest_2014";
	
	private static ArrayList<ScalarIMR> buildIMRs(ParameterChangeWarningListener listener) {
		ArrayList<ScalarIMR> imrs = new ArrayList<ScalarIMR>();
		imrs.add(AttenRelRef.ASK_2014.instance(listener));
		imrs.add(AttenRelRef.BSSA_2014.instance(listener));
		imrs.add(AttenRelRef.CB_2014.instance(listener));
		imrs.add(AttenRelRef.CY_2014.instance(listener));
		imrs.add(AttenRelRef.IDRISS_2014.instance(listener));
		for (ScalarIMR imr : imrs) {
			imr.setParamDefaults();
		}
		return imrs;
	}
	private static ArrayList<Double> getWeights() {
		return Lists.newArrayList(0.22, 0.22, 0.22, 0.22, 0.12);
	}
	
	public NGAWest_2014_Averaged_AttenRel(ParameterChangeWarningListener listener) {
		super(buildIMRs(listener), getWeights());
	}

	@Override
	public String getShortName() {
		return SHORT_NAME;
	}

	@Override
	public String getName() {
		return NAME;
	}

}
