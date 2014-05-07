package msmb.model;

import msmb.gui.MainGui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import msmb.utility.*;

import org.COPASI.*;


import msmb.parsers.mathExpression.MR_Expression_Parser;
import msmb.parsers.mathExpression.syntaxtree.CompleteExpression;
import msmb.parsers.mathExpression.syntaxtree.CompleteFunctionDeclaration;
import msmb.parsers.mathExpression.visitor.GetFunctionNameVisitor;
import msmb.parsers.mathExpression.visitor.GetUsedVariablesInEquation;
import msmb.parsers.mathExpression.visitor.ToStringVisitor;


public class Function implements Comparable<Function> {
	String name = new String();
    private static CellParsers parser = new CellParsers();
	
	HashMap<String, Integer> parametesRoles = new HashMap<String, Integer>();
	TreeMap<String, Integer> parametesOrder = new TreeMap<String, Integer>();
	String equation = new String();
	public String getEquation() {
		return equation;
	}

	public Integer getParameterRole(String parName) {
		return parametesRoles.get(parName);
	}

	
	public Function(Function f) {
		this.name = (f.name);
		this.parametesRoles.putAll(f.parametesRoles);
		this.parametesOrder.putAll(f.parametesOrder);
		this.equation = f.equation;
	}
	
	
	String compactedEquation = new String();
	String notes = new String();
	boolean show = true;

	public boolean toShow() {
		return show;
	}


	public void setShow(boolean show) {
		this.show = show;
	}


	int type = CFunction.MassAction; 
	int numSubstrates_ifMassAction = 0;
	//the other possibility is CFunction.UserDefined and will be used for all Copasi and userDefined functions
	
	public boolean isParameterDefined(String name) {
		return parametesOrder.containsKey(name);
	}
	

	public int getNumParam() {
		return parametesOrder.keySet().size();
	}
	
	public String getNotes() {
		return notes;
	}


	public void setNotes(String notes) {
		this.notes = notes;
	}


	public String printCompleteSignature() {
		Vector<String> t = getParametersTypes();
		Vector<String> n = getParametersNames();
		String ret = new String(this.getName());
		ret += "(";
		for(int i = 0; i < n.size(); i++) {
			ret += t.get(i) + " " + n.get(i) + ",";
		}
		if(n.size()>0) ret = ret.substring(0, ret.length()-1);
		ret += ")";
		return ret;
	}
	
	
	boolean completeFunSignatureInTable = false;
	
	public boolean isCompleteFunSignatureInTable() {
		return completeFunSignatureInTable;
	}
	public void setCompleteFunSignatureInTable(boolean completeFunSignatureInTable) {
		this.completeFunSignatureInTable = completeFunSignatureInTable;
	}

	
	public Function() {}
	
