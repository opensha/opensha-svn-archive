package gov.usgs.sha.calc;

import org.scec.data.function.ArbitrarilyDiscretizedFunc;

/**
 * <p>Title: SMSsS1Calculator</p>
 *
 * <p>Description: </p>
 *
 * @author Ned Field,Nitin Gupta and E.V.Leyendecker
 *
 * @version 1.0
 */
public class SMSsS1Calculator {

  private ArbitrarilyDiscretizedFunc calculateSMSsS1(ArbitrarilyDiscretizedFunc
      saVals, float fa, float fv) {
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
    function.set(saVals.getX(0), fa * saVals.getY(0));
    function.set(saVals.getX(1), fv * saVals.getY(1));
    String title = "Spectral Response Accelerations SMs and SM1";
    String subTitle = "SMs = FaSs and SM1 = FvS1";
    String text1 = "SMs";
    String text2 = "SM1";
    return function;
  }

}
