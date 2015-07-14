package acgui;

import java.io.Serializable;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

/**
 * @author Thomas
 *
 */
public class ACComponentNode implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Module parent;
	private ACComponentDefinition definition;
	private mxCell drawingCell;
	private mxGeometry drawingCellGeometry;
	private String drawingCellStyle;
	
	public ACComponentNode(Module iParent)
	{
		parent = iParent;
		definition = null;
		drawingCell = null;
		drawingCellGeometry = null;
		drawingCellStyle = null;
	}
	
	public ACComponentNode(Module iParent, ACComponentDefinition iDefinition)
	{
		parent = iParent;
		definition = iDefinition;
		drawingCell = null;
		drawingCellGeometry = null;
		drawingCellStyle = null;
	}
	
	public ACComponentNode(Module iParent, mxCell iCell)
	{
		parent = iParent;
		drawingCell = iCell;
		drawingCellGeometry = null;
		drawingCellStyle = null;
	}
	
	public ACComponentNode(Module iParent, ACComponentDefinition iDefinition, mxCell iCell, mxGeometry iGeo)
	{
		parent = iParent;
		definition = iDefinition;
		drawingCell = iCell;
		drawingCellGeometry = iGeo;
		drawingCellStyle = null;
	}
	
	public ACComponentNode(Module iParent, mxCell iCell, mxGeometry iGeo, String iStyle)
	{
		parent = iParent;
		drawingCell = iCell;
		drawingCellGeometry = iGeo;
		drawingCellStyle = iStyle;
	}
	
	public Module getParent()
	{
		return parent;
	}
	
	public void setDefinition(ACComponentDefinition iDefinition)
	{
		definition = iDefinition;
	}
	
	public ACComponentDefinition getDefinition()
	{
		return definition;
	}
	
	public void setDrawingCell(mxCell iCell)
	{
		drawingCell = iCell;
	}
	
	public mxCell getDrawingCell()
	{
		return drawingCell;
	}
	
	public void setDrawingCellGeometry(mxGeometry iGeo)
	{
		drawingCellGeometry = iGeo;
	}
	
	public mxGeometry getDrawingCellGeometry()
	{
		return drawingCellGeometry;
	}
	
	public void setDrawingCellStyle(String iStyle)
	{
		drawingCellStyle = iStyle;
	}
	
	public String getDrawingCellStyle()
	{
		return drawingCellStyle;
	}
}
