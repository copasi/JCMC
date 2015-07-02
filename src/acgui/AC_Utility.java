package acgui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import msmb.commonUtilities.ChangedElement;

import org.COPASI.CCopasiDataModel;
import org.sbml.libsbml.GeneralGlyph;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

public class AC_Utility
{
	public static String eol = System.getProperty("line.separator");
	private static Set<String> moduleDefinitionNameSet = new HashSet<String>();
	private static ArrayList<String> moduleNameList = new ArrayList<String>();
	private static ArrayList<ModuleDefinition> submoduleList = new ArrayList<ModuleDefinition>();
	//private static ArrayList<ModuleDefinition> moduleDefinitionList = new ArrayList<ModuleDefinition>();
	private static int id_index;
	
	public static Module createInstance(String name, Module parent, ModuleDefinition definition)
	{
		Module module = new Module(name, definition, parent);
		definition.addInstance(module);
		setMSMBData(module);
		module.setDrawingCellStyle("Submodule_No_Show_Information");
		AC_GUI.treeView.createNode(module);
		AC_GUI.drawingBoard.createCell(module);
		if (parent != null)
		{
			parent.addChild(module);
		}
		moduleNameList.add(name);

		return module;
	}
	
	public static Module createInstance(String name, String modulesbmlID, Module parent, ModuleDefinition definition, GeneralGlyph glyph)
	{
		// called from SBMLParser
		Module module = new Module(name, definition, parent);
		String cellStyle;
		module.setID(modulesbmlID);
		definition.addInstance(module);
		setMSMBData(module);
		if (definition instanceof MathematicalAggregatorDefinition)
		{
			cellStyle = ((MathematicalAggregatorDefinition)definition).getOperation().toString();
		}
		else
		{
			cellStyle = "Submodule_No_Show_Information";
		}
		module.setDrawingCellStyle(cellStyle);
		//masterModuleList.add(mod);
		AC_GUI.treeView.createNode(module);
		AC_GUI.drawingBoard.createCell(module, glyph);
		if (parent != null)
		{
			parent.addChild(module);
		}
		moduleNameList.add(name);
		return module;
	}
	
	public static Module createInstance(String name, ModuleDefinition definition, Module parent, mxGeometry moduleGeo, mxGeometry submoduleGeo, String cellStyle)
	{
		// called from AC_IO
		//public Module(ModuleDefinition iModuleDef, Module iParent, DefaultMutableTreeNode iTreeNode, mxCell iCell, mxGeometry iModuleGeo, mxGeometry iSubmoduleGeo, String iStyle)
		//public ModuleDefinition(String iName, ModuleDefinition iParent, String imsmbData)
		//ModuleDefinition modDef = createModuleDefinition(definitionName, modDefParent, msmbData, createDatamodel);
		Module mod = new Module(name, definition, parent, moduleGeo, submoduleGeo, cellStyle);
		definition.addInstance(mod);
		//setMSMBData(mod);
		AC_GUI.treeView.createNode(mod);
		AC_GUI.drawingBoard.createCell(mod);
		if (parent != null)
		{
			parent.addChild(mod);
		}
		moduleNameList.add(name);
		return mod;
	}
	
	/**
	 * Create a Module with the given name.
	 * @param name the name of the Module
	 * @param createDatamodel flag to create a new Copasi data model
	 * @return the created Module
	 */
	public static Module createModule(String name, String definitionName, boolean createDatamodel)
	{
		// called from File->New
		//ModuleDefinition modDef = createModuleDefinition(definitionName, createDatamodel);
		ModuleDefinition modDef = createModuleDefinition(name, createDatamodel);
		Module mod = new Module(name, modDef);
		modDef.addInstance(mod);
		setMSMBData(mod);
		mod.setDrawingCellStyle("Module");
		//masterModuleList.add(mod);
		AC_GUI.treeView.createNode(mod);
		AC_GUI.drawingBoard.createCell(mod);
		//AC_GUI.drawingBoard.changeModule(mod);
		//AC_GUI.modelBuilder.loadModel(mod, false, false, true);
		//modDef.setMSMBData(new String(AC_GUI.modelBuilder.saveModel()));
		//AC_GUI.modelBuilder.setVisible(true);

		//AC_GUI.changeActiveModule(mod);
		moduleNameList.add(name);
		return mod;
	}

	/**
	 * Create a Module with the given name.
	 * @param name the name of the Module
	 * @param parent the parent of the Module
	 * @param createDatamodel flag to create a new Copasi data model
	 * @return the created Module
	 */
	public static Module createModule(String name, String definitionName, Module parent, boolean createDatamodel)
	{
		// called from Module->Add Submodule (New)
		ModuleDefinition modDef = createModuleDefinition(definitionName, parent.getModuleDefinition(), createDatamodel);
		Module mod = new Module(name, modDef, parent);
		modDef.addInstance(mod);
		setMSMBData(mod);
		mod.setDrawingCellStyle("Submodule_No_Show_Information");
		//masterModuleList.add(mod);
		AC_GUI.treeView.createNode(mod);
		AC_GUI.drawingBoard.createCell(mod);
		//AC_GUI.drawingBoard.changeModule(mod);
		//AC_GUI.modelBuilder.loadModel(mod, false, false, true);
		//modDef.setMSMBData(new String(AC_GUI.modelBuilder.saveModel()));
		//AC_GUI.modelBuilder.setVisible(true);
		parent.addChild(mod);
		//AC_GUI.changeActiveModule(mod);
		moduleNameList.add(name);
		return mod;
	}
	
	/**
	 * Create a Module with the given name.
	 * @param name the name of the Module
	 * @param glyph the graphical information of the Module
	 * @param createDatamodel flag to create a new Copasi data model
	 * @return the created Module
	 */
	public static Module createModule(String name, String definitionName, GeneralGlyph glyph, boolean createDatamodel)
	{
		ModuleDefinition modDef = createModuleDefinition(definitionName, createDatamodel);
		Module mod = new Module(name, modDef);
		modDef.addInstance(mod);
		setMSMBData(mod);
		mod.setDrawingCellStyle("Module");
		//masterModuleList.add(mod);
		AC_GUI.treeView.createNode(mod);
		AC_GUI.drawingBoard.createCell(mod, glyph);
		//AC_GUI.drawingBoard.changeModule(mod);
		//AC_GUI.modelBuilder.loadModel(mod, false, false, true);
		//modDef.setMSMBData(new String(AC_GUI.modelBuilder.saveModel()));
		//AC_GUI.modelBuilder.setVisible(true);
		//AC_GUI.changeActiveModule(mod);
		moduleNameList.add(name);
		return mod;
	}
	
	/**
	 * Create a Module with the given name.
	 * @param name the name of the Module
	 * @param parent the parent of the Module
	 * @param glyph the graphical information of the Module
	 * @param createDatamodel flag to create a new Copasi data model
	 * @return the created Module
	 */
	public static Module createModule(String name, String definitionName, Module parent, GeneralGlyph glyph, boolean createDatamodel)
	{
		ModuleDefinition modDef = createModuleDefinition(definitionName, parent.getModuleDefinition(), createDatamodel);
		Module mod = new Module(name, modDef, parent);
		modDef.addInstance(mod);
		setMSMBData(mod);
		mod.setDrawingCellStyle("Submodule_No_Show_Information");
		//masterModuleList.add(mod);
		AC_GUI.treeView.createNode(mod);
		AC_GUI.drawingBoard.createCell(mod, glyph);
		//AC_GUI.drawingBoard.changeModule(mod);
		//AC_GUI.modelBuilder.loadModel(mod, false, false, true);
		//modDef.setMSMBData(new String(AC_GUI.modelBuilder.saveModel()));
		//AC_GUI.modelBuilder.setVisible(true);
		parent.addChild(mod);
		//AC_GUI.changeActiveModule(mod);
		moduleNameList.add(name);
		return mod;
	}
	
	/**
	 * Create a Module with the given information.
	 * @param name the name of the Module
	 * @param modParent the parent of the Module
	 * @param moduleGeo the module geometry of the Module
	 * @param submoduleGeo the submodule geometry of the Module
	 * @param cellStyle the drawing cell style of the Module
	 * @param modDefParent the parent of the Module Definition of the Module
	 * @param msmbData the msmb data of the Module Definition of the Module
	 * @param createDatamodel flag to create a new Copasi data model
	 * @return the created Module
	 */
	public static Module createModule(String name, String definitionName, Module modParent, mxGeometry moduleGeo, mxGeometry submoduleGeo, String cellStyle, ModuleDefinition modDefParent, byte[] msmbData, boolean createDatamodel)
	{
		// called from AC_IO
		//public Module(ModuleDefinition iModuleDef, Module iParent, DefaultMutableTreeNode iTreeNode, mxCell iCell, mxGeometry iModuleGeo, mxGeometry iSubmoduleGeo, String iStyle)
		//public ModuleDefinition(String iName, ModuleDefinition iParent, String imsmbData)
		ModuleDefinition modDef = createModuleDefinition(definitionName, modDefParent, msmbData, createDatamodel);
		Module mod = new Module(name, modDef, modParent, moduleGeo, submoduleGeo, cellStyle);
		modDef.addInstance(mod);
		setMSMBData(mod);
		AC_GUI.treeView.createNode(mod);
		AC_GUI.drawingBoard.createCell(mod);
		
		modParent.addChild(mod);
		moduleNameList.add(name);
		return mod;
	}
	
	public static Module createMathematicalAggregator(String name, String definitionName, Module parent, int terms, Operation op)
	{
		MathematicalAggregatorDefinition maDefinition = createMathematicalAggregatorDefinition(definitionName, parent.getModuleDefinition(), terms, op, true);
		Module maModule = new Module(name, maDefinition, parent);
		maDefinition.addInstance(maModule);
		setMSMBData(maModule);
		maModule.setDrawingCellStyle(op.toString());
		
		createMathematicalAggregatorPorts(maModule);
		
		AC_GUI.treeView.createNode(maModule);
		AC_GUI.drawingBoard.createMathematicalAggregatorNode(maModule);
		
		parent.addChild(maModule);
		moduleNameList.add(name);
		return maModule;
	}
	
	public static Module createMathematicalAggregator(String name, String definitionName, Module parent, int terms, Operation op, GeneralGlyph glyph)
	{
		MathematicalAggregatorDefinition maDefinition = createMathematicalAggregatorDefinition(definitionName, parent.getModuleDefinition(), terms, op, false);
		Module maModule = new Module(name, maDefinition);
		maDefinition.addInstance(maModule);
		setMSMBData(maModule);
		maModule.setDrawingCellStyle(op.toString());
		
		createMathematicalAggregatorPorts(maModule);
		
		AC_GUI.treeView.createNode(maModule);
		AC_GUI.drawingBoard.createMathematicalAggregatorNode(maModule, glyph);
		
		parent.addChild(maModule);
		moduleNameList.add(name);
		return maModule;
	}
	
