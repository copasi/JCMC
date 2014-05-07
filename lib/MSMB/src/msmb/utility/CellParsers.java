package msmb.utility;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.apache.commons.lang3.tuple.MutablePair;

import  msmb.parsers.chemicalReaction.MR_ChemicalReaction_Parser;
import  msmb.parsers.chemicalReaction.syntaxtree.CompleteReaction;
import  msmb.parsers.chemicalReaction.visitor.ExtractSubProdModVisitor;
import msmb.parsers.chemicalReaction.visitor.SubstitutionVisitorReaction;
import  msmb.parsers.mathExpression.MR_Expression_Parser;
import msmb.parsers.mathExpression.MR_Expression_ParserConstants;
import  msmb.parsers.mathExpression.MR_Expression_ParserConstantsNOQUOTES;
import  msmb.parsers.mathExpression.MR_Expression_Parser_ReducedParserException;


import  msmb.debugTab.*;
import  msmb.model.*;
import msmb.utility.Constants.BooleanType;
import  msmb.parsers.mathExpression.syntaxtree.*;
import  msmb.parsers.mathExpression.visitor.*;
import  msmb.parsers.multistateSpecies.MR_MultistateSpecies_Parser;
import  msmb.parsers.multistateSpecies.MR_MultistateSpecies_ParserConstantsNOQUOTES;
import  msmb.parsers.multistateSpecies.syntaxtree.CompleteMultistateSpecies;
import  msmb.parsers.multistateSpecies.syntaxtree.CompleteMultistateSpecies_Operator;
import msmb.parsers.multistateSpecies.syntaxtree.CompleteMultistateSpecies_RangeString;
import  msmb.parsers.multistateSpecies.visitor.MultistateSpeciesVisitor;
import msmb.parsers.multistateSpecies.visitor.MultistateSpecies_SubstitutionVisitor;
import  msmb.parsers.multistateSpecies.visitor.MultistateSpecies_UndefinedSitesVisitor;
import  msmb.gui.*;


public class CellParsers {
	
	
	private static HashMap<String, String> cleanedNames = new HashMap<String,String>();
	
	
	public CellParsers() { 	}
	
	
	
	public static boolean isValidCharacterBetweenQuotes(char s) {
		if(s=='\"') return false;
		
		if(s==' ') return true;
		if(s=='!') return true;
		if(s=='#') return true;
		if(s=='$') return true;
		if(s=='%') return true;
		if(s=='&') return true;
		if(s=='\'') return true;
		if(s=='(') return true;
		if(s==')') return true;
		if(s=='*') return true;
		if(s=='+') return true;
		if(s==',') return true;
		if(s=='-') return true;
		if(s=='.') return true;
		if(s=='/') return true;
		if(s==':') return true;
		if(s==';') return true;
		if(s=='<') return true;
		if(s=='=') return true;
		if(s=='>') return true;
		if(s=='?') return true;
		if(s=='[') return true;
		if(s=='\\') return true;
		if(s==']') return true;
		if(s=='^') return true;
		if(s=='_') return true;
		if(s=='`') return true;
		if(s=='{') return true;
		if(s=='|') return true;
		if(s=='}') return true;
		if(s=='~') return true;
		
		
		String in = new String();
		in += s;
		Pattern p = Pattern.compile("\\p{InGreek}*");
		Matcher m = p.matcher(in);
		boolean b = m.matches();
		
		return b;
	}
	
	
	
