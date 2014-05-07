package  msmb.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;


import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.BoxLayout;
import javax.swing.JSlider;
import java.awt.FlowLayout;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.GroupLayout;

import msmb.utility.Constants;
import msmb.utility.GraphicalProperties;
import msmb.utility.SwingUtils;

public class PreferencesFrame extends JDialog {

	
	private static final long serialVersionUID = 1L;
	private JCheckBox jCheckBoxAutocomplete = null;
	private JCheckBox jCheckBoxDialogWindow = null;
	

	private javax.swing.ButtonGroup buttonGroup1;
	private javax.swing.ButtonGroup buttonGroup2;
	private JCheckBox jCheckBoxHighlightCellOpenIssues = null;
//	private javax.swing.JCheckBox jCheckBoxPopUpWarnings;
//	private JCheckBox jCheckBoxShowAllAvailableFunctions = null;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JRadioButton jRadioButton1;
	private javax.swing.JRadioButton jRadioButton2;
	private javax.swing.JRadioButton jRadioButton3;
	private javax.swing.JPanel panelBehavior;
	private JTextField defaultCompName;
	private JTextField defaultGlobalQValue;
	private JTextField defaultSpeciesInitialValue;
	private JTextField defaultCompartmentInitialValue;
	private JButton jButtonOK;
	private JButton jButtonRestoreDefaults;
	
	private JScrollPane scrollPaneColorPalette;
	private JScrollPane scrollPaneBehavior;
	private JPanel colorchooserPreview;
	private JPanel panel_1;
	private JLabel labelMajourIssues;
	private JLabel labelHightlight;
	private JLabel labelDefaults;
	private JLabel currentLabel;
	private JRadioButton radioButton_labelDefaults;
	private JRadioButton radioButton_labelMajorIssues;
	private JRadioButton radioButton_labelHighlight;
	private JPanel panelColors;
	private JPanel panelButtons;
	private JPanel contentPanel;
	private JPanel panelMain;
	private JPanel panelButtonLeft;
	private JPanel panelRight;
	private JPanel panelDefaults;
	private JPanel panelDirectories;
	private JPanel panelAutosave;

	private JLabel lblDefault;
	private JPanel panel_6;
	//private JTextField txtCell;
//	private JTextField textField;
//	private JTextField textField_1;
	private JLabel lblNewLabel_1;
	private JScrollPane scrollPaneButtonLeft;
	private JPanel panel_5;
	private JButton btnNewButton;
	private JButton btnNewButton_1;
	private JPanel panel_7;
	private JButton btnNewButton_2;
	private JButton btnNewButton_3;
	private JPanel panel_2;
	private JPanel panelFontSize;
	private JScrollPane scrollPaneDefaults;
	private JSlider slider;
	private JLabel lblNewLabel_2;
	
	private MainGui gui;
	private JScrollPane scrollPaneDirectories;
	private JPanel panel_4;
	private JLabel lblDirectoryPath;
	private JTextField textFieldDirectoryAutosave;
	private JButton btnButtonDirectoryAutosave;
	private JPanel panel_8;
	private JPanel panel_11;
	private JLabel lblIWantTo;
	private JSpinner spinner_1;
	private JLabel lblMinutes;
	private JPanel panel_12;
	private JLabel lblDirectoryForBug;
	private JTextField textField_3;
	private JButton btnBrowse;
	private JPanel panel_13;
	private JLabel lblBaseWorkingDirectory;
	private JTextField textField_4;
	private JButton btnBrowse_1;
	private JFileChooser fileChooser;
	private JPanel panel_14;
	private JPanel panel_15;
	private JCheckBox chckbxNewCheckBox;
	private JButton btnNewButton_4;
	private JPanel panel_vtpalette;
	//private JSpinner spinnerAutocompletionDelay;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PreferencesFrame frame = new PreferencesFrame(null);
					//frame.setExpressionAndShow("a+b");
					
					//frame.revalidate();
					frame.pack();
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	@Override
	public void setVisible(boolean b) {
		if(b) {
			GraphicalProperties.resetFonts(this);
			GraphicalProperties.resetFonts(panelColors);
			GraphicalProperties.resetFonts(panelButtons);
			GraphicalProperties.resetFonts(panelDefaults);
			GraphicalProperties.resetFonts(panelDirectories);
			GraphicalProperties.resetFonts(panelAutosave);
			GraphicalProperties.resetFonts(panelFontSize);
			GraphicalProperties.resetFonts(panel_1);
		}
		if(btnNewButton!=null){
			btnNewButton.doClick();
			btnNewButton.setSelected(true);
			btnNewButton_1.setSelected(false);
			btnNewButton_2.setSelected(false);
			btnNewButton_3.setSelected(false);
		}
		pack();
		panelRight.repaint();
		super.setVisible(b);
	}
	
	public PreferencesFrame(MainGui gui) {

		this.setTitle("Preferences...");
		this.setSize(new Dimension(453, 341));

		contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setSize(new Dimension(456, 358));
		this.setContentPane(contentPanel);

		this.gui = gui;
		initComponents();

		btnNewButton.doClick();
		GraphicalProperties.resetFonts(this);
	}
	
	


