package scratch.UCERF3.simulatedAnnealing.params;

public enum CoolingScheduleType {
	/**
	 * classical SA cooling schedule (Geman and Geman, 1984) (slow but ensures convergence)
	 */
	CLASSICAL_SA,
	/**
	 * fast SA cooling schedule (Szu and Hartley, 1987)
	 */
	FAST_SA,
	/**
	 * very fast SA cooling schedule (Ingber, 1989) (recommended)
	 */
	VERYFAST_SA;
}