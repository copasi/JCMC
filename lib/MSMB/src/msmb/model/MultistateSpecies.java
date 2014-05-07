package msmb.model;

import msmb.gui.MainGui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import msmb.utility.*;
import msmb.utility.Constants.BooleanType;

import org.apache.commons.lang3.tuple.MutablePair;

import msmb.parsers.multistateSpecies.MR_MultistateSpecies_Parser;
import msmb.parsers.multistateSpecies.MR_MultistateSpecies_ParserConstants;
import msmb.parsers.multistateSpecies.ParseException;
import msmb.parsers.multistateSpecies.syntaxtree.CompleteMultistateSpecies;
import msmb.parsers.multistateSpecies.syntaxtree.CompleteMultistateSpecies_RangeString;
import msmb.parsers.multistateSpecies.visitor.MultistateSpeciesVisitor;

import com.google.common.collect.Sets;




public class MultistateSpecies extends Species implements Serializable {
	
	private TreeMap<String, Vector<String>> sites = new TreeMap<String, Vector<String>>();
	private HashMap<String, SiteType> sites_type = new HashMap<String, SiteType>();
	private HashMap<String, MutablePair<String,String>> sites_rangesWithVariables = new HashMap<String, MutablePair<String,String>>();
	MultiModel multiModel = null;
	
	
	private HashMap<String, String> initialQuantities = new HashMap<String, String>();
	private boolean evaluateLimits;
	public boolean definitionHasIssues = false;
	public HashMap<String, String> getInitialQuantity_multi() {	return this.initialQuantities; }
	public void setInitialQuantity(HashMap<String,String> initials) { 	if(initials!= null) this.initialQuantities.putAll(initials);	}
	
	public void setSitesRangesWithVariables(HashMap<String, MutablePair<String,String>> sites_rangesWithVar) {
	sites_rangesWithVariables.clear();
		for(String siteName : sites_rangesWithVar.keySet()) {
			sites_rangesWithVariables.put(siteName, sites_rangesWithVar.get(siteName));
			sites_type.put(siteName,new SiteType(SiteType.RANGE));
		}
	}
	
	public HashMap<String, MutablePair<String,String>> getSitesRangesWithVariables() {
		HashMap<String, MutablePair<String,String>> newCollection = new HashMap<String, MutablePair<String,String>>();
		newCollection.putAll(sites_rangesWithVariables);
		return newCollection;
	}
	
	@Override public String getDisplayedName() { return printCompleteDefinition(); }
	
	public String getInitial_singleConfiguration(Vector<Vector> sites_value) {
		String key = this.name + "(";
		for(int i = 0; i < sites_value.size(); i = i + 2) {
			key += sites_value.get(i) + "{" + sites_value.get(i+1) + "}";
			key += ";";
		}
		key = key.substring(0, key.length()-1);
		key += ")";
		return this.initialQuantities.get(key);
	}
	
	
	public String getInitial_singleConfiguration(Species sp) {
		if(!this.initialQuantities.containsKey(sp.getDisplayedName())) return new String(MainGui.species_defaultInitialValue);
		return this.initialQuantities.get(sp.getDisplayedName());
		
	}
	

