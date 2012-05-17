package scratch.kevin.ucerf3.inversion;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.hpc.mpj.taskDispatch.MPJTaskCalculator;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.XMLUtils;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.hazardMap.HazardCurveSetCalculator;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sra.calc.parallel.MPJ_EAL_Calc;
import org.opensha.sra.calc.parallel.ThreadedEALCalc;
import org.opensha.sra.gui.portfolioeal.Asset;
import org.opensha.sra.gui.portfolioeal.CalculationExceptionHandler;
import org.opensha.sra.gui.portfolioeal.Portfolio;
import org.opensha.sra.rtgm.RTGM;
import org.opensha.sra.rtgm.RTGM.Frequency;

public class MPJ_AssetRTGM_Calc extends MPJ_EAL_Calc {

	public MPJ_AssetRTGM_Calc(CommandLine cmd, Portfolio portfolio, Element el)
	throws IOException, DocumentException, InvocationTargetException {
		this(cmd, portfolio, el, null);
	}
	
	public MPJ_AssetRTGM_Calc(CommandLine cmd, Portfolio portfolio, Element el,
			File outputFile) throws IOException, DocumentException,
			InvocationTargetException {
		super(cmd, portfolio, el, outputFile);
		
		ThreadedEALCalc prevCalc = this.calc;
		
		this.calc = new ThreadedRTGMCalc(prevCalc.getAssets(), prevCalc.getERFs(), prevCalc.getIMRs(),
				this, maxSourceDistance);
	}
	
	private class ThreadedRTGMCalc extends ThreadedEALCalc {

		public ThreadedRTGMCalc(List<Asset> assets, ERF[] erfs,
				ScalarIMR[] imrs, CalculationExceptionHandler handler,
				double maxSourceDistance) {
			super(assets, erfs, imrs, handler, maxSourceDistance);
		}

		@Override
		public double[] calculateBatch(int[] batch) throws InterruptedException {
			double[] results = new double[batch.length];
			
			ArrayDeque<Asset> deque = new ArrayDeque<Asset>();
			for (int index : batch)
				deque.add(assets.get(index));
			this.stack = deque;
			int numThreads = imrs.length;
			
			ArrayList<RTGMCalcThread> threads = new ArrayList<RTGMCalcThread>();
			
			for (int i=0; i<numThreads; i++) {
				ERF erf;
				if (erfs.length > 1)
					erf = erfs[i];
				else
					erf = erfs[0];
				threads.add(new RTGMCalcThread(erf, imrs[i], sites[i], this));
			}
			
			// start the threSiteads
			for (Thread t : threads) {
				t.start();
			}
			
			HashMap<Asset, Double> rtgms = new HashMap<Asset, Double>();
			
			// join the threads
			for (RTGMCalcThread t : threads) {
				t.join();
				rtgms.putAll(t.rtgms);
			}
			
			for (int i=0; i<batch.length; i++) {
				int index = batch[i];
				Asset asset = assets.get(index);
				results[i] = rtgms.get(asset);
			}
			
			return results;
		}
		
	}
	
	private class RTGMCalcThread extends Thread {
		
		private ScalarIMR imr;
		private Site site;
		private ERF erf;
		private ThreadedRTGMCalc calc;
		public RTGMCalcThread(ERF erf, ScalarIMR imr, Site site, ThreadedRTGMCalc calc) {
			this.imr = imr;
			this.site = site;
			this.erf = erf;
			this.calc = calc;
		}
		
		private HazardCurveCalculator curveCalc;
		
		private HashMap<Asset, Double> rtgms = new HashMap<Asset, Double>();
		
		@Override
		public void run() {
			curveCalc = new HazardCurveCalculator();
			
			Asset asset = calc.popAsset();
			
			while (asset != null) {
				asset.siteSetup(site);
				ArbitrarilyDiscretizedFunc func = IMT_Info.getUSGS_SA_01_AND_02_Function();
				ArbitrarilyDiscretizedFunc logFunc = HazardCurveSetCalculator.getLogFunction(func);
				curveCalc.getHazardCurve(logFunc, asset.getSite(), imr, erf);
				func = HazardCurveSetCalculator.unLogFunction(func, logFunc);
				
				double rtgm = RTGM.create(func, Frequency.SA_0P20, null).get();
				
				rtgms.put(asset, rtgm);
				
				asset = calc.popAsset();
			}
		}
	}

	public static void main(String[] args) {
		args = MPJTaskCalculator.initMPJ(args);
		
		try {
			Options options = createOptions();
			
			CommandLine cmd = parse(options, args, MPJ_EAL_Calc.class);
			
			args = cmd.getArgs();
			
			if (args.length < 2 || args.length > 3) {
				System.err.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(MPJ_EAL_Calc.class)
						+" [options] <portfolio_file> <calculation_params_file> [<output_file>]");
				abortAndExit(2);
			}

			Portfolio portfolio = Portfolio.createPortfolio(new File(args[0]));

			Document doc = XMLUtils.loadDocument(new File(args[1]));
			Element root = doc.getRootElement();
			
			if (args.length == 2) {
				// batch mode
				
				Iterator<Element> it = root.elementIterator(BATCH_ELEMENT_NAME);
				
				while (it.hasNext()) {
					MPJ_AssetRTGM_Calc driver = new MPJ_AssetRTGM_Calc(cmd, portfolio, it.next());
					
					driver.run();
				}
			} else {
				File outputFile = new File(args[2]);
				
				MPJ_AssetRTGM_Calc driver = new MPJ_AssetRTGM_Calc(cmd, portfolio, root, outputFile);
				
				driver.run();
			}
			
			finalizeMPJ();
			
			System.exit(0);
		} catch (Throwable t) {
			abortAndExit(t);
		}
	}

}
