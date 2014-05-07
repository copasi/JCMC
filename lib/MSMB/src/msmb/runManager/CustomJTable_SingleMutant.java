package msmb.runManager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.RowFilter;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.tuple.MutablePair;

import msmb.commonUtilities.tables.CustomJTable;
import msmb.commonUtilities.tables.CustomTableModel;
import msmb.commonUtilities.tables.EditableCellRenderer;
import msmb.utility.Constants;
import msmb.utility.GraphicalProperties;

public class CustomJTable_SingleMutant extends CustomJTable {

	HashSet<Integer> localRedefinition = new HashSet<Integer>(); //only rowIndex
	HashSet<Integer> fromBaseSet = new HashSet<Integer>(); //only rowIndex
	HashMap<Integer, String> cumulativeRedefinition = new HashMap<Integer,String>(); //rowIndex and mutant name
	String tableName = new String();
	private TableRowSorter<TableModel> sorter;
	
	public CustomJTable_SingleMutant(final SingleMutantFrame parent, CustomTableModel tableModel, Font customFont) {
		initializeLocalVars();//to register the tooltipmanager and allow tooltip to show
		initializeCustomTable(tableModel);
		setModel(tableModel);
		getTableHeader().setFont(customFont); //font for the header
		setCustomFont(customFont); // font for the content
		tableName = tableModel.getTableName();
		 addMouseListener(new MouseAdapter() {
	            public void mouseClicked(MouseEvent evt) {
	                if (evt.getClickCount() == 2) {
	                    Point pnt = evt.getPoint();
	                    int row = rowAtPoint(pnt);
	                    parent.showLocalChangeFrame(convertRowIndexToModel(row), dataModel, tableName);
	                }
	            }
	        });
		 
		 sorter = new TableRowSorter<TableModel>(getModel());
		 sorter.setComparator(0, new Comparator<Integer>() {
			    public int compare(Integer s1, Integer s2) {
				            return s1.compareTo(s2);
				    }
			});
		setRowSorter(sorter);
		
	}
	
	public void applyFilter(String filterText) {
		if(filterText.length() ==0)   {
			sorter.setRowFilter(null);
			return;
		}
		
		 List<RowFilter<TableModel,Object>> filters = new ArrayList<RowFilter<TableModel,Object>>();
	       
		 RowFilter<TableModel, Object> compoundRowFilter = null;
		try {
	    	for(int i = 0; i <= getColumnCount(); i++) {
	    		RowFilter<TableModel, Object> rf = RowFilter.regexFilter("(?i)"+filterText, i);
	            filters.add(rf);
	    	}
	        compoundRowFilter = RowFilter.orFilter(filters); // you may also choose the OR filter
	    } catch (java.util.regex.PatternSyntaxException e) {
	        return;
	    }
		  
        sorter.setRowFilter(compoundRowFilter); 
   
	}
	
