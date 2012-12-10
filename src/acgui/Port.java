package acgui;

import java.io.Serializable;

/**
 * The information for a port of a module.
 * @author T.C. Jones
 * @version September 12, 2012
 */
public class Port implements Serializable 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Module parent;
	//private String type;
	private PortType type;
	private String name;
	private Object drawingCell;
	
	/**
	 * Construct the port object.
	 * @param iParent the module where the port is located
	 * @param iType the type of port
	 * @param iName the name of the port
	 */
	public Port(Module iParent, PortType iType, String iName)
	{
		parent = iParent;
		type = iType;
		name = iName;
	}
	
	/**
	 * Return the parent of the port.
	 * @return the parent of the port
	 */
	public Module getParent()
	{
		return parent;
	}
	
	/**
	 * Sets the drawing cell representation of the port.
	 * @param dCell the drawing cell representation of the port
	 */
	public void setDrawingCell(Object dCell)
	{
		drawingCell = dCell;
	}
	
	/**
	 * Return the drawing cell representation of the port.
	 * @return the drawing cell representation of the port
	 */
	public Object getDrawingCell()
	{
		return drawingCell;
	}
	
	/**
	 * Return the type of port.
	 * @return the type of port
	 */
	public PortType getType()
	{
		return type;
	}
	
	/**
	 * Set the port type.
	 * @param newType the type of port
	 */
	public void setType(PortType newType)
	{
		type = newType;
	}
	
	/**
	 * Return the name of the port.
	 * @return the name of the port
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Return the string representation of the port.
	 * @return the string representation of the port
	 */
	public String toString()
	{
		return name;
	}
}
