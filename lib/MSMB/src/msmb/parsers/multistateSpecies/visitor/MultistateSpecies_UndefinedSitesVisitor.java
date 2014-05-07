package msmb.parsers.multistateSpecies.visitor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import org.apache.commons.lang3.tuple.MutablePair;

import msmb.model.MultiModel;
import msmb.model.MultistateSpecies;
import msmb.model.Species;


import msmb.parsers.multistateSpecies.MR_MultistateSpecies_Parser;
import msmb.parsers.multistateSpecies.MR_MultistateSpecies_ParserConstants;
import msmb.parsers.multistateSpecies.MR_MultistateSpecies_ParserConstantsNOQUOTES;
import msmb.parsers.multistateSpecies.ParseException;
import msmb.parsers.multistateSpecies.syntaxtree.*;
import msmb.utility.CellParsers;

public class MultistateSpecies_UndefinedSitesVisitor extends MultistateSpeciesVisitor
{
	boolean undefinedSites = false;
	 
	public boolean isMultistateSpeciesName_withUndefinedStates() { return undefinedSites; }
	
	public MultistateSpecies_UndefinedSitesVisitor(MultiModel mm) {
		super(mm);
	}

	@Override
	
	   public void visit(MultistateSpecies_SiteSingleElement n) {
		if(ToStringVisitor.toString(n).compareTo(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.UNDEFINED_SITE_SYMBOL))==0) {
			undefinedSites = true;
		}
		  super.visit(n);
	  }
	   
	
			  
}
