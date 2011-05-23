package org.opensha.sha.imr.attenRelImpl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opensha.sha.imr.AttenRelImpl;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;

@RunWith(Parameterized.class)
public class ProductionIMRsInstantiationTest {
	
	private AttenRelImpl impl;
	
	public ProductionIMRsInstantiationTest(AttenRelImpl impl) {
		this.impl = impl;
	}
	
	@Parameters
	public static Collection<AttenRelImpl[]> data() {
		Set<AttenRelImpl> set = AttenRelImpl.prodSet();
		ArrayList<AttenRelImpl[]> ret = new ArrayList<AttenRelImpl[]>();
		for (AttenRelImpl imr : set) {
			AttenRelImpl[] array = { imr };
			ret.add(array);
		}
		return ret;
	}
	
	@Test
	public void testInstantiation() {
		ScalarIntensityMeasureRelationshipAPI imr = impl.instance(null);
		assertNotNull("IMR instance returned is NULL!", imr);
		imr.setParamDefaults();
	}

}
