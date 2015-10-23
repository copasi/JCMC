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
	
	public static Operation getType(String value)
	{
		if (value == null)
		{
			throw new IllegalArgumentException();
		}
		
		for (Operation p : values())
		{
			if (value.equals(p.toString()))
			{
				return p;
			}
		}
		throw new IllegalArgumentException();
	}
}
