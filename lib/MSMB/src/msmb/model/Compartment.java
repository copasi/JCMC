package msmb.model;

import msmb.gui.MainGui;

import java.util.Vector;

import msmb.utility.CellParsers;
import msmb.utility.Constants;
import msmb.utility.MySyntaxException;

import org.COPASI.CCompartment;

public class Compartment {
	String name = new String();
	int type = CCompartment.FIXED;
	String initialVolume = MainGui.compartment_defaultInitialValue;
	String notes = new String();
	String expression = new String();
	
	private String editableVolume= new String();
	private String editableExpression = expression;
	public String getEditableVolume() {	return editableVolume; }
	public String getEditableExpression() {	return editableExpression; }
	
	
	public String getExpression() {	return expression;}
	
	public void setExpression(MultiModel m, String expr) throws Throwable {
		if(expr.length()==0) return;
		this.expression = expr;
		try {
				CellParsers.parseExpression_getUndefMisused(m, expr, Constants.TitlesTabs.COMPARTMENTS.getDescription(),Constants.CompartmentsColumns.EXPRESSION.getDescription());
			} catch (MySyntaxException ex) {
				throw ex;
			}
		finally {
			editableExpression = expression;
		}
	}
	
	public void setExpression_withoutParsing(String expression2) {
		this.expression = expression2;
	}
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public int getType() {
		return type;
	}

	public void setType(String type_descr) {
		if(type_descr.length() > 0) 	this.type = Constants.CompartmentsType.getCopasiTypeFromDescription(type_descr);
	}
	public String getInitialVolume() {
		return initialVolume;
	}
	
	
	public void setInitialVolume(MultiModel m, String initialVolume) throws Throwable {
		if(initialVolume.length()==0) return;
		this.initialVolume = initialVolume;
		try{
			Double.parseDouble(initialVolume);
			return;
		} catch (Exception e) {// not a number, expression... so let's try to parse it
			if(initialVolume.length() == 0) return ;
			try {
				CellParsers.parseExpression_getUndefMisused(m, initialVolume, Constants.TitlesTabs.COMPARTMENTS.getDescription(),Constants.CompartmentsColumns.INITIAL_SIZE.getDescription());
			} catch (Exception ex) {
				throw ex;
			}
		}
		 finally {
			 editableVolume = getInitialVolume();
		}
	}

	
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}



	

	public Compartment() { name = MainGui.compartment_default_for_dialog_window;}
	public Compartment(String name) { setName(name);}
	
	
	
	public Vector<Object> getAllFields() {
		Vector<Object> r = new Vector<Object>();
		r.add(this.getName());
		r.add(this.getType());
		r.add(this.getInitialVolume());
		r.add(this.getExpression());
		r.add(this.getNotes());
		return r;
	}
}
