package msmb.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.apache.commons.lang3.tuple.MutablePair;

import msmb.commonUtilities.ChangedElement;
import msmb.commonUtilities.MSMB_Element;
import msmb.commonUtilities.MSMB_InterfaceChange;
import msmb.commonUtilities.tables.CustomJTable;
import msmb.commonUtilities.tables.CustomTableModel;
import msmb.commonUtilities.tables.CustomTableModelListener;
import msmb.commonUtilities.tables.EditableCellRenderer;
import msmb.commonUtilities.tables.ScientificFormatCellRenderer;
import msmb.commonUtilities.tables.UnquotingCellEditor;
import msmb.debugTab.DebugConstants;
import msmb.debugTab.DebugMessage;
import msmb.model.Function;
import msmb.parsers.mathExpression.MR_Expression_Parser;
import msmb.parsers.mathExpression.syntaxtree.SingleFunctionCall;
import msmb.parsers.mathExpression.visitor.GetFunctionNameVisitor;
import msmb.utility.Constants;
import msmb.utility.GraphicalProperties;

public class CustomTableModel_MSMB extends CustomTableModel{
	protected transient MainGui gui;
   private transient  MultistateBuilderFrame multiStateDialog;
		
   public CustomTableModel_MSMB(String name, Vector<String> all, Vector<Integer> booleanColsIndx, MainGui w){
	   super(name);
		column0_referenceIndex = true;
		this.setColumnNames(all, booleanColsIndx);
		this.gui = w;
		initializeTableModel();
	}
	
	public CustomTableModel_MSMB(String name, Vector<String> all, Vector<Integer> booleanColsIndx, MultistateBuilderFrame w){
		super(name);
		column0_referenceIndex = true;
		this.setColumnNames(all, booleanColsIndx);
		this.multiStateDialog = w;
		this.initializeTableModel();
	}
	
	
	public CustomTableModel_MSMB(String name, Vector<String> all, Vector<Integer> booleanColsIndx, MainGui w, boolean alwaysEmptyRow, boolean column0_referenceIndex){
		super(name);
		this.alwaysEmptyRow = alwaysEmptyRow;
		this.column0_referenceIndex = column0_referenceIndex;
		this.setColumnNames(all, booleanColsIndx);
		this.gui = w;
		this.initializeTableModel();
	}
	
	public CustomTableModel_MSMB(String name,Vector<String> all, Vector<Integer> booleanColsIndx, MultistateBuilderFrame w, boolean alwaysEmptyRow, boolean column0_referenceIndex){
		super(name);
		this.alwaysEmptyRow = alwaysEmptyRow;
		this.column0_referenceIndex = column0_referenceIndex;
		this.setColumnNames(all, booleanColsIndx);
		this.multiStateDialog = w;
		this.initializeTableModel();
	}
	
	public Class getColumnClass(int c) {
		Object element = getValueAt(0, c);
		if(element == null) {
			System.out.println(this.getTableName()+" null at column "+c);
		}
        return element.getClass();
   }
	
