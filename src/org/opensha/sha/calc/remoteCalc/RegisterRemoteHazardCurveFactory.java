package org.opensha.sha.calc.remoteCalc;

import java.rmi.Naming;

/**
 * <p>Title: RegisterRemoteHazardCurveFactory</p>
 * <p>Description: This class creates a RMI server that will listen all the
 * RMI request coming on to the server.</p>
 * @author : Ned (Edward) Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class RegisterRemoteHazardCurveFactory {

  public final static String registrationName = "rmi://opensha.usc.edu:1099/HazardCurve_FactoryServer";


   public static void main(String[] args) {
     try {
       // register the Hazard Curve Calculator with the naming service
       RemoteHazardCurveFactoryAPI hazardCurveServer = new RemoteHazardCurveFactoryImpl();
       Naming.rebind(registrationName, hazardCurveServer);
       System.out.println("Registered Hazard Curve Factory Server as " + registrationName);
     }
     catch (Exception e) {
       System.out.println("exception in starting server");
       e.printStackTrace();
       e.getMessage();
       return;
     }

 }
}
