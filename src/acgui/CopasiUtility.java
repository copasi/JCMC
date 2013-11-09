package acgui;

import java.util.ListIterator;

import org.COPASI.*;

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
	
	public String getSBML(String dataModelName)
	{
		String sbmlModel = "";
		CCopasiDataModel dataModel = getCopasiModelFromModelName(dataModelName);
		
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
	
	
	public void exportModel(String dataModelName)
	{
		CCopasiDataModel dataModel = getCopasiModelFromModelName(dataModelName);

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
	
	/*
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
	*/
	
	public CCopasiDataModel getCopasiModelFromModelName(String dataModelName) {
		String searchFor = "CN=Root,Model="+dataModelName;
	 
		DataModelVector modelList = CCopasiRootContainer.getDatamodelList();
		CCopasiDataModel model = null;
		for(long index = 0; index < modelList.size(); index++)
		{
			model = CCopasiRootContainer.get(index);
			//System.out.println("model cn: " + model.getModel().getCN().getString());
			CObjectInterface whatsit = model.getObject(new CCopasiObjectName(searchFor));
			if (whatsit == null) {
							//System.out.println("dont have a: " + searchFor);
			} else {
				return model;
			}
		}
		
		return null;
	}
	
	public long getNumberOfModels()
	{
		return CCopasiRootContainer.getDatamodelList().size();
	}
	
	public void printDataModelList()
	{
		DataModelVector modelList = CCopasiRootContainer.getDatamodelList();
		CCopasiDataModel model = null;
		System.out.println("Number of CCopasiDataModels: " + modelList.size());
		for(long index = 0; index < modelList.size(); index++)
		{
			model = CCopasiRootContainer.get(index);
			System.out.println("model[" + index + "] cn: " + model.getModel().getCN().getString());
		}
		System.out.println();
	}
	
	private void setLayout(Module mod)
	{
		ListIterator<Module> children = mod.getChildren().listIterator();
		Module child;
		String name = mod.getName() + "_Layout";
		CCopasiDataModel dataModel = getCopasiModelFromModelName(mod.getName());
		CListOfLayouts layoutList = dataModel.getListOfLayouts();
		CLayout modLayout = new CLayout(name, dataModel);
		CLDimensions dim = new CLDimensions();
		CLGraphicalObject obj = new CLGraphicalObject("Model1_Fake_Port", dataModel);
		obj.setPosition(new CLPoint(21.0, 21.0));
		CLayout childLayout;
		CLCompartmentGlyph comp;
		CLPoint pos;
		double xPos;
		double yPos;
		String info = "";
		info = "Module: " + mod.getName() + "\n";
		info += "layout key: " + modLayout.getKey() + "\n";
		System.out.println(info);
		//modLayout.addGraphicalObject(obj);
		layoutList.addLayout(modLayout);
		while(children.hasNext())
		{
			child = children.next();
			childLayout = new CLayout(child.getName() + "_Layout", getCopasiModelFromModelName(child.getName()));
			comp = new CLCompartmentGlyph();
			comp.setModelObjectKey(child.getKey());
			xPos = child.getDrawingCellGeometry().getX();
			yPos = child.getDrawingCellGeometry().getY();
			pos = new CLPoint(xPos, yPos);
			comp.setPosition(pos);
			childLayout.addCompartmentGlyph(comp);
			
			info = "Submodule: " + child.getName() + "\n";
			info += "key: " + child.getKey() + "\n";
			info += "comp model object name: " + comp.getModelObjectName() + "\n";
			info += "comp model object display name: " + comp.getModelObjectDisplayName() + "\n";
			info += "comp model object key: " + comp.getModelObjectKey() + "\n";
			System.out.println(info);
			layoutList.add(childLayout);
		}
		//obj.setModelObjectKey(mod.getKey());
		//obj.setPosition(pos);
		//layout.addGraphicalObject(obj);
		
		System.out.println("Number of layouts: " + layoutList.size());
		for(long index = 0; index < layoutList.size(); index++)
		{
			CCopasiObject tlayout = layoutList.get(index);
			info = "Layout key: " + tlayout.getKey() + "\n";
			info += "layout object name: " + tlayout.getObjectName() + "\n";
			info += "layout object type: " + tlayout.getObjectType() + "\n";
			info += "layout object display name: " + tlayout.getObjectDisplayName() + "\n";
			System.out.println(info);
		}
	}
}
