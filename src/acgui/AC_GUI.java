package acgui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.JFileChooser;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;

import msmb.gui.MainGui;

import org.COPASI.CCopasiDataModel;
import org.sbml.libsbml.GeneralGlyph;
import org.sbml.libsbml.libsbml;

import com.mxgraph.model.mxCell;

/**
 * @author Thomas
 *
 */
public class AC_GUI extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static String ac_version = "0.0";
	protected static DrawingBoard drawingBoard;
	protected static TreeView treeView;
	protected static ModelBuilder modelBuilder;
	protected static Module rootModule;
	protected static Module activeModule;
	protected static Module selectedModule;
	protected static CopasiUtility copasiUtility;
	protected static boolean modelSavedInACDataStructure;
	protected static boolean modelSavedInACFile;
	protected static String lastLoadSave_file;
	
	private static JMenuBar menuBar;
	private static JMenu fileMenu;
	private static JMenu recentFilesMenu;
	private static JMenu moduleMenu;
	private static JMenu toolsMenu;
	private static JMenu helpMenu;
	
	private static String file_RecentFiles = new String();
	private static Vector<String> recentFiles = new Vector<String>();
	
	/**
	 * Construct the AC_GUI object.
	 */
	public AC_GUI()
	{
		super(Constants.TOOL_NAME_FULL);
		copasiUtility = new CopasiUtility();
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
			addLibraryPath(".\\lib");
			System.loadLibrary("sbmlj");
			System.out.println("Using LibSBML: " + libsbml.getLibSBMLDottedVersion());
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}

		final AC_GUI currentGUI = new AC_GUI();
		//currentGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
	
	private String getVersionFromFile() 
	{
		BufferedReader fin;
		String strLine;
		String major = "";
		String minor = "";
		String commit = "";
		
		try 
		{
			InputStream is = getClass().getResourceAsStream("util/version.txt");
			if(is == null)
			{
				 return "0.9.0";
			}
			fin = new BufferedReader(new InputStreamReader(is));
			while ((strLine = fin.readLine()) != null)
			{
				if(strLine.toLowerCase().contains("major"))
				{
					major = strLine.substring(strLine.indexOf("=")+1).trim();
				}
				else if (strLine.toLowerCase().contains("minor"))
				{
					minor = strLine.substring(strLine.indexOf("=")+1).trim();
				}
				else if (strLine.toLowerCase().contains("commit"))
				{
					commit = strLine.substring(strLine.indexOf("=")+1).trim();
				} 
			 }
			fin.close();
			return major+"."+minor+"."+commit;	
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			return "0.9.0";
		}
	}
	
	public static void load(String fileName, boolean external)
	{
		Module mod = null;
		String ext = fileName.substring(fileName.lastIndexOf("."));
    	
		try
		{
			if (ext.equals(".xml"))
			{
				mod = SBMLParser.importSBML(fileName, external);
			}
			else if (ext.equals(".cps"))
			{
				mod = CopasiUtility.importCopasiFile(fileName);
			}
			else if (ext.equals(".ac"))
			{
				mod = AC_IO.loadModule(fileName);
				setSavedInACFile(true);
			}
				//System.out.println("Number of models in the CCopasiRootContainer: " + CCopasiRootContainer.getDatamodelList().size());
		}
		catch (java.lang.Exception ex){
			ex.printStackTrace();
			System.err.println( "Error while importing the model from file named \"" + fileName + "\"." );
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
		//treeView.addNode(mod);
		if (mod == null)
		{
			return;
		}
		lastLoadSave_file = fileName;
		addRecentFile(fileName);
		rootModule = mod;
		AC_Utility.addSubmoduleDefinitionsToList(mod);
		changeActiveModule(mod);
		//activeModule = mod;
	}
	
	public static void loadSubmodule(String fileName, Module parent, boolean external)
	{
		Module mod = null;
		String ext = fileName.substring(fileName.lastIndexOf("."));
    	
		try
		{
			if (ext.equals(".xml"))
			{
				mod = SBMLParser.importSBML(fileName, parent, external);
			}
			else if (ext.equals(".cps"))
			{
				mod = CopasiUtility.importCopasiFile(fileName, parent);
				mod.getModuleDefinition().setExternal(false);
			}
			else if (ext.equals(".ac"))
			{
				mod = AC_IO.loadModule(fileName, parent);
			}
				//System.out.println("Number of models in the CCopasiRootContainer: " + CCopasiRootContainer.getDatamodelList().size());
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
		if (mod == null)
		{
			return;
		}
		/*
		if (mod.getChildren().size() == 0)
		{
			AC_Utility.addSubmoduleDefinition(mod.getModuleDefinition());
		}
		*/
		addRecentFile(fileName);
		AC_Utility.addSubmoduleDefinitionsToList(mod);
		setSavedInACFile(false);
		changeActiveModule(activeModule);
	}
	
	public static void save(Module module, String fileName)
	{
		/*
		SBMLParser sbmlParser = new SBMLParser();
		String key = selectedModule.getKey();
		sbmlParser.saveSBML(copasiUtility.getSBML(key), selectedModule, fileName);
		*/
		//copasiUtility.exportModel(key);
		if (module == activeModule)
		{
			if (drawingBoard.getActiveSubmoduleButtonCell() != null)
			{
				// the submodule info view is currently active
				removeSubmoduleInfoView(drawingBoard.getActiveSubmoduleButtonCell(), false);
			}
			byte[] code = modelBuilder.saveModel();
			if (code == null || code.length == 0)
			{
				System.err.println("AC_GUI.save(): " + activeModule.getName() + "'s msmb data is NULL.");
			}
			activeModule.getModuleDefinition().setMSMBData(code);
			drawingBoard.saveCurrentPositions();
		}
		AC_IO.saveModule(module, fileName);
		lastLoadSave_file = fileName;
		addRecentFile(fileName);
		setSavedInACFile(true);
		setSavedInACDataStructure(true);
	}
	
	public static void exportSBML(String fileName)
	{
		//CopasiUtility.printDataModelList();
		if (!saveModules())
		{
			System.err.println("Problem saving Modules.");
			return;
		}
		//CopasiUtility.printDataModelList();
		if (SBMLParser.exportSBML(activeModule, fileName))
		{
			JOptionPane.showMessageDialog(null, "The module has been saved in " + fileName);
			addRecentFile(fileName);
			setSavedInACDataStructure(true);
		}
		else
		{
			JOptionPane.showMessageDialog(null,
				    "Export error. A SBML document was not created.",
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public static Module newModule(String name)
	{
		Module mod = AC_Utility.createModule(name, "", true);
		treeView.addNode(mod);
		//drawingBoard.addModuleCell(mod);
		drawingBoard.changeModule(mod);
		modelBuilder.loadModel(mod, true, false, true);
		rootModule = mod;
		activeModule = mod;
		setSavedInACFile(false);
		setSavedInACDataStructure(true);
		return mod;
	}
	
	/*
	public static Module newModule(String copasiData, Module parent, GeneralGlyph glyph, boolean isContainerModule)
	{
		Module mod;
		
		if (parent == null)
		{
			mod = CopasiUtility.importModuleCopasiData(copasiData, glyph);
		}
		else
		{
			mod = CopasiUtility.importModuleCopasiData(copasiData, parent, glyph);
		}
		//treeView.addNode(mod);
		//drawingBoard.addModuleCell(mod, glyph);
		//drawingBoard.changeModule(mod);
		//modelBuilder.loadModel(mod, true, false, true);
		//activeModule = mod;
		return mod;
	}
	*/
	
	public static Module newSubmodule(String name, Module parent)
	{
		Module mod = AC_Utility.createModule(name, "", parent, true);
		treeView.addNode(mod);
		drawingBoard.addModuleCell(mod);
		if (AC_Utility.isSubmoduleDefinition(parent.getModuleDefinition()))
		{
			AC_Utility.removeSubmoduleDefinition(parent.getModuleDefinition());
		}
		AC_Utility.addSubmoduleDefinitionsToList(mod);
		setSavedInACFile(false);
		return mod;
	}
	
	public static Module newMathematicalAggregator(String name, int inputs, Operation op)
	{
		Module maModule = AC_Utility.createMathematicalAggregator(name, "", activeModule, inputs, op);

		treeView.addNode(maModule);
		drawingBoard.addMathematicalAggregator(maModule);
		loadPortsIntoModelBuilder(maModule);
		if (AC_Utility.isSubmoduleDefinition(activeModule.getModuleDefinition()))
		{
			AC_Utility.removeSubmoduleDefinition(activeModule.getModuleDefinition());
		}
		setSavedInACFile(false);
		return maModule;
	}
	/*
	public static Module newMathematicalAggregator(Module parent, String copasiData, int inputs, String op, GeneralGlyph glyph)
	{
		Operation operation = null;
		if (op.equals(Operation.PRODUCT.toString()))
		{
			operation = Operation.PRODUCT;
		}
		else if (op.equals(Operation.SUM.toString()))
		{
			operation = Operation.SUM;
		}
		else
		{
			System.err.println("AC_GUI.newMathematicalAggregator: A valid Operation was not found.");
		}
		
		Module maModule = CopasiUtility.importMathematicalAggregatorCopasiData(copasiData, parent, inputs, operation, glyph);

		treeView.addNode(maModule);
		drawingBoard.addMathematicalAggregator(maModule);
		loadPortsIntoModelBuilder(maModule);
		return maModule;
	}
	*/
	public static void newModuleInstance(String newModuleName, ModuleDefinition definition, Module parent)
	{
		Module mod = AC_Utility.instantiateModuleDefinition(newModuleName, definition, parent);
		if (mod == null)
		{
			return;
		}
		treeView.addNode(mod);
		drawingBoard.addModuleCell(mod);
		drawingBoard.changeModule(activeModule);
		modelBuilder.updatePorts();
		if (AC_Utility.isSubmoduleDefinition(parent.getModuleDefinition()))
		{
			AC_Utility.removeSubmoduleDefinition(parent.getModuleDefinition());
		}
		setSavedInACFile(false);
	}
	
	/**
	 * Remove the given module, and its children, from all three panels.
	 * @param mod the module to be removed
	 */
	public static void removeSubmodule(Module mod, boolean directDeletion)
	{
		Module parent = mod.getParent();
		if (directDeletion)
		{
			if (canModuleBeModified(mod))
			{
				setSelectedModule(mod.getParent());
				
				treeView.removeNode(mod.getTreeNode());
				
				ArrayList<ACComponentNode> ports;
				
				// get the list of ports
				ports = mod.getPorts();
				
				while(ports.size() > 0)
				{
					removePort((PortNode)ports.get(0), false);
				}

				drawingBoard.removeCell(mod.getDrawingCell());
				AC_Utility.deleteModule(mod);
				if ((parent != rootModule) && (parent.getChildren().size() == 0))
				{
					AC_Utility.addSubmoduleDefinition(parent.getModuleDefinition());
				}
				setSavedInACFile(false);
			}
			else
			{
				System.err.println("AC_GUI.removeSubmodule(): the module cannot be modified.");
			}
		}
		else
		{
			setSelectedModule(mod.getParent());
			
			treeView.removeNode(mod.getTreeNode());
			
			ArrayList<ACComponentNode> ports;
			
			// get the list of ports
			ports = mod.getPorts();
			
			while(ports.size() > 0)
			{
				removePort((PortNode)ports.get(0), false);
			}

			drawingBoard.removeCell(mod.getDrawingCell());
			AC_Utility.deleteModule(mod);
			if ((parent != rootModule) && (parent.getChildren().size() == 0))
			{
				AC_Utility.addSubmoduleDefinition(parent.getModuleDefinition());
			}
			setSavedInACFile(false);
		}
	}
	/*
	public static void addEquivalenceNode(Module parentMod, Object cell)
	{
		//Port sourcePort = (Port)((mxCell)cell).getSource().getValue();
		//Port targetPort = (Port)((mxCell)cell).getTarget().getValue();
		String refName = ((PortNode)((mxCell)cell).getSource().getValue()).getPortDefinition().getRefName();
		//refName = "E" + refName;
		EquivalenceNode eNode = AC_Utility.createEquivalence(refName, VariableType.SPECIES, parentMod);
		drawingBoard.addEquivalenceNode(eNode, cell);
		
		TerminalType sourceType = TerminalType.PORT;
		TerminalType targetType = TerminalType.EQUIVALENCE;
		mxCell sourceCell;
		mxCell targetCell = eNode.getDrawingCell();
		String drawingCellStyle = "ConectionEdge";
		
		//create the two connections for the equivalence node
		sourceCell = (mxCell)((mxCell)cell).getSource();
		ConnectionNode edge1 = AC_Utility.createConnection(parentMod, sourceCell, sourceType, targetCell, targetType, drawingCellStyle);
		drawingBoard.addComponentNodeCell(edge1);
		
		sourceCell = (mxCell)((mxCell)cell).getTarget();
		ConnectionNode edge2 = AC_Utility.createConnection(parentMod, sourceCell, sourceType, targetCell, targetType, drawingCellStyle);
		drawingBoard.addComponentNodeCell(edge2);
		
		//add a new species to msmb
		modelBuilder.addSpecies(refName);
	}
	*/
	
	public static void showVariable(String iRefName)
	{
		String refName = iRefName;
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
		
		PortNode port = AC_Utility.findPortMatch(refName, vType);
		if (port != null)
		{
			ACComponentNode node = null;
			mxCell source = null;
			TerminalType sourceType = null;
			mxCell target = null;
			TerminalType targetType = null;
			String drawingCellStyle = null;
			boolean noProblems = true;
			// the variable to show is linked to a PortNode
			switch (port.getPortDefinition().getType())
			{
				case INPUT:
					node = AC_Utility.createVisibleVariable(refName, vType, activeModule);
					drawingBoard.addComponentNodeCell(node);
					if (!AC_Utility.addNodetoRemainingInstances(activeModule, ((VisibleVariableNode)node).getVisibleVariableDefinition()))
					{
						System.err.println("Error AC_GUI.showVariable(): VisibleVariableNodes were not successfully added to all Module instances.");
					}
					source = port.getDrawingCell();
					sourceType = TerminalType.PORT;
					target = node.getDrawingCell();
					targetType = TerminalType.VISIBLEVARIABLE;
					drawingCellStyle = "DashedConnectionEdge";
					break;
				case OUTPUT:
					node = AC_Utility.createVisibleVariable(refName, vType, activeModule);
					drawingBoard.addComponentNodeCell(node);
					if (!AC_Utility.addNodetoRemainingInstances(activeModule, ((VisibleVariableNode)node).getVisibleVariableDefinition()))
					{
						System.err.println("Error AC_GUI.showVariable(): VisibleVariableNodes were not successfully added to all Module instances.");
					}
					source = node.getDrawingCell();
					sourceType = TerminalType.VISIBLEVARIABLE;
					target = port.getDrawingCell();
					targetType = TerminalType.PORT;
					drawingCellStyle = "ConnectionEdge";
					break;
				case EQUIVALENCE:
					node = AC_Utility.createEquivalence(refName, vType, activeModule);
					drawingBoard.addComponentNodeCell(node);
					if (!AC_Utility.addNodetoRemainingInstances(activeModule, ((EquivalenceNode)node).getEquivalenceDefinition()))
					{
						System.err.println("Error AC_GUI.showVariable(): EquivalenceNodes were not successfully added to all Module instances.");
					}
					source = node.getDrawingCell();
					sourceType = TerminalType.EQUIVALENCE;
					target = port.getDrawingCell();
					targetType = TerminalType.PORT;
					drawingCellStyle = "ConnectionEdge";
					break;
				default:
					noProblems = false;
			}
			// add a connection from the node to the port
			if (noProblems)
			{
				addConnection(activeModule, source, sourceType, target, targetType, drawingCellStyle);
			}
		}
		else
		{
			addVisibleVariable(iRefName);
		}
		setSavedInACFile(false);
		setSavedInACDataStructure(false);
		//addVisibleVariable(iRefName);
	}
	
	public static void addEquivalenceNode(Module parentMod, String refName, mxCell drawingCell)
	{
		EquivalenceNode eNode = AC_Utility.createEquivalence(refName, VariableType.SPECIES, drawingCell, parentMod);
		drawingBoard.setValue(drawingCell, eNode);
		drawingBoard.addComponentNodeCell(eNode);
		
		// check if species is listed in msmb
		if (!modelBuilder.isSpeciesName(refName))
		{
			// species is not listed, add a new species to msmb
			modelBuilder.addSpecies(refName);
		}
		if (!AC_Utility.addNodetoRemainingInstances(activeModule, eNode.getEquivalenceDefinition()))
		{
			System.err.println("Error AC_GUI.addEquivalenceNode(): EquivalenceNodes were not successfully added to all Module instances.");
		}
		setSavedInACFile(false);
		setSavedInACDataStructure(false);
	}
	
	public static void addEquivalenceNode(Module parentMod, String refName, GeneralGlyph glyph)
	{
		EquivalenceNode eNode = AC_Utility.createEquivalence(refName, VariableType.SPECIES, parentMod, glyph);
		drawingBoard.addComponentNodeCell(eNode);
		setSavedInACFile(false);
		setSavedInACDataStructure(false);
	}
	
	public static void removeEquivalenceNode(EquivalenceNode eNode, boolean directDeletion)
	{
		if (directDeletion)
		{
			drawingBoard.removeEdges(eNode.getDrawingCell());
			drawingBoard.removeCell(eNode.getDrawingCell());
			//modelBuilder.removeSpecies(eNode.getEquivalenceDefinition().getRefName());
			AC_Utility.deleteEquivalence(eNode);
			setSavedInACFile(false);
			setSavedInACDataStructure(false);
		}
		else
		{
			drawingBoard.removeEdges(eNode.getDrawingCell());
			drawingBoard.removeCell(eNode.getDrawingCell());
			//modelBuilder.removeSpecies(eNode.getEquivalenceDefinition().getRefName());
			AC_Utility.deleteEquivalence(eNode);
			setSavedInACFile(false);
			setSavedInACDataStructure(false);
		}
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
		
		VisibleVariableNode variableNode = AC_Utility.createVisibleVariable(refName, vType, activeModule);
		drawingBoard.addComponentNodeCell(variableNode);
		if (!AC_Utility.addNodetoRemainingInstances(activeModule, variableNode.getVisibleVariableDefinition()))
		{
			System.err.println("Error AC_GUI.addVisibleVariable(): VisibleVariableNodes were not successfully added to all Module instances.");
		}
		setSavedInACFile(false);
		setSavedInACDataStructure(false);
	}
	
	public static void addVisibleVariable(Module parentMod, String refName, mxCell drawingCell)
	{
		VisibleVariableNode variableNode = AC_Utility.createVisibleVariable(refName, VariableType.SPECIES, parentMod, drawingCell);
		drawingBoard.setValue(drawingCell, variableNode);
		drawingBoard.addComponentNodeCell(variableNode);
		
		// check if species is listed in msmb
		if (!modelBuilder.isSpeciesName(refName))
		{
			// species is not listed, add a new species to msmb
			modelBuilder.addSpecies(refName);
		}
		if (!AC_Utility.addNodetoRemainingInstances(activeModule, variableNode.getVisibleVariableDefinition()))
		{
			System.err.println("Error AC_GUI.addVisibleVariable(): VisibleVariableNodes were not successfully added to all Module instances.");
		}
		setSavedInACFile(false);
		setSavedInACDataStructure(false);
	}
	
	public static void addVisibleVariable(Module parentMod, String refName, String varType, GeneralGlyph glyph)
	{
		VariableType vType= null;
		if (varType.equals(VariableType.SPECIES.toString()))
		{
			vType = VariableType.SPECIES;
		}
		else if (varType.equals(VariableType.GLOBAL_QUANTITY.toString()))
		{
			vType = VariableType.GLOBAL_QUANTITY;
		}
		else
		{
			System.err.println("AC_GUI.addVisibleVariable: A valid VariableType was not found.");
		}
		
		VisibleVariableNode variableNode = AC_Utility.createVisibleVariable(refName, vType, parentMod, glyph);
		drawingBoard.addComponentNodeCell(variableNode);
		setSavedInACDataStructure(false);
	}
	
	public static void removeVisibleVariable(VisibleVariableNode var, boolean directDeletion)
	{
		if (directDeletion)
		{
			drawingBoard.removeEdges(var.getDrawingCell());
			drawingBoard.removeCell(var.getDrawingCell());
			//modelBuilder.removeSpecies(var.getVisibleVariableDefinition().getRefName());
			AC_Utility.deleteVisibleVariable(var);
			setSavedInACFile(false);
			setSavedInACDataStructure(false);
		}
		else
		{
			drawingBoard.removeEdges(var.getDrawingCell());
			drawingBoard.removeCell(var.getDrawingCell());
			//modelBuilder.removeSpecies(var.getVisibleVariableDefinition().getRefName());
			AC_Utility.deleteVisibleVariable(var);
			setSavedInACFile(false);
			setSavedInACDataStructure(false);
		}
	}
	
	/**
	 * Add a port to the given module.
	 * @param mod the module to add the port
	 * @param name the name of the port
	 * @param type the type of the port
	 */
	public static void addPort(Module parentMod, String refName, String name, PortType pType)
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
			System.err.println("AC_GUI.addPort: A valid VariableType was not found.");
		}
		
		PortNode pNode = AC_Utility.createPort(parentMod, refName, name, pType, vType);
		modelBuilder.addPort(pNode);
		drawingBoard.addPort(pNode);
		//drawingBoard.addPort(parentMod, pNode);
		if (!AC_Utility.addNodetoRemainingInstances(parentMod, pNode.getPortDefinition()))
		{
			System.err.println("Error AC_GUI.addPort(): PortNodes were not successfully added to all Module instances.");
		}
		setSavedInACFile(false);
		setSavedInACDataStructure(false);
	}
	
	public static void addPort(Module parentMod, String name, String refName, String portType, String varType, GeneralGlyph portGlyph)
	{
		PortType pType = null;
		VariableType vType= null;
		
		if (varType.equals(VariableType.SPECIES.toString()))
		{
			vType = VariableType.SPECIES;
		}
		else if (varType.equals(VariableType.GLOBAL_QUANTITY.toString()))
		{
			vType = VariableType.GLOBAL_QUANTITY;
		}
		else
		{
			System.err.println("AC_GUI.addPort: A valid VariableType was not found.");
		}

		if (portType.equals(PortType.INPUT.toString()))
		{
			pType = PortType.INPUT;
		}
		else if (portType.equals(PortType.OUTPUT.toString()))
		{
			pType = PortType.OUTPUT;
		}
		else if (portType.equals(PortType.EQUIVALENCE.toString()))
		{
			pType = PortType.EQUIVALENCE;
		}
		else
		{
			System.err.println("AC_GUI.addPort: A valid PortType was not found.");
		}
		
		PortNode newPort = AC_Utility.createPort(parentMod, refName, name, pType, vType, portGlyph);
		//modelBuilder.addPort(newPort);
		//drawingBoard.addPort(parentMod, newPort, portGlyph);
	}
	
	/**
	 * Remove the given port.
	 * @param port the port to be removed
	 */
	public static void removePort(PortNode pNode, boolean directDeletion)
	{
		if (directDeletion)
		{
			// remove connections from the port
			removeConnectionsFromPort(pNode);
			// remove the drawing cell representation from the drawing board
			drawingBoard.removeCell(pNode.getDrawingCell());
			// remove the port from the model builder
			modelBuilder.removePort(pNode);
			// remove the port node
			AC_Utility.deletePort(pNode);
			setSavedInACFile(false);
		}
		else
		{
			// remove connections from the port
			removeConnectionsFromPort(pNode);
			// remove the drawing cell representation from the drawing board
			drawingBoard.removeCell(pNode.getDrawingCell());
			// remove the port from the model builder
			modelBuilder.removePort(pNode);
			// remove the port node
			AC_Utility.deletePort(pNode);
			setSavedInACFile(false);
		}
	}
	
	public static void removeConnectionsFromPort(PortNode pNode)
	{
		// remove any edges connected to the port in the current view
		drawingBoard.removeEdges(pNode.getDrawingCell());
		// remove connections from the container module
		Module containerModule = pNode.getParent().getParent();
		if (containerModule != null)
		{
			// find connections attached to the port in the container module
			PortDefinition portDefinition = pNode.getPortDefinition();
			ListIterator<ConnectionNode> nodeList = containerModule.getConnections().listIterator();
			ConnectionNode connectionNode;
			ConnectionDefinition connectionDefinition;
			ArrayList<ConnectionNode> deletionList = new ArrayList<ConnectionNode>();
			while (nodeList.hasNext())
			{
				connectionNode = nodeList.next();
				connectionDefinition = connectionNode.getConnectionDefinition();
				if (connectionDefinition.getSourceType() == TerminalType.PORT)
				{
					if (connectionDefinition.getSourceDefinition() == portDefinition)
					{
						deletionList.add(connectionNode);
					}
				}
				
				if (connectionDefinition.getTargetType() == TerminalType.PORT)
				{
					if (connectionDefinition.getTargetDefinition() == portDefinition)
					{
						deletionList.add(connectionNode);
					}
				}
			}
			// delete connections attached to the port in the container module
			ListIterator<ConnectionNode> deletionListIterator = deletionList.listIterator();
			while (deletionListIterator.hasNext())
			{
				AC_Utility.deleteConnection(deletionListIterator.next());
			}
		}
	}
	
	/**
	 * Add a connection to the given module.
	 * @param parentMod the module to add a connection
	 * @param connectionCell the drawing cell representation of the connection
	 */
	public static void addConnection(Module parentMod, mxCell connectionCell, TerminalType sourceType, TerminalType targetType)
	{
		// make a connection object
		ConnectionNode edge = AC_Utility.createConnection(parentMod, connectionCell, sourceType, targetType);
		if (!AC_Utility.addNodetoRemainingInstances(parentMod, edge))
		{
			System.err.println("Error AC_GUI.addConnection(): ConnectionNodes were not successfully added to all Module instances.");
		}
		setSavedInACFile(false);
	}
	
	public static void addConnection(Module parentMod, mxCell source, TerminalType sourceType, mxCell target, TerminalType targetType, String drawingCellStyle)
	{
		//ConnectionNode edge = new ConnectionNode(parentMod);
		ConnectionNode edge = AC_Utility.createConnection(parentMod, source, sourceType, target, targetType, drawingCellStyle);
		drawingBoard.addComponentNodeCell(edge);
		if (!AC_Utility.addNodetoRemainingInstances(parentMod, edge))
		{
			System.err.println("Error AC_GUI.addConnection(): ConnectionNodes were not successfully added to all Module instances.");
		}
	}
	
	/**
	 * Remove the connection from the given module.
	 * @param parentMod the module containing the connection
	 * @param connectionCell the drawing cell representation of the connection to remove
	 */
	public static void removeConnection(ConnectionNode edge, boolean directDeletion)
	{
		if (directDeletion)
		{
			drawingBoard.removeCell(edge.getDrawingCell());
			AC_Utility.deleteConnection(edge);
		}
		else
		{
			drawingBoard.removeCell(edge.getDrawingCell());
			AC_Utility.deleteConnection(edge);
		}
		setSavedInACFile(false);
	}
	
	public static void updatePort(PortNode port, String value, int col)
	{
		switch (col)
		{
			case 1:
				port.getPortDefinition().setRefName(value);
				break;
			case 2:
				removeConnectionsFromPort(port);
				port.getPortDefinition().setType(PortType.valueOf(value.toUpperCase()));
				break;
			case 3:
				port.getPortDefinition().setName(value);
				break;
		}
		
		drawingBoard.updatePort(port.getDrawingCell());
		setSavedInACFile(false);
	}
	
	public static void updatePortInstantiations(PortNode port, String value, int col)
	{
		ModuleDefinition moduleDefinition = port.getPortDefinition().getParent();
		/*
		ListIterator<Module> instanceList = moduleDefinition.getInstances().listIterator();
		Module module;
		while (instanceList.hasNext())
		{
			
		}
		*/
		
		ListIterator<ACComponentDefinition> portList = moduleDefinition.getPorts().listIterator();
		PortDefinition portDefinition;
		while (portList.hasNext())
		{
			portDefinition = (PortDefinition)portList.next();
		}
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
		//System.out.println("Selected Module = " + mod.getName());
	}
	
	/**
	 * Select the module represented by the given tree node. 
	 * @param treeNode the treeView representation of the module to be selected
	 */
	public static void setSelectedModule(DefaultMutableTreeNode treeNode)
	{
		setSelectedModule((Module)treeNode.getUserObject());
	}
	
	/**
	 * Select the module represented by the given drawing cell.
	 * @param drawingCell the drawingBoard representation of the module to be selected
	 */
	public static void setSelectedModule(mxCell drawingCell)
	{
		setSelectedModule((Module)drawingCell.getValue());
	}
	
	public static void setSelectedDrawingBoardPort(PortNode portSelected)
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
		ListIterator<ACComponentNode> vars = activeModule.getVisibleVariables().listIterator();
		ListIterator<ACComponentNode> eNodes = activeModule.getEquivalences().listIterator();
		
		System.out.println("Selected variable type: " + vType.toString() + "...name: " + refName);
		VisibleVariableNode currentVar;
		while(vars.hasNext())
		{
			currentVar = (VisibleVariableNode)vars.next();
			if ((currentVar.getVisibleVariableDefinition().getVariableType() == vType) && currentVar.getVisibleVariableDefinition().getRefName().equalsIgnoreCase(refName))
			{
				drawingBoard.setSelected(currentVar.getDrawingCell());
				return;
			}
		}
		
		EquivalenceNode currenteNode;
		while(eNodes.hasNext())
		{
			currenteNode = (EquivalenceNode)eNodes.next();
			if((vType == VariableType.SPECIES) && currenteNode.getEquivalenceDefinition().getRefName().equalsIgnoreCase(refName))
			{
				drawingBoard.setSelected(currenteNode.getDrawingCell());
				return;
			}
		}
	}
	
	public static void loadModelBuilder(Module mod, boolean uneditable, boolean display)
	{
		//AC_GUI.modelBuilder.loadModel(mod.getMSMBData().getBytes(), uneditable, display);
		modelBuilder.loadModel(mod, true, uneditable, display);
		loadPortsIntoModelBuilder(mod);
	}
	
	public static void loadPortsIntoModelBuilder(Module mod)
	{
		//add activeModule's Ports
		ListIterator<ACComponentNode> portList = mod.getPorts().listIterator();
		while (portList.hasNext())
		{
			modelBuilder.addPort((PortNode)portList.next());
		}
		
		//add activeModule's Children's Ports
		ListIterator<Module> children = mod.getChildren().listIterator();
		while(children.hasNext())
		{
			portList = children.next().getPorts().listIterator();
			while (portList.hasNext())
			{
				modelBuilder.addPort((PortNode)portList.next());
			}
		}
	}
	
	public static void displaySubmoduleInfoView(Module mod, mxCell buttonCell)
	{
		drawingBoard.drawSubmoduleMiniComponents(mod, buttonCell);
		loadModelBuilder(mod, true, true);
	}
	
	public static void removeSubmoduleInfoView(mxCell buttonCell, boolean showActiveModule)
	{
		drawingBoard.removeSubmoduleMiniComponents(buttonCell);
		if (showActiveModule)
		{
			loadModelBuilder(AC_GUI.activeModule, false, true);
		}
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
	
	public static boolean canModuleBeModified(Module module)
	{
		ModuleDefinition definition = module.getModuleDefinition();
		int userInput;
		if (definition.getInstances().size() > 1)
		{
			userInput = AC_Utility.promptUserSubmoduleChange(module);
			byte[] code;
			switch (userInput)
			{
				case JOptionPane.YES_OPTION:
					// user chose to save a new module definition
					// copy the current module definition
					if (AC_Utility.copyDefinition(activeModule, null))
					{
						System.out.println("AC_GUI.canModuleBeModified(): definition copy success.");
						modelBuilder.setModuleDefinitionName(activeModule.getModuleDefinition().getName());
					}
					else
					{
						System.err.println("AC_GUI.canModuleBeModified(): definition copy failed.");
					}
					// save the updated msmb data
					code = modelBuilder.saveModel();
					if (code == null || code.length == 0)
					{
						System.err.println("AC_GUI.canModuleBeModified(): msmb data is NULL.");
					}
					activeModule.getModuleDefinition().setMSMBData(code);
					setSavedInACFile(false);
					break;
				case JOptionPane.NO_OPTION:
					// user chose to save the current module definition
					// check if the definition is external
					if (definition.isExternal())
					{
						int n = AC_Utility.promptUserExternalModuleChange(module);
						
						switch (n)
						{
							case  JOptionPane.YES_OPTION:
								definition.setExternal(false);
								setSavedInACFile(false);
								break;
							case  JOptionPane.NO_OPTION:
								return false;
								//break;
						}
					}
					/*
					// save the updated msmb data
					code = modelBuilder.saveModel();
					if (code == null || code.length == 0)
					{
						System.err.println("AC_GUI.canModuleBeModified(): msmb data is NULL.");
					}
					activeModule.getModuleDefinition().setMSMBData(code);
					*/
					break;
				case JOptionPane.CANCEL_OPTION:
					return false;
					//loadModelBuilder(activeModule, false, true);
					//setSavedInACDataStructure(true);
					//break;
			}
		}
		else
		{
			if (definition.isExternal())
			{
				userInput = AC_Utility.promptUserExternalModuleChange(module);
				
				switch (userInput)
				{
					case  JOptionPane.YES_OPTION:
						definition.setExternal(false);
						setSavedInACFile(false);
						break;
					case  JOptionPane.NO_OPTION:
						return false;
						//break;
				}
			}
		}
		
		return true;
	}
	
	public static boolean canModuleAddSubmodule(Module module)
	{
		ModuleDefinition definition = module.getModuleDefinition();
		int userInput;
		if (definition.getInstances().size() > 1)
		{
			userInput = AC_Utility.promptUserAddSubmodule(module);
			byte[] code;
			switch (userInput)
			{
				case JOptionPane.YES_OPTION:
					// user chose to save a new module definition
					// copy the current module definition
					if (AC_Utility.copyDefinition(activeModule, null))
					{
						System.out.println("AC_GUI.canModuleAddSubmodule(): definition copy success.");
						modelBuilder.setModuleDefinitionName(activeModule.getModuleDefinition().getName());
					}
					else
					{
						System.err.println("AC_GUI.canModuleBeModified(): definition copy failed.");
					}
					// save the updated msmb data
					code = modelBuilder.saveModel();
					if (code == null || code.length == 0)
					{
						System.err.println("AC_GUI.canModuleBeModified(): msmb data is NULL.");
					}
					activeModule.getModuleDefinition().setMSMBData(code);
					setSavedInACFile(false);
					break;
				case JOptionPane.NO_OPTION:
					return false;
					//break;
			}
		}
		else
		{
			if (definition.isExternal())
			{
				userInput = AC_Utility.promptUserExternalModuleChange(module);
				
				switch (userInput)
				{
					case  JOptionPane.YES_OPTION:
						definition.setExternal(false);
						setSavedInACFile(false);
						break;
					case  JOptionPane.NO_OPTION:
						return false;
						//break;
				}
			}
		}
		
		return true;
	}
	
	public static boolean canModuleRemoveSubmodule(Module module)
	{
		int userInput;
		ModuleDefinition definition = module.getModuleDefinition();
		if (definition.isExternal())
		{
			userInput = AC_Utility.promptUserExternalModuleChange(module);
			
			switch (userInput)
			{
				case  JOptionPane.YES_OPTION:
					definition.setExternal(false);
					setSavedInACFile(false);
					break;
				case  JOptionPane.NO_OPTION:
					return false;
					//break;
			}
		}

		return true;
	}
	
	/*
	public static void activeModuleChanged()
	{
		int n;
		setSavedInACDataStructure(false);
		if (activeModule.getModuleDefinition().isExternal())
		{
			n = AC_Utility.promptUserExternalModuleChange(activeModule);
			
		}
		if (AC_Utility.isSubmoduleDefinition(activeModule.getModuleDefinition()))
		{
			if (activeModule.getModuleDefinition().getInstances().size() > 1)
			{
				n = AC_Utility.promptUserSubmoduleChange(activeModule);
				
				applyActiveModuleChanges(n);
			}
		}
	}
	*/
	
	public static void applyActiveModuleChanges(int userInput)
	{
		/*
		if (n == JOptionPane.YES_OPTION)
		{
			AC_Utility.copyTemplate(activeModule.getModuleDefinition(), activeModule);
		}
		else if (n == JOptionPane.NO_OPTION)
		{
			
			if (n == JOptionPane.YES_OPTION)
			{
				activeModule.getModuleDefinition().setMSMBData(modelBuilder.saveModel());
				setSavedInACDataStructure(true);
			}
		}
		else if (n == JOptionPane.CANCEL_OPTION)
		{
			loadModelBuilder(activeModule, false, true);
			setSavedInACDataStructure(true);
		}
		*/
		byte[] code;
		switch (userInput)
		{
			case JOptionPane.YES_OPTION:
				// user chose to save a new module definition
				// copy the current module definition
				if (AC_Utility.copyDefinition(activeModule, null))
				{
					System.out.println("AC_GUI.applyActiveModuleChanged: definition copy success.");
					modelBuilder.setModuleDefinitionName(activeModule.getModuleDefinition().getName());
				}
				else
				{
					System.err.println("AC_GUI.applyActiveModuleChanged(): definition copy failed.");
				}
				// save the updated msmb data
				code = modelBuilder.saveModel();
				if (code == null || code.length == 0)
				{
					System.err.println("AC_GUI.applyActiveModuleChanges(): msmb data is NULL.");
				}
				activeModule.getModuleDefinition().setMSMBData(code);
				break;
			case JOptionPane.NO_OPTION:
				// user chose to save the current module definition
				// save the updated msmb data
				code = modelBuilder.saveModel();
				if (code == null || code.length == 0)
				{
					System.err.println("AC_GUI.applyActiveModuleChanges(): msmb data is NULL.");
				}
				activeModule.getModuleDefinition().setMSMBData(code);
				break;
			case JOptionPane.CANCEL_OPTION:
				loadModelBuilder(activeModule, false, true);
				setSavedInACDataStructure(true);
				break;
		}
	}
	
	public static void applyExternalModuleChange(int userInput)
	{
		switch(userInput)
		{
			case JOptionPane.YES_OPTION:
				//System.out.println("The user chose New Module.");
				activeModule.getModuleDefinition().setExternal(false);
				break;
			case JOptionPane.NO_OPTION:
				//System.out.print("The user chose Current Module.");
				activeModule.getModuleDefinition().setExternal(true);
				loadModelBuilder(activeModule, false, true);
				setSavedInACDataStructure(true);
				break;
		}
	}
	
	public static void changeActiveModule(Module mod)
	{
		if (activeModule != null)
		{
			byte[] code = modelBuilder.saveModel();
			activeModule.getModuleDefinition().setMSMBData(code);
			
			if (drawingBoard.getActiveSubmoduleButtonCell() != null)
			{
				// the submodule info view is currently active
				removeSubmoduleInfoView(drawingBoard.getActiveSubmoduleButtonCell(), false);
			}
			//System.out.println(activeModule.getName() + " saved to COPASI: " + modelBuilder.saveToCK(activeModule.getKey()));
		}
			
		activeModule = mod;
		drawingBoard.changeModule(mod);
		treeView.refreshTree();
		//int cCount = mod.getConnections().size();
		//System.out.println("Number of connnections: " + cCount);
		//modelBuilder.loadModel(mod.getKey());
		byte[] newCode = mod.getModuleDefinition().getMSMBData();
		if (newCode != null && newCode.length > 0)
		{
			// load from msmb data
			//byte[] data = mod.getModuleDefinition().getMSMBData().getBytes();
			//modelBuilder.loadModel(data, false, true);
			modelBuilder.loadModel(mod, true, false, true);
		}
		else
		{
			// load from copasi data structure
			modelBuilder.loadModel(mod, false, false, true);
			mod.getModuleDefinition().setMSMBData(modelBuilder.saveModel());
		}		
		
		// populate the Ports tab in Model Builder
		loadPortsIntoModelBuilder(activeModule);
		modelBuilder.setVisible(true);
		setSelectedModule(mod);
		setSavedInACDataStructure(true);
	}
	
	public static void close()
	{
		if (rootModule != null)
		{
			if (!modelSavedInACFile)
			{
				//Object[] options = {"Yes","No","Cancel"};
				int userInput = JOptionPane.showOptionDialog(null,
						"Model \""+ activeModule.getName() +"\" has been modified. Do you want to save the changes?",
						"Question",
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null, null, null);
						//options,
						//options[0]);
				
				switch (userInput)
				{
					case JOptionPane.YES_OPTION:
						if (lastLoadSave_file != null)
						{
							AC_GUI.save(AC_GUI.activeModule, lastLoadSave_file);
							JOptionPane.showMessageDialog(null, "The module has been saved in " + lastLoadSave_file);
						}
						else
						{
							String fileName = null;
							File file;
							JFileChooser fileChooser = new JFileChooser (new File ("."));
							fileChooser.setFileFilter (new FileNameExtensionFilter("Model file (.ac)","ac"));
							while (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
							{
								file = fileChooser.getSelectedFile();
								fileName = file.getName();
								try
								{
									if (file.exists())
									{
										String msg = "A file named \"" + fileName + "\" already exists.  Do you want to replace it?";
										int n = JOptionPane.showConfirmDialog(null, msg, "", JOptionPane.OK_CANCEL_OPTION);
										if (n == JOptionPane.OK_OPTION)
										{
											fileName = file.getAbsolutePath();
											if (!fileName.endsWith(".ac"))
											{
												fileName += ".ac";
											}
											AC_GUI.save(AC_GUI.rootModule, fileName);
											JOptionPane.showMessageDialog(null, "The module has been saved in " + fileName);
											break;
										}
									}
									else
									{
										fileName = file.getAbsolutePath();
										if (!fileName.endsWith(".ac"))
										{
											fileName += ".ac";
										}
										AC_GUI.save(AC_GUI.rootModule, fileName);
										JOptionPane.showMessageDialog(null, "The module has been saved in " + fileName);
										break;
									}
								}
								catch (Exception e)
								{
									System.err.println("AC_GUI.exit(): save failed.");
									e.printStackTrace();
								}
							}
						}
						break;
					case JOptionPane.NO_OPTION:
						// do nothing
						break;
					case JOptionPane.CANCEL_OPTION:
						return;
				}
			}
		}
		treeView.clear();
		drawingBoard.clear();
		modelBuilder.clear();
		CopasiUtility.clear();
		AC_Utility.reset();
		rootModule = null;
		activeModule = null;
		selectedModule = null;
		lastLoadSave_file = null;
		setSavedInACDataStructure(false);
		setSavedInACFile(false);
	}
	
	public static void exit()
	{
		if (rootModule != null)
		{
			if (!modelSavedInACFile)
			{
				//Object[] options = {"Yes","No","Cancel"};
				int userInput = JOptionPane.showOptionDialog(null,
						"Model \""+ activeModule.getName() +"\" has been modified. Do you want to save the changes?",
						"Question",
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null, null, null);
						//options,
						//options[0]);
				
				switch (userInput)
				{
					case JOptionPane.YES_OPTION:
						if (lastLoadSave_file != null)
						{
							AC_GUI.save(AC_GUI.activeModule, lastLoadSave_file);
							JOptionPane.showMessageDialog(null, "The module has been saved in " + lastLoadSave_file);
						}
						else
						{
							String fileName = null;
							File file;
							JFileChooser fileChooser = new JFileChooser (new File ("."));
							fileChooser.setFileFilter (new FileNameExtensionFilter("Model file (.ac)","ac"));
							while (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
							{
								file = fileChooser.getSelectedFile();
								fileName = file.getName();
								try
								{
									if (file.exists())
									{
										String msg = "A file named \"" + fileName + "\" already exists.  Do you want to replace it?";
										int n = JOptionPane.showConfirmDialog(null, msg, "", JOptionPane.OK_CANCEL_OPTION);
										if (n == JOptionPane.OK_OPTION)
										{
											fileName = file.getAbsolutePath();
											if (!fileName.endsWith(".ac"))
											{
												fileName += ".ac";
											}
											AC_GUI.save(AC_GUI.rootModule, fileName);
											JOptionPane.showMessageDialog(null, "The module has been saved in " + fileName);
											break;
										}
									}
									else
									{
										fileName = file.getAbsolutePath();
										if (!fileName.endsWith(".ac"))
										{
											fileName += ".ac";
										}
										AC_GUI.save(AC_GUI.rootModule, fileName);
										JOptionPane.showMessageDialog(null, "The module has been saved in " + fileName);
										break;
									}
								}
								catch (Exception e)
								{
									System.err.println("AC_GUI.exit(): save failed.");
									e.printStackTrace();
								}
							}
						}
						break;
					case JOptionPane.NO_OPTION:
						// do nothing
						break;
					case JOptionPane.CANCEL_OPTION:
						return;
				}
			}
		}
		saveRecentFiles();
		System.gc();
		System.exit(0);
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

		file_RecentFiles = Constants.RECENT_FILE_NAME;
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
		ac_version = getVersionFromFile();
		this.setTitle(Constants.TOOL_NAME_FULL + " - version " + ac_version);
		
		addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent evt) {
		        exit();
		    }
		});
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.pack();
	}

	/**
	 * Setup the menu items.
	 */
	private void initializeMenuItems()
	{
		ACMenuListener menuListener = new ACMenuListener();
		
		// File
		fileMenu = new JMenu("File");
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
		recentFilesMenu = new JMenu("Recent Files");
		fileMenu.add(recentFilesMenu);
		loadRecentFiles();
		//recentFilesMenu.add(makeMenuItem(MenuItem.RECENT, menuListener, -1));
		fileMenu.addSeparator();
		
		fileMenu.add(makeMenuItem(MenuItem.EXIT, menuListener, -1));

		// Module
		moduleMenu = new JMenu("Module");
		moduleMenu.add(makeMenuItem(MenuItem.ADD_SUBMODULE_NEW, menuListener, -1));
		moduleMenu.add(makeMenuItem(MenuItem.ADD_SUBMODULE_LOAD, menuListener, -1));
		moduleMenu.addSeparator();
		moduleMenu.add(makeMenuItem(MenuItem.ADD_SUMMATION_MODULE, menuListener, -1));
		moduleMenu.add(makeMenuItem(MenuItem.ADD_PRODUCT_MODULE, menuListener, -1));
		moduleMenu.addSeparator();
		//moduleMenu.add(makeMenuItem(MenuItem.SAVE_SUBMODULE_AS_TEMPLATE, menuListener, -1));
		//moduleMenu.addSeparator();
		moduleMenu.add(makeMenuItem(MenuItem.REMOVE_SUBMODULE, menuListener, -1));

		// Tools
		toolsMenu = new JMenu("Tools");
		toolsMenu.add(makeMenuItem(MenuItem.VALIDATE_MODEL, menuListener, -1));
		toolsMenu.addSeparator();
		//toolsMenu.add(makeMenuItem(MenuItem.VIEW_MODEL, menuListener, -1));
		//toolsMenu.add(makeMenuItem(MenuItem.FLATTEN_MODEL, menuListener, -1));
		//toolsMenu.addSeparator();
		//toolsMenu.add(makeMenuItem(MenuItem.DECOMPOSE_INTO_MODULES, menuListener, -1));
		
		// Help
		helpMenu = new JMenu("Help");
		helpMenu.add(makeMenuItem(MenuItem.HELP_CONTENTS, menuListener, -1));
		helpMenu.add(makeMenuItem(MenuItem.ABOUT_AGGREGATION_CONNECTOR, menuListener, -1));

		// Add items to the menu bar
		menuBar = new JMenuBar();
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
	
	private static void loadRecentFiles() 
	{	
		BufferedReader fin;
		String strLine;
		if(file_RecentFiles.length() == 0)
		{
			return;
		}
		
		try
		{
			fin = new BufferedReader(new FileReader(file_RecentFiles));
			while ((strLine = fin.readLine()) != null)
			{
				 addRecentFile(strLine);
			}
			fin.close();
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			recentFiles = new Vector(5);
		}
	}
	
	private static void addRecentFile(String fileName)
	{
		File file = new File(fileName);
		
		//a file that has been moved or deleted is not going to be shown in the list
		if(!file.exists())
		{
			return;
		}
		
		int index = recentFiles.indexOf(file.getAbsolutePath());
		if(index!= -1)
		{
			recentFiles.remove(index);
			recentFilesMenu.remove(recentFilesMenu.getItemCount()-index-1);
		}
		
		if(recentFiles.size() > 11)
		{
				recentFiles.remove(0);
				recentFilesMenu.remove(0);
		}
		
		
		
		recentFiles.add(file.getAbsolutePath());
		JMenuItem item = new JMenuItem(file.getName());
		//item.addActionListener(new RecentItemActionListener(f));
    
		recentFilesMenu.add(item,0);
		recentFilesMenu.validate();
	}
	
	private static void saveRecentFiles()
	{
		BufferedWriter out; 
		try
		{
			out = new BufferedWriter(new FileWriter(file_RecentFiles));
			
			for(int i = 0; i < recentFiles.size(); i++)
			{
				out.write(recentFiles.get(i).toString());
				out.write(AC_Utility.eol);
			}
			out.flush();
			out.close();
		}
		catch (Exception e)
		{
			System.err.println("Trouble writing recentFiles directories: "+ e);
			e.printStackTrace();
		}
	}
	
	private static boolean saveModules()
	{
		boolean successfulSave = true;
		byte[] code;
		
		code = modelBuilder.saveModel();
		if (code != null)
		{
			activeModule.getModuleDefinition().setMSMBData(code);
		}
		else
		{
			System.err.println("Error saving msmb data.");
			return false;
		}
		
		//System.out.println("The CopasiDataModel list before saving module definition: " + activeModule.getModuleDefinition().getName());
		//CopasiUtility.printDataModelList();
		if (!modelBuilder.saveToCopasi(activeModule.getModuleDefinition().getName()))
		{
			System.err.println("Error saving copasi data.");
			return false;
		}
		//System.out.println("The CopasiDataModel list after saving module definition: " + activeModule.getModuleDefinition().getName());
		//CopasiUtility.printDataModelList();
		
		// load and save each of the activeModule's children
		ListIterator<Module> children = activeModule.getChildren().listIterator();
		ArrayList<String> savedDefinitions = new ArrayList<String>();
		Module currentModule;
		String moduleName;
		String definitionName;
		while (children.hasNext())
		{
			currentModule = children.next();
			moduleName = currentModule.getName();
			definitionName = currentModule.getModuleDefinition().getName();
			if (savedDefinitions.contains(definitionName))
			{
				// the current definition has already been saved
				continue;
			}
			code = currentModule.getModuleDefinition().getMSMBData();
			if (code != null)
			{
				modelBuilder.loadModel(currentModule, true, false, false);
			}
			else
			{
				System.err.println("Error loading " + moduleName + " msmb data.");
				successfulSave = false;
				break;
			}
			
			if (!modelBuilder.saveToCopasi(definitionName))
			{
				System.err.println("Error saving " + moduleName + " copasi data.");
				successfulSave = false;
				break;
			}
			savedDefinitions.add(definitionName);
		}
		
		// reload the original activeModule
		code = activeModule.getModuleDefinition().getMSMBData();
		//data = activeModule.getModuleDefinition().getMSMBData().getBytes();
		//modelBuilder.loadModel(data, false, true);
		modelBuilder.loadModel(activeModule, true, false, true);
		loadPortsIntoModelBuilder(activeModule);
		return successfulSave;
	}
	
	private static boolean oldsaveModules()
	{
		boolean successfulSave = true;
		Module child;
		byte[] code;
		//byte[] data;
		CCopasiDataModel dmodel;
		
		System.out.println();
		System.out.println("The CopasiDataModel list before saving module definition: " + activeModule.getModuleDefinition().getName());
		CopasiUtility.printDataModelList();
		// save the activeModule to COPASI
		//System.out.println("AC_GUI.saveModules(): " + activeModule.getName() + "'s copasi key = " + activeModule.getKey());
		dmodel = CopasiUtility.getCopasiModelFromModelName(activeModule.getModuleDefinition().getName());
		if (dmodel == null)
		{
			System.out.println("The COPASI datamodel for " + activeModule.getModuleDefinition().getName() + " is null BEFORE saving.");
		}
		successfulSave = modelBuilder.saveToCopasi(activeModule.getModuleDefinition().getName());
		dmodel = CopasiUtility.getCopasiModelFromModelName(activeModule.getModuleDefinition().getName());
		if (dmodel == null)
		{
			System.out.println("The COPASI datamodel for " + activeModule.getModuleDefinition().getName() + " is null AFTER saving.");
		}
		if (!successfulSave)
		{
			return false;
		}
		System.out.println("The CopasiDataModel list after saving model: " + activeModule.getName());
		CopasiUtility.printDataModelList();
		
		// store the activeModule's msmb data
		code = modelBuilder.saveModel();
		activeModule.getModuleDefinition().setMSMBData(code);
		
		// load and save each of the activeModule's children
		ListIterator<Module> children = activeModule.getChildren().listIterator();
		while (children.hasNext())
		{
			child = children.next();
			
			code = child.getModuleDefinition().getMSMBData();
			if (code != null)
			{
				//data = child.getModuleDefinition().getMSMBData().getBytes();
				//modelBuilder.loadModel(data, false, false);
				modelBuilder.loadModel(child, true, false, false);
			}
			else
			{
				//System.err.println("AC_GUI.saveModules(): " + child.getName() + "'s msmb data is NULL.");
				//System.exit(0);
				//modelBuilder.loadModel(child.getKey(), false, false);
				modelBuilder.loadModel(child, false, false, false);
				
				// store the child module's msmb data
				code = modelBuilder.saveModel();
				child.getModuleDefinition().setMSMBData(code);
			}		
			
			System.out.println("The CopasiDataModel list before saving model: " + child.getName());
			CopasiUtility.printDataModelList();
			
			// save the child module to COPASI
			//System.out.println("AC_GUI.saveModules(): " + child.getName() + "'s copasi key = " + child.getKey());
			dmodel = CopasiUtility.getCopasiModelFromModelName(child.getModuleDefinition().getName());
			if (dmodel == null)
			{
				System.err.println("The COPASI datamodel for " + child.getName() + " is null BEFORE saving.");
			}
			successfulSave =  modelBuilder.saveToCopasi(child.getModuleDefinition().getName());
			dmodel = CopasiUtility.getCopasiModelFromModelName(child.getModuleDefinition().getName());
			if (dmodel == null)
			{
				System.out.println("The COPASI datamodel for " + child.getName() + " is null AFTER saving.");
			}
			if (!successfulSave)
			{
				return false;
			}
			System.out.println("The CopasiDataModel list after saving model: " + child.getName());
			CopasiUtility.printDataModelList();
			// store the child module's msmb data
			//code = new String(modelBuilder.saveModel());
			//child.setMSMBData(code);
		}
		
		// reload the original activeModule
		code = activeModule.getModuleDefinition().getMSMBData();
		//data = activeModule.getModuleDefinition().getMSMBData().getBytes();
		//modelBuilder.loadModel(data, false, true);
		modelBuilder.loadModel(activeModule, true, false, true);
		loadPortsIntoModelBuilder(activeModule);
		
		//CopasiUtility.printDataModelList();
		return successfulSave;
	}
	
	private static void setSavedInACDataStructure(boolean value)
	{
		modelSavedInACDataStructure = value;
	}
	
	public static void setSavedInACFile(boolean value)
	{
		modelSavedInACFile = value;
	}
}
