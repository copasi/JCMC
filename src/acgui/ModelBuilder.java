package acgui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.MenuElement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;

import msmb.commonUtilities.ChangedElement;
import msmb.commonUtilities.MSMB_Element;
import msmb.commonUtilities.MSMB_Interface;
import msmb.commonUtilities.MSMB_InterfaceChange;
import msmb.commonUtilities.MSMB_MenuItem;
import msmb.commonUtilities.tables.CustomJTable;
import msmb.commonUtilities.tables.CustomTableModel;
import msmb.gui.MainGui;


/**
 * The model builder panel in the aggregation connector.
 * 
 * @author T.C. Jones
 * @version December 17, 2012
 */
public class ModelBuilder
{	

	final MSMB_Interface msmb;
	private static CustomJTable jTableCustom;
	private static CustomTableModel cTableModel;
	private Vector<String> refNames;
	
	/**
	 * Construct the model builder object.
	 */
	public ModelBuilder()
	{
		msmb = new MainGui();
		refNames = new Vector<String>();
		addPortTab();
	}
	
	/**
	 * Load the given Copasi model into the model builder.
	 * @param key the unique Copasi key referencing the model
	 */
	public void loadModel(String key)
	{
		try 
		{
			 msmb.loadFromCopasiKey(key);
		} catch (Exception e) {
			 //I still don't know which exception I need to push to your part... probably it is enough for me to catch them and
			 //display the usual error message that I already show... but I'm not sure, so I left the throw expception in the
			 //method declaration
			 e.printStackTrace();
		}
		cTableModel.clearData();
		updateRefNameColumn();
	}
	
	/**
	 * Return the MSMB panel.
	 * @return the MSMB panel
	 */
	public JTabbedPane getPanel()
	{
		return msmb.getMSMB_MainTabPanel();
	}
	
	/**
	 * Set the visibility of the MSMB panel.
	 * @param vis the new visibility of the MSMB panel
	 */
	public void setVisible(boolean vis)
	{
		msmb.getMSMB_MainTabPanel().setVisible(vis);
	}
	
	/**
	 * Return the number of species in the current MSMB.
	 * @return the number of species in the current MSMB
	 */
	public int getNumberofSpecies()
	{
		return 0;
	}
	
	/**
	 * Return the list of Ref Names.
	 * @return the list of Ref Names
	 */
	public Vector<String> getRefNames()
	{
		updateRefNames();
		return refNames;
	}
	
	public void addPort(Port newPort)
	{
		Vector portInfo = new Vector();
		portInfo.add(newPort.getRefName());
		portInfo.add(newPort.getType());
		portInfo.add(newPort.getName());
		cTableModel.addRow(portInfo);
	}
	
	public void removePort(Port port)
	{
		cTableModel.removeRow(port.getRowIndex());
	}
	
