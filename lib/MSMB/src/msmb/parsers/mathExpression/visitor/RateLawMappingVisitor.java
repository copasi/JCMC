package msmb.parsers.mathExpression.visitor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Vector;

import msmb.gui.MainGui;
import msmb.model.Function;
import msmb.model.MultiModel;


import msmb.parsers.mathExpression.MR_Expression_Parser;
import msmb.parsers.mathExpression.syntaxtree.ArgumentList;
import msmb.parsers.mathExpression.syntaxtree.CompleteExpression;
import msmb.parsers.mathExpression.syntaxtree.INode;
import msmb.parsers.mathExpression.syntaxtree.NodeOptional;
import msmb.parsers.mathExpression.syntaxtree.NodeSequence;
import msmb.parsers.mathExpression.syntaxtree.SpeciesReferenceOrFunctionCall_prefix;
import msmb.parsers.mathExpression.ParseException;

import msmb.utility.Constants;


public class RateLawMappingVisitor extends DepthFirstVoidVisitor {

	  protected int row = -1;
	  protected String equation = new String();
	  Vector<Throwable> exceptions = new Vector<Throwable>();
	 public Vector<Throwable> getExceptions() { return exceptions; }
	   MultiModel multiModel = null;
	  Vector<String> actualsGlobalQ_PARtype = new Vector<String>();
	   
	  public Vector<String> getGlobalQ_PARtype() {	return actualsGlobalQ_PARtype;	}
	  public RateLawMappingVisitor(MultiModel mm, int row, String equation)  { multiModel = mm; this.row = row; this.equation = equation;}
	   
	  
	  private void addMapping_singleFunctionCall(int row, String equation){
		try{
			InputStream is = new ByteArrayInputStream(equation.getBytes("UTF-8"));
			MR_Expression_Parser parser = new MR_Expression_Parser(is, "UTF-8");
			CompleteExpression root = parser.CompleteExpression();
			GetFunctionNameVisitor name = new GetFunctionNameVisitor();
			root.accept(name);
			String funName  = name.getFunctionName();
			if(funName.length()==0) return;
			Function f = multiModel.getFunctionByName(funName);
			HashSet<Integer> h = null;
			
			multiModel.removeRateLawMappingForRow(row);
			
			if(multiModel.getWhereFuncIsUsed(funName)==null) {
				h = new HashSet<Integer>();
			} else {
				h = multiModel.getWhereFuncIsUsed(funName);
			}
			h.add(new Integer(row));
			
			
			multiModel.setWhereFuncIsUsed(funName, h);
			
			


			InputStream is2 = new ByteArrayInputStream(equation.getBytes("UTF-8"));
			MR_Expression_Parser parser2 = new MR_Expression_Parser(is2,"UTF-8");
			CompleteExpression root2 = parser2.CompleteExpression();
			GetFunctionParametersVisitor v = new GetFunctionParametersVisitor();
			root2.accept(v);
			Vector<String> parametersActuals = v.getActualParameters();

			Vector mapping_vector = new Vector();
			mapping_vector.add(f);
			Vector<String> param_names = f.getParametersNames();
			Vector<String> param_roles = f.getParametersTypes();

			for(int i = 0; i < parametersActuals.size(); i++) {
				String actualValue = parametersActuals.get(i);
				mapping_vector.add(param_names.get(i));
				mapping_vector.add(actualValue);
				
				if(param_roles.get(i).compareTo(Constants.FunctionParamType.PARAMETER.signatureType)==0) {
					actualsGlobalQ_PARtype.add(actualValue);
				}
			}
			multiModel.addMapping(row, mapping_vector);
		} catch(Throwable ex) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
			exceptions.add(ex);
		}
	  }
			
			
	  
	  
	  @Override
		public void visit(SpeciesReferenceOrFunctionCall_prefix n) {
			String name = ToStringVisitor.toString(n.name.nodeChoice.choice);
			String fun = new String();
			if(n.nodeOptional.present())  {
				NodeOptional nodeOptional = (NodeOptional) ((NodeSequence) n.nodeOptional.node).nodes.get(1);
				if(nodeOptional.node==null){
					//System.out.println("FUNCTION CALL ("0");
					fun = new String(name);
				}
				else {
					if(!isMultistateSitesList(nodeOptional.node)) {
						//System.out.println("FUNCTION CALL ("+getNumberArguments((ArgumentList)nodeOptional.node)+"): " +name);
						fun = new String(name);
					} else {
						//System.out.println("SPECIES: "+ToStringVisitor.toString(n)); // to print complete "multistate" definition
					}
				}
			} else {
				//System.out.println("SPECIES: "+ToStringVisitor.toString(n));
				fun = new String();
			}
			if(fun.length() >0) {
				Function f = null;
				try {
					f = multiModel.getFunctionByName(fun);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(f==null) exceptions.add(new ParseException("Undefined function: "+fun));
				else {
					addMapping_singleFunctionCall(row,ToStringVisitor.toString(n));
				}
			}
			super.visit(n);
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
