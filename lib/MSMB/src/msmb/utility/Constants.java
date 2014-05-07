package msmb.utility;

import java.awt.Color;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;


import org.COPASI.CCompartment;
import org.COPASI.CFunction;
import org.COPASI.CFunctionParameter;
import org.COPASI.CMetab;
import org.COPASI.CModel;
import org.COPASI.CModelValue;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;

import msmb.gui.MainGui;
import  msmb.parsers.mathExpression.MR_Expression_ParserConstants;
import  msmb.parsers.mathExpression.MR_Expression_ParserConstantsNOQUOTES;

public class Constants {
	
	//-----------------------------------------------------
	//- VIRGINIA TECH COLOR PALETTE FROM:
	//- http://www.unirel.vt.edu/old-files/web/guidelines/web-palette/index.html
	//-----------------------------------------------------
	public static final Color vt_maroon = new Color(102,0,0);
	public static final Color vt_orange = new Color(255,102,0);
	
	public static final Color vt_blues_1 = new Color(147,167,179);
	public static final Color vt_blues_2 = new Color(85,112,130);
	public static final Color vt_blues_3 = new Color(60,91,111);
	public static final Color vt_blues_4 = new Color(18,37,44);
	
	public static final Color vt_red_1 = new Color(152,0,0);
	public static final Color vt_red_2 = new Color(122,0,0);
	public static final Color vt_red_3 = new Color(92,0,0);
	public static final Color vt_red_4 = new Color(76,0,0);
	
	public static final Color vt_green_1 = new Color(152,182,143);
	public static final Color vt_green_2 = new Color(121,148,108);
	public static final Color vt_green_3 = new Color(81,111,67);
	public static final Color vt_green_4 = new Color(48,80,32);
	
	public static final Color vt_gold_1 = new Color(180,122,31);
	public static final Color vt_gold_2 = new Color(168,97,7);
	public static final Color vt_gold_3 = new Color(140,82,6);
	public static final Color vt_gold_4 = new Color(153,51,0);
	
	public static final Color vt_cream_1 = new Color(245,245,235);
	public static final Color vt_cream_2 = new Color(237,234,218);
	public static final Color vt_cream_3 = new Color(219,216,188);
	public static final Color vt_cream_4 = new Color(157,152,121);
	
	public static final Color vt_gray_1 = new Color(194,193,186);
	public static final Color vt_gray_2 = new Color(100,100,100);
	public static final Color vt_gray_3 = new Color(64,64,57);
	public static final Color vt_gray_4 = new Color(51,51,51);
		
	  //-----------------------------------------------------
	
		public  static enum ErrorLevelDisplay {
			   ALL_MESSAGES(-1,"All messages"), //ALSO THE FIXED ISSUES?
			   NO_MESSAGES(0,"Suppress Messages"), 
			   MAJOR(1,"Major issues"), 
			   DEFAULTS(2, "Defaults"),
			   MINOR(3,"Minor issues");
			          
			   public final int code;
			   public final String description;
			          
			   ErrorLevelDisplay(int code, String descr) {
			              this.code = code;
			              this.description = descr;
			    }
			   
			   public static int getCode(String descr){
				   if (descr != null) {
					      for (ErrorLevelDisplay b : ErrorLevelDisplay.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.code;
					      }
					    }
				} 
				   return -100;
			   }
			   
