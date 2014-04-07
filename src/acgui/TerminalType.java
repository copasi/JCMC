package acgui;

/**
 * An enum to represent the type of terminal.
 * @author Thomas
 *
 */
public enum TerminalType
{
	EQUIVALENCE("Equivalence"), VISIBLEVARIABLE("VisibleVariable"), PORT("Port");
	
	private final String name;
	
	/**
	 * Construct the object.
	 * @param iName the name of the port
	 */
	private TerminalType(String iName)
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
	
	public static TerminalType getType(String value)
	{
		if (value == null)
		{
			throw new IllegalArgumentException();
		}
		
		for (TerminalType t : values())
		{
			if (value.equals(t.toString()))
			{
				return t;
			}
		}
		throw new IllegalArgumentException();
	}
}
