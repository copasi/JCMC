package acgui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A list of modules.
 * @author T.C. Jones
 * @version July 6, 2012
 */
public class ModuleList
{
	ArrayList<Module> list;

	/**
	 * Construct the module list.
	 */
	public ModuleList()
	{
		list = new ArrayList<Module>();
	}

	/**
	 * Add the given module to the list.
	 * @param item the module to be added
	 */
	public void add(Module mod)
	{
		list.add(mod);
	}

	/**
	 * Remove the given module, and its children, from the list.
	 * @param item the module to be removed
	 */
	public void remove(Module mod)
	{
		removeChildren(mod);

		// Remove the module from this list
		list.remove(mod);
	}

	/**
	 * Find the module in the list represented by the given tree node.
	 * @param node the tree node of the module to find
	 * @return the module represented by the given tree node
	 */
	public Module findModule(DefaultMutableTreeNode node)
	{
		Module current;
		Iterator<Module> iterator = list.iterator();

		while (iterator.hasNext())
		{
			current = iterator.next();
			if (current.getTreeNode() == node)
			{
				return current;
			}
		}
		return null;
	}

	/**
	 * Find the module in the list represented by the given drawing cell.
	 * @param cell the drawing cell of the module to find
	 * @return the module represented by the given drawing cell
	 */
	public Module findModule(Object cell)
	{
		Module current;
		Iterator<Module> iterator = list.iterator();

		while (iterator.hasNext())
		{
			current = iterator.next();
			if (current.getDrawingCell() == cell)
			{
				return current;
			}
		}
		return null;
	}

	/**
	 * Get the current size of the module list.
	 * @return the current size of the module list
	 */
	public int getSize()
	{
		return list.size();
	}

	public void printList()
	{
		Module current;
		Iterator<Module> iterator = list.iterator();
		System.out.println("Module List:");
		while (iterator.hasNext())
		{
			current = iterator.next();
			System.out.println(current.toString());
		}
		System.out.println();
	}

	/**
	 * Return a list iterator for the module list.
	 * @return the list iterator for the module list
	 */
	public ListIterator<Module> getListIterator()
	{
		return list.listIterator();
	}

	private void removeChildren(Module mod)
	{
		Module child;
		// Remove the module's children		
		ArrayList<Module> children = mod.getChildren();
		if (children.size() > 0)
		{
			ListIterator<Module> iterator = children.listIterator();
			while (iterator.hasNext())
			{
				child = iterator.next();
				removeChildren(child);
				list.remove(child);
			}
		}
	}
}
