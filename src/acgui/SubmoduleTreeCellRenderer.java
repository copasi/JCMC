/**
 * 
 */
package acgui;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * @author T.C. Jones
 *
 */
public class SubmoduleTreeCellRenderer extends DefaultTreeCellRenderer
{

	private ArrayList<ModuleDefinition> nonSelectable;
	
	public SubmoduleTreeCellRenderer(ArrayList<ModuleDefinition> iNonSelectable)
	{
		nonSelectable = new ArrayList<ModuleDefinition>(iNonSelectable);
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
 
        DefaultMutableTreeNode currentNode =  (DefaultMutableTreeNode) value;
        ModuleDefinition definition = (ModuleDefinition)currentNode.getUserObject();
        if (nonSelectable.contains(definition))
        {
        	this.setEnabled(false);
        }
        return this;
	}
}
