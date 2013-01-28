package acgui;

import org.COPASI.CCopasiDataModel;
import org.COPASI.CCopasiRootContainer;

/**
 * A utility to handle all things Copasi.
 * @author T.C. Jones
 * @version December 17, 2012
 */
public class CopasiUtility
{

	public CopasiUtility()
	{
		CCopasiRootContainer.init();
		if (CCopasiRootContainer.getRoot() == null)
		{
			System.out.println("COPASI not setup correctly.");
			System.exit(0);
		}
	}
	
	public CCopasiDataModel createDataModel()
	{
		return CCopasiRootContainer.addDatamodel();
	}
}
