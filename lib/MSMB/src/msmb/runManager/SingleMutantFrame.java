package msmb.runManager;



import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.JButton;

import org.apache.commons.lang3.tuple.MutablePair;
import msmb.commonUtilities.tables.CustomTableModel;
import msmb.commonUtilities.tables.EditableCellRenderer;
import msmb.gui.CustomTableModel_MSMB;
import msmb.gui.MainGui;
import msmb.debugTab.DebugConstants;
import msmb.debugTab.DebugMessage;
import msmb.debugTab.FoundElement;
import msmb.debugTab.TreeDebugMessages;
import msmb.model.FunctionsDB;
import msmb.model.MultiModel;
import msmb.parsers.chemicalReaction.MR_ChemicalReaction_Parser;
import msmb.parsers.chemicalReaction.syntaxtree.CompleteReaction;
import msmb.parsers.chemicalReaction.visitor.ExtractNamesSpeciesUsedVisitor;
import msmb.parsers.mathExpression.MR_Expression_Parser;
import msmb.parsers.mathExpression.MR_Expression_Parser_ReducedParserException;
import msmb.parsers.mathExpression.syntaxtree.CompleteExpression;
import msmb.parsers.mathExpression.visitor.EvaluateExpressionVisitor;
import msmb.parsers.mathExpression.visitor.ExtractNamesUsedVisitor;
import msmb.parsers.mathExpression.visitor.Look4UndefinedMisusedVisitor;
import msmb.utility.CellParsers;
import msmb.utility.Constants;
import msmb.utility.GraphicalProperties;
import msmb.utility.ReversePolishNotation;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.border.TitledBorder;
import java.awt.Color;

public class SingleMutantFrame extends JDialog {

	private ExitOption exitOption = ExitOption.CANCEL;
	private JPanel contentPane;
	private CustomTableModel tableGLQmodel;
	private CustomJTable_SingleMutant jTableGLQ;
	private CustomTableModel tableSPCmodel;
	private CustomJTable_SingleMutant jTableSPC;
	private CustomTableModel tableRCTmodel;
	private CustomJTable_SingleMutant jTableRCT;
	private CustomTableModel tableEVmodel;
	private CustomJTable_SingleMutant jTableEV;
	private CustomTableModel tableFUNmodel;
	private CustomJTable_SingleMutant jTableFUN;
	private CustomTableModel tableCMPmodel;
	private CustomJTable_SingleMutant jTableCMP;
	private MultiModel multiModel;
	private JTabbedPane tabs;
	private JScrollPane jScrollPaneDebug;
	private JScrollPane jScrollPaneTableGlobalQ;
	private JScrollPane jScrollPaneTableSpecies;
	private JScrollPane jScrollPaneTableReactions;
	private JScrollPane jScrollPaneTableFunctions;
	private JScrollPane jScrollPaneTableEvents;
	private JScrollPane jScrollPaneTableCompartments;
	private Mutant currentMutant;
	private Vector<Mutant> currentAncestors;
	private HashSet<String> conflicts;
	private LocalChangeFrame localChangeFrame;
	private JPanel jPanelDebug;
	private TreeDebugMessages jPanelTreeDebugMessages_RM;
	private JTextField filter;
	private JPanel mainContentPane;
	private JPanel panel;
	private JLabel lblName;
	private JTextField textFieldName;
	private JPanel singleMutantPane;
	private JPanel panel_1;
	private JButton btnCancel;
	private MutantsDB mutDB;
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					SingleMutantFrame frame = new SingleMutantFrame(null,null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @param multiModel 
	 */
	public SingleMutantFrame(MultiModel multiModel, MutantsDB mutDB) {
		this.multiModel = multiModel;
		this.mutDB = mutDB;
		setTitle("Edit single mutant");
		setModal(true);

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(5, 5));
		
		tabs = new JTabbedPane();
		tabs.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		tabs.addChangeListener(new ChangeListener() {
		      public void stateChanged(ChangeEvent changeEvent) {
		    	  applyNewFilter();
		        }
		      });
		
		Vector col = new Vector(Constants.reactions_columns);


	
		col= new Vector(Constants.reactions_columns);
		tableRCTmodel = new CustomTableModel(Constants.TitlesTabs.REACTIONS.getDescription(),false);
		tableRCTmodel.setColumnNames(col, new Vector());
		tableRCTmodel.initializeTableModel();
		jTableRCT = new CustomJTable_SingleMutant(this, tableRCTmodel,MainGui.customFont );
		jTableRCT.setEnabled(false);
		tabs.addTab(Constants.TitlesTabs.REACTIONS.getDescription(), null, getJPanelReactions(), null);
	
		col= new Vector(Constants.species_columns);
		tableSPCmodel = new CustomTableModel(Constants.TitlesTabs.SPECIES.getDescription(),false);
		tableSPCmodel.setColumnNames(col, new Vector());
		tableSPCmodel.initializeTableModel();
		jTableSPC = new CustomJTable_SingleMutant(this, tableSPCmodel,MainGui.customFont );
		tabs.addTab(Constants.TitlesTabs.SPECIES.getDescription(), null, getJPanelSpecies(), null);
		
		
		col = new Vector(Constants.globalQ_columns);
		tableGLQmodel = new CustomTableModel(Constants.TitlesTabs.GLOBALQ.getDescription(),false);
		tableGLQmodel.setColumnNames(col, new Vector());
		tableGLQmodel.initializeTableModel();
		jTableGLQ = new CustomJTable_SingleMutant(this, tableGLQmodel,MainGui.customFont );
		tabs.addTab(Constants.TitlesTabs.GLOBALQ.getDescription(), null, getJPanelGlobalQ(), null);
		
		col = new Vector(Constants.events_columns);
		tableEVmodel = new CustomTableModel(Constants.TitlesTabs.EVENTS.getDescription(),false);
		tableEVmodel.setColumnNames(col, new Vector());
		tableEVmodel.initializeTableModel();
		jTableEV = new CustomJTable_SingleMutant(this, tableEVmodel,MainGui.customFont );
		tabs.addTab(Constants.TitlesTabs.EVENTS.getDescription(), null, getJPanelEvents(), null);
		
		
		col = new Vector(Constants.functions_columns);
		tableFUNmodel = new CustomTableModel(Constants.TitlesTabs.FUNCTIONS.getDescription(),false);
		tableFUNmodel.setColumnNames(col, new Vector());
		tableFUNmodel.initializeTableModel();
		jTableFUN = new CustomJTable_SingleMutant(this, tableFUNmodel,MainGui.customFont );
		tabs.addTab(Constants.TitlesTabs.FUNCTIONS.getDescription(), null, getJPanelFunctions(), null);
		
		col= new Vector(Constants.compartments_columns);
		tableCMPmodel = new CustomTableModel(Constants.TitlesTabs.COMPARTMENTS.getDescription(),false);
		tableCMPmodel.setColumnNames(col, new Vector());
		tableCMPmodel.initializeTableModel();
		jTableCMP = new CustomJTable_SingleMutant(this, tableCMPmodel,MainGui.customFont );
		tabs.addTab(Constants.TitlesTabs.COMPARTMENTS.getDescription(), null, getJPanelCompartments(), null);
	
		
		
		TableColumnModel colModel = jTableSPC.getColumnModel();
		colModel.getColumn(Constants.SpeciesColumns.INITIAL_QUANTITY.index).setCellRenderer(new EditableCellRenderer());
		colModel = jTableGLQ.getColumnModel();
		colModel.getColumn(Constants.GlobalQColumns.VALUE.index).setCellRenderer(new EditableCellRenderer());
	
		
		
		jScrollPaneDebug = new JScrollPane();
		jScrollPaneDebug.setViewportView(getJPanelDebug());
		tabs.addTab(Constants.TitlesTabs.DEBUG.getDescription(),  null, jScrollPaneDebug , null);
	
		contentPane.add(tabs);
		tabs.setPreferredSize(new Dimension(800,400));
		
		mainContentPane = new JPanel();
		mainContentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(mainContentPane);
		
		panel = new JPanel();
		panel.setBorder(new EmptyBorder(6, 6, 6, 6));
		mainContentPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		lblName = new JLabel("Name:  ");
		panel.add(lblName, BorderLayout.WEST);
		
		textFieldName = new JTextField();
		panel.add(textFieldName);
		textFieldName.setColumns(10);
		
		singleMutantPane = new JPanel();
		singleMutantPane.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), " ", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		singleMutantPane.setLayout(new BorderLayout(3, 3));
		
