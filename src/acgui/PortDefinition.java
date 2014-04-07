package acgui;

/**
 * @author Thomas
 *
 */
public class PortDefinition extends ACComponentDefinition
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PortType portType;
	private VariableType variableType;
	private String refNameSBMLid;
	
	/**
	 * @param iParent
	 */
	public PortDefinition(ModuleDefinition iParent)
	{
		super(iParent);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param iParent
	 * @param iRefName
	 */
	public PortDefinition(ModuleDefinition iParent, String iRefName)
	{
		super(iParent, iRefName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param iParent
	 * @param iRefName
	 * @param iName
	 */
	public PortDefinition(ModuleDefinition iParent, String iRefName, String iName)
	{
		super(iParent, iRefName, iName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param iParent
	 * @param iRefName
	 * @param iName
	 */
	public PortDefinition(ModuleDefinition iParent, String iRefName, String iName, PortType iPortType, VariableType iVariableType)
	{
		super(iParent, iRefName, iName);
		portType = iPortType;
		variableType = iVariableType;
		refNameSBMLid = "";
	}
	
	public void setType(PortType iType)
	{
		portType = iType;
	}
	
	public PortType getType()
	{
		return portType;
	}
	
	public void setVariableType(VariableType iType)
	{
		variableType = iType;
	}
	
	public VariableType getVariableType()
	{
		return variableType;
	}
	
	public void setRefNameSBMLID(String id)
	{
		refNameSBMLid = id;
	}
	
	public String getRefNameSBMLID()
	{
		return refNameSBMLid;
	}
}
