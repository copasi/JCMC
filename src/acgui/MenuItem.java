package acgui;

/**
 * An enum to represent the menu items.
 * @author T.C. Jones
 */
public enum MenuItem
{
	PREFERENCES("Preferences..."),	
	
	NEW("New"), // a root model created
	OPEN("Open"), // a root model opened from file
	RECENT("Recent Files"),
	SAVE("Save"),
	SAVE_AS("Save As"),
	EXPORT_SBML("Export SBML"),
	CLOSE("Close"), // close the model and get back to new
	EXIT("Exit"), // exit application
	// -------------------------------------------------------------------------------------
	ADD_SUBMODULE_NEW("Add Submodule (New)"),
	ADD_SUBMODULE_TEMPLATE("Add Submodule (Template)"),
	ADD_SUMMATION_MODULE("Add Summation Module"),
	ADD_PRODUCT_MODULE("Add Product Module"),
	SAVE_SUBMODULE_AS_TEMPLATE("Save Submodule (As Template)"),
	REMOVE_SUBMODULE("Remove Submodule"),
	// -------------------------------------------------------------------------------------
	VALIDATE_MODEL("Validate Model"),
	VIEW_MODEL("View Model"),
	FLATTEN_MODEL("Flatten Model"),
	DECOMPOSE_INTO_MODULES("Decompose into Modules"),
	// -------------------------------------------------------------------------------------
	HELP_CONTENTS("Help Contents"),
	ABOUT_AGGREGATION_CONNECTOR("About Aggregation Connector");
	
	private final String name;
	
	/**
	 * Construct the object.
	 * @param iName the name of the menu item
	 */
	private MenuItem(String iName)
	{
		name = iName;
	}
	
	/**
	 * Return the string representation of the menu item.
	 * @return the string representation of the menu item
	 */
	public String toString()
	{
		return name;
	}
}
