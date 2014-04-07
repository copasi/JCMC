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
	private mxCell drawingCell;
	private mxGeometry drawingCellGeometry;
	private String drawingCellStyle;
	
	public ACComponentNode(Module iParent)
	{
		parent = iParent;
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
	
	public ACComponentNode(Module iParent, mxCell iCell, mxGeometry iGeo)
	{
		parent = iParent;
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
