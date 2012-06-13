package scratch.UCERF3.enumTreeBranches;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Shaw_2009_MagAreaRel;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

import com.google.common.collect.Lists;

public enum MagAreaRelationships implements LogicTreeBranchNode<MagAreaRelationships> {
	
	HB_08(		"Hanks & Bakun (2002)",	"HB08",		0d,	new HanksBakun2002_MagAreaRel()),
	ELL_B(		"Ellsworth B",			"EllB", 	0d,	new Ellsworth_B_WG02_MagAreaRel()),
	SHAW_09(	"Shaw (2009)",			"Shaw09",	0d,	new Shaw_2009_MagAreaRel()),
	AVE_UCERF2(	"Average UCERF2",		"AvU2",		0d,	Lists.newArrayList(new Ellsworth_B_WG02_MagAreaRel(),
																new HanksBakun2002_MagAreaRel()));
	
	private List<MagAreaRelationship> rels;
	
	private String name, shortName;
	private double weight;
	
	private MagAreaRelationships(String name, String shortName, double weight, MagAreaRelationship rel) {
		this(name, shortName, weight, Lists.newArrayList(rel));
	}
	
	private MagAreaRelationships(String name, String shortName, double weight, List<MagAreaRelationship> rels) {
		this.rels = Collections.unmodifiableList(rels);
		this.name = name;
		this.shortName = shortName;
		this.weight = weight;
	}
	
	public String getName() {
		return name;
	}
	
	public String getShortName() {
		return shortName;
	}

	@Override
	public String toString() {
		return name;
	}
	
	public List<MagAreaRelationship> getMagAreaRelationships() {
		return rels;
	}

	
	public static void makePlot() {
		
		ArbitrarilyDiscretizedFunc sh09_func = new ArbitrarilyDiscretizedFunc();
		sh09_func.setName("SHAW_2009");
		ArbitrarilyDiscretizedFunc ellB_func = new ArbitrarilyDiscretizedFunc();
		ellB_func.setName("ELLSWORTH_B");
		ArbitrarilyDiscretizedFunc hb_func = new ArbitrarilyDiscretizedFunc();
		hb_func.setName("HANKS_BAKUN_08");
		
		MagAreaRelationship sh09 = MagAreaRelationships.SHAW_09.getMagAreaRelationships().get(0);
		MagAreaRelationship ellB = MagAreaRelationships.ELL_B.getMagAreaRelationships().get(0);
		MagAreaRelationship hb = MagAreaRelationships.HB_08.getMagAreaRelationships().get(0);
		
		// log10 area from 1 to 5
    	for(int i=50; i<=20000; i+=10) {
    		double area = (double)i;
     		sh09_func.set(area,sh09.getMedianMag(area));
    		ellB_func.set(area,ellB.getMedianMag(area));
    		hb_func.set(area,hb.getMedianMag(area));
    	}
    	
    	ArrayList<ArbitrarilyDiscretizedFunc> funcs = new ArrayList<ArbitrarilyDiscretizedFunc>();
    	funcs.add(sh09_func);
    	funcs.add(ellB_func);
    	funcs.add(hb_func);
    	
    	ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, null, 1f, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, null, 1f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, null, 1f, Color.GREEN));

    	
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Mag-Area Relationships",plotChars); 
		graph.setX_AxisLabel("Area (km-sq)");
		graph.setY_AxisLabel("Magnitude");
		graph.setXLog(true);
		graph.setX_AxisRange(50, 2e4);
		graph.setY_AxisRange(5, 9);
		graph.setPlotLabelFontSize(18);
		graph.setAxisLabelFontSize(16);
		graph.setTickLabelFontSize(14);
	}
	
	//public 
	public static void main(String[] args) throws IOException {
		makePlot();
	}

	@Override
	public double getRelativeWeight() {
		return weight;
	}

	@Override
	public String encodeChoiceString() {
		return "Ma"+getShortName();
	}
	


}
