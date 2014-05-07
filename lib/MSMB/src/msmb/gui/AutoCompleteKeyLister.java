package  msmb.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

import msmb.model.MultiModel;
import msmb.utility.AutocompleteDB;
import msmb.utility.CellParsers;
import msmb.utility.Constants;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;

import  msmb.parsers.mathExpression.MR_Expression_Parser;
import  msmb.parsers.mathExpression.MR_Expression_ParserConstantsNOQUOTES;
import  msmb.parsers.mathExpression.syntaxtree.CompleteExpression;
import  msmb.parsers.mathExpression.visitor.GetElementBeforeSpecialExtension;

public class AutoCompleteKeyLister implements KeyListener, ActionListener {
	AutoCompletion autoCompletion = null;
	AutocompleteDB autocomDB = null;
	boolean isInitialExpression = false;
	boolean manuallyTriggered = false;
	MultiModel multiModel = null;
	//Timer timer = null;
	private JTextComponent source;
	
	//public void stopTimerAutocompletion() {	timer.stop();}
	
	public AutoCompleteKeyLister(AutoCompletion ac, MultiModel mm, boolean isInitialExpr) { 
		this.autoCompletion = ac; 
		multiModel = mm;
		autocomDB = new AutocompleteDB(multiModel);
		isInitialExpression = isInitialExpr;
		int mask = InputEvent.CTRL_MASK;
		
		//timer = new Timer(200, this);
		//timer.setRepeats(false);
		
		//ac.setTriggerKey(KeyStroke.getKeyStroke(KeyEvent.VK_H, mask));
		
	}

	
	
	public void doGenericAutocompletion() {	
		DefaultCompletionProvider provider = null;
		//Vector<Vector<String>> completions = autocomDB.getAllDefinedFunctionsAutocompletion();
		autoCompletion.hideChildWindows();
		
		Vector<Vector<String>> completions = autocomDB.getAutocompletionFromContext();
		if(completions!= null) {
			provider = new DefaultCompletionProvider(){
				@Override
				public String getAlreadyEnteredText(JTextComponent comp) {
					if(comp.getSelectedText()!= null && AutocompleteDB.selectedTextIsSignatureType(comp.getSelectedText())) {
						return new String();
					}
					else return super.getAlreadyEnteredText(comp);
			
				}
			};
			if(completions.size() == 0) {
				String signature = Constants.NO_AUTOCOMPLETION_AVAILABLE;
				String longDescr ="No autocompletion elements of type "+Constants.FunctionParamType.getSignatureDescriptionFromCopasiType(MainGui.autocompletionContext)+" are available";
				provider.addCompletion(new BasicCompletion(provider, signature ,null, longDescr)); 
			}
			else {
				for(int i = 0; i<completions.size(); i++) {
					Vector<String> el = completions.get(i);
					String signature = el.get(0);
					String longDescr = el.get(1);
					provider.addCompletion(new BasicCompletion(provider, signature ,null, longDescr)); 
				}
			}
		}
		
		if(provider!= null) { 
			autoCompletion.setCompletionProvider(provider);
			autoCompletion.doCompletion();
		}
	}
	
	
	public void applyBetweenQuotesCompletion(final int quotedStartingAt, final String prevText) {	
		DefaultCompletionProvider provider = null;
		
		Vector<Vector<String>> completions = autocomDB.getAllDefinedFunctions();
		
		if(completions!= null) {
			provider = new DefaultCompletionProvider() {
				@Override
				protected boolean isValidChar(char arg0) {
					return (super.isValidChar(arg0)	|| CellParsers.isValidCharacterBetweenQuotes(arg0)
							);
				}
				
				public String getAlreadyEnteredText(JTextComponent comp) {
					
					Document doc = comp.getDocument();

					int dot = comp.getCaretPosition();
					Element root = doc.getDefaultRootElement();
					int index = root.getElementIndex(dot);
					Element elem = root.getElement(index);
					int start = elem.getStartOffset();
					int len = dot-start;
					try {
						doc.getText(start, len, seg);
					} catch (BadLocationException ble) {
						ble.printStackTrace();
						return EMPTY_STRING;
					}

					int segEnd = seg.offset + len;
					start = segEnd - 1;
					while (start>=seg.offset && isValidChar(seg.array[start]) && start >= quotedStartingAt) { 
											//added the last condition to stop once i find the quote
						start--;
					}
					start++;

					len = segEnd - start;
						return len==0 ? EMPTY_STRING : new String(seg.array, start, len);

				}

			};
			
			
			
			for(int i = 0; i<completions.size(); i++) {
					Vector<String> el = completions.get(i);
					String signature = el.get(0);
					String longDescr = el.get(1);
					provider.addCompletion(new BasicCompletion(provider, signature ,null, longDescr)); 
				
			}
			
		}
		
		if(provider!= null) { 
			autoCompletion.setCompletionProvider(provider);
			autoCompletion.doCompletion();
		}
	}
	
