package acgui;

import java.io.Serializable;

/**
 * @author Thomas
 *
 */
public class ACComponentDefinition implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ModuleDefinition parent;
	private VariableType variableType;
	private String refName;
	private String name;
	
	public ACComponentDefinition(ModuleDefinition iParent)
	{
		parent = iParent;
		variableType = null;
		refName = null;
		name = null;
	}
	
	public ACComponentDefinition(ModuleDefinition iParent, String iRefName)
	{
		parent = iParent;
		refName = iRefName;
		variableType = null;
		name = null;
	}
	
	public ACComponentDefinition(ModuleDefinition iParent, VariableType iVariableType, String iRefName)
	{
		parent = iParent;
		variableType = iVariableType;
		refName = iRefName;
		name = null;
	}
	
	public ACComponentDefinition(ModuleDefinition iParent, String iRefName, String iName)
	{
		parent = iParent;
		refName = iRefName;
		name = iName;
		variableType = null;
	}
	
	public ACComponentDefinition(ModuleDefinition iParent, VariableType iVariableType, String iRefName, String iName)
	{
		parent = iParent;
		variableType = iVariableType;
		refName = iRefName;
		name = iName;
	}
	
	public ModuleDefinition getParent()
	{
		return parent;
	}
	
	public void setVariableType(VariableType iVariableType)
	{
		variableType = iVariableType;
	}
	
	public VariableType getVariableType()
	{
		return variableType;
	}
	
	public void setName(String iName)
	{
		name = iName;
	}

	public String getName()
	{
		return name;
	}
	
	public void setRefName(String newName)
	{
		refName = newName;
	}
	
	public String getRefName()
	{
		return refName;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}
