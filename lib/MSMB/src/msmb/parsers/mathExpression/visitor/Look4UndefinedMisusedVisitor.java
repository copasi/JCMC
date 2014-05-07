package msmb.parsers.mathExpression.visitor;
import msmb.model.Function;
import msmb.model.GlobalQ;
import msmb.model.MultiModel;
import msmb.model.MultistateSpecies;
import msmb.model.Species;
import msmb.parsers.mathExpression.MR_Expression_ParserConstants;
import msmb.parsers.mathExpression.MR_Expression_ParserConstantsNOQUOTES;
import msmb.parsers.mathExpression.MR_Expression_Parser_ReducedParserException;
import msmb.parsers.mathExpression.syntaxtree.*;
import msmb.parsers.multistateSpecies.MR_MultistateSpecies_Parser;
import msmb.parsers.multistateSpecies.ParseException;
import msmb.parsers.multistateSpecies.syntaxtree.CompleteMultistateSpecies_Operator;
import msmb.parsers.multistateSpecies.visitor.MultistateSpeciesVisitor;

import msmb.gui.MainGui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import org.apache.commons.lang3.tuple.MutablePair;

import msmb.utility.CellParsers;
import msmb.utility.Constants;

public class Look4UndefinedMisusedVisitor extends DepthFirstVoidVisitor {
		Vector<String> missing;
		Vector<String> misused;
		Vector<String> parents;
		Vector<MutablePair<String,String>> usedAs;
		private String returnName;
		private boolean getSubName;
		MultiModel multiModel = null;
		
		//used to collect variable associated to the SITE type for function, so that to the multiModel they look as named Element and they will not be flagged as missing. If they are really missing SUM is going to realize that
		Vector<String> tempSiteName = new Vector<String>();
		
		private MultistateSpecies multistateForDependentSum;
	
	   public Look4UndefinedMisusedVisitor(MultiModel mm)  {
		   missing = new Vector<String>();
		   misused = new Vector<String>();
		   parents = new Vector<String>();
		   usedAs = new Vector<MutablePair<String,String>>();
		   multiModel = mm;
	   }

		public Look4UndefinedMisusedVisitor(MultiModel mm,	MultistateSpecies multistateForDependentSum) {
			   missing = new Vector<String>();
			   misused = new Vector<String>();
			   usedAs = new Vector<MutablePair<String,String>>();
			   multiModel = mm;
			   this.multistateForDependentSum = multistateForDependentSum;
	}

		public Vector<String> getParents() {	return parents;	}
		public Vector<String> getMisusedElements() {	return misused;	}
		public Vector<String> getUndefinedElements() {	return missing;	}
		public Vector<MutablePair<String,String>>  getUsedAsElements() { return usedAs;}
	
		@Override
		public void visit(CompleteExpression n) {
			super.visit(n);
			for(int i = 0; i< tempSiteName.size(); i++){
				multiModel.removeNamedElement(tempSiteName.get(i), -1);
			}
		}
		
