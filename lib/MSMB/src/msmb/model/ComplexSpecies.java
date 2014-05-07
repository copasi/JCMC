package msmb.model;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.UIManager;

import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.collect.HashBiMap;

import msmb.gui.MainGui;
import msmb.parsers.multistateSpecies.MR_MultistateSpecies_Parser;
import msmb.parsers.multistateSpecies.syntaxtree.CompleteMultistateSpecies_Operator;
import msmb.parsers.multistateSpecies.visitor.MultistateSpeciesVisitor;
import msmb.utility.CellParsers;
import msmb.utility.Constants;

public class ComplexSpecies extends MultistateSpecies implements Serializable{

	String prefixName = new String();
	String customFullName = new String();
	
	//List of regular species components
	Vector<String> components = new Vector<String>();

	//List of multistate species components (species name, tracking couple (site, states))
	HashMap<String,Vector<MutablePair<String,String>>> components_multi = new HashMap<String,Vector<MutablePair<String,String>>>();

	//Map with site name (in the complex) and site configuration
	HashMap<String,String> complex_sitesConfiguration = new HashMap<String, String>();
	
	//Reversible hashmap with complex site name and pair multistate component name, multistate component site
	HashBiMap<String, MutablePair<String, String>> complexSite_originSpecies_originSite = HashBiMap.create();
	
		
	//list of the names that are not tracking specific states of the multi components but they are tracking the generic "all"
	//This information need to be stored separately because the specific current range need to be stored to fully define the complex
	//however if the site is tracking "all" and the multistate component changes, then the definition of the complex need to change accordingly
	//For specific tracked ranges, instead, this automatic update should NOT occur, but a revalidation of the previous state need to be done
	HashSet<String> complexSite_emptyTracking_implicitAllStates = new HashSet<String>();
	
	
	//if true, getComplexation/Decomplexation reaction will return null, so that the caller knows that the user didn't want to add the reaction to the model
	private boolean returnOnlySpecies = true;
	
	//if the reaction are added to the model, they can linked to the structure of the complex so that any change will be propagated when happens
	//note that cells with linked reactions need to be disabled since the user cannot type them directly 
	private MutablePair<Integer, Integer> linkedReactionIndexes;
	//private boolean linkReaction = true;

	
	
	public Vector<Vector<String>> getTrackedTriplets(String originSpeciesName) {
		Vector<Vector<String>> ret = new Vector<Vector<String>>();
		Iterator<String> it = complex_sitesConfiguration.keySet().iterator();
		while(it.hasNext()) {
			String complexSite = it.next();
			Vector<String> triplet = new Vector<String>();
			triplet.add(complexSite);
			MutablePair<String, String> originReference = complexSite_originSpecies_originSite.get(complexSite);
			triplet.add(originReference.right);
			if(complexSite_emptyTracking_implicitAllStates.contains(complexSite)) triplet.add(new String());
			else triplet.add(complex_sitesConfiguration.get(complexSite));
			if(originSpeciesName.compareTo(originReference.left)==0) { ret.add(triplet);	}
		}
		return ret;
	}


	
	
	
	public ComplexSpecies(MultiModel m, String complete_string,
			boolean isReactantReactionWithPossibleRanges) throws Exception {
		super(m, complete_string, isReactantReactionWithPossibleRanges);
		setType(Constants.SpeciesType.COMPLEX.copasiType);
	}

	public ComplexSpecies(MultiModel m, String complete_string) throws Exception {
		super(m, complete_string);
		setType(Constants.SpeciesType.COMPLEX.copasiType);
	}
	
	/*public ComplexSpecies(String complete_string) throws Exception {
		super(null, complete_string);
		setType(Constants.SpeciesType.COMPLEX.copasiType);
		prefixName = new String(complete_string);
	}*/
	
