/**
 * 
 */
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
 * @author T.C. Jones
 * @version March 25, 2013
 */
public class MathematicalAggregatorAddEditor extends JDialog implements ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField textfield1;
	private JTextField textfield2;
	private mxGraphComponent graphComponent;
	private String type;
	private Operation op;
	private Module parent;
	
	/**
	 * 
	 */
	public MathematicalAggregatorAddEditor(Module iParent, mxGraphComponent iGraphComponent, Operation iOp)
	{
		super();
		parent = iParent;
		graphComponent = iGraphComponent;
		op = iOp;
		
		if(op == Operation.SUM)
		{
			type = "Summation";
		}
		else
		{
			type = "Product";
		}
		initializeComponents();
	}

	private void initializeComponents()
	{
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new GridLayout(0, 2, 15, 5));
		
		//create and add the Name text box
		textfield1 = new JTextField(15);
		textfield1.setText(type);
		JPanel upperPanel1 = new JPanel();
		//upperPanel3.add(new Label("Port Name:"));
		JLabel label1 = new JLabel(type + " Module Name:");
		label1.setFont(new Font("Serif", Font.PLAIN, 14));
		upperPanel.add(label1);
		upperPanel.add(textfield1);
		
		//create and add the input text box
		textfield2 = new JTextField(15);
		//textfield2.setText("");
		JPanel upperPanel2 = new JPanel();
		//upperPanel3.add(new Label("Port Name:"));
		JLabel label2 = new JLabel("Number of Inputs:");
		label2.setFont(new Font("Serif", Font.PLAIN, 14));
		upperPanel.add(label2);
		upperPanel.add(textfield2);
		
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
		this.getRootPane().setDefaultButton(addButton);
		
		setTitle("Add " + type + " Module");
		setSize(650, 500);
		pack();
		setLocationRelativeTo(graphComponent);
	}

	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getActionCommand().equalsIgnoreCase("add"))
		{
			String name = textfield1.getText();
			if (name.equals(""))
			{
				JOptionPane.showMessageDialog(null, "Please enter a Module Name.");
			}
			else if (textfield2.getText().equals(""))
			{
				JOptionPane.showMessageDialog(null, "Please enter the number of inputs.");
			}
			else
			{
				if (AC_Utility.newModuleNameValidation(parent, name, true))
				{
					AC_GUI.newMathematicalAggregator(textfield1.getText(), Integer.parseInt(textfield2.getText()), op);
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
