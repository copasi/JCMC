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
	Module parent;
	Object source;
	Object target;
	Object drawingCell;
	
	/**
	 * Construct the object.
	 */
	public Connection()
	{
		parent = null;
		source = null;
		target = null;
		drawingCell = null;
	}
	
	/**
	 * Construct the object with the initial given values.
	 * @param iSource the source port
	 * @param iTarget the target port
	 * @param cell the drawing cell
	 */
	public Connection(Module iParent, Object cell)
	{
		parent = iParent;
		source = ((mxCell)cell).getSource();
		target = ((mxCell)cell).getTarget();
		drawingCell = cell;
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
	
	/**
	 * Return the source port.
	 * @return the source port
	 */
	public Object getSource()
	{
		return source;
	}
	
	/**
	 * Return the target port.
	 * @return the target port
	 */
	public Object getTarget()
	{
		return target;
	}
}
