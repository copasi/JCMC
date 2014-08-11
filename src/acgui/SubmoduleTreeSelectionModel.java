/**
 * 
 */
package acgui;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.RowMapper;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * @author T.C. Jones
 *
 */
public class SubmoduleTreeSelectionModel implements TreeSelectionModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TreeSelectionModel selectionModel;
	private ArrayList<ModuleDefinition> nonSelectable;
	
	public SubmoduleTreeSelectionModel(ArrayList<ModuleDefinition> iNonSelectable)
	{
		selectionModel = new DefaultTreeSelectionModel();
		nonSelectable = new ArrayList<ModuleDefinition>(iNonSelectable);
		this.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}
	
	private boolean canPathBeAdded(TreePath path)
	{
		DefaultMutableTreeNode node;
		ModuleDefinition definition;
		
		if (path != null)
		{
			node = (DefaultMutableTreeNode)(path.getLastPathComponent());
			if (node != null)
			{
				definition = (ModuleDefinition)node.getUserObject();
				if (definition != null)
				{
					if (!nonSelectable.contains(definition))
					{
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private TreePath[] getFilteredPaths(TreePath[] paths)
	{
		ArrayList<TreePath> approvedPaths = new ArrayList<TreePath>();
		for (TreePath currentPath : paths)
		{
			if (canPathBeAdded(currentPath))
			{
				approvedPaths.add(currentPath);
			}
		}
		
		return approvedPaths.toArray(new TreePath[approvedPaths.size()]);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void addPropertyChangeListener(PropertyChangeListener arg0)
	{
		selectionModel.addPropertyChangeListener(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#addSelectionPath(javax.swing.tree.TreePath)
	 */
	@Override
	public void addSelectionPath(TreePath path)
	{
		if (canPathBeAdded(path))
		{
			selectionModel.addSelectionPath(path);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#addSelectionPaths(javax.swing.tree.TreePath[])
	 */
	@Override
	public void addSelectionPaths(TreePath[] paths)
	{
		paths = getFilteredPaths(paths);
		selectionModel.addSelectionPaths(paths);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#addTreeSelectionListener(javax.swing.event.TreeSelectionListener)
	 */
	@Override
	public void addTreeSelectionListener(TreeSelectionListener arg0)
	{
		selectionModel.addTreeSelectionListener(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#clearSelection()
	 */
	@Override
	public void clearSelection()
	{
		selectionModel.clearSelection();
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#getLeadSelectionPath()
	 */
	@Override
	public TreePath getLeadSelectionPath()
	{
		return selectionModel.getLeadSelectionPath();
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#getLeadSelectionRow()
	 */
	@Override
	public int getLeadSelectionRow()
	{
		return selectionModel.getLeadSelectionRow();
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#getMaxSelectionRow()
	 */
	@Override
	public int getMaxSelectionRow()
	{
		return selectionModel.getMaxSelectionRow();
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#getMinSelectionRow()
	 */
	@Override
	public int getMinSelectionRow()
	{
		return selectionModel.getMinSelectionRow();
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#getRowMapper()
	 */
	@Override
	public RowMapper getRowMapper()
	{
		return selectionModel.getRowMapper();
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#getSelectionCount()
	 */
	@Override
	public int getSelectionCount()
	{
		return selectionModel.getSelectionCount();
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#getSelectionMode()
	 */
	@Override
	public int getSelectionMode()
	{
		return selectionModel.getSelectionMode();
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#getSelectionPath()
	 */
	@Override
	public TreePath getSelectionPath()
	{
		return selectionModel.getSelectionPath();
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#getSelectionPaths()
	 */
	@Override
	public TreePath[] getSelectionPaths()
	{
		return selectionModel.getSelectionPaths();
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#getSelectionRows()
	 */
	@Override
	public int[] getSelectionRows()
	{
		return selectionModel.getSelectionRows();
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#isPathSelected(javax.swing.tree.TreePath)
	 */
	@Override
	public boolean isPathSelected(TreePath arg0)
	{
		return selectionModel.isPathSelected(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#isRowSelected(int)
	 */
	@Override
	public boolean isRowSelected(int arg0)
	{
		return selectionModel.isRowSelected(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#isSelectionEmpty()
	 */
	@Override
	public boolean isSelectionEmpty()
	{
		return selectionModel.isSelectionEmpty();
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void removePropertyChangeListener(PropertyChangeListener arg0)
	{
		selectionModel.removePropertyChangeListener(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#removeSelectionPath(javax.swing.tree.TreePath)
	 */
	@Override
	public void removeSelectionPath(TreePath arg0)
	{
		selectionModel.removeSelectionPath(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#removeSelectionPaths(javax.swing.tree.TreePath[])
	 */
	@Override
	public void removeSelectionPaths(TreePath[] arg0)
	{
		selectionModel.removeSelectionPaths(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#removeTreeSelectionListener(javax.swing.event.TreeSelectionListener)
	 */
	@Override
	public void removeTreeSelectionListener(TreeSelectionListener arg0)
	{
		selectionModel.removeTreeSelectionListener(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#resetRowSelection()
	 */
	@Override
	public void resetRowSelection()
	{
		selectionModel.resetRowSelection();
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#setRowMapper(javax.swing.tree.RowMapper)
	 */
	@Override
	public void setRowMapper(RowMapper arg0)
	{
		selectionModel.setRowMapper(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#setSelectionMode(int)
	 */
	@Override
	public void setSelectionMode(int arg0)
	{
		selectionModel.setSelectionMode(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#setSelectionPath(javax.swing.tree.TreePath)
	 */
	@Override
	public void setSelectionPath(TreePath path)
	{
		if (canPathBeAdded(path))
		{
			selectionModel.setSelectionPath(path);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeSelectionModel#setSelectionPaths(javax.swing.tree.TreePath[])
	 */
	@Override
	public void setSelectionPaths(TreePath[] paths)
	{
		paths = getFilteredPaths(paths);
		selectionModel.setSelectionPaths(paths);
	}
}
