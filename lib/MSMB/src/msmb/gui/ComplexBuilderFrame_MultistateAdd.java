package msmb.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.JTextPane;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import java.awt.GridLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;
import javax.swing.DefaultComboBoxModel;

import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.collect.HashBiMap;

import msmb.model.MultistateSpecies;
import msmb.parsers.mathExpression.MR_Expression_Parser_ReducedParserException;
import msmb.parsers.mathExpression.syntaxtree.CompleteListOfExpression;
import msmb.parsers.mathExpression.visitor.ExtractElementsVisitor;
import msmb.parsers.multistateSpecies.MR_MultistateSpecies_Parser;
import msmb.parsers.multistateSpecies.ParseException;
import msmb.parsers.multistateSpecies.syntaxtree.CompleteMultistateSpecies_Range;
import msmb.parsers.multistateSpecies.syntaxtree.CompleteMultistateSpecies_RangeString;
import msmb.parsers.multistateSpecies.visitor.MultistateSpeciesVisitor;
import msmb.utility.CellParsers;
import msmb.utility.GraphicalProperties;
import msmb.utility.MySyntaxException;
import msmb.utility.SwingUtils;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ComplexBuilderFrame_MultistateAdd extends JDialog implements WindowListener {

	private JPanel contentPane;
	private JTextField txtSomething;
	private JTextField textField;
	private JPanel panel_2;
	private int totSites;
	private MultistateSpecies currentMultistateSpecies;
	//private DefaultComboBoxModel comboBoxModel;
	private JTextPane jTextPane_message;
	private ComplexBuilderFrame parentFrame;
	private Vector<String> siteNameAlreadyUsed = new Vector<String>();
	protected ExitOption exitOption;
	static HashSet<String> usedNames = new HashSet<String>();
	protected static String selectOneString = "(Select one)";
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ComplexBuilderFrame_MultistateAdd frame = new ComplexBuilderFrame_MultistateAdd(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	
	public ComplexBuilderFrame_MultistateAdd(ComplexBuilderFrame complexBuilderFrame)  {
		
		parentFrame = complexBuilderFrame;
		setTitle("Add sites to COMPLEX");
		setModal(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		jTextPane_message = new JTextPane();
		jTextPane_message.setEditable(false);
		
		contentPane.add(jTextPane_message, BorderLayout.NORTH);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.CENTER);
		
		panel_2 = new JPanel();
		scrollPane.setViewportView(panel_2);
		
		panel_2.setLayout(new GridLayout(5, 1, 0, 3));
		
		JPanel panel_3 = new JPanel();
		panel.add(panel_3, BorderLayout.NORTH);
		panel_3.setLayout(new BorderLayout(0, 0));
		
	
		totSites = 1;
		JButton btnNewButton = new JButton("Add site");
		
		addTitleLabels();
		
	
		
		panel_3.add(btnNewButton, BorderLayout.NORTH);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPanel panel_new = createPanelNewSite(null,null,null);
				if(totSites>4) {
					panel_2.setLayout(new GridLayout(++totSites, 1, 0, 3));
				} else {++totSites;}
				panel_2.add(panel_new);
				//revalidate();
			}
		});
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.SOUTH);
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					parentFrame.addElementsToComplex(getElementsToAdd());
					panel_2.removeAll();
					addTitleLabels();
					exitOption = ExitOption.OK;
					setVisible(false);
				} catch(Exception ex) {
					//something wrong in the consistency checks so I cannot add the sites or dispose the dialog
					ex.printStackTrace();
				}
			}
		});
		panel_1.add(btnOk);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
					panel_2.removeAll();
					addTitleLabels();
					exitOption = ExitOption.CANCEL;
					setVisible(false);
			}
		});	
		panel_1.add(btnCancel);
		
		
		GraphicalProperties.resetFonts(this);
		addWindowListener(this);
	}

	
	private void addTitleLabels() {
		JPanel panel_labels = new JPanel();
		Border border = LineBorder.createGrayLineBorder();
		panel_labels.setLayout(new GridLayout(0, 3, 3, 3));
		JLabel name = new JLabel(" New site name ");
		name.setBorder(border);
		panel_labels.add(name);
		JLabel name2 = new JLabel(" Available sites ");
		panel_labels.add(name2);
		name2.setBorder(border);
		
		JLabel name3 = new JLabel(" States restrictions ");
		panel_labels.add(name3);
		name3.setBorder(border);
		
		GraphicalProperties.resetFonts(panel_labels);
		panel_2.add(panel_labels);
		
	}

	@Override
	public void setVisible(boolean b) {
		GraphicalProperties.resetFonts(this);
		pack();
		setLocationRelativeTo(null);
		super.setVisible(b);
	}

	protected Vector getElementsToAdd() throws Exception {
		//first entry: Vector<MutablePair<String, String>> to make the species definition (site1{subRangeSelected};site2{subRangeSelected})
		//second entry(to build the reactions): a vector where each element is a vector of 3 elements:
		//								- complexSite name
		//								- multistateSpecies name origin
		//								- multiateSpecies site origin
		//third entry (to build the reaction): a hashmap with sites and state selected (note: here we can have the site states empty because it will be used in the reactants)
		//forth entry: hashbimap of renamed sites
		
		Vector<String> currentNames = new Vector<String>();
		currentNames.addAll(siteNameAlreadyUsed);
		Vector<String> siteTracked = new Vector<String>();
		
		Vector<MutablePair<String, String>> first_entry = new Vector<MutablePair<String, String>>();
		Vector<Vector<String>> second_entry = new Vector<Vector<String>>();
		HashMap<String,String> third_entry = 	new HashMap<String,String> ();
			
		//Loop in the panels and pick site name and subRange (if empty subrange, pick definition of currentMultistateSpecies)
		List<JPanel> sitePanels = SwingUtils.getDescendantsOfClass(JPanel.class,panel_2);
			
		// Iterator<JTextField> it4 = textFields.iterator();
	
		 
		for(int i = 1; i < sitePanels.size(); i++) {
			 MutablePair<String, String> element_for_first_entry = new MutablePair<String, String>();
			 Vector<String> element_for_second_entry = new  Vector<String>();
				 
			 JPanel currentSite = sitePanels.get(i);
			
			 List<JComboBox> comboBoxes = SwingUtils.getDescendantsOfClass(JComboBox.class,currentSite);
			List<JTextField> textFields = SwingUtils.getDescendantsOfClass(JTextField.class,currentSite);
		
			if(textFields.get(1).getBackground()==GraphicalProperties.color_cell_with_errors) {
				JOptionPane.showMessageDialog(null, "There are unresolved issues in some sites!","Consistency checks",JOptionPane.WARNING_MESSAGE);
				throw new Exception();
			}
			
			String siteName = textFields.get(0).getText();
			if(!currentNames.contains(siteName)) {
				currentNames.add(siteName);
			} else {
				JOptionPane.showMessageDialog(null, "Duplicate names are not allowed ("+siteName+")!", "Consistency checks",JOptionPane.WARNING_MESSAGE);
				throw new Exception();
			}
			
			 String statesString = textFields.get(1).getText();
			 String site = comboBoxes.get(0).getSelectedItem().toString();
				if(!siteTracked.contains(site)) {
					siteTracked.add(site);
				} else {
					JOptionPane.showMessageDialog(null, "You cannot track the same site more than once ("+site+")!", "Consistency checks",JOptionPane.WARNING_MESSAGE);
					throw new Exception();
				}
			 
			if(siteName.length() ==0 || site.compareTo(selectOneString)==0) continue;
			element_for_first_entry.left = new String(siteName);
			element_for_second_entry.add(siteName);
			element_for_second_entry.add(currentMultistateSpecies.getSpeciesName());
			element_for_second_entry.add(site);
			
			third_entry.put(site, statesString);
				
			 if(statesString.length() ==0) {
					 Vector states = currentMultistateSpecies.getSiteStates(site);
					 String start = (String)states.get(0);
					 String end = (String)states.get(1);
					 String list = (String)states.get(2);
					 boolean bool = (Boolean)states.get(3);
						
					if(start.equals("0") & end.equals("0")) {
						statesString = list;
					 }
					else{
						statesString = start + ":"+end;
					}
			 }
			  element_for_first_entry.right = new String(statesString);
			 first_entry.add(element_for_first_entry);
			 second_entry.add(element_for_second_entry);
			 
		}
			 
	
				 
		
		Vector ret = new Vector();
		ret.add(first_entry);
		ret.add(second_entry);
		ret.add(third_entry);
		ret.add(renamed_sites);
		return ret;
	}

	private JPanel createPanelNewSite(String siteName, String originSite, String states) {
		JPanel panel_4 = new JPanel();
		if(currentMultistateSpecies != null) {
			panel_4.setLayout(new GridLayout(0, 3, 3, 3));
			txtSomething = new JTextField();
			panel_4.add(txtSomething);
			txtSomething.setColumns(10);
			
			if(siteName!= null) txtSomething.setText(siteName);
			
			txtSomething.addFocusListener(new FocusListener_Renaming(this));
			
			
			
			Set names = currentMultistateSpecies.getSitesNames();
			DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
			comboBoxModel.addElement(selectOneString );
			for(Object current : names) {
				comboBoxModel.addElement(current);
			}
		  JComboBox comboBoxNewSite = new JComboBox(comboBoxModel);
		  if(originSite != null) comboBoxNewSite.setSelectedItem(originSite);
		  
		  
			textField = new JTextField();
			textField.addFocusListener(new FocusListener_withComboBox(comboBoxNewSite, this));
			
			if(states!= null) {
				textField.setText(states);
				try {
					fixStatesString(originSite, states);
					textField.setBackground(Color.WHITE);
				} catch (Throwable ex) {
					//something wrong in the format, color textfield to show the problem
					textField.setBackground(GraphicalProperties.color_cell_with_errors);
				}
			}
			
			comboBoxNewSite.addItemListener(new ItemChangeListener_withTextField(textField, this));
		  
			panel_4.add(comboBoxNewSite);
			
			
			panel_4.add(textField);
				textField.setColumns(10);
			}
			
			GraphicalProperties.resetFonts(panel_4);
		return panel_4;
	}

	protected String fixStatesString(String originalSiteName, String current) throws Throwable {
		current = current.trim();
		if(current.length() == 0) return current;
		if(current.startsWith("{")) {	current = current.substring(1);	}
		if(current.endsWith("}")) {	current = current.substring(0, current.length()-1);	}
		String finalList = new String();
		String from = new String();
		String to = new String();
		Vector<String> singleStates = new Vector<String>();
		
		 try{
			 
				  InputStream is = new ByteArrayInputStream(current.getBytes("UTF-8"));
				  MR_Expression_Parser_ReducedParserException parser = new MR_Expression_Parser_ReducedParserException(is,"UTF-8");
				  CompleteListOfExpression root = parser.CompleteListOfExpression();
				  ExtractElementsVisitor elementsVisitor = new ExtractElementsVisitor(MainGui.multiModel);
				  root.accept(elementsVisitor);
				  singleStates = elementsVisitor.getElements();
				  for(String element : singleStates) {
					  finalList += element.trim() + ",";
				  }
				  finalList = finalList.substring(0, finalList.length()-1);
			 } catch (Throwable e) {
				 //check if it is a range
				 if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 		 e.printStackTrace();
				  try {  	 
					  InputStream is = new ByteArrayInputStream(current.getBytes());
	    			 MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is);
	    			 CompleteMultistateSpecies_RangeString range = react.CompleteMultistateSpecies_RangeString();
	    			 MultistateSpeciesVisitor v = new MultistateSpeciesVisitor(MainGui.multiModel);
	    			 range.accept(v);
	    			
	    			 MutablePair<String, String> pair = v.getStringRangeLimits();
	    			 from = pair.left;
	    			 to = pair.right;
	    			 
	    		  	finalList = from.trim() +":"+ to.trim();
	 	    			
	    				 
	    				 
				  } catch (Throwable e2) {
					  if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)   e2.printStackTrace();
					  throw new Exception("Something wrong in the states format.");
				  }
			}
		 
		 try{
			 checkCompatibilityStates(originalSiteName, from,to, singleStates);
		 }  catch (Throwable e) {
			  if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)   e.printStackTrace();
			  throw e;
			  //throw new Exception("Something wrong in the states format.");
		  }
		 
		 finalList = "{"+finalList+"}";
	
		return finalList;
	}

	private void checkCompatibilityStates(String originalSpeciesSite, String from, String to,	Vector<String> singleStates) throws Exception {
		Vector sites = currentMultistateSpecies.getSiteStates_complete(originalSpeciesSite);
		
		if(currentMultistateSpecies.isRange(originalSpeciesSite)) {
			//original is a range
			if(from.length() > 0) { //it's a range specification
				
				Double original_from = null;
				try{
						original_from = Double.parseDouble(sites.get(0).toString());
				} catch (Exception ex) {
					try {
						original_from = new Double(CellParsers.evaluateExpression(sites.get(0).toString()));
					} catch (Throwable e) {
						new Exception("Problem parsing string: "+sites.get(0).toString());
					}
				}
				
				Double from_d = null;
						try{
							from_d = Double.parseDouble(from);
					} catch (Exception ex) {
						try {
							from_d = new Double(CellParsers.evaluateExpression(from));
						} catch (Throwable e) {
							new Exception("Problem parsing string: "+from);
						}
					}
				Double original_to = null;
						try{
							original_to = Double.parseDouble(sites.get(sites.size()-1).toString());
					} catch (Exception ex) {
						try {
							original_to = new Double(CellParsers.evaluateExpression(sites.get(sites.size()-1).toString()));
						} catch (Throwable e) {
							throw new Exception("Problem parsing string: "+sites.get(sites.size()-1).toString());
						}
					}
					
				Double to_d  = null;
				try{
					to_d = Double.parseDouble(to);
			} catch (Exception ex) {
				try {
					to_d = new Double(CellParsers.evaluateExpression(to));
				} catch (Throwable e) {
					new Exception("Problem parsing string: "+to);
				}
			}
				if(from_d < original_from || from_d >original_to || to_d > original_to || from_d > to_d) {
					 throw new Exception("Something wrong in the states format: indexes out of range");
				}
			} 	else { //the original can be a range, but the complex can specify a list
				 for(String single : singleStates) {
					 if(!sites.contains(single)) {
						 throw new Exception("Something wrong in the states format: indexes out of range");
					 }
				 }
			}
		}
		 else {//it's a list specification
			 if(singleStates.size() ==0) {
				 throw new Exception("Something wrong in the states format: cannot use a range if the original one is a list");
			 }
			 for(String single : singleStates) {
				 if(!sites.contains(single)) {
					 throw new Exception("Something wrong in the states format: indexes out of range");
				 }
			 }
		}
		
	}

	public void setMultistateSpecies(MultistateSpecies selectedValue, Vector names) {
		if(selectedValue == null) return;
		siteNameAlreadyUsed.clear();
		siteNameAlreadyUsed.addAll(names);
		renamed_sites.clear();
		try {
			currentMultistateSpecies = new MultistateSpecies(MainGui.multiModel, selectedValue.toString());
		} catch (Exception e) {
			//e.printStackTrace();
		}
		if(currentMultistateSpecies!= null) {
			String text = "The species you want to add has multistate sites.\r\n\n"+currentMultistateSpecies.toString()+"\r\n\r\nAdd the sites that you want to track in the complex.\r\nRemember that each site in the complex needs to have a different name \nand that any non-tracked state is lost once the complex is created.";
		
			text += "\n\nNames already used as sites:\n";
			for(String name : siteNameAlreadyUsed ) {
				text += name +"\n";
			}
			
			jTextPane_message.setText(text);
		}
		
	}
	
	protected MultistateSpecies getMultistateSpecies() {
		return currentMultistateSpecies;
	}

	public void setMultistateTracking(Vector<Vector<String>> trackedTriplets) {
		panel_2.removeAll();
		addTitleLabels();
		for(Vector<String> sites : trackedTriplets) {
			JPanel panel_new = createPanelNewSite(sites.get(0),sites.get(1),sites.get(2));
			if(totSites>4) {
				panel_2.setLayout(new GridLayout(++totSites, 1, 0, 3));
			} else {++totSites;}
			panel_2.add(panel_new);
			
		}
		//revalidate();
		
	}



	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {
		if(exitOption!= ExitOption.OK) exitOption = ExitOption.CANCEL;
	}


	@Override
	public void windowClosing(WindowEvent e) {	}



	@Override
	public void windowDeactivated(WindowEvent e) {}



	@Override
	public void windowDeiconified(WindowEvent e) {}


	@Override
	public void windowIconified(WindowEvent e) {	}


	@Override
	public void windowOpened(WindowEvent e) {	}



	HashBiMap<String, String> renamed_sites = HashBiMap.create();
	
	public void renameSite(String before, String current) {
		if(before.length() == 0) return;
		if(renamed_sites.inverse().containsKey(before)) {
			before = renamed_sites.inverse().get(before);
		}
		renamed_sites.put(before, current);
	}

	

}

