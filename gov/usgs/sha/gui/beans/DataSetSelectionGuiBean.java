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

}