		singleMutantPane.add(contentPane, BorderLayout.CENTER);
		mainContentPane.add(singleMutantPane, BorderLayout.CENTER);
		
		JPanel panel_2 = new JPanel();
		singleMutantPane.add(panel_2, BorderLayout.NORTH);
		panel_2.setLayout(new BorderLayout());
		
		filter = new JTextField();
		filter.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
				  applyNewFilter();
			  }
			  public void removeUpdate(DocumentEvent e) {
				  applyNewFilter();
			  }
			  public void insertUpdate(DocumentEvent e) {
				  applyNewFilter();
			  }

			
			});
		JPanel panel_2_filter = new JPanel();
		panel_2_filter.setLayout(new BorderLayout());
		panel_2_filter.add(filter,BorderLayout.CENTER );
		panel_2_filter.add(new JLabel("  Filter rows:  "), BorderLayout.WEST );
		panel_2.add(panel_2_filter, BorderLayout.CENTER);
		
		panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		mainContentPane.add(panel_1, BorderLayout.SOUTH);
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitOption = ExitOption.CANCEL;
				dispose();
			}
		});
		panel_1.add(btnCancel);
		
		JButton btnSaveCurrentConfiguration = new JButton("Save configuration & Close");
		panel_1.add(btnSaveCurrentConfiguration);
		btnSaveCurrentConfiguration.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				exitOption = ExitOption.OK;
				dispose();
			}
		});
			
	}
	
	public void applyNewFilter() {
		if(filter==null) return;
		 int sel= tabs.getSelectedIndex();
	     if(sel == Constants.TitlesTabs.REACTIONS.index) {
	    	 jTableRCT.applyFilter(filter.getText());
	     } else if(sel == Constants.TitlesTabs.SPECIES.index) {
	    	 jTableSPC.applyFilter(filter.getText());
	     } else if(sel == Constants.TitlesTabs.GLOBALQ.index) {
	    	 jTableGLQ.applyFilter(filter.getText());
	     }   else if(sel == Constants.TitlesTabs.COMPARTMENTS.index) {
	    	 jTableCMP.applyFilter(filter.getText());
	     }  else if (sel == Constants.TitlesTabs.EVENTS.index) {
	    	 jTableEV.applyFilter(filter.getText());
	     } 
	}


	
	private JPanel getJPanelDebug() {
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.gridx = 0;
		jPanelDebug = new JPanel();
	
		jPanelDebug.setLayout(new BorderLayout());
		jPanelTreeDebugMessages_RM = new TreeDebugMessages(this);
		jPanelDebug.add(jPanelTreeDebugMessages_RM, BorderLayout.CENTER);
		
		return jPanelDebug;
}
	
	
	
	public void clear_debugMessages_relatedWith(String table, double priority, int row, int col) {
		String key = table+"@"+priority+"_"+row+"_"+col;
		
		if(currentMutant.debugMessages.get(key)==null) {
				decolorCell(row,col,table);
		}
		recolorCell(currentMutant.debugMessages.get(key),false);
		currentMutant.debugMessages.remove(key);
		updateDebugTab();
		
	}
	
	private void decolorCell(int row, int col, String tableDescription)  {
		CustomJTable_SingleMutant table = getTableFromDescription(tableDescription);
		table.cell_no_defaults(row,col);
		table.cell_no_errors(row,col);
		table.cell_no_minorIssue(row,col);
		table.revalidate(); 
	}
	
	
	public CustomJTable_SingleMutant getTableFromDescription(String descr) {
		CustomJTable_SingleMutant table;
		if(descr.compareTo(Constants.TitlesTabs.REACTIONS.getDescription()) == 0) { table = jTableRCT;	}
		else if(descr.compareTo(Constants.TitlesTabs.SPECIES.getDescription()) == 0) { table = jTableSPC;}
		else if(descr.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription()) == 0) { table = jTableCMP;	}
		else if(descr.compareTo(Constants.TitlesTabs.EVENTS.getDescription()) == 0) { table = jTableEV;	}
		else if(descr.compareTo(Constants.TitlesTabs.FUNCTIONS.getDescription()) == 0) { table = jTableFUN;	}
		else if(descr.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription()) == 0) { table = jTableGLQ;	}
		else table = null;
		return table;
	}
	
	private void recolorCell(DebugMessage dm, boolean asError) {
		if(dm==null) return;
		String tableDescr = dm.getOrigin_table();
		CustomJTable_SingleMutant table = getTableFromDescription(tableDescr);
		if(dm.getPriority()==DebugConstants.PriorityType.PARSING.priorityCode 
				|| dm.getPriority()==DebugConstants.PriorityType.INCONSISTENCIES.priorityCode
				|| dm.getPriority()==DebugConstants.PriorityType.MISSING.priorityCode
				|| dm.getPriority()== DebugConstants.PriorityType.EMPTY.priorityCode) {
	
				if(asError) table.cell_has_errors(dm.getOrigin_row()-1,dm.getOrigin_col());
				else  table.cell_no_errors(dm.getOrigin_row()-1,dm.getOrigin_col());
				table.revalidate();
		}
		else if(dm.getPriority()==DebugConstants.PriorityType.DEFAULTS.priorityCode) {
			if(asError) table.cell_has_defaults(dm.getOrigin_row()-1,dm.getOrigin_col());
			else  table.cell_no_defaults(dm.getOrigin_row()-1,dm.getOrigin_col());
			table.revalidate();
		}else if(dm.getPriority()==DebugConstants.PriorityType.MINOR_EMPTY.priorityCode
				||dm.getPriority()==DebugConstants.PriorityType.MINOR_IMPORT_ISSUES.priorityCode
				) {
			if(asError) table.cell_has_minorIssue(dm.getOrigin_row()-1,dm.getOrigin_col());
			else  table.cell_no_minorIssue(dm.getOrigin_row()-1,dm.getOrigin_col());
			table.revalidate();
		}
	}
	
	public void addDebugMessage_ifNotPresent(DebugMessage dm, boolean concatenateErrorMessage) {
		String key = dm.getOrigin_table()+"@"+dm.getPriority()+"_"+dm.getOrigin_row()+"_"+dm.getOrigin_col();
		
		String key_parsing = dm.getOrigin_table()+"@"+DebugConstants.PriorityType.PARSING.priorityCode+"_"+dm.getOrigin_row()+"_"+dm.getOrigin_col();
			 
			 if(currentMutant.debugMessages.get(key) == null && currentMutant.debugMessages.get(key_parsing) == null) {
				 currentMutant.debugMessages.put(key, dm);
			 } else {
				 if(currentMutant.debugMessages.get(key_parsing) == null) {
					 DebugMessage old = currentMutant.debugMessages.get(key);
					 if(!old.getProblem().contains(dm.getProblem()) && concatenateErrorMessage) {
						 old.setProblem(old.getProblem()+System.getProperty("line.separator")+dm.getProblem());
					 } 
					 currentMutant.debugMessages.put(key, old);
				 } else {
					clear_debugMessages_relatedWith(dm.getOrigin_table(), DebugConstants.PriorityType.PARSING.priorityCode, dm.getOrigin_row(), dm.getOrigin_col());
					clear_debugMessages_relatedWith(dm.getOrigin_table(), DebugConstants.PriorityType.INCONSISTENCIES.priorityCode, dm.getOrigin_row(), dm.getOrigin_col());
					clear_debugMessages_relatedWith(dm.getOrigin_table(), DebugConstants.PriorityType.MISSING.priorityCode, dm.getOrigin_row(), dm.getOrigin_col());
					currentMutant.debugMessages.put(key, dm);
				 }
			 }
		updateDebugTab();
	}
	
	public void updateDebugTab() {
		jPanelTreeDebugMessages_RM.updateDebugMessages(currentMutant.debugMessages);
		jPanelTreeDebugMessages_RM.updateTreeView();
	 
		try{
			Iterator iterator = currentMutant.debugMessages.keySet().iterator(); 
		    while (iterator.hasNext()) {  
		    	DebugMessage dm = (DebugMessage)currentMutant.debugMessages.get(iterator.next());
		    	recolorCell(dm, true);
			   }
			} catch(Exception ex) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
			}
	}

	
	
	public void setMutantAndShow(
			Mutant userObject, 
			Vector<Mutant> ancestors, 
			HashSet<String> conflicts) {
		try {
			currentMutant = userObject;
			currentAncestors = new Vector<Mutant>();
			currentAncestors.addAll(ancestors);
			this.conflicts = new HashSet<String>();
			this.conflicts.addAll(conflicts);
		
			loadReactionsTable();
			loadSpeciesTable(currentMutant);
			loadGlobalQTable(currentMutant);
			loadCompartmentTable(currentMutant);
			loadEventsTable();
			loadFunctionTable();
			
			revalidateExpressions();
			
	
			Iterator it= MainGui.debugMessages.keySet().iterator();
			while(it.hasNext()) {
				addDebugMessage_ifNotPresent( MainGui.debugMessages.get((String) it.next()), true);
			}
			
			filter.setText("");
			textFieldName.setText(userObject.getName());
			updateDebugTab();
			
			
			setTitle("Edit single mutant - "+currentMutant.getName());
			setVisible(true);
		
			//once the window is closed
			if(exitOption != ExitOption.CANCEL) {
				while(!updateName()) {}
				
				currentMutant.clearChanges();
				currentMutant.clearCumulativeChanges();
				currentMutant.updateChanges(jTableGLQ.getAllChanges());
				currentMutant.updateChanges(jTableSPC.getAllChanges());
				currentMutant.updateChanges(jTableCMP.getAllChanges());
				
				mutDB.rename(currentMutant, textFieldName.getText().trim());
			}
			
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	
	
	 protected boolean updateName() {
			
		 	String newName = textFieldName.getText().trim();
	 		  if(newName.compareTo(currentMutant.getName())==0) return true;
			  boolean success = true;
			  if(newName.length()==0) {
					JOptionPane.showMessageDialog(null, "The name cannot be empty.\n", "Error!", JOptionPane.ERROR_MESSAGE);
					success= false;
			  }
			  else if(mutDB.isNameDuplicate(newName)) {
				JOptionPane.showMessageDialog(null, "The name already exists.\nProvide a different name.", "Error!", JOptionPane.ERROR_MESSAGE);
				success= false;
			  }
			  return success;
		  }
	 
	private void revalidateExpressions() {
		HashMap<String, String> localChanges = currentMutant.getChanges();
		
		for(int row = 0; row < tableSPCmodel.getRowCount(); row++) {
			String name = tableSPCmodel.getValueAt(row, Constants.SpeciesColumns.NAME.index).toString();
			String key = Mutant.generateChangeKey(MutantChangeType.SPC_INITIAL_VALUE, name);
			try {
				if(localChanges.containsKey(key)) {
					String tableName = Constants.TitlesTabs.SPECIES.getDescription();
					int col = Constants.SpeciesColumns.INITIAL_QUANTITY.index;
					String colDescr = Constants.SpeciesColumns.INITIAL_QUANTITY.getDescription();
					String expression = tableSPCmodel.getValueAt(row, col).toString();
					Vector<Vector<String>> elements = parseExpression_withRefParents(multiModel, expression,tableName,colDescr);
					clear_debugMessages_relatedWith(tableName, DebugConstants.PriorityType.PARSING.priorityCode, row+1, col);
					clear_debugMessages_relatedWith(tableName, DebugConstants.PriorityType.INCONSISTENCIES.priorityCode, row+1, col);
					clear_debugMessages_relatedWith(tableName, DebugConstants.PriorityType.MISSING.priorityCode, row+1, col);
					currentMutant.addDebugMessage_expression(elements,currentAncestors,tableName, row, col );
				}
			} catch(Throwable e) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
				continue;
			}
		}
		
		for(int row = 0; row < tableGLQmodel.getRowCount(); row++) {
			String name = tableGLQmodel.getValueAt(row, Constants.GlobalQColumns.NAME.index).toString();
			String key = Mutant.generateChangeKey(MutantChangeType.GLQ_INITIAL_VALUE, name);
			try {
				if(localChanges.containsKey(key)) {
					String tableName = Constants.TitlesTabs.GLOBALQ.getDescription();
					int col = Constants.GlobalQColumns.VALUE.index;
					String colDescr = Constants.GlobalQColumns.VALUE.getDescription();
					String expression = tableGLQmodel.getValueAt(row, col).toString();
					Vector<Vector<String>> elements = parseExpression_withRefParents(multiModel, expression, tableName,colDescr);
					clear_debugMessages_relatedWith(tableName, DebugConstants.PriorityType.PARSING.priorityCode, row+1, col);
					clear_debugMessages_relatedWith(tableName, DebugConstants.PriorityType.INCONSISTENCIES.priorityCode, row+1, col);
					clear_debugMessages_relatedWith(tableName, DebugConstants.PriorityType.MISSING.priorityCode, row+1, col);
					currentMutant.addDebugMessage_expression(elements,currentAncestors,tableName, row, col );
					
				}
			} catch(Throwable e) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
				continue;
			}
		}
		
		for(int row = 0; row < tableCMPmodel.getRowCount(); row++) {
			String name = tableCMPmodel.getValueAt(row, Constants.CompartmentsColumns.NAME.index).toString();
			String key = Mutant.generateChangeKey(MutantChangeType.COMP_INITIAL_VALUE, name);
			try {
				if(localChanges.containsKey(key)) {
					String tableName = Constants.TitlesTabs.COMPARTMENTS.getDescription();
					int col = Constants.CompartmentsColumns.INITIAL_SIZE.index;
					String colDescr = Constants.CompartmentsColumns.INITIAL_SIZE.getDescription();
					String expression = tableGLQmodel.getValueAt(row, col).toString();
					Vector<Vector<String>> elements = parseExpression_withRefParents(multiModel, expression, tableName,colDescr);
					clear_debugMessages_relatedWith(tableName, DebugConstants.PriorityType.PARSING.priorityCode, row+1, col);
					clear_debugMessages_relatedWith(tableName, DebugConstants.PriorityType.INCONSISTENCIES.priorityCode, row+1, col);
					clear_debugMessages_relatedWith(tableName, DebugConstants.PriorityType.MISSING.priorityCode, row+1, col);
					currentMutant.addDebugMessage_expression(elements,currentAncestors,tableName, row, col );
					
				}
			} catch(Throwable e) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
				continue;
			}
		}
		
	    	   		
	    updateDebugTab();
		
	}

	private Component getJPanelGlobalQ() {
		jScrollPaneTableGlobalQ = new JScrollPane();
		jScrollPaneTableGlobalQ.setViewportView(jTableGLQ);
		return jScrollPaneTableGlobalQ;
	}
	
	private Component getJPanelSpecies() {
		jScrollPaneTableSpecies= new JScrollPane();
		jScrollPaneTableSpecies.setViewportView(jTableSPC);
		return jScrollPaneTableSpecies;
	}
	
	private Component getJPanelCompartments() {
		jScrollPaneTableCompartments= new JScrollPane();
		jScrollPaneTableCompartments.setViewportView(jTableCMP);
		return jScrollPaneTableCompartments;
	}
	
	private Component getJPanelReactions() {
		jScrollPaneTableReactions= new JScrollPane();
		jScrollPaneTableReactions.setViewportView(jTableRCT);
		return jScrollPaneTableReactions;
	}
	private Component getJPanelEvents() {
		jScrollPaneTableEvents= new JScrollPane();
		jScrollPaneTableEvents.setViewportView(jTableEV);
		return jScrollPaneTableEvents;
	}
	private Component getJPanelFunctions() {
		jScrollPaneTableFunctions= new JScrollPane();
		jScrollPaneTableFunctions.setViewportView(jTableFUN);
		return jScrollPaneTableFunctions;
	}
	
	private void loadSpeciesTable(Mutant userObject) throws Throwable {
		tableSPCmodel.clearData();
		tableSPCmodel.removeAddEmptyRow_Listener();
		jTableSPC.clearLocalRedefinition();
		jTableSPC.clearCumulativeRedefinition();
		jTableSPC.clearFromBaseSet();
		jTableSPC.resetFilter();
		Vector rows = multiModel.loadSpeciesTable_fromMultimodel(); 
	   HashMap<String, String> changes = userObject.getChanges();
	   HashMap<String, MutablePair<String, String>> all_changes = userObject.getCumulativeChanges();
	   HashSet<String> fromBaseSet = userObject.getFromBaseSet();
	   
	   
       for (int i = 0;i < rows.size();i++) {
    	   Vector r = (Vector)rows.get(i);
    	   if(userObject!= null) {
    		   String name = (String) r.get(Constants.SpeciesColumns.NAME.index-1);
    		   String key = Mutant.generateChangeKey(MutantChangeType.SPC_INITIAL_VALUE, name);
    		   if(conflicts.contains(key)) {
    			   r.set(Constants.SpeciesColumns.INITIAL_QUANTITY.index-1,RunManager.NotesLabels.CONFLICT.getLabel());
      			   r.set(Constants.SpeciesColumns.NOTES.index-1, RunManager.NotesLabels.CONFLICT.getLabel());
      			 DebugMessage dm = new DebugMessage();
				 dm.setOrigin_table(Constants.TitlesTabs.SPECIES.getDescription());
				 dm.setProblem("Conflict on element definition");
				 dm.setPriority(DebugConstants.PriorityType.INCONSISTENCIES.priorityCode);
				 dm.setOrigin_col(Constants.SpeciesColumns.INITIAL_QUANTITY.index); 
				 dm.setOrigin_row(i+1);
				currentMutant.addDebugMessage(dm); 
    		   } 
    		   else if(changes.containsKey(key)) {
    			   r.set(Constants.SpeciesColumns.INITIAL_QUANTITY.index-1, changes.get(key));
    			   jTableSPC.addLocalRedefinition(i);
    			   r.set(Constants.SpeciesColumns.NOTES.index-1, RunManager.NotesLabels.LOCAL.getLabel());
    		   } 
    		   else if(fromBaseSet.contains(key)) {
      			   r.set(Constants.SpeciesColumns.NOTES.index-1, RunManager.NotesLabels.FROM_BASESET.getLabel());
    		   } 
    		   else  if(all_changes.containsKey(key)) {
    			   MutablePair<String, String> expression_origin = all_changes.get(key);
    			   r.set(Constants.SpeciesColumns.INITIAL_QUANTITY.index-1, expression_origin.left);
    			   jTableSPC.addCumulativeRedefinition(i, expression_origin.right);
    			   r.set(Constants.SpeciesColumns.NOTES.index-1, RunManager.NotesLabels.FROM_ANCESTOR.getLabel()+expression_origin.right);
    		   } else {
    			   r.set(Constants.SpeciesColumns.NOTES.index-1, RunManager.NotesLabels.FROM_BASESET.getLabel());
    		   }
    	   
    	   tableSPCmodel.addRow(r);
         }
       }
		
       tableSPCmodel.addAddEmptyRow_Listener();
       tableSPCmodel.fireTableDataChanged();
        jTableSPC.revalidate();
	}
	
	private void loadCompartmentTable(Mutant userObject) throws Throwable {
		tableCMPmodel.clearData();
		tableCMPmodel.removeAddEmptyRow_Listener();
		jTableCMP.clearLocalRedefinition();
		jTableCMP.clearCumulativeRedefinition();
		jTableCMP.clearFromBaseSet();
		jTableCMP.resetFilter();
		Vector rows = multiModel.loadCompartmentsTable_fromMultimodel(); 
	   HashMap<String, String> changes = userObject.getChanges();
	   HashMap<String, MutablePair<String, String>> all_changes = userObject.getCumulativeChanges();
	   HashSet<String> fromBaseSet = userObject.getFromBaseSet();
	   
       for (int i = 0;i < rows.size();i++) {
    	   Vector r = (Vector)rows.get(i);
    	   if(userObject!= null) {
    		   String name = (String) r.get(Constants.CompartmentsColumns.NAME.index-1);
    		   String key = Mutant.generateChangeKey(MutantChangeType.COMP_INITIAL_VALUE, name);
    		   if(conflicts.contains(key)) {
    			   r.set(Constants.CompartmentsColumns.INITIAL_SIZE.index-1, RunManager.NotesLabels.CONFLICT.getLabel());
      			   r.set(Constants.CompartmentsColumns.NOTES.index-1, RunManager.NotesLabels.CONFLICT.getLabel());
	      			 DebugMessage dm = new DebugMessage();
					 dm.setOrigin_table(Constants.TitlesTabs.COMPARTMENTS.getDescription());
					 dm.setProblem("Conflict on element definition");
					 dm.setPriority(DebugConstants.PriorityType.INCONSISTENCIES.priorityCode);
					 dm.setOrigin_col(Constants.CompartmentsColumns.INITIAL_SIZE.index); 
					 dm.setOrigin_row(i+1);
					currentMutant.addDebugMessage(dm); 
			   } 
    		   else if(changes.containsKey(key)) {
    			   r.set(Constants.CompartmentsColumns.INITIAL_SIZE.index-1, changes.get(key));
    			   jTableCMP.addLocalRedefinition(i);
    			   r.set(Constants.CompartmentsColumns.NOTES.index-1, RunManager.NotesLabels.LOCAL.getLabel());
    		   } 
    		   else if(fromBaseSet.contains(key)) {
      			   r.set(Constants.CompartmentsColumns.NOTES.index-1, RunManager.NotesLabels.FROM_BASESET.getLabel());
    		   } 
    		   else  if(all_changes.containsKey(key)) {
    			   MutablePair<String, String> expression_origin = all_changes.get(key);
    			   r.set(Constants.CompartmentsColumns.INITIAL_SIZE.index-1, expression_origin.left);
    			   jTableCMP.addCumulativeRedefinition(i, expression_origin.right);
    			   r.set(Constants.CompartmentsColumns.NOTES.index-1, RunManager.NotesLabels.FROM_ANCESTOR.getLabel()+expression_origin.right);
    		   } else {
    			   r.set(Constants.CompartmentsColumns.NOTES.index-1, RunManager.NotesLabels.FROM_BASESET.getLabel());
    		   }
    	   
    	   tableCMPmodel.addRow(r);
         }
       }
		
       tableCMPmodel.addAddEmptyRow_Listener();
       tableCMPmodel.fireTableDataChanged();
        jTableCMP.revalidate();
	}
	
	
	private void loadReactionsTable() throws Throwable {
		tableRCTmodel.clearData();
		tableRCTmodel.removeAddEmptyRow_Listener();
		jTableRCT.clearLocalRedefinition();
		jTableRCT.clearCumulativeRedefinition();
		jTableRCT.clearFromBaseSet();
		jTableRCT.resetFilter();
		
		Vector rows = multiModel.loadReactionsTable_fromMultimodel(); 
	   	   
       for (int i = 0;i < rows.size();i++) {
    	   Vector r = (Vector)rows.get(i);
    	   tableRCTmodel.addRow(r);
       }
		
       tableRCTmodel.addAddEmptyRow_Listener();
       tableRCTmodel.fireTableDataChanged();
        jTableRCT.revalidate();
	}
	
	private void loadEventsTable() throws Throwable {
		tableEVmodel.clearData();
		tableEVmodel.removeAddEmptyRow_Listener();
		jTableEV.clearLocalRedefinition();
		jTableEV.clearCumulativeRedefinition();
		jTableEV.clearFromBaseSet();
		jTableEV.resetFilter();
		Vector rows = multiModel.loadEventsTable_fromMultimodel(); 
	   	   
       for (int i = 0;i < rows.size();i++) {
    	   Vector r = (Vector)rows.get(i);
    	   tableEVmodel.addRow(r);
       }
		
       tableEVmodel.addAddEmptyRow_Listener();
       tableEVmodel.fireTableDataChanged();
       jTableEV.revalidate();
	}
	
	
	private void loadFunctionTable() throws Throwable {
		tableFUNmodel.clearData();
		tableFUNmodel.removeAddEmptyRow_Listener();
		jTableFUN.clearLocalRedefinition();
		jTableFUN.clearCumulativeRedefinition();
		jTableFUN.clearFromBaseSet();
		jTableFUN.resetFilter();
		
		Vector rows = multiModel.loadFunctionsTable_fromMultimodel(); 
	   	   
       for (int i = 0;i < rows.size();i++) {
    	   Vector r = (Vector)rows.get(i);
    	   tableFUNmodel.addRow(r);
       }
		
       tableFUNmodel.addAddEmptyRow_Listener();
       tableFUNmodel.fireTableDataChanged();
       jTableFUN.revalidate();
	}
	
	
	private void loadGlobalQTable(Mutant userObject) throws Throwable {
		tableGLQmodel.clearData();
		tableGLQmodel.removeAddEmptyRow_Listener();
		jTableGLQ.clearLocalRedefinition();
		jTableGLQ.clearCumulativeRedefinition();
		jTableGLQ.clearFromBaseSet();
		jTableGLQ.resetFilter();
		Vector rows = multiModel.loadGlobalQTable_fromMultimodel(); 
	   HashMap<String, String> changes = userObject.getChanges();
	   HashMap<String, MutablePair<String, String>> all_changes = userObject.getCumulativeChanges();
	   HashSet<String> fromBaseSet = userObject.getFromBaseSet();
	   
	   
       for (int i = 0;i < rows.size();i++) {
    	   Vector r = (Vector)rows.get(i);
    	   if(userObject!= null) {
    		   String name = (String) r.get(Constants.GlobalQColumns.NAME.index-1);
    		   String key = Mutant.generateChangeKey(MutantChangeType.GLQ_INITIAL_VALUE, name);
    		   if(conflicts.contains(key)) {
    			   r.set(Constants.GlobalQColumns.VALUE.index-1, RunManager.NotesLabels.CONFLICT.getLabel());
      			   r.set(Constants.GlobalQColumns.NOTES.index-1, RunManager.NotesLabels.CONFLICT.getLabel());
      			 DebugMessage dm = new DebugMessage();
				 dm.setOrigin_table(Constants.TitlesTabs.GLOBALQ.getDescription());
				 dm.setProblem("Conflict on element definition");
				 dm.setPriority(DebugConstants.PriorityType.INCONSISTENCIES.priorityCode);
				 dm.setOrigin_col(Constants.GlobalQColumns.VALUE.index); 
				 dm.setOrigin_row(i+1);
				currentMutant.addDebugMessage(dm); 
    		   } 
    		   else if(changes.containsKey(key)) {
    			   r.set(Constants.GlobalQColumns.VALUE.index-1, changes.get(key));
    			   jTableGLQ.addLocalRedefinition(i);
    			   r.set(Constants.GlobalQColumns.NOTES.index-1, RunManager.NotesLabels.LOCAL.getLabel());
    		   } 
    		   else if(fromBaseSet.contains(key)) {
      			   r.set(Constants.GlobalQColumns.NOTES.index-1, RunManager.NotesLabels.FROM_BASESET.getLabel());
    		   } 
    		   else  if(all_changes.containsKey(key)) {
    			   MutablePair<String, String> expression_origin = all_changes.get(key);
    			   r.set(Constants.GlobalQColumns.VALUE.index-1, expression_origin.left);
    			   jTableGLQ.addCumulativeRedefinition(i, expression_origin.right);
    			   r.set(Constants.GlobalQColumns.NOTES.index-1, RunManager.NotesLabels.FROM_ANCESTOR.getLabel()+expression_origin.right);
    		   } else {
    			   r.set(Constants.GlobalQColumns.NOTES.index-1, RunManager.NotesLabels.FROM_BASESET.getLabel());
    		   }
    	   
    	   tableGLQmodel.addRow(r);
         }
       }
		
	    tableGLQmodel.addAddEmptyRow_Listener();
        tableGLQmodel.fireTableDataChanged();
        jTableGLQ.revalidate();
	}

	
	public void highlightElement_relatedWith(DebugMessage dm) {
		tabs.setSelectedIndex(Constants.TitlesTabs.getIndexFromDescription(dm.getOrigin_table()));
		if(dm.getOrigin_table().compareTo(Constants.TitlesTabs.SPECIES.getDescription())==0) { 
				jTableSPC.setCell_to_highlight(new MutablePair(dm.getOrigin_row(),  dm.getOrigin_col()));
				jTableSPC.scrollRectToVisible(new Rectangle(jTableSPC.getCellRect(dm.getOrigin_row()-1, 0, true)));  
		 	}
	     	else if(dm.getOrigin_table().compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0) { 
	     		jTableRCT.setCell_to_highlight(new MutablePair(dm.getOrigin_row(),  dm.getOrigin_col()));
	     		jTableRCT.scrollRectToVisible(new Rectangle(jTableRCT.getCellRect(dm.getOrigin_row()-1, 0, true)));  
 		 	}
	     	else if(dm.getOrigin_table().compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())==0) { 
	     		jTableGLQ.setCell_to_highlight(new MutablePair(dm.getOrigin_row(),  dm.getOrigin_col()));
	     		jTableGLQ.scrollRectToVisible(new Rectangle(jTableGLQ.getCellRect(dm.getOrigin_row()-1, 0, true)));  
 	     		}
	     	else if(dm.getOrigin_table().compareTo(Constants.TitlesTabs.FUNCTIONS.getDescription())==0) {
	     		jTableFUN.setCell_to_highlight(new MutablePair(dm.getOrigin_row(),  dm.getOrigin_col()));
	     		jTableFUN.scrollRectToVisible(new Rectangle(jTableFUN.getCellRect(dm.getOrigin_row()-1, 0, true)));  
 		    	}
	     	else if(dm.getOrigin_table().compareTo(Constants.TitlesTabs.EVENTS.getDescription())==0) { 
	     		jTableEV.setCell_to_highlight(new MutablePair(dm.getOrigin_row(),  dm.getOrigin_col()));
	     		jTableEV.scrollRectToVisible(new Rectangle(jTableEV.getCellRect(dm.getOrigin_row()-1, 0, true)));  
 	     		}
	     	else if(dm.getOrigin_table().compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription())==0) { 
	     		jTableCMP.setCell_to_highlight(new MutablePair(dm.getOrigin_row(),  dm.getOrigin_col()));
	     		jTableCMP.scrollRectToVisible(new Rectangle(jTableCMP.getCellRect(dm.getOrigin_row()-1, 0, true)));  
 		 	}
	
	}


	public void showLocalChangeFrame(int row, TableModel dataModel, String tableName) {
		int  columnToChange = -1;
		int noteColumn = -1;
		int nameColumn = -1;
		CustomJTable_SingleMutant whichTable = null;
		MutantChangeType mctype = null;
		String columnDescription = new String();
		if(tableName.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription()) == 0){
			columnToChange = Constants.GlobalQColumns.VALUE.index;
			columnDescription = Constants.GlobalQColumns.VALUE.getDescription();
			nameColumn = Constants.GlobalQColumns.NAME.index;
			noteColumn = Constants.GlobalQColumns.NOTES.index;
			mctype = MutantChangeType.GLQ_INITIAL_VALUE;
			whichTable = jTableGLQ;
		} else if(tableName.compareTo(Constants.TitlesTabs.SPECIES.getDescription()) == 0){
			columnToChange = Constants.SpeciesColumns.INITIAL_QUANTITY.index;
			columnDescription = Constants.SpeciesColumns.INITIAL_QUANTITY.getDescription();
				nameColumn = Constants.SpeciesColumns.NAME.index;
			noteColumn = Constants.SpeciesColumns.NOTES.index;
			mctype = MutantChangeType.SPC_INITIAL_VALUE;
			whichTable = jTableSPC;
		} else if(tableName.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription()) == 0){
			columnToChange = Constants.CompartmentsColumns.INITIAL_SIZE.index;
			columnDescription = Constants.CompartmentsColumns.INITIAL_SIZE.getDescription();
			nameColumn = Constants.CompartmentsColumns.NAME.index;
			noteColumn = Constants.CompartmentsColumns.NOTES.index;
			mctype = MutantChangeType.COMP_INITIAL_VALUE;
			whichTable = jTableCMP;
		} else {
			JOptionPane.showMessageDialog(null, "Only initial values of Species, Global quantities and Compartments can be changed.\nThis table is shown for reference purposes only.", "Error!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		
		String element = dataModel.getValueAt(row, nameColumn).toString();
		 localChangeFrame = new LocalChangeFrame(this);
		 localChangeFrame.setTitle("Changing: "+element);
			
          MutablePair<Integer, String> ret = 
        		  localChangeFrame.initializeAndShow(
        				  				getMutantAncestors(element,tableName), 
        				  				dataModel.getValueAt(row, columnToChange).toString(),
        				  				dataModel.getValueAt(row, noteColumn).toString());
      
          if(ret==null || ret.left == 0) return;

	
         if(ret.left == RunManager.NotesLabels.FROM_ANCESTOR.getOption()) {
	         String returned = ret.right;
             int indexSepStart = returned.indexOf("(");
             int indexSepEnd = returned.lastIndexOf(")");
             String value = returned.substring(indexSepStart+1, indexSepEnd).trim();
             String parent = returned.substring(0, indexSepStart).trim();
             dataModel.setValueAt(value, row, columnToChange);
             whichTable.addCumulativeRedefinition(row, ret.right);
        	  dataModel.setValueAt(RunManager.NotesLabels.FROM_ANCESTOR.getLabel()+parent, row, noteColumn);
       } else if(ret.left == RunManager.NotesLabels.LOCAL.getOption()){
    	   String newExpression = ret.right;
    	   	try {
    	   		Vector<Vector<String>> elements = parseExpression_withRefParents(multiModel, newExpression, tableName,columnDescription);
    	   		boolean errorsFound = currentMutant.addDebugMessage_expression(elements,currentAncestors,tableName, row, columnToChange );
    	   		if(!errorsFound) {
    	   			clear_debugMessages_relatedWith(tableName, DebugConstants.PriorityType.PARSING.priorityCode, row+1, columnToChange);
    	   			clear_debugMessages_relatedWith(tableName, DebugConstants.PriorityType.INCONSISTENCIES.priorityCode, row+1, columnToChange);
    	   			clear_debugMessages_relatedWith(tableName, DebugConstants.PriorityType.MISSING.priorityCode, row+1, columnToChange);
        	   	}
    	   		updateDebugTab();
    	   	} catch (Throwable ex) {
	   			ex.printStackTrace();
	   		}
    	   	currentMutant.addChange(mctype, element, newExpression);
   		
    	   dataModel.setValueAt(newExpression, row, columnToChange);
			whichTable.addLocalRedefinition(row);
			dataModel.setValueAt(RunManager.NotesLabels.LOCAL.getLabel(), row, noteColumn);
		} else if(ret.left == RunManager.NotesLabels.FROM_BASESET.getOption()){
			String valueFromBaseSet = new String();
			if(tableName.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription()) == 0){
				valueFromBaseSet = multiModel.getGlobalQ(element).getInitialValue();
			}else 	if(tableName.compareTo(Constants.TitlesTabs.SPECIES.getDescription()) == 0){
				valueFromBaseSet = multiModel.getSpecies(element).getInitialQuantity_listString();
			}else 	if(tableName.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription()) == 0){
				valueFromBaseSet = multiModel.getComp(element).getInitialVolume();
			}
			dataModel.setValueAt(valueFromBaseSet, row, columnToChange);
			whichTable.removeLocalCumulativeRedefinition(row);
			dataModel.setValueAt(RunManager.NotesLabels.FROM_BASESET.getLabel(), row, noteColumn);
		}
          
	}

	
	private Vector<FoundElement> searchRM(String s) {
		
		//to avoid the rename of global quantities that has the same name of a species (when the species is renamed) 
		//I need to exclude the main column of global quantities table from the search
		
		if(s.trim().length() ==0) return new Vector<FoundElement>();
		Vector<Vector> tablesAndColumns = new Vector<Vector>();
		
		Vector element = null;
		
		element = new Vector();
		element.add(tableSPCmodel);
		element.add(Constants.TitlesTabs.SPECIES.getDescription());
		element.add(Constants.SpeciesColumns.EXPRESSION.index);
		tablesAndColumns.add(element);
		
		element = new Vector();
		element.add(tableGLQmodel);
		element.add(Constants.TitlesTabs.GLOBALQ.getDescription());
		element.add(Constants.GlobalQColumns.EXPRESSION.index);
		tablesAndColumns.add(element);
		
		element = new Vector();
		element.add(tableCMPmodel);
		element.add(Constants.TitlesTabs.COMPARTMENTS.getDescription());
		element.add(Constants.CompartmentsColumns.EXPRESSION.index);
		tablesAndColumns.add(element);
	
		
		Vector<FoundElement> found = new Vector<FoundElement>();
		for(Vector el : tablesAndColumns) {
			if(el.get(0) instanceof CustomTableModel) {
				CustomTableModel tModel = (CustomTableModel) el.get(0);
				String descr = (String) el.get(1);
				for(int row = 0; row < (tModel).getRowCount(); row++){
					for(int i = 2;  i < el.size(); i++)
					{
						int col = (int) Integer.parseInt(el.get(i).toString());
						String next = (tModel.getValueAt(row, col).toString().trim());
						if(next.trim().length() ==0) continue;
						try {
							Vector<String> names = new Vector<String>();
							InputStream is = new ByteArrayInputStream(next.getBytes("UTF-8"));
								MR_Expression_Parser_ReducedParserException parser = new MR_Expression_Parser_ReducedParserException(is,"UTF-8");
								ExtractNamesUsedVisitor v = new ExtractNamesUsedVisitor(multiModel);
								CompleteExpression root = parser.CompleteExpression();
								root.accept(v);
								names.addAll(v.getNamesUsed());
					
									for(String n : names) {
										if(n.compareTo(s)==0)
										{
											found.add(new FoundElement(descr, row, col));
											break;
										} 
										
									}
							} catch (Throwable e) {
								continue;
							}
						}
					}
				}
			}
    	return found;
    }
	

	
	private Vector<Vector<String>> parseExpression_withRefParents(MultiModel m, String expression, String table_descr, String column_descr) throws Throwable {
		 Vector ret = new Vector();
			
		  InputStream is = new ByteArrayInputStream(expression.getBytes("UTF-8"));
		  MR_Expression_Parser_ReducedParserException parser = new MR_Expression_Parser_ReducedParserException(is,"UTF-8");
		  CompleteExpression root = parser.CompleteExpression();
		  Look4UndefinedMisusedVisitor undefVisitor = new Look4UndefinedMisusedVisitor(m);
		  root.accept(undefVisitor);
		  Vector<String> undef = undefVisitor.getUndefinedElements();
		  Vector<String> misused = undefVisitor.getMisusedElements();
		  Vector<String> parents = undefVisitor.getParents();
			  
		  ret.add(undef);
		  ret.add(misused);
		  ret.add(parents);
		  		  
		  return ret;
	}

	public Vector<String> getMutantAncestors(String elementToChange, String tableName) {
		Vector<String> ret = new Vector<String>();
		String valueFromBaseSet = null;
		MutantChangeType mchangetype = null;
		if(tableName.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription()) == 0){
			valueFromBaseSet = multiModel.getGlobalQ(elementToChange).getInitialValue();
			mchangetype  = MutantChangeType.GLQ_INITIAL_VALUE;
		}else if(tableName.compareTo(Constants.TitlesTabs.SPECIES.getDescription()) == 0){
			valueFromBaseSet = multiModel.getSpecies(elementToChange).getInitialQuantity_listString();
			mchangetype = MutantChangeType.SPC_INITIAL_VALUE;
		}else if(tableName.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription()) == 0){
			valueFromBaseSet = multiModel.getComp(elementToChange).getInitialVolume();
			mchangetype = MutantChangeType.COMP_INITIAL_VALUE;
		}
		
		for (Mutant p : currentAncestors) {
			String displayValue= new String();
			MutablePair<String, String> valInPar= p.getCumulativeChanges().get(p.generateChangeKey(mchangetype, elementToChange));
			if(valInPar==null) {
				String localInPar = p.getChanges().get(p.generateChangeKey(mchangetype, elementToChange));
				if(localInPar == null) {
					displayValue = valueFromBaseSet;
				} else {
					displayValue = localInPar;
				}
			} else {
				displayValue = valInPar.left;
			}
			ret.add(p.getName()+" ("+displayValue+")");
		}
		return ret;
}

}
