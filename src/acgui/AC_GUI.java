package acgui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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

/**
 * Aggregation Connector. This tool is used to connect SBML models together.
 * 
 * @author T.C. Jones
 * @version June 27, 2012
 */
public class AC_GUI extends JFrame
{
	private static final long serialVersionUID = 1L;

	protected final static String MENU_NEW = "New"; // a root model created
	protected final static String MENU_OPEN = "Open"; // a root model opened from file
	protected final static String MENU_RECENT = "Recent Files";
	protected final static String MENU_SAVE = "Save";
	protected final static String MENU_SAVE_AS = "Save As";
	protected final static String MENU_CLOSE = "Close"; // close the model and get back to new
	protected final static String MENU_EXIT = "Exit"; // exit application
	// -------------------------------------------------------------------------------------
	protected final static String MENU_ADD_SUBMODULE = "Add Submodule";
	protected final static String MENU_REMOVE_SUBMODULE = "Remove Submodule";
	// -------------------------------------------------------------------------------------
	protected final static String MENU_HELP = "Help Contents";
	protected final static String MENU_ABOUT = "About Aggregation Connector";

	protected static AC_GUI currentGUI;
	protected static DrawingBoard drawingBoard;
	protected static TreeView treeView;
	protected static ModuleList masterModuleList;
	protected static Module selectedModule;
	protected static boolean isModuleOpen;

	/**
	 * Construct the AC_GUI object.
	 */
	public AC_GUI()
	{
		super("Aggregation Connector");
		// System.out.println("AC_GUI constructor");
		//moduleList = new ModuleList();
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
	 * Create a new module in the three panels.
	 * @param name the name of the new module
	 */
	public void newModule(String name)
	{
		masterModuleList = new ModuleList();
		Module mod = new Module(name);
		masterModuleList.add(mod);
		treeView.setup(mod);
		//drawingBoard.setup(mod);
		drawingBoard.createCell(mod);
		drawingBoard.changeModule(mod);
		
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
		drawingBoard.removeCell(mod.getDrawingCell());
		mod.getParent().removeChild(mod);
		//moduleList.remove(mod);
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
	 * Select the module represented by the given drawing cell,
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
		JPanel modelBuilderPanel = new JPanel();
		JScrollPane modelBuilderWindow = new JScrollPane(modelBuilderPanel);

		// The aggregation window
		drawingBoard = new DrawingBoard();
		// drawingBoard.newModel();
		// JScrollPane aggregationWindow = new JScrollPane(drawingBoard);
		// aggregationWindow.setOpaque(true);

		// The tree window
		treeView = new TreeView();
		JScrollPane treeWindow = new JScrollPane(treeView);
		treeWindow.setOpaque(true);

		initializeMenuItems();

		// JSplitPane verticalLine = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeWindow, aggregationWindow);
		JSplitPane verticalLine = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeWindow, drawingBoard);
		//verticalLine.setDividerLocation(180 + verticalLine.getInsets().left);
		Dimension dim = new Dimension(180, 500);
		treeWindow.setMinimumSize(dim);
		JSplitPane horizontalLine = new JSplitPane(JSplitPane.VERTICAL_SPLIT, verticalLine, modelBuilderWindow);
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
		fileMenu.add(makeMenuItem(MENU_NEW, menuListener, KeyEvent.VK_N));
		fileMenu.add(makeMenuItem(MENU_OPEN, menuListener, KeyEvent.VK_O));
		// recentMenuItem = new JMenu("Recent Files");
		// this.loadRecentFiles();
		fileMenu.addSeparator();
		fileMenu.add(makeMenuItem(MENU_SAVE, menuListener, KeyEvent.VK_S));
		fileMenu.add(makeMenuItem(MENU_SAVE_AS, menuListener, -1));
		fileMenu.addSeparator();
		fileMenu.add(makeMenuItem(MENU_CLOSE, menuListener, -1));
		fileMenu.addSeparator();
		fileMenu.add(makeMenuItem(MENU_EXIT, menuListener, -1));

		// Module
		JMenu moduleMenu = new JMenu("Module");
		moduleMenu.add(makeMenuItem(MENU_ADD_SUBMODULE, menuListener, -1));
		moduleMenu.addSeparator();
		moduleMenu.add(makeMenuItem(MENU_REMOVE_SUBMODULE, menuListener, -1));

		// Help
		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(makeMenuItem(MENU_HELP, menuListener, -1));
		helpMenu.add(makeMenuItem(MENU_ABOUT, menuListener, -1));

		// Add items to the menu bar
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(moduleMenu);
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
	private JMenuItem makeMenuItem(String name, ActionListener aListener, int keyEvent)
	{
		int defaultShortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		JMenuItem menuItem = new JMenuItem(name);
		menuItem.setActionCommand(name);
		menuItem.addActionListener(aListener);
		if (keyEvent != -1)
		{
			menuItem.setMnemonic(keyEvent);
			menuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, defaultShortcutMask));
		}
		return menuItem;
	}
}
