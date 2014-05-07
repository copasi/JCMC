package msmb.parsers.multistateSpecies.visitor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import javax.mail.search.IntegerComparisonTerm;
import javax.security.auth.kerberos.KerberosKey;

import org.apache.commons.lang3.tuple.MutablePair;

import msmb.gui.MainGui;
import msmb.model.MultiModel;
import msmb.model.MultistateSpecies;
import msmb.model.Species;


import msmb.parsers.mathExpression.MR_Expression_ParserConstantsNOQUOTES;
import msmb.parsers.multistateSpecies.MR_MultistateSpecies_Parser;
import msmb.parsers.multistateSpecies.MR_MultistateSpecies_ParserConstants;
import msmb.parsers.multistateSpecies.MR_MultistateSpecies_ParserConstantsNOQUOTES;
import msmb.parsers.multistateSpecies.ParseException;
import msmb.parsers.multistateSpecies.syntaxtree.*;
import msmb.utility.CellParsers;

public class MultistateSpeciesVisitor extends DepthFirstVoidVisitor
{
	
	 private String speciesName;
 	 private HashMap<String,Vector<String>> site_states;
	 private HashMap<String, MutablePair<String, String>> pureRange_sites;
	 private HashSet<String> circular_sites;
    Vector<Exception> exceptions = new Vector<Exception>();
	public Vector<Exception> getExceptions() { return exceptions; }
	   
	 private String current_site = null;
	 private Vector<String> current_states = null;
	 private Integer current_high;
	 private Integer current_low;
	 private String current_highString;
	 private String current_lowString;
	
	 public String getSpeciesName() { return speciesName; }
	 public Vector<String> getSite_states(String site) {	return site_states.get(site);	}
	 public Set<String> getSitesNames() {	return site_states.keySet();	}
	 

	 public String getSiteStates_string(String site_name) {
			String ret = new String();
			Vector<String> states = getSite_states(site_name);
			if(states == null  || states.size() == 0) return new String();
			for(String element : states) {
				ret += element + ",";
			}
			if(states.size() >= 1) ret = ret.substring(0,ret.length()-1);
			return ret;
		}
	 
	 public Set<String> getAllSites_names() {	
		 return site_states.keySet();	
		 }
	 public MutablePair<String, String> getPureRangeLimits(String site) { return pureRange_sites.get(site);	}
	 public MutablePair<String, String> getStringRangeLimits() { return new MutablePair<String, String>(current_lowString, current_highString) ;	}
		
	// Vector <Species> singleStateMultiStateSpecies = new Vector<Species>(); //for the expansion
	
	 private Vector<Species> expandedForm = null;
	 private HashMap<String,String> current_site_nextState = new HashMap<String,String>();;
	MultiModel multiModel = null;
	private boolean enforceRangesNumeric;
	
	
	public Vector<Species> getProductExpansion() {		
		//if the product expansion is more than one, it means that there were some sites not specified between the reactant for transfer sites species
		//I am not sure that we want to allow that, but I think that is the most general case
		return expandedForm;	
	}
	
	 public MultistateSpeciesVisitor(MultiModel mm) { multiModel = mm;	}
	 
	/*public MultistateSpeciesVisitor(MultiModel mm, Vector<Species> multistate_reactants, Vector <Species> singleStateMultiStateSpecies) { //constructor used in the expansion
		 try {
			 multiModel = mm;	
			 for(int i = 0; i < multistate_reactants.size(); ++i) {
				 MultistateSpecies reactant =  new MultistateSpecies(multiModel,multistate_reactants.get(i).getDisplayedName());
				 reactants.add(reactant);
			 }
			this.singleStateMultiStateSpecies.addAll(singleStateMultiStateSpecies);
			} catch (Exception e) {
				e.printStackTrace();
		}
	}*/
		
	
	 private Vector<MultistateSpecies> reactants = new Vector<MultistateSpecies>();
	 private HashMap<String, String> aliases_of_reactants = new HashMap<String, String>();
	 
