package acgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.JPanel;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

/**
 * The drawing board panel in the aggregation connector.
 * @author T.C. Jones
 * @version June 29, 2012
 */
public class DrawingBoard extends JPanel
{
	private static final long serialVersionUID = 1L;

	private final int DEFAULT_MODULE_HEIGHT = 400;
	private final int DEFAULT_MODULE_WIDTH = 350;
	private final int DEFAULT_SUBMODULE_HEIGHT = 100;
	private final int DEFAULT_SUBMODULE_WIDTH = 120;

	private mxGraph graph;
	private Object parent;
	private Module activeModule;
	mxGraphComponent graphComponent;
	ArrayList<Object> cells = new ArrayList<Object>();

	/**
	 * Construct the drawing board.
	 */
	public DrawingBoard()
	{
		graph = new mxGraph();
		this.styleSetup();
		parent = graph.getDefaultParent();
		this.setBackground(Color.WHITE);
		this.setLayout(new BorderLayout());
		this.setVisible(true);
		this.setOpaque(true);
		graphComponent = new mxGraphComponent(graph);
		graphComponent.getGraphHandler().setRemoveCellsFromParent(false);
		graphComponent.getViewport().setOpaque(true);
		graphComponent.getViewport().setBackground(Color.WHITE);
		this.add(graphComponent);
		graph.setConstrainChildren(true);
		installSelectionListener();
	}

