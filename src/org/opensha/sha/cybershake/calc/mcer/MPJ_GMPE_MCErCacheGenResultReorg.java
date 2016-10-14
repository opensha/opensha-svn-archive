package org.opensha.sha.cybershake.calc.mcer;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

public class MPJ_GMPE_MCErCacheGenResultReorg {
	
	public static void main(String[] args) throws IOException {
		File mainDir = new File("/home/kevin/CyberShake/MCER/gmpe_cache_gen");
		String prefix = "2016_09_30-ucerf3_downsampled_ngaw2_binary_0.02_";
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
			
			String identifier = dirName.substring(prefix.length());
			Preconditions.checkState(!identifier.isEmpty());
			Files.copy(dataFile, new File(outputDir, identifier+".bin"));
		}
	}

}
