package org.opensha.commons.eq.cat.io;

import static org.junit.Assert.*;
import static org.opensha.commons.eq.cat.util.DataType.*;
import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensha.commons.eq.cat.Catalog;
import org.opensha.commons.eq.cat.DefaultCatalog;

public class ReaderTests {

	private static File ANSS_DAT = loadFile("ANSS.cat");
	
	private static File loadFile(String name) {
		try {
			return new File(ReaderTests.class.getResource(
				"cats/"+name).toURI());
		} catch (Exception e) { return null; }
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {}

	@Test
	public void testReader_ANSS() {
		try {
			Catalog c = new DefaultCatalog(ANSS_DAT, new Reader_ANSS(20));
			assertEquals(c.getValue(LATITUDE, 3), 37.4525, 0.00001);
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public static void main(String[] args) {
		try {
			Catalog c = new DefaultCatalog(ANSS_DAT, new Reader_ANSS(20));
			//assertEquals(c.getValue(LATITUDE, 3), 37.4525, 0.00001);
		} catch (IOException e) { e.printStackTrace(); }
	}

}
