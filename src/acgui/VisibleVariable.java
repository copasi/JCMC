package acgui;

import java.io.Serializable;

import com.mxgraph.model.mxGeometry;
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
	private VariableType variableType;
	private Object drawingCell;
	private mxRectangle drawingCellBounds;
	private mxGeometry drawingCellGeometry;
	
	/**
	 * 
	 */
	public VisibleVariable(Module iParent, String iRefName, VariableType vType)
	{
		parent = iParent;
		refName = iRefName;
		variableType = vType;
	}
	
	public VisibleVariable(Module iParent, String iRefName, Object iDrawingCell, VariableType vType)
	{
		parent = iParent;
		refName = iRefName;
		drawingCell = iDrawingCell;
		variableType = vType;
	}
	
	public VisibleVariable(Module iParent, String iRefName, Object iDrawingCell, mxRectangle iCellBounds, mxGeometry iCellGeo, VariableType vType)
	{
		parent = iParent;
		refName = iRefName;
		drawingCell = iDrawingCell;
		drawingCellBounds = iCellBounds;
		drawingCellGeometry = iCellGeo;
		variableType = vType;
	}

	public Module getParent()
	{
		return parent;
	}
	
	public void setDrawingCell(Object cell)
	{
		drawingCell = cell;
	}
	
	public Object getDrawingCell()
	{
		return drawingCell;
	}
	
	public void setRefName(String iRefName)
	{
		refName = iRefName;
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
	
	public void setDrawingCellGeometry(mxGeometry geo)
	{
		drawingCellGeometry = geo;
	}
	
	public mxGeometry getDrawingCellGeometry()
	{
		return drawingCellGeometry;
	}
	
	public VariableType getVariableType()
	{
		return variableType;
	}
	
	public void setVariableType(VariableType varType)
	{
		variableType = varType;
	}
	
	public String toString()
	{
		return refName;
	}
}
