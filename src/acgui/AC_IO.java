package acgui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.COPASI.CCopasiDataModel;

import com.mxgraph.util.mxRectangle;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

/**
 * @author T.C. Jones
 * @version March 25, 2013
 */
public class AC_IO
{

	/**
	 * 
	 */
	public AC_IO()
	{
		
	}
	
	public static void saveModule(Module mod, String fileName)
	{
		Map<String, Object> data = packModule(mod);
		
		try
		{ 
			FileOutputStream fout = new FileOutputStream(fileName);
			ObjectOutputStream oos = new ObjectOutputStream(fout);   
			oos.writeObject(data);
			oos.close();
			System.out.println("Done");
		}catch(Exception ex){
		   ex.printStackTrace();
		}
	}
	
	public static Module loadModule(String fileName)
	{
		return loadModule(fileName, null);
	}
	
	public static Module loadModule(String fileName, Module parent)
	{
		Map<String, Object> data = null;
		Module mod = null;
		
		try
		{	 
			FileInputStream fin = new FileInputStream(fileName);
			ObjectInputStream ois = new ObjectInputStream(fin);
			data = ((HashMap<String, Object>)ois.readObject());
			ois.close();
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		} 
		
		if(data != null)
		{
			mod = unpackModule(data, parent);
			System.out.println(mod.getName() + " successfully loaded.");
		}
		
		return mod;
	}
	
	private static Map<String, Object> packModule(Module mod)
	{
		Map<String, Object> data = new HashMap<String, Object>();
		ArrayList<Map<String, Object>> packedList;
		data.put("name", mod.getName());
		data.put("msmbData", mod.getMSMBData());
		data.put("treeNode", mod.getTreeNode());
		data.put("drawingCell", mod.getDrawingCell());
		data.put("copasiDatamodelKey", mod.getKey());
		data.put("drawingCellStyle", mod.getDrawingCellStyle());
		data.put("drawingCellBounds", mod.getDrawingCellBounds());
		data.put("drawingCellGeometry", mod.getDrawingCellGeometry());
		
		if(mod instanceof MathematicalAggregator)
		{
			data.put("mathAgg", "yes");
			return packMathAggregator(data, (MathematicalAggregator)mod);
		}
		data.put("mathAgg", "no");
		
		// pack the submodules
		packedList = null;
		int submoduleCount = mod.getChildren().size();
		if(submoduleCount != 0)
		{
			packedList = new ArrayList<Map<String, Object>>(submoduleCount);
			ListIterator<Module> children = mod.getChildren().listIterator();
			while(children.hasNext())
			{
				packedList.add(packModule(children.next()));
			}
		}
		
		data.put("children", packedList);
		
		// pack the ports
		packedList = null;
		int portCount = mod.getPorts().size();
		if(portCount != 0)
		{
			packedList = new ArrayList<Map<String, Object>>(portCount);
			ListIterator<Port> ports = mod.getPorts().listIterator();
			while(ports.hasNext())
			{
				packedList.add(packPort(ports.next()));
			}
		}
		
		data.put("ports", packedList);
		
		// pack the visible variables
		packedList = null;
		int visibleVariableCount = mod.getVisibleVariables().size();
		if(visibleVariableCount != 0)
		{
			packedList = new ArrayList<Map<String, Object>>(visibleVariableCount);
			ListIterator<VisibleVariable> vars = mod.getVisibleVariables().listIterator();
			while(vars.hasNext())
			{
				packedList.add(packVisibleVariable(vars.next()));
			}
		}
		
		data.put("visibleVariables", packedList);
		
		// pack the equivalence nodes
		packedList = null;
		int equivalenceNodeCount = mod.getEquivalenceNodes().size();
		if(equivalenceNodeCount != 0)
		{
			packedList = new ArrayList<Map<String, Object>>(equivalenceNodeCount);
			ListIterator<EquivalenceNode> eNodes = mod.getEquivalenceNodes().listIterator();
			while(eNodes.hasNext())
			{
				packedList.add(packEquivalenceNode(eNodes.next()));
			}
		}
		
		data.put("equivalenceNodes", packedList);
		
		// pack the connections
		packedList = null;
		int connectionCount = mod.getConnections().size();
		if(connectionCount != 0)
		{
			packedList = new ArrayList<Map<String, Object>>(connectionCount);
			ListIterator<Connection> connections = mod.getConnections().listIterator();
			while(connections.hasNext())
			{
				packedList.add(packConnection(connections.next()));
			}
		}
		
		data.put("connections", packedList);
		
		return data;
	}
	
