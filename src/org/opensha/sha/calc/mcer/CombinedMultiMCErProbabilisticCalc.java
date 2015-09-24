package org.opensha.sha.calc.mcer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Averages Probabilistic MCEr results from multiple calculators
 * @author kevin
 *
 */
public class CombinedMultiMCErProbabilisticCalc extends
		AbstractMCErProbabilisticCalc {
	
	private List<AbstractMCErProbabilisticCalc> calcs;
	private double weightEach;
	
	public CombinedMultiMCErProbabilisticCalc(AbstractMCErProbabilisticCalc... calcs) {
		this(Lists.newArrayList(calcs));
	}
	
	public CombinedMultiMCErProbabilisticCalc(List<? extends AbstractMCErProbabilisticCalc> calcs) {
		Preconditions.checkArgument(!calcs.isEmpty());
		this.calcs = Collections.unmodifiableList(calcs);
		weightEach = 1d/(double)calcs.size();
	}

	@Override
	public DiscretizedFunc calc(Site site, Collection<Double> periods) {
		ArbitrarilyDiscretizedFunc result = new ArbitrarilyDiscretizedFunc();
		
		for (double period : periods) {
			double val = 0d;
			for (AbstractMCErProbabilisticCalc calc : calcs)
				val += weightEach*calc.calc(site, period);
			result.set(period, val);
		}
		
		return result;
	}

}
