package acgui;

import javax.swing.JOptionPane;

import org.COPASI.*;
import org.sbml.libsbml.GeneralGlyph;

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
	
	public static void clear()
	{
		//CCopasiRootContainer.getDatamodelList().clear();
		while (CCopasiRootContainer.getDatamodelList().size()!=0) 
		{
			CCopasiRootContainer.removeDatamodelWithIndex(0);
		}
		System.out.println("Number of models in the CCopasiRootContainer: " + CCopasiRootContainer.getDatamodelList().size());
	}
	
	public static CCopasiDataModel createDataModel()
	{
		return CCopasiRootContainer.addDatamodel();
	}
	
	public static CCopasiDataModel createDataModel(String copasiData)
	{
		return importModelFromString(copasiData);
	}
	
	public static boolean removeDataModel(String dataModelName)
	{
		CCopasiDataModel dataModel = getCopasiModelFromModelName(dataModelName);
		if (dataModel == null)
		{
			return false;
		}
		
		return CCopasiRootContainer.removeDatamodel(dataModel);
		
		/*
		String searchFor = "CN=Root,Model="+dataModelName;
		 
		DataModelVector modelList = CCopasiRootContainer.getDatamodelList();
		CCopasiDataModel model = null;
		for(long index = 0; index < modelList.size(); index++)
		{
			model = CCopasiRootContainer.get(index);
			//System.out.println("model cn: " + model.getModel().getCN().getString());
			CObjectInterface whatsit = model.getObject(new CCopasiObjectName(searchFor));
			if (whatsit == null)
			{
							//System.out.println("dont have a: " + searchFor);
			}
			else 
			{
				return CCopasiRootContainer.removeDatamodelWithIndex(index);
			}
		}
		return false;
		*/
	}
	/*
	public static Module importModuleCopasiData(String copasiData, GeneralGlyph glyph)
	{
		CCopasiDataModel dataModel = null;
		String modelName = null;
		Module mod = null;
		
		dataModel = importModelFromString(copasiData);
		if (dataModel != null)
		{
			modelName = validateModelName(dataModel);
			mod = AC_Utility.createModule(modelName, glyph, false);
		}
		return mod;
	}
	
	public static Module importModuleCopasiData(String copasiData, Module parent, GeneralGlyph glyph)
	{
		CCopasiDataModel dataModel = null;
		String modelName = null;
		Module mod = null;
		
		dataModel = importModelFromString(copasiData);
		if (dataModel != null)
		{
			modelName = validateModelName(dataModel);
			mod = AC_Utility.createModule(modelName, parent, glyph, false);
		}
		return mod;
	}
	
	public static Module importMathematicalAggregatorCopasiData(String copasiData, Module parent, int inputs, Operation op, GeneralGlyph glyph)
	{
		CCopasiDataModel dataModel = null;
		String modelName = null;
		Module mod = null;
		
		dataModel = importModelFromString(copasiData);
		if (dataModel != null)
		{
			modelName = validateModelName(dataModel);
			mod = AC_Utility.createMathematicalAggregator(modelName, parent, inputs, op, glyph);
		}
		return mod;
	}
	*/
	public static Module importCopasiFile(String fileName)
	{
		CCopasiDataModel dataModel = null;
		//String moduleDefinitionName = null;
		String names[];
		Module mod = null;
		
		dataModel = importModelFromFile(fileName);
		
		if (dataModel != null)
		{
			//moduleDefinitionName = validateModuleDefinitionName(dataModel);
			names = assignNames(dataModel, null);
			if (names != null)
			{
				mod = AC_Utility.createModule(names[0], names[1], false);
				AC_Utility.addTreeNode(mod);
			}
			if (mod == null)
			{
				CCopasiRootContainer.removeDatamodel(dataModel);
			}
		}
		
		return mod;
	}
	
	public static Module importCopasiFile(String fileName, Module parent)
	{
		CCopasiDataModel dataModel = null;
		//String moduleDefinitionName = null;
		String names[];
		Module mod = null;
		
		dataModel = importModelFromFile(fileName);
		
		if (dataModel != null)
		{
			//moduleDefinitionName = validateModuleDefinitionName(dataModel);
			names = assignNames(dataModel, parent);
			if (names != null)
			{
				mod = AC_Utility.createModule(names[0], names[1], parent, false);
				AC_Utility.addTreeNode(mod);
			}
			if (mod == null)
			{
				CCopasiRootContainer.removeDatamodel(dataModel);
			}
		}
		
		return mod;
	}
	
	public static String getSBML(String dataModelName)
	{
		String sbmlModel = "";
		CCopasiDataModel dataModel = getCopasiModelFromModelName(dataModelName);
		
		if(dataModel == null)
		{
			System.err.println("Error accessing Copasi Data Models.");
			return null;
		}
		
		try
		{
			sbmlModel = dataModel.exportSBMLToString(3, 1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return sbmlModel;
	}
	
	
	public static void exportModel(String dataModelName)
	{
		CCopasiDataModel dataModel = getCopasiModelFromModelName(dataModelName);

		try
		{
			dataModel.exportSBML("DirectCopasiOutput.sbml");
		}
		catch (Exception e)
		{
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
	
	public static CCopasiDataModel getCopasiModelFromModelName(String dataModelName) {
		String searchFor = "CN=Root,Model="+dataModelName;
	 
		DataModelVector modelList = CCopasiRootContainer.getDatamodelList();
		CCopasiDataModel model = null;
		for(long index = 0; index < modelList.size(); index++)
		{
			model = CCopasiRootContainer.get(index);
			//System.out.println("model cn: " + model.getModel().getCN().getString());
			CObjectInterface whatsit = model.getObject(new CCopasiObjectName(searchFor));
			if (whatsit == null)
			{
							//System.out.println("dont have a: " + searchFor);
			}
			else 
			{
				return model;
			}
		}
		
		return null;
	}
	
	public static long getNumberOfModels()
	{
		return CCopasiRootContainer.getDatamodelList().size();
	}
	
	public static void printDataModelList()
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
	
	private static String[] assignNames(CCopasiDataModel dataModel, Module parent)
	{
		String names[] = new String[2];
		String moduleDefinitionName;
		String moduleName;
		
		moduleDefinitionName = validateModuleDefinitionName(dataModel, parent);
		moduleName = validateModuleName(moduleDefinitionName, parent);
		
		if ((moduleDefinitionName == null) || (moduleName == null))
		{
			return null;
		}
		
		names[0] = moduleName;
		names[1] = moduleDefinitionName;
		
		return names;
	}
	
	private static CCopasiDataModel importModelFromFile(String fileName)
	{
		CCopasiDataModel dataModel = null;
		
		try
		{
			dataModel = createDataModel();
			dataModel.loadModel(fileName);
		}
		catch (java.lang.Exception ex)
		{
			ex.printStackTrace();
			System.err.println( "Error while importing the Copasi model from file named \"" + fileName + "\"." );
		}
		
		return dataModel;
	}
	
	private static CCopasiDataModel importModelFromString(String data)
	{
		CCopasiDataModel dataModel = null;
		
		try
		{
			dataModel = createDataModel();
			dataModel.importSBMLFromString(data);
		}
		catch (java.lang.Exception ex)
		{
			ex.printStackTrace();
		}
		
		return dataModel;
	}
	
	private static String validateModuleDefinitionName(CCopasiDataModel dataModel, Module parent)
	{
		String modelName = dataModel.getModel().getObjectName();
		
		if (modelName == null || modelName.isEmpty())
		{
			String message = "The imported Module Definition does not have a name.";
			JOptionPane.showMessageDialog(null, message, "Invalid Name", JOptionPane.WARNING_MESSAGE);
			
			modelName = AC_Utility.promptUserForNewModuleName("Enter a Module Definition name:");
		}
		else
		{
			if (!AC_Utility.moduleNameValidation(modelName, true))
			{
				modelName = AC_Utility.promptUserForNewModuleName("Enter a Module Definition name:");
			}
		}
		
		if (modelName != null)
		{
			dataModel.getModel().setObjectName(modelName);
		}
		
		return modelName;
	}
	
	private static String validateModuleName(String definitionName, Module parent)
	{
		if (definitionName == null)
		{
			return null;
		}
		String newName = null;
		String message;
		boolean validName = false;
		
		while (!validName)
		{
			newName = AC_Utility.promptUserForNewModuleName(parent, "Enter a Module name:");
			if (newName == null)
			{
				return null;
			}
			if (newName.equals(definitionName))
			{
				message = "A Module and a Module Definition cannot share the same name." + AC_Utility.eol;
				message += "Please enter a different name.";
				JOptionPane.showMessageDialog(null, message, "Invalid Name", JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				validName = true;
			}
		}
		
		return newName;
	}
	/*
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
	*/
}
