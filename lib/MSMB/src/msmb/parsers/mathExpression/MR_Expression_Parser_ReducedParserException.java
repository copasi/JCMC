package msmb.parsers.mathExpression;

import java.io.InputStream;

public class MR_Expression_Parser_ReducedParserException extends
		MR_Expression_Parser {

	public MR_Expression_Parser_ReducedParserException(InputStream stream) {
		super(stream);
	}
	
	  public MR_Expression_Parser_ReducedParserException(InputStream stream, String encoding) {
		  super(stream, encoding);
	  }

	
	@Override
	public ParseException generateParseException() {
		ParseException exc = super.generateParseException();
		 String eol = System.getProperty("line.separator", "\n");
		String retval = "Found token \"";
	    Token tok = exc.currentToken.next;
	    retval += " " + MR_Expression_ParserConstantsNOQUOTES.getTokenImage(tok.kind);
	    retval += " \" at position #" + exc.currentToken.next.beginColumn;
	    retval += "." + eol;
	    retval += "The expression does not follow the correct syntax."+eol;
	    
	    if(tok.kind == MR_Expression_ParserConstants.DOT){
	    	retval += "Was expecting one of:" + eol + "    ";
	    	retval += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(EXTENSION_COMPARTMENT);
	    	retval += eol+ "    ";
	    	retval += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(EXTENSION_CONC);
	    	retval += eol+ "    ";
	    	retval += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(EXTENSION_GLOBALQ);
	    	retval += eol+ "    ";
	    	retval += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(EXTENSION_INIT);
	    	retval += eol+ "    ";
	    	retval += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(EXTENSION_PARTICLE);
	    	retval += eol+ "    ";
	    	retval += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(EXTENSION_RATE);
	    	retval += eol+ "    ";
	    	retval += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(EXTENSION_SPECIES);
	    	retval += eol+ "    ";
	    	retval += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(EXTENSION_TRANS);
	    	retval += eol+ "    ";
	    } else    if(tok.kind == MR_Expression_ParserConstants.PERC){
	    	retval += "Was expecting one of:" + eol + "    ";
	    	retval += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(CONST_AVOGADRO);
		   	retval += eol+ "    ";
		   	retval += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(CONST_MODEL_TIME);
		   	retval += eol+ "    ";
		   	retval += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(CONST_MODEL_TIME_INITIAL);
		   	retval += eol+ "    ";
		   	retval += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(CONST_QUANTITY_CONV_FACTOR);
		   	retval += eol+ "    ";
	    } else {
	    	StringBuffer expected = new StringBuffer();
	        int maxSize = 0;
	        for (int i = 0; i < exc.expectedTokenSequences.length; i++) {
	          if (maxSize < exc.expectedTokenSequences[i].length) {
	            maxSize = exc.expectedTokenSequences[i].length;
	          }
	          for (int j = 0; j < exc.expectedTokenSequences[i].length; j++) {
	            expected.append(tokenImage[exc.expectedTokenSequences[i][j]]).append(' ');
	          }
	          if (exc.expectedTokenSequences[i][exc.expectedTokenSequences[i].length - 1] != 0) {
	            expected.append("...");
	          }
	          expected.append(eol).append("    ");
	        }
	        retval += expected.toString();
	    }
	    
		return new ParseException(retval);
	}

}
