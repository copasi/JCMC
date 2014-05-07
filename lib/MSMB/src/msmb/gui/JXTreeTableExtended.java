package  msmb.gui;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreePath;

import msmb.utility.Constants;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;


import msmb.debugTab.FoundElement;
import msmb.debugTab.FoundElementToDelete;

public class JXTreeTableExtended extends JXTreeTable {
	
	private static final long serialVersionUID = 1L;
	protected MyCellEditorModel rm;
	public void initializeRowEditorModel()
	{
		this.rm =  new MyCellEditorModel();
	}

	public MyCellEditorModel getRowEditorModel()
	{
		return rm;
	}

	public TableCellEditor getCellEditor(int row, int col)
	{
		TableCellEditor tmpEditor = null;
		
		if (rm!=null)
			tmpEditor = rm.getEditor(row,col);
		if (tmpEditor!=null)
			return tmpEditor;
		return super.getCellEditor(row,col);
	}

	public void addEditorForCell(int row, int col, TableCellEditor e )
	{
		rm.addEditorForCell(row, col, e);
	
	}
	
	
	public void resetComboBoxValues() {
		rm.clear();
		
		DefaultMutableTreeTableNode checkFromHere = (DefaultMutableTreeTableNode) this.getTreeTableModel().getRoot();
		//Vector<DefaultMutableTreeTableNode> children = new Vector<DefaultMutableTreeTableNode>();
		for(int i = 0; i < checkFromHere.getChildCount(); i++) {
			DefaultMutableTreeTableNode n = (DefaultMutableTreeTableNode) checkFromHere.getChildAt(i);
			if(n.getUserObject() instanceof FoundElementToDelete) {
					addComboBox_andRecursiveChildren(n);
			}
		}
	
	}

	private void addComboBox_andRecursiveChildren(DefaultMutableTreeTableNode n) {
		int row = this.getRowForPath(new TreePath(((TreeTableModel)this.getTreeTableModel()).getPathToRoot(n)));
		JComboBox comboBox = new JComboBox();
		for(int i = 0; i < Constants.deleteActions.size()-1; i++) { 
			comboBox.addItem((String)Constants.deleteActions.get(i));
		}
		
		FoundElementToDelete which = (FoundElementToDelete) n.getUserObject();
		if(which.getTableDescription().compareTo(Constants.TitlesTabs.REACTIONS.getDescription()) !=0 || which.getCol() != Constants.ReactionsColumns.KINETIC_LAW.index) {
			String newValue = Constants.DeleteActions.ASSIGN_NEW_VALUE.custom_description;
			newValue = newValue.replace("\"\"", "\""+getParentName(n)+"\"");
			comboBox.addItem(newValue);
		}
			
		
		DefaultCellEditor ed = new DefaultCellEditor(comboBox);
	    this.addEditorForCell(row, Constants.DeleteColumns.ACTION_TO_TAKE.index, ed);
		for(int i = 0; i < n.getChildCount(); i++) {
			DefaultMutableTreeTableNode n2 = (DefaultMutableTreeTableNode) n.getChildAt(i);
			if( n2.getUserObject() instanceof FoundElementToDelete) {
				addComboBox_andRecursiveChildren(n2);
			}
		}
		
	}
	
	 private String getParentName(DefaultMutableTreeTableNode node) {
		 int row = this.getRowForPath(new TreePath(((TreeTableModel)this.getTreeTableModel()).getPathToRoot(node)));
	    	String ret = new String();
	    	TreePath path = getPathForRow(row);
	    	TreeTableNode defNode = (TreeTableNode) path.getLastPathComponent();
			TreeTableNode parent = defNode.getParent();
			if(parent!=null) {
				if (parent.getUserObject() instanceof FoundElement) {
					FoundElement elementParent = (FoundElement) parent.getUserObject();
					ret = MainGui.printMainElementRow(elementParent);
				}
			}
			return ret;
		}

}


