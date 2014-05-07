package msmb.parsers.chemicalReaction.visitor;

import msmb.parsers.chemicalReaction.MR_ChemicalReaction_ParserConstants;
import msmb.parsers.chemicalReaction.syntaxtree.*;
import msmb.parsers.mathExpression.MR_Expression_ParserConstants;
import msmb.utility.CellParsers;

import java.util.*;

import org.apache.commons.lang3.tuple.MutablePair;

import msmb.model.MultiModel;
import msmb.model.MultistateSpecies;



public class ExtractSubProdModVisitor extends DepthFirstVoidVisitor {
		Vector<MutablePair<Double, String>> substrates;
		Vector<MutablePair<Double, String>> products;
		Vector<MutablePair<Double, String>> modifiers;
		HashMap<String, String> alias_completeMultistateDefinition;
		HashMap<Integer, String> alias_reactantIndex_aliasName;
		private int use_vector = 0;
		private Double stoichiometry = new Double(1.0);
		MultiModel multiModel = null;
		
	   public ExtractSubProdModVisitor(MultiModel m)  {
		   substrates = new Vector<MutablePair<Double, String>>();
		   products = new Vector<MutablePair<Double, String>>();
		   modifiers = new Vector<MutablePair<Double, String>>();
		   alias_completeMultistateDefinition = new HashMap<String, String>();
		   alias_reactantIndex_aliasName = new HashMap<Integer, String>();
		   multiModel = m;
	   }

		Vector<Exception> exceptions = new Vector<Exception>();
		 public Vector<Exception> getExceptions() { return exceptions; }
	   
		public Vector<Vector<MutablePair<Double,String>>> getAll() {	
			Vector ret = new Vector<Vector<MutablePair<Double,String>>> ();
			ret.add(getSubstrates());
			ret.add(getProducts());
			ret.add(getModifiers());
			return ret;	
		}
		
		public Vector<Vector<String>> getAll_asString() {	
			Vector ret = new Vector<Vector<MutablePair<Double,String>>> ();
			ret.add(getSubstrates_asString());
			ret.add(getProducts_asString());
			ret.add(getModifiers_asString());
			return ret;	
		}
		
		
		public HashMap<String, String> getAliases_CompleteReactant() {
			return alias_completeMultistateDefinition;
		}
		 
		public HashMap<Integer, String> getAliases_2_CompleteReactant() {
			return alias_reactantIndex_aliasName;
		}
				public Vector<Vector<MutablePair<Double,String>>> getAll_onlyNames() {	
					Vector ret = new Vector<Vector<MutablePair<Double,String>>> ();
					ret.add(getSubstrates_onlyNames());
					ret.add(getProducts_onlyNames());
					ret.add(getModifiers_onlyNames());
					return ret;	
				}
				
		

		private Vector<MutablePair<Double,String>> getSubstrates_onlyNames() {	
			Vector ret = new Vector<String>();
			for(MutablePair<Double, String> element  : substrates) {
				String name = new String(element.getRight());
				if(!ret.contains(name)) 	ret.add(name);
			}
			return ret;		
		}
		
		private Vector<MutablePair<Double,String>> getProducts_onlyNames() {	
			Vector ret = new Vector<String>();
			for(MutablePair<Double, String> element  : products) {
				String name = new String(element.getRight());
				if(!ret.contains(name)) 	ret.add(name);
			}
			return ret;		
		}
		private Vector<MutablePair<Double,String>> getModifiers_onlyNames() {	
			Vector ret = new Vector<String>();
			for(MutablePair<Double, String> element  : modifiers) {
				String name = new String(element.getRight());
				if(!ret.contains(name)) 	ret.add(name);
			}
			return ret;		
		}
	
		
		private Vector<Vector<String>> getSubstrates_asString() {	
			Vector ret = new Vector<String>();
			for(MutablePair<Double, String> element  : substrates) {
				String name = new String();
				if(element.getLeft().doubleValue() != 1.0) name += element.getLeft().toString() + " * ";
				name += element.getRight();
				ret.add(name);
			}
			return ret;		
		}
		
		private Vector<Vector<String>> getProducts_asString() {	
			Vector ret = new Vector<String>();
			for(MutablePair<Double, String> element  : products) {
				String name = new String();
				if(element.getLeft().doubleValue() != 1.0) name += element.getLeft().toString() + " * ";
				name += element.getRight();
				ret.add(name);
			}
			return ret;		
		}
		
