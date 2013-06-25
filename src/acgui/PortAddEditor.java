package acgui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import com.mxgraph.swing.mxGraphComponent;

/**
 * A dialog box to help the user add a port.
 */
public class PortAddEditor extends JDialog
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Component parent;
	private Module module;
	private JTextField textfield;
	private JComboBox<String> comboBox1;
	private JComboBox<PortType> comboBox2;
	private mxGraphComponent graphComponent;

	/**
	 * Constructs the object.
	 * @param mod the module where the port will be added
	 */
	public PortAddEditor(Object cell, mxGraphComponent iGraphComponent)
	{
		super();
		module = AC_GUI.masterModuleList.findModule(cell);
		graphComponent = iGraphComponent;
		initComponents();
	}

	/**
	 * Initialize and display the dialog box.
	 */
	private void initComponents()
	{		
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new GridLayout(0, 2, 15, 5));

		//create, fill, and add the Ref Name combo box
		Vector<String> refNames = AC_GUI.modelBuilder.getRefNames();
		SortedComboBoxModel sortedModel = new SortedComboBoxModel(refNames, new RefNameComparator());
		comboBox1 = new JComboBox<String>(sortedModel);
		// has to be editable
        comboBox1.setEditable(true);
        // get the combo boxes editor component
        JTextComponent editor = (JTextComponent) comboBox1.getEditor().getEditorComponent();
        // change the editor's document
        editor.setDocument(new ComboBoxFilter(comboBox1));
		JPanel upperPanel1 = new JPanel();
		//upperPanel1.setBorder(BorderFactory.createTitledBorder("Ref Name: "));
		//upperPanel1.setLayout(new GridLayout(1, 1));
		JLabel label1 = new JLabel("Ref Name:");
		label1.setFont(new Font("Serif", Font.PLAIN, 14));
		//upperPanel1.add(new Label("Ref Name:"));
		upperPanel.add(label1);
		upperPanel.add(comboBox1);
		
		//create, fill, and add the Port Type combo box
		comboBox2 = new JComboBox<PortType>();
		comboBox2.addItem(PortType.INPUT);
		comboBox2.addItem(PortType.OUTPUT);
		comboBox2.addItem(PortType.EQUIVALENCE);
		JPanel upperPanel2 = new JPanel();
		//upperPanel2.setBorder(BorderFactory.createTitledBorder("Port Type: "));
		//upperPanel2.setLayout(new GridLayout(1, 1));
		//upperPanel2.add(new Label("Port Type:"));
		JLabel label2 = new JLabel("Port Type:");
		label2.setFont(new Font("Serif", Font.PLAIN, 14));
		upperPanel.add(label2);
		upperPanel.add(comboBox2);
		
		//create and add the Port Name text box
		textfield = new JTextField(15);
		textfield.setText("newPort");
		JPanel upperPanel3 = new JPanel();
		//upperPanel3.add(new Label("Port Name:"));
		JLabel label3 = new JLabel("Port Name:");
		label3.setFont(new Font("Serif", Font.PLAIN, 14));
		upperPanel.add(label3);
		upperPanel.add(textfield);

		//add the user input panels
		/*
		upperPanel.add(upperPanel1);
		upperPanel.add(upperPanel2);
		upperPanel.add(upperPanel3);
		*/
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new FlowLayout());
		JButton addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				int checkPorts = AC_GUI.portValidation(textfield.getText(), (String)comboBox1.getSelectedItem());
				//System.out.println("checkPorts = " + checkPorts);
				String msg;
				switch(checkPorts)
				{
				case 0:
					dispose();
					AC_GUI.currentGUI.addPort(module, (String)comboBox1.getSelectedItem(), textfield.getText(), (PortType)comboBox2.getSelectedItem());
					break;
				case 1:
					msg = "\"" + (String)comboBox1.getSelectedItem() + "\"";
					msg += " is already associated with a Port.";
					msg += " Cannot associate the same Ref Name with multiple Ports.";
					JOptionPane.showMessageDialog(null, msg);
					break;
				case 2:
					msg = "\"" + textfield.getText() + "\"";
					msg += " is already the name of a Port.";
					msg += " Cannot assign the same Port Name to multiple Ports.";
					JOptionPane.showMessageDialog(null, msg);
					break;
				default:
					
				}
			}
		});
		lowerPanel.add(addButton);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		lowerPanel.add(cancelButton);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(upperPanel, BorderLayout.CENTER);
		getContentPane().add(lowerPanel, BorderLayout.SOUTH);

		setTitle("Add a new port ...");
		setSize(650, 500);
		pack();
		setLocationRelativeTo(graphComponent);
	}
}