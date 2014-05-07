package msmb.parsers.mathExpression.visitor;
import msmb.parsers.mathExpression.MR_Expression_ParserConstantsNOQUOTES;
import msmb.parsers.mathExpression.syntaxtree.*;
import msmb.utility.CellParsers;

import java.util.*;

public class GetUsedVariablesInEquation extends DepthFirstVoidVisitor {
	 TreeMap<String, Comparable> names;
	 boolean complexExpression = false;
	private boolean unaryExpression = false;
	
	   public GetUsedVariablesInEquation()  {
		   names = new  TreeMap<String, Comparable>();
	   }

		public TreeMap<String, Comparable> getNames() {	return names;	}
		
		
	@Override
	public void visit(SpeciesReferenceOrFunctionCall_prefix n) {
		String name = ToStringVisitor.toString(n.name.nodeChoice.choice);
		if(n.nodeOptional.present())  {
			complexExpression = true;
			NodeOptional nodeOptional = (NodeOptional) ((NodeSequence) n.nodeOptional.node).nodes.get(1);
			if(nodeOptional.node==null){
				//System.out.println("FUNCTION CALL (0): "+name);
			}
			else {
				if(!isMultistateSitesList(nodeOptional.node)) {
					//System.out.println("FUNCTION CALL ("+getNumberArguments((ArgumentList)nodeOptional.node)+"): " +name);
				} else {
					//System.out.println("SPECIES: "+ToStringVisitor.toString(n)); // to print complete "multistate" definition
					//names.put(namePar,index); // order as they appear
					if(!CellParsers.isKeyword(ToStringVisitor.toString(n))){
						names.put(ToStringVisitor.toString(n),ToStringVisitor.toString(n)); // order alphabetical
						complexExpression = false;
					}
					
				}
			}
		} else {
			//System.out.println("SPECIES: "+ToStringVisitor.toString(n));
			if(!CellParsers.isKeyword(ToStringVisitor.toString(n))) {
				names.put(ToStringVisitor.toString(n),ToStringVisitor.toString(n));
				complexExpression = false;
			}
		}
		super.visit(n);
	}
	  
	 @Override
	public void visit(NodeToken n) {
		 if(   n.tokenImage.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.DIV))==0 ||
				 n.tokenImage.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TIMES))==0 ||
				 n.tokenImage.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.ASSIGN))==0 ||
				 n.tokenImage.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXPONENT))==0 
			)
			 { 
			 complexExpression = true;
			 }
		 if(n.tokenImage.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.PLUS))==0 ||
			n.tokenImage.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MINUS))==0) {
			 if(!unaryExpression) complexExpression = true;
		 }
		 super.visit(n);
	}
	 
	 @Override
	public void visit(UnaryExpression n) {
		unaryExpression  = true;
		super.visit(n);
	}
	 
	@Override
	public void visit(PrimaryExpression n) {
		unaryExpression  = false;
		super.visit(n);
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

	public boolean isComplexExpression() {
		if(names.size() > 1) return true;
		else return complexExpression;
	}





	
}
