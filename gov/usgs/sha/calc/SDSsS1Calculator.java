package gov.usgs.sha.calc;

import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import gov.usgs.sha.data.DataDisplayFormatter;

/**
 * <p>Title: SDSsS1Calculator </p>
 *
 * <p>Description: </p>
 * @author Ned Field,Nitin Gupta, E.V.Leyendecker
 * @version 1.0
 */
public class SDSsS1Calculator {

  public ArbitrarilyDiscretizedFunc calculateSDSsS1(ArbitrarilyDiscretizedFunc
      saVals, float fa, float fv,String siteClass) {
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
    function.set(saVals.getX(0), fa * saVals.getY(0) * 2.0 / 3.0);
    function.set(saVals.getX(1), fv * saVals.getY(1) * 2.0 / 3.0);
    String title = "Spectral Response Accelerations SDs and SD1";
    String subTitle = "SDs = 2/3 x SMs and SD1 = 2/3 x SM1";
    String SA = "Sa";
    String text1 = "SDs";
    String text2 = "SD1";
    String info ="";
    info += DataDisplayFormatter.createSubTitleString(subTitle,siteClass,fa,fv);
    info += DataDisplayFormatter.createFunctionInfoString(function,SA,text1,text2,siteClass);
    function.setInfo(info);
    return function;
  }

}
