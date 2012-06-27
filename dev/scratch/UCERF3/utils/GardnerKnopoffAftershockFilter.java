package scratch.UCERF3.utils;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.exceptions.InvalidRangeException;
import org.opensha.commons.exceptions.XY_DataSetException;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;

/**
 * Utility class that stores relationship between event rates for clustered and
 * declustered earthquake catalogs.
 * 
 * @author Ned Field
 * @version $Id:$
 */
public class GardnerKnopoffAftershockFilter extends EvenlyDiscretizedFunc {
	
	// fraction of events that are mainshocks >= M 5
	// from Table 21 of Felzer's UCERF2 Appendix I (http://pubs.usgs.gov/of/2007/1437/i/of2007-1437i.pdf)
	public final static double FRACT_MAINSH_GTM5 = 4.17/7.5;

	private static GardnerKnopoffAftershockFilter instance;
	
	GutenbergRichterMagFreqDist allGR;
	GutenbergRichterMagFreqDist mainGR;
	
	static {
		instance = new GardnerKnopoffAftershockFilter(0.05, 9.95, 100);
	}
	
	/**
	 * Returns the value to scale the rate of the supplied magnitude by to
	 * generate a declustered event rate. The supplied magnitude is assumed to
	 * be for a complete catalog.
	 * @param m
	 * @return
	 */
	public static double scaleForMagnitude(double m) {
		checkArgument(m > instance.getMinX() && m < instance.getMaxX());
		return instance.getClosestY(m);
	}
	
	/**
	 * @param min
	 * @param max
	 * @param num
	 * @param fractMainshocksAtMgt5
	 * @throws XY_DataSetException
	 * @throws InvalidRangeException
	 */
	public GardnerKnopoffAftershockFilter(double min, double max, int num) throws XY_DataSetException,
			InvalidRangeException {
		super(min, max, num);

		allGR = new GutenbergRichterMagFreqDist(min, max, num);
		mainGR = new GutenbergRichterMagFreqDist(min, max, num);

		allGR.setAllButTotCumRate(allGR.getMinX(), allGR.getMaxX(), 1.0, 1.0);
		mainGR.setAllButTotCumRate(allGR.getMinX(), allGR.getMaxX(), 1.0, 0.8);
		int mag5index = allGR.getClosestXIndex(5.0 + allGR.getDelta() / 2);
		allGR.scaleToCumRate(mag5index, 1.0);
		mainGR.scaleToCumRate(mag5index, FRACT_MAINSH_GTM5);

		for (int i = 0; i < num; i++) {
			double fract = mainGR.getY(i) / allGR.getY(i);
			if (fract <= 1)
				set(i, fract);
			else
				set(i, 1.0);
		}


	}
	
	
	
	public static EvenlyDiscretizedFunc getKarensFractions() {
// 		from her table 10 in
//		4.0≤M<4.5	65%
//		4.5≤M<5.0	60%
//		5.0≤M<5.5	47%
//		5.5≤M<6.0	62%
//		6.0≤M<6.5	18%
//		6.5≤M<7.0	17%
//		7.0≤M<7.5	0
		
		EvenlyDiscretizedFunc fracFunc = new EvenlyDiscretizedFunc(4.25,7.25,7);
		fracFunc.set(0,1.0-0.65);
		fracFunc.set(1,1.0-0.60);
		fracFunc.set(2,1.0-0.47);
		fracFunc.set(3,1.0-0.62);
		fracFunc.set(4,1.0-0.18);
		fracFunc.set(5,1.0-0.17);
		fracFunc.set(6,1.0);
		
		return fracFunc;

	}
	
	public void plotResults() {
		 ArrayList<EvenlyDiscretizedFunc> hists = new ArrayList<EvenlyDiscretizedFunc>();
		 hists.add(allGR);
		 hists.add(mainGR);
		 // hists.add(allGR.getCumRateDist());
		 // hists.add(mainGR.getCumRateDist());
		 hists.add(this);
		 hists.add(getKarensFractions());
		
		 GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(hists, "Test");
		 graph.setX_AxisLabel("Mag");
		 graph.setY_AxisLabel("Fraction");
		 graph.setAxisLabelFontSize(18);
		 graph.setPlotLabelFontSize(20);
		 graph.setTickLabelFontSize(16);
		 graph.setYLog(true);

	}

	public static void main(String[] args) {
		GardnerKnopoffAftershockFilter test = new GardnerKnopoffAftershockFilter(0.05, 9.95, 100);
		test.plotResults();


	}

}
