/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb;

import java.util.ArrayList;

import org.opensha.refFaultParamDb.dao.exception.QueryException;
import org.opensha.refFaultParamDb.vo.DeformationModelSummary;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelSummaryDB_DAO;


/**
 * This class holds a deformation model summary (name, associated fault model) for each deformation model.
 * 
 * @author Ned Field
 *
 */
public class DeformationModelSummaryFinal {
	
	private static ArrayList<DeformationModelSummary> deformationModelSummariesList;

	  public DeformationModelSummaryFinal() {
		  // do this once to create file, and then comment it out
		  writeDeformationModelSummariesXML_File();
	  }

	  /**
	   * Get a deformation model based on deformation model ID
	   * 
	   * @param faultModelId
	   * @return
	   * @throws QueryException
	   */
	  public DeformationModelSummary getDeformationModel(int deformationModelId) throws QueryException {
		  // need to add code to get the summary from the list by Id
		DeformationModelSummary deformationModel=null;
	    return deformationModel;

	  }
	  
	  /**
	   * Get all the deformation Models from the database
	   * @return
	   * @throws QueryException
	   */
	  public ArrayList getAllDeformationModels() throws QueryException {
	   return deformationModelSummariesList;
	  }

	  
	  /**
	   * Get a deformation model based on deformation model Name
	   * 
	   * @param deformationModelName
	   * @return
	   * @throws QueryException
	   */
	  public DeformationModelSummary getDeformationModel(String deformationModelName) throws QueryException {
		  // need to add code to get the summary from the list by Name
		DeformationModelSummary deformationModel=null;
	    return deformationModel;

	  }
	  
	  /**
	   * This reads from the oracle database and writes the results to an XML file (only need to do once)
	   */
	  private void writeDeformationModelSummariesXML_File() {
			DeformationModelSummaryDB_DAO deformationModelSummaryDB_DAO = new DeformationModelSummaryDB_DAO(DB_AccessAPI.dbConnection);
			ArrayList<DeformationModelSummary> deformationModelSummariesFromDatabaseList = deformationModelSummaryDB_DAO.getAllDeformationModels();
			DeformationModelSummary deformationModelSummary;
/*			
			// test to see the results
			for(int i=0; i<deformationModelSummariesFromDatabaseList.size(); ++i) {
				deformationModelSummary = (DeformationModelSummary) deformationModelSummariesFromDatabaseList.get(i);
				System.out.print(deformationModelSummary.getDeformationModelId()+", ");
				System.out.print(deformationModelSummary.getDeformationModelName()+", ");
				System.out.print(deformationModelSummary.getContributor()+", ");	// this is an object with no toString() method
				System.out.print(deformationModelSummary.getFaultModel()+"\n");	// this is an object with no toString() method
			}
*/			
			// Need to add code to write these to XML file ****************
			
			//do this for now (until read from XML is implemented
			deformationModelSummariesList = deformationModelSummariesFromDatabaseList;
	  }
	  
	  /**
	   * This reads the XML file containing the deformation model summaries and puts them into deformationModelSummariesList
	   */
	  private void readDeformationModelSummariesXML_File() {
		// Need to add code to read these from XML ****************
	  }
	  
	  
	public static void main(String[] args) {
		DeformationModelSummaryFinal test = new DeformationModelSummaryFinal();
	}
}
