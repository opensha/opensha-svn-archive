package gov.usgs.sha.data;

import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.data.function.DiscretizedFuncList;
import java.text.DecimalFormat;

/**
 * <p>Title: DataDisplayFormatter</p>
 *
 * <p>Description: This class formats the data to be displayed in the application.</p>
 *
 * @author Ned Field,Nitin Gupta , E.V.Leyendecker
 *
 * @version 1.0
 */
public final class DataDisplayFormatter {


  private static DecimalFormat periodFormat = new DecimalFormat("0.0#");
  private static DecimalFormat saValFormat = new DecimalFormat("0.000");


    /**
     * Creates the SubTitle info String
     * @param subtitle String
     * @param siteClass String
     * @param fa float
     * @param fv float
     * @return String
     */
    public static String createSubTitleString(String subtitle, String siteClass,
                                      float fa,
                                      float fv) {
    String dataInfo = "";
    dataInfo += subtitle + "\n";
    dataInfo += siteClass + " - " + " Fa = " + fa +
        " ,Fv = " + fv + "\n\n";
    return dataInfo;
  }


  /**
   * Formats the data to be displayed
   * @param function ArbitrarilyDiscretizedFunc
   * @param saString String
   * @param text1 String : First text to be displayed
   * @param text2 String : Second Text to be displayed
   * @param siteClass String
   * @return String
   */
  public static String createFunctionInfoString(ArbitrarilyDiscretizedFunc
                                                 function,
                                                 String saString, String text1,
                                                 String text2,String siteClass) {
    String dataInfo = "";
    dataInfo += "\nPeriod\t" + saString + "\n";
    dataInfo += "(sec)\t (g)\n";

    dataInfo += periodFormat.format(function.getX(0)) + "\t" +
        saValFormat.format(function.getY(0)) + "  " + text1 + "," +
        siteClass + "\n";
    dataInfo += periodFormat.format(function.getX(1)) + "\t" +
        saValFormat.format(function.getY(1)) + "  " + text2 + "," +
        siteClass + "\n\n";

    return dataInfo;
  }


  /**
   * Formats the data to be displayed
   * @param functions DiscretizedFuncList
   * @param siteClass String
   * @return String
   */
  public static String createFunctionInfoString(DiscretizedFuncList
                                                 functionList,String siteClass) {
    String dataInfo = "";
    dataInfo += "\nPeriod\t" + "Sa\t"+"Sd"+ "\n";
    dataInfo += "(sec)\t (g)\t (g)\n";



      ArbitrarilyDiscretizedFunc function1 = (ArbitrarilyDiscretizedFunc)functionList.get(1);
      ArbitrarilyDiscretizedFunc function2 = (ArbitrarilyDiscretizedFunc)functionList.get(0);
      for(int i=0;i<function1.getNum();++i) {
        dataInfo +=saValFormat.format(function1.getX(i))+"\t"+
            saValFormat.format(function1.getY(i))+"\t"+saValFormat.format(function2.getY(i))+"\n";
      }

    return dataInfo;
  }

}
