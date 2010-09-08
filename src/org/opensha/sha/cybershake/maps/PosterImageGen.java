package org.opensha.sha.cybershake.maps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.sha.cybershake.bombay.ModProbConfig;
import org.opensha.sha.cybershake.bombay.ModProbConfigFactory;
import org.opensha.sha.cybershake.bombay.ScenarioBasedModProbConfig;
import org.opensha.sha.cybershake.maps.InterpDiffMap.InterpDiffMapType;

public class PosterImageGen {

	private static void downloadURL(String addr, File outFile) throws IOException {
		System.out.println("Downloading " + addr + " to " + outFile.getAbsolutePath());
		URL u = new URL(addr);

		InputStream in = u.openStream();         // throws an IOException

		FileOutputStream out = new FileOutputStream(outFile);

		byte[] buf = new byte[4 * 1024]; // 4K buffer
		int bytesRead;
		while ((bytesRead = in.read(buf)) > 0) {
			out.write(buf, 0, bytesRead);
		}
		
		in.close();
		out.close();
		System.out.println("DONE");
	}

	private static void saveCurves(String webAddr, String mainDir, String name, boolean base) throws IOException {
		if (!webAddr.endsWith("/"))
			webAddr += "/";
		if (base)
			webAddr += "basemap";
		else
			webAddr += "interpolated";
		webAddr += ".300.png";
		
		File outFile = new File(mainDir + File.separator + name + ".png");
		
		downloadURL(webAddr, outFile);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String mainDir = "/home/kevin/CyberShake/oef/images";
		new File(mainDir).mkdirs();
		
		boolean logPlot = true;
		int imTypeID = 21;
		boolean isProbAt_IML = true;
		double val = 0.2;
		
		String baseMapName = "cb2008";
		
		Double normCustomMin = -8.259081006598409;
		Double normCustomMax = -2.5;
		
		String normLabel = "POE "+(float)val+"G 3sec SA in 1 day";
		String gainLabel = "Probability Gain";
		
		InterpDiffMapType[] types = { InterpDiffMapType.INTERP_NOMARKS };
		HardCodedInterpDiffMapCreator.gainPlotTypes = types;
		HardCodedInterpDiffMapCreator.normPlotTypes = types;

		try {
			for (ModProbConfig config : ModProbConfigFactory.modProbConfigs.values()) {
				String name = config.getName().replaceAll(" ", "");
				String normAddr = 
					HardCodedInterpDiffMapCreator.getMap(logPlot, imTypeID, normCustomMin, normCustomMax,
							isProbAt_IML, val, baseMapName, config, false, normLabel);
				saveCurves(normAddr, mainDir, name, false);
				if (config instanceof ScenarioBasedModProbConfig) {
					String gainAddr = 
						HardCodedInterpDiffMapCreator.getMap(logPlot, imTypeID, null, null,
								isProbAt_IML, val, baseMapName, config, true, gainLabel);
					saveCurves(gainAddr, mainDir, name+"_gain", false);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(2);
		}
		System.exit(0);
	}

}
