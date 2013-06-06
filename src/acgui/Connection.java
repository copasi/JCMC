package acgui;

import java.io.Serializable;

import com.mxgraph.model.mxCell;

/**
 * A connection between two ports.
 * @author T.C. Jones
 * @version November 27, 2012
 */
public class Connection implements Serializable 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Module parent;
	private Object drawingCell;
	private String drawingCellStyle;
	
	/**
	 * Construct the object.
	 */
	public Connection()
	{
		parent = null;
		drawingCell = null;
		drawingCellStyle = "";
	}
	
	/**
	 * Construct the object with the initial given value.
	 * @param iParent the parent module
	 */
	public Connection(Module iParent)
	{
		parent = iParent;
		drawingCell = null;
		drawingCellStyle = "";
	}
	
	/**
	 * Construct the object with the initial given values.
	 * @param iParent the parent module
	 * @param cell the drawing cell
	 */
	public Connection(Module iParent, Object cell)
	{
		parent = iParent;
		drawingCell = cell;
		drawingCellStyle = ((mxCell)cell).getStyle();
	}
	
	/**
	 * Return the parent module of the connection.
	 * @return the parent module of the connection
	 */
	public Module getParent()
	{
		return parent;
	}
	
	/**
	 * Set the drawing cell.
	 * @param cell the drawing cell
	 */
	public void setDrawingCell(Object cell)
	{
		drawingCell = cell;
	}
	
	/**
	 * Return the drawing cell.
	 * @return the drawing cell
	 */
	public Object getDrawingCell()
	{
		return drawingCell;
	}
	
	public void setDrawingCellStyle(String style)
	{
		drawingCellStyle = style;
	}
	
	public String getDrawingCellStyle()
	{
		return drawingCellStyle;
	}
	
	/**
	 * Return the source port.
	 * @return the source port
	 */
	public Object getSource()
	{
		return ((mxCell)drawingCell).getSource();
	}
	
	/**
	 * Return the target port.
	 * @return the target port
	 */
	public Object getTarget()
	{
		return ((mxCell)drawingCell).getTarget();
	}
}