	 public void setValueAt_old(Object value, int row, int col) {
	       MainGui.cell_to_highlight = null;
	       if(row < 0) return;
	       Vector r = (Vector)data.get(row);
		   Object old = r.get(col);
		   r.set(col, value);
		   data.set(row, r);
		   try{
			   if(gui != null) {
				   int tabSelected = -1;
				   if(tableName.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription())==0) tabSelected = Constants.TitlesTabs.COMPARTMENTS.index;
				   else if(tableName.compareTo(Constants.TitlesTabs.DEBUG.getDescription())==0) tabSelected = Constants.TitlesTabs.DEBUG.index;
				   else if(tableName.compareTo(Constants.TitlesTabs.EQUATIONS.getDescription())==0) tabSelected = Constants.TitlesTabs.EQUATIONS.index;
				   else if(tableName.compareTo(Constants.TitlesTabs.EVENTS.getDescription())==0) tabSelected = Constants.TitlesTabs.EVENTS.index;
				   else if(tableName.compareTo(Constants.TitlesTabs.FUNCTIONS.getDescription())==0) tabSelected = Constants.TitlesTabs.FUNCTIONS.index;
				   else if(tableName.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())==0) tabSelected = Constants.TitlesTabs.GLOBALQ.index;
				   else if(tableName.compareTo(Constants.TitlesTabs.SPECIES.getDescription())==0) tabSelected = Constants.TitlesTabs.SPECIES.index;
				   else if(tableName.compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0) { 
					   MainGui.addedByReaction = true; tabSelected = Constants.TitlesTabs.REACTIONS.index;
				   }
				   
				   gui.jTabGeneral.setSelectedIndex(tabSelected);
	 			   gui.updateModelFromTable(row, col);
			   }
			   else if(this.multiStateDialog != null) multiStateDialog.updateMultisiteSpeciesConcentrationFromTable(row);
		   } catch(Throwable ex) {
			   if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
			   r.set(col, old);
			   data.set(row, r);
			   return;
		   }
		   fireTableDataChanged();
		   fireTableCellUpdated(row, col);
		   try {
			gui.validateMultiModel(false, false);
			} catch (Throwable e) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 
					e.printStackTrace();
			}
	    }
	    
	    public void setValueAt_old_withoutUpdate(Object value, int row, int col) {
	    	MainGui.cell_to_highlight = null;
	    	if(row == data.size()) this.addRow(new Vector()); //this happens if importFromSBMLorCPS because the listener that adds the last row is not invoked
	    	Vector r = (Vector)data.get(row);
	 	   Object old = r.get(col);
	 	   r.set(col, value);
	 	   data.set(row, r);
	 	   if(!MainGui.importFromSBMLorCPS) {
	 		   fireTableDataChanged();
	 	   	   fireTableCellUpdated(row, col);
	 	   }
	     }

	    
		
		@Override
	    public void setValueAt(Object value, int row, int column)
	    {
			if(getValueAt(row, column).toString().compareTo(value.toString())==0) return;
			
			MainGui.modelHasBeenModified = true;
			MSMB_InterfaceChange changeToReport = new MSMB_InterfaceChange(MSMB_Element.SOMETHING_CHANGED);
			changeToReport.setElementBefore(null);
			changeToReport.setElementAfter(null);
	        setValueAt(value, row, column, true);
	      
			if(!MainGui.donotFireSomethingChange) MainGui.setChangeToReport(changeToReport);
	    }

		  public void setValueAt_withoutUpdate(Object value, int row, int col) {
			  setValueAt_withoutUpdate(value, row, col, true);
		  }
		  
		  public void setValueAt_withoutUpdate(Object value, int row, int col, boolean undoable) {
		        if(this.tableName.compareTo(MainGui.cellTableEdited) != 0 || 
			    		row != MainGui.cellSelectedRow || col != MainGui.cellSelectedCol) {
			    	undoable = false;
			    }
			    UndoableEditListener listeners[] = getListeners(UndoableEditListener.class);
		        if (undoable == false || listeners == null)
		        {
		            this.setValueAt_old_withoutUpdate(value, row, col);
		             return;
		        }
		        
		    
		        //Object oldValue = getValueAt(row, col);
			    setValueAt_old_withoutUpdate(value, row, col);
		        /*JvCellEdit cellEdit = new JvCellEdit(this, oldValue, value, row, col);
		        UndoableEditEvent editEvent = new UndoableEditEvent(this, cellEdit);
		        for (UndoableEditListener listener : listeners)
		            listener.undoableEditHappened(editEvent);*/
		  }
		    public void setValueAt(Object value, int row, int column, boolean undoable)
		    {
			    if(this.tableName.compareTo(MainGui.cellTableEdited) != 0 || 
			    		row != MainGui.cellSelectedRow || column != MainGui.cellSelectedCol) {
			    	undoable = false;
			    }
		        UndoableEditListener listeners[] = getListeners(UndoableEditListener.class);
		        if (undoable == false || listeners == null)
		        {
		            this.setValueAt_old(value, row, column);
		            return;
		        }
		        
		       // Object oldValue = getValueAt(row, column);
			       
		        setValueAt_old(value, row, column);
		        /*JvCellEdit cellEdit = new JvCellEdit(this, oldValue, value, row, column);
		        UndoableEditEvent editEvent = new UndoableEditEvent(this, cellEdit);
		        for (UndoableEditListener listener : listeners)
		            listener.undoableEditHappened(editEvent);*/
		    }

			public MutablePair<Integer, Integer> getFunctionDefinitionIndex() {
				MutablePair<Integer, Integer> tableIndex_funRowIndex = new MutablePair<Integer, Integer>();
				String equation = gui.getViewIn(MainGui.cellTableEdited, MainGui.cellSelectedRow, MainGui.cellSelectedCol, Constants.Views.EDITABLE.index);//gui.getCellContent(new FoundElement(MainGui.cellTableEdited, MainGui.cellSelectedRow, MainGui.cellSelectedCol));
				try{
					InputStream is = new ByteArrayInputStream(equation.getBytes("UTF-8"));
					MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
					SingleFunctionCall root = parser.SingleFunctionCall();
					GetFunctionNameVisitor vis = new GetFunctionNameVisitor();
					root.accept(vis);
					String funName  = vis.getFunctionName();
					Function f = gui.multiModel.funDB.getFunctionByName(funName);
					boolean builtIn = false;
					if(f== null) {
						f = gui.multiModel.funDB.getBuiltInFunctionByName(funName);
						if(f == null) return null;
						builtIn = true;
					}
					int indexTable = -1;
					if(builtIn) indexTable = Constants.TitlesTabs.BUILTINFUNCTIONS.index;
					else indexTable = Constants.TitlesTabs.FUNCTIONS.index;
					
					tableIndex_funRowIndex.setLeft(indexTable);
					tableIndex_funRowIndex.setRight(gui.multiModel.funDB.getFunctionIndex(f));
					return tableIndex_funRowIndex;
				} catch(Throwable ex) {
					return null;
				}
			}

			public HashSet<String> cells_with_expanded_view = new HashSet<String>();


			public void setViewExpanded(int row, int column) {
				disableCell(row, column);
				cells_with_expanded_view.add(row+"_"+column);
			}
			
			public void setViewEditable(int row, int column) {
				enableCell(row, column);
				cells_with_expanded_view.remove(row+"_"+column);
			}

			public boolean isViewEditable(int row, int column) {
				return !cells_with_expanded_view.contains(row+"_"+column);
			}
}


class CustomTableModelListener_MSMB extends CustomTableModelListener{
	
	 
	 public CustomTableModelListener_MSMB(CustomTableModel_MSMB m) {
		 super(m);
	 }
	 
	 public void tableChanged(TableModelEvent e) {
		int rowChanged = e.getFirstRow();
	   if(rowChanged != mod.getRowCount()-1) { 
       		if(e.getColumn()!=-1) {
       			if(!MainGui.importFromSBMLorCPS) MainGui.addRemoveEmptyFields_inConsistencyChecks((CustomTableModel_MSMB) mod, mod.getTableName());
       		}
	   }
	 }
  }


