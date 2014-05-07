package msmb.parsers.mathExpression.visitor;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Vector;

import msmb.parsers.mathExpression.MR_Expression_ParserConstants;
import msmb.parsers.mathExpression.MR_Expression_ParserConstantsNOQUOTES;
import msmb.parsers.mathExpression.syntaxtree.*;
import msmb.utility.CellParsers;

public class GetElementWithExtensions extends DepthFirstVoidVisitor {
		String returnName = null;
		Vector<String> extensions = new Vector<String>();
		boolean getName = false;
		
		public String getElementName() { return returnName; }	
		public Vector<String> getExtensions() { return extensions; }	
		
	   public GetElementWithExtensions()  {}
		
	
	    @Override
	   public void visit(SpeciesReferenceOrFunctionCall n) {
		   returnName = ToStringVisitor.toString(n.speciesReferenceOrFunctionCall_prefix);
		   super.visit(n);
	   };
	
	
	@Override
	public void visit(PossibleExtensions n) {
		extensions.add(ToStringVisitor.toString(n.nodeChoice.choice));
		super.visit(n);
	}
	  




	
}
