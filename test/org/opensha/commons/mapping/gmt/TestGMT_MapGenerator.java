package org.opensha.commons.mapping.gmt;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensha.commons.data.ArbDiscretizedXYZ_DataSet;
import org.opensha.commons.exceptions.GMT_MapException;

public class TestGMT_MapGenerator {
	
	private static GMT_MapGenerator gmt;
	private static ArbDiscretizedXYZ_DataSet xyz;
	
	@BeforeClass
	public static void setUp() throws Exception {
		gmt = new GMT_MapGenerator();
		xyz = new ArbDiscretizedXYZ_DataSet();
		int y = 0;
		for (double lat=33.5; lat<=35; lat+=0.1) {
			int x = 0;
			for (double lon=-119; lon<=-117; lon+=0.1) {
				xyz.addValue(lat, lon, x + y);
//				xyz.addValue(lon, lat, x + y);
				x++;
			}
			y++;
		}
		System.out.println("X Range: " + xyz.getMinX() + " => " + xyz.getMaxX());
		System.out.println("Y Range: " + xyz.getMinY() + " => " + xyz.getMaxY());
		System.out.println("Z Range: " + xyz.getMinZ() + " => " + xyz.getMaxZ());
	}

	@Test
	public void testGetGMTMapSpecification() {
		GMT_Map map = gmt.getGMTMapSpecification(xyz);
		assertNotNull(map);
	}

	@Test
	public void testMakeMapUsingServletGMT_MapStringString() {
		GMT_Map map = gmt.getGMTMapSpecification(xyz);
		map.setCustomScaleMin(null);
		map.setCustomScaleMax(null);
		String metadata = "My Map Metadata";
		String addr = null;
		try {
			addr = gmt.makeMapUsingServlet(map, metadata, "jUnitTest_" + System.currentTimeMillis());
		} catch (GMT_MapException e) {
			e.printStackTrace();
			fail("GMT_MapException: " + e.getMessage());
		} catch (RuntimeException e) {
			e.printStackTrace();
			fail("RuntimeException: " + e.getMessage());
		}
		assertNotNull("Image address should not be null", addr);
		assertTrue("Image address should not be of length 0", addr.length() > 0);
		assertTrue("Image address should start with 'http://'", addr.startsWith("http://"));
		URL url = null;
		try {
			url =  new URL(addr);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("MalformedURLException for URL '" + addr + "', " + e.getMessage());
		}
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection)url.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException opening connection: " + e.getMessage());
		}
		assertTrue("Connection to fetch image should give JPEG content type",
				conn.getContentType().contains("jpeg"));
		BufferedImage image = null;
		try {
			image =  ImageIO.read(conn.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException reading connection as image: " + e.getMessage());
		}
		int width = image.getWidth();
		int height = image.getHeight();
		
		System.out.println("Image size: " + width + "x" + height);
		
		assertTrue("Image width should be at least 100 pixels", width >= 100);
		assertTrue("Image hgithg should be at least 100 pixels", height >= 100);
		
		// the image should also not be simply blank, so lets make sure there are 100 unique colors
		ArrayList<Integer> colors = new ArrayList<Integer>();
		for (int x=0; x<width; x++) {
			for (int y=0; y<height; y++) {
				Integer argb = image.getRGB(x, y);
				if (!colors.contains(argb))
					colors.add(argb);
			}
		}
		System.out.println("Out of " + (width * height) + " pixels, " +
				"there are " + colors.size() + " unique colors.");
		int minUnique = 10000;
//		int minUnique = 30000; // temp test to fail
		assertTrue("there should be at least " + minUnique + " unique colors", colors.size() > minUnique);
	}

}