		@Override
		public void visit(Name n) {
			returnName = ToStringVisitor.toString(n.nodeChoice.choice);
			if(returnName.startsWith(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MUTANT_PARENT_SEPARATOR))) {
				parents.add(returnName.substring(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MUTANT_PARENT_SEPARATOR).length()));
			}
			if(n.nodeChoice.which ==0) {
				super.visit(n);
				if(getSubName) {
					/*NodeSequence nodes = (NodeSequence) n.nodeChoice.choice;
					returnName = ToStringVisitor.toString(nodes.nodes.get(0));*/
					NodeToken nodes =  (NodeToken) n.nodeChoice.choice;
					returnName = ToStringVisitor.toString(nodes);
				}
			}
		}
		
		@Override
		public void visit(PossibleExtensions n) {
			String ext = ToStringVisitor.toString(n);
			if(ext.startsWith(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MUTANT_PARENT_SEPARATOR))) {
				parents.add(ext.substring(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MUTANT_PARENT_SEPARATOR).length()));
			}
			getSubName = true;
		}
	
		
		
	@Override
	public void visit(SpeciesReferenceOrFunctionCall_prefix n) {
		super.visit(n);
		//String name = ToStringVisitor.toString(n.name.nodeChoice.choice);
		String name = returnName;
		
		if(n.nodeOptional.present())  {
			NodeOptional nodeOptional = (NodeOptional) ((NodeSequence) n.nodeOptional.node).nodes.get(1);
			if(nodeOptional.node==null){
				
				//System.out.println("FUNCTION CALL (0): "+name);
			//	Function f = multiModel.getFunctionByName(name);
				
					Function f = null;
					try {
						f = multiModel.getFunctionByName(ToStringVisitor.toString(n.name.nodeChoice.choice));
					} catch (Throwable e) {
						e.printStackTrace();
					}
					if(f==null) {	
						
						if(!CellParsers.isKeyword(ToStringVisitor.toString(n.name.nodeChoice.choice))) {
							if(!missing.contains(name))	{
									missing.add(name); 
							}
								
						}
					}
			}
			else {
				if(!isMultistateSitesList(nodeOptional.node)) {
					//System.out.println("FUNCTION CALL ("+getNumberArguments((ArgumentList)nodeOptional.node)+"): " +name);
					//Function f = multiModel.getFunctionByName(name);
					name = ToStringVisitor.toString(n.name.nodeChoice.choice);
					
					
						Function f = null;
						try {
							f = multiModel.getFunctionByName(name);
						} catch (Throwable e) {
							e.printStackTrace();
						}
						if(f==null){	
							if(!CellParsers.isKeyword(ToStringVisitor.toString(n.name.nodeChoice.choice))) {
								if(!missing.contains(name))	{
									missing.add(name); 
									}
								if(!misused.contains(name))	{
									misused.add(name); 
									}
								}
							}
						else{
							checkParameterUsage((ArgumentList)nodeOptional.node,f);
						}
				
				} else {
					//System.out.println("SPECIES: "+ToStringVisitor.toString(n)); // to print complete "multistate" definition
					if(isMultistateSpeciesDefined(n)) {
						//ok multistate species existing
					} else if(!CellParsers.isKeyword(name)	
							&& multiModel.getWhereNameIsUsed(name)==null) {
						if(!misused.contains(ToStringVisitor.toString(n))&&!missing.contains(name))	{
							missing.add(name);
						}
					}
				}
			}
		} else {
			if(!CellParsers.isKeyword(name)		
					&& multiModel.getWhereNameIsUsed(name)==null) {
				if(!missing.contains(name))	{
					missing.add(name);
				}
			}
		}
		super.visit(n);
	}
	  
	 
	
	private boolean isMultistateSpeciesDefined(SpeciesReferenceOrFunctionCall_prefix n) {
		String element = ToStringVisitor.toString(n);
		 InputStream is = new ByteArrayInputStream(element.getBytes());
		 MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is);
		 try {
			CompleteMultistateSpecies_Operator start = react.CompleteMultistateSpecies_Operator();
			MultistateSpeciesVisitor v = new MultistateSpeciesVisitor(multiModel);
	
			start.accept(v);
			MultistateSpecies sp = (MultistateSpecies) multiModel.getSpecies(v.getSpeciesName());
		
			if(sp.containsSpecificConfiguration(element)) return true;
			else {
				misused.add(element);
				return false;
			}
			
			/*//to check if that is a state existing from the sp definition
			MultistateSpeciesVisitor v2 = new MultistateSpeciesVisitor(multiModel,sp);
			 start.accept(v2);
			 String exp = v2.getProductExpansion();
			 
			 
			 
			 if(exp != null && v.getExceptions().size() == 0) { return true; }
			 else {
				 return false;
			 }
			*/
		 } catch (Exception e) {
			//	e.printStackTrace();
				return false;
			}
	}


	private int indexSum = -1;
	Vector<SumExpansion> sumExpansion = new Vector<SumExpansion>();
	
	public Vector<SumExpansion> getSumExpansions() {
		//to initialize correctly the weights. I don't know why, but if I delete that part the weights are not in the final expression!??!
		for(SumExpansion element : sumExpansion) {
			element.printCompleteSum();
		}
		return sumExpansion;
	}

	@Override
	public void visit(ArgumentList_MultistateSum n) {
		
		MultistateSpecies sp = (MultistateSpecies) multiModel.getSpecies(ToStringVisitor.toString(n.name));
	
		if(sp== null) {
			missing.add("\nFunction \"SUM\" is misformed. The multistate species "+ToStringVisitor.toString(n.name)+" does not exist.");
			return;
		}
		
		if(n.nodeOptional.present()) {
			ArgumentList_MultistateSum_Selectors sel = (ArgumentList_MultistateSum_Selectors) ((NodeOptional) n.nodeOptional).node;
			indexSum++;
			checkParameterUsage_SUM(sp.getSpeciesName(),sel);
		} else { //all sites, all states, no weight
			SumExpansion se = null;
			boolean modify = false;
			if(indexSum > -1 && indexSum < sumExpansion.size()) {
				modify = true;
				se = sumExpansion.get(indexSum);
			}
			if(se==null) se = new SumExpansion(sp.getDisplayedName(), multiModel);
			Iterator it = sp.getSitesNames().iterator();
			while(it.hasNext()){
				String siteName = (String) it.next();
				se.addSite(siteName);
				Vector states = sp.getSiteStates_complete(siteName);
				se.addStates(siteName, states);
			}
			
			if(modify)sumExpansion.set(indexSum,se);
			else sumExpansion.add(se);
		}
	}
	
	
	/*private void checkParameterUsage_SUM(ArgumentList_MultistateSum_Selectors node) {
		indexSum++;
		int found = getNumberArguments(node);
		/*if(found something about the number of parameters????) {
			misused.add("\nFunction "+f.getName()+" should have "+types.size()+ " parameters and not "+ found + " as in "+f.getName() + "("+ToStringVisitor.toString(node)+")");
			return;
		}*
		MultistateSpecies multi_sp = null;
		for(int i = 0; i < found;) {
			INode elementNode = null;
			if(i ==0) {
				elementNode = ((NodeSequence)(node.nodeChoice.choice)).nodes.get(0);
				String element = ToStringVisitor.toString(elementNode);
				Species s = multiModel.getSpecies(element);
				if(s == null || !(s instanceof MultistateSpecies)) {
					misused.add("\nFunction \"SUM\" should  be used only on multistate species. The first parameter "+element+" is not a multistate species name.");
					return;
				}
				multi_sp = (MultistateSpecies) s;
				i++;
			}
			else {
				INode element2 = ((NodeSequence)(node.nodeChoice.choice)).nodes.get(1);
				try{
					i = threeElements(element2, i,multi_sp);
				} catch(Exception ex) {
					try{
						i = onlyName(element2, i,multi_sp);
					} catch(Exception ex2) {
						try{
							i = onlyName(element2, i,multi_sp);
						} catch(Exception ex2) {sumExpansion.remove(indexSum);
						indexSum--;
						misused.add("\nFunction \"SUM\" is misformed");
						return;

					}
				}


			}

		}


		return;

	}*/

	
	
	private void checkParameterUsage_SUM(String speciesName, ArgumentList_MultistateSum_Selectors node) {
		int nselectors = node.nodeListOptional.size();
		boolean res = checkSelector(speciesName, node.selector, 0, nselectors);
			if(res) {
				for(int i = 0; i < nselectors; i++){
					INode element = node.nodeListOptional.elementAt(i);
					Selector elementNode = (Selector) ((NodeSequence) element).nodes.get(1); //0 is the semicolon
					res = checkSelector(speciesName, elementNode, i+1,nselectors);
					if(!res) break;
				}
			}
	}

	
	
	
	
	private boolean checkSelector(String speciesName, Selector selector, int i, int nselectors) {
		String nameSiteOrFuN = ToStringVisitor.toString(selector.name);
		MultistateSpecies sp = (MultistateSpecies) multiModel.getSpecies(speciesName);
		
		if(!selector.nodeOptional.present()) {//name it's a site, no ranges
			if(!sp.getSitesNames().contains(nameSiteOrFuN)) {
				misused.add("\nFunction \"SUM\" is misformed. The site "+nameSiteOrFuN+" does not exist for species "+speciesName+".");
				return false;
			}
			else {
				SumExpansion se = null;
				boolean modify = false;
				if(indexSum > -1 && indexSum < sumExpansion.size()) {
					modify = true;
					se = sumExpansion.get(indexSum);
				}
				if(se==null) se = new SumExpansion(sp.getDisplayedName(), multiModel);
				se.addSite(nameSiteOrFuN);
				Vector states = sp.getSiteStates_complete(nameSiteOrFuN);
				se.addStates(nameSiteOrFuN, states);
				
				if(modify)sumExpansion.set(indexSum,se);
				else sumExpansion.add(se);
				return true;
			}
		}
		
		if( ((NodeChoice)(selector.nodeOptional.node)).which == 1 &&	i != nselectors) {
			misused.add("\nFunction \"SUM\" is misformed. The weight function "+nameSiteOrFuN+" should be the last element in the list of selectors.");
			return false;
		}
		
		if( ((NodeChoice)(selector.nodeOptional.node)).which == 0) { //site selector
			SiteSelector_postFix siteSel = (SiteSelector_postFix) ((NodeChoice)(selector.nodeOptional.node)).choice;
			Vector<String> listedStates = new Vector<String>();
			String stateFirst = ToStringVisitor.toString(siteSel.expression);
			if(!sp.getSitesNames().contains(nameSiteOrFuN)) {
				misused.add("\nFunction \"SUM\" is misformed. The site "+nameSiteOrFuN+" does not exist for species "+speciesName+".");
				return false;
			}
			
			if(!sp.getSitesRangesWithVariables().containsKey(nameSiteOrFuN)) {
					listedStates.add(stateFirst);
			} else {
				try{
					Long value = Math.round(Double.parseDouble(stateFirst));
					listedStates.add(value.toString()); // it's a normal number
				} catch(Exception ex) {
					//it's a variable or an expression
					try{
						listedStates.add(multiModel.getGlobalQ_integerValue(stateFirst).toString());
					} catch(Throwable ex2) {
						if(multistateForDependentSum== null) {
							misused.add("\nProblems evaluating element "+stateFirst);
						}
						else {
							Integer intVal;
							try {
								String evaluatedExpressionWithSum = CellParsers.evaluateExpressionWithDependentSum(stateFirst, multistateForDependentSum);
								intVal = CellParsers.evaluateExpression(evaluatedExpressionWithSum);
								listedStates.add(intVal.toString());
							} catch (Throwable e) {
								if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)e.printStackTrace();
							}
						
						}
						return false;
					}
				
				}
				
			}
			
			boolean range = false;
			NodeChoice sequenceOrRange = (NodeChoice)siteSel.nodeOptional.node;
			if(sequenceOrRange.which == 0) {//sequence
				Iterator it = ((NodeList)(sequenceOrRange.choice)).nodes.iterator();
				while (it.hasNext()) {
					NodeSequence current = (NodeSequence) it.next();
					listedStates.add(ToStringVisitor.toString(current.nodes.get(1)));
					range = false;
				}
			} else { // range
				String stateSecond = ToStringVisitor.toString(((NodeSequence)(sequenceOrRange.choice)).nodes.get(1));
				if(!sp.getSitesRangesWithVariables().containsKey(nameSiteOrFuN)) {
					listedStates.add(stateSecond);
			} else {
				try{
					Long value = Math.round(Double.parseDouble(stateSecond));
					listedStates.add(value.toString()); // it's a normal number
				} catch(Exception ex) {
					//it's a variable or an expression
					try{
						listedStates.add(multiModel.getGlobalQ_integerValue(stateSecond).toString());
					} catch(Throwable ex2) {
						if(multistateForDependentSum==null) {
							misused.add("\nProblems evaluating element "+stateSecond);
						}
						else {
							Integer intVal;
							try {
								String evaluatedExpressionWithSum = CellParsers.evaluateExpressionWithDependentSum(stateSecond, multistateForDependentSum);
								intVal = CellParsers.evaluateExpression(evaluatedExpressionWithSum);
								listedStates.add(intVal.toString());
							} catch (Throwable e) {
								if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)e.printStackTrace();
							}
						}
						return false;
					}
				
					
					
				}
			}
				range = true;
			}
			
			
			Vector states = sp.getSiteStates_complete(nameSiteOrFuN);
			
			
			for(int i1 = 0; i1< listedStates.size(); i1++) {
				if( states.indexOf(listedStates.get(i1)) == -1 ) {
					misused.add("\nFunction \"SUM\" is misformed. The site's state "+listedStates.get(i1)+" does not exist for site "+nameSiteOrFuN+".");
					return false;
				}
			}
			
			if(	range ) {
				if(!sp.isCircular(nameSiteOrFuN) &&
						states.indexOf(listedStates.get(0)) > states.indexOf(listedStates.get(1))) {
						misused.add("\nFunction \"SUM\" is misformed. The site's state "+listedStates.get(0)+" should precede the site's state "+listedStates.get(1)+" for site "+nameSiteOrFuN+".");
						return false;
				}
				if(!sp.isCircular(nameSiteOrFuN)) {
					String rangeMin = listedStates.get(0);
					String rangeMax = listedStates.get(1);
					listedStates.clear();
					int index = states.indexOf(rangeMin);
					do {
						listedStates.add(states.get(index).toString());
						index++;
					}while(index !=  states.indexOf(rangeMax)+1);
					
				} else { //circular
					String first = listedStates.get(0);
					String last = listedStates.get(1);
					listedStates.clear();
					int indexFirst = states.indexOf(first);
					int indexLast = states.indexOf(last);
					for(int i1 = indexFirst; i1 < states.size(); i1++) {
						listedStates.add(states.get(i1).toString());
					}
					for(int i1 = 0; i1 <= indexLast; i1++) {
						listedStates.add(states.get(i1).toString());
					}
				}
				
			}
			
			/*String stateMax = ToStringVisitor.toString(siteSel.nodeChoice1);
			if( states.indexOf(stateFirst) == -1) {
				misused.add("\nFunction \"SUM\" is misformed. The site's state "+stateMin+" does not exist for site "+nameSiteOrFuN+".");
				return false;
			}
			if( states.indexOf(stateMax) == -1) {
				misused.add("\nFunction \"SUM\" is misformed. The site's state "+stateMax+" does not exist for site "+nameSiteOrFuN+".");
				return false;
			}
			
			if( states.indexOf(stateMin) > states.indexOf(stateMax)) {
				misused.add("\nFunction \"SUM\" is misformed. The site's state "+stateMin+" should precede the site's state "+stateMax+" for site "+nameSiteOrFuN+".");
				return false;
			}*/
			
			SumExpansion se = null;
			boolean modify = false;
			if(indexSum > -1 && indexSum < sumExpansion.size()) {
				modify = true;
				se = sumExpansion.get(indexSum);
			}
			if(se==null) se = new SumExpansion(sp.getDisplayedName(), multiModel);
			se.addSite(nameSiteOrFuN);
			//se.addStates(nameSiteOrFuN, new Vector(states.subList(states.indexOf(stateMin), states.indexOf(stateMax)+1)));
			se.addStates(nameSiteOrFuN, listedStates);
			if(modify)sumExpansion.set(indexSum,se);
			else sumExpansion.add(se);
			
			return true;
		}
		
		if( ((NodeChoice)(selector.nodeOptional.node)).which == 1) { //weight fun
			CoeffFunction_postFix weightSel = (CoeffFunction_postFix) ((NodeChoice)(selector.nodeOptional.node)).choice;
			String functionCall = nameSiteOrFuN+ToStringVisitor.toString(weightSel);
			try {
				InputStream is = new ByteArrayInputStream(functionCall.getBytes("UTF-8"));
				MR_Expression_Parser_ReducedParserException parser = new MR_Expression_Parser_ReducedParserException(is);
				CompleteExpression root;
				root = parser.CompleteExpression();
				Look4UndefinedMisusedVisitor undefVisitor = new Look4UndefinedMisusedVisitor(multiModel);
				root.accept(undefVisitor);
				Vector<String> undef = undefVisitor.getUndefinedElements();
				Vector<String> misused2 = undefVisitor.getMisusedElements();
				
				  Vector<String> realUndef = new Vector();
				   Set sites = sp.getSitesNames();
				  for(String which : undef) {
					  if(!sites.contains(which)) {
						  realUndef.add(which);
					  }
				  }
				  
				  
				if(realUndef.size() > 0) { 
					missing.addAll(realUndef); return false;
				}
				if(misused2.size() > 0) { misused.addAll(undef); return false;}
				
				
				SumExpansion se = null;
				boolean modify = false;
				if(indexSum > -1 && indexSum < sumExpansion.size()) {
					modify = true;
					se = sumExpansion.get(indexSum);
				}
				if(se==null) se = new SumExpansion(sp.getDisplayedName(), multiModel);
				se.addWeightFun(nameSiteOrFuN, weightSel);
				if(modify)sumExpansion.set(indexSum,se);
				else sumExpansion.add(se);
				
			} catch (Throwable e) {
				misused.add("\nFunction \"SUM\" is misformed. Problems parsing the weight function "+functionCall+".");
				return false;
			}
			
		}
		return true;
		
	}

