package gov.usgs.sha.gui.beans;

import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
import javax.swing.border.TitledBorder;
import javax.swing.border.Border;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.ParameterChangeListener;
import org.scec.param.event.ParameterChangeEvent;

/**
 * <p>Title: AnalysisOptionSelectionGuiBean</p>
 *
 * <p>Description: Allows user to select from the choices of various analysis types.</p>
 * @author : Ned Field, Nitin Gupta and E.V. Leyendecker
 * @version 1.0
 */
public class AnalysisOptionSelectionGuiBean
    extends JPanel implements ParameterChangeListener{


  //static declaration for the analysis choices
  public static final String PROB_HAZ_CURVES = "Probabilistic hazard curves";
  public static final String PROB_UNIFORM_HAZ_RES = "Probabilistic Uniform Hazard Response Spectra";
  public static final String NEHRP = "NEHRP Recommended Provisions for Seismic Regulations for New Buildings and Other Structure";
  public static final String FEMA_273 = "FEMA 273,MCE Guidelines for the Seismic Rehabilitation of Buildings";
  public static final String FEMA_356 = "FEMA 356,Prestandard and Commentary for the Seismic Rehabilitation of Buildings";
  public static final String INTL_BUILDING_CODE = "International Building Code";
  public static final String INTL_RESIDENTIAL_CODE = "International Residential Code";
  public static final String INTL_EXIST_CODE = "International Existing Building Code";
  public static final String NFPA_5000 = "NFPA 5000 Building construction and safety code";
  public static final String ASCE_7 = "ASCE 7 standard , Minimum Design Loads for Building and other structures";

  private static final String ANALYSIS_CHOICES_PARAM_NAME = "Analysis Options";
  //Parameters that allows for the selection for the choices of analysis.
  private StringParameter analysisChoicesParam;



  //Parameters that allows for the selection for the choices of edition.
  private StringParameter editionChoicesParam;
  private static final String EDITION_PARAM_NAME = "Select Edition";


  //Parameters that allows for the selection for the choices of geographic region.
  private StringParameter geographicRegionSelectionParam;
  private final static String GEOGRAPHIC_REGION_SELECTION_PARAM_NAME = "Select Geographic Region";

  private static final String analysis_choices_info = "The User may perform an "+
      "analysis for a site by selecting from the options listed. The type of analysis "+
      "depends on the option selected. In all cases the site location may be specified "+
      "by latitude-longiude (recommended) or zip code. The brief description of the "+
      "options are intended to provide information for the knowledgeable user. The "+
      "description are not a substitute for technical knowledge of seismic design "+
      "and/or analysis.";



  //static declaration for the supported geographic regions
  private static final String CONTER_48_STATES = "Conterminous 48 States";
  private static final String ALASKA = "Alaska";
  private static final String HAWAII = "Hawaii";
  private static final String PUERTO_RICO = "Puerto Rico";
  private static final String CULEBRA = "Culebra";
  private static final String ST_CROIX = "St. Croix";
  private static final String ST_JOHN = "St. John";
  private static final String ST_THOMAS = "St. Thomas";
  private static final String VIEQUES = "Vieques";
  private static final String TUTUILA = "Tutuila";
  private static final String GUAM = "Guam";

  //static declaration of the data editions suppported within this framework.
  private static final String data_1996 = "1996 Data";
  private static final String data_2002 = "2002 Data";
  private static final String data_1998 = "1998 Data";
  private static final String data_2003 = "2003 Data";
  private static final String NEHRP_1997 = "1997 NEHRP Seismic Design Provisions";
  private static final String NEHRP_2000 = "2000 NEHRP Seismic Design Provisions";
  private static final String NEHRP_2003 = "2003 NEHRP Seismic Design Provisions";
  private static final String ASCE_1998 = "1998 ASCE 7 Standard";
  private static final String ASCE_2002 = "2002 ASCE 7 Stanadard";
  private static final String ASCE_2005 = "2005 ASCE 7 Standard";
  private static final String IBC_2000 = "2000 International Building Code";
  private static final String IBC_2003 = "2003 International Building Code";
  private static final String IBC_2004 = "2004 International Building Code";
  private static final String IBC_2006 = "2006 International Building Code";
  private static final String IRC_2000 = "2000 International Residential Code";
  private static final String IRC_2003 = "2003 International Residential Code";
  private static final String IRC_2004 = "2004 International Residential Code";
  private static final String IRC_2006 = "2006 International Residential Code";
  private static final String FEMA_273_DATA = "FEMA 273, MCE Guidelines for the Seismic Rehab. of Bldgs";
  private static final String FEMA_310_DATA = "FEMA 310";
  private static final String FEMA_356_DATA = "FEMA 356, Prestandard for Siesmic Rehab of Bldgs";
  private static final String ASCE_PRESTANDARD = "ASCE PreStandard";
  private static final String IEBC_2003 = "2003 International Existing Building Code";

  private JButton ExplainButton = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  private JDialog frame;
  private JTextPane explainationText;

  public AnalysisOptionSelectionGuiBean() {
    try {
      jbInit();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private void jbInit() throws Exception {

    //creating the list of analysis choices
    ArrayList analysisList = new ArrayList();

    //creating the Analysis Option list
    analysisList.add(PROB_HAZ_CURVES);
    analysisList.add(PROB_UNIFORM_HAZ_RES);
    analysisList.add(NEHRP);
    analysisList.add(FEMA_273);
    analysisList.add(FEMA_356);
    analysisList.add(INTL_BUILDING_CODE);
    analysisList.add(INTL_RESIDENTIAL_CODE);
    analysisList.add(INTL_EXIST_CODE);
    analysisList.add(NFPA_5000);
    analysisList.add(ASCE_7);

    //creating the String parameter for user user to make the choice for the analysis type.
    StringParameter analysisChoicesParam = new StringParameter(ANALYSIS_CHOICES_PARAM_NAME,
        analysisList,(String)analysisList.get(0));
    analysisChoicesParam.setInfo(analysis_choices_info);
    ConstrainedStringParameterEditor analysisChoicesParamEditor = new ConstrainedStringParameterEditor(analysisChoicesParam);
    this.setLayout(gridBagLayout1);
    ExplainButton.setText("Explain");
    this.add(analysisChoicesParamEditor,
             new GridBagConstraints(0, 0, 1, 1, 0.5, 0.0
                                    , GridBagConstraints.CENTER,
                                    GridBagConstraints.BOTH,
                                    new Insets(4, 4, 4, 4), 180, 0)); // get the panel for increasing the font and border
    this.add(ExplainButton, new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(8, 10, 7, 10), 32, 5));
    JPanel panel = analysisChoicesParamEditor.getOuterPanel();
    TitledBorder titledBorder1 = new TitledBorder(BorderFactory.createLineBorder(new Color( 80, 80, 140 ),3),"");
    titledBorder1.setTitleColor(new Color( 80, 80, 140 ));
    Font DEFAULT_LABEL_FONT = new Font( "Arial", Font.BOLD, 15 );
    titledBorder1.setTitleFont(DEFAULT_LABEL_FONT);
    titledBorder1.setTitle(ANALYSIS_CHOICES_PARAM_NAME);
    Border border1 = BorderFactory.createCompoundBorder(titledBorder1,BorderFactory.createEmptyBorder(0,0,3,0));
    panel.setBorder(border1);

  }

  public void parameterChange( ParameterChangeEvent event ) {
    String frameTitle = (String)event.getNewValue();
    if(frame !=null){
      frame.setTitle(frameTitle);
      setExplainationForSelectedAnalysisOption(frameTitle);
    }
    //analysisChoicesParamEditor.refreshParamEditor();
  }


  /*
   * Creating the parameter that allows user to choose the geographic region list
   *
   */
  private void createGeographicRegionSelectionParameter(){
    ArrayList  supportedRegionList = new ArrayList();

    String selectedAnalysisOption = (String)analysisChoicesParam.getValue();
    if(selectedAnalysisOption.equals(PROB_HAZ_CURVES)){
      supportedRegionList.add(CONTER_48_STATES);
      supportedRegionList.add(ALASKA);
      supportedRegionList.add(HAWAII);
      supportedRegionList.add(PUERTO_RICO);
      supportedRegionList.add(CULEBRA);
      supportedRegionList.add(ST_CROIX);
      supportedRegionList.add(ST_JOHN);
      supportedRegionList.add(ST_THOMAS);
      supportedRegionList.add(VIEQUES);
    }
    else if(selectedAnalysisOption.equals(PROB_UNIFORM_HAZ_RES)){
      supportedRegionList.add(CONTER_48_STATES);
      supportedRegionList.add(ALASKA);
      supportedRegionList.add(HAWAII);
      supportedRegionList.add(PUERTO_RICO);
      supportedRegionList.add(CULEBRA);
      supportedRegionList.add(ST_CROIX);
      supportedRegionList.add(ST_JOHN);
      supportedRegionList.add(ST_THOMAS);
      supportedRegionList.add(VIEQUES);
    }
    else if(selectedAnalysisOption.equals(NEHRP)){
      supportedRegionList.add(CONTER_48_STATES);
      supportedRegionList.add(ALASKA);
      supportedRegionList.add(HAWAII);
      supportedRegionList.add(PUERTO_RICO);
      supportedRegionList.add(CULEBRA);
      supportedRegionList.add(ST_CROIX);
      supportedRegionList.add(ST_JOHN);
      supportedRegionList.add(ST_THOMAS);
      supportedRegionList.add(VIEQUES);
      supportedRegionList.add(TUTUILA);
      supportedRegionList.add(GUAM);
    }
    else if(selectedAnalysisOption.equals(FEMA_273)){
      supportedRegionList.add(CONTER_48_STATES);
      supportedRegionList.add(ALASKA);
      supportedRegionList.add(HAWAII);
    }
    else if(selectedAnalysisOption.equals(FEMA_356)){
      supportedRegionList.add(CONTER_48_STATES);
      supportedRegionList.add(ALASKA);
      supportedRegionList.add(HAWAII);
    }
    else if(selectedAnalysisOption.equals(INTL_BUILDING_CODE)){
      supportedRegionList.add(CONTER_48_STATES);
      supportedRegionList.add(ALASKA);
      supportedRegionList.add(HAWAII);
      supportedRegionList.add(PUERTO_RICO);
      supportedRegionList.add(CULEBRA);
      supportedRegionList.add(ST_CROIX);
      supportedRegionList.add(ST_JOHN);
      supportedRegionList.add(ST_THOMAS);
      supportedRegionList.add(VIEQUES);
      supportedRegionList.add(TUTUILA);
      supportedRegionList.add(GUAM);
    }
    else if(selectedAnalysisOption.equals(INTL_RESIDENTIAL_CODE)){
      supportedRegionList.add(CONTER_48_STATES);
      supportedRegionList.add(ALASKA);
      supportedRegionList.add(HAWAII);
      supportedRegionList.add(PUERTO_RICO);
      supportedRegionList.add(CULEBRA);
      supportedRegionList.add(ST_CROIX);
      supportedRegionList.add(ST_JOHN);
      supportedRegionList.add(ST_THOMAS);
      supportedRegionList.add(VIEQUES);
      supportedRegionList.add(TUTUILA);
      supportedRegionList.add(GUAM);

    }
    else if(selectedAnalysisOption.equals(INTL_EXIST_CODE)){
      supportedRegionList.add(CONTER_48_STATES);
      supportedRegionList.add(ALASKA);
      supportedRegionList.add(HAWAII);
    }
    else if(selectedAnalysisOption.equals(NFPA_5000)){
      supportedRegionList.add(CONTER_48_STATES);
      supportedRegionList.add(ALASKA);
      supportedRegionList.add(HAWAII);
      supportedRegionList.add(PUERTO_RICO);
      supportedRegionList.add(CULEBRA);
      supportedRegionList.add(ST_CROIX);
      supportedRegionList.add(ST_JOHN);
      supportedRegionList.add(ST_THOMAS);
      supportedRegionList.add(VIEQUES);
      supportedRegionList.add(TUTUILA);
      supportedRegionList.add(GUAM);
    }
    else if(selectedAnalysisOption.equals(ASCE_7)){
      supportedRegionList.add(CONTER_48_STATES);
      supportedRegionList.add(ALASKA);
      supportedRegionList.add(HAWAII);
      supportedRegionList.add(PUERTO_RICO);
      supportedRegionList.add(CULEBRA);
      supportedRegionList.add(ST_CROIX);
      supportedRegionList.add(ST_JOHN);
      supportedRegionList.add(ST_THOMAS);
      supportedRegionList.add(VIEQUES);
      supportedRegionList.add(TUTUILA);
      supportedRegionList.add(GUAM);
    }

    geographicRegionSelectionParam = new StringParameter(GEOGRAPHIC_REGION_SELECTION_PARAM_NAME,
        supportedRegionList,(String)supportedRegionList.get(0));

  }

  /**
   *
   */
  private void createEditionSelectionParameter(){

    ArrayList  supportedEditionList = new ArrayList();
    String selectedAnalysisOption = (String)analysisChoicesParam.getValue();

    String selectedRegionOption = (String)geographicRegionSelectionParam.getValue();

    if(selectedAnalysisOption.equals(PROB_HAZ_CURVES)){
      if(selectedRegionOption.equals(ALASKA) || selectedRegionOption.equals(HAWAII))
        supportedEditionList.add(data_1998);
      else if(selectedRegionOption.equals(CONTER_48_STATES)){
        supportedEditionList.add(data_1996);
        supportedEditionList.add(data_2002);
      }
      else
        supportedEditionList.add(data_2003);
    }

    else if(selectedAnalysisOption.equals(PROB_UNIFORM_HAZ_RES)){
      if (selectedRegionOption.equals(ALASKA) || selectedRegionOption.equals(HAWAII))
        supportedEditionList.add(data_1998);
      else if (selectedRegionOption.equals(CONTER_48_STATES)) {
        supportedEditionList.add(data_1996);
        supportedEditionList.add(data_2002);
      }
      else
        supportedEditionList.add(data_2003);
    }
    else if(selectedAnalysisOption.equals(NEHRP)){
      supportedEditionList.add(NEHRP_1997);
      supportedEditionList.add(NEHRP_2000);
      supportedEditionList.add(NEHRP_2003);
    }
    else if(selectedAnalysisOption.equals(FEMA_273)){
      supportedEditionList.add(FEMA_273_DATA);
      supportedEditionList.add(FEMA_310_DATA);
      supportedEditionList.add(FEMA_356_DATA);
      supportedEditionList.add(ASCE_PRESTANDARD);
      supportedEditionList.add(IEBC_2003);
    }
    else if(selectedAnalysisOption.equals(FEMA_356)){
      supportedEditionList.add(FEMA_273_DATA);
      supportedEditionList.add(FEMA_310_DATA);
      supportedEditionList.add(FEMA_356_DATA);
      supportedEditionList.add(ASCE_PRESTANDARD);
      supportedEditionList.add(IEBC_2003);
    }
    else if(selectedAnalysisOption.equals(INTL_BUILDING_CODE)){
      supportedEditionList.add(IBC_2000);
      supportedEditionList.add(IBC_2003);
      supportedEditionList.add(IBC_2004);
      supportedEditionList.add(IBC_2006);
    }
    else if(selectedAnalysisOption.equals(INTL_RESIDENTIAL_CODE)){
      supportedEditionList.add(IRC_2000);
      supportedEditionList.add(IRC_2003);
      supportedEditionList.add(IRC_2004);
      supportedEditionList.add(IRC_2006);
    }
    else if(selectedAnalysisOption.equals(INTL_EXIST_CODE)){
      supportedEditionList.add(FEMA_273_DATA);
      supportedEditionList.add(FEMA_310_DATA);
      supportedEditionList.add(FEMA_356_DATA);
      supportedEditionList.add(ASCE_PRESTANDARD);
      supportedEditionList.add(IEBC_2003);
    }
    else if(selectedAnalysisOption.equals(NFPA_5000)){
      supportedEditionList.add(ASCE_1998);
      supportedEditionList.add(ASCE_2002);
      supportedEditionList.add(ASCE_2005);
    }
    else if(selectedAnalysisOption.equals(ASCE_7)){
      supportedEditionList.add(ASCE_1998);
      supportedEditionList.add(ASCE_2002);
      supportedEditionList.add(ASCE_2005);
    }

    editionChoicesParam = new StringParameter(EDITION_PARAM_NAME,supportedEditionList,
                                                   (String)supportedEditionList.get(0));
  }

  /**
   *
   * @param frameTitle String
   */
  private void setExplainationForSelectedAnalysisOption(String selectedAnalysisOption){
    if(selectedAnalysisOption.equals(PROB_HAZ_CURVES))
      this.explainationText.setText("Probabilistic Hazard Curves  - "+
                                    "This option allows the user to obtain "+
                                    "hazard curves for a number of acceleration "+
                                    "parameters, such as peak ground acceleration "+
                                    "or response spectral accleration.    "+
                                    "Data sets include the following: 48 conterminous states "+
                                    "- 1996 and 2002, Alaska - 1998, Hawaii - 1998, "+
                                    "Puerto Rico and the Virgin Islands - 2003.");
    else if(selectedAnalysisOption.equals(PROB_UNIFORM_HAZ_RES))
      this.explainationText.setText("Probabilistic Uniform Hazard Response Spectra  - "+
                                    "This option allows the user to obtain uniform hazard "+
                                    "response spectra for 2% probabililty of "+
                                    "exceedance in 50 years, 10% probability of "+
                                    "exceedance in 50 years, and in a few cases "+
                                    "for 5% probability of exceedance in 50 years.   "+
                                    "Data sets include the following: 48 conterminous "+
                                    "states - 1996 and 2002, Alaska - 1998, Hawaii - 1998, "+
                                    "Puerto Rico and the Virgin Islands - 2003. ");
    else if(selectedAnalysisOption.equals(NEHRP))
      this.explainationText.setText("NEHRP Recommended Provisions for Seismic "+
                                    "Regulations for New Buildings and Other "+
                                    "Structures  - This option may be used for "+
                                    "the 1997, 2000, and 2003 editions of the  "+
                                    "NEHRP Recommended Provisions for Seismic "+
                                    "Regulations for New Buildings and Other Structures.  "+
                                    "The user may calculate seismic design parameters "+
                                    "and response spectra (both for period and displacement), "+
                                    "for Site Class A through E.");
    else if(selectedAnalysisOption.equals(FEMA_273))
      this.explainationText.setText("FEMA 273, MCE Guidelines for the Seismic "+
                                    "Rehabilitation of Buildings  - "+
                                    "This option may be used for FEMA 273,  "+
                                    "MCE Guidelines for the Seismic Rehabilitation of Buildings "+
                                    "(1997).  The user may calculate seismic "+
                                    "design parameters and response spectra "+
                                    "(both for period and displacement), for "+
                                    "Site Class A through E.");
    else if(selectedAnalysisOption.equals(FEMA_356))
      this.explainationText.setText("FEMA 356, Prestandard and Commentary for "+
                                    "the Seismic Rehabilitation of Buildings  - "+
                                    "This option may be used for FEMA 356,  "+
                                    "Prestandard and Commentary for the Seismic "+
                                    "Rehabilitation of Buildings (2000).  The "+
                                    "user may calculate seismic design parameters "+
                                    "and response spectra (both for period and "+
                                    "displacement), for Site Class A through E.");
    else if(selectedAnalysisOption.equals(INTL_BUILDING_CODE))
        this.explainationText.setText("International Building Code  - This "+
                                      "option may be used for the 2000 and 2003 "+
                                      "editions of the  International Building Code.  "+
                                      "The user may calculate seismic design parameters "+
                                      "and response spectra (both for period and displacement), "+
                                      "for Site Class A through E.");
    else if(selectedAnalysisOption.equals(INTL_RESIDENTIAL_CODE))
      this.explainationText.setText("International Residential Code  - "+
                                    "This option may be used for the 2000, "+
                                    "2003, and 2004 editions of the  "+
                                    "International Residential Code.  The "+
                                    "user may determine the Seismic Design "+
                                    "Categories for the default Site Class D.");
    else if(selectedAnalysisOption.equals(INTL_EXIST_CODE))
      this.explainationText.setText("International Existing Building Code  - "+
                                    "This option may be used for the 1997, 2000, "+
                                    "and 2003 editions of the  International Existing "+
                                    "Building Code.  The user may calculate seismic "+
                                    "design parameters and response spectra "+
                                    "(both for period and displacement), "+
                                    "for Site Class A through E.");
    else if(selectedAnalysisOption.equals(NFPA_5000))
      this.explainationText.setText("NFPA 5000 Building Construction and Safety Code "+
                                   "- This option may be used for the 2000 edition "+
                                   "of the  NFPA 5000 Building Construction and "+
                                   "Safety Code.  The user may calculate seismic "+
                                   "design parameters and response spectra (both "+
                                   "for period and displacement), for Site Class A through E.");
    else if(selectedAnalysisOption.equals(ASCE_7))
      this.explainationText.setText("ASCE 7 Standard, Minimum Design Loads for "+
                                    "Buildings and Other Structures  - This option "+
                                    "may be used for the 1998 and 2002 editions "+
                                    "of the ASCE 7 Standard,  Minimum Design Loads "+
                                    "for Buildings and Other Structures.  "+
                                    "The user may calculate seismic design "+
                                    "parameters and response spectra (both for "+
                                    "period and displacement), for Site Class A through E.");
  }


  /**
   *
   * @param actionEvent ActionEvent
   */
  private void ExplainButton_actionPerformed(ActionEvent actionEvent) {
    if(frame == null)
      showSelectedAnalysisExplaination();
    frame.pack();
    frame.show();
  }

  /**
   *
   *
   */
  private void showSelectedAnalysisExplaination() {

    this.explainationText = new JTextPane();

    //Panel Parent
    Container parent = this;
    /*This loops over all the parent of this class until the parent is Frame(applet)
         this is required for the passing in the JDialog to keep the focus on the adjustable params
         frame*/
    while (! (parent instanceof JFrame) && parent != null)
      parent = parent.getParent();
    frame = new JDialog( (JFrame) parent);
    frame.setModal(true);
    frame.setSize(300, 300);
    frame.getContentPane().setLayout(new GridBagLayout());
    frame.getContentPane().add(explainationText,
                               new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(4, 4, 4, 4), 0, 0));

    //Adding Button to update the forecast
    JButton button = new JButton();
    button.setText("OK");
    button.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        button_actionPerformed(e);
      }
    });
    frame.getContentPane().add(button,
                               new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(4, 4, 4, 4), 0, 0));
    frame.show();
    frame.pack();

  }

  /**
   *
   * @param actionEvent ActionEvent
   */
  private void button_actionPerformed(ActionEvent actionEvent) {
    frame.dispose();
  }

}
