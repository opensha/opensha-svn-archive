package scratch.UCERF3.data;

import org.opensha.commons.exceptions.InvalidRangeException;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

/**
 * This class contains year after which each magnitude is thought to be complete throughout the entire RELM region, 
 * taken from Table L9 of Felzer (2013, UCERF3_TI Appendix L; http://pubs.usgs.gov/of/2013/1165/pdf/ofr2013-1165_appendixL.pdf)
 * 
 * @author field
 *
 */
public class U3_EqkCatalogStatewideCompleteness extends IncrementalMagFreqDist  {

    public U3_EqkCatalogStatewideCompleteness() {
    	super(2.55,8.45,60);
    	setTolerance(delta/1000000);
    	
    	double[] magThresh = {2.5,4.0,5.1,5.6,6.0,6.9,7.1,7.2,7.4,8.0, 9.0};
    	double[] yearThresh = {2007,1997,1957,1942,1932,1910,1885,1870,1865,1850};
    	int yearIndex = 0;
    	int magIndex = 1;
    	double mag=magThresh[magIndex];
    	double year=yearThresh[yearIndex];
    	for(int i=0;i<size();i++) {
    		if(getX(i)<mag) {
    			set(i, year);
    		}
    		else {
    			yearIndex+=1;
    			magIndex+=1;
    	    	mag=magThresh[magIndex];
    	    	year=yearThresh[yearIndex];
    			set(i, year);
    		}
    	}
    	
//    	System.out.println(this);    		

    }

	
}
