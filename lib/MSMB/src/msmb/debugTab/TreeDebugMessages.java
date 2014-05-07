package msmb.debugTab;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import msmb.gui.MainGui;
//import msmb.runManager.SingleMutantFrame;



public class TreeDebugMessages extends JPanel {
    TreeView treeView;
    JTextArea textArea;
    JList currentViewArea;
    DefaultListModel listModel;
    
    final static String newline = "\n";

    //SingleMutantFrame singleMutantFrame = null;
    Object singleMutantFrame = null;
    //if not null, is from the run manager
    public TreeDebugMessages(final Object singleMutantFrame) {
    	 treeView = new TreeView();
    	 textArea = new JTextArea();
    	 listModel = new DefaultListModel();
    	 currentViewArea = new JList(listModel); //data has type Object[]
    	 
    	// this.singleMutantFrame = singleMutantFrame;
    	 currentViewArea.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	 currentViewArea.setVisibleRowCount(-1);
    	 
    	 currentViewArea.addListSelectionListener(new ListSelectionListener() {
			@Override
			 public void valueChanged(ListSelectionEvent e) {
				JList lsm = (JList)e.getSource();

		       if (!lsm.isSelectionEmpty()) {
		            // Find out which indexes are selected.
		            int index = lsm.getMinSelectionIndex();
		            DebugMessage dm = (DebugMessage) listModel.get(index);
		            textArea.setText(dm.getCompleteDescription());
		            revalidate();
		                 
		        }
		      }
		});
    	 
    	 currentViewArea.addMouseListener(new MouseAdapter()
	        {
	            public void mouseClicked(MouseEvent e)
	            {
	                if (e.getComponent().isEnabled() && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
	                {
	                	DebugMessage dm = (DebugMessage)currentViewArea.getSelectedValue();
	                	if(singleMutantFrame==null) MainGui.highlightElement_relatedWith(dm);
	                	else {
	                		//singleMutantFrame.highlightElement_relatedWith(dm);
	                	}
	                } else {
	                	 if (e.getComponent().isEnabled() && e.getButton() == MouseEvent.BUTTON3){
	                		 int index = ((JList)(e.getComponent())).locationToIndex(new Point(e.getX(),e.getY()));
	                		 ((JList)(e.getComponent())).setSelectedIndex(index);
	                	 	 DebugMessage dm = (DebugMessage)currentViewArea.getSelectedValue();
	                	 	 if(dm.getPriority() != DebugConstants.PriorityType.DEFAULTS.priorityCode &&
	                	 			dm.getPriority() != DebugConstants.PriorityType.MINOR_EMPTY.priorityCode	 ) {
	                	 		if(singleMutantFrame==null)  MainGui.ackMenuItem.setEnabled(false);
	                	 	 } else  {
	                	 		if(singleMutantFrame==null) {
	                	 			MainGui.ackMenuItem.setEnabled(true);
	                	 			MainGui.popupDebugMessageActions.show(e.getComponent(), e.getX(), e.getY());
	                	 			MainGui.toBeAck_debugMessage = dm;
	                	 		}
	                	 	 }
	                	}
	                }
	            }
	        });
	        
    	 
         treeView.setMinimumSize(new Dimension(200,190));
        
         textArea.setEditable(false);
         textArea.setWrapStyleWord(true);
         textArea.setFont(MainGui.customFont);
         
         JScrollPane scrollPane = new JScrollPane();
         scrollPane.setViewportView(currentViewArea);
         scrollPane.setPreferredSize(new Dimension(150,150));
         currentViewArea.setPreferredSize(new Dimension(100,100));
         
         JScrollPane scrollPane2 = new JScrollPane();
         scrollPane2.setViewportView(textArea);
         
    	JSplitPane jSplitPane = new JSplitPane();
    	JSplitPane jSplitPane_right = new JSplitPane();
		jSplitPane.setLeftComponent(treeView);
		jSplitPane.setRightComponent(jSplitPane_right);
		jSplitPane_right.setLeftComponent(scrollPane);
		jSplitPane_right.setDividerLocation(0.5);
		jSplitPane_right.setOneTouchExpandable(true);
		jSplitPane_right.setRightComponent(scrollPane2);
		jSplitPane_right.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		jSplitPane.setDividerLocation(0.65);
		jSplitPane.setOneTouchExpandable(true);
		
		this.setLayout(new BorderLayout());
		add(jSplitPane, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        
        
    }
    
    public void selectDebugMessage(DebugMessage maxPriority) {
    	treeView.selectDebugMessagePriority(maxPriority.getPriority());
    	currentViewArea.setSelectedValue(maxPriority, true);
    }

	public void expandAll() {
    	treeView.expandAll();
    }

    public void updateTreeView() {
    	treeView.updateTreeView();
    }
    
    public void updateDebugMessages(HashMap<String, DebugMessage> dms) {
    	treeView.updateDebugMessages(dms);
    }

    class TreeView extends JScrollPane implements TreeExpansionListener {
        Dimension minSize = new Dimension(200, 200);
        JTree tree;
		private HashMap<Double, Integer> priority_rowIndex_hash;
		private HashMap<String, DebugMessage> debugMessages;
    
        public void expandAll() {
        	for (int i = 0; i < tree.getRowCount(); i++) {
        		tree.expandRow(i);
        	}
        }
        
        public void selectDebugMessagePriority(double priority) {
			tree.setSelectionRow(priority_rowIndex_hash.get(priority));
		}

		public TreeView() {
            TreeNode rootNode = createNodes();
            tree = new JTree(rootNode);
            tree.addTreeExpansionListener(this);
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            //Listen for when the selection changes.
            tree.addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(TreeSelectionEvent arg0) {
					//Returns the last path element of the selection.
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)  tree.getLastSelectedPathComponent();
					    if (node == null)	    return;

					    node.getUserObject();
					    //if (node.isLeaf()) {
					    //textArea.append("Selected: " + arg0.getPath().getLastPathComponent()+System.getProperty("line.separator"));
					    printSelectedDebugMessages((DefaultMutableTreeNode) arg0.getPath().getLastPathComponent());
				}

			});

            setViewportView(tree);
        }

