package org.opensha.sha.simulators.writers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.opensha.sha.simulators.RectangularElement;

public class WardFileWriter {
	
	public static void writeToWardFile(File outputFile, List<RectangularElement> elems) throws IOException {
		FileWriter efw = new FileWriter(outputFile);
		for (RectangularElement rectElem : elems) {
			efw.write(rectElem.toWardFormatLine() + "\n");
		}
		efw.close();
	}

}
