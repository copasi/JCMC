package msmb.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;

import msmb.utility.CellParsers;
import msmb.utility.Constants;
import msmb.utility.GraphicalProperties;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JFileChooser;

import org.COPASI.CCopasiDataModel;
import org.COPASI.CCopasiRootContainer;
import org.COPASI.CMetab;
import org.COPASI.CModel;
import org.COPASI.CModelValue;
import org.COPASI.CReaction;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jdesktop.swingx.JXList;
import java.awt.FlowLayout;

public class ImportAnnotationsFrame extends JDialog {

	private JPanel contentPane;
	private DefaultListModel listImported_unmatched_model;
	private DefaultListModel list_matched_model;
	private JXList listMSMB_unmatched;
	private JXList list_matched;
	private DefaultListModel listMSMB_unmatched_model;
	private JXList listImported_unmatched;
	private JTextField textFieldFileName;
	private JTextArea txtrXmlAnnotationCode;
	ExitOption exitOption = ExitOption.CANCEL;

	public static void main(String[] args) {
		 
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainGui.addLibraryPath("..\\libs");
					 MainGui.addLibraryPath("..\\..\\libs");
					 MainGui.addLibraryPath(".\\libs");
					ImportAnnotationsFrame frame = new ImportAnnotationsFrame();
					
					frame.showDialog();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void addMSMBelements(Vector<MutablePair<String, String>> element_type) {
		listMSMB_unmatched.removeAll();
		listMSMB_unmatched_model.clear();
		Vector<MSMBelement> items = new Vector<MSMBelement>();
		for(MutablePair<String, String> pair : element_type) {
			items.add(new MSMBelement(pair.left, pair.right));
		}
		Collections.sort(items);
		 for(MSMBelement i : items){
			 listMSMB_unmatched_model.addElement(i);
	    }  
	}
	
	Vector<Vector<MutablePair<String, String>>> showDialog() {
		exitOption = ExitOption.CANCEL;
		
		GraphicalProperties.resetFonts(this);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		if(exitOption == ExitOption.CANCEL) return null;
		
		Vector<Vector<MutablePair<String, String>>> spc_rct_glq_model = new Vector<Vector<MutablePair<String, String>>>();
		Vector<MutablePair<String, String>> species  = new Vector<MutablePair<String, String>>();
		Vector<MutablePair<String, String>> reactions  = new Vector<MutablePair<String, String>>();
		Vector<MutablePair<String, String>> globalQuanties  = new Vector<MutablePair<String, String>>();
		Vector<MutablePair<String, String>> model  = new Vector<MutablePair<String, String>>();
		
		for(int i = 0; i < list_matched.getElementCount(); i++) {
			AnnotationAssociation element = (AnnotationAssociation) list_matched.getElementAt(i);
			MSMBelement msmbElement = element.getSpeciesMSMB();
			SBMLelementWithAnnotation sbmlElement = element.getSpeciesImport();
			MutablePair<String, String> toStore = new MutablePair<String, String>(msmbElement.getName(), sbmlElement.getAnnotation());
			if(msmbElement.elementType.equals(Constants.TitlesTabs.SPECIES.getDescription())) {		
				toStore.left = CellParsers.extractMultistateName(toStore.left);
				species.add(toStore);
			}
			else if(msmbElement.elementType.equals(Constants.TitlesTabs.REACTIONS.getDescription())) {		reactions.add(toStore);	}
			else if(msmbElement.elementType.equals(Constants.TitlesTabs.GLOBALQ.getDescription())) {		globalQuanties.add(toStore);	}
			else if(msmbElement.elementType.equals(Constants.TitlesTabs.DEBUG.getDescription())) {		model.add(toStore);	}
			else { System.err.println("Element type not supported: "+msmbElement.elementName);}
		}
		
		spc_rct_glq_model.add(species);
		spc_rct_glq_model.add(reactions);
		spc_rct_glq_model.add(globalQuanties);
		spc_rct_glq_model.add(model);
		
		
	    return spc_rct_glq_model;
	}
	

	
	private void automaticMatching() {
		HashMap<String, SBMLelementWithAnnotation> imported = new HashMap<String, SBMLelementWithAnnotation>();
		for(int i = 0; i < listImported_unmatched_model.size(); i++) {
			SBMLelementWithAnnotation element = (SBMLelementWithAnnotation) listImported_unmatched_model.get(i);
			imported.put(element.getName(), element);
		}
		
		HashMap<String, MSMBelement> MSMB = new HashMap<String, MSMBelement>();
		for(int i = 0; i < listMSMB_unmatched_model.size(); i++) {
			MSMBelement element = (MSMBelement) listMSMB_unmatched_model.get(i);
			MSMB.put(element.getName(), element);
		}
		
		Iterator<String> it = MSMB.keySet().iterator();
		Vector<AnnotationAssociation> items = new Vector<AnnotationAssociation>();
		list_matched_model.clear();
		
		while(it.hasNext()) {
			String name = it.next();
			String unquoted = name;
			if(unquoted.startsWith("\"")&&unquoted.endsWith("\"")) unquoted = unquoted.substring(1, unquoted.length()-1);
				if(imported.containsKey(unquoted) ) {
					items.add(new AnnotationAssociation(MSMB.get(name),imported.get(unquoted)));
					listImported_unmatched_model.removeElement(imported.get(unquoted));
					listMSMB_unmatched_model.removeElement(MSMB.get(name));
			}
		}
			
		Collections.sort(items);
		 for(AnnotationAssociation i : items){
			 list_matched_model.addElement(i);
	    }  

		
	}

	

	public ImportAnnotationsFrame() {
		// to display xml nicely in tree: http://www.javalobby.org/java/forums/t19666.html
		setTitle("Import annotations");
		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(4, 4));
		setContentPane(contentPane);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.8);
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(0.5);
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setLeftComponent(splitPane_1);
		
