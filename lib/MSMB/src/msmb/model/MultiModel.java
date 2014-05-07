package msmb.model;


import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

import msmb.utility.*;
import msmb.utility.Constants.GlobalQType;
import msmb.utility.Constants.SpeciesType;

import org.COPASI.*;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jgraph.graph.AttributeMap;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;

import com.google.common.collect.Sets;

import msmb.parsers.chemicalReaction.MR_ChemicalReaction_Parser;
import msmb.parsers.chemicalReaction.syntaxtree.CompleteSpeciesWithCoefficient;
import msmb.parsers.chemicalReaction.visitor.ExtractSubProdModVisitor;
import msmb.parsers.mathExpression.MR_Expression_Parser;
import msmb.parsers.mathExpression.MR_Expression_ParserConstants;
import msmb.parsers.mathExpression.MR_Expression_ParserConstantsNOQUOTES;
import msmb.parsers.mathExpression.syntaxtree.CompleteExpression;
import msmb.parsers.mathExpression.syntaxtree.SingleFunctionCall;
import msmb.parsers.mathExpression.visitor.CopasiVisitor;
import msmb.parsers.mathExpression.visitor.EvaluateExpressionVisitor;
import msmb.parsers.mathExpression.visitor.ExpressionVisitor;
import msmb.parsers.mathExpression.visitor.GetFunctionNameVisitor;
import msmb.parsers.mathExpression.visitor.GetFunctionParametersVisitor;
import msmb.parsers.mathExpression.visitor.GetUsedVariablesInEquation;
import msmb.parsers.multistateSpecies.MR_MultistateSpecies_Parser;
import msmb.parsers.multistateSpecies.MR_MultistateSpecies_ParserConstantsNOQUOTES;
import msmb.parsers.multistateSpecies.syntaxtree.CompleteMultistateSpecies;
import msmb.parsers.multistateSpecies.syntaxtree.CompleteMultistateSpecies_Operator;
import msmb.parsers.multistateSpecies.visitor.MultistateSpeciesVisitor;

import msmb.debugTab.*;
import msmb.gui.*;

public class MultiModel {
	
	private HashMap<String, Vector<Integer>> allNamedElements = new HashMap<String, Vector<Integer>>(); // name + list of indices of tables where the name is already defined
	public Vector<Integer> getWhereNameIsUsed(String name) { 
		if(CellParsers.isMultistateSpeciesName(name)) {
			name = CellParsers.extractMultistateName(name);
		}
		Vector ret = allNamedElements.get(name);
		if(ret!= null && ret.size() > 0) 	return ret;
		return null;
	}
	
