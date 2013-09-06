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
	
	public void exportSBML(String modelCopasi, Module rootModule, String fName)
	{
		try {
			out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fName), "UTF-8")); 
			//out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		String output;
		
		//print header
		output = printModelCompHeader();
		
		//output = addSpace(2) + "<model id=\"" + rootModule.getName() + "\">";
		System.out.println("getContainerDefinition() start.");
		String containerOutput = getContainerDefinition(rootModule);
		System.out.println("getContainerDefinition() end.");
		int endofModelDefIndex = containerOutput.indexOf("</model>");
		String part1 = containerOutput.substring(0, endofModelDefIndex);
		String part2 = containerOutput.substring(endofModelDefIndex);
		
		output += part1;
		//out.print(output);
		if (!rootModule.getChildren().isEmpty())
		{
			System.out.println("getSubmodelInformation() start.");
			output += getSubmodelInformation(rootModule) + eol;
			System.out.println("getSubmodelInformation() end.");
		}
		/*
		if (!rootModule.getPorts().isEmpty())
		{
			printPortInformation(rootModule);
		}
		*/
		output += addSpace(2) + part2;
		//out.println(output);
		
		System.out.println("addReplacements() start.");
		output = addReplacements(output, rootModule);
		System.out.println("addReplacements() end.");
		
		System.out.println("getModelDefinitions() start.");
		output += getModelDefinitions(rootModule);
		System.out.println("getModelDefinitions() end.");
		
		output += "</sbml>" + eol;
		
		System.out.println("removeCOPASIMetaID start.");
		output = removeCOPASIMetaID(output);
		System.out.println("removeCOPASIMetaID end.");
		
		System.out.println("fixReactionModifiers start.");
		output = fixReactionModifiers(rootModule, output);
		System.out.println("fixReactionModifiers end.");
		
		System.out.println("addLayoutInformation start.");
		output = addLayoutInformation(rootModule, output);
		System.out.println("addLayoutInformation end.");
		
		out.println(output);
		out.close();
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
			mod =  ximportContainerModule(sdoc, parent);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return mod;
	}
	
	/**
	 * Return the model definitions.
	 * @param mod the model to output
	 */
	private String getModelDefinitions(Module mod)
	{
		ListIterator<Module> children = mod.getChildren().listIterator();
		Module child;
		CCopasiDataModel dataModel;
		boolean hasPorts = false;
		String annotationHeader = "<COPASI xmlns=\"http://www.copasi.org/static/sbml\">\n";
		annotationHeader +=  "<params>\n";
		String annotationFooter = "</params>\n" + "</COPASI>\n";
		String output = "";
		//out.println(addSpace(2) + "<comp:listOfModelDefinitions>");
		System.out.println("children loop start.");
		output += addSpace(2) + "<comp:listOfModelDefinitions>" + eol;
		while(children.hasNext())
		{
			child = children.next();
			System.out.println("child " + child.getName() + " start.");
			System.out.println("getCopasiModelFromKey() start.");
			dataModel = AC_GUI.copasiUtility.getCopasiModelFromKey(child.getKey());
			if (dataModel == null)
			{
				System.err.println(child.getName() + "'s dataModel is empty");
			}
			System.out.println("getCopasiModelFromKey() end.");
			//System.out.println(child.getName() + " key = " + child.getKey());
			//System.out.println(child.getName() + " dataModel key = " + dataModel.getModel().getKey());
			//System.out.println(child.getName() + " dataModel name = " + dataModel.getObjectName());
			hasPorts = (child.getPorts().size() != 0);
			System.out.println("hasPorts = " + hasPorts);
			/*
			if (!child.getPorts().isEmpty())
			{
				String portDef = annotationHeader;
				portDef += getPortDefinitions(child);
				portDef += annotationFooter;
				//System.out.println(portDef);
				//if (!dataModel.getModel().addUnsupportedAnnotation("http://myannotation.org", "<test xmlns=\"http://myannotation.org\" value='blaaaahaaa'/>"))
				if (!dataModel.getModel().addUnsupportedAnnotation("http://www.copasi.org/static/sbml", portDef))
				{
					System.err.println("couldn't set port annotation: ");
					System.err.println(CCopasiMessage.getAllMessageText());
				}
			}
			*/
			try
			{
				//System.out.println(dataModel.exportSBMLToString());
				//System.out.println();
				//System.out.println(dataModel.exportSBMLToString(3, 1));
				//modelSBML = dataModel.exportSBMLToString(3, 1);
				System.out.println("SBMLparser.getModelDefinitions() " + child.getName() + "'s copasi key = " + child.getKey());
				System.out.println("Datamodel name = " + dataModel.getModel().getObjectName());
				System.out.println("exportSBMLToString() start.");
				modelSBML = dataModel.exportSBMLToString(3, 1);
				System.out.println("exportSBMLToString() end.");
				System.out.println("substring and replace start.");
				modelSBML = modelSBML.substring(modelSBML.indexOf("<model"));
				modelSBML = modelSBML.replaceAll("<model", "<comp:modelDefinition");
				modelSBML = modelSBML.replaceAll("</model>", "</comp:modelDefinition>");
				modelSBML = modelSBML.replaceAll("\n</sbml>", "");
				modelSBML = modelSBML.replaceAll("</sbml>", "");
				modelSBML = addSpace(2) + modelSBML;
				System.out.println("substring and replace end.");
				
				// replace the old id with the correct id
				System.out.println("replace id start.");
				int startIDIndex = modelSBML.indexOf(" id=");
				int endIDIndex = modelSBML.indexOf("name=");
				String oldID = modelSBML.substring(startIDIndex+1, endIDIndex);
				//System.out.println("OldID: " + oldID);
				String newID = "id=\"" + child.getKey() + "\" ";
				modelSBML = modelSBML.replaceAll(oldID, newID);
				System.out.println("replace id end.");
				
				// add the port definitions to the submodule
				//int endListofReactionsIndex = modelSBML.indexOf("</listOfReactions>");
				int endofModelDefIndex = modelSBML.indexOf("</comp:modelDefinition>");
				if (hasPorts)
				{
					String part1 = modelSBML.substring(0, endofModelDefIndex);
					String part2 = modelSBML.substring(endofModelDefIndex);
					System.out.println("setPortIDRefs start.");
					setPortIDRefs(part1, child);
					System.out.println("setPortIDRefs end.");
					System.out.println("getPortDefinitions() start.");
					String portDefinitions = getPortDefinitions(child);
					System.out.println("getPortDefinitions() end.");
					
					modelSBML = part1 + portDefinitions + addSpace(2) + part2;
				}
				
				//System.out.println(modelSBML.substring(endListofReactionsIndex));
				
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			//out.println(modelSBML);
			output += modelSBML;
			System.out.println("child " + child.getName() + " end.");
		}
		//out.println(addSpace(2) + "</comp:listOfModelDefinitions>");
		System.out.println("children loop end.");
		output += addSpace(2) + "</comp:listOfModelDefinitions>" + eol;
		return output;
	}
	

	private String getContainerDefinition(Module mod)
	{
		String modSBML = "";
		CCopasiDataModel dataModel;
		System.out.println("getCopasiModelFromKey() start.");
		dataModel = AC_GUI.copasiUtility.getCopasiModelFromKey(mod.getKey());
		if (dataModel == null)
		{
			System.err.println(mod.getName() + "'s dataModel is empty");
		}
		System.out.println("getCopasiModelFromKey() end.");
		//AC_GUI.copasiUtility.setLayout(mod);
		try
		{
			System.out.println("parser.getContDef() " + mod.getName() + "'s copasi key = " + mod.getKey());
			System.out.println("Datamodel name = " + dataModel.getModel().getObjectName());
			System.out.println("exportSBMLToString() start.");
			modSBML = dataModel.exportSBMLToString(3, 1);
			System.out.println("exportSBMLToString() end.");
			modSBML = modSBML.substring(modSBML.indexOf("<model"));
			modSBML = modSBML.replaceAll("\n</sbml>", "");
			modSBML = modSBML.replaceAll("</sbml>", "");
			modSBML = addSpace(2) + modSBML;
			
			// replace the old id with the correct id
			int startIDIndex = modSBML.indexOf(" id=");
			int endIDIndex = modSBML.indexOf("name=");
			String oldID = modSBML.substring(startIDIndex+1, endIDIndex);
			//System.out.println("OldID: " + oldID);
			String newID = "id=\"" + mod.getName() + "\" ";
			modSBML = modSBML.replaceAll(oldID, newID);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return modSBML;
	}
	/**
	 * Gather the port definitions of the given module.
	 * @param mod the module containing the ports
	 * @return the port definitions of the given module
	 */
	private String getPortDefinitions(Module mod)
	{		
		ListIterator<Port> ports = mod.getPorts().listIterator();
		Port port;
		
		//<port id="p_RL1_1" idRef="RL1_1" name="RL1" type="O"/>
		String portInfo;
		
		String idRef;
		String id;
		
		portInfo = addSpace(2) + "<comp:listOfPorts>" + eol;
		
		while(ports.hasNext())
		{
			port = ports.next();
			
			idRef = port.getVariableSBMLid();
			id = port.getName();
			
			portInfo += addSpace(6) + "<comp:port ";
			portInfo += "comp:idRef=\"" + idRef + "\" ";
			portInfo += "comp:id=\"" + id + "\" ";
			portInfo += "/>" + eol;
		}
		
		portInfo += addSpace(4) + "</comp:listOfPorts>" + eol;
		return portInfo;
	}
	
	/**
	 * Write the submodel information of the given module.
	 * @param mod the module containing the submodels
	 */
	private String getSubmodelInformation(Module mod)
	{
		ListIterator<Module> children = mod.getChildren().listIterator();
		Module child;
		
		String output;
		String space = addSpace(2);
		String id = "";
		String modelRef = "";
		
		//out.println(space + "<comp:listOfSubmodels>");
		output = space + "<comp:listOfSubmodels>" + eol;
		space = addSpace(6);
		while(children.hasNext())
		{
			//<comp:submodel id="subMod1" modelRef="Module_1_1_1"/>
			child = children.next();
			id = child.getName();
			modelRef = child.getKey();
			
			//out.println(space + "<comp:submodel comp:id=\"" + id + "\" comp:modelRef=\"" + modelRef + "\"/>");
			output += space + "<comp:submodel comp:id=\"" + id + "\" comp:modelRef=\"" + modelRef + "\"/>" + eol;
		}
		space = addSpace(4);
		//out.println(space + "</comp:listOfSubmodels>");
		output += space + "</comp:listOfSubmodels>";
		return output;
	}
	
	private String addReplacements(String sbml, Module mod)
	{
		String output = "";
		int listofCompartmentsIndex = sbml.indexOf("<listOfCompartments>");
		int startCompartmentIndex = sbml.indexOf("<compartment", listofCompartmentsIndex);
		int endCompartmentIndex = sbml.indexOf(">", startCompartmentIndex);
		String oldCompartment = sbml.substring(startCompartmentIndex, endCompartmentIndex+1);
		String newCompartment = oldCompartment.substring(0, oldCompartment.length()-2) + ">" + eol;
		//System.out.println(oldCompartment);
		
		ListIterator<Module> children = mod.getChildren().listIterator();
		Module child;
		String submodelRef;
		String idRef;
		newCompartment += addSpace(8) + "<comp:listOfReplacedElements>" + eol;
		while(children.hasNext())
		{
			// <comp:replacedElement comp:idRef="comp" comp:submodelRef="A"/>
			child = children.next();
			submodelRef = child.getName();
			idRef = "compartment_1";
			
			newCompartment += addSpace(10);
			newCompartment += "<comp:replacedElement ";
			newCompartment += "comp:idRef=\"" + idRef + "\" ";
			newCompartment += "comp:submodelRef=\"" + submodelRef + "\"/>" + eol;
		}
		newCompartment += addSpace(8) + "</comp:listOfReplacedElements>" + eol;
		newCompartment += addSpace(6) + "</compartment>";
		output = sbml.replaceAll(oldCompartment, newCompartment);
		if (!AC_GUI.modelBuilder.isSpeciesListEmpty())
		{
			output = addReplacementSpecies(output, mod);
		}
		return output;
	}
	
	private String addReplacementSpecies(String sbml, Module mod)
	{
		String output = sbml;
		int startlistofSpeciesIndex = output.indexOf("<listOfSpecies>");
		int endlistofSpeciesIndex = output.indexOf("</listOfSpecies>", startlistofSpeciesIndex+5);
		String oldSpeciesList = output.substring(startlistofSpeciesIndex, endlistofSpeciesIndex);
		String newSpeciesList = output.substring(startlistofSpeciesIndex, endlistofSpeciesIndex);
		int startSpeciesIndex;
		int endSpeciesIndex;
		String oldSpecies;
		String newSpecies;
		String speciesName;
		
		//System.out.println("oldSpeciesList: ");
		//System.out.println(oldSpeciesList);
		//System.out.println();
		startSpeciesIndex = newSpeciesList.indexOf("<species");
		while (startSpeciesIndex != -1)
		{
			System.out.println("addReplacementSpecies while loop. Mod = : " + mod.getName());
			System.out.println("startSpeciesIndex = " + startSpeciesIndex);
			endSpeciesIndex = newSpeciesList.indexOf(">", startSpeciesIndex);
			oldSpecies = newSpeciesList.substring(startSpeciesIndex, endSpeciesIndex+1);
			speciesName = oldSpecies.substring(oldSpecies.indexOf("name=\"") + 6, oldSpecies.indexOf("\" compartment"));
			//System.out.println(speciesName);
			
			Object node = findVariable(speciesName, mod);
			if (node != null)
			{
				newSpecies = oldSpecies.replace("/", "") + eol; 
				if (node instanceof VisibleVariable)
				{
					newSpecies += getVisibleVariableReplacements((VisibleVariable)node);
					newSpecies += addSpace(6) + "</species>";
					newSpeciesList.replace(oldSpecies, newSpecies);
				}
				else if (node instanceof EquivalenceNode)
				{
					newSpecies += getEquivalenceNodeReplacements((EquivalenceNode)node);
					newSpecies += addSpace(6) + "</species>";
					newSpeciesList.replace(oldSpecies, newSpecies);					
				}
				newSpeciesList = newSpeciesList.replace(oldSpecies, newSpecies);
			}
			startSpeciesIndex = newSpeciesList.indexOf("<species", endSpeciesIndex);
		}
		output = output.replace(oldSpeciesList, newSpeciesList);
		return output;
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
	private String getVisibleVariableReplacements(VisibleVariable var)
	{
		Object outgoingEdges [] = mxGraphModel.getOutgoingEdges(AC_GUI.drawingBoard.getGraph().getModel(), var.getDrawingCell());
		Object incomingEdges [] = mxGraphModel.getIncomingEdges(AC_GUI.drawingBoard.getGraph().getModel(), var.getDrawingCell());
		String portRef;
		String submodelRef;
		String output = "";
		
		if ((outgoingEdges.length == 0) && (incomingEdges.length == 0))
		{
			return output;
		}
		
		output += addSpace(8) + "<comp:listOfReplacedElements>" + eol;
		
		if (outgoingEdges.length != 0)
		{			
			for (int i = 0; i < outgoingEdges.length; i++)
			{
				// <comp:replacedElement comp:portRef="S" comp:submodelRef="A"/>
				portRef = ((Port)((mxCell)outgoingEdges[i]).getTarget().getValue()).getName();
				submodelRef = ((Port)((mxCell)outgoingEdges[i]).getTarget().getValue()).getParent().getName();
				output += addSpace(10) + "<comp:replacedElement ";
				output += "comp:portRef=\"" + portRef + "\" ";
				output += "comp:submodelRef=\"" + submodelRef + "\"/>" + eol;
			}
		}
		
		if (incomingEdges.length != 0)
		{
			// <comp:replacedBy comp:portRef="D_port" comp:submodelRef="B"/>
			portRef = ((Port)((mxCell)incomingEdges[0]).getSource().getValue()).getName();
			submodelRef = ((Port)((mxCell)incomingEdges[0]).getSource().getValue()).getParent().getName();
			output += addSpace(10) + "<comp:replacedElement ";
			output += "comp:portRef=\"" + portRef + "\" ";
			output += "comp:submodelRef=\"" + submodelRef + "\"/>" + eol;
		}
		
		output += addSpace(8) + "</comp:listOfReplacedElements>" + eol;
			
		return output;
	}
	
	private String getEquivalenceNodeReplacements(EquivalenceNode eNode)
	{
		Object outgoingEdges [] = mxGraphModel.getOutgoingEdges(AC_GUI.drawingBoard.getGraph().getModel(), eNode.getDrawingCell());
		String portRef;
		String submodelRef;
		
		String output = addSpace(8) + "<comp:listOfReplacedElements>" + eol;
		
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
		
		return output;
	}
	
	/**
	 * Write the port information of the given module.
	 * @param mod the module containing the ports
	 */
	private void printPortInformation(Module mod)
	{
		//<comp:port id="Cdc20a_1" name="Cdc20a" type="I" idRef="p_Cdc20a_1" submodelRef="subMod4" />
		
		ListIterator<Port> ports = mod.getPorts().listIterator();
		Port port;
		
		String portName = "";
		String portType;
		String portParent = "";
		
		out.println("  <comp:listOfPorts>");
		while(ports.hasNext())
		{
			port = ports.next();
			
			portName = port.getName();
			portParent = port.getParent().getName();
			
			switch(port.getType())
			{
			case INPUT:
				portType = "I";
				break;
			case OUTPUT:
				portType = "O";
				break;
			case EQUIVALENCE:
				portType = "E";
				break;
			default:
				portType = "";
			}
			
			out.println("    <comp:port id=\"" + portName + "\" name=\"" + portName + "\" type=\"" + portType + "\" idRef=\"" + portName + "\" submodelRef=\"" + portParent + "\" />");
		}
		out.println("  </comp:listOfPorts>");
		
		/*
		String portInfo = "";
		
		ListIterator<Port> ports = mod.getPorts().listIterator();
		Port port;
		
		String portName = "";
		PortType portType;
		String portParent = "";
		
		portInfo = "<COPASI xmlns=\"http://www.copasi.org/static/sbml\">\n";
		portInfo += "<params>\n";
		portInfo += "<listOfPorts>\n";
		while(ports.hasNext())
		{
			port = ports.next();
			portName = port.getName();
			portType = port.getType();
			portParent = port.getParent().getName();
			
			portInfo += "<port id=\"" + portName + "\" name=\"" + portName + "\" type=\"" + portType + "\" idRef=\"" + portName + "\" submodelRef=\"" + portParent + "\" />\n";
		}
		portInfo += "</listOfPorts>\n";
		portInfo += "</params>\n";
		portInfo += "</COPASI>\n";
		//System.out.println(portInfo);
		//model.addUnsupportedAnnotation("http://myannotation.org", portInfo);
		if (!model.addUnsupportedAnnotation("http://www.copasi.org/static/sbml", portInfo))
		{
			System.err.println("couldn't set annotation: ");
			System.err.println(CCopasiMessage.getAllMessageText());
		}
		*/
	}
	
	private static String addSpace(int length)
	{
		String space = "";
		
		for(int i = 0; i < length; i++)
		{
			space += " ";
		}
		
		return space;
	}
	
	private String printModelCompHeader()
	{
		String header;
		String space = addSpace(6);
		header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + eol;
		header += "<sbml xmlns=\"http://www.sbml.org/sbml/level3/version1/core\" level=\"3\" version=\"1\" " + eol;
		header += space + "xmlns:comp=\"http://www.sbml.org/sbml/level3/version1/comp/version1\" comp:required=\"true\"" + eol;
		header += space + "xmlns:layout=\"http://www.sbml.org/sbml/level3/version1/layout/version1\" layout:required=\"false\"" + eol;
		header += space + "xmlns:html=\"http://www.w3.org/1999/xhtml\"" + eol;
		header += space + "xmlns:jigcell=\"http://www.sbml.org/2001/ns/jigcell\"" + eol;
		header += space + "xmlns:math=\"http://www.w3.org/1998/Math/MathML\">\n" + eol;
		
		return header;
	}
	
	private String removeCOPASIMetaID(String sbml)
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
	
	private void setPortIDRefs(String sbml, Module mod)
	{
		ListIterator<Port> ports = mod.getPorts().listIterator();
		Port port;
		int startlistofSpeciesIndex = sbml.indexOf("<listOfSpecies>");
		int endlistofSpeciesIndex = sbml.indexOf("</listOfSpecies>", startlistofSpeciesIndex+5);
		String speciesList = null;
		if ((startlistofSpeciesIndex != -1) && (endlistofSpeciesIndex != -1))
		{
			speciesList = sbml.substring(startlistofSpeciesIndex, endlistofSpeciesIndex);
		}
		
		int startlistofGlobalQIndex = sbml.indexOf("<listOfParameters>");
		int endlistofGlobalQIndex = sbml.indexOf("</listOfParameters>", startlistofGlobalQIndex);
		String globalQList = null;
		if ((startlistofGlobalQIndex != -1) && (endlistofGlobalQIndex != -1))
		{
			globalQList = sbml.substring(startlistofGlobalQIndex, endlistofGlobalQIndex);
		}
		
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
				sbmlID = getSpeciesSBMLid(speciesList, name);
				break;
			case GLOBAL_QUANTITY:
				sbmlID = getGlobalQSBMLid(globalQList, name);
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
	
	private String getSpeciesSBMLid(String speciesList, String name)
	{
		String id = null;
		int nameIndex;
		int startIndex;
		int startIDIndex;
		int endIDIndex;
		
		nameIndex = speciesList.indexOf("name=\""+ name + "\"");
		if (nameIndex == -1)
		{
			System.err.println("SBMLParser.getSpeciesSBMLid: species name \"" + name + "\" not found.");
		}
		
		startIndex = nameIndex - 50;
		if (startIndex < 0)
		{
			startIndex = 0;
		}
		startIDIndex = speciesList.indexOf(" id=\"", startIndex);
		startIDIndex = startIDIndex + 5;
		endIDIndex = speciesList.indexOf("\"", startIDIndex);
		
		id = speciesList.substring(startIDIndex, endIDIndex);
		return id;
	}
	
	private String getGlobalQSBMLid(String globalQList, String name)
	{
		String id = null;
		int nameIndex;
		int startIndex;
		int startIDIndex;
		int endIDIndex;
		
		nameIndex = globalQList.indexOf("name=\""+ name + "\"");
		if (nameIndex == -1)
		{
			System.err.println("SBMLParser.getGlobalQSBMLid: species name \"" + name + "\" not found.");
		}
		
		startIndex = nameIndex - 50;
		if (startIndex < 0)
		{
			startIndex = 0;
		}
		startIDIndex = globalQList.indexOf(" id=\"", startIndex);
		startIDIndex = startIDIndex + 5;
		endIDIndex = globalQList.indexOf("\"", startIDIndex);
		
		id = globalQList.substring(startIDIndex, endIDIndex);
		return id;
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
	
	private static void importContainerModule(Document doc, SBMLDocument sdoc)
	{
		String name = "";
		String sbmlID = "";
		String space = addSpace(6);
		String modString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + eol;
		modString += "<sbml xmlns=\"http://www.sbml.org/sbml/level3/version1/core\" level=\"3\" version=\"1\" " + eol;
		modString += space + "xmlns:comp=\"http://www.sbml.org/sbml/level3/version1/comp/version1\">" + eol;
		modString += space + "xmlns:html=\"http://www.w3.org/1999/xhtml\"" + eol;
		modString += space + "xmlns:jigcell=\"http://www.sbml.org/2001/ns/jigcell\"" + eol;
		modString += space + "xmlns:math=\"http://www.w3.org/1998/Math/MathML\">\n" + eol;
		//System.out.println ("Root element of the doc is " + doc.getDocumentElement().getNodeName());
		NodeList nodeLst = doc.getElementsByTagName("model");
		assert nodeLst.getLength() == 1;
		modString += "<model ";
		Node modNode = nodeLst.item(0);		   
		NamedNodeMap nnMap = modNode.getAttributes();
		Node iNode;
		for(int i = 0; i < nnMap.getLength();i++)
		{
			iNode = nnMap.item(i);
			modString += " " + iNode.getNodeName() + "=\"" + iNode.getNodeValue() + "\"";
			if (iNode.getNodeName().equals("name"))
			{
				name = iNode.getNodeValue();
			}
			else if (iNode.getNodeName().equals("id"))
			{
				sbmlID = iNode.getNodeValue();
			}
		}
		modString += "\">\"";
		
		nodeLst = modNode.getChildNodes();
		Node child = null;
		for(int i = 0; i < nodeLst.getLength(); i++)
		{
			child = nodeLst.item(i);
			if(child.getNodeName().equals("comp:listOfSubmodels"))
			{
				continue;
			}
			
			modString += elementToString(child);
		}
		modString += eol + "</model>" + eol + "</sbml>";
		
		LayoutModelPlugin mplugin = (LayoutModelPlugin)sdoc.getModel().getPlugin("layout");
		Module mod = AC_GUI.currentGUI.newModule(name, sbmlID, modString);
		importAllSubmodules(doc, sdoc, mod);
		//System.out.println(modString);
	}
	
	private static Module ximportContainerModule(SBMLDocument doc, Module parent)
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
		GeneralGlyph glyph = layout.getGeneralGlyph(sbmlID + "_glyph");
		
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
		Module mod = AC_GUI.xnewModule(name, sbmlID, modString, parent, glyph);
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
			ximportAllSubmodules(modelCompPlugin.getListOfSubmodels(), docCompPlugin.getListOfModelDefinitions(), nameSpaces, glyph, mod);
		}
		// add connections
		if (containerMod.getNumSpecies() > 0)
		{
			importReplacementsFromSpecies(containerMod.getListOfSpecies(), mod);
		}
		//AC_GUI.changeActiveModule(mod);
		return mod;
	}
	
	private static void importAllSubmodules(Document doc, SBMLDocument sdoc, Module parent)
	{
		String name = "";
		String sbmlID = "";
		String space = addSpace(6);
		String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + eol;
		prefix += "<sbml xmlns=\"http://www.sbml.org/sbml/level3/version1/core\" level=\"3\" version=\"1\" comp:required=\"false\"" + eol;
		prefix += space + "xmlns:comp=\"http://www.sbml.org/sbml/level3/version1/comp/version1\">" + eol;
		prefix += space + "xmlns:html=\"http://www.w3.org/1999/xhtml\"" + eol;
		prefix += space + "xmlns:jigcell=\"http://www.sbml.org/2001/ns/jigcell\"" + eol;
		prefix += space + "xmlns:math=\"http://www.w3.org/1998/Math/MathML\">\n" + eol;
		String modString = prefix;
		NodeList nodeLst1 = doc.getElementsByTagName("comp:modelDefinition");
		NodeList nodeLst2;
		//System.out.println("All the models in the file:");
		Node submoduleNode;
		for (int i = 0; i < nodeLst1.getLength(); i++) 
		{
			submoduleNode = nodeLst1.item(i);
			if (submoduleNode == null)
			{
				continue;
			}
			modString += "<model ";	   
			NamedNodeMap nnMap = submoduleNode.getAttributes();
			Node iNode;
			for(int j = 0; j < nnMap.getLength(); j++)
			{
				iNode = nnMap.item(j);
				modString += " " + iNode.getNodeName() + "=\"" + iNode.getNodeValue() + "\"";
				if (iNode.getNodeName().equals("name"))
				{
					name = iNode.getNodeValue();
				}
				else if (iNode.getNodeName().equals("id"))
				{
					sbmlID = iNode.getNodeValue();
				}
			}
			modString += ">";
			
			nodeLst2 = submoduleNode.getChildNodes();
			Node child = null;
			for(int j = 0; j < nodeLst2.getLength(); j++)
			{
				child = nodeLst2.item(j);
				if(child.getNodeName().equals("comp:listOfSubmodels"))
				{
					continue;
				}
				if(child.getNodeName().equals("comp:listOfPorts"))
				{
					continue;
				}
				if(child.getNodeName().equals("layoutlistOfLayouts"))
				{
					continue;
				}
				modString += elementToString(child);
			}
			modString += eol + "</model>" + eol + "</sbml>";
			//System.out.println(modString);

			//AC_GUI.currentGUI.newSubmodule(name, sbmlID, modString, parent);
			modString = prefix;
		}
	}
	
	private static void ximportAllSubmodules(ListOfSubmodels submodules, ListOfModelDefinitions moduleDefinitions, SBMLNamespaces nameSpaces, GeneralGlyph parentGlyph, Module parent)
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
				mod = AC_GUI.xnewModule(sbmlID, sbmlID, modString, parent, glyph);
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
				ximportAllSubmodules(submodulePlugin.getListOfSubmodels(), moduleDefinitions, nameSpaces, glyph, mod);
			}
			// add connections
			if (moduleDefinition.getNumSpecies() > 0)
			{
				importReplacementsFromSpecies(moduleDefinition.getListOfSpecies(), mod);
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
	
	private static void importReplacementsFromSpecies(ListOfSpecies speciesList, Module mod)
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
				importConnections(specPlugin.getListOfReplacedElements(), spec, mod);
			}
		}
	}
	
	private static void importConnections(ListOfReplacedElements replacedElementsList, Species spec, Module mod)
	{
		Object node = findVariable(spec.getName(), mod);
		Object variableDrawingCell;
		if (node instanceof VisibleVariable)
		{
			importVisibleVariableConnections((VisibleVariable)node, replacedElementsList, mod);
		}
		else if (node instanceof EquivalenceNode)
		{
			importEquivalenceNodeConnections((EquivalenceNode)node, replacedElementsList, mod);
		}
	}
	
	private static void importVisibleVariableConnections(VisibleVariable var, ListOfReplacedElements replacedElementsList, Module mod)
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
			currentPort = findPort(portRef, submoduleRef, mod);
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
				AC_GUI.addConnection(mod, portDrawingCell, variableDrawingCell, "DashedConnectionEdge");
			}
			else
			{
				AC_GUI.addConnection(mod, variableDrawingCell, portDrawingCell, "ConnectionEdge");
			}
		}
	}
	
	private static void importEquivalenceNodeConnections(EquivalenceNode eNode, ListOfReplacedElements replacedElementsList, Module mod)
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
			currentPort = findPort(portRef, submoduleRef, mod);
			if (currentPort == null)
			{
				System.err.println("Error SBMLParser.importSingleConnection: Port " + portRef + " not found.");
				System.exit(0);
			}
			portDrawingCell = currentPort.getDrawingCell();
			AC_GUI.addConnection(mod, variableDrawingCell, portDrawingCell, "ConnectionEdge");
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
	
	private static String addLayoutInformation(Module mod, String sbml)
	{
		SBMLDocument doc = libsbml.readSBMLFromString(sbml);
		long numErrors = doc.getNumErrors();

		if (numErrors > 0) {
			System.err.println("Encountered errors while reading the file. ");
			System.err.println("Please correct the following errors and try again.");
			doc.printErrors();
			System.exit(2);
		}
		
		Model model = doc.getModel();
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
			
		return libsbml.writeSBMLToString(doc);
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
		}
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
	
	private static Module findModule(String moduleName, Module mod)
	{
		if (moduleName.equalsIgnoreCase(mod.getName()))
		{
			return mod;
		}
		
		ListIterator<Module> children = mod.getChildren().listIterator();
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
	
	private static Port findPort(String portName, String parentModuleName, Module mod)
	{
		Module portParentMod = findModule(parentModuleName, mod);
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
}
