package msmb.parsers.mathExpression.visitor;
import msmb.parsers.mathExpression.syntaxtree.*;
import java.util.*;

//import com.sun.tools.javac.code.Attribute.Array;

import msmb.utility.CellParsers;

public class GetFunctionCallsInEquation extends DepthFirstVoidVisitor {
		HashMap<String,Vector> funCalls_actuals = new HashMap<String,Vector>();
		
	   public GetFunctionCallsInEquation()  {  }

	 public HashMap<String,Vector>  getFunctionCallsWithActualParam() { return funCalls_actuals;}
	 
	 
	
	 
	 
	@Override
	public void visit(SpeciesReferenceOrFunctionCall_prefix n) {
		super.visit(n);
		String name = ToStringVisitor.toString(n.name.nodeChoice.choice);
		
		if(n.nodeOptional.present())  {
			NodeOptional nodeOptional = (NodeOptional) ((NodeSequence) n.nodeOptional.node).nodes.get(1);
			if(nodeOptional.node==null){
				//System.out.println("FUNCTION CALL (0): "+name);
				name = ToStringVisitor.toString(n.name.nodeChoice.choice);
				if(!CellParsers.isKeyword(name)) {
							funCalls_actuals.put(name, new Vector());
				}
			}
			else {
				if(!isMultistateSitesList(nodeOptional.node)) {
					//System.out.println("FUNCTION CALL ("+getNumberArguments((ArgumentList)nodeOptional.node)+"): " +name);
					//Function f = multiModel.getFunctionByName(name);
					name = ToStringVisitor.toString(n.name.nodeChoice.choice);
					if(!CellParsers.isKeyword(ToStringVisitor.toString(n.name.nodeChoice.choice))) {
								Vector act = getActuals((ArgumentList)nodeOptional.node);
								funCalls_actuals.put(name, act);
					}
						
				
				} else {
					//System.out.println("SPECIES: "+ToStringVisitor.toString(n)); // to print complete "multistate" definition
				}
			}
		}
		
		super.visit(n);
	}
	  
	
	

	private Vector getActuals(ArgumentList node) {
		int found = getNumberArguments(node);
		Vector ret = new Vector<String>();
		
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

			String element = ToStringVisitor.toString(elementNode);
			ret.add(element);
				
			}
			 return ret;
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

	private int getNumberArguments(ArgumentList node) {
		int size = ((NodeSequence)(node.nodeChoice.choice)).nodes.size()-1;
		INode element = ((NodeSequence)(node.nodeChoice.choice)).nodes.get(1);
		if(element instanceof NodeListOptional) {
			NodeListOptional optList = (NodeListOptional) element;
			size += optList.nodes.size();
		}
		return size;
		
	}



	
}
