package  msmb.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import javax.swing.DefaultCellEditor;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import msmb.utility.CellParsers;
import msmb.utility.Constants;

public class ExpressionsCellEditor  extends DefaultCellEditor {
	
	
	private static final long serialVersionUID = 1L;
	HashSet <String> type_parameter_string_copied_to_clipboard = new HashSet<String>();
	boolean selected = false;
	private JMenuItem pasteSignature;
	//private boolean showPopup;
	private JPopupMenu popupMenu;
	Font customFont = null;
    public Font getCustomFont() {return customFont;	}
	public void setCustomFont(Font customFont) {	
		this.customFont = customFont;	
	}
	
	public ExpressionsCellEditor(JTextField textField) {
		super(textField);
		popupMenu = new JPopupMenu();
		pasteSignature = new JMenuItem("Paste signature from Clipboard");
		
       	// pasteSignature.addActionListener(getActionForKeyStroke(KeyStroke.getKeyStroke("control V")));
       	pasteSignature.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						pasteText();
						    
					}
				});
		    popupMenu.add(pasteSignature);
		
		KeyListener keyListener = new KeyListener() {
		      public void keyPressed(final KeyEvent keyEvent) { 
		    	//  if(keyEvent.getKeyCode() ==KeyEvent.VK_LEFT || keyEvent.getKeyCode() ==KeyEvent.VK_RIGHT) { 
		    	  if(keyEvent.getKeyCode() !=KeyEvent.CTRL_DOWN_MASK) { 
		    		  SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							checkPositionAndSelect(keyEvent);
							
						}

						
		    		  }); 
		    	  }
		      }
		      public void keyReleased(KeyEvent keyEvent) {  }
		      public void keyTyped(KeyEvent keyEvent) {	}
		    
		    };
		   textField.addKeyListener(keyListener);
		   
		  MouseListener mouseListener = new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopUpPaste(e);
				} 
			}
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopUpPaste(e);
				} 
			}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(final MouseEvent e) {	 
				if(MainGui.displayTablesUneditable) return;
                
				if (e.isPopupTrigger()) {
	        		showPopUpPaste(e);
				} else {
				SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					checkPositionAndSelect(e);
				}
    		  });
				}
			}
		};
		
		  textField.addMouseListener(mouseListener);
		  
		  textField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK), "none");
			
		   
	}
	
	
	
	private void pasteText() {
		  String result = "";
		    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		    Transferable contents = clipboard.getContents(null);
		    boolean hasTransferableText =
		      (contents != null) &&
		      contents.isDataFlavorSupported(DataFlavor.stringFlavor)
		    ;
		    if ( hasTransferableText ) {
		      try {
		        result = (String)contents.getTransferData(DataFlavor.stringFlavor);
		  	  final JTextField source = (JTextField) this.getComponent();
			    source.getDocument().insertString(source.getCaretPosition(), result, null);
		      }
		        catch (Exception ex) {
		      	        if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
		      }
		    }
	}
	
	private void showPopUpPaste(MouseEvent e) {
		  if(	MainGui.copiedSignature==true) {
        	  final JTextField source = ((JTextField)e.getSource());
			 popupMenu.show(source, e.getX(), e.getY());
	       }
		  
	}
	
	  private void checkPositionAndSelect(InputEvent event) {
		  final JTextField source = ((JTextField)event.getSource());
		  
		String current = source.getText();
		  int cursor  = source.getCaretPosition();
		  MainGui.updateAutocompletionContext(current, cursor);
		 if(current.length()==0 || cursor==current.length()) return;
		  int startToken = 0;
		  int endToken = 0;
		  
		  
		  if(current.charAt(cursor)=='(' || current.charAt(cursor)==',') {
				 if(current.charAt(cursor-1)!='\\'){
					 if(event instanceof KeyEvent && ((KeyEvent)event).getKeyCode()==KeyEvent.VK_LEFT){
						 startToken = -1;
					 } else {
						 startToken = cursor;
					 }
				 } 
		  }
		  
		  if(current.charAt(cursor)==')' ) {
				 if(current.charAt(cursor-1)!='\\'){
					 if(event instanceof KeyEvent && ((KeyEvent)event).getKeyCode()==KeyEvent.VK_RIGHT){
						 endToken = -1;
					 } else {
						 endToken = cursor;
					 }
				 } 
		  }
		  
		  if(startToken!=-1) {
			  for(int i = cursor; i > 1; i--) {
				  if(current.charAt(i)=='(' || current.charAt(i)==','){
					  if(current.charAt(i-1)!='\\'){
							 startToken = i+1;
							 break; 
						 }
				 } else {
					 if(current.charAt(i)=='+' ||  current.charAt(i)=='-' || current.charAt(i)=='*' || current.charAt(i)=='/') {
						 if(current.charAt(i-1)!='\\'){
							 startToken = -1;
						 	 break;
						 }
					 }
				 }
				
			 }
		  }
		 if(endToken !=-1) {
			 for(int i = cursor; i < current.length(); i++) {
				  if(current.charAt(i)==')' || current.charAt(i)==',' || current.charAt(i)=='('){
					  if(i>1 && current.charAt(i-1)!='\\'){
						  	endToken = i;
							 break; 
						 }
				 } 
				 else {
					 if(current.charAt(i)=='+' ||  current.charAt(i)=='-' || current.charAt(i)=='*' || current.charAt(i)=='/') {
						 if(current.charAt(i-1)!='\\'){
							 endToken = -1;
							 break;
						 }
					 }
				 }
			 }
		 }
		 /*System.out.println("-----------------------------------");
		 System.out.println("start:"+startToken);
		 System.out.println("endToken:"+endToken);
		 System.out.println("cursor:"+cursor);
		 for(int i = 0; i < current.length(); i++){
			 System.out.println(i+":"+current.charAt(i));
		 }
		 System.out.println("-----------------------------------");*/
		 if(startToken != -1 && endToken != -1 ) {
			 String substring = new String();
			if( startToken<endToken)   substring = current.substring(startToken,endToken);
			else   substring = current.substring(endToken,startToken);
			//substring = substring.replace("\\ ", "\\_");
			if(substring.length() > 0 && 
					(	substring.contains(Constants.FunctionParamType.MODIFIER.signatureType+" ") ||
						substring.contains(Constants.FunctionParamType.PARAMETER.signatureType+" ") ||
						substring.contains(Constants.FunctionParamType.VARIABLE.signatureType+" ") ||
						substring.contains(Constants.FunctionParamType.PRODUCT.signatureType+" ") ||
						substring.contains(Constants.FunctionParamType.SITE.signatureType+" ") ||
						substring.contains(Constants.FunctionParamType.SUBSTRATE.signatureType+" ") ||
						substring.contains(Constants.FunctionParamType.VOLUME.signatureType+" ")
					)) {
				
				SwingUtilities.invokeLater(new MyRunnable(event,startToken,endToken));
				//selected = true;
			}
		 }
    		  
    
      }
	
	
	
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		
		Component c = super.getTableCellEditorComponent(table, value.toString().trim(), isSelected, row, column);
		
		if(customFont != null) c.setFont(customFont);
		return c;
	}
	
	/*public void stopTimerAutocompletion() {
		try{
			JTextField textField = (JTextField)super.editorComponent;
			KeyListener[] listeners = textField.getKeyListeners(); 
			for(int i = 0; i< listeners.length; i++) {
				KeyListener elem = listeners[i];
				if( elem instanceof AutoCompleteKeyLister) {
					((AutoCompleteKeyLister)elem).stopTimerAutocompletion();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}*/

}

class MyRunnable implements Runnable
{
	JTextField source;
	int startToken, endToken;
	InputEvent event;
	public MyRunnable(InputEvent ev, int start, int end) {
		this.event = ev;
		source = (JTextField) event.getSource(); startToken = start; endToken = end;
	}
	
	public void run()
	{
		source.requestFocus();
		//final int begin = source.getSelectionStart();
	//	final int end = source.getSelectionEnd();
		
		if(event instanceof KeyEvent) {
			if(((KeyEvent)event).getKeyCode() ==KeyEvent.VK_LEFT){
				source.setCaretPosition(endToken);
				source.moveCaretPosition(startToken);
			}
			if(((KeyEvent)event).getKeyCode() ==KeyEvent.VK_RIGHT){
				source.setCaretPosition(startToken);
				source.moveCaretPosition(endToken);
			}
		} else {
			source.select(startToken, endToken);
		}
	/*	SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				 System.out.println("cursor dopo: "+source.getCaretPosition());
				}
		});*/
	}
}


