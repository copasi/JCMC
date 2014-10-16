package msmb.runManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Rectangle;

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

import org.COPASI.CCopasiMethod;
import org.apache.commons.lang3.tuple.MutablePair;
import msmb.model.MultiModel;
import msmb.parsers.mathExpression.MR_Expression_Parser_ReducedParserException;
import msmb.parsers.mathExpression.syntaxtree.CompleteExpression;
import msmb.parsers.mathExpression.visitor.Look4UndefinedMisusedVisitor;
import msmb.utility.Constants;
import msmb.utility.GraphicalProperties;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import javax.swing.JTextPane;

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
	private JLabel jTextFieldIntervalSize_source;
	private JLabel jTextFieldIntervals_source;
	private JLabel jTextFieldMethod_source;
	private LocalChangeFrame localChangeFrame;
	private SingleSimulationAddParameterListFrame addParListFrame;
	private JTextField jTextFieldMethod;
	private DefaultTableModel tableModel;
	private MutantsDB mutDB;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SingleSimulationFrame frame = new SingleSimulationFrame(null,null,null);
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
	public SingleSimulationFrame(SimulationsDB simulationsDB, MultiModel mModel, MutantsDB mDB) {
		this.simDB = simulationsDB;
		this.multiModel = mModel;
		this.mutDB = mDB;
		setTitle("Time course settings");
		setModal(true);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

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
		
		
		 addParListFrame = new SingleSimulationAddParameterListFrame(simDB,mutDB);
		JButton btnAddParametersLists = new JButton("Change Parameters lists");
		btnAddParametersLists.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				  Rectangle screen = getGraphicsConfiguration().getBounds();
				  GraphicalProperties.resetFonts(addParListFrame);
				  addParListFrame.pack();
				  addParListFrame.setLocation(
				        screen.x + (screen.width - addParListFrame.getWidth()) / 2,
				        screen.y + (screen.height - addParListFrame.getHeight()) / 2 ); 
				     
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
		jTextFieldIntervalSize.setEditable(false);
		panel_4.add(jTextFieldIntervalSize);
		jTextFieldIntervalSize.setColumns(10);
		jTextFieldIntervalSize.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				            if (evt.getClickCount() == 2) {
			                    showLocalChangeFrame((JTextField) evt.getSource());
			                }
			            
			}
		});
		
		jTextFieldIntervalSize_source = new JLabel();
		jTextFieldIntervalSize_source.setOpaque(true);
		panel_4.add(jTextFieldIntervalSize_source);
		
		JLabel lblIntervals = new JLabel("Number of Intervals");
		panel_4.add(lblIntervals);
		
		jTextFieldIntervals = new JTextField();
		jTextFieldIntervals.setEditable(false);
		panel_4.add(jTextFieldIntervals);
		jTextFieldIntervals.setColumns(10);
		jTextFieldIntervals.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				            if (evt.getClickCount() == 2) {
			                    showLocalChangeFrame((JTextField) evt.getSource());
			                }
			            
			}
		});
		
		jTextFieldIntervals_source = new JLabel();
		jTextFieldIntervals_source.setOpaque(true);
		panel_4.add(jTextFieldIntervals_source);
		
		
			
		
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
		
		JPanel panel_9 = new JPanel();
		panel_6.add(panel_9, BorderLayout.CENTER);
		panel_9.setLayout(new GridLayout(0, 2, 0, 0));
		
		jTextFieldMethod = new JTextField();
		jTextFieldMethod.setEditable(false);
		panel_9.add(jTextFieldMethod);
		jTextFieldMethod.setColumns(10);
		jTextFieldMethod.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				            if (evt.getClickCount() == 2) {
			                    showLocalChangeFrame((JTextField) evt.getSource());
			                }
			            
			}
		});
		
		jTextFieldMethod_source = new JLabel("");
		jTextFieldMethod_source.setOpaque(true);
		panel_9.add(jTextFieldMethod_source);
		
		JPanel panel_7 = new JPanel();
		panel_5.add(panel_7, BorderLayout.CENTER);
		panel_7.setLayout(new BorderLayout(0, 3));
		
		JLabel lblParameters = new JLabel("Method's internal parameters");
		panel_7.add(lblParameters, BorderLayout.NORTH);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		panel_7.add(scrollPane_1, BorderLayout.CENTER);
		
		table = new JTable();
		table.setFont(GraphicalProperties.customFont);
		
		tableModel = new DefaultTableModel();
	
			
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
			
		 	String newName = jTextFieldName.getText().trim();
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
			  return success;
		  }

	 
	 public void showLocalChangeFrame(JTextField whichTextField) {
			SimulationChangeType simType = null;
			JLabel whichTextField_source = null;
			Boolean userSetIntervalNumber = null;
			String defaultValue = null;
			if(whichTextField == jTextFieldDuration){
				userSetIntervalNumber = true;
				whichTextField_source = jTextFieldDuration_source;
				simType = SimulationChangeType.TOTAL_TIME;
				defaultValue = SimulationsDB.DEFAULT_DURATION;
			} else	if(whichTextField == jTextFieldIntervals){
				whichTextField_source = jTextFieldIntervals_source;
				simType = SimulationChangeType.INTERVAL_NUMBER;
				userSetIntervalNumber = true;
				defaultValue = SimulationsDB.DEFAULT_STEPS;
			} else	if(whichTextField == jTextFieldIntervalSize){
				whichTextField_source = jTextFieldIntervalSize_source;
				simType = SimulationChangeType.INTERVAL_SIZE;
				userSetIntervalNumber = false;
				defaultValue = new Double(Double.parseDouble(SimulationsDB.DEFAULT_DURATION)/Double.parseDouble(SimulationsDB.DEFAULT_STEPS)).toString();
			} else	if(whichTextField == jTextFieldMethod){
				whichTextField_source = jTextFieldMethod_source;
				simType = SimulationChangeType.METHOD;
				userSetIntervalNumber = null;
				defaultValue = SimulationsDB.DEFAULT_METHOD;
			} else {
				JOptionPane.showMessageDialog(null, "You cannot change this field", "Error!", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			localChangeFrame = new LocalChangeFrame(this);
			localChangeFrame.setTitle("Changing: "+simType.getGuiLabel());
				
	        MutablePair<Integer, String> ret = null;
	        
	        if(simType == SimulationChangeType.METHOD) {
	        	Vector<String> methods = new Vector<String>();
	        	methods.add(Simulation.CopasiMethods.DETERMINISTIC.getDescription());
	        	methods.add(Simulation.CopasiMethods.GIBSONBRUCK.getDescription());
	        	methods.add(Simulation.CopasiMethods.GILLESPIE.getDescription());
	        	methods.add(Simulation.CopasiMethods.TAULEAP.getDescription());
	        	methods.add(Simulation.CopasiMethods.ADAPTIVETAU.getDescription());
	        	
	        	ret = localChangeFrame.initializeAndShowWithComboBox(
		  				getMutantAncestors(simType), 
		  				whichTextField.getText(),
		  				whichTextField_source.getText(),
		  				methods);
	        }
	        else{		 
	        	ret = localChangeFrame.initializeAndShow(
	        				  				getMutantAncestors(simType), 
	        				  				whichTextField.getText(),
	        				  				whichTextField_source.getText());
	        }
	      
	          if(ret==null || ret.left == 0) return;
	          String value = null;
	          String parent = null;
	        if(ret.left == RunManager.NotesLabels.FROM_ANCESTOR.getOption()) {
		         String returned = ret.right;
	             int indexSepStart = returned.indexOf("(");
	             int indexSepEnd = returned.lastIndexOf(")");
	             value = returned.substring(indexSepStart+1, indexSepEnd).trim();
	             parent = returned.substring(0, indexSepStart).trim();
	             conflicts.remove(Simulation.generateChangeKey(simType, ""));
	        } else if(ret.left == RunManager.NotesLabels.LOCAL.getOption()){
	        	value = ret.right;
	        	conflicts.remove(Simulation.generateChangeKey(simType, ""));
	       	} else if(ret.left == RunManager.NotesLabels.FROM_BASESET.getOption()){
		   		value = defaultValue;
		   		conflicts.remove(Simulation.generateChangeKey(simType, ""));
			}
	        
	        resetField(simType, whichTextField, whichTextField_source, value, ret.left, parent);
	        
	        if(userSetIntervalNumber!= null && userSetIntervalNumber == true) {
	        	Double val1 = simDB.RM_buildCopasiExpression(jTextFieldDuration.getText(), currentSimulation, SimulationChangeType.TOTAL_TIME);
	        	Double val2 = simDB.RM_buildCopasiExpression(jTextFieldIntervals.getText(), currentSimulation, SimulationChangeType.INTERVAL_NUMBER);
	        	value =  new Double(val1/val2).  toString();
	        	conflicts.remove(Simulation.generateChangeKey(SimulationChangeType.INTERVAL_SIZE, ""));
	        	resetField(SimulationChangeType.INTERVAL_SIZE, jTextFieldIntervalSize, jTextFieldIntervalSize_source, value, ret.left, parent);
	        	 
	        } else  if(userSetIntervalNumber!= null && userSetIntervalNumber == false) {
	        	Double val1 = simDB.RM_buildCopasiExpression(jTextFieldDuration.getText(), currentSimulation, SimulationChangeType.TOTAL_TIME);
	        	Double val2 = simDB.RM_buildCopasiExpression(jTextFieldIntervalSize.getText(), currentSimulation, SimulationChangeType.INTERVAL_SIZE);
	        	value =  new Double(val1/val2).toString();
	        	conflicts.remove(Simulation.generateChangeKey(SimulationChangeType.INTERVAL_NUMBER, ""));
		        	resetField(SimulationChangeType.INTERVAL_NUMBER, jTextFieldIntervals, jTextFieldIntervals_source, value, ret.left, parent);
	        } 
	  }

	 
	 public Vector<String> getMutantAncestors(SimulationChangeType simchangetype) {
			Vector<String> ret = new Vector<String>();
			String valueFromBaseSet = simDB.getDefaultValue(simchangetype);
			
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
			
			resetField(SimulationChangeType.TOTAL_TIME, jTextFieldDuration, jTextFieldDuration_source);
			resetField(SimulationChangeType.INTERVAL_NUMBER, jTextFieldIntervals, jTextFieldIntervals_source);
			resetField(SimulationChangeType.INTERVAL_SIZE, jTextFieldIntervalSize, jTextFieldIntervalSize_source);
			resetField(SimulationChangeType.METHOD, jTextFieldMethod, jTextFieldMethod_source);
						
			sortJListMutants();
			
			
			HashMap<String, Double> metParam = currentSimulation.getMethodParameters();
			Iterator<String> it = metParam.keySet().iterator();
			tableModel = new DefaultTableModel(new Object[] { "","" }, 0){
			    @Override
				public boolean isCellEditable(int row, int column)
			    {
			    	if(column==0) return false;
					return true;
					
			    }
			};
			
			while(it.hasNext()) {
				String name = it.next();
				Vector row = new Vector();
				tableModel.addRow(new Object[] { name,metParam.get(name), });
			}
			table.setModel(tableModel);
			tableModel.fireTableDataChanged();
			table.invalidate();
			
			setVisible(true);
		
			//once the window is closed
			if(exitOption != ExitOption.CANCEL) {
				
				while(!updateName()) {}
				
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
				 
				 
				  source = jTextFieldIntervalSize_source.getText();
					 value = jTextFieldIntervalSize.getText();
					 if(source.startsWith(RunManager.NotesLabels.FROM_ANCESTOR.getLabel())) {
					         int indexSepStart = source.indexOf(RunManager.NotesLabels.FROM_ANCESTOR.getLabel());
				             String parent = source.substring(indexSepStart+1).trim();
				             currentSimulation.addCumulativeChange(
				            		 Simulation.generateChangeKey(SimulationChangeType.INTERVAL_SIZE, ""), 
				            		 new MutablePair<String, String>(parent, value));
				             currentSimulation.removeLocalChange(SimulationChangeType.INTERVAL_SIZE);
				             currentSimulation.removeFromBaseSet(SimulationChangeType.INTERVAL_SIZE);
				        } else if(source.equals(RunManager.NotesLabels.LOCAL.getLabel())){
				    		 currentSimulation.setIntervalSize(value,false);
					   	} else if(source.equals(RunManager.NotesLabels.FROM_BASESET.getLabel())){
					   		 currentSimulation.setIntervalSize(new Double(Double.parseDouble(SimulationsDB.DEFAULT_DURATION)/Double.parseDouble(SimulationsDB.DEFAULT_STEPS)).toString(), true);
					   	}
					 
					 
					 source = jTextFieldIntervals_source.getText();
					 value = jTextFieldIntervals.getText();
					 if(source.startsWith(RunManager.NotesLabels.FROM_ANCESTOR.getLabel())) {
					         int indexSepStart = source.indexOf(RunManager.NotesLabels.FROM_ANCESTOR.getLabel());
				             String parent = source.substring(indexSepStart+1).trim();
				             currentSimulation.addCumulativeChange(
				            		 Simulation.generateChangeKey(SimulationChangeType.INTERVAL_NUMBER, ""), 
				            		 new MutablePair<String, String>(parent, value));
				             currentSimulation.removeLocalChange(SimulationChangeType.INTERVAL_NUMBER);
				             currentSimulation.removeFromBaseSet(SimulationChangeType.INTERVAL_NUMBER);
				        } else if(source.equals(RunManager.NotesLabels.LOCAL.getLabel())){
				    		 currentSimulation.setIntervals(value,false);
					   	} else if(source.equals(RunManager.NotesLabels.FROM_BASESET.getLabel())){
					   		 currentSimulation.setIntervals(SimulationsDB.DEFAULT_STEPS, true);
					   	}
					 
					 
					 source = jTextFieldMethod_source.getText();
					 value = jTextFieldMethod.getText();
					 if(source.startsWith(RunManager.NotesLabels.FROM_ANCESTOR.getLabel())) {
					         int indexSepStart = source.indexOf(RunManager.NotesLabels.FROM_ANCESTOR.getLabel());
				             String parent = source.substring(indexSepStart+1).trim();
				             currentSimulation.addCumulativeChange(
				            		 Simulation.generateChangeKey(SimulationChangeType.METHOD, ""), 
				            		 new MutablePair<String, String>(parent, value));
				             currentSimulation.removeLocalChange(SimulationChangeType.METHOD);
				             currentSimulation.removeFromBaseSet(SimulationChangeType.METHOD);
				        } else if(source.equals(RunManager.NotesLabels.LOCAL.getLabel())){
				    		 //currentSimulation.setIntervals(value,false);
					   	} else if(source.equals(RunManager.NotesLabels.FROM_BASESET.getLabel())){
					   		// currentSimulation.setMethod(SimulationsDB.DEFAULT_METHOD, true);
					   	}
					 
				}
			simDB.rename(currentSimulation, jTextFieldName.getText().trim());
			
			
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return;
	}
	
	 private void resetField(SimulationChangeType simtype, JTextField field, JLabel source) {
		 if(conflicts.contains(Simulation.generateChangeKey(simtype, ""))) {
				source.setText(RunManager.NotesLabels.CONFLICT.getLabel());
		   		source.setBackground(GraphicalProperties.color_cell_with_errors);
		   		field.setBackground(GraphicalProperties.color_cell_with_errors);
				field.setText(RunManager.NotesLabels.CONFLICT.getLabel());
				source.setForeground(GraphicalProperties.color_cell_with_errors);
		 }
		 else {	
			 source.setForeground(Color.black);
		   	
		 String expression = null;
		 if(simtype.equals(SimulationChangeType.TOTAL_TIME)) {
			 expression = currentSimulation.getDuration();
		 } else if (simtype.equals(SimulationChangeType.INTERVAL_NUMBER)) {
			 expression = currentSimulation.getIntervals();
		 } else if (simtype.equals(SimulationChangeType.INTERVAL_SIZE)) {
			 expression = currentSimulation.getIntervalSize();
		 }
		 Double evaluated = simDB.RM_buildCopasiExpression(expression,currentSimulation, simtype);
  	  
		if(currentSimulation.hasChange(simtype, "")) {
			source.setText(RunManager.NotesLabels.LOCAL.getLabel());
			field.setBackground(RunManager.colorLocalRedefinition);
			field.setText(expression);
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
		  		 if(simtype.equals(SimulationChangeType.TOTAL_TIME)) { 
		  			 field.setText(SimulationsDB.DEFAULT_DURATION);
		  		 } else if (simtype.equals(SimulationChangeType.INTERVAL_NUMBER)) {
		  			field.setText(SimulationsDB.DEFAULT_STEPS);
				 } else if (simtype.equals(SimulationChangeType.INTERVAL_SIZE)) {
					 field.setText(new Double(Double.parseDouble(SimulationsDB.DEFAULT_DURATION)/Double.parseDouble(SimulationsDB.DEFAULT_STEPS)).toString());
				 } else if (simtype.equals(SimulationChangeType.METHOD)) {
					 field.setText(SimulationsDB.DEFAULT_METHOD);
				 }
		  		field.setBackground(Color.white);
 	   		 }
 	   	  }
 	   	  }
	}
	 
	 private void resetField(SimulationChangeType simtype, JTextField field, JLabel source, String expressionString, int sourceCode, String parent) {
		 if(conflicts.contains(Simulation.generateChangeKey(simtype, ""))) {
				source.setText(RunManager.NotesLabels.CONFLICT.getLabel());
		   		source.setBackground(GraphicalProperties.color_cell_with_errors);
		   		source.setForeground(GraphicalProperties.color_cell_with_errors);
		   		field.setBackground(GraphicalProperties.color_cell_with_errors);
				field.setText(RunManager.NotesLabels.CONFLICT.getLabel());
				
		 }
		 else {
			 source.setForeground(Color.black);
		   		
		 Double evaluated = null;
		 
		 if(simtype!= SimulationChangeType.METHOD) {
			 evaluated = simDB.RM_buildCopasiExpression(expressionString,currentSimulation, simtype);
		 }
		 
		if(sourceCode == RunManager.NotesLabels.LOCAL.getOption()) {
			source.setText(RunManager.NotesLabels.LOCAL.getLabel());
			field.setBackground(RunManager.colorLocalRedefinition);
			field.setText(expressionString);
	   		 if(evaluated != null || simtype == SimulationChangeType.METHOD) {
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
