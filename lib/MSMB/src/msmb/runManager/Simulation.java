package msmb.runManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import msmb.utility.CellParsers;

public class Simulation extends Mutant {
	private HashSet<Mutant> mutantsParameters = null;
	String duration = new String();
	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration, boolean isFromBaseSet) {		
		String k = generateChangeKey(SimulationChangeType.TOTAL_TIME, "");
		
		if(duration == null  || duration.trim().length() == 0) {
			changes.remove(k);
			fromBaseSet.remove(k);
			this.duration = new String();
			return;
		} 
		this.duration = duration;	
		if(!isFromBaseSet) {
			addChange(SimulationChangeType.TOTAL_TIME, "", duration);
		}
		else {
			changes.remove(k);
			fromBaseSet.add(k);
			cumulativeChanges.remove(k);
		}
	}
	
	public String getIntervalSize() {		return intervalSize;	}
	public void setIntervalSize(String intervalSize) {		this.intervalSize = intervalSize;	}
	public String getIntervals() {		return intervals;	}
	public void setIntervals(String intervals) {		this.intervals = intervals;	}

	String intervalSize = new String();
	String intervals = new String();
	

	public Simulation(String name) {
		super(name);
		mutantsParameters = new HashSet<Mutant>();
	}
	
	public void addChange(SimulationChangeType ty, String element_name, String element_new_value) {
		changes.put(generateChangeKey(ty, element_name), element_new_value);
	}
	
	public static String generateChangeKey(SimulationChangeType ty, String element_name) {
		return new String(ty.getDescription()+"%"+CellParsers.cleanName(element_name));
	}
	
	public void addMutantParameter(Mutant mutantParam) {
		mutantsParameters.add(mutantParam);
	}
	
	public void removeMutantParameter(Mutant mutantParam) {
		mutantsParameters.remove(mutantParam);
	}
	

	
	public boolean hasMutantParameter(Mutant mutantParam) {
		return mutantsParameters.contains(mutantParam);
	}
	
	public boolean hasChange(SimulationChangeType ty, String element_name) {
		return changes.containsKey(generateChangeKey(ty, element_name));
	}
	

	
	public String printSimulation() {
		String ret = mutantsParameters.toString();
		return ret +"\n"+ printMutant();
	}
	
	public Vector<Object> getMutantsParameters() {
		 return (new Vector<Object>(Arrays.asList(mutantsParameters.toArray())));
	}

	public void setName(String newName) {
		name = new String(newName);
	}

	public void removeLocalChange(SimulationChangeType simType) {
		changes.remove(generateChangeKey(simType, ""));
	}
	public void removeFromBaseSet(SimulationChangeType ty) {
		fromBaseSet.remove(generateChangeKey(ty, ""));
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
	        if (!(obj instanceof Simulation))
	            return false;

	        Simulation rhs = (Simulation) obj;
	        return new EqualsBuilder().
	            append(name, rhs.name).
	            isEquals();
	}
	
	
	@Override
	public int hashCode() {
        return new HashCodeBuilder(73, 45). 
            append(name).
            toHashCode();
    }

	public void addAllMutantParameter(Vector<Mutant> newList) {
		mutantsParameters.addAll(newList);
	}

	public void clearMutantParameters() {
		mutantsParameters.clear();
	}

	

}


enum SimulationChangeType {	
	TOTAL_TIME("TOTALTIME", "Duration"), 
	METHOD("METHOD", "Method");
	
	String description;
	String GUI_label;
	   
	SimulationChangeType(String descr, String guilabel) {
		   this.description = descr;
		   this.GUI_label = guilabel;
	   }
	   
	   public String getDescription(){
		    return description;
		   }
	   
	   public String getGuiLabel() {
		   return GUI_label;
	   }
};