package scratch.peter.nshmp;

import static org.dom4j.DocumentHelper.*;
import static org.opensha.nshmp2.util.SourceRegion.WUS;
import static org.opensha.nshmp2.util.SourceType.FAULT;

import java.awt.geom.Point2D;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.Interpolate;
import org.opensha.commons.util.XMLUtils;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.erf.source.FaultERF;
import org.opensha.nshmp2.erf.source.FaultSource;
import org.opensha.nshmp2.erf.source.GridERF;
import org.opensha.nshmp2.erf.source.Sources;
import org.opensha.nshmp2.util.Utils;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import com.google.common.collect.Lists;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class CSEP_Forecast {

	private static final String TEMPLATE_PATH =
			"/Users/pmpowers/Documents/NSHMP/CSEP/csep-forecast-template-m5.xml";
	private static final String DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static final String issueDate, startDate, endDate;
	private static final IncrementalMagFreqDist modelMFD;
	
	static {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FMT);
		GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		cal.set(2010, 0, 0);
		issueDate = sdf.format(cal.getTime());
		cal.set(2008, 0, 0);
		startDate = sdf.format(cal.getTime());
		cal.set(2013, 0, 0);
		endDate = sdf.format(cal.getTime());
		
		modelMFD = new IncrementalMagFreqDist(5.0,  9.0,  41);
	}
	
	/**
	 * @param args
	 * 
	 * -- loop all GridERFs averaging MFDs at 4 nearest locations to CSEP grid
	 * -- grid source mfds may be different sizes :-(
	 * -- resample mfd into CSEP discretization
	 *     6.05    6.15   6.25   6.35
	 *     
	 */
	public static void main(String[] args) {
		GridERF erf = (GridERF) Sources.get("CAmap.21.ch.in");
		Location loc = NEHRP_TestCity.SAN_FRANCISCO.location();
		System.out.println(erf.getMFD(loc));
		
//		FaultERF erf = (FaultERF) Sources.get("brange.3dip.ch.in");
//		erf.updateForecast();
//		String sourceName = "997bcd Sevier/Toroweap fault zone (southern)";
//		
//		for (ProbEqkSource src : erf) {
//			if (!src.getName().equals(sourceName)) continue;
//			FaultSource fSrc = (FaultSource) src;
//			List<IncrementalMagFreqDist> mfds = fSrc.getMFDs();
//			for (IncrementalMagFreqDist mfd : mfds) {
////				System.out.println("Original cum rate: " + mfd.getCumRate(0));
//				System.out.println(mfd);
//				IncrementalMagFreqDist mfdResam = CSEP_Forecast.resample(
//					mfd, CSEP_Forecast.newForecastMFD(), true, true);
////				System.out.println("Resampled cum rate: " + mfdResam.getCumRate(0));
//				System.out.println(mfdResam);
//			}
//		}
	}
	
	
	
//	public static void createForecast(File out) {
//		Document doc = XMLUtils.loadDocument(TEMPLATE_PATH);
//		Element forecastData = initDocument(doc);
//		
//		// build location list of cells
//		List<?> cellElements = forecastData.elements("cell");
//		for ()
//		
//	}
	
