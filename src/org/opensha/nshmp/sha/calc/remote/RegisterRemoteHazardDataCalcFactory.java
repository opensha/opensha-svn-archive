package org.opensha.nshmp.sha.calc.remote;

import java.rmi.Naming;

import org.opensha.nshmp.sha.calc.remote.api.RemoteHazardDataCalcFactoryAPI;
import org.opensha.nshmp.util.GlobalConstants;

/**
 * <p>Title: RegisterRemoteHazardDataCalcFactory</p>
 * <p>Description: This class creates a RMI server that will listen all the
 * RMI request coming on to the server.</p>
 * @author : Ned (Edward) Field, Nitin Gupta and E.Ve.leyendecker
 * @version 1.0
 */

public class RegisterRemoteHazardDataCalcFactory {

  public static void main(String[] args) {
    try {
      // register the Hazard Curve Calculator with the naming service
      RemoteHazardDataCalcFactoryAPI hazardDataServer = new
          RemoteHazardDataCalcFactoryImpl();
      Naming.rebind(GlobalConstants.registrationName, hazardDataServer);
      System.out.println("Registered USGS Hazard Data Calc Factory Server as " +
                         GlobalConstants.registrationName);
    }
    catch (Exception e) {
      System.out.println("exception in starting server");
      e.printStackTrace();
      e.getMessage();
      return;
    }

  }
}
