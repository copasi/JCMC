package msmb.parsers.mathExpression.visitor;
import msmb.model.MultistateSpecies;
import msmb.parsers.mathExpression.visitor.ToStringVisitor;
import msmb.parsers.mathExpression.syntaxtree.*;
import msmb.utility.CellParsers;

import msmb.gui.MainGui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.commons.lang3.tuple.MutablePair;

public class MR_SubstitutionVisitor extends DepthFirstVoidVisitor {

		String originalName = new String();
		String replacementExpr = new String();
		PrintWriter out;
		ByteArrayOutputStream newExpression = new ByteArrayOutputStream();
		 
		 
		CompleteExpression newCompleteExpression = null;
	
		boolean isVariableIndexMultistate = false;
	   public MR_SubstitutionVisitor(String originalVar, String replacementExpression, boolean isVariableIndexMultistate)  {
		   originalName = originalVar;
		   replacementExpr = replacementExpression;
		   this.isVariableIndexMultistate=isVariableIndexMultistate;
		   out = new PrintWriter(newExpression, true); 
	   }
	   
	   
	   public String getNewExpression() {	
		 return CellParsers.reprintExpression_brackets(newExpression.toString(), MainGui.FULL_BRACKET_EXPRESSION);
		   //	return newExpression.toString();
	   }
	   
	   NumberFormat formatter = new DecimalFormat("##########################.##########################");
		

		private void printToken(final String s) {	
			try{
				Double d = Double.parseDouble(s);
				out.print(formatter.format(d));
			} catch(Exception ex) { out.print(s);}
			finally { out.flush();}	
		}

	
		@Override
		public void visit(final NodeToken n) {   printToken(n.tokenImage); }

		
		@Override
		public void visit(SpeciesReferenceOrFunctionCall_prefix n) {
			String name = ToStringVisitor.toString(n.name.nodeChoice.choice);
			if(n.nodeOptional.present())  {
				NodeOptional nodeOptional = (NodeOptional) ((NodeSequence) n.nodeOptional.node).nodes.get(1);
				if(nodeOptional.node==null){
					//System.out.println("FUNCTION CALL (0): "+name);
					if(name.compareTo(originalName)==0) printToken(replacementExpr);
					else {	printToken(originalName);	}
					super.visit(n);
				}
				else {
					if(!isMultistateSitesList(nodeOptional.node)) {
						//System.out.println("FUNCTION CALL ("+getNumberArguments((ArgumentList)nodeOptional.node)+"): " +name);
						if(name.compareTo(originalName)==0) printToken(replacementExpr);
						else {	printToken(name);	}
						super.visit(n.nodeOptional);
					} else {
						//System.out.println("SPECIES: "+ToStringVisitor.toString(n)); // to print complete "multistate" definition
						if(name.compareTo(originalName)==0) printToken("("+replacementExpr+")");
						else {
							MutablePair<String, String> aliasPair = CellParsers.extractAlias(name);
							if(aliasPair.left != null) {
								name = aliasPair.right;
								out.print(aliasPair.left+"=");
							}
							if(CellParsers.isMultistateSpeciesName(name)) {
								if(!isVariableIndexMultistate) printNewMultistate(name, originalName, replacementExpr);
								else printNewMultistate_changeSite(name, originalName, replacementExpr);
							}
							else super.visit(n);
						}
						
					}
				}
			} else {
				//System.out.println("SPECIES: "+ToStringVisitor.toString(n));
				if(name.compareTo(originalName)==0) printToken("("+replacementExpr+")");
				else super.visit(n);
			}
			
			

	}
	  
	 
		private void printNewMultistate(String currentSpecies, String originalName,	String replacementExpr) {
			String onlyNameOriginal = CellParsers.extractMultistateName(originalName);
			String onlyNameReplacementExpr = CellParsers.extractMultistateName(replacementExpr);
			String onlyNameCurrentSpecies = CellParsers.extractMultistateName(currentSpecies);
						
			try{
				if(onlyNameCurrentSpecies.compareTo(onlyNameOriginal) == 0) {
					out.print(currentSpecies.replaceFirst(onlyNameOriginal, onlyNameReplacementExpr));
				} else {
					out.print(currentSpecies);
				}
			} catch(Exception ex) { 
				//ex.printStackTrace();
				out.print(currentSpecies);
				
			}
			finally { out.flush();}	
			}
		
		private void printNewMultistate_changeSite(String currentSpecies, String originalName,	String replacementExpr) {
			try{
				MultistateSpecies ms = new MultistateSpecies(MainGui.multiModel, currentSpecies);			
				if(ms.containsRangeVariable(originalName)) {
					ms.replaceRangeVariable(originalName, replacementExpr);
				}
				System.out.println("replaced multistate expr "+ ms.printCompleteDefinition());	
				out.print(ms.printCompleteDefinition());
			} catch(Exception ex) { 
				//ex.printStackTrace();
				out.print(currentSpecies);
				
			}
			finally { out.flush();}	
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
