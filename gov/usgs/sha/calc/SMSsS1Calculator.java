package gov.usgs.sha.calc;

import org.scec.data.function.*;
import gov.usgs.util.ui.*;

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

  /**
   *
   * @param saVals ArbitrarilyDiscretizedFunc
   * @param fa float
   * @param fv float
   * @param siteClass String
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc calculateSMSsS1(ArbitrarilyDiscretizedFunc
      saVals, float fa, float fv, String siteClass) {
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
    function.set(saVals.getX(0), fa * saVals.getY(0));
    function.set(saVals.getX(1), fv * saVals.getY(1));
    String title = "Spectral Response Accelerations SMs and SM1";
    String subTitle = "SMs = FaSs and SM1 = FvS1";
    String SA = "Sa";
    String text1 = "SMs";
    String text2 = "SM1";
    String info = "";
    info += title;
    info +=
        DataDisplayFormatter.createSubTitleString(subTitle, siteClass, fa, fv);
    info +=
        DataDisplayFormatter.createFunctionInfoString(function, SA, text1, text2,
        siteClass);
    function.setInfo(info);
    return function;
  }

}
