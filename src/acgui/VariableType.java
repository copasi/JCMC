package acgui;

/**
 * An enum to represent the type of variable.
 * @author T.C. Jones
 */
public enum VariableType
{

	SPECIES("Species"), GLOBAL_QUANTITY("Global Quantity");
	
	private final String name;
	
	/**
	 * Construct the object.
	 * @param iName the name of the variable type
	 */
	private VariableType(String iName)
	{
		name = iName;
	}
	
	/**
	 * Return the string representation of the variable type.
	 * @return the string representation of the variable type
	 */
	public String toString()
	{
		return name;
	}
}
