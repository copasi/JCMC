package  msmb.gui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.commons.lang3.tuple.MutablePair;

import java.util.*;

import msmb.commonUtilities.tables.ScientificFormatCellRenderer;
import msmb.model.*;
import msmb.utility.*;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class MultistateBuilderFrame extends JDialog	 {

	private static final long serialVersionUID = 1L;
	private int marginSize = 6;
	protected static int row_to_highlight;
	private JPanel jContentPane = null;
	private JPanel upper = null;
	private JPanel jPanelConcentrationSpecies = null;
	private JScrollPane jScrollPaneConcentrationSpecies= null;
	private JPanel panel;
	
	private JLabel jLabel = null;
	private JTextField jTextField_species = null;
	private JLabel jLabel1 = null;
	private JTextField jTextField_newSite = null;
	private JComboBox spinner_lower = null;
	private JComboBox spinner_upper = null;
	private JTextField jTextField_listStates = null;
	private JButton jButton = null;
	private JScrollPane jScrollPane = null;
	private JLabel jLabel2 = null;
	private MultistateSpecies species; 
	private MainGui parentFrame;
	private JButton jButton1 = null;
	private JButton jButton2 = null;
	private JButton jButton3 = null;
	private CustomFocusTraversalPolicy newPolicy;
	private JTabbedPane jTabbedPane = null;
	private CustomTableModel_MSMB tableConcentrationSpeciesmodel= null;
	private CustomJTable_MSMB jTableConcentrationSpecies;
	private JList jListSite = null;
	private JLabel lblWarning;
	private JRadioButton jRadioBoolean;
	private HashMap<String, String> renamed_sites = new HashMap<String, String>();
	private RendererForErrorsJList rendererJListSite;
	
	private JPanel jPanelSitesDetails;
	private JPanel jPanelSitesDetails_site;
	private JPanel jPanelSitesDetails_listAndButton;
	private JPanel jPanelButtons;
	private JPanel jPanelSiteName;
	private JPanel jPanelStatesChoices_container;
	private JPanel jPanelStatesChoices;
	private JPanel jPanelChoiceRange;
	
	public MultistateBuilderFrame(MainGui owner) throws Exception {
		super(owner);
		initialize();
		parentFrame = owner;
		GraphicalProperties.resetFonts(this);
	}
	
	private void initialize() {
		renamed_sites.clear();
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setContentPane(getJContentPane());
		this.setTitle("Multistate Builder");
		this.setLocationRelativeTo(parentFrame);
		this.setResizable(true);
		
		Vector<Component> order = new Vector<Component>(7);
        order.add(jTextField_species);
        order.add(jTextField_newSite);
        order.add(spinner_lower);
        order.add(spinner_upper);
        
        order.add(jTextField_listStates);
        order.add(jButton);
        order.add(jButton3);
        newPolicy = new CustomFocusTraversalPolicy(order);
        this.setFocusTraversalPolicy(newPolicy);
                
         this.setTabbedPane_enable(false);
        
  }
	
	@Override
	public void setVisible(boolean b) {
		GraphicalProperties.resetFonts(this);
		pack();
		setLocationRelativeTo(null);
		try {
			if(species==null || species.getName().length()==0) {
				species = new MultistateSpecies(MainGui.multiModel,new String());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.setVisible(true);
	}
	
	public void setContentLowerUpperRange(Vector<String> existingGlqs) {
	
		Collections.sort(existingGlqs);
		for(String element : existingGlqs) {
			spinner_lower.addItem(element);
			spinner_upper.addItem(element);
		}
		spinner_lower.setSelectedItem("");
		spinner_upper.setSelectedItem("");
	}
	
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getUpper(), BorderLayout.NORTH);
			jContentPane.add(getJTabbedPane(), BorderLayout.CENTER);
			JPanel jpanelforalignment = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			jpanelforalignment.add(getJButton3());
			jContentPane.add(jpanelforalignment, BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	private JTabbedPane getJTabbedPane() {
	
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			jTabbedPane.addTab("Sites' details", null, this.getJPanelSpeciesDetails(), null);
			jTabbedPane.addTab(Constants.MultistateBuilder_QUANTITIES_description, null, getPanel(), null);
		}
		return jTabbedPane;
	}
	
	
	private JPanel getUpper() {
		if (upper == null) {
			upper = new JPanel();
			upper.setLayout(new BorderLayout(marginSize,marginSize));
			upper.setBorder( new EmptyBorder(marginSize, marginSize, marginSize, marginSize ) );//to add the space around
			jLabel = new JLabel();
			jLabel.setText("Species name: ");
			upper.add(jLabel, BorderLayout.WEST);
			upper.add(getJTextField_species(), BorderLayout.CENTER);
			//upper.add(getJButton3(), BorderLayout.EAST);
		}
		return upper;
	}
	
	private void setTabbedPane_enable(boolean b) {
		jTabbedPane.setEnabled(b);
		jPanelConcentrationSpecies.setEnabled(b);
		jTableConcentrationSpecies.setEnabled(b);
		this.jTextField_listStates.setEnabled(b);
		this.jTextField_newSite.setEnabled(b);
		this.spinner_lower.setEnabled(b);
		this.spinner_upper.setEnabled(b);
		this.jLabel1.setEnabled(b);
		this.jLabel2.setEnabled(b);
		lblWarning.setEnabled(b);
		jRadioBoolean.setEnabled(b);
		this.jListSite.setEnabled(b);
		this.jButton.setEnabled(b);
		this.jButton1.setEnabled(b);
		this.jButton2.setEnabled(b);
		this.jButton3.setEnabled(b);
		if(!b) jButton.setForeground(Color.lightGray);
		else jButton.setForeground(jButton1.getForeground());
	}
	
	private JTextField getJTextField_species() {
		if (jTextField_species == null) {
			jTextField_species = new JTextField();
			jTextField_species.setBounds(new Rectangle(86, 10, 125, 18));
			jTextField_species.addKeyListener(new KeyListener() {
			        public void keyTyped(KeyEvent keyEvent) {
			        	return;
			        }

					public void keyPressed(KeyEvent arg0) {
						return;
					}

					public void keyReleased(KeyEvent e) {
						String checkName = jTextField_species.getText();
			        	if(CellParsers.isKeyword(checkName)) {
			        		 JOptionPane.showMessageDialog(null,"The name "+checkName+" is a reserved word. Please chose a different Species name!", "Error", JOptionPane.ERROR_MESSAGE);
			      			setTabbedPane_enable(false); 
			        		return;
			        	}
			          if(checkName.length() > 0) {
			        	 setTabbedPane_enable(true);   	  
			          } else {
			        	  setTabbedPane_enable(false); 
			          }
					}
			      
			      });
		}
		return jTextField_species;
	}

	private JPanel getJPanelSpeciesDetails() {
		if (jPanelSitesDetails == null) {
			jPanelSitesDetails = new JPanel();
			jPanelSitesDetails.setLayout(new BorderLayout(marginSize,marginSize));
			jPanelSitesDetails.setBorder( new EmptyBorder(marginSize, marginSize, marginSize, marginSize ) );//to add the space around
			
			jPanelSitesDetails_site = new JPanel();
			jPanelSitesDetails_site.setLayout(new BorderLayout(marginSize,marginSize));
			jPanelSitesDetails_site.setBorder(BorderFactory.createTitledBorder(null, "Current site information:", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, UIManager.getFont("Label.font"), new Color(51, 51, 51)));
			
			
			jPanelSitesDetails_listAndButton = new JPanel();
			jPanelSitesDetails_listAndButton.setLayout(new BorderLayout(marginSize,marginSize));
			jPanelSitesDetails_listAndButton.setBorder(BorderFactory.createTitledBorder(null, "All sites:", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, UIManager.getFont("Label.font"), new Color(51, 51, 51)));
			
			jPanelSitesDetails.add(jPanelSitesDetails_site, BorderLayout.CENTER);
			jPanelSitesDetails.add(jPanelSitesDetails_listAndButton, BorderLayout.SOUTH);
			
			jPanelSitesDetails_listAndButton.add(getJScrollPaneJListSite(), BorderLayout.CENTER);
			jPanelButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
			jPanelButtons.add(getJButton1());
			jPanelButtons.add(getJButton2());
			jPanelSitesDetails_listAndButton.add(jPanelButtons,BorderLayout.SOUTH);
			
			
			lblWarning = new JLabel("<html><p ALIGN=\"LEFT\">WARNING: any change in the sites' definition will reset all the defined <p ALIGN=\"LEFT\">initial quantities to the default initial value of "+MainGui.species_defaultInitialValue+" </html>");
			lblWarning.setBorder( new EmptyBorder(marginSize, marginSize, marginSize, marginSize ) );
			jPanelSitesDetails_site.add(lblWarning, BorderLayout.SOUTH);
		
			jPanelSiteName = new JPanel(new BorderLayout(marginSize,marginSize));
			jPanelSitesDetails_site.add(jPanelSiteName, BorderLayout.NORTH);
			jLabel1 = new JLabel();
			jLabel1.setText("Site name: ");
			jPanelSiteName.setBorder( new EmptyBorder(marginSize, marginSize, marginSize, marginSize ) );
			jPanelSiteName.add(jLabel1, BorderLayout.WEST);
			jTextField_newSite = new JTextField();
			jPanelSiteName.add(jTextField_newSite, BorderLayout.CENTER);
			
			jPanelStatesChoices_container = new JPanel(new BorderLayout());
			jPanelStatesChoices_container.add(getJButtonAddChange(), BorderLayout.EAST);
			jPanelSitesDetails_site.add(jPanelStatesChoices_container, BorderLayout.CENTER);
			
			jPanelStatesChoices = new JPanel(new GridLayout(3, 1, 0, 0));
			jPanelStatesChoices.setPreferredSize(new Dimension(10,130));
			jPanelStatesChoices.setBorder(BorderFactory.createTitledBorder(null, "Alternative site's definition:", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, UIManager.getFont("Label.font"), new Color(51, 51, 51)));
			jPanelStatesChoices_container.add(jPanelStatesChoices, BorderLayout.CENTER);
			
			
			jPanelChoiceRange = new JPanel(new FlowLayout(FlowLayout.LEFT));
			jLabel2 = new JLabel();
			jLabel2.setText(" : ");
			jPanelChoiceRange.add(getSpinner_lower());
			jPanelChoiceRange.add(jLabel2);
			jPanelChoiceRange.add(getSpinner_upper());
			
		
			
			jPanelStatesChoices.add(jPanelChoiceRange);
			
			jRadioBoolean = new JRadioButton("boolean {TRUE, FALSE}c");
			jRadioBoolean.setBorder(BorderFactory.createEmptyBorder());
			jRadioBoolean.setEnabled(false);
			jRadioBoolean.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if(jRadioBoolean.isSelected()) {
						spinner_upper.setSelectedItem(""); spinner_upper.revalidate();
						spinner_lower.setSelectedItem(""); spinner_lower.revalidate();
						jTextField_listStates.setText(""); jTextField_listStates.revalidate();
						jRadioBoolean.setSelected(true);
					}
				}
			});
			
			jPanelStatesChoices.add(jRadioBoolean);
			JPanel jpanelforborder = new JPanel(new BorderLayout());
			jpanelforborder.setBorder( new EmptyBorder(4,4,4,4 ) );
			jpanelforborder.add(getJTextField_listStates(), BorderLayout.CENTER);
			jPanelStatesChoices.add(jpanelforborder);
			jPanelStatesChoices.add(jpanelforborder);
			jPanelStatesChoices.add(jpanelforborder);
			
			
	
			
		}
		return jPanelSitesDetails;
	}
	
	private JPanel getJPanelConcentrationSpecies() {
		if (jPanelConcentrationSpecies == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.gridx = 0;
			jPanelConcentrationSpecies = new JPanel();
			jPanelConcentrationSpecies.setLayout(new GridBagLayout());
			jPanelConcentrationSpecies.add(getJScrollPaneConcentrationSpecies(), gridBagConstraints);
		}
		return jPanelConcentrationSpecies;
	}
	
	private JScrollPane getJScrollPaneConcentrationSpecies() {
		if (jScrollPaneConcentrationSpecies == null) {
			jScrollPaneConcentrationSpecies = new JScrollPane();
			jScrollPaneConcentrationSpecies.setViewportView(getJTableConcentrationSpecies());
		}
		return jScrollPaneConcentrationSpecies;
	}
	
	private JTable getJTableConcentrationSpecies() {
		if (jTableConcentrationSpecies == null) {
			Vector<String> col = new Vector<String>();
			col.add("Expanded species");
				col.add("Initial Quantity");
			
						
			tableConcentrationSpeciesmodel = new CustomTableModel_MSMB(Constants.MultistateBuilder_QUANTITIES_description,col,new Vector(),this);
			jTableConcentrationSpecies = new CustomJTable_MSMB();
			jTableConcentrationSpecies.setModel(tableConcentrationSpeciesmodel);

			
			if(parentFrame!=null) jTableConcentrationSpecies.setCustomFont(parentFrame.getCustomFont());
	        
			jTableConcentrationSpecies.initializeCustomTable(tableConcentrationSpeciesmodel);
			
		}
		return jTableConcentrationSpecies;
	}
	
	private void updateJTableConcentrationSpecies(){
		
		Vector<String> col = new Vector<String>();
		if(!rendererJListSite.isEmptySiteWithProblem()) {
			tableConcentrationSpeciesmodel.clearData();
			jTableConcentrationSpecies.revalidate();
			jTableConcentrationSpecies.setEnabled(false);
			return;
		}
		
		Set<String> names= this.species.getSitesNames();

	    Iterator<String> iterator = names.iterator();  
	    while (iterator.hasNext()) {  
	    	col.add(iterator.next());
	    }  
	    
	    if(names.size() ==0) {
	    	col.add("Expanded species");
	    }
		col.add("Initial Quantity");
		
		tableConcentrationSpeciesmodel = new CustomTableModel_MSMB(Constants.MultistateBuilder_QUANTITIES_description,col,new Vector(),this,false,true);
		
		jTableConcentrationSpecies = new CustomJTable_MSMB();
		jTableConcentrationSpecies.setFont(MainGui.customFont);
		jTableConcentrationSpecies.getTableHeader().setFont(MainGui.customFont);
		jTableConcentrationSpecies.setEnabled(true);
		jTableConcentrationSpecies.setModel(tableConcentrationSpeciesmodel);
		
		jTableConcentrationSpecies.initializeCustomTable(tableConcentrationSpeciesmodel);
		jTableConcentrationSpecies.revalidate();
		jScrollPaneConcentrationSpecies.setViewportView(jTableConcentrationSpecies);
	
		
		TableColumn column = jTableConcentrationSpecies.getColumnModel().getColumn(col.size()-1);  
        column.setCellRenderer(new ScientificFormatCellRenderer());
        
        TableColumn column2 = jTableConcentrationSpecies.getColumnModel().getColumn(col.size()-2);  
        column2.setCellRenderer(new ScientificFormatCellRenderer());
      
        
		 HashMap<String, Integer> index_columns = new HashMap<String, Integer>();
		 for(int i = 0; i < tableConcentrationSpeciesmodel.getColumnCount(); i++ ){
			 index_columns.put(tableConcentrationSpeciesmodel.getColumnName(i), new Integer(i));
		 }
		 
		 Vector<Vector> singleConfigurations = this.species.getExpandedVectors();
		 for(int i = 0; i < singleConfigurations.size(); i++) {
			 Vector<Vector> singleConf = singleConfigurations.get(i);
			 Object[] singleConf_state = new Object[singleConf.size()/2];
			 for(int j = 0; j < singleConf.size(); j=j+2 ){
				 singleConf_state[(index_columns.get(singleConf.get(j))).intValue()-1] = singleConf.get(j+1);
			 }
			 Vector<Object> v = new Vector<Object>(Arrays.asList(singleConf_state));
			
			 
		 String quantity = species.getInitial_singleConfiguration(singleConf);
			 if(quantity!= null) v.add(quantity);
			 else v.add(MainGui.species_defaultInitialValue);
			 
			 
			 this.tableConcentrationSpeciesmodel.addRow(v);
			 for(int jj = 0; jj <tableConcentrationSpeciesmodel.getColumnCount()-1; jj++ ) {
				 tableConcentrationSpeciesmodel.disableCell(i, jj);
			 }
				
		 }
		 
		 this.jTableConcentrationSpecies.revalidate();
	    
	}



	private JComboBox getSpinner_lower() {
		if (spinner_lower == null) {
			spinner_lower = new JComboBox();
			spinner_lower.setEditable(true);
			spinner_lower.addItemListener(new ItemListener () {
				public void itemStateChanged(ItemEvent e) {
					jTextField_listStates.setText("");
					jRadioBoolean.setSelected(false);
					jTextField_listStates.revalidate();
				}
			});
			spinner_lower.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyTyped(java.awt.event.KeyEvent e) {
					jTextField_listStates.setText("");
					jRadioBoolean.setSelected(false);
					jTextField_listStates.revalidate();
				}
			});
		}
		return spinner_lower;
	}

	private JComboBox getSpinner_upper() {
		if (spinner_upper == null) {
			spinner_upper = new JComboBox();
			spinner_upper.setEditable(true);
			spinner_upper.addItemListener(new ItemListener () {
				public void itemStateChanged(ItemEvent e) {
					jTextField_listStates.setText("");
					jRadioBoolean.setSelected(false);
					jTextField_listStates.revalidate();
				}
			});
			spinner_upper.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyTyped(java.awt.event.KeyEvent e) {
					jTextField_listStates.setText("");
					jRadioBoolean.setSelected(false);
					jTextField_listStates.revalidate();
				}
			});
		}
		return spinner_upper;
	}

	private JTextField getJTextField_listStates() {
		if (jTextField_listStates == null) {
			jTextField_listStates = new JTextField();
			jTextField_listStates.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyTyped(java.awt.event.KeyEvent e) {
					spinner_upper.setSelectedItem(""); spinner_upper.revalidate();
					spinner_lower.setSelectedItem(""); spinner_lower.revalidate();
					jRadioBoolean.setSelected(false);
				}
			});
			
		}
		return jTextField_listStates;
	}

	private JButton getJButtonAddChange() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setHorizontalAlignment(SwingConstants.CENTER);
			jButton.setText("<html><p ALIGN=\"CENTER\">Add /<p ALIGN=\"CENTER\">Change</html>");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					if(name_before.trim().length() == 0) {
						addNewSite();
					} else {
						applyChangeSite();
					}
					
					name_before = new String();
					states_before.clear();
				}
			});
		}
		return jButton;
	}
	
