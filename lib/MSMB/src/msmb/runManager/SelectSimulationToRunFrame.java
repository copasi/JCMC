package msmb.runManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import msmb.gui.MainGui;
import msmb.utility.GraphicalProperties;
import javax.swing.JScrollPane;

public class SelectSimulationToRunFrame extends JDialog {
	private ExitOption exitOption = ExitOption.CANCEL;
	private final JPanel contentPanel = new JPanel();
	private DefaultTreeModel treeModel;
	private JCheckBoxTree jcheckboxtree;

	public static void main(String[] args) {
		try {
			SelectSimulationToRunFrame dialog = new SelectSimulationToRunFrame();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    private void createNodes(DefaultMutableTreeNode hiddenRoot, Set<Simulation> simulations) {
    	Iterator<Simulation> it = simulations.iterator();
    	
    	while(it.hasNext()) {
    			Simulation s = it.next();
    			DefaultMutableTreeNode sim = new DefaultMutableTreeNode(s);
    			hiddenRoot.add(sim);
    			Vector<Object> mutants = s.getMutantsParameters();
    			Iterator it2 = mutants.iterator();
    			while(it2.hasNext()) {
    				Mutant m = (Mutant) it2.next();
    				DefaultMutableTreeNode mut = new DefaultMutableTreeNode(m);
    				sim.add(mut);
    			}
    	}
   }
	
	public TreePath[] setVariablesAndShow(Set<Simulation> simulations) {
		try {
			 DefaultMutableTreeNode hiddenRoot = new DefaultMutableTreeNode("Simulation to run");
			 createNodes(hiddenRoot, simulations);
	    	 treeModel = new DefaultTreeModel(hiddenRoot);
	    	 jcheckboxtree.setModel(treeModel);
	    	
			GraphicalProperties.resetFonts(this);
			jcheckboxtree.setFont(MainGui.customFont);
			jcheckboxtree.setRowHeight(MainGui.customFont.getSize()+10);
			pack();
			setLocationRelativeTo(null);
			
			setVisible(true);
		
			//once the window is closed
			if(exitOption != ExitOption.CANCEL) {
				return jcheckboxtree.getCheckedPaths();
			}
			
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	

	
	public SelectSimulationToRunFrame() {
		setTitle("Simulations to Run");
		setModal(true);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						exitOption = ExitOption.OK;
						dispose();
					}
				});
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						exitOption = ExitOption.CANCEL;
						dispose();
					}
				});
			}
		}
		 contentPanel.setLayout(new BorderLayout(0, 0));
		  {
		  	JScrollPane scrollPane = new JScrollPane();
		  	contentPanel.add(scrollPane, BorderLayout.CENTER);
		  	scrollPane.setPreferredSize(new Dimension(200,300));
		  	 jcheckboxtree = new JCheckBoxTree();
		  	 scrollPane.setViewportView(jcheckboxtree);
		  }
		  this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		/*  jcheckboxtree.addCheckChangeEventListener(new JCheckBoxTree.CheckChangeEventListener() {
	            public void checkStateChanged(JCheckBoxTree.CheckChangeEvent event) {
	                System.out.println("event");
	                TreePath[] paths = jcheckboxtree.getCheckedPaths();
	                for (TreePath tp : paths) {
	                    for (Object pathPart : tp.getPath()) {
	                        System.out.print(pathPart + ",");
	                    }                   
	                    System.out.println();
	                }
	            }           
	        });     */
	      
	}

}


class JCheckBoxTree extends JTree {


    // Defining data structure that will enable to fast check-indicate the state of each node
    // It totally replaces the "selection" mechanism of the JTree
    private class CheckedNode {
        boolean isSelected;
        boolean hasChildren;
        boolean allChildrenSelected;

        public CheckedNode(boolean isSelected_, boolean hasChildren_, boolean allChildrenSelected_) {
            isSelected = isSelected_;
            hasChildren = hasChildren_;
            allChildrenSelected = allChildrenSelected_;
        }
    }
    HashMap<TreePath, CheckedNode> nodesCheckingState;
    HashSet<TreePath> checkedPaths = new HashSet<TreePath>();

    // Defining a new event type for the checking mechanism and preparing event-handling mechanism
    protected EventListenerList listenerList = new EventListenerList();

