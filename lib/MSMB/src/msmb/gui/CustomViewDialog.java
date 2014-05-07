package  msmb.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.DefaultListModel;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JList;

import msmb.parsers.mathExpression.MR_Expression_Parser;
import msmb.parsers.mathExpression.syntaxtree.CompleteExpression;
import msmb.parsers.mathExpression.visitor.ExpressionVisitor;

import msmb.model.MultiModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class CustomViewDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private String compressedExpression = new String();
	public DefaultListModel listModel = new DefaultListModel();
	private JTextPane txtpnSelectTheFunctions;
	private JList list;
	MultiModel multiModel;

	public String getCompressedExpression() {		return compressedExpression;	}
	
	public void setCompressedExpression(String compressedExpr) throws Exception {		
		this.compressedExpression = compressedExpr;
		txtpnSelectTheFunctions.setText("Select the terms that will be expanded in the expression  \r"+compressedExpression);
		fillFunctionTermList(compressedExpr);
	}

	
	private void fillFunctionTermList(String compressedExpr) throws Exception {
		MainGui.jListFunctionToCompact.setSelectionInterval(0, MainGui.listModel_FunctionToCompact.size()-1);
		/*CellParsers.parser.parseExpression(compressedExpr);
		OdeExpressionVisitor_DELETE_oldParser visitor = new OdeExpressionVisitor_DELETE_oldParser(MainGui.jListFunctionToCompact.getSelectedValuesList(),multiModel);
		if(CellParsers.parser.getErrorInfo()!= null) {
				try {
				throw new Exception(CellParsers.parser.getErrorInfo());
			} catch (Exception e) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
			}
		}
		visitor.toString(CellParsers.parser.getTopNode());
		Vector<String> v = visitor.getFunctionsInTheExpression();*/
		Vector<String> v = new Vector<String>();
		v.add("TODO: add list of functions used in the expression");
		v.add("TODO: add list of functions used in the expression");
		v.add("TODO: add list of functions used in the expression");
		v.add("TODO: add list of functions used in the expression");
		
		for(String el : v) {
			listModel.addElement(el);
		}
		if(v.size()==0) {
			throw new Exception();
		}
		
	}
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			CustomViewDialog dialog = new CustomViewDialog(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public CustomViewDialog(MultiModel mm) {
		multiModel = mm;
		setTitle("Custom view");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
	
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			txtpnSelectTheFunctions = new JTextPane();
			txtpnSelectTheFunctions.setEditable(false);
			txtpnSelectTheFunctions.setBackground(SystemColor.menu);
			txtpnSelectTheFunctions.setText("Select the terms that will be expanded in the expression  \r"+compressedExpression +"\r");
			contentPanel.add(txtpnSelectTheFunctions, BorderLayout.NORTH);
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			{
				list = new JList(listModel);
				scrollPane.setViewportView(list);
			}
		}
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.SOUTH);
			{
				JButton btnSelectAll = new JButton("Select all");
				btnSelectAll.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						list.setSelectionInterval(0, listModel.size()-1);
						list.revalidate();
					}
				});
				panel.add(btnSelectAll);
			}
			{
				JButton btnDeselectAll = new JButton("Deselect all");
				btnDeselectAll.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						list.clearSelection();
					}
				});
				panel.add(btnDeselectAll);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
		setModal(true);
		this.setLocationRelativeTo(null);
		pack();
	}

	public String getReturnString() throws Exception {
		if(list.getSelectedValues().length == 0) throw new Exception("No functions to expand");
	/*	CellParsers.parser.parseExpression(compressedExpression);
		OdeExpressionVisitor_DELETE_oldParser visitor = new OdeExpressionVisitor_DELETE_oldParser(true, list.getSelectedValuesList());
		if(CellParsers.parser.getErrorInfo()!= null) {
			try {
				throw new Exception(CellParsers.parser.getErrorInfo());
			} catch (Exception e) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
			}
		}
		
		return visitor.toString(CellParsers.parser.getTopNode());*/
		
		String tmp = new String();
		try{
			InputStream is = new ByteArrayInputStream(compressedExpression.getBytes("UTF-8"));
			MR_Expression_Parser parser = new MR_Expression_Parser(is);
			CompleteExpression root = parser.CompleteExpression();
			ExpressionVisitor vis = new ExpressionVisitor(Arrays.asList(list.getSelectedValues()),multiModel,false);
			root.accept(vis);
			if(vis.getExceptions().size() == 0) {
				tmp  = vis.getExpression();
				
			} else {
				throw vis.getExceptions().get(0);
			}

		}catch (Throwable e) {
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
		}
		return tmp;
	}
	

}
