package acgui;

/**
 * @author Thomas
 *
 */
public class VisibleVariableDefinition extends ACComponentDefinition
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param iParent
	 */
	public VisibleVariableDefinition(ModuleDefinition iParent)
	{
		super(iParent);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param iParent
	 * @param iRefName
	 */
	public VisibleVariableDefinition(ModuleDefinition iParent, String iRefName)
	{
		super(iParent, iRefName);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param iParent
	 * @param iRefName
	 */
	public VisibleVariableDefinition(ModuleDefinition iParent, String iRefName, VariableType iVariableType)
	{
		super(iParent, iVariableType, iRefName);
	}

	/**
	 * @param iParent
	 * @param iRefName
	 * @param iName
	 */
	public VisibleVariableDefinition(ModuleDefinition iParent, String iRefName, String iName)
	{
		super(iParent, iRefName, iName);
		// TODO Auto-generated constructor stub
	}
}
