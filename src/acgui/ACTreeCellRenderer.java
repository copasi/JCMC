/**
 * 
 */
package acgui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Cell renderer for tree nodes.
 * @author T.C. Jones
 * @version January 21, 2013
 */
public class ACTreeCellRenderer extends DefaultTreeCellRenderer
{
	Font normalFont, boldFont;
    private int baseSize = -1;
    
    public ACTreeCellRenderer(Font thisFont)
    {
        super();
        normalFont = thisFont;
        boldFont = normalFont.deriveFont(Font.BOLD);
    }
    
    /**
     * Return the cell renderer component.
     * @return the cell renderer component
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
 
        DefaultMutableTreeNode currentNode =  (DefaultMutableTreeNode) value;
        Module currentModule = (Module)currentNode.getUserObject();
        if (currentModule == AC_GUI.activeModule)
        {
            setFont(boldFont);
        }
        else
        {
        	setFont(normalFont);
        }
        
       return this;
    }

    /**
     * Recalculate the width of this JLabel. 
     * @return the preferred dimension
     */
	public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		FontMetrics fm = getFontMetrics(getFont());
		char[] chars = getText().toCharArray();
		
		int w = getIconTextGap() + 20;
		for (char ch : chars)  {
			w += fm.charWidth(ch);
		}
		w += getText().length();
		dim.width = w;
	return dim;
}
}