	private void initComponents() {

		//tabbedPane = new JTabbedPane();
		//tabbedPane.setTabPlacement(JTabbedPane.LEFT);
		//contentPanel.add(tabbedPane, BorderLayout.NORTH);

		panelColors = new JPanel();
		panelColors.setLayout(new BorderLayout(0, 0));
		
		
		//tabbedPane.addTab("Colors", null, panelColors, null);

		panelButtons = new JPanel();
		contentPanel.add(panelButtons, BorderLayout.SOUTH);



		buttonGroup1 = new javax.swing.ButtonGroup();

		/*  // An AutoCompletion acts as a "middle-man" between a text component
	      // and a CompletionProvider. It manages any options associated with
	      // the auto-completion (the popup trigger key, whether to display a
	      // documentation window along with completion choices, etc.). Unlike
	      // CompletionProviders, instances of AutoCompletion cannot be shared
	      // among multiple text components.
	      AutoCompletion ac = new AutoCompletion(Constants.provider);
	      ac.install(defaultCompName);*/

		scrollPaneColorPalette = new JScrollPane();
		scrollPaneColorPalette.setViewportView(panelColors);

		panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "VT palette", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelColors.add(panel_1, BorderLayout.EAST);
		
		panelFontSize = new JPanel();
		panelFontSize.setBorder(new TitledBorder(null, "Fonts", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		FlowLayout flowLayout = (FlowLayout) panelFontSize.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panelColors.add(panelFontSize, BorderLayout.NORTH);
		
		lblNewLabel_2 = new JLabel("Size");
		panelFontSize.add(lblNewLabel_2);
		
		slider = new JSlider();
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setMinorTickSpacing(5);
		slider.setMinimum(5);
		slider.setMaximum(40);
		if(gui!= null) slider.setValue(gui.getCustomFont().getSize());
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(gui!= null) gui.setCustomFont(slider.getValue());
			}
		});
		Hashtable labelTable = new Hashtable();
		for(int i = slider.getMinimum(); i <= slider.getMaximum(); i = i + slider.getMinorTickSpacing() ) {
			labelTable.put( new Integer(i), new JLabel(new Integer(i).toString()) );
		}
		slider.setLabelTable( labelTable );
		