	public Function(String stringName) throws Exception { 
		
		InputStream is = new ByteArrayInputStream(stringName.getBytes("UTF-8"));
		MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
		CompleteFunctionDeclaration root = parser.CompleteFunctionDeclaration();
		GetFunctionNameVisitor nameV = new GetFunctionNameVisitor();
		root.accept(nameV);
		String funName  = nameV.getFunctionName();
		if(funName.length()==0) {
			throw new MySyntaxException(Constants.FunctionsColumns.NAME.index, "Wrong function definition", Constants.TitlesTabs.FUNCTIONS.getDescription());
		}
		
		int start = stringName.indexOf(funName)+funName.length();
		int end = stringName.length()-1;
		if(start < end) {
			String parameters = stringName.substring(start+1, end);
			if(parameters != null && parameters.length()>0) {
				assignParameterRoles(parameters);
				this.completeFunSignatureInTable = true;
			} 
		}
		
		
		
		
		this.name = new String(funName);
		this.type = CFunction.UserDefined;
		this.setEquation(new String(),type,0);

	}
		
		
	private void assignParameterRoles(String parameters) throws Exception {
		StringTokenizer st = new StringTokenizer(parameters, ", ");
		TreeMap<String, Integer> OLDparametesOrder = new TreeMap<String, Integer>();
		OLDparametesOrder.putAll(parametesOrder);
		parametesOrder.clear();
		TreeMap<String, Integer> OLDparametesRoles = new TreeMap<String, Integer>();
		OLDparametesRoles.putAll(parametesRoles);
		parametesRoles.clear();
		int i = 0;
		while(st.hasMoreElements()) {
			try {
				String type = st.nextToken();
				String param = st.nextToken();

				if(parametesOrder.containsKey(param)) {
					parametesOrder.clear();
					parametesOrder.putAll(OLDparametesOrder);
					parametesRoles.clear();
					parametesRoles.putAll(OLDparametesRoles);
					throw new MyChangeNotAllowedException(Constants.FunctionsColumns.NAME.index, this.printCompleteSignature(), "Change not allowed. Duplicate name \""+param+"\"");
				}

				this.parametesOrder.put(param, i);

				int role = Constants.FunctionParamType.getCopasiTypeFromSignatureType(type);
				if(role == Constants.FunctionParamType.MISSING.copasiType){
					throw new MySyntaxException(Constants.FunctionsColumns.NAME.index, "Role \""+type+"\" is not an available type. Choose between TOOOOOODOOOOOOOOO.", Constants.TitlesTabs.FUNCTIONS.getDescription());
				}
				else this.parametesRoles.put(param, role);

				i++;
			} catch(Exception ex) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 
					ex.printStackTrace();
				throw new MySyntaxException(Constants.FunctionsColumns.NAME.index, "Wrong signature syntax.",Constants.TitlesTabs.FUNCTIONS.getDescription());
			}
		}
	}

	
	public void setType(int CFunction_type, int numSubstrates_ifMassAction) {
		this.type = CFunction_type;
		if(type == CFunction.MassAction) {
			if (name.length()!=0)	this.name = "MassAction_"+name;
			else this.name = "MassAction";
		} 
		this.numSubstrates_ifMassAction = numSubstrates_ifMassAction;
		if(this.type == CFunction.PreDefined) this.show = MainGui.showAllAvailableFunctions;
		else this.show = true;
		
	}
	
	public int getType() {
		return this.type;
	}
	
	public void setEquation(String mathematicalExpression, int CFunction_type, int numSubstrates_ifMassAction) throws Exception {
		
		if(mathematicalExpression.length() == 0) {return;}
		
		this.setType(CFunction_type,numSubstrates_ifMassAction);
		
		if(this.type != CFunction.MassAction) {
			
			mathematicalExpression = CellParsers.cleanMathematicalExpression(mathematicalExpression);
			
			InputStream is = new ByteArrayInputStream(mathematicalExpression.getBytes("UTF-8"));
			  MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
			  CompleteExpression root = parser.CompleteExpression();
  			  GetUsedVariablesInEquation v = new GetUsedVariablesInEquation();
  			  root.accept(v);
  					  
			  this.equation =  ToStringVisitor.toString(root);
			  
			  Iterator it = entriesSortedByValues(v.getNames()).iterator();
			
			if(!this.completeFunSignatureInTable) {
				int order = 0;
				while(it.hasNext()){
					
					String n = ((Map.Entry<String,Integer>)it.next()).getKey();
					parametesOrder.put(n, new Integer(order));
					order++;
				}
				this.completeFunSignatureInTable = true;
			} else { // check if all the parameter in the equation are defined in the function already
				Vector undefined = new Vector();
				while(it.hasNext()){
					String inEquation = ((Map.Entry<String,Integer>)it.next()).getKey();
					if(!parametesOrder.containsKey(inEquation)) {
						undefined.add(inEquation);
					}
				}
				if(undefined.size() >0) {
					throw new MyInconsistencyException(Constants.FunctionsColumns.EQUATION.index, "Undefined variables: "+undefined.toString());
				}
				
			}
			
		
			
			
		} else { //massAction
			for(int i = 0; i < this.numSubstrates_ifMassAction; i++) {
				mathematicalExpression += " * _Subs_"+(i+1);
			}
			
			
			/*
			Node parsedExpression = parser.parse(mathematicalExpression);
			this.equation = parser.toString(parsedExpression);
			SymbolTable st = parser.getSymbolTable();
			Enumeration var = st.elements();
			TreeMap<String, Integer> TMPparametesOrder = new TreeMap<String, Integer>();
			while(var.hasMoreElements()) {
				String name = ((XVariable)var.nextElement()).getName();
				if(name.startsWith("_")) parametesRoles.put(name, CFunctionParameter.SUBSTRATE);
				else  parametesRoles.put(name, CFunctionParameter.PARAMETER);
				TMPparametesOrder.put(name, mathematicalExpression.indexOf(name));
			}
			
			Iterator it = entriesSortedByValues(TMPparametesOrder).iterator();
			int order = 0;
			while(it.hasNext()){
				parametesOrder.put(((Map.Entry<String,Integer>)it.next()).getKey(), new Integer(order));
				order++;
			}*/
			
		}
		
		fillMissingRoles();
		setCompactedEquation();
	}
	
	
	/*static boolean checkIfParJustSubstringOfOtherPar(String equation, int index, String par) {
		if(index == -1) return true;//the parameter is not there and it should not be added
		
		boolean noCharBefore = false;
		boolean noCharAfter = false;
		Operator[] operators = parser.getOperatorSet().getOperators();
		
		if(index == 0) { 
			noCharBefore = true;
		}
		else {
			String sub = equation.substring(index-1);
			if(sub.startsWith("(") || sub.startsWith(" ") || 
					sub.startsWith(",") || sub.startsWith(")")) noCharBefore = true;
			for(int i = 0; i < operators.length; i++) {
				if(sub.startsWith(operators[i].getSymbol())) {
					noCharBefore = true;
				}
			}
		}
		
		if(index+par.length() >= equation.length()) {
			noCharAfter = true;
		} else {
			String sub2 = equation.substring(index+par.length());
			if(sub2.startsWith("(") || sub2.startsWith(")") || sub2.startsWith(",") || sub2.startsWith(" ")) noCharAfter = true;
			for(int i = 0; i < operators.length; i++) {
					if(sub2.startsWith(operators[i].getSymbol())) {
						noCharAfter = true;
					}
			}
				
		}
		
		return !(noCharBefore && noCharAfter);
	}*/

	/*static int findRealIndexOf(String par, String mathematicalExpression) {
		int index = mathematicalExpression.indexOf(par);
		if(index == -1) return index;
		
		if((index+par.length()) > mathematicalExpression.length()) {
			return index;
		}
		
		String sub = mathematicalExpression.substring(index+par.length());
		boolean followedByParenthesisOrSpace = false;
		boolean followedByOperator = false;
		boolean precByOperator = false;
		boolean precByParehtesisOrSpace = false;
		if(index-1 >=0) {
			String prec = new String();
			prec += mathematicalExpression.charAt(index-1);
			if(prec.compareTo(" ")==0 || prec.compareTo(")")==0 ||
					prec.compareTo("(")==0 || prec.compareTo(",")==0  ) {
				precByOperator = true;
			}
		} else {
			if(index-1 == -1) precByParehtesisOrSpace = true;
		}
		
		if(sub == null || sub.length() == 0 || 
				sub.startsWith(" ") ||
				sub.startsWith(")") ||
				sub.startsWith("(") ||
				sub.startsWith(",")
		) {
			followedByParenthesisOrSpace = true;
			
		}
		Operator[] operators = parser.getOperatorSet().getOperators();
		
		for(int i = 0; i < operators.length; i++) {
				if(sub.startsWith(operators[i].getSymbol())) {
					followedByOperator = true;
				}
				if(index-1 >=0) {
					String prec = new String();
					prec += mathematicalExpression.charAt(index-1);
					if(prec.compareTo(operators[i].getSymbol())==0) {
						precByOperator = true;
					}
				}
		}
		
		if((precByOperator || precByParehtesisOrSpace) &&
			(followedByOperator || followedByParenthesisOrSpace)	) {
			return index;
		}
		
		return index+par.length()+findRealIndexOf(par, mathematicalExpression.substring(index+par.length())); //+1??
		
	}*/

	private void fillMissingRoles() {
		
		Iterator it = parametesOrder.keySet().iterator();
		while(it.hasNext()) {
			String name = (String) it.next();
			if(!parametesRoles.containsKey(name)) {
				parametesRoles.put(name, CFunctionParameter.VARIABLE);
			}
		}
		
	}

	private void setCompactedEquation() {
		compactedEquation = new String(this.name+"(");
		Iterator it = entriesSortedByValues(parametesOrder).iterator();
		boolean values = false;
		while(it.hasNext()){
			compactedEquation += ((Map.Entry<String,Integer>)it.next()).getKey() + ",";
			values = true;
		}
		if(values) compactedEquation = compactedEquation.substring(0,compactedEquation.length()-1);
		compactedEquation += ")";
	}
	
	public String getCompactedEquation(Vector parameterValues) throws Exception {
		if(parameterValues.size()==0) {
			return new String(compactedEquation);
		}
		if(parametesOrder.keySet().size() != parameterValues.size()) {
			throw new Exception("The function "+compactedEquation+" is not applicable for the arguments "+parameterValues);
		}
		else{
			String ret = new String();
			boolean values = false;
			for(int i = 0; i <parameterValues.size(); i++){
				ret += parameterValues.get(i) + ",";
				values = true;
			}
			if(values) ret = ret.substring(0,ret.length()-1);
			ret = this.name+"("+ret +")";
			return ret; 
		}
	}
	
	public String getExpandedEquation(Vector parameterValues) throws Exception {
		if(parameterValues.size()==0) {
			return new String(equation);
		}
		if(parametesOrder.keySet().size() != parameterValues.size()) {
			throw new Exception("The function "+compactedEquation+" is not applicable for the arguments "+parameterValues);
		}
		else{
			
			
			
			Iterator it1 = entriesSortedByValues(parametesOrder).iterator();
			String newExpression = new String(equation);
			int index = 0;
			
			while(it1.hasNext()){
				String name = ((Map.Entry<String,Integer>)it1.next()).getKey();
				String value = "__"+index+"__";
			//	System.out.println("name: "+ name);
			//	System.out.println("value: "+ value);
				newExpression = CellParsers.replaceVariableInExpression(newExpression,name,value,false);
				index++;
			}
			
			int max_value = index;
			index = 0;
			for(index = 0; index < max_value;index++){
				String name = "__"+index+"__";
				String value = (String) parameterValues.get(index);
			//	System.out.println("name: "+ name);
			//	System.out.println("value: "+ value);
				newExpression = CellParsers.replaceVariableInExpression(newExpression,name,value,false);
			}
			
		//	System.out.println("newExpression: "+ newExpression);
			if(newExpression.trim().length() == 0) {
				JOptionPane.showMessageDialog(null,"ExpandedEquation empty for a function!!! from equation="+equation, "Problem", JOptionPane.ERROR_MESSAGE);
			}
			return newExpression;
		}
	}
	
	
	
	public Integer getIndex(String name) {
		return this.parametesOrder.get(name);
	}
	
	
	
	public Vector<String> getParametersTypes() {
		
		Vector<String> ret = new Vector();
		if(parametesRoles== null) return ret;
			
		Iterator it = entriesSortedByValues(parametesOrder).iterator();
		while(it.hasNext()) {
			String par = ((Map.Entry<String,Integer>)it.next()).getKey();
			String type = null;
			if(par!= null) type = Constants.FunctionParamType.getSignatureDescriptionFromCopasiType(parametesRoles.get(par));
			if(type!= null) ret.add(type);
		}
		
		return ret;
	}
	
	public Vector<Integer> getParametersTypes_CFunctionParameter() {
		Vector<Integer> ret = new Vector();

		Iterator it = entriesSortedByValues(parametesOrder).iterator();
		while(it.hasNext()) {
			String par = ((Map.Entry<String,Integer>)it.next()).getKey();
			ret.add(parametesRoles.get(par));
		}
		
		return ret;
	}
	
	
	public Vector<String> getParametersNames() {
		Vector<String> ret = new Vector();

		Iterator it = entriesSortedByValues(parametesOrder).iterator();
		while(it.hasNext()) {
			String par = ((Map.Entry<String,Integer>)it.next()).getKey();
			ret.add(par);
		}
		
		return ret;
	}
	
	
	
	static <K,V extends Comparable<? super V>>
	SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
	    SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
	        new Comparator<Map.Entry<K,V>>() {
	            @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
	                return e1.getValue().compareTo(e2.getValue());
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}

	public String getName() {
		return this.name;
	}


	public void setParameterRole(String paramName, Integer cparamType) {
		this.parametesRoles.put(paramName, cparamType);
	}

	public void setParameterIndex(String string, long variableIndex) {
		this.parametesOrder.put(string, (int)variableIndex);
		
	}


	@Override
	public int compareTo(Function o) {
		if(this.getType() == o.getType()) return this.getName().compareTo(o.getName());
		if(this.getType() == CFunction.PreDefined && o.getType() == CFunction.UserDefined) return 1;
		if(this.getType() == CFunction.UserDefined && o.getType() == CFunction.PreDefined) return -1;
		return 0;
	}


	public void setName(String newName) throws Exception {
		InputStream is = new ByteArrayInputStream(newName.getBytes("UTF-8"));
		MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
		CompleteFunctionDeclaration root = parser.CompleteFunctionDeclaration();
		GetFunctionNameVisitor nameV = new GetFunctionNameVisitor();
		root.accept(nameV);
		String funName  = nameV.getFunctionName();
		if(funName.length()==0) {
			throw new MySyntaxException(Constants.FunctionsColumns.NAME.index, "Wrong function definition", Constants.TitlesTabs.FUNCTIONS.getDescription());
		}
		
		int start = newName.indexOf(funName)+funName.length();
		int end = newName.length()-1;
		if(start < end) {
			throw new MySyntaxException(Constants.FunctionsColumns.NAME.index, "Wrong function definition: this method is used to set only the pure name of the function", Constants.TitlesTabs.FUNCTIONS.getDescription());
		}
			
		this.name = new String(funName);
	}


	@Override
	public String toString() {
		String name = this.getName();
		return name;
	}

	public Integer getParameterIndex(String paramName) {
		return this.parametesOrder.get(paramName);
	}

	
	public void addUnusedVariablesInSignature(CFunctionParameters variables) {
		
		Vector<String> originalParams = this.getParametersNames();
		
		for(int i = 0; i < variables.size(); i++) {
			CFunctionParameter current = variables.getParameter(i);
			String name = current.getObjectName();
			Integer index = this.getParameterIndex(name);
			originalParams = this.getParametersNames();
				if(index == null) {
					for(int j = 0; j < originalParams.size(); j++) {
						int currentIndex = this.parametesOrder.get(originalParams.get(j));
						if(currentIndex >=i ) {
							currentIndex++;
							this.parametesOrder.put(originalParams.get(j), currentIndex);
						}
						
					}
					this.parametesOrder.put(name, i);
					this.parametesRoles.put(name, CFunctionParameter.VARIABLE);
				
			}
		}
		
	}

	public void renameParameter(String oldName, String newName) throws Exception {
		Integer index = parametesOrder.get(oldName);
		Integer role = parametesRoles.get(oldName);
		this.parametesOrder.remove(oldName);
		this.parametesRoles.remove(oldName);
		this.parametesOrder.put(newName, index);
		this.parametesRoles.put(newName, role);
		this.equation = CellParsers.replaceVariableInExpression(getEquation(), oldName, newName,false);
	}
}
