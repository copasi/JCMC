package  msmb.gui;
import java.util.Hashtable;

import javax.swing.table.TableCellEditor;

import org.apache.commons.lang3.tuple.MutablePair;

public class MyCellEditorModel
 {
     private Hashtable data;
      public MyCellEditorModel()
      {
         data = new Hashtable();
      }
     public void clear() {data.clear();}
     
	public void addEditorForCell(int row, int col, TableCellEditor e )
     {
         data.put(new MutablePair(row,col), e);
     }
	
     public void removeEditorForCell(int row, int col)
     {
         data.remove(new MutablePair(row,col));
     }
     
     public TableCellEditor getEditor(int row, int col)
     {
         return (TableCellEditor)data.get(new MutablePair(row,col));
     }
 }