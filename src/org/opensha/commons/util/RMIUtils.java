package org.opensha.commons.util;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIUtils {
	
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

}
