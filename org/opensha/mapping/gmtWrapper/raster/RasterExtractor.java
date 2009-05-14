package org.opensha.mapping.gmtWrapper.raster;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
		boolean colorimage = false;
		
		boolean ascii85 = false;
		
		int width = 0;
		int height = 0;
		int pixels = 0;
		int expected = 0;
		byte[] bytes = null;
		int byteCount = 0;
		
		int curLine = 0;
		
		String asciiImage = "";
		
		for (String line : lines) {
			if (!reading) {
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
					colorimage = true;
					continue;
				} else if (line.contains("ASCII85Decode")) {
					
					int depth = 0;
					
					for (int i=curLine - 2; i<curLine+3; i++) {
						String parseLine = lines.get(i);
						StringTokenizer parseTok = new StringTokenizer(parseLine);
						while (parseTok.hasMoreTokens()) {
							String token = parseTok.nextToken();
							if (token.contains("/Width"))
								width = Integer.parseInt(parseTok.nextToken());
							else if (token.contains("/Height"))
								height = Integer.parseInt(parseTok.nextToken());
							else if (token.contains("/BitsPerComponent"))
								depth = Integer.parseInt(parseTok.nextToken());
						}
						if (width > 0 && height > 0 && depth > 0)
							break;
					}
					
					System.out.println(width + " " + height + " " + depth);
					
					if (width <= 0 || height <= 0 || depth != 8)
						return null;
					
					pixels = width * height;
					expected = pixels * 3; // pixels * 1 byte for R, G, and B
					bytes = new byte[expected];
					
					System.out.println("time to READ! " + width + " " + height + " " + pixels);
					
					reading = true;
					ascii85 = true;
					continue;
				}
			}
			if (reading && bytes != null) { //we're in the middle of the string
				if (ascii85) {
					if (line.startsWith(">> image"))
						continue;
					
					asciiImage += line + "\n";
					
					if (line.contains("~>")) {
//						System.out.println(line);
						break;
					}
				} else if (colorimage) {
					if (line.startsWith("U"))
						break;
					for (int i=0; i<line.length(); i+=2) {
						bytes[byteCount] = (byte) Integer.parseInt(line.substring(i, i+2), 16);
						
						byteCount++;
					}
				}
			}
			
			curLine++;
		}
		
		if (ascii85) {
			InputStream is = new ByteArrayInputStream(asciiImage.getBytes("UTF-8"));
			ASCII85InputStream ais = new ASCII85InputStream(is);
			
//			System.out.println(asciiImage);
			
			while (!ais.isEndReached()) {
				if (byteCount < expected) {
					bytes[byteCount] = (byte) ais.read();
					byteCount++;
				} else
					break;
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
		String psFileName =  "/home/kevin/OpenSHA/basin/plots/temp/basin.ps";
		String pngFileName = "/home/kevin/OpenSHA/basin/plots/temp/extract.png";
		
		RasterExtractor extract = new RasterExtractor(psFileName, pngFileName);
		
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
