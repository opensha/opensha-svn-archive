package scratchJavaDevelopers.kevin;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.opensha.util.FileUtils;

public class RasterExtractor {
	
	String psFileName;
	String outFileName;
	boolean doTrans = true;
	
	byte transR = 0;
	byte transG = 0;
	byte transB = 0;
	
	public RasterExtractor(String psFileName, String outFileName) {
		this.psFileName = psFileName;
		this.outFileName = outFileName;
	}
	
	public BufferedImage getRasterImage() throws FileNotFoundException, IOException {
		
		ArrayList<String> lines = FileUtils.loadFile(psFileName);
		
		boolean reading = false;
		
		int width = 0;
		int height = 0;
		int pixels = 0;
		int expected = 0;
		byte[] bytes = null;
		int byteCount = 0;
		
		for (String line : lines) {
			if (line.contains("false 3 colorimage")) {
				reading = true;
				StringTokenizer tok = new StringTokenizer(line);
				
				width = Integer.parseInt(tok.nextToken());
				height = Integer.parseInt(tok.nextToken());
				int depth = Integer.parseInt(tok.nextToken());
				if (depth != 8) {
					System.out.println("BAD DEPTH! EXITING!");
					return null;
				}
				pixels = width * height;
				expected = pixels * 3; // pixels * 1 byte for R, G, and B
				bytes = new byte[expected];
				
				System.out.println("time to READ! " + width + " " + height + " " + pixels);
				continue;
			}
			if (reading && bytes != null) { //we're in the middle of the string
				if (line.startsWith("U"))
					break;
				
				for (int i=0; i<line.length(); i+=2) {
					bytes[byteCount] = (byte) Integer.parseInt(line.substring(i, i+2), 16);
					
					byteCount++;
				}
			}
		}
		
		System.out.println("Read in " + byteCount + " bytes...expected: " + expected);
		
		return this.getBufferedImage(bytes, width, height);
	}
	
	
	public BufferedImage getBufferedImage(byte[] bytes, int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		int i=0;
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
//				int rgb = (int)bytes[i++]&0xffffff;
				
				byte b = bytes[i++];
				byte g = bytes[i++];
				byte r = bytes[i++];
				
				
				byte a = (byte) 255;
				if (doTrans && r == transR && b == transB && g == transG)
					a = (byte)0;
				
//				int rgb = ((int)r&0xFF)  // R
//                | ((int)g&0xFF) << 8   // G
//                | ((int)b&0xFF) << 16  // B
//                | 0xFF000000;
				
				int argb = ((int)r&0xFF)  // R
				| ((int)g&0xFF) << 8   // G
                | ((int)b&0xFF) << 16  // B
                | ((int)a&0xFF) << 24  // A
                | 0x00000000;
				
				image.setRGB(x, y, argb);
//				int rInt = r ;
//				System.out.println("0x " +r + " " + g + " " + b);
			}
		}
		
//		for (int i=0; i<bytes.length; i+=3) {
//			
//		}
		
		return image;
	}
	
	public void writePNG() throws FileNotFoundException, IOException {
		ImageIO.write(this.getRasterImage(), "png", new File(outFileName));
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String psFileName = "/home/kevin/OpenSHA/maps/ucerf_big/poe.ps";
		
		RasterExtractor extract = new RasterExtractor(psFileName, "/home/kevin/OpenSHA/maps/ucerf_big/poe_extract.png");
		
		try {
			extract.writePNG();
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
