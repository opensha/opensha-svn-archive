package scratchJavaDevelopers.martinez;

import java.util.ArrayList;

import org.opensha.data.function.DiscretizedFunc;

public class KLPGAVlnFn extends VulnerabilityModel {
	
	private double[] IML = {
			1.00E-003, 1.02E-003, 1.04E-003, 1.06E-003, 1.08E-003, 1.11E-003,
			1.13E-003, 1.15E-003, 1.17E-003, 1.20E-003, 1.22E-003, 1.25E-003,
			1.27E-003, 1.30E-003, 1.32E-003, 1.35E-003, 1.38E-003, 1.40E-003,
			1.43E-003, 1.46E-003, 1.49E-003, 1.52E-003, 1.55E-003, 1.58E-003,
			1.62E-003, 1.65E-003, 1.68E-003, 1.72E-003, 1.75E-003, 1.79E-003,
			1.82E-003, 1.86E-003, 1.90E-003, 1.93E-003, 1.97E-003, 2.01E-003,
			2.05E-003, 2.10E-003, 2.14E-003, 2.18E-003, 2.23E-003, 2.27E-003,
			2.32E-003, 2.36E-003, 2.41E-003, 2.46E-003, 2.51E-003, 2.56E-003,
			2.61E-003, 2.66E-003, 2.72E-003, 2.77E-003, 2.83E-003, 2.89E-003,
			2.94E-003, 3.00E-003, 3.06E-003, 3.13E-003, 3.19E-003, 3.25E-003,
			3.32E-003, 3.39E-003, 3.46E-003, 3.53E-003, 3.60E-003, 3.67E-003,
			3.74E-003, 3.82E-003, 3.90E-003, 3.97E-003, 4.06E-003, 4.14E-003,
			4.22E-003, 4.31E-003, 4.39E-003, 4.48E-003, 4.57E-003, 4.66E-003,
			4.76E-003, 4.86E-003, 4.95E-003, 5.05E-003, 5.16E-003, 5.26E-003,
			5.37E-003, 5.47E-003, 5.58E-003, 5.70E-003, 5.81E-003, 5.93E-003,
			6.05E-003, 6.17E-003, 6.30E-003, 6.42E-003, 6.55E-003, 6.69E-003,
			6.82E-003, 6.96E-003, 7.10E-003, 7.24E-003, 7.39E-003, 7.54E-003,
			7.69E-003, 7.85E-003, 8.00E-003, 8.17E-003, 8.33E-003, 8.50E-003,
			8.67E-003, 8.85E-003, 9.03E-003, 9.21E-003, 9.39E-003, 9.58E-003,
			9.78E-003, 9.97E-003, 1.02E-002, 1.04E-002, 1.06E-002, 1.08E-002,
			1.10E-002, 1.12E-002, 1.15E-002, 1.17E-002, 1.19E-002, 1.22E-002,
			1.24E-002, 1.27E-002, 1.29E-002, 1.32E-002, 1.35E-002, 1.37E-002,
			1.40E-002, 1.43E-002, 1.46E-002, 1.49E-002, 1.52E-002, 1.55E-002,
			1.58E-002, 1.61E-002, 1.64E-002, 1.68E-002, 1.71E-002, 1.75E-002,
			1.78E-002, 1.82E-002, 1.85E-002, 1.89E-002, 1.93E-002, 1.97E-002,
			2.01E-002, 2.05E-002, 2.09E-002, 2.13E-002, 2.18E-002, 2.22E-002,
			2.26E-002, 2.31E-002, 2.36E-002, 2.40E-002, 2.45E-002, 2.50E-002,
			2.55E-002, 2.61E-002, 2.66E-002, 2.71E-002, 2.77E-002, 2.82E-002,
			2.88E-002, 2.94E-002, 3.00E-002, 3.06E-002, 3.12E-002, 3.18E-002,
			3.25E-002, 3.31E-002, 3.38E-002, 3.45E-002, 3.52E-002, 3.59E-002,
			3.66E-002, 3.73E-002, 3.81E-002, 3.89E-002, 3.96E-002, 4.04E-002,
			4.13E-002, 4.21E-002, 4.29E-002, 4.38E-002, 4.47E-002, 4.56E-002,
			4.65E-002, 4.75E-002, 4.84E-002, 4.94E-002, 5.04E-002, 5.14E-002,
			5.25E-002, 5.35E-002, 5.46E-002, 5.57E-002, 5.68E-002, 5.80E-002,
			5.91E-002, 6.03E-002, 6.16E-002, 6.28E-002, 6.41E-002, 6.54E-002,
			6.67E-002, 6.80E-002, 6.94E-002, 7.08E-002, 7.22E-002, 7.37E-002,
			7.52E-002, 7.67E-002, 7.83E-002, 7.98E-002, 8.15E-002, 8.31E-002,
			8.48E-002, 8.65E-002, 8.82E-002, 9.00E-002, 9.18E-002, 9.37E-002,
			9.56E-002, 9.75E-002, 9.95E-002, 1.01E-001, 1.04E-001, 1.06E-001,
			1.08E-001, 1.10E-001, 1.12E-001, 1.14E-001, 1.17E-001, 1.19E-001,
			1.22E-001, 1.24E-001, 1.26E-001, 1.29E-001, 1.32E-001, 1.34E-001,
			1.37E-001, 1.40E-001, 1.43E-001, 1.45E-001, 1.48E-001, 1.51E-001,
			1.54E-001, 1.58E-001, 1.61E-001, 1.64E-001, 1.67E-001, 1.71E-001,
			1.74E-001, 1.78E-001, 1.81E-001, 1.85E-001, 1.89E-001, 1.92E-001,
			1.96E-001, 2.00E-001, 2.04E-001, 2.09E-001, 2.13E-001, 2.17E-001,
			2.21E-001, 2.26E-001, 2.30E-001, 2.35E-001, 2.40E-001, 2.45E-001,
			2.50E-001, 2.55E-001, 2.60E-001, 2.65E-001, 2.70E-001, 2.76E-001,
			2.81E-001, 2.87E-001, 2.93E-001, 2.99E-001, 3.05E-001, 3.11E-001,
			3.17E-001, 3.24E-001, 3.30E-001, 3.37E-001, 3.44E-001, 3.51E-001,
			3.58E-001, 3.65E-001, 3.72E-001, 3.80E-001, 3.88E-001, 3.95E-001,
			4.03E-001, 4.12E-001, 4.20E-001, 4.28E-001, 4.37E-001, 4.46E-001,
			4.55E-001, 4.64E-001, 4.73E-001, 4.83E-001, 4.93E-001, 5.03E-001,
			5.13E-001, 5.23E-001, 5.34E-001, 5.45E-001, 5.56E-001, 5.67E-001,
			5.78E-001, 5.90E-001, 6.02E-001, 6.14E-001, 6.26E-001, 6.39E-001,
			6.52E-001, 6.65E-001, 6.79E-001, 6.92E-001, 7.06E-001, 7.21E-001,
			7.35E-001, 7.50E-001, 7.65E-001, 7.81E-001, 7.96E-001, 8.12E-001,
			8.29E-001, 8.46E-001, 8.63E-001, 8.80E-001, 8.98E-001, 9.16E-001,
			9.34E-001, 9.53E-001, 9.73E-001, 9.92E-001, 1.01E+000, 1.03E+000,
			1.05E+000, 1.07E+000, 1.10E+000, 1.12E+000, 1.14E+000, 1.16E+000,
			1.19E+000, 1.21E+000, 1.24E+000, 1.26E+000, 1.29E+000, 1.31E+000,
			1.34E+000, 1.37E+000, 1.39E+000, 1.42E+000, 1.45E+000, 1.48E+000,
			1.51E+000, 1.54E+000, 1.57E+000, 1.60E+000, 1.64E+000, 1.67E+000,
			1.70E+000, 1.74E+000, 1.77E+000, 1.81E+000, 1.84E+000, 1.88E+000,
			1.92E+000, 1.96E+000, 2.00E+000, 2.04E+000, 2.08E+000, 2.12E+000,
			2.16E+000, 2.21E+000, 2.25E+000, 2.30E+000, 2.34E+000, 2.39E+000,
			2.44E+000, 2.49E+000, 2.54E+000, 2.59E+000, 2.64E+000, 2.70E+000,
			2.75E+000, 2.81E+000, 2.86E+000, 2.92E+000, 2.98E+000, 3.04E+000,
			3.10E+000, 3.17E+000, 3.23E+000, 3.29E+000, 3.36E+000, 3.43E+000,
			3.50E+000, 3.57E+000, 3.64E+000, 3.71E+000, 3.79E+000, 3.87E+000,
			3.94E+000, 4.02E+000, 4.11E+000, 4.19E+000, 4.27E+000, 4.36E+000,
			4.45E+000, 4.54E+000, 4.63E+000, 4.72E+000, 4.82E+000, 4.91E+000,
			5.01E+000, 5.12E+000, 5.22E+000, 5.32E+000, 5.43E+000, 5.54E+000,
			5.65E+000, 5.77E+000, 5.88E+000, 6.00E+000, 6.12E+000, 6.25E+000,
			6.37E+000, 6.50E+000, 6.63E+000, 6.77E+000, 6.91E+000, 7.04E+000,
			7.19E+000, 7.33E+000, 7.48E+000, 7.63E+000, 7.79E+000, 7.94E+000,
			8.10E+000, 8.27E+000, 8.43E+000, 8.60E+000, 8.78E+000, 8.96E+000,
			9.14E+000, 9.32E+000, 9.51E+000, 9.70E+000, 9.90E+000
	};
	