	public void addNamedElement(String name, Integer tableIndex) throws Throwable { 
		if(name.trim().length()==0) return;
		Vector<Integer> where = getWhereNameIsUsed(name);
		if(where != null) { 
			if(where.contains(tableIndex)
					&& tableIndex != Constants.TitlesTabs.SPECIES.index) throw new Exception(); //already defined in that table
		} else {
			where = new Vector<Integer>();
		}
		if(!where.contains(tableIndex)) where.add(tableIndex);
		
		
		if(where.size() > 1) {
			for(int i = 0; i < where.size(); i++) {
				 DebugMessage dm = new DebugMessage();
				 String descr = Constants.TitlesTabs.getDescriptionFromIndex(where.get(i));
				 dm.setOrigin_table(descr);
				 dm.setProblem(DebugConstants.SAMENAME_MESSAGE + name);
				 dm.setPriority(DebugConstants.PriorityType.DUPLICATES.priorityCode);
				 dm.setOrigin_col(MainGui.getMainElementColumn(descr)); 
				 Integer nrow= -1;
				if(descr.compareTo(Constants.TitlesTabs.REACTIONS.getDescription()) == 0) { nrow = reactionDB.getReactionIndex(name);	}
				else if(descr.compareTo(Constants.TitlesTabs.SPECIES.getDescription()) == 0) { nrow = speciesDB.getSpeciesIndex(name);}
				else if(descr.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription()) == 0) {  nrow = compDB.getCompIndex(name);	}
				else if(descr.compareTo(Constants.TitlesTabs.FUNCTIONS.getDescription()) == 0) { nrow = funDB.getFunctionIndex(name);	}
				else if(descr.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription()) == 0) {  nrow = globalqDB.getGlobalQIndex(name);}
				if(nrow!= null && nrow!= -1) {
					 MainGui.addDebugMessage_ifNotPresent(dm,false);
					dm.setOrigin_row(nrow.intValue());
				}
			
			}
		} else {
			MainGui.clear_debugMessages_samename_relatedWith(name);
		}
		
		allNamedElements.put(name, where);
	}
	
	
	public void removeNamedElement(String name, Integer fromTable) {
			if(name.trim().length()==0) return; 
			Vector<Integer> where = getWhereNameIsUsed(name);
			if(where!=null) {
				where.removeElement(fromTable);
				allNamedElements.put(name, where);
			}
	}
	
	public void removeAllNamedElement(Collection<String> names, Integer fromTable) {
		Iterator it = names.iterator();
		while(it.hasNext()){		
				String name = (String) it.next();
				removeNamedElement(name, fromTable);
				//allNamedElements.remove(name);
		}
}
	
	public int getNumSpecies() { return speciesDB.getAllNames().size();  }
	public int getNumSpeciesExpanded() throws Throwable { return speciesDB.getNumSpeciesExpanded();  }
	public int getNumComp() { return compDB.getAllNames().size(); }
	public int getNumGlobalQ() { return globalqDB.getAllNames().size(); }
	public int getNumEvents() { return eventsDB.getAllEvents().size()-1;} // because the first for row 0 is null 
	//public int getNumReactions() { return reactionsDB.???.size(); }
	//public int getNumReactionsExpanded() { return reactionsDB.???.size(); }
	
	public Vector<Compartment> getAllCompartments() { return compDB.getAllCompartments(); }
	//!!!!!!!!!!!!TO BE DELETED WHEN A REACTIONSDB IS IMPLEMENTED !!!!!!!!!!!!!!!!!!
	CustomTableModel_MSMB tableReactionmodel;
	public void setTableReactionModel(CustomTableModel_MSMB tableReactionmodel2) {	tableReactionmodel = tableReactionmodel2;	}
	public int getNumReactions(){
		int counter = 0;
		for(int i = 0; i < tableReactionmodel.getRowCount()-1 ; i++ ) {
			String string_reaction = ((String)tableReactionmodel.getValueAt(i, Constants.ReactionsColumns.REACTION.index));
			if(string_reaction.trim().length() <= 0) continue;
			counter++;
		}
		return counter;
	}
	
	public int getNumReactionsExpanded() { return this.counterExpandedReactions; }
	private int counterExpandedReactions;
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	
	public SpeciesDB speciesDB = new SpeciesDB(this);
	private ReactionDB reactionDB = new ReactionDB(this);
	private CompartmentsDB compDB = new CompartmentsDB(this);
	private GlobalQDB globalqDB = new GlobalQDB(this); 
	private EventsDB eventsDB = new EventsDB(this);
	public FunctionsDB funDB = new FunctionsDB(this);
	
	
	public CCopasiDataModel copasiDataModel;
	private String copasiDataModel_key = new String("");
	private HashMap<Integer, String> modified_species = new HashMap();

	private String copasiDataModel_modelName = new String(Constants.DEFAULT_MODEL_NAME);
	private String copasiDataModel_modelDefinition = new String(Constants.DEFAULT_MODEL_NAME);
	
	
	public MultiModel() {}
	
	public void addGlobalQ(int index,String name, String value, String type, String expression, String notes) throws Throwable {
		this.globalqDB.addChangeGlobalQ(index,name, value, type, expression, notes);
	}
	
	public boolean changeGlobalQ(String name, String value) throws Throwable {
		//used to change GLQ from the Run Manager for internal evaluations
		int index = this.globalqDB.getGlobalQIndex(name);
		if(index == -1) return false;
		else {
			GlobalQ g = globalqDB.getGlobalQ(name);
			this.globalqDB.addChangeGlobalQ(index,name, value, GlobalQType.getDescriptionFromCopasiType(g.getType()), g.getExpression(), g.getNotes());
			return true;
		}
	}
	public boolean changeSpecies(String name, String value) throws Throwable {
		//used to change SPC from the Run Manager for internal evaluations
		int index = this.speciesDB.getSpeciesIndex(name);
		if(index == -1) return false;
		else {
			Species g = speciesDB.getSpecies(name);
			HashMap<String, String> entry_q = new HashMap<String, String>();
			entry_q.put(name,value);
			this.speciesDB.addChangeSpecies(index,new String(),name,entry_q ,g.getType(), g.getCompartment_listString(), g.getExpression(),false, g.getNotes(), false, false);
			return true;
		}
	}
	
	public boolean changeCompartment(String name, String value) throws Throwable {
		//used to change CMP from the Run Manager for internal evaluations
		int index = this.compDB.getCompIndex(name);
		if(index == -1) return false;
		else {
			Compartment g = compDB.getComp(name);
			this.compDB.addChangeComp(index, name, Constants.CompartmentsType.getDescriptionFromCopasiType(g.getType()), value, g.getExpression(), g.getNotes());
			return true;
		}
	}
	
	
	public GlobalQ getGlobalQ(String name) {
		if(this.globalqDB.getGlobalQ(name) == null) return null;
		//return this.globalqDB.getGlobalQ(name).getInitialValue();
		return this.globalqDB.getGlobalQ(name);
	}
	
	public MultistateSpecies getMultistateSpecies(String name) {
		try {
			MultistateSpecies ret = (MultistateSpecies)this.speciesDB.getSpecies(name);
			return ret;
		} catch(Exception ex) { //problem retrieving the species
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
			return null; 
		}	
	}
	
	
	public boolean isGlobalQAlreadyDefined(String name) {
		return this.globalqDB.contains(name);
	}
	
	public int getSizeSpeciesDB() {
		return this.speciesDB.getSizeSpeciesDB();
	}
	

	
	public String mergeMultistateSpecies(int indexOld, MultistateSpecies newSp) throws Throwable {
		int ind = -(speciesDB.addChangeSpecies(-1,new String(),newSp.printCompleteDefinition(),newSp.getInitialQuantity_multi(),newSp.getType(), newSp.getCompartment_listString(), newSp.getExpression(),false, new String(),true,true));
		return ((MultistateSpecies)speciesDB.getSpecies(ind)).printCompleteDefinition();
	}

	//RESETS COMPLETELY THE SPECIES WITH THE NEW NAME/SITES
	public void modifyMultistateSpecies(MultistateSpecies species, boolean fromMultistateBuilder,int row, boolean autoMergeSpecies) throws Throwable {
		String name = species.printCompleteDefinition();
		int n = this.speciesDB.addChangeSpecies(row+1, new String(), name,species.getInitialQuantity_multi(), species.getType(), species.getCompartment_listString(), species.getExpression(),fromMultistateBuilder, new String(),autoMergeSpecies,true);
		
	}
	
	public int findCompartment(String name) {
		return this.findCompartment(name, false);
	}
	
	private int findCompartment_key(String name) {
		return this.findCompartment(name, true);
	}
	
	public int findCompartment(String name, boolean key) {
		if(name.startsWith("\"")&&name.endsWith("\"")) {	name = name.substring(1,name.length()-1); }
		CModel model = copasiDataModel.getModel();
		int i, iMax =(int) model.getCompartments().size();
        for (i = 0;i < iMax;++i)
        {
            CCompartment comp = model.getCompartment(i);
            assert comp != null;
            String current = new String();
            if(!key) current = comp.getObjectName();
            else current = comp.getKey();
            if(current.startsWith("\"")&&current.endsWith("\"")) {	current = current.substring(1,current.length()-1); }
        	
            if(name.compareTo(current) == 0) return i;
        }
        
        return -1;
	}
	
	public int findReaction(String name, boolean key) {
		if(name.startsWith("\"")&&name.endsWith("\"")) {	name = name.substring(1,name.length()-1); }
		CModel model = copasiDataModel.getModel();
		int i, iMax =(int) model.getReactions().size();
        for (i = 0;i < iMax;++i)
        {
            CReaction r = model.getReaction(i);
            assert r != null;
            String current = new String();
            
            if(!key) current = r.getObjectName();
            else current = r.getKey();
            if(current.startsWith("\"")&&current.endsWith("\"")) {	current = current.substring(1,current.length()-1); }
    		
            if(name.compareTo(current) == 0) return i;
        }
        
        return -1;
	}
	public int findReaction(String name) {
		return this.findReaction(name, false);
	}
	
	public int findGlobalQ(String name) {
		return this.findGlobalQ(name, false);
	}
	
	private int findGlobalQ_key(String name) {
		return this.findGlobalQ(name, true);
	}
	
	
	public int findGlobalQ(String name, boolean key) {
		if(name.startsWith("\"")&&name.endsWith("\"")) {	name = name.substring(1,name.length()-1); }
		CModel model = copasiDataModel.getModel();
		int i, iMax =(int) model.getModelValues().size();
        for (i = 0;i < iMax;++i)
        {
            CModelValue m = model.getModelValue(i);
            String current = new String();
            if(!key) current = m.getObjectName();
            else current = m.getKey();
            if(current.startsWith("\"")&&current.endsWith("\"")) {	current = current.substring(1,current.length()-1); }
        	
            if(name.compareTo(current) == 0) return i;
        }
        
        return -1;
	}
	
	private int findEvent(String name) {
		 
		CModel model = copasiDataModel.getModel();
		int i, iMax =(int) model.getEvents().size();
        for (i = 0;i < iMax;++i)
        {
            if(name.compareTo(model.getEvents().get(i).getObjectName()) == 0) return i;
        }
        
        return -1;
	}
	
	
	
	private int findLocalParameter_key(String name, CCopasiParameterGroup group) {
		 
		for (int i = 0; i < group.size(); i++)
        {
            CCopasiParameter par = group.getParameter(i);
            String current = par.getKey();
            if(name.compareTo(current) == 0) return i;
        }
        
        return -1;
	}
	
	
	public int findMetabolite(String name, String cmp) throws Exception {
		return findMetabolite(name,cmp, false);
	}
	
	private int findMetabolite_key(String name) throws Exception {
		return findMetabolite(name,null, true);
	}
	
	
	
	
	private int findMetabolite(String name, String cmp, boolean key) throws Exception {
		
		if(CellParsers.isMultistateSpeciesName(name)) {
			MultistateSpecies ms = new MultistateSpecies(this,name);
			name = ms.printCompleteDefinition(); 
			//because in "name" the order of the sites can be different from the order used for defining the metabolite species.
			//Building the multistateSpecies and printing again its complete definition will make the two definitions identical w.r.t. the order
		}
		
		if(name.startsWith("\"")&&name.endsWith("\"")) {	name = name.substring(1,name.length()-1); }
		if(cmp!= null && cmp.startsWith("\"")&&cmp.endsWith("\"")) {	cmp = cmp.substring(1,cmp.length()-1); }
			
		CModel model = copasiDataModel.getModel();
		int i, iMax =(int) model.getMetabolites().size();
		
        for (i = 0;i < iMax;++i)
        {
            CMetab metab = model.getMetabolite(i);
            assert metab != null;
            if(!key) {
	            String current = metab.getObjectName();
	        	if(current.startsWith("\"")&&current.endsWith("\"")) {	current = current.substring(1,current.length()-1); }
	        	
	          /*  int level_similarity = 1;
	            if(!current.contains("(") && !name.contains("(") &&
	            	SimilarityStrings.damlev(current, name)!= 0 &&
	                SimilarityStrings.damlev(current, name) <= level_similarity) {
	         		DebugMessage dm = new DebugMessage();
	        	//	dm.setOrigin_cause("Similarity strings <= "+level_similarity);
	        		dm.setOrigin_table(Constants.TitlesTabs.SPECIES.getDescription());
	        		dm.setProblem("Species "+ name + " and " + current + " have a degree of similarity lower than "+level_similarity+"\n. Two species have been added but maybe a misstype has occurred in the definition of the two species.");
	        		dm.setPriority(DebugConstants.PriorityType.SIMILARITY.priorityCode);
	        		dm.setOrigin_col(1);
	        		//MainGui.debugMessages.add(dm);
	        	}*/
	            
	            if(name.compareTo(current) == 0) {
	            	if(cmp!= null) { 
	            		String cmp2 = metab.getCompartment().getObjectName();
	            		if(cmp2!= null && cmp2.startsWith("\"")&&cmp2.endsWith("\"")) {	cmp2 = cmp2.substring(1,cmp2.length()-1); }
	            		
	            		if(cmp2.compareTo(cmp)==0 ) return i;
	            		else continue;
	            	}
	            	else return i;
	            }
            } else {
            	String current = metab.getKey();
            	
            	if(name.compareTo(current) == 0) return i;
            }
        }
        
        return -1;
	}
	
	private int findMetabolite_sbmlID(String id) {
		
		CModel model = copasiDataModel.getModel();
		int i, iMax =(int) model.getMetabolites().size();
		
        for (i = 0;i < iMax;++i)
        {
            CMetab metab = model.getMetabolite(i);
            String current = metab.getSBMLId();
	 
	        if(id.compareTo(current) == 0) return i;
           
        }
        
        return -1;
	}
	
	
	public void saveSBML(File file) throws Exception {
		 this.copasiDataModel.exportSBML(file.getAbsolutePath(), true, 3, 1);
		 
		 clearCopasiDataModel();
		 MainGui.clearCopasiFunctions();
		 System.out.println("saved");System.out.flush();
		 System.out.println("cleared");System.out.flush();
	}
	
	
	public String saveCPS(
						boolean clearOldData,
						File file, 
						CustomTableModel_MSMB tableReactionmodel, 
						ProgressBarFrame progressBarFrame) throws Throwable {
		
		clearCopasiDataModel(clearOldData);
		
		 progress(progressBarFrame,Constants.ProgressBar.LOADING_CPS.progress);
		 	
		 fillCopasiDataModel(tableReactionmodel, progressBarFrame);
		 		
		 System.out.println("created"); System.out.flush();
				 			
		 
		 progress(progressBarFrame,Constants.ProgressBar.COMPILING_CPS.progress);
		copasiDataModel.getModel().compile();
		 System.out.println("compiled"); System.out.flush();
			
		// progress(progressBarFrame,Constants.ProgressBar.UPDATING_CPS.progress);
		 //this.copasiDataModel.getModel().updateInitialValues(changedObjects);
		// System.out.println("updated");System.out.flush();
		 progress(progressBarFrame,Constants.ProgressBar.SAVING_CPS.progress);
		 if(file != null) {
			 //Serialize4Debug.writeCopasiStateSummary(copasiDataModel.getModel(), "TEST");
			// System.out.println("about to save"); System.out.flush();
			 
			 this.copasiDataModel.saveModel(file.getAbsolutePath(), true);
			 System.out.println("saved");System.out.flush();
			 progress(progressBarFrame,Constants.ProgressBar.COMPILING_CPS.progress);
			 
			 //addAnnotations(file);
			 
			 clearCopasiDataModel();
			 MainGui.clearCopasiFunctions();
			 System.out.println("cleared");System.out.flush();
			 progress(progressBarFrame,Constants.ProgressBar.END.progress);
			 return null;
		 } else {
			 progress(progressBarFrame,100);
				
				return this.copasiDataModel.getModel().getKey();
		 }
		
	}
	
	
	public String exportXPP(File file, CustomTableModel_MSMB tableReactionmodel2, ProgressBarFrame progressBarFrame) throws Throwable {
				
		progress(progressBarFrame,Constants.ProgressBar.LOADING_CPS.progress);

		fillCopasiDataModel(tableReactionmodel2, progressBarFrame);

		progress(progressBarFrame,Constants.ProgressBar.COMPILING_CPS.progress);

		this.copasiDataModel.getModel().compile();
		System.out.println("compiled");
		System.out.flush();
		//progress(progressBarFrame,Constants.ProgressBar.UPDATING_CPS.progress);
		//System.out.println("updated");System.out.flush();
		progress(progressBarFrame,Constants.ProgressBar.SAVING_CPS.progress);
		if(file != null) {
			
			System.out.println("exported");System.out.flush();
			String math = this.copasiDataModel.exportMathModelToString("XPPAUT (*.ode)");
			
			BufferedWriter buffout= new BufferedWriter(new FileWriter(file,true));
			PrintWriter out = new PrintWriter(buffout);
			out.println(math);
			
			out.flush();
			out.close();
			progress(progressBarFrame,Constants.ProgressBar.COMPILING_CPS.progress);
			System.out.println("saved");System.out.flush();
			
			//addAnnotations(file);

			clearCopasiDataModel();
			System.out.println("cleared");System.out.flush();
			progress(progressBarFrame,Constants.ProgressBar.END.progress);
			return null;
		} else {
			progress(progressBarFrame,100);

			return this.copasiDataModel.getModel().getSBMLId();
		}

	}
	
	
	
	
	
	
	private void progress(ProgressBarFrame progressBarFrame, int i) throws InterruptedException {
		if(progressBarFrame !=null) {
				synchronized (progressBarFrame) {
		 			progressBarFrame.progress(i);
		 			progressBarFrame.notifyAll();
			 	}
		 	}
		 return;
	}
	
	
	private void fillCopasiDataModel(CustomTableModel_MSMB tableReactionmodel2, ProgressBarFrame progressBarFrame) throws Throwable {
		
		
		
		
		
		CFunctionDB copasiFunDB = null;
		boolean problemsWithSomeExpression = false;
		Vector remainingSpecies = new Vector();
		Vector remainingGlobalQ = new Vector();
		Vector remainingCompartments = new Vector();
		
		try {
			if(progressBarFrame!=null) progress(progressBarFrame,Constants.ProgressBar.LOADING_GLOBQ__CPS.progress);
			Vector<GlobalQ> allGlobalQ = this.globalqDB.getAllGlobalQ();
			remainingGlobalQ = fillCopasiDataModel_globalQ_fixed(allGlobalQ);
		} catch(Exception ex) {
			problemsWithSomeExpression = true; // but I should wait to signal that because some expression can refer to reaction fluxes, so I will run this loop after the reactions
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)		ex.printStackTrace();
		}
		
		
		
		//System.out.println("....compartments"); System.out.flush();
	
		try {
			try {
				if(progressBarFrame!=null) progress(progressBarFrame,Constants.ProgressBar.LOADING_COMP_CPS.progress);
				Vector<Compartment> allCompartments = this.compDB.getAllCompartments();
				remainingCompartments = fillCopasiDataModel_compartments(allCompartments);
			} catch(Exception ex) {
				problemsWithSomeExpression = true; // but I should wait to signal that because some expression can refer to reaction fluxes, so I will run this loop after the reactions
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)	ex.printStackTrace();
			}
			
			
			try {
				if(progressBarFrame!=null) progress(progressBarFrame,Constants.ProgressBar.LOADING_SPECIES_CPS.progress);
				Vector<Species> allSpecies = this.speciesDB.getAllSpecies();
				remainingSpecies = fillCopasiDataModel_species(allSpecies);
				//System.out.println("....species"); System.out.flush();
			} catch(Exception ex) {
				problemsWithSomeExpression = true; // but I should wait to signal that because some expression can refer to reaction fluxes, so I will run this loop after the reactions
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)	ex.printStackTrace();

			}
			
			if(!MainGui.quantityIsConc) {
				CModel model = this.copasiDataModel.getModel();
				MSMB_UnsupportedAnnotations partNum_annotation = new MSMB_UnsupportedAnnotations(MSMB_UnsupportedAnnotations.MSMB_UnsupportedAnnotations_type.GET_PARTICLE_NUMBER, "");
				model.addUnsupportedAnnotation(partNum_annotation.getName(), partNum_annotation.getAnnotation());
			}

			try {
				if(progressBarFrame!=null) progress(progressBarFrame,Constants.ProgressBar.LOADING_FUNCTIONS_CPS.progress);
				copasiFunDB = fillCopasiDataModel_functions();
				//System.out.println("....functions"); System.out.flush();
				//System.out.println("after fillCopasiDataModel_functions:" +funDB.loadedFunctions().size());
			} catch(Exception ex) {
				problemsWithSomeExpression = true; // but I should wait to signal that because some expression can refer to reaction fluxes, so I will run this loop after the reactions
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)	ex.printStackTrace();

			}
			try {
				remainingGlobalQ = fillCopasiDataModel_globalQ_assignment_ode(remainingGlobalQ);
				//System.out.println("....globalq_assignment"); System.out.flush();
			} catch(Exception ex) {
				problemsWithSomeExpression = true; // but I should wait to signal that because some expression can refer to reaction fluxes, so I will run this loop after the reactions
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)	ex.printStackTrace();
			}
			
			int remainingSp = remainingSpecies.size();
			int remainingGlQ = remainingGlobalQ.size();
			int remainingComp = remainingCompartments.size();
			int counter = 20;

			while(remainingSpecies.size()!=0 || remainingGlobalQ.size() != 0 || remainingCompartments.size() !=0) {
				//System.out.println("counter: "+counter +": Problems exporting "+remainingCompartments.size()+" compartments, "+remainingSpecies.size()+" species and " + remainingGlobalQ.size() +" parameters");
				//System.out.flush();
				if(counter == 0) {
					if(progressBarFrame!=null) progress(progressBarFrame,Constants.ProgressBar.END.progress);
					throw new Exception("Problems exporting "+remainingCompartments.size()+" compartments, "+remainingSpecies.size()+" species and " + remainingGlobalQ.size() +" parameters");
				}
				
				remainingGlobalQ = fillCopasiDataModel_globalQ_assignment_ode(remainingGlobalQ);
				remainingCompartments = fillCopasiDataModel_compartments(remainingCompartments);
				remainingSpecies = fillCopasiDataModel_species(remainingSpecies);
				if(remainingSpecies.size() == remainingSp && remainingGlobalQ.size() == remainingGlQ && remainingCompartments.size() == remainingComp) {
					counter--;
				} else {
					remainingSp = remainingSpecies.size();
					remainingGlQ = remainingGlobalQ.size();
					remainingComp = remainingCompartments.size();
				}

			}
		} catch(Throwable ex) {
			problemsWithSomeExpression = true; // but I should wait to signal that because some expression can refer to reaction fluxes, so I will run this loop after the reactions
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)	ex.printStackTrace();
			
		}
		
		//CFunctionVectorN allFunctions = funDB.loadedFunctions();
		CModel model = copasiDataModel.getModel();

		if(progressBarFrame!=null) progress(progressBarFrame,Constants.ProgressBar.LOADING_REACTIONS_CPS.progress);
		counterExpandedReactions = 0;
		if(tableReactionmodel2!= null) {
			//--REACTIONS------------------------
			//TAKE REACTIONS FROM THE DB not from the GUIIIIIIIIIIIIII
			//TOOOOOOOOOOOO BEEEEEEEEEEEEE FIXED.
			HashMap<String, Integer> assignedUsage = new  HashMap<String, Integer>();
			
			for(int i = 0; i < tableReactionmodel2.getRowCount() ; i++ ) {
				//System.out.println("Exporting reaction " + i + " of "+ tableReactionmodel.getRowCount()); System.out.flush();
				String string_reaction = ((String)tableReactionmodel2.getValueAt(i, 2));
				if(string_reaction.trim().length() <= 0) continue;
		

				boolean parseErrors = false;
				Vector metabolites = new Vector();
				HashMap<String, String> aliases = null;
				HashMap<Integer, String> aliases2 = null;
				try{ 
					metabolites = CellParsers.parseReaction(this,string_reaction,i+1);
					aliases = CellParsers.getAllAliases(string_reaction);
					 aliases2 = CellParsers.getAllAliases_2(string_reaction);
		
				} catch(Exception ex) {
					if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
					parseErrors = true;
					metabolites.add(new Vector());
					metabolites.add(new Vector());
					metabolites.add(new Vector());
				}

				if(parseErrors) continue;


				Vector singleConfigurations = expandReaction(metabolites, aliases,aliases2, i);
				CReaction reaction;
				for(int j = 0; j < singleConfigurations.size(); j++) {
					Vector expandedReaction = (Vector) singleConfigurations.get(j);

					String reaction_name = ((String)tableReactionmodel2.getValueAt(i, 1)).trim();
					boolean reaction_name_empty = false;
					if(reaction_name.length() ==0) {
						reaction_name_empty = true;
						reaction_name = (i+1)+"";
						if(singleConfigurations.size() > 1) {
							reaction_name += "_"+(j+1)+"";
						} 
					} else {
						if(singleConfigurations.size() > 1) {
							reaction_name = reaction_name+"_"+(j+1);
						}
					}
					reaction_name = reaction_name.replace("\"", "");
					reaction = model.createReaction(reaction_name);
			
					counterExpandedReactions++;
					if(reaction == null) { 
						reaction = model.getReaction(this.findReaction(reaction_name)); 
						reaction.cleanup();
						counterExpandedReactions--;
					}
					
					if(reaction_name_empty) {
					  MSMB_UnsupportedAnnotations rct_annotation = new MSMB_UnsupportedAnnotations(MSMB_UnsupportedAnnotations.MSMB_UnsupportedAnnotations_type.REACTION_EMPTY_NAME, null);
					  reaction.addUnsupportedAnnotation(rct_annotation.getName(), rct_annotation.getAnnotation());
					}
					
					if(singleConfigurations.size() > 1) {
						MSMB_UnsupportedAnnotations multistate_reaction = new MSMB_UnsupportedAnnotations(MSMB_UnsupportedAnnotations.MSMB_UnsupportedAnnotations_type.MULTISTATE_REACTION, string_reaction);
						reaction.addUnsupportedAnnotation(multistate_reaction.getName(), multistate_reaction.getAnnotation());
						MSMB_UnsupportedAnnotations multistate_reactionRateLaw = new MSMB_UnsupportedAnnotations(MSMB_UnsupportedAnnotations.MSMB_UnsupportedAnnotations_type.MULTISTATE_REACTION_RATE_LAW, ((String)tableReactionmodel2.getValueAt(i, Constants.ReactionsColumns.KINETIC_LAW.index)).trim());
						reaction.addUnsupportedAnnotation(multistate_reactionRateLaw.getName(), multistate_reactionRateLaw.getAnnotation());
					}
					
					reaction.setReversible(false);
					reaction.setNotes((String)tableReactionmodel2.getValueAt(i, Constants.ReactionsColumns.NOTES.index));
					try{
						String rateLaw = ((String)tableReactionmodel2.getValueAt(i, Constants.ReactionsColumns.KINETIC_LAW.index)).trim();
						String kineticType = ((String)tableReactionmodel2.getValueAt(i, Constants.ReactionsColumns.TYPE.index)).trim();
						addDependentModifier_ifNecessary(expandedReaction);
						generateKineticLaw(reaction,i, expandedReaction, rateLaw, kineticType,assignedUsage);
					} catch(Throwable ex) {
						problemsWithSomeExpression = true; // but I should wait to signal that because some expression can refer to species with assignments, so I will run this loop after the reactions
						if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)	ex.printStackTrace();
						
					}					
				}
					
				
			}
		}
		
		
		if(problemsWithSomeExpression) {
			int remainingSp = remainingSpecies.size();
			int remainingGlQ = remainingGlobalQ.size();

			int counter = 10;

			try{
				while(remainingSpecies.size()!=0 || remainingGlobalQ.size() != 0) {
					if(counter == 0) {
						if(progressBarFrame!=null) progress(progressBarFrame,Constants.ProgressBar.END.progress);
						throw new Exception("Problems exporting "+remainingSpecies.size()+" species and " + remainingGlobalQ.size() +" parameters");
					}
					//System.out.println("....remaining species and global q"+remainingSpecies+" ... "+remainingGlobalQ); System.out.flush();

					remainingSpecies = fillCopasiDataModel_species(remainingSpecies);
					remainingGlobalQ = fillCopasiDataModel_globalQ_assignment_ode(remainingGlobalQ);
					if(remainingSpecies.size() == remainingSp || remainingGlobalQ.size() == remainingGlQ) {
						counter--;
					} else {
						remainingSp = remainingSpecies.size();
						remainingGlQ = remainingGlobalQ.size();
					}

				}
			} catch(Throwable ex) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)	ex.printStackTrace();

			}

		}
		
		
		if(progressBarFrame!=null) progress(progressBarFrame,Constants.ProgressBar.LOADING_EVENTS_CPS.progress);
		
		fillCopasiDataModel_events();
		//System.out.println("....events"); System.out.flush();
		
		//if(MainGui.modelName.trim().length() ==0) MainGui.modelName = Constants.DEFAULT_MODEL_NAME;
		//model.setObjectName(MainGui.modelName);
		

		
		
	}
		

	private void addDependentModifier_ifNecessary(Vector expandedReaction) {
		//Vector<String> subs = (Vector)expandedReaction.get(0);
		//Vector<String> prod =(Vector)expandedReaction.get(1);
		Vector<String> mod = (Vector)expandedReaction.get(2);
				
		Vector<Species> species_to_add = new Vector<Species>();
		for(int i = 0; i < mod.size(); i++) {
			String current = mod.get(i);
			String mod_onlyName = CellParsers.extractMultistateName(current);
			String cmp = CellParsers.extractCompartmentLabel(current);
			if(cmp.length()==0) cmp = null;
			int index_metab;
			try {
				index_metab = this.findMetabolite(mod_onlyName,cmp);
				if(index_metab == -1) {
					Species incomplete_mod = this.getSpecies(mod_onlyName);
					MultistateSpecies newSpecies = new MultistateSpecies(this, current);
					newSpecies.setExpression(this, CellParsers.evaluateExpressionWithDependentSum( incomplete_mod.getEditableExpression(), newSpecies)); 
					species_to_add.add(newSpecies);
					
				} 
			} catch (Throwable e) {
				//Problems in the expression... should have been catched before, but if I find them here stop and tell the user!
				e.printStackTrace();
				continue;
			}
		}
		
		if(species_to_add.size() > 0) {
			//System.out.println("species_to_add : "+species_to_add);
			try {
				fillCopasiDataModel_species(species_to_add);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			
		}
	}

	public Vector<Vector<String>> check_ifSingleFunctionCallOrSingleGlobalQ(boolean massAction, int reaction_row, String reactionName, String equation) {
		Vector<Vector<String>> ret = new Vector<Vector<String>>();
		if(!massAction) {
			String funName  = new String();
			try{
				InputStream is = new ByteArrayInputStream(equation.getBytes("UTF-8"));
				MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
				SingleFunctionCall root = parser.SingleFunctionCall();
				GetFunctionNameVisitor vis = new GetFunctionNameVisitor();
				root.accept(vis);
				funName  = vis.getFunctionName();
				Function f = funDB.getFunctionByName(funName);
				if(f==null) {
					f = funDB.getBuiltInFunctionByName(funName);
					if(f == null) {
						return new Vector<Vector<String>>();
					}
				}
				
				
				
				GetFunctionParametersVisitor vis2 = new GetFunctionParametersVisitor();
				root.accept(vis2);
				Vector<String> param = vis2.getActualParameters();
				if(param.size() != f.getNumParam()) {
						return new Vector<Vector<String>>();
				}
				
				for(int i = 0; i < param.size(); i++) {
					is = new ByteArrayInputStream(param.get(i).getBytes("UTF-8"));
					parser = new MR_Expression_Parser(is,"UTF-8");
					CompleteExpression root2 = parser.CompleteExpression();
					GetUsedVariablesInEquation v = new GetUsedVariablesInEquation();
		  			root2.accept(v);
		  			if(v.isComplexExpression()) {
		  			/*	Vector elementGlq = new Vector();
		  				elementGlq.add(Constants.TitlesTabs.GLOBALQ.getDescription());
		  				elementGlq.add(getName_globalQ_4_reaction(reactionName, reaction_row)+"_"+(i+1));
		  				elementGlq.add(param.get(i));
		  				elementGlq.add(i);
		  				ret.add(elementGlq);*/
		  				Vector element = new Vector();
		  				element.add(Constants.TitlesTabs.FUNCTIONS.getDescription());
		  				element.add(getName_function_4_reaction(reactionName, reaction_row));
		  				element.add(equation);
		  				ret.add(element);
		  				break;
		  			}
		  			
		  			
		  		}
				
				return ret;
			} catch (Throwable e) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
				Vector element =  new Vector();
				element.add(Constants.TitlesTabs.FUNCTIONS.getDescription());
				element.add(getName_function_4_reaction(reactionName, reaction_row));
				element.add(equation);
  				ret.add(element);
				return ret;
			} 
		}
		else {
			try {
				InputStream is = new ByteArrayInputStream(equation.getBytes("UTF-8"));
				MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
				CompleteExpression root;
				root = parser.CompleteExpression();
				GetUsedVariablesInEquation v = new GetUsedVariablesInEquation();
	  			root.accept(v);
	  			if(v.isComplexExpression()) {
	  				Vector elementGlq = new Vector();
	  				elementGlq.add(Constants.TitlesTabs.GLOBALQ.getDescription());
	  				elementGlq.add(getName_globalQ_4_reaction(reactionName, reaction_row));
	  				elementGlq.add(equation);
	  				ret.add(elementGlq);
	  			}
	  			return ret;
			} catch (Exception e) {
				return null;
			}
		}
			
	}
	/*public Vector check_ifSingleFunctionCallAndSingleParameters(boolean massAction, int reaction_row, String reactionName, String equation) {
		Vector newEquation_listOfNewNames = new Vector();
		Vector listOfNewNames = new Vector();
		
		if(!massAction) {
			String funName  = new String();
			try{
				InputStream is = new ByteArrayInputStream(equation.getBytes("UTF-8"));
				MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
				SingleFunctionCall root = parser.SingleFunctionCall();
				GetFunctionNameVisitor vis = new GetFunctionNameVisitor();
				root.accept(vis);
				funName  = vis.getFunctionName();
				//singleFunctionCall = true;
				newEquation_listOfNewNames.add("");
				newEquation_listOfNewNames.add(new Vector());
				return newEquation_listOfNewNames;
			} catch (Exception e) {
				//e.printStackTrace();
				//singleFunctionCall = false;
				
				MutablePair<String, String> pair = add_function_4_reaction_funDB(reactionName, reaction_row, equation);
				listOfNewNames.add(pair.right);
				newEquation_listOfNewNames.add(pair.left);
				newEquation_listOfNewNames.add(listOfNewNames);
				return newEquation_listOfNewNames;
			} 
		}
		else {
			
			try {
				InputStream is = new ByteArrayInputStream(equation.getBytes("UTF-8"));
				MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
				CompleteExpression root;
				
					root = parser.CompleteExpression();
				
	  			GetUsedVariablesInEquation v = new GetUsedVariablesInEquation();
	  			root.accept(v);
	  			if(v.getNames().size()==0) {
	  				newEquation_listOfNewNames.add("");
					newEquation_listOfNewNames.add(new Vector());
					return newEquation_listOfNewNames;
	  			}
	  		
	  			if(v.isComplexExpression()) {
	  				MutablePair<String, String> pair = add_globalQ_4_reaction_globalQDB(reactionName, reaction_row, equation);
					listOfNewNames.add(pair.right);
					newEquation_listOfNewNames.add(pair.left);
					newEquation_listOfNewNames.add(listOfNewNames);
					return newEquation_listOfNewNames;
				} else {
					return null;
				}
			} catch (Exception e) {
				return newEquation_listOfNewNames;
			}
		}
			
	}*/
	
	public static String getName_function_4_reaction(String reactionName, int reaction_row) {
		 String name = reactionName;
		 if(name.length()==0) { name = Integer.toString(reaction_row); }
		 
		 name = Constants.PREFIX_FUN_4_REACTION_NAME +name;
		 return name;
	}
	
	public static String getName_globalQ_4_reaction(String reactionName, int reaction_row) {
		 String name = reactionName;
		 if(name.length()==0) { name = Integer.toString(reaction_row); }
		 
		 name = Constants.PREFIX_GLQ_4_REACTION_NAME +name;
		 return name;
	}
	
	public MutablePair<String,String> add_globalQ_4_reaction_globalQDB(String reactionName, int reaction_nrow, String globalQName, String rateLaw) {
		 MutablePair<String,String> functionCall_globalQDef = new MutablePair();
		
			String name = globalQName;
			if(globalQName == null || globalQName.trim().length() == 0) {
				name = getName_globalQ_4_reaction(reactionName, reaction_nrow);
			}
		
		  try {
				String globalQ = name;
				  
				functionCall_globalQDef.left = globalQ;
				functionCall_globalQDef.right = globalQ + " = " + rateLaw;
				
				
				updateGlobalQ(getNumGlobalQ()+1, globalQ, "0.0", Constants.GlobalQType.ASSIGNMENT.getDescription(), rateLaw, new String());
				
		} catch (Throwable e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)
			e.printStackTrace();
		}
	    return functionCall_globalQDef;
	}
	
	public MutablePair<String,String> add_function_4_reaction_funDB(String reactionName, int reaction_nrow, String functionName, String rateLaw) {
		 MutablePair<String,String> functionCall_functionDef = new MutablePair();
		String name = functionName;
		if(functionName == null || functionName.trim().length() == 0) {
			name = getName_function_4_reaction(reactionName, reaction_nrow);
		}
		
		  try {
				Function f = new Function(name); 
				f.setCompleteFunSignatureInTable(false);
				f.setEquation(CellParsers.reprintExpression_brackets(rateLaw,MainGui.FULL_BRACKET_EXPRESSION), CFunction.UserDefined, 0);
				Vector<String> names = f.getParametersNames();
				
				for(int n = 0; n < names.size(); n++) { 
					f.setParameterRole(names.get(n), CFunctionParameter.VARIABLE  );
					f.setParameterIndex(names.get(n), n);
				}
				
				
				f.setCompleteFunSignatureInTable(true);
				
				String functionCall = f.getCompactedEquation(names);
				  
				  
				functionCall_functionDef.left = functionCall;
				functionCall_functionDef.right = f.printCompleteSignature() + " = " + f.getExpandedEquation(names);
				//if(cfun.getType() != CFunction.PreDefined)  {
				//funDB.addChangeFunction(funDB.userDefinedFun.size(), f);	
				funDB.addChangeFunction(-1, f);	
				funDB.correctRoles(f);
				Vector<Object> paramInEquation = funDB.addMapping(reaction_nrow-1, functionCall, Constants.ReactionType.USER_DEFINED.getDescription());
				for (Object element : paramInEquation) {
					if(element instanceof Exception) throw ((Exception)element);
				}
				
		} catch (Exception e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)
			e.printStackTrace();
		}
		 
		  
		    return functionCall_functionDef;
		
	}
	
	
	private MutablePair<String,String> add_function_4_reaction(CFunctionDB copasiFunDB, CReaction reaction, int i, String rateLaw) {
		
		 String name = reaction.getSBMLId();
		 
		 if(name.length()==0) { name = Integer.toString(i+1); }
		 
		 name = "function_4_reaction_"+name;
		
		   CFunction function = (CFunction)copasiFunDB.createFunction(name,CEvaluationTree.UserDefined);
			 if(function == null) {
				 function =(CFunction)copasiFunDB.findFunction(name);
			 }
			 
			 rateLaw = rateLaw.replace("'", "_pr "); // variable in infix in copasi cannot contain '
			 
			 
		   function.setInfix(rateLaw);
		  function.setReversible(COPASI.TriFalse);
		 
		
		  
		  MutablePair<String, String> funName_newRateLawExpression = new MutablePair<String, String>();
		  
		  funName_newRateLawExpression.left = name;
		  
		  CFunctionParameters variables = function.getVariables();
		  String functionCall = new String(name + "(");
		  int j = 0;
		  for(; j < variables.size(); j++) {
			   if(j==(-1 & 0xffffffffL)) break;
			    CFunctionParameter parame = variables.getParameter(j);
			    parame.setUsage(CFunctionParameter.PARAMETER);
			    functionCall += parame.getObjectName() + ",";
		  }
		  functionCall = functionCall.substring(0,functionCall.length()-1);
		  functionCall += ")";
		  

		  functionCall = functionCall.replace("_pr","'");
		  funName_newRateLawExpression.right = functionCall;
		  
	//	  System.out.println("in add_function_4_reaction functionCall = "+functionCall);
		  
		  
		  try {
				Function f = new Function(name); 
				f.setCompleteFunSignatureInTable(false);
				f.setEquation(function.getInfix(), CFunction.UserDefined, 0);
				Vector<String> names = f.getParametersNames();
				
				for(int n = 0; n < names.size(); n++) {
					int type = (variables.getParameter(function.getVariableIndex(names.get(n)))).getUsage(); 
					if(type==CFunctionParameter.PARAMETER) type = CFunctionParameter.VARIABLE;
					f.setParameterRole(names.get(n), type  );
				}
				for(int n = 0; n < names.size(); n++) {
					f.setParameterIndex(names.get(n), function.getVariableIndex(names.get(n)));
				}
				f.setCompleteFunSignatureInTable(true);
				f.setShow(true);
			//	funDB.addChangeFunction(funDB.userDefinedFun.size(), f);	
				funDB.addChangeFunction(-1, f);	
				funDB.correctRoles(f);
				Vector<Object> paramInEquation = funDB.addMapping(i, functionCall, Constants.ReactionType.USER_DEFINED.getDescription());
				for (Object element : paramInEquation) {
					if(element instanceof Exception) throw ((Exception)element);
				}
				
		} catch (Exception e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)
			e.printStackTrace();
		}
		   return funName_newRateLawExpression;
		
	}
	
	

	CModelValue add_globalQ_4_function(CModel model, CReaction reaction, int index_reaction, int index_param, String actualModelParameterExpression) {
		String name = reaction.getSBMLId();
		 
		 if(name.length()==0) { name = Integer.toString(index_reaction+1); }
		 
		 name = "glq_4_reac_"+name+"_param_"+Integer.toString(index_param+1);
		
		
		CModelValue modelValue = model.createModelValue(name, 0.0);	
		if(modelValue == null) { modelValue = model.getModelValue(name);		}
		
		modelValue.setStatus(CModelValue.ASSIGNMENT);
		
		try {
				MutablePair<String, Boolean> pair = buildCopasiExpression(actualModelParameterExpression,false,false);
			String expr = pair.left;
			System.out.println("qui: "+actualModelParameterExpression + ";expr = "+expr);
			modelValue.setExpression(expr);

		} catch (Exception e) {
			//if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 
				e.printStackTrace();
		}
	
		//CCopasiObject object = modelValue.getObject(new CCopasiObjectName("Reference=InitialValue"));
		//changedObjects.add(object);
		model.compileIfNecessary();
		
		return modelValue;
	}
		
	
	
	
	private void generateKineticLaw(CReaction reaction, int i, Vector expandedReaction, String rateLaw, String kineticType, HashMap<String, Integer> assignedUsage) throws Exception {
		CModel model = copasiDataModel.getModel();
		CChemEq chemEq = reaction.getChemEq();
		chemEq.cleanup();
	
		Vector<String> subs = (Vector)expandedReaction.get(0);
		Vector<String> prod =(Vector)expandedReaction.get(1);
		Vector<String> mod = (Vector)expandedReaction.get(2);
		
		Vector<String> subs_onlyNames = new Vector<String>();
		Vector<String> prod_onlyNames = new Vector<String>();
		Vector<String> mod_onlyNames = new Vector<String>();
		
		for(String element : subs) {
			subs_onlyNames.add(CellParsers.extractMultistateName(extractName(element)));
		}
		
		for(String element : prod) {
			prod_onlyNames.add(CellParsers.extractMultistateName(extractName(element)));
		}
		
		for(String element : mod) {
			mod_onlyNames.add(CellParsers.extractMultistateName(extractName(element)));
		}
		
		double multiplicity = 1.0;
		for(int i1 = 0; i1 < subs.size(); i1++) {
			String s = (String)subs.get(i1);
			multiplicity = extractMultiplicity(s);
			CMetab metab;
			int index_metab = -1;
			String cmp = CellParsers.extractCompartmentLabel(s);
			if(cmp.length() > 0 ) {
				String justName = CellParsers.extractMultistateName(extractName(s)); //extractName separates just the multiplicity
				index_metab = this.findMetabolite(justName, cmp);
			} else {
				String onlyName = extractName(s);
				index_metab = this.findMetabolite(onlyName,null);
			}
			
			if(index_metab==-1) {
				continue; 
			} else {
				metab = model.getMetabolite(index_metab);
			}
			chemEq.addMetabolite(metab.getKey(), multiplicity, CChemEq.SUBSTRATE);
		}

		for(int i1 = 0; i1 < prod.size(); i1++) {
			String s = (String)prod.get(i1);
			CMetab metab;
			multiplicity = extractMultiplicity(s);
			int index_metab = -1;
			String cmp = CellParsers.extractCompartmentLabel(s);
			if(cmp.length() > 0 ) {
				String justName = CellParsers.extractMultistateName(extractName(s)); //extractName separates just the multiplicity
				index_metab = this.findMetabolite(justName, cmp);
			} else {
				String onlyName = extractName(s);
				index_metab = this.findMetabolite(onlyName,null);
			}
			if(index_metab==-1) {
				continue;
			} else {
				metab = model.getMetabolite(index_metab);
			}
			chemEq.addMetabolite(metab.getKey(), multiplicity, CChemEq.PRODUCT);
		}

		for(int i1 = 0; i1 < mod.size(); i1++) {
			String s = (String)mod.get(i1);
			CMetab metab;
			int index_metab = -1;
			String cmp = CellParsers.extractCompartmentLabel(s);
			if(cmp.length() > 0 ) {
				String justName = CellParsers.extractMultistateName(extractName(s)); //extractName separates just the multiplicity
				index_metab = this.findMetabolite(justName, cmp);
			} else {
				String onlyName = extractName(s);
				index_metab = this.findMetabolite(onlyName,null);
			}
			multiplicity = extractMultiplicity(s);
			s = extractName(s);
			if(index_metab==-1) {
				continue;
			} else {
				metab = model.getMetabolite(index_metab);
			}
			chemEq.addMetabolite(metab.getKey(), multiplicity, CChemEq.MODIFIER);
		}
		

		if(rateLaw.trim().length() == 0 || kineticType.length() ==0) {
			return; // copasi undefined reaction rate law
		}
		
		
		boolean foundUserDefFunction = false;
		boolean foundPredefinedFunction = false;
		boolean foundMassAction = false;
		

		if(kineticType.compareTo(Constants.ReactionType.USER_DEFINED.getDescription())==0) { foundUserDefFunction = true; }
		else if(kineticType.compareTo(Constants.ReactionType.PRE_DEFINED.getDescription())==0) { foundPredefinedFunction = true; }
		else if(kineticType.compareTo(Constants.ReactionType.MASS_ACTION.getDescription())==0) {  foundMassAction = true;    	 }

		CFunctionDB copasiFunDB = CCopasiRootContainer.getFunctionList();
		boolean singleFunctionCall = false;
		
		if(foundPredefinedFunction) {
			CFunction val = (CFunction) copasiFunDB.findFunction(MainGui.cleanNamesPredefinedFunctions.get(rateLaw.substring(0,rateLaw.lastIndexOf("("))));
			reaction.setFunction(val);
		}
		else if (foundMassAction) {
			CFunction val = (CFunction)copasiFunDB.findFunction(this.getCompleteNameSuitableFunction("Mass action",subs.size(), prod.size()));
			reaction.setFunction(val);
		}
		else if(foundUserDefFunction) {
			String funName  = new String();
			try{
				InputStream is = new ByteArrayInputStream(rateLaw.getBytes("UTF-8"));
				MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
				SingleFunctionCall root = parser.SingleFunctionCall();
				GetFunctionNameVisitor vis = new GetFunctionNameVisitor();
				root.accept(vis);
				funName  = vis.getFunctionName();
				singleFunctionCall = true;
			} catch (Exception e) {
				singleFunctionCall = false;
				//e.printStackTrace();
			}
			
			if(singleFunctionCall) {
				if(MainGui.cleanNamesPredefinedFunctions.containsKey(funName)) {
					funName = MainGui.cleanNamesPredefinedFunctions.get(funName);
				}
				
				CFunction val = (CFunction)copasiFunDB.findFunction(funName);
			
				val = checkIfFunctionWithVarAlreadyUsedWithOtherRoles(i,assignedUsage, val);
				funName = val.getObjectDisplayName();
				reaction.setFunction(val);
				
			} else {
				MutablePair<String, String> pair = add_function_4_reaction(copasiFunDB, reaction, i, rateLaw);
				funName = pair.left;
				CFunction val = (CFunction)copasiFunDB.findFunction(funName);
				reaction.setFunction(val);
			}
		}
		
		
		
		//System.out.println("Exporting "+rateLaw);
		//System.out.flush();
		if(foundUserDefFunction || foundPredefinedFunction) {
			Vector paramMapping = new Vector();
			paramMapping = (Vector) funDB.getMapping(i);
			Function f = (Function)paramMapping.get(0);
			
			Vector<Integer> paramRoles =  f.getParametersTypes_CFunctionParameter();
			Vector roleVector = new Vector();
			roleVector.add(CFunctionParameter.PARAMETER);
			roleVector.add(CFunctionParameter.SUBSTRATE);
			roleVector.add(CFunctionParameter.PRODUCT);
			roleVector.add(CFunctionParameter.MODIFIER);
			roleVector.add(CFunctionParameter.VOLUME);
			roleVector.add(Constants.ROLE_EXPRESSION);
			roleVector.add(Constants.SITE_FOR_WEIGHT_IN_SUM);
			
			CFunction chosenFun = reaction.getFunction();
			
			
			for(int iii = 1, jjj = 0; iii < paramMapping.size(); iii=iii+2,jjj++) {
				String parameterNameInFunction = (String)paramMapping.get(iii);
				String actualModelParameter = (String)paramMapping.get(iii+1);
				String actualModelParameterWithExtension = new String(actualModelParameter);
				
				MutablePair<String, Vector<String>> nameAndPossibleExtensions = CellParsers.extractNameExtensions(actualModelParameter);
				Vector<String> extensions = nameAndPossibleExtensions.right;
				actualModelParameter = nameAndPossibleExtensions.left;
				
				
				Vector expandedVectorElements = new Vector();
				int role = paramRoles.get(jjj);
				boolean checkAllRoles = false;
				int indexRole = 0;
				
				
				if(role==CFunctionParameter.VARIABLE) { checkAllRoles  = true; role= (Integer) roleVector.get(indexRole);}
				do{
					try {
						switch(role) {
						case CFunctionParameter.PARAMETER:     
							try{
								Double parValue = Double.parseDouble(actualModelParameter);
								reaction.setParameterValue(parameterNameInFunction,parValue.doubleValue());
								assignedUsage.put(chosenFun.getObjectName()+"_"+parameterNameInFunction, CFunctionParameter.FLOAT64);
								checkAllRoles = false;
							} catch(NumberFormatException ex) { //the parameter is not a number but a globalQ
								if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
								int index = this.findGlobalQ(actualModelParameter);
								if(index == -1) throw new NullPointerException();
								
								if(extensions!= null && extensions.size() > 0) {
									String kind = CellParsers.extractKindQuantifier_fromExtensions(extensions);
									if(kind.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())!=0) throw new NullPointerException();
								}
								CModelValue modelValue = model.getModelValue(actualModelParameter);
								int index_in_function = findParamIndex_InCFunction(chosenFun, parameterNameInFunction);
								if(index_in_function == -1) throw new NullPointerException();
								
								reaction.setParameterMapping(index_in_function, modelValue.getKey());
								assignedUsage.put(chosenFun.getObjectName()+"_"+parameterNameInFunction, CFunctionParameter.PARAMETER);
								checkAllRoles = false;
							}
							break;
						case Constants.ROLE_EXPRESSION:
							CModelValue modelValue = add_globalQ_4_function(model, reaction, i, jjj, actualModelParameter);
							int index_in_function = findParamIndex_InCFunction(chosenFun, parameterNameInFunction);
							if(index_in_function == -1) throw new NullPointerException();
							reaction.setParameterMapping(index_in_function, modelValue.getKey());
							assignedUsage.put(chosenFun.getObjectName()+"_"+parameterNameInFunction, CFunctionParameter.PARAMETER);
							checkAllRoles = false;
							break;
						case CFunctionParameter.SUBSTRATE: 
							
							expandedVectorElements.addAll(subs);
							
							int index_metab = this.findMetabolite(actualModelParameter,null);
							if(!subs_onlyNames.contains(actualModelParameter)) { index_metab = -1;}
							boolean realMultistate = false;
							if(index_metab == -1) {
								for(int g = 0; g < expandedVectorElements.size(); g++) {
									String current = (String)(expandedVectorElements.get(g));
									if(current.contains(actualModelParameter)) {
										index_metab = this.findMetabolite(current,null);
										if(!subs_onlyNames.contains(CellParsers.extractMultistateName(current))) { index_metab = -1;}
										if(CellParsers.isMultistateSpeciesName(current)) {
											realMultistate = true;
										}
									}
								}
							}
							if(index_metab == -1) { //it's not a normal multistate species but the one with multiple compartment
								if(CellParsers.isMultistateSpeciesName(actualModelParameter)) {
									String cmp = CellParsers.extractCompartmentLabel(actualModelParameter);
									String justName = CellParsers.extractMultistateName(actualModelParameter);
									index_metab = this.findMetabolite(justName, cmp);
									if(!subs_onlyNames.contains(justName)) { index_metab = -1;}
									if(index_metab != -1) realMultistate = false;
								}
								
							}
							
							if(index_metab == -1) {
								throw new NullPointerException();
							}
							
							  
							CMetab metab = model.getMetabolite(index_metab);
							index_in_function = findParamIndex_InCFunction(chosenFun, parameterNameInFunction);
							if(index_in_function == -1) {
								throw new NullPointerException();
							}
							reaction.setParameterMapping(index_in_function, metab.getKey());
							
							
							
							CFunctionParameters variables = chosenFun.getVariables();
							CFunctionParameter parame = variables.getParameter(index_in_function);
							parame.setUsage(CFunctionParameter.SUBSTRATE);
							assignedUsage.put(chosenFun.getObjectName()+"_"+parameterNameInFunction, CFunctionParameter.SUBSTRATE);
							
							checkAllRoles = false;
							
							if(realMultistate) {
								MSMB_UnsupportedAnnotations multistate_actual = new MSMB_UnsupportedAnnotations(MSMB_UnsupportedAnnotations.MSMB_UnsupportedAnnotations_type.MULTISTATE_TYPE_ACTUAL_PARAMETER, new Integer(index_in_function).toString());
								reaction.addUnsupportedAnnotation(multistate_actual.getName(), multistate_actual.getAnnotation());
							}
							
							
							break;
						case CFunctionParameter.PRODUCT:  
							expandedVectorElements.addAll(prod); 
							index_metab = this.findMetabolite(actualModelParameter,null);
							if(!prod_onlyNames.contains(actualModelParameter)) { index_metab = -1;}
							
							realMultistate = false;
							if(index_metab == -1) {
								for(int g = 0; g < expandedVectorElements.size(); g++) {
									String current = (String)(expandedVectorElements.get(g));
									if(current.contains(actualModelParameter)) {
										index_metab = this.findMetabolite(current,null);
										if(CellParsers.isMultistateSpeciesName(current)) {
											realMultistate = true;
										}
										if(!prod_onlyNames.contains(current)) { index_metab = -1;}
									}
								}
							}
							if(index_metab == -1) { //it's not a normal multistate species but the one with multiple compartment
								if(CellParsers.isMultistateSpeciesName(actualModelParameter)) {
									String cmp = CellParsers.extractCompartmentLabel(actualModelParameter);
									String justName = CellParsers.extractMultistateName(actualModelParameter);
									index_metab = this.findMetabolite(justName, cmp);
									if(!prod_onlyNames.contains(justName)) { index_metab = -1;}
									if(index_metab != -1) realMultistate = false;
								}
								
							}
							
							if(index_metab == -1) { 
								throw new NullPointerException();
							}
							metab = model.getMetabolite(index_metab);
							index_in_function = findParamIndex_InCFunction(chosenFun, parameterNameInFunction);
							 variables = chosenFun.getVariables();
							 parame = variables.getParameter(index_in_function);
							parame.setUsage(CFunctionParameter.PRODUCT);
							
							reaction.setParameterMapping(index_in_function, metab.getKey());
							assignedUsage.put(chosenFun.getObjectName()+"_"+parameterNameInFunction, CFunctionParameter.PRODUCT);
							checkAllRoles = false;
							
							if(realMultistate) {
								MSMB_UnsupportedAnnotations multistate_actual = new MSMB_UnsupportedAnnotations(MSMB_UnsupportedAnnotations.MSMB_UnsupportedAnnotations_type.MULTISTATE_TYPE_ACTUAL_PARAMETER, new Integer(index_in_function).toString());
								reaction.addUnsupportedAnnotation(multistate_actual.getName(), multistate_actual.getAnnotation());
							}
							break;
						case CFunctionParameter.MODIFIER:   
							expandedVectorElements.addAll(mod);
							index_metab = this.findMetabolite(actualModelParameter,null);
							if(!mod_onlyNames.contains(actualModelParameter)) { index_metab = -1;}
							realMultistate = false;
							if(index_metab == -1) {
								for(int g = 0; g < expandedVectorElements.size(); g++) {
									String current = (String)(expandedVectorElements.get(g));
									if(current.contains(actualModelParameter)) {
										index_metab = this.findMetabolite(current,null);
										if(CellParsers.isMultistateSpeciesName(current)) {
											realMultistate = true;
										}
										if(!mod_onlyNames.contains(CellParsers.extractMultistateName(current))) { index_metab = -1;}
										
									}
								}
							}
							if(index_metab == -1) { //it's not a normal multistate species but the one with multiple compartment
								if(CellParsers.isMultistateSpeciesName(actualModelParameter)) {
									String cmp = CellParsers.extractCompartmentLabel(actualModelParameter);
									String justName = CellParsers.extractMultistateName(actualModelParameter);
									index_metab = this.findMetabolite(justName, cmp);
									if(index_metab != -1) realMultistate = false;
									if(!mod_onlyNames.contains(justName)) { index_metab = -1;}
								}
								
							}
							if(index_metab == -1) 
								throw new NullPointerException();
							metab = model.getMetabolite(index_metab);
							index_in_function = findParamIndex_InCFunction(chosenFun, parameterNameInFunction);
							reaction.setParameterMapping(index_in_function, metab.getKey());
							 variables = chosenFun.getVariables();
							 parame = variables.getParameter(index_in_function);
							parame.setUsage(CFunctionParameter.MODIFIER);
							assignedUsage.put(chosenFun.getObjectName()+"_"+parameterNameInFunction, CFunctionParameter.MODIFIER);
							
							checkAllRoles = false;
							if(realMultistate) {
								MSMB_UnsupportedAnnotations multistate_actual = new MSMB_UnsupportedAnnotations(MSMB_UnsupportedAnnotations.MSMB_UnsupportedAnnotations_type.MULTISTATE_TYPE_ACTUAL_PARAMETER, new Integer(index_in_function).toString());
								reaction.addUnsupportedAnnotation(multistate_actual.getName(), multistate_actual.getAnnotation());
							}
							break;
						case CFunctionParameter.VOLUME:    
							int index = this.findCompartment(actualModelParameter);
							if(index == -1) throw new NullPointerException();
							CCompartment comp = model.getCompartment(index);
							reaction.setParameterMapping(findParamIndex_InCFunction(chosenFun, parameterNameInFunction), comp.getKey());
							checkAllRoles = false;
							break;
						case CFunctionParameter.TIME:    
							reaction.setParameterMapping(findParamIndex_InCFunction(chosenFun, parameterNameInFunction), model.getKey());
							break;
						case Constants.SITE_FOR_WEIGHT_IN_SUM:
							Double parValue = null;
						
							String speciesName = nameAndPossibleExtensions.left;
							
							if(extensions.size()!=0) {
								String site = extensions.get(0).substring(1);
								for(int j = 0; j < subs.size(); j++){
									String element1 =subs.get(j).toString();
									element1 = extractName(element1);
									String element1_justName_ifMultistate = element1;
									if(CellParsers.isMultistateSpeciesName(element1)) {
										element1_justName_ifMultistate = CellParsers.extractMultistateName(element1);
									}
									if(speciesName.compareTo(element1_justName_ifMultistate) == 0) {
										MultistateSpecies sp = new MultistateSpecies(this, element1);
										if(sp.getSitesNames().contains(site)) {
											Vector siteValues = sp.getSiteStates_complete(site);
											try{
												parValue = Double.parseDouble(siteValues.get(0).toString());
												reaction.setParameterValue(parameterNameInFunction,parValue.doubleValue());
												checkAllRoles = false;
											} catch(NumberFormatException ex) { //the site has not a numerical value should be an error stopped before
												ex.printStackTrace();
											}
										}
									}
								}
								
								
							} else {
								throw new NullPointerException();
							}
							break;
						default:
							System.out.println("missing parameter role in function, "+chosenFun.getObjectName()+" for actual value " + actualModelParameter);
						}
					} catch(Exception ex) {
						if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 
							ex.printStackTrace();
						if(checkAllRoles==false) throw ex;
						indexRole++;
						try {
							role = (Integer) roleVector.get(indexRole);
						} catch(Exception ex2) {
							if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex2.printStackTrace();
							
							break;
						}
					}
				}while(checkAllRoles || indexRole >=roleVector.size());
			}
		}



		if(foundMassAction) {
			try {
				Double parValue = Double.parseDouble(rateLaw);
				reaction.setParameterValue(reaction.getParameters().getName(0),parValue,true);
			} catch(NumberFormatException ex) { //the parameter is not a number but a globalQ
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
				CModelValue modelValue = model.getModelValue(rateLaw);
				reaction.setParameterMapping(0, modelValue.getKey());
			}
			
			
		} 
		


		//Vector mod_indexes = new Vector();
		//Vector sub_indexes = new Vector();
		//Vector prod_indexes = new Vector();
		int volume_index = -1;
	
		CEvaluationTree f = copasiFunDB.findFunction(reaction.getFunction().getObjectName());
		
		if(foundMassAction) {
			for(int r = 0; r < mod.size(); r++ ) {
				String s = (String)mod.get(r);
				s = extractName(s);
				CMetab metab;
				int index_metab = this.findMetabolite(s,null);
				metab = model.getMetabolite(index_metab);
				//if(mod_indexes.size() > 0 ) reaction.setParameterMapping((Integer) mod_indexes.get(r),  metab.getKey());
				//else 
				reaction.addParameterMapping("modifier",  metab.getKey());
			}
	
			
			
			for(int r = 0; r < subs.size(); r++ ) {
				String s = (String)subs.get(r);
				multiplicity = extractMultiplicity(s);
				s = extractName(s);
				CMetab metab;
				int index_metab = this.findMetabolite(s,null);
				
				if(index_metab == -1) { //it's not a normal multistate species but the one with multiple compartment
					if(CellParsers.isMultistateSpeciesName(s)) {
						String cmp = CellParsers.extractCompartmentLabel(s);
						String justName = CellParsers.extractMultistateName(s);
						index_metab = this.findMetabolite(justName, cmp);
					}
					
				}
				metab = model.getMetabolite(index_metab);
				/*if(sub_indexes.size() > 0 ) {
					reaction.setParameterMapping((Integer) sub_indexes.get(r),  metab.getKey());
				}
				else {
				*/	
					if(multiplicity == Math.floor(multiplicity)){
						for(int i11 = 0; i11 < Math.floor(multiplicity); i11++) {
							reaction.addParameterMapping("substrate",  metab.getKey());
						}
					}
					else {
						reaction.addParameterMapping("substrate",  metab.getKey());
					}	
				//}
			}
			//if(volume_index != -1 ) reaction.setParameterMapping(volume_index,  model.getCompartment(0).getKey());
	
	
			for(int r = 0; r < prod.size(); r++ ) {
				String s = (String)prod.get(r);
				s = extractName(s);
				CMetab metab;
				int index_metab = this.findMetabolite(s,null);
				if(index_metab == -1) { //it's not a normal multistate species but the one with multiple compartment
					if(CellParsers.isMultistateSpeciesName(s)) {
						String cmp = CellParsers.extractCompartmentLabel(s);
						String justName = CellParsers.extractMultistateName(s);
						index_metab = this.findMetabolite(justName, cmp);
					}
					
				}
				metab = model.getMetabolite(index_metab);
				/*if(prod_indexes.size() > 0 ) { 
					reaction.setParameterMapping((Integer) prod_indexes.get(r),  metab.getKey());
				} else */
				reaction.addParameterMapping("product",  metab.getKey());
	
			}
		}
		return;
	}
	
	
	static int sameFunDiffRolesIndex = 1;
	private CFunction checkIfFunctionWithVarAlreadyUsedWithOtherRoles(int row, HashMap<String, Integer> assignedUsage, CFunction val) {
		CFunctionDB copasiFunDB = CCopasiRootContainer.getFunctionList();
		Vector paramMapping = (Vector) funDB.getMapping(row);
		Function f = (Function)paramMapping.get(0);
		
		Vector<Integer> paramRoles =  f.getParametersTypes_CFunctionParameter();
		
		for(int iii = 1, jjj = 0; iii < paramMapping.size(); iii=iii+2,jjj++) {
			String parameterNameInFunction = (String)paramMapping.get(iii);
			String actualModelParameter = (String)paramMapping.get(iii+1);
			
			
			MutablePair<String, Vector<String>> nameAndPossibleExtensions = CellParsers.extractNameExtensions(actualModelParameter);
			Vector<String> extensions = nameAndPossibleExtensions.right;
			actualModelParameter = nameAndPossibleExtensions.left;
			
			int role = paramRoles.get(jjj);
			if(role==CFunctionParameter.VARIABLE) { 
				Integer usedAlreadyAs = assignedUsage.get(f.getName()+"_"+parameterNameInFunction);
				Integer wantToUseAs = new Integer(-1);
				if(usedAlreadyAs != null) {
					//as local parameter
					try{
						Double parValue = Double.parseDouble(actualModelParameter);
						wantToUseAs =CFunctionParameter.FLOAT64;
					}  catch (Exception e) {	}
					//--
					
					//as SPECIES
					if(wantToUseAs == -1) {
						int index_element = -1;
						try {
							index_element = this.findMetabolite(actualModelParameter,null);
						} catch (Exception e) {
							index_element = -1;
						}
						if(index_element != -1) wantToUseAs = CFunctionParameter.SUBSTRATE;
					}
					//--
					
					
					//as GLQ
					if(wantToUseAs == -1) {
						int index_element = -1;
						try {
							index_element = this.findGlobalQ(actualModelParameter);
						} catch (Exception e) {
							index_element = -1;
						}
						if(index_element != -1) wantToUseAs = CFunctionParameter.PARAMETER;
					}
					//--	
					
					if(wantToUseAs != -1 && usedAlreadyAs != wantToUseAs) {
						//I need to generate a new function
						 String newname = val.getObjectName()+"___"+sameFunDiffRolesIndex;
						 sameFunDiffRolesIndex++;
						String previousInfix = val.getInfix();
						 val = (CFunction)copasiFunDB.createFunction(newname,CEvaluationTree.UserDefined);
						  val.setInfix(previousInfix);
						  val.setReversible(COPASI.TriFalse);
						  
						  CFunctionParameters variables = val.getVariables();
						  int j = 0;
						  for(; j < variables.size(); j++) {
							   if(j==(-1 & 0xffffffffL)) break;
							    CFunctionParameter parame = variables.getParameter(j);
							    parame.setUsage(CFunctionParameter.PARAMETER);
						  }
						
					}
						
				}
			}
		}
		
	
		return val;
	}

	public String extractName(String s) {
			
		String ret = null;
		try{
			InputStream is = new ByteArrayInputStream(s.getBytes("UTF-8"));
			MR_ChemicalReaction_Parser react = new MR_ChemicalReaction_Parser(is,"UTF-8");
			CompleteSpeciesWithCoefficient start = react.CompleteSpeciesWithCoefficient();
			ExtractSubProdModVisitor v = new ExtractSubProdModVisitor(this);
		    start.accept(v);
			ret = new String(v.getExtractedName_fromSpeciesWithCoefficient().getBytes("UTF-8"),"UTF-8");
		
		
		}catch (Exception e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)		e.printStackTrace();
		}
		return ret;
	}

	public double extractMultiplicity(String s) {
		
		Double ret = new Double(1.0);
		try{
			InputStream is = new ByteArrayInputStream(s.getBytes("UTF-8"));
			MR_ChemicalReaction_Parser react = new MR_ChemicalReaction_Parser(is,"UTF-8");
			CompleteSpeciesWithCoefficient start = react.CompleteSpeciesWithCoefficient();
			ExtractSubProdModVisitor v = new ExtractSubProdModVisitor(this);
		    start.accept(v);
			ret = v.getExtractedCoeff_fromSpeciesWithCoefficient();
		}catch (Exception e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)
			e.printStackTrace();
		}
		return ret;
	}
	
	public void cleanUpModel() {
		//System.out.println("cleanUpModel()");
		//System.out.flush();
		if(copasiDataModel==null) return;
		
		if(!MainGui.fromInterface) {
			while (CCopasiRootContainer.getDatamodelList().size()!=0) {
				CCopasiRootContainer.removeDatamodelWithIndex(0);
			}
			copasiDataModel = CCopasiRootContainer.addDatamodel();
			copasiDataModel_key = new String(copasiDataModel.getModel().getKey());
		}
		
	/*	copasiDataModel.deleteOldData();
		CModel model = this.copasiDataModel.getModel();
		

		System.out.println("BEFORE: "+model.getModelValues().size());
		System.out.flush();
		
		if(model!=null) {
			model.getMetabolites().cleanup(); //or clear()?
			model.getModelValues().cleanup();//or clear()?
			model.getCompartments().cleanup();//or clear()?
			model.getEvents().cleanup();//or clear()?
			model.getReactions().cleanup();//or clear()?
			//model.cleanup();
		}
		
		
		System.out.println("AFTER: "+model.getModelValues().size());
		System.out.flush();
		*/
		
		
		modified_species.clear();
		allNamedElements.clear();
		speciesDB.clear();
		reactionDB.clear();
		compDB.clear();
		eventsDB.clear();
		funDB.clear();
		globalqDB.clear();
		
		
		System.gc();
	}
	
	static int speciesToBeExported = 0;
	
	private Vector<Species> fillCopasiDataModel_species(Vector<Species> species) throws Throwable {
		if(species.size() == 0) return new Vector<Species> ();
		
		speciesToBeExported = species.size();
		
		CModel model = this.copasiDataModel.getModel();
		HashMap<String, String> annotations = listOfAnnotations.get(Constants.TitlesTabs.SPECIES.getDescription());
		
		Vector<Species> species_with_expression_not_added = new Vector<Species>();
		
		for(int i = 0; i < species.size(); i++) {
			Species s = species.get(i);
			if(s!= null){
				if(s instanceof Species && !(s instanceof MultistateSpecies)) {
					String name = s.getDisplayedName();
					if(name.trim().length()== 0) continue;
					String compartmentList = s.getCompartment_listString();
					//FOR NOW ALL in different compartments HAVE THE SAME INITIAL CONCENTRATION/EXPRESSION... NEEEEEEEEEED TO BE FIIIIIIIIIIIIXED
					Vector<String> compNames = CellParsers.extractNamesInList(this,compartmentList, Constants.TitlesTabs.SPECIES.getDescription(), Constants.SpeciesColumns.COMPARTMENT.getDescription());
										
					for(int ii = 0; ii < compNames.size(); ii++) {
						//double initial_conc = s.getInitialConcentration().doubleValue();
						String comp = compNames.get(ii);
						String initialExpression = new String();
						double initial_conc = 0.0;
						if(MainGui.quantityIsConc) {
							try{
								initial_conc = Double.parseDouble(s.getInitialQuantity().get(ii));
							} catch(Exception ex) {
								if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
								initialExpression = s.getInitialQuantity().get(ii);
							}
						}

						int type = s.getType();
						CMetab m;
						int index_metab = this.findMetabolite(name,comp);
							 
						 if(index_metab == -1) {
							 m = model.createMetabolite(name.replace("\"", ""), comp , initial_conc,  type);
						 }
						else m = model.getMetabolite(index_metab);
										
						String sbmlID = s.getSBMLid().trim();
						if(sbmlID.length()==0)  sbmlID = m.getKey();
						m.setSBMLId(sbmlID);

						
						if(s.getExpression().trim().length() > 0) {
							try {
								MutablePair<String, Boolean> pair = buildCopasiExpression(s.getExpression(),true,false);
								String expr = pair.left;
								if(expr.length()==0) throw new Exception("Problems exporting the expression "+s.getExpression());
								m.setExpression(expr);
							} catch (Throwable e) {
								if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
								species_with_expression_not_added.add(s);
								
							}
						}

						if(!MainGui.quantityIsConc)		{
							try{
								m.setInitialValue(Double.parseDouble(s.getInitialQuantity().get(ii)));
							} catch(Throwable ex) { 
								if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
								initialExpression = s.getInitialQuantity().get(ii);
							}
						}
						if(initialExpression.length()>0 ) {
							if(!CellParsers.isNaN(initialExpression)) {
								try{	
									MutablePair<String, Boolean> pair = buildCopasiExpression(initialExpression, false,true);
									String expr = pair.left;
									//System.out.println("Expression: "+expr);
									m.setInitialExpression(expr);
								} catch(Throwable ex) { 
									if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
									species_with_expression_not_added.add(s);
								}	
							}
							else m.setInitialExpression(Constants.COPASI_STRING_NAN);
						} else {
							m.setInitialConcentration(initial_conc);
						}					
						m.setNotes(s.getNotes());
						if(annotations!= null && annotations.get(name) != null) {
							String annotString = annotations.get(name);
							annotString = fixAnnotation(annotString, m.getKey());
							m.setMiriamAnnotation(annotString, m.getKey(), m.getKey());
						}
					}
				
				} else {

					MultistateSpecies multi = (MultistateSpecies) s;
					
					Vector<Species> expanded = multi.getExpandedSpecies(this);
					//expanded = this.speciesDB.assignInitals_expandedMultistateVector(expanded);

					//CCompartment compartment = model.createCompartment(multi.getCompartment());

					for(int j = 0; j < expanded.size(); j++) {
						Species single = expanded.get(j);
						
						String name = single.getDisplayedName();
						String comp = single.getCompartment_listString();
						double initial_conc = 0.0;
						if(MainGui.quantityIsConc) {
							try{
								String init_q = multi.getInitial_singleConfiguration(single);
								if(init_q!= null) {
									initial_conc = Double.parseDouble(init_q);
								}
							} catch(Throwable ex) {//FOR NOW NO EXPRESSION IN THE MULTISTATE SPECIES
								if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
								throw ex;
							} 
						}
						
						int type = single.getType();

						CMetab m;
						int index_metab = this.findMetabolite(name,null);
						if(index_metab == -1) m = model.createMetabolite(name, comp , initial_conc,  type);
						else m = model.getMetabolite(index_metab);
						
						if(MainGui.quantityIsConc) { 
							m.setInitialConcentration(initial_conc); 
							//object = m.getObject(new CCopasiObjectName("Reference=InitialConcentration"));
						}
						
						double initial_amount = 0.0;
						if(!MainGui.quantityIsConc)		{
							try{
								String init_q = multi.getInitial_singleConfiguration(single);
								if(init_q!= null) {
									initial_amount = Double.parseDouble(init_q);
								}
							} catch(Exception ex) { //FOR NOW NO EXPRESSION IN THE MULTISTATE SPECIES
								if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
								throw ex;
							}
							m.setInitialValue(initial_amount);
							//object = m.getObject(new CCopasiObjectName("Reference=InitialParticleNumber"));
							
						}
						
						
						try {
							MutablePair<String, Boolean> pair = buildCopasiExpression(single.getExpression(),true,false);
							String expr = pair.left;
							m.setExpression(expr);
						} catch (Exception e) {
							if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
							species_with_expression_not_added.add(single);
						}
						m.setNotes(s.getNotes());
						String nameForAnnotation = CellParsers.extractMultistateName(name);
						if(annotations!= null && annotations.get(nameForAnnotation) != null) {
							String annotString = annotations.get(nameForAnnotation);
							annotString = fixAnnotation(annotString, m.getKey());
							m.setMiriamAnnotation(annotString, m.getKey(), m.getKey());
						}
						
						MSMB_UnsupportedAnnotations multistate_annotation = new MSMB_UnsupportedAnnotations(MSMB_UnsupportedAnnotations.MSMB_UnsupportedAnnotations_type.MULTISTATE_SPECIES, multi.printCompleteDefinition());
						m.addUnsupportedAnnotation(multistate_annotation.getName(), multistate_annotation.getAnnotation());
					
						//changedObjects.add(object);
					}
				}
				
				
			}

		}
		
		
		if(species_with_expression_not_added.size() < speciesToBeExported) return fillCopasiDataModel_species(species_with_expression_not_added);
		return species_with_expression_not_added;
		
	}

	private String getCompleteNameSuitableFunction(String string, int numSub, int numProd) {
		CFunctionDB funDB = CCopasiRootContainer.getFunctionList();
		CFunctionStdVector suitableFunctions = funDB.suitableFunctions(numSub, numProd, COPASI.TriFalse);
		int i1 = 0;
		int iMax=(int)suitableFunctions.size();

		for (i1=0;i1<iMax;i1++) {
			if (suitableFunctions.get(i1).getObjectName().indexOf(string) != -1)    { 
				return   suitableFunctions.get(i1).getObjectName();
			}
		}
		return null;
	}

	private int findParamIndex_InCFunction(CFunction chosenFun, String param) {
		CFunctionParameters var = chosenFun.getVariables();
		
		for(int i = 0; i < var.size(); i++) {
			CFunctionParameter par = var.getParameter(i);
			if(par.getObjectName().compareTo(param) == 0) return i; 
		}
		return -1;
	}
	 
	
	private CFunctionDB fillCopasiDataModel_functions() {
		CFunctionDB funDB_copasi = CCopasiRootContainer.getFunctionList();
		if(!MainGui.fromInterface) MainGui.clearCopasiFunctions(); //
		
		Collection<Function> defFunctions = funDB.getAllFunctions();
		 Iterator it2 = defFunctions.iterator();
		 while(it2.hasNext()) {
			 Function fun = (Function)it2.next();
			 if(fun == null) continue;
			 
			
				 CFunction function =(CFunction)funDB_copasi.findFunction(fun.name);
				 if(function == null ) {
					 //if I get a null pointer it means that the function is not there so I have to add
				 //the function in the database. 
			  if(fun.getType()==CFunction.UserDefined ||
				   (fun.getType() == CFunction.PreDefined && 
				      (fun.name.contains(CellParsers.cleanName(Constants.DEFAULT_SUFFIX_COPASI_BACKWARD_REACTION))
				    	||fun.name.contains(CellParsers.cleanName(Constants.DEFAULT_SUFFIX_COPASI_FORWARD_REACTION)) )
				    )
				 ) {
				  
				  String funName = fun.getName();
				  if(funName.startsWith("\"") || funName.endsWith("\"")) funName = funName.substring(1, funName.length()-1);
				  function = (CFunction) funDB_copasi.createFunction(funName,CEvaluationTree.UserDefined);
				  boolean result = function.setInfix(fun.getEquation());
				  function.setReversible(COPASI.TriFalse);
				 function.compile();
				 // function.setNotes(fun.getNotes());
				  CFunctionParameters variables = function.getVariables();
				  
				  Vector orderedParameters = fun.getParametersNames();
				  for(int ord = 0; ord < orderedParameters.size(); ord++) {
					  String nextParam = (String) orderedParameters.get(ord);
					  long index = function.getVariableIndex(nextParam);

					  if(index==(-1)) {//the parameter in the signature but not used in the equation
						  function.addVariable(nextParam, ord);
						  index = function.getVariableIndex(nextParam);
					  }
					  if(index==(-1 & 0xffffffffL)) break;
					  if(ord!=index) {
						  variables.swap(index, ord);
						  index = function.getVariableIndex(nextParam);
					  }
					
					  CFunctionParameter parame = variables.getParameter(index);
				
					  Integer usage = fun.parametesRoles.get(nextParam);
					  
					  if(usage == CFunctionParameter.PARAMETER) {
						  if(nextParam.startsWith("\"") || nextParam.endsWith("\"")) nextParam = nextParam.substring(1, nextParam.length()-1);
						  MSMB_UnsupportedAnnotations glq_annotation = new MSMB_UnsupportedAnnotations(MSMB_UnsupportedAnnotations.MSMB_UnsupportedAnnotations_type.GLQ_PARAMETER_TYPE_IN_CFUNCTION, nextParam);
						  function.addUnsupportedAnnotation(glq_annotation.getName(), glq_annotation.getAnnotation());
					  }
					  
					  if(usage == CFunctionParameter.VARIABLE) usage = CFunctionParameter.PARAMETER;
					  if(usage == Constants.SITE_FOR_WEIGHT_IN_SUM) usage = CFunctionParameter.PARAMETER;
					  
					
					  parame.setUsage(usage);
				
				 }
				  
			  }
			 }
		 }
		 
		
		 return funDB_copasi;
	}
	
	private Vector<GlobalQ> fillCopasiDataModel_globalQ_fixed(Vector<GlobalQ> globalQuantities) {
		CModel model = this.copasiDataModel.getModel();
		Vector<GlobalQ> ret = new Vector<GlobalQ>();
		HashMap<String, String> annotations = listOfAnnotations.get(Constants.TitlesTabs.GLOBALQ.getDescription());
		for(int i = 0; i< globalQuantities.size(); i++) {
			GlobalQ g = globalQuantities.get(i);
			if(g== null) continue;
			if(g.type != CModelValue.FIXED) { 
				//we want to create the global quantity anyway so that the order will be preserved
				CModelValue modelValue = model.createModelValue( g.getName(), new Double(0.0));	
				ret.add(g);
				continue; 
			}
			String name = g.getName();
			CModelValue modelValue = model.createModelValue(name, new Double(0.0));	
			if(modelValue == null) {
				modelValue = model.getModelValue(name);
			}
			
			try{
				Double value = Double.parseDouble(g.getInitialValue());
				modelValue.setInitialValue(value);
			}catch(Exception ex) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
				if(CellParsers.isNaN(g.getInitialValue())) {
					modelValue.setInitialExpression(Constants.COPASI_STRING_NAN);
				} else {
					//modelValue.setInitialExpression(g.getInitialValue());
					MutablePair<String, Boolean> pair;
					try {
						pair = buildCopasiExpression(g.getInitialValue(),false,true);
						modelValue.setInitialExpression(pair.left);
					} catch (Exception e) {
						// expression refer to not yet defined elements
						if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
						ret.add(g);
						continue; 
					}
					
				}
			}	
			
			modelValue.setNotes(g.getNotes());
			if(annotations!= null && annotations.get(name) != null) {
				String annotString = annotations.get(name);
				annotString = fixAnnotation(annotString, modelValue.getKey());
				modelValue.setMiriamAnnotation(annotString, modelValue.getKey(), modelValue.getKey());
			}
		}
		
		
		return ret;
	
	}
	
	private void fillCopasiDataModel_events() throws Exception {
		CModel model = this.copasiDataModel.getModel();
		Iterator it = this.eventsDB.getAllEvents().iterator();
		model.getEvents().cleanup();
		int index_event = 0;
		while(it.hasNext()) {
		
			Event ev = (Event) it.next();
			if(ev== null) { continue;}
			
			if(ev.getTrigger().trim().length() == 0) continue;
			MutablePair<String, Boolean> pair = parseExpressionGlobalQ(ev.getTrigger(),MainGui.exportConcentration,false,false);
			
			String trig = pair.left;
			String name = ev.getName();
			if(name.startsWith("\"")&&name.endsWith("\"")) name = name.substring(1, name.length()-1);
	   		
			int index = this.findEvent(name);
			CEvent event = null;
		
			if(index != -1) event = model.getEvent(index);
			else {
				index_event++;
				if(name.trim().length()==0) name = "event_"+index_event;
				event = model.createEvent(name);
			}

			event.setTriggerExpression(trig);
			if(ev.getExpandActionVolume()!=-1) {
				MSMB_UnsupportedAnnotations expandedVolume = new MSMB_UnsupportedAnnotations(MSMB_UnsupportedAnnotations.MSMB_UnsupportedAnnotations_type.EXPANDED_EVENT, ev.getActionsAsString());
				event.addUnsupportedAnnotation(expandedVolume.getName(), expandedVolume.getAnnotation());
			}
			
			Vector<String> actions = buildCopasiExpressionAssignment(ev.getActions(), ev.getExpandActionVolume());
			
			for(int i = 0; i < actions.size(); i=i+2) {
				CEventAssignment assign = event.createAssignment();
				assign.setTargetKey(actions.get(i));
				assign.setExpression(actions.get(i+1));
			}
			
			String delayAssignment = ev.getDelay();
			if(delayAssignment.length() >0) {
				event.setDelayAssignment(!ev.isDelayAfterCalculation());
				pair = buildCopasiExpression(delayAssignment, false,false);
				event.setDelayExpression(pair.left);
			}
			//index_event++;
			event.setNotes(ev.getNotes());
		}
		//model.compile();
		
	}
	
	
	private Vector<Compartment> fillCopasiDataModel_compartments(Vector<Compartment> compartments) throws Exception {
		if(compartments.size() == 0) return new Vector<Compartment>();
		
		CModel model = this.copasiDataModel.getModel();
		
		Vector<Compartment> comp_with_expression_not_added = new Vector<Compartment>();
		HashMap<String, String> annotations = listOfAnnotations.get(Constants.TitlesTabs.COMPARTMENTS.getDescription());
		
		for(int i = 0; i < compartments.size(); i++) {
			Compartment c = compartments.get(i);
			if(c==null) continue;
			String name = c.getName();
			CCompartment comp = null;
			try{
				Double size = Double.parseDouble(c.getInitialVolume());
				comp = model.createCompartment(name, size);
			} catch(Throwable ex) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
				comp = model.createCompartment(name, 1.0);
				comp.setInitialExpression(c.getInitialVolume());
			}
			
			if(comp == null) { comp = model.getCompartment(this.findCompartment(name)); }
			
		
			
			if(c.getType()!=CCompartment.FIXED) {
				comp.setStatus(c.getType());
				try {
					MutablePair<String, Boolean> pair = buildCopasiExpression(c.getExpression(),false,false);
					String expr = pair.left;
					comp.setExpression(expr);
				} catch(Throwable ex2) {
					if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex2.printStackTrace();
					comp_with_expression_not_added.add(c);
					continue;
				}
				try{
					Double size = Double.parseDouble(c.getInitialVolume());
					comp.setInitialValue(size);
				} catch(Throwable ex) {
					if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
					comp = model.createCompartment(name, 1.0);
					try {
						MutablePair<String, Boolean> pair = buildCopasiExpression(c.getInitialVolume(),false,true);
						comp.setInitialExpression(pair.left);
					} catch(Throwable ex2) {
						if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex2.printStackTrace();
						comp_with_expression_not_added.add(c);
						continue;
					}
					
				}
			}
			
			comp.setNotes(c.getNotes());
			if(annotations!= null && annotations.get(name) != null) {
				String annotString = annotations.get(name);
				annotString = fixAnnotation(annotString, comp.getKey());
				comp.setMiriamAnnotation(annotString, comp.getKey(), comp.getKey());
			}
	
		}

		return comp_with_expression_not_added;
	}
	
	
	
	private Vector<GlobalQ> fillCopasiDataModel_globalQ_assignment_ode(Vector<GlobalQ> globalQ) throws Exception {
		CModel model = this.copasiDataModel.getModel();
		
		int globalQToBeExported = globalQ.size();
		Iterator it = globalQ.iterator();
		
		Vector<GlobalQ> globalQ_with_expression_not_added = new Vector<GlobalQ>();
		HashMap<String, String> annotations = listOfAnnotations.get(Constants.TitlesTabs.GLOBALQ.getDescription());
		
		while(it.hasNext()) {
			 
			GlobalQ g = (GlobalQ) it.next();
			if(g == null || g.getName().trim().length() == 0) {	continue;	}
			CModelValue modelValue = null;
			
			if(g.getType() == CModelValue.FIXED) { 
				//because the initial expression of a fixed glq can depend on non-fixed glq
				modelValue = model.getModelValue(g.getName());
				MutablePair<String, Boolean> pair = buildCopasiExpression(g.getInitialValue(),false,true);
				modelValue.setInitialExpression(pair.left);
				continue; 
			}
			
			Double value = Double.parseDouble(CellParsers.reprintExpression_brackets(g.getInitialValue(),false));
			try{
				modelValue = model.createModelValue(g.getName(), value);	
			}catch(Exception ex) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
				modelValue = model.createModelValue(g.getName(), 0.0);
				if(modelValue == null) { modelValue = model.getModelValue(g.getName());		}
				MutablePair<String, Boolean> pair = buildCopasiExpression(g.getInitialValue(),false,true);
				modelValue.setInitialExpression(pair.left);
			
			}	
				
		
			if(modelValue == null) { modelValue = model.getModelValue(g.getName());		}
			
			if(g.getType() == CModelValue.ODE) { 
				modelValue.setInitialValue(value); 
			}
			modelValue.setStatus(g.getType());
			
			try {
				MutablePair<String, Boolean> pair = buildCopasiExpression(g.getExpression(),false,false);
				String expr = pair.left;
				Boolean containsSum = pair.right;
				if(containsSum) {
					MSMB_UnsupportedAnnotations containsSum_annotation = new MSMB_UnsupportedAnnotations(MSMB_UnsupportedAnnotations.MSMB_UnsupportedAnnotations_type.EXPRESSION_WITH_MULTISTATE_SUM, g.getExpression());
					modelValue.addUnsupportedAnnotation(containsSum_annotation.getName(), containsSum_annotation.getAnnotation());
				}
				modelValue.setExpression(expr);
				
			} catch (Throwable e) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)	e.printStackTrace();
				globalQ_with_expression_not_added.add(g);
				continue;
			}
		
			modelValue.setNotes(g.getNotes());
		
			if(annotations!= null && annotations.get(g.getName()) != null) {
				String annotString = annotations.get(g.getName());
				annotString = fixAnnotation(annotString, modelValue.getKey());
				modelValue.setMiriamAnnotation(annotString, modelValue.getKey(), modelValue.getKey());
			}
		}
		
	
		if(globalQ_with_expression_not_added.size() < globalQToBeExported) fillCopasiDataModel_globalQ_assignment_ode(globalQ_with_expression_not_added);
		return globalQ_with_expression_not_added;
		
	}
	
	
	public String extract_weightFunction_in_SUM(String element) {
		int ind_LastBracket= element.lastIndexOf("(");
		String weightFunctionString = new String();
		if(ind_LastBracket != element.indexOf("(")) {
			int ind_previousComma = (element.substring(0,ind_LastBracket)).lastIndexOf(",");
			weightFunctionString = element.substring(ind_previousComma+1,element.lastIndexOf(")"));
			
		}
		return weightFunctionString;
	}
	
	public MultistateSpecies extract_object_of_SUM(String element) throws Exception {
		String weightFunctionString = extract_weightFunction_in_SUM(element);
		if(weightFunctionString.length() > 0) {
			element = element.substring(0,element.length()- weightFunctionString.length()-2);
		} 
		
		StringTokenizer sum_st = new StringTokenizer(element,"(,)");
		sum_st.nextToken(); //SUM
		String multistate_species_name = sum_st.nextToken();
		//controllare che esista
		String site = new String();
		HashMap<String, Vector<Integer>> sitesSum = new HashMap<String, Vector<Integer>>();
		try {
			while(sum_st.hasMoreTokens()) {
				site = sum_st.nextToken();
				Vector<Integer> limits = new Vector<Integer>();
				try{
					String lower_bound = sum_st.nextToken();
					String upper_bound = sum_st.nextToken();
					limits.add(Integer.parseInt(lower_bound));
					limits.add(Integer.parseInt(upper_bound));
				} catch (NoSuchElementException ex){ //there are no lower-upper bounds --> all the site states
					if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
					MultistateSpecies ms = (MultistateSpecies) this.speciesDB.getSpecies(multistate_species_name);
					Vector states = ms.getSiteStates_complete(site);
					String lower_bound = (String) states.get(0);
					String upper_bound = (String)states.get(states.size()-1);
					limits.add(Integer.parseInt(lower_bound));
					limits.add(Integer.parseInt(upper_bound));
				}
				sitesSum.put(site, limits);
			}
		} catch(NumberFormatException numberEx) { 
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) numberEx.printStackTrace();
			throw new NumberFormatException("Only numerical states can be used as indexes in SUM");
		}
		
		//DEFINISCO NUOVA MULTISTATE CON SOLO I RANGE INDICATI SOPRA E STAMPO EXPANDED NELLA SOMMA
		MultistateSpecies ms = (MultistateSpecies) this.speciesDB.getSpecies(multistate_species_name);
		String complete_string = new String();
		Iterator all_sites = ms.getSitesNames().iterator();
		
		while(all_sites.hasNext()) {
			String name = (String) all_sites.next();
			complete_string += name+"{";
			if(sitesSum.containsKey(name)) {
				//CHEEEEEEEEEEEEEEEEECK IF LOWER E UPPER SONO COERENTI CON LA DEFINIZIONE DEL SITO
				//E CHE IL SITO SIA DEFINITO CON UN RANGE!!! ALTRIMENTI COMPLETE_STRING ORA E' SBAGLIATA
				int lower = sitesSum.get(name).get(0);
				int upper = sitesSum.get(name).get(1);
				for(int i = lower; i < upper; i++) {
					complete_string += i+",";
				}
				complete_string += upper+"}";
			} else {
				Iterator it = ms.getSiteStates_complete(name).iterator();
				while(it.hasNext()) {
					complete_string += it.next()+",";
				}
				complete_string = complete_string.substring(0,complete_string.length()-1);
			}
			complete_string += ";";
		}
		
		complete_string = complete_string.substring(0,complete_string.length()-1);
		
		complete_string = multistate_species_name+"("+ complete_string + ")";
		
		MultistateSpecies reduced = new MultistateSpecies(this,complete_string);
		return reduced;
	}

	static int testcount = 0;
	
	
	MutablePair<String,Boolean> parseExpressionGlobalQ(String expression, boolean conc, boolean expressionInSpecies, boolean isInitialExpression) throws Exception {
		if(expression.length() == 0) return new MutablePair(new String(), false); 
		
		
		//this.copasiDataModel.getModel().compile();
		 
		CModel model = this.copasiDataModel.getModel();
		
		
		InputStream is = new ByteArrayInputStream(expression.getBytes("UTF-8"));
		MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
		CompleteExpression root = parser.CompleteExpression();
			CopasiVisitor vis = new CopasiVisitor(model,this,conc,isInitialExpression);
		try{
			root.accept(vis);
		} catch (Exception e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
		}
		if(vis.getExceptions().size() == 0) {
			String copasiExpr  = vis.getCopasiExpression();
			boolean containsSUM = vis.containsSUM();
			return new MutablePair(copasiExpr, containsSUM);
		} else {
			throw vis.getExceptions().get(0);
		}
		
		
		
	}

	
	
	
	
	private Vector<String> expand_element_weightFunctionCall(int row, MultistateSpecies reduced, String weightFunctionCall, boolean conc) throws Throwable {
		Vector<String> ret = new Vector<String>();
		if(weightFunctionCall.length() == 0) return ret;
		Vector<Species> single_sp = reduced.getExpandedSpecies(this);
		
	    Vector paramMapping = (Vector) funDB.get_mappings_weight_globalQ_withSUM(row, reduced, weightFunctionCall);
		Function f = (Function)paramMapping.get(0);
		Vector<Integer> paramRoles =  f.getParametersTypes_CFunctionParameter();
		
		
	    CModel model = this.copasiDataModel.getModel();
	   
	    for(int i = 0; i < single_sp.size(); i++) {
	    	Vector parameterValues_couple = new Vector();
				
			for(int iii = 1, jjj = 0; iii < paramMapping.size(); iii=iii+2,jjj++) {
				String paramNameInFunction = (String) paramMapping.get(iii);
				String actualValue = (String) paramMapping.get(iii+1);
				switch(paramRoles.get(jjj)) {
					case CFunctionParameter.PARAMETER:     
						try{
							Double parValue = Double.parseDouble(actualValue);
							parameterValues_couple.add(parValue);
						} catch(NumberFormatException ex) { //the parameter is not a number but a globalQ
							if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
							parameterValues_couple.add(actualValue);
						}
						break;
					case Constants.SITE_FOR_WEIGHT_IN_SUM:
						Species single = single_sp.get(i);
						
						if(!CellParsers.isMultistateSpeciesName(single.getDisplayedName())) {
							throw new Exception("Only Multistate speciescan be used in the SUM operator.");
						} 
						
						MultistateSpecies singleM = new MultistateSpecies(this, single.getDisplayedName());
						String val = singleM.getValueOfSite(actualValue);
						try{
							Double siteValue = Double.parseDouble(val);
							parameterValues_couple.add(val);
						} catch(NumberFormatException ex) { //the parameter is not a number but a globalQ
							if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
							throw new Exception("ONLY NUMERICAL SITES CAN BE USED IN WEIGHT FUNCTIONS");
						}
						break;
					default: throw new Exception("WRONG PARAMETER TYPES IN SUM!!!!!");
				}
				
				
			}
			MutablePair<String, Boolean> pair =  parseExpressionGlobalQ(f.getExpandedEquation(parameterValues_couple), conc, false,false);
			String final_expression = pair.left;
					
			ret.add(final_expression);
			
		}
		return ret;
	}

	

	private Vector<String> parseExpressionAssignment(Vector<String> expression, int expansionActionWithVolume) throws Exception {
		// this.copasiDataModel.getModel().compile();
		
		
		 
		Vector<String> target_assignment = new Vector<String>();
		Vector<Vector<String>> originalValue_copasiTerm = new Vector<Vector<String>>();
		
		Vector<CCompartment> compartment_modified = new Vector<CCompartment>();
		
		for(int i = 0; i < expression.size(); i++) {

			String current = expression.get(i);
			int index_equal = current.indexOf("=");
			
			String target =current.substring(0, index_equal).trim();
			
			String assignment = current.substring(index_equal+1).trim();
			
			target = CellParsers.reprintExpression_brackets(target, false);
			assignment = CellParsers.reprintExpression_brackets(assignment, false);
			
			CModel model = this.copasiDataModel.getModel();
			Vector<String> couple = new Vector<String>();
			
	   		if(CellParsers.isMultistateSpeciesName(target)) { //is a species and is multistate
			    			//should be a single state... no : or , allowed...
			    	if(target.contains(":") || target.contains(",")) throw new Exception("Reference of multistate species in expression can only refer to a single state");
			    	MultistateSpecies m = new MultistateSpecies(this,target);
			    	String complete_name = m.printCompleteDefinition();
			    		
			    	//int index_metab = this.findMetabolite(complete_name,null);
			    	int index_metab = -1;
			    	
			    	String cmp = CellParsers.extractCompartmentLabel(complete_name);
					if(cmp.length() > 0 ) {
						String justName = CellParsers.extractMultistateName(complete_name); //extractName separates just the multiplicity
						index_metab = this.findMetabolite(justName, cmp);
					} else {
					
						index_metab = this.findMetabolite(complete_name,null);
					}
					
					CMetab metab = model.getMetabolite(index_metab);
							
					String element_copasiTerm = metab.getKey();
					//metab.getObject(new CCopasiObjectName("Reference=Concentration")).getCN().getString();
					//element_copasiTerm="<"+element_copasiTerm+">";
					couple.add(target);
				    couple.add(element_copasiTerm);
				    originalValue_copasiTerm.add(couple);
			   } else {
				   //int index = this.findMetabolite(target,null);
				   int index = -1;
			    	String cmp = CellParsers.extractCompartmentLabel(target);
					if(cmp.length() > 0 ) {
						String justName = CellParsers.extractMultistateName(target); //extractName separates just the multiplicity
						index = this.findMetabolite(justName, cmp);
					} else {
						
						index = this.findMetabolite(target,null);
					}
					String element_copasiTerm = new String();
					if(index!= -1) { //species
							CMetab metab = model.getMetabolite(index);
							element_copasiTerm = metab.getKey();//metab.getObject(new CCopasiObjectName("Reference=Concentration")).getCN().getString();
					} else { 
							index = this.findGlobalQ(target);
							if(index!= -1) { //parameter
								CModelValue m = model.getModelValue(index);
								element_copasiTerm = m.getKey();//m.getObject(new CCopasiObjectName("Reference=Value")).getCN().getString();
							} else { //compartment
								index = this.findCompartment(target);
								CCompartment comp = model.getCompartment(index);
								compartment_modified.add(comp);
								element_copasiTerm = comp.getKey();//comp.getObject(new CCopasiObjectName("Reference=Volume")).getCN().getString();
							}
					}
					//element_copasiTerm="<"+element_copasiTerm+">";
					couple.add(target);
					couple.add(element_copasiTerm);
					originalValue_copasiTerm.add(couple);
				}
	   		
	   			String final_target = new String();
			
				for(int ii = 0; ii < originalValue_copasiTerm.size(); ii++ ) {
					Vector<String> entry = originalValue_copasiTerm.get(ii);
					String newValue = entry.get(1);
					target = target.replace(entry.get(0), newValue);
					
					int end = target.indexOf(newValue)+newValue.length();
						
					final_target += target.substring(0,end);
					if(end != target.length()) target = target.substring(end);
				}
				
				target_assignment.add(final_target);
				MutablePair<String, Boolean> pair = parseExpressionGlobalQ(assignment, MainGui.exportConcentration, false,false);
				target_assignment.add(pair.left); 
				// always concentration because the events hold the particle number automatically
				originalValue_copasiTerm.clear();
		}
		
		if(expansionActionWithVolume!= -1) {
			for(int i = 0; i < compartment_modified.size(); i++) {
				CCompartment comp = compartment_modified.get(i);
				MetabVectorNS species = comp.getMetabolites();
				for(int j = 0; j < species.size(); j++) {
					org.COPASI.CCopasiObject sp = (org.COPASI.CCopasiObject) species.get(j);
					CCopasiObjectName objectName = null;
					if(expansionActionWithVolume == MR_Expression_ParserConstants.EXTENSION_CONC) { 
						objectName = new CCopasiObjectName("Reference=Concentration");
					}
					else { 
						objectName = new CCopasiObjectName("Reference=ParticleNumber"); 
						}
					String recalculate_conc = "<"+sp.getObject(objectName).getCN().getString()+">";
					target_assignment.add(sp.getKey());
					target_assignment.add(recalculate_conc);
				}
			}
		}
		return target_assignment;
			
	}
	
	//boolean containSUM used to add the unsupported annotation with the compressed version of the expression
	public MutablePair<String, Boolean> buildCopasiExpression(String expression, boolean expressionInSpecies, boolean isInitialExpression) throws Exception {
		return parseExpressionGlobalQ(expression, MainGui.exportConcentration, expressionInSpecies,isInitialExpression); 
	}
	
	
	//boolean containSUM used to add the unsupported annotation with the compressed version of the expression
		MutablePair<String, Boolean> buildCopasiExpression(String expression, MultistateSpecies ms) throws Exception {
			if(expression.length() == 0) return new MutablePair(new String(), false); 
			
			CModel model = this.copasiDataModel.getModel();
			
			InputStream is = new ByteArrayInputStream(expression.getBytes("UTF-8"));
			MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
			CompleteExpression root = parser.CompleteExpression();
			CopasiVisitor vis = new CopasiVisitor(model,this,ms);
			try{
				root.accept(vis);
			} catch (Exception e) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
			}
			if(vis.getExceptions().size() == 0) {
				String copasiExpr  = vis.getCopasiExpression();
				boolean containsSUM = vis.containsSUM();
				return new MutablePair(copasiExpr, containsSUM);
			} else {
				throw vis.getExceptions().get(0);
			}
			
		}
		
		
	private Vector<String> buildCopasiExpressionAssignment(Vector<String> expression, int expansionActionWithVolume) throws Exception {
		return parseExpressionAssignment(expression, expansionActionWithVolume);
	}


	public Vector expandReaction(Vector metabolites, HashMap<String, String> aliases, HashMap<Integer, String> aliases_2, int row) throws Throwable {
		Vector ret = new Vector();
	//	Vector<String> problems = new Vector<String>();
		
		Vector subs = (Vector)metabolites.get(0);
	    Vector prod =(Vector)metabolites.get(1);
		Vector mod = (Vector)metabolites.get(2);
		
			
		Vector non_multistate_react = new Vector();
		Vector non_multistate_mod = new Vector();
		Vector non_multistate_prod = new Vector();
		HashMap<String, Vector<Species>> multistate_name_expansion_react = new HashMap<String, Vector<Species>>();
		HashMap<String, Vector<Species>> multistate_name_expansion_mod = new HashMap<String, Vector<Species>>();
		HashMap<String, String> alias_temp_originalName = new HashMap<String, String>();
		
		String sub_prefix = "SUB_";
		String mod_prefix = "MOD_";
		
		for(int i = 0; i < subs.size(); i++) {
			String species = (String) subs.get(i);
			Species sp = this.getSpecies(species);
			if(!(sp instanceof MultistateSpecies)) {
				non_multistate_react.add(species);
			} else {
					sp = new MultistateSpecies(this, ((MultistateSpecies)sp).printCompleteDefinition());
				if(aliases_2 != null && aliases_2.containsKey(i+1)) {
					sp.setName(aliases_2.get(i+1));
				}
				
				//merge the possibly missing sites/states
				//vv
				MultistateSpecies msp = new MultistateSpecies(this, sp.getDisplayedName());
				//msp.setSitesRangesWithVariables(((MultistateSpecies)sp).getSitesRangesWithVariables());
				MultistateSpecies current = null;
				//e.g. Cdh1(p) (with no range) is used in the reactant, it cannot be used to build a complete multistate species
				//
				 InputStream is;
					try{
						is = new ByteArrayInputStream(species.getBytes("UTF-8"));
						 MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is,"UTF-8");
						 CompleteMultistateSpecies_Operator start = react.CompleteMultistateSpecies_Operator();
						 MultistateSpeciesVisitor v = new MultistateSpeciesVisitor(this,aliases);
						 start.accept(v);
						 String justName = v.getSpeciesName();
						 String newCompleteMultiForMerging = justName;
						 Set<String> sites = v.getAllSites_names();
						 if(sites.size() > 0) 			{
							 newCompleteMultiForMerging +="(";
						 }
						 for(String site : sites) {
							 if(v.getSiteStates_string(site).length() ==0) {
								 newCompleteMultiForMerging += site+"{"+msp.getSiteStates_string(site)+"};";
							 } else newCompleteMultiForMerging += site+"{"+v.getSiteStates_string(site)+"};";
						 }
						if(sites.size() > 0) {
							newCompleteMultiForMerging = newCompleteMultiForMerging.substring(0,newCompleteMultiForMerging.length()-1);
							 newCompleteMultiForMerging += ")";
						}
						 current = new MultistateSpecies(this, newCompleteMultiForMerging);
				
					} catch (Exception e1) {
						current = new MultistateSpecies(this, species);
						
					}
				
				 if(aliases_2 != null  && aliases_2.containsKey(i+1)) {
					 current.setName(aliases_2.get(i+1));
					}
				 
				current.mergeStatesWith_Minimum(msp);
				//^^
			//MultistateSpecies multi = new MultistateSpecies(this,  sub_prefix+current);
		
				/*if(multistate_name_expansion_react.containsKey(multi.getSpeciesName())) {
					String alias =  multi.getSpeciesName() + "_"+i;
					alias_temp_originalName.put(alias, multi.getSpeciesName());
					multi.setName(alias);
				}
				multistate_name_expansion_react.put(multi.getSpeciesName(), multi.getExpandedSpecies(this));*/
				
				current.setName(sub_prefix+current.getSpeciesName());
				if(multistate_name_expansion_react.containsKey(current.getSpeciesName())) {
					String alias =  current.getSpeciesName() + "_"+i;
					alias_temp_originalName.put(alias, current.getSpeciesName());
					current.setName(alias);
				}
				multistate_name_expansion_react.put(current.getSpeciesName(), current.getExpandedSpecies(this));
			}
		}
		
		for(int i = 0; i < mod.size(); i++) {
			String species = (String) mod.get(i);
			Species sp = this.getSpecies(species);
			if(!(sp instanceof MultistateSpecies)) {
				non_multistate_mod.add(species);
			} else {
				try{//try because modifier can contain transfer state and it will be fixed later
				//merge the possibly missing sites/states
				//vv
				MultistateSpecies msp = new MultistateSpecies(this, sp.getDisplayedName());
				MultistateSpecies current = new MultistateSpecies(this, species);
				current.mergeStatesWith_Minimum(msp);
				//^^
				MultistateSpecies multi = new MultistateSpecies(this, mod_prefix+current);
				multistate_name_expansion_mod.put(multi.getSpeciesName(), multi.getExpandedSpecies(this));
				}catch(Throwable e){
					//e.printStackTrace();
					continue;
					
				}
			}
		}
		
		 List<String> keys = new ArrayList<String>(multistate_name_expansion_react.keySet());
		 List<Set<Species>> values = new Vector<Set<Species>>();
		 Iterator react_iterator = multistate_name_expansion_react.keySet().iterator();
		while (react_iterator.hasNext()) {  
			String name = react_iterator.next().toString();  
		    Vector expandedConf = multistate_name_expansion_react.get(name);
		 	Set<Species> values_configurations = Sets.newLinkedHashSet(expandedConf);
		    values.add(values_configurations);
		}
		
		
		keys = new ArrayList<String>(multistate_name_expansion_mod.keySet());
		Iterator mod_iterator = multistate_name_expansion_mod.keySet().iterator();
		while (mod_iterator.hasNext()) {  
			String name = mod_iterator.next().toString();  
		    Vector expandedConf = multistate_name_expansion_mod.get(name);
		 	Set<Species> values_configurations = Sets.newLinkedHashSet(expandedConf);
		    values.add(values_configurations);
		}
		   		    
	    Set<List<Species>> combination = Sets.cartesianProduct(values);
	    
	    for (List<Species> single : combination) {
			  
			//System.out.println("the combination is "+single);
			Vector single_reaction_mod_toCombine = new Vector();
			non_multistate_mod = new Vector();
			
			for(int i = 0; i < mod.size(); i++) {
				String species = (String) mod.get(i);
				Species sp = this.getSpecies(species);
				if(!(sp instanceof MultistateSpecies)) {
					non_multistate_mod.add(species);
				} else {
				  try {
						InputStream is = new ByteArrayInputStream(species.getBytes("UTF-8"));
						MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is,"UTF-8");
						CompleteMultistateSpecies_Operator start = react.CompleteMultistateSpecies_Operator();
						MultistateSpeciesVisitor v = new MultistateSpeciesVisitor(this,single, aliases, sub_prefix);
						start.accept(v);
						Vector<Species> expansion = v.getProductExpansion();
						
						if(expansion.size() == 0) {
							
							try { //check if is a simple multistate species with no operator
								is = new ByteArrayInputStream(species.getBytes("UTF-8"));
								react = new MR_MultistateSpecies_Parser(is,"UTF-8");
								CompleteMultistateSpecies start2 = react.CompleteMultistateSpecies();
								MultistateSpeciesVisitor v2 = new MultistateSpeciesVisitor(null);
								start2.accept(v2);
								non_multistate_mod.add(species); 
							} catch (Throwable e) {
								DebugMessage dm = new DebugMessage();
								dm.setOrigin_table(Constants.TitlesTabs.REACTIONS.getDescription());
								dm.setOrigin_col(Constants.ReactionsColumns.REACTION.index);
								dm.setOrigin_row(row+1);
								Vector elements = new Vector();
								for(Species s : single) {
									String speciesName = s.getDisplayedName();
									 if(speciesName.startsWith(sub_prefix)) {
										 speciesName = speciesName.substring(sub_prefix.length());
									 }
									 elements.add(speciesName);
								}
								dm.setProblem("Problem in the expansion of "+species);
								dm.setPriority(DebugConstants.PriorityType.MISSING.priorityCode);
								MainGui.addDebugMessage_ifNotPresent(dm);
							
							}
							
							
						} else {
							non_multistate_mod.add(expansion.get(0).toString());
						}
					} catch(Throwable e) {
						//e.printStackTrace();
						DebugMessage dm = new DebugMessage();
						dm.setOrigin_table(Constants.TitlesTabs.REACTIONS.getDescription());
						dm.setOrigin_col(Constants.ReactionsColumns.REACTION.index);
						dm.setOrigin_row(row+1);
						Vector elements = new Vector();
						for(Species s : single) {
							String speciesName = s.getDisplayedName();
							 if(speciesName.startsWith(sub_prefix)) {
								 speciesName = speciesName.substring(sub_prefix.length());
							 }
							 elements.add(speciesName);
						}
						dm.setProblem("Problem in the expansion of "+species);
						dm.setPriority(DebugConstants.PriorityType.MISSING.priorityCode);
						MainGui.addDebugMessage_ifNotPresent(dm);
						continue;
					}
					
				}
			}
			    
	    
	    
	   //for (List<Species> single : combination) {
			  
			//System.out.println("the combination is "+single);
			Vector single_reaction_prod_toCombine = new Vector();
			non_multistate_prod = new Vector();
			for(int i = 0; i < prod.size(); i++) {
				String species = (String) prod.get(i);
				Species sp = this.getSpecies(species);
				if(!(sp instanceof MultistateSpecies)) {
					non_multistate_prod.add(species);
				} else {
				  try {
						InputStream is = new ByteArrayInputStream(species.getBytes("UTF-8"));
						MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is,"UTF-8");
						CompleteMultistateSpecies_Operator start = react.CompleteMultistateSpecies_Operator();
						MultistateSpeciesVisitor v = new MultistateSpeciesVisitor(this,single, aliases, sub_prefix);
						start.accept(v);
						Vector<Species> expansion = v.getProductExpansion();
						
						if(expansion.size() == 0) {
							
							try { //check if is a simple multistate species with no operator
								is = new ByteArrayInputStream(species.getBytes("UTF-8"));
								react = new MR_MultistateSpecies_Parser(is,"UTF-8");
								CompleteMultistateSpecies start2 = react.CompleteMultistateSpecies();
								MultistateSpeciesVisitor v2 = new MultistateSpeciesVisitor(null);
								start2.accept(v2);
							 	 non_multistate_prod.add(species); 
							} catch (Throwable e) {
								DebugMessage dm = new DebugMessage();
								dm.setOrigin_table(Constants.TitlesTabs.REACTIONS.getDescription());
								dm.setOrigin_col(Constants.ReactionsColumns.REACTION.index);
								dm.setOrigin_row(row+1);
								Vector elements = new Vector();
								for(Species s : single) {
									String speciesName = s.getDisplayedName();
									 if(speciesName.startsWith(sub_prefix)) {
										 speciesName = speciesName.substring(sub_prefix.length());
									 }
									 elements.add(speciesName);
								}
								dm.setProblem("Problem in the expansion of "+species);
								dm.setPriority(DebugConstants.PriorityType.MISSING.priorityCode);
								MainGui.addDebugMessage_ifNotPresent(dm);
							
							}
							
							
						} else {
							single_reaction_prod_toCombine.add(expansion);
						}
					} catch(Throwable e) {
						//e.printStackTrace();
						DebugMessage dm = new DebugMessage();
						dm.setOrigin_table(Constants.TitlesTabs.REACTIONS.getDescription());
						dm.setOrigin_col(Constants.ReactionsColumns.REACTION.index);
						dm.setOrigin_row(row+1);
						Vector elements = new Vector();
						for(Species s : single) {
							String speciesName = s.getDisplayedName();
							 if(speciesName.startsWith(sub_prefix)) {
								 speciesName = speciesName.substring(sub_prefix.length());
							 }
							 elements.add(speciesName);
						}
						dm.setProblem("Problem in the expansion of "+species);
						dm.setPriority(DebugConstants.PriorityType.MISSING.priorityCode);
						MainGui.addDebugMessage_ifNotPresent(dm);
						continue;
					}
					
				}
			}
			
			values.clear();
			
			for (int i = 0; i< single_reaction_prod_toCombine.size(); ++i) {  
			 	Set<Species> values_configurations = Sets.newLinkedHashSet((Vector)single_reaction_prod_toCombine.get(i));
			    values.add(values_configurations);
			}
			
		
			   		    
		    Set<List<Species>> combination2 = Sets.cartesianProduct(values);
		    
			 if(combination2.size() ==0) { //reaction with no reactants but I can have products (normal species or single states multistate)
			    	non_multistate_prod = new Vector();
					for(int i = 0; i < prod.size(); i++) {
						String species = (String) prod.get(i);
						non_multistate_prod.add(species);
					}
					Vector single_reaction = new Vector();
					single_reaction.add(non_multistate_react);
					single_reaction.add(non_multistate_prod);
					single_reaction.add(non_multistate_mod);
					ret.add(single_reaction);
					
			    }
			 
			for (List<Species> single2 : combination2) {
				Vector single_reaction = new Vector();
				Vector single_reaction_subs = new Vector();
				Vector single_reaction_prod = new Vector();
				Vector single_reaction_mod = new Vector();
					
				//System.out.println("the combination of product is "+single2);
				single_reaction_subs.addAll(non_multistate_react);
				single_reaction_prod.addAll(non_multistate_prod);
				single_reaction_mod.addAll(non_multistate_mod);
			
				Iterator combination_element = single.iterator();
				while(combination_element.hasNext()) {
					Species element = (Species) combination_element.next();
					String which = element.getSpeciesName().substring(0, sub_prefix.length());
					String realName =  element.getSpeciesName();
					
					
					if(alias_temp_originalName.containsKey(CellParsers.extractMultistateName(realName))) {
						try {
							MultistateSpecies temp = new MultistateSpecies(null, realName);
							temp.setName(alias_temp_originalName.get(CellParsers.extractMultistateName(realName)));
							realName = temp.printCompleteDefinition();
						} catch(Exception ex) {
							ex.printStackTrace();
						}
						
					}
					realName =  realName.substring(sub_prefix.length());
					if(aliases.containsKey(CellParsers.extractMultistateName(realName))) {
						try {
							MultistateSpecies temp = new MultistateSpecies(null, realName);
							String fullAliasReference = aliases.get(CellParsers.extractMultistateName(realName));
							temp.setName(CellParsers.extractMultistateName(fullAliasReference));
							realName = temp.printCompleteDefinition();
						} catch(Exception ex) {
							ex.printStackTrace();
						}
					}
					
					
					if(which.compareTo(sub_prefix)==0) {
						single_reaction_subs.add(realName);
					} else {
						single_reaction_mod.add(realName);
					}
				}
				for(Species prodSp : single2) {
					single_reaction_prod.add(prodSp.getSpeciesName());
				}
				
				
		
					single_reaction.add(single_reaction_subs);
					single_reaction.add(single_reaction_prod);
					single_reaction.add(single_reaction_mod);
					ret.add(single_reaction);
				
			}
			
		 	
		}
		
	 	return ret;
	}
	
	
	private Vector getExpandedStatesReactant(String species) throws Throwable {

		Vector<Species> ret = new Vector<Species>();
		MultistateSpecies temp = new MultistateSpecies(this,species,true);
		
		 InputStream is = new ByteArrayInputStream(species.getBytes());
		 MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is);
		 CompleteMultistateSpecies start = react.CompleteMultistateSpecies();
		 MultistateSpeciesVisitor v = new MultistateSpeciesVisitor(this);
	 	 start.accept(v);
	 	 Species existingSp = this.getSpecies(v.getSpeciesName());
	 	 if(existingSp instanceof MultistateSpecies)  {
	 		 MultistateSpecies existingMulti = (MultistateSpecies) existingSp;
			temp.mergeStatesWith_Minimum(existingMulti);
		 } else { 
			 //is multistate because of just compartment so the "temp" should be ok...
		 }
		 
		return temp.getExpandedSpecies(this);

	}

	
	
	/*private void checkSimilarityName(String name, Integer nrow) {
		double level_similarity = 1.0;
	
		Vector<String> names = this.speciesDB.getAllNames();
		
       	for(int i = 0; i < names.size(); i++) {
       		String current = names.get(i);
       	
       		if(debugTab.SimilarityStrings.damlev(current, name)!= 0 &&
       		   debugTab.SimilarityStrings.damlev(current, name) <= level_similarity) {
       			DebugMessage dm = new DebugMessage();
       			//dm.setOrigin_cause("Similarity strings <= "+level_similarity);
       			dm.setOrigin_table(Constants.TitlesTabs.SPECIES.getDescription());
       			dm.setProblem("Species "+ name + " and " + current + " have a degree of similarity lower than "+level_similarity+"\n. Two species have been added but maybe a misstype has occurred in the definition of the two species.");
       			dm.setPriority(DebugConstants.PriorityType.SIMILARITY.priorityCode);
       			dm.setOrigin_col(1);
       			dm.setOrigin_row(nrow);
       			//MainGui.debugMessages.add(dm);
       		}
       	}
		
	}*/
	
	
	public void clearCopasiDataModel() {
		clearCopasiDataModel(false);
	}
	
	public void clearCopasiDataModel(boolean deleteOldDataCopasiDataModel) {
		
		if(CCopasiRootContainer.getDatamodelList().size()==0) {
			copasiDataModel = CCopasiRootContainer.addDatamodel();
			copasiDataModel_key = new String(copasiDataModel.getModel().getKey());
		}
		
		loadCopasiDataModel_fromModelName(copasiDataModel_modelName);
		
		
		if((MainGui.fromMainGuiTest||deleteOldDataCopasiDataModel) && copasiDataModel!= null) {
			try {
					/*if(MainGui.fromInterface) {
						CModel model = copasiDataModel.getModel();
						model.getMetabolites().clear(); // this is WRONG, it does not clear all the datastructure/lists
						
						long total = model.getEvents().size();
						for (long i = total-1; i >= 0; i--)	{
							CEvent  m = (CEvent) model.getEvents().get(i);
							model.removeEvent(m, true);
						}		
					
						total = model.getMetabolites().size();
						for (long i = total-1; i >= 0; i--)	{
							CMetab  m = (CMetab) model.getMetabolites().get(i);
							model.removeMetabolite(m, true);
						}		
						
						total = model.getReactions().size();
						for (long i = total-1; i >= 0; i--)	{
							CReaction  m = (CReaction) model.getReactions().get(i);
							model.removeReaction(m, true);
						}		
						
						total = model.getModelValues().size();
						for (long i = total-1; i >= 0; i--)	{
							CModelValue  m = (CModelValue) model.getModelValues().get(i);
							model.removeModelValue(m, true);
						}		
			
					} */
				
				if(!MainGui.fromInterface) copasiDataModel.newModel();
				
						copasiDataModel.deleteOldData();
						copasiDataModel.getModel().setTimeUnit(MainGui.timeUnit);
						copasiDataModel.getModel().setVolumeUnit(MainGui.volumeUnit);
						copasiDataModel.getModel().setQuantityUnit(MainGui.quantityUnit);
						copasiDataModel.getModel().setObjectName(copasiDataModel_modelName);

										
			} catch (Exception e) {
					e.printStackTrace();
			}
		}
		
		 
	}
	
	public void setUnits(int unitTime, int unitVolume, int unitQuantity) {
		if(copasiDataModel!=null) {
			CModel model = copasiDataModel.getModel(); 
			model.setTimeUnit(unitTime);
			model.setVolumeUnit(unitVolume);
			model.setQuantityUnit(unitQuantity);
		}
		MainGui.timeUnit = unitTime;
		MainGui.volumeUnit = unitVolume;
		MainGui.quantityUnit = unitQuantity;
		 
	}
	
	
	
	public void clear() {
		clearCopasiDataModel();
		speciesDB = new SpeciesDB(this);
		reactionDB= new ReactionDB(this);
		globalqDB = new GlobalQDB(this);
		compDB = new CompartmentsDB(this);
		eventsDB = new EventsDB(this);
		funDB = new FunctionsDB(this);
		allNamedElements.clear();
	}
	
	public void loadCPS(File f) {
		
		if(CCopasiRootContainer.getDatamodelList().size() ==0) {
			copasiDataModel = CCopasiRootContainer.addDatamodel();
			copasiDataModel_key = new String(copasiDataModel.getModel().getKey());
		}
		else {
			//loadCopasiDataModel_fromKey(copasiDataModel_key);
			loadCopasiDataModel_fromModelName(copasiDataModel_modelName);
		}
		
	     try{
	    	 copasiDataModel.loadModel(f.getAbsolutePath());
	    	 
	    	 CModel model = copasiDataModel.getModel();
	    		int numFuncAnnot = model.getNumUnsupportedAnnotations();
	    		if (numFuncAnnot > 0)	{
	    			for (int j = 0; j < numFuncAnnot; j++) {
	    				String name = model.getUnsupportedAnnotationName(j);
	    				if (name.startsWith(MSMB_UnsupportedAnnotations.xmlns)) {
	    					String annotation = model.getUnsupportedAnnotation(j);
	    					if(MSMB_UnsupportedAnnotations.is_getParticleNumber(annotation)) {
	    						 MainGui.quantityIsConc= false;
	    						 MainGui.exportConcentration= false;
	    						
	    					}
	    				} 
	    			}
	    		}
	 		
	     }
	     catch (java.lang.Exception ex)
       {
           System.err.println("Error while loading the model from file named \"" + f.getAbsolutePath() + "\".");
           if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)    
        	   ex.printStackTrace();
           return;
       }
	   
	 }
	
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
	
	public void loadCopasiDataModel_fromModelName(String copasiDataModel_modelName) {
		if(copasiDataModel_modelName== null){
			if(copasiDataModel==null && MainGui.fromMainGuiTest) {
				copasiDataModel = CCopasiRootContainer.get(0);
				copasiDataModel_key = new String(copasiDataModel.getModel().getKey());
				copasiDataModel_modelName = copasiDataModel_key;
			}
		}
		
		/*for (long s = 0; s < CCopasiRootContainer.getDatamodelList().size(); s++) 
		   {
			 copasiDataModel = CCopasiRootContainer.get(s); //CCopasiRootContainer.getDatamodel(s);// 
			 CModel model = copasiDataModel.getModel();
			 if (model.getKey().compareTo(copasiDataModel_key) == 0)	 {
				this.copasiDataModel_key = copasiDataModel_key;
				 break;
				 
			 }
				int numFuncAnnot = model.getNumUnsupportedAnnotations();
	    		if (numFuncAnnot > 0)	{
	    			for (int j = 0; j < numFuncAnnot; j++) {
	    				String name = model.getUnsupportedAnnotationName(j);
	    				if (name.startsWith(MSMB_UnsupportedAnnotations.xmlns)) {
	    					String annotation = model.getUnsupportedAnnotation(j);
	    					if(MSMB_UnsupportedAnnotations.is_getParticleNumber(annotation)) {
	    						 MainGui.quantityIsConc= false;
	    						 MainGui.exportConcentration= false;
	    					}
	    				} 
	    			}
	    		}
		   }*/
		
		try{
			if(copasiDataModel_modelName!= null) {
				copasiDataModel = getCopasiModelFromModelName(copasiDataModel_modelName);
				if(copasiDataModel==null) {
					copasiDataModel = CCopasiRootContainer.addDatamodel();
					copasiDataModel_key = new String(copasiDataModel.getModel().getKey());
					copasiDataModel_modelName = copasiDataModel_key;
				}
				String searchFor = "CN=Root,Model="+copasiDataModel_modelName;
				CObjectInterface whatsit = copasiDataModel.getObject(new CCopasiObjectName(searchFor));
				if (whatsit == null) {
								//System.out.println("dont have a: " + searchFor);
				} else	{
							CModel mod = (CModel)whatsit.toObject();
							 CModel model = copasiDataModel.getModel();
							this.copasiDataModel_key = model.getKey();
				}
			}
		}catch(Throwable t) {
			t.printStackTrace();
		}
		
	}

	public CModel getCurrentCModel() {
		if(copasiDataModel == null) return null;
		return copasiDataModel.getModel();
	}
	
	
	/*public void modifySBMLid(String newSBML_ID) {
		copasiDataModelSBML_ID = new String(newSBML_ID);
	}*/

	
	public void createNewModel(String modelName) {
		copasiDataModel = CCopasiRootContainer.addDatamodel();
		copasiDataModel_key = new String(copasiDataModel.getModel().getKey());
		copasiDataModel.getModel().setSBMLId(copasiDataModel_key);
		copasiDataModel.getModel().setTimeUnit(CModel.s);
		copasiDataModel.getModel().setVolumeUnit(CModel.fl);
		copasiDataModel.getModel().setQuantityUnit(CModel.number);
		copasiDataModel.getModel().setObjectName(modelName);
	}
	
	
	
	
	
	public void loadSBML(File f) {
	
		if(CCopasiRootContainer.getDatamodelList().size() ==0) {
			copasiDataModel = CCopasiRootContainer.addDatamodel();
			copasiDataModel_key = new String(copasiDataModel.getModel().getKey());
		}
		else {
			//loadCopasiDataModel_fromKey(copasiDataModel_key);
			loadCopasiDataModel_fromModelName(copasiDataModel_modelName);
		}
		
		try{
	    	 copasiDataModel.importSBML(f.getAbsolutePath());
	       }
	     catch (java.lang.Exception ex)
      {
          System.err.println("Error while loading the model from file named \"" + f.getAbsolutePath() + "\".");
          if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)           ex.printStackTrace();
          return;
      }
		
	}
	
	public Vector loadGlobalQTable_fromMultimodel() {
		Vector rows = new Vector();
		Vector<GlobalQ> glqs = getAllGlobalQ();
        for (int i = 0;i < glqs.size();i++)
        {
        	Vector row = new Vector();
    		GlobalQ val = glqs.get(i);
    		if(val==null) continue;
           String newName = val.getName();
            row.add(newName);
//------------------------------------------------------------------
            row.add(val.getInitialValue());
//------------------------------------------------------------------
            row.add(Constants.GlobalQType.getDescriptionFromCopasiType(val.getType()));
//------------------------------------------------------------------
           row.add(val.getExpression());
//------------------------------------------------------------------
            row.add(val.getNotes());
            rows.add(row);
        }
        return rows;
	}
	
	public Vector loadReactionsTable_fromMultimodel() {
		Vector rows = new Vector();
		Vector<Reaction> elements = getAllReactions();
        for (int i = 0;i < elements.size();i++)
        {
        	Vector row = new Vector();
    		Reaction val = elements.get(i);
    		if(val==null) continue;
           String newName = val.getName();
            row.add(newName);
//------------------------------------------------------------------
            row.add(val.getReactionString());
//------------------------------------------------------------------
            row.add(Constants.ReactionType.getDescriptionFromCopasiType(val.getType()));
//------------------------------------------------------------------
            row.add(val.getRateLaw());
          //------------------------------------------------------------------
            row.add(val.getNotes());
            rows.add(row);
        }
        return rows;
	}
	

	
	public Vector loadFunctionsTable_fromMultimodel() {
		Vector rows = new Vector();
		Vector<Function> elements = getAllUserDefinedFunctins();
        for (int i = 0;i < elements.size();i++)
        {
        	Vector row = new Vector();
        	Function val = elements.get(i);
    		if(val==null) continue;
           String newName = val.getName();
            row.add(newName);
//------------------------------------------------------------------
            row.add(val.getEquation());
//------------------------------------------------------------------
            row.add(val.getNotes());
            rows.add(row);
        }
        return rows;
	}
	
	
	public Vector loadEventsTable_fromMultimodel() {
		Vector rows = new Vector();
		Vector<Event> elements = getAllEvents();
        for (int i = 0;i < elements.size();i++)
        {
        	Vector row = new Vector();
        	Event val = elements.get(i);
    		if(val==null) continue;
           String newName = val.getName();
            row.add(newName);
		//------------------------------------------------------------------
		    row.add(val.getTrigger());
		  //------------------------------------------------------------------
		 row.add(val.getActionsAsString());
           //------------------------------------------------------------------
		    row.add(val.getDelay());
	            //------------------------------------------------------------------
		row.add(new Boolean(val.delayAfterCalculation).toString()); 
		//------------------------------------------------------------------
		row.add(val.getNotes()); 
		//------------------------------------------------------------------
		    row.add(new Boolean(val.expandActionVolume==-1).toString());
		  rows.add(row);
                  }
        
        return rows;
	}
	
	
	public Vector loadSpeciesTable_fromMultimodel() {
		Vector rows = new Vector();
		Vector<Species> elements = getAllSpecies();
        for (int i = 0;i < elements.size();i++)
        {
        	Vector row = new Vector();
    		Species val = elements.get(i);
    		if(val==null) continue;
           String newName = val.getName();
            row.add(newName);
//------------------------------------------------------------------
            row.add(val.getInitialQuantity_listString());
//------------------------------------------------------------------
            row.add(Constants.SpeciesType.getDescriptionFromCopasiType(val.getType()));
//------------------------------------------------------------------
            row.add(val.getCompartment_listString());
          //------------------------------------------------------------------
           row.add(val.getExpression());
//------------------------------------------------------------------
            row.add(val.getNotes());
            rows.add(row);
        }
        return rows;
	}
	
	public Vector loadCompartmentsTable_fromMultimodel() {
		Vector rows = new Vector();
		Vector<Compartment> elements = getAllCompartments();
        for (int i = 0;i < elements.size();i++)
        {
        	Vector row = new Vector();
        	Compartment val = elements.get(i);
    		if(val==null) continue;
           String newName = val.getName();
            row.add(newName);
//------------------------------------------------------------------
            row.add(Constants.CompartmentsType.getDescriptionFromCopasiType(val.getType()));
//------------------------------------------------------------------
            row.add(val.getInitialVolume());
//------------------------------------------------------------------
             row.add(val.getExpression());
//------------------------------------------------------------------
            row.add(val.getNotes());
            rows.add(row);
        }
        return rows;
	}
	
	
	public Vector loadGlobalQTable() {
		if(copasiDataModel == null) return new Vector();
		CModel model = copasiDataModel.getModel();
		if(model == null) return  new Vector();
		
		Vector rows = new Vector();
		    
        int iMax = (int)model.getModelValues().size();
        
        for (int i = 0;i < iMax;i++)
        {
       	Vector row = new Vector();
    		CModelValue val = model.getModelValue(i);
            
    		String expressionFromAnnotation = new String();
    		  int numFuncAnnot = val.getNumUnsupportedAnnotations();
  			if (numFuncAnnot > 0)	{
  				for (int j = 0; j < numFuncAnnot; j++) {
  					String name = val.getUnsupportedAnnotationName(j);
  					if (name.startsWith(MSMB_UnsupportedAnnotations.xmlns)) {
  						String annotation = val.getUnsupportedAnnotation(j);
  						if(MSMB_UnsupportedAnnotations.is_ExpressionWithMultistateSum(annotation)) {
  						 expressionFromAnnotation = MSMB_UnsupportedAnnotations.extractAnnotationValue(annotation);
  						}
  					} 
  				}
  			}
  			
    		String newName = CellParsers.cleanName(val.getObjectName());
            row.add(newName);
            if(newName!= val.getObjectName()) val.setObjectName(newName);
//------------------------------------------------------------------
            row.add(val.getInitialValue());
//------------------------------------------------------------------
            row.add(Constants.GlobalQType.getDescriptionFromCopasiType(val.getStatus()));
//------------------------------------------------------------------
            if(		val.getStatus() == CModelValue.ASSIGNMENT || 
            		val.getStatus() == CModelValue.ODE) {
                  	if(expressionFromAnnotation.length() > 0) {
                  		row.add(expressionFromAnnotation);
                  	}
                  	else {
                  		row.add(CellParsers.cleanMathematicalExpression(this.buildMRExpression_fromCopasiExpr(val.getExpression())));
                  	}
            } else row.add("");
//------------------------------------------------------------------
            row.add(val.getNotes());
           rows.add(row);
        }
        
        return rows;
		
	}
	

	
	public Vector loadEventsTable() {
		if(copasiDataModel == null) return new Vector();
		CModel model = copasiDataModel.getModel();
		if(model == null) return  new Vector();
		
		Vector rows = new Vector();
		    
        int iMax = (int)model.getEvents().size();
        
        
        for (int i=0; i<iMax; i++)
        {
        	Vector row = new Vector();
        	CEvent val = model.getEvent(i);
    		
            row.add(val.getObjectName());
//------------------------------------------------------------------
            row.add(this.buildMRExpression_fromCopasiExpr(val.getTriggerExpression()));
//------------------------------------------------------------------
            boolean isExpandedEvent = false;
            String compactedEvent = new String();
        	int numFuncAnnot = val.getNumUnsupportedAnnotations();
    		if (numFuncAnnot > 0)	{
    			for (int j = 0; j < numFuncAnnot; j++) {
    				String name = val.getUnsupportedAnnotationName(j);
    				if (name.startsWith(MSMB_UnsupportedAnnotations.xmlns)) {
    					String annotation = val.getUnsupportedAnnotation(j);
    					if(MSMB_UnsupportedAnnotations.is_ExpandedEvent(annotation)) {
    						compactedEvent = MSMB_UnsupportedAnnotations.extractAnnotationValue(annotation);
    						isExpandedEvent = true;
    					}
    				} 
    			}
    		}
    		if(!isExpandedEvent) {
    			row.add(this.buildMRAssigments_fromCopasiExpr(val));
            	//------------------------------------------------------------------
    		}  else {
	        	 row.add(compactedEvent);
	         }
	            row.add(this.buildMRExpression_fromCopasiExpr(val.getDelayExpression()));
	            //------------------------------------------------------------------
	            row.add(new Boolean(!val.getDelayAssignment()).toString()); 
	            //------------------------------------------------------------------
	            row.add(val.getNotes()); 
	            //------------------------------------------------------------------
	            row.add(new Boolean(isExpandedEvent).toString());
            rows.add(row);
        }
        
        return rows;
		
	}

	
	public Vector loadSpeciesTable() {
		
		HashSet<String> multistateSpeciesAdded = new HashSet<String>();
		HashMap<String, HashMap<String, String>> multistateSingleConf_InitialQuantity = new HashMap<String, HashMap<String, String>>();
				
				
		if(copasiDataModel == null) return new Vector();
		
		CModel model = copasiDataModel.getModel();
		if(model == null) return  new Vector();
		
		MainGui.loadedExisting = true;
		
		MainGui.timeUnit = model.getTimeUnitEnum();
		MainGui.volumeUnit = model.getVolumeUnitEnum();
		MainGui.quantityUnit = model.getQuantityUnitEnum();
		setModelName(model.getObjectName());
		
		model.setTimeUnit(Constants.UnitTypeTime.DIMENSIONLESS_TIME.copasiType);
		model.setVolumeUnit(Constants.UnitTypeVolume.DIMENSIONLESS_VOL.copasiType);
		model.setQuantityUnit(Constants.UnitTypeQuantity.DIMENSIONLESS_QUANTITY.copasiType);
		
		Vector rows = new Vector();
		HashMap<Long, String> SBML_IDS = new HashMap<Long, String>();
		
        int iMax = (int)model.getMetabolites().size();
        HashMap<String, Integer> namesCollected_index = new HashMap<String, Integer>();
        
        for (int i = 0;i < iMax;i++)
        {
        	
        	Vector row = new Vector();
            CMetab metab = model.getMetabolite(i);
            
            String cleanName = CellParsers.cleanName(metab.getObjectName(),true);

            int numFuncAnnot = metab.getNumUnsupportedAnnotations();
			if (numFuncAnnot > 0)	{
				for (int j = 0; j < numFuncAnnot; j++) {
					String name = metab.getUnsupportedAnnotationName(j);
					if (name.startsWith(MSMB_UnsupportedAnnotations.xmlns)) {
						String annotation = metab.getUnsupportedAnnotation(j);
						if(MSMB_UnsupportedAnnotations.is_MultistateSpecies(annotation)) {
								String parName = MSMB_UnsupportedAnnotations.extractAnnotationValue(annotation);
								cleanName = parName;
								if(multistateSpeciesAdded.contains(cleanName)) {
									break;
								} 
						}
					} 
				}
			
			}
        		 
			
						 
            if(!namesCollected_index.containsKey(cleanName) && !multistateSpeciesAdded.contains(cleanName)) {
               
            	row.add(cleanName);
	            SBML_IDS.put((long) i+1,metab.getSBMLId());
	      
	            if(MainGui.quantityIsConc) row.add(new Double(metab.getInitialConcentration()).toString());
	           else row.add(new Double(metab.getInitialValue()).toString());
	           
	           if(metab.getInitialExpression().trim().length()>0) {
	        	   row.set(row.size()-1, buildMRExpression_fromCopasiExpr(metab.getInitialExpression()));
	           }
	            
	           if(CellParsers.isMultistateSpeciesName(cleanName)) row.add(Constants.SpeciesType.MULTISTATE.getDescription());
	           else row.add(Constants.SpeciesType.getDescriptionFromCopasiType(metab.getStatus()));
	            
	            
	            String cleanNameComp = CellParsers.cleanName(metab.getCompartment().getObjectName(),false);
	            row.add(cleanNameComp);
	            
	          
	            
	            if(metab.getStatus() == CMetab.ASSIGNMENT || metab.getStatus() == CMetab.ODE) {
	            	row.add(CellParsers.cleanMathematicalExpression(this.buildMRExpression_fromCopasiExpr(metab.getExpression())));
	            } else row.add("");
	            
	            
	       	
	            row.add(metab.getNotes());
	            namesCollected_index.put(cleanName, i);
	            if(CellParsers.isMultistateSpeciesName(cleanName)) multistateSpeciesAdded.add(cleanName);
	        	String multistateSingleConfigName = CellParsers.cleanName(metab.getObjectName(),true);
        	    String init_quantity = "0.0";
        	      if(MainGui.quantityIsConc) init_quantity = new Double(metab.getInitialConcentration()).toString();
   	           else init_quantity = new Double(metab.getInitialValue()).toString();
        	      
        	      String justName = CellParsers.extractMultistateName(multistateSingleConfigName);
        	      HashMap<String, String> currentConfig= null;
        	      if(multistateSingleConf_InitialQuantity.containsKey(justName)) {
        	    	  currentConfig = multistateSingleConf_InitialQuantity.get(justName);
        	      } else {
        	    	  currentConfig =  new  HashMap<String, String> ();
        	      }
        		  currentConfig.put(multistateSingleConfigName, init_quantity);
        	      
        	      multistateSingleConf_InitialQuantity.put(justName, currentConfig);
        	      
	           rows.add(row);
        	} else if(multistateSpeciesAdded.contains(cleanName)) { //I have to collect the initial value to store it for later
        	    String multistateSingleConfigName = CellParsers.cleanName(metab.getObjectName(),true);
        	    String init_quantity = "0.0";
        	      if(MainGui.quantityIsConc) init_quantity = new Double(metab.getInitialConcentration()).toString();
   	           else init_quantity = new Double(metab.getInitialValue()).toString();
        	      
        	      String justName = CellParsers.extractMultistateName(multistateSingleConfigName);
        	      HashMap<String, String> currentConfig= null;
        	      if(multistateSingleConf_InitialQuantity.containsKey(justName)) {
        	    	  currentConfig = multistateSingleConf_InitialQuantity.get(justName);
        	      } else {
        	    	  currentConfig =  new  HashMap<String, String> ();
        	      }
        		  currentConfig.put(multistateSingleConfigName, init_quantity);
        	      
        	      multistateSingleConf_InitialQuantity.put(justName, currentConfig);
        	      
        	} else { //the species already exist in another compartment, I have to add the compartment and other data to the same row as the previously added one
        		
        		//if the only difference is the initial quantity, I can compress the two species on a single row
        		//if the type/expression is also different, I will create another species with another name 
        		//(because otherwise it would conflict with our standard interpretation of a multistate species:
        		//we allow different states to only differ in initial condition, not the general behavior (type/expression))
        		
        		Integer index = namesCollected_index.get(cleanName);
        		
        		CMetab oldMetab = model.getMetabolite(index);
        		
        		if(metab.getStatus()!= oldMetab.getStatus()) {
        			 cleanName = CellParsers.cleanName(metab.getObjectName()+"_"+metab.getCompartment().getObjectName(),true);
        			 row.add(cleanName);
      	            SBML_IDS.put((long) i+1,metab.getSBMLId());
      	            if(MainGui.quantityIsConc) row.add(new Double(metab.getInitialConcentration()).toString());
      	           else row.add(new Double(metab.getInitialValue()).toString());
      	           if(metab.getInitialExpression().trim().length()>0) {
      	        	   row.set(row.size()-1, buildMRExpression_fromCopasiExpr(metab.getInitialExpression()));
      	           }
      	            row.add(Constants.SpeciesType.getDescriptionFromCopasiType(metab.getStatus()));
      	            String cleanNameComp = CellParsers.cleanName(metab.getCompartment().getObjectName(),false);
      	            row.add(cleanNameComp);
      	            if(metab.getStatus() == CMetab.ASSIGNMENT || metab.getStatus() == CMetab.ODE) {
      	            	row.add(CellParsers.cleanMathematicalExpression(this.buildMRExpression_fromCopasiExpr(metab.getExpression())));
      	            } else row.add("");
      	            row.add(metab.getNotes());
      	            namesCollected_index.put(cleanName, i);
      	            rows.add(row);	
        			modified_species.put(i,cleanName);
        		} else {
        		
        			row = (Vector) rows.get(index);
	        		
	        		//SBML_IDS.put((long) i+1,metab.getSBMLId());
	        		
	        		String initialExpression = new String();
	        		
	        		if(MainGui.quantityIsConc) initialExpression = new Double(metab.getInitialConcentration()).toString();
	 	            else initialExpression = new Double(metab.getInitialValue()).toString();
	 	            
	        		if(metab.getInitialExpression().trim().length()>0) {
	        			initialExpression = buildMRExpression_fromCopasiExpr(metab.getInitialExpression());
	 	           	}
	 	            
	 	            String cleanNameComp = CellParsers.cleanName(metab.getCompartment().getObjectName(),false);
	 	            String oldComp = (String) row.get(Constants.SpeciesColumns.COMPARTMENT.index-1);
	 	            
	 	            row.set(Constants.SpeciesColumns.COMPARTMENT.index-1, oldComp+", "+cleanNameComp);
	 	     
	 	            String oldInitial = (String) row.get(Constants.SpeciesColumns.INITIAL_QUANTITY.index-1);
	 	 	        row.set(Constants.SpeciesColumns.INITIAL_QUANTITY.index-1, oldInitial+", "+initialExpression);
	 	  	     
	 	           // if(metab.getStatus() == CMetab.ASSIGNMENT || metab.getStatus() == CMetab.ODE) {
	 	          //  	row.add(CellParsers.cleanMathematicalExpression(this.buildMRExpression_fromCopasiExpr(metab.getExpression())));
	 	          //  } else row.add("");
	 	            rows.set(index, row);
        		}
        	}
        }
        
        Vector ret = new Vector();
        ret.add(rows);
        ret.add(SBML_IDS);
        ret.add(new Vector(Arrays.asList(modified_species.values().toArray())));
        ret.add(multistateSingleConf_InitialQuantity);
        
          
        return ret;
	}
	
	public Vector loadReactionTable(Vector<String> reactionWithProblems) throws Exception {
		//added parameter because the "forced" irreversible should change the build-it function reference from name (reversible) to name (irreversible) (that should be available, otherwise no import of the entire model)
		
		if(copasiDataModel == null) return new Vector();
		CModel model = copasiDataModel.getModel();
		if(model == null) return  new Vector();
				
		Vector rows = new Vector();
		Vector row = new Vector();
        Vector<CFunction> predefined_to_be_loaded = new Vector<CFunction>();
        
    	CFunctionDB copasiFunDB = CCopasiRootContainer.getFunctionList();
    	HashSet<String> multistateReactionAdded = new HashSet<String>();
		
		int iMax = (int)model.getReactions().size();
        
        for (int i = 0;i < iMax;i++)
        {
        	CReaction reaction = model.getReaction(i);
            if(reaction.isReversible()) {
            	System.out.println("!!!!!!-----REVERSIBLE REACTION NOT IMPORTED-----");
            	System.out.println(reaction.getObjectName());
               	System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
               	continue;
            }
            
            String reactionName = CellParsers.cleanName(reaction.getObjectName());
            String r_string = new String();
            
            HashSet <String> multistateActualParameters = new   HashSet <String>();
           String kineticLawFromAnnotation = new String();
            
            int numFuncAnnot = reaction.getNumUnsupportedAnnotations();
			if (numFuncAnnot > 0)	{
				for (int j = 0; j < numFuncAnnot; j++) {
					String name = reaction.getUnsupportedAnnotationName(j);
					if (name.startsWith(MSMB_UnsupportedAnnotations.xmlns)) {
						String annotation = reaction.getUnsupportedAnnotation(j);
						if(MSMB_UnsupportedAnnotations.is_ReactionEmptyName(annotation)) {
							reactionName = "";
						} else 	if(MSMB_UnsupportedAnnotations.is_MultistateActualParameter(annotation)) {
							multistateActualParameters.add(MSMB_UnsupportedAnnotations.extractAnnotationValue(annotation));
						} else 	if(MSMB_UnsupportedAnnotations.is_MultistateReaction(annotation)) {
							r_string = MSMB_UnsupportedAnnotations.extractAnnotationValue(annotation);
							if(multistateReactionAdded.contains(r_string)) {
								break;
							} 
						} else 	if(MSMB_UnsupportedAnnotations.is_MultistateReactionRateLaw(annotation)) {
							kineticLawFromAnnotation = MSMB_UnsupportedAnnotations.extractAnnotationValue(annotation);
						}
						
					} 
				}
			}
			
		 if(r_string.trim().length() == 0 || !multistateReactionAdded.contains(r_string)) {
            
            row.add(reactionName);
             
            
            if(r_string.trim().length() == 0) { //if not empty, the correct full reaction string should already be in the annotation
            	//------------------------------------------------------------------
		            CChemEq eq = reaction.getChemEq();
		
		            //--undefined reaction
		            if(eq.getSubstrates().size() == 0 && eq.getProducts().size() == 0) {	 
		            	rows.add(new Vector(row));
		            	row.clear(); continue;    
		            }
		            //--  
		
		         
		            long nsub = eq.getSubstrates().size();
		            for(int ii = 0; ii < nsub; ii++) {
		            	CChemEqElement sub = eq.getSubstrate(ii);
		            	Double mult = sub.getMultiplicity();
		            	if(mult!=1.0) {r_string += mult.doubleValue() + " * ";}
		            	
		            	long metabIndex = findMetabolite(sub.getMetabolite().getObjectName(), sub.getMetabolite().getCompartment().getObjectName());
		            	String cleanName = null;
		            	if(modified_species.containsKey(new Long(metabIndex).intValue())) {
		            		 cleanName = modified_species.get(new Long(metabIndex).intValue());
		            	} else {
		            		cleanName = CellParsers.cleanName(sub.getMetabolite().getObjectName(),true);
		            	}
		            	
		            	if(isSpeciesWithMultipleCompartment(cleanName)) {
		            		cleanName = addCompartmentLabel(cleanName, sub.getMetabolite().getCompartment().getObjectName());
		            	}
		            	r_string += cleanName  + " + ";
		            }
		            if(r_string.length() > 3) r_string = r_string.substring(0, r_string.length()-3);
		
		            r_string += " -> ";
		
		            long nprod = eq.getProducts().size();
		            boolean products = false;
		            for(int ii = 0; ii < nprod; ii++) {
		            	products = true;
		            	CChemEqElement sp = eq.getProduct(ii);
		            	Double mult = sp.getMultiplicity();
		            	if(mult!=1.0) {r_string += mult.doubleValue() + " * ";}
		            	
		            	long metabIndex = findMetabolite(sp.getMetabolite().getObjectName(), sp.getMetabolite().getCompartment().getObjectName());
		            	String cleanName = null;
		            	if(modified_species.containsKey(new Long(metabIndex).intValue())) {
		            		 cleanName = modified_species.get(new Long(metabIndex).intValue());
		            	} else {
		            		cleanName = CellParsers.cleanName(sp.getMetabolite().getObjectName(),true);
		            	}
		            	
		            	if(isSpeciesWithMultipleCompartment(cleanName)) {
		            		cleanName = addCompartmentLabel(cleanName, sp.getMetabolite().getCompartment().getObjectName());
		            	}
		            	r_string += cleanName + " + ";
		            }
		            if(r_string.length() > 3 && products== true) r_string = r_string.substring(0, r_string.length()-3);
		
		
		            long nmod = eq.getModifiers().size();
		            boolean modifiers = false;
		            if(nmod != 0) {
		            	r_string += "; ";
		            }
		            for(int ii = 0; ii < nmod; ii++) {
		            	modifiers = true;
		            	CChemEqElement sp = eq.getModifier(ii);
		            	long metabIndex = findMetabolite(sp.getMetabolite().getObjectName(), sp.getMetabolite().getCompartment().getObjectName());
		            	String cleanName = null;
		            	if(modified_species.containsKey(new Long(metabIndex).intValue())) {
		            		 cleanName = modified_species.get(new Long(metabIndex).intValue());
		            	} else {
		            		cleanName = CellParsers.cleanName(sp.getMetabolite().getObjectName(),true);
		            	}
		            	if(isSpeciesWithMultipleCompartment(cleanName)) {
		            		cleanName = addCompartmentLabel(cleanName, sp.getMetabolite().getCompartment().getObjectName());
		            	}
		            	r_string +=  cleanName + " ";
		            }
		            if(r_string.length() > 3 && modifiers== true) r_string = r_string.substring(0, r_string.length()-1);
            } else {
            	multistateReactionAdded.add(r_string);
            }
            
            row.add(r_string.trim());
            if(reaction.getFunction().getObjectName().contains("undefined")) { 
            	rows.add(new Vector(row));
            	row.clear();
            	continue;
            }
//------------------------------------------------------------------
           row.add(Constants.ReactionType.getDescriptionFromCopasiType(reaction.getFunction().getType()));
           
//------------------------------------------------------------------
           
           String unquoted = reactionName;
           if(unquoted.startsWith("\"")&&unquoted.endsWith("\"")) unquoted = unquoted.substring(1, reactionName.length()-1);
   		
           if(reactionWithProblems.size() > 0 && reactionWithProblems.contains(unquoted)){
        	   String irrFunName = reaction.getFunction().getObjectName();
        	   irrFunName = irrFunName.replace(" (reversible)",  " (irreversible)");
        	   CFunction val = (CFunction)copasiFunDB.findFunction(irrFunName);
				if(val!= null)reaction.setFunction(val);
           }
           
           if(kineticLawFromAnnotation.trim().length() > 0) {
        	   row.add(kineticLawFromAnnotation);
           } else {
        	     row.add(buildMRKineticLaw_fromCopasiFunction(reaction, multistateActualParameters));
           }
         
          
           if(reaction.getFunction().getType() == CFunction.PreDefined ||
        		   reaction.getFunction().getObjectName().contains(Constants.DEFAULT_SUFFIX_COPASI_BACKWARD_REACTION) ||
        		   reaction.getFunction().getObjectName().contains(Constants.DEFAULT_SUFFIX_COPASI_FORWARD_REACTION)) {
        	   predefined_to_be_loaded.add(reaction.getFunction());
           }
           row.add("");
           row.add(reaction.getNotes());
                      
           rows.add(new Vector(row));
            
           row.clear();
		 }
        }
       
        
        
        rows.add(predefined_to_be_loaded);
        return rows;
 	}
	
	private String addCompartmentLabel(String cleanName, String compartmentName) {
		return CellParsers.addCompartmentLabel(cleanName, compartmentName);
	}

	private boolean isSpeciesWithMultipleCompartment(String cleanName) {
		return  this.speciesDB.isSpeciesWithMultipleCompartment(cleanName);
	}

	private String buildMRKineticLaw_fromCopasiFunction(CReaction reaction, HashSet<String> multistateActualParams) throws Exception {
			return buildMRKineticLaw_fromCopasiFunction(reaction, false,multistateActualParams);
	}
	
	private String buildMRKineticLaw_fromCopasiFunction(CReaction reaction, boolean isBackward, HashSet<String> multistateActualParams) throws Exception {
		CModel model = copasiDataModel.getModel();
		String ret = new String();
		if(reaction.getFunction().getType() == CFunction.MassAction) {
			int index = findGlobalQ_key(reaction.getParameterMapping(reaction.getParameters().getName(0)).get(0));
			if(index != -1) {
				CModelValue val = model.getModelValue(index);
				ret +=CellParsers.cleanName(val.getObjectName());
			} else {
				ret += reaction.getParameters().getParameter(0).getDblValue();
			}
			
			return ret;
		}
		
		
		
		ret += CellParsers.cleanName(reaction.getFunction().getObjectName());
		if(isBackward) ret += Constants.DEFAULT_SUFFIX_BACKWARD_REACTION;
		ret += "(";
		VectorOfStringVectors param = reaction.getParameterMappings();
		CCopasiParameterGroup group = reaction.getParameters(); //local constants
		
		for(int i = 0; i < param.size(); i++) {
			String currentKey = param.get(i).get(0);
			CCopasiObject val = null;
			if(currentKey.contains("Value")) {
				int index = findGlobalQ_key(currentKey);
				if(index != -1) {
					val = model.getModelValue(index);
					ret += CellParsers.cleanName(val.getObjectName()) + ",";
				} else {
					ret += reaction.getParameters().getParameter(i).getDblValue() + ",";
				}
			} else if(currentKey.contains("Metabolite")){
				
				int index = findMetabolite_key(currentKey);
				val = model.getMetabolite(index);
				String complete_name_species = val.getObjectName();
				String element = new String();
				if(modified_species.containsKey(new Long(index).intValue())) {
					element = modified_species.get(new Long(index).intValue());
            	} else {
	            	if(multistateActualParams.contains(new Integer(i).toString())) {
            			element = complete_name_species;
            		}
            		else element = CellParsers.cleanName(complete_name_species);
					if(isSpeciesWithMultipleCompartment(element)) {
						element = addCompartmentLabel(element, model.getMetabolite(index).getCompartment().getObjectName());
	            	}
            	}
				ret +=  element + ",";
			} else if(currentKey.contains("Compartment")) {
				int index = findCompartment_key(currentKey);
				val = model.getCompartment(index);
				ret += CellParsers.cleanName(val.getObjectName()) + ",";
			} else if(currentKey.contains("Parameter")) { 
				int index = findLocalParameter_key(currentKey, group);
				CCopasiParameter parameter = group.getParameter(index);
				ret += parameter.getDblValue() + ",";
			}else if(currentKey.contains("Model")) { //model time
				ret += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.CONST_MODEL_TIME) + ",";
			}
			else {
				System.out.println("PARAM NOT IMPORTED: "+currentKey);
			}
			
			
		}
		if(param.size()>0) ret = ret.substring(0, ret.length()-1);
		ret += ")";
		return ret;
	}

		
	public Vector loadCompartmentsTable() {
		if(copasiDataModel == null) return new Vector();
		CModel model = copasiDataModel.getModel();
		if(model == null) return  new Vector();
				
		
		Vector rows = new Vector();
		
		int iMax = (int)model.getCompartments().size();
        
        for (int i = 0;i < iMax;i++)
        {
   		Vector row = new Vector();
         	CCompartment val = model.getCompartment(i);
         	String newName = CellParsers.cleanName(val.getObjectName());
            if(newName!= val.getObjectName()) val.setObjectName(newName);
            
            row.add(newName);
//------------------------------------------------------------------
           row.add(Constants.CompartmentsType.getDescriptionFromCopasiType(val.getStatus()));
//------------------------------------------------------------------
            row.add(new String(Double.toString(val.getInitialValue())));
//------------------------------------------------------------------
            row.add(buildMRExpression_fromCopasiExpr(val.getExpression()));
//------------------------------------------------------------------
            row.add(val.getNotes());
            rows.add(row);
        }
        
       return rows;
		
	}
	
	
	
	private String buildMRExpression_fromCopasiExpr(String expression) {
		//example = <CN=Root,Model=New Model,Vector=Values[alpha],Reference=Value>*<CN=Root,Model=New Model,Vector=Compartments[cell],Reference=Volume>
		
		String ret = new String();
		if(expression.trim().length() ==0) return ret;
	
		expression = CellParsers.replaceIfNotBetweenQuotes(expression, " < ", " lt ");
		expression = CellParsers.replaceIfNotBetweenQuotes(expression, " > ", " gt ");
		expression = CellParsers.replaceIfNotBetweenQuotes(expression, " <= ", " le ");
		expression = CellParsers.replaceIfNotBetweenQuotes(expression, " >= ", " ge ");
		expression = CellParsers.replaceIfNotBetweenQuotes(expression, " == ", " eq ");
		expression = CellParsers.replaceIfNotBetweenQuotes(expression, " && ", " and ");
		expression = CellParsers.replaceIfNotBetweenQuotes(expression, " || ", " or ");
		
	/*	expression = expression.replace(" < ", " lt ");
		expression = expression.replace(" > ", " gt ");
		expression = expression.replace(" <= ", " le ");
		expression = expression.replace(" >= ", " ge ");
		//expression = expression.replace(" == ", " eq ");
		expression = expression.replace(" && ", " and ");
		expression = expression.replace(" || ", " or ");*/
		 
			
		StringTokenizer st_elem = new StringTokenizer(expression, "<>");
		while(st_elem.hasMoreTokens()) {
			String elem = st_elem.nextToken();
			elem = elem.replace("\\,", "\\*MY*COMMA");
			int whichElement = -1;
			if(elem.contains("[")) {
				StringTokenizer st_vector = new StringTokenizer(elem,",");
				String real_elem = new String();
				String real_elem2 = new String();
				int open_br = 0;
				int closed_br = 0;
				boolean first = true;
				
				while(st_vector.hasMoreTokens()) {
					//for Metabolites there are 2 "Vector=[]": the first is the compartment the second the metabolite... 
					String sub_elem = st_vector.nextToken();
					//for Metabolites there are 2 "Vector=[]": the first is the compartment the second the metabolite... 
					if(sub_elem.contains("Vector") && !first) {
						real_elem2 = new String(sub_elem);
						real_elem2 = real_elem2.replace('\\', '\u00A3');
						real_elem2 = real_elem2.replaceAll("\u00A3", "");
						open_br = real_elem2.indexOf("[");
						closed_br = real_elem2.lastIndexOf("]");
						if(real_elem2.contains("Metabolite")) whichElement = Constants.TitlesTabs.SPECIES.index;
						real_elem2 = real_elem2.substring(open_br+1,closed_br);
						real_elem2 = real_elem2.replace("*MY*COMMA", ",");
						
						if(whichElement == Constants.TitlesTabs.SPECIES.index) {
							try {
								int index = findMetabolite(real_elem2,real_elem);
								if(modified_species.containsKey(new Long(index).intValue())) {
									real_elem2 = modified_species.get(new Long(index).intValue());
				            	} else {
				            		real_elem2 = CellParsers.cleanName(real_elem2,true);
				            	}
							} catch (Exception e) {
								if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)			e.printStackTrace();
							}
						
						}
						else real_elem2 = CellParsers.cleanName(real_elem2);
						
						real_elem = addCompartmentLabel(real_elem2, real_elem);
					}
					
					if(sub_elem.contains("Vector") && first) {
						real_elem = new String(sub_elem);
						real_elem = real_elem.replace('\\', '\u00A3');
						real_elem = real_elem.replaceAll("\u00A3", "");
						open_br = real_elem.indexOf("[");
						closed_br = real_elem.lastIndexOf("]");
						if(real_elem.contains("Metabolite")) whichElement = Constants.TitlesTabs.SPECIES.index;
						else if(real_elem.contains("Values")) whichElement = Constants.TitlesTabs.GLOBALQ.index;
						else if(real_elem.contains("Compartments")) whichElement = Constants.TitlesTabs.COMPARTMENTS.index;
						else if(real_elem.contains("Reaction")) whichElement = Constants.TitlesTabs.REACTIONS.index;
						real_elem = real_elem.substring(open_br+1,closed_br);
						real_elem = real_elem.replace("*MY*COMMA", ",");
						if(whichElement == Constants.TitlesTabs.SPECIES.index) real_elem = CellParsers.cleanName(real_elem,true);
						else real_elem = CellParsers.cleanName(real_elem);
						first = false;
					}
				}
				
					
				st_vector = new StringTokenizer(elem,",");
				elem = elem.replace("\\,", "\\*MY*COMMA");
				String ref = new String();
				while(st_vector.hasMoreTokens()) {
					 ref = st_vector.nextToken();
					if(ref.contains("Reference")) {
							ref = ref.substring(ref.indexOf("=")+1);
							if(whichElement == Constants.TitlesTabs.SPECIES.index) {
								real_elem += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_SPECIES);
								if(ref.compareTo("Concentration")==0) real_elem +=  MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_CONC);
								else if(ref.compareTo("ParticleNumber")==0) real_elem += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_PARTICLE);
								else if(ref.compareTo("Rate")==0) real_elem += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_RATE);
								else if(ref.compareTo("InitialConcentration")==0) real_elem += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_CONC)+MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_INIT);
								else if(ref.compareTo("InitialParticleNumber")==0) real_elem += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_PARTICLE)+MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_INIT);
							} else 	if(whichElement == Constants.TitlesTabs.GLOBALQ.index) {
								real_elem += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_GLOBALQ);
								if(ref.compareTo("Value")==0) real_elem += "";
								else if(ref.compareTo("InitialValue")==0) real_elem += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_INIT);
								else if(ref.compareTo("Rate")==0) real_elem += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_RATE);
							} else 	if(whichElement == Constants.TitlesTabs.COMPARTMENTS.index) {
								real_elem += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_COMPARTMENT);
								if(ref.compareTo("Volume")==0) real_elem += "";
								else if(ref.compareTo("InitialVolume")==0) real_elem += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_INIT);
								else if(ref.compareTo("Rate")==0) real_elem += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_RATE);
							} else 	if(whichElement == Constants.TitlesTabs.REACTIONS.index) {
								real_elem += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_REACTION);
								if(ref.compareTo("Flux")==0) real_elem +=  MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_FLUX);
								else real_elem += "SUFFIX_NOT_SUPPORTED";
							}
					}		
				}
				real_elem = real_elem.replace("*MY*COMMA", ",");
				
				ret += real_elem;
				
				
		
			} else {
				if(elem.contains(Constants.COPASI_STRING_TIME) || elem.contains(Constants.COPASI_STRING_INITIAL_TIME)){
						ret+= MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TIME);
				}
				else if(elem.contains(Constants.COPASI_STRING_AVOGADRO)){
					ret+= MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.CONST_AVOGADRO);
				}
				else ret += elem; //operator or parenthesis
			}
		} 
		
		
		ret = CellParsers.replaceIfNotBetweenQuotes(ret, " eq ", " == ");
		ret = CellParsers.replaceIfNotBetweenQuotes(ret, " lt ", " < ");
		ret = CellParsers.replaceIfNotBetweenQuotes(ret, " gt ", " > ");
		ret = CellParsers.replaceIfNotBetweenQuotes(ret, " le ", " <= ");
		ret = CellParsers.replaceIfNotBetweenQuotes(ret, " ne ", " != ");
		ret = CellParsers.replaceIfNotBetweenQuotes(ret, " ge ", " >= ");
		ret = CellParsers.replaceIfNotBetweenQuotes(ret, " and ", " && ");
		ret = CellParsers.replaceIfNotBetweenQuotes(ret, " or ", " || ");
		/*ret = ret.replace(" lt ", " < ");
		ret = ret.replace(" gt ", " > ");
		ret = ret.replace(" le ", " <= ");
		ret = ret.replace(" ge ", " >= ");
		ret = ret.replace(" eq ", " == ");
		ret = ret.replace(" and ", " && ");
		ret = ret.replace(" or ", " || ");*/
		
		try {
			ret = reprintExpression(ret, false);
		} catch (Throwable e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 
				e.printStackTrace();
		}
		
		
		return ret;
	}
	
	
	
	public String reprintExpression(String expr, boolean forceExpansion_elements) throws Throwable {
		String ret = new String();
		try{
			
			InputStream is = new ByteArrayInputStream(expr.getBytes("UTF-8"));
			MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
			CompleteExpression root = parser.CompleteExpression();
			MainGui.jListFunctionToCompact.setSelectionInterval(0, MainGui.listModel_FunctionToCompact.size()-1);
			
			ExpressionVisitor vis = new ExpressionVisitor(Arrays.asList(MainGui.jListFunctionToCompact.getSelectedValues()),this,forceExpansion_elements);
			
			root.accept(vis);
			if(vis.getExceptions().size() == 0) {
				ret  = vis.getExpression();
				ret  = CellParsers.reprintExpression_brackets(ret, MainGui.FULL_BRACKET_EXPRESSION); 
				
			} else {
				throw vis.getExceptions().get(0);
			}
		} catch (Throwable e) {
			throw e;
		}
		return ret;
	}
	
	public String reprintExpression_forceCompressionElements(String expr,String table, String column) throws Throwable {
		return reprintExpression_forceCompressionElements(expr, null, table, column);
	}
	
	public String reprintExpression_forceCompressionElements(String expressionList, String listSeparator, String table, String column) throws Throwable {
		String ret = new String();
		if(expressionList.trim().length() ==0) return ret;
		Vector<Throwable> exceptions = new Vector<Throwable>();
	
		/*Vector<String> elements = new Vector<String>();
			if(listSeparator!=null) {
			StringTokenizer st = new StringTokenizer(expressionList, listSeparator);
			while(st.hasMoreTokens())  {
				elements.add(st.nextToken().trim());
			}
		} else {
			//simple expressions with just a single element
			elements.add(expressionList);
		}
		 */
		
		Vector<String> elements = new Vector<String>();
		
		elements = CellParsers.extractElementsInList(this,expressionList, table, column);
		
			
		for (String expr : elements) {
			try{
				String current = new String();
						
				InputStream is = new ByteArrayInputStream(expr.getBytes("UTF-8"));
				MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
				CompleteExpression root = parser.CompleteExpression();
				MainGui.jListFunctionToCompact.setSelectionInterval(0, MainGui.listModel_FunctionToCompact.size()-1);

				ArrayList list = new ArrayList();
				list.addAll(Arrays.asList(MainGui.jListFunctionToCompact.getSelectedValues()));
				list.add("SUM");
				ExpressionVisitor vis = new ExpressionVisitor(list, this,false);

				root.accept(vis);
				if(vis.getExceptions().size() == 0) {
					
					current  = vis.getCompressedExpression();
						
					current  = CellParsers.reprintExpression_brackets(current, MainGui.FULL_BRACKET_EXPRESSION); 
				} else {
					exceptions.add(vis.getExceptions().get(0));
				}
				
				ret += current;
				if(listSeparator!= null) ret += listSeparator + " ";
			} catch (Exception e) {
				exceptions.add(e);
			}
		}
		
		if(exceptions.size() > 0) {
			throw exceptions.get(0);
		} else {
			if(listSeparator!= null) ret = ret.substring(0, ret.length()-listSeparator.length()-1);
			return ret;
		}
	}
	
	
	
	private String buildMRAssigments_fromCopasiExpr(CEvent event) {
		
		String ret = new String();
		CModel model = copasiDataModel.getModel();
		long assignSize = event.getAssignments().size();
		for(int i = 0; i < assignSize; i++) {
			CEventAssignment element = (CEventAssignment) event.getAssignment(i);
			try { //IT CAN BE SOMETHING DIFFERENT FROM A METAAAAAAAAAAAAAAAAAAAAAAAAAAAABOLITE
					//TO BE FIIIIIIIIIIIIIIIIIIIIIIIIIIIIXED
				
				int index = this.findMetabolite(element.getTargetKey(),null,true);
				
				if(index!= -1) { //species
					if(modified_species.containsKey(new Long(index).intValue())) {
						ret += modified_species.get(new Long(index).intValue());
	            	} else {
	            		ret += CellParsers.cleanName(model.getMetabolite(index).getObjectName(),true);
	            	}
		    	} else { 
						index = this.findGlobalQ(element.getTargetKey(),true);
						if(index!= -1) { //parameter
							ret += CellParsers.cleanName(model.getModelValue(index).getObjectName(),false);
						} else { //compartment
							index = this.findCompartment(element.getTargetKey(),true);
							ret += CellParsers.cleanName(model.getCompartment(index).getObjectName(),false);
						}
				}
			} catch (Exception e) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
			}
			ret += "="+this.buildMRExpression_fromCopasiExpr(element.getExpression());
			ret += "; ";
		}
		
		ret = ret.substring(0, ret.length()-2);
		return ret;
	}

	private void compressReactions() {
		return;
	}
	
	public void compressSpecies() throws Throwable {
	
		this.speciesDB.compressSpecies();
		
		for(int i = 1; i < this.speciesDB.getSizeSpeciesDB(); i++) {
			Species sp = speciesDB.getSpecies(i);
			if(sp instanceof ComplexSpecies) continue;
			
			if(!(sp instanceof MultistateSpecies)) {
				this.updateSpecies(i, sp.getDisplayedName(), 
										//sp.getInitialConcentration(), 
										//sp.getInitialAmount(), 
										sp.getInitialQuantity_listString(), 
										(String)Constants.SpeciesType.getDescriptionFromCopasiType(sp.getType()),
										sp.getCompartment_listString(),
										sp.getExpression(), false, sp.getNotes(),true);
			} else {
				this.updateSpeciesMultistate(i, sp.getDisplayedName(), 
						((MultistateSpecies)sp).getInitialQuantity_multi(), 
						(String)Constants.SpeciesType.getDescriptionFromCopasiType(sp.getType()),
						sp.getCompartment_listString(),
						sp.getExpression(), false, sp.getNotes(),true);
			}
		}
		
		
	
	}
		
	public void removeSpecies(Vector species_default_for_dialog_window) throws Throwable {
			this.speciesDB.removeSpecies(species_default_for_dialog_window);
	}

	public boolean updateGlobalQ(Integer nrow, String name, String value, String type, String expression, String notes) throws Throwable {
		try{ 
			this.addGlobalQ(nrow,name, value, type, expression, notes);
		} catch(MySyntaxException ex) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
		    DebugMessage dm = new DebugMessage();
			dm.setOrigin_table(Constants.TitlesTabs.GLOBALQ.getDescription());
		    dm.setOrigin_col(ex.getColumn());
		    dm.setOrigin_row(nrow);
			dm.setProblem(ex.getMessage());
		    dm.setPriority(DebugConstants.PriorityType.PARSING.priorityCode);
			MainGui.addDebugMessage_ifNotPresent(dm);
			throw ex;
		}
		
		/*if(CellParsers.isNaN(expression)) {
			  DebugMessage dm = new DebugMessage();
				dm.setOrigin_table(Constants.TitlesTabs.GLOBALQ.getDescription());
			    dm.setOrigin_col(Constants.GlobalQColumns.EXPRESSION.index);
			    dm.setOrigin_row(nrow);
				dm.setProblem("NaN value - will be exported as (undefined) in the SBML/Copasi model");
			    dm.setPriority(DebugConstants.PriorityType.MINOR.priorityCode);
				MainGui.addDebugMessage_ifNotPresent(dm);
		} else {*/
			MainGui.clear_debugMessages_relatedWith_table_col_priority(Constants.TitlesTabs.GLOBALQ.getDescription(), Constants.GlobalQColumns.EXPRESSION.index, DebugConstants.PriorityType.MINOR.priorityCode);
		//}
		
		
	/*	if(CellParsers.isNaN(value)) {
			  DebugMessage dm = new DebugMessage();
				dm.setOrigin_table(Constants.TitlesTabs.GLOBALQ.getDescription());
			    dm.setOrigin_col(Constants.GlobalQColumns.VALUE.index);
			    dm.setOrigin_row(nrow);
				dm.setProblem("NaN value - will be exported as () in the SBML/Copasi model");
			    dm.setPriority(DebugConstants.PriorityType.MINOR.priorityCode);
				MainGui.addDebugMessage_ifNotPresent(dm);
		}else {*/
			MainGui.clear_debugMessages_relatedWith_table_col_priority(Constants.TitlesTabs.GLOBALQ.getDescription(), Constants.GlobalQColumns.VALUE.index, DebugConstants.PriorityType.MINOR.priorityCode);
		//}
		
		speciesDB.recalculateSpeciesWithVariableRanges();
		return true;
	}

	public Vector<Species> getAllSpecies() {
		return new Vector(this.speciesDB.getAllSpecies());
	}

	
	public Vector<Vector> updateSpecies(Integer nrow, String name, String initialQ, String type, String compartment, String expression, boolean autoFill, String notes, boolean autoMergeSpecies) throws Throwable{
		Exception exToThrow = null;
		boolean parseErrors = false;
		try{ 
			
			HashMap<String, String> entry_q = new HashMap<String, String>();
			entry_q.put(name,initialQ);
			this.speciesDB.addChangeSpecies(nrow,new String(),name,entry_q,Constants.SpeciesType.getCopasiTypeFromDescription(type),compartment,expression,false,notes,autoMergeSpecies,true);
		
			if(CellParsers.isMultistateSpeciesName_withUndefinedStates(name)) {
				   DebugMessage dm = new DebugMessage();
					dm.setOrigin_table(Constants.TitlesTabs.SPECIES.getDescription());
				    dm.setOrigin_col(Constants.SpeciesColumns.NAME.index);
				    dm.setOrigin_row(nrow);
					dm.setProblem("Multistate species with undefined states");
				    dm.setPriority(DebugConstants.PriorityType.PARSING.priorityCode);
					MainGui.addDebugMessage_ifNotPresent(dm);
				}
			
		} catch(MyInconsistencyException ex) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 
				ex.printStackTrace();
			exToThrow = ex;
		}catch(MySyntaxException ex) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 
				ex.printStackTrace();
		    DebugMessage dm = new DebugMessage();
			dm.setOrigin_table(Constants.TitlesTabs.SPECIES.getDescription());
		   if(ex.getColumn()!= -1) dm.setOrigin_col(ex.getColumn());
		   else dm.setOrigin_col(Constants.SpeciesColumns.NAME.index);
		    dm.setOrigin_row(nrow);
			dm.setProblem(ex.getMessage());
		    dm.setPriority(DebugConstants.PriorityType.PARSING.priorityCode);
			MainGui.addDebugMessage_ifNotPresent(dm);
			parseErrors = true;
			//MainGui.donotCleanDebugMessages = true;
			exToThrow = ex;
		}
		
		/*if(expression.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.NAN)) == 0) {
			  DebugMessage dm = new DebugMessage();
				dm.setOrigin_table(Constants.TitlesTabs.SPECIES.getDescription());
			    dm.setOrigin_col(Constants.SpeciesColumns.EXPRESSION.index);
			    dm.setOrigin_row(nrow);
				dm.setProblem("NaN value - will be exported as (undefined) in the SBML/Copasi model");
			    dm.setPriority(DebugConstants.PriorityType.MINOR.priorityCode);
				MainGui.addDebugMessage_ifNotPresent(dm);
		} else {*/
			MainGui.clear_debugMessages_relatedWith_table_col_priority(Constants.TitlesTabs.SPECIES.getDescription(), Constants.SpeciesColumns.EXPRESSION.index, DebugConstants.PriorityType.MINOR.priorityCode);
		//}
		
	/*	if(initialQ.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.NAN)) == 0) {
			  DebugMessage dm = new DebugMessage();
				dm.setOrigin_table(Constants.TitlesTabs.SPECIES.getDescription());
			    dm.setOrigin_col(Constants.SpeciesColumns.INITIAL_QUANTITY.index);
			    dm.setOrigin_row(nrow);
				dm.setProblem("NaN value - will be exported as (undefined) in the SBML/Copasi model");
			    dm.setPriority(DebugConstants.PriorityType.MINOR.priorityCode);
				MainGui.addDebugMessage_ifNotPresent(dm);
		}else {*/
			MainGui.clear_debugMessages_relatedWith_table_col_priority(Constants.TitlesTabs.SPECIES.getDescription(), Constants.SpeciesColumns.INITIAL_QUANTITY.index, DebugConstants.PriorityType.MINOR.priorityCode);
		//}
		
		Vector rows = new Vector();
		
		Species sp = this.speciesDB.getSpecies(name);
		if(sp == null) return rows;
		try{
			sp.setCompartment(this,compartment);
			sp.setInitialQuantity(this,initialQ);
			sp.setType(Constants.SpeciesType.getCopasiTypeFromDescription(type));
		
			//checkSimilarityName(name, nrow);
		} catch(MySyntaxException ex) { // something undefined in the initial quantity/expression of the compartment... but I have to move on with the species definition anyway
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 
				ex.printStackTrace();
		    DebugMessage dm = new DebugMessage();
			dm.setOrigin_table(ex.getTable());
		    dm.setOrigin_col(ex.getColumn());
		    dm.setOrigin_row(nrow);
			dm.setProblem(ex.getMessage());
		    dm.setPriority(DebugConstants.PriorityType.PARSING.priorityCode);
			MainGui.addDebugMessage_ifNotPresent(dm);

			//if(exToThrow!=null) throw exToThrow;
			return rows;
		}
		//if(exToThrow!=null) throw exToThrow;
		return rows;
	 }
	
	public Vector<Vector> updateSpeciesMultistate(Integer nrow, String name, HashMap<String, String> initialQ, String type, String compartment, String expression, boolean autoFill, String notes, boolean autoMergeSpecies) throws Throwable{
		Exception exToThrow = null;
		boolean parseErrors = false;
		try{ 
			
			this.speciesDB.addChangeSpecies(nrow,new String(),name,initialQ,Constants.SpeciesType.getCopasiTypeFromDescription(type),compartment,expression,false,notes,autoMergeSpecies,true);
		
			if(CellParsers.isMultistateSpeciesName_withUndefinedStates(name)) {
				   DebugMessage dm = new DebugMessage();
					dm.setOrigin_table(Constants.TitlesTabs.SPECIES.getDescription());
				    dm.setOrigin_col(Constants.SpeciesColumns.NAME.index);
				    dm.setOrigin_row(nrow);
					dm.setProblem("Multistate species with undefined states");
				    dm.setPriority(DebugConstants.PriorityType.PARSING.priorityCode);
					MainGui.addDebugMessage_ifNotPresent(dm);
				}
			
		} catch(MyInconsistencyException ex) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
			exToThrow = ex;
		}catch(MySyntaxException ex) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
		    DebugMessage dm = new DebugMessage();
			dm.setOrigin_table(ex.getTable());
		    dm.setOrigin_col(ex.getColumn());
		    dm.setOrigin_row(nrow);
			dm.setProblem(ex.getMessage());
		    dm.setPriority(DebugConstants.PriorityType.PARSING.priorityCode);
			MainGui.addDebugMessage_ifNotPresent(dm);
			parseErrors = true;
			//MainGui.donotCleanDebugMessages = true;
			exToThrow = ex;
		}
		
		/*if(expression.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.NAN)) == 0) {
			  DebugMessage dm = new DebugMessage();
				dm.setOrigin_table(Constants.TitlesTabs.SPECIES.getDescription());
			    dm.setOrigin_col(Constants.SpeciesColumns.EXPRESSION.index);
			    dm.setOrigin_row(nrow);
				dm.setProblem("NaN value - will be exported as (undefined) in the SBML/Copasi model");
			    dm.setPriority(DebugConstants.PriorityType.MINOR.priorityCode);
				MainGui.addDebugMessage_ifNotPresent(dm);
		} else {*/
			MainGui.clear_debugMessages_relatedWith_table_col_priority(Constants.TitlesTabs.SPECIES.getDescription(), Constants.SpeciesColumns.EXPRESSION.index, DebugConstants.PriorityType.MINOR.priorityCode);
		//}
		
	/*	if(initialQ.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.NAN)) == 0) {
			  DebugMessage dm = new DebugMessage();
				dm.setOrigin_table(Constants.TitlesTabs.SPECIES.getDescription());
			    dm.setOrigin_col(Constants.SpeciesColumns.INITIAL_QUANTITY.index);
			    dm.setOrigin_row(nrow);
				dm.setProblem("NaN value - will be exported as (undefined) in the SBML/Copasi model");
			    dm.setPriority(DebugConstants.PriorityType.MINOR.priorityCode);
				MainGui.addDebugMessage_ifNotPresent(dm);
		}else {*/
			MainGui.clear_debugMessages_relatedWith_table_col_priority(Constants.TitlesTabs.SPECIES.getDescription(), Constants.SpeciesColumns.INITIAL_QUANTITY.index, DebugConstants.PriorityType.MINOR.priorityCode);
		//}
		
		Vector rows = new Vector();
		
		MultistateSpecies sp = (MultistateSpecies) this.speciesDB.getSpecies(name);
		
		try{
			sp.setCompartment(this,compartment);
			sp.setInitialQuantity(initialQ);
			sp.setType(Constants.SpeciesType.getCopasiTypeFromDescription(type));
		
			//checkSimilarityName(name, nrow);
		} catch(MySyntaxException ex) { // something undefined in the initial quantity/expression of the compartment... but I have to move on with the species definition anyway
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
		    DebugMessage dm = new DebugMessage();
			dm.setOrigin_table(ex.getTable());
		    dm.setOrigin_col(ex.getColumn());
		    dm.setOrigin_row(nrow);
			dm.setProblem(ex.getMessage());
		    dm.setPriority(DebugConstants.PriorityType.PARSING.priorityCode);
			MainGui.addDebugMessage_ifNotPresent(dm);

			//if(exToThrow!=null) throw exToThrow;
			return rows;
		}
		//if(exToThrow!=null) throw exToThrow;
		return rows;
	 }
	
	public Vector<String> getUndefinedSpeciesInReaction(Integer nrow, String reaction_string) {
		Vector ret = new Vector<String>();
		Vector metabolites = new Vector();
		try{ 
			metabolites = CellParsers.parseReaction(this,reaction_string,nrow-1);
		} catch(Exception ex) {
			return ret;
		}
		Vector subs = (Vector)metabolites.get(0);
		Vector prod =(Vector)metabolites.get(1);
		Vector mod = (Vector)metabolites.get(2);
		
		for(int i = 0; i < subs.size(); i++){
			String sp = (String) subs.get(i);
			
			sp = extractName(sp);
			Species s = speciesDB.getSpecies(sp);
			if(s==null) {
				if(CellParsers.isMultistateSpeciesName(sp)) {
					String autocompletedMultistate = generateAutocompleteMultistateName(sp);
					if(!ret.contains(autocompletedMultistate)) ret.add(autocompletedMultistate);
				} else {
					if(!ret.contains(sp)) ret.add(sp);
				}
			} 
		}
		
		for(int i = 0; i < prod.size(); i++){
			String sp = (String) prod.get(i);
			sp = extractName(sp);
			Species s = speciesDB.getSpecies(sp);
			 if(s==null) {
				if(CellParsers.isMultistateSpeciesName(sp)) {
					String autocompletedMultistate = generateAutocompleteMultistateName(sp);
					if(!ret.contains(autocompletedMultistate)) ret.add(autocompletedMultistate);
				} else {
					if(!ret.contains(sp)) ret.add(sp);
				}
			}
		}
		for(int i = 0; i < mod.size(); i++){
			String sp = (String) mod.get(i);
			sp = extractName(sp);
			Species s = speciesDB.getSpecies(sp);
			if(s==null) {
				if(CellParsers.isMultistateSpeciesName(sp)) {
					String autocompletedMultistate = generateAutocompleteMultistateName(sp);
					if(!ret.contains(autocompletedMultistate)) ret.add(autocompletedMultistate);
				} else {
					if(!ret.contains(sp)) ret.add(sp);
				}
			}
		}
		return ret;
	}
	
	
	private String generateAutocompleteMultistateName(String sp) {
		String autocompletedMultistate = new String();
		try {
			MultistateSpecies m = new MultistateSpecies(this, sp, true);
			Set sites = m.getSitesNames();
			autocompletedMultistate += m.getSpeciesName();
			autocompletedMultistate += MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.OPEN_R);
			Iterator itSites = sites.iterator();
			while(itSites.hasNext()) {
				String site = itSites.next().toString();
				autocompletedMultistate += site 
											+ MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.OPEN_C)
											+ "?"
											+ MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.CLOSED_C);
				autocompletedMultistate += MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.SITE_NAMES_SEPARATOR);
			}
			if(sites.size()>0) {
				autocompletedMultistate = autocompletedMultistate.substring(0,autocompletedMultistate.length()-1);
			}
			autocompletedMultistate += MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.CLOSED_R);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return autocompletedMultistate;
	}

	public Vector<Vector> updateReaction(Integer nrow,	String name, String reaction_string, String type, String equation, String exp,	String notes, boolean autocompleteWithDefaults, boolean actionInColumnName) throws Throwable{
		Vector<Vector> table_rows = new Vector<Vector>();
		int prev_error_messages = MainGui.debugMessages.size();

		//MainGui.clear_debugMessages_relatedWith(nrow);
		MainGui.species_default_for_dialog_window.clear();

		boolean parseErrors = false;
		Vector metabolites = new Vector();
		try{ 
			
			metabolites = CellParsers.parseReaction(this,reaction_string,nrow-1);
		} catch(Exception ex) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
			parseErrors = true;
			metabolites.add(new Vector());
			metabolites.add(new Vector());
			metabolites.add(new Vector());
		}

		

		if(parseErrors) return null;

		if (autocompleteWithDefaults && actionInColumnName) {
			Vector subs = (Vector)metabolites.get(0);
			Vector prod =(Vector)metabolites.get(1);
			Vector mod = (Vector)metabolites.get(2);

			Vector<Integer> indexes_row_species = new Vector<Integer>();

			for(int i = 0; i < subs.size(); i++) {
				String s = (String)subs.get(i);
				s = extractName(s); //separates just the coefficent, multistate has to be still complete with state definition
				if(this.speciesDB.containsSpecies(s,true)) {
					Species sp = this.speciesDB.getSpecies(s);
					checkIfInMultipleCompartmentsHasProperCompLabel(s,sp,nrow);
					continue;
				}
				String speciesName = new String();
				if(CellParsers.isMultistateSpeciesName(s)) {
					speciesName = generateAutocompleteMultistateName(s);
				} else {
					speciesName = s;
				}
				
				HashMap<String, String> entry_q = new HashMap<String, String>();
				entry_q.put(s,MainGui.species_defaultInitialValue);
				
				indexes_row_species.add(speciesDB.addChangeSpecies(-1,new String(),speciesName,entry_q,CMetab.REACTIONS, MainGui.compartment_default_for_dialog_window, new String(),false,"",true,true));
				/*if(CellParsers.isMultistateSpeciesName_withUndefinedStates(name)) {
					throw new MySyntaxException(Constants.SpeciesColumns.NAME.index, 
												"Multistate species with undefined states", 
												Constants.TitlesTabs.SPECIES.getDescription());
					
				}*/
				if(CellParsers.isMultistateSpeciesName_withUndefinedStates(name)) {
				   DebugMessage dm = new DebugMessage();
					dm.setOrigin_table(Constants.TitlesTabs.SPECIES.getDescription());
				    dm.setOrigin_col(Constants.SpeciesColumns.NAME.index);
				    dm.setOrigin_row(nrow);
					dm.setProblem("Multistate species with undefined states");
				    dm.setPriority(DebugConstants.PriorityType.PARSING.priorityCode);
					MainGui.addDebugMessage_ifNotPresent(dm);
				}
					
			}

			for(int i = 0; i < prod.size(); i++) {
				String s = (String)prod.get(i);
				s = extractName(s); //separates just the coefficent, multistate has to be still complete with state definition
				if(//!s.contains("(") && 
						this.speciesDB.containsSpecies(s)) {continue;};
				
				HashMap<String, String> entry_q = new HashMap<String, String>();
				entry_q.put(s,MainGui.species_defaultInitialValue);
			indexes_row_species.add(speciesDB.addChangeSpecies(-1,new String(),s,entry_q, CMetab.REACTIONS,MainGui.compartment_default_for_dialog_window, new String(),false,"",true,true));
			
			}


			for(int i = 0; i < mod.size(); i++) {
				String s = (String)mod.get(i);
				s = extractName(s);
				if(this.speciesDB.containsSpecies(s)) {continue;};
				
				HashMap<String, String> entry_q = new HashMap<String, String>();
				entry_q.put(s,MainGui.species_defaultInitialValue);
				indexes_row_species.add(speciesDB.addChangeSpecies(-1,new String(),s,entry_q,CMetab.REACTIONS,MainGui.compartment_default_for_dialog_window, new String(),false,"",true,true));
			
			}

			for(int j = 0; j < indexes_row_species.size(); j++) {
				int species_row_index = Math.abs(indexes_row_species.get(j));
				if(species_row_index!= 0) {
					Species sp = speciesDB.getSpecies(species_row_index);
					Vector row = new Vector();
					row.add(species_row_index);
					row.addAll(sp.getAllFields());
					table_rows.add(row);
			
					MainGui.species_default_for_dialog_window.add(sp.getDisplayedName());
					//this.checkSimilarityName(sp.getSpeciesName(), species_row_index);
				}
			}
			
		}
		try {
			reactionDB.addChangeReaction(nrow, name, reaction_string, Constants.ReactionType.getCopasiTypeFromDescription(type), equation, notes);
			ConsistencyChecks.all_parameters_in_functionCalls_exist(Constants.TitlesTabs.REACTIONS.index, this, equation,nrow-1);
		} catch (Exception e) {
			//e.printStackTrace();
			//ok, the problems are going to be handled somewhere else (in mainGui)
		}
		
		return table_rows;
	 }
		
	public void checkIfInMultipleCompartmentsHasProperCompLabel(String s, Species sp, Integer nrow) {
		if(sp.getCompartments().size() > 1) { //check that the species in the reaction is fully qualified, not just the name
			String cmpLabel = CellParsers.extractCompartmentLabel(s);
			if(cmpLabel.trim().length() == 0) {
				DebugMessage dm = new DebugMessage();
				dm.setOrigin_table(Constants.TitlesTabs.REACTIONS.getDescription());
				dm.setOrigin_col(Constants.ReactionsColumns.REACTION.index);
				dm.setOrigin_row(nrow);
				String message = "Species "+sp+" exists in more than one compartment. \n The reaction should specify which one to use between: \n";
				Vector<String> cmps = sp.getCompartments();
				for(int i1 = 0; i1 < cmps.size(); i1++) {
					message += CellParsers.addCompartmentLabel(s, cmps.get(i1))+"\n";
				}
				dm.setProblem(message);
				dm.setPriority(DebugConstants.PriorityType.PARSING.priorityCode);
				MainGui.addDebugMessage_ifNotPresent(dm);
			}
		}
	}

	public int updateCompartment(Integer nrow, String name, String type,
			String initial, String expression, String notes) throws Throwable {
		
		try{
			return this.compDB.addChangeComp(nrow, CellParsers.cleanName(name), type, initial, expression, notes);
		} catch(MySyntaxException ex) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
			    DebugMessage dm = new DebugMessage();
				dm.setOrigin_table(Constants.TitlesTabs.COMPARTMENTS.getDescription());
			    dm.setOrigin_col(ex.getColumn());
			    dm.setOrigin_row(nrow);
				dm.setProblem(ex.getMessage());
			    dm.setPriority(DebugConstants.PriorityType.PARSING.priorityCode);
				MainGui.addDebugMessage_ifNotPresent(dm);
				throw ex;
			}
		
	}

	public boolean updateEvent(Integer nrow, String name, String trigger, String actions, String delay, boolean delayAfterCalculation, String notes, int expandActionVolume){
		try {
			boolean ret =  eventsDB.addChangeEvent(nrow, CellParsers.cleanName(name), trigger, actions,delay, delayAfterCalculation, notes,expandActionVolume);
			boolean okTrigger = ConsistencyChecks.isOkEventTriggerExpression(trigger);
			if(!okTrigger) {
				throw new MySyntaxException(Constants.EventsColumns.TRIGGER.index, 
						"The trigger expression does not contain a proper boolean expression.\nCommon mistakes:\n * using = instead of ==, \n * missing relational operator (==, >=, <=),\n * using & or | instead of && and ||",Constants.TitlesTabs.EVENTS.getDescription());
			}
			return ret;
		} catch(MySyntaxException ex) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
		    DebugMessage dm = new DebugMessage();
			dm.setOrigin_table(Constants.TitlesTabs.EVENTS.getDescription());
		    dm.setOrigin_col(ex.getColumn());
		    dm.setOrigin_row(nrow);
			dm.setProblem(ex.getMessage());
		    dm.setPriority(DebugConstants.PriorityType.PARSING.priorityCode);
			MainGui.addDebugMessage_ifNotPresent(dm);
			return false;
		//	throw ex;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	
	}


	/*public String getSBMLid() {
		return this.copasiDataModelSBML_ID;
	}*/
	
	
	
	public String getDifferentialEquations(List listFunctionToCompact) throws Throwable {
		 // Check if species:
	      // i)   is a constant
	      // ii)   is set by a rule
	      // if none of the above calculate the differential equation
		String ret = new String();
		Collection<Function> defFunctions = funDB.getAllFunctions();
		Vector<String> funcs = new Vector<String>();
		Iterator it2 = defFunctions.iterator();
		while(it2.hasNext()) {
			Function fun = (Function)it2.next();
			if(fun==null) continue;
			if(listFunctionToCompact.contains(fun.getName())) {
				String expression = fun.getExpandedEquation(new Vector());
				if(expression.length()>0) {
					funcs.add(fun.getCompactedEquation(new Vector())+"="+expression);
				}
			}
		}
		Collections.sort(funcs);
		/*for(int i = 0; i < funcs.size();i++) {
			System.out.println(funcs.get(i));
		}*/
		
		HashSet<String> notOdes = new HashSet<String>();
		Vector<Species> all = speciesDB.getAllSpecies();
		Vector<String> odes = new Vector<String>();
		
		for(int i = 1; i < all.size();i++) {
			Species s = all.get(i);
			if(s instanceof MultistateSpecies) {
				ret = "Multi-State species in the model: equation generation not supported.\n\n"+
					  "The generation of the underlying ODE for multistate systems is coming soon!\n\n"+
					  "For now you can export the model to COPASI and see the ODE system there! :)";
				return ret;
			}
			String expression = s.getExpression();
			if(expression.length()>0) {
				/*CellParsers.parser.parseExpression(expression);
				OdeExpressionVisitor_DELETE_oldParser visitor = new OdeExpressionVisitor_DELETE_oldParser(///i,
						listFunctionToCompact, this);
				if(CellParsers.parser.getErrorInfo()!= null) {
					throw new Exception(CellParsers.parser.getErrorInfo());
				}
				String expr = visitor.toString(CellParsers.parser.getTopNode());*/
				
				InputStream is = new ByteArrayInputStream(expression.getBytes("UTF-8"));
				MR_Expression_Parser parser = new MR_Expression_Parser(is);
				CompleteExpression root = parser.CompleteExpression();
				ExpressionVisitor vis = new ExpressionVisitor(listFunctionToCompact,this,false);
				root.accept(vis);
				String expr = null;
				if(vis.getExceptions().size() == 0) {
					expr  = vis.getExpression();
				} else {
					throw vis.getExceptions().get(0);
				}
				
				odes.add("d"+s.getSpeciesName()+"/dt="+expr);
				notOdes.add(s.getSpeciesName());
			}
			if(s.getType()== Constants.SpeciesType.FIXED.copasiType) {
				odes.add("d"+s.getSpeciesName()+"/dt=0");
				notOdes.add(s.getSpeciesName());
			}
			
		}
		Collections.sort(odes);
		if(odes.size() >0){
			ret+= "---- Determined by assignment rules ----------------" + System.getProperty("line.separator");
		}
		for(int i = 0; i < odes.size();i++) {
			 ret+= odes.get(i) + System.getProperty("line.separator");
		}
		if(odes.size() >0){
			ret+= "----------------------------------------------------" + System.getProperty("line.separator");
		}
		
		
	    // Go through all the reactions in the model and append to diffEquation
	    int numberOfReactions = this.getNumReactions();
	    String diffEquation = new String("");
	    TreeMap variableToEquationMap = new TreeMap();
	    for (int i = 0; i < numberOfReactions; i++) {
	    	String reaction_string = ((String) tableReactionmodel.getValueAt(i, Constants.ReactionsColumns.REACTION.index)).trim();
	        if(reaction_string.trim().length() == 0) continue; 
	    	Vector metabolites = CellParsers.parseReaction(this,reaction_string,i+1);
	    	Vector subs = (Vector)metabolites.get(0);
			Vector prod =(Vector)metabolites.get(1);
			Vector mod = (Vector)metabolites.get(2);
			
	    	//String infixFunction = convertMathToInfix (reaction.getKineticLaw ().getMath (), false);
	    	//String currentEquation = getEquation (reaction.getKineticLaw ().getMath (), infixFunction);
	        //Matcher getSpeciesName = getSpeciesFromFunction.matcher (infixFunction);
			String currentEquation = ((String) tableReactionmodel.getValueAt(i, Constants.ReactionsColumns.KINETIC_LAW.index)).trim();
			String currentEq_expanded = expandFunctionCalls(currentEquation,i,listFunctionToCompact);
	         for (int j = 0; j < subs.size(); j++) {
	        	String stoichStr = "";
	            diffEquation = "";
	            String sp = (String) subs.get(j);
	            if(notOdes.contains(sp)) {continue;}
	            if((String)variableToEquationMap.get(sp)==null) {
	            	 variableToEquationMap.put (sp, "");
	            }
	            diffEquation += " -";
	            
	            
	          //   String reactantCompartment = (String)speciesToCompartmentMap.get (reactantId);
	          //  stoich = reactant.getStoichiometry ();
	          //  if (stoich != 1.0)
	           //    stoichStr = stoich + " * ";
	            diffEquation += " ("  + stoichStr + currentEq_expanded + ") ";
	           /* if (getSpeciesName.matches ()) { 
	               Species speciesInFunction = (Species)model.findElementWithId (getSpeciesName.group (1), Model.SPECIES);
	               String speciesInFunctionCompartment = "";
	               if (speciesInFunction != null)
	                  speciesInFunctionCompartment = (String)speciesToCompartmentMap.get (speciesInFunction.getId ());
	               // Check whether species in kinetic law is in a different comparment.
	               // If this is the case then adjust its concentration by dividing comparment sizes together.
	               // C/dt = N * Vn/Vc    -> C species in comparment cytoplasm, N species in comparment nucleus
	               //                     -> Vc cytoplasm volume, Vn nucleus volume 
	               if (!reactantCompartment.equals (speciesInFunctionCompartment)) 
	                  diffEquation += " *(" + speciesInFunctionCompartment + "/" + reactantCompartment + ")";
	            }
	            variableToEquationMap.put (reactantId, (String)variableToEquationMap.get (reactantId) + replaceIdWithName (diffEquation));*/
	            variableToEquationMap.put (sp, (String)variableToEquationMap.get(sp) + diffEquation); 
	         }
	         for (int j = 0; j < prod.size(); j++) {
		            String stoichStr = "";
		            diffEquation = "";
		            String sp = (String) prod.get(j);
		            if(notOdes.contains(sp)) {continue;}
			        if((String)variableToEquationMap.get(sp)==null) {
		            	 variableToEquationMap.put (sp, "");
		            } else {
		            	 diffEquation += "+";
		            }
		          //   String reactantCompartment = (String)speciesToCompartmentMap.get (reactantId);
		          //  stoich = reactant.getStoichiometry ();
		          //  if (stoich != 1.0)
		           //    stoichStr = stoich + " * ";
		            diffEquation += " ("  + stoichStr + currentEq_expanded + ") ";
		           /* if (getSpeciesName.matches ()) { 
		               Species speciesInFunction = (Species)model.findElementWithId (getSpeciesName.group (1), Model.SPECIES);
		               String speciesInFunctionCompartment = "";
		               if (speciesInFunction != null)
		                  speciesInFunctionCompartment = (String)speciesToCompartmentMap.get (speciesInFunction.getId ());
		               // Check whether species in kinetic law is in a different comparment.
		               // If this is the case then adjust its concentration by dividing comparment sizes together.
		               // C/dt = N * Vn/Vc    -> C species in comparment cytoplasm, N species in comparment nucleus
		               //                     -> Vc cytoplasm volume, Vn nucleus volume 
		               if (!reactantCompartment.equals (speciesInFunctionCompartment)) 
		                  diffEquation += " *(" + speciesInFunctionCompartment + "/" + reactantCompartment + ")";
		            }
		            variableToEquationMap.put (reactantId, (String)variableToEquationMap.get (reactantId) + replaceIdWithName (diffEquation));*/
		            variableToEquationMap.put (sp, (String)variableToEquationMap.get(sp) + diffEquation); 
	         	}
	    }
	    
	    Set key = variableToEquationMap.keySet();
	    Iterator it = key.iterator();
	 	while(it.hasNext()) {
	 		String elem = (String) it.next();
			ret += "d"+elem+"/dt="+variableToEquationMap.get(elem) + System.getProperty("line.separator");
		}
	 	
	 	
	 	//global quantities determined by assignment and odes
		Vector<GlobalQ> all_glq = globalqDB.getAllGlobalQ();
		Vector<String> assign_glq = new Vector<String>();
		Vector<String> odes_glq = new Vector<String>();
		
		
		for(int i = 1; i < all_glq.size();i++) {
			GlobalQ s = all_glq.get(i);
			if(s == null || s.getType()== Constants.GlobalQType.FIXED.copasiType) {
				continue;
			}
			String expression = s.getExpression();
			if(expression.length()>0) {
				InputStream is = new ByteArrayInputStream(expression.getBytes("UTF-8"));
				MR_Expression_Parser parser = new MR_Expression_Parser(is);
				CompleteExpression root = parser.CompleteExpression();
				ExpressionVisitor vis = new ExpressionVisitor(listFunctionToCompact,this,false);
				root.accept(vis);
				String expr = null;
				if(vis.getExceptions().size() == 0) {
					expr  = vis.getExpression();
				} else {
					throw vis.getExceptions().get(0);
				}
				
				if(s.getType()== Constants.GlobalQType.ASSIGNMENT.copasiType) {
					assign_glq.add(s.getName()+"="+expr);
				} else if(s.getType()== Constants.GlobalQType.ODE.copasiType) {
					odes_glq.add("d"+s.getName()+"/dt="+expr);
				} 
			}
		}
		Collections.sort(odes_glq);
		if(odes_glq.size() >0){
			ret+= "---- Global quantities determined by odes ----------------" + System.getProperty("line.separator");
		}
		for(int i = 0; i < odes_glq.size();i++) {
			 ret+= odes_glq.get(i) + System.getProperty("line.separator");
		}
		if(odes_glq.size() >0){
			ret+= "---------------------------------------------------------------------------" + System.getProperty("line.separator");
		}
		
		if(assign_glq.size() >0){
			ret+= "---- Global quantities determined by assignment ----------------" + System.getProperty("line.separator");
		}
		for(int i = 0; i < assign_glq.size();i++) {
			 ret+= assign_glq.get(i) + System.getProperty("line.separator");
		}
		if(assign_glq.size() >0){
			ret+= "---------------------------------------------------------------------------" + System.getProperty("line.separator");
		}
		
		return ret;
		
	}
	
		
	private String expandFunctionCalls(String currentEquation, int i, 
			List listFunctionToCompact) throws Throwable {
		boolean foundUserDefFunction = false;
		boolean foundPredefinedFunction= false;
		boolean foundMassAction= false;
		String ret = new String();
		
		
		if(((String)tableReactionmodel.getValueAt(i, Constants.ReactionsColumns.TYPE.index)).compareTo(Constants.ReactionType.USER_DEFINED.getDescription())==0) { foundUserDefFunction = true; }
		else if(((String)tableReactionmodel.getValueAt(i, Constants.ReactionsColumns.TYPE.index)).compareTo(Constants.ReactionType.PRE_DEFINED.getDescription())==0) { foundPredefinedFunction = true; }
		else if(((String)tableReactionmodel.getValueAt(i, Constants.ReactionsColumns.TYPE.index)).compareTo(Constants.ReactionType.MASS_ACTION.getDescription())==0) {  foundMassAction = true;    	 }

	
			
		
		String expr =new String();
		if(listFunctionToCompact.size()!=0) {
			if(listFunctionToCompact.size() == funDB.getAllFunctions().size()) {
					expr = MainGui.getViewIn(Constants.TitlesTabs.REACTIONS.getDescription(),i,Constants.ReactionsColumns.KINETIC_LAW.index,Constants.Views.COMPRESSED.index);
				}
			else {
				expr = tableReactionmodel.getValueAt(i, Constants.ReactionsColumns.KINETIC_LAW.index).toString();
				do {
					String tmp = new String();
					try{
						InputStream is = new ByteArrayInputStream(expr.getBytes("UTF-8"));
						MR_Expression_Parser parser = new MR_Expression_Parser(is);
						CompleteExpression root = parser.CompleteExpression();
						ExpressionVisitor vis = new ExpressionVisitor(listFunctionToCompact,this,false);
						root.accept(vis);
						if(vis.getExceptions().size() == 0) {
							tmp  = vis.getExpression();
						} else {
							throw vis.getExceptions().get(0);
						}

					}catch (Exception e) {
						if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
					}
					if(expr.compareTo(tmp)==0) break;
					expr = tmp;
			} while(true);
			}
		} else {
			expr = MainGui.getViewIn(Constants.TitlesTabs.REACTIONS.getDescription(),i,Constants.ReactionsColumns.KINETIC_LAW.index,Constants.Views.EXPANDED.index);
		}
		 
		ret = expr;
		//EXPANDED VIEW of MassAction reaction already adds the substrates!!!
	/*	if(expr.length() > 0) {
			if(foundMassAction) {
				Vector metabolites = CellParsers.parseReaction(this,(String) tableReactionmodel.getValueAt(i,Constants.ReactionsColumns.REACTION.index),i+1);
				Vector subs = (Vector) metabolites.get(0);
				ret+=expr+"*(";
				
				for(int j = 0; j < subs.size(); j++) {
					String sp = (String) subs.get(j);
					ret+=sp+"*";
				}
				ret = ret.substring(0,ret.length()-1);
				ret+=")";
			} else {
				ret+=expr;
			}
		}
		else { throw new ParseException("Parsing problem generating the ODE system.");}*/
		
		return ret;
	}
	
	public void updateSBMLids(HashMap<Long, String> SBMLids) {
		this.speciesDB.updateSBMLids(SBMLids);
	}
	
	
	
	/*public Vector addSpeciesFromSBMLidList(List<String> metaboliteSBMLidInCopasiDataModel) throws Throwable{
		CModel model = copasiDataModel.getModel();
		Vector rows = new Vector();
		HashMap<Long, String> SBML_IDS = new HashMap<Long, String>();
		
		for(int i = 0; i < metaboliteSBMLidInCopasiDataModel.size(); i++) {
			String key = metaboliteSBMLidInCopasiDataModel.get(i);
			Vector row = new Vector();
            CMetab metab = model.getMetabolite(this.findMetabolite_sbmlID(key));
            
            String cleanName = CellParsers.cleanName(metab.getObjectName(),true);
            if(cleanName.compareTo(metab.getObjectName())!=0) {
            	metab.setObjectName(cleanName);
            }
            row.add(metab.getObjectName());
            SBML_IDS.put((long) (this.speciesDB.getNumSpeciesExpanded()+i+1), metab.getSBMLId());
            if(MainGui.quantityIsConc) row.add(new Double(metab.getInitialConcentration()).toString());
            else row.add(new Double(metab.getInitialValue()).toString());
           
           if(metab.getInitialExpression().trim().length()>0) {
        	   row.set(row.size()-1, buildMRExpression_fromCopasiExpr(metab.getInitialExpression()));
           }
            
            row.add(Constants.SpeciesType.getDescriptionFromCopasiType(metab.getStatus()));
            
            row.add(metab.getCompartment().getObjectName());
            
            if(metab.getStatus() == CMetab.ASSIGNMENT || metab.getStatus() == CMetab.ODE) {
            	row.add(CellParsers.cleanMathematicalExpression(this.buildMRExpression_fromCopasiExpr(metab.getExpression())));
            }
            
           rows.add(row);
		}
		Vector ret = new Vector();
        ret.add(rows);
        ret.add(SBML_IDS);
        return ret;
	}*/
	
	
	public void removeComp(int[] selected) {
		for(int i = 0; i < selected.length; i++) {
			this.compDB.removeComp(selected[i]-i);
		}
	}
	
	public void removeSpecies(int[] selected) {
		for(int i = 0; i < selected.length; i++) {
			this.speciesDB.removeSpecies(selected[i]-i);
		}
	}
	
	public void removeInvisibleSpecies(String name) {
			this.speciesDB.removeInvisibleSpecies(name);
	}
	
	public void removeGlobalQ(int[] selected) {
		for(int i = 0; i < selected.length; i++) {
			this.globalqDB.removeGlobalQ(selected[i]-i);
		}
	}
	
	public void clearDataOldMultistateSpecies(String valueAt) {
		if(valueAt.trim().length() > 0) {
			String name = valueAt.substring(0,valueAt.indexOf("("));
			speciesDB.clearDataOldMultistateSpecies(name);
		}
	}
	
	public Vector<String> convert2nonReversible() {
		CModel model = copasiDataModel.getModel();
		
		Vector<String> ret = new Vector<String>();
		
		if(model == null) return  ret;
		
		 // clear warnings / error messages
		CCopasiMessage.clearDeque();
		
		
		 boolean converted = model.convert2NonReversible();
	     
	      if(!converted) {
	        	
	        	int i = 0;
	        	int index = -1;
	        	while(CCopasiMessage.size() > 0) {
	        		CCopasiMessage message = CCopasiMessage.getFirstMessage();
	        		
	        		if(message.getType()==CCopasiMessage.ERROR) {
	        			String text = message.getText();
	        			int indexOf = text.indexOf("could not be split into two irreversible");
	        			if(indexOf != -1) {
	        				int firstQuote = text.indexOf("'");
	        				String reactionName = text.substring(firstQuote+1, indexOf-2).trim();
	        				index = findReaction(reactionName);
	        				ret.add(reactionName);
	        				if(index != -1) {
	        					CReaction r = model.getReaction(index);
	        					r.setReversible(false);
	        				}
	        				index = -1;
		        		}
	        		}
	        		i++;
	        	}
	        	converted = model.convert2NonReversible();
	        	if(!converted) {
	        		System.out.println("!!!!!!----- PROBLEMS IN convert2NonReversible-----");
	        		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	        		return null;
	        	}
	        }
	       return ret;
		
	}
	
	public void removeSpecies(int atIndex) throws Throwable {
		this.speciesDB.removeSpecies(atIndex);
	}
	
	public void removeComp(int atIndex) throws Throwable {
		this.compDB.removeComp(atIndex);
	}
	
	public void removeGlobalQ(int atIndex) throws Throwable {
		this.globalqDB.removeGlobalQ(atIndex);
	}
	
	public void removeFunction(int atIndex) throws Throwable {
		this.funDB.removeFunction(atIndex);
	}
	
	public void removeEvent(int atIndex) throws Throwable {
		this.eventsDB.removeEvent(atIndex);
	}
	
	public void removeReaction(int atIndex) throws Throwable {
		this.reactionDB.removeReaction(atIndex);
		tableReactionmodel.removeRow(atIndex);
		tableReactionmodel.fireTableDataChanged();
	}
	
	public Vector<GlobalQ> getAllGlobalQ() {
		return new Vector(this.globalqDB.getAllGlobalQ());
	}
	
	public Vector<Event> getAllEvents() {
		return new Vector(this.eventsDB.getAllEvents());
	}
	
	public Vector<Reaction> getAllReactions() {
		return new Vector(this.reactionDB.getAllReactions());
	}
	

	public boolean containsSpecies(String name) {
		return speciesDB.containsSpecies(name);
	}

	public String getEditableExpression(String table, int row, int column) {
		String editableString = new String("getEditableExpression_TO_BE_IMPLEMENTED");
		if(table.compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0) { editableString = reactionDB.getEditableExpression(row, column);} 
		else if(table.compareTo(Constants.TitlesTabs.SPECIES.getDescription())==0) {		editableString = speciesDB.getEditableExpression(row, column); }
		else if(table.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())==0) { 	editableString = globalqDB.getEditableExpression(row, column);}
		else if(table.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription())==0) {editableString = compDB.getEditableExpression(row, column);}
	/*	else if(table.compareTo(Constants.TitlesTabs.FUNCTIONS.getDescription())==0) { 	editableString = (String) MainGui.tableFunctionsmodel.getValueAt(row, column);}
		else if(table.compareTo(Constants.TitlesTabs.EVENTS.getDescription())==0) { 		editableString = (String) MainGui.tableEventsmodel.getValueAt(row, column);}*/
		else editableString = new String("");
		return editableString;
	}

	public void setEditableExpression(String editableString, int row, int column) throws Throwable {
		speciesDB.setEditableExpression(editableString, row, column);
	}

	public Species getSpecies(String name, String compartment) {
		Species sp = speciesDB.getSpecies(name);
		if(compartment != null) {
			if(sp.getCompartments().contains(compartment)) return sp;
			else return null;
		} else return sp;
	}
	
	public Species getSpecies(String name){
		return getSpecies(name, null);
	}
	
	
	public Reaction getReaction(int index) {
		return reactionDB.getReaction(index);
	}


	public Compartment getComp(String name) {
		return compDB.getComp(name);
	}

	public Function getFunctionByName(String fun) throws Throwable {
		Function f = funDB.getFunctionByName(fun);
		if(f == null) f = funDB.getBuiltInFunctionByName(fun);
		return f;
	}

	public HashSet<Integer> getWhereFuncIsUsed(String funName) {
		return funDB.whereFuncIsUsed.get(funName);
		
	}
	
	public void setWhereFuncIsUsed(String funName, HashSet<Integer> h) {
		if(h == null) {
			funDB.whereFuncIsUsed.remove(funName);
		} else {
			funDB.whereFuncIsUsed.put(funName,h);
		}
	}
	
	public void addMapping(int i, Vector v) {
		funDB.mappings.put(new Integer(i),v);
		
	}

	public HashMap<String, HashMap<String, String>> getMultistateInitials() {
		return speciesDB.getMultistateInitials();
	}

	public void setMultistateInitials(
			HashMap<String, HashMap<String, String>> multistateInitials) {
		if(multistateInitials!= null) speciesDB.setMultistateInitials(multistateInitials);
	}

	public Integer getSpeciesIndex(String name) {
		
		return speciesDB.getSpeciesIndex(name);
	}
	
