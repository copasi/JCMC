package acgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.sbml.libsbml.GeneralGlyph;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxStylesheet;

/**
 * @author Thomas
 *
 */
public class DrawingBoard extends JPanel
{

	private final int DEFAULT_MODULE_HEIGHT = 450;
	private final int DEFAULT_MODULE_WIDTH = 643;
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
	private Module submoduleView;
	private mxCell activeSubmoduleButtonCell;
	mxGraphComponent graphComponent;
	
	/**
	 * Construct the drawing board.
	 */
	public DrawingBoard()
	{
		graph = new ACGraph();
		this.styleSetup();
		graph.setDropEnabled(false);
		parent = graph.getDefaultParent();
		submoduleView = null;
		activeSubmoduleButtonCell = null;
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
	
	public void setup(Module mod)
	{
		mxCell v1;
		graph.getModel().beginUpdate();
		try
		{
			// int width = graphComponent.getViewport().getWidth() - 10;
			// int height = graphComponent.getViewport().getHeight() - 10;
			double width = graphComponent.getVisibleRect().getWidth() - 50;
			double height = graphComponent.getVisibleRect().getHeight() - 50;
			// v1 = graph.insertVertex(parent, null, mod, 10, 10, 600, 600,
			// "defaultVertex;fillColor=white;strokeColor=blue;strokeWidth=5.0");
			v1 = (mxCell)graph.insertVertex(parent, null, mod, 25, 25, width, height, "Module");
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
		double defaultModuleX = 25;
		double defaultModuleY = 25;
		double defaultModuleWidth = graphComponent.getVisibleRect().getWidth() - 50;
		double defaultModuleHeight = graphComponent.getVisibleRect().getHeight() - 50;
		double defaultSubmoduleX = 0;
		double defaultSubmoduleY = 0;
		double defaultSubmoduleWidth = DEFAULT_SUBMODULE_WIDTH;
		double defaultSubmoduleHeight = DEFAULT_SUBMODULE_HEIGHT;

		Object parentCell;
		// Determine what the parentCell will be
		if (mod.getParent() != null)
		{
			// The module's parent's drawing cell will be the parentCell
			parentCell = mod.getParent().getDrawingCell();
			int childCount = mod.getParent().getChildren().size();
			defaultSubmoduleX = 40 + (childCount * 20);
			defaultSubmoduleY = 40 + (childCount * 20);
		}
		else
		{
			// The module has no parent, the default parent will be the
			// parentCell
			parentCell = parent;
		}

		// Create the cell
		mxCell cell = (mxCell)graph.createVertex(parentCell, null, mod, 0, 0, 1, 1, "");
		cell.setConnectable(false);

		// Assign the created cell to the module
		mod.setDrawingCell(cell);
		
		if (mod.getDrawingCellGeometryModule() == null)
		{
			mod.setDrawingCellGeometryModule(new mxGeometry(defaultModuleX, defaultModuleY, defaultModuleWidth, defaultModuleHeight));
		}
		
		if (mod.getDrawingCellGeometrySubmodule() == null)
		{
			mod.setDrawingCellGeometrySubmodule(new mxGeometry(defaultSubmoduleX, defaultSubmoduleY, defaultSubmoduleWidth, defaultSubmoduleHeight));
		}
	}
	
	public void createCell(Module mod, GeneralGlyph glyph)
	{
		if (glyph == null)
		{
			System.err.println("Glyph is null.");
		}
		System.out.println("Glyph id: " + glyph.getId());
		if (glyph.getBoundingBox() == null)
		{
			System.err.println("Glyph boundingbox is null.");
		}
		
		Object parentCell;
		mxGeometry geo;
		double defaultModuleX = 25;
		double defaultModuleY = 25;
		double defaultModuleWidth = graphComponent.getVisibleRect().getWidth() - 50;
		double defaultModuleHeight = graphComponent.getVisibleRect().getHeight() - 50;
		double defaultSubmoduleX = 0;
		double defaultSubmoduleY = 0;
		double defaultSubmoduleWidth = DEFAULT_SUBMODULE_WIDTH;
		double defaultSubmoduleHeight = DEFAULT_SUBMODULE_HEIGHT;
		double x = glyph.getBoundingBox().x();
		double y = glyph.getBoundingBox().y();
		double width = glyph.getBoundingBox().width();
		double height = glyph.getBoundingBox().height();
		
		geo = new mxGeometry(x, y, width, height);
		
		// Determine what the parentCell will be
		if (mod.getParent() != null)
		{
			// The module's parent's drawing cell will be the parentCell
			parentCell = mod.getParent().getDrawingCell();
			if (!(mod.getModuleDefinition() instanceof MathematicalAggregatorDefinition))
			{
				geo.setWidth(DEFAULT_SUBMODULE_WIDTH);
				geo.setHeight(DEFAULT_SUBMODULE_HEIGHT);
			}
			// set default module dimensions
			mod.setDrawingCellGeometryModule(new mxGeometry(defaultModuleX, defaultModuleY, defaultModuleWidth, defaultModuleHeight));
			// the glyph corresponds to submodule dimensions
			mod.setDrawingCellGeometrySubmodule(geo);
			
		}
		else
		{
			// The module has no parent, the default parent will be the
			// parentCell
			parentCell = parent;
			// the glyph corresponds to module dimensions
			mod.setDrawingCellGeometryModule(geo);
			// set default submodule dimensions
			mod.setDrawingCellGeometrySubmodule(new mxGeometry(defaultSubmoduleX, defaultSubmoduleY, defaultSubmoduleWidth, defaultSubmoduleHeight));
		}

		// Create the cell
		mxCell cell = (mxCell)graph.createVertex(parentCell, null, mod, 0, 0, 1, 1, "");
		cell.setConnectable(false);
		cell.setGeometry(geo);
		
		// Assign the created cell to the module
		mod.setDrawingCell(cell);
	}

	public void createMathematicalAggregatorNode(Module maModule)
	{
		createCell(maModule);
		
		mxCell childCell = maModule.getDrawingCell();
		int childCount = maModule.getParent().getChildren().size();
		mxGeometry geo;
		double x;
		double y;
		double width = DEFAULT_AGGREGATOR_WIDTH;
		double height = (20 * (maModule.getPorts().size()-1));

		x = 40 + (childCount * 20);
		y = 40 + (childCount * 20);
		geo = new mxGeometry(x, y, width, height);
		childCell.setGeometry(geo);
		
		maModule.setDrawingCell(childCell);
		maModule.setDrawingCellGeometrySubmodule(geo);
		
		alignMathematicalAggregatorPorts(maModule);
	}

	public void createMathematicalAggregatorNode(Module maModule, GeneralGlyph glyph)
	{
		createCell(maModule);
		
		mxCell childCell = maModule.getDrawingCell();
		mxGeometry geo;
		double x;
		double y;
		double width = DEFAULT_AGGREGATOR_WIDTH;
		double height = (20 * (maModule.getPorts().size()-1));

		x = glyph.getBoundingBox().x();
		y = glyph.getBoundingBox().y();
		geo = new mxGeometry(x, y, width, height);
		childCell.setGeometry(geo);
		
		maModule.setDrawingCell(childCell);
		maModule.setDrawingCellGeometrySubmodule(geo);
		
		alignMathematicalAggregatorPorts(maModule);
	}
	
	/**
	 * Add the drawing cell representation of the given Module to the graph. 
	 * @param mod the Module to add
	 */
	public void addModuleCell(Module mNode)
	{
		Object parentCell;
		mxGeometry geo;
		if (mNode.getParent() == null)
		{
			parentCell = parent;
			geo = mNode.getDrawingCellGeometryModule();
		}
		else
		{
			parentCell = mNode.getParent().getDrawingCell();
			geo = mNode.getDrawingCellGeometrySubmodule();
		}
		Object moduleCell = mNode.getDrawingCell();
		
		graph.getModel().beginUpdate();
		try
		{
			graph.getModel().add(parentCell, moduleCell, 0);
			graph.getModel().setGeometry(moduleCell, geo);
			graph.getModel().setStyle(moduleCell, mNode.getDrawingCellStyle());
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		mNode.setDrawingCellGeometrySubmodule(geo);
		if (!(mNode.getModuleDefinition() instanceof MathematicalAggregatorDefinition) && (parentCell != parent))
		{
			drawSubmoduleButton(moduleCell);
		}
	}
	
	/**
	 * Add the drawing cell representation of the given Module to the graph.
	 * @param mod the Module to add
	 * @param glyph the graphical information for the Module
	 */
	public void addModuleCell(Module mNode, GeneralGlyph glyph)
	{
		Object parentCell;
		if (mNode.getParent() == null)
		{
			parentCell = parent;
		}
		else
		{
			parentCell = mNode.getParent().getDrawingCell();
		}
		Object moduleCell = mNode.getDrawingCell();
		mxGeometry geo = new mxGeometry(glyph.getBoundingBox().x(), glyph.getBoundingBox().y(), glyph.getBoundingBox().width(), glyph.getBoundingBox().height());
		
		graph.getModel().beginUpdate();
		try
		{
			graph.getModel().add(parentCell, moduleCell, 0);
			graph.getModel().setGeometry(moduleCell, geo);
			graph.getModel().setStyle(moduleCell, mNode.getDrawingCellStyle());
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		mNode.setDrawingCellGeometrySubmodule(geo);
		if (!(mNode.getModuleDefinition() instanceof MathematicalAggregatorDefinition) && (parentCell != parent))
		{
			drawSubmoduleButton(moduleCell);
		}
	}
	
	public void createACComponentNodeCell(ACComponentNode node)
	{
		mxGeometry geo = node.getDrawingCellGeometry();
		
		if (node instanceof PortNode)
		{
			double offsetX = -geo.getWidth() / 2;
			double offsetY = -geo.getHeight() / 2;
			
			geo.setOffset(new mxPoint(offsetX, offsetY));
			geo.setRelative(true);
		}
		
		mxCell nodeCell = new mxCell(node, geo, node.getDrawingCellStyle());
		nodeCell.setVertex(true);
		nodeCell.setConnectable(true);
		
		node.setDrawingCell(nodeCell);
	}
	
	/**
	 * Add the drawing cell representation of the given Component Node to the graph.
	 * @param node the Component Node to add
	 */
	public void addComponentNodeCell(ACComponentNode node)
	{
		mxCell parentCell = node.getParent().getDrawingCell();
		mxCell nodeCell = node.getDrawingCell();
		mxGeometry geo = node.getDrawingCellGeometry();

		graph.getModel().beginUpdate();
		try
		{
			graph.getModel().add(parentCell, nodeCell, 0);
			if (geo != null)
			{
				graph.getModel().setGeometry(nodeCell, geo);
			}
			graph.getModel().setStyle(nodeCell, node.getDrawingCellStyle());
		}
		finally
		{
			graph.getModel().endUpdate();
		}
	}
	
	/**
	 * Add the drawing cell representation of the given Component Node to the graph.
	 * @param node the Component Node to add
	 * @param glyph the graphical information for the Component Node
	 */
	public void addComponentNodeCell(ACComponentNode node, GeneralGlyph glyph)
	{
		mxCell parentCell = node.getParent().getDrawingCell();
		mxCell nodeCell = node.getDrawingCell();
		mxGeometry geo = new mxGeometry(glyph.getBoundingBox().x(), glyph.getBoundingBox().y(), glyph.getBoundingBox().width(), glyph.getBoundingBox().height());

		graph.getModel().beginUpdate();
		try
		{
			graph.getModel().add(parentCell, nodeCell, 0);
			graph.getModel().setGeometry(nodeCell, geo);
			graph.getModel().setStyle(nodeCell, node.getDrawingCellStyle());
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		node.setDrawingCellGeometry(geo);
	}
	
	public void createPort(PortNode pNode)
	{
		mxCell portCell;
		mxGeometry geo;
		double width = DEFAULT_PORT_WIDTH;
		double height = DEFAULT_PORT_HEIGHT;
		double offsetX = -width / 2;
		double offsetY = -height / 2;

		geo = new mxGeometry(0, 0.5, width, height);
		geo.setOffset(new mxPoint(offsetX, offsetY));
		geo.setRelative(true);
		
		portCell = new mxCell(pNode, geo, "Port");
		portCell.setVertex(true);
		portCell.setConnectable(true);
		
		pNode.setDrawingCell(portCell);
		pNode.setDrawingCellGeometry(geo);
		pNode.setDrawingCellStyle("Port");
	}
	
	public void createPort(PortNode pNode, GeneralGlyph glyph)
	{
		mxCell portCell;
		mxGeometry geo;
		double x = glyph.getBoundingBox().x();
		double y = glyph.getBoundingBox().y();
		double width = DEFAULT_PORT_WIDTH;
		double height = DEFAULT_PORT_HEIGHT;
		double offsetX = -width / 2;
		double offsetY = -height / 2;

		geo = new mxGeometry(x, y, width, height);
		geo.setOffset(new mxPoint(offsetX, offsetY));
		geo.setRelative(true);
		
		portCell = new mxCell(pNode, geo, "Port");
		portCell.setVertex(true);
		portCell.setConnectable(true);
		
		pNode.setDrawingCell(portCell);
		pNode.setDrawingCellGeometry(geo);
		pNode.setDrawingCellStyle("Port");
	}
	
	public void createPort(PortNode pNode, mxGeometry iGeo)
	{
		mxCell portCell;
		mxGeometry geo;
		double x = iGeo.getX();
		double y = iGeo.getY();
		double width = DEFAULT_PORT_WIDTH;
		double height = DEFAULT_PORT_HEIGHT;
		double offsetX = -width / 2;
		double offsetY = -height / 2;

		geo = new mxGeometry(x, y, width, height);
		geo.setOffset(new mxPoint(offsetX, offsetY));
		geo.setRelative(true);
		
		portCell = new mxCell(pNode, geo, "Port");
		portCell.setVertex(true);
		portCell.setConnectable(true);
		
		pNode.setDrawingCell(portCell);
		pNode.setDrawingCellGeometry(geo);
		pNode.setDrawingCellStyle("Port");
	}
	
	public void addPort(PortNode pNode)
	{
		mxCell parentCell = pNode.getParent().getDrawingCell();
		mxCell nodeCell = pNode.getDrawingCell();
		mxGeometry geo = pNode.getDrawingCellGeometry();
		double width = DEFAULT_PORT_WIDTH;
		double height = DEFAULT_PORT_HEIGHT;
		double offsetX;
		double offsetY;
		
		graph.getModel().beginUpdate();
		try
		{
			if (AC_GUI.activeModule != pNode.getParent())
			{
				// the Port is for a submodule
				width = width / 2;
				height = height / 2;
			}
			
			offsetX = -width / 2;
			offsetY = -height / 2;
			
			geo.setWidth(width);
			geo.setHeight(height);
			geo.setOffset(new mxPoint(offsetX, offsetY));
			
			graph.getModel().add(parentCell, nodeCell, 0);
			graph.getModel().setGeometry(nodeCell, geo);
			graph.getModel().setStyle(nodeCell, pNode.getDrawingCellStyle());
			graph.updatePortOrientation(nodeCell, geo, false);
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		pNode.setDrawingCellGeometry(geo);
	}
	
	/**
	 * Add a drawing cell representation of the given port to the 
	 * drawing cell representation of the given module.
	 * 
	 * @param parentMod the parent module of the port
	 * @param port the port to be added
	 */
	/*
	public void addPort(Module parentMod, PortNode port)
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
			graph.updatePortOrientation(port1, geo1, false);
		}
		finally
		{
			graph.getModel().endUpdate();
		}

		port.setDrawingCell(port1);
	}
	*/
	/**
	 * Add a drawing cell representation of the given port to the 
	 * drawing cell representation of the given module.
	 * 
	 * @param parentMod the parent module of the port
	 * @param port the port to be added
	 */
	/*
	public void addPort(Module parentMod, PortNode port, GeneralGlyph glyph)
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
			graph.updatePortOrientation(port1, geo1, false);
		}
		finally
		{
			graph.getModel().endUpdate();
		}

		port.setDrawingCell(port1);
	}
	*/
	public void createVisibleVariable(VisibleVariableNode var)
	{
		Object parentCell = var.getParent().getDrawingCell();
		mxCell varCell = null;
		varCell = (mxCell)graph.createVertex(parentCell, null, var, 5, 5, DEFAULT_VISIBLEVARIABLE_WIDTH, DEFAULT_VISIBLEVARIABLE_HEIGHT, "");
		//var1 = new mxCell(var);
		if (var.getDrawingCellGeometry() != null)
		{
			varCell.setGeometry(var.getDrawingCellGeometry());
		}
		varCell.setVertex(true);
		varCell.setConnectable(true);
		
		var.setDrawingCell(varCell);
		var.setDrawingCellStyle("VisibleVariable");
		var.setDrawingCellGeometry(varCell.getGeometry());
	}
	
	public void createVisibleVariable(VisibleVariableNode var, GeneralGlyph glyph)
	{
		Object parentCell = var.getParent().getDrawingCell();
		mxCell varCell = null;
		varCell = (mxCell)graph.createVertex(parentCell, null, var, 5, 5, 10, 10, "");
		mxGeometry geo = new mxGeometry(glyph.getBoundingBox().x(), glyph.getBoundingBox().y(), DEFAULT_VISIBLEVARIABLE_WIDTH, DEFAULT_VISIBLEVARIABLE_HEIGHT);
		varCell.setGeometry(geo);
		varCell.setVertex(true);
		varCell.setConnectable(true);
		
		var.setDrawingCell(varCell);
		var.setDrawingCellStyle("VisibleVariable");
		var.setDrawingCellGeometry(varCell.getGeometry());
	}
	
	public void createVisibleVariable(VisibleVariableNode var, mxGeometry geo)
	{
		Object parentCell = var.getParent().getDrawingCell();
		mxCell varCell = null;
		varCell = (mxCell)graph.createVertex(parentCell, null, var, geo.getX(), geo.getY(), DEFAULT_VISIBLEVARIABLE_WIDTH, DEFAULT_VISIBLEVARIABLE_HEIGHT, "");
		//var1 = new mxCell(var);
		if (var.getDrawingCellGeometry() != null)
		{
			varCell.setGeometry(var.getDrawingCellGeometry());
		}
		varCell.setVertex(true);
		varCell.setConnectable(true);
		
		var.setDrawingCell(varCell);
		var.setDrawingCellStyle("VisibleVariable");
		var.setDrawingCellGeometry(varCell.getGeometry());
	}
	
	public void createEquivalenceNode(EquivalenceNode eNode)
	{
		Object parentCell = eNode.getParent().getDrawingCell();
		mxCell eNodeCell = null;
		eNodeCell = (mxCell)graph.createVertex(parentCell, null, eNode, 5, 5, DEFAULT_EQUIVALENCENODE_WIDTH, DEFAULT_EQUIVALENCENODE_HEIGHT, "");
		if (eNode.getDrawingCellGeometry() != null)
		{
			eNodeCell.setGeometry(eNode.getDrawingCellGeometry());
		}
		eNodeCell.setVertex(true);
		eNodeCell.setConnectable(true);
		
		eNode.setDrawingCell(eNodeCell);
		eNode.setDrawingCellStyle("EquivalenceNode");
		eNode.setDrawingCellGeometry(eNodeCell.getGeometry());
	}
	
	public void createEquivalenceNode(EquivalenceNode eNode, GeneralGlyph glyph)
	{
		Object parentCell = eNode.getParent().getDrawingCell();
		mxCell eNodeCell = null;
		eNodeCell = (mxCell)graph.createVertex(parentCell, null, eNode, 5, 5, 10, 10, "");
		mxGeometry geo = new mxGeometry(glyph.getBoundingBox().x(), glyph.getBoundingBox().y(), DEFAULT_EQUIVALENCENODE_WIDTH, DEFAULT_EQUIVALENCENODE_HEIGHT);
		eNodeCell.setGeometry(geo);
		eNodeCell.setVertex(true);
		eNodeCell.setConnectable(true);
		
		eNode.setDrawingCell(eNodeCell);
		eNode.setDrawingCellStyle("EquivalenceNode");
		eNode.setDrawingCellGeometry(eNodeCell.getGeometry());
	}
	
	public void createEquivalenceNode(EquivalenceNode eNode, mxGeometry geo)
	{
		Object parentCell = eNode.getParent().getDrawingCell();
		mxCell eNodeCell = null;
		eNodeCell = (mxCell)graph.createVertex(parentCell, null, eNode, geo.getX(), geo.getY(), DEFAULT_EQUIVALENCENODE_WIDTH, DEFAULT_EQUIVALENCENODE_HEIGHT, "");
		if (eNode.getDrawingCellGeometry() != null)
		{
			eNodeCell.setGeometry(eNode.getDrawingCellGeometry());
		}
		eNodeCell.setVertex(true);
		eNodeCell.setConnectable(true);
		
		eNode.setDrawingCell(eNodeCell);
		eNode.setDrawingCellStyle("EquivalenceNode");
		eNode.setDrawingCellGeometry(eNodeCell.getGeometry());
	}
	
	public void createConnection(ConnectionNode edge, Object source, Object target)
	{
		Object parentCell = edge.getParent().getDrawingCell();
		String drawingCellStyle;
		
		if ((edge.getDrawingCellStyle() != null) && (edge.getDrawingCellStyle() != ""))
		{
			drawingCellStyle = edge.getDrawingCellStyle();
		}
		else
		{
			drawingCellStyle = "ConnectionEdge";
		}
		// createEdge
		//mxCell edgeCell = (mxCell)graph.createEdge(parentCell, null, edge, source, target, drawingCellStyle);
		mxCell edgeCell = (mxCell)graph.insertEdge(parentCell, null, edge, source, target, drawingCellStyle);
		edge.setDrawingCell(edgeCell);
		edge.setDrawingCellStyle(drawingCellStyle);
	}
	
	public void addVisibleVariable(Module parentMod, VisibleVariableNode var)
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

		var.setDrawingCell(var1);
		//var.setDrawingCellBounds(bounds);
		var.setDrawingCellGeometry(var1.getGeometry());
	}
	
	public void addMathematicalAggregator(Module maModule)
	{
		mxCell parentCell = maModule.getParent().getDrawingCell();
		mxCell childCell = maModule.getDrawingCell();
		mxGeometry geo = maModule.getDrawingCellGeometrySubmodule();
		
		graph.getModel().beginUpdate();
		try
		{
			graph.getModel().add(parentCell, childCell, 0);
			graph.getModel().setGeometry(childCell, geo);
			graph.getModel().setStyle(childCell, maModule.getDrawingCellStyle());
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		maModule.setDrawingCellGeometrySubmodule(geo);
		drawPorts(maModule);
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
		
		mxCell eNodeCell = eNode.getDrawingCell();
		graph.getModel().beginUpdate();
		try
		{
			/*
			eNodeCell = (mxCell)graph.createVertex(cell, null, eNode, 5, 5, 10, 10, "");
			eNodeCell.setVertex(true);
			eNodeCell.setConnectable(true);
			*/
			
			xPosition = (formerGeo.getSourcePoint().getX() + formerGeo.getTargetPoint().getX()) / 2 - (width/2);
			yPosition = (formerGeo.getSourcePoint().getY() + formerGeo.getTargetPoint().getY()) / 2 - (height/2);

			mxGeometry geo = new mxGeometry(xPosition, yPosition, width, height);
			graph.getModel().add(cell, eNodeCell, 0);
			graph.getModel().setGeometry(eNodeCell, geo);
			graph.getModel().setStyle(eNodeCell, eNode.getDrawingCellStyle());
		}
		finally
		{
			graph.getModel().endUpdate();
		}

		eNode.setDrawingCell(eNodeCell);
		eNode.setDrawingCellGeometry(eNodeCell.getGeometry());
	}
	/*
	public void createConnection(ConnectionNode edge, Object source, Object target)
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
		mxCell edgeCell = (mxCell)graph.insertEdge(parentCell, null, edge, source, target, drawingCellStyle);
		edge.setDrawingCell(edgeCell);
		edge.setDrawingCellStyle(drawingCellStyle);
	}
	
	public void createConnection(ConnectionNode edge, Object source, Object target, String drawingCellStyle)
	{
		Object parentCell = edge.getParent().getDrawingCell();
		//Object edgeCell = graph.createEdge(parentCell, null, edge, source, target, drawingCellStyle);
		mxCell edgeCell = (mxCell)graph.insertEdge(parentCell, null, edge, source, target, drawingCellStyle);
		edge.setDrawingCell(edgeCell);
		edge.setDrawingCellStyle(drawingCellStyle);
	}
	*/
	
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
	
	public void removeExtensions(ACComponentNode node)
	{
		if (node instanceof PortNode)
		{
			return;
		}
		mxCell nodeCell;
		nodeCell = node.getDrawingCell();
		int connectionCount = graph.getModel().getEdgeCount(nodeCell);
		if (connectionCount <= 1)
		{
			if (node instanceof EquivalenceNode)
			{
				AC_GUI.removeEquivalenceNode((EquivalenceNode)node, false);
			}
			else if (node instanceof VisibleVariableNode)
			{
				AC_GUI.removeVisibleVariable((VisibleVariableNode)node, false);
			}
		}
	}
	
	/**
	 * Remove any edges connected to the given drawing cell.
	 * @param cell the drawing cell whose edges will be removed
	 */
	public void removeEdges(mxCell cell)
	{
		// check if there are any connections to the drawing cell
		int connectionCount = graph.getModel().getEdgeCount(cell);
		if (connectionCount > 0)
		{
			// remove the existing connections from the drawing cell
			mxCell edgeCell;
			ConnectionNode edge;
			mxCell oppositeTerminalCell = null;
			ACComponentNode oppositeTerminal;
			for(int i = 0; i < connectionCount; i++)
			{
				// get the connection drawing cell
				edgeCell = (mxCell)graph.getModel().getEdgeAt(cell, 0);
				if (edgeCell.getSource() == cell)
				{
					oppositeTerminalCell = (mxCell)edgeCell.getTarget();
				}
				else if (edgeCell.getTarget() == cell)
				{
					oppositeTerminalCell = (mxCell)edgeCell.getSource();
				}
				oppositeTerminal = (ACComponentNode)oppositeTerminalCell.getValue();
				// get the connection object from the drawing cell
				edge = (ConnectionNode)edgeCell.getValue();
				// remove the connection from the module
				AC_GUI.removeConnection(edge, false);
				// remove any extensions
				removeExtensions(oppositeTerminal);
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
		graph.updatePortOrientation(portCell, ((mxCell)portCell).getGeometry(), false);
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
 
	public void setSubmoduleInfoView(Module mod)
	{
		submoduleView = mod;
	}
	
	public Module getSubmoduleInfoView()
	{
		return submoduleView;
	}
	
	public mxCell getActiveSubmoduleButtonCell()
	{
		return activeSubmoduleButtonCell;
	}
	
	public void drawSubmoduleMiniComponents(Module mod, mxCell buttonCell)
	{
		Object submoduleCell = mod.getDrawingCell();
		
		String buttonStyle = "ActiveButton";
		String buttonValue = "-";
		String submoduleStyle = "Submodule_Show_Information";
		
		graph.getModel().beginUpdate();
		try
		{
			graph.getModel().setStyle(buttonCell, buttonStyle);
			graph.getModel().setValue(buttonCell, buttonValue);
			graph.getModel().setStyle(submoduleCell, submoduleStyle);
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		submoduleView = mod;
		activeSubmoduleButtonCell = buttonCell;
		drawSubmoduleMiniChildren(mod);
		drawSubmoduleMiniVisibleVariables(mod);
		drawSubmoduleMiniEquivalenceNodes(mod);
		drawSubmoduleMiniConnections(mod);
	}

	public void removeSubmoduleMiniComponents(mxCell buttonCell)
	{
		if (submoduleView != null)
		{
			Object submoduleCell = submoduleView.getDrawingCell();
			
			String buttonStyle = "InactiveButton";
			String buttonValue = "+";
			String submoduleStyle = "Submodule_No_Show_Information";
			
			graph.getModel().beginUpdate();
			try
			{
				graph.getModel().setStyle(buttonCell, buttonStyle);
				graph.getModel().setValue(buttonCell, buttonValue);
				graph.getModel().setStyle(submoduleCell, submoduleStyle);
			}
			finally
			{
				graph.getModel().endUpdate();
			}
			
			if (hasMiniComponents(submoduleView))
			{		
				Object submoduleComponents [] = graph.getChildCells(submoduleCell);
				String cellStyleMatch = Constants.MINI;
				for (int i = 0; i < submoduleComponents.length; i++)
				{
					if (graph.getModel().getStyle(submoduleComponents[i]).endsWith(cellStyleMatch))
					{
						graph.getModel().remove(submoduleComponents[i]);
					}
				}
			}
			submoduleView = null;
			activeSubmoduleButtonCell = null;
		}
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
		//System.out.println("width = " + width + " height = " + height);
		mxGeometry geo;
		//mod.setDrawingCellGeometry(geo);
		Object cell = mod.getDrawingCell();
		if (activeModule != null)
		{
			removeVisibleCells();
		}
		graph.getModel().beginUpdate();
		try
		{
			geo = mod.getDrawingCellGeometryModule();
			if (geo == null)
			{
				geo = new mxGeometry();
				xPosition = 25;
				yPosition = 25;
				geo.setX(xPosition);
				geo.setY(yPosition);
				geo.setWidth(width);
				geo.setHeight(height);
			}
			//graph.resizeCell(childCell, bounds);
			graph.getModel().add(parent, cell, 0);
			graph.getModel().setGeometry(cell, geo);
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
			activeModule.setDrawingCellGeometryModule(activeModule.getDrawingCell().getGeometry());
			
			ListIterator<Module> children = activeModule.getChildren().listIterator();
			Module child;
			mxCell childCell;
			mxGeometry geo;

			// save the submodule positions
			while (children.hasNext())
			{
				child = children.next();
				childCell = child.getDrawingCell();

				geo = childCell.getGeometry();
				child.setDrawingCellGeometrySubmodule(geo);
				
				if (child.getPorts().size() > 0)
				{
					// save the submodule's PortNode positions
					saveACComponentNodePositions(child.getPorts().listIterator());
				}
			}
			
			// save the activeModule's PortNode positions
			saveACComponentNodePositions(activeModule.getPorts().listIterator());
			// save the activeModule's VisibleVariableNode positions
			saveACComponentNodePositions(activeModule.getVisibleVariables().listIterator());
			// save the activeModule's EquivalenceNode positions
			saveACComponentNodePositions(activeModule.getEquivalences().listIterator());
		}
	}
	
	private void saveACComponentNodePositions(ListIterator<ACComponentNode> list)
	{
		ACComponentNode node;
		mxCell nodeCell;
		mxGeometry geo;
		while(list.hasNext())
		{
			node = list.next();
			nodeCell = node.getDrawingCell();
			
			geo = nodeCell.getGeometry();
			node.setDrawingCellGeometry(geo);
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
				geo = child.getDrawingCellGeometrySubmodule();
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
				//((mxCell)childCell).setGeometry(geo);
				graph.getModel().add(parentCell, childCell, 0);
				if(child.getDrawingCellStyle().equals(""))
				{
					child.setDrawingCellStyle("Submodule_No_Show_Information");
				}
				graph.getModel().setStyle(childCell, child.getDrawingCellStyle());
				graph.getModel().setGeometry(childCell, geo);
				
			}
			finally
			{
				graph.getModel().endUpdate();
			}
			//child.setDrawingCellBounds(bounds);
			if (!(child.getModuleDefinition() instanceof MathematicalAggregatorDefinition))
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
		ListIterator<ACComponentNode> ports;
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
			((mxCell)portCell).setConnectable(true);

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
				graph.updatePortOrientation(portCell, geo, false);
			}
			finally
			{
				graph.getModel().endUpdate();
			}
		}
	}

	private void drawVisibleVariables(Module mod)
	{
		ListIterator<ACComponentNode> vars;
		Object cell;
		Object varCell;
		//mxGeometry geo;
		double x;
		double y;
		double width;
		double height;
		double offsetX;
		double offsetY;
		ACComponentNode var;
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
				//((mxCell)varCell).setGeometry(geo);
				((mxCell) varCell).setConnectable(true);
				graph.getModel().add(cell, varCell, 0);
				graph.getModel().setStyle(varCell, "VisibleVariable");
				graph.getModel().setGeometry(varCell, geo);
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
		ListIterator<ACComponentNode> eNodes;
		Object cell;
		Object eNodeCell;
		//mxGeometry geo;
		double x;
		double y;
		double width;
		double height;
		double offsetX;
		double offsetY;
		ACComponentNode eNode;
		int eNodeIndex;
		mxGeometry geo;
		double xPosition;
		double yPosition;
		
		// get the list of equivalence nodes
		eNodes = mod.getEquivalences().listIterator();
		
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
				//((mxCell)eNodeCell).setGeometry(geo);
				((mxCell) eNodeCell).setConnectable(true);
				graph.getModel().add(cell, eNodeCell, 0);
				graph.getModel().setStyle(eNodeCell, "EquivalenceNode");
				graph.getModel().setGeometry(eNodeCell, geo);
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
		ListIterator<ConnectionNode> connections;
		Object parentCell = mod.getDrawingCell();
		Object connectionCell;
		ConnectionNode currentConnection;
		
		// get the list of connections
		connections = mod.getConnections().listIterator();
		
		while(connections.hasNext())
		{
			currentConnection = connections.next();
			connectionCell = currentConnection.getDrawingCell();
			
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
				graph.updateConnectionOrientation(connectionCell, false);
				//graph.getModel().setStyle(connectionCell, currentConnection.getDrawingCellStyle());
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
	
	private void drawSubmoduleMiniChildren(Module mod)
	{
		ListIterator<Module> children;
		Module child;
		Object parentCell;
		Object childCell;
		mxGeometry geo;
		mxGeometry geo_mini;
		double xPosition;
		double yPosition;
		double xPosition_mini;
		double yPosition_mini;
		double width_mini;
		double height_mini;
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
				geo = child.getDrawingCellGeometrySubmodule();
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
				xPosition_mini = (geo.getX() - 25)/DEFAULT_MODULE_WIDTH;
				//System.out.print("xPosition_mini = ");
				//System.out.print("( " + geo.getX() + "- 25 )");
				//System.out.println("/ " + DEFAULT_MODULE_WIDTH);
				yPosition_mini = (geo.getY() - 25)/DEFAULT_MODULE_HEIGHT;
				//System.out.print("yPosition_mini = ");
				//System.out.print("( " + geo.getY() + "- 25 )");
				//System.out.println("/ " + DEFAULT_MODULE_HEIGHT);
				width_mini = geo.getWidth() * (geo.getWidth() / DEFAULT_MODULE_WIDTH);
				height_mini = geo.getHeight() * (geo.getHeight() / DEFAULT_MODULE_HEIGHT);
				geo_mini = new mxGeometry(xPosition_mini, yPosition_mini, width_mini, height_mini);
				geo_mini.setRelative(true);
				((mxCell)childCell).setGeometry(geo_mini);
				graph.getModel().add(parentCell, childCell, 0);
				graph.getModel().setStyle(childCell, "Submodule_" + Constants.MINI);
			}
			finally
			{
				graph.getModel().endUpdate();
			}
			// draw the ports of the current child
			drawSubmoduleMiniPorts(child);
		}
	}
	
	private void drawSubmoduleMiniPorts(Module mod)
	{
		ListIterator<ACComponentNode> ports;
		Object parentCell;
		mxCell portCell;
		mxGeometry parentGeo;
		mxGeometry geo;
		mxGeometry geo_mini;
		double x;
		double y;
		double width;
		double height;
		double width_mini;
		double height_mini;
		double offsetX;
		double offsetY;

		// get the list of ports
		ports = mod.getPorts().listIterator();

		// get the drawing cell where the ports will be added
		parentCell = mod.getDrawingCell();

		// loop through each port of the module
		while (ports.hasNext())
		{
			// get the drawing cell of the current port
			portCell = (mxCell) ports.next().getDrawingCell();

			// draw the current port
			graph.getModel().beginUpdate();
			try
			{
				parentGeo = graph.getModel().getGeometry(parentCell);
				x = portCell.getGeometry().getX();
				y = portCell.getGeometry().getY();
				width = DEFAULT_PORT_WIDTH / 2;
				height = DEFAULT_PORT_HEIGHT / 2;
				//width_mini = width * (width / parentGeo.getWidth());
				//height_mini = height * (height / parentGeo.getHeight());
				width_mini = width * (width / DEFAULT_SUBMODULE_WIDTH);
				height_mini = height * (height / DEFAULT_SUBMODULE_WIDTH);			
				offsetX = -width_mini / 2;
				offsetY = -height_mini / 2;
				//geo = new mxGeometry(x, y, width, height);
				//geo.setOffset(new mxPoint(offsetX, offsetY));
				//geo.setRelative(true);
				geo_mini = new mxGeometry(x, y, width_mini, height_mini);
				geo_mini.setOffset(new mxPoint(offsetX, offsetY));
				geo_mini.setRelative(true);
				portCell.setConnectable(false);
				graph.getModel().add(parentCell, portCell, 0);
				graph.getModel().setGeometry(portCell, geo_mini);
				graph.updatePortOrientation(portCell, geo_mini, true);
			}
			finally
			{
				graph.getModel().endUpdate();
			}
		}
	}
	
	private void drawSubmoduleMiniVisibleVariables(Module mod)
	{
		ListIterator<ACComponentNode> vars;
		Object cell;
		mxCell varCell;
		//mxGeometry geo;
		double x;
		double y;
		double width;
		double height;
		double offsetX;
		double offsetY;
		ACComponentNode var;
		int varIndex;
		mxGeometry geo;
		double xPosition;
		double yPosition;
		double xPosition_mini;
		double yPosition_mini;
		double width_mini;
		double height_mini;
		mxGeometry geo_mini;
		
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
			varCell = (mxCell)var.getDrawingCell();

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
				//((mxCell)varCell).setGeometry(geo);
				xPosition_mini = (geo.getX() - 25)/DEFAULT_MODULE_WIDTH;
				yPosition_mini = (geo.getY() - 25)/DEFAULT_MODULE_HEIGHT;
				width_mini = geo.getWidth() * (geo.getWidth() / DEFAULT_MODULE_WIDTH);
				height_mini = geo.getHeight() * (geo.getHeight() / DEFAULT_MODULE_HEIGHT);
				geo_mini = new mxGeometry(xPosition_mini, yPosition_mini, width_mini, height_mini);
				geo_mini.setRelative(true);
				varCell.setConnectable(false);
				graph.getModel().add(cell, varCell, 0);
				graph.getModel().setStyle(varCell, "VisibleVariable_" + Constants.MINI);
				graph.getModel().setGeometry(varCell, geo_mini);
			}
			finally
			{
				graph.getModel().endUpdate();
			}
		}
	}
	
	private void drawSubmoduleMiniEquivalenceNodes(Module mod)
	{
		ListIterator<ACComponentNode> eNodes;
		Object cell;
		mxCell eNodeCell;
		//mxGeometry geo;
		double x;
		double y;
		double width;
		double height;
		double offsetX;
		double offsetY;
		ACComponentNode eNode;
		int eNodeIndex;
		mxGeometry geo;
		double xPosition;
		double yPosition;
		double xPosition_mini;
		double yPosition_mini;
		double width_mini;
		double height_mini;
		mxGeometry geo_mini;
		
		// get the list of equivalence nodes
		eNodes = mod.getEquivalences().listIterator();
		
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
			eNodeCell = (mxCell)eNode.getDrawingCell();
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
				//((mxCell)eNodeCell).setGeometry(geo);
				xPosition_mini = (geo.getX() - 25)/DEFAULT_MODULE_WIDTH;
				yPosition_mini = (geo.getY() - 25)/DEFAULT_MODULE_HEIGHT;
				width_mini = geo.getWidth() * (geo.getWidth() / DEFAULT_MODULE_WIDTH);
				height_mini = geo.getHeight() * (geo.getHeight() / DEFAULT_MODULE_HEIGHT);
				geo_mini = new mxGeometry(xPosition_mini, yPosition_mini, width_mini, height_mini);
				geo_mini.setRelative(true);
				eNodeCell.setConnectable(false);
				graph.getModel().add(cell, eNodeCell, 0);
				graph.getModel().setStyle(eNodeCell, "EquivalenceNode_" + Constants.MINI);
				graph.getModel().setGeometry(eNodeCell, geo_mini);
			}
			finally
			{
				graph.getModel().endUpdate();
			}
			
		}
	}
	
	private void drawSubmoduleMiniConnections(Module mod)
	{
		ListIterator<ConnectionNode> connections;
		Object parentCell = mod.getDrawingCell();
		Object connectionCell;
		ConnectionNode currentConnection;
		
		// get the list of connections
		connections = mod.getConnections().listIterator();
		
		while(connections.hasNext())
		{
			currentConnection = connections.next();
			connectionCell = currentConnection.getDrawingCell();
			
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
				//graph.getModel().setStyle(connectionCell, currentConnection.getDrawingCellStyle() + "_Mini");
				//graph.getModel().setStyle(connectionCell, currentConnection.getDrawingCellStyle());
				graph.updateConnectionOrientation(connectionCell, true);
			}
			finally
			{
				graph.getModel().endUpdate();
			}
		}
	}
	
	/**
	 * Check if the given module has components.
	 * @param mod, the module to check
	 * @return true if this module has submodules, equivalence nodes, visible variable nodes, or connections,
	 * false otherwise.
	 */
	private boolean hasMiniComponents(Module mod)
	{
		int submoduleComponentCount = mod.getChildren().size();
		submoduleComponentCount += mod.getEquivalences().size();
		submoduleComponentCount += mod.getVisibleVariables().size();
		submoduleComponentCount += mod.getConnections().size();
		
		return submoduleComponentCount > 0;
	}
	
	private void alignMathematicalAggregatorPorts(Module maModule)
	{
		ListIterator<ACComponentNode> ports;
		PortNode currentPNode;
		mxCell cell;
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
		portSpacing = 0.8 / (maModule.getPorts().size() - 2);
		
		inputPortIndex = 0;
		cell = maModule.getDrawingCell();
		ports = maModule.getPorts().listIterator();
		while(ports.hasNext())
		{
			currentPNode = (PortNode)ports.next();

			switch(currentPNode.getPortDefinition().getType())
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

			portCell = currentPNode.getDrawingCell();
			portCell.setGeometry(geo);
			currentPNode.setDrawingCellGeometry(geo);
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
	
	private String generateName(String oldName)
	{
		String name = oldName;
		int index;
		
		while(AC_Utility.speciesInModelBuilder(name))
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
		cell.put(mxConstants.STYLE_MOVABLE, "0");
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

		styleSheet.putCellStyle("Sum", cell);
		
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
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		//cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "1.0");
		cell.put(mxConstants.STYLE_EXIT_X, "0.5");
		cell.put(mxConstants.STYLE_EXIT_Y, "1.0");
		
		styleSheet.putCellStyle(Constants.SOURCE_NORTH_MODULE_TARGET_STANDARD_SOLID_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		//cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.0");
		cell.put(mxConstants.STYLE_EXIT_X, "0.5");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.0");
		
		styleSheet.putCellStyle(Constants.SOURCE_SOUTH_MODULE_TARGET_STANDARD_SOLID_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		//cell.put(mxConstants.STYLE_ENTRY_X, "0.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_EAST_MODULE_TARGET_STANDARD_SOLID_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "1.0");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_WEST_MODULE_TARGET_STANDARD_SOLID_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		cell.put(mxConstants.STYLE_ENTRY_Y, "1.0");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_NORTH_MODULE_SOLID_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_SOUTH_MODULE_SOLID_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.0");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_EAST_MODULE_SOLID_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_WEST_MODULE_SOLID_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "0.5");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.0");
		
		styleSheet.putCellStyle(Constants.SOURCE_NORTH_SUBMODULE_TARGET_STANDARD_SOLID_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "0.5");
		cell.put(mxConstants.STYLE_EXIT_Y, "1.0");
		
		styleSheet.putCellStyle(Constants.SOURCE_SOUTH_SUBMODULE_TARGET_STANDARD_SOLID_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "1.0");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_EAST_SUBMODULE_TARGET_STANDARD_SOLID_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_WEST_SUBMODULE_TARGET_STANDARD_SOLID_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_NORTH_SUBMODULE_SOLID_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		cell.put(mxConstants.STYLE_ENTRY_Y, "1.0");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_SOUTH_SUBMODULE_SOLID_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_EAST_SUBMODULE_SOLID_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.0");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_WEST_SUBMODULE_SOLID_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		//cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "1.0");
		cell.put(mxConstants.STYLE_EXIT_X, "0.5");
		cell.put(mxConstants.STYLE_EXIT_Y, "1.0");
		
		styleSheet.putCellStyle(Constants.SOURCE_NORTH_MODULE_TARGET_STANDARD_DASHED_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		//cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.0");
		cell.put(mxConstants.STYLE_EXIT_X, "0.5");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.0");
		
		styleSheet.putCellStyle(Constants.SOURCE_SOUTH_MODULE_TARGET_STANDARD_DASHED_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		//cell.put(mxConstants.STYLE_ENTRY_X, "0.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_EAST_MODULE_TARGET_STANDARD_DASHED_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "1.0");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_WEST_MODULE_TARGET_STANDARD_DASHED_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		cell.put(mxConstants.STYLE_ENTRY_Y, "1.0");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_NORTH_MODULE_DASHED_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_SOUTH_MODULE_DASHED_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.0");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_EAST_MODULE_DASHED_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_WEST_MODULE_DASHED_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "0.5");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.0");
		
		styleSheet.putCellStyle(Constants.SOURCE_NORTH_SUBMODULE_TARGET_STANDARD_DASHED_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "0.5");
		cell.put(mxConstants.STYLE_EXIT_Y, "1.0");
		
		styleSheet.putCellStyle(Constants.SOURCE_SOUTH_SUBMODULE_TARGET_STANDARD_DASHED_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "1.0");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_EAST_SUBMODULE_TARGET_STANDARD_DASHED_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_WEST_SUBMODULE_TARGET_STANDARD_DASHED_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_NORTH_SUBMODULE_DASHED_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		cell.put(mxConstants.STYLE_ENTRY_Y, "1.0");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_SOUTH_SUBMODULE_DASHED_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_EAST_SUBMODULE_DASHED_EDGE, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.0");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_WEST_SUBMODULE_DASHED_EDGE, cell);
		
		
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
		
		styleSheet.putCellStyle("Submodule_" + Constants.MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_NORTH);
		cell.put(mxConstants.STYLE_FILLCOLOR, "red");
		cell.put(mxConstants.STYLE_PERIMETER, "trianglePerimeter");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_NOLABEL, "1");

		styleSheet.putCellStyle("InputPort_North_" + Constants.MINI, cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_SOUTH);
		cell.put(mxConstants.STYLE_FILLCOLOR, "red");
		cell.put(mxConstants.STYLE_PERIMETER, "trianglePerimeter");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_NOLABEL, "1");

		styleSheet.putCellStyle("InputPort_South_" + Constants.MINI, cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_EAST);
		cell.put(mxConstants.STYLE_FILLCOLOR, "red");
		cell.put(mxConstants.STYLE_PERIMETER, "trianglePerimeter");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		
		styleSheet.putCellStyle("InputPort_East_" + Constants.MINI, cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_WEST);
		cell.put(mxConstants.STYLE_FILLCOLOR, "red");
		cell.put(mxConstants.STYLE_PERIMETER, "trianglePerimeter");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		
		styleSheet.putCellStyle("InputPort_West_" + Constants.MINI, cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_NORTH);
		cell.put(mxConstants.STYLE_FILLCOLOR, "blue");
		cell.put(mxConstants.STYLE_PERIMETER, "trianglePerimeter");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		
		styleSheet.putCellStyle("OutputPort_North_" + Constants.MINI, cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_SOUTH);
		cell.put(mxConstants.STYLE_FILLCOLOR, "blue");
		cell.put(mxConstants.STYLE_PERIMETER, "trianglePerimeter");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		
		styleSheet.putCellStyle("OutputPort_South_" + Constants.MINI, cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_EAST);
		cell.put(mxConstants.STYLE_FILLCOLOR, "blue");
		cell.put(mxConstants.STYLE_PERIMETER, "trianglePerimeter");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		
		styleSheet.putCellStyle("OutputPort_East_" + Constants.MINI, cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_WEST);
		cell.put(mxConstants.STYLE_FILLCOLOR, "blue");
		cell.put(mxConstants.STYLE_PERIMETER, "trianglePerimeter");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		
		styleSheet.putCellStyle("OutputPort_West_" + Constants.MINI, cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_NORTH);
		cell.put(mxConstants.STYLE_FILLCOLOR, "yellow");
		cell.put(mxConstants.STYLE_PERIMETER, "rhombusPerimeter");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		
		styleSheet.putCellStyle("EquivalencePort_North_" + Constants.MINI, cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_SOUTH);
		cell.put(mxConstants.STYLE_FILLCOLOR, "yellow");
		cell.put(mxConstants.STYLE_PERIMETER, "rhombusPerimeter");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		
		styleSheet.putCellStyle("EquivalencePort_South_" + Constants.MINI, cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_EAST);
		cell.put(mxConstants.STYLE_FILLCOLOR, "yellow");
		cell.put(mxConstants.STYLE_PERIMETER, "rhombusPerimeter");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		
		styleSheet.putCellStyle("EquivalencePort_East_" + Constants.MINI, cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
		cell.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_WEST);
		cell.put(mxConstants.STYLE_FILLCOLOR, "yellow");
		cell.put(mxConstants.STYLE_PERIMETER, "rhombusPerimeter");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		
		styleSheet.putCellStyle("EquivalencePort_West_" + Constants.MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		cell.put(mxConstants.STYLE_STROKECOLOR, "black");
		cell.put(mxConstants.STYLE_FILLCOLOR, "white");
		cell.put(mxConstants.STYLE_FOLDABLE, "0");
		cell.put(mxConstants.STYLE_PERIMETER, "ellipsePerimeter");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_NOLABEL, "1");

		styleSheet.putCellStyle("VisibleVariable_" + Constants.MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
		cell.put(mxConstants.STYLE_FILLCOLOR, "yellow");
		cell.put(mxConstants.STYLE_PERIMETER, "rhombusPerimeter");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_NOLABEL, "1");

		styleSheet.putCellStyle("EquivalenceNode_" + Constants.MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		
		styleSheet.putCellStyle("ConnectionEdge_" + Constants.MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		
		styleSheet.putCellStyle("DashedConnectionEdge_" + Constants.MINI, cell);

		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		//cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "1.0");
		cell.put(mxConstants.STYLE_EXIT_X, "0.5");
		cell.put(mxConstants.STYLE_EXIT_Y, "1.0");
		
		styleSheet.putCellStyle(Constants.SOURCE_NORTH_MODULE_TARGET_STANDARD_SOLID_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		//cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.0");
		cell.put(mxConstants.STYLE_EXIT_X, "0.5");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.0");
		
		styleSheet.putCellStyle(Constants.SOURCE_SOUTH_MODULE_TARGET_STANDARD_SOLID_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		//cell.put(mxConstants.STYLE_ENTRY_X, "0.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_EAST_MODULE_TARGET_STANDARD_SOLID_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "1.0");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_WEST_MODULE_TARGET_STANDARD_SOLID_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		cell.put(mxConstants.STYLE_ENTRY_Y, "1.0");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_NORTH_MODULE_SOLID_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_SOUTH_MODULE_SOLID_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.0");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_EAST_MODULE_SOLID_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_WEST_MODULE_SOLID_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "0.5");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.0");
		
		styleSheet.putCellStyle(Constants.SOURCE_NORTH_SUBMODULE_TARGET_STANDARD_SOLID_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "0.5");
		cell.put(mxConstants.STYLE_EXIT_Y, "1.0");
		
		styleSheet.putCellStyle(Constants.SOURCE_SOUTH_SUBMODULE_TARGET_STANDARD_SOLID_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "1.0");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_EAST_SUBMODULE_TARGET_STANDARD_SOLID_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_WEST_SUBMODULE_TARGET_STANDARD_SOLID_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_NORTH_SUBMODULE_SOLID_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		cell.put(mxConstants.STYLE_ENTRY_Y, "1.0");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_SOUTH_SUBMODULE_SOLID_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_EAST_SUBMODULE_SOLID_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.0");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_WEST_SUBMODULE_SOLID_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		//cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "1.0");
		cell.put(mxConstants.STYLE_EXIT_X, "0.5");
		cell.put(mxConstants.STYLE_EXIT_Y, "1.0");
		
		styleSheet.putCellStyle(Constants.SOURCE_NORTH_MODULE_TARGET_STANDARD_DASHED_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		//cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.0");
		cell.put(mxConstants.STYLE_EXIT_X, "0.5");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.0");
		
		styleSheet.putCellStyle(Constants.SOURCE_SOUTH_MODULE_TARGET_STANDARD_DASHED_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		//cell.put(mxConstants.STYLE_ENTRY_X, "0.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_EAST_MODULE_TARGET_STANDARD_DASHED_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "1.0");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_WEST_MODULE_TARGET_STANDARD_DASHED_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		cell.put(mxConstants.STYLE_ENTRY_Y, "1.0");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_NORTH_MODULE_DASHED_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_SOUTH_MODULE_DASHED_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.0");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_EAST_MODULE_DASHED_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_WEST_MODULE_DASHED_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "0.5");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.0");
		
		styleSheet.putCellStyle(Constants.SOURCE_NORTH_SUBMODULE_TARGET_STANDARD_DASHED_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "0.5");
		cell.put(mxConstants.STYLE_EXIT_Y, "1.0");
		
		styleSheet.putCellStyle(Constants.SOURCE_SOUTH_SUBMODULE_TARGET_STANDARD_DASHED_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "1.0");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_EAST_SUBMODULE_TARGET_STANDARD_DASHED_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		//cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		//cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_WEST_SUBMODULE_TARGET_STANDARD_DASHED_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_NORTH_SUBMODULE_DASHED_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.5");
		cell.put(mxConstants.STYLE_ENTRY_Y, "1.0");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_SOUTH_SUBMODULE_DASHED_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		cell.put(mxConstants.STYLE_ENTRY_X, "1.0");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_EAST_SUBMODULE_DASHED_EDGE_MINI, cell);
		
		cell = new HashMap<String, Object>();
		cell.put(mxConstants.STYLE_NOLABEL, "1");
		cell.put(mxConstants.STYLE_MOVABLE, "0");
		cell.put(mxConstants.STYLE_ENDARROW, "none");
		cell.put(mxConstants.STYLE_DASHED, "1");
		cell.put(mxConstants.STYLE_ENTRY_X, "0.0");
		cell.put(mxConstants.STYLE_ENTRY_Y, "0.5");
		//cell.put(mxConstants.STYLE_EXIT_X, "0.0");
		//cell.put(mxConstants.STYLE_EXIT_Y, "0.5");
		
		styleSheet.putCellStyle(Constants.SOURCE_STANDARD_TARGET_WEST_SUBMODULE_DASHED_EDGE_MINI, cell);
		
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

		if (((mxCell)cell).isEdge())
		{
			if (activeModule == ((ConnectionNode)cellValue).getParent())
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
						ConnectionNode cNode = (ConnectionNode)((mxCell)cell).getValue();
						//System.out.println("Connected edge count: " + graph.getModel().getEdgeCount(((mxCell)cell).getSource()));
						if (AC_GUI.canModuleBeModified(cNode.getParent()))
						{
							// remove the connection from the module
							AC_GUI.removeConnection(cNode, true);
						}
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
						ConnectionNode cNode = (ConnectionNode)((mxCell)cell).getValue();
						
						sourceValue = ((mxCell)cell).getSource().getValue();
						targetValue = ((mxCell)cell).getTarget().getValue();
						
						if (sourceValue instanceof PortNode)
						{
							source = "Port ";
							source += ((PortNode)sourceValue).getParent().getModuleDefinition().getName();
							source += "." + ((PortNode)sourceValue).getPortDefinition().getName();
						} else if (sourceValue instanceof VisibleVariableNode)
						{
							source = "Variable ";
							source += ((VisibleVariableNode)sourceValue).getVisibleVariableDefinition().getRefName();
						} else if (sourceValue instanceof EquivalenceNode)
						{
							source = "Equivalence Node ";
							source += ((EquivalenceNode)sourceValue).getEquivalenceDefinition().getRefName();
						}
						
						if (targetValue instanceof PortNode)
						{
							target = "Port ";
							target += ((PortNode)targetValue).getParent().getModuleDefinition().getName();
							target += "." + ((PortNode)targetValue).getPortDefinition().getName();
						} else if (targetValue instanceof VisibleVariableNode)
						{
							target = "Variable ";
							target += ((VisibleVariableNode)targetValue).getVisibleVariableDefinition().getRefName();
						} else if (targetValue instanceof EquivalenceNode)
						{
							target = "Equivalence Node ";
							target += ((EquivalenceNode)targetValue).getEquivalenceDefinition().getRefName();
						}
						
						String msg = "Source = " + source;
						msg += AC_Utility.eol;
						msg += "Destination = " + target;
						msg += AC_Utility.eol;
						msg += "CellStyle: " + cNode.getDrawingCellStyle();
						JOptionPane.showMessageDialog(null, msg);
					}
				});
			}
			else
			{
				// the connection node belongs to a submodule
			}
		}
		else
		{
			if (cellValue instanceof Module)
			{	
				if (activeModule == (Module)cellValue)
				{
					menu.add(new AbstractAction("Edit Name") {
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;

						public void actionPerformed(ActionEvent e)
						{
							//graphComponent.startEditingAtCell(cell);
							if (AC_GUI.canModuleBeModified(activeModule))
							{
								AC_Utility.promptUserEditModuleName(activeModule, activeModule.getName());
							}
						}
					});
					
					menu.add(new AbstractAction("Add Port") {
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;
	
						public void actionPerformed(ActionEvent e)
						{
							if (AC_GUI.canModuleBeModified(activeModule))
							{
								PortAddEditor portAddEditor = new PortAddEditor((mxCell)cell, graphComponent);
								portAddEditor.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
								portAddEditor.setModal(true);
								portAddEditor.setVisible(true);
							}
						}
					});
					
					menu.add(new AbstractAction("Show Variable") {
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;
	
						public void actionPerformed(ActionEvent e)
						{
							if (AC_GUI.canModuleBeModified(activeModule))
							{
								VariableAddEditor varAddEditor = new VariableAddEditor(activeModule, graphComponent);
								varAddEditor.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
								varAddEditor.setModal(true);
								varAddEditor.setVisible(true);
							}
						}
					});
				}
				else if (activeModule == ((Module)cellValue).getParent())
				{
					// the module selected is a submodule
					menu.add(new AbstractAction("Edit Name") {
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;

						public void actionPerformed(ActionEvent e)
						{
							//graphComponent.startEditingAtCell(cell);
							if (AC_GUI.canModuleBeModified(activeModule))
							{
								AC_Utility.promptUserEditModuleName(AC_GUI.selectedModule, AC_GUI.selectedModule.getName());
							}
						}
					});
				}
				else
				{
					// the module selected belongs to a submodule
				}
			} 
			else if (cellValue instanceof PortNode)
			{
				if (activeModule == ((PortNode)cellValue).getParent())
				{
					/*
					menu.add(new AbstractAction("Change Type") {
						
						private static final long serialVersionUID = 1L;
	
						public void actionPerformed(ActionEvent e)
						{
							PortNode pNode = (PortNode) ((mxCell) cell).getValue();
							if (AC_GUI.canModuleBeModified(pNode.getParent()))
							{
								Object[] possibleValues = { "Input", "Output", "Equivalence" };
								Object selectedValue = JOptionPane.showInputDialog(null, "Select Type", "Change Port Type", JOptionPane.INFORMATION_MESSAGE, null, possibleValues, pNode.getPortDefinition().getType().toString());
		
								if (selectedValue.toString().compareTo("Input") == 0)
								{
									pNode.getPortDefinition().setType(PortType.INPUT);
								}
								else if (selectedValue.toString().compareTo("Output") == 0)
								{
									pNode.getPortDefinition().setType(PortType.OUTPUT);
								}
								else if (selectedValue.toString().compareTo("Equivalence") == 0)
								{
									pNode.getPortDefinition().setType(PortType.EQUIVALENCE);
								}
		
								mxGeometry geo = ((mxCell) cell).getGeometry();
								graph.updatePortOrientation(cell, geo, false);
							}
						}
					});
					*/
					menu.add(new AbstractAction("Remove") {
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;
	
						public void actionPerformed(ActionEvent e)
						{
							// get the Port object from the drawing cell
							PortNode pNode = (PortNode) ((mxCell) cell).getValue();
							//String msg = "Number of edges connected to the port: ";
							//msg += graph.getModel().getEdgeCount(cell) + ".";
							//System.out.println(msg);
							if (AC_GUI.canModuleBeModified(pNode.getParent()))
							{
								// call AC_GUI to fully remove the port
								AC_GUI.removePort(pNode, true);
							}
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
			else if (cellValue instanceof VisibleVariableNode)
			{
				if (activeModule == ((VisibleVariableNode)cellValue).getParent())
				{
					menu.add(new AbstractAction("Remove") {
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;
	
						public void actionPerformed(ActionEvent e)
						{
							// get the Visible Variable object from the drawing cell
							VisibleVariableNode vNode = (VisibleVariableNode) ((mxCell) cell).getValue();
							//String msg = "Number of edges connected to the variable: ";
							//msg += graph.getModel().getEdgeCount(cell) + ".";
							//System.out.println(msg);
							if (AC_GUI.canModuleBeModified(vNode.getParent()))
							{
								// call AC_GUI to fully remove the variable
								AC_GUI.removeVisibleVariable(vNode, true);
							}						
						}
					});
				}
				else
				{
					// the visible variable node belongs to a submodule
				}
			}
			else if (cellValue instanceof EquivalenceNode)
			{
				if (activeModule == ((EquivalenceNode)cellValue).getParent())
				{
					menu.add(new AbstractAction("Remove") {
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;
	
						public void actionPerformed(ActionEvent e)
						{
							// get the Equivalence Node object from the drawing cell
							EquivalenceNode eNode = (EquivalenceNode) ((mxCell) cell).getValue();
							//String msg = "Number of edges connected to the eNode: ";
							//msg += graph.getModel().getEdgeCount(cell) + ".";
							//System.out.println(msg);
							if (AC_GUI.canModuleBeModified(eNode.getParent()))
							{
								// call AC_GUI to fully remove the equivalence node
								AC_GUI.removeEquivalenceNode(eNode, true);
							}
						}
					});
				}
				else
				{
					// the equivalence node belongs to a submodule
				}
			}
		}

		return menu;
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
					if (cell.getValue() instanceof Module)
					{
						// System.out.println("cell=" + graph.getLabel(cell));
						// Module mod = AC_GUI.moduleList.findModule(cell);
						// AC_GUI.treeView.setSelected(mod.getTreeNode());
						AC_GUI.setSelectedModule(cell);
						//printCellCount(cell);
					}
					
					//System.out.println("activeModule: " + AC_GUI.activeModule.getModuleDefinition().getName());
					//System.out.println("loadedModule: " + AC_GUI.modelBuilder.getLoadedModule().getModuleDefinition().getName());
					if (AC_GUI.activeModule == AC_GUI.modelBuilder.getLoadedModule())
					{
						if (cell.getValue() instanceof VisibleVariableNode)
						{
							VisibleVariableNode var = (VisibleVariableNode)cell.getValue();
							AC_GUI.setSelectedModelBuilderVariable(var.getVisibleVariableDefinition().getRefName(), var.getVisibleVariableDefinition().getVariableType());
						}
						
						if (cell.getValue() instanceof EquivalenceNode)
						{
							AC_GUI.setSelectedModelBuilderVariable(((EquivalenceNode)cell.getValue()).getEquivalenceDefinition().getRefName(), VariableType.SPECIES);
						}
						
						if (cell.getValue() instanceof PortNode)
						{
							// System.out.println("port=" + ((mxCell) cell).getValue().toString());
							// p = (Port) ((mxCell)cell).getValue();
							AC_GUI.modelBuilder.setSelectedPort((PortNode)cell.getValue());
						}
					}
					else
					{
						if (cell.getValue() instanceof PortNode)
						{
							// System.out.println("port=" + ((mxCell) cell).getValue().toString());
							PortNode port = (PortNode) ((mxCell)cell).getValue();
							if (port.getParent() == AC_GUI.modelBuilder.getLoadedModule())
							{
								AC_GUI.modelBuilder.setSelectedPort((PortNode)cell.getValue());
							}
						}
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
				mxCell parentCell;
				Module parentMod;
				
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
						//System.out.println("A button was clicked.");
						AC_GUI.setSelectedModule(parentMod);
						if (submoduleView != null)
						{
							// another submoduleView is active
							// remove the current submoduleView
							AC_GUI.removeSubmoduleInfoView(activeSubmoduleButtonCell, false);
						}
						// display the submodule info
						AC_GUI.displaySubmoduleInfoView(parentMod, cell);
					}
					else if (graph.getModel().getStyle(cell).equalsIgnoreCase("ActiveButton"))
					{
						//System.out.println("A button was clicked.");
						AC_GUI.setSelectedModule(parentMod);
						AC_GUI.removeSubmoduleInfoView(cell, true);
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
				
				if (activeSubmoduleButtonCell != null)
				{
					// the submodule info view is currently active
					AC_GUI.removeSubmoduleInfoView(activeSubmoduleButtonCell, true);
				}
				
				Object sourceObject = source.getValue();
				Object targetObject = target.getValue();
				String newName;
				
				TerminalType sourceType;
				TerminalType targetType;
				
				removeCell(cell);
				
				if (!AC_GUI.canModuleBeModified(activeModule))
				{
					return;
				}
				
				if (sourceObject instanceof VisibleVariableNode)
				{
					// the source is a visible variable
					VisibleVariableNode sourceVisibleVariable = (VisibleVariableNode)sourceObject;
					
					if (targetObject instanceof VisibleVariableNode)
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
					else if (targetObject instanceof PortNode)
					{
						// the source is a visible variable
						// the target is a port
						PortNode targetPort = (PortNode)targetObject;
						PortType targetPortType = ((PortNode)targetObject).getPortDefinition().getType();
						Module targetModule = targetPort.getParent();
						
						if(targetModule == AC_GUI.activeModule)
						{
							// the source is a visible variable
							// the target is a port
							// the targetModule is the active module
							sourceType = TerminalType.VISIBLEVARIABLE;
							targetType = TerminalType.PORT;
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
								AC_GUI.addConnection(activeModule, varToPortCell, sourceType, targetType);
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
							sourceType = TerminalType.VISIBLEVARIABLE;
							targetType = TerminalType.PORT;
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
								AC_GUI.addConnection(activeModule, varToPortCell, sourceType, targetType);
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
					if (targetObject instanceof VisibleVariableNode)
					{
						// the source is an equivalence node
						// the target is a visible variable
						sourceType = TerminalType.EQUIVALENCE;
						targetType = TerminalType.VISIBLEVARIABLE;
						mxCell eNodeToVarCell = (mxCell)createConnectionCell(null, source, target, "DashedConnectionEdge");
						AC_GUI.addConnection(activeModule, eNodeToVarCell, sourceType, targetType);
						return;
					}
					else if (targetObject instanceof PortNode)
					{
						// the source is an equivalence node
						// the target is a port
						sourceType = TerminalType.EQUIVALENCE;
						targetType = TerminalType.PORT;
						PortNode targetPort = (PortNode)targetObject;
						PortType targetPortType = ((PortNode)targetObject).getPortDefinition().getType();
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
								AC_GUI.addConnection(activeModule, eNodeToOutputCell, sourceType, targetType);
								return;
							case EQUIVALENCE:
								/*
								if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
								{
									return "An equivalence port cannot have more than one incoming connection.";
								}
								*/
								mxCell eNodeToPortCell = (mxCell)createConnectionCell(null, source, target, "ConnectionEdge");
								AC_GUI.addConnection(activeModule, eNodeToPortCell, sourceType, targetType);
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
								AC_GUI.addConnection(activeModule, eNodeToPortCell, sourceType, targetType);
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
								AC_GUI.addConnection(activeModule, eNodeToPortCell1, sourceType, targetType);
								return;
							default:
									
							}
						}
					}
				}
				else if (sourceObject instanceof PortNode)
				{
					// the source is a port
					PortNode sourcePort = (PortNode)sourceObject;
					PortType sourcePortType = sourcePort.getPortDefinition().getType();
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
						else if (targetObject instanceof VisibleVariableNode)
						{
							// the source is a port
							// the sourceModule is the active module
							// the target is a visible variable
							sourceType = TerminalType.PORT;
							targetType = TerminalType.VISIBLEVARIABLE;
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
								AC_GUI.addConnection(activeModule, portToVarCell, sourceType, targetType);
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
								AC_GUI.addConnection(activeModule, eportToVarCell, sourceType, targetType);
								return;
							default:
									
							}
						}
						else if (targetObject instanceof PortNode)
						{
							// the source is a port
							// the sourceModule is the active module
							// the target is a port
							sourceType = TerminalType.PORT;
							targetType = TerminalType.PORT;
							PortNode targetPort = (PortNode)targetObject;
							PortType targetPortType = targetPort.getPortDefinition().getType();
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
										if (existingEdgeTarget.getValue() instanceof VisibleVariableNode)
										{
											// add connection from existing visible variable to the selected target
											mxCell varToTargetCell = (mxCell)createConnectionCell(null, existingEdgeTarget, target, "ConnectionEdge");
											//addCell(null, varToTargetCell);
											AC_GUI.addConnection(activeModule, varToTargetCell, TerminalType.VISIBLEVARIABLE, targetType);
										}
										return;
									}
									// the source has no existing outgoing edges
									System.out.println("Source has no existing edges.");
									// create a visible variable
									//newName = generateName(sourcePort.getPortDefinition().getRefName());
									mxCell varCell = (mxCell)createVisibleVariableCell(null);
									addCell(null, varCell);
									setCellGeometryToCenterOfEdge(varCell, cell);
									AC_GUI.addVisibleVariable(activeModule, sourcePort.getPortDefinition().getRefName(), varCell);
									mxCell sourceToVarCell = (mxCell)createConnectionCell(null, source, varCell, "DashedConnectionEdge");
									AC_GUI.addConnection(activeModule, sourceToVarCell, sourceType, TerminalType.VISIBLEVARIABLE);
									mxCell varToTargetCell = (mxCell)createConnectionCell(null, varCell, target, "ConnectionEdge");
									AC_GUI.addConnection(activeModule, varToTargetCell, TerminalType.VISIBLEVARIABLE, targetType);
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
											AC_GUI.addConnection(activeModule, eNodeToTargetCell, TerminalType.EQUIVALENCE, targetType);
										
										}
										return;
									}
									// the source has no existing outgoing edges
									System.out.println("Source has no existing outgoing edges.");
									// create an equivalence node
									//newName = generateName(sourcePort.getPortDefinition().getRefName());
									eNodeCell = (mxCell)createEquivalenceNodeCell(null);
									addCell(null, eNodeCell);
									setCellGeometryToCenterOfEdge(eNodeCell, cell);
									AC_GUI.addEquivalenceNode(activeModule, sourcePort.getPortDefinition().getRefName(), eNodeCell);
									sourceToeNodeCell = (mxCell)createConnectionCell(null, eNodeCell, source, "ConnectionEdge");
									AC_GUI.addConnection(activeModule, sourceToeNodeCell, TerminalType.EQUIVALENCE, sourceType);
									eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
									AC_GUI.addConnection(activeModule, eNodeToTargetCell, TerminalType.EQUIVALENCE, targetType);
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
										//newName = generateName(sourcePort.getPortDefinition().getRefName());
										eNodeCell = (mxCell)createEquivalenceNodeCell(null);
										addCell(null, eNodeCell);
										setCellGeometryToCenterOfEdge(eNodeCell, cell);
										AC_GUI.addEquivalenceNode(activeModule, sourcePort.getPortDefinition().getRefName(), eNodeCell);
										sourceToeNodeCell = (mxCell)createConnectionCell(null, eNodeCell, source, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, sourceToeNodeCell, TerminalType.EQUIVALENCE, sourceType);
										eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, eNodeToTargetCell, TerminalType.EQUIVALENCE, targetType);
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
												AC_GUI.addConnection(activeModule, eNodeToTargetCell, TerminalType.EQUIVALENCE, targetType);
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
											AC_GUI.addConnection(activeModule, eNodeToSourceCell, TerminalType.EQUIVALENCE, sourceType);
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
						sourceType = TerminalType.PORT;
						if (targetObject instanceof EquivalenceNode)
						{
							// the source is a port
							// the sourceModule is a submodule
							// the target is an equivalence node
							// cannot occur
							return;
						}
						else if (targetObject instanceof VisibleVariableNode)
						{
							// the source is a port
							// the sourceModule is a submodule
							// the target is a visible variable
							targetType = TerminalType.VISIBLEVARIABLE;
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
								AC_GUI.addConnection(activeModule, portToVarCell, sourceType, targetType);
								return;
							case EQUIVALENCE:
								/*
								if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
								{
									return "A variable cannot have more than one incoming connection.";
								}
								*/
								mxCell portToVarCell1 = (mxCell)createConnectionCell(null, source, target, "DashedConnectionEdge");
								AC_GUI.addConnection(activeModule, portToVarCell1, sourceType, targetType);
								return;
							default:
							}
						}
						else if (targetObject instanceof PortNode)
						{
							// the source is a port
							// the sourceModule is a submodule
							// the target is a port
							targetType = TerminalType.PORT;
							PortNode targetPort = (PortNode)targetObject;
							PortType targetPortType = targetPort.getPortDefinition().getType();
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
												if (((mxCell)outgoingEdges[i]).getTarget().getValue() instanceof VisibleVariableNode)
												{
													varCell = (mxCell)((mxCell)outgoingEdges[i]).getTarget();
												}
											}
											if (varCell != null)
											{
												mxCell varToPortCell = (mxCell)createConnectionCell(null, varCell, target, "ConnectionEdge");
												AC_GUI.addConnection(activeModule, varToPortCell, TerminalType.VISIBLEVARIABLE, targetType);
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
										//newName = generateName(sourcePort.getPortDefinition().getRefName());
										mxCell varCell = (mxCell)createVisibleVariableCell(null);
										addCell(null, varCell);
										setCellGeometryToCenterOfEdge(varCell, cell);
										AC_GUI.addVisibleVariable(activeModule, targetPort.getPortDefinition().getRefName(), varCell);
										mxCell sourceToVarCell = (mxCell)createConnectionCell(null, source, varCell, "DashedConnectionEdge");
										AC_GUI.addConnection(activeModule, sourceToVarCell, sourceType, TerminalType.VISIBLEVARIABLE);
										mxCell varToTargetCell = (mxCell)createConnectionCell(null, varCell, target, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, varToTargetCell, TerminalType.VISIBLEVARIABLE, targetType);
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
											//newName = generateName(sourcePort.getPortDefinition().getRefName());
											eNodeCell = (mxCell)createEquivalenceNodeCell(null);
											addCell(null, eNodeCell);
											setCellGeometryToCenterOfEdge(eNodeCell, cell);
											AC_GUI.addEquivalenceNode(activeModule, targetPort.getPortDefinition().getRefName(), eNodeCell);
											mxCell sourceToeNodeCell = (mxCell)createConnectionCell(null, eNodeCell, source, "ConnectionEdge");
											AC_GUI.addConnection(activeModule, sourceToeNodeCell, TerminalType.EQUIVALENCE, sourceType);
											mxCell eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
											AC_GUI.addConnection(activeModule, eNodeToTargetCell, TerminalType.EQUIVALENCE, targetType);
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
													AC_GUI.addConnection(activeModule, eNodeToTargetCell, TerminalType.EQUIVALENCE, targetType);
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
												AC_GUI.addConnection(activeModule, eNodeToSourceCell, TerminalType.EQUIVALENCE, sourceType);
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
												if (((mxCell)outgoingEdges[i]).getTarget().getValue() instanceof VisibleVariableNode)
												{
													varCell = (mxCell)((mxCell)outgoingEdges[i]).getTarget();
												}
											}
											if (varCell != null)
											{
												mxCell varToPortCell = (mxCell)createConnectionCell(null, varCell, target, "ConnectionEdge");
												AC_GUI.addConnection(activeModule, varToPortCell, TerminalType.VISIBLEVARIABLE, targetType);
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
										newName = generateName(sourcePort.getPortDefinition().getRefName());
										mxCell varCell = (mxCell)createVisibleVariableCell(null);
										addCell(null, varCell);
										setCellGeometryToCenterOfEdge(varCell, cell);
										AC_GUI.addVisibleVariable(activeModule, newName, varCell);
										mxCell sourceToVarCell = (mxCell)createConnectionCell(null, source, varCell, "DashedConnectionEdge");
										AC_GUI.addConnection(activeModule, sourceToVarCell, sourceType, TerminalType.VISIBLEVARIABLE);
										mxCell varToTargetCell = (mxCell)createConnectionCell(null, varCell, target, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, varToTargetCell, TerminalType.VISIBLEVARIABLE, targetType);
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
												AC_GUI.addConnection(activeModule, eNodeToTargetCell, TerminalType.EQUIVALENCE, targetType);
											
											}
											return;
										}
										// the source has no existing outgoing edges
										System.out.println("Source has no existing edges.");
										// create an equivalence node
										newName = generateName(sourcePort.getPortDefinition().getRefName());
										eNodeCell = (mxCell)createEquivalenceNodeCell(null);
										addCell(null, eNodeCell);
										setCellGeometryToCenterOfEdge(eNodeCell, cell);
										AC_GUI.addEquivalenceNode(activeModule, newName, eNodeCell);
										sourceToeNodeCell = (mxCell)createConnectionCell(null, eNodeCell, source, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, sourceToeNodeCell, TerminalType.EQUIVALENCE, sourceType);
										eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
										AC_GUI.addConnection(activeModule, eNodeToTargetCell, TerminalType.EQUIVALENCE, targetType);
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
											sourceType = TerminalType.PORT;
											targetType = TerminalType.PORT;
											// create an equivalence node
											newName = generateName(sourcePort.getPortDefinition().getRefName());
											eNodeCell = (mxCell)createEquivalenceNodeCell(null);
											addCell(null, eNodeCell);
											setCellGeometryToCenterOfEdge(eNodeCell, cell);
											AC_GUI.addEquivalenceNode(activeModule, newName, eNodeCell);
											sourceToeNodeCell = (mxCell)createConnectionCell(null, eNodeCell, source, "ConnectionEdge");
											AC_GUI.addConnection(activeModule, sourceToeNodeCell, TerminalType.EQUIVALENCE, sourceType);
											eNodeToTargetCell = (mxCell)createConnectionCell(null, eNodeCell, target, "ConnectionEdge");
											AC_GUI.addConnection(activeModule, eNodeToTargetCell, TerminalType.EQUIVALENCE, targetType);
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
											sourceType = TerminalType.PORT;
											targetType = TerminalType.PORT;
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
													AC_GUI.addConnection(activeModule, eNodeToTargetCell, TerminalType.EQUIVALENCE, targetType);
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
											sourceType = TerminalType.PORT;
											targetType = TerminalType.PORT;
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
												AC_GUI.addConnection(activeModule, eNodeToSourceCell, TerminalType.EQUIVALENCE, sourceType);
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
}
