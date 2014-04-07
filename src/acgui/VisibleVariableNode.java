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
	private VisibleVariableDefinition definition;
	
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
		super(iParent);
		definition = iDefinition;
	}
	
	/**
	 * @param iParent
	 * @param iCell
	 * @param iGeo
	 */
	public VisibleVariableNode(Module iParent, VisibleVariableDefinition iDefinition, mxCell iCell, mxGeometry iGeo)
	{
		super(iParent, iCell, iGeo);
		definition = iDefinition;
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

	public void setVisibleVariableDefinition(VisibleVariableDefinition iDef)
	{
		definition = iDef;
	}
	
	public VisibleVariableDefinition getVisibleVariableDefinition()
	{
		return definition;
	}
	
	@Override
	public String toString()
	{
		return definition.getRefName();
	}
}