	private double [] DF = {
			3.11E-054, 6.38E-054, 1.31E-053, 2.67E-053, 5.43E-053, 1.10E-052,
			2.23E-052, 4.51E-052, 9.09E-052, 1.83E-051, 3.66E-051, 7.32E-051,
			1.46E-050, 2.90E-050, 5.75E-050, 1.14E-049, 2.24E-049, 4.42E-049,
			8.67E-049, 1.70E-048, 3.31E-048, 6.45E-048, 1.25E-047, 2.43E-047,
			4.69E-047, 9.04E-047, 1.74E-046, 3.33E-046, 6.37E-046, 1.21E-045,
			2.31E-045, 4.39E-045, 8.30E-045, 1.57E-044, 2.95E-044, 5.55E-044,
			1.04E-043, 1.94E-043, 3.62E-043, 6.74E-043, 1.25E-042, 2.31E-042,
			4.27E-042, 7.87E-042, 1.44E-041, 2.65E-041, 4.84E-041, 8.82E-041,
			1.60E-040, 2.91E-040, 5.27E-040, 9.51E-040, 1.71E-039, 3.08E-039,
			5.51E-039, 9.86E-039, 1.76E-038, 3.13E-038, 5.55E-038, 9.83E-038,
			1.74E-037, 3.06E-037, 5.38E-037, 9.43E-037, 1.65E-036, 2.88E-036,
			5.01E-036, 8.71E-036, 1.51E-035, 2.61E-035, 4.50E-035, 7.74E-035,
			1.33E-034, 2.28E-034, 3.89E-034, 6.63E-034, 1.13E-033, 1.91E-033,
			3.24E-033, 5.47E-033, 9.21E-033, 1.55E-032, 2.60E-032, 4.34E-032,
			7.25E-032, 1.21E-031, 2.01E-031, 3.33E-031, 5.50E-031, 9.08E-031,
			1.50E-030, 2.46E-030, 4.03E-030, 6.59E-030, 1.08E-029, 1.75E-029,
			2.84E-029, 4.61E-029, 7.45E-029, 1.20E-028, 1.94E-028, 3.11E-028,
			4.99E-028, 7.98E-028, 1.27E-027, 2.03E-027, 3.22E-027, 5.11E-027,
			8.08E-027, 1.28E-026, 2.01E-026, 3.16E-026, 4.95E-026, 7.75E-026,
			1.21E-025, 1.89E-025, 2.93E-025, 4.55E-025, 7.04E-025, 1.09E-024,
			1.68E-024, 2.58E-024, 3.96E-024, 6.06E-024, 9.27E-024, 1.41E-023,
			2.15E-023, 3.27E-023, 4.96E-023, 7.50E-023, 1.13E-022, 1.70E-022,
			2.56E-022, 3.84E-022, 5.75E-022, 8.60E-022, 1.28E-021, 1.91E-021,
			2.83E-021, 4.19E-021, 6.20E-021, 9.15E-021, 1.35E-020, 1.98E-020,
			2.91E-020, 4.25E-020, 6.21E-020, 9.06E-020, 1.32E-019, 1.91E-019,
			2.77E-019, 4.01E-019, 5.79E-019, 8.35E-019, 1.20E-018, 1.72E-018,
			2.47E-018, 3.52E-018, 5.03E-018, 7.16E-018, 1.02E-017, 1.44E-017,
			2.04E-017, 2.89E-017, 4.07E-017, 5.73E-017, 8.04E-017, 1.13E-016,
			1.58E-016, 2.20E-016, 3.07E-016, 4.27E-016, 5.94E-016, 8.23E-016,
			1.14E-015, 1.57E-015, 2.17E-015, 2.98E-015, 4.09E-015, 5.61E-015,
			7.67E-015, 1.05E-014, 1.43E-014, 1.94E-014, 2.64E-014, 3.58E-014,
			4.84E-014, 6.54E-014, 8.81E-014, 1.19E-013, 1.59E-013, 2.14E-013,
			2.86E-013, 3.82E-013, 5.09E-013, 6.78E-013, 9.01E-013, 1.20E-012,
			1.58E-012, 2.09E-012, 2.76E-012, 3.63E-012, 4.78E-012, 6.27E-012,
			8.22E-012, 1.08E-011, 1.40E-011, 1.83E-011, 2.38E-011, 3.09E-011,
			4.01E-011, 5.19E-011, 6.71E-011, 8.65E-011, 1.11E-010, 1.43E-010,
			1.84E-010, 2.36E-010, 3.01E-010, 3.85E-010, 4.90E-010, 6.24E-010,
			7.93E-010, 1.01E-009, 1.27E-009, 1.61E-009, 2.03E-009, 2.56E-009,
			3.22E-009, 4.05E-009, 5.08E-009, 6.35E-009, 7.94E-009, 9.91E-009,
			1.23E-008, 1.54E-008, 1.91E-008, 2.36E-008, 2.93E-008, 3.62E-008,
			4.46E-008, 5.50E-008, 6.76E-008, 8.30E-008, 1.02E-007, 1.25E-007,
			1.52E-007, 1.86E-007, 2.27E-007, 2.76E-007, 3.35E-007, 4.06E-007,
			4.92E-007, 5.95E-007, 7.18E-007, 8.65E-007, 1.04E-006, 1.25E-006,
			1.50E-006, 1.80E-006, 2.15E-006, 2.57E-006, 3.07E-006, 3.78E-006,
			4.70E-006, 5.82E-006, 7.20E-006, 8.88E-006, 1.09E-005, 1.34E-005,
			1.65E-005, 2.01E-005, 2.46E-005, 2.99E-005, 3.63E-005, 4.48E-005,
			5.73E-005, 7.30E-005, 9.24E-005, 1.16E-004, 1.46E-004, 1.82E-004,
			2.26E-004, 2.80E-004, 3.44E-004, 4.22E-004, 5.15E-004, 6.26E-004,
			7.57E-004, 9.12E-004, 1.09E-003, 1.31E-003, 1.56E-003, 1.85E-003,
			2.18E-003, 2.56E-003, 3.01E-003, 3.51E-003, 4.09E-003, 4.74E-003,
			5.49E-003, 6.32E-003, 7.26E-003, 8.31E-003, 9.48E-003, 1.08E-002,
			1.22E-002, 1.38E-002, 1.56E-002, 1.75E-002, 1.97E-002, 2.20E-002,
			2.45E-002, 2.73E-002, 3.02E-002, 3.34E-002, 3.69E-002, 4.06E-002,
			4.46E-002, 4.89E-002, 5.34E-002, 5.83E-002, 6.34E-002, 6.88E-002,
			7.46E-002, 8.06E-002, 8.70E-002, 9.37E-002, 1.01E-001, 1.08E-001,
			1.16E-001, 1.24E-001, 1.32E-001, 1.41E-001, 1.49E-001, 1.59E-001,
			1.68E-001, 1.78E-001, 1.88E-001, 1.98E-001, 2.09E-001, 2.20E-001,
			2.31E-001, 2.42E-001, 2.53E-001, 2.65E-001, 2.77E-001, 2.89E-001,
			3.01E-001, 3.13E-001, 3.26E-001, 3.38E-001, 3.51E-001, 3.64E-001,
			3.76E-001, 3.89E-001, 4.02E-001, 4.15E-001, 4.28E-001, 4.40E-001,
			4.53E-001, 4.66E-001, 4.79E-001, 4.91E-001, 5.04E-001, 5.16E-001,
			5.29E-001, 5.41E-001, 5.53E-001, 5.65E-001, 5.77E-001, 5.88E-001,
			6.00E-001, 6.11E-001, 6.22E-001, 6.33E-001, 6.44E-001, 6.55E-001,
			6.65E-001, 6.75E-001, 6.85E-001, 6.95E-001, 7.05E-001, 7.14E-001,
			7.23E-001, 7.32E-001, 7.41E-001, 7.50E-001, 7.58E-001, 7.66E-001,
			7.74E-001, 7.82E-001, 7.90E-001, 7.97E-001, 8.04E-001, 8.11E-001,
			8.18E-001, 8.24E-001, 8.31E-001, 8.37E-001, 8.43E-001, 8.48E-001,
			8.54E-001, 8.60E-001, 8.65E-001, 8.70E-001, 8.75E-001, 8.80E-001,
			8.84E-001, 8.89E-001, 8.93E-001, 8.97E-001, 9.01E-001, 9.05E-001,
			9.09E-001, 9.12E-001, 9.16E-001, 9.19E-001, 9.22E-001, 9.25E-001,
			9.28E-001, 9.31E-001, 9.34E-001, 9.37E-001, 9.39E-001, 9.42E-001,
			9.44E-001, 9.46E-001, 9.49E-001, 9.51E-001, 9.53E-001, 9.55E-001,
			9.56E-001, 9.58E-001, 9.60E-001, 9.62E-001, 9.63E-001, 9.65E-001,
			9.66E-001, 9.68E-001, 9.69E-001, 9.70E-001, 9.72E-001, 9.73E-001,
			9.74E-001, 9.75E-001, 9.76E-001, 9.77E-001, 9.78E-001, 9.79E-001,
			9.80E-001, 9.81E-001, 9.81E-001, 9.82E-001, 9.83E-001, 9.84E-001,
			9.84E-001, 9.85E-001, 9.86E-001, 9.86E-001, 9.87E-001
	};
	
	
	public KLPGAVlnFn() {
		ADF = 3.11E-054;
		BDF = 9.87E-001;
		NIML = IML.length;
		supportedTypes = new ArrayList<String>();
		supportedTypes.add("org.riskagora.devel.WoodFrame");
		register(supportedTypes);
	}
	
	@Override
	public double getDF(double IML) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ArrayList<double[]> getDFTable() {
		ArrayList<double[]> rtn = new ArrayList<double[]>();
		
		for(int i = 0; i < NIML; ++i) {
			double[] tmp = {IML[i], DF[i], 0.0};
			rtn.add(tmp);
		}
		return rtn;
	}

	@Override
	public DiscretizedFunc getIMLForHazardTemplate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIMT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Double> getIntensityMeasureLevels() {
		// TODO Auto-generated method stub
		return null;
	}

}
