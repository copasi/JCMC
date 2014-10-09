package acgui;

import java.util.Map;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

/**
 * Extension of the mxGraph class.
 * @author T.C. Jones
 */
public class ACGraph extends mxGraph
{

	/**
	 * 
	 */
	public ACGraph()
	{
		super();
	}

	/**
	 * @param arg0
	 */
	public ACGraph(mxIGraphModel arg0)
	{
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public ACGraph(mxStylesheet arg0)
	{
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ACGraph(mxIGraphModel arg0, mxStylesheet arg1)
	{
		super(arg0, arg1);
	}

	/**
	 * Returns the textual representation for the given cell.
	 * 
	 * @param cell Cell to be converted to a string.
	 * @return Returns the textual representation of the cell.
	 */
	public String convertValueToString(Object cell)
	{
		String label = null;
		Object result = model.getValue(cell);

		if ((result instanceof Module) && (AC_GUI.activeModule != null))
		{
			Module module = (Module)result;
			ModuleDefinition definition = module.getModuleDefinition();
			if ((module != AC_GUI.activeModule) && !(definition instanceof MathematicalAggregatorDefinition))
			{
				label = module.getModuleDefinition().getName();
				label += AC_Utility.eol;
				label += "\"" + module.getName() + "\"";
			}
		}
		
		if (label == null)
		{
			label = "";
			if (result != null)
			{
				label = result.toString();
			}
		}
		//return (result != null) ? result.toString() : "";
		return label;
	}
	
	// Ports are not used as terminals for edges, they are
	// only used to compute the graphical connection point
	/*
	 * public boolean isPort(Object cell) { mxGeometry geo = getCellGeometry(cell); return (geo != null) ?
	 * geo.isRelative() : false; }
	 */
	// Implements a tooltip that shows the actual
	// source and target of an edge
	public String getToolTipForCell(Object cell)
	{
		if (model.isEdge(cell))
		{
			return convertValueToString(model.getTerminal(cell, true)) + " -> "
					+ convertValueToString(model.getTerminal(cell, false));
		}

		if(((mxCell)cell).getValue() instanceof PortNode)
		{
			if(((PortNode)((mxCell)cell).getValue()).getParent() == AC_GUI.activeModule)
			{
				return ((PortNode)((mxCell)cell).getValue()).getPortDefinition().getRefName() + " = " + AC_GUI.modelBuilder.getValue(((PortNode)((mxCell)cell).getValue()).getPortDefinition().getRefName());
			}
		}
		
		return super.getToolTipForCell(cell);
	}

	// Removes the folding icon and disables any folding
	public boolean isCellFoldable(Object cell, boolean collapse)
	{
		return false;
	}

	/**
	 * Returns true if the given cell is movable. This implementation returns movable.
	 * 
	 * @param cell Cell whose movable state should be returned.
	 * @return Returns true if the cell is movable.
	 */
	public boolean isCellMovable(Object cell)
	{
		mxCellState state = view.getState(cell);
		Map<String, Object> style = (state != null) ? state.getStyle() : getCellStyle(cell);
		// System.out.println("isCellMovable(" + cell.toString() + ")");
		/*
		 * return isCellsMovable() && !isCellLocked(cell) && mxUtils.isTrue(style, mxConstants.STYLE_MOVABLE, true);
		 */
		return mxUtils.isTrue(style, mxConstants.STYLE_MOVABLE, true);
	}

	/**
	 * Translates the geometry of the given cell and stores the new, translated geometry in the model as an atomic
	 * change.
	 * @param cell the object to translate
	 * @param dx the change in the x position
	 * @param dy the change in the y position
	 */
	public void translateCell(Object cell, double dx, double dy)
	{

		mxGeometry geo = model.getGeometry(cell);
		Object value = model.getValue(cell);
		double currentX = geo.getX();
		double currentY = geo.getY();
		
		if (geo != null)
		{
			//System.out.println("Geo is not null. dx=" + dx + " dy=" + dy);
			geo = (mxGeometry) geo.clone();
			geo.translate(dx, dy);

			/*
			if (model.isVertex(cell) && !isAllowNegativeCoordinates())
			{
				geo.setX(Math.max(0, geo.getX()));
				geo.setY(Math.max(0, geo.getY()));
			}
			*/
			
			if (value instanceof acgui.PortNode)
			{
				Object cellParent = model.getParent(cell);
				if (cellParent == getDefaultParent() || cellParent == getCurrentRoot())
				{
					System.err.println("Problem: Parent cell is defaultParent or currentRoot.");
					return;
				}
				else if (cellParent != null)
				{
					mxGeometry parentGeo = model.getGeometry(cellParent);
					if (parentGeo != null)
					{
						double parentWidth = parentGeo.getWidth();
						double parentHeight = parentGeo.getHeight();
						double relativeDx = 0.0;
						double relativeDy = 0.0;
						double newX = 0.0;
						double newY = 0.0;

						if (((currentX == 0.0) || (currentX == 1.0)) && ((currentY != 0.0) && (currentY != 1.0)))
						{
							// port is on left or right side of the parent

							// normalize the Y translation
							relativeDy = dy / parentHeight;
						}
						else if (((currentY == 0.0) || (currentY == 1.0)) && ((currentX != 0.0) && (currentX != 1.0)))
						{
							// port is on the top or bottom of the parent

							// normalize the X translation
							relativeDx = dx / parentWidth;
						}
						else
						{
							// port is in a corner
							//System.out.println("port is in a corner");
							if (Math.abs(dx) > Math.abs(dy))
							{
								relativeDx = dx / parentWidth;
							}
							else
							{
								relativeDy = dy / parentHeight;
							}
						}

						// Calculate the relative translation

						if (relativeDx < 0) // the port was translated to the left
						{
							// ensure the port stays on the parent border
							newX = Math.max(0.0, (currentX + relativeDx));
						}
						else if (relativeDx > 0) // the port was translated to the right
						{
							// ensure the port stays on the parent border
							newX = Math.min(1.0, (currentX + relativeDx));
						}
						else
						{
							// no horizontal translation occurred
							newX = currentX;
						}

						if (relativeDy < 0) // the port was translated down
						{
							// ensure the port stays on the parent border
							newY = Math.max(0.0, (currentY + relativeDy));
						}
						else if (relativeDy > 0) // the port was translated up
						{
							// ensure the port stays on the parent border
							newY = Math.min(1.0, (currentY + relativeDy));
						}
						else
						{
							// no vertical translation occurred
							newY = currentY;
						}

						// set the new coordinates
						geo.setX(newX);
						geo.setY(newY);

						// update the port orientation
						//updatePortOrientation(cell, geo, false);
					}
					else
					{
						System.out.println("Problem: Parent geometry is null.");
						return;
					}
				}
				else
				{
					System.out.println("Problem: Parent cell is null.");
					return;
				}
			}

			model.beginUpdate();
			try
			{
				model.setGeometry(cell, geo);
			}
			finally
			{
				model.endUpdate();
			}
			
			if (value instanceof acgui.PortNode)
			{
				// update the port orientation
				updatePortOrientation(cell, geo, false);
			}
		}
	}
	
	/**
	 * Translates the geometry of the given cell and stores the new, translated geometry in the model as an atomic
	 * change.
	 * @param cell the object to translate
	 * @param dx the change in the x position
	 * @param dy the change in the y position
	 */
	public void xtranslateCell(Object cell, double dx, double dy)
	{

		mxGeometry geo = model.getGeometry(cell);
		double currentX = geo.getX();
		double currentY = geo.getY();

		if (geo != null)
		{
			geo = (mxGeometry) geo.clone();
			geo.translate(dx, dy);

			if (!geo.isRelative() && model.isVertex(cell) && !isAllowNegativeCoordinates())
			{
				geo.setX(Math.max(0, geo.getX()));
				geo.setY(Math.max(0, geo.getY()));
			}

			if (geo.isRelative() && !model.isEdge(cell))
			{
				Object cellParent = model.getParent(cell);
				if (cellParent == getDefaultParent() || cellParent == getCurrentRoot())
				{
					System.out.println("Problem: Parent cell is defaultParent or currentRoot.");
					System.exit(0);
				}
				else if (cellParent != null)
				{
					mxGeometry parentGeo = model.getGeometry(cellParent);
					if (parentGeo != null)
					{
						double parentWidth = parentGeo.getWidth();
						double parentHeight = parentGeo.getHeight();
						double relativeDx = 0.0;
						double relativeDy = 0.0;
						double newX = 0.0;
						double newY = 0.0;

						if (((currentX == 0.0) || (currentX == 1.0)) && ((currentY != 0.0) && (currentY != 1.0)))
						{
							// port is on left or right side of the parent

							// normalize the Y translation
							relativeDy = dy / parentHeight;
						}
						else if (((currentY == 0.0) || (currentY == 1.0)) && ((currentX != 0.0) && (currentX != 1.0)))
						{
							// port is on the top or bottom of the parent

							// normalize the X translation
							relativeDx = dx / parentWidth;
						}
						else
						{
							// port is in a corner
							//System.out.println("port is in a corner");
							if (Math.abs(dx) > Math.abs(dy))
							{
								relativeDx = dx / parentWidth;
							}
							else
							{
								relativeDy = dy / parentHeight;
							}
						}

						// Calculate the relative translation

						if (relativeDx < 0) // the port was translated to the left
						{
							// ensure the port stays on the parent border
							newX = Math.max(0.0, (currentX + relativeDx));
						}
						else if (relativeDx > 0) // the port was translated to the right
						{
							// ensure the port stays on the parent border
							newX = Math.min(1.0, (currentX + relativeDx));
						}
						else
						{
							// no horizontal translation occurred
							newX = currentX;
						}

						if (relativeDy < 0) // the port was translated down
						{
							// ensure the port stays on the parent border
							newY = Math.max(0.0, (currentY + relativeDy));
						}
						else if (relativeDy > 0) // the port was translated up
						{
							// ensure the port stays on the parent border
							newY = Math.min(1.0, (currentY + relativeDy));
						}
						else
						{
							// no vertical translation occurred
							newY = currentY;
						}

						// set the new coordinates
						geo.setX(newX);
						geo.setY(newY);

						// update the port orientation
						updatePortOrientation(cell, geo, false);
					}
					else
					{
						System.out.println("Problem: Parent geometry is null.");
						System.exit(0);
					}
				}
				else
				{
					System.out.println("Problem: Parent cell is null.");
					System.exit(0);
				}
			}

			model.beginUpdate();
			try
			{
				model.setGeometry(cell, geo);
			}
			finally
			{
				model.endUpdate();
			}
		}
	}

	public void updatePortOrientation(Object cell, mxGeometry geo, boolean isMini)
	{
		double xCoord = geo.getX();
		double yCoord = geo.getY();
		String newStyle = "";
		String shapeType = "";
		String shapeOrientation = "";
		String shapeColor = "";

		PortNode port = (PortNode) ((mxCell) cell).getValue();
		PortType portType = port.getPortDefinition().getType();

		if (portType == PortType.OUTPUT)
		{
			if (xCoord == 0.0)
			{
				shapeOrientation = "West";
			}
			else if (xCoord == 1.0)
			{
				shapeOrientation = "East";
			}
			else if (yCoord == 0.0)
			{
				shapeOrientation = "North";
			}
			else if (yCoord == 1.0)
			{
				shapeOrientation = "South";
			}
		}
		else
		{
			if (xCoord == 0.0)
			{
				shapeOrientation = "East";
			}
			else if (xCoord == 1.0)
			{
				shapeOrientation = "West";
			}
			else if (yCoord == 0.0)
			{
				shapeOrientation = "South";
			}
			else if (yCoord == 1.0)
			{
				shapeOrientation = "North";
			}
		}

		newStyle = portType.toString() + "Port_" + shapeOrientation;

		if (isMini)
		{
			newStyle += "_" + Constants.MINI;
		}
		
		model.beginUpdate();
		try
		{
			model.setStyle(cell, newStyle);
		}
		finally
		{
			model.endUpdate();
		}
		port.setDrawingCellStyle(newStyle);
		
		Object outgoingEdges [] = mxGraphModel.getOutgoingEdges(this.getModel(), cell);
		Object incomingEdges [] = mxGraphModel.getIncomingEdges(this.getModel(), cell);
		
		if (outgoingEdges.length > 0 )
		{
			for (int i = 0; i < outgoingEdges.length; i++)
			{
				updateConnectionOrientation(outgoingEdges[i], isMini);
			}
		}
		
		if (incomingEdges.length > 0)
		{
			for (int i = 0; i < incomingEdges.length; i++)
			{
				updateConnectionOrientation(incomingEdges[i], isMini);
			}
		}
	}
	
	public void updateConnectionOrientation(Object cell, boolean isMini)
	{
		ConnectionNode connection = null;
		ConnectionDefinition definition;
		try
		{
			connection = (ConnectionNode)((mxCell)cell).getValue();
		}
		catch (Exception e)
		{
			return;
		}
		definition = connection.getConnectionDefinition();
		String sourceStyle;
		String targetStyle;
		
		sourceStyle = "source" + getConnectionEndOrientation(connection.getParent(), connection.getDrawingCell().getSource(), definition.getSourceType());
		targetStyle = "target" + getConnectionEndOrientation(connection.getParent(), connection.getDrawingCell().getTarget(), definition.getTargetType());
		
		if (isDashedLine(definition.getTargetType(), definition.getTargetDefinition()))
		{
			targetStyle += "_" + Constants.DASHED_EDGE;
		}
		else
		{
			targetStyle += "_" + Constants.SOLID_EDGE;
		}
		
		String connectionStyle = sourceStyle + "_" + targetStyle;
		
		if (isMini)
		{
			connectionStyle += "_" + Constants.MINI;
		}
		model.beginUpdate();
		try
		{
			model.setStyle(connection.getDrawingCell(), connectionStyle);
		}
		finally
		{
			model.endUpdate();
		}
		connection.setDrawingCellStyle(connectionStyle);
	}
	
	public String getConnectionEndOrientation(Module connectionParent, mxICell terminalDrawingCell, TerminalType terminalType)
	{
		String style = new String();
		ACComponentNode terminal = (ACComponentNode)terminalDrawingCell.getValue();
		String terminalStyle = terminal.getDrawingCellStyle();
		double xCoord =terminalDrawingCell.getGeometry().getX();
		double yCoord = terminalDrawingCell.getGeometry().getY();
		PortType portType;
		
		switch (terminalType)
		{
			case EQUIVALENCE:
				style += "_standard";
				break;
			case VISIBLEVARIABLE:
				style += "_standard";
				break;
			case PORT:
				portType = ((PortNode)terminal).getPortDefinition().getType();
				switch (portType)
				{
				case INPUT:
					
					break;
				case OUTPUT:
					
					break;
				case EQUIVALENCE:
					
					break;
				}
				
				if (xCoord == 0.0)
				{
					style += "_west";
				}
				else if (xCoord == 1.0)
				{
					style += "_east";
				}
				else if (yCoord == 0.0)
				{
					style += "_north";
				}
				else if (yCoord == 1.0)
				{
					style += "_south";
				}
				//style += terminalStyle.substring(terminalStyle.indexOf("_")).toLowerCase();
				
				if (connectionParent == terminal.getParent())
				{
					// the terminal port is on the module
					style += "_module";
				}
				else
				{
					// the terminal port is on a submodule
					style += "_submodule";
				}
				break;
			default:
				
		}
		
		return style;
	}
	
	public boolean isDashedLine(TerminalType terminalType, ACComponentDefinition terminalDefinition)
	{
		switch (terminalType)
		{
		case EQUIVALENCE:
			return true;
		case VISIBLEVARIABLE:
			return true;
		case PORT:
			PortType portType = ((PortDefinition)terminalDefinition).getType();
			switch (portType)
			{
			case INPUT:
				return false;
			case OUTPUT:
				return false;
			case EQUIVALENCE:
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Return an error message for the given
	 * edge and terminals if the connection is not valid.
	 * 
	 * @param edge Cell that represents the edge to validate.
	 * @param source Cell that represents the source terminal.
	 * @param target Cell that represents the target terminal.
	 * @return an error message if the edge is not valid,
	 * otherwise return null
	 */
	public String validateEdge(Object edge, Object source, Object target)
	{
		/*
		if (((mxCell)source).getValue() instanceof VisibleVariable)
		{
			System.out.println("Source is a Visible Variable.");
		}
		if (((mxCell)target).getValue() instanceof VisibleVariable)
		{
			System.out.println("Target is a Visible Variable.");
		}
		*/
		Object sourceObject = ((mxCell)source).getValue();
		Object targetObject = ((mxCell)target).getValue();
		
		/*
		if (targetObject instanceof EquivalenceNode)
		{
			return "An equivalence node cannot have an incoming connection.";
		}
		
		if (targetObject instanceof Port)
		{
			PortType targetPortType = ((Port)targetObject).getType();
			
			if((targetPortType.compareTo(PortType.INPUT) == 0) && (mxGraphModel.getDirectedEdgeCount(model, target,
					false, edge) != 0))
			{
				return "An input port cannot have more than one incoming connection.";
			}
			
			if((targetPortType.compareTo(PortType.OUTPUT) == 0) && (mxGraphModel.getDirectedEdgeCount(model, target,
					false, edge) != 0))
			{
				return "An output port cannot have more than one incoming connection.";
			}
		}
		*/
		if (sourceObject instanceof VisibleVariableNode)
		{
			// the source is a visible variable
			VisibleVariableNode sourceVisibleVariable = (VisibleVariableNode)sourceObject;
			
			if (targetObject instanceof VisibleVariableNode)
			{
				// the source is a visible variable
				// the target is a visible variable
				return "A variable cannot connect to another variable.";
			}
			else if (targetObject instanceof EquivalenceNode)
			{
				// the source is a visible variable
				// the target is an equivalence node
				return "A variable cannot connect to an equivalence node.";
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
					switch(targetPortType)
					{
					case INPUT:
						return "A variable cannot connect to a module input port.";
					case OUTPUT:
						if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
						{
							return "An output port cannot have more than one incoming connection.";
						}
						if (createsPortLoop((mxCell)source, (mxCell)target, (mxCell)edge))
						{
							return "This connection would create a port loop.";
						}
						return null;
					case EQUIVALENCE:
						return "A variable cannot connect to a module equivalence port.";
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
						if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
						{
							return "An input port cannot have more than one incoming connection.";
						}
						if (createsPortLoop((mxCell)source, (mxCell)target, (mxCell)edge))
						{
							return "This connection would create a port loop.";
						}
						return null;
					case OUTPUT:
						return "A variable cannot connect to a submodule output port.";
					case EQUIVALENCE:
						return "A variable cannot connect to a submodule equivalence port.";
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
				return "An equivalence node cannot connect to another equivalence node.";
			}
			if (targetObject instanceof VisibleVariableNode)
			{
				// the source is an equivalence node
				// the target is a visible variable
				/*
				if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
				{
					return "A variable cannot have more than one incoming connection.";
				}
				return null;
				*/
				return "An equivalence node cannot connect to a variable.";
			}
			else if (targetObject instanceof PortNode)
			{
				// the source is an equivalence node
				// the target is a port
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
						return "An equivalence node cannot connect to a module input port.";
					case OUTPUT:
						if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
						{
							return "An output port cannot have more than one incoming connection.";
						}
						return null;
					case EQUIVALENCE:
						if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
						{
							return "An equivalence port cannot have more than one incoming connection.";
						}
						return null;
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
						if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
						{
							return "An input port cannot have more than one incoming connection.";
						}
						return null;
					case OUTPUT:
						return "An equivalence node cannot connect to a submodule output port.";
					case EQUIVALENCE:
						if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
						{
							return "An equivalence port cannot have more than one incoming connection.";
						}
						return null;
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
					return "A module port cannot connect to an equivalence node.";
				}
				else if (targetObject instanceof VisibleVariableNode)
				{
					// the source is a port
					// the sourceModule is the active module
					// the target is a visible variable
					switch(sourcePortType)
					{
					case INPUT:
						if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
						{
							return "A variable cannot have more than one incoming connection.";
						}
						if (createsPortLoop((mxCell)source, (mxCell)target, (mxCell)edge))
						{
							return "This connection would create a port loop.";
						}
						return null;
					case OUTPUT:
						return "A module output port cannot connect to a variable.";
					case EQUIVALENCE:
						if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
						{
							return "A variable cannot have more than one incoming connection.";
						}
						if (createsPortLoop((mxCell)source, (mxCell)target, (mxCell)edge))
						{
							return "This connection would create a port loop.";
						}
						return null;
					default:
							
					}
				}
				else if (targetObject instanceof PortNode)
				{
					// the source is a port
					// the sourceModule is the active module
					// the target is a port
					PortNode targetPort = (PortNode)targetObject;
					PortType targetPortType = targetPort.getPortDefinition().getType();
					Module targetModule = targetPort.getParent();
					
					if (sourceModule == targetModule)
					{
						// the source and target ports belong to the same module
						return "Ports of the same module cannot share a connection.";
					}
					
					// since the sourceModule is the active module,
					// the targetModule must be a submodule
					switch(sourcePortType)
					{
					case INPUT:
						switch(targetPortType)
						{
						case INPUT:
							if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
							{
								return "An input port cannot have more than one incoming connection.";
							}
							return null;
						case OUTPUT:
							return "A module input port cannot connect to a submodule output port.";
						case EQUIVALENCE:
							return "A module input port cannot connect to a submodule equivalence port.";
						default:
						}
						break;
					case OUTPUT:
						switch(targetPortType)
						{
						case INPUT:
							return "A module output port cannot connect to a submodule input port.";
						case OUTPUT:
							return "A module output port cannot connect to a submodule output port.";
						case EQUIVALENCE:
							return "A module output port cannot connect to a submodule equivalence port.";
						default:
						}
						break;
					case EQUIVALENCE:
						switch(targetPortType)
						{
						case INPUT:
							if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
							{
								return "An input port cannot have more than one incoming connection.";
							}
							return null;
						case OUTPUT:
							return "A module equivalence port cannot connect to a submodule output port.";
						case EQUIVALENCE:
							/*
							if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
							{
								return "An equivalence port cannot have more than one incoming connection.";
							}
							*/
							return null;
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
					return "A submodule port cannot connect to an equivalence node.";
				}
				else if (targetObject instanceof VisibleVariableNode)
				{
					// the source is a port
					// the sourceModule is a submodule
					// the target is a visible variable
					switch(sourcePortType)
					{
					case INPUT:
						return "A submodule input port cannot connect to a variable.";
					case OUTPUT:
						if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
						{
							return "A variable cannot have more than one incoming connection.";
						}
						if (createsPortLoop((mxCell)source, (mxCell)target, (mxCell)edge))
						{
							return "This connection would create a port loop.";
						}
						return null;
					case EQUIVALENCE:
						if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
						{
							return "A variable cannot have more than one incoming connection.";
						}
						if (createsPortLoop((mxCell)source, (mxCell)target, (mxCell)edge))
						{
							return "This connection would create a port loop.";
						}
						return null;
					default:
					}
				}
				else if (targetObject instanceof PortNode)
				{
					// the source is a port
					// the sourceModule is a submodule
					// the target is a port
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
								return "A submodule input port cannot connect to a module input port.";
							case OUTPUT:
								return "A submodule input port cannot connect to a module output port.";
							case EQUIVALENCE:
								return "A submodule input port cannot connect to a module equivalence port.";
							default:
							}
							break;
						case OUTPUT:
							switch(targetPortType)
							{
							case INPUT:
								return "A submodule output port cannot connect to a module input port.";
							case OUTPUT:
								if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
								{
									return "An output port cannot have more than one incoming connection.";
								}
								return null;
							case EQUIVALENCE:
								return "A submodule output port cannot connect to a module equivalence port.";
							default:
							}
							break;
						case EQUIVALENCE:
							switch(targetPortType)
							{
							case INPUT:
								return "A submodule equivalence port cannot connect to a module input port.";
							case OUTPUT:
								return "A submodule equivalence port cannot connect to a module output port.";
							case EQUIVALENCE:
								/*
								if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
								{
									return "An equivalence port cannot have more than one incoming connection.";
								}
								*/
								return null;
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
							return "Ports of the same submodule cannot connect.";
						}
						
						switch(sourcePortType)
						{
						case INPUT:
							switch(targetPortType)
							{
							case INPUT:
								return "A submodule input port cannot connect to a submodule module input port.";
							case OUTPUT:
								return "A submodule input port cannot connect to a submodule output port.";
							case EQUIVALENCE:
								return "A submodule input port cannot connect to a submodule equivalence port.";
							default:
							}
							break;
						case OUTPUT:
							switch(targetPortType)
							{
							case INPUT:
								if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
								{
									return "An input port cannot have more than one incoming connection.";
								}
								return null;
							case OUTPUT:
								return "A submodule output port cannot connect to a submodule output port.";
							case EQUIVALENCE:
								return "A submodule output port cannot connect to a submodule equivalence port.";
							default:
							}
							break;
						case EQUIVALENCE:
							switch(targetPortType)
							{
							case INPUT:
								if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
								{
									return "An input port cannot have more than one incoming connection.";
								}
								return null;
							case OUTPUT:
								return "A submodule equivalence port cannot connect to a submodule output port.";
							case EQUIVALENCE:
								/*
								if (mxGraphModel.getDirectedEdgeCount(model, target, false, edge) != 0)
								{
									return "An equivalence port cannot have more than one incoming connection.";
								}
								*/
								return null;
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
		if (sourceObject instanceof Port && targetObject instanceof Port)
		{
			Port sourcePort = (Port)sourceObject;
			Port targetPort = (Port)targetObject;
			Module sourceModule = sourcePort.getParent();
			Module targetModule = targetPort.getParent();
			
			//System.out.println("Source: " + ((mxCell)source).getValue().toString());
			//System.out.println("Target: " + ((mxCell)target).getValue().toString());
			PortType sourcePortType = ((Port)sourceObject).getType();
			PortType targetPortType = ((Port)targetObject).getType();
			
			if(sourceModule == targetModule)
			{
				return "Two ports of the same Module cannot be connected.";
			}
			
			if(sourceModule == targetModule.getParent())
			{
				// the targetModule is a submodule
				switch(sourcePortType)
				{
				case INPUT:
					if (targetPortType.compareTo(PortType.OUTPUT) == 0)
					{
						return "A Module input port cannot connect to a Submodule output port.";
					}
					break;
				case OUTPUT:
					if (targetPortType.compareTo(PortType.INPUT) == 0)
					{
						return "A Module output port cannot connect to a Submodule input port.";
					}
					break;
				case EQUIVALENCE:
					if (targetPortType.compareTo(PortType.OUTPUT) == 0)
					{
						return "A Module equivalence port cannot connect to a Submodule output port.";
					}
					break;
				default:
				}
			}
			else if (sourceModule.getParent() == targetModule)
			{
				// the sourceModule is a submodule
				switch(sourcePortType)
				{
				case INPUT:
					if (targetPortType.compareTo(PortType.OUTPUT) == 0)
					{
						return "A Submodule input port cannot connect to a Module output port.";
					}
					break;
				case OUTPUT:
					switch(targetPortType)
					{
					case INPUT:
						return "A Submodule output port cannot connect to a Module input port.";
					case OUTPUT:
						break;
					case EQUIVALENCE:
						return "A Submodule output port cannot connect to a Module equivalence port.";
					default:
					}
					break;
				case EQUIVALENCE:
					break;
				default:
				}
			}
			else
			{
				// both the sourceModule and the targetModule are submodules
				switch(sourcePortType)
				{
				case INPUT:
					switch(targetPortType)
					{
					case INPUT:
						return "A Submodule input port cannot connect to another Submodule input port.";
					case OUTPUT:
						return "A Submodule input port cannot connect to another Submodule output port.";
					case EQUIVALENCE:
						return "A Submodule input port cannot connect to another Submodule equivalence port.";
					default:
					}
				case OUTPUT:
					if (targetPortType.compareTo(PortType.OUTPUT) == 0)
					{
						return "A Submodule output port cannot connect to another Submodule output port.";
					}
					break;
				case EQUIVALENCE:
					if (targetPortType.compareTo(PortType.OUTPUT) == 0)
					{
						return "A Submodule equivalence port cannot connect to another Submodule output port.";
					}
					break;
				default:
				}
			}
		}
		*/
		/*
		if ((sourceType.compareTo(PortType.OUTPUT) == 0) && (targetType.compareTo(PortType.INPUT) == 0))
		{
			return null;
		}
		else
		{
			return "Incorrect port match.";
		}
		*/
		return null;
	}
	
	private boolean createsPortLoop(mxCell source, mxCell target, mxCell edge)
	{
		Module sourceModule = null;
		if (((mxCell)source).getValue() instanceof VisibleVariableNode)
		{
			if (mxGraphModel.getDirectedEdgeCount(model, source, false, edge) == 0)
			{
				return false;
			}
			//mxCell existingEdgeSource = (mxCell)((mxCell)model.getEdgeAt(source, 0)).getSource();
			mxCell existingEdgeSource = (mxCell)((mxCell)mxGraphModel.getIncomingEdges(model, source)[0]).getSource();
			if (existingEdgeSource.getValue() instanceof PortNode)
			{
				sourceModule = ((PortNode)existingEdgeSource.getValue()).getParent();
				if (target.getValue() instanceof PortNode)
				{
					if (((PortNode)target.getValue()).getParent() == sourceModule)
					{
						return true;
					}
					return false;
				}
			}
			else
			{
				return false;
			}
		}
		else if (((mxCell)source).getValue() instanceof PortNode)
		{
			if (mxGraphModel.getDirectedEdgeCount(model, target, true, edge) == 0)
			{
				return false;
			}
			sourceModule = ((PortNode)((mxCell)source).getValue()).getParent();
		}
		else
		{
			return true;
		}
		
		Object targetOutgoingEdges [] = mxGraphModel.getOutgoingEdges(model, target);
		for (int i = 0; i < targetOutgoingEdges.length; i++)
		{
			if (((mxCell)targetOutgoingEdges[i]).getTarget().getValue() instanceof PortNode)
			{
				PortNode port = (PortNode)((mxCell)targetOutgoingEdges[i]).getTarget().getValue();
				if (port.getParent() == sourceModule)
				{
					return true;
				}
			}
		}
		return false;
	}
}