	private static Module unpackModule(Map<String, Object> data, Module parent)
	{
		String aggregator = (String)data.get("mathAgg");
		if (aggregator.equals("yes"))
		{
			return unpackMathAggregator(data, parent);
		}
		String name = (String)data.get("name");
		String msmbData = (String)data.get("msmbData");
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)data.get("treeNode");
		Object drawingCell = data.get("drawingCell");
		//String datamodelKey = (String)data.get("copasiDatamodelKey");
		String cellStyle = (String)data.get("drawingCellStyle");
		mxRectangle oldBounds = (mxRectangle)data.get("drawingCellBounds");
		mxGeometry oldGeo = (mxGeometry)data.get("drawingCellGeometry");
		
		mxRectangle newBounds = null;
		if(oldBounds != null)
		{
			newBounds = new mxRectangle(oldBounds.getX(), oldBounds.getY(), oldBounds.getWidth(), oldBounds.getHeight());
		}
		
		mxGeometry newGeo = null;
		if(oldGeo != null)
		{
			newGeo = new mxGeometry(oldGeo.getX(), oldGeo.getY(), oldGeo.getWidth(), oldGeo.getHeight());
		}
		
		if (cellStyle.equals("Module") && (parent!=null))
		{
			cellStyle = "Submodule";
		}
		CCopasiDataModel dataModel = AC_GUI.copasiUtility.createDataModel();
		dataModel.getModel().setObjectName(name);
		Module mod = new Module(name, dataModel.getModel().getKey(), msmbData, treeNode, drawingCell, newBounds, newGeo, cellStyle, parent);
		//((mxCell)drawingCell).setValue(mod);
		//treeNode.setUserObject(mod);
		if (parent == null)
		{
			AC_GUI.masterModuleList.add(mod);
		}
		else
		{
			parent.addChild(mod);
		}
		AC_GUI.treeView.addNode(mod);
		AC_GUI.drawingBoard.createCell(mod);
		
		ArrayList<Map<String, Object>> packedList;
		ListIterator<Map<String, Object>> packedListIterator;
		if(data.get("children") != null)
		{
			System.out.println("Found " + mod.getName() + "'s children.");
			packedList = (ArrayList<Map<String, Object>>)data.get("children");
			packedListIterator = packedList.listIterator();
			while(packedListIterator.hasNext())
			{
				mod.addChild(unpackModule(packedListIterator.next(), mod));
			}
		}
		if(data.get("ports") != null)
		{
			System.out.println("Found " + mod.getName() + "'s ports.");
			packedList = (ArrayList<Map<String, Object>>)data.get("ports");
			packedListIterator = packedList.listIterator();
			while(packedListIterator.hasNext())
			{
				mod.addPort(unpackPort(packedListIterator.next(), mod));
			}
		}
		if(data.get("visibleVariables") != null)
		{
			System.out.println("Found " + mod.getName() + "'s visible variables.");
			packedList = (ArrayList<Map<String, Object>>)data.get("visibleVariables");
			packedListIterator = packedList.listIterator();
			while(packedListIterator.hasNext())
			{
				mod.addVisibleVariable(unpackVisibleVariable(packedListIterator.next(), mod));
			}
		}
		if(data.get("equivalenceNodes") != null)
		{
			System.out.println("Found " + mod.getName() + "'s equivalence nodes.");
			packedList = (ArrayList<Map<String, Object>>)data.get("equivalenceNodes");
			packedListIterator = packedList.listIterator();
			while(packedListIterator.hasNext())
			{
				mod.addEquivalenceNode(unpackEquivalenceNode(packedListIterator.next(), mod));
			}
		}
		if(data.get("connections") != null)
		{
			System.out.println("Found " + mod.getName() + "'s connections.");
			packedList = (ArrayList<Map<String, Object>>)data.get("connections");
			packedListIterator = packedList.listIterator();
			while(packedListIterator.hasNext())
			{
				mod.addConnection(unpackConnection(packedListIterator.next(), mod));
			}
		}
		
