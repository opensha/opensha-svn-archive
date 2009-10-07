package org.opensha.sha.calc.remoteCalc;

import java.rmi.Naming;

/**
 * <p>Title: RegisterRemoteHazardCurveFactory</p>
 * <p>Description: This class creates a RMI server that will listen all the
 * RMI request coming on to the server.</p>
 * @author : Ned (Edward) Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class RegisterRemoteResponseSpectrumFactory {

  public final static String registrationName = "rmi://opensha.usc.edu:1099/ResponseSpectrum_FactoryServer";


   public static void main(String[] args) {
     try {
       // register the Response Spectrum  Calculator with the naming service
       RemoteResponseSpectrumFactoryAPI responseSpectrumServer = new RemoteResponseSpectrumFactoryImpl();
       Naming.rebind(registrationName, responseSpectrumServer);
       System.out.println("Registered  response spectrum Factory Server as " + registrationName);
     }
     catch (Exception e) {
       System.out.println("exception in starting server");
       e.printStackTrace();
       e.getMessage();
       return;
     }

 }
}
