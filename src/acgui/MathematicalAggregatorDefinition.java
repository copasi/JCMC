package acgui;

import org.COPASI.CCompartment;
import org.COPASI.CCopasiDataModel;
import org.COPASI.CCopasiObjectName;
import org.COPASI.CModel;
import org.COPASI.CModelValue;

/**
 * @author Thomas
 *
 */
public class MathematicalAggregatorDefinition extends ModuleDefinition
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String var_term_prefix = "term_";
	private String var_weight_prefix = "weight_";
	private String var_math_aggregation = "";
	
	private static String PLUS = " + ";
	private static String TIMES = " * ";
	
	private Operation op;
	//private CCopasiDataModel dataModel;
	private int inputNum;
	
	public MathematicalAggregatorDefinition(String iName, ModuleDefinition parent, byte[] imsmbData, int num_terms, Operation iOp)
	{
		super(iName, parent, imsmbData);
		op = iOp;
		//dataModel = AC_GUI.copasiUtility.getCopasiModelFromKey(copasiKey);
		inputNum = num_terms;
		
		switch(op) {
    	case SUM:	
    			var_math_aggregation = "SumTotal";
    			break;
    	case PRODUCT: 
    			var_math_aggregation = "ProdTotal";
    			break;
    	default:
    		System.err.println("MathematicalAggregator: operation not recognized");
    		return;
		}
	}
	
	public MathematicalAggregatorDefinition(String iName, String id, ModuleDefinition parent, int num_terms, Operation iOp)
	{
		super(iName, id, parent);
		op = iOp;
		//dataModel = AC_GUI.copasiUtility.getCopasiModelFromKey(copasiKey);
		inputNum = num_terms;
		
		switch(op) {
    	case SUM:	
    			var_math_aggregation = "SumTotal";
    			break;
    	case PRODUCT: 
    			var_math_aggregation = "ProdTotal";
    			break;
    	default:
    		System.err.println("MathematicalAggregator: operation not recognized");
    		return;
		}
	}
	
	public MathematicalAggregatorDefinition(String iName, ModuleDefinition parent, int num_terms, Operation iOp)
	{
		super(iName, parent);
		String OPERATION = "";
		boolean weighted = false;
		op = iOp;
		//dataModel = AC_GUI.copasiUtility.getCopasiModelFromKey(copasiKey);
		inputNum = num_terms;
		
	    switch(op) {
	    	case SUM:	
	    			OPERATION = PLUS; 	
	    			weighted = true;
	    			var_math_aggregation = "SumTotal";
	    			break;
	    	case PRODUCT: 
	    			OPERATION = TIMES; 
	    			weighted = false;
	    			var_math_aggregation = "ProdTotal";
	    			break;
	    	default:
	    		System.err.println("MathematicalAggregator: operation not recognized");
	    		return;
	    }
		
	    CCopasiDataModel dataModel = CopasiUtility.getCopasiModelFromModelName(iName);
		CModel model = dataModel.getModel();
		CCompartment comp = model.createCompartment("cell");
		
		String aggregated_expression = new String();
		
		for(int i = 0; i < num_terms; i++) {
			CModelValue m = model.createModelValue(var_term_prefix+i, 1.0);
			m.setStatus(CModelValue.FIXED);
			CModelValue w = null;
			if(weighted) {
				w = model.createModelValue(var_weight_prefix+i, 1.0);
				w.setStatus(CModelValue.FIXED);
			}
			
			aggregated_expression += "<"+m.getObject(new CCopasiObjectName("Reference=Value")).getCN().getString()+">";
			if(weighted) {
				aggregated_expression += TIMES
															+"<"+w.getObject(new CCopasiObjectName("Reference=Value")).getCN().getString()+">";
			}
			aggregated_expression += OPERATION;
		}
		aggregated_expression = aggregated_expression.substring(0, aggregated_expression.length() - PLUS.length());
		
		CModelValue modelValue = model.createModelValue(var_math_aggregation, 0.0);
		modelValue.setStatus(CModelValue.ASSIGNMENT);
		modelValue.setExpression(aggregated_expression);

		
		//----------- 
		//THIS PART NEED TO BE DELETED. Only there for debug purposes, so you can see that the model is actually generated
		// in AC however, the above code is going to fill the dataModel.getKey() model with the right code, 
		//so you don't want to save anything into file, the model is existing in that key, that now you can pass to 
		//your instance of MSMB to be displayed
		/*
		 try {
			 dataModel.saveModel(var_math_aggregation+".cps", true);
		       dataModel.exportSBML(var_math_aggregation+".xml", true, 2, 3);
	     }  catch(java.lang.Exception ex) {
	        ex.printStackTrace();
	     }
	     */
		//----------- 
	}
	
	public Operation getOperation()
	{
		return op;
	}
	
	public String getInputPrefix()
	{
		return var_term_prefix;
	}
	
	public String getOutputName()
	{
		return var_math_aggregation;
	}
	
	public int getNumberofInputs()
	{
		return inputNum;
	}
}
