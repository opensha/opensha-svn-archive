package scratch.kevin.ucerf3.etas;

import java.io.File;
import java.io.IOException;

import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.util.ClassUtils;

import scratch.UCERF3.erf.ETAS.ETAS_CatalogIO;
import scratch.UCERF3.erf.ETAS.ETAS_CatalogIO.BinarayCatalogsIterable;
import scratch.UCERF3.erf.ETAS.ETAS_MultiSimAnalysisTools;

public class ETAS_GriddedNuclPlotIterable {

	public static void main(String[] args) throws IOException, GMT_MapException {
		if (args.length != 5) {
			System.err.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(ETAS_GriddedNuclPlotIterable.class)
					+" <bin-file> <output-dir> <duration> <name> <prefix>");
			System.exit(2);
		}
		File catFile = new File(args[0]);
		File outputDir = new File(args[1]);
		
		BinarayCatalogsIterable catalogs = ETAS_CatalogIO.getBinaryCatalogsIterable(catFile, 0d);
		int numCatalogs = catalogs.getNumCatalogs();
		
		double duration = Double.parseDouble(args[2]);
		String name = args[3];
		String prefix = args[4];
		
		double[] mags = { 2.5 };
		
		ETAS_MultiSimAnalysisTools.plotCubeNucleationRates(catalogs, numCatalogs,
				duration, outputDir, name, prefix, mags, true);
	}

}
