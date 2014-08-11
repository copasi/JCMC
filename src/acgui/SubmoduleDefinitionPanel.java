/**
 * 
 */
package acgui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * @author T.C. Jones
 *
 */
public class SubmoduleDefinitionPanel extends JPanel 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DefaultMutableTreeNode rootNode;
	private DefaultTreeModel treeModel;
	private JTree tree;
	//private SubmoduleTreeSelectionModel submoduleTreeSelector;
	private ArrayList<ModuleDefinition> nonSelectableList;
	
	public SubmoduleDefinitionPanel(Module rootModule, TreeSelectionListener selectionListener)
	{
		rootNode = new DefaultMutableTreeNode(rootModule.getModuleDefinition());
		nonSelectableList = new ArrayList<ModuleDefinition>();
		createNodes(rootNode);
		populateNonSelectableList();
		//treeModel = new DefaultTreeModel(rootNode);
		tree = new JTree(rootNode);
		tree.setRootVisible(true);
		tree.setEditable(false);
		//tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		//submoduleTreeSelector = new SubmoduleTreeSelectionModel(nonSelectableList);
		tree.setSelectionModel(new SubmoduleTreeSelectionModel(nonSelectableList));
		tree.setCellRenderer(new SubmoduleTreeCellRenderer(nonSelectableList));
		tree.setShowsRootHandles(true);
		tree.addTreeSelectionListener(selectionListener);
		
		this.setLayout(new BorderLayout());
		this.setVisible(true);
		this.add(tree);
	}
	
	private void populateNonSelectableList()
	{
		nonSelectableList.add(AC_GUI.rootModule.getModuleDefinition());
		findNonSelectableSubmodules(AC_GUI.rootModule);
	}
	
	private void findNonSelectableSubmodules(Module module)
	{
		if (module == AC_GUI.activeModule)
		{
			if (!nonSelectableList.contains(module.getModuleDefinition()))
			{
				nonSelectableList.add(module.getModuleDefinition());
			}
		}
		
		if (module.getChildren().size() > 0)
		{
			if (!nonSelectableList.contains(module.getModuleDefinition()))
			{
				nonSelectableList.add(module.getModuleDefinition());
			}
		}
		
		ListIterator<Module> submoduleList = module.getChildren().listIterator();
		Module currentModule;
		while (submoduleList.hasNext())
		{
			currentModule = submoduleList.next();
			findNonSelectableSubmodules(currentModule);
		}
	}
	
	private void createNodes(DefaultMutableTreeNode top)
	{
		Module rootModule = AC_GUI.rootModule;
		ListIterator<Module> children = rootModule.getChildren().listIterator();
		while (children.hasNext())
		{
			addNode(top, children.next());
		}
	}
	
	private void addNode(DefaultMutableTreeNode parentNode, Module module)
	{
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(module.getModuleDefinition());
		parentNode.add(node);
		
		ListIterator<Module> children = module.getChildren().listIterator();
		while (children.hasNext())
		{
			addNode(node, children.next());
		}
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
	
	public void clearSelection()
	{
		tree.clearSelection();
	}
}
