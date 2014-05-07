package msmb.commonUtilities.tables;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


public class EditableCellRenderer extends DefaultTableCellRenderer {
	 
	private static final long serialVersionUID = 1L;
	Font customFont = null;
    public Font getCustomFont() {return customFont;	}
	public void setCustomFont(Font customFont) {	this.customFont = customFont;	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
		  super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		  if(customFont!=null) setFont(customFont);
		return this;
	}
		
}