		private Vector<Vector<String>> getModifiers_asString() {	
			Vector ret = new Vector<String>();
			for(MutablePair<Double, String> element  : modifiers) {
				String name = new String();
				if(element.getLeft().doubleValue() != 1.0) name += element.getLeft().toString() + " * ";
				name += element.getRight();
				ret.add(name);
			}
			return ret;		
		}
		
		private Vector<MutablePair<Double,String>> getSubstrates() {	
			Vector ret = new Vector<MutablePair<Double,String>>();	ret.addAll(substrates);	return ret;		}
		
		private Vector<MutablePair<Double,String>> getProducts() {	
			Vector ret = new Vector<MutablePair<Double,String>>();	ret.addAll(products);	return ret;		}
		
		private Vector<MutablePair<Double,String>> getModifiers() {	
			Vector ret = new Vector<MutablePair<Double,String>>();	ret.addAll(modifiers);	return ret;		}
		
	
		
		@Override
	public void visit(Species n) {
		String name = new String(n.nodeToken.tokenImage);
		if(n.nodeListOptional.present()) {
			name = ToStringVisitor.toString(n);
		}
		
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
		  
		  if(use_vector==0) {
			  addAlias(name);
			  //substrates.add(new MutablePair(stoichiometry, name));
			  MutablePair<String, String> aliasPair = CellParsers.extractAlias(name);
			  substrates.add(new MutablePair(stoichiometry, aliasPair.right));
			  addIndexAlias(substrates.size(), aliasPair.left);
			  stoichiometry = new Double(1.0);
		  } else if(use_vector == 1){
			  products.add(new MutablePair(stoichiometry, name));
			  stoichiometry = new Double(1.0);
		  } else {
			  modifiers.add(new MutablePair(stoichiometry, name));
			  stoichiometry = new Double(1.0);
		  }
		  
		  super.visit(n);
		}
		
		
		private void addAlias(String name) {
		
			MutablePair<String, String> alias_restOfName = CellParsers.extractAlias(name);
			if(alias_restOfName.left == null) return;
			
			if(alias_completeMultistateDefinition.containsKey(alias_restOfName.left))  {
			     exceptions.add(new Exception("Error: Duplicate alias "+alias_restOfName.left));
			     return;
			}
			alias_completeMultistateDefinition.put(alias_restOfName.left, alias_restOfName.right);
			
			return;
		}
		
		private void addIndexAlias(Integer index, String alias) {
			if(alias == null) return;
			alias_reactantIndex_aliasName.put(index, alias);
			return;
		}
		
		
		

		@Override
		public void visit(ListModifiers n) {
			use_vector = 2;
			super.visit(n);
		}
		
		public void visit(NodeToken n)
		  {
		    
			String noQuotes = new String(MR_ChemicalReaction_ParserConstants.tokenImage[n.kind]);
			//noQuotes = noQuotes.substring(1,noQuotes.length()-1).trim();
			if(noQuotes.compareTo(MR_ChemicalReaction_ParserConstants.tokenImage[MR_ChemicalReaction_ParserConstants.ARROW])==0 ||
				noQuotes.compareTo(MR_ChemicalReaction_ParserConstants.tokenImage[MR_ChemicalReaction_ParserConstants.ARROW2])==0){
				use_vector = 1;
			}
			
		  }
		
		
		@Override
		public void visit(SpeciesWithCoeff n) {
			if(n.nodeOptional.present()) {
				Stoichiometry nodeOptional = (Stoichiometry) ((NodeSequence) n.nodeOptional.node).nodes.get(0);
				stoichiometry = new Double(ToStringVisitor.toString(nodeOptional));
			} 
			super.visit(n);
		}
		
		
		String justSpeciesName = new String();
		@Override
		public void visit(CompleteSpeciesWithCoefficient n) {
			justSpeciesName = new String(n.speciesWithCoeff.species.nodeToken.tokenImage);
			if(n.speciesWithCoeff.species.nodeListOptional.present()) {
				justSpeciesName += ToStringVisitor.toString(n.speciesWithCoeff.species.nodeListOptional);
			}
			
			if(n.speciesWithCoeff.nodeOptional.present()) {
				Stoichiometry nodeOptional = (Stoichiometry) ((NodeSequence) n.speciesWithCoeff.nodeOptional.node).nodes.get(0);
				stoichiometry = new Double(ToStringVisitor.toString(nodeOptional));
			} 
		}
		
		public String getExtractedName_fromSpeciesWithCoefficient() {
			return  justSpeciesName;
		}
		
		public Double getExtractedCoeff_fromSpeciesWithCoefficient() {
			return  stoichiometry;
		}
		


	
}
