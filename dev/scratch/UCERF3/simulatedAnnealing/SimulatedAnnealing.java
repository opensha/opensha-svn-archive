package scratch.UCERF3.simulatedAnnealing;

import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.params.CoolingScheduleType;
import scratch.UCERF3.simulatedAnnealing.params.GenerationFunctionType;
import scratch.UCERF3.simulatedAnnealing.params.NonnegativityConstraintType;

public interface SimulatedAnnealing {

	public void setCalculationParams(CoolingScheduleType coolingFunc,
			NonnegativityConstraintType nonnegativeityConstraintAlgorithm,
			GenerationFunctionType perturbationFunc);

	public CoolingScheduleType getCoolingFunc();

	public void setCoolingFunc(CoolingScheduleType coolingFunc);

	public NonnegativityConstraintType getNonnegativeityConstraintAlgorithm();

	public void setNonnegativeityConstraintAlgorithm(
			NonnegativityConstraintType nonnegativeityConstraintAlgorithm);

	public GenerationFunctionType getPerturbationFunc();

	public void setPerturbationFunc(GenerationFunctionType perturbationFunc);

	public double[] getBestSolution();

	public double getBestEnergy();

	public void setResults(double Ebest, double[] xbest);

	public void iterate(long numIterations);

	public void iterate(CompletionCriteria completion);

	public void iterate(long startIter, CompletionCriteria criteria);

}