			   public static String getDescription(int code){
			      for (ErrorLevelDisplay b : ErrorLevelDisplay.values()) {
				        if (code == b.code) {
				          return b.description;
				      }
				    }
				   return new String();
			   }
			   
			   	 
		};
		//----------------------------------------------------
		
		public static enum StatusIssue {
			   OPEN(0,"Open"), //ALSO THE FIXED ISSUES?
			   ACKNOWLEDGED(1,"Acknowledged"), 
			   FIXED(-1,"Fixed");
			          
			   public final int code;
			   public final String description;
			          
			   StatusIssue(int code, String descr) {
			              this.code = code;
			              this.description = descr;
			    }
			   
			   public static int getCode(String descr){
				   if (descr != null) {
					      for (StatusIssue b : StatusIssue.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.code;
					      }
					    }
				} 
				   return -100;
			   }
			   
			   public static String getDescription(int code){
			      for (StatusIssue b : StatusIssue.values()) {
				        if (code == b.code) {
				          return b.description;
				      }
				    }
				   return new String();
			   }
		 
		};
		
		

		
		
		public static final int DEFAULT_START_INDEX_LIST_STATES = 1;
		public static final String COPASI_STRING_TIME = "Reference=Time";
		public static final String COPASI_STRING_INITIAL_TIME = "Reference=Initial Time";
		public static final String COPASI_STRING_PI = "PI";
		public static final String COPASI_STRING_EXPONENTIALE = "EXPONENTIALE";		
		public static final String COPASI_STRING_NAN = "nan";		
			public static final String COPASI_STRING_AVOGADRO= "Reference=Avogadro Constant";		
		
		public static final int RENAMING_OPTION_ALL = 100;
		public static final int RENAMING_OPTION_CUSTOM = 101;
		public static final int RENAMING_OPTION_NONE = 102;
		
		public static final String RENAMING_OPTION_ALL_STRING = new String("Automatic rename all connected elements");
		public static final String RENAMING_OPTION_CUSTOM_STRING = new String("Custom renaming choice");
		public static final String RENAMING_OPTION_NONE_STRING = new String("No automatic renaming");
		
		public static final Vector<String> compartments_columns;

		static{
			compartments_columns = new Vector<String>();
			for(int i = 1; ;i++) {
				String descr = CompartmentsColumns.getDescriptionFromIndex(i);
				if(descr.length() <=0) break;
				compartments_columns.add(descr);
			}
		}
		
		public static enum CompartmentsColumns {
			   NAME(1,"Name"), 
			   TYPE(2,"Type"), 
			   INITIAL_SIZE(3,"Initial size"), 
			   EXPRESSION(4,"Expression"), 
			   NOTES(5,"Notes");   
			          
			   public final int index;
			   public final String description;
			   
			   CompartmentsColumns(int index, String descr) {
			              this.index = index;
			              this.description = descr;
			    }
			   
			   public static int getIndex(String descr){
				   if (descr != null) {
					      for (CompartmentsColumns b : CompartmentsColumns.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.index;
					      }
					    }
				   } 
				   return -1;
			   }
			   
			   public static String getDescriptionFromIndex(int index){
			      for (CompartmentsColumns b : CompartmentsColumns.values()) {
				        if (index == b.index) {
				          return b.description;
				      }
				    }
				   return new String();
			   }

			public String getDescription() {
				return description;
			}
		};
		
		
		
		
		
		public  static enum CompartmentsType {
			   FIXED(0,"Fixed",CCompartment.FIXED), 
			   ASSIGNMENT(1,"Assignment", CCompartment.ASSIGNMENT), 
			   ODE(2,"ODE", CCompartment.ODE);
			          
			   public final int arrayIndex;
			   public final String description;
			   public final int copasiType;
			   
			          
			   CompartmentsType(int index, String descr, int cType) {
			              this.arrayIndex = index;
			              this.description = descr;
			              this.copasiType = cType;
			    }
			   
			   public static int getIndex(String descr){
				   if (descr != null) {
					      for (CompartmentsType b : CompartmentsType.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.arrayIndex;
					      }
					    }
				   } 
				   return -100;
			   }
			   
			   public static String getDescriptionFromIndex(int index){
			      for (CompartmentsType b : CompartmentsType.values()) {
				        if (index == b.arrayIndex) {
				          return b.description;
				      }
				    }
				   return new String();
			   }
			   
			   public static String getDescriptionFromCopasiType(int ctype){
				      for (CompartmentsType b : CompartmentsType.values()) {
					        if (ctype == b.copasiType) {
					          return b.description;
					      }
					    }
					   return new String();
				   }
		
			   public static int getCopasiTypeFromIndex(int index){
				      for (CompartmentsType b : CompartmentsType.values()) {
					        if (index == b.arrayIndex) {
					          return b.copasiType;
					      }
					    }
					   return -100;
				   }
			   
			   public static int getCopasiTypeFromDescription(String description){
				      for (CompartmentsType b : CompartmentsType.values()) {
					        if (description.compareTo(b.description)==0) {
					          return b.copasiType;
					      }
					    }
					   return -100;
				   }

			public String getDescription() {
				return description;
			}
		};
		
		
		public static final Vector<String> compartmentsTypes;
		static {
			compartmentsTypes = new Vector<String>();
			compartmentsTypes.add(Constants.CompartmentsType.FIXED.description);
			compartmentsTypes.add(Constants.CompartmentsType.ASSIGNMENT.description);
			compartmentsTypes.add(Constants.CompartmentsType.ODE.description);
		}
	   
		
		
		
		public static final Vector<String> globalQ_columns;
		static{
			globalQ_columns = new Vector<String>();
			for(int i = 1; ;i++) {
				String descr = GlobalQColumns.getDescriptionFromIndex(i);
				if(descr.length() <=0) break;
				globalQ_columns.add(descr);
			}
		}
		
		public static enum GlobalQColumns {
			   NAME(1,"Name"), 
			   VALUE(2, "Initial value"),
			   TYPE(3,"Type"), 
			   EXPRESSION(4,"Expression"), 
			   NOTES(5,"Notes");   
			          
			   public final int index;
			   public final String description;
			   
			   GlobalQColumns(int index, String descr) {
			              this.index = index;
			              this.description = descr;
			    }
			   
			   public static int getIndex(String descr){
				   if (descr != null) {
					      for (GlobalQColumns b : GlobalQColumns.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.index;
					      }
					    }
				   } 
				   return -1;
			   }
			   
			   public static String getDescriptionFromIndex(int index){
			      for (GlobalQColumns b : GlobalQColumns.values()) {
				        if (index == b.index) {
				          return b.description;
				      }
				    }
				   return new String();
			   }

			public String getDescription() {
				return description;
			}
		};
		
		
		
		public  static enum GlobalQType {
			   FIXED(0,"Fixed",CModelValue.FIXED), 
			   ASSIGNMENT(1,"Assignment", CModelValue.ASSIGNMENT), 
			   ODE(2,"ODE", CModelValue.ODE);
			          
			   public final int arrayIndex;
			   public final String description;
			   public final int copasiType;
			   
			   GlobalQType(int index, String descr, int cType) {
			              this.arrayIndex = index;
			              this.description = descr;
			              this.copasiType = cType;
			    }
			   
			   public static int getIndex(String descr){
				   if (descr != null) {
					      for (GlobalQType b : GlobalQType.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.arrayIndex;
					      }
					    }
				   } 
				   return -100;
			   }
			   
			   public static String getDescriptionFromIndex(int index){
			      for (GlobalQType b : GlobalQType.values()) {
				        if (index == b.arrayIndex) {
				          return b.description;
				      }
				    }
				   return new String();
			   }
			   
			   public static String getDescriptionFromCopasiType(int ctype){
				      for (GlobalQType b : GlobalQType.values()) {
					        if (ctype == b.copasiType) {
					          return b.description;
					      }
					    }
					   return new String();
				   }
		
			   public static int getCopasiTypeFromIndex(int index){
				      for (GlobalQType b : GlobalQType.values()) {
					        if (index == b.arrayIndex) {
					          return b.copasiType;
					      }
					    }
					   return -100;
				   }
			   
			   public static int getCopasiTypeFromDescription(String description){
				      for (GlobalQType b : GlobalQType.values()) {
					        if (description.compareTo(b.description)==0) {
					          return b.copasiType;
					      }
					    }
					   return -100;
				   }

			public String getDescription() {
				return description;
			}
		};
		
		public static final Vector<String> globalQTypes;
		static {
			globalQTypes = new Vector<String>();
			globalQTypes.add(Constants.GlobalQType.FIXED.description);
			globalQTypes.add(Constants.GlobalQType.ASSIGNMENT.description);
			globalQTypes.add(Constants.GlobalQType.ODE.description);
		}
		
		
		
		public static final Vector<String> functions_columns;
		static{
			functions_columns = new Vector<String>();
			for(int i = 1; ;i++) {
				String descr = FunctionsColumns.getDescriptionFromIndex(i);
				if(descr.length() <=0) break;
				functions_columns.add(descr);
			}
		}
		
		public static enum FunctionsColumns {
			   NAME(1,"Name"), 
			   EQUATION(2, "Equation"),
			   PARAMETER_ROLES(3,"   "), 
			  // SIGNATURE(4,"   "), 
			   NOTES(4,"Notes");   
			          
			   public final int index;
			   public final String description;
			   
			   FunctionsColumns(int index, String descr) {
			              this.index = index;
			              this.description = descr;
			    }
			   
			   public static int getIndex(String descr){
				   if (descr != null) {
					      for (FunctionsColumns b : FunctionsColumns.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.index;
					      }
					    }
				   } 
				   return -1;
			   }
			   
			   public static String getDescriptionFromIndex(int index){
			      for (FunctionsColumns b : FunctionsColumns.values()) {
				        if (index == b.index) {
				          return b.description;
				      }
				    }
				   return new String();
			   }
		};
		
		
		
		
		
		public static final Vector<String> reactions_columns;
		static{
			reactions_columns = new Vector<String>();
			for(int i = 1; ;i++) {
				String descr = ReactionsColumns.getDescriptionFromIndex(i);
				if(descr.length() <=0) break;
				reactions_columns.add(descr);
			}
		}
		
		public static enum ReactionsColumns {
			   NAME(1,"Name (opt)"), 
			   REACTION(2, "Reaction"),
			   TYPE(3,"Kinetic Type"), 
			   KINETIC_LAW(4, "Kinetic Law"),
			   EXPANDED(5,"   "), 
			   NOTES(6,"Notes");   
			          
			   public final int index;
			   public final String description;
			   
			   ReactionsColumns(int index, String descr) {
			              this.index = index;
			              this.description = descr;
			    }
			   
			   public static int getIndex(String descr){
				   if (descr != null) {
					      for (ReactionsColumns b : ReactionsColumns.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.index;
					      }
					    }
				   } 
				   return -1;
			   }
			   
			   public static String getDescriptionFromIndex(int index){
			      for (ReactionsColumns b : ReactionsColumns.values()) {
				        if (index == b.index) {
				          return b.description;
				      }
				    }
				   return new String();
			   }

			public String getDescription() {
				return description;
			}
		};
		
		
		public static enum ReactionType {
			   MASS_ACTION(0,"Mass Action", CFunction.MassAction), 
			   USER_DEFINED(1,"User Defined", CFunction.UserDefined),
			   PRE_DEFINED(2,"User Defined", CFunction.PreDefined),
			   FUNCTION(1,"User Defined", CFunction.Function);
		          
			          
			   public final int arrayIndex;
			   public final String description;
			   public final int copasiType;
			   
			   ReactionType(int index, String descr, int cType) {
			              this.arrayIndex = index;
			              this.description = descr;
			              this.copasiType = cType;
			    }
			   
			   public static int getIndex(String descr){
				   if (descr != null) {
					      for (ReactionType b : ReactionType.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.arrayIndex;
					      }
					    }
				   } 
				   return -100;
			   }
			   
			   public static String getDescriptionFromIndex(int index){
			      for (ReactionType b : ReactionType.values()) {
				        if (index == b.arrayIndex) {
				          return b.description;
				      }
				    }
				   return new String();
			   }
			   
			   public static String getDescriptionFromCopasiType(int ctype){
				      for (ReactionType b : ReactionType.values()) {
					        if (ctype == b.copasiType) {
					          return b.description;
					      }
					    }
					   return new String();
				   }
		
			   public static int getCopasiTypeFromIndex(int index){
				      for (ReactionType b : ReactionType.values()) {
					        if (index == b.arrayIndex) {
					          return b.copasiType;
					      }
					    }
					   return -100;
				   }
			   
			   public static int getCopasiTypeFromDescription(String description){
				      for (ReactionType b : ReactionType.values()) {
					        if (description.compareTo(b.description)==0) {
					          return b.copasiType;
					      }
					    }
					   return -100;
				   }

			public String getDescription() {
					return description;
			}
		};
		
		public static final Vector<String> reactionTypes;
		static {
			reactionTypes = new Vector<String>();
			reactionTypes.add(Constants.ReactionType.MASS_ACTION.description);
			reactionTypes.add(Constants.ReactionType.USER_DEFINED.description);
//			reactionTypes.add(Constants.ReactionType.PRE_DEFINED.description);
		}
		
		
		/*public static enum FunctionsType {
			   MASS_ACTION(0,"Mass Action", CFunction.MassAction), 
			   USER_DEFINED(1,"User Defined", CFunction.UserDefined),
			   PRE_DEFINED(2,"Predefined", CFunction.PreDefined),
			   FUNCTION(1,"User Defined", CFunction.Function),
			   WEIGHT(3,"Weight in SUM", Constants.WEIGHT_IN_SUM);
			   
		       public final int arrayIndex;
			   public final String description;
			   public final int copasiType;
			   
			   FunctionsType(int index, String descr, int cType) {
			              this.arrayIndex = index;
			              this.description = descr;
			              this.copasiType = cType;
			    }
			   
			   public static int getIndex(String descr){
				   if (descr != null) {
					      for (FunctionsType b : FunctionsType.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.arrayIndex;
					      }
					    }
				   } 
				   return -100;
			   }
			   
			   public static String getDescriptionFromIndex(int index){
			      for (FunctionsType b : FunctionsType.values()) {
				        if (index == b.arrayIndex) {
				          return b.description;
				      }
				    }
				   return new String();
			   }
			   
			   public static String getDescriptionFromCopasiType(int ctype){
				      for (FunctionsType b : FunctionsType.values()) {
					        if (ctype == b.copasiType) {
					          return b.description;
					      }
					    }
					   return new String();
				   }
		
			   public static int getCopasiTypeFromIndex(int index){
				      for (FunctionsType b : FunctionsType.values()) {
					        if (index == b.arrayIndex) {
					          return b.copasiType;
					      }
					    }
					   return -100;
				   }
			   
			   public static int getCopasiTypeFromDescription(String description){
				      for (FunctionsType b : FunctionsType.values()) {
					        if (description.compareTo(b.description)==0) {
					          return b.copasiType;
					      }
					    }
					   return -100;
				   }
		};
		
		public static final Vector functionTypes;
		static {
			functionTypes = new Vector();
			functionTypes.add(Constants.FunctionsType.MASS_ACTION.description);
			functionTypes.add(Constants.FunctionsType.USER_DEFINED.description);
			functionTypes.add(Constants.FunctionsType.PRE_DEFINED.description);
			functionTypes.add(Constants.FunctionsType.WEIGHT.description);
		}*/
		
		
		
		public static final Vector<String> species_columns;
		static{
			species_columns = new Vector<String>();
			for(int i = 1; ;i++) {
				String descr = SpeciesColumns.getDescriptionFromIndex(i);
				if(descr.length() <=0) break;
				species_columns.add(descr);
			}
		}
		
		public static enum SpeciesColumns {
			   NAME(1,"Name"), 
			   INITIAL_QUANTITY(2,"Initial quantity"),
			   TYPE(3,"Type"), 
			   COMPARTMENT(4,"Compartment"), 
			   EXPRESSION(5,"Expression"), 
			   NOTES(6,"Notes");   
			          
			   public final int index;
			   public final String description;
			   
			   SpeciesColumns(int index, String descr) {
			              this.index = index;
			              this.description = descr;
			    }
			   
			   public static int getIndex(String descr){
				   if (descr != null) {
					      for (SpeciesColumns b : SpeciesColumns.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.index;
					      }
					    }
				   } 
				   return -1;
			   }
			   
			   public static String getDescriptionFromIndex(int index){
			      for (SpeciesColumns b : SpeciesColumns.values()) {
				        if (index == b.index) {
				          return b.description;
				      }
				    }
				   return new String();
			   }

			public String getDescription() {
				
				return description;
			}
		};
		
		public static final Vector<String> deleteActions;
		static {
			deleteActions = new Vector<String>();
			deleteActions.add(Constants.DeleteActions.SELECT.description);
			deleteActions.add(Constants.DeleteActions.DELETE.description);
			deleteActions.add(Constants.DeleteActions.INCONSISTENT.description);
			//deleteActions.add(Constants.DeleteActions.EXCLUDE.description);
			deleteActions.add(Constants.DeleteActions.ASSIGN_NEW_VALUE.description);
		}
	   
		public static enum DeleteActions {
				SELECT(0,"(select one)"), 
			   DELETE(1,"Delete element"), 
			   INCONSISTENT(2,"Leave inconsistency"), 
			   ASSIGN_NEW_VALUE(3, "Replace", "Replace \"\" with ");
			 //  EXCLUDE(3,"Exclude parent from element expression");
			   
			   public final int index;
			   public final String description;
			   public final String custom_description;
			   
			   DeleteActions(int index, String descr) {
			              this.index = index;
			              this.description = descr;
			              this.custom_description = new String();
			    }
			   
			   DeleteActions(int index, String descr, String custom_descr) {
		              this.index = index;
		              this.description = descr;
		              this.custom_description = custom_descr;
		    }
			   
			   public static int getIndex(String descr){
				   if (descr != null) {
					      for (DeleteActions b : DeleteActions.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.index;
					      }
					    }
				   } 
				   return -1;
			   }
			   
			   public static String getDescriptionFromIndex(int index){
			      for (DeleteActions b : DeleteActions.values()) {
				        if (index == b.index) {
				          return b.description;
				      }
				    }
				   return new String();
			   }

			public String getDescription() {
			
				return description;
			}
			   
			   
		};
		
		
		public static enum DeleteColumns {
			   TREE_ELEMENT(0,"Element"), 
			   ELEMENT(1,"Where parent is used"), 
			   ACTION_TO_TAKE(2,"Action to take"),
			   NEW_VALUE(3,"New value");
			   
			   
			   public static int getNumColumns() {return 4;}
			   
			   public final int index;
			   public final String description;
			   
			   DeleteColumns(int index, String descr) {
			              this.index = index;
			              this.description = descr;
			    }
			   
			   public static int getIndex(String descr){
				   if (descr != null) {
					      for (DeleteColumns b : DeleteColumns.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.index;
					      }
					    }
				   } 
				   return -1;
			   }
			   
			   public static String getDescriptionFromIndex(int index){
			      for (DeleteColumns b : DeleteColumns.values()) {
				        if (index == b.index) {
				          return b.description;
				      }
				    }
				   return new String();
			   }
			   
			   
		};
		
		public static enum BooleanType {
			   TRUE(0,MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstants.TRUE_3].substring(1,MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstants.TRUE_3].length()-1), "T"), 
			   FALSE(1,MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstants.FALSE_3].substring(1,MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstants.FALSE_3].length()-1), "F");
			 //  TRUE_lower(0,"true", "T"),
			//   FALSE_lower(0,"false", "F");   
			          
			   public final int value;
			   public final String description;
			   public final String shortDescr;
			   
			   BooleanType(int index, String descr, String sh) {
			              this.value = index;
			              this.description = descr;
			              this.shortDescr = sh;
			    }
			   
			   public static int getIndex(String descr){
				   if (descr != null) {
					      for (BooleanType b : BooleanType.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.value;
					      }
					    }
				   } 
				   return -100;
			   }
			   
			   public static String getDescriptionFromIndex(int index){
			      for (BooleanType b : BooleanType.values()) {
				        if (index == b.value) {
				          return b.description;
				      }
				    }
				   return new String();
			   }
			   
			   public static String getDescriptionFromShortDescr(String sh){
				      for (BooleanType b : BooleanType.values()) {
					        if (sh.compareTo(b.shortDescr) == 0) {
					          return b.description;
					      }
					    }
					   return new String();
				   }
		
			   public static String getShortDescFromIndex(int index){
				      for (BooleanType b : BooleanType.values()) {
					        if (index == b.value) {
					          return b.shortDescr;
					      }
					    }
					   return new String();
				   }
			   
			   public static String getShortDescFromDescription(String description){
				      for (BooleanType b : BooleanType.values()) {
					        if (description.compareTo(b.description)==0) {
					          return b.shortDescr;
					      }
					    }
					   return new String();
				   }

			public static boolean isTrue(String string) {
				String test = MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstants.TRUE_1];
				test = test.substring(1,test.length()-1);
				if(test.compareTo(string)==0) return true;
				test = MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstants.TRUE_2];
				test = test.substring(1,test.length()-1);
				if(test.compareTo(string)==0) return true;
				test = MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstants.TRUE_3];
				test = test.substring(1,test.length()-1);
				if(test.compareTo(string)==0) return true;
				return false;
			}

			public static boolean isFalse(String string) {
				String test = MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstants.FALSE_1];
				test = test.substring(1,test.length()-1);
				if(test.compareTo(string)==0) return true;
				test = MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstants.FALSE_2];
				test = test.substring(1,test.length()-1);
				if(test.compareTo(string)==0) return true;
				test = MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstants.FALSE_3];
				test = test.substring(1,test.length()-1);
				if(test.compareTo(string)==0) return true;
				return false;
			}

			public String getDescription() {
				return description;
			}
		};
		
		public static enum SpeciesType {
			   FIXED(0,"Fixed", CMetab.FIXED), 
			   REACTIONS(1,"Reactions", CMetab.REACTIONS),
			   ASSIGNMENT(2,"Assignment", CMetab.ASSIGNMENT),
			   ODE(3,"ODE", CMetab.ODE),
			   MULTISTATE(4,"Multistate", Constants.MULTISTATE_TYPE),
			   COMPLEX(5,"Complex", Constants.COMPLEX_TYPE);
			   
			          
			   public final int arrayIndex;
			   public final String description;
			   public final int copasiType;
			   
			   SpeciesType(int index, String descr, int cType) {
			              this.arrayIndex = index;
			              this.description = descr;
			              this.copasiType = cType;
			    }
			   
			   public static int getIndex(String descr){
				   if (descr != null) {
					      for (SpeciesType b : SpeciesType.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.arrayIndex;
					      }
					    }
				   } 
				   return -100;
			   }
			   
			   public static String getDescriptionFromIndex(int index){
			      for (SpeciesType b : SpeciesType.values()) {
				        if (index == b.arrayIndex) {
				          return b.description;
				      }
				    }
				   return new String();
			   }
			   
			   public static String getDescriptionFromCopasiType(int ctype){
				      for (SpeciesType b : SpeciesType.values()) {
					        if (ctype == b.copasiType) {
					          return b.description;
					      }
					    }
					   return new String();
				   }
		
			   public static int getCopasiTypeFromIndex(int index){
				      for (SpeciesType b : SpeciesType.values()) {
					        if (index == b.arrayIndex) {
					          return b.copasiType;
					      }
					    }
					   return -100;
				   }
			   
			   public static int getCopasiTypeFromDescription(String description){
				      for (SpeciesType b : SpeciesType.values()) {
					        if (description.compareTo(b.description)==0) {
					          return b.copasiType;
					      }
					    }
					   return -100;
				   }

			public String getDescription() {
				return description;
			}
		};
		
		public static final Vector<String> speciesTypes;
		static {
			speciesTypes = new Vector<String>();
			speciesTypes.add(Constants.SpeciesType.FIXED.description);
			speciesTypes.add(Constants.SpeciesType.REACTIONS.description);
			speciesTypes.add(Constants.SpeciesType.ASSIGNMENT.description);
			speciesTypes.add(Constants.SpeciesType.ODE.description);
			speciesTypes.add(Constants.SpeciesType.MULTISTATE.description);
			//speciesTypes.add(Constants.SpeciesType.COMPLEX.description);
		}
		
		
		
		public static final Vector<String> events_columns;
		static{
			events_columns = new Vector<String>();
			for(int i = 1; ;i++) {
				String descr = EventsColumns.getDescriptionFromIndex(i);
				if(descr.length() <=0) break;
				events_columns.add(descr);
			}
		}
		
		public static enum EventsColumns {
			   NAME(1,"Name (opt)"), 
			   TRIGGER(2,"Trigger expression"),
			   ACTIONS(3,"Actions"), 
			   DELAY(4,"Delay"), 
			   DELAYCALC(5,"DlyCalc"), 
			   NOTES(6,"Notes"),
			   EXPAND_ACTION_ONVOLUME_TOSPECIES_C(7, "ExpAct"+MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstants.EXTENSION_CONC].substring(1,MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstants.EXTENSION_CONC].length()-1));   
			  // EXPAND_ACTION_ONVOLUME_TOSPECIES_P(8, "ExpAct"+MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstants.EXTENSION_PARTICLE].substring(1,MR_Expression_ParserConstants.tokenImage[MR_Expression_ParserConstants.EXTENSION_PARTICLE].length()-1));   
				          
			   public final int index;
			   public final String description;
			   
			   EventsColumns(int index, String descr) {
			              this.index = index;
			              this.description = descr;
			    }
			   
			   public static int getIndex(String descr){
				   if (descr != null) {
					      for (EventsColumns b : EventsColumns.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.index;
					      }
					    }
				   } 
				   return -1;
			   }
			   
			   public static String getDescriptionFromIndex(int index){
			      for (EventsColumns b : EventsColumns.values()) {
				        if (index == b.index) {
				          return b.description;
				      }
				    }
				   return new String();
			   }

			public String getDescription() {
				return description;
			}
		};
		
		
		
		public static enum FunctionParamType {
			   PARAMETER(0,"Global Quantity", CFunctionParameter.PARAMETER,"GLQ"), 
			   SUBSTRATE(1,"Substrate", CFunctionParameter.SUBSTRATE,"SUB"),
			   PRODUCT(2,"Product", CFunctionParameter.PRODUCT,"PROD"),
			   MODIFIER(3,"Modifier", CFunctionParameter.MODIFIER,"MOD"), 
			   VARIABLE(4,"Variable", CFunctionParameter.VARIABLE,"VAR"),
			   VOLUME(5,"Volume", CFunctionParameter.VOLUME,"VOL"),
			   SITE(6,"Site", Constants.SITE_FOR_WEIGHT_IN_SUM,"SITE"),
			   TIME(7,"Time", CFunctionParameter.TIME,"TIME"),
			   MISSING(-1,"Missing", -1,"MISSING"),
			    FUNCTION(-2,"Function", Constants.PARAM_TYPE_FUN,"FUN"), 
			    ASSIGNMENT_FLAG(-3,"AssignFlagForAutocompletion", Constants.ASSIGNMENT_FLAG_FOR_AUTOCOMPLETION,"VAR");
						   
						          
			   final int arrayIndex;
			   public final String description;
			   public final int copasiType;
			   public final String signatureType;
			   
			   
			   FunctionParamType(int index, String descr, int cType, String sign) {
			              this.arrayIndex = index;
			              this.description = descr;
			              this.copasiType = cType;
			              this.signatureType = sign;
			    }
			   
			   public static int getIndex(String descr){
				   if (descr != null) {
					      for (FunctionParamType b : FunctionParamType.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.arrayIndex;
					      }
					    }
				   } 
				   return -100;
			   }
			   
			   public static String getDescriptionFromIndex(int index){
			      for (FunctionParamType b : FunctionParamType.values()) {
				        if (index == b.arrayIndex) {
				          return b.description;
				      }
				    }
				   return new String();
			   }
			   
			   public static String getDescriptionFromCopasiType(int ctype){
				      for (FunctionParamType b : FunctionParamType.values()) {
					        if (ctype == b.copasiType) {
					          return b.description;
					      }
					    }
					   return new String();
				   }
		
			   public static int getCopasiTypeFromIndex(int index){
				      for (FunctionParamType b : FunctionParamType.values()) {
					        if (index == b.arrayIndex) {
					          return b.copasiType;
					      }
					    }
					   return -100;
				   }
			   
			   public static int getCopasiTypeFromDescription(String description){
				      for (FunctionParamType b : FunctionParamType.values()) {
					        if (description.compareTo(b.description)==0) {
					          return b.copasiType;
					      }
					    }
					   return -100;
				   }
			   
			   public static String getSignatureDescriptionFromCopasiType(int copasiType) {
				   for (FunctionParamType b : FunctionParamType.values()) {
				        if (copasiType == b.copasiType) {
				          return b.signatureType;
				      }
				    }
				   return new String();
				}
			   
			   public static int getCopasiTypeFromSignatureType(String sign) {
				   for (FunctionParamType b : FunctionParamType.values()) {
				        if (sign.compareTo(b.signatureType) == 0) {
				          return b.copasiType;
				      }
				    }
				   return Constants.FunctionParamType.MISSING.copasiType;
				   
				}
				
		};
		
		
		public static final Vector<String> paramTypes;
		static {
			paramTypes = new Vector<String>();
			paramTypes.add(Constants.FunctionParamType.PARAMETER.description);
			paramTypes.add(Constants.FunctionParamType.SUBSTRATE.description);
			paramTypes.add(Constants.FunctionParamType.PRODUCT.description);
			paramTypes.add(Constants.FunctionParamType.MODIFIER.description);
			paramTypes.add(Constants.FunctionParamType.VARIABLE.description);
			paramTypes.add(Constants.FunctionParamType.VOLUME.description);
			paramTypes.add(Constants.FunctionParamType.SITE.description);
		}
		
		public static final int MULTISTATE_TYPE = -127;
		public static final int COMPLEX_TYPE = -524;
		public static final int SITE_FOR_WEIGHT_IN_SUM = -376;
		public static final int ROLE_EXPRESSION = -603;
		public static final int PARAM_TYPE_FUN = -739;
		public static final int ASSIGNMENT_FLAG_FOR_AUTOCOMPLETION = -9875;
		
		public static final String DEFAULT_COMPARTMENT_NAME = new String("cell");
		public static final String DEFAULT_GLOBALQ_INITIAL_VALUE = new String("0.0");
		public static final String DEFAULT_SPECIES_INITIAL_VALUE = new String("0.0");
		public static final String DEFAULT_COMPARTMENT_INITIAL_VALUE =  new String("1.0");
		
		public static final String DEFAULT_MODEL_NAME = new String("NewModel");
		public static final String DEFAULT_SUFFIX_BACKWARD_REACTION = new String("_BKWD");
		public static final String DEFAULT_SUFFIX_COPASI_BACKWARD_REACTION = new String("(backward part)");
		public static final String DEFAULT_SUFFIX_COPASI_FORWARD_REACTION = new String("(forward part)");
		
		public static final String COPASI_REFERENCE_TRANS_CONCENTRATION = new String("Concentration");
		public static final String COPASI_REFERENCE_TRANS_PARTICLE_NUM = new String("ParticleNumber");
		public static final String COPASI_REFERENCE_TRANS_VALUE = new String("Value");
		public static final String COPASI_REFERENCE_TRANS_VOLUME = new String("Volume");
		public static final String COPASI_REFERENCE_INIT_VALUE = new String("InitialValue");
		public static final String COPASI_REFERENCE_INIT_VOLUME = new String("InitialVolume");
			public static final String COPASI_REFERENCE_RATE_VALUE = new String("Rate");
		//public static final String COPASI_REFERENCE_RATE_CONCENTRATION  = new String("Rate");
		public static final String COPASI_REFERENCE_INIT_CONCENTRATION  = new String("InitialConcentration");
		public static final String COPASI_REFERENCE_INIT_PARTICLE_NUM  = new String("InitialParticleNumber");
		public static final String COPASI_REFERENCE_RATE_PARTICLE_NUM   = new String("ParticleNumberRate");
		
		
		public static final String AUTOSAVE_TMP_PREFIX = new String("#_");
		public static final String AUTOSAVE_TMP_SUFFIX = new String("_autosaved");
		public static final String AUTOSAVE_SESSION_PREFIX = new String("_start_session_");
		public static final String AUTOSAVE_SESSION_SUFFIX = new String("_autosaved");
		public static final String AUTOSAVE_UNTITLED = new String("untitled");
		
		public final static String FILE_EXTENSION_MSMB = ".msmb";
		public final static String FILE_EXTENSION_COPASI = ".cps";
		public final static String FILE_EXTENSION_SBML = ".sbml";
		public final static String FILE_EXTENSION_XML = ".xml";
			
		
		public static final Vector<String> volumeUnits;
		static {
			volumeUnits = new Vector<String>();
			volumeUnits.add(Constants.UnitTypeVolume.FL.description);
			volumeUnits.add(Constants.UnitTypeVolume.PL.description);
			volumeUnits.add(Constants.UnitTypeVolume.NL.description);
			volumeUnits.add(Constants.UnitTypeVolume.MICROL.description);
			volumeUnits.add(Constants.UnitTypeVolume.ML.description);
			volumeUnits.add(Constants.UnitTypeVolume.L.description);
			volumeUnits.add(Constants.UnitTypeVolume.M3.description);
			volumeUnits.add(Constants.UnitTypeVolume.DIMENSIONLESS_VOL.description);
		}
		
		public static final Vector<String> timeUnits;
		static {
			timeUnits = new Vector<String>();
			timeUnits.add(Constants.UnitTypeTime.FS.description);
			timeUnits.add(Constants.UnitTypeTime.PS.description);
			timeUnits.add(Constants.UnitTypeTime.NS.description);
			timeUnits.add(Constants.UnitTypeTime.MICROS.description);
			timeUnits.add(Constants.UnitTypeTime.MS.description);
			timeUnits.add(Constants.UnitTypeTime.S.description);
			timeUnits.add(Constants.UnitTypeTime.MIN.description);
			timeUnits.add(Constants.UnitTypeTime.H.description);
			timeUnits.add(Constants.UnitTypeTime.D.description);
			timeUnits.add(Constants.UnitTypeTime.DIMENSIONLESS_TIME.description);
		}
		
		public static final Vector<String> quantityUnits;
		public static final String TOOL_NAME = new String("MSMB");
		public static final String TOOL_NAME_FULL = "MultiState Model Builder (MSMB)";
		
		public static final String RECENT_FILE_NAME = new String(TOOL_NAME+"_recents.cfg");
		public static final String PREFERENCES_FILE_NAME = new String(TOOL_NAME+"_preferences.cfg");
		public static final String MultistateBuilder_QUANTITIES_description = new String("Initial quantities");
		public static final int DELETE_JUST_ENTITIES = 5640;
		public static final int DELETE_RECURSIVELY_ALL = 4321;
		public static final int DELETE_CANCEL =1230;
		public static final int MERGE_SPECIES = 670;
		public static final int DELETE_SPECIES_AND_REDIRECT = 340;
		public static final int DUPLICATE_SPECIES_NAME = 320;
		public static final Color DEFAULT_COLOR_HIGHLIGHT = Color.YELLOW;
		public static final Color DEFAULT_COLOR_DEFAULTS = Color.MAGENTA;
		public static final Color DEFAULT_COLOR_ERRORS = Constants.vt_orange;
		public static final String PREFIX_FUN_4_REACTION_NAME = "function_4_reaction_";
		public static final String PREFIX_GLQ_4_REACTION_NAME = "globalQ_4_reaction_";
		public static final String NO_AUTOCOMPLETION_AVAILABLE = "No autocompletion available";
		
		static {
			quantityUnits = new Vector<String>();
			quantityUnits.add(Constants.UnitTypeQuantity.NUMBER.description);
			quantityUnits.add(Constants.UnitTypeQuantity.FMOL.description);
			quantityUnits.add(Constants.UnitTypeQuantity.PMOL.description);
			quantityUnits.add(Constants.UnitTypeQuantity.NMOL.description);
			quantityUnits.add(Constants.UnitTypeQuantity.MICROMOL.description);
			quantityUnits.add(Constants.UnitTypeQuantity.MMOL.description);
			quantityUnits.add(Constants.UnitTypeQuantity.MOL.description);
			quantityUnits.add(Constants.UnitTypeQuantity.DIMENSIONLESS_QUANTITY.description);
		}
		
		public static enum UnitTypeVolume {
			/*FL("fl", CUnit.fl), 
			ML("ml", CUnit.ml),
			PL("pl", CUnit.pl),
			NL("nl", CUnit.nl),
			MICROL("ml", CUnit.microl),
			L("l", CUnit.l),
			M3("m3",CUnit.m3),
			DIMENSIONLESS_VOL("dimensionlessVol", CUnit.dimensionlessVolume);*/
			FL("fl", CModel.fl), 
			ML("ml", CModel.ml),
			PL("pl", CModel.pl),
			NL("nl", CModel.nl),
			MICROL("ml", CModel.microl),
			L("l", CModel.l),
			M3("m3",CModel.m3),
			DIMENSIONLESS_VOL("dimensionlessVol", CModel.dimensionlessVolume);
			  public final String description;
			   public final int copasiType;
			   
			   UnitTypeVolume(String descr, int cType) {
				   this.description = descr;
			       this.copasiType = cType;
			   }
			   
			   
			   public static String getDescriptionFromCopasiType(int ctype){
				      for (UnitTypeVolume b :UnitTypeVolume.values()) {
					        if (ctype == b.copasiType) {
					          return b.description;
					      }
					    }
					   return new String();
				   }
		
			 
			   public static int getCopasiTypeFromDescription(String description){
				      for (UnitTypeVolume b : UnitTypeVolume.values()) {
					        if (description.compareTo(b.description)==0) {
					          return b.copasiType;
					      }
					    }
					   return -100;
				   }
			   
		};
		
		
			
		public static enum UnitTypeTime {	
			/*FS("fs", CUnit.fs), 
			MS("ms", CUnit.ms),
			PS("ps", CUnit.ps),
			NS("ns", CUnit.ns),
			MICROS("micros", CUnit.micros),
			S("s", CUnit.s),
			MIN("min",CUnit.min),
			H("h",CUnit.h),
			D("d",CUnit.d),
			DIMENSIONLESS_TIME("dimensionlessTime", CUnit.dimensionlessTime);*/
			FS("fs", CModel.fs), 
			MS("ms", CModel.ms),
			PS("ps", CModel.ps),
			NS("ns", CModel.ns),
			MICROS("micros", CModel.micros),
			S("s", CModel.s),
			MIN("min",CModel.min),
			H("h",CModel.h),
			D("d",CModel.d),
			DIMENSIONLESS_TIME("dimensionlessTime", CModel.dimensionlessTime);
			  public final String description;
			   public final int copasiType;
			   
			   UnitTypeTime(String descr, int cType) {
				   this.description = descr;
			       this.copasiType = cType;
			   }
			   
			   
			   public static String getDescriptionFromCopasiType(int ctype){
				      for (UnitTypeTime b : UnitTypeTime.values()) {
					        if (ctype == b.copasiType) {
					          return b.description;
					      }
					    }
					   return new String();
				   }
		
			 
			   public static int getCopasiTypeFromDescription(String description){
				      for (UnitTypeTime b : UnitTypeTime.values()) {
					        if (description.compareTo(b.description)==0) {
					          return b.copasiType;
					      }
					    }
					   return -100;
				   }
			   
		};
		
		
		public static enum UnitTypeQuantity {
			/*NUMBER("#", CUnit.number), 
			FMOL("fmol", CUnit.fMol), 
			MMOL("mmol", CUnit.mMol),
			PMOL("pmol", CUnit.pMol),
			NMOL("nmol", CUnit.nMol),
			MICROMOL("micromol", CUnit.microMol),
			MOL("mol", CUnit.Mol),
			DIMENSIONLESS_QUANTITY("dimensionlessQuantity", CUnit.dimensionlessQuantity);*/
			NUMBER("#", CModel.number), 
			FMOL("fmol", CModel.fMol), 
			MMOL("mmol", CModel.mMol),
			PMOL("pmol", CModel.pMol),
			NMOL("nmol", CModel.nMol),
			MICROMOL("micromol", CModel.microMol),
			MOL("mol", CModel.Mol),
			DIMENSIONLESS_QUANTITY("dimensionlessQuantity", CModel.dimensionlessQuantity);
			 	   
			   public final String description;
			   public final int copasiType;
			   
			   UnitTypeQuantity(String descr, int cType) {
				   this.description = descr;
			       this.copasiType = cType;
			   }
			   
			   
			   public static String getDescriptionFromCopasiType(int ctype){
				      for (UnitTypeQuantity b : UnitTypeQuantity.values()) {
					        if (ctype == b.copasiType) {
					          return b.description;
					      }
					    }
					   return new String();
				   }
		
			 
			   public static int getCopasiTypeFromDescription(String description){
				      for (UnitTypeQuantity b : UnitTypeQuantity.values()) {
					        if (description.compareTo(b.description)==0) {
					          return b.copasiType;
					      }
					    }
					   return -100;
				   }
			   
		};
		
		
		
		/*public static enum BuiltInMathematicalFunctions {
			LOG("log"),
			EXP("exp");
			public final String description;
			   
			BuiltInMathematicalFunctions(String descr) {
			         this.description = descr;
			 }
			
			 public static boolean contains(String descr) {
				   for (BuiltInMathematicalFunctions b : BuiltInMathematicalFunctions.values()) {
				        if (descr.compareToIgnoreCase(b.description) == 0) {
				          return true;
				      }
				    }
				   return false;
				   
				}
		}*/
		
		
		public static enum TitlesTabs {
			   REACTIONS("Reactions", 0), 
			   SPECIES("Species", 1), 
			   GLOBALQ("Global quantities", 2),
			   FUNCTIONS("Functions", 3),
			   EVENTS("Events",4),
			   COMPARTMENTS("Compartments",5),
			   EQUATIONS("Equations",6),
			   DEBUG("Model properties",7) ,
			   BUILTINFUNCTIONS("BuiltInFunctions", 8);
				 
			   private final static int numTabMSMB = 8;
				
			   private final String descriptionAC = "Module variables";
			   private final String description;
			   public final int index;
			   
			   TitlesTabs(String descr, int index) {
			         this.description = descr;
			         this.index = index;
			   }
			   
			   public String getDescription() {
				   if(MainGui.fromInterface && description.equals(GLOBALQ.description)) return descriptionAC;
				   else return description;
			   }
			   
			   public static int getIndexFromDescription(String descr){
				   if (descr != null) {
					   	      for (TitlesTabs b : TitlesTabs.values()) {
					        	if (descr.compareTo(b.getDescription())==0) {
					        			return b.index;
					        	} 
					     
					    }
				   } 
				   return -1;
			   }

			   
			   public static String getDescriptionFromIndex(int index){
				        for (TitlesTabs b : TitlesTabs.values()) {
					        if (index == b.index) {
					          return new String(b.getDescription());
					      }
					} 
				   return new String();
			   }
			public static int getNumTabs() {
				return numTabMSMB;
			}

			public static boolean isMSMBtab(int index) {
				if(index == REACTIONS.index) return true;
				if(index == SPECIES.index) return true;
				if(index == GLOBALQ.index) return true;
				if(index == FUNCTIONS.index) return true;
				if(index == EVENTS.index) return true;
				if(index == COMPARTMENTS.index) return true;
				if(index == EQUATIONS.index) return true;
				if(index == DEBUG.index) return true;
				return false;
			}
			   
		}
		
		public static String NOT_EDITABLE_VIEW = "EXPRESSION_FROM_NOT_EDITABLE_VIEW";
		public static enum Views {
			   EDITABLE("Editable view", 0), 
			   EXPANDED("Expanded view", 1), 
			   EXPANDED_ALL("Expand all elements", 2), 
			   COMPRESSED("Compressed view", 3),
			   CUSTOM("Custom view...", 4), 
			   CURRENT_AS_EDITABLE("Set the current view as the editable one", 5);
			 
			   public final String description;
			   public final int index;
			   
			   Views(String descr, int index) {
			         this.description = descr;
			         this.index = index;
			   }
			   
			   public static int getIndexFromDescription(String descr){
				   if (descr != null) {
					      for (Views b : Views.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.index;
					      }
					    }
				   } 
				   return -1;
			   }
			   
			   
			   public static String getDescriptionFromIndex(int index){
				   if (index >=0 ) {
					      for (Views b : Views.values()) {
					        if (index == b.index) {
					          return b.description;
					      }
					    }
				   } 
				   return null;
			   }

			public String getDescription() {
			
				return description;
			}
				   
		}
		
		
		public static enum ProgressBar {
			   LOADING_CPS(10,"Creating model..."), 
			   LOADING_GLOBQ__CPS(12,"   GlobalQ..."), 
			   LOADING_COMP_CPS(15,"   Compartments..."), 
			   LOADING_SPECIES_CPS(20,"   Species..."), 
			   LOADING_FUNCTIONS_CPS(30,"   Functions..."), 
			   LOADING_REACTIONS_CPS(32,"   Reactions..."), 
			   LOADING_EVENTS_CPS(38,"   Events..."), 
			   COMPILING_CPS(40, "Compiling model..."),
			   UPDATING_CPS(50,"Updating model..."), 
			   SAVING_CPS(60, "Saving model..."),
			   CLEARING_CPS(80,"Clearing..."), 
			   END(100,"Finished!");   
			          
			   public final int progress;
			   public final String description;
			   
			   ProgressBar(int index, String descr) {
			              this.progress = index;
			              this.description = descr;
			    }
			   
			   public static int getProgress(String descr){
				   if (descr != null) {
					      for (ProgressBar b : ProgressBar.values()) {
					        if (descr.compareTo(b.description)==0) {
					          return b.progress;
					      }
					    }
				   } 
				   return -1;
			   }
			   
			   public static String getDescriptionFromProgress(int index){
			      for (ProgressBar b : ProgressBar.values()) {
				        if (index == b.progress) {
				          return b.description;
				      }
				    }
				   return new String();
			   }
		};
		
		
		
		
		public static enum Preferences {
			SEPARATOR("%"), 
			AUTOCOMPLETE("Autocomplete"), 
			POPUP_AUTOCOMPLETE("PopUpAutocomplete"),
			CHECKED("on"),
			UNCHECKED("off"),
			RENAMING("Renaming"),
			RENAME_AUTO("Auto"),
			RENAME_CUSTOM("Custom"),
			RENAME_NONE("None"),
			COMP_NAME("DefaultCompartmentName"),
			INITIAL_COMP_SIZE("DefaultCompartmentInitialSize"),
			INITIAL_SPECIES_VALUE("DefaultSpeciesInitialQuantity"),
			INITIAL_GLOBALQ_VALUE("DefaultGlobalQInitialValue"), 
			SHOW_ALL_FUNCTIONS("ShowAllAvailableFunctions"), 
			COLOR_DEFAULTS("BorderColorDefaults"), 
			COLOR_MAJOR("BackgroundColorMajor"), 
			COLOR_MINOR("BackgroundColorMinor"), 
			COLOR_HIGHLIGHT("BackgroundColorHighlight"), 
			FONT_SIZE("FontSize"), 
			AUTOSAVE_PATH("AutosavePath"), 
			AUTOSAVE_TIME("AutosaveTimeMin"), 
			//AUTOCOMPLETION_DELAY("AutocompletionDelay"),
			AUTOSAVE_ACTIVE("AutosaveActive");
			
			public final String description;
			   
			Preferences(String descr) {
				   this.description = descr;
		   }

			public String getDescription() {
				
				return description;
			}
		};
		
		     // A DefaultCompletionProvider is the simplest concrete implementation
		      // of CompletionProvider. This provider has no understanding of
		      // language semantics. It simply checks the text entered up to the
		      // caret position for a match against known completions. This is all
		      // that is needed in the majority of cases.
		 public static   DefaultCompletionProvider provider = new DefaultCompletionProvider(){
				@Override
				protected boolean isValidChar(char arg0) {
					return super.isValidChar(arg0)|| arg0==' ';
				}
			
				
			};
		public static String MULTISTATE_TITLE_TABLE_PDF = new String("Multistate species");
		
		 static{
		      // A BasicCompletion is just
		      // a straightforward word completion.
		      provider.addCompletion(new BasicCompletion(provider, "c", "concentrations","<html><body>concentrations</body></html>"));
		      provider.addCompletion(new BasicCompletion(provider, "p"));
		      provider.addCompletion(new BasicCompletion(provider, "sp"));
		      provider.addCompletion(new BasicCompletion(provider, "cmp"));      
		      
		 }
		  
	
}