	public ComplexSpecies(MultiModel m, ComplexSpecies c) throws Exception {
		super(m, c.getDisplayedName());
		setType(Constants.SpeciesType.COMPLEX.copasiType);
		components.addAll(c.components);
		components_multi.putAll(c.components_multi);
		complex_sitesConfiguration.putAll(c.complex_sitesConfiguration);
		complexSite_originSpecies_originSite.putAll(c.complexSite_originSpecies_originSite);
		customFullName = new String(c.getCustomFullName());
		prefixName = new String(c.getPrefixName());
		returnOnlySpecies = c.returnOnlySpecies;
		MutablePair<Integer, Integer> element = c.getLinkedReactionIndexes();
		if(element != null) linkedReactionIndexes = new MutablePair<Integer, Integer>(element.left, element.right);
		else linkedReactionIndexes = null;
		//linkReaction = c.linkReaction;
		complexSite_emptyTracking_implicitAllStates.addAll(c.complexSite_emptyTracking_implicitAllStates);
		setCompartment(MainGui.multiModel, c.getCompartment_listString());
		setInitialQuantity(c.getInitialQuantity_multi());
	}

	//for now we don't want to allow multiple multistate species with the same name in a complex
	//We will add it later, once we implement the "tagging" in the reactions grammar
	public boolean containsComponent(Species componentSpecies) {
		String componentName = componentSpecies.getSpeciesName();
		for(String s: components) {
			if(s.compareTo(componentName)==0) return true;
		}
		return false;
	}
	
	

	public void addAll_regularSpecies(List selectedValuesList) {
		components.addAll(selectedValuesList);
	}

	public Vector getSiteNamesUsed() {
		Vector<String> ret = new Vector<String>();
		//ret.addAll(siteNamesUsed);
		ret.addAll(complexSite_originSpecies_originSite.keySet());
		return ret;
	}

	public void clearComponents() {
		components.clear();
		complex_sitesConfiguration.clear();
		//siteNamesUsed.clear();
		components_multi.clear();
	}
	
	public String getDecomplexationReaction() {
		if(returnOnlySpecies) return null;
		
		String ret = new String();
		if(components_multi.size() ==0 && components.size()==0) return "";
		String fullComplexName = getFullComplexName() ;
		ret += fullComplexName;
		ret += " -> ";
		for(String el : components) {
			if(!(CellParsers.isMultistateSpeciesName(el))) ret += el + " + ";
		}
		
		Set<String> keys = components_multi.keySet();
		for(String k : keys) {
			MutablePair<String, String> alias = CellParsers.extractAlias(k);
			k = alias.right;
			ret += k +"(";

			Vector<MutablePair<String, String>> sitesList = null;
			if(alias.left == null) 	 sitesList = components_multi.get(k);
			else	{
				sitesList =  components_multi.get(alias.left+"="+alias.right);
			}
		
			for(MutablePair<String, String> pair : sitesList){
					ret += pair.left;
					MutablePair toFind = null;
					if(alias.left == null) toFind = new MutablePair<String, String>(k,pair.left);
					else	toFind = new MutablePair<String, String>(alias.left, pair.left);
					String siteComplex = complexSite_originSpecies_originSite.inverse().get(toFind);
					ret += "="+fullComplexName+"."+siteComplex;
					ret +=";";
			}
			ret = ret.substring(0, ret.length()-1);
			ret +=")" + " + ";
		}
		
		
		ret = ret.substring(0, ret.length()-3);
		
		
		return ret;

	}

	public String getComplexationReaction() {
		if(returnOnlySpecies) return null;
		
		String ret = new String();
		if(components_multi.size() ==0 && components.size()==0) return "";
		
		for(String el : components) {
			if(!(CellParsers.isMultistateSpeciesName(el))) ret += el + " + ";
		}
		
		Set<String> keys = components_multi.keySet();
		for(String k : keys) {
			ret += k +"(";
			Vector<MutablePair<String, String>> sitesList = components_multi.get(k);
			for(MutablePair<String, String> pair : sitesList){
					ret += pair.left;
					if(pair.right.length() > 0) {
						String siteConf = pair.right;
						if(!siteConf.startsWith("{")) {	siteConf = "{"+siteConf;	}
						if(!siteConf.endsWith("}")) {	siteConf += "}";	}
						ret += siteConf;
					}
					ret +=";";
			}
			ret = ret.substring(0, ret.length()-1);
			ret +=")" + " + ";
		}
		ret = ret.substring(0, ret.length()-3);
		
		ret += " -> "+getFullComplexName();
		
		if(complexSite_originSpecies_originSite.keySet().size() > 0) {
			ret += "(";
			Iterator<String> it = complexSite_originSpecies_originSite.keySet().iterator();
			while(it.hasNext()) {
				String key = it.next();
				MutablePair<String, String> pieces = complexSite_originSpecies_originSite.get(key);
				ret += key+"="+pieces.left+"."+pieces.right+";";
			}
			ret = ret.substring(0, ret.length()-1);
			ret += ")";
		}
		
		return ret;
	}
	
