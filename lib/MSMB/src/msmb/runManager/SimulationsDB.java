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
import org.COPASI.ReportItemVector;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jgraph.graph.AttributeMap;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;


public class SimulationsDB
{
	public static final String DEFAULT_DURATION = "1";
	public static final String DEFAULT_STEPS = "100";
	
	MultiModel multiModel = null;
	ListenableDirectedGraph<Simulation, DefaultEdge> graphOfSimulations = new ListenableDirectedGraph<Simulation, DefaultEdge>(DefaultEdge.class);
	Simulation baseSet = null;
	private CModel model;
    
	public SimulationsDB(MultiModel m){
		multiModel = m;
	}
	
	public Simulation getSimulationOfMutant(Mutant m) {
	  	Iterator<Simulation> iter =  new DepthFirstIterator<Simulation, DefaultEdge>(graphOfSimulations);
        while (iter.hasNext()) {
        	  Simulation vertex = iter.next();
        	  if(vertex.hasMutantParameter(m)) return vertex;
        }
        return null;
	}
	
    public void addSimulation(Simulation m) {
        	graphOfSimulations.addVertex(m);
    }
    
    public DefaultEdge addConnection(Simulation child, Simulation parent){
    	return graphOfSimulations.addEdge(child, parent);
    }

    public void setBaseSetSimulation(Simulation m) {   	baseSet = m;    }
     public Simulation getBaseSetSimulation() {	return baseSet;	}
    
     public ListenableDirectedGraph<Simulation, DefaultEdge> getJgraphT() {
    	 return graphOfSimulations;
     }
     
    public boolean detectCycles() {
    	CycleDetector< Simulation, DefaultEdge> cd = new CycleDetector<Simulation, DefaultEdge>(graphOfSimulations);
       return cd.detectCycles();
    }
    
    public Vector<Simulation> getParents(Simulation m) {
    	Vector<Simulation> anc = new Vector<Simulation>();
    	if(m == null || graphOfSimulations == null)  return anc;
    	try {
	    	Set<DefaultEdge> con = graphOfSimulations.edgesOf(m);
	    	for(DefaultEdge edge : con) {
	    		Simulation target = graphOfSimulations.getEdgeTarget(edge);
	    		if(target!= m) { //to exclude incoming edges
	    			anc.add(target);
	    		}
	    	}
    	} catch(Exception ex) {
    		if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
    	}
    	return anc;
    }
    
  
    public Vector<DefaultEdge> getEdgesToParents(Simulation m){
    	Vector<DefaultEdge> ret = new Vector<DefaultEdge>();
    	if(m == null || graphOfSimulations == null)  return ret;
    	try {
	    	Set<DefaultEdge> con = graphOfSimulations.edgesOf(m);
	    	for(DefaultEdge edge : con) {
	    		Simulation target = graphOfSimulations.getEdgeTarget(edge);
	    		if(target!= m) { //to exclude incoming edges
	    			ret.add(edge);
	    		}
	    	}
    	} catch(Exception ex) {
    		if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
    	}
    	return ret;
    }
    
   
    
    
    public Vector<Simulation> getChildren(Simulation m) {
    	Vector<Simulation> anc = new Vector<Simulation>();
    	Set<DefaultEdge> con = graphOfSimulations.edgesOf(m);
    	for(DefaultEdge edge : con) {
    		Simulation target = graphOfSimulations.getEdgeTarget(edge);
    		if(target== m) { //to only include incoming edges
    			anc.add(graphOfSimulations.getEdgeSource(edge));
    		}
    	}
    	return anc;
    }
    
    public Vector<Simulation> collectAncestors(Simulation startVertex) {
    	Vector<Simulation> ret = new Vector<Simulation>();
    	graphOfSimulations.containsVertex(startVertex);
    	GraphIterator<Simulation, DefaultEdge> iterator = new BreadthFirstIterator<Simulation, DefaultEdge>(graphOfSimulations,startVertex);
    	 	while (iterator.hasNext()) {
    	 		Simulation element = iterator.next();
       	 		   if(element!= startVertex) ret.add(element);
       	 	}
       	 return ret;
    }
    
  
    public HashSet<String> detectConflict(Simulation startVertex) {
    	Vector<Simulation> parents = getParents(startVertex);
    	HashSet<String> conflicts = new HashSet<String>();
    	
    	if(parents.size() <= 1) return conflicts; 
        
    	Vector<Set<String>> keySets = new Vector<Set<String>>();
    	Set<String> keyInCurrent = startVertex.getChanges().keySet();
    	Vector<Set<String>> keySetsCumulative = new Vector<Set<String>>();
    	//conflict only if conflict between local redefinition of parents OR cumulative redefinition of parents
    	//NOT if intersection between local of one and cumulative of another one
		for (Simulation mutant : parents) {
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
    
    public String printSimulationsChanges() {
    	String ret = new String();
        Iterator<Simulation> iter =  new DepthFirstIterator<Simulation, DefaultEdge>(graphOfSimulations);
        Simulation vertex;
        while (iter.hasNext()) {
            vertex = iter.next();
            ret += vertex.printSimulation() + "\n";
        }
        return ret;
    }
    
    
    @Override
    public String toString() {
    	String ret = new String();
        Iterator<Simulation> iter =  new DepthFirstIterator<Simulation, DefaultEdge>(graphOfSimulations);
        Simulation vertex;
        while (iter.hasNext()) {
            vertex = iter.next();
            ret += "Vertex " + vertex.toString() + " is connected to: " + graphOfSimulations.edgesOf(vertex).toString()+"\n";
        }
        return ret;
    }
    
 
    
   
   public Set<Simulation> getVertexSet() {
	   return graphOfSimulations.vertexSet();
   }
   
   public ArrayList<MutablePair<String, String>> getEdgesSet() {
	   ArrayList<MutablePair<String, String>> ret = new ArrayList<MutablePair<String,String>>();
	   Set<DefaultEdge> edges = graphOfSimulations.edgeSet();
	   for (DefaultEdge defaultEdge : edges) {
		   MutablePair<String, String> element = new MutablePair<String, String>();
		   element.left = graphOfSimulations.getEdgeSource(defaultEdge).getName();
		   element.right = graphOfSimulations.getEdgeTarget(defaultEdge).getName();
		   ret.add(element);
	   }
	   
	    return ret;
   } 
    
   

	public void accumulateChanges(Simulation startVertex) {
    	Vector<Simulation> parents = getParents(startVertex);
    	 if(parents.size() == 0) {
    		startVertex.addCumulativeChanges(startVertex.getChanges(), startVertex.getName());
    	} else {
    		HashSet<String> alreadyAdded = new HashSet<String>();
    		HashMap<String, MutablePair<String, String>> toBeAdded = new HashMap<String, MutablePair<String, String>>();
    		
    		for(int i = 0; i < parents.size(); ++i) {
    			Simulation p = parents.get(i);
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
     	Iterator<Simulation> iterator = graphOfSimulations.vertexSet().iterator();
         while (iterator.hasNext()) {
        	 Simulation mtnt = iterator.next();
      		if(detectConflict(mtnt).size() > 0) {
      			return true;
      		}
      	 }
        return false;
    }
    
    public void exportMutantGraph(
    		String baseFileName, 
    		MutantsDB mutDB){
    	
    	if(detectConflict_allNodes()) {
			System.out.println("CANNOT EXPORT, CONFLICTS TO BE FIXED");
			return;
		} 
    	Iterator<Simulation> iterator = graphOfSimulations.vertexSet().iterator();
    	try {
			String copasiKey =  multiModel.saveCPS(false, null, MainGui.tableReactionmodel,null);
			int i = 1;
			while(iterator.hasNext()) {
				Simulation sim = iterator.next();
				Iterator<Object> iterator2 = sim.getMutantsParameters().iterator();
				while (iterator2.hasNext()) {
					Mutant mtnt_parameters = (Mutant) iterator2.next();
					mutDB.exportMutant(mtnt_parameters, null);
					System.out.println(i + ": simulating "+mtnt_parameters);
		       		exportSimulation(sim, baseFileName+"_"+mtnt_parameters.getName());
		       		System.out.println(i++ + ": done");
		       	 }
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
    
    }
    

   
	private String ELEMENTS_SEPARATOR = ",";
	
			
	public  void exportSimulation(Simulation sim,String baseFileName) {
		
		multiModel.copasiDataModel.getModel().compile();
		sim.clearCumulativeChanges();
		accumulateChanges(sim);
		
		model = multiModel.copasiDataModel.getModel();
		
		model.forceCompile();
		model.updateInitialValues();
		model.calculate();
		
		CFunctionDB funDB_copasi = CCopasiRootContainer.getFunctionList();
		CFunctionVectorN funcs = funDB_copasi.loadedFunctions();
		for (int i = 0; i < funcs.size();i++)
		{
			CEvaluationTree val = funDB_copasi.findFunction(funcs.get(i).getObjectName());
			CFunction cfun = (CFunction)val;
			cfun.compile();
		}
				
		//ADD CHANGES FROM LOCAL NODE OF SIMULATION
		String duration = DEFAULT_DURATION;
		String steps = DEFAULT_STEPS;
		String change = sim.getChanges().get(Simulation.generateChangeKey(SimulationChangeType.TOTAL_TIME, ""));
		if(change != null) duration = new String(change);
		
		 CReportDefinitionVector reports = multiModel.copasiDataModel.getReportDefinitionList();
		 reports.cleanup();
		 reports.clear();
		 CReportDefinition report = reports.createReportDefinition("Report", "Output for timecourse");
		 report.setTaskType(CCopasiTask.timeCourse);
		 report.setIsTable(false);
		 report.setSeparator(new CCopasiReportSeparator(ELEMENTS_SEPARATOR+" "));
		 ReportItemVector header = report.getHeaderAddr();
		 ReportItemVector body = report.getBodyAddr();
		 body.add(new CRegisteredObjectName(model.getObject(new CCopasiObjectName("Reference=Time")).getCN().getString()));
		 header.add(new CRegisteredObjectName(new CCopasiStaticString("time").getCN().getString()));
		
		 int i, iMax =(int) model.getMetabolites().size();
		 
		 if(iMax>0) {
			 header.add(new CRegisteredObjectName(report.getSeparator().getCN().getString()));
			 body.add(new CRegisteredObjectName(report.getSeparator().getCN().getString()));
			  for (i = 0;i < iMax;++i)
			 {
				 CMetab metab = model.getMetabolite(i);
				 assert metab != null;
				 body.add(new CRegisteredObjectName(metab.getObject(new CCopasiObjectName("Reference=Concentration")).getCN().getString()));
				 
				 String name = metab.getObjectName()+"_"+metab.getCompartment().getObjectName();
				 String separator = report.getSeparator().getObjectName().toString().trim();
				 if(name.contains(separator)) {
					 header.add(new CRegisteredObjectName(new CCopasiStaticString("\""+metab.getObjectName()+"_"+metab.getCompartment().getObjectName()+"\"").getCN().getString()));
				 } else {
					 String metabName = metab.getObjectName();
					 if(metabName.startsWith("\"") && metabName.endsWith("\"")) metabName = metabName.replace("\"", "");
					 metabName = metabName.replace("\"", "''");
					 
					 String metabComp = metab.getCompartment().getObjectName();
					 if(metabComp.startsWith("\"") && metabComp.endsWith("\"")) metabComp = metabComp.replace("\"", "");
					 metabComp = metabComp.replace("\"", "''");
					// header.add(new CRegisteredObjectName(new CCopasiStaticString(metabName+"_"+metabComp).getCN().getString()));
					 header.add(new CRegisteredObjectName(new CCopasiStaticString(metabName).getCN().getString()));
				 }
				 
				 if(i!=iMax-1)
				 {
					 body.add(new CRegisteredObjectName(report.getSeparator().getCN().getString()));
					 header.add(new CRegisteredObjectName(report.getSeparator().getCN().getString()));
				 }
			 }
		 }
		 
		
		 iMax =(int) model.getModelValues().size();
		 if(iMax>0) {
			 body.add(new CRegisteredObjectName(report.getSeparator().getCN().getString()));
			 header.add(new CRegisteredObjectName(report.getSeparator().getCN().getString()));
			 
			 for (i = 0;i < iMax;++i)
		 
		 {
			 CModelValue modelValue = model.getModelValue(i);
			 assert modelValue != null;
				 body.add(new CRegisteredObjectName(modelValue.getObject(new CCopasiObjectName("Reference=Value")).getCN().getString()));
				 header.add(new CRegisteredObjectName(new CCopasiStaticString(modelValue.getObjectName()).getCN().getString()));
				 if(i!=iMax-1)
				 {
					 body.add(new CRegisteredObjectName(report.getSeparator().getCN().getString()));
					 header.add(new CRegisteredObjectName(report.getSeparator().getCN().getString()));
				 }
		 }
		 }
		 
		 iMax =(int) model.getCompartments().size();
	
		 if(iMax>0) {
			 body.add(new CRegisteredObjectName(report.getSeparator().getCN().getString()));
			 header.add(new CRegisteredObjectName(report.getSeparator().getCN().getString()));
		 
		
		 for (i = 0;i < iMax;++i)
		 {
			 CCompartment modelValue = model.getCompartment(i);
			 assert modelValue != null;
				 body.add(new CRegisteredObjectName(modelValue.getObject(new CCopasiObjectName("Reference=Volume")).getCN().getString()));
				 header.add(new CRegisteredObjectName(new CCopasiStaticString(modelValue.getObjectName()).getCN().getString()));
				 if(i!=iMax-1)
				 {
					 body.add(new CRegisteredObjectName(report.getSeparator().getCN().getString()));
					 header.add(new CRegisteredObjectName(report.getSeparator().getCN().getString()));
				 }
		 }
		 }
	
		 CTrajectoryTask trajectoryTask = (CTrajectoryTask)multiModel.copasiDataModel.getTask("Time-Course");
		 if (trajectoryTask == null)
		 {
			 trajectoryTask = new CTrajectoryTask();
			 multiModel.copasiDataModel.getTaskList().addAndOwn(trajectoryTask);
		 }
		 
		 trajectoryTask.setMethodType(CCopasiMethod.deterministic);
		 trajectoryTask.getProblem().setModel(multiModel.copasiDataModel.getModel());
		 trajectoryTask.setScheduled(true);

		 trajectoryTask.getReport().setReportDefinition(report);
		 trajectoryTask.getReport().setTarget(baseFileName + sim.getName()+".txt");
		 trajectoryTask.getReport().setAppend(false);
		 
		 CTrajectoryProblem problem = (CTrajectoryProblem)trajectoryTask.getProblem();
		 problem.setStepNumber(Long.parseLong(steps));
		 multiModel.copasiDataModel.getModel().setInitialTime(0.0);
		 problem.setDuration(Double.parseDouble(duration));
		 problem.setTimeSeriesRequested(true);

		 CTrajectoryMethod method = (CTrajectoryMethod)trajectoryTask.getMethod();
		 CCopasiParameter parameter = method.getParameter("Absolute Tolerance");
		 parameter.setDblValue(1.0e-12);
		
		 boolean result=true;
		 try
		 {
			 result=trajectoryTask.process(true);
		 }
		 catch (Exception ex)
		 {
			 trajectoryTask.restore();
			 trajectoryTask.clearRefresh();
			 trajectoryTask.clearDirectDependencies();
			 trajectoryTask.cleanup();
				 
			 if (CCopasiMessage.size() > 0)
			 {
				 // print the messages in chronological order
				System.err.println(CCopasiMessage.getAllMessageText(true));
			 }
			 ex.printStackTrace();
		 }
		 if(result==false)
		 {
			 System.err.println( "An error occured while running the time course simulation." );
			 if (CCopasiMessage.size() > 0)
			 {
				 System.err.println(CCopasiMessage.getAllMessageText(true));
			 }
		}
		 
		 CTimeSeries timeSeries = trajectoryTask.getTimeSeries();
      
		 /*System.out.println( "The time series consists of " + (new Long(timeSeries.getRecordedSteps())).toString() + "." );
         System.out.println( "Each step contains " + (new Long(timeSeries.getNumVariables())).toString() + " variables." );
         System.out.println( "The initial state is: " );
         iMax = (int)timeSeries.getNumVariables();
         for (i = 0;i < iMax;++i)
         {
            if(		   timeSeries.getTitle(i).contains("SBF") 
            		|| timeSeries.getTitle(i).contains("MCM1")
            		|| timeSeries.getTitle(i).contains("CLB2")
            		) { 
            	System.out.println(timeSeries.getTitle(i) + ": " + (new Double(new Double(timeSeries.getData(0, i))/6.02214179E23).toString() ));
            } else if (	timeSeries.getTitle(i).contains("kamcm")
            		|| timeSeries.getTitle(i).contains("kimcm")
            		|| timeSeries.getTitle(i).contains("Jamcm")
            		|| timeSeries.getTitle(i).contains("Jimcm")
            		|| timeSeries.getTitle(i).contains("Vasbf")
            		|| timeSeries.getTitle(i).contains("Visbf")
            		|| timeSeries.getTitle(i).contains("Jasbf")
            		|| timeSeries.getTitle(i).contains("Jisbf")){
              	System.out.println(timeSeries.getTitle(i) + ": " + (new Double(new Double(timeSeries.getData(0, i))).toString() ));
                      	
            }
         }*/
         
        /* System.out.println( "The final state is: " );
         iMax = (int)timeSeries.getNumVariables();
         int lastIndex = (int)timeSeries.getRecordedSteps() - 1;
         for (i = 0;i < iMax;++i)
         {
        	 
             System.out.println(timeSeries.getTitle(i) + ": " + (new Double(timeSeries.getData(lastIndex, i))).toString() );
         }*/
	
	
	}
	
	
	public boolean isNameDuplicate(String name) {
			return graphOfSimulations.containsVertex(new Simulation(name));
	}

	public String cleanMutantName(String name) {
		name = name.trim();
		return CellParsers.cleanName(name,false);
	}

	public void initializeJGraph(HashMap<Object, AttributeMap> savedView) {
		 Set<Object> elements = savedView.keySet();
		 Vector<DefaultEdge> connections = new Vector<DefaultEdge>();
		for (Object e : elements) {
			if(e instanceof Simulation) {
				addSimulation((Simulation) e);
			} else if(e instanceof DefaultEdge) {
				DefaultEdge edge = (DefaultEdge)e;
				connections.add(edge);
			}
		}
		
		for(DefaultEdge edge : connections) {
			StringTokenizer st = new StringTokenizer(edge.toString(), "( :)");
			addConnection(new Simulation(st.nextToken()), new Simulation(st.nextToken()));
		}
	}

	public void setMultiModel(MultiModel mm) {
		this.multiModel = mm;
	}
	
	 protected Double RM_buildCopasiExpression(String expression, Simulation exportingSim, SimulationChangeType schangetype) {
		 if(expression == null || exportingSim == null || expression.length() == 0) return null;
		 try{
				InputStream is = new ByteArrayInputStream(expression.getBytes("UTF-8"));
				MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
				CompleteExpression root = parser.CompleteExpression();
			    EvaluateExpressionVisitor vis = new EvaluateExpressionVisitor(multiModel,false);
			    vis.setFromRunManager(true);
			    root.accept(vis);
					
			
				if(vis.getExceptions().size() == 0) {
					Set<DefaultEdge> edges = graphOfSimulations.outgoingEdgesOf(exportingSim);
					String parsedexpression  = vis.getExpression();
					Vector<String> parentsRefs = vis.getParentsReferences();
					for (String parentRef : parentsRefs) {
						for (DefaultEdge edge : edges) {
							if(graphOfSimulations.getEdgeTarget(edge).getName().compareTo(parentRef)==0) {
								Simulation parent = graphOfSimulations.getEdgeTarget(edge);
								HashMap<String, MutablePair<String,String>> cumulativeChanges = parent.getCumulativeChanges();
								
								String toBeReplaced = 
										MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MUTANT_PARENT_SEPARATOR)
										+parentRef;
								String toBeEvaluated = cumulativeChanges.get(Simulation.generateChangeKey(schangetype,"")).left;
								Double replacement = CellParsers.evaluateExpression(toBeEvaluated, multiModel);
								parsedexpression = parsedexpression.replaceAll(toBeReplaced, replacement.toString());
							}
						}
						
					}
					Double evaluated = CellParsers.evaluateExpression(parsedexpression, multiModel);
					return evaluated;
				} else {
					vis.getExceptions().get(0).printStackTrace();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return null;
		}

	


   
  }
