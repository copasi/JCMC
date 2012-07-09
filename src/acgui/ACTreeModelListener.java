package acgui;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

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
	public void treeNodesChanged(TreeModelEvent arg0)
	{
		//System.out.println("treeNodesChanged event");

	}

	/**
	 * Invoked after nodes have been inserted into the tree.
	 * @see javax.swing.event.TreeModelListener#treeNodesInserted(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeNodesInserted(TreeModelEvent arg0)
	{
		//System.out.println("treeNodesInserted event");

	}

	/**
	 * Invoked after nodes have been removed from the tree.
	 * @see javax.swing.event.TreeModelListener#treeNodesRemoved(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeNodesRemoved(TreeModelEvent arg0)
	{
		//System.out.println("treeNodesRemoved event");

	}

	/**
	 * Invoked after the tree has drastically changed structure from a given node down.
	 * @see javax.swing.event.TreeModelListener#treeStructureChanged(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeStructureChanged(TreeModelEvent arg0)
	{
		//System.out.println("treeStructureChanged event");

	}
}
