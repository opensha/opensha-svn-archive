package org.scec.sha.hazus;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class HazusIML_FileGenerator {

  public HazusIML_FileGenerator() {
    /**
     *using the equation provided for the Ned to get the Prob. for the Hazus.
     */
    double prob =0;
    double time = 50;

    double returnPd = 100;
    prob = -1*(1- Math.exp((1/returnPd)*50));
    System.out.println("Prob for 100: "+prob);

    returnPd = 250;
    prob = -1*(1- Math.exp((1/returnPd)*50));
    System.out.println("Prob for 250: "+prob);

    returnPd = 500;
    prob = -1*(1- Math.exp((1/returnPd)*50));
    System.out.println("Prob for 500: "+prob);

    returnPd = 750;
    prob = -1*(1- Math.exp((1/returnPd)*50));
    System.out.println("Prob for 750: "+prob);

    returnPd = 1000;
    prob = -1*(1- Math.exp((1/returnPd)*50));
    System.out.println("Prob for 1000: "+prob);

    returnPd = 1500;
    prob = -1*(1- Math.exp((1/returnPd)*50));
    System.out.println("Prob for 1500: "+prob);

    returnPd = 2000;
    prob = -1*(1- Math.exp((1/returnPd)*50));
    System.out.println("Prob for 2000: "+prob);

    returnPd = 2500;
    prob = -1*(1- Math.exp((1/returnPd)*50));
    System.out.println("Prob for 2500: "+prob);
  }
  public static void main(String[] args) {
    HazusIML_FileGenerator hazusIML_FileGenerator1 = new HazusIML_FileGenerator();
  }
}