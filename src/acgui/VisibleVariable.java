package acgui;

import java.io.Serializable;

import com.mxgraph.util.mxRectangle;

/**
 * A variable made visible so it can be connected to ports.
 * @author T.C. Jones
 * @version March 15, 2013
 */
public class VisibleVariable implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Module parent;
	private String refName;
	private Object drawingCell;
	private mxRectangle drawingCellBounds;
	
	/**
	 * 
	 */
	public VisibleVariable(Module iParent, String iRefName)
	{
		parent = iParent;
		refName = iRefName;
	}

	public void setDrawingCell(Object cell)
	{
		drawingCell = cell;
	}
	
	public Object getDrawingCell()
	{
		return drawingCell;
	}
	
	public String getRefName()
	{
		return refName;
	}
	
	/**
	 * Set the location for the submodule representation.
	 * @param bounds the location of the submodule representation
	 */
	public void setDrawingCellBounds(mxRectangle bounds)
	{
		drawingCellBounds = bounds;
	}
	
	/**
	 * Return the submodule bounds.
	 * @return the submodule bounds
	 */
	public mxRectangle getDrawingCellBounds()
	{
		return drawingCellBounds;
	}
	
	public String toString()
	{
		return refName;
	}
}