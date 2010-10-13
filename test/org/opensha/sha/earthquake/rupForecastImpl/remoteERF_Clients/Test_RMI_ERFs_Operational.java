package org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients;


import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import org.junit.Before;
import org.junit.Test;
import org.opensha.commons.util.RMIUtils;
import org.opensha.commons.util.ServerPrefs;
import org.opensha.sha.earthquake.rupForecastImpl.remote.RegisterRemoteERF_Factory;
import org.opensha.sha.earthquake.rupForecastImpl.remote.RegisterRemoteERF_ListFactory;

import util.TestUtils;

public class Test_RMI_ERFs_Operational {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testFrankel96() throws Throwable {
		TestUtils.runTestWithTimer("runFrankel96", this, 60);
	}
	
	@SuppressWarnings("unused")
	private void runFrankel96() {
		Frankel96_AdjustableEqkRupForecastClient erf = null;
		try {
			erf = new Frankel96_AdjustableEqkRupForecastClient();
		} catch (RemoteException e) {
			e.printStackTrace();
			fail("RemoteException: " + e.getMessage());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("MalformedURLException: " + e.getMessage());
		} catch (NotBoundException e) {
			e.printStackTrace();
			fail("NotBoundException: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception connectiong to ERF: " + e.getMessage());
		}
		assertNotNull("ERF should not be null!", erf);
		try {
			erf.updateForecast();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception updating forecast: " + e.getMessage());
		}
	}
	
	@Test
	public void testWG02_Fortran() throws Throwable {
		TestUtils.runTestWithTimer("runWG02", this, 60);
	}
	
	@SuppressWarnings("unused")
	private void runWG02() {
		WG02_FortranWrappedERF_EpistemicListClient erf = null;
		try {
			erf = new WG02_FortranWrappedERF_EpistemicListClient();
		} catch (RemoteException e) {
			e.printStackTrace();
			fail("RemoteException: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception connecting to ERF: " + e.getMessage());
		}
		assertNotNull("ERF should not be null!", erf);
		try {
			erf.updateForecast();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			fail("NullPointerException updating forecast: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception updating forecast: " + e.getMessage());
		}
	}
	
	@Test
	public void testProdPorts() throws Throwable {
		TestUtils.runTestWithTimer("runTestProdPorts", this, 15);
	}
	
	@Test
	public void testDevPorts() throws Throwable {
		TestUtils.runTestWithTimer("runTestDevPorts", this, 15);
	}
	
	@SuppressWarnings("unused")
	private void runTestProdPorts() {
		testPortsForPrefs(ServerPrefs.PRODUCTION_PREFS);
	}
	
	@SuppressWarnings("unused")
	private void runTestDevPorts() {
		testPortsForPrefs(ServerPrefs.DEV_PREFS);
	}
	
	private boolean contains(String[] list, String testName) {
		for (String name : list) {
			if (name.equals(testName))
				return true;
		}
		return false;
	}
	
	private void testPortsForPrefs(ServerPrefs prefs) {
		try {
			Registry reg = RMIUtils.getRegistry(prefs);
			String[] list = reg.list();
			System.out.println("******* Naming list for "+prefs.name()+" *******");
			for (String name : list)
				System.out.println("- "+name);
			String[] testNames = { RegisterRemoteERF_Factory.registrationName,
					RegisterRemoteERF_ListFactory.registrationName };
			for (String testName : testNames) {
				assertTrue("ServerPrefs '"+prefs.name()+"' doesn't have RMI binding for '"+testName+"'", 
						contains(list, testName));
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			fail("Remote exceptoin!");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception testing registry for '"+prefs.name()+"': " + e.getMessage());
		}
	}

}
