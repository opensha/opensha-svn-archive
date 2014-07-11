package scratch.kevin.ucerf3.etas;

import java.io.File;
import java.io.IOException;

import scratch.UCERF3.erf.ETAS.ETAS_SimAnalysisTools;

public class ETAS_CatalogVerify {
	
	private static int verify(File dir) {
		int good = 0;
		int bad = 0;
		for (File file : dir.listFiles()) {
			if (file.isDirectory())
				good += verify(file);
			if (file.getName().equals("simulatedEvents.txt")) {
				try {
					if (MPJ_ETAS_Simulator.isAlreadyDone(dir))
						good++;
					else
						bad++;
				} catch (IOException e1) {
					bad++;
				}
//				try {
//					ETAS_SimAnalysisTools.loadCatalog(file);
//					good++;
//				} catch (Exception e) {
//					bad++;
//				}
			}
		}
		int tot = good+bad;
		if (tot > 1)
			System.out.println(good+"/"+tot+" succeeded "+dir.getName());
		return good;
	}

	public static void main(String[] args) {
		verify(new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2014_interns"));
	}

}
