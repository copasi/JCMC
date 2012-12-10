package acgui;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Listener for any changes to the tree model.
 * @author T.C. Jones
 * @version July 5, 2012
 */
public class ACTreeModelListener implements TreeModelListener
{

	/**
	 * Invoked after a node (or a set of siblings) has changed in some way.
	 * @see javax.swing.event.TreeModelListener#treeNodesChanged(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeNodesChanged(TreeModelEvent tme)
	{
		DefaultMutableTreeNode node;
		node = (DefaultMutableTreeNode)(tme.getTreePath().getLastPathComponent());
		/*
		 * If the event lists children, then the changed node is the child of the node we've already gotten. Otherwise,
		 * the changed node and the specified node are the same.
		 */
		if (tme.getChildren() != null)
		{
			int index = tme.getChildIndices()[0];
			node = (DefaultMutableTreeNode)(node.getChildAt(index));
		}
			
		Module currentModule = AC_GUI.masterModuleList.findModule(node);
		currentModule.setName((String)node.getUserObject());
	}

	/**
	 * Invoked after nodes have been inserted into the tree.
	 * @see javax.swing.event.TreeModelListener#treeNodesInserted(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeNodesInserted(TreeModelEvent tme)
	{
		// System.out.println("treeNodesInserted event");

	}

	/**
	 * Invoked after nodes have been removed from the tree.
	 * @see javax.swing.event.TreeModelListener#treeNodesRemoved(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeNodesRemoved(TreeModelEvent tme)
	{
		//System.out.println("treeNodesRemoved event.");

	}

	/**
	 * Invoked after the tree has drastically changed structure from a given node down.
	 * @see javax.swing.event.TreeModelListener#treeStructureChanged(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeStructureChanged(TreeModelEvent tme)
	{
		// System.out.println("treeStructureChanged event");

	}
}