private void applyChangeSite() {
		
		String sp = this.jTextField_species.getText().trim();
		if(sp.length() <= 0) return;
	
		String name = this.jTextField_newSite.getText().trim();
		if(name.length() <= 0) return;
		
		if(CellParsers.isKeyword(name)) {
    		 JOptionPane.showMessageDialog(null,"The name "+name+" is a reserved word. Please chose a different site name!", "Error", JOptionPane.ERROR_MESSAGE);
  			return;
    	}

		this.species.deleteSite(name_before);
		renamed_sites .put(name_before, name);
		
		addNewSite();
}


	private void addNewSite() {
		
		String sp = this.jTextField_species.getText().trim();
		if(sp.length() <= 0) return;
	
		String name = this.jTextField_newSite.getText().trim();
		if(name.length() <= 0) return;
	
		if(CellParsers.isKeyword(name)) {
			JOptionPane.showMessageDialog(null,"The name "+name+" is a reserved word. Please chose a different site name!", "Error", JOptionPane.ERROR_MESSAGE);
 			return;
		}
		
		String lowerStr = null;
		String upperStr = null;
		Integer lower = null;
		Integer upper = null;
		try {
			lower = new Integer(this.spinner_lower.getSelectedItem().toString());
		} catch(Exception ex) {
			try {
				lowerStr = this.spinner_lower.getSelectedItem().toString(); //even if everything is not ok, we should save it for later
				lower = CellParsers.evaluateExpression(this.spinner_lower.getSelectedItem().toString());
			} catch (Throwable e) {
				e.printStackTrace();
				//problems in parsing/evaluating the expression: I should just make the site with warning errors but accept it
			}
		}
		
		try {
			upper =  new Integer(this.spinner_upper.getSelectedItem().toString());
		} catch(Exception ex) {
			try {
				upperStr = this.spinner_upper.getSelectedItem().toString();
				upper = CellParsers.evaluateExpression(this.spinner_upper.getSelectedItem().toString());
			} catch (Throwable e) {
				e.printStackTrace();
				//problems in parsing/evaluating the expression: I should just make the site with warning errors but accept it
			}
	
		}
		if(lower != null && upper != null) {
			if(upper < lower) {
				  JOptionPane.showMessageDialog(this,"The upper value of the range cannot be smaller than the lower!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		if(lower != null && lowerStr== null) 	lowerStr = lower.toString();
		if(upper != null && upperStr == null)	upperStr= upper.toString();
	
		try {
			if(jRadioBoolean.isSelected()) {		
				this.species.addSite_string(name, Constants.BooleanType.TRUE.description + "," +Constants.BooleanType.FALSE.description);
			}
			else if(this.jTextField_listStates.getText().trim().length() > 0) {
				this.species.addSite_string(name, this.jTextField_listStates.getText().trim());
			}
			else {
				this.species.addSite_range(name, lowerStr, upperStr);
			}
		} catch(Throwable ex) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 
				ex.printStackTrace();
			//problem in parsing species (e.g. cdhBoolWrong(p{TRUE,FALSE,somethingElse}))
			  JOptionPane.showMessageDialog(this,"Problem parsing the current site. \n"+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			this.refreshListSites();
			this.updateJTableConcentrationSpecies();
		}
		
	}
	
	
	private void deleteSite() {
		int selected = this.jListSite.getSelectedIndex();//this.jTableListSite.getSelectedRow();
		if(selected == -1) return;
		//String site = (String) (this.jTableListSite.getModel().getValueAt(selected, 0));
		String site = (String) this.jListSite.getSelectedValue();
		if(site.length() == 0 ) return;
		this.species.deleteSite(site.substring(0, site.indexOf("{")));
		this.refreshListSites();
		this.updateJTableConcentrationSpecies();
		
	}
	
	
	
	
	
	private void refreshListSites() {
		DefaultListModel model = (DefaultListModel)this.jListSite.getModel();
		model.clear();
		
		
		Set<String> names = this.species.getSitesNames();
		
	    Iterator<String> iterator = names.iterator();  
	    while (iterator.hasNext()) {  
	       String site_name = iterator.next();
	       String pr = species.printSite(site_name);
	       rendererJListSite.removeSiteWithProblem(site_name);
	       model.addElement(pr);
	    }  
	    
	    HashMap<String, MutablePair<String, String>> sitesWithProblems = this.species.getSitesRangesWithVariables();
	   
	    Iterator<String> iterator2 = sitesWithProblems.keySet().iterator();  
	    while (iterator2.hasNext()) {  
	       String site_name = iterator2.next();
	       if(names.contains(site_name)) continue; //site already printed before
	       rendererJListSite.addSiteWithProblem(site_name);
	       String pr = species.printSiteWithUndefinedElements(site_name);
	       model.addElement(pr);
	      
	    }  
	    
	    
	    
		this.jTextField_newSite.setText("");
		this.jTextField_listStates.setText("");
		this.jRadioBoolean.setSelected(false);
		this.spinner_lower.setSelectedItem("");
		this.spinner_upper.setSelectedItem("");
		
		
		this.jListSite.revalidate();
		
		
	}



	

	private JScrollPane getJScrollPaneJListSite() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJListSite());
		}
		return jScrollPane;
	}

	private JList getJListSite() {
		if (jListSite == null) {
			jListSite = new JList();
			jListSite.setModel(new DefaultListModel());
			jListSite.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			rendererJListSite = new RendererForErrorsJList();
			jListSite.setCellRenderer(rendererJListSite);
		}
		return jListSite;
	}
	
	


	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("Delete site");
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					deleteSite();
				}
			});
		}
		return jButton1;
	}
	
	private JButton getJButton2() {
		if (jButton2 == null) {
			jButton2 = new JButton();
			jButton2.setText("Modify site");
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					modifySite();
				}
			});
		}
		return jButton2;
	}

	
	String name_before = new String();
	Vector states_before = new Vector();
	
	protected void modifySite() {
		
		if(jListSite.getSelectedIndex() == -1) return;
		
		String site = (String) jListSite.getSelectedValue();
		if(site.length() == 0) return;
		String name = site.substring(0,site.indexOf("{"));
		Vector<?> states = species.getSiteStates(name);
		String start = (String)states.get(0);
		String end = (String)states.get(1);
		String list = (String)states.get(2);
		boolean bool = (Boolean)states.get(3);
		
		if(bool) {
			jRadioBoolean.setSelected(true);
			spinner_upper.setSelectedItem("");
			spinner_lower.setSelectedItem("");
			jTextField_listStates.setText("");
		}
		else { 
			jRadioBoolean.setSelected(false);
			if(list.compareTo("?")==0) list = "";
			jTextField_listStates.setText(list);
			if(list.length() == 0) {
				spinner_upper.setSelectedItem(end);
				spinner_lower.setSelectedItem(start);
			} else {
				spinner_upper.setSelectedItem("");
				spinner_lower.setSelectedItem("");
			}
			
		}
		jTextField_newSite.setText(name);
		name_before = name;
		states_before.clear();
		states_before.addAll(states);
	}
	
	
	

	private JButton getJButton3() {
		if (jButton3 == null) {
			jButton3 = new JButton();
			jButton3.setText("Update Model");
			jButton3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try{
						updateModel();
					} catch(Throwable ex) {
						if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 	ex.printStackTrace();
					}
				}
			});
		}
		return jButton3;
	}
	
	private void updateModel() throws Throwable{
		this.species.setName(new String(this.jTextField_species.getText()));
		this.species.setType(Constants.SpeciesType.MULTISTATE.copasiType);
		try {
			this.parentFrame.updateModel_fromMultiBuilder(this.species, renamed_sites);
		} catch(Throwable ex) {
			ex.printStackTrace();
		}
		dispose();
	}
	
	public void setMultistateSpecies(MultistateSpecies sp) {
		this.species =  sp; //new MultistateSpecies(completeDef);
		String speciesName = this.species.getSpeciesName();
		 if(speciesName.startsWith("\"")&&speciesName.endsWith("\"")) speciesName = speciesName.substring(1, speciesName.length()-1);
			
			
		this.jTextField_species.setText(speciesName);
		if(jTextField_species.getText().length() >0) {
			this.setTabbedPane_enable(true);
		}
		
		updateJTableConcentrationSpecies();
		refreshListSites();
		
		if(sp.getCompartments().size() == 0) {
			try {
				sp.setCompartment(MainGui.multiModel, MainGui.compartment_default_for_dialog_window);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		
	}
	
	public void clearAll() throws Exception {
		jContentPane = null;
		jTabbedPane = null;
		upper = null;
		jLabel = null;
		jTextField_species = null;
		jTextField_newSite = null;
		spinner_lower = null;
		spinner_upper = null;
		jTextField_listStates = null;
		jButton = null;
		jListSite = null;
		jScrollPane = null;
		jLabel2 = null;
		jButton1 = null;
		jButton2 = null;
		jButton3 = null;
		species = new MultistateSpecies(MainGui.multiModel,new String());
		jPanelSitesDetails = null;
		initialize();
		updateJTableConcentrationSpecies();
	}


	
	public void updateMultisiteSpeciesConcentrationFromTable(int row) throws Exception {
		int quantityColumn = tableConcentrationSpeciesmodel.getColumnCount()-1;
		String initialQuantity = (String) this.tableConcentrationSpeciesmodel.getValueAt(row, quantityColumn);
		
		String singleConfiguration = this.species.getExpandedSites().get(row);
		singleConfiguration = this.species.getSpeciesName() + "(" + singleConfiguration + ")";
		
		HashMap<String, String> singleEntry = new HashMap<String, String>();
		singleEntry.put(singleConfiguration, initialQuantity);
		this.species.setInitialQuantity(singleEntry);
		
		 this.jTableConcentrationSpecies.revalidate();
	    
	     
	 }
	
	
	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.add(getJPanelConcentrationSpecies(), BorderLayout.CENTER);
		}
		return panel;
	}

	public void selectInitialQuantityTab() {
		jTabbedPane.setSelectedIndex(1);
	}
}  