class ItemChangeListener_withTextField implements ItemListener{
	JTextField values = null;
	ComplexBuilderFrame_MultistateAdd parent = null;
	
	public ItemChangeListener_withTextField(JTextField siteValues, ComplexBuilderFrame_MultistateAdd parent_dialog) {
		values = siteValues;
		parent = parent_dialog;
	}
	
    @Override
	 public void itemStateChanged(ItemEvent event) {
	       if (event.getStateChange() == ItemEvent.SELECTED) {
	          String item = event.getItem().toString();
	            if(item.compareTo(parent.selectOneString)==0) {
	            	values.setText("");
	            } /*else {
	            	//listener that add all the states by default -- deleted
		        	MultistateSpecies m = parent.getMultistateSpecies();
	            	String siteString = m.printSite(item);
	            	siteString = siteString.substring(item.length()+1, siteString.length()-1);
	              	try {
	              		siteString = parent.fixStatesString(item, siteString);
	      	          	values.setBackground(Color.WHITE);
					} catch (Exception ex) {
						//something wrong in the format, color textfield to show the problem
						JOptionPane.showMessageDialog(null, ex.getMessage(), "Error in states",JOptionPane.ERROR_MESSAGE);

						values.setBackground(GraphicalProperties.color_cell_with_errors);
					}
					
	            	values.setText(siteString);
	            }*/
	       }
	    }       
	
}