//	private Document buildForecastDoc(
//			String name, 
//			String version, 
//			Date issueDate,
//			Date startDate,
//			Date endDate
//			) {
//		Element root = createElement("CSEPForecast");
//		root.addNamespace(null, CSEP_NS);
//		Document doc = DocumentHelper.createDocument(root);
//		root.addElement("modelName").addText(name);
//		root.addElement("version").addText(version);
//		root.addElement("author").addText("USGS");
//		root.addElement("forecastStartDate").addText(sdf.format(startDate));
//		root.addElement("forecastEndDate").addText(sdf.format(endDate));
//		root.addElement("defaultCellDimension")
//			.addAttribute("latRange", "0.1")
//			.addAttribute("latRange", "0.1");
//		root.addElement("defaultMagBinDimension").addText("0.1");
//		root.addElement("lastMagBinOpen").addText("1");
//		root.addElement("depthLayer")
//			.addAttribute("min", "0.0")
//			.addAttribute("max", "30.0");
//		
//		
//		doc.addElement("CSEPForecast");
//
//		return null;
//	}
	
	public void locLooper(LocationList locs, double binWidth) {
		
		// 
		
	}
	
	/**
	 * Returns an MFD for a {@code Location} that is assumed to be at the
	 * center of 4 grid nodes in the supplied {@code erf}.
	 * @param erf
	 * @param loc
	 * @param spacing of erf grid
	 * @return an MFD
	 */
	public static IncrementalMagFreqDist offsetGridMFD(GridERF erf, Location loc, double spacing) {
		double w = spacing / 2;
		double lat = loc.getLatitude();
		double lon = loc.getLongitude();
		List<Location> locs = Lists.newArrayList(
			new Location(lat - w, lon - w),
			new Location(lat + w, lon - w),
			new Location(lat + w, lon + w),
			new Location(lat - w, lon + w));
		return null;
//		List<>
//		SummedMagFreqDist summedMFD = new Summ
//		erf.getMFD(loc)
	}
	
	/**
	 * Returns a CSEP forecast MFD with all rates initialized to 0.
	 * @return
	 */
	public static IncrementalMagFreqDist newForecastMFD() {
		return new IncrementalMagFreqDist(5.0,  9.0,  41);
	}
	
	private static Element initDocument(Document doc) {
		Element fd = doc.getRootElement().element("forecastData");
		fd.element("modelName").setText("NSHMP 2008");
		fd.element("version").setText("3.0");
		fd.element("author").setText("USGS");
		fd.element("issueDate").setText("issueDate");
		fd.element("forecastStartDate").setText("startDate");
		fd.element("forecastEndDate").setText("endDate");
		fd.element("lastMagBinOpen").setText("0");
		return fd.element("depthLayer");
	}
	
	/**
	 * Resamples a magnitude frequency distribution preserving event rates. This
	 * algorithm distributes src rates according to the position of 
	 * src mags relative to those of dest. If a src mag exactly matches a dest mag,
	 * then 100% of the rate is applied to the dest mag bin. If a src mag exactly
	 * matches a dest mag bin edge, then 50% of the rate is assigned to the
	 * adjacent dest mags. Any mag or mag rate that falls outside the range
	 * spanned by dest is ignored.
	 * 
	 * @param src MFD
	 * @param dest MFD
	 * @return dest MFD
	 */
	public static IncrementalMagFreqDist resampleMFD(
			IncrementalMagFreqDist src,
			IncrementalMagFreqDist dest) {
		double d = dest.getDelta();
		double d2 = d / 2.0;
		double destMin = dest.getMinX() - d2;
		double destMax = dest.getMaxX() + d2;
		for (int i=0; i<src.getNum(); i++) {
			double srcMag = src.getX(i);
			double srcRate = src.getY(i);
			// skip out of range rates
			if (srcMag >= destMax || srcMag <= destMin) continue;
			// mag offset from bin center
			double destMag = dest.getClosestX(srcMag);
			double dM = srcMag - destMag;
			// rate wt for current bin
			double binWt = (Math.abs(dM) + d2) / d;
			// set rate
			int destIdx = dest.getClosestXIndex(srcMag);
			double destRate = dest.getY(destIdx);
			dest.set(destIdx, destRate + srcRate * binWt);
			// switch to bin index above or below [-1, 0, 1]
			destIdx += (int) Math.signum(dM);
			// skip if out of range
			if (destIdx < 0 || destIdx >= dest.getNum()) continue;
			// set rate
			destRate = dest.getY(destIdx);
			dest.set(destIdx, destRate + srcRate * binWt);
		}
		return dest;
	}
	
	/**
	 * MFD resampler. Algorithm determines the {@code dest} magnitudes bins that
	 * span the entire range of {@code src} magnitudes. It determines event
	 * rates via linear or logY interpolation of {@code src} rates, fills out
	 * {@code dest} with rates resampled from {@code src}, and scales
	 * {@code dest} to have the same cumulative rate or Mo rate as original.
	 * 
	 * @param src MFD
	 * @param dest MFD, rate values will be overwritten
	 * @param momentBalance {@code true} for Mo preservation, {@code false} for
	 *        cumulative rate
	 * @param logInterp {@code true} for logY interpolation, {@code false} for
	 *        linear
	 * @return a reference to the supplied {@code dest} MFD
	 */
	public static IncrementalMagFreqDist resample(IncrementalMagFreqDist src,
			IncrementalMagFreqDist dest, boolean momentBalance,
			boolean logInterp) {

		// src mfd points used as basis for interpolation
		double[] srcMags = new double[src.getNum()];
		double[] srcRates = new double[src.getNum()];
		int idx = 0;
		for (Point2D p : src) {
			srcMags[idx] = p.getX();
			srcRates[idx++] = p.getY();
		}

		// iterate dest
		int minDestIdx = dest.getClosestXIndex(src.getMinMagWithNonZeroRate());
		int maxDestIdx = dest.getClosestXIndex(src.getMaxMagWithNonZeroRate());
		for (int destIdx = minDestIdx; destIdx <= maxDestIdx; destIdx++) {
			// min and max indices are already clamped to dest min max
			double destMag = dest.getX(destIdx);
			double destRate = (logInterp) ? Interpolate.findLogY(srcMags,
				srcRates, destMag) : Interpolate.findY(srcMags, srcRates,
				destMag);
			dest.set(destIdx, destRate);
		}
		if (momentBalance) {
			dest.scaleToTotalMomentRate(src.getTotalMomentRate());
		} else {
			dest.scaleToCumRate(0, src.getCumRate(0));
		}
		return dest;
	}

}