	public void newParser(String complete_string, boolean isReactantReactionWithPossibleRanges) throws MySyntaxException {
		 try {
			 InputStream is = new ByteArrayInputStream(complete_string.getBytes("UTF-8"));
			 MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is,"UTF-8");
			 CompleteMultistateSpecies start = react.CompleteMultistateSpecies();
			 MultistateSpeciesVisitor v = new MultistateSpeciesVisitor(multiModel,isReactantReactionWithPossibleRanges);
			 start.accept(v);
			 if(v.getExceptions().size()>0) throw v.getExceptions().get(0);
			 HashSet<String> circularSites = v.getCircularSites();
			 this.setName(v.getSpeciesName());
			  initialQuantities = new HashMap<String, String>();
			  Iterator<String> it = v.getAllSites_names().iterator();
			  try {
				  this.setCompartment(multiModel,MainGui.compartment_default_for_dialog_window);
			  } catch(MySyntaxException ex) {
				//do nothing?
			 }
			  while(it.hasNext()) {
				  String site_name = it.next();
				  MutablePair<String, String> pureRangeSite = v.getPureRangeLimits(site_name);
				  if(pureRangeSite!= null) {
					  try {
						  this.addSite_range(site_name, pureRangeSite.getLeft(), pureRangeSite.getRight());
					  } catch(Exception ex) {
						 // ex.printStackTrace();
						  //undefined variables
						  this.addSite_string(site_name, pureRangeSite.left + ":"+pureRangeSite.right);
						  this.sites_type.put(name,new SiteType(SiteType.RANGE));
						  throw new MySyntaxException(-1,"Problems with range limits: "+ex.getMessage(), null);//because other tables/col can call this method, so it's up to them to add the proper debug message
					  }
				  } else {
					  Vector<String> single_states = v.getSite_states(site_name);
					  if(single_states!=null) this.addSite_vector(site_name, single_states);
					  else {
						  this.addSite_vector(site_name, new Vector());
					  }
				  }
				  if(circularSites.contains(site_name))  this.setCircular(site_name, true);
				  else  this.setCircular(site_name, false);
			  }
			
			  
		 } catch(MySyntaxException ex) {
			 //ex.printStackTrace();
			throw new MySyntaxException(ex.getColumn(),"Problem parsing species:\n"+ex.getMessage(), null);//because other tables/col can call this method, so it's up to them to add the proper debug message

		 } catch (Throwable e) {
					// e.printStackTrace();
			 		if(evaluateLimits)	throw new MySyntaxException(-1,"Problem parsing species:\n"+e.getMessage(), null); //because other tables/col can call this method, so it's up to them to add the proper debug message

		}

	}
	
	public MultistateSpecies(MultiModel m, String complete_string) throws Exception { 
		this(m,complete_string,false,true);
	}
	
	public MultistateSpecies(MultiModel m, String complete_string,boolean isReactantReactionWithPossibleRanges) throws Exception { 
		this(m,complete_string,isReactantReactionWithPossibleRanges,true);
	}
	
	
	public MultistateSpecies(MultiModel m, String complete_string, boolean isReactantReactionWithPossibleRanges,boolean evaluateLimits) throws Exception{
		multiModel = m;
		this.evaluateLimits = evaluateLimits;
		if(complete_string.length() == 0) return;
		newParser(complete_string,isReactantReactionWithPossibleRanges);
		this.setType(Constants.SpeciesType.MULTISTATE.copasiType);
		
	}
	
	
	public void addSite_range(String name, String start, String end) throws Throwable {
		Vector st = new Vector();
		Integer start_bound = -1;
		Integer end_bound =-1;
		sites_type.put(name,new SiteType(SiteType.RANGE));

		try{
			start_bound = new Integer(start);
		} catch(Exception ex) {
			sites_rangesWithVariables.put(name, new MutablePair<String, String>(start, end));
			//not a number, need to find the numerical value in the model
			/*GlobalQ limit = multiModel.getGlobalQ(start);
			if(limit == null || limit.type != Constants.GlobalQType.FIXED.copasiType) {
				throw new Exception("Lower bound variable "+start+ " is not defined or its type is not fixed.");
			}
			try{
				Long value = Math.round(Double.parseDouble(limit.getInitialValue()));
				start_bound = value.intValue();
			} catch(Exception ex2) {
				//initial expression to be evaluated
				try {
					start_bound = CellParsers.evaluateExpression(limit.getInitialValue());
				} catch (Throwable ex3) {
					//e.printStackTrace();
					throw ex3;
				}
			}*/
			start_bound = multiModel.getGlobalQ_integerValue(start);
		}
		
		try{
			end_bound = new Integer(end);
		} catch(Exception ex) {
			sites_rangesWithVariables.put(name, new MutablePair<String, String>(start, end));
			//not a number, need to find the numerical value in the model
		/*	GlobalQ limit = multiModel.getGlobalQ(end);
			if(limit == null || limit.type != Constants.GlobalQType.FIXED.copasiType) {
				throw new Exception("Upper bound variable "+end+ " is not defined or its type is not fixed.");
			}
			try{
				Long value = Math.round(Double.parseDouble(limit.getInitialValue()));
				end_bound = value.intValue();
			} catch(Exception ex2) {
				//initial expression to be evaluated
				try {
					end_bound = CellParsers.evaluateExpression(limit.getInitialValue());
				} catch (Throwable e) {
					//e.printStackTrace();
					throw e;
				}
			}*/
			end_bound = multiModel.getGlobalQ_integerValue(end);
		}
		if(start_bound == null || end_bound == null) {
			throw new Exception("Problems evaluating one of the indexes ("+start+ ", "+end+")");
		}
		if(start_bound > end_bound) {
			throw new Exception("Lower bound variable "+start+ " > upper bound variable "+end);
		}
		for(int i = start_bound; i <= end_bound; i++) { st.add(Integer.toString(i)); }
		sites.put(name, st);
		sites_rangesWithVariables.put(name, new MutablePair<String, String>(start, end));
	}
	
	public void addSite_vector(String name, Vector states) throws Exception {
		Iterator it = states.iterator();
		boolean foundTrue = false, foundFalse = false;
		while(it.hasNext()) {
			String s = (String)it.next();
			if(BooleanType.isTrue(s)){
				foundTrue = true;
			}
			if(BooleanType.isFalse(s)){
				foundFalse = true;
			}
		}
		if((foundFalse||foundTrue) && states.size()>2) {
			throw new Exception("Boolean keyword used in a list different from \""+BooleanType.TRUE.getDescription()+","+BooleanType.FALSE.getDescription()+"\"");
		}
		
		sites.put(name, states);
		if(foundFalse||foundTrue) {
			sites_type.put(name, new SiteType(SiteType.BOOLEAN));
		}
		else sites_type.put(name, new SiteType(SiteType.LIST));
	}
	
	
	
	public void addSite_string(String name, String states) throws Exception {
		boolean shouldBeRange = false;
		
		
		try {
			MutablePair<String, String> possibleRangeWithVariables = CellParsers.getMultistateSpecies_rangeWithVariables(states,true);
			try {
					if(possibleRangeWithVariables.left!=null) {
					addSite_range(name, possibleRangeWithVariables.left, possibleRangeWithVariables.right);
					return;
				}
			} catch(Throwable ex) {
				//ex.printStackTrace();
				//If problems, add the list with the strings;
				shouldBeRange = true;
			}
	
		} catch(Throwable ex) {
			//ex.printStackTrace();
			//not a range
			shouldBeRange = false;
		}
	
		
		Vector parsedStates = CellParsers.parseMultistateSpecies_states(states);
		sites.put(name, parsedStates);
		if(parsedStates.size()==2 && 
				( BooleanType.isTrue((String)parsedStates.get(0))
				&& BooleanType.isFalse((String)parsedStates.get(1)) )
						)
				
		{
			sites_type.put(name, new SiteType(SiteType.BOOLEAN));
		} else {
				if(!shouldBeRange)sites_type.put(name,new SiteType(SiteType.LIST));
		}
		//sites_fromRanges.remove(name);
		//sites_start_indexes.put(name, new Integer(1));
	}
	
	public void deleteSite(String name) {
		sites.remove(name);
		sites_type.remove(name);
		sites_rangesWithVariables.remove(name);
		//sites_fromRanges.remove(name);
		//sites_boolean.remove(name);
		//sites_start_indexes.remove(name);
	}
	
	
	public void addNewState(String name, String state) {
		Vector old = (Vector)sites.get(name);
		old.add(state);
		sites.put(name, old);
	}
	
	public Set<String> getSitesNames() {	return this.sites.keySet();	}
	
	public Vector getSiteStates_complete(String site_name) {//always unfolded
		return (Vector)sites.get(site_name);
	}
	
	
	public String getSiteStates_string(String site_name) {
		String ret = new String();
		Vector states = getSiteStates(site_name);
		String start = (String)states.get(0);
		String end = (String)states.get(1);
		String list = (String)states.get(2);
		boolean bool = (Boolean)states.get(3);
		if(start.equals("0") & end.equals("0")) {
			ret = list;
		}
		else{
			ret = start + ":"+end;
		}
		return ret;
	}
	
	
	public Vector getSiteStates(String site_name) {//compacted if possible
		Vector start_end_list = new Vector();
		String start = new String("0");
		String end = new String("0");
		String list = new String("");
		
		Vector states = (Vector)sites.get(site_name);
		//if(this.sites_fromRanges.contains(site_name)) {
		if(this.sites_type.get(site_name).getType()== SiteType.RANGE) {
			
			if(sites_rangesWithVariables.containsKey(site_name)) {
				MutablePair<String, String> ranges = sites_rangesWithVariables.get(site_name);
				start = ranges.left;
				end = ranges.right;
			} else {
				start = (String)states.get(0);
				end = (String)states.get(states.size()-1);
			}
		} else {
			String separator = MR_MultistateSpecies_ParserConstants.tokenImage[MR_MultistateSpecies_Parser.SITE_STATES_SEPARATOR];
			separator = separator.substring(1,separator.length()-1);
			//if(!sites_boolean.contains(site_name)) {
				for(int i = 0; i < states.size()-1 ;i++) {
					list += (String)states.get(i)+ separator;
				}
				list +=(String)states.get(states.size()-1);
		/*	} else {
				list += Constants.BooleanType.TRUE.getDescription() + "," + Constants.BooleanType.FALSE.getDescription();
			}*/
		}
		
		start_end_list.add(start);
		start_end_list.add(end);
		start_end_list.add(list);
		//start_end_list.add(new Boolean(sites_boolean.contains(site_name)));
		start_end_list.add(new Boolean(sites_type.get(site_name).getType()==SiteType.BOOLEAN));
		return start_end_list;
	}
	
	public String printSite(String name) {
		
		return printSite(name, false);
	}
	
	public String printSiteWithUndefinedElements(String site_name) {
			if(definitionHasIssues==false) definitionHasIssues = true;
			MutablePair<String, String> element = sites_rangesWithVariables.get(site_name);
			if(element == null) return new String();
			return site_name+"{"+element.left + ":"+element.right+"}";
	}
	
	public String printSite(String name,boolean alphabetical ) {
		return printSite(name, alphabetical, false);
	}
	
	private String printSite(String name, boolean alphabetical, boolean actualValuesOfVariableStates
		//	,boolean withStartIndex
			) {
		if(name.contains("succ") || name.contains("prec")) return name;
		
		String r = new String();
		Vector values = new Vector();
		values.addAll((Vector)sites.get(name));
		if(values.size() ==0) return name;
		
		if(alphabetical) Collections.sort(values);
		if(values == null) return r;
		
		r+= name+"{";
		SiteType ty = this.sites_type.get(name);
		//if(sites_fromRanges.contains(name)) {.
		if(ty.getType() == SiteType.RANGE){
			if(sites_rangesWithVariables.containsKey(name)) {
				MutablePair<String, String> ranges = sites_rangesWithVariables.get(name);
				if(!actualValuesOfVariableStates) {
					r+= ranges.left + ":" + ranges.right;
				}
				else {	
					GlobalQ limit = multiModel.getGlobalQ(ranges.left);
					if(limit == null || limit.type != Constants.GlobalQType.FIXED.copasiType) {
						//throw new Exception("Lower bound variable "+ranges.left+ " is not defined or its type is not fixed.");
						r+= ranges.left + ":";
					} else {
						try{
							Long value = Math.round(Double.parseDouble(limit.getInitialValue()));
							r+= value.intValue() + ":";
						} catch(Exception ex2) {
							
							try {
								Integer ret = CellParsers.evaluateExpression(limit.getInitialValue());
								r+= ret + ":";
							} catch (Throwable e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					limit = multiModel.getGlobalQ(ranges.right);
					if(limit == null || limit.type != Constants.GlobalQType.FIXED.copasiType) {
						//throw new Exception("Lower bound variable "+ranges.left+ " is not defined or its type is not fixed.");
						r+= ranges.right;
					} else {
						try{
							Long value = Math.round(Double.parseDouble(limit.getInitialValue()));
							r+= value.intValue();
						} catch(Exception ex2) {
							
							try {
								Integer ret = CellParsers.evaluateExpression(limit.getInitialValue());
								r+= ret;
							} catch (Throwable e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}
					}
				}
			}
			else r+=values.get(0) + ":" + values.get(values.size()-1);
		} else if(ty.getType() == SiteType.BOOLEAN) {
			r+= Constants.BooleanType.TRUE.getDescription() + "," + Constants.BooleanType.FALSE.getDescription();
	    }else {
			for(int i = 0; i < values.size()-1; i++) {
				r+= values.get(i) + ",";
			}
			if(values.size() > 0)r+= values.get(values.size()-1);
		}
		
		
		r+="}";
		if(ty.isCircular()) r+="c";
		
		return r;
	}

	public MultistateSpecies mergeStatesWith(MultistateSpecies newSpecies, boolean fromMultistateBuilder) throws Exception {
		if(this.printCompleteDefinition().compareTo(newSpecies.printCompleteDefinition()) == 0) {
			return this;
		}

		MultistateSpecies merged = new MultistateSpecies(multiModel,this.getDisplayedName());
		merged.setName(newSpecies.getSpeciesName());
		Iterator site_it2 = newSpecies.sites.keySet().iterator();
		while (site_it2.hasNext()) {  
			String key = site_it2.next().toString();  
			Vector states = (Vector)newSpecies.sites.get(key);
			Vector statesOld = (Vector)merged.sites.get(key);
			
			SiteType thisType = this.sites_type.get(key);
			SiteType mergedType = merged.sites_type.get(key);
			
			if((thisType.circular != mergedType.circular) || thisType.type != mergedType.type) {
				throw new MySyntaxException(Constants.SpeciesColumns.NAME.index, "Inconsistent site type during merging.", Constants.TitlesTabs.SPECIES.getDescription());
			}
			if(thisType.getType() == SiteType.BOOLEAN) {
				continue;
			} else if(thisType.getType() == SiteType.RANGE) {
				HashSet statesWithoutDuplicates = new HashSet();
			    if(states != null) statesWithoutDuplicates.addAll(states);
			    if(statesOld != null) statesWithoutDuplicates.addAll(statesOld);
			    Vector ordered = new Vector(Arrays.asList(statesWithoutDuplicates.toArray()));
			    for(int i = 0; i < ordered.size(); i++) { //are they all numbers?
			    	try {
			    		Integer.parseInt((String)ordered.get(i));	    	 
			    	} catch(Exception ex) {
			    		if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 
			    			ex.printStackTrace();
			    		throw new MySyntaxException(Constants.SpeciesColumns.NAME.index, "Site type range with states no numbers", Constants.TitlesTabs.SPECIES.getDescription());
			    	}
			    }
			   
			    Collections.sort(ordered, new AlphanumComparator());
				  
				merged.sites.put(key, ordered);	
				continue;
			} else {
				 Vector all = new Vector();
				 if(statesOld != null) all.addAll(statesOld);
				HashSet statesAlreadyAdded = new HashSet();
				 if(statesOld != null) statesAlreadyAdded.addAll(statesOld);
				if(states!= null) {
					for(int i = 0; i < states.size(); i++) {
							if(!statesAlreadyAdded.contains(states.get(i))) all.add(states.get(i));
					}
				}
				merged.sites.put(key, all);
			}
		
		}  

		merged.setInitialQuantity(this.getInitialQuantity_multi());
		merged.setInitialQuantity(newSpecies.getInitialQuantity_multi());

		merged.setType(this.getType());
		merged.setCompartment(multiModel,this.getCompartment_listString());

		return merged;
	}
	
	
	
	public String printCompleteDefinition() {
		String r = this.name; 
		r = CellParsers.cleanName(r);
		Set<String> names = getSitesNames();
		 HashMap<String, MutablePair<String, String>> sitesWithPossibleProblems = this.getSitesRangesWithVariables();
			
		if(names.size() <= 0 && sitesWithPossibleProblems.size() <=0) return r;
		r += "(";
		 Iterator iterator = names.iterator();  
		 int i = 0;
		 while (iterator.hasNext()) {  
		       String pr = printSite((String)iterator.next());//, true); 
		       r+=pr+";";
	    }
		 
		   
		    Iterator<String> iterator2 = sitesWithPossibleProblems.keySet().iterator();  
		    while (iterator2.hasNext()) {  
		       String site_name = iterator2.next();
		       if(names.contains(site_name)) continue; //site already printed before
		       String pr = printSiteWithUndefinedElements(site_name);
		       r+=pr+";";
		   }  
		    
		    
		 r = r.substring(0,r.length()-1) + ")";
		return r;
	}
	
	

	
	public Vector<MultistateSpecies> getExpandedSpecies(MultiModel m, boolean onlyCombinationSitesStates) throws Throwable {
		Vector<MultistateSpecies> ret = new Vector<MultistateSpecies>();
		
		Set keySet = this.sites.keySet();
		
		 List<String> keys = new ArrayList<String>(keySet);
		 List<Set<String>> values = new Vector<Set<String>>();
		 Iterator sites_iterator = keySet.iterator();
		while (sites_iterator.hasNext()) {  
			String name = sites_iterator.next().toString();  
		    Vector<String> states = (Vector<String>)this.sites.get(name);
		 	Set<String> values_site = Sets.newLinkedHashSet(states);
		     values.add(values_site);
		}
		   		    
		Set<List<String>> product = Sets.cartesianProduct(values);
		
			
		for (List<String> v : product) {
			   Vector<String> site_value = new Vector<String>();
			   for (int i = 0; i < keys.size(); ++i) {
				   String key = keys.get(i).toString();
				   
		            String value = v.get(i).toString();
		            site_value.add(key);
		            site_value.add(value);
		        }
		        MultistateSpecies singleConf = new MultistateSpecies(m,createSingleConfigurationState(m,site_value).getDisplayedName());
		        ret.add(singleConf);
		    }
		return ret;
	}
	
	public Vector<Species> getExpandedSpecies(MultiModel m) throws Throwable {
		Vector<Species> ret = new Vector<Species>();
		
		Set keySet = this.sites.keySet();
		
		 List<String> keys = new ArrayList<String>(keySet);
		 List<Set<String>> values = new Vector<Set<String>>();
		 Iterator sites_iterator = keySet.iterator();
		while (sites_iterator.hasNext()) {  
			String name = sites_iterator.next().toString();  
		    Vector<String> states = (Vector<String>)this.sites.get(name);
		 	Set<String> values_site = Sets.newLinkedHashSet(states);
		     values.add(values_site);
		}
		   		    
		Set<List<String>> product = Sets.cartesianProduct(values);
		
			
		for (List<String> v : product) {
			   Vector<String> site_value = new Vector<String>();
			   for (int i = 0; i < keys.size(); ++i) {
				   String key = keys.get(i).toString();
				   
		            String value = v.get(i).toString();
		            site_value.add(key);
		            site_value.add(value);
		        }
		        Species singleConf = createSingleConfigurationState(m,site_value);
		        singleConf.setCompartment(multiModel,this.getCompartment_listString());
		        if(this.getEditableExpression().trim().length()==0){
		        	singleConf.setType(Constants.SpeciesType.REACTIONS.copasiType);
		        } else {
		          	singleConf.setType(Constants.SpeciesType.ASSIGNMENT.copasiType);
		    			singleConf.setExpression(multiModel, CellParsers.evaluateExpressionWithDependentSum(this.getExpression(), new MultistateSpecies(m, singleConf.getDisplayedName()))); 
				}
		       
		        ret.add(singleConf);
		    }
		return ret;
	}

	private Species createSingleConfigurationState(MultiModel m,Vector<String> site_value) throws Throwable {
		String name = new String(this.getSpeciesName());
		if(site_value.size() > 0) {
			name += "(";
			for(int i = 0; i < site_value.size(); i=i+2) {
				name+= site_value.get(i) + "{"+ site_value.get(i+1)+"}"; 
				name+=";";
			}
			name = name.substring(0, name.length()-1);
			name += ")";
		}
		Species ret = new Species();
		ret.setName(name);
		ret.setCompartment(multiModel,this.getCompartment_listString());
		ret.setInitialQuantity(m,new String(MainGui.species_defaultInitialValue));
		
		ret.setType(this.getType());
		
		return ret;
	}

	public Vector<String> getExpandedSites() {
		
		if(this.sites.size() ==0) return new Vector(); 
			
		Vector<String> ret = new Vector<String>();
		
		 List<String> keys = new ArrayList<String>(this.sites.keySet());
		 List<Set<String>> values = new Vector<Set<String>>();
		Iterator sites_iterator = this.sites.keySet().iterator();
		while (sites_iterator.hasNext()) {  
			String name = sites_iterator.next().toString();  
		    Vector<String> states = (Vector<String>)this.sites.get(name);
		 	Set<String> values_site = Sets.newLinkedHashSet(states);
		    values.add(values_site);
		}
		   		    
	    Set<List<String>> product = Sets.cartesianProduct(values);
	
	    for (List<String> v : product) {
			   String singleConf = new String();
		       
				for (int i = 0; i < keys.size(); ++i) {
		    	    String key = (String) keys.get(i);
		            String value = v.get(i);
		            singleConf += key + "{";
		            singleConf += value + "};";
		        }
				singleConf = singleConf.substring(0, singleConf.length()-1);
		        ret.add(singleConf);
		    }
		return ret;
	}
	
	
	public Vector<Vector> getExpandedVectors() {
		
		if(this.sites.size() ==0) return new Vector(); 
			
		Vector<Vector> ret = new Vector<Vector>();
		
		 List<String> keys = new ArrayList<String>(this.sites.keySet());
		 List<Set<String>> values = new Vector<Set<String>>();
		 Iterator sites_iterator = this.sites.keySet().iterator();
		while (sites_iterator.hasNext()) {  
			String name = sites_iterator.next().toString();  
		    Vector states = (Vector)this.sites.get(name);
		 	Set<String> values_site = Sets.newLinkedHashSet(states);
		    values.add(values_site);
		}
		   		    
	    Set<List<String>> product = Sets.cartesianProduct(values);
	    
		for (List<String> v : product) {
			  
			   Vector singleConf = new Vector();
		       
				for (int i = 0; i < keys.size(); ++i) {
		    	    String key = (String) keys.get(i);
		            String value = v.get(i);
		            singleConf.add(key);
		            singleConf.add(value);
		        }
				 ret.add(singleConf);
		    }
		return ret;
	}


	
	//public String getSucc(String site, String state, boolean circular) {
	public String getSucc(String site, String state) {
		Vector states = (Vector) sites.get(site);
		int i = 0;
		for( i = 0; i < states.size()-1; i++) {
	    	 if(((String)states.get(i)).compareTo(state) == 0) {
	    		 return (String)states.get(i+1);
	    	 }	    	 
	     }
		if(!isCircular(site)) return null;
		return (String)states.get(0);
	}
	
	//public String getPrec(String site, String state, boolean circular) {
	public String getPrec(String site, String state) {
		Vector states = (Vector) sites.get(site);
		for(int i = 1; i < states.size(); i++) {
	    	 if(((String)states.get(i)).compareTo(state) == 0) {
	    		 return (String)states.get(i-1);
	    	 }	    	 
	    	 
	     }
//		if(sites_fromRanges.contains(site)) return null;
		if(!isCircular(site)) return null;
		return (String)states.get(states.size()-1);
	}
	
	
	public boolean isCircular(String site) {
		SiteType s = sites_type.get(site);
		return s.isCircular();
	}
	
	public boolean isRange(String site) {
		return this.sites_type.get(site).getType()== SiteType.RANGE;
	}
	
	
	private void setCircular(String site, boolean b) {
		SiteType s = sites_type.get(site);
		if(s == null) return;
		s.setCircular(b);
		sites_type.put(site, s);
	}
	
	public void mergeStatesWith_Minimum(MultistateSpecies existing) throws Exception {
		this.setType(existing.getType());
		this.expandRangesSitesFrom(existing);
		
		if(existing.printCompleteDefinition().compareTo(this.printCompleteDefinition()) == 0) {
			return;
		}
		
		Iterator site_it2 = existing.sites.keySet().iterator();
		while (site_it2.hasNext()) {  
		       String key = site_it2.next().toString();  
		       if(this.sites.containsKey(key)) { continue;}
		       Vector states = (Vector)existing.sites.get(key);
		       //HashSet statesWithoutDuplicates = new HashSet();
		      // statesWithoutDuplicates.addAll(states);
		     //  Vector ordered = new Vector(Arrays.asList(statesWithoutDuplicates.toArray()));
		     // this.sites.put(key, ordered);
		       this.sites.put(key, states);
		 }  
		
		
		Iterator sites = this.sites.keySet().iterator();
		while (sites.hasNext()) {  
			 String key = sites.next().toString();  
			 boolean allNumbers = true;
		     Vector states = (Vector)this.sites.get(key);
		    
		     for(int i = 0; i < states.size(); i++) { //are they all numbers?
		    	 try {
		    		 Integer.parseInt((String)states.get(i));	    	 
		    	 } catch(Exception ex) {
		    		 if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
		    		 allNumbers = false;
		    		 break;
		    	 }
		     }
		     
		     if(allNumbers && states.size() > 1) {
			     int startRange = Integer.parseInt((String)states.get(0));
			     int endRange = Integer.parseInt((String)states.get(states.size()-1));
			     
			     if((endRange-startRange)==states.size()-1) { //the range is complete
			    	 //this.sites_fromRanges.add(key);
			    	 this.sites_type.put(key, new SiteType(SiteType.RANGE));
			     } else {
			    	 this.sites_type.put(key, new SiteType(SiteType.LIST));
			     }
		     } else {
		    	 this.sites_type.put(key, new SiteType(SiteType.LIST));
		     }
		    	 
		     
		}

	
		return;
	}

	private void expandRangesSitesFrom(MultistateSpecies existing) throws Exception {
	//	 MultistateSpecies thisRecalculated = new MultistateSpecies(multiModel, this.printCompleteDefinition_withActualValues());
		
		  Iterator iterator = this.sites.keySet().iterator();  
		  while (iterator.hasNext()) {  
		      String key = iterator.next().toString();  
		      Vector<String> states = (Vector)this.sites.get(key);
		      Vector<String> existing_states = existing.getSiteStates_complete(key);
		      Vector<String> final_states = new Vector<String>();
		      for(int i = 0; i < states.size(); i++) {
		    	  String currentState = (String) states.get(i);
		    	 // try{
	    			 InputStream is = new ByteArrayInputStream(currentState.getBytes());
	    			 MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is);
	    			 CompleteMultistateSpecies_RangeString range = react.CompleteMultistateSpecies_RangeString();
	    			 MultistateSpeciesVisitor v = new MultistateSpeciesVisitor(multiModel);
	    			 range.accept(v);
	    			 if(v.getExceptions().size() >0) {
	    				 	if(existing.sites_rangesWithVariables.containsKey(key)) {
		    					GlobalQ limit = multiModel.getGlobalQ(currentState);
		    					if(limit == null || limit.type != Constants.GlobalQType.FIXED.copasiType) {
		    						//throw new Exception("Lower bound variable "+ranges.left+ " is not defined or its type is not fixed.");
		    						final_states.add(currentState);
		    					} else {
			    					try{
			    						Long value = Math.round(Double.parseDouble(limit.getInitialValue()));
			    						final_states.add(new Integer(value.intValue()).toString());
			    					} catch(Exception ex2) {
			    						//ex2.printStackTrace();
			    						
			    						try {
			    							Integer ret = CellParsers.evaluateExpression(limit.getInitialValue());
			    							final_states.add(ret.toString());
			    						} catch (Throwable e) {
			    							if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)e.printStackTrace();
			    						}
			    					}
		    					}
	    				 	} else { 
	    				 			final_states.add(currentState);//it was not a range, so it should be ok
	    				 	}
	    				 	continue;
			    	 }
	    			
	    			 MutablePair<String, String> pair = v.getStringRangeLimits();
	    			 
	    			 int from = -1;
	    			 int to = -1;
					try {
						from = existing_states.indexOf(multiModel.getGlobalQ_integerValue(pair.left).toString());
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						to = existing_states.indexOf(multiModel.getGlobalQ_integerValue(pair.right).toString());
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    			 if(from==-1 && to == -1) {
	    				 throw new ParseException("Missing states: "+currentState);
	    			 }
	    			 if(from > to && !existing.sites_type.get(key).isCircular()) throw new ParseException("Wrong range for the non-circular site "+key);
	    			 else {
	    				 if(from <= to) {
	    					 final_states.addAll(existing_states.subList(from, to+1));
	    				 } else {
	    					 final_states.addAll(existing_states.subList(from, existing_states.size()));
	    					 final_states.addAll(existing_states.subList(0, to+1));
	    				 }
	    			 }
	    			 
	    			 
		    	 /* } catch(Exception ex){
		    		  	ex.printStackTrace();
		    		}*/
		    	  
		      }
		      
		      
		      //if(sites_type.get(key)==SiteType.RANGE) Collections.sort(states, new AlphanumComparator());
		      this.sites.put(key, final_states);
		 }  
		    
		
	}
	public boolean containsSpecificConfiguration(String s) throws Exception {
		if(!CellParsers.isMultistateSpeciesName(s)) {
			return true; // because is just a name without sites, so it is the general configuration
		}
		MultistateSpecies single = new MultistateSpecies(multiModel,s);
		String configuration = single.getExpandedSites().get(0);
		Vector<String> current = this.getExpandedSites();
		for(int i = 0; i < current.size(); i++) {
			String site = current.get(i);
			if(site.compareTo(configuration)==0) return true;
		}
		return false;
	}
	
	public String getValueOfSite(String siteName) {
	
		String completeName = this.getDisplayedName();
		int index_site = completeName.indexOf(siteName+"{");
		String sub = completeName.substring(index_site+siteName.length()+1);
		
		int index_end_species = sub.indexOf(")");
		int index_semicolon = sub.indexOf(";");
		String value = new String();
		if(index_semicolon != -1) {
			value = sub.substring(0,index_semicolon);
		} else {
			value = sub.substring(0, index_end_species);
		}
		return value;
	}

	
	public String printCompleteDefinition(boolean b) {
		String r = this.name; 
		if(getSitesNames().size() <= 0) return r;
		r += "(";
		 Iterator iterator = getSitesNames().iterator();  
		 int i = 0;
		 while (iterator.hasNext()) {  
		       String pr = printSite((String)iterator.next(),b);//, true);  
		       r+=pr+";";
	    }
		 r = r.substring(0,r.length()-1) + ")";
		return r;
	}

	public String printCompleteDefinition_withActualValues() {
		String r = this.name; 
		if(getSitesNames().size() <= 0) return r;
		r += "(";
		 Iterator iterator = getSitesNames().iterator();  
		 int i = 0;
		 while (iterator.hasNext()) {  
		       String pr = printSite((String)iterator.next(),false, true);
		       r+=pr+";";
	    }
		 r = r.substring(0,r.length()-1) + ")";
		return r;
	}
	
	public boolean containsRangeVariable(String elementToSearch) {
		Iterator<String> it =sites_rangesWithVariables.keySet().iterator();
		while(it.hasNext()) {
			String site = it.next();
			MutablePair<String, String> pair = sites_rangesWithVariables.get(site);
			if(pair.left.compareTo(elementToSearch)==0) return true;
			if(pair.right.compareTo(elementToSearch)==0) return true;
		}
		return false;
	}
	
	public void replaceRangeVariable(String elementToSearch, String replacement) {
		Iterator<String> it =sites_rangesWithVariables.keySet().iterator();
		while(it.hasNext()) {
			String site = it.next();
			MutablePair<String, String> pair = sites_rangesWithVariables.get(site);
			if(pair.left.compareTo(elementToSearch)==0) {
				pair.left = replacement;
			}
			if(pair.right.compareTo(elementToSearch)==0) {
				pair.right = replacement;
			}
		
			if(!sites.containsKey(site)) {
				Vector states = new Vector();
				states.add(pair.left+":"+pair.right);
				sites.put(site,states);
			}
			sites_rangesWithVariables.put(site, pair);
		}
	}
	
}


class CartesianIterator implements Iterator<Object[]> {

	private final Iterable[] iterables;
	private final Iterator[] iterators;
	private Object[] values;
	private int size;
	private boolean empty;

	/**
	 * Constructor
	 * @param iterables array of Iterables being the source for the Cartesian product.
	 */
	public CartesianIterator(Iterable ...iterables) {
		this.size = iterables.length;
		this.iterables = iterables;
		this.iterators = new Iterator[size];
		
		// Initialize iterators
		for (int i = 0; i < size; i++) {
			iterators[i] = iterables[i].iterator();
			// If one of the iterators is empty then the whole Cartesian product is empty
			if (!iterators[i].hasNext()) {
				empty = true;
				break;
			}
		}
		
		// Initialize the tuple of the iteration values except the last one
		if (!empty) {
			values = new Object[size];
			for (int i = 0; i < size-1; i++) setNextValue(i);
		}
	}

	@Override
	public boolean hasNext() {
		if (empty) return false;
		for (int i = 0; i < size; i++)
			if (iterators[i].hasNext())
				return true;
		return false;
	}

	@Override
	public Object[] next() {
		// Find first in reverse order iterator the has a next element
		int cursor;
		for (cursor = size-1; cursor >= 0; cursor--)
			if (iterators[cursor].hasNext()) break;

		// Initialize iterators next from the current one  
		for (int i = cursor+1; i < size; i++) iterators[i] = iterables[i].iterator();
		
		// Get the next value from the current iterator and all the next ones  
		for (int i = cursor; i < size; i++) setNextValue(i);

		return values.clone();
	}

	/**
	 * Gets the next value provided there is one from the iterator at the given index. 
	 * @param index
	 */
	private void setNextValue(int index) {
		Iterator it = iterators[index];
		if (it.hasNext())
			values[index] = it.next();
	}

	@Override
	public void remove() { throw new UnsupportedOperationException(); }
}


class SiteType {
	static int RANGE = 0;
	static int BOOLEAN = 1;
	static int LIST = 2;

	int type;
	boolean circular = false;
	
	SiteType(int t) {   this.type = t;   }
	int getType() {return type;}
	void setCircular(boolean b) {	circular = b; }
	public boolean isCircular() {	return circular;	}
	
	@Override
	public boolean equals(Object obj) {
		return (this.type == ((SiteType)obj).type) && (circular ==  ((SiteType)obj).circular);
	}
};
