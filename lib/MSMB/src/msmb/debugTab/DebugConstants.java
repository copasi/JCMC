package  msmb.debugTab;

public class DebugConstants {
	
	public static final String SAMENAME_MESSAGE = "Entites with the same name: ";
	
	public static enum PriorityType {
		   DEFAULTS(0.0,"Defaults additions"), 
		   MINOR(1.0,"Minor issues"),
		   //SIMILARITY(1.2,"Similarity between names"),
		   MINOR_IMPORT_ISSUES(1.1, "Issues at import"),
		   MINOR_EMPTY(1.2, "Important missing definitions"),
		   DUPLICATES(1.3,"Duplicates entities"),
		   MAJOR(2.0,"Major issues"),
		   PARSING(2.1,"Parsing errors"),
		   INCONSISTENCIES(2.2,"Inconsistencies"),
		   MISSING(2.3, "Missing definitions"), 
		   EMPTY(2.4, "Empty field");
		   			          
		   public final double priorityCode;
		   public final String description;
		   
		   PriorityType(double index, String descr) {
		              this.priorityCode = index;
		              this.description = descr;
		    }
		   
		   public static double getIndex(String descr){
			   if (descr != null) {
				      for (PriorityType b : PriorityType.values()) {
				        if (descr.compareTo(b.description)==0) {
				          return b.priorityCode;     }			    
				        }
			   } 
			   return -100.0;
		   }
		   
		   public static String getDescriptionFromIndex(double index){
		      for (PriorityType b : PriorityType.values()) {
			        if (index == b.priorityCode) {
			          return b.description;      }
			    }
			   return new String();
		   }

		public String getDescription() {
		
			return description;
		}
	}


}
