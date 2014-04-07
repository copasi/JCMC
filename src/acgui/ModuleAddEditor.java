package acgui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mxgraph.swing.mxGraphComponent;

/**
 * @author Thomas
 *
 */
public class ModuleAddEditor extends JDialog implements ActionListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Module parent;
	private mxGraphComponent graphComponent;
	private JTextField textfield;
	private String acceptButtonName;
	
	public ModuleAddEditor(Module iParent, mxGraphComponent iGraphComponent, String buttonName)
	{
		super();
		parent = iParent;
		graphComponent = iGraphComponent;
		acceptButtonName = buttonName;
		initializeComponents();
	}

	private void initializeComponents()
	{
		JPanel upperPanel = new JPanel();
		
		JLabel label = new JLabel("Module name:");
		//label.setFont(new Font("Serif", Font.PLAIN, 14));
		textfield = new JTextField();
		textfield.setColumns(20);
		//textfield.setBorder(BorderFactory.createLoweredBevelBorder());
		
		upperPanel.add(label);
		upperPanel.add(textfield);
		//upperPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		JPanel lowerPanel = new JPanel();
        lowerPanel.setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton(acceptButtonName);
        //addButton.setActionCommand("add");
        addButton.addActionListener(this);
        JButton cancelButton = new JButton("Cancel");
        //cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        lowerPanel.add(buttonPanel, BorderLayout.EAST);
        
        getContentPane().setLayout(new BorderLayout());
		getContentPane().add(upperPanel, BorderLayout.NORTH);
		//getContentPane().add(templatePanel, BorderLayout.CENTER);
		//getContentPane().add(tpadding, BorderLayout.CENTER);
		getContentPane().add(lowerPanel, BorderLayout.SOUTH);
		//getContentPane().setPreferredSize(new Dimension(650, 500));
		this.getRootPane().setDefaultButton(addButton);
		
		setTitle("New Module");
		setSize(650, 500);
		pack();
		setLocationRelativeTo(graphComponent);
	}

	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getActionCommand().equals(acceptButtonName))
		{
			String name = textfield.getText();
			if (AC_Utility.moduleNameValidation(name, true))
			{
				if (parent == null)
				{
					// the new Module will be the activeModule
					AC_GUI.newModule(name);
				}
				else
				{
					// the new Module will be a Submodule
					AC_GUI.newSubmodule(name, parent);
				}
					
				dispose();
			}
		} 
		else if (ae.getActionCommand().equals("Cancel"))
		{
			dispose();
		}
	}
}
