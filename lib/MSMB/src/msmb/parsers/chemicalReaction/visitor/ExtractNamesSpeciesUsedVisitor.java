package msmb.parsers.chemicalReaction.visitor;

import msmb.parsers.chemicalReaction.syntaxtree.*;
import msmb.parsers.mathExpression.MR_Expression_ParserConstants;
import msmb.utility.CellParsers;

import java.util.*;

import org.apache.commons.lang3.tuple.MutablePair;

import msmb.model.MultiModel;



public class ExtractNamesSpeciesUsedVisitor extends DepthFirstVoidVisitor {
		
		MultiModel multiModel = null;
		Vector<String> names = new Vector<String>();
		
	   public ExtractNamesSpeciesUsedVisitor(MultiModel m)  {
		   multiModel = m;
	   }

		Vector<Exception> exceptions = new Vector<Exception>();
		public Vector<Exception> getExceptions() { return exceptions; }
	   
		public Vector<String> getNamesSpeciesUsed() {
			return names;
		}
		
		@Override
		public void visit(Species n) {
		String name = new String(n.nodeToken.tokenImage);
		  int[] vectorExtensions = {
				  	MR_Expression_ParserConstants.EXTENSION_COMPARTMENT,
				  	MR_Expression_ParserConstants.EXTENSION_CONC,
				  	MR_Expression_ParserConstants.EXTENSION_GLOBALQ,
				  	MR_Expression_ParserConstants.EXTENSION_PARTICLE,
				  	MR_Expression_ParserConstants.EXTENSION_RATE,
				  	MR_Expression_ParserConstants.EXTENSION_SPECIES,
				  	MR_Expression_ParserConstants.EXTENSION_TRANS
		  };
		  for(int index : vectorExtensions) {
			  String noQuotes= new String(MR_Expression_ParserConstants.tokenImage[index]);
			  noQuotes = noQuotes.substring(1,noQuotes.length()-1);
		      if(name.toString().endsWith(noQuotes))  {
				     exceptions.add(new Exception("Error: The name cannot end with the reserved sequence "+noQuotes));
				     return;
			  }
		  }
		  
			MutablePair<String, String> aliasPair = CellParsers.extractAlias(name);
			if(aliasPair.left != null) {
				name = aliasPair.right;
			}
		  
		  if(CellParsers.isMultistateSpeciesName(name)) {
			  /*MultistateSpecies r = null;
			  try {
				  r = new MultistateSpecies(multiModel,name);
			  } catch (Exception e) {afsd
				  exceptions.add(new Exception("Error: "+e.getMessage()));
				  return;
			  }
			  names.add(r.getSpeciesName());*/
			  names.add(CellParsers.extractMultistateName(name));
		  } else {
			  names.add(name);
		  }

		  
		  super.visit(n);
		}
		
		
	
}
