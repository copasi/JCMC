package acgui;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

/**
 * @author Thomas
 *
 */
public class EquivalenceNode extends ACComponentNode
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private EquivalenceDefinition definition;
	
	/**
	 * @param iParent
	 */
	public EquivalenceNode(Module iParent)
	{
		super(iParent);
	}
	
	/**
	 * @param iParent
	 */
	public EquivalenceNode(Module iParent, EquivalenceDefinition iDefinition)
	{
		super(iParent);
		definition = iDefinition;
	}
	
	/**
	 * @param iParent
	 * @param iCell
	 * @param iGeo
	 */
	public EquivalenceNode(Module iParent, mxCell iCell, mxGeometry iGeo)
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
	public EquivalenceNode(Module iParent, mxCell iCell, mxGeometry iGeo, String iStyle)
	{
		super(iParent, iCell, iGeo, iStyle);
		// TODO Auto-generated constructor stub
	}

	public void setEquivalenceDefinition(EquivalenceDefinition iDef)
	{
		definition = iDef;
	}
	
	public EquivalenceDefinition getEquivalenceDefinition()
	{
		return definition;
	}
	
	@Override
	public String toString()
	{
		return definition.getRefName();
	}
}
