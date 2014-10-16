package msmb.runManager;

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.COPASI.CCopasiMethod;
import org.COPASI.CTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jfree.data.xy.XYSeries;

import msmb.gui.MainGui;
import msmb.utility.CellParsers;
import msmb.utility.Constants;
import msmb.utility.Constants.CompartmentsType;

public class RMPlot extends Mutant {
	private HashSet<String> simulations = null;
	static final long serialVersionUID = -8360637429062645395L;
	
	Vector<PlottedVariable> plottedVariablesY = null;
	String variableX = new String("Time");

	private Boolean showTitle = true;
	private Font fontTitle = MainGui.customFont;
	private Color titleColor = Color.BLACK;
	private Color plotBackground = Color.WHITE;
	private boolean orientationVertical = false;
	private boolean logScaleX = false;
	private boolean logScaleY = false;
	private double minY = 0;
	private double maxY = 100;
	private double maxX = 100;
	private double minX = 0;
	private boolean autoadjustX = true;
	private boolean autoadjustY = true;
	private String xaxis = "Time";
	private String labelY = "value";
	private Font labelXfont = MainGui.customFont;
	private Font labelYfont = MainGui.customFont;
	private Color labelXcolor = Color.BLACK;
	private Color labelYcolor = Color.BLACK;
	
	public RMPlot(String name) {
		super(name);
		simulations = new HashSet<String>();
		plottedVariablesY = new Vector<PlottedVariable>();
	}

	public void addSimulation(String sim) {
		simulations.add(sim);
	}
	
	public void removeSimulation(String sim) {
		simulations.remove(sim);
	}
		
	public boolean hasSimulation(String sim) {
		return simulations.contains(sim);
	}
	
	
	public Object getSimulation(String sim) {
		if(hasSimulation(sim)) {
			Vector<Object> v = new Vector<Object>(Arrays.asList(simulations.toArray()));
			int index = v.indexOf(sim);
			if(index!= -1) 	return v.get(index);
			else return null;
		}	else return null;
	}
	
	public void addPlottedVariable(PlottedVariable p) {
		plottedVariablesY.add(p);
	}
	
	public void removePlottedVariable(PlottedVariable p) {
		plottedVariablesY.remove(p);
	}
		
	public boolean hasPlottedVariable(PlottedVariable p) {
		return plottedVariablesY.contains(p);
	}
	
	public PlottedVariable getPlottedVariable(String name) {
		int index = plottedVariablesY.indexOf(new PlottedVariable(name));
		if(index!= -1) 	return plottedVariablesY.get(index);
		else return null;
	}
	


	
	public String printPlot() {
		String ret = simulations.toString();
		return ret +"\n"+ printMutant();
	}
	
	public Vector<Object> getSimulations() {
		 return (new Vector<Object>(Arrays.asList(simulations.toArray())));
	}

	public void setName(String newName) {
		name = new String(newName);
	}


	
	public void removeCumulativeChange(String k) {
		cumulativeChanges.remove(k);
	}
	
	
	
	@Override
	public boolean equals(Object obj) {
		 if (obj == null)
	            return false;
	        if (obj == this)
	            return true;
	        if (!(obj instanceof RMPlot))
	            return false;

	        RMPlot rhs = (RMPlot) obj;
	        return new EqualsBuilder().
	            append(name, rhs.name).
	            isEquals();
	}
	
	
	@Override
	public int hashCode() {
        return new HashCodeBuilder(93, 71). 
            append(name).
            toHashCode();
    }

	public void addAllSimulations(Vector<String> newList) {
		simulations.addAll(newList);
	}

	public void clearSimulations() {
		simulations.clear();
	}

	public void clearPlottedVariables() {
		plottedVariablesY.clear();
	}

