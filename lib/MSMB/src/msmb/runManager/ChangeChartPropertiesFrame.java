package msmb.runManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;
import java.awt.Color;
import javax.swing.JTextField;
import java.awt.Dialog.ModalityType;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.JSpinner;
import javax.swing.JComboBox;

import msmb.gui.MainGui;
import msmb.runManager.ChangeLinePlotFrame.MyShapes;
import msmb.utility.Constants;
import msmb.utility.GraphicalProperties;

import com.l2fprod.common.swing.JFontChooser;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ChangeChartPropertiesFrame extends JDialog {

	private HashMap<PlotChangeType, Object> localValues = new HashMap<PlotChangeType, Object>();
	private final JPanel contentPanel = new JPanel();
	private JTextField textFieldLabelX;
	private JTextField textField_minX;
	private JTextField textField_maxX;
	private JTextField textFieldLabelY;
	private JTextField textField_minY;
	private JTextField textField_maxY;
	private JLabel labelXfont;
	private JLabel labelYfont;
	private JLabel labelColorTitle;
	private JLabel labelColorX;
	private JLabel labelColorY;
	private JLabel labelColorBackground;
	private JLabel labelLine;
	private ExitOption exitOption = ExitOption.CANCEL;
	private JList list;
	private SortedListModel listModel ;
	
	private SortedListModel listModel_sims;
	private JList list_sims;
	private SortedListModel listModel_simsAdded;
	private JList list_simsAdded;
	private SortedListModel listModel_vars;
	private JList list_vars;
	private SortedListModel listModel_varsAdded;
	private JList list_varsAdded;
	
	Vector<PlottedVariable> plottedVariables_toReturn = new Vector<PlottedVariable>();
	Vector<String> simulations_toReturn = new Vector<String>();
	Vector chartProperties_toReturn = new Vector();
	
	private ListSelectionModel listSelectionModel;
	private JSpinner spinnerLineThickness;
	private JSpinner spinnerInterval;
	private JComboBox comboBoxSymbol;
	private JCheckBox checkBoxShowTitle;
	private JLabel labelTitleFont;
	private JCheckBox checkBoxOrientationVertical;
	private JCheckBox checkBoxAutoadjustY;
	private JCheckBox checkBoxAutoadjustX;
	private DefaultComboBoxModel comboModel_xaxis;
	private JLabel txtName;
	private JTextField jTextFieldName;
	private RMPlot currentPlot;
	private RMPlotDB plotDB;
	private JComboBox comboBoxVariableX;
	private JCheckBox checkboxLogScaleX;
	private JCheckBox checkboxLogScaleY;
	private SimulationsDB simDB;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ChangeChartPropertiesFrame dialog = new ChangeChartPropertiesFrame(null,null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
			dialog.setLocationRelativeTo(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Vector setVariablesAndShow(RMPlot s, Set<Simulation> allSims, Vector<String> allSPC,  Vector<String> allGLQ, Vector<String> allCMP) {
		currentPlot = s;
		listModel.clear();
		listModel_sims.clear();
		listModel_simsAdded.clear();
		listModel_vars.clear();
		listModel_varsAdded.clear();
		comboModel_xaxis.removeAllElements();
		comboModel_xaxis.addElement("Time");
		jTextFieldName.setText(s.getName());
		
		Iterator<String> it = allSPC.iterator();
		while(it.hasNext()) {
			String name = it.next();
			PlottedVariable var = s.getPlottedVariable(name);
			if(var!= null) {
				listModel.add(var);
				listModel_varsAdded.add(var.getName());
			} else {
				listModel_vars.add(new String(name));
			}
			comboModel_xaxis.addElement(name);
		}
		
		it = allGLQ.iterator();
		while(it.hasNext()) {
			String name = it.next();
			name = "Values["+name+"]";
			PlottedVariable var = s.getPlottedVariable(name);
			if(var!= null) {
				listModel.add(var);
				listModel_varsAdded.add(var.getName());
			} else {
				listModel_vars.add(new String(name));
			}
			comboModel_xaxis.addElement(name);
		}
		
		it = allCMP.iterator();
		while(it.hasNext()) {
			String name = it.next();
			name = "Compartments["+name+"]";
			PlottedVariable var = s.getPlottedVariable(name);
			if(var!= null) {
				listModel.add(var);
				listModel_varsAdded.add(var.getName());
			} else {
				listModel_vars.add(new String(name));
			}
			comboModel_xaxis.addElement(name);
		}
		
		Iterator it2 = allSims.iterator();
		while(it2.hasNext()) {
			Simulation sim = (Simulation) it2.next();
			String var = (String) s.getSimulation(sim.getName());
			if(var!= null) {
				listModel_simsAdded.add(var);
			} else {
				listModel_sims.add(sim.getName());
			}
		}
	
		comboModel_xaxis.setSelectedItem(s.getXaxis());
		checkBoxShowTitle.setSelected(s.getShowTitle());
		checkboxLogScaleX.setSelected(s.isLogScaleX());
		checkboxLogScaleY.setSelected(s.isLogScaleY());
		comboBoxVariableX.setSelectedItem(s.getVariableX());
		labelColorBackground.setBackground(s.getPlotBackground());
		labelColorTitle.setBackground(s.getTitleColor());
		labelTitleFont.setText(s.getTitleFont().getName() + ", "+s.getTitleFont().getSize());
		
		textFieldLabelX.setText(s.getXaxis());
		labelColorX.setBackground(s.getLabelXcolor());
		labelXfont.setText(s.getLabelXfont().getName() + ", "+s.getLabelXfont().getSize());
		checkBoxAutoadjustX.setSelected(s.isAutoadjustX());
		String min = new String();
		String max = new String();
		if(!s.isAutoadjustX()) {
			min = new Double(s.getMinX()).toString();
			max = new Double(s.getMaxX()).toString();
		} else {
			textField_minX.setEnabled(false);
			textField_maxX.setEnabled(false);
		}
		textField_minX.setText(min);
		textField_maxX.setText(max);
		
		
		textFieldLabelY.setText(s.getLabelY());
		labelColorY.setBackground(s.getLabelYcolor());
		labelYfont.setText(s.getLabelYfont().getName() + ", "+s.getLabelYfont().getSize());
		checkBoxAutoadjustY.setSelected(s.isAutoadjustY());
		min = new String();
		max = new String();
		if(!s.isAutoadjustY()) {
			min = new Double(s.getMinY()).toString();
			max = new Double(s.getMaxY()).toString();
		}else {
			textField_minY.setEnabled(false);
			textField_maxY.setEnabled(false);
		}
		textField_minY.setText(min);
		textField_maxY.setText(max);
		
		
		
		setCursor(null);
		setVisible(true);
	
		//once the window is closed
		if(exitOption != ExitOption.CANCEL) {
			while(!updateName()) {}
			
			Vector ret = new Vector();
			plottedVariables_toReturn.clear();
			simulations_toReturn.clear();
			chartProperties_toReturn.clear();
			for(int i = 0; i < listModel.getSize(); ++i) {
				plottedVariables_toReturn.add((PlottedVariable) listModel.getElementAt(i));
			}
			for(int i = 0; i < listModel_simsAdded.getSize(); ++i) {
				simulations_toReturn.add((String) listModel_simsAdded.getElementAt(i));
			}
			
			chartProperties_toReturn.add(checkBoxShowTitle.isSelected());
			String fontText = labelTitleFont.getText();
			int indexComma = fontText.lastIndexOf(",");
			Font f = new Font(fontText.substring(0, indexComma).trim(),Font.PLAIN, Integer.parseInt(fontText.substring(indexComma+1).trim()));
			chartProperties_toReturn.add(f);
			chartProperties_toReturn.add(labelColorTitle.getBackground());
			chartProperties_toReturn.add(labelColorBackground.getBackground());
			
			
			chartProperties_toReturn.add(!checkBoxOrientationVertical.isSelected());

			chartProperties_toReturn.add(textFieldLabelX.getText());
			chartProperties_toReturn.add(labelColorX.getBackground());
			fontText = labelXfont.getText();
			indexComma = fontText.lastIndexOf(",");
			f = new Font(fontText.substring(0, indexComma).trim(),Font.PLAIN, Integer.parseInt(fontText.substring(indexComma+1).trim()));
			chartProperties_toReturn.add(f);
			chartProperties_toReturn.add(checkBoxAutoadjustX.isSelected());
			Double minX = new Double(0.0); 
			try {	minX =Double.parseDouble(textField_minX.getText());	}catch(Exception ex){}
			chartProperties_toReturn.add(minX);
			Double maxX = new Double(0.0); 
			try {	maxX =Double.parseDouble(textField_maxX.getText());	}catch(Exception ex){}
			chartProperties_toReturn.add(maxX);
			
			
			chartProperties_toReturn.add(textFieldLabelY.getText());
			chartProperties_toReturn.add(labelColorY.getBackground());
			fontText = labelYfont.getText();
			indexComma = fontText.lastIndexOf(",");
			f = new Font(fontText.substring(0, indexComma).trim(),Font.PLAIN, Integer.parseInt(fontText.substring(indexComma+1).trim()));
			chartProperties_toReturn.add(f);
			chartProperties_toReturn.add(checkBoxAutoadjustY.isSelected());
			Double minY = new Double(0.0); 
			try {	minY =Double.parseDouble(textField_minY.getText());	}catch(Exception ex){}
			chartProperties_toReturn.add(minY);
			Double maxY = new Double(0.0); 
			try {	maxY =Double.parseDouble(textField_maxY.getText());	}catch(Exception ex){}
			chartProperties_toReturn.add(maxY);
			
			chartProperties_toReturn.add(checkboxLogScaleX.isSelected());
			chartProperties_toReturn.add(checkboxLogScaleY.isSelected());
			chartProperties_toReturn.add(comboBoxVariableX.getSelectedItem().toString());
			
			
			ret.add(plottedVariables_toReturn);
			ret.add(simulations_toReturn);
			ret.add(chartProperties_toReturn);
			ret.add(localValues);
			plotDB.rename(s, jTextFieldName.getText().trim());
			return ret;
		}
		
		return null;
	}
	

	 protected boolean updateName() {
			
		 	String newName = jTextFieldName.getText().trim();
	 		  if(newName.compareTo(currentPlot.getName())==0) return true;
			  boolean success = true;
			  if(newName.length()==0) {
					JOptionPane.showMessageDialog(null, "The name cannot be empty.\n", "Error!", JOptionPane.ERROR_MESSAGE);
					success= false;
			  }
			  else if(plotDB.isNameDuplicate(newName)) {
				JOptionPane.showMessageDialog(null, "The name already exists.\nProvide a different name.", "Error!", JOptionPane.ERROR_MESSAGE);
				success= false;
			  }
			  return success;
		  }
	 
	 
	public ChangeChartPropertiesFrame(RMPlotDB plotDB, final SimulationsDB simDB ) {
		this.plotDB = plotDB;
		this.simDB = simDB;
		setModal(true);
		setResizable(true);
		setTitle("Plot properties");
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		int fractionForWidth = 5;
		int fractionForHeight = 3;
		{
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			contentPanel.add(tabbedPane);
			{
				JPanel panelSims = new JPanel();
				tabbedPane.addTab("Time Courses & Variables", null, panelSims, null);
				panelSims.setLayout(new BorderLayout(0, 0));
				{
					{
						{
							{
								listModel_sims = new SortedListModel();
								
							}
						}
					}
					{
						{
							{
								listModel_simsAdded = new SortedListModel();
							}
						}
					}
				}
				{
					{
						{
							{
								listModel_vars = new SortedListModel();
							}
						}
					}
					{
						{
							{
								listModel_varsAdded = new SortedListModel();
							}
						}
					}
				}
				{
					JPanel panelup = new JPanel();
					panelSims.add(panelup, BorderLayout.CENTER);
					panelup.setLayout(new GridLayout(2, 0, 0, 0));
					JPanel panel = new JPanel();
					panelup.add(panel);
					panel.setBorder(new TitledBorder(null, "Time courses:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					panel.setLayout(new GridLayout(0, 3, 0, 0));
					JPanel panel_1 = new JPanel();
					panel.add(panel_1);
					panel_1.setLayout(new BorderLayout(0, 0));
					JScrollPane scrollPane = new JScrollPane();
					panel_1.add(scrollPane);
					list_sims = new JList(listModel_sims);
					scrollPane.setViewportView(list_sims);
					{
						JPanel panel_buttons = new JPanel();
						panel_buttons.setBorder(new EmptyBorder(9, 9, 9, 9));
						panel.add(panel_buttons);
					
						{
							JButton btnAddSelected = new JButton("Add selected -->");
							btnAddSelected.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									Object[] sel = list_sims.getSelectedValues();
									if(sel==null) return;
									listModel_simsAdded.addAll(sel);
									for(int i = 0; i < sel.length; ++i) {
										listModel_sims.removeElement(sel[i]);
									}
									list_sims.clearSelection();
									list_simsAdded.clearSelection();
								}
							});
							panel_buttons.setLayout(new GridLayout(0, 1, 3, 3));
							panel_buttons.add(btnAddSelected);
						}
						{
							JButton btnAddwithDescendants = new JButton("Add (recursive) ->");
							btnAddwithDescendants.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									Object[] sel = list_sims.getSelectedValues();
									if(sel==null) return;
									for(int i = 0; i < sel.length; ++i) {
										String current = sel[i].toString();
										listModel_sims.removeElement(current);
										listModel_simsAdded.add(current);
										Vector<Simulation> desc = simDB.collectDescendants(new Simulation(current));
										for(int j = 0; j < desc.size(); ++j) {
											String s = desc.get(j).getName();
											listModel_simsAdded.add(s);
											listModel_sims.removeElement(s);
										}
									}
									list_sims.clearSelection();
									list_simsAdded.clearSelection();
								}
							});
							panel_buttons.add(btnAddwithDescendants);
						}
						{
							JButton btnAddAll = new JButton("Add all ->");
							btnAddAll.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									for(int i = 0; i < listModel_sims.getSize(); ++i) {
										listModel_simsAdded.add(listModel_sims.getElementAt(i));
									}
									listModel_sims.clear();
									list_simsAdded.clearSelection();
									list_sims.clearSelection();
								}
								
							});
							panel_buttons.add(btnAddAll);
						}
						{
							JButton button = new JButton("<- Delete selected");
							button.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									Object[] sel = list_simsAdded.getSelectedValues();
									if(sel==null) return;
									listModel_sims.addAll(sel);
									for(int i = 0; i < sel.length; ++i) {
										listModel_simsAdded.removeElement(sel[i]);
									}
									list_sims.clearSelection();
									list_simsAdded.clearSelection();
								}
							});
							panel_buttons.add(button);
						}
						{
							JButton button = new JButton("<- Delete all");
							button.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									for(int i = 0; i < listModel_simsAdded.getSize(); ++i) {
										listModel_sims.add(listModel_simsAdded.getElementAt(i));
									}
									listModel_simsAdded.clear();
									list_simsAdded.clearSelection();
									list_sims.clearSelection();
								}
								
							});
							panel_buttons.add(button);
						}
					}
					
						JPanel panel_2 = new JPanel();
						
						panel.add(panel_2);
						
						panel_2.setLayout(new BorderLayout(0, 0));
						JScrollPane scrollPane_1 = new JScrollPane();
						panel_2.add(scrollPane_1);
						list_simsAdded = new JList(listModel_simsAdded);
						scrollPane_1.setViewportView(list_simsAdded);
						{
							JPanel panel_3 = new JPanel();
							panelup.add(panel_3);
							panel_3.setBorder(new TitledBorder(null, "Y axis: ", TitledBorder.LEADING, TitledBorder.TOP, null, null));
							panel_3.setLayout(new BorderLayout(0, 0));
							JPanel panel_2_1 = new JPanel();
							panel_3.add(panel_2_1, BorderLayout.CENTER);
							panel_2_1.setBorder(null);
							panel_2_1.setLayout(new GridLayout(1, 3, 0, 0));
							JPanel panel_1_1 = new JPanel();
							panel_2_1.add(panel_1_1);
							panel_1_1.setLayout(new BorderLayout(0, 0));
							JScrollPane scrollPane_2 = new JScrollPane();
							scrollPane_2.setPreferredSize(new Dimension(this.getPreferredSize().width/fractionForWidth, this.getPreferredSize().height/fractionForHeight));
							panel_1_1.add(scrollPane_2, BorderLayout.CENTER);
							list_vars = new JList(listModel_vars);
							scrollPane_2.setViewportView(list_vars);
							{
								JPanel panel_11 = new JPanel();
								panel_11.setBorder(new EmptyBorder(9, 9, 9, 9));
								panel_11.setLayout(new GridLayout(4, 1,9,9));
								panel_2_1.add(panel_11);
								{
									JButton button = new JButton("Add selected ->");
									button.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											Object[] sel = list_vars.getSelectedValues();
											if(sel==null) return;
											for(int i = 0; i < sel.length; ++i) {
												listModel_varsAdded.add(sel[i]);
												int index = listModel.getSize()+i;
												 int indexShape = index%(MyShapes.values().length-1);
												listModel.add(new PlottedVariable(sel[i].toString(), 
														RunManager.colorPalette.get(index),
														1,
														indexShape+1));
											}
											
											for(int i = 0; i < sel.length; ++i) {
												listModel_vars.removeElement(sel[i]);
											}
											list_vars.clearSelection();
											list_varsAdded.clearSelection();
										}
									});
									panel_11.setLayout(new GridLayout(0, 1, 0, 3));
									panel_11.add(button);
								}
								
								{
									JButton button = new JButton("Add all ->");
									panel_11.add(button);
									button.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											for(int i = 0; i < listModel_vars.getSize(); ++i) {
												listModel_varsAdded.add(listModel_vars.getElementAt(i));
												int index = listModel.getSize()+i;
												int indexShape = index%(MyShapes.values().length-1);
												
												listModel.add(new PlottedVariable(listModel_vars.getElementAt(i).toString(), 
														RunManager.colorPalette.get(index),
														1,
														indexShape+1));
											}
											listModel_vars.clear();
											list_varsAdded.clearSelection();
											list_vars.clearSelection();
										}
										
									});
								}
								{
									JButton button = new JButton("<- Delete selected");
									button.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											Object[] sel = list_varsAdded.getSelectedValues();
											if(sel==null) return;
											listModel_vars.addAll(sel);
											for(int i = 0; i < sel.length; ++i) {
												listModel_varsAdded.removeElement(sel[i]);
												listModel.removeElement(new PlottedVariable(sel[i].toString()));
											}
											list_vars.clearSelection();
											list_varsAdded.clearSelection();
										}
									});
									panel_11.add(button);
								}
								{
									JButton button = new JButton("<- Delete all");
									button.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											for(int i = 0; i < listModel_varsAdded.getSize(); ++i) {
												listModel_vars.add(listModel_varsAdded.getElementAt(i));
											}
											listModel_varsAdded.clear();
											listModel.clear();
											list_varsAdded.clearSelection();
											list_vars.clearSelection();
										}
										
									});
									panel_11.add(button);
								}
								{
									JLabel label = new JLabel("");
									panel_11.add(label);
								}
							}
							JPanel panel_11 = new JPanel();
							panel_2_1.add(panel_11);
							panel_11.setLayout(new BorderLayout(0, 0));
							JScrollPane scrollPane1 = new JScrollPane();
							scrollPane1.setPreferredSize(new Dimension(this.getPreferredSize().width/fractionForWidth, this.getPreferredSize().height/fractionForHeight));
							
							panel_11.add(scrollPane1, BorderLayout.CENTER);
							list_varsAdded = new JList(listModel_varsAdded);
							scrollPane1.setViewportView(list_varsAdded);
							{
								checkboxLogScaleY = new JCheckBox("log scale?");
								panel_3.add(checkboxLogScaleY, BorderLayout.SOUTH);
							}
						}
				}
				{
					JPanel panel_1_1 = new JPanel();
					panelSims.add(panel_1_1, BorderLayout.SOUTH);
					panel_1_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "X axis: ", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
					panel_1_1.setLayout(new BorderLayout(0, 0));
					{
						checkboxLogScaleX = new JCheckBox("log scale?");
						panel_1_1.add(checkboxLogScaleX, BorderLayout.SOUTH);
					}
					{
						comboModel_xaxis = new DefaultComboBoxModel();
						comboBoxVariableX = new JComboBox(comboModel_xaxis);
						comboBoxVariableX.addItemListener(new ItemListener() {
							@Override
						    public void itemStateChanged(ItemEvent event) {
						       if (event.getStateChange() == ItemEvent.SELECTED) {
						          Object item = event.getItem();
						          textFieldLabelX.setText(item.toString());
						        }
						    }       
						});
						panel_1_1.add(comboBoxVariableX, BorderLayout.CENTER);
					}
				}
			}
			{
				JPanel panelVars = new JPanel();
				tabbedPane.addTab("Curves layout", null, panelVars, null);
				panelVars.setLayout(new GridLayout(0, 1, 0, 0));
				{
					JPanel panel = new JPanel();
					panelVars.add(panel);
					panel.setLayout(new BorderLayout(0, 0));
					{
						JPanel panel_1 = new JPanel();
						panel.add(panel_1, BorderLayout.CENTER);
						panel_1.setLayout(new BorderLayout(0, 0));
						{
							JPanel panel_2 = new JPanel();
							panel_1.add(panel_2);
							panel_2.setBorder(new TitledBorder(null, "Available variables:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
							panel_2.setLayout(new BorderLayout(0, 0));
							{
								JScrollPane scrollPane = new JScrollPane();
								
								panel_2.add(scrollPane);
								{
									listModel= new SortedListModel();
									list = new JList(listModel );
									listSelectionModel = list.getSelectionModel();
									 listSelectionModel.addListSelectionListener(new ListSelectionListener() {
																	@Override
																	  public void valueChanged(ListSelectionEvent e) {
																		boolean isAdjusting = e.getValueIsAdjusting();
																		if(isAdjusting) return;

																		ListSelectionModel lsm = (ListSelectionModel)e.getSource();

																		if (!lsm.isSelectionEmpty()) {
																			int minSel = lsm.getMinSelectionIndex();
																			if(minSel!=-1) {
																				PlottedVariable sel = (PlottedVariable) listModel.getElementAt(minSel);
																				labelLine.setBackground(sel.getColor());
																				spinnerLineThickness.setValue(sel.getLineWidth());
																				spinnerInterval.setValue(sel.getIntervalPlot());
																				comboBoxSymbol.setSelectedIndex(sel.getIndexShape());

																			}
																		}
																       
																    }
																});
									    
									scrollPane.setViewportView(list);
								}
							}
						}
						{
							JPanel panel_2 = new JPanel();
							panel_1.add(panel_2, BorderLayout.SOUTH);
							panel_2.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Properties:", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
							panel_2.setLayout(new GridLayout(0, 3, 0, 0));
							{
								JLabel label = new JLabel("Color:");
								label.setHorizontalAlignment(SwingConstants.LEFT);
								panel_2.add(label);
							}
							{
								labelLine = new JLabel(" ");
								labelLine.setOpaque(true);
								labelLine.setHorizontalAlignment(SwingConstants.TRAILING);
								labelLine.setBackground(Color.BLACK);
								panel_2.add(labelLine);
							}
							{
								JButton button = new JButton("Select...");
								panel_2.add(button);
								button.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										openColorChooser(labelLine);
									}
								});
							}
							{
								JLabel lblThickness = new JLabel("Thickness:");
								panel_2.add(lblThickness);
							}
							{
								 spinnerLineThickness = new JSpinner();
								panel_2.add(spinnerLineThickness);
							}
							{
								JPanel panel_3 = new JPanel();
								panel_2.add(panel_3);
							}
							{
								JLabel lblSymbol = new JLabel("Symbol:");
								panel_2.add(lblSymbol);
							}
							{
								comboBoxSymbol = new JComboBox();
								 MyShapes[] shapes = MyShapes.values();
								 for(int i = 0; i < shapes.length; ++i) {
									 comboBoxSymbol.addItem(shapes[i].getLabel());
								 }
								panel_2.add(comboBoxSymbol);
							}
							{
								JPanel panel_3 = new JPanel();
								panel_2.add(panel_3);
							}
							{
								JLabel lblPlotEvery = new JLabel("Plot every");
								panel_2.add(lblPlotEvery);
							}
							{
								spinnerInterval = new JSpinner();
								panel_2.add(spinnerInterval);
							}
							{
								JLabel lblDataPoints = new JLabel("  data point(s)");
								panel_2.add(lblDataPoints);
							}
						}
					}
					{
						JPanel panel_1 = new JPanel();
						FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
						flowLayout.setAlignment(FlowLayout.LEFT);
						panel.add(panel_1, BorderLayout.SOUTH);
						{
							JButton btnNewButton_2 = new JButton("Apply changes");
							btnNewButton_2.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
							
									Color c = labelLine.getBackground();
									float t = 1.0f;
									try {
										t = Float.parseFloat(spinnerLineThickness.getValue().toString());
									} catch(Exception ex){
										if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
									}
									int interval = 1;
									try {
										interval = Integer.parseInt(spinnerInterval.getValue().toString());
									} catch(Exception ex){
										if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
									}
									
									int shape = comboBoxSymbol.getSelectedIndex();

									Object[] sel = list.getSelectedValues();
									Vector<PlottedVariable> toAdd = new Vector<PlottedVariable>();
									for(int i = 0; i < sel.length; i++) {
										PlottedVariable newvar = new PlottedVariable(sel[i].toString(), c, t, shape);
										newvar.setIntervalPlot(interval);
										toAdd.add(newvar);
									}
									for(int i = 0; i < sel.length; ++i) {
										listModel.removeElement(sel[i]);
									}
									listModel.addAll(toAdd.toArray());
									list.clearSelection();
									comboBoxSymbol.setSelectedIndex(0);
									spinnerLineThickness.setValue(new Float(1));
									spinnerInterval.setValue(new Integer(1));
									
								}
							});
							panel_1.add(btnNewButton_2);
						}
					}
				}
			}
			{
				JPanel panelGeneral = new JPanel();
				tabbedPane.addTab("Plot layout", null, panelGeneral, null);
				panelGeneral.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel = new JPanel();
					panel.setBorder(new TitledBorder(null, "Title: ", TitledBorder.LEFT, TitledBorder.TOP, null, null));
					panelGeneral.add(panel, BorderLayout.NORTH);
					panel.setLayout(new GridLayout(0, 3, 0, 0));
					{
						JLabel lblShowTitle = new JLabel("Show title:");
						lblShowTitle.setHorizontalAlignment(SwingConstants.LEFT);
						panel.add(lblShowTitle);
					}
					{
						checkBoxShowTitle = new JCheckBox("");
						checkBoxShowTitle.setSelected(true);
						panel.add(checkBoxShowTitle);
					}
					{
						JLabel label = new JLabel("");
						panel.add(label);
					}
					{
						JLabel lblFont = new JLabel("Font:");
						lblFont.setHorizontalAlignment(SwingConstants.LEFT);
						panel.add(lblFont);
					}
					{
						labelTitleFont = new JLabel("Tahoma Bold, 20");
						panel.add(labelTitleFont);
					}
					{
						JButton btnSelect = new JButton("Select...");
						panel.add(btnSelect);
						btnSelect.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								openFontChooser(labelTitleFont);
							}
						});
					}
					{
						JLabel lblNewLabel = new JLabel("Color:");
						lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
						panel.add(lblNewLabel);
					}
					{
						labelColorTitle = new JLabel(" ");
						labelColorTitle.setHorizontalAlignment(SwingConstants.TRAILING);
						labelColorTitle.setBackground(Color.BLACK);
						labelColorTitle.setOpaque(true);
						panel.add(labelColorTitle);
					}
					{
						JButton btnSelect_1 = new JButton("Select...");
						panel.add(btnSelect_1);
						btnSelect_1.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								openColorChooser(labelColorTitle);
							}
						});
					}
				}
				{
					JPanel panel = new JPanel();
					panelGeneral.add(panel, BorderLayout.SOUTH);
					panel.setLayout(new GridLayout(0, 3, 0, 0));
					{
						JLabel lblBackgroundColor = new JLabel("Chart background color:");
						panel.add(lblBackgroundColor);
					}
					{
						labelColorBackground = new JLabel(" ");
						labelColorBackground.setOpaque(true);
						labelColorBackground.setBackground(Color.WHITE);
						panel.add(labelColorBackground);
					}
					{
						JButton btnSelect_2 = new JButton("Select...");
						panel.add(btnSelect_2);
						btnSelect_2.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								openColorChooser(labelColorBackground);
							}
						});
						
					}
				}
				{
					JPanel panel = new JPanel();
					panel.setBorder(new TitledBorder(null, "Axis:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					panelGeneral.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BorderLayout(0, 0));
					{
						JTabbedPane tabbedPane_1 = new JTabbedPane(JTabbedPane.TOP);
						panel.add(tabbedPane_1);
						{
							JPanel panelX = new JPanel();
							tabbedPane_1.addTab("X axis", null, panelX, null);
							panelX.setLayout(new GridLayout(2, 0, 0, 0));
							{
								JPanel panel_1 = new JPanel();
								panel_1.setBorder(new TitledBorder(null, "General:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
								panelX.add(panel_1);
								panel_1.setLayout(new GridLayout(0, 3, 0, 0));
								{
									JLabel lblNewLabel_1 = new JLabel("Label:");
									panel_1.add(lblNewLabel_1);
								}
								{
									textFieldLabelX = new JTextField();
									panel_1.add(textFieldLabelX);
									textFieldLabelX.setColumns(10);
								}
								{
									JPanel panel_1_1 = new JPanel();
									panel_1.add(panel_1_1);
								}
								{
									JLabel lblNewLabel_2 = new JLabel("Font:");
									panel_1.add(lblNewLabel_2);
								}
								{
									labelXfont = new JLabel("Tahoma Bold, 20");
									panel_1.add(labelXfont);
								}
								{
									JButton btnNewButton = new JButton("Select...");
									btnNewButton.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											openFontChooser(labelXfont);
											
										}
									});
									panel_1.add(btnNewButton);
								}
								{
									JLabel lblNewLabel_4 = new JLabel("Color:");
									panel_1.add(lblNewLabel_4);
								}
								
								{
									labelColorX = new JLabel(" ");
									labelColorX.setOpaque(true);
									labelColorX.setHorizontalAlignment(SwingConstants.TRAILING);
									labelColorX.setBackground(Color.BLACK);
									panel_1.add(labelColorX);
								}
								{
									JButton btnNewButton_1 = new JButton("Select...");
									panel_1.add(btnNewButton_1);
									btnNewButton_1.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											openColorChooser(labelColorX);
											
										}
									});
								}
							}
							{
								JPanel panel_1 = new JPanel();
								panel_1.setBorder(new TitledBorder(null, "Other:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
								panelX.add(panel_1);
								panel_1.setLayout(new GridLayout(3, 0, 0, 0));
								{
									JPanel panel_2 = new JPanel();
									panel_1.add(panel_2);
								}
								{
									checkBoxAutoadjustX = new JCheckBox("Auto-adjust range");
									checkBoxAutoadjustX.setSelected(true);
									checkBoxAutoadjustX.addItemListener(new ItemListener() {
										@Override
										public void itemStateChanged(ItemEvent e) {
										    if (e.getStateChange() == ItemEvent.SELECTED) {
										    	textField_minX.setEnabled(false);
										    	textField_maxX.setEnabled(false);
										    } else {
										    	textField_minX.setEnabled(true);
										    	textField_maxX.setEnabled(true);
										    }
										}
									});
									panel_1.add(checkBoxAutoadjustX);
								}
								{
									JLabel lblMinimum = new JLabel("Minimum:");
									panel_1.add(lblMinimum);
								}
								{
									textField_minX = new JTextField();
									panel_1.add(textField_minX);
									textField_minX.setColumns(10);
								}
								{
									JLabel lblMaximum = new JLabel("Maximum:");
									panel_1.add(lblMaximum);
								}
								{
									textField_maxX = new JTextField();
									panel_1.add(textField_maxX);
									textField_maxX.setColumns(10);
								}
							}
						}
						{
							JPanel panelY = new JPanel();
							tabbedPane_1.addTab("Y axis", null, panelY, null);
							panelY.setLayout(new GridLayout(2, 0, 0, 0));
							{
								JPanel panel_2 = new JPanel();
								panel_2.setBorder(new TitledBorder(null, "General:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
								panelY.add(panel_2);
								panel_2.setLayout(new GridLayout(0, 3, 0, 0));
								{
									JLabel label = new JLabel("Label:");
									panel_2.add(label);
								}
								{
									textFieldLabelY = new JTextField();
									textFieldLabelY.setColumns(10);
									panel_2.add(textFieldLabelY);
								}
								{
									JPanel panel_3 = new JPanel();
									panel_2.add(panel_3);
								}
								{
									JLabel label = new JLabel("Font:");
									panel_2.add(label);
								}
								{
									labelYfont = new JLabel("Tahoma Bold, 20");
									panel_2.add(labelYfont);
								}
								{
									JButton button = new JButton("Select...");
									panel_2.add(button);
									button.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											openFontChooser(labelYfont);
											
										}
									});
								}
								{
									JLabel label = new JLabel("Color:");
									panel_2.add(label);
								}
								{
									labelColorY = new JLabel(" ");
									labelColorY.setOpaque(true);
									labelColorY.setHorizontalAlignment(SwingConstants.TRAILING);
									labelColorY.setBackground(Color.BLACK);
									panel_2.add(labelColorY);
								}
								{
									JButton button = new JButton("Select...");
									panel_2.add(button);
									button.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											openColorChooser(labelColorY);
										}
									});
								}
							}
							{
								JPanel panel_2 = new JPanel();
								panel_2.setBorder(new TitledBorder(null, "Other:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
								panelY.add(panel_2);
								panel_2.setLayout(new GridLayout(3, 0, 0, 0));
								{
									JPanel panel_3 = new JPanel();
									panel_2.add(panel_3);
								}
								{
									checkBoxAutoadjustY = new JCheckBox("Auto-adjust range");
									checkBoxAutoadjustY.setSelected(true);
									checkBoxAutoadjustY.addItemListener(new ItemListener() {
										@Override
										public void itemStateChanged(ItemEvent e) {
										    if (e.getStateChange() == ItemEvent.SELECTED) {
										    	textField_minY.setEnabled(false);
										    	textField_maxY.setEnabled(false);
										    } else {
										    	textField_minY.setEnabled(true);
										    	textField_maxY.setEnabled(true);
										    }
										}
									});
									panel_2.add(checkBoxAutoadjustY);
								}
								{
									JLabel label = new JLabel("Minimum:");
									panel_2.add(label);
								}
								{
									textField_minY = new JTextField();
									textField_minY.setColumns(10);
									panel_2.add(textField_minY);
								}
								{
									JLabel label = new JLabel("Maximum:");
									panel_2.add(label);
								}
								{
									textField_maxY = new JTextField();
									textField_maxY.setColumns(10);
									panel_2.add(textField_maxY);
								}
							}
						}
					}
					{
						checkBoxOrientationVertical = new JCheckBox("Horizontal orientation (unchecked = Vertical)");
						panel.add(checkBoxOrientationVertical, BorderLayout.NORTH);
					}
				}
			}
		}
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.NORTH);
			panel.setLayout(new BorderLayout(0, 0));
			{
				txtName = new JLabel();
				txtName.setText("Name: ");
				panel.add(txtName, BorderLayout.WEST);
			}
			{
				jTextFieldName = new JTextField();
				panel.add(jTextFieldName, BorderLayout.CENTER);
				jTextFieldName.setColumns(10);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						exitOption = ExitOption.OK;
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						exitOption = ExitOption.CANCEL;
						dispose();
					}
				});
				buttonPane.add(cancelButton);
			}
		}
	}

	protected void openFontChooser(JLabel label) {
		Font selected = JFontChooser.showDialog(null, "Select font", MainGui.customFont);
		if(selected!= null)	label.setText(selected.getFontName() + ", "+ selected.getSize());
	}
	
	protected void openColorChooser(JLabel label) {
		 Color selected = JColorChooser.showDialog(null, "Select font", label.getBackground());
		 
		 if(selected!= null) {
			 if(label.equals(labelColorX)) {	 localValues.put(PlotChangeType.LABELXCOLOR, selected); }
			 else if(label.equals(labelColorY)) {	 localValues.put(PlotChangeType.LABELYCOLOR, selected); }
			 else if(label.equals(labelColorTitle)) {	 localValues.put(PlotChangeType.COLOR_TITLE, selected); }
			 else if(label.equals(labelColorBackground)) {	 localValues.put(PlotChangeType.PLOT_BACKGROUND, selected); }
			 label.setBackground(selected);
		 }
	}




}