class CustomFocusTraversalPolicy extends FocusTraversalPolicy {
Vector<Component> order;
private int currentIndex = 0;

	public CustomFocusTraversalPolicy(Vector<Component> order) {
		this.order = new Vector<Component>(order.size());
		this.order.addAll(order);
		currentIndex = 0;
	}
		
	public Component getComponentAfter(Container focusCycleRoot, Component aComponent)
	{
		int index = order.indexOf(aComponent);
		if(index ==-1) {//trick because traversal policy has issues with comboboxes
			if(currentIndex ==2) {
				currentIndex = 3;
				return order.get(3);
			} else {
				currentIndex = 4;
				return order.get(4);
			}
		} else {
			int idx = (order.indexOf(aComponent) + 1) % order.size();
			currentIndex = idx;
			return order.get(idx);	
		}
	}
	
	public Component getComponentBefore(Container focusCycleRoot,
	                          Component aComponent)
	{
		
		int index = order.indexOf(aComponent);
		if(index ==-1) {//trick because traversal policy has issues with comboboxes
			if(currentIndex ==2) {
				currentIndex = 1;
				return order.get(1);
			} else {
				currentIndex = 2;
				return order.get(2);
			}
		}
			
		int idx = index - 1;
		if (idx < 0) {
			currentIndex = idx;
			idx = order.size() - 1;
		}
		return order.get(idx);
	}
	
