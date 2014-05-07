package  msmb.gui;

import msmb.utility.Constants;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;


import msmb.debugTab.FoundElement;
import msmb.debugTab.FoundElementToDelete;

public class TreeTableModel  extends DefaultTreeTableModel  {

	
	public TreeTableModel(TreeTableNode node) {	
		super(node); 
	}

	public int getColumnCount() {       return Constants.DeleteColumns.getNumColumns();      }

	
	public Object getValueAt(Object node, int column) {
		String toBeDisplayed = "";
		if (node instanceof DefaultMutableTreeTableNode) {
			DefaultMutableTreeTableNode defNode = (DefaultMutableTreeTableNode) node;
			if (defNode.getUserObject() instanceof FoundElementToDelete) {
				FoundElementToDelete element = (FoundElementToDelete) defNode.getUserObject();
				if (column == Constants.DeleteColumns.ELEMENT.index) 	{
					if(!(element.getTableDescription().compareTo(Constants.TitlesTabs.REACTIONS.getDescription()) ==0  && element.getCol()==Constants.ReactionsColumns.REACTION.index)) toBeDisplayed +="("+MainGui.printCellHeader(element) +") ";
					toBeDisplayed += MainGui.printCellContent(element);
				}
				else if (column == Constants.DeleteColumns.ACTION_TO_TAKE.index) 	{
					toBeDisplayed = element.getActionToTake();
					if(toBeDisplayed.toString().compareTo(Constants.DeleteActions.ASSIGN_NEW_VALUE.getDescription())==0) {
						String newValue = Constants.DeleteActions.ASSIGN_NEW_VALUE.custom_description;
						
						TreeTableNode parent = defNode.getParent();
						if(parent!=null) {
							if (parent.getUserObject() instanceof FoundElement) {
								FoundElement elementParent = (FoundElement) parent.getUserObject();
								newValue = newValue.replace("\"\"", "\""+MainGui.printMainElementRow(elementParent)+"\"");
								toBeDisplayed = newValue;
							}
						}
						
					}
				}
				else if (column == Constants.DeleteColumns.NEW_VALUE.index) 	{
					if (element.getActionToTake().compareTo(Constants.DeleteActions.ASSIGN_NEW_VALUE.getDescription())!= 0) toBeDisplayed ="";  
					else {
						String typeParentToDelete = null;
						TreeTableNode parent = defNode.getParent();
						if(parent!=null) {
							if (parent.getUserObject() instanceof FoundElement) {
								FoundElement elementParent = (FoundElement) parent.getUserObject();
								typeParentToDelete = elementParent.getTableDescription();
								if(element.getNewValue().length()==0) {
									//toBeDisplayed = MainGui.printMainElementRow(elementParent) + " = ??? ";
									if(typeParentToDelete.compareTo(Constants.TitlesTabs.SPECIES.getDescription())==0) {	toBeDisplayed = MainGui.species_defaultInitialValue;	}
									else if(typeParentToDelete.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription())==0) {toBeDisplayed = MainGui.compartment_defaultInitialValue;	}
									else if(typeParentToDelete.compareTo(Constants.TitlesTabs.EVENTS.getDescription())==0) {	toBeDisplayed = "";	}
									else if(typeParentToDelete.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())==0) {	toBeDisplayed = MainGui.globalQ_defaultValue_for_dialog_window;	}
									else if(typeParentToDelete.compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0) {	toBeDisplayed = "";		}
								
									element.setOldValue(MainGui.printMainElementRow(elementParent));
									element.setNewValue((String) toBeDisplayed);
									
								} else  toBeDisplayed = element.getNewValue();
								
						}
						}
					}
				
					
				}
				
			}
		}
		return toBeDisplayed;
	}

	 @Override
	 public String getColumnName(int column) {
	       return Constants.DeleteColumns.getDescriptionFromIndex(column);
	 }
        
	 
        /**
		 * Called when done editing a cell.
		 */
		public void setValueAt(Object value, Object node, int column) {
			if (node instanceof DefaultMutableTreeTableNode) {
				
				DefaultMutableTreeTableNode defNode = (DefaultMutableTreeTableNode) node;
				
				if (defNode.getUserObject() instanceof FoundElementToDelete) {
					FoundElementToDelete element = (FoundElementToDelete) defNode.getUserObject();
					if (column == Constants.DeleteColumns.ELEMENT.index) 	{
						element.setTableDescription(value.toString());
						
					}
					else if (column == Constants.DeleteColumns.ACTION_TO_TAKE.index)  {
						element.setActionToTake(value.toString());
						if(value.toString().startsWith(Constants.DeleteActions.ASSIGN_NEW_VALUE.getDescription())) {
							element.setActionToTake(Constants.DeleteActions.ASSIGN_NEW_VALUE.getDescription());
						} 
					}
					else if (column == Constants.DeleteColumns.NEW_VALUE.index)  {
						if(!value.toString().contains("???")) {
							element.setNewValue(value.toString());
						} else {
							element.setNewValue("");
						}
					}
					
					
				}
			
				

			}
			
			
			
			
			
			DeleteFrame.resetPartialViewOfCompleteMutableTree((DefaultMutableTreeTableNode) node);
		}
	


        
      @Override
        public boolean isCellEditable(Object node, int col)
        {
    	    
    	  	if (col != Constants.DeleteColumns.NEW_VALUE.index &&
    	  			col != Constants.DeleteColumns.ACTION_TO_TAKE.index	) return false;
    	  	
    	  	if (node instanceof DefaultMutableTreeTableNode) {
				DefaultMutableTreeTableNode defNode = (DefaultMutableTreeTableNode) node;
				if (defNode.getUserObject() instanceof FoundElementToDelete) {
					FoundElementToDelete element = (FoundElementToDelete) defNode.getUserObject();
					if(element.getTableDescription().compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0 && element.getCol()==Constants.ReactionsColumns.REACTION.index) return false;
					if(element.equals(DeleteFrame.rootElementToDelete)) return false;
					
					if(((FoundElementToDelete)((DefaultMutableTreeTableNode) node).getParent().getUserObject()).getTableDescription().compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription())==0) {
						if(element.getTableDescription().compareTo(Constants.TitlesTabs.SPECIES.getDescription())==0) {
							return false;
						}
					}
					
					if(element.getTableDescription().compareTo(Constants.TitlesTabs.EVENTS.getDescription())==0 && 
							element.getCol()==Constants.EventsColumns.ACTIONS.index &&
							MainGui.isOnLeftHandSide(element)) return false;
					
					
					if(col == Constants.DeleteColumns.ACTION_TO_TAKE.index) return true;
					if(element.getActionToTake().compareTo(Constants.DeleteActions.ASSIGN_NEW_VALUE.getDescription())==0) return true;
					return false;
				}
			}
    	  	return true;
			
        } 
        
      	
        
    }


