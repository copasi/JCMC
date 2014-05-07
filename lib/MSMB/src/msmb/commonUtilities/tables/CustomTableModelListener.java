package msmb.commonUtilities.tables;

import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class CustomTableModelListener implements TableModelListener{
	protected CustomTableModel mod;
	 
	 public CustomTableModelListener(CustomTableModel m) {
		 mod = m;
	 }
	 
	 public void tableChanged(TableModelEvent e) {
		int rowChanged = e.getFirstRow();
	    mod.modified = true;
       if(rowChanged == mod.getRowCount()-1) {
       	   mod.addRow(new Vector());
       }
    }
	 
  }