class CustomJTable_MSMB extends CustomJTable  {
	private static final long serialVersionUID = 1L;
	JPopupMenu popupMenu = new JPopupMenu();
	private PopUpViewActionListener_2 editableListener;
	private PopUpViewActionListener_2 expandedListener;
	private PopUpViewActionListener_2 expandedAllListener;
	private PopUpViewActionListener_2 compressedListener;
	private PopUpViewActionListener_2 customListener;
	private PopUpViewActionListener_2 currentAsEditable;
	
	   
	public void initializeCustomTable(CustomTableModel_MSMB m) {
		super.initializeCustomTable(m);
		
	    if(model.getTableName().compareTo(Constants.MultistateBuilder_QUANTITIES_description)==0) 
	    	this.setAutoCreateRowSorter(true); 

	 
		if(this.getMouseListeners().length <=2) {
		    addMouseListener(new MouseAdapter()   {
		    
		    	private JMenuItem ackMenuItem;
		    	private JMenuItem copySignature;
				private JMenuItem errorMenuItem;
				
				//on different OS popupTrigger is recognized in different calls (either mouse pressed or mouse released or mouse click) so I call the same method for all cases
				public void mousePressed(MouseEvent e) {   	if(e.getButton() != MouseEvent.BUTTON1) mouseClicked(e); 		}
		    
				public void mouseReleased(MouseEvent e) {  if(e.getButton() != MouseEvent.BUTTON1) mouseClicked(e);     	}
		    	 
		        public void mouseClicked(MouseEvent e)      {
		      	  if (e.getButton() == MouseEvent.BUTTON1 || e.getModifiers() == MouseEvent.BUTTON1_MASK )   {
		        		revalidate(); // to recolor cell not highlighted any more
				        repaint(); // to display the cleared cells
				        
		                Point p = e.getPoint();
		                int row = rowAtPoint(p); 
		                int col = columnAtPoint(p); 
		                    
		            	if(model.getTableName().compareTo(Constants.MultistateBuilder_QUANTITIES_description) == 0) {
			        		MultistateBuilderFrame.row_to_highlight = row;
			        		return;
			        	}
		            	
		                if(row == -1 || col == -1) {
		                	//System.err.println("row at point "+row + ", col at point "+ col);
	        				return;
		                }
		        
		               if(MainGui.cellTableEdited.compareTo(model.getTableName()) !=0) MainGui.resetViewsInExpressions();
		                
		                MainGui.cellValueBeforeChange = model.getValueAt(row,col).toString();
		             	
		             	MainGui.cellSelectedRow= row;
		             	MainGui.cellSelectedCol=col;
		             	MainGui.cellTableEdited = model.getTableName();
		   
		             	highlightCellSelectedRow();
		             	
		            	if(model.getTableName().compareTo(Constants.TitlesTabs.SPECIES.getDescription()) == 0) {
		    		        	MSMB_InterfaceChange changeToReport = new MSMB_InterfaceChange(MSMB_Element.SELECTED_SPECIES);
		    		        	changeToReport.setElementBefore(null);
		    		        	 changeToReport.setElementAfter(new ChangedElement(model.getValueAt(row,Constants.SpeciesColumns.NAME.index).toString(), MSMB_Element.SELECTED_SPECIES));
		    		        	MainGui.setChangeToReport(changeToReport);
		            	} else if(model.getTableName().compareTo(Constants.TitlesTabs.GLOBALQ.getDescription()) == 0) {
		                	MSMB_InterfaceChange changeToReport = new MSMB_InterfaceChange(MSMB_Element.SELECTED_GLOBAL_QUANTITY);
	    		        	changeToReport.setElementBefore(null);
	    		        	 changeToReport.setElementAfter(new ChangedElement(model.getValueAt(row,Constants.GlobalQColumns.NAME.index).toString(), MSMB_Element.SELECTED_GLOBAL_QUANTITY));
	    		        	MainGui.setChangeToReport(changeToReport);
	    		        }
		            	
		             	try {
							if(!MainGui.donotCleanDebugMessages) MainGui.clear_debugMessages_defaults_relatedWith(MainGui.cellTableEdited, MainGui.cellSelectedRow+1);
						} catch (Throwable e1) {
							e1.printStackTrace();
						}
		             } else {
		     		       
		            	showPopup(e);
		            }
		        }
		        
		        private void showPopup(MouseEvent e) {
		        	if(MainGui.displayTablesUneditable) return;
	                
		        	if (e.isPopupTrigger()) {
		        		if (isEditing()){ getCellEditor().stopCellEditing(); }
		        		
		        		boolean showPopup = false;
		            	popupMenu.removeAll();
			            Point p = e.getPoint();
			             final int row = rowAtPoint(p); 
			             final int col = columnAtPoint(p); 
			             if(row == -1 || col == -1) return;
			            
			         	MainGui.cellSelectedRow=row;
		             	MainGui.cellSelectedCol=col;
		             	MainGui.cellTableEdited = model.getTableName();
		             	highlightCellSelectedRow();
		             	
		             	
		             	
			             if(MainGui.isCellWithDefaultValue(model.getTableName(), row, col)) {
			            	 showPopup = true;
			            	 DebugMessage dm = new DebugMessage();
				             dm.setOrigin_table(model.getTableName());
				             dm.setProblem("");
				             dm.setPriority(DebugConstants.PriorityType.DEFAULTS.priorityCode);
				             dm.setOrigin_col(col);
				             dm.setOrigin_row(row+1);
							 MainGui.toBeAck_debugMessage =dm;
			            	 ackMenuItem = new JMenuItem("Acknowledge default value");
			    			 ackMenuItem.addActionListener(new ActionListener() {
			    				  public void actionPerformed(ActionEvent e) {
			    					  try {
			    						MainGui.ackSelectedDebugMessage();
			    					} catch (Throwable e1) {
			    						if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e1.printStackTrace();
			    					}
			    				  }
			    			  });
			    		    popupMenu.insert(ackMenuItem,0);
			    		 } else {
			    			 if(ackMenuItem!= null) popupMenu.remove(ackMenuItem);
			    		 }
			            
			             if(MainGui.isCellWithErrors(model.getTableName(), row, col)) {
			            	 	 showPopup = true;
			               	    errorMenuItem = new JMenuItem("Go to error");
			               	    errorMenuItem.addActionListener(new ActionListener() {
			         			  @Override
			         			  public void actionPerformed(ActionEvent e) {
			         				  			MainGui.goToError(model.getTableName(), row, col);
			         			  }
			         				});
			    		    popupMenu.insert(errorMenuItem,0);
			    		 } else {
			    			 if(errorMenuItem!= null) popupMenu.remove(errorMenuItem);
			    		 }
			             
			             if(MainGui.isCellWithMultipleView(model.getTableName(), row, col) && isCellEditable(row, col)  && !MainGui.isCellWithErrors(model.getTableName(), row, col)) {
			                
			            	 MainGui.cellTableEdited = model.getTableName();
			            	 
			            	JMenuItem editableView = new JMenuItem(Constants.Views.EDITABLE.getDescription());
			         	    editableListener = new PopUpViewActionListener_2(Constants.Views.EDITABLE.index);
			         		editableView.addActionListener(editableListener);
			         		popupMenu.add(editableView);
			         	    
			         		JMenuItem expandedView = new JMenuItem(Constants.Views.EXPANDED.getDescription());
			         	    expandedListener = new PopUpViewActionListener_2(Constants.Views.EXPANDED.index);
			         		expandedView.addActionListener(expandedListener);
			         		popupMenu.add(expandedView);
			         	
			         		popupMenu.addSeparator();
			         		
			         		
			         	/*	if(model.getTableName().compareTo(Constants.TitlesTabs.REACTIONS.getDescription())!=0) {
			         			JMenuItem expandedAllView = new JMenuItem(Constants.Views.EXPANDED_ALL.getDescription());
			         			expandedAllListener = new PopUpViewActionListener_2(Constants.Views.EXPANDED_ALL.index);
				         		expandedAllView.addActionListener(expandedAllListener);
				         		expandedAllView.setEnabled(false);
				         		popupMenu.add(expandedAllView);
			         		}*/
			         		
			         		/*JMenuItem compressedView = new JMenuItem(Constants.Views.COMPRESSED.getDescription());
			         	    //compressedListener = new PopUpViewActionListener(Constants.Views.COMPRESSED.index);
			         		//compressedView.addActionListener(compressedListener);
			         		//popupMenu.add(compressedView); // compressed is the editable*/
			         		
			         	    JMenuItem customView = new JMenuItem(Constants.Views.CUSTOM.getDescription());
			         	    customListener = new PopUpViewActionListener_2(Constants.Views.CUSTOM.index);
			         	    customView.addActionListener(customListener);
			         	    // popupMenu.addSeparator();
				         	//popupMenu.add(customView);
			         		
			         	    
			         	   if(model.getTableName().compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0 ||
			         		  (model.getTableName().compareTo(Constants.TitlesTabs.SPECIES.getDescription())==0 && col == Constants.SpeciesColumns.EXPRESSION.index)	   ||
			         		 (model.getTableName().compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())==0 && col == Constants.GlobalQColumns.EXPRESSION.index)	 
			         		  
			         			   ) {
			         			 JMenuItem textAreaExpression = new JMenuItem("Edit in Text Area");
			         			 textAreaExpression.addActionListener(new ActionListener() {
			         				  @Override
			         				  public void actionPerformed(ActionEvent e) {
			         					 String newExpression = MainGui.openTextAreaExpression(model.getValueAt(row, col).toString());
			         					 model.setValueAt(newExpression, row, col);
			         				  }
			         			  });
			         			  popupMenu.add(textAreaExpression);	
			         		  
			         	   }
			         	     
			         	   if(model.getTableName().compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0 && !MainGui.isCellWithErrors(model.getTableName(), row, col)) {
			         			  JMenuItem goToDefinition = new JMenuItem("Go to Function Definition");
			         			  goToDefinition.addActionListener(new ActionListener() {
			         				  @Override
			         				  public void actionPerformed(ActionEvent e) {
			         					  MutablePair<Integer, Integer> index = ((CustomTableModel_MSMB)model).getFunctionDefinitionIndex();

			         					  if(index != null) {
			         						  index.right = index.right -1; //because the returned one is the nrow not the row in the table
			         						  if(index.left == Constants.TitlesTabs.FUNCTIONS.index) {
			         							  MainGui.jTableFunctions.setRowSelectionInterval(index.right, index.right);
			         							  MainGui.jTableFunctions.scrollRectToVisible(new Rectangle(MainGui.jTableFunctions.getCellRect(index.right+5, 0, true))); 
			         							  MainGui.jTableFunctions.revalidate();
			         							  MainGui.row_to_highlight.set(Constants.TitlesTabs.FUNCTIONS.index, index.right);
			         							  MainGui.jTableBuiltInFunctions.clearSelection();
			         						  } else {
			         							  MainGui.jTableBuiltInFunctions.setRowSelectionInterval(index.right, index.right);
			         							  MainGui.jTableBuiltInFunctions.scrollRectToVisible(new Rectangle(MainGui.jTableBuiltInFunctions.getCellRect(index.right+5, 0, true))); 
			         							  MainGui.jTableBuiltInFunctions.revalidate();
			         							  MainGui.row_to_highlight.set(Constants.TitlesTabs.BUILTINFUNCTIONS.index, index.right);
			         							  MainGui.jTableFunctions.clearSelection();
			         						  }
			         						  MainGui.jTabGeneral.setSelectedIndex(Constants.TitlesTabs.FUNCTIONS.index);

			         					  }
			         				  }
			         			  });
			         			  popupMenu.add(goToDefinition);	
			         		  
			         	   }
			         	     
			         	  if(model.getTableName().compareTo(Constants.TitlesTabs.REACTIONS.getDescription())!=0) {  
			         		  popupMenu.addSeparator();
			         	     JMenuItem setCurrentAsEditable = new JMenuItem(Constants.Views.CURRENT_AS_EDITABLE.getDescription());
			         	     currentAsEditable = new PopUpViewActionListener_2(Constants.Views.CURRENT_AS_EDITABLE.index);
			         	     setCurrentAsEditable.addActionListener(currentAsEditable);
			         		 popupMenu.add(setCurrentAsEditable);
			         	  }
			         		
			         		if(editableListener != null) {
			         			editableListener.setTable(model.getTableName());
			         			editableListener.setRow(row);
			         			editableListener.setColumn(col);
			         		}
			         		if(expandedListener != null) {
			         			expandedListener.setTable(model.getTableName());
			         			expandedListener.setRow(row);
			         			expandedListener.setColumn(col);
			         		}
			         		if(expandedAllListener != null) {
			         			expandedAllListener.setTable(model.getTableName());
			         			expandedAllListener.setRow(row);
			         			expandedAllListener.setColumn(col);
			         		}
			         		if(compressedListener != null) {
			         			compressedListener.setTable(model.getTableName());
			         			compressedListener.setRow(row);
			         			compressedListener.setColumn(col);
			         		}
			         		if(customListener != null) {
			         			customListener.setTable(model.getTableName());
			         			customListener.setRow(row);
			         			customListener.setColumn(col);
			         		}
	
			         		if(currentAsEditable != null) {
			         			currentAsEditable.setTable(model.getTableName());
			         			currentAsEditable.setRow(row);
			         			currentAsEditable.setColumn(col);
			         		}
				             showPopup = true;
				         }
			             
			             if(model.getTableName().compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0
			            		 && col == Constants.ReactionsColumns.REACTION.index && !isCellEditable(row, col)) {
		         			  //reaction linked to a complex
		         			 JMenuItem unlinkReaction = new JMenuItem("Unlink reaction");
		         			unlinkReaction.addActionListener(new ActionListener() {
		         				  @Override
		         				  public void actionPerformed(ActionEvent e) {
		         					  MainGui.unlinkReaction(row);
		         				  }
		         			  });
		         			  popupMenu.add(unlinkReaction);	
		         			 showPopup = true;
			             }
			             
			             if(model.getTableName().compareTo(Constants.TitlesTabs.FUNCTIONS.getDescription())==0 && !MainGui.isCellWithErrors(model.getTableName(), row, col)) {
			            	 copySignature = new JMenuItem("Copy signature to Clipboard");
			            	 copySignature.addActionListener(new ActionListener() {
			    				  public void actionPerformed(ActionEvent e) {
			    					  try {
			    						  String typedText = (String) model.getValueAt(row, Constants.FunctionsColumns.NAME.index);
			    						  Clipboard clipboard = getToolkit().getSystemClipboard();
			    					  clipboard.setContents(new StringSelection(typedText), null );
			    					  MainGui.copiedSignature=true;
			    					} catch (Exception e1) {
			    						if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e1.printStackTrace();
			    					}
			    				  }
	
								
			    			  });
			    		    popupMenu.insert(copySignature,0);
			    		    showPopup = true;
			             } else {
			    			 if(copySignature!= null) popupMenu.remove(copySignature);
			    		 }
			             
			             
			             
			             
			             if(model.getTableName().compareTo(Constants.TitlesTabs.SPECIES.getDescription())==0
			            		 && col == Constants.SpeciesColumns.EXPRESSION.index && !isCellEditable(row, col)
			            		 && ((String) MainGui.tableSpeciesmodel.getValueAt(row, Constants.SpeciesColumns.TYPE.index)).compareTo(Constants.SpeciesType.MULTISTATE.getDescription()) == 0) {
		         			 // SUM with indexes from reaction
			            	 popupMenu.removeAll();
			            	 JMenuItem textAreaExpression = new JMenuItem("Edit in Text Area");
		         			 textAreaExpression.addActionListener(new ActionListener() {
		         				  @Override
		         				  public void actionPerformed(ActionEvent e) {
		         					  MainGui.openTextAreaExpression(row);
		         				  }
		         			  });
		         			  popupMenu.add(textAreaExpression);	
		         			 showPopup = true;
			             }
			             
			             
			              if(showPopup) {
			            	 popupMenu.show(e.getComponent(), e.getX(), e.getY());
				         }
				
	            }
	          }
	    });
		}    
	}
	


	 protected void highlightCellSelectedRow() {
		 
		 int row = MainGui.cellSelectedRow;
		 
		 if(MainGui.cellTableEdited.compareTo(Constants.TitlesTabs.SPECIES.getDescription())==0) { 
     		MainGui.row_to_highlight.set(Constants.TitlesTabs.SPECIES.index, row);	
     		MainGui.jTableSpecies.setRowSelectionInterval(row, row);
     		MainGui.jTableSpecies.revalidate();
     	}
     	else if(MainGui.cellTableEdited.compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0) { 
     		MainGui.row_to_highlight.set(Constants.TitlesTabs.REACTIONS.index, row);
     		MainGui.jTableReactions.setRowSelectionInterval(row, row);
     		MainGui.jTableReactions.revalidate();
     		}
     	else if(MainGui.cellTableEdited.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())==0) { 
     		MainGui.row_to_highlight.set(Constants.TitlesTabs.GLOBALQ.index, row);	
     		MainGui.jTableGlobalQ.setRowSelectionInterval(row, row);
     		MainGui.jTableGlobalQ.revalidate();
     		}
     	else if(MainGui.cellTableEdited.compareTo(Constants.TitlesTabs.FUNCTIONS.getDescription())==0) {
     		MainGui.row_to_highlight.set(Constants.TitlesTabs.FUNCTIONS.index, row);
     		MainGui.jTableFunctions.setRowSelectionInterval(row, row);
     		MainGui.jTableFunctions.revalidate();
    		MainGui.row_to_highlight.set(Constants.TitlesTabs.BUILTINFUNCTIONS.index, -1);
     		MainGui.jTableBuiltInFunctions.clearSelection();
     		MainGui.jTableBuiltInFunctions.revalidate();
     
     	}
     	else if(MainGui.cellTableEdited.compareTo(Constants.TitlesTabs.BUILTINFUNCTIONS.getDescription())==0) {
     		MainGui.row_to_highlight.set(Constants.TitlesTabs.BUILTINFUNCTIONS.index, row);
     		MainGui.jTableBuiltInFunctions.setRowSelectionInterval(row, row);
     		MainGui.jTableBuiltInFunctions.revalidate();
     		MainGui.row_to_highlight.set(Constants.TitlesTabs.FUNCTIONS.index, -1);
     		MainGui.jTableFunctions.clearSelection();
     		MainGui.jTableFunctions.revalidate();
     	}
     	else if(MainGui.cellTableEdited.compareTo(Constants.TitlesTabs.EVENTS.getDescription())==0) { 
     		MainGui.row_to_highlight.set(Constants.TitlesTabs.EVENTS.index, row);	
     		MainGui.jTableEvents.setRowSelectionInterval(row, row);
     		MainGui.jTableEvents.revalidate();
     		}
     	else if(MainGui.cellTableEdited.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription())==0) { 
     		MainGui.row_to_highlight.set(Constants.TitlesTabs.COMPARTMENTS.index, row);	
     		MainGui.jTableCompartments.setRowSelectionInterval(row, row);
     		MainGui.jTableCompartments.revalidate();
     	}
 	
	 }
	 
	 
	 @Override
	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
		if(MainGui.importFromSBMLorCPS) return null;
		Component c = null;
		try{
			c = super.prepareRenderer(renderer, rowIndex, vColIndex);
			
			
			updateDisabledCell_MSMB(rowIndex, vColIndex);
			
		
		
			
			//if(renderer instanceof EditableCellRenderer_2 ) {
				
				if(rowIndex== getRowCount()-1 && this.getValueAt(rowIndex, vColIndex).toString().trim().length() ==0) {
					if(c.getBackground() != GraphicalProperties.color_cell_with_errors && c.getBackground() != GraphicalProperties.color_cell_to_highlight )
					{
						if (rowIndex % 2 != 0) {
							c.setBackground(GraphicalProperties.color_shading_table);
							
						} else {
							c.setBackground(Color.white);
						}
					}
					
					
				}
				
				clearSelection();
				
			    if(customFont!=null) c.setFont(customFont);
			    if(this.isRowSelected(rowIndex)) {
					c.setBackground(GraphicalProperties.color_cell_to_highlight);
					c.setFont(c.getFont().deriveFont(Font.BOLD));
					if(vColIndex== 0) {
						c.setForeground(Color.BLACK);
					}
				} else {
					if(vColIndex== 0) {
						c.setBackground(GraphicalProperties.color_shading_table);
						c.setForeground(Color.BLACK);
					}
					else {
						if(c.getBackground() != GraphicalProperties.color_cell_with_errors && c.getBackground() != GraphicalProperties.color_cell_to_highlight )
						{
						if (rowIndex % 2 != 0) {
							c.setBackground(GraphicalProperties.color_shading_table);
						} else {
							c.setBackground(Color.white);
						}
						
					 }
					}
				}
				
				
		//	}
			
			
				
			if(this.model.disabledCell.contains(rowIndex+"_"+vColIndex)) {
				c.setBackground(Color.LIGHT_GRAY);
				
				if(this.model.getTableName().compareTo(Constants.TitlesTabs.BUILTINFUNCTIONS.getDescription())==0) c.setForeground(Color.GRAY);
				else if(this.model.getTableName().compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0 && vColIndex==Constants.ReactionsColumns.KINETIC_LAW.index) c.setForeground(Color.BLACK);
				else if(this.model.getTableName().compareTo(Constants.MultistateBuilder_QUANTITIES_description)==0) c.setForeground(Color.BLACK);
				else if(this.model.getTableName().compareTo(Constants.TitlesTabs.SPECIES.getDescription())==0) c.setForeground(Color.DARK_GRAY);
				else if(this.model.getTableName().compareTo(Constants.TitlesTabs.SPECIES.getDescription())==0 && vColIndex==Constants.SpeciesColumns.INITIAL_QUANTITY.index) c.setForeground(c.getBackground());
				else if(!((CustomTableModel_MSMB)model).isViewEditable(rowIndex, vColIndex)) c.setForeground(Color.BLACK);
				else {
					if(this.isRowSelected(rowIndex)) {
						c.setForeground(GraphicalProperties.color_cell_to_highlight);
					}
					else c.setForeground(Color.GRAY);
				}
			} else {
				c.setForeground(getForeground());
			}
			
	
			
			if(isCellWithDefaults(rowIndex,vColIndex) && !(this.model.disabledCell.contains(rowIndex+"_"+vColIndex))){
				Border compound = null;
				Border redline = BorderFactory.createLineBorder(GraphicalProperties.color_border_defaults,3);
				compound = BorderFactory.createCompoundBorder(redline, compound);
				if(c instanceof EditableCellRenderer_MSMB) ((EditableCellRenderer_MSMB)c).setBorder(compound);
				else if(c instanceof ScientificFormatCellRenderer) ((ScientificFormatCellRenderer)c).setBorder(compound);
			} 
			
		if((isCellWithError(rowIndex,vColIndex))
					//&&isRowSelected(rowIndex)
					){
				Border compound = null;
				Border redline = BorderFactory.createLineBorder(GraphicalProperties.color_cell_with_errors,3);
				compound = BorderFactory.createCompoundBorder(redline, compound);
				if(c instanceof EditableCellRenderer_MSMB) ((EditableCellRenderer_MSMB)c).setBorder(compound);
				else if(c instanceof ScientificFormatCellRenderer) ((ScientificFormatCellRenderer)c).setBorder(compound);
			} 
			
			if((isCellWithMinorIssue(rowIndex,vColIndex))&&isRowSelected(rowIndex)&&GraphicalProperties.color_cell_with_minorIssues!=null){
				Border compound = null;
				Border redline = BorderFactory.createLineBorder(GraphicalProperties.color_cell_with_errors,3);
				compound = BorderFactory.createCompoundBorder(redline, compound);
				if(c instanceof EditableCellRenderer_MSMB) ((EditableCellRenderer_MSMB)c).setBorder(compound);
				else if(c instanceof ScientificFormatCellRenderer) ((ScientificFormatCellRenderer)c).setBorder(compound);
			} 
		
			if(this.model.disabledCell.contains(rowIndex+"_"+vColIndex)){
				if(isRowSelected(rowIndex)){
						c.setBackground(GraphicalProperties.color_cell_to_highlight);
						c.setForeground(Color.GRAY);
						Border compound = null;
						Border redline = BorderFactory.createLineBorder(Color.LIGHT_GRAY,3);
						compound = BorderFactory.createCompoundBorder(redline, compound);
						if(c instanceof EditableCellRenderer_MSMB) ((EditableCellRenderer_MSMB)c).setBorder(compound);
						else if(c instanceof ScientificFormatCellRenderer) ((ScientificFormatCellRenderer)c).setBorder(compound);
						else if(c instanceof DefaultTableCellRenderer) ((DefaultTableCellRenderer)c).setBorder(compound);
				}
				if(isCellWithError(rowIndex,vColIndex)) {
					Border compound = null;
					Border redline = BorderFactory.createLineBorder(GraphicalProperties.color_cell_with_errors,3);
					compound = BorderFactory.createCompoundBorder(redline, compound);
					if(c instanceof EditableCellRenderer_MSMB) ((EditableCellRenderer_MSMB)c).setBorder(compound);
					else if(c instanceof ScientificFormatCellRenderer) ((ScientificFormatCellRenderer)c).setBorder(compound);
				}	else {
					if(c instanceof EditableCellRenderer_MSMB) ((EditableCellRenderer_MSMB)c).setBorder(null);
					else if(c instanceof ScientificFormatCellRenderer) ((ScientificFormatCellRenderer)c).setBorder(null);
				}
			} 
			
	
			
			if(MainGui.displayTablesUneditable) {
				c.setBackground(Color.LIGHT_GRAY);
				if(this.isRowSelected(rowIndex)) {
					c.setForeground(GraphicalProperties.color_cell_to_highlight);
				}
			}
				
			return c;
			
		} catch(Exception ex) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
			System.out.println("Problems rendering column: "+this.model.getColumnName(vColIndex));
			if(customFont!=null)  c.setFont(customFont);
 		}
		return c;
		
	}

		
	@Override
	public boolean isRowSelected(int row) {
		int selRow = -1;
		if(model.getTableName().compareTo(Constants.MultistateBuilder_QUANTITIES_description)==0) {
     		selRow=MultistateBuilderFrame.row_to_highlight;
     	} else	{
     		selRow = MainGui.row_to_highlight.get(MainGui.jTabGeneral.getSelectedIndex());
     	}
     	return row==selRow;
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		if(MainGui.displayTablesUneditable) return false;
		return super.isCellEditable(row, column);
	}
	
	

	private void updateDisabledCell_MSMB(int row, int col) {
		if(MainGui.displayTablesUneditable) return;
		CustomTableModel_MSMB model = (CustomTableModel_MSMB) this.model;
		if(model.getTableName().compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())==0) {
				if(model.getValueAt(row, Constants.GlobalQColumns.TYPE.index).toString().compareTo(Constants.GlobalQType.ASSIGNMENT.getDescription())==0) {
				if(model.isViewEditable(row,Constants.GlobalQColumns.EXPRESSION.index)) 
					model.enableCell(row, Constants.GlobalQColumns.EXPRESSION.index);
				model.disableCell(row, Constants.GlobalQColumns.VALUE.index);	
			} else {
				if(model.isViewEditable(row,Constants.GlobalQColumns.VALUE.index)) 
								model.enableCell(row, Constants.GlobalQColumns.VALUE.index);
				if(model.isViewEditable(row,Constants.GlobalQColumns.EXPRESSION.index)) 
					model.enableCell(row, Constants.GlobalQColumns.EXPRESSION.index); //because ode needs it
		 		if(model.getValueAt(row, Constants.GlobalQColumns.TYPE.index).toString().compareTo(Constants.GlobalQType.FIXED.getDescription())==0) {
		 			model.disableCell(row, Constants.GlobalQColumns.EXPRESSION.index);
		 		}
	 		}
		}
		
		else	if(model.getTableName().compareTo(Constants.TitlesTabs.SPECIES.getDescription())==0) {
			String typeS = model.getValueAt(row, Constants.SpeciesColumns.TYPE.index).toString();
			if(typeS.compareTo(Constants.SpeciesType.ASSIGNMENT.getDescription())==0) {
				if(model.isViewEditable(row,Constants.SpeciesColumns.EXPRESSION.index)) 
					model.enableCell(row, Constants.SpeciesColumns.EXPRESSION.index);
				model.disableCell(row, Constants.SpeciesColumns.INITIAL_QUANTITY.index);
			} else 	if(typeS.compareTo(Constants.SpeciesType.MULTISTATE.getDescription())==0 || typeS.compareTo(Constants.SpeciesType.COMPLEX.getDescription())==0) {
				model.disableCell(row, Constants.SpeciesColumns.EXPRESSION.index);
				model.disableCell(row, Constants.SpeciesColumns.INITIAL_QUANTITY.index);
			}else if(typeS.compareTo(Constants.SpeciesType.ODE.getDescription())==0) {
				if(model.isViewEditable(row,Constants.SpeciesColumns.INITIAL_QUANTITY.index)) 
					model.enableCell(row, Constants.SpeciesColumns.INITIAL_QUANTITY.index);
				
				if(model.isViewEditable(row,Constants.SpeciesColumns.EXPRESSION.index)) 
						model.enableCell(row, Constants.SpeciesColumns.EXPRESSION.index); //because ode needs it
			}	else if(typeS.compareTo(Constants.SpeciesType.FIXED.getDescription())==0) {
				model.disableCell(row, Constants.SpeciesColumns.EXPRESSION.index);
				if(model.isViewEditable(row,Constants.SpeciesColumns.INITIAL_QUANTITY.index)) 
						model.enableCell(row, Constants.SpeciesColumns.INITIAL_QUANTITY.index);
	 		}
			else if(typeS.compareTo(Constants.SpeciesType.REACTIONS.getDescription())==0) {
				model.disableCell(row, Constants.SpeciesColumns.EXPRESSION.index);
				if(model.isViewEditable(row,Constants.SpeciesColumns.INITIAL_QUANTITY.index)) 
					model.enableCell(row, Constants.SpeciesColumns.INITIAL_QUANTITY.index);
		}
		
		}
	}

	
};



