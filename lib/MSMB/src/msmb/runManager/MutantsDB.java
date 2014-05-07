package msmb.runManager;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import msmb.gui.MainGui;
import msmb.model.MultiModel;
import msmb.parsers.mathExpression.MR_Expression_Parser;
import msmb.parsers.mathExpression.MR_Expression_ParserConstants;
import msmb.parsers.mathExpression.MR_Expression_ParserConstantsNOQUOTES;
import msmb.parsers.mathExpression.syntaxtree.CompleteExpression;
import msmb.parsers.mathExpression.visitor.CopasiVisitor;
import msmb.utility.CellParsers;
import msmb.utility.Constants;

import org.COPASI.CCompartment;
import org.COPASI.CCopasiObject;
import org.COPASI.CMetab;
import org.COPASI.CModel;
import org.COPASI.CModelParameter;
import org.COPASI.CModelParameterSet;
import org.COPASI.CModelValue;
import org.COPASI.ModelParameterSetVectorN;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jgraph.graph.AttributeMap;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;


public class MutantsDB
{
	MultiModel multiModel = null;
	ListenableDirectedGraph<Mutant, DefaultEdge> graphOfMutants = new ListenableDirectedGraph<Mutant, DefaultEdge>(DefaultEdge.class);;
    Mutant baseSet = null;
	private CModel model;
    
	public MutantsDB(MultiModel m){
		multiModel = m;
	}
	
    public void addMutant(Mutant m) {
    	 graphOfMutants.addVertex(m);
    }
    
    public DefaultEdge addConnection(Mutant child, Mutant parent){
    	return graphOfMutants.addEdge(child, parent);
    }

    public void setBaseSetMutant(Mutant m) {   	baseSet = m;    }
     public Mutant getBaseSetMutant() {	return baseSet;	}
     public ListenableDirectedGraph<Mutant, DefaultEdge> getJgraphT() {
    	 return graphOfMutants;
     }
     
    public boolean detectCycles() {
    	CycleDetector< Mutant, DefaultEdge> cd = new CycleDetector<Mutant, DefaultEdge>(graphOfMutants);
       return cd.detectCycles();
    }
    
    public Vector<Mutant> getParents(Mutant m) {
    	Vector<Mutant> anc = new Vector<Mutant>();
    	if(m == null || graphOfMutants == null)  return anc;
    	try {
	    	Set<DefaultEdge> con = graphOfMutants.edgesOf(m);
	    	for(DefaultEdge edge : con) {
	    		Mutant target = graphOfMutants.getEdgeTarget(edge);
	    		if(target!= m) { //to exclude incoming edges
	    			anc.add(target);
	    		}
	    	}
    	} catch(Exception ex) {
    		if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
    	}
    	return anc;
    }
    
  
    public Vector<DefaultEdge> getEdgesToParents(Mutant m){
    	Vector<DefaultEdge> ret = new Vector<DefaultEdge>();
    	if(m == null || graphOfMutants == null)  return ret;
    	try {
	    	Set<DefaultEdge> con = graphOfMutants.edgesOf(m);
	    	for(DefaultEdge edge : con) {
	    		Mutant target = graphOfMutants.getEdgeTarget(edge);
	    		if(target!= m) { //to exclude incoming edges
	    			ret.add(edge);
	    		}
	    	}
    	} catch(Exception ex) {
    		if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
    	}
    	return ret;
    }
    
   
    
    
    public Vector<Mutant> getChildren(Mutant m) {
    	Vector<Mutant> anc = new Vector<Mutant>();
    	Set<DefaultEdge> con = graphOfMutants.edgesOf(m);
    	for(DefaultEdge edge : con) {
    		Mutant target = graphOfMutants.getEdgeTarget(edge);
    		if(target== m) { //to only include incoming edges
    			anc.add(graphOfMutants.getEdgeSource(edge));
    		}
    	}
    	return anc;
    }
    
