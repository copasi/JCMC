package msmb.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import msmb.debugTab.DebugConstants;
import msmb.debugTab.DebugMessage;
import msmb.gui.MainGui;
import msmb.parsers.chemicalReaction.MR_ChemicalReaction_Parser;
import msmb.parsers.chemicalReaction.syntaxtree.CompleteReaction;
import msmb.parsers.chemicalReaction.visitor.ExtractSubProdModVisitor;
import msmb.parsers.mathExpression.MR_Expression_Parser_ReducedParserException;
import msmb.parsers.mathExpression.ParseException;
import msmb.parsers.mathExpression.syntaxtree.CompleteExpression;
import msmb.parsers.mathExpression.visitor.Look4UndefinedMisusedVisitor;
import msmb.utility.CellParsers;
import msmb.utility.Constants;
import msmb.utility.MySyntaxException;


public class Reaction {
	String name = new String();
	String notes = new String();
	String SBMLid = new String();
	int type = Constants.ReactionType.MASS_ACTION.copasiType;
	String rateLaw = new String();
	private String editableRateLaw = rateLaw;
	String reactionString = new String();
	
	
	public Vector<String> getSubstrates(MultiModel m) {
		Vector ret = new Vector<String>();
		Vector metabolites;
		try {
			metabolites = getSubProdMod(m);
		} catch (Throwable e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
			return null;
		}
		Vector subs = (Vector)metabolites.get(0);
		 ret.addAll(subs);
		return ret;
	 
	}
	
	public Vector<String> getProducts(MultiModel m) {
		Vector ret = new Vector<String>();
		Vector metabolites;
		try {
			metabolites = getSubProdMod(m);
		} catch (Throwable e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
			return null;
		}
		Vector prod = (Vector)metabolites.get(1);
		 ret.addAll(prod);
		return ret;
	 
	}
	
	
	public Vector<String> getModifiers(MultiModel m) {
		Vector<String> ret = new Vector<String>();
		Vector metabolites;
		try {
			metabolites = getSubProdMod(m);
		} catch (Throwable e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
			return null;
		}
		Vector mod = (Vector)metabolites.get(2);
		 ret.addAll(mod);
		return ret;
	 
	}
	
	private Vector getSubProdMod(MultiModel m) throws Throwable {
		Vector metabolites = new Vector<Object>();

		InputStream is = new ByteArrayInputStream(reactionString.getBytes("UTF-8"));
		MR_ChemicalReaction_Parser react = new MR_ChemicalReaction_Parser(is,"UTF-8");
		CompleteReaction start = react.CompleteReaction();
		ExtractSubProdModVisitor v = new ExtractSubProdModVisitor(m);
		start.accept(v);

		if(v.getExceptions().size() != 0) {
			throw new Exception(v.getExceptions().get(0).getMessage());
		}

		metabolites.addAll(v.getAll_asString());
		
		return metabolites;
	}


	public String getRateLaw() { 	return rateLaw.trim();	}
		
	public void setRateLaw(MultiModel m, String expr) throws Throwable {	
		if(expr.compareTo(Constants.NOT_EDITABLE_VIEW) == 0) return;
		this.rateLaw = expr;	
		if(expr.length() == 0) return ;
		try {
			CellParsers.parseExpression_getUndefMisused(m,rateLaw, Constants.TitlesTabs.REACTIONS.getDescription(),Constants.ReactionsColumns.KINETIC_LAW.getDescription());
		} catch (Exception ex) {
			if(ex instanceof MySyntaxException){ 
				Vector metabolites;
				
					metabolites = CellParsers.parseReaction(m,reactionString,-1);
					HashMap<String, String> aliases = CellParsers.getAllAliases(reactionString);
					HashMap<Integer, String> aliases2 = CellParsers.getAllAliases_2(reactionString);
				
						Vector singleConfigurations = m.expandReaction(metabolites, aliases, aliases2,-1);
				
					  InputStream is = new ByteArrayInputStream(rateLaw.getBytes("UTF-8"));
					  MR_Expression_Parser_ReducedParserException parser = new MR_Expression_Parser_ReducedParserException(is,"UTF-8");
					  CompleteExpression root = parser.CompleteExpression();
					  Look4UndefinedMisusedVisitor undefVisitor = new Look4UndefinedMisusedVisitor(m);
					  root.accept(undefVisitor);
					  Vector<String> undef = undefVisitor.getUndefinedElements();
					  Vector<String> misused = undefVisitor.getMisusedElements();
					  
					  Vector<String> real_undef = new Vector();
					  
					for(int j = 0; j < singleConfigurations.size(); j++) {
						Vector<?> expandedReaction = (Vector<?>) singleConfigurations.get(j);

						Vector<?> subs = (Vector<?>)expandedReaction.get(0);
						Vector<?> prod =(Vector<?>)expandedReaction.get(1);
						Vector<?> mod = (Vector<?>)expandedReaction.get(2);

						for(int i1 = 0; i1 < subs.size(); i1++) {
							String s = (String)subs.get(i1);
							s = m.extractName(s);
							if(!m.containsSpecies(s))  {
								throw ex;
							} else {
								Species sp = m.getSpecies(s);
								if(sp instanceof MultistateSpecies) {
									MultistateSpecies m1 = (MultistateSpecies)sp;
									
									Set sites = m1.getSitesNames();
									for(int i = 0; i < undef.size(); i++) {
										String current = undef.get(0);
										if(sites.contains(current)) {
											continue;
										} else {
											real_undef.add(current);
										}
									}
									
									if(real_undef.size() != 0 || misused.size() != 0) {
									    String message = new String();
										if(real_undef.size() >0) {
											 message += "Missing element definition: " + real_undef.toString();
										}
										if(misused.size() > 0) message += "\n" + "The following elements are misused: " +misused.toString();
										throw new MySyntaxException(((MySyntaxException) ex).getColumn(), message,((MySyntaxException) ex).getTable());
								  } 
									
									
								} else {
									throw ex;
								}
							}
						}
					}
						
					
					
			
			} else throw ex;
		}
		editableRateLaw = expr;
	}

	public void setRateLaw_withoutParsing(String expression) {	this.rateLaw = expression; editableRateLaw = expression;	}

	
	public String getEditableRateLaw() {	return editableRateLaw; }
	
	
	public String getSBMLid() {return SBMLid;}
	public void setSBMLid(String sBMLid) {	SBMLid = sBMLid;}
	
	public Reaction() {}
	public Reaction(String name) { this.setName(name);}
	
	public Vector getAllFields() { 
		Vector r = new Vector();
		r.add(this.getName());
		r.add(this.getReactionString());
		r.add(this.getType());
		r.add(this.getRateLaw());
		r.add(this.getNotes());
		r.add(this.getSBMLid());
		return r;
	}
	
	public String getReactionString() {	return reactionString;	}
	public void setReactionString(String r) {	reactionString = new String(r);	}
	


	public String getName() { return name; }
	public void setName(String name) {	this.name = name;	}
	public int getType() {	return type;	}
	public void setType(int CMetab_Type) {		this.type = CMetab_Type;	}
	public String getNotes() {		return notes;	}
	public void setNotes(String notes) {		this.notes = notes;	}
	
	

	public void setEditableRateLaw(MultiModel m,String editableString) throws Throwable {
		setRateLaw(m,editableString);		
	}



	



}
