package msmb.runManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import msmb.gui.MainGui;
import msmb.utility.GraphicalProperties;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

public class ChangeIntervalPlotFrame extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private ExitOption exitOption = ExitOption.CANCEL;
	Vector<String> variablesWithCurrentInterval = new Vector<String>();
	private JList list;
	private DefaultListModel listModel;
	private JButton btnSaveInterval;
	private JLabel lbl2;
	private JSpinner spinner;
	private JLabel lbl1;

	public static void main(String[] args) {
		try {
			ChangeIntervalPlotFrame dialog = new ChangeIntervalPlotFrame();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Vector<String> setVariablesAndShow(
			Vector<String> varsWithCurrentInterval) {
		try {
			listModel.clear();
			for(int i = 0; i < varsWithCurrentInterval.size(); ++i) {
				listModel.addElement(varsWithCurrentInterval.get(i));
			}
			
			GraphicalProperties.resetFonts(this);
			pack();
			setLocationRelativeTo(null);
			
			setVisible(true);
		
			//once the window is closed
			if(exitOption != ExitOption.CANCEL) {
				variablesWithCurrentInterval.clear();
					for(int i = 0; i < listModel.getSize(); ++i) {
					variablesWithCurrentInterval.add(listModel.get(i).toString());
				}
				return variablesWithCurrentInterval;
			}
			
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public ChangeIntervalPlotFrame() {
		setModal(true);
		setTitle("Change plotting interval");
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.CENTER);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JScrollPane scrollPane = new JScrollPane();
				panel.add(scrollPane);
				{
					listModel = new DefaultListModel();
					list = new JList(listModel);
					
					scrollPane.setViewportView(list);
				}
			}
			{
				JLabel lblNewLabel = new JLabel("Variables");
				panel.add(lblNewLabel, BorderLayout.NORTH);
			}
			{
				JPanel panel_1 = new JPanel();
				panel.add(panel_1, BorderLayout.SOUTH);
				panel_1.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel_2 = new JPanel();
					panel_1.add(panel_2);
					panel_2.setLayout(new BorderLayout(10, 10));
					{
						 lbl1 = new JLabel("  Plot every");
						 lbl1.setEnabled(false);
						panel_2.add(lbl1, BorderLayout.WEST);
					}
					{
						 spinner = new JSpinner();
						 spinner.setEnabled(false);
						 spinner.setPreferredSize(new Dimension(100, 10));
						panel_2.add(spinner, BorderLayout.CENTER);
						spinner.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
					}
					{
						 lbl2 = new JLabel("time step(s)");
						 lbl2.setEnabled(false);
						panel_2.add(lbl2, BorderLayout.EAST);
					}
				}
				{
					JPanel panel_2 = new JPanel();
					panel_1.add(panel_2, BorderLayout.EAST);
					{
						 btnSaveInterval = new JButton("Save interval");
						 btnSaveInterval.addActionListener(new ActionListener() {
						 	public void actionPerformed(ActionEvent e) {
						 	
								int[] sel = list.getSelectedIndices();
								for(int i = 0; i < sel.length; ++i) {
									String current = listModel.get(sel[i]).toString();
									int last = current.lastIndexOf("(");
									current = current.substring(0, last) + "(" + spinner.getValue() + ")";
									listModel.set(sel[i], current);
								}
								lbl2.setEnabled(false);
								lbl1.setEnabled(false);
								spinner.setEnabled(false);
								btnSaveInterval.setEnabled(false);
								spinner.setValue(1);
						 	}
						 });
						panel_2.add(btnSaveInterval);
					}
				}
				{
					JButton btnNewButton = new JButton("Change interval");
					btnNewButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if(list.getSelectedIndices().length > 0) {
								lbl2.setEnabled(true);
								lbl1.setEnabled(true);
								spinner.setEnabled(true);
								btnSaveInterval.setEnabled(true);
								int sel = list.getSelectedIndex();
								String current = listModel.get(sel).toString();
								int start = current.lastIndexOf("(");
								int end = current.lastIndexOf(")");
								int val = Integer.parseInt(current.substring(start+1,end).trim());
								spinner.setValue(val);
							}
						}
					});
					panel_1.add(btnNewButton, BorderLayout.WEST);
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						exitOption = ExitOption.CANCEL;
						dispose();
					}
				});
				{
					JButton okButton = new JButton("OK");
					okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							exitOption = ExitOption.OK;
							dispose();
						}
					});
					okButton.setActionCommand("OK");
					buttonPane.add(okButton);
					getRootPane().setDefaultButton(okButton);
				}
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}