class FocusListener_withComboBox implements FocusListener{
	JComboBox site = null;
	ComplexBuilderFrame_MultistateAdd parent = null;
	
	public FocusListener_withComboBox(JComboBox siteNames, ComplexBuilderFrame_MultistateAdd parent_dialog) {
		site = siteNames;
		parent = parent_dialog;
	}
	
	@Override
	public void focusLost(FocusEvent event) {
		JTextField textField = (JTextField)event.getComponent();
	    String current = textField.getText();
		try {
			if(site.getSelectedItem().toString().compareTo(ComplexBuilderFrame_MultistateAdd.selectOneString)==0) {
				throw new Exception("To validate the states you must select a site!");
			}	
			current = parent.fixStatesString(site.getSelectedItem().toString(), current);
			
			textField.setBackground(Color.WHITE);
		} catch (Throwable ex) {
			//something wrong in the format, color textfield to show the problem
			JOptionPane.showMessageDialog(null, ex.getMessage(), "Error in states",JOptionPane.ERROR_MESSAGE);

			textField.setBackground(GraphicalProperties.color_cell_with_errors);
		}
		
		textField.setText(current);
	}

	@Override
	public void focusGained(FocusEvent e) {
		
	}
}

class FocusListener_Renaming implements FocusListener{
	ComplexBuilderFrame_MultistateAdd parent = null;
	String before = null;
	
	public FocusListener_Renaming(ComplexBuilderFrame_MultistateAdd parent_dialog) {
		parent = parent_dialog;
	}
	
	@Override
	public void focusLost(FocusEvent event) {
		JTextField textField = (JTextField)event.getComponent();
	    String current = textField.getText();
	    if(before != null && before.compareTo(current) != 0) {
	    	parent.renameSite(before, current);
	    }
	}

	@Override
	public void focusGained(FocusEvent e) {
		JTextField textField = (JTextField)e.getComponent();
	    before = textField.getText();
	}
}


