package acgui;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * The information for a module.
 * @author T.C. Jones
 * @version July 6, 2012
 */
public class Module
{
	private String name;
	private DefaultMutableTreeNode treeNode;
	private Object drawingCell;
	
	public Module()
	{
		name = "";
		treeNode = null;
		drawingCell = null;
	}
	
	public Module(String iName)
	{
		name = iName;
		treeNode = null;
		drawingCell = null;
	}
	
	public Module(String iName, DefaultMutableTreeNode tNode, Object dCell)
	{
		name = iName;
		treeNode = tNode;
		drawingCell = dCell;
	}
	
	public void setName(String iName)
	{
		name = iName;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setDrawingCell(Object dCell)
	{
		drawingCell = dCell;
	}
	
	public Object getDrawingCell()
	{
		return drawingCell;
	}
	
	public void setTreeNode(DefaultMutableTreeNode tNode)
	{
		treeNode = tNode;
	}
	
	public DefaultMutableTreeNode getTreeNode()
	{
		return treeNode;
	}
}