    public Vector<Mutant> collectAncestors(Mutant startVertex) {
    	Vector<Mutant> ret = new Vector<Mutant>();
    	
    	GraphIterator<Mutant, DefaultEdge> iterator = new BreadthFirstIterator<Mutant, DefaultEdge>(graphOfMutants,startVertex);
    	 	while (iterator.hasNext()) {
       	 		   Mutant element = iterator.next();
       	 		   if(element!= startVertex) ret.add(element);
       	 	}
       	 return ret;
    }
    
  
    public HashSet<String> detectConflict(Mutant startVertex) {
    	Vector<Mutant> parents = getParents(startVertex);
    	HashSet<String> conflicts = new HashSet<String>();
    	
    	if(parents.size() <= 1) return conflicts; 
        
    	Vector<Set<String>> keySets = new Vector<Set<String>>();
    	Set<String> keyInCurrent = startVertex.getChanges().keySet();
    	Vector<Set<String>> keySetsCumulative = new Vector<Set<String>>();
    	//conflict only if conflict between local redefinition of parents OR cumulative redefinition of parents
    	//NOT if intersection between local of one and cumulative of another one
		for (Mutant mutant : parents) {
			mutant.clearCumulativeChanges();
    		accumulateChanges(mutant);
    		Set<String> keyInParents_cumulative = mutant.getCumulativeChanges().keySet();
			
    		Set<String> keyInParents = mutant.getChanges().keySet();
    		keySets.add(Sets.difference(keyInParents, keyInCurrent));
    		keySetsCumulative.add(Sets.difference(Sets.difference(keyInParents_cumulative, keyInCurrent),keyInParents));
 		}
		
		for(int i = 0; i < parents.size()-1; i++) {
    		for(int j = i+1; j < parents.size(); j++) {
    			if(!Sets.intersection(keySets.get(i), keySets.get(j)).isEmpty()) {
    				  SetView<String> conf = Sets.intersection(keySets.get(i), keySets.get(j));		
   					  conflicts.addAll(conf);
   					}
    			if(!Sets.intersection(keySetsCumulative.get(i), keySetsCumulative.get(j)).isEmpty()) { 
    				   SetView<String> conf = Sets.intersection(keySetsCumulative.get(i), keySetsCumulative.get(j));		
   					  conflicts.addAll(conf);
   					}
    		}
    	}
    	
    	return conflicts;
    }
    
    public String printMutantChanges() {
    	String ret = new String();
        Iterator<Mutant> iter =  new DepthFirstIterator<Mutant, DefaultEdge>(graphOfMutants);
        Mutant vertex;
        while (iter.hasNext()) {
            vertex = iter.next();
            ret += vertex.printMutant() + "\n";
        }
        return ret;
    }
    
    
    @Override
    public String toString() {
    	String ret = new String();
        Iterator<Mutant> iter =  new DepthFirstIterator<Mutant, DefaultEdge>(graphOfMutants);
        Mutant vertex;
        while (iter.hasNext()) {
            vertex = iter.next();
            ret += "Vertex " + vertex.toString() + " is connected to: " + graphOfMutants.edgesOf(vertex).toString()+"\n";
        }
        return ret;
    }
    
 
   
   public Set<Mutant> getVertexSet() {
	   return graphOfMutants.vertexSet();
   }
   
   public ArrayList<MutablePair<String, String>> getEdgesSet() {
	   ArrayList<MutablePair<String, String>> ret = new ArrayList<MutablePair<String,String>>();
	   Set<DefaultEdge> edges = graphOfMutants.edgeSet();
	   for (DefaultEdge defaultEdge : edges) {
		   MutablePair<String, String> element = new MutablePair<String, String>();
		   element.left = graphOfMutants.getEdgeSource(defaultEdge).getName();
		   element.right = graphOfMutants.getEdgeTarget(defaultEdge).getName();
		   ret.add(element);
	   }
	   
	    return ret;
   } 
    
    public static void main(String [] args)
    {
    	
    }
    
    
   

