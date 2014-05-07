package msmb.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.collect.HashBiMap;

import msmb.model.ComplexSpecies;
import msmb.model.MultistateSpecies;
import msmb.model.Species;
import msmb.parsers.multistateSpecies.MR_MultistateSpecies_Parser;
import msmb.parsers.multistateSpecies.syntaxtree.CompleteMultistateSpecies_Operator;
import msmb.parsers.multistateSpecies.visitor.MultistateSpeciesVisitor;
import msmb.utility.CellParsers;
import msmb.utility.Constants;
import msmb.utility.GraphicalProperties;
import msmb.utility.MySyntaxException;

import java.awt.FlowLayout;
import javax.swing.JCheckBox;

public class ComplexBuilderFrame extends JDialog {

	private JPanel contentPane;
	private JTextField textField;
	private JList listMultistateSpecies; 
	private JList listRegularSpecies;
	protected ComplexBuilderFrame_MultistateAdd multistateAdd_frame;
	protected ComplexBuilderFrame_Initials complexInitials_frame;
	private DefaultListModel listMultistateSpeciesModel;
	private JSplitPane splitPane;
	private DynamicTree treePanel;

	protected ComplexSpecies complexSpecies = null; //new ComplexSpecies("NO_NAME");
	protected HashMap<String,String> trackableMultistate = new HashMap<String,String>();
	private DefaultListModel listRegularSpeciesModel;
	protected ExitOption exitOption;
	private JLabel labelName;
	private JCheckBox checkCustomName;
	private JCheckBox checkboxAddReactions;
	private JCheckBox checkboxAddComplexSpecies;
	//private JCheckBox checkboxLinkReactions;
	private JSplitPane splitPane_global;

	HashBiMap<String, String> renamed_sites = HashBiMap.create();
	protected Integer indexCurrentComplex;
	static boolean fromChangeMenu = false;
	