	/**
	 * Create the first containment cell.
	 * @param mod the module to be represented
	 */
	public void setup(Module mod)
	{
		Object v1;
		graph.getModel().beginUpdate();
		try
		{
			// int width = graphComponent.getViewport().getWidth() - 10;
			// int height = graphComponent.getViewport().getHeight() - 10;
			double width = graphComponent.getVisibleRect().getWidth() - 50;
			double height = graphComponent.getVisibleRect().getHeight() - 50;
			// v1 = graph.insertVertex(parent, null, mod, 10, 10, 600, 600,
			// "defaultVertex;fillColor=white;strokeColor=blue;strokeWidth=5.0");
			v1 = graph.insertVertex(parent, null, mod, 25, 25, width, height, "Module");
			// graphModel.add(null, v1, 0);
			/*
			 * mxGeometry geometry = new mxGeometry(25, 25, width, height); mxCell root = new mxCell(mod, geometry,
			 * "defaultVertex;fillColor=white;strokeColor=blue;strokeWidth=5.0"); graphModel.setRoot(root);
			 */
			//cells.add(v1);
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		mod.setDrawingCell(v1);
		activeModule = mod;
		/*
		 * System.out.println("v1: " + v1.toString()); 
		 * System.out.println("Graph Model root: " + graph.getModel().getRoot()); 
		 * System.out.println("Graph Model contains v1? " + graph.getModel().contains(v1));
		 */
	}

	/**
	 * Create a drawing cell representation of the module.
	 * @param mod the module to be represented
	 */
	public void createCell(Module mod)
	{
		Object parentCell;
		// Determine what the parentCell will be
		if (mod.getParent() != null)
		{
			// The module's parent's drawing cell will be the parentCell
			parentCell = mod.getParent().getDrawingCell();
		}
		else
		{
			// The module has no parent, the default parent will be the parentCell
			parentCell = parent;
		}

		// Create the cell
		Object cell = graph.createVertex(parentCell, null, mod, 5, 5, 10, 10, "");
		((mxCell)cell).setConnectable(false);
		// Assign the created cell to the module
		mod.setDrawingCell(cell);
	}

	/**
	 * Add the drawing cell representation of the given module to the module's parent.
	 * @param mod the module to add
	 */
	public void addCell(Module mod)
	{
		Object parentCell = mod.getParent().getDrawingCell();
		Object childCell = mod.getDrawingCell();
		int childCount = mod.getParent().getChildren().size();
		mxRectangle bounds = new mxRectangle();
		double xPosition;
		double yPosition;

		graph.getModel().beginUpdate();
		try
		{
			if (!graph.getModel().isVisible(childCell))
			{
				//System.out.println("Submodule not visible.");
				graph.getModel().setVisible(childCell, true);
			}
			graph.getModel().add(parentCell, childCell, 0);
			xPosition = 40 + (childCount * 20);
			yPosition = 40 + (childCount * 20);
			bounds.setX(xPosition);
			bounds.setY(yPosition);
			bounds.setWidth(DEFAULT_SUBMODULE_WIDTH);
			bounds.setHeight(DEFAULT_SUBMODULE_HEIGHT);
			graph.resizeCell(childCell, bounds);
			graph.getModel().setStyle(childCell, "Submodule");
		}
		finally
		{
			graph.getModel().endUpdate();
		}
	}

	/**
	 * Remove the given drawing cell, and its children, from the drawing board.
	 * @param cell the drawing cell to be removed
	 */
	public void removeCell(Object cell)
	{
		graph.getModel().beginUpdate();
		try
		{
			//graph.getModel().remove(cell);
			//System.out.println("Parent " + parentCell + " child count: "
			//		+ graph.getModel().getChildCount(parentCell));
			/*
			if (graph.getModel().getChildCount(cell) > 0)
			{
				makeChildrenInvisible(cell);
			}
			*/
			//graph.getModel().setVisible(cell, false);
			graph.getModel().remove(cell);
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		//cells.remove(cell);
	}

	/**
	 * Update the name of the given cell.
	 * @param cell the cell to change the name
	 * @param newName the new name
	 */
	public void updateCellName(Object cell, String newName)
	{
		graph.cellLabelChanged(cell, newName, false);
	}

	/**
	 * Select the given cell on the drawing board.
	 * @param cell the cell to be selected
	 */
	public void setSelected(Object cell)
	{
		graph.setSelectionCell(cell);
	}

	/**
	 * Return the current loaded module.
	 * @return the current loaded module
	 */
	public Module getActiveModule()
	{
		return activeModule;
	}

	/**
	 * Change the current module displayed.
	 * @param mod the new module to display
	 */
	public void changeModule(Module mod)
	{
		double width = graphComponent.getVisibleRect().getWidth() - 50;
		double height = graphComponent.getVisibleRect().getHeight() - 50;
		mxRectangle bounds = new mxRectangle(25, 25, width, height);
		Object cell = mod.getDrawingCell();

		makeAllModulesInvisible();
		graph.getModel().beginUpdate();
		try
		{
			if (!graph.getModel().isVisible(cell))
			{
				//System.out.println("Module not visible.");
				graph.getModel().setVisible(cell, true);
			}
			graph.getModel().add(parent, cell, 0);
			graph.resizeCell(cell, bounds);
			graph.getModel().setStyle(cell, "Module");
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		activeModule = mod;
		drawChildren();
	}

	/**
	 * Draw the children of the currentModule.
	 */
	private void drawChildren()
	{
		ListIterator<Module> children = activeModule.getChildren().listIterator();
		Object parentCell = activeModule.getDrawingCell();
		Object childCell;
		mxRectangle bounds = new mxRectangle();
		double xPosition;
		double yPosition;
		int childIndex;

		while (children.hasNext())
		{
			childIndex = children.nextIndex();
			childCell = children.next().getDrawingCell();
			graph.getModel().beginUpdate();
			try
			{
				if (!graph.getModel().isVisible(childCell))
				{
					graph.getModel().setVisible(childCell, true);
				}
				graph.getModel().add(parentCell, childCell, 0);
				xPosition = 40 + (childIndex * 20);
				yPosition = 40 + (childIndex * 20);
				bounds.setX(xPosition);
				bounds.setY(yPosition);
				bounds.setWidth(DEFAULT_SUBMODULE_WIDTH);
				bounds.setHeight(DEFAULT_SUBMODULE_HEIGHT);
				graph.resizeCell(childCell, bounds);
				graph.getModel().setStyle(childCell, "Submodule");
			}
			finally
			{
				graph.getModel().endUpdate();
			}
		}
	}

	/**
	 * Make all the modules invisible.
	 */
	private void makeAllModulesInvisible()
	{
		ListIterator<Module> iterator = AC_GUI.masterModuleList.getListIterator();

		while (iterator.hasNext())
		{
			graph.getModel().beginUpdate();
			try
			{
				graph.getModel().setVisible(iterator.next().getDrawingCell(), false);
			}
			finally
			{
				graph.getModel().endUpdate();
			}
		}
	}

	/**
	 * Make the children of the given cell invisible.
	 * @param cell the cell whose children will be made invisible
	 */
	private void makeChildrenInvisible(Object cell)
	{
		int childCount = graph.getModel().getChildCount(cell);
		Object currentChild = null;

		for (int i = 0; i < childCount; i++)
		{
			graph.getModel().beginUpdate();
			try
			{
				currentChild = graph.getModel().getChildAt(cell, i);
				graph.getModel().setVisible(currentChild, false);
			}
			finally
			{
				graph.getModel().endUpdate();
			}
		}
	}

	/**
	 * Add various display properties to the graph stylesheet.
	 */
	private void styleSetup()
	{
		mxStylesheet styleSheet = graph.getStylesheet();
		Map<String, Object> cell = new HashMap<String, Object>();

		cell.put(mxConstants.STYLE_STROKECOLOR, "blue");
		cell.put(mxConstants.STYLE_FILLCOLOR, "white");
		cell.put(mxConstants.STYLE_STROKEWIDTH, "3.0");
		cell.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
		cell.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
		cell.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_MIDDLE);
		cell.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "white");
		cell.put(mxConstants.STYLE_LABEL_BORDERCOLOR, "red");
		cell.put(mxConstants.STYLE_SPACING_TOP, "-10");
		cell.put(mxConstants.STYLE_FONTFAMILY, "Times New Roman");
		cell.put(mxConstants.STYLE_FONTSIZE, "12");
		cell.put(mxConstants.STYLE_FONTSTYLE, "1");
		cell.put(mxConstants.STYLE_FONTCOLOR, "black");
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		cell.put(mxConstants.STYLE_ROUNDED, "false");
		cell.put(mxConstants.STYLE_FOLDABLE, "0");

		styleSheet.putCellStyle("Module", cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_STROKECOLOR, "blue");
		cell.put(mxConstants.STYLE_FILLCOLOR, "green");
		cell.put(mxConstants.STYLE_OPACITY, "50.0");
		cell.put(mxConstants.STYLE_STROKEWIDTH, "3.0");
		cell.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
		cell.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
		cell.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_MIDDLE);
		cell.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "white");
		cell.put(mxConstants.STYLE_LABEL_BORDERCOLOR, "red");
		cell.put(mxConstants.STYLE_SPACING_TOP, "-10");
		cell.put(mxConstants.STYLE_FONTFAMILY, "Times New Roman");
		cell.put(mxConstants.STYLE_FONTSIZE, "12");
		cell.put(mxConstants.STYLE_FONTSTYLE, "1");
		cell.put(mxConstants.STYLE_FONTCOLOR, "black");
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		cell.put(mxConstants.STYLE_ROUNDED, "true");
		cell.put(mxConstants.STYLE_FOLDABLE, "0");

		styleSheet.putCellStyle("Submodule", cell);
	}

	/**
	 * When a cell is selected the corresponding tree node is also highlighted.
	 */
	private void installSelectionListener()
	{
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e)
			{
				Object cell = graphComponent.getCellAt(e.getX(), e.getY());

				if (cell != null)
				{
					//System.out.println("cell="+graph.getLabel(cell));
					//Module mod = AC_GUI.moduleList.findModule(cell);
					//AC_GUI.treeView.setSelected(mod.getTreeNode());
					AC_GUI.currentGUI.setSelectedModule(cell);
				}
			}
		});
	}
}
