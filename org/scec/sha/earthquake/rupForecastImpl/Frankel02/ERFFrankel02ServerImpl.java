/*
 * Created on Apr 1, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.scec.sha.earthquake.rupForecastImpl.Frankel02;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.scec.data.Location;
import org.scec.data.TimeSpan;
import org.scec.data.region.GeographicRegion;
import org.scec.param.ParameterAPI;
import org.scec.param.ParameterList;
import org.scec.sha.earthquake.ProbEqkRupture;
import org.scec.sha.earthquake.ProbEqkSource;

/**
 * @author cmeutils
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ERFFrankel02ServerImpl
	extends UnicastRemoteObject
	implements ERFFrankel02Server {

   Frankel02_AdjustableEqkRupForecast forecast = null;
   
   public ERFFrankel02ServerImpl() throws IOException {
	 forecast = new Frankel02_AdjustableEqkRupForecast();
	 System.out.println("On ERFServer: " + forecast.getTimeSpan());
   }

	
	/* (non-Javadoc)
	 * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#updateForecast()
	 */
	public void updateForecast(ParameterList list, TimeSpan timeSpan) throws RemoteException {
		Iterator it = list.getParametersIterator();
		while(it.hasNext()) {
		  ParameterAPI param = (ParameterAPI) it.next();
		  forecast.getParameter(param.getName()).setValue(param.getValue());	
		}
		forecast.setTimeSpan(timeSpan);
		forecast.updateForecast();
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getName()
	 */
	public String getName() throws RemoteException {
		return forecast.getName();
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#setTimeSpan(org.scec.data.TimeSpan)
	 */
	public void setTimeSpan(TimeSpan time) throws RemoteException {
	    forecast.setTimeSpan(time);
	}

	/* (non-Javadoc)
	 * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getTimeSpan()
	 */
	public TimeSpan getTimeSpan() throws RemoteException {
		TimeSpan timeSpan = forecast.getTimeSpan();
		System.out.println("ERFFrankel02ServerImpl getTimeSpan() called. Timespan="+timeSpan);
		// TODO Auto-generated method stub
		return timeSpan;
	}

	/* (non-Javadoc)
	 * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getAdjustableParamsIterator()
	 */
	public ListIterator getAdjustableParamsIterator() throws RemoteException {
		// TODO Auto-generated method stub
		return forecast.getAdjustableParamsIterator();
	}

	/* (non-Javadoc)
	 * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#isLocWithinApplicableRegion(org.scec.data.Location)
	 */
	public boolean isLocWithinApplicableRegion(Location loc)
		throws RemoteException {
		// TODO Auto-generated method stub
		return forecast.isLocWithinApplicableRegion(loc);
	}

	/* (non-Javadoc)
	 * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getApplicableRegion()
	 */
	public GeographicRegion getApplicableRegion() throws RemoteException {
		// TODO Auto-generated method stub
		return forecast.getApplicableRegion();
	}

	/* (non-Javadoc)
	 * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getAdjustableParameterList()
	 */
	public ParameterList getAdjustableParameterList() throws RemoteException {
		// TODO Auto-generated method stub
		return forecast.getAdjustableParameterList();
	}

	/* (non-Javadoc)
	 * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getNumSources()
	 */
	public int getNumSources() throws RemoteException {
		// TODO Auto-generated method stub
		return forecast.getNumSources();
	}

	/* (non-Javadoc)
	 * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getSourceList()
	 */
	public ArrayList getSourceList() throws RemoteException {
		// TODO Auto-generated method stub
		return forecast.getSourceList();
	}

	/* (non-Javadoc)
	 * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getSource(int)
	 */
	public ProbEqkSource getSource(int iSource) throws RemoteException {
		// TODO Auto-generated method stub
		return forecast.getSource(iSource);
	}

	/* (non-Javadoc)
	 * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getNumRuptures(int)
	 */
	public int getNumRuptures(int iSource) throws RemoteException {
		// TODO Auto-generated method stub
		return forecast.getNumRuptures(iSource);
	}

	/* (non-Javadoc)
	 * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getRupture(int, int)
	 */
	public ProbEqkRupture getRupture(int iSource, int nRupture)
		throws RemoteException {
		// TODO Auto-generated method stub
		return forecast.getRupture(iSource, nRupture);
	}

}
