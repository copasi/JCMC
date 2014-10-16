package msmb.runManager;

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

import msmb.model.MultiModel;
import msmb.utility.CellParsers;
import msmb.utility.Constants;
import msmb.utility.Constants.CompartmentsType;

public class Simulation extends Mutant{
	private HashSet<Mutant> mutantsParameters = null;
	String duration = new String();
	Method method = new Method();
	
	static final long serialVersionUID = 3925046071696394682L;
	
	public String getDuration() {
		return duration;
	}
	
	@Override
	public void clearCumulativeChanges() {
		cumulativeChanges.clear();
		 resetFieldsWithChanges();
	}

	private void resetFieldsWithChanges() {
		Iterator<String> it = changes.keySet().iterator();
		
		while(it.hasNext()) {
				String k = it.next();
				boolean isFromBaseSet = fromBaseSet.contains(k);
				if(k.compareTo(generateChangeKey(SimulationChangeType.TOTAL_TIME, ""))==0) {
					this.duration = changes.get(k);
				} else if(k.compareTo(generateChangeKey(SimulationChangeType.INTERVAL_SIZE, ""))==0) {
					this.intervalSize= changes.get(k);
				} else if(k.compareTo(generateChangeKey(SimulationChangeType.INTERVAL_NUMBER, ""))==0) {
					this.intervals=changes.get(k);
				}
		}
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
	
	public void setIntervalSize(String intervalSize, boolean isFromBaseSet) {	
			String k = generateChangeKey(SimulationChangeType.INTERVAL_SIZE, "");
			if(intervalSize == null  || intervalSize.trim().length() == 0) {
				changes.remove(k);
				fromBaseSet.remove(k);
				this.intervalSize = new String();
				return;
			} 
			this.intervalSize = intervalSize;	
			if(!isFromBaseSet) {
				addChange(SimulationChangeType.INTERVAL_SIZE, "", intervalSize);
			}
			else {
				changes.remove(k);
				fromBaseSet.add(k);
				cumulativeChanges.remove(k);
			}	
		}
	
	
	
	public String getIntervals() {		return intervals;	}
	
	public void setIntervals(String intervals, boolean isFromBaseSet) {		
		
		String k = generateChangeKey(SimulationChangeType.INTERVAL_NUMBER, "");
		if(intervals == null  || intervals.trim().length() == 0) {
			changes.remove(k);
			fromBaseSet.remove(k);
			this.intervals = new String();
			return;
		} 
		this.intervals = intervals;	
		if(!isFromBaseSet) {
			addChange(SimulationChangeType.INTERVAL_NUMBER, "", intervals);
		}
		else {
			changes.remove(k);
			fromBaseSet.add(k);
			cumulativeChanges.remove(k);
		}	
		
	}

	String intervalSize = new String();
	String intervals = new String();
	transient  private HashMap<String, HashMap<String, Vector<Double>>> savedTimeSeries;
	

	public Simulation(String name) {
		super(name);
		mutantsParameters = new HashSet<Mutant>();
	}
	
	public Simulation(Simulation s) {
		this(s.getName());
		mutantsParameters.addAll(s.mutantsParameters);
		this.fromBaseSet.addAll(s.fromBaseSet);
		this.changes.putAll(s.changes);
		this.cumulativeChanges.putAll(s.cumulativeChanges);
		this.intervals = s.intervals;
		this.duration = s.duration;
		this.intervalSize = s.intervalSize;
		this.method = s.method;
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
	
		int ret = new HashCodeBuilder(73, 45). 
	            append(name).
	            toHashCode();

	    return ret;
    }

	public void addAllMutantParameter(Vector<Mutant> newList) {
		mutantsParameters.addAll(newList);
	}

	public void clearMutantParameters() {
		mutantsParameters.clear();
	}

	public int getMethod() {
		return method.getMethodType();
	}

	public HashMap<String, Double> getMethodParameters() {
		return method.getMethodParameters();
	}

	
	
	public static enum CopasiMethods {
		DETERMINISTIC("Deterministic (LSODA)", CCopasiMethod.deterministic),
		GILLESPIE("Stochastic (Direct method)", CCopasiMethod.directMethod),
		GIBSONBRUCK("Stochastic (Gibson + Bruck)", CCopasiMethod.stochastic),
		TAULEAP("Stochastic (tau leap)", CCopasiMethod.tauLeap),
		ADAPTIVETAU("Stochastic (Adaptive SSA/tau leap)", CCopasiMethod.adaptiveSA);
		
		
		String description;
		int copasiIndex;
		
		CopasiMethods(String descr, int cindex) {
			   this.description = descr;
			   this.copasiIndex = cindex;
		   }
		
		public String getDescription() {return description;}
		public int getCopasiIndex() {return copasiIndex;}
		
		public static String getDescriptionFromIndex(int index){
		      for (CopasiMethods b : CopasiMethods.values()) {
			        if (index == b.copasiIndex) {
			          return b.description;
			      }
			    }
			   return new String();
		   }
		
		public static int getIndexFromDescription(String d){
		      for (CopasiMethods b : CopasiMethods.values()) {
			        if (d.equals(b.description)) {
			          return b.copasiIndex;
			      }
			    }
			   return -1;
		   }
		   
	};
	
	
	public static enum CopasiMethodsParameters {
		DETERMINISTIC(CCopasiMethod.deterministic),
		GILLESPIE(CCopasiMethod.directMethod),
		GIBSONBRUCK(CCopasiMethod.stochastic),
		TAULEAP(CCopasiMethod.tauLeap),
		ADAPTIVETAU(CCopasiMethod.adaptiveSA);
		
		public Vector<String> parameters;
		public Vector<Double> defaults;
		
		CopasiMethodsParameters(int which) {
			parameters = new Vector<String>();
			defaults = new Vector<Double>();
			
			if(which == CCopasiMethod.deterministic) {
				parameters.add("Absolute Tolerance");
				parameters.add("Relative Tolerance");
				parameters.add("Integrate Reduced Model");
				parameters.add("Max Internal Steps");
				defaults.add(new Double(1e-12));
				defaults.add(new Double(1e-06));
				defaults.add(new Double(0));
				defaults.add(new Double(10000));
			} else if(which == CCopasiMethod.stochastic) {
				parameters.add("Max Internal Steps");
				parameters.add("Subtype");
				parameters.add("Use Random Seed");
				parameters.add("Random Seed");
				defaults.add(new Double(1000000));
				defaults.add(new Double(2));
				defaults.add(new Double(0));
				defaults.add(new Double(1));
			}else if(which == CCopasiMethod.stochastic) {
				parameters.add("Max Internal Steps");
				parameters.add("Use Random Seed");
				parameters.add("Random Seed");
				defaults.add(new Double(1000000));
				defaults.add(new Double(0));
				defaults.add(new Double(1));
			}else if(which == CCopasiMethod.tauLeap) {
				parameters.add("Epsilon");
				parameters.add("Max Internal Steps");
				parameters.add("Use Random Seed");
				parameters.add("Random Seed");
				defaults.add(new Double(0.001));
				defaults.add(new Double(10000));
				defaults.add(new Double(0));
				defaults.add(new Double(1));
			} if(which == CCopasiMethod.adaptiveSA) {
				parameters.add("Epsilon");
				parameters.add("Max Internal Steps");
				parameters.add("Use Random Seed");
				parameters.add("Random Seed");
				defaults.add(new Double(0.03));
				defaults.add(new Double(1000000));
				defaults.add(new Double(0));
				defaults.add(new Double(1));
			} 
		  }
		
		public Vector<String> getParameters() { 
			Vector<String>  ret = new Vector<String>();
			ret.addAll(parameters);
			return ret;
		}
		
		public Vector<Double> getDefaults() { 
			Vector<Double>  ret = new Vector<Double>();
			ret.addAll(defaults);
			return ret;
		}
		   
	}

	public void clearTimeSeries() {
		savedTimeSeries = new HashMap<String, HashMap<String, Vector<Double>>>();
	}
	
	public void addTimeSeries(CTimeSeries timeSeries, String mutant) {
		if(timeSeries==null) return;
	    int iMax = (int)timeSeries.getNumVariables();
	    int lastIndex = (int)timeSeries.getRecordedSteps() - 1;
	    HashMap<String, Vector<Double>> singleTS = new HashMap<String, Vector<Double>>();
	    	Vector<Double> varT = new Vector<Double>();
	        String T = "TIME"; //time
	       	 for(int j = 0; j <= lastIndex; ++j) {
	        		 double y = timeSeries.getData(j, 0);
	        		 varT.add(y);
	        	 }
           singleTS.put(T, varT);
      
           for (int i = 1;i < iMax;++i)  {
	    	Vector<Double> var = new Vector<Double>();
	        String element = timeSeries.getTitle(i);
	       	 for(int j = 0; j <= lastIndex; ++j) {
	        		 double y = 0;
	        		 if(element.startsWith("Values[") || element.startsWith("Compartments[") ) {
        				 y = timeSeries.getData(j, i);
        			 }
        			 else {
        				 y =timeSeries.getConcentrationData(j, i);//6.0221415e23;
        			 }
            		 var.add(y);
	        	 }
           singleTS.put(element, var);
        }
		savedTimeSeries.put(mutant, singleTS);
		
	};
	
	public  Vector<Double> getTimeSeries(String mutant, String variable) {
		return savedTimeSeries.get(mutant).get(variable);
	}
	
	public  Vector<String> getTimeSeriesVariables(String mutant) {
		Vector<String> ret = new Vector<String>();
		ret.addAll(savedTimeSeries.get(mutant).keySet());
		return ret;
	}



}


enum SimulationChangeType {	
	TOTAL_TIME("TOTALTIME", "Duration"), 
	INTERVAL_NUMBER("INTERVALS", "Intervals"), 
	INTERVAL_SIZE("INTERVAL_SIZE", "Interval size"), 
	METHOD("METHOD", "Method"),
	PARAMETER("PARAMETER", "Method parameter");
	
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
	  
}



class Method implements Serializable {
	int method;
	HashMap<String, Double> parameters = new HashMap<String, Double>();
	
	public Method() {
		method = Simulation.CopasiMethods.getIndexFromDescription(SimulationsDB.DEFAULT_METHOD);
		Vector<String> param = new Vector<String>();
		Vector<Double> def  = new Vector<Double>();
		if(method == CCopasiMethod.deterministic) {
			param = Simulation.CopasiMethodsParameters.DETERMINISTIC.getParameters();
			def = Simulation.CopasiMethodsParameters.DETERMINISTIC.getDefaults();
		}
		for(int i = 0; i < param.size(); ++i) {
			parameters.put(param.get(i), def.get(i));
		}
		
	}
	
	public int getMethodType() {
		return method;
	}
	
	public HashMap<String, Double> getMethodParameters() {
		HashMap<String, Double> ret = new HashMap<String, Double>();
		ret.putAll(parameters);
		return ret;
	}
	
}