/**
 * 
 */
package acgui;

import java.util.Vector;

import javax.swing.JOptionPane;

import msmb.commonUtilities.tables.CustomTableModel;

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
	private Vector<Port> portsListed;

	/**
	 * @param name
	 * @param alwaysEmptyRow
	 */
	public ACCustomTableModel(String name, boolean alwaysEmptyRow)
	{
		super(name, alwaysEmptyRow);
		portsListed = new Vector<Port>();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param name
	 */
	public ACCustomTableModel(String name)
	{
		super(name);
		portsListed = new Vector<Port>();
		// TODO Auto-generated constructor stub
	}
	
	public void addPort(Port newPort)
	{
		String refName = "";
		
		refName = newPort.getRefName();
		
		if (newPort.getParent() != AC_GUI.activeModule)
		{
			refName += " - " + newPort.getParent().getName();
		}
		
		Vector portInfo = new Vector();
		portInfo.add(refName);
		portInfo.add(newPort.getType());
		portInfo.add(newPort.getName());
		this.addRow(portInfo);
		portsListed.add(newPort);
	}
	
	public void removePort(Port port)
	{
		//System.out.println("Port row index: " + port.getRowIndex());
		//System.out.println("Port vector index: " + portsListed.indexOf(port));
		int portIndex = portsListed.indexOf(port);
		//tableModel.removeRow(port.getRowIndex());
		this.removeRow(portIndex);
		portsListed.remove(portIndex);
		fireTableDataChanged();
	}
	
	@Override
	public void clearData()
	{
		super.clearData();
		portsListed.removeAllElements();
	}
	
	@Override
	public void setValueAt(Object value, int row, int col) {
		if(row < 0) return;
		Port changed = portsListed.get(row);
		//System.out.println("List Name: " + portName);
		//System.out.println("Object Value: " + (String)value);
		if ((changed.getParent() != AC_GUI.activeModule) && (col == 1))
		{
			String msg = "Cannot edit the Ref Name of a Submodule Port.";
			JOptionPane.showMessageDialog(null, msg);
			//fireTableDataChanged();
			//fireTableCellUpdated(row, col);
			return;
		}
		Vector r = (Vector)data.get(row);
		Object old = r.get(col);
		r.set(col, value);
		data.set(row, r);
		fireTableDataChanged();
		fireTableCellUpdated(row, col);
		//System.out.println("Row: " + row);
		//System.out.println("Col: " + col);
		//System.out.println("Value: " + value.toString());
		AC_GUI.updatePort(portsListed.get(row), (String)value, col);
	}
}
