package msmb.parsers.mathExpression.visitor;
import msmb.model.Function;
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

//import com.sun.tools.javac.code.Attribute.Array;

import msmb.utility.CellParsers;
import msmb.utility.Constants;

public class ExtractElementsVisitor extends DepthFirstVoidVisitor {
		Vector<String> elements = new Vector<String>();
		MultiModel multiModel = null;
		
	
	   public ExtractElementsVisitor(MultiModel mm)  {
		   multiModel = mm;
	   }

		public Vector<String> getElements() {	
			Vector ret = new Vector<String>();
			ret.addAll(elements);
			return ret;	
		}
		
	@Override
	public void visit(CompleteListOfExpression n) {
		elements.add(ToStringVisitor.toString(n.expression));
		if(n.nodeListOptional.present()) {
			 ArrayList<INode> list = (n.nodeListOptional.nodes);
			 for(int i = 0; i < list.size(); i++) {
				 NodeSequence element = (NodeSequence)list.get(i);
				 elements.add(ToStringVisitor.toString(element.nodes.get(1)));
			 }
			
		}
		
	}
	
	@Override
	public void visit(CompleteListOfExpression_Events n) {
		elements.add(ToStringVisitor.toString(n.expression));
		if(n.nodeListOptional.present()) {
			 ArrayList<INode> list = (n.nodeListOptional.nodes);
			 for(int i = 0; i < list.size(); i++) {
				 NodeSequence element = (NodeSequence)list.get(i);
				 elements.add(ToStringVisitor.toString(element.nodes.get(1)));
			 }
			
		}
		
	}

	
}
