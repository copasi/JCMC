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
import msmb.parsers.mathExpression.visitor.EvaluateExpressionVisitor;
import msmb.testing.BiomodelTest;
import msmb.utility.CellParsers;
import msmb.utility.Constants;

import org.COPASI.CCompartment;
import org.COPASI.CCopasiMessage;
import org.COPASI.CCopasiMethod;
import org.COPASI.CCopasiObjectName;
import org.COPASI.CCopasiParameter;
import org.COPASI.CCopasiReportSeparator;
import org.COPASI.CCopasiRootContainer;
import org.COPASI.CCopasiStaticString;
import org.COPASI.CCopasiTask;
import org.COPASI.CEvaluationTree;
import org.COPASI.CFunction;
import org.COPASI.CFunctionDB;
import org.COPASI.CFunctionVectorN;
import org.COPASI.CMetab;
import org.COPASI.CModel;
import org.COPASI.CModelValue;
import org.COPASI.CRegisteredObjectName;
import org.COPASI.CReportDefinition;
import org.COPASI.CReportDefinitionVector;
import org.COPASI.CTimeSeries;
import org.COPASI.CTrajectoryMethod;
import org.COPASI.CTrajectoryProblem;
import org.COPASI.CTrajectoryTask;
import org.COPASI.ModelParameterSetVectorN;
import org.COPASI.ReportItemVector;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jgraph.graph.AttributeMap;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;


public class RMPlotDB
{
	
	/*public String getDefaultValue(PlotChangeType simchangetype) {
		String value = null;
		if(simchangetype.equals(PlotChangeType.TOTAL_TIME)){
			value = RMPlotDB.DEFAULT_DURATION;
		} else if(simchangetype.equals(PlotChangeType.INTERVAL_NUMBER)){
			value = RMPlotDB.DEFAULT_STEPS;
		} else  if(simchangetype.equals(PlotChangeType.INTERVAL_SIZE)){
			value = new Double(Double.parseDouble(RMPlotDB.DEFAULT_DURATION)/Double.parseDouble(RMPlotDB.DEFAULT_STEPS)).toString();
		}  else  if(simchangetype.equals(PlotChangeType.METHOD)){
			value = RMPlotDB.DEFAULT_METHOD;
		}
		return value;
	}*/
	
	
	ListenableDirectedGraph<RMPlot, DefaultEdge> graphOfPlots = new ListenableDirectedGraph<RMPlot, DefaultEdge>(DefaultEdge.class);
	RMPlot baseSet = null;
	private RunManager runManager;
    
	public RMPlotDB(){
		
	}
	
	public Vector<RMPlot> getPlotOfSimulation(String m) {
		Vector<RMPlot> ret = new Vector<RMPlot>();
	  	Iterator<RMPlot> iter =  new DepthFirstIterator<RMPlot, DefaultEdge>(graphOfPlots);
        while (iter.hasNext()) {
        	RMPlot vertex = iter.next();
        	  if(vertex.hasSimulation(m)) ret.add(vertex);
        }
        return ret;
	}
	
	public void  replaceSimulation(String oldS, String newS) {
	  	Iterator<RMPlot> iter =  new DepthFirstIterator<RMPlot, DefaultEdge>(graphOfPlots);
        while (iter.hasNext()) {
        	RMPlot vertex = iter.next();
        	  if(vertex.hasSimulation(oldS)) {
        		  vertex.removeSimulation(oldS);
        		  vertex.addSimulation(newS);
        	  }
        }
        return;
	}
	
    public void addPlot(RMPlot m) {
    	graphOfPlots.addVertex(m);
    }
    
    public DefaultEdge addConnection(RMPlot child, RMPlot parent){
    	return graphOfPlots.addEdge(child, parent);
    }

    public void setBaseSetPlot(RMPlot m) {   	baseSet = m;    }
     public RMPlot getBaseSetPlot() {	return baseSet;	}
    
     public ListenableDirectedGraph<RMPlot, DefaultEdge> getJgraphT() {
    	 return graphOfPlots;
     }
     
     
    
    public boolean detectCycles() {
    	CycleDetector< RMPlot, DefaultEdge> cd = new CycleDetector<RMPlot, DefaultEdge>(graphOfPlots);
       return cd.detectCycles();
    }
    
    public Vector<RMPlot> getParents(RMPlot m) {
    	Vector<RMPlot> anc = new Vector<RMPlot>();
    	if(m == null || graphOfPlots == null)  return anc;
    	try {
	    	Set<DefaultEdge> con = graphOfPlots.edgesOf(m);
	    	for(DefaultEdge edge : con) {
	    		RMPlot target = graphOfPlots.getEdgeTarget(edge);
	    		if(target!= m) { //to exclude incoming edges
	    			anc.add(target);
	    		}
	    	}
    	} catch(Exception ex) {
    		if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
    	}
    	return anc;
    }
    
  
    public Vector<DefaultEdge> getEdgesToParents(RMPlot m){
    	Vector<DefaultEdge> ret = new Vector<DefaultEdge>();
    	if(m == null || graphOfPlots == null)  return ret;
    	try {
	    	Set<DefaultEdge> con = graphOfPlots.edgesOf(m);
	    	for(DefaultEdge edge : con) {
	    		RMPlot target = graphOfPlots.getEdgeTarget(edge);
	    		if(target!= m) { //to exclude incoming edges
	    			ret.add(edge);
	    		}
	    	}
    	} catch(Exception ex) {
    		if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
    	}
    	return ret;
    }
    
   
    
    
    public Vector<RMPlot> getChildren(RMPlot m) {
    	Vector<RMPlot> anc = new Vector<RMPlot>();
    	Set<DefaultEdge> con = graphOfPlots.edgesOf(m);
    	for(DefaultEdge edge : con) {
    		RMPlot target = graphOfPlots.getEdgeTarget(edge);
    		if(target== m) { //to only include incoming edges
    			anc.add(graphOfPlots.getEdgeSource(edge));
    		}
    	}
    	return anc;
    }
    
    public Vector<RMPlot> collectAncestors(RMPlot startVertex) {
    	Vector<RMPlot> ret = new Vector<RMPlot>();
    	graphOfPlots.containsVertex(startVertex);
    	GraphIterator<RMPlot, DefaultEdge> iterator = new BreadthFirstIterator<RMPlot, DefaultEdge>(graphOfPlots,startVertex);
    	 	while (iterator.hasNext()) {
    	 		RMPlot element = iterator.next();
       	 		   if(element!= startVertex) ret.add(element);
       	 	}
       	 return ret;
    }
    
  
    public HashSet<String> detectConflict(RMPlot startVertex) {
    	Vector<RMPlot> parents = getParents(startVertex);
    	HashSet<String> conflicts = new HashSet<String>();
    	
    	if(parents.size() <= 1) return conflicts; 
        
    	Vector<Set<String>> keySets = new Vector<Set<String>>();
    	Set<String> keyInCurrent = startVertex.getChanges().keySet();
    	Vector<Set<String>> keySetsCumulative = new Vector<Set<String>>();
    	//conflict only if conflict between local redefinition of parents OR cumulative redefinition of parents
    	//NOT if intersection between local of one and cumulative of another one
		for (RMPlot mutant : parents) {
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
    
    public String printRMPlotsChanges() {
    	String ret = new String();
        Iterator<RMPlot> iter =  new DepthFirstIterator<RMPlot, DefaultEdge>(graphOfPlots);
        RMPlot vertex;
        while (iter.hasNext()) {
            vertex = iter.next();
            ret += vertex.printPlot() + "\n";
        }
        return ret;
    }
    
    
    @Override
    public String toString() {
    	String ret = new String();
        Iterator<RMPlot> iter =  new DepthFirstIterator<RMPlot, DefaultEdge>(graphOfPlots);
        RMPlot vertex;
        while (iter.hasNext()) {
            vertex = iter.next();
            ret += "Vertex " + vertex.toString() + " is connected to: " + graphOfPlots.edgesOf(vertex).toString()+"\n";
        }
        return ret;
    }
    
 
    
   
   public Set<RMPlot> getVertexSet() {
	   return graphOfPlots.vertexSet();
   }
   
   public ArrayList<MutablePair<String, String>> getEdgesSet() {
	   ArrayList<MutablePair<String, String>> ret = new ArrayList<MutablePair<String,String>>();
	   Set<DefaultEdge> edges = graphOfPlots.edgeSet();
	   for (DefaultEdge defaultEdge : edges) {
		   MutablePair<String, String> element = new MutablePair<String, String>();
		   element.left = graphOfPlots.getEdgeSource(defaultEdge).getName();
		   element.right = graphOfPlots.getEdgeTarget(defaultEdge).getName();
		   ret.add(element);
	   }
	   
	    return ret;
   } 
    
   

	public void accumulateChanges(RMPlot startVertex) {
    	Vector<RMPlot> parents = getParents(startVertex);
    	 if(parents.size() == 0) {
    		startVertex.addCumulativeChanges(startVertex.getChanges(), startVertex.getName());
    	} else {
    		HashSet<String> alreadyAdded = new HashSet<String>();
    		HashMap<String, MutablePair<String, String>> toBeAdded = new HashMap<String, MutablePair<String, String>>();
    		
    		for(int i = 0; i < parents.size(); ++i) {
    			RMPlot p = parents.get(i);
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
 		   
 			Iterator<String> it = startVertex.fromBaseSet.iterator();
 			while(it.hasNext()) {
 				String key = it.next();
 				startVertex.removeCumulativeChange(key);
 			}
    	}
   }
   

    public boolean detectConflict_allNodes() {
     	Iterator<RMPlot> iterator = graphOfPlots.vertexSet().iterator();
         while (iterator.hasNext()) {
        	 RMPlot mtnt = iterator.next();
      		if(detectConflict(mtnt).size() > 0) {
      			return true;
      		}
      	 }
        return false;
    }
    
    public void setRunManager(RunManager rm) { this.runManager = rm; }
    
    
    public void exportMutantGraph(){
    	
    	if(detectConflict_allNodes()) {
			System.out.println("CANNOT EXPORT, CONFLICTS TO BE FIXED");
			return;
		} 
    	Iterator<RMPlot> iterator = graphOfPlots.vertexSet().iterator();
    	iterator = graphOfPlots.vertexSet().iterator();
        try {
			while(iterator.hasNext()) {
				RMPlot sim = iterator.next();
				Iterator<Object> iterator2 = sim.getSimulations().iterator();
				while (iterator2.hasNext()) {
					Mutant mtnt_parameters = (Mutant) iterator2.next();
				}
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
    
    }
    

   public boolean isNameDuplicate(String name) {
			return graphOfPlots.containsVertex(new RMPlot(name));
	}

	public String cleanMutantName(String name) {
		name = name.trim();
		return CellParsers.cleanName(name,false);
	}

	public void initializeJGraph(HashMap<Object, AttributeMap> savedView) {
		 Set<Object> elements = savedView.keySet();
		 Vector<DefaultEdge> connections = new Vector<DefaultEdge>();
		for (Object e : elements) {
			if(e instanceof RMPlot) {
				addPlot((RMPlot) e);
			} else if(e instanceof DefaultEdge) {
				DefaultEdge edge = (DefaultEdge)e;
				connections.add(edge);
			}
		}
		
		for(DefaultEdge edge : connections) {
			StringTokenizer st = new StringTokenizer(edge.toString(), "( :)");
			addConnection(new RMPlot(st.nextToken()), new RMPlot(st.nextToken()));
		}
	}

	public boolean hasAllCustomRMPlot(HashSet<String> simsNames) {
			return false;
	}

	
	public void rename(RMPlot currentNode, String newName) {
		if(currentNode.getName().compareTo(newName)==0) return;
		Vector<DefaultEdge> edgesToParents = getEdgesToParents(currentNode);		
		Vector<RMPlot> children = getChildren(currentNode);
		graphOfPlots.removeVertex(currentNode);
		
		currentNode.setName(newName);
		graphOfPlots.addVertex(currentNode);
		for(int i = 0; i < children.size(); ++i) {
			addConnection(children.get(i), currentNode);
		}
		for(int i = 0; i < edgesToParents.size(); ++i) {
			RMPlot target = graphOfPlots.getEdgeTarget(edgesToParents.get(i));
			addConnection(currentNode, target);
		}
		return;
	}
  }
