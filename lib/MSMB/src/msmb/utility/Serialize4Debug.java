package msmb.utility;

import  msmb.gui.MainGui;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.COPASI.*;

public class Serialize4Debug {

	//!!!CModel cannot be serialized !!!! 
	/*public static void serializeCModel(CModel model, String shortHintBug) throws Exception {
		String fileName = model.getObjectName().replaceAll(" ", "") + "__" +shortHintBug + ".ser";
		ObjectOutput out = new ObjectOutputStream(new FileOutputStream(fileName,true));
	    out.writeObject(model);
	    out.close();
	}
		
	public static CModel deserializeCModel(String fileName) throws Exception {
		File file = new File(fileName);
	    ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
	    CModel model = (CModel) in.readObject();
	    in.close();
		return model;
	}*/
 
	public static void writeCopasiStateSummary(String copasiDataModelSBML_ID, String testName) throws Exception {
		for (long s = 0; s < CCopasiRootContainer.getDatamodelList().size(); s++) 
		   {
			 CCopasiDataModel copasiDataModel = CCopasiRootContainer.get(s); //CCopasiRootContainer.getDatamodel(s); //
			 CModel model = copasiDataModel.getModel();
			 if (model.getSBMLId().compareTo(copasiDataModelSBML_ID) == 0)	 {
				 writeCopasiStateSummary(model, testName);
				 break;
			 }
		   }
	}
	
	
	public static void writeCopasiStateSummary(CModel model, String testName) throws Exception {
		String fileName = model.getObjectName().replaceAll(" ", "") + "__"+ testName+"__complete.summary";
		
		writeCModelSummary(model, fileName);
		writeCFunctionDBSummary(fileName);
	}

	private static void writeCFunctionDBSummary(String fileName) throws Exception {
		BufferedWriter buffout= new BufferedWriter(new FileWriter(fileName,true));
		PrintWriter out = new PrintWriter(buffout);
		CFunctionDB funDB_copasi = CCopasiRootContainer.getFunctionList();
		CFunctionVectorN list = funDB_copasi.loadedFunctions();
		out.println("");
		int size = (int) list.size();
		out.println("___ Functions ("+size+")___");
		for(int i = 0; i < size ; i++ ) {
			String original = list.get(i).getObjectName();
			try{
				CFunction element = (CFunction)funDB_copasi.findFunction(original);
				out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
				out.println(element.getObjectName());
				out.println(element.getInfix());
				out.println(Constants.ReactionType.getDescriptionFromCopasiType(element.getType()));
				//MIIIIIIIIIIIIIIIIIIIIIIIIISSING PARAMETERS DATA... MAYBE I WILL NEED TO PRINT THOSE TOO
				//}
				out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			} catch(Exception ex) {
				 //if I get an exception it means that the function is not there any more (for some mysterious reasons)
				//so I can print an error message
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
				out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
				out.println("Error trying to export function: "+ original);
				out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			}
			
		}
		out.flush();
    	out.close();
	}

	private static void writeCModelSummary(CModel model, String fileName) throws Exception {
		BufferedWriter buffout= new BufferedWriter(new FileWriter(fileName,false));
		PrintWriter out = new PrintWriter(buffout);
		out.println("");
		out.println(model.getObjectName());
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SS");
		out.println(formatter.format(new Date()));
		out.println("");
		out.flush();
    	out.close();
    	
		writeMetabolitesSummary(model, fileName);
		
		writeCompartmentsSummary(model, fileName);
		
		writeReactionsSummary(model, fileName);

		writeEventsSummary(model, fileName);
		
		writeGlobalQSummary(model, fileName);
	}

	private static void writeEventsSummary(CModel model, String fileName) throws Exception {
		BufferedWriter buffout= new BufferedWriter(new FileWriter(fileName,true));
		PrintWriter out = new PrintWriter(buffout);
		out.println("");
		out.println("___ EVENTS___");
		int iMax = (int)model.getEvents().size();
		for(int i = 0; i < iMax; i++) {
			CEvent element = model.getEvent(i);
			out.println("___");
			out.println(element.getTriggerExpression());
			long assignSize = element.getAssignments().size();
			for(int j = 0; j < assignSize; j++) {
				CEventAssignment el = (CEventAssignment) element.getAssignment(j);
				out.println(el.getTargetKey() + "=" + el.getExpression());
			}
			if(element.getDelayAssignment()) out.println(element.getDelayExpression());
			out.println("---");
		}
		
		//out.println("I cannot get the CEvents from the vector");
		out.println("-------------");
		out.flush();
    	out.close();
	}

	private static void writeReactionsSummary(CModel model,	String fileName) throws Exception {
		BufferedWriter buffout= new BufferedWriter(new FileWriter(fileName,true));
		PrintWriter out = new PrintWriter(buffout);
		out.println("___ REACTIONS ___");
		int iMax = (int)model.getReactions().size();
		for(int i = 0; i < iMax; i++) {
			CReaction element = model.getReaction(i);
			out.println("___");
			out.println(element.getChemEq().getObjectDisplayName());
			out.println(element.getFunction().getObjectName());
			out.println("---");
		}
		out.println("-----------------");
		out.flush();
    	out.close();
	}

	private static void writeMetabolitesSummary(CModel model, String fileName) throws Exception {
		BufferedWriter buffout= new BufferedWriter(new FileWriter(fileName,true));
		PrintWriter out = new PrintWriter(buffout);
		out.println("___ METABOLITES ___");
		int iMax = (int)model.getMetabolites().size();
		for(int i = 0; i < iMax; i++) {
			CMetab element = model.getMetabolite(i);
			out.println("___");
			out.println(element.getObjectName());
			out.println("sbmlID: "+element.getSBMLId());
			out.println(element.getInitialConcentration());
			out.println(element.getInitialValue());
			out.println("type:"+Constants.SpeciesType.getDescriptionFromCopasiType(element.getStatus()));
			out.println("expression:"+element.getExpression());
			out.println("comp:"+element.getCompartment().getObjectName());
			out.println("---");
		}
		out.println("-----------------");
		out.flush();
    	out.close();
	}
	
	private static void writeGlobalQSummary(CModel model, String fileName) throws Exception {
		BufferedWriter buffout= new BufferedWriter(new FileWriter(fileName,true));
		PrintWriter out = new PrintWriter(buffout);
		out.println("___ GLOBAL Q ___");
		int iMax = (int)model.getModelValues().size();
		for(int i = 0; i < iMax; i++) {
			CModelValue element = model.getModelValue(i);
			out.println("___");
			out.println(element.getObjectName());
			out.println(element.getInitialValue());
			out.println("type:"+Constants.GlobalQType.getDescriptionFromCopasiType(element.getStatus()));
			out.println("expression:"+element.getExpression());
			out.println("---");
		}
		out.println("-----------------");
		out.flush();
    	out.close();
	}
	
	
	private static void writeCompartmentsSummary(CModel model, String fileName) throws Exception {
		BufferedWriter buffout= new BufferedWriter(new FileWriter(fileName,true));
		PrintWriter out = new PrintWriter(buffout);
		out.println("___ COMPARTMENTS ___");
		int iMax = (int)model.getCompartments().size();
		for(int i = 0; i < iMax; i++) {
			CCompartment element = model.getCompartment(i);
			out.println("___");
			out.println(element.getObjectName());
			out.println(element.getInitialValue());
			out.println(Constants.CompartmentsType.getDescriptionFromCopasiType(element.getStatus()));
			out.println(element.getExpression());
			out.println("---");
		}
		out.println("-----------------");
		out.flush();
    	out.close();
	}
	
	
	
}
