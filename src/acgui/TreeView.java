package acgui;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * @author Thomas
 *
 */
public class TreeView extends JPanel implements TreeSelectionListener
{

	/**
	 * 
	 */
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
		tree.setEditable(false);
		tree.setToggleClickCount(2);
		// tree.setBackground(Color.WHITE);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(false);

		tree.setCellRenderer(new ACTreeCellRenderer(tree.getFont()));
		tree.addTreeSelectionListener(this);
		treeModel.addTreeModelListener(new ACTreeModelListener());
		installListeners();
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

	public DefaultMutableTreeNode getRootNode()
	{
		return rootNode;
	}
	
	public void clear()
	{
		DefaultMutableTreeNode node;
		while(treeModel.getChildCount(rootNode) > 0)
		{
			node = (DefaultMutableTreeNode)treeModel.getChild(rootNode, 0);
			treeModel.removeNodeFromParent(node);
		}
	}
	
	public void refreshTree()
	{
		//treeModel.reload();
		tree.repaint();
	}
	
	public void createNode(Module mod)
	{
		DefaultMutableTreeNode child = new DefaultMutableTreeNode(mod);
		mod.setTreeNode(child);
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
		/*
		if(AC_GUI.activeModule == null)
		{
			if(mod.getTreeNode() == null)
			{
				child = new DefaultMutableTreeNode(mod);
				mod.setTreeNode(child);
			}
			else
			{
				child = mod.getTreeNode();
			}
			
			//treeModel.setRoot(rootNode);
			//tree.setRootVisible(true);
			treeModel.insertNodeInto(child, rootNode, rootNode.getChildCount());
			refreshTree();
			return;
		}
		*/
		if (mod.getParent() == null)
		{
			parent = rootNode;
		} else if (mod.getParent().getTreeNode() == null)
		{
			parent = rootNode;
		}
		else
		{
			parent = mod.getParent().getTreeNode();
		}
		/*
		// find the parent tree node
		parent = mod.getParent().getTreeNode();
		if (parent == null)
		{
			// no node is selected, choose the root by default
			parent = rootNode;
		}
		*/
		//child = new DefaultMutableTreeNode(mod);
		//mod.setTreeNode(child);

		child = mod.getTreeNode();
		// insert the child node under the parent
		treeModel.insertNodeInto(child, parent, parent.getChildCount());
		// make sure the new node is visible
		tree.scrollPathToVisible(new TreePath(child.getPath()));
		// make the new node the currently selected node
		//tree.setSelectionPath(new TreePath(child.getPath()));

		//mod.setTreeNode(child);
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

	/**
	 * Set the highlighted node on the tree.
	 * @param node the node to be highlighted
	 */
	public void setSelected(DefaultMutableTreeNode node)
	{
		tree.setSelectionPath(new TreePath(node.getPath()));
	}
	
	private JPopupMenu createPopupMenu(final Point pt, final Object cell) {
		JPopupMenu menu = new JPopupMenu();
		
		menu.add(new AbstractAction("Edit Name") {
			public void actionPerformed(ActionEvent e) {
				//tree.setEditable(true);
				//tree.startEditingAtPath(tree.getSelectionPath());
				AC_Utility.promptUserEditModuleName(AC_GUI.selectedModule.getName());
			}
		});
		
		if(AC_GUI.drawingBoard.getActiveModule() == AC_GUI.selectedModule)
		{
			menu.add(new AbstractAction("Save Module") {
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(null,
							"User can save a module (in internal datastructure) anytime when the module is still loaded for editing (not yet implemented).");
					}
			});
		}
		else
		{
			menu.add(new AbstractAction("Load Module") {
				public void actionPerformed(ActionEvent e) {
					AC_GUI.changeActiveModule(AC_GUI.selectedModule);
					//AC_GUI.drawingBoard.changeModule(AC_GUI.selectedModule);
					//refreshTree();
				}
			});
		}
		
		menu.add(new AbstractAction("Properties") {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
					"Will show a message box containing some basic information about the module, for example, list of the ports (not yet implemented).");
			}
		});
		
		return menu;
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
			AC_GUI.setSelectedModule(node);
		}

		//System.out.println("Tree node " + node.toString() + " selected.");
	}

	private void installListeners()
	{
		// right-click listener
		tree.addMouseListener( new MouseAdapter() {
			public void mousePressed(MouseEvent e)
			{
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1)
				{
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
					AC_GUI.setSelectedModule(node);
					if(SwingUtilities.isRightMouseButton(e))
					{
						JPopupMenu menu = createPopupMenu(e.getPoint(), AC_GUI.selectedModule);
						menu.show(tree, e.getX(), e.getY());
						//System.out.println("Selected node: " + node.toString());
					}
					else if (e.getClickCount() == 2)
					{
						//DefaultMutableTreeNode node = (DefaultMutableTreeNode)(path.getLastPathComponent());
						//Module mod = AC_GUI.moduleList.findModule(node);
						//AC_GUI.drawingBoard.changeModule(mod);
						
					}
					
				}
			}
		});
		/*
		DefaultTreeCellEditor cellEditor = new DefaultTreeCellEditor(tree, (DefaultTreeCellRenderer) tree.getCellRenderer());
		cellEditor.addCellEditorListener(new CellEditorListener() {
			public void editingCanceled(ChangeEvent event) {}
			public void editingStopped(ChangeEvent event) {}
		});
		*/
	}
}