	public static void main(String[] args) {
			  EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					  UIManager.setLookAndFeel(
					            UIManager.getSystemLookAndFeelClassName());
				
					ComplexBuilderFrame frame = new ComplexBuilderFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	

	MutablePair<ComplexSpecies, HashMap<String, String>> showDialog() {
		exitOption = ExitOption.CANCEL;
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	    
		if(exitOption == ExitOption.CANCEL) return null;
		
		if(complexSpecies.getComponents().size() ==0 && complexSpecies.getComponents_multi().size() ==0  ) {
	    	return null;
	    }
	    if(checkCustomName.isSelected() && textField.getText().length() > 0) {
	    	complexSpecies.setCustomFullName(textField.getText());
	    } else {
	    	complexSpecies.setCustomFullName(new String());
	    	complexSpecies.setPrefixName(textField.getText());
	    }
	    
	    if(checkboxAddReactions.isSelected())  {
	    	complexSpecies.returnOnlySpecies(false);
	     } else {
	    	 complexSpecies.returnOnlySpecies(true);
	     }
	    
	 /*   if(checkboxLinkReactions.isSelected()) {
	    	complexSpecies.setLinkReactions(true);
	    } else {
	    	 complexSpecies.setLinkReactions(false);
	    }*/
	    
	    try {
			if(complexSpecies.getCompartment_listString().trim().length() ==0) complexSpecies.setCompartment(MainGui.multiModel, MainGui.compartment_default_for_dialog_window);
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	    MutablePair<ComplexSpecies,  HashMap<String, String>> ret = new MutablePair<ComplexSpecies,  HashMap<String, String>>();
	    ret.left = complexSpecies;
	    HashMap<String, String> fromTo = new HashMap<String, String>();
	    fromTo.putAll(renamed_sites);
	    ret.right = fromTo;
	    
	    return ret;
	}
	
	
	public ComplexBuilderFrame() throws Exception {
		setTitle("Complex Builder");
		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(3, 3));
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(3, 3));
		TitledBorder titlePanel = BorderFactory.createTitledBorder("Complex name");
		panel.setBorder(titlePanel);
		
		labelName = new JLabel("Prefix:");
		panel.add(labelName, BorderLayout.WEST);
		
		textField = new JTextField();
		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				if(checkCustomName.isSelected()) {
					complexSpecies.setCustomFullName(textField.getText());
				} else {
					complexSpecies.setPrefixName(textField.getText());
				}
			}
		});
		
		JPanel panel2 = new JPanel();
		panel2.setLayout(new BorderLayout(3, 3));
		JTextPane lblName2 = new JTextPane();
		lblName2.setEditable(false);
		lblName2.setOpaque(false);
		lblName2.setText("By default, the name will be composed of the prefix and the list of all the components of the complex. \r\nIf, instead, you want to give a custom name, select the checkbox below");
		panel.add(panel2, BorderLayout.SOUTH);
		panel2.add(lblName2, BorderLayout.NORTH);
		checkCustomName = new JCheckBox("Full custom name");
		panel2.add(checkCustomName, BorderLayout.CENTER);
		checkCustomName.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					labelName.setText("Prefix: ");
		        } else {
		        	labelName.setText("Full custom name: ");
		        }
			}
		});
			
		
		
		panel.add(textField);
		textField.setColumns(10);
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(3, 0));
		
		JPanel panel_5 = new JPanel();
		panel_1.add(panel_5, BorderLayout.SOUTH);
		multistateAdd_frame = new ComplexBuilderFrame_MultistateAdd(this);
		complexInitials_frame = new ComplexBuilderFrame_Initials();
		complexInitials_frame.setModal(true);
		
		
		
		JButton btnAddToCurrent = new JButton("Add selected to complex definition");
		btnAddToCurrent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if(listRegularSpecies.getSelectedValues().length > 0) {
					String[] regularSpecies = (String[]) listRegularSpecies.getSelectedValues();
					complexSpecies.addAll_regularSpecies(Arrays.asList(regularSpecies));
					for(String e : regularSpecies) {
					  treePanel.addObject(null, e);
					}
				} else {
					/*Vector<String> names = complexSpecies.getSiteNamesUsed();
					Vector<String> namesWithRenaming = new Vector<String>();
					for(String name : names) {
						if(renamed_sites.containsKey(name)) namesWithRenaming.add(renamed_sites.get(name));
						else namesWithRenaming.add(name);
					}*/
					MultistateSpecies selectedMulti = (MultistateSpecies) listMultistateSpecies.getSelectedValue();
					
					if(complexSpecies.getComponents_multi()!= null && complexSpecies.getComponents_multi().containsKey(selectedMulti.getSpeciesName())) {
						int reply = JOptionPane.showConfirmDialog(null, "Species "+selectedMulti.getSpeciesName()+" is already in the Complex.\nIf you include it again, Aliases will be introduced.\nDo you want to proceed?", "Warning!", JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
				        if (reply == JOptionPane.YES_OPTION) {
				        	multistateAdd_frame.setMultistateSpecies(selectedMulti, complexSpecies.getSiteNamesUsed() );
							multistateAdd_frame.setVisible(true);
						//	do all these things only if the user then choose ok, because if s/he selects cancel then I have to undo those changes :)
							//CHANGE THE MULTISTATE IN THE TREE WITH THE ALIAS=NAME, REFRESH TREE
						/*	if(multistateAdd_frame.exitOption!=ExitOption.CANCEL) {
								complexSpecies.getNextAlias(alreadyTrackedMulti);
							}*/
				        }
				        else {
				        	return;
				        }
					} else {
			        	multistateAdd_frame.setMultistateSpecies(selectedMulti, complexSpecies.getSiteNamesUsed() );
								multistateAdd_frame.setVisible(true);
			
					}
				
					
				}
				listMultistateSpecies.clearSelection();
				listRegularSpecies.clearSelection();
				//resetMSMBcompliantString();
				
			}
		});
		panel_5.add(btnAddToCurrent);
		
		JButton btnDeleteAllFrom = new JButton("Delete all from complex definition");
		panel_5.add(btnDeleteAllFrom);
		btnDeleteAllFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				complexSpecies.clearComponents();
				listMultistateSpecies.clearSelection();
				listRegularSpecies.clearSelection();
				treePanel.clear();
				//resetMSMBcompliantString();
			}
		});
		
		
		listMultistateSpeciesModel = new DefaultListModel();
		
		
	/*	Vector<MultistateSpecies> multistateComponents = new Vector<MultistateSpecies>();
		multistateComponents.add(new MultistateSpecies(null, "MultiA(p{0:10};q{free,bound})"));
		multistateComponents.add(new MultistateSpecies(null, "MultiB(p{0:3};q{free,bound,hidden})"));
		multistateComponents.add(new MultistateSpecies(null, "MultiC(p{0:3})"));
		 
		for(MultistateSpecies element : multistateComponents) {
			listMultistateSpeciesModel.addElement(element);
			 trackableMultistate.put(element.getSpeciesName(), element.getDisplayedName());
		}*/
		
		JPanel panel_6 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_6.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_1.add(panel_6, BorderLayout.NORTH);
		
		JLabel lblRegularSpeciesleft = new JLabel("Regular species (upper left) - Multistate species (lower left) - Parts (right)");
		lblRegularSpeciesleft.setHorizontalAlignment(SwingConstants.LEFT);
		panel_6.add(lblRegularSpeciesleft);
		
		splitPane_global = new JSplitPane();
		panel_1.add(splitPane_global, BorderLayout.CENTER);
		

		treePanel = new DynamicTree(this);
	    
		splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.5);
		splitPane_global.setLeftComponent(splitPane);
		splitPane_global.setRightComponent(treePanel);
		splitPane_global.setResizeWeight(0.5);
		
		 splitPane_global.setPreferredSize(new Dimension(100,200));
		 
		
		
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);
		
		listRegularSpeciesModel = new DefaultListModel();
		
	/*	Vector<String> regularComponents = new Vector<String>();
		regularComponents.add(("A"));
		regularComponents.add(("B"));
		regularComponents.add(("C"));
		regularComponents.add(("eeeeeeeeeeeeeeeeeeeeeeee"));
		 
		for(String element : regularComponents) {
			listRegularSpeciesModel.addElement(element);
		}*/
		
		listRegularSpecies = new JList(listRegularSpeciesModel);
		
		listRegularSpecies.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				listMultistateSpecies.clearSelection();
			}
		});
		
		scrollPane.setViewportView(listRegularSpecies);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		splitPane.setRightComponent(scrollPane_1);
		
				listMultistateSpecies = new JList(listMultistateSpeciesModel);
				listMultistateSpecies.addFocusListener(new FocusAdapter() {
					@Override
					public void focusGained(FocusEvent e) {
						listRegularSpecies.clearSelection();
					}
				});
				
				listMultistateSpecies.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				
					scrollPane_1.setViewportView(listMultistateSpecies);
		
		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.SOUTH);
		panel_2.setLayout(new BorderLayout(3, 0));
		
		JPanel panel_32 = new JPanel();
		panel_32.setLayout(new BorderLayout(3, 0));
				
		JPanel panel_3 = new JPanel();
		panel_2.add(panel_32, BorderLayout.CENTER);
		panel_32.add(panel_3, BorderLayout.CENTER);
		
		//panel_3.setLayout(new GridLayout(3, 0,0,0));
		panel_3.setLayout(new GridLayout(2, 0,0,0));
		
		checkboxAddComplexSpecies = new JCheckBox("Add complex to Species");
		panel_3.add(checkboxAddComplexSpecies);
		checkboxAddComplexSpecies.setSelected(true);
		checkboxAddComplexSpecies.setEnabled(false);
		checkboxAddReactions = new JCheckBox("Add complexation/decomplexation Reaction");
		checkboxAddReactions.setSelected(true);
		panel_3.add(checkboxAddReactions);
		
		/*checkboxLinkReactions = new JCheckBox("Link Reactions to Complex updates");
		panel_3.add(checkboxLinkReactions);
			checkboxAddReactions.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					checkboxLinkReactions.setSelected(false);
		        } 
			}
		});
		*/
		
		JPanel panel_4 = new JPanel();
		panel_2.add(panel_4,  BorderLayout.SOUTH);
		
		JButton button = new JButton("Set initial quantities");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				complexInitials_frame.setComplexSpecies(complexSpecies);
				ComplexSpecies complex = complexInitials_frame.showDialog();
				complexSpecies.setInitialQuantity(complex.getInitialQuantity_multi());
			}
		});
		panel_4.add(button);
		
		JButton btnNewButton_1 = new JButton("Update model");
		btnNewButton_1.addActionListener(new ActionListener() {
		
	
			public void actionPerformed(ActionEvent e) {
				if(MainGui.multiModel.speciesDB.getSpeciesIndex(complexSpecies.getSpeciesName())!=null && MainGui.multiModel.speciesDB.getSpeciesIndex(complexSpecies.getSpeciesName()) !=
						indexCurrentComplex)		{
					JOptionPane.showMessageDialog(null, "A Species with name "+complexSpecies.getSpeciesName()+ " already exists.\n Please provide a different name for the complex.", "Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				exitOption = ExitOption.OK;
				dispose();
			}
		});
		panel_4.add(btnNewButton_1);
		
		JButton btnNewButton_2 = new JButton("Cancel");
		panel_4.add(btnNewButton_2);
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cancel();
			}
		});
		
		GraphicalProperties.resetFonts(this);
	}


	protected void cancel() {
		exitOption = ExitOption.CANCEL;
		dispose();
	}



	@Override
	public void setVisible(boolean b) {
		GraphicalProperties.resetFonts(this);
		pack();
		splitPane_global.setDividerLocation(0.5);
		setLocationRelativeTo(null);
		super.setVisible(true);
	}
	
	
	
	public void addComponents(Vector<Species> modelSpecies) {
		listRegularSpeciesModel.clear();
		listMultistateSpeciesModel.clear();
		trackableMultistate.clear();
		treePanel.clear();
		
		Vector<String> simpleSpecies = new Vector<String>();
		Vector<String> multistate = new Vector<String>();
		for(Species sp : modelSpecies) {
			if(sp==null) continue;
			String spName = sp.getDisplayedName();
			if(sp instanceof MultistateSpecies) {
				multistate.add(spName);
			} else {
				simpleSpecies.add(spName);
			}
		}
		
		Collections.sort(simpleSpecies);
		Collections.sort(multistate);
		
		for(String spName : multistate) {
			MultistateSpecies sp = null;
			try {
				sp = new MultistateSpecies(MainGui.multiModel, spName);
			} catch (Exception e) {
			
				e.printStackTrace();
			}
				listMultistateSpeciesModel.addElement(sp);
		   	   trackableMultistate.put(sp.getSpeciesName(), sp.getDisplayedName());
		}
		for(String sp : simpleSpecies) {
			listRegularSpeciesModel.addElement(sp);
		}
	
		listMultistateSpecies.revalidate();
		listRegularSpecies.revalidate();
	}
	
	public void applyRenamingSites(HashBiMap<String, String> renamed) {
		Set<String> from = renamed.keySet();
		for(String before : from) {
			String before_inComplex = before;
			if(renamed_sites.inverse().containsKey(before)) {
				before_inComplex = renamed_sites.inverse().get(before);
			}
			renamed_sites.put(before_inComplex, renamed.get(before));
		}
	}
	
	public void addElementsToComplex(Vector elementsToAdd) {
		if(((Vector)elementsToAdd.get(0)).size() == 0) return;
		
		HashBiMap<String, String> renamingOfFromMultistate = HashBiMap.create();
		renamingOfFromMultistate.putAll((HashBiMap<String, String>) elementsToAdd.get(elementsToAdd.size()-1));
		applyRenamingSites(renamingOfFromMultistate);
		
		elementsToAdd.removeElementAt(elementsToAdd.size()-1);
		String componentToAdd = complexSpecies.addMultistateElementsToComplex(elementsToAdd);
		if(!fromChangeMenu ) {
			treePanel.addObject(null, componentToAdd);
			 listMultistateSpecies.clearSelection();
			//deleteFromList(componentToAdd);
		} else {
			treePanel.changeObject(componentToAdd);
		}
		complexSpecies.setInitialQuantity(new HashMap<String,String>());
		resetTree();
	}



	private void deleteFromList(String componentToDelete) {
		  try {
				int indexToDelete = -1;
				String name = CellParsers.extractMultistateName(componentToDelete);
				for(int i = 0; i < listMultistateSpeciesModel.getSize(); i++) {
					MultistateSpecies element = (MultistateSpecies) listMultistateSpeciesModel.get(i);
					if(element.getSpeciesName().compareTo(name)==0) {
						indexToDelete = i;
						break;
					}
				}
				if(indexToDelete != -1) listMultistateSpeciesModel.remove(indexToDelete);
				listMultistateSpecies.revalidate();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}



	public void removeComponentFromComplex(String removedComponent) {
		complexSpecies.removeComponentFromComplex(removedComponent);
		String multistateNotTracked = trackableMultistate.get(CellParsers.extractMultistateName(removedComponent));
		try {
			if(multistateNotTracked!=null) listMultistateSpeciesModel.addElement(new MultistateSpecies(MainGui.multiModel, multistateNotTracked));
		} catch (Exception e) {
			e.printStackTrace();
		}
		listMultistateSpecies.clearSelection();
	}

	public void clearReferencesTo(String multistateDefinition) {
		MultistateSpecies m;
		try {
			m = new MultistateSpecies(MainGui.multiModel, multistateDefinition);
			complexSpecies.removeComponentFromComplex(m.getDisplayedName());
		} catch (Exception e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
			//this function can be used also to check with Cdh1(p) tracked... so I have to analyze name without complete site states.
			 InputStream is;
			try {
				is = new ByteArrayInputStream(multistateDefinition.getBytes("UTF-8"));
				 MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is,"UTF-8");
				 CompleteMultistateSpecies_Operator start = react.CompleteMultistateSpecies_Operator();
				 MultistateSpeciesVisitor v = new MultistateSpeciesVisitor(MainGui.multiModel);
				 start.accept(v);
				 String justName = v.getSpeciesName();
				 complexSpecies.removeComponentFromComplex(justName);
			} catch (Exception e1) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e1.printStackTrace();
			}
		}
	}

	private void resetTree() {
		treePanel.clearOnlyTree();
		for(String e : complexSpecies.getComponents()) {
		  treePanel.addObject(null, e);
		}
		
		Set<String> keys = complexSpecies.getComponents_multi().keySet();
		for(String k : keys) {
			String element = new String();
			element += k +"(";
			Vector<MutablePair<String, String>> sitesList = complexSpecies.getComponents_multi().get(k);
			for(MutablePair<String, String> pair : sitesList){
				element += pair.left;
					if(pair.right.length() > 0) {
						String siteConf = pair.right;
						if(!siteConf.startsWith("{")) {	siteConf = "{"+siteConf;	}
						if(!siteConf.endsWith("}")) {	siteConf += "}";	}
						element += siteConf;
					}
					element +=";";
			}
			element = element.substring(0, element.length()-1);
			element +=")";
			  treePanel.addObject(null, element);
			
		}
	}


	public void setComplexSpecies(ComplexSpecies originalComplex, int index) {
		try {
			complexSpecies = new ComplexSpecies(MainGui.multiModel,originalComplex);
			renamed_sites.clear();
			indexCurrentComplex = index; 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(complexSpecies.getCustomFullName().length() > 0) {
			textField.setText(originalComplex.getCustomFullName());
			checkCustomName.setSelected(true);
		} else {
			textField.setText(originalComplex.getPrefixName());
			checkCustomName.setSelected(false);
		}
		
		if(complexSpecies.getSpeciesName().compareTo("NO_NAME")!=0)  {
			MutablePair<Integer, Integer> linked = complexSpecies.getLinkedReactionIndexes();
			if(linked.left != -1 || linked.right != -1){
				checkboxAddReactions.setEnabled(false);
				checkboxAddReactions.setSelected(true);
				checkboxAddReactions.setText("Add complexation/decomplexation Reaction (already in the model)");
			} else {
				checkboxAddReactions.setText("Add complexation/decomplexation Reaction");
				checkboxAddReactions.setEnabled(true);
			}
		}else {
			checkboxAddReactions.setText("Add complexation/decomplexation Reaction");
			checkboxAddReactions.setEnabled(true);
		}
		treePanel.clearOnlyTree();
		for(String e : complexSpecies.getComponents()) {
		  treePanel.addObject(null, e);
		}
		
		Set<String> keys = complexSpecies.getComponents_multi().keySet();
		for(String k : keys) {
			String element = new String();
			element += k +"(";
			Vector<MutablePair<String, String>> sitesList = complexSpecies.getComponents_multi().get(k);
			for(MutablePair<String, String> pair : sitesList){
				element += pair.left;
					if(pair.right.length() > 0) {
						String siteConf = pair.right;
						if(!siteConf.startsWith("{")) {	siteConf = "{"+siteConf;	}
						if(!siteConf.endsWith("}")) {	siteConf += "}";	}
						element += siteConf;
					}
					element +=";";
			}
			element = element.substring(0, element.length()-1);
			element +=")";
			  treePanel.addObject(null, element);
			
		}
		
		trackableMultistate.remove(complexSpecies.getSpeciesName());
		listMultistateSpeciesModel.clear();
		Vector<String> sorted = new Vector<String>();
		sorted.addAll(trackableMultistate.keySet());
		Collections.sort(sorted);
		
		for(String element : sorted) {
			try {
					listMultistateSpeciesModel.addElement(new MultistateSpecies(MainGui.multiModel, trackableMultistate.get(element)));
			} catch (Exception e1) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)e1.printStackTrace();
			}
		}
		
		//revalidate();
	}



	public void clearAll() {
		checkboxAddReactions.setText("Add complexation/decomplexation Reaction");
		checkboxAddReactions.setEnabled(true);
		textField.setText("");
		try {
			complexSpecies = new ComplexSpecies(MainGui.multiModel,"NO_NAME");
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}



	public ComplexSpecies getCurrentComplex() {
		try {
			return new ComplexSpecies(MainGui.multiModel,complexSpecies);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;
	}

	
	
}


class DynamicTree extends JPanel {
	  protected DefaultMutableTreeNode rootNode;
	  protected DefaultTreeModel treeModel;
	  protected JTree tree;
	private JPopupMenu popup;
	private JMenuItem deleteMenu;
	private JMenuItem changeMultistate;
	private JMenuItem changeAlias;
	ComplexBuilderFrame parentDialog;
	

	  public void expandAll() {
	    int row = 0;
	    while (row < tree.getRowCount()) {
	      tree.expandRow(row);
	      row++;
	      }
	    }
	  
	  
	  
	  public DynamicTree(ComplexBuilderFrame parent) {
	    super(new GridLayout(1, 0));
	    parentDialog = parent;

	    rootNode = new DefaultMutableTreeNode("Components");
	    treeModel = new DefaultTreeModel(rootNode);
	 
	    tree = new JTree(treeModel);
	    tree.setEditable(false);
	    tree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION);
	    tree.setShowsRootHandles(true);
	   tree.setCellRenderer(new RendererForErrors(parentDialog));
	   
	    createPopupMenu();
	    
	    // add MouseListener to tree
			MouseAdapter ma = new MouseAdapter() {
				private void myPopupEvent(MouseEvent e) {
					int x = e.getX();
					int y = e.getY();
					JTree tree = (JTree)e.getSource();
					TreePath path = tree.getPathForLocation(x, y);
					if (path == null)
						return;	
					tree.setSelectionPath(path);
					DefaultMutableTreeNode obj = (DefaultMutableTreeNode)path.getLastPathComponent();
					String stringObj = obj.toString();
					MutablePair<String, String> alias = CellParsers.extractAlias(stringObj);
					
					if(alias != null && alias.left!= null && alias.left.length() > 0) {
						changeAlias.setText("Change alias "+alias.left);
						 popup.add(changeAlias);
					} else {
						if(popup.getComponentCount() > 1) popup.remove(1);
					} 
					
					
					if(CellParsers.isMultistateSpeciesName(alias.right)) {
						changeMultistate.setText("Change "+alias.right+" tracking");
						 popup.add(changeMultistate);
					 } else {
						 if(popup.getComponentCount() > 1) popup.remove(1);
					 }
				
					popup.show(tree, x, y);
				}
				public void mousePressed(MouseEvent e) {
					if (e.isPopupTrigger()) myPopupEvent(e);
				}
				public void mouseReleased(MouseEvent e) {
					if (e.isPopupTrigger()) myPopupEvent(e);
				}
			};
			
			tree.addMouseListener(ma);

	    JScrollPane scrollPane = new JScrollPane(tree);
	    add(scrollPane);
	  }

	  private void createPopupMenu() {
		
			popup = new JPopupMenu();
			
			deleteMenu = new JMenuItem("Delete");
			deleteMenu.addActionListener(new ActionListener() {
				 public void actionPerformed(ActionEvent arg0) {
					String removedComponent = removeCurrentNode();
					if(removedComponent != null) 	removeComponentFromComplex(removedComponent);
				 }
			});
			
			popup.add(deleteMenu);

			changeMultistate = new JMenuItem("Change multistate tracking");
			changeMultistate.addActionListener(new ActionListener() {
				 public void actionPerformed(ActionEvent arg0) {
					 String prefix = "Change ";
					 String postfix = " tracking";
					 String label = changeMultistate.getText();
					 String multistateChosen = label.substring(prefix.length(), label.length()-postfix.length());
		
					 String multistateTracked = parentDialog.trackableMultistate.get(CellParsers.extractMultistateName(multistateChosen));
					try {
						Vector<Vector<String>> trackedTriplets = parentDialog.complexSpecies.getTrackedTriplets(CellParsers.extractMultistateName(multistateChosen));
						parentDialog.fromChangeMenu = true;
						ComplexSpecies old = parentDialog.getCurrentComplex();
						parentDialog.clearReferencesTo(multistateTracked);
						parentDialog.multistateAdd_frame.setMultistateSpecies(new MultistateSpecies(MainGui.multiModel, multistateTracked), parentDialog.complexSpecies.getSiteNamesUsed());
						parentDialog.multistateAdd_frame.setMultistateTracking(trackedTriplets);
						parentDialog.multistateAdd_frame.setVisible(true);
						
						if(parentDialog.multistateAdd_frame.exitOption == ExitOption.CANCEL) {
							parentDialog.setComplexSpecies(old,-1);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally{
						parentDialog.fromChangeMenu = false;
						tree.updateUI();
					}
				 }

			});
			
			
			popup.add(changeMultistate);
			
			
			changeAlias = new JMenuItem("Change alias");
			changeAlias.addActionListener(new ActionListener() {
				 public void actionPerformed(ActionEvent arg0) {
					 String prefix = "Change ";
					 String postfix = " alias";
					 String label = changeMultistate.getText();
					 System.out.println("TO BE COMPLETED");
					/* String multistateChosen = label.substring(prefix.length(), label.length()-postfix.length());
		
					 String multistateTracked = parentDialog.trackableMultistate.get(CellParsers.extractMultistateName(multistateChosen));
					try {
						Vector<Vector<String>> trackedTriplets = parentDialog.complexSpecies.getTrackedTriplets(CellParsers.extractMultistateName(multistateChosen));
						parentDialog.fromChangeMenu = true;
						ComplexSpecies old = parentDialog.getCurrentComplex();
						parentDialog.clearReferencesTo(multistateTracked);
						parentDialog.multistateAdd_frame.setMultistateSpecies(new MultistateSpecies(null, multistateTracked), parentDialog.complexSpecies.getSiteNamesUsed());
						parentDialog.multistateAdd_frame.setMultistateTracking(trackedTriplets);
						parentDialog.multistateAdd_frame.setVisible(true);
						
						if(parentDialog.multistateAdd_frame.exitOption == ExitOption.CANCEL) {
							parentDialog.setComplexSpecies(old);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally{
						parentDialog.fromChangeMenu = false;
						tree.updateUI();
					}
					*/
				 }

			});
			
			
			popup.add(changeMultistate);
			
		
	}



	protected void removeComponentFromComplex(String removedComponent) {
		parentDialog.removeComponentFromComplex(removedComponent);
	}



	/** Remove all nodes except the root node. */
	  public void clear() {
		  int children = rootNode.getChildCount();
		  for(int i = 0; i < children; i++) {
			  tree.setSelectionRow(1);
			  String removedComponent = removeCurrentNode();
			removeComponentFromComplex(removedComponent);
			treeModel.reload();
		  }
	   
		
	  }
	  
	  public void clearOnlyTree() {
		  int children = rootNode.getChildCount();
		  for(int i = 0; i < children; i++) {
			  tree.setSelectionRow(1);
			  String removedComponent = removeCurrentNode();
			  treeModel.reload();
		  }
	   
		
	  }

	  // Remove the currently selected node.
	  //  Returns the removed node string so that the complex can clear all the data connected to that
	  public String removeCurrentNode() {
	    TreePath currentSelection = tree.getSelectionPath();
	    if (currentSelection != null) {
	      DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection
	          .getLastPathComponent());
	      MutableTreeNode parent = (MutableTreeNode) (currentNode.getParent());
	      if (parent != null) {
	        treeModel.removeNodeFromParent(currentNode);
	        return currentNode.toString();
	      }
	    }
		return null;
	  }

	  /** Add child to the currently selected node. */
	  public void changeObject(Object child) {
	   TreePath path = tree.getSelectionPath();
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) (path.getLastPathComponent());
	    node.setUserObject(child);
	  }
	  
	  /** Add child to the currently selected node. */
	  public DefaultMutableTreeNode addObject(Object child) {
	    DefaultMutableTreeNode parentNode = null;
	    TreePath parentPath = tree.getSelectionPath();
	    if (parentPath == null) {
	      parentNode = rootNode;
	    } else {
	      parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
	    }
	    return addObject(parentNode, child, true);
	  }

	  public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
	      Object child) {
	    return addObject(parent, child, false);
	  }

	  public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
	      Object child, boolean shouldBeVisible) {
	    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);

	    if (parent == null) {
	      parent = rootNode;
	    }

	    treeModel.insertNodeInto(childNode, parent, parent.getChildCount());

	   if (shouldBeVisible) {
	      tree.scrollPathToVisible(new TreePath(childNode.getPath()));
	    }
	    expandAll();
	    return childNode;
	  }
}
	  

