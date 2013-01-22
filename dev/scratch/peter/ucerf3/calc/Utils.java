package scratch.peter.ucerf3.calc;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;

import cern.colt.Arrays;

import com.google.common.io.Files;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class Utils {

	private static final String CONSOL_DIR = "consolidated";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String dirName = "/Users/pmpowers/Documents/OpenSHA/RTGM/deaggTmp/TMP/UC3FM3P1/EUREKA";
		String fileName = "DisaggregationPlot.png";
		consolidateFiles(fileName, dirName);
	}
	
	
	
	public static void consolidateFiles(
			String fileName,
			String dirName) {
		
		try {
			File dirIn = new File(dirName);
			File dirOut = new File(dirIn, "consolidated");
			dirOut.mkdir();
			File[] dirList = dirIn.listFiles();
			for (File d : dirList) {
				if (!d.isDirectory()) continue;
				if (d.getName().equals(CONSOL_DIR)) continue;
				String idx = substringAfterLast(d.getName(), "-");
				File plotIn = new File(d, fileName);
				String plotOutName = substringBeforeLast(fileName, ".") + "-" +
					idx + "." + substringAfterLast(fileName, ".");
				File plotOut = new File(dirOut, plotOutName);
				Files.copy(plotIn, plotOut);
			}			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
