package msmb.utility;

import java.util.HashMap;
import java.util.Vector;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

import msmb.gui.CustomTableModel_MSMB;
import msmb.gui.MainGui;
import msmb.model.Compartment;
import msmb.model.Function;
import msmb.model.GlobalQ;
import msmb.model.MultiModel;
import msmb.model.Reaction;
import msmb.model.Species;

import org.COPASI.CFunctionParameter;
import org.COPASI.CReaction;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;

import  msmb.parsers.mathExpression.MR_Expression_ParserConstants;
import  msmb.parsers.mathExpression.MR_Expression_ParserConstantsNOQUOTES;

public class AutocompleteDB {
	
	private final String initial = new String("_INITIAL");
	private final String constantString = new String("_CONST");
	private final String all = new String("_ALL");
	private final String conflictString = new String("_CONFLICT");
	MultiModel multiModel = null;
	
	HashMap<String, Vector<String>> completionOptions = new HashMap<String, Vector<String>>();
	HashMap<String, String> optionShortDescription = new HashMap<String, String>();
	HashMap<String, String> optionSummary = new HashMap<String, String>();
	
	public AutocompleteDB(MultiModel mm) {
		multiModel = mm;
		fillShortDescription();
		fillSummary();
		
		Vector constants = new Vector();
		String completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.CONST_AVOGADRO);
		constants.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.CONST_QUANTITY_CONV_FACTOR);
		constants.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.CONST_MODEL_TIME);
		constants.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.CONST_MODEL_TIME_INITIAL);
		constants.add(completion);
		
		String condition = constantString;
		Vector element = new Vector();	
		element.addAll(constants);
		completionOptions.put(condition, element);
	
		
		
		condition = conflictString+"_C_G_S";
		element = new Vector();	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_COMPARTMENT);
		element.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_GLOBALQ);
		element.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_SPECIES);
		element.add(completion);
		completionOptions.put(condition, element);
		
		condition = conflictString+"_C_G";
		element = new Vector();	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_COMPARTMENT);
		element.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_GLOBALQ);
		element.add(completion);
		completionOptions.put(condition, element);
		
		
		
		condition = conflictString+"_C_S";
		element = new Vector();	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_COMPARTMENT);
		element.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_SPECIES);
		element.add(completion);
		completionOptions.put(condition, element);
		
		condition = conflictString+"_G_S";
		element = new Vector();	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_GLOBALQ);
		element.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_SPECIES);
		element.add(completion);
		completionOptions.put(condition, element);
		
		condition = all;
		element = new Vector();	
		element.addAll(constants);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_COMPARTMENT);
		element.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_SPECIES);
		element.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC);
		element.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE);
		element.add(completion);	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS);
		element.add(completion);	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_RATE);
		element.add(completion);	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT);
		element.add(completion);	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_GLOBALQ);
		element.add(completion);
		completionOptions.put(condition, element);
		
		
		condition = Constants.TitlesTabs.SPECIES.getDescription() + initial;
		element = new Vector();	
		 completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_SPECIES);
		element.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC);
		completion += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT);
		element.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE);
		completion += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT);
		element.add(completion);	
		completionOptions.put(condition, element);
		
		
		condition = Constants.TitlesTabs.SPECIES.getDescription();
		element = new Vector();	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_SPECIES);
		element.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC);
		element.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE);
		element.add(completion);	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS);
		element.add(completion);	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_RATE);
		element.add(completion);	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT);
		element.add(completion);	
		completionOptions.put(condition, element);
		
		condition = Constants.TitlesTabs.COMPARTMENTS.getDescription() + initial;
		element = new Vector();	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_COMPARTMENT);
		element.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT);
		element.add(completion);	
		completionOptions.put(condition, element);
		
		condition = Constants.TitlesTabs.COMPARTMENTS.getDescription();
		element = new Vector();	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_COMPARTMENT);
		element.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS);
		element.add(completion);	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_RATE);
		element.add(completion);	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT);
		element.add(completion);	
		completionOptions.put(condition, element);
		
		
		
		condition = Constants.TitlesTabs.GLOBALQ.getDescription() + initial;
		element = new Vector();	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_GLOBALQ);
		element.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT);
		element.add(completion);	
		completionOptions.put(condition, element);
		
		
		condition = Constants.TitlesTabs.GLOBALQ.getDescription();
		element = new Vector();	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_GLOBALQ);
		element.add(completion);
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS);
		element.add(completion);	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_RATE);
		element.add(completion);	
		completion = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT);
		element.add(completion);	
		completionOptions.put(condition, element);
		
	}
	
	
	
	private void fillSummary() {
		String key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_SPECIES);
		String value = "Extension to indicate that the name belongs to the <b>Species</b> list. <br> It is <i>necessary</i> if the same name is used for different entities (e.g. Compartments, Global Quantites)";
		optionSummary.put(key, value);

		key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_COMPARTMENT);
		value = "Extension to indicate that the name belongs to the <b>Compartment</b> list. <br> It is <i>necessary</i> if the same name is used for different entities (e.g. Species, Global Quantites)";
		optionSummary.put(key, value);
	
		key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_GLOBALQ);
		value = "Extension to indicate that the name belongs to the <b>Global Quantity</b> list. <br> It is <i>necessary</i> if the same name is used for different entities (e.g. Compartments, Species)";
		optionSummary.put(key, value);
		
		key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC);
		value = "Extension to refer to the <b>Concentration value</b> of a Species.";
		optionSummary.put(key, value);
		
		key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE);
		value = "Extension to refer to the <b>Particle Number value</b> of a Species.";
		optionSummary.put(key, value);
		
		key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS);
		value = "Extension to refer to a <b>Transient value</b> of an entity.";
		optionSummary.put(key, value);
	
		key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_RATE);
		value = "Extension to refer to the <b>Rate</b> of an entity.";
		optionSummary.put(key, value);
	
		key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT);
		value = "Extension to refer to an <b>Initial value</b> of an entity.";
		optionSummary.put(key, value);
		
		key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.CONST_AVOGADRO);
		value = "Constant referring to the <b>Avogadro Number</b>.";
		optionSummary.put(key, value);
		
		key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.CONST_MODEL_TIME);
		value = "Constant referring to the <b>Model time</b>.";
		optionSummary.put(key, value);
		
		key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.CONST_MODEL_TIME_INITIAL);
		value = "Constant referring to the <b>Model initial time</b>.";
		optionSummary.put(key, value);
		
		key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.CONST_QUANTITY_CONV_FACTOR);
		value = "Constant referring to the <b>Quantity conversion factor</b>.";
		optionSummary.put(key, value);
	}



	private void fillShortDescription() {
		
		String key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_SPECIES);
		String value = "Species extension";
		optionShortDescription.put(key, value);
	
		key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_COMPARTMENT);
		value = "Compartment extension";
		optionShortDescription.put(key, value);
	
		key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_GLOBALQ);
		value = "Global quantity extension";
		optionShortDescription.put(key, value);
		
		key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_CONC);
		value = "Concentration value";
		optionShortDescription.put(key, value);
		
		key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_PARTICLE);
		value = "Particle Number value";
		optionShortDescription.put(key, value);
		
		key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_TRANS);
		value = "Transient value";
		optionShortDescription.put(key, value);
	
		key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_RATE);
		value = "Rate value";
		optionShortDescription.put(key, value);
	
		key = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_INIT);
		value = "Initial value";
		optionShortDescription.put(key, value);
	
	}


	public static  boolean selectedTextIsSignatureType(String selectedText) {
		if(selectedText.length() > 0 && 
				(	selectedText.startsWith(Constants.FunctionParamType.MODIFIER.signatureType+" ") ||
						selectedText.startsWith(Constants.FunctionParamType.PARAMETER.signatureType+" ") ||
						selectedText.startsWith(Constants.FunctionParamType.VARIABLE.signatureType+" ") ||
						selectedText.startsWith(Constants.FunctionParamType.PRODUCT.signatureType+" ") ||
						selectedText.startsWith(Constants.FunctionParamType.SITE.signatureType+" ") ||
						selectedText.startsWith(Constants.FunctionParamType.SUBSTRATE.signatureType+" ") ||
						selectedText.startsWith(Constants.FunctionParamType.VOLUME.signatureType+" ")
				)) {
			return true;
		}
		else return false;
	}
	public DefaultCompletionProvider getDefaultCompletionProvider(char trigger, String element, boolean isInitialExpression) {
		
		//System.out.println("element before " +element);
		if(element.endsWith(".")) {
		//	endWithDot = true;
			element = element.substring(0,element.length()-1);
		}
		//System.out.println("element after " +element);
		Vector<String> completions = new Vector<String>();
		DefaultCompletionProvider ret = new DefaultCompletionProvider()	{
			@Override
		
			public String getAlreadyEnteredText(JTextComponent comp) {
				if(comp.getSelectedText()!= null && selectedTextIsSignatureType(comp.getSelectedText())) {
					return new String();
				}
			else return super.getAlreadyEnteredText(comp);
	
		}
			};
			
		if(element.length() ==0) {
			if(trigger == '%') {
				completions = completionOptions.get(constantString);
				if(completions!= null) {
					for(int i = 0; i<completions.size(); i++) {
						if(completions.get(i).startsWith(Character.toString(trigger))) {
							String shortD = optionShortDescription.get(completions.get(i));
							String summary = optionSummary.get(completions.get(i));
							ret.addCompletion(new BasicCompletion(ret, completions.get(i).substring(1)+" ",shortD, summary)); // substring because I have to get rid of the first char that has already been typed to trigger this
						}
					}
				}
				return ret;
			
			}
		}
		
		if(trigger == '.') {
			Vector<Integer> definedInTables = null;

			String suffix = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_COMPARTMENT);
			if(element.endsWith(suffix)) {
				element = element.substring(0, suffix.length()-2);
				definedInTables = new Vector<Integer>();
				definedInTables.add(new Integer(Constants.TitlesTabs.COMPARTMENTS.index));
			} else {
				suffix = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_SPECIES);
				if(element.endsWith(suffix)) {
					element = element.substring(0, suffix.length()-2);
					definedInTables = new Vector<Integer>();
					definedInTables.add(new Integer(Constants.TitlesTabs.SPECIES.index));
				} else {
					suffix = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstants.EXTENSION_GLOBALQ);
					if(element.endsWith(suffix)) {
						element = element.substring(0, suffix.length()-2);
						definedInTables = new Vector<Integer>();
						definedInTables.add(new Integer(Constants.TitlesTabs.GLOBALQ.index));
					} 
				}
			}

	
			if(definedInTables==null) {
				definedInTables = multiModel.getWhereNameIsUsed(element);	
			}

			if(definedInTables.size() == 1) {
				String where = new String();
				if(definedInTables.get(0).intValue()==Constants.TitlesTabs.SPECIES.index) where =  Constants.TitlesTabs.SPECIES.getDescription();
				else if(definedInTables.get(0).intValue()==Constants.TitlesTabs.COMPARTMENTS.index) where =  Constants.TitlesTabs.COMPARTMENTS.getDescription();
				else if(definedInTables.get(0).intValue()==Constants.TitlesTabs.GLOBALQ.index) where =  Constants.TitlesTabs.GLOBALQ.getDescription();
				if(isInitialExpression) where += initial;
				completions.addAll(completionOptions.get(where));
			}
			else {
				String conflicts = conflictString;
				boolean confl_S = false;
				boolean confl_G = false;
				boolean confl_C = false;

				for(int i = 0; i < definedInTables.size(); i++) {
					if(definedInTables.get(i).intValue() == Constants.TitlesTabs.SPECIES.index) confl_S = true;
					if(definedInTables.get(i).intValue() == Constants.TitlesTabs.COMPARTMENTS.index) confl_C = true;
					if(definedInTables.get(i).intValue() == Constants.TitlesTabs.GLOBALQ.index) confl_G = true;
				}

				if(confl_C) conflicts += "_C";
				if(confl_G) conflicts += "_G";
				if(confl_S) conflicts += "_S";

				completions.addAll(completionOptions.get(conflicts));
			}
			for(int i = 0; i<completions.size(); i++) {
				if(completions.get(i).startsWith(Character.toString(trigger))) {
					String shortD = optionShortDescription.get(completions.get(i));
					String summary = optionSummary.get(completions.get(i));
					String tmp_compl = completions.get(i);
					//if(excludeFirstChar)
					tmp_compl= tmp_compl.substring(1); // substring because I have to get rid of the first char that has already been typed to trigger this
					ret.addCompletion(new BasicCompletion(ret, tmp_compl+" ",shortD, summary)); 
				}
			}
		
		} else {
			Vector<Vector<String>> completions1 = getAutocompletionFromContext();
			
			//Vector<Vector<String>> completions1 = getAllDefinedFunctionsAutocompletion();
			if(completions1!= null) {
				for(int i = 0; i<completions1.size(); i++) {
						Vector<String> el = completions1.get(i);
						String signature = el.get(0);
						String longDescr = el.get(1);
						ret.addCompletion(new BasicCompletion(ret, signature ,null, longDescr)); 
					
				}
			}
		}

	
		//ret.setAutoActivationRules(true, "\"");
	
		return ret;



	}



	public Vector<Vector<String>> getAutocompletionFromContext() {
		
		if(MainGui.autocompletionContext==Constants.FunctionParamType.VARIABLE.copasiType) {
			return getAllNamedElements();
		}
		else if(MainGui.autocompletionContext==Constants.FunctionParamType.SUBSTRATE.copasiType) {
			 if(MainGui.cellTableEdited.compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0) {
				 return MainGui.getSubstratesSelectedReaction();
			} else return new Vector<Vector<String>>();
		}
		else if(MainGui.autocompletionContext==Constants.FunctionParamType.MODIFIER.copasiType) {
			 if(MainGui.cellTableEdited.compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0) {
				 return MainGui.getModifiersSelectedReaction();
			} else return new Vector<Vector<String>>();
		}
		else if(MainGui.autocompletionContext==Constants.FunctionParamType.PRODUCT.copasiType) {
			 if(MainGui.cellTableEdited.compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0) {
				 return MainGui.getModifiersSelectedReaction();
			} else return new Vector<Vector<String>>();
		}
		else if(MainGui.autocompletionContext==Constants.FunctionParamType.PARAMETER.copasiType) {
			return getAllDefinedGlobalQuantities();
		}
		else if(MainGui.autocompletionContext==Constants.FunctionParamType.FUNCTION.copasiType) {
			return getAllDefinedFunctions();
		}
		else 	if(MainGui.autocompletionContext==Constants.FunctionParamType.ASSIGNMENT_FLAG.copasiType) {
			Vector<Vector<String>> allNames = getAllNamedElements();
			CustomTableModel_MSMB tableModel = MainGui.getTableModelFromDescription(MainGui.cellTableEdited);
			String currentVariable = (String) tableModel.getValueAt(MainGui.cellSelectedRow, MainGui.getMainElementColumn(MainGui.cellTableEdited));
			Vector<Vector<String>> ret = new Vector<Vector<String>>();
			for(Vector<String> element : allNames) {
				if(element.get(0).compareTo(currentVariable)==0 &&
						element.get(1).startsWith(MainGui.cellTableEdited.substring(0, MainGui.cellTableEdited.indexOf(" "))) ) {
					continue;
				} else {
					ret.add(element);
				}
			}
			return ret;
		}
		return getAllNamedElements();
		
		
		
		
	}



	public Vector<Vector<String>> getAllNamedElements() {
		Vector<Vector<String>> ret = new Vector<Vector<String>>();
		if(multiModel == null) return ret;
		
		ret.addAll(getAllDefinedFunctions());
		ret.addAll(getAllDefinedSpecies());
		ret.addAll(getAllDefinedGlobalQuantities());
		ret.addAll(getAllDefinedCompartments());
		return ret;
	}

	public Vector<Vector<String>> getAllDefinedSpecies() {
		Vector<Vector<String>> ret = new Vector<Vector<String>>();
		Vector<Species> elements = multiModel.getAllSpecies();
		
		for(int i = 0; i< elements.size(); i++) {
			Vector<String> element = new Vector<String>();
			Species current =  elements.get(i);
			if(current==null) continue;
			String name  = current.getSpeciesName();
			
			if(name.toString().trim().startsWith("\"") &&  name.toString().trim().endsWith("\"")) {
				name = (name.toString().trim().substring(1,name.toString().trim().length()-1));
			}
			
			element.add(name);
			try {
				element.add("Species complete definition: \n" +current.getAllFields().toString());// long description with the function equation
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			} 
			ret.add(element);
		}
		return ret;
	}
	
	public Vector<Vector<String>> getAllDefinedGlobalQuantities() {
		Vector<Vector<String>> ret = new Vector<Vector<String>>();
		Vector<GlobalQ> elements = multiModel.getAllGlobalQ();
		
		for(int i = 0; i< elements.size(); i++) {
			Vector<String> element = new Vector<String>();
			GlobalQ current =  elements.get(i);
			if(current==null) continue;
			String name  = current.getName();
			
			if(name.toString().trim().startsWith("\"") &&  name.toString().trim().endsWith("\"")) {
				name = (name.toString().trim().substring(1,name.toString().trim().length()-1));
			}
			
			element.add(name);
			try {
				element.add("Global quantity complete definition: \n" +current.getAllFields().toString());// long description with the function equation
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			} 
			ret.add(element);
		}
		return ret;
	}
	
	public Vector<Vector<String>> getAllDefinedCompartments() {
		Vector<Vector<String>> ret = new Vector<Vector<String>>();
		Vector<Compartment> elements = multiModel.getAllCompartments();
		
		for(int i = 0; i< elements.size(); i++) {
			Vector<String> element = new Vector<String>();
			Compartment current =  elements.get(i);
			if(current==null) continue;
			String name  = current.getName();
			
			if(name.toString().trim().startsWith("\"") &&  name.toString().trim().endsWith("\"")) {
				name = (name.toString().trim().substring(1,name.toString().trim().length()-1));
			}
			
			element.add(name);
			try {
				element.add("Compartment complete definition: \n" +current.getAllFields().toString());// long description with the function equation
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			} 
			ret.add(element);
		}
		return ret;
	}
	
	public Vector<Vector<String>> getAllDefinedFunctions() {
		if(MainGui.cellTableEdited.compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0) {
			return getAllDefinedFunctions(true);
		} else {
			return  getAllDefinedFunctions(false);
		}
	}
	
	public Vector<Vector<String>> getAllDefinedFunctions(boolean allowSubProdMod) {
		Vector<Vector<String>> ret = new Vector<Vector<String>>();
		Vector<Function> functions = multiModel.funDB.getAllUserDefinedFunctions();
		
	
		for(int i = 0; i< functions.size(); i++) {
			Vector<String> element = new Vector<String>();
			Function f = functions.get(i);
			if(f==null) continue;
			String name = f.getName();
			if(name.toString().trim().startsWith("\"") &&  name.toString().trim().endsWith("\"")) {
				name = (name.toString().trim().substring(1,name.toString().trim().length()-1));
			}
			
			Vector<Integer> types = f.getParametersTypes_CFunctionParameter();
			if(!allowSubProdMod) {
				boolean typesOK=true;
				for(int t = 0; t < types.size(); t++) {
					Integer element1 = types.get(t);
					if(element1 == CFunctionParameter.SUBSTRATE || 
							element1 == CFunctionParameter.MODIFIER ||
							element1 == CFunctionParameter.PRODUCT ) {
						typesOK = false;
						break;
					}
				}
				if(!typesOK) continue;
			} else {
				Reaction reaction = multiModel.getReaction(MainGui.cellSelectedRow+1);
				Vector subs = reaction.getSubstrates(multiModel);
				Vector prod = reaction.getProducts(multiModel);
				Vector mod = reaction.getModifiers(multiModel);
				boolean typesOK=true;
				for(int t = 0; t < types.size(); t++) {
					Integer element1 = types.get(t);
					if(element1 == CFunctionParameter.SUBSTRATE && (subs== null || subs.size() == 0) ) {
						typesOK = false;
						break;
					}
					if(element1 == CFunctionParameter.MODIFIER  && (mod== null || mod.size() == 0) ) {
						typesOK = false;
						break;
					}
					if(element1 == CFunctionParameter.PRODUCT  && (prod== null || prod.size() == 0) ) {
						typesOK = false;
						break;
					}
				}
				if(!typesOK) continue;
			}
			
			
			
			element.add(name);
			try {
				element.add(f.printCompleteSignature()+"\n\n"+f.getExpandedEquation(new Vector()));// long description with the function equation
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			} 
			ret.add(element);
		}
		
		functions = multiModel.funDB.getAllBuiltInFunctions();
		for(int i = 0; i< functions.size(); i++) {
			Vector<String> element = new Vector<String>();
			Function f = functions.get(i);
			if(f==null) continue;
			String name = f.getName();
			if(name.toString().trim().startsWith("\"") &&  name.toString().trim().endsWith("\"")) {
				name = (name.toString().trim().substring(1,name.toString().trim().length()-1));
			}
			Vector<Integer> types = f.getParametersTypes_CFunctionParameter();
			if(!allowSubProdMod) {
				boolean typesOK=true;
				for(int t = 0; t < types.size(); t++) {
					Integer element1 = types.get(t);
					if(element1 == CFunctionParameter.SUBSTRATE || 
							element1 == CFunctionParameter.MODIFIER ||
							element1 == CFunctionParameter.PRODUCT ) {
						typesOK = false;
						break;
					}
				}
				if(!typesOK) continue;
			} else {
				Reaction reaction = multiModel.getReaction(MainGui.cellSelectedRow+1);
				Vector subs = reaction.getSubstrates(multiModel);
				Vector prod = reaction.getProducts(multiModel);
				Vector mod = reaction.getModifiers(multiModel);
				boolean typesOK=true;
				for(int t = 0; t < types.size(); t++) {
					Integer element1 = types.get(t);
					if(element1 == CFunctionParameter.SUBSTRATE && (subs== null || subs.size() == 0) ) {
						typesOK = false;
						break;
					}
					if(element1 == CFunctionParameter.MODIFIER  && (mod== null || mod.size() == 0) ) {
						typesOK = false;
						break;
					}
					if(element1 == CFunctionParameter.PRODUCT  && (prod== null || prod.size() == 0) ) {
						typesOK = false;
						break;
					}
				}
				if(!typesOK) continue;
			}
			
			element.add(name);
			try {
				element.add(f.printCompleteSignature()+"\n\n"+f.getExpandedEquation(new Vector()));// long description with the function equation
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			} 
			ret.add(element);
		}
		return ret;
	}



	
	/*public DefaultCompletionProvider getDefaultCompletionProvider(char trigger) {
	
			DefaultCompletionProvider ret = new DefaultCompletionProvider();
			Vector<String> completions = completionOptions.get(all);
			if(completions!= null) {
				for(int i = 0; i<completions.size(); i++) {
					if(completions.get(i).startsWith(Character.toString(trigger))) {
						String shortD = optionShortDescription.get(completions.get(i));
						String summary = noContextualAutocomplete + "<br><br>" +optionSummary.get(completions.get(i));
						ret.addCompletion(new BasicCompletion(ret, completions.get(i).substring(1)+" ", shortD, summary)); // substring because I have to get rid of the first char that has already been typed to trigger this
					}
				}
			}
			return ret;
		
	}*/

}
