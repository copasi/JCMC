package acgui;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

/**
 * @author Thomas
 *
 */
public class PortNode extends ACComponentNode
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PortDefinition definition;
	
	/**
	 * @param iParent
	 */
	public PortNode(Module iParent)
	{
		super(iParent, null, null);
	}
	
	/**
	 * @param iParent
	 */
	public PortNode(Module iParent, PortDefinition iDefinition)
	{
		super(iParent, null, null);
		definition = iDefinition;
	}
	
	/**
	 * @param iParent
	 * @param iCell
	 * @param iGeo
	 */
	public PortNode(Module iParent, mxCell iCell, mxGeometry iGeo)
	{
		super(iParent, iCell, iGeo);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param iParent
	 * @param iCell
	 * @param iGeo
	 * @param iStyle
	 */
	public PortNode(Module iParent, mxCell iCell, mxGeometry iGeo, String iStyle)
	{
		super(iParent, iCell, iGeo, iStyle);
		// TODO Auto-generated constructor stub
	}

	public void setPortDefinition(PortDefinition iDef)
	{
		definition = iDef;
	}
	
	public PortDefinition getPortDefinition()
	{
		return definition;
	}
	
	@Override
	public String toString()
	{
		return definition.getName();
	}
}
