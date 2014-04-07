/**
 * 
 */
package acgui;

import java.util.Comparator;

/**
 * A comparator for Ref Names.
 * @author T.C. Jones
 * @version March 4, 2013
 */
public class RefNameComparator implements Comparator<String>
{

	/**
	 * 
	 */
	public RefNameComparator()
	{
		
	}

	@Override
	public int compare(String arg0, String arg1)
	{
		return arg0.compareToIgnoreCase(arg1);
	}
}
