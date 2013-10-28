package acgui;

/**
 * @author T.C. Jones
 *
 */
public class ModuleTemplate
{
	private String id;
	private String name;
	
	public ModuleTemplate(String iID, String iName)
	{
		id = iID;
		name = iName;
	}
	
	public String getID()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}
}
