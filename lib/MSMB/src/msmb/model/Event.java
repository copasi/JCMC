package msmb.model;

import msmb.gui.MainGui;
import msmb.utility.CellParsers;
import msmb.utility.Constants;
import msmb.utility.MySyntaxException;

import java.util.*;

public class Event {
	private String name = new String();
	String trigger = new String();
	Vector<String> actions = new Vector<String>();
	String notes = new String();
	String delay = new String();
	boolean delayAfterCalculation = true;
	boolean shortCut_recalculateConcentration_changingVolume = false;
	int expandActionVolume = -1;
	
	public String getDelay() {	return delay;}
	public void setDelay(String delay) {	this.delay = delay.trim();	}
	
	public boolean isDelayAfterCalculation() {		return delayAfterCalculation;	}
	public void setDelayAfterCalculation(boolean delayAfterCalculation) {		this.delayAfterCalculation = delayAfterCalculation;	}
	public void setExpandActionVolume(int expandExtension) { this.expandActionVolume = expandExtension; }
	public int getExpandActionVolume() { return this.expandActionVolume; }
	
	public Event(String name) {
		this.name = name;
		if(MainGui.importFromTables) shortCut_recalculateConcentration_changingVolume = true;
	}
	
	public String getName() {		return name;	}
	public void setName(String name) {	this.name = name;	}
	public String getTrigger() {		return trigger;	}
	
	public void setTrigger(MultiModel m, String trigger) throws Throwable {
		if(trigger.length()==0) return;
		this.trigger = trigger;
		try {
				CellParsers.parseExpression_getUndefMisused(m, trigger, Constants.TitlesTabs.EVENTS.getDescription(),Constants.EventsColumns.TRIGGER.getDescription());
			} catch (MySyntaxException ex) {
				throw ex;
			} catch(Throwable ex) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
				throw new MySyntaxException(Constants.EventsColumns.TRIGGER.index, "Problems parsing the trigger. One of the elements is a misformed mathematical expression.", Constants.TitlesTabs.EVENTS.getDescription()); 
			} 
	}

	public Vector<String> getActions() {
		return actions;
	}
	
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public void setActions(MultiModel m, String actions) throws Throwable {
		
		Vector<String> elements = new Vector<String>();
		try {
			elements = CellParsers.extractElementsInList(m,actions, 
					Constants.TitlesTabs.EVENTS.getDescription(), Constants.EventsColumns.ACTIONS.getDescription());
		}  catch(Throwable ex) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
			throw new MySyntaxException(Constants.EventsColumns.ACTIONS.index, "Problems parsing the list of actions. One of the elements is a misformed mathematical expression.", Constants.TitlesTabs.EVENTS.getDescription()); 
		} 
		
		MySyntaxException complete = null; 
			
		for(String el : elements) {
			try {
				CellParsers.parseExpression_getUndefMisused(m, el, Constants.TitlesTabs.EVENTS.getDescription(),Constants.EventsColumns.ACTIONS.getDescription());
			} catch(Throwable ex) {
				if(complete == null) complete = new MySyntaxException(Constants.EventsColumns.ACTIONS.index, ex.getMessage(), Constants.TitlesTabs.EVENTS.getDescription());
				else {
					complete = new MySyntaxException(complete.getMessage()+"\n"+ex.getMessage(), complete);
				}
			} 
			this.actions.add(el.trim());
		}
		if(complete != null) throw complete;
	}
	
	public String getActionsAsString() {
		Iterator it = actions.iterator();
		String ret = new String();
		while(it.hasNext())  {
			ret += it.next();
			ret += ";";
		}
		ret = ret.substring(0, ret.length()-1);
		return ret;
	}
	
}
