package  msmb.gui;

import org.COPASI.CFunction;
import org.apache.commons.lang3.tuple.MutablePair;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Dimension;
import javax.swing.JScrollPane;
import java.awt.GridLayout;
import javax.swing.JComboBox;


import msmb.model.Function;
import msmb.utility.CellParsers;
import msmb.utility.Constants;
import msmb.utility.GridLayout2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import javax.swing.SwingConstants;
import java.awt.FlowLayout;
import javax.swing.border.CompoundBorder;
import javax.swing.border.BevelBorder;

public class FunctionParameterFrame extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JLabel jLabel = null;
	private JTextField jLabelFunName = null;
	private JLabel jLabel1 = null;
	private JLabel jLabelEquation = null;
	private JScrollPane jScrollPane = null;
	private GridLayout gridLayout;  
	private JPanel jPanel = null;
	
	private Function function = null;
	private JButton jButton = null;
	
	private int modifiedRow = -1;
	private MainGui parentFrame = null;
	private String oldName;
	private JPanel panel;
	private JPanel panel_1;
	private JPanel panel_2;
	private JPanel panel_3;
	private String jTextFieldName_alternativeName = "alternativeName";
	
	public FunctionParameterFrame(MainGui owner, Function f, int row) {
		super();
		initialize();
		modifiedRow = row;
		parentFrame  = owner;
		if(f!= null)
			try {
				function = new Function(f);
				function.setType(CFunction.UserDefined, 0);
				fillFrameFields(f);
			} catch (Exception e) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
			}
	}
	
	public void setFunction(int row, Function f) {
		if(f!= null)
			try {
				function = new Function(f);
				modifiedRow = row;
				oldName = f.getName();
				fillFrameFields(f);
			} catch (Exception e) {
				if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
			}
	}
	
	public void clearAll() {
		this.function = null;
		jLabelFunName.setText("");
		this.jLabelEquation.setText("");
		jPanel.removeAll();
	}
	
	
	
	private void initialize() {
		this.setSize(372, 286);
		this.setContentPane(getJContentPane());
		this.setTitle("Function properties...");
	//	this.setResizable(false);
		this.setLocationRelativeTo(null);
	}

	
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout(0, 0));
			jContentPane.add(getJScrollPane());
			jContentPane.add(getPanel_1(), BorderLayout.SOUTH);
			jContentPane.add(getPanel(), BorderLayout.NORTH);
			
		}
		return jContentPane;
	}

	
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJPanel());
		}
		return jScrollPane;
	}

	

	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			EmptyBorder border = new EmptyBorder(6,6,5, 5);
			
			jPanel.setBorder(border);
		}
		return jPanel;
	}
	
	private void fillFrameFields(Function f) throws Exception{
		this.jLabelFunName.setText(f.getName());
		this.jLabelEquation.setText(f.getExpandedEquation(new Vector<String>()));
		
		Vector<String> paramNames = f.getParametersNames();
		Vector<String> paramTypes = f.getParametersTypes();
		gridLayout = new GridLayout2();
		int nparam = paramNames.size();
		if(nparam < 5) gridLayout.setRows(5);
		else gridLayout.setRows(nparam);
		
		gridLayout.setHgap(2);
		gridLayout.setVgap(5);
		
		
		jPanel.setLayout(gridLayout);
		
		Vector<String> pTypes = new Vector<String>();
		pTypes.addAll(Constants.paramTypes);
		
		for(int i = 0; i < nparam; i++) {
			Dimension dim = new Dimension(15,20);
			JLabel nameLabel = new JLabel();
			if(nameLabel.getPreferredSize().width < dim.width) nameLabel.setPreferredSize(dim);
			if(nameLabel.getMinimumSize().width < dim.width) nameLabel.setMinimumSize(dim);
			nameLabel.setText(paramNames.get(i));
	
			JTextField newNameTextField = new JTextField();
			if(newNameTextField.getPreferredSize().width < dim.width) newNameTextField.setPreferredSize(dim);
			if(newNameTextField.getMinimumSize().width < dim.width) newNameTextField.setMinimumSize(dim);
			newNameTextField.setText(paramNames.get(i));
			newNameTextField.setName(jTextFieldName_alternativeName);
	
			JComboBox types = new JComboBox(pTypes);
			String t = paramTypes.get(i);
			types.setPreferredSize(new Dimension(50,20));
			types.setMinimumSize(new Dimension(50,20));
			for(int j = 0; j <pTypes.size(); j++) {
				if(pTypes.get(j).toLowerCase().contains(t.toLowerCase())) {
					types.setSelectedIndex(j);
					break;
				}
			}
			JTextField order = new JTextField(new Integer(i+1).toString());
			order.setMinimumSize(dim);
			order.setMaximumSize(dim);
			order.setPreferredSize(dim);
			jPanel.add(order);
			jPanel.add(nameLabel);
			jPanel.add(newNameTextField);
			jPanel.add(types);
			
		}
		
		if(nparam < 5) {//fill the grid with empty labels
			
			for(int i = 0; i < 15-nparam*3; i++) {
				JLabel nameLabel = new JLabel(" ");
				jPanel.add(nameLabel);
			}
		}
		
		//revalidate();
	}

	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton("Update Model");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
			Function alreadyExist = null;
					try {
						alreadyExist = parentFrame.multiModel.funDB.getFunctionByName(jLabelFunName.getText());
					} catch (Exception e2) {
							if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e2.printStackTrace();
					}
					int alreadyExIndex = parentFrame.multiModel.funDB.getFunctionIndex(alreadyExist);
					if(alreadyExIndex!=-1 && alreadyExIndex!=modifiedRow) {
						  JOptionPane.showMessageDialog(new JButton(),"The new name you chose already exists!", "Invalid name!", JOptionPane.ERROR_MESSAGE);
						  return;
					}
				
					
					try {
						function.setName(jLabelFunName.getText());
					} catch (Exception ex) {
						if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
					}
					HashMap<String, MutablePair<Integer, Integer>> changedOrders = null;
					HashMap<String, String> changedNames = null;
					if(modifiedRow != -1) {
						try {
							Vector changedElements = updateFunctionParameter();
							changedOrders = (HashMap<String, MutablePair<Integer, Integer>>) changedElements.get(0);
							changedNames = (HashMap<String, String>) changedElements.get(1);
							
						} catch (Exception e1) {
							if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 	e1.printStackTrace();
							  JOptionPane.showMessageDialog(new JButton(),e1.getMessage(), "Invalid change!", JOptionPane.ERROR_MESSAGE);
							  return;
						}
						boolean oldHidePopupWarning = MainGui.hidePopupWarning;
						MainGui.hidePopupWarning = true;
						parentFrame.renameFunction_fromCellOrfromFunctionParameterFrame(function, modifiedRow,oldName,changedOrders,changedNames);
						MainGui.hidePopupWarning = oldHidePopupWarning;
					}
					setVisible(false);
				}
			
			});
			
		}
		return jButton;
	}
	
	//Vector containing as first entry HashMap<String, MutablePair<Integer, Integer>>  changedOrders
	//	 as second entry HashMap<String, String >  changedNames
	private Vector updateFunctionParameter() throws Exception {
		Component[] comp = jPanel.getComponents();
		String paramName = null;
		String paramType = null;
		String paramNameInTextField = null;
		Integer cparamType = null;
		Integer paramOrder = null;
		HashSet<Integer> indexes = new HashSet<Integer>();
		
		HashMap<String, MutablePair<Integer, Integer>> ret_entry1 = new HashMap<String, MutablePair<Integer, Integer>>();
		HashMap<String, String> ret_entry2 = new HashMap<String, String>();
		HashSet<String> uniqueNewNames = new HashSet<String>();
		
		for(int i = 0; i < comp.length; i++) {
			Component current = comp[i];
			if(current instanceof JTextField) {
				if(current.getName()== null || current.getName().compareTo(jTextFieldName_alternativeName)!=0) {
					try{
						paramOrder =Integer.parseInt(((JTextField)current).getText());
					} catch(Exception ex) {
						if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) ex.printStackTrace();
						throw new Exception("\""+((JTextField)current).getText()+"\" is not a valid integer index.");
					}
					if(indexes.contains(paramOrder)) {
						throw new Exception(paramOrder+": duplicate index.");
					}
					indexes.add(paramOrder);
				} else {
					paramNameInTextField = CellParsers.cleanName(((JTextField)current).getText().trim());
					if(uniqueNewNames.contains(paramNameInTextField)) {
						throw new Exception("Duplicate names for parameters are not allowed (duplicate entry \""+paramNameInTextField+"\")");
					} else {
						uniqueNewNames.add(paramNameInTextField);
					}
				}
			}
			if(current instanceof JLabel) {
				paramName = ((JLabel)current).getText();
			}
			if(current instanceof JComboBox) {
				paramType = ((String)((JComboBox)current).getSelectedItem());
			}
			if(paramName!= null && paramType != null && paramOrder != null && paramNameInTextField !=null) {
				
				if(function.getNumParam() < paramOrder){
					throw new Exception("The index "+paramOrder+" is out of bound. The function contains "+function.getNumParam()+ " parameters.");
				}
				
				cparamType = Constants.FunctionParamType.getCopasiTypeFromDescription(paramType);
				function.setParameterRole(paramName, cparamType);
				int before = function.getParameterIndex(paramName);
				paramOrder = paramOrder-1;
				function.setParameterIndex(paramName, paramOrder);
				if(before != paramOrder) {
					ret_entry1.put(paramName, new MutablePair<Integer, Integer>(before, paramOrder));
				}
				
			 	if(paramName.compareTo(paramNameInTextField)!=0) {
					ret_entry2.put(paramName, paramNameInTextField);
				}
				paramName = null;
				paramType = null;
				paramOrder = null;
				paramNameInTextField = null;
			}
		}
		
		
		Vector ret = new Vector();
		ret.add(ret_entry1);
		ret.add(ret_entry2);
		return ret;
		
	}

	public String getSignature() {
		if(this.function!= null) return this.function.printCompleteSignature();
		else return null;
	}

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			panel.setLayout(new BorderLayout(2, 2));
			panel.add(getPanel_2(), BorderLayout.WEST);
			panel.add(getPanel_3(), BorderLayout.CENTER);
		}
		return panel;
	}
	private JPanel getPanel_1() {
		if (panel_1 == null) {
			panel_1 = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
			flowLayout.setAlignment(FlowLayout.RIGHT);
			panel_1.add(getJButton());
		}
		return panel_1;
	}
	private JPanel getPanel_2() {
		if (panel_2 == null) {
			panel_2 = new JPanel();
			panel_2.setLayout(new GridLayout(2, 1, 0, 2));
			jLabel = new JLabel();
			panel_2.add(jLabel);
			jLabel.setText(" Function name:");
			jLabel1 = new JLabel();
			panel_2.add(jLabel1);
			jLabel1.setText(" Equation:");
		}
		return panel_2;
	}
	private JPanel getPanel_3() {
		if (panel_3 == null) {
			panel_3 = new JPanel();
			panel_3.setLayout(new GridLayout(0, 1, 0, 0));
			jLabelFunName = new JTextField();
			panel_3.add(jLabelFunName);
			jLabelEquation = new JLabel();
			panel_3.add(jLabelEquation);
			jLabelEquation.setText("");
		}
		return panel_3;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
