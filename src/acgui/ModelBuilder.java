package acgui;

import javax.swing.JTabbedPane;

import msmb.gui.MSMB_Interface;
import msmb.gui.MainGui;

/**
 * The model builder panel in the aggregation connector.
 * 
 * @author T.C. Jones
 * @version December 17, 2012
 */
public class ModelBuilder
{

	final MSMB_Interface msmb;
	
	/**
	 * Construct the model builder object.
	 */
	public ModelBuilder()
	{
		msmb = new MainGui();
	}
	
	/**
	 * Load the given Copasi model into the model builder.
	 * @param key the unique Copasi key referencing the model
	 */
	public void loadModel(String key)
	{
		try 
		{
			 msmb.importFromCopasiKey(key);
		} catch (Exception e) {
			 //I still don't know which exception I need to push to your part... probably it is enough for me to catch them and
			 //display the usual error message that I already show... but I'm not sure, so I left the throw expception in the
			 //method declaration
			 e.printStackTrace();
		}
	}
	
	/**
	 * Return the MSMB panel.
	 * @return the MSMB panel
	 */
	public JTabbedPane getPanel()
	{
		return msmb.getMSMB_MainTabPanel();
	}
	
	/**
	 * Set the visibility of the MSMB panel.
	 * @param vis the new visibility of the MSMB panel
	 */
	public void setVisible(boolean vis)
	{
		msmb.getMSMB_MainTabPanel().setVisible(vis);
	}
	
	/**
	 * Return the number of species in the current MSMB.
	 * @return the number of species in the current MSMB
	 */
	public int getNumberofSpecies()
	{
		return msmb.getMSMB_numSpecies();
	}
}