/*	private int onlyName(INode element2, int i, MultistateSpecies multi_sp) throws Exception {
		INode elementNode = null;
		if(element2 instanceof NodeListOptional) {
			NodeSequence seq = (NodeSequence) ((NodeListOptional) element2).nodes.get(i-1);
			elementNode = seq.nodes.get(1); // because the first should be the separator ,
		}
		String site_name = ToStringVisitor.toString(elementNode); // site name

		Vector<String> sites = multi_sp.getSiteStates_complete(site_name);

		if(sites == null) {
			misused.add("\nFunction \"SUM\" is misformed. The site "+site_name+" does not exist for species "+multi_sp.getDisplayedName()+".");
			throw new Exception();
		}
		//the name is ok
		i++;
		SumExpansion se = new SumExpansion(multi_sp.getDisplayedName());
		se.addSite(site_name);
		sumExpansion.set(indexSum,se);
		se.addStates(site_name, new Vector(sites));
		return i;
	}


	private int threeElements(INode element2, int i, MultistateSpecies multi_sp) throws Exception {
		INode elementNode = null;
		if(element2 instanceof NodeListOptional) {
			NodeSequence seq = (NodeSequence) ((NodeListOptional) element2).nodes.get(i-1);
			elementNode = seq.nodes.get(1); // because the first should be the separator ,
		}
		String site_name = ToStringVisitor.toString(elementNode); // site name

		Vector<String> sites = multi_sp.getSiteStates_complete(site_name);

		if(sites == null) {
			misused.add("\nFunction \"SUM\" is misformed. The site "+site_name+" does not exist for species "+multi_sp.getDisplayedName()+".");
			throw new Exception();
		}

		SumExpansion se = new SumExpansion(multi_sp.getDisplayedName());
		se.addSite(site_name);
		sumExpansion.add(indexSum,se);

		//the name is ok
		i++;
		if(element2 instanceof NodeListOptional) {
			NodeSequence seq = (NodeSequence) ((NodeListOptional) element2).nodes.get(i-1);
			elementNode = seq.nodes.get(1); // because the first should be the separator ,
		}
		String element = ToStringVisitor.toString(elementNode);
		if( !sites.contains(element)) {
			i--;
			misused.add("\nFunction \"SUM\" is misformed. The site "+site_name+" does not contain the state "+element+".");
			se.addStates(site_name, new Vector(sites));
			throw new Exception();
		}
		int from = sites.indexOf(element);
		//the first element is ok

		i++;

		if(element2 instanceof NodeListOptional) {
			NodeSequence seq = (NodeSequence) ((NodeListOptional) element2).nodes.get(i-1);
			elementNode = seq.nodes.get(1); // because the first should be the separator ,
		}
		element = ToStringVisitor.toString(elementNode);
		if( !sites.contains(element)) {
			i--;
			i--;
			misused.add("\nFunction \"SUM\" is misformed. The site "+site_name+" does not contain the state "+element+".");
			se.addStates(site_name, new Vector(sites));
			throw new Exception();

		}
		int to = sites.indexOf(element);

		if(to < from) {
			misused.add("\nFunction \"SUM\" is misformed. The site states are not in the right order");
			throw new Exception();
		}

		se.addStates(site_name, new Vector(sites.subList(from, to+1)));

		i++;
		return i;
	}
*/



	private void checkParameterUsage(ArgumentList node, Function f) {
		Vector<String> types = f.getParametersTypes();
		int found = getNumberArguments(node);
		if(types.size() != found) {
			misused.add("\nFunction "+f.getName()+" should have "+types.size()+ " parameters and not "+ found + " as in "+f.getName() + "("+ToStringVisitor.toString(node)+")");
			return;
		}
		
		for(int i = 0; i < found; i++) {
			
			INode elementNode = null;
			if(i ==0) elementNode = ((NodeSequence)(node.nodeChoice.choice)).nodes.get(0);
			else {
				INode element2 = ((NodeSequence)(node.nodeChoice.choice)).nodes.get(1);
				if(element2 instanceof NodeListOptional) {
					NodeSequence seq = (NodeSequence) ((NodeListOptional) element2).nodes.get(i-1);
					elementNode = seq.nodes.get(1); // because the first should be the separator ,
				}
			
			}

			if(Constants.FunctionParamType.VARIABLE.signatureType.compareTo(types.get(i))==0) {
				continue; //I DON'T CHECK ANYTHING... and it's THE ONLY type that allows SOMETHING to BE AN EXPRESSION WITHOUT RAISING ANY PROBLEM
			}
			else {
			
				String element = ToStringVisitor.toString(elementNode);
			
				
				/*Integer definedInTable = multiModel.getWhereNameIsUsed(element);
				if(definedInTable==null) {//it means that is a number or an expression... and if the function requires something else than a variable, this is not allowed
					misused.add("Encountered \""+element+"\". Was expecting a "+types.get(i));
				} else {
					if (	Constants.FunctionParamType.SUBSTRATE.signatureType.compareTo(types.get(i))==0 ||
							Constants.FunctionParamType.PRODUCT.signatureType.compareTo(types.get(i))==0	||
							Constants.FunctionParamType.MODIFIER.signatureType.compareTo(types.get(i))==0) {
						if(definedInTable!=Constants.TitlesTabs.SPECIES.index) misused.add(element);
					} else if (Constants.FunctionParamType.PARAMETER.signatureType.compareTo(types.get(i))==0) {
						if(definedInTable!=Constants.TitlesTabs.GLOBALQ.index) misused.add(element);
						
					} else if (Constants.FunctionParamType.VOLUME.signatureType.compareTo(types.get(i))==0) {
						if(definedInTable!=Constants.TitlesTabs.COMPARTMENTS.index) misused.add(element);
					}
				}*/
				 //TOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO CHECK... IF A NAME IS USED IN MORE THAN ONE SOMEWHERE THERE SHOULD BE THE CHECK THAT A SUFFIX EXISTS.... PROBABLY NOT HERE...
				
				if(Constants.FunctionParamType.SITE.signatureType.compareTo(types.get(i))==0) {
					try {
						multiModel.addNamedElement(element, -1); //temporary add the name as defined so that following visit will find it existing
						tempSiteName.add(element);
					} catch (Throwable e) {
						
						e.printStackTrace();
					}
					usedAs.add(new MutablePair<String, String>(element, types.get(i)));
					continue; 
				}
				
				Vector<Integer> definedInTable = multiModel.getWhereNameIsUsed(element);
				if(definedInTable==null){//it means that is a number or an expression... and if the function requires something else than a variable, this is not allowed
					if(element.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.CONST_MODEL_TIME))==0 ||
							element.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.CONST_MODEL_TIME_INITIAL))==0) {
						if(types.get(i).compareTo(Constants.FunctionParamType.TIME.signatureType)!=0) {
								misused.add("Encountered \""+element+"\". Was expecting a "+types.get(i));
						}
					}
					else if(!missing.contains(element)) { // because if it is missing it is also misused, but the user should see only the missing error
						misused.add("Encountered \""+element+"\". Was expecting a "+types.get(i));
					}
				}
				else {if (	Constants.FunctionParamType.SUBSTRATE.signatureType.compareTo(types.get(i))==0 ||
						Constants.FunctionParamType.PRODUCT.signatureType.compareTo(types.get(i))==0	||
						Constants.FunctionParamType.MODIFIER.signatureType.compareTo(types.get(i))==0) {
					if(!definedInTable.contains(new Integer(Constants.TitlesTabs.SPECIES.index))) {
						if(!missing.contains(element)) { // because if it is missing it is also misused, but the user should see only the missing error
							misused.add(element);
						}
					} else {
						usedAs.add(new MutablePair<String, String>(element, types.get(i)));
					} 
				} else if (Constants.FunctionParamType.PARAMETER.signatureType.compareTo(types.get(i))==0) {
					if(!definedInTable.contains(new Integer(Constants.TitlesTabs.GLOBALQ.index))) {
						if(!missing.contains(element)) { // because if it is missing it is also misused, but the user should see only the missing error
							misused.add(element);
						}
					} 
				} else if (Constants.FunctionParamType.VOLUME.signatureType.compareTo(types.get(i))==0) {
					if(!definedInTable.contains(new Integer(Constants.TitlesTabs.COMPARTMENTS.index))) { 
						if(!missing.contains(element)) { // because if it is missing it is also misused, but the user should see only the missing error
							misused.add(element);
						}
					}
				}}
			//}
			}

		}
		return;
		
	}

	private int getNumberArguments(ArgumentList node) {
		int size = ((NodeSequence)(node.nodeChoice.choice)).nodes.size()-1;
		INode element = ((NodeSequence)(node.nodeChoice.choice)).nodes.get(1);
		if(element instanceof NodeListOptional) {
			NodeListOptional optList = (NodeListOptional) element;
			size += optList.nodes.size();
		}
		return size;
		
	}

	boolean isMultistateSitesList(INode n) {
		 if(n instanceof ArgumentList) {
			 if(((ArgumentList)n).nodeChoice.which ==0){
				 return true;
			 }  else return false;
		 }
		 else {
			 System.out.println("ERROR!" + n.getClass());
			 return false;
		 }
	 }





	
}


