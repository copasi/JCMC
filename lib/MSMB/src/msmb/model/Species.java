package msmb.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.Vector;

import msmb.gui.MainGui;
import msmb.parsers.mathExpression.MR_Expression_Parser_ReducedParserException;
import msmb.parsers.mathExpression.syntaxtree.CompleteExpression;
import msmb.parsers.mathExpression.visitor.Look4UndefinedMisusedVisitor;
import msmb.utility.*;


public class Species   implements Serializable{
	String name = new String();
	boolean invisibleSpecies = false;
	Vector<String> initialQuantity = new Vector<String>(); 
	Vector<String> compartment = new Vector<String>(); 
	int type = Constants.SpeciesType.REACTIONS.copasiType;
	String expression = new String();
	private String editableInitialQuantity = new String();
	private String editableExpression = expression;
	public String getExpression() { 	
		if(expression == null) return new String();
		return expression.trim();	}
		
	public void setExpression(MultiModel m, String expr) throws Throwable {	
		if(expr.compareTo(Constants.NOT_EDITABLE_VIEW) == 0) return;
		if(expr.length() == 0) return ;
		boolean oldImportFromSBMLorCPS = MainGui.importFromSBMLorCPS;
		try {
			this.expression = expr;	
			if(this instanceof MultistateSpecies) {
				MultistateSpecies ms = (MultistateSpecies)this;
				Vector<MultistateSpecies> singles = ms.getExpandedSpecies(m, true);
				for(MultistateSpecies single : singles) {
					CellParsers.parseExpression_getUndefMisused(m,expression, Constants.TitlesTabs.SPECIES.getDescription(),Constants.SpeciesColumns.EXPRESSION.getDescription(),single);
				}
			} else {
				CellParsers.parseExpression_getUndefMisused(m,expression, Constants.TitlesTabs.SPECIES.getDescription(),Constants.SpeciesColumns.EXPRESSION.getDescription());
			}
			MainGui.importFromSBMLorCPS = false;
			this.expression = m.reprintExpression_forceCompressionElements(expression,Constants.TitlesTabs.SPECIES.getDescription(),Constants.SpeciesColumns.EXPRESSION.getDescription());
		} catch (Exception ex) {

			throw ex;
		} finally {
			editableExpression = expr;
			MainGui.importFromSBMLorCPS = oldImportFromSBMLorCPS;
		}
	}

	
	public String getEditableInitialQuantity() {	return editableInitialQuantity; }
	public String getEditableExpression() {	return editableExpression; }
	
	String notes = new String();
	String SBMLid = new String();

	public String getSBMLid() {
		return SBMLid;
	}
	public void setSBMLid(String sBMLid) {
		SBMLid = sBMLid;
	}
	
	public Species() { initialQuantity.clear(); initialQuantity.add(new String()); }
	public Species(String name) { this.setName(name);  initialQuantity.clear(); initialQuantity.add(new String()); }
	
	public Vector getAllFields() { /// should have the field ordered according to the columns in the tables
		Vector r = new Vector();
		r.add(this.getDisplayedName());
		r.add(this.getInitialQuantity_listString());
		r.add(this.getType());
		r.add(this.getCompartment_listString());
		r.add(this.getNotes());
		r.add(this.getSBMLid());
		return r;
	}
	
	public String getSpeciesName() { 
				 return name; 
	}
	public String getDisplayedName() { 	return getSpeciesName();	} //multistate species override this method
	public String getName() { return getDisplayedName(); }
	
	public void setName(String newName) {
		try {
		this.name = new String(newName.getBytes("UTF-8"),"UTF-8");
	} catch (UnsupportedEncodingException e) {
		//e.printStackTrace();
	}	}
	
	public Vector<String> getInitialQuantity() {	
		return initialQuantity; 
	}
	
