package scratch.UCERF3.utils;

import java.util.ArrayList;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.exceptions.InvalidRangeException;
import org.opensha.commons.exceptions.XY_DataSetException;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;

public class GardnerKnopoffAftershockFilter extends EvenlyDiscretizedFunc {
	
	  public GardnerKnopoffAftershockFilter(double min, double max, int num, double fractMainshocksAtMgt5) throws
      XY_DataSetException, InvalidRangeException {
    super(min, max, num);
    
    GutenbergRichterMagFreqDist allGR = new GutenbergRichterMagFreqDist(min, max, num);
    GutenbergRichterMagFreqDist mainGR = new GutenbergRichterMagFreqDist(min, max, num);
    
    allGR.setAllButTotCumRate(allGR.getMinX(), allGR.getMaxX(), 1.0, 1.0);
    mainGR.setAllButTotCumRate(allGR.getMinX(), allGR.getMaxX(), 1.0, 0.8);
    int mag5index = allGR.getClosestXIndex(5.0+allGR.getDelta()/2);
    allGR.scaleToCumRate(mag5index, 1.0);
    mainGR.scaleToCumRate(mag5index, fractMainshocksAtMgt5);
    
    for(int i=0;i<num; i++) {
    	double fract = mainGR.getY(i)/allGR.getY(i);
    	if(fract <= 1)
    		set(i,fract);
    	else
    		set(i,1.0);
    }
    
//	ArrayList<EvenlyDiscretizedFunc> hists = new ArrayList<EvenlyDiscretizedFunc>();
//	hists.add(allGR);
//	hists.add(mainGR);
////	hists.add(allGR.getCumRateDist());
////	hists.add(mainGR.getCumRateDist());
//	hists.add(this);
//	
//	GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(hists, "Test"); 
//	graph.setX_AxisLabel("Mag");
//	graph.setY_AxisLabel("Fraction");
//	graph.setAxisLabelFontSize(18);
//	graph.setPlotLabelFontSize(20);
//	graph.setTickLabelFontSize(16);
//	graph.setYLog(true);
 
  }

		public static void main(String[] args) {
			GardnerKnopoffAftershockFilter test = new GardnerKnopoffAftershockFilter(0.05,9.95,100,0.5);
			
		}

}