public Integer getGlobalQIndex(String name) {
		
		return globalqDB.getGlobalQIndex(name);
	}

	public Vector<MultistateSpecies> getAllMultistateSpecies() {
		return speciesDB.getAllMultistateSpecies();
	}

	public void addCompartmentToSpecies(String name, String cmpName) {
		speciesDB.addCompartmentToSpecies(name, cmpName);
		
	}

	public void updateReactionEditableView(int row, String editableView) {
		reactionDB.updateReactionEditableView(row, editableView);
		
	}

	public Vector<Function> getAllUserDefinedFunctins() {
		return funDB.getAllUserDefinedFunctions();
	}

	
	public boolean checkUsageOfSiteType(int row, String completeSiteRef, boolean checkingAllRoles) {
		Vector misused = new Vector<String>();
		
		MutablePair<String, Vector<String>> nameAndPossibleExtensions = CellParsers.extractNameExtensions(completeSiteRef);
		
		Vector<String> extensions = nameAndPossibleExtensions.right;
		String speciesName = nameAndPossibleExtensions.left;
		
		if(extensions.size() ==0 ) {
			if(!checkingAllRoles) {
				DebugMessage dm = new DebugMessage();
				dm.setOrigin_table(Constants.TitlesTabs.REACTIONS.getDescription());
			    dm.setOrigin_col(Constants.ReactionsColumns.KINETIC_LAW.index);
			    dm.setOrigin_row(row+1);
				dm.setProblem("Site specification has to be in the form of SpeciesName.siteName");
			    dm.setPriority(DebugConstants.PriorityType.PARSING.priorityCode);
				MainGui.addDebugMessage_ifNotPresent(dm);
			}
			return false;
		} else {
				String siteName = extensions.get(0).substring(1);
			
		
		
		
		Reaction r = reactionDB.getReaction(row+1);
		Vector<String> subs = r.getSubstrates(this);
		Vector<String> prod = r.getProducts(this);
		Vector<String> mod = r.getModifiers(this);
		for(int j = 0; j < subs.size(); j++){
				String element1 = subs.get(j);
				element1 = this.extractName(element1);
				String element1_justName_ifMultistate = element1;
				if(CellParsers.isMultistateSpeciesName(element1)) {
					element1_justName_ifMultistate = CellParsers.extractMultistateName(element1);
				}
				
				
			if(speciesName.compareTo(element1_justName_ifMultistate) == 0) {
				if(!(this.getSpecies(element1_justName_ifMultistate) instanceof MultistateSpecies)) {
					DebugMessage dm = new DebugMessage();
					dm.setOrigin_table(Constants.TitlesTabs.REACTIONS.getDescription());
				    dm.setOrigin_col(Constants.ReactionsColumns.KINETIC_LAW.index);
				    dm.setOrigin_row(row+1);
					dm.setProblem("Site not available for a non-multistate Species!");
				    dm.setPriority(DebugConstants.PriorityType.MISSING.priorityCode);
					MainGui.addDebugMessage_ifNotPresent(dm);
					return false;
				}
				
				MultistateSpecies sp = (MultistateSpecies) this.getSpecies(element1_justName_ifMultistate);
				if(sp.getSitesNames().contains(siteName)) {
					Vector values = sp.getSiteStates_complete(siteName);
					
					Iterator it = values.iterator();
					while(it.hasNext()) {
						String current = it.next().toString();
						if(current.trim().length() == 0) continue;
						try {
							Double val = Double.parseDouble(current);
						} catch(Exception ex) { //non numeric site value cannot be used as site value in rate laws
							return false;
						}
					}
					return true;
				}
				}
			}
		}
		
		return false;
	}
	
	public Vector<String> checkUsageElementsOfReaction(Vector<MutablePair<String, String>> usedAsElements, int row) {
		return reactionDB.checkUsage(usedAsElements, row);
	}

	public Vector<String> getAllSpecies_names() {
		return speciesDB.getAllNames();
	}
	
	public Vector<String> getAllInvisibleSpecies_names() {
		return speciesDB.getAllInvisibleNames();
	}
	
	public Vector<Species> getAllInvisibleSpecies() {
		return speciesDB.getAllInvisibleSpecies();
	}

	public Vector<String> getAllGlobalQuantities_names() {
		return globalqDB.getAllNames();
	}
	
	public Vector<String> getAllCompartments_names() {
		return compDB.getAllNames();
	}
	
	public Vector<String> getAllReaction_names() {
		return reactionDB.getAllNames();
	}

	
	public void removeRateLawMappingForRow(int row) {
		funDB.removeRateLawMappingForRow(row);
		
	}

	public void addSpecies_fromInterface(String name, String initialQuantity, String compartment) throws Exception{
		HashMap<String, String> entry_q = new HashMap<String, String>();
		entry_q.put(name,initialQuantity);
		try {
			
			
			this.speciesDB.addChangeSpecies(-1,new String(),name,entry_q,Constants.SpeciesType.REACTIONS.copasiType,compartment,"",false,"",false,true);
		} catch (Throwable e) {
				//if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
				throw new Exception("Error adding the new Species: "+e.getMessage());
		}

		return;
	}
	
	public void addGlobalQuantity_fromInterface(String name, String initialQuantity) throws Exception{
		HashMap<String, String> entry_q = new HashMap<String, String>();
		entry_q.put(name,initialQuantity);
		try {
			this.globalqDB.addChangeGlobalQ(-1, name, initialQuantity, Constants.GlobalQType.FIXED.getDescription(), new String(), new String());
		} catch (Throwable e) {
				//if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
				throw new Exception("Error adding the new Global Quantity: "+e.getMessage());
		}

		return;
	}

	public void setCopasiKey(String copasiKey) {
		this.copasiDataModel_key = copasiKey;
	}
	
	public void setModelName(String copasiModelName) {
		this.copasiDataModel_modelName = copasiModelName;
	}
	
	public void setModelDefinition(String modelDefinition) {
		this.copasiDataModel_modelDefinition = modelDefinition;
	}

	
	public String getModelName() {
		//return copasiDataModel.getModel().getObjectName();
		return 	this.copasiDataModel_modelName;
	}
	
	public String getModelDefinition() {
		return 	this.copasiDataModel_modelDefinition;
	}

	
	
	public void addInvisibleSpecies(String name, String initialQuantity,
			String compartment) throws Exception {
		this.speciesDB.addInvisibleSpecies(name, initialQuantity, compartment);
		
	}

	public ComplexSpecies getComplexSpecies(String name) {
		return speciesDB.getComplexSpecies(name);
	}

	public void updateComplex(int index, ComplexSpecies newComplex) throws Throwable {
		speciesDB.updateComplex(index, newComplex);
	}

	public void addChangeComplex(int index, ComplexSpecies complex) {
		try {
			speciesDB.addChangeComplex(index, complex);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
	}

	public Vector getAllComplexSpeciesSerialized() {
		return speciesDB.getAllComplexSpeciesSerialized();
	}

	public void setComplexSpecies(Vector listOfComplex) {
		speciesDB.initializeComplexFromImport(listOfComplex);
	}

	public void moveUpRow_linkedReaction(int row) {
		speciesDB.moveUpRow_linkedReaction(row);
	}
	
	public void unlinkReaction(int row) {
		speciesDB.unlinkReaction(row);
	}

	public Species getSpeciesAt(Integer index) {
		return speciesDB.getSpecies(index);
	}

	public void updateSBMLid_fromCopasiDataModel(String table, String name, String compartment) {
		if(table.compareTo(Constants.TitlesTabs.SPECIES.getDescription())==0) {
			try {
				int index = findMetabolite(name, compartment, false);
				if(index != -1) {
					CMetab metab = copasiDataModel.getModel().getMetabolite(index);
					String sbmlId = metab.getSBMLId();
					//System.out.println("current sbml id (species) = "+sbmlId);
					this.speciesDB.updateSBMLid_fromCopasiDataModel(name, compartment, sbmlId);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if(table.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())==0) {
			try {
				int index = findGlobalQ(name,  false);
				if(index != -1) {
					CModelValue element = copasiDataModel.getModel().getModelValue(index);
					String sbmlId = element.getSBMLId();
					//System.out.println("current sbml id (global q) = "+sbmlId);
					this.globalqDB.updateSBMLid_fromCopasiDataModel(name, sbmlId);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
		
		
		
		
	}

	public Integer getGlobalQ_integerValue(String element) throws Throwable {
		try{
			Long value = Math.round(Double.parseDouble(element));
			return value.intValue(); // it's a normal number
		} catch(Exception ex) {
			//it's a variable
			GlobalQ limit = this.getGlobalQ(element);
			if(limit == null && element.contains("+") || element.contains("-") || element.contains("*") || element.contains("/")) {
				//maybe is an expression that need to be evaluated
				try {
					String expressionToEvaluate = evaluateExpression_justParse(element);
					//System.out.println("expression to evaluate = "+expressionToEvaluate);
					Integer ret = CellParsers.evaluateExpression(expressionToEvaluate);
					//System.out.println("Evaluates to = "+ret);
					return ret;
				} catch (Throwable e) {
					//e.printStackTrace();
					throw e;
				}
			} else {
				if(limit == null) {
					return null;
				}
				if(limit.getType() != Constants.GlobalQType.FIXED.copasiType) {
					throw new Exception("Global quantity is not fixed.");
				}
				try{
					Long value = Math.round(Double.parseDouble(limit.getInitialValue()));
					return value.intValue();
				} catch(Exception ex2) {
					 	String expressionToEvaluate = evaluateExpression_justParse(limit.getInitialValue());
						Integer ret = CellParsers.evaluateExpression(expressionToEvaluate);
						return ret;
								
				}
			}
		}
		
	}

	private String evaluateExpression_justParse(String element) throws Throwable {
		InputStream isR = new ByteArrayInputStream(element.getBytes("UTF-8"));
		  MR_Expression_Parser parserR = new MR_Expression_Parser(isR);
		  CompleteExpression rootR = parserR.CompleteExpression();
		  EvaluateExpressionVisitor vis = new EvaluateExpressionVisitor(this);
		 rootR.accept(vis);
		  if(vis.getExceptions().size() == 0) {
				return vis.getExpression();
				
			} else {
				throw vis.getExceptions().get(0);
			} 
	}

	public boolean isComplexSpecies(String name) {
		return speciesDB.isComplexSpecies(name);
	}

	public String getFirstCompartment() {
		try {
			return compDB.getAllNames().get(0).trim();
		} catch(Exception ex) {
			return null;
		}
	}

	public boolean isRangeVariableInMultistate(String species, String elementToSearch ) {
		
		if(!CellParsers.isMultistateSpeciesName(species)) {
			System.out.println("isRangeVariableInMultistate false");
			return false;
		}
		
		boolean ret = false;
		try {
			MultistateSpecies ms = new MultistateSpecies(this, species,false,false);
			ret = ms.containsRangeVariable(elementToSearch);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//System.out.println("isRangeVariableInMultistate " + ret);
		return ret;
	}

	
	HashMap<String, HashMap<String, String>> listOfAnnotations = new HashMap<String, HashMap<String, String>>();


	
	public void saveAnnotations(Vector<Vector<MutablePair<String, String>>> annotationToSave) {
		Vector<MutablePair<String, String>> elements = annotationToSave.get(0); //species
		String type = Constants.TitlesTabs.SPECIES.getDescription();
		HashMap<String, String> group = new HashMap<String, String>();
		for(MutablePair<String, String> el : elements) {	group.put(el.left, el.right); }
		listOfAnnotations.put(type, group);
		
		elements = annotationToSave.get(1); //reaction
		type = Constants.TitlesTabs.REACTIONS.getDescription();
		group = new HashMap<String, String>();
		for(MutablePair<String, String> el : elements) {	group.put(el.left, el.right);	}
		listOfAnnotations.put(type, group);
		
		elements = annotationToSave.get(2); //global quantities
		type = Constants.TitlesTabs.GLOBALQ.getDescription();
		group = new HashMap<String, String>();
		for(MutablePair<String, String> el : elements) {	group.put(el.left, el.right);	}
		listOfAnnotations.put(type, group);
		
	}

	
	private String fixAnnotation(String annotation, String newId) {
		if(annotation == null || annotation.length() == 0) return null;
		
		String delimiter = "rdf:about=";
		int indexAbout = annotation.indexOf(delimiter);
		int start  = indexAbout+delimiter.length()+1+1; //+1 quote, +1 hashsign
		String oldId = annotation.substring(start, annotation.indexOf("\"",start))	;
		String ret = annotation.replaceAll(oldId, newId);
		return ret;
	}

	

	
	
	
}