	public void setShowTitle(Boolean b) {	showTitle = new Boolean(b);	}
	public void setTitleFont(Font f) {		fontTitle = f;	}
	public void setTitleColor(Color c) {		titleColor = c;	}
	public void setPlotBackground(Color c) {		plotBackground = c;	}
	public void setOrientationVertical(boolean b) {	orientationVertical = b;	}
	public void setMinY(double n) { minY = n; }
	public void setMinX(double n) {  minX = n; }
	public void setMaxY(double n) { maxY = n; }
	public void setMaxX(double n) { maxX = n; }
	public void setAutoadjustX(boolean b) {	 autoadjustX = b;	}
	public void setAutoadjustY(boolean b) {	autoadjustY = b;	}
	public void setXaxis(String l) {	 xaxis = new String(l);	}
	public void setLabelY(String l) {	 labelY = new String(l);	}
	public void setLabelXFont(Font f) {		labelXfont = f;	}
	public void setLabelYFont(Font f) {		labelYfont = f;	}
	public void setLabelXColor(Color c) {		labelXcolor = c;	}
	public void setLabelYColor(Color c) {		labelYcolor = c;	}
	public void setVariableX(String variableX) {		this.variableX = variableX;	}
	public void setLogScaleX(boolean logScaleX) {		this.logScaleX = logScaleX;	}
	public void setLogScaleY(boolean logScaleY) {		this.logScaleY = logScaleY;	}


	public Boolean getShowTitle() {	return showTitle;	}	
	public String getVariableX() {		return variableX;	}
	public boolean isLogScaleX() {		return logScaleX;	}
	public boolean isLogScaleY() {		return logScaleY;	}
	public Font getTitleFont() {		return fontTitle;	}
	public Color getTitleColor() {		return titleColor;	}
	public Color getPlotBackground() {		return plotBackground ;	}
	public boolean isOrientationVertical() {	return orientationVertical;	}
	public double getMinY() {	return minY;	}
	public double getMaxY() {	return maxY;	}
	public double getMaxX() {		return maxX;	}
	public double getMinX() {		return minX;	}
	public boolean isAutoadjustX() {		return autoadjustX;	}
	public boolean isAutoadjustY() {		return autoadjustY;	}
	public String getXaxis() {		return xaxis;	}
	public String getLabelY() {		return labelY;	}
	public Font getLabelXfont() {		return labelXfont;	}
	public Font getLabelYfont() {		return labelYfont;	}
	public Color getLabelXcolor() {		return labelXcolor;	}
	public Color getLabelYcolor() {		return labelYcolor;	}
	
	public Vector<PlottedVariable> getAllPlottedVariables() {
		return plottedVariablesY;
	}

	public Vector<String> getAllPlottedVariablesNames() {
		Iterator<PlottedVariable> it = plottedVariablesY.iterator();
		Vector<String> ret = new Vector<String>();
		while(it.hasNext()) {
			ret.add(it.next().getName());
		}
		return ret;
	}

	
	public void addChange(PlotChangeType ty, String element_new_value) {
		changes.put(generateChangeKey(ty), element_new_value);
	}
	
	public static String generateChangeKey(PlotChangeType ty) {
		return new String(ty.getDescription()+"%");
	}
	public boolean hasChange(PlotChangeType ty, String element_name) {
		return changes.containsKey(generateChangeKey(ty));
	}
	public void removeLocalChange(PlotChangeType simType) {
			changes.remove(generateChangeKey(simType));
	}
	public void removeFromBaseSet(PlotChangeType ty) {
		fromBaseSet.remove(generateChangeKey(ty));
	}

	public void setAsLocalChanges(HashMap<PlotChangeType, Object> localValues) {
		changes.clear();
		Iterator it = localValues.keySet().iterator();
		while(it.hasNext()) {
			PlotChangeType key = (PlotChangeType) it.next();
			changes.put(generateChangeKey(key), (String) localValues.get(key));
		}
	}
	
	
}

enum PlotChangeType {	
	SHOW_TITLE("SHOW_TITLE"), 
	FONT_TITLE("FONT_TITLE"), 
	COLOR_TITLE("COLOR_TITLE"), 
	PLOT_BACKGROUND("PLOT_BACKGROUND"),
	ORIENTATION_VERTICAL("ORIENTATION_VERTICAL"),
	MINY("MINY"),
	MAXY("MAXY"),
	MINX("MINX"),
	MAXX("MAXX"),
	AUTOADJUSTX("AUTOADJUSTX"),
	AUTOADJUSTY("AUTOADJUSTY"),
	LABELX("LABELX"),
	LABELY("LABELY"),
	LABELXFONT("LABELXFONT"),
	LABELYFONT("LABELYFONT"),
	LABELXCOLOR("LABELXCOLOR"),
	LABELYCOLOR("LABELYCOLOR");
	
	String description;
	   
	PlotChangeType(String descr) {
		   this.description = descr;
	   }
	   
	   public String getDescription(){
		    return description;
		   }
	  
}