	private String getFullComplexName() {
		
		if(customFullName.length() >0) return CellParsers.cleanName(customFullName);
		
		String completeDef = prefixName;
		for(String el : components) {
			if(el.startsWith("\"")&&el.endsWith("\"")) el = el.substring(1, el.length()-1);
			if(!(CellParsers.isMultistateSpeciesName(el))) completeDef += "_"+el;
		}
		if(complex_sitesConfiguration.keySet().size() > 0) {
			Set<String> keys = components_multi.keySet();
			for(String k : keys) {
				if(k.startsWith("\"")&&k.endsWith("\"")) k = k.substring(1, k.length()-1);
				MutablePair<String, String> alias = CellParsers.extractAlias(k);
				k = alias.right;
				completeDef += "_"+k;
			}
		}
		return CellParsers.cleanName(completeDef);
	}

	public String getFullComplexDefinition() {
		String completeDef = getFullComplexName();
		
		if(complex_sitesConfiguration.keySet().size() > 0) {
			completeDef += "(";
			for(String site : complex_sitesConfiguration.keySet()) {
				String siteConf = complex_sitesConfiguration.get(site);
				if(!siteConf.startsWith("{")) {	siteConf = "{"+siteConf;	}
				if(!siteConf.endsWith("}")) {	siteConf += "}";	}
				completeDef += site + siteConf +";";
			}
			completeDef = completeDef.substring(0,completeDef.length()-1);
			completeDef += ")";
		}
		
		return completeDef;
	}
	

	
	//first entry: the strings to make the species definition (site1{subRangeSelected};site2{subRangeSelected})
	//second entry(to build the reactions): a vector where each element is a vector of 3 elements:
	//								- complexSite name
	//								- multistateSpecies name origin
	//								- multistateSpecies site origin
	//third entry (to build the reaction): a hashmap with sites and state selected (note: here we can have the site states empty because it will be used in the reactants)
	//Returns: the string of the multistate species that is tracking, string to add in the components tree
	public String addMultistateElementsToComplex(Vector  elementsToAdd) {
		Vector<MutablePair<String, String>> for_definition = (Vector<MutablePair<String, String>>) elementsToAdd.get(0);
		Vector<Vector<String>> for_reactions = (Vector<Vector<String>>)elementsToAdd.get(1);
		HashMap<String,String> currentMultistateConfiguration = (HashMap<String, String>)elementsToAdd.get(2);
		
		
		for(MutablePair<String,String> def : for_definition) {
			complex_sitesConfiguration.put(def.left, def.right);
			//siteNamesUsed.add(def.left);
		}
				
		String currentMultistateSpeciesName = new String();
		for(Vector<String> react : for_reactions) {
			MutablePair<String, String> element = new MutablePair<String, String>(react.get(1),react.get(2));
			String alias = "";
			if(complexSite_originSpecies_originSite.containsValue(element)) {
				alias = getNextAlias(element);
				element.left = alias;
			}
			complexSite_originSpecies_originSite.put(react.get(0), element);
			if(alias.length() > 0) currentMultistateSpeciesName = alias+"=";
			currentMultistateSpeciesName += react.get(1);
		}
		
		Iterator<String> it = currentMultistateConfiguration.keySet().iterator();
		String sitesFinal = new String();
		
		Vector<MutablePair<String, String>> entry = new Vector<MutablePair<String, String>>();
		while(it.hasNext()) {
			String siteName = it.next();
			String siteStates = currentMultistateConfiguration.get(siteName);
			entry.add(new MutablePair<String, String>(siteName, siteStates));
			sitesFinal+=siteName;
			MutablePair toFind = new MutablePair<String, String>(currentMultistateSpeciesName,siteName);
			String siteComplex = complexSite_originSpecies_originSite.inverse().get(toFind);
			if(siteStates.length()> 0) {
				sitesFinal+=siteStates;
				complexSite_emptyTracking_implicitAllStates.remove(siteComplex);
			} else {
				complexSite_emptyTracking_implicitAllStates.add(siteComplex);
			}
			sitesFinal+=";";
		}
		if(sitesFinal.length() > 0) sitesFinal = sitesFinal.substring(0, sitesFinal.length()-1);
		
		components_multi.put(currentMultistateSpeciesName, entry);
			
		String ret = currentMultistateSpeciesName + "("+sitesFinal+")";
		
		return ret;
	}

