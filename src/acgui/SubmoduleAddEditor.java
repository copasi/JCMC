package acgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.sbml.libsbml.SBMLDocument;

import com.mxgraph.swing.mxGraphComponent;

/**
 * @author T.C. Jones
 *
 */
public class SubmoduleAddEditor extends JDialog implements TreeSelectionListener, ActionListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPanel modules;
	private JPanel modulePanel;
	private JPanel existingModulesPanel;
	private JPanel importModulePanel;
	private JPanel validationPanel;
	private JTextField textfield;
	private JCheckBox checkBox;
	private JButton addButton;
	private mxGraphComponent graphComponent;
	private File file;
	private boolean lastSelectionWasFromList;
	private SubmoduleDefinitionPanel submoduleDefinitions;
	
	/**
	 * 
	 */
	public SubmoduleAddEditor(mxGraphComponent iGraphComponent)
	{
		super();
		graphComponent = iGraphComponent;
		initializeComponents();
		lastSelectionWasFromList = false;
	}
	
	private void initializeComponents()
	{
		Border border;
		JPanel upperPanel = new JPanel();
		//upperPanel.setLayout(new GridLayout(0, 2, 15, 5));
		upperPanel.setLayout(new BorderLayout());
		JPanel paddingPanel;
		
		modules = new JPanel();
		modules.setLayout(new BorderLayout());
        
        //Create the tree and put it in a scroll pane.
        submoduleDefinitions = new SubmoduleDefinitionPanel(AC_GUI.rootModule, this);
        JScrollPane listScrollPane = new JScrollPane(submoduleDefinitions);
        listScrollPane.setPreferredSize(new Dimension(170, 210));
        
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		
        existingModulesPanel = new JPanel();
        existingModulesPanel.setLayout(new BorderLayout());
        existingModulesPanel.add(listScrollPane, BorderLayout.CENTER);
        existingModulesPanel.setBorder(border);
        
		border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		//border.setTitleJustification(TitledBorder.LEFT);
		//listScrollPane.setBorder(border1);
		//modules.setPreferredSize(new Dimension(300, 350));
        modules.setBorder(border);
		//modules.add(listScrollPane, BorderLayout.NORTH);
        modules.add(existingModulesPanel, BorderLayout.NORTH);

	    JPanel labelPanel = new JPanel();
	    labelPanel.setLayout(new GridLayout(2, 1));
	    JLabel label = new JLabel("Please select the source of the submodule:");
		label.setFont(new Font("Dialog", Font.PLAIN, 12));
		//labelPanel.add(new JPanel());
		labelPanel.add(label);
		labelPanel.add(new JPanel());
		upperPanel.add(labelPanel, BorderLayout.NORTH);
        
        //upperPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        
		textfield = new JTextField();
		textfield.setEditable(false);
		textfield.setColumns(20);
		textfield.setBorder(BorderFactory.createLoweredBevelBorder());
		validationPanel = new JPanel();
		validationPanel.setLayout(new GridLayout(3, 1, 0, 0));
		validationPanel.setPreferredSize(new Dimension(115, 45));
		JButton browseButton = new JButton("Browse");
		browseButton.setActionCommand("browse");
		browseButton.addActionListener(this);
		JPanel browseButtonPanel = new JPanel();
		browseButtonPanel.add(browseButton);
		checkBox = new JCheckBox("Save as a local definition");
		checkBox.setFont(new Font("Dialog", Font.PLAIN, 11));
		checkBox.setSelected(true);
		checkBox.setEnabled(false);
		
		paddingPanel = new JPanel();
		//paddingPanel.setLayout(new BorderLayout());
		paddingPanel.add(validationPanel);
		
		border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		
		importModulePanel = new JPanel();
		importModulePanel.setLayout(new BorderLayout());
		importModulePanel.setBorder(border);
		
		importModulePanel.add(textfield, BorderLayout.NORTH);
		importModulePanel.add(browseButtonPanel, BorderLayout.EAST);
		importModulePanel.add(paddingPanel, BorderLayout.WEST);
		importModulePanel.add(checkBox, BorderLayout.SOUTH);
		
		modules.add(importModulePanel, BorderLayout.SOUTH);
		
		border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		JPanel tpadding = new JPanel();
		tpadding.setLayout(new BorderLayout());
		modulePanel = new JPanel();
        modulePanel.setLayout(new BorderLayout());
        modulePanel.setBorder(border);
        modulePanel.add(modules, BorderLayout.NORTH);
        //templatePanel.add(templates);
        //templatePanel.add(importTemplatePanel, BorderLayout.SOUTH);
        
        //tpadding.add(templatePanel, BorderLayout.NORTH);
        
        JPanel lowerPanel = new JPanel();
        lowerPanel.setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        addButton.setActionCommand("add");
        addButton.addActionListener(this);
        addButton.setEnabled(false);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        lowerPanel.add(buttonPanel, BorderLayout.EAST);
        
        getContentPane().setLayout(new BorderLayout());
		getContentPane().add(upperPanel, BorderLayout.NORTH);
		getContentPane().add(modulePanel, BorderLayout.CENTER);
		//getContentPane().add(tpadding, BorderLayout.CENTER);
		getContentPane().add(lowerPanel, BorderLayout.SOUTH);
		//getContentPane().setPreferredSize(new Dimension(650, 500));
		
		setTitle("Add Submodule");
		setSize(650, 500);
		pack();
		setLocationRelativeTo(graphComponent);
	}

	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getActionCommand().equalsIgnoreCase("browse"))
		{
			submoduleDefinitions.clearSelection();
			JFileChooser fileChooser = new JFileChooser(".");
			if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) 
            {
            	file = fileChooser.getSelectedFile();
                //inputFile = file.getName().substring(0,file.getName().lastIndexOf("."));
            	textfield.setText(file.getName());
            	lastSelectionWasFromList = false;
            	String ext = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
            	String msg1 = "";
            	String msg2 = "";
            	
            	if (ext.equals(".xml"))
            	{
	            	SBMLDocument sDoc = SBMLParser.SBMLValidation(file.getAbsolutePath());
	            	long errorNumber = sDoc.getNumErrors();
	            	
	            	if (errorNumber > 0)
	            	{
	            		addButton.setEnabled(false);
	            		msg1 = "Invalid SBML file.";
	            		msg2 = sDoc.getNumErrors() + " ";
	            		if (errorNumber > 1)
	            		{
	            			msg2 += "errors found.";
	            		}
	            		else
	            		{
	            			msg2 += "error found.";
	            		}
	            	}else
	            	{
	            		addButton.setEnabled(true);
	            		checkBox.setSelected(true);
	            		//checkBox.setEnabled(true);
	            		msg1 = "Valid SBML file.";
	            		msg2 = "Level " + sDoc.getLevel() + " Version " + sDoc.getVersion() + ".";
	            	}
            	}else if (ext.equalsIgnoreCase(".cps"))
            	{
            		msg1 = "COPASI";
            		msg2 = "file selected.";
            		addButton.setEnabled(true);
            		checkBox.setSelected(true);
            		checkBox.setEnabled(false);
            	}else if (ext.equalsIgnoreCase(".ac"))
            	{
            		msg1 = "Aggregation Connector";
            		msg2 = "file selected.";
            		addButton.setEnabled(true);
            		checkBox.setSelected(true);
            		checkBox.setEnabled(false);
            	}else
            	{
            		msg1 = "Invalid file type.";
            		file = null;
            		addButton.setEnabled(false);
            		checkBox.setSelected(true);
            		checkBox.setEnabled(false);
            	}
            	
            	validationPanel.removeAll();
            	JLabel vLabel1 = new JLabel(msg1);
        		vLabel1.setFont(new Font("Dialog", Font.ITALIC, 11));
        		JLabel vLabel2 = new JLabel(msg2);
        		vLabel2.setFont(new Font("Dialog", Font.ITALIC, 11));
        		validationPanel.add(new JLabel());
        		validationPanel.add(vLabel1);
        		validationPanel.add(vLabel2);
        		validationPanel.revalidate();
        		validationPanel.repaint();
        		pack();
            }
		}else if (ae.getActionCommand().equalsIgnoreCase("add"))
		{
			if (lastSelectionWasFromList)
			{
				//ModuleDefinition definition = AC_Utility.getSubmoduleDefinition((String)list.getSelectedValue());
				ModuleDefinition definition = (ModuleDefinition)submoduleDefinitions.getSelected().getUserObject();
				if (definition != null)
				{
					String newInstanceName = AC_Utility.promptUserForNewModuleName("Enter a Module name for the new instance.");
					if (newInstanceName != null)
					{
						AC_GUI.newModuleInstance(newInstanceName, definition, AC_GUI.activeModule);
					}
					dispose();
				}
			}
			if (file != null)
			{
				dispose();
				//AC_GUI.loadSubmodule(file.getAbsolutePath(), AC_GUI.activeModule, !checkBox.isSelected());
				AC_GUI.loadSubmodule(file.getAbsolutePath(), AC_GUI.activeModule, false);
			}
		}else if (ae.getActionCommand().equalsIgnoreCase("cancel"))
		{
			dispose();
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		TreePath path = e.getPath();
		if (path != null)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
			if (node != null)
			{
				ModuleDefinition definition = (ModuleDefinition)node.getUserObject();
				file = null;
                addButton.setEnabled(true);
                checkBox.setSelected(true);
        		checkBox.setEnabled(false);
                textfield.setText(definition.getName());
                lastSelectionWasFromList = true;
			}
		}
		validationPanel.removeAll();
		validationPanel.revalidate();
		validationPanel.repaint();
		pack();
	}
}
