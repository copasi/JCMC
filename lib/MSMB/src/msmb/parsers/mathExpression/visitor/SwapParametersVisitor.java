package msmb.parsers.mathExpression.visitor;
import msmb.model.Function;
import msmb.parsers.mathExpression.MR_Expression_Parser;
import msmb.parsers.mathExpression.MR_Expression_ParserConstantsNOQUOTES;
import msmb.parsers.mathExpression.syntaxtree.*;
import msmb.utility.CellParsers;

import msmb.gui.MainGui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.lang3.tuple.MutablePair;

public class SwapParametersVisitor extends DepthFirstVoidVisitor {

	String newExpression = new String();	
	HashMap<Integer, Integer> from_to = new HashMap<Integer, Integer>();
	
	   public SwapParametersVisitor(HashMap<String, MutablePair<Integer, Integer>>changedOrders)  {
		   Vector<MutablePair<Integer, Integer>> changes = new Vector<MutablePair<Integer, Integer>>();
			changes.addAll(changedOrders.values());
			for(int i = 0; i< changes.size(); i++) {
				from_to.put(changes.get(i).left, changes.get(i).right);
			}
			
	   }
	   
	   @Override
		public void visit(NodeToken n) {
			newExpression +=ToStringVisitor.toString(n);
		}
	   
		@Override
		public void visit(SpeciesReferenceOrFunctionCall_prefix n) {
			String name = ToStringVisitor.toString(n.name.nodeChoice.choice);
			if(n.nodeOptional.present())  {
				NodeOptional nodeOptional = (NodeOptional) ((NodeSequence) n.nodeOptional.node).nodes.get(1);
				if(nodeOptional.node==null){
					//System.out.println("FUNCTION CALL (0): "+name);
					newExpression += name + "()";
				}
				else {
					if(!isMultistateSitesList(nodeOptional.node)) {
						//System.out.println("FUNCTION CALL ("+getNumberArguments((ArgumentList)nodeOptional.node)+"): " +name);
						newExpression += name + "(";
						Vector newArgs = swapParameters((ArgumentList)nodeOptional.node);
						for(int i = 0; i < newArgs.size(); i++) {
							newExpression += newArgs.get(i) + ",";
						}
						if(newArgs.size() > 0) { newExpression = newExpression.substring(0,newExpression.length()-1);}
						newExpression += ")";
					} else {
						//System.out.println("SPECIES: "+ToStringVisitor.toString(n)); // to print complete "multistate" definition
						newExpression +=ToStringVisitor.toString(n);
					}
				}
			} else {
				//System.out.println("SPECIES: "+ToStringVisitor.toString(n));
				
				newExpression +=ToStringVisitor.toString(n);
			}

	}
	  
	 
	  
	private Vector swapParameters(ArgumentList node) {
			int size = getNumberArguments(node);
			Vector finalParameters = new Vector<String>(size);
			for(int i = 0; i < size; i++)  {
				finalParameters.add(new String());
			}
			
			for(int i = 0; i < size; i++) {
				
				INode elementNode = null;
				if(i ==0) elementNode = ((NodeSequence)(node.nodeChoice.choice)).nodes.get(0);
				else {
					INode element2 = ((NodeSequence)(node.nodeChoice.choice)).nodes.get(1);
					if(element2 instanceof NodeListOptional) {
						NodeSequence seq = (NodeSequence) ((NodeListOptional) element2).nodes.get(i-1);
						elementNode = seq.nodes.get(1); // because the first should be the separator ,
					}
				
				}

				
				String element = ToStringVisitor.toString(elementNode);
				
				if(from_to.containsKey(i)) {
					finalParameters.set(from_to.get(i), element);
				} else {
					finalParameters.set(i, element);
				}
				
				}
				 return finalParameters;
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


	public String getNewExpression() {
		
		return newExpression;
	}





	
}
