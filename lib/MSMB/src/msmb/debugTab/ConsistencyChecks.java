package  msmb.debugTab;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import msmb.gui.CustomTableModel_MSMB;
import msmb.gui.MainGui;
import msmb.model.*;
import msmb.utility.CellParsers;
import msmb.utility.Constants;
import msmb.utility.Constants.TitlesTabs;


import org.COPASI.CFunctionParameter;
import org.apache.commons.lang3.tuple.MutablePair;

import  msmb.parsers.mathExpression.MR_Expression_Parser;
import  msmb.parsers.mathExpression.syntaxtree.CompleteExpression;
import  msmb.parsers.mathExpression.syntaxtree.SingleFunctionCall;
import msmb.parsers.mathExpression.visitor.EvaluateExpressionVisitor;
import  msmb.parsers.mathExpression.visitor.GetFunctionNameVisitor;
import  msmb.parsers.mathExpression.visitor.Look4UndefinedMisusedVisitor;
import msmb.parsers.mathExpression.visitor.ToStringVisitor;

public class ConsistencyChecks {

	
	private static final HashSet<Integer> optionalColumns_Reactions;
	static {
		optionalColumns_Reactions = new HashSet<Integer>();
		optionalColumns_Reactions.add(Constants.ReactionsColumns.NAME.index);
		optionalColumns_Reactions.add(Constants.ReactionsColumns.EXPANDED.index);
		optionalColumns_Reactions.add(Constants.ReactionsColumns.NOTES.index);
	}
	
	private static final HashSet<Integer> suggestedColumns_Reactions;
	static {
		suggestedColumns_Reactions = new HashSet<Integer>();
		suggestedColumns_Reactions.add(Constants.ReactionsColumns.KINETIC_LAW.index);
		suggestedColumns_Reactions.add(Constants.ReactionsColumns.TYPE.index);
	}
	
	private static final HashSet<Integer> suggestedColumns_Species;
	static {
		suggestedColumns_Species  = new HashSet<Integer>();
		suggestedColumns_Species.add(Constants.SpeciesColumns.INITIAL_QUANTITY.index);
	}
	
	private static final HashSet<Integer> optionalColumns_Species;
	static {
		optionalColumns_Species = new HashSet<Integer>();
		optionalColumns_Species.add(Constants.SpeciesColumns.EXPRESSION.index);
		optionalColumns_Species.add(Constants.SpeciesColumns.NOTES.index);
	}
	
	private static final HashSet<Integer> optionalColumns_GlobalQ;
	static {
		optionalColumns_GlobalQ = new HashSet<Integer>();
		optionalColumns_GlobalQ.add(Constants.GlobalQColumns.EXPRESSION.index);
		optionalColumns_GlobalQ.add(Constants.GlobalQColumns.NOTES.index);
	}
	
	private static final HashSet<Integer> suggestedColumns_GlobalQ;
	static {
		suggestedColumns_GlobalQ  = new HashSet<Integer>();
		suggestedColumns_GlobalQ.add(Constants.GlobalQColumns.VALUE.index);
	}
	
	private static final HashSet<Integer> optionalColumns_Functions;
	static {
		optionalColumns_Functions = new HashSet<Integer>();
		optionalColumns_Functions.add(Constants.FunctionsColumns.PARAMETER_ROLES.index);
		//optionalColumns_Functions.add(Constants.FunctionsColumns.SIGNATURE.index);
		optionalColumns_Functions.add(Constants.FunctionsColumns.NOTES.index);
	}
	
	private static final HashSet<Integer> optionalColumns_Compartments;
	static {
		optionalColumns_Compartments = new HashSet<Integer>();
		optionalColumns_Compartments.add(Constants.CompartmentsColumns.EXPRESSION.index);
		optionalColumns_Compartments.add(Constants.CompartmentsColumns.NOTES.index);
	}
	
	private static final HashSet<Integer> optionalColumns_Events;
	static {
		optionalColumns_Events = new HashSet<Integer>();
		optionalColumns_Events.add(Constants.EventsColumns.NAME.index);
		optionalColumns_Events.add(Constants.EventsColumns.DELAY.index);
		optionalColumns_Events.add(Constants.EventsColumns.NOTES.index);
	}
	
	private static final HashSet<Integer> suggestedColumns_Events;
	static {
		suggestedColumns_Events = new HashSet<Integer>();
		suggestedColumns_Events.add(Constants.EventsColumns.ACTIONS.index);
	}
	
	public static HashSet<String> emptyFields = new HashSet<String>();
	public static HashSet<String> emptyNonMandatoryFields = new HashSet<String>();
	
	public static void put_EmptyFields(String table_name, Integer row, Integer col) {
			String key = table_name+"_"+row+"_"+col;
			boolean addedInEmptyFields = false;
			boolean addedInEmptyNonMandatoryFields = false;
			if(table_name.compareTo(Constants.TitlesTabs.REACTIONS.getDescription()) == 0) {
				if(!optionalColumns_Reactions.contains(col)) {	
					if(suggestedColumns_Reactions.contains(col)){
						addedInEmptyNonMandatoryFields = true;
						emptyNonMandatoryFields.add(key);
					}
					else  {
						emptyFields.add(key); 
						addedInEmptyFields = true;
					}
				}
			
			} else if(table_name.compareTo(Constants.TitlesTabs.SPECIES.getDescription()) == 0) {
				if(!optionalColumns_Species.contains(col)) {	
					if(suggestedColumns_Species.contains(col)){
						addedInEmptyNonMandatoryFields = true;
						emptyNonMandatoryFields.add(key);
					}
					else  {
						emptyFields.add(key); 
						addedInEmptyFields = true;
					}
				}
				//if(!optionalColumns_Species.contains(col)) {	emptyFields.add(key); addedInEmptyFields = true;} 
			}else if(table_name.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription()) == 0) {
				if(!optionalColumns_Compartments.contains(col)) {	emptyFields.add(key); addedInEmptyFields = true;}
			}else if(table_name.compareTo(Constants.TitlesTabs.EVENTS.getDescription()) == 0) {
				if(!optionalColumns_Events.contains(col)) {	
					if(suggestedColumns_Events.contains(col)){
						addedInEmptyNonMandatoryFields = true;
						emptyNonMandatoryFields.add(key);
					}
					else  {
						emptyFields.add(key); 
						addedInEmptyFields = true;
					}
				}
			}else if(table_name.compareTo(Constants.TitlesTabs.FUNCTIONS.getDescription()) == 0) {
				if(!optionalColumns_Functions.contains(col)) {	emptyFields.add(key); addedInEmptyFields = true;}
			}else if(table_name.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription()) == 0) {
				if(!optionalColumns_GlobalQ.contains(col)) {	
					if(suggestedColumns_GlobalQ.contains(col)){
						addedInEmptyNonMandatoryFields = true;
						emptyNonMandatoryFields.add(key);
					}
					else  {
						emptyFields.add(key); 
						addedInEmptyFields = true;
					}
				}
				//if(!optionalColumns_GlobalQ.contains(col)) {	emptyFields.add(key); addedInEmptyFields = true;}
			}
			
			if(addedInEmptyFields) {
				DebugMessage dm = new DebugMessage();
				dm.setOrigin_table(table_name);
				dm.setProblem("Empty mandatory field");
				dm.setPriority(DebugConstants.PriorityType.EMPTY.priorityCode);
				dm.setOrigin_col(col);
				dm.setOrigin_row(row);
				MainGui.addDebugMessage_ifNotPresent(dm);
			} 
			if(addedInEmptyNonMandatoryFields) {
				DebugMessage dm = new DebugMessage();
				dm.setOrigin_table(table_name);
				dm.setProblem("Empty important non-mandatory field");
				dm.setPriority(DebugConstants.PriorityType.MINOR_EMPTY.priorityCode);
				dm.setOrigin_col(col);
				dm.setOrigin_row(row);
				MainGui.addDebugMessage_ifNotPresent(dm);
			}
	}
	
	public static void remove_EmptyFields(String table_name, Integer row, Integer col) {
		emptyFields.remove(table_name+"_"+row+"_"+col);
		emptyNonMandatoryFields.remove(table_name+"_"+row+"_"+col);
		MainGui.clear_debugMessages_relatedWith(table_name,DebugConstants.PriorityType.EMPTY.priorityCode,row,col);
		MainGui.clear_debugMessages_relatedWith(table_name,DebugConstants.PriorityType.MINOR_EMPTY.priorityCode,row,col);
	}
	
	
	
		
	
	public static Vector<String>  all_parameters_in_functionCalls_exist(int whichTable, MultiModel multiModel, String functionCall, int rowIndex)throws Exception {
		Vector<?> paramMapping = new Vector<Object>();
		if(functionCall.trim().length() ==0) return new Vector();
		if(whichTable == TitlesTabs.REACTIONS.index) {
			paramMapping = (Vector<?>) multiModel.funDB.getMapping(rowIndex);
		} else if(whichTable == TitlesTabs.SPECIES.index) {
			paramMapping = 	multiModel.funDB.get_mappings_speciesExpression(rowIndex, functionCall);
		}
		
		if(paramMapping== null) {
			//the function itself in undefined
			Vector ret = new Vector();
			try{
				InputStream is = new ByteArrayInputStream(functionCall.getBytes("UTF-8"));
				MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
				SingleFunctionCall root = parser.SingleFunctionCall();
				GetFunctionNameVisitor vis = new GetFunctionNameVisitor();
				root.accept(vis);
				ret.add(vis.getFunctionName());
				DebugMessage dm = new DebugMessage();
				 dm.setOrigin_table(Constants.TitlesTabs.REACTIONS.getDescription());
				 dm.setProblem("Missing function definition: " + vis.getFunctionName());
				 dm.setPriority(DebugConstants.PriorityType.MISSING.priorityCode);
				 dm.setOrigin_col(Constants.ReactionsColumns.KINETIC_LAW.index);
				 dm.setOrigin_row(rowIndex+1);
				 MainGui.addDebugMessage_ifNotPresent(dm);
				return ret;
			} catch(Exception ex) {
				//this is not a function call but it is a single parameter, its existance is checked elsewhere
				//ex.printStackTrace();
			}
		} else {//the mapping exist but maybe the function name has been changed!
			
			String funName = ((Function) paramMapping.get(0)).getName();
			try{
				InputStream is = new ByteArrayInputStream(functionCall.getBytes("UTF-8"));
				MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
				SingleFunctionCall root = parser.SingleFunctionCall();
				GetFunctionNameVisitor vis = new GetFunctionNameVisitor();
				root.accept(vis);
				String currentName = vis.getFunctionName();
				if(currentName.compareTo(funName)!=0) {
					DebugMessage dm = new DebugMessage();
					 dm.setOrigin_table(Constants.TitlesTabs.REACTIONS.getDescription());
					 dm.setProblem("Missing function definition: " + vis.getFunctionName());
					 dm.setPriority(DebugConstants.PriorityType.MISSING.priorityCode);
					 dm.setOrigin_col(Constants.ReactionsColumns.KINETIC_LAW.index);
					 dm.setOrigin_row(rowIndex+1);
					 MainGui.addDebugMessage_ifNotPresent(dm);
				}
			} catch(Exception ex) {
				//ex.printStackTrace();
			}
			
		}

		return all_parameters_in_functionCalls_exist(multiModel,rowIndex,paramMapping);
		
	}
	
	
	private static Vector<String> all_parameters_in_functionCalls_exist(MultiModel multiModel, int rowIndex, Vector<?> paramMapping) throws Exception {
			Vector<String> missingParameters = new Vector<String>();
			
			if(paramMapping == null) return missingParameters;
			Function f = (Function)paramMapping.get(0);
			Vector<Integer> paramRoles =  f.getParametersTypes_CFunctionParameter();
			Vector<Integer> roleVector = new Vector<Integer>();
			roleVector.add(CFunctionParameter.PARAMETER);
			roleVector.add(CFunctionParameter.SUBSTRATE);
			roleVector.add(CFunctionParameter.PRODUCT);
			roleVector.add(CFunctionParameter.MODIFIER);
			roleVector.add(CFunctionParameter.VOLUME);
			roleVector.add(Constants.SITE_FOR_WEIGHT_IN_SUM);
			roleVector.add(Constants.ROLE_EXPRESSION);
			
			for(int iii = 1, jjj = 0; iii < paramMapping.size(); iii=iii+2,jjj++) {
					boolean errorAlreadyAdded = false;
					//String parameterNameInFunction = (String)paramMapping.get(iii);
					String actualModelParameter = (String)paramMapping.get(iii+1);
					String actualModelParameterWithExtensions = new String(actualModelParameter);
					MutablePair<String, Vector<String>> nameAndPossibleExtensions = CellParsers.extractNameExtensions(actualModelParameter);
					
					Vector<String> extensions = nameAndPossibleExtensions.right;
					actualModelParameter = nameAndPossibleExtensions.left;
							
					int role = paramRoles.get(jjj);
					
					boolean checkAllRoles = false;
					int indexRole = 0;
					if(role==CFunctionParameter.VARIABLE) { checkAllRoles  = true; role= (Integer) roleVector.get(indexRole);}
					do{
						try {
							Vector<String> undef = null;
							switch(role) {
							case CFunctionParameter.PARAMETER:     
								try{
									Double.parseDouble(actualModelParameter);
									checkAllRoles = false;
								} catch(NumberFormatException ex) { //the parameter is not a number but a globalQ
									if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
									GlobalQ value = multiModel.getGlobalQ(actualModelParameter);
									if(value == null) {
										throw new NullPointerException();
									}
									if(extensions!= null && extensions.size() > 0) {
										String kind = CellParsers.extractKindQuantifier_fromExtensions(extensions);
										if(kind.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())!=0) throw new NullPointerException();
									}
									checkAllRoles = false;
								}
								break;
							case CFunctionParameter.SUBSTRATE: 
							case CFunctionParameter.PRODUCT:  
							case CFunctionParameter.MODIFIER:   
								
								Species sp = multiModel.getSpecies(actualModelParameter);
								if(sp == null) {
									throw new NullPointerException();
								}
								if(extensions!= null && extensions.size() > 0) {
									String kind = CellParsers.extractKindQuantifier_fromExtensions(extensions);
									if(kind.compareTo(Constants.TitlesTabs.SPECIES.getDescription())!=0) throw new NullPointerException();
								}
								checkAllRoles = false;
								break;
							case CFunctionParameter.VOLUME:    
								Compartment c = multiModel.getComp(actualModelParameter);
								if(c == null) throw new NullPointerException();
								checkAllRoles = false;
								break;
							case Constants.ROLE_EXPRESSION:  
								if(actualModelParameter.length() >0) {
									  InputStream is = new ByteArrayInputStream(actualModelParameter.getBytes("UTF-8"));
									  MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
									  CompleteExpression root = parser.CompleteExpression();
									  Look4UndefinedMisusedVisitor undefVisitor = new Look4UndefinedMisusedVisitor(multiModel);
									  root.accept(undefVisitor);
									  undef = undefVisitor.getUndefinedElements();
								}
								if(undef == null || undef.size() ==0)	{checkAllRoles = false; }
								else throw new NullPointerException();
								break;
							case CFunctionParameter.TIME:    
								checkAllRoles = false;
								break;
							case Constants.SITE_FOR_WEIGHT_IN_SUM:
								if(multiModel.checkUsageOfSiteType(rowIndex, actualModelParameterWithExtensions,checkAllRoles)) {
									checkAllRoles = false; 
								} else {
									if(!checkAllRoles) { //error already added by subroutine
										errorAlreadyAdded = true;
										MainGui.updateDebugTab();
										missingParameters.add(actualModelParameter);
									}
									throw new NullPointerException();
								}
								break;
							default: 
								//System.out.println("missing parameter role in function, for actual value " + actualModelParameter);
								throw new NullPointerException();
							}
						} catch(Exception ex) {
							if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
							if(checkAllRoles==false) throw ex;
							indexRole++;
							try {
								role = (Integer) roleVector.get(indexRole);
							} catch(Exception ex2) {
								if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 	ex2.printStackTrace();
								if(!errorAlreadyAdded) {
									DebugMessage dm = new DebugMessage();
									 dm.setOrigin_table(Constants.TitlesTabs.REACTIONS.getDescription());
									 dm.setProblem("Missing element definition: " + actualModelParameter);
									 dm.setPriority(DebugConstants.PriorityType.MISSING.priorityCode);
									 dm.setOrigin_col(Constants.ReactionsColumns.KINETIC_LAW.index);
									 dm.setOrigin_row(rowIndex+1);
									 MainGui.addDebugMessage_ifNotPresent(dm);
									//System.out.println("Missing parameter definition: "+actualModelParameter);
									MainGui.updateDebugTab();
									missingParameters.add(actualModelParameter);
								}
								break;
							}
						}
					}while(checkAllRoles || indexRole >=roleVector.size());
				}
			
			
				return missingParameters;
	}
	
	
	//FOR NOW I'M USING THE TABLE REACTION MODEL BUT NEED TO BE CHANGED ONCE THERE WILL BE
	//A DATA STRUCTURE FOR THE REACTIONS IN THE MULTIMODEL
	public static boolean all_elements_in_reaction_exist(MultiModel multiModel, CustomTableModel_MSMB tableReactionmodel) throws Exception{
		Vector<String> missingSpecies = new Vector<String>();
		
		
		if(tableReactionmodel!= null ) {
			for(int i = 0; i < tableReactionmodel.getRowCount()-1 ; i++ ) {
				String string_reaction = ((String)tableReactionmodel.getValueAt(i, 2)).trim();
				if(string_reaction.trim().length() <= 0) continue;

				missingSpecies.addAll(all_parameters_in_functionCalls_exist(Constants.TitlesTabs.REACTIONS.index, multiModel, (String)tableReactionmodel.getValueAt(i, Constants.ReactionsColumns.KINETIC_LAW.index), i));
				
						
				//boolean parseErrors = false;
				Vector<?> metabolites = new Vector<Object>();
				Vector<?> singleConfigurations = new Vector<Object>();
				try{ 
					metabolites = CellParsers.parseReaction(multiModel,string_reaction,i+1);
					HashMap<String, String> aliases = CellParsers.getAllAliases(string_reaction);
					HashMap<Integer, String> aliases2 = CellParsers.getAllAliases_2(string_reaction);
					
					singleConfigurations = multiModel.expandReaction(metabolites,aliases,aliases2,i);
				} catch(Throwable ex) {
					//System.out.println("PROBLEMS WITH REACTION "+string_reaction);
					if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 	ex.printStackTrace();
					 DebugMessage dm = new DebugMessage();
					 dm.setOrigin_table(Constants.TitlesTabs.REACTIONS.getDescription());
					dm.setProblem("Problem parsing the reaction string.\n" + ex.getMessage());
				    dm.setPriority(DebugConstants.PriorityType.PARSING.priorityCode);
					 dm.setOrigin_col(Constants.ReactionsColumns.REACTION.index);
					 dm.setOrigin_row(i+1);
					 MainGui.addDebugMessage_ifNotPresent(dm,false);
					//parseErrors = true;
					continue;
				}
	
				
				for(int j = 0; j < singleConfigurations.size(); j++) {
					Vector<?> expandedReaction = (Vector<?>) singleConfigurations.get(j);

					Vector<?> subs = (Vector<?>)expandedReaction.get(0);
					Vector<?> prod =(Vector<?>)expandedReaction.get(1);
					Vector<?> mod = (Vector<?>)expandedReaction.get(2);

					for(int i1 = 0; i1 < subs.size(); i1++) {
						String s = (String)subs.get(i1);
						s = multiModel.extractName(s);
						if(!multiModel.containsSpecies(s))  {
							 DebugMessage dm = new DebugMessage();
							 dm.setOrigin_table(Constants.TitlesTabs.REACTIONS.getDescription());
							 dm.setProblem("Missing species definition: " + s);
							 dm.setPriority(DebugConstants.PriorityType.MISSING.priorityCode);
							 dm.setOrigin_col(Constants.ReactionsColumns.REACTION.index);
							 dm.setOrigin_row(i+1);
							 MainGui.addDebugMessage_ifNotPresent(dm);
							missingSpecies.add(s);
						} else {
							Species sp = multiModel.getSpecies(s);
							if(sp instanceof MultistateSpecies) {
								MultistateSpecies m = (MultistateSpecies)sp;
								if(!m.containsSpecificConfiguration(s)) {
									DebugMessage dm = new DebugMessage();
									 dm.setOrigin_table(Constants.TitlesTabs.REACTIONS.getDescription());
									 dm.setProblem("Missing species definition: " + s);
									 dm.setPriority(DebugConstants.PriorityType.MISSING.priorityCode);
									 dm.setOrigin_col(Constants.ReactionsColumns.REACTION.index);
									 dm.setOrigin_row(i+1);
									 MainGui.addDebugMessage_ifNotPresent(dm);
									missingSpecies.add(s);
								}
							} else {
								multiModel.checkIfInMultipleCompartmentsHasProperCompLabel(s,sp,i+1);
							}
						}
					}
					
					for(int i1 = 0; i1 < prod.size(); i1++) {
						String s = (String)prod.get(i1);
						s = multiModel.extractName(s);
						if(!multiModel.containsSpecies(s))  {
							DebugMessage dm = new DebugMessage();
							 dm.setOrigin_table(Constants.TitlesTabs.REACTIONS.getDescription());
							 dm.setProblem("Missing species definition: " + s);
							 dm.setPriority(DebugConstants.PriorityType.MISSING.priorityCode);
							 dm.setOrigin_col(Constants.ReactionsColumns.REACTION.index);
							 dm.setOrigin_row(i+1);
							 MainGui.addDebugMessage_ifNotPresent(dm);
							missingSpecies.add(s);
						} 
					}
					
					for(int i1 = 0; i1 < mod.size(); i1++) {
						String s = (String)mod.get(i1);
						//modifiers can depend on reactant and be calculated with dependent sum (so no explicit states)
						s = CellParsers.extractMultistateName(s);
						if(!multiModel.containsSpecies(s))  {
							DebugMessage dm = new DebugMessage();
							 dm.setOrigin_table(Constants.TitlesTabs.REACTIONS.getDescription());
							 dm.setProblem("Missing species definition: " + s);
							 dm.setPriority(DebugConstants.PriorityType.MISSING.priorityCode);
							 dm.setOrigin_col(Constants.ReactionsColumns.REACTION.index);
							 dm.setOrigin_row(i+1);
							 MainGui.addDebugMessage_ifNotPresent(dm);
							missingSpecies.add(s);
						} 
					}
					
					
				}
			}
			
			if(missingSpecies.size() >0) {
				throw new Exception("Error during validation: the following elements are not defined "+missingSpecies);
			} 
		}
		
		return true;
	}

	
		
	public static Vector<Object> missing_fields() {
		return new Vector<Object>(Arrays.asList(emptyFields.toArray()));
	}
	
	public static Vector<Object> missing_nonMandatory_fields() {
		return new Vector<Object>(Arrays.asList(emptyNonMandatoryFields.toArray()));
	}
	
	
	public static Vector<FoundElement> getDuplicateReactions() {
		Vector<FoundElement> ret = new Vector<FoundElement>();
		
		 HashMap<String, Vector> colData = new HashMap<String, Vector>();
		 HashSet<String> keyToReturn = new HashSet<String>();
		 for (int i = 0; i < MainGui.tableReactionmodel.getRowCount(); i++) {
		      //Vector row = (Vector) data.elementAt(i);
		      //String element = row.get(Constants.ReactionsColumns.REACTION.index).toString().trim();
			 String element =  MainGui.tableReactionmodel.getValueAt(i, Constants.ReactionsColumns.REACTION.index).toString().trim();
		      if(element.length() ==0) continue;
		      if(!colData.containsKey(element)) {
		    	  Vector<FoundElement> foundAt = new Vector<FoundElement>();
		    	  FoundElement fe = new FoundElement(Constants.TitlesTabs.REACTIONS.getDescription(), i+1, Constants.ReactionsColumns.REACTION.index);
		    	  foundAt.add(fe);
		    	  colData.put(element, foundAt);
		      } else {
		    	  Vector<FoundElement> foundAt = colData.get(element);
		     	  FoundElement fe = new FoundElement(Constants.TitlesTabs.REACTIONS.getDescription(), i+1, Constants.ReactionsColumns.REACTION.index);
		    	  foundAt.add(fe);
		    	  colData.put(element, foundAt);
		    	  keyToReturn.add(element);
		      }
		   }
		 
		 Iterator<String> it = keyToReturn.iterator();
		 while(it.hasNext()) {
			 String reaction = it.next();
			 ret.addAll(colData.get(reaction));
		 }
	
		 return ret;
	}
	
	
	public static void warnings_for_duplicate_reactions() {
		MainGui.clear_debugMessages_duplicatesReactions();
		Vector<FoundElement> duplicates = getDuplicateReactions();
		for(int i = 0; i < duplicates.size(); i++) {
			FoundElement fe = duplicates.get(i);
			DebugMessage dm = new DebugMessage();
			dm.setOrigin_table(fe.getTableDescription());
		    dm.setOrigin_col(fe.getCol());
		    dm.setOrigin_row(fe.getRow());
			dm.setProblem("Duplicate reactions! It may be a mistake! Please make sure that it is what you want!");
		    dm.setPriority(DebugConstants.PriorityType.DUPLICATES.priorityCode);
			MainGui.addDebugMessage_ifNotPresent(dm);
		}
	}
	
	public static boolean isOkEventTriggerExpression(String expression) throws Throwable {
		
		/*System.out.println("...........isOkEventTriggerExpression..............");
		System.out.println(expression);
		System.out.println(".................................");
	*/
		if(expression.trim().length()==0) return true;
		 boolean ret = false;
	      ByteArrayInputStream is2 = new ByteArrayInputStream(expression.getBytes("UTF-8"));
			  MR_Expression_Parser parser = new MR_Expression_Parser(is2,"UTF-8");
		  	  CompleteExpression start = parser.CompleteExpression();
		  	 EvaluateExpressionVisitor vis = new EvaluateExpressionVisitor(MainGui.multiModel,false);
		      start.accept(vis);
			  if(vis.getExceptions().size() == 0) {
				  ret  = vis.isBooleanExpression();
			  } else {
					throw vis.getExceptions().get(0);
				}
			  return ret;
		
	}

	

}
