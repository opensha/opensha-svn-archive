package scratch.kevin.simulators;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;

import com.google.common.collect.Lists;

public class QuietPeriodIdenMatcher implements RuptureIdentifier {
	
	private RuptureIdentifier matchIden;
	private double allowedAftershockYears;
	private double quietYears;
	private RuptureIdentifier[] quietMatchIdens;
	
	public QuietPeriodIdenMatcher(RuptureIdentifier matchIden, double allowedAftershockYears,
			double quietYears, List<RuptureIdentifier> quietMatchIdens) {
		this(matchIden, allowedAftershockYears, quietYears, quietMatchIdens.toArray(new RuptureIdentifier[0]));
	}
	
	public QuietPeriodIdenMatcher(RuptureIdentifier matchIden, double allowedAftershockYears,
			double quietYears, RuptureIdentifier... quietMatchIdens) {
		this.matchIden = matchIden;
		this.allowedAftershockYears = allowedAftershockYears;
		this.quietYears = quietYears;
		this.quietMatchIdens = quietMatchIdens;
	}

	@Override
	public boolean isMatch(EQSIM_Event event) {
		throw new IllegalStateException("Can't call this on QuietPeriod iden as we don't have the event list");
	}

	@Override
	public List<EQSIM_Event> getMatches(List<EQSIM_Event> events) {
		List<EQSIM_Event> matches = Lists.newArrayList(matchIden.getMatches(events));
		int origNum = matches.size();
		HashSet<Integer> nonQuiets = new HashSet<Integer>();
		
		for (RuptureIdentifier quietIden : quietMatchIdens) {
			// look for any ruptures within the window of any matches
			List<EQSIM_Event> quietMatches = quietIden.getMatches(events);
			
			int targetStartIndex = 0;
			for (int i=0; i<matches.size(); i++) {
				EQSIM_Event match = matches.get(i);
				double matchTime = match.getTimeInYears();
				double targetWindowStart = matchTime + allowedAftershockYears;
				double targetWindowEnd = matchTime + quietYears;
				
				for (int j=targetStartIndex; j<quietMatches.size(); j++) {
					double targetTime = quietMatches.get(j).getTimeInYears();
					if (targetTime <= targetWindowStart) {
						targetStartIndex = j;
						continue;
					}
					if (targetTime <= targetWindowEnd)
						// this means that we're in a quiet period, therefore this match isn't quiet
						nonQuiets.add(i);
					// skip to next match
					break;
				}
			}
		}
		List<Integer> nonQuietsList = Lists.newArrayList(nonQuiets);
		// sort low to high
		Collections.sort(nonQuietsList);
		// needs to be from high to low for removal
		Collections.reverse(nonQuietsList);
		for (int ind : nonQuietsList)
			matches.remove(ind);
		
		System.out.println("Quiet Matcher: "+matches.size()+"/"+origNum+" match with quiet period of "+quietYears+" years");
		Collections.sort(matches);
		
		return matches;
	}

	public double getQuietYears() {
		return quietYears;
	}

}
