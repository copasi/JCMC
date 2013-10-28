/**
 * 
 */
package acgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.sbml.libsbml.SBMLDocument;

import com.mxgraph.swing.mxGraphComponent;

/**
 * @author T.C. Jones
 *
 */
public class TemplateAddEditor extends JDialog implements ListSelectionListener, ActionListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPanel externalTemplate;
	private JPanel localTemplates;
	private JPanel templatePanel;
	private JPanel validationPanel;
	private JTextField textfield;
	private JCheckBox checkBox;
	private JButton addButton;
	private JList list;
    private DefaultListModel listModel;
	private mxGraphComponent graphComponent;
	
	/**
	 * 
	 */
	public TemplateAddEditor(mxGraphComponent iGraphComponent)
	{
		super();
		graphComponent = iGraphComponent;
		initializeComponents();
	}
	
	private void initializeComponents()
	{
		JPanel upperPanel = new JPanel();
		//upperPanel.setLayout(new GridLayout(0, 2, 15, 5));
		upperPanel.setLayout(new BorderLayout());
		JPanel paddingPanel;
		
		localTemplates = new JPanel();
		
		listModel = new DefaultListModel();
        listModel.addElement("Synthesis1");
        listModel.addElement("Synthesis2");
        //listModel.addElement("Synthesis_Template3");
        listModel.addElement("Degradation1");
        //listModel.addElement("Degradation_Template2");
        //listModel.addElement("Degradation_Template3");
 
        //Create the list and put it in a scroll pane.
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(-1);
        list.addListSelectionListener(this);
        list.setVisibleRowCount(5);
        JScrollPane listScrollPane = new JScrollPane(list);
        listScrollPane.setPreferredSize(new Dimension(170, 210));
        
		TitledBorder border1;
		border1 = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Local Templates");
		border1.setTitleJustification(TitledBorder.LEFT);
		//listScrollPane.setBorder(border1);
		localTemplates.setPreferredSize(new Dimension(200, 250));
        localTemplates.setBorder(border1);
		localTemplates.add(listScrollPane);
        
		externalTemplate = new JPanel();
		externalTemplate.setLayout(new BorderLayout());
		externalTemplate.setPreferredSize(new Dimension(200, 250));
		textfield = new JTextField();
		textfield.setEditable(false);
		textfield.setColumns(20);
		textfield.setBorder(BorderFactory.createLoweredBevelBorder());
		validationPanel = new JPanel();
		validationPanel.setLayout(new GridLayout(3, 1, 0, 0));
		JButton browseButton = new JButton("Browse");
		browseButton.setActionCommand("browse");
		browseButton.addActionListener(this);
		JPanel browseButtonPanel = new JPanel();
		browseButtonPanel.add(browseButton);
		checkBox = new JCheckBox("Save as a local template");
		checkBox.setFont(new Font("Dialog", Font.PLAIN, 11));
		checkBox.setSelected(true);
		checkBox.setEnabled(false);
		
		paddingPanel = new JPanel();
		//paddingPanel.setLayout(new BorderLayout());
		paddingPanel.add(validationPanel);
		
		
		externalTemplate.add(textfield, BorderLayout.NORTH);
		externalTemplate.add(browseButtonPanel, BorderLayout.EAST);
		externalTemplate.add(paddingPanel, BorderLayout.WEST);
		externalTemplate.add(checkBox, BorderLayout.SOUTH);
		
		TitledBorder border2;
		border2 = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Import Template");
		border2.setTitleJustification(TitledBorder.LEFT);
		externalTemplate.setBorder(border2);
		
		JRadioButton localButton = new JRadioButton("Existing Template");
	    localButton.setActionCommand("local");

	    JRadioButton externalButton = new JRadioButton("External Template");
	    externalButton.setActionCommand("external");
	    
	    ButtonGroup buttonGroup = new ButtonGroup();
	    buttonGroup.add(localButton);
	    buttonGroup.add(externalButton);
	    
	    localButton.addActionListener(this);
	    externalButton.addActionListener(this);
	    
	    JPanel labelPanel = new JPanel();
	    labelPanel.setLayout(new GridLayout(2, 1));
	    JLabel label = new JLabel("Please select the source of the submodule:");
		label.setFont(new Font("Dialog", Font.PLAIN, 12));
		//labelPanel.add(new JPanel());
		labelPanel.add(label);
		labelPanel.add(new JPanel());
		upperPanel.add(labelPanel, BorderLayout.NORTH);
        upperPanel.add(localButton, BorderLayout.WEST);
        upperPanel.add(externalButton, BorderLayout.EAST);
        //upperPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        
        templatePanel = new JPanel();
        
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
		getContentPane().add(templatePanel, BorderLayout.CENTER);
		getContentPane().add(lowerPanel, BorderLayout.SOUTH);
		//getContentPane().setPreferredSize(new Dimension(650, 500));
		
		setTitle("Add Submodule");
		setSize(650, 500);
		pack();
		setLocationRelativeTo(graphComponent);
	}

	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		if (lse.getValueIsAdjusting() == false) {
			 
            if (list.getSelectedIndex() == -1) {
            //No selection, disable fire button.
                addButton.setEnabled(false);
 
            } else {
            //Selection, enable the fire button.
                addButton.setEnabled(true);
            }
        }
	}

	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getActionCommand().equalsIgnoreCase("local"))
		{
			list.clearSelection();
			//list.repaint();
			addButton.setEnabled(false);
			templatePanel.removeAll();
			templatePanel.add(localTemplates);
			templatePanel.revalidate();
			templatePanel.repaint();
			validationPanel.removeAll();
			validationPanel.revalidate();
    		validationPanel.repaint();
			pack();
		}else if (ae.getActionCommand().equalsIgnoreCase("external"))
		{
			addButton.setEnabled(false);
			templatePanel.removeAll();
			templatePanel.add(externalTemplate);
			templatePanel.revalidate();
			templatePanel.repaint();
			textfield.setText("");
			validationPanel.removeAll();
			validationPanel.revalidate();
    		validationPanel.repaint();
			pack();
		}else if (ae.getActionCommand().equalsIgnoreCase("browse"))
		{
			JFileChooser fileChooser = new JFileChooser(".");
			
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) 
            {
            	File file = fileChooser.getSelectedFile();
                //inputFile = file.getName().substring(0,file.getName().lastIndexOf("."));
            	textfield.setText(file.getName());
            	String ext = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
				
            	SBMLDocument sDoc = SBMLParser.SBMLValidation(file.getAbsolutePath());
            	long errorNumber = sDoc.getNumErrors();
            	String msg1;
            	String msg2;
            	if (errorNumber > 0)
            	{
            		addButton.setEnabled(false);
            		msg1 = "Invalid SBML file.";
            		msg2 = sDoc.getNumErrors() + " errors found.";
            	}else
            	{
            		addButton.setEnabled(true);
            		msg1 = "Valid SBML file.";
            		msg2 = "Level " + sDoc.getLevel() + " Version " + sDoc.getVersion() + ".";
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
			
		}else if (ae.getActionCommand().equalsIgnoreCase("cancel"))
		{
			dispose();
		}
	}
}