class SumExpansion {
	Vector<String> sites = new Vector<String>();
	Vector<Vector<String>> sitesStates = new Vector<Vector<String>>();
	String species_name = new String();
	Vector<Species> elementsSum = null;
	
	String weightFunctionName = new String();
	CoeffFunction_postFix coeffFunctionArgumentList = null;
	MultiModel multiModel = null;
	
	public SumExpansion(String name, MultiModel mm) {
		species_name = name;
		multiModel = mm;
	}
	
	public void addWeightFun(String functionName, CoeffFunction_postFix argumentList) {
		weightFunctionName = functionName;
		coeffFunctionArgumentList = argumentList;
	}

	void addSite(String name) {
		if(!sites.contains(name)) sites.add(name);
	}
	
	void addStates(String siteName, Vector<String> states) {
		int ind = sites.indexOf(siteName);
		if(ind >=0) {
			sitesStates.add(ind, states);
		}
	}
	
	@Override
	public String toString() {
		/*String ret = sites.toString() + "\n";
		for(Vector<String> el : sitesStates) {
			ret += el.toString();
		}*/
		String ret = printCompleteSum();
		return  ret;
	}
	
	
	public Vector<String> getWeightFun() {
		Vector ret = new Vector<String>();
		try {
			for(int i = 0; i < elementsSum.size(); i++){
				Species current = elementsSum.get(i);
				String currentWeightFun;
				currentWeightFun = weightFunWithCurrentSpecies(new MultistateSpecies(null,current.getDisplayedName()));
				if(currentWeightFun!=null) ret.add(currentWeightFun);
			}

		} catch (Exception e) {
			return null;
		}
		return ret;
	}
	