	public static Module createMathematicalAggregator(String name, String definitionName, Module parent, byte[] msmbData, int terms, Operation op)
	{
		MathematicalAggregatorDefinition maDefinition = createMathematicalAggregatorDefinition(definitionName, parent.getModuleDefinition(), msmbData, terms, op, false);
		Module maModule = new Module(name, maDefinition);
		maDefinition.addInstance(maModule);
		setMSMBData(maModule);
		maModule.setDrawingCellStyle(op.toString());
		
		createMathematicalAggregatorPorts(maModule);
		
		AC_GUI.treeView.createNode(maModule);
		AC_GUI.drawingBoard.createMathematicalAggregatorNode(maModule);
		
		parent.addChild(maModule);
		moduleNameList.add(name);
		return maModule;
	}
	
	public static void deleteModule(Module mod)
	{
		if (mod.getParent() != null)
		{
			mod.getParent().removeChild(mod);
		}
		
		
		if (mod.getModuleDefinition() != null)
		{
			ModuleDefinition definition = mod.getModuleDefinition();
			definition.removeInstance(mod);
			if (definition.getInstances().isEmpty())
			{
				deleteModuleDefinition(definition);
			}
		}
		
		moduleNameList.remove(mod.getName());
	}
	
	public static ConnectionNode createConnection(Module parent, mxCell drawingCell, TerminalType sourceType, TerminalType targetType)
	{
		ACComponentDefinition sourceDefinition;
		switch(sourceType)
		{
			case EQUIVALENCE:
				sourceDefinition = ((EquivalenceNode)drawingCell.getSource().getValue()).getEquivalenceDefinition();
				break;
			case PORT:
				sourceDefinition = ((PortNode)drawingCell.getSource().getValue()).getPortDefinition();
				break;
			case VISIBLEVARIABLE:
				sourceDefinition = ((VisibleVariableNode)drawingCell.getSource().getValue()).getVisibleVariableDefinition();
				break;
			default:
				System.err.println("AC_Utility.createConnection: " + sourceType + " is not a valid source TerminalType.");
				//displayErrorMessage("There was a problem while creating the connection.");
				displayMessage(JOptionPane.ERROR_MESSAGE, "Connection Failure", "There was a problem while creating the connection.");
				return null;
		}
		
		ACComponentDefinition targetDefinition;
		switch(targetType)
		{
			case EQUIVALENCE:
				targetDefinition = ((EquivalenceNode)drawingCell.getTarget().getValue()).getEquivalenceDefinition();
				break;
			case PORT:
				targetDefinition = ((PortNode)drawingCell.getTarget().getValue()).getPortDefinition();
				break;
			case VISIBLEVARIABLE:
				targetDefinition = ((VisibleVariableNode)drawingCell.getTarget().getValue()).getVisibleVariableDefinition();
				break;
			default:
				System.err.println("AC_Utility.createConnection: " + targetType + " is not a valid target TerminalType.");
				//displayErrorMessage("There was a problem while creating the connection.");
				displayMessage(JOptionPane.ERROR_MESSAGE, "Connection Failure", "There was a problem while creating the connection.");
				return null;
		}
		
		ConnectionDefinition cDefinition = createConnectionDefinition(parent.getModuleDefinition(), sourceDefinition, sourceType, targetDefinition, targetType);
		ConnectionNode cNode = new ConnectionNode(parent, cDefinition, drawingCell);
		cNode.setDrawingCellStyle(drawingCell.getStyle());
		
		AC_GUI.drawingBoard.setValue(drawingCell, cNode);
		parent.addConnection(cNode);
		
		return cNode;
	}
	
	public static ConnectionNode createConnection(Module parent, mxCell source, TerminalType sourceType, mxCell target, TerminalType targetType, String drawingCellStyle)
	{
		ACComponentDefinition sourceDefinition;
		switch(sourceType)
		{
			case EQUIVALENCE:
				sourceDefinition = ((EquivalenceNode)source.getValue()).getEquivalenceDefinition();
				break;
			case PORT:
				sourceDefinition = ((PortNode)source.getValue()).getPortDefinition();
				break;
			case VISIBLEVARIABLE:
				sourceDefinition = ((VisibleVariableNode)source.getValue()).getVisibleVariableDefinition();
				break;
			default:
				System.err.println("AC_Utility.createConnection: " + sourceType + " is not a valid source TerminalType.");
				//displayErrorMessage("There was a problem while creating the connection.");
				displayMessage(JOptionPane.ERROR_MESSAGE, "Connection Failure", "There was a problem while creating the connection.");
				return null;
		}
		
		ACComponentDefinition targetDefinition;
		switch(targetType)
		{
			case EQUIVALENCE:
				targetDefinition = ((EquivalenceNode)target.getValue()).getEquivalenceDefinition();
				break;
			case PORT:
				targetDefinition = ((PortNode)target.getValue()).getPortDefinition();
				break;
			case VISIBLEVARIABLE:
				targetDefinition = ((VisibleVariableNode)target.getValue()).getVisibleVariableDefinition();
				break;
			default:
				System.err.println("AC_Utility.createConnection: " + targetType + " is not a valid target TerminalType.");
				//displayErrorMessage("There was a problem while creating the connection.");
				displayMessage(JOptionPane.ERROR_MESSAGE, "Connection Failure", "There was a problem while creating the connection.");
				return null;
		}
		
		ConnectionDefinition cDefinition = createConnectionDefinition(parent.getModuleDefinition(), sourceDefinition, sourceType, targetDefinition, targetType);
		ConnectionNode cNode = new ConnectionNode(parent, cDefinition, drawingCellStyle);
		
		AC_GUI.drawingBoard.createConnection(cNode, source, target);
		parent.addConnection(cNode);
		
		return cNode;
	}
	
	public static ConnectionNode createConnectionNode(Module parent, ConnectionNode templateNode)
	{
		ACComponentNode sourceNode = getTerminalNode(parent, templateNode.getConnectionDefinition().getSourceType(), templateNode.getConnectionDefinition().getSourceDefinition().getRefName());
		ACComponentNode targetNode = getTerminalNode(parent, templateNode.getConnectionDefinition().getTargetType(), templateNode.getConnectionDefinition().getTargetDefinition().getRefName());
		ConnectionNode node = new ConnectionNode(parent, templateNode.getConnectionDefinition(), templateNode.getDrawingCellStyle());
		AC_GUI.drawingBoard.createConnection(node, sourceNode.getDrawingCell(), targetNode.getDrawingCell());
		parent.addConnection(node);
		
		return node;
	}
	
	public static boolean addNodetoRemainingInstances(Module module, ConnectionNode templateNode)
	{
		boolean success = true;
		ModuleDefinition definition = module.getModuleDefinition();
		if (definition.getInstances().size() <= 1)
		{
			return success;
		}
		ListIterator<Module> moduleList = definition.getInstances().listIterator();
		Module parent;
		while (moduleList.hasNext())
		{
			parent = moduleList.next();
			if (parent == module)
			{
				continue;
			}
			else
			{
				if (createConnectionNode(parent, templateNode) == null)
				{
					success = false;
				}
			}
		}
		return success;
	}
	
	public static void deleteConnection(ConnectionNode cNode)
	{
		ConnectionDefinition connectionDefinition = cNode.getConnectionDefinition();
		ModuleDefinition definition = cNode.getParent().getModuleDefinition();
		if (definition.getInstances().size() == 1)
		{
			// there is only one instance of the module,
			// we only need to delete one connection node
			cNode.getParent().removeConnection(cNode);
		}
		else
		{
			// there are multiple instances of the module,
			// we need to delete the connection node from each instance
			ListIterator<Module> moduleList = definition.getInstances().listIterator();
			Module parent;
			ConnectionNode node;
			while (moduleList.hasNext())
			{
				parent = moduleList.next();
				node = getConnectionNode(parent, connectionDefinition);
				if (node == null)
				{
					String msg = "AC_Utility.deleteConnection(): ConnectionNode is null." + eol;
					msg += "ConnectionNode information:" + eol;
					msg += "Source Definition refName: " + connectionDefinition.getSourceDefinition().getRefName();
					msg += eol;
					msg += "Target Definition refName: " + connectionDefinition.getTargetDefinition().getRefName();
					msg += eol;
					System.err.println(msg);
				}
				else
				{
					parent.removeConnection(node);
				}
			}
		}
		
		// delete the connection definition
		deleteConnectionDefinition(connectionDefinition);
	}
	
	public static EquivalenceNode createEquivalence(String refName, VariableType vType, Module parent)
	{
		EquivalenceDefinition eDefinition = createEquivalenceDefinition(parent.getModuleDefinition(), refName, vType);
		EquivalenceNode eNode = new EquivalenceNode(parent, eDefinition);
		AC_GUI.drawingBoard.createEquivalenceNode(eNode);
		
		parent.addEquivalence(eNode);
		
		return eNode;
	}
	
	public static EquivalenceNode createEquivalence(String refName, VariableType vType, mxCell drawingCell, Module parent)
	{
		EquivalenceDefinition eDefinition = createEquivalenceDefinition(parent.getModuleDefinition(), refName, vType);
		EquivalenceNode eNode = new EquivalenceNode(parent, eDefinition);
		eNode.setDrawingCell(drawingCell);
		eNode.setDrawingCellGeometry(drawingCell.getGeometry());
		eNode.setDrawingCellStyle(drawingCell.getStyle());
		//AC_GUI.drawingBoard.createEquivalenceNode(eNode);
		
		parent.addEquivalence(eNode);
		
		return eNode;
	}
	
	public static EquivalenceNode createEquivalence(String refName, VariableType vType, Module parent, GeneralGlyph glyph)
	{
		EquivalenceDefinition eDefinition = createEquivalenceDefinition(parent.getModuleDefinition(), refName, vType);
		EquivalenceNode eNode = new EquivalenceNode(parent, eDefinition);
		AC_GUI.drawingBoard.createEquivalenceNode(eNode, glyph);
		
		parent.addEquivalence(eNode);
		
		return eNode;
	}
	
	public static EquivalenceNode createEquivalenceNode(Module parent, EquivalenceDefinition eDefinition)
	{
		EquivalenceNode eNode = new EquivalenceNode(parent, eDefinition);
		AC_GUI.drawingBoard.createEquivalenceNode(eNode);
		
		parent.addEquivalence(eNode);
		
		return eNode;
	}
	
	public static EquivalenceNode createEquivalenceNode(Module parent, EquivalenceDefinition eDefinition, GeneralGlyph glyph)
	{
		EquivalenceNode eNode = new EquivalenceNode(parent, eDefinition);
		AC_GUI.drawingBoard.createEquivalenceNode(eNode, glyph);
		
		parent.addEquivalence(eNode);
		
		return eNode;
	}
	
	public static boolean addNodetoRemainingInstances(Module module, EquivalenceDefinition eDefinition)
	{
		boolean success = true;
		ModuleDefinition definition = module.getModuleDefinition();
		if (definition.getInstances().size() <= 1)
		{
			return success;
		}
		ListIterator<Module> moduleList = definition.getInstances().listIterator();
		Module parent;
		while (moduleList.hasNext())
		{
			parent = moduleList.next();
			if (parent == module)
			{
				continue;
			}
			else
			{
				if (createEquivalenceNode(parent, eDefinition) == null)
				{
					success = false;
				}
			}
		}
		return success;
	}
	
