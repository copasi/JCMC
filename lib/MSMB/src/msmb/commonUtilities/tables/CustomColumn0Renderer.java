package msmb.commonUtilities.tables;

import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

public class CustomColumn0Renderer extends DefaultTableCellRenderer  {  
    private static final long serialVersionUID = 1L;
	int selectedRow;  
    Font customFont = null;
    public Font getCustomFont() {return customFont;	}
	public void setCustomFont(Font customFont) {	this.customFont = customFont;	}

	public CustomColumn0Renderer()      {  
        setHorizontalAlignment(JLabel.CENTER);  
        setBackground(UIManager.getColor("TableHeader.background"));  
        selectedRow = -1;  
    }  
   
    public Component getTableCellRendererComponent(JTable table,  Object value,  boolean isSelected,  boolean hasFocus,  int row, int column)  {  
        super.getTableCellRendererComponent(table, value, isSelected,  hasFocus, row, column);  
        setBorder(BorderFactory.createRaisedBevelBorder());
        if(customFont!=null)  setFont(customFont);
        return this;  
    }  
   
   
    public void setSelectedRow(int selected)     {  
        selectedRow = selected;  
        repaint();  
    }
}  
