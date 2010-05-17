package org.opensha.sha.gui.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;

import org.opensha.commons.param.DependentParameterAPI;
import org.opensha.commons.param.DoubleDiscreteConstraint;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.StringParameter;
import org.opensha.commons.param.editor.ParameterListEditor;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.sha.gui.beans.event.IMTChangeEvent;
import org.opensha.sha.gui.beans.event.IMTChangeListener;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.event.ScalarIMRChangeEvent;
import org.opensha.sha.imr.event.ScalarIMRChangeListener;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.util.TectonicRegionType;

public class IMT_NewGuiBean extends ParameterListEditor
implements ParameterChangeListener, ScalarIMRChangeListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static double default_period = 1.0;
	
	public final static String IMT_PARAM_NAME =  "IMT";
	
	public final static String TITLE =  "Set IMT";
	
	private boolean commonParamsOnly = false;
	
	private ParameterList imtParams;
	
	private StringParameter imtParameter;
	
	private ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs;
	
	private ArrayList<IMTChangeListener> listeners = new ArrayList<IMTChangeListener>();
	
	private ArrayList<Double> allPeriods;
	private ArrayList<Double> currentSupportedPeriods;

	public IMT_NewGuiBean(ScalarIntensityMeasureRelationshipAPI imr) {
		this(wrapInList(imr));
	}
	
	public IMT_NewGuiBean(IMR_MultiGuiBean imrGuiBean) {
		this(imrGuiBean.getIMRs());
		this.addIMTChangeListener(imrGuiBean);
		imrGuiBean.addIMRChangeListener(this);
	}
	
	public IMT_NewGuiBean(ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs) {
		this.setTitle(TITLE);
		setIMRs(imrs);
	}
	
	private static ArrayList<ScalarIntensityMeasureRelationshipAPI> wrapInList(
			ScalarIntensityMeasureRelationshipAPI imr) {
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs =
			new ArrayList<ScalarIntensityMeasureRelationshipAPI>();
		imrs.add(imr);
		return imrs;
	}
	
	public void setIMR(ScalarIntensityMeasureRelationshipAPI imr) {
		this.setIMRs(wrapInList(imr));
	}
	
	public void setIMRs(ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs) {
		this.imrs = imrs;
		
		// first get a master list of all of the supported Params
		// this is hardcoded to allow for checking of common SA period
		ArrayList<Double> saPeriods;
		ParameterList paramList = new ParameterList();
		for (ScalarIntensityMeasureRelationshipAPI imr : imrs) {
			for (ParameterAPI<?> param : imr.getSupportedIntensityMeasuresList()) {
				if (paramList.containsParameter(param.getName())) {
					// it's already in there, do nothing
				} else {
					paramList.addParameter(param);
				}
			}
		}
		
		SA_Param oldSAParam = null;
		if (commonParamsOnly) {
			// now we weed out the ones that aren't supported by everyone
			ParameterList toBeRemoved = new ParameterList();
			for (ParameterAPI param : paramList) {
				boolean remove = false;
				for (ScalarIntensityMeasureRelationshipAPI imr : imrs) {
					if (!imr.getSupportedIntensityMeasuresList().containsParameter(param.getName())) {
						remove = true;
						break;
					}
				}
				if (remove) {
					if (!toBeRemoved.containsParameter(param.getName())) {
						toBeRemoved.addParameter(param);
					}
					// if SA isn't supported, we can skip the below logic
					continue;
				}
				ArrayList<Double> badPeriods = new ArrayList<Double>();
				if (param.getName().equals(SA_Param.NAME)) {
					oldSAParam = (SA_Param)param;
				}
			}
			// now we remove them
			for (ParameterAPI badParam : toBeRemoved) {
				paramList.removeParameter(badParam.getName());
			}
			saPeriods = getCommonPeriods(imrs);
		} else {
			for (ParameterAPI<?> param : paramList) {
				if (param.getName().equals(SA_Param.NAME)) {
					oldSAParam = (SA_Param) param;
					break;
				}
			}
			saPeriods = getAllSupportedPeriods(imrs);
		}
		if (oldSAParam != null && paramList.containsParameter(oldSAParam.getName())) {
			Collections.sort(saPeriods);
			allPeriods = saPeriods;
			DoubleDiscreteConstraint pConst = new DoubleDiscreteConstraint(saPeriods);
			double defaultPeriod = default_period;
			if (!pConst.isAllowed(defaultPeriod))
				defaultPeriod = saPeriods.get(0);
			PeriodParam periodParam = new PeriodParam(pConst, defaultPeriod, true);
			periodParam.addParameterChangeListener(this);
//			System.out.println("new period param with " + saPeriods.size() + " periods");
			SA_Param replaceSA = new SA_Param(periodParam, oldSAParam.getDampingParam());
			replaceSA.setValue(defaultPeriod);
			paramList.replaceParameter(replaceSA.getName(), replaceSA);
		}
		
		this.imtParams = paramList;
		
		ParameterList finalParamList = new ParameterList();
		
		ArrayList<String> imtNames = new ArrayList<String>();
		for (ParameterAPI<?> param : paramList) {
			imtNames.add(param.getName());
		}
		
		// add the IMT paramter
		imtParameter = new StringParameter (IMT_PARAM_NAME,imtNames,
				(String)imtNames.get(0));
		imtParameter.addParameterChangeListener(this);
		finalParamList.addParameter(imtParameter);
		for (ParameterAPI<?> param : paramList) {
			finalParamList.addParameter(param);
		}
		updateGUI();
	}
	
	private void updateGUI() {
		ParameterList params = new ParameterList();
		params.addParameter(imtParameter);
		
		// now add the independent params for the selected IMT
		String imtName = imtParameter.getValue();
		System.out.println("Updating GUI for: " + imtName);
		DependentParameterAPI<?> imtParam = (DependentParameterAPI<?>) imtParams.getParameter(imtName);
		ListIterator<ParameterAPI<?>> paramIt = imtParam.getIndependentParametersIterator();
		while (paramIt.hasNext()) {
			ParameterAPI<?> param = paramIt.next();
			if (param.getName().equals(PeriodParam.NAME)) {
				PeriodParam periodParam = (PeriodParam) param;
				ArrayList<Double> periods = currentSupportedPeriods;
				if (periods == null)
					periods = allPeriods;
				DoubleDiscreteConstraint pConst = new DoubleDiscreteConstraint(periods);
				periodParam.setConstraint(pConst);
				if (periodParam.getValue() == null) {
					if (periodParam.isAllowed(default_period))
						periodParam.setValue(default_period);
					else
						periodParam.setValue(periods.get(0));
				}
				periodParam.getEditor().setParameter(periodParam);
			}
			params.addParameter(param);
		}
		
		this.setParameterList(params);
		this.refreshParamEditor();
		this.revalidate();
		this.repaint();
	}
	
	public String getSelectedIMT() {
		return imtParameter.getValue();
	}
	
	public void setSelectedIMT(String imtName) {
		if (!imtName.equals(getSelectedIMT())) {
			imtParameter.setValue(imtName);
			updateGUI();
			fireIMTChangeEvent();
		}
	}
	
	@SuppressWarnings("unchecked")
	public DependentParameterAPI<Double> getSelectedIM() {
		return (DependentParameterAPI<Double>) imtParams.getParameter(getSelectedIMT());
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		String paramName = event.getParameterName();
		
		if (paramName.equals(IMT_PARAM_NAME)) {
			updateGUI();
			fireIMTChangeEvent();
		} else if (paramName.equals(PeriodParam.NAME)) {
			fireIMTChangeEvent();
		}
			
	}
	
	public void addIMTChangeListener(IMTChangeListener listener) {
		listeners.add(listener);
	}
	
	public void removeIMTChangeListener(IMTChangeListener listener) {
		listeners.remove(listener);
	}
	
	public void clearIMTChangeListeners(IMTChangeListener listener) {
		listeners.clear();
	}
	
	private void fireIMTChangeEvent() {
		IMTChangeEvent event = new IMTChangeEvent(this, getSelectedIM());
		
		for (IMTChangeListener listener : listeners) {
			listener.imtChange(event);
		}
	}
	
	public void setIMTinIMR(ScalarIntensityMeasureRelationshipAPI imr) {
		setIMTinIMR(getSelectedIM(), imr);
	}
	
	public void setIMTinIMRs(HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> imrMap) {
		setIMTinIMRs(getSelectedIM(), imrMap);
	}
	
	@SuppressWarnings("unchecked")
	public static void setIMTinIMR(
			DependentParameterAPI<Double> imt,
			ScalarIntensityMeasureRelationshipAPI imr) {
		imr.setIntensityMeasure(imt.getName());
		DependentParameterAPI<Double> newIMT = (DependentParameterAPI<Double>) imr.getIntensityMeasure();
		
		ListIterator<ParameterAPI<?>> paramIt = newIMT.getIndependentParametersIterator();
		while (paramIt.hasNext()) {
			ParameterAPI toBeSet = paramIt.next();
			ParameterAPI newVal = imt.getIndependentParameter(toBeSet.getName());
			
			toBeSet.setValue(newVal.getValue());
		}
	}
	
	public static void setIMTinIMRs(
			DependentParameterAPI<Double> imt,
			HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> imrMap) {
		for (TectonicRegionType trt : imrMap.keySet()) {
			ScalarIntensityMeasureRelationshipAPI imr = imrMap.get(trt);
			setIMTinIMR(imt, imr);
		}
	}
	
	public void setSupportedPeriods(ArrayList<Double> supportedPeriods) {
		this.currentSupportedPeriods = supportedPeriods;
		Collections.sort(currentSupportedPeriods);
		updateGUI();
	}
	
	public static ArrayList<Double> getCommonPeriods(Collection<ScalarIntensityMeasureRelationshipAPI> imrs) {
		ArrayList<Double> allPeriods = getAllSupportedPeriods(imrs);
		
		ArrayList<Double> commonPeriods = new ArrayList<Double>();
		for (Double period : allPeriods) {
			boolean include = true;
			for (ScalarIntensityMeasureRelationshipAPI imr : imrs) {
				imr.setIntensityMeasure(SA_Param.NAME);
				SA_Param saParam = (SA_Param)imr.getIntensityMeasure();
				PeriodParam periodParam = saParam.getPeriodParam();
				if (!periodParam.isAllowed(period)) {
					include = false;
					break;
				}
			}
			
			if (include)
				commonPeriods.add(period);
		}
		
		return commonPeriods;
	}
	
	public static ArrayList<Double> getAllSupportedPeriods(Collection<ScalarIntensityMeasureRelationshipAPI> imrs) {
		ArrayList<Double> periods = new ArrayList<Double>();
		for (ScalarIntensityMeasureRelationshipAPI imr : imrs) {
			if (imr.isIntensityMeasureSupported(SA_Param.NAME)) {
				imr.setIntensityMeasure(SA_Param.NAME);
				SA_Param saParam = (SA_Param)imr.getIntensityMeasure();
				PeriodParam periodParam = saParam.getPeriodParam();
				for (double period : periodParam.getAllowedDoubles()) {
					if (!periods.contains(period))
						periods.add(period);
				}
			}
		}
		return periods;
	}

	@Override
	public void imrChange(ScalarIMRChangeEvent event) {
		this.setSupportedPeriods(getCommonPeriods(event.getNewIMRs().values()));
	}

}
