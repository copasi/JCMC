package acgui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.LayoutManager;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;

/**
 * @author thomas
 *
 */
public class PortsPanel extends JPanel
{

	private static ACCustomJTable jTableCustom;
	private static ACCustomTableModel tableModel;
	private Vector<String> refNames;
	SortedComboBoxModel refNameModel;
	JComboBox<String> refNameComboBox;
	JComboBox<String> portTypeComboBox;
	
	/**
	 * 
	 */
	public PortsPanel(Font font)
	{
		super(new BorderLayout());
		JScrollPane jScrollPaneTablePorts = new JScrollPane();
		Vector<String> col = new Vector<String>();
		col.add("Ref Name");
		col.add("Port Type");
		col.add("Port Name");
		
		tableModel = new ACCustomTableModel("PortsTableModel", false);
		tableModel.setColumnNames(col, new Vector());
		tableModel.initializeTableModel();
		
		//jTableCustom = new CustomJTable();
		jTableCustom = new ACCustomJTable();
		jTableCustom.initializeCustomTable(tableModel);
		jTableCustom.setModel(tableModel);
		jTableCustom.getTableHeader().setFont(font); //font for the header
		jTableCustom.setCustomFont(font); // font for the content
		
		setupRefNameColumn();
		setupPortTypeColumn();
			
		jScrollPaneTablePorts.setViewportView(jTableCustom);
		
		this.add(jScrollPaneTablePorts, BorderLayout.CENTER);	
	}
	
	public void updateRefNames(Vector<String> iRefNames)
	{
		refNames.clear();
		refNames.addAll(iRefNames);
	}
	
	public Vector<String> getRefNames()
	{
		return refNames;
	}
	
	public void updateRefNameColumn()
	{
		refNameModel.resetModel(refNames);
		refNameComboBox = new JComboBox<String>(refNameModel);
		jTableCustom.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(refNameComboBox));
	}
	
	public void clear()
	{
		tableModel.clearData();
	}
	
	public void addPort(PortNode port)
	{
		tableModel.addPort(port);
	}
	
	public void removePort(PortNode port)
	{
		tableModel.removePort(port);
	}
	
	public void setSelectedPort(PortNode port)
	{
		int portIndex = tableModel.getPortIndex(port);
		try
		{
			jTableCustom.setRowSelectionInterval(portIndex, portIndex);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public String getPortName(int row)
	{
		return (String)jTableCustom.getValueAt(row, 1);
	}
	
	public PortNode getPort(int row)
	{
		return tableModel.getPort(row);
	}
	
	public void updateFont(Font font)
	{
		jTableCustom.getTableHeader().setFont(font);
		jTableCustom.setCustomFont(font);
	}
	
	public void setUneditable(boolean value)
	{
		tableModel.setUneditableTable(value);
		jTableCustom.setUneditableTable(value);
	}
	
	/**
	 * Setup the Ref Name column.
	 */
	private void setupRefNameColumn()
	{
		refNames = new Vector<String>();
		refNameModel = new SortedComboBoxModel(refNames, new Comparator<String>() {
		    public int compare(String str1, String str2)
		    {
		        int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
		        return (res != 0) ? res : str1.compareTo(str2);
		    }
		});
		refNameComboBox = new JComboBox<String>(refNameModel);
		jTableCustom.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(refNameComboBox));
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
		
		portTypeComboBox = new JComboBox<String>(portTypes);
		jTableCustom.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(portTypeComboBox));
	}
}