		JPanel panel_matched = new JPanel();
		splitPane_1.setLeftComponent(panel_matched);
		panel_matched.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel_matched.add(panel, BorderLayout.SOUTH);
		
		JButton btnNewButton = new JButton("Delete selected associations");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Object[] selected = (Object[]) list_matched.getSelectedValues();
				for(Object element : selected) {
					AnnotationAssociation annotation = (AnnotationAssociation) element;
					listMSMB_unmatched_model.addElement(annotation.getSpeciesMSMB());
					listImported_unmatched_model.addElement(annotation.getSpeciesImport());
					list_matched_model.removeElement(element);
				}
			
			}
		});
		panel.add(btnNewButton);
		
		list_matched_model = new DefaultListModel();
	
		JScrollPane scrollPane_jlistMatched = new JScrollPane();
		panel_matched.add(scrollPane_jlistMatched, BorderLayout.CENTER);
		list_matched = new JXList(list_matched_model);
		list_matched.setAutoCreateRowSorter(true);
		list_matched.toggleSortOrder();
		
		scrollPane_jlistMatched.setViewportView(list_matched);
			
		list_matched.addListSelectionListener(new ListSelectionListener() {
			@Override
			 public void valueChanged(ListSelectionEvent e) {
				int firstIndexView = list_matched.getSelectedIndex();
				
				if(firstIndexView != -1) {
					int firstIndex = list_matched.convertIndexToModel(firstIndexView);
						txtrXmlAnnotationCode.setText(
							((AnnotationAssociation)list_matched_model.getElementAt(firstIndex)).getAnnotation());
					txtrXmlAnnotationCode.revalidate();
			    }
			}
		});
		
		panel_matched.add(scrollPane_jlistMatched, BorderLayout.CENTER);
		
		JPanel panel_unmatched = new JPanel();
		panel_unmatched.setLayout(new BorderLayout(0, 0));
		splitPane_1.setRightComponent(panel_unmatched);
		
		JPanel panel_1 = new JPanel();
		panel_unmatched.add(panel_1, BorderLayout.SOUTH);
		
		JButton btnAddAssociation = new JButton("Add association");
		btnAddAssociation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int selMSMBView = listMSMB_unmatched.getSelectedIndex();
				if(selMSMBView==-1) return;
				int selMSMB = listMSMB_unmatched.convertIndexToModel(selMSMBView);
				int selImportView =  listImported_unmatched.getSelectedIndex();
				int selImport = listMSMB_unmatched.convertIndexToModel(selImportView);
				if(selMSMB == -1 || selImport == -1) {
					JOptionPane.showMessageDialog(new JButton(),"No element is selected in the list of elements \nfrom the model and/or from the imported file.", "Missing selection", JOptionPane.ERROR_MESSAGE);
					return;
				}
				AnnotationAssociation aa = new AnnotationAssociation(
						(MSMBelement) listMSMB_unmatched.getSelectedValue(), (SBMLelementWithAnnotation) listImported_unmatched.getSelectedValue());
				list_matched_model.addElement(aa);
				listMSMB_unmatched_model.remove(selMSMB);
				listImported_unmatched_model.remove(selImport);
				listMSMB_unmatched.revalidate();
				listImported_unmatched.revalidate();
				
			}
		});
		panel_1.add(btnAddAssociation);
		
		JSplitPane splitPane_2 = new JSplitPane();
		splitPane_2.setResizeWeight(0.5);
		panel_unmatched.add(splitPane_2, BorderLayout.CENTER);
		
		listMSMB_unmatched_model = new DefaultListModel();
		
		listMSMB_unmatched = new JXList(listMSMB_unmatched_model);
		listMSMB_unmatched.setAutoCreateRowSorter(true);
		listMSMB_unmatched.toggleSortOrder();
		
		JScrollPane scrollPane_jlistMSMB_unmatched = new JScrollPane();
		scrollPane_jlistMSMB_unmatched.setViewportView(listMSMB_unmatched);
		
		listMSMB_unmatched.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel splitPane_lower_left = new JPanel();
		splitPane_lower_left.setLayout(new BorderLayout(4, 4));
		JLabel unmatched = new JLabel(" Model elements unmatched");
		splitPane_lower_left.add(unmatched, BorderLayout.NORTH);
		splitPane_lower_left.add(scrollPane_jlistMSMB_unmatched, BorderLayout.CENTER);
		splitPane_2.setLeftComponent(splitPane_lower_left);
		
		
		listImported_unmatched_model = new DefaultListModel();
		 
		 listImported_unmatched = new JXList(listImported_unmatched_model);
		 listImported_unmatched.setAutoCreateRowSorter(true);
		 listImported_unmatched.toggleSortOrder();
		
		listImported_unmatched.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JScrollPane scrollPane_listImported_unmatched = new JScrollPane();
		scrollPane_listImported_unmatched.setViewportView(listImported_unmatched);
			
		listImported_unmatched.addListSelectionListener(new ListSelectionListener() {
			@Override
			 public void valueChanged(ListSelectionEvent e) {
				int firstIndexView = listImported_unmatched.getSelectedIndex();
				
				if(firstIndexView!=-1) {
					int firstIndex = listImported_unmatched.convertIndexToModel(firstIndexView);
							txtrXmlAnnotationCode.setText(
							((SBMLelementWithAnnotation)listImported_unmatched_model.getElementAt(firstIndex)).getAnnotation());
					txtrXmlAnnotationCode.revalidate();
				    }
				}	
		});
		
		
		JPanel splitPane_lower_right = new JPanel();
		splitPane_lower_right.setLayout(new BorderLayout(4, 4));
		JLabel unmatched2 = new JLabel(" Imported elements unmatched");
		splitPane_lower_right.add(unmatched2, BorderLayout.NORTH);
		splitPane_lower_right.add(scrollPane_listImported_unmatched, BorderLayout.CENTER);
		splitPane_2.setRightComponent(splitPane_lower_right);
		
		JScrollPane scrollPane_xmlAnnotation = new JScrollPane();
		splitPane.setRightComponent(scrollPane_xmlAnnotation);
		
		txtrXmlAnnotationCode = new JTextArea();
		txtrXmlAnnotationCode.setEditable(false);
		scrollPane_xmlAnnotation.setViewportView(txtrXmlAnnotationCode);
		
		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.NORTH);
		panel_2.setLayout(new BorderLayout(4, 4));
		
		JLabel lblNewLabel = new JLabel(" Import annotations from file: ");
		panel_2.add(lblNewLabel, BorderLayout.WEST);
		
		textFieldFileName = new JTextField();
		panel_2.add(textFieldFileName, BorderLayout.CENTER);
		textFieldFileName.setColumns(10);
		
		JButton btnNewButton_1 = new JButton("Browse...");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				int returnVal = fileChooser.showOpenDialog(null);
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fileChooser.getSelectedFile();
		            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			    	assert CCopasiRootContainer.getRoot() != null;
		            CCopasiDataModel dataModel = CCopasiRootContainer.addDatamodel();
		            assert CCopasiRootContainer.getDatamodelList().size() == 1;
		            textFieldFileName.setText(file.getAbsolutePath());
		         	try {
			    	    	 dataModel.importSBML(file.getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
					}
		            CModel model = dataModel.getModel();
		            model.convert2NonReversible();
		        	setCursor(null);
		        	
		            int iMax = (int)model.getMetabolites().size();
		            for (int i = 0;i < iMax;i++)
		            {
		                CMetab metab = model.getMetabolite(i);
		                
		                String annotation = metab.getMiriamAnnotation(); 
		                String processedAnnotation = processAnnotation(annotation,true);
		                if(processedAnnotation != null) {
		                	SBMLelementWithAnnotation element = new SBMLelementWithAnnotation(metab.getObjectName(), 
		                																Constants.TitlesTabs.SPECIES.getDescription(),
		                																processedAnnotation);
		                	listImported_unmatched_model.addElement(element);
		                }
		            }
		          
		            
		             iMax = (int)model.getReactions().size();
		            for (int i = 0;i < iMax;i++)
		            {
		                CReaction model_element = model.getReaction(i);
		               
		                String annotation = model_element.getMiriamAnnotation(); 
		                String processedAnnotation = processAnnotation(annotation,false);
		                if(processedAnnotation != null) {
		                	SBMLelementWithAnnotation element = new SBMLelementWithAnnotation(model_element.getObjectName(), 
		                																Constants.TitlesTabs.REACTIONS.getDescription(),
		                																processedAnnotation);
		                	listImported_unmatched_model.addElement(element);
		                }
		            }
		            
		            iMax = (int)model.getModelValues().size();
		            for (int i = 0;i < iMax;i++)
		            {
		                CModelValue model_element = model.getModelValue(i);
		               
		                String annotation = model_element.getMiriamAnnotation(); 
		                String processedAnnotation = processAnnotation(annotation,false);
		                if(processedAnnotation != null) {
		                	SBMLelementWithAnnotation element = new SBMLelementWithAnnotation(model_element.getObjectName(), 
		                																Constants.TitlesTabs.GLOBALQ.getDescription(),
		                																processedAnnotation);
		                	listImported_unmatched_model.addElement(element);
		                }
		            }
		          
		            
		            automaticMatching();
		            
		        } 
			}

		});
		panel_2.add(btnNewButton_1, BorderLayout.EAST);
		
		JPanel panel_3 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_3.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		contentPane.add(panel_3, BorderLayout.SOUTH);
		
		JButton btnUpdateModel = new JButton("Update model");
		btnUpdateModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitOption = ExitOption.OK;
					dispose();
			}
		});
		panel_3.add(btnUpdateModel);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitOption = ExitOption.CANCEL;
					dispose();
			}
		});
		panel_3.add(btnCancel);
		
		pack();
	}
	
	HashSet<Vector<String>> uniqueAnnotations = new HashSet<Vector<String>>();

	private String processAnnotation(String annotation, boolean listOnlyUniqueAnnotationElements) {
		if(annotation == null || annotation.length() == 0) return null;
		String resourceDelimiter = "rdf:resource=";
		Vector<String> listIDs = new Vector<String>();
		String[] splitString = annotation.split(resourceDelimiter);
		for (int i = 0; i < splitString.length; ++i) {
			String piece = splitString[i];
			if(piece.startsWith("\"")) {
				listIDs.add(piece.substring(1, piece.indexOf("\"",1)));
			}
		}
		
		Collections.sort(listIDs);
		
		if(listOnlyUniqueAnnotationElements) {
			if(uniqueAnnotations.contains(listIDs)) {
				//System.out.println("---------duplicate");
				return null;
			} else {
				uniqueAnnotations.add(listIDs);
				return annotation;
			}
		} else {
			return annotation;
		}
	}

}