	public String getInitialQuantity_listString() {		
		String ret = new String();
		if(initialQuantity.size() > 0) {
			for(int i = 0; i< initialQuantity.size()-1; i++) {
				ret += initialQuantity.get(i)+", ";
			}
			ret += initialQuantity.get(initialQuantity.size()-1);
		}
		return ret; 
	}
	
	
	public void setInitialQuantity(MultiModel m, String initialQ) throws Throwable {	
		if(m==null) return;
		if(initialQ==null || initialQ.compareTo(Constants.NOT_EDITABLE_VIEW) == 0) return;
		if(initialQ.length()==0) return;
		
		try{
			Double.parseDouble(initialQ);
			this.initialQuantity.clear();	
			this.initialQuantity.add(initialQ);
		} catch (Exception e) {// not a number, expression... so let's try to parse it
				Vector<String> elements = new Vector<String>();
				try {
					elements = CellParsers.extractElementsInList(m,initialQ, Constants.TitlesTabs.SPECIES.getDescription(), Constants.SpeciesColumns.INITIAL_QUANTITY.getDescription());
				} catch (MySyntaxException e1) {
					e1.printStackTrace();
				}
				this.initialQuantity.clear();	
				
				Vector<MySyntaxException> exToThrow = new Vector();
				
				for(int i = 0; i < elements.size(); i++) {
					try {
						CellParsers.parseExpression_getUndefMisused(m,elements.get(i), Constants.TitlesTabs.SPECIES.getDescription(),Constants.SpeciesColumns.INITIAL_QUANTITY.getDescription());
						this.initialQuantity.add(elements.get(i));
					} catch (MySyntaxException e1) {
						exToThrow.add(e1);
						continue;
					}
				}
				
				if(exToThrow.size() > 0) {
					MySyntaxException ex = exToThrow.get(0);
					for(int i = 1; i < exToThrow.size(); i++) {
						ex = new MySyntaxException(ex.getMessage(), exToThrow.get(i));
					}
					throw ex;
				}
		
		} finally {

			editableInitialQuantity = getInitialQuantity_listString();
		}
		
	
		
		
		
	}
	
	public int getType() {
			return type;
	}
	
	public void setType(int CMetab_Type) {	
		this.type = CMetab_Type;
	}
	
	public String getCompartment_listString() {		
		String ret = new String();
		if(compartment == null) return ret;
		if(compartment.size() > 0) {
			for(int i = 0; i< compartment.size()-1; i++) {
				ret += compartment.get(i)+", ";
			}
			ret += compartment.get(compartment.size()-1);
		}
		return ret; 
	}
	
	public Vector<String> getCompartments() {		
		return this.compartment; 
	}
	
	public void setCompartment(MultiModel m, String compartment) throws Exception {	
		if(compartment == null) return;
		if(m==null) return;
		if(compartment.compareTo(Constants.NOT_EDITABLE_VIEW) == 0) return;
		if(compartment.length()==0) return;
		try {
			//Vector<Vector<String>> undef_misused = CellParsers.parseExpression_getUndefMisused(m,compartment, Constants.TitlesTabs.SPECIES.getDescription(),Constants.SpeciesColumns.COMPARTMENT.getDescription());
			Vector<Vector<String>> undef_misused = CellParsers.parseListExpression_getUndefMisused(m,compartment, Constants.TitlesTabs.SPECIES.getDescription(),Constants.SpeciesColumns.COMPARTMENT.getDescription());
			
		
		} catch (Exception ex) {
			/*if(m.getComp(compartment)!=null) {
				if(!this.compartment.contains(compartment))	this.compartment.add(compartment);	
			}*/
			throw ex;	
		}	
		
		Vector<String> names = new Vector<String>();
		try {
			names = CellParsers.extractNamesInList(m,compartment, Constants.TitlesTabs.SPECIES.getDescription(), Constants.SpeciesColumns.COMPARTMENT.getDescription());
		} catch (MySyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i = 0; i < names.size(); i++) {
			if(m.getComp(names.get(i))!=null) {
				if(!this.compartment.contains(names.get(i))) this.compartment.add(names.get(i));	
			} else {
				throw new MySyntaxException(Constants.SpeciesColumns.COMPARTMENT.index, "Compartment  \""+ names.get(i)+"\" is not defined.", Constants.TitlesTabs.SPECIES.getDescription());
			}
		}
		
		
	}

	public String getNotes() {		return notes;	}
	public void setNotes(String notes) {		this.notes = notes;	}
	
	
	
	



	public void setEditableInitialQuantity(MultiModel m, String editableString) throws Throwable {
		setInitialQuantity(m,editableString);
	}

	public void setEditableExpression(MultiModel m,String editableString) throws Throwable {
		setExpression(m,editableString);		
	}



	public void setExpression_withoutParsing(String expression) {
		this.expression = expression;
		
	}




	public boolean alreadyInComp(String cmpName) {
	   for(int i = 0; i < compartment.size(); i++) {
		   String current = compartment.get(i);
		   if(current.compareTo(cmpName)==0) return true;
	   }
		return false;
	}


	@Override
	public String toString() {
		return this.getDisplayedName();
	}

	public void setInvisible(boolean b) {
		invisibleSpecies = b;		
	}

}
