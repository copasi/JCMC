package msmb.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

import msmb.commonUtilities.tables.ScientificFormatCellRenderer;
import msmb.model.ComplexSpecies;
import msmb.model.MultistateSpecies;
import msmb.model.Species;
import msmb.utility.CellParsers;
import msmb.utility.Constants;
import msmb.utility.GraphicalProperties;
import msmb.utility.MySyntaxException;

public class ComplexBuilderFrame_Initials extends JDialog {
	private JPanel  panelInitials;
	private JPanel jPanelConcentrationSpecies;
	private JScrollPane jScrollPaneConcentrationSpecies;
	private CustomJTable_MSMB jTableConcentrationSpecies;
	private CustomTableModel_MSMB tableConcentrationSpeciesmodel;
	private JPanel contentPane;
	ComplexSpecies species;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ComplexBuilderFrame_Initials frame = new ComplexBuilderFrame_Initials();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void setVisible(boolean b) {
		GraphicalProperties.resetFonts(this);
		this.setSize(45*MainGui.customFont.getSize(), 25*MainGui.customFont.getSize());
		setLocationRelativeTo(null);
		pack();
		super.setVisible(b);
	}

	public ComplexBuilderFrame_Initials() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setModal(true);
		setBounds(100, 100, 450, 300);
		setTitle("Complex initial quantities");
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		contentPane.add(getInitialsPanel());
		/*try {
			species = new ComplexSpecies(new String("Multi(a{0:4};b{3:4})"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		updateJTableConcentrationSpecies();*/
	}
	
	
	public void setComplexSpecies(ComplexSpecies complexSpecies) {
		try {
			this.species =  new ComplexSpecies(MainGui.multiModel,complexSpecies);
	
		} catch (Exception e) {
			e.printStackTrace();
		}
		updateJTableConcentrationSpecies();
	}
	
	ComplexSpecies showDialog() {
	    setVisible(true);
	    //called after the OK make it invisible so that the jdialog returns the value to the other part
		int quantityColumn = tableConcentrationSpeciesmodel.getColumnCount()-1;
		int rowCount = tableConcentrationSpeciesmodel.getRowCount();
		for(int row = 0; row < rowCount; row++) {
			String initialQuantity = (String) this.tableConcentrationSpeciesmodel.getValueAt(row, quantityColumn);
			
			Vector<String> expandedSites = this.species.getExpandedSites();
			String singleConfiguration = new String();
			HashMap<String, String> singleEntry = new HashMap<String, String>();
				if(expandedSites.size() > 0) {
				singleConfiguration = this.species.getExpandedSites().get(row);
				singleConfiguration = CellParsers.extractMultistateName(this.species.getSpeciesName()) + "(" + singleConfiguration + ")";
			} else {
				singleConfiguration = this.species.getDisplayedName();
			}
			singleEntry.put(singleConfiguration, initialQuantity);
			this.species.setInitialQuantity(singleEntry);
		}
		return this.species;
	}
	
	/*public MultistateSpecies getComplexWithInitials(){
	;
	}*/
	
	private void updateJTableConcentrationSpecies(){
		
		Vector<String> col = new Vector<String>();
		Set<String> names= this.species.getSitesNames();

	    Iterator<String> iterator = names.iterator();  
	    while (iterator.hasNext()) {  
	    	col.add(iterator.next());
	    }  
	    
	    if(names.size() ==0) {
	    	col.add("Species");
	    }
		col.add("Initial Quantity");
		
		tableConcentrationSpeciesmodel = new CustomTableModel_MSMB(Constants.MultistateBuilder_QUANTITIES_description,col,new Vector(),(MultistateBuilderFrame)null,false,true);
		
		jTableConcentrationSpecies = new CustomJTable_MSMB();
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
		 
		 //no multistate components, can still set the concentration of the complex
		    if(names.size() ==0) {
		    	Vector<String> row = new Vector<String>();
		    	
		    	row.add("Complex");
		    	row.add(species.getInitial_singleConfiguration(new Species(species.getName())));
		    	this.tableConcentrationSpeciesmodel.addRow(row);
		    	 tableConcentrationSpeciesmodel.disableCell(0, 1);
		    }
		 
		 this.jTableConcentrationSpecies.revalidate();
	    
	}
	
	private JPanel getInitialsPanel() {
		if (panelInitials == null) {
			panelInitials = new JPanel();
			panelInitials.setLayout(new BorderLayout());
			panelInitials.add(getJPanelConcentrationSpecies(), BorderLayout.CENTER);
			JPanel panel_ok = new JPanel();
			panelInitials.add(panel_ok, BorderLayout.SOUTH);
			
			JButton btnSaveInitials = new JButton("Save initial quantities");
			btnSaveInitials.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);  
					   dispose(); 
				}
			});
			panel_ok.add(btnSaveInitials);
				
		}
		return panelInitials;
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
			tableConcentrationSpeciesmodel = new CustomTableModel_MSMB(Constants.MultistateBuilder_QUANTITIES_description,col,new Vector(),(MultistateBuilderFrame)null);
			jTableConcentrationSpecies = new CustomJTable_MSMB();
			jTableConcentrationSpecies.setModel(tableConcentrationSpeciesmodel);
			jTableConcentrationSpecies.initializeCustomTable(tableConcentrationSpeciesmodel);
		}
		return jTableConcentrationSpecies;
	}

}