	 public MultistateSpeciesVisitor(MultiModel mm, List<Species> reactants_combination, HashMap<String, String> aliases_1,String sub_prefix) {
		 multiModel = mm;
		 Iterator it = reactants_combination.iterator();
		 while(it.hasNext()) {
			 Species sp = (Species) it.next();
			 String speciesName = sp.getDisplayedName();
			 String realName = null;
			 if(speciesName.startsWith(sub_prefix)) {
				 realName = speciesName.substring(sub_prefix.length());
			 } else {
				 continue;
			 }
			 try {
				reactants.add(new MultistateSpecies(multiModel, realName));
			} catch (Exception e) {
					e.printStackTrace();
			}
		 }
			aliases_of_reactants.clear();
			aliases_of_reactants.putAll(aliases_1);
	 }
	 
	 
	public MultistateSpeciesVisitor(MultiModel mm,	boolean isReactantReactionWithPossibleRanges) {
		 multiModel = mm;	
		 enforceRangesNumeric = !isReactantReactionWithPossibleRanges;
	}

	public MultistateSpeciesVisitor(MultiModel multiModel2,  HashMap<String, String> aliases) {
		if(aliases != null) {
			aliases_of_reactants.clear();
			aliases_of_reactants.putAll(aliases);
		}
	}
	
	


	boolean isRealMultiStateSpecies = false;
	
	public boolean isRealMultiStateSpecies() {
		return isRealMultiStateSpecies || isRealMultiStateSpecies_WithTransferSiteState;
	}
	
	private boolean isBetweenReactants(String product) {
		boolean isBetweenReactants = false;
		for(Species element : reactants) {
			if(product.compareTo(CellParsers.extractMultistateName(element.getDisplayedName())) == 0) {
				isBetweenReactants = true;
						break;
			}
		} 
		return isBetweenReactants;
	}
	

