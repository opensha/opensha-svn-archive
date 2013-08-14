package scratch.kevin.simulators.dists;

import java.util.List;

import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;

import scratch.kevin.simulators.ElementMagRangeDescription;
import scratch.kevin.simulators.PeriodicityPlotter;
import scratch.kevin.simulators.RuptureIdentifier;
import scratch.kevin.simulators.dists.RandomCatalogBuilder.CatalogBuilder;
import scratch.kevin.simulators.dists.RandomCatalogBuilder.ProbabalisticCatalogBuilder;
import scratch.kevin.simulators.dists.RandomCatalogBuilder.StandardCatalogBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public enum RandomDistType {
		NORMAL("Random Normal Dist", "rand_norm_dist") {
			@Override
			public RandomReturnPeriodProvider instance(RuptureIdentifier rupIden, double[] rps, List<EQSIM_Event> events) {
				return new NormalDistReturnPeriodProvider(rps);
			}
		},
		EXPONENTIAL("Random Exponential Dist", "rand_exp_dist") {
			@Override
			public RandomReturnPeriodProvider instance(RuptureIdentifier rupIden, double[] rps, List<EQSIM_Event> events) {
				return new ExponentialDistReturnPeriodProvider(rps);
			}
		},
		LOG_NORMAL("Random Log-Normal Dist", "rand_lognorm_dist") {
			@Override
			public RandomReturnPeriodProvider instance(RuptureIdentifier rupIden, double[] rps, List<EQSIM_Event> events) {
				return new LogNormalDistReturnPeriodProvider(rps);
			}
		},
		POISSON("Random Poisson Dist", "rand_poisson_dist") {
			@Override
			public RandomReturnPeriodProvider instance(RuptureIdentifier rupIden, double[] rps, List<EQSIM_Event> events) {
				return new PoissonDistReturnPeriodProvider(rps);
			}
		},
		ACTUAL("Random Actual Dist", "rand_actual_dist") {
			@Override
			public RandomReturnPeriodProvider instance(RuptureIdentifier rupIden, double[] rps, List<EQSIM_Event> events) {
				return new ActualDistReturnPeriodProvider(rps);
			}
		},
		PREFERRED_SYN("Random Preferred Synthetic", "rand_preferred") {
			@Override
			public RandomReturnPeriodProvider instance(RuptureIdentifier rupIden, double[] rps, List<EQSIM_Event> events) {
				if (rupIden != null && rupIden instanceof ElementMagRangeDescription) {
					List<Integer> ids = ((ElementMagRangeDescription)rupIden).getElementIDs();
					if (ids.size() == 1 && ids.get(0) == ElementMagRangeDescription.SAN_JACINTO__ELEMENT_ID) {
						// San Jacinto
						List<RandomReturnPeriodProvider> provs = Lists.newArrayList();
						List<Double> weights = Lists.newArrayList();
						
						provs.add(new LogNormalDistReturnPeriodProvider(Math.log(98), 0.2));
						weights.add(1.0);
						provs.add(new LogNormalDistReturnPeriodProvider(Math.log(160), 0.15));
						weights.add(1.65);
						provs.add(new LogNormalDistReturnPeriodProvider(Math.log(235), 0.18));
						weights.add(0.95);
						
//						provs.add(new LogNormalDistReturnPeriodProvider(Math.log(98), 0.2));
//						weights.add(1.0);
//						provs.add(new LogNormalDistReturnPeriodProvider(Math.log(160), 0.15));
//						weights.add(1.65);
//						provs.add(new LogNormalDistReturnPeriodProvider(Math.log(235), 0.1));
//						weights.add(1.0);
						
						CompoundDistReturnPeriodProvider comp = new CompoundDistReturnPeriodProvider(provs, weights);
						
						// now invert a bit
//						return RIDistPlot.invertForSJDist(RIDistPlot.getHistFunc(rps), comp, 1000, 0.3, 5);
						return comp;
					}
				}
				// default: log normal
				return LOG_NORMAL.instance(rupIden, rps, events);
			}
		},
		MOJAVE_DRIVER("Mojave Driver Dist", "rand_mojave_driver_dist") {
			private List<EQSIM_Event> mojaveMatches;
			ElementMagRangeDescription mojaveIden;
			
			@Override
			public synchronized RandomReturnPeriodProvider instance(
					RuptureIdentifier rupIden, double[] rps, List<EQSIM_Event> events) {
				Preconditions.checkState(rupIden instanceof ElementMagRangeDescription);
				ElementMagRangeDescription elemIden = (ElementMagRangeDescription)rupIden;
				if (mojaveMatches == null) {
					mojaveIden = new ElementMagRangeDescription(ElementMagRangeDescription.smartName("SAF Mojave", elemIden),
							ElementMagRangeDescription.SAF_MOJAVE_ELEMENT_ID, elemIden.getMinMag(), elemIden.getMaxMag());
					mojaveMatches = mojaveIden.getMatches(events);
				}
				if (elemIden.getElementIDs().contains(ElementMagRangeDescription.SAF_MOJAVE_ELEMENT_ID))
					return ACTUAL.instance(rupIden, rps, events);
				
				List<EQSIM_Event> followerMatches = rupIden.getMatches(events);
				return new FollowerReturnPeriodProvider(events, mojaveIden, mojaveMatches, rupIden, followerMatches, 10d, 1500);
			}

			@Override
			public CatalogBuilder getBuilder() {
				return new ProbabalisticCatalogBuilder();
			}
		},
		MOJAVE_COACHELLA_CODRIVER("Mojave/Coachella Co-Driver Dist", "rand_coach_mojave_codriver_dist") {
			private List<EQSIM_Event> mojaveMatches;
			ElementMagRangeDescription mojaveIden;
			private List<EQSIM_Event> coachellaMatches;
			ElementMagRangeDescription coachellaIden;
			
			@Override
			public synchronized RandomReturnPeriodProvider instance(
					RuptureIdentifier rupIden, double[] rps, List<EQSIM_Event> events) {
				Preconditions.checkState(rupIden instanceof ElementMagRangeDescription);
				ElementMagRangeDescription elemIden = (ElementMagRangeDescription)rupIden;
				if (mojaveMatches == null) {
					mojaveIden = new ElementMagRangeDescription(ElementMagRangeDescription.smartName("SAF Mojave", elemIden),
							ElementMagRangeDescription.SAF_MOJAVE_ELEMENT_ID, elemIden.getMinMag(), elemIden.getMaxMag());
					mojaveMatches = mojaveIden.getMatches(events);
				}
				if (coachellaMatches == null) {
					coachellaIden = new ElementMagRangeDescription(ElementMagRangeDescription.smartName("SAF Coachella", elemIden),
							ElementMagRangeDescription.SAF_COACHELLA_ELEMENT_ID, elemIden.getMinMag(), elemIden.getMaxMag());
					coachellaMatches = coachellaIden.getMatches(events);
				}
				if (elemIden.getElementIDs().contains(ElementMagRangeDescription.SAF_MOJAVE_ELEMENT_ID))
//					return ACTUAL.instance(rupIden, rps, events);
					new FollowerReturnPeriodProvider(events, coachellaIden, coachellaMatches, mojaveIden, mojaveMatches, 10d, 1500);
				
				List<EQSIM_Event> followerMatches = rupIden.getMatches(events);
				return new FollowerReturnPeriodProvider(events, mojaveIden, mojaveMatches, rupIden, followerMatches, 10d, 1500);
			}

			@Override
			public CatalogBuilder getBuilder() {
				return new ProbabalisticCatalogBuilder();
			}
		};
		
		private String name, fNameAdd;
		private RandomDistType(String name, String fNameAdd) {
			this.name = name;
			this.fNameAdd = fNameAdd;
		}
		public String getName() {
			return name;
		}
		public String getFNameAdd() {
			return fNameAdd;
		}
		public abstract RandomReturnPeriodProvider instance(RuptureIdentifier rupIden, double[] rps, List<EQSIM_Event> events);
		
		public RandomReturnPeriodProvider instance(RuptureIdentifier rupIden, List<EQSIM_Event> events) {
			List<EQSIM_Event> matches = rupIden.getMatches(events);
			double[] rps = PeriodicityPlotter.getRPs(matches);
			return instance(rupIden, rps, events);
		}
		
		public CatalogBuilder getBuilder() {
			return new StandardCatalogBuilder();
		}
	}