		return mod;
	}
	
	private static Map<String, Object> packPort(Port port)
	{
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("name", port.getName());
		data.put("refName", port.getRefName());
		data.put("drawingCell", port.getDrawingCell());
		data.put("type", port.getType().toString());
		data.put("variableType", port.getVariableType().toString());
		data.put("rowIndex", port.getRowIndex());
		data.put("drawingCellGeometry", ((mxCell)port.getDrawingCell()).getGeometry());
		
		return data;
	}
	
	private static Port unpackPort(Map<String, Object> data, Module parent)
	{
		String name = (String)data.get("name");
		String refName = (String)data.get("refName");
		Object drawingCell = data.get("drawingCell");
		String type = (String)data.get("type");
		String variableType = (String)data.get("variableType");
		mxGeometry oldCellGeo = (mxGeometry)data.get("drawingCellGeometry");
		int rowIndex = (Integer)data.get("rowIndex");
		
		PortType pType = null;
		if(type.equalsIgnoreCase(PortType.INPUT.toString()))
		{
			pType = PortType.INPUT;
		} else if(type.equalsIgnoreCase(PortType.OUTPUT.toString()))
		{
			pType = PortType.OUTPUT;
		} else if(type.equalsIgnoreCase(PortType.EQUIVALENCE.toString()))
		{
			pType = PortType.EQUIVALENCE;
		}
		
		VariableType vType = null;
		if(variableType.equalsIgnoreCase(VariableType.SPECIES.toString()))
		{
			vType = VariableType.SPECIES;
		} else if(variableType.equalsIgnoreCase(VariableType.GLOBAL_QUANTITY.toString()))
		{
			vType = VariableType.GLOBAL_QUANTITY;
		}
		
		Port newPort = new Port(parent, refName, pType, vType, name, rowIndex);
		//((mxCell)drawingCell).setValue(newPort);
		//newPort.setDrawingCell(drawingCell);
		AC_GUI.drawingBoard.createPort(newPort, oldCellGeo);
		
		return newPort;
	}
	
	private static Map<String, Object> packVisibleVariable(VisibleVariable var)
	{
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("refName", var.getRefName());
		data.put("variableType", var.getVariableType().toString());
		data.put("drawingCell", var.getDrawingCell());
		data.put("drawingCellBounds", var.getDrawingCellBounds());
		data.put("drawingCellGeometry", var.getDrawingCellGeometry());
		
		return data;
	}
	
	private static VisibleVariable unpackVisibleVariable(Map<String, Object> data, Module parent)
	{
		String refName = (String)data.get("refName");
		String variableType = (String)data.get("variableType");
		Object drawingCell = data.get("drawingCell");
		mxRectangle oldBounds = (mxRectangle)data.get("drawingCellBounds");
		mxGeometry oldGeo = (mxGeometry)data.get("drawingCellGeometry");
		
		mxRectangle newBounds = null;
		if(oldBounds != null)
		{
			newBounds = new mxRectangle(oldBounds.getX(), oldBounds.getY(), oldBounds.getWidth(), oldBounds.getHeight());
		}
		
		mxGeometry newGeo = null;
		if(oldGeo != null)
		{
			newGeo = new mxGeometry(oldGeo.getX(), oldGeo.getY(), oldGeo.getWidth(), oldGeo.getHeight());
		}
		
		VariableType vType = null;
		if(variableType.equalsIgnoreCase(VariableType.SPECIES.toString()))
		{
			vType = VariableType.SPECIES;
		} else if(variableType.equalsIgnoreCase(VariableType.GLOBAL_QUANTITY.toString()))
		{
			vType = VariableType.GLOBAL_QUANTITY;
		}
		
		VisibleVariable var = new VisibleVariable(parent, refName, drawingCell, newBounds, newGeo, vType);
		//((mxCell)drawingCell).setValue(var);
		AC_GUI.drawingBoard.createVisibleVariable(var);
		
		return var;
	}
	
	private static Map<String, Object> packEquivalenceNode(EquivalenceNode eNode)
	{
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("refName", eNode.getRefName());
		data.put("drawingCell", eNode.getDrawingCell());
		//data.put("drawingCellBounds", eNode.getDrawingCellBounds());
		data.put("drawingCellGeometry", eNode.getDrawingCellGeometry());
		
		return data;
	}
	
	private static EquivalenceNode unpackEquivalenceNode(Map<String, Object> data, Module parent)
	{
		String refName = (String)data.get("refName");
		Object drawingCell = data.get("drawingCell");
		mxGeometry oldGeo = (mxGeometry)data.get("drawingCellGeometry");
		
		mxGeometry newGeo = null;
		if(oldGeo != null)
		{
			newGeo = new mxGeometry(oldGeo.getX(), oldGeo.getY(), oldGeo.getWidth(), oldGeo.getHeight());
		}
		
		EquivalenceNode eNode = new EquivalenceNode(parent, refName);
		eNode.setDrawingCellGeometry(newGeo);
		AC_GUI.drawingBoard.createEquivalenceNode(eNode);
		
		return eNode;
	}
	
	private static Map<String, Object> packConnection(Connection edge)
	{
		Map<String, Object> data = new HashMap<String, Object>();
		//data.put("target", edge.getTarget());
		//data.put("source", edge.getSource());
		data.put("drawingCell", edge.getDrawingCell());
		data.put("drawingCellStyle", edge.getDrawingCellStyle());
		
		String sourceType = "";
		String targetType = "";
		String sourceID = "";
		String targetID = "";
		
		if (((mxCell)edge.getSource()).getValue() instanceof Port)
		{
			Port source = (Port)((mxCell)edge.getSource()).getValue();
			sourceID = source.getParent().getName() + "." + source.getRefName();
			sourceType = "port";
		} else if (((mxCell)edge.getSource()).getValue() instanceof VisibleVariable)
		{
			VisibleVariable source = (VisibleVariable)((mxCell)edge.getSource()).getValue();
			sourceID = source.getParent().getName() + "." + source.getRefName();
			sourceType = "visiblevariable";
		} else if (((mxCell)edge.getSource()).getValue() instanceof EquivalenceNode)
		{
			EquivalenceNode source = (EquivalenceNode)((mxCell)edge.getSource()).getValue();
			sourceID = source.getParent().getName() + "." + source.getRefName();
			sourceType = "equivalencenode";
		}
		
		if (((mxCell)edge.getTarget()).getValue() instanceof Port)
		{
			Port target = (Port)((mxCell)edge.getTarget()).getValue();
			targetID = target.getParent().getName() + "." + target.getRefName();
			targetType = "port";
		} else if (((mxCell)edge.getTarget()).getValue() instanceof VisibleVariable)
		{
			VisibleVariable target = (VisibleVariable)((mxCell)edge.getTarget()).getValue();
			targetID = target.getParent().getName() + "." + target.getRefName();
			targetType = "visiblevariable";
		} else if (((mxCell)edge.getTarget()).getValue() instanceof EquivalenceNode)
		{
			EquivalenceNode target = (EquivalenceNode)((mxCell)edge.getTarget()).getValue();
			targetID = target.getParent().getName() + "." + target.getRefName();
			targetType = "equivalencenode";
		}

		data.put("source", sourceID);
		data.put("target", targetID);
		data.put("sourceType", sourceType);
		data.put("targetType", targetType);
		
		return data;
	}
	
	private static Connection unpackConnection(Map<String, Object> data, Module parent)
	{
		String sourceType = (String)data.get("sourceType");
		String targetType = (String)data.get("targetType");
		String sourceID = (String)data.get("source");
		String targetID = (String)data.get("target");
		Object drawingCell = data.get("drawingCell");
		String drawingCellSyle = (String)data.get("drawingCellStyle");
		
		Connection edge = null;
		Object source = findTerminal(sourceType, sourceID, parent);
		Object target = findTerminal(targetType, targetID, parent);
		Object sourceCell = null;
		Object targetCell = null;
		
		if((source == null) || (target == null))
		{
			System.err.println("Unable to unpack connection.");
			System.exit(0);
		}
		
		if(source instanceof Port)
		{
			sourceCell = ((Port)source).getDrawingCell();
		}else if (source instanceof VisibleVariable)
		{
			sourceCell = ((VisibleVariable)source).getDrawingCell();
		}else if (source instanceof EquivalenceNode)
		{
			sourceCell = ((EquivalenceNode)source).getDrawingCell();
		}
		else
		{
			System.err.println("Unpack connection error. Source.");
			System.exit(0);
		}
		
		if(target instanceof Port)
		{
			targetCell = ((Port)target).getDrawingCell();
		}else if (target instanceof VisibleVariable)
		{
			targetCell = ((VisibleVariable)target).getDrawingCell();
		}else if (target instanceof EquivalenceNode)
		{
			targetCell = ((EquivalenceNode)target).getDrawingCell();
		}
		else
		{
			System.err.println("Unpack connection error. Target.");
			System.exit(0);
		}
		
		edge = new Connection(parent);
		edge.setDrawingCellStyle(drawingCellSyle);
		AC_GUI.drawingBoard.createConnection(edge, sourceCell, targetCell);
		
		//((mxCell)drawingCell).setValue(edge);
		
		return edge;
	}
	
	private static Map<String, Object> packMathAggregator(Map<String, Object> data, MathematicalAggregator mathAgg)
	{
		data.put("inputNumber", mathAgg.getNumberofInputs());
		data.put("operation", mathAgg.getOperation());
		
		// pack the ports
		ArrayList<Map<String, Object>> packedList;
		packedList = null;
		int portCount = mathAgg.getPorts().size();
		if(portCount != 0)
		{
			packedList = new ArrayList<Map<String, Object>>(portCount);
			ListIterator<Port> ports = mathAgg.getPorts().listIterator();
			while(ports.hasNext())
			{
				packedList.add(packPort(ports.next()));
			}
		}
		
		data.put("ports", packedList);
		
		return data;
	}
	
	private static MathematicalAggregator unpackMathAggregator(Map<String, Object> data, Module parent)
	{
		String name = (String)data.get("name");
		String msmbData = (String)data.get("msmbData");
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)data.get("treeNode");
		Object drawingCell = data.get("drawingCell");
		String datamodelKey = (String)data.get("copasiDatamodelKey");
		String cellStyle = (String)data.get("drawingCellStyle");
		mxRectangle oldBounds = (mxRectangle)data.get("drawingCellBounds");
		mxGeometry oldGeo = (mxGeometry)data.get("drawingCellGeometry");
		int inputNumber = (Integer)data.get("inputNumber");
		Operation op = (Operation)data.get("operation");
		
		mxRectangle newBounds = null;
		if(oldBounds != null)
		{
			newBounds = new mxRectangle(oldBounds.getX(), oldBounds.getY(), oldBounds.getWidth(), oldBounds.getHeight());
		}
		
		mxGeometry newGeo = null;
		if(oldGeo != null)
		{
			newGeo = new mxGeometry(oldGeo.getX(), oldGeo.getY(), oldGeo.getWidth(), oldGeo.getHeight());
		}
		
		CCopasiDataModel dataModel = AC_GUI.copasiUtility.createDataModel();
		dataModel.getModel().setObjectName(name);
		MathematicalAggregator mathAgg = null;
		try
		{
			mathAgg = new MathematicalAggregator(name, dataModel.getModel().getKey(), msmbData, inputNumber, op, parent);
		}
		catch (Exception e)
		{
			if (msmbData.isEmpty())
			{
				System.err.println("AC_IO.unpackMathAggregator(): The MathematicalAggregator \"" + name + "\" has msmbData which is currently empty.");
			}
			e.printStackTrace();
			System.exit(0);
		}
		//MathematicalAggregator mathAgg = new MathematicalAggregator(name, dataModel.getModel().getKey(), inputNumber, op, parent);
		mathAgg.setDrawingCellStyle(cellStyle);
		mathAgg.setDrawingCellBounds(newBounds);
		mathAgg.setDrawingCellGeometry(newGeo);
		AC_GUI.treeView.addNode(mathAgg);
		AC_GUI.drawingBoard.createCell(mathAgg);
		
		ArrayList<Map<String, Object>> packedList;
		ListIterator<Map<String, Object>> packedListIterator;
		if(data.get("ports") != null)
		{
			System.out.println("Found " + mathAgg.getName() + "'s ports.");
			packedList = (ArrayList<Map<String, Object>>)data.get("ports");
			packedListIterator = packedList.listIterator();
			while(packedListIterator.hasNext())
			{
				mathAgg.addPort(unpackPort(packedListIterator.next(), mathAgg));
			}
		}
		
		return mathAgg;
	}
	
	private static Object findTerminal(String type, String id, Module parent)
	{
		String parentName = id.substring(0, id.indexOf('.'));
		String terminalName = id.substring(id.indexOf('.')+1);
		Object terminal = null;
		
		if (parentName.equals(parent.getName()))
		{
			if (type.equals("visiblevariable"))
			{
				ListIterator<VisibleVariable> vars = parent.getVisibleVariables().listIterator();
				VisibleVariable var = null;
				while(vars.hasNext())
				{
					var = vars.next();
					if (terminalName.equals(var.getRefName()))
					{
						return var;
					}
				}
			}
			else if (type.equals("equivalencenode"))
			{
				ListIterator<EquivalenceNode> eNodes = parent.getEquivalenceNodes().listIterator();
				EquivalenceNode eNode = null;
				while(eNodes.hasNext())
				{
					eNode = eNodes.next();
					if (terminalName.equals(eNode.getRefName()))
					{
						return eNode;
					}
				}
			}
			else if (type.equals("port"))
			{
				terminal = scanPorts(terminalName, parent);
			}
			if (terminal != null)
			{
				return terminal;
			}
		} else
		{
			// check children ports
			ListIterator<Module> children = parent.getChildren().listIterator();
			Module mod = null;
			while(children.hasNext())
			{
				mod = children.next();
				if(parentName.equals(mod.getName()))
				{
					terminal = scanPorts(terminalName, mod);
					if(terminal != null)
					{
						return terminal;
					}
				}
			}
		}
		
		return null;
	}
	
	private static Port scanPorts(String name, Module mod)
	{
		ListIterator<Port> ports = mod.getPorts().listIterator();
		Port currentPort = null;
		
		while (ports.hasNext())
		{
			currentPort = ports.next();
			if (name.equals(currentPort.getRefName()))
			{
				return currentPort;
			}
		}
		return null;		
	}
}
