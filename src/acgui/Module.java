package acgui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.tree.DefaultMutableTreeNode;

import com.mxgraph.util.mxRectangle;

/**
 * The information for a module.
 * @author T.C. Jones
 * @version July 6, 2012
 */
public class Module implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Module parent;
	private String name;
	private String copasiDatamodelKey;
	private DefaultMutableTreeNode treeNode;
	private Object drawingCell;
	private ArrayList<Module> children;
	private ArrayList<Port> ports;
	private ArrayList<Connection> connections;
	private mxRectangle submoduleBounds;

	/**
	 * Construct a module.
	 */
	public Module()
	{
		parent = null;
		name = "";
		copasiDatamodelKey = "";
		treeNode = null;
		drawingCell = null;
		children = new ArrayList<Module>();
		ports = new ArrayList<Port>();
		connections = new ArrayList<Connection>();
	}

	/**
	 * Construct a module.
	 * @param iName the name of the module
	 */
	public Module(String iName)
	{
		parent = null;
		name = iName;
		copasiDatamodelKey = "";
		treeNode = null;
		drawingCell = null;
		children = new ArrayList<Module>();
		ports = new ArrayList<Port>();
		connections = new ArrayList<Connection>();
	}

	/**
	 * Construct a module.
	 * @param iName the name of the module
	 * @param iKey the key of the copasi datamodel
	 */
	public Module(String iName, String iKey)
	{
		parent = null;
		name = iName;
		copasiDatamodelKey = iKey;
		treeNode = null;
		drawingCell = null;
		children = new ArrayList<Module>();
		ports = new ArrayList<Port>();
		connections = new ArrayList<Connection>();
	}
	
	/**
	 * Construct a module.
	 * @param iName the name of the module
	 * @param iParent the parent to the module
	 */
	public Module(String iName, Module iParent)
	{
		parent = iParent;
		name = iName;
		copasiDatamodelKey = "";
		treeNode = null;
		drawingCell = null;
		children = new ArrayList<Module>();
		ports = new ArrayList<Port>();
		connections = new ArrayList<Connection>();
	}
	
	/**
	 * Construct a module.
	 * @param iName the name of the module
	 * @param tNode the tree node representing the module
	 * @param dCell the drawn object representing the module
	 */
	public Module(String iName, DefaultMutableTreeNode tNode, Object dCell)
	{
		name = iName;
		copasiDatamodelKey = "";
		treeNode = tNode;
		drawingCell = dCell;
		children = new ArrayList<Module>();
		ports = new ArrayList<Port>();
		connections = new ArrayList<Connection>();
	}

	/**
	 * Set the name of the module.
	 * @param iName the name of the module
	 */
	public void setName(String iName)
	{
		name = iName;
		AC_GUI.drawingBoard.updateCellName(drawingCell, name);
	}

	/**
	 * Get the name of the module.
	 * @return the name of the module
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Set the Copasi datamodel key of the module.
	 * @param iKey the Copasi datamodel key of the module
	 */
	public void setKey(String iKey)
	{
		copasiDatamodelKey = iKey;
	}

	/**
	 * Get the Copasi datamodel key of the module.
	 * @return the Copasi datamodel key of the module
	 */
	public String getKey()
	{
		return copasiDatamodelKey;
	}
	
	/**
	 * Set the drawn object representing the module.
	 * @param dCell the drawn object representing the module
	 */
	public void setDrawingCell(Object dCell)
	{
		drawingCell = dCell;
		AC_GUI.drawingBoard.updateCellName(drawingCell, name);
	}

	/**
	 * Get the drawn object representing the module.
	 * @return the drawn object representing the module
	 */
	public Object getDrawingCell()
	{
		return drawingCell;
	}

	/**
	 * Set the tree node representing the module.
	 * @param tNode the tree node representing the module
	 */
	public void setTreeNode(DefaultMutableTreeNode tNode)
	{
		treeNode = tNode;
	}

	/**
	 * Get the tree node representing the module.
	 * @return the tree node representing the module
	 */
	public DefaultMutableTreeNode getTreeNode()
	{
		return treeNode;
	}
	
	/**
	 * Get the parent of the module.
	 * @return the parent of the module
	 */
	public Module getParent()
	{
		return parent;
	}
	
	/**
	 * Add the given module as a child of the current module.
	 * @param mod the module to add as a child
	 */
	public void addChild(Module mod)
	{
		children.add(mod);
		AC_GUI.masterModuleList.add(mod);
	}
	
	/**
	 * Remove the given module from the list of children.
	 * @param mod the module to be removed
	 */
	public void removeChild(Module mod)
	{
		children.remove(mod);
		AC_GUI.masterModuleList.remove(mod);
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
	public void addPort(Port port)
	{
		ports.add(port);
	}
	
	/**
	 * Remove the given port from the list of ports.
	 * @param port the port to be removed
	 */
	public void removePort(Port port)
	{
		ports.remove(port);
		if(port.getRowIndex() != ports.size())
		{
			//the deleted Port did not occupy the last row in the Port table,
			//the indices of the Ports listed below it must be updated.
			updatePortIndices(port);
		}
	}
	
	/**
	 * Return the list of ports.
	 * @return the list of ports
	 */
	public ArrayList<Port> getPorts()
	{
		return ports;
	}
	
	/**
	 * Add the given connection to the list of connections.
	 * @param connection the connection to add
	 */
	public void addConnection(Connection connection)
	{
		connections.add(connection);
	}
	
	/**
	 * Remove the given connection from the list of connections.
	 * @param connection the connection to be removed
	 */
	public void removeConnection(Connection connection)
	{
		connections.remove(connection);
	}
	
	/**
	 * Return the list of connections.
	 * @return the list of connections
	 */
	public ArrayList<Connection> getConnections()
	{
		return connections;
	}
	
	/**
	 * Set the location for the submodule representation.
	 * @param bounds the location of the submodule representation
	 */
	public void setSubmoduleBounds(mxRectangle bounds)
	{
		submoduleBounds = bounds;
	}
	
	/**
	 * Return the submodule bounds.
	 * @return the submodule bounds
	 */
	public mxRectangle getSubmoduleBounds()
	{
		return submoduleBounds;
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
	
	/**
	 * Update the indices of the Ports listed after the given port.
	 * @param deletedPort the Port that was deleted
	 */
	private void updatePortIndices(Port deletedPort)
	{		
		int index = deletedPort.getRowIndex();
		
		//create a list iterator that starts at the index of the deleted Port
		ListIterator<Port> portsIterator = ports.listIterator(index);
		//reset the index of each Port that occur after the deleted Port
		while(portsIterator.hasNext())
		{
			portsIterator.next().setRowIndex(index);
			index++;
		}
	}
}
