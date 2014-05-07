package msmb.runManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JList;
import java.awt.GridLayout;
import javax.swing.JSeparator;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;

import org.apache.commons.lang3.tuple.MutablePair;
import msmb.model.MultiModel;
import msmb.parsers.mathExpression.MR_Expression_Parser_ReducedParserException;
import msmb.parsers.mathExpression.syntaxtree.CompleteExpression;
import msmb.parsers.mathExpression.visitor.Look4UndefinedMisusedVisitor;
import msmb.utility.GraphicalProperties;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SingleSimulationFrame extends JDialog {
	private ExitOption exitOption = ExitOption.CANCEL;
	private JPanel contentPane;
	private JTextField jTextFieldName;
	private JTextField jTextFieldDuration;
	private JTextField jTextFieldIntervalSize;
	private JTextField jTextFieldIntervals;
	private JTable table;
	private Simulation currentSimulation;
	private Vector<Simulation> currentAncestors;
	private HashSet<String> conflicts;
	private DefaultListModel listModel_mutants;
	private SimulationsDB simDB;
	private MultiModel multiModel;
	private JLabel jTextFieldDuration_source;
	private JTextField jTextFieldIntervalSize_source;
	private JTextField jTextFieldIntervals_source;
	private LocalChangeFrame localChangeFrame;
	private SingleSimulationAddParameterListFrame addParListFrame;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SingleSimulationFrame frame = new SingleSimulationFrame(null,null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public SingleSimulationFrame(SimulationsDB simulationsDB, final MultiModel multiModel) {
		this.simDB = simulationsDB;
		this.multiModel = multiModel;
		setTitle("Time course settings");
		setModal(true);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(6, 6));
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(6, 6));
		
		JLabel lblName = new JLabel("Name:");
		panel.add(lblName, BorderLayout.WEST);
		
		jTextFieldName = new JTextField();

		
		panel.add(jTextFieldName, BorderLayout.CENTER);
		jTextFieldName.setColumns(10);
		jTextFieldName.setEditable(false);
		JButton btnRename = new JButton("Rename");
		btnRename.setEnabled(false);
		btnRename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateName();
			}
		});
		panel.add(btnRename, BorderLayout.EAST);
		
		JSplitPane splitPane = new JSplitPane();
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		JPanel panel_1 = new JPanel();
		splitPane.setLeftComponent(panel_1);
		panel_1.setLayout(new BorderLayout(3, 3));
		
		JLabel lblApplyTo = new JLabel("Apply to:");
		panel_1.add(lblApplyTo, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane);
		
		listModel_mutants= new DefaultListModel();
		final JList jListMutants = new JList(listModel_mutants);
		scrollPane.setViewportView(jListMutants);
		jListMutants.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {if (e.isPopupTrigger()) showPopup(e);}
			
			@Override
			public void mousePressed(MouseEvent e) {if (e.isPopupTrigger()) showPopup(e);}
			
			@Override
			public void mouseExited(MouseEvent e) {}
			
			@Override
			public void mouseEntered(MouseEvent e) {	}
			
			@Override
			public void mouseClicked(MouseEvent e) {if (e.isPopupTrigger()) showPopup(e);}
			
			public void showPopup(final MouseEvent e) {
						JPopupMenu menu = new JPopupMenu();
						menu.add(new AbstractAction("Remove") {
							public void actionPerformed(ActionEvent e) {
								Object[] sel = jListMutants.getSelectedValues();
								for(int i =0; i< sel.length; ++i) {
									listModel_mutants.removeElement(sel[i]);
									currentSimulation.removeMutantParameter((Mutant) sel[i]);
								}
							}
						});
						menu.show((Component) e.getSource(), e.getX(), e.getY());
					}
		});
		
		
		 addParListFrame = new SingleSimulationAddParameterListFrame();
		JButton btnAddParametersLists = new JButton("Change Parameters lists");
		btnAddParametersLists.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Vector<Mutant> newList = addParListFrame.setListAndShow(currentSimulation.getName(), currentSimulation.getMutantsParameters());
				if(newList!= null) {
					currentSimulation.clearMutantParameters();
					currentSimulation.addAllMutantParameter(newList);
					sortJListMutants();
				}
			}
		});
		panel_1.add(btnAddParametersLists, BorderLayout.SOUTH);
		
		JPanel panel_2 = new JPanel();
		splitPane.setRightComponent(panel_2);
		panel_2.setLayout(new BorderLayout(0, 3));
		
		JPanel panel_3 = new JPanel();
		panel_2.add(panel_3, BorderLayout.NORTH);
		panel_3.setLayout(new BorderLayout(3, 3));
		
		JPanel panel_4 = new JPanel();
		panel_3.add(panel_4);
		panel_4.setLayout(new GridLayout(0, 3, 2, 0));
		
		JLabel lblDuration = new JLabel("Duration");
		panel_4.add(lblDuration);
		
		jTextFieldDuration = new JTextField();
		jTextFieldDuration.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				            if (evt.getClickCount() == 2) {
			                    showLocalChangeFrame((JTextField) evt.getSource());
			                }
			            
			}
		});
		jTextFieldDuration.setEditable(false);
		panel_4.add(jTextFieldDuration);
		jTextFieldDuration.setColumns(10);
		
		jTextFieldDuration_source = new JLabel();
		jTextFieldDuration_source.setOpaque(true);
		panel_4.add(jTextFieldDuration_source);
		
		JLabel lblIntervalSize = new JLabel("Interval Size");
		panel_4.add(lblIntervalSize);
		
		jTextFieldIntervalSize = new JTextField();
		panel_4.add(jTextFieldIntervalSize);
		jTextFieldIntervalSize.setColumns(10);
		
		jTextFieldIntervalSize_source = new JTextField();
		jTextFieldIntervalSize_source.setEditable(false);
		panel_4.add(jTextFieldIntervalSize_source);
		jTextFieldIntervalSize_source.setColumns(10);
		
		JLabel lblIntervals = new JLabel("Intervals");
		panel_4.add(lblIntervals);
		
		jTextFieldIntervals = new JTextField();
		panel_4.add(jTextFieldIntervals);
		jTextFieldIntervals.setColumns(10);
		
		jTextFieldIntervals_source = new JTextField();
		jTextFieldIntervals_source.setEditable(false);
		panel_4.add(jTextFieldIntervals_source);
		jTextFieldIntervals_source.setColumns(10);
		
		
			
		
		JLabel lblNewLabel = new JLabel("Time Course");
		panel_3.add(lblNewLabel, BorderLayout.NORTH);
		
		JPanel panel_5 = new JPanel();
		panel_2.add(panel_5, BorderLayout.CENTER);
		panel_5.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_6 = new JPanel();
		panel_5.add(panel_6, BorderLayout.NORTH);
		panel_6.setLayout(new BorderLayout(0, 0));
		
		JSeparator separator = new JSeparator();
		panel_6.add(separator, BorderLayout.NORTH);
		
		JLabel lblMethod = new JLabel("Method");
		panel_6.add(lblMethod, BorderLayout.WEST);
		
		JComboBox comboBox = new JComboBox();
		panel_6.add(comboBox, BorderLayout.CENTER);
		
		JPanel panel_7 = new JPanel();
		panel_5.add(panel_7, BorderLayout.CENTER);
		panel_7.setLayout(new BorderLayout(0, 3));
		
		JLabel lblParameters = new JLabel("Method's internal parameters");
		panel_7.add(lblParameters, BorderLayout.NORTH);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		panel_7.add(scrollPane_1, BorderLayout.CENTER);
		
		table = new JTable();
		table.setModel(new DefaultTableModel(
			new Object[][] {
				{"Name parameter", null},
			},
			new String[] {
				"New column", "New column"
			}
		));
		scrollPane_1.setViewportView(table);
		
		JPanel panel_8 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_8.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		contentPane.add(panel_8, BorderLayout.SOUTH);
		
		JButton btnSaveConfiguration = new JButton("Save Configuration & Close");
		btnSaveConfiguration.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(validateValues()) {
					exitOption = ExitOption.OK;
					dispose();
				}
			}
		});
		panel_8.add(btnSaveConfiguration);
	}

		protected boolean validateValues() {
			return updateName();
		}
	
	 protected boolean updateName() {
			//JOptionPane.showMessageDialog(null, "The name cannot be empty.\n", "Error!", JOptionPane.ERROR_MESSAGE);
			return true;
			
		 	/*String newName = jTextFieldName.getText().trim();
	 		  if(newName.compareTo(currentSimulation.getName())==0) return true;
			  boolean success = true;
			  if(newName.length()==0) {
					JOptionPane.showMessageDialog(null, "The name cannot be empty.\n", "Error!", JOptionPane.ERROR_MESSAGE);
					success= false;
			  }
			  else if(simDB.isNameDuplicate(newName)) {
				JOptionPane.showMessageDialog(null, "The name already exists.\nProvide a different name.", "Error!", JOptionPane.ERROR_MESSAGE);
				success= false;
			  }
			  simDB.rename(currentSimulation, newName);
			  jTextFieldName.setText(currentSimulation.getName());
		
			  return success;*/
		  }

	 
	 public void showLocalChangeFrame(JTextField whichTextField) {
			SimulationChangeType simType = null;
			JLabel whichTextField_source = null;
			if(whichTextField == jTextFieldDuration){
				whichTextField_source = jTextFieldDuration_source;
				simType = SimulationChangeType.TOTAL_TIME;
			} else {
				JOptionPane.showMessageDialog(null, "You cannot change this field", "Error!", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			localChangeFrame = new LocalChangeFrame(this);
			localChangeFrame.setTitle("Changing: "+simType.getGuiLabel());
				
	        MutablePair<Integer, String> ret = 
	        		  localChangeFrame.initializeAndShow(
	        				  				getMutantAncestors(simType), 
	        				  				whichTextField.getText(),
	        				  				whichTextField_source.getText());
	      
	          if(ret==null || ret.left == 0) return;
	          String value = null;
	          String parent = null;
	        if(ret.left == RunManager.NotesLabels.FROM_ANCESTOR.getOption()) {
		         String returned = ret.right;
	             int indexSepStart = returned.indexOf("(");
	             int indexSepEnd = returned.lastIndexOf(")");
	             value = returned.substring(indexSepStart+1, indexSepEnd).trim();
	             parent = returned.substring(0, indexSepStart).trim();
	           
	        } else if(ret.left == RunManager.NotesLabels.LOCAL.getOption()){
	        	value = ret.right;
		   	} else if(ret.left == RunManager.NotesLabels.FROM_BASESET.getOption()){
		   		value = SimulationsDB.DEFAULT_DURATION;
			}
	        
	        resetField(simType, whichTextField, whichTextField_source, value, ret.left, parent);
	  }

	 
	 public Vector<String> getMutantAncestors(SimulationChangeType simchangetype) {
			Vector<String> ret = new Vector<String>();
			String valueFromBaseSet = null;
			if(simchangetype.equals(SimulationChangeType.TOTAL_TIME)){
				valueFromBaseSet = new String("145"); //take this from the model
			}
			
			for (Simulation p : currentAncestors) {
				String displayValue= new String();
				MutablePair<String, String> valInPar= p.getCumulativeChanges().get(p.generateChangeKey(simchangetype, ""));
				if(valInPar==null) {
					String localInPar = p.getChanges().get(p.generateChangeKey(simchangetype, ""));
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
	 
	public void setSimulationAndShow(
			Simulation userObject, 
			Vector<Simulation> ancestors, 
			HashSet<String> conflicts) {
		try {
			currentSimulation = userObject;
			currentAncestors = new Vector<Simulation>();
			currentAncestors.addAll(ancestors);
			this.conflicts = new HashSet<String>();
			this.conflicts.addAll(conflicts);
		
			
			setTitle("Edit single simulation settings - "+currentSimulation.getName());
			
			
			jTextFieldName.setText(currentSimulation.getName());
			
			jTextFieldIntervals.setText(currentSimulation.getIntervals());
			jTextFieldIntervalSize.setText(currentSimulation.getIntervalSize());
			
			resetField(SimulationChangeType.TOTAL_TIME, jTextFieldDuration, jTextFieldDuration_source);
			
			sortJListMutants();
			
			
			GraphicalProperties.resetFonts(this);
			setLocationRelativeTo(null);
			
			setVisible(true);
		
			//once the window is closed
			if(exitOption != ExitOption.CANCEL) {
				currentSimulation.clearChanges();
				currentSimulation.clearCumulativeChanges();
			
				String source = jTextFieldDuration_source.getText();
				 String value = jTextFieldDuration.getText();
				 if(source.startsWith(RunManager.NotesLabels.FROM_ANCESTOR.getLabel())) {
				         int indexSepStart = source.indexOf(RunManager.NotesLabels.FROM_ANCESTOR.getLabel());
			             String parent = source.substring(indexSepStart+1).trim();
			             currentSimulation.addCumulativeChange(
			            		 Simulation.generateChangeKey(SimulationChangeType.TOTAL_TIME, ""), 
			            		 new MutablePair<String, String>(parent, value));
			             currentSimulation.removeLocalChange(SimulationChangeType.TOTAL_TIME);
			             currentSimulation.removeFromBaseSet(SimulationChangeType.TOTAL_TIME);
			        } else if(source.equals(RunManager.NotesLabels.LOCAL.getLabel())){
			    		 currentSimulation.setDuration(value,false);
				   	} else if(source.equals(RunManager.NotesLabels.FROM_BASESET.getLabel())){
				   		 currentSimulation.setDuration(SimulationsDB.DEFAULT_DURATION, true);
				   	}
			}
			
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	 private void resetField(SimulationChangeType simtype, JTextField field, JLabel source) {
		 String expression = null;
		 if(simtype.equals(SimulationChangeType.TOTAL_TIME)) {
			 expression = currentSimulation.getDuration();
		 }
		 Double evaluated = simDB.RM_buildCopasiExpression(
				expression,currentSimulation, simtype);
  	  
		if(currentSimulation.hasChange(simtype, "")) {
			source.setText(RunManager.NotesLabels.LOCAL.getLabel());
			field.setBackground(RunManager.colorLocalRedefinition);
			field.setText(currentSimulation.getDuration());
	   		 if(evaluated != null) {
	   		 	field.setBackground(Color.white);
	   		 	source.setBackground(RunManager.colorLocalRedefinition);
  			 } else {
  				field.setBackground(GraphicalProperties.color_cell_with_errors);
  			 }
 	   	  } else {
 	   		 MutablePair<String, String> parent = currentSimulation.cumulativeChanges.get(Simulation.generateChangeKey(simtype, ""));
 	   		 if(parent != null) {
 	   			source.setText(RunManager.NotesLabels.FROM_ANCESTOR.getLabel()+parent.right);
 	   			source.setBackground(RunManager.colorCumulativeRedefinition);
 	   			field.setText(parent.left);
	  	   } else {
		  		 source.setText(RunManager.NotesLabels.FROM_BASESET.getLabel());
		  		source.setBackground(RunManager.colorBaseSet);
		  		field.setText(SimulationsDB.DEFAULT_DURATION);
		  		field.setBackground(Color.white);
 	   		 }
 	   	  }
	}
	 
	 private void resetField(SimulationChangeType simtype, JTextField field, JLabel source, String expressionString, int sourceCode, String parent) {
		
		 Double evaluated = simDB.RM_buildCopasiExpression(
				 expressionString,currentSimulation, simtype);
  	  
		if(sourceCode == RunManager.NotesLabels.LOCAL.getOption()) {
			source.setText(RunManager.NotesLabels.LOCAL.getLabel());
			field.setBackground(RunManager.colorLocalRedefinition);
			field.setText(expressionString);
	   		 if(evaluated != null) {
	   		 	field.setBackground(Color.white);
	   		 	source.setBackground(RunManager.colorLocalRedefinition);
  			 } else {
  				field.setBackground(GraphicalProperties.color_cell_with_errors);
  			 }
 	   	  } else if(sourceCode == RunManager.NotesLabels.FROM_ANCESTOR.getOption()) {
 	   		 	source.setText(RunManager.NotesLabels.FROM_ANCESTOR.getLabel()+parent);
 	   			source.setBackground(RunManager.colorCumulativeRedefinition);
 	   			field.setText(expressionString);
	  	   } else if(sourceCode == RunManager.NotesLabels.FROM_BASESET.getOption()) {
	 	  		 source.setText(RunManager.NotesLabels.FROM_BASESET.getLabel());
		  		source.setBackground(RunManager.colorBaseSet);
		  		field.setText(expressionString);
		  		field.setBackground(Color.white);
 	   		 }
 	   	  
	}


	private void sortJListMutants() {
			listModel_mutants.clear();
			Vector<Object> nodes = currentSimulation.getMutantsParameters();
			ArrayList<Mutant> toBeSortedNodes = new ArrayList<Mutant>();
			Iterator it = nodes.iterator();
			while(it.hasNext()) {
				toBeSortedNodes.add((Mutant) it.next());
			}
			Collections.sort(toBeSortedNodes);
			for (Mutant element : toBeSortedNodes) {
				listModel_mutants.addElement(element);
			}
	}

	 
	 private Vector<Vector<String>> parseExpression_withRefParents(MultiModel m, String expression) throws Throwable {
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
	 
	
}
