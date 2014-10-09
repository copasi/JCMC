package acgui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

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
 * Dialog window to add a visible variable.
 * @author T.C. Jones
 * @version March 15, 2013
 */
public class VariableAddEditor extends JDialog implements ActionListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTextField textfield;
	private JComboBox comboBox1;
	private mxGraphComponent graphComponent;
	
	
	/**
	 * 
	 */
	public VariableAddEditor(mxGraphComponent iGraphComponent)
	{
		super();
		graphComponent = iGraphComponent;
		initializeComponents();
	}

	private void initializeComponents()
	{
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new GridLayout(0, 2, 15, 5));

		//create, fill, and add the Ref Name combo box
		Vector<String> refNames = AC_GUI.modelBuilder.getRefNames();
		SortedComboBoxModel sortedModel = new SortedComboBoxModel(refNames, new RefNameComparator());
		comboBox1 = new JComboBox(sortedModel);
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
		
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new FlowLayout());
		JButton addButton = new JButton("Add");
		addButton.setActionCommand("add");
		addButton.addActionListener(this);
		lowerPanel.add(addButton);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		lowerPanel.add(cancelButton);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(upperPanel, BorderLayout.CENTER);
		getContentPane().add(lowerPanel, BorderLayout.SOUTH);

		setTitle("Show Variable");
		setSize(650, 500);
		pack();
		setLocationRelativeTo(graphComponent);
	}
	
	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getActionCommand().equalsIgnoreCase("add"))
		{
			String msg;
			
			if ((comboBox1.getSelectedItem() == null) || ((String)comboBox1.getSelectedItem()).equals(""))
			{
				JOptionPane.showMessageDialog(null, "Please select a Ref Name.");
			}
			else
			{
				if (AC_Utility.showVariableValidation((String)comboBox1.getSelectedItem()))
				{
					//AC_GUI.addVisibleVariable((String)comboBox1.getSelectedItem());
					AC_GUI.showVariable((String)comboBox1.getSelectedItem());
					dispose();
				}
			}
		}
		else if (ae.getActionCommand().equalsIgnoreCase("cancel"))
		{
			dispose();
		}
	}
}
