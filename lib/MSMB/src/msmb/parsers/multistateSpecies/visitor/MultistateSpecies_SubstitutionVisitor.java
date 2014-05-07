package msmb.parsers.multistateSpecies.visitor;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

import msmb.gui.MainGui;
import msmb.parsers.multistateSpecies.syntaxtree.*;
import msmb.utility.CellParsers;

public class MultistateSpecies_SubstitutionVisitor  extends DepthFirstVoidVisitor {
	String originalSpeciesFullDefinition = new String();
	String originalSpeciesTransferAssignment = new String();
	String replacementSpeciesName = new String();
	HashMap<String, String> sitesName_originalReplacement = new HashMap<String, String> ();
	boolean replaceElementsAfterTransferAssign = false;
	PrintWriter out;
	ByteArrayOutputStream newMultistate = new ByteArrayOutputStream();
	private HashMap<String, String> aliases = new HashMap<String, String> ();
	private boolean isVariableRangeMultistate = false;
	private String replacementVariableIndex;
	private String fromVariableIndex;
	 
	 public String getNewMultistate() {	
		if(isVariableRangeMultistate) {
			String newDefinition =  newMultistate.toString();
			try {
				msmb.model.MultistateSpecies ms = new msmb.model.MultistateSpecies(MainGui.multiModel, newDefinition,false,false);
				if(ms.containsRangeVariable(fromVariableIndex)) {
					ms.replaceRangeVariable(fromVariableIndex, replacementVariableIndex);
				}
				return ms.printCompleteDefinition();
			} catch (Exception e) {
				e.printStackTrace();
				return newMultistate.toString();
			}
			
		}
		else return newMultistate.toString();
	 }
	 
   public MultistateSpecies_SubstitutionVisitor(String originalSpFull, String replacementSpName, HashMap<String, String> sitesName_origRepl)  {
	   this(originalSpFull,replacementSpName,sitesName_origRepl, false, new HashMap<String, String>());
   }
   
   
	 
   public MultistateSpecies_SubstitutionVisitor(String originalSpFull, String from, String replacement,  boolean isVariableRangeMultistate)  {
	   this.isVariableRangeMultistate  = isVariableRangeMultistate;
	   originalSpeciesFullDefinition = originalSpFull;
	   replacementVariableIndex = replacement;
	   fromVariableIndex= from;
	   replacementSpeciesName = CellParsers.extractMultistateName(originalSpFull);
	   sitesName_originalReplacement.clear();
	   out = new PrintWriter(newMultistate, true); 
	   originalSpeciesTransferAssignment = null;
	   this.aliases.clear();
   }
   
   
    public MultistateSpecies_SubstitutionVisitor(String originalSpFull, String replacementSpName, HashMap<String, String> sitesName_origRepl, boolean replaceElementsAfterTransferAssign, HashMap<String, String> aliases)  {
	   originalSpeciesFullDefinition = originalSpFull;
	   replacementSpeciesName = replacementSpName;
	   sitesName_originalReplacement.clear();
	   sitesName_originalReplacement.putAll(sitesName_origRepl);
	   out = new PrintWriter(newMultistate, true); 
	   this.replaceElementsAfterTransferAssign = replaceElementsAfterTransferAssign;
	   originalSpeciesTransferAssignment = null;
	   this.aliases.clear();
	 	 this.aliases.putAll(aliases);
   }
    
    public MultistateSpecies_SubstitutionVisitor(String originalSpFull, String originTransferSpName, String replacementTransferSpName, HashMap<String, String> aliases)  {
 	   originalSpeciesFullDefinition = originalSpFull;
 	   out = new PrintWriter(newMultistate, true); 
 	  originalSpeciesTransferAssignment = originTransferSpName;
 	  replacementSpeciesName = replacementTransferSpName;
 	 this.replaceElementsAfterTransferAssign = true;
 	 this.aliases.clear();
 	 this.aliases.putAll(aliases);
    }
   
    @Override
    public void visit(MultistateSpecies_Name n) {
    	String name = ToStringVisitor.toString(n);
    	
    	if(!replaceElementsAfterTransferAssign) {
	    	if(originalSpeciesFullDefinition!= null && name.compareTo(CellParsers.extractMultistateName(originalSpeciesFullDefinition))==0 &&
	    			replacementSpeciesName != null) {
	    		out.print(replacementSpeciesName);
	    	} else {
	    		out.print(name);
	    	}
	    	 out.flush();
    	}  else {
    		out.print(name);
    		out.flush();
    	}
    	
    }
    
