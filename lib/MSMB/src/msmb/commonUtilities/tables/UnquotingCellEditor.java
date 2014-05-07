package msmb.commonUtilities.tables;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;

public class UnquotingCellEditor extends JTextField implements TableCellEditor{  
	private static final long serialVersionUID = 1L;
	  Font customFont = null;
	  public Font getCustomFont() {return customFont;	}
		public void setCustomFont(Font customFont) {	this.customFont = customFont;	}

	protected Vector<CellEditorListener> listeners;  

	public UnquotingCellEditor()  {
		listeners = new Vector<CellEditorListener>();  
		}  


	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,  
			int row, int column) {  
		if(value.toString().trim().startsWith("\"") &&  value.toString().trim().endsWith("\"")) {
			this.setText(value.toString().trim().substring(1,value.toString().trim().length()-1));
		}   else {
			this.setText(value.toString().trim());
		}
		
		if(customFont!=null) this.setFont(customFont);
		return this;
		
	}  

	public void cancelCellEditing(){  
		fireEditingCanceled();  
	}  


	public boolean stopCellEditing(){  
		fireEditingStopped();  
		return true;  
	}  

	public Object getCellEditorValue(){  
		return this.getText();  
	}  

	@Override
	public boolean isCellEditable(EventObject oe){
		  try { 
			  MouseEvent me = (MouseEvent) oe;
			  if (me.getClickCount() == 2) return true;
	      	 else return false;
		  } catch (Exception e) { // is not a mouse event, can be a tab or some other key events
			 return false;
		}
	 }  

	@Override
	public boolean shouldSelectCell(EventObject ev) { return true;}  
	
	@Override
	public void addCellEditorListener(CellEditorListener cel){  
		listeners.addElement(cel);  
	}  

	@Override
	public void removeCellEditorListener(CellEditorListener cel){  
		listeners.removeElement(cel);  
	}  

	protected void fireEditingCanceled(){  
		ChangeEvent ce = new ChangeEvent(this);  
		for(int i=0; i<listeners.size(); i++){  
			((CellEditorListener)listeners.elementAt(i)).editingCanceled(ce);  
		}  
	}  

	protected void fireEditingStopped(){  
		ChangeEvent ce = new ChangeEvent(this);  
		for(int i=0; i<listeners.size(); i++){  
			((CellEditorListener)listeners.elementAt(i)).editingStopped(ce);  
		}  
	}
}  
