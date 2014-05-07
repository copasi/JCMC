package  msmb.gui;


import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import msmb.model.MultiModel;
import msmb.model.MultistateSpecies;
import msmb.utility.Constants;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import java.awt.SystemColor;
import javax.swing.SwingConstants;
import javax.swing.LayoutStyle.ComponentPlacement;


import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JRadioButton;

public class RenamingDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JLabel btnNewButton_1;
	private JLabel lblASpeciesWith;
	private JTextPane txtpnIfADifferent;
	private JLabel lblOr;
	private JPanel panel;
	private JPanel panel_2;
	String clashingName = new String();
	String speciesRow = new String();
	String textIfADifferent = new String();
	String completeClashingMultistate = new String();
	
	String returnString = new String();
	//private String speciesOldName;
	static MultiModel multiModel = MainGui.multiModel;
	private JTextField textField_1;
	private JTextField textField_2;
	private JRadioButton rdbtnNewSpeciesName;
	private JRadioButton rdbtnNewCompartmentName;
	private String newSpeciesName;
	private String newCompartmentName;
	
	public String getNewSpeciesName() {
		if(newSpeciesName==null) return null;
		return newSpeciesName.trim();
	}
	public String getNewCompartmentName() {
		if(newCompartmentName==null) return null;
		return newCompartmentName.trim();
	}
	
	public String getReturnString() {
		return returnString;
	} 

	 
	public RenamingDialog(MultiModel m, String clashingName, String elementRow, String elementOldName, int actionsType) {
		multiModel=m;
		this.clashingName = clashingName;
		this.speciesRow = elementRow;
	//	this.speciesOldName = speciesOldName;
		this.textIfADifferent = "If a different name is NOT provided, the current "+MainGui.cellTableEdited+" \r"+elementRow+"\r\nwill be DELETED and " +
				"all the references to "+elementOldName+" will be redirected to "+clashingName+".";
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(
				 new WindowAdapter() {
			            @Override
			            public void windowClosing(WindowEvent e) {
			            	cancelOption();
			            }
			        });
		setModal(true);
		setResizable(false);
		setTitle("Existing name");
		setBounds(100, 100, 454, 221);
		Icon icon = UIManager.getIcon("OptionPane.warningIcon");
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.NORTH);
		{
			lblOr = new JLabel((String) null);
		}
		contentPanel.setLayout(new BorderLayout(10, 0));
		contentPanel.add(lblOr);
		{
			panel = new JPanel();
			contentPanel.add(panel);
			btnNewButton_1 = new JLabel(icon);
			{
				lblASpeciesWith = new JLabel("A "+MainGui.cellTableEdited+" with that name already exists. You can provide a");
				lblASpeciesWith.setHorizontalAlignment(SwingConstants.LEFT);
			}
			
			
			GroupLayout gl_panel = new GroupLayout(panel);
			gl_panel.setHorizontalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel.createSequentialGroup()
						.addContainerGap()
						.addComponent(btnNewButton_1)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(lblASpeciesWith, GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
						.addGap(11))
			);
			gl_panel.setVerticalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
							.addComponent(btnNewButton_1)
							.addComponent(lblASpeciesWith, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE))
						.addGap(0, 0, Short.MAX_VALUE))
			);
			panel.setLayout(gl_panel);
		}
		
		JPanel panel_newName = new JPanel();
		panel_newName.setForeground(Color.WHITE);
		contentPanel.add(panel_newName, BorderLayout.SOUTH);
		if(MainGui.cellTableEdited.compareTo(Constants.TitlesTabs.SPECIES.getDescription()) == 0){
			panel_newName.setLayout(new GridLayout(2, 0, 0, 0));
		} else {
			panel_newName.setLayout(new GridLayout(1, 0, 0, 0));
		}
		
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_newName.add(panel_1);
		
		rdbtnNewSpeciesName = new JRadioButton("New "+MainGui.cellTableEdited+" name:  ");
		panel_1.add(rdbtnNewSpeciesName);
		rdbtnNewSpeciesName.setSelected(true);
		
		textField_1 = new JTextField();
		panel_1.add(textField_1);
		textField_1.setColumns(10);
		ButtonGroup names = new ButtonGroup();
		names.add(rdbtnNewSpeciesName);
		
		if(MainGui.cellTableEdited.compareTo(Constants.TitlesTabs.SPECIES.getDescription()) == 0){
			JPanel panel_3 = new JPanel();
			FlowLayout flowLayout_1 = (FlowLayout) panel_3.getLayout();
			flowLayout_1.setAlignment(FlowLayout.LEFT);
			panel_newName.add(panel_3);
			
			rdbtnNewCompartmentName = new JRadioButton("New Compartment name:");
			
			panel_3.add(rdbtnNewCompartmentName);
			names.add(rdbtnNewCompartmentName);
			textField_2 = new JTextField();
			panel_3.add(textField_2);
			textField_2.setColumns(10);
			textField_2.setEditable(false);
			rdbtnNewCompartmentName.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if(rdbtnNewCompartmentName.isSelected()) {
						textField_1.setEditable(false);
						textField_1.setText("");
						textField_2.setEditable(true);
					}
				}
			});
		}
		
	
		rdbtnNewSpeciesName.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(rdbtnNewSpeciesName.isSelected()) {
					textField_2.setEditable(false);
					textField_2.setText("");
					textField_1.setEditable(true);
				}
			}
		});
		
	
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			
			if(actionsType == Constants.DELETE_SPECIES_AND_REDIRECT)
			{
				
				{
					JButton btnNewButton = new JButton("Merge "+MainGui.cellTableEdited+"\n");
					btnNewButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							deleteSpeciesAndRedirect();
						}
					});
					buttonPane.add(btnNewButton);
				}
			}
			else if(actionsType == Constants.MERGE_SPECIES)
			{
				
				JButton btnNewButton = new JButton("Merge "+MainGui.cellTableEdited+"\n");
				btnNewButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						mergeSpecies();
					}
				});
				buttonPane.add(btnNewButton);
			}
			/*else		{
				
				JButton btnNewButton = new JButton("Use new name\n");
				btnNewButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						useNewName();
					}
				});
				buttonPane.add(btnNewButton);
			}*/
			
			
			{
				if(actionsType == Constants.MERGE_SPECIES 
						|| actionsType == Constants.DELETE_SPECIES_AND_REDIRECT
						|| actionsType == Constants.DUPLICATE_SPECIES_NAME) {
					JButton newName = new JButton("Use New Name");
					
					newName.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								useNewName();
							}
						});
					
					buttonPane.add(newName);
				}
				
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							cancelOption();
						}
					});
					cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
				
				
			}
		}
		panel_2 = new JPanel();
		getContentPane().add(panel_2, BorderLayout.CENTER);
		{
			txtpnIfADifferent = new JTextPane();
			txtpnIfADifferent.setBackground(SystemColor.menu);
			txtpnIfADifferent.setEditable(false);
				txtpnIfADifferent.setText(textIfADifferent);
		}
		if(actionsType == Constants.MERGE_SPECIES || actionsType == Constants.DELETE_SPECIES_AND_REDIRECT)
		{
			GroupLayout gl_panel_2 = new GroupLayout(panel_2);
			gl_panel_2.setHorizontalGroup(
				gl_panel_2.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel_2.createSequentialGroup()
						.addContainerGap()
						.addComponent(txtpnIfADifferent, GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
						.addContainerGap())
			);
			gl_panel_2.setVerticalGroup(
				gl_panel_2.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel_2.createSequentialGroup()
						.addComponent(txtpnIfADifferent, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(18, Short.MAX_VALUE))
			);
			panel_2.setLayout(gl_panel_2);
		}
		this.setLocationRelativeTo(null);
		pack();
	}
	
	public RenamingDialog(String name) {
		//reduced renaming dialog used when I try to create a species with the same name as an existing one (i.e. I don't need to delete o redirect the references)
		this(multiModel,name, new String(),"", Constants.DUPLICATE_SPECIES_NAME);
		this.textIfADifferent = new String();
		txtpnIfADifferent.setText(textIfADifferent);
		pack();
	}

	public RenamingDialog(String name, String completeClashingMultistate, String existingSpecies) {
		//reduced renaming dialog used when I try to create a multistatespecies with the same name as an existing one (i.e. I need to ask if the user wants to use a fresh name or merge the states)
		this(multiModel,name, new String() ,"",Constants.MERGE_SPECIES);
		this.completeClashingMultistate = completeClashingMultistate;
		this.textIfADifferent =  "If a different name is NOT provided, the new species \r"+completeClashingMultistate+"\r\nwill be MERGED with "+existingSpecies;
		txtpnIfADifferent.setText(textIfADifferent);
		pack();
	}
	
	protected void cancelOption() {
		returnString = null;
		setVisible(false);
	}
	protected void deleteSpeciesAndRedirect() {
		returnString = "TO_BE_DELETED";
		setVisible(false);
	}
	
	protected void mergeSpecies() {
		returnString = "MERGED_SPECIES";
		setVisible(false);
	}
	
	protected void useNewName() {
		if(rdbtnNewCompartmentName!= null && rdbtnNewCompartmentName.isSelected()) {
			newCompartmentName = textField_2.getText();
			newSpeciesName = null;
			returnString = "NEW_NAME";
		} else {
			newCompartmentName = null;
			newSpeciesName = textField_1.getText();
			if(newSpeciesName.trim().length() == 0) 	returnString = null;
			else {
				if(MainGui.cellValueBeforeChange.compareTo(newSpeciesName)==0)  {
					returnString = null;
					setVisible(false);
					return;
				}
				if(multiModel.getWhereNameIsUsed(newSpeciesName)!= null && multiModel.getWhereNameIsUsed(newSpeciesName).contains(Constants.TitlesTabs.getIndexFromDescription(MainGui.cellTableEdited))) {
					JOptionPane.showMessageDialog(this, "A "+MainGui.cellTableEdited+" with the new name already exists\n Provide a different name!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				returnString = "NEW_NAME";
			}
			
		}
		
		if(returnString==null || returnString.trim().length() ==0) {
			JOptionPane.showMessageDialog(this, "The new name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
	
		
		
		setVisible(false);
	}
	
	protected void freshNameMultistateOption() {
		MultistateSpecies ms = null;
		try {
			ms = new MultistateSpecies(multiModel,completeClashingMultistate);
		} catch (Exception e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
		}
		ms.setName(textField.getText().trim());
		returnString = ms.printCompleteDefinition();
		if(completeClashingMultistate.compareTo(returnString)==0) {
			JOptionPane.showMessageDialog(this, "The new name should be different from the original one!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		setVisible(false);
		
	}
	
	protected void freshNameOption() {
		
		returnString = textField.getText().trim();
		if(clashingName.compareTo(returnString)==0) {
			JOptionPane.showMessageDialog(this, "The new name should be different from the original one!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		setVisible(false);
		
	}
}
