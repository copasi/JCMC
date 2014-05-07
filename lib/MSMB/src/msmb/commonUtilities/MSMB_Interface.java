package msmb.commonUtilities;

import java.util.Vector;

import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;

import msmb.model.Compartment;
import msmb.model.GlobalQ;
import msmb.model.Species;

import java.awt.*;

public interface MSMB_Interface {
	public JTabbedPane getMSMB_MainTabPanel();
	public JMenuBar getMSMB_MenuBar();
	
	public void loadFromCopasiModelName(String copasiModelName, boolean displayUneditable)  throws Exception;
	public boolean saveToCopasiModelName(String copasiModelName);
	
	public void loadFromMSMB(byte[] msmbByteArray, boolean displayUneditable);
	public byte[]  saveToMSMB();
	
	
	public Vector<String> getMSMB_listOfSpecies();
	public Vector<String> getMSMB_listOfGlobalQuantities();
	public Vector<String> getMSMB_listOfCompartments();
	
	
	public Species getMSMB_getSpecies(String name);
	public GlobalQ getMSMB_getGlobalQuantity(String name);
	public Compartment getMSMB_getCompartment(String name);

	public void addSpecies(String name, String initialQuantity, String compartment) throws Exception;
	public void addGlobalQuantity(String name, String initialQuantity) throws Exception;

	public void removeSpecies(String name);
	public void removeGlobalQuantity(String name);

	/*public void addInvisibleSpecies(String name, String initialQuantity, String compartment) throws Exception;
	public Vector<String> getMSMB_listOfInvisibleSpecies();
	public void removeInvisibleSpecies(String name);
	 */
	
	
	public String getDefault_CompartmentName();
	public String getDefault_SpeciesInitialQuantity();
	public String getDefault_GlobalQInitialQuantity();
	

	public Font getCustomFont();
	public void addChangeListener(ChangeListener c, MSMB_Element element);	
	
	
	public void setModelName(String newModelName);
	public String getModelName();
	
	public void setModelDefinition(String newModelDefinition);
	public String getModelDefinition();
	
	public void highlightElement(MSMB_Element element, String name);
	
	//0 = OK, 1 = major issues, 2 = minor issues	
	public int validateMSMB();
	
}
