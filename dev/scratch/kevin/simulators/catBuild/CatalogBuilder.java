package scratch.kevin.simulators.catBuild;

import java.util.List;

import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;

import scratch.kevin.simulators.dists.RandomReturnPeriodProvider;

public interface CatalogBuilder {
	public List<EQSIM_Event> buildCatalog(List<EQSIM_Event> events,
			List<RandomReturnPeriodProvider> randomRPsList,
			List<List<EQSIM_Event>> eventListsToResample, boolean trim);
}