	public void resetFilter() {
	   sorter.setRowFilter(null);
	}
	
	
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
	
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex,	int vColIndex) {
		Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
		Color original = c.getBackground();
		if(!isCellSelected(rowIndex, vColIndex)) {
			if(localRedefinition.contains(new Integer(convertRowIndexToModel(rowIndex)))) {
				c.setBackground(RunManager.colorLocalRedefinition);
			}
			else 	if(cumulativeRedefinition.containsKey(new Integer(convertRowIndexToModel(rowIndex)))) {
				c.setBackground(RunManager.colorCumulativeRedefinition);
			} 
			else {
				c.setBackground(original);
			}
		}
		if(isCellWithError(rowIndex,vColIndex)) {
			Border compound = null;
			Border redline = BorderFactory.createLineBorder(GraphicalProperties.color_cell_with_errors,3);
			compound = BorderFactory.createCompoundBorder(redline, compound);
			((EditableCellRenderer)c).setBorder(compound);
			c.setBackground(GraphicalProperties.color_cell_with_errors);
		}
	
		
		if(getCell_to_highlight()!= null) {
			if(getCell_to_highlight().getLeft().intValue()-1 == rowIndex && getCell_to_highlight().getRight().intValue() == vColIndex){
				c.setBackground(GraphicalProperties.color_cell_to_highlight);
				c.setForeground(Color.BLACK);
				c.setFont(GraphicalProperties.customFont);
				}
		} 
		
		
		
		return c;
	}

	public void removeLocalCumulativeRedefinition(int row) {
		//is from baseSet
		cumulativeRedefinition.remove(new Integer(row));
		localRedefinition.remove(new Integer(row));
		fromBaseSet.add(row);
	}
	
	
	public void addLocalRedefinition(int rowIndex) {
		localRedefinition.add(new Integer(rowIndex));
		cumulativeRedefinition.remove(new Integer(rowIndex));
	}
	
	public void addCumulativeRedefinition(int rowIndex, String sourceMutant) {
		cumulativeRedefinition.put(new Integer(rowIndex), sourceMutant);
		localRedefinition.remove(new Integer(rowIndex));
	}
	
	public void clearLocalRedefinition() {
		localRedefinition.clear();
	}

	public void clearCumulativeRedefinition() {
		cumulativeRedefinition.clear();
	}

	public void clearFromBaseSet() {
		fromBaseSet.clear();
	}


	@Override
	public String getToolTipText(MouseEvent event) {
		int row = rowAtPoint(event.getPoint());
		if(cumulativeRedefinition.containsKey(new Integer(row))) {
			return cumulativeRedefinition.get(new Integer(row));
		}
		return super.getToolTipText(event);
	}
	
	public Vector getAllChanges() {
		Vector ret = new Vector();
		HashMap<String, String> local = new HashMap<String, String>();
		int  columnToChange = -1;
		int noteColumn = -1;
		int nameColumn = -1;
		MutantChangeType mchangetype = null;
		if(tableName.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription()) == 0){
			columnToChange = Constants.GlobalQColumns.VALUE.index;
			nameColumn = Constants.GlobalQColumns.NAME.index;
			noteColumn = Constants.GlobalQColumns.NOTES.index;
			mchangetype = MutantChangeType.GLQ_INITIAL_VALUE;
		} else if(tableName.compareTo(Constants.TitlesTabs.SPECIES.getDescription()) == 0){
			columnToChange = Constants.SpeciesColumns.INITIAL_QUANTITY.index;
			nameColumn = Constants.SpeciesColumns.NAME.index;
			noteColumn = Constants.SpeciesColumns.NOTES.index;
			mchangetype = MutantChangeType.SPC_INITIAL_VALUE;
		} else if(tableName.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription()) == 0){
			columnToChange = Constants.CompartmentsColumns.INITIAL_SIZE.index;
			nameColumn = Constants.CompartmentsColumns.NAME.index;
			noteColumn = Constants.CompartmentsColumns.NOTES.index;
			mchangetype = MutantChangeType.COMP_INITIAL_VALUE;
		} 
		
		for (Integer row: localRedefinition) {
			String name = Mutant.generateChangeKey(mchangetype, dataModel.getValueAt(row, nameColumn).toString());
			local.put(name,
					dataModel.getValueAt(row, columnToChange).toString());
		}
		
		//key: element changed, value: new expression, mutant that the change comes from
		HashMap<String, MutablePair<String, String>> cumulative = new HashMap<String, MutablePair<String, String>>();
		for (Integer row: cumulativeRedefinition.keySet()) {
			MutablePair<String, String> element = new MutablePair<String, String>();
			String name = Mutant.generateChangeKey(mchangetype, dataModel.getValueAt(row, nameColumn).toString());
			element.left = dataModel.getValueAt(row, columnToChange).toString();
			element.right = cumulativeRedefinition.get(row);
			cumulative.put(name, element);
		}
		
		HashSet<String> baseSet = new HashSet<String>();
		for (Integer row: fromBaseSet) {
			String name = Mutant.generateChangeKey(mchangetype, dataModel.getValueAt(row, nameColumn).toString());
			baseSet.add(name);
		}
		
		ret.add(local);
		ret.add(cumulative);
		ret.add(baseSet);
		return ret;
	}

}



