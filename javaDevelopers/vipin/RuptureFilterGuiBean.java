package javaDevelopers.vipin;

import javax.swing.JPanel;
import org.opensha.param.StringParameter;
import org.opensha.param.DoubleParameter;
import java.util.ArrayList;

/**
 * <p>Title: RuptureFilterGuiBean.java </p>
 * <p>Description: This gui Bean allows user to select various parameters to filter
 * the ruptures from the master rupture list </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class RuptureFilterGuiBean extends JPanel {

  // various parameter names
  private final static String SECTION_PARAM_NAME = "Fault Section";
  private final static String MIN_RUP_LENTH_PARAM_NAME="Min Rupture Length";
  private final static String MAX_RUP_LENTH_PARAM_NAME="Max Rupture Length";
  private final static String ALL = "All";
  // various parameters
  private StringParameter sectionParam;
  private DoubleParameter minRupLengthParam, maxRupLengthParam;

  public RuptureFilterGuiBean(ArrayList sectionNames) {
    sectionNames.add(0, ALL);
    initParamsAndEditor(sectionNames);
  }

  private void initParamsAndEditor(ArrayList sectionNames) {
    // parameter to allow user to choose section name
    sectionParam = new StringParameter(SECTION_PARAM_NAME, sectionNames, (String)sectionNames.get(0));
    // parameter to enter min rupture length
    minRupLengthParam = new DoubleParameter(MIN_RUP_LENTH_PARAM_NAME);
    //parameter to enter
  }



}