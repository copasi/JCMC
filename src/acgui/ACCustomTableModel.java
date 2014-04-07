/**
 * 
 */
package acgui;

import java.awt.Component;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.table.TableCellRenderer;

import msmb.commonUtilities.tables.CustomTableModel;
import msmb.gui.MainGui;

/**
 * @author Thomas
 *
 */
public class ACCustomTableModel extends CustomTableModel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Vector<PortNode> portsListed;
	private boolean displayUneditableTable;

	/**
	 * @param name
	 * @param alwaysEmptyRow
	 */
	public ACCustomTableModel(String name, boolean alwaysEmptyRow)
	{
		super(name, alwaysEmptyRow);
		portsListed = new Vector<PortNode>();
		displayUneditableTable = false;
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param name
	 */
	public ACCustomTableModel(String name)
	{
		super(name);
		portsListed = new Vector<PortNode>();
		// TODO Auto-generated constructor stub
	}
	
	public void addPort(PortNode newPort)
	{
		String refName = "";
		
		refName = newPort.getPortDefinition().getRefName();
		
		if (newPort.getParent() != AC_GUI.activeModule)
		{
			refName += " - " + newPort.getParent().getName();
		}
		
		Vector portInfo = new Vector();
		portInfo.add(refName);
		portInfo.add(newPort.getPortDefinition().getType());
		portInfo.add(newPort.getPortDefinition().getName());
		this.addRow(portInfo);
		portsListed.add(newPort);
	}
	
	public void removePort(PortNode port)
	{
		//System.out.println("Port row index: " + port.getRowIndex());
		//System.out.println("Port vector index: " + portsListed.indexOf(port));
		int portIndex = portsListed.indexOf(port);
		//tableModel.removeRow(port.getRowIndex());
		this.removeRow(portIndex);
		portsListed.remove(portIndex);
		fireTableDataChanged();
	}
	
	public int getPortIndex(PortNode port)
	{
		return portsListed.indexOf(port);
	}
	
	public PortNode getPort(int index)
	{
		return portsListed.get(index);
	}
	
	public void setUneditableTable(boolean uneditable)
	{
		displayUneditableTable = uneditable;
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		if(displayUneditableTable)
		{
			return false;
		}
		return super.isCellEditable(row, column);
	}
	
	@Override
	public void clearData()
	{
		super.clearData();
		portsListed.removeAllElements();
	}
	
	@Override
	public void setValueAt(Object selection, int row, int col) {
		if(row < 0) return;
		if(selection == null) return;
		if (portsListed.size() == 0) return;
		String value = (String)selection;
		PortNode changed = portsListed.get(row);
		int userInput = -1;
		boolean keepChanges = false;
		//System.out.println("List Name: " + portName);
		//System.out.println("Object Value: " + (String)value);
		Vector r;
		Object old;
		
		if (changed.getParent() != AC_GUI.activeModule)
		{
			String msg = "Cannot edit the information of a Submodule Port." + AC_Utility.eol;
			msg += "Please load the Submodule as the Active Module," + AC_Utility.eol;
			msg += "then make changes.";
			JOptionPane.showMessageDialog(null, msg);
			// revert back to original value
			//r.set(col, old);
			//data.set(row, r);
			//fireTableDataChanged();
			//fireTableCellUpdated(row, col);
			return;
		}
		
		switch (col)
		{
			case 1:
				if (value.equals(changed.getPortDefinition().getRefName() + " - " + changed.getPortDefinition().getVariableType().toString()))
				{
					// no change was made, the original refName was reselected
					return;
				}
				
				//VariableType vType= null;			
				if (AC_Utility.portRefNameValidation(value, AC_GUI.activeModule))
				{
					// ask user if they want to change all instances
					//userInput = AC_Utility.promptUserSubmoduleChange(AC_GUI.activeModule);
					if (value.endsWith(VariableType.SPECIES.toString()))
					{
						value = value.replace(" - " + VariableType.SPECIES.toString(), "");
						//vType = VariableType.SPECIES;
					}
					else if (value.endsWith(VariableType.GLOBAL_QUANTITY.toString()))
					{
						value = value.replace(" - " + VariableType.GLOBAL_QUANTITY.toString(), "");
						//vType = VariableType.GLOBAL_QUANTITY;
					}
					else
					{
						System.err.println("ACCustomTableModel.setValueAt: A valid VariableType was not found.");
					}
				}
				else
				{
					return;
				}
				break;
			case 2:
				if (value.equals(changed.getPortDefinition().getType().toString()))
				{
					// no change was made
					return;
				}
				String msg = "Changing the port type will remove any existing connections to the port." + AC_Utility.eol;
				msg += "Are you sure?";
				Object[] options = {"Yes", "No"};
				int n = JOptionPane.showOptionDialog(null,
					    msg,
					    "Modify Port Type",
					    JOptionPane.YES_NO_OPTION,
					    JOptionPane.WARNING_MESSAGE,
					    null,     //do not use a custom Icon
					    options,  //the titles of buttons
					    options[1]); //default button title
				switch(n)
				{
					case JOptionPane.YES_OPTION:
						System.out.println("The user confirmed changing the port type.");
						//userInput = AC_Utility.promptUserSubmoduleChange(AC_GUI.activeModule);
						break;
					case JOptionPane.NO_OPTION:
						System.out.println("The user did not confirm changing the port type.");
						return;
						//break;
				}
				break;
			case 3:
				if (value.equals(changed.getPortDefinition().getName()))
				{
					// no change was made
					return;
				}
				if (!AC_Utility.portNameValidation(value, changed.getParent()))
				{
					return;
				}
				//userInput = AC_Utility.promptUserSubmoduleChange(AC_GUI.activeModule);
				break;
		}
		
		// ask user if they want to change all instances
		userInput = AC_Utility.promptUserSubmoduleChange(AC_GUI.activeModule);
		
		switch(userInput)
		{
			case JOptionPane.YES_OPTION:
				//System.out.println("The user chose New Module.");
				// copy the current module definition
				if (AC_Utility.copyDefinition(AC_GUI.activeModule, null))
				{
					System.out.println("ACCustomTableModel.setValueAt(): definition copy success.");
					AC_GUI.modelBuilder.setModuleDefinitionName(AC_GUI.activeModule.getModuleDefinition().getName());
				}
				else
				{
					System.err.println("ACCustomTableModel.setValueAt(): definition copy failed.");
				}
				// save the updated msmb data
				byte[] code = AC_GUI.modelBuilder.saveModel();
				if (code == null || code.length == 0)
				{
					System.err.println("ACCustomTableModel.setValueAt(): msmb data is NULL.");
				}
				AC_GUI.activeModule.getModuleDefinition().setMSMBData(code);
				// change the port
				break;
			case JOptionPane.NO_OPTION:
				//System.out.print("The user chose Current Module.");
				// change the port for all instances
				
				break;
			case JOptionPane.CANCEL_OPTION:
				//System.out.println("The user chose Cancel.");
				return;
		}
		
		r = (Vector)data.get(row);
		old = r.get(col);
		r.set(col, value);
		data.set(row, r);
		fireTableDataChanged();
		fireTableCellUpdated(row, col);
		//System.out.println("Row: " + row);
		//System.out.println("Col: " + col);
		//System.out.println("Value: " + value.toString());
		AC_GUI.updatePort(portsListed.get(row), value, col);
	}
}
