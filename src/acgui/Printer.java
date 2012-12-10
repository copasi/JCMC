package acgui;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ListIterator;

import org.COPASI.*;

/**
 * A printer for the module.
 * @author T.C. Jones
 */
public class Printer {

	PrintWriter out;
	String modelSBML;
	String fileName;
	
	/**
	 * Construct the object.
	 */
	public Printer()
	{
		//System.out.println(CCopasiRootContainer.getDatamodelList().size());
		out = null;
		modelSBML = "";
		fileName = "";
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
			out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		modelSBML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		modelSBML += "<sbml xmlns=\"http://www.sbml.org/sbml/level3/version1/core\" level=\"3\" version=\"1\" xmlns:comp=\"http://www.sbml.org/sbml/level3/version1/comp/version1\" comp:required=\"true\">";
		out.println(modelSBML);

		printModelDefinitions(mod);
		
		modelSBML = "<model id=\"" + mod.getName() + "\">";
		out.println(modelSBML);
		if (!mod.getChildren().isEmpty())
		{
			printSubmodelInformation(mod);
		}
		
		if (!mod.getPorts().isEmpty())
		{
			printPortInformation(mod);
		}
		modelSBML = "</model>\n";
		modelSBML += "</sbml>\n";
		out.println(modelSBML);
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
		
		modelSBML = "<comp:listOfModelDefinitions>";
		out.println(modelSBML);
		while(children.hasNext())
		{
			child = children.next();
			dataModel = CCopasiRootContainer.addDatamodel();
			dataModel.setObjectName(child.getName());
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
			try
			{
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
			out.print(modelSBML);
		}
		modelSBML = "</comp:listOfModelDefinitions>";
		out.println(modelSBML);
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
		
		String childName = "";
		String parentName = "";
		
		out.println("  <comp:listOfSubmodels>");
		while(children.hasNext())
		{
			//<comp:submodel id="subMod1" modelRef="Module_1_1_1"/>
			child = children.next();
			parentName = child.getParent().getName();
			childName = child.getName();
			out.println("    <comp:submodel id=\"" + childName + "\" modelRef=\"" + parentName + "\"/>");
		}
		out.println("  </comp:listOfSubmodels>");
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
}
