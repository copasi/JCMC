package acgui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;

import com.mxgraph.model.mxCell;

import msmb.commonUtilities.ChangedElement;

import org.COPASI.*;

import org.sbml.libsbml.*;

/**
 * Aggregation Connector. This tool is used to connect SBML models together.
 * 
 * @author T.C. Jones
 * @version June 27, 2012
 */
public class AC_GUI extends JFrame
{
	private static final long serialVersionUID = 1L;

	protected static AC_GUI currentGUI;
	protected static DrawingBoard drawingBoard;
	protected static TreeView treeView;
	protected static ModelBuilder modelBuilder;
	protected static ModuleList masterModuleList;
	protected static Module activeModule;
	protected static Module selectedModule;
	protected static CopasiUtility copasiUtility;
	protected static Vector<ModuleTemplate> listOfTemplates;
	static String eol = System.getProperty("line.separator");

	/**
	 * Construct the AC_GUI object.
	 */
	public AC_GUI()
	{
		super("Aggregation Connector");
		//moduleList = new ModuleList();
		copasiUtility = new CopasiUtility();
		masterModuleList = new ModuleList();
		listOfTemplates = new Vector<ModuleTemplate>();
		activeModule = null;
		initializeComponents();
		this.setVisible(true);
	}

	/**
	 * Starts the tool.
	 * 
	 * @param args command line arguments
	 */
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			addLibraryPath("..\\lib");
			System.loadLibrary("sbmlj");
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}

		currentGUI = new AC_GUI();
		currentGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		currentGUI.setSize(900, 800);
		// make the frame full screen
		// currentGUI.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	/**
	* Adds the specified path to the java library path
	*
	* @param pathToAdd the path to add
	* @throws Exception
	*/
	public static void addLibraryPath(String pathToAdd) throws Exception{
		final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
		usrPathsField.setAccessible(true);
	 
		//get array of paths
		final String[] paths = (String[])usrPathsField.get(null);
	 
		//check if the path to add is already present
		for(String path : paths) {
			if(path.equals(pathToAdd)) {
				return;
			}
		}
	 
		//add the new path
		final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
		newPaths[newPaths.length-1] = pathToAdd;
		usrPathsField.set(null, newPaths);
	}
	
	public void load(String fileName)
	{
		CCopasiDataModel dataModel = null;
		Module mod = null;
		String ext = fileName.substring(fileName.lastIndexOf("."));
    	
		try
		{
			if (ext.equals(".xml"))
			{
				//dataModel.importSBML(fileName);
				mod = SBMLParser.importSBML(fileName);
			}
			else if (ext.equals(".cps"))
			{
				dataModel = copasiUtility.createDataModel();
				dataModel.loadModel(fileName);
			}
			else if (ext.equals(".ac"))
			{
				mod = AC_IO.loadModule(fileName);
			}
				//System.out.println("Number of models in the CCopasiRootContainer: " + CCopasiRootContainer.getDatamodelList().size());
		}
		catch (java.lang.Exception ex){
			ex.printStackTrace();
			System.err.println( "Error while importing the model from file named \"" + fileName + "\"." );
		}
		
		if (ext.equals(".cps"))
		{
			String modelName = dataModel.getModel().getObjectName();
			if (modelName == null || modelName.isEmpty())
			{
				String newName = JOptionPane.showInputDialog("Name of the loaded module:", "Module");
				newName = nameValidation(newName);
				dataModel.getModel().setObjectName(newName);
			}
			mod = new Module(dataModel.getModel().getObjectName(), dataModel.getModel().getKey());
			masterModuleList.add(mod);
			treeView.addNode(mod);
			drawingBoard.createCell(mod);
		}
		
		//drawingBoard.changeModule(mod);
		/*
		if (ext.equals(".cps") || ext.equals(".ac"))
		{
			changeActiveModule(mod);
			//modelBuilder.loadModel(mod.getKey());
			modelBuilder.setVisible(true);
			activeModule = mod;
		}
		*/
		changeActiveModule(mod);
		modelBuilder.setVisible(true);
		//activeModule = mod;
	}
	
	public void loadSubmodule(String fileName, Module parent)
	{
		CCopasiDataModel dataModel = null;
		Module mod = null;
		String ext = fileName.substring(fileName.lastIndexOf("."));
    	
		try
		{
			if (ext.equals(".xml"))
			{
				mod = SBMLParser.importSBML(fileName, parent);
			}
			else if (ext.equals(".cps"))
			{
				dataModel = copasiUtility.createDataModel();
				dataModel.loadModel(fileName);
			}
			else if (ext.equals(".ac"))
			{
				mod = AC_IO.loadModule(fileName, parent);
			}
				System.out.println("Number of models in the CCopasiRootContainer: " + CCopasiRootContainer.getDatamodelList().size());
		}
		catch (java.lang.Exception ex){
			ex.printStackTrace();
			System.err.println( "Error while importing the model from file named \"" + fileName + "\"." );
		}
		
		/*
		if (ext.equals(".xml") || ext.equals(".cps"))
		{
			dataModel.getModel().setObjectName(optName);
			mod = new Module(dataModel.getModel().getObjectName(), dataModel.getModel().getKey(), parent);
			mod.setDrawingCellStyle("Submodule");
			treeView.addNode(mod);
			drawingBoard.createCell(mod);
			drawingBoard.addCell(mod);
		}
		*/
		
		if (ext.equals(".cps"))
		{
			String modelName = dataModel.getModel().getObjectName();
			if (modelName == null || modelName.isEmpty())
			{
				String newName = JOptionPane.showInputDialog("Name of the loaded module:", "Module");
				newName = nameValidation(newName);
				dataModel.getModel().setObjectName(newName);
			}
			mod = new Module(dataModel.getModel().getObjectName(), dataModel.getModel().getKey(), parent);
			mod.setDrawingCellStyle("Submodule_No_Show_Information");
			treeView.addNode(mod);
			drawingBoard.createCell(mod);
			parent.addChild(mod);
		}
		
		changeActiveModule(activeModule);
	}
	
	public void save(String fileName)
	{
		/*
		SBMLParser sbmlParser = new SBMLParser();
		String key = selectedModule.getKey();
		sbmlParser.saveSBML(copasiUtility.getSBML(key), selectedModule, fileName);
		*/
		//copasiUtility.exportModel(key);
		String code = new String(modelBuilder.saveModel());
		if (code.isEmpty())
		{
			System.err.println("AC_GUI.save(): " + activeModule.getName() + "'s msmb data is NULL.");
		}
		activeModule.setMSMBData(code);
		drawingBoard.saveCurrentPositions();
		AC_IO.saveModule(activeModule, fileName);
	}
	
	public void loadTest(String fileName, Module parent)
	{
		Module mod = AC_IO.loadModule(fileName, parent);
		parent.addChild(mod);
		//CCopasiDataModel dataModel = copasiUtility.createDataModel();
		//dataModel.getModel().setObjectName(mod.getName());
		//mod.setKey(dataModel.getModel().getKey());
		//byte[] data = mod.getMSMBData().getBytes();
		//modelBuilder.loadModel(data);
		//treeView.addNode(mod);
		//drawingBoard.changeModule(mod);	
		//System.out.println(modelBuilder.saveToCK(mod.getKey()));
		changeActiveModule(activeModule);
		modelBuilder.setVisible(true);
		//activeModule = mod;
	}
	
	public static void exportSBML(String fileName)
	{
		System.out.println("Number of COPASI data models = " + copasiUtility.getNumberOfModels());
		/*
		if (!saveModules())
		{
			System.err.println("Problem saving Modules.");
		}
		System.out.println("Number of COPASI data models = " + copasiUtility.getNumberOfModels());
		*/
		System.out.println(copasiUtility.getSBML(activeModule.getKey()));
		if (!modelBuilder.saveToCK(activeModule.getKey()))
		{
			System.err.println("Problem saving Module.");
		}
		SBMLParser.exportSBML(activeModule, fileName);
	}
	
	/**
	 * Create a new module in the three panels.
	 * @param name the name of the new module
	 */
	public void newModule(String name)
	{
		CCopasiDataModel dataModel = copasiUtility.createDataModel();
		dataModel.getModel().setObjectName(name);
		Module mod = new Module(name, dataModel.getModel().getKey());
		mod.setDrawingCellStyle("Module");
		//System.out.println(name + " key = " + mod.getKey());
		masterModuleList.add(mod);
		//treeView.setup(mod);
		//treeView.setup();
		treeView.addNode(mod);
		drawingBoard.createCell(mod);
		drawingBoard.changeModule(mod);
		modelBuilder.loadModel(mod.getKey(), false, true);
		modelBuilder.setVisible(true);		
		
		activeModule = mod;
	}
	
	public Module newModule(String name, String sbmlID, String copasiData)
	{
		CCopasiDataModel dataModel = copasiUtility.createDataModel();
		dataModel.getModel().setObjectName(name);
		dataModel.getModel().setSBMLId(sbmlID);
		//dataModel.importSBMLFromString(copasiData);
		Module mod = new Module(name, dataModel.getModel().getKey());
		mod.setDrawingCellStyle("Module");
		//System.out.println(name + " key = " + mod.getKey());
		masterModuleList.add(mod);
		//treeView.setup(mod);
		//treeView.setup();
		treeView.addNode(mod);
		drawingBoard.createCell(mod);
		drawingBoard.changeModule(mod);
		modelBuilder.loadModel(mod.getKey(), false, true);
		modelBuilder.setVisible(true);		
		
		activeModule = mod;
		System.out.println("Module created. Name: " + name + "...SBMLid: " + sbmlID + "");
		return mod;
	}
	
	public static Module newModule(String name, String sbmlID, String copasiData, GeneralGlyph glyph)
	{
		CCopasiDataModel dataModel = copasiUtility.createDataModel();
		dataModel.getModel().setObjectName(name);
		dataModel.getModel().setSBMLId(sbmlID);
		
		try
		{
			dataModel.importSBMLFromString(copasiData);
		}
		catch (java.lang.Exception ex)
		{
			ex.printStackTrace();
		}
		//dataModel.importSBMLFromString(copasiData);
		Module mod = new Module(name, dataModel.getModel().getKey());
		mod.setDrawingCellStyle("Module");
		//System.out.println(name + " key = " + mod.getKey());
		masterModuleList.add(mod);
		//treeView.setup(mod);
		//treeView.setup();
		treeView.addNode(mod);
		drawingBoard.createCell(mod, glyph);
		drawingBoard.changeModule(mod);
		modelBuilder.loadModel(mod.getKey(), false, true);
		modelBuilder.setVisible(true);		
		
		activeModule = mod;
		System.out.println("Module created. Name: " + name + "...SBMLid: " + sbmlID + "");
		return mod;
	}
	
	public static Module xnewModule(String name, String sbmlID, String copasiData, Module parent, GeneralGlyph glyph, boolean isContainerModule)
	{
		CCopasiDataModel dataModel = copasiUtility.createDataModel();
		if (isContainerModule)
		{
			name = nameValidation(name);
		}
		
		//dataModel.getModel().setSBMLId(sbmlID);
		System.out.println();
		//System.out.println(copasiData);
		System.out.println();
		try
		{
			dataModel.importSBMLFromString(copasiData);
		}
		catch (java.lang.Exception ex)
		{
			ex.printStackTrace();
		}
		dataModel.getModel().setObjectName(name);
		//dataModel.importSBMLFromString(copasiData);
		Module mod = new Module(name, dataModel.getModel().getKey(), parent);
		if (parent == null)
		{
			mod.setDrawingCellStyle("Module");
			masterModuleList.add(mod);
		}
		else
		{
			mod.setDrawingCellStyle("Submodule_No_Show_Information");
			parent.addChild(mod);
		}
		
		//System.out.println(name + " key = " + mod.getKey());
		
		//treeView.setup(mod);
		//treeView.setup();
		treeView.addNode(mod);
		drawingBoard.createCell(mod, glyph);
		//drawingBoard.changeModule(mod);
		//modelBuilder.loadModel(mod.getKey(), true);
		//modelBuilder.setVisible(true);		
		
		//activeModule = mod;
		System.out.println("Module created. Name: " + name + "...SBMLid: " + mod.getKey() + "");
		System.out.println("Number of COPASI data models = " + copasiUtility.getNumberOfModels());
		return mod;
	}
	
	public static Module xxnewModule(String name, String sbmlID, String copasiData, Module parent, GeneralGlyph glyph, boolean isContainerModule)
	{
		CCopasiDataModel dataModel = copasiUtility.createDataModel();
		if (isContainerModule)
		{
			name = nameValidation(name);
		}
		
		//dataModel.getModel().setSBMLId(sbmlID);
		System.out.println();
		//System.out.println(copasiData);
		System.out.println();
		try
		{
			dataModel.importSBMLFromString(copasiData);
		}
		catch (java.lang.Exception ex)
		{
			ex.printStackTrace();
		}
		dataModel.getModel().setObjectName(name);
		//dataModel.importSBMLFromString(copasiData);
		Module mod = new Module(name, dataModel.getModel().getKey(), parent);
		if (parent == null)
		{
			mod.setDrawingCellStyle("Module");
			masterModuleList.add(mod);
		}
		else
		{
			mod.setDrawingCellStyle("Submodule_No_Show_Information");
			parent.addChild(mod);
		}
		
		//System.out.println(name + " key = " + mod.getKey());
		
		//treeView.setup(mod);
		//treeView.setup();
		treeView.addNode(mod);
		drawingBoard.createCell(mod, glyph);
		//drawingBoard.changeModule(mod);
		modelBuilder.loadModel(mod.getKey(), false, true);
		//modelBuilder.setVisible(true);		
		
		//activeModule = mod;
		System.out.println("Module created. Name: " + name + "...SBMLid: " + mod.getKey() + "");
		System.out.println("Number of COPASI data models = " + copasiUtility.getNumberOfModels());
		return mod;
	}
	
	/**
	 * Create a new submodule in the three panels.
	 * @param name the name of the new submodule
	 */
	public void newSubmodule(String name, Module parent)
	{
		CCopasiDataModel dataModel = copasiUtility.createDataModel();
		dataModel.getModel().setObjectName(name);
		Module mod = new Module(name, dataModel.getModel().getKey(), parent);
		mod.setDrawingCellStyle("Submodule_No_Show_Information");
		//System.out.println(name + " key = " + mod.getKey());
		parent.addChild(mod);
		treeView.addNode(mod);
		drawingBoard.createCell(mod);
		drawingBoard.addCell(mod);
		drawingBoard.changeModule(parent);
		//moduleList.add(mod);
		//printList();
	}
	
	public Module newSubmodule(String name, String sbmlID, String copasiData, Module parent, GeneralGlyph glyph)
	{
		CCopasiDataModel dataModel = copasiUtility.createDataModel();
		dataModel.getModel().setObjectName(name);
		dataModel.getModel().setSBMLId(sbmlID);
		
		try
		{
			dataModel.importSBMLFromString(copasiData);
		}
		catch (java.lang.Exception ex)
		{
			ex.printStackTrace();
		}
		
		Module mod = new Module(name, dataModel.getModel().getKey(), parent);
		mod.setDrawingCellStyle("Submodule_No_Show_Information");
		//System.out.println(name + " key = " + mod.getKey());
		parent.addChild(mod);
		treeView.addNode(mod);
		drawingBoard.createCell(mod);
		drawingBoard.addCell(mod, glyph);
		//drawingBoard.addCell(mod);
		drawingBoard.changeModule(parent);
		System.out.println("Submodule created. Name: " + name + "...SBMLid: " + sbmlID + "");
		return mod;
	}
	
	public Module xnewSubmodule(String name, String sbmlID, String copasiData, Module parent, GeneralGlyph glyph)
	{
		CCopasiDataModel dataModel = copasiUtility.createDataModel();
		dataModel.getModel().setObjectName(name);
		dataModel.getModel().setSBMLId(sbmlID);
		
		try
		{
			dataModel.importSBMLFromString(copasiData);
		}
		catch (java.lang.Exception ex)
		{
			ex.printStackTrace();
		}
		
		Module mod = new Module(name, dataModel.getModel().getKey(), parent);
		mod.setDrawingCellStyle("Submodule_No_Show_Information");
		//System.out.println(name + " key = " + mod.getKey());
		parent.addChild(mod);
		treeView.addNode(mod);
		drawingBoard.createCell(mod, glyph);
		//drawingBoard.addCell(mod, glyph);
		//drawingBoard.addCell(mod);
		//drawingBoard.changeModule(parent);
		System.out.println("Submodule created. Name: " + name + "...SBMLid: " + sbmlID + "");
		return mod;
	}
	
	public static void addVisibleVariable(String refName)
	{
		VariableType vType= null;
		if (refName.endsWith(VariableType.SPECIES.toString()))
		{
			refName = refName.replace(" - " + VariableType.SPECIES.toString(), "");
			vType = VariableType.SPECIES;
		}
		else if (refName.endsWith(VariableType.GLOBAL_QUANTITY.toString()))
		{
			refName = refName.replace(" - " + VariableType.GLOBAL_QUANTITY.toString(), "");
			vType = VariableType.GLOBAL_QUANTITY;
		}
		else
		{
			System.err.println("AC_GUI.addVisibleVariable: A valid VariableType was not found.");
		}
		
		VisibleVariable var = new VisibleVariable(activeModule, refName, vType);
		drawingBoard.addVisibleVariable(activeModule, var);
		activeModule.addVisibleVariable(var);
	}
	
	public static void addVisibleVariable(Module parentMod, String refName, Object varCell)
	{
		VisibleVariable var = new VisibleVariable(activeModule, refName, varCell, VariableType.SPECIES);
		drawingBoard.setValue(varCell, var);
		activeModule.addVisibleVariable(var);
		
		//add a new species to msmb
		modelBuilder.addSpecies(refName);
	}
	
	public static void addVisibleVariable(Module parentMod, String refName, String varType, GeneralGlyph glyph)
	{
		VariableType vType= null;
		if (varType.equalsIgnoreCase(VariableType.SPECIES.toString()))
		{
			vType = VariableType.SPECIES;
		}
		else if (varType.equalsIgnoreCase(VariableType.GLOBAL_QUANTITY.toString()))
		{
			vType = VariableType.GLOBAL_QUANTITY;
		}
		else
		{
			System.err.println("AC_GUI.addVisibleVariable: A valid VariableType was not found.");
		}
		
		VisibleVariable var = new VisibleVariable(parentMod, refName, vType);
		drawingBoard.createVisibleVariable(var, glyph);
		parentMod.addVisibleVariable(var);
	}
	
	public static void removeVisibleVariable(VisibleVariable var)
	{
		drawingBoard.removeEdges(var.getDrawingCell());
		drawingBoard.removeCell(var.getDrawingCell());
		modelBuilder.removeSpecies(var.getRefName());
		activeModule.removeVisibleVariable(var);
	}
	
	/**
	 * Remove the given module, and its children, from all three panels.
	 * @param mod the module to be removed
	 */
	public void removeSubmodule(Module mod)
	{
		setSelectedModule(mod.getParent());
		treeView.removeNode(mod.getTreeNode());
		
		ArrayList<Port> ports;
		//ListIterator<Port> ports;
		
		// get the list of ports
		ports = mod.getPorts();
		
		/*
		if (ports.size() != 0)
		{
			for(int i = 0; i < ports.size(); i++)
			{
				removePort(ports.get(0));
			}
		}
		*/
		while(ports.size() > 0)
		{
			removePort(ports.get(0));
		}
		/*
		// loop through each port of the module
		while (ports.hasNext())
		{
			removePort(ports.next());
		}
		*/
		drawingBoard.removeCell(mod.getDrawingCell());
		mod.getParent().removeChild(mod);
		//moduleList.remove(mod);
	}
	
	/**
	 * Add a port to the given module.
	 * @param mod the module to add the port
	 * @param name the name of the port
	 * @param type the type of the port
	 */
	public void addPort(Module parentMod, String refName, String name, PortType pType)
	{
		int portCount = parentMod.getPorts().size();
		VariableType vType= null;
		if (refName.endsWith(VariableType.SPECIES.toString()))
		{
			refName = refName.replace(" - " + VariableType.SPECIES.toString(), "");
			vType = VariableType.SPECIES;
		}
		else if (refName.endsWith(VariableType.GLOBAL_QUANTITY.toString()))
		{
			refName = refName.replace(" - " + VariableType.GLOBAL_QUANTITY.toString(), "");
			vType = VariableType.GLOBAL_QUANTITY;
		}
		else
		{
			System.err.println("AC_GUI.addPort: A valid VariableType was not found.");
		}
		
		Port newPort = new Port(parentMod, refName, pType, vType, name, portCount);
		modelBuilder.addPort(newPort);
		drawingBoard.addPort(parentMod, newPort);
		parentMod.addPort(newPort);
	}
	
	public static void addPort(Module parentMod, String name, String refName, String portType, String varType, GeneralGlyph portGlyph)
	{
		int portCount = parentMod.getPorts().size();
		PortType pType = null;
		VariableType vType= null;
		
		if (varType.equalsIgnoreCase(VariableType.SPECIES.toString()))
		{
			vType = VariableType.SPECIES;
		}
		else if (varType.equalsIgnoreCase(VariableType.GLOBAL_QUANTITY.toString()))
		{
			vType = VariableType.GLOBAL_QUANTITY;
		}
		else
		{
			System.err.println("AC_GUI.addPort: A valid VariableType was not found.");
		}

		if (portType.equalsIgnoreCase(PortType.INPUT.toString()))
		{
			pType = PortType.INPUT;
		}
		else if (portType.equalsIgnoreCase(PortType.OUTPUT.toString()))
		{
			pType = PortType.OUTPUT;
		}
		else if (portType.equalsIgnoreCase(PortType.EQUIVALENCE.toString()))
		{
			pType = PortType.EQUIVALENCE;
		}
		else
		{
			System.err.println("AC_GUI.addPort: A valid PortType was not found.");
		}
		
		Port newPort = new Port(parentMod, refName, pType, vType, name, portCount);
		modelBuilder.addPort(newPort);
		drawingBoard.addPort(parentMod, newPort, portGlyph);
		parentMod.addPort(newPort);
	}
	
	public static void xaddPort(Module parentMod, String name, String refName, String portType, String varType, GeneralGlyph portGlyph)
	{
		int portCount = parentMod.getPorts().size();
		PortType pType = null;
		VariableType vType= null;
		
		if (varType.equalsIgnoreCase(VariableType.SPECIES.toString()))
		{
			vType = VariableType.SPECIES;
		}
		else if (varType.equalsIgnoreCase(VariableType.GLOBAL_QUANTITY.toString()))
		{
			vType = VariableType.GLOBAL_QUANTITY;
		}
		else
		{
			System.err.println("AC_GUI.addPort: A valid VariableType was not found.");
		}

		if (portType.equalsIgnoreCase(PortType.INPUT.toString()))
		{
			pType = PortType.INPUT;
		}
		else if (portType.equalsIgnoreCase(PortType.OUTPUT.toString()))
		{
			pType = PortType.OUTPUT;
		}
		else if (portType.equalsIgnoreCase(PortType.EQUIVALENCE.toString()))
		{
			pType = PortType.EQUIVALENCE;
		}
		else
		{
			System.err.println("AC_GUI.addPort: A valid PortType was not found.");
		}
		
		Port newPort = new Port(parentMod, refName, pType, vType, name, portCount);
		//modelBuilder.addPort(newPort);
		//drawingBoard.addPort(parentMod, newPort, portGlyph);
		drawingBoard.createPort(newPort, portGlyph);
		parentMod.addPort(newPort);
	}
	
	/**
	 * Remove the given port.
	 * @param port the port to be removed
	 */
	public void removePort(Port port)
	{
		// get the parent module of the port
		Module parentMod = port.getParent();
		// remove any edges connected to the port
		drawingBoard.removeEdges(port.getDrawingCell());
		// remove the drawing cell representation from the drawing board
		drawingBoard.removeCell(port.getDrawingCell());
		// remove the port from the model builder
		modelBuilder.removePort(port);
		// remove the port from the parent module
		parentMod.removePort(port);
	}
	
	/**
	 * Add a connection to the given module.
	 * @param parentMod the module to add a connection
	 * @param connectionCell the drawing cell representation of the connection
	 */
	public static void addConnection(Module parentMod, Object connectionCell)
	{
		// make a connection object
		Connection edge = new Connection(parentMod, connectionCell);
		// set the connection as the user object of the drawing cell
		drawingBoard.setValue(connectionCell, edge);
		// add the edge to the parent module
		parentMod.addConnection(edge);
	}
	
	public static void addConnection(Module parentMod, Object source, Object target, String drawingCellStyle)
	{
		Connection edge = new Connection(parentMod);
		drawingBoard.createConnection(edge, source, target, drawingCellStyle);
		parentMod.addConnection(edge);
	}
	
	/**
	 * Remove the connection from the given module.
	 * @param parentMod the module containing the connection
	 * @param connectionCell the drawing cell representation of the connection to remove
	 */
	public void removeConnection(Connection edge)
	{
		// get the parent module of the connection
		Module parentMod = edge.getParent();
		// remove the connection object from the parent module
		parentMod.removeConnection(edge);
	}
	
	public static void addMathAggregator(String name, int inputs, Operation op)
	{
		CCopasiDataModel dataModel = copasiUtility.createDataModel();
		dataModel.getModel().setObjectName(name);
		MathematicalAggregator mathAgg = new MathematicalAggregator(name, dataModel.getModel().getKey(), inputs, op, activeModule);
		if(op == Operation.SUM)
		{
			mathAgg.setDrawingCellStyle("Summation");
		}
		else
		{
			mathAgg.setDrawingCellStyle("Product");
		}
		//System.out.println(name + " key = " + mod.getKey());
		addMathAggPorts(mathAgg);
		activeModule.addChild(mathAgg);
		treeView.addNode(mathAgg);
		drawingBoard.addMathAggregator(mathAgg);
	}
	
	public static MathematicalAggregator addMathAggregator(String name, String sbmlID, String copasiData, Module parent, int inputs, String op, GeneralGlyph glyph)
	{
		CCopasiDataModel dataModel = copasiUtility.createDataModel();
		dataModel.getModel().setObjectName(name);
		dataModel.getModel().setSBMLId(sbmlID);
		Operation oper = null;
		
		try
		{
			dataModel.importSBMLFromString(copasiData);
		}
		catch (java.lang.Exception ex)
		{
			ex.printStackTrace();
		}
		
		if(op.equalsIgnoreCase(Operation.SUM.toString()))
		{
			oper = Operation.SUM;
		}
		else if(op.equalsIgnoreCase(Operation.PRODUCT.toString()))
		{
			oper = Operation.PRODUCT;
		}
		else
		{
			System.err.println("AC_GUI.addMathAggregator: A valid Operation was not found.");
		}
		
		//Module mod = new Module(name, dataModel.getModel().getKey(), parent);
		MathematicalAggregator mod = new MathematicalAggregator(name, dataModel.getModel().getKey(), null, inputs, oper, parent);
		
		if(op.equalsIgnoreCase(Operation.SUM.toString()))
		{
			mod.setDrawingCellStyle("Summation");
		}
		else if(op.equalsIgnoreCase(Operation.PRODUCT.toString()))
		{
			mod.setDrawingCellStyle("Product");
		}
		
		//System.out.println(name + " key = " + mod.getKey());
		parent.addChild(mod);
		treeView.addNode(mod);
		drawingBoard.createCell(mod, glyph);
		//drawingBoard.addMathAggregator(mod);
		//drawingBoard.addCell(mod, glyph);
		//drawingBoard.changeModule(parent);
		System.out.println("MathematicalAggregator created. Name: " + name + "...SBMLid: " + sbmlID + "");
		return mod;
	}
	
	public static void addEquivalenceNode(Module parentMod, Object cell)
	{
		//Port sourcePort = (Port)((mxCell)cell).getSource().getValue();
		//Port targetPort = (Port)((mxCell)cell).getTarget().getValue();
		String refName = ((Port)((mxCell)cell).getSource().getValue()).getRefName();
		//refName = "E" + refName;
		EquivalenceNode eNode = new EquivalenceNode(parentMod, refName);
		drawingBoard.addEquivalenceNode(eNode, cell);
		parentMod.addEquivalenceNode(eNode);
		
		//create the two connections for the equivalence node
		Connection edge1 = new Connection(parentMod);
		drawingBoard.createConnection(edge1, ((mxCell)cell).getSource(), eNode.getDrawingCell());
		parentMod.addConnection(edge1);
		
		Connection edge2 = new Connection(parentMod);
		drawingBoard.createConnection(edge2, ((mxCell)cell).getTarget(), eNode.getDrawingCell());
		parentMod.addConnection(edge2);
		
		//add a new species to msmb
		modelBuilder.addSpecies(refName);
	}
	
	public static void addEquivalenceNode(Module parentMod, String refName, Object eNodeCell)
	{
		//refName = "E" + refName;
		EquivalenceNode eNode = new EquivalenceNode(parentMod, refName, eNodeCell);
		drawingBoard.setValue(eNodeCell, eNode);
		activeModule.addEquivalenceNode(eNode);
		
		//add a new species to msmb
		modelBuilder.addSpecies(refName);
	}
	
	public static void addEquivalenceNode(Module parentMod, String refName, GeneralGlyph glyph)
	{
		EquivalenceNode eNode = new EquivalenceNode(parentMod, refName);
		drawingBoard.createEquivalenceNode(eNode, glyph);
		parentMod.addEquivalenceNode(eNode);
	}
	
	public static void removeEquivalenceNode(EquivalenceNode eNode)
	{
		drawingBoard.removeEdges(eNode.getDrawingCell());
		drawingBoard.removeCell(eNode.getDrawingCell());
		modelBuilder.removeSpecies(eNode.getRefName());
		activeModule.removeEquivalenceNode(eNode);
	}
	
	/**
	 * Select the representation of the given module on the treeView and drawingBoard.
	 * @param mod the module to be selected
	 */
	public static void setSelectedModule(Module mod)
	{
		selectedModule = mod;
		treeView.setSelected(mod.getTreeNode());
		drawingBoard.setSelected(mod.getDrawingCell());
	}
	
	/**
	 * Select the module represented by the given tree node. 
	 * @param treeNode the treeView representation of the module to be selected
	 */
	public void setSelectedModule(DefaultMutableTreeNode treeNode)
	{
		setSelectedModule(masterModuleList.findModule(treeNode));
	}
	
	/**
	 * Select the module represented by the given drawing cell.
	 * @param drawingCell the drawingBoard representation of the module to be selected
	 */
	public void setSelectedModule(Object drawingCell)
	{
		setSelectedModule(masterModuleList.findModule(drawingCell));
	}
	
	public static void setSelectedDrawingBoardPort(Port portSelected)
	{
		/*
		String name = modelBuilder.getNameFromPortTable(rowSelected);
		int endRefNameIndex = name.indexOf("-") - 1;
		int startModNameIndex = name.indexOf("-") + 2;
		String refName = name.substring(0, endRefNameIndex);
		String modName = name.substring(startModNameIndex);
		System.out.println(refName + ", " + modName);
		*/
		if (portSelected != null)
		{
			drawingBoard.setSelected(portSelected.getDrawingCell());
		}
	}
	
	public static void setSelectedModelBuilderVariable(String refName, VariableType vType)
	{
		modelBuilder.setSelectedVariable(refName, vType);
	}
	
	public static void setSelectedDrawingBoardVariable(String refName, VariableType vType)
	{
		ListIterator<VisibleVariable> vars = activeModule.getVisibleVariables().listIterator();
		ListIterator<EquivalenceNode> eNodes = activeModule.getEquivalenceNodes().listIterator();
		
		System.out.println("Selected variable type: " + vType.toString() + "...name: " + refName);
		VisibleVariable currentVar;
		while(vars.hasNext())
		{
			currentVar = vars.next();
			if ((currentVar.getVariableType() == vType) && currentVar.getRefName().equalsIgnoreCase(refName))
			{
				drawingBoard.setSelected(currentVar.getDrawingCell());
				return;
			}
		}
		
		EquivalenceNode currenteNode;
		while(eNodes.hasNext())
		{
			currenteNode = eNodes.next();
			if((vType == VariableType.SPECIES) && currenteNode.getRefName().equalsIgnoreCase(refName))
			{
				drawingBoard.setSelected(currenteNode.getDrawingCell());
				return;
			}
		}
	}
	
	public static void loadModelBuilder(Module mod, boolean uneditable, boolean display)
	{
		AC_GUI.modelBuilder.loadModel(mod.getMSMBData().getBytes(), uneditable, display);
		loadPortsIntoModelBuilder(mod);
	}
	
	public static void changeActiveModule(Module mod)
	{
		if (activeModule != null)
		{
			String code = new String(modelBuilder.saveModel());
			activeModule.setMSMBData(code);
			
			
			//System.out.println(activeModule.getName() + " saved to COPASI: " + modelBuilder.saveToCK(activeModule.getKey()));
		}
			
		drawingBoard.changeModule(mod);
		treeView.refreshTree();
		
		//modelBuilder.loadModel(mod.getKey());
		String newCode = mod.getMSMBData();
		if (newCode != null)
		{
			byte[] data = mod.getMSMBData().getBytes();
			modelBuilder.loadModel(data, false, true);
		}
		else
		{
			modelBuilder.loadModel(mod.getKey(), false, true);
		}		
		activeModule = mod;
		
		loadPortsIntoModelBuilder(activeModule);
		
		setSelectedModule(mod);
	}
	
	/**
	 * Initialize the components within the AC frame.
	 */
	private void initializeComponents()
	{
		// The model builder window
		modelBuilder = new ModelBuilder();
		JPanel modelBuilderPanel = new JPanel();
		modelBuilderPanel.setLayout(new BorderLayout());
		modelBuilderPanel.add(modelBuilder.getPanel(), BorderLayout.CENTER);
		modelBuilder.setVisible(false);
		//JScrollPane modelBuilderWindow = new JScrollPane(modelBuilderPanel);

		// The aggregation window
		drawingBoard = new DrawingBoard();

		// The tree window
		treeView = new TreeView();
		JScrollPane treeWindow = new JScrollPane(treeView);
		treeWindow.setOpaque(true);

		initializeMenuItems();

		JSplitPane verticalLine = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeWindow, drawingBoard);
		//verticalLine.setDividerLocation(180 + verticalLine.getInsets().left);
		Dimension dim = new Dimension(180, 500);
		//treeWindow.setMinimumSize(dim);
		treeWindow.setPreferredSize(dim);
		//JSplitPane horizontalLine = new JSplitPane(JSplitPane.VERTICAL_SPLIT, verticalLine, modelBuilderWindow);
		JSplitPane horizontalLine = new JSplitPane(JSplitPane.VERTICAL_SPLIT, verticalLine, modelBuilderPanel);
		//horizontalLine.setDividerLocation(610 + horizontalLine.getInsets().top);
		//System.out.println("Vertical line: " + verticalLine.getDividerLocation());
		//System.out.println("Horizontal line: " + horizontalLine.getDividerLocation());
		
		JScrollPane window = new JScrollPane(horizontalLine);
		this.add(window);
		//this.add(horizontalLine);

		this.pack();
	}

	/**
	 * Setup the menu items.
	 */
	private void initializeMenuItems()
	{
		ACMenuListener menuListener = new ACMenuListener();
		
		// File
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(makeMenuItem(MenuItem.NEW, menuListener, KeyEvent.VK_N));
		fileMenu.add(makeMenuItem(MenuItem.OPEN, menuListener, KeyEvent.VK_O));
		// recentMenuItem = new JMenu("Recent Files");
		// this.loadRecentFiles();
		fileMenu.addSeparator();
		fileMenu.add(makeMenuItem(MenuItem.SAVE, menuListener, KeyEvent.VK_S));
		fileMenu.add(makeMenuItem(MenuItem.SAVE_AS, menuListener, -1));
		fileMenu.add(makeMenuItem(MenuItem.EXPORT_SBML, menuListener, -1));
		fileMenu.addSeparator();
		fileMenu.add(makeMenuItem(MenuItem.CLOSE, menuListener, -1));
		fileMenu.addSeparator();
	
		fileMenu.add(makeMenuItem(MenuItem.PREFERENCES, menuListener, -1));
		fileMenu.addSeparator();
		
		fileMenu.add(makeMenuItem(MenuItem.EXIT, menuListener, -1));

		// Module
		JMenu moduleMenu = new JMenu("Module");
		moduleMenu.add(makeMenuItem(MenuItem.ADD_SUBMODULE_NEW, menuListener, -1));
		moduleMenu.add(makeMenuItem(MenuItem.ADD_SUBMODULE_TEMPLATE, menuListener, -1));
		moduleMenu.addSeparator();
		moduleMenu.add(makeMenuItem(MenuItem.ADD_SUMMATION_MODULE, menuListener, -1));
		moduleMenu.add(makeMenuItem(MenuItem.ADD_PRODUCT_MODULE, menuListener, -1));
		moduleMenu.addSeparator();
		moduleMenu.add(makeMenuItem(MenuItem.SAVE_SUBMODULE_AS_TEMPLATE, menuListener, -1));
		moduleMenu.addSeparator();
		moduleMenu.add(makeMenuItem(MenuItem.REMOVE_SUBMODULE, menuListener, -1));

		// Tools
		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.add(makeMenuItem(MenuItem.VALIDATE_MODEL, menuListener, -1));
		toolsMenu.addSeparator();
		toolsMenu.add(makeMenuItem(MenuItem.VIEW_MODEL, menuListener, -1));
		toolsMenu.add(makeMenuItem(MenuItem.FLATTEN_MODEL, menuListener, -1));
		toolsMenu.addSeparator();
		toolsMenu.add(makeMenuItem(MenuItem.DECOMPOSE_INTO_MODULES, menuListener, -1));
		
		// Help
		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(makeMenuItem(MenuItem.HELP_CONTENTS, menuListener, -1));
		helpMenu.add(makeMenuItem(MenuItem.ABOUT_AGGREGATION_CONNECTOR, menuListener, -1));

		// Add items to the menu bar
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(moduleMenu);
		menuBar.add(toolsMenu);
		menuBar.add(helpMenu);

		// Add the menu bar to the frame
		this.setJMenuBar(menuBar);
	}

	/**
	 * Create a menu item.
	 * 
	 * @param name the name
	 * @param aListener the corresponding action listener
	 * @param keyEvent the shortcut key pressed.
	 * @return a new menu item
	 */
	private JMenuItem makeMenuItem(MenuItem menuItem, ActionListener aListener, int keyEvent)
	{
		int defaultShortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		JMenuItem item = new JMenuItem(menuItem.toString());
		item.setActionCommand(menuItem.toString());
		item.addActionListener(aListener);
		if (keyEvent != -1)
		{
			item.setMnemonic(keyEvent);
			item.setAccelerator(KeyStroke.getKeyStroke(keyEvent, defaultShortcutMask));
		}
		return item;
	}

	private static void addMathAggPorts(MathematicalAggregator mathAgg)
	{
		int inputs = mathAgg.getNumberofInputs();
		String inputPrefix = mathAgg.getInputPrefix();
		String outputName = mathAgg.getOutputName();

		Port newPort;
		
		// create the input ports
		for(int i = 0; i < inputs; i++)
		{
			newPort = new Port(mathAgg, inputPrefix+i, PortType.INPUT, VariableType.GLOBAL_QUANTITY, inputPrefix+i+"Port", i);
			modelBuilder.addPort(newPort);
			mathAgg.addPort(newPort);
		}
		
		// create the output port
		newPort = new Port(mathAgg, outputName, PortType.OUTPUT, VariableType.GLOBAL_QUANTITY, "TotalPort", mathAgg.getPorts().size());
		modelBuilder.addPort(newPort);
		mathAgg.addPort(newPort);
	}
	
	private static boolean saveModules()
	{
		boolean successfulSave = true;
		Module child;
		String code;
		byte[] data;
		
		// save the activeModule to COPASI
		System.out.println("AC_GUI.saveModules(): " + activeModule.getName() + "'s copasi key = " + activeModule.getKey());
		CCopasiDataModel dmodel = copasiUtility.getCopasiModelFromKey(activeModule.getKey());
		if (dmodel == null)
		{
			System.err.println("The COPASI datamodel for " + activeModule.getName() + " is null.");
		}
		System.out.println(activeModule.getName() + "'s datamodel is not null.");
		successfulSave = modelBuilder.saveToCK(activeModule.getKey());
		if (!successfulSave)
		{
			return false;
		}
		
		// store the activeModule's msmb data
		code = new String(modelBuilder.saveModel());
		activeModule.setMSMBData(code);
		
		// load and save each of the activeModule's children
		ListIterator<Module> children = activeModule.getChildren().listIterator();
		while (children.hasNext())
		{
			child = children.next();
			
			code = child.getMSMBData();
			if (code != null)
			{
				data = child.getMSMBData().getBytes();
				modelBuilder.loadModel(data, false, false);
			}
			else
			{
				//System.err.println("AC_GUI.saveModules(): " + child.getName() + "'s msmb data is NULL.");
				//System.exit(0);
				modelBuilder.loadModel(child.getKey(), false, false);
				
				// store the child module's msmb data
				code = new String(modelBuilder.saveModel());
				child.setMSMBData(code);
			}		
			
			// save the child module to COPASI
			System.out.println("AC_GUI.saveModules(): " + child.getName() + "'s copasi key = " + child.getKey());
			successfulSave =  modelBuilder.saveToCK(child.getKey());
			if (!successfulSave)
			{
				return false;
			}
			
			// store the child module's msmb data
			//code = new String(modelBuilder.saveModel());
			//child.setMSMBData(code);
		}
		
		// reload the original activeModule
		code = activeModule.getMSMBData();
		data = activeModule.getMSMBData().getBytes();
		modelBuilder.loadModel(data, false, true);
		loadPortsIntoModelBuilder(activeModule);
		return successfulSave;
	}
	
	private static void loadPortsIntoModelBuilder(Module mod)
	{
		//add activeModule's Ports
		ListIterator<Port> portList = mod.getPorts().listIterator();
		while (portList.hasNext())
		{
			modelBuilder.addPort(portList.next());
		}
		
		//add activeModule's Children's Ports
		ListIterator<Module> children = mod.getChildren().listIterator();
		while(children.hasNext())
		{
			portList = children.next().getPorts().listIterator();
			while (portList.hasNext())
			{
				modelBuilder.addPort(portList.next());
			}
		}
	}
	
	/**
	 * Open the Preferences display from MSMB.
	 */
	public static void openPreferencesMSMB() {
		modelBuilder.openPreferencesMSMB();
	}
	
	/**
	 * Return if a module is currently open.
	 * @return true if a module is currently open,
	 * false otherwise
	 */
	public static boolean isModuleOpen()
	{
		return (activeModule != null);
	}
	
	public static void updatePort(Port port, String value, int col)
	{
		
		if (col == 1)
		{
			port.setRefName(value);
		}
		else if (col == 2)
		{
			port.setType(PortType.valueOf(value.toUpperCase()));
		}
		else if (col == 3)
		{
			port.setName(value);
		}
		
		drawingBoard.updatePort(port.getDrawingCell());
	}
	
	public static boolean submoduleNameValidation(String name)
	{
		if (activeModule == null)
		{
			return true;
		}
		
		if (name.compareToIgnoreCase(activeModule.getName()) == 0)
		{
			return false;
		}
		
		ListIterator<Module> children = activeModule.getChildren().listIterator();
		
		while(children.hasNext())
		{
			if (name.compareToIgnoreCase(children.next().getName()) == 0)
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean portValidation(String portName, String refName)
	{
		ListIterator<Port> ports = activeModule.getPorts().listIterator();
		Port currentPort;
		String msg;
		String oldRefName = refName;
		VariableType vType= null;
		
		// trim the refName
		if (refName.endsWith(VariableType.SPECIES.toString()))
		{
			System.out.println("Old refName: " + refName);
			refName = refName.replace(" - " + VariableType.SPECIES.toString(), "");
			System.out.println("New refName: " + refName);
			vType = VariableType.SPECIES;
		}
		else if (refName.endsWith(VariableType.GLOBAL_QUANTITY.toString()))
		{
			System.out.println("Old refName: " + refName);
			refName = refName.replace(" - " + VariableType.GLOBAL_QUANTITY.toString(), "");
			System.out.println("New refName: " + refName);
			vType = VariableType.GLOBAL_QUANTITY;
		}
		else
		{
			System.err.println("AC_GUI.portValidation: A valid VariableType was not found.");
		}
		
		while (ports.hasNext())
		{
			currentPort = ports.next();
			
			if ((refName.compareToIgnoreCase(currentPort.getRefName()) == 0) && (vType.equals(currentPort.getVariableType())))
			{
				//System.out.println("comp refName: " + refName.compareToIgnoreCase(currentPort.getRefName()));
				msg = "\"" + oldRefName + "\"";
				msg += " is already associated with a Port.";
				msg += " Cannot associate the same Ref Name with multiple Ports.";
				JOptionPane.showMessageDialog(null, msg);
				return false;
			}
			
			if (portName.compareTo(currentPort.getName()) == 0)
			{
				//System.out.println("comp portName: " + portName.compareToIgnoreCase(currentPort.getName()));
				msg = "\"" + portName + "\"";
				msg += " is already the name of a Port.";
				msg += " Cannot assign the same Port Name to multiple Ports.";
				JOptionPane.showMessageDialog(null, msg);
				return false;
			}
		}
		return true;
	}
	
	public static boolean portRefNameValidation(String refName)
	{
		ListIterator<Port> ports = activeModule.getPorts().listIterator();
		Port currentPort;
		String msg;
		String oldRefName = refName;
		
		// trim the refName
		if (refName.endsWith(VariableType.SPECIES.toString()))
		{
			System.out.println("Old refName: " + refName);
			refName = refName.replace(" - " + VariableType.SPECIES.toString(), "");
			System.out.println("New refName: " + refName);
		}
		else if (refName.endsWith(VariableType.GLOBAL_QUANTITY.toString()))
		{
			System.out.println("Old refName: " + refName);
			refName = refName.replace(" - " + VariableType.GLOBAL_QUANTITY.toString(), "");
			System.out.println("New refName: " + refName);
		}
		else
		{
			System.err.println("AC_GUI.portNameValidation: A valid VariableType was not found.");
		}
		
		while (ports.hasNext())
		{
			currentPort = ports.next();
			
			if (refName.compareToIgnoreCase(currentPort.getRefName()) == 0)
			{
				//System.out.println("comp refName: " + refName.compareToIgnoreCase(currentPort.getRefName()));
				msg = "\"" + oldRefName + "\"";
				msg += " is already associated with a Port.";
				msg += " Cannot associate the same Ref Name with multiple Ports.";
				JOptionPane.showMessageDialog(null, msg);
				return false;
			}
		}
		return true;
	}
	
	public static boolean portNameValidation(String portName, Module parentMod)
	{
		ListIterator<Port> ports = parentMod.getPorts().listIterator();
		Port currentPort;
		String msg;
		
		while (ports.hasNext())
		{
			currentPort = ports.next();
			
			if (portName.compareTo(currentPort.getName()) == 0)
			{
				//System.out.println("comp refName: " + refName.compareToIgnoreCase(currentPort.getRefName()));
				msg = "\"" + portName + "\"";
				msg += " is already the name of a Port.";
				msg += " Cannot assign the same Port Name to multiple Ports.";
				JOptionPane.showMessageDialog(null, msg);
				return false;
			}
		}
		return true;
	}
	
	public static boolean visibleVariableValidation(String refName)
	{
		ListIterator<VisibleVariable> vars = activeModule.getVisibleVariables().listIterator();
		ListIterator<EquivalenceNode> eNodes = activeModule.getEquivalenceNodes().listIterator();
		String oldRefName = refName;
		String msg;
		VariableType vType = null;
		
		// trim the refName
		if (refName.endsWith(VariableType.SPECIES.toString()))
		{
			System.out.println("Old refName: " + refName);
			refName = refName.replace(" - " + VariableType.SPECIES.toString(), "");
			System.out.println("New refName: " + refName);
			vType = VariableType.SPECIES;
		}
		else if (refName.endsWith(VariableType.GLOBAL_QUANTITY.toString()))
		{
			System.out.println("Old refName: " + refName);
			refName = refName.replace(" - " + VariableType.GLOBAL_QUANTITY.toString(), "");
			System.out.println("New refName: " + refName);
			vType = VariableType.GLOBAL_QUANTITY;
		}
		else
		{
			System.err.println("AC_GUI.visibleVariableValidation: A valid VariableType was not found.");
		}
		
		VisibleVariable currentVar;
		while (vars.hasNext())
		{
			currentVar = vars.next();
			
			if ((refName.compareToIgnoreCase(currentVar.getRefName()) == 0) && (vType.equals(currentVar.getVariableType())))
			{
				//System.out.println("comp refName: " + refName.compareToIgnoreCase(currentPort.getRefName()));
				msg = "\"" + oldRefName + "\"";
				msg += " is already visible.";
				msg += " Cannot show the same variable multiple times.";
				JOptionPane.showMessageDialog(null, msg);
				return false;
			}
		}
		
		EquivalenceNode currenteNode;
		while (eNodes.hasNext())
		{
			currenteNode = eNodes.next();
			
			if ((refName.compareToIgnoreCase(currenteNode.getRefName()) == 0) && (vType.equals(VariableType.SPECIES)))
			{
				msg = "\"" + oldRefName + "\"";
				msg += " is already visible.";
				msg += " Cannot show the same variable multiple times.";
				JOptionPane.showMessageDialog(null, msg);
				return false;
			}
		}
		return true;
	}
	
	public static boolean newNameValidation(String newSpecies)
	{
		Vector<String> refNames = modelBuilder.getRefNames();
		return !refNames.contains(newSpecies);
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
		
		ListIterator<Port> ports = activeModule.getPorts().listIterator();
		Port currentPort;
		while(ports.hasNext())
		{
			currentPort = ports.next();
			if (before.equalsIgnoreCase(currentPort.getRefName()))
			{
				currentPort.setRefName(after);
				changeRequired = true;
			}
		}
		
		ListIterator<VisibleVariable> vars = activeModule.getVisibleVariables().listIterator();
		VisibleVariable currentVar;
		while(vars.hasNext())
		{
			currentVar = vars.next();
			if (before.equalsIgnoreCase(currentVar.getRefName()))
			{
				currentVar.setRefName(after);
				changeRequired = true;
			}
		}
		
		ListIterator<EquivalenceNode> eNodes = activeModule.getEquivalenceNodes().listIterator();
		EquivalenceNode currenteNode;
		while(eNodes.hasNext())
		{
			currenteNode = eNodes.next();
			if (before.equalsIgnoreCase(currenteNode.getRefName()))
			{
				currenteNode.setRefName(after);
				changeRequired = true;
			}
		}
		
		if(changeRequired)
		{
			//changeActiveModule(activeModule);
			drawingBoard.changeModule(activeModule);
			modelBuilder.updatePorts();
		}
	}
	
	public static void changeModuleName(Module mod, String newName, boolean fromModelBuilder)
	{
		mod.setName(newName);
		treeView.refreshTree();
		if (mod == activeModule)
		{
			if (!fromModelBuilder)
			{
				modelBuilder.setModelName(newName);
			}
		}
		// call msmb and set the model name
		/*
		dataModel = copasiUtility.getCopasiModelFromKey(mod.getKey());
		if (dataModel != null)
		{
			dataModel.getModel().setObjectName(newName);
		}
		else
		{
			System.err.println("AC_GUI.changeModuleName, no Copasi data model found.");
		}
		*/
	}
	
	public static String nameValidation(String name)
	{
		String newName = name;
		String message = "";
		while(newName != null)
		{
			if (!newName.isEmpty())
			{
				if (AC_GUI.sbmlNameValidation(newName))
				{
					if (AC_GUI.submoduleNameValidation(newName))
					{
						//dataModel.getModel().setObjectName(newName);
						name = newName;
						break;
					}
					else
					{
						message = "There already exists a submodule with the same name." + eol;
						message += "Please enter a different name:";
						//JOptionPane.showMessageDialog(null, message);
					}
				}
				else
				{
					message = "Invalid name. Names must adhere to the following rules:" + eol;
					message += "\u2022 Names cannot start with a number or punctuation character." + eol;
					message += "\u2022 Names cannot start with the letters \"xml\"." + eol;
					message += "\u2022 Names cannot contain spaces." + eol;
					message += "Please enter a different name:";
											
					//JOptionPane.showMessageDialog(null, message);
				}
			}
			newName = JOptionPane.showInputDialog(message, newName);
		}
		return name;
	}
	
	public static boolean sbmlNameValidation(String name)
	{
		String first = name.substring(0, 1);
		
		if(Pattern.matches("\\d", first))
		{
			return false;
		}
		
		if(name.matches(".*\\s.*"))
		{
			return false;
		}
		
		if(Pattern.matches("\\p{Punct}", first))
		{
			return false;
		}
		
		if(name.toLowerCase().startsWith("xml"))
		{
			return false;
		}
		
		return true;
	}
	
	public static void close()
	{
		masterModuleList.clearList();
		listOfTemplates.clear();
		treeView.clear();
		drawingBoard.clear();
		copasiUtility.clear();
		modelBuilder.setVisible(false);
		activeModule = null;
	}
}
