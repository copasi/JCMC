package  msmb.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.JButton;

import java.awt.Color;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JTextPane;

import org.apache.commons.lang3.tuple.MutablePair;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ExpandedReactionsDialog extends JDialog {

	
	private static final long serialVersionUID = 1L;
	private int totNumReact = 0;
	private int lastReactionNumber = 0;
	private int numberReactionShowed = 0;
	private int row = 0;
	
	public void setRow(int row) {
		this.row = row;
	}

	public void setLastReactionNumber(int lastReactionNumber) {
		this.lastReactionNumber = lastReactionNumber;
		int to = lastReactionNumber;
		if(lastReactionNumber > totNumReact) {
			btnShowMore.setEnabled(false);
			to = totNumReact;
		} else { btnShowMore.setEnabled(true);}
		fromToLabel.setText("Reaction from "+(lastReactionNumber-numberReactionShowed+1)+" to "+to);
	}
	
	public void setNumberReactionShowed(int n) {
		this.numberReactionShowed = n;
		int to = lastReactionNumber;
		if(lastReactionNumber > totNumReact) to = totNumReact;
		fromToLabel.setText("Reaction from "+(lastReactionNumber-numberReactionShowed+1)+" to "+to);
	}
	
	
	public void setShowedReactions(String reactionsOK, String reactionIncomplete) {
		textArea.setText("");
		try {
			styledDoc.insertString(styledDoc.getLength(), reactionsOK,styledDoc.getStyle("regular"));
			styledDoc.insertString(styledDoc.getLength(), reactionIncomplete,styledDoc.getStyle("bold"));
		} catch (BadLocationException e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
		}
			textArea.setCaretPosition(0);
		textArea.revalidate();
	}
	public void setTotNumReact(int totNumReact) {
		this.totNumReact = totNumReact;
		labelExpansion.setText("The expansion will generate: "+totNumReact+" reactions");
		labelExpansion.revalidate();
	}

	private JPanel contentPane;
	private JLabel labelExpansion;
	private JTextPane textArea;
	private StyledDocument styledDoc;
	private JLabel fromToLabel;
	private JButton btnShowMore;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ExpandedReactionsDialog frame = new ExpandedReactionsDialog();
					frame.setVisible(true);
				} catch (Exception e) {
					if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ExpandedReactionsDialog() {
		setModal(true);
		setTitle("Expanded reactions");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane);
		
		    
	        
		textArea = new JTextPane();
	     styledDoc = textArea.getStyledDocument();
	    Style def = StyleContext.getDefaultStyleContext().
        getStyle(StyleContext.DEFAULT_STYLE);
	    Style regular = styledDoc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");
        StyleConstants.setFontSize(def, 12);
        
        Style s = styledDoc.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);
 
        s = styledDoc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);
        StyleConstants.setForeground(s, Color.RED);
        StyleConstants.setUnderline(s, true);
        
		 textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		
		fromToLabel = new JLabel("Reaction from "+(lastReactionNumber-numberReactionShowed)+" to "+lastReactionNumber);
		panel.add(fromToLabel, BorderLayout.SOUTH);
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		labelExpansion = new JLabel("The expansion will generate: "+totNumReact+" reactions");
		panel_1.add(labelExpansion);
		
		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.SOUTH);
		
		JButton btnNewButton_1 = new JButton("Close");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		panel_2.setLayout(new BorderLayout(0, 0));
		panel_2.add(btnNewButton_1, BorderLayout.EAST);
		
		btnShowMore = new JButton("Show more...");
		btnShowMore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					showMore();
				} catch (Throwable e1) {
					if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e1.printStackTrace();
				}
			}
		});
		panel_2.add(btnShowMore, BorderLayout.WEST);
		
		JPanel panel_3 = new JPanel();
		contentPane.add(panel_3, BorderLayout.EAST);
		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));
		setLocationRelativeTo(null);
	}

	protected void showMore() throws Throwable {
		MutablePair<String, String> pair = new MutablePair<String, String>();
		String reactions = new String();
		String missing = new String();
		pair = MainGui.getExpandedMultistateReactions(row, lastReactionNumber+1 , lastReactionNumber+numberReactionShowed);
		reactions = pair.left;
		missing = pair.right;
		setShowedReactions(reactions, missing);
		setLastReactionNumber(lastReactionNumber+numberReactionShowed);
		
	}

}
