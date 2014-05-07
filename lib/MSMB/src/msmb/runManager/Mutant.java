package msmb.runManager;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import msmb.debugTab.DebugConstants;
import msmb.debugTab.DebugMessage;
import msmb.gui.MainGui;
import msmb.model.MultiModel;
import msmb.utility.CellParsers;
import msmb.utility.Constants;
import msmb.utility.Constants.UnitTypeTime;

import org.COPASI.CModel;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.collect.Sets;

public class Mutant implements Comparable, Serializable{
		static final long serialVersionUID = -1413489169413099946L;
		protected String name = new String();
		public HashMap<String, DebugMessage> debugMessages = new HashMap<String, DebugMessage>();
		
		
		//to store the keys of the element explicitly requested to be taken from the MSMB model
		protected HashSet<String> fromBaseSet = new HashSet<String>();
		
		protected HashMap<String, String> changes = new HashMap<String,String>();
		
		//key: element changed, value: new expression, mutant that the change comes from
		protected HashMap<String, MutablePair<String, String>> cumulativeChanges = new HashMap<String, MutablePair<String, String>>();
		
		public String getName() { return name;}
		public HashMap<String, String> getChanges() { return new HashMap<String, String>(changes);}
		public HashSet<String> getFromBaseSet() { return new HashSet<String>(fromBaseSet);}
		public HashMap<String, MutablePair<String, String>> getCumulativeChanges() { return new HashMap<String, MutablePair<String, String>>(cumulativeChanges);}
	
		public void clearCumulativeChanges(){
			cumulativeChanges.clear();
		}
		
		public void clearChanges(){
			changes.clear();
		}
		
		public void addCumulativeChanges(HashMap<String, String> localChanges, String fromMutant) {
			for (String k : localChanges.keySet()) {
				cumulativeChanges.put(k, new MutablePair(localChanges.get(k),fromMutant));
			}
		}
		
		public void addCumulativeChange(String k, MutablePair<String,String> expr_from) {
			cumulativeChanges.put(k, expr_from);
		}
		
		public void addCumulativeChanges(HashMap<String, MutablePair<String, String>> c) {
			cumulativeChanges.putAll(c);
		}
		
		public Mutant(String name) {
			this.name = new String(name);
		}
		
		public void addChange(MutantChangeType ty, String element_name, String element_new_value) {
			changes.put(generateChangeKey(ty, element_name), element_new_value);
		}
		
		public static String generateChangeKey(MutantChangeType ty, String element_name) {
			return new String(ty.getDescription()+"%"+CellParsers.cleanName(element_name));
		}
		
		public static String extractTypeFromKey(String key) {
			return key.substring(0, key.indexOf("%"));
		}
		
		public static String extractElementNameFromKey(String key) {
			return key.substring(key.indexOf("%")+1);
		}
		
		public String printMutant() {
			String ret = new String();
			ret += "VVVVVVVVVVVVVVVVVVVV"+"\n";
			ret +="MUTANT: "+name+"\n";
			//System.out.println("Parents: "+parents);
			ret +="Changes: "+changes+"\n";
			ret +="From base set: "+fromBaseSet+"\n";
			ret +="^^^^^^^^^^^^^^^^^^^^"+"\n";
			return ret;
		}
		
		@Override
		public String toString() {
			return name;
		}

		@Override
		public int compareTo(Object o) {
			return this.name.compareTo(((Mutant)o).name);
			}
		
			
		@Override
		public boolean equals(Object obj) {
			 if (obj == null)
		            return false;
		        if (obj == this)
		            return true;
		        if (!(obj instanceof Mutant))
		            return false;

		        Mutant rhs = (Mutant) obj;
		        return new EqualsBuilder().
		            append(name, rhs.name).
		            isEquals();
		}
		
		
		@Override
		public int hashCode() {
	        return new HashCodeBuilder(409, 191). 
	            append(name).
	            toHashCode();
	    }
		
		
		
		public void updateChanges(Vector<HashMap> allChanges) {
			changes.putAll(allChanges.get(0));
			
			cumulativeChanges.putAll(allChanges.get(1));
			
			fromBaseSet.clear();
			fromBaseSet.addAll((Collection<? extends String>) allChanges.get(2));
		}
		
		
		public void addDebugMessage(DebugMessage dm) {
			String keyDM = dm.getOrigin_table()+"@"+dm.getPriority()+"_"+dm.getOrigin_row()+"_"+dm.getOrigin_col();
			debugMessages.put(keyDM, dm);
		}
		
		
		public boolean addDebugMessage_expression(Vector<Vector<String>> elements, Vector<Mutant> actual_parents, 
			String table, int row, int col) {
			Vector<String> undef = elements.get(0);
			Vector<String> misused = elements.get(1);
			Vector<String> parents = elements.get(2);
			
			boolean errorsFound = false;
			if(parents.size() > 0) {
				for (String p : parents) {
					if(!actual_parents.contains(new Mutant(p))) {
						  DebugMessage dm = new DebugMessage();
						 dm.setOrigin_table(table);
						 dm.setProblem("Reference to @"+p+", but "+p+" is not an ancestor of the node.");
						 dm.setPriority(DebugConstants.PriorityType.INCONSISTENCIES.priorityCode);
						 dm.setOrigin_col(col); 
						 dm.setOrigin_row(row+1);
						String key = dm.getOrigin_table()+"@"+dm.getPriority()+"_"+dm.getOrigin_row()+"_"+dm.getOrigin_col();
						 debugMessages.put(key, dm);
						 errorsFound = true;
					}
				}
			}
			
			for(String err: undef) {
				  DebugMessage dm = new DebugMessage();
					 dm.setOrigin_table(table);
					 dm.setProblem("Undefined element: "+err);
					 dm.setPriority(DebugConstants.PriorityType.MISSING.priorityCode);
					 dm.setOrigin_col(col); 
					 dm.setOrigin_row(row+1);
					String key = dm.getOrigin_table()+"@"+dm.getPriority()+"_"+dm.getOrigin_row()+"_"+dm.getOrigin_col();
					 debugMessages.put(key, dm);
					 errorsFound = true;
			}
			
			
			return errorsFound;
			
			
		}


		
		
		
	
}


enum MutantChangeType {	
	GLQ_INITIAL_VALUE("GLQ"), 
	SPC_INITIAL_VALUE("SPC"),
	COMP_INITIAL_VALUE("COMP");
	
	String description;
	   
	   MutantChangeType(String descr) {
		   this.description = descr;
	   }
	   
	   
	   public String getDescription(){
		    return description;
		   }
};