	/*private void getFunctionAutocompletion(String beginning) {
		if(beginning.toString().trim().endsWith("\"")) {
				beginning = beginning.toString().trim().substring(0,beginning.toString().trim().length()-1);
		}
		DefaultCompletionProvider provider = null;
		Vector<Vector<String>> completions = autocomDB.getAllDefinedFunctionsAutocompletion();
		if(completions!= null) {
			provider = new DefaultCompletionProvider();
			for(int i = 0; i<completions.size(); i++) {
				
					Vector<String> el = completions.get(i);
					String signature = el.get(0);
					String longDescr = el.get(1);
					if(signature.toLowerCase().startsWith(beginning.toLowerCase())) provider.addCompletion(new BasicCompletion(provider, signature ,null, longDescr)); 
				
			}
		}
		
		if(provider!= null) { 
			autoCompletion.setCompletionProvider(provider);
			autoCompletion.doCompletion();
		}
		
	}*/
	
	
	@Override
	public void keyReleased(KeyEvent e) {
		
		int keyCode = e.getKeyCode();
		if(keyCode == KeyEvent.VK_ESCAPE) {
			return;
		}
		source = (JTextComponent)e.getSource();
		
		if ((e.getModifiers() & InputEvent.CTRL_MASK) != 0 && (keyCode == KeyEvent.VK_H) ) {
			manuallyTriggered = true;
			applyAutocompletion();
			return;
		}	
		
	  
	//	timer.stop();
	//	timer.setInitialDelay(MainGui.delayAutocompletion);
	//	timer.start();
	}
	
	
	



	private String getLastElement(String subString) throws Exception {
		boolean endWithDot = false;
		if(subString.endsWith(".")) {
			endWithDot = true;
			subString = subString.substring(0,subString.length()-1);
		}
		String ret = new String();
		subString += MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MY_SPECIAL_EXTENSION);
		InputStream is = new ByteArrayInputStream(subString.getBytes("UTF-8"));
		MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
		CompleteExpression root = parser.CompleteExpression();
		GetElementBeforeSpecialExtension name = new GetElementBeforeSpecialExtension();
		root.accept(name);
		ret  = name.getElementName();
		if(endWithDot) ret += ".";
		return ret;
	}
	
	
	
	@Override
	public void keyPressed(KeyEvent e) {
		//timer.stop();
	}
	@Override
	public void keyTyped(KeyEvent e) {}



	@Override
	public void actionPerformed(ActionEvent e) {
		if(manuallyTriggered) return;
		manuallyTriggered = false;
		applyAutocompletion();
	}

	private void applyAutocompletion() {
		String text = source.getText();
		int caret = source.getCaretPosition();
		
		if(caret>0) {
			char prev_char = ' ';
			if(caret > 0) prev_char = text.charAt(caret-1);
			   String prevText = text.substring(0,caret);
						/*char trigger = '.';
				if(prev_char==' ') trigger = '%';
			   
			   if(!prevText.endsWith(new String()+trigger)) {
					String lastPart = null;
					if(testo.length() >= caret+1) lastPart=testo.substring(caret+1,testo.length());
					String complete = prevText + trigger;
					if(lastPart!=null) complete+= lastPart;
					source.setText(complete);
				}*/
			   
				int numOfPrevQuotes = prevText.split("\"").length-1; //if this is odd it means that the autocompletion is between quotes 
				DefaultCompletionProvider provider = null;
				if(numOfPrevQuotes%2==0) {
					try {
						 String lastElement = getLastElement(prevText);
						 provider =  autocomDB.getDefaultCompletionProvider(prev_char,lastElement,isInitialExpression); 
					} catch (Exception ex) {
						//ex.printStackTrace();
						provider = null;
						doGenericAutocompletion();
					} finally {
						if(provider!= null) { 
							autoCompletion.setCompletionProvider(provider);
							
							autoCompletion.doCompletion();
						} 
					}
				} else { // between quotes
					try {
						int initialQuotedAt = prevText.lastIndexOf("\"");
						applyBetweenQuotesCompletion(initialQuotedAt+1, prevText);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
	} else {
		doGenericAutocompletion();
	}
	}
}