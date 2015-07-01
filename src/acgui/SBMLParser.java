package acgui;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.COPASI.*;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.sbml.libsbml.*;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;

/**
 * A printer for the module.
 * @author T.C. Jones
 */
public class SBMLParser {

	/*
	private PrintWriter out;
	private String modelSBML;
	private String fileName;
	*/
	private static final String CONTAINER_MODULE_GLYPH_CODE = "_ContainerGlyph";
	private static ArrayList<ModuleDefinition> definitionList = new ArrayList<ModuleDefinition>();
	
	//private static String eol = System.getProperty("line.separator");
	
	/**
	 * Construct the object.
	 */
	public SBMLParser()
	{
		//System.out.println(CCopasiRootContainer.getDatamodelList().size());
		/*
		out = null;
		modelSBML = "";
		fileName = "";
		*/
	}
	
	public static boolean exportSBML(Module rootModule, String fName)
	{
		//System.out.println("createUniqueIDs() start.");
		AC_Utility.createUniqueIDs(rootModule);
		//System.out.println("createUniqueIDs() end.");
		
		//System.out.println("getContainerDefinition() start.");
		SBMLDocument document = exportContainerDefinition(rootModule);
		//System.out.println("getContainerDefinition() end.");
		
		//System.out.println(libsbml.writeSBMLToString(document));
		//libsbml.writeSBMLToFile(document, fName);
		
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_UNITS_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_MATHML_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_MODELING_PRACTICE, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_GENERAL_CONSISTENCY, false);
		document.checkConsistency();
		if (document.getNumErrors() > 0) {
			System.err.println("Encountered errors while reading the file. ");
			System.err.println("Please correct the following errors and try again.");
			document.printErrors();
			return false;
		}
		
