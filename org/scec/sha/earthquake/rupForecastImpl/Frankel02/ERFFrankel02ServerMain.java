/*
 * Created on Apr 1, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.scec.sha.earthquake.rupForecastImpl.Frankel02;

import java.rmi.Naming;

/**
 * @author cmeutils
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ERFFrankel02ServerMain {

  public static void main(String[] args) {
    try {
      System.out.println("Starting Main ERF Server");
      String registrationName = "rmi://gravity.usc.edu:1099/ERFFrankel02Server";
      ERFFrankel02Server erfServer = new ERFFrankel02ServerImpl();
      Naming.rebind(registrationName, erfServer);
      System.out.println("Registered ERFServer as " + registrationName);
    }
    catch (Exception e) {
      System.out.println("exception in starting server");
      e.printStackTrace();
      e.getMessage();
      return;
    }

	}
}
