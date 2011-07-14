package scratch.UCERF3.simulatedAnnealing.params;

public enum NonnegativityConstraintType {
	/**
	 * sets rate to zero if they are perturbed to negative values (recommended - anneals much faster!)
	 */
	TRY_ZERO_RATES_OFTEN,
	/**
	 * re-perturb rates if they are perturbed to negative values 
	 */
	LIMIT_ZERO_RATES;
}