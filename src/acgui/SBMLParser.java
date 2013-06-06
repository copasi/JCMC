package acgui;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ListIterator;

import org.COPASI.*;

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
	private String eol;
	
	/**
	 * Construct the object.
	 */
	public SBMLParser()
	{
		//System.out.println(CCopasiRootContainer.getDatamodelList().size());
		out = null;
		modelSBML = "";
		fileName = "";
		eol = System.getProperty("line.separator");
	}
	
	public void saveSBML(String modelCopasi, Module rootModule, String fName)
	{
		try {
			out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fName), "UTF-8")); 
			//out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		String output;
		
		//print header
		printModelCompHeader();
		
		//output = addSpace(2) + "<model id=\"" + rootModule.getName() + "\">";
		output = getContainerDefinition(rootModule);
		int endofModelDefIndex = output.indexOf("</model>");
		String part1 = output.substring(0, endofModelDefIndex);
		String part2 = output.substring(endofModelDefIndex);
		
		output = part1;
		//out.print(output);
		if (!rootModule.getChildren().isEmpty())
		{
			output += getSubmodelInformation(rootModule) + eol;
		}
		/*
		if (!rootModule.getPorts().isEmpty())
		{
			printPortInformation(rootModule);
		}
		*/
		output += addSpace(2) + part2;
		//out.println(output);
		
		output = addReplacements(output, rootModule);
		
		output += getModelDefinitions(rootModule);
		
		output += "</sbml>" + eol;
		
		output = removeCOPASIMetaID(output);
		out.println(output);
		out.close();
	}
	
	
	/**
	 * Print the model in SBML format.
	 * @param mod the model to output
	 * @return the name of the file
	 */
	public String print(Module mod)
	{
		/*
		out.println("<model id=\"" + mod.getName() + "\" name=\"" + mod.getName() + "\">");
		printSubmodelInformation(mod);
		printPortInformation(mod);
		out.println("</model>");
		out.close();
		*/
		fileName = mod.getName() + ".sbml";
		try {
			out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8")); 
			//out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
		} catch (IOException e) {
			
			e.printStackTrace();
		}

		printModelCompHeader();
		
		modelSBML = "<model id=\"" + mod.getName() + "\">";
		out.println(modelSBML);
		if (!mod.getChildren().isEmpty())
		{
			//printSubmodelInformation(mod);
		}
		
		/*
		if (!mod.getPorts().isEmpty())
		{
			printPortInformation(mod);
		}
		*/
		modelSBML = "</model>\n";
		modelSBML += "</sbml>\n";
		out.println(modelSBML);
		
		//printModelDefinitions(mod);
		
		out.close();
		return fileName;
	}
	
	/**
	 * Print the model definitions.
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
		output += addSpace(2) + "<comp:listOfModelDefinitions>" + eol;
		while(children.hasNext())
		{
			child = children.next();
			dataModel = AC_GUI.copasiUtility.getCopasiModelFromKey(child.getKey());
			System.out.println(child.getName() + " key = " + child.getKey());
			System.out.println(child.getName() + " dataModel key = " + dataModel.getModel().getKey());
			System.out.println(child.getName() + " dataModel name = " + dataModel.getObjectName());
			hasPorts = (child.getPorts().size() != 0);
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
				modelSBML = dataModel.exportSBMLToString(3, 1);
				modelSBML = modelSBML.substring(modelSBML.indexOf("<model"));
				modelSBML = modelSBML.replaceAll("<model", "<comp:modelDefinition");
				modelSBML = modelSBML.replaceAll("</model>", "</comp:modelDefinition>");
				modelSBML = modelSBML.replaceAll("\n</sbml>", "");
				modelSBML = modelSBML.replaceAll("</sbml>", "");
				modelSBML = addSpace(2) + modelSBML;
				
				// replace the old id with the correct id
				int startIDIndex = modelSBML.indexOf(" id=");
				int endIDIndex = modelSBML.indexOf("name=");
				String oldID = modelSBML.substring(startIDIndex+1, endIDIndex);
				System.out.println("OldID: " + oldID);
				String newID = "id=\"" + child.getKey() + "\" ";
				modelSBML = modelSBML.replaceAll(oldID, newID);
				
				// add the port definitions to the submodule
				//int endListofReactionsIndex = modelSBML.indexOf("</listOfReactions>");
				int endofModelDefIndex = modelSBML.indexOf("</comp:modelDefinition>");
				if (hasPorts)
				{
					String part1 = modelSBML.substring(0, endofModelDefIndex);
					String part2 = modelSBML.substring(endofModelDefIndex);
					String portDefinitions = getPortDefinitions(child);
					
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
		}
		//out.println(addSpace(2) + "</comp:listOfModelDefinitions>");
		output += addSpace(2) + "</comp:listOfModelDefinitions>" + eol;
		return output;
	}
	

	private String getContainerDefinition(Module mod)
	{
		String modSBML = "";
		CCopasiDataModel dataModel;
		
		dataModel = AC_GUI.copasiUtility.getCopasiModelFromKey(mod.getKey());
		
		try
		{
			modSBML = dataModel.exportSBMLToString(3, 1);
			modSBML = modSBML.substring(modSBML.indexOf("<model"));
			modSBML = modSBML.replaceAll("\n</sbml>", "");
			modSBML = modSBML.replaceAll("</sbml>", "");
			modSBML = addSpace(2) + modSBML;
			
			// replace the old id with the correct id
			int startIDIndex = modSBML.indexOf(" id=");
			int endIDIndex = modSBML.indexOf("name=");
			String oldID = modSBML.substring(startIDIndex+1, endIDIndex);
			System.out.println("OldID: " + oldID);
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
			
			idRef = port.getRefName();
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
		System.out.println(oldCompartment);
		
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
		output = addReplacementSpecies(output, mod);
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
		
		System.out.println("oldSpeciesList: ");
		System.out.println(oldSpeciesList);
		System.out.println();
		startSpeciesIndex = newSpeciesList.indexOf("<species");
		while (startSpeciesIndex != -1)
		{
			endSpeciesIndex = newSpeciesList.indexOf(">", startSpeciesIndex);
			oldSpecies = newSpeciesList.substring(startSpeciesIndex, endSpeciesIndex+1);
			speciesName = oldSpecies.substring(oldSpecies.indexOf("name=\"") + 6, oldSpecies.indexOf("\" compartment"));
			System.out.println(speciesName);
			
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
	
	private Object findVariable(String name, Module mod)
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
	
	private String getVisibleVariableReplacements(VisibleVariable var)
	{
		Object outgoingEdges [] = mxGraphModel.getOutgoingEdges(AC_GUI.drawingBoard.getGraph().getModel(), var.getDrawingCell());
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
	
	private String addSpace(int length)
	{
		String space = "";
		
		for(int i = 0; i < length; i++)
		{
			space += " ";
		}
		
		return space;
	}
	
	private void printModelCompHeader()
	{
		String space = addSpace(6);
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<sbml xmlns=\"http://www.sbml.org/sbml/level3/version1/core\" level=\"3\" version=\"1\" ");
		out.println(space + "xmlns:comp=\"http://www.sbml.org/sbml/level3/version1/comp/version1\" comp:required=\"true\">");
		out.println(space + "xmlns:html=\"http://www.w3.org/1999/xhtml\"");
		out.println(space + "xmlns:jigcell=\"http://www.sbml.org/2001/ns/jigcell\"");
		out.println(space + "xmlns:math=\"http://www.w3.org/1998/Math/MathML\">\n");
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
}
