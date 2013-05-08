package acgui;

import org.COPASI.CCopasiDataModel;
import org.COPASI.CCopasiRootContainer;
import org.COPASI.DataModelVector;

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
	
	public void clear()
	{
		CCopasiRootContainer.getDatamodelList().clear();
		System.out.println("Number of models in the CCopasiRootContainer: " + CCopasiRootContainer.getDatamodelList().size());
	}
	
	public CCopasiDataModel createDataModel()
	{
		return CCopasiRootContainer.addDatamodel();
	}
	
	public String getSBML(String dataModelKey)
	{
		String sbmlModel = "";
		CCopasiDataModel dataModel = getCopasiModelFromKey(dataModelKey);

		if(dataModel == null)
		{
			System.out.println("Error accessing Copasi Data Models.");
			System.exit(0);
		}
		
		try
		{
			sbmlModel = dataModel.exportSBMLToString(3, 1);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sbmlModel;
	}
	
	
	public void exportModel(String dataModelKey)
	{
		CCopasiDataModel dataModel = getCopasiModelFromKey(dataModelKey);

		try
		{
			dataModel.exportSBML("DirectCopasiOutput.sbml");
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Key: " + dataModel.getModel().getKey());
		System.out.println("SBMLID: " + dataModel.getModel().getSBMLId());
	}
	
	
	public CCopasiDataModel getCopasiModelFromKey(String dataModelKey)
	{
		//System.out.println("G key: " + dataModelKey);
		DataModelVector modelList = CCopasiRootContainer.getDatamodelList();
		CCopasiDataModel model = null;
		for(long index = 0; index < modelList.size(); index++)
		{
			model = CCopasiRootContainer.get(index);
			//System.out.println("F key: " + model.getModel().getKey());
			if(dataModelKey.equals(model.getModel().getKey()))
			{
				return model;
			}
		}
		return null;
	}
}
