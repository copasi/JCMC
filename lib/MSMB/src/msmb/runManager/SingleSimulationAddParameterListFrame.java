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

public class SingleSimulationAddParameterListFrame extends JDialog {
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
					SingleSimulationAddParameterListFrame frame = new SingleSimulationAddParameterListFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	public Vector<Mutant> setListAndShow(String nameNodeAnalysis, Vector<Object> currentList) {
		setTitle("Add Parameter list to - "+nameNodeAnalysis);
		HashSet all = new HashSet(RunManager.getAllMutants_parametersList());
		listIn_model.clear();
		listNotIn_model.clear();
		for(int i = 0; i < currentList.size(); ++i) {
			Mutant m = (Mutant) currentList.get(i);
			listIn_model.add(m);
			all.remove(m);
		}
		Iterator<Mutant> it = all.iterator();
		while(it.hasNext()) {
			listNotIn_model.add(it.next());
		}
		
		GraphicalProperties.resetFonts(this);
		setLocationRelativeTo(null);
		
		
		
		setVisible(true);
	
		Vector<Mutant> ret = new Vector<Mutant>();
		//once the window is closed
		if(exitOption != ExitOption.CANCEL) {
				System.out.println("MUTANTS FOR SIMULATION " +listIn_model);
				it = listIn_model.iterator();
				while(it.hasNext()) {
					ret.add(it.next());
				}
				return ret;
		} else {
			return null;
		}
		
	}
	
	
	public SingleSimulationAddParameterListFrame() {
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
		listNotIn_model.add(new Mutant("A"));
		listNotIn_model.add(new Mutant("b"));
		listNotIn_model.add(new Mutant("c"));
		listNotIn_model.add(new Mutant("d"));
		listNotIn_model.add(new Mutant("e"));
		
		 jListNotIn = new JList(listNotIn_model);
		scrollPane.setViewportView(jListNotIn);
		
		JLabel lblAvailableParametersLists = new JLabel("Available Parameters lists");
		panel_4.add(lblAvailableParametersLists, BorderLayout.NORTH);
		
		JPanel panel_1 = new JPanel();
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
		
		JButton btnNewButton = new JButton("Add (with descendants) ->");
		btnNewButton.addActionListener(new ActionListener() {
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
		panel_1.add(btnNewButton);
		
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
		
		JLabel lblInCurrentAnalysis = new JLabel("In current Analysis node");
		panel_5.add(lblInCurrentAnalysis, BorderLayout.NORTH);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		panel_5.add(scrollPane_1, BorderLayout.CENTER);
		
		listIn_model = new SortedListModel();
		 jListIn = new JList(listIn_model);
		scrollPane_1.setViewportView(jListIn);
		
		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.NORTH);
		
		JLabel lblMoveToThe = new JLabel("Select the Parameters lists that you want to associate to the current Analysis node.");
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

class SortedListModel extends AbstractListModel {

	  SortedSet model;

	  public SortedListModel() {
	    model = new TreeSet();
	  }

	  public int getSize() {    return model.size();	  }

	  public Object getElementAt(int index) {	    return model.toArray()[index];	  }

  public void add(Object element) {
	    if (model.add(element)) {
	      fireContentsChanged(this, 0, getSize());
	    }
	  }

	  public void addAll(Object elements[]) {
	    Collection c = Arrays.asList(elements);
	    model.addAll(c);
	    fireContentsChanged(this, 0, getSize());
	  }

	  public void clear() {
	    model.clear();
	    fireContentsChanged(this, 0, getSize());
	  }

	  public boolean contains(Object element) {
	    return model.contains(element);
	  }

	  public Object firstElement() {
	    return model.first();
	  }

	  public Iterator iterator() {
	    return model.iterator();
	  }

	  public Object lastElement() {
		    return model.last();
	  }

	  public boolean removeElement(Object element) {
	    boolean removed = model.remove(element);
	    if (removed) {
	      fireContentsChanged(this, 0, getSize());
	    }
	    return removed;   
	  }
	}