	public static void deleteEquivalence(EquivalenceNode eNode)
	{
		EquivalenceDefinition eDefinition = eNode.getEquivalenceDefinition();
		ModuleDefinition definition = eNode.getParent().getModuleDefinition();
		if (definition.getInstances().size() == 1)
		{
			// there is only one instance of the module,
			// we only need to delete one equivalence node
			eNode.getParent().removeEquivalence(eNode);
		}
		else
		{
			// there are multiple instances of the module,
			// we need to delete the equivalence node from each instance
			ListIterator<Module> moduleList = definition.getInstances().listIterator();
			Module parent;
			EquivalenceNode node;
			while (moduleList.hasNext())
			{
				parent = moduleList.next();
				node = (EquivalenceNode)getEquivalenceNode(parent, eDefinition.getRefName());
				if (node == null)
				{
					String msg = eol;
					msg += "AC_Utility.deleteEquivalence(): EquivalenceNode is null." + eol;
					msg += "EquivalenceNode refName: " + eDefinition.getRefName() + eol;
					System.err.println(msg);
				}
				else
				{
					parent.removeEquivalence(node);
				}
			}
		}

		// delete the equivalence definition
		deleteEquivalenceDefinition(eNode.getEquivalenceDefinition());
	}
	
	public static PortNode createPort(Module parent, String refName, String name, PortType pType, VariableType vType)
	{
		PortDefinition pDefinition = createPortDefinition(parent.getModuleDefinition(), refName, name, pType, vType);
		PortNode pNode = new PortNode(parent, pDefinition);
		AC_GUI.drawingBoard.createPort(pNode);
		
		parent.addPort(pNode);
		
		return pNode;
	}
	
	public static PortNode createPort(Module parent, String refName, String name, PortType pType, VariableType vType, GeneralGlyph glyph)
	{
		PortDefinition pDefinition = createPortDefinition(parent.getModuleDefinition(), refName, name, pType, vType);
		PortNode pNode = new PortNode(parent, pDefinition);
		AC_GUI.drawingBoard.createPort(pNode, glyph);
		
		parent.addPort(pNode);
		
		return pNode;
	}
	
	public static PortNode createPortNode(Module parent, PortDefinition definition)
	{
		PortNode pNode = new PortNode(parent, definition);
		AC_GUI.drawingBoard.createPort(pNode);
		
		parent.addPort(pNode);
		
		return pNode;
	}
	
	public static PortNode createPortNode(Module parent, String name, PortDefinition definition, GeneralGlyph glyph)
	{
		PortNode pNode = new PortNode(parent, definition);
		AC_GUI.drawingBoard.createPort(pNode, glyph);
		
		parent.addPort(pNode);
		
		return pNode;
	}
	
	public static boolean addNodetoRemainingInstances(Module module, PortDefinition pDefinition)
	{
		boolean success = true;
		ModuleDefinition definition = module.getModuleDefinition();
		if (definition.getInstances().size() <= 1)
		{
			return success;
		}
		ListIterator<Module> moduleList = definition.getInstances().listIterator();
		Module parent;
		while (moduleList.hasNext())
		{
			parent = moduleList.next();
			if (parent == module)
			{
				continue;
			}
			else
			{
				if (createPortNode(parent, pDefinition) == null)
				{
					success = false;
				}
			}
		}
		return success;
	}
	
	public static void deletePort(PortNode pNode)
	{
		PortDefinition pDefinition = pNode.getPortDefinition();
		ModuleDefinition definition = pNode.getParent().getModuleDefinition();
		if (definition.getInstances().size() == 1)
		{
			// there is only one instance of the module,
			// we only need to delete one port node
			pNode.getParent().removePort(pNode);
		}
		else
		{
			// there are multiple instances of the module,
			// we need to delete the port node from each instance
			ListIterator<Module> moduleList = definition.getInstances().listIterator();
			Module parent;
			PortNode node;
			while (moduleList.hasNext())
			{
				parent = moduleList.next();
				node = (PortNode)getPortNode(parent, pDefinition.getRefName());
				if (node == null)
				{
					String msg = eol;
					msg += "AC_Utility.deletePort(): PortNode is null." + eol;
					msg += "PortNode refName: " + pDefinition.getRefName() + eol;
					System.err.println(msg);
				}
				else
				{
					parent.removePort(node);
				}
			}
		}
		
		// delete the port definition
		deletePortDefinition(pDefinition);
	}
	
	public static VisibleVariableNode createVisibleVariable(String refName, VariableType vType, Module parent)
	{
		VisibleVariableDefinition vDefinition = createVisibleVariableDefinition(parent.getModuleDefinition(), refName, vType);
		VisibleVariableNode vNode = new VisibleVariableNode(parent, vDefinition);
		AC_GUI.drawingBoard.createVisibleVariable(vNode);
		
		parent.addVisibleVariable(vNode);
		
		return vNode;
	}
	
	public static VisibleVariableNode createVisibleVariable(String refName, VariableType vType, Module parent, GeneralGlyph glyph)
	{
		VisibleVariableDefinition vDefinition = createVisibleVariableDefinition(parent.getModuleDefinition(), refName, vType);
		VisibleVariableNode vNode = new VisibleVariableNode(parent, vDefinition);
		AC_GUI.drawingBoard.createVisibleVariable(vNode, glyph);
		
		parent.addVisibleVariable(vNode);
		
		return vNode;
	}

	public static VisibleVariableNode createVisibleVariable(String refName, VariableType vType, Module parent, mxCell drawingCell)
	{
		VisibleVariableDefinition vDefinition = createVisibleVariableDefinition(parent.getModuleDefinition(), refName, vType);
		VisibleVariableNode vNode = new VisibleVariableNode(parent, vDefinition, drawingCell, drawingCell.getGeometry());
		vNode.setDrawingCellStyle("VisibleVariable");
		
		parent.addVisibleVariable(vNode);
		
		return vNode;
	}
	
	public static VisibleVariableNode createVisibleVariableNode(Module parent, VisibleVariableDefinition vDefinition)
	{
		VisibleVariableNode vNode = new VisibleVariableNode(parent, vDefinition);
		AC_GUI.drawingBoard.createVisibleVariable(vNode);
		
		parent.addVisibleVariable(vNode);
		
		return vNode;
	}
	
	public static VisibleVariableNode createVisibleVariableNode(Module parent, VisibleVariableDefinition vDefinition, GeneralGlyph glyph)
	{
		VisibleVariableNode vNode = new VisibleVariableNode(parent, vDefinition);
		AC_GUI.drawingBoard.createVisibleVariable(vNode, glyph);
		
		parent.addVisibleVariable(vNode);
		
		return vNode;
	}
	
	public static boolean addNodetoRemainingInstances(Module module, VisibleVariableDefinition vDefinition)
	{
		boolean success = true;
		ModuleDefinition definition = module.getModuleDefinition();
		if (definition.getInstances().size() <= 1)
		{
			return success;
		}
		ListIterator<Module> moduleList = definition.getInstances().listIterator();
		Module parent;
		while (moduleList.hasNext())
		{
			parent = moduleList.next();
			if (parent == module)
			{
				continue;
			}
			else
			{
				if (createVisibleVariableNode(parent, vDefinition) == null)
				{
					success = false;
				}
			}
		}
		return success;
	}
	
	public static void deleteVisibleVariable(VisibleVariableNode vNode)
	{
		VisibleVariableDefinition vDefinition = vNode.getVisibleVariableDefinition();
		ModuleDefinition definition = vNode.getParent().getModuleDefinition();
		if (definition.getInstances().size() == 1)
		{
			// there is only one instance of the module,
			// we only need to delete one visible variable  node
			vNode.getParent().removeVisibleVariable(vNode);
		}
		else
		{
			// there are multiple instances of the module,
			// we need to delete the visible variable node from each instance
			ListIterator<Module> moduleList = definition.getInstances().listIterator();
			Module parent;
			VisibleVariableNode node;
			while (moduleList.hasNext())
			{
				parent = moduleList.next();
				node = (VisibleVariableNode)getVisibleVariableNode(parent, vDefinition.getRefName());
				if (node == null)
				{
					String msg = eol;
					msg += "AC_Utility.deleteVisibleVariable(): VisibleVariableNode is null." + eol;
					msg += "VisibleVariableNode refName: " + vDefinition.getRefName() + eol;
					System.err.println(msg);
				}
				else
				{
					while (node != null)
					{
						parent.removeVisibleVariable(node);
						node = (VisibleVariableNode)getVisibleVariableNode(parent, vDefinition.getRefName());
					}
				}
			}
		}
		
		// delete the visible variable definition
		deleteVisibleVariableDefinition(vDefinition);
	}
	
	public static boolean copyDefinition(Module module, String newName)
	{
		String name;
		if (newName == null)
		{
			name = promptUserForNewModuleName(module.getParent(), "Please enter a new Module Template name:");
			if (name == null)
			{
				System.err.println("No new definition created.");
				return false;
			}
		}
		else
		{
			name = newName;
		}
		
		ModuleDefinition oldDefinition = module.getModuleDefinition();
		ModuleDefinition newDefinition = copyModuleDefinition(module, name);
		
		oldDefinition.removeInstance(module);
		newDefinition.addInstance(module);
		
		module.setModuleDefinition(newDefinition);
		addSubmoduleDefinition(newDefinition);
		return true;
	}
	
	public static Module instantiateModuleDefinition(String moduleName, ModuleDefinition definition, Module parent)
	{
		Module module = new Module(moduleName, definition, parent);
		if (definition.getInstances().size() == 0)
		{
			System.err.println("No instances of the module definition " + definition.getName() + " exist.");
			return null;
		}
		Module templateModule = definition.getInstances().get(0);
		module.setDrawingCellStyle(templateModule.getDrawingCellStyle());
		AC_GUI.treeView.createNode(module);
		AC_GUI.drawingBoard.createCell(module);
		definition.addInstance(module);
		moduleNameList.add(moduleName);
		parent.addChild(module);
		ListIterator<ACComponentNode> iterator;
		
		PortNode templatePNode;
		iterator = templateModule.getPorts().listIterator();
		while (iterator.hasNext())
		{
			templatePNode = (PortNode)iterator.next();
			PortNode pNode = new PortNode(module, templatePNode.getPortDefinition());
			AC_GUI.drawingBoard.createPort(pNode, templatePNode.getDrawingCellGeometry());
			pNode.setDrawingCellStyle(templatePNode.getDrawingCellStyle());
			module.addPort(pNode);
		}
		
		EquivalenceNode templateENode;
		iterator = templateModule.getEquivalences().listIterator();
		while (iterator.hasNext())
		{
			templateENode = (EquivalenceNode)iterator.next();
			EquivalenceNode eNode = new EquivalenceNode(module, templateENode.getEquivalenceDefinition());
			AC_GUI.drawingBoard.createEquivalenceNode(eNode, templateENode.getDrawingCellGeometry());
			eNode.setDrawingCellStyle(templateENode.getDrawingCellStyle());
			module.addEquivalence(eNode);
		}
		
		VisibleVariableNode templateVNode;
		iterator = templateModule.getVisibleVariables().listIterator();
		while (iterator.hasNext())
		{
			templateVNode = (VisibleVariableNode)iterator.next();
			VisibleVariableNode vNode = new VisibleVariableNode(module, templateVNode.getVisibleVariableDefinition());
			AC_GUI.drawingBoard.createVisibleVariable(vNode, templateVNode.getDrawingCellGeometry());
			vNode.setDrawingCellStyle(templateVNode.getDrawingCellStyle());
			module.addVisibleVariable(vNode);
		}
		
		ConnectionNode templateCNode;
		ListIterator<ConnectionNode> connections = templateModule.getConnections().listIterator();
		while (connections.hasNext())
		{
			templateCNode = connections.next();
			ACComponentNode sourceNode = getTerminalNode(module, templateCNode.getConnectionDefinition().getSourceType(), templateCNode.getConnectionDefinition().getSourceDefinition().getRefName());
			ACComponentNode targetNode = getTerminalNode(module, templateCNode.getConnectionDefinition().getTargetType(), templateCNode.getConnectionDefinition().getTargetDefinition().getRefName());
			ConnectionNode node = new ConnectionNode(module, templateCNode.getConnectionDefinition(), templateCNode.getDrawingCellStyle());
			AC_GUI.drawingBoard.createConnection(node, sourceNode.getDrawingCell(), targetNode.getDrawingCell());
			//parent.addConnection(node);
			module.addConnection(node);
		}
		
		return module;
	}
	
