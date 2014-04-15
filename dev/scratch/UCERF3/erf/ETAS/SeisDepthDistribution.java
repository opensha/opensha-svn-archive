package scratch.UCERF3.erf.ETAS;

import java.util.ArrayList;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.gui.plot.GraphWindow;

public class SeisDepthDistribution {
	
	double[] depthVals = {0.0, 3.0, 6.0, 12.0, 24.0};
	double[] relWtVals = {0.0, 1.0, 10.0, 2.0, 0.0};
	ArbitrarilyDiscretizedFunc depthDistFunc;

	public SeisDepthDistribution() {
		depthDistFunc = new ArbitrarilyDiscretizedFunc();
		double tot=0;
		for(int i=0;i<depthVals.length;i++) {
			depthDistFunc.set(depthVals[i], relWtVals[i]);		
			tot += relWtVals[i];
		}
		double totArea = 0;
		for(int i=1;i<depthVals.length;i++) {
			totArea += 0.5*(depthVals[i]-depthVals[i-1])*Math.abs(relWtVals[i]-relWtVals[i-1])+Math.min(relWtVals[i],relWtVals[i-1])*(depthVals[i]-depthVals[i-1]);			
		}
		depthDistFunc.scale(1.0/totArea);
		
		HistogramFunction histFunc = new HistogramFunction(0.05, 240,0.1);
		for(int i=0;i<histFunc.getNum();i++)
			histFunc.set(i,depthDistFunc.getInterpolatedY(histFunc.getX(i)));
		
		HistogramFunction cumHistFunc = histFunc.getCumulativeDistFunction();
		cumHistFunc.scale(1.0/cumHistFunc.getMaxY());
		
		ArbitrarilyDiscretizedFunc inverseCumDepthDistFunc = new ArbitrarilyDiscretizedFunc();
		for(int i=0;i<cumHistFunc.getNum();i++) {
			if(i==0)
				inverseCumDepthDistFunc.set(0.0,cumHistFunc.getX(i));
			else
				inverseCumDepthDistFunc.set(cumHistFunc.getY(i),cumHistFunc.getX(i));
		}
		
		
		HistogramFunction testRandHistFunc = new HistogramFunction(0.05, 240,0.1);
		int numSamples = 1000000;
		for(int i=0;i<numSamples;i++) {
			double randDepth = inverseCumDepthDistFunc.getInterpolatedY(Math.random());
			testRandHistFunc.add(randDepth, 1.0);
		}
		testRandHistFunc.scale(1.0/(numSamples*0.1));

		ArrayList<XY_DataSet> funcs = new ArrayList<XY_DataSet>();
		funcs.add(depthDistFunc);
		funcs.add(testRandHistFunc);
		GraphWindow graph3 = new GraphWindow(histFunc, "histFunc"); 
		GraphWindow graph4 = new GraphWindow(histFunc.getCumulativeDistFunction(), "histFunc.getCumulativeDistFunction()"); 
		GraphWindow graph2 = new GraphWindow(inverseCumDepthDistFunc, "inverseCumDepthDistFunc"); 
		GraphWindow graph1 = new GraphWindow(funcs, "depthDistFunc"); 


	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		SeisDepthDistribution test = new SeisDepthDistribution();

	}

}
