package acgui;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Thomas
 *
 */
public class ModuleDefinition implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ModuleDefinition parent;
	protected String name;
	private byte[] msmbData;
	private boolean valid;
	private boolean external;
	private String externalSource;
	private String externalModelRef;
	private String md5;
	private String id;
	private ArrayList<ModuleDefinition> children;
	private ArrayList<ACComponentDefinition> ports;
	private ArrayList<ConnectionDefinition> connections;
	private ArrayList<ACComponentDefinition> visibleVariables;
	private ArrayList<ACComponentDefinition> equivalences;
	private ArrayList<Module> instances;

	/**
	 * 
	 */
	public ModuleDefinition()
	{
		children = new ArrayList<ModuleDefinition>();
		ports = new ArrayList<ACComponentDefinition>();
		connections = new ArrayList<ConnectionDefinition>();
		visibleVariables = new ArrayList<ACComponentDefinition>();
		equivalences = new ArrayList<ACComponentDefinition>();
		instances = new ArrayList<Module>();
	}
	
	public ModuleDefinition(String iName)
	{
		this();
		name = iName;
		parent = null;
		msmbData = null;
		id = null;
	}
	
	public ModuleDefinition(String iName, ModuleDefinition iParent)
	{
		this();
		name = iName;
		parent = iParent;
		msmbData = null;
		id = null;
	}
	
	public ModuleDefinition(String iName, String iID, ModuleDefinition iParent)
	{
		this();
		name = iName;
		parent = iParent;
		msmbData = null;
		id = iID;
	}
	
	public ModuleDefinition(String iName, ModuleDefinition iParent, byte[] imsmbData)
	{
		this();
		name = iName;
		parent = iParent;
		msmbData = imsmbData;
		id = null;
	}
	
	public ModuleDefinition getParent()
	{
		return parent;
	}
	
	public void setName(String iName)
	{
		name = iName;
		//AC_GUI.drawingBoard.updateCellName(drawingCell, name);
	}

	public String getName()
	{
		return name;
	}
	
	public void setMSMBData(byte[] data)
	{
		msmbData = data;
	}
	
	public byte[] getMSMBData()
	{
		return msmbData;
	}
	
	public void setID(String newID)
	{
		id = newID;
	}

	public String getID()
	{
		return id;
	}
	
	public void setValid(boolean value)
	{
		valid = value;
	}
	
	public boolean isValid()
	{
		return valid;
	}
	
	public void setExternal(boolean value)
	{
		external = value;
	}
	
	public boolean isExternal()
	{
		return external;
	}
	
	public void setExternalSource(String value)
	{
		externalSource = value;
	}
	
	public String getExternalSource()
	{
		return externalSource;
	}
	
	public void setExternalModelRef(String value)
	{
		externalModelRef = value;
	}
	
	public String getExternalModelRef()
	{
		return externalModelRef;
	}
	
	public void setmd5(String value)
	{
		md5 = value;
	}
	
	public String getmd5()
	{
		return md5;
	}
	
	public void addChild(ModuleDefinition mod)
	{
		children.add(mod);
		//AC_GUI.masterModuleList.add(mod);
	}

	public void removeChild(ModuleDefinition mod)
	{
		children.remove(mod);
		//AC_GUI.masterModuleList.remove(mod);
	}
	
	public ArrayList<ModuleDefinition> getChildren()
	{
		return children;
	}
	
	/**
	 * Add the given port to the module.
	 * @param port the port to add
	 */
	public void addPort(PortDefinition port)
	{
		ports.add(port);
	}
	
	/**
	 * Remove the given port from the list of ports.
	 * @param port the port to be removed
	 */
	public void removePort(PortDefinition port)
	{
		ports.remove(port);
		/*
		if(port.getRowIndex() != ports.size())
		{
			//the deleted Port did not occupy the last row in the Port table,
			//the indices of the Ports listed below it must be updated.
			updatePortIndices(port);
		}
		*/
	}
	
	/**
	 * Return the list of ports.
	 * @return the list of ports
	 */
	public ArrayList<ACComponentDefinition> getPorts()
	{
		return ports;
	}
	
	/**
	 * Add the given connection to the list of connections.
	 * @param connection the connection to add
	 */
	public void addConnection(ConnectionDefinition connection)
	{
		connections.add(connection);
	}
	
	/**
	 * Remove the given connection from the list of connections.
	 * @param connection the connection to be removed
	 */
	public void removeConnection(ConnectionDefinition connection)
	{
		connections.remove(connection);
	}
	
	/**
	 * Return the list of connections.
	 * @return the list of connections
	 */
	public ArrayList<ConnectionDefinition> getConnections()
	{
		return connections;
	}
	
	public void addVisibleVariable(VisibleVariableDefinition var)
	{
		visibleVariables.add(var);
	}
	
	public void removeVisibleVariable(VisibleVariableDefinition var)
	{
		visibleVariables.remove(var);
	}
	
	public ArrayList<ACComponentDefinition> getVisibleVariables()
	{
		return visibleVariables;
	}
	
	public void addEquivalence(EquivalenceDefinition eNode)
	{
		equivalences.add(eNode);
	}
	
	public void removeEquivalence(EquivalenceDefinition eNode)
	{
		equivalences.remove(eNode);
	}
	
	public ArrayList<ACComponentDefinition> getEquivalences()
	{
		return equivalences;
	}
	
	public void addInstance(Module module)
	{
		instances.add(module);
	}
	
	public void removeInstance(Module module)
	{
		instances.remove(module);
	}
	
	public ArrayList<Module> getInstances()
	{
		return instances;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}
