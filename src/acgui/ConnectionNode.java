package acgui;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

/**
 * @author Thomas
 *
 */
public class ConnectionNode extends ACComponentNode
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ConnectionDefinition definition;
	
	/**
	 * @param iParent
	 */
	public ConnectionNode(Module iParent)
	{
		super(iParent);
	}
	
	/**
	 * @param iParent
	 */
	public ConnectionNode(Module iParent, ConnectionDefinition iDefinition)
	{
		super(iParent);
		definition = iDefinition;
	}
	
	/**
	 * @param iParent
	 * @param iCell
	 */
	public ConnectionNode(Module iParent, ConnectionDefinition iDefinition, mxCell iCell)
	{
		super(iParent, iCell);
		definition = iDefinition;
	}
	
	/**
	 * @param iParent
	 * @param iStyle
	 */
	public ConnectionNode(Module iParent, ConnectionDefinition iDefinition, String iStyle)
	{
		super(iParent, null, null, iStyle);
		definition = iDefinition;
	}
	
	
	/**
	 * @param iParent
	 * @param iCell
	 * @param iGeo
	 * @param iStyle
	 */
	public ConnectionNode(Module iParent, mxCell iCell, mxGeometry iGeo, String iStyle)
	{
		super(iParent, iCell, iGeo, iStyle);
		// TODO Auto-generated constructor stub
	}

	public void setConnectionDefinition(ConnectionDefinition iDef)
	{
		definition = iDef;
	}
	
	public ConnectionDefinition getConnectionDefinition()
	{
		return definition;
	}
}
