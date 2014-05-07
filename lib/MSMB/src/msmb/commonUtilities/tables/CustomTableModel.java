package  msmb.commonUtilities.tables;


import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.*;
import javax.swing.undo.*;

import org.apache.commons.lang3.tuple.MutablePair;

//import com.sun.org.apache.xml.internal.resolver.readers.ExtendedXMLCatalogReader;

import msmb.model.Function;
import msmb.utility.Constants;

import msmb.parsers.mathExpression.MR_Expression_Parser;
import msmb.parsers.mathExpression.syntaxtree.SingleFunctionCall;
import msmb.parsers.mathExpression.visitor.GetFunctionNameVisitor;

import msmb.debugTab.DebugConstants;
import msmb.debugTab.DebugMessage;
import msmb.debugTab.FoundElement;
import msmb.gui.MainGui;



public class CustomTableModel extends DefaultTableModel {  

	
    public boolean modified = false;

    
	private static final long serialVersionUID = 1L;
	private Vector<String> columnNames = new Vector<String>();
	private Vector<Integer> booleanColumnIndexes = new Vector<Integer>(); 
	
	protected String tableName = new String();
	
	protected boolean column0_referenceIndex = true;
	protected Vector data = new Vector();
	
    protected boolean alwaysEmptyRow = true;
    private transient CustomTableModelListener addEmptyRow_CustomTableModelListener;
    public boolean isActive_AddEmptyRow;
    
  	
	public HashSet<String> disabledCell = new HashSet<String>();
	
	
	public void setColumnNames(Vector<String> all, Vector<Integer> booleanColsIndx) {
		this.columnNames.clear();
		if(column0_referenceIndex) all.insertElementAt("#",0);
		this.columnNames.addAll(all);	
		booleanColumnIndexes.clear();
		booleanColumnIndexes.addAll(booleanColsIndx);
		
	}
	
	public void clearData() {
		this.data.clear();
		this.initializeTableModel();
		this.modified = false;
		disabledCell.clear();
	}
	
	public CustomTableModel(String name, boolean alwaysEmptyRow) {
		this.columnNames.clear();
		this.tableName = new String(name);
		this.alwaysEmptyRow = alwaysEmptyRow;
	}
	
	public CustomTableModel(String name) {
		this(name,true);
	}
		
	
	
	public void initializeTableModel(){
		   Vector<Object> newR = new Vector<Object>();
	   	   if(column0_referenceIndex) {
	   		   newR.add(1);
	   	   }
	   	  for(int i=newR.size(); i < columnNames.size(); i++) {
	   		  if(booleanColumnIndexes.contains(new Integer(i))) {
	   			 newR.add(new Boolean(false));
	   		  } else {
	   			 newR.add(new String(""));
	   		  }
	   	  }
	   	  
	   	   data.add(newR);
	   	   if(alwaysEmptyRow) {
	   		   addEmptyRow_CustomTableModelListener = new CustomTableModelListener(this);
	   		  this.addTableModelListener(addEmptyRow_CustomTableModelListener);
	   	   }
	   	   
	
	}
	
	
	@Override
	public void insertRow(int row, Vector rowData) {
		if(rowData.size() > 0) {
     		Vector newR = new Vector();
     		newR.add(row+1);
        	newR.addAll(rowData);
        	for(int i=newR.size(); i < this.getColumnCount(); i++) {
        		if(booleanColumnIndexes.contains(new Integer(i))) {
        			newR.add(new Boolean(false));
        		} else {
        			newR.add(new String(""));
        		}
        	}
        	data.insertElementAt(newR, row+1);
        	
        	changeNumerationBelow(row+1);
        	fireTableDataChanged(); 
		}	
	}
	
	private void changeNumerationBelow(int i) {
		for(;i < data.size();i++) {
			((Vector)(data.get(i))).setElementAt(i+1, 0);
		}
	}

	public void removeAddEmptyRow_Listener() {
			isActive_AddEmptyRow = false;
			this.removeTableModelListener(addEmptyRow_CustomTableModelListener);
	}

	public void addAddEmptyRow_Listener() {
		isActive_AddEmptyRow = true;
		this.addTableModelListener(addEmptyRow_CustomTableModelListener);
}

	@Override
    public int getColumnCount() {
        return columnNames.size();
    }

	@Override
    public int getRowCount() {
    	if(data!=null)  	return data.size();
    	else return 0;
    }
     

    public String getColumnName(int col) {
        return columnNames.get(col).toString();
    }

