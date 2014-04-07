package acgui;

import java.io.Serializable;

/**
 * @author Thomas
 *
 */
public class ConnectionDefinition implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ModuleDefinition parent;
	private ACComponentDefinition sourceDefinition;
	private ACComponentDefinition targetDefinition;
	private TerminalType sourceType;
	private TerminalType targetType;

	public ConnectionDefinition(ModuleDefinition iParent, ACComponentDefinition sDefinition, TerminalType sType, ACComponentDefinition tDefinition, TerminalType tType)
	{
		parent = iParent;
		sourceDefinition = sDefinition;
		targetDefinition = tDefinition;
		sourceType = sType;
		targetType = tType;
	}
	
	public ModuleDefinition getParent()
	{
		return parent;
	}
	
	public ACComponentDefinition getSourceDefinition()
	{
		return sourceDefinition;
	}
	
	public ACComponentDefinition getTargetDefinition()
	{
		return targetDefinition;
	}
	
	public TerminalType getSourceType()
	{
		return sourceType;
	}
	
	public TerminalType getTargetType()
	{
		return targetType;
	}
}