class EditableCellRenderer_MSMB extends EditableCellRenderer {
	private static final long serialVersionUID = 1L;
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

	  super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		/*		CustomTableModel_MSMB mod = (CustomTableModel_MSMB)(((CustomJTable_MSMB)table).getModel());
				String descr = mod.getTableName();
				String def = null;
				if(descr.compareTo(Constants.TitlesTabs.SPECIES.getDescription())==0) {	mod = MainGui.tableSpeciesmodel;
					if(column == Constants.SpeciesColumns.INITIAL_QUANTITY.index) def = MainGui.species_defaultInitialValue;
					if(column == Constants.SpeciesColumns.COMPARTMENT.index) def = MainGui.compartment_default_for_dialog_window;
				}
				/*else if(descr.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription())==0) {	mod = MainGui.tableCompartmentsmodel;	
				if(column == Constants.CompartmentsColumns.INITIAL_SIZE.index) def = MainGui.compartment_defaultInitialValue;
				if(column == Constants.CompartmentsColumns.NAME.index) def = MainGui.compartment_default_for_dialog_window;
				}
				else if(descr.compareTo(Constants.TitlesTabs.EVENTS.getDescription())==0) {	mod = MainGui.tableEventsmodel;	}
				else if(descr.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())==0) {	
					mod = MainGui.tableGlobalQmodel;	
					if(column == Constants.GlobalQColumns.VALUE.index) def = MainGui.globalQ_defaultValue_for_dialog_window;
				}
				else if(descr.compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0) {	mod = MainGui.tableReactionmodel;	}
				*/
				return this;

	}
	
	


}


class PopUpViewActionListener_2 implements ActionListener {

	int whichView = Constants.Views.EDITABLE.index;
	int column = 0;
	int row = 0;
	String table = new String();
	
	public PopUpViewActionListener_2(int view) {
		super();
		this.whichView = view;
		
	}
	
	public int getColumn() {return column;}
	public void setColumn(int column) {		this.column = column;	}
	public int getRow() {		return row;	}
	public void setRow(int row) {		this.row = row;	}
	public String getTable() {		return table;	}
	public void setTable(String table) {		this.table = table;	}

	public void actionPerformed(ActionEvent e) {
			 try {
				 MainGui.setView(whichView,table,row,column);
				 
			} catch (Throwable e1) {
				e1.printStackTrace();
			}
		  }
  }




