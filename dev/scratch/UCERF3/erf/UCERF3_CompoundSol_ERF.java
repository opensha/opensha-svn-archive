package scratch.UCERF3.erf;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.FaultSystemSolutionFetcher;
import scratch.UCERF3.erf.mean.MeanUCERF3;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.LogicTreeBranchNode;

import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.util.ClassUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class UCERF3_CompoundSol_ERF extends UCERF3_FaultSysSol_ERF {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final boolean D = true;
	
	public static final String NAME = "UCERF3 Single Branch ERF";
	
	private Map<Class<? extends LogicTreeBranchNode<?>>, EnumParameter<?>> enumParamsMap;
	
	private FaultSystemSolutionFetcher fetch;
	
	private static final String COMPOUND_FILE_NAME = "full_ucerf3_compound_sol.zip";
	
	private static FaultSystemSolutionFetcher loadFetcher() throws ZipException, IOException {
		File storeDir = MeanUCERF3.getStoreDir();
		
		File compoundFile = new File(storeDir, COMPOUND_FILE_NAME);
		
		// allow errors so that app doesn't crash if can't download
		MeanUCERF3.checkDownload(compoundFile, true);
		
		if (!compoundFile.exists())
			return null;
		
		return CompoundFaultSystemSolution.fromZipFile(compoundFile);
	}
	
	public UCERF3_CompoundSol_ERF() throws ZipException, IOException {
		this(loadFetcher(), null);
	}
	
	public UCERF3_CompoundSol_ERF(FaultSystemSolutionFetcher fetch, LogicTreeBranch initial) {
		this.fetch = fetch;
		
		enumParamsMap = Maps.newHashMap();
		
		Preconditions.checkState(initial == null || initial.isFullySpecified(),
				"Initial branch must be null or fully specified");
		
		if (fetch != null && !fetch.getBranches().isEmpty()) {
			// build enum paramters, allow every option in the fetcher
			// note that not-present combinations may still be possible
			Collection<LogicTreeBranch> branches = fetch.getBranches();
			List<Class<? extends LogicTreeBranchNode<?>>> logicTreeNodeClasses = LogicTreeBranch.getLogicTreeNodeClasses();
			for (int i=0; i < logicTreeNodeClasses.size(); i++) {
				Class<? extends LogicTreeBranchNode<?>> clazz = logicTreeNodeClasses.get(i);
				EnumParameter<?> param = buildParam(clazz, branches, initial);
				param.addParameterChangeListener(this);
				adjustableParams.addParameter(i, param);
				enumParamsMap.put(clazz, param);
			}
		}
		
		adjustableParams.removeParameter(fileParam);
	}
	
	private static <E extends Enum<E>> EnumParameter<E> buildParam(
			Class<? extends LogicTreeBranchNode<?>> clazz, Collection<LogicTreeBranch> branches,
			LogicTreeBranch initial) {
		HashSet<E> set = new HashSet<E>();
		
		E defaultValue;
		if (initial != null)
			defaultValue = (E) initial.getValueUnchecked(clazz);
		else
			defaultValue = null;
		
		String name = null;
		
		for (LogicTreeBranch branch : branches) {
			Preconditions.checkState(branch.isFullySpecified());
			LogicTreeBranchNode<?> val = branch.getValueUnchecked(clazz);
			Preconditions.checkNotNull(val);
			set.add((E)val);
			if (defaultValue == null)
				defaultValue = (E)val;
			if (name == null)
				name = val.getBranchLevelName();
		}
		
		EnumSet<E> choices = EnumSet.copyOf(set);
		
		return new EnumParameter<E>(name, choices, defaultValue, null);
	}
	
	@Override
	public void updateForecast() {
		if (D) System.out.println("updateForecast called");
		if (getSolution() == null) {
			// this means that we have to load the solution (parameter change or never loaded)
			fetchSolution();
		}
		super.updateForecast();
	}
	
	private void fetchSolution() {
		if (fetch == null)
			return;
		
		List<LogicTreeBranchNode<?>> vals = Lists.newArrayList();
		for (EnumParameter<?> param : enumParamsMap.values()) {
			vals.add((LogicTreeBranchNode<?>) param.getValue());
		}
		LogicTreeBranch branch = LogicTreeBranch.fromValues(vals);
		Preconditions.checkState(branch.isFullySpecified(), "Somehow branch from enums isn't fully specified");
		
		FaultSystemSolution sol = fetch.getSolution(branch);
		setSolution(sol);
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		// check if it's one of our enum params
		if (enumParamsMap != null && enumParamsMap.values().contains(event.getParameter())) {
			setSolution(null);
		} else {
			super.parameterChange(event);
		}
	}

}
