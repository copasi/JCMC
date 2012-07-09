package acgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JPanel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
/**
 * The drawing board panel in the aggregation connector.
 * @author T.C. Jones
 * @version June 29, 2012
 */
public class DrawingBoard extends JPanel
{
	private static final long serialVersionUID = 1L;

	private mxGraph graph;
	private Object parent;
	mxGraphComponent graphComponent;
	ArrayList<Object> cells = new ArrayList<Object>();
	
	public DrawingBoard()
	{
		graph = new mxGraph();
		parent = graph.getDefaultParent();
		this.setBackground(Color.WHITE);
		this.setLayout(new BorderLayout());
		this.setVisible(true);
		this.setOpaque(true);
		graphComponent = new mxGraphComponent(graph);
		graphComponent.getViewport().setOpaque(true);
		graphComponent.getViewport().setBackground(Color.WHITE);
		this.add(graphComponent);
	}
	
	public Object newModel(String name)
	{
		int count = AC_GUI.moduleList.getSize();
		
		Object v1;
		graph.getModel().beginUpdate();
		try
		{
			v1 = graph.insertVertex(parent, null, name, 20+(count*20), 20+(count*20), 100, 100);
			cells.add(v1);
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		return v1;
		//System.out.println("Cell style: " + graph.getModel().getStyle(cells.get(0)));

		//graphComponent = new mxGraphComponent(graph);
		//this.add(graphComponent);
	}
	
	public void removeModel(Object cell)
	{
		Object toDelete[] = new Object[1];
		toDelete[0] = cell;
		graph.getModel().beginUpdate();
		try
		{
			graph.removeCells(toDelete);
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		cells.remove(cell);
	}
}