    @Override
    public Object getValueAt(int row, int col) {
    	Vector r = (Vector)data.get(row);
    	if(r != null && r.size() > 1) {
    		return r.get(col);
    	}
    	else return null;
		
    }

    
    
 
    @Override
    public boolean isCellEditable(int row, int col) {
        if (col < 1) {
            return false;
        } else {
        	if(disabledCell.contains(row+"_"+col) ) {
        		return false;
        	}
        	return true;
        }
    }
    
   
    public void addRow(Vector v) {
     	if(v.size() > 0) {
     		Vector newR = new Vector();
     		if(this.column0_referenceIndex && !this.alwaysEmptyRow) newR.add(this.getRowCount()+1);
     		else if(this.column0_referenceIndex) newR.add(this.getRowCount());
        	newR.addAll(v);
        	
        	for(int i=0; i < newR.size(); i++) {
        		if(booleanColumnIndexes.contains(new Integer(i))) {	   
        			newR.set(i, Boolean.parseBoolean((String) newR.get(i)));
	   			   }
	   		 }	
        	
        	
        	for(int i=newR.size(); i < this.getColumnCount(); i++) {
        		if(booleanColumnIndexes.contains(new Integer(i))) {
	   			     newR.add(new Boolean(false));
	   			   }
	   			   else { newR.add(new String("")); }
        	}
        	
        	
        	 if(this.alwaysEmptyRow) {
        		 data.set(this.getRowCount()-1, newR);
        	     addEmptyRow_CustomTableModelListener.tableChanged(new TableModelEvent(this, this.getRowCount()-1));
        	 } else{
        		 if(((String)((Vector)data.get(0)).get(1)).length()==0) {
        			 if(this.column0_referenceIndex) newR.set(0, 1);
        			 data.set(0, newR);
        		 } else {
        			 data.add(newR);
        		 }
        	 }
             fireTableDataChanged();
        } else {
        	Vector newR = new Vector();
        	if(data.size() > 0) {
	        	Vector lastRow = (Vector)data.get(data.size()-1);
	        	if(isEmpty(lastRow)) {	
	        		for (int i = 0; i < lastRow.size(); i++) {
	        			enableCell(data.size()-1, i);
	        		}
	        		return;   	
	        	}
        	}
        	int i = 0;
        	if(this.column0_referenceIndex) {
        		newR.add(this.getRowCount()+1);
        		i = 1;
        	}
   	   	    for(;i < columnNames.size();i++) {
   	   	    	if(booleanColumnIndexes.contains(new Integer(i))) {
			     		newR.add(new Boolean(false));
   	   		   }
			   else { newR.add(new String("")); }
   	   	    }
   	   	    data.add(newR);
        }
     	

     }
    
    
    private boolean isEmpty(Vector row) {
		for (Object object : row) {
			if(object instanceof String) {
				if(((String)object).trim().length() > 0) return false;
			}
		}
		return true;
	}
    
    
    public boolean isEmpty(int index) {
    	Vector row = new Vector();
    	if(column0_referenceIndex) { row.addAll((Vector)data.get(index)); row.remove(0); }
    	else row = (Vector)data.get(index);
    	return isEmpty(row);
	}
    
    

	public void removeRow(int nrow) {
     	data.removeElementAt(nrow);
     	
     	for(int i = 0; i < data.size(); i++) {
    		Vector row = new Vector();
    		Vector current = (Vector)data.get(i);
    		row.add(i+1);
    		for(int j = 1; j < current.size(); j++){
    				row.add(current.get(j));
     		}
    		data.set(i,row);
    	}
     	if(data.size() == 0) {
     		addRow(new Vector());
     	}
     	fireTableDataChanged();
     }
    
    public void setRow(int index, Vector v) {
    	Vector newR = new Vector();
    	newR.add(index+1);
    	newR.addAll(v);
    	for(int i=newR.size(); i < this.getColumnCount(); i++) {		 
    		if(booleanColumnIndexes.contains(new Integer(i))) {
    			newR.add(new Boolean(false));	  
    		}
    		 else {newR.add(new String(""));}  
    	}
     	data.set(index, newR);
     }

    
    public void disableCell(int row, int col) {
		this.disabledCell.add(row+"_"+col);
	}
    
    
	public void enableCell(int row, int col) {
		this.disabledCell.remove(row+"_"+col);
	}

	public String getTableName() {
		return this.tableName;
	}

	public void enableAllCells() {
		disabledCell.clear();
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		
		   if(row < 0) return;
	       Vector r = (Vector)data.get(row);
		   Object old = r.get(col);
		   r.set(col, value);
		   data.set(row, r);
		   fireTableDataChanged();
		   fireTableCellUpdated(row, col);
	}


   
}














