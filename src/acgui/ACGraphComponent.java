/**
 * 
 */
package acgui;

import java.util.EventObject;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.view.mxGraph;

/**
 * Extension of the mxGraphComponent class.
 * @author T.C. Jones
 */
public class ACGraphComponent extends mxGraphComponent
{

	/**
	 * Construct the object.
	 * @param arg0
	 */
	public ACGraphComponent(mxGraph arg0)
	{
		super(arg0);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Sets the label of the specified cell to the given value using
	 * mxGraph.cellLabelChanged and fires mxEvent.LABEL_CHANGED while the
	 * transaction is in progress. Returns the cell whose label was changed.
	 * 
	 * @param cell
	 *            Cell whose label should be changed.
	 * @param value
	 *            New value of the label.
	 * @param evt
	 *            Optional event that triggered the change.
	 */
	public Object labelChanged(Object cell, Object value, EventObject evt)
	{
		mxIGraphModel model = graph.getModel();

		if (((mxCell) cell).getValue() instanceof Module)
		{
			String newName = (String) value;
			Module mod = (Module) ((mxCell)cell).getValue();
			
			model.beginUpdate();
			try
			{
				AC_GUI.changeModuleName(mod, newName, false);
				graph.cellLabelChanged(cell, mod, graph.isAutoSizeCell(cell));
				eventSource.fireEvent(new mxEventObject(mxEvent.LABEL_CHANGED,"cell", cell, "value", value, "event", evt));
			}
			finally
			{
				model.endUpdate();
			}
		}

		return cell;
	}
}
