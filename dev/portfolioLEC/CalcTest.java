package portfolioLEC;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.param.Parameter;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sra.asset.Asset;
import org.opensha.sra.asset.Portfolio;
import org.opensha.sra.asset.io.CSVPortfolioParser;
import org.opensha.sra.calc.portfolioLEC.MomentMatchingPortfolioLECCalculator;

public class CalcTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Portfolio port = CSVPortfolioParser.loadSingleCSV(
				new File("/home/kevin/OpenSHA/portfolio_lec/Porter (24 Jun 2011) Portfolio LEC CEA proxy portfolio.csv"));
		
		ERF erf = new MeanUCERF2();
		erf.updateForecast();
		
		ScalarIMR imr = new CB_2008_AttenRel(null);
		imr.setParamDefaults();
		
		for (Asset asset : port) {
			Site site = asset.getSite();
			Iterator<Parameter<?>> siteParamsIt = imr.getSiteParamsIterator();
			while (siteParamsIt.hasNext()) {
				Parameter<?> param = siteParamsIt.next();
				if (!site.containsParameter(param))
					site.addParameter((Parameter)param.clone());
			}
		}
		
		MomentMatchingPortfolioLECCalculator momentLEC = new MomentMatchingPortfolioLECCalculator();
		
		ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
		for (int k=0; k<51; k++) {
			double x = Math.pow(10d, -5d + 0.1 * k);
			function.set(x, 0d);
		}
		momentLEC.calcProbabilityOfExceedanceCurve(imr, erf, port, function);
		
		System.out.println(function);
	}

}
