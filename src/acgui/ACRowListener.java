package acgui;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Listener for tables in the ModelBuilder panel.
 * @author T.C. Jones
 * @version June 24, 2013
 */
public class ACRowListener implements ListSelectionListener
{

	/**
	 * Invoked when a row in the ModelBuilder panel has been selected.
	 * @param lse the list selection event
	 */
	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		if (lse.getValueIsAdjusting())
		{
            return;
        }
		int tabSelected = AC_GUI.modelBuilder.getSelectedTabIndex();
		int rowSelected = lse.getFirstIndex();
		System.out.println("Selected tab: " + tabSelected + "........Selected row: " + rowSelected);

	}

	/*
	@Override
	public void tableChanged(TableModelEvent tme)
	{
		int tabSelected = AC_GUI.modelBuilder.getSelectedTabIndex();
		int rowSelected = tme.getFirstRow();
		System.out.println("Selected tab: " + tabSelected + "......Selected row: " + rowSelected);
		
	}
	*/
	
	/*
	public void mouseClicked(MouseEvent e)
	{
		JTable table = (JTable)e.getSource();
		int tabSelected = AC_GUI.modelBuilder.getSelectedTabIndex();
		int rowSelected = -1;
		
		if (e.getButton() == MouseEvent.BUTTON1)  {
            Point p = e.getPoint();
            rowSelected = table.rowAtPoint(p);
		}
		
		System.out.println("Selected tab: " + tabSelected + "......Selected row: " + rowSelected);
	}
	*/
}
