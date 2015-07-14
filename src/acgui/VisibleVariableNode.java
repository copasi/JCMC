/**
 * 
 */
package acgui;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

/**
 * @author Thomas
 *
 */
public class VisibleVariableNode extends ACComponentNode
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param iParent
	 */
	public VisibleVariableNode(Module iParent)
	{
		super(iParent);
	}
	
	/**
	 * @param iParent
	 * @param iCell
	 * @param iGeo
	 */
	public VisibleVariableNode(Module iParent, VisibleVariableDefinition iDefinition)
	{
		super(iParent, iDefinition);
	}
	
	/**
	 * @param iParent
	 * @param iCell
	 * @param iGeo
	 */
	public VisibleVariableNode(Module iParent, VisibleVariableDefinition iDefinition, mxCell iCell, mxGeometry iGeo)
	{
		super(iParent, iDefinition, iCell, iGeo);
	}

	/**
	 * @param iParent
	 * @param iCell
	 * @param iGeo
	 * @param iStyle
	 */
	public VisibleVariableNode(Module iParent, mxCell iCell, mxGeometry iGeo, String iStyle)
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
