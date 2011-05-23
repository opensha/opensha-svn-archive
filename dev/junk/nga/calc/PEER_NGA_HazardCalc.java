package junk.nga.calc;

import java.util.ArrayList;

import junk.nga.EqkRuptureFromNGA;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.xyz.ArbDiscrGeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.util.SiteTranslator;


/**
 * <p>Title: PEER_NGA_HazardCalc</p>
 * <p>Description: This class computes the XYZ data constituting of site and
 * IML or Prob value for that site . IML or Prob value are computed based on what
 * user has selected. If he ghas selected IML@Prob the iml is comoputed else
 * prob is computed.</p>
 * @author : Edward (Ned) Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class PEER_NGA_HazardCalc {

	SiteTranslator siteTranslator = null;



	public PEER_NGA_HazardCalc() {
		siteTranslator = new SiteTranslator();
	}


	/**
	 *
	 * @param rupture : Info for the stored rupture
	 * @param imr : Selected AttenuationRelationship model
	 * @param isProbAtIML : what is to be compute IML@prob or Prob@IML
	 * @param value : based on above paramter value for prob or IML, corresponding
	 * to which other has be computed.
	 * @return
	 */
	public  GeoDataSet getXYZData(EqkRuptureFromNGA rupture,
			ScalarIMR imr,boolean isProbAtIML,double value){

		//instance of the XYZ dataset to store the Lat, Lon and IML or Prob based n what the user has choosen
		//XYZ_DataSetAPI xyzDataSet;


		System.out.println("Rupture Name: ,Rupture Info:"+rupture.getEqkName()+", "+ rupture.getInfo());

		ArrayList siteList = rupture.getSiteList();
		int size = siteList.size();
		ArrayList siteVs30 = rupture.getSiteVs30();

		//rake ArrayList
		ArrayList rakeList = rupture.getObservedRuptureSiteRakeList();
		
		GeoDataSet xyz = new ArbDiscrGeoDataSet(true);

		//iterating over all the sites to get the IML or Prob value based on what user
		//has selected IMLAtProb or Prob@IML.
		for(int i=0;i<size;++i){

			Site site = (Site)siteList.get(i);
			//setting the rake for each site in the rupture
			rupture.setAveRake(((Double)rakeList.get(i)).doubleValue());
			imr.setSite(site);
			//setting the rupture inside the AttenuationRelationship
			imr.setEqkRupture(rupture);
			//setSiteParamsInIMR(imr,site,(Double)siteVs30.get(i));

			if(isProbAtIML) //if Prob@IML set the Intensity Measure Level
				xyz.set(site.getLocation(), imr.getExceedProbability(value));
			else{
				try{ //if IML@Prob set the Exceed Prob param for the Attenuation.
					xyz.set(site.getLocation(), imr.getIML_AtExceedProb(value));
				}catch(ParameterException e){
					throw new ParameterException(e.getMessage());
				}
			}
		}

		//setSiteParamsInIMR(imr,rupture
		return xyz;
	}




	/**
	 * set the site params in IMR according to basin Depth and vs 30
	 * @param imr
	 * @param lon
	 * @param lat
	 * @param vs30
	 * @param basinDepth
	 */
	/*private void setSiteParamsInIMR(AttenuationRelationshipAPI imr,Site site,Dou vs30 ) {
    Iterator it = imr.getSiteParamsIterator(); // get site params for this IMR
    while(it.hasNext()) {
      ParameterAPI tempParam = (ParameterAPI)it.next();
      //adding the site Params from the CVM, if site is out the range of CVM then it
      //sets the site with whatever site Parameter Value user has choosen in the application
      boolean flag = siteTranslator.setParameterValue(tempParam,vs30,Double.NaN);
      site.addParameter(tempParam);
    }
  }*/
}