	private String weightFunWithCurrentSpecies(MultistateSpecies current) {
		String ret = new String(weightFunctionName+ToStringVisitor.toString(coeffFunctionArgumentList));
		try {
			if(weightFunctionName == null || weightFunctionName.length() == 0) return null;
			Function f = multiModel.getFunctionByName(weightFunctionName);
			Vector<String> types = f.getParametersTypes();
			
			ArgumentList node = (ArgumentList) coeffFunctionArgumentList.nodeOptional.node; 
			
			for(int i = 0; i < types.size(); i++) {
				INode elementNode = null;
				if(i ==0) elementNode = ((NodeSequence)(node.nodeChoice.choice)).nodes.get(0);
				else {
					INode element2 = ((NodeSequence)(node.nodeChoice.choice)).nodes.get(1);
					if(element2 instanceof NodeListOptional) {
						NodeSequence seq = (NodeSequence) ((NodeListOptional) element2).nodes.get(i-1);
						elementNode = seq.nodes.get(1); // because the first should be the separator ,
					}
				
				}

				if(Constants.FunctionParamType.SITE.signatureType.compareTo(types.get(i))==0) {
					String element = ToStringVisitor.toString(elementNode);
					if(!current.getSitesNames().contains(element)) {
						throw new Exception("Site "+element+ " used in weight function "+weightFunctionName + " does not exist in species" +current.getDisplayedName());
					}
					Vector states = current.getSiteStates_complete(element);
					
					 boolean allNumbers = true;
				     
				     for(int i1 = 0; i1 < states.size(); i1++) { //are they all numbers?
				    	 try {
				    		 Integer.parseInt((String)states.get(i1));	    	 
				    	 } catch(Exception ex) {
				    		 if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
				    		 allNumbers = false;
				    		 break;
				    	 }
				     }
				     if(!allNumbers) {
							throw new Exception("Site "+element+ " used in weight function "+weightFunctionName + " contains non-integer values and cannot be used as a parameter in weight function.");
					 }
				     
				     
				     String original = weightFunctionName+ToStringVisitor.toString(coeffFunctionArgumentList);
				     String find = element;
				     String replacement = current.getSiteStates_complete(element).get(0).toString();
				    		 
				     ret = CellParsers.replaceVariableInExpression(original,find,replacement,false);
				     //System.out.println("finalExpr "+finalExpr);
				     
				}
				else {
					continue; //all the other type should be ok because of the other checkParameter
				
				}

			}

		} catch (Throwable e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
		}
		return ret;
		//return weightFunctionName+ToStringVisitor.toString(weightFunctionArgumentList);
	}
	
	

