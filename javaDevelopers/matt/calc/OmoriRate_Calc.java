package javaDevelopers.matt.calc;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author Matt Gerstenberger
 * @version 1.0
 */
public class OmoriRate_Calc {
  private double k_value;
  private double c_value;
  private double p_value;
  private double time_start;
  private double time_end;
  private double eventRate;


  public OmoriRate_Calc(double[] OmoriParms, double [] TimeParms) {
    set_OmoriParms(OmoriParms, TimeParms);
  }

  /**
   * set_OmoriParms
   * set the k,c and p values
   * set the begin time and end time for the calculations
   * do the calculation
   */
  public void set_OmoriParms(double[] omoriParms, double [] timeParms) {
    k_value = omoriParms[0];
    c_value = omoriParms[1];
    p_value = omoriParms[2];
    time_start = timeParms[0];
    time_end = timeParms[1];
    calc_OmoriRate();
  }

  /**
   * get_OmoriRate
   * return the calculated rate
   */
  public double get_OmoriRate() {
    return eventRate;
  }

  /**
   * calc_OmoriRate
   * calculate the Omori rate based on the set parameters
   * k,c,p, start time and end time
   */
  private void calc_OmoriRate() {

    double pInv = 1 - p_value;

    if (p_value == 1)
        eventRate = k_value*Math.log((time_end+c_value)/(time_start+c_value));
    else
        eventRate = k_value/(pInv)*(Math.pow(time_end+c_value,pInv)-Math.pow(time_start+c_value,pInv));
  }


}
