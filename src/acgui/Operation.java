package acgui;

public enum Operation
{
	SUM("Sum"), PRODUCT("Product");
	
	private final String name;
	
	/**
	 * Construct the object.
	 * @param iName the name of the port
	 */
	private Operation(String iName)
	{
		name = iName;
	}
	
	/**
	 * Return the string representation of the operation.
	 * @return the string representation of the operation
	 */
	public String toString()
	{
		return name;
	}
}
