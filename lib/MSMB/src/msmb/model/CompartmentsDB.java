package msmb.model;

import msmb.gui.MainGui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import msmb.utility.*;

import msmb.parsers.mathExpression.MR_Expression_Parser;
import msmb.parsers.mathExpression.MR_Expression_Parser_ReducedParserException;
import msmb.parsers.mathExpression.syntaxtree.CompleteExpression;
import msmb.parsers.mathExpression.visitor.Look4UndefinedMisusedVisitor;


//ADD SBML ID!!!

public class CompartmentsDB {
	TreeMap<Integer, Compartment> compVector = new TreeMap<Integer, Compartment>();
	HashMap<String, Integer> compIndexes = new HashMap<String, Integer>();
	MultiModel multiModel = null;
	
	
	
	public CompartmentsDB(MultiModel mm) {
		compVector = new TreeMap<Integer, Compartment>();
		compIndexes = new HashMap<String, Integer>();
		compVector.put(0,null);
		multiModel = mm;
	}
	
	public Vector<Compartment> getAllCompartments() {
		return new Vector(this.compVector.values());
	}
	
	public Vector<String> getAllNames() {
		Vector n = new Vector();
		for(int i = 0; i < compVector.size(); i++) {
			Compartment s = compVector.get(i);
			if(s!=null)n.add(s.getName());
		}
		return n;
	}
	
	public Compartment getComp(String name) {
		if(compIndexes.get(name)== null) return null;
		int ind = compIndexes.get(name).intValue();
		return getComp(ind);
	}
	
	/*public Vector<Compartment> getComp(String name) {
		Vector<String> names;
		try {
			names = CellParsers.extractListElements(multiModel,name, Constants.TitlesTabs.SPECIES.getDescription(), Constants.SpeciesColumns.COMPARTMENT.getDescription());
		} catch (MySyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		Vector<Compartment> ret = new Vector<Compartment>();
		for(int i = 0; i < names.size(); i++) {
			if(compIndexes.get(names.get(0))== null) ret.add(null);
			int ind = compIndexes.get(name).intValue();
			ret.add(getComp(ind));
		}
		return ret;
	}*/
	
	public Compartment getComp(int index) {
		if(index < 0 || index >= compVector.size()) {
			return null;
		}
		Compartment ret = compVector.get(index);
		return ret;
	}

	
	public int addChangeComp(String name) throws Throwable {
		if(name.length() == 0) name = MainGui.compartment_default_for_dialog_window;
		if(!compIndexes.containsKey(name)) { //it is a new comp
			Compartment c = new Compartment(name);
			compIndexes.put(c.getName(), compVector.size());
			multiModel.addNamedElement(c.getName(), Constants.TitlesTabs.COMPARTMENTS.index);
			compVector.put(compVector.size(),c);
				return compVector.size()-1;
		} else { //comp already defined
			return 0;//nothing to do?
		}
		
	}
	
