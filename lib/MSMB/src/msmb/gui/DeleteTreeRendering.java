package  msmb.gui;

import msmb.utility.Constants;



public class DeleteTreeRendering {
    
    public static void configureColumnFactory(CustomColumnFactory factory, Class<?> resourceBase) {
    	
    	// add hints for column sizing
        factory.addPrototypeValue(Constants.DeleteColumns.TREE_ELEMENT.description, "....................................");
        factory.addPrototypeValue(Constants.DeleteColumns.ELEMENT.description, "....................");
        factory.addPrototypeValue(Constants.DeleteColumns.ACTION_TO_TAKE.description,".................");
        factory.addPrototypeValue(Constants.DeleteColumns.NEW_VALUE.description,".................");
        
         
     }
}

