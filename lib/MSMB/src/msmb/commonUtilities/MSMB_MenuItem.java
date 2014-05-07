package msmb.commonUtilities;

import msmb.utility.Constants;

public enum MSMB_MenuItem
{
	FILE("File"),
		NEW("New"),
		OPEN("Open"),
			OPEN_CPS(".cps"),		
			OPEN_SBML(".xml"),
			OPEN_MSMB(".msmb"),
		SAVE("Save"),
		SAVE_AS("Save as..."),
			SAVE_MSMB(".msmb"),
		EXPORT("Export"),
			SAVE_CPS("to .cps"),		
			EXPORT_SBML("to .xml"),
			EXPORT_XPP("to .xpp"),
		PRINT_TABLES_PDF("Print tables to PDF"),
		PREFERENCES("Preferences..."),
		RECENT("Recent files"),	
		EXIT("Exit"),	

	EDIT("Edit"),
	DELETE_ELEMENT("Delete element..."),
	SHOW_EXPANDED_EXPR("Show expanded expression in table..."),
			REACTIONS(Constants.TitlesTabs.REACTIONS.getDescription()),
			SPECIES(Constants.TitlesTabs.SPECIES.getDescription()),
			GLOBALQ(Constants.TitlesTabs.GLOBALQ.getDescription()),
			COMPARTMENTS(Constants.TitlesTabs.COMPARTMENTS.getDescription()),
			EVENTS(Constants.TitlesTabs.EVENTS.getDescription()),
		ADD_REVERSE_REACTION("Add reverse reaction"),
		VALIDATE("Validate model"),
		MULTISTATE_BUILDER("Multistate builder..."),
		COMPLEX_BUILDER("Complex builder..."),
		IMPORT_ANNOTATIONS("Import annotations...");
	
	private final String menuString;
	
	private MSMB_MenuItem(String displayedString)	{		menuString = displayedString;	}
	
	public String getMenuString() { return menuString; }
	
	
	 public static MSMB_MenuItem getEnum(String value) {
		    if(value == null)      {
		    	throw new IllegalArgumentException();
		    }
		    
		    for(MSMB_MenuItem v : values()) {
	            if(value.compareTo(v.menuString)==0) {
	            	return v;
	            }
		    }
	        return null;
	    }
	
}