	public static boolean isNaN(String name) {
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.NAN1)) == 0) return true;
		else if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.NAN2)) == 0) return true;
		//else if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.NAN3)) == 0) return true;
		else return false;
	}
	
	public static boolean isKeyword(String name) {
		
		if(isNaN(name)) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.CONST_AVOGADRO)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.PI)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXPONENTIALE)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.DELAY)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.CEIL)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.FACTORIAL)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.COS)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.ACOS)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.ASIN)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.ATAN)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.ABS)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.LOG10)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.COSH)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TAN)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TANH)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.SIN)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TYPE_MOD)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TYPE_PAR)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TYPE_PROD)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TYPE_SITE)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TYPE_SUB)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TYPE_VAR)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TYPE_VOL)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TIME)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.FLOOR)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.SQRT)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MAX)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MIN)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXP)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.LOG)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MIN)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MAX)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.SEC)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.CSC)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.COT)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.SINH)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.ARCSEC)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.ARCCSC)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.ARCCOT)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.ARCSINH)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.ARCCOSH)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.ARCTANH)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.ARCSECH)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.ARCCSCH)) == 0) return true;
		if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.ARCCOTH)) == 0) return true;
		
		
		if(name.compareTo(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.EXTENSION_CONC).substring(1)) == 0) return true;
		if(name.compareTo(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.EXTENSION_FLUX).substring(1)) == 0) return true;
		if(name.compareTo(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.EXTENSION_FUNCTION).substring(1)) == 0) return true;
		if(name.compareTo(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.EXTENSION_GLOBALQ).substring(1)) == 0) return true;
		if(name.compareTo(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.EXTENSION_INIT).substring(1)) == 0) return true;
		if(name.compareTo(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.EXTENSION_PARTICLE).substring(1)) == 0) return true;
		if(name.compareTo(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.EXTENSION_RATE).substring(1)) == 0) return true;
		if(name.compareTo(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.EXTENSION_REACTION).substring(1)) == 0) return true;
		if(name.compareTo(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.EXTENSION_SPECIES).substring(1)) == 0) return true;
		if(name.compareTo(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.EXTENSION_TRANS).substring(1)) == 0) return true;
			
		

		return false;
	}
	
	public static boolean isMultistateSpeciesName(String name) {
		if( MainGui.importFromSBMLorCPS && CellParsers.extractCompartmentLabel(name).length() ==0) {//because I may have added the compartment label during the import so it may have become a real multistate species
			return false;
		}
		if(name.startsWith("\"")&&name.endsWith("\"")) return false;
		
		InputStream is;
		try {
			is = new ByteArrayInputStream(name.getBytes("UTF-8"));
			 MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is,"UTF-8");
			 CompleteMultistateSpecies_Operator start = react.CompleteMultistateSpecies_Operator();
			 MultistateSpeciesVisitor v = new MultistateSpeciesVisitor(null);
			 start.accept(v);
			 return v.isRealMultiStateSpecies(); 
		} catch (Throwable e1) {
			try {
				is = new ByteArrayInputStream(name.getBytes("UTF-8"));
			
				MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is,"UTF-8");
			 CompleteMultistateSpecies start = react.CompleteMultistateSpecies();
			 MultistateSpeciesVisitor v = new MultistateSpeciesVisitor(null);
			 start.accept(v);
			 return v.isRealMultiStateSpecies(); 
			 
			} catch (Throwable e) {
				return false;
			}
		}

	
	}

	
	
	
	
	
	public static String reprintExpression_brackets(String expression, boolean full_brackets) {
	
	//	System.out.println("...........reprintExpression_brackets..............");
	//	System.out.println(expression);
	//	System.out.println(".................................");
	
		if(expression.trim().length()==0) return expression;
		try {
		  String ret = new String();
	      ByteArrayInputStream is2 = new ByteArrayInputStream(expression.getBytes("UTF-8"));
			  MR_Expression_Parser parser = new MR_Expression_Parser(is2,"UTF-8");
		  	  CompleteExpression start = parser.CompleteExpression();
		      ExpressionBracketsVisitor vis = new ExpressionBracketsVisitor();
		      start.accept(vis);
			  if(vis.getExceptions().size() == 0) {
				  ret  = vis.reprintExpression(full_brackets);
			  } else {
						throw vis.getExceptions().get(0);
				}
			  return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
public static String evaluateExpressionWithDependentSum(String expression, MultistateSpecies multistateForDependentSum) throws Throwable {
		
		/*	System.out.println("...........evaluateExpression multistateForDependentSum..............");
			System.out.println(expression);
			System.out.println(multistateForDependentSum.printCompleteDefinition());
			System.out.println(".................................");
		*/
			if(expression.trim().length()==0) return null;
			try {
			  String ret = null;
		      ByteArrayInputStream is2 = new ByteArrayInputStream(expression.getBytes("UTF-8"));
				  MR_Expression_Parser parser = new MR_Expression_Parser(is2,"UTF-8");
			  	  CompleteExpression start = parser.CompleteExpression();
			      EvaluateExpressionVisitor vis = new EvaluateExpressionVisitor(MainGui.multiModel, multistateForDependentSum);
			      start.accept(vis);
				  if(vis.getExceptions().size() == 0) {
					  ret  = vis.getExpression();
				  } else {
							throw vis.getExceptions().get(0);
					}
				  return ret;
			} catch (Exception e) {
					return null;
			}
		}
	
public static Double evaluateExpression(String expression, MultiModel multiModel) throws Throwable {
	
	//	System.out.println("...........evaluateExpression..............");
	//	System.out.println(expression);
	//	System.out.println(".................................");
	
		if(expression.trim().length()==0) return null;
		try {
		  Double ret = new Double(0.0);
	      ByteArrayInputStream is2 = new ByteArrayInputStream(expression.getBytes("UTF-8"));
			  MR_Expression_Parser parser = new MR_Expression_Parser(is2,"UTF-8");
		  	  CompleteExpression start = parser.CompleteExpression();
		      EvaluateExpressionVisitor vis = new EvaluateExpressionVisitor(multiModel);
		      start.accept(vis);
			  if(vis.getExceptions().size() == 0) {
				  ret  = vis.evaluateExpression_notTruncated();
			  } else {
						throw vis.getExceptions().get(0);
				}
			  return ret;
		} catch (Exception e) {
			return null;
		}
	}


	public static Integer evaluateExpression(String expression) throws Throwable {
		  Double d = evaluateExpression(expression, MainGui.multiModel);
		  Integer ret = new Long(Math.round(d)).intValue();
		  return ret;
		}
	
	
	public static String replaceVariableInExpression(String original, String find, String replacement, boolean isVariableIndexMultistate) {
		
		try {
		//	System.out.println(".................................");
		//	System.out.println("original = "+original+"; find = "+find + "; repl = "+replacement);
			if(find.compareTo(replacement)==0) return original;
			InputStream is = new ByteArrayInputStream(original.getBytes("UTF-8"));
			MR_Expression_Parser_ReducedParserException parser = new MR_Expression_Parser_ReducedParserException(is,"UTF-8");
			CompleteExpression root = parser.CompleteExpression();
			MR_SubstitutionVisitor mySV= new MR_SubstitutionVisitor(find, replacement,isVariableIndexMultistate);
			root.accept(mySV);
			String newExpr = mySV.getNewExpression();
			
	//		System.out.println("newExpr1 "+newExpr);
	//		System.out.println(".................................");
		
			InputStream is2 = new ByteArrayInputStream(newExpr.getBytes("UTF-8"));
			parser = new MR_Expression_Parser_ReducedParserException(is2,"UTF-8");
			root = parser.CompleteExpression();
			String newExprParsed = ToStringVisitor.toString(root);
			
			//Node parsedExpression = CellParsers.parser.parse(newExprParsed);
			//newExprParsed = CellParsers.parser.toString(parsedExpression);
			
			//System.out.println(newExprParsed);
			//System.out.println(".................................");
			
			
			return newExprParsed;
			
		} catch (Throwable e) {
			try{
				InputStream is = new ByteArrayInputStream(original.getBytes("UTF-8"));
				MR_MultistateSpecies_Parser parser = new MR_MultistateSpecies_Parser(is);
				CompleteMultistateSpecies_Operator complete = parser.CompleteMultistateSpecies_Operator();
				MultistateSpecies_SubstitutionVisitor mySV = new MultistateSpecies_SubstitutionVisitor(original, find,replacement, isVariableIndexMultistate);
				complete.accept(mySV);
				String newExpr = mySV.getNewMultistate();
		//		System.out.println("newExpr3 "+newExpr);
		//		System.out.println(".................................");
				return newExpr;
			}catch (Throwable e22) {
			
				try{
				InputStream is = new ByteArrayInputStream(original.getBytes("UTF-8"));
				MR_ChemicalReaction_Parser parser = new MR_ChemicalReaction_Parser(is,"UTF-8");
				 CompleteReaction root = parser.CompleteReaction();
				 SubstitutionVisitorReaction mySV= new SubstitutionVisitorReaction(find, replacement, isVariableIndexMultistate);
				root.accept(mySV);
				String newExpr = mySV.getNewExpression();
				
		//		System.out.println("newExpr2 "+newExpr);
		//		System.out.println(".................................");
		
				return newExpr;
				}catch (Throwable e2) {
					if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)	e2.printStackTrace();
					return null;
				}

			}
			
		}
	 }
	
	
	public static String replaceSpeciesName_AfterTransferAssignment(String fullSpDefinition, String originalSpName, String replacementSpName, HashMap<String, String> aliases) {		try{
		InputStream is = new ByteArrayInputStream(fullSpDefinition.getBytes("UTF-8"));
			 MR_MultistateSpecies_Parser parser = new MR_MultistateSpecies_Parser(is);
			 CompleteMultistateSpecies_Operator complete = parser.CompleteMultistateSpecies_Operator();
			 MultistateSpecies_SubstitutionVisitor mySV = new MultistateSpecies_SubstitutionVisitor(fullSpDefinition,originalSpName,replacementSpName,aliases);
			complete.accept(mySV);
			String newExpr = mySV.getNewMultistate();
			return newExpr;
		}catch (Throwable e2) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)	e2.printStackTrace();
			return null;
		}
 }
	
	
	public static String replaceNamesInMultistateAfterAssignment(String fullSpDefinition, String replacementSpName, HashMap<String, String> sitesName_origRepl, HashMap<String, String> aliases) {
			try{
			InputStream is = new ByteArrayInputStream(fullSpDefinition.getBytes("UTF-8"));
			 MR_MultistateSpecies_Parser parser = new MR_MultistateSpecies_Parser(is);
			 CompleteMultistateSpecies_Operator complete = parser.CompleteMultistateSpecies_Operator();
			 MultistateSpecies_SubstitutionVisitor mySV = new MultistateSpecies_SubstitutionVisitor(fullSpDefinition,replacementSpName,sitesName_origRepl, true, aliases);
			complete.accept(mySV);

			String newExpr = mySV.getNewMultistate();
			return newExpr;
			}catch (Throwable e2) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)	e2.printStackTrace();
				return null;
			}
	 }

	public static String replaceNamesInMultistate(String fullSpDefinition, String replacementSpName, HashMap<String, String> sitesName_origRepl) {
		try {
			InputStream is = new ByteArrayInputStream(fullSpDefinition.getBytes("UTF-8"));
			 MR_MultistateSpecies_Parser parser = new MR_MultistateSpecies_Parser(is);
			 CompleteMultistateSpecies complete = parser.CompleteMultistateSpecies();
			 MultistateSpecies_SubstitutionVisitor mySV = new MultistateSpecies_SubstitutionVisitor(fullSpDefinition,replacementSpName,sitesName_origRepl);
			 complete.accept(mySV);

			String newExpr = mySV.getNewMultistate();
			
			return newExpr;
			
		} catch (Throwable e) {
			try{
			InputStream is = new ByteArrayInputStream(fullSpDefinition.getBytes("UTF-8"));
			 MR_MultistateSpecies_Parser parser = new MR_MultistateSpecies_Parser(is);
			 CompleteMultistateSpecies_Operator complete = parser.CompleteMultistateSpecies_Operator();
			 MultistateSpecies_SubstitutionVisitor mySV = new MultistateSpecies_SubstitutionVisitor(fullSpDefinition,replacementSpName,sitesName_origRepl);
			 complete.accept(mySV);

			String newExpr = mySV.getNewMultistate();
			return newExpr;
			}catch (Throwable e2) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)	e2.printStackTrace();
				return null;
			}
		}
	 }
	
	public static Vector<String> extractNamesInList(MultiModel m, String expression, String table_descr, String column_descr) throws MySyntaxException {
		 Vector<String> ret = new Vector<String>();
		 expression = expression.trim();
		 if(expression.length()==0) return ret;
		 int column_tab = -1;
			if(table_descr.compareTo(Constants.TitlesTabs.SPECIES.getDescription())== 0) {
				column_tab = Constants.SpeciesColumns.getIndex(column_descr);
			} else if(table_descr.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())== 0) {
				column_tab = Constants.GlobalQColumns.getIndex(column_descr);
			}  else if(table_descr.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription())== 0) {
				column_tab = Constants.CompartmentsColumns.getIndex(column_descr);
			}
		 try{
			
		  if(expression.length() >0) {
				  InputStream is = new ByteArrayInputStream(expression.getBytes("UTF-8"));
				  MR_Expression_Parser_ReducedParserException parser = new MR_Expression_Parser_ReducedParserException(is,"UTF-8");
				  CompleteListOfExpression root = parser.CompleteListOfExpression();
				  ExtractNamesUsedVisitor usedVisitor = new ExtractNamesUsedVisitor(m);
				  root.accept(usedVisitor);
				 ret = usedVisitor.getNamesUsed();
			  }
		 } catch (Exception e) {
				 throw new MySyntaxException(column_tab, e.getMessage(),table_descr);
		}
		return ret;
	}
	
	
	public static Vector<String> extractElementsInList(MultiModel m, String expression, String table_descr, String column_descr) throws MySyntaxException {
		 Vector<String> ret = new Vector<String>();
		 expression = expression.trim();
		 if(expression.length()==0) return ret;
		 int column_tab = -1;
			if(table_descr.compareTo(Constants.TitlesTabs.SPECIES.getDescription())== 0) {
				column_tab = Constants.SpeciesColumns.getIndex(column_descr);
			} else if(table_descr.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())== 0) {
				column_tab = Constants.GlobalQColumns.getIndex(column_descr);
			}  else if(table_descr.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription())== 0) {
				column_tab = Constants.CompartmentsColumns.getIndex(column_descr);
			} 
		
			if(table_descr.compareTo(Constants.TitlesTabs.EVENTS.getDescription())!=0) {
					 try{
						
					  if(expression.length() >0) {
							  InputStream is = new ByteArrayInputStream(expression.getBytes("UTF-8"));
							  MR_Expression_Parser_ReducedParserException parser = new MR_Expression_Parser_ReducedParserException(is,"UTF-8");
							  CompleteListOfExpression root = parser.CompleteListOfExpression();
							  ExtractElementsVisitor elementsVisitor = new ExtractElementsVisitor(m);
							  root.accept(elementsVisitor);
							  ret = elementsVisitor.getElements();
						  }
					 } catch (Throwable e) {
						 if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
						 throw new MySyntaxException(column_tab, e.getMessage(),table_descr);
					}
					return ret;
			} else {
				 try{
					  if(expression.length() >0) {
							  InputStream is = new ByteArrayInputStream(expression.getBytes("UTF-8"));
							  MR_Expression_Parser_ReducedParserException parser = new MR_Expression_Parser_ReducedParserException(is,"UTF-8");
							  CompleteListOfExpression_Events root = parser.CompleteListOfExpression_Events();
							  ExtractElementsVisitor elementsVisitor = new ExtractElementsVisitor(m);
							  root.accept(elementsVisitor);
							  ret = elementsVisitor.getElements();
						  }
					 } catch (Throwable e) {
						 if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
						 throw new MySyntaxException(column_tab, e.getMessage(),table_descr);
					}
					return ret;
			}
	}
	
	public static Vector<Vector<String>> parseListExpression_getUndefMisused(MultiModel m, String expression, String table_descr, String column_descr) throws MySyntaxException {
		 Vector ret = new Vector();
		 expression = expression.trim();
		 if(expression.length()==0) return ret;
		 int column_tab = -1;
			if(table_descr.compareTo(Constants.TitlesTabs.SPECIES.getDescription())== 0) {
				column_tab = Constants.SpeciesColumns.getIndex(column_descr);
			} else if(table_descr.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())== 0) {
				column_tab = Constants.GlobalQColumns.getIndex(column_descr);
			}  else if(table_descr.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription())== 0) {
				column_tab = Constants.CompartmentsColumns.getIndex(column_descr);
			}
		 try{
			
		    if(expression.length() >0) {
				  InputStream is = new ByteArrayInputStream(expression.getBytes("UTF-8"));
				  MR_Expression_Parser_ReducedParserException parser = new MR_Expression_Parser_ReducedParserException(is,"UTF-8");
				  CompleteListOfExpression root = parser.CompleteListOfExpression();
				  Look4UndefinedMisusedVisitor undefVisitor = new Look4UndefinedMisusedVisitor(m);
				  root.accept(undefVisitor);
				  Vector<String> undef = undefVisitor.getUndefinedElements();
				  Vector<String> misused = undefVisitor.getMisusedElements();
				  
				  ret.add(undef);
				  ret.add(misused);
				  
				  if(undef.size() != 0 || misused.size() != 0) {
					    String message = new String();
						if(undef.size() >0) {
							 message += "Missing element definition: " + undef.toString();
						}
						if(misused.size() > 0) message +=  "\n"+ "The following elements are misused: " +misused.toString();
						throw new MySyntaxException(column_tab, message,table_descr);
				  } 
				  
				
			  }
		 } catch (Exception e) {
				 throw new MySyntaxException(column_tab, e.getMessage(),table_descr);
		}
		return ret;
	}
	
	private static Vector<Vector<String>> parseExpression_getUndefMisused_2(MultiModel m, String expression, String table_descr, String column_descr, MultistateSpecies multistateForDependentSum) throws MySyntaxException, Exception {
		 Vector ret = new Vector();
		 expression = expression.trim();
		 if(expression.length()==0) return ret;
		 int column_tab = -1;
			if(table_descr.compareTo(Constants.TitlesTabs.SPECIES.getDescription())== 0) {
				column_tab = Constants.SpeciesColumns.getIndex(column_descr);
			} else if(table_descr.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())== 0) {
				column_tab = Constants.GlobalQColumns.getIndex(column_descr);
			}  else if(table_descr.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription())== 0) {
				column_tab = Constants.CompartmentsColumns.getIndex(column_descr);
			} else if(table_descr.compareTo(Constants.TitlesTabs.REACTIONS.getDescription())== 0) {
				column_tab = Constants.ReactionsColumns.getIndex(column_descr);
			}else if(table_descr.compareTo(Constants.TitlesTabs.EVENTS.getDescription())== 0) {
				column_tab = Constants.EventsColumns.getIndex(column_descr);
			}
		
		try{
			
		    if(expression.length() >0) {
				  InputStream is = new ByteArrayInputStream(expression.getBytes("UTF-8"));
				  MR_Expression_Parser_ReducedParserException parser = new MR_Expression_Parser_ReducedParserException(is,"UTF-8");
				  CompleteExpression root = parser.CompleteExpression();
				  Look4UndefinedMisusedVisitor undefVisitor = new Look4UndefinedMisusedVisitor(m, multistateForDependentSum);
				  root.accept(undefVisitor);
				  Vector<String> undef = undefVisitor.getUndefinedElements();
				  Vector<String> misused = undefVisitor.getMisusedElements();
				  
				  ret.add(undef);
				  ret.add(misused);
				  
				  if(undef.size() != 0 || misused.size() != 0) {
					    String message = new String();
					    
						if(undef.size() >0 ) {
							 message += "Missing element definition: " + undef.toString();
						}
						if(misused.size() > 0) message += "\n" + "The following elements are misused: " +misused.toString();
						throw new MySyntaxException(column_tab, message,table_descr);
				  } 
				  
				
			  }
		 } catch (msmb.parsers.mathExpression.ParseException e) {
			 throw e;
		}
		return ret;
	}
		    
		    
	public static Vector<Vector<String>> parseExpression_getUndefMisused(MultiModel m, String expression, String table_descr, String column_descr) throws Throwable {
		return parseExpression_getUndefMisused(m,expression,table_descr,column_descr,null);
	}
	
	public static Vector<Vector<String>> parseExpression_getUndefMisused(MultiModel m, String expression, String table_descr, String column_descr, MultistateSpecies multistateForDependentSum) throws Throwable {
		try {
			return parseExpression_getUndefMisused_2(m, expression,table_descr,column_descr, multistateForDependentSum);
		} catch (Throwable e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
			if(!(e instanceof MySyntaxException) ){
				if(table_descr.compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0) {
					if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
					Vector ret = new Vector();
					 DebugMessage dm = new DebugMessage();
					dm.setOrigin_table(table_descr);
					//dm.setProblem(e.getMessage()); not very easy to interpret so I will rephrase the message, but it is not general
					dm.setProblem("Error parsing the mathematical expression.");
				    dm.setPriority(DebugConstants.PriorityType.PARSING.priorityCode);
					ret.add(dm);
					return ret; 
				} else {
					throw e;
				}
			}
			else {
				throw (MySyntaxException)e;
			}
		}
	}
	

	
	public static String cleanName(String objectName, boolean species) {
		try {
			objectName = new String(objectName.getBytes("UTF-8"),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
		}
		if(!objectName.startsWith("\"")) {
			if(objectName.indexOf(' ')!=-1 ||
					objectName.indexOf('+')!=-1 ||
					objectName.indexOf('-')!=-1 ||
					objectName.indexOf('*')!=-1 ||
					objectName.indexOf('#')!=-1 ||
					objectName.indexOf('/')!=-1 ||
					objectName.indexOf('=')!=-1 ||
					objectName.indexOf('[')!=-1 ||
					objectName.indexOf(']')!=-1 ||
					objectName.indexOf('&')!=-1 ||
					objectName.indexOf('|')!=-1 ||
					objectName.indexOf('<')!=-1 ||
					objectName.indexOf('>')!=-1 ||
					objectName.indexOf('=')!=-1 ||
					objectName.indexOf('@')!=-1 ||

					objectName.indexOf('^')!=-1) {
				return "\""+objectName+"\"";
			}
			
			if(objectName.indexOf('\"')!=-1 ){
				return new String(objectName.replace("\"", "''"));
			}
			
			if(isKeyword(objectName) && objectName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TIME)) != 0) {
				return new String("\""+objectName+"\"");
			}
			if(species) {
				if(!CellParsers.isMultistateSpeciesName(objectName) && 
						(objectName.indexOf('(')!=-1 
						|| objectName.indexOf(':')!=-1 
						|| objectName.indexOf(',')!=-1
						|| objectName.indexOf(';')!=-1
						|| objectName.indexOf('{')!=-1
						|| objectName.indexOf('}')!=-1)) {
					return new String("\""+objectName+"\"");
				} 
			}
			else {
				if((objectName.indexOf('(')!=-1 
						|| objectName.indexOf(':')!=-1 
						|| objectName.indexOf(',')!=-1
						|| objectName.indexOf(';')!=-1
						|| objectName.indexOf('{')!=-1
						|| objectName.indexOf('}')!=-1)) {
					return new String("\""+objectName+"\"");
				}
			}
			
			
			if(objectName.indexOf('.') != -1) {
				//TO CHECK IF IT'S OK WITH NAME WITH EXTENSIONS IN EXPRESSIONS
				return new String("\""+objectName+"\"");
			} 
			
			try {
				Double d = new Double(objectName);
				return new String("\""+objectName+"\""); // if the name successfully convert into a number, it should be quoted
			} catch (Exception ex) {
				//if it does not convert into a number, it should be fine
			}
	
			if(CellParsers.isKeyword(objectName)) {
				// if the name is a keyword, it should be quoted
				return "\""+objectName+"\"";
			}
			
			 return objectName;
			
		}
		else return objectName;
	}
	
	public static String cleanName(String objectName) {
		return cleanName(objectName,false);
	}
	
	public static String parseMultistateSpecies_product(String multiStateProd, HashMap multiStateReact) {
			if(multiStateProd.contains("succ") || multiStateProd.contains("pred")) { 
				StringTokenizer st = new StringTokenizer(multiStateProd, "[];");
				String name = new String((String)st.nextToken());
				if(!multiStateReact.containsKey(name))	{ return new String(); }			
				
				while(st.hasMoreTokens()) {
				    		String site = (String)(st.nextToken()).trim();
				    		if(site.contains("^")) { continue; }
				    		int start = 0;
				    		int end = site.length();
				    		if(site.contains("succ") || site.contains("prec")) {
				    			start = site.indexOf("(") + 1;
				    			end = site.indexOf(")");
				    		}
				 			String name_site = site.substring(start, end);
			    			MultistateSpecies react = (MultistateSpecies)multiStateReact.get(name);
			    			if(!react.getSitesNames().contains(name_site)) { return new String(); }
			  }
				return multiStateProd;
			}
			
			return multiStateProd;
	}
	
	public static MutablePair<String, String> getMultistateSpecies_rangeWithVariables(String states, boolean evaluate) throws Exception {
		MutablePair<String, String> lower_upper = new MutablePair<String, String>();
		
	  try {  	 
			  InputStream is = new ByteArrayInputStream(states.getBytes());
			 MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is);
			 CompleteMultistateSpecies_RangeString range = react.CompleteMultistateSpecies_RangeString();
			 MultistateSpeciesVisitor v = new MultistateSpeciesVisitor(null);
			 range.accept(v);
			
			 MutablePair<String, String> pair = v.getStringRangeLimits();
			 if(!evaluate) return pair;
			 Integer res1 = CellParsers.evaluateExpression(pair.left);
			 Integer res2 = CellParsers.evaluateExpression(pair.right);
			 if(res1 != null)	 lower_upper.left = res1.toString();
			 else lower_upper.left = pair.left;
			 if(res2 != null)	 lower_upper.right = res2.toString();
			 else lower_upper.right = pair.right;
		  } catch (Throwable e2) {
			  if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)   e2.printStackTrace();
			  throw new Exception("Something wrong in the states format.");
		  }
		
		
		return lower_upper;
	}
	

	public static Vector parseMultistateSpecies_states(String states) throws Exception {
		StringTokenizer st_states = new StringTokenizer(states, ",");
		Vector single_states = new Vector();
		boolean foundTrue = false;
		boolean foundFalse = false;
		while(st_states.hasMoreTokens()) {
				String s = (String)st_states.nextToken().trim();
				if(BooleanType.isTrue(s)
						//s.compareTo(Constants.BooleanType.TRUE.getDescription()) ==0 
						//|| s.compareTo(Constants.BooleanType.TRUE_lower.getDescription()) ==0
						){
					foundTrue = true;
				}
				if(BooleanType.isFalse(s)
						//s.compareTo(Constants.BooleanType.FALSE.getDescription()) ==0 
						//|| s.compareTo(Constants.BooleanType.FALSE_lower.getDescription()) ==0
						){
					foundFalse = true;
				}
				if(CellParsers.isKeyword(s)) {
		    		 throw new Exception("Problem adding state "+s+": it is a reserved word.");
		    	}
				single_states.add(s);
		}
		if((foundFalse||foundTrue) && single_states.size()!=2) {
			return null;
		}
		return single_states;
	}
	
	private static int findRealIndexModifiersSeparator(String reaction) {
		boolean multistate_started = false;
		boolean multistate_ended = false;
		boolean semicolon = false;
		
		for(int i = 0; i < reaction.length(); i++) {
			if(reaction.charAt(i) == '(') {
				multistate_started = true; continue;
			}
			
			if(reaction.charAt(i) == ';' && multistate_started==true) {
				semicolon = true; continue;
			} else {
				if(reaction.charAt(i) == ';' && multistate_started==false) {
					return i;
				}
			}
			if(reaction.charAt(i) == ')') {
				multistate_ended = true;
				multistate_started = false;
				semicolon = false;
			}
		}
		return reaction.length();
	} 
	
	private static Vector extractModifiers(MultiModel m,String modifiersList) throws Exception {
		Vector mod = new Vector();
		
		StringTokenizer st_modifiers = new StringTokenizer(modifiersList," ,");
		while(st_modifiers.hasMoreTokens()) {
					String species = (String)(st_modifiers.nextToken());
			    	String mod_to_add = new String(species);
			    	if(CellParsers.isMultistateSpeciesName(species)) {
			    		if(!species.contains(")")) {
			    			throw new Exception("Only single multisite species states can be used as modifier. No ranges (:) or list (,) operators are allowed");
			    		}
			    		MultistateSpecies r = new MultistateSpecies(m,species); //just to use the parser in the constructor
			    		mod_to_add = r.printCompleteDefinition();
			    		if(mod_to_add.trim().length() <= 0) throw new Exception("PROBLEMS PARSING MODIFIERS");
				 	}	
			    	mod.add(mod_to_add);
				}
			return mod;
		}

	public static Vector parseReaction_2(MultiModel m, String reaction_complete, int row) throws Exception {
		Vector subs_prod_mod = new Vector();
		
		
		
	//	MainGui.clear_debugMessages_relatedWith_table_col_priority(Constants.TitlesTabs.REACTIONS.getDescription(), Constants.ReactionsColumns.REACTION.index, DebugConstants.PriorityType.PARSING.priorityCode);
		
		try {
			InputStream is = new ByteArrayInputStream(reaction_complete.getBytes("UTF-8"));
			MR_ChemicalReaction_Parser react = new MR_ChemicalReaction_Parser(is,"UTF-8");
		  	CompleteReaction start = react.CompleteReaction();
		  	ExtractSubProdModVisitor v = new ExtractSubProdModVisitor(m);
		    start.accept(v);
		    
		    if(v.getExceptions().size() != 0) {
		    	throw new Exception(v.getExceptions().get(0).getMessage());
		    }
		   
		    subs_prod_mod.addAll(v.getAll_asString());
		      
		} catch(Throwable ex) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 
				ex.printStackTrace();
			
			 DebugMessage dm = new DebugMessage();
			 dm.setOrigin_table(Constants.TitlesTabs.REACTIONS.getDescription());
			 dm.setProblem("Reaction not following the correct syntax. Common causes: missing blank separator or quotes. "+ex.getMessage());
			 dm.setOrigin_row(row);
			 dm.setOrigin_col(Constants.ReactionsColumns.REACTION.index);
			 dm.setPriority(DebugConstants.PriorityType.PARSING.priorityCode);
			 MainGui.addDebugMessage_ifNotPresent(dm);
			 throw new Exception("Problem parsing reaction");
		}

		
		return subs_prod_mod;
		
	}
	
	public static Vector parseReaction(MultiModel m, String reaction_complete, int row) throws Exception{
		return parseReaction_2(m, reaction_complete.trim(), row);
	}
	
	
	public static HashMap<String, String> getAllAliases(String reaction_complete) throws Exception {
		HashMap<String, String> ret =  new HashMap<String, String>();
		
		try {
			InputStream is = new ByteArrayInputStream(reaction_complete.getBytes("UTF-8"));
			MR_ChemicalReaction_Parser react = new MR_ChemicalReaction_Parser(is,"UTF-8");
		  	CompleteReaction start = react.CompleteReaction();
		  	ExtractSubProdModVisitor v = new ExtractSubProdModVisitor(null);
		    start.accept(v);
		    
		    if(v.getExceptions().size() != 0) {
		    	throw new Exception(v.getExceptions().get(0).getMessage());
		    }
		   ret.putAll(v.getAliases_CompleteReactant());
		      
		} catch(Throwable ex) {
			//if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 
				ex.printStackTrace();
		
		}
		return ret;
	}
	
	public static HashMap<Integer, String> getAllAliases_2(String reaction_complete) throws Exception {
		//this method returns the aliases name associated to each reactant index, ordered according to the parser (not necessarily the same as the user)
	
		HashMap<Integer, String> ret =  new HashMap<Integer, String>();
		
		try {
			InputStream is = new ByteArrayInputStream(reaction_complete.getBytes("UTF-8"));
			MR_ChemicalReaction_Parser react = new MR_ChemicalReaction_Parser(is,"UTF-8");
		  	CompleteReaction start = react.CompleteReaction();
		  	ExtractSubProdModVisitor v = new ExtractSubProdModVisitor(null);
		    start.accept(v);
		    
		    if(v.getExceptions().size() != 0) {
		    	throw new Exception(v.getExceptions().get(0).getMessage());
		    }
		   ret.putAll(v.getAliases_2_CompleteReactant());
		      
		} catch(Throwable ex) {
			//if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 
				ex.printStackTrace();
		
		}

	
	return ret;
	
}
		
		
	
	
	/*public static Vector parseReaction_old(String reaction_complete, int row) throws Exception{
		if(reaction_complete.trim().length() ==0) return new Vector();
				Vector subs_prod_mod = new Vector();
			
			Vector subs = new Vector();
			Vector prod = new Vector();
			Vector mod = new Vector();
			
			MainGui.clear_debugMessages_relatedWith_table_col_priority(Constants.TitlesTabs.REACTIONS.getDescription(), Constants.ReactionsColumns.REACTION.index, DebugConstants.PriorityType.PARSING.priorityCode);
			
			
		String modifiers = new String();
		
		int index_modifiers_list = reaction_complete.lastIndexOf(";");
		
		String reaction = new String();
		if(index_modifiers_list != -1) { //can contain modifiers
			//but ; is also used in multistate species.
			//so I have to split it manually and fill the modifiers appropriately
			int index_semicolon_modifier = findRealIndexModifiersSeparator(reaction_complete);
			reaction = reaction_complete.substring(0, index_semicolon_modifier);
			if(index_semicolon_modifier < reaction_complete.length()) {
				mod.addAll(extractModifiers(reaction_complete.substring(index_semicolon_modifier+1)));
			}
		} else {  //no modifiers
			reaction = new String(reaction_complete);
		}
		
		StringTokenizer st_reactants_products = new StringTokenizer(reaction.trim(), "->");
		
		if(!reaction.contains("->")) {
		    DebugMessage dm = new DebugMessage();
		    dm.setOrigin_table(Constants.TitlesTabs.REACTIONS.getDescription());
		   dm.setProblem("Reaction not following the correct syntax: missing -> element");
		   dm.setOrigin_row(row);
		   dm.setOrigin_col(Constants.ReactionsColumns.REACTION.index);
			dm.setPriority(DebugConstants.PriorityType.PARSING.priorityCode);
			MainGui.addDebugMessage_ifNotPresent(dm);
			throw new Exception("Problem parsing reaction");
		}
		
		String reactants = new String();
		String products = new String();
		
		
		if(reaction.trim().indexOf("->") > 0) {
			reactants = (String)st_reactants_products.nextToken().trim();
		}
		
		if(st_reactants_products.hasMoreElements()) {
			products = (String)st_reactants_products.nextToken().trim();
		}
		
		StringTokenizer st_species_reactants = new StringTokenizer(reactants, " +*");
		
		HashMap multiStateReactants = new HashMap();
		
		int compacted_reactants_with_stoichiometry = 0;
		int compacted_products_with_stoichiometry = 0;
		
		double stoic = 1.0;
		
		while(st_species_reactants.hasMoreTokens()) {
		    	String species = (String)(st_species_reactants.nextToken());
		    	try{
		    		stoic = Double.parseDouble(species);
		    		species = (String)(st_species_reactants.nextToken());
		    	} catch(Exception ex) { //no stoichiometry in this species, just the classical one
		    		if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
		    	} finally {
		    		String subs_to_add = new String(species);
			    	if(species.contains("(")) {
			    		MultistateSpecies r = new MultistateSpecies(species);
			    		multiStateReactants.put(r.getSpeciesName(), r);
			    		subs_to_add = r.printCompleteDefinition();
			    		if(subs_to_add.trim().length() <= 0) throw new Exception("PROBLEMS PARSING REACTANT");
				 	}	
			    	if(stoic != 1.0) {
			    		subs.add(new Double(stoic) + "*" + subs_to_add);
			    		compacted_reactants_with_stoichiometry++;
			    		stoic = 1.0;
			    	} else {
			    		subs.add(subs_to_add);
			    		compacted_reactants_with_stoichiometry++;
			    	}
			    	
			    	/*for(int i = 0; i < stoic; i++) {
			    		subs.add(subs_to_add);
			    		compacted_reactants_with_stoichiometry++;
			    	}*
			    	compacted_reactants_with_stoichiometry--;
		    	}
		    
		}
		
		stoic = 1.0;
		StringTokenizer st_species_product = new StringTokenizer(products, " +*");
		while(st_species_product.hasMoreTokens()) {
	    	String species = (String)(st_species_product.nextToken());
	    
	    	try{
	    		stoic = Double.parseDouble(species);
	    		species = (String)(st_species_product.nextToken());
	    	} catch(Exception ex) {  //no stoichiometry in this species, just the classical one
	    		if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
	    	} finally {
	    		String prod_to_add = new String(species);
	    		/*if(species.contains("(")) {
	    		prod_to_add = parseMultistateSpecies_product(species, multiStateReactants);
	    		if(prod_to_add.trim().length() <= 0) throw new Exception("PROBLEMS PARSING PRODUCT");
	 	    	}*

	    		if(prod_to_add.length() > 0)	{ 
	    			/*for(int i = 0; i < stoic; i++) {
	    				prod.add(prod_to_add); 
	    				compacted_products_with_stoichiometry++;
	    			}
	    			compacted_products_with_stoichiometry--;*
	    			if(stoic != 1.0) {
	    				prod.add(new Double(stoic) + "*" + prod_to_add);
	    				compacted_products_with_stoichiometry++;
			    		stoic = 1.0;
			    	} else {
			    		prod.add(prod_to_add);
			    		compacted_products_with_stoichiometry++;
			    	}
	    			compacted_products_with_stoichiometry--;
	    		}
	    	}
    	}
		
		subs_prod_mod.add(subs);
		subs_prod_mod.add(prod);
		subs_prod_mod.add(mod);
		
		int count_plus_reactants = 0;
		int count_plus_products = 0;
		
		
		int lastIndex = 0;
		while(lastIndex != -1){
		       lastIndex = reactants.indexOf("+",lastIndex+1);
		       if( lastIndex != -1){  count_plus_reactants ++;	      }
		}
		
		lastIndex = 0;
		while(lastIndex != -1){
		       lastIndex = products.indexOf("+",lastIndex+1);
		       if( lastIndex != -1){  count_plus_products ++;	      }
		}
		
		
		if(count_plus_reactants != 0 && count_plus_reactants != subs.size()-1-compacted_reactants_with_stoichiometry ) {
		    DebugMessage dm = new DebugMessage();
			//dm.setOrigin_cause("Parsing reaction error");
			dm.setProblem("Reaction not following the correct syntax: too many + characters between the reactants");
			dm.setPriority(DebugConstants.PriorityType.MAJOR.priorityCode);
			MainGui.addDebugMessage_ifNotPresent(dm);
			throw new Exception("Problem parsing reaction");
		}
		
		if(count_plus_products != 0 && count_plus_products != prod.size()-1-compacted_products_with_stoichiometry ) {
		    DebugMessage dm = new DebugMessage();
			//dm.setOrigin_cause("Parsing reaction error");
			dm.setProblem("Reaction not following the correct syntax: too many + characters between the products");
			dm.setPriority(DebugConstants.PriorityType.MAJOR.priorityCode);
			MainGui.addDebugMessage_ifNotPresent(dm);
			throw new Exception("Problem parsing reaction");
		}
		
			
		return subs_prod_mod;
	}*/

	public static String cleanMathematicalExpression(String mathematicalExpression) {
			String ret = new String(mathematicalExpression);
		
		ret = replaceIfNotBetweenQuotes(ret, " eq ", "==");
		ret = replaceIfNotBetweenQuotes(ret, " ge ", ">=");
		ret = replaceIfNotBetweenQuotes(ret, " le ", "<=");
		ret = replaceIfNotBetweenQuotes(ret, " ne ", "!=");
		ret = replaceIfNotBetweenQuotes(ret, " gt", ">");
		ret = replaceIfNotBetweenQuotes(ret, " lt", "<");
		ret = replaceIfNotBetweenQuotes(ret, " and ", "&&");
		ret = replaceIfNotBetweenQuotes(ret, " or ", "||");
		
/*	//	ret = ret.replace(" eq ", "==");
		ret = ret.replace(" ge ", ">=");
		ret = ret.replace(" le ", "<=");
		ret = ret.replace(" gt", ">");
		ret = ret.replace(" lt", "<");
		ret = ret.replace(" and ", "&&");
		ret = ret.replace(" or ", "||");*/
		
		try {
			InputStream is = new ByteArrayInputStream(ret.getBytes("UTF-8"));
			MR_Expression_Parser_ReducedParserException parser = new MR_Expression_Parser_ReducedParserException(is,"UTF-8");
			CompleteExpression root = parser.CompleteExpression();
			QuoteKeywordInExpressionVisitor quoted = new QuoteKeywordInExpressionVisitor();
			root.accept(quoted);
			ret = quoted.getNewExpression();
			
		} catch (Exception e) {
			
		}
	/*	int index_open = mathematicalExpression.indexOf("\"");
		if(index_open == -1) return ret;
		
		ret = new String();
		int index_close = mathematicalExpression.indexOf("\"",index_open+1);
		
		
		ret += mathematicalExpression.substring(0,index_open);
		
		String cleanName = CellParsers.cleanName(mathematicalExpression.substring(index_open+1, index_close));

		ret += cleanName;
		
		ret += cleanMathematicalExpression(mathematicalExpression.substring(index_close+1));
		
		ret = ret.replace(" eq ", "==");
		ret = ret.replace(" ge ", ">=");
		ret = ret.replace(" le ", "<=");
		ret = ret.replace(" gt", ">");
		ret = ret.replace(" lt", "<");
		ret = ret.replace(" and ", "&&");
		ret = ret.replace(" or ", "||");
		
		
		*/
		
		
		
		return ret;
	}


	public static String replaceIfNotBetweenQuotes(String string, String elementToSearch, String replacement) {
		int to  = string.indexOf(elementToSearch);
		if(to == -1) return string;
		to += elementToSearch.length();
		int from = 0;
		String ret = new String();
		try {
		while(from < string.length()) {
			String subString = new String(string.substring(from,to).getBytes("UTF-8"),"UTF-8");
			String subString_ret_tmp = ret + subString;
			int numOfPrevQuotes = subString_ret_tmp.split("\"").length-1; //if this is odd it means that the element is between quotes so no replacement
			if(numOfPrevQuotes%2==0) {
				subString = subString.replace(elementToSearch, replacement);
			}
			ret += subString;
			from = to;
			to  = string.indexOf(elementToSearch,to);
			if(to == -1) {
				ret += string.substring(from);
				break;
			}
			to += elementToSearch.length();
		}
		
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return ret;
		
	}

	public static String replaceIfBetweenQuotes(String string, String elementToSearch, String replacement) {
		int to  = string.indexOf(elementToSearch);
		if(to == -1) return string;
		to += elementToSearch.length();
		int from = 0;
		String ret = new String();
		try {
		while(from < string.length()) {
			String subString = new String(string.substring(from,to).getBytes("UTF-8"),"UTF-8");
			String subString_ret_tmp = ret + subString;
			int numOfPrevQuotes = subString_ret_tmp.split("\"").length-1; //if this is odd it means that the element is between quotes so no replacement
			if(numOfPrevQuotes%2!=0 || numOfPrevQuotes==0) {
				subString = subString.replace(elementToSearch, replacement);
			}
			ret += subString;
			from = to;
			to  = string.indexOf(elementToSearch,to);
			if(to == -1) {
				ret += string.substring(from);
				break;
			}
			to += elementToSearch.length();
		}
		
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return ret;
		
	}
	


	public static boolean isMultistateSpeciesName_withUndefinedStates(String name) {
		if(name.trim().length() ==0) return false;
		if(name.startsWith("\"")&&name.endsWith("\"")) return false;
		
		InputStream is;
		try {
			is = new ByteArrayInputStream(name.getBytes("UTF-8"));
			 MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is, "UTF-8");
				 
			 CompleteMultistateSpecies start = react.CompleteMultistateSpecies();
			 MultistateSpecies_UndefinedSitesVisitor v = new MultistateSpecies_UndefinedSitesVisitor(null);
			 start.accept(v);
			 return v.isMultistateSpeciesName_withUndefinedStates(); 
		} catch (Exception e1) {
			e1.printStackTrace();
			try {
				is = new ByteArrayInputStream(name.getBytes("UTF-8"));
			
				MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is,"UTF-8");
			 CompleteMultistateSpecies_Operator start = react.CompleteMultistateSpecies_Operator();
			 MultistateSpecies_UndefinedSitesVisitor v = new MultistateSpecies_UndefinedSitesVisitor(null);
			 start.accept(v);
			 return v.isMultistateSpeciesName_withUndefinedStates(); 
			 
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

	}


	public static String addCompartmentLabel(String cleanName, String compartment) {
		String ret = new String();

		if(isMultistateSpeciesName(cleanName)) {
			//ret = "ADD_COMPARTMENT_TO_MULTISTATE_SPECIES";
			ret = cleanName;
		} else {
			ret = cleanName + 
				 MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_Parser.OPEN_R) +
				 MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_COMPARTMENT).substring(1) +
				 MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_Parser.OPEN_C) +
				 cleanName(compartment, false)+
				 MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_Parser.CLOSED_C) +
				 MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_Parser.CLOSED_R);
		}
		
		return ret;
	}
	
	
	public static MutablePair<String, Vector<String>> extractNameExtensions(String element)  {
		GetElementWithExtensions name;
		try{
			InputStream is = new ByteArrayInputStream(element.getBytes("UTF-8"));
			MR_Expression_Parser parser = new MR_Expression_Parser(is);
			CompleteExpression root = parser.CompleteExpression();
			name = new GetElementWithExtensions();
			root.accept(name);
		}catch (Throwable e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 	e.printStackTrace();
			return new MutablePair<String, Vector<String>>(element, new Vector<String>());
		}
		
		Vector<String> extensions = name.getExtensions();
		String stringExtensions = "";
		for(String p : extensions){
			stringExtensions+= p;
		}
	
			String element_name = name.getElementName();
			if(element_name==null || element_name.length() ==0 ||
					ToStringVisitor.toBinary(element_name+stringExtensions).length()!= ToStringVisitor.toBinary(element).length() ) {
				element_name = element; // for simple numbers and for things that do not look ok because of the encoding
			}
		
			return new MutablePair<String, Vector<String>>(element_name, extensions);
	
		
			
	}
	
	//it returns the title of the table, not the extension
	public static String extractKindQuantifier_fromExtensions(Vector<String> extensions) {
		for(int i = 0; i < extensions.size(); i++) {
			String ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_COMPARTMENT);
			if(extensions.get(i).compareTo(ext)==0) {	return Constants.TitlesTabs.COMPARTMENTS.getDescription();	} 
			ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_SPECIES);
			if(extensions.get(i).compareTo(ext)==0) {	return Constants.TitlesTabs.SPECIES.getDescription();	} 
			ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_GLOBALQ);
			if(extensions.get(i).compareTo(ext)==0) {	return Constants.TitlesTabs.GLOBALQ.getDescription();	} 
			ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_REACTION);
			if(extensions.get(i).compareTo(ext)==0) {	return Constants.TitlesTabs.REACTIONS.getDescription();	} 
		}
		return null;
	}

	public String extractQuantityQuantifier_fromExtensions(Vector<String> extensions) {
		for(int i = 0; i < extensions.size(); i++) {
				String ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE);
				if(extensions.get(i).compareTo(ext)==0) {	return ext;	} 
				ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC);
				if(extensions.get(i).compareTo(ext)==0) {	return ext;	} 
			}
			return null;
	}

	public String extractTimingQuantifier_fromExtensions(Vector<String> extensions) {
		for(int i = 0; i < extensions.size(); i++) {
			String ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT);
			if(extensions.get(i).compareTo(ext)==0) {	return ext;	} 
			ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_RATE);
			if(extensions.get(i).compareTo(ext)==0) {	return ext;	} 
			ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS);
			if(extensions.get(i).compareTo(ext)==0) {	return ext;	} 
		}
		return null;
	}

	
	
	
	public static String extractCompartmentLabel(String name) {
		String ret = new String();

		/*if(isMultistateSpeciesName(name)) { extract compartment from "real" classic multistate species
			ret = "EXTRACT_COMPARTMENT_FROM_MULTISTATE_SPECIES";
		} else {*/
			String prefix = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_COMPARTMENT).substring(1)+MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_Parser.OPEN_C);
			int index_cmp_label = name.indexOf(prefix);
			int index_closed_bracket =  name.indexOf(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_Parser.CLOSED_C),index_cmp_label);
			if(index_cmp_label == -1 || index_closed_bracket == -1) return ret;
			else ret = name.substring(index_cmp_label+prefix.length(), index_closed_bracket);
		//}
			return ret;
	}
	
		
	public static String extractMultistateName(String name) {
		String ret = name;

		if(isMultistateSpeciesName(name)) {
			MultistateSpecies ms = null;
			try {
				ms = new MultistateSpecies(MainGui.multiModel, name);
			} catch (Exception e) {
				//e.printStackTrace();
				 try {
					 InputStream is = new ByteArrayInputStream(name.getBytes("UTF-8"));
					 MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is,"UTF-8");
					 CompleteMultistateSpecies_Operator start = react.CompleteMultistateSpecies_Operator();
					 MultistateSpeciesVisitor v = new MultistateSpeciesVisitor(null);
					 start.accept(v);
					 return v.getSpeciesName(); 
				 } catch (Throwable ex ){
					 //ex.printStackTrace();
				 }
			}
			ret = ms.getSpeciesName();
			
		}
		
	
 		
		return ret;
	}

	


	
	public static MutablePair<String, String> extractAlias(String name) {
		MutablePair<String, String>  ret = new MutablePair<String, String>();
		ret.left = null;
		ret.right = null;
		
		if(name.trim().startsWith("\"") &&  name.trim().endsWith("\"")) {
			ret.right = name;
			return ret;
		}
		name = name.trim();
		int index = name.indexOf("=");
		if(index==-1) {
			ret.right = name;
			return ret;
		}
		int indexRoundBracket = name.indexOf("(");
		if(indexRoundBracket!= -1 && index > indexRoundBracket) {
			//is the = for the transfer state
			ret.right = name;
			return ret;
		}

		String alias = name.substring(0,index);
		alias = alias.trim();
		
		String restOfName = name.substring(index+1);
		restOfName = restOfName.trim();
		
		ret.left = alias;
		ret.right = restOfName;
		return ret;
	}

	public static boolean compareMultistateSpecies(MultiModel m, String element1, String name) {
		MultistateSpecies sp;
		try {
			sp = new MultistateSpecies(m, element1);
			return sp.containsSpecificConfiguration(name);
		
		} catch (Exception e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
		}
		return false;
		
	}



	public static boolean isSpeciesWithTransferSiteState(String pr) {
		
	
		InputStream is;
		try {
			is = new ByteArrayInputStream(pr.getBytes("UTF-8"));
			 MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is,"UTF-8");
			 CompleteMultistateSpecies_Operator start = react.CompleteMultistateSpecies_Operator();
			 MultistateSpeciesVisitor v = new MultistateSpeciesVisitor(null);
			 start.accept(v);
			 return v.isRealMultiStateSpecies_WithTransferSiteState(); 
		} catch (Throwable e1) {
			e1.printStackTrace();
			return false;
		}
	}







	
	
	

}
