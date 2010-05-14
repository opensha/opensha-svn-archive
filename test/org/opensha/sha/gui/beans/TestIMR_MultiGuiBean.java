package org.opensha.sha.gui.beans;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import javax.swing.JCheckBox;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensha.commons.param.DependentParameterAPI;
import org.opensha.commons.util.ListUtils;
import org.opensha.sha.gui.infoTools.AttenuationRelationshipsInstance;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.BA_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel;
import org.opensha.sha.imr.event.ScalarIMRChangeEvent;
import org.opensha.sha.imr.event.ScalarIMRChangeListener;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.util.TectonicRegionType;

public class TestIMR_MultiGuiBean implements ScalarIMRChangeListener {
	
	static ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs;
	static ArrayList<TectonicRegionType> demoTRTs;
	static ArrayList<TectonicRegionType> demoSingleTRT;
	
	IMR_MultiGuiBean gui;
	
	Stack<ScalarIMRChangeEvent> eventStack = new Stack<ScalarIMRChangeEvent>();

	@BeforeClass
	public static void setUpBeforeClass() {
		AttenuationRelationshipsInstance inst = new AttenuationRelationshipsInstance();
		imrs = inst.createIMRClassInstance(null);
		for (ScalarIntensityMeasureRelationshipAPI imr : imrs) {
			imr.setParamDefaults();
		}
		demoTRTs = new ArrayList<TectonicRegionType>();
		demoTRTs.add(TectonicRegionType.ACTIVE_SHALLOW);
		demoTRTs.add(TectonicRegionType.STABLE_SHALLOW);
		demoTRTs.add(TectonicRegionType.SUBDUCTION_INTERFACE);
		demoTRTs.add(TectonicRegionType.SUBDUCTION_SLAB);
		demoSingleTRT = new ArrayList<TectonicRegionType>();
		demoSingleTRT.add(TectonicRegionType.ACTIVE_SHALLOW);
	}
	
	@Before
	public void setUp() throws Exception {
		gui = new IMR_MultiGuiBean(imrs);
	}
	
	@Test
	public void testSetTRT() {
		assertNull("TRTs should be null by default", gui.getTectonicRegions());
		
		assertFalse("without TRTs, multiple IMRs sould be false", gui.isMultipleIMRs());
		
		JCheckBox singleIMRBox = gui.singleIMRBox;
		
		assertFalse("Checkbox should not be showing with no TRTs", gui.isCheckBoxVisible());
		
		HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> imrMap = gui.getIMRMap();
		ScalarIntensityMeasureRelationshipAPI singleIMR = gui.getSelectedIMR();
		
		assertEquals("IMRMap should be of size 1 with no TRTs", 1, imrMap.size());
		assertEquals("Single IMR not returning same as first from Map",
				getSingleIMR(imrMap).getName(), singleIMR.getName());
		
		gui.setTectonicRegions(demoTRTs);
		
		assertTrue("Checkbox should now be showing with TRTs", gui.isCheckBoxVisible());
		assertTrue("Checkbox should be selected by default", singleIMRBox.isSelected());
		
		try {
			gui.getSelectedIMR();
		} catch (Exception e) {
			fail("getSelectedIMR should still work with multiple TRTs, but single IMR selected");
		}
		
		gui.setMultipleIMRs(true);
		assertFalse("Checkbox should now be deselected", singleIMRBox.isSelected());
		
		try {
			gui.getSelectedIMR();
			fail("getSelectedIMR should throw exception when multiple IMRs selected");
		} catch (Exception e) {}
		
		imrMap = gui.getIMRMap();
		
		assertEquals("IMRMap should be of size "+demoTRTs.size()+" with TRTs and multiple selected",
				demoTRTs.size(), imrMap.size());
		
		gui.setMultipleIMRs(false);
		
		try {
			gui.getSelectedIMR();
		} catch (Exception e) {
			fail("getSelectedIMR should still work with multiple TRTs, but single IMR selected");
		}
		
		imrMap = gui.getIMRMap();
		
		assertEquals("IMRMap should be of size 1 with TRTs and single selected", 1, imrMap.size());
		
		gui.setMultipleIMRs(true);
		gui.setMultipleIMRsEnabled(false);
		assertFalse("disabling multi IMRs should deselect as well", gui.isMultipleIMRs());
		gui.setMultipleIMRsEnabled(true);
		gui.setMultipleIMRs(true);
		
		gui.setTectonicRegions(null);
		
		assertFalse("Checkbox should not be showing with no TRTs", gui.isCheckBoxVisible());
		
		gui.setTectonicRegions(demoSingleTRT);
		assertFalse("Checkbox should not be showing with only 1 TRT", gui.isCheckBoxVisible());
	}
	
