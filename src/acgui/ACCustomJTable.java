package acgui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import msmb.commonUtilities.tables.CustomJTable;
import msmb.utility.Constants;
import msmb.utility.GraphicalProperties;

/**
 * @author T.C. Jones
 *
 */
public class ACCustomJTable extends CustomJTable
{

	private boolean displayUneditableTable;
	
	public ACCustomJTable()
	{
		super();
		displayUneditableTable = false;
	}
	
	public void setUneditableTable(boolean uneditable)
	{
		displayUneditableTable = uneditable;
	}
	
	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
		Component c = null;
		try{
			c = super.prepareRenderer(renderer, rowIndex, vColIndex);
			
			/*if(cell_to_highlight != null) {
				if(cell_to_highlight.getLeft().intValue()-1 == rowIndex && cell_to_highlight.getRight().intValue() == vColIndex){
					c.setBackground(GraphicalProperties.color_cell_to_highlight);
					return c;
				}
			} */
			
			
			
			if(isCellUneditable(rowIndex,vColIndex)) {
				c.setBackground(Constants.vt_blues_1);
			}else {
				if(isCellWithError(rowIndex,vColIndex)) {
					c.setBackground(GraphicalProperties.color_cell_with_errors);
				} else {
					if(isCellWithMinorIssue(rowIndex,vColIndex)&&GraphicalProperties.color_cell_with_minorIssues!=null) {
						c.setBackground(GraphicalProperties.color_cell_with_errors);
					} else {
						if (rowIndex % 2 != 0) {
							c.setBackground(GraphicalProperties.color_shading_table);
						} else {
							c.setBackground(Color.white);
						}
					}
				}
			}
		
			/*if( vColIndex == 0 && model.column0_referenceIndex) {
				c.setBackground(UIManager.getColor("TableHeader.background"));
			}*/
		
			if(customFont!=null) c.setFont(customFont);
	
			if(this.isRowSelected(rowIndex)) {
				c.setBackground(GraphicalProperties.color_cell_to_highlight);
				c.setFont(c.getFont().deriveFont(Font.BOLD));
				c.setForeground(Color.BLACK);
			} else {
				if(vColIndex== 0) {
					c.setBackground(GraphicalProperties.color_shading_table);
					c.setForeground(Color.BLACK);
				}
				else {
					if(c.getBackground() != GraphicalProperties.color_cell_with_errors)
					{
					if (rowIndex % 2 != 0) {
						c.setBackground(GraphicalProperties.color_shading_table);
					} else {
						c.setBackground(Color.white);
					}
					c.setForeground(Color.BLACK);
					}
				 }
					
			}
			
			if(displayUneditableTable)
			{
				c.setBackground(Color.LIGHT_GRAY);
				if(this.isRowSelected(rowIndex)) {
					c.setForeground(GraphicalProperties.color_cell_to_highlight);
				}
				else
				{
					c.setForeground(Color.BLACK);
				}
			}
			
			return c;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			//System.out.println("Problems rendering column: "+this.model.getColumnName(vColIndex));
			if(customFont!=null)  c.setFont(customFont);
			return c;
		}
		
	}

}
