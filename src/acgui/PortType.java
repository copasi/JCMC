package acgui;

/**
 * An enum to represent the type of port.
 * @author T.C. Jones
 */
public enum PortType
{
	INPUT("Input"), OUTPUT("Output"), EQUIVALENCE("Equivalence");
	
	private final String name;
	
	/**
	 * Construct the object.
	 * @param iName the name of the enum
	 */
	private PortType(String iName)
	{
		name = iName;
	}
	
	/**
	 * Return the string representation of the enum.
	 * @return the string representation of the enum
	 */
	public String toString()
	{
		return name;
	}
	
	public static PortType getType(String value)
	{
		if (value == null)
		{
			throw new IllegalArgumentException();
		}
		
		for (PortType p : values())
		{
			if (value.equals(p.toString()))
			{
				return p;
			}
		}
		throw new IllegalArgumentException();
	}
}