	@Override
	public void visit(CompleteMultistateSpecies_Operator n) {
		expandedForm = new Vector<Species>();
		 site_states = new HashMap<String,Vector<String>>();
		 pureRange_sites = new HashMap<String, MutablePair<String, String>> ();
		 circular_sites = new HashSet<String> ();
		isRealMultiStateSpecies = n.multistateSpecies_Operator.nodeOptional.present();
		speciesName = ToStringVisitor.toString(n.multistateSpecies_Operator.multistateSpecies_Name.nodeChoice.choice);
		
		if(reactants.size()> 0) {
		
			current_site_nextState = new HashMap<String,String>();
			super.visit(n);
			
			Iterator it_react = reactants.iterator();
			
			boolean okExpanded = false;
			while(it_react.hasNext()) {
				MultistateSpecies react = (MultistateSpecies) it_react.next();
				if(speciesName.compareTo(react.getSpeciesName())==0) { //normal succ/prec of a reactant
					expandedForm.add(new Species(buildExpandedForm(react)));
					okExpanded = true;
					break;
				}
			}
			
			if(!okExpanded) {//complex or transfer site species
				Species transferSite = multiModel.getSpecies(speciesName);
				if(!(transferSite instanceof MultistateSpecies)) {
					expandedForm.add(new Species(speciesName));
					okExpanded = true;
				} else {
					try { //check if is a simple multistate species with no operator
						 String fullDef = ToStringVisitor.toString(n);
						ByteArrayInputStream is = new ByteArrayInputStream(fullDef.getBytes("UTF-8"));
						MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is,"UTF-8");
						CompleteMultistateSpecies start2 = react.CompleteMultistateSpecies();
						MultistateSpeciesVisitor v2 = new MultistateSpeciesVisitor(null);
						start2.accept(v2);
						expandedForm.add(new Species(fullDef)); 
						okExpanded = true;
						return;
					} catch(Exception ee) {
					
					Vector<String> currentTransferPieces = new Vector<String>();
					
					MultistateSpecies transferSiteSpecies = (MultistateSpecies)transferSite;
					it_react = reactants.iterator();
					while(it_react.hasNext()) {
						MultistateSpecies react = (MultistateSpecies) it_react.next();
						Iterator it_sites = current_site_nextState.keySet().iterator();
						while(it_sites.hasNext()) {
							String siteAssigned = (String) it_sites.next();
							String transferFrom = current_site_nextState.get(siteAssigned);
							String operator = transferFrom_extractOperator(transferFrom);
							if(operator.length() > 0) {
								transferFrom = transferFrom.substring(operator.length()+1, transferFrom.length()-1);
							}
							if(transferFrom_extractSpeciesName(transferFrom).compareTo(react.getSpeciesName()) ==0 ) {
								String siteFrom = transferFrom_extractSiteName(transferFrom);
								Vector states = new Vector();
								if(operator.length() == 0) {
									states = react.getSiteStates_complete(siteFrom);
								} else {
									
									if(operator.compareTo(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstants.SUCC))==0) {
										Vector react_states = react.getSiteStates_complete(siteFrom);
										if(react_states.size() > 0) {
											MultistateSpecies from = (MultistateSpecies) multiModel.getSpecies(react.getSpeciesName());
											String succState = ((MultistateSpecies) from).getSucc(siteFrom,react_states.get(0).toString());
											if(succState!=null) {
												states.add(succState);
											} else {
												//System.out.println("SOMETHING WRONG WITH THE TRANSFER SITES, RAISE EXCEPTION");
												return;
											}
										}
									
									}
								}
								if(states != null && states.size() == 1) {
									currentTransferPieces.add(siteAssigned+"{"+states.get(0)+"}");
								} else {
									//System.out.println("SOMETHING WRONG WITH THE TRANSFER SITES, RAISE EXCEPTION");
									//return;
									exceptions.add(new Exception("Problems with the transfer state from "+transferFrom));
									return;
								}
							} else { // not the right name, but maybe it is an alias
								
								if(aliases_of_reactants!= null && aliases_of_reactants.containsKey(transferFrom_extractSpeciesName(transferFrom))) {
									String speciesReferenced = aliases_of_reactants.get(transferFrom_extractSpeciesName(transferFrom));
									if(CellParsers.extractMultistateName(speciesReferenced).compareTo(react.getSpeciesName()) ==0 ) {
											String siteFrom = transferFrom_extractSiteName(transferFrom);
											Vector states = new Vector();
											if(operator.length() == 0) {
												states = react.getSiteStates_complete(siteFrom);
											} else {
												
												if(operator.compareTo(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstants.SUCC))==0) {
													Vector react_states = react.getSiteStates_complete(siteFrom);
													if(react_states.size() > 0) {
														MultistateSpecies from = (MultistateSpecies) multiModel.getSpecies(react.getSpeciesName());
														String succState = ((MultistateSpecies) from).getSucc(siteFrom,react_states.get(0).toString());
														if(succState!=null) {
															states.add(succState);
														} else {
															//System.out.println("SOMETHING WRONG WITH THE TRANSFER SITES, RAISE EXCEPTION");
															return;
														}
													}
												
												}
											}
											if(states != null && states.size() == 1) {
												currentTransferPieces.add(siteAssigned+"{"+states.get(0)+"}");
											} else {
												//System.out.println("SOMETHING WRONG WITH THE TRANSFER SITES, RAISE EXCEPTION");
												//return;
												exceptions.add(new Exception("Problems with the transfer state from "+transferFrom));
												return;
											}
									}	 
								} 
								
							}
							
							
						}
					}
					
					if(currentTransferPieces.size() != current_site_nextState.size()){
						exceptions.add(new Exception("Problems with the transfer state of "+ToStringVisitor.toString(n)));
						return;
					}
					String currentTransferSpeciesPieced = new String(speciesName+"(");
					for(int i = 0; i < currentTransferPieces.size(); ++i) {
						String element = currentTransferPieces.get(i);
						currentTransferSpeciesPieced += element+";";
					}
					currentTransferSpeciesPieced = currentTransferSpeciesPieced.substring(0,currentTransferSpeciesPieced.length()-1);
					currentTransferSpeciesPieced += ")";
				
					try {
						//merge the possibly missing sites/states
						//vv
						MultistateSpecies current = new MultistateSpecies(multiModel, currentTransferSpeciesPieced);
						current.mergeStatesWith_Minimum(transferSiteSpecies);
						//^^
						expandedForm = current.getExpandedSpecies(multiModel);
						okExpanded = true;
					} catch (Throwable e) {
						e.printStackTrace();
					}
			
				
				}
				
				}
			}
		
			if(!okExpanded && !isBetweenReactants(speciesName)) {
				expandedForm.add(new Species(ToStringVisitor.toString(n)));
				super.visit(n);
				return;
			}
			
		/*	if(reactants.size() == 1 && speciesName.compareTo(reactants.get(0).getSpeciesName())==0) {
				expandedForm  = buildExpandedForm(reactants.get(0));
				if(expandedForm == null) {
						exceptions.add(new Exception("SOMETHING WRONG"));
				}
			} else {
				boolean okExpanded = false;
				for(int i = 0; i < singleStateMultiStateSpecies.size(); i++) {
					Species sp = singleStateMultiStateSpecies.get(i);
					if(CellParsers.isMultistateSpeciesName(sp.getDisplayedName())) {
						MultistateSpecies msp = null;
						try {
							msp = new MultistateSpecies(multiModel, sp.getDisplayedName());
						if(speciesName.compareTo(msp.getSpeciesName())==0) {
							MultistateSpecies current = new MultistateSpecies(multiModel, ToStringVisitor.toString(n));
							current.mergeStatesWith_Minimum(msp);
							expandedForm = current.getDisplayedName();
							okExpanded = true;
							break;
						}
						} catch (Exception e) {
							if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)	e.printStackTrace();
						}
						
					}
				}
				if(!okExpanded) {
					okExpanded = buildExpandedFormFromTransferSiteState();
				}
				
				
				if(!okExpanded) expandedForm = speciesName;
			}*/
		} else {
			super.visit(n);
		}
	}
	
	/*@Override
	public void visit(CompleteMultistateSpecies_Operator n) {
		isRealMultiStateSpecies = n.multistateSpecies_Operator.nodeOptional.present();
		speciesName = ToStringVisitor.toString(n.multistateSpecies_Operator.multistateSpecies_Name.nodeChoice.choice);
		if(reactants.size()> 0) {
						
			current_site_nextState = new HashMap<String,String>();
			super.visit(n);
			
			if(reactants.size() == 1 && speciesName.compareTo(reactants.get(0).getSpeciesName())==0) {
				expandedForm  = buildExpandedForm(reactants.get(0));
				if(expandedForm == null) {
						exceptions.add(new Exception("SOMETHING WRONG"));
				}
			} else {
				boolean okExpanded = false;
				for(int i = 0; i < singleStateMultiStateSpecies.size(); i++) {
					Species sp = singleStateMultiStateSpecies.get(i);
					if(CellParsers.isMultistateSpeciesName(sp.getDisplayedName())) {
						MultistateSpecies msp = null;
						try {
							msp = new MultistateSpecies(multiModel, sp.getDisplayedName());
						if(speciesName.compareTo(msp.getSpeciesName())==0) {
							MultistateSpecies current = new MultistateSpecies(multiModel, ToStringVisitor.toString(n));
							current.mergeStatesWith_Minimum(msp);
							expandedForm = current.getDisplayedName();
							okExpanded = true;
							break;
						}
						} catch (Exception e) {
							if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)	e.printStackTrace();
						}
						
					}
				}
				if(!okExpanded) {
					okExpanded = buildExpandedFormFromTransferSiteState();
				}
				
				
				if(!okExpanded) expandedForm = speciesName;
			}
		} else {
			super.visit(n);
		}
	}*/

	
	private String transferFrom_extractSpeciesName(String transferFrom) {
		//TO CHANGE to account for the fact that . can exists in names, if between quotes
			return transferFrom.substring(0, transferFrom.lastIndexOf("."));
	}	
	
	private String transferFrom_extractOperator(String transferFrom) {
		
		if(transferFrom.startsWith(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstants.SUCC)+"(")) {
			return MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstants.SUCC);
		} else if(transferFrom.startsWith(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstants.PREC)+"(")) {
			return MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstants.PREC);
		}
		else return "";
  }
	private String transferFrom_extractSiteName(String transferFrom) {
		//TO CHANGE to account for the fact that . can exists in names, if between quotes
			return transferFrom.substring(transferFrom.lastIndexOf(".")+1);
	}
	@Override
	public void visit(CompleteMultistateSpecies n) {
		isRealMultiStateSpecies = n.multistateSpecies.nodeOptional.present();
		speciesName = ToStringVisitor.toString(n.multistateSpecies.multistateSpecies_Name.nodeChoice.choice);
		super.visit(n);
	}
	 
	
	private boolean isRealMultiStateSpecies_WithTransferSiteState = false;
	
	@Override
	 public void visit(MultistateSpecies_Operator_SingleSite n) {
		 if(n.nodeChoice.which==0) {
			 NodeSequence seq = (NodeSequence)(n.nodeChoice.choice);
			 String operator = ToStringVisitor.toString(seq.nodes.get(0));
			 String siteName = ToStringVisitor.toString(seq.nodes.get(2));
			 current_site_nextState.put(siteName, operator);
		 } else {
			 NodeSequence seq = (NodeSequence)(n.nodeChoice.choice);
			 String siteName = ToStringVisitor.toString(seq.nodes.get(0));
			 site_states.put(siteName, null);
			 if(seq.nodes.size()>1) {
				 NodeOptional opt = (NodeOptional)(seq.nodes.get(1));
				 NodeChoice nchoice = (NodeChoice) (opt.node);
				 if(nchoice == null) {//only the name, but should be ok
					 return;
				 }
				 if(nchoice.which == 0) {
					 NodeSequence seq2 = (NodeSequence) (nchoice.choice);
					 if(seq2!=null) {	
						 	isRealMultiStateSpecies_WithTransferSiteState = true;
						 	String state = ToStringVisitor.toString(seq2.nodes.get(1));
						 	 if(state.startsWith("{") &&state.endsWith("}"))
						 		state = state.substring(1, state.length()-1);
					 		current_site_nextState.put(siteName, state);
							 }
				 } else {
					 NodeSequence seq2 = (NodeSequence) (nchoice.choice);
					 if(seq2!=null) {	
						 	String state = ToStringVisitor.toString(seq2);
							 if(state.startsWith("{") &&state.endsWith("}"))
							 		state = state.substring(1, state.length()-1);
					 		current_site_nextState.put(siteName, state);
					 }
					 
				 }
				 
			 }
			 super.visit(n);
		 }
	 }
	 
	 @SuppressWarnings("unchecked")
	private String buildExpandedForm(MultistateSpecies reactant) {
		 String ret = new String();
		if(speciesName.compareTo(reactant.getSpeciesName())!= 0) {
			return speciesName;
		}
			 MultistateSpecies multi = (MultistateSpecies) multiModel.getSpecies(speciesName);
			 ret+= speciesName + "(";
			// HashMap product_sites = new HashMap();
				
			//  current_site_operator.keySet().iterator();
			 Iterator<String> it = reactant.getSitesNames().iterator();
			 while(it.hasNext()) {
					String site = it.next();
					String state=(String) reactant.getSiteStates_complete(site).get(0);
					if(!current_site_nextState.containsKey(site)) {
						ret += site + "{" + state + "};";
					} else {
						String nextState = null;
						String val = current_site_nextState.get(site);
						if(val.compareTo(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstants.SUCC))==0) {
							nextState = multi.getSucc(site,state);//,false);
						} else if(val.compareTo(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstants.PREC))==0) {
							nextState = multi.getPrec(site,state);//,false);
						}/* else if(val.compareTo(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstants.CIRC_R_SHIFT))==0) {
							nextState = multi.getSucc(site,state,true);
						} else if(val.compareTo(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstants.CIRC_L_SHIFT))==0) {
							nextState = multi.getPrec(site,state,true);
						}*/ else {
							nextState = val;
						}
						if(nextState==null) return null;
						ret += site + "{" + nextState + "};";
						
					}
					
				}
			ret = ret.substring(0, ret.length()-1);
			ret += ")";	
		
		 
		return ret;
	}
	 
		
	 
	 
	public void visit(NodeToken n)
	  {
	    //System.out.println("visit " + MR_MultistateSpecies_ParserConstants.tokenImage[n.kind] + "-->" + n.tokenImage);
	  }
	
	@Override
	public void visit(msmb.parsers.multistateSpecies.syntaxtree.MultistateSpecies n) {
		 speciesName = ToStringVisitor.toString(n.multistateSpecies_Name.nodeChoice.choice);
		 site_states = new HashMap<String,Vector<String>>();
		 pureRange_sites = new HashMap<String, MutablePair<String, String>> ();
		 circular_sites = new HashSet<String> ();
		 super.visit(n);
		 if(enforceRangesNumeric) fill_pureRange_sites();
	}
	

	@Override
	public void visit(MultistateSpecies_Operator n) {
		 speciesName = ToStringVisitor.toString(n.multistateSpecies_Name.nodeChoice.choice);
		 site_states = new HashMap<String,Vector<String>>();
		 pureRange_sites = new HashMap<String, MutablePair<String, String>> ();
		 circular_sites = new HashSet<String> ();
		 super.visit(n);
		 if(enforceRangesNumeric) fill_pureRange_sites();
	}
	
	  
	@Override
	public void visit(MultistateSpecies_Operator_SiteSingleState n) {
			  super.visit(n);
			  site_states.put(current_site, current_states);
			  current_site = null;
			  current_states = null;
			  current_lowString = null;
			  current_highString = null;
		  }
	
	
	@Override
	public void visit(MultistateSpecies_Operator_SiteName n) {
		  current_site  = new String(ToStringVisitor.toString(n.nodeChoice.choice));
		  super.visit(n);
	};
	

	  @Override
	  public void visit(MultistateSpecies_SingleStateDefinition n) {
		  super.visit(n);
		  site_states.put(current_site, current_states);
		  if(n.nodeOptional.present()) {
			  NodeSequence states = (NodeSequence) n.nodeOptional.node;
			  if (ToStringVisitor.toString(states.nodes.get(states.size()-1)).compareTo(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.CIRCULAR_FLAG)) == 0) {
				  circular_sites.add(current_site);
			  }
		
		  }
		  current_site = null;
		  current_states = null;
		  current_lowString = null;
		  current_highString = null;
	  }
	  
	  
	  @Override
	  public void visit(MultistateSpecies_SiteName n) {
		  current_site  = new String(ToStringVisitor.toString(n.nodeChoice.choice));
		  super.visit(n);
	  }
	
	  public void visit(MultistateSpecies_SiteSingleElement n) {
		  if(current_states == null) current_states = new Vector<String>();
		  
		  if(n.nodeOptional.present() && current_lowString == null) {
			  current_lowString = ToStringVisitor.toString(n.nodeChoice.choice);
			  current_highString = ToStringVisitor.toString(((NodeSequence)(n.nodeOptional.node)).nodes.get(3));
			  current_states.add(current_lowString+":"+current_highString);
		  } else {
			  current_states.add(ToStringVisitor.toString(n.nodeChoice.choice));
		  }	  
		  
		  super.visit(n);
	  }
	   
	@Override
	public void visit(CompleteMultistateSpecies_RangeString n) {
		  try{
			  current_lowString = null;
			  current_highString = null;
		
		if(n.multistateSpecies_SiteSingleElement.nodeOptional.present()) {
		  current_lowString = ToStringVisitor.toString(n.multistateSpecies_SiteSingleElement.nodeChoice);
		  current_highString = ToStringVisitor.toString(((NodeSequence)(n.multistateSpecies_SiteSingleElement.nodeOptional.node)).nodes.get(3));
		} else { 
			throw new ParseException(); // no range 
		}
		
	} catch(Exception ex) {
		  exceptions.add(ex);
	   }
	}
	   

	@Override
	public void visit(MultistateSpecies_SiteSingleElement_Range n) {
	  try{
		  if(n.multistateSpecies_SiteSingleElement_Range_Limits.nodeChoice.which ==0 &&
				n.multistateSpecies_SiteSingleElement_Range_Limits1.nodeChoice.which ==0
				  ) { //both numbers
			  current_low = new Integer(ToStringVisitor.toString(n.multistateSpecies_SiteSingleElement_Range_Limits));
			  current_high = new Integer(ToStringVisitor.toString(n.multistateSpecies_SiteSingleElement_Range_Limits1));
			  current_lowString = current_low.toString();
			  current_highString = current_high.toString();
			  
			  //System.out.println("low = "+current_low+"; high = "+current_high);
			  if(current_low>=current_high) throw new Exception("Error in \""+ToStringVisitor.toString(n)+"\": lower bound should be < upper bound.");
	 	  } else {
	 		  
	 		 current_lowString = ToStringVisitor.toString(n.multistateSpecies_SiteSingleElement_Range_Limits);
	 		current_highString = ToStringVisitor.toString(n.multistateSpecies_SiteSingleElement_Range_Limits1);
			
	 	  }
		  
 	  } catch(Exception ex) {
		   ex.printStackTrace();
		   exceptions.add(ex);
	   }
	  super.visit(n); 
    
	}
	
	
	  void fill_pureRange_sites() {
		  Iterator<String> it = site_states.keySet().iterator();
			  while(it.hasNext()) {
				  String site = it.next();
						  if(site != null && site_states != null && site_states.get(site)!= null && site_states.get(site).size() == 1) {
					  String states = site_states.get(site).get(0);
					  if(states.contains(MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.RANGE_SEPARATOR))) {
						  
					  
					  try {  InputStream is = new ByteArrayInputStream(states.getBytes("UTF-8"));
					  		MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is);
					  		CompleteMultistateSpecies_Range range = react.CompleteMultistateSpecies_Range();
					  		range.accept(this);
					  		pureRange_sites.put(site, new MutablePair<String,String>(current_lowString,current_highString));
					  } catch (Exception e) {
						  //e.printStackTrace();
						  exceptions.add(new ParseException("Range for site "+site+" used with non-integer values ("+states+")"));
					}
				  	}
				  }
				}
			  
		  }
	  
	
	  
	public HashSet<String> getCircularSites() {
		return circular_sites;
	}
	
	

	public boolean isRealMultiStateSpecies_WithTransferSiteState() {
		return isRealMultiStateSpecies_WithTransferSiteState;
	}
	
			  
	}