	public void removeComponentFromComplex(String removedComponent) {
		if(CellParsers.isMultistateSpeciesName(removedComponent)) {
			MultistateSpecies m;
			try {
				m = new MultistateSpecies(null, removedComponent);
				
				for(Object site : m.getSitesNames()) {
					MutablePair toFind = new MutablePair<String, String>(m.getSpeciesName(),site.toString());
					String siteComplex = complexSite_originSpecies_originSite.inverse().get(toFind);
					complexSite_originSpecies_originSite.remove(siteComplex);
					complex_sitesConfiguration.remove(siteComplex);
				}
				components.remove(m.getSpeciesName());
				components_multi.remove(m.getSpeciesName());
			} catch (Exception e) {
				//cases with implicit track all
				try{
					ByteArrayInputStream is = new ByteArrayInputStream(removedComponent.getBytes("UTF-8"));
					 MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is,"UTF-8");
					 CompleteMultistateSpecies_Operator start = react.CompleteMultistateSpecies_Operator();
					 MultistateSpeciesVisitor v = new MultistateSpeciesVisitor(null);
					 start.accept(v);
					for(Object site : v.getSitesNames()) {
						MutablePair toFind = new MutablePair<String, String>(v.getSpeciesName(),site.toString());
						String siteComplex = complexSite_originSpecies_originSite.inverse().get(toFind);
						complexSite_originSpecies_originSite.remove(siteComplex);
						complex_sitesConfiguration.remove(siteComplex);
					}
					components.remove(v.getSpeciesName());
					components_multi.remove(v.getSpeciesName());
				 
				} catch (Exception e2) {
					//e2.printStackTrace();
				}
			}
			
		} 
		