        private TreeNode createNodes() {
            DefaultMutableTreeNode root;
           
            root = new DefaultMutableTreeNode("Messages");
            priority_rowIndex_hash = new HashMap<Double, Integer>();
            priority_rowIndex_hash.put(new Double(-1), new Integer(0));
         
            DefaultMutableTreeNode major = new DefaultMutableTreeNode(DebugConstants.PriorityType.MAJOR.getDescription() +" ("+ 
            														MainGui.getDebugMessages(DebugConstants.PriorityType.MAJOR.priorityCode).size() 
            														+")");
            priority_rowIndex_hash.put(new Double(DebugConstants.PriorityType.MAJOR.priorityCode), new Integer(1));
            root.add(major);
            
            DefaultMutableTreeNode pars = new DefaultMutableTreeNode(DebugConstants.PriorityType.PARSING.getDescription() +" ("+ 
					MainGui.getDebugMessages(DebugConstants.PriorityType.PARSING.priorityCode).size() 
					+")");
            major.add(pars);
            priority_rowIndex_hash.put(new Double(DebugConstants.PriorityType.PARSING.priorityCode), new Integer(2));
              
            DefaultMutableTreeNode inc = new DefaultMutableTreeNode(DebugConstants.PriorityType.INCONSISTENCIES.getDescription() +" ("+ 
					MainGui.getDebugMessages(DebugConstants.PriorityType.INCONSISTENCIES.priorityCode).size() 
					+")");
            major.add(inc);
            priority_rowIndex_hash.put(new Double(DebugConstants.PriorityType.INCONSISTENCIES.priorityCode), new Integer(3));
            
            DefaultMutableTreeNode miss = new DefaultMutableTreeNode(DebugConstants.PriorityType.MISSING.getDescription() +" ("+ 
					MainGui.getDebugMessages(DebugConstants.PriorityType.MISSING.priorityCode).size() 
					+")");
            major.add(miss);
            priority_rowIndex_hash.put(new Double(DebugConstants.PriorityType.MISSING.priorityCode), new Integer(4));
            
            
            DefaultMutableTreeNode empty = new DefaultMutableTreeNode(DebugConstants.PriorityType.EMPTY.getDescription() +" ("+ 
					MainGui.getDebugMessages(DebugConstants.PriorityType.EMPTY.priorityCode).size() 
					+")");
            major.add(empty);
            priority_rowIndex_hash.put(new Double(DebugConstants.PriorityType.EMPTY.priorityCode), new Integer(5));
            
            DefaultMutableTreeNode def = new DefaultMutableTreeNode(DebugConstants.PriorityType.DEFAULTS.getDescription() +" ("+ 
					MainGui.getDebugMessages(DebugConstants.PriorityType.DEFAULTS.priorityCode).size() 
					+")");
            root.add(def);
            priority_rowIndex_hash.put(new Double(DebugConstants.PriorityType.DEFAULTS.priorityCode), new Integer(6));
            
            
            DefaultMutableTreeNode minor = new DefaultMutableTreeNode(DebugConstants.PriorityType.MINOR.getDescription() +" ("+ 
					MainGui.getDebugMessages(DebugConstants.PriorityType.MINOR.priorityCode).size() 
					+")");
            root.add(minor);
            priority_rowIndex_hash.put(new Double(DebugConstants.PriorityType.MINOR.priorityCode), new Integer(7));
             

            DefaultMutableTreeNode min_importIssues = new DefaultMutableTreeNode(DebugConstants.PriorityType.MINOR_IMPORT_ISSUES.getDescription() +" ("+ 
					MainGui.getDebugMessages(DebugConstants.PriorityType.MINOR_IMPORT_ISSUES.priorityCode).size() 
					+")");
            minor.add(min_importIssues);
            priority_rowIndex_hash.put(new Double(DebugConstants.PriorityType.MINOR_IMPORT_ISSUES.priorityCode), new Integer(8));
            
            
            DefaultMutableTreeNode min_missing = new DefaultMutableTreeNode(DebugConstants.PriorityType.MINOR_EMPTY.getDescription() +" ("+ 
					MainGui.getDebugMessages(DebugConstants.PriorityType.MINOR_EMPTY.priorityCode).size() 
					+")");
            minor.add(min_missing);
            priority_rowIndex_hash.put(new Double(DebugConstants.PriorityType.MINOR_EMPTY.priorityCode), new Integer(9));
            

             DefaultMutableTreeNode sim = new DefaultMutableTreeNode(DebugConstants.PriorityType.DUPLICATES.getDescription() +" ("+ 
 					MainGui.getDebugMessages(DebugConstants.PriorityType.DUPLICATES.priorityCode).size() 
 					+")");
             minor.add(sim);
                  priority_rowIndex_hash.put(new Double(DebugConstants.PriorityType.DUPLICATES.priorityCode), new Integer(10));
            
            
           /* DefaultMutableTreeNode sim = new DefaultMutableTreeNode(DebugConstants.PriorityType.SIMILARITY.getDescription() +" ("+ 
					MainGui.getDebugMessages(DebugConstants.PriorityType.SIMILARITY.priorityCode).size() 
					+")");
            minor.add(sim);
                 priority_rowIndex_hash.put(new Double(DebugConstants.PriorityType.SIMILARITY.priorityCode), new Integer(10));
       */
            
            return root;
        }
    