    public class CheckChangeEvent extends EventObject {     
              public CheckChangeEvent(Object source) {
            super(source);          
        }       
    }   

    public interface CheckChangeEventListener extends EventListener {
        public void checkStateChanged(CheckChangeEvent event);
    }

    public void addCheckChangeEventListener(CheckChangeEventListener listener) {
        listenerList.add(CheckChangeEventListener.class, listener);
    }
    public void removeCheckChangeEventListener(CheckChangeEventListener listener) {
        listenerList.remove(CheckChangeEventListener.class, listener);
    }

    void fireCheckChangeEvent(CheckChangeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] == CheckChangeEventListener.class) {
                ((CheckChangeEventListener) listeners[i + 1]).checkStateChanged(evt);
            }
        }
    }

    // Override
    public void setModel(TreeModel newModel) {
        super.setModel(newModel);
        resetCheckingState();
    }

    // New method that returns only the checked paths (totally ignores original "selection" mechanism)
    public TreePath[] getCheckedPaths() {
        return checkedPaths.toArray(new TreePath[checkedPaths.size()]);
    }

    // Returns true in case that the node is selected, has children but not all of them are selected
    public boolean isSelectedPartially(TreePath path) {
        CheckedNode cn = nodesCheckingState.get(path);
        return cn.isSelected && cn.hasChildren && !cn.allChildrenSelected;
    }

    private void resetCheckingState() { 
        nodesCheckingState = new HashMap<TreePath, CheckedNode>();
        checkedPaths = new HashSet<TreePath>();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)getModel().getRoot();
        if (node == null) {
            return;
        }
        addSubtreeToCheckingStateTracking(node);
    }

    // Creating data structure of the current model for the checking mechanism
    private void addSubtreeToCheckingStateTracking(DefaultMutableTreeNode node) {
        TreeNode[] path = node.getPath();   
        TreePath tp = new TreePath(path);
        CheckedNode cn = new CheckedNode(false, node.getChildCount() > 0, false);
        nodesCheckingState.put(tp, cn);
        for (int i = 0 ; i < node.getChildCount() ; i++) {              
            addSubtreeToCheckingStateTracking((DefaultMutableTreeNode) tp.pathByAddingChild(node.getChildAt(i)).getLastPathComponent());
        }
    }

    // Overriding cell renderer by a class that ignores the original "selection" mechanism
    // It decides how to show the nodes due to the checking-mechanism
    private class CheckBoxCellRenderer extends JPanel implements TreeCellRenderer {     
        JCheckBox checkBox;     
        public CheckBoxCellRenderer() {
            super();
            this.setLayout(new BorderLayout());
            checkBox = new JCheckBox();
            add(checkBox, BorderLayout.CENTER);
            setOpaque(false);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            Object obj = node.getUserObject();          
            TreePath tp = new TreePath(node.getPath());
            CheckedNode cn = nodesCheckingState.get(tp);
            if (cn == null) {
                return this;
            }
            checkBox.setSelected(cn.isSelected);
            checkBox.setText(obj.toString());
            checkBox.setOpaque(true);
            
           Color someChildrenSelected = Color.CYAN;
           Color allChildrenSelected = Color.GREEN;
           Color mutantSelected = Color.white;
           Color mutant = Color.white;
           Color simulation = Color.white;
           
            
            if(node.getUserObject() instanceof Simulation) {
	            if(cn.isSelected && cn.hasChildren && ! cn.allChildrenSelected) {
	            	checkBox.setBackground(someChildrenSelected);
	            } else   if(cn.isSelected && cn.hasChildren && cn.allChildrenSelected) {
	            	checkBox.setBackground(allChildrenSelected);
	            } else {
	            	if(!cn.hasChildren && cn.isSelected) {
	            		checkBox.setBackground(allChildrenSelected);
	            	}
	            	else checkBox.setBackground(simulation);
	            }
            } else if(node.getUserObject() instanceof Mutant) {
	            if(cn.isSelected ) {
	            	checkBox.setBackground(mutantSelected);
	            } else {
	            	checkBox.setBackground(mutant);
	            }
            }
            
            Border border = BorderFactory.createEmptyBorder ( 4, 4, 4, 4 );

            checkBox.setBorder ( border );
        
            	
            return this;
        }       
    }

    public JCheckBoxTree() {
    	 super();
    	 setRootVisible( false );
    	 setShowsRootHandles(true);
       		    
        // Disabling toggling by double-click
        this.setToggleClickCount(0);
        // Overriding cell renderer by new one defined above
        CheckBoxCellRenderer cellRenderer = new CheckBoxCellRenderer();
        this.setCellRenderer(cellRenderer);

        // Overriding selection model by an empty one
        DefaultTreeSelectionModel dtsm = new DefaultTreeSelectionModel() {      
            // Totally disabling the selection mechanism
            public void setSelectionPath(TreePath path) {
            }           
            public void addSelectionPath(TreePath path) {                       
            }           
            public void removeSelectionPath(TreePath path) {
            }
            public void setSelectionPaths(TreePath[] pPaths) {
            }
        };
        
        // Calling checking mechanism on mouse click
        this.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
            	
            if ( SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
			    	e.consume();	
			        TreePath tp = JCheckBoxTree.this.getPathForLocation(e.getX(), e.getY());
	                if (tp == null) {
	                    return;
	                }
	                boolean checkMode = ! nodesCheckingState.get(tp).isSelected;
	                checkSubTree(tp, checkMode);
	                updatePredecessorsWithCheckMode(tp);
			  	  }
			       // Firing the check change event
                fireCheckChangeEvent(new CheckChangeEvent(new Object()));
                // Repainting tree after the data structures were updated
                JCheckBoxTree.this.repaint();                       
            }           
            public void mouseEntered(MouseEvent arg0) { }           
            public void mouseExited(MouseEvent arg0) { }
            public void mousePressed(MouseEvent arg0) {}
            public void mouseReleased(MouseEvent arg0) {}           
            
         
        });
        this.setSelectionModel(dtsm);
    }

    

    
    
    
	// When a node is checked/unchecked, updating the states of the predecessors
    protected void updatePredecessorsWithCheckMode(TreePath tp) {
        TreePath parentPath = tp.getParentPath();
        // If it is the root, stop the recursive calls and return
        if (parentPath == null) {
            return;
        }       
        CheckedNode parentCheckedNode = nodesCheckingState.get(parentPath);
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();     
        parentCheckedNode.allChildrenSelected = true;
        parentCheckedNode.isSelected = false;
        for (int i = 0 ; i < parentNode.getChildCount() ; i++) {                
            TreePath childPath = parentPath.pathByAddingChild(parentNode.getChildAt(i));
            CheckedNode childCheckedNode = nodesCheckingState.get(childPath);           
            // It is enough that even one subtree is not fully selected
            // to determine that the parent is not fully selected
            if (! childCheckedNode.allChildrenSelected) {
                parentCheckedNode.allChildrenSelected = false;      
            }
            // If at least one child is selected, selecting also the parent
            if (childCheckedNode.isSelected) {
                parentCheckedNode.isSelected = true;
            }
        }
        if (parentCheckedNode.isSelected) {
            checkedPaths.add(parentPath);
        } else {
            checkedPaths.remove(parentPath);
        }
        // Go to upper predecessor
        updatePredecessorsWithCheckMode(parentPath);
    }

    // Recursively checks/unchecks a subtree
    protected void checkSubTree(TreePath tp, boolean check) {
        CheckedNode cn = nodesCheckingState.get(tp);
        cn.isSelected = check;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();
        cn.allChildrenSelected = check;
        for (int i = 0 ; i < node.getChildCount() ; i++) {          
        	DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
        	if(child.getUserObject() instanceof Simulation) {
        		cn.allChildrenSelected = !check;
        	} else {
        		checkSubTree(tp.pathByAddingChild(child), check);
        	}
        }
       
        if (check) {
            checkedPaths.add(tp);
        } else {
            checkedPaths.remove(tp);
        }
    }
    
    // Just check the current node and not the subtree
    protected void checkNode(TreePath tp, boolean check) {
        CheckedNode cn = nodesCheckingState.get(tp);
        cn.isSelected = check;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();
        if(node.getChildCount()==0) {
        	cn.allChildrenSelected = check;
        }
        if (check) {
            checkedPaths.add(tp);
        } else {
            checkedPaths.remove(tp);
        }
    }

}