class RendererForErrors extends DefaultTreeCellRenderer {
  
	private ComplexBuilderFrame parent;

	public RendererForErrors(ComplexBuilderFrame parentDialog) {
		parent = parentDialog;
	}
	
	
    public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {

        super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus);
        
        if (leaf && isCellWithError(value)) {
        	Border compound = null;
			Border redline = BorderFactory.createLineBorder(GraphicalProperties.color_cell_with_errors,2);
			compound = BorderFactory.createCompoundBorder(redline, compound);
			setBorder(compound);
	    }  else {
            setBorder(null);
        }
        

        return this;
    }

    protected boolean isCellWithError(Object value) {
    	DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
    	if(node.getParent() == null) return false;
    	try {
    		String multiName = CellParsers.extractMultistateName(value.toString());

    		HashMap<String, Vector<MutablePair<String, String>>> multi = parent.complexSpecies.getComponents_multi();

    		if(multi.containsKey(multiName)) {
    			String complete = parent.trackableMultistate.get(multiName);
    			MultistateSpecies ms = new MultistateSpecies(MainGui.multiModel, complete);
    			Vector<MutablePair<String, String>> element = multi.get(multiName);
    			String tempSpecies = "checkStates(";
    			boolean sitesToCheck = false;
    			for(MutablePair<String, String> sites : element) {
    				if(sites.right.length() ==0 ) {
    					if( ms.getSitesNames().contains(sites.left)) {
    						//implicit tracking all states of an existing sites, nothing to check on this site
    						continue;
    					} else {
    						//something wrong I can stop here
    						return true;
    					}
    				} else {
    					tempSpecies += sites.left + sites.right+";";
    					sitesToCheck = true;
    				}
    			}

    			if(!sitesToCheck) return false;

    			tempSpecies = tempSpecies.substring(0,tempSpecies.length()-1);
    			tempSpecies += ")";
    			MultistateSpecies checkStates = new MultistateSpecies(MainGui.multiModel, tempSpecies);
    			Set<String> sitesToCheck_set = checkStates.getSitesNames();
    			for(String site : sitesToCheck_set) {
    				Vector<String> allStates = checkStates.getSiteStates_complete(site);
    				for(String state : allStates) {
    					if(!(ms.getSiteStates_complete(site).contains(state))) { 
    						return true;
    					}
    				}
    			}
    		}
    	} catch (Exception e) {
    		return true;
    	}
    	return false;
    }
};

enum ExitOption {
	   CANCEL(0), OK(1);
	   			          
	   public final int code;
	   
	   ExitOption(int index) {
	              this.code = index;
	    }
	   
	  }

	 