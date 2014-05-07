package  msmb.gui;


import java.awt.EventQueue;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import msmb.utility.Constants;


import msmb.debugTab.DebugConstants.PriorityType;

public class RecordAutosave {
	
	
	Timer myGlobalTimer = null;
		MainGui mainGui = null;
		private String path;
		private String baseName;
		private String outputFileCompleteName = new String();
		
	public String getOutputFileCompleteName() {
			return outputFileCompleteName;
		}


	public RecordAutosave(MainGui mainGui) {
		this.mainGui = mainGui;
	}
	
	
	public void startAutosave() {
		if(myGlobalTimer != null) return;
		if(myGlobalTimer == null) myGlobalTimer = new Timer();
		path = mainGui.getAutosaveDirectory();
		baseName = mainGui.getAutosaveBaseName();
		final Runnable doUpdateCursor = new Runnable() {
				    public void run() {
			    	  try {
			    		  outputFileCompleteName = path+Constants.AUTOSAVE_TMP_PREFIX +baseName+Constants.AUTOSAVE_SESSION_SUFFIX+Constants.FILE_EXTENSION_MSMB;
			    		  File outputfile = new File(outputFileCompleteName);
			    		  ExportMultistateFormat.setFile(outputfile);
				          ExportMultistateFormat.export_MSMB_format(false);
				          
					} catch (Exception e) {
						if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES)
							e.printStackTrace();
					}

			    }
				    
			};
			

			TimerTask task = new TimerTask() {
			    public void run() {
			    	EventQueue.invokeLater(doUpdateCursor);
			    }
			};
		myGlobalTimer.schedule(task, 0, mainGui.getAutosaveTime());
	}
	
	public void stopAutosave() {
		if(myGlobalTimer != null) myGlobalTimer.cancel();
		myGlobalTimer = null;
	}

public String getPath() {
		
		return path;
	}
	
}