		components.remove(removedComponent);
	}

	


	@Override
	public String toString() {
		return getFullComplexDefinition();
	}


	public void returnOnlySpecies(boolean b) {
		returnOnlySpecies = b;
	}
	
	/*public void setLinkReactions(boolean b) {
		linkReaction = b;
	}
	public boolean getLinkReactions() {return linkReaction;}
	*/
	
	public boolean getLinkReactions() {
		if(linkedReactionIndexes.right != -1) return true;
		if(linkedReactionIndexes.left != -1) return true;
		
		return false;
	}
	
	
	public MutablePair<Integer, Integer> getLinkedReactionIndexes() {
		return linkedReactionIndexes;
	}
	
	public void setLinkedReactionIndexes(int compl_index, int decompl_index) {
	/*	if(this.linkReaction) 	linkedReactionIndexes = new MutablePair<Integer, Integer>(compl_index, decompl_index);
		else linkedReactionIndexes = new MutablePair<Integer, Integer>(-1, -1);*/
		linkedReactionIndexes = new MutablePair<Integer, Integer>(compl_index, decompl_index);
	}
	
	
	@Override
	public String getSpeciesName() {
		return CellParsers.extractMultistateName(getFullComplexDefinition());
	}
	
	@Override
	public String getDisplayedName() {
		return getFullComplexDefinition();
	}

	public String getCustomFullName() {
		return customFullName;
	}


	public void setCustomFullName(String customFullName) {
		this.customFullName = customFullName;
	}
	
	public Vector<String> getComponents() {
		return components;
	}
	
	public HashMap<String, Vector<MutablePair<String, String>>>  getComponents_multi() {
		return components_multi;
	}


	public void setComponents(Vector<String> components) {
		this.components = components;
	}


	public String getPrefixName() {
		return prefixName;
	}


	public void setPrefixName(String text) {
		prefixName = new String(text);
	}


	public boolean onlyNameDifferent(ComplexSpecies newComplex) {
		boolean answer = false;
		String nameThis = getSpeciesName();
		boolean nameDifferent = false;
		boolean everythingElseSame = false;
		if(nameThis.compareTo(newComplex.getSpeciesName()) == 0) nameDifferent = false;
		else nameDifferent = true;
		
		Vector<String> componentsNew = newComplex.getComponents();
		Vector<String> siteNamesUsedNew = newComplex.getSiteNamesUsed();
		HashMap<String,Vector<MutablePair<String,String>>> components_multiNew = newComplex.getComponents_multi();
			
		everythingElseSame = componentsNew.equals(components) 
												&& siteNamesUsedNew.equals(this.getSiteNamesUsed()) 
												&& components_multiNew.equals(components_multi);
		
			
		answer = nameDifferent && everythingElseSame;
		return answer;
	}


	public boolean replaceElement(String toSearch, String replace) {
		if(components.contains(toSearch)) {
			int index = components.indexOf(toSearch);
			components.set(index, replace);
			return true;
		}
		return false;
	}

	
	public boolean validComplex(MultiModel m) {
		Set<String> multiElements = components_multi.keySet();
		
		for(String multiElement : multiElements) {
			Vector<Vector<String>> tracked_triplet = getTrackedTriplets(multiElement);
			MultistateSpecies mspecies = (MultistateSpecies) m.getSpecies(multiElement);
			MultistateSpecies tempComplex = null;
			try {
				tempComplex = new MultistateSpecies(multiModel, this.getFullComplexDefinition());
			} catch (Exception e) {
				//e.printStackTrace();
			}
			for(Vector<String> element : tracked_triplet) {
				String complex_siteName = element.get(0);
				String origin_species_siteName = element.get(1);
				String origin_species_siteTracking = element.get(2);
				if(complexSite_emptyTracking_implicitAllStates.contains(complex_siteName)
						&& mspecies.getSitesNames().contains(origin_species_siteTracking)){
					continue;
				} else {
						Vector states = mspecies.getSiteStates_complete(origin_species_siteName);
						Vector<String> complexStates = tempComplex.getSiteStates_complete(complex_siteName);
						for(String old : complexStates) {
							if(!states.contains(old)) {
								return false;
							}
						}
					}
			}		
		
		}
		
		return true;
	}
	
	public boolean replaceMultistateElementInComplex(String oldMultiSpecies, MultistateSpecies sp, HashMap<String, String> renamed_sites) throws Exception {
		String multiName = CellParsers.extractMultistateName(oldMultiSpecies);
		if(components_multi.containsKey(multiName)) {
			
			Vector<Vector<String>> tracked_triplet = getTrackedTriplets(multiName);
			for(Vector<String> element : tracked_triplet) {
				String complex_siteName = element.get(0);
				String origin_species_siteName = element.get(1);
				String origin_species_siteTracking = element.get(2);
				
				if(renamed_sites.containsKey(origin_species_siteName)) {
					String renamedSite = renamed_sites.get(origin_species_siteName);
					MutablePair<String, String> newTracking = new MutablePair<String, String>(sp.getSpeciesName(), renamedSite);
					complexSite_originSpecies_originSite.put(complex_siteName, newTracking);
					Vector states = sp.getSiteStates_complete(renamedSite);
					if(complexSite_emptyTracking_implicitAllStates.contains(complex_siteName)){
						String new_allStates = sp.getSiteStates_string(renamedSite);
						complex_sitesConfiguration.put(complex_siteName,new_allStates);
					} else {
						Vector<String> oldStates = this.getSiteStates_complete(complex_siteName);
						for(String old : oldStates) {
							if(!states.contains(old)) {
								throw new Exception("Tracking of site "+renamedSite+ " not compatible with Multistate component "+sp.printCompleteDefinition());
							}
						}
					}
					
				} else {
					if(sp.getSpeciesName().compareTo(multiName)!= 0) {
						MutablePair<String, String> newTracking = new MutablePair<String, String>(sp.getSpeciesName(), origin_species_siteName);
						complexSite_originSpecies_originSite.put(complex_siteName, newTracking);
					}
					if(complexSite_emptyTracking_implicitAllStates.contains(origin_species_siteName)){
						String new_allStates = sp.getSiteStates_string(origin_species_siteName);
						complex_sitesConfiguration.put(complex_siteName,new_allStates);
					} else {
						Vector states = sp.getSiteStates_complete(origin_species_siteName);
						Vector<String> oldStates = this.getSiteStates_complete(complex_siteName);
						for(String old : oldStates) {
							if(!states.contains(old)) {
								throw new Exception("Tracking of site "+origin_species_siteName+ " not compatible with Multistate component "+sp.printCompleteDefinition());
							}
						}
					}
				}
				
				Vector<MutablePair<String, String>> multi_element = components_multi.get(multiName);
				components_multi.remove(multiName);
				Vector<MutablePair<String, String>> trackedSites = new Vector<MutablePair<String,String>>();
				for(MutablePair<String, String> tracking : multi_element) {
					String siteName = tracking.left; 
					if(renamed_sites.containsKey(siteName)){
						siteName = renamed_sites.get(origin_species_siteName);
					}
					trackedSites.add(new MutablePair<String, String>(siteName, tracking.right));
				}
				components_multi.put(sp.getSpeciesName(), trackedSites);
			
				
			}
			
			//components.set(index, replace);adf
			return true;
		}
		return false;
	}


	@Override
	public int getType() {
		return Constants.SpeciesType.COMPLEX.copasiType;
	}


	public ComplexSpecies(Vector serializedInfo) throws Exception {
		this(null,"NO_NAME");
		try{
			prefixName = (String) serializedInfo.get(0);
			customFullName = (String) serializedInfo.get(1);
			
			components = new Vector<String>();
			components.addAll( (Vector<String>) serializedInfo.get(2));
			
			components_multi = new HashMap<String,Vector<MutablePair<String,String>>>();
			components_multi.putAll((HashMap<String,Vector<MutablePair<String,String>>>)serializedInfo.get(3));
			
			complex_sitesConfiguration = new HashMap<String, String>();
			complex_sitesConfiguration.putAll((HashMap<String,String>) serializedInfo.get(4));
			
			complexSite_originSpecies_originSite = HashBiMap.create();
			complexSite_originSpecies_originSite.putAll((HashBiMap<String, MutablePair<String, String>>) serializedInfo.get(5));
				
			complexSite_emptyTracking_implicitAllStates = new HashSet<String>();
			complexSite_emptyTracking_implicitAllStates.addAll((	HashSet<String>) serializedInfo.get(6));
			
			returnOnlySpecies = ((Boolean) serializedInfo.get(7)).booleanValue();
			
			linkedReactionIndexes = (MutablePair<Integer, Integer>) serializedInfo.get(8);
			
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	public Vector getSerializedInfo() {
		Vector ret = new Vector();
		ret.add(prefixName);
		ret.add(customFullName);
		ret.add(components);
		ret.add(components_multi);
		ret.add(complex_sitesConfiguration);
		ret.add(complexSite_originSpecies_originSite);
		ret.add(complexSite_emptyTracking_implicitAllStates);
		ret.add(new Boolean(returnOnlySpecies));
		ret.add(linkedReactionIndexes);
		return ret;
	}




	private HashMap<String, String> aliases = new HashMap<String, String>(); 
	
	public String getNextAlias(MutablePair<String, String> alreadyTrackedMulti) {
		String alias = "";
		
		if(getComponents_multi().containsKey(alreadyTrackedMulti.left)) {
			alias = new String("A");
			int  aliasCounter = aliases.size()+1;
			
			if(!aliases.containsValue(alreadyTrackedMulti.left)) {
				String previousElement = complexSite_originSpecies_originSite.inverse().get(alreadyTrackedMulti);
				String aliasPrevious = alias+aliasCounter++;
				complexSite_originSpecies_originSite.put(previousElement, new MutablePair<String, String>( aliasPrevious,alreadyTrackedMulti.right));
				Vector<MutablePair<String,String>> entryPrevious = components_multi.get(alreadyTrackedMulti.left);
				
				components_multi.put(aliasPrevious+"="+alreadyTrackedMulti.left, entryPrevious);
				components_multi.remove(alreadyTrackedMulti.left);
				
				aliases.put(aliasPrevious, alreadyTrackedMulti.left);
			}
			
			while(true) {
				alias += aliasCounter++;
				if(!aliases.containsKey(alias)) {
					aliases.put(alias, null);
					break;
				}
			}
			
		}
		return alias;
	}
	
}
