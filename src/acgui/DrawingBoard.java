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

import org.sbml.libsbml.GeneralGlyph;

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
	private final int DEFAULT_BUTTON_HEIGHT = 20;
	private final int DEFAULT_BUTTON_WIDTH = 20;
	
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

		Object cell = graph.createVertex(parentCell, null, mod, 0, 0, 1, 1, "");
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
	
	public void createCell(Module mod, GeneralGlyph glyph)
	{
		Object parentCell;
		mxGeometry geo = new mxGeometry();
		double x = glyph.getBoundingBox().x();
		double y = glyph.getBoundingBox().y();
		double width = glyph.getBoundingBox().width();
		double height = glyph.getBoundingBox().height();
		// Determine what the parentCell will be
		if (mod.getParent() != null)
		{
			// The module's parent's drawing cell will be the parentCell
			parentCell = mod.getParent().getDrawingCell();
			if (!(mod instanceof MathematicalAggregator))
			{
				width = DEFAULT_SUBMODULE_WIDTH;
				height = DEFAULT_SUBMODULE_HEIGHT;
			}
		}
		else
		{
			// The module has no parent, the default parent will be the
			// parentCell
			parentCell = parent;
		}

		// Create the cell

		Object cell = graph.createVertex(parentCell, null, mod, 0, 0, 1, 1, "");
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
		
		if (glyph == null)
		{
			System.err.println("Glyph is null.");
		}
		System.out.println("Glyph id: " + glyph.getId());
		if (glyph.getBoundingBox() == null)
		{
			System.err.println("Glyph boundingbox is null.");
		}
		
		
		graph.getModel().beginUpdate();
		try
		{
			geo.setX(x);
			geo.setY(y);
			geo.setWidth(width);
			geo.setHeight(height);
			((mxCell)cell).setGeometry(geo);
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		//mod.setDrawingCellBounds(bounds);
		mod.setDrawingCellGeometry(geo);
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
			
			//xPosition = 40 + (childCount * 20);
			//yPosition = 40 + (childCount * 20);
			xPosition = 0.2 + (childCount * 0.01);
			yPosition = 0.2 + (childCount * 0.01);
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
	
	/**
	 * Add the drawing cell representation of the given module to the module's parent.
	 * 
	 * @param mod the module to add
	 */
	public void addCell(Module mod, GeneralGlyph glyph)
	{
		Object parentCell = mod.getParent().getDrawingCell();
		Object childCell = mod.getDrawingCell();
		int childCount = mod.getParent().getChildren().size();
		mxGeometry geo = new mxGeometry();
		double x = glyph.getBoundingBox().x();
		double y = glyph.getBoundingBox().y();
		double width = glyph.getBoundingBox().width();
		double height = glyph.getBoundingBox().height();
		
		graph.getModel().beginUpdate();
		try
		{
			if (!graph.getModel().isVisible(childCell))
			{
				// System.out.println("Submodule not visible.");
				graph.getModel().setVisible(childCell, true);
			}

			geo.setX(x);
			geo.setY(y);
			geo.setWidth(width);
			geo.setHeight(height);
			((mxCell)childCell).setGeometry(geo);
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
	
	public void createPort(Port port, GeneralGlyph glyph)
	{
		mxCell port1 = null;
		double x = glyph.getBoundingBox().x();
		double y = glyph.getBoundingBox().y();
		double width = DEFAULT_PORT_WIDTH;
		double height = DEFAULT_PORT_HEIGHT;
		mxGeometry geo = new mxGeometry(x, y, width, height);
		geo.setRelative(true);
		
		port1 = new mxCell(port, geo, "Port");
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

	/**
	 * Add a drawing cell representation of the given port to the 
	 * drawing cell representation of the given module.
	 * 
	 * @param parentMod the parent module of the port
	 * @param port the port to be added
	 */
	public void addPort(Module parentMod, Port port, GeneralGlyph glyph)
	{
		Object cell = parentMod.getDrawingCell();
		double width = DEFAULT_PORT_WIDTH;
		double height = DEFAULT_PORT_HEIGHT;
		double x = glyph.getBoundingBox().x();
		double y = glyph.getBoundingBox().y();
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
			mxGeometry geo1 = new mxGeometry(x, y, width, height);
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
		var1 = (mxCell)graph.createVertex(parentCell, null, var, 0, 0, 10, 10, "");
		//var1 = new mxCell(var);
		var1.setGeometry(var.getDrawingCellGeometry());
		var1.setVertex(true);
		var1.setConnectable(true);
		
		var.setDrawingCell(var1);
	}
	
	public void createVisibleVariable(VisibleVariable var, GeneralGlyph glyph)
	{
		Object parentCell = var.getParent().getDrawingCell();
		mxCell var1 = null;
		var1 = (mxCell)graph.createVertex(parentCell, null, var, 5, 5, 10, 10, "");
		//var1 = new mxCell(var);
		double x = glyph.getBoundingBox().x();
		double y = glyph.getBoundingBox().y();
		double width = DEFAULT_VISIBLEVARIABLE_WIDTH;
		double height = DEFAULT_VISIBLEVARIABLE_HEIGHT;
		mxGeometry geo = new mxGeometry(x, y, width, height);
		var1.setGeometry(geo);
		var1.setVertex(true);
		var1.setConnectable(true);
		
		var.setDrawingCell(var1);
		var.setDrawingCellGeometry(geo);
	}
	
	public void createEquivalenceNode(EquivalenceNode eNode)
	{
		Object parentCell = eNode.getParent().getDrawingCell();
		mxCell eNodeCell = null;
		eNodeCell = (mxCell)graph.createVertex(parentCell, null, eNode, 0, 0, 10, 10, "");
		eNodeCell.setGeometry(eNode.getDrawingCellGeometry());
		eNodeCell.setVertex(true);
		eNodeCell.setConnectable(true);
		
		eNode.setDrawingCell(eNodeCell);
	}
	
	public void createEquivalenceNode(EquivalenceNode eNode, GeneralGlyph glyph)
	{
		Object parentCell = eNode.getParent().getDrawingCell();
		mxCell eNodeCell = null;
		eNodeCell = (mxCell)graph.createVertex(parentCell, null, eNode, 5, 5, 10, 10, "");
		double x = glyph.getBoundingBox().x();
		double y = glyph.getBoundingBox().y();
		double width = DEFAULT_EQUIVALENCENODE_WIDTH;
		double height = DEFAULT_EQUIVALENCENODE_HEIGHT;
		mxGeometry geo = new mxGeometry(x, y, width, height);
		eNodeCell.setGeometry(geo);
		eNodeCell.setVertex(true);
		eNodeCell.setConnectable(true);
		
		eNode.setDrawingCell(eNodeCell);
		eNode.setDrawingCellGeometry(geo);
	}
	
	public void addVisibleVariable(Module parentMod, VisibleVariable var)
	{
		//System.out.println("Time to add a visible variable:");
		//System.out.println("Variable Ref Name: " + var.getRefName());
		Object cell = parentMod.getDrawingCell();
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
			
			xPosition = 40 + (varIndex * 20);
			yPosition = 40 + (varIndex * 20);

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
			
			xPosition = (formerGeo.getSourcePoint().getX() + formerGeo.getTargetPoint().getX()) / 2 - (width/2);
			yPosition = (formerGeo.getSourcePoint().getY() + formerGeo.getTargetPoint().getY()) / 2 - (height/2);

			mxGeometry geo = new mxGeometry(xPosition, yPosition, width, height);
			//geo.setOffset(new mxPoint(offsetX, offsetY));
			eNodeCell.setGeometry(geo);
			graph.getModel().add(cell, eNodeCell, 0);
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
		String drawingCellStyle = "";
		if (edge.getDrawingCellStyle() != "")
		{
			drawingCellStyle = edge.getDrawingCellStyle();
		}
		else
		{
			drawingCellStyle = "ConnectionEdge";
		}
		Object edgeCell = graph.insertEdge(parentCell, null, edge, source, target, drawingCellStyle);
		edge.setDrawingCell(edgeCell);
	}
	
	public void createConnection(Connection edge, Object source, Object target, String drawingCellStyle)
	{
		Object parentCell = edge.getParent().getDrawingCell();
		//Object edgeCell = graph.createEdge(parentCell, null, edge, source, target, drawingCellStyle);
		Object edgeCell = graph.insertEdge(parentCell, null, edge, source, target, drawingCellStyle);
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

	public void setValue(Object cell, Object value)
	{
		graph.getModel().beginUpdate();
		try
		{
			graph.getModel().setValue(cell, value);
		}
		finally
		{
			graph.getModel().endUpdate();
		}
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
		double xPosition = 25;
		double yPosition = 25;
		double width = graphComponent.getVisibleRect().getWidth() - 50;
		double height = graphComponent.getVisibleRect().getHeight() - 50;
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
			//geo = mod.getDrawingCellGeometry();
			geo = new mxGeometry();
			xPosition = 25;
			yPosition = 25;
			geo.setX(xPosition);
			geo.setY(yPosition);
			geo.setWidth(width);
			geo.setHeight(height);
			((mxCell)cell).setGeometry(geo);
			//graph.resizeCell(childCell, bounds);
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
			mxGeometry geo;

			while (children.hasNext())
			{
				child = children.next();
				childCell = child.getDrawingCell();

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
	
	private void addCell(Object pCell, Object cell)
	{
		Object parentCell = null;
		if (pCell == null)
		{
			parentCell = activeModule.getDrawingCell();
		}
		else
		{
			parentCell = pCell;
		}
		
		graph.getModel().beginUpdate();
		try
		{
			graph.getModel().add(parentCell, cell, 0);
		}
		finally
		{
			graph.getModel().endUpdate();
		}
	}
	
	private Object createEquivalenceNodeCell(Object pCell)
	{
		Object parentCell = null;
		if (pCell == null)
		{
			parentCell = activeModule.getDrawingCell();
		}
		else
		{
			parentCell = pCell;
		}
		
		mxCell eNodeCell = (mxCell)graph.createVertex(parentCell, null, null, 5, 5, DEFAULT_EQUIVALENCENODE_WIDTH, DEFAULT_EQUIVALENCENODE_HEIGHT, "EquivalenceNode");
		eNodeCell.setVertex(true);
		eNodeCell.setConnectable(true);
		
		return eNodeCell;
	}
	
	private Object createVisibleVariableCell(Object pCell)
	{
		Object parentCell = null;
		if (pCell == null)
		{
			parentCell = activeModule.getDrawingCell();
		}
		else
		{
			parentCell = pCell;
		}
		
		mxCell varCell = (mxCell)graph.createVertex(parentCell, null, null, 5, 5, DEFAULT_VISIBLEVARIABLE_WIDTH, DEFAULT_VISIBLEVARIABLE_HEIGHT, "VisibleVariable");
		varCell.setVertex(true);
		varCell.setConnectable(true);
		
		return varCell;
	}
	
	private Object createConnectionCell(Object pCell, Object source, Object target, String style)
	{
		Object parentCell = null;
		if (pCell == null)
		{
			parentCell = activeModule.getDrawingCell();
		}
		else
		{
			parentCell = pCell;
		}
		
		Object edgeCell = graph.insertEdge(parentCell, null, null, source, target, style);
		
		return edgeCell;
	}

	private void setCellGeometryToCenterOfEdge(Object cell, Object edge)
	{
		mxGeometry formerGeo = ((mxCell)cell).getGeometry();
		mxGeometry edgeGeo = ((mxCell)edge).getGeometry();
		double width = formerGeo.getWidth();
		double height = formerGeo.getHeight();
		double xPosition = (edgeGeo.getSourcePoint().getX() + edgeGeo.getTargetPoint().getX()) / 2 - (width/2);;
		double yPosition = (edgeGeo.getSourcePoint().getY() + edgeGeo.getTargetPoint().getY()) / 2 - (height/2);;
		
		mxGeometry geo = new mxGeometry(xPosition, yPosition, width, height);
		
		graph.getModel().beginUpdate();
		try
		{
			graph.getModel().setGeometry(cell, geo);
		}
		finally
		{
			graph.getModel().endUpdate();
		}
	}
	
	private void setCellGeometryToCenterPointOfCells(Object cell, Object sourceCell, Object targetCell)
	{
		mxGeometry sourceGeo = ((mxCell)sourceCell).getGeometry();
		if (((mxCell)sourceCell).getValue() instanceof Port)
		{
			// sourceCell represents a Port.
			// The drawing cell of a Port has relative geometry to its parent.
			// Set sourceGeo to the geometry of the Port's parent.
			sourceGeo = ((mxCell)((Port)((mxCell)sourceCell).getValue()).getParent().getDrawingCell()).getGeometry();
		}
		mxGeometry targetGeo = ((mxCell)targetCell).getGeometry();
		if (((mxCell)targetCell).getValue() instanceof Port)
		{
			// targetCell represents a Port.
			// The drawing cell of a Port has relative geometry to its parent.
			// Set targetGeo to the geometry of the Port's parent.
			targetGeo = ((mxCell)((Port)((mxCell)targetCell).getValue()).getParent().getDrawingCell()).getGeometry();
		}
		double width = ((mxCell)cell).getGeometry().getWidth();
		double height = ((mxCell)cell).getGeometry().getHeight();
		double xPosition = (sourceGeo.getX() + targetGeo.getX()) / 2 - (width/2);;
		double yPosition = (sourceGeo.getY() + targetGeo.getY()) / 2 - (height/2);;
		
		mxGeometry geo = new mxGeometry(xPosition, yPosition, width, height);
		
		graph.getModel().beginUpdate();
		try
		{
			graph.getModel().setGeometry(cell, geo);
		}
		finally
		{
			graph.getModel().endUpdate();
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
				if (geo == null || (geo.getHeight() <= 10))
				{
					geo = new mxGeometry();
					xPosition = 40 + (childIndex * 20);
					yPosition = 40 + (childIndex * 20);
					geo.setX(xPosition);
					geo.setY(yPosition);
					geo.setWidth(DEFAULT_SUBMODULE_WIDTH);
					geo.setHeight(DEFAULT_SUBMODULE_HEIGHT);
				}
				((mxCell)childCell).setGeometry(geo);
				graph.getModel().add(parentCell, childCell, 0);
				if(child.getDrawingCellStyle().equals(""))
				{
					child.setDrawingCellStyle("Submodule_No_Show_Information");
				}
				graph.getModel().setStyle(childCell, child.getDrawingCellStyle());
			}
			finally
			{
				graph.getModel().endUpdate();
			}
			//child.setDrawingCellBounds(bounds);
			if (!(child instanceof MathematicalAggregator))
			{
				drawSubmoduleButton(childCell);
			}
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
				if (geo == null)
				{
					geo = new mxGeometry();
					xPosition = 40 + (varIndex * 20);
					yPosition = 40 + (varIndex * 20);
					geo.setX(xPosition);
					geo.setY(yPosition);
					geo.setWidth(DEFAULT_VISIBLEVARIABLE_WIDTH);
					geo.setHeight(DEFAULT_VISIBLEVARIABLE_HEIGHT);
				}
				((mxCell)varCell).setGeometry(geo);
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
				if (geo == null)
				{
					geo = new mxGeometry();
					xPosition = 40 + (eNodeIndex * 20);
					yPosition = 40 + (eNodeIndex * 20);
					geo.setX(xPosition);
					geo.setY(yPosition);
					geo.setWidth(DEFAULT_EQUIVALENCENODE_WIDTH);
					geo.setHeight(DEFAULT_EQUIVALENCENODE_HEIGHT);
				}
				((mxCell)eNodeCell).setGeometry(geo);
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
				/*
				if (!graph.getModel().isVisible(connectionCell))
				{
					System.err.println("Connection not visible.");
					graph.getModel().setVisible(connectionCell, true);
				}
				*/
				graph.getModel().add(parentCell, connectionCell, 0);
			}
			finally
			{
				graph.getModel().endUpdate();
			}
		}
	}
	
	private void drawSubmoduleButton(Object parentCell)
	{
		mxCell buttonCell;
		mxGeometry geo = new mxGeometry(0, 0, DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT);
		geo.setRelative(true);
		
		graph.getModel().beginUpdate();
		try
		{
			buttonCell = (mxCell)graph.createVertex(null, null, null, 0, 0, 10, 10, "");
			buttonCell.setConnectable(false);
			
			graph.getModel().add(parentCell, buttonCell, 0);
			graph.getModel().setGeometry(buttonCell, geo);
			graph.getModel().setValue(buttonCell, "+");
			graph.getModel().setStyle(buttonCell, "InactiveButton");
		}
		finally
		{
			graph.getModel().endUpdate();
		}
	}
	
	private void drawSubmoduleChildren(Module mod)
	{
		
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
	
	private String generateName(String oldName)
	{
		String name = oldName;
		int index;
		
		while(!AC_GUI.newNameValidation(name))
		{
			try
			{
				if (name.length() > 1)
				{
					if (name.charAt(name.length()-2) == '_')
					{
						index = Integer.parseInt(name.substring(name.length()-1));
						name = name.substring(0, name.length()-1);
						index++;
						name += index;
					}
					else
					{
						name += "_1";
					}
				}
				else
				{
					name += "_1";
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Problem parsing oldName.");
			}
		}
		
		return name;
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

		styleSheet.putCellStyle("Submodule_No_Show_Information", cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_STROKECOLOR, "blue");
		cell.put(mxConstants.STYLE_FILLCOLOR, "white");
		cell.put(mxConstants.STYLE_STROKEWIDTH, "3.0");
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		//cell.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
		//cell.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
		//cell.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_MIDDLE);
		//cell.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "white");
		//cell.put(mxConstants.STYLE_LABEL_BORDERCOLOR, "red");
		//cell.put(mxConstants.STYLE_SPACING_TOP, "-10");
		//cell.put(mxConstants.STYLE_FONTFAMILY, "Times New Roman");
		//cell.put(mxConstants.STYLE_FONTSIZE, "12");
		//cell.put(mxConstants.STYLE_FONTSTYLE, "1");
		//cell.put(mxConstants.STYLE_FONTCOLOR, "black");
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		cell.put(mxConstants.STYLE_ROUNDED, "true");
		cell.put(mxConstants.STYLE_FOLDABLE, "0");
		//cell.put(mxConstants.STYLE_WHITE_SPACE, "wrap");

		styleSheet.putCellStyle("Submodule_Show_Information", cell);
		
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
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		
		styleSheet.putCellStyle("DashedConnectionEdge", cell);
		
		cell = new HashMap<String, Object>();
		//cell.put(mxConstants.STYLE_STROKECOLOR, "white");
		cell.put(mxConstants.STYLE_FILLCOLOR, "white");
		cell.put(mxConstants.STYLE_GRADIENTCOLOR, "gray");
		cell.put(mxConstants.STYLE_GRADIENT_DIRECTION, mxConstants.DIRECTION_SOUTH);
		//cell.put(mxConstants.STYLE_OPACITY, "50.0");
		cell.put(mxConstants.STYLE_STROKEWIDTH, "1.0");
		cell.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
		//cell.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
		cell.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_MIDDLE);
		//cell.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "white");
		//cell.put(mxConstants.STYLE_LABEL_BORDERCOLOR, "red");
		//cell.put(mxConstants.STYLE_SPACING_TOP, "-10");
		cell.put(mxConstants.STYLE_FONTFAMILY, "Times New Roman");
		cell.put(mxConstants.STYLE_FONTSIZE, "16");
		cell.put(mxConstants.STYLE_FONTSTYLE, "1");
		cell.put(mxConstants.STYLE_FONTCOLOR, "black");
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		cell.put(mxConstants.STYLE_ROUNDED, "true");
		cell.put(mxConstants.STYLE_FOLDABLE, "0");
		cell.put(mxConstants.STYLE_WHITE_SPACE, "wrap");
		cell.put(mxConstants.STYLE_MOVABLE, "0");

		styleSheet.putCellStyle("InactiveButton", cell);
		
		cell = new HashMap<String, Object>();
		//cell.put(mxConstants.STYLE_STROKECOLOR, "white");
		cell.put(mxConstants.STYLE_FILLCOLOR, "white");
		cell.put(mxConstants.STYLE_GRADIENTCOLOR, "gray");
		cell.put(mxConstants.STYLE_GRADIENT_DIRECTION, mxConstants.DIRECTION_SOUTH);
		//cell.put(mxConstants.STYLE_OPACITY, "50.0");
		cell.put(mxConstants.STYLE_STROKEWIDTH, "1.0");
		cell.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
		//cell.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
		cell.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_MIDDLE);
		//cell.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "white");
		//cell.put(mxConstants.STYLE_LABEL_BORDERCOLOR, "red");
		//cell.put(mxConstants.STYLE_SPACING_TOP, "-10");
		cell.put(mxConstants.STYLE_FONTFAMILY, "Times New Roman");
		cell.put(mxConstants.STYLE_FONTSIZE, "16");
		cell.put(mxConstants.STYLE_FONTSTYLE, "1");
		cell.put(mxConstants.STYLE_FONTCOLOR, "black");
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		cell.put(mxConstants.STYLE_ROUNDED, "true");
		cell.put(mxConstants.STYLE_FOLDABLE, "0");
		cell.put(mxConstants.STYLE_WHITE_SPACE, "wrap");
		cell.put(mxConstants.STYLE_MOVABLE, "0");

		styleSheet.putCellStyle("ActiveButton", cell);
		
		cell = new HashMap<String, Object>();
		//cell.put(mxConstants.STYLE_STROKECOLOR, "blue");
		cell.put(mxConstants.STYLE_FILLCOLOR, "green");
		cell.put(mxConstants.STYLE_OPACITY, "50.0");
		//cell.put(mxConstants.STYLE_STROKEWIDTH, "3.0");
		cell.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
		//cell.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
		//cell.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_MIDDLE);
		//cell.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "white");
		//cell.put(mxConstants.STYLE_LABEL_BORDERCOLOR, "red");
		//cell.put(mxConstants.STYLE_SPACING_TOP, "-10");
		//cell.put(mxConstants.STYLE_FONTFAMILY, "Times New Roman");
		//cell.put(mxConstants.STYLE_FONTSIZE, "12");
		//cell.put(mxConstants.STYLE_FONTSTYLE, "1");
		//cell.put(mxConstants.STYLE_FONTCOLOR, "black");
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		cell.put(mxConstants.STYLE_ROUNDED, "true");
		cell.put(mxConstants.STYLE_FOLDABLE, "0");
		//cell.put(mxConstants.STYLE_WHITE_SPACE, "wrap");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		
		styleSheet.putCellStyle("Submodule_Mini", cell);
	}

	/**
	 * Install various listeners for the graph.
	 */
	private void installListeners()
	{

		// when right-click is pressed, create a popup menu.
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e)
			{
				//Object cell = graphComponent.getCellAt(e.getX(), e.getY());
				mxCell cell = (mxCell) graphComponent.getCellAt(e.getX(), e.getY());
				
				
				if (cell != null)
				{
					if (cell.getValue() instanceof Port)
					{
						// System.out.println("port=" + ((mxCell) cell).getValue().toString());
						// p = (Port) ((mxCell)cell).getValue();
						AC_GUI.modelBuilder.setSelectedPort((Port)cell.getValue());
					}

					if (cell.getValue() instanceof Module)
					{
						// System.out.println("cell=" + graph.getLabel(cell));
						// Module mod = AC_GUI.moduleList.findModule(cell);
						// AC_GUI.treeView.setSelected(mod.getTreeNode());
						AC_GUI.currentGUI.setSelectedModule(cell);
						//printCellCount(cell);
					}
					
					if (cell.getValue() instanceof VisibleVariable)
					{
						VisibleVariable var = (VisibleVariable)cell.getValue();
						AC_GUI.setSelectedModelBuilderVariable(var.getRefName(), var.getVariableType());
					}
					
					if (cell.getValue() instanceof EquivalenceNode)
					{
						AC_GUI.setSelectedModelBuilderVariable(((EquivalenceNode)cell.getValue()).getRefName(), VariableType.SPECIES);
					}
					
					if (SwingUtilities.isRightMouseButton(e))
					{
						// System.out.println("cell=" + graph.getLabel(cell));
						// System.out.println("button = " + e.getButton());
						JPopupMenu menu = createPopupMenu(e.getPoint(), cell);
						menu.show(graphComponent, e.getX(), e.getY());
					}
				}

			}
			
			public void mouseClicked(MouseEvent e)
			{
				mxCell cell = (mxCell) graphComponent.getCellAt(e.getX(), e.getY());
				String newButtonStyle = "";
				String newButtonValue = "";
				String newSubmoduleStyle = "";
				mxCell parentCell;
				Module parentMod;
				boolean buttonPushed = false;
				
				if (cell != null)
				{
					parentCell = (mxCell) graph.getModel().getParent(cell);
					
					if ((parentCell == parent) || (parentCell == AC_GUI.activeModule.getDrawingCell()))
					{
						return;
					}
					
					parentMod = (Module)parentCell.getValue();
					
					if (graph.getModel().getStyle(cell).equalsIgnoreCase("InactiveButton"))
					{
						System.out.println("A button was clicked.");
						newButtonStyle = "ActiveButton";
						newButtonValue = "-";
						newSubmoduleStyle = "Submodule_Show_Information";
						//AC_GUI.modelBuilder.loadModel(parentMod.getMSMBData().getBytes(), true, true);
						AC_GUI.loadModelBuilder(parentMod, true, true);
						buttonPushed = true;
					}
					else if (graph.getModel().getStyle(cell).equalsIgnoreCase("ActiveButton"))
					{
						System.out.println("A button was clicked.");
						newButtonStyle = "InactiveButton";
						newButtonValue = "+";
						newSubmoduleStyle = "Submodule_No_Show_Information";
						//AC_GUI.modelBuilder.loadModel(AC_GUI.activeModule.getMSMBData().getBytes(), false, true);
						AC_GUI.loadModelBuilder(AC_GUI.activeModule, false, true);
						buttonPushed = true;
					}
					
					if (buttonPushed)
					{
						graph.getModel().beginUpdate();
						try
						{
							graph.getModel().setValue(cell, newButtonValue);
							graph.getModel().setStyle(cell, newButtonStyle);
							graph.getModel().setStyle(parentCell, newSubmoduleStyle);
						}
						finally
						{
							graph.getModel().endUpdate();
						}
					}
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
				mxCell source = (mxCell)cell.getSource();
				mxCell target = (mxCell)cell.getTarget();
				
				if ((source == null) || (target == null))
				{
					removeCell(cell);
					return;
				}
				
				Object sourceObject = source.getValue();
				Object targetObject = target.getValue();
				String newName;
				
				removeCell(cell);
				
				if (sourceObject instanceof VisibleVariable)
				{
					// the source is a visible variable
					VisibleVariable sourceVisibleVariable = (VisibleVariable)sourceObject;
					
					if (targetObject instanceof VisibleVariable)
					{
						// the source is a visible variable
						// the target is a visible variable
						// cannot occur
						return;
					}
					else if (targetObject instanceof EquivalenceNode)
					{
						// the source is a visible variable
						// the target is an equivalence node
						// cannot occur
						return;
					}
					else if (targetObject instanceof Port)
					{
						// the source is a visible variable
						// the target is a port
						Port targetPort = (Port)targetObject;
						PortType targetPortType = ((Port)targetObject).getType();
						Module targetModule = targetPort.getParent();
						
						if(targetModule == AC_GUI.activeModule)
						{
							// the source is a visible variable
							// the target is a port
							// the targetModule is the active module
							switch(targetPortType)
							{
							case INPUT:
								// cannot occur
								return;
							case OUTPUT:
								/*
								if (mxGraphModel.getDirectedEdgeCount(graph.getModel(), target, false, cell) != 0)
								{
									return "An output port cannot have more than one incoming connection.";
								}
								*/
								mxCell varToPortCell = (mxCell)createConnectionCell(null, source, target, "ConnectionEdge");
								AC_GUI.addConnection(activeModule, varToPortCell);
								return;
							case EQUIVALENCE:
								// cannot occur
								return;
							default:
									
							}
						}
						else
						{
							// the source is a visible variable
							// the target is a port
							// the targetModule is a submodule
							switch(targetPortType)
							{
							case INPUT:
								/*
								if (mxGraphModel.getDirectedEdgeCount(graph.getModel(), target, false, cell) != 0)
								{
									return "An input port cannot have more than one incoming connection.";
								}
								*/
								mxCell varToPortCell = (mxCell)createConnectionCell(null, source, target, "ConnectionEdge");
								AC_GUI.addConnection(activeModule, varToPortCell);
								return;
							case OUTPUT:
								// cannot occur
								return;
							case EQUIVALENCE:
								// cannot occur
								return;
							default:
									
							}
						}
					}
				}
				else if (sourceObject instanceof EquivalenceNode)
				{
					// the source is an equivalence node
					EquivalenceNode sourceENode = (EquivalenceNode)sourceObject;
					
					if (targetObject instanceof EquivalenceNode)
					{
						// the source is an equivalence node
						// the target is an equivalence node
						// cannot occur
						return;
					}
					if (targetObject instanceof VisibleVariable)
					{
						// the source is an equivalence node
						// the target is a visible variable
						mxCell eNodeToVarCell = (mxCell)createConnectionCell(null, source, target, "DashedConnectionEdge");
						AC_GUI.addConnection(activeModule, eNodeToVarCell);
						return;
					}
					else if (targetObject instanceof Port)
					{
						// the source is an equivalence node
						// the target is a port
						Port targetPort = (Port)targetObject;
						PortType targetPortType = ((Port)targetObject).getType();
						Module targetModule = targetPort.getParent();
						
						if (targetModule == AC_GUI.activeModule)
						{
							// the source is an equivalence node
							// the target is a port
							// the targetModule is the active module
							switch(targetPortType)
							{
							case INPUT:
								// cannot occur
								return;
							case OUTPUT:
								/*
								if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
								{
									return "An output port cannot have more than one incoming connection.";
								}
								*/
								
								mxCell eNodeToOutputCell = (mxCell)createConnectionCell(null, source, target, "ConnectionEdge");
								AC_GUI.addConnection(activeModule, eNodeToOutputCell);
								return;
							case EQUIVALENCE:
								/*
								if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
								{
									return "An equivalence port cannot have more than one incoming connection.";
								}
								*/
								mxCell eNodeToPortCell = (mxCell)createConnectionCell(null, source, target, "ConnectionEdge");
								AC_GUI.addConnection(activeModule, eNodeToPortCell);
								return;
							default:
									
							}
						}
						else
						{
							// the source is an equivalence node
							// the target is a port
							// the targetModule is a submodule
							switch(targetPortType)
							{
							case INPUT:
								/*
								if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
								{
									return "An input port cannot have more than one incoming connection.";
								}
								*/
								
								mxCell eNodeToPortCell = (mxCell)createConnectionCell(null, source, target, "ConnectionEdge");
								AC_GUI.addConnection(activeModule, eNodeToPortCell);
								return;
							case OUTPUT:
								// cannot occur
								return;
							case EQUIVALENCE:
								/*
								if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
								{
									return "An equivalence port cannot have more than one incoming connection.";
								}
								*/
								mxCell eNodeToPortCell1 = (mxCell)createConnectionCell(null, source, target, "ConnectionEdge");
								AC_GUI.addConnection(activeModule, eNodeToPortCell1);
								return;
							default:
									
							}
						}
					}
				}
				else if (sourceObject instanceof Port)
				{
					// the source is a port
					Port sourcePort = (Port)sourceObject;
					PortType sourcePortType = sourcePort.getType();
					Module sourceModule = sourcePort.getParent();
					
					if (sourceModule == AC_GUI.activeModule)
					{
						// the source is a port
						// the sourceModule is the active module
						if (targetObject instanceof EquivalenceNode)
						{
							// the source is a port
							// the sourceModule is the active module
							// the target is an equivalence node
							// cannot occur
							return;
						}
						else if (targetObject instanceof VisibleVariable)
						{
							// the source is a port
							// the sourceModule is the active module
							// the target is a visible variable
							switch(sourcePortType)
							{
							case INPUT:
								/*
								if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
								{
									return "A variable cannot have more than one incoming connection.";
								}
								*/
								mxCell portToVarCell = (mxCell)createConnectionCell(null, source, target, "DashedConnectionEdge");
								AC_GUI.addConnection(activeModule, portToVarCell);
								return;
							case OUTPUT:
								// cannot occur
								return;
							case EQUIVALENCE:
								/*
								if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
								{
									return "A variable cannot have more than one incoming connection.";
								}
								*/
								mxCell eportToVarCell = (mxCell)createConnectionCell(null, source, target, "DashedConnectionEdge");
								AC_GUI.addConnection(activeModule, eportToVarCell);
								return;
							default:
									
							}
						}
						else if (targetObject instanceof Port)
						{
							// the source is a port
							// the sourceModule is the active module
							// the target is a port
							Port targetPort = (Port)targetObject;
							PortType targetPortType = targetPort.getType();
							Module targetModule = targetPort.getParent();
							
							if (sourceModule == targetModule)
							{
								// the source and target ports belong to the same module
								// cannot occur
								return;
							}
							
							// since the sourceModule is the active module,
							// the targetModule must be a submodule
							switch(sourcePortType)
							{
							case INPUT:
								switch(targetPortType)
								{
								case INPUT:
									/*
									if (mxGraphModel.getDirectedEdgeCount(graph.getModel(), target, false, cell) != 0)
									{
										
										return;
									}
									*/
									if (mxGraphModel.getDirectedEdgeCount(graph.getModel(), source, true, cell) != 0)
									{
										// the source has an existing outgoing edge
										System.out.println("Source has existing edges.");
										mxCell existingEdgeTarget = (mxCell)((mxCell)graph.getModel().getEdgeAt(source, 0)).getTarget();
										if (existingEdgeTarget.getValue() instanceof VisibleVariable)
										{
											// add connection from existing visible variable to the selected target
											mxCell varToTargetCell = (mxCell)createConnectionCell(null, existingEdgeTarget, target, "ConnectionEdge");
											//addCell(null, varToTargetCell);
											AC_GUI.addConnection(activeModule, varToTargetCell);
										}
										return;
									}
									// the source has no existing outgoing edges
									System.out.println("Source has no existing edges.");
									// create a visible variable
									newName = generateName(sourcePort.getRefName());
									mxCell varCell = (mxCell)createVisibleVariableCell(null);
									addCell(null, varCell);
									setCellGeometryToCenterOfEdge(varCell, cell);
									AC_GUI.addVisibleVariable(activeModule, newName, varCell);
									mxCell sourceToVarCell = (mxCell)createConnectionCell(null, source, varCell, "DashedConnectionEdge");
									AC_GUI.addConnection(activeModule, sourceToVarCell);
									mxCell varToTargetCell = (mxCell)createConnectionCell(null, varCell, target, "ConnectionEdge");
									AC_GUI.addConnection(activeModule, varToTargetCell);
									/*
									if (AC_GUI.newNameValidation(sourcePort.getRefName()))
									{
										mxCell varCell = (mxCell)createVisibleVariableCell(null);
										addCell(null, varCell);
										setCellGeometryToCenterOfEdge(varCell, cell);
										AC_GUI.addVisibleVariable(activeModule, sourcePort.getRefName(), varCell);
										mxCell sourceToVarCell = (mxCell)createConnectionCell(null, source, varCell, "DashedConnectionEdge");
										AC_GUI.addConnection(activeModule, sourceToVarCell);
										mxCell varToTargetCell = (mxCell)createConnectionCell(null, varCell, target, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, varToTargetCell);
									}
									else
									{
										String msg = "Error: \"";
										msg += sourcePort.getRefName();
										msg += "\" is already the name of a Species or Global Quantity.";
										JOptionPane.showMessageDialog(null, msg);
										return;
									}
									*/
									//mxCell portToPortCell = (mxCell)createConnectionCell(null, source, target, "ConnectionEdge");
									//AC_GUI.addConnection(activeModule, portToPortCell);
									return;
								case OUTPUT:
									// cannot occur
									return;
								case EQUIVALENCE:
									// cannot occur
									return;
								default:
								}
								break;
							case OUTPUT:
								switch(targetPortType)
								{
								case INPUT:
									// cannot occur
									return;
								case OUTPUT:
									// cannot occur
									return;
								case EQUIVALENCE:
									// cannot occur
									return;
								default:
								}
								break;
							case EQUIVALENCE:
								mxCell eNodeCell;
								mxCell sourceToeNodeCell;
								mxCell eNodeToTargetCell;
								switch(targetPortType)
								{
								case INPUT:
									if (mxGraphModel.getDirectedEdgeCount(graph.getModel(), source, false, cell) != 0)
									{
										// the source has an existing incoming edge
										mxCell existingEdgeSource = (mxCell)((mxCell)graph.getModel().getEdgeAt(source, 0)).getSource();
										if (existingEdgeSource.getValue() instanceof EquivalenceNode)
										{
											// the existing edge connects from an equivalence node
											EquivalenceNode eNode = (EquivalenceNode)existingEdgeSource.getValue();
										
											// add connection from existing eNode to the selected target
											eNodeToTargetCell = (mxCell)createConnectionCell(null, existingEdgeSource, target, "ConnectionEdge");
											AC_GUI.addConnection(activeModule, eNodeToTargetCell);
										
										}
										return;
									}
									// the source has no existing outgoing edges
									System.out.println("Source has no existing outgoing edges.");
									// create an equivalence node
									newName = generateName(sourcePort.getRefName());
									eNodeCell = (mxCell)createEquivalenceNodeCell(null);
									addCell(null, eNodeCell);
									setCellGeometryToCenterOfEdge(eNodeCell, cell);
									AC_GUI.addEquivalenceNode(activeModule, newName, eNodeCell);
									sourceToeNodeCell = (mxCell)createConnectionCell(null, eNodeCell, source, "ConnectionEdge");
									AC_GUI.addConnection(activeModule, sourceToeNodeCell);
									eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
									AC_GUI.addConnection(activeModule, eNodeToTargetCell);
									/*
									if (AC_GUI.newNameValidation(sourcePort.getRefName()))
									{
										mxCell eNodeCell = (mxCell)createEquivalenceNodeCell(null);
										addCell(null, eNodeCell);
										setCellGeometryToCenterOfEdge(eNodeCell, cell);
										AC_GUI.addEquivalenceNode(activeModule, sourcePort.getRefName(), eNodeCell);
										mxCell sourceToeNodeCell = (mxCell)createConnectionCell(null, eNodeCell, source, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, sourceToeNodeCell);
										mxCell eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, eNodeToTargetCell);
									}
									else
									{
										String msg = "Error: \"";
										msg += sourcePort.getRefName();
										msg += "\" is already the name of a Species or Global Quantity.";
										JOptionPane.showMessageDialog(null, msg);
										return;
									}
									*/
									//mxCell portToPortCell = (mxCell)createConnectionCell(null, source, target, "ConnectionEdge");
									//AC_GUI.addConnection(activeModule, portToPortCell);
									return;
								case OUTPUT:
									// cannot occur
									return;
								case EQUIVALENCE:
									/*
									if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
									{
										return "An equivalence port cannot have more than one incoming connection.";
									}
									*/
									boolean sourceHasIncomingEdge;
									boolean targetHasIncomingEdge;
									
									
									sourceHasIncomingEdge = (mxGraphModel.getDirectedEdgeCount(graph.getModel(), source, false, cell) != 0);
									targetHasIncomingEdge = (mxGraphModel.getDirectedEdgeCount(graph.getModel(), target, false, cell) != 0);
									eNodeCell = null;
									
									if (!sourceHasIncomingEdge && !targetHasIncomingEdge)
									{
										// the source has no incoming edges
										// the target has no incoming edges
										// create an equivalence node
										// create connection from eNode->source port
										// create connection from eNode->target port
										
										// create an equivalence node
										newName = generateName(sourcePort.getRefName());
										eNodeCell = (mxCell)createEquivalenceNodeCell(null);
										addCell(null, eNodeCell);
										setCellGeometryToCenterOfEdge(eNodeCell, cell);
										AC_GUI.addEquivalenceNode(activeModule, newName, eNodeCell);
										sourceToeNodeCell = (mxCell)createConnectionCell(null, eNodeCell, source, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, sourceToeNodeCell);
										eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, eNodeToTargetCell);
										/*
										if (AC_GUI.newNameValidation(sourcePort.getRefName()))
										{
											eNodeCell = (mxCell)createEquivalenceNodeCell(null);
											addCell(null, eNodeCell);
											setCellGeometryToCenterOfEdge(eNodeCell, cell);
											AC_GUI.addEquivalenceNode(activeModule, sourcePort.getRefName(), eNodeCell);
											mxCell sourceToeNodeCell = (mxCell)createConnectionCell(null, eNodeCell, source, "ConnectionEdge");
											AC_GUI.addConnection(activeModule, sourceToeNodeCell);
											mxCell eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
											AC_GUI.addConnection(activeModule, eNodeToTargetCell);
										}
										else
										{
											String msg = "Error: \"";
											msg += sourcePort.getRefName();
											msg += "\" is already the name of a Species or Global Quantity.";
											JOptionPane.showMessageDialog(null, msg);
											return;
										}
										*/
										return;
									}
									
									if (sourceHasIncomingEdge)
									{
										// the source has an incoming edge
										
										Object sourceIncomingEdges [] = mxGraphModel.getIncomingEdges(graph.getModel(), source);
										mxCell existingeNodeCell = null;
										for(int i = 0; i < sourceIncomingEdges.length; i++)
										{
											if (((mxCell)sourceIncomingEdges[i]).getSource().getValue() instanceof EquivalenceNode)
											{
												existingeNodeCell = (mxCell)((mxCell)sourceIncomingEdges[i]).getSource();
											}
										}
										if (existingeNodeCell != null)
										{
											// the source of the incoming edge is an equivalence node
											// check if target equivalence port is already connected to the eNode
											int edgesBetween = mxGraphModel.getEdgesBetween(graph.getModel(), existingeNodeCell, target, false).length;
											if (edgesBetween == 0)
											{
												// the target equivalence port is not connected to the eNode
												// create an edge between the target equivalence port and the eNode
												eNodeToTargetCell = (mxCell)createConnectionCell(null, existingeNodeCell, target, "ConnectionEdge");
												AC_GUI.addConnection(activeModule, eNodeToTargetCell);
											}
											else
											{
												// the target equivalence port is already connected to the eNode
												JOptionPane.showMessageDialog(null,
														"The two equivalence ports are already connected through an equivalence node.");
											}
										}
										else
										{
											System.out.println("Problem: module.equivalenceport->submodule.equivalenceport" +
													"existingeNodeCell equals null");
										}
										return;
									}
									
									// the source has no incoming edges
									System.out.println("Source has no existing incoming edges.");
									if (targetHasIncomingEdge)
									{
										// the target has existing incoming edges
										Object targetIncomingEdges [] = mxGraphModel.getIncomingEdges(graph.getModel(), target);
										for(int i = 0; i < targetIncomingEdges.length; i++)
										{
											if (((mxCell)targetIncomingEdges[i]).getSource().getValue() instanceof EquivalenceNode)
											{
												eNodeCell = (mxCell)((mxCell)targetIncomingEdges[i]).getSource();
											}
										}
										if (eNodeCell != null)
										{
											// create connection from eNode->source port
											mxCell eNodeToSourceCell = (mxCell)createConnectionCell(null, eNodeCell, source, "ConnectionEdge");
											AC_GUI.addConnection(activeModule, eNodeToSourceCell);
										}
										else
										{
											System.out.println("Problem: module.equivalenceport->submodule.equivalenceport" +
													" eNodeCell equals null");
										}
										return;
									}
									return;
								default:
								}
								break;
							default:
							}
						}
					}
					else
					{
						// the source is a port
						// the sourceModule is a submodule
						if (targetObject instanceof EquivalenceNode)
						{
							// the source is a port
							// the sourceModule is a submodule
							// the target is an equivalence node
							// cannot occur
							return;
						}
						else if (targetObject instanceof VisibleVariable)
						{
							// the source is a port
							// the sourceModule is a submodule
							// the target is a visible variable
							switch(sourcePortType)
							{
							case INPUT:
								// cannot occur
								return;
							case OUTPUT:
								/*
								if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
								{
									return "A variable cannot have more than one incoming connection.";
								}
								*/
								mxCell portToVarCell = (mxCell)createConnectionCell(null, source, target, "DashedConnectionEdge");
								AC_GUI.addConnection(activeModule, portToVarCell);
								return;
							case EQUIVALENCE:
								/*
								if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
								{
									return "A variable cannot have more than one incoming connection.";
								}
								*/
								mxCell portToVarCell1 = (mxCell)createConnectionCell(null, source, target, "DashedConnectionEdge");
								AC_GUI.addConnection(activeModule, portToVarCell1);
								return;
							default:
							}
						}
						else if (targetObject instanceof Port)
						{
							// the source is a port
							// the sourceModule is a submodule
							// the target is a port
							Port targetPort = (Port)targetObject;
							PortType targetPortType = targetPort.getType();
							Module targetModule = targetPort.getParent();
							
							if (targetModule == AC_GUI.activeModule)
							{
								// the source is a port
								// the sourceModule is a submodule
								// the target is a port
								// the targetModule is the active module
								switch(sourcePortType)
								{
								case INPUT:
									switch(targetPortType)
									{
									case INPUT:
										// cannot occur
										return;
									case OUTPUT:
										// cannot occur
										return;
									case EQUIVALENCE:
										// cannot occur
										return;
									default:
									}
									break;
								case OUTPUT:
									switch(targetPortType)
									{
									case INPUT:
										// cannot occur
										return;
									case OUTPUT:
										/*
										if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
										{
											return "An output port cannot have more than one incoming connection.";
										}
										*/
										if (mxGraphModel.getDirectedEdgeCount(graph.getModel(), source, true, cell) != 0)
										{
											// the source has existing outgoing edges
											System.out.println("Source has existing outgoing edges.");
											mxCell varCell = null;
											Object outgoingEdges [] = mxGraphModel.getOutgoingEdges(graph.getModel(), source);
											for(int i = 0; i < outgoingEdges.length; i++)
											{
												if (((mxCell)outgoingEdges[i]).getTarget().getValue() instanceof VisibleVariable)
												{
													varCell = (mxCell)((mxCell)outgoingEdges[i]).getTarget();
												}
											}
											if (varCell != null)
											{
												mxCell varToPortCell = (mxCell)createConnectionCell(null, varCell, target, "ConnectionEdge");
												AC_GUI.addConnection(activeModule, varToPortCell);
											}
											else
											{
												System.out.println("Problem: submodule.outputport->module.outputport, " +
														"varCell is null.");
											}
											return;
										}
										// the source has no existing outgoing edges
										System.out.println("Source has no existing edges.");
										// create a visible variable
										newName = generateName(sourcePort.getRefName());
										mxCell varCell = (mxCell)createVisibleVariableCell(null);
										addCell(null, varCell);
										setCellGeometryToCenterOfEdge(varCell, cell);
										AC_GUI.addVisibleVariable(activeModule, sourcePort.getRefName(), varCell);
										mxCell sourceToVarCell = (mxCell)createConnectionCell(null, source, varCell, "DashedConnectionEdge");
										AC_GUI.addConnection(activeModule, sourceToVarCell);
										mxCell varToTargetCell = (mxCell)createConnectionCell(null, varCell, target, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, varToTargetCell);
										/*
										if (AC_GUI.newNameValidation(sourcePort.getRefName()))
										{
											mxCell varCell = (mxCell)createVisibleVariableCell(null);
											addCell(null, varCell);
											setCellGeometryToCenterOfEdge(varCell, cell);
											AC_GUI.addVisibleVariable(activeModule, sourcePort.getRefName(), varCell);
											mxCell sourceToVarCell = (mxCell)createConnectionCell(null, source, varCell, "DashedConnectionEdge");
											AC_GUI.addConnection(activeModule, sourceToVarCell);
											mxCell varToTargetCell = (mxCell)createConnectionCell(null, varCell, target, "ConnectionEdge");
											AC_GUI.addConnection(activeModule, varToTargetCell);
										}
										else
										{
											String msg = "Error: \"";
											msg += sourcePort.getRefName();
											msg += "\" is already the name of a Species or Global Quantity.";
											JOptionPane.showMessageDialog(null, msg);
											return;
										}
										*/
										//mxCell portToPortCell = (mxCell)createConnectionCell(null, source, target, "ConnectionEdge");
										//AC_GUI.addConnection(activeModule, portToPortCell);
										return;
									case EQUIVALENCE:
										// cannot occur
										return;
									default:
									}
									break;
								case EQUIVALENCE:
									switch(targetPortType)
									{
									case INPUT:
										// cannot occur
										return;
									case OUTPUT:
										// cannot occur
										return;
									case EQUIVALENCE:
										/*
										if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
										{
											return "An equivalence port cannot have more than one incoming connection.";
										}
										*/
										/*
										// the source has no existing outgoing edges
										System.out.println("Source has no existing edges.");
										// create an equivalence node
										mxCell eNodeCell = (mxCell)createEquivalenceNodeCell(null);
										addCell(null, eNodeCell);
										setCellGeometryToCenterOfEdge(eNodeCell, cell);
										AC_GUI.addEquivalenceNode(activeModule, sourcePort.getRefName(), eNodeCell);
										mxCell sourceToeNodeCell = (mxCell)createConnectionCell(null, source, eNodeCell, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, sourceToeNodeCell);
										mxCell eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, eNodeToTargetCell);
										//mxCell portToPortCell = (mxCell)createConnectionCell(null, source, target, "ConnectionEdge");
										//AC_GUI.addConnection(activeModule, portToPortCell);
										return;
										*/
										boolean sourceHasIncomingEdge;
										boolean targetHasIncomingEdge;
										mxCell eNodeCell;
										
										sourceHasIncomingEdge = (mxGraphModel.getDirectedEdgeCount(graph.getModel(), source, false, cell) != 0);
										targetHasIncomingEdge = (mxGraphModel.getDirectedEdgeCount(graph.getModel(), target, false, cell) != 0);
										eNodeCell = null;
										
										if (!sourceHasIncomingEdge && !targetHasIncomingEdge)
										{
											// the source has no incoming edges
											// the target has no incoming edges
											// create an equivalence node
											// create connection from eNode->source port
											// create connection from eNode->target port
											
											// create an equivalence node
											newName = generateName(sourcePort.getRefName());
											eNodeCell = (mxCell)createEquivalenceNodeCell(null);
											addCell(null, eNodeCell);
											setCellGeometryToCenterOfEdge(eNodeCell, cell);
											AC_GUI.addEquivalenceNode(activeModule, newName, eNodeCell);
											mxCell sourceToeNodeCell = (mxCell)createConnectionCell(null, eNodeCell, source, "ConnectionEdge");
											AC_GUI.addConnection(activeModule, sourceToeNodeCell);
											mxCell eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
											AC_GUI.addConnection(activeModule, eNodeToTargetCell);
											/*
											if (AC_GUI.newNameValidation(sourcePort.getRefName()))
											{
												eNodeCell = (mxCell)createEquivalenceNodeCell(null);
												addCell(null, eNodeCell);
												setCellGeometryToCenterOfEdge(eNodeCell, cell);
												AC_GUI.addEquivalenceNode(activeModule, sourcePort.getRefName(), eNodeCell);
												mxCell sourceToeNodeCell = (mxCell)createConnectionCell(null, eNodeCell, source, "ConnectionEdge");
												AC_GUI.addConnection(activeModule, sourceToeNodeCell);
												mxCell eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
												AC_GUI.addConnection(activeModule, eNodeToTargetCell);
											}
											else
											{
												String msg = "Error: \"";
												msg += sourcePort.getRefName();
												msg += "\" is already the name of a Species or Global Quantity.";
												JOptionPane.showMessageDialog(null, msg);
												return;
											}
											*/
											return;
										}
										
										if (sourceHasIncomingEdge)
										{
											// the source has an incoming edge
											
											Object sourceIncomingEdges [] = mxGraphModel.getIncomingEdges(graph.getModel(), source);
											mxCell existingeNodeCell = null;
											for(int i = 0; i < sourceIncomingEdges.length; i++)
											{
												if (((mxCell)sourceIncomingEdges[i]).getSource().getValue() instanceof EquivalenceNode)
												{
													existingeNodeCell = (mxCell)((mxCell)sourceIncomingEdges[i]).getSource();
												}
											}
											if (existingeNodeCell != null)
											{
												// the source of the incoming edge is an equivalence node
												// check if target equivalence port is already connected to the eNode
												int edgesBetween = mxGraphModel.getEdgesBetween(graph.getModel(), existingeNodeCell, target, false).length;
												if (edgesBetween == 0)
												{
													// the target equivalence port is not connected to the eNode
													// create an edge between the target equivalence port and the eNode
													mxCell eNodeToTargetCell = (mxCell)createConnectionCell(null, existingeNodeCell, target, "ConnectionEdge");
													AC_GUI.addConnection(activeModule, eNodeToTargetCell);
												}
												else
												{
													// the target equivalence port is already connected to the eNode
													JOptionPane.showMessageDialog(null,
															"The two equivalence ports are already connected through an equivalence node.");
												}
											}
											else
											{
												System.out.println("Problem: submodule.equivalenceport->module.equivalenceport" +
														"existingeNodeCell equals null");
											}
											return;
										}
										
										// the source has no incoming edges
										System.out.println("Source has no existing incoming edges.");
										if (targetHasIncomingEdge)
										{
											// the target has existing incoming edges
											Object targetIncomingEdges [] = mxGraphModel.getIncomingEdges(graph.getModel(), target);
											for(int i = 0; i < targetIncomingEdges.length; i++)
											{
												if (((mxCell)targetIncomingEdges[i]).getSource().getValue() instanceof EquivalenceNode)
												{
													eNodeCell = (mxCell)((mxCell)targetIncomingEdges[i]).getSource();
												}
											}
											if (eNodeCell != null)
											{
												// create connection from eNode->source port
												mxCell eNodeToSourceCell = (mxCell)createConnectionCell(null, eNodeCell, source, "ConnectionEdge");
												AC_GUI.addConnection(activeModule, eNodeToSourceCell);
											}
											else
											{
												System.out.println("Problem: submodule.equivalenceport->module.equivalenceport" +
														" eNodeCell equals null");
											}
											return;
										}
										return;
									default:
									}
									break;
								default:
								}
							}
							else
							{
								// the source is a port
								// the sourceModule is a submodule
								// the target is a port
								// the targetModule is a submodule
								if (sourceModule == targetModule)
								{
									// the source and target ports belong to the same submodule
									// cannot occur
									return;
								}
								
								switch(sourcePortType)
								{
								case INPUT:
									switch(targetPortType)
									{
									case INPUT:
										// cannot occur
										return;
									case OUTPUT:
										// cannot occur
										return;
									case EQUIVALENCE:
										// cannot occur
										return;
									default:
									}
									break;
								case OUTPUT:
									switch(targetPortType)
									{
									case INPUT:
										/*
										if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
										{
											return "An input port cannot have more than one incoming connection.";
										}
										*/
										if (mxGraphModel.getDirectedEdgeCount(graph.getModel(), source, true, cell) != 0)
										{
											// the source has existing outgoing edges
											System.out.println("Source has existing outgoing edges.");
											mxCell varCell = null;
											Object outgoingEdges [] = mxGraphModel.getOutgoingEdges(graph.getModel(), source);
											for(int i = 0; i < outgoingEdges.length; i++)
											{
												if (((mxCell)outgoingEdges[i]).getTarget().getValue() instanceof VisibleVariable)
												{
													varCell = (mxCell)((mxCell)outgoingEdges[i]).getTarget();
												}
											}
											if (varCell != null)
											{
												mxCell varToPortCell = (mxCell)createConnectionCell(null, varCell, target, "ConnectionEdge");
												AC_GUI.addConnection(activeModule, varToPortCell);
											}
											else
											{
												System.out.println("Problem: submodule.outputport->submodule.inputport, " +
														"varCell is null.");
											}
											return;
										}
										// the source has no existing outgoing edges
										System.out.println("Source has no existing edges.");
										// create a visible variable
										newName = generateName(sourcePort.getRefName());
										mxCell varCell = (mxCell)createVisibleVariableCell(null);
										addCell(null, varCell);
										setCellGeometryToCenterOfEdge(varCell, cell);
										AC_GUI.addVisibleVariable(activeModule, newName, varCell);
										mxCell sourceToVarCell = (mxCell)createConnectionCell(null, source, varCell, "DashedConnectionEdge");
										AC_GUI.addConnection(activeModule, sourceToVarCell);
										mxCell varToTargetCell = (mxCell)createConnectionCell(null, varCell, target, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, varToTargetCell);
										/*
										if (AC_GUI.newNameValidation(sourcePort.getRefName()))
										{
											mxCell varCell = (mxCell)createVisibleVariableCell(null);
											addCell(null, varCell);
											setCellGeometryToCenterOfEdge(varCell, cell);
											AC_GUI.addVisibleVariable(activeModule, sourcePort.getRefName(), varCell);
											mxCell sourceToVarCell = (mxCell)createConnectionCell(null, source, varCell, "DashedConnectionEdge");
											AC_GUI.addConnection(activeModule, sourceToVarCell);
											mxCell varToTargetCell = (mxCell)createConnectionCell(null, varCell, target, "ConnectionEdge");
											AC_GUI.addConnection(activeModule, varToTargetCell);
										}
										else
										{
											String msg = "Error: \"";
											msg += sourcePort.getRefName();
											msg += "\" is already the name of a Species or Global Quantity.";
											JOptionPane.showMessageDialog(null, msg);
											return;
										}
										*/
										//mxCell portToPortCell = (mxCell)createConnectionCell(null, source, target, "ConnectionEdge");
										//AC_GUI.addConnection(activeModule, portToPortCell);
										return;
									case OUTPUT:
										// cannot occur
										return;
									case EQUIVALENCE:
										// cannot occur
										return;
									default:
									}
									break;
								case EQUIVALENCE:
									mxCell eNodeCell;
									mxCell sourceToeNodeCell;
									mxCell eNodeToTargetCell;
									switch(targetPortType)
									{
									case INPUT:
										/*
										if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
										{
											return "An input port cannot have more than one incoming connection.";
										}
										*/
										if (mxGraphModel.getDirectedEdgeCount(graph.getModel(), source, false, cell) != 0)
										{
											// the source has an existing incoming edge
											mxCell existingEdgeSource = (mxCell)((mxCell)graph.getModel().getEdgeAt(source, 0)).getSource();
											if (existingEdgeSource.getValue() instanceof EquivalenceNode)
											{
												// the existing edge connects from an equivalence node
												EquivalenceNode eNode = (EquivalenceNode)existingEdgeSource.getValue();
												
												// add connection from existing visible variable to the selected target
												eNodeToTargetCell = (mxCell)createConnectionCell(null, existingEdgeSource, target, "ConnectionEdge");
												AC_GUI.addConnection(activeModule, eNodeToTargetCell);
											
											}
											return;
										}
										// the source has no existing outgoing edges
										System.out.println("Source has no existing edges.");
										// create an equivalence node
										newName = generateName(sourcePort.getRefName());
										eNodeCell = (mxCell)createEquivalenceNodeCell(null);
										addCell(null, eNodeCell);
										setCellGeometryToCenterOfEdge(eNodeCell, cell);
										AC_GUI.addEquivalenceNode(activeModule, newName, eNodeCell);
										sourceToeNodeCell = (mxCell)createConnectionCell(null, eNodeCell, source, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, sourceToeNodeCell);
										eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, eNodeToTargetCell);
										/*
										if (AC_GUI.newNameValidation(sourcePort.getRefName()))
										{
											mxCell eNodeCell = (mxCell)createEquivalenceNodeCell(null);
											addCell(null, eNodeCell);
											setCellGeometryToCenterOfEdge(eNodeCell, cell);
											AC_GUI.addEquivalenceNode(activeModule, sourcePort.getRefName(), eNodeCell);
											mxCell sourceToeNodeCell = (mxCell)createConnectionCell(null, eNodeCell, source, "ConnectionEdge");
											AC_GUI.addConnection(activeModule, sourceToeNodeCell);
											mxCell eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
											AC_GUI.addConnection(activeModule, eNodeToTargetCell);
										}
										else
										{
											String msg = "Error: \"";
											msg += sourcePort.getRefName();
											msg += "\" is already the name of a Species or Global Quantity.";
											JOptionPane.showMessageDialog(null, msg);
											return;
										}
										*/
										//mxCell portToPortCell = (mxCell)createConnectionCell(null, source, target, "ConnectionEdge");
										//AC_GUI.addConnection(activeModule, portToPortCell);
										return;
									case OUTPUT:
										// cannot occur
										return;
									case EQUIVALENCE:
										/*
										if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
										{
											return "An equivalence port cannot have more than one incoming connection.";
										}
										*/
										/*										// the source has no existing outgoing edges
										System.out.println("Source has no existing edges.");
										// create an equivalence node
										mxCell eNodeCell = (mxCell)createEquivalenceNodeCell(null);
										addCell(null, eNodeCell);
										setCellGeometryToCenterOfEdge(eNodeCell, cell);
										AC_GUI.addEquivalenceNode(activeModule, sourcePort.getRefName(), eNodeCell);
										mxCell sourceToeNodeCell = (mxCell)createConnectionCell(null, source, eNodeCell, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, sourceToeNodeCell);
										mxCell eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, eNodeToTargetCell);
										//mxCell portToPortCell1 = (mxCell)createConnectionCell(null, source, target, "ConnectionEdge");
										//AC_GUI.addConnection(activeModule, portToPortCell1);
										return;
										*/
										boolean sourceHasIncomingEdge;
										boolean targetHasIncomingEdge;
										
										sourceHasIncomingEdge = (mxGraphModel.getDirectedEdgeCount(graph.getModel(), source, false, cell) != 0);
										targetHasIncomingEdge = (mxGraphModel.getDirectedEdgeCount(graph.getModel(), target, false, cell) != 0);
										eNodeCell = null;
										
										if (!sourceHasIncomingEdge && !targetHasIncomingEdge)
										{
											// the source has no incoming edges
											// the target has no incoming edges
											// create an equivalence node
											// create connection from eNode->source port
											// create connection from eNode->target port
											
											// create an equivalence node
											newName = generateName(sourcePort.getRefName());
											eNodeCell = (mxCell)createEquivalenceNodeCell(null);
											addCell(null, eNodeCell);
											setCellGeometryToCenterOfEdge(eNodeCell, cell);
											AC_GUI.addEquivalenceNode(activeModule, newName, eNodeCell);
											sourceToeNodeCell = (mxCell)createConnectionCell(null, eNodeCell, source, "ConnectionEdge");
											AC_GUI.addConnection(activeModule, sourceToeNodeCell);
											eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
											AC_GUI.addConnection(activeModule, eNodeToTargetCell);
											/*
											if (AC_GUI.newNameValidation(sourcePort.getRefName()))
											{
												eNodeCell = (mxCell)createEquivalenceNodeCell(null);
												addCell(null, eNodeCell);
												setCellGeometryToCenterOfEdge(eNodeCell, cell);
												AC_GUI.addEquivalenceNode(activeModule, sourcePort.getRefName(), eNodeCell);
												mxCell sourceToeNodeCell = (mxCell)createConnectionCell(null, eNodeCell, source, "ConnectionEdge");
												AC_GUI.addConnection(activeModule, sourceToeNodeCell);
												mxCell eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
												AC_GUI.addConnection(activeModule, eNodeToTargetCell);
											}
											else
											{
												String msg = "Error: \"";
												msg += sourcePort.getRefName();
												msg += "\" is already the name of a Species or Global Quantity.";
												JOptionPane.showMessageDialog(null, msg);
												return;
											}
											*/
											return;
										}
										
										if (sourceHasIncomingEdge)
										{
											// the source has an incoming edge
											
											Object sourceIncomingEdges [] = mxGraphModel.getIncomingEdges(graph.getModel(), source);
											eNodeCell = null;
											for(int i = 0; i < sourceIncomingEdges.length; i++)
											{
												if (((mxCell)sourceIncomingEdges[i]).getSource().getValue() instanceof EquivalenceNode)
												{
													eNodeCell = (mxCell)((mxCell)sourceIncomingEdges[i]).getSource();
												}
											}
											if (eNodeCell != null)
											{
												// the source of the incoming edge is an equivalence node
												// check if target equivalence port is already connected to the eNode
												int edgesBetween = mxGraphModel.getEdgesBetween(graph.getModel(), eNodeCell, target, false).length;
												if (edgesBetween == 0)
												{
													// the target equivalence port is not connected to the eNode
													// create an edge between the target equivalence port and the eNode
													eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
													AC_GUI.addConnection(activeModule, eNodeToTargetCell);
												}
												else
												{
													// the target equivalence port is already connected to the eNode
													JOptionPane.showMessageDialog(null,
															"The two equivalence ports are already connected through an equivalence node.");
												}
											}
											else
											{
												System.out.println("Problem: submodule.equivalenceport->submodule.equivalenceport" +
														"existingeNodeCell equals null");
											}
											return;
										}
										
										// the source has no incoming edges
										System.out.println("Source has no existing incoming edges.");
										if (targetHasIncomingEdge)
										{
											// the target has existing incoming edges
											Object targetIncomingEdges [] = mxGraphModel.getIncomingEdges(graph.getModel(), target);
											eNodeCell = null;
											for(int i = 0; i < targetIncomingEdges.length; i++)
											{
												if (((mxCell)targetIncomingEdges[i]).getSource().getValue() instanceof EquivalenceNode)
												{
													eNodeCell = (mxCell)((mxCell)targetIncomingEdges[i]).getSource();
												}
											}
											if (eNodeCell != null)
											{
												// create connection from eNode->source port
												mxCell eNodeToSourceCell = (mxCell)createConnectionCell(null, eNodeCell, source, "ConnectionEdge");
												AC_GUI.addConnection(activeModule, eNodeToSourceCell);
											}
											else
											{
												System.out.println("Problem: submodule.equivalenceport->submodule.equivalenceport" +
														" eNodeCell equals null");
											}
											return;
										}
										return;
									default:
									}
									break;
								default:
								}
							}
						}
					}
				}
				
				/*
				//check if the edge is connecting two equivalence ports
				if (source.getValue() instanceof Port && target.getValue() instanceof Port)
				{
					Port sourcePort = (Port)((mxCell)source).getValue();
					Port targetPort = (Port)((mxCell)target).getValue();
					Module sourceModule = sourcePort.getParent();
					Module targetModule = targetPort.getParent();
					
					//System.out.println("Source: " + ((mxCell)source).getValue().toString());
					//System.out.println("Target: " + ((mxCell)target).getValue().toString());
					PortType sourceType = ((Port)((mxCell)source).getValue()).getType();
					PortType targetType = ((Port)((mxCell)target).getValue()).getType();
					
					removeCell(cell);
					
					switch(sourceType)
					{
					case OUTPUT:
						System.out.println("Source = Output Port");
						System.out.println("Source edge count: " + graph.getModel().getEdgeCount(source));
						if (graph.getModel().getEdgeCount(source) == 0)
						{
							// the source has no existing outgoing edges
							System.out.println("Source has no existing edges.");
							mxCell varCell = (mxCell)createVisibleVariableCell(null);
							addCell(null, varCell);
							setCellGeometryToCenterOfEdge(varCell, cell);
							//removeCell(cell);
							AC_GUI.addVisibleVariable(activeModule, sourcePort.getRefName(), varCell);
							mxCell sourceToVarCell = (mxCell)createConnectionCell(null, source, varCell, "DashedConnectionEdge");
							//addCell(null, sourceToVarCell);
							AC_GUI.addConnection(activeModule, sourceToVarCell);
							mxCell varToTargetCell = (mxCell)createConnectionCell(null, varCell, target, "ConnectionEdge");
							//addCell(null, varToTargetCell);
							AC_GUI.addConnection(activeModule, varToTargetCell);
						}
						else
						{
							// the source has an existing outgoing edge
							mxCell existingEdgeTarget = (mxCell)((mxCell)graph.getModel().getEdgeAt(source, 0)).getTarget();
							if (existingEdgeTarget.getValue() instanceof VisibleVariable)
							{
								// add connection from existing visible variable to the selected target
								mxCell varToTargetCell = (mxCell)createConnectionCell(null, existingEdgeTarget, target, "ConnectionEdge");
								//addCell(null, varToTargetCell);
								AC_GUI.addConnection(activeModule, varToTargetCell);
							}
						}
						break;
					case EQUIVALENCE:
						switch(targetType)
						{
						case INPUT:
							
							break;
						case OUTPUT:
							AC_GUI.addConnection(activeModule, cell);
							break;
						case EQUIVALENCE:
							//the edge is connecting two equivalence ports
							System.out.println("Time to create an equivalence node.");
							if (graph.getModel().getEdgeCount(source) == 0)
							{
								mxCell eNodeCell = (mxCell)createEquivalenceNodeCell(null);
								addCell(null, eNodeCell);
								setCellGeometryToCenterOfEdge(eNodeCell, cell);
								AC_GUI.addEquivalenceNode(activeModule, sourcePort.getRefName(), eNodeCell);
								mxCell sourceToeNodeCell = (mxCell)createConnectionCell(null, source, eNodeCell, "ConnectionEdge");
								AC_GUI.addConnection(activeModule, sourceToeNodeCell);
								mxCell eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
								AC_GUI.addConnection(activeModule, eNodeToTargetCell);
							}
							else
							{
								// the source has an existing outgoing edge
								mxCell existingEdgeTarget = (mxCell)((mxCell)graph.getModel().getEdgeAt(source, 0)).getTarget();
								if (existingEdgeTarget.getValue() instanceof EquivalenceNode)
								{
									// add connection from existing visible variable to the selected target
									mxCell eNodeToTargetCell = (mxCell)createConnectionCell(null, existingEdgeTarget, target, "ConnectionEdge");
									//addCell(null, varToTargetCell);
									AC_GUI.addConnection(activeModule, eNodeToTargetCell);
								}
							}
							//AC_GUI.addEquivalenceNode(activeModule, cell);
							//graph.getModel().remove(cell);
							return;
						default:
						
						}
						break;
					default:
							
					}
					*/
					/*
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
				*/
				//AC_GUI.currentGUI.addConnection(activeModule, evt.getProperty("cell"));
				
				//Object cell1 = graph.createEdge(cell, null, "val", "s", "t", "");
				/*
				// set the connection edge style
				graph.getModel().beginUpdate();
				try
				{
					//graph.getModel().remove(cell);
					//graph.getModel().setStyle(evt.getProperty("cell"), "ConnectionEdge");
				}
				finally
				{
					graph.getModel().endUpdate();
				}
				*/
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
		Object cellValue;
		JPopupMenu menu = new JPopupMenu();

		cellValue = ((mxCell)cell).getValue();
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
					//System.out.println("Connected edge count: " + graph.getModel().getEdgeCount(((mxCell)cell).getSource()));
					// get the connection object from the drawing cell
					Connection edge = (Connection)((mxCell)cell).getValue();
					// remove the drawing cell
					removeCell(edge.getDrawingCell());
					// remove the connection from the module
					AC_GUI.currentGUI.removeConnection(edge);
					//System.out.println("Connected edge count: " + graph.getModel().getEdgeCount(((mxCell)cell).getSource()));
				}
			});
			
			menu.add(new AbstractAction("Properties") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e)
				{
					Object sourceValue;
					Object targetValue;
					String source = "";
					String target = "";
					
					sourceValue = ((mxCell)cell).getSource().getValue();
					targetValue = ((mxCell)cell).getTarget().getValue();
					
					if (sourceValue instanceof Port)
					{
						source = "Port ";
						source += ((Port)sourceValue).getParent().getName();
						source += "." + ((Port)sourceValue).getName();
					} else if (sourceValue instanceof VisibleVariable)
					{
						source = "Variable ";
						source += ((VisibleVariable)sourceValue).getRefName();
					} else if (sourceValue instanceof EquivalenceNode)
					{
						source = "Equivalence Node ";
						source += ((EquivalenceNode)sourceValue).getRefName();
					}
					
					if (targetValue instanceof Port)
					{
						target = "Port ";
						target += ((Port)targetValue).getParent().getName();
						target += "." + ((Port)targetValue).getName();
					} else if (targetValue instanceof VisibleVariable)
					{
						target = "Variable ";
						target += ((VisibleVariable)targetValue).getRefName();
					} else if (targetValue instanceof EquivalenceNode)
					{
						target = "Equivalence Node ";
						target += ((EquivalenceNode)targetValue).getRefName();
					}
					
					String msg = "Source = " + source + "\nDestination = " + target;
					JOptionPane.showMessageDialog(null, msg);
				}
			});
		}
		else
		{
			if (cellValue instanceof Module)
			{
				if (activeModule == (Module)cellValue)
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
				else
				{
					// the module selected is a submodule
				}
			} 
			else if (cellValue instanceof Port)
			{
				if (activeModule == ((Port)cellValue).getParent())
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
				else
				{
					// the port belongs to a submodule
				}
			}
			else if (cellValue instanceof VisibleVariable)
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
			else if (cellValue instanceof EquivalenceNode)
			{
				menu.add(new AbstractAction("Remove") {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent e)
					{
						// get the Equivalence Node object from the drawing cell
						EquivalenceNode currentENode = (EquivalenceNode) ((mxCell) cell).getValue();
						String msg = "Number of edges connected to the eNode: ";
						msg += graph.getModel().getEdgeCount(cell) + ".";
						//System.out.println(msg);
						// call AC_GUI to fully remove the port
						AC_GUI.removeEquivalenceNode(currentENode);
					}
				});
			}
		}

		return menu;
	}
}