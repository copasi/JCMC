package msmb.parsers.mathExpression.visitor;
import msmb.parsers.mathExpression.MR_Expression_ParserConstantsNOQUOTES;
import msmb.parsers.mathExpression.syntaxtree.*;
import msmb.utility.CellParsers;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class QuoteKeywordInExpressionVisitor extends DepthFirstVoidVisitor {

		PrintWriter out;
		ByteArrayOutputStream newExpression = new ByteArrayOutputStream();
		 
		 
		CompleteExpression newCompleteExpression = null;
	
	
	   public QuoteKeywordInExpressionVisitor()  {
		   out = new PrintWriter(newExpression, true); 
	   }
	   
	   
	   public String getNewExpression() {	return newExpression.toString();	}
	   
	   NumberFormat formatter = new DecimalFormat("##########################.##########################");
		

		private void printToken(final String s) {	
			try{
				Double d = Double.parseDouble(s);
				out.print(formatter.format(d));
			} catch(Exception ex) { out.print(s);}
			finally { out.flush();}	
			
		}

	
		@Override
		public void visit(final NodeToken n) {   
				if(n.tokenImage.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.XOR))==0) out.print(" ");
				printToken(n.tokenImage); 
				if(n.tokenImage.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.XOR))==0) out.print(" ");
				if(n.tokenImage.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.BANG))==0) out.print(" ");
				super.visit(n);
			}
			
		

		
		@Override
		public void visit(SpeciesReferenceOrFunctionCall_prefix n) {
			String name = ToStringVisitor.toString(n.name.nodeChoice.choice);
			if(n.nodeOptional.present())  {
				NodeOptional nodeOptional = (NodeOptional) ((NodeSequence) n.nodeOptional.node).nodes.get(1);
				if(nodeOptional.node==null){
					//System.out.println("FUNCTION CALL (0): "+name);
					super.visit(n);
				}
				else {
					if(!isMultistateSitesList(nodeOptional.node)) {
						//System.out.println("FUNCTION CALL ("+getNumberArguments((ArgumentList)nodeOptional.node)+"): " +name);
						super.visit(n);
					} else {
						//System.out.println("SPECIES: "+ToStringVisitor.toString(n)); // to print complete "multistate" definition
						if(isKeywordAllowedForVariableNames(name)) printToken("\""+name+"\"");
						else super.visit(n);
					}
				}
			} else {
				//System.out.println("SPECIES: "+ToStringVisitor.toString(n));
				if(isKeywordAllowedForVariableNames(name)) printToken("\""+name+"\"");
				else super.visit(n);
			}

	}
	  
	 
	  
	 
		public static boolean isKeywordAllowedForVariableNames(String name) {
			
			if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TYPE_MOD)) == 0) return true;
			if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TYPE_PAR)) == 0) return true;
			if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TYPE_PROD)) == 0) return true;
			if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TYPE_SITE)) == 0) return true;
			if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TYPE_SUB)) == 0) return true;
			if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TYPE_VAR)) == 0) return true;
			if(name.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TYPE_VOL)) == 0) return true;
				
				return false;
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
