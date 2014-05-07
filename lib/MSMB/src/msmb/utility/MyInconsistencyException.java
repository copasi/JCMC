package msmb.utility;

import java.io.IOException;

public class MyInconsistencyException extends IOException {
   
    private int column;
	private Throwable cause;
	private int row;

    public MyInconsistencyException(int column, String message) {
    	super(message);
      
       this.column = column;
    }

    public MyInconsistencyException(int column, String message, Throwable cause) {
		super(message);
	    this.column = column;
	    this.cause = cause;
	}

    public MyInconsistencyException(int row, int column, String message, Throwable cause) {
		super(message);
	    this.column = column;
	    this.cause = cause;
	    this.row = row;
	}

    public int getColumn() {
    	return this.column;
    }
	
	@Override
	public synchronized Throwable getCause() {
		return this.cause;
	}

	public int getRow() {
		return this.row;
	}

  
}
