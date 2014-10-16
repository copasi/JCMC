package msmb.runManager;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.AbstractListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JList;

import msmb.utility.GraphicalProperties;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

public class SinglePlotAddTimeSeriesFrame extends JDialog {
	private ExitOption exitOption = ExitOption.CANCEL;

	private JPanel contentPane;
	private SortedListModel listNotIn_model;
	private SortedListModel listIn_model;
	private JList jListIn;
	private JList jListNotIn;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SinglePlotAddTimeSeriesFrame frame = new SinglePlotAddTimeSeriesFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	public Set<String> setListAndShow(String nameNodeAnalysis, Set<String> currentList) {
		setTitle("Add Time Series to - "+nameNodeAnalysis);
		HashSet all = new HashSet(RunManager.getTimeSeriesVariables());
		listIn_model.clear();
		listNotIn_model.clear();
		Iterator<String> it0 = currentList.iterator();
		while(it0.hasNext()) {
			String m = it0.next();
			listIn_model.add(m);
			all.remove(m);
		}
		Iterator<String> it = all.iterator();
		while(it.hasNext()) {
			listNotIn_model.add(it.next());
		}
		
		GraphicalProperties.resetFonts(this);
		setLocationRelativeTo(null);
		
		setVisible(true);
	
		Set<String> ret = new HashSet<String>();
		//once the window is closed
		if(exitOption != ExitOption.CANCEL) {
				it = listIn_model.iterator();
				while(it.hasNext()) {
					ret.add(it.next());
				}
				return ret;
		} else {
			return null;
		}
		
	}
	
	
	public SinglePlotAddTimeSeriesFrame() {
		setModal(true);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new GridLayout(1, 0, 0, 0));
		
		JPanel panel_4 = new JPanel();
		panel.add(panel_4);
		panel_4.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel_4.add(scrollPane, BorderLayout.CENTER);
		
		listNotIn_model = new SortedListModel();
		
		
		 jListNotIn = new JList(listNotIn_model);
		scrollPane.setViewportView(jListNotIn);
		
		JLabel lblAvailableParametersLists = new JLabel("Available variables");
		panel_4.add(lblAvailableParametersLists, BorderLayout.NORTH);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(9, 9, 9, 9));
		panel_1.setLayout(new GridLayout(4, 1,9,9));
		
		panel.add(panel_1);
		
		JButton btnAddSelected = new JButton("Add selected ->");
		btnAddSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] sel = jListNotIn.getSelectedValues();
				if(sel==null) return;
				listIn_model.addAll(sel);
				for(int i = 0; i < sel.length; ++i) {
					listNotIn_model.removeElement(sel[i]);
				}
				jListIn.clearSelection();
				jListNotIn.clearSelection();
			}
		});
		panel_1.add(btnAddSelected);
		
		JButton btnAddAll = new JButton("Add all ->");
		btnAddAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(int i = 0; i < listNotIn_model.getSize(); ++i) {
					listIn_model.add(listNotIn_model.getElementAt(i));
				}
				listNotIn_model.clear();
				jListIn.clearSelection();
				jListNotIn.clearSelection();
			}
			
		});
		panel_1.add(btnAddAll);
		
		JButton button = new JButton("<- Remove");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] sel = jListIn.getSelectedValues();
				if(sel==null) return;
				listNotIn_model.addAll(sel);
				for(int i = 0; i < sel.length; ++i) {
					listIn_model.removeElement(sel[i]);
				}
				jListIn.clearSelection();
				jListNotIn.clearSelection();
			}
		});
		panel_1.add(button);
		
		JButton button_1 = new JButton("<- Remove all");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(int i = 0; i < listIn_model.getSize(); ++i) {
					listNotIn_model.add(listIn_model.getElementAt(i));
				}
				listIn_model.clear();
				jListIn.clearSelection();
				jListNotIn.clearSelection();
			}
		
		});
		panel_1.add(button_1);
		
		JPanel panel_5 = new JPanel();
		panel.add(panel_5);
		panel_5.setLayout(new BorderLayout(0, 0));
		
		JLabel lblInCurrentAnalysis = new JLabel("In current plot");
		panel_5.add(lblInCurrentAnalysis, BorderLayout.NORTH);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		panel_5.add(scrollPane_1, BorderLayout.CENTER);
		
		listIn_model = new SortedListModel();
		 jListIn = new JList(listIn_model);
		scrollPane_1.setViewportView(jListIn);
		
		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.NORTH);
		
		JLabel lblMoveToThe = new JLabel("Select the variables that you want to plot.");
		panel_2.add(lblMoveToThe);
		
		JPanel panel_3 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_3.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		contentPane.add(panel_3, BorderLayout.SOUTH);
		
		JButton btnSaveClose = new JButton("Save & close");
		btnSaveClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitOption = ExitOption.OK;
				dispose();
			}
		});
		panel_3.add(btnSaveClose);
	}
	
	enum ExitOption {
		OK(0),
	   CANCEL(-1);
	   			          
	   public final int code;
	   
	   ExitOption(int index) {
	              this.code = index;
	    }
	   
	  }

}




