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
import org.opensha.sha.gcim.imr.param.IntensityMeasureParams.ASI_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.DampingParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodInterpolatedParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_InterpolatedParam;
import org.opensha.sha.imr.param.OtherParams.TectonicRegionTypeParam;
import org.opensha.sha.util.TectonicRegionType;

/**
 * <b>Title:</b>Bradley11_AsiSa_ImCorrRel<br>
 *
 * <b>Description:</b>  This implements the Bradley 2011 correlation
 * relationship between ASI and SA 
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

public class Bradley11_AsiSa_ImCorrRel extends ImCorrelationRelationship {

    final static String C = "Bradley11_AsiSa_ImCorrRel";
    public final static String NAME = "Bradley (2011) ASI,SA";
    public final static String SHORT_NAME = "Bradley2011";
    private static final long serialVersionUID = 1234567890987654353L;
    
    public final static String TRT_ACTIVE_SHALLOW = TectonicRegionType.ACTIVE_SHALLOW.toString();
    
    private double t_min = 0.01, t_max = 10; //min and max periods
    
    //coefficients
    double[] a = {  0.9,   0.8,  1.059};
    double[] b = {  0.8, 0.953,   0.22};
    double[] c = {0.042,  0.15,   0.80};
    double[] d = {  2.2,   2.2,    0.7};
    double[] e = {0.075,   0.3,     10};
    double[] f = {    1,     1,    1.4};
    
    /**
     * no-argument constructor.
     */
    public Bradley11_AsiSa_ImCorrRel() {

    	super();
        
    	initOtherParams();
      	initSupportedIntensityMeasureParams();
        
      	this.ti = Double.NaN;
    }
    
    /**
     * Computes the correlation coefficient between ASI and SA at period ti
     * @param ti, spectral period in seconds
     * @return pearson correlation coefficient between lnASI and lnPGA
     */
    public double getImCorrelation(){
    	if ((imi.getName()==SA_InterpolatedParam.NAME&&imj.getName()==ASI_Param.NAME)||
        		(imi.getName()==ASI_Param.NAME&&imj.getName()==SA_InterpolatedParam.NAME)) {
    		
    		if (imi.getName()==SA_InterpolatedParam.NAME)
    			ti = ((SA_InterpolatedParam) imi).getPeriodInterpolatedParam().getValue();
    		else if (imj.getName()==SA_InterpolatedParam.NAME)
    			ti = ((SA_InterpolatedParam) imj).getPeriodInterpolatedParam().getValue();
    		
    		int i;
    		if (ti <=e[0] ) {
    			i = 0;
    		}
    	    else if (ti <= e[1]) {
    	    	i = 1;
    	    }
    	    else {
    			i = 2;
    	    }
    		return  (a[i]+b[i])/2-(a[i]-b[i])/2*Math.tanh(d[i]*Math.log(Math.pow(ti/c[i],f[i])));
    	//The standard deviation in the transformed z value is not presently considered
    		
    	} else {
    		return Double.NaN;
    	}
    		
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
     *  Creates the supported IM parameters (ASI and SA).
     *  Makes the parameters noneditable.
     */
    protected void initSupportedIntensityMeasureParams() {

      // Create asiParam:
      asiParam = new ASI_Param();
      asiParam.setNonEditable();
   // Create saParam:
  	  InterpPeriodiParam = new PeriodInterpolatedParam(t_min, t_max, 1.0, false);
  	  saiDampingParam = new DampingParam();
  	  saiInterpParam = new SA_InterpolatedParam(InterpPeriodiParam, saiDampingParam);
  	  saiInterpParam.setNonEditable();

      //Now add the supported IMi and IMj params to the two lists 
//	  supportedIMiParams.clear();       					supportedIMjParams.clear();
//	  supportedIMiParams.addParameter(asiParam);			supportedIMjParams.addParameter(saInterpParam);
//	  supportedIMiParams.addParameter(saInterpParam);		supportedIMjParams.addParameter(asiParam);
    
	  supportedIMiParams.clear();       			supportedIMjParams.clear();
	  supportedIMiParams.add(asiParam);				supportedIMjParams.add(saiInterpParam);
	  supportedIMiParams.add(saiInterpParam);		supportedIMjParams.add(asiParam);
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

