package msmb.gui;

import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import msmb.model.Function;
import msmb.model.MultiModel;
import msmb.utility.CellParsers;
import msmb.utility.Constants;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;

public class AutoCompletion_MSMB extends AutoCompletion{
	MultiModel multiModel = null;
	
	public AutoCompletion_MSMB(CompletionProvider arg0, MultiModel multiModel) {	
		super(arg0);
		this.multiModel = multiModel;
	}

	
	@Override
	protected void insertCompletion(Completion c) {
		JTextComponent textComp = getTextComponent();
		textComp.replaceSelection(""); // to delete the part selected because part of a signature
		super.insertCompletion(c);
	}

	
	@Override
	protected String getReplacementText(Completion c, Document doc, int start, int len) { // what happens after something is selected: I can manipulate that string
			    
			    String text = super.getReplacementText(c, doc, start, len);
			    if(text.compareTo(Constants.NO_AUTOCOMPLETION_AVAILABLE)==0) {
			    	return new String();
			    }
			    
			    text = CellParsers.cleanName(text);
			
			    try {
					Function f = multiModel.funDB.getFunctionByName(text);
					if(f == null) {
						f = multiModel.funDB.getBuiltInFunctionByName(text);
						if(f!= null) text = f.printCompleteSignature();
					}
					if(f!= null) text = f.printCompleteSignature();
					
					if(start > 0) {
					    try {
							String maybeQuote = doc.getText(start-1, 1);
							if(maybeQuote.compareTo("\"")==0 && text.startsWith("\"")) text = text.substring(1);
						} catch (BadLocationException e1) {
							if (MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e1.printStackTrace();
						}
					}
				
			} catch (Exception e) {
				if (MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
			}
            return text;
             
        
		}
}