	public Component getDefaultComponent(Container focusCycleRoot) {
		return order.get(0);
	}
	
	public Component getLastComponent(Container focusCycleRoot) {
		return order.lastElement();
	}
	
	public Component getFirstComponent(Container focusCycleRoot) {
		return order.get(0);
	}
	
}

class RendererForErrorsJList  extends JLabel implements  ListCellRenderer  {
	
	private static final long serialVersionUID = 1L;
	private HashSet<String> siteNamesWithProblems = new HashSet<String>();
	public RendererForErrorsJList() {
		 setOpaque(true);
		 setFont(MainGui.customFont);
	}
	
	public boolean isEmptySiteWithProblem() {
		return siteNamesWithProblems.isEmpty();
	}
	
	public void addSiteWithProblem(String site_name) {
		siteNamesWithProblems.add(site_name);
	}
	
	public void removeSiteWithProblem(String site_name) {
		siteNamesWithProblems.remove(site_name);
	}
	    
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
    	String selectedItem = value.toString();
    	setText(selectedItem);
    	if(selectedItem.indexOf("{")==-1) return this;
    	String site_name = selectedItem.substring(0,selectedItem.indexOf("{"));
    	
         if (siteNamesWithProblems.contains(site_name)) {
          
          	Border compound = null;
  			Border redline = BorderFactory.createLineBorder(GraphicalProperties.color_cell_with_errors,2);
  			compound = BorderFactory.createCompoundBorder(redline, compound);
  			setBorder(compound);
         } else {
        	 setBorder(null);
         }
         
         if(isSelected) {
        	   setBackground(list.getSelectionBackground());
               setForeground(list.getSelectionForeground());
         } else {
        	   setBackground(list.getBackground());
               setForeground(list.getForeground());
         }
            
        return this;
    }

    
};