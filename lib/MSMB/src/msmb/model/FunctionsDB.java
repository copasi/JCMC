package msmb.model;

import msmb.gui.MainGui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import msmb.parsers.mathExpression.syntaxtree.CompleteExpression;
import msmb.parsers.mathExpression.syntaxtree.CompleteFunctionDeclaration;
import msmb.parsers.mathExpression.visitor.GetFunctionCallsInEquation;
import msmb.parsers.mathExpression.visitor.GetFunctionNameVisitor;
import msmb.parsers.mathExpression.visitor.Look4UndefinedMisusedVisitor;
import msmb.parsers.mathExpression.visitor.RateLawMappingVisitor;

import msmb.utility.CellParsers;
import msmb.utility.Constants;
import msmb.utility.MyInconsistencyException;
import msmb.utility.MySyntaxException;

import org.COPASI.CFunction;
import org.omg.CORBA.Current;

import msmb.parsers.mathExpression.MR_Expression_Parser;
import msmb.parsers.mathExpression.MR_Expression_Parser_ReducedParserException;

import msmb.debugTab.DebugConstants;
import msmb.debugTab.DebugMessage;



public class FunctionsDB {
	MultiModel multiModel = null;
	public FunctionsDB(MultiModel mm) {		
		multiModel = mm;	
		userDefinedFun.put(0,null);//rows starts at index 1
		builtInFun.put(0,null);//rows starts at index 1
	}
	
	public static CellParsers parser  = new CellParsers();
	
	HashMap<Integer,Function> userDefinedFun = new HashMap<Integer, Function>(); 
	HashMap<Integer,Function> builtInFun = new HashMap<Integer, Function>(); 
	
	public HashMap<Integer, Vector> mappings = new HashMap<Integer, Vector>();
	public HashMap<String, HashSet<Integer>> whereFuncIsUsed = new HashMap<String, HashSet<Integer>>();
	
	HashMap<String, Vector> mappings_weight_globalQ_withSUM = new HashMap<String, Vector>();
	HashMap<String, Vector> mappings_speciesExpression = new HashMap<String, Vector>();
	
	HashMap<String, Vector> mappings_weight_subFunctions_Functions = new HashMap<String, Vector>(); 
	
	
	public Vector<String> correctRoles(Function f) throws MyInconsistencyException {
		String mathematicalExpression = f.getEquation();
		Vector ret = new Vector();
		InputStream is =null;
		  MR_Expression_Parser parser = null;
		  CompleteExpression root = null;
		try {
			is = new ByteArrayInputStream(mathematicalExpression.getBytes("UTF-8"));
			   parser = new MR_Expression_Parser(is,"UTF-8");
			  root = parser.CompleteExpression();
		} catch (Throwable e1) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e1.printStackTrace();
			return ret;
		}
		
		
		GetFunctionCallsInEquation v_fun = new GetFunctionCallsInEquation();
		root.accept(v_fun);
		HashMap<String, Vector> funCalls = v_fun.getFunctionCallsWithActualParam();
		Iterator it = funCalls.keySet().iterator();
			
		while(it.hasNext()) {
			String call = (String) it.next();
			Function called = null;
			try {
				called = multiModel.getFunctionByName(call);
			} catch (Throwable e) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)e.printStackTrace();
				called = null;
			
			}
			if(called == null) {
				ret.add(call);
				continue;
			}
			Vector<String> types = called.getParametersTypes();
			Vector names = funCalls.get(call);
			
			if(types.size() != names.size() ) {
				throw new MyInconsistencyException(Constants.FunctionsColumns.EQUATION.index, 
						"Incompatible usage of "+call+" in the expression of "+f.getName()+" function: subfunction is expecting " + types.size() +" parameters");
			}
			
			for(int i = 0; i< names.size(); i++) {
				String parName =(String) names.get(i);
				Integer called_role= Constants.FunctionParamType.getCopasiTypeFromSignatureType(types.get(i));
				Integer current_role= f.getParameterRole(parName);
				if(current_role == null) {
					if(called_role==Constants.FunctionParamType.VARIABLE.copasiType) { 
						continue;//is an expression, so the type should not be changed, each element in the expression will have a VAR type
					} else {
						//subfunction is expecting something specific, but the current state is an expression --> error
						throw new MyInconsistencyException(Constants.FunctionsColumns.EQUATION.index, 
								"Incompatible usage of "+parName+": subfunction is expecting a "+Constants.FunctionParamType.getSignatureDescriptionFromCopasiType(called_role)
								+ " not a complex expression.");
				
					}
				}
				if(current_role==Constants.FunctionParamType.VARIABLE.copasiType) {
					f.setParameterRole(parName, called_role.intValue());
				} else {
					if(called_role==Constants.FunctionParamType.VARIABLE.copasiType) { 
						continue;//in the called function, VAR is more permissive than in the caller, so it should be ok
					} else {
						if(current_role!=called_role) {
							throw new MyInconsistencyException(Constants.FunctionsColumns.EQUATION.index, 
									"Incompatible usage of parameter "+parName+": "+Constants.FunctionParamType.getSignatureDescriptionFromCopasiType(called_role)+ " in one subfuction, "
											+Constants.FunctionParamType.getSignatureDescriptionFromCopasiType(current_role)+" in another subfunction."   );
						}
					}
				}
				
			}
			
		}
		return ret;
		
	}
	
	
	public void addChangeFunction(int row, Function f) throws Exception {
	
		/*int existing = getIndex(f);
		if(existing != -1) {
			row = existing;
		} else {
			if(f.getType() == CFunction.UserDefined) row = userDefinedFun.size();
			else row = builtInFun.size();
		}
		
	*/	
		if(f.getType() == CFunction.UserDefined) {
			Function f2 = getUserDefinedFunctionByIndex(row);
			if(f2==null) {
				row = userDefinedFun.size();
			}
		}
		else {
			Function f2 = getBuiltInFunctionByIndex(row);
			if(f2==null) {
				row = builtInFun.size();
			}
		}
		
		if(f.getType() == CFunction.UserDefined) userDefinedFun.put(row, f); 
		else  builtInFun.put(row, f); 
		
		//recalculate mappings
		if(whereFuncIsUsed.get(f.getName())!=null) {
			HashSet<Integer> used = whereFuncIsUsed.get(f.getName());
			Iterator<Integer> it = used.iterator();
			
			while(it.hasNext()) {
				int row_reaction = it.next();
				Vector mapping_vector = mappings.get(row_reaction);
				if(mapping_vector == null) continue;
				Vector new_mapping_vector = new Vector();
				Vector<String> param_names = f.getParametersNames();
			//	Vector<String> param_roles = f.getParametersTypes();
				int j = 0;
				new_mapping_vector.add(f);
				for(int i = 1; i < mapping_vector.size(); i=i+2, j++) {
					new_mapping_vector.add(param_names.get(j));
					new_mapping_vector.add(mapping_vector.get(i+1));
				}
				mappings.put(row_reaction, new_mapping_vector);
			/*	if(!mapping_vector.equals(new_mapping_vector)) {
					DebugMessage dm = new DebugMessage();
					dm.setOrigin_table(Constants.TitlesTabs.REACTIONS.getDescription());
					dm.setProblem("The signature of function " + f.getName() + " has been modified. \nThe new mapping of reaction at row " + row+ " is " + new_mapping_vector.subList(1, new_mapping_vector.size()));
					dm.setPriority(DebugConstants.PriorityType.MINOR.priorityCode);
					dm.setOrigin_col(Constants.ReactionsColumns.KINETIC_LAW.index);
					dm.setOrigin_row(row_reaction+1);
					MainGui.addDebugMessage_ifNotPresent(dm);
				} else {
					try {
						MainGui.clear_debugMessages_relatedWith(Constants.TitlesTabs.REACTIONS.getDescription(), DebugConstants.PriorityType.MINOR.priorityCode, row_reaction+1, Constants.ReactionsColumns.KINETIC_LAW.index);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}*/
			}
		} else {
			try {
				MainGui.clear_debugMessages_relatedWith_table_col_priority(Constants.TitlesTabs.REACTIONS.getDescription(), 
						Constants.ReactionsColumns.KINETIC_LAW.index,DebugConstants.PriorityType.MINOR.priorityCode);
			} catch (Throwable e) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
			}
		}
	}
	
	public int getFunctionIndex(Function f){
		int ret = -1;
		if(f==null) return ret;
		if(f.getType() == CFunction.UserDefined) {
			Iterator<Integer> it = userDefinedFun.keySet().iterator();
			while(it.hasNext()) {
				Integer index = it.next();
				Function f2 = userDefinedFun.get(index);
				if(f2==null) continue;
				if(f.getName().compareTo(f2.getName()) == 0) {
					ret = index.intValue();
					break;
				}
			}
		} else {	
			Iterator<Integer> it = builtInFun.keySet().iterator();
		while(it.hasNext()) {
			Integer index = it.next();
			Function f2 = builtInFun.get(index);
			if(f2==null) continue;
			if(f.getName().compareTo(f2.getName()) == 0) {
				ret = index.intValue();
				break;
			}
		}
	}
		
		return ret;
	}

	public static String extractJustName(String functionNamePlusPossibleParameters) throws Exception {
		String ret = new String();
		if(functionNamePlusPossibleParameters.trim().length() == 0) return ret;
		InputStream is;
		try {
			is = new ByteArrayInputStream(functionNamePlusPossibleParameters.getBytes("UTF-8"));
	
		  MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
		  
	 	  CompleteFunctionDeclaration root = parser.CompleteFunctionDeclaration();
		  GetFunctionNameVisitor name = new GetFunctionNameVisitor();
		  root.accept(name);
		  ret = name.getFunctionName();
		  
		} catch (Throwable e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 	e.printStackTrace();
			throw new MySyntaxException(Constants.FunctionsColumns.NAME.index, e.getMessage(),Constants.TitlesTabs.FUNCTIONS.getDescription());
			//throw e;
		}
		return ret;
	}
	
	
	public Function getFunctionByName(String name) throws Exception {
		
		if(name.endsWith(")")) name = extractJustName(name);
		Iterator<Integer> it = userDefinedFun.keySet().iterator();
		while(it.hasNext()){
			int index = it.next();
			Function f = (Function)userDefinedFun.get(index);
			if(f == null) continue;
			if(f.getName().compareTo(name) == 0) {
				return f;
			}
		}
		return null;
	}
	
	public Function getBuiltInFunctionByName(String name) throws Exception {
		
		if(name.endsWith(")")) name = extractJustName(name);
		Iterator<Integer> it = builtInFun.keySet().iterator();
		while(it.hasNext()){
			int index = it.next();
			Function f = (Function)builtInFun.get(index);
			if(f == null) continue;
			if(f.getName().compareTo(name) == 0) {
				return f;
			}
		}
		return null;
	}
	
	
	
	public Vector<Object> addMapping(int row, String equation, String type)  {
		Vector<Object> ret = new Vector<Object>();
		if(equation.length() ==0) return ret;
		//System.out.println(equation);
		try {
				 
				InputStream is = new ByteArrayInputStream(equation.getBytes("UTF-8"));
				MR_Expression_Parser_ReducedParserException parser = new MR_Expression_Parser_ReducedParserException(is,"UTF-8");
				CompleteExpression root = parser.CompleteExpression();
				Look4UndefinedMisusedVisitor undefVisitor = new Look4UndefinedMisusedVisitor(multiModel);
				root.accept(undefVisitor);
				Vector<String> undef = undefVisitor.getUndefinedElements();
				Vector<String> misused = undefVisitor.getMisusedElements();
				
				misused.addAll(multiModel.checkUsageElementsOfReaction(undefVisitor.getUsedAsElements(),row));
				  
				 
				if(misused.size() != 0) {
					String message = new String();
					if(misused.size() > 0) message += "The following elements are misused: " +misused.toString();
					//throw new MySyntaxException(Constants.ReactionsColumns.KINETIC_LAW.index, message,Constants.TitlesTabs.REACTIONS.getDescription());
					ret.add(0,new MySyntaxException(Constants.ReactionsColumns.KINETIC_LAW.index, message,Constants.TitlesTabs.REACTIONS.getDescription()));
				}
				Vector<String> PARs = new Vector();
				if(type.compareTo(Constants.ReactionType.MASS_ACTION.getDescription())!=0) {
					InputStream is1 = new ByteArrayInputStream(equation.getBytes("UTF-8"));
					MR_Expression_Parser parser1 = new MR_Expression_Parser(is1,"UTF-8");
					CompleteExpression root1 = parser1.CompleteExpression();
					RateLawMappingVisitor vis = new RateLawMappingVisitor(multiModel,row, equation);
					root1.accept(vis);
					PARs = vis.getGlobalQ_PARtype();
					
				} else {
					PARs.addAll(undef);// the single parameter of a mass action kinetic (added 
				}

				String message = new String("Missing element definition: " );
				boolean found_non_PAR_missing = false;
				for(int i = 0; i <undef.size();i++ ) {
					String undef_maybePar = undef.get(i);
					if(PARs.contains(undef_maybePar)){
						ret.add(undef_maybePar);
					}
					else {
						found_non_PAR_missing = true;
						message += undef_maybePar +" ";
					}
				}
				if(found_non_PAR_missing) {
					//throw new MySyntaxException(Constants.ReactionsColumns.KINETIC_LAW.index, message,Constants.TitlesTabs.REACTIONS.getDescription());
					ret.add(new MySyntaxException(Constants.ReactionsColumns.KINETIC_LAW.index, message,Constants.TitlesTabs.REACTIONS.getDescription()));
					
				}
			/*else if(found_non_PAR_missing && MainGui.autocompleteWithDefaults) {
				return ret;
			}*/
		} catch (Exception e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
			ret.add(e);
			return ret;
			//throw e;
		}
		
		  
	
		return ret;
	}
	
	public Vector getMapping(int row) {
		return mappings.get(row);
	}

	public int getNumUserDefinedFunctions() {
		return this.userDefinedFun.size();
	}
	
	public int getNumBuiltInFunctions() {
		return this.builtInFun.size();
	}
	
	public void addMapping_weight_globalQ_withSUM(int row, MultistateSpecies sp, String weight_functionCall) throws Exception {
		int openBr = weight_functionCall.indexOf("(");
		while(openBr!=-1) {
			if(weight_functionCall.charAt(openBr-1)=='\\') { 
				openBr=weight_functionCall.indexOf("(",openBr+1);	
			} else {
				break ;
			}
		}		
		int closedBr = weight_functionCall.lastIndexOf(")");
		
		if(openBr==-1 || closedBr == -1) return;
		
		String funName = weight_functionCall.substring(0,openBr);
		
		Function f = this.getFunctionByName(funName);
		
		String param = weight_functionCall.substring(openBr+1, closedBr);
		StringTokenizer st = new StringTokenizer(param, " ,");
		Vector mapping_vector = new Vector();
		mapping_vector.add(f);
		Vector<String> param_names = f.getParametersNames();
		int i = 0;
		while(st.hasMoreTokens()) {
			String actualValue = st.nextToken();
			mapping_vector.add(param_names.get(i));
			mapping_vector.add(actualValue);
			i++;
		}
		mappings_weight_globalQ_withSUM.put(new String(row+"_"+sp.getSpeciesName()+"_"+funName), mapping_vector);
		return;
	}
	
	
	
	public void addMapping_speciesExpression(int row, String functionCall) throws Exception {
		int openBr = functionCall.indexOf("(");
		while(openBr!=-1) {
			if(functionCall.charAt(openBr-1)=='\\') { 
				openBr=functionCall.indexOf("(",openBr+1);	
			} else {
				break ;
			}
		}		
		int closedBr = functionCall.lastIndexOf(")");
		
		if(openBr==-1 || closedBr == -1) return;
		
		String funName = functionCall.substring(0,openBr);
		
		Function f = this.getFunctionByName(funName);
		
		String param = functionCall.substring(openBr+1, closedBr);
		StringTokenizer st = new StringTokenizer(param, " ,");
		Vector mapping_vector = new Vector();
		mapping_vector.add(f);
		Vector<String> param_names = f.getParametersNames();
		int i = 0;
		while(st.hasMoreTokens()) {
			String actualValue = st.nextToken();
			mapping_vector.add(param_names.get(i));
			mapping_vector.add(actualValue);
			i++;
		}
		mappings_speciesExpression.put(new String(row+"_"+funName), mapping_vector);
		return;
	}
	
	public Vector get_mappings_speciesExpression(int row, String functionCall) {
		int openBr = functionCall.indexOf("(");
		int closedBr = functionCall.lastIndexOf(")");
		while(openBr!=-1) {
			if(functionCall.charAt(openBr-1)=='\\') { 
				openBr=functionCall.indexOf("(",openBr+1);	
			} else {
				break ;
			}
		}		
		if(openBr==-1 || closedBr == -1) return new Vector();
		
		String funName = functionCall.substring(0,openBr);
		return mappings_speciesExpression.get(new String(row+"_"+funName));
	}
	
	public Vector get_mappings_weight_globalQ_withSUM(int row, MultistateSpecies sp, String weight_functionCall) {
		int openBr = weight_functionCall.indexOf("(");
		int closedBr = weight_functionCall.lastIndexOf(")");
		
		if(openBr==-1 || closedBr == -1) return new Vector();
		
		String funName = weight_functionCall.substring(0,openBr);
		return mappings_weight_globalQ_withSUM.get(new String(row+"_"+sp.getSpeciesName()+"_"+funName));
	}
		
	
	
	
	
	public void addMapping_subFunctions_Functions(int row, String functionCall, int index_subfunction) throws Exception {
		int openBr = functionCall.indexOf("(");
		while(openBr!=-1) {
			if(functionCall.charAt(openBr-1)=='\\') { 
				openBr=functionCall.indexOf("(",openBr+1);	
			} else {
				break ;
			}
		}		
		int closedBr = functionCall.lastIndexOf(")");
		
		if(openBr==-1 || closedBr == -1) return;
		
		String funName = functionCall.substring(0,openBr);
		
		Function f = this.getFunctionByName(funName);
		
		String param = functionCall.substring(openBr+1, closedBr);
		StringTokenizer st = new StringTokenizer(param, " ,"); //JUST ONE LEVEL: FUNCTION = 1 + SUBFUNCTION(1,2) ... no FUNCTION = 1 + SUBFUNCTION(SUBSUBFUNCTION(1,2))
		Vector mapping_vector = new Vector();
		mapping_vector.add(f);
		Vector<String> param_names = f.getParametersNames();
		int i = 0;
		while(st.hasMoreTokens()) {
			String actualValue = st.nextToken();
			mapping_vector.add(param_names.get(i));
			mapping_vector.add(actualValue);
			i++;
		}
		mappings_weight_subFunctions_Functions.put(new String(row+"_"+funName+"_"+index_subfunction), mapping_vector);
		return;
	}
	
	public Vector get_mappings_subFunctions_Functions(int row, String subfunctionCall, int index_subfunction) {
		int openBr = subfunctionCall.indexOf("(");
		int closedBr = subfunctionCall.lastIndexOf(")");
		
		if(openBr==-1 || closedBr == -1) return new Vector();
		
		String funName = subfunctionCall.substring(0,openBr);
		return mappings_weight_subFunctions_Functions.get(new String(row+"_"+funName+"_"+index_subfunction));
	}

	
	public int getIndex(Function f) throws Exception {
		if(f==null) return -1;
		String funName = f.getName(); //extractJustName(f.getName());
		if(f.getType() == CFunction.UserDefined) {
			Iterator<Integer> it = userDefinedFun.keySet().iterator();
			while(it.hasNext()) {
				Integer index = it.next();
				Function f2 = userDefinedFun.get(index);
				if(f2==null) continue;
				if(f2.getName().compareTo(funName)==0) return index;
			}
		} else {
			Iterator<Integer> it = builtInFun.keySet().iterator();
			while(it.hasNext()) {
				Integer index = it.next();
				Function f2 = builtInFun.get(index);
				if(f2==null) continue;
				if(f2.getName().compareTo(funName)==0) return index;
			}
		}
		return -1;
	}
	
	public Integer getFunctionIndex(String name) {
		Iterator<Integer> it = userDefinedFun.keySet().iterator();
		while(it.hasNext()) {
			Integer index = it.next();
			Function f2 = userDefinedFun.get(index);
			if(f2==null) continue;
			if(f2.getName().compareTo(name)==0) return index;
		}
		return -1;
	}

	public Function getUserDefinedFunctionByIndex(int i) {
		return userDefinedFun.get(new Integer(i));
	}

	public Function getBuiltInFunctionByIndex(int i) {
		return builtInFun.get(new Integer(i));
	}

	
	public Collection<Function> getAllFunctions() {
		List<Function> mapValues2 = new ArrayList<Function>(this.builtInFun.values());
		//Collections.sort(mapValues2);
		
		List<Function> mapValues = new ArrayList<Function>(this.userDefinedFun.values());
		//Collections.sort(mapValues);
		
		
		mapValues2.addAll(mapValues);
		return mapValues2;
	}
	
	public Vector<Function> getAllUserDefinedFunctions() {
		List<Function> mapValues = new ArrayList<Function>(this.userDefinedFun.values());
		return new Vector(mapValues);
	}
	
	public Vector<Function> getAllBuiltInFunctions() {
		List<Function> mapValues = new ArrayList<Function>(this.builtInFun.values());
		return new Vector(mapValues);
	}
	
	public Collection<Function> getUsedFunctions() {
		Collection<Function> ret = new Vector<Function>();
		HashSet<String> printed = new HashSet<String>();
		
		
		Iterator it2 = this.userDefinedFun.values().iterator();
		while(it2.hasNext()){
			Function f = (Function) it2.next();
			if(printed.contains(f.getName())) continue;
			if(f.getType() == CFunction.PreDefined && f.toShow()==false) continue;
			ret.add(f);
			printed.add(f.getName());
		}
		
		
		
		return ret;
	}
	
	
	
	public boolean removeFunction(int toBeRemoved) {
		
		int size = userDefinedFun.size();
		Function fun = this.getUserDefinedFunctionByIndex(toBeRemoved+1);
		userDefinedFun.remove(toBeRemoved+1);
		whereFuncIsUsed.remove(fun.getName());
		multiModel.removeNamedElement(fun.getName(),new Integer(Constants.TitlesTabs.FUNCTIONS.index));
	
		boolean moved = false;
		for(int i = toBeRemoved+1; i < size; i++) {
			Vector succ = mappings.get(i+1);
			Function func = userDefinedFun.get(i+1);
			if(func==null) {
				if(moved){
					userDefinedFun.remove(userDefinedFun.size()-1);
					mappings.remove(userDefinedFun.size()-1);
				}
				break; 
			}
			mappings.put(new Integer(i), succ);
			userDefinedFun.put(new Integer(i), func);
			moved = true;
		}
		return true;
}
	
	public void clear() {
		userDefinedFun.clear();
		userDefinedFun.put(0, null);
		mappings.clear();
		whereFuncIsUsed.clear();
		mappings_weight_globalQ_withSUM.clear();
		mappings_speciesExpression.clear();
		mappings_weight_subFunctions_Functions.clear();
	}


	public void removeRateLawMappingForRow(int row) {
		Iterator<String> it = whereFuncIsUsed.keySet().iterator();
		while(it.hasNext()) {
			String key =  it.next();
			HashSet<Integer> current = whereFuncIsUsed.get(key);
			if(current.contains(new Integer(row))) {
					current.remove(new Integer(row));
					whereFuncIsUsed.put(key, current);
			}
			
			
		}
		
	}

	


}
