package  msmb.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.UIManager;

import msmb.utility.Constants;
import msmb.utility.GraphicalProperties;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.JScrollPane;

public class FunctionFromExpressionDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JPanel panel_listElement;
	Vector<Vector<String>> table_elementName_equation = new Vector();
	Vector<Vector<String>> elementsToDefine = new Vector();
	
	public Vector<Vector<String>>  getElementsToReturn() {return table_elementName_equation;}
	
	public FunctionFromExpressionDialog(Vector<Vector<String>> elementsToDefine, String kineticEquation, boolean massAction) {
		if(!massAction) setTitle("New elements definition");
		else setTitle("New global quantity definition");
		setMaximumSize(new Dimension(600,400));
		setMinimumSize(new Dimension(200,100));
		this.elementsToDefine.addAll(elementsToDefine);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				fillReturnValue();
			}
		});
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setAlwaysOnTop(true);
		setModal(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setBounds(100, 100, 450, 164);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.NORTH);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JTextPane txtpnKineticLawsNeed = new JTextPane();
				txtpnKineticLawsNeed.setEditable(false);
				txtpnKineticLawsNeed.setBackground(UIManager.getColor("Panel.background"));
				if(!massAction) txtpnKineticLawsNeed.setText("Kinetic Laws need to be expressed as single function call with single actual parameters.\r\nMSMB will define the following new elements for you:");
				else txtpnKineticLawsNeed.setText("Mass Action Kinetic Laws need to be expressed as single global quantity reference.\r\nMSMB will define the following new global quantity for you:");
				panel.add(txtpnKineticLawsNeed);
			}
		}
		{
			JLabel lblProvideADifferent = new JLabel("Provide a different name for each element, if you don't like the default one.");
			contentPanel.add(lblProvideADifferent, BorderLayout.SOUTH);
		}
		{
			JPanel panel = new JPanel();
			panel.setBorder(null);
			contentPanel.add(panel, BorderLayout.CENTER);
			panel_listElement = new JPanel();
			panel_listElement.setBorder(null);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JScrollPane scrollPane_listElement = new JScrollPane();
				panel.add(scrollPane_listElement, BorderLayout.CENTER);
				scrollPane_listElement.setViewportView(panel_listElement);
				scrollPane_listElement.setBorder(BorderFactory.createEmptyBorder());
			}
			{
				
				panel_listElement.setLayout(new GridLayout(elementsToDefine.size(), 1, 0, 0));
				for(int i = 0; i < elementsToDefine.size(); i++) {
					addPanelSingleElement(elementsToDefine.get(i));
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						fillReturnValue();
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
	
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
		
		
		GraphicalProperties.resetFonts(this);
		
	}

	
	/*private void resetFonts() {
		 List<JButton> buttons = SwingUtils.getDescendantsOfClass(JButton.class, this);
		 Iterator<JButton> it = buttons.iterator();
		 while(it.hasNext()) {
			 JButton current = it.next();
			 current.setFont(MainGui.customFont);
		 }
		 
		 List<JTextPane> textPanes = SwingUtils.getDescendantsOfClass(JTextPane.class, this);
		 Iterator<JTextPane> it2 = textPanes.iterator();
		 while(it2.hasNext()) {
			 JTextPane current = it2.next();
			 current.setFont(MainGui.customFont);
		 }
		 
		 List<JLabel> labels = SwingUtils.getDescendantsOfClass(JLabel.class, this);
		 Iterator<JLabel> it3 = labels.iterator();
		 while(it3.hasNext()) {
			 JLabel current = it3.next();
			 current.setFont(MainGui.customFont);
		 }
		 
		 List<JTextField> textFields = SwingUtils.getDescendantsOfClass(JTextField.class, this);
		 Iterator<JTextField> it4 = textFields.iterator();
		 while(it4.hasNext()) {
			 JTextField current = it4.next();
			 current.setFont(MainGui.customFont);
		 }
		
		this.revalidate();
		
	}*/

	protected void fillReturnValue() {
		
		for(int i = 0; i < panel_listElement.getComponentCount(); i++) {
			JPanel panel = (JPanel) panel_listElement.getComponent(i);
			JPanel subPanel = (JPanel) panel.getComponent(1);
			JTextField textfield = (JTextField) subPanel.getComponent(0);
			
			Vector element = elementsToDefine.get(i);
			Vector newElement = new Vector();
			newElement.add(element.get(0));
			newElement.add(textfield.getText().trim());
			newElement.add(element.get(2));
			if(element.size() > 3) newElement.add(element.get(3));
			table_elementName_equation.add(newElement);
		}
	/*	table_textfieldName_labelEquation
		
		for(int i = 0; i < table_textfieldName_labelEquation.size(); i++) {
			Vector element = table_textfieldName_labelEquation.get(i);
			JTextField txtFunctionreaction = (JTextField) element.get(1);
			JLabel lblNewLabel_2 = (JLabel) element.get(2);
			
			Vector newElement = new Vector();
			newElement.add(element.get(0));
			newElement.add(txtFunctionreaction.getText().trim());
			newElement.add(lblNewLabel_2.getText().trim());
			if(element.size() > 3) newElement.add(element.get(3));
			table_elementName_equation.add(newElement);
		}*/
		
	}

	void addPanelSingleElement(Vector<String> element) {
		JPanel panel_singleElement = new JPanel();
		panel_listElement.add(panel_singleElement);
		{
			panel_singleElement.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel_singleElement_labels = new JPanel();
				panel_singleElement.add(panel_singleElement_labels, BorderLayout.WEST);
				panel_singleElement_labels.setLayout(new GridLayout(0, 1, 0, 0));
				{
					JLabel lblNewLabel = null;
					if(element.get(0).compareTo(Constants.TitlesTabs.FUNCTIONS.getDescription())==0) {
						lblNewLabel = new JLabel("Function name: ");
					
					} else {
						lblNewLabel = new JLabel("Global Quantity name: ");
						
						
					}
					panel_singleElement_labels.add(lblNewLabel);
				}
				{
					JLabel lblNewLabel_1 = new JLabel("Equation: ");
					panel_singleElement_labels.add(lblNewLabel_1);
				}
			}
			{
				JPanel panel_2 = new JPanel();
				panel_singleElement.add(panel_2, BorderLayout.CENTER);
				panel_2.setLayout(new GridLayout(0, 1, 0, 0));
				JTextField txtFunctionreaction = new JTextField();
				
				JLabel lblNewLabel_2 = new JLabel(element.get(2));
				txtFunctionreaction.setText(element.get(1));
				panel_2.add(txtFunctionreaction);
				txtFunctionreaction.setColumns(10);
				panel_2.add(lblNewLabel_2);
				
				/*Vector newElement = new Vector();
				newElement.add(element.get(0));
				newElement.add(txtFunctionreaction);
				newElement.add(lblNewLabel_2);
				if(element.size() > 3) newElement.add(element.get(3));
				table_textfieldName_labelEquation.add(newElement);*/
			}
		}
	}


	/*protected void returnValue() {
		if(txtFunctionreaction.getText().trim().length() ==0) {
			JOptionPane.showMessageDialog(new JButton(),"The name cannot be empty!", "Error!", JOptionPane.ERROR_MESSAGE);
		}
		else {
			nameToReturn = txtFunctionreaction.getText().trim();
		}
		
	}*/

}
