package org.scec.sha.gui.infoTools;

import org.scec.data.function.DiscretizedFuncList;
import org.scec.gui.plot.jfreechart.DiscretizedFunctionXYDataSet;


/**
 * <p>Title: GraphWindowAPI</p>
 * <p>Description: This interface has to be implemented by the application that uses
 * GraphWindow class</p>
 * @author : Nitin Gupta
 * @version 1.0
 */

public interface GraphWindowAPI {

  /**
   *
   * @returns the instance to the JPanel showing the JFreechart adn metadata
   */
  public GraphPanel getGraphPanel();


  /**
   *
   * @returns the DiscretizedFuncList for all the data curves
   */
  public DiscretizedFuncList getCurveFunctionList();


  /**
   *
   * @returns the DiscretizedFunctionXYDataSet to the data
   */
  public DiscretizedFunctionXYDataSet getXY_DataSet();

}