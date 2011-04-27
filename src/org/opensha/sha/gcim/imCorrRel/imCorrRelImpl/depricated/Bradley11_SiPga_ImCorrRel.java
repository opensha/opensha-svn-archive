/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.sha.gcim.imCorrRel.imCorrRelImpl.depricated;


import org.opensha.commons.param.StringConstraint;
import org.opensha.sha.gcim.imCorrRel.ImCorrelationRelationship;
import org.opensha.sha.gcim.imr.param.IntensityMeasureParams.SI_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.IA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_InterpolatedParam;
import org.opensha.sha.imr.param.OtherParams.TectonicRegionTypeParam;
import org.opensha.sha.util.TectonicRegionType;

/**
 * <b>Title:</b>Bradley11_SiPga_ImCorrRel<br>
 *
 * <b>Description:</b>  This implements the Bradley 2011 correlation
 * relationship between SI and PGA 
 * 
 * See: Bradley BA (2011) Empirical Correlation between velocity and acceleration 
 * spectrum intensities, spectral and peak ground acceleration intensity measures 
 * from shallow crustal earthquakes (in prep) <p>
 *
 * @author Brendon Bradley
 * @version 1.0 1 JUly 2010
 * 
 * verified against the matlab code developed based on the above reference
 */

public class Bradley11_SiPga_ImCorrRel extends ImCorrelationRelationship {

    final static String C = "Bradley11_SiPga_ImCorrRel";
    public final static String NAME = "Bradley (2011) SI,PGA";
    public final static String SHORT_NAME = "Bradley2011";
    private static final long serialVersionUID = 1234567890987654353L;
    
    public final static String TRT_ACTIVE_SHALLOW = TectonicRegionType.ACTIVE_SHALLOW.toString();
    
    /**
     * no-argument constructor.
     */
    public Bradley11_SiPga_ImCorrRel() {

    	super();
        
    	initOtherParams();
      	initSupportedIntensityMeasureParams();
    }
    
    /**
     * Computes the correlation coefficient between SI and PGA
     * @return pearson correlation coefficient between lnSI and lnPGA
     */
    public double getImCorrelation(){
    	if ((imi.getName()==SA_InterpolatedParam.NAME&&imj.getName()==IA_Param.NAME)||
        		(imi.getName()==IA_Param.NAME&&imj.getName()==SA_InterpolatedParam.NAME)) {
    		
    		return 0.553;
    		
    	} else {
    		return Double.NaN;
    	}
    	
    	
    	//The standard deviation in the transformed z value of 0.027 is not presently considered
    }
    
    /**
     *  Creates other Parameters
     *  such as the tectonic region (and possibly others)
     */
    protected void initOtherParams() {
    	
    	// init other params defined in parent class
        super.initOtherParams();
        
    	// tectonic region
    	StringConstraint trtConstraint = new StringConstraint();
    	trtConstraint.addString(TRT_ACTIVE_SHALLOW);
    	trtConstraint.setNonEditable();
		tectonicRegionTypeParam = new TectonicRegionTypeParam(trtConstraint,TRT_ACTIVE_SHALLOW); // Constraint and default value
		
		// add these to the list
		otherParams.replaceParameter(tectonicRegionTypeParam.NAME, tectonicRegionTypeParam);
    }

    /**
     *  Creates the supported IM parameters (ASI and PGA).
     *  Makes the parameters noneditable.
     */
    protected void initSupportedIntensityMeasureParams() {

      // Create asiParam:
      siParam = new SI_Param();
      siParam.setNonEditable();
  	  //Create siParam
  	  pgaParam = new PGA_Param();
  	  pgaParam.setNonEditable();

      //Now add the supported IMi and IMj params to the two lists 
//	  supportedIMiParams.clear();       				supportedIMjParams.clear();
//	  supportedIMiParams.addParameter(siParam);			supportedIMjParams.addParameter(pgaParam);
//	  supportedIMiParams.addParameter(pgaParam);		supportedIMjParams.addParameter(siParam);
	  
	  supportedIMiParams.clear();       	supportedIMjParams.clear();
	  supportedIMiParams.add(siParam);		supportedIMjParams.add(pgaParam);
	  supportedIMiParams.add(pgaParam);		supportedIMjParams.add(siParam);
	  
    }

    /**
     * Returns the name of the object
     *
     */
    public String getName() {
      return NAME;
    }
    
    /**
     * Returns the short name of the object
     *
     */
    public String getShortName() {
      return SHORT_NAME;
    }
}

