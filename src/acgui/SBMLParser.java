package acgui;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ListIterator;

import org.COPASI.*;

/**
 * A printer for the module.
 * @author T.C. Jones
 */
public class SBMLParser {

	private PrintWriter out;
	private String modelSBML;
	private String fileName;
	
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
		
		output = addSpace(2) + "<model id=\"" + rootModule.getName() + "\">";
		out.println(output);
		if (!rootModule.getChildren().isEmpty())
		{
			printSubmodelInformation(rootModule);
		}
		/*
		if (!rootModule.getPorts().isEmpty())
		{
			printPortInformation(rootModule);
		}
		*/
		output = addSpace(2) + "</model>";
		out.println(output);
		
		printModelDefinitions(rootModule);
		
		out.println("</sbml>");
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
			printSubmodelInformation(mod);
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
	private void printModelDefinitions(Module mod)
	{
		ListIterator<Module> children = mod.getChildren().listIterator();
		Module child;
		CCopasiDataModel dataModel;
		String annotationHeader = "<COPASI xmlns=\"http://www.copasi.org/static/sbml\">\n";
		annotationHeader +=  "<params>\n";
		String annotationFooter = "</params>\n" + "</COPASI>\n";
		
		out.println(addSpace(2) + "<comp:listOfModelDefinitions>");
		while(children.hasNext())
		{
			child = children.next();
			dataModel = AC_GUI.copasiUtility.getCopasiModelFromKey(child.getKey());
			System.out.println(child.getName() + " key = " + child.getKey());
			System.out.println(child.getName() + " dataModel key = " + dataModel.getModel().getKey());
			System.out.println(child.getName() + " dataModel name = " + dataModel.getObjectName());
			
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
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			out.println(modelSBML);
		}
		out.println(addSpace(2) + "</comp:listOfModelDefinitions>");
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
		
		String portName;
		String portType;
		
		portInfo = "<listOfPorts>\n";
		
		while(ports.hasNext())
		{
			port = ports.next();
			
			portName = port.getName();
			
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
			
			portInfo += "<port id=\"" + portName + "_port" + "\" ";
			portInfo += "idRef=\"" + portName + "\" ";
			portInfo += "name=\"" + portName + "\" ";
			portInfo += "type=\"" + portType + "\"/>\n";
		}
		
		portInfo += "</listOfPorts>\n";
		return portInfo;
	}
	
	/**
	 * Write the submodel information of the given module.
	 * @param mod the module containing the submodels
	 */
	private void printSubmodelInformation(Module mod)
	{
		ListIterator<Module> children = mod.getChildren().listIterator();
		Module child;
	
		String output;
		String space = addSpace(4);
		String id = "";
		String modelRef = "";
		
		out.println(space + "<comp:listOfSubmodels>");
		space = addSpace(6);
		while(children.hasNext())
		{
			//<comp:submodel id="subMod1" modelRef="Module_1_1_1"/>
			child = children.next();
			id = child.getName();
			modelRef = child.getKey();
			
			out.println(space + "<comp:submodel comp:id=\"" + id + "\" comp:modelRef=\"" + modelRef + "\"/>");
		}
		space = addSpace(4);
		out.println(space + "</comp:listOfSubmodels>");
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
}
