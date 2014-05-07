package  msmb.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import java.awt.SystemColor;
import javax.swing.SwingConstants;
import javax.swing.LayoutStyle.ComponentPlacement;


import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;

import msmb.utility.Constants;

public class DeletingDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JLabel btnNewButton_1;
	private JLabel lblASpeciesWith;
	private JTextPane txtpnIfADifferent;
	private JLabel lblOr;
	private JPanel panel;
	private JPanel panel_1;
	private JPanel panel_2;
	String clashingName = new String();
	String speciesRow = new String();
	
	int returnOption = Constants.DELETE_CANCEL;
	//private String speciesOldName;
	
	public int getReturnOption() {
		return returnOption;
	}

	public DeletingDialog(String message) {
			setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setModal(true);
		setResizable(false);
		setTitle("Deleting entity...");
		setBounds(100, 100, 512, 308);
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
				lblASpeciesWith = new JLabel("The entity(ies) that you want to delete are referenced from the following rows:");
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
		{
			panel_1 = new JPanel();
			contentPanel.add(panel_1, BorderLayout.SOUTH);
			
			JScrollPane scrollPane = new JScrollPane();
			GroupLayout gl_panel_1 = new GroupLayout(panel_1);
			gl_panel_1.setHorizontalGroup(
				gl_panel_1.createParallelGroup(Alignment.TRAILING)
					.addGroup(Alignment.LEADING, gl_panel_1.createSequentialGroup()
						.addGap(50)
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 431, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(15, Short.MAX_VALUE))
			);
			gl_panel_1.setVerticalGroup(
				gl_panel_1.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel_1.createSequentialGroup()
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(15, Short.MAX_VALUE))
			);
			
			JTextPane txtFoundIn = new JTextPane();
			txtFoundIn.setEditable(false);
			scrollPane.setViewportView(txtFoundIn);
			txtFoundIn.setText(message);
			txtFoundIn.setCaretPosition(0);
			panel_1.setLayout(gl_panel_1);
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			{
				JButton okButton = new JButton("Delete just the entity(ies)");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						returnOption = Constants.DELETE_JUST_ENTITIES;
						setVisible(false);
					}
				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton btnNewButton = new JButton("<html><p>Recursively, delete all </p><p>entity(ies) and referencing elements</p></html>");
				btnNewButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						returnOption = Constants.DELETE_RECURSIVELY_ALL;
						setVisible(false);
					}
				});
				buttonPane.add(btnNewButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						returnOption = Constants.DELETE_CANCEL;
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		{
			panel_2 = new JPanel();
			getContentPane().add(panel_2, BorderLayout.CENTER);
			{
				txtpnIfADifferent = new JTextPane();
				txtpnIfADifferent.setBackground(SystemColor.menu);
				txtpnIfADifferent.setEditable(false);
				txtpnIfADifferent.setText("What do you want to do?");
			}
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
	
/*	protected void cancelOption() {
		returnString = null;
		setVisible(false);
		
	}
	protected void deleteSpeciesAndRedirect() {
		returnString = "TO_BE_DELETED";
		setVisible(false);
	}
	
/*	protected void freshNameOption() {
		returnString = textField.getText().trim();
		if(clashingName.compareTo(returnString)==0) {
			JOptionPane.showMessageDialog(this, "The new name should be different from the original one!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		setVisible(false);
	}*/
}