		if (document == null)
		{
			return false;
		}
		else
		{
			/*
			ConversionProperties properties = new ConversionProperties();
			SBMLConverter converter;
			
			properties.addOption("flatten comp");
			properties.addOption("leavePorts", false);
			
			converter = SBMLConverterRegistry.getInstance().getConverterFor(properties);
			
			if (converter == null)
			{
				System.err.println("SBMLConverter is null.");
			}
			else
			{
				
				document.setConsistencyChecks(libsbml.LIBSBML_CAT_UNITS_CONSISTENCY, false);
				document.setConsistencyChecks(libsbml.LIBSBML_CAT_MATHML_CONSISTENCY, false);
				document.setConsistencyChecks(libsbml.LIBSBML_CAT_MODELING_PRACTICE, false);
				document.setConsistencyChecks(libsbml.LIBSBML_CAT_GENERAL_CONSISTENCY, false);
				
				document.setConsistencyChecksForConversion(libsbml.LIBSBML_CAT_UNITS_CONSISTENCY, false);
				document.setConsistencyChecksForConversion(libsbml.LIBSBML_CAT_MATHML_CONSISTENCY, false);
				document.setConsistencyChecksForConversion(libsbml.LIBSBML_CAT_MODELING_PRACTICE, false);
				document.setConsistencyChecksForConversion(libsbml.LIBSBML_CAT_GENERAL_CONSISTENCY, false);
				converter.setDocument(document);
				int result = converter.convert();
				if (result != libsbml.LIBSBML_OPERATION_SUCCESS)
				{
					System.err.println("Conversion failed.");
					document.printErrors();
				}
			}
			*/
			//document.checkConsistency();
			libsbml.writeSBMLToFile(document, fName);
			return true;
		}
	}
	
	public static Module importSBML(String fileName, boolean external)
	{
		return importSBML(fileName, null, external);
	}
	
	public static Module importSBML(String fileName, Module parent, boolean external)
	{
		definitionList.clear();
		Module mod = null;
		SBMLDocument document;
		try
		{			
			document = libsbml.readSBMLFromFile(fileName);
			
			//System.out.println("Document Level: " + sdoc.getLevel());
			//System.out.println("Document Version: " + sdoc.getVersion());

			if (document.getNumErrors() > 0)
			{
				String msg = "This SBML file contains errors. Please try to open a valid SBML file.";
				JOptionPane.showMessageDialog(null,
					    msg,
					    "Import Error",
					    JOptionPane.ERROR_MESSAGE);
				/*
				System.err.println("Encountered errors while reading the file. ");
				System.err.println("Please correct the following errors and try again.");
				document.printErrors();
				*/
				return null;
			}
			
			if (document.getLevel() < 3)
			{
				String msg = "SBML level " + document.getLevel() + " is not currently supported.";
				JOptionPane.showMessageDialog(null,
					    msg,
					    "Import Error",
					    JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}	
		catch (Exception e)
		{
			System.err.println("SBMLParser.importSBML(): document import failed.");
			e.printStackTrace();
			return null;
		}
		
		mod = importContainerModule(document, parent);
		if (mod == null)
		{
			// there was an error importing the module
			return null;
		}
		mod.getModuleDefinition().setExternal(external);
		if (external)
		{
			mod.getModuleDefinition().setExternalSource(fileName);
			//mod.getModuleDefinition().setExternalModelRef(mod.getModuleDefinition().getName());
			String md5;
			try
			{
				md5 = CheckSumGenerator.generate(fileName);
				mod.getModuleDefinition().setmd5(md5);
			} 
			catch (Exception e)
			{
				System.err.println("SBMLParser.importSBML(): CheckSumGenerator failed.");
				e.printStackTrace();
			}
		}
		AC_Utility.addTreeNode(mod);
		return mod;
	}
	
	public static SBMLDocument SBMLValidation(String fileName)
	{
		return libsbml.readSBMLFromFile(fileName);
	}
	
	public static ModuleDefinition importExternalDefinition(ExternalModelDefinition moduleDefinition, ModuleDefinition parent, SBMLNamespaces nameSpaces, GeneralGlyph glyph)
	{
		String sbmlID = moduleDefinition.getId();
		String name = moduleDefinition.getName();
		String source = moduleDefinition.getSource();
		String modelRef = moduleDefinition.getModelRef();
		String md5 = moduleDefinition.getMd5();
		String sbmlString;
		SBMLFileResolver fileResolver = new SBMLFileResolver();
		SBMLDocument externalDoc = fileResolver.resolve(source);
		if (externalDoc == null)
		{
			return null;
		}
		else
		{
			/*
			JOptionPane.showMessageDialog(null,
				    "Time to import an external definition.",
				    "Information",
				    JOptionPane.INFORMATION_MESSAGE);
			*/
			if (modelRef.isEmpty())
			{
				// return the containing model
				removePluginData(externalDoc);
				sbmlString = libsbml.writeSBMLToString(externalDoc);
				ModuleDefinition definition = AC_Utility.createModuleDefinition(sbmlID, name, sbmlString, parent);
				definition.setExternal(true);
				definition.setExternalSource(source);
				definition.setExternalModelRef(modelRef);
				definition.setmd5(md5);
				return definition;
			}
			else
			{
				// find the correct Model to import
				// search listOfModelDefinitions
				// search listOfExternalModelDefinitions
			}
		}
		return null;
	}
	
	private static SBMLDocument exportContainerDefinition(Module containerModule)
	{
		ModuleDefinition containerDefinition = containerModule.getModuleDefinition();
		SBMLDocument document;
		String modSBML = "";
		CCopasiDataModel dataModel;
		System.out.println("getCopasiModelFromName(" + containerDefinition.getName() + ") start");
		dataModel = CopasiUtility.getCopasiModelFromModelName(containerDefinition.getName());
		if (dataModel == null)
		{
			System.err.println(containerDefinition.getName() + "'s dataModel is empty");
			return null;
		}
		System.out.println("getCopasiModelFromName(" + containerDefinition.getName() + ") end");
		
		try
		{
			System.out.println("exportSBMLToString() start.");
			modSBML = dataModel.exportSBMLToString(3, 1);
			System.out.println("exportSBMLToString() end.");
			
			System.out.println("removeCOPASIMetaID() start.");
			modSBML = removeCOPASIMetaID(modSBML);			
			System.out.println("removeCOPASIMetaID() end.");
			
			//System.out.println("removeRenderPackage() start.");
			//modSBML = removeRenderPackage(modSBML);
			//System.out.println("removeRenderPackage() end.");
			modSBML = ensureCompIsEnabled(modSBML);
		}
		catch (Exception e)
		{
			System.err.println("Error SBMLParser.exportContainerDefinition: try/catch statement exception.");
			e.printStackTrace();
			return null;
		}
		if (modSBML == null)
		{
			System.err.println(containerDefinition.getName() + "'s modSBML string is null.");
			return null;
		}
		document = libsbml.readSBMLFromString(modSBML);
		//System.out.println("Document valid: " + tempDoc.validateSBML());
		//System.out.println("Document Level: " + tempDoc.getLevel());
		//System.out.println("Document Version: " + tempDoc.getVersion());

		if (document.getNumErrors() > 0)
		{
			System.err.println("Encountered errors while reading SBML from string for container Module Definition: " + containerDefinition.getName() + ".");
			System.err.println("Please correct the following errors and try again.");
			document.printErrors();
			//System.exit(2);
			return null;
		}
		/*
		System.out.println();
		System.out.println();
		System.out.println(libsbml.writeSBMLToString(document));
		System.out.println();
		System.out.println();
		*/
		if (!document.isPackageEnabled("comp"))
		{
			if (document.enablePackage("http://www.sbml.org/sbml/level3/version1/comp/version1", "comp", true) != 0)
			{
				System.err.println("Error enabling Comp Package.");
				return null;
			}
			document.setPackageRequired("comp", true);
		}
		
		if (!document.isPackageEnabled("layout"))
		{
			if (document.enablePackage("http://www.sbml.org/sbml/level3/version1/layout/version1", "layout", true) != 0)
			{
				System.err.println("Error enabling Layout Package.");
				return null;
			}
			document.setPackageRequired("layout", false);
		}
		
		CompSBMLDocumentPlugin documentCompPlugin = (CompSBMLDocumentPlugin)document.getPlugin("comp");
		
		if (document.getModel() == null)
		{
			System.err.println("document.getModel() is null.");
			return null;
		}
		//setSBMLNamespaces(sdoc, tempDoc.getNamespaces());
		document.getModel().setId(containerDefinition.getID());
		document.getModel().setName(containerDefinition.getName());
		/*
		System.out.println();
		System.out.println();
		System.out.println(libsbml.writeSBMLToString(document));
		System.out.println();
		System.out.println();
		System.out.println("Document valid: " + document.validateSBML());
		*/
		
		if (containerDefinition.getPorts().size() > 0)
		{
			if (!exportPortDefinitions(document.getModel(), containerDefinition))
			{
				System.err.println("Error SBMLParser.exportContainerDefinition: exportPortDefinitions failed.");
				return null;
			}
		}
		
		if (containerDefinition.getChildren().size() > 0)
		{			
			System.out.println("exportSubmodelDefinitions() start.");
			if (!exportSubmodelDefinitions(documentCompPlugin, containerModule))
			{
				System.err.println("Error SBMLParser.exportContainerDefinition: exportSubmodelDefinitions failed.");
				return null;
			}
			System.out.println("exportSubmodelDefinitions() end.");
			
			System.out.println("exportSubmodelInformation() start.");
			//System.out.println("isCompEnabled: " + document.getModel().isPackageEnabled("comp"));
			//System.out.println("isCompartmentEnabled: " + document.getModel().getCompartment(0).isPackageEnabled("comp"));
			if (!exportSubmodelInformation(document.getModel(), containerModule))
			{
				System.err.println("Error SBMLParser.exportContainerDefinition: exportSubmodelInformation failed.");
				return null;
			}
			System.out.println("exportSubmodelInformation() end.");
			
			//System.out.println(libsbml.writeSBMLToString(sdoc));
		}
		
		System.out.println("addReplacements() start.");
		if (!exportReplacements(documentCompPlugin, document.getModel(), containerModule))
		{
			System.err.println("Error SBMLParser.exportContainerDefinition: exportReplacements failed.");
			return null;
		}
		System.out.println("addReplacements() end.");
		
		System.out.println("addLayoutInformation start.");
		
		if (!exportLayoutInformation(document.getModel(), containerModule))
		{
			System.err.println("Error SBMLParser.exportContainerDefinition: exportLayoutInformation failed.");
			return null;
		}
		
		System.out.println("addLayoutInformation end.");
		
		return document;
	}
	
	/**
	 * Return the model definitions.
	 * @param mod the model to output
	 */
	private static boolean exportSubmodelDefinitions(CompSBMLDocumentPlugin docPlugin, Module module)
	{
		ListIterator<Module> submodules = module.getChildren().listIterator();
		Module currentModule;
		ModuleDefinition currentModuleDefinition;
		CCopasiDataModel dataModel;
		String prependName;
		//CompSBMLDocumentPlugin docPlugin = (CompSBMLDocumentPlugin)sdoc.getPlugin("comp");
		//ListOfModelDefinitions moduleDefinitions = docPlugin.getListOfModelDefinitions();
		ModelDefinition moduleDefinition;
		String modSBML = "";
		SBMLDocument tempDoc;
		while(submodules.hasNext())
		{
			currentModule = submodules.next();
			currentModuleDefinition = currentModule.getModuleDefinition();
			if (currentModuleDefinition.isExternal())
			{
				if (docPlugin.getExternalModelDefinition(currentModuleDefinition.getID()) != null)
				{
					// the external definition has already been added
					continue;
				}
				if (!exportExternalSubmodelDefinition(docPlugin, currentModuleDefinition))
				{
					return false;
				}
			}
			else
			{
				if (docPlugin.getModelDefinition(currentModuleDefinition.getID()) != null)
				{
					// the definition has already been added
					continue;
				}
				dataModel = CopasiUtility.getCopasiModelFromModelName(currentModuleDefinition.getName());
				if (dataModel == null)
				{
					System.err.println(currentModuleDefinition.getName() + "'s dataModel is empty");
					return false;
				}
				//hasPorts = (child.getPorts().size() != 0);
				//System.out.println("hasPorts = " + hasPorts);
				
				try
				{
					modSBML = dataModel.exportSBMLToString(3, 1);
					modSBML = removeCOPASIMetaID(modSBML);
					//modSBML = removeRenderPackage(modSBML);
					modSBML = ensureCompIsEnabled(modSBML);				
				}
				catch (Exception e)
				{
					System.err.println("Error SBMLParser.exportSubmodelDefinitions: Module Definition " + currentModuleDefinition.getName() + " try/catch statement exception.");
					e.printStackTrace();
					return false;
				}
				
				if (modSBML == null)
				{
					System.err.println("Error SBMLParser.exportSubmodelDefinitions: Module Definition " + currentModuleDefinition.getName() +"'s modSBML string is null.");
					return false;
				}
				
				tempDoc = libsbml.readSBMLFromString(modSBML);
				if (tempDoc.getNumErrors() > 0)
				{
					System.err.println("Encountered errors while reading SBML from string for Module Definition: " + currentModuleDefinition.getName() + ".");
					System.err.println("Please correct the following errors and try again.");
					tempDoc.printErrors();
					//System.exit(2);
					return false;
				}
				tempDoc.getModel().setId(currentModuleDefinition.getID());
				/*
				if (currentModuleDefinition.getInstances().size() == 1)
				{
					// prepend species, parameters, container, etc. names with the submodel name
					prependNameToComponents(tempDoc, currentModule.getName());
					prependName = currentModule.getName();
				}
				else
				{
					// prepend species, parameters, container, etc. names with the module definition name
					prependNameToComponents(tempDoc, currentModuleDefinition.getName());
					prependName = currentModuleDefinition.getName();
				}
				*/
				moduleDefinition = new ModelDefinition(tempDoc.getModel());
				
				if (currentModuleDefinition.getPorts().size() > 0)
				{
					if (!exportPortDefinitions(moduleDefinition, currentModuleDefinition))
					{
						System.err.println("Error SBMLParser.exportSubmodelDefinitions: Module Definition " + currentModuleDefinition.getName() + " exportPortDefinitions failed.");
						return false;
					}
				}
				
				if (currentModuleDefinition.getChildren().size() > 0)
				{
					if (!exportSubmodelDefinitions(docPlugin, currentModule))
					{
						System.err.println("Error SBMLParser.exportSubmodelDefinitions: Module Definition " + currentModuleDefinition.getName() + " exportSubmodelDefinitions failed.");
						return false;
					}
					
					if (!exportSubmodelInformation(moduleDefinition, currentModule))
					{
						System.err.println("Error SBMLParser.exportSubmodelDefinitions: Module Definition " + currentModuleDefinition.getName() + " exportSubmodelInformation failed.");
						return false;
					}
				}
				
				if (!exportReplacements(docPlugin, moduleDefinition, currentModule))
				{
					System.err.println("Error SBMLParser.exportSubmodelDefinitions: Module Definition " + currentModuleDefinition.getName() + " exportReplacements failed.");
					return false;
				}
				
				docPlugin.addModelDefinition(moduleDefinition);
			}
		}
		return true;
	}
	
	private static boolean exportExternalSubmodelDefinition(CompSBMLDocumentPlugin docPlugin, ModuleDefinition moduleDefinition)
	{
		ExternalModelDefinition extDefinition = docPlugin.createExternalModelDefinition();
		extDefinition.setId(moduleDefinition.getID());
		extDefinition.setSource(moduleDefinition.getExternalSource());
		if (moduleDefinition.getExternalModelRef() != null)
		{
			extDefinition.setModelRef(moduleDefinition.getExternalModelRef());
		}
		if (moduleDefinition.getmd5() != null)
		{
			extDefinition.setMd5(moduleDefinition.getmd5());
		}
		return true;
	}
	
	private static boolean exportPortDefinitions(Model model, ModuleDefinition definition)
	{		
		ListIterator<ACComponentDefinition> ports = definition.getPorts().listIterator();
		PortDefinition portDefinition;
		String idRef;
		String id;
		String variableName;
		
		CompModelPlugin compModulePlugin = (CompModelPlugin)model.getPlugin("comp");
		org.sbml.libsbml.Port modelPort;
		// <comp:port comp:idRef="Metabolite_2" comp:id="CycBPort"/>
		
		while(ports.hasNext())
		{
			portDefinition = (PortDefinition)ports.next();
			
			id = portDefinition.getName();
			variableName = portDefinition.getRefName();

			switch(portDefinition.getVariableType())
			{
			case SPECIES:
				//idRef = getSpeciesSBMLid(model.getListOfSpecies(), portDefinition.getRefName());
				idRef = getSpeciesSBMLid(model.getListOfSpecies(), variableName);
				break;
			case GLOBAL_QUANTITY:
				//idRef = getGlobalQSBMLid(model.getListOfParameters(), portDefinition.getRefName());
				idRef = getGlobalQSBMLid(model.getListOfParameters(), variableName);
				break;
			default:
				idRef = null;
			}
			
			if (idRef == null)
			{
				System.err.println("Error SBMLParser.exportPortDefinitions(" + definition.getName() + "): Port " + id + "'s sbmlID cannot be found.");
				return false;
			}
			
			//modelPort = new org.sbml.libsbml.Port();
			modelPort = compModulePlugin.createPort();
			modelPort.setIdRef(idRef);
			modelPort.setId(id);
			
			//compModulePlugin.addPort(modelPort);
		}
		return true;
	}
	
	/**
	 * Write the submodel information of the given module.
	 * @param mod the module containing the submodels
	 */
	private static boolean exportSubmodelInformation(Model model, Module parent)
	{
		ListIterator<Module> children = parent.getChildren().listIterator();
		Module child;
		CompModelPlugin compModulePlugin = (CompModelPlugin)model.getPlugin("comp");
		Submodel submodule;
		
		String id;
		String name;
		String modelRef;
		
		while(children.hasNext())
		{
			child = children.next();
			id = child.getID();
			name = child.getName();
			modelRef = child.getModuleDefinition().getID();
			
			//submodule = new Submodel();
			submodule = compModulePlugin.createSubmodel();
			submodule.setId(id);
			submodule.setName(name);
			submodule.setModelRef(modelRef);
			
			//compModulePlugin.addSubmodel(submodule);
		}
		return true;
	}
	
	private static boolean exportReplacements(CompSBMLDocumentPlugin docPlugin, Model model, Module parent)
	{
		if (parent.getChildren().size() > 0)
		{
			if (!exportReplacementCompartments(docPlugin, model, parent))
			{
				System.err.println("Error SBMLParser.exportReplacements(" + parent.getName() + ": exportReplacementCompartments failed.");
				return false;
			}
		}

		if (parent.getModuleDefinition().getVisibleVariables().size() > 0)
		{
			if (!exportVisibleVariables(model, parent))
			{
				System.err.println("Error SBMLParser.exportReplacements(" + parent.getName() + ": exportVisibleVariables failed.");
				return false;
			}
		}
		
		if (parent.getModuleDefinition().getEquivalences().size() > 0)
		{
			if (!exportEquivalenceNodes(model, parent))
			{
				System.err.println("Error SBMLParser.exportReplacements(" + parent.getName() + ": exportEquivalenceNodes failed.");
				return false;
			}
		}
		
		return true;
	}
	
	private static boolean exportReplacementCompartments(CompSBMLDocumentPlugin docPlugin, Model model, Module module)
	{
		ListIterator<Module> children = module.getChildren().listIterator();
		Module child;
		String submodelRef;
		String modelRef;
		String idRef;
		
		long numberOfModuleCompartments = model.getNumCompartments();
		Compartment moduleCompartment;
		CompSBasePlugin compartmentPlugin;
		long numberOfSubmoduleCompartments;
		Compartment submoduleCompartment;
		Model submodel;
		//Compartment compartment = model.getCompartment(0);
		//CompSBasePlugin compartmentPlugin = (CompSBasePlugin)compartment.getPlugin("comp");
		ReplacedElement replacedElement;
		while(children.hasNext())
		{
			// <comp:replacedElement comp:idRef="comp" comp:submodelRef="A"/>
			child = children.next();
			modelRef = child.getModuleDefinition().getID();
			submodelRef = child.getID();
			//System.out.println("child: " + submodelRef);
			//System.out.println("modelRef: " + modelRef);
			if (docPlugin.getModelDefinition(modelRef) == null)
			{
				if (docPlugin.getExternalModelDefinition(modelRef) == null)
				{
					System.err.println("model definition is null");
					return false;
				}
				else
				{
					submodel = docPlugin.getExternalModelDefinition(modelRef).getReferencedModel();
				}
			}
			else
			{
				submodel = docPlugin.getModelDefinition(modelRef);
			}
			numberOfSubmoduleCompartments = submodel.getNumCompartments();
			for (long i = 0; i < numberOfSubmoduleCompartments; i++)
			{
				moduleCompartment = null;
				submoduleCompartment = submodel.getCompartment(i);
				if (submoduleCompartment == null)
				{
					System.err.println("model compartment is null");
					return false;
				}
				// find a matching compartment in the module
				for (long j = 0; j < numberOfModuleCompartments; j++)
				{
					if (model.getCompartment(j).getName().equals(submoduleCompartment.getName()))
					{
						moduleCompartment = model.getCompartment(j);
					}
				}
				if (moduleCompartment == null)
				{
					// there is no matching compartment in the module
					// create one
					if (model.addCompartment(submoduleCompartment) != libsbml.LIBSBML_OPERATION_SUCCESS)
					{
						System.err.println("SBMLParser.exportReplacementCompartments(): failed adding submodule compartment to module.");
						return false;
					}
					moduleCompartment = model.getCompartment(numberOfModuleCompartments);
					numberOfModuleCompartments++;
				}
				compartmentPlugin = (CompSBasePlugin)moduleCompartment.getPlugin("comp");
				idRef = submoduleCompartment.getId();
				
				replacedElement = compartmentPlugin.createReplacedElement();
				replacedElement.setIdRef(idRef);
				replacedElement.setSubmodelRef(submodelRef);
			}
			
			//idRef = docPlugin.getModelDefinition(modelRef).getCompartment(0).getId();
			
			//replacedElement = compartmentPlugin.createReplacedElement();
			//replacedElement.setIdRef(idRef);
			//replacedElement.setSubmodelRef(submodelRef);
		}
		return true;
	}
	
	private static Object findVariable(String name, Module mod)
	{
		ListIterator<ACComponentNode> vars = mod.getVisibleVariables().listIterator();
		VisibleVariableNode vNode;
		while(vars.hasNext())
		{
			vNode = (VisibleVariableNode)vars.next();
			if (name.equals(vNode.getVisibleVariableDefinition().getRefName()))
			{
				return vNode;
			}
		}
		
		ListIterator<ACComponentNode> eNodes = mod.getEquivalences().listIterator();
		EquivalenceNode eNode;
		while(eNodes.hasNext())
		{
			eNode = (EquivalenceNode)eNodes.next();
			if (name.equals(eNode.getEquivalenceDefinition().getRefName()))
			{
				return eNode;
			}
		}
		
		return null;
	}
	
	/*
	private String getVisibleVariableReplacements(VisibleVariable var)
	{
		Object outgoingEdges [] = mxGraphModel.getOutgoingEdges(AC_GUI.drawingBoard.getGraph().getModel(), var.getDrawingCell());
		String portRef;
		String submodelRef;
		String output = "";
		if (outgoingEdges.length != 0)
		{
			output += addSpace(8) + "<comp:listOfReplacedElements>" + eol;
			
			for (int i = 0; i < outgoingEdges.length; i++)
			{
				// <comp:replacedElement comp:portRef="S" comp:submodelRef="A"/>
				portRef = ((Port)((mxCell)outgoingEdges[i]).getTarget().getValue()).getName();
				submodelRef = ((Port)((mxCell)outgoingEdges[i]).getTarget().getValue()).getParent().getName();
				output += addSpace(10) + "<comp:replacedElement ";
				output += "comp:portRef=\"" + portRef + "\" ";
				output += "comp:submodelRef=\"" + submodelRef + "\"/>" + eol;
			}
			
			output += addSpace(8) + "</comp:listOfReplacedElements>" + eol;
		}
		Object incomingEdges [] = mxGraphModel.getIncomingEdges(AC_GUI.drawingBoard.getGraph().getModel(), var.getDrawingCell());
		
		if (incomingEdges.length != 0)
		{
			// <comp:replacedBy comp:portRef="D_port" comp:submodelRef="B"/>
			portRef = ((Port)((mxCell)incomingEdges[0]).getSource().getValue()).getName();
			submodelRef = ((Port)((mxCell)incomingEdges[0]).getSource().getValue()).getParent().getName();
			output += addSpace(8) + "<comp:replacedBy ";
			output += "comp:portRef=\"" + portRef + "\" ";
			output += "comp:submodelRef=\"" + submodelRef + "\"/>" + eol;
		}
		return output;
	}
	*/
	
	private static boolean exportVisibleVariables(Model model, Module parent)
	{
		ListIterator<ACComponentNode> vars = parent.getVisibleVariables().listIterator();
		while(vars.hasNext())
		{
			if (!addVisibleVariableReplacements(model, (VisibleVariableNode)vars.next()))
			{
				return false;
			}
		}
		return true;
	}
	
	private static boolean addVisibleVariableReplacements(Model model, VisibleVariableNode node)
	{
		Object outgoingEdges [] = mxGraphModel.getOutgoingEdges(AC_GUI.drawingBoard.getGraph().getModel(), node.getDrawingCell());
		Object incomingEdges [] = mxGraphModel.getIncomingEdges(AC_GUI.drawingBoard.getGraph().getModel(), node.getDrawingCell());
		CompSBasePlugin plugin = null;
		String variableName;
		
		if ((outgoingEdges.length == 0) && (incomingEdges.length == 0))
		{
			return true;
		}
		variableName = node.getVisibleVariableDefinition().getRefName();
		
		//Species species = getSpecies(model.getListOfSpecies(), node.getVisibleVariableDefinition().getRefName());
		Species species = getSpecies(model.getListOfSpecies(), variableName);
		Parameter parameter = getParameter(model.getListOfParameters(), variableName);
		if ((species == null) && (parameter == null))
		{
			// the variable was not found
			System.err.println("Error SBMLParser.addVisibleVariableReplacements(): Variable " + variableName + " not found.");
			return false;
		}
		
		if (species != null)
		{
			// the variable is a species
			plugin = (CompSBasePlugin)species.getPlugin("comp");
		}
		else
		{
			// the variable is a parameter
			plugin = (CompSBasePlugin)parameter.getPlugin("comp");
		}
		
		ReplacedElement replacedElement;
		PortNode port;
		String portRef;
		String submodelRef;
		Module parent;
		
		if (outgoingEdges.length != 0)
		{
			for (int i = 0; i < outgoingEdges.length; i++)
			{
				// <comp:replacedElement comp:portRef="S" comp:submodelRef="A"/>
				port = (PortNode)((mxCell)outgoingEdges[i]).getTarget().getValue();
				parent = port.getParent();
				if ((parent == node.getParent()) && (variableName.equals(port.getPortDefinition().getRefName())))
				{
					// this variable is already assigned to the port
					continue;
				}
				portRef = port.getPortDefinition().getName();
				//submodelRef = port.getPortDefinition().getParent().getID();
				submodelRef = port.getParent().getID();
				replacedElement = new ReplacedElement();
				replacedElement.setPortRef(portRef);
				replacedElement.setSubmodelRef(submodelRef);
				plugin.addReplacedElement(replacedElement);
			}
		}
		
		if (incomingEdges.length != 0)
		{
			// <comp:replacedBy comp:portRef="D_port" comp:submodelRef="B"/>
			port = (PortNode)((mxCell)incomingEdges[0]).getSource().getValue();
			parent = port.getParent();
			if (!((parent == node.getParent()) && (variableName.equals(port.getPortDefinition().getRefName()))))
			{
				portRef = port.getPortDefinition().getName();
				//submodelRef = port.getPortDefinition().getParent().getID();
				submodelRef = port.getParent().getID();
				replacedElement = new ReplacedElement();
				replacedElement.setPortRef(portRef);
				replacedElement.setSubmodelRef(submodelRef);
				plugin.addReplacedElement(replacedElement);
				//ReplacedBy replacedBy = speciesPlugin.createReplacedBy();
				//replacedBy.setPortRef(portRef);
				//replacedBy.setSubmodelRef(submodelRef);
			}
		}
		
		return true;
	}
	
	private static boolean exportEquivalenceNodes(Model model, Module parent)
	{
		ListIterator<ACComponentNode> eNodes = parent.getEquivalences().listIterator();
		while(eNodes.hasNext())
		{
			if (!addEquivalenceNodeReplacements(model, (EquivalenceNode)eNodes.next()))
			{
				return false;
			}
		}
		return true;
	}
	
	private static boolean addEquivalenceNodeReplacements(Model model, EquivalenceNode eNode)
	{
		Object outgoingEdges [] = mxGraphModel.getOutgoingEdges(AC_GUI.drawingBoard.getGraph().getModel(), eNode.getDrawingCell());
		String variableName;
		
		if (outgoingEdges.length == 0)
		{
			return true;
		}
		variableName = eNode.getEquivalenceDefinition().getRefName();
		
		//Species species = getSpecies(model.getListOfSpecies(), eNode.getEquivalenceDefinition().getRefName());
		Species species = getSpecies(model.getListOfSpecies(), variableName);
		if (species == null)
		{
			System.err.println("Error SBMLParser.addEquivalenceNodeReplacements(): Species " + eNode.getEquivalenceDefinition().getRefName() + " not found.");
			return false;
		}
		CompSBasePlugin speciesPlugin = (CompSBasePlugin)species.getPlugin("comp");
		ReplacedElement replacedElement;
		PortNode port;
		String portRef;
		String submodelRef;
		
		for (int i = 0; i < outgoingEdges.length; i++)
		{
			// <comp:replacedElement comp:portRef="S" comp:submodelRef="A"/>
			port = (PortNode)((mxCell)outgoingEdges[i]).getTarget().getValue();
			portRef = port.getPortDefinition().getName();
			//submodelRef = port.getPortDefinition().getParent().getID();
			submodelRef = port.getParent().getID();
			replacedElement = new ReplacedElement();
			replacedElement.setPortRef(portRef);
			replacedElement.setSubmodelRef(submodelRef);
			speciesPlugin.addReplacedElement(replacedElement);
		}
		
		return true;
	}
	
	private String printModelCompHeader()
	{
		String header;
		header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + AC_Utility.eol;
		header += "<sbml xmlns=\"http://www.sbml.org/sbml/level3/version1/core\" level=\"3\" version=\"1\" " + AC_Utility.eol;
		header += "xmlns:comp=\"http://www.sbml.org/sbml/level3/version1/comp/version1\" comp:required=\"true\"" + AC_Utility.eol;
		header += "xmlns:layout=\"http://www.sbml.org/sbml/level3/version1/layout/version1\" layout:required=\"false\"" + AC_Utility.eol;
		header += "xmlns:html=\"http://www.w3.org/1999/xhtml\"" + AC_Utility.eol;
		header += "xmlns:jigcell=\"http://www.sbml.org/2001/ns/jigcell\"" + AC_Utility.eol;
		header += "xmlns:math=\"http://www.w3.org/1998/Math/MathML\">\n" + AC_Utility.eol;
		
		return header;
	}
	
	private static String removeCOPASIMetaID(String sbml)
	{
		String output = sbml;
		int startMetaIDIndex;
		int endMetaIDIndex;
		String metaID;
		
		startMetaIDIndex = output.indexOf("metaid=");
		while (startMetaIDIndex != -1)
		{
			endMetaIDIndex = output.indexOf("\"", startMetaIDIndex+10);
			metaID = output.substring(startMetaIDIndex, endMetaIDIndex+2);
			output = output.replace(metaID, "");
			startMetaIDIndex = output.indexOf("metaid=", startMetaIDIndex);
		}
		return output;
	}
	
	private static String removeRenderPackage(String sbml)
	{
		String renderInfo;
		int startIndex = -1;
		int endIndex = 0;;
		int index;
		
		index = sbml.indexOf("render:required=\"false\"");
		if (index != -1)
		{
			startIndex = index;
			endIndex = index + 23;
		}
		
		index = sbml.indexOf("render:required=\"true\"");
		if (index != -1)
		{
			startIndex = index;
			endIndex = index + 22;
		}
		
		if (startIndex == -1)
		{
			// no render package information is present
			return sbml;
		}
		
		renderInfo = sbml.substring(startIndex, endIndex);
		sbml = sbml.replace(renderInfo, "");
		return sbml;
	}
	
	/*
	private void setPortIDRefs(Model model, Module module)
	{
		ListIterator<Port> ports = module.getPorts().listIterator();
		Port port;
		
		
		//System.out.println(speciesList);
		String name;
		String sbmlID = null;
		VariableType vType;
		
		while(ports.hasNext())
		{
			port = ports.next();
			name = port.getRefName();
			vType = port.getVariableType();
			
			switch(vType)
			{
			case SPECIES:
				sbmlID = getSpeciesSBMLid(model.getListOfSpecies(), name);
				break;
			case GLOBAL_QUANTITY:
				sbmlID = getGlobalQSBMLid(model.getListOfParameters(), name);
				break;
			}
			
			//System.out.println("Port name: " + name + ".......ID: " + sbmlID);
			if (sbmlID != null)
			{
				port.setVariableSBMLid(sbmlID);
			}
			else
			{
				System.err.println("SBMLParser.setPortIDRefs: sbmlID is null.");
			}
		}
	}
	*/
	
	private static String getSpeciesSBMLid(ListOfSpecies speciesList, String speciesName)
	{
		for (long i = 0; i < speciesList.size(); i++)
		{
			if (speciesName.equals(speciesList.get(i).getName()))
			{
				return speciesList.get(i).getId();
			}
		}
		
		return null;
	}
	
	private static String getGlobalQSBMLid(ListOfParameters parameterList, String parameterName)
	{
		for (long i = 0; i < parameterList.size(); i++)
		{
			if (parameterName.equals(parameterList.get(i).getName()))
			{
				return parameterList.get(i).getId();
			}
		}
		
		return null;
	}
	
	// return string representation of a Node (with XML tags and full expansion) 
	private static String elementToString(Node n) 
	{
		String name = n.getNodeName();
		short type = n.getNodeType();
		if (Node.CDATA_SECTION_NODE == type) 
		{
		  return "<![CDATA[" + n.getNodeValue() + "]]&gt;";
		}
		
		if (name.startsWith("#"))
		{
			return "";
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append('<').append(name);
		
		NamedNodeMap attrs = n.getAttributes();
		if (attrs != null)
		{
			for (int i = 0; i < attrs.getLength(); i++)
			{
				Node attr = attrs.item(i);
				sb.append(' ').append(attr.getNodeName()).append("=\"").append(attr.getNodeValue()).append("\"");
			}
		}
		
		String textContent = null;
		NodeList children = n.getChildNodes();
		
		if (children.getLength() == 0)
		{
			if ((textContent = n.getTextContent()) != null && !"".equals(textContent))
			{
				sb.append(textContent).append("</").append(name).append('>');
			}
			else
			{	
				sb.append("/>").append(AC_Utility.eol);
			}
		}
		else
		{
			sb.append('>').append(AC_Utility.eol);
			boolean hasValidChildren = false;
			for (int i = 0; i < children.getLength(); i++)
			{
				String childToString = elementToString(children.item(i));
				if (!"".equals(childToString))
				{
					sb.append(childToString);
					hasValidChildren = true;
				}
			}
			
			if (!hasValidChildren && ((textContent = n.getTextContent()) != null))
			{
				sb.append(textContent);
			}
			sb.append("</").append(name).append('>');
		}
		return sb.toString();
	}
	
	private static Module importContainerModule(SBMLDocument doc, Module parent)
	{
		String moduleName = "";
		String definitionName = "";
		String sbmlID = "";
		String modString = "";
		boolean hasCompPackage = false;
		boolean hasLayoutPackage = false;
		CompModelPlugin modelCompPlugin = null;
		LayoutModelPlugin lplugin = null;
		GeneralGlyph glyph = null;
		Model containerMod;
		
		if (doc.getModel() == null)
		{
			// there is no Model in the sbml document
			System.err.println("Error in SBMLParser.importContainerModule: There is no Model in the SBML document.");
			return null;
		}
		if (!validateModuleDefinitionName(doc))
		{
			System.err.println("Error in SBMLParser.importContainerModule: validateModuleDefinitionName() returns false.");
			return null;
		}
		containerMod = doc.getModel();
		definitionName = containerMod.getName();
		if (parent == null)
		{
			// imported module will be the root module
			moduleName = definitionName;
		}
		else
		{
			// imported module will be a submodule
			moduleName = AC_Utility.promptUserForNewModuleName(parent, "Enter a Module name:");
		}
		sbmlID = containerMod.getId();
		
		if (doc.getLevel() < 3)
		{
			hasCompPackage = false;
			hasLayoutPackage = false;
		}
		else
		{
			// check if the document uses the comp package
			/*
			SBasePlugin plugin;
			plugin = doc.getPlugin("comp");
			if (plugin == null)
			{
				hasCompPackage = false;
			}
			*/
			if (doc.isPackageEnabled("comp"))
			{
				hasCompPackage = true;
			}
			// check if the document uses the layout package
			/*
			plugin = doc.getPlugin("layout");
			if (plugin == null)
			{
				hasLayoutPackage = false;
			}
			*/
			if (doc.isPackageEnabled("layout"))
			{
				hasLayoutPackage = true;
			}
		}
		
		if (hasCompPackage)
		{
			modelCompPlugin = (CompModelPlugin)containerMod.getPlugin("comp");
		}
		
		if (hasLayoutPackage)
		{
			lplugin = (LayoutModelPlugin)containerMod.getPlugin("layout");
			ListOfLayouts layouts = lplugin.getListOfLayouts();
			System.out.println("Number of layouts: " + layouts.size());
			Layout layout = layouts.get(0);
			//glyph = layout.getGeneralGlyph(name + "_glyph");
			glyph = layout.getGeneralGlyph(0);
		}
		
		/*
		System.out.println("Submodules number = " + modelCompPlugin.getNumSubmodels());
		System.out.println("Ports number = " + modelCompPlugin.getNumPorts());
		System.out.println("Layouts number = " + layouts.size());
		System.out.println("glyphID: " + glyph.getId());
		System.out.println("Number of containerMod replacedElements = " + modelCompPlugin.getNumReplacedElements());
		CompSBasePlugin compartmentCompPlugin = (CompSBasePlugin)containerMod.getListOfCompartments().get(0).getPlugin("comp");
		System.out.println("Number of compartment replacedElements = " + compartmentCompPlugin.getNumReplacedElements());
		// compartmentCompPlugin.getListOfReplacedElements().removeFromParentAndDelete();
		//layout.getListOfAdditionalGraphicalObjects().removeFromParentAndDelete();
		*/
		SBMLNamespaces ns = new SBMLNamespaces();
		ns.addNamespaces(doc.getNamespaces());
		SBMLDocument newDoc = new SBMLDocument(ns);
		int setModelCode = newDoc.setModel(containerMod);
		if(setModelCode != 0)
		{
			System.err.println("Error in SBMLParser.importContainerModule: setModelCode = " + setModelCode);
			return null;
		}
		
		//newDoc.setPackageRequired("layout", false);
		//newDoc.setPackageRequired("comp", false);
		removePluginData(newDoc);
		modString = libsbml.writeSBMLToString(newDoc);
		//System.out.println(modString);
		System.out.println("Validate container module: " + containerMod.getId());
		if (!checkValidSBML(modString))
		{
			System.err.println("The container Model is invalid.");
			return null;
		}
		System.out.println("Validation successful.");
		//modString = modString.replace("comp:required=\"false\"", "comp:required=\"true\" render:required=\"false\"");
		//modString = modString.replace("xmlns:comp=\"http://www.sbml.org/sbml/level3/version1/comp/version1\"", "");
		//modString = modString.replace("comp:required=\"false\"", "");
		//String name, GeneralGlyph glyph, boolean createDatamodel
		//Module mod = AC_GUI.xnewModule(name, sbmlID, modString, parent, glyph, true);
		ModuleDefinition definition;
		Module module = null;
		if (parent == null)
		{
			definition = AC_Utility.createModuleDefinition(sbmlID, definitionName, modString, null);
		}
		else
		{
			definition = AC_Utility.createModuleDefinition(sbmlID, definitionName, modString, parent.getModuleDefinition());
		}
		definitionList.add(definition);
		
		if (!hasCompPackage && !hasLayoutPackage)
		{
			module = AC_Utility.createInstance(moduleName, parent, definition);
		}
		
		if (hasCompPackage && hasLayoutPackage)
		{
			String glyphID = glyph.getId();
			String modulesbmlID = glyphID.substring(0, glyphID.length() - CONTAINER_MODULE_GLYPH_CODE.length());
			module = AC_Utility.createInstance(moduleName, modulesbmlID, parent, definition, glyph);
			// add visible variables and equivalence nodes
			ListOfGraphicalObjects subGlyphs = glyph.getListOfSubGlyphs();
			GeneralGlyph subGlyph;
			for (long i = 0; i < glyph.getNumSubGlyphs(); i++)
			{
				subGlyph = (GeneralGlyph)subGlyphs.get(i);
				if (subGlyph.getId().endsWith("_VisibleVariableGlyph"))
				{
					importVisibleVariable(module, subGlyph);
				}
				else if (subGlyph.getId().endsWith("_EquivalenceNodeGlyph"))
				{
					importEquivalence(module, subGlyph);
				}
			}
			// add ports
			if(modelCompPlugin.getNumPorts() > 0)
			{
				importPorts(modelCompPlugin.getListOfPorts(), containerMod, glyph, module);
			}
			
			// add submodules
			if(modelCompPlugin.getNumSubmodels() > 0)
			{
				CompSBMLDocumentPlugin docCompPlugin = (CompSBMLDocumentPlugin)doc.getPlugin("comp");
				SBMLNamespaces nameSpaces = new SBMLNamespaces();
				nameSpaces.addNamespaces(doc.getNamespaces());
				importSubmodules(modelCompPlugin.getListOfSubmodels(), docCompPlugin, nameSpaces, glyph, module);
			}
			// add connections
			if (containerMod.getNumSpecies() > 0)
			{
				importReplacementsFromSpecies(containerMod.getListOfSpecies(), module, true);
			}
		}
		//AC_GUI.changeActiveModule(mod);
		return module;
	}
	
	private static void importSubmodules(ListOfSubmodels submodules, CompSBMLDocumentPlugin docCompPlugin, SBMLNamespaces nameSpaces, GeneralGlyph parentGlyph, Module parent)
	{
		ListOfGraphicalObjects subGlyphs = parentGlyph.getListOfSubGlyphs();
		//System.out.println("Number of subglyphs: " + subGlyphs.size());
		ListOfModelDefinitions internalModuleDefinitions = docCompPlugin.getListOfModelDefinitions();
		ListOfExternalModelDefinitions externalModuleDefinitions = docCompPlugin.getListOfExternalModelDefinitions();
		String name = "";
		String sbmlID = "";
		String modelRef = "";
		String modString = "";
		String cellStyle = "";
		Submodel submodule;
		SBMLNamespaces ns;
		SBMLDocument newDoc;
		SBase genericDefinition;
		ModelDefinition internalModuleDefinition;
		ExternalModelDefinition externalModuleDefinition;
		Model moduleDefinition = null;
		ModuleDefinition definition;
		Module module;
		CompModelPlugin submodulePlugin;
		GeneralGlyph glyph = null;
		boolean externalDefinition;
		for(long i = 0; i < submodules.size(); i++)
		{
			externalDefinition = false;
			submodule = (Submodel)submodules.get(i);
			name = submodule.getName();
			sbmlID = submodule.getId();
			modelRef = submodule.getModelRef();
			internalModuleDefinition = internalModuleDefinitions.get(modelRef);
			externalModuleDefinition = externalModuleDefinitions.get(modelRef);
			glyph = (GeneralGlyph)subGlyphs.get(sbmlID + "_glyph");
			
			/*
			System.out.println("glyphID: " + glyph.getId());
			System.out.println("sbmlID: " + sbmlID);
			System.out.println("modelRef: " + modelRef);
			System.out.println("module Ref name: " + moduleDefinition.getName());
			System.out.println("module Ref model name: " + moduleDefinition.getModel().getName());
			*/
			
			//modString = modString.replace("xmlns:comp=\"http://www.sbml.org/sbml/level3/version1/comp/version1\"", "");
			//modString = modString.replace("comp:required=\"false\"", "");
			definition = getModuleDefinition(modelRef);
			if (definition == null)
			{
				if ((internalModuleDefinition != null) && (externalModuleDefinition == null))
				{
					// module definition is internal
					moduleDefinition = internalModuleDefinition;
					//System.out.println(moduleDefinition.toSBML());
					definition = importModuleDefinition(moduleDefinition, parent.getModuleDefinition(), nameSpaces, glyph);
				}
				else if ((internalModuleDefinition == null) && (externalModuleDefinition != null))
				{
					// module definition is external
					String externalSource = externalModuleDefinition.getSource();
					String md5 = externalModuleDefinition.getMd5();
					boolean validExternalFile = AC_Utility.validateExternalFile(externalSource, md5);
					if (validExternalFile)
					{
						//definition = importExternalDefinition(externalSource, externalModelRef);
						definition = importExternalDefinition(externalModuleDefinition, parent.getModuleDefinition(), nameSpaces, glyph);
						moduleDefinition = externalModuleDefinition.getReferencedModel();
						//System.out.println(moduleDefinition.toSBML());
					}
				}
				else
				{
					// no definition
					moduleDefinition = null;
				}
				
				if (definition == null)
				{
					System.err.println("Error in SBMLParser.importSubmodules: module definition is null.");
					return;
				}
				definitionList.add(definition);
			}
			if ((internalModuleDefinition != null) && (externalModuleDefinition == null))
			{
				// module definition is internal
				moduleDefinition = internalModuleDefinition;
			}
			else if ((internalModuleDefinition == null) && (externalModuleDefinition != null))
			{
				// module definition is external
				moduleDefinition = externalModuleDefinition.getReferencedModel();
			}
			else
			{
				// no definition
				moduleDefinition = null;
			}
			
			if (moduleDefinition == null)
			{
				System.err.println("Error in SBMLParser.importSubmodules: module definition is null.");
				return;
			}
			
			name = validateModuleName(parent, name);
			if (name == null)
			{
				System.err.println("Error in SBMLParser.importSubmodules: validateModuleName() returns null.");
				return;
			}
			module = AC_Utility.createInstance(name, sbmlID, parent, definition, glyph);
			
			// add visible variables and equivalence nodes
			ListOfGraphicalObjects submoduleSubGlyphs = glyph.getListOfSubGlyphs();
			GeneralGlyph subGlyph;
			for (long j = 0; j < glyph.getNumSubGlyphs(); j++)
			{
				subGlyph = (GeneralGlyph)submoduleSubGlyphs.get(j);
				if (subGlyph.getId().endsWith("_VisibleVariableGlyph"))
				{
					importVisibleVariable(module, subGlyph);
				}
				else if (subGlyph.getId().endsWith("_EquivalenceNodeGlyph"))
				{
					importEquivalence(module, subGlyph);
				}
			}
			
			submodulePlugin = (CompModelPlugin)moduleDefinition.getPlugin("comp");
			// add ports
			if(submodulePlugin.getNumPorts() > 0)
			{
				importPorts(submodulePlugin.getListOfPorts(), moduleDefinition, glyph, module);
			}
			// add submodules
			if(submodulePlugin.getNumSubmodels() > 0)
			{
				importSubmodules(submodulePlugin.getListOfSubmodels(), docCompPlugin, nameSpaces, glyph, module);
			}
			// add connections
			if (moduleDefinition.getNumSpecies() > 0)
			{
				importReplacementsFromSpecies(moduleDefinition.getListOfSpecies(), module, false);
			}
		}
	}
	
	private static ModuleDefinition importModuleDefinition(Model moduleDefinition, ModuleDefinition parent, SBMLNamespaces nameSpaces, GeneralGlyph glyph)
	{
		String sbmlID = moduleDefinition.getId();
		String name = moduleDefinition.getName();
		String sbmlString;
		SBMLDocument doc = new SBMLDocument(nameSpaces);
		
		int setModelCode = doc.setModel(moduleDefinition);
		if(setModelCode != 0)
		{
			System.err.println("Error in SBMLParser.importModuleDefinition: setModelCode = " + setModelCode);
			return null;
		}
		if (!validateModuleDefinitionName(doc))
		{
			System.err.println("Error in SBMLParser.importModuleDefinition: validateModuleDefinitionName() returns false.");
			return null;
		}
		//newDoc.setPackageRequired("layout", false);
		//newDoc.setPackageRequired("comp", false);
		removePluginData(doc);
		sbmlString = libsbml.writeSBMLToString(doc);
		//System.out.println(modString);
		System.out.println("Validate submodule: " + moduleDefinition.getId());
		if (!checkValidSBML(sbmlString))
		{
			System.err.println("Error in SBMLParser.importModuleDefinition: A submodel definition is invalid.");
			return null;
		}
		System.out.println("Validation successful.");
		
		if (glyph.getAnnotation() != null)
		{
			// the submodule is a MathematicalAggregator
			XMLNode modAnnotation = glyph.getAnnotation().getChild(0);
			String inputNumber = modAnnotation.getAttrValue("inputs", "http://www.copasi.org/softwareprojects");
			String op = modAnnotation.getAttrValue("type", "http://www.copasi.org/softwareprojects");
			Operation operation;
			if (op.equals(Operation.PRODUCT.toString()))
			{
				operation = Operation.PRODUCT;
			}
			else if (op.equals(Operation.SUM.toString()))
			{
				operation = Operation.SUM;
			}
			else
			{
				System.err.println("Error in SBMLParser.importModuleDefinition: Invalid operation for a MathematicalAggregator.");
				return null;
			}
			return AC_Utility.createMathematicalAggregatorDefinition(sbmlID, name, sbmlString, Integer.parseInt(inputNumber), operation, parent);
		}
		else
		{
			// the submodule is a Submodule
			return AC_Utility.createModuleDefinition(sbmlID, name, sbmlString, parent);
		}
	}

	private static void importPorts(ListOfPorts ports, Model parentModule, GeneralGlyph parentGlyph, Module mod)
	{
		String portIDRef;
		String portName;
		String portRefName;
		String portType;
		String varType;
		ModuleDefinition parentDefinition = mod.getModuleDefinition();
		ACComponentDefinition componentDefinition;
		PortDefinition portDefinition;
		org.sbml.libsbml.Port port;
		ListOfGraphicalObjects subGlyphs = parentGlyph.getListOfSubGlyphs();
		//System.out.println("parentGlyph name: " + parentGlyph.getName());
		//System.out.println("Number of subglyphs: " + parentGlyph.getNumSubGlyphs());
		GeneralGlyph portGlyph;
		XMLNode portAnnotation;
		for(long i = 0; i < ports.size(); i++)
		{
			port = (org.sbml.libsbml.Port)ports.get(i);
			portName = port.getId();
			portIDRef = port.getIdRef();
			portGlyph = (GeneralGlyph)subGlyphs.get(portName + "_PortGlyph");
			//System.out.println("port name: " + portName);
			if(portGlyph == null)
			{
				System.err.println("portGlyph = NULL");
			}
			//System.out.println("portGlyph name: " + portGlyph.getName());
			if (portGlyph.getAnnotation() == null)
			{
				System.err.println("portGlyph.getAnn = NULL");
			}
			portAnnotation = portGlyph.getAnnotation().getChild(0);
			//System.out.println("portAnnotation name: " + portAnnotation.getName());
			//System.out.println("portAnnotation namespace uri: " + portAnnotation.getNamespaceURI());
			portRefName = portAnnotation.getAttrValue("refName", "http://www.copasi.org/softwareprojects");
			portType = portAnnotation.getAttrValue("pType", "http://www.copasi.org/softwareprojects");
			varType = portAnnotation.getAttrValue("vType", "http://www.copasi.org/softwareprojects");
			
			// determine if the port definition has already been created
			if (parentDefinition.getInstances().size() > 1)
			{
				// this is not the first instantiation of the module definition
				// a port definition should already exist
				// only a port node needs to be created
				componentDefinition = getACComponentDefinition(parentDefinition.getPorts().listIterator(), portRefName);
				if (componentDefinition == null)
				{
					System.err.println("Error in SBMLParser.importPorts: componentDefinition for Port " + portName + " is null.");
					return;
				}
				else
				{
					portDefinition = (PortDefinition)componentDefinition;
					AC_Utility.createPortNode(mod, portName, portDefinition, portGlyph);
				}
			}
			else
			{
				// this is the first instantiation of the module definition
				// a port definition and port node need to be created
				//determine the porttype and vartype and then call acutility.createport
				PortType pType;
				VariableType vType;
				try
				{
					pType = PortType.getType(portType);
					vType = VariableType.getType(varType);
				}
				catch (Exception e)
				{
					System.err.println("Error in SBMLParser.importPorts: Imported PortType or VariableType is invalid.");
					e.printStackTrace();
					return;
				}
				AC_Utility.createPort(mod, portRefName, portName, pType, vType, portGlyph);
			}
			
			//AC_GUI.addPort(mod, portName, portRefName, portType, varType, portGlyph);
		}
	}
	
	private static void importVisibleVariable(Module parent, GeneralGlyph glyph)
	{
		ModuleDefinition parentDefinition = parent.getModuleDefinition();
		ACComponentDefinition componentDefinition;
		VisibleVariableDefinition visibleVariableDefinition;
		XMLNode annotation = glyph.getAnnotation().getChild(0);
		String refName = annotation.getAttrValue("name", "http://www.copasi.org/softwareprojects");
		String varType = annotation.getAttrValue("vType", "http://www.copasi.org/softwareprojects");
		
		// determine if the visible variable definition has already been created
		if (parentDefinition.getInstances().size() > 1)
		{
			// this is not the first instantiation of the module definition
			// a visible variable definition should already exist
			// only a visible variable node needs to be created
			componentDefinition = getACComponentDefinition(parentDefinition.getVisibleVariables().listIterator(), refName);
			if (componentDefinition == null)
			{
				System.err.println("Error in SBMLParser.importVisiableVariable: componentDefinition for VisibleVariable " + refName + " is null.");
				return;
			}
			else
			{
				visibleVariableDefinition = (VisibleVariableDefinition)componentDefinition;
				AC_Utility.createVisibleVariableNode(parent, visibleVariableDefinition, glyph);
			}
		}
		else
		{
			// this is the first instantiation of the module definition
			// a visible variable definition and visible variable node need to be created
			VariableType vType;
			try
			{
				vType = VariableType.getType(varType);
			}
			catch (Exception e)
			{
				System.err.println("Error in SBMLParser.importVisibleVariable: Imported VariableType is invalid.");
				e.printStackTrace();
				return;
			}
			AC_Utility.createVisibleVariable(refName, vType, parent, glyph);
		}
		
		//AC_GUI.addVisibleVariable(parent, refName, vType, glyph);
	}
	
	private static void importEquivalence(Module parent, GeneralGlyph glyph)
	{
		ModuleDefinition parentDefinition = parent.getModuleDefinition();
		ACComponentDefinition componentDefinition;
		EquivalenceDefinition equivalenceDefinition;
		XMLNode annotation = glyph.getAnnotation().getChild(0);
		String refName = annotation.getAttrValue("name", "http://www.copasi.org/softwareprojects");
		String varType = annotation.getAttrValue("vType", "http://www.copasi.org/softwareprojects");
		
		// determine if the equivalence definition has already been created
		if (parentDefinition.getInstances().size() > 1)
		{
			// this is not the first instantiation of the module definition
			// an equivalence definition should already exist
			// only an equivalence node needs to be created
			componentDefinition = getACComponentDefinition(parentDefinition.getEquivalences().listIterator(), refName);
			if (componentDefinition == null)
			{
				System.err.println("Error in SBMLParser.importEquivalence: componentDefinition for Equivalence " + refName + " is null.");
				return;
			}
			else
			{
				equivalenceDefinition = (EquivalenceDefinition)componentDefinition;
				AC_Utility.createEquivalenceNode(parent, equivalenceDefinition, glyph);
			}
		}
		else
		{
			// this is the first instantiation of the module definition
			// an equivalence definition and equivalence node need to be created
			VariableType vType;
			try
			{
				vType = VariableType.getType(varType);
			}
			catch (Exception e)
			{
				System.err.println("Error in SBMLParser.importEquivalence: Imported VariableType is invalid.");
				e.printStackTrace();
				return;
			}
			AC_Utility.createEquivalence(refName, vType, parent, glyph);
		}
		
		//AC_GUI.addEquivalenceNode(parent, refName, glyph);
	}
	
	private static void importReplacementsFromSpecies(ListOfSpecies speciesList, Module module, boolean fromContainer)
	{
		Species spec;
		CompSBasePlugin specPlugin;
		ListOfReplacedElements replacedElementsList;
		ReplacedElement repElement;
		for (long i = 0; i < speciesList.size(); i++)
		{
			spec = speciesList.get(i);
			specPlugin = (CompSBasePlugin)spec.getPlugin("comp");
			if (specPlugin.getNumReplacedElements() > 0)
			{
				importConnections(specPlugin.getListOfReplacedElements(), spec, module, fromContainer);
			}
		}
	}
	
	private static void importConnections(ListOfReplacedElements replacedElementsList, Species spec, Module module, boolean fromContainer)
	{
		Object node = findVariable(spec.getName(), module);
		Object variableDrawingCell;
		if (node instanceof VisibleVariableNode)
		{
			importVisibleVariableConnections((VisibleVariableNode)node, replacedElementsList, module, fromContainer);
		}
		else if (node instanceof EquivalenceNode)
		{
			importEquivalenceNodeConnections((EquivalenceNode)node, replacedElementsList, module, fromContainer);
		}
	}
	
	private static void importVisibleVariableConnections(VisibleVariableNode var, ListOfReplacedElements replacedElementsList, Module module, boolean fromContainer)
	{
		mxCell variableDrawingCell = var.getDrawingCell();
		String portRef;
		String submoduleRef;
		PortNode currentPort;
		ReplacedElement element;
		boolean inputToVisibleVariable;
		mxCell portDrawingCell;
		for (long i = 0; i < replacedElementsList.size(); i++)
		{
			element = (ReplacedElement)replacedElementsList.get(i);
			portRef = element.getPortRef();
			submoduleRef = element.getSubmodelRef();
			currentPort = findPort(portRef, submoduleRef, module, fromContainer);
			if (currentPort == null)
			{
				System.err.println("Error SBMLParser.importVisibleVariableConnections: Port " + portRef + " not found.");
				return;
			}
			
			switch(currentPort.getPortDefinition().getType())
			{
			case INPUT:
				if (currentPort.getParent() == var.getParent())
				{
					inputToVisibleVariable = true;
				}
				else
				{
					inputToVisibleVariable = false;
				}
				break;
			case OUTPUT:
				if (currentPort.getParent() == var.getParent())
				{
					inputToVisibleVariable = false;
				}
				else
				{
					inputToVisibleVariable = true;
				}
				break;
			default: inputToVisibleVariable = false;
			}
			
			portDrawingCell = currentPort.getDrawingCell();
			if (inputToVisibleVariable)
			{
				AC_GUI.addConnection(module, portDrawingCell, TerminalType.PORT, variableDrawingCell, TerminalType.VISIBLEVARIABLE, "DashedConnectionEdge");
			}
			else
			{
				AC_GUI.addConnection(module, variableDrawingCell, TerminalType.VISIBLEVARIABLE, portDrawingCell, TerminalType.PORT, "ConnectionEdge");
			}
		}
	}
	
	private static void importEquivalenceNodeConnections(EquivalenceNode eNode, ListOfReplacedElements replacedElementsList, Module module, boolean fromContainer)
	{
		mxCell eNodeDrawingCell = eNode.getDrawingCell();
		String portRef;
		String submoduleRef;
		PortNode currentPort;
		ReplacedElement element;
		mxCell portDrawingCell;
		for (long i = 0; i < replacedElementsList.size(); i++)
		{
			element = (ReplacedElement)replacedElementsList.get(i);
			portRef = element.getPortRef();
			submoduleRef = element.getSubmodelRef();
			currentPort = findPort(portRef, submoduleRef, module, fromContainer);
			if (currentPort == null)
			{
				System.err.println("Error SBMLParser.importSingleConnection: Port " + portRef + " not found.");
				return;
			}
			portDrawingCell = currentPort.getDrawingCell();
			AC_GUI.addConnection(module, eNodeDrawingCell, TerminalType.EQUIVALENCE, portDrawingCell, TerminalType.PORT, "ConnectionEdge");
		}
	}
	
	private static String fixReactionModifiers(Module mod, String output)
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(output)));
			doc.getDocumentElement().normalize();
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return output;
	}
	
	private static boolean exportLayoutInformation(Model model, Module mod)
	{
		/*
		SBMLDocument doc = libsbml.readSBMLFromString(sbml);
		long numErrors = doc.getNumErrors();

		if (numErrors > 0) {
			System.err.println("Encountered errors while reading the file. ");
			System.err.println("Please correct the following errors and try again.");
			doc.printErrors();
			System.exit(2);
		}
		Model model = doc.getModel();
		*/
		
		//System.out.println("Model id: " + model.getId());
		//System.out.println("Model name: " + model.getName());
		SBasePlugin basePlugin = (model.getPlugin("layout"));
		LayoutModelPlugin layoutPlugin = (LayoutModelPlugin)basePlugin;
		Layout layout = layoutPlugin.createLayout();
		layout.setId("Layout_0");
		
		GeneralGlyph gly = layout.createGeneralGlyph();
		gly.setId(mod.getID() + "_ContainerGlyph");
		//gly.setName("GeneralGlyph");
		BoundingBox box = new BoundingBox();
		Dimensions dim = new Dimensions ();
		dim.setHeight(((mxCell)mod.getDrawingCell()).getGeometry().getHeight());
		dim.setWidth(((mxCell)mod.getDrawingCell()).getGeometry().getWidth());
		Point point = new Point();
		point.setX(((mxCell)mod.getDrawingCell()).getGeometry().getX());
		point.setY(((mxCell)mod.getDrawingCell()).getGeometry().getY());
		box.setDimensions(dim);
		box.setPosition(point);
		gly.setBoundingBox(box);
		
		// add ports to the layout
		if(mod.getPorts().size() > 0)
		{
			addPortLayoutInformation(mod, gly);
		}
		
		// add visible variables to the layout
		if(mod.getVisibleVariables().size() > 0)
		{
			addVisibleVariableLayoutInformation(mod, gly);
		}
		
		// add equivalence nodes to the layout
		if(mod.getEquivalences().size() > 0)
		{
			addEquivalenceLayoutInformation(mod, gly);
		}
		
		// add submodules to the layout
		if(mod.getChildren().size() > 0)
		{
			addSubmoduleLayoutInformation(mod, gly);
		}
			
		//return libsbml.writeSBMLToString(doc);
		return true;
	}
	
	private static void addPortLayoutInformation(Module mod, GeneralGlyph parentGly)
	{
		PortNode node;
		PortDefinition definition;
		Point point;
		BoundingBox box;
		String portAnnotation;
		GeneralGlyph gly;
		ListIterator<ACComponentNode> listOfPorts = mod.getPorts().listIterator();
		while(listOfPorts.hasNext())
		{
			node = (PortNode)listOfPorts.next();
			definition = node.getPortDefinition();
			point = new Point();
			box = new BoundingBox();
			gly = new GeneralGlyph();
			
			point.setX(((mxCell)node.getDrawingCell()).getGeometry().getX());
			point.setY(((mxCell)node.getDrawingCell()).getGeometry().getY());
			box.setPosition(point);
			gly.setBoundingBox(box);
			gly.setId(definition.getName() + "_PortGlyph");
			portAnnotation = "<ac:portInfo";
			portAnnotation += " xmlns:ac=\"http://www.copasi.org/softwareprojects\"";
			portAnnotation += " ac:refName=\"" + definition.getRefName() + "\"";
			portAnnotation += " ac:name=\"" + definition.getName() + "\"";
			portAnnotation += " ac:pType=\"" + definition.getType().toString() + "\"";
			portAnnotation += " ac:vType=\"" + definition.getVariableType().toString() + "\"";
			portAnnotation += " ac:parentMod=\"" + node.getParent().getID() + "\"";
			portAnnotation += "/>";
			gly.appendAnnotation(XMLNode.convertStringToXMLNode(portAnnotation));
			
			parentGly.addSubGlyph(gly);
		}
	}
	
	private static void addVisibleVariableLayoutInformation(Module mod, GeneralGlyph parentGly)
	{
		VisibleVariableNode node;
		VisibleVariableDefinition definition;
		Point point;
		BoundingBox box;
		GeneralGlyph gly;
		String varAnnotation;
		ListIterator<ACComponentNode> vars = mod.getVisibleVariables().listIterator();
		while(vars.hasNext())
		{
			node = (VisibleVariableNode)vars.next();
			definition = node.getVisibleVariableDefinition();
			point = new Point();
			box = new BoundingBox();
			gly = new GeneralGlyph();
			
			point.setX(((mxCell)node.getDrawingCell()).getGeometry().getX());
			point.setY(((mxCell)node.getDrawingCell()).getGeometry().getY());
			box.setPosition(point);
			gly.setBoundingBox(box);
			gly.setId(definition.getRefName() + "_VisibleVariableGlyph");
			
			varAnnotation = "<ac:VisibleVariableInfo";
			varAnnotation += " xmlns:ac=\"http://www.copasi.org/softwareprojects\"";
			varAnnotation += " ac:name=\"" + definition.getRefName() + "\"";
			varAnnotation += " ac:vType=\"" + definition.getVariableType().toString() + "\"";
			varAnnotation += "/>";
			gly.appendAnnotation(XMLNode.convertStringToXMLNode(varAnnotation));
			
			parentGly.addSubGlyph(gly);
		}
	}
	
	private static void addEquivalenceLayoutInformation(Module mod, GeneralGlyph parentGly)
	{
		EquivalenceNode node;
		EquivalenceDefinition definition;
		Point point;
		BoundingBox box;
		GeneralGlyph gly;
		String eNodeAnnotation;
		ListIterator<ACComponentNode> eNodes = mod.getEquivalences().listIterator();
		while(eNodes.hasNext())
		{
			node = (EquivalenceNode)eNodes.next();
			definition = node.getEquivalenceDefinition();
			point = new Point();
			box = new BoundingBox();
			gly = new GeneralGlyph();
			
			point.setX(((mxCell)node.getDrawingCell()).getGeometry().getX());
			point.setY(((mxCell)node.getDrawingCell()).getGeometry().getY());
			box.setPosition(point);
			gly.setBoundingBox(box);
			gly.setId(definition.getRefName() + "_EquivalenceNodeGlyph");
			
			eNodeAnnotation = "<ac:EquivalenceNodeInfo";
			eNodeAnnotation += " xmlns:ac=\"http://www.copasi.org/softwareprojects\"";
			eNodeAnnotation += " ac:name=\"" + definition.getRefName() + "\"";
			eNodeAnnotation += " ac:vType=\"" + "Species" + "\"";
			eNodeAnnotation += "/>";
			gly.appendAnnotation(XMLNode.convertStringToXMLNode(eNodeAnnotation));
			
			parentGly.addSubGlyph(gly);
		}
	}
	
	private static void addSubmoduleLayoutInformation(Module mod, GeneralGlyph parentGly)
	{
		Module child;
		ModuleDefinition definition;
		Dimensions dim;
		Point point;
		BoundingBox box;
		String submoduleAnnotation;
		boolean isMathAgg;
		GeneralGlyph gly;
		ListIterator<Module> children = mod.getChildren().listIterator();
		System.out.println(mod.getID() + " has " + mod.getChildren().size() + " children.");
		while(children.hasNext())
		{
			child = children.next();
			definition = child.getModuleDefinition();
			dim = new Dimensions();
			point = new Point();
			box = new BoundingBox();
			gly = new GeneralGlyph();
			
			if (definition instanceof MathematicalAggregatorDefinition)
			{
				isMathAgg = true;
			}
			else
			{
				isMathAgg = false;
			}
			
			dim.setHeight(((mxCell)child.getDrawingCell()).getGeometry().getHeight());
			dim.setWidth(((mxCell)child.getDrawingCell()).getGeometry().getWidth());
			point.setX(((mxCell)child.getDrawingCell()).getGeometry().getX());
			point.setY(((mxCell)child.getDrawingCell()).getGeometry().getY());
			box.setDimensions(dim);
			box.setPosition(point);
			gly.setBoundingBox(box);
			gly.setId(child.getID() + "_glyph");
			
			if (isMathAgg)
			{
				MathematicalAggregatorDefinition maDefinition = (MathematicalAggregatorDefinition)definition;
				submoduleAnnotation = "<ac:MathAggInfo";
				submoduleAnnotation += " xmlns:ac=\"http://www.copasi.org/softwareprojects\"";
				submoduleAnnotation += " ac:type=\"" + maDefinition.getOperation().toString() + "\"";
				submoduleAnnotation += " ac:inputs=\"" + maDefinition.getNumberofInputs() + "\"";
				submoduleAnnotation += "/>";
				gly.appendAnnotation(XMLNode.convertStringToXMLNode(submoduleAnnotation));
			}
			
			// add ports to the layout
			if(child.getPorts().size() > 0)
			{
				addPortLayoutInformation(child, gly);
			}
			
			// add visible variables to the layout
			if(child.getVisibleVariables().size() > 0)
			{
				addVisibleVariableLayoutInformation(child, gly);
			}
			
			// add equivalence nodes to the layout
			if(child.getEquivalences().size() > 0)
			{
				addEquivalenceLayoutInformation(child, gly);
			}
			
			// add submodules to the layout
			if(child.getChildren().size() > 0)
			{
				addSubmoduleLayoutInformation(child, gly);
			}
			
			parentGly.addSubGlyph(gly);
			System.out.println("Added " + gly.getId() + " glyph.");
		}
	}
	
	private static Species getSpecies(ListOfSpecies speciesList, String name)
	{
		Species currentSpecies;
		for (long i = 0; i < speciesList.size(); i++)
		{
			currentSpecies = speciesList.get(i);
			if (name.equalsIgnoreCase(currentSpecies.getName()))
			{
				return currentSpecies;
			}
		}
		return null;
	}
	
	private static Parameter getParameter(ListOfParameters parameterList, String name)
	{
		Parameter currentParameter;
		for (long i = 0; i < parameterList.size(); i++)
		{
			currentParameter = parameterList.get(i);
			if (name.equalsIgnoreCase(currentParameter.getName()))
			{
				return currentParameter;
			}
		}
		return null;
	}
	
	private static String getSpeciesName(ListOfSpecies speciesList, String idRef)
	{
		Species spec = speciesList.get(idRef);
		if(spec == null)
		{
			System.err.println("Error getSpeciesName(" + idRef + ") returns NULL.");
			System.exit(0);
		}
		return spec.getName();
	}
	
	private static String getGlobalQuantityName(ListOfParameters parameterList, String idRef)
	{
		Parameter param = parameterList.get(idRef);
		if(param == null)
		{
			System.err.println("Error getGlobalQuantityName(" + idRef + ") returns NULL.");
			System.exit(0);
		}
		return param.getName();
	}
	
	private static Module findModule(String moduleID, Module module, boolean fromContainer)
	{
		if (fromContainer)
		{
			if (moduleID.equals(module.getModuleDefinition().getID()))
			{
				return module;
			}
		}
		
		if (moduleID.equals(module.getID()))
		{
			return module;
		}
		
		ListIterator<Module> children = module.getChildren().listIterator();
		Module child;
		
		while(children.hasNext())
		{
			child = children.next();
			if (moduleID.equals(child.getID()))
			{
				return child;
			}
		}
		return null;
	}
	
	private static PortNode findPort(String portName, String parentModuleID, Module module, boolean fromContainer)
	{
		Module portParentMod = findModule(parentModuleID, module, fromContainer);
		if (portParentMod == null)
		{
			System.err.println("Error SBMLParser.findPort: Module " + parentModuleID + " not found.");
			return null;
		}
		ListIterator<ACComponentNode> portList = portParentMod.getPorts().listIterator();
		PortNode port;
		while(portList.hasNext())
		{
			port = (PortNode)portList.next();
			if (portName.equals(port.getPortDefinition().getName()))
			{
				return port;
			}
		}
		return null;
	}
	
	private static void removePluginData(SBMLDocument document)
	{
		/*
		CompModelPlugin compPlugin = (CompModelPlugin)model.getPlugin("comp");
		// remove submodels
		if (compPlugin.getNumSubmodels() > 0)
		{
			compPlugin.getListOfSubmodels().removeFromParentAndDelete();
		}
		
		// remove ports
		if (compPlugin.getNumPorts() > 0)
		{
			compPlugin.getListOfPorts().removeFromParentAndDelete();
		}
		
		LayoutModelPlugin layoutPlugin = (LayoutModelPlugin)model.getPlugin("layout");
		// remove layouts
		if (layoutPlugin.getNumLayouts() > 0)
		{
			layoutPlugin.getListOfLayouts().removeFromParentAndDelete();
		}
		
		// remove replaced elements
		if (model.getNumCompartments() > 0)
		{
			removeReplacedElements(model.getListOfCompartments());
		}
		
		if (model.getNumSpecies() > 0)
		{
			removeReplacedElements(model.getListOfSpecies());
		}
		
		if (model.getNumParameters() > 0)
		{
			removeReplacedElements(model.getListOfParameters());
		}
		*/
		SBasePlugin plugin;
		
		try
		{
			// remove the render package
			plugin = document.getPlugin("render");
			if (plugin == null)
			{
				System.out.println("The render package is not present.");
			}
			else
			{
				document.disablePackage(plugin.getURI(), plugin.getPrefix());
			}
		}
		catch (Exception e)
		{
			System.err.println("Error disabling the render package.");
			e.printStackTrace();
		}
		
		try
		{
			// remove the comp package
			plugin = document.getPlugin("comp");
			if (plugin == null)
			{
				System.out.println("The comp package is not present.");
			}
			else
			{
				document.disablePackage(plugin.getURI(), plugin.getPrefix());
			}
		}
		catch (Exception e)
		{
			System.err.println("Error disabling the comp package.");
			e.printStackTrace();
		}
		
		try
		{
			// remove the layout package
			plugin = document.getPlugin("layout");
			if (plugin == null)
			{
				System.out.println("The layout package is not present.");
			}
			else
			{
				document.disablePackage(plugin.getURI(), plugin.getPrefix());
			}
		}
		catch (Exception e)
		{
			System.err.println("Error disabling the layout package.");
			e.printStackTrace();
		}
	}
	
	private static void removeReplacedElements(ListOf list)
	{
		SBase item;
		CompSBasePlugin itemPlugin;
		for (long i = 0; i < list.size(); i++)
		{
			item = list.get(i);
			itemPlugin = (CompSBasePlugin)item.getPlugin("comp");
			if (itemPlugin.getNumReplacedElements() > 0)
			{
				itemPlugin.getListOfReplacedElements().removeFromParentAndDelete();
			}
		}
	}
	
	private static boolean checkValidSBML(String sbml)
	{
		SBMLDocument sdoc = libsbml.readSBMLFromString(sbml);
		System.out.println("Document Level: " + sdoc.getLevel());
		System.out.println("Document Version: " + sdoc.getVersion());

		if (sdoc.getNumErrors() > 0) {
			System.err.println("Encountered errors while reading the file. ");
			System.err.println("Please correct the following errors and try again.");
			sdoc.printErrors();
			return false;
		}
		return true;
		//libsbml.writeSBMLToFile(sdoc, "_" + sdoc.getModel().getId() + "_debug.xml");
	}
	
	private static void setSBMLNamespaces(SBMLDocument doc, XMLNamespaces importedNamespaces)
	{
		// create the new SBMLNamspace with level 3 version 1
		SBMLNamespaces sbmlns = new SBMLNamespaces(3, 1);
		
		// add the Layout and Comp packages
		sbmlns.addPackageNamespace("layout", 1);
		sbmlns.addPackageNamespace("comp", 1);
		
		// add a list of default namespaces
		sbmlns.addNamespace("http://www.w3.org/1999/xhtml", "html");
		sbmlns.addNamespace("http://www.sbml.org/2001/ns/jigcell", "jigcell");
		sbmlns.addNamespace("http://www.w3.org/1998/Math/MathML", "math");
		
		// add any namespaces that were imported with the model
		sbmlns.addNamespaces(importedNamespaces);
		
		// set the namespaces for the document
		System.out.println("set namespaces: " + doc.setNamespaces(sbmlns.getNamespaces()));
		//System.out.println(doc.isSetPackageRequired("comp"));
		doc.setPackageRequired("comp", true);
		doc.setPackageRequired("layout", false);
		doc.setPackageRequired("render", false);
		//System.out.println(doc.isSetPackageRequired("comp"));
	}
	
	private static void generateModuleIDs(Module mod)
	{
		int count;
		String prefix;
		String id;

		count = 0;
		prefix = "Model_";
		
		id = prefix + count;
		mod.setID(id);
		
		ListIterator<Module> children = mod.getChildren().listIterator();
		Module child;
	
		count++;
		while(children.hasNext())
		{
			child = children.next();
			id = prefix + count;
			
			child.setID(id);
			
			count++;
		}
	}
	
	private static String validateModuleName(Module parent, String name)
	{		
		if (name == null || name.isEmpty())
		{
			String message = "The imported Module does not have a name.";
			JOptionPane.showMessageDialog(null, message, "Invalid Name", JOptionPane.WARNING_MESSAGE);
			
			name = AC_Utility.promptUserForNewModuleName(parent, "Enter a Module name:");
		}
		else
		{
			if (!AC_Utility.newModuleNameValidation(parent, name, true))
			{
				name = AC_Utility.promptUserForNewModuleName(parent, "Enter a Module name:");	
			}
		}
		
		return name;
	}
	
	private static boolean validateModuleDefinitionName(SBMLDocument document)
	{
		String name = document.getModel().getName();
		
		if (name == null || name.isEmpty())
		{
			String message = "The imported Module Template does not have a name.";
			JOptionPane.showMessageDialog(null, message, "Invalid Name", JOptionPane.WARNING_MESSAGE);
			
			name = AC_Utility.promptUserForNewModuleDefinitionName("Enter a Module Template name:");
		}
		else
		{
			if (!AC_Utility.moduleDefinitionNameValidation(name, true))
			{
				name = AC_Utility.promptUserForNewModuleDefinitionName("Enter a Module Template name:");	
			}
		}
		
		if (name == null)
		{
			return false;
		}
		
		document.getModel().setName(name);
		return true;
	}
	
	private static boolean validateContainerModuleName(SBMLDocument document)
	{
		String name = document.getModel().getName();
		
		if (name == null || name.isEmpty())
		{
			String message = "The imported Module does not have a name.";
			JOptionPane.showMessageDialog(null, message, "Invalid Name", JOptionPane.WARNING_MESSAGE);
			
			name = AC_Utility.promptUserForNewModuleName(null, "Enter a Module name:");
		}
		else
		{
			if (!AC_Utility.newModuleNameValidation(null, name, true))
			{
				name = AC_Utility.promptUserForNewModuleName(null, "Enter a Module name:");	
			}
		}
		
		if (name == null)
		{
			return false;
		}
		
		document.getModel().setName(name);
		return true;
	}
	
	private static String generateContainerModuleDefinitionName(String moduleName)
	{
		int index = 0;
		String base = moduleName + "Template";
		String candidate = base;
		while (!AC_Utility.moduleDefinitionNameValidation(candidate, false))
		{
			index++;
			candidate = base + "_" + index;
		}
		return candidate;
	}
	
	private static ModuleDefinition getModuleDefinition(String id)
	{
		ModuleDefinition currentDefinition;
		ListIterator<ModuleDefinition> list = definitionList.listIterator();
		while (list.hasNext())
		{
			currentDefinition = list.next();
			if (currentDefinition.getID().equals(id))
			{
				return currentDefinition;
			}
		}
		return null;
	}
	
	private static ACComponentDefinition getACComponentDefinition(ListIterator<ACComponentDefinition> list, String refName)
	{
		ACComponentDefinition currentDefinition;
		while (list.hasNext())
		{
			currentDefinition = list.next();
			if (currentDefinition.getRefName().equals(refName))
			{
				return currentDefinition;
			}
		}
		return null;
	}
	
	private static void prependNameToComponents(SBMLDocument doc, String name)
	{
		if (!prependNameToSpecies(doc, name))
		{
			System.err.println("SBMLParser.prependNameToVariables: prependNameToSpecies was unsuccessful.");
		}
		
		if (!prependNameToParameters(doc,name))
		{
			System.err.println("SBMLParser.prependNameToVariables: prependNameToParameters was unsuccessful.");
		}
		
		if (!prependNameToCompartments(doc, name))
		{
			System.err.println("SBMLParser.prependNameToVariables: prependNameToCompartments was unsuccessful.");
		}
		
		if (!prependNameToReactions(doc, name))
		{
			System.err.println("SBMLParser.prependNameToVariables: prependNameToReactionss was unsuccessful.");
		}
	}
	
	private static boolean prependNameToSpecies(SBMLDocument doc, String name)
	{
		ListOfSpecies list = doc.getModel().getListOfSpecies();
		String newName;
		Species species;
		for (long i = 0; i < list.size(); i++)
		{
			species = list.get(i);
			newName = name + "_" + species.getName();
			if (species.setName(newName) != libsbml.LIBSBML_OPERATION_SUCCESS)
			{
				System.err.println("Error in SBMLParser.prependNameToSpecies: could not rename species " + species.getName() + ".");
				return false;
			}
		}
		return true;
	}
	
	private static boolean prependNameToParameters(SBMLDocument doc, String name)
	{
		ListOfParameters list = doc.getModel().getListOfParameters();
		String newName;
		Parameter parameter;
		for (long i = 0; i < list.size(); i++)
		{
			parameter = list.get(i);
			newName = name + "_" + parameter.getName();
			if (parameter.setName(newName) != libsbml.LIBSBML_OPERATION_SUCCESS)
			{
				System.err.println("Error in SBMLParser.prependNameToParameters: could not rename parameter " + parameter.getName() + ".");
				return false;
			}
		}
		return true;
	}
	
	private static boolean prependNameToCompartments(SBMLDocument doc, String name)
	{
		ListOfCompartments list = doc.getModel().getListOfCompartments();
		String newName;
		Compartment compartment;
		for (long i = 0; i < list.size(); i++)
		{
			compartment = list.get(i);
			newName = name + "_" + compartment.getName();
			if (compartment.setName(newName) != libsbml.LIBSBML_OPERATION_SUCCESS)
			{
				System.err.println("Error in SBMLParser.prependNameToCompartments: could not rename compartment " + compartment.getName() + ".");
				return false;
			}
		}
		return true;
	}
	
	private static boolean prependNameToReactions(SBMLDocument doc, String name)
	{
		ListOfReactions list = doc.getModel().getListOfReactions();
		String newName;
		Reaction reaction;
		for (long i = 0; i < list.size(); i++)
		{
			reaction = list.get(i);
			newName = name + "_" + reaction.getName();
			if (reaction.setName(newName) != libsbml.LIBSBML_OPERATION_SUCCESS)
			{
				System.err.println("Error in SBMLParser.prependNameToReactionss: could not rename reaction " + reaction.getName() + ".");
				return false;
			}
		}
		return true;
	}
	
	private static String ensureCompIsEnabled(String sbmlString)
	{
		SBMLDocument document = libsbml.readSBMLFromString(sbmlString);
		if (document.getNumErrors(libsbml.LIBSBML_SEV_ERROR) > 0)
		{
			// real error to deal with ... stop here
			System.err.println("Error: SBMLParser.ensureCompIsEnabled(), document contains errors.");
			//return sbmlString;
			return null;
		}
		
		if (document.isPackageEnabled("comp"))
		{
			// nothing to do
			return sbmlString;
		}
		
		// flip on comp
		if (document.enablePackage("http://www.sbml.org/sbml/level3/version1/comp/version1", "comp", true) 
			!= libsbml.LIBSBML_OPERATION_SUCCESS)
		{
			// could not enable comp ... panic
			System.err.println("Error: SBMLParser.ensureCompIsEnabled(), could not enable COMP package.");
			//return sbmlString;
			return null;
		}
		
		// set required flag
		document.setPackageRequired("comp", true);
		
		// return string model
		return libsbml.writeSBMLToString(document);	
	}
}