	public void accumulateChanges(Mutant startVertex) {
    	Vector<Mutant> parents = getParents(startVertex);
     	if(parents.size() == 0) {
    		startVertex.addCumulativeChanges(startVertex.getChanges(), startVertex.getName());
    	} else {
    		HashSet<String> alreadyAdded = new HashSet<String>();
    		HashMap<String, MutablePair<String, String>> toBeAdded = new HashMap<String, MutablePair<String, String>>();
    		
    		for(int i = 0; i < parents.size(); ++i) {
    			Mutant p = parents.get(i);
    			accumulateChanges(p);
    			HashMap<String, MutablePair<String, String>> cc = p.getCumulativeChanges();
    			for (String element : cc.keySet()) {
    				MutablePair<String, String> where = cc.get(element);
    				if(where.right.compareTo(p.getName())==0) {
    					startVertex.addCumulativeChange(element, where);
    					alreadyAdded.add(element);
    					toBeAdded.remove(element);
    				} else {
    					toBeAdded.put(element, where);
    				}
				}
    		}
    		Iterator<String> added = alreadyAdded.iterator();
    		while(added.hasNext()) {
    			toBeAdded.remove(added.next());
    		}
    		
    		for (String toadd : toBeAdded.keySet()) {
    				startVertex.addCumulativeChange(toadd, toBeAdded.get(toadd));
    		}
    		
    		
 			startVertex.addCumulativeChanges(startVertex.getChanges(), startVertex.getName());
 		   
    	}
   }
   

    public boolean detectConflict_allNodes() {
     	Iterator<Mutant> iterator = graphOfMutants.vertexSet().iterator();
         while (iterator.hasNext()) {
      		Mutant mtnt = iterator.next();
      		if(detectConflict(mtnt).size() > 0) {
      			return true;
      		}
      	 }
        return false;
    }
    
    public void exportMutantGraph(String baseFileName){
    	if(detectConflict_allNodes()) {
			System.out.println("CANNOT EXPORT, CONFLICTS TO BE FIXED");
			return;
		} 
    	Iterator<Mutant> iterator = graphOfMutants.vertexSet().iterator();
    	try {
			String copasiKey =  multiModel.saveCPS(true, null, MainGui.tableReactionmodel,null);
		//	String baseModel =  multiModel.copasiDataModel.exportSBMLToString();
			
			while (iterator.hasNext()) {
	       		Mutant mtnt = iterator.next();
	       		exportMutant(mtnt, baseFileName);
	       		ModelParameterSetVectorN sets = model.getModelParameterSets(); 
	    		System.out.println("after export there are n sets: "+ sets.size());

	       		
	       		//to reset all the values as the initial ones... 
	       		//multiModel.copasiDataModel.importSBMLFromString(baseModel);
	       		
	       	 }
			multiModel.copasiDataModel.saveModel(baseFileName+".cps", true);
				
			
		} catch (Throwable e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
		}
    
    }
    

   
    
	public void exportMutant_OLD(Mutant mtnt,String baseFileName) {
		mtnt.clearCumulativeChanges();
		accumulateChanges(mtnt);
		
		model = multiModel.copasiDataModel.getModel();
		
		Iterator it = mtnt.getChanges().keySet().iterator();  
		while(it.hasNext()) {
			String key = (String) it.next();
			String type = Mutant.extractTypeFromKey(key);
			String element_name = Mutant.extractElementNameFromKey(key);
			if(type.equals(MutantChangeType.GLQ_INITIAL_VALUE.getDescription())) {
				CModelValue toChange = model.getModelValue(element_name);
				if(toChange==null) {
					System.err.println("PROBLEM: ELEMENT TO CHANGE not in the model");
				}
				String initialValueExpression = mtnt.getCumulativeChanges().get(key).left;
				try {
					toChange.setInitialValue(RM_buildCopasiExpression(initialValueExpression, mtnt));
				} catch (Exception e) {
						e.printStackTrace();
				}
			} else if(type.equals(MutantChangeType.SPC_INITIAL_VALUE.getDescription())) {
				 CMetab toChange = model.getMetabolite(element_name);
				if(toChange==null) {
					System.err.println("PROBLEM: ELEMENT TO CHANGE not in the model");
				}
				String initialValueExpression = mtnt.getCumulativeChanges().get(key).left;
				try {
					 toChange.setInitialValue(RM_buildCopasiExpression(initialValueExpression, mtnt));
				} catch (Exception e) {
						e.printStackTrace();
				}
			
				
			}else if(type.equals(MutantChangeType.COMP_INITIAL_VALUE.getDescription())) {
				 CCompartment toChange = model.getCompartment(element_name);
				if(toChange==null) {
					System.err.println("PROBLEM: ELEMENT TO CHANGE not in the model");
				}
				String initialValueExpression = mtnt.getCumulativeChanges().get(key).left;
				try {
					toChange.setInitialValue(RM_buildCopasiExpression(initialValueExpression, mtnt));
				
				} catch (Exception e) {
						e.printStackTrace();
				}
			}
			
			
		}
		

		if(baseFileName!=null) {
			multiModel.copasiDataModel.saveModel(baseFileName+"_"+mtnt.getName()+".cps", true);
			
		}
		
	}
	
	
	 private static void printModelParameter(CModelParameter param)
	    {                
	        System.out.print(String.format(" %s (%s)", param.getName(), param.getCN().getString()));
	        
	        if (param.hasValue())
	            System.out.print(String.format(" --> %f", param.getValue()));
	        else if (param.isInitialExpressionValid() && param.getInitialExpression().length() > 0 )
	            System.out.print(String.format(" --> %s", param.getInitialExpression()));
	        System.out.println();
	        int numChildren = (int)param.getNumChildren();
	        for (int i = 0; i < numChildren; i++)
	        {
	            CModelParameter current = param.getChild(i);
	            printModelParameter(current);
	        }        
	    }
	 
	 
	public void exportMutant(Mutant mtnt,String baseFileName) {
		//if baseFileName = null, the changes are loaded in the current model
		//used to be simulated
		mtnt.clearCumulativeChanges();
		accumulateChanges(mtnt);
		
		model = multiModel.copasiDataModel.getModel();
		ModelParameterSetVectorN sets = null;
		CModelParameterSet newSet = null;
		if(baseFileName!= null)	 {
			sets = model.getModelParameterSets(); 
			System.out.println("There are n sets: "+ sets.size());
			newSet = new CModelParameterSet(mtnt.getName(), model);
			newSet.createFromModel();
		}
		
		HashMap<String, MutablePair<String, String>> changes = mtnt.getCumulativeChanges();
		Iterator it = changes.keySet().iterator();  
		
		while(it.hasNext()) {
			String key = (String) it.next();
			String type = Mutant.extractTypeFromKey(key);
			String element_name = Mutant.extractElementNameFromKey(key);
			CCopasiObject toChange = null;
			String initialValueExpression = changes.get(key).left;
			if(type.equals(MutantChangeType.GLQ_INITIAL_VALUE.getDescription())) {
				 toChange = model.getModelValue(element_name);
				  if(baseFileName== null) ((CModelValue)toChange).setInitialValue(RM_buildCopasiExpression(initialValueExpression, mtnt));
			} else if(type.equals(MutantChangeType.SPC_INITIAL_VALUE.getDescription())) {
				  toChange = model.getMetabolite(element_name);
				  if(baseFileName== null) ((CMetab)toChange).setInitialValue(RM_buildCopasiExpression(initialValueExpression, mtnt));
			}else if(type.equals(MutantChangeType.COMP_INITIAL_VALUE.getDescription())) {
				  toChange = model.getCompartment(element_name);
				  if(baseFileName== null) ((CCompartment)toChange).setInitialValue(RM_buildCopasiExpression(initialValueExpression, mtnt));
			}
			if(sets!= null)	 {
				CModelParameter inSet = newSet.getModelParameter(toChange.getCN().getString());
				if(toChange==null || inSet == null) {
					System.err.println("PROBLEM: ELEMENT TO CHANGE not in the model");
				}
				inSet.setValue(RM_buildCopasiExpression(initialValueExpression, mtnt));
			} 
		}
		
		if(sets!= null)	 {
			sets.add(newSet);
		}
        
		
	}

	private Double RM_buildCopasiExpression(String initialValueExpression, Mutant exportingMutant) {
		try{
			InputStream is = new ByteArrayInputStream(initialValueExpression.getBytes("UTF-8"));
			MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
			CompleteExpression root = parser.CompleteExpression();
			CopasiVisitor vis = new CopasiVisitor(model,multiModel,MainGui.exportConcentration,true);
			vis.setFromRunManagerVisit(true);
			root.accept(vis);
				
		
			if(vis.getExceptions().size() == 0) {
				Set<DefaultEdge> edges = graphOfMutants.outgoingEdgesOf(exportingMutant);
				String copasiExpr  = vis.getCopasiExpression();
				Vector<MutablePair<String, String>> parentsRefs = vis.getParentsReferences();
				for (MutablePair<String, String> element_parent : parentsRefs) {
					for (DefaultEdge edge : edges) {
						if(graphOfMutants.getEdgeTarget(edge).getName().compareTo(element_parent.right)==0) {
							Mutant parent = graphOfMutants.getEdgeTarget(edge);
							HashMap<String, MutablePair<String,String>> cumulativeChanges = parent.getCumulativeChanges();
							Vector<Integer> where = multiModel.getWhereNameIsUsed(element_parent.left);
							if(where.size() > 1) {
								throw new Exception("Reference to parent's element in multiple tables not supported");
							}
							MutantChangeType mchangetype = null;
							String extToReplace = null;
							if(where.get(0).intValue() == Constants.TitlesTabs.GLOBALQ.index) {
									mchangetype = MutantChangeType.GLQ_INITIAL_VALUE;
									extToReplace = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_GLOBALQ) ;
							} else if(where.get(0).intValue() == Constants.TitlesTabs.SPECIES.index) {
								mchangetype = MutantChangeType.SPC_INITIAL_VALUE;
								extToReplace = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_SPECIES) ;
							}else if(where.get(0).intValue() == Constants.TitlesTabs.COMPARTMENTS.index) {
								mchangetype = MutantChangeType.COMP_INITIAL_VALUE;
								extToReplace = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_COMPARTMENT) ;
							}
								String toBeReplaced = 
										element_parent.left
										+ extToReplace
										+MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MUTANT_PARENT_SEPARATOR)
										+element_parent.right;
								updateMultiModelWithCumulativeChanges(cumulativeChanges,mchangetype);
								String toBeEvaluated = cumulativeChanges.get(Mutant.generateChangeKey(mchangetype, element_parent.left)).left;
								Double replacement = CellParsers.evaluateExpression(toBeEvaluated, multiModel);
								copasiExpr = copasiExpr.replaceAll(toBeReplaced, replacement.toString());
						
						}
					}
					
				}
				Double evaluated = CellParsers.evaluateExpression(copasiExpr, multiModel);
				return evaluated;
			} else {
				vis.getExceptions().get(0).printStackTrace();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	private void updateMultiModelWithCumulativeChanges(HashMap<String, MutablePair<String,String>> cumulativeChanges, MutantChangeType mchangetype) {
		try {
			 Set<String> keys = cumulativeChanges.keySet();
			 for (String k :keys) {
				if(mchangetype == MutantChangeType.GLQ_INITIAL_VALUE) {
					multiModel.changeGlobalQ(Mutant.extractElementNameFromKey(k), cumulativeChanges.get(k).left);
				} else if (mchangetype == MutantChangeType.SPC_INITIAL_VALUE) {
					multiModel.changeSpecies(Mutant.extractElementNameFromKey(k), cumulativeChanges.get(k).left);
				} else if (mchangetype == MutantChangeType.COMP_INITIAL_VALUE) {
					multiModel.changeCompartment(Mutant.extractElementNameFromKey(k), cumulativeChanges.get(k).left);
				}
			 }
		} catch (Throwable e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
		}
	
	}

	

	public boolean isNameDuplicate(String name) {
			return graphOfMutants.containsVertex(new Mutant(name));
	}

	public String cleanMutantName(String name) {
		name = name.trim();
		return CellParsers.cleanName(name,false);
	}

	public void initializeJGraph(HashMap<Object, AttributeMap> savedView) {
		 Set<Object> elements = savedView.keySet();
		 Vector<DefaultEdge> connections = new Vector<DefaultEdge>();
		for (Object e : elements) {
			if(e instanceof Mutant) {
				addMutant((Mutant) e);
			} else if(e instanceof DefaultEdge) {
				DefaultEdge edge = (DefaultEdge)e;
				connections.add(edge);
			}
		}
		
		for(DefaultEdge edge : connections) {
			StringTokenizer st = new StringTokenizer(edge.toString(), "( :)");
			addConnection(new Mutant(st.nextToken()), new Mutant(st.nextToken()));
		}
	}

	public void setMultiModel(MultiModel mm) {
		this.multiModel = mm;
	}


   
  }
