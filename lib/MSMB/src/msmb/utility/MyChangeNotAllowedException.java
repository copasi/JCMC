package msmb.utility;

import java.io.IOException;




public class MyChangeNotAllowedException extends IOException {
  
    private String prevValue;
    private int column;

    public MyChangeNotAllowedException(int column, String previousValue, String message) {
    	super(message);
    
       this.column = column;
       this.prevValue = previousValue;
    }

    public int getColumn() {
    	return this.column;
    }

   
    public String getPreviousValue() {
    	return this.prevValue;
    }
    
    public String setPreviousValue(String previousValue) {
    	return this.prevValue = previousValue;
    }
}
