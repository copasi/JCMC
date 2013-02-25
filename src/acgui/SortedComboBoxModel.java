/**
 * 
 */
package acgui;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;

/**
 * Custom model to make sure the items are stored in a sorted order.
 * The default is to sort in the natural order of the item, but a
 * Comparator can be used to customize the sort order.
 *
 * The data is initially sorted before the model is created. Any updates
 * to the model will cause the items to be inserted in sorted order.
 * 
 * @author T.C. Jones
 * @version Febraury 4, 2013
 */
public class SortedComboBoxModel extends DefaultComboBoxModel<String>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Comparator<String> comparator;
	
	/**
	 * Construct the model.
	 */
	public SortedComboBoxModel()
	{
		super();
	}
	
	/**
	 * Construct the model with the given parameters.
	 * @param items list of Strings
	 * @param comp Comparator to compare the Strings
	 */
	public SortedComboBoxModel(Vector<String> items, Comparator<String> comp)
	{
		super(sort(items, comp));
		comparator = comp;
		//setSelectedItem(items.elementAt(0));
    }
	
	/**
	 * Add an element to the model.
	 * @param element the String to add
	 */
	@Override
	public void addElement(String element)
	{
		insertElementAt(element, 0);
	}
	
	/**
	 * Insert an element into the model at the appropriate
	 * index so the model stays in sorted order.
	 * @param element the String to insert
	 * @param index index to add the element, this is ignored
	 */
	@Override
	public void insertElementAt(String element, int index)
	{
		int size = getSize();
		
		// Determine where to insert the element to keep the model in sorted order
		for (index = 0; index < size; index++)
		{
			if (comparator != null)
			{
				String str = getElementAt(index);
				
				if (comparator.compare(str,  element) > 0)
				{
					break;
				}
			}
			else
			{
				Comparable<String> c = (Comparable<String>)getElementAt(index);
				if (c.compareTo(element) > 0)
				{
					break;
				}
			}
		}
		
		super.insertElementAt(element,  index);
	}
	
	/**
	 * Sort the given Vector according to the given Comparator.
	 * If the Comparator is null, sort in the natural order of
	 * the items.
	 * @param items list of Strings
	 * @param comp Comparator to compare the Strings
	 * @return the list of Strings in sorted order
	 */
	public static Vector<String> sort(Vector<String> items, Comparator<String> comp)
	{
		Collections.sort(items, comp);
		return items;
	}
}
