package gov.usgs.sha.gui.beans;

import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.ParameterChangeListener;
import org.scec.param.event.ParameterChangeEvent;

/**
 * <p>Title: DataSetSelectionGuiBean</p>
 *
 * <p>Description: Allows user to select from the choices of various analysis types.</p>
 * @author : Ned Field, Nitin Gupta and E.V. Leyendecker
 * @version 1.0
 */
public class DataSetSelectionGuiBean
    {


  //Parameters that allows for the selection for the choices of edition.
  private StringParameter editionChoicesParam;
  public static final String EDITION_PARAM_NAME = "Select Edition";


  //Parameters that allows for the selection for the choices of geographic region.
  private StringParameter geographicRegionSelectionParam;
  public final static String GEOGRAPHIC_REGION_SELECTION_PARAM_NAME = "Select Geographic Region";

  //parameter list that holds the parameters to be shown to the user in the application.
  private ParameterList paramList;
  private ParameterListEditor editor;



  public DataSetSelectionGuiBean() {
  }


  /**
   * Creating the Editor for user to choose the Geographic Region and Data edition
   */
  public void createDataSetEditor(){
    paramList = new ParameterList();
    paramList.addParameter(geographicRegionSelectionParam);
    paramList.addParameter(editionChoicesParam);

    editor = new ParameterListEditor(paramList);
    editor.setTitle("");
  }



  /**
   * Returns the parameter list editor that holds the Parameter List
   * @return ParameterListEditor
   */
  public ParameterListEditor getDatasetSelectionEditor(){
    return editor;
  }

  /**
   * Returns the selected geographic region parameter
   * @return String
   */
  public String getSelectedGeographicRegion(){
    return (String)geographicRegionSelectionParam.getValue();
  }


  /**
   * Returns the selected geographic region parameter
   * @return String
   */
  public String getSelectedDataSetEdition(){
    return (String)editionChoicesParam.getValue();
  }

  /*
   * Creating the parameter that allows user to choose the geographic region list
   *
   */
  public void createGeographicRegionSelectionParameter(ArrayList supportedRegionList) {

    geographicRegionSelectionParam = new StringParameter(
        GEOGRAPHIC_REGION_SELECTION_PARAM_NAME,
        supportedRegionList, (String) supportedRegionList.get(0));

  }


  /**
   *
   * @return ParameterAPI
   */
  public ParameterAPI getGeographicRegionSelectionParameter(){
    return geographicRegionSelectionParam;
  }

  /**
   *
   * @return ParameterAPI
   */
  public ParameterAPI getEditionSelectionParameter(){
    return editionChoicesParam;
  }


  /**
   * Creates the Parameter that allows user to select  the Editions based on the
   * selected Analysis and choosen geographic region.
   */
  public void createEditionSelectionParameter(ArrayList supportedEditionList) {
    editionChoicesParam = new StringParameter(EDITION_PARAM_NAME,
                                              supportedEditionList,
                                              (String) supportedEditionList.get(0));
    if (editor != null) {
      editor.replaceParameterForEditor(this.EDITION_PARAM_NAME,
                                       this.editionChoicesParam);
      editor.refreshParamEditor();
    }

  }




  /**
   * Creates the Parameter that allows user to select  the Editions based on the
   * selected Analysis and choosen geographic region.
   */
 /* private void createEditionSelectionParameter(){

    ArrayList  supportedEditionList = new ArrayList();
    String selectedAnalysisOption = (String)analysisChoicesParam.getValue();

    String selectedRegionOption = (String)geographicRegionSelectionParam.getValue();

    //Prob Haz. curves
    if(selectedAnalysisOption.equals(PROB_HAZ_CURVES)){
      //selected region is Alaska or Hawaii
      if(selectedRegionOption.equals(ALASKA) || selectedRegionOption.equals(HAWAII))
        supportedEditionList.add(data_1998);
      //selected region is conterminous 48 states
      else if(selectedRegionOption.equals(CONTER_48_STATES)){
        supportedEditionList.add(data_1996);
        supportedEditionList.add(data_2002);
      }
      else //if anything else is selected
        supportedEditionList.add(data_2003);
    }
    //Prob UHRS selected
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
    //NEHRP selected
    else if(selectedAnalysisOption.equals(NEHRP)){
      supportedEditionList.add(NEHRP_1997);
      supportedEditionList.add(NEHRP_2000);
      supportedEditionList.add(NEHRP_2003);
    }
    //FEMA 273 selected
    else if(selectedAnalysisOption.equals(FEMA_273)){
      supportedEditionList.add(FEMA_273_DATA);
      supportedEditionList.add(FEMA_310_DATA);
      supportedEditionList.add(FEMA_356_DATA);
      supportedEditionList.add(ASCE_PRESTANDARD);
      supportedEditionList.add(IEBC_2003);
    }

    //FEMA 356 selected
    else if(selectedAnalysisOption.equals(FEMA_356)){
      supportedEditionList.add(FEMA_273_DATA);
      supportedEditionList.add(FEMA_310_DATA);
      supportedEditionList.add(FEMA_356_DATA);
      supportedEditionList.add(ASCE_PRESTANDARD);
      supportedEditionList.add(IEBC_2003);
    }
    //International Building code
    else if(selectedAnalysisOption.equals(INTL_BUILDING_CODE)){
      supportedEditionList.add(IBC_2000);
      supportedEditionList.add(IBC_2003);
      supportedEditionList.add(IBC_2004);
      supportedEditionList.add(IBC_2006);
    }
    //International Residential Code
    else if(selectedAnalysisOption.equals(INTL_RESIDENTIAL_CODE)){
      supportedEditionList.add(IRC_2000);
      supportedEditionList.add(IRC_2003);
      supportedEditionList.add(IRC_2004);
      supportedEditionList.add(IRC_2006);
    }
    //International Existing Building code
    else if(selectedAnalysisOption.equals(INTL_EXIST_CODE)){
      supportedEditionList.add(FEMA_273_DATA);
      supportedEditionList.add(FEMA_310_DATA);
      supportedEditionList.add(FEMA_356_DATA);
      supportedEditionList.add(ASCE_PRESTANDARD);
      supportedEditionList.add(IEBC_2003);
    }
    //NFPA
    else if(selectedAnalysisOption.equals(NFPA_5000)){
      supportedEditionList.add(ASCE_1998);
      supportedEditionList.add(ASCE_2002);
      supportedEditionList.add(ASCE_2005);
    }
    //ASCE
    else if(selectedAnalysisOption.equals(ASCE_7)){
      supportedEditionList.add(ASCE_1998);
      supportedEditionList.add(ASCE_2002);
      supportedEditionList.add(ASCE_2005);
    }

    editionChoicesParam = new StringParameter(EDITION_PARAM_NAME,supportedEditionList,
                                                   (String)supportedEditionList.get(0));

    editionChoicesParam.addParameterChangeListener(this);
    if(editor !=null){
      editor.replaceParameterForEditor(this.EDITION_PARAM_NAME,
                                       this.editionChoicesParam);
      editor.refreshParamEditor();
    }
  }*/

}
