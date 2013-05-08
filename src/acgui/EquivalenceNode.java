/**
 * 
 */
package acgui;

import java.io.Serializable;

import com.mxgraph.model.mxGeometry;

/**
 * An equivalence node.
 * @author T.C. Jones
 * @version April 23, 2013
 */
public class EquivalenceNode implements Serializable
{

	private Module parent;
	private String refName;
	private Object drawingCell;
	private mxGeometry drawingCellGeometry;
	
	private static final long serialVersionUID = 1L;

	/**
	 * Construct the Equivalence Node.
	 */
	public EquivalenceNode(Module iParent, String iName)
	{
		parent = iParent;
		refName = iName;
		drawingCell = null;
		drawingCellGeometry = null;
	}
	
	/**
	 * Construct the Equivalence Node.
	 */
	public EquivalenceNode(Module iParent, String iName, Object iCell)
	{
		parent = iParent;
		refName = iName;
		drawingCell = iCell;
		drawingCellGeometry = null;
	}
	
	public Module getParent()
	{
		return parent;
	}
	
	public void setRefName(String iName)
	{
		refName = iName;
	}
	
	public String getRefName()
	{
		return refName;
	}
	
	public void setDrawingCell(Object iCell)
	{
		drawingCell = iCell;
	}
	
	public Object getDrawingCell()
	{
		return drawingCell;
	}

	public void setDrawingCellGeometry(mxGeometry geo)
	{
		drawingCellGeometry = geo;
	}
	
	public mxGeometry getDrawingCellGeometry()
	{
		return drawingCellGeometry;
	}
	
	public String toString()
	{
		return refName;
	}
}
