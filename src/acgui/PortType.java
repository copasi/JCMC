package acgui;

/**
 * An enum to represent the types of ports.
 * @author T.C. Jones
 */
public enum PortType
{
	INPUT("Input"), OUTPUT("Output"), EQUIVALENCE("Equivalence");
	
	private final String name;
	
	/**
	 * Construct the object.
	 * @param iName the name of the port
	 */
	private PortType(String iName)
	{
		name = iName;
	}
	
	/**
	 * Return the string representation of the port.
	 * @return the string representation of the port
	 */
	public String toString()
	{
		return name;
	}
}