	public Vector<Species> getSpeciesSum(){
		try {
			MultistateSpecies ms = new MultistateSpecies(multiModel, new String(species_name));
			ms = new MultistateSpecies(multiModel, ms.printCompleteDefinition_withActualValues());
			for(int i = 0; i < sites.size(); i++) {
				ms.addSite_vector(sites.get(i),sitesStates.get(i));
			}
			
			elementsSum = ms.getExpandedSpecies(null);
		} catch (Throwable e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)				e.printStackTrace();
		}
		
		return elementsSum;
	}
	
	public String printCompleteSum() {
		String ret = new String();
		
		try {
			Vector<Species> spec = getSpeciesSum();
			Vector<String> weight = getWeightFun();
			for(int i = 0; i < spec.size()-1; i++) {
				Species s = spec.get(i);
				if(weight!= null &&  weight.size()==spec.size() && weight.get(i).length() > 0) {	ret += weight.get(i) + " * ";	}
				ret += s.getDisplayedName() + " + ";
			}
			if(weight!= null &&  weight.size()==spec.size() && weight.get(spec.size()-1).length()>0) {	ret += weight.get(spec.size()-1) + " * ";	}
			ret += spec.get(spec.size()-1).getDisplayedName();
			
		} catch (Exception e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)		e.printStackTrace();
		}
		
		
		return ret;
	}
	
	
	
	
}