	public static String promptUserForNewModuleDefinitionName(String message)
	{
		//String message = "Enter Module Template name:";
		String newName = JOptionPane.showInputDialog(message, "");
		while (newName != null)
		{
			if (moduleDefinitionNameValidation(newName, true))
			{
				return newName;
			}
			newName = JOptionPane.showInputDialog(message, "");
		}
		return null;
	}
	
	public static String promptUserForNewModuleName(String message)
	{
		//String message = "Enter Module name:";
		String newName = JOptionPane.showInputDialog(message, "");
		while (newName != null)
		{
			if (moduleNameValidation(newName, true))
			{
				return newName;
			}
			newName = JOptionPane.showInputDialog(message, "");
		}
		return null;
	}
	
	public static String promptUserForNewModuleName(Module parent, String message)
	{
		//String message = "Enter " + moduleType + " name:";
		String newName = JOptionPane.showInputDialog(message, "");
		while (newName != null)
		{
			if (newModuleNameValidation(parent, newName, true))
			{
				return newName;
			}
			newName = JOptionPane.showInputDialog(message, "");
		}
		return null;
	}
	
	public static void promptUserEditModuleName(String initialName)
	{
		String msg = "Please enter a name:";
		String name = (String)JOptionPane.showInputDialog(null, msg, "Edit Module Name", JOptionPane.QUESTION_MESSAGE, null, null, initialName);
		while (name != null)
		{
			name = name.trim();
			if (moduleNameValidation(name, true))
			{
				changeModuleName(AC_GUI.selectedModule, name, false);
				return;
			}
			name = (String)JOptionPane.showInputDialog(null, msg, "Edit Module Name", JOptionPane.QUESTION_MESSAGE, null, null, initialName);
		}
	}
	
	public static void promptUserEditModuleName(Module module, String initialName)
	{
		String msg = "Please enter a name:";
		String name = (String)JOptionPane.showInputDialog(null, msg, "Edit Module Name", JOptionPane.QUESTION_MESSAGE, null, null, initialName);
		while (name != null)
		{
			name = name.trim();
			if (editModuleNameValidation(module, name, true))
			{
				changeModuleName(module, name, false);
				return;
			}
			name = (String)JOptionPane.showInputDialog(null, msg, "Edit Module Name", JOptionPane.QUESTION_MESSAGE, null, null, initialName);
		}
	}
	
	/**
	 * Determine if the given name is the name of an existing Module.
	 * @param name the name to check
	 * @return true if the name is not being used by an existing Module,
	 * otherwise false.
	 */
	public static boolean moduleNameValidation(String name, boolean displayMessage)
	{
		String message;
		
		if ((name == null) || (name.isEmpty()))
		{
			if (displayMessage)
			{
				message = "Please enter a name.";
				JOptionPane.showMessageDialog(null, message, "Invalid Name", JOptionPane.WARNING_MESSAGE);
			}
			return false;
		}
		
		if (sbmlNameValidation(name, displayMessage))
		{
			if (moduleNameList.contains(name))
			{
				if (displayMessage)
				{
					message = "There already exists a Module with the name \"" + name + "\"." + eol;
					message += "Please enter a different name.";
					JOptionPane.showMessageDialog(null, message, "Invalid Name", JOptionPane.WARNING_MESSAGE);
				}
				return false;
			}
			return true;
		}
		
		return false;
	}
	
	public static boolean newModuleNameValidation(Module parent, String name, boolean displayMessage)
	{		
		if (isNameEmpty(name, displayMessage))
		{
			return false;
		}
		
		if (sbmlNameValidation(name, displayMessage))
		{
			if (parent != null)
			{
				// the name in question belongs to a submodule
				if (moduleNameConflictParent(parent, name, displayMessage))
				{
					return false;
				}
				else if (moduleNameConflictSiblings(parent, name, displayMessage))
				{
					return false;
				}
			}
			return true;
		}
		
		return false;
	}
	
