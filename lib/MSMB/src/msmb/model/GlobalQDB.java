package msmb.model;

import msmb.commonUtilities.ChangedElement;
import msmb.commonUtilities.MSMB_Element;
import msmb.commonUtilities.MSMB_InterfaceChange;
import msmb.debugTab.DebugConstants;
import msmb.debugTab.DebugMessage;
import msmb.gui.MainGui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

import msmb.utility.*;

import msmb.parsers.mathExpression.syntaxtree.CompleteExpression;
import msmb.parsers.mathExpression.visitor.Look4UndefinedMisusedVisitor;


import msmb.parsers.mathExpression.MR_Expression_Parser;
import msmb.parsers.multistateSpecies.MR_MultistateSpecies_Parser;
import msmb.parsers.multistateSpecies.syntaxtree.CompleteMultistateSpecies_Operator;
import msmb.parsers.multistateSpecies.visitor.MultistateSpeciesVisitor;


public class GlobalQDB {
	
	TreeMap<Integer, GlobalQ> globalQVector = new TreeMap<Integer, GlobalQ>();
	HashMap<String, Integer> globalQIndexes = new HashMap<String, Integer>();
	MultiModel multiModel = null;
	
	public GlobalQDB(MultiModel mm) {
		globalQVector = new TreeMap<Integer, GlobalQ>();
		globalQIndexes = new HashMap<String, Integer>();
		globalQVector.put(0,null);//rows starts at index 1
		multiModel = mm;
	}
	
	public Vector<GlobalQ> getAllGlobalQ() {
		return new Vector(this.globalQVector.values());
	}
	
	public Vector<String> getAllNames() {
		Vector n = new Vector();
		for(int i = 0; i < globalQVector.size(); i++) {
			GlobalQ s = globalQVector.get(i);
			if(s!=null)n.add(s.getName());
		}
		return n;
	}
	
	
	public GlobalQ getGlobalQ(String name) {
		if(globalQIndexes.get(name)==null) return null;
		int ind = globalQIndexes.get(name).intValue();
		return getGlobalQ(ind);
	}
	
	
	public GlobalQ getGlobalQ(int index) {
		if(index < 0 || index >= globalQVector.size()) {
			return null;
		}
		GlobalQ ret = globalQVector.get(index);
		return ret;
	}
	
