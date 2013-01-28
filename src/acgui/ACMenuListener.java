package acgui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import msmb.utility.Constants;

/**
 * Listener for the AC_GUI menu.
 * @author T.C. Jones
 * @version July 5, 2012
 */
public class ACMenuListener implements ActionListener
{

	/**
	 * Detect which action occurred and perform the appropriate task.
	 * @param ae the action event
	 */
	@Override
	public void actionPerformed(ActionEvent ae)
	{
		String command = ae.getActionCommand();
		command = command.replace("(", "");
		command = command.replace(")", "");
		command = command.replaceAll(" ", "_");
		MenuItem selection = MenuItem.valueOf(command.toUpperCase());
		String name;
		Module parentMod;
		DefaultMutableTreeNode node;
		DefaultMutableTreeNode parent;
		
		
		switch(selection)
		{
		case NEW:
			//JOptionPane.showMessageDialog(null, "An empty model will be created (not yet implemented).");
			name = null;
			if (AC_GUI.isModuleOpen == false)
			{
				name = JOptionPane.showInputDialog("Name of the new module:", "Module");
				if (name != null)
				{
					AC_GUI.currentGUI.newModule(name);
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null,
						"Please save and close the current module first.");
			}
			break;
		case OPEN:
			/*
			JOptionPane.showMessageDialog(
					null,
					"An existing model will be opened from a SBML file and will load the three panels accordingly (not yet implemented).");
			*/
			JFileChooser fileChooser = new JFileChooser();
    		
    		int returnVal = fileChooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
            	File file = fileChooser.getSelectedFile();
                //inputFile = file.getName().substring(0,file.getName().lastIndexOf("."));
                AC_GUI.currentGUI.load(file.getName());
            }
			break;
		case RECENT:
			JOptionPane.showMessageDialog(null,
					"Will show recently opened list of files (not yet implemented).");
			break;
		case SAVE:
			/*
			JOptionPane.showMessageDialog(null,
					"Will save the entire model in one SBML file (not yet implemented).");
			*/
			Printer output = new Printer();
			String fileName = output.print(AC_GUI.drawingBoard.getActiveModule());
			JOptionPane.showMessageDialog(null, "The module has been saved in " + fileName + ".");
			break;
		case SAVE_AS:
			JOptionPane.showMessageDialog(null,
					"Will save the entire model in one SBML file (not yet implemented).");
			break;
		case CLOSE:
			JOptionPane.showMessageDialog(
					null,
					"Will give the option to save the model as a SBML file, unload/clear all three panels, and go back to the empty screen (not yet implemented).");
			break;
		case EXIT:
			JOptionPane.showMessageDialog(
					null,
					"Will exit from the tool after the completing the steps descri bed under the Close menu item (not yet implemented).");
			break;
		case ADD_SUBMODULE_NEW:
			//JOptionPane.showMessageDialog(null, "Will add an empty submodule under the module selected in the TreeView (not yet implemented).");
			name = null;
			//node = AC_GUI.selectedModule.getTreeNode();
			parentMod = null;

			if (AC_GUI.isModuleOpen != false)
			{
				if (AC_GUI.selectedModule != null)
				{
					//parent = AC_GUI.moduleList.findModule(node);
					parentMod = AC_GUI.selectedModule;
					if (parentMod == AC_GUI.drawingBoard.getActiveModule())
					{
						name = JOptionPane.showInputDialog("Name of the new submodule:", "Submodule");
						if (name != null)
						{
							AC_GUI.currentGUI.newSubmodule(name, parentMod);
						}
					}
					else
					{
						JOptionPane.showMessageDialog(null,
								"You can only add submodules to the active module.");
					}
				}
				else
				{
					// no node was selected.
					JOptionPane.showMessageDialog(null,
							"Please select a module from the tree to add a submodule.");
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null,
						"Please create a new module first.");
			}
			break;
		case ADD_SUBMODULE_TEMPLATE:
			JOptionPane.showMessageDialog(null,
					"Will open up a file selection dialog box to select an already saved template model and will add that as a submodule under the selected module (not yet implemented).");
			break;
		case SAVE_SUBMODULE_AS_TEMPLATE:
			break;
		case REMOVE_SUBMODULE:
			//JOptionPane.showMessageDialog(null, "Will add an empty submodule under the module selected in the TreeView (not yet implemented).");
			

			if (AC_GUI.isModuleOpen != false)
			{
				if (AC_GUI.selectedModule != null)
				{
					node = AC_GUI.selectedModule.getTreeNode();
					parent = (DefaultMutableTreeNode)(node.getParent());
					if (parent != null)
					{
						if (AC_GUI.selectedModule != AC_GUI.drawingBoard.getActiveModule())
						{
							AC_GUI.currentGUI.removeSubmodule(AC_GUI.selectedModule);
						}
						else
						{
							JOptionPane.showMessageDialog(null, "Cannot remove the active module.");
						}
					}
					else
					{
						// the root was selected
						JOptionPane.showMessageDialog(null, "Cannot remove the RootNode.");
					}

				}
				else
				{
					// no node was selected.
					JOptionPane.showMessageDialog(null,
							"Please select a module from the tree to remove.");
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null,
						"No modules are open to be removed.");
			}
			
			break;
		case VALIDATE_MODEL:
			JOptionPane.showMessageDialog(null,
					"Check if the model is a valid SBML model (not yet implemented).");
			break;
		case VIEW_MODEL:
			JOptionPane.showMessageDialog(null,
					"Needs further discussion and exploration (not yet implemented).");
			break;
		case FLATTEN_MODEL:
			JOptionPane.showMessageDialog(null,
					"Remove the hierarchical structure and generate a SBML file (not yet implemented).");
			break;
		case DECOMPOSE_INTO_MODULES:
			JOptionPane.showMessageDialog(null,
					"Needs further discussion and exploration (not yet implemented).");
			break;
		case HELP_CONTENTS:
			JOptionPane.showMessageDialog(null,
					"Will display some sort of help tool (not yet implemented).");
			break;
		case ABOUT_AGGREGATION_CONNECTOR:
			JOptionPane.showMessageDialog(null,
					"Will give information about the tool (not yet implemented).");
			break;
		default:
			JOptionPane.showMessageDialog(null,
					"Error.");
		}
	}
}
