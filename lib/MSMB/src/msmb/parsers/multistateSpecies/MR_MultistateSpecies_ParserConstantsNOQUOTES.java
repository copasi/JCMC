package msmb.parsers.multistateSpecies;

public  class MR_MultistateSpecies_ParserConstantsNOQUOTES implements MR_MultistateSpecies_ParserConstants {
	
	public static String getTokenImage(int which) {
		String ret = MR_MultistateSpecies_ParserConstants.tokenImage[which];
		ret = ret.substring(1,ret.length()-1);
		return ret;
	}

}
