package msmb.commonUtilities.tables;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;

public class CustomTableRowSorter extends TableRowSorter<CustomTableModel> {

	Vector <CustomComparator> comps = new Vector<CustomComparator>();
	
	public CustomTableRowSorter(CustomTableModel m) {
		super(m);
		for(int i = 0; i < m.getColumnCount(); i++) {
			CustomComparator customComp = new CustomComparator();
			comps.add(customComp);
		}

	}
	
	@Override
	public Comparator<?> getComparator(int column) {
		return comps.get(column);
	}
	
	//http://stackoverflow.com/questions/10842290/non-live-non-realtime-sorting-of-jtable
	 @Override
     public void toggleSortOrder(int column) {
		  if(column>=0 && column<getModelWrapper().getColumnCount()   && isSortable(column)) {
			      List<SortKey> keys = new ArrayList<SortKey>(getSortKeys());
		          if(!keys.isEmpty()) {
		        	  if(comps.get(column).getOrder().compareTo("null") == 0){
		               	comps.get(column).setOrder(SortOrder.ASCENDING);
			           	List <RowSorter.SortKey> sortKeys  = new ArrayList<RowSorter.SortKey>();
			            sortKeys.add(new RowSorter.SortKey(column, SortOrder.ASCENDING));
			            setSortKeys(sortKeys);
			            super.toggleSortOrder(column);
		        	  }
		        	  else {
			            SortKey sortKey = keys.get(0);
			             if(sortKey.getColumn()==column && comps.get(column).getOrder().compareTo(SortOrder.DESCENDING.toString())==0) {
			            	comps.get(column).setOrder(SortOrder.ASCENDING);
			              } else {
			            	comps.get(column).setOrder(SortOrder.DESCENDING);
			            }
			            super.toggleSortOrder(column);
			          }
		          
		          }  else {
		        	  if(comps.get(column).getOrder().compareTo("null") == 0){
		        		  comps.get(column).setOrder(SortOrder.ASCENDING);
		        		  List <RowSorter.SortKey> sortKeys  = new ArrayList<RowSorter.SortKey>();
		        		  sortKeys.add(new RowSorter.SortKey(column, SortOrder.ASCENDING));
		        		  setSortKeys(sortKeys);
		        		  super.toggleSortOrder(column);
	        	   }
		          }
		        }

			        
		 }
	 
}

class CustomComparator implements Comparator<String> {
	private SortOrder sort_order = null;
	
	@Override
	public int compare(String o1, String o2) {
        if (o1.trim().length() == 0 && o2.trim().length() == 0) {
            return 0;
       }
        
        if (o1.trim().length() == 0) {
        	if(sort_order == null) {
        		return 1;
        	}
        	if(sort_order == SortOrder.ASCENDING) return -1;
            else return 1;
        }

        if (o2.trim().length() == 0) {
        
        	if(sort_order == null) {
        		return -1;
        	}
        	 if(sort_order == SortOrder.ASCENDING) return 1;
        	   else return -1;
        }

        
        return  o1.compareTo(o2);
    }

	public String getOrder() {
		if(sort_order == null) return "null";
		return sort_order.toString();
	}

	public void setOrder(SortOrder order) {
		sort_order = order;
	}
	
	@Override
	public String toString() {
		return "sort_order = "+getOrder();
	}
	
}