	@Test
	public void testIMRChangeEvents() {
		gui.addIMRChangeListener(this);
		
		assertEquals("Event stack should be empty to start", 0, eventStack.size());
		
		/*		Test IMR changes firing events				*/
		ScalarIntensityMeasureRelationshipAPI prevIMR = gui.getSelectedIMR();
		gui.setSelectedSingleIMR(BA_2008_AttenRel.NAME);
		assertEquals("Changing IMR should fire a single event", 1, eventStack.size());
		ScalarIMRChangeEvent event = eventStack.pop();
		assertEquals("New IMR in event is wrong!", BA_2008_AttenRel.NAME,
				getSingleIMR(event.getNewIMRs()).getName());
		assertEquals("Old IMR in event is wrong!", prevIMR.getName(),
				getSingleIMR(event.getOldValue()).getName());
		
		/*		Test TRT changes firing events				*/
		gui.setTectonicRegions(demoTRTs);
		assertEquals("Should not fire event when TRTs added, but not multiple IMRs selected", 0, eventStack.size());
		
		gui.setMultipleIMRs(false);
		assertEquals("Should not fire event setting to single with single already selected", 0, eventStack.size());
		
		gui.setMultipleIMRs(true);
		assertEquals("Should fire event setting to multiple with single selected", 1, eventStack.size());
		event = eventStack.pop();
		assertEquals("Event newIMRMap should be of size "+demoTRTs.size()+" with TRTs and multiple selected",
				demoTRTs.size(), event.getNewIMRs().size());
		assertEquals("Event oldIMRMap should be of size 1 here",
				1, event.getOldValue().size());
		
		gui.setMultipleIMRs(false);
		assertEquals("Should fire event setting to single with multiple selected", 1, eventStack.size());
		event = eventStack.pop();
		assertEquals("Event newIMRMap should be of size "+demoTRTs.size()+" with TRTs and single selected",
				1, event.getNewIMRs().size());
		assertEquals("Event oldIMRMap should be of size "+demoTRTs.size()+" here",
				demoTRTs.size(), event.getOldValue().size());
		
		/*		Test IMT changes firing events				*/
		gui.setIMT((DependentParameterAPI<Double>) gui.getSelectedIMR().getIntensityMeasure());
		assertEquals("Should not fire event setting IMT to current IMT", 0, eventStack.size());
		
		ScalarIntensityMeasureRelationshipAPI shakeMapIMR =
			(ScalarIntensityMeasureRelationshipAPI) ListUtils.getObjectByName(imrs, ShakeMap_2003_AttenRel.NAME);
		shakeMapIMR.setIntensityMeasure(ShakeMap_2003_AttenRel.MMI_NAME);
		DependentParameterAPI<Double> mmiIMR = (DependentParameterAPI<Double>) shakeMapIMR.getIntensityMeasure();
		gui.setIMT(mmiIMR);
		assertEquals("Should fire event setting IMT to MMI when IMR doesn't support it", 1, eventStack.size());
		event = eventStack.pop();
		assertTrue("New IMR should support IMT",
				getSingleIMR(event.getNewIMRs()).isIntensityMeasureSupported(mmiIMR));
		assertFalse("Old IMR should not support IMT",
				getSingleIMR(event.getOldValue()).isIntensityMeasureSupported(mmiIMR));
		
		// now lets change back to something that they both support
		ScalarIntensityMeasureRelationshipAPI cb2008 =
			(ScalarIntensityMeasureRelationshipAPI) ListUtils.getObjectByName(imrs, CB_2008_AttenRel.NAME);
		cb2008.setIntensityMeasure(PGA_Param.NAME);
		DependentParameterAPI<Double> pgaIMR = (DependentParameterAPI<Double>) cb2008.getIntensityMeasure();
		gui.setIMT(pgaIMR);
		assertEquals("Should not fire event setting IMT to one supported by current IMR", 0, eventStack.size());
		gui.setIMT(null);
		assertEquals("Should not fire event setting IMT to null", 0, eventStack.size());
	}
	
	private static ScalarIntensityMeasureRelationshipAPI getSingleIMR(
			HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> imrMap) {
		return imrMap.values().iterator().next();
	}

	@Override
	public void imrChange(ScalarIMRChangeEvent event) {
		eventStack.push(event);
	}

}
