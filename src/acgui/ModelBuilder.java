package acgui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.MenuElement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;

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
	private Module loadedModule;
	PortsPanel portsPanel;
	//private static ACCustomJTable jTableCustom;
	//private static ACCustomTableModel tableModel;
	//private Vector<String> refNames;
	
	
	/**
	 * Construct the model builder object.
	 */
	public ModelBuilder()
	{
		msmb = new MainGui(true);
		loadedModule = null;
		//refNames = new Vector<String>();
		addPortTab();
		installListeners();
	}
	
	/**
	 * Load the given Copasi model into the model builder.
	 * @param key the unique Copasi key referencing the model
	 */
	public void loadModel(Module mod, boolean fromMSMBData, boolean uneditable, boolean display)
	{
		//System.out.println("Start ModelBuilder.load(" + mod.getModuleDefinition().getName() + "). Number of Copasi data models: " + CopasiUtility.getNumberOfModels());
		if (fromMSMBData)
		{
			//System.out.println("Number of copasi datamodels: " + CopasiUtility.getNumberOfModels());
			msmb.loadFromMSMB(mod.getModuleDefinition().getMSMBData(), uneditable);
			//System.out.println("Number of copasi datamodels: " + CopasiUtility.getNumberOfModels());
		}
		else
		{
			try 
			{
				 msmb.loadFromCopasiModelName(mod.getModuleDefinition().getName(), uneditable);
			} catch (Exception e) {
				 //I still don't know which exception I need to push to your part... probably it is enough for me to catch them and
				 //display the usual error message that I already show... but I'm not sure, so I left the throw expception in the
				 //method declaration
				 e.printStackTrace();
			}
		}
		
		if (display)
		{
			portsPanel.clear();
			updateRefNameColumn();
			portsPanel.setUneditable(uneditable);
			msmb.setModelName(mod.getName());
			msmb.setModelDefinition(mod.getModuleDefinition().getName());
			setVisible(true);
		}
		loadedModule = mod;
	}
	
	public void loadModel(byte[] data, Module setLoadedModule)
	{
		msmb.loadFromMSMB(data, false);
		loadedModule = setLoadedModule;
	}
	
	public byte[] saveModel()
	{
		return msmb.saveToMSMB();
	}
	
	public boolean saveToCopasi(String name)
	{
		//System.out.println("Save COPASI Model name: " + name);
		return msmb.saveToCopasiModelName(name);
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
		return portsPanel.getRefNames();
	}
	
	public String getValue(String name)
	{
		Object msmbRef = null;
		
		msmbRef = msmb.getMSMB_getSpecies(name);
		if (msmbRef != null)
		{
			return msmb.getMSMB_getSpecies(name).getInitialQuantity_listString();
		}
		
		msmbRef = msmb.getMSMB_getGlobalQuantity(name);
		if (msmbRef != null)
		{
			return msmb.getMSMB_getGlobalQuantity(name).getInitialValue();
		}
		
		return name;
	}
	
	public void addPort(PortNode newPort)
	{
		portsPanel.addPort(newPort);
	}
	
	public void removePort(PortNode port)
	{
		portsPanel.removePort(port);
	}
	
	public void updatePorts()
	{
		portsPanel.clear();
		updateRefNameColumn();
		
		if (AC_GUI.activeModule != null)
		{	
			//add activeModule's Ports
			ListIterator<ACComponentNode> portList = AC_GUI.activeModule.getPorts().listIterator();
			while (portList.hasNext())
			{
				portsPanel.addPort((PortNode)portList.next());
			}
			
			//add activeModule's Children's Ports
			ListIterator<Module> children = AC_GUI.activeModule.getChildren().listIterator();
			while(children.hasNext())
			{
				portList = children.next().getPorts().listIterator();
				while (portList.hasNext())
				{
					portsPanel.addPort((PortNode)portList.next());
				}
			}
		}
	}
	
	public void addSpecies(String speciesName)
	{
		String speciesQuantity =  msmb.getDefault_SpeciesInitialQuantity();
		String compartment = msmb.getDefault_CompartmentName();
		try 
		{
			msmb.addSpecies(speciesName, speciesQuantity, compartment);
		} 
		catch (Exception e) 
		{
			//something went wrong (e.g. the species already existed or some other error that I cannot think about now)
			//you can popup an error message or something similar.
			e.printStackTrace();
		}
	}
	
	public void removeSpecies(String speciesName)
	{
		try 
		{
			msmb.removeSpecies(speciesName);
		} 
		catch (Exception e) 
		{
			//you can popup an error message or something similar.
			e.printStackTrace();
			System.out.println("Error removing the species " + speciesName + " from the msmb panel.");
		}
	}
	
	public void setSelectedPort(PortNode port)
	{
		//getMSMB_MainTabPanel() gets the JTabbed panel
		JTabbedPane tabPanel = msmb.getMSMB_MainTabPanel();
		int lastTabIndex = tabPanel.getTabCount() - 1;
		tabPanel.setSelectedIndex(lastTabIndex);
		portsPanel.setSelectedPort(port);
	}
	
	public void setSelectedVariable(String name, VariableType vType)
	{
		/*
		JTabbedPane tabPanel = msmb.getMSMB_MainTabPanel();
		int tabPanelIndex = 0;
		
		switch(vType)
		{
		case SPECIES:
			tabPanelIndex = 1;
			break;
		case GLOBAL_QUANTITY:
			tabPanelIndex = 2;
			break;
		}
		
		tabPanel.setSelectedIndex(tabPanelIndex);
		*/
		MSMB_Element type = null;
		switch(vType)
		{
		case SPECIES:
			type = MSMB_Element.SPECIES;
			break;
		case GLOBAL_QUANTITY:
			type = MSMB_Element.GLOBAL_QUANTITY;
			break;
		}
		msmb.highlightElement(type, name);
	}
	
	public int getSelectedTabIndex()
	{
		return msmb.getMSMB_MainTabPanel().getSelectedIndex();
	}
	
	public void setModuleName(String name)
	{
		msmb.setModelName(name);
	}

	public void setModuleDefinitionName(String name)
	{
		msmb.setModelDefinition(name);
	}
	
	public String getNameFromPortTable(int row)
	{
		return portsPanel.getPortName(row);
	}
	
	public PortNode getPortFromPortTable(int row)
	{
		return portsPanel.getPort(row);
	}
	
	public boolean isSpeciesListEmpty()
	{
		return msmb.getMSMB_listOfSpecies().isEmpty();
	}
	
	public Module getLoadedModule()
	{
		return loadedModule;
	}
	
	public void clear()
	{
		portsPanel.clear();
		setVisible(false);
		loadedModule = null;
	}
	
	public boolean isSpeciesName(String name)
	{
		return msmb.getMSMB_listOfSpecies().contains(name);
	}
	
	public int validateModel()
	{
		return msmb.validateMSMB();
	}
	
	private void containerModuleNameModification(MSMB_Element nameType, String oldName, String newName)
	{
		String message = "The root module is unique in that both the module name " + AC_Utility.eol;
		message += "and module template name must be the same." + AC_Utility.eol;
		AC_Utility.displayMessage(JOptionPane.INFORMATION_MESSAGE, "Root Module", message);
		
		if (AC_Utility.editModuleNameValidation(loadedModule, newName, true) &&
				AC_Utility.moduleDefinitionNameValidation(newName, true))
		{
			// name change is valid
			// first, set the names in modelbuilder
			if (nameType == MSMB_Element.MODEL_NAME)
			{
				// the module name field has already been changed in modelbuilder,
				// now set the module definition name field in modelbuilder
				setModuleDefinitionName(newName);
			}
			else
			{
				// the module definition name field has already been changed in modelbuilder,
				// now set the module name field in modelbuilder
				setModuleDefinitionName(newName);
			}
			// next, set the names in the rest of JCMC
			AC_Utility.changeModuleName(loadedModule, newName, true);
			AC_Utility.changeModuleDefinitionName(loadedModule.getModuleDefinition(), newName, true);
		}
		else
		{
			// name change is invalid
			// reset the appropriate name field in modelbuilder
			if (nameType == MSMB_Element.MODEL_NAME)
			{
				// the module name field was changed,
				// set it back to the previous name
				setModuleName(oldName);
			}
			else
			{
				// the module definition name field was changed,
				// set it back to the previous name
				setModuleDefinitionName(oldName);
			}
		}
	}
	
	/**
	 * Display the Port tab with the rest of the MSMB panel.
	 */
	private void addPortTab()
	{
		//getMSMB_MainTabPanel() gets the JTabbed panel
		JTabbedPane tabPanel = msmb.getMSMB_MainTabPanel();
		//now you can add your ports tab as a normal jtabbedpanel
		portsPanel = new PortsPanel(msmb.getCustomFont());
		tabPanel.addTab("Ports", null, portsPanel, null);
		/*
		JPanel panelPorts = new JPanel();
		panelPorts.setLayout(new BorderLayout());
		 
		JScrollPane jScrollPaneTablePorts = new JScrollPane();
		Vector<String> col = new Vector<String>();
		col.add("Ref Name");
		//col.add("Ref Type");
		//col.add("Port?");
		col.add("Port Type");
		col.add("Port Name");
		
		tableModel = new ACCustomTableModel("PortsTableModel", false);
		tableModel.setColumnNames(col, new Vector());
		tableModel.initializeTableModel();
		
		//jTableCustom = new CustomJTable();
		jTableCustom = new ACCustomJTable();
		jTableCustom.initializeCustomTable(tableModel);
		jTableCustom.setModel(tableModel);
		jTableCustom.getTableHeader().setFont(msmb.getCustomFont()); //font for the header
		jTableCustom.setCustomFont(msmb.getCustomFont()); // font for the content
		*/
		
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
		
		/*
		updateRefNameColumn();
		setupPortTypeColumn();
		*/
		
		//jScrollPaneTablePorts.setViewportView(jTableCustom);
		
		//Add specific listeners
		/*
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
							if(before!=null) System.out.println("Species before1 = " + before.getName());
							if(after!=null)  System.out.println("Species after1 = " + after.getName());
						}
					}, 
					MSMB_Element.SPECIES);
		   
		   msmb.addChangeListener(
					new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							ChangedElement before = (((MSMB_InterfaceChange)e.getSource()).getElementBefore());
							ChangedElement after = (((MSMB_InterfaceChange)e.getSource()).getElementAfter());
							if(before!=null) System.out.println("Model name before1 = " + before.getName());
							if(after!=null)  System.out.println("Model name after1 = " + after.getName());
						}
					}, 
					MSMB_Element.MODEL);
		*/
		//panelPorts.add(jScrollPaneTablePorts, BorderLayout.CENTER);
	}
	
	/**
	 * Update the items in the Ref Name column.
	 */
	private void updateRefNameColumn()
	{
		updateRefNames();
		portsPanel.updateRefNameColumn();
	}
	
	/**
	 * Clear and reload the refNames list.
	 * The list is a combination of the list of species and list global quantities from the MSMB.
	 */
	private void updateRefNames()
	{
		Vector<String> species;
		Vector<String> newSpeciesList = null;
		Vector<String> globalq;
		Vector<String> newGlobalQList = null;
		Vector<String> newRefNames = new Vector<String>();
		
		try
		{
			species = msmb.getMSMB_listOfSpecies();
			newSpeciesList = new Vector<String>(species.size());
			for (int i = 0; i < species.size(); i++)
			{
				newSpeciesList.add(i, species.get(i) + " - Species");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("Error updating Species list.");
		}
		
		try
		{
			globalq = msmb.getMSMB_listOfGlobalQuantities();
			newGlobalQList = new Vector<String>(globalq.size());
			for (int i = 0; i < globalq.size(); i++)
			{
				newGlobalQList.add(i, globalq.get(i) + " - Module Quantity");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("Error updating GlobalQ list.");
		}
		
		if ((newSpeciesList != null) && (newGlobalQList != null))
		{
			newRefNames.addAll(newSpeciesList);
			newRefNames.addAll(newGlobalQList);
			portsPanel.updateRefNames(newRefNames);
		}
		else
		{
			System.err.println("Error in ModelBuilder.updateRefNames().");
		}
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
		
	private void installListeners()
	{
		
		msmb.addChangeListener(
				new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						ChangedElement before = (((MSMB_InterfaceChange)e.getSource()).getElementBefore());
						ChangedElement after = (((MSMB_InterfaceChange)e.getSource()).getElementAfter());
						if(before!=null) System.out.println("Species before = " + before.getName());
						if(after!=null)  System.out.println("Species after = " + after.getName());
						AC_Utility.changeName(before, after);
					}
				}, 
				MSMB_Element.SPECIES);
		
		msmb.addChangeListener(
				new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						ChangedElement before = (((MSMB_InterfaceChange)e.getSource()).getElementBefore());
						ChangedElement after = (((MSMB_InterfaceChange)e.getSource()).getElementAfter());
						if(before!=null) System.out.println("Global Quantity before = " + before.getName());
						if(after!=null)  System.out.println("Global Quantity after = " + after.getName());
						AC_Utility.changeName(before, after);
					}
				}, 
				MSMB_Element.GLOBAL_QUANTITY);
		
		msmb.addChangeListener(
				new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						ChangedElement before = (((MSMB_InterfaceChange)e.getSource()).getElementBefore());
						ChangedElement after = (((MSMB_InterfaceChange)e.getSource()).getElementAfter());
						if(before!=null) System.out.println("Model name before = " + before.getName());
						if(after!=null)  System.out.println("Model name after = " + after.getName());
						if ((before != null) && (after != null))
						{
							if (before.getName().equals(after.getName()))
							{
								return;
							}
						}
						if (loadedModule == AC_GUI.rootModule)
						{
							containerModuleNameModification(MSMB_Element.MODEL_NAME, before.getName(), after.getName());
						}
						else if (AC_Utility.editModuleNameValidation(loadedModule, after.getName(), true))
						{
							AC_Utility.changeModuleName(loadedModule, after.getName(), true);
						}
						else
						{
							setModuleName(before.getName());
						}
					}
				}, 
				MSMB_Element.MODEL_NAME);
		
		msmb.addChangeListener(
				new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						ChangedElement before = (((MSMB_InterfaceChange)e.getSource()).getElementBefore());
						ChangedElement after = (((MSMB_InterfaceChange)e.getSource()).getElementAfter());
						if(before!=null) System.out.println("Model Definition name before = " + before.getName());
						if(after!=null)  System.out.println("Model Definition name after = " + after.getName());
						if ((before != null) && (after != null))
						{
							if (before.getName().equals(after.getName()))
							{
								return;
							}
						}
						if (loadedModule == AC_GUI.rootModule)
						{
							containerModuleNameModification(MSMB_Element.MODEL_DEFINITION, before.getName(), after.getName());
						}
						else if (AC_Utility.moduleDefinitionNameValidation(after.getName(), true))
						{
							if (loadedModule.getModuleDefinition().getInstances().size() > 1)
							{
								int userInput = AC_Utility.promptUserSubmoduleChange(AC_GUI.activeModule);
								
								byte[] code;
								switch (userInput)
								{
									case JOptionPane.YES_OPTION:
										// user chose to save a new module definition
										// copy the current module definition
										if (AC_Utility.copyDefinition(loadedModule, after.getName()))
										{
											System.out.println("ModelBuilder.Model_Definition_Name_Changed: definition copy success.");
											//setModuleDefinitionName(activeModule.getModuleDefinition().getName());
											//AC_Utility.changeModuleDefinitionName(AC_GUI.activeModule.getModuleDefinition(), after.getName(), true);
										}
										else
										{
											System.err.println("ModelBuilder.Model_Definition_Name_Changed: definition copy failed.");
										}
										// save the updated msmb data
										code = saveModel();
										if (code == null || code.length == 0)
										{
											System.err.println("ModelBuilder.Model_Definition_Name_Changed: msmb data is NULL.");
										}
										loadedModule.getModuleDefinition().setMSMBData(code);
										if (!saveToCopasi(loadedModule.getModuleDefinition().getName()))
										{
											System.err.println("ModelBuilder.Model_Definition_Name_Changed: copasi datamodel not saved.");
										}
										break;
									case JOptionPane.NO_OPTION:
										// user chose to save the current module definition
										// change the definition name
										AC_Utility.changeModuleDefinitionName(loadedModule.getModuleDefinition(), after.getName(), true);
										// save the updated msmb data
										code = saveModel();
										if (code == null || code.length == 0)
										{
											System.err.println("ModelBuilder.Model_Definition_Name_Changed: msmb data is NULL.");
										}
										loadedModule.getModuleDefinition().setMSMBData(code);
										break;
									case JOptionPane.CANCEL_OPTION:
										//loadModelBuilder(activeModule, false, true);
										setModuleDefinitionName(before.getName());
										//setSavedInACDataStructure(true);
										break;
								}
							}
							else
							{
								AC_Utility.changeModuleDefinitionName(loadedModule.getModuleDefinition(), after.getName(), true);
							}
						}
						else
						{
							setModuleDefinitionName(before.getName());
						}
					}
				}, 
				MSMB_Element.MODEL_DEFINITION);
		
		msmb.addChangeListener(
				new ChangeListener() {// my part will call this state change.. this code the  actions you want to do once the change is triggered
					@Override
					public void stateChanged(ChangeEvent e) { 
						//you don't need the before after, but you know that something has been changed, so you need to ask for the new value
						Font newFont = msmb.getCustomFont();
						portsPanel.updateFont(newFont);
						/*
						jTableCustom.getTableHeader().setFont(newFont);
						//any other action that you need to do on your side when the size of the font is changed
						jTableCustom.setCustomFont(newFont);
						*/
					}
				}, 
				MSMB_Element.FONT);
		
		// add a selection listener for the species table
		msmb.addChangeListener(
                new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                    	if (AC_GUI.activeModule == loadedModule)
                    	{
	                        ChangedElement before = (((MSMB_InterfaceChange)e.getSource()).getElementBefore());
	                        ChangedElement after = (((MSMB_InterfaceChange)e.getSource()).getElementAfter());
	                        //the before of the selection is always going to because you don't need the before for the selection I guess
	                        if(before!=null) System.out.println("Species selection before = " + before.getName());
	                        if(after!=null && !after.getName().isEmpty())
	                        {
	                        	AC_GUI.setSelectedDrawingBoardVariable(after.getName(), VariableType.SPECIES);
	                        	System.out.println("Species selection after = " + after.getName());
	                        }
	                        System.out.println("AC select species after");
                    	}
                    }
                },
                MSMB_Element.SELECTED_SPECIES);
		
		// add a selection listener for the global quantity table
		msmb.addChangeListener(
                new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                    	if (AC_GUI.activeModule == loadedModule)
                    	{
	                        ChangedElement before = (((MSMB_InterfaceChange)e.getSource()).getElementBefore());
	                        ChangedElement after = (((MSMB_InterfaceChange)e.getSource()).getElementAfter());
	                        //the before of the selection is always going to because you don't need the before for the selection I guess
	                        if(before!=null) System.out.println("GLQ selection before = " + before.getName());
	                        if(after!=null && !after.getName().isEmpty())
	                        {
	                        	AC_GUI.setSelectedDrawingBoardVariable(after.getName(), VariableType.GLOBAL_QUANTITY);
	                        	System.out.println("GLQ selection after = " + after.getName());
	                        }
	                        System.out.println("AC select GLQ after");
                    	}
                    }
                },
                MSMB_Element.SELECTED_GLOBAL_QUANTITY);
		
		// add a listener for when something has been changed in the ModelBuilder
		msmb.addChangeListener(
				new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						//ChangedElement before = (((MSMB_InterfaceChange)e.getSource()).getElementBefore());
						//ChangedElement after = (((MSMB_InterfaceChange)e.getSource()).getElementAfter());
						//if(before!=null) System.out.println("Model name before = " + before.getName());
						//if(after!=null)  System.out.println("Model name after = " + after.getName());
						System.out.println("Something has changed in the ModelBuilder.");
						//AC_GUI.activeModuleChanged();
						if (AC_GUI.canModuleBeModified(AC_GUI.activeModule))
						{
							// save the updated msmb data
							byte[] code = saveModel();
							if (code == null || code.length == 0)
							{
								System.err.println("ModelBuilder.installListeners(): something changed, msmb data is NULL.");
							}
							AC_GUI.activeModule.getModuleDefinition().setMSMBData(code);
							AC_GUI.setSavedInACFile(false);
						}
						else
						{
							AC_GUI.loadModelBuilder(AC_GUI.activeModule, false, true);
						}
					}
				}, 
				MSMB_Element.SOMETHING_CHANGED);
		
		// add selection listener for the ports table
		//jTableCustom.getSelectionModel().addListSelectionListener(new ACRowListener());
	}
}

