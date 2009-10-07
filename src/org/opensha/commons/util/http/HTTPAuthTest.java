package org.opensha.commons.util.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;


public class HTTPAuthTest {
	
	public static String intensityMD5 = "2a 74 46 db b4 47 64 db f2 26 9c 95 67 2a cb 57";
	
	public HTTPAuthTest() throws IOException {
		
		InstallSSLCert installCert = new InstallSSLCert(intensityMD5, "intensity.usc.edu");
		
		File keystore = installCert.getKeyStore();
		
		if (keystore != null) {
			System.out.println("Loading keystore from: " + keystore.getAbsolutePath());
			System.setProperty("javax.net.ssl.trustStore", keystore.getAbsolutePath());
			
			Authenticator.setDefault(new HTTPAuthenticator());
			URL url = new URL("https://intensity.usc.edu/trac/opensha/wiki/");
//			URL url = new URL("https://intensity.usc.edu/");
			while (true) {
				try {
					InputStream ins = url.openConnection().getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
					String str;
					System.out.println("Authentication successful!");
					break;
				} catch (java.net.ProtocolException e) {
					System.out.println("Your password is incorrect!");
					continue;
				}
			}
			keystore.delete();
		}
	}
    
    public static void main(String args[]) {
    	try {
			new HTTPAuthTest();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.exit(0);
    }
}
