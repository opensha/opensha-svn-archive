package org.opensha.sha.calc.IM_EventSet.v03.test;

import junit.framework.TestCase;

import org.opensha.commons.param.DependentParameterAPI;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.sha.calc.IM_EventSet.v03.IM_EventSetOutputWriter;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;

public class IMT_String_Test extends TestCase {
	
	private CB_2008_AttenRel cb08 = new CB_2008_AttenRel(null);

	public IMT_String_Test(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	private void checkIsSetCorrectly(double period) {
		ParameterAPI<?> imt = cb08.getIntensityMeasure();
		assertEquals(SA_Param.NAME, imt.getName());
		assertTrue(imt instanceof DependentParameterAPI);
		DependentParameterAPI<?> depIMT = (DependentParameterAPI<?>)imt;
		ParameterAPI<?> periodParam = depIMT.getIndependentParameter(PeriodParam.NAME);
		double imtPer = (Double)periodParam.getValue();
		System.out.println("got: " + imtPer + " sec, expecting: " + period + " sec");
		assertEquals(period, imtPer);
	}
	
	private void doTestPeriod(String imtStr, double imtPeriod) {
		IM_EventSetOutputWriter.setIMTFromString(imtStr, cb08);
		checkIsSetCorrectly(imtPeriod);
		String newStr = IM_EventSetOutputWriter.getHAZ01IMTString(cb08.getIntensityMeasure());
		assertEquals(imtStr, newStr);
	}
	
	public void test0_1Sec() {
		doTestPeriod("SA01", 0.1);
	}
	
	public void test0_25Sec() {
		doTestPeriod("SA025", 0.25);
	}
	
	public void test0_5Sec() {
		doTestPeriod("SA05", 0.5);
	}
	
	public void test1Sec() {
		doTestPeriod("SA1", 1.0);
	}
	
	public void test1_5Sec() {
		doTestPeriod("SA15", 1.5);
	}
	
	public void test5Sec() {
		doTestPeriod("SA50", 5.0);
	}
	
	public void test10Sec() {
		doTestPeriod("SA100", 10.0);
	}

}
