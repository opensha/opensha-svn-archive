package org.opensha.commons.util;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIUtils {
	
	public static Registry getCreateRegistry() throws RemoteException {
		return getCreateRegistry(ServerPrefUtils.SERVER_PREFS.getRMIPort());
	}
	
	protected static Registry getCreateRegistry(int port) throws RemoteException {
		Registry registry = null;
		try {
			registry = LocateRegistry.getRegistry(port);
			registry.list(); // make sure this is real
		} catch (Exception e) {
			e.printStackTrace();
			// if we're here, then we need to create a registry
			System.out.println("RMIUtils: creating registry on port "+port); 
			registry = LocateRegistry.createRegistry(port);
		}
		return registry;
	}

}