	public static boolean editModuleNameValidation(Module module, String name, boolean displayMessage)
	{		
		if (isNameEmpty(name, displayMessage))
		{
			return false;
		}
		
		if (module.getName().equals(name))
		{
			return true;
		}
		
		if (sbmlNameValidation(name, displayMessage))
		{
			Module parent = module.getParent();
			if (parent != null)
			{
				// the name in question belongs to a submodule
				if (moduleNameConflictParent(parent, name, displayMessage))
				{
					return false;
				}
				else if (moduleNameConflictSiblings(parent, name, displayMessage))
				{
					return false;
				}
			}
			
			if (moduleNameConflictChildren(module, name, displayMessage))
			{
				return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	public static boolean moduleDefinitionNameValidation(String name, boolean displayMessage)
	{
		if (isNameEmpty(name, displayMessage))
		{
			return false;
		}
		
		if (sbmlNameValidation(name, displayMessage))
		{
			if (moduleDefinitionNameConflict(name, displayMessage))
			{
				return false;
			}
			return true;
		}
		
		return false;
	}
	
	public static boolean moduleDefinitionNameConflict(String name, boolean displayMessage)
	{
		if (moduleDefinitionNameSet.contains(name))
		{
			if (displayMessage)
			{
				String message = "A module template with the name \"" + name + "\" already exists." + eol;
				message += "Module template names must be unique." + eol;
				message += "Please enter a different name.";
				//JOptionPane.showMessageDialog(null, message, "Invalid Name", JOptionPane.WARNING_MESSAGE);
				displayMessage(JOptionPane.WARNING_MESSAGE, "Invalid Name", message);
			}
			return true;
		}
		
		return false;
	}
	
	public static boolean isNameEmpty(String name, boolean displayMessage)
	{
		if ((name == null) || (name.isEmpty()))
		{
			if (displayMessage)
			{
				String message = "Please enter a name.";
				//JOptionPane.showMessageDialog(null, message, "Invalid Name", JOptionPane.WARNING_MESSAGE);
				displayMessage(JOptionPane.WARNING_MESSAGE, "Invalid Name", message);
			}
			return true;
		}
		return false;
	}
	

	public static boolean moduleNameConflictParent(Module parent, String name, boolean displayMessage)
	{
		if (parent.getName().equals(name))
		{
			// a module cannot have the same name as its container module
			if (displayMessage)
			{
				String message = "The container module is already named \"" + name + "\"." + eol;
				message += "A submodule cannot have the same name as its container module." + eol;
				message += "Please enter a different name.";
				//JOptionPane.showMessageDialog(null, message, "Invalid Name", JOptionPane.WARNING_MESSAGE);
				displayMessage(JOptionPane.WARNING_MESSAGE, "Invalid Name", message);
			}
			return true;
		}
		return false;
	}
	
	public static boolean moduleNameConflictSiblings(Module parent, String name, boolean displayMessage)
	{
		if (parent.checkSubmoduleName(name))
		{
			if (displayMessage)
			{
				String message = "A submodule with the name \"" + name + "\" already exists." + eol;
				message += "Submodules on the same hierarchical level must have unique names." + eol;
				message += "Please enter a different name.";
				//JOptionPane.showMessageDialog(null, message, "Invalid Name", JOptionPane.WARNING_MESSAGE);
				displayMessage(JOptionPane.WARNING_MESSAGE, "Invalid Name", message);
			}
			return true;
		}
		return false;
	}
	
	public static boolean moduleNameConflictChildren(Module module, String name, boolean displayMessage)
	{
		if (module.checkSubmoduleName(name))
		{
			if (displayMessage)
			{
				String message = "\"" + module.getName() + "\" ";
				message += " already contains a submodule with the name \"" + name + "\"." + eol;
				message += "A container cannot have the same name as one of its submodules." + eol;
				message += "Please enter a different name.";
				//JOptionPane.showMessageDialog(null, message, "Invalid Name", JOptionPane.WARNING_MESSAGE);
				displayMessage(JOptionPane.WARNING_MESSAGE, "Invalid Name", message);
			}
			return true;
		}
		return false;
	}
	
	public static int promptUserExternalModuleChange(Module module)
	{
		String msg = "You have attempted to edit an external entity. You cannot edit such a Module." + eol;
		msg += "Would you like to save the Module as an internal definition?" + eol;
		msg += "New Module: Save changes as a new Module." + eol;
		msg += "Cancel: Do not save changes." + eol;
		Object[] options = {"New Module", "Cancel"};
		int n = JOptionPane.showOptionDialog(null,
			    msg,
			    "Warning",
			    JOptionPane.YES_NO_OPTION,
			    JOptionPane.WARNING_MESSAGE,
			    null,     //do not use a custom Icon
			    options,  //the titles of buttons
			    JOptionPane.NO_OPTION); //default button title
		switch(n)
		{
			case JOptionPane.YES_OPTION:
				System.out.println("The user chose to save as a New Module.");
				break;
			case JOptionPane.NO_OPTION:
				System.out.print("The user chose not to save changes.");
				break;
		}
		return n;
	}
	
	public static int promptUserSubmoduleChange(Module currentModule)
	{
		ModuleDefinition definition = currentModule.getModuleDefinition();
		if (definition.getInstances().size() > 1)
		{
			ListIterator<Module> instanceList = definition.getInstances().listIterator();
			Module iModule;
			String instances = "";
			while (instanceList.hasNext())
			{
				iModule = instanceList.next();
				if (!iModule.getName().equals(currentModule.getName()))
				{
					instances += "    " + iModule.getName() + eol;
				}
			}
			String msg = "You have attempted to edit a Module that has multiple instantiations. Editing the current Module" + eol;
			msg += "will change the following instances:" + eol;
			msg += instances + eol;
			msg += "How would you like to save?" + eol;
			msg += "New Module: Save changes as a new Module (do not change other instantiations)." + eol;
			msg += "Current Module: Save changes to the current Module (change all instantiations)." + eol;
			msg += "Cancel: Do not save changes." + eol;
			Object[] options = {"New Module", "Current Module", "Cancel"};
			int n = JOptionPane.showOptionDialog(null,
				    msg,
				    "Warning",
				    JOptionPane.YES_NO_CANCEL_OPTION,
				    JOptionPane.WARNING_MESSAGE,
				    null,     //do not use a custom Icon
				    options,  //the titles of buttons
				    JOptionPane.CANCEL_OPTION); //default button title
			switch(n)
			{
				case JOptionPane.YES_OPTION:
					System.out.println("The user chose New Module.");
					break;
				case JOptionPane.NO_OPTION:
					System.out.print("The user chose Current Module.");
					msg = "This will change the current Module and the following instances:" + eol;
					msg += instances + eol;
					msg += "Are you sure?";
					Object[] options2 = {"Yes", "No"};
					int n2 = JOptionPane.showOptionDialog(null,
						    msg,
						    "Warning",
						    JOptionPane.YES_NO_OPTION,
						    JOptionPane.WARNING_MESSAGE,
						    null,     //do not use a custom Icon
						    options2,  //the titles of buttons
						    options2[1]); //default button title
					switch(n2)
					{
						case JOptionPane.YES_OPTION:
							System.out.println("The user confirmed changing the current Module.");
							break;
						case JOptionPane.NO_OPTION:
							System.out.println("The user did not confirm changing the current Module.");
							return promptUserSubmoduleChange(currentModule);
							//break;
					}
					break;
				case JOptionPane.CANCEL_OPTION:
					System.out.println("The user chose Cancel.");
					break;
			}
			return n;
		}
		return -1;
	}
	
	public static int promptUserAddSubmodule(Module module)
	{
		String msg = "To add a Submodule, the current Module must be saved as a new Module." + eol;
		Object[] options = {"New Module", "Cancel"};
		int n = JOptionPane.showOptionDialog(null,
			    msg,
			    "Warning",
			    JOptionPane.YES_NO_OPTION,
			    JOptionPane.WARNING_MESSAGE,
			    null,     //do not use a custom Icon
			    options,  //the titles of buttons
			    JOptionPane.NO_OPTION); //default button title
		switch(n)
		{
			case JOptionPane.YES_OPTION:
				System.out.println("The user chose to save as a New Module.");
				break;
			case JOptionPane.NO_OPTION:
				System.out.print("The user chose not to save changes.");
				break;
		}
		return n;
	}
	
	public static boolean showVariableValidation(String extendedRefName, Module module)
	{
		String shortRefName = "";
		VariableType vType = null;
		ModuleDefinition moduleDefinition = module.getModuleDefinition();
		
		// trim the refName
		if (extendedRefName.endsWith(VariableType.SPECIES.toString()))
		{
			shortRefName = extendedRefName.replace(" - " + VariableType.SPECIES.toString(), "");
			vType = VariableType.SPECIES;
		}
		else if (extendedRefName.endsWith(VariableType.GLOBAL_QUANTITY.toString()))
		{
			shortRefName = extendedRefName.replace(" - " + VariableType.GLOBAL_QUANTITY.toString(), "");
			vType = VariableType.GLOBAL_QUANTITY;
		}
		else
		{
			System.err.println("AC_GUI.visibleVariableValidation: A valid VariableType was not found.");
		}
		
		if (moduleDefinition.checkDisplayedVariableRefName(extendedRefName))
		{
			String message = vType + " " + shortRefName;
			message += " is already visible." + eol;
			message += "Cannot show the same " + vType.toString().toLowerCase() + " multiple times.";
			//JOptionPane.showMessageDialog(null, message);
			JOptionPane.showMessageDialog(null, message, "Invalid Operation", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		return true;
	}
	
	public static boolean portValidation(Module module, String portName, String refName)
	{
		return portNameValidation(portName, module) && portRefNameValidation(refName, module);
	}
	
	public static boolean portRefNameValidation(String extendedRefName, Module module)
	{
		VariableType vType = null;
		ModuleDefinition moduleDefinition = module.getModuleDefinition();
		String shortRefName = "";
		
		// trim the refName
		if (extendedRefName.endsWith(VariableType.SPECIES.toString()))
		{
			shortRefName = extendedRefName.replace(" - " + VariableType.SPECIES.toString(), "");
			vType = VariableType.SPECIES;
		}
		else if (extendedRefName.endsWith(VariableType.GLOBAL_QUANTITY.toString()))
		{
			shortRefName = extendedRefName.replace(" - " + VariableType.GLOBAL_QUANTITY.toString(), "");
			vType = VariableType.GLOBAL_QUANTITY;
		}
		else
		{
			System.err.println("AC_Utility:portRefNameValidation: A valid VariableType was not found.");
		}
		
		if (moduleDefinition.checkPortRefName(extendedRefName))
		{
			String message = vType + " " + shortRefName;
			message += " is already associated with a Port." + eol;
			message += "Cannot associate the same " + vType.toString().toLowerCase() + " with multiple Ports.";
			//JOptionPane.showMessageDialog(null, message);
			JOptionPane.showMessageDialog(null, message, "Invalid Name", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		return true;
	}
	
	public static boolean portNameValidation(String portName, Module module)
	{
		ModuleDefinition pModuleDefinition = module.getModuleDefinition();
		
		if (pModuleDefinition.checkPortName(portName))
		{
			String message = "\"" + portName + "\"";
			message += " is already the name of a Port." + eol;
			message += "Cannot assign the same name to multiple Ports.";
			//JOptionPane.showMessageDialog(null, message);
			JOptionPane.showMessageDialog(null, message, "Invalid Name", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		return true;
	}
	
	public static PortNode findPortMatch(String refName, VariableType vType)
	{
		PortNode currentPort;
		ListIterator<ACComponentNode> list = AC_GUI.activeModule.getPorts().listIterator();
		while (list.hasNext())
		{
			currentPort = (PortNode)list.next();
			if ((currentPort.getPortDefinition().getVariableType() == vType) && (currentPort.getPortDefinition().getRefName().equals(refName)))
			{
				return currentPort;
			}
		}
		return null;
	}
	
	public static boolean speciesInModelBuilder(String name)
	{
		return AC_GUI.modelBuilder.isSpeciesName(name);
	}
	
	public static void changeName(ChangedElement beforeE, ChangedElement afterE)
	{
		if (beforeE == null || afterE == null)
		{
			return;
		}
		
		boolean changeRequired = false;
		String before = beforeE.getName();
		String after = afterE.getName();
		
		ListIterator<ACComponentNode> ports = AC_GUI.activeModule.getPorts().listIterator();
		PortNode currentPort;
		while(ports.hasNext())
		{
			currentPort = (PortNode)ports.next();
			if (before.equalsIgnoreCase(currentPort.getPortDefinition().getRefName()))
			{
				currentPort.getPortDefinition().setRefName(after);
				changeRequired = true;
			}
		}
		
		ListIterator<ACComponentNode> vars = AC_GUI.activeModule.getVisibleVariables().listIterator();
		VisibleVariableNode currentVar;
		while(vars.hasNext())
		{
			currentVar = (VisibleVariableNode)vars.next();
			if (before.equalsIgnoreCase(currentVar.getVisibleVariableDefinition().getRefName()))
			{
				currentVar.getVisibleVariableDefinition().setRefName(after);
				changeRequired = true;
			}
		}
		
		ListIterator<ACComponentNode> eNodes = AC_GUI.activeModule.getEquivalences().listIterator();
		EquivalenceNode currenteNode;
		while(eNodes.hasNext())
		{
			currenteNode = (EquivalenceNode)eNodes.next();
			if (before.equalsIgnoreCase(currenteNode.getEquivalenceDefinition().getRefName()))
			{
				currenteNode.getEquivalenceDefinition().setRefName(after);
				changeRequired = true;
			}
		}
		
		if(changeRequired)
		{
			//changeActiveModule(activeModule);
			AC_GUI.drawingBoard.changeModule(AC_GUI.activeModule);
			AC_GUI.modelBuilder.updatePorts();
			AC_GUI.setSavedInACFile(false);
		}
	}
	
	public static void changeModuleName(Module module, String newName, boolean fromModelBuilder)
	{
		//mod.getModuleDefinition().setName(newName);
		String oldName = module.getName();
		module.setName(newName);
		AC_GUI.treeView.refreshTree();
		AC_GUI.drawingBoard.getGraph().refresh();
		if (module == AC_GUI.activeModule)
		{
			if (!fromModelBuilder)
			{
				AC_GUI.modelBuilder.setModuleName(newName);
			}
		}
		moduleNameList.remove(oldName);
		moduleNameList.add(newName);
		AC_GUI.setSavedInACFile(false);
	}
	
	public static void changeModuleDefinitionName(ModuleDefinition definition, String newName, boolean fromModelBuilder)
	{
		String oldName = definition.getName();
		definition.setName(newName);
		if (!fromModelBuilder)
		{
			AC_GUI.modelBuilder.setModuleDefinitionName(newName);
		}
		CCopasiDataModel dataModel = CopasiUtility.getCopasiModelFromModelName(oldName);
		if (dataModel == null)
		{
			System.err.println("AC_Utility.changeModuleDefinitionName: copasi datamodel not found.");
		}
		else
		{
			dataModel.getModel().setObjectName(newName);
		}
		moduleNameList.remove(oldName);
		moduleNameList.add(newName);
		moduleDefinitionNameSet.remove(oldName);
		moduleDefinitionNameSet.add(newName);
		AC_GUI.setSavedInACFile(false);
	}
	
	/**
	 * Add a representation of the given Module to the TreeView panel.
	 * @param module the Module to add
	 */
	public static void addTreeNode(Module module)
	{
		AC_GUI.treeView.addNode(module);
		if (module.getChildren().size() != 0)
		{
			ListIterator<Module> children = module.getChildren().listIterator();
			while (children.hasNext())
			{
				addTreeNode(children.next());
			}
		}
	}
	
	public static void addSubmoduleDefinitionsToList(Module module)
	{
		ModuleDefinition definition = module.getModuleDefinition();
		if ((module != AC_GUI.rootModule) && (module.getChildren().size() == 0))
		{
			if (!isSubmoduleDefinition(definition))
			{
				addSubmoduleDefinition(definition);
			}
			return;
		}
		
		ListIterator<Module> list = module.getChildren().listIterator();
		while(list.hasNext())
		{
			addSubmoduleDefinitionsToList(list.next());
		}
	}
	
	public static boolean addSubmoduleDefinition(ModuleDefinition definition)
	{
		if (submoduleList.contains(definition))
		{
			return false;
		}
		return submoduleList.add(definition);
	}
	
	public static boolean removeSubmoduleDefinition(ModuleDefinition definition)
	{
		if (!submoduleList.contains(definition))
		{
			return false;
		}
		return submoduleList.remove(definition);
	}
	
	public static boolean isSubmoduleDefinition(ModuleDefinition definition)
	{
		return submoduleList.contains(definition);
	}
	
	public static ModuleDefinition getSubmoduleDefinition(String name)
	{
		ModuleDefinition definition;
		ListIterator<ModuleDefinition> list = submoduleList.listIterator();
		while (list.hasNext())
		{
			definition = list.next();
			if (definition.getName().equals(name))
			{
				return definition;
			}
		}
		return null;
	}
	
	public static ArrayList<ModuleDefinition> getSubmoduleList()
	{
		return submoduleList;
	}
	
	public static void reset()
	{
		submoduleList.clear();
		moduleNameList.clear();
		moduleDefinitionNameSet.clear();
	}
	
	public static boolean createUniqueIDs(Module module)
	{
		id_index = 0;
		try
		{
			setModuleDefinitionID(module.getModuleDefinition());
			setModuleID(module);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static void validateAllModules()
	{
		ArrayList<String> definitionList = new ArrayList<String>();
		String columns = "Module:\t\tIssues:" + eol;
		String result = "";
		String message;
		
		// save the active module MSMB data
		byte[] currentMSMBData = AC_GUI.modelBuilder.saveModel();
		AC_GUI.activeModule.getModuleDefinition().setMSMBData(currentMSMBData);
		
		// load and save new Module data
		result += validateModule(AC_GUI.rootModule);
		message = columns + result;
		System.out.println(message);
		//reload the active module MSMB data
		AC_GUI.modelBuilder.loadModel(currentMSMBData, AC_GUI.activeModule);
		AC_GUI.modelBuilder.updatePorts();
		
		JOptionPane.showMessageDialog(null,
			    new JTextArea(message),
			    "Module Validation",
			    JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static String validateModule(Module module)
	{
		String moduleName = module.getName();
		String definitionName = module.getModuleDefinition().getName();
		int validStatus = -1;
		String status = "";
		String modelInfo = "";
		AC_GUI.modelBuilder.loadModel(module.getModuleDefinition().getMSMBData(), module);
		validStatus = AC_GUI.modelBuilder.validateModel();
		
		String msg = "Module \"" + moduleName + "\", Definition \"" + definitionName + "\"";
		msg += " valid status: " + validStatus;
		
		switch (validStatus)
		{
			case 0:
				status = "none";
				break;
			case 1:
				status = "major";
				break;
			case 2:
				status = "minor";
				break;
		}
		
		modelInfo += moduleName + "\t\t" + status + eol;
		
		System.out.println(msg);
		if (module.getChildren().size() == 0)
		{
			return modelInfo;
		}
		else
		{
			ListIterator<Module> list = module.getChildren().listIterator();
			while(list.hasNext())
			{
				modelInfo += validateModule(list.next());
			}
			return modelInfo;
		}
	}
	
	public static void validateModuleDefinition(ModuleDefinition definition)
	{
		
	}
	
	public static boolean validateExternalFile(String externalSource, String md5)
	{
		String newmd5;
		
		try
		{
			newmd5 = CheckSumGenerator.generate(externalSource);
		}
		catch (Exception e)
		{
			System.err.println("AC_Utility.validateExternalFile: CheckSumGenerator failed.");
			return false;
		}
		
		System.out.println("oldmd5: " + md5);
		System.out.println("newmd5: " + newmd5);
		System.out.println("External file valid? " + md5.equals(newmd5));
		if (md5.equals(newmd5))
		{
			return true;
		}
		
		return false;
	}
	
	public static void printModuleTree()
	{
		Module root = AC_GUI.rootModule;
		if (root == null)
		{
			return;
		}
		ModuleDefinition rootDefinition = root.getModuleDefinition();
		StringBuilder output = new StringBuilder();
		// get the ModuleDefinition structure
		output.append(eol);
		output.append("------module definition structure start------" + eol);
		output.append(rootDefinition.getName() + " (root definition)" + eol);
		getModuleDefinitionStructure(rootDefinition, 0, true, output);
		output.append("------module definition structure end------" + eol);
		
		output.append(eol);
		// get the Module Structure
		output.append("------module structure start------" + eol);
		output.append(root.getName() + " (root module)" + eol);
		getModuleStructure(root, 0, output);
		output.append("------module structure end------" + eol);
		output.append(eol);
		// print the structures
		System.out.println(output.toString());
		CopasiUtility.printDataModelList();
	}
	
	public static void displayMessage(int type, String title, String message)
	{
		JOptionPane.showMessageDialog(null,
			    message,
			    title,
			    type);
	}
	
	private static void getModuleDefinitionStructure(ModuleDefinition definition, int level, boolean includeInstances, StringBuilder msg)
	{
		if (definition.getChildren().size() == 0)
		{
			return;
		}
		
		ListIterator<ModuleDefinition> list = definition.getChildren().listIterator();
		String spacer = "";
		for (int i = 0; i < level; i++)
		{
			spacer += " ";
		}
		
		ModuleDefinition currentDefinition;
		while (list.hasNext())
		{
			currentDefinition = list.next();
			msg.append(spacer);
			msg.append("-");
			msg.append(currentDefinition.getName());
			msg.append(eol);
			msg.append("External: " + currentDefinition.isExternal() + eol);
			if (includeInstances)
			{
				getModuleInstances(currentDefinition, level+2, msg);
			}
			getModuleDefinitionStructure(currentDefinition, level+4, includeInstances, msg);
		}
	}
	
	private static void getModuleInstances(ModuleDefinition definition, int level, StringBuilder msg)
	{
		if (definition.getInstances().size() == 0)
		{
			return;
		}
		
		ListIterator<Module> list = definition.getInstances().listIterator();
		String spacer = "";
		for (int i = 0; i < level; i++)
		{
			spacer += " ";
		}
		
		msg.append(spacer);
		msg.append("Instantiations: ");
		Module currentModule;
		while (list.hasNext())
		{
			currentModule = list.next();
			msg.append(currentModule.getName());
			msg.append(" ");
		}
		msg.append(eol);
	}
	
	private static void getModuleStructure(Module module, int level, StringBuilder msg)
	{
		if (module.getChildren().size() == 0)
		{
			return;
		}
		
		ListIterator<Module> list = module.getChildren().listIterator();
		String spacer = "";
		for (int i = 0; i < level; i++)
		{
			spacer += " ";
		}
		
		Module currentModule;
		while (list.hasNext())
		{
			currentModule = list.next();
			msg.append(spacer);
			msg.append("-");
			msg.append(currentModule.getName());
			msg.append(eol);
			getModuleStructure(currentModule, level+4, msg);
		}
	}
	
	private static void setModuleDefinitionID(ModuleDefinition definition)
	{
		String prefix = "Module_";
		String id = prefix + id_index;
		definition.setID(id);
		id_index++;
		//System.out.println("ModuleDefinition name: " + definition.getName() + ", id: " + definition.getID());
		
		if (definition.getChildren().size() > 0)
		{
			ListIterator<ModuleDefinition> list = definition.getChildren().listIterator();
			while(list.hasNext())
			{
				setModuleDefinitionID(list.next());
			}
		}
	}
	
	private static void setModuleID(Module module)
	{
		String prefix = "Module_";
		String id = prefix + id_index;
		//module.setID(id);
		module.setID(module.getName());
		id_index++;
		//System.out.println("Module name: " + module.getName() + ", id: " + module.getID());
		
		if (module.getChildren().size() > 0)
		{
			ListIterator<Module> list = module.getChildren().listIterator();
			while(list.hasNext())
			{
				setModuleID(list.next());
			}
		}
	}
	
	/**
	 * Create a Module Definition with the given name.
	 * @param name the name of the Module Definition
	 * @param createDatamodel flag to create a new Copasi data model
	 * @return the created Module Definition
	 */
	private static ModuleDefinition createModuleDefinition(String name, boolean createDatamodel)
	{
		String definitionName = checkModuleDefinitionName(name);
		ModuleDefinition mDefinition = new ModuleDefinition(definitionName);
		if (createDatamodel)
		{
			CCopasiDataModel dataModel = CopasiUtility.createDataModel();
			dataModel.getModel().setObjectName(definitionName);
		}
		
		moduleNameList.add(definitionName);
		moduleDefinitionNameSet.add(definitionName);
		return mDefinition;
	}
	
	/**
	 * Create a Module Definition with the given name.
	 * @param name the name of the Module Definition
	 * @param createDatamodel flag to create a new Copasi data model
	 * @return the created Module Definition
	 */
	private static ModuleDefinition createModuleDefinition(String name, ModuleDefinition parent, boolean createDatamodel)
	{
		String definitionName = checkModuleDefinitionName(name);
		ModuleDefinition mDefinition = new ModuleDefinition(definitionName, parent);
		if (createDatamodel)
		{
			CCopasiDataModel dataModel = CopasiUtility.createDataModel();
			dataModel.getModel().setObjectName(definitionName);
		}
		parent.addChild(mDefinition);
		moduleNameList.add(definitionName);
		moduleDefinitionNameSet.add(definitionName);
		return mDefinition;
	}
	
	/**
	 * Create a Module Definition with the given name, parent, and msmb data.
	 * @param name the name of the Module Definition
	 * @param parent the parent of the Module Definition
	 * @param msmbData the msmb data of the Module Definition
	 * @param createDatamodel flag to create a new Copasi data model
	 * @return the created Module Definition
	 */
	public static ModuleDefinition createModuleDefinition(String name, ModuleDefinition parent, byte[] msmbData, boolean createDatamodel)
	{
		String definitionName = checkModuleDefinitionName(name);
		ModuleDefinition mDefinition = new ModuleDefinition(definitionName, parent, msmbData);
		if (createDatamodel)
		{
			CCopasiDataModel dataModel = CopasiUtility.createDataModel();
			dataModel.getModel().setObjectName(definitionName);
		}
		if (parent != null)
		{
			parent.addChild(mDefinition);
		}
		moduleNameList.add(definitionName);
		moduleDefinitionNameSet.add(definitionName);
		return mDefinition;
	}
	
	public static ModuleDefinition createModuleDefinition(String sbmlID, String name, String copasiData, ModuleDefinition parent)
	{
		String definitionName = checkModuleDefinitionName(name);
		CCopasiDataModel dataModel = CopasiUtility.createDataModel(copasiData);
		if (dataModel == null)
		{
			return null;
		}
		dataModel.getModel().setObjectName(definitionName);
		ModuleDefinition mDefinition = new ModuleDefinition(definitionName, parent);
		mDefinition.setID(sbmlID);
		if (parent != null)
		{
			parent.addChild(mDefinition);
		}
		moduleNameList.add(definitionName);
		moduleDefinitionNameSet.add(definitionName);
		return mDefinition;
	}
	
	private static MathematicalAggregatorDefinition createMathematicalAggregatorDefinition(String name, ModuleDefinition parent, int terms, Operation op, boolean createDatamodel)
	{
		String definitionName = checkModuleDefinitionName(name);
		if (createDatamodel)
		{
			CCopasiDataModel dataModel = CopasiUtility.createDataModel();
			dataModel.getModel().setObjectName(definitionName);
		}
		MathematicalAggregatorDefinition maDefinition = new MathematicalAggregatorDefinition(definitionName, parent, terms, op);
		if (parent != null)
		{
			parent.addChild(maDefinition);
		}
		moduleNameList.add(definitionName);
		moduleDefinitionNameSet.add(definitionName);
		return maDefinition;
	}
	
	public static MathematicalAggregatorDefinition createMathematicalAggregatorDefinition(String sbmlID, String name, String copasiData, int terms, Operation op, ModuleDefinition parent)
	{
		String definitionName = checkModuleDefinitionName(name);
		CCopasiDataModel dataModel = CopasiUtility.createDataModel(copasiData);
		if (dataModel == null)
		{
			return null;
		}
		dataModel.getModel().setObjectName(definitionName);
		MathematicalAggregatorDefinition maDefinition = new MathematicalAggregatorDefinition(definitionName, sbmlID, parent, terms, op);
		parent.addChild(maDefinition);
		moduleNameList.add(definitionName);
		moduleDefinitionNameSet.add(definitionName);
		return maDefinition;
	}
	
	public static MathematicalAggregatorDefinition createMathematicalAggregatorDefinition(String name, ModuleDefinition parent, byte[] msmbData, int terms, Operation op, boolean createDatamodel)
	{
		String definitionName = checkModuleDefinitionName(name);
		if (createDatamodel)
		{
			CCopasiDataModel dataModel = CopasiUtility.createDataModel();
			dataModel.getModel().setObjectName(definitionName);
		}
		MathematicalAggregatorDefinition maDefinition = new MathematicalAggregatorDefinition(definitionName, parent, msmbData, terms, op);
		parent.addChild(maDefinition);
		moduleNameList.add(definitionName);
		moduleDefinitionNameSet.add(definitionName);
		return maDefinition;
	}
	
	private static void deleteModuleDefinition(ModuleDefinition definition)
	{
		if (definition.getParent() != null)
		{
			definition.getParent().removeChild(definition);
		}
		System.out.println("Number of Copasi data models: " + CopasiUtility.getNumberOfModels());
		if (!CopasiUtility.removeDataModel(definition.getName()))
		{
			// the Copasi data model was not successfully removed
			String message = "The module " + definition.getName() + " was not completely removed.";
			//displayErrorMessage("The module " + definition.getName() + " was not completely removed.");
			displayMessage(JOptionPane.WARNING_MESSAGE, "Remove Module", message);
		}
		moduleNameList.remove(definition.getName());
		moduleDefinitionNameSet.remove(definition.getName());
		removeSubmoduleDefinition(definition);
		System.out.println("Number of Copasi data models: " + CopasiUtility.getNumberOfModels());
	}
	
	private static ConnectionDefinition createConnectionDefinition(ModuleDefinition parent, ACComponentDefinition sourceDefinition, TerminalType sourceType, ACComponentDefinition targetDefinition, TerminalType targetType)
	{
		ConnectionDefinition cDefinition = new ConnectionDefinition(parent, sourceDefinition, sourceType, targetDefinition, targetType);
		parent.addConnection(cDefinition);
		return cDefinition;
	}
	
	private static void deleteConnectionDefinition(ConnectionDefinition cDefinition)
	{
		cDefinition.getParent().removeConnection(cDefinition);
	}
	
	public static EquivalenceDefinition createEquivalenceDefinition(ModuleDefinition parent, String refName, VariableType varType)
	{
		EquivalenceDefinition eDefinition = new EquivalenceDefinition(parent, refName, varType);
		parent.addEquivalence(eDefinition);
		return eDefinition;
	}
	
	private static void deleteEquivalenceDefinition(EquivalenceDefinition eDefinition)
	{
		eDefinition.getParent().removeEquivalence(eDefinition);
	}
	
	private static PortDefinition createPortDefinition(ModuleDefinition parent, String iRefName, String iName, PortType iPortType, VariableType iVariableType)
	{
		PortDefinition pDefinition = new PortDefinition(parent, iRefName, iName, iPortType, iVariableType);
		parent.addPort(pDefinition);
		return pDefinition;
	}
	
	private static void deletePortDefinition(PortDefinition pDefinition)
	{
		pDefinition.getParent().removePort(pDefinition);
	}
	
	public static VisibleVariableDefinition createVisibleVariableDefinition(ModuleDefinition parent, String refName, VariableType varType)
	{
		VisibleVariableDefinition vDefinition = new VisibleVariableDefinition(parent, refName, varType);
		parent.addVisibleVariable(vDefinition);
		return vDefinition;
	}
	
	private static void deleteVisibleVariableDefinition(VisibleVariableDefinition vDefinition)
	{
		vDefinition.getParent().removeVisibleVariable(vDefinition);
	}
	
	private static void setMSMBData(Module module)
	{
		ModuleDefinition mDefinition = module.getModuleDefinition();
		if (mDefinition.getMSMBData() == null)
		{
			if (AC_GUI.modelBuilder.getLoadedModule() == null)
			{
				AC_GUI.modelBuilder.loadModel(module, false, false, false);
				//mDefinition.setMSMBData(new String(AC_GUI.modelBuilder.saveModel()));
				mDefinition.setMSMBData(AC_GUI.modelBuilder.saveModel());
			}
			else
			{
				// save the active module MSMB data
				byte[] currentMSMBData = AC_GUI.modelBuilder.saveModel();
				// load and save new Module data
				AC_GUI.modelBuilder.loadModel(module, false, false, false);
				mDefinition.setMSMBData(AC_GUI.modelBuilder.saveModel());
				//reload the active module MSMB data
				AC_GUI.modelBuilder.loadModel(currentMSMBData, AC_GUI.activeModule);
				AC_GUI.modelBuilder.updatePorts();
			}			
		}
	}
	
	private static void createMathematicalAggregatorPorts(Module maModule)
	{
		MathematicalAggregatorDefinition maDefinition = (MathematicalAggregatorDefinition)maModule.getModuleDefinition();
		int inputs = maDefinition.getNumberofInputs();
		String inputPrefix = maDefinition.getInputPrefix();
		String outputName = maDefinition.getOutputName();

		PortNode newPNode;
		
		// create the input ports
		for(int i = 0; i < inputs; i++)
		{
			//newPNode = new PortNode(maModule, inputPrefix+i, PortType.INPUT, VariableType.GLOBAL_QUANTITY, inputPrefix+i+"Port");
			newPNode = createPort(maModule, inputPrefix+i, inputPrefix+i+"Port", PortType.INPUT, VariableType.GLOBAL_QUANTITY);
			//modelBuilder.addPort(newPNode);
		}
		
		// create the output port
		//newPNode = new PortNode(maModule, outputName, PortType.OUTPUT, VariableType.GLOBAL_QUANTITY, "TotalPort", mathAgg.getPorts().size());
		newPNode = createPort(maModule, outputName, "TotalPort", PortType.OUTPUT, VariableType.GLOBAL_QUANTITY);
		//modelBuilder.addPort(newPNode);
	}
	
	private static ACComponentNode getTerminalNode(Module terminalParent, TerminalType type, String refName)
	{		
		switch (type)
		{
			case EQUIVALENCE:
				return getEquivalenceNode(terminalParent, refName);
			case PORT:
				return getPortNode(terminalParent, refName);
			case VISIBLEVARIABLE:
				return getVisibleVariableNode(terminalParent, refName);
			default:
				// there is an error
				System.err.println("getTerminalNode: " + type + " is not a valid TerminalType.");
				return null;
		}
	}
	
	private static ACComponentNode getPortNode(Module parent, String refName)
	{
		ListIterator<ACComponentNode> list = parent.getPorts().listIterator();
		PortNode currentNode;
		while (list.hasNext())
		{
			currentNode = (PortNode)list.next();
			if (currentNode.getPortDefinition().getRefName().equals(refName))
			{
				return currentNode;
			}
		}
		return null;
	}
	
	private static ACComponentNode getEquivalenceNode(Module parent, String refName)
	{
		ListIterator<ACComponentNode> list = parent.getEquivalences().listIterator();
		EquivalenceNode currentNode;
		while (list.hasNext())
		{
			currentNode = (EquivalenceNode)list.next();
			if (currentNode.getEquivalenceDefinition().getRefName().equals(refName))
			{
				return currentNode;
			}
		}
		return null;
	}
	
	private static ACComponentNode getVisibleVariableNode(Module parent, String refName)
	{
		ListIterator<ACComponentNode> list = parent.getVisibleVariables().listIterator();
		VisibleVariableNode currentNode;
		while (list.hasNext())
		{
			currentNode = (VisibleVariableNode)list.next();
			if (currentNode.getVisibleVariableDefinition().getRefName().equals(refName))
			{
				return currentNode;
			}
		}
		return null;
	}
	
	private static ConnectionNode getConnectionNode(Module parent, ConnectionDefinition definition)
	{
		ListIterator<ConnectionNode> connectionList = parent.getConnections().listIterator();
		ConnectionNode node;
		while (connectionList.hasNext())
		{
            node = connectionList.next();
			if (node.getConnectionDefinition() == definition)
			{
				return node;
			}
		}
		return null;
	}
	
	private static ACComponentDefinition getACComponentDefinition(ArrayList<ACComponentDefinition> list, String refName)
	{
		ListIterator<ACComponentDefinition> iterator = list.listIterator();
		ACComponentDefinition currentDefinition;
		while (iterator.hasNext())
		{
			currentDefinition = iterator.next();
			if (currentDefinition.getRefName().equals(refName))
			{
				return currentDefinition;
			}
		}
		return null;
	}
	
	private static ConnectionDefinition findConnectionDefinition(ArrayList<ConnectionDefinition> list, ConnectionDefinition oldDefinition)
	{
		ListIterator<ConnectionDefinition> definitionIterator = list.listIterator();
		ConnectionDefinition possibleMatch;
		TerminalType sourceType = oldDefinition.getSourceType();
		TerminalType targetType = oldDefinition.getTargetType();
		String sourceRefName = oldDefinition.getSourceDefinition().getRefName();
		String targetRefName = oldDefinition.getTargetDefinition().getRefName();
		
		while (definitionIterator.hasNext())
		{
			possibleMatch = definitionIterator.next();
			
			if ((sourceType == possibleMatch.getSourceType()) && (targetType == possibleMatch.getTargetType()))
			{
				if ((sourceRefName.equals(possibleMatch.getSourceDefinition().getRefName())) && 
						(targetRefName.equals(possibleMatch.getTargetDefinition().getRefName())))
				{
					return possibleMatch;
				}
			}
		}
		return null;
	}
	
	/*
	private static ACComponentDefinition getPortDefinition(ModuleDefinition parent, String refName)
	{
		ListIterator<ACComponentDefinition> list = parent.getPorts().listIterator();
		PortDefinition currentDefinition;
		while (list.hasNext())
		{
			currentDefinition = (PortDefinition)list.next();
			if (currentDefinition.getRefName().equals(refName))
			{
				return currentDefinition;
			}
		}
		return null;
	}
	
	private static ACComponentDefinition getEquivalenceDefinition(ModuleDefinition parent, String refName)
	{
		ListIterator<ACComponentDefinition> list = parent.getEquivalences().listIterator();
		EquivalenceDefinition currentDefinition;
		while (list.hasNext())
		{
			currentDefinition = (EquivalenceDefinition)list.next();
			if (currentDefinition.getRefName().equals(name))
			{
				return currentDefinition;
			}
		}
		return null;
	}
	
	private static ACComponentDefinition getVisibleVariableDefinition(ModuleDefinition parent, String name)
	{
		ListIterator<ACComponentDefinition> list = parent.getVisibleVariables().listIterator();
		VisibleVariableDefinition currentDefinition;
		while (list.hasNext())
		{
			currentDefinition = (VisibleVariableDefinition)list.next();
			if (currentDefinition.getRefName().equals(name))
			{
				return currentDefinition;
			}
		}
		return null;
	}
	*/
	
	private static boolean sbmlNameValidation(String name, boolean displayMessage)
	{
		boolean valid = true;
		String first = name.substring(0, 1);
		
		if(Pattern.matches("\\d", first))
		{
			valid = false;
		}
		
		if(name.matches(".*\\s.*"))
		{
			valid = false;
		}
		
		if(Pattern.matches("\\p{Punct}", first))
		{
			valid = false;
		}
		
		if(name.toLowerCase().startsWith("xml"))
		{
			valid = false;
		}
		
		if (!valid && displayMessage)
		{
			String message = "Names must adhere to the following rules:" + eol;
			message += "\u2022 Names cannot start with a number or punctuation character." + eol;
			message += "\u2022 Names cannot start with the letters \"xml\"." + eol;
			message += "\u2022 Names cannot contain spaces." + eol;
			message += "The name \"" + name + "\" is invalid." + eol;
			message += "Please enter a different name.";
			//JOptionPane.showMessageDialog(null, message);
			JOptionPane.showMessageDialog(null, message, "Invalid Name", JOptionPane.WARNING_MESSAGE);
		}
		
		return valid;
	}
	
	private static String checkModuleDefinitionName(String iName)
	{
		String name = iName;
		if ((name == null) || name.isEmpty())
		{
			name = generateModuleDefinitionName();
		}
		return name;
	}
	
	private static String generateModuleDefinitionName()
	{
		int index = 0;
		String candidate = "ModuleTemplate_" + index;
		while (moduleDefinitionNameSet.contains(candidate))
		{
			index++;
			candidate = "ModuleTemplate_" + index;
		}
		return candidate;
	}
	
	private static ModuleDefinition copyModuleDefinition(Module module, String newName)
	{
		ModuleDefinition oldDefinition = module.getModuleDefinition();
		ModuleDefinition newDefinition = new ModuleDefinition(newName, oldDefinition.getParent());
		
		oldDefinition.getParent().addChild(newDefinition);
		
		ListIterator<ACComponentDefinition> list = oldDefinition.getPorts().listIterator();
		PortDefinition port;
		// copy port definitions
		while (list.hasNext())
		{
			port = copyPortDefinition((PortDefinition)list.next(), newDefinition);
			newDefinition.addPort(port);
		}
		// sync port nodes to port definitions
		syncPorts(module.getPorts(), newDefinition.getPorts());
		
		list = oldDefinition.getVisibleVariables().listIterator();
		VisibleVariableDefinition variable;
		// copy visible variable definitions
		while (list.hasNext())
		{
			variable = copyVisibleVariableDefinition((VisibleVariableDefinition)list.next(), newDefinition);
			newDefinition.addVisibleVariable(variable);
		}
		// sync visible variable nodes to visible variable definitions
		syncVisibleVariables(module.getVisibleVariables(), newDefinition.getVisibleVariables());
		
		list = oldDefinition.getEquivalences().listIterator();
		EquivalenceDefinition equivalence;
		// copy equivalence definitions
		while (list.hasNext())
		{
			equivalence = copyEquivalenceDefinition((EquivalenceDefinition)list.next(), newDefinition);
			newDefinition.addEquivalence(equivalence);
		}
		// sync equivalence nodes to equivalence definitions
		syncEquivalences(module.getEquivalences(), newDefinition.getEquivalences());
		
		ListIterator<ConnectionDefinition> connectionList = oldDefinition.getConnections().listIterator();
		ConnectionDefinition connection;
		// copy connection definitions
		while (connectionList.hasNext())
		{
			connection = copyConnectionDefinition(connectionList.next(), newDefinition);
			newDefinition.addConnection(connection);
		}
		// sync connection nodes to connection definitions
		syncConnections(module.getConnections(), newDefinition.getConnections());
		moduleNameList.add(newName);
		moduleDefinitionNameSet.add(newName);
		newDefinition.setExternal(false);
		return newDefinition;
	}
	
	private static ACComponentDefinition copyACComponentDefinition(ACComponentDefinition oldDefinition, ModuleDefinition parent)
	{
		ACComponentDefinition newDefinition = new ACComponentDefinition(parent);
		
		newDefinition.setName(new String(oldDefinition.getName()));
		newDefinition.setRefName(new String(oldDefinition.getRefName()));
		
		return newDefinition;
	}
	
	private static PortDefinition copyPortDefinition(PortDefinition oldDefinition, ModuleDefinition parent)
	{
		//PortDefinition newDefinition = copyACComponentDefinition(oldDefinition, parent);
		PortDefinition newDefinition = new PortDefinition(parent);
		newDefinition.setName(new String(oldDefinition.getName()));
		newDefinition.setRefName(new String(oldDefinition.getRefName()));
		newDefinition.setType(oldDefinition.getType());
		newDefinition.setVariableType(oldDefinition.getVariableType());
		
		return newDefinition;
	}
	
	private static VisibleVariableDefinition copyVisibleVariableDefinition(VisibleVariableDefinition oldDefinition, ModuleDefinition parent)
	{
		//VisibleVariableDefinition newDefinition = (VisibleVariableDefinition)copyACComponentDefinition(oldDefinition, parent);
		VisibleVariableDefinition newDefinition = new VisibleVariableDefinition(parent);
		//newDefinition.setName(new String(oldDefinition.getName()));
		newDefinition.setRefName(new String(oldDefinition.getRefName()));
		newDefinition.setVariableType(oldDefinition.getVariableType());
		
		return newDefinition;
	}
	
	private static EquivalenceDefinition copyEquivalenceDefinition(EquivalenceDefinition oldDefinition, ModuleDefinition parent)
	{
		//EquivalenceDefinition newDefinition = (EquivalenceDefinition)copyACComponentDefinition(oldDefinition, parent);
		EquivalenceDefinition newDefinition = new EquivalenceDefinition(parent);
		//newDefinition.setName(new String(oldDefinition.getName()));
		newDefinition.setRefName(new String(oldDefinition.getRefName()));
		newDefinition.setVariableType(oldDefinition.getVariableType());
		
		return newDefinition;
	}
	
	private static ConnectionDefinition copyConnectionDefinition(ConnectionDefinition oldDefinition, ModuleDefinition parent)
	{
		ArrayList<ACComponentDefinition> list = null;
		ACComponentDefinition source = null;
		String sourceName = oldDefinition.getSourceDefinition().getRefName();
		TerminalType sourceType = oldDefinition.getSourceType();
		switch (sourceType)
		{
			case EQUIVALENCE:
				list = parent.getEquivalences();
				break;
			case PORT:
				list = parent.getPorts();
				break;
			case VISIBLEVARIABLE:
				list = parent.getVisibleVariables();
				break;
		}
		source = getACComponentDefinition(list, sourceName);
		
		ACComponentDefinition target = null;
		String targetName = oldDefinition.getTargetDefinition().getRefName();
		TerminalType targetType = oldDefinition.getTargetType();
		switch (targetType)
		{
			case EQUIVALENCE:
				list = parent.getEquivalences();
				break;
			case PORT:
				list = parent.getPorts();
				break;
			case VISIBLEVARIABLE:
				list = parent.getVisibleVariables();
				break;
		}
		target = getACComponentDefinition(list, targetName);
		
		// check if source or target are null
		if ((source == null) || (target == null))
		{
			if (source == null)
			{
				System.err.println("Error AC_Utility.copyConnectionDefinition(): source is null.");
			}
			
			if (target == null)
			{
				System.err.println("Error AC_Utility.copyConnectionDefinition(): target is null.");
			}
			return null;
		}
		return new ConnectionDefinition(parent, source, sourceType, target, targetType);
	}
	
	private static void syncPorts(ArrayList<ACComponentNode> nodeList, ArrayList<ACComponentDefinition> definitionList)
	{
		ListIterator<ACComponentNode> nodeIterator = nodeList.listIterator();
		PortNode node;
		PortDefinition definition;
		String refName;
		while (nodeIterator.hasNext())
		{
			definition = null;
			node = (PortNode)nodeIterator.next();
			refName = node.getPortDefinition().getRefName();
			definition = (PortDefinition)getACComponentDefinition(definitionList, refName);
			if (definition == null)
			{
				System.err.println("Error AC_Utility.syncPorts: definition is null.");
			}
			node.setPortDefinition(definition);
		}
	}
	
	private static void syncVisibleVariables(ArrayList<ACComponentNode> nodeList, ArrayList<ACComponentDefinition> definitionList)
	{
		ListIterator<ACComponentNode> nodeIterator = nodeList.listIterator();
		VisibleVariableNode node;
		VisibleVariableDefinition definition;
		String refName;
		while (nodeIterator.hasNext())
		{
			definition = null;
			node = (VisibleVariableNode)nodeIterator.next();
			refName = node.getVisibleVariableDefinition().getRefName();
			definition = (VisibleVariableDefinition)getACComponentDefinition(definitionList, refName);
			if (definition == null)
			{
				System.err.println("Error AC_Utility.syncVisibleVariables: definition is null.");
			}
			node.setVisibleVariableDefinition(definition);
		}
	}
	
	private static void syncEquivalences(ArrayList<ACComponentNode> nodeList, ArrayList<ACComponentDefinition> definitionList)
	{
		ListIterator<ACComponentNode> nodeIterator = nodeList.listIterator();
		EquivalenceNode node;
		EquivalenceDefinition definition;
		String refName;
		while (nodeIterator.hasNext())
		{
			definition = null;
			node = (EquivalenceNode)nodeIterator.next();
			refName = node.getEquivalenceDefinition().getRefName();
			definition = (EquivalenceDefinition)getACComponentDefinition(definitionList, refName);
			if (definition == null)
			{
				System.err.println("Error AC_Utility.syncEquivalences: definition is null.");
			}
			node.setEquivalenceDefinition(definition);
		}
	}
	
	private static void syncConnections(ArrayList<ConnectionNode> nodeList, ArrayList<ConnectionDefinition> definitionList)
	{
		ListIterator<ConnectionNode> nodeIterator = nodeList.listIterator();
		ConnectionNode node;
		ConnectionDefinition definition;
		while (nodeIterator.hasNext())
		{
			definition = null;
			node = nodeIterator.next();
			definition = findConnectionDefinition(definitionList, node.getConnectionDefinition());
			if (definition == null)
			{
				System.err.println("Error AC_Utility.syncConnections: definition is null.");
			}
			node.setConnectionDefinition(definition);
		}
	}
	
	private static void displayErrorMessage(String msg)
	{
		JOptionPane.showMessageDialog(null,
			    msg,
			    "Error",
			    JOptionPane.ERROR_MESSAGE);
	}
}
