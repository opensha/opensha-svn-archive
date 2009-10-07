package org.opensha.sha.calc.remoteCalc;

import java.rmi.Naming;

/**
 * <p>Title: RegisterRemoteDisaggregationCalcFactory</p>
 * <p>Description: This class creates a RMI server that will listen all the
 * RMI request for disaggregation calculation coming on to the server.</p>
 * @author : Ned (Edward) Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class RegisterRemoteDisaggregationCalcFactory {

  public final static String registrationName = "rmi://opensha.usc.edu:1099/DisaggregationCalc_FactoryServer";


   public static void main(String[] args) {
     try {
       // register the Disaggregation Calculator with the naming service
       RemoteDisaggregationCalcFactoryAPI disaggCalcServer = new RemoteDisaggregationCalcFactoryImpl();
       Naming.rebind(registrationName, disaggCalcServer);
       System.out.println("Registered Disaggregation Calc Factory Server as " + registrationName);
     }
     catch (Exception e) {
       System.out.println("exception in starting server");
       e.printStackTrace();
       e.getMessage();
       return;
     }

 }
}