//<?xml version='1.0' encoding='UTF-8'?>
//<CSEPForecast xmlns='http://www.scec.org/xml-ns/csep/forecast/0.1'>
//  <forecastData publicID='smi:org.scec/csep/forecast/1'>
//    <modelName>unknown</modelName>
//    <version>1.0</version>
//    <author>CSEP</author>
//    <issueDate>2005-06-18T10:30:00Z</issueDate>
//    <forecastStartDate>2006-01-01T00:00:00Z</forecastStartDate>
//    <forecastEndDate>2011-01-01T00:00:00Z</forecastEndDate>
//    <defaultCellDimension latRange='0.1' lonRange='0.1'/>
//    <defaultMagBinDimension>0.1</defaultMagBinDimension>
//    <lastMagBinOpen>1</lastMagBinOpen>
//    <depthLayer max='30.0' min='0.0'>
//      <cell lat='40.15' lon='-125.35'>
//        <bin m='5.0'>0.0</bin>
//        <bin m='5.1'>0.0</bin>
//        <bin m='5.2'>0.0</bin>
//        <bin m='5.3'>0.0</bin>
//        <bin m='5.4'>0.0</bin>
//        <bin m='5.5'>0.0</bin>
//        <bin m='5.6'>0.0</bin>
//        <bin m='5.7'>0.0</bin>
//        <bin m='5.8'>0.0</bin>
//        <bin m='5.9'>0.0</bin>
//        <bin m='6.0'>0.0</bin>
//        <bin m='6.1'>0.0</bin>
//        <bin m='6.2'>0.0</bin>
//        <bin m='6.3'>0.0</bin>
//        <bin m='6.4'>0.0</bin>
//        <bin m='6.5'>0.0</bin>
//        <bin m='6.6'>0.0</bin>
//        <bin m='6.7'>0.0</bin>
//        <bin m='6.8'>0.0</bin>
//        <bin m='6.9'>0.0</bin>
//        <bin m='7.0'>0.0</bin>
//        <bin m='7.1'>0.0</bin>
//        <bin m='7.2'>0.0</bin>
//        <bin m='7.3'>0.0</bin>
//        <bin m='7.4'>0.0</bin>
//        <bin m='7.5'>0.0</bin>
//        <bin m='7.6'>0.0</bin>
//        <bin m='7.7'>0.0</bin>
//        <bin m='7.8'>0.0</bin>
//        <bin m='7.9'>0.0</bin>
//        <bin m='8.0'>0.0</bin>
//        <bin m='8.1'>0.0</bin>
//        <bin m='8.2'>0.0</bin>
//        <bin m='8.3'>0.0</bin>
//        <bin m='8.4'>0.0</bin>
//        <bin m='8.5'>0.0</bin>
//        <bin m='8.6'>0.0</bin>
//        <bin m='8.7'>0.0</bin>
//        <bin m='8.8'>0.0</bin>
//        <bin m='8.9'>0.0</bin>
//        <bin m='9.0'>0.0</bin>
//      </cell>

