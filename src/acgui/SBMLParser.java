package acgui;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ListIterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.COPASI.*;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
	
	public static void importSBML(String fileName)
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(fileName);
			doc.getDocumentElement().normalize();
			importContainerModule(doc);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
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
					setPortIDRefs(part1, child);
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
	
	private static String addSpace(int length)
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
		
		System.out.println(speciesList);
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
			
			System.out.println("Port name: " + name + ".......ID: " + sbmlID);
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
		if (Node.CDATA_SECTION_NODE == type) {
		  return "<![CDATA[" + n.getNodeValue() + "]]&gt;";
		}
		
		if (name.startsWith("#")) 
		  return "";
		
		StringBuffer sb = new StringBuffer();
		sb.append('<').append(name);
		
		NamedNodeMap attrs = n.getAttributes();
		if (attrs != null) {
		  for (int i = 0; i < attrs.getLength(); i++) {
		    Node attr = attrs.item(i);
		    sb.append(' ').append(attr.getNodeName()).append("=\"").append(attr.getNodeValue()).append(
		"\"");
		  }
		}
		
		String textContent = null;
		NodeList children = n.getChildNodes();
		
		if (children.getLength() == 0) {
		 if ((textContent = n.getTextContent()) != null && !"".equals(textContent)) {
		sb.append(textContent).append("</").append(name).append('>');
		    ;
		  } else {	
		sb.append("/>").append('\n');
		  }
		} else {
		  sb.append('>').append('\n');
		  boolean hasValidChildren = false;
		  for (int i = 0; i < children.getLength(); i++) {
		    String childToString = elementToString(children.item(i));
		    if (!"".equals(childToString)) {
		      sb.append(childToString);
		      hasValidChildren = true;
		    }
		  }
		
		  if (!hasValidChildren && ((textContent = n.getTextContent()) != null)) {
		    sb.append(textContent);
		  }
		  sb.append("</").append(name).append('>');
		}
		return sb.toString();
	}
	
	private static void importContainerModule(Document doc)
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
		System.out.println ("Root element of the doc is " + doc.getDocumentElement().getNodeName());
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
		
		Module mod = AC_GUI.currentGUI.newModule(name, sbmlID, modString);
		importAllSubmodules(doc, mod);
		//System.out.println(modString);
	}
	
	private static void importAllSubmodules(Document doc, Module parent)
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
		System.out.println("All the models in the file:");
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
				modString += elementToString(child);
			}
			modString += eol + "</model>" + eol + "</sbml>";
			System.out.println(modString);
			AC_GUI.currentGUI.newSubmodule(name, sbmlID, modString, parent);
			modString = prefix;
		}
	}
}
