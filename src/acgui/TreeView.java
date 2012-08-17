package acgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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
	 * Construct the tree.
	 */
	public TreeView()
	{
		rootNode = new DefaultMutableTreeNode(null);
		treeModel = new DefaultTreeModel(rootNode);
		tree = new JTree(treeModel);
		tree.setRootVisible(false);
		tree.setEditable(true);
		tree.setToggleClickCount(0);
		// tree.setBackground(Color.WHITE);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);

		tree.addTreeSelectionListener(this);
		treeModel.addTreeModelListener(new ACTreeModelListener());
		installDoubleClickListener();
		this.setLayout(new BorderLayout());
		this.setVisible(true);
		this.add(tree);
	}

	public void setup(Module mod)
	{
		rootNode = new DefaultMutableTreeNode(mod);
		treeModel.setRoot(rootNode);
		tree.setRootVisible(true);
		mod.setTreeNode(rootNode);
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
	 * Add a child to the currently selected node in the tree.
	 * @param mod the submodule to add
	 */
	public void addNode(Module mod)
	{
		DefaultMutableTreeNode parent;
		DefaultMutableTreeNode child;
		/*
				// get the path of the currently selected node
				TreePath parentPath = tree.getSelectionPath();

				if (parentPath == null)
				{
					// no node is selected, choose root by default
					parent = rootNode;
				}
				else
				{
					// set parent as the currently selected node
					parent = (DefaultMutableTreeNode)(parentPath.getLastPathComponent());
				}
		*/
		// find the parent tree node
		parent = mod.getParent().getTreeNode();
		if (parent == null)
		{
			// no node is selected, choose the root by default
			parent = rootNode;
		}

		// create new child node
		child = new DefaultMutableTreeNode(mod);

		// insert the child node under the parent
		treeModel.insertNodeInto(child, parent, parent.getChildCount());
		// make sure the new node is visible
		tree.scrollPathToVisible(new TreePath(child.getPath()));
		// make the new node the currently selected node
		//tree.setSelectionPath(new TreePath(child.getPath()));

		mod.setTreeNode(child);
	}

	/**
	 * Remove the given node from the tree.
	 * @param node the node to be removed
	 */
	public void removeNode(DefaultMutableTreeNode node)
	{
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode)(node.getParent());
		if (parent != null)
		{
			// remove the node from the tree
			treeModel.removeNodeFromParent(node);
		}
		else
		{
			JOptionPane.showMessageDialog(null, "Cannot remove the RootNode.");
		}
		//tree.setSelectionPath(new TreePath(rootNode.getPath()));
	}

	/**
	 * Return the currently selected node in the tree.
	 * @return the currently selected node in the tree
	 */
	public DefaultMutableTreeNode getSelected()
	{
		TreePath currentSelection = tree.getSelectionPath();
		DefaultMutableTreeNode currentNode = null;

		if (currentSelection != null)
		{
			currentNode = (DefaultMutableTreeNode)(currentSelection.getLastPathComponent());
		}
		return currentNode;
	}

	public void setSelected(DefaultMutableTreeNode node)
	{
		TreePath path = new TreePath(node.getPath());
		tree.setSelectionPath(path);
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
		if (node != null)
		{
			//Module mod = AC_GUI.moduleList.findModule(node);
			//AC_GUI.drawingBoard.setSelected(mod.getDrawingCell());
			AC_GUI.currentGUI.setSelectedModule(node);
		}

		//System.out.println("Tree node " + node.toString() + " selected.");
	}
	
	private void installDoubleClickListener()
	{
		tree.addMouseListener( new MouseAdapter() {
			public void mousePressed(MouseEvent e)
			{
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1)
				{
					if (e.getClickCount() == 2)
					{
						//DefaultMutableTreeNode node = (DefaultMutableTreeNode)(path.getLastPathComponent());
						//Module mod = AC_GUI.moduleList.findModule(node);
						//AC_GUI.drawingBoard.changeModule(mod);
						AC_GUI.drawingBoard.changeModule(AC_GUI.selectedModule);
					}
				}
			}
		});
	}
}