    @Override
    public void visit(MultistateSpecies_SiteName n) {
    	String name = ToStringVisitor.toString(n);
    	if(!replaceElementsAfterTransferAssign) {
	    	if(sitesName_originalReplacement.containsKey(name)) {
	    		out.print(sitesName_originalReplacement.get(name));
	    	} else {
	    		out.print(name);
	    	}
	    	 out.flush();
    	}	 else {
    		out.print(name);
    		out.flush();
    	} 
    }
    
    
    @Override
    public void visit(MultistateSpecies_Operator_SiteTransferSelector n) {
    	if(!replaceElementsAfterTransferAssign) {
    		out.print(ToStringVisitor.toString(n));
    		out.flush();
    		return;
    	}
    	
    	String spFromName = new String();
    	String siteFromName = new String();
    	if(n.nodeChoice.which == 0){
			NodeSequence sequence = (NodeSequence)(n.nodeChoice.choice);
			out.println(ToStringVisitor.toString(sequence.nodes.get(0))); // print succ/pred
			out.println(ToStringVisitor.toString(sequence.nodes.get(1))); // print (
			spFromName = ToStringVisitor.toString(sequence.nodes.get(2));
			if(originalSpeciesTransferAssignment!= null && spFromName.compareTo(originalSpeciesTransferAssignment)==0) {
				out.print(replacementSpeciesName); // print species name
			}	else {
				out.print(spFromName); // print species name
			}
			
			
			out.println(ToStringVisitor.toString(sequence.nodes.get(3))); // print dot
			
			siteFromName = ToStringVisitor.toString(sequence.nodes.get(4));
			//print old name or new name for site
			if(aliases != null && aliases.containsKey(spFromName)) {
				spFromName = CellParsers.extractMultistateName(aliases.get(spFromName));
			}
    		if(spFromName.compareTo(replacementSpeciesName) == 0) {
				if(originalSpeciesTransferAssignment!= null) {
		    		out.print(sitesName_originalReplacement.get(siteFromName));
		    	} else {
		    		out.print(siteFromName);
		    	}
		    	 out.flush();
			} else {
				out.print(siteFromName);
			}
    		out.println(ToStringVisitor.toString(sequence.nodes.get(5))); // print )
			
			 out.flush();
	    		
    	} else {
    			NodeSequence sequence = (NodeSequence)(n.nodeChoice.choice);
    			spFromName = ToStringVisitor.toString(sequence.nodes.get(0));
    			siteFromName = ToStringVisitor.toString(sequence.nodes.get(2));
    			if(originalSpeciesTransferAssignment!= null && spFromName.compareTo(originalSpeciesTransferAssignment)==0) {
    				out.print(replacementSpeciesName); // print species name
    			}	else {
    				out.print(spFromName); // print species name
    			}
    			
    			out.println(ToStringVisitor.toString(sequence.nodes.get(1))); // print dot
    			
    			//print old name or new name for site
    			if(aliases != null && aliases.containsKey(spFromName)) {
    				spFromName = CellParsers.extractMultistateName(aliases.get(spFromName));
    			}
    			if(spFromName.compareTo(replacementSpeciesName) == 0) {
    				if(sitesName_originalReplacement.containsKey(siteFromName)) {
    		    		out.print(sitesName_originalReplacement.get(siteFromName));
    		    	} else {
    		    		out.print(siteFromName);
    		    	}
    		    	 out.flush();
    			} else {
    				out.print(siteFromName);
    			}
    			 out.flush();
    	}
    	
    	
    
    }
    	
    
    @Override
    public void visit(MultistateSpecies_Operator_SiteName n) {
    	String name = ToStringVisitor.toString(n);
    	if(!replaceElementsAfterTransferAssign) {
	    	if(sitesName_originalReplacement.containsKey(name)) {
	    		out.print(sitesName_originalReplacement.get(name));
	    	} else {
	    		out.print(name);
	    	}
	    	 out.flush();
    	} else {
	     		out.print(name);
	     		out.flush();
	     	} 
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

}
