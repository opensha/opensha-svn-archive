/*
 * Created on Apr 1, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.scec.sha.earthquake.rupForecastImpl.remote;

import java.rmi.Naming;

/**
 * @author cmeutils
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class RegisterRemoteERF_Factory {
 public final static String registrationName = "rmi://gravity.usc.edu:1099/ERF_FactoryServer";
  public static void main(String[] args) {
    try {
      System.out.println("Starting ERF Factory Server");
      RemoteERF_FactoryAPI erfServer = new RemoteERF_FactoryImpl();
      Naming.rebind(registrationName, erfServer);
      System.out.println("Registered ERF Factory Server as " + registrationName);
    }
    catch (Exception e) {
      System.out.println("exception in starting server");
      e.printStackTrace();
      e.getMessage();
      return;
    }

	}
}
