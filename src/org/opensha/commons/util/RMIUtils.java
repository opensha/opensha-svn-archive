package org.opensha.commons.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.util.Random;

public class RMIUtils {
	
	public static void initSocketFactory() throws IOException {
		initSocketFactory(ServerPrefUtils.SERVER_PREFS);
	}
	
	public static void initSocketFactory(ServerPrefs prefs) throws IOException {
		RMISocketFactory.setSocketFactory(new FixedPortRMISocketFactory(prefs));
	}

	public static Registry getRegistry() throws RemoteException {
		return getRegistry(ServerPrefUtils.SERVER_PREFS);
	}

	public static Registry getRegistry(ServerPrefs prefs) throws RemoteException {
		int port = prefs.getRMIPort();
		String host = prefs.getHostName();
		return LocateRegistry.getRegistry(host, port);
	}

	public static Registry getCreateRegistry() throws RemoteException {
		return getCreateRegistry(ServerPrefUtils.SERVER_PREFS);
	}

	public static Registry getCreateRegistry(ServerPrefs prefs) throws RemoteException {
		int port = prefs.getRMIPort();
		Registry registry = null;
		try {
			registry = LocateRegistry.getRegistry(port);
			registry.list(); // make sure this is real
		} catch (Exception e) {
			//			e.printStackTrace();
			// if we're here, then we need to create a registry
			System.out.println("RMIUtils: creating registry on port "+port); 
			registry = LocateRegistry.createRegistry(port);
		}
		return registry;
	}

	public static class FixedPortRMISocketFactory extends RMISocketFactory {
		
		private ServerPrefs prefs;
		private Random r = new Random();
		
		public FixedPortRMISocketFactory(ServerPrefs prefs) {
			this.prefs = prefs;
		}
		
		private int getRandomPort() {
			int min = prefs.getMinRMISocketPort();
			int max = prefs.getMaxRMISocketPort();
			int delta = max - min;
			return min + r.nextInt(delta);
		}

		@Override
		public ServerSocket createServerSocket(int port) throws IOException {
			port = (port == 0 ? getRandomPort() : port);
			System.out.println("creating ServerSocket on port " + port);
			return new ServerSocket(port);
		}

		@Override
		public Socket createSocket(String host, int port) throws IOException {
			System.out.println("creating socket to host : " + host + " on port " + port);
			return new Socket(host, port);
		}

	}

}
