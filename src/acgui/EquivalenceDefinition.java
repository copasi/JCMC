/**
 * 
 */
package acgui;

/**
 * @author Thomas
 *
 */
public class EquivalenceDefinition extends ACComponentDefinition
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param iParent
	 */
	public EquivalenceDefinition(ModuleDefinition iParent)
	{
		super(iParent);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param iParent
	 * @param iRefName
	 */
	public EquivalenceDefinition(ModuleDefinition iParent, String iRefName)
	{
		super(iParent, iRefName);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param iParent
	 * @param iRefName
	 */
	public EquivalenceDefinition(ModuleDefinition iParent, String iRefName, VariableType iVariableType)
	{
		super(iParent, iVariableType, iRefName);
	}

	/**
	 * @param iParent
	 * @param iRefName
	 * @param iName
	 */
	public EquivalenceDefinition(ModuleDefinition iParent, String iRefName, String iName)
	{
		super(iParent, iRefName, iName);
		// TODO Auto-generated constructor stub
	}
}
