package acgui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * The tree panel in the aggregation connector.
 * @author T.C. Jones
 * @version June 29, 2012
 */
public class TreeView extends JPanel implements TreeSelectionListener
{
	private static final long serialVersionUID = 1L;
	private DefaultMutableTreeNode rootNode;
	private DefaultTreeModel treeModel;
	private JTree tree;
	
	/**
	 * Constructs the tree.
	 */
	public TreeView()
	{
		rootNode = new DefaultMutableTreeNode("RootNode");
		treeModel = new DefaultTreeModel(rootNode);
		tree = new JTree(treeModel);
		tree.setRootVisible(true);
		//tree.setEditable(true);
		//tree.setBackground(Color.WHITE);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		
		tree.addTreeSelectionListener(this);
		treeModel.addTreeModelListener(new ACTreeModelListener());
		this.setLayout(new BorderLayout());
		this.setVisible(true);
        this.add(tree);
	}
	
	/**
	 * Return the tree.
	 * @return the tree
	 */
	public JTree getTree()
	{
		return tree;
	}
	
	/**
	 * Add a child to the root of the tree.
	 * @param name the name of the child
	 */
	public DefaultMutableTreeNode addChild(String name)
	{
		DefaultMutableTreeNode parent;
		DefaultMutableTreeNode child;
		
		
		
		TreePath parentPath = tree.getSelectionPath();
		
		if (parentPath == null)
		{
			parent = rootNode;
		}
		else
		{
			parent = (DefaultMutableTreeNode)(parentPath.getLastPathComponent());
		}
		//name = name.concat(Integer.toString(parent.getChildCount()));
		child = new DefaultMutableTreeNode(name);
		
		treeModel.insertNodeInto(child, parent, parent.getChildCount());
		return child;
	}

	/**
	 * Remove the currently selected node from the tree.
	 */
	public DefaultMutableTreeNode removeChild()
	{
		TreePath currentSelection = tree.getSelectionPath();
		DefaultMutableTreeNode currentNode = null;
        if (currentSelection != null) {
            currentNode = (DefaultMutableTreeNode)(currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
            if (parent != null) {
                treeModel.removeNodeFromParent(currentNode);
            }
            else
            {
            	JOptionPane.showMessageDialog(null, "Cannot remove the RootNode.");
            }
        }
        else
        {
        	// No module was selected.
        	JOptionPane.showMessageDialog(null, "Please select a module from the tree to remove.");
        }
        
        return currentNode;
	}
	
	/**
	 * A tree node has been selected.
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 * @param event the treeselectionevent
	 */
	@Override
	public void valueChanged(TreeSelectionEvent event)
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		if (node == null)
		{
			return;
		}
		//System.out.println("Tree node " + node.toString() + " selected.");
	}
}