        public Dimension getMinimumSize() {
            return minSize;
        }

      
        // Required by TreeExpansionListener interface.
        public void treeExpanded(TreeExpansionEvent e) {
           // saySomething("Tree-expanded event detected", e);
        }

        // Required by TreeExpansionListener interface.
        public void treeCollapsed(TreeExpansionEvent e) {
            //saySomething("Tree-collapsed event detected", e);
        }
        
        public void updateTreeView() {
        	textArea.setText("");
        	DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
            visitAllNodes(root);
        	DefaultMutableTreeNode sel = (DefaultMutableTreeNode)  tree.getLastSelectedPathComponent();
            if(sel != null)printSelectedDebugMessages(sel);
        }
        
        public void updateDebugMessages(HashMap<String, DebugMessage> dms) {
        	this.debugMessages = new HashMap<String, DebugMessage>();
        	this.debugMessages.putAll(dms);
        }
        
        public void visitAllNodes(DefaultMutableTreeNode node) {
  
         Iterator it =  debugMessages.keySet().iterator();
         if(node.toString().indexOf("(") != -1) { //root has no number of messages and should not be processed
        	 String priorityDescr = node.toString().substring(0, node.toString().indexOf("(")-1);
        	 int count = 0;
        	 String partial_key_string = new Double(DebugConstants.PriorityType.getIndex(priorityDescr)).toString();
     		if(partial_key_string.endsWith(".0")) { partial_key_string = partial_key_string.substring(0, partial_key_string.indexOf(".0"));}
     		while(it.hasNext()) {
     			String key = (String) it.next();
     			if(key.contains("@"+partial_key_string+".")  || key.contains("@"+partial_key_string+"_")){
        	      			 count++; 
        		 } 
        	 }
        	 node.setUserObject(priorityDescr + " (" + count + ")");
         }
         if (node.getChildCount() >= 0) {
                for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                	DefaultMutableTreeNode n = (DefaultMutableTreeNode)e.nextElement();
                    visitAllNodes(n);
                }
            }
         
         
        }
        
        
        private void printSelectedDebugMessages(DefaultMutableTreeNode selectedNode) {
                	Iterator it = debugMessages.keySet().iterator();
        	listModel.clear();
            if(selectedNode.toString().indexOf("(") != -1) { //root has no number of messages and should not be processed
           	 String priorityDescr = selectedNode.toString().substring(0, selectedNode.toString().indexOf("(")-1);
           	 Vector<DebugMessage> sorted = new Vector<DebugMessage>();
           	 while(it.hasNext()) {
           		 String key = (String) it.next();
           		 if(key.contains("@"+DebugConstants.PriorityType.getIndex(priorityDescr)+"_")) {
           			sorted.add((DebugMessage)debugMessages.get(key));
           			 
           		 } 
           	 }
           	 Collections.sort(sorted);
           	 for (DebugMessage x : sorted) {listModel.addElement(x);  
           	}
            currentViewArea.revalidate();
            textArea.setText("");
            /*MouseListener popupListener = new PopupListener();
            currentViewArea.addMouseListener(popupListener);*/
		}

    }
}
    
}