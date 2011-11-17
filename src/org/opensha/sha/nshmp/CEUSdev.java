package org.opensha.sha.nshmp;

import java.io.File;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.NSHMP_Utils;


/**
 * Add comments here
 * 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class CEUSdev {

	private static String datPath = "/resources/data/nshmp/sources/";
	private static Logger log;
	
	public static void main(String[] args) {
		log = NSHMP_Utils.logger();
		Level level = Level.FINE;
		log.setLevel(level);
		for (Handler h : NSHMP_Utils.logger().getHandlers()) {
			h.setLevel(level);
		}

		//log.setLevel(Level.FINE);
		// log date and class as these are suppressed by custom formatter
		log.info((new Date()) + " " + CEUSdev.class.getName());

		//FaultParser dev = new FaultParser(log);
//		String srcPath = datPath + "WUS/faults/brange.3dip.gr.in";
//		String srcPath = datPath + "WUS/faults/brange.3dip.ch.in";
		String srcPath = datPath + "WUS/faults/brange.3dip.65.in";
//		String srcPath = datPath + "CEUS/faults/CEUScm.in";

		File f = FileUtils.toFile(CEUSdev.class.getResource(srcPath));
		if (f != null) {
			log.info("Source: " + srcPath);
			//dev.parse(f);
		} else {
			log.log(Level.SEVERE, "Bad source file: " + srcPath);
		}
	}
	

	
}
