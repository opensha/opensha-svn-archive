package org.opensha.refFaultParamDb.gui;
import org.opensha.param.*;
import org.opensha.param.editor.*;
/**
 * <p>Title: ExactTimeGuiBean.java </p>
 * <p>Description: This GUI allows the user to enter all the information
 * so that user can enter all the information related to the Gregorian Calendar.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ExactTimeGuiBean extends ParameterListEditor{
    // Start-Time Parameters
    public final static String YEAR_PARAM_NAME = "Year";
    private IntegerParameter yearParam;
    private IntegerConstraint yearConstraint = new IntegerConstraint(0,Integer.MAX_VALUE);
    private final static Integer YEAR_PARAM_DEFAULT = new Integer(2005);
    public final static String MONTH_PARAM_NAME = "Month";
    private IntegerParameter monthParam;
    private IntegerConstraint monthConstraint = new IntegerConstraint(1,12);
    private final static Integer MONTH_PARAM_DEFAULT = new Integer(1);
    public final static String DAY_PARAM_NAME = "Day";
    private IntegerParameter dayParam;
    private final static Integer DAY_PARAM_DEFAULT = new Integer(1);
    private IntegerConstraint dayConstraint = new IntegerConstraint(1,31);
    public final static String HOUR_PARAM_NAME = "Hour";
    private IntegerParameter hourParam;
    private final static Integer HOUR_PARAM_DEFAULT = new Integer(0);
    private IntegerConstraint hourConstraint = new IntegerConstraint(0,59);
    public final static String MINUTE_PARAM_NAME = "Minute";
    private IntegerParameter minuteParam;
    private final static Integer MINUTE_PARAM_DEFAULT = new Integer(0);
    private IntegerConstraint minuteConstraint = new IntegerConstraint(0,59);
    public final static String SECOND_PARAM_NAME = "Second";
    private IntegerParameter secondParam;
    private final static Integer SECOND_PARAM_DEFAULT = new Integer(0);
    private IntegerConstraint secondConstraint = new IntegerConstraint(0,59);

  public ExactTimeGuiBean(String title) {
    initParamsList();
    this.addParameters();
    this.setTitle(title);
  }

  /**
   * Initialize the parameters
   */
  private void initParamsList() {
    parameterList = new ParameterList();
    yearParam = new IntegerParameter(YEAR_PARAM_NAME, yearConstraint, YEAR_PARAM_DEFAULT);
    monthParam = new IntegerParameter(MONTH_PARAM_NAME, monthConstraint, MONTH_PARAM_DEFAULT);
    dayParam = new IntegerParameter(DAY_PARAM_NAME, dayConstraint, DAY_PARAM_DEFAULT);
    hourParam = new IntegerParameter(HOUR_PARAM_NAME, hourConstraint,HOUR_PARAM_DEFAULT);
    minuteParam = new IntegerParameter(MINUTE_PARAM_NAME, minuteConstraint, MINUTE_PARAM_DEFAULT);
    secondParam = new IntegerParameter(SECOND_PARAM_NAME, secondConstraint, SECOND_PARAM_DEFAULT);
    parameterList.addParameter(yearParam);
    parameterList.addParameter(monthParam);
    parameterList.addParameter(dayParam);
    parameterList.addParameter(hourParam);
    parameterList.addParameter(minuteParam);
    parameterList.addParameter(secondParam);
  }


}