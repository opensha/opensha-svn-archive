package org.opensha.nshmp.util.ui;

import java.text.DecimalFormat;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncList;

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
  private static DecimalFormat annualExceedanceFormat = new DecimalFormat(
      "0.000E00#");

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
        " ,Fv = " + fv + "\n";
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
                                                String text2, String siteClass) {
    String dataInfo = "";
		dataInfo += "\n" + pad("Period", 2) + pad(saString,2) + "\n";
		dataInfo += colPad("(sec)","Period",2) + colPad("(g)", saString, 2) + "\n";
		/*
    dataInfo += "\nPeriod     " + saString + "\n";
    dataInfo += "(sec)       (g)\n";
*/

		dataInfo += colPad(periodFormat.format(function.getX(0)),"Period",2) +
			colPad(saValFormat.format(function.getY(0)),saString,2) +
			text1 + ", " + siteClass + "\n";
		dataInfo += colPad(periodFormat.format(function.getX(1)),"Period",2) +
			colPad(saValFormat.format(function.getY(1)),saString,2) +
			text2+ ", " + siteClass + "\n";
    /*dataInfo += periodFormat.format(function.getX(0)) + "     " +
        saValFormat.format(function.getY(0)) + "  " + text1 + "," +
        siteClass + "\n";
    dataInfo += periodFormat.format(function.getX(1)) + "     " +
        saValFormat.format(function.getY(1)) + "  " + text2 + "," +
        siteClass + "\n";
*/
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
  public static String createFunctionInfoString_HazardCurves(
      ArbitrarilyDiscretizedFunc
      function,
      String xAxisString, String yAxisString,
      String xAxisUnits, String yAxisUnits,
      String text) {
    String dataInfo = "";
		dataInfo += text + "\n" + pad(xAxisString, 2) + pad(yAxisString, 2) + "\n" +
			colPad("(" + xAxisUnits + ")", xAxisString, 2) + 
			colPad("(" + yAxisUnits + ")", yAxisString, 2) + "\n";

		for (int i = 0; i < function.getNum(); ++i) {
			dataInfo += colPad(saValFormat.format(function.getX(i)),xAxisString,2) +
				colPad(annualExceedanceFormat.format(function.getY(i)),yAxisString,2) +
				"\n";
	  }	
    return dataInfo;
  }

  /**
   * Formats the data to be displayed
   * @param functions DiscretizedFuncList
   * @param siteClass String
   * @return String
   */
  public static String createFunctionInfoString(DiscretizedFuncList
                                                functionList, String siteClass) {
    String dataInfo = "";
		dataInfo += "\n" + colPad("Period", 6, 2) + colPad("Sa", 6, 2) +
			colPad("Sd", 6, 2) + "\n";
		dataInfo += colPad("(sec)", 6, 2) + colPad("(g)", 6, 2) +
			colPad("(inches)", 6, 2) + "\n";

    ArbitrarilyDiscretizedFunc function1 = (ArbitrarilyDiscretizedFunc)
        functionList.get(1);
    ArbitrarilyDiscretizedFunc function2 = (ArbitrarilyDiscretizedFunc)
        functionList.get(0);
		for (int i = 0; i < function1.getNum(); ++i) {
			dataInfo += colPad(saValFormat.format(function1.getX(i)),"Period",2) +
				colPad(saValFormat.format(function1.getY(i)), 6, 2) +
				colPad(saValFormat.format(function2.getY(i)), 6, 2) + "\n";
    }

    return dataInfo;
  }

	public static String center(String str, int width) {
		int strLen = str.length();
		if (strLen >= width ) return str;
	
		String result = str;
		int dif = width - strLen;
		dif = dif / 2;
		for(int i = 0; i < dif; ++i) {
			result = " " + result;
		}
		while(result.length() < width) {
			result = result + " ";
		}
		return result;
	}

  public static String pad(String str, int padding) {
		int width = str.length() + (2*padding);
		return center(str, width);
	}

	public static String colPad(String str, String heading, int padding) {
		int width = heading.length();
		width += (2*padding);
		return center(str, width);
	}

	public static String colPad(String str, int headWidth, int padding) {
		int width = headWidth + (2*padding);
		return center(str, width);
	}
}
