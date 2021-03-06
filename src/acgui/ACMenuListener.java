package acgui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Listener for the AC_GUI menu.
 * @author T.C. Jones
 * @version July 5, 2012
 */
public class ACMenuListener implements ActionListener
{

	final JFileChooser fileChooser = new JFileChooser(".");
	File file;
	String suggestedName;
	String fileName;
	String msg;
	
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
		command = command.replace(".", "");
		command = command.replaceAll(" ", "_");
		MenuItem selection = MenuItem.valueOf(command.toUpperCase());
		String name;
		DefaultMutableTreeNode node;
		DefaultMutableTreeNode parent;
		MathematicalAggregatorAddEditor mathAddEditor;
		
		
		switch(selection)
		{
		case NEW:
			//JOptionPane.showMessageDialog(null, "An empty model will be created (not yet implemented).");
			if (AC_GUI.isModuleOpen())
			{
				msg = "There is already a Module opened. Please close the current Module first.";
				JOptionPane.showMessageDialog(null, msg);
			}
			else
			{
				String o = "awesome";
				name = AC_Utility.promptUserForNewModuleName(null, "Please enter a name:");
				if (name != null)
				{
					AC_GUI.newModule(name);
				}
			}
			break;
		case OPEN:
			/*
			JOptionPane.showMessageDialog(
					null,
					"An existing model will be opened from a SBML file and will load the three panels accordingly (not yet implemented).");
			*/
			if (AC_GUI.isModuleOpen())
			{
				msg = "There is already a Module opened. Please close the current Module first.";
				JOptionPane.showMessageDialog(null, msg);
			}
			else
			{
				//fileChooser = new JFileChooser(".");
	    		
	            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) 
	            {
	            	File file = fileChooser.getSelectedFile();
	                //inputFile = file.getName().substring(0,file.getName().lastIndexOf("."));
	            	String ext = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
					//AC_GUI.load(file.getAbsolutePath(), true);
	            	AC_GUI.load(file.getAbsolutePath(), false);
	            	//AC_GUI.currentGUI.loadTest(file.getAbsolutePath());
	            }
	            else
	            {
	            	
	            }
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
			/*
			SBMLParser output = new SBMLParser();
			String fileName = output.print(AC_GUI.drawingBoard.getActiveModule());
			JOptionPane.showMessageDialog(null, "The module has been saved in " + fileName + ".");
			*/
			//return;
			if (AC_GUI.isModuleOpen())
			{
				if (AC_GUI.lastLoadSave_file != null)
				{
					AC_GUI.save(AC_GUI.rootModule, AC_GUI.lastLoadSave_file);
					JOptionPane.showMessageDialog(null, "The module has been saved in " + AC_GUI.lastLoadSave_file);
				}
				else
				{
					fileName = null;
					//fileChooser = new JFileChooser (new File ("."));
					suggestedName = AC_GUI.rootModule.getModuleDefinition().getName();
					fileChooser.setFileFilter (new FileNameExtensionFilter("Model file (.jcmc)","jcmc"));
					fileChooser.setSelectedFile(new File(suggestedName));
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
									if (!fileName.endsWith(".jcmc"))
									{
										fileName += ".jcmc";
									}
									AC_GUI.save(AC_GUI.rootModule, fileName);
									JOptionPane.showMessageDialog(null, "The module has been saved in " + fileName);
									break;
								}
							}
							else
							{
								fileName = file.getAbsolutePath();
								if (!fileName.endsWith(".jcmc"))
								{
									fileName += ".jcmc";
								}
								AC_GUI.save(AC_GUI.rootModule, fileName);
								JOptionPane.showMessageDialog(null, "The module has been saved in " + fileName);
								break;
							}
						}
						catch (Exception e)
						{
							System.err.println("ACMenuListener: save failed.");
							e.printStackTrace();
						}
					}
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Please create a new module first.");
			}
			/*
			if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
			{
				file = fileChooser.getSelectedFile();
				
				fileName = fileChooser.getSelectedFile().getAbsolutePath();
				if (!fileName.endsWith(".ac"))
				{
					fileName += ".ac";
				}
				AC_GUI.save(AC_GUI.activeModule, fileName);
				JOptionPane.showMessageDialog(null, "The module has been saved in " + fileName);
			}
			*/
			break;
		case SAVE_AS:
			/*JOptionPane.showMessageDialog(null,
					"Will save the entire model in one SBML file (not yet implemented).");
			*/
			if (AC_GUI.isModuleOpen())
			{
				fileName = null;
				//fileChooser = new JFileChooser (new File ("."));
				suggestedName = AC_GUI.rootModule.getModuleDefinition().getName();
				fileChooser.setFileFilter (new FileNameExtensionFilter("Model file (.jcmc)","jcmc"));
				fileChooser.setSelectedFile(new File(suggestedName));
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
								if (!fileName.endsWith(".jcmc"))
								{
									fileName += ".jcmc";
								}
								AC_GUI.save(AC_GUI.rootModule, fileName);
								JOptionPane.showMessageDialog(null, "The module has been saved in " + fileName);
								break;
							}
						}
						else
						{
							fileName = file.getAbsolutePath();
							if (!fileName.endsWith(".jcmc"))
							{
								fileName += ".jcmc";
							}
							AC_GUI.save(AC_GUI.rootModule, fileName);
							JOptionPane.showMessageDialog(null, "The module has been saved in " + fileName);
							break;
						}
					}
					catch (Exception e)
					{
						System.err.println("ACMenuListener: save failed.");
						e.printStackTrace();
					}
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Please create a new module first.");
			}
			break;
		case EXPORT_SBML:
			if (AC_GUI.isModuleOpen())
			{
				fileName = null;
				//fileChooser = new JFileChooser (new File ("."));
				suggestedName = AC_GUI.rootModule.getModuleDefinition().getName();
				fileChooser.setFileFilter (new FileNameExtensionFilter("SBML file (.xml)","xml"));
				fileChooser.setSelectedFile(new File(suggestedName));
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
								if (!fileName.endsWith(".xml"))
								{
									fileName += ".xml";
								}
								AC_GUI.exportSBML(fileName);
								//JOptionPane.showMessageDialog(null, "The SBML file has been saved in " + fileName);
								break;
							}
						}
						else
						{
							fileName = file.getAbsolutePath();
							if (!fileName.endsWith(".xml"))
							{
								fileName += ".xml";
							}
							AC_GUI.exportSBML(fileName);
							//JOptionPane.showMessageDialog(null, "The SBML file has been saved in " + fileName);
							break;
						}
					}
					catch (Exception e)
					{
						System.err.println("ACMenuListener: exportSBML failed.");
						e.printStackTrace();
					}
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Please create a new module first.");
			}
			break;
		case PREFERENCES:
			//AC_GUI.openPreferencesMSMB();
			break;
		case CLOSE:
			/*
			JOptionPane.showMessageDialog(
					null,
					"Will give the option to save the model as a SBML file, unload/clear all three panels, and go back to the empty screen (not yet implemented).");
			*/
			AC_GUI.close();
			break;
		case EXIT:
			/*
			JOptionPane.showMessageDialog(
					null,
					"Will exit from the tool after the completing the steps described under the Close menu item (not yet implemented).");
			*/
			AC_GUI.exit();
			break;
		case ADD_SUBMODULE_NEW:
			//JOptionPane.showMessageDialog(null, "Will add an empty submodule under the module selected in the TreeView (not yet implemented).");
			if (AC_GUI.isModuleOpen())
			{
				if (AC_GUI.canModuleAddSubmodule(AC_GUI.activeModule))
				{
					name = AC_Utility.promptUserForNewModuleName(AC_GUI.activeModule, "Please enter a name:");
					if (name != null)
					{
						AC_GUI.newSubmodule(name, AC_GUI.activeModule);
					}
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Please create a new module first.");
			}
			break;
		case ADD_SUBMODULE_LOAD:
			//JOptionPane.showMessageDialog(null,"Will open up a file selection dialog box to select an already saved template model and will add that as a submodule under the selected module (not yet implemented).");
			if (AC_GUI.isModuleOpen())
			{
				if (AC_GUI.canModuleAddSubmodule(AC_GUI.activeModule))
				{
					SubmoduleAddEditor tae = new SubmoduleAddEditor(AC_GUI.activeModule, AC_GUI.drawingBoard.graphComponent);
					tae.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					tae.setModal(true);
					tae.setVisible(true);
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Please create a new module first.");
			}
			/*
			name = null;
			//node = AC_GUI.selectedModule.getTreeNode();
			parentMod = null;

			if (AC_GUI.isModuleOpen())
			{
				if (AC_GUI.selectedModule != null)
				{
					//parent = AC_GUI.moduleList.findModule(node);
					parentMod = AC_GUI.selectedModule;
					if (parentMod == AC_GUI.drawingBoard.getActiveModule())
					{
						fileChooser = new JFileChooser(".");
			    		
			            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) 
			            {
			            	File file = fileChooser.getSelectedFile();
			                //inputFile = file.getName().substring(0,file.getName().lastIndexOf("."));
			            	String ext = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
							AC_GUI.currentGUI.loadSubmodule(file.getAbsolutePath(), AC_GUI.selectedModule);
			            	//AC_GUI.currentGUI.loadTest(file.getAbsolutePath());
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
			*/
			break;
		case ADD_SUMMATION_MODULE:
			if (AC_GUI.isModuleOpen())
			{
				if (AC_GUI.canModuleAddSubmodule(AC_GUI.activeModule))
				{
					mathAddEditor = new MathematicalAggregatorAddEditor(AC_GUI.activeModule, AC_GUI.drawingBoard.graphComponent, Operation.SUM);
					mathAddEditor.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					mathAddEditor.setModal(true);
					mathAddEditor.setVisible(true);
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Please create a new module first.");
			}
			break;
		case ADD_PRODUCT_MODULE:
			if (AC_GUI.isModuleOpen())
			{
				if (AC_GUI.canModuleAddSubmodule(AC_GUI.activeModule))
				{
					mathAddEditor = new MathematicalAggregatorAddEditor(AC_GUI.activeModule, AC_GUI.drawingBoard.graphComponent, Operation.PRODUCT);
					mathAddEditor.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					mathAddEditor.setModal(true);
					mathAddEditor.setVisible(true);
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Please create a new module first.");
			}
			break;
		case SAVE_SUBMODULE_AS_TEMPLATE:
			break;
		case REMOVE_SUBMODULE:
			
			if (AC_GUI.isModuleOpen())
			{
				if (AC_GUI.selectedModule != null)
				{
					node = AC_GUI.selectedModule.getTreeNode();
					parent = (DefaultMutableTreeNode)(node.getParent());
					if (parent != AC_GUI.treeView.getRootNode())
					{
						if (AC_GUI.selectedModule != AC_GUI.activeModule)
						{
							if (AC_GUI.canModuleRemoveSubmodule(AC_GUI.activeModule))
							{
								msg = "<html>Removing a Module will remove all associated connections and<br>";
								msg += "any Submodules contained within.<br>";
								msg += "This operation is not reversible.<br>";
								msg += "Are you sure you want to remove ";
								msg += "<b>" + AC_GUI.selectedModule.getName() + "</b>?";
								int n = JOptionPane.showOptionDialog(AC_GUI.drawingBoard.graphComponent,
									    msg,
									    "Warning",
									    JOptionPane.YES_NO_OPTION,
									    JOptionPane.WARNING_MESSAGE,
									    null,     //do not use a custom Icon
									    null,  //the titles of buttons
									    JOptionPane.NO_OPTION); //default button title
								switch(n)
								{
									case JOptionPane.YES_OPTION:
										//System.out.println("The user chose to remove.");
										AC_GUI.removeSubmodule(AC_GUI.selectedModule, true);
										break;
									case JOptionPane.NO_OPTION:
										//System.out.print("The user chose not to remove.");
										break;
								}
							}
						}
						else
						{
							// the activeModule was selected
							JOptionPane.showMessageDialog(null,
								    "Cannot remove the active module.",
								    "Invalid Operation",
								    JOptionPane.WARNING_MESSAGE);
						}
					}
					else
					{
						// the root Module was selected
						JOptionPane.showMessageDialog(null,
							    "Cannot remove the RootNode.",
							    "Invalid Operation",
							    JOptionPane.WARNING_MESSAGE);
					}

				}
				else
				{
					// no Module was selected.
					JOptionPane.showMessageDialog(null,
							"Please select a module to remove.");
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null,
						"No modules are currently open.");
			}
			break;
		case VALIDATE_MODEL:
			//JOptionPane.showMessageDialog(null, "Check if the model is a valid SBML model (not yet implemented).");
			if (AC_GUI.rootModule != null)
			{
				AC_Utility.validateAllModules();
				//AC_Utility.validateModule(AC_GUI.rootModule);
			}
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
			JOptionPane.showMessageDialog(null, "The user manual is located in the \"doc\" folder.");
			//AC_Utility.printModuleTree();
			break;
		case ABOUT_JIGCELL_MODEL_CONNECTOR:
			String info = Constants.TOOL_NAME_FULL + AC_Utility.eol;
			info += "Version: " + AC_GUI.ac_version;
			ImageIcon icon = new ImageIcon("util/logo.png");
			Image image = icon.getImage();
			Image scaledImage = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
			icon = new ImageIcon(scaledImage);
			//System.out.println(icon.getIconHeight() + "  x " + icon.getIconWidth());
			JOptionPane.showMessageDialog(null,
				    info,
				    "About",
				    JOptionPane.INFORMATION_MESSAGE,
				    icon);
			//JOptionPane.showMessageDialog(null, info);
			//System.out.println("Copasi data model number: " + CopasiUtility.getNumberOfModels());
			//CopasiUtility.printDataModelList();
			break;
		default:
			JOptionPane.showMessageDialog(null,
					"Error.");
		}
	}
}
