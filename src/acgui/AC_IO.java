package acgui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.JOptionPane;

import org.sbml.libsbml.SBMLDocument;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

/**
 * @author Thomas
 *
 */
public class AC_IO
{

	private static ArrayList<ModuleDefinition> definitionList = new ArrayList<ModuleDefinition>();
	/**
	 * 
	 */
	public AC_IO()
	{
		
	}

	public static void saveModule(Module mod, String fileName)
	{
		AC_Utility.createUniqueIDs(mod);
		Map<String, Object> data = packModule(mod, true);
		
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
		
		definitionList.clear();
		
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
			mod = unpackModule(data, parent, true);
			if (mod != null)
			{
				// check to make sure all module names are valid
				/*
				if (parent != null)
				{
					parent.addChild(mod);
				}
				*/
				System.out.println(mod.getModuleDefinition().getName() + " successfully loaded.");
				
				AC_Utility.addTreeNode(mod);
			}
		}
		
		definitionList.clear();
		return mod;
	}
	
	private static Map<String, Object> packModuleDefinition(ModuleDefinition definition)
	{
		Map<String, Object> data = new HashMap<String, Object>();
		ArrayList<Map<String, Object>> packedList;
		int count;
		data.put("name", definition.getName());
		data.put("msmbData", definition.getMSMBData());
		data.put("id", definition.getID());
		data.put("external", definition.isExternal() ? 1 : 0);
		data.put("externalSource", definition.getExternalSource());
		data.put("externalModelRef", definition.getExternalModelRef());
		data.put("md5", definition.getmd5());
		
		if (definition.getParent() == null)
		{
			data.put("parent", null);
		}
		else
		{
			data.put("parent", definition.getParent().getName());
		}
		
		if (definition instanceof MathematicalAggregatorDefinition)
		{
			data.put("MathematicalAggregator", "yes");
			return packMathematicalAggregatorDefinition(data, (MathematicalAggregatorDefinition)definition);
		}
		data.put("MathematicalAggregator", "no");
		
		// pack the PortDefinitions
		packedList = null;
		count = definition.getPorts().size();
		if (count != 0)
		{
			packedList = new ArrayList<Map<String, Object>>(count);
			ListIterator<ACComponentDefinition> ports = definition.getPorts().listIterator();
			while(ports.hasNext())
			{
				packedList.add(packPortDefinition((PortDefinition)ports.next()));
			}
		}
		data.put("portDefinitions", packedList);
		
		// pack the submodules
		packedList = null;
		count = definition.getChildren().size();
		if(count != 0)
		{
			packedList = new ArrayList<Map<String, Object>>(count);
			ListIterator<ModuleDefinition> children = definition.getChildren().listIterator();
			while(children.hasNext())
			{
				packedList.add(packModuleDefinition(children.next()));
			}
		}
		data.put("childrenDefinitions", packedList);
				
		// pack the visible variables
		packedList = null;
		count = definition.getVisibleVariables().size();
		if(count != 0)
		{
			packedList = new ArrayList<Map<String, Object>>(count);
			ListIterator<ACComponentDefinition> vars = definition.getVisibleVariables().listIterator();
			while(vars.hasNext())
			{
				packedList.add(packVisibleVariableDefinition((VisibleVariableDefinition)vars.next()));
			}
		}
		data.put("visibleVariableDefinitions", packedList);
		
		// pack the equivalence nodes
		packedList = null;
		count = definition.getEquivalences().size();
		if(count != 0)
		{
			packedList = new ArrayList<Map<String, Object>>(count);
			ListIterator<ACComponentDefinition> eNodes = definition.getEquivalences().listIterator();
			while(eNodes.hasNext())
			{
				packedList.add(packEquivalenceDefinition((EquivalenceDefinition)eNodes.next()));
			}
		}
		data.put("equivalenceDefinitions", packedList);
		
		// pack the connections
		packedList = null;
		count = definition.getConnections().size();
		if(count != 0)
		{
			packedList = new ArrayList<Map<String, Object>>(count);
			ListIterator<ConnectionDefinition> connections = definition.getConnections().listIterator();
			while(connections.hasNext())
			{
				packedList.add(packConnectionDefinition(connections.next()));
			}
		}
		data.put("connectionDefinitions", packedList);
				
		return data;
	}
	
	private static ModuleDefinition unpackModuleDefinition(Map<String, Object> data, ModuleDefinition parent)
	{
		ArrayList<Map<String, Object>> packedList;
		ListIterator<Map<String, Object>> packedListIterator;
		String isAggregator = (String)data.get("MathematicalAggregator");
		if (isAggregator.equals("yes"))
		{
			return unpackMathematicalAggregatorDefinition(data, parent);
		}
		String name = (String)data.get("name");
		byte[] msmbData = (byte[])data.get("msmbData");
		String id = (String)data.get("id");
		int isExternal = (Integer)data.get("external");
		boolean external = (1 == isExternal);
		String externalSource = (String)data.get("externalSource");
		String externalModelRef = (String)data.get("externalModelRef");
		String md5 = (String)data.get("md5");

		/*
		int isExternal;
		boolean external = false;
		String externalSource = "";
		String externalModelRef = "";
		String md5 = "";
		*/
		
		if (external)
		{
			boolean validExternalFile = AC_Utility.validateExternalFile(externalSource, md5);
			if (!validExternalFile)
			{
				//return SBMLParser.importExternalDefinition(externalSource, externalModelRef);
				return null;
			}
		}
		
		//ModuleDefinition definition = new ModuleDefinition(name, parent, msmbData);
		name = validateModuleDefinitionName(name);
		if (name == null)
		{
			return null;
		}
		ModuleDefinition definition = AC_Utility.createModuleDefinition(name, parent, msmbData, true);
		definition.setID(id);
		definition.setExternal(external);
		definition.setExternalSource(externalSource);
		definition.setExternalModelRef(externalModelRef);
		definition.setmd5(md5);
		
		// unpack PortDefinitions
		if(data.get("portDefinitions") != null)
		{
			System.out.println("Found " + definition.getName() + "'s ports.");
			packedList = (ArrayList<Map<String, Object>>)data.get("portDefinitions");
			packedListIterator = packedList.listIterator();
			while(packedListIterator.hasNext())
			{
				definition.addPort(unpackPortDefinition(packedListIterator.next(), definition));
			}
		}
		
		// unpack submodules
		if(data.get("childrenDefinitions") != null)
		{
			System.out.println("Found " + definition.getName() + "'s children.");
			packedList = (ArrayList<Map<String, Object>>)data.get("childrenDefinitions");
			packedListIterator = packedList.listIterator();
			while(packedListIterator.hasNext())
			{
				unpackModuleDefinition(packedListIterator.next(), definition);
			}
		}
		
		// unpack VisibleVariableDefinitions
		if(data.get("visibleVariableDefinitions") != null)
		{
			System.out.println("Found " + definition.getName() + "'s visible variable definitions.");
			packedList = (ArrayList<Map<String, Object>>)data.get("visibleVariableDefinitions");
			packedListIterator = packedList.listIterator();
			while(packedListIterator.hasNext())
			{
				definition.addVisibleVariable(unpackVisibleVariableDefinition(packedListIterator.next(), definition));
			}
		}
		
		// unpack EquivalenceDefinitions
		if(data.get("equivalenceDefinitions") != null)
		{
			System.out.println("Found " + definition.getName() + "'s equivalence definitions.");
			packedList = (ArrayList<Map<String, Object>>)data.get("equivalenceDefinitions");
			packedListIterator = packedList.listIterator();
			while(packedListIterator.hasNext())
			{
				definition.addEquivalence(unpackEquivalenceDefinition(packedListIterator.next(), definition));
			}
		}
		
		// unpack ConnectionDefinitions
		if(data.get("connectionDefinitions") != null)
		{
			System.out.println("Found " + definition.getName() + "'s connection definitions.");
			packedList = (ArrayList<Map<String, Object>>)data.get("connectionDefinitions");
			packedListIterator = packedList.listIterator();
			while(packedListIterator.hasNext())
			{
				definition.addConnection(unpackConnectionDefinition(packedListIterator.next(), definition));
			}
		}
		
		definitionList.add(definition);
		return definition;
	}
	
	private static Map<String, Object> packMathematicalAggregatorDefinition(Map<String, Object> data, MathematicalAggregatorDefinition definition)
	{
		ArrayList<Map<String, Object>> packedList;
		int count;
		char operation;
		
		switch (definition.getOperation())
		{
			case PRODUCT:
				operation = 'P';
				break;
			case SUM:
				operation = 'S';
				break;
			default:
				operation = '0';
		}
		
		data.put("inputNumber", definition.getNumberofInputs());
		data.put("operation", operation);
		
		// pack the PortDefinitions
		packedList = null;
		count = definition.getPorts().size();
		if (count != 0)
		{
			packedList = new ArrayList<Map<String, Object>>(count);
			ListIterator<ACComponentDefinition> ports = definition.getPorts().listIterator();
			while(ports.hasNext())
			{
				packedList.add(packPortDefinition((PortDefinition)ports.next()));
			}
		}
		data.put("portDefinitions", packedList);
		
		return data;
	}
	
	private static MathematicalAggregatorDefinition unpackMathematicalAggregatorDefinition(Map<String, Object> data, ModuleDefinition parent)
	{
		ArrayList<Map<String, Object>> packedList;
		ListIterator<Map<String, Object>> packedListIterator;
		String name = (String)data.get("name");
		byte[] msmbData = (byte[])data.get("msmbData");
		String id = (String)data.get("id");
		int inputNumber = (Integer)data.get("inputNumber");
		char operation = (Character)data.get("operation");
		
		Operation op = null;		
		switch (operation)
		{
			case 'P':
				op = Operation.PRODUCT;	
				break;
			case 'S':
				op = Operation.SUM;
				break;
			default:
				// there is an error
				System.err.println("unpackMathematicalAggregatorDefinition: " + operation + " is not a valid Operation.");
				displayErrorMessage();
				return null;
		}
		
		//MathematicalAggregatorDefinition definition = new MathematicalAggregatorDefinition(name, parent, msmbData, inputNumber, op);
		MathematicalAggregatorDefinition definition = AC_Utility.createMathematicalAggregatorDefinition(name, parent, msmbData, inputNumber, op, true);
		definition.setID(id);
		
		// unpack PortDefinitions
		if(data.get("portDefinitions") != null)
		{
			System.out.println("Found " + definition.getName() + "'s ports.");
			packedList = (ArrayList<Map<String, Object>>)data.get("portDefinitions");
			packedListIterator = packedList.listIterator();
			while(packedListIterator.hasNext())
			{
				definition.addPort(unpackPortDefinition(packedListIterator.next(), definition));
			}
		}
		
		return definition;
	}
	
	private static Map<String, Object> packPortDefinition(PortDefinition definition)
	{
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("portType", definition.getType().name());
		packACComponentDefinition(data, definition);
		return data;
	}
	
	private static PortDefinition unpackPortDefinition(Map<String, Object> data, ModuleDefinition parent)
	{
		PortDefinition definition = new PortDefinition(parent);
		unpackACComponentDefinition(data, definition);
		PortType portType = null;
		
		try
		{
			String type = (String)data.get("portType");
			portType = PortType.valueOf(type);
		}catch(Exception ex)
		{
			   ex.printStackTrace();
			   System.err.println("unpackPortDefinition failed");
		}
		
		definition.setType(portType);
		
		return definition;
	}
	
	private static Map<String, Object> packVisibleVariableDefinition(VisibleVariableDefinition definition)
	{
		Map<String, Object> data = new HashMap<String, Object>();
		packACComponentDefinition(data, definition);
		return data;
	}
	
	private static VisibleVariableDefinition unpackVisibleVariableDefinition(Map<String, Object> data, ModuleDefinition parent)
	{
		VisibleVariableDefinition definition = new VisibleVariableDefinition(parent);
		unpackACComponentDefinition(data, definition);	
		return definition;
	}
	
	private static Map<String, Object> packEquivalenceDefinition(EquivalenceDefinition definition)
	{
		Map<String, Object> data = new HashMap<String, Object>();
		packACComponentDefinition(data, definition);
		return data;
	}
	
	private static EquivalenceDefinition unpackEquivalenceDefinition(Map<String, Object> data, ModuleDefinition parent)
	{
		EquivalenceDefinition definition = new EquivalenceDefinition(parent);
		unpackACComponentDefinition(data, definition);
		return definition;
	}
	
	private static Map<String, Object> packConnectionDefinition(ConnectionDefinition definition)
	{
		Map<String, Object> data = new HashMap<String, Object>();		
		data.put("sourceParent", definition.getSourceDefinition().getParent().getID());
		data.put("targetParent", definition.getTargetDefinition().getParent().getID());
		data.put("sourceRefName", definition.getSourceDefinition().getRefName());
		data.put("targetRefName", definition.getTargetDefinition().getRefName());
		data.put("sourceVariableType", definition.getSourceDefinition().getVariableType().name());
		data.put("targetVariableType", definition.getTargetDefinition().getVariableType().name());
		data.put("sourceTerminalType", definition.getSourceType().name());
		data.put("targetTerminalType", definition.getTargetType().name());
		return data;
	}
	
	private static ConnectionDefinition unpackConnectionDefinition(Map<String, Object> data, ModuleDefinition parent)
	{
		String sourceParent = (String)data.get("sourceParent");
		String targetParent = (String)data.get("targetParent");
		String sourceRefName = (String)data.get("sourceRefName");
		String targetRefName = (String)data.get("targetRefName");
		VariableType sourceVariableType = null;
		VariableType targetVariableType = null;
		TerminalType sourceTerminalType = null;
		TerminalType targetTerminalType = null;

		try
		{
			String type = (String)data.get("sourceVariableType");
			sourceVariableType = VariableType.valueOf(type);
		}catch(Exception ex)
		{
			   ex.printStackTrace();
			   System.err.println("unpackConnectionDefinition failed, sourceVariableType");
		}
		
		try
		{
			String type = (String)data.get("targetVariableType");
			targetVariableType = VariableType.valueOf(type);
		}catch(Exception ex)
		{
			   ex.printStackTrace();
			   System.err.println("unpackConnectionDefinition failed, targetVariableType");
		}
		
		try
		{
			String type = (String)data.get("sourceTerminalType");
			sourceTerminalType = TerminalType.valueOf(type);
		}catch(Exception ex)
		{
			   ex.printStackTrace();
			   System.err.println("unpackConnectionDefinition failed, sourceTerminalType");
		}
		
		try
		{
			String type = (String)data.get("targetTerminalType");
			targetTerminalType = TerminalType.valueOf(type);
		}catch(Exception ex)
		{
			   ex.printStackTrace();
			   System.err.println("unpackConnectionDefinition failed, targetTerminalType");
		}
		
		ACComponentDefinition source = getTerminalDefinition(parent, sourceParent, sourceVariableType, sourceTerminalType, sourceRefName);
		ACComponentDefinition target = getTerminalDefinition(parent, targetParent, targetVariableType, targetTerminalType, targetRefName);
		
		if (source == null)
		{
			// there is an error
			System.err.println("unpackConnectionDefinition: " + sourceRefName + " source terminal not found.");
			displayErrorMessage();
			return null;
		}
		
		if (target == null)
		{
			// there is an error
			System.err.println("unpackConnectionDefinition: " + targetRefName + " target terminal not found.");
			displayErrorMessage();
			return null;
		}
		
		return new ConnectionDefinition(parent, source, sourceTerminalType, target, targetTerminalType);
	}
		
	private static void packACComponentDefinition(Map<String, Object> data, ACComponentDefinition definition)
	{
		data.put("variableType", definition.getVariableType().name());
		data.put("refName", definition.getRefName());
		data.put("name", definition.getName());
	}
	
	private static void unpackACComponentDefinition(Map<String, Object> data, ACComponentDefinition definition)
	{
		VariableType variableType = null;
		try
		{
			String type = (String)data.get("variableType");
			variableType = VariableType.valueOf(type);
		}catch(Exception ex)
		{
			   ex.printStackTrace();
			   System.err.println("unpackACComponentDefinition failed");
		}
		
		String refName = (String)data.get("refName");
		String name = (String)data.get("name");
		definition.setVariableType(variableType);
		definition.setRefName(refName);
		definition.setName(name);
	}
	
	private static Map<String, Object> packModule(Module mod, boolean packDefinition)
	{
		Map<String, Object> data = new HashMap<String, Object>();
		ArrayList<Map<String, Object>> packedList;
		int count;
		if (packDefinition)
		{
			data.put("definition", packModuleDefinition(mod.getModuleDefinition()));
		}
		data.put("definitionName", mod.getModuleDefinition().getName());
		data.put("definitionID", mod.getModuleDefinition().getID());
		data.put("drawingCellGeometry_Module", packCellGeometry(mod.getDrawingCellGeometryModule()));
		data.put("drawingCellGeometry_Submodule", packCellGeometry(mod.getDrawingCellGeometrySubmodule()));
		data.put("drawingCellStyle", mod.getDrawingCellStyle());
		if (mod.getParent() == null)
		{
			// mod is the root module
			data.put("name", mod.getModuleDefinition().getName());
		}
		else
		{
			// mod is a submodule
			data.put("name", mod.getName());
		}
		data.put("id", mod.getID());
		
		// pack the PortNodes
		packedList = null;
		count = mod.getPorts().size();
		if (count != 0)
		{
			packedList = new ArrayList<Map<String, Object>>(count);
			ListIterator<ACComponentNode> ports = mod.getPorts().listIterator();
			while(ports.hasNext())
			{
				packedList.add(packPortNode((PortNode)ports.next()));
			}
		}
		data.put("portNodes", packedList);
		
		// pack the submodules
		packedList = null;
		count = mod.getChildren().size();
		if(count != 0)
		{
			packedList = new ArrayList<Map<String, Object>>(count);
			ListIterator<Module> children = mod.getChildren().listIterator();
			while(children.hasNext())
			{
				packedList.add(packModule(children.next(), false));
			}
		}
		data.put("children", packedList);
				
		// pack the visible variables
		packedList = null;
		count = mod.getVisibleVariables().size();
		if(count != 0)
		{
			packedList = new ArrayList<Map<String, Object>>(count);
			ListIterator<ACComponentNode> vars = mod.getVisibleVariables().listIterator();
			while(vars.hasNext())
			{
				packedList.add(packVisibleVariableNode((VisibleVariableNode)vars.next()));
			}
		}
		data.put("visibleVariableNodes", packedList);
		
		// pack the equivalence nodes
		packedList = null;
		count = mod.getEquivalences().size();
		if(count != 0)
		{
			packedList = new ArrayList<Map<String, Object>>(count);
			ListIterator<ACComponentNode> eNodes = mod.getEquivalences().listIterator();
			while(eNodes.hasNext())
			{
				packedList.add(packEquivalenceNode((EquivalenceNode)eNodes.next()));
			}
		}
		data.put("equivalenceNodes", packedList);
		
		// pack the connections
		packedList = null;
		count = mod.getConnections().size();
		if(count != 0)
		{
			packedList = new ArrayList<Map<String, Object>>(count);
			ListIterator<ConnectionNode> connections = mod.getConnections().listIterator();
			while(connections.hasNext())
			{
				packedList.add(packConnectionNode(connections.next()));
			}
		}
		data.put("connectionNodes", packedList);
		
		return data;
	}
	
	private static Module unpackModule(Map<String, Object> data, Module parent, boolean unpackDefinition)
	{
		ArrayList<Map<String, Object>> packedList;
		ListIterator<Map<String, Object>> packedListIterator;
		/*
		data.put("name", mod.getModuleDefinition().getName());
		data.put("drawingCellGeometry_Module", packCellGeometry(mod.getDrawingCellGeometryModule()));
		data.put("drawingCellGeometry_Submodule", packCellGeometry(mod.getDrawingCellGeometrySubmodule()));
		data.put("drawingCellStyle", mod.getDrawingCellStyle());
		*/
		String name = (String)data.get("name");
		String definitionName = (String)data.get("definitionName");
		String definitionID = (String)data.get("definitionID");
		mxGeometry moduleGeometry = unpackCellGeometry((Map<String, Object>)data.get("drawingCellGeometry_Module"));
		mxGeometry submoduleGeometry = unpackCellGeometry((Map<String, Object>)data.get("drawingCellGeometry_Submodule"));
		String cellStyle = (String)data.get("drawingCellStyle");
		String id = (String)data.get("id");
		
		ModuleDefinition definition;
		if (unpackDefinition)
		{
			if (parent == null)
			{
				definition = unpackModuleDefinition((Map<String, Object>)data.get("definition"), null);
			}
			else
			{
				definition = unpackModuleDefinition((Map<String, Object>)data.get("definition"), parent.getModuleDefinition());
				//parent.getModuleDefinition().addChild(definition);
			}
		}
		else
		{
			//definition = getModuleDefinitionfromName(parent.getModuleDefinition().getChildren().listIterator(), definitionName);
			definition = getModuleDefinitionfromID(parent.getModuleDefinition().getChildren().listIterator(), definitionID);
		}
		if (definition == null)
		{
			// there is an error
			System.err.println("unpackModule: Module (" + name + ") ModuleDefinition (" + definitionName + ") not found.");
			displayErrorMessage();
			return null;
		}
		if (parent != null)
		{
			if (definition instanceof MathematicalAggregatorDefinition)
			{
				cellStyle = ((MathematicalAggregatorDefinition)definition).getOperation().toString();
			}
			else
			{
				cellStyle = "Submodule_No_Show_Information";
			}
		}
		
		/*
		Module module = new Module(name, definition, parent, moduleGeometry, submoduleGeometry, cellStyle);
		module.setID(id);
		//AC_GUI.treeView.createNode(module);
		//AC_GUI.drawingBoard.createCell(module);
		//definition.addInstance(module);
		*/
		name = validateModuleName(name, parent);
		if (name == null)
		{
			return null;
		}
		Module module = AC_Utility.createInstance(name, definition, parent, moduleGeometry, submoduleGeometry, cellStyle);
		module.setID(id);
		
		// unpack PortNodes
		if (data.get("portNodes") != null)
		{
			System.out.println("Found PortNodes.");
			packedList = (ArrayList<Map<String, Object>>)data.get("portNodes");
			packedListIterator = packedList.listIterator();
			while(packedListIterator.hasNext())
			{
				module.addPort(unpackPortNode(packedListIterator.next(), module));
			}
		}
		
		// unpack submodules
		if (data.get("children") != null)
		{
			System.out.println("Found submodules.");
			Module child;
			packedList = (ArrayList<Map<String, Object>>)data.get("children");
			packedListIterator = packedList.listIterator();
			while(packedListIterator.hasNext())
			{
				child = unpackModule(packedListIterator.next(), module, false);
				if (child == null)
				{
					return null;
				}
			}
		}
		
		// unpack VisibleVariableNodes
		if (data.get("visibleVariableNodes") != null)
		{
			System.out.println("Found VisibleVariableNodes.");
			packedList = (ArrayList<Map<String, Object>>)data.get("visibleVariableNodes");
			packedListIterator = packedList.listIterator();
			while(packedListIterator.hasNext())
			{
				module.addVisibleVariable(unpackVisibleVariableNode(packedListIterator.next(), module));
			}
		}
		
		// unpack EquivalenceNodes
		if (data.get("equivalenceNodes") != null)
		{
			System.out.println("Found EquivalenceNodes.");
			packedList = (ArrayList<Map<String, Object>>)data.get("equivalenceNodes");
			packedListIterator = packedList.listIterator();
			while(packedListIterator.hasNext())
			{
				module.addEquivalence(unpackEquivalenceNode(packedListIterator.next(), module));
			}
		}
		
		// unpack ConnectionNodes
		if (data.get("connectionNodes") != null)
		{
			System.out.println("Found ConnectionNodes.");
			packedList = (ArrayList<Map<String, Object>>)data.get("connectionNodes");
			packedListIterator = packedList.listIterator();
			while(packedListIterator.hasNext())
			{
				module.addConnection(unpackConnectionNode(packedListIterator.next(), module));
			}
		}
		
		return module;
	}

	
	private static Map<String, Object> packPortNode(PortNode port)
	{
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("refName", port.getDefinition().getRefName());
		data.put("variableType", port.getDefinition().getVariableType().name());
		packACComponentNode(data, port);
		return data;
	}
	
	private static PortNode unpackPortNode(Map<String, Object> data, Module parent)
	{
		String refName = (String)data.get("refName");
		
		VariableType variableType = null;
		try
		{
			String type = (String)data.get("variableType");
			variableType = VariableType.valueOf(type);
		}catch(Exception ex)
		{
			   ex.printStackTrace();
			   System.err.println("unpackPortNode failed");
		}
		
		ACComponentDefinition definition = getACComponentDefinition(parent.getModuleDefinition().getPorts().listIterator(), refName, variableType);
		if (definition == null)
		{
			// there is an error
			System.err.println("unpackPortNode: " + refName + " PortDefinition not found.");
			displayErrorMessage();
			return null;
		}
		PortNode node = new PortNode(parent, (PortDefinition)definition);
		unpackACComponentNode(data, node);
		return node;
	}
	
	private static Map<String, Object> packVisibleVariableNode(VisibleVariableNode visibleVariable)
	{
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("refName", visibleVariable.getDefinition().getRefName());
		data.put("variableType", visibleVariable.getDefinition().getVariableType().name());
		packACComponentNode(data, visibleVariable);
		return data;
	}
	
	private static VisibleVariableNode unpackVisibleVariableNode(Map<String, Object> data, Module parent)
	{
		String refName = (String)data.get("refName");
		
		VariableType variableType = null;
		try
		{
			String type = (String)data.get("variableType");
			variableType = VariableType.valueOf(type);
		}catch(Exception ex)
		{
			   ex.printStackTrace();
			   System.err.println("unpackVisibleVariableNode failed");
		}
		
		ACComponentDefinition definition = getACComponentDefinition(parent.getModuleDefinition().getVisibleVariables().listIterator(), refName, variableType);
		if (definition == null)
		{
			// there is an error
			System.err.println("unpackVisibleVariableNode: " + refName + " VisibleVariableDefinition not found.");
			displayErrorMessage();
			return null;
		}
		VisibleVariableNode node = new VisibleVariableNode(parent, (VisibleVariableDefinition)definition);
		unpackACComponentNode(data, node);
		return node;
	}
	
	private static Map<String, Object> packEquivalenceNode(EquivalenceNode eNode)
	{
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("refName", eNode.getDefinition().getRefName());
		data.put("variableType", eNode.getDefinition().getVariableType().name());
		packACComponentNode(data, eNode);
		return data;
	}
	
	private static EquivalenceNode unpackEquivalenceNode(Map<String, Object> data, Module parent)
	{
		String refName = (String)data.get("refName");
		
		VariableType variableType = null;
		try
		{
			String type = (String)data.get("variableType");
			variableType = VariableType.valueOf(type);
		}catch(Exception ex)
		{
			   ex.printStackTrace();
			   System.err.println("unpackEquivalenceNode failed");
		}
		
		ACComponentDefinition definition = getACComponentDefinition(parent.getModuleDefinition().getEquivalences().listIterator(), refName, variableType);
		if (definition == null)
		{
			// there is an error
			System.err.println("unpackEquivalenceNode: " + refName + " EquivalenceDefinition not found.");
			displayErrorMessage();
			return null;
		}
		EquivalenceNode node = new EquivalenceNode(parent, (EquivalenceDefinition)definition);
		unpackACComponentNode(data, node);
		return node;
	}
	
	private static Map<String, Object> packConnectionNode(ConnectionNode connection)
	{
		Map<String, Object> data = new HashMap<String, Object>();
		// how to connect a ConnectionNode to a ConnectionDefinition?
		String sourceParentID = ((ACComponentNode)connection.getDrawingCell().getSource().getValue()).getParent().getID();
		String targetParentID = ((ACComponentNode)connection.getDrawingCell().getTarget().getValue()).getParent().getID();
		data.put("sourceParent", connection.getConnectionDefinition().getSourceDefinition().getParent().getName());
		data.put("targetParent", connection.getConnectionDefinition().getTargetDefinition().getParent().getName());
		data.put("sourceParentID", sourceParentID);
		data.put("targetParentID", targetParentID);
		data.put("sourceRefName", connection.getConnectionDefinition().getSourceDefinition().getRefName());
		data.put("targetRefName", connection.getConnectionDefinition().getTargetDefinition().getRefName());
		data.put("sourceVariableType", connection.getConnectionDefinition().getSourceDefinition().getVariableType().name());
		data.put("targetVariableType", connection.getConnectionDefinition().getTargetDefinition().getVariableType().name());
		data.put("sourceTerminalType", connection.getConnectionDefinition().getSourceType().name());
		data.put("targetTerminalType", connection.getConnectionDefinition().getTargetType().name());
		packACComponentNode(data, connection);
		return data;
	}
	
	private static ConnectionNode unpackConnectionNode(Map<String, Object> data, Module parent)
	{
		String sourceParentID = (String)data.get("sourceParentID");
		String targetParentID = (String)data.get("targetParentID");
		String sourceRefName = (String)data.get("sourceRefName");
		String targetRefName = (String)data.get("targetRefName");
		String drawingCellStyle = (String)data.get("drawingCellStyle");
		VariableType sourceVariableType = null;
		VariableType targetVariableType = null;
		TerminalType sourceTerminalType = null;
		TerminalType targetTerminalType = null;
		
		try
		{
			String type = (String)data.get("sourceVariableType");
			sourceVariableType = VariableType.valueOf(type);
		}catch(Exception ex)
		{
			   ex.printStackTrace();
			   System.err.println("unpackConnectionDefinition failed, sourceVariableType");
		}
		
		try
		{
			String type = (String)data.get("targetVariableType");
			targetVariableType = VariableType.valueOf(type);
		}catch(Exception ex)
		{
			   ex.printStackTrace();
			   System.err.println("unpackConnectionDefinition failed, targetVariableType");
		}
		
		try
		{
			String type = (String)data.get("sourceTerminalType");
			sourceTerminalType = TerminalType.valueOf(type);
		}catch(Exception ex)
		{
			   ex.printStackTrace();
			   System.err.println("unpackConnectionDefinition failed, sourceTerminalType");
		}
		
		try
		{
			String type = (String)data.get("targetTerminalType");
			targetTerminalType = TerminalType.valueOf(type);
		}catch(Exception ex)
		{
			   ex.printStackTrace();
			   System.err.println("unpackConnectionDefinition failed, targetTerminalType");
		}
		
		ConnectionDefinition definition = getConnectionDefinition(parent.getModuleDefinition().getConnections().listIterator(), sourceVariableType, sourceTerminalType, sourceRefName, targetVariableType, targetTerminalType, targetRefName);
		ACComponentNode source = getTerminalNode(parent, sourceParentID, sourceVariableType, sourceTerminalType, sourceRefName);
		ACComponentNode target = getTerminalNode(parent, targetParentID, targetVariableType, targetTerminalType, targetRefName);
		
		if (definition == null)
		{
			// there is an error
			System.err.println("unpackConnectionNode: " + parent.getModuleDefinition().getName() + " Connection Definition not found.");
			displayErrorMessage();
			return null;
		}
		
		if (source == null)
		{
			// there is an error
			System.err.println("unpackConnectionNode: " + sourceRefName + " source terminal not found.");
			displayErrorMessage();
			return null;
		}
		
		if (target == null)
		{
			// there is an error
			System.err.println("unpackConnectionNode: " + targetRefName + " target terminal not found.");
			displayErrorMessage();
			return null;
		}
		
		ConnectionNode node = new ConnectionNode(parent, definition, drawingCellStyle);
		AC_GUI.drawingBoard.createConnection(node, source.getDrawingCell(), target.getDrawingCell());
		return node;
	}
	
	private static void packACComponentNode(Map<String, Object> data, ACComponentNode node)
	{
		data.put("drawingCellStyle", node.getDrawingCellStyle());
		data.put("drawingCellGeometry", packCellGeometry(node.getDrawingCellGeometry()));
	}
	
	private static void unpackACComponentNode(Map<String, Object> data, ACComponentNode node)
	{
		String drawingCellStyle = (String)data.get("drawingCellStyle");
		mxGeometry drawingCellGeometry = unpackCellGeometry((Map<String, Object>)data.get("drawingCellGeometry"));
		node.setDrawingCellStyle(drawingCellStyle);
		node.setDrawingCellGeometry(drawingCellGeometry);
		AC_GUI.drawingBoard.createACComponentNodeCell(node);
	}
	
	private static Map<String, Object> packCellGeometry(mxGeometry geo)
	{
		Map<String, Object> data= null;
		if (geo != null)
		{
			data = new HashMap<String, Object>();
			data.put("x", geo.getX());
			data.put("y", geo.getY());
			data.put("width", geo.getWidth());
			data.put("height", geo.getHeight());
		}
		return data;
	}
	
	private static mxGeometry unpackCellGeometry(Map<String, Object> data)
	{
		mxGeometry geo = null;
		if (data != null)
		{
			double x = (Double)data.get("x");
			double y = (Double)data.get("y");
			double width = (Double)data.get("width");
			double height = (Double)data.get("height");
			geo = new mxGeometry(x, y, width, height);
		}
		return geo;
	}
	
	private static ACComponentDefinition getTerminalDefinition(ModuleDefinition parent, String parentID, VariableType variableType, TerminalType terminalType, String refName)
	{
		ACComponentDefinition definition;
		ModuleDefinition terminalParent;
		ListIterator<ACComponentDefinition> list;
		
		if (parent.getID().equals(parentID))
		{
			terminalParent = parent;
		}
		else
		{
			// the terminal must belong to a child of parent
			terminalParent = getModuleDefinitionfromID(parent.getChildren().listIterator(), parentID);
			if (terminalParent == null)
			{
				// there is an error
				return null;
			}
		}
		switch (terminalType)
		{
			case EQUIVALENCE:
				list = terminalParent.getEquivalences().listIterator();
				break;
			case PORT:
				list = terminalParent.getPorts().listIterator();
				break;
			case VISIBLEVARIABLE:
				list = terminalParent.getVisibleVariables().listIterator();
				break;
			default:
				// there is an error
				System.err.println("getTerminalDefinition: " + terminalType + " is not a valid TerminalType.");
				displayErrorMessage();
				return null;
		}
		
		definition = getACComponentDefinition(list, refName, variableType);
		return definition;
	}
	
	private static ACComponentNode getTerminalNode(Module parent, String parentID, VariableType variableType, TerminalType terminalType, String refName)
	{
		Module terminalParent;
		
		if (parent.getID().equals(parentID))
		{
			terminalParent = parent;
		}
		else
		{
			// the terminal must belong to a child of parent
			terminalParent = getModule(parent.getChildren().listIterator(), parentID);
			if (terminalParent == null)
			{
				// there is an error
				return null;
			}
		}
		
		switch (terminalType)
		{
			case EQUIVALENCE:
				return getEquivalenceNode(terminalParent, refName, variableType);
			case PORT:
				return getPortNode(terminalParent, refName, variableType);
			case VISIBLEVARIABLE:
				return getVisibleVariableNode(terminalParent, refName, variableType);
			default:
				// there is an error
				System.err.println("getTerminalNode: " + terminalType + " is not a valid TerminalType.");
				displayErrorMessage();
				return null;
		}
	}
	
	private static ModuleDefinition getModuleDefinitionfromID(ListIterator<ModuleDefinition> list, String id)
	{
		ModuleDefinition currentDefinition;
		while (list.hasNext())
		{
			currentDefinition = list.next();
			if (currentDefinition.getID().equals(id))
			{
				return currentDefinition;
			}
		}
		
		ListIterator<ModuleDefinition> backupList = definitionList.listIterator();
		while (backupList.hasNext())
		{
			currentDefinition = backupList.next();
			if (currentDefinition.getID().equals(id))
			{
				return currentDefinition;
			}
		}
		return null;
	}
	
	private static Module getModule(ListIterator<Module> list, String id)
	{
		Module currentModule;
		while (list.hasNext())
		{
			currentModule = list.next();
			if (currentModule.getID().equals(id))
			{
				return currentModule;
			}
		}
		return null;
	}
	
	private static ACComponentDefinition getACComponentDefinition(ListIterator<ACComponentDefinition> list, String refName, VariableType variableType)
	{
		ACComponentDefinition currentDefinition;
		while (list.hasNext())
		{
			currentDefinition = list.next();
			if ((currentDefinition.getRefName().equals(refName)) && (currentDefinition.getVariableType() == variableType))
			{
				return currentDefinition;
			}
		}
		return null;
	}
	
	private static ACComponentNode getPortNode(Module parent, String refName, VariableType variableType)
	{
		ListIterator<ACComponentNode> list = parent.getPorts().listIterator();
		PortNode currentNode;
		while (list.hasNext())
		{
			currentNode = (PortNode)list.next();
			if ((currentNode.getDefinition().getRefName().equals(refName))
					&& (currentNode.getDefinition().getVariableType() == variableType))
			{
				return currentNode;
			}
		}
		return null;
	}
	
	private static ACComponentNode getEquivalenceNode(Module parent, String refName, VariableType variableType)
	{
		ListIterator<ACComponentNode> list = parent.getEquivalences().listIterator();
		EquivalenceNode currentNode;
		while (list.hasNext())
		{
			currentNode = (EquivalenceNode)list.next();
			if ((currentNode.getDefinition().getRefName().equals(refName))
					&& (currentNode.getDefinition().getVariableType() == variableType))
			{
				return currentNode;
			}
		}
		return null;
	}
	
	private static ACComponentNode getVisibleVariableNode(Module parent, String refName, VariableType variableType)
	{
		ListIterator<ACComponentNode> list = parent.getVisibleVariables().listIterator();
		VisibleVariableNode currentNode;
		while (list.hasNext())
		{
			currentNode = (VisibleVariableNode)list.next();
			if ((currentNode.getDefinition().getRefName().equals(refName))
					&& (currentNode.getDefinition().getVariableType() == variableType))
			{
				return currentNode;
			}
		}
		return null;
	}
	
	private static ConnectionDefinition getConnectionDefinition(ListIterator<ConnectionDefinition> list, VariableType sourceVariableType, TerminalType sourceTerminalType, String sourceRefName, VariableType targetVariableType, TerminalType targetTerminalType, String targetRefName)
	{
		ConnectionDefinition currentDefinition;
		while (list.hasNext())
		{
			currentDefinition = list.next();
			if ((sourceVariableType == currentDefinition.getSourceDefinition().getVariableType())
					&& (targetVariableType == currentDefinition.getTargetDefinition().getVariableType())
					&&(sourceTerminalType == currentDefinition.getSourceType()) 
					&& (targetTerminalType == currentDefinition.getTargetType())
					&& (sourceRefName.equals(currentDefinition.getSourceDefinition().getRefName()))
					&& (targetRefName.equals(currentDefinition.getTargetDefinition().getRefName())))
			{
				return currentDefinition;
			}
		}
		return null;
	}
	
	private static String validateModuleName(String name, Module parent)
	{
		if (!AC_Utility.newModuleNameValidation(parent, name, true))
		{
			return AC_Utility.promptUserForNewModuleName(parent, "Enter Module name:");	
		}
		return name;
	}
	
	private static String validateModuleDefinitionName(String name)
	{
		if (!AC_Utility.moduleDefinitionNameValidation(name, true))
		{
			return AC_Utility.promptUserForNewModuleDefinitionName("Enter Module Template name:");	
		}
		return name;
	}
	
	private static void displayErrorMessage()
	{
		JOptionPane.showMessageDialog(null,
			    "The input file is corrupt.",
			    "Error",
			    JOptionPane.ERROR_MESSAGE);
	}
}
