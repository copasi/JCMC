package msmb.parsers.mathExpression;

public  class MR_Expression_ParserConstantsNOQUOTES implements MR_Expression_ParserConstants {
	
	public static String getTokenImage(int which) {
		String ret = MR_Expression_ParserConstants.tokenImage[which];
		ret = ret.substring(1,ret.length()-1);
		return ret;
	}

}
