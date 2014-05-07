package msmb.parsers.mathExpression.visitor;

import msmb.parsers.mathExpression.MR_Expression_Parser;
import msmb.parsers.mathExpression.MR_Expression_ParserConstants;
import msmb.parsers.mathExpression.MR_Expression_ParserConstantsNOQUOTES;
import msmb.parsers.mathExpression.MR_Expression_Parser_ReducedParserException;
import msmb.parsers.mathExpression.ParseException;
import msmb.parsers.mathExpression.syntaxtree.*;
import msmb.parsers.multistateSpecies.MR_MultistateSpecies_Parser;
import msmb.parsers.multistateSpecies.MR_MultistateSpecies_ParserConstantsNOQUOTES;
import msmb.utility.CellParsers;
import msmb.utility.Constants;
import msmb.utility.ReversePolishNotation;

import msmb.gui.MainGui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.tuple.MutablePair;

import msmb.model.Function;
import msmb.model.MultiModel;
import msmb.model.MultistateSpecies;
import msmb.model.Species;

public class EvaluateExpressionVisitor extends DepthFirstVoidVisitor {
	
	Vector<Throwable> exceptions = new Vector<Throwable>();
	public Vector<Throwable> getExceptions() { return exceptions; }
		
	private String expression = new String();
	public String getExpression() {return expression;}
	private Vector<String> splittedExpression = new Vector<String>();

	MultiModel multiModel = null;

	private MultistateSpecies multistateForDependentSUM = null;

	private boolean fromRunManager = false;
	Vector<String> parentsRefs = new Vector<String>();
	
	public void setFromRunManager(boolean b) {fromRunManager = b;}
	

	
		//for now only simple mathematical expressions are allowed, no function calls.
	public EvaluateExpressionVisitor(MultiModel mm, boolean doNotEvaluate)  { 
		  multiModel = mm;
		  this.doEvaluation = doNotEvaluate;
	}
	
	public EvaluateExpressionVisitor(MultiModel mm)  { 
		this(mm, null);
	}
	
	public EvaluateExpressionVisitor(MultiModel mm, MultistateSpecies multistateForDependentSUM)  { 
		  multiModel = mm;
		  this.doEvaluation = true;
		  this.multistateForDependentSUM  = multistateForDependentSUM;
	}
	
	//the visitor of the relational operator will set it true if proper relational operator are found
		private boolean isBooleanExpression= false;
		private boolean doEvaluation = true;


	@Override
	public void visit(RelationalOperator n) {
		if(ToStringVisitor.toString(n).compareTo( MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.ASSIGN))==0) {
			isBooleanExpression = false; //expressions for event trigger cannot contain assignments
		} else {
			isBooleanExpression = true;
		}
		super.visit(n);
	}
	

	public boolean isBooleanExpression() {
		return isBooleanExpression;
	}

	
	@Override
	public void visit(NodeToken n) {
		expression+=n.tokenImage;
		splittedExpression.add(n.tokenImage);
		super.visit(n);
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
	public void visit(SpeciesReferenceOrFunctionCall n) {
		try {
			String element = ToStringVisitor.toString(n);
			if(doEvaluation) {
				Integer elementValue = generateElement(element);
				if(elementValue == null) {
					exceptions.add(new Exception("Problem in evaluating element: "+element));
				}
				expression += elementValue;
				splittedExpression.add(elementValue.toString());
			} else {
				if(fromRunManager) {
					String name = ToStringVisitor.toString(n);
					if(name.startsWith(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MUTANT_PARENT_SEPARATOR))) {
						String parent = name.substring(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MUTANT_PARENT_SEPARATOR).length());
						parentsRefs.add(parent);
					}
				} 
				expression += element;
				splittedExpression.add(element);
			}
			
		} catch (Throwable e) {
			exceptions.add(e);
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
		}
	}
	
	public Integer generateElement(String element) {
		if(CellParsers.isKeyword(element)) return null;
		Integer ret = null;
		try{
			Integer simpleEval =  multiModel.getGlobalQ_integerValue(element);
			if(simpleEval!= null) return simpleEval;
		} catch(Throwable ex){
			ex.printStackTrace();
		}
		
		if(multistateForDependentSUM != null) {
			MutablePair<String, Vector<String>> pair = CellParsers.extractNameExtensions(element);
			String speciesName = pair.left;
			if(speciesName.compareTo(multistateForDependentSUM.getSpeciesName())!= 0) {
				System.err.println("Problems in dependent SUM: references species name != current species name");
				return ret;
			}
			Vector<String> sites = pair.right;
			if(sites == null || sites.size() != 1) {
				System.err.println("Problems in dependent SUM: more than one site is listed");
				return ret;
			}
			String site = sites.get(0).substring(1);
			Vector<String> states = multistateForDependentSUM.getSiteStates_complete(site);
			
			if(states == null || states.size() != 1) {
				System.err.println("Problems in dependent SUM: more than one state value is available");
				return ret;
			}
			
			String state = states.get(0);
			try {
				Integer value = new Integer(state);
				return value;
			} catch(Exception ex) {
				System.err.println("Problems in dependent SUM: non numeric state value");
				return ret;
			}
			
		}
		else {
			try {
				ret = multiModel.getGlobalQ_integerValue(element);
			} catch (Throwable e) {
					exceptions.add(e);
			}
		}
		return ret;
	}
	
	
	public Integer evaluateExpression() {
		
	/*	ReversePolishNotation rpn = new ReversePolishNotation(new HashMap<String,Integer>());
		double r;
		try {
			String[] output = rpn.infixToRPN(splittedExpression.toArray());
			r = rpn.evaluateRPN(output);
		} catch (Exception e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)e.printStackTrace();
			return null;
		}*/
	    //System.out.println("Evaluated expression: "+r);
	    Integer ret = new Long(Math.round(evaluateExpression_notTruncated())).intValue();
	    //System.out.println("Rounded to: "+ret);
		return ret;
	}
	
	
	
public Double evaluateExpression_notTruncated() {
	
	
	
		ReversePolishNotation rpn = new ReversePolishNotation(new HashMap<String,Integer>());
		double r;
		try {
			String[] output = rpn.infixToRPN(splittedExpression.toArray());
			r = rpn.evaluateRPN(output);
		} catch (Exception e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)e.printStackTrace();
			return null;
		}
	   
		return new Double(r);
	}

public Vector<String> getParentsReferences() {
	return parentsRefs;
}


	

	
}
