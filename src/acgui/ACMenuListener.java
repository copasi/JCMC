package acgui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

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

		if (command.equals(AC_GUI.MENU_NEW))
		{
			JOptionPane.showMessageDialog(null, "An empty model will be created (not yet implemented).");
		}
		else if (command.equals(AC_GUI.MENU_OPEN))
		{
			JOptionPane.showMessageDialog(null, "An existing model will be opened from a SBML file and will load the three panels accordingly (not yet implemented).");
		}
		else if (command.equals(AC_GUI.MENU_RECENT))
		{
			JOptionPane.showMessageDialog(null, "Will show recently opened list of files (not yet implemented).");
		}
		else if (command.equals(AC_GUI.MENU_SAVE))
		{
			JOptionPane.showMessageDialog(null, "Will save the entire model in one SBML file (not yet implemented).");
		}
		else if (command.equals(AC_GUI.MENU_SAVE_AS))
		{
			JOptionPane.showMessageDialog(null, "Will save the entire model in one SBML file (not yet implemented).");
		}
		else if (command.equals(AC_GUI.MENU_CLOSE))
		{
			JOptionPane.showMessageDialog(null, "Will give the option to save the model as a SBML file, unload/clear all three panels, and go back to the empty screen (not yet implemented).");
		}
		else if (command.equals(AC_GUI.MENU_ADD_SUBMODULE))
		{
			//JOptionPane.showMessageDialog(null, "Will add an empty submodule under the module selected in the TreeView (not yet implemented).");
			String name = null;
			name = JOptionPane.showInputDialog("Name of the new module:", "Module");
			if (name != null)
			{
				AC_GUI.currentGUI.newModule(name);
			}
		}
		else if (command.equals(AC_GUI.MENU_REMOVE_SUBMODULE))
		{
			 //JOptionPane.showMessageDialog(null, "Will add an empty submodule under the module selected in the TreeView (not yet implemented).");
			AC_GUI.currentGUI.removeModule();
		}
		else if (command.equals(AC_GUI.MENU_EXIT))
		{
			JOptionPane.showMessageDialog(null, "Will exit from the tool after the completing the steps described under the Close menu item (not yet implemented).");
		}
		else if (command.equals(AC_GUI.MENU_HELP))
		{
			JOptionPane.showMessageDialog(null, "Will display some sort of help tool (not yet implemented).");
		}
		else if (command.equals(AC_GUI.MENU_ABOUT))
		{
			JOptionPane.showMessageDialog(null, "Will give information about the tool (not yet implemented).");
		}
	}
}
