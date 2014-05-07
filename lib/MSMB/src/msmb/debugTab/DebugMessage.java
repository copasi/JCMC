package  msmb.debugTab;

import java.io.Serializable;

import msmb.utility.Constants;


public class DebugMessage implements Comparable<DebugMessage>, Serializable{
	private static final long serialVersionUID = 1L;
	private double priority = 0; //priority 0.2 = priority 0 sub-item 2 (tree view in debug tab)
	private String origin_table = new String();
	private int origin_row = 0;
	private int origin_col = 0;
	private String problem = new String();
	private int status = 0; 

	/*private String mnemonic = new String();
	public String getMnemonic() {return mnemonic;	}
	public void setMnemonic(String mnemonic) {	this.mnemonic = mnemonic;}*/
	
	public int getStatus() {	return status;	}
	public void setStatus(int statusIssue) {this.status = statusIssue;	}
	
	public String getOrigin_table() { return origin_table;}
	public void setOrigin_table(String t) {origin_table = t;}
	
	public int getOrigin_row() { return origin_row;}
	public void setOrigin_row(int t) {origin_row = t;}
	
	public int getOrigin_col() { return origin_col;}
	public void setOrigin_col(int t) {origin_col = t;}
	
	public String getProblem() { return problem;}
	public void setProblem(String t) {problem = t;}
	
	public double getPriority() { return priority;}
	public void setPriority(double t) {priority = t;}
	
	public String getShortDescription() {
		String name_column = new String();
		if(origin_table.compareTo(Constants.TitlesTabs.REACTIONS.getDescription()) == 0) name_column = Constants.ReactionsColumns.getDescriptionFromIndex(origin_col);
		else if(origin_table.compareTo(Constants.TitlesTabs.SPECIES.getDescription()) == 0) name_column = Constants.SpeciesColumns.getDescriptionFromIndex(origin_col);
		else if(origin_table.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription()) == 0) name_column = Constants.GlobalQColumns.getDescriptionFromIndex(origin_col);
		else if(origin_table.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription()) == 0) name_column = Constants.CompartmentsColumns.getDescriptionFromIndex(origin_col);
		else if(origin_table.compareTo(Constants.TitlesTabs.EVENTS.getDescription()) == 0) name_column = Constants.EventsColumns.getDescriptionFromIndex(origin_col);
		else if(origin_table.compareTo(Constants.TitlesTabs.FUNCTIONS.getDescription()) == 0) name_column = Constants.FunctionsColumns.getDescriptionFromIndex(origin_col);
		String s = new String(origin_table+"@("+origin_row+","+ name_column+"): " + DebugConstants.PriorityType.getDescriptionFromIndex(priority));
		return s;
	}
	
	public String getCompleteDescription() {
		String s = new String();
		String name_column = new String();
		if(origin_table.compareTo(Constants.TitlesTabs.REACTIONS.getDescription()) == 0) name_column = Constants.ReactionsColumns.getDescriptionFromIndex(origin_col);
		else if(origin_table.compareTo(Constants.TitlesTabs.SPECIES.getDescription()) == 0) name_column = Constants.SpeciesColumns.getDescriptionFromIndex(origin_col);
		else if(origin_table.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription()) == 0) name_column = Constants.GlobalQColumns.getDescriptionFromIndex(origin_col);
		else if(origin_table.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription()) == 0) name_column = Constants.CompartmentsColumns.getDescriptionFromIndex(origin_col);
		else if(origin_table.compareTo(Constants.TitlesTabs.EVENTS.getDescription()) == 0) name_column = Constants.EventsColumns.getDescriptionFromIndex(origin_col);
		else if(origin_table.compareTo(Constants.TitlesTabs.FUNCTIONS.getDescription()) == 0) name_column = Constants.FunctionsColumns.getDescriptionFromIndex(origin_col);
		s+= "Issue in table " + origin_table + " at row " + origin_row + ", column \"" + name_column + "\"" + System.getProperty("line.separator");
		s+= problem + System.getProperty("line.separator");
		return s;
	}
	
	@Override
	public String toString() {
		String s = new String();
		s+= this.getShortDescription();
		return s;
	}
	
	@Override
	public int compareTo(DebugMessage o) {
		int ret = this.getOrigin_table().compareTo(o.getOrigin_table());
		if(ret == 0) {
			ret = this.origin_row - o.getOrigin_row();
			if(ret ==0) ret = this.getOrigin_col() - o.getOrigin_col();
		}
		return ret;
	}
		
}
