package msmb.parsers.mathExpression.visitor;

import msmb.parsers.mathExpression.*;
import msmb.parsers.mathExpression.ParseException;
import msmb.parsers.mathExpression.syntaxtree.*;
import msmb.parsers.multistateSpecies.*;
import msmb.parsers.multistateSpecies.syntaxtree.CompleteMultistateSpecies_Operator;
import msmb.parsers.multistateSpecies.visitor.MultistateSpeciesVisitor;
import msmb.parsers.multistateSpecies.TokenMgrError;

import msmb.utility.CellParsers;
import msmb.utility.Constants;

import msmb.gui.MainGui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Vector;

import msmb.model.Function;
import msmb.model.MultiModel;
import msmb.model.MultistateSpecies;
import msmb.model.Species;

import org.COPASI.CCompartment;
import org.COPASI.CCopasiObjectName;
import org.COPASI.CMetab;
import org.COPASI.CModel;
import org.COPASI.CModelValue;
import org.COPASI.CReaction;
import org.apache.commons.lang3.tuple.MutablePair;


public class CopasiVisitor extends DepthFirstVoidVisitor {

	private PrintWriter out;
	  private CModel model = null;
	  private boolean conc = false;
	  private boolean isInitialExpression = false;
	  Vector<Exception> exceptions = new Vector<Exception>();
		 public Vector<Exception> getExceptions() { return exceptions; }
		
	  private String copasiExpression = new String();
	  public String getCopasiExpression() {return copasiExpression;}
	  MultiModel multiModel = null;
	private boolean expressionWithSumMultistate;
	private MultistateSpecies multistateForDependentSum = null;
	  
	public CopasiVisitor(CModel model, MultiModel mm, boolean conc, boolean isInitialExpression2)  { 
		  this.model = model;
		  this.conc = conc;
		  this.isInitialExpression = isInitialExpression2;
		  multiModel = mm;
	}
	
	public CopasiVisitor(CModel model, MultiModel mm, MultistateSpecies ms)  { 
		  this.model = model;
		  this.conc = true;
		  this.isInitialExpression = false;
		  multiModel = mm;
		  multistateForDependentSum  = ms;
	}

	@Override
	public void visit(NodeToken n) {
		if(n.tokenImage.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.XOR))==0||
				n.tokenImage.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.BANG	))==0) {
			copasiExpression+=" ";
		}
		copasiExpression+=n.tokenImage;
		if(n.tokenImage.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.XOR))==0||
				n.tokenImage.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.BANG))==0) {
			copasiExpression+=" ";
		}
		super.visit(n);
	}
	
	private boolean fromRunManager = false;
	private boolean referenceToParent = false;
	private String currentElementNameForRM = null;
	Vector<MutablePair<String, String>> parentsRefs = new Vector<MutablePair<String,String>>();
	
	
	
	@Override
	public void visit(PossibleExtensions n) {
		//nothing it will be taken care of by generateElement, unless is from RM
		
		if(fromRunManager) {
			String ext = ToStringVisitor.toString(n);
			if(ext.startsWith(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MUTANT_PARENT_SEPARATOR))) {
				String parent = ext.substring(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MUTANT_PARENT_SEPARATOR).length());
				parentsRefs.add(new MutablePair<String, String>(currentElementNameForRM, parent));
				referenceToParent = true;
			} else {
				referenceToParent = false;
			}
		} else {
			referenceToParent = false;
		}
	}
	
	@Override
	public void visit(MultistateSum n) {
		if(isSumMultistate(ToStringVisitor.toString(n))) {
			printSumMultistate(ToStringVisitor.toString(n));
			expressionWithSumMultistate = true;
		}
	}
	
	boolean nodeIsAFunctionCall = false;
	
	
	@Override
	public void visit(SpeciesReferenceOrFunctionCall_prefix n) {
		String name = ToStringVisitor.toString(n.name.nodeChoice.choice);
		if(fromRunManager){
			currentElementNameForRM = new String(name);
		}
		String fun = new String();
		if(n.nodeOptional.present())  {
			NodeOptional nodeOptional = (NodeOptional) ((NodeSequence) n.nodeOptional.node).nodes.get(1);
			if(nodeOptional.node==null){
				//System.out.println("FUNCTION CALL ("0");
				nodeIsAFunctionCall = true;
			}
			else {
				if(!isMultistateSitesList(nodeOptional.node)) {
					//System.out.println("FUNCTION CALL ("+getNumberArguments((ArgumentList)nodeOptional.node)+"): " +name);
					nodeIsAFunctionCall = true;
				} else {
					//System.out.println("SPECIES: "+ToStringVisitor.toString(n)); // to print complete "multistate" definition
					nodeIsAFunctionCall = false;
					
				
				}
			}
		} else {
			//System.out.println("SPECIES: "+ToStringVisitor.toString(n));
			nodeIsAFunctionCall = false;
		}
		
	}
	
	  
	@Override
	public void visit(SpeciesReferenceOrFunctionCall n) {
		try {
			super.visit(n);
			String element = ToStringVisitor.toString(n);
			if(nodeIsAFunctionCall) {
				
					String funName  = new String();

					try {
						InputStream is = new ByteArrayInputStream(element.getBytes("UTF-8"));
						MR_Expression_Parser parser = new MR_Expression_Parser(is);
						CompleteExpression root;
						root = parser.CompleteExpression();
						GetFunctionNameVisitor name = new GetFunctionNameVisitor();
						root.accept(name);
						funName  = name.getFunctionName();
						if(funName.length()==0) return;
						Function f = multiModel.getFunctionByName(funName);
						if(f==null) throw new Exception("FunName not defined");
						copasiExpression += funName+"(";
					} catch(Throwable ex) {
						if(funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.FLOOR))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.ABS))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.LOG10))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.CEIL))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.COS))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.ACOS))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.ASIN))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.ATAN))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.COSH))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.TAN))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.FACTORIAL))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.TANH))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.SIN))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.DELAY))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.SQRT))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.MAX))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.MIN))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXP))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.SEC))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.CSC))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.COT))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.SINH))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.ARCSEC))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.ARCCSC))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.ARCCOT))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.ARCSINH))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.ARCCOSH))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.ARCTANH))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.ARCSECH))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.ARCCSCH))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.ARCCOTH))==0 ||
								funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.LOG))==0) {
							copasiExpression += funName+"(";
						} 
						else {
							if(!isMultistateSpeciesDefined(ToStringVisitor.toString(n.speciesReferenceOrFunctionCall_prefix))) {
								throw ex;
							}

						}
					}


					InputStream is2 = new ByteArrayInputStream(element.getBytes("UTF-8"));
					MR_Expression_Parser parser2 = new MR_Expression_Parser(is2);
					CompleteExpression root2 = parser2.CompleteExpression();
					GetFunctionParametersVisitor v = new GetFunctionParametersVisitor();
					root2.accept(v);
					Vector<String> parametersActuals = v.getActualParameters();
					for(int i = 0; i < parametersActuals.size(); i++) {
						InputStream isR = new ByteArrayInputStream(parametersActuals.get(i).getBytes("UTF-8"));
						MR_Expression_Parser parserR = new MR_Expression_Parser(isR);
						CompleteExpression rootR = parserR.CompleteExpression();
						CopasiVisitor vis = new CopasiVisitor(model,multiModel,conc,isInitialExpression);
						rootR.accept(vis);
						if(vis.getExceptions().size() == 0) {
							String copasiExpr  = vis.getCopasiExpression();
							copasiExpression += copasiExpr+",";
						} else {
							this.exceptions.addAll(vis.exceptions);
						}
					}
					if(parametersActuals.size()>0) copasiExpression = copasiExpression.substring(0, copasiExpression.length()-1);
					copasiExpression += ")";
					
			} else {
				generateCopasiElement(element);
			}
			
			
		} catch (Throwable e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)
				e.printStackTrace();
		}
	}
	
	
	
	private void printSumMultistate(String element) {
		
		 try {
			  InputStream is = new ByteArrayInputStream(element.getBytes("UTF-8"));
			  MR_Expression_Parser_ReducedParserException parser = new MR_Expression_Parser_ReducedParserException(is);
			  CompleteExpression root = parser.CompleteExpression();
			  Look4UndefinedMisusedVisitor multistateSum = new Look4UndefinedMisusedVisitor(multiModel,multistateForDependentSum);
			  root.accept(multistateSum);
			  Vector<SumExpansion> sum = multistateSum.getSumExpansions();
			 for(int i = 0; i < sum.size(); i++) {
				  copasiExpression += "(";
				 SumExpansion el = sum.get(i);
				 Vector<String> weights = el.getWeightFun();
				 Vector<Species> species = el.getSpeciesSum();
				  for(int j =0; j < species.size()-1; j++){
					  if(weights!= null &&  weights.size()==species.size() && weights.get(j).length()>0) {
						  InputStream isR = new ByteArrayInputStream(weights.get(j).getBytes("UTF-8"));
						  MR_Expression_Parser parserR = new MR_Expression_Parser(isR);
						  CompleteExpression rootR = parserR.CompleteExpression();
						  CopasiVisitor vis = new CopasiVisitor(model,multiModel,conc,isInitialExpression);
						  rootR.accept(vis);
						  if(vis.getExceptions().size() == 0) {
								String copasiExpr  = vis.getCopasiExpression();
								copasiExpression += copasiExpr+"*";
							} else {
								this.exceptions.addAll(vis.exceptions);
							} 
					  }
					  generateCopasiElement(species.get(j).getDisplayedName());
					  copasiExpression += " + ";
				}
				  //and for the last element
				  if(weights!= null &&  weights.size()==species.size() && weights.get(species.size()-1).length()>0) {
					  InputStream isR = new ByteArrayInputStream(weights.get(species.size()-1).getBytes("UTF-8"));
					  MR_Expression_Parser parserR = new MR_Expression_Parser(isR);
					  CompleteExpression rootR = parserR.CompleteExpression();
					  CopasiVisitor vis = new CopasiVisitor(model,multiModel,conc,isInitialExpression);
					  rootR.accept(vis);
					  if(vis.getExceptions().size() == 0) {
							String copasiExpr  = vis.getCopasiExpression();
							copasiExpression += copasiExpr+"*";
						} else {
							this.exceptions.addAll(vis.exceptions);
						} 
				  }
				  generateCopasiElement(species.get(species.size()-1).getDisplayedName());
				  copasiExpression += ")";
			  }
			
			  
		  }catch (Exception e) {
			 return ;
		}
		return ;
	}

	private boolean isSumMultistate(String element) {
		 try {
			  InputStream is = new ByteArrayInputStream(element.getBytes("UTF-8"));
			  MR_Expression_Parser_ReducedParserException parser = new MR_Expression_Parser_ReducedParserException(is);
			  CompleteExpression root = parser.CompleteExpression();
			  Look4UndefinedMisusedVisitor multistateSum = new Look4UndefinedMisusedVisitor(multiModel);
			  root.accept(multistateSum);
			  //Vector<SumExpansion> sum = multistateSum.getSumExpansions();
			  return true;
		  }catch (Exception e) {
			 return false;
		}
	}

	private boolean isMultistateSpeciesDefined(String element) {
		 InputStream is = new ByteArrayInputStream(element.getBytes());
		 MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is);
		 try {
			CompleteMultistateSpecies_Operator start = react.CompleteMultistateSpecies_Operator();
			MultistateSpeciesVisitor v = new MultistateSpeciesVisitor(multiModel);
	
			start.accept(v);
			if(v.getExceptions().size() > 0) throw v.getExceptions().get(0);
			MultistateSpecies sp = (MultistateSpecies) multiModel.getSpecies(v.getSpeciesName());
		
			if(sp.containsSpecificConfiguration(element)) return true;
			else {
				exceptions.add(new ParseException("Model yet not complete. Element "+element+" not found"));
				return false;
			}
		
		 } catch (TokenMgrError e) {
				return false;
		} catch (Exception e) {
			//e.printStackTrace();
			return false;
		}
		
	}
	
	public void generateCopasiElement(String element) {
		String element_copasiTerm = new String();
	
		if(element.compareTo(MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstantsNOQUOTES.TIME]) ==0 
			|| element.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.TIME)) ==0) {
			if(!isInitialExpression)	element_copasiTerm = model.getObject(new CCopasiObjectName(Constants.COPASI_STRING_TIME)).getCN().getString();
			else element_copasiTerm = model.getObject(new CCopasiObjectName(Constants.COPASI_STRING_INITIAL_TIME)).getCN().getString();
		} 
		else if(element.compareTo(MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstantsNOQUOTES.PI]) ==0 
				|| element.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.PI)) ==0) {
				element_copasiTerm = Constants.COPASI_STRING_PI;
			} 
		else if(element.compareTo(MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstantsNOQUOTES.EXPONENTIALE]) ==0 
				|| element.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXPONENTIALE)) ==0) {
				element_copasiTerm = Constants.COPASI_STRING_EXPONENTIALE;
			} 
		else if(element.compareTo(MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstantsNOQUOTES.CONST_AVOGADRO]) ==0 
				|| element.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.CONST_AVOGADRO)) ==0) {
			element_copasiTerm = model.getObject(new CCopasiObjectName(Constants.COPASI_STRING_AVOGADRO)).getCN().getString();
			} 
		else  if(!isMultistateSpeciesDefined(element)) {
			
			int index = -1;
			GetElementWithExtensions name = null;
			try{
				
				InputStream is = new ByteArrayInputStream(element.getBytes("UTF-8"));
				MR_Expression_Parser parser = new MR_Expression_Parser(is);
				CompleteExpression root = parser.CompleteExpression();
				name = new GetElementWithExtensions();
				root.accept(name);
			}catch (Exception e) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
				return;
				
			}
			
			
				String element_substring = name.getElementName();
				String cmp = CellParsers.extractCompartmentLabel(element_substring);
				Vector<String> extensions = name.getExtensions();
				String element_kind_quantifier = getKindQuantifier(extensions);
				String element_timing_quantifier = getTimingQuantifier(extensions);
				String element_quantity_quantifier = getQuantityQuantifier(extensions);
				try {
					if(element_kind_quantifier == null || element_kind_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_SPECIES))==0) {
						if(cmp.length() > 0 ) {
							MultistateSpecies ms = new MultistateSpecies(null, element_substring);
							String justName = ms.getSpeciesName();
							index = multiModel.findMetabolite(justName, cmp);
						} else {
							index = multiModel.findMetabolite(element_substring,null);
						}
					}
				} catch (Exception e) {
					if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
					index = -1;
				}
			if(index!= -1) { //species
				CMetab metab = model.getMetabolite(index);
				if(!isInitialExpression) {
					if(element_quantity_quantifier == null && element_timing_quantifier == null) {
						if(!conc) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=ParticleNumber")).getCN().getString();
						else element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=Concentration")).getCN().getString();
					} else {
						if(element_timing_quantifier == null) {
							if (element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=ParticleNumber")).getCN().getString();
							else if(element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=Concentration")).getCN().getString();
						}else if(element_quantity_quantifier == null) {
							if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT))==0 && conc) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=InitialConcentration")).getCN().getString();
							else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS))==0 && conc) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=Concentration")).getCN().getString();
							else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_RATE))==0 && conc) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=Rate")).getCN().getString();
							else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT))==0 && !conc) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=InitialParticleNumber")).getCN().getString();
							else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS))==0 && !conc) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=ParticleNumber")).getCN().getString();
							else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_RATE))==0 && !conc) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=ParticleNumberRate")).getCN().getString();
						}
						else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT))==0 && element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=InitialConcentration")).getCN().getString();
						else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT))==0 && element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=InitialParticleNumber")).getCN().getString();
						else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS))==0 && element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=ParticleNumber")).getCN().getString();
						else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS))==0 && element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=Concentration")).getCN().getString();
						else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_RATE))==0 && element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=ParticleNumberRate")).getCN().getString();
						else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_RATE))==0 && element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=Rate")).getCN().getString();

					}
				} else {
					if(fromRunManager && referenceToParent) {
						System.out.println("element: "+element );
						int indexOfSeparator = element.indexOf(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MUTANT_PARENT_SEPARATOR));
						copasiExpression+=element.substring(0, indexOfSeparator) 
								+ MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_SPECIES) 
								+MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MUTANT_PARENT_SEPARATOR)
								+element.substring(indexOfSeparator+1);
						return;
					}
					
					if(element_quantity_quantifier == null && element_timing_quantifier == null) {
						if(!conc) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=InitialParticleNumber")).getCN().getString();
						else element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=InitialConcentration")).getCN().getString();
					} else {
						if(element_timing_quantifier == null) {
							if (element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=InitialParticleNumber")).getCN().getString();
							else if(element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=InitialConcentration")).getCN().getString();
						}else if(element_quantity_quantifier == null) {
							if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT))==0 && conc) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=InitialConcentration")).getCN().getString();
							else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT))==0 && !conc) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=InitialParticleNumber")).getCN().getString();
							else element_copasiTerm = null;
						}
						else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT))==0 && element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=InitialConcentration")).getCN().getString();
						else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT))==0 && element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=InitialParticleNumber")).getCN().getString();
						else element_copasiTerm = null;
					}
				}
			} else {
				if(element_kind_quantifier == null || element_kind_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_GLOBALQ))==0) {
						index = multiModel.findGlobalQ(element_substring,false);
				}
				if(index!= -1) { //parameter
					CModelValue m = model.getModelValue(index);
					if(!isInitialExpression) {
						if(element_timing_quantifier==null) element_copasiTerm = m.getObject(new CCopasiObjectName("Reference=Value")).getCN().getString();
						else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT))==0) element_copasiTerm = m.getObject(new CCopasiObjectName("Reference=InitialValue")).getCN().getString();
						else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS))==0) element_copasiTerm = m.getObject(new CCopasiObjectName("Reference=Value")).getCN().getString();
						else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_RATE))==0) element_copasiTerm = m.getObject(new CCopasiObjectName("Reference=Rate")).getCN().getString();
						else element_copasiTerm = null;
					} else {
						if(fromRunManager && referenceToParent) {
							System.out.println("element: "+element );
							int indexOfSeparator = element.indexOf(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MUTANT_PARENT_SEPARATOR));
							copasiExpression+=element.substring(0, indexOfSeparator) 
									+ MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_GLOBALQ) 
									+MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MUTANT_PARENT_SEPARATOR)
									+element.substring(indexOfSeparator+1);
							return;
						}
						if(element_timing_quantifier==null) element_copasiTerm = m.getObject(new CCopasiObjectName("Reference=InitialValue")).getCN().getString();
						else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT))==0) element_copasiTerm = m.getObject(new CCopasiObjectName("Reference=InitialValue")).getCN().getString();
						else element_copasiTerm = null;
					}
				} else { //compartment?
					if(element_kind_quantifier == null || element_kind_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_COMPARTMENT))==0) {
						index = multiModel.findCompartment(element_substring,false);
					}
					if(index != -1) {
						CCompartment comp = model.getCompartment(index);
						if(!isInitialExpression) {
							if(element_timing_quantifier==null) element_copasiTerm = comp.getObject(new CCopasiObjectName("Reference=Volume")).getCN().getString();
							else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT))==0) element_copasiTerm = comp.getObject(new CCopasiObjectName("Reference=InitialVolume")).getCN().getString();
							else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS))==0) element_copasiTerm = comp.getObject(new CCopasiObjectName("Reference=Volume")).getCN().getString();
							else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_RATE))==0) element_copasiTerm = comp.getObject(new CCopasiObjectName("Reference=Rate")).getCN().getString();
							else element_copasiTerm = null;
						} else {
							if(fromRunManager && referenceToParent) {
								System.out.println("element: "+element );
								int indexOfSeparator = element.indexOf(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MUTANT_PARENT_SEPARATOR));
								copasiExpression+=element.substring(0, indexOfSeparator) 
										+ MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_COMPARTMENT) 
										+MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MUTANT_PARENT_SEPARATOR)
										+element.substring(indexOfSeparator+1);
								return;
							}
							if(element_timing_quantifier==null) element_copasiTerm = comp.getObject(new CCopasiObjectName("Reference=InitialVolume")).getCN().getString();
							else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT))==0) element_copasiTerm = comp.getObject(new CCopasiObjectName("Reference=InitialVolume")).getCN().getString();
							else element_copasiTerm = null;
						}
					} else {//reaction?
						if(element_kind_quantifier == null || element_kind_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_REACTION))==0) {
							index = multiModel.findReaction(element_substring,false);
						}
						if(index != -1) {
							CReaction reac = model.getReaction(index);
							if(!isInitialExpression) {
								if(element_timing_quantifier==null) element_copasiTerm = reac.getObject(new CCopasiObjectName("Reference=Flux")).getCN().getString();
								else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT))==0) element_copasiTerm = reac.getObject(new CCopasiObjectName("Reference=InitialFlux")).getCN().getString();
								else element_copasiTerm = null;
							} else {
								if(element_timing_quantifier==null) element_copasiTerm = reac.getObject(new CCopasiObjectName("Reference=InitialFlux")).getCN().getString();
								else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT))==0) element_copasiTerm = reac.getObject(new CCopasiObjectName("Reference=InitialFlux")).getCN().getString();
								else element_copasiTerm = null;
							}
						}
					}
				}
			}
		} else {
			try {
				
				
				InputStream is = new ByteArrayInputStream(element.getBytes());
				MR_MultistateSpecies_Parser react = new MR_MultistateSpecies_Parser(is);
				CompleteMultistateSpecies_Operator start;
				start = react.CompleteMultistateSpecies_Operator();
				MultistateSpeciesVisitor v = new MultistateSpeciesVisitor(multiModel);

				start.accept(v);
				MultistateSpecies sp = (MultistateSpecies) multiModel.getSpecies(v.getSpeciesName());
			
				
				int index = multiModel.findMetabolite(element,null);
				
				//TOOOOO DOOOOOO
				
				String element_kind_quantifier = null; //getKindQuantifier(extensions);
				String element_timing_quantifier = null;//getTimingQuantifier(extensions);
				String element_quantity_quantifier = null; //getQuantityQuantifier(extensions);
			
				if(index!= -1) { //species
					CMetab metab = model.getMetabolite(index);
					if(element_quantity_quantifier == null && element_timing_quantifier == null) {
						if(!conc) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=ParticleNumber")).getCN().getString();
						else element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=Concentration")).getCN().getString();
					} else {
						if(element_timing_quantifier == null) {
							if (element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=ParticleNumber")).getCN().getString();
							else if(element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=Concentration")).getCN().getString();
						}else if(element_quantity_quantifier == null) {
							if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT))==0 && conc) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=InitialConcentration")).getCN().getString();
							else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS))==0 && conc) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=Concentration")).getCN().getString();
							else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_RATE))==0 && conc) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=Rate")).getCN().getString();
							else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT))==0 && !conc) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=InitialParticleNumber")).getCN().getString();
							else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS))==0 && !conc) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=ParticleNumber")).getCN().getString();
							else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_RATE))==0 && !conc) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=ParticleNumberRate")).getCN().getString();
						}
						else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT))==0 && element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=InitialConcentration")).getCN().getString();
						else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT))==0 && element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=InitialParticleNumber")).getCN().getString();
						else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS))==0 && element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=ParticleNumber")).getCN().getString();
						else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS))==0 && element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=Concentration")).getCN().getString();
						else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_RATE))==0 && element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=ParticleNumberRate")).getCN().getString();
						else if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_RATE))==0 && element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC))==0) element_copasiTerm = metab.getObject(new CCopasiObjectName("Reference=Rate")).getCN().getString();
						
					}
				} else {
					throw new Exception("problem exporting multistate species");
				}
				
				
			} catch (Exception e) {

				//e.printStackTrace();
				exceptions.add(e);
			}
		
			
		}
		
		
		if(element_copasiTerm == null) exceptions.add(new ParseException("Non compatible qualifiers"));
		if(element_copasiTerm.length() == 0) 
			exceptions.add(new ParseException("Model yet not complete. Element "+element+" not found"));
		if(element_copasiTerm.compareTo(Constants.COPASI_STRING_PI)==0
				||element_copasiTerm.compareTo(Constants.COPASI_STRING_EXPONENTIALE)==0) copasiExpression+=element_copasiTerm;
		else copasiExpression+="<"+element_copasiTerm+">";
	}

	private String getKindQuantifier(Vector<String> extensions) {
		for(int i = 0; i < extensions.size(); i++) {
			String ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_COMPARTMENT);
			if(extensions.get(i).compareTo(ext)==0) {	return ext;	} 
			ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_SPECIES);
			if(extensions.get(i).compareTo(ext)==0) {	return ext;	} 
			ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_GLOBALQ);
			if(extensions.get(i).compareTo(ext)==0) {	return ext;	} 
			ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_REACTION);
			if(extensions.get(i).compareTo(ext)==0) {	return ext;	} 
		}
		return null;
	}

	private String getQuantityQuantifier(Vector<String> extensions) {
		for(int i = 0; i < extensions.size(); i++) {
				String ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE);
				if(extensions.get(i).compareTo(ext)==0) {	return ext;	} 
				ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC);
				if(extensions.get(i).compareTo(ext)==0) {	return ext;	} 
			}
			return null;
	}

	private String getTimingQuantifier(Vector<String> extensions) {
		for(int i = 0; i < extensions.size(); i++) {
			String ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT);
			if(extensions.get(i).compareTo(ext)==0) {	return ext;	} 
			ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_RATE);
			if(extensions.get(i).compareTo(ext)==0) {	return ext;	} 
			ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS);
			if(extensions.get(i).compareTo(ext)==0) {	return ext;	} 
		}
		return null;
	}

	/*private int findCompartment(String name, boolean key) {
		if(name.startsWith("\"")&name.endsWith("\"")) { name = name.substring(1,name.length()-1); }
		int i, iMax =(int) model.getCompartments().size();
        for (i = 0;i < iMax;++i)
        {
            CCompartment comp = model.getCompartment(i);
            assert comp != null;
            String current = new String();
            if(!key) current = comp.getObjectName();
            else current = comp.getKey();
            if(name.compareTo(current) == 0) return i;
        }
        
        return -1;
	}
	
	private int findGlobalQ(String name, boolean key) {
		if(name.startsWith("\"")&name.endsWith("\"")) { name = name.substring(1,name.length()-1); }
			
		int i, iMax =(int) model.getModelValues().size();
        for (i = 0;i < iMax;++i)
        {
            CModelValue m = model.getModelValue(i);
            String current = new String();
            if(!key) current = m.getObjectName();
            else current = m.getKey();
            if(name.compareTo(current) == 0) return i;
        }
        
        return -1;
	}
	
	
	private int findMetabolite(String name, boolean key) throws Exception {
			
			if(CellParsers.isMultistateSpeciesName(name)) {
				MultistateSpecies ms = new MultistateSpecies(multiModel,name);
				name = ms.printCompleteDefinition(); 
				//because in "name" the order of the sites can be different from the order used for defining the metabolite species.
				//Building the multistateSpecies and printing again its complete definition will make the two definitions identical w.r.t. the order
			}
		
			if(name.startsWith("\"")) {	name = name.substring(1);	}
			if(name.endsWith("\"")) { name = name.substring(0,name.length()-1); }
		
			int i, iMax =(int) model.getMetabolites().size();
			
	        for (i = 0;i < iMax;++i)
	        {
	            CMetab metab = model.getMetabolite(i);
	            assert metab != null;
	            if(!key) {
		            String current = metab.getObjectName();
		            if(name.compareTo(current) == 0) return i;
	            } else {
	            	String current = metab.getKey();
	            	if(name.compareTo(current) == 0) return i;
	            }
	        }
	        
	        return -1;
		}*/
	
	 boolean isMultistateSitesList(INode n) {
		 if(n instanceof ArgumentList) {
			 if(((ArgumentList)n).nodeChoice.which ==0){
				 return true;
			 }  else return false;
		 }
		 else {
			 System.out.println("ERROR!" + n.getClass());
			 return false;
		 }
	 }

	public boolean containsSUM() {
		return expressionWithSumMultistate;
	}

	public void setFromRunManagerVisit(boolean b) {
		fromRunManager  = b;
	}

	public Vector<MutablePair<String, String>> getParentsReferences() {
		return parentsRefs;
	}

	
	
	/*public MultistateSpecies extract_object_of_SUM(String element) throws Exception {
		*String weightFunctionString = extract_weightFunction_in_SUM(element);
		if(weightFunctionString.length() > 0) {
			element = element.substring(0,element.length()- weightFunctionString.length()-2);
		} *
		
		StringTokenizer sum_st = new StringTokenizer(element,"(,)");
		sum_st.nextToken(); //SUM
		String multistate_species_name = sum_st.nextToken();
		//controllare che esista
		String site = new String();
		HashMap<String, Vector<Integer>> sitesSum = new HashMap<String, Vector<Integer>>();
		try {
			while(sum_st.hasMoreTokens()) {
				site = sum_st.nextToken();
				Vector<Integer> limits = new Vector<Integer>();
				try{
					String lower_bound = sum_st.nextToken();
					String upper_bound = sum_st.nextToken();
					limits.add(new Double(Double.parseDouble(lower_bound)).intValue());
					limits.add(new Double(Double.parseDouble(upper_bound)).intValue());
				} catch (NoSuchElementException ex){ //there are no lower-upper bounds --> all the site states
					if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
					MultistateSpecies ms = (MultistateSpecies) MultiModel.speciesDB.getSpecies(multistate_species_name);
					Vector states = ms.getSiteStates_complete(site);
					String lower_bound = (String) states.get(0);
					String upper_bound = (String)states.get(states.size()-1);
					limits.add(new Double(Double.parseDouble(lower_bound)).intValue());
					limits.add(new Double(Double.parseDouble(upper_bound)).intValue());
				}
				sitesSum.put(site, limits);
			}
		} catch(NumberFormatException numberEx) { 
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) numberEx.printStackTrace();
			throw new NumberFormatException("Only numerical states can be used as indexes in SUM");
		}
		
		//DEFINISCO NUOVA MULTISTATE CON SOLO I RANGE INDICATI SOPRA E STAMPO EXPANDED NELLA SOMMA
		MultistateSpecies ms = (MultistateSpecies) MultiModel.speciesDB.getSpecies(multistate_species_name);
		String complete_string = new String();
		Iterator all_sites = ms.getSitesNames().iterator();
		
		while(all_sites.hasNext()) {
			String name = (String) all_sites.next();
			complete_string += name+"{";
			if(sitesSum.containsKey(name)) {
				//CHEEEEEEEEEEEEEEEEECK IF LOWER E UPPER SONO COERENTI CON LA DEFINIZIONE DEL SITO
				//E CHE IL SITO SIA DEFINITO CON UN RANGE!!! ALTRIMENTI COMPLETE_STRING ORA E' SBAGLIATA
				int lower = sitesSum.get(name).get(0);
				int upper = sitesSum.get(name).get(1);
				for(int i = lower; i < upper; i++) {
					complete_string += i+",";
				}
				complete_string += upper+"}";
			} else {
				Iterator it = ms.getSiteStates_complete(name).iterator();
				while(it.hasNext()) {
					complete_string += it.next()+",";
				}
				complete_string = complete_string.substring(0,complete_string.length()-1);
			}
			complete_string += ";";
		}
		
		complete_string = complete_string.substring(0,complete_string.length()-1);
		
		complete_string = multistate_species_name+"("+ complete_string + ")";
		
		MultistateSpecies reduced = new MultistateSpecies(complete_string);
		return reduced;
	}*/
}
