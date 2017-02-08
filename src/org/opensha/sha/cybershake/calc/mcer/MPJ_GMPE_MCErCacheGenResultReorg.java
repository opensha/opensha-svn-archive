package org.opensha.sha.cybershake.calc.mcer;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.sha.calc.hazardMap.BinaryHazardCurveReader;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.common.primitives.Doubles;

public class MPJ_GMPE_MCErCacheGenResultReorg {
	
	public static void main(String[] args) throws Exception {
		File mainDir = new File("/home/kevin/CyberShake/MCER/gmpe_cache_gen");
//		String prefix = "2016_09_30-ucerf3_downsampled_ngaw2_binary_0.02_";
		String prefix = "2017_01_20-ucerf3_downsampled_ngaw2_binary_0.02_";
		String dataFileName = "NGAWest_2014_NoIdr_MeanUCERF3_downsampled_RotD100_mcer.bin";
		File outputDir = new File(mainDir, prefix+"results");
		Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
		
		for (File dir : mainDir.listFiles()) {
			if (!dir.isDirectory())
				continue;
			if (dir.equals(outputDir))
				continue;
			String dirName = dir.getName();
			if (!dirName.startsWith(prefix))
				continue;
			File dataFile = new File(dir, dataFileName);
			Preconditions.checkState(dataFile.exists(), "Data file doesn't exist: %s", dataFile.getAbsolutePath());
			
			System.out.println("Loading "+dirName);
			BinaryHazardCurveReader reader = new BinaryHazardCurveReader(dataFile.getAbsolutePath());
			Map<Location, ArbitrarilyDiscretizedFunc> map = reader.getCurveMap();
			System.out.println("Loaded "+map.size());
			for (Location loc : map.keySet()) {
				ArbitrarilyDiscretizedFunc mcer = map.get(loc);
				Preconditions.checkNotNull(mcer);
				for (Point2D pt : mcer)
					Preconditions.checkState(Doubles.isFinite(pt.getY()));
			}
			System.out.println("All values validated\n");
			
			String identifier = dirName.substring(prefix.length());
			Preconditions.checkState(!identifier.isEmpty());
			Files.copy(dataFile, new File(outputDir, identifier+".bin"));
		}
	}

}
