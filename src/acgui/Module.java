/**
 * 
 */
package acgui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

/**
 * @author Thomas
 *
 */
public class Module implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Module parent;
	private ModuleDefinition moduleDef;
	private String id;
	private String name;
	private DefaultMutableTreeNode treeNode;
	private mxCell drawingCell;
	private mxGeometry drawingCellGeometry_Module;
	private mxGeometry drawingCellGeometry_Submodule;
	private String drawingCellStyle;
	private ArrayList<Module> children;
	private Set<String> submoduleNames;
	private ArrayList<ACComponentNode> ports;
	private ArrayList<ConnectionNode> connections;
	private ArrayList<ACComponentNode> visibleVariables;
	private ArrayList<ACComponentNode> equivalences;
	
	public Module()
	{
		children = new ArrayList<Module>();
		submoduleNames = new HashSet<String>();
		ports = new ArrayList<ACComponentNode>();
		connections = new ArrayList<ConnectionNode>();
		visibleVariables = new ArrayList<ACComponentNode>();
		equivalences = new ArrayList<ACComponentNode>();
	}
	
	public Module(String iName, Module iParent)
	{
		this();
		name = iName;
		parent = iParent;
		moduleDef = null;
		id = null;
		treeNode = null;
		drawingCell = null;
		drawingCellGeometry_Module = null;
		drawingCellGeometry_Submodule = null;
		drawingCellStyle = null;
	}
	
	public Module(String iName, ModuleDefinition iModuleDef)
	{
		this();
		name = iName;
		moduleDef = iModuleDef;
		parent = null;
		id = null;
		treeNode = null;
		drawingCell = null;
		drawingCellGeometry_Module = null;
		drawingCellGeometry_Submodule = null;
		drawingCellStyle = null;
	}
	
	public Module(String iName, ModuleDefinition iModuleDef, Module iParent)
	{
		this();
		name = iName;
		moduleDef = iModuleDef;
		parent = iParent;
		id = null;
		treeNode = null;
		drawingCell = null;
		drawingCellGeometry_Module = null;
		drawingCellGeometry_Submodule = null;
		drawingCellStyle = null;
	}
	
	public Module(String iName, ModuleDefinition iModuleDef, Module iParent, mxGeometry iModuleGeo, mxGeometry iSubmoduleGeo, String iStyle)
	{
		this();
		name = iName;
		moduleDef = iModuleDef;
		parent = iParent;
		treeNode = null;
		drawingCell = null;
		drawingCellGeometry_Module = iModuleGeo;
		drawingCellGeometry_Submodule = iSubmoduleGeo;
		drawingCellStyle = iStyle;
	}
	
	public Module getParent()
	{
		return parent;
	}
	
	public void setModuleDefinition(ModuleDefinition newDefinition)
	{
		moduleDef = newDefinition;
	}
	
	public ModuleDefinition getModuleDefinition()
	{
		return moduleDef;
	}
	
	public void setID(String newID)
	{
		id = newID;
	}

	public String getID()
	{
		return id;
	}
	
	public void setName(String newName)
	{
		name = newName;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setTreeNode(DefaultMutableTreeNode iNode)
	{
		treeNode = iNode;
	}

	public DefaultMutableTreeNode getTreeNode()
	{
		return treeNode;
	}
	
	public void setDrawingCell(mxCell iCell)
	{
		drawingCell = iCell;
	}
	
	public mxCell getDrawingCell()
	{
		return drawingCell;
	}
	
	public void setDrawingCellGeometryModule(mxGeometry iGeo)
	{
		drawingCellGeometry_Module = iGeo;
	}
	
	public mxGeometry getDrawingCellGeometryModule()
	{
		return drawingCellGeometry_Module;
	}
	
	public void setDrawingCellGeometrySubmodule(mxGeometry iGeo)
	{
		drawingCellGeometry_Submodule = iGeo;
	}
	
	public mxGeometry getDrawingCellGeometrySubmodule()
	{
		return drawingCellGeometry_Submodule;
	}
	
	public void setDrawingCellStyle(String iStyle)
	{
		drawingCellStyle = iStyle;
	}
	
	public String getDrawingCellStyle()
	{
		return drawingCellStyle;
	}
	
	/**
	 * Add the given module as a child of the current module.
	 * @param mod the module to add as a child
	 */
	public void addChild(Module mod)
	{
		children.add(mod);
		submoduleNames.add(mod.getName());
	}
	
	/**
	 * Remove the given module from the list of children.
	 * @param mod the module to be removed
	 */
	public void removeChild(Module mod)
	{
		children.remove(mod);
		submoduleNames.remove(mod.getName());
	}
	
	/**
	 * Return the list of children.
	 * @return the list of children
	 */
	public ArrayList<Module> getChildren()
	{
		return children;
	}
	
	/**
	 * Add the given port to the module.
	 * @param port the port to add
	 */
	public void addPort(PortNode port)
	{
		ports.add(port);
	}
	
	/**
	 * Remove the given port from the list of ports.
	 * @param port the port to be removed
	 */
	public void removePort(PortNode port)
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
	public ArrayList<ACComponentNode> getPorts()
	{
		return ports;
	}
	
	/**
	 * Add the given connection to the list of connections.
	 * @param connection the connection to add
	 */
	public void addConnection(ConnectionNode connection)
	{
		connections.add(connection);
	}
	
	/**
	 * Remove the given connection from the list of connections.
	 * @param connection the connection to be removed
	 */
	public void removeConnection(ConnectionNode connection)
	{
		connections.remove(connection);
	}
	
	/**
	 * Return the list of connections.
	 * @return the list of connections
	 */
	public ArrayList<ConnectionNode> getConnections()
	{
		return connections;
	}
	
	public void addVisibleVariable(VisibleVariableNode var)
	{
		visibleVariables.add(var);
	}
	
	public void removeVisibleVariable(VisibleVariableNode var)
	{
		visibleVariables.remove(var);
	}
	
	public ArrayList<ACComponentNode> getVisibleVariables()
	{
		return visibleVariables;
	}
	
	public void addEquivalence(EquivalenceNode eNode)
	{
		equivalences.add(eNode);
	}
	
	public void removeEquivalence(EquivalenceNode eNode)
	{
		equivalences.remove(eNode);
	}
	
	public ArrayList<ACComponentNode> getEquivalences()
	{
		return equivalences;
	}
	
	/**
	 * Check if the given String is the name of a submodule.
	 * @param name the name to check
	 * @return true if there exists a submodule with the given name,
	 * otherwise false.
	 */
	public boolean checkSubmoduleName(String name)
	{
		return submoduleNames.contains(name);
	}
	
	/**
	 * Return the name of the Module.
	 * @return the name of the Module
	 */
	@Override
	public String toString()
	{
		return name;
	}
}