	public int addChangeGlobalQ(int index, String name,  String initialValue, String type, String expression, String notes) throws Throwable {
		if(name.trim().length() == 0) return -1;
		Integer ind = globalQIndexes.get(name);
		

		if(index != -1) {
			GlobalQ old = globalQVector.get(index);
			if(old!= null) {
				multiModel.removeNamedElement(old.getName(), new Integer(Constants.TitlesTabs.GLOBALQ.index));
				globalQIndexes.remove(old.getName());
				
			}
		}
		
		if(ind != null && ind != index ) { // the name is already assigned to another species
			Throwable cause = new Throwable(name);
			throw new ClassNotFoundException("A globalQ already exists with the name "+name, cause);
		}
		
		
		int columnToAnalyze = -1;;
		try{
		//	if(ind == null) {//it is a new globalq
			
			if(!globalQIndexes.containsKey(name) && 
					(index >= globalQIndexes.size() || index == -1)) { //it is a new globalq
				MSMB_InterfaceChange changeToReport_IntfGlq = new MSMB_InterfaceChange(MSMB_Element.GLOBAL_QUANTITY);
				changeToReport_IntfGlq.setElementBefore(null);
				changeToReport_IntfGlq.setElementAfter(new ChangedElement(name,MSMB_Element.GLOBAL_QUANTITY));
				if(MainGui.actionInColumnName) MainGui.setChangeToReport(changeToReport_IntfGlq);
			
				
				GlobalQ c = new GlobalQ(name);
				c.setNotes(notes);
				c.setType(type);
				if(index==-1) ind = globalQVector.size();
				else ind = index;
				globalQIndexes.put(c.getName(), ind);
				globalQVector.put(ind,c); //take the place even if expressions contains error
				multiModel.addNamedElement(c.getName(), Constants.TitlesTabs.GLOBALQ.index);
				columnToAnalyze  = Constants.GlobalQColumns.EXPRESSION.index;
				c.setExpression(multiModel,expression);
				columnToAnalyze  = Constants.GlobalQColumns.VALUE.index;
				c.setInitialValue(multiModel,initialValue);
				globalQVector.put(ind,c);
				return globalQVector.size()-1;
			} else { //globalQ already defined
				GlobalQ c = globalQVector.get(index);
				
				String oldName = c.getName();
				if(oldName.compareTo(name)!=0) {
					MSMB_InterfaceChange changeToReport_IntfGlq = new MSMB_InterfaceChange(MSMB_Element.GLOBAL_QUANTITY);
					changeToReport_IntfGlq.setElementBefore(new ChangedElement(oldName,MSMB_Element.GLOBAL_QUANTITY));
					changeToReport_IntfGlq.setElementAfter(new ChangedElement(name,MSMB_Element.GLOBAL_QUANTITY));
					if(MainGui.actionInColumnName) MainGui.setChangeToReport(changeToReport_IntfGlq);
				}
				c.setName(name);
				
				globalQIndexes.put(name, index);
				columnToAnalyze  = Constants.GlobalQColumns.EXPRESSION.index;
				c.setExpression(multiModel,expression);
				columnToAnalyze  = Constants.GlobalQColumns.VALUE.index;
					
				c.setInitialValue(multiModel,initialValue);
				c.setNotes(notes);
				c.setType(type);
				globalQVector.put(index,c);
				multiModel.addNamedElement(name, Constants.TitlesTabs.GLOBALQ.index);
					
				if(!MainGui.donotCleanDebugMessages && ind!=null) MainGui.clear_debugMessages_defaults_relatedWith(Constants.TitlesTabs.GLOBALQ.getDescription(), ind);
				return index;
			}
		} catch (MySyntaxException ex) {
			if(ex.getColumn()==Constants.GlobalQColumns.EXPRESSION.index && expression.trim().length() >0) {
				Vector<String> undef = null;
				Vector<String> misused = null;
				if(expression.length() >0) {
					  InputStream is = new ByteArrayInputStream(expression.getBytes("UTF-8"));
					  MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
					  CompleteExpression root = parser.CompleteExpression();
					  Look4UndefinedMisusedVisitor undefVisitor = new Look4UndefinedMisusedVisitor(multiModel);
					  root.accept(undefVisitor);
					  undef = undefVisitor.getUndefinedElements();
					  misused = undefVisitor.getMisusedElements();
				}
				if(undef != null){
					if(undef.size() ==0) throw ex; 
					if(undef.size()==1 && undef.get(0).compareTo(name)==0
							&& !misused.contains(name)) { 
						//just self reference in ode/expression and it is allowed, however if it is in the misused, it means that is a case like mu*m(1-m/ms) where the first m is a function call and ms is the species reference
						return addChangeGlobalQ_withoutParsing(name,  initialValue, type, expression, notes);
					} 
					 
					 else {
						 for(int i = 0; i < undef.size(); i++) {
							 if(undef.get(i).compareTo(name)==0){
								 undef.remove(i);
								 break;
							 }
						 }
						 
						 if(MainGui.importFromSBMLorCPS) {
							 	//because there may be circular references in the expressions of different parameters so
							 	//each has to be added anyway and then checked only at the end
								 return addChangeGlobalQ_withoutParsing(name,  initialValue, type, expression, notes);
						 }
						
						 if(undef.size() > 0){
							 String message = "Missing element definition: " + undef.toString();
							 ex = new MySyntaxException(message, ex);
						 } else {
							 if(misused.size() > 0){
								 String message = "Misused element: " + misused.toString();
								 ex = new MySyntaxException(message, ex);
							 } 
						 }
						 
					
					 }
					throw ex;
				} 

			} 
			throw ex;
			//return -1; 
		} catch (Throwable e) {
			e.printStackTrace();
			MySyntaxException ex = new MySyntaxException(columnToAnalyze, "Problem parsing the expression.", Constants.TitlesTabs.GLOBALQ.getDescription());
			throw ex;
		}

	}
	
	
	public int addChangeGlobalQ_withoutParsing(String name,  String initialValue, String type, String expression, String notes) throws Throwable {
		if(name.trim().length() == 0) return -1;
		if(name.length() == 0) name = "default";
		if(!globalQIndexes.containsKey(name)) { //it is a new comp
			GlobalQ c = new GlobalQ(name);
			c.setExpression_withoutParsing(expression);
			c.setInitialValue(multiModel,initialValue);
			c.setNotes(notes);
			c.setType(type);
			globalQIndexes.put(c.getName(), globalQVector.size());
			multiModel.addNamedElement(c.getName(), Constants.TitlesTabs.GLOBALQ.index);
			globalQVector.put(globalQVector.size(),c);
			return globalQVector.size()-1;
		} else { //globalQ already defined
			Integer ind = globalQIndexes.get(name);
			GlobalQ c = globalQVector.get(ind);
			c.setExpression_withoutParsing(expression);
			c.setInitialValue(multiModel,initialValue);
			c.setNotes(notes);
			c.setType(type);
			globalQIndexes.put(name, ind);
			globalQVector.put(ind,c);
			if(!MainGui.importFromSBMLorCPS) multiModel.addNamedElement(c.getName(), Constants.TitlesTabs.GLOBALQ.index);
			
			if(!MainGui.donotCleanDebugMessages) MainGui.clear_debugMessages_defaults_relatedWith(Constants.TitlesTabs.GLOBALQ.getDescription(), ind);
			return ind;
		}
			
	}

	public boolean contains(String name) {
		return globalQIndexes.containsKey(name);
	}
	
	
	public boolean removeGlobalQ(int toBeRemoved) {
			int size = globalQVector.keySet().size();
			if(toBeRemoved+1>=size) return true;
			globalQIndexes.remove(globalQVector.get(toBeRemoved+1).getName());
			multiModel.removeNamedElement(globalQVector.get(toBeRemoved+1).getName(),new Integer(Constants.TitlesTabs.GLOBALQ.index));
			for(int i = toBeRemoved+1; i < size; i++) {
				GlobalQ succ = globalQVector.get(i+1);
				if(succ==null) {
					globalQVector.remove(globalQVector.size()-1);
					break;
				}
				
				globalQVector.put(i, succ);
				globalQIndexes.put(succ.getName(), i);
			}
			return true;
	}
	
	public void clear() {
		globalQVector.clear();
		globalQIndexes.clear();
	}

	public String getEditableExpression(int row, int column) {
			GlobalQ element = this.globalQVector.get(row+1);
			String ret = null;
			if(column == Constants.GlobalQColumns.VALUE.index) {ret = element.getEditableValue();}
			if (column == Constants.GlobalQColumns.EXPRESSION.index) { ret = element.getEditableExpression();}
			return ret;
		
	}

	public Integer getGlobalQIndex(String name) {
		return globalQIndexes.get(name);
	}

	public void updateSBMLid_fromCopasiDataModel(String name, String sbmlId) {
			Integer index = getGlobalQIndex(name);
			if(index != null) {
				GlobalQ el = globalQVector.get(index);
				if(el!=null) el.setSBMLid(sbmlId); 
				globalQVector.put(index, el);
			}
		
	}
	
	
}
