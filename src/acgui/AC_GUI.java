package acgui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

/**
 * Aggregation Connector. This tool is used to connect SBML models together.
 * 
 * @author T.C. Jones
 * @version June 27, 2012
 */
public class AC_GUI extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;

	private final static String MENU_NEW = "New"; // a root model created
	private final static String MENU_OPEN = "Open"; // a root model opened from file
	private final static String MENU_RECENT = "Recent Files";
	// -------------------------------------------------------------------------------------
	private final static String MENU_SAVE = "Save";
	private final static String MENU_SAVE_AS = "Save As";
	// -------------------------------------------------------------------------------------
	private final static String MENU_CLOSE = "Close"; // close the model and get back to new
	// -------------------------------------------------------------------------------------
	private final static String MENU_EXIT = "Exit"; // exit application
	private final static String MENU_HELP = "Help Contents";
	private final static String MENU_ABOUT = "About Aggregation Connector";

	/**
	 * Construct the AC_GUI object.
	 */
	public AC_GUI()
	{
		super("Aggregation Connector");
		System.out.println("AC_GUI constructor");
		initializeComponents();
		this.setVisible(true);
	}

	/**
	 * Detect which action occurred and perform the appropriate task.
	 * 
	 * @param ae the action event
	 */
	@Override
	public void actionPerformed(ActionEvent ae)
	{
		String command = ae.getActionCommand();

		if (command.equals(MENU_NEW))
		{
			JOptionPane.showMessageDialog(null, "An empty model will be created (not yet implemented).");
		}
		else if (command.equals(MENU_OPEN))
		{
			JOptionPane.showMessageDialog(null, "An existing model will be opened from a SBML file and will load the three panels accordingly (not yet implemented).");
		}
		else if (command.equals(MENU_RECENT))
		{
			JOptionPane.showMessageDialog(null, "Will show recently opened list of files (not yet implemented).");
		}
		else if (command.equals(MENU_SAVE))
		{
			JOptionPane.showMessageDialog(null, "Will save the entire model in one SBML file (not yet implemented).");
		}
		else if (command.equals(MENU_SAVE_AS))
		{
			JOptionPane.showMessageDialog(null, "Will save the entire model in one SBML file (not yet implemented).");
		}
		else if (command.equals(MENU_CLOSE))
		{
			JOptionPane.showMessageDialog(null, "Will give the option to save the model as a SBML file, unload/clear all three panels, and go back to the empty screen (not yet implemented).");
		}
		else if (command.equals(MENU_EXIT))
		{
			JOptionPane.showMessageDialog(null, "Will exit from the tool after the completing the steps described under the Close menu item (not yet implemented).");
		}
		else if (command.equals(MENU_HELP))
		{
			JOptionPane.showMessageDialog(null, "Will display some sort of help tool (not yet implemented).");
		}
		else if (command.equals(MENU_ABOUT))
		{
			JOptionPane.showMessageDialog(null, "Will give information about the tool (not yet implemented).");
		}
	}

	/**
	 * Starts the tool.
	 * 
	 * @param args command line arguments
	 */
	public static void main(String[] args)
	{
		AC_GUI currentGUI = new AC_GUI();
		currentGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		currentGUI.setSize(900, 800);
		// make the frame full screen
		currentGUI.setExtendedState(JFrame.MAXIMIZED_BOTH);
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
		JPanel aggregationPanel = new JPanel();
		JScrollPane aggregationWindow = new JScrollPane(aggregationPanel);
		aggregationWindow.setOpaque(true);

		// The tree window
		JPanel treePanel = new JPanel();
		JScrollPane treeWindow = new JScrollPane(treePanel);
		treeWindow.setOpaque(true);

		initializeMenuItems();

		JSplitPane verticalLine = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeWindow, aggregationWindow);
		verticalLine.setDividerLocation(180 + verticalLine.getInsets().left);
		JSplitPane horizontalLine = new JSplitPane(JSplitPane.VERTICAL_SPLIT, verticalLine, modelBuilderWindow);
		horizontalLine.setDividerLocation(610 + horizontalLine.getInsets().top);
		this.add(horizontalLine);

		this.pack();
	}

	/**
	 * Setup the menu items.
	 */
	private void initializeMenuItems()
	{
		// File
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(makeMenuItem(MENU_NEW, this, KeyEvent.VK_N));
		fileMenu.add(makeMenuItem(MENU_OPEN, this, KeyEvent.VK_O));
		// recentMenuItem = new JMenu("Recent Files");
		// this.loadRecentFiles();
		fileMenu.addSeparator();
		fileMenu.add(makeMenuItem(MENU_SAVE, this, KeyEvent.VK_S));
		fileMenu.add(makeMenuItem(MENU_SAVE_AS, this, -1));
		fileMenu.addSeparator();
		fileMenu.add(makeMenuItem(MENU_CLOSE, this, -1));
		fileMenu.addSeparator();
		fileMenu.add(makeMenuItem(MENU_EXIT, this, -1));

		// Help
		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(makeMenuItem(MENU_HELP, this, -1));
		helpMenu.add(makeMenuItem(MENU_ABOUT, this, -1));

		// Add items to the menu bar
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
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
