package gov.usgs.sha.calc.remote;

import gov.usgs.sha.calc.remote.RemoteHazardDataCalcFactoryImpl;
import gov.usgs.sha.calc.remote.api.RemoteHazardDataCalcFactoryAPI;
import java.rmi.Naming;

import gov.usgs.util.GlobalConstants;

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
       RemoteHazardDataCalcFactoryAPI hazardDataServer = new RemoteHazardDataCalcFactoryImpl();
       Naming.rebind(GlobalConstants.registrationName, hazardDataServer);
       System.out.println("Registered USGS Hazard Data Calc Factory Server as " + GlobalConstants.registrationName);
     }
     catch (Exception e) {
       System.out.println("exception in starting server");
       e.printStackTrace();
       e.getMessage();
       return;
     }

 }
}
