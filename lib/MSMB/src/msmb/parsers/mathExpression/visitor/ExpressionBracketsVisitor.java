package msmb.parsers.mathExpression.visitor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Vector;

import msmb.parsers.mathExpression.MR_Expression_Parser;
import msmb.parsers.mathExpression.MR_Expression_Parser_ReducedParserException;
import msmb.parsers.mathExpression.ParseException;
import msmb.parsers.mathExpression.syntaxtree.*;
import msmb.utility.CellParsers;
import msmb. utility.ReversePolishNotation;

import msmb.model.MultiModel;

public class ExpressionBracketsVisitor extends DepthFirstVoidVisitor
{
  
	Vector<Exception> exceptions = new Vector<Exception>();
	public Vector<Exception> getExceptions() { return exceptions; }

	private Vector<String> splittedExpression = new Vector<String>();
	private HashMap<String, Integer> functions_nameNumArguments = new HashMap<String, Integer>();
	private boolean FULL_BRACKETS;
	

	public ExpressionBracketsVisitor()  {
		functions_nameNumArguments.put("if",3);
	}

	
	@Override
	public void visit(final NodeToken n) {  
		if(n.tokenImage.startsWith(".")) {
			String lastElem = splittedExpression.get(splittedExpression.size()-1);
			splittedExpression.remove(splittedExpression.size()-1);
			splittedExpression.add(lastElem+n.tokenImage);
		}
		else splittedExpression.add(n.tokenImage);
		
	}
	
	

	@Override
	public void visit(UnaryExpression n) {
		if(n.nodeChoice.which == 0) {
			NodeSequence seq = ((NodeSequence)n.nodeChoice.choice);
			String symbol = ToStringVisitor.toString(seq.nodes.get(0));
			if(symbol.compareTo("-")==0) splittedExpression.add("*unary*minus");
			if(symbol.compareTo("+")==0) splittedExpression.add("*unary*plus");
			super.visit((UnaryExpression)seq.nodes.get(1));
		}
		else super.visit(n);
	}
	
	@Override
	public void visit(MultistateSum n) {
		String full = ToStringVisitor.toString(n);
		splittedExpression.add(full);
	}
	
	
	@Override
	public void visit(SpeciesReferenceOrFunctionCall_prefix n) {
		if(n.nodeOptional.present())  {
			NodeOptional nodeOptional = (NodeOptional) ((NodeSequence) n.nodeOptional.node).nodes.get(1);
			if(nodeOptional.node==null){
				//System.out.println("FUNCTION CALL (0): "+name);
				String name = ToStringVisitor.toString(n.name.nodeChoice.choice);
				functions_nameNumArguments.put(name, 0);
				splittedExpression.add(ToStringVisitor.toString(n));
			}
			else {
				if(!isMultistateSitesList(nodeOptional.node)) {
					//System.out.println("FUNCTION CALL ("+getNumberArguments((ArgumentList)nodeOptional.node)+"): " +name);
			
					String name = ToStringVisitor.toString(n.name.nodeChoice.choice);
					splittedExpression.add(name);
					splittedExpression.add("(");
					add_reprintedArguments((ArgumentList)nodeOptional.node);
					splittedExpression.add(")");
					functions_nameNumArguments.put(name, getNumberArguments((ArgumentList)nodeOptional.node));
				} else {
					//System.out.println("SPECIES: "+ToStringVisitor.toString(n)); // to print complete "multistate" definition
					splittedExpression.add(ToStringVisitor.toString(n));
				}
			}
		} else {
			//System.out.println("SPECIES: "+ToStringVisitor.toString(n));
			splittedExpression.add(ToStringVisitor.toString(n));
		}

	}

	
	private String reprintSubExpression(INode subExpression){
		try{
			String ret = new String();
		String expression2 = ToStringVisitor.toString(subExpression);
		  ByteArrayInputStream is2 = new ByteArrayInputStream(expression2.getBytes("UTF-8"));
		  MR_Expression_Parser react2 = new MR_Expression_Parser(is2);
	  	  CompleteExpression start2 = react2.CompleteExpression();
	      ExpressionBracketsVisitor vis = new ExpressionBracketsVisitor();
		  start2.accept(vis);
		  if(vis.getExceptions().size() == 0) {
					ret  = vis.reprintExpression(FULL_BRACKETS);
			} else {
					throw vis.getExceptions().get(0);
			}
		  return ret;
		  } catch (Exception e) {
				e.printStackTrace();
			}
		return null;
	}
	
	private void add_reprintedArguments(ArgumentList node) {
		NodeSequence seq = (NodeSequence)node.nodeChoice.choice;
		splittedExpression.add(reprintSubExpression(seq.nodes.get(0)));
		INode element = ((NodeSequence)(node.nodeChoice.choice)).nodes.get(1);
		if(element instanceof NodeListOptional) {
			NodeListOptional optList = (NodeListOptional) element;
			for(int i = 0; i < optList.nodes.size(); i++) {
				splittedExpression.add(reprintSubExpression(((NodeSequence)(optList.nodes.get(i))).nodes.get(1)));
			}
		}
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
		
		public String reprintExpression(boolean full_brackets) {
			FULL_BRACKETS = full_brackets;
			String ret = new String();
			ReversePolishNotation rpn = new ReversePolishNotation(functions_nameNumArguments );
			String[] output = rpn.infixToRPN(splittedExpression.toArray());
		/*	System.out.println("splitted: "+splittedExpression);
	        System.out.println("RPN:");
	        for (String token : output) {
	            System.out.print(token + " ");
	        }
	        System.out.println("");
	        System.out.println("Infix:");*/
			
	        ReversePolishNotation.FULL_BRACKETS = full_brackets;
	        //System.out.println(ReversePolishNotation.RPNtoInfix(output));
	        ret = rpn.RPNtoInfix(output);
	        return ret;
		}
}


