package  msmb.debugTab;

import msmb.gui.MainGui;
import msmb.utility.Constants;

public class FoundElementToDelete extends FoundElement {
	
	String actionToTake = new String(Constants.DeleteActions.SELECT.getDescription());
	String newValue = new String();
	String oldValue = new String();

	public String getActionToTake() {	return actionToTake;	}
	public void setActionToTake(String actionToTake) {		this.actionToTake = actionToTake;	}
	public String getNewValue() {		return newValue;	}
	public void setNewValue(String newValue) {		this.newValue = newValue;	}

	public String getOldValue() {		return oldValue;	}
	public void setOldValue(String newValue) {		this.oldValue = newValue;	}

	public FoundElementToDelete(String tableDescr, int row, int col) {
		super(tableDescr, row, col);
		if(tableDescr.compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0 && this.getCol()==Constants.ReactionsColumns.REACTION.index) actionToTake=Constants.DeleteActions.DELETE.getDescription();
		
		
		/*if(tableDescr.compareTo(Constants.TitlesTabs.SPECIES.getDescription())==0) {	newValue = MainGui.species_defaultInitialValue;	}
		else if(tableDescr.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription())==0) {newValue = MainGui.compartment_defaultInitialValue;	}
		else if(tableDescr.compareTo(Constants.TitlesTabs.EVENTS.getDescription())==0) {	newValue = "";	}
		else if(tableDescr.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())==0) {	newValue = MainGui.globalQ_defaultValue_for_dialog_window;	}
		else if(tableDescr.compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0) {	newValue = "";		}*/

	}

	public FoundElementToDelete(FoundElement el) {
		super(el.getTableDescription(), el.getRow(), el.getCol());	
		if(this.getTableDescription().compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0 && this.getCol()==Constants.ReactionsColumns.REACTION.index) 
			actionToTake=Constants.DeleteActions.DELETE.getDescription();
		
	/*	if(this.getTableDescription().compareTo(Constants.TitlesTabs.SPECIES.getDescription())==0) {	newValue = MainGui.species_defaultInitialValue;	}
		else if(this.getTableDescription().compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription())==0) {newValue = MainGui.compartment_defaultInitialValue;	}
		else if(this.getTableDescription().compareTo(Constants.TitlesTabs.EVENTS.getDescription())==0) {	newValue = "";	}
		else if(this.getTableDescription().compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())==0) {	newValue = MainGui.globalQ_defaultValue_for_dialog_window;	}
		else if(this.getTableDescription().compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0) {	newValue = "";		}*/
	
	}
	
	public FoundElementToDelete(FoundElementToDelete el) {
		super(el.getTableDescription(), el.getRow(), el.getCol());	
		this.setActionToTake(el.getActionToTake());
		this.setCol(el.getCol());
		this.setNewValue(el.getNewValue());
		this.setOldValue(el.getOldValue());
		this.setRow(el.getRow());
		this.setTableDescription(el.getTableDescription());
	}
	
		
	//I DON'T CHECK THE COLUMN BECAUSE I USE THOSE METHODS FOR THE DELETE ELEMENTS AND I WANT THAT FOUNDELEMENTS IN THE SAME ROW (NO MATTER WHAT THE COLUMN IS) ARE CONSIDERED EQUAL
	@Override
	public boolean equals(Object obj) {
		FoundElementToDelete o = (FoundElementToDelete)obj;
		int r = this.tableDescription.compareTo(o.getTableDescription());
		if(r!=0) return false;
		if(this.row != o.getRow()) return false;
		return true;
	}
	
	
	//I DON'T CHECK THE COLUMN BECAUSE I USE THOSE METHODS FOR THE DELETE ELEMENTS AND I WANT THAT FOUNDELEMENTS IN THE SAME ROW (NO MATTER WHAT THE COLUMN IS) ARE CONSIDERED EQUAL
	@Override 
	public int hashCode() {
        return (27 * (7 + this.row) + Constants.TitlesTabs.getIndexFromDescription(this.tableDescription));
    }
	
	@Override
	public String toString() {
		String ret = new String();
		ret	+= "("+this.getTableDescription()+") " + MainGui.printMainElementRow(this);
		return ret;
	}
}