	/**
	 * Display the Port tab with the rest of the MSMB panel.
	 */
	private void addPortTab()
	{
		//getMSMB_MainTabPanel() gets the JTabbed panel
		JTabbedPane tabPanel = msmb.getMSMB_MainTabPanel();
		//now you can add your ports tab as a normal jtabbedpanel
		JPanel panelPorts = new JPanel();
		panelPorts.setLayout(new BorderLayout());
		 
		JScrollPane jScrollPaneTablePorts = new JScrollPane();
		Vector<String> col = new Vector<String>();
		col.add("Ref Name");
		//col.add("Ref Type");
		//col.add("Port?");
		col.add("Port Type");
		col.add("Port Name");
		
		cTableModel = new CustomTableModel("PortsTableModel", false);
		cTableModel.setColumnNames(col, new Vector());
		cTableModel.initializeTableModel();
		
		jTableCustom = new CustomJTable();
		jTableCustom.initializeCustomTable(cTableModel);
		jTableCustom.setModel(cTableModel);
		jTableCustom.getTableHeader().setFont(msmb.getCustomFont()); //font for the header
		jTableCustom.setCustomFont(msmb.getCustomFont()); // font for the content
			
		
		//Setup the RefName column
		/*
		SortedComboBoxModel sortedModel = new SortedComboBoxModel(refNames, null);
		JComboBox<String> comboBox = new JComboBox<String>(sortedModel);
		TableColumn refNameColumn = jTableCustom.getColumnModel().getColumn(2);
		refNameColumn.setCellEditor(new DefaultCellEditor(comboBox));
		
		jScrollPaneTablePorts.setViewportView(jTableCustom);
		*/
		
		/*
		if (!refNames.addAll(msmb.getMSMB_listOfSpecies()))
		{
			System.out.println("Error adding list of species to RefNames column.");
			System.out.print("List of Species size: ");
			System.out.println(msmb.getMSMB_listOfSpecies().size());
		}
		if (!refNames.addAll(msmb.getMSMB_listOfGlobalQuantities()))
		{
			System.out.println("Error adding list of global quantities to RefNames column.");
			System.out.print("List of Global Quantities size: ");
			System.out.println(msmb.getMSMB_listOfGlobalQuantities().size());
		}
		*/
		updateRefNameColumn();
		setupPortTypeColumn();
			
		jScrollPaneTablePorts.setViewportView(jTableCustom);
		//Add specific listeners
		 msmb.addChangeListener(
					new ChangeListener() {// my part will call this state change.. this code the  actions you want to do once the change is triggered
						@Override
						public void stateChanged(ChangeEvent e) { 
							//you don't need the before after, but you know that something has been changed, so you need to ask for the new value
							Font newFont = msmb.getCustomFont();
							jTableCustom.getTableHeader().setFont(newFont);
							//any other action that you need to do on your side when the size of the font is changed
							jTableCustom.setCustomFont(newFont);
							
						}
					}, 
					MSMB_Element.FONT);
		   
		
		   msmb.addChangeListener(
					new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							ChangedElement before = (((MSMB_InterfaceChange)e.getSource()).getElementBefore());
							ChangedElement after = (((MSMB_InterfaceChange)e.getSource()).getElementAfter());
							if(before!=null) System.out.println("Species before = " + before.getName());
							if(after!=null)  System.out.println("Species after = " + after.getName());
						}
					}, 
					MSMB_Element.SPECIES);
		   
		   msmb.addChangeListener(
					new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							ChangedElement before = (((MSMB_InterfaceChange)e.getSource()).getElementBefore());
							ChangedElement after = (((MSMB_InterfaceChange)e.getSource()).getElementAfter());
							if(before!=null) System.out.println("Model name before = " + before.getName());
							if(after!=null)  System.out.println("Model name after = " + after.getName());
						}
					}, 
					MSMB_Element.MODEL);
		
		panelPorts.add(jScrollPaneTablePorts, BorderLayout.CENTER);	
		tabPanel.addTab("Ports", null, panelPorts, null);
	}
	
	/**
	 * Update the items in the Ref Name column.
	 */
	private void updateRefNameColumn()
	{
		updateRefNames();
		
		SortedComboBoxModel sortedModel = new SortedComboBoxModel(refNames, null);
		JComboBox<String> comboBox = new JComboBox<String>(sortedModel);
		TableColumn refNameColumn = jTableCustom.getColumnModel().getColumn(1);
		refNameColumn.setCellEditor(new DefaultCellEditor(comboBox));
	}
	
	/**
	 * Clear and reload the refNames list.
	 * The list is a combination of the list of species and list global quantities from the MSMB.
	 */
	private void updateRefNames()
	{
		//reset and update the refNames list
		refNames.clear();
		refNames.addAll(msmb.getMSMB_listOfSpecies());
		refNames.addAll(msmb.getMSMB_listOfGlobalQuantities());
	}
	
	/**
	 * Setup the items in the Port Type column.
	 */
	private void setupPortTypeColumn()
	{
		Vector<String> portTypes = new Vector<String>();
		portTypes.add("Input");
		portTypes.add("Output");
		portTypes.add("Equivalence");
		
		JComboBox<String> comboBox = new JComboBox<String>(portTypes);
		TableColumn portTypeColumn = jTableCustom.getColumnModel().getColumn(2);
		portTypeColumn.setCellEditor(new DefaultCellEditor(comboBox));
	}

	public void openPreferencesMSMB() {
		JMenuBar bar = msmb.getMSMB_MenuBar();
		
		MenuElement[] menus = bar.getSubElements();
		for(int i = 0; i < menus.length; i++) {
			String element = ((JMenu)menus[i]).getText();
			MSMB_MenuItem selection = MSMB_MenuItem.getEnum(element);
			if(selection ==MSMB_MenuItem.FILE) {
				JMenu fileMenu = (JMenu)menus[i];
				for(int i2 = 0; i2 < fileMenu.getItemCount(); i2++) {
								JMenuItem item = fileMenu.getItem(i2);
								if(item!=null) {
									element =item.getText();
									MSMB_MenuItem selection2 = MSMB_MenuItem.getEnum(element);
									if(selection2!= null && selection2 == MSMB_MenuItem.PREFERENCES) {
										item.doClick();
									}
								}
						}
				}
			}
		}
		
		
	}