	public int addChangeComp(int index, String name, String type,
			String initial, String expression, String notes) throws Throwable {
		
		if(name.trim().length() == 0) return -1;
		Integer ind = compIndexes.get(name);
		

		if(index != -1) {
			Compartment old = compVector.get(index);
			if(old!= null) {
				multiModel.removeNamedElement(old.getName(), new Integer(Constants.TitlesTabs.COMPARTMENTS.index));
				compIndexes.remove(old.getName());
				
			}
		}
		
		if(ind != null && ind != index ) { // the name is already assigned to another element
			Throwable cause = new Throwable(name);
			throw new ClassNotFoundException("A compartment already exists with the name "+name, cause);
		}
		
		
		try{
			if(ind ==null) { //it is a new comp
				Compartment c = new Compartment(name);
				c.setNotes(notes);
				c.setType(type);
				if(index==-1) ind = compVector.size();
				else ind = index;
				compIndexes.put(c.getName(), ind);
				compVector.put(ind,c); //take the place even if expressions contains error
				multiModel.addNamedElement(c.getName(), Constants.TitlesTabs.COMPARTMENTS.index);
				c.setExpression(multiModel,expression.trim());
				c.setInitialVolume(multiModel,initial);
				compVector.put(ind,c);
				if(!MainGui.donotCleanDebugMessages) MainGui.clear_debugMessages_defaults_relatedWith(Constants.TitlesTabs.COMPARTMENTS.getDescription(), ind);
				return compVector.size()-1;
				
			} else { //comp already defined
				
				Compartment c = compVector.get(ind);
				compIndexes.put(name, ind);
				multiModel.addNamedElement(name, Constants.TitlesTabs.COMPARTMENTS.index);
			
				c.setNotes(notes);
				c.setType(type);
				c.setExpression(multiModel,expression);
				c.setInitialVolume(multiModel,initial);

				compVector.put(ind, c);

				if(!MainGui.donotCleanDebugMessages) MainGui.clear_debugMessages_defaults_relatedWith(Constants.TitlesTabs.COMPARTMENTS.getDescription(), ind);
				return ind;
			}
		} catch (MySyntaxException ex) {
			if(ex.getColumn()==Constants.CompartmentsColumns.EXPRESSION.index && expression.trim().length() >0) {
				Vector<String> undef = null;
				if(expression.length() >0) {
					  InputStream is = new ByteArrayInputStream(expression.getBytes("UTF-8"));
					  MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
					  CompleteExpression root = parser.CompleteExpression();
					  Look4UndefinedMisusedVisitor undefVisitor = new Look4UndefinedMisusedVisitor(multiModel);
					  root.accept(undefVisitor);
					  undef = undefVisitor.getUndefinedElements();
				}
				if(undef != null){
					 if(undef.size()==1 && undef.get(0).compareTo(name)==0) { //just self reference in ode/expression and it is allowed
						return  addChangeComp_withoutParsing(name,  type, initial,expression, notes);
					} 
					 
					 else {
						  for(int i = 0; i < undef.size(); i++) {
							 if(undef.get(i).compareTo(name)==0){
								 undef.remove(i);
								 break;
							 }
						 }
						 String message = "Missing element definition: " + undef.toString();
						 ex = new MySyntaxException(message, ex);
					 }
					throw ex;
				} 

			}
			return -1; 
			
			
			
		}
		
	}
	
	public int addChangeComp_withoutParsing(String name, String type,
			String initial, String expression, String notes) throws Throwable {
		if(!compIndexes.containsKey(name)) { //it is a new comp
				Compartment c = new Compartment(name);
				c.setExpression_withoutParsing(expression);
				c.setInitialVolume(multiModel,initial);
				c.setNotes(notes);
				c.setType(type);
				compIndexes.put(c.getName(), compVector.size());
				compVector.put( compVector.size(),c);
				multiModel.addNamedElement(c.getName(), Constants.TitlesTabs.COMPARTMENTS.index);
				return compVector.size()-1;
			} else { //comp already defined
				int ind = compIndexes.get(name);
				Compartment c = compVector.get(ind);
				c.setNotes(notes);
				c.setType(type);
				c.setExpression_withoutParsing(expression);
				c.setInitialVolume(multiModel,initial);

				compVector.put(compIndexes.get(name), c);

				if(!MainGui.donotCleanDebugMessages) MainGui.clear_debugMessages_defaults_relatedWith(Constants.TitlesTabs.COMPARTMENTS.getDescription(), ind);
				return 0;//nothing to do?
			}
		
		
	}
	
	
	public boolean removeComp(int toBeRemoved){
		int size = compVector.keySet().size();
		compIndexes.remove(compVector.get(toBeRemoved+1).getName());
		multiModel.removeNamedElement(compVector.get(toBeRemoved+1).getName(), new Integer(Constants.TitlesTabs.COMPARTMENTS.index));
		for(int i = toBeRemoved+1; i < size; i++) {
			Compartment succ = compVector.get(i+1);
			if(succ==null) {
				compVector.remove(compVector.size()-1);
				break; 
			}
			compVector.put(i, succ);
			compIndexes.put(succ.getName(), i);
			
		}
		return true;
		
	}
	
	public void clear() {
		compVector.clear();
		compIndexes.clear();
	}
	
	public String getEditableExpression(int row, int column) {
		Compartment element = this.compVector.get(row+1);
		String ret = null;
		if(column == Constants.CompartmentsColumns.INITIAL_SIZE.index) {ret = element.getEditableVolume();}
		if (column == Constants.CompartmentsColumns.EXPRESSION.index) { ret = element.getEditableExpression();}
		return ret;
	
}
	
	public Integer getCompIndex(String name) {
		return compIndexes.get(name);
	}
	
}

/*class ValueComparator implements Comparator {

	  Map base;
	  public ValueComparator(Map base) {
	      this.base = base;
	  }

	 public int compare(Object a, Object b) {

	    if((Integer)a < (Integer)b) {
	      return 1;
	    } else if((Integer)a == (Integer)b) {
	      return 0;
	    } else {
	      return -1;
	    }
	  }
	}
*/