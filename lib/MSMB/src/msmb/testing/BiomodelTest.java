package msmb.testing;
import msmb.gui.MainGui;
import uk.ac.ebi.biomodels.ws.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.COPASI.*;


public class BiomodelTest {
	static boolean alsoNonCurated = false;
	static boolean deleteFilesAfterProcessing = false;
	static boolean collectStatisticsMode = false;
	static 	String subdirectoryTests = "tests/BiomodelTests/";
	static String collectStatisticsFile = new String(subdirectoryTests+"BioModels_statistics.txt");
	
	static String nameFileLogReport = subdirectoryTests+"MSMB_simulationDiff_report.txt";
	static String progressLog = subdirectoryTests+"MSMB_progressLog.txt";
	static Vector<String> failedTests = new Vector<String>();
	static boolean simulateAll = true;
	static Integer indexToSimulate = new Integer(0);
	
	static boolean FULL_BRACKET_EXPRESSION = true;
	
	//IDs with delay --> NO SIMULATION ALLOWED 
	public static final Vector<String> id_delay;
	static {
		id_delay = new Vector<String>();
		id_delay.add("BIOMD0000000024");
		id_delay.add("BIOMD0000000025");
		id_delay.add("BIOMD0000000034");
		id_delay.add("BIOMD0000000154");
		id_delay.add("BIOMD0000000155");
		id_delay.add("BIOMD0000000196");
		id_delay.add("BIOMD0000000297"); // simulations not possible because: Simultaneous event assignments encountered. The simulation cannot continue.
	}				  
	
	
	public static final Vector<String> models_with_known_problems;
	static {
		models_with_known_problems = new Vector<String>();
		
		//non-splittable reversible reactions cannot be interpreted as irreversible (simulation results are different)
		models_with_known_problems.add("BIOMD0000000051");
		models_with_known_problems.add("BIOMD0000000075");
		models_with_known_problems.add("BIOMD0000000081");
		models_with_known_problems.add("BIOMD0000000126");
		models_with_known_problems.add("BIOMD0000000145");
		models_with_known_problems.add("BIOMD0000000148");
		models_with_known_problems.add("BIOMD0000000161");
		models_with_known_problems.add("BIOMD0000000165");
		models_with_known_problems.add("BIOMD0000000166");
		models_with_known_problems.add("BIOMD0000000182"); 
		models_with_known_problems.add("BIOMD0000000232");
		models_with_known_problems.add("BIOMD0000000237");
		models_with_known_problems.add("BIOMD0000000245");
		models_with_known_problems.add("BIOMD0000000246");
		models_with_known_problems.add("BIOMD0000000248");		
		models_with_known_problems.add("BIOMD0000000250");
		models_with_known_problems.add("BIOMD0000000256");
		models_with_known_problems.add("BIOMD0000000265");
		models_with_known_problems.add("BIOMD0000000268");
		models_with_known_problems.add("BIOMD0000000273");
		models_with_known_problems.add("BIOMD0000000327");
		models_with_known_problems.add("BIOMD0000000392"); 
		models_with_known_problems.add("BIOMD0000000393");
		models_with_known_problems.add("BIOMD0000000450"); 
		models_with_known_problems.add("BIOMD0000000480");
		models_with_known_problems.add("BIOMD0000000404");//not all the  reversible reactions that can be split because they use functions with parameter and the - is in the parameters, so is wrong to change them manually as irreversible
		  //if I make them all irreversible is ok at export, but I get an error in simulation (simultaneous events) that is probably caused by the "force to be irreversible" change
			models_with_known_problems.add("BIOMD0000000408");//not all the  reversible reactions that can be split because they use functions with parameter and the - is in the parameters, so is wrong to change them manually as irreversible
		  //if I make them all irreversible is ok at export, but I get an error in simulation (simultaneous events) that is probably caused by the "force to be irreversible" change
		models_with_known_problems.add("BIOMD0000000409");
		models_with_known_problems.add("BIOMD0000000426");
		models_with_known_problems.add("BIOMD0000000428");//not all the  reversible reactions that can be split because they use functions with parameter and the - is in the parameters, so is wrong to change them manually as irreversible
		  //if I make them all irreversible is ok at export, but I get an error in simulation (simultaneous events) that is probably caused by the "force to be irreversible" change
		models_with_known_problems.add("BIOMD0000000429");
		models_with_known_problems.add("BIOMD0000000445");
	
	}
		
		
	
	
	public static void main(String[] args) throws Throwable {
		try {
			String rscriptPath = new String("C:\\Program Files\\R\\R-2.15.2\\bin\\x64\\RScript");
			RunSimulation.setRScriptPath(rscriptPath);
			if(args.length > 0) { 
				for(int i = 0; i<args.length; i++) {
					String current = args[i];

					if(current.compareTo("-del")==0) {	deleteFilesAfterProcessing = true;	}
					else if(current.compareTo("-o")==0){ 
						String subDir = args[i+1];
						if(!subDir.endsWith("/")) subDir += "/";
						File dir=new File(subDir);
						if(!dir.exists()){
							boolean result = dir.mkdir();  
						    if(!result){    
						    	System.out.println("Unable to create the specified output directory.");
						    	System.exit(1);
						     }

						}
						subdirectoryTests = subDir;
						nameFileLogReport = subdirectoryTests+"MSMB_simulationDiff_report.txt";
						progressLog = subdirectoryTests+"MSMB_progressLog.txt";
						i++;
					} else if(current.compareTo("-all")==0) { simulateAll = true; 
					} else if(current.compareTo("-single")==0) { 
						simulateAll = false;
						indexToSimulate = new Integer(Integer.parseInt(args[i+1]));
						i++;
					} else if(current.compareTo("-rscript")==0){ 
						String rscript = args[i+1];
						File file=new File(rscript);
						if(!file.exists()){
							System.out.println("Unable to find the RScript path.");
						    System.exit(1);
					    } else {
					    	RunSimulation.setRScriptPath(rscript);
					    }
					}

				}
			} else {
				File dir=new File(subdirectoryTests);
				if(!dir.exists()){
					boolean result = dir.mkdir();  
				    if(!result){    
				    	System.out.println("...");
				    	System.out.println("Unable to create the output directory "+subdirectoryTests+".");
				    	System.out.println("Please provide an output directory path with the option -o.");
				    	System.out.println("...");
				    	System.exit(1);
				     }

				}
				
				
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("... start BiomodelTest ...");
		BiomodelTest test = new BiomodelTest();
		test.runTest();
		System.out.println("... end BiomodelTest test ...");
		System.out.println("... FAILED SIMULATION TESTS :"+ failedTests);
		
		
		System.out.println("... See files "+nameFileLogReport+ " and "+progressLog + " for progress reports ...");
		System.out.println("...");
		
		System.exit(0);
	}



	
	
	public void runTest() throws Throwable{
		try{
			//System.setOut(new PrintStream(new File(subdirectoryTests+"SystemOutput.txt")));
			//System.setErr(new PrintStream(new File(subdirectoryTests+"SystemErr.txt")));

			if(collectStatisticsMode) initializeCollectStatistics();
			
			MainGui m = new MainGui(false);
			m.setFullBracketExpression(FULL_BRACKET_EXPRESSION);
			BioModelsWSClient client = new BioModelsWSClient();
			System.out.println("... WS retrieval of all curated IDs ...");
			System.out.flush();
			
			String[] curated = client.getAllCuratedModelsId();
			System.out.println("... total curated IDs: "+curated.length+" ...");
			System.out.println("... done ...");
			System.out.flush();
			ArrayList<String> curatedIDs = new ArrayList<String>();
			
			String[] noncurated = null;
			
			if(alsoNonCurated) {
				noncurated = client.getAllNonCuratedModelsId();
				System.out.println("... total non-curated IDs: "+noncurated.length+" ...");
				System.out.println("... done ...");
				System.out.flush();
				
			}
			
			curatedIDs.addAll(Arrays.asList(curated));
			if(alsoNonCurated) {
				curatedIDs.addAll(Arrays.asList(noncurated));
			}
			Collections.sort(curatedIDs);

			ArrayList<Integer> indices = new ArrayList<Integer>();
			if(simulateAll) {
				//LOAD THE LAST START OF progressLog
				int idBeforeCrash = loadIdBeforeCrash_fromProgressLog();
				for(int i = idBeforeCrash; i <= curatedIDs.size(); i++) { indices.add(i); }
			} else {
				indices.add(indexToSimulate);
				if(indexToSimulate <=0 || indexToSimulate > curatedIDs.size()) {
					throw new FileNotFoundException("The model ID "+indexToSimulate+ " is not available as curated model in Biomodel!");
				}
			}					
			generateLogReport();

			FileOutputStream buffoutProgressLog= new FileOutputStream(progressLog,true);
			PrintWriter outProgressLog = new PrintWriter(new OutputStreamWriter(buffoutProgressLog,"UTF-8"));
			SimpleDateFormat formatDate = new SimpleDateFormat("HH:mm:ss:SS");

		/*indices.clear();
		//indices.add(new Integer(18)); //high% e-021
		//indices.add(new Integer(19));//high relative, but in a single point, all the previous/following are the same -> rounding issue
		//indices.add(new Integer(39)); //1% should be ok
		//indices.add(new Integer(85));//high% e-028
		//indices.add(new Integer(206));//1% should be ok
		//indices.add(new Integer(232));//high relative, but in a single point, all the previous/following points are the same -> rounding issue
		//indices.add(new Integer(399));//high relative, but in a single point, all the previous/following points are the same -> rounding issue
		//indices.add(new Integer(266)); //2 glucose in different compartments, one with ODE the other fixed, we don't allow that in MSMB but we rename the second species at import and everything works fine
		*/
			
		
			/*indices.clear();
			indices.add(new Integer(469));*/
							
			boolean simulate255 = false;
			
			for(int i = 0; i < indices.size(); i++) {
				Integer index = indices.get(i);
				if(index == 0) continue;
				String sbmlID = curatedIDs.get(index-1);
				if(models_with_known_problems.contains(sbmlID)){
					System.out.println(sbmlID+": Model with known problems. We are working to solve them :)");
					outProgressLog.println(sbmlID + ", parseErrors , "+ formatDate.format(new Date()));
					outProgressLog.flush();
					continue;
				}


				FileOutputStream buffout= new FileOutputStream(subdirectoryTests+sbmlID+".xml");
				PrintWriter out = new PrintWriter(new OutputStreamWriter(buffout,"UTF-8"));
				System.out.println("... WS retrieval of "+sbmlID+" ...");
				System.out.flush();
				out.println(client.getModelSBMLById(sbmlID));
				out.flush();
				out.close();


				outProgressLog.println(sbmlID + ", start , "+ formatDate.format(new Date()));
				outProgressLog.flush();
				System.out.println("... Analyzing "+sbmlID+"...");
				System.out.flush();
				
			
				if(simulate255==false && sbmlID.compareTo("BIOMD0000000255")==0) {
					System.out.println("Very big model, we will load it after all the other models");
					System.out.flush();
					indices.add(255);
					simulate255 = true;
					continue;		
				}
				
				if(collectStatisticsMode) {
					collectStatistics(sbmlID);
					continue;
				}
				
				
				MainGui.cleanUpModel();
				MainGui.clearCopasiFunctions();
				MainGui.fromMainGuiTest = true;
				
				File sbmlFile = new File(subdirectoryTests+sbmlID+".xml");
				File cpsFile = new File(subdirectoryTests+sbmlID+"_MSMB.cps");
				try{
					m.loadSBML(sbmlFile);
					m.updateStatusQuantityIsConcentration(true);
					m.updateStatusExportConcentration(true);//it does not matter because all the expression are expanded so the default choice is never used
					System.out.println("... Model loaded, now saveCPS ...");
					System.out.flush();
					m.saveCPS(cpsFile,false);
					System.out.println("... Model saved, now simulations ...");
					System.out.flush();
					outProgressLog.println(sbmlID + ", exported , "+ formatDate.format(new Date()));
					outProgressLog.flush();
					double maxTime = 200;
					if(sbmlID.compareTo("BIOMD0000000437")==0) maxTime = 100; //otherwise we get a simutaneous events error
				
					RunSimulation.RunSimulations_CPSfromMSMB_OriginalSBML(
							cpsFile.getAbsolutePath(),
							sbmlFile.getAbsolutePath(), 
							2000, maxTime,new File(nameFileLogReport).getAbsolutePath());


					outProgressLog.println(sbmlID + ", simulated , "+ formatDate.format(new Date()));
					outProgressLog.flush();


				} catch(Throwable t){
					//t.printStackTrace();
					if(t instanceof FileNotFoundException) {
						throw t;
					}
					else if(t instanceof Exception) {
						Exception ex = (Exception)t;
						if(!id_delay.contains(sbmlID))	{
							if(ex.getCause()!=null && ex.getCause().getMessage() != null) {
								if(ex.getCause().getMessage().compareTo("convert2nonReversible")==0) {
									outProgressLog.println(sbmlID + ", convert2nonReversible , "+ formatDate.format(new Date()));
									outProgressLog.flush();
								} else {
									outProgressLog.println(sbmlID + ", parseErrors , "+ formatDate.format(new Date()));
									outProgressLog.flush();
								}
							} else {
								ex.printStackTrace();
								outProgressLog.println(sbmlID + ", otherSimulationError , "+ formatDate.format(new Date()));
								outProgressLog.flush();
							}
							failedTests.add(sbmlID);
						}
						else {
							outProgressLog.println(sbmlID + ", delayAndSimilar , "+ formatDate.format(new Date()) );
							outProgressLog.flush();

						}
					}
					continue;
				}


				if(deleteFilesAfterProcessing) {
					boolean success = sbmlFile.delete();
					if (!success) System.err.println("Delete: deletion sbmlFile failed");
					success = cpsFile.delete();
					if (!success) System.err.println("Delete: deletion cpsFile failed");
				}
				System.gc();


			}
			outProgressLog.flush();
			outProgressLog.close();

		}	catch(Exception ex) {

			ex.printStackTrace();
		}
	}
	
	private void initializeCollectStatistics() {
		File file = new File(collectStatisticsFile);
		
		try {
			FileOutputStream buffout2 = new FileOutputStream(file.getAbsolutePath(),false);

			PrintWriter out2 = new PrintWriter(new OutputStreamWriter(buffout2,"UTF-8"));
			out2.print("ModelID");
			out2.print(", ");
			out2.print("Species");
			out2.print(", ");
			out2.print("Compartments");
			out2.print(", ");
			out2.print("Reactions");
			out2.print(", ");
			out2.print("GlobalQuantities");
			out2.print(", ");
			out2.print("Events");
			out2.println("");
			out2.flush();
			out2.close();
			out2.flush();
			out2.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void collectStatistics(String sbmlID) {
		try{
			File file = new File(collectStatisticsFile);

			FileOutputStream buffout2= new FileOutputStream(file.getAbsolutePath(),true);
			PrintWriter out2 = new PrintWriter(new OutputStreamWriter(buffout2,"UTF-8"));

			File sbmlFile = new File(subdirectoryTests+sbmlID+".xml");

			CCopasiRootContainer.removeDatamodelWithIndex(0);
			CCopasiDataModel dataModel = CCopasiRootContainer.addDatamodel();
			dataModel.importSBML(sbmlFile.getAbsolutePath());


			CModel model = dataModel.getModel();
			boolean converted = model.convert2NonReversible();
			if(!converted) {
				int i = 0;
				int index = -1;
				while(CCopasiMessage.size() > 0) {
					CCopasiMessage message = CCopasiMessage.getFirstMessage();
					if(message.getType()==CCopasiMessage.ERROR) {
						String text = message.getText();
						if(text.contains("could not be split into two irreversible")) {
							StringTokenizer st = new StringTokenizer(text, "'");
							st.nextToken();
							String reactionName = st.nextToken();

							index = findReaction(reactionName,dataModel);
							System.out.println("reversible that could not be split index = "+index);
							if(index != -1) {
								CReaction r = model.getReaction(index);
								r.setReversible(false);
								model.compile();
							}
							index = -1;

						}

					}
					i++;
				}

				converted = model.convert2NonReversible();
			}

			out2.print(sbmlID);
			out2.print(", ");
			out2.print(model.getMetabolites().size());
			out2.print(", ");
			out2.print(model.getCompartments().size());
			out2.print(", ");
			out2.print(model.getReactions().size());
			out2.print(", ");
			out2.print(model.getModelValues().size());
			out2.print(", ");
			out2.print(model.getEvents().size());
			out2.println("");
			out2.flush();
			out2.close();
			if(deleteFilesAfterProcessing) {
				boolean success = sbmlFile.delete();
				if (!success) throw new IllegalArgumentException("Delete: deletion sbmlFile failed");
				}
			System.gc();
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	private int findReaction(String name, CCopasiDataModel copasiDataModel) {
		if(name.startsWith("\"")&&name.endsWith("\"")) {	name = name.substring(1,name.length()-1); }
		CModel model = copasiDataModel.getModel();
		int i, iMax =(int) model.getReactions().size();
        for (i = 0;i < iMax;++i)
        {
            CReaction r = model.getReaction(i);
            assert r != null;
            String current = new String();
            current = r.getObjectName();
            if(current.startsWith("\"")&&current.endsWith("\"")) {	current = current.substring(1,current.length()-1); }
    		if(name.compareTo(current) == 0) return i;
        }
        
        return -1;
	}


	private int loadIdBeforeCrash_fromProgressLog() {
			try {
				File file = new File(progressLog);
				if(!file.exists()) return 1;
		        BufferedReader reader = new BufferedReader(new FileReader(file));
		        String line = new String();
		        String analyzingBioModel = new String();
		        int index = 0;
		        String tmpTextFile = new String();
		        String textFile = new String();
		        while((line = reader.readLine()) != null) {
		        		if(line.trim().length()==0) continue; 
		        		if(line.indexOf(",")==-1) continue;
		        		if(line.indexOf(".")!=-1 || line.indexOf("=")!=-1|| line.indexOf("reaching")!=-1 || line.indexOf("-")!=-1) continue;
		        		String tmp = line.substring(0,line.indexOf(","));
		        		if(tmp.compareTo(analyzingBioModel)!=0) {
		        			index++;
		        			if(index != new Integer(tmp.substring(12,15))) {
		        				index--;
		        				break;
		        			}
		        			analyzingBioModel = new String(tmp);
		        			tmpTextFile += line+"\n";
		        			textFile += tmpTextFile;
		        			tmpTextFile = new String();
		        		} else {
		        			tmpTextFile += line+"\n";
		        		}
				 }
		        if(tmpTextFile.length()==0) {
		        	if(textFile.lastIndexOf("\n")!= -1) {
		        		int lastUsefulNewLine = textFile.substring(0,textFile.lastIndexOf("\n")).lastIndexOf("\n");
		        		textFile = textFile.substring(0, lastUsefulNewLine);
		        	}
		        }
				 reader.close();
		         
		     	FileOutputStream buffout2= new FileOutputStream(file.getAbsolutePath(),false);
				PrintWriter out2 = new PrintWriter(new OutputStreamWriter(buffout2,"UTF-8"));
				out2.println(textFile);
				out2.flush();
				out2.close();
		         
		         return index;
				} catch (Exception e) {
					e.printStackTrace();
					return -1;
				}
			
		}
	
		
		
		private void generateLogReport() {
			//file collecting maxPercentageError and similar info
			try {
				File file = new File(nameFileLogReport);
				if(!file.exists()) {
					FileOutputStream buffout= new FileOutputStream(file.getAbsolutePath());
					PrintWriter out = new PrintWriter(new OutputStreamWriter(buffout,"UTF-8"));
					out.println("StartIF, IDNumber, MaxPercentageError, MaxAbsoluteError,  SimulatedFrom, Simulated to, NumberSteps, MaxPercentageErrorOnSpecies");
					out.flush();
					out.println();
					out.close();
				}
			}catch(Exception ex) {
				ex.printStackTrace();
			}
			

			
		}

	
}
