package acgui;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A list of modules.
 * @author T.C. Jones
 * @version July 6, 2012
 */
public class ModuleList
{
	ArrayList<Module> list;

	public ModuleList()
	{
		list = new ArrayList<Module>();
	}
	
	public void add(Module item)
	{
		list.add(item);
	}
	
	public void remove(Module item)
	{
		list.remove(item);
	}
	
	public Module findModule(DefaultMutableTreeNode node)
	{
		Module current;
		Iterator<Module> iterator = list.iterator();
		
		while(iterator.hasNext())
		{
			current = iterator.next();
			if(current.getTreeNode() == node)
			{
				return current;
			}
		}
		return null;
	}
	
	public int getSize()
	{
		return list.size();
	}
}
