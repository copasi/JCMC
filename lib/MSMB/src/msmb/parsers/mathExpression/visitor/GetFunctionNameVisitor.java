package msmb.parsers.mathExpression.visitor;
import msmb.parsers.mathExpression.syntaxtree.*;

public class GetFunctionNameVisitor extends DepthFirstVoidVisitor {
		String returnName = new String();
		
	   public GetFunctionNameVisitor()  {}
		
	   @Override
	public void visit(CompleteFunctionDeclaration n) {
		   returnName = n.functionDeclarator.nodeToken.tokenImage;
		   super.visit(n);
	}   
	
	   @Override
		public void visit(SingleFunctionCall n) {
			   returnName = ToStringVisitor.toString(n.name.nodeChoice.choice);
			   //super.visit(n);
		}   
		
	   
	   
	@Override
	public void visit(SpeciesReferenceOrFunctionCall_prefix n) {
		String name = ToStringVisitor.toString(n.name.nodeChoice.choice);
		if(n.nodeOptional.present())  {
			NodeOptional nodeOptional = (NodeOptional) ((NodeSequence) n.nodeOptional.node).nodes.get(1);
			if(nodeOptional.node==null){
				//System.out.println("FUNCTION CALL ("0");
				returnName = new String(name);
			}
			else {
				if(!isMultistateSitesList(nodeOptional.node)) {
					//System.out.println("FUNCTION CALL ("+getNumberArguments((ArgumentList)nodeOptional.node)+"): " +name);
					returnName = new String(name);
				} else {
					//System.out.println("SPECIES: "+ToStringVisitor.toString(n)); // to print complete "multistate" definition
				}
			}
		} else {
			//System.out.println("SPECIES: "+ToStringVisitor.toString(n));
			returnName = new String();
		}
		
	}
	  
	 
	public String getFunctionName() { return returnName; }	

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
