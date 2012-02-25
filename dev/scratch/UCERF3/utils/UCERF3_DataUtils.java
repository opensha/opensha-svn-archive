package scratch.UCERF3.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import com.google.common.base.Preconditions;

public class UCERF3_DataUtils {
	
	/**
	 * This is the location for scratch data that is computed locally and will never
	 * be committed to the repository
	 */
	public static File DEFAULT_SCRATCH_DATA_DIR = new File("dev/scrach/UCERF3/data/scratch");
	
	/**
	 * This is the URL prefix for loading file from the persistant data directory
	 */
	public static String DATA_URL_PREFIX = "/scratch/UCERF3/data";
	
	/**
	 * This gives the URL of a file in our UCERF3 data directory.
	 * 
	 * @param fileName
	 * @return
	 */
	public static URL locateResource(String fileName) {
		return locateResource(null, fileName);
	}
	
	/**
	 * This gives the URL of a file in the specified sub directory if our UCERF3 data directory.
	 * 
	 * @param dataSubDirName
	 * @param fileName
	 * @return
	 */
	public static URL locateResource(String dataSubDirName, String fileName) {
		String relativePath = getRelativePath(dataSubDirName, fileName);
		URL url = UCERF3_DataUtils.class.getResource(relativePath);
		Preconditions.checkNotNull(url, "Resource '"+fileName+"' could not be located: "+relativePath);
		return url;
	}
	
	private static String getRelativePath(String dataSubDirName, String fileName) {
		String relativePath = DATA_URL_PREFIX;
		if (dataSubDirName != null && !dataSubDirName.isEmpty())
			relativePath += "/"+dataSubDirName;
		relativePath += "/"+fileName;
		return relativePath;
	}
	
	/**
	 * This loads the given file as a stream from our data directory.
	 * 
	 * @param fileName
	 * @return
	 */
	public static InputStream locateResourceAsStream(String fileName) {
		return locateResourceAsStream(null, fileName);
	}
	
	/**
	 * This loads the given file in the given sub directory as a stream from our data directory
	 * 
	 * @param dataSubDirName
	 * @param fileName
	 * @return
	 */
	public static InputStream locateResourceAsStream(String dataSubDirName, String fileName) {
		String relativePath = getRelativePath(dataSubDirName, fileName);
		InputStream stream = UCERF3_DataUtils.class.getResourceAsStream(relativePath);
		Preconditions.checkNotNull(stream, "Resource '"+fileName+"' could not be located: "+relativePath);
		return stream;
	}
	
	/**
	 * This creates an input stream reader for the given input stream. Note that this
	 * is NOT buffered, so you should buffer it yourself if needed by wrapping in a 
	 * buffered stream reader.
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static Reader getReader(InputStream is) throws IOException {
		return new InputStreamReader(is);
	}
	
	/**
	 * This creates an input stream reader for the given URL. Note that this
	 * is NOT buffered, so you should buffer it yourself if needed by wrapping in a 
	 * buffered stream reader.
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static Reader getReader(URL url) throws IOException {
		URLConnection uc = url.openConnection();
		return new InputStreamReader((InputStream) uc.getContent());
	}

}
