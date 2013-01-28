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

import org.COPASI.*;

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
	protected static ModuleList masterModuleList;
	protected static Module selectedModule;
	protected static boolean isModuleOpen;
	private CopasiUtility copasiUtility;
	private ModelBuilder modelBuilder;

	/**
	 * Construct the AC_GUI object.
	 */
	public AC_GUI()
	{
		super("Aggregation Connector");
		//moduleList = new ModuleList();
		copasiUtility = new CopasiUtility();
		initializeComponents();
		isModuleOpen = false;
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
		CCopasiDataModel dataModel = copasiUtility.createDataModel();
		try
		{
			dataModel.importSBML(fileName);
			System.out.println("Number of models in the CCopasiRootContainer: " +CCopasiRootContainer.getDatamodelList().size());
		}
		catch (java.lang.Exception ex){
			ex.printStackTrace();
			System.err.println( "Error while importing the model from file named \"" + fileName + "\"." );
		}
		
		Module mod = new Module(fileName, dataModel.getModel().getKey());
		masterModuleList.add(mod);
		treeView.setup(mod);
		drawingBoard.createCell(mod);
		drawingBoard.changeModule(mod);
		
		modelBuilder.loadModel(mod.getKey());
		modelBuilder.setVisible(true);
	}
	
	/**
	 * Create a new module in the three panels.
	 * @param name the name of the new module
	 */
	public void newModule(String name)
	{
		masterModuleList = new ModuleList();
		CCopasiDataModel dataModel = copasiUtility.createDataModel();
		Module mod = new Module(name, dataModel.getModel().getKey());
		masterModuleList.add(mod);
		treeView.setup(mod);
		drawingBoard.createCell(mod);
		drawingBoard.changeModule(mod);
		modelBuilder.loadModel(mod.getKey());
		modelBuilder.setVisible(true);		
		
		isModuleOpen = true;
	}
	
	/**
	 * Create a new submodule in the three panels.
	 * @param name the name of the new submodule
	 */
	public void newSubmodule(String name, Module parent)
	{
		Module mod = new Module(name, parent);
		parent.addChild(mod);
		treeView.addNode(mod);
		drawingBoard.createCell(mod);
		drawingBoard.addCell(mod);
		//moduleList.add(mod);
		//printList();
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
		
		if (ports.size() != 0)
		{
			for(int i = 0; i < ports.size(); i++)
			{
				removePort(ports.get(0));
			}
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
	public void addPort(Module parentMod, String name, PortType type)
	{
		Port newPort = new Port(parentMod, type, name);
		drawingBoard.addPort(parentMod, newPort);
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
		drawingBoard.removeEdges(port);
		// remove the drawing cell representation from the drawing board
		drawingBoard.removeCell(port.getDrawingCell());
		// remove the port from the parent module
		parentMod.removePort(port);	
	}
	
	/**
	 * Add a connection to the given module.
	 * @param parentMod the module to add a connection
	 * @param connectionCell the drawing cell representation of the connection
	 */
	public void addConnection(Module parentMod, Object connectionCell)
	{
		// make a connection object
		Connection edge = new Connection(parentMod, connectionCell);
		// set the connection as the user object of the drawing cell
		((mxCell)connectionCell).setValue(edge);
		// add the edge to the parent module
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
	
	/**
	 * Select the representation of the given module on the treeView and drawingBoard.
	 * @param mod the module to be selected
	 */
	public void setSelectedModule(Module mod)
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
		treeWindow.setMinimumSize(dim);
		//JSplitPane horizontalLine = new JSplitPane(JSplitPane.VERTICAL_SPLIT, verticalLine, modelBuilderWindow);
		JSplitPane horizontalLine = new JSplitPane(JSplitPane.VERTICAL_SPLIT, verticalLine, modelBuilderPanel);
		//horizontalLine.setDividerLocation(610 + horizontalLine.getInsets().top);
		//System.out.println("Vertical line: " + verticalLine.getDividerLocation());
		//System.out.println("Horizontal line: " + horizontalLine.getDividerLocation());
		
		
		this.add(horizontalLine);

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
		fileMenu.addSeparator();
		fileMenu.add(makeMenuItem(MenuItem.CLOSE, menuListener, -1));
		fileMenu.addSeparator();
		fileMenu.add(makeMenuItem(MenuItem.EXIT, menuListener, -1));

		// Module
		JMenu moduleMenu = new JMenu("Module");
		moduleMenu.add(makeMenuItem(MenuItem.ADD_SUBMODULE_NEW, menuListener, -1));
		moduleMenu.add(makeMenuItem(MenuItem.ADD_SUBMODULE_TEMPLATE, menuListener, -1));
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
}