class AnnotationAssociation implements Comparable<AnnotationAssociation>{
	
	MSMBelement speciesMSMB = new MSMBelement("", "");
	SBMLelementWithAnnotation speciesImport = new SBMLelementWithAnnotation("","", "");
	
	public MSMBelement getSpeciesMSMB() {		return speciesMSMB;	}
	public SBMLelementWithAnnotation getSpeciesImport() {		return speciesImport;	}

	public AnnotationAssociation(MSMBelement msmb_speciesName, SBMLelementWithAnnotation import_SBMLelement) {
		speciesMSMB = new MSMBelement(msmb_speciesName);
		speciesImport = new SBMLelementWithAnnotation(import_SBMLelement);
	}
	
	@Override
	public String toString() {
		return speciesMSMB.getName() +" <-> " + speciesImport;
	}
	
	public String getAnnotation() {return speciesImport.getAnnotation();}
	
	@Override
	public int compareTo(AnnotationAssociation o) {
		return speciesMSMB.compareTo(o.speciesMSMB);
	}
	
}

class SBMLelementWithAnnotation implements Comparable<SBMLelementWithAnnotation>{
	
	String elementName = new String();
	String elementType = new String();
	String elementAnnotation = new String();
	
	public String getAnnotation() {return elementAnnotation;}
	public String getName() {return elementName;}
	
	public SBMLelementWithAnnotation(String name, String type, String annot) {
		elementName = new String(name);
		elementType = new String(type);
		elementAnnotation = new String(annot);
	}
	
	public SBMLelementWithAnnotation(SBMLelementWithAnnotation from) {
		elementName = new String(from.elementName);
		elementType = new String(from.elementType);
		elementAnnotation = new String(from.elementAnnotation);
	}
	
	@Override
	public String toString() {
		return elementName +" (" + elementType+")";
	}
	@Override
	public int compareTo(SBMLelementWithAnnotation o) {
		return elementName.compareTo(o.elementName);
	}
}

class MSMBelement implements Comparable<MSMBelement> {
	String elementName = new String();
	String elementType = new String();
	public String getName() {return elementName;}
	
	
	public MSMBelement(String name, String type) {
		elementName = new String(name);
		elementType = new String(type);
	}
	
	public MSMBelement(MSMBelement o) {
		elementName = new String(o.elementName);
		elementType = new String(o.elementType);
	}
	
	@Override
	public String toString() {
		return elementName +" (" + elementType+")";
	}


	@Override
	public int compareTo(MSMBelement o) {
		return elementName.compareTo(o.elementName);
	}
	
}