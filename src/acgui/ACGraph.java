package acgui;

import java.util.Map;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
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

		if(((mxCell)cell).getValue() instanceof Port)
		{
			if(((Port)((mxCell)cell).getValue()).getParent() == AC_GUI.activeModule)
			{
				return ((Port)((mxCell)cell).getValue()).getRefName() + " = " + AC_GUI.modelBuilder.getValue(((Port)((mxCell)cell).getValue()).getRefName());
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
						updatePortOrientation(cell, geo);
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

	public void updatePortOrientation(Object cell, mxGeometry geo)
	{
		//mxGeometry geo = ((mxCell) cell).getGeometry();
		double xCoord = geo.getX();
		double yCoord = geo.getY();
		String newStyle = "";
		String shapeType = "";
		String shapeOrientation = "";
		String shapeColor = "";

		Port port = (Port) ((mxCell) cell).getValue();
		PortType portType = port.getType();

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

		model.beginUpdate();
		try
		{
			model.setStyle(cell, newStyle);
		}
		finally
		{
			model.endUpdate();
		}
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
		if (((mxCell)target).getValue() instanceof Port)
		{
			PortType targetType = ((Port)((mxCell)target).getValue()).getType();
			
			if((targetType.compareTo(PortType.INPUT) == 0) && (mxGraphModel.getDirectedEdgeCount(model, target,
					false, edge) != 0))
			{
				return "An input port cannot have more than one incoming connection.";
			}
			
			if((targetType.compareTo(PortType.OUTPUT) == 0) && (mxGraphModel.getDirectedEdgeCount(model, target,
					false, edge) != 0))
			{
				return "An output port cannot have more than one incoming connection.";
			}
		}
		
		if (((mxCell)source).getValue() instanceof Port && ((mxCell)target).getValue() instanceof Port)
		{
			Port sourcePort = (Port)((mxCell)source).getValue();
			Port targetPort = (Port)((mxCell)target).getValue();
			Module sourceModule = sourcePort.getParent();
			Module targetModule = targetPort.getParent();
			
			//System.out.println("Source: " + ((mxCell)source).getValue().toString());
			//System.out.println("Target: " + ((mxCell)target).getValue().toString());
			PortType sourceType = ((Port)((mxCell)source).getValue()).getType();
			PortType targetType = ((Port)((mxCell)target).getValue()).getType();
			
			if(sourceModule == targetModule)
			{
				return "Two ports of the same Module cannot be connected.";
			}
			
			if(sourceModule == targetModule.getParent())
			{
				// the targetModule is a submodule
				switch(sourceType)
				{
				case INPUT:
					if (targetType.compareTo(PortType.OUTPUT) == 0)
					{
						return "A Module input port cannot connect to a Submodule output port.";
					}
					break;
				case OUTPUT:
					if (targetType.compareTo(PortType.INPUT) == 0)
					{
						return "A Module output port cannot connect to a Submodule input port.";
					}
					break;
				case EQUIVALENCE:
					if (targetType.compareTo(PortType.OUTPUT) == 0)
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
				switch(sourceType)
				{
				case INPUT:
					if (targetType.compareTo(PortType.OUTPUT) == 0)
					{
						return "A Submodule input port cannot connect to a Module output port.";
					}
					break;
				case OUTPUT:
					switch(targetType)
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
				switch(sourceType)
				{
				case INPUT:
					switch(targetType)
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
					if (targetType.compareTo(PortType.OUTPUT) == 0)
					{
						return "A Submodule output port cannot connect to another Submodule output port.";
					}
					break;
				case EQUIVALENCE:
					if (targetType.compareTo(PortType.OUTPUT) == 0)
					{
						return "A Submodule equivalence port cannot connect to another Submodule output port.";
					}
					break;
				default:
				}
			}
		}
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
}
