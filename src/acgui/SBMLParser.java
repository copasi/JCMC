package acgui;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ListIterator;
import java.util.Vector;

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

	private PrintWriter out;
	private String modelSBML;
	private String fileName;
	private static String eol = System.getProperty("line.separator");
	
	/**
	 * Construct the object.
	 */
	public SBMLParser()
	{
		//System.out.println(CCopasiRootContainer.getDatamodelList().size());
		out = null;
		modelSBML = "";
		fileName = "";
	}
	
	public static void exportSBML(Module rootModule, String fName)
	{
		System.out.println("getContainerDefinition() start.");
		SBMLDocument sdoc = exportContainerDefinition(rootModule);
		System.out.println("getContainerDefinition() end.");
		
		libsbml.writeSBMLToFile(sdoc, fName);
	}
	
	public static Module importSBML(String fileName)
	{
		return importSBML(fileName, null);
	}
	
	public static Module importSBML(String fileName, Module parent)
	{
		Module mod = null;
		try
		{
			/*
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(fileName);
			doc.getDocumentElement().normalize();
			*/
			
			SBMLDocument sdoc = libsbml.readSBMLFromFile(fileName);
			//System.out.println("Document Level: " + sdoc.getLevel());
			//System.out.println("Document Version: " + sdoc.getVersion());
			long numErrors = sdoc.getNumErrors();

			if (numErrors > 0) {
				System.err.println("Encountered errors while reading the file. ");
				System.err.println("Please correct the following errors and try again.");
				sdoc.printErrors();
				System.exit(2);
			}
			//importContainerModule(doc, sdoc);
			mod =  importContainerModule(sdoc, parent);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return mod;
	}
	
	public static SBMLDocument SBMLValidation(String fileName)
	{
		return libsbml.readSBMLFromFile(fileName);
	}
	
	private static SBMLDocument exportContainerDefinition(Module containerModule)
	{
		SBMLNamespaces sbmlns = new SBMLNamespaces(3, 1);
		sbmlns.addPackageNamespace("layout", 1);
		sbmlns.addPackageNamespace("comp", 1);
		
		SBMLDocument sdoc = new SBMLDocument(sbmlns);
		//sdoc.setPackageRequired("layout", false);
		//sdoc.setPackageRequired("comp", true);
		//System.out.println(sdoc.setPackageRequired("layout", false));
		//System.out.println(sdoc.setPackageRequired("comp", true));
		
		String modSBML = "";
		CCopasiDataModel dataModel;
		System.out.println("getCopasiModelFromKey() start.");
		dataModel = AC_GUI.copasiUtility.getCopasiModelFromKey(containerModule.getKey());
		if (dataModel == null)
		{
			System.err.println(containerModule.getName() + "'s dataModel is empty");
		}
		System.out.println("getCopasiModelFromKey() end.");
		
		try
		{
			System.out.println("exportSBMLToString() start.");
			modSBML = dataModel.exportSBMLToString(3, 1);
			System.out.println("exportSBMLToString() end.");
			System.out.println("removeCOPASIMetaID() start.");
			modSBML = removeCOPASIMetaID(modSBML);			
			System.out.println("removeCOPASIMetaID() end.");
			System.out.println("removeRenderPackage() start.");
			modSBML = removeRenderPackage(modSBML);
			System.out.println("removeRenderPackage() end.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		//System.out.println();
		//System.out.println();
		//System.out.println(modSBML);
		//System.out.println();
		//System.out.println();
		SBMLDocument tempDoc = libsbml.readSBMLFromString(modSBML);
		//System.out.println("Document Level: " + sdoc.getLevel());
		//System.out.println("Document Version: " + sdoc.getVersion());
		long numErrors = tempDoc.getNumErrors();

		if (numErrors > 0) {
			System.err.println("Encountered errors while reading the file. ");
			System.err.println("Please correct the following errors and try again.");
			tempDoc.printErrors();
			//System.exit(2);
		}
		
		if (tempDoc.getModel() == null)
		{
			System.err.println("tempDoc.getModel() is null.");
		}
		System.out.println(sdoc.setNamespaces(tempDoc.getNamespaces()));
		System.out.println(sdoc.setModel(tempDoc.getModel()));
		setSBMLNamespaces(sdoc, tempDoc.getNamespaces());
		sdoc.getModel().setId(containerModule.getKey());
		
		if (sdoc.getModel() == null)
		{
			System.err.println("sdoc.getModel() is null.");
		}
		sdoc.getModel().enablePackage("http://www.sbml.org/sbml/level3/version1/comp/version1", "comp", true);
		sdoc.getModel().enablePackage("http://www.sbml.org/sbml/level3/version1/layout/version1", "layout", true);
		sdoc.setPackageRequired("layout", false);
		sdoc.setPackageRequired("comp", true);
		
		CompSBMLDocumentPlugin docPlugin = (CompSBMLDocumentPlugin)sdoc.getPlugin("comp");
		
		if (!containerModule.getPorts().isEmpty())
		{
			exportPortDefinitions(sdoc.getModel(), containerModule);
		}
		
		if (!containerModule.getChildren().isEmpty())
		{			
			System.out.println("getModelDefinitions() start.");
			exportSubmodelDefinitions(docPlugin, containerModule);
			System.out.println("getModelDefinitions() end.");
			
			System.out.println("getSubmodelInformation() start.");
			exportSubmodelInformation(sdoc.getModel(), containerModule);
			System.out.println("getSubmodelInformation() end.");
			
			//System.out.println(libsbml.writeSBMLToString(sdoc));
		}
		
		System.out.println("addReplacements() start.");
		exportReplacements(docPlugin, sdoc.getModel(), containerModule);
		System.out.println("addReplacements() end.");

		System.out.println("addLayoutInformation start.");
		exportLayoutInformation(sdoc.getModel(), containerModule);
		System.out.println("addLayoutInformation end.");
		
		return sdoc;
	}
	
	/**
	 * Return the model definitions.
	 * @param mod the model to output
	 */
	private static void exportSubmodelDefinitions(CompSBMLDocumentPlugin docPlugin, Module mod)
	{
		ListIterator<Module> children = mod.getChildren().listIterator();
		Module child;
		CCopasiDataModel dataModel;
		
		//CompSBMLDocumentPlugin docPlugin = (CompSBMLDocumentPlugin)sdoc.getPlugin("comp");
		//ListOfModelDefinitions moduleDefinitions = docPlugin.getListOfModelDefinitions();
		ModelDefinition moduleDefinition;
		String modSBML;
		SBMLDocument tempDoc;
		while(children.hasNext())
		{
			child = children.next();
			dataModel = AC_GUI.copasiUtility.getCopasiModelFromKey(child.getKey());
			if (dataModel == null)
			{
				System.err.println(child.getName() + "'s dataModel is empty");
			}
			//hasPorts = (child.getPorts().size() != 0);
			//System.out.println("hasPorts = " + hasPorts);
			
			try
			{
				modSBML = dataModel.exportSBMLToString(3, 1);
				modSBML = removeCOPASIMetaID(modSBML);
				modSBML = removeRenderPackage(modSBML);
				tempDoc = libsbml.readSBMLFromString(modSBML);
				tempDoc.getModel().setId(child.getKey());
				moduleDefinition = new ModelDefinition(tempDoc.getModel());
				
				if (!child.getPorts().isEmpty())
				{
					exportPortDefinitions(moduleDefinition, child);
				}
				
				if (!child.getChildren().isEmpty())
				{
					exportSubmodelDefinitions(docPlugin, child);
					
					exportSubmodelInformation(moduleDefinition, child);
				}
				
				exportReplacements(docPlugin, moduleDefinition, child);
				
				docPlugin.addModelDefinition(moduleDefinition);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private static void exportPortDefinitions(Model model, Module mod)
	{		
		ListIterator<Port> ports = mod.getPorts().listIterator();
		acgui.Port modulePort;
		String idRef;
		String id;
		
		CompModelPlugin compModulePlugin = (CompModelPlugin)model.getPlugin("comp");
		org.sbml.libsbml.Port modelPort;
		// <comp:port comp:idRef="Metabolite_2" comp:id="CycBPort"/>
		
		while(ports.hasNext())
		{
			modulePort = ports.next();
			
			id = modulePort.getName();
			
			switch(modulePort.getVariableType())
			{
			case SPECIES:
				idRef = getSpeciesSBMLid(model.getListOfSpecies(), modulePort.getRefName());
				break;
			case GLOBAL_QUANTITY:
				idRef = getGlobalQSBMLid(model.getListOfParameters(), modulePort.getRefName());
				break;
			default:
				idRef = null;
			}
			
			if (idRef == null)
			{
				System.err.println("Error SBMLParser.exportPortDefinitions(): Port " + id + "'s sbmlID cannot be found.");
			}
			
			modelPort = new org.sbml.libsbml.Port();
			modelPort.setIdRef(idRef);
			modelPort.setId(id);
			
			compModulePlugin.addPort(modelPort);
		}
	}
	
	/**
	 * Write the submodel information of the given module.
	 * @param mod the module containing the submodels
	 */
	private static void exportSubmodelInformation(Model model, Module parent)
	{
		ListIterator<Module> children = parent.getChildren().listIterator();
		Module child;
		CompModelPlugin compModulePlugin = (CompModelPlugin)model.getModel().getPlugin("comp");
		Submodel submodule;
		
		String id = "";
		String modelRef = "";
		
		while(children.hasNext())
		{
			child = children.next();
			id = child.getName();
			modelRef = child.getKey();
			
			submodule = new Submodel();
			submodule.setId(id);
			submodule.setModelRef(modelRef);
			
			compModulePlugin.addSubmodel(submodule);
		}
	}
	
	private static void exportReplacements(CompSBMLDocumentPlugin docPlugin, Model model, Module parent)
	{
		if (!parent.getChildren().isEmpty())
		{
			exportReplacementCompartments(docPlugin, model, parent);
		}
		
		if (!AC_GUI.modelBuilder.isSpeciesListEmpty())
		{
			exportVisibleVariables(model, parent);
			exportEquivalenceNodes(model, parent);
		}
	}
	
	private static void exportReplacementCompartments(CompSBMLDocumentPlugin docPlugin, Model model, Module module)
	{
		ListIterator<Module> children = module.getChildren().listIterator();
		Module child;
		String submodelRef;
		String modelRef;
		String idRef;
		//CompSBMLDocumentPlugin docPlugin = (CompSBMLDocumentPlugin)sdoc.getPlugin("comp");
		
		Compartment compartment = model.getCompartment(0);
		CompSBasePlugin compartmentPlugin = (CompSBasePlugin)compartment.getPlugin("comp");
		ReplacedElement replacedElement;
		while(children.hasNext())
		{
			// <comp:replacedElement comp:idRef="comp" comp:submodelRef="A"/>
			child = children.next();
			modelRef = child.getKey();
			submodelRef = child.getName();
			System.out.println("child: " + submodelRef);
			System.out.println("modelRef: " + modelRef);
			if (docPlugin.getModelDefinition(modelRef) == null)
			{
				System.err.println("model definition is null");
			}
			if (docPlugin.getModelDefinition(modelRef).getCompartment(0) == null)
			{
				System.err.println("model compartment is null");
			}
			idRef = docPlugin.getModelDefinition(modelRef).getCompartment(0).getId();
			
			replacedElement = new ReplacedElement();
			replacedElement.setIdRef(idRef);
			replacedElement.setSubmodelRef(submodelRef);
			compartmentPlugin.addReplacedElement(replacedElement);
		}
	}
	
	private static Object findVariable(String name, Module mod)
	{
		ListIterator<VisibleVariable> vars = mod.getVisibleVariables().listIterator();
		VisibleVariable currentVar;
		while(vars.hasNext())
		{
			currentVar = vars.next();
			if (name.equalsIgnoreCase(currentVar.getRefName()))
			{
				return currentVar;
			}
		}
		
		ListIterator<EquivalenceNode> eNodes = mod.getEquivalenceNodes().listIterator();
		EquivalenceNode currenteNode;
		while(eNodes.hasNext())
		{
			currenteNode = eNodes.next();
			if (name.equalsIgnoreCase(currenteNode.getRefName()))
			{
				return currenteNode;
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
	
	private static void exportVisibleVariables(Model model, Module parent)
	{
		ListIterator<VisibleVariable> vars = parent.getVisibleVariables().listIterator();
		while(vars.hasNext())
		{
			xaddVisibleVariableReplacements(model, vars.next());
		}
	}
	
	private static void xaddVisibleVariableReplacements(Model model, VisibleVariable var)
	{
		Object outgoingEdges [] = mxGraphModel.getOutgoingEdges(AC_GUI.drawingBoard.getGraph().getModel(), var.getDrawingCell());
		Object incomingEdges [] = mxGraphModel.getIncomingEdges(AC_GUI.drawingBoard.getGraph().getModel(), var.getDrawingCell());
		
		if ((outgoingEdges.length == 0) && (incomingEdges.length == 0))
		{
			return;
		}
		
		Species species = getSpecies(model.getListOfSpecies(), var.getRefName());
		if (species == null)
		{
			System.err.println("Error SBMLParser.xaddVisibleVariableReplacements(): Species " + var.getRefName() + " not found.");
			return;
		}
		CompSBasePlugin speciesPlugin = (CompSBasePlugin)species.getPlugin("comp");
		ReplacedElement replacedElement;
		String portRef;
		String submodelRef;
		
		if (outgoingEdges.length != 0)
		{			
			for (int i = 0; i < outgoingEdges.length; i++)
			{
				// <comp:replacedElement comp:portRef="S" comp:submodelRef="A"/>
				portRef = ((Port)((mxCell)outgoingEdges[i]).getTarget().getValue()).getName();
				submodelRef = ((Port)((mxCell)outgoingEdges[i]).getTarget().getValue()).getParent().getName();
				replacedElement = new ReplacedElement();
				replacedElement.setPortRef(portRef);
				replacedElement.setSubmodelRef(submodelRef);
				speciesPlugin.addReplacedElement(replacedElement);
			}
		}
		
		if (incomingEdges.length != 0)
		{
			// <comp:replacedBy comp:portRef="D_port" comp:submodelRef="B"/>
			portRef = ((Port)((mxCell)incomingEdges[0]).getSource().getValue()).getName();
			submodelRef = ((Port)((mxCell)incomingEdges[0]).getSource().getValue()).getParent().getName();
			replacedElement = new ReplacedElement();
			replacedElement.setPortRef(portRef);
			replacedElement.setSubmodelRef(submodelRef);
			speciesPlugin.addReplacedElement(replacedElement);
		}
	}
	
	private static void exportEquivalenceNodes(Model model, Module parent)
	{
		ListIterator<EquivalenceNode> eNodes = parent.getEquivalenceNodes().listIterator();
		while(eNodes.hasNext())
		{
			xaddEquivalenceNodeReplacements(model, eNodes.next());
		}
	}
	
	private static void xaddEquivalenceNodeReplacements(Model model, EquivalenceNode eNode)
	{
		Object outgoingEdges [] = mxGraphModel.getOutgoingEdges(AC_GUI.drawingBoard.getGraph().getModel(), eNode.getDrawingCell());
		
		if (outgoingEdges.length == 0)
		{
			return;
		}
		
		Species species = getSpecies(model.getListOfSpecies(), eNode.getRefName());
		if (species == null)
		{
			System.err.println("Error SBMLParser.xaddEquivalenceNodeReplacements(): Species " + eNode.getRefName() + " not found.");
			return;
		}
		CompSBasePlugin speciesPlugin = (CompSBasePlugin)species.getPlugin("comp");
		ReplacedElement replacedElement;
		String portRef;
		String submodelRef;
		
		for (int i = 0; i < outgoingEdges.length; i++)
		{
			// <comp:replacedElement comp:portRef="S" comp:submodelRef="A"/>
			portRef = ((Port)((mxCell)outgoingEdges[i]).getTarget().getValue()).getName();
			submodelRef = ((Port)((mxCell)outgoingEdges[i]).getTarget().getValue()).getParent().getName();
			replacedElement = new ReplacedElement();
			replacedElement.setPortRef(portRef);
			replacedElement.setSubmodelRef(submodelRef);
			speciesPlugin.addReplacedElement(replacedElement);
		}
	}
	
	private String printModelCompHeader()
	{
		String header;
		header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + eol;
		header += "<sbml xmlns=\"http://www.sbml.org/sbml/level3/version1/core\" level=\"3\" version=\"1\" " + eol;
		header += "xmlns:comp=\"http://www.sbml.org/sbml/level3/version1/comp/version1\" comp:required=\"true\"" + eol;
		header += "xmlns:layout=\"http://www.sbml.org/sbml/level3/version1/layout/version1\" layout:required=\"false\"" + eol;
		header += "xmlns:html=\"http://www.w3.org/1999/xhtml\"" + eol;
		header += "xmlns:jigcell=\"http://www.sbml.org/2001/ns/jigcell\"" + eol;
		header += "xmlns:math=\"http://www.w3.org/1998/Math/MathML\">\n" + eol;
		
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
	
	private static String getSpeciesSBMLid(ListOfSpecies speciesList, String speciesName)
	{
		for (long i = 0; i < speciesList.size(); i++)
		{
			if (speciesName.equalsIgnoreCase(speciesList.get(i).getName()))
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
			if (parameterName.equalsIgnoreCase(parameterList.get(i).getName()))
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
				sb.append("/>").append(eol);
			}
		}
		else
		{
			sb.append('>').append(eol);
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
		String name = "";
		String sbmlID = "";
		String modString = "";
		Model containerMod = doc.getModel();
		name = containerMod.getName();
		sbmlID = containerMod.getId();
		
		CompModelPlugin modelCompPlugin = (CompModelPlugin)containerMod.getPlugin("comp");
		
		LayoutModelPlugin lplugin = (LayoutModelPlugin)containerMod.getPlugin("layout");
		ListOfLayouts layouts = lplugin.getListOfLayouts();
		Layout layout = layouts.get(0);
		GeneralGlyph glyph = layout.getGeneralGlyph(name + "_glyph");
		
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
			System.exit(0);
		}
		newDoc.setPackageRequired("layout", false);
		newDoc.setPackageRequired("comp", false);
		removePluginData(newDoc.getModel());
		modString = libsbml.writeSBMLToString(newDoc);
		//System.out.println(modString);
		System.out.println("Validate container module: " + containerMod.getId());
		checkValidSBML(modString);
		System.out.println("Validation successful.");
		//modString = modString.replace("comp:required=\"false\"", "comp:required=\"true\" render:required=\"false\"");
		modString = modString.replace("xmlns:comp=\"http://www.sbml.org/sbml/level3/version1/comp/version1\"", "");
		modString = modString.replace("comp:required=\"false\"", "");
		Module mod = AC_GUI.xnewModule(name, sbmlID, modString, parent, glyph, true);
		// add visible variables and equivalence nodes
		ListOfGraphicalObjects subGlyphs = glyph.getListOfSubGlyphs();
		GeneralGlyph subGlyph;
		for (long i = 0; i < glyph.getNumSubGlyphs(); i++)
		{
			subGlyph = (GeneralGlyph)subGlyphs.get(i);
			if (subGlyph.getId().endsWith("_VisibleVariableGlyph"))
			{
				importVisibleVariable(mod, subGlyph);
			}
			else if (subGlyph.getId().endsWith("_EquivalenceNodeGlyph"))
			{
				importEquivalenceNode(mod, subGlyph);
			}
		}
		// add ports
		if(modelCompPlugin.getNumPorts() > 0)
		{
			importPorts(modelCompPlugin.getListOfPorts(), containerMod, glyph, mod);
		}
		// add submodules
		if(modelCompPlugin.getNumSubmodels() > 0)
		{
			CompSBMLDocumentPlugin docCompPlugin = (CompSBMLDocumentPlugin)doc.getPlugin("comp");
			SBMLNamespaces nameSpaces = new SBMLNamespaces();
			nameSpaces.addNamespaces(doc.getNamespaces());
			importAllSubmodules(modelCompPlugin.getListOfSubmodels(), docCompPlugin.getListOfModelDefinitions(), nameSpaces, glyph, mod);
		}
		// add connections
		if (containerMod.getNumSpecies() > 0)
		{
			importReplacementsFromSpecies(containerMod.getListOfSpecies(), mod, containerMod);
		}
		//AC_GUI.changeActiveModule(mod);
		return mod;
	}
	
	private static void importAllSubmodules(ListOfSubmodels submodules, ListOfModelDefinitions moduleDefinitions, SBMLNamespaces nameSpaces, GeneralGlyph parentGlyph, Module parent)
	{
		ListOfGraphicalObjects subGlyphs = parentGlyph.getListOfSubGlyphs();
		//System.out.println("Number of subglyphs: " + subGlyphs.size());
		
		String name = "";
		String sbmlID = "";
		String modelRef = "";
		String modString = "";
		Submodel submodule;
		SBMLNamespaces ns;
		SBMLDocument newDoc;
		ModelDefinition moduleDefinition;
		CompModelPlugin submodulePlugin;
		GeneralGlyph glyph = null;
		for(long i = 0; i < submodules.size(); i++)
		{
			submodule = (Submodel)submodules.get(i);
			name = submodule.getName();
			sbmlID = submodule.getId();
			modelRef = submodule.getModelRef();
			moduleDefinition = moduleDefinitions.get(modelRef);
			glyph = (GeneralGlyph)subGlyphs.get(sbmlID + "_glyph");
			
			/*
			System.out.println("glyphID: " + glyph.getId());
			System.out.println("sbmlID: " + sbmlID);
			System.out.println("modelRef: " + modelRef);
			System.out.println("module Ref name: " + moduleDefinition.getName());
			System.out.println("module Ref model name: " + moduleDefinition.getModel().getName());
			*/
			
			ns = new SBMLNamespaces(nameSpaces);
			
			newDoc = new SBMLDocument(ns);
			//newDoc.createModel();
			int setModelCode = newDoc.setModel(moduleDefinition);
			if(setModelCode != 0)
			{
				System.err.println("Error in SBMLParser.importAllSubmodules: setModelCode = " + setModelCode);
				System.exit(0);
			}
			newDoc.setPackageRequired("layout", false);
			newDoc.setPackageRequired("comp", false);
			removePluginData(newDoc.getModel());
			modString = libsbml.writeSBMLToString(newDoc);
			//System.out.println(modString);
			System.out.println("Validate submodule: " + moduleDefinition.getId());
			checkValidSBML(modString);
			System.out.println("Validation successful.");
			modString = modString.replace("xmlns:comp=\"http://www.sbml.org/sbml/level3/version1/comp/version1\"", "");
			modString = modString.replace("comp:required=\"false\"", "");
			Module mod = null;
			if (glyph.getAnnotation() != null)
			{
				// the submodule is a MathematicalAggregator
				XMLNode modAnnotation = glyph.getAnnotation().getChild(0);
				String inputNumber = modAnnotation.getAttrValue("inputs", "http://www.copasi.org/softwareprojects");
				String op = modAnnotation.getAttrValue("type", "http://www.copasi.org/softwareprojects");
				mod = AC_GUI.addMathAggregator(sbmlID, sbmlID, modString, parent, Integer.valueOf(inputNumber), op, glyph);
				//System.out.println("--------------------");
				//System.out.println();
				//System.out.println(modString);
				//System.out.println();
			}
			else
			{
				// the submodule is a Submodule
				mod = AC_GUI.xnewModule(sbmlID, sbmlID, modString, parent, glyph, false);
			}
			
			submodulePlugin = (CompModelPlugin)moduleDefinition.getPlugin("comp");
			// add visible variables and equivalence nodes
			ListOfGraphicalObjects submoduleSubGlyphs = glyph.getListOfSubGlyphs();
			GeneralGlyph subGlyph;
			for (long j = 0; j < glyph.getNumSubGlyphs(); j++)
			{
				subGlyph = (GeneralGlyph)submoduleSubGlyphs.get(j);
				if (subGlyph.getId().endsWith("_VisibleVariableGlyph"))
				{
					importVisibleVariable(mod, subGlyph);
				}
				else if (subGlyph.getId().endsWith("_EquivalenceNodeGlyph"))
				{
					importEquivalenceNode(mod, subGlyph);
				}
			}
			// add ports
			if(submodulePlugin.getNumPorts() > 0)
			{
				importPorts(submodulePlugin.getListOfPorts(), moduleDefinition, glyph, mod);
			}
			// add submodules
			if(submodulePlugin.getNumSubmodels() > 0)
			{
				importAllSubmodules(submodulePlugin.getListOfSubmodels(), moduleDefinitions, nameSpaces, glyph, mod);
			}
			// add connections
			if (moduleDefinition.getNumSpecies() > 0)
			{
				importReplacementsFromSpecies(moduleDefinition.getListOfSpecies(), mod, null);
			}
		}
	}
	
	private static void importPorts(ListOfPorts ports, Model parentModule, GeneralGlyph parentGlyph, Module mod)
	{
		String portIDRef;
		String portName;
		String portRefName;
		String portType;
		String varType;
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
			
			AC_GUI.xaddPort(mod, portName, portRefName, portType, varType, portGlyph);
		}
	}
	
	private static void importVisibleVariable(Module parent, GeneralGlyph glyph)
	{
		XMLNode annotation = glyph.getAnnotation().getChild(0);
		String refName = annotation.getAttrValue("name", "http://www.copasi.org/softwareprojects");
		String vType = annotation.getAttrValue("vType", "http://www.copasi.org/softwareprojects");
		
		AC_GUI.addVisibleVariable(parent, refName, vType, glyph);
	}
	
	private static void importEquivalenceNode(Module parent, GeneralGlyph glyph)
	{
		XMLNode annotation = glyph.getAnnotation().getChild(0);
		String refName = annotation.getAttrValue("name", "http://www.copasi.org/softwareprojects");
		String vType = annotation.getAttrValue("vType", "http://www.copasi.org/softwareprojects");
		
		AC_GUI.addEquivalenceNode(parent, refName, glyph);
	}
	
	private static void importReplacementsFromSpecies(ListOfSpecies speciesList, Module module, Model model)
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
				importConnections(specPlugin.getListOfReplacedElements(), spec, module, model);
			}
		}
	}
	
	private static void importConnections(ListOfReplacedElements replacedElementsList, Species spec, Module module, Model model)
	{
		Object node = findVariable(spec.getName(), module);
		Object variableDrawingCell;
		if (node instanceof VisibleVariable)
		{
			importVisibleVariableConnections((VisibleVariable)node, replacedElementsList, module, model);
		}
		else if (node instanceof EquivalenceNode)
		{
			importEquivalenceNodeConnections((EquivalenceNode)node, replacedElementsList, module, model);
		}
	}
	
	private static void importVisibleVariableConnections(VisibleVariable var, ListOfReplacedElements replacedElementsList, Module module, Model model)
	{
		Object variableDrawingCell = var.getDrawingCell();
		String portRef;
		String submoduleRef;
		Port currentPort;
		ReplacedElement element;
		boolean inputToVisibleVariable;
		Object portDrawingCell;
		for (long i = 0; i < replacedElementsList.size(); i++)
		{
			element = (ReplacedElement)replacedElementsList.get(i);
			portRef = element.getPortRef();
			submoduleRef = element.getSubmodelRef();
			currentPort = findPort(portRef, submoduleRef, module, model);
			if (currentPort == null)
			{
				System.err.println("Error SBMLParser.importSingleConnection: Port " + portRef + " not found.");
				System.exit(0);
			}
			
			switch(currentPort.getType())
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
				AC_GUI.addConnection(module, portDrawingCell, variableDrawingCell, "DashedConnectionEdge");
			}
			else
			{
				AC_GUI.addConnection(module, variableDrawingCell, portDrawingCell, "ConnectionEdge");
			}
		}
	}
	
	private static void importEquivalenceNodeConnections(EquivalenceNode eNode, ListOfReplacedElements replacedElementsList, Module module, Model model)
	{
		Object variableDrawingCell = eNode.getDrawingCell();
		String portRef;
		String submoduleRef;
		Port currentPort;
		ReplacedElement element;
		Object portDrawingCell;
		for (long i = 0; i < replacedElementsList.size(); i++)
		{
			element = (ReplacedElement)replacedElementsList.get(i);
			portRef = element.getPortRef();
			submoduleRef = element.getSubmodelRef();
			currentPort = findPort(portRef, submoduleRef, module, model);
			if (currentPort == null)
			{
				System.err.println("Error SBMLParser.importSingleConnection: Port " + portRef + " not found.");
				System.exit(0);
			}
			portDrawingCell = currentPort.getDrawingCell();
			AC_GUI.addConnection(module, variableDrawingCell, portDrawingCell, "ConnectionEdge");
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
	
	private static void exportLayoutInformation(Model model, Module mod)
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
		SBasePlugin basePlugin = (model.getPlugin ("layout"));
		LayoutModelPlugin layoutPlugin = (LayoutModelPlugin)basePlugin;
		Layout layout = layoutPlugin.createLayout ();
		layout.setId ("Layout_1");
		
		GeneralGlyph gly = layout.createGeneralGlyph();
		gly.setId(mod.getName() + "_glyph");
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
		if(mod.getEquivalenceNodes().size() > 0)
		{
			addEquivalenceNodeLayoutInformation(mod, gly);
		}
		
		// add submodules to the layout
		if(mod.getChildren().size() > 0)
		{
			addSubmoduleLayoutInformation(mod, gly);
		}
			
		//return libsbml.writeSBMLToString(doc);
	}
	
	private static void addPortLayoutInformation(Module mod, GeneralGlyph parentGly)
	{
		Port port;
		Point point;
		BoundingBox box;
		String portAnnotation;
		GeneralGlyph gly;
		ListIterator<Port> listOfPorts = mod.getPorts().listIterator();
		while(listOfPorts.hasNext())
		{
			port = listOfPorts.next();
			point = new Point();
			box = new BoundingBox();
			gly = new GeneralGlyph();
			
			point.setX(((mxCell)port.getDrawingCell()).getGeometry().getX());
			point.setY(((mxCell)port.getDrawingCell()).getGeometry().getY());
			box.setPosition(point);
			gly.setBoundingBox(box);
			gly.setId(port.getName() + "_PortGlyph");
			portAnnotation = "<ac:portInfo";
			portAnnotation += " xmlns:ac=\"http://www.copasi.org/softwareprojects\"";
			portAnnotation += " ac:refName=\"" + port.getRefName() + "\"";
			portAnnotation += " ac:name=\"" + port.getName() + "\"";
			portAnnotation += " ac:pType=\"" + port.getType().name() + "\"";
			portAnnotation += " ac:vType=\"" + port.getVariableType().toString() + "\"";
			portAnnotation += " ac:parentMod=\"" + port.getParent().getName() + "\"";
			portAnnotation += "/>";
			gly.appendAnnotation(XMLNode.convertStringToXMLNode(portAnnotation));
			
			parentGly.addSubGlyph(gly);
		}
	}
	
	private static void addVisibleVariableLayoutInformation(Module mod, GeneralGlyph parentGly)
	{
		VisibleVariable currentVar;
		Point point;
		BoundingBox box;
		GeneralGlyph gly;
		String varAnnotation;
		ListIterator<VisibleVariable> vars = mod.getVisibleVariables().listIterator();
		while(vars.hasNext())
		{
			currentVar = vars.next();
			point = new Point();
			box = new BoundingBox();
			gly = new GeneralGlyph();
			
			point.setX(((mxCell)currentVar.getDrawingCell()).getGeometry().getX());
			point.setY(((mxCell)currentVar.getDrawingCell()).getGeometry().getY());
			box.setPosition(point);
			gly.setBoundingBox(box);
			gly.setId(currentVar.getRefName() + "_VisibleVariableGlyph");
			
			varAnnotation = "<ac:VisibleVariableInfo";
			varAnnotation += " xmlns:ac=\"http://www.copasi.org/softwareprojects\"";
			varAnnotation += " ac:name=\"" + currentVar.getRefName() + "\"";
			varAnnotation += " ac:vType=\"" + currentVar.getVariableType().toString() + "\"";
			varAnnotation += "/>";
			gly.appendAnnotation(XMLNode.convertStringToXMLNode(varAnnotation));
			
			parentGly.addSubGlyph(gly);
		}
	}
	
	private static void addEquivalenceNodeLayoutInformation(Module mod, GeneralGlyph parentGly)
	{
		EquivalenceNode currenteNode;
		Point point;
		BoundingBox box;
		GeneralGlyph gly;
		String eNodeAnnotation;
		ListIterator<EquivalenceNode> eNodes = mod.getEquivalenceNodes().listIterator();
		while(eNodes.hasNext())
		{
			currenteNode = eNodes.next();
			point = new Point();
			box = new BoundingBox();
			gly = new GeneralGlyph();
			
			point.setX(((mxCell)currenteNode.getDrawingCell()).getGeometry().getX());
			point.setY(((mxCell)currenteNode.getDrawingCell()).getGeometry().getY());
			box.setPosition(point);
			gly.setBoundingBox(box);
			gly.setId(currenteNode.getRefName() + "_EquivalenceNodeGlyph");
			
			eNodeAnnotation = "<ac:EquivalenceNodeInfo";
			eNodeAnnotation += " xmlns:ac=\"http://www.copasi.org/softwareprojects\"";
			eNodeAnnotation += " ac:name=\"" + currenteNode.getRefName() + "\"";
			eNodeAnnotation += " ac:vType=\"" + "Species" + "\"";
			eNodeAnnotation += "/>";
			gly.appendAnnotation(XMLNode.convertStringToXMLNode(eNodeAnnotation));
			
			parentGly.addSubGlyph(gly);
		}
	}
	
	private static void addSubmoduleLayoutInformation(Module mod, GeneralGlyph parentGly)
	{
		Module child;
		Dimensions dim;
		Point point;
		BoundingBox box;
		String submoduleAnnotation;
		boolean isMathAgg;
		GeneralGlyph gly;
		ListIterator<Module> children = mod.getChildren().listIterator();
		System.out.println(mod.getName() + " has " + mod.getChildren().size() + " children.");
		while(children.hasNext())
		{
			child = children.next();
			dim = new Dimensions();
			point = new Point();
			box = new BoundingBox();
			gly = new GeneralGlyph();
			
			if (child instanceof MathematicalAggregator)
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
			gly.setId(child.getName() + "_glyph");
			
			if (isMathAgg)
			{
				submoduleAnnotation = "<ac:MathAggInfo";
				submoduleAnnotation += " xmlns:ac=\"http://www.copasi.org/softwareprojects\"";
				submoduleAnnotation += " ac:type=\"" + ((MathematicalAggregator)child).getOperation().toString() + "\"";
				submoduleAnnotation += " ac:inputs=\"" + ((MathematicalAggregator)child).getNumberofInputs() + "\"";
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
			if(child.getEquivalenceNodes().size() > 0)
			{
				addEquivalenceNodeLayoutInformation(child, gly);
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
	
	private static Module findModule(String moduleName, Module module, Model model)
	{
		if (model != null)
		{
			if (!module.getName().equalsIgnoreCase(model.getName()))
			{
				// a module name change was required, so moduleName must be compared to the old name
				if (moduleName.equalsIgnoreCase(model.getName()))
				{
					return module;
				}
			}
		}
		
		if (moduleName.equalsIgnoreCase(module.getName()))
		{
			return module;
		}
		
		ListIterator<Module> children = module.getChildren().listIterator();
		Module child;
		
		while(children.hasNext())
		{
			child = children.next();
			if (moduleName.equalsIgnoreCase(child.getName()))
			{
				return child;
			}
		}
		return null;
	}
	
	private static Port findPort(String portName, String parentModuleName, Module module, Model model)
	{
		Module portParentMod = findModule(parentModuleName, module, model);
		if (portParentMod == null)
		{
			System.err.println("Error SBMLParser.findPort: Module " + parentModuleName + " not found.");
			System.exit(0);
		}
		ListIterator<Port> portList = portParentMod.getPorts().listIterator();
		Port port;
		while(portList.hasNext())
		{
			port = portList.next();
			if (portName.equalsIgnoreCase(port.getName()))
			{
				return port;
			}
		}
		return null;
	}
	
	private static void removePluginData(Model model)
	{
		
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
	
	private static void checkValidSBML(String sbml)
	{
		SBMLDocument sdoc = libsbml.readSBMLFromString(sbml);
		System.out.println("Document Level: " + sdoc.getLevel());
		System.out.println("Document Version: " + sdoc.getVersion());
		long numErrors = sdoc.getNumErrors();

		if (numErrors > 0) {
			System.err.println("Encountered errors while reading the file. ");
			System.err.println("Please correct the following errors and try again.");
			sdoc.printErrors();
			System.exit(2);
		}
	
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
	}
}
