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
		super(iParent, iDefinition);
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

	@Override
	public String toString()
	{
		return this.getDefinition().getRefName();
	}
}
