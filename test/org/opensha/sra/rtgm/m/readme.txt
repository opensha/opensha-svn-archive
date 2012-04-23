

This directory contains various files used to generate rtgm result sets
used for testing. RTGM_ResultBuilder.m generates hazard curve data and
computes risk-targeted ground motions for all NEHRP cities at 5Hz and 1sec
spectral accelerations.

RTGM_Calculator.m		: original m-file from Nico Luco
RTGM_ResultBuilder.m	: builds results.txt
Cities.m				: MatLab struct of NEHRP test city locations
results.txt				: result file that is ingested by RTGM_Tests.java
						  in parent directory
						  