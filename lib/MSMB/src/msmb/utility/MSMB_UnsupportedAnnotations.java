package msmb.utility;

public class MSMB_UnsupportedAnnotations {

	public static String xmlns = "http://www.copasi.org/tiki-index.php?page=MSMB&structure=SoftwareProjects";
	public static enum MSMB_UnsupportedAnnotations_type {
		GET_PARTICLE_NUMBER,
		MULTISTATE_SPECIES,
		MULTISTATE_REACTION,
		MULTISTATE_REACTION_RATE_LAW,
		GLQ_PARAMETER_TYPE_IN_CFUNCTION,
		MULTISTATE_TYPE_ACTUAL_PARAMETER,
		EXPRESSION_WITH_MULTISTATE_SUM,
		EXPANDED_EVENT,
		REACTION_EMPTY_NAME;		
	}
	
	
	String name = new String();
	String annotation = new String();
	
	public MSMB_UnsupportedAnnotations(MSMB_UnsupportedAnnotations_type type, String value) {
		switch(type) {
		case GET_PARTICLE_NUMBER:
			name = xmlns+"_getParticleNumber";
			annotation =  "<"+MSMB_xml_annotation.GET_PART_NUM.description+" xmlns=\""+xmlns+"\" value=\""+value+"\"/>";
		break;
			case MULTISTATE_REACTION:
				name = xmlns+"_multistateReaction";
				annotation =  "<"+MSMB_xml_annotation.MULTISTATE_R.description+" xmlns=\""+xmlns+"\" value=\""+value+"\"/>";
			break;
			case MULTISTATE_REACTION_RATE_LAW:
				name = xmlns+"_multistateReactionRateLaw";
				annotation =  "<"+MSMB_xml_annotation.MULTISTATE_R_RATELAW.description+" xmlns=\""+xmlns+"\" value=\""+value+"\"/>";
			break;
			case MULTISTATE_SPECIES:
				name = xmlns+"_multistateSpecies";
				annotation =  "<"+MSMB_xml_annotation.MULTISTATE_SP.description+" xmlns=\""+xmlns+"\" value=\""+value+"\"/>";
				break;
			case GLQ_PARAMETER_TYPE_IN_CFUNCTION:
				name = xmlns+"_"+value;
				annotation =  "<"+MSMB_xml_annotation.GLQ_TYPE.description+" xmlns=\""+xmlns+"\" value=\""+value+"\"/>";
				break;
			case MULTISTATE_TYPE_ACTUAL_PARAMETER:
				name = xmlns+"_"+value; //the value is the index
				annotation =  "<"+MSMB_xml_annotation.MULTISTATE_ACTUAL.description+" xmlns=\""+xmlns+"\" value=\""+value+"\"/>";
			break;
			case REACTION_EMPTY_NAME:
				name = xmlns+"_reactionNameEmpty";
				annotation =  "<"+MSMB_xml_annotation.REACTION_EMPTY.description+" xmlns=\""+xmlns+"\" value=\"\"/>";
				break;
			case EXPRESSION_WITH_MULTISTATE_SUM:
				name = xmlns+"_expressionWithMultistateSum";
				annotation =  "<"+MSMB_xml_annotation.MULTISTATE_SUM.description+" xmlns=\""+xmlns+"\" value=\""+value+"\"/>";
				break;
			case EXPANDED_EVENT:
				name = xmlns+"_expressionEventExpanded";
				annotation =  "<"+MSMB_xml_annotation.EXPANDED_EVENT.description+" xmlns=\""+xmlns+"\" value=\""+value+"\"/>";
				break;
			default:
				System.err.println("Not an available MSMB annotation");
				break;
		}
		
	}
	
	public String getName() { return name;  }
	public String getAnnotation() { return annotation; }
	
	

	
	enum MSMB_xml_annotation {
		   GET_PART_NUM("msmb_getParticleNumber"),
		   MULTISTATE_R ("msmb_multistateReact"),
		   MULTISTATE_R_RATELAW ("msmb_multistateRateLaw"),
		   MULTISTATE_SP ("msmb_multistateSp"),
		   MULTISTATE_SUM("msmb_multistateSumExpression"),
		   GLQ_TYPE("msmb_GLQtype"),
		   MULTISTATE_ACTUAL("msmb_multistateActual"),
		   EXPANDED_EVENT("msmb_expandedEvent"),
		   REACTION_EMPTY("msmb_reactionEmpty");
		          
		   public final String description;
		   
		   MSMB_xml_annotation(String descr) {
		              this.description = descr;
		    }
	}


	public static boolean is_MultistateSpecies(String annotation) {
		return annotation.contains(MSMB_xml_annotation.MULTISTATE_SP.description);
	}
	
	public static boolean is_ExpandedEvent(String annotation) {
		return annotation.contains(MSMB_xml_annotation.EXPANDED_EVENT.description);
	}
	
	public static boolean is_MultistateActualParameter(String annotation) {
		return annotation.contains(MSMB_xml_annotation.MULTISTATE_ACTUAL.description);
	}
	
	public static boolean is_getParticleNumber(String annotation) {
		return annotation.contains(MSMB_xml_annotation.GET_PART_NUM.description);
	}

	public static boolean is_MultistateReaction(String annotation) {
		return annotation.contains(MSMB_xml_annotation.MULTISTATE_R.description);
	}
	
	public static boolean is_ExpressionWithMultistateSum(String annotation) {
		return annotation.contains(MSMB_xml_annotation.MULTISTATE_SUM.description);
	}
	
	public static boolean is_MultistateReactionRateLaw(String annotation) {
		return annotation.contains(MSMB_xml_annotation.MULTISTATE_R_RATELAW.description);
	}
	
	public static boolean is_GlqParTypeInFun(String annotation) {
		return annotation.contains(MSMB_xml_annotation.GLQ_TYPE.description);
	}
	
	public static boolean is_ReactionEmptyName(String annotation) {
		return annotation.contains(MSMB_xml_annotation.REACTION_EMPTY.description);
	}

	public static String extractAnnotationValue(String annotation) {
		int valStart = annotation.indexOf("value=") + (new String("value=")).length()+1;
		int valEnd = annotation.indexOf('"',valStart);
		return annotation.substring(valStart,valEnd);
	}

	
}





