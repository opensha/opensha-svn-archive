package org.opensha.metadata;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.IntensityMeasureRelationship;

public class MetadataLoader implements ParameterChangeWarningListener {

	public MetadataLoader() {
	}

	/**
	 * Creates a class instance from a string of the full class name including packages.
	 * This is how you dynamically make objects at runtime if you don't know which\
	 * class beforehand.
	 *
	 */
	public static Object createClassInstance(String className) throws InvocationTargetException{
		return createClassInstance(className, null, null);
	}
	
	/**
	 * Creates a class instance from a string of the full class name including packages.
	 * This is how you dynamically make objects at runtime if you don't know which\
	 * class beforehand.
	 *
	 */
	public static Object createClassInstance(String className, ArrayList<Object> args) throws InvocationTargetException{
		return createClassInstance(className, null, null);
	}
	
	/**
	 * Creates a class instance from a string of the full class name including packages.
	 * This is how you dynamically make objects at runtime if you don't know which\
	 * class beforehand.
	 *
	 */
	public static Object createClassInstance(String className, ArrayList<Object> args, ArrayList<String> argNames) throws InvocationTargetException{
		try {
			Object[] paramObjects;;
			Class[] params;
			if (args == null) {
				paramObjects = new Object[]{};
				params = new Class[]{};
			} else {
				paramObjects = new Object[args.size()];
				params = new Class[args.size()];
				for (int i=0; i<args.size(); i++) {
					Object obj = args.get(i);
					paramObjects[i] = obj;
					if (argNames == null) {
						params[i] = obj.getClass();
					} else {
						String name = argNames.get(i);
						params[i] = Class.forName(name);
					}
				}
			}
			
			Class newClass = Class.forName( className );
			Constructor con = newClass.getConstructor(params);
			Object obj = con.newInstance( paramObjects );
			return obj;
		} catch (InvocationTargetException e) {
			throw e;
		} catch (Exception e ) {
			throw new RuntimeException(e);
		}
	}


	public static void main(String args[]) {
		try {
			SAXReader reader = new SAXReader();
	        Document document = reader.read(new File("output.xml"));
	        IntensityMeasureRelationship imr = IntensityMeasureRelationship.fromXMLMetadata(document.getRootElement().element(IntensityMeasureRelationship.XML_METADATA_NAME), new MetadataLoader());
	        System.out.println("Name: " + imr.getName());
	        System.out.println("IMT: " + imr.getIntensityMeasure().getName());
	        System.out.println("Period: " + imr.getParameter(AttenuationRelationship.PERIOD_NAME).getValue());
	        EqkRupForecast erf = EqkRupForecast.fromXMLMetadata(document.getRootElement().element(EqkRupForecast.XML_METADATA_NAME));
	        System.out.println("Name: " + erf.getName());
	        System.out.println("Background: " + erf.getAdjustableParameterList().getParameter(UCERF2.BACK_SEIS_NAME).getValue());
	        System.out.println("TimeSpan: " + erf.getTimeSpan().getStartTimeYear() + ", " + erf.getTimeSpan().getDuration());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void parameterChangeWarning(ParameterChangeWarningEvent event) {
		// TODO Auto-generated method stub
		
	}
}
