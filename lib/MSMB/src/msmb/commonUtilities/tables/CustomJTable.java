package msmb.commonUtilities.tables;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import msmb.utility.Constants;
import msmb.utility.GraphicalProperties;

import org.apache.commons.lang3.tuple.MutablePair;


public class CustomJTable extends JTable  {
	private static final long serialVersionUID = 1L;
	
	MutablePair<Integer, Integer> cell_to_highlight = null;
	public MutablePair<Integer, Integer> getCell_to_highlight() {return cell_to_highlight;}
	
	public void setCell_to_highlight(MutablePair<Integer, Integer> cell_to_highlight) {
		this.cell_to_highlight = cell_to_highlight;
	}


	protected CustomTableModel model ;
	
    protected Font customFont = null;
    public Font getCustomFont() {return customFont;	}
	public void setCustomFont(Font customFont) {	
		this.customFont = customFont; 	
		if(customFont!= null) {
			FontMetrics metrics = this.getFontMetrics(customFont);
			int fontHeight = metrics.getHeight();
		    this.setRowHeight(fontHeight+5);	
		} else {
			this.setRowHeight(20);
		}
	}
	
	
	
	public void initializeCustomTable(CustomTableModel m) {
		if(customFont!= null) {
			FontMetrics metrics = this.getFontMetrics(customFont);
			int fontHeight = metrics.getHeight();
		    this.setRowHeight(fontHeight);
		} else {
			this.setRowHeight(20);
		}
	    
	    model = m;    
	    this.setModel(m);
	    this.setColumnSelectionAllowed(false);
	
	    this.getTableHeader().setReorderingAllowed(false);
		
	    setBackground(UIManager.getColor("Button.background"));
	  
		TableColumnModel colModel = this.getColumnModel();
		if(model != null && model.column0_referenceIndex) {
			TableColumn col = colModel.getColumn(0);  
			col.setCellRenderer(new CustomColumn0Renderer());  
			col.setPreferredWidth(40);
			col.setMaxWidth(100);
			col.setMinWidth(40);
			col.setWidth(40);
		} 
		
		TableColumn colLast = colModel.getColumn(colModel.getColumnCount()-1);  
		colLast.setPreferredWidth(40);
		colLast.setMinWidth(40);
		colLast.setWidth(40);
		
		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
	    
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION );
		
		
		
		
		/*addMouseListener(new MouseAdapter() {
		    	public void mousePressed(MouseEvent e) { }
		    	public void mouseReleased(MouseEvent e) { }
		    	    
		        public void mouseClicked(MouseEvent e)   {
		            if (e.getButton() == MouseEvent.BUTTON1)  {
		                Point p = e.getPoint();
		                int row = rowAtPoint(p); 
		                int col = columnAtPoint(p); 
		             //   clearSelection();
                    //	setColumnSelectionInterval(col, col);
                    //	if(row >= 0 && row < getRowCount()) setRowSelectionInterval(row, row);
                    	revalidate();
		            } 
		        }
		});*/
		
		
		
	}
	 @Override
	public boolean getScrollableTracksViewportHeight()  {
	        Component parent = getParent();

	        if (parent instanceof JViewport) {
	        	  return parent.getHeight() > getPreferredSize().height;
	        }
	        return false;
	    }
	
	 HashSet<String> cells_with_errors = new HashSet<String>();
		HashSet<String> cells_with_defaults = new HashSet<String>();
		HashSet<String> cells_with_minorIssue = new HashSet<String>();
		
		public void cell_has_defaults(int row, int col) { cells_with_defaults.add(new String(row+"_"+col)); }
		public void cell_no_defaults(int row, int col) { cells_with_defaults.remove(new String(row+"_"+col)); }
		public boolean isCellWithDefaults(int row, int col) { return cells_with_defaults.contains(new String(row+"_"+col));}
		
		public void cell_has_errors(int row, int col) { 	cells_with_errors.add(new String(row+"_"+col)); }
		public void cell_no_errors(int row, int col) { cells_with_errors.remove(new String(row+"_"+col)); }
		public boolean isCellWithError(int row, int col) {	return cells_with_errors.contains(new String(row+"_"+col));	}
		
		public void cell_has_minorIssue(int row, int col) { 	cells_with_minorIssue.add(new String(row+"_"+col)); }
		public void cell_no_minorIssue(int row, int col) { cells_with_minorIssue.remove(new String(row+"_"+col)); }
		public boolean isCellWithMinorIssue(int row, int col) {	return cells_with_minorIssue.contains(new String(row+"_"+col));	}
		
		public void printCellsWithErrors() {System.out.println(cells_with_errors);}
		public void clearCellsWithErrors() { cells_with_errors.clear();	}
		public void clearCellsWithDefaults() { cells_with_defaults.clear();	}

@Override
public void setRowSelectionInterval(int index0, int index1) {
	cell_to_highlight = null;
	super.setRowSelectionInterval(index0, index1);
};
		
		

	public boolean isCellUneditable(int row, int col) { 
		return this.model.disabledCell.contains(row+"_"+col);}

	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
		Component c = null;
		try{
			c = super.prepareRenderer(renderer, rowIndex, vColIndex);
			
			if(cell_to_highlight != null) {
				if(cell_to_highlight.getLeft().intValue()-1 == rowIndex && cell_to_highlight.getRight().intValue() == vColIndex){
					c.setBackground(GraphicalProperties.color_cell_to_highlight);
					return c;
				}
			} 
			
			
			
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
		
			if( vColIndex == 0 && model.column0_referenceIndex) {
				c.setBackground(UIManager.getColor("TableHeader.background"));
			}
		
			if(customFont!=null) c.setFont(customFont);
	
			if(this.isRowSelected(rowIndex)) {
				c.setBackground(GraphicalProperties.color_cell_to_highlight);
				c.setFont(c.getFont().deriveFont(Font.BOLD));
				c.setForeground(Color.BLACK);
			} else {
				if(vColIndex== 0) {
					c.setBackground(GraphicalProperties.color_shading_table);
				}
				else {
					if(c.getBackground() != GraphicalProperties.color_cell_with_errors)
					{
					if (rowIndex % 2 != 0) {
						c.setBackground(GraphicalProperties.color_shading_table);
					} else {
						c.setBackground(Color.white);
					}
					}
				 }
					
			}
			
			return c;
			
		
		} catch(Exception ex) {
			ex.printStackTrace();
			//System.out.println("Problems rendering column: "+this.model.getColumnName(vColIndex));
			if(customFont!=null)  c.setFont(customFont);
			return c;
		}
		
	}
	
};


