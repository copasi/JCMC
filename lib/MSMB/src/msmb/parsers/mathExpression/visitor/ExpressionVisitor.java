package msmb.parsers.mathExpression.visitor;

import msmb.parsers.mathExpression.MR_Expression_Parser;
import msmb.parsers.mathExpression.MR_Expression_ParserConstants;
import msmb.parsers.mathExpression.MR_Expression_ParserConstantsNOQUOTES;
import msmb.parsers.mathExpression.MR_Expression_Parser_ReducedParserException;
import msmb.parsers.mathExpression.ParseException;
import msmb.parsers.mathExpression.syntaxtree.*;
import msmb.parsers.multistateSpecies.MR_MultistateSpecies_Parser;
import msmb.parsers.multistateSpecies.MR_MultistateSpecies_ParserConstantsNOQUOTES;
import msmb.utility.CellParsers;
import msmb.utility.Constants;

import msmb.gui.MainGui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import msmb.model.Function;
import msmb.model.MultiModel;
import msmb.model.MultistateSpecies;
import msmb.model.Species;



public class ExpressionVisitor extends DepthFirstVoidVisitor {
	
	public static final int FULL_BRACKET = 1;
	private PrintWriter out;
	private boolean conc = false;
	private boolean isInitialExpression = false;
	Vector<Throwable> exceptions = new Vector<Throwable>();
	public Vector<Throwable> getExceptions() { return exceptions; }
		
	private String expression = new String();
	private String compressed_expression = new String();
	public String getExpression() {return expression;}
	public String getCompressedExpression() {return expression;}
	MultiModel multiModel = null;
	List listFunctionToCompact = null;
	boolean elementsExpanded = true; // if false, I will try to compress them as much as possible
	
	
	public void setElementsExpanded(boolean b) {elementsExpanded = b;}
	
	public ExpressionVisitor(List funcToCompact, MultiModel mm)  { 
		  multiModel = mm;
		  listFunctionToCompact = new ArrayList<String>();
		  listFunctionToCompact.addAll(funcToCompact);
	}
	
	public ExpressionVisitor(List funcToCompact, MultiModel mm, boolean forced_expanded_all)  { 
		this(funcToCompact,mm);
		elementsExpanded = forced_expanded_all;
	}

	@Override
	public void visit(NodeToken n) {
		if(n.tokenImage.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.XOR))==0||
				n.tokenImage.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.BANG	))==0) {
			expression+=" ";
		}
		expression+=n.tokenImage;
		if(n.tokenImage.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.XOR))==0||
				n.tokenImage.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.BANG))==0) expression+=" ";
		super.visit(n);
	}
	
	

	
	boolean nodeIsAFunctionCall = false;
	/*@Override
	public void visit(SpeciesReferenceOrFunctionCall_prefix n) {
		String name = ToStringVisitor.toString(n.name.nodeChoice.choice);
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
		
	}*/
	
	public void generateFunctionCall(String element) throws Throwable {
		try {
			InputStream is = new ByteArrayInputStream(element.getBytes("UTF-8"));
			MR_Expression_Parser parser = new MR_Expression_Parser(is);
			CompleteExpression root;
			root = parser.CompleteExpression();
			GetFunctionNameVisitor name = new GetFunctionNameVisitor();
			root.accept(name);
			String funName  = name.getFunctionName();
			if(funName.length()==0) return;
			Function f = multiModel.getFunctionByName(funName);
			InputStream is2 = new ByteArrayInputStream(element.getBytes("UTF-8"));
			MR_Expression_Parser parser2 = new MR_Expression_Parser(is2);
			CompleteExpression root2 = parser2.CompleteExpression();
			GetFunctionParametersVisitor v = new GetFunctionParametersVisitor();
			root2.accept(v);
			Vector<String> parametersActuals = v.getActualParameters();

			Vector mapping_vector = new Vector();
			if(!listFunctionToCompact.contains(funName)){
				String r = f.getExpandedEquation(parametersActuals);
				expression += "(";
				expression += r;
				expression += ")";
			}
		}catch (Exception e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
			throw e;
		}
	}
	  
	
	public void visit(MultistateSum n) {
		try {
			if(!elementsExpanded) {
				expression +=  ToStringVisitor.toString(n);
				return;
			}
		Vector<SumExpansion> expansion = getSumMultistateVector(ToStringVisitor.toString(n));

		for(int i = 0; i < expansion.size(); i++) {
			  expression += "(";
			 SumExpansion el = expansion.get(i);
			 Vector<String> weights = el.getWeightFun();
			 Vector<Species> species = el.getSpeciesSum();
			  for(int j =0; j < species.size()-1; j++){
				  if(weights!= null &&  weights.size()==species.size() && weights.get(j).length()>0) {
					  InputStream isR = new ByteArrayInputStream(weights.get(j).getBytes("UTF-8"));
					  MR_Expression_Parser parserR = new MR_Expression_Parser(isR);
					  CompleteExpression rootR = parserR.CompleteExpression();
					  ExpressionVisitor vis = new ExpressionVisitor(this.listFunctionToCompact, multiModel);
					 rootR.accept(vis);
					  if(vis.getExceptions().size() == 0) {
							String copasiExpr  = vis.getExpression();
							expression += copasiExpr+"*";
						} else {
							this.exceptions.addAll(vis.exceptions);
						} 
				  }
				  //generateCopasiElement(species.get(j).getDisplayedName());
				  expression += species.get(j).getDisplayedName();//expansion.get(i).toString();
				  expression += " + ";
			}
			  //and for the last element
			  if(weights!= null &&  weights.size()==species.size() && weights.get(species.size()-1).length()>0) {
				  InputStream isR = new ByteArrayInputStream(weights.get(species.size()-1).getBytes("UTF-8"));
				  MR_Expression_Parser parserR = new MR_Expression_Parser(isR);
				  CompleteExpression rootR = parserR.CompleteExpression();
				  ExpressionVisitor vis = new ExpressionVisitor(this.listFunctionToCompact, multiModel);
				  rootR.accept(vis);
				  if(vis.getExceptions().size() == 0) {
						String copasiExpr  = vis.getExpression();
						expression += copasiExpr+"*";
					} else {
						this.exceptions.addAll(vis.exceptions);
					} 
			  }
			  expression += species.get(species.size()-1).getDisplayedName();
			  expression += ")";
		  }
		  }catch (Exception e) {
				 e.printStackTrace() ;
			}
		
		return;
		/*expression += "(";
		for(int i = 0; i < expansion.size(); i++) {
			expression += expansion.get(i).toString();
		}
		expression += ")";*/
	}
	
	private Vector<SumExpansion> getSumMultistateVector(String element) {
		 try {
			  InputStream is = new ByteArrayInputStream(element.getBytes("UTF-8"));
			  MR_Expression_Parser_ReducedParserException parser = new MR_Expression_Parser_ReducedParserException(is);
			  CompleteExpression root = parser.CompleteExpression();
			  Look4UndefinedMisusedVisitor multistateSum = new Look4UndefinedMisusedVisitor(multiModel);
			  root.accept(multistateSum);
			  Vector<SumExpansion> sum = multistateSum.getSumExpansions();
			   return sum;
		  }catch (Exception e) {
			 return new Vector();
		}
	}
	
	
	@Override
	public void visit(SpeciesReferenceOrFunctionCall n) {
		try {
			String element = ToStringVisitor.toString(n);
			if(isNodeAFunctionCall(n)) {
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
					if(listFunctionToCompact.contains(funName)) expression += funName+"(";
					else{
						 generateFunctionCall(element);
						 return;
					}
				} catch(Throwable ex) {
					if(		funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.FLOOR))==0 ||
							funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.SQRT))==0 ||
							funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.MIN))==0 ||
							funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.MAX))==0 ||
							funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.CEIL))==0 ||
							funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.FACTORIAL))==0 ||
							funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.ABS))==0 ||
							funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.LOG10))==0 ||
							funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.COS))==0 ||
							funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.ACOS))==0 ||
							funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.ASIN))==0 ||
							funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.ATAN))==0 ||
							funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.COSH))==0 ||
							funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.TAN))==0 ||
							funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.TANH))==0 ||
							funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.SIN))==0 ||
							funName.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.DELAY))==0 ||
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
						expression += funName+"(";
					}
					else {
						throw ex;
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
					ExpressionVisitor vis = new ExpressionVisitor(listFunctionToCompact,multiModel,false);
					rootR.accept(vis);
					if(vis.getExceptions().size() == 0) {
						String copasiExpr  = vis.getExpression();
						expression += copasiExpr+",";
					} else {
						this.exceptions.addAll(vis.exceptions);
					}
				}
				if(parametersActuals.size()>0) expression = expression.substring(0, expression.length()-1);
				expression += ")";
			} else {
				expression += generateElement(element);
			}
			
		} catch (Throwable e) {
			exceptions.add(e);
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
		}
	}
	
	
	private boolean isNodeAFunctionCall(SpeciesReferenceOrFunctionCall node) {
		SpeciesReferenceOrFunctionCall_prefix n = node.speciesReferenceOrFunctionCall_prefix;
		if(n.nodeOptional.present())  {
			NodeOptional nodeOptional = (NodeOptional) ((NodeSequence) n.nodeOptional.node).nodes.get(1);
			if(nodeOptional.node==null){
				//System.out.println("FUNCTION CALL ("0");
				return  true;
			}
			else {
				if(!isMultistateSitesList(nodeOptional.node)) {
					//System.out.println("FUNCTION CALL ("+getNumberArguments((ArgumentList)nodeOptional.node)+"): " +name);
					return true;
				} else {
					//System.out.println("SPECIES: "+ToStringVisitor.toString(n)); // to print complete "multistate" definition
					return false;
				}
			}
		} else {
			//System.out.println("SPECIES: "+ToStringVisitor.toString(n));
			return false;
		}
	}

	private String getKindQuantifier(Vector<String> extensions) {
		for(int i = 0; i < extensions.size(); i++) {
			String ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_COMPARTMENT);
			if(extensions.get(i).compareTo(ext)==0) {	return ext;	} 
			ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_SPECIES);
			if(extensions.get(i).compareTo(ext)==0) {	return ext;	} 
			ext = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_GLOBALQ);
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
	
	
	public String generateElement(String element) {
		if(CellParsers.isKeyword(element)) return element;
		String asCompactedAsPossible = new String();
		try {
			asCompactedAsPossible = generateElement_extended(element);
			if(!elementsExpanded) {
				InputStream is = new ByteArrayInputStream(asCompactedAsPossible.getBytes("UTF-8"));
				MR_Expression_Parser parser = new MR_Expression_Parser(is);
				CompleteExpression root = parser.CompleteExpression();
				GetElementWithExtensions name = new GetElementWithExtensions();
				root.accept(name);

				String element_substring = name.getElementName();
				Vector<String> extensions = name.getExtensions();
				String element_kind_quantifier = getKindQuantifier(extensions);
				String element_timing_quantifier = getTimingQuantifier(extensions);
				String element_quantity_quantifier = getQuantityQuantifier(extensions);
						
				String comp = CellParsers.extractCompartmentLabel(element_substring);
				if(comp!= null && comp.length() > 0) {
					String speciesName = CellParsers.extractMultistateName(element_substring);
					if(multiModel.getSpecies(speciesName)!= null && multiModel.getSpecies(speciesName).getCompartments().size()==1) {
						element_substring = speciesName;
					}
				}
				
				String element_newView = element_substring;
				
				Vector<Integer> where = multiModel.getWhereNameIsUsed(element_substring);
				if(where == null) return element;
		
				if(where.size() == 1) {
					element_kind_quantifier = ""; // because in only one table
				}
				
					if(element_timing_quantifier != null) {
						if(element_timing_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS)) ==0 ) {
							element_timing_quantifier = "";
						}
					} else element_timing_quantifier = "";

					if(element_quantity_quantifier != null) {
						if(element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC))== 0) {
							if(MainGui.exportConcentration) {
								element_quantity_quantifier = "";
							}
						}
						if(element_quantity_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE))== 0) {
							if(!MainGui.exportConcentration) {
								element_quantity_quantifier = "";
							}
						}
					} else element_quantity_quantifier ="";

					element_newView += element_kind_quantifier + element_quantity_quantifier + element_timing_quantifier;


				

				asCompactedAsPossible = element_newView;
			}
			
		} catch(Exception ex) {
			exceptions.add(ex);
			//ex.printStackTrace();
			
		}
		return new String(asCompactedAsPossible);
				
	}
	
	public String generateElement_extended(String element) {
		//System.out.println("in generateElement:" + element);
		//String element = ToStringVisitor.toString(n);
		if(CellParsers.isKeyword(element)) return element;
		
		if(!elementsExpanded) {
			if(CellParsers.isMultistateSpeciesName(element)) { //if it is a multistate element just for the cmp site, it means that it can be a single simple species
				
				String sp_name = CellParsers.extractMultistateName(element);
				Species sp =multiModel.getSpecies(sp_name);
				int numberOfSites = 1;
				if(sp == null) return element;
 				if(sp instanceof MultistateSpecies) {
					numberOfSites =  ((MultistateSpecies)sp).getSitesNames().size();
				}
				if(sp.getCompartments().size() == 1 && numberOfSites == 1) {
					String comp = sp.getCompartments().get(0);
					String toReplace = sp_name 
							+ MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.OPEN_R)
						   + MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.EXTENSION_COMPARTMENT).substring(1)
						   + MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.OPEN_C)
						   +comp
						   + MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.CLOSED_C)
						   + MR_MultistateSpecies_ParserConstantsNOQUOTES.getTokenImage(MR_MultistateSpecies_ParserConstantsNOQUOTES.CLOSED_R);
					element = element.replace(toReplace, sp_name);
				}
				
			}
		}
		
		String element_newView = new String();
		if(element.compareTo(MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstantsNOQUOTES.TIME]) ==0 
				||element.compareTo(MR_Expression_ParserConstantsNOQUOTES.tokenImage[MR_Expression_ParserConstantsNOQUOTES.TIME]) ==0) {
			element_newView = element;
		} 
		else {
			GetElementWithExtensions name = null;
			try{
				InputStream is = new ByteArrayInputStream(element.getBytes("UTF-8"));
				MR_Expression_Parser parser = new MR_Expression_Parser(is);
				CompleteExpression root = parser.CompleteExpression();
				name = new GetElementWithExtensions();
				root.accept(name);
			}catch (Exception e) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
				//throw e;
				//return null;

			}
			String element_substring = name.getElementName();
			
			String comp = CellParsers.extractCompartmentLabel(element_substring);
			if(comp!= null && comp.length() > 0) {
				String speciesName = CellParsers.extractMultistateName(element_substring);
				if(multiModel.getSpecies(speciesName)!= null && multiModel.getSpecies(speciesName).getCompartments().size()==1) {
					element_substring = speciesName;
				}
			}
			
			
			Vector<String> extensions = name.getExtensions();
			String element_kind_quantifier = getKindQuantifier(extensions);
			String element_timing_quantifier = getTimingQuantifier(extensions);
			String element_quantity_quantifier = getQuantityQuantifier(extensions);
			element_newView = element_substring;

			Vector<Integer> where = multiModel.getWhereNameIsUsed(element_substring);
			if(where == null) return element;
			/*	System.out.println("in generateElement where:" + where);
			System.out.println("in generateElement element_kind_quantifier:" + element_kind_quantifier);
			System.out.println("in generateElement element_timing_quantifier:" + element_timing_quantifier);
			System.out.println("in generateElement element_quantity_quantifier:" + element_quantity_quantifier);
			System.out.println("in generateElement element_substring:" + element_substring);
			 */
			boolean isASpecies = false;
			if(element_kind_quantifier == null) {
				if(where.size() > 1) { // no quantifier, not unique name --> problem
					exceptions.add( new ParseException("Ambiguous name: "+element+". Expansion not available"));
				}
				int table = where.get(0);
				if(table == Constants.TitlesTabs.SPECIES.index) {
					if(elementsExpanded) element_newView += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_SPECIES);
					isASpecies= true;
				} else if(table == Constants.TitlesTabs.GLOBALQ.index) {
					if(elementsExpanded) element_newView += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_GLOBALQ);
				} else if(table == Constants.TitlesTabs.COMPARTMENTS.index) {
					if(elementsExpanded) element_newView += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_COMPARTMENT);
				}
			} else {
				if(element_kind_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_SPECIES))==0){
					isASpecies = true;
				}
				
				int table = where.get(0);

				if(where.size() == 1) {

					if(element_kind_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_SPECIES))==0 &&
							table != Constants.TitlesTabs.SPECIES.index) {
						exceptions.add(  new ParseException("Element "+element+" is supposed to have a .sp extension"));
					} 
					else if(element_kind_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_COMPARTMENT))==0 &&
							table != Constants.TitlesTabs.COMPARTMENTS.index) {
						exceptions.add(  new ParseException("Element "+element+" is supposed to have a .cmp extension"));
					}	
					else if(element_kind_quantifier.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_GLOBALQ))==0 &&
							table != Constants.TitlesTabs.GLOBALQ.index) {
						exceptions.add(  new ParseException("Element "+element+" is supposed to have a .glq extension"));
					} else {
						if(elementsExpanded) element_newView += element_kind_quantifier;
					}	
				}
				else {
					element_newView += element_kind_quantifier;
				}
			}


			if(elementsExpanded) {
				if(element_timing_quantifier == null) {
					element_newView += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS);
				} else {
					element_newView +=element_timing_quantifier;
				}
			}
			if(isASpecies) {
				if(element_quantity_quantifier == null ) {
					if(MainGui.exportConcentration) element_newView +=MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC);
					else element_newView +=MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE);
				}else {
					element_newView +=element_quantity_quantifier;
				}
			}
			if(element_newView.length() == 0) exceptions.add(new  ParseException("Model yet not complete. Element "+element+" not found"));

			
		}
		return element_newView;
		
	}
			
	
	


	

	
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
