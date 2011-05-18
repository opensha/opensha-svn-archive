package scratch.kevin;

import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;

public class LECDebug2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArbDiscrEmpiricalDistFunc distFunc = new ArbDiscrEmpiricalDistFunc();
		distFunc.set(1.00E-05,	-18.615744);
		distFunc.set(1.26E-05,	-18.025915);
		distFunc.set(1.58E-05,	-17.436085);
		distFunc.set(2.00E-05,	-16.846256);
		distFunc.set(2.51E-05,	-16.256428);
		distFunc.set(3.16E-05,	-15.666597);
		distFunc.set(3.98E-05,	-15.076768);
		distFunc.set(5.01E-05,	-14.486939);
		distFunc.set(6.31E-05,	-13.89711);
		distFunc.set(7.94E-05,	-13.307281);
		distFunc.set(1.00E-04,	-12.717451);
		distFunc.set(1.26E-04,	-12.127622);
		distFunc.set(1.58E-04,	-11.537792);
		distFunc.set(2.00E-04,	-10.947963);
		distFunc.set(2.51E-04,	-10.358133);
		distFunc.set(3.16E-04,	-9.768304);
		distFunc.set(3.98E-04,	-9.178475);
		distFunc.set(5.01E-04,	-8.588646);
		distFunc.set(6.31E-04,	-7.9988165);
		distFunc.set(7.94E-04,	-7.408987);
		distFunc.set(0.001,	-6.8191576);
		distFunc.set(0.001258925,	-6.229328);
		distFunc.set(0.001584893,	-5.6394987);
		distFunc.set(0.001995262,	-5.0496697);
		distFunc.set(0.002511886,	-4.4598403);
		distFunc.set(0.003162278,	-3.8700109);
		distFunc.set(0.003981072,	-3.2801816);
		distFunc.set(0.005011872,	-2.6903522);
		distFunc.set(0.006309574,	-2.1005228);
		distFunc.set(0.007943282,	-1.5106936);
		distFunc.set(0.01,	-0.92086416);
		distFunc.set(0.012589254,	-0.33103484);
		distFunc.set(0.015848933,	0.25879452);
		distFunc.set(0.019952623,	0.8486239);
		distFunc.set(0.025118865,	1.4384532);
		distFunc.set(0.031622775,	2.0282826);
		distFunc.set(0.039810717,	2.6181118);
		distFunc.set(0.050118722,	3.2079413);
		distFunc.set(0.06309573,	3.7977705);
		distFunc.set(0.07943282,	4.3876);
		distFunc.set(0.1,	4.9774294);
		distFunc.set(0.12589253,	5.5672584);
		distFunc.set(0.15848932,	6.157088);
		distFunc.set(0.19952624,	6.7469172);
		distFunc.set(0.25118864,	7.3367467);
		distFunc.set(0.31622776,	7.926576);
		distFunc.set(0.39810717,	8.516405);
		distFunc.set(0.5011872,	9.106235);
		distFunc.set(0.63095737,	9.696064);
		distFunc.set(0.7943282,	10.285893);
		distFunc.set(1.0,	10.875723);
		
		System.out.println(distFunc.getNum());
		
		ArbitrarilyDiscretizedFunc normCumDist = distFunc.getNormalizedCumDist();
		
		System.out.println(normCumDist.getNum());
		
		for (int k=0; k<normCumDist.getNum(); k++) {
			double x = normCumDist.getX(k);
			double distFuncY = distFunc.getY(k);
			double y = normCumDist.getY(k);
			double oneMinusY = 1-y;
			System.out.println(x+"\t"+distFuncY+"\t"+y+"\t"+oneMinusY);
		}
	}

}
