package acgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxStylesheet;

/**
 * The drawing board panel in the aggregation connector.
 * 
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
	private final int DEFAULT_PORT_HEIGHT = 60;
	private final int DEFAULT_PORT_WIDTH = 60;
	private final int DEFAULT_VISIBLEVARIABLE_HEIGHT = 50;
	private final int DEFAULT_VISIBLEVARIABLE_WIDTH = 50;
	private final int DEFAULT_AGGREGATOR_HEIGHT = 80;
	private final int DEFAULT_AGGREGATOR_WIDTH = 90;
	private final int DEFAULT_EQUIVALENCENODE_HEIGHT = 60;
	private final int DEFAULT_EQUIVALENCENODE_WIDTH = 60;
	
	private ACGraph graph;
	private Object parent;
	private Module activeModule;
	mxGraphComponent graphComponent;
	ArrayList<Object> cells = new ArrayList<Object>();

	/**
	 * Construct the drawing board.
	 */
	public DrawingBoard()
	{
		graph = new ACGraph();
		this.styleSetup();
		graph.setDropEnabled(false);
		parent = graph.getDefaultParent();
		this.setBackground(Color.WHITE);
		this.setLayout(new BorderLayout());
		this.setVisible(true);
		this.setOpaque(true);
		graphComponent = new ACGraphComponent(graph);
		graphComponent.getGraphHandler().setRemoveCellsFromParent(false);
		graphComponent.getViewport().setOpaque(true);
		graphComponent.getViewport().setBackground(Color.WHITE);
		//new mxKeyboardHandler(graphComponent);
		this.add(graphComponent);
		graph.setConstrainChildren(true);
		graphComponent.setToolTips(true);
		installListeners();
	}

	/**
	 * Create the first containment cell.
	 * 
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
			 * "defaultVertex;fillColor=white;strokeColor=blue;strokeWidth=5.0" ); graphModel.setRoot(root);
			 */
			// cells.add(v1);
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		mod.setDrawingCell(v1);
		activeModule = mod;
		/*
		 * System.out.println("v1: " + v1.toString()); System.out.println("Graph Model root: " +
		 * graph.getModel().getRoot()); System.out.println("Graph Model contains v1? " + graph.getModel().contains(v1));
		 */
	}

	public void clear()
	{
		((mxGraphModel)graph.getModel()).clear();
		parent = graph.getDefaultParent();
	}
	/**
	 * Create a drawing cell representation of the module.
	 * 
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
			// The module has no parent, the default parent will be the
			// parentCell
			parentCell = parent;
		}

		// Create the cell

		Object cell = graph.createVertex(parentCell, null, mod, 5, 5, 1, 1, "");
		((mxCell) cell).setConnectable(false);

		/*
		 * mxGeometry geo = new mxGeometry(5, 5, 10, 10); mxCell cell = null; graph.getModel().beginUpdate(); try { geo
		 * = new mxGeometry(5, 5, 10, 10); cell = new mxCell(mod, geo, ""); //cell.setParent(((mxCell)parentCell));
		 * cell.setConnectable(false); cell.setVisible(true); graph.getModel().add(parent, cell, 0);
		 * //graph.addCell(cell, parentCell); } finally { graph.getModel().endUpdate(); }
		 */
		// Assign the created cell to the module
		Object obj = graph.getModel().getValue(cell);
		mod.setDrawingCell(cell);
		// Object cobj = mod.getDrawingCell();
		// obj = graph.getModel().getValue(cobj);
		// System.out.println("HI");
	}

	/**
	 * Add the drawing cell representation of the given module to the module's parent.
	 * 
	 * @param mod the module to add
	 */
	public void addCell(Module mod)
	{
		Object parentCell = mod.getParent().getDrawingCell();
		Object childCell = mod.getDrawingCell();
		int childCount = mod.getParent().getChildren().size();
		mxRectangle bounds = new mxRectangle();
		mxGeometry geo = new mxGeometry();
		double xPosition;
		double yPosition;

		graph.getModel().beginUpdate();
		try
		{
			if (!graph.getModel().isVisible(childCell))
			{
				// System.out.println("Submodule not visible.");
				graph.getModel().setVisible(childCell, true);
			}
			
			xPosition = 40 + (childCount * 20);
			yPosition = 40 + (childCount * 20);
			geo.setX(xPosition);
			geo.setY(yPosition);
			geo.setWidth(DEFAULT_SUBMODULE_WIDTH);
			geo.setHeight(DEFAULT_SUBMODULE_HEIGHT);
			((mxCell)childCell).setGeometry(geo);
			//graph.resizeCell(childCell, bounds);
			graph.getModel().add(parentCell, childCell, 0);
			graph.getModel().setStyle(childCell, mod.getDrawingCellStyle());
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		//mod.setDrawingCellBounds(bounds);
		mod.setDrawingCellGeometry(geo);
		// printBoardStats();
	}

	public void createPort(Port port, mxGeometry geo)
	{
		mxCell port1 = null;
		mxGeometry geo1 = new mxGeometry(geo.getX(), geo.getY(), geo.getWidth(), geo.getHeight());
		geo1.setRelative(true);

		port1 = new mxCell(port, geo1, "Port");
		port1.setVertex(true);
		port1.setConnectable(true);
		
		port.setDrawingCell(port1);
	}
	
	/**
	 * Add a drawing cell representation of the given port to the 
	 * drawing cell representation of the given module.
	 * 
	 * @param parentMod the parent module of the port
	 * @param port the port to be added
	 */
	public void addPort(Module parentMod, Port port)
	{
		Object cell = parentMod.getDrawingCell();
		double width = DEFAULT_PORT_WIDTH;
		double height = DEFAULT_PORT_HEIGHT;
		// System.out.println("Cell obj: " + ((mxCell) cell).getValue().toString());
		// System.out.println("Parentmod obj: " + parentMod.getName());
		if (cell != activeModule.getDrawingCell())
		{
			width = width / 2;
			height = height / 2;
		}
		double offsetX = 0;
		double offsetY = 0;

		offsetX = -width / 2;
		offsetY = -height / 2;

		mxCell port1 = null;
		graph.getModel().beginUpdate();
		try
		{
			mxGeometry geo1 = new mxGeometry(0, 0.5, width, height);
			geo1.setOffset(new mxPoint(offsetX, offsetY));
			geo1.setRelative(true);

			port1 = new mxCell(port, geo1, "Port");
			port1.setVertex(true);
			port1.setConnectable(true);

			graph.getModel().add(cell, port1, 0);
			// graph.updatePortOrientation(port1, geo1);
			graph.updatePortOrientation(port1, geo1);
		}
		finally
		{
			graph.getModel().endUpdate();
		}

		port.setDrawingCell(port1);
	}

	public void createVisibleVariable(VisibleVariable var)
	{
		Object parentCell = var.getParent().getDrawingCell();
		mxCell var1 = null;
		var1 = (mxCell)graph.createVertex(parentCell, null, var, 5, 5, 10, 10, "");
		//var1 = new mxCell(var);
		var1.setGeometry(var.getDrawingCellGeometry());
		var1.setVertex(true);
		var1.setConnectable(true);
		
		var.setDrawingCell(var1);
	}
	
	public void createEquivalenceNode(EquivalenceNode eNode)
	{
		Object parentCell = eNode.getParent().getDrawingCell();
		mxCell eNodeCell = null;
		eNodeCell = (mxCell)graph.createVertex(parentCell, null, eNode, 5, 5, 10, 10, "");
		eNodeCell.setGeometry(eNode.getDrawingCellGeometry());
		eNodeCell.setVertex(true);
		eNodeCell.setConnectable(true);
		
		eNode.setDrawingCell(eNodeCell);
	}
	
	public void addVisibleVariable(Module parentMod, VisibleVariable var)
	{
		//System.out.println("Time to add a visible variable:");
		//System.out.println("Variable Ref Name: " + var.getRefName());
		Object cell = parentMod.getDrawingCell();
		mxRectangle bounds = new mxRectangle();
		double xPosition;
		double yPosition;
		double width = DEFAULT_VISIBLEVARIABLE_WIDTH;
		double height = DEFAULT_VISIBLEVARIABLE_HEIGHT;
		// System.out.println("Cell obj: " + ((mxCell) cell).getValue().toString());
		// System.out.println("Parentmod obj: " + parentMod.getName());
		
		double offsetX = 0;
		double offsetY = 0;

		//offsetX = -width / 2;
		//offsetY = -height / 2;

		int varIndex = parentMod.getVisibleVariables().size();
		mxCell var1 = null;
		graph.getModel().beginUpdate();
		try
		{
			//mxGeometry geo1 = new mxGeometry(0, 0, width, height);
			//geo1.setOffset(new mxPoint(offsetX, offsetY));
			//geo1.setRelative(true);
			var1 = (mxCell)graph.createVertex(cell, null, var, 5, 5, 10, 10, "");
			var1.setVertex(true);
			var1.setConnectable(true);
			//graph.getModel().add(cell, var1, 0);
			
			
			bounds = new mxRectangle();
			xPosition = 40 + (varIndex * 20);
			yPosition = 40 + (varIndex * 20);
			bounds.setX(xPosition);
			bounds.setY(yPosition);
			bounds.setWidth(DEFAULT_VISIBLEVARIABLE_WIDTH);
			bounds.setHeight(DEFAULT_VISIBLEVARIABLE_HEIGHT);

			mxGeometry geo = new mxGeometry(xPosition, yPosition, DEFAULT_VISIBLEVARIABLE_WIDTH, DEFAULT_VISIBLEVARIABLE_HEIGHT);
			var1.setGeometry(geo);
			graph.getModel().add(cell, var1, 0);
			//graph.resizeCell(var1, bounds);
			graph.getModel().setStyle(var1, "VisibleVariable");
			// graph.updatePortOrientation(port1, geo1);
			//graph.updatePortOrientation(port1, geo1);
		}
		finally
		{
			graph.getModel().endUpdate();
		}

		var.setDrawingCell((Object)var1);
		//var.setDrawingCellBounds(bounds);
		var.setDrawingCellGeometry(var1.getGeometry());
	}
	
	public void addMathAggregator(MathematicalAggregator mathAgg)
	{
		createCell(mathAgg);
		
		Object parentCell = mathAgg.getParent().getDrawingCell();
		Object childCell = mathAgg.getDrawingCell();
		int childCount = mathAgg.getParent().getChildren().size();
		mxRectangle bounds = new mxRectangle();
		mxGeometry geo = new mxGeometry();
		double xPosition;
		double yPosition;
		double width = DEFAULT_AGGREGATOR_WIDTH;
		double height = (20 * (mathAgg.getPorts().size()-1));

		graph.getModel().beginUpdate();
		try
		{
			if (!graph.getModel().isVisible(childCell))
			{
				// System.out.println("Submodule not visible.");
				graph.getModel().setVisible(childCell, true);
			}
			
			xPosition = 40 + (childCount * 20);
			yPosition = 40 + (childCount * 20);
			geo.setX(xPosition);
			geo.setY(yPosition);
			geo.setWidth(width);
			geo.setHeight(height);
			//graph.resizeCell(childCell, bounds);
			((mxCell)childCell).setGeometry(geo);
			graph.getModel().add(parentCell, childCell, 0);
			graph.getModel().setStyle(childCell, mathAgg.getDrawingCellStyle());
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		//mathAgg.setDrawingCellBounds(bounds);
		mathAgg.setDrawingCellGeometry(geo);
		addMathAggregatorPorts(mathAgg);
	}
	
	public void addEquivalenceNode(EquivalenceNode eNode, Object formerEdge)
	{
		Module parentMod = eNode.getParent();
		Object cell = parentMod.getDrawingCell();
		//mxRectangle bounds = new mxRectangle();
		mxGeometry formerGeo = ((mxCell)formerEdge).getGeometry();
		double xPosition;
		double yPosition;
		double width = DEFAULT_EQUIVALENCENODE_WIDTH;
		double height = DEFAULT_EQUIVALENCENODE_HEIGHT;
		double offsetX;
		double offsetY;
		// System.out.println("Cell obj: " + ((mxCell) cell).getValue().toString());
		// System.out.println("Parentmod obj: " + parentMod.getName());
		
		offsetX = -width;
		offsetY = -height;

		int equivIndex = parentMod.getEquivalenceNodes().size();
		mxCell eNodeCell = null;
		graph.getModel().beginUpdate();
		try
		{
			//mxGeometry geo1 = new mxGeometry(0, 0, width, height);
			//geo1.setOffset(new mxPoint(offsetX, offsetY));
			//geo1.setRelative(true);
			eNodeCell = (mxCell)graph.createVertex(cell, null, eNode, 5, 5, 10, 10, "");
			eNodeCell.setVertex(true);
			eNodeCell.setConnectable(true);
			//graph.getModel().add(cell, var1, 0);
			
			
			//bounds = new mxRectangle();
			xPosition = (formerGeo.getSourcePoint().getX() + formerGeo.getTargetPoint().getX()) / 2 - (width/2);
			yPosition = (formerGeo.getSourcePoint().getY() + formerGeo.getTargetPoint().getY()) / 2 - (height/2);
			//bounds.setX(xPosition);
			//bounds.setY(yPosition);
			//bounds.setWidth(DEFAULT_VISIBLEVARIABLE_WIDTH);
			//bounds.setHeight(DEFAULT_VISIBLEVARIABLE_HEIGHT);

			mxGeometry geo = new mxGeometry(xPosition, yPosition, width, height);
			//geo.setOffset(new mxPoint(offsetX, offsetY));
			eNodeCell.setGeometry(geo);
			graph.getModel().add(cell, eNodeCell, 0);
			//graph.resizeCell(var1, bounds);
			graph.getModel().setStyle(eNodeCell, "EquivalenceNode");
			// graph.updatePortOrientation(port1, geo1);
			//graph.updatePortOrientation(port1, geo1);
		}
		finally
		{
			graph.getModel().endUpdate();
		}

		eNode.setDrawingCell((Object)eNodeCell);
		//var.setDrawingCellBounds(bounds);
		eNode.setDrawingCellGeometry(eNodeCell.getGeometry());
	}
	
	public void createConnection(Connection edge, Object source, Object target)
	{
		Object parentCell = edge.getParent().getDrawingCell();
		//Object edgeCell = graph.createEdge(parentCell, null, edge, source, target, "ConnectionEdge");
		Object edgeCell = graph.insertEdge(parentCell, null, edge, source, target, "ConnectionEdge");
		edge.setDrawingCell(edgeCell);
	}
	
	/**
	 * Remove the given drawing cell, and its children, from the drawing board.
	 * 
	 * @param cell the drawing cell to be removed
	 */
	public void removeCell(Object cell)
	{
		graph.getModel().beginUpdate();
		try
		{
			// graph.getModel().remove(cell);
			// System.out.println("Parent " + parentCell + " child count: "
			// + graph.getModel().getChildCount(parentCell));
			/*
			 * if (graph.getModel().getChildCount(cell) > 0) { makeChildrenInvisible(cell); }
			 */
			// graph.getModel().setVisible(cell, false);
			graph.getModel().remove(cell);
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		// cells.remove(cell);
	}
	
	/**
	 * Remove any edges connected to the given drawing cell.
	 * @param cell the drawing cell whose edges will be removed
	 */
	public void removeEdges(Object cell)
	{
		// check if there are any connections to the drawing cell
		int connectionCount = graph.getModel().getEdgeCount(cell);
		if (connectionCount != 0)
		{
			// remove the existing connections from the drawing cell
			Connection edge;
			for(int i = 0; i < connectionCount; i++)
			{
				// get the connection object from the drawing cell
				edge = (Connection)((mxCell)graph.getModel().getEdgeAt(cell, 0)).getValue();
				// remove the drawing cell from the graph
				removeCell(edge.getDrawingCell());
				// remove the connection from the module
				AC_GUI.currentGUI.removeConnection(edge);
			}
		}
	}
	
	/**
	 * Update the name of the given cell.
	 * 
	 * @param cell the cell to change the name
	 * @param newName the new name
	 */
	public void updateCellName(Object cell, String newName)
	{
		// graph.cellLabelChanged(cell, newName, false);
		graph.cellLabelChanged(cell, ((mxCell) cell).getValue(), false);
		/*
		 * graph.getModel().beginUpdate(); try { mxCellState state = graph.getView().getState(cell);
		 * state.setLabel(newName); } finally { graph.getModel().endUpdate(); } graph.repaint();
		 */
	}
	
	public void updatePort(Object portCell)
	{
		graph.updatePortOrientation(portCell, ((mxCell)portCell).getGeometry());
		graph.refresh();
	}

	/**
	 * Select the given cell on the drawing board.
	 * 
	 * @param cell the cell to be selected
	 */
	public void setSelected(Object cell)
	{
		graph.setSelectionCell(cell);
	}

	/**
	 * Return the current loaded module.
	 * 
	 * @return the current loaded module
	 */
	public Module getActiveModule()
	{
		return activeModule;
	}
	
	/**
	 * Return the graph.
	 * @return the graph
	 */
	public ACGraph getGraph()
	{
		return graph;
	}
 
	
	/**
	 * Change the current module displayed.
	 * 
	 * @param mod the new module to display
	 */
	public void changeModule(Module mod)
	{
		saveCurrentPositions();
		double width = graphComponent.getVisibleRect().getWidth() - 50;
		double height = graphComponent.getVisibleRect().getHeight() - 50;
		//mxRectangle bounds = new mxRectangle(25, 25, width, height);
		mxGeometry geo = new mxGeometry(25, 25, width, height);
		//mod.setDrawingCellGeometry(geo);
		Object cell = mod.getDrawingCell();
		if (activeModule != null)
		{
			removeVisibleCells();
		}
		graph.getModel().beginUpdate();
		try
		{
			/*
			 * if (!graph.getModel().isVisible(cell)) { //System.out.println("Module not visible.");
			 * graph.getModel().setVisible(cell, true); }
			 */
			((mxCell)cell).setGeometry(geo);
			graph.getModel().add(parent, cell, 0);
			//graph.resizeCell(cell, bounds);
			graph.getModel().setStyle(cell, "Module");
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		activeModule = mod;
		drawPorts(mod);
		drawChildren(mod);
		drawVisibleVariables(mod);
		drawEquivalenceNodes(mod);
		drawConnections(mod);
		// printCellCount(cell);
		// printBoardStats();
	}

	/**
	 * Used for debugging purposes.
	 */
	/*
	private void printBoardStats()
	{
		mxGraphModel model = (mxGraphModel) graph.getModel();
		// Object cells[] = model.getChildren(model, model.getRoot());
		Object cells[] = model.getChildren(graph.getModel(), graph.getModel().getRoot());
		mxCell child;
		Module mod;
		int count = model.getChildCount(model.getRoot());
		System.out.println("Root children: " + graph.getModel().getChildCount(graph.getModel().getRoot()));
		System.out.println("Active module cell children:"
				+ graph.getModel().getChildCount(activeModule.getDrawingCell()));
		System.out.println("Default parent children: " + graph.getModel().getChildCount(graph.getDefaultParent()));
		System.out.println("All children: "
				+ mxGraphModel.getChildCells(graph.getModel(), graph.getDefaultParent(), true, true).length);
	}
	*/
	/**
	 * Save the submodule positions within the active module.
	 */
	public void saveCurrentPositions()
	{
		if (activeModule != null)
		{
			ListIterator<Module> children = activeModule.getChildren().listIterator();
			Module child;
			Object childCell;
			mxRectangle bounds;
			mxGeometry geo;

			while (children.hasNext())
			{
				child = children.next();
				childCell = child.getDrawingCell();

				//bounds = graph.getBoundingBox(childCell);
				//child.setDrawingCellBounds(bounds);
				geo = ((mxCell)childCell).getGeometry();
				child.setDrawingCellGeometry(geo);
			}
			
			ListIterator<VisibleVariable> vars = activeModule.getVisibleVariables().listIterator();
			VisibleVariable var;
			Object varCell;
			while (vars.hasNext())
			{
				var = vars.next();
				varCell = var.getDrawingCell();
				
				//bounds = graph.getBoundingBox(varCell);
				//var.setDrawingCellBounds(bounds);
				geo = ((mxCell)varCell).getGeometry();
				var.setDrawingCellGeometry(geo);
				//var.setDrawingCellGeometry(((mxCell)varCell).getGeometry());
			}
			
			ListIterator<EquivalenceNode> eNodes = activeModule.getEquivalenceNodes().listIterator();
			EquivalenceNode eNode;
			Object eNodeCell;
			while(eNodes.hasNext())
			{
				eNode = eNodes.next();
				eNodeCell = eNode.getDrawingCell();
				
				geo = ((mxCell)eNodeCell).getGeometry();
				eNode.setDrawingCellGeometry(geo);
			}
		}
	}

	/**
	 * Used for debugging purposes.
	 * @param cell the parent cell
	 */
	private void printCellCount(Object cell)
	{
		int childCount = graph.getModel().getChildCount(cell);
		// System.out.println("Child count: " + childCount);
	}

	/**
	 * Draw the children of the given module.
	 * @param mod The module whose children will be drawn
	 */
	private void drawChildren(Module mod)
	{
		ListIterator<Module> children;
		Module child;
		Object parentCell;
		Object childCell;
		mxRectangle bounds;
		mxGeometry geo;
		double xPosition;
		double yPosition;
		int childIndex;

		// get the list of children modules
		children = mod.getChildren().listIterator();

		// get the parent drawing cell where the children cells will be added
		parentCell = mod.getDrawingCell();

		while (children.hasNext())
		{
			// get the current child, its drawing cell, and its index
			child = children.next();
			childCell = child.getDrawingCell();
			childIndex = children.nextIndex();

			// draw the current child cell
			graph.getModel().beginUpdate();
			try
			{				
				geo = child.getDrawingCellGeometry();
				//bounds = child.getDrawingCellBounds();
				if (geo == null || (geo.getHeight() <= 10))
				{
					//bounds = new mxRectangle();
					geo = new mxGeometry();
					xPosition = 40 + (childIndex * 20);
					yPosition = 40 + (childIndex * 20);
					geo.setX(xPosition);
					geo.setY(yPosition);
					geo.setWidth(DEFAULT_SUBMODULE_WIDTH);
					geo.setHeight(DEFAULT_SUBMODULE_HEIGHT);
				}
				((mxCell)childCell).setGeometry(geo);
				//graph.resizeCell(childCell, bounds);
				graph.getModel().add(parentCell, childCell, 0);
				if(child.getDrawingCellStyle().equals(""))
				{
					child.setDrawingCellStyle("Submodule");
				}
				graph.getModel().setStyle(childCell, child.getDrawingCellStyle());
			}
			finally
			{
				graph.getModel().endUpdate();
			}
			//child.setDrawingCellBounds(bounds);
			// draw the ports of the current child
			drawPorts(child);
			/*
			if (child instanceof MathematicalAggregator)
			{
				drawMathAggregatorPorts((MathematicalAggregator)child);
			}
			else
			{
				drawPorts(child);
			}
			*/
		}
	}

	/**
	 * Draw the ports of the given module.
	 * @param mod The module whose ports will be drawn
	 */
	private void drawPorts(Module mod)
	{
		ListIterator<Port> ports;
		Object cell;
		Object portCell;
		mxGeometry geo;
		double x;
		double y;
		double width;
		double height;
		double offsetX;
		double offsetY;

		// get the list of ports
		ports = mod.getPorts().listIterator();

		// get the drawing cell where the ports will be added
		cell = mod.getDrawingCell();

		// loop through each port of the module
		while (ports.hasNext())
		{
			// get the drawing cell of the current port
			portCell = (mxCell) ports.next().getDrawingCell();

			// draw the current port
			graph.getModel().beginUpdate();
			try
			{
				x = ((mxCell) portCell).getGeometry().getX();
				y = ((mxCell) portCell).getGeometry().getY();
				if (mod == activeModule)
				{
					width = DEFAULT_PORT_WIDTH;
					height = DEFAULT_PORT_HEIGHT;
				}
				else
				{
					width = DEFAULT_PORT_WIDTH / 2;
					height = DEFAULT_PORT_HEIGHT / 2;
				}
				offsetX = -width / 2;
				offsetY = -height / 2;
				geo = new mxGeometry(x, y, width, height);
				geo.setOffset(new mxPoint(offsetX, offsetY));
				geo.setRelative(true);
				graph.getModel().add(cell, portCell, 0);
				graph.getModel().setGeometry(portCell, geo);
				graph.updatePortOrientation(portCell, geo);
			}
			finally
			{
				graph.getModel().endUpdate();
			}
		}
	}

	private void drawVisibleVariables(Module mod)
	{
		ListIterator<VisibleVariable> vars;
		Object cell;
		Object varCell;
		//mxGeometry geo;
		double x;
		double y;
		double width;
		double height;
		double offsetX;
		double offsetY;
		VisibleVariable var;
		int varIndex;
		mxRectangle bounds;
		mxGeometry geo;
		double xPosition;
		double yPosition;

		// get the list of visible variables
		vars = mod.getVisibleVariables().listIterator();

		// get the drawing cell where the visible variables will be added
		cell = mod.getDrawingCell();

		// loop through each visible variable of the module
		while (vars.hasNext())
		{
			// get the index of the next visible variable
			varIndex = vars.nextIndex();
			// get the next visible variable
			var = vars.next();
			// get the drawing cell of the current visible variable
			varCell = var.getDrawingCell();

			// draw the current visible variable
			graph.getModel().beginUpdate();
			try
			{
				/*
				x = ((mxCell) varCell).getGeometry().getX();
				y = ((mxCell) varCell).getGeometry().getY();
				
				//offsetX = -width / 2;
				//offsetY = -height / 2;
				geo = new mxGeometry(x, y, width, height);
				geo.setOffset(new mxPoint(offsetX, offsetY));
				geo.setRelative(true);
				*/
				
				geo = var.getDrawingCellGeometry();
				//bounds = var.getDrawingCellBounds();
				if (geo == null)
				{
					//bounds = new mxRectangle();
					geo = new mxGeometry();
					xPosition = 40 + (varIndex * 20);
					yPosition = 40 + (varIndex * 20);
					geo.setX(xPosition);
					geo.setY(yPosition);
					geo.setWidth(DEFAULT_VISIBLEVARIABLE_WIDTH);
					geo.setHeight(DEFAULT_VISIBLEVARIABLE_HEIGHT);
				}
				((mxCell)varCell).setGeometry(geo);
				//graph.resizeCell(varCell, bounds);
				graph.getModel().add(cell, varCell, 0);
				graph.getModel().setStyle(varCell, "VisibleVariable");
				//graph.getModel().setGeometry(varCell, geo);
			}
			finally
			{
				graph.getModel().endUpdate();
			}
		}
	}
	
	/**
	 * Draw the equivalence nodes of the given module
	 * @param mod the module whose equivalence nodes will be drawn
	 */
	private void drawEquivalenceNodes(Module mod)
	{
		ListIterator<EquivalenceNode> eNodes;
		Object cell;
		Object eNodeCell;
		//mxGeometry geo;
		double x;
		double y;
		double width;
		double height;
		double offsetX;
		double offsetY;
		EquivalenceNode eNode;
		int eNodeIndex;
		mxRectangle bounds;
		mxGeometry geo;
		double xPosition;
		double yPosition;
		
		// get the list of equivalence nodes
		eNodes = mod.getEquivalenceNodes().listIterator();
		
		// get the drawing cell where the equivalence nodes will be added
		cell = mod.getDrawingCell();
		
		// loop through each visible variable of the module
		while (eNodes.hasNext())
		{
			// get the index of the next visible variable
			eNodeIndex = eNodes.nextIndex();
			// get the next visible variable
			eNode = eNodes.next();
			// get the drawing cell of the current visible variable
			eNodeCell = eNode.getDrawingCell();
			graph.getModel().beginUpdate();
			try
			{				
				geo = eNode.getDrawingCellGeometry();
				//bounds = var.getDrawingCellBounds();
				if (geo == null)
				{
					//bounds = new mxRectangle();
					geo = new mxGeometry();
					xPosition = 40 + (eNodeIndex * 20);
					yPosition = 40 + (eNodeIndex * 20);
					geo.setX(xPosition);
					geo.setY(yPosition);
					geo.setWidth(DEFAULT_EQUIVALENCENODE_WIDTH);
					geo.setHeight(DEFAULT_EQUIVALENCENODE_HEIGHT);
				}
				((mxCell)eNodeCell).setGeometry(geo);
				//graph.resizeCell(eNodeCell, bounds);
				graph.getModel().add(cell, eNodeCell, 0);
				graph.getModel().setStyle(eNodeCell, "EquivalenceNode");
				//graph.getModel().setGeometry(eNodeCell, geo);
			}
			finally
			{
				graph.getModel().endUpdate();
			}
			
		}
	}
	
	/**
	 * Draw the connections of the given module.
	 * @param mod the module whose connections will be drawn
	 */
	private void drawConnections(Module mod)
	{
		ListIterator<Connection> connections;
		Object parentCell = mod.getDrawingCell();
		Object connectionCell;
		
		// get the list of connections
		connections = mod.getConnections().listIterator();
		
		while(connections.hasNext())
		{
			connectionCell = connections.next().getDrawingCell();
			
			// draw the current connection cell
			graph.getModel().beginUpdate();
			try
			{
				graph.getModel().add(parentCell, connectionCell, 0);
			}
			finally
			{
				graph.getModel().endUpdate();
			}
		}
	}
	
	private void drawMathAggregatorPorts(MathematicalAggregator mathAgg)
	{
		ListIterator<Port> ports;
		Port currentPort;
		Object cell;
		mxCell portCell;
		mxGeometry geo;
		double x;
		double y;
		double width;
		double height;
		double offsetX;
		double offsetY;
		double portSpacing;
		int inputPortIndex; 
		
		/*
		width = DEFAULT_PORT_WIDTH/2;
		height = DEFAULT_PORT_HEIGHT/2;

		offsetX = -width / 2;
		offsetY = -height / 2;
		*/
		if (mathAgg == activeModule)
		{
			width = DEFAULT_PORT_WIDTH;
			height = DEFAULT_PORT_HEIGHT;
		}
		else
		{
			width = DEFAULT_PORT_WIDTH / 2;
			height = DEFAULT_PORT_HEIGHT / 2;
		}
		offsetX = -width / 2;
		offsetY = -height / 2;
		//portSpacing = 0.35;
		portSpacing = 0.8 / (mathAgg.getPorts().size() - 2);
		
		inputPortIndex = 0;
		cell = mathAgg.getDrawingCell();
		ports = mathAgg.getPorts().listIterator();
		while(ports.hasNext())
		{
			currentPort = ports.next();
			portCell = (mxCell)currentPort.getDrawingCell();
			
			graph.getModel().beginUpdate();
			try
			{
				switch(currentPort.getType())
				{
				case INPUT:
					x = 0;
					y = (inputPortIndex * portSpacing) + 0.1;
					inputPortIndex++;
					break;
				case OUTPUT:
					x = 1.0;
					y = 0.5;
					break;
				default:
					x = 0.0;
					y = 0.0;
					x = ((mxCell) portCell).getGeometry().getX();
					y = ((mxCell) portCell).getGeometry().getY();
				}
				
				geo = new mxGeometry(x, y, width, height);
				geo.setOffset(new mxPoint(offsetX, offsetY));
				geo.setRelative(true);
	
				
				portCell.setVertex(true);
				portCell.setConnectable(true);
	
				graph.getModel().add(cell, portCell, 0);
				graph.getModel().setGeometry(portCell, geo);
				graph.updatePortOrientation(portCell, geo);
			}
			finally
			{
				graph.getModel().endUpdate();
			}
	
			currentPort.setDrawingCell(portCell);
		}
	}
	
	private void addMathAggregatorPorts(MathematicalAggregator mathAgg)
	{
		ListIterator<Port> ports;
		Port currentPort;
		Object cell;
		mxCell portCell;
		mxGeometry geo;
		double x;
		double y;
		double width;
		double height;
		double offsetX;
		double offsetY;
		double portSpacing;
		int inputPortIndex; 
		
		width = DEFAULT_PORT_WIDTH/2;
		height = DEFAULT_PORT_HEIGHT/2;

		offsetX = -width / 2;
		offsetY = -height / 2;
		//portSpacing = 0.35;
		portSpacing = 0.8 / (mathAgg.getPorts().size() - 2);
		
		inputPortIndex = 0;
		cell = mathAgg.getDrawingCell();
		ports = mathAgg.getPorts().listIterator();
		while(ports.hasNext())
		{
			currentPort = ports.next();
			graph.getModel().beginUpdate();
			try
			{
				switch(currentPort.getType())
				{
				case INPUT:
					x = 0;
					y = (inputPortIndex * portSpacing) + 0.1;
					inputPortIndex++;
					break;
				case OUTPUT:
					x = 1.0;
					y = 0.5;
					break;
				default:
					x = 0.0;
					y = 0.0;
				}
				
				geo = new mxGeometry(x, y, width, height);
				geo.setOffset(new mxPoint(offsetX, offsetY));
				geo.setRelative(true);
	
				portCell = new mxCell(currentPort, geo, "Port");
				portCell.setVertex(true);
				portCell.setConnectable(true);
	
				graph.getModel().add(cell, portCell, 0);
				// graph.updatePortOrientation(port1, geo1);
				graph.updatePortOrientation(portCell, geo);
			}
			finally
			{
				graph.getModel().endUpdate();
			}
	
			currentPort.setDrawingCell(portCell);
		}
	}
	
	/**
	 * Remove the visible cells.
	 */
	private void removeVisibleCells()
	{

		removeChildren(graph.getDefaultParent());
		/*
		 * graph.getModel().beginUpdate(); try { System.out.println("Number of verticies to remove: " +
		 * graph.getChildVertices(graph.getDefaultParent()).length);
		 * graph.removeCells(graph.getChildVertices(graph.getDefaultParent())); } finally {
		 * graph.getModel().endUpdate(); }
		 */
	}

	/**
	 * Remove the given drawing cell's children from the graph.
	 * @param cell the drawing cell whose children will be removed
	 */
	private void removeChildren(Object cell)
	{
		int count = graph.getChildVertices(cell).length;
		if (count == 0)
		{
			return;
		}

		Object children[] = graph.getChildVertices(cell);
		for (int i = 0; i < count; i++)
		{
			removeChildren(children[i]);
		}

		
		graph.removeCells(graph.getChildVertices(cell));
	}

	/**
	 * Make the children of the given cell invisible.
	 * 
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
		//cell.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
		//cell.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_MIDDLE);
		//cell.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "white");
		//cell.put(mxConstants.STYLE_LABEL_BORDERCOLOR, "red");
		//cell.put(mxConstants.STYLE_SPACING_TOP, "-10");
		cell.put(mxConstants.STYLE_FONTFAMILY, "Times New Roman");
		cell.put(mxConstants.STYLE_FONTSIZE, "12");
		cell.put(mxConstants.STYLE_FONTSTYLE, "1");
		cell.put(mxConstants.STYLE_FONTCOLOR, "black");
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		cell.put(mxConstants.STYLE_ROUNDED, "true");
		cell.put(mxConstants.STYLE_FOLDABLE, "0");
		cell.put(mxConstants.STYLE_WHITE_SPACE, "wrap");

		styleSheet.putCellStyle("Submodule", cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		cell.put(mxConstants.STYLE_STROKECOLOR, "black");
		cell.put(mxConstants.STYLE_FILLCOLOR, "white");
		cell.put(mxConstants.STYLE_FONTFAMILY, "Times New Roman");
		cell.put(mxConstants.STYLE_FONTSIZE, "12");
		cell.put(mxConstants.STYLE_FONTSTYLE, "1");
		cell.put(mxConstants.STYLE_FONTCOLOR, "black");
		cell.put(mxConstants.STYLE_FOLDABLE, "0");
		cell.put(mxConstants.STYLE_PERIMETER, "ellipsePerimeter");

		styleSheet.putCellStyle("VisibleVariable", cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_STROKECOLOR, "blue");
		cell.put(mxConstants.STYLE_FILLCOLOR, "orange");
		cell.put(mxConstants.STYLE_OPACITY, "50.0");
		cell.put(mxConstants.STYLE_STROKEWIDTH, "3.0");
		cell.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
		//cell.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
		//cell.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_MIDDLE);
		//cell.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "white");
		//cell.put(mxConstants.STYLE_LABEL_BORDERCOLOR, "red");
		//cell.put(mxConstants.STYLE_SPACING_TOP, "-10");
		cell.put(mxConstants.STYLE_FONTFAMILY, "Times New Roman");
		cell.put(mxConstants.STYLE_FONTSIZE, "12");
		cell.put(mxConstants.STYLE_FONTSTYLE, "1");
		cell.put(mxConstants.STYLE_FONTCOLOR, "black");
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		cell.put(mxConstants.STYLE_ROUNDED, "true");
		cell.put(mxConstants.STYLE_FOLDABLE, "0");
		cell.put(mxConstants.STYLE_WHITE_SPACE, "wrap");

		styleSheet.putCellStyle("Summation", cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_STROKECOLOR, "blue");
		cell.put(mxConstants.STYLE_FILLCOLOR, "orange");
		cell.put(mxConstants.STYLE_OPACITY, "50.0");
		cell.put(mxConstants.STYLE_STROKEWIDTH, "3.0");
		cell.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
		//cell.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
		//cell.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_MIDDLE);
		//cell.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "white");
		//cell.put(mxConstants.STYLE_LABEL_BORDERCOLOR, "red");
		//cell.put(mxConstants.STYLE_SPACING_TOP, "-10");
		cell.put(mxConstants.STYLE_FONTFAMILY, "Times New Roman");
		cell.put(mxConstants.STYLE_FONTSIZE, "12");
		cell.put(mxConstants.STYLE_FONTSTYLE, "1");
		cell.put(mxConstants.STYLE_FONTCOLOR, "black");
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		cell.put(mxConstants.STYLE_ROUNDED, "true");
		cell.put(mxConstants.STYLE_FOLDABLE, "0");
		cell.put(mxConstants.STYLE_WHITE_SPACE, "wrap");

		styleSheet.putCellStyle("Product", cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
		cell.put(mxConstants.STYLE_FILLCOLOR, "yellow");
		cell.put(mxConstants.STYLE_PERIMETER, "rhombusPerimeter");
		// cell.put(mxConstants.STYLE_OPACITY, "75.0");

		styleSheet.putCellStyle("EquivalenceNode", cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_NORTH);
		cell.put(mxConstants.STYLE_FILLCOLOR, "red");
		cell.put(mxConstants.STYLE_PERIMETER, "trianglePerimeter");
		// cell.put(mxConstants.STYLE_OPACITY, "75.0");

		styleSheet.putCellStyle("InputPort_North", cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_SOUTH);
		cell.put(mxConstants.STYLE_FILLCOLOR, "red");
		cell.put(mxConstants.STYLE_PERIMETER, "trianglePerimeter");
		// cell.put(mxConstants.STYLE_OPACITY, "75.0");

		styleSheet.putCellStyle("InputPort_South", cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_EAST);
		cell.put(mxConstants.STYLE_FILLCOLOR, "red");
		cell.put(mxConstants.STYLE_PERIMETER, "trianglePerimeter");
		// cell.put(mxConstants.STYLE_OPACITY, "75.0");

		styleSheet.putCellStyle("InputPort_East", cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_WEST);
		cell.put(mxConstants.STYLE_FILLCOLOR, "red");
		cell.put(mxConstants.STYLE_PERIMETER, "trianglePerimeter");
		// cell.put(mxConstants.STYLE_OPACITY, "75.0");

		styleSheet.putCellStyle("InputPort_West", cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_NORTH);
		cell.put(mxConstants.STYLE_FILLCOLOR, "blue");
		cell.put(mxConstants.STYLE_PERIMETER, "trianglePerimeter");
		// cell.put(mxConstants.STYLE_OPACITY, "75.0");

		styleSheet.putCellStyle("OutputPort_North", cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_SOUTH);
		cell.put(mxConstants.STYLE_FILLCOLOR, "blue");
		cell.put(mxConstants.STYLE_PERIMETER, "trianglePerimeter");
		// cell.put(mxConstants.STYLE_OPACITY, "75.0");

		styleSheet.putCellStyle("OutputPort_South", cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_EAST);
		cell.put(mxConstants.STYLE_FILLCOLOR, "blue");
		cell.put(mxConstants.STYLE_PERIMETER, "trianglePerimeter");
		// cell.put(mxConstants.STYLE_OPACITY, "75.0");

		styleSheet.putCellStyle("OutputPort_East", cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_WEST);
		cell.put(mxConstants.STYLE_FILLCOLOR, "blue");
		cell.put(mxConstants.STYLE_PERIMETER, "trianglePerimeter");
		// cell.put(mxConstants.STYLE_OPACITY, "75.0");

		styleSheet.putCellStyle("OutputPort_West", cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_NORTH);
		cell.put(mxConstants.STYLE_FILLCOLOR, "yellow");
		cell.put(mxConstants.STYLE_PERIMETER, "rhombusPerimeter");
		// cell.put(mxConstants.STYLE_OPACITY, "75.0");

		styleSheet.putCellStyle("EquivalencePort_North", cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_SOUTH);
		cell.put(mxConstants.STYLE_FILLCOLOR, "yellow");
		cell.put(mxConstants.STYLE_PERIMETER, "rhombusPerimeter");
		// cell.put(mxConstants.STYLE_OPACITY, "75.0");

		styleSheet.putCellStyle("EquivalencePort_South", cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_EAST);
		cell.put(mxConstants.STYLE_FILLCOLOR, "yellow");
		cell.put(mxConstants.STYLE_PERIMETER, "rhombusPerimeter");
		// cell.put(mxConstants.STYLE_OPACITY, "75.0");

		styleSheet.putCellStyle("EquivalencePort_East", cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_WEST);
		cell.put(mxConstants.STYLE_FILLCOLOR, "yellow");
		cell.put(mxConstants.STYLE_PERIMETER, "rhombusPerimeter");
		// cell.put(mxConstants.STYLE_OPACITY, "75.0");

		styleSheet.putCellStyle("EquivalencePort_West", cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		
		styleSheet.putCellStyle("ConnectionEdge", cell);
	}

	/**
	 * Install various listeners for the graph.
	 */
	private void installListeners()
	{
		// when a cell is selected the corresponding tree node is also highlighted.
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e)
			{
				// Object cell = graphComponent.getCellAt(e.getX(), e.getY());
				mxCell cell = (mxCell) graphComponent.getCellAt(e.getX(), e.getY());
				// Object obj = graph.getModel().getValue(cell);
				// Module val = (Module) ((mxCell)cell).getValue();
				mxCell dCell = ((mxCell) cell);
				Port p;

				if (cell != null)
				{
					if (((mxCell) cell).getValue() instanceof Port)
					{
						// System.out.println("port=" + ((mxCell) cell).getValue().toString());
						// p = (Port) ((mxCell)cell).getValue();
					}

					if (((mxCell) cell).getValue() instanceof Module)
					{
						// System.out.println("cell=" + graph.getLabel(cell));
						// Module mod = AC_GUI.moduleList.findModule(cell);
						// AC_GUI.treeView.setSelected(mod.getTreeNode());
						AC_GUI.currentGUI.setSelectedModule(cell);
						//printCellCount(cell);
					}
				}
				else
				{
					// System.out.println("Null click");
				}
			}
		});

		// when right-click is pressed, create a popup menu.
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e)
			{
				Object cell = graphComponent.getCellAt(e.getX(), e.getY());

				if (cell != null && SwingUtilities.isRightMouseButton(e))
				{
					// System.out.println("cell=" + graph.getLabel(cell));
					// System.out.println("button = " + e.getButton());
					JPopupMenu menu = createPopupMenu(e.getPoint(), cell);
					menu.show(graphComponent, e.getX(), e.getY());
				}

			}
		});
		
		// listen for when a valid edge is created.
		graphComponent.getConnectionHandler().addListener(mxEvent.CONNECT, new mxIEventListener() {
			@Override
			public void invoke(Object sender, mxEventObject evt)
			{
				//System.out.println("edge="+evt.getProperty("cell"));
				//Port sourcePort = (Port) ((mxCell)evt.getProperty("cell")).getSource().getValue();
				//Port targetPort = (Port) ((mxCell)evt.getProperty("cell")).getTarget().getValue();
				
				//System.out.println("Source port: " + sourcePort.getName());
				//System.out.println("Target port: " + targetPort.getName());
				mxCell cell = (mxCell)evt.getProperty("cell");
				//check if the edge is connecting two equivalence ports
				if (cell.getSource().getValue() instanceof Port && cell.getTarget().getValue() instanceof Port)
				{
					if (((Port)cell.getSource().getValue()).getType() == PortType.EQUIVALENCE 
							&& ((Port)cell.getTarget().getValue()).getType() == PortType.EQUIVALENCE)
					{
						//the edge is connecting two equivalence ports
						System.out.println("Time to create an equivalence node.");
						AC_GUI.addEquivalenceNode(activeModule, cell);
						graph.getModel().remove(cell);
						return;
					}
				}
				
				AC_GUI.currentGUI.addConnection(activeModule, evt.getProperty("cell"));
				//Object cell1 = graph.createEdge(cell, null, "val", "s", "t", "");
				// set the connection edge style
				graph.getModel().beginUpdate();
				try
				{
					graph.getModel().setStyle(evt.getProperty("cell"), "ConnectionEdge");
				}
				finally
				{
					graph.getModel().endUpdate();
				}
			}
		});
	}

	/**
	 * Create a popup menu for modules and ports.
	 * @param pt the location of the menu
	 * @param cell the drawing cell the menu will be created for
	 * @return the popup menu
	 */
	private JPopupMenu createPopupMenu(final Point pt, final Object cell)
	{
		JPopupMenu menu = new JPopupMenu();

		menu.add(new AbstractAction("Edit Name") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				//graphComponent.startEditingAtCell(cell);
			}
		});

		if (((mxCell)cell).isEdge())
		{
			menu.add(new AbstractAction("Remove Connection") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;
				
				public void actionPerformed(ActionEvent e)
				{
					// get the connection object from the drawing cell
					Connection edge = (Connection)((mxCell)cell).getValue();
					// remove the drawing cell
					removeCell(edge.getDrawingCell());
					// remove the connection from the module
					AC_GUI.currentGUI.removeConnection(edge);
				}
			});
			
			menu.add(new AbstractAction("Properties") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e)
				{
					String source = "";
					String target = "";
					
					if (((mxCell)cell).getSource().getValue() instanceof Port)
					{
						source = "Port ";
						source += ((Port)((mxCell)cell).getSource().getValue()).getParent().getName();
						source += "." + ((Port)((mxCell)cell).getSource().getValue()).getName();
					} else if (((mxCell)cell).getSource().getValue() instanceof VisibleVariable)
					{
						source = "Variable ";
						source += ((VisibleVariable)((mxCell)cell).getSource().getValue()).getRefName();
					}
					
					if (((mxCell)cell).getTarget().getValue() instanceof Port)
					{
						target = "Port ";
						target += ((Port)((mxCell)cell).getTarget().getValue()).getParent().getName();
						target += "." + ((Port)((mxCell)cell).getTarget().getValue()).getName();
					} else if (((mxCell)cell).getTarget().getValue() instanceof VisibleVariable)
					{
						target = "Variable ";
						target += ((VisibleVariable)((mxCell)cell).getTarget().getValue()).getRefName();
					}
					
					String msg = "Source = " + source + "\nDestination = " + target;
					JOptionPane.showMessageDialog(null, msg);
				}
			});
		}
		else
		{
			if (((mxCell) cell).getValue() instanceof Module)
			{
				menu.add(new AbstractAction("Add Port") {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent e)
					{
						PortAddEditor portAddEditor = new PortAddEditor(cell, graphComponent);
						portAddEditor.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						portAddEditor.setModal(true);
						portAddEditor.setVisible(true);
					}
				});
				
				menu.add(new AbstractAction("Show Variable") {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent e)
					{
						VariableAddEditor varAddEditor = new VariableAddEditor(graphComponent);
						varAddEditor.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						varAddEditor.setModal(true);
						varAddEditor.setVisible(true);
					}
				});
			}
			else if (((mxCell) cell).getValue() instanceof Port)
			{
				menu.add(new AbstractAction("Change Type") {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent e)
					{
						Port currentPort = (Port) ((mxCell) cell).getValue();

						Object[] possibleValues = { "Input", "Output", "Equivalence" };
						Object selectedValue = JOptionPane.showInputDialog(null, "Select Type", "Change Port Type", JOptionPane.INFORMATION_MESSAGE, null, possibleValues, currentPort.getType().toString());

						if (selectedValue.toString().compareTo("Input") == 0)
						{
							currentPort.setType(PortType.INPUT);
						}
						else if (selectedValue.toString().compareTo("Output") == 0)
						{
							currentPort.setType(PortType.OUTPUT);
						}
						else if (selectedValue.toString().compareTo("Equivalence") == 0)
						{
							currentPort.setType(PortType.EQUIVALENCE);
						}

						mxGeometry geo = ((mxCell) cell).getGeometry();
						graph.updatePortOrientation(cell, geo);
					}
				});

				menu.add(new AbstractAction("Remove") {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent e)
					{
						// get the Port object from the drawing cell
						Port currentPort = (Port) ((mxCell) cell).getValue();
						String msg = "Number of edges connected to the port: ";
						msg += graph.getModel().getEdgeCount(cell) + ".";
						//System.out.println(msg);
						// call AC_GUI to fully remove the port
						AC_GUI.currentGUI.removePort(currentPort);
					}
				});

				menu.add(new AbstractAction("Properties") {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent e)
					{

					}
				});
			}
			else if (((mxCell) cell).getValue() instanceof VisibleVariable)
			{
				menu.add(new AbstractAction("Remove") {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent e)
					{
						// get the Visible Variable object from the drawing cell
						VisibleVariable currentVar = (VisibleVariable) ((mxCell) cell).getValue();
						String msg = "Number of edges connected to the variable: ";
						msg += graph.getModel().getEdgeCount(cell) + ".";
						//System.out.println(msg);
						// call AC_GUI to fully remove the port
						AC_GUI.removeVisibleVariable(currentVar);
					}
				});
			}
		}
		

		return menu;
	}
}