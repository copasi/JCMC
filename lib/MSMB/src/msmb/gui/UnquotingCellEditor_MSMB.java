package msmb.gui;

import java.awt.Component;
import java.awt.Point;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

import msmb.commonUtilities.tables.CustomTableModel;
import msmb.commonUtilities.tables.UnquotingCellEditor;
import msmb.model.MultiModel;
import msmb.utility.Constants;
import msmb.utility.MySyntaxException;

public class UnquotingCellEditor_MSMB extends  UnquotingCellEditor{
	
	private static final long serialVersionUID = 1;

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
	    
	  String tableModelName = ((CustomTableModel)(table.getModel())).getTableName();
       if(MainGui.cellTableEdited.compareTo(tableModelName) !=0) MainGui.resetViewsInExpressions();
       if(tableModelName.compareTo(Constants.TitlesTabs.REACTIONS.getDescription()) == 0 && table.getRowCount() > row+1) {
			try {
				MainGui.setView(Constants.Views.EDITABLE.index,tableModelName,row,Constants.ReactionsColumns.KINETIC_LAW.index);
			} catch (Throwable e) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
			}
		}
       
        MainGui.cellValueBeforeChange = value.toString();
     	
     	MainGui.cellSelectedRow=row;
     	MainGui.cellSelectedCol=column;
     	MainGui.cellTableEdited = tableModelName;
     
     	super.getTableCellEditorComponent(table,value,isSelected, row, column);
		
     	this.setText(super.getText().toString().trim());
     	MainGui.validationsOn=true;
     	MainGui.validateOnce=false;
		
		return this;
		  
	}


}