		panelFontSize.add(slider);
		MouseListener pickColorBackground_listener = new MouseListener() {
			public void changeColor(MouseEvent e) {
				Color newColor = ((JComponent)(e.getSource())).getBackground();
				if(newColor!= null) {
					if(currentLabel.getText().contains("Border")) {
						currentLabel.setBorder(new LineBorder(newColor, 3));
					} else {
						currentLabel.setBackground(newColor);
					}
				}
				
			}

			@Override
			public void mouseClicked(MouseEvent e) { changeColor(e);	}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}
		};


		Vector<Color> colors = new Vector<Color>();
		colors.add(Constants.vt_orange);
		colors.add(Constants.vt_maroon);
		colors.add(Constants.vt_red_1);
		colors.add(Constants.vt_red_2);
		colors.add(Constants.vt_red_3);
		colors.add(Constants.vt_red_4);
		colors.add(Constants.vt_gold_1);
		colors.add(Constants.vt_gold_2);
		colors.add(Constants.vt_gold_3);
		colors.add(Constants.vt_gold_4);
		colors.add(Constants.vt_green_1);
		colors.add(Constants.vt_green_2);
		colors.add(Constants.vt_green_3);
		colors.add(Constants.vt_green_4);
		colors.add(Constants.vt_blues_1);
		colors.add(Constants.vt_blues_2);
		colors.add(Constants.vt_blues_3);
		colors.add(Constants.vt_blues_4);
		colors.add(Constants.vt_gray_1);
		colors.add(Constants.vt_gray_2);
		colors.add(Constants.vt_gray_3);
		colors.add(Constants.vt_gray_4);
		colors.add(Constants.vt_cream_1);
		colors.add(Constants.vt_cream_2);
		colors.add(Constants.vt_cream_3);
		colors.add(Constants.vt_cream_4);
		panel_vtpalette = new JPanel();
		panel_1.setLayout(new BorderLayout(3, 3));
		
		for(int i = 0; i < colors.size(); i++) {
			JLabel lblNewLabel = new JLabel("     ");
			lblNewLabel.setOpaque(true);
			lblNewLabel.setBackground(colors.get(i));
			lblNewLabel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			lblNewLabel.addMouseListener(pickColorBackground_listener);
			panel_vtpalette.add(lblNewLabel);
		}
		GridLayout gr2 = new GridLayout();
		gr2.setRows(4);
		gr2.setHgap(3);
		gr2.setVgap(3);
		
		panel_vtpalette.setLayout(gr2);
		
		btnNewButton_4 = new JButton("More Colors...");
		btnNewButton_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showColorChooser();
			}
		});
		panel_1.add(btnNewButton_4, BorderLayout.SOUTH);
		panel_1.add(panel_vtpalette, BorderLayout.CENTER);
		
		panel_1.add(panel_vtpalette);
		




		colorchooserPreview = new JPanel();

		MouseListener pickLabelToChange_listener = new MouseListener() {
			public void mouseClicked(MouseEvent e) { 
				if(e.getSource().equals(labelDefaults)) radioButton_labelDefaults.setSelected(true);
				if(e.getSource().equals(labelMajourIssues)) radioButton_labelMajorIssues.setSelected(true);
				if(e.getSource().equals(labelHightlight)) radioButton_labelHighlight.setSelected(true);
			}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		};

		labelDefaults = new JLabel(" Border cell with Default value ");
		labelDefaults.setBorder(new LineBorder(GraphicalProperties.color_border_defaults, 3));
		labelDefaults.addMouseListener(pickLabelToChange_listener);
		colorchooserPreview.setLayout(new GridLayout(0, 2, 0, 0));
		colorchooserPreview.add(labelDefaults);

		labelMajourIssues = new JLabel(" Background cell with Major Issue ");
		labelMajourIssues.setBackground(GraphicalProperties.color_cell_with_errors);

		buttonGroup2 = new ButtonGroup();
		
		
		labelMajourIssues.setOpaque(true);
		labelMajourIssues.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		labelMajourIssues.addMouseListener(pickLabelToChange_listener);
		
				radioButton_labelDefaults = new JRadioButton("");
				radioButton_labelDefaults.setHorizontalAlignment(SwingConstants.CENTER);
				buttonGroup2.add(radioButton_labelDefaults);
				
						colorchooserPreview.add(radioButton_labelDefaults);
						
						
								radioButton_labelDefaults.addItemListener(new ItemListener() {
									public void itemStateChanged(ItemEvent e) {		if(radioButton_labelDefaults.isSelected()) currentLabel = labelDefaults;	}
								});
								radioButton_labelDefaults.setSelected(false);
		colorchooserPreview.add(labelMajourIssues);

		panelColors.add(colorchooserPreview, BorderLayout.WEST);
		
		
		labelHightlight = new JLabel(" Background Highlighted cell ");
		labelHightlight.setBackground(GraphicalProperties.color_cell_to_highlight);
		labelHightlight.setOpaque(true);
		labelHightlight.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		labelHightlight.addMouseListener(pickLabelToChange_listener);
						
								radioButton_labelMajorIssues = new JRadioButton("");
								radioButton_labelMajorIssues.setHorizontalAlignment(SwingConstants.CENTER);
								buttonGroup2.add(radioButton_labelMajorIssues);
								colorchooserPreview.add(radioButton_labelMajorIssues);
								radioButton_labelMajorIssues.addItemListener(new ItemListener() {
									public void itemStateChanged(ItemEvent e) {		if(radioButton_labelMajorIssues.isSelected()) currentLabel = labelMajourIssues;	}
								});
								radioButton_labelMajorIssues.setSelected(false);
		colorchooserPreview.add(labelHightlight);
				
						radioButton_labelHighlight = new JRadioButton("");
						radioButton_labelHighlight.setHorizontalAlignment(SwingConstants.CENTER);
						buttonGroup2.add(radioButton_labelHighlight);
						colorchooserPreview.add(radioButton_labelHighlight);
						radioButton_labelHighlight.addItemListener(new ItemListener() {
							public void itemStateChanged(ItemEvent e) {		if(radioButton_labelHighlight.isSelected()) currentLabel = labelHightlight;	}
						});
						radioButton_labelHighlight.setSelected(false);
		//colorchooser.setPreviewPanel(colorchooserPreview);

		

		jButtonRestoreDefaults = new JButton("Restore original Defaults");
		panelButtons.add(jButtonRestoreDefaults);
		jButtonOK = new JButton("OK");
		panelButtons.add(jButtonOK);
		jButtonOK.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					MainGui.updateFunctionsView();
					Vector usage = MainGui.search(defaultCompName.getText());
					MainGui.revalidateExpressions(usage);
					usage = MainGui.search(defaultGlobalQValue.getText());
					MainGui.revalidateExpressions(usage);
					usage = MainGui.search(defaultSpeciesInitialValue.getText());
					MainGui.revalidateExpressions(usage);
					usage = MainGui.search(defaultCompartmentInitialValue.getText());
					MainGui.revalidateExpressions(usage);
					GraphicalProperties.color_border_defaults = ((LineBorder)(labelDefaults.getBorder())).getLineColor();
					GraphicalProperties.color_cell_to_highlight = labelHightlight.getBackground();
					GraphicalProperties.color_cell_with_errors = labelMajourIssues.getBackground();
				} catch (Throwable e1) {
					if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e1.printStackTrace();
				} finally {
					setCursor(null);
				}
				savePreferencesToFile();

				setVisible(false);
				
			}


		});

		jButtonRestoreDefaults.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				defaultGlobalQValue.setText(Constants.DEFAULT_GLOBALQ_INITIAL_VALUE);
				defaultCompName.setText(Constants.DEFAULT_COMPARTMENT_NAME);
				defaultSpeciesInitialValue.setText(Constants.DEFAULT_SPECIES_INITIAL_VALUE);
				defaultCompartmentInitialValue.setText(Constants.DEFAULT_COMPARTMENT_INITIAL_VALUE);
				labelHightlight.setBackground(Constants.DEFAULT_COLOR_HIGHLIGHT);
				labelDefaults.setBorder(new LineBorder(Constants.DEFAULT_COLOR_DEFAULTS, 3));
				labelMajourIssues.setBackground(Constants.DEFAULT_COLOR_ERRORS);
				panelBehavior.revalidate();
				colorchooserPreview.revalidate();
			}
		});
		contentPanel.add(panelButtons, BorderLayout.SOUTH);
		
		scrollPaneDefaults = new JScrollPane();
		panelDefaults = new JPanel();
		scrollPaneDefaults.setViewportView(panelDefaults);
		panelDefaults.setLayout(new BorderLayout(0, 0));

		panel_2 = new JPanel();
		panelDefaults.add(panel_2, BorderLayout.NORTH);
		BoxLayout b = new BoxLayout(panel_2, BoxLayout.X_AXIS);
		panel_2.setLayout(b);
		
		lblDefault = new JLabel("Default");
		panel_2.add(Box.createHorizontalStrut(10));
		panel_2.add(lblDefault);
		panel_2.add(Box.createHorizontalStrut(10));
		//panelButtons.add(scrollPaneDefaults);
		defaultCompName = new JTextField(MainGui.compartment_default_for_dialog_window);

		defaultCompName.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				updateDefault();
			}
			public void removeUpdate(DocumentEvent e) {
				updateDefault();
			}
			public void insertUpdate(DocumentEvent e) {
				updateDefault();
			}

			public void updateDefault() {
				String s = defaultCompName.getText();
				MainGui.compartment_default_for_dialog_window = s;
		
				//MOVE THOSE COMMANDS AFTER THE USER CLICK OK!!!
				MainGui.updateDefaultValue(Constants.TitlesTabs.SPECIES.getDescription(), Constants.SpeciesColumns.COMPARTMENT.index, MainGui.compartment_default_for_dialog_window);
				MainGui.updateDefaultValue(Constants.TitlesTabs.COMPARTMENTS.getDescription(), Constants.CompartmentsColumns.NAME.index, MainGui.compartment_default_for_dialog_window);
			}
		});

		defaultGlobalQValue = new JTextField(MainGui.globalQ_defaultValue_for_dialog_window);
		defaultGlobalQValue.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				updateDefault();
			}
			public void removeUpdate(DocumentEvent e) {
				updateDefault();
			}
			public void insertUpdate(DocumentEvent e) {
				updateDefault();
			}

			public void updateDefault() {
				MainGui.globalQ_defaultValue_for_dialog_window = defaultGlobalQValue.getText();
				MainGui.updateDefaultValue(Constants.TitlesTabs.GLOBALQ.getDescription(), Constants.GlobalQColumns.VALUE.index, MainGui.globalQ_defaultValue_for_dialog_window);
			}
		});

		defaultSpeciesInitialValue = new JTextField(MainGui.species_defaultInitialValue);
		defaultSpeciesInitialValue.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				updateDefault();
			}
			public void removeUpdate(DocumentEvent e) {
				updateDefault();
			}
			public void insertUpdate(DocumentEvent e) {
				updateDefault();
			}

			public void updateDefault() {
				new String(defaultSpeciesInitialValue.getText());
				MainGui.species_defaultInitialValue = defaultSpeciesInitialValue.getText

						();
				MainGui.updateDefaultValue(Constants.TitlesTabs.SPECIES.getDescription(), 

						Constants.SpeciesColumns.INITIAL_QUANTITY.index, MainGui.species_defaultInitialValue);
			}
		});


		defaultCompartmentInitialValue = new JTextField(MainGui.compartment_defaultInitialValue);
		defaultCompartmentInitialValue.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				updateDefault();
			}
			public void removeUpdate(DocumentEvent e) {
				updateDefault();
			}
			public void insertUpdate(DocumentEvent e) {
				updateDefault();
			}

			public void updateDefault() {
				MainGui.compartment_defaultInitialValue = 

						defaultCompartmentInitialValue.getText();
				MainGui.updateDefaultValue(Constants.TitlesTabs.COMPARTMENTS.getDescription(), 

						Constants.CompartmentsColumns.INITIAL_SIZE.index, MainGui.compartment_defaultInitialValue);
			}
		});
		
		
		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		panel_6 = new JPanel();
		panel_2.add(panel_6);
		GridLayout gr = new GridLayout(0, 2, 0, 0);
		gr.setVgap(10);
		gr.setHgap(2);
		panel_6.setLayout(gr);

		JLabel jLabel3 = new JLabel("Species initial value:");
		panel_6.add(jLabel3);

		//defaultSpeciesInitialValue = new JTextField();
		panel_6.add(defaultSpeciesInitialValue);
		defaultSpeciesInitialValue.setColumns(10);

		JLabel jLabel2 = new JLabel("Global Quantity initial value:");
		panel_6.add(jLabel2);

		//textField = new JTextField();
		panel_6.add(defaultGlobalQValue);
		defaultGlobalQValue.setColumns(10);

		JLabel lblCompartmentName = new JLabel("Compartment name:");
		panel_6.add(lblCompartmentName);

		//textField_1 = new JTextField();
		panel_6.add(defaultCompName);
		defaultCompName.setColumns(10);

		lblNewLabel_1 = new JLabel("Compartment size:");
		panel_6.add(lblNewLabel_1);

		//	textField_2 = new JTextField();
		panel_6.add(defaultCompartmentInitialValue);
		defaultCompartmentInitialValue.setColumns(10);
		//tabbedPane.addTab("Colors", null, scrollPaneColorPalette, null);

		panelMain = new JPanel();
		contentPanel.add(panelMain, BorderLayout.CENTER);
		panelMain.setBackground(Color.RED);
		panelMain.setLayout(new BorderLayout(0, 0));

		panelButtonLeft = new JPanel();
		panelButtonLeft.setBackground(Color.MAGENTA);
		panelMain.add(panelButtonLeft, BorderLayout.WEST);
		panelButtonLeft.setLayout(new BorderLayout(0, 0));

		scrollPaneButtonLeft = new JScrollPane();
		panelButtonLeft.add(scrollPaneButtonLeft);

		panel_5 = new JPanel();
		scrollPaneButtonLeft.setViewportView(panel_5);
		panel_5.setLayout(new GridLayout(0, 1, 0, 0));

		btnNewButton = new JButton("General behavior");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnNewButton.setSelected(true);
				btnNewButton_1.setSelected(false);
				btnNewButton_2.setSelected(false);
				btnNewButton_3.setSelected(false);
				panelRight.removeAll();
				panelRight.add(scrollPaneBehavior, BorderLayout.CENTER);
				panelRight.revalidate();
				Component c = panelRight.getParent();
				while(true) {
					if(c instanceof JDialog) break;
					c = c.getParent();
				}
				((JDialog)c).pack();
				panelRight.repaint();
			}
		});
		panel_5.add(btnNewButton);

		btnNewButton_1 = new JButton("Default values");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnNewButton.setSelected(false);
				btnNewButton_1.setSelected(true);
				btnNewButton_2.setSelected(false);
				btnNewButton_3.setSelected(false);
				panelRight.removeAll();
				panelRight.add(scrollPaneDefaults, BorderLayout.CENTER);
				panelRight.revalidate();
				Component c = panelRight.getParent();
				while(true) {
					if(c instanceof JDialog) break;
					c = c.getParent();
				}
				((JDialog)c).pack();
			}
		});
		panel_5.add(btnNewButton_1);
		
		btnNewButton_3 = new JButton("Autosave");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnNewButton.setSelected(false);
				btnNewButton_1.setSelected(false);
				btnNewButton_2.setSelected(false);
				btnNewButton_3.setSelected(true);
				panelRight.removeAll();
				panelRight.add(scrollPaneDirectories, BorderLayout.CENTER);
				panelRight.revalidate();
				Component c = panelRight.getParent();
				while(true) {
					if(c instanceof JDialog) break;
					c = c.getParent();
				}
				((JDialog)c).pack();
			}
		});
		panel_5.add(btnNewButton_3);
		
		btnNewButton_2 = new JButton("Fonts and Colors");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnNewButton.setSelected(false);
				btnNewButton_1.setSelected(false);
				btnNewButton_2.setSelected(true);
				btnNewButton_3.setSelected(false);
				panelRight.removeAll();
				panelRight.add(scrollPaneColorPalette, BorderLayout.CENTER);
				panelRight.revalidate();
				Component c = panelRight.getParent();
				while(true) {
					if(c instanceof JDialog) break;
					c = c.getParent();
				}
				((JDialog)c).pack();

			}
		});
		panel_5.add(btnNewButton_2);
		
		scrollPaneDirectories = new JScrollPane();
		
		panelDirectories = new JPanel();
		scrollPaneDirectories.setViewportView(panelDirectories);
		panelDirectories.setLayout(new BorderLayout(0, 0));
		
	
		panel_7 = new JPanel();
		panelDirectories.add(panel_7, BorderLayout.CENTER);
		
		panel_14 = new JPanel();
		panel_7.add(panel_14);
		panel_14.setLayout(new BorderLayout(0, 0));
		
		panelAutosave = new JPanel();
		panel_14.add(panelAutosave);
		panelAutosave.setBorder(new TitledBorder(null, "Autosave", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelAutosave.setLayout(new GridLayout(0, 1, 0, 5));
		
		panel_8 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel_8.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		panelAutosave.add(panel_8);
		
		lblDirectoryPath = new JLabel("Directory path:");
		panel_8.add(lblDirectoryPath);
		
		textFieldDirectoryAutosave = new JTextField();
		textFieldDirectoryAutosave.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				updateAutosavePath();
			}
			public void removeUpdate(DocumentEvent e) {
				updateAutosavePath();
			}
			public void insertUpdate(DocumentEvent e) {
				updateAutosavePath();
			
			}
			private void updateAutosavePath() {
				gui.setAutosaveDirectory(textFieldDirectoryAutosave.getText());
			}

			
		});
		panel_8.add(textFieldDirectoryAutosave);
		textFieldDirectoryAutosave.setColumns(20);
		
		btnButtonDirectoryAutosave = new JButton("Browse...");
		btnButtonDirectoryAutosave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 int returnVal = fileChooser.showOpenDialog(null);
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     File file = fileChooser.getSelectedFile();
                     textFieldDirectoryAutosave.setText(file.getAbsolutePath());
                 }
			}
		});
		panel_8.add(btnButtonDirectoryAutosave);
		
		panel_11 = new JPanel();
		panelAutosave.add(panel_11);
		
		lblIWantTo = new JLabel("I want to save every");
		panel_11.add(lblIWantTo);
		
		spinner_1 = new JSpinner();
		spinner_1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				gui.setAutosaveTimeMin((Integer)spinner_1.getValue());
			}
		});
		spinner_1.setModel(new SpinnerNumberModel(5, 1, 30, 1));
		panel_11.add(spinner_1);
		
		lblMinutes = new JLabel("minutes.");
		panel_11.add(lblMinutes);
		
		panel_15 = new JPanel();
		FlowLayout flowLayout_6 = (FlowLayout) panel_15.getLayout();
		flowLayout_6.setAlignment(FlowLayout.LEFT);
		panel_14.add(panel_15, BorderLayout.NORTH);
		
		chckbxNewCheckBox = new JCheckBox("Activate Autosave");
		chckbxNewCheckBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				boolean sel = chckbxNewCheckBox.isSelected();
				panelAutosave.setEnabled(sel);
				textFieldDirectoryAutosave.setEnabled(sel);
				btnButtonDirectoryAutosave.setEnabled(sel);
				lblDirectoryPath.setEnabled(sel);
				lblMinutes.setEnabled(sel);
				lblIWantTo.setEnabled(sel);
				spinner_1.setEnabled(sel);
				
				panelAutosave.revalidate();
				panelAutosave.repaint();
				if(gui!=null)gui.setAutosaveActive(sel);
			}
		});
		chckbxNewCheckBox.setSelected(true);
		panel_15.add(chckbxNewCheckBox);

		panelRight = new JPanel();
		panelMain.add(panelRight, BorderLayout.CENTER);

		panelBehavior = new javax.swing.JPanel();
		jPanel1 = new javax.swing.JPanel();
		jCheckBoxAutocomplete = new javax.swing.JCheckBox();
		jCheckBoxDialogWindow = new javax.swing.JCheckBox();
		jCheckBoxHighlightCellOpenIssues = new javax.swing.JCheckBox();
	//	jCheckBoxPopUpWarnings = new javax.swing.JCheckBox();
	//	jCheckBoxShowAllAvailableFunctions = new javax.swing.JCheckBox();
		jPanel2 = new javax.swing.JPanel();
		jRadioButton1 = new javax.swing.JRadioButton();
		jRadioButton2 = new javax.swing.JRadioButton();
		jRadioButton3 = new javax.swing.JRadioButton();

		jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Customize software behavior", 

				javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION)); // NOI18N


		jCheckBoxAutocomplete.setText("Autocomplete related spreadsheets and cells");
		jCheckBoxAutocomplete.setName("jCheckBoxAutocomplete");

		jCheckBoxDialogWindow.setText("Show pop-up when Species are defined by the tool"); 
		
		/*jCheckBoxShowAllAvailableFunctions.setText("Show all available functions"); 
		jCheckBoxShowAllAvailableFunctions.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				try {
					updateStatusAllAvailableFunctions();
				} catch (Exception e1) {
					if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e1.printStackTrace();
				}

			}
		});
		jCheckBoxShowAllAvailableFunctions.setSelected(true);
		 */
		
		jCheckBoxAutocomplete.setSelected(MainGui.autocompleteWithDefaults);
		jCheckBoxAutocomplete.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent e) {
				updateStatusAutocomplete();
			}
		});

		jCheckBoxDialogWindow.setSelected(MainGui.show_defaults_dialog_window);
		jCheckBoxDialogWindow.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent e) {
				updateStatusDialogWindow();
			}
		});

		jCheckBoxHighlightCellOpenIssues.setText("Color cells with minor issues");
		jCheckBoxHighlightCellOpenIssues.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent e) {
				if(jCheckBoxHighlightCellOpenIssues.isSelected()) {
						GraphicalProperties.color_cell_with_minorIssues = GraphicalProperties.color_cell_with_errors;
				} else {
					GraphicalProperties.color_cell_with_minorIssues = null;
				}
			}
		});
		/*jCheckBoxPopUpWarnings.setText("Show pop-up windows for warning messages");
		jCheckBoxPopUpWarnings.setEnabled(false);*/
		
		//JLabel lblNewLabel_3 = new JLabel("Delay autocompletion pop-up (in ms)");
		
		/*spinnerAutocompletionDelay = new JSpinner();
		spinnerAutocompletionDelay.setModel(new SpinnerNumberModel(0, 0, 600000, 100));
		if(MainGui.delayAutocompletion == Integer.MAX_VALUE) spinnerAutocompletionDelay.setValue(((SpinnerNumberModel)spinnerAutocompletionDelay.getModel()).getMaximum());
		spinnerAutocompletionDelay.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				MainGui.delayAutocompletion = (int) spinnerAutocompletionDelay.getValue();
			}
		});*/
		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1Layout.setHorizontalGroup(
			jPanel1Layout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
					.addContainerGap()
					.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
						.addComponent(jCheckBoxAutocomplete)
						.addComponent(jCheckBoxDialogWindow)
						.addGroup(jPanel1Layout.createSequentialGroup()
							.addComponent(jCheckBoxHighlightCellOpenIssues)
							.addPreferredGap(ComponentPlacement.RELATED)
							//.addComponent(spinnerAutocompletionDelay, GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
							)
							)
					.addGap(6))
		);
		jPanel1Layout.setVerticalGroup(
			jPanel1Layout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
					.addComponent(jCheckBoxAutocomplete)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(jCheckBoxDialogWindow)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(jCheckBoxHighlightCellOpenIssues)
						//.addComponent(spinnerAutocompletionDelay, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		jPanel1.setLayout(jPanel1Layout);

		jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Renamig options", 

				javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION)); // NOI18N
		jPanel2.setName("jPanel2"); // NOI18N

		buttonGroup1.add(jRadioButton1);


		jRadioButton1.setText(Constants.RENAMING_OPTION_ALL_STRING); 

		buttonGroup1.add(jRadioButton2);
		jRadioButton2.setText(Constants.RENAMING_OPTION_CUSTOM_STRING); 
		buttonGroup1.add(jRadioButton3);
		jRadioButton3.setText(Constants.RENAMING_OPTION_NONE_STRING); 


		jRadioButton1.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				renamingOptionChanged(Constants.RENAMING_OPTION_ALL);
			}
		});

		jRadioButton2.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				renamingOptionChanged(Constants.RENAMING_OPTION_CUSTOM);
			}
		});

		jRadioButton3.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				renamingOptionChanged(Constants.RENAMING_OPTION_NONE);
			}
		});
		panelRight.setLayout(new BorderLayout(0, 0));




		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(
				jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jRadioButton1)
								.addComponent(jRadioButton2)
								.addComponent(jRadioButton3)
								)
								.addContainerGap())
				);
		jPanel2Layout.setVerticalGroup(
				jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup()
						.addComponent(jRadioButton1)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jRadioButton2)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jRadioButton3)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addContainerGap(25, Short.MAX_VALUE))
				);
		//tabbedPane.addTab("General", mainPanel);
		scrollPaneBehavior = new JScrollPane();
		//panelRight.add(scrollPaneDirectories, BorderLayout.CENTER);
		panelRight.add(scrollPaneBehavior, BorderLayout.CENTER);
		scrollPaneBehavior.setViewportView(panelBehavior);

		panelBehavior.setLayout(new BorderLayout(0, 0));
		panelBehavior.add(jPanel1, BorderLayout.CENTER);
		panelBehavior.add(jPanel2, BorderLayout.SOUTH);
		switch (MainGui.renamingOption) {
		case Constants.RENAMING_OPTION_ALL:
			jRadioButton1.setSelected(true);
			break;
		case Constants.RENAMING_OPTION_CUSTOM:
			jRadioButton2.setSelected(true);
			break;
		case Constants.RENAMING_OPTION_NONE:
			jRadioButton3.setSelected(true);
			break;
		default:
			break;
		}
		this.setModal(true);
		this.pack();
		this.setLocationRelativeTo(null);
	}
	

	protected void showColorChooser() {
		Color initial = null;
		if(currentLabel!= null) {
			if(currentLabel.getText().contains("Border")) {
				initial = ((LineBorder)(currentLabel.getBorder())).getLineColor();
			} else {
				initial = currentLabel.getBackground();
			}
		}
		Color newColor = JColorChooser.showDialog(this, "Color chooser", initial);
		if(newColor!= null) {
			if(currentLabel.getText().contains("Border")) {
				currentLabel.setBorder(new LineBorder(newColor, 3));
			} else {
				currentLabel.setBackground(newColor);
			}
		}
		
	}


	protected void renamingOptionChanged(int i) {
		MainGui.renamingOption = i;

	}

	


	public void updateStatusAutocomplete() {
		if(jCheckBoxAutocomplete.isSelected()) MainGui.autocompleteWithDefaults = true;
		else {
			MainGui.autocompleteWithDefaults = false;
			this.jCheckBoxDialogWindow.setSelected(false);
		}
	}


		/*public void updateStatusAllAvailableFunctions() throws Exception {
		if(jCheckBoxShowAllAvailableFunctions.isSelected()) MainGui.showAllAvailableFunctions = true;
		else {
			MainGui.showAllAvailableFunctions = false;
		}
		MainGui.updateFunctionsView();
	}*/

	public void updateStatusDialogWindow() {
		if(jCheckBoxDialogWindow.isSelected()) {
			this.jCheckBoxAutocomplete.setSelected(true);
			MainGui.show_defaults_dialog_window = true;
		}
		else MainGui.show_defaults_dialog_window = false;
	}

	public void setCheckboxDialogWindowForDefaults(boolean b) {
		jCheckBoxDialogWindow.setSelected(b);
	}


	public void extractPreferences(Vector<String> pref) {
		if(pref.size() == 0) return;
		for(int i = 0; i < pref.size(); i++) {
			String element = pref.get(i);
			StringTokenizer st = new StringTokenizer(element, Constants.Preferences.SEPARATOR.getDescription());
			String name = st.nextToken().trim();
			String value = new String();
			if(st.hasMoreTokens())  value = st.nextToken();
			
			
			if(name.compareTo(Constants.Preferences.AUTOCOMPLETE.getDescription())==0) {
				if(value.compareTo(Constants.Preferences.CHECKED.getDescription())==0) jCheckBoxAutocomplete.setSelected(true);
				else jCheckBoxAutocomplete.setSelected(false);
				continue;
			} 


/*			if(name.compareTo(Constants.Preferences.SHOW_ALL_FUNCTIONS.getDescription())==0) {
				if(value.compareTo(Constants.Preferences.CHECKED.getDescription())==0) jCheckBoxShowAllAvailableFunctions.setSelected(true);
				else jCheckBoxShowAllAvailableFunctions.setSelected(false);
				continue;
			} */

			/*if(name.compareTo(Constants.Preferences.AUTOCOMPLETION_DELAY.getDescription())==0) {
				try{
					Integer val = new Integer(value);
					Integer max = new Integer((Integer) ((SpinnerNumberModel)spinnerAutocompletionDelay.getModel()).getMaximum());
					if(val > max) val = max;
					spinnerAutocompletionDelay.setValue(val);
				} catch (Exception e) {
					if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) 
						e.printStackTrace();
				} finally {
					continue;
				}
			} */

			if(name.compareTo(Constants.Preferences.COMP_NAME.getDescription())==0) {
				defaultCompName.setText(value);
				continue;
			} 
			if(name.compareTo(Constants.Preferences.INITIAL_COMP_SIZE.getDescription())==0) {
				defaultCompartmentInitialValue.setText(value);
				continue;
			}
			if(name.compareTo(Constants.Preferences.INITIAL_GLOBALQ_VALUE.getDescription())==0) {
				defaultGlobalQValue.setText(value);
				continue;
			}
			if(name.compareTo(Constants.Preferences.INITIAL_SPECIES_VALUE.getDescription())==0) {
				defaultSpeciesInitialValue.setText(value);
				continue;
			}
			if(name.compareTo(Constants.Preferences.POPUP_AUTOCOMPLETE.getDescription())==0) {
				if(value.compareTo(Constants.Preferences.CHECKED.getDescription())==0) jCheckBoxDialogWindow.setSelected(true);
				else jCheckBoxDialogWindow.setSelected(false);
				continue;
			} 
			if(name.compareTo(Constants.Preferences.RENAMING.getDescription())==0) {
				if(value.compareTo(Constants.Preferences.RENAME_AUTO.getDescription())==0) jRadioButton1.setSelected(true);
				else if(value.compareTo(Constants.Preferences.RENAME_CUSTOM.getDescription())==0) jRadioButton2.setSelected(true);
				else if(value.compareTo(Constants.Preferences.RENAME_NONE.getDescription())==0) jRadioButton3.setSelected(true);
				continue;
			} 

			if(name.compareTo(Constants.Preferences.COLOR_DEFAULTS.getDescription())==0) {
				labelDefaults.setBorder(new LineBorder(new Color(Integer.parseInt(value),true), 3));
				GraphicalProperties.color_border_defaults = new Color(Integer.parseInt(value),true);
				continue;
			} 

			if(name.compareTo(Constants.Preferences.COLOR_HIGHLIGHT.getDescription())==0) {
				labelHightlight.setBackground(new Color(Integer.parseInt(value),true));
				GraphicalProperties.color_cell_to_highlight = new Color(Integer.parseInt(value),true);
				continue;
			} 

			if(name.compareTo(Constants.Preferences.COLOR_MAJOR.getDescription())==0) {
				labelMajourIssues.setBackground(new Color(Integer.parseInt(value),true));
				GraphicalProperties.color_cell_with_errors = new Color(Integer.parseInt(value),true);
				continue;
			} 
			
			if(name.compareTo(Constants.Preferences.COLOR_MINOR.getDescription())==0) {
				if(value.compareTo("null")==0) {
					jCheckBoxHighlightCellOpenIssues.setSelected(false);
					GraphicalProperties.color_cell_with_minorIssues =null;
					} else {
						GraphicalProperties.color_cell_with_minorIssues =GraphicalProperties.color_cell_with_errors;
						jCheckBoxHighlightCellOpenIssues.setSelected(true);
					}
				continue;
			} 
			
			if(name.compareTo(Constants.Preferences.FONT_SIZE.getDescription())==0) {
				slider.setValue(Integer.parseInt(value));
				GraphicalProperties.customFont = GraphicalProperties.customFont.deriveFont(Float.parseFloat(value));
				continue;
			} 
			if(name.compareTo(Constants.Preferences.AUTOSAVE_PATH.getDescription())==0) {
				textFieldDirectoryAutosave.setText(value);
				gui.setAutosaveDirectory(value);
				continue;
			} 
			if(name.compareTo(Constants.Preferences.AUTOSAVE_TIME.getDescription())==0) {
				spinner_1.setValue(Integer.parseInt(value));
				gui.setAutosaveTimeMin(Integer.parseInt(value));
				continue;
			} 
			if(name.compareTo(Constants.Preferences.AUTOSAVE_ACTIVE.getDescription())==0) {
				chckbxNewCheckBox.setSelected(Boolean.parseBoolean(value));
				gui.setAutosaveActive(Boolean.parseBoolean(value));
				continue;
			} 
			
		}
	}

	void savePreferencesToFile() {
		BufferedWriter out; 
		try {
			out = new BufferedWriter(new FileWriter(MainGui.file_preferences));

			out.write(Constants.Preferences.AUTOCOMPLETE.getDescription()+Constants.Preferences.SEPARATOR.getDescription());
			if(jCheckBoxAutocomplete.isSelected()) out.write(Constants.Preferences.CHECKED.getDescription());
			else out.write(Constants.Preferences.UNCHECKED.getDescription());
			out.write(System.getProperty("line.separator"));

			out.write(Constants.Preferences.COMP_NAME.getDescription()+Constants.Preferences.SEPARATOR.getDescription());
			out.write(defaultCompName.getText());
			out.write(System.getProperty("line.separator"));

			out.write(Constants.Preferences.INITIAL_COMP_SIZE.getDescription()+Constants.Preferences.SEPARATOR.getDescription());
			out.write(defaultCompartmentInitialValue.getText());
			out.write(System.getProperty("line.separator"));

			out.write(Constants.Preferences.INITIAL_GLOBALQ_VALUE.getDescription()+Constants.Preferences.SEPARATOR.getDescription());
			out.write(defaultGlobalQValue.getText());
			out.write(System.getProperty("line.separator"));

			out.write(Constants.Preferences.INITIAL_SPECIES_VALUE.getDescription()+Constants.Preferences.SEPARATOR.getDescription());
			out.write(defaultSpeciesInitialValue.getText());
			out.write(System.getProperty("line.separator"));

			out.write(Constants.Preferences.POPUP_AUTOCOMPLETE.getDescription()+Constants.Preferences.SEPARATOR.getDescription());
			if(jCheckBoxDialogWindow.isSelected()) out.write(Constants.Preferences.CHECKED.getDescription());
			else out.write(Constants.Preferences.UNCHECKED.getDescription());
			out.write(System.getProperty("line.separator"));
			
			/*	out.write(Constants.Preferences.AUTOCOMPLETION_DELAY.getDescription()+Constants.Preferences.SEPARATOR.getDescription());
			out.write(new String(new Integer(MainGui.delayAutocompletion).toString()));
			out.write(System.getProperty("line.separator"));

			out.write(Constants.Preferences.SHOW_ALL_FUNCTIONS.getDescription()+Constants.Preferences.SEPARATOR.getDescription());
			if(jCheckBoxShowAllAvailableFunctions.isSelected()) out.write(Constants.Preferences.CHECKED.getDescription());
			else out.write(Constants.Preferences.UNCHECKED.getDescription());
			out.write(System.getProperty("line.separator"));
*/
			out.write(Constants.Preferences.RENAMING.getDescription()+Constants.Preferences.SEPARATOR.getDescription());
			if(jRadioButton1.isSelected())	out.write(Constants.Preferences.RENAME_AUTO.getDescription());
			else if(jRadioButton2.isSelected())	out.write(Constants.Preferences.RENAME_CUSTOM.getDescription());
			else if(jRadioButton3.isSelected())	out.write(Constants.Preferences.RENAME_NONE.getDescription());
			out.write(System.getProperty("line.separator"));

			out.write(Constants.Preferences.COLOR_DEFAULTS.getDescription()+Constants.Preferences.SEPARATOR.getDescription());
			out.write(new Integer(GraphicalProperties.color_border_defaults.getRGB()).toString());
			out.write(System.getProperty("line.separator"));

			out.write(Constants.Preferences.COLOR_MINOR.getDescription()+Constants.Preferences.SEPARATOR.getDescription());
			if(GraphicalProperties.color_cell_with_minorIssues==null)out.write("null");
			else out.write(new Integer(GraphicalProperties.color_cell_with_minorIssues.getRGB()).toString());
			out.write(System.getProperty("line.separator"));

			out.write(Constants.Preferences.COLOR_MAJOR.getDescription()+Constants.Preferences.SEPARATOR.getDescription());
			out.write(new Integer(GraphicalProperties.color_cell_with_errors.getRGB()).toString());
			out.write(System.getProperty("line.separator"));

			out.write(Constants.Preferences.COLOR_HIGHLIGHT.getDescription()+Constants.Preferences.SEPARATOR.getDescription());
			out.write(new Integer(GraphicalProperties.color_cell_to_highlight.getRGB()).toString());
			out.write(System.getProperty("line.separator"));
			
			out.write(Constants.Preferences.FONT_SIZE.getDescription()+Constants.Preferences.SEPARATOR.getDescription());
			out.write(new Integer(GraphicalProperties.customFont.getSize()).toString());
			out.write(System.getProperty("line.separator"));
			
			out.write(Constants.Preferences.AUTOSAVE_ACTIVE.getDescription()+Constants.Preferences.SEPARATOR.getDescription());
			out.write(new Boolean(gui.isAutosaveActive()).toString());
			out.write(System.getProperty("line.separator"));
			
			out.write(Constants.Preferences.AUTOSAVE_PATH.getDescription()+Constants.Preferences.SEPARATOR.getDescription());
			out.write(gui.getAutosaveDirectory());
			out.write(System.getProperty("line.separator"));
			
			out.write(Constants.Preferences.AUTOSAVE_TIME.getDescription()+Constants.Preferences.SEPARATOR.getDescription());
			out.write(new Integer(gui.getAutosaveTimeMin()).toString());
			out.write(System.getProperty("line.separator"));

			out.flush();
			out.close();
		}
		catch (Exception e) {
			System.err.println("Trouble writing Preferences File: "+ e);
			if(MainGui.DEBUG_SHOW_PRINTSTACKTRACES) e.printStackTrace();
		}
	}
}

