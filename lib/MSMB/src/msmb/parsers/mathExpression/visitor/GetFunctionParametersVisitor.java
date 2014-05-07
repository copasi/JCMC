package msmb.parsers.mathExpression.visitor;
import msmb.parsers.mathExpression.syntaxtree.*;

import java.util.*;

import org.apache.commons.lang3.tuple.MutablePair;

public class GetFunctionParametersVisitor extends DepthFirstVoidVisitor {
		Vector<String> actuals = new Vector<String>();
		Vector<MutablePair<String,String>> formals = new Vector<MutablePair<String,String>>();
		
	   public GetFunctionParametersVisitor()  {}
		
	   
	   
	   @Override
	   public void visit(FormalParameter n) {
		  String type = ToStringVisitor.toString(n.primitiveType.nodeChoice.choice);
		  String name = ToStringVisitor.toString(n.variableDeclaratorId.nodeToken);
		  formals.add(new MutablePair<String, String>(type, name));
	   }
	   
	@Override
	public void visit(SpeciesReferenceOrFunctionCall_prefix n) {
		if(n.nodeOptional.present())  {
			NodeOptional nodeOptional = (NodeOptional) ((NodeSequence) n.nodeOptional.node).nodes.get(1);
			if(nodeOptional.node==null){
				//System.out.println("FUNCTION CALL ("0");
				actuals = new Vector<String>();
				return;
			}
			else {
				if(!isMultistateSitesList(nodeOptional.node)) {
					//System.out.println("FUNCTION CALL ("+getNumberArguments((ArgumentList)nodeOptional.node)+"): " +name);
					visit((ArgumentList)nodeOptional.node);
				} else {
					//System.out.println("SPECIES: "+ToStringVisitor.toString(n)); // to print complete "multistate" definition
					return;
				}
			}
		} else {
			//System.out.println("SPECIES: "+ToStringVisitor.toString(n));
			return;
		}
		
	}
	  
	
	@Override
	public void visit(ArgumentList n) {
		int size = ((NodeSequence)(n.nodeChoice.choice)).nodes.size()-1;
		INode element0 = ((NodeSequence)(n.nodeChoice.choice)).nodes.get(0);
		actuals.add(ToStringVisitor.toString(element0));
		INode element = ((NodeSequence)(n.nodeChoice.choice)).nodes.get(1);
		if(element instanceof NodeListOptional) {
			NodeListOptional optList = (NodeListOptional) element;
			for(int i = 0; i < optList.nodes.size(); i++) {
				actuals.add(ToStringVisitor.toString(((NodeSequence)optList.nodes.get(i)).nodes.get(1)));
			}
		}
	}
	 
	public Vector<String> getActualParameters() { return actuals; }	
	public Vector<MutablePair<String,String>> getFormalParameters() { return formals; }	

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
