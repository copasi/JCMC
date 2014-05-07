package msmb.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import javax.swing.*;

import org.apache.commons.lang3.tuple.MutablePair;

import msmb.model.ComplexSpecies;
import msmb.model.MultiModel;
import msmb.utility.CellParsers;
import msmb.utility.Constants;
import msmb.utility.GraphicalProperties;
import msmb.utility.MySyntaxException;

public class TextAreaExpressionFrame extends JDialog {

	private JPanel contentPane;
	private MultiModel multiModel;
	private JTextPane textPane;
	String originalString = null;
	private JTextField replaceFrom;
	private JTextField replaceTo;
	private boolean exitCodeOK = false;

	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TextAreaExpressionFrame frame = new TextAreaExpressionFrame(null);
					frame.showDialog(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	public String showDialog(String initialString) {
		this.originalString = initialString;
		textPane.setText(initialString);
		GraphicalProperties.resetFonts(this);
		pack();
		setLocationRelativeTo(null);
		
		setVisible(true);
		
		String ret = originalString;
		if(exitCodeOK)	{
			ret = textPane.getText();
			ret = ret.replaceAll("[\\n\\r]", "");
			ret = CellParsers.cleanMathematicalExpression(ret);
					
		}
	    
	    
	    return ret;
	}
	
	
	
	/**
	 * Create the frame.
	 */
	public TextAreaExpressionFrame(MultiModel multiModel) {
		this.multiModel = multiModel;
		setTitle("Expression editor");
		setModal(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(4,4));
		
		JPanel panel = new JPanel();
		contentPane.add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane);
		scrollPane.setPreferredSize(new Dimension(45*MainGui.customFont.getSize(), 25*MainGui.customFont.getSize()));
		scrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		textPane = new JTextPane();
		textPane.setEditorKit(new WrapEditorKit());
		scrollPane.setViewportView(textPane);
		textPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK), "none");
		textPane.getInputMap().put(KeyStroke.getKeyStroke("ctrl H"), "none");
	    
		
		AutoCompletion_MSMB autoCompletion = new AutoCompletion_MSMB(Constants.provider, multiModel);
       	autoCompletion.setShowDescWindow(true);
   		autoCompletion.setAutoActivationEnabled(true);
   		autoCompletion.setAutoCompleteSingleChoices(false);
   		autoCompletion.setAutoCompleteEnabled(false);
   		autoCompletion.install(textPane);
   		textPane.addKeyListener(new AutoCompleteKeyLister(autoCompletion,multiModel,false));
   		KeyListener keyListener = new KeyListener() {
		      public void keyPressed(final KeyEvent keyEvent) { 
		    	  if(keyEvent.getKeyCode() !=KeyEvent.CTRL_DOWN_MASK) { 
		    		  SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							  final JTextComponent source = ((JTextComponent)keyEvent.getSource());
							  String current = source.getText();
								  int cursor  = source.getCaretPosition();
								  MainGui.updateAutocompletionContext(current, cursor);
						}
		    		  }); 
		    	  }
		      }
		      public void keyReleased(KeyEvent keyEvent) {  }
		      public void keyTyped(KeyEvent keyEvent) {	}
		    
		    };
		    textPane.addKeyListener(keyListener);
		    MouseListener mouseListener = new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent e) {	}
				@Override
				public void mousePressed(MouseEvent e) {}
				@Override
				public void mouseExited(MouseEvent e) {}
				@Override
				public void mouseEntered(MouseEvent e) {}
				@Override
				public void mouseClicked(final MouseEvent e) {	  
					if(MainGui.displayTablesUneditable) return;
	                
					SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						  final JTextComponent source = ((JTextComponent)e.getSource());
						  String current = source.getText();
							  int cursor  = source.getCaretPosition();
							  MainGui.updateAutocompletionContext(current, cursor);
					}
	    		  });
				}
			};
			
			textPane.addMouseListener(mouseListener);
		JLabel lblNoteContextAssist = new JLabel("Note: context assist is available while typing (key combination: Ctrl-H)");
		
		
		JPanel buttonsAndLabelPanel = new JPanel();
		buttonsAndLabelPanel.setLayout(new BorderLayout(10,10));
		buttonsAndLabelPanel.add(lblNoteContextAssist, BorderLayout.NORTH);
		contentPane.add(buttonsAndLabelPanel, BorderLayout.NORTH);
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BorderLayout(10,10));
		buttonsAndLabelPanel.add(buttonsPanel, BorderLayout.CENTER);
		
		JButton reset = new JButton("Reset string");
		buttonsPanel.add(reset, BorderLayout.WEST);
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(originalString!=null) textPane.setText(originalString);
				else textPane.setText("");
			}
		});
		
		JPanel replaceAllPanel = new JPanel();
		replaceAllPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
	
		JLabel replaceLab = new JLabel("Replace: ", JLabel.CENTER);
		c.weightx = 0.05;
		c.gridx = 0;
		c.gridy = 0;
		c.insets= new Insets(0, 4, 0, 4);
		replaceAllPanel.add(replaceLab, c);
		
		
		replaceFrom = new JTextField();
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 0;
		replaceAllPanel.add(replaceFrom, c);
		
		JLabel with = new JLabel("with: ", JLabel.CENTER);
		c.weightx = 0.05;
		c.gridx = 2;
		c.gridy = 0;
		replaceAllPanel.add(with, c);
		
		 replaceTo = new JTextField();
		c.weightx = 0.5;
		c.gridx = 3;
		c.gridy = 0;
		replaceAllPanel.add(replaceTo, c);
		
		
		JButton replaceAll = new JButton("Replace all");
		replaceAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(replaceFrom.getText().trim().length()==0 && replaceTo.getText().trim().length()==0) return;
					String old = textPane.getText();
					textPane.setText(old.replaceAll(replaceFrom.getText().trim(), replaceTo.getText().trim()));
					textPane.revalidate();
			}
		});
		c.weightx = 0.1;
		c.gridx = 4;
		c.gridy = 0;
		replaceAllPanel.add(replaceAll, c);
		
		buttonsPanel.add(replaceAllPanel, BorderLayout.CENTER);
		
		JButton updateModel = new JButton("Update model");
		JPanel updateModelPanel = new JPanel();
		updateModelPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		updateModelPanel.add(updateModel );
		contentPane.add(updateModelPanel, BorderLayout.SOUTH);
		
		updateModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				exitCodeOK = true;
				dispose();
			}
		});
	}
	
	
	public void setText(String text) {
		textPane.setText(text);
		textPane.setCaretPosition(0);
	}
	
	
	class WrapEditorKit extends StyledEditorKit {
        ViewFactory defaultFactory=new WrapColumnFactory();
        public ViewFactory getViewFactory() {
            return defaultFactory;
        }

    }

    class WrapColumnFactory implements ViewFactory {
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new WrapLabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new ParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }

            // default to text display
            return new LabelView(elem);
        }
    }

    class WrapLabelView extends LabelView {
        public WrapLabelView(Element elem) {
            super(elem);
        }

        public float getMinimumSpan(int axis) {
            switch (axis) {
                case View.X_AXIS:
                    return 0;
                case View.Y_AXIS:
                    return super.getMinimumSpan(axis);
                default:
                    throw new IllegalArgumentException("Invalid axis: " + axis);
            }
        }

    }
	

}
