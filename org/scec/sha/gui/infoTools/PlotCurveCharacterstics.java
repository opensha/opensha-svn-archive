package org.scec.sha.gui.infoTools;

import java.awt.Color;

/**
 * <p>Title: PlotCurveCharacterstics</p>
 * <p>Description: This class handle characterstic feature of each curve, like
 * its color, linetype(rendrer type), width</p>
 * @author : Nitin Gupta
 * @version 1.0
 */

public class PlotCurveCharacterstics {

  //shape of curve while plotting it
  private String curveType;
  //color of the color
  private Color curveColor;
  //width of the curve when plotted
  private double curveWidth;

  //num of continuous curves with same characterstics as above
  private int numCurvesWithSamePlottingFeatures =1;

  public PlotCurveCharacterstics() {};

  /**
   *
   * @param lineType
   * @param lineColor
   * @param lineWidth
   */
  public PlotCurveCharacterstics(String lineType,Color lineColor,double lineWidth){
    curveType =lineType;
    curveColor =lineColor;
    curveWidth =lineWidth;
  }

  /**
   * Sets the shape of curve
   * @param curveType
   */
  public void setCurveType(String curveType){
    this.curveType = curveType;
  }


  /**
   * Set curve color
   * @param curveColor
   */
  public void setCurveColor(Color curveColor){
    this.curveColor = curveColor;
  }

  /**
   * set the curve line width
   * @param width
   */
  public void setCurveWidth(double width){
    curveWidth =width;
  }

  /**
   * set the curve characterstics for plotting
   * @param lineType
   * @param lineColor
   * @param lineWidth
   */
  public void setCurveCharacterstics(String lineType,Color lineColor,double lineWidth){
    curveType = lineType;
    curveColor = lineColor;
    curveWidth = lineWidth;
  }

  /**
   *
   * @returns the shape of curve like line, dashed, triangle
   */
  public String getCurveType(){
    return curveType;
  }

  /**
   *
   * @returns the curve color
   */
  public Color getCurveColor(){
    return curveColor;
  }

  /**
   *
   * @returns the curve line width
   */
  public double getCurveWidth(){
    return curveWidth;
  }


  /**
   * Set the number of continuous curves with same number of plotting characterstics.
   * eg: can be of weighted functionlist in which all the individual curves
   * have same plotting characterstics.
   * @param num
   */
  public void setNumContinuousCurvesWithSameCharaceterstics(int num){
    numCurvesWithSamePlottingFeatures = num;
  }

  /**
   *
   * @returns the number of curves with same number curve plotting characterstics
   */
  public int getNumContinuousCurvesWithSameCharacterstics(){
    return numCurvesWithSamePlottingFeatures;
  }


}