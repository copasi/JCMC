package msmb.runManager;
import java.awt.event.ActionEvent;

import msmb.commonUtilities.MSMB_MenuItem;
import msmb.gui.ExportMultistateFormat;
import msmb.parsers.mathExpression.MR_Expression_Parser;
import msmb.parsers.mathExpression.MR_Expression_ParserConstantsNOQUOTES;
import msmb.parsers.mathExpression.syntaxtree.CompleteExpression;
import msmb.parsers.mathExpression.visitor.EvaluateExpressionVisitor;
import msmb.runManager.ProgressBarFrame;
import msmb.runManager.ChangeLinePlotFrame.MyShapes;

import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import org.COPASI.CCopasiParameter;
import org.COPASI.CModel;
import org.COPASI.CTimeSeries;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.painter.*;

import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.Renderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import msmb.gui.MainGui;
import msmb.model.MultiModel;
import msmb.utility.Constants;
import msmb.utility.GraphicalProperties;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.graph.JGraphSimpleLayout;
import com.jgraph.layout.graph.JGraphSpringLayout;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;
import com.jgraph.layout.organic.JGraphFastOrganicLayout;
import com.jgraph.layout.organic.JGraphOrganicLayout;
import com.jgraph.layout.organic.JGraphSelfOrganizingOrganicLayout;
import com.jgraph.layout.tree.JGraphCompactTreeLayout;
import com.jgraph.layout.tree.JGraphRadialTreeLayout;
import com.jgraph.layout.tree.JGraphTreeLayout;
import javax.swing.JTabbedPane;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.JList;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.JLabel;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.GlossPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.PinstripePainter;
import org.jfree.chart.axis.*;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.chart.*;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.ShapeUtilities;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.CellHandle;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphContext;
import org.jgraph.graph.GraphUndoManager;
import org.jgraph.graph.Port;
import org.jgraph.graph.PortView;
import org.jgrapht.ext.JGraphModelAdapter;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class RunManager  extends JFrame {
	public static final Color colorCumulativeRedefinition = Color.green;
	public static final Color colorLocalRedefinition = Color.cyan;
	public static final Color colorBaseSet = Color.white;
	
	static int newMutantIndex = 0;
	static String defaultViewName = "**initial**";

	JGraph jgraph_graph_parameters;
	protected JGraphLayoutMorphingManager morpher = new JGraphLayoutMorphingManager();
	private JGraphModelAdapter m_jgAdapter;
	private DefaultListModel listModel_mutants;
	private AbstractAction remove;
	private SingleMutantFrame singleMutantFrame;
	private JSplitPane splitPaneGraph_parameters;

	private JPanel jpanelParameterLists;
	
	
	private JPanel jPanelAnalysis1;
	JGraph jgraph_graph_analysis1;
	protected JGraphLayoutMorphingManager morpher_analysis1 = new JGraphLayoutMorphingManager();
	private JGraphModelAdapter m_jgAdapter_analysis1;
	private DefaultListModel listModel_analysis1;
	private JSplitPane splitPaneGraph_analysis1;
	private AbstractAction remove_analysis1;
	
	private JPanel jPanelPlot;
	JGraph jgraph_graph_plot;
	protected JGraphLayoutMorphingManager morpher_plot = new JGraphLayoutMorphingManager();
	private JGraphModelAdapter m_jgAdapter_plot;
	private DefaultListModel listModel_plot;
	private JSplitPane splitPaneGraph_plot;
	private AbstractAction remove_plot;
	
	
	String MUTANT_BASIC = new String("MutantBasicShape");
	String SIMULATION_BASIC =  new String("SimulationBasicShape");
	String PLOT_BASIC =  new String("PlotBasicShape");
	
	private static MutantsDB mutantsDB;
	private SimulationsDB simDB;
	private RMPlotDB plotDB;
	private Hashtable basicCell_attributeMap;
	

	private MultiModel multiModel;
	private SingleSimulationFrame singleAnalysis1Frame;
	private JTabbedPane jTabRM;
	protected boolean onSelectedCellsMutants = false;
	protected boolean onSelectedCellsAnalysis1 = false;
	protected boolean onSelectedCellsPlot = false;
	private JPanel panel_plots;
	private GridLayout gridlayout_plots;
	private JScrollPane scrollPane_results;
	private ProgressBarFrame progressBarFrame;
	private Thread progressBarThread;
	private JFrame mainGui;
	private HashMap<XYPlot, HashMap<String, PlottedVariable>> plotProperties;
	private SinglePlotAddTimeSeriesFrame addTimeSeriesFrame;
	private ChangeIntervalPlotFrame changeIntervalPlotFrame;
	private ChangeLinePlotFrame changeLinePlotFrame;
	private ChangeChartPropertiesFrame changeChartPropertiesFrame;
	private SelectSimulationToRunFrame selectSimulationToRunFrame;
	private HashSet<JFreeChart> chartForExport;
	HashBiMap<JFrame, JMenuItem> separateWindows = HashBiMap.create();
	private JMenu windowMenu;
	private JMenuItem menuItemCloseAllSeparateWindows;
	
	public static GraphUndoManager undoManager;
	public static GraphUndoManager undoManagerAnalysis1;
	public static boolean notUndoableAction = false;
	
	
	public RunManager() {
		setTitle("MSMB - RM (Run Manager)");

		 plotProperties = new HashMap<XYPlot, HashMap<String, PlottedVariable>>();
		 chartForExport = new HashSet<JFreeChart>();
		 separateWindows = HashBiMap.create();
		initializeColorPalette(300);
		 UIManager.put("TaskPaneContainer.useGradient", Boolean.TRUE);
		 UIManager.put("TaskPaneContainer.background", Color.LIGHT_GRAY);
		UIManager.put("TaskPane.titleBackgroundGradientStart", Color.gray.brighter().brighter());
		UIManager.put("TaskPane.titleBackgroundGradientEnd", Color.gray.brighter());
		UIManager.put("TaskPane.titleForeground", Color.BLACK);
		 UIManager.put("TaskPane.font", new FontUIResource(GraphicalProperties.customFont));
		 
		 UIManager.put("TaskPaneContainer.font", new FontUIResource(GraphicalProperties.customFont));
		 UIManager.put("Label.font",new FontUIResource(GraphicalProperties.customFont));
		 
		basicCell_attributeMap = new Hashtable();
		GraphConstants.setLabelEnabled(basicCell_attributeMap, false);
		GraphConstants.setEditable(basicCell_attributeMap, false);
		GraphConstants.setEndFill(basicCell_attributeMap, false);
		GraphConstants.setLineWidth(basicCell_attributeMap, 2.0f);
		GraphConstants.setRouting(basicCell_attributeMap, GraphConstants.ROUTING_DEFAULT);
		GraphConstants.setBendable(basicCell_attributeMap, true);
		GraphConstants.setResize(basicCell_attributeMap, true);
		GraphConstants.setInset(basicCell_attributeMap, 10);
		GraphConstants.setBackground(basicCell_attributeMap, Color.DARK_GRAY);
		GraphConstants.setForeground(basicCell_attributeMap, Color.white);
		GraphConstants.setLineColor(basicCell_attributeMap, Color.BLUE);
		GraphConstants.setOpaque(basicCell_attributeMap, true);
		GraphConstants.setLineEnd(basicCell_attributeMap, GraphConstants.ARROW_TECHNICAL);
		//GraphConstants.setBorder(basicCell_attributeMap, new LineBorder(Color.GRAY,1));
		GraphConstants.setFont(basicCell_attributeMap, new Font("Tahoma", Font.BOLD, 12));
		
		initializeFrame();
		populateJPanelParameterLists();

		setSize(700, 539);
		addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent evt) {
		    	MainGui.modelHasBeenModified = true;
				dispose();
		    }
		});
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	
	}
	
	/** this painter draws a gradient fill */
	public Painter getPainter() {
	  int width = 500;
	  int height = 500;
	  Color color1 = Color.GRAY.brighter();
	  Color color2 = Color.gray.darker();

	  LinearGradientPaint gradientPaint =
	      new LinearGradientPaint(0.0f, 0.0f, width, height,
	                              new float[]{0.0f, 1.0f},
	                              new Color[]{color1, color2});
	  MattePainter mattePainter = new MattePainter(gradientPaint);
	  return mattePainter;
	}


	
	private JXTaskPaneContainer  addTaskPane(final JGraph currentGraph) {
		
		
			JXTaskPaneContainer  taskPane = new JXTaskPaneContainer();
			
			taskPane.setBackgroundPainter(getPainter());
				// Configures the taskpane
				Color colorbackground = Color.gray.brighter().brighter();
				JXTaskPane taskGroup = new JXTaskPane();
				((JComponent) taskGroup.getContentPane()).setBackground(colorbackground);
				//((JComponent) taskGroup.getContentPane()).setFont(GraphicalProperties.customFont);
				taskGroup.setTitle("Graph layouts");
				taskGroup.add(new AbstractAction("1 - Hierarchical") {
					public void actionPerformed(ActionEvent e) {
						JGraphHierarchicalLayout h = new JGraphHierarchicalLayout();
						h.setOrientation(SwingConstants.SOUTH);
						execute(h,currentGraph);
					}
				});
				taskGroup.add(new AbstractAction("2 - Organic") {
					public void actionPerformed(ActionEvent e) {
						JGraphOrganicLayout layout = new JGraphOrganicLayout();
						layout.setOptimizeBorderLine(false);
						layout.setEdgeLengthCostFactor(0.001); //bigger and edges will be close together
						layout.setEdgeCrossingCostFactor(500); //bigger and if should cross less, but nodes may be spread more resulting in very long edges
						execute(layout,currentGraph);
					}
				});
				taskGroup.add(new AbstractAction("3 - Fast Organic") {
					public void actionPerformed(ActionEvent e) {
						JGraphFastOrganicLayout layout = new JGraphFastOrganicLayout();
						layout.setForceConstant(60);
						execute(layout,currentGraph);
					}
				});
				
				taskGroup.add(new AbstractAction("4 - Circle") {
					public void actionPerformed(ActionEvent e) {
						execute(new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_CIRCLE),currentGraph);
					}
				});
			
				taskGroup.add(new AbstractAction("5 - Tilt current layout") {
					public void actionPerformed(ActionEvent e) {
						execute(new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_TILT),currentGraph);
					}
				});
				taskGroup.add(new AbstractAction("6 - Random") {
					public void actionPerformed(ActionEvent e) {
						execute(new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_RANDOM, 640, 480),currentGraph);
					}
				});
				
				JXTaskPane taskGroup_subLayout = new JXTaskPane();
				((JComponent) taskGroup_subLayout.getContentPane()).setBackground(colorbackground);
				
				taskGroup_subLayout.setTitle("More layouts...");
				taskGroup_subLayout.setCollapsed(true);
				taskGroup.add(taskGroup_subLayout);
				taskGroup_subLayout.add(new AbstractAction("Self-Organizing") {
					public void actionPerformed(ActionEvent e) {
						JGraphSelfOrganizingOrganicLayout layout = new JGraphSelfOrganizingOrganicLayout();
						layout.setStartRadius(400);
						layout.setMinRadius(10000);
						layout.setMaxIterationsMultiple(40);
						layout.setDensityFactor(10000);
						execute(new JGraphSelfOrganizingOrganicLayout(),currentGraph);
					}
				});
				
				taskGroup_subLayout.add(new AbstractAction("Spring") {
					public void actionPerformed(ActionEvent e) {
						execute(new JGraphSpringLayout(300),currentGraph);
					}
				});
			
				taskGroup_subLayout.add(new AbstractAction("Tree") {
					public void actionPerformed(ActionEvent e) {
						execute(new JGraphTreeLayout(),currentGraph);
					}
				});
				
				taskGroup_subLayout.add(new AbstractAction("Compact Tree") {
					public void actionPerformed(ActionEvent e) {
						JGraphCompactTreeLayout l = new JGraphCompactTreeLayout();
						l.setNodeDistance(10);
						execute(l,currentGraph);
					}
				});
				
				taskGroup_subLayout.add(new AbstractAction("Radial Tree") {
					public void actionPerformed(ActionEvent e) {
						execute(new JGraphRadialTreeLayout(),currentGraph);
					}
				});
			
				taskGroup.add(new JSeparator());
				taskGroup.add(new JSeparator());
					taskGroup.add(new AbstractAction("Undo graphical edit") {
					public void actionPerformed(ActionEvent e) {
						try {
							if(currentGraph.getName().contains("Mutants")) undoManager.undo();
							else if(currentGraph.getName().contains("Analysis1")) undoManagerAnalysis1.undo();
						} catch(Exception ex) {
							//nothing to do... undoable action
						}
					}
				});
				
				taskGroup.add(new AbstractAction("Redo graphical edit") {
					public void actionPerformed(ActionEvent e) {
					try{
						if(currentGraph.getName().contains("Mutants")) undoManager.redo();
						else if(currentGraph.getName().contains("Analysis1")) undoManagerAnalysis1.redo();
					} catch(Exception ex) {
						//nothing to do... undoable action
					}
					}
				});
				
				 taskPane.add(taskGroup);

				taskGroup = new JXTaskPane();
				((JComponent) taskGroup.getContentPane()).setBackground(colorbackground);
				taskGroup.setTitle("View");
				
				taskGroup.add(new AbstractAction("Zoom in") {
					public void actionPerformed(ActionEvent e) {
						currentGraph.setScale(currentGraph.getScale()+0.5);
					}
				});
				
				
				taskGroup.add(new AbstractAction("Zoom out") {
					public void actionPerformed(ActionEvent e) {
						currentGraph.setScale(currentGraph.getScale()-0.5);
					}
				});
				
			
				
			
				taskGroup.add(new AbstractAction("Actual Size") {
					public void actionPerformed(ActionEvent e) {
						currentGraph.setScale(1);
					}
				});
				taskGroup.add(new AbstractAction("Fit Window") {
					public void actionPerformed(ActionEvent e) {
						JGraphLayoutMorphingManager.fitViewport(currentGraph);
					}
				});
				
				taskPane.add(taskGroup);

				
				taskGroup = new JXTaskPane();
				((JComponent) taskGroup.getContentPane()).setBackground(colorbackground);
				
				taskGroup.setTitle("Save/Load");
				taskGroup.add(new AbstractAction("Save to image") {
					public void actionPerformed(ActionEvent e) {
							Color bg = null; // Use this to make the background transparent
							bg = currentGraph.getBackground(); // Use this to use the graph backgroundcolor
							BufferedImage img = currentGraph.getImage(bg, 10);
							
							JFileChooser fileChooser = new JFileChooser();
							int returnVal = fileChooser.showOpenDialog(null);
					        if (returnVal == JFileChooser.APPROVE_OPTION) {
					            File file = fileChooser.getSelectedFile();
					            int ext = file.getAbsolutePath().lastIndexOf(".");
					            String format = "png";
					            if(ext !=-1) {
					            	format = file.getAbsolutePath().substring(ext+1);
					            } else {
					            	file = new File( file.getAbsolutePath() + "."+format);
					            }
					            try {
									ImageIO.write(img, format, file);
									JOptionPane.showMessageDialog(null, "Image available at:\n"+file.getCanonicalPath(), "Successful export to image", JOptionPane.INFORMATION_MESSAGE);
								} catch (IOException e1) {
									JOptionPane.showMessageDialog(null, "Error exporting the file:\n"+e1.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
									return;
								}
					        }
					}
				});
				
				
				taskGroup.add(new AbstractAction("Save current view") {
					public void actionPerformed(ActionEvent e) {
						String name=JOptionPane.showInputDialog("Provide a name for the view:");
						if(name == null || name.trim().length() == 0) {
							return;
						}
						while(savedView.containsKey(name)) {
							JOptionPane.showMessageDialog(null, "The name already exists.\nProvide a different name.", "Error!", JOptionPane.ERROR_MESSAGE);
							name=JOptionPane.showInputDialog("Provide a name for the view:", name);
							if(name==null) return;
						}
						saveCurrentView(name,currentGraph);
					
					}
				});
				taskPane.add(taskGroup);
				
				taskGroup.add(new AbstractAction("Load view") {
					public void actionPerformed(ActionEvent e) {
						Set<String> allNames = savedView.keySet();
						Vector sorted = new Vector<String>();
						sorted.addAll(allNames);
						sorted.remove(getDefaultViewName(0));
						sorted.remove(getDefaultViewName(1));
						Collections.sort(sorted);
						for(int i = 0; i < sorted.size(); i++) {
							String newName = (String) sorted.get(i);
							newName = newName.substring(0, newName.lastIndexOf("%"));
							sorted.set(i, newName);
						}
						sorted.add(0, defaultViewName);
						
						String name=(String) JOptionPane.showInputDialog(
								null,
								"Select the view to load:",
								"Load view",
								JOptionPane.QUESTION_MESSAGE,
								null,
								sorted.toArray(),
								getDefaultViewName(0));
						if(name == null || name.trim().length() == 0) {
							return;
						}
						
						loadSavedView(name,currentGraph);
					}
				});
				taskPane.add(taskGroup);
				
				taskGroup = new JXTaskPane();
				((JComponent) taskGroup.getContentPane()).setBackground(colorbackground);
				
				taskGroup.setTitle("Edit");
				final JCheckBox onSelectedCells = new  JCheckBox("Apply only on selected cells");
				onSelectedCells.setFont(((JComponent) taskGroup.getContentPane()).getFont());
				onSelectedCells.setOpaque(false);
				onSelectedCells.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						if(onSelectedCells.isSelected()) {
							if(currentGraph.getName().contains("Mutants")) {
								onSelectedCellsMutants = true;
							} else if(currentGraph.getName().contains("Analysis1")) {
								onSelectedCellsAnalysis1 = true;
							}else if(currentGraph.getName().contains("Plot")) {
								onSelectedCellsPlot = true;
							}
						} else{
							if(currentGraph.getName().contains("Mutants")) {
								onSelectedCellsMutants = false;
							} else if(currentGraph.getName().contains("Analysis1")) {
								onSelectedCellsAnalysis1 = false;
							}else if(currentGraph.getName().contains("Plot")) {
								onSelectedCellsPlot = false;
							}
						}
						
					}
				});
				taskGroup.add(onSelectedCells);
				
				
				taskGroup.add(new AbstractAction("Node color (background)") {
					public void actionPerformed(ActionEvent e) {
						applyColor(0,currentGraph);
					}
				});
				
				taskGroup.add(new AbstractAction("Edge color") {
					public void actionPerformed(ActionEvent e) {
						applyColor(2,currentGraph);
					}
				});
				
				taskGroup.add(new AbstractAction("Text color") {
					public void actionPerformed(ActionEvent e) {
						applyColor(1,currentGraph);
					}
				});
				
			
				taskPane.add(taskGroup);
				GraphicalProperties.resetFonts(taskPane);
				onSelectedCells.setFont(((JComponent) taskGroup.getContentPane()).getFont());
				
				return taskPane;
		
	}

	protected void applyColor(int which, JGraph currentGraph) {
		//0 background
		//1 foreground
		//2 line
		Map nested = new Hashtable();
		Map attributeMap1 = new Hashtable();
		boolean onSelectedCells = false;
		if(currentGraph.getName().contains("Mutant")) {
			onSelectedCells = onSelectedCellsMutants;
		}	else if(currentGraph.getName().contains("Analysis1")) {
			onSelectedCells = onSelectedCellsAnalysis1;
		} else if(currentGraph.getName().contains("Plot")) {
			onSelectedCells = onSelectedCellsPlot;
		}
			Color newColor = JColorChooser.showDialog(rootPane, "Color Chooser", GraphConstants.getBackground(basicCell_attributeMap));
			if(newColor!= null) {
				if(which == 0) GraphConstants.setBackground(attributeMap1, newColor);
				else if (which == 1) GraphConstants.setForeground(attributeMap1, newColor);
				else if(which == 2)	GraphConstants.setLineColor(attributeMap1, newColor);
			}
			
			Object[] all = null;
			if(onSelectedCells) {
				 all = currentGraph.getSelectionCells();
			} else {
				all = JGraphModelAdapter.getAll(currentGraph.getModel());	
			}
			for(int i = 0; i < all.length; i++) {
				nested.put(all[i], attributeMap1);
			}
			currentGraph.getGraphLayoutCache().edit(nested, null, null, null);
		
	}
 
	
	public MutantsDB getMutantsDB() {
		if(mutantsDB == null) {
			mutantsDB = new MutantsDB(multiModel);
		}
		return mutantsDB;
	}
	
	public SimulationsDB getAnalysis1DB() {
		if(simDB == null) {
			simDB = new SimulationsDB(multiModel);
			simDB.setRunManager(this);
		}
		return simDB;
	}
	
	public RMPlotDB getPlotDB() {
		if(plotDB == null) {
			plotDB = new RMPlotDB();
			plotDB.setRunManager(this);
		}
		return plotDB;
	}
	
	public void clearRM() {
		simDB = null;
		mutantsDB = null;
	}
	

	public void initializeMutantsGraph_fromSavedMSMB(HashMap<String,HashMap<Object, AttributeMap>> savedView_toApply) {
		mutantsDB = new MutantsDB(multiModel);
		simDB = new SimulationsDB(multiModel);
		simDB.setRunManager(this);
		plotDB = new RMPlotDB();
		plotDB.setRunManager(this);
			
		mutantsDB.initializeJGraph(savedView_toApply.get(getDefaultViewName(0)));
		simDB.initializeJGraph(savedView_toApply.get(getDefaultViewName(1)));
		plotDB.initializeJGraph(savedView_toApply.get(getDefaultViewName(2)));
			
		initializeMutantsGraph();
		initializeAnalysis1Graph();
		initializePlotGraph();
		
		initializeGraphsScrollPanels();
		initializeTaskPanelMutantsGraph();
		initializeTaskPanelAnalysis1();
		initializeTaskPanelPlot();
		
		savedView= new HashMap<String, HashMap<Object,AttributeMap>>();
		savedView.putAll(savedView_toApply);
		
		loadSavedView(defaultViewName,jgraph_graph_parameters);
		loadSavedView(defaultViewName,jgraph_graph_analysis1);
		loadSavedView(defaultViewName,jgraph_graph_plot);
		 chartForExport = new HashSet<JFreeChart>();
		 separateWindows = HashBiMap.create();
		this.pack();
		
	}
	
	
	public void initializeAndShow(MultiModel multiModel, JFrame parent) {
		mainGui = parent;
		mainGui.setFocusable(false);
		mainGui.setEnabled(false);
		GraphicalProperties.resetFonts(this);
		
		this.multiModel = multiModel;
		
		mutantsDB = getMutantsDB();
		mutantsDB.setMultiModel(this.multiModel);
		initializeMutantsGraph();
		
		simDB  = getAnalysis1DB();
		simDB.setMultiModel(this.multiModel);
		initializeAnalysis1Graph();
	
		plotDB  = getPlotDB();
		initializePlotGraph();
		
		initializeGraphsScrollPanels();
		initializeTaskPanelMutantsGraph();
		initializeTaskPanelAnalysis1() ;
		initializeTaskPanelPlot();
	
		
		addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent evt) {
		    	menuItemCloseAllSeparateWindows.doClick();
		    	saveCurrentView(defaultViewName, jgraph_graph_parameters);
		    	saveCurrentView(defaultViewName, jgraph_graph_analysis1);
		    	saveCurrentView(defaultViewName, jgraph_graph_plot);
		    	mainGui.setFocusable(true);
				mainGui.setEnabled(true);
				dispose();
		    }
		});	
		undoManager = new GraphUndoManager();
		undoManagerAnalysis1 = new GraphUndoManager();
		jgraph_graph_parameters.getModel().addUndoableEditListener(undoManager);
		jgraph_graph_analysis1.getModel().addUndoableEditListener(undoManagerAnalysis1);
		singleMutantFrame = new SingleMutantFrame(multiModel, mutantsDB);
		singleAnalysis1Frame = new SingleSimulationFrame(simDB, multiModel, mutantsDB);
		changeIntervalPlotFrame = new ChangeIntervalPlotFrame();
		changeLinePlotFrame = new ChangeLinePlotFrame();
		changeChartPropertiesFrame = new ChangeChartPropertiesFrame(plotDB,simDB);
		selectSimulationToRunFrame = new SelectSimulationToRunFrame();
		pack();
		 chartForExport = new HashSet<JFreeChart>();
		 separateWindows = HashBiMap.create();
		 Rectangle screen = parent.getGraphicsConfiguration().getBounds();
		 setLocation(
		        screen.x + (screen.width - getWidth()) / 2,
		        screen.y + (screen.height - getHeight()) / 2 ); 
		
		
		
		loadSavedView(defaultViewName,jgraph_graph_parameters);
		loadSavedView(defaultViewName,jgraph_graph_analysis1);
		loadSavedView(defaultViewName,jgraph_graph_plot);
		panel_plots.removeAll();
		jTabRM.setSelectedIndex(0);
		setVisible(true);

	}
	
	
	
	private void initializeGraphsScrollPanels() {
		JScrollPane graphScrollPane = new JScrollPane();
		graphScrollPane.setViewportView(jgraph_graph_parameters);
		splitPaneGraph_parameters.setLeftComponent(graphScrollPane);
		
		graphScrollPane = new JScrollPane();
		graphScrollPane.setViewportView(jgraph_graph_analysis1);
		splitPaneGraph_analysis1.setLeftComponent(graphScrollPane);
		
		graphScrollPane = new JScrollPane();
		graphScrollPane.setViewportView(jgraph_graph_plot);
		splitPaneGraph_plot.setLeftComponent(graphScrollPane);
	}
	
	private void initializeTaskPanelMutantsGraph() {
		JScrollPane taskPaneScrollPane = new JScrollPane(	);
		taskPaneScrollPane.setViewportView(addTaskPane(jgraph_graph_parameters));
		taskPaneScrollPane.setPreferredSize(new Dimension(300,600));
		splitPaneGraph_parameters.setRightComponent(taskPaneScrollPane);
	}

	private void initializeTaskPanelAnalysis1() {
		JScrollPane taskPaneScrollPane = new JScrollPane();
		taskPaneScrollPane.setViewportView(addTaskPane(jgraph_graph_analysis1));
		taskPaneScrollPane.setPreferredSize(new Dimension(300,600));
		splitPaneGraph_analysis1.setRightComponent(taskPaneScrollPane);
	}
	
	private void initializeTaskPanelPlot() {
		JScrollPane taskPaneScrollPane = new JScrollPane();
		taskPaneScrollPane.setViewportView(addTaskPane(jgraph_graph_plot));
		taskPaneScrollPane.setPreferredSize(new Dimension(300,600));
		splitPaneGraph_plot.setRightComponent(taskPaneScrollPane);
	}
	
	private void initializeMutantsGraph() {
		
		sortJListMutants();
		
	    m_jgAdapter = new JGraphModelAdapter( mutantsDB.getJgraphT() );
     	jgraph_graph_parameters = new JGraph(m_jgAdapter );
     	jgraph_graph_parameters.setName("%Mutants");
		remove = new AbstractAction("", null) {
			public void actionPerformed(ActionEvent e) {
				if (!jgraph_graph_parameters.isSelectionEmpty()) {
					Object[] cells = jgraph_graph_parameters.getSelectionCells();
					
					//collect things to revalidate conflicts
					HashSet<Mutant> children = new HashSet<Mutant>();
					for(int i = 0; i < cells.length; ++i) {
						DefaultGraphCell c = ((DefaultGraphCell)cells[i]);
						if( c instanceof DefaultEdge ) {
							//something else
							DefaultEdge ed = (DefaultEdge)c;
							DefaultGraphCell source = (DefaultGraphCell) ((DefaultPort)ed.getSource()).getParent();
							children.add((Mutant) source.getUserObject());
							
						} else if(c instanceof DefaultPort) {
							//nothing
						} else {
							//is a vertex --> mutant
							children.addAll(mutantsDB.getChildren((Mutant)c.getUserObject()));
							listModel_mutants.removeElement((Mutant)c.getUserObject());
						}
						
					}
					
					//Custom button text
					Object[] options = {"Yes",
					                    "No",
					                    };
					int n = JOptionPane.showOptionDialog(
							null,
						    "You are about to delete an element in the graph.\n"
						    + "This will also delete all the edges from/to this node\n"
						    + "And it may cause changes in the following nodes\n"+children+"\n"
						    + "Are you sure you want to proceed?",
						    "WARNING: delete action",
						   
						    JOptionPane.YES_NO_OPTION,
						    JOptionPane.QUESTION_MESSAGE,
						    null,
						    options,
						    options[1]);
					if(n==0) {
						m_jgAdapter.remove(cells);
						revalidateConflicts(new Vector(Arrays.asList(children.toArray())));
						undoManager.discardAllEdits();
					}
				}
			}
		};
		
		// create a visualization using JGraph, via an adapter
    	jgraph_graph_parameters.setDisconnectable(false);
		jgraph_graph_parameters.setCloneable(false);
		
		//to handle add/remove control points when right click and select appropriate menu item
		jgraph_graph_parameters.getGraphLayoutCache().setFactory(new DefaultCellViewFactory() {
			// Override Superclass Method to Return Custom EdgeView
			protected EdgeView createEdgeView(Object cell) {
				// Return Custom EdgeView
				return new EdgeView(cell) {
					public CellHandle getHandle(GraphContext context) {
						return new MyEdgeHandle(this, context);
					}

				};
			}
		
		});
		
		jgraph_graph_parameters.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}

			@Override
				public void keyPressed(KeyEvent e) {
				// Listen for Delete Key Press
				if (e.getKeyCode() == KeyEvent.VK_DELETE)
					// Execute Remove Action on Delete Key Press
					remove.actionPerformed(null);
			}
		});
		jgraph_graph_parameters.setMarqueeHandler(new MyMarqueeHandler(jgraph_graph_parameters));
				
		Map nested = new Hashtable();
		Object[] all = JGraphModelAdapter.getAll(jgraph_graph_parameters.getModel());	
		for(int i = 0; i < all.length; i++) {
			nested.put(all[i], basicCell_attributeMap );
		}
		jgraph_graph_parameters.getGraphLayoutCache().edit(nested, null, null, null);
		m_jgAdapter.setDefaultEdgeAttributes(new AttributeMap(basicCell_attributeMap));
		m_jgAdapter.setDefaultVertexAttributes(new AttributeMap(basicCell_attributeMap));
		
	}
	
	private void initializeAnalysis1Graph() {
		
		sortJListAnalysis1();
		
	    m_jgAdapter_analysis1 = new JGraphModelAdapter( simDB.getJgraphT() );
	    jgraph_graph_analysis1 = new JGraph(m_jgAdapter_analysis1 );
     	jgraph_graph_analysis1.setName("%Analysis1");
		remove_analysis1 = new AbstractAction("", null) {
			public void actionPerformed(ActionEvent e) {
				if (!jgraph_graph_analysis1.isSelectionEmpty()) {
					Object[] cells = jgraph_graph_analysis1.getSelectionCells();
					
					//collect things to revalidate conflicts
					HashSet<Simulation> children = new HashSet<Simulation>();
					for(int i = 0; i < cells.length; ++i) {
						DefaultGraphCell c = ((DefaultGraphCell)cells[i]);
						if( c instanceof DefaultEdge ) {
							//something else
							DefaultEdge ed = (DefaultEdge)c;
							DefaultGraphCell source = (DefaultGraphCell) ((DefaultPort)ed.getSource()).getParent();
							children.add((Simulation) source.getUserObject());
							
						} else if(c instanceof DefaultPort) {
							//nothing
						} else {
							//is a vertex --> mutant
							children.addAll(simDB.getChildren((Simulation)c.getUserObject()));
							listModel_analysis1.removeElement((Simulation)c.getUserObject());
						}
						
					}
					
					//Custom button text
					Object[] options = {"Yes",
					                    "No",
					                    };
					int n = JOptionPane.showOptionDialog(
							null,
						    "You are about to delete an element in the graph.\n"
						    + "This will also delete all the edges from/to this node\n"
						    + "And it may cause changes in the following nodes\n"+children+"\n"
						    + "Are you sure you want to proceed?",
						    "WARNING: delete action",
						   
						    JOptionPane.YES_NO_OPTION,
						    JOptionPane.QUESTION_MESSAGE,
						    null,
						    options,
						    options[1]);
					if(n==0) {
						m_jgAdapter_analysis1.remove(cells);
						revalidateConflicts(new Vector(Arrays.asList(children.toArray())));
						undoManager.discardAllEdits();
					}
				}
			}
		};
		
		// create a visualization using JGraph, via an adapter
		jgraph_graph_analysis1.setDisconnectable(false);
		jgraph_graph_analysis1.setCloneable(false);
		
		//to handle add/remove control points when right click and select appropriate menu item
		jgraph_graph_analysis1.getGraphLayoutCache().setFactory(new DefaultCellViewFactory() {
			// Override Superclass Method to Return Custom EdgeView
			protected EdgeView createEdgeView(Object cell) {
				// Return Custom EdgeView
				return new EdgeView(cell) {
					public CellHandle getHandle(GraphContext context) {
						return new MyEdgeHandle(this, context);
					}

				};
			}
		
		});
		
		jgraph_graph_analysis1.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}

			@Override
				public void keyPressed(KeyEvent e) {
				// Listen for Delete Key Press
				if (e.getKeyCode() == KeyEvent.VK_DELETE)
					// Execute Remove Action on Delete Key Press
					remove_analysis1.actionPerformed(null);
			}
		});
		jgraph_graph_analysis1.setMarqueeHandler(new MyMarqueeHandler(jgraph_graph_analysis1));
				
		Map nested = new Hashtable();
		Object[] all = JGraphModelAdapter.getAll(jgraph_graph_analysis1.getModel());	
		for(int i = 0; i < all.length; i++) {
			nested.put(all[i], basicCell_attributeMap );
		}
		jgraph_graph_analysis1.getGraphLayoutCache().edit(nested, null, null, null);
		m_jgAdapter_analysis1.setDefaultEdgeAttributes(new AttributeMap(basicCell_attributeMap));
		m_jgAdapter_analysis1.setDefaultVertexAttributes(new AttributeMap(basicCell_attributeMap));
		
	}
	
private void initializePlotGraph() {
		
		sortJListPlot();
		
	    m_jgAdapter_plot = new JGraphModelAdapter( plotDB.getJgraphT() );
     	jgraph_graph_plot  = new JGraph(m_jgAdapter_plot );
     	jgraph_graph_plot .setName("%Plot");
		remove_plot = new AbstractAction("", null) {
			public void actionPerformed(ActionEvent e) {
				if (!jgraph_graph_plot.isSelectionEmpty()) {
					Object[] cells = jgraph_graph_plot.getSelectionCells();
					
					//collect things to revalidate conflicts
					HashSet<RMPlot> children = new HashSet<RMPlot>();
					for(int i = 0; i < cells.length; ++i) {
						DefaultGraphCell c = ((DefaultGraphCell)cells[i]);
						if( c instanceof DefaultEdge ) {
							//something else
							DefaultEdge ed = (DefaultEdge)c;
							DefaultGraphCell source = (DefaultGraphCell) ((DefaultPort)ed.getSource()).getParent();
							children.add((RMPlot) source.getUserObject());
							
						} else if(c instanceof DefaultPort) {
							//nothing
						} else {
							//is a vertex --> mutant
							children.addAll(plotDB.getChildren((RMPlot)c.getUserObject()));
							listModel_plot.removeElement((RMPlot)c.getUserObject());
						}
						
					}
					
					//Custom button text
					Object[] options = {"Yes",
					                    "No",
					                    };
					int n = JOptionPane.showOptionDialog(
							null,
						    "You are about to delete an element in the graph.\n"
						    + "This will also delete all the edges from/to this node\n"
						    + "And it may cause changes in the following nodes\n"+children+"\n"
						    + "Are you sure you want to proceed?",
						    "WARNING: delete action",
						   
						    JOptionPane.YES_NO_OPTION,
						    JOptionPane.QUESTION_MESSAGE,
						    null,
						    options,
						    options[1]);
					if(n==0) {
						m_jgAdapter_plot.remove(cells);
						revalidateConflicts(new Vector(Arrays.asList(children.toArray())));
						undoManager.discardAllEdits();
					}
				}
			}
		};
		
		// create a visualization using JGraph, via an adapter
		jgraph_graph_plot.setDisconnectable(false);
		jgraph_graph_plot.setCloneable(false);
		
		//to handle add/remove control points when right click and select appropriate menu item
		jgraph_graph_plot.getGraphLayoutCache().setFactory(new DefaultCellViewFactory() {
			// Override Superclass Method to Return Custom EdgeView
			protected EdgeView createEdgeView(Object cell) {
				// Return Custom EdgeView
				return new EdgeView(cell) {
					public CellHandle getHandle(GraphContext context) {
						return new MyEdgeHandle(this, context);
					}

				};
			}
		
		});
		
		jgraph_graph_plot.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}

			@Override
				public void keyPressed(KeyEvent e) {
				// Listen for Delete Key Press
				if (e.getKeyCode() == KeyEvent.VK_DELETE)
					// Execute Remove Action on Delete Key Press
					remove_plot.actionPerformed(null);
			}
		});
		jgraph_graph_plot.setMarqueeHandler(new MyMarqueeHandler(jgraph_graph_plot));
				
		Map nested = new Hashtable();
		Object[] all = JGraphModelAdapter.getAll(jgraph_graph_plot.getModel());	
		for(int i = 0; i < all.length; i++) {
			nested.put(all[i], basicCell_attributeMap );
		}
		jgraph_graph_plot.getGraphLayoutCache().edit(nested, null, null, null);
		m_jgAdapter_plot.setDefaultEdgeAttributes(new AttributeMap(basicCell_attributeMap));
		m_jgAdapter_plot.setDefaultVertexAttributes(new AttributeMap(basicCell_attributeMap));
		
	}
	
	public static Set<Mutant> getAllMutants_parametersList() {
		Set<Mutant> nodes = mutantsDB.getVertexSet();
		return nodes;
	}
	
	 private void sortJListMutants() {
			listModel_mutants.clear();
			Set<Mutant> nodes = mutantsDB.getVertexSet();
			ArrayList<Mutant> toBeSortedNodes = new ArrayList<Mutant>();
			toBeSortedNodes.addAll(nodes);
			Collections.sort(toBeSortedNodes);
			for (Mutant element : toBeSortedNodes) {
				listModel_mutants.addElement(element);
			}
	}
	 
		
	 private void sortJListAnalysis1() {
			listModel_analysis1.clear();
			Set<Simulation> nodes = simDB.getVertexSet();
			ArrayList<Simulation> toBeSortedNodes = new ArrayList<Simulation>();
			toBeSortedNodes.addAll(nodes);
			Collections.sort(toBeSortedNodes);
			for (Simulation element : toBeSortedNodes) {
				listModel_analysis1.addElement(element);
			}
	}
	 
	 private void sortJListPlot() {
			listModel_plot.clear();
			Set<RMPlot> nodes = plotDB.getVertexSet();
			ArrayList<RMPlot> toBeSortedNodes = new ArrayList<RMPlot>();
			toBeSortedNodes.addAll(nodes);
			Collections.sort(toBeSortedNodes);
			for (RMPlot element : toBeSortedNodes) {
				listModel_plot.addElement(element);
			}
	}

	 protected void revalidateConflicts(Vector<Mutant> children) {
		Map nested = new Hashtable();
		Map withConflict = new Hashtable();
		withConflict.putAll(basicCell_attributeMap);
		GraphConstants.setLineBegin(withConflict, GraphConstants.ARROW_CIRCLE);
		GraphConstants.setBeginFill(withConflict, true);
		GraphConstants.setDashPattern(withConflict, new float[] {10,5});
		Map noConflict = new Hashtable();
		noConflict.putAll(basicCell_attributeMap);
		GraphConstants.setLineBegin(noConflict, GraphConstants.ARROW_NONE);
		GraphConstants.setBeginFill(noConflict, false);
		GraphConstants.setDashPattern(noConflict, new float[] {10, 0});
		
		
		/*Map redundant = new Hashtable();
		redundant.putAll(basicCell_attributeMap);
		GraphConstants.setLineBegin(redundant, GraphConstants.ARROW_NONE);
		GraphConstants.setBeginFill(redundant, true);
		GraphConstants.setDashPattern(redundant, new float[] {2, 2});*/
	
		for (Mutant mutant : children) {
			if(mutant instanceof Simulation) {
				Simulation sim = (Simulation) mutant;
				Vector<org.jgrapht.graph.DefaultEdge> edges = simDB.getEdgesToParents(sim);
				if(simDB.detectConflict(sim).size() > 0) {
					for (org.jgrapht.graph.DefaultEdge defaultEdge : edges) {
						DefaultEdge edge = m_jgAdapter_analysis1.getEdgeCell(defaultEdge);
						nested.put(edge, withConflict);
					}
				} else {
					for (org.jgrapht.graph.DefaultEdge defaultEdge : edges) {
						DefaultEdge edge = m_jgAdapter_analysis1.getEdgeCell(defaultEdge);
						Simulation target = (Simulation) ((DefaultGraphCell)JGraphModelAdapter.getTargetVertex(m_jgAdapter_analysis1, edge)).getUserObject();
						nested.put(edge, noConflict);
					}
				}
			jgraph_graph_analysis1.getGraphLayoutCache().edit(nested, null, null, null);

		} else if(mutant instanceof RMPlot) {
			RMPlot pl = (RMPlot) mutant;
			Vector<org.jgrapht.graph.DefaultEdge> edges = plotDB.getEdgesToParents(pl);
			if(plotDB.detectConflict(pl).size() > 0) {
				for (org.jgrapht.graph.DefaultEdge defaultEdge : edges) {
					DefaultEdge edge = m_jgAdapter_plot.getEdgeCell(defaultEdge);
					nested.put(edge, withConflict);
				}
			} else {
				for (org.jgrapht.graph.DefaultEdge defaultEdge : edges) {
					DefaultEdge edge = m_jgAdapter_plot.getEdgeCell(defaultEdge);
					RMPlot target = (RMPlot) ((DefaultGraphCell)JGraphModelAdapter.getTargetVertex(m_jgAdapter_plot, edge)).getUserObject();
					nested.put(edge, noConflict);
				}
			}
		jgraph_graph_plot.getGraphLayoutCache().edit(nested, null, null, null);
		}else {//mutant parameters
				Vector<org.jgrapht.graph.DefaultEdge> edges = mutantsDB.getEdgesToParents(mutant);
				if(mutantsDB.detectConflict(mutant).size() > 0) {
					for (org.jgrapht.graph.DefaultEdge defaultEdge : edges) {
						DefaultEdge edge = m_jgAdapter.getEdgeCell(defaultEdge);
						nested.put(edge, withConflict);
					}
				} else {
					for (org.jgrapht.graph.DefaultEdge defaultEdge : edges) {
						DefaultEdge edge = m_jgAdapter.getEdgeCell(defaultEdge);
						Mutant target = (Mutant) ((DefaultGraphCell)JGraphModelAdapter.getTargetVertex(m_jgAdapter, edge)).getUserObject();
						nested.put(edge, noConflict);
					}
				}
			}
			
			jgraph_graph_parameters.getGraphLayoutCache().edit(nested, null, null, null);
			}
			
			
					
		
	}

	private void initializeFrame() {
		JMenuBar jMenuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem itemSave = new JMenuItem("Save to .msmb");
		itemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		itemSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
            	saveCurrentView(defaultViewName, jgraph_graph_parameters);
		    	saveCurrentView(defaultViewName, jgraph_graph_analysis1);
		    	saveCurrentView(defaultViewName, jgraph_graph_plot);
		    	((MainGui) mainGui).save();
                   
            }
		}
		);
		
		fileMenu.add(itemSave);
		
		JMenuItem menuItem1 = new JMenuItem("Export to .cps");
		menuItem1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				int returnVal = fileChooser.showSaveDialog(null);
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fileChooser.getSelectedFile();
		            int ext = file.getAbsolutePath().lastIndexOf(".");
		            String baseFileName = file.getAbsolutePath();
		            if(ext !=-1) {
		            	baseFileName = baseFileName.substring(0, ext);
		            }
		            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		            mutantsDB.exportMutantGraph(baseFileName);
		            setCursor(null);
		        }
				
			}
		});
		fileMenu.add(menuItem1);
		
		 addTimeSeriesFrame = new SinglePlotAddTimeSeriesFrame();
		jMenuBar.add(fileMenu);		
		JMenu editMenu = new JMenu("Edit");
		
		JMenuItem menuItemRunSim= new JMenuItem("Run simulations");
		menuItemRunSim.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(plotDB.getVertexSet().size() == 0) {
					JOptionPane.showMessageDialog(null, "No custom Plot Settings are defined!\nSimulation results will be displayed \n with default settings.", "Error!", JOptionPane.WARNING_MESSAGE);
				}
		
			
				TreePath[] newList = selectSimulationToRunFrame.setVariablesAndShow(simDB.getVertexSet());
				selectSimulationToRunFrame.dispose();
				
				HashSet<String> toBeSimulated = new HashSet<String>();
				HashSet<String> simsNames = new HashSet<String>();
				if(newList!= null && newList.length > 0) {
				
					for (TreePath tp : newList) {
						if(tp.getPath().length > 1) {
							DefaultMutableTreeNode mutant = (DefaultMutableTreeNode) tp.getLastPathComponent();  
							if(! (mutant.getUserObject() instanceof Simulation)) {
								DefaultMutableTreeNode parent = (DefaultMutableTreeNode) tp.getParentPath().getLastPathComponent();
								toBeSimulated.add(parent + "__"+mutant);			
								simsNames.add(parent.toString());
							}
			             }
	                }
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				    
					/*if(plotDB.hasAllCustomRMPlot(simsNames)) {
						JOptionPane.showMessageDialog(null, "Not all selected simulations have custom plot settings.\n Those simulations will be displayed with default settings.", "Error!", JOptionPane.WARNING_MESSAGE);
					}*/
			
					
					try {
						createAndShowProgressBarFrame();
						progress(1,"Initializing simulations...");
						timeSeriesVariables.clear();
						panel_plots.removeAll();
						plotProperties.clear();
						simInCharts.clear();
						chartForExport.clear();
						separateWindows.clear();
					    simDB.exportMutantGraph(null,mutantsDB,toBeSimulated);
					    
					} catch (Exception e1) {
						e1.printStackTrace();
						 setCursor(null);
					}
			
		            
		            setCursor(null);
				}
				
				    
		 	}
		});
		editMenu.add(menuItemRunSim);
		
		/*JMenuItem menuItem2 = new JMenuItem("Run simulations (time series to file)");
		menuItem2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				int returnVal = fileChooser.showOpenDialog(null);
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fileChooser.getSelectedFile();
		            int ext = file.getAbsolutePath().lastIndexOf(".");
		            String baseFileName = file.getAbsolutePath();
		            if(ext !=-1) {
		            	baseFileName = baseFileName.substring(0, ext);
		            }
		            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		            simDB.exportMutantGraph(baseFileName,mutantsDB);
		            setCursor(null);
		        }
				
			}
		});
		testMenu.add(menuItem2);*/
		
		jMenuBar.add(editMenu);		
		
		windowMenu = new JMenu("Window");
		jMenuBar.add(windowMenu);		
		menuItemCloseAllSeparateWindows = new JMenuItem("Close all separate windows");
		menuItemCloseAllSeparateWindows.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Iterator<JFrame> it = 	separateWindows.keySet().iterator();
				while(it.hasNext()){
					JFrame toClose = it.next();
					WindowEvent wev = new WindowEvent(toClose, WindowEvent.WINDOW_CLOSING);
	                Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
				}
				
			}
		});
		windowMenu.add(menuItemCloseAllSeparateWindows);
		
		
		this.setJMenuBar(jMenuBar);
	
		jTabRM = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(jTabRM, BorderLayout.CENTER);
		
		jpanelParameterLists = new JPanel();

		jTabRM.addTab("Parameter lists", null, jpanelParameterLists, null);
		populateJPanelParameterLists();
		
		jPanelAnalysis1 = new JPanel();
		jTabRM.addTab("Time course", null, jPanelAnalysis1, null);
		jPanelAnalysis1.setLayout(new BorderLayout(0, 0));
		populateJPanelAnalysis(jPanelAnalysis1);
		
		jPanelPlot = new JPanel();
		jTabRM.addTab("Plotting", null, jPanelPlot, null);
		jPanelPlot.setLayout(new BorderLayout(0, 0));
		populateJPanelPlotting();
		
		JPanel panel_results = new JPanel();
		jTabRM.addTab("Results", null, panel_results, null);
		panel_results.setLayout(new BorderLayout(6, 6));
		
		JPanel panel = new JPanel();
		panel_results.add(panel, BorderLayout.NORTH);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		JPanel panel_2 = new JPanel();
		panel.add(panel_2);
		panel_2.setLayout(new BorderLayout(6, 0));
		
		JLabel lblNewLabel_1 = new JLabel(" Columns");
		panel_2.add(lblNewLabel_1, BorderLayout.WEST);
		
		JSpinner spinner_1 = new JSpinner();
		spinner_1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				try {
					Integer col = Integer.parseInt(((JSpinner)e.getSource()).getValue().toString());
					gridlayout_plots.setColumns(col);
					panel_plots.revalidate();
					panel_plots.repaint();
					scrollPane_results.revalidate();
					scrollPane_results.repaint();
				} catch(Exception ex) {
					((JSpinner)e.getSource()).setValue(1);
				}
			}
		});
		spinner_1.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
		panel_2.add(spinner_1);
		
		scrollPane_results = new JScrollPane();
		panel_results.add(scrollPane_results, BorderLayout.CENTER);
		panel_plots = new JPanel();
		scrollPane_results.setPreferredSize(new Dimension(100, 100));
		
		gridlayout_plots = new GridLayout(0, 1, 0, 0);
	     panel_plots.setLayout(gridlayout_plots);
	     
	     scrollPane_results.setViewportView(panel_plots);
		JPanel panel_3 = new JPanel();
		panel_results.add(panel_3, BorderLayout.SOUTH);
		
		
		JButton btnNewButton_1 = new JButton("Export all to images");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPanel contentPanel = new JPanel();
				contentPanel.setPreferredSize(new Dimension(500,100));
				JSpinner spinnerH = new JSpinner();
				JSpinner spinnerW = new JSpinner();
				final JTextField textFieldDir = new JTextField();
				JTextField textField_baseFileName = new JTextField();
				contentPanel.setLayout(new BorderLayout(0, 10));
				{
					JPanel panel = new JPanel();
					contentPanel.add(panel, BorderLayout.NORTH);
					panel.setLayout(new BorderLayout(10, 0));
					{
						JLabel lblSelectDirectory = new JLabel("Select directory:");
						panel.add(lblSelectDirectory, BorderLayout.WEST);
					}
					{
						panel.add(textFieldDir, BorderLayout.CENTER);
						textFieldDir.setColumns(10);
					}
					{
						JButton btnNewButton = new JButton("Browse...");
						btnNewButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								JFileChooser chooser = new JFileChooser();
								chooser.setDialogTitle("Select target directory");
								chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
								chooser.setCurrentDirectory(new File(textFieldDir.getText().trim()));
								int returnVal = chooser.showOpenDialog(null);
							
								if(returnVal == JFileChooser.APPROVE_OPTION) {
									File chosenfile = chooser.getSelectedFile();
									textFieldDir.setText(chosenfile.getAbsolutePath());
								} else {
									textFieldDir.setText("");
								}
							}
						});
						panel.add(btnNewButton, BorderLayout.EAST);
					}
				}
				{
					JPanel panel = new JPanel();
					contentPanel.add(panel);
					panel.setLayout(new GridLayout(3, 0, 0, 0));
					{
						JLabel lblNewLabel_2 = new JLabel("Base file name (optional)  ");
						lblNewLabel_2.setHorizontalAlignment(SwingConstants.RIGHT);
						panel.add(lblNewLabel_2);
					}
					{
						
						panel.add(textField_baseFileName);
						textField_baseFileName.setColumns(10);
					}
					{
						JPanel panel_1 = new JPanel();
						panel.add(panel_1);
					}
					{
						JLabel lblNewLabel = new JLabel("Image width  ");
						lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
						panel.add(lblNewLabel);
					}
					{
					
						panel.add(spinnerW);
					}
					{
						JLabel lblPixels = new JLabel("pixels");
						panel.add(lblPixels);
					}
					{
						JLabel lblNewLabel_1 = new JLabel("Image height  ");
						lblNewLabel_1.setHorizontalAlignment(SwingConstants.RIGHT);
						panel.add(lblNewLabel_1);
					}
					{
					
						panel.add(spinnerH);
					}
					{
						JLabel lblPixels_1 = new JLabel("pixels");
						panel.add(lblPixels_1);
					}
				}
				Integer width = null;
				Integer height = null;
			
				File chosenFile = null;
				int ret = -1;
				GraphicalProperties.resetFonts(contentPanel);
				
				while(chosenFile==null || width == null || height == null) {
					spinnerW.setValue(1000);
					spinnerH.setValue(500);
					ret = JOptionPane.showConfirmDialog(null, contentPanel, "Export to images...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
					if(ret == JOptionPane.OK_OPTION) {
						try {	width = Integer.parseInt(spinnerW.getValue().toString());} catch(Exception ex) {}
						try {	height = Integer.parseInt(spinnerH.getValue().toString());} catch(Exception ex) {}
						chosenFile = new File(textFieldDir.getText().trim());
					} else {
						break;
					}
				}
				if(ret == JOptionPane.OK_OPTION) {
					String fileName =textFieldDir.getText().trim()+"/";
					if(textField_baseFileName.getText().trim().length() >0) fileName += textField_baseFileName.getText().trim()+"_";
												
						Iterator<JFreeChart> it = chartForExport.iterator();
						while(it.hasNext()) {
							JFreeChart c = it.next();
							String title = c.getTitle().getText().replaceAll(" ","_");
							String currentFileName = fileName + title+".png";
							try {
								ChartUtilities.saveChartAsPNG(new File(currentFileName), c, width, height);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
				    }
			}
		});
		
		panel_3.add(btnNewButton_1);
		
		JButton btnNewButton_2 = new JButton("Open plots in separate windows");
		panel_3.add(btnNewButton_2);
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					Iterator<JFreeChart> it = chartForExport.iterator();
					int i = 0;
					if(it.hasNext()) {
						windowMenu.addSeparator();
					}
					while(it.hasNext()) {
							JFrame newFrame = new JFrame();
							JPanel contentPane = new JPanel();
							contentPane.setLayout(new BorderLayout());
							newFrame.setContentPane(contentPane);
							JFreeChart chart = it.next();
							ChartPanel chartPanel = new ChartPanel(chart);
							addCustomItemsPopupMenu(chartPanel);
					      	
							newFrame.setTitle(chart.getTitle().getText());
							contentPane.add(chartPanel, BorderLayout.CENTER);
							newFrame.pack();
							 Rectangle screen = getGraphicsConfiguration().getBounds();
							 newFrame.setLocation(
							        screen.x + (screen.width - newFrame.getWidth()) / 2,
							        screen.y + (screen.height - newFrame.getHeight()) / 2 ); 
							Point p = new Point();
							Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
							p.x = newFrame.getLocation().x+(i*20)%screenSize.width;
							p.y = newFrame.getLocation().y+(i*20)%screenSize.height;
							i++;
							newFrame.setLocation(p);
							final JMenuItem itemWin = new JMenuItem(newFrame.getTitle());
							itemWin.addActionListener(new ActionListener() {
					            public void actionPerformed(ActionEvent arg0) {
					            	final JFrame fr = separateWindows.inverse().get(itemWin);
					            	java.awt.EventQueue.invokeLater(new Runnable() {
					            	    @Override
					            	    public void run() {
					            	    	fr.toFront();
					            	    	fr.repaint();
					            	    }
					            	});
					            }
							}
							);
							separateWindows.put(newFrame, itemWin);
							windowMenu.add(itemWin);
							
							newFrame.addWindowListener(new WindowAdapter() {
							    @Override
							    public void windowClosing(WindowEvent evt) {
							    	JFrame currentFrame = (JFrame)evt.getWindow();
							    	final ChartPanel chartPanel = ((ChartPanel)((JPanel)currentFrame.getContentPane()).getComponent(0));
							      	JScrollPane scrollPane = new JScrollPane();
							      	scrollPane.setPreferredSize(new Dimension(625, 475));
							      	scrollPane.getViewport().add( chartPanel );
							      	panel_plots.add(scrollPane);
							      	panel_plots.revalidate();
							      	JMenuItem toRemove = separateWindows.get(currentFrame);
							      	windowMenu.remove(toRemove);
							      	separateWindows.remove(currentFrame);
							      	currentFrame.dispose();
							      	if(windowMenu.getComponentCount()==2) windowMenu.remove(1);//remove separator if this is the only remaining item
							    }
							});	
							
							newFrame.setVisible(true);
					}
					panel_plots.removeAll();
					panel_plots.revalidate();
			}
		});
		
	}
	
	void addCustomItemsPopupMenu(final ChartPanel chartPanel) {
    	JPopupMenu popup = chartPanel.getPopupMenu();
      	JMenuItem intervals = new JMenuItem("Plotting intervals...");
      	intervals.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFreeChart chart = chartPanel.getChart();
				MutablePair<Simulation, String> clicked = simInCharts.get(chart);
				if(clicked == null) return;
				Simulation s = clicked.left;
				String mut = clicked.right;
				LegendItemCollection legendItemsOld = chart.getPlot().getLegendItems();
				Vector<String> included = new Vector<String>();
				Iterator it = legendItemsOld.iterator();
				XYSeriesCollection dataset =  (XYSeriesCollection) ((XYPlot)(chart.getPlot())).getDataset();
				
				String xname = ((XYPlot)(chart.getPlot())).getDomainAxis().getLabel();
				if(xname.compareToIgnoreCase("TIME")==0) xname = "TIME";
				Vector<Double> seriesX = s.getTimeSeries(mut, xname);
				
				HashMap<String, PlottedVariable> currentSet = plotProperties.get((XYPlot)(chart.getPlot()));
				while(it.hasNext()) {
					String varName = ((LegendItem)it.next()).getLabel();
				/*	XYSeries old = dataset.getSeries(varName);
					Integer everyXpoints = time.size() / old.getItemCount();*/
					included.add(varName + " ("+currentSet.get(varName).getIntervalPlot()+")");
				}
				
				Vector<String> newList = changeIntervalPlotFrame.setVariablesAndShow(included);
				
				if(newList!= null) {
					Vector<String> variables = new Vector<String>();
					Vector<Integer> intervalSize = new Vector<Integer>();
					for(int i = 0; i < newList.size(); ++i) {
						String current = newList.get(i);
						int start = current.lastIndexOf("(");
						int end = current.lastIndexOf(")");
						int val = Integer.parseInt(current.substring(start+1,end).trim());
						String varName = current.substring(0, start).trim();
						variables.add(varName);
						intervalSize.add(val);
					}
					changeIntervalSize(chart, xname, variables, intervalSize);
				}
				
			
			}
		});
      	popup.add(intervals);
      	
      	JMenuItem lines = new JMenuItem("Line style...");
      	lines.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFreeChart chart = chartPanel.getChart();
				MutablePair<Simulation, String> clicked = simInCharts.get(chart);
				if(clicked == null) return;
				Simulation s = clicked.left;
				String mut = clicked.right;
				Vector<PlottedVariable> included = new Vector<PlottedVariable>();
				included.addAll(plotProperties.get((XYPlot)chart.getPlot()).values());
				Vector<PlottedVariable> newList = changeLinePlotFrame.setVariablesAndShow(included);
				
				if(newList!= null) {
					changeLineStyle(chart, newList);
				}
				
			
			}

		
		});
      	popup.add(lines);
      	
      
      	
      	JMenuItem addRemoveVars = new JMenuItem("Add/Remove variable...");
      	addRemoveVars.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFreeChart chart = chartPanel.getChart();
				MutablePair<Simulation, String> clicked = simInCharts.get(chart);
				if(clicked == null) return;
				Simulation s = clicked.left;
				String mut = clicked.right;
				LegendItemCollection legendItemsOld = chart.getPlot().getLegendItems();
				Set<String> included = new HashSet<String>();
				Iterator it = legendItemsOld.iterator();
				while(it.hasNext()) {
					included.add(((LegendItem)it.next()).getLabel());
				}
				
				Set<String> newList = addTimeSeriesFrame.setListAndShow(
																s.getName() + " (" +mut+")", 
																included);
				if(newList!= null) {
					addVariable(chart, newList);
					Set<String> deleted = new HashSet<String>();
					deleted.addAll(Sets.difference(included,newList));
					removeVariable(chart, deleted);
					XYSeriesCollection dataset =  (XYSeriesCollection) ((XYPlot)(chart.getPlot())).getDataset();
					
					fixColorsAfterRemove(dataset,(XYPlot)chart.getPlot());
					
				}
				
				
			}
		});
      	popup.add(addRemoveVars);
     	
     	
      	
      	JMenuItem removeLegend = new JMenuItem("Hide legend");
      	removeLegend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chartPanel.getChart().removeLegend();
				legendShowing = false;
			}
		});
      	popup.add(removeLegend);
     	JMenuItem showLegend = new JMenuItem("Show legend");
     	showLegend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(legendShowing) return;
				Plot plot = chartPanel.getChart().getPlot();
				LegendItemCollection legendItemsOld = plot.getLegendItems();
				final LegendItemCollection legendItemsNew = new LegendItemCollection();
				Iterator it = legendItemsOld.iterator();
				while(it.hasNext()) {
					legendItemsNew.add((LegendItem) it.next());
				}
				LegendItemSource source = new LegendItemSource() {
					LegendItemCollection lic = new LegendItemCollection();
					{lic.addAll(legendItemsNew);}
					public LegendItemCollection getLegendItems() {  
					    return lic;
					}
					};
					LegendTitle legendTitle = new LegendTitle(source);
					legendTitle.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
					legendTitle.setBorder(new BlockBorder());
					legendTitle.setBackgroundPaint(Color.white);
					legendTitle.setPosition(RectangleEdge.RIGHT);
				   chartPanel.getChart().addLegend(legendTitle);
				   legendShowing = true;
			}
		});
      	popup.add(showLegend);
	}

	
	void createAndShowProgressBarFrame() throws Exception {
		progressBarFrame = new ProgressBarFrame(this, "Simulation in progress...");
		progressBarThread = new Thread(progressBarFrame);
		progressBarThread.start();
    }
	
	void progress(int i, String name) throws InterruptedException {
		if(progressBarFrame !=null) {
				synchronized (progressBarFrame) {
		 			progressBarFrame.progress(i, name);
		 			progressBarFrame.notifyAll();
			 	}
		 	}
		 return;
	}
	
	
	private JFreeChart createChart(XYSeriesCollection dataset, String title, RMPlot plotSettings) {
	
		PlotOrientation orientation;
		if(plotSettings.isOrientationVertical()) {
			orientation = PlotOrientation.VERTICAL;
		} else {
			orientation = PlotOrientation.HORIZONTAL;
		}
		
		JFreeChart chart = ChartFactory.createXYLineChart(
				null, // title
				"time",             // x-axis label
				"",  				 // y-axis label
				dataset,            // data
				orientation,
				true,               // create legend?
				true,               // generate tooltips?
				false               // generate URLs?
		);

        chart.setBackgroundPaint(Color.white);
        LegendTitle legend = chart.getLegend();
  
       
    	
    	chartForExport.add(chart);
    	XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(plotSettings.getPlotBackground());
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        NumberAxis y = null;
     	if(plotSettings.isLogScaleY()) {
      		y = new LogarithmicAxis(plotSettings.getLabelY());
      	} else {
      		y = new NumberAxis(plotSettings.getLabelY());
      	}
        y.setLabel(plotSettings.getLabelY());
        y.setAutoRange(plotSettings.isAutoadjustY());
        if(!plotSettings.isAutoadjustY()) {
        	y.setRange(plotSettings.getMinY(),plotSettings.getMaxY());
        }
        y.setLabelFont(plotSettings.getLabelYfont());
        y.setLabelPaint(plotSettings.getLabelYcolor());
        plot.setRangeAxis(y);
   
        
        NumberAxis x  = null;
     	if(plotSettings.isLogScaleX()) {
      		x = new LogarithmicAxis(plotSettings.getVariableX());
      	} else {
      		x = new NumberAxis(plotSettings.getVariableX());
      	};
        x.setAutoRange(plotSettings.isAutoadjustX());
        if(!plotSettings.isAutoadjustX()) {
        	  x.setRange(plotSettings.getMinX(),plotSettings.getMaxX());
        }
        x.setLabelFont(plotSettings.getLabelXfont());
        x.setLabel(plotSettings.getXaxis());
        x.setLabelPaint(plotSettings.getLabelXcolor());
        plot.setDomainAxis(x);
        
                
        // render shapes and lines
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
	     renderer.setBaseShapesVisible(true);
	     renderer.setBaseShapesFilled(true);
	    
	     HashMap<String,PlottedVariable> allOptions = new HashMap<String,PlottedVariable>();
       
        for(int i = 0; i < dataset.getSeriesCount(); ++i) {
        	String name = dataset.getSeries(i).getKey().toString();
        	PlottedVariable options = plotSettings.getPlottedVariable(name);
        	Color c =  options.getColor();
        	renderer.setSeriesPaint(i, c, true); 
    		Stroke st = new BasicStroke(options.getLineWidth());
			renderer.setSeriesStroke(i, st);
			Shape shape = MyShapes.getShapeFromIndex(options.getIndexShape());
			if(shape!=null) {
			     renderer.setSeriesShape(i, shape);
			     renderer.setSeriesShapesVisible(i, true);
			} else {
				renderer.setSeriesShapesVisible(i, false);
			}
			allOptions.put(name,  options);
    	}
        if(plotSettings.getShowTitle()) {
    		TextTitle txt = new TextTitle(title, plotSettings.getTitleFont());
    		txt.setPaint(plotSettings.getTitleColor());
    		chart.setTitle(txt);
    		 List<Title> subTitles = new ArrayList<Title>();
    	     subTitles.add(new TextTitle(plotSettings.getName()));
    		chart.setSubtitles(subTitles);
    	      chart.addLegend(legend);
    	}
        if(!plotProperties.containsKey(plot)) {
    		  plotProperties.put(plot, allOptions);
		  } 
        
        plot.setRenderer(renderer);
        
        return chart;
    }
	
	
	public static Vector<Color> colorPalette = new Vector<Color> ();
	
	void initializeColorPalette(int number_of_colors_needed) {
		DefaultDrawingSupplier drawingSup = new DefaultDrawingSupplier();
		for(int i = 0; i < number_of_colors_needed; ++i) {
			colorPalette.add((Color)drawingSup.getNextPaint());
		}
		
	}
	
	HashMap<JFreeChart, MutablePair<Simulation, String>> simInCharts = new HashMap<JFreeChart, MutablePair<Simulation, String>>();
	protected boolean legendShowing;

	
	
	
	
	static Vector<String> timeSeriesVariables = new Vector<String>();
	
	void initializeTimeSeriesVariables(Vector<String> vars) {
		timeSeriesVariables.clear();
		timeSeriesVariables.addAll(vars);
	}
	
	static Vector<String> getTimeSeriesVariables() {
		Vector<String> ret = new Vector<String>();
		ret.addAll(timeSeriesVariables);
		return ret;
	}
	
	void plotMutantGraph(Simulation s, String mutantName) {
			Vector<RMPlot> multiplePlotSettings = plotDB.getPlotOfSimulation(s.getName());
			
			for(int p = 0; p < multiplePlotSettings.size(); ++p) {
				XYSeriesCollection dataset = new XYSeriesCollection();
				RMPlot plotSettings = multiplePlotSettings.get(p);
			Vector<PlottedVariable> vars = plotSettings.getAllPlottedVariables();
			
			String xname = plotSettings.getVariableX();
			if(xname.compareToIgnoreCase("TIME")==0) xname = "TIME";
			
			for(int i = 0; i < vars.size(); ++i) {
				String varName = vars.get(i).getName().trim();
				Vector<Double> seriesY = s.getTimeSeries(mutantName,varName);
				Vector<Double> seriesX = s.getTimeSeries(mutantName,xname);
				int oneEvery =	plotSettings.getPlottedVariable(varName).getIntervalPlot();
		       				
				XYSeries var = new  XYSeries(varName);
			        	 for(int j = 0; j < seriesX.size(); ++j) {
			        		 if(j%oneEvery==0) {
			        			 double x = seriesX.get(j);
				        		 double y = 0;
				        		 y = seriesY.get(j);
				        		 var.add(x,y);
			        		 }
			        	 }
			        	 dataset.addSeries(var);
		        	
			}
			
	        
	        JFreeChart chart = createChart(dataset, s.getName() + " ("+mutantName+")", plotSettings);
	        
	        LegendTitle legend = chart.getLegend();
	        legend.setPosition(RectangleEdge.RIGHT);
	        simInCharts.put(chart, new MutablePair(s, mutantName));
	        final ChartPanel chartPanel = new ChartPanel(chart);
	      	chartPanel.setPreferredSize(new java.awt.Dimension(200, 200));
	      	chartPanel.setMouseZoomable(true, false);
	      	
	      	addCustomItemsPopupMenu(chartPanel);
	     	      
	      	JScrollPane scrollPane = new JScrollPane();
	      	scrollPane.setPreferredSize(new Dimension(625, 475));
	      	scrollPane.getViewport().add( chartPanel );
	      	
	      	panel_plots.add(scrollPane);
	      	panel_plots.repaint();
			}
		}
	

    
	protected void changeIntervalSize(JFreeChart chart, String xvar, Vector<String> vars, Vector<Integer> intervalSize) {
		MutablePair<Simulation, String> clicked = simInCharts.get(chart);
		if(clicked == null) return;
		XYSeriesCollection dataset =  (XYSeriesCollection) ((XYPlot)(chart.getPlot())).getDataset();
		Simulation s = clicked.left;
		String mut = clicked.right;
		Iterator<String> it = vars.iterator();
		for(int i = 0; i < vars.size(); ++i) {
			String varName = vars.get(i);
			Integer oneEvery = intervalSize.get(i);
			XYSeries old = dataset.getSeries(varName);
			
			int index = dataset.getSeriesIndex(varName);
			dataset.removeSeries(index);
			Vector<Double> seriesY = s.getTimeSeries(mut, varName);
			Vector<Double> seriesX = s.getTimeSeries(mut, xvar);
			
			XYSeries var = new  XYSeries(varName);
	    	 for(int j = 0; j < seriesX.size(); ++j) {
	    		 if(j%oneEvery==0) {
	    			 double x = seriesX.get(j);
	    			 double y = seriesY.get(j);
	    			 var.add(x,y);
	    		 }
	    	 }
	    	 try {
	    		 dataset.addSeries(var);
	    	 } catch(Exception ex){
	    		 //dataset with same key already added
	    		 ex.printStackTrace();
	    		 continue;
	    	 }
		}
		fixColorsAfterRemove(dataset, (XYPlot)(chart.getPlot()));
	}
	
	private void fixColorsAfterRemove(XYSeriesCollection dataset, XYPlot xyPlot) {
		
		HashMap<String, PlottedVariable> properties = plotProperties.get(xyPlot);
		LegendItemCollection legendItemsOld =xyPlot.getLegendItems();
		XYItemRenderer renderer = (xyPlot).getRenderer();
	
		Iterator it = legendItemsOld.iterator();
		while(it.hasNext()) {
			String varName = ((LegendItem)it.next()).getLabel();
			  int i = dataset.getSeriesIndex(varName);
			  PlottedVariable p = properties.get(varName);
			renderer.setSeriesPaint(i, p.getColor()); 
		}
		(xyPlot).setRenderer(renderer);
	}

	protected void changeLineStyle(JFreeChart chart, Vector<PlottedVariable> vars) {
		MutablePair<Simulation, String> clicked = simInCharts.get(chart);
		if(clicked == null) return;
		XYPlot plot = (XYPlot)(chart.getPlot());
		
		XYSeriesCollection dataset =  (XYSeriesCollection) (plot).getDataset();
		Simulation s = clicked.left;
		String mut = clicked.right;
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) (plot).getRenderer();
		 HashMap<String,PlottedVariable> allOptions = plotProperties.get(plot);
		     		 
		for(int i = 0; i < vars.size(); ++i) {
			PlottedVariable var = vars.get(i);
			String varName = var.getName();
			Color c =  var.getColor();
			int index = dataset.getSeriesIndex(varName);
		    renderer.setSeriesPaint(index, c); 
	    	Stroke st = new BasicStroke(var.getLineWidth());
			renderer.setSeriesStroke(index, st);
			Shape shape = MyShapes.getShapeFromIndex(var.getIndexShape());
			if(shape!=null) {
			     renderer.setSeriesShape(index, shape);
			     renderer.setSeriesShapesVisible(index, true);
			} else {
				renderer.setSeriesShapesVisible(index, false);
				
			}
				allOptions.put(varName,  var);
	    	}
		 plotProperties.put(plot, allOptions);
	     plot.setRenderer(renderer);
  }
    
    

	protected void addVariable(JFreeChart chart, Set<String> vars) {
		MutablePair<Simulation, String> clicked = simInCharts.get(chart);
		if(clicked == null) return;
		if(vars.size() == 0) return;
		XYSeriesCollection dataset =  (XYSeriesCollection) ((XYPlot)(chart.getPlot())).getDataset();
		Simulation s = clicked.left;
		String mut = clicked.right;
		Iterator<String> it = vars.iterator();

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) ((XYPlot)(chart.getPlot())).getRenderer();
		 
      HashMap<String, PlottedVariable> currentPlotProperties = plotProperties.get((XYPlot)(chart.getPlot()));
      if(currentPlotProperties==null) {
    	  currentPlotProperties = new HashMap<String, PlottedVariable>();
      }
	   int i = currentPlotProperties.keySet().size();
	    while(it.hasNext()) {
			String varName = it.next();
			Vector<Double> timeSeries = s.getTimeSeries(mut, varName);
			Vector<Double> time = s.getTimeSeries(mut, "TIME");
			if(timeSeries==null) return;
		  PlottedVariable newPlottedVariable = new PlottedVariable(varName);
			XYSeries var = new  XYSeries(varName);
	    	 for(int j = 0; j < timeSeries.size(); ++j) {
	    		 double x = time.get(j);
	    		 double y = timeSeries.get(j);
	    		 var.add(x,y);
	    	 }
	    	 try {
	    		 dataset.addSeries(var);
	    		Color c =  colorPalette.get(i++);
	    		newPlottedVariable.setColor(c);
	    	    int index = dataset.getSeriesIndex(varName);
				 renderer.setSeriesPaint(index, c,true); 
				 int indexShape = index%(MyShapes.values().length-1);
				renderer.setSeriesShape(index, MyShapes.getShapeFromIndex(indexShape));
				newPlottedVariable.setShape(indexShape);
	    	 } catch(Exception ex){
	    		 //dataset with same key already added
	    		 continue;
	    	 }
	    	 currentPlotProperties.put(varName, newPlottedVariable);
		}
		  
	    plotProperties.put((XYPlot)(chart.getPlot()), currentPlotProperties);
	   
	  ((XYPlot)(chart.getPlot())).setRenderer(renderer);
   }
	
	protected void removeVariable(JFreeChart chart, Set<String> vars) {
		MutablePair<Simulation, String> clicked = simInCharts.get(chart);
		if(clicked == null) return;
		if(vars.size() == 0) return;
		XYSeriesCollection dataset =  (XYSeriesCollection) ((XYPlot)(chart.getPlot())).getDataset();
		Simulation s = clicked.left;
		String mut = clicked.right;
		Iterator<String> it = vars.iterator();
		while(it.hasNext()) {
			String varName = it.next();
			dataset.removeSeries(dataset.getSeries(varName));
		}
	}

	
	private void populateJPanelPlotting() {
		
		splitPaneGraph_plot = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPaneGraph_plot.setDividerLocation(350);
		splitPaneGraph_plot.setResizeWeight(0.5);
		splitPaneGraph_plot.revalidate();
		
			jPanelPlot.setLayout(new BorderLayout(0, 0));
			
			JPanel panel = new JPanel();
			jPanelPlot.add(panel);
			panel.setLayout(new BorderLayout(0, 0));
			
			JSplitPane splitPane = new JSplitPane();
			splitPane.setResizeWeight(0.2);
			panel.add(splitPane, BorderLayout.CENTER);
			
			JScrollPane scrollPane = new JScrollPane();
			JPanel panel_left= new JPanel();
			panel_left.setLayout(new BorderLayout(3, 3));
			panel_left.add(scrollPane, BorderLayout.CENTER);
			splitPane.setLeftComponent(panel_left);
			JLabel label = new JLabel(" List of existing plots settings:");
			panel_left.add(label, BorderLayout.NORTH);
			
			listModel_plot= new DefaultListModel();
			JList jList = new JList(listModel_plot);
	    	scrollPane.setViewportView(jList);
	    	jList.addListSelectionListener(new ListSelectionListener() {
	    	    public void valueChanged(ListSelectionEvent event) {
	    	        if (!event.getValueIsAdjusting()){
	    	            JList source = (JList)event.getSource();
	    	            Object[] selected = source.getSelectedValues();
	    	            ArrayList<Object> selectedGraphNodes = new ArrayList<Object>();
	    	            for(int i = 0; i < selected.length; ++i) {
	    	            	selectedGraphNodes.add(m_jgAdapter_plot.getVertexCell(selected[i]));
	    	            } 
	    	            jgraph_graph_plot.setSelectionCells(selectedGraphNodes.toArray());
	    	        }
	    	    }
	    	});
	    		
	    	
	    	JPanel panel_right= new JPanel();
	    	panel_right.setLayout(new BorderLayout(3, 3));
	    	panel_right.add(splitPaneGraph_plot, BorderLayout.CENTER);
			JLabel keyForEdges = new JLabel(" Hold SHIFT and click on control points (edges) to add/delete them");
			panel_right.add(keyForEdges, BorderLayout.SOUTH);
			
			splitPane.setRightComponent(panel_right);
			
			JPanel panel_1 = new JPanel();
			jPanelPlot.add(panel_1, BorderLayout.NORTH);
		
			
			JButton btnCreateMutantFrom = new JButton("Create new plot settings");
			btnCreateMutantFrom.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addRMPlotNode(null);
				}
			});
			
			panel_1.add(btnCreateMutantFrom);
			
			JButton btnChangeSelectedConfiguration = new JButton("Change selected configuration");
			btnChangeSelectedConfiguration.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
						if(jgraph_graph_plot.getSelectionCells().length>0) {
							Object cell = jgraph_graph_plot.getSelectionCells()[0];
							openSinglePlotSettings(cell);
						}
					}
				}
			);
				
			panel_1.add(btnChangeSelectedConfiguration);
			
			JButton btnCreateChildConfiguration = new JButton("Create child configuration");
			btnCreateChildConfiguration.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Object[] parents = jgraph_graph_plot.getSelectionCells();
					Vector<DefaultGraphCell> parentNodes = new Vector<DefaultGraphCell>();
					for(int i = 0; i < parents.length; i++) {
						Object p = parents[i];
						if( p instanceof DefaultEdge ||
							p instanceof DefaultPort) {continue;}
						else{
							parentNodes.add((DefaultGraphCell) p);
						}
					}
					if(parentNodes.size() == 0) return;
					
					String name=JOptionPane.showInputDialog("Provide a name for the new plot settings:");
					if(name == null || name.trim().length() == 0) {
						return;
					}
					
					while(plotDB.isNameDuplicate(name)) {
						JOptionPane.showMessageDialog(null, "The name already exists.\nProvide a different name.", "Error!", JOptionPane.ERROR_MESSAGE);
						name=JOptionPane.showInputDialog("Provide a name for the new plot settings:", name);
						if(name==null) return;
					}
					name = plotDB.cleanMutantName(name);
					RMPlot newMutantSim = new RMPlot(name);
					
					plotDB.addPlot(newMutantSim);
					DefaultGraphCell cell = m_jgAdapter_plot.getVertexCell(newMutantSim);
					Map nested = new Hashtable();
					Map withPossibleConflict = new Hashtable(basicCell_attributeMap);
					//the bounds values are not used because the graph has setResize, but bounds need to be non null to trigger that
					AttributeMap allAttributes = cell.getAttributes();
					java.awt.geom.Rectangle2D bounds = cell.getAttributes().createRect(10, 10, 20, 20);
					GraphConstants.setBounds(allAttributes, bounds);
					
					Vector<org.jgrapht.graph.DefaultEdge> connection = new Vector<org.jgrapht.graph.DefaultEdge>();
					for(int i = 0; i < parentNodes.size(); i++) {
						org.jgrapht.graph.DefaultEdge ed = plotDB.addConnection(newMutantSim, (RMPlot) (parentNodes.get(i)).getUserObject());
						connection.add(ed);
					}
					
					boolean conflict = plotDB.detectConflict(newMutantSim).size() > 0;
					if(conflict) {
						for(int i = 0; i < connection.size(); i++) {
							DefaultEdge edge = m_jgAdapter_plot.getEdgeCell(connection.get(i));
							GraphConstants.setLineBegin(withPossibleConflict, GraphConstants.ARROW_CIRCLE);
							GraphConstants.setBeginFill(withPossibleConflict, true);
							GraphConstants.setDashPattern(withPossibleConflict, new float[] {10,5});
							nested.put(edge, withPossibleConflict);
						}
					}
					nested.put(cell, withPossibleConflict);
					
					jgraph_graph_plot.getGraphLayoutCache().edit(nested, null, null, null);
					sortJListPlot();
				}
			});
			panel_1.add(btnCreateChildConfiguration);
			
		
	}
	
	
	private void populateJPanelAnalysis(JPanel jPanelAnalysis) {
		
		splitPaneGraph_analysis1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPaneGraph_analysis1.setDividerLocation(350);
		splitPaneGraph_analysis1.setResizeWeight(0.5);
		splitPaneGraph_analysis1.revalidate();
		
			jPanelAnalysis.setLayout(new BorderLayout(0, 0));
			
			JPanel panel = new JPanel();
			jPanelAnalysis.add(panel);
			panel.setLayout(new BorderLayout(0, 0));
			
			JSplitPane splitPane = new JSplitPane();
			splitPane.setResizeWeight(0.2);
			panel.add(splitPane, BorderLayout.CENTER);
			
			JScrollPane scrollPane = new JScrollPane();
			JPanel panel_left= new JPanel();
			panel_left.setLayout(new BorderLayout(3, 3));
			panel_left.add(scrollPane, BorderLayout.CENTER);
			splitPane.setLeftComponent(panel_left);
			JLabel label_mutantList = new JLabel(" List of existing analysis settings:");
			panel_left.add(label_mutantList, BorderLayout.NORTH);
			
			listModel_analysis1= new DefaultListModel();
			JList jListAnalysis = new JList(listModel_analysis1);
	    	scrollPane.setViewportView(jListAnalysis);
	    	jListAnalysis.addListSelectionListener(new ListSelectionListener() {
	    	    public void valueChanged(ListSelectionEvent event) {
	    	        if (!event.getValueIsAdjusting()){
	    	            JList source = (JList)event.getSource();
	    	            Object[] selected = source.getSelectedValues();
	    	            ArrayList<Object> selectedGraphNodes = new ArrayList<Object>();
	    	            for(int i = 0; i < selected.length; ++i) {
	    	            	selectedGraphNodes.add(m_jgAdapter_analysis1.getVertexCell(selected[i]));
	    	            } 
	    	            jgraph_graph_analysis1.setSelectionCells(selectedGraphNodes.toArray());
	    	        }
	    	    }
	    	});
	    		
	    	
	    	JPanel panel_right= new JPanel();
	    	panel_right.setLayout(new BorderLayout(3, 3));
	    	panel_right.add(splitPaneGraph_analysis1, BorderLayout.CENTER);
			JLabel keyForEdges = new JLabel(" Hold SHIFT and click on control points (edges) to add/delete them");
			panel_right.add(keyForEdges, BorderLayout.SOUTH);
			
			splitPane.setRightComponent(panel_right);
			
			JPanel panel_1 = new JPanel();
			jPanelAnalysis.add(panel_1, BorderLayout.NORTH);
		
			
			JButton btnCreateMutantFrom = new JButton("Create new analysis settings");
			btnCreateMutantFrom.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					String name=JOptionPane.showInputDialog("Provide a name for the new setting:");
					if(name == null || name.trim().length() == 0) {
						return;
					}
					
					while(simDB.isNameDuplicate(name)) {
						JOptionPane.showMessageDialog(null, "The name already exists.\nProvide a different name.", "Error!", JOptionPane.ERROR_MESSAGE);
						name=JOptionPane.showInputDialog("Provide a name for the new setting:", name);
						if(name==null) return;
					}
					name = simDB.cleanMutantName(name);
						
					Simulation newMutant = new Simulation(name);
					simDB.addSimulation(newMutant);
					
					DefaultGraphCell cell = m_jgAdapter_analysis1.getVertexCell(newMutant);
					cell.addPort();
					Map nested = new Hashtable();
					Map withRelocation = new Hashtable();
					withRelocation.putAll(basicCell_attributeMap);
					//the bounds values are not used because the graph has setResize, but bounds need to be non null to trigger that
					AttributeMap allAttributes = cell.getAttributes();
					java.awt.geom.Rectangle2D bounds = cell.getAttributes().createRect(10, 10, 20, 20);
					GraphConstants.setBounds(allAttributes, bounds);
					nested.put(cell, basicCell_attributeMap);
					
					jgraph_graph_analysis1.getGraphLayoutCache().edit(nested, null, null, null);
					sortJListAnalysis1();
				}
			});
			
			panel_1.add(btnCreateMutantFrom);
			
			JButton btnChangeSelectedConfiguration = new JButton("Change selected configuration");
			btnChangeSelectedConfiguration.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
						if(jgraph_graph_analysis1.getSelectionCells().length>0) {
							Object cell = jgraph_graph_analysis1.getSelectionCells()[0];
							openSingleAnalysis1Frame(cell);
						}
					}
				}
			);
				
			panel_1.add(btnChangeSelectedConfiguration);
			
			JButton btnCreateChildConfiguration = new JButton("Create child configuration");
			btnCreateChildConfiguration.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Object[] parents = jgraph_graph_analysis1.getSelectionCells();
					Vector<DefaultGraphCell> parentNodes = new Vector<DefaultGraphCell>();
					for(int i = 0; i < parents.length; i++) {
						Object p = parents[i];
						if( p instanceof DefaultEdge ||
							p instanceof DefaultPort) {continue;}
						else{
							parentNodes.add((DefaultGraphCell) p);
						}
					}
					if(parentNodes.size() == 0) return;
					
					String name=JOptionPane.showInputDialog("Provide a name for the new "+ jTabRM.getTitleAt(1)+" Analysis settings:");
					if(name == null || name.trim().length() == 0) {
						return;
					}
					
					while(simDB.isNameDuplicate(name)) {
						JOptionPane.showMessageDialog(null, "The name already exists.\nProvide a different name.", "Error!", JOptionPane.ERROR_MESSAGE);
						name=JOptionPane.showInputDialog("Provide a name for the new "+ jTabRM.getTitleAt(1)+" Analysis settings:", name);
						if(name==null) return;
					}
					name = simDB.cleanMutantName(name);
					Simulation newMutantSim = new Simulation(name);
					
					simDB.addSimulation(newMutantSim);
					DefaultGraphCell cell = m_jgAdapter_analysis1.getVertexCell(newMutantSim);
					Map nested = new Hashtable();
					Map withPossibleConflict = new Hashtable(basicCell_attributeMap);
					//the bounds values are not used because the graph has setResize, but bounds need to be non null to trigger that
					AttributeMap allAttributes = cell.getAttributes();
					java.awt.geom.Rectangle2D bounds = cell.getAttributes().createRect(10, 10, 20, 20);
					GraphConstants.setBounds(allAttributes, bounds);
					
					Vector<org.jgrapht.graph.DefaultEdge> connection = new Vector<org.jgrapht.graph.DefaultEdge>();
					for(int i = 0; i < parentNodes.size(); i++) {
						org.jgrapht.graph.DefaultEdge ed = simDB.addConnection(newMutantSim, (Simulation) (parentNodes.get(i)).getUserObject());
						connection.add(ed);
					}
					
					boolean conflict = simDB.detectConflict(newMutantSim).size() > 0;
					if(conflict) {
						for(int i = 0; i < connection.size(); i++) {
							DefaultEdge edge = m_jgAdapter_analysis1.getEdgeCell(connection.get(i));
							GraphConstants.setLineBegin(withPossibleConflict, GraphConstants.ARROW_CIRCLE);
							GraphConstants.setBeginFill(withPossibleConflict, true);
							GraphConstants.setDashPattern(withPossibleConflict, new float[] {10,5});
							nested.put(edge, withPossibleConflict);
						}
					}
					nested.put(cell, withPossibleConflict);
					
					jgraph_graph_analysis1.getGraphLayoutCache().edit(nested, null, null, null);
					sortJListAnalysis1();
				}
			});
			panel_1.add(btnCreateChildConfiguration);
			
		
	}
	

	private void populateJPanelParameterLists() {
		
		
	   splitPaneGraph_parameters = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPaneGraph_parameters.setDividerLocation(350);
		splitPaneGraph_parameters.setResizeWeight(0.5);
		
		splitPaneGraph_parameters.revalidate();
	
		
		jpanelParameterLists.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		jpanelParameterLists.add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.2);
		panel.add(splitPane, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane();
		JPanel panel_left= new JPanel();
		panel_left.setLayout(new BorderLayout(3, 3));
		panel_left.add(scrollPane, BorderLayout.CENTER);
		splitPane.setLeftComponent(panel_left);
		JLabel label_mutantList = new JLabel(" List of existing mutants:");
		panel_left.add(label_mutantList, BorderLayout.NORTH);
		
		listModel_mutants= new DefaultListModel();
		JList jListMutants = new JList(listModel_mutants);
    	scrollPane.setViewportView(jListMutants);
    	jListMutants.addListSelectionListener(new ListSelectionListener() {
    	    public void valueChanged(ListSelectionEvent event) {
    	        if (!event.getValueIsAdjusting()){
    	            JList source = (JList)event.getSource();
    	            Object[] selected = source.getSelectedValues();
    	            ArrayList<Object> selectedGraphNodes = new ArrayList<Object>();
    	            for(int i = 0; i < selected.length; ++i) {
    	            	selectedGraphNodes.add(m_jgAdapter.getVertexCell(selected[i]));
    	            } 
    	            jgraph_graph_parameters.setSelectionCells(selectedGraphNodes.toArray());
    	        }
    	    }
    	});
    	
    	JPanel panel_right= new JPanel();
    	panel_right.setLayout(new BorderLayout(3, 3));
    	panel_right.add(splitPaneGraph_parameters, BorderLayout.CENTER);
		JLabel keyForEdges = new JLabel(" Hold SHIFT to add/delete control points on edges");
		panel_right.add(keyForEdges, BorderLayout.SOUTH);
		
		splitPane.setRightComponent(panel_right);
		
		JPanel panel_1 = new JPanel();
		jpanelParameterLists.add(panel_1, BorderLayout.NORTH);
		
		
		JButton btnCreateMutantFrom = new JButton("Create mutant from current model values");
		btnCreateMutantFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String name=JOptionPane.showInputDialog("Provide a name for the new Mutant:");
				if(name == null || name.trim().length() == 0) {
					return;
				}
				
				while(mutantsDB.isNameDuplicate(name)) {
					JOptionPane.showMessageDialog(null, "The name already exists.\nProvide a different name.", "Error!", JOptionPane.ERROR_MESSAGE);
					name=JOptionPane.showInputDialog("Provide a name for the new Mutant:", name);
					if(name==null) return;
				}
				name = mutantsDB.cleanMutantName(name);
					
				Mutant newMutant = new Mutant(name);
				mutantsDB.addMutant(newMutant);
				
				DefaultGraphCell cell = m_jgAdapter.getVertexCell(newMutant);
				cell.addPort();
				Map nested = new Hashtable();
				Map withRelocation = new Hashtable();
				withRelocation.putAll(basicCell_attributeMap);
				//the bounds values are not used because the graph has setResize, but bounds need to be non null to trigger that
				AttributeMap allAttributes = cell.getAttributes();
				java.awt.geom.Rectangle2D bounds = cell.getAttributes().createRect(10, 10, 20, 20);
				GraphConstants.setBounds(allAttributes, bounds);
				nested.put(cell, basicCell_attributeMap);
				
				jgraph_graph_parameters.getGraphLayoutCache().edit(nested, null, null, null);
				sortJListMutants();
			}
		});
		
		panel_1.add(btnCreateMutantFrom);
		
		JButton btnChangeSelectedConfiguration = new JButton("Change selected configuration");
		btnChangeSelectedConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					Object cell = jgraph_graph_parameters.getSelectionCells()[0];
					openSingleMutantFrame(cell);
				}
			}
		);
			
		panel_1.add(btnChangeSelectedConfiguration);
		
		JButton btnCreateChildConfiguration = new JButton("Create child configuration");
		btnCreateChildConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] parents = jgraph_graph_parameters.getSelectionCells();
				Vector<DefaultGraphCell> parentNodes = new Vector<DefaultGraphCell>();
				for(int i = 0; i < parents.length; i++) {
					Object p = parents[i];
					if( p instanceof DefaultEdge ||
						p instanceof DefaultPort) {continue;}
					else{
						parentNodes.add((DefaultGraphCell) p);
					}
				}
				if(parentNodes.size() == 0) return;
				
				String name=JOptionPane.showInputDialog("Provide a name for the new Mutant:");
				if(name == null || name.trim().length() == 0) {
					return;
				}
				
				while(mutantsDB.isNameDuplicate(name)) {
					JOptionPane.showMessageDialog(null, "The name already exists.\nProvide a different name.", "Error!", JOptionPane.ERROR_MESSAGE);
					name=JOptionPane.showInputDialog("Provide a name for the new Mutant:", name);
					if(name==null) return;
				}
				name = mutantsDB.cleanMutantName(name);
				Mutant newMutant = new Mutant(name);
				
				mutantsDB.addMutant(newMutant);
					
				DefaultGraphCell cell = m_jgAdapter.getVertexCell(newMutant);
				Map nested = new Hashtable();
				Map withPossibleConflict = new Hashtable(basicCell_attributeMap);
				//the bounds values are not used because the graph has setResize, but bounds need to be non null to trigger that
				AttributeMap allAttributes = cell.getAttributes();
				java.awt.geom.Rectangle2D bounds = cell.getAttributes().createRect(10, 10, 20, 20);
				GraphConstants.setBounds(allAttributes, bounds);
				
				Vector<org.jgrapht.graph.DefaultEdge> connection = new Vector<org.jgrapht.graph.DefaultEdge>();
				for(int i = 0; i < parentNodes.size(); i++) {
					org.jgrapht.graph.DefaultEdge ed = mutantsDB.addConnection(newMutant, (Mutant) (parentNodes.get(i)).getUserObject());
					connection.add(ed);
				}
				
				boolean conflict = mutantsDB.detectConflict(newMutant).size() > 0;
				if(conflict) {
					for(int i = 0; i < connection.size(); i++) {
						DefaultEdge edge = m_jgAdapter.getEdgeCell(connection.get(i));
						GraphConstants.setLineBegin(withPossibleConflict, GraphConstants.ARROW_CIRCLE);
						GraphConstants.setBeginFill(withPossibleConflict, true);
						GraphConstants.setDashPattern(withPossibleConflict, new float[] {10,5});
						nested.put(edge, withPossibleConflict);
					}
				}
				nested.put(cell, withPossibleConflict);
				
				jgraph_graph_parameters.getGraphLayoutCache().edit(nested, null, null, null);
				sortJListMutants();
			}
		});
		panel_1.add(btnCreateChildConfiguration);
		
	}

	/*protected void runLayout() {
		Object cell = mxGraph_graph.getSelectionCell();
		
		if (cell == null
				|| mxGraph_graph.getModel().getChildCount(cell) == 0)
		{
			cell = mxGraph_graph.getDefaultParent();
		}
		mxGraph_graph.getModel().beginUpdate();
		try
		{
			long t0 = System.currentTimeMillis();
			//layout.execute(cell);
		}
		finally
		{
			mxMorphing morph = new mxMorphing(RunManager.graphComponent_mutants, 20,1.2, 20);

			morph.addListener(mxEvent.DONE, new mxIEventListener()
			{

				public void invoke(Object sender, mxEventObject evt)
				{
					mxGraph_graph.getModel().endUpdate();
				}

			});

			morph.startAnimation();
			RunManager.graphComponent_mutants.zoomAndCenter();
		}
	}
	*/
	/**
	 * Executes the current layout on the current graph by creating a facade and
	 * progress monitor for the layout and invoking it's run method in a
	 * separate thread so this method call returns immediately. To display the
	 * result of the layout algorithm a {@link JGraphLayoutMorphingManager} is
	 * used.
	 */
	public void execute(final JGraphLayout layout, final JGraph currentGraph) {
		if (currentGraph != null && currentGraph.isEnabled() && currentGraph.isMoveable()
				&& layout != null) {
			final JGraphFacade facade = createFacade(currentGraph);
			facade.resetControlPoints();
			final ProgressMonitor progressMonitor = null;
			new Thread() {
				public void run() {
					synchronized (this) {
						try {
							// Executes the layout and checks if the user has
							// clicked
							// on cancel during the layout run. If no progress
							// monitor
							// has been displayed or cancel has not been pressed
							// then
							// the result of the layout algorithm is processed.
							layout.run(facade);

							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									boolean ignoreResult = false;
									if (progressMonitor != null) {
										ignoreResult = progressMonitor
												.isCanceled();
										progressMonitor.close();
									}
									if (!ignoreResult) {

										// Processes the result of the layout
										// algorithm
										// by creating a nested map based on the
										// global
										// settings and passing the map to a
										// morpher
										// for the graph that should be changed.
										// The morpher will animate the change
										// and then
										// invoke the edit method on the graph
										// layout
										// cache.
										Map map = facade.createNestedMap(true, true);
										morpher.morph(currentGraph, map);
										currentGraph.requestFocus();
									}
								}
							});
						} catch (Exception e) {
							e.printStackTrace();
							JOptionPane
									.showMessageDialog(currentGraph, e.getMessage());
						}
					}
				}
			}.start(); // fork
		}
	}

	/**
	 * Creates a {@link JGraphFacade} and makes sure it contains a valid set of
	 * root cells if the specified layout is a tree layout. A root cell in this
	 * context is one that has no incoming edges.
	 * 
	 * @param graph
	 *            The graph to use for the facade.
	 * @return Returns a new facade for the specified layout and graph.
	 */
	protected JGraphFacade createFacade(JGraph graph) {
		// Creates and configures the facade using the global switches
		JGraphFacade facade = new JGraphFacade(graph, graph.getSelectionCells());
		facade.setIgnoresUnconnectedCells(true);
		facade.setIgnoresCellsInGroups(true);
		facade.setIgnoresHiddenCells(true);
		return facade;
	}


	public static void main(String[] args)
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		RunManager frame = new RunManager();
		//frame.initializeCopasiModel()
	}
	
	/*private void styleSetup(mxGraph graph)
	{
		mxStylesheet styleSheet = graph.getStylesheet();
		Map<String, Object> basicSetup = new HashMap<String, Object>();
		
		Map<String, Object> specificSetup = new HashMap<String, Object>();
		 mxConstants.SHADOW_OFFSETX = 5;
		 mxConstants.SHADOW_OFFSETY = 5;
		 basicSetup.put(mxConstants.STYLE_STROKECOLOR, "blue");
		 basicSetup.put(mxConstants.STYLE_FILLCOLOR, "white");
		 basicSetup.put(mxConstants.STYLE_STROKEWIDTH, "2.0");
		 basicSetup.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
		 basicSetup.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
		 basicSetup.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_MIDDLE);
		 basicSetup.put(mxConstants.STYLE_FONTFAMILY, "Times New Roman");
		 basicSetup.put(mxConstants.STYLE_FONTSIZE, "12");
		 basicSetup.put(mxConstants.STYLE_FONTSTYLE, "1");
		 basicSetup.put(mxConstants.STYLE_FONTCOLOR, "black");
		 basicSetup.put(mxConstants.STYLE_FOLDABLE, "0");
		 basicSetup.put(mxConstants.STYLE_SHADOW, "true");
		 basicSetup.put(mxConstants.STYLE_AUTOSIZE, "1"); 
		 basicSetup.put(mxConstants.STYLE_EDITABLE, "0"); 
		 
		 basicSetup.put(mxConstants.STYLE_ORTHOGONAL, true);
		 basicSetup.put(mxConstants.STYLE_BENDABLE, false);
		 basicSetup.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_SEGMENT);
		 
		 specificSetup.putAll(basicSetup);
		 specificSetup.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		 specificSetup.put(mxConstants.STYLE_ROUNDED, "true");
		styleSheet.putCellStyle(MUTANT_BASIC, specificSetup);
		
	
		
		specificSetup = new HashMap<String, Object>();
		 specificSetup.putAll(basicSetup);
		 specificSetup.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE);
		 specificSetup.put(mxConstants.STYLE_ROUNDED, "true");
		styleSheet.putCellStyle(SIMULATION_BASIC, specificSetup);
		
	}

	static public mxGraphComponent getGraphComponent()
	{
		return graphComponent_mutants;
	}
	
	protected void showGraphPopupMenu(MouseEvent e)
	{
		Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(),graphComponent_mutants);
		PopupMenuNode menu = new PopupMenuNode();
		menu.show(graphComponent_mutants, pt.x, pt.y);
		graphComponent_mutants.getGraph().setSelectionCell(graphComponent_mutants.getCellAt(pt.x, pt.y));
		graphComponent_mutants.getGraph().refresh();
		e.consume();
	}
*/

	//
	// Custom MarqueeHandler

	// MarqueeHandler that Connects Vertices and Displays PopupMenus
	class MyMarqueeHandler extends BasicMarqueeHandler {

		// Holds the Start and the Current Point
		protected Point2D start, current;

		// Holds the First and the Current Port
		protected PortView port, firstPort;

		/**
		 * Component that is used for highlighting cells if
		 * the graph does not allow XOR painting.
		 */
		protected JComponent highlight = new JPanel();

		private JGraph jgraph_graph_local;
		public MyMarqueeHandler(JGraph jgraph) {
			// Configures the panel for highlighting ports
			highlight = createHighlight();
			jgraph_graph_local = jgraph;
		}

		/**
		 * Creates the component that is used for highlighting cells if
		 * the graph does not allow XOR painting.
		 */
		protected JComponent createHighlight() {
			JPanel panel = new JPanel();
		//	panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			panel.setVisible(false);
			panel.setOpaque(false);
			return panel;
		}
		
		// Override to Gain Control (for PopupMenu and ConnectMode)
		public boolean isForceMarqueeEvent(MouseEvent e) {
			if (e.isShiftDown())
				return false;
			// If Right Mouse Button we want to Display the PopupMenu
			if (SwingUtilities.isRightMouseButton(e))
				// Return Immediately
				return true;
			
			if(e.getClickCount() > 1) {
				Object cell = jgraph_graph_local.getFirstCellForLocation(e.getX(), e.getY());
				if(cell==null) return true;
				if(((DefaultGraphCell)cell).getUserObject() instanceof Simulation) {
					openSingleAnalysis1Frame(cell);
				} else	if(((DefaultGraphCell)cell).getUserObject() instanceof RMPlot) {
					openSinglePlotSettings(cell);
				}else {
					openSingleMutantFrame(cell);
				}
				return super.isForceMarqueeEvent(e);
			}
			
			// Find and Remember Port
			port = getSourcePortAt(e.getPoint());
			// If Port Found and in ConnectMode (=Ports Visible)
			if (port != null)// && graph.isPortsVisible())
				return true;
			// Else Call Superclass
			return super.isForceMarqueeEvent(e);
		}

		// Display PopupMenu or Remember Start Location and First Port
		public void mousePressed(final MouseEvent e) {
			// If Right Mouse Button
			if (SwingUtilities.isRightMouseButton(e)) {
				// Find Cell in Model Coordinates
				Object cell = jgraph_graph_local.getFirstCellForLocation(e.getX(), e.getY());
				if(cell != null) {
					// Create PopupMenu for the Cell
					JPopupMenu menu = null;
					if(((DefaultGraphCell)cell).getUserObject() instanceof Simulation) {
						menu = createPopupMenu_simulations(e.getPoint(), cell);
					} else if(((DefaultGraphCell)cell).getUserObject() instanceof RMPlot) {
						menu = createPopupMenu_rmplot(e.getPoint(), cell);
					}else {
						menu = createPopupMenu_parameters(e.getPoint(), cell);
					} 
					
					// Display PopupMenu
					menu.show(jgraph_graph_local, e.getX(), e.getY());
				}
				// Else if in ConnectMode and Remembered Port is Valid
			} else if (port != null){ // && jgraph_graph.isPortsVisible()) {
				
				if(e.getClickCount() > 1) {
					//Object cell = jgraph_graph.getFirstCellForLocation(e.getX(), e.getY());
					return;
				}
						
				// Remember Start Location
				start = jgraph_graph_local.toScreen(port.getLocation());
				// Remember First Port
				firstPort = port;
			} else {
				// Call Superclass
				super.mousePressed(e);
			}
		}

		// Find Port under Mouse and Repaint Connector
		public void mouseDragged(MouseEvent e) {
			// If remembered Start Point is Valid
			if (start != null) {
				// Fetch Graphics from Graph
				Graphics g = jgraph_graph_local.getGraphics();
				// Reset Remembered Port
				PortView newPort = getTargetPortAt(e.getPoint());
				// Do not flicker (repaint only on real changes)
				if (newPort == null || newPort != port) {
					// Xor-Paint the old Connector (Hide old Connector)
					paintConnector(Color.black, jgraph_graph_parameters.getBackground(), g);
					// If Port was found then Point to Port Location
					port = newPort;
					if (port != null)
						current = jgraph_graph_local.toScreen(port.getLocation());
					// Else If no Port was found then Point to Mouse Location
					else
						current = jgraph_graph_local.snap(e.getPoint());
					// Xor-Paint the new Connector
					paintConnector(jgraph_graph_local.getBackground(), Color.black, g);
				}
			}
			// Call Superclass
			super.mouseDragged(e);
		}

		public PortView getSourcePortAt(Point2D point) {
			// Disable jumping
			jgraph_graph_local.setJumpToDefaultPort(false);
			PortView result;
			try {
				// Find a Port View in Model Coordinates and Remember
				result = jgraph_graph_local.getPortViewAt(point.getX(), point.getY());
			} finally {
				jgraph_graph_local.setJumpToDefaultPort(true);
			}
			return result;
		}

		// Find a Cell at point and Return its first Port as a PortView
		protected PortView getTargetPortAt(Point2D point) {
			// Find a Port View in Model Coordinates and Remember
			return jgraph_graph_local.getPortViewAt(point.getX(), point.getY());
		}
		
		// Insert a new Edge between source and target
		public void connect_old(Port source, Port target) {
			// Construct Edge with no label
			DefaultEdge edge = new DefaultEdge();
			//!! here source and target can be empty but still the connection is established
			if (jgraph_graph_local.getModel().acceptsSource(edge, source)
					&& jgraph_graph_local.getModel().acceptsTarget(edge, target)) {
				// Create a Map thath holds the attributes for the edge
				edge.getAttributes().applyMap(basicCell_attributeMap);
				// Insert the Edge and its Attributes
				jgraph_graph_local.getGraphLayoutCache().insertEdge(edge, source, target);
			}
		}
		
		// Insert a new Edge between source and target
		public void connect(Object source, Object target) {
			if(source == null || target == null) return;
			Object sourceUserObject = ((DefaultGraphCell)source).getUserObject();
			Object targetUserObject = ((DefaultGraphCell)target).getUserObject();
			if(sourceUserObject instanceof Simulation) {
				simDB.addConnection((Simulation)sourceUserObject, (Simulation) targetUserObject);
			} else if(sourceUserObject instanceof RMPlot) {
				plotDB.addConnection((RMPlot)sourceUserObject, (RMPlot) targetUserObject);
			}else {//mutant parameters
				mutantsDB.addConnection((Mutant) sourceUserObject, (Mutant)targetUserObject);
			} 
		}
		
		

		// Connect the First Port and the Current Port in the Graph or Repaint
		public void mouseReleased(MouseEvent e) {
			highlight(jgraph_graph_local, null);
			
			// If Valid Event, Current and First Port
			if (e != null && port != null && firstPort != null
					&& firstPort != port) {
				// Then Establish Connection
				connect(firstPort.getParentView().getCell(),port.getParentView().getCell());
				e.consume();
				// Else Repaint the Graph
			} else {
				jgraph_graph_local.repaint();
			}
			
			if(start != null) {
				Object cell = jgraph_graph_local.getFirstCellForLocation(start.getX(), start.getY());
				if(cell != null) {
					Vector<Mutant> toRevalidate = new Vector<Mutant>();
					Object end = ((DefaultGraphCell)cell).getUserObject();
					if(end instanceof Mutant) {
						toRevalidate.add((Mutant) end);
						revalidateConflicts(toRevalidate);
					}
					
				}
			}
			
			// Reset Global Vars
			firstPort = port = null;
			start = current = null;
			// Call Superclass
			super.mouseReleased(e);
		}

		// Show Special Cursor if Over Port
		public void mouseMoved(MouseEvent e) {
			// Check Mode and Find Port
			if (e != null && getSourcePortAt(e.getPoint()) != null
					//&& jgraph_graph.isPortsVisible()
					) {
				// Set Cusor on Graph (Automatically Reset)
				jgraph_graph_local.setCursor(new Cursor(Cursor.HAND_CURSOR));
				// Consume Event
				// Note: This is to signal the BasicGraphUI's
				// MouseHandle to stop further event processing.
				e.consume();
			} else
				// Call Superclass
				super.mouseMoved(e);
		}

		// Use Xor-Mode on Graphics to Paint Connector
		protected void paintConnector(Color fg, Color bg, Graphics g) {
			if (jgraph_graph_local.isXorEnabled()) {
				// Set Foreground
				g.setColor(fg);
				// Set Xor-Mode Color
				g.setXORMode(bg);
				// Highlight the Current Port
				paintPort(jgraph_graph_local.getGraphics());
				
				drawConnectorLine(g);
			} else {
				Rectangle dirty = new Rectangle((int) start.getX(), (int) start.getY(), 1, 1);
				
				if (current != null) {
					dirty.add(current);
				}
				
				dirty.grow(1, 1);
				
				jgraph_graph_parameters.repaint(dirty);
				highlight(jgraph_graph_local, port);
			}
		}
		
		// Overrides parent method to paint connector if
		// XOR painting is disabled in the graph
		public void paint(JGraph graph, Graphics g)
		{
			super.paint(graph, g);
			
			if (!graph.isXorEnabled())
			{
				g.setColor(Color.black);
				drawConnectorLine(g);
			}
		}
		
		protected void drawConnectorLine(Graphics g) {
			if (firstPort != null && start != null && current != null) {
				// Then Draw A Line From Start to Current Point
				g.drawLine((int) start.getX(), (int) start.getY(),
						(int) current.getX(), (int) current.getY());
			}
		}

		// Use the Preview Flag to Draw a Highlighted Port
		protected void paintPort(Graphics g) {
			// If Current Port is Valid
			if (port != null) {
				// If Not Floating Port...
				boolean o = (GraphConstants.getOffset(port.getAllAttributes()) != null);
				// ...Then use Parent's Bounds
				java.awt.geom.Rectangle2D r = (o) ? port.getBounds() : port.getParentView()
						.getBounds();
				// Scale from Model to Screen
				r = jgraph_graph_local.toScreen((java.awt.geom.Rectangle2D) r.clone());
				// Add Space For the Highlight Border
				r.setFrame(r.getX() - 3, r.getY() - 3, r.getWidth() + 6, r
						.getHeight() + 6);
				// Paint Port in Preview (=Highlight) Mode
				jgraph_graph_local.getUI().paintCell(g, port, r, true);
			}
		}

		/**
		 * Highlights the given cell view or removes the highlight if
		 * no cell view is specified.
		 * 
		 * @param graph
		 * @param cellView
		 */
		protected void highlight(JGraph graph, CellView cellView)
		{
			if (cellView != null)
			{
				highlight.setBounds(getHighlightBounds(graph, cellView));

				if (highlight.getParent() == null)
				{
					graph.add(highlight);
					highlight.setVisible(true);
				}
			}
			else
			{
				if (highlight.getParent() != null)
				{
					highlight.setVisible(false);
					highlight.getParent().remove(highlight);
				}
			}
		}

		/**
		 * Returns the bounds to be used to highlight the given cell view.
		 * 
		 * @param graph
		 * @param cellView
		 * @return
		 */
		protected Rectangle getHighlightBounds(JGraph graph, CellView cellView)
		{
			boolean offset = (GraphConstants.getOffset(cellView.getAllAttributes()) != null);
			java.awt.geom.Rectangle2D r = (offset) ? cellView.getBounds() : cellView
					.getParentView().getBounds();
			r = jgraph_graph_local.toScreen((java.awt.geom.Rectangle2D) r.clone());
			int s = 3;

			return new Rectangle((int) (r.getX() - s), (int) (r.getY() - s),
					(int) (r.getWidth() + 2 * s), (int) (r.getHeight() + 2 * s));
		}


	} // End of Editor.MyMarqueeHandler


	

	public JPopupMenu createPopupMenu_parameters(final Point pt, final Object cell) {
		JPopupMenu menu = new JPopupMenu();
		if (cell != null && !(cell instanceof DefaultEdge)) {
			menu.add(new AbstractAction("Edit") {
				public void actionPerformed(ActionEvent e) {
					openSingleMutantFrame(cell);
				}
			});
		

		if (!jgraph_graph_parameters.isSelectionEmpty()) {
			menu.add(new AbstractAction("Remove") {
				public void actionPerformed(ActionEvent e) {
					remove.actionPerformed(e);
				}
			});
			menu.addSeparator();
			
			menu.add(new AbstractAction("Add new analysis") {
					public void actionPerformed(ActionEvent e) {
						Object[] sel = jgraph_graph_parameters.getSelectionCells();
						 Object[] selectionValues = { "Time course"};
						    String initialSelection = "Time course";
						    Object selection = JOptionPane.showInputDialog(null, "Which analysis node do you want to add?",
						        "Analysis", JOptionPane.QUESTION_MESSAGE, null, selectionValues, initialSelection);
						    if(selection!=initialSelection) return;
						addAnalysis1Node(sel);
					
					}
				});
			
			menu.add(new AbstractAction("Add to existing analysis node") {
				Object selSim = null;
		
				public void actionPerformed(ActionEvent e) {
					Object[] sel = jgraph_graph_parameters.getSelectionCells();
					Vector<Mutant> which = new Vector<Mutant>();
					for(int i = 0; i < sel.length; i++) {
						Object s = sel[i];
						if( s instanceof DefaultEdge ||
							s instanceof DefaultPort) {continue;}
						else{
							which.add((Mutant) ((DefaultGraphCell)s).getUserObject());
				 		}
					}
					if(which.size() == 0) {
						//only edges or ports are selected, so nothing to do
						return;
					}
				
					JPanel main = new JPanel();
				    main.setLayout(new GridLayout(2,1));

				    JComboBox analysis = new JComboBox();
				    final JComboBox names = new JComboBox();
				    analysis.addItem("Time course");
				    analysis.setEditable(false);
				    names.setEditable(false);
				    main.add(analysis);
				    main.add(names);
				    Iterator it = simDB.getVertexSet().iterator();
				    while(it.hasNext()) {
				    	names.addItem(it.next());
				    }
				   	GraphicalProperties.resetFonts(main);		    
				    int input = JOptionPane.showConfirmDialog(null, main, "Add mutant parameters to:"
	                        ,JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				    selSim = names.getSelectedItem();
				    if (input == 0 && selSim !=null) {
			            // OK
				    	selSim = names.getSelectedItem();
				    	changeAnalysis1Node(which, (Simulation) selSim);
			        } else {
			            // Cancel
			        	return;
			        }
				}
			});
			}
		}
		return menu;
	}
	
	public JPopupMenu createPopupMenu_simulations(final Point pt, final Object cell) {
		JPopupMenu menu = new JPopupMenu();
		if (cell != null && !(cell instanceof DefaultEdge)) {
			menu.add(new AbstractAction("Edit") {
				public void actionPerformed(ActionEvent e) {
					openSingleAnalysis1Frame(cell);
				}
			});
		

		if (!jgraph_graph_analysis1.isSelectionEmpty()) {
			menu.add(new AbstractAction("Remove") {
				public void actionPerformed(ActionEvent e) {
					remove.actionPerformed(e);
				}
			});
			menu.addSeparator();
			
			menu.add(new AbstractAction("Add new plotting") {
					public void actionPerformed(ActionEvent e) {
						Object[] sel = jgraph_graph_analysis1.getSelectionCells();
						addRMPlotNode(sel);
					}
				});
			
			menu.add(new AbstractAction("Add to existing plotting node") {
				Object selPlot = null;
		
				public void actionPerformed(ActionEvent e) {
					Object[] sel = jgraph_graph_analysis1.getSelectionCells();
					Vector<Simulation> which = new Vector<Simulation>();
					for(int i = 0; i < sel.length; i++) {
						Object s = sel[i];
						if( s instanceof DefaultEdge ||
							s instanceof DefaultPort) {continue;}
						else{
							which.add((Simulation) ((DefaultGraphCell)s).getUserObject());
				 		}
					}
					if(which.size() == 0) {
						//only edges or ports are selected, so nothing to do
						return;
					}
				
					JPanel main = new JPanel();
				    main.setLayout(new GridLayout(2,1));

				    JLabel add = new JLabel();
				    final JComboBox names = new JComboBox();
				    add.setText("Plotting");
				    names.setEditable(false);
				    main.add(add);
				    main.add(names);
				    Iterator it = plotDB.getVertexSet().iterator();
				    while(it.hasNext()) {
				    	names.addItem(it.next());
				    }
				   	GraphicalProperties.resetFonts(main);		    
				    int input = JOptionPane.showConfirmDialog(null, main, "Add simulation to:"
	                        ,JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				    selPlot = names.getSelectedItem();
				    if (input == 0 && selPlot !=null) {
			            // OK
				    	selPlot = names.getSelectedItem();
				    	changePlottingNode(which, (RMPlot) selPlot);
			        } else {
			            // Cancel
			        	return;
			        }
				}
			});
			}
		}
		return menu;
	}
	
	public JPopupMenu createPopupMenu_rmplot(final Point pt, final Object cell) {
		JPopupMenu menu = new JPopupMenu();
		if (cell != null && !(cell instanceof DefaultEdge)) {
			menu.add(new AbstractAction("Edit") {
				public void actionPerformed(ActionEvent e) {
					openSinglePlotSettings(cell);
				}
			});
		
		if (!jgraph_graph_plot.isSelectionEmpty()) {
			menu.add(new AbstractAction("Remove") {
				public void actionPerformed(ActionEvent e) {
					remove.actionPerformed(e);
				}
			});
			menu.addSeparator();
	      }
		}
		return menu;
	}
	

	
	protected void addRMPlotNode(Object[] selectedCells) {
		
	
		Vector<String> which = new Vector<String>();
		if(selectedCells!= null) {
			for(int i = 0; i < selectedCells.length; i++) {
				Object s = selectedCells[i];
				if( s instanceof DefaultEdge ||
					s instanceof DefaultPort) {continue;}
				else{
					which.add(((Simulation) ((DefaultGraphCell)s).getUserObject()).getName());
		 		}
			}
		}
		
		String name=JOptionPane.showInputDialog("Provide a name for the new setting:");
		if(name == null || name.trim().length() == 0) {
			return;
		}
		
		while(plotDB.isNameDuplicate(name)) {
			JOptionPane.showMessageDialog(null, "The name already exists.\nProvide a different name.", "Error!", JOptionPane.ERROR_MESSAGE);
			name=JOptionPane.showInputDialog("Provide a name for the new setting:", name);
			if(name==null) return;
		}
		name = plotDB.cleanMutantName(name);
			
		RMPlot newMutant = new RMPlot(name);
		newMutant.addAllSimulations(which);
		plotDB.addPlot(newMutant);
		
		
		DefaultGraphCell cell = m_jgAdapter_plot.getVertexCell(newMutant);
		cell.addPort();
		Map nested = new Hashtable();
		Map withRelocation = new Hashtable();
		withRelocation.putAll(basicCell_attributeMap);
		//the bounds values are not used because the graph has setResize, but bounds need to be non null to trigger that
		AttributeMap allAttributes = cell.getAttributes();
		java.awt.geom.Rectangle2D bounds = cell.getAttributes().createRect(10, 10, 20, 20);
		GraphConstants.setBounds(allAttributes, bounds);
		nested.put(cell, basicCell_attributeMap);
		
		jgraph_graph_plot.getGraphLayoutCache().edit(nested, null, null, null);
		sortJListPlot();
		
	}
	protected void changePlottingNode(Vector<Simulation> which, RMPlot selPlot) {
	 	Iterator<Simulation> it2 = which.iterator();
		while(it2.hasNext()) {
			Simulation m = it2.next();
			((RMPlot)selPlot).addSimulation(m.getName());
		}
		sortJListPlot();
	}
	protected void changeAnalysis1Node(Vector<Mutant> which, Simulation selSim) {
	 	Iterator<Mutant> it2 = which.iterator();
		while(it2.hasNext()) {
			Mutant m = it2.next();
			((Simulation)selSim).addMutantParameter(m);
		}
		sortJListAnalysis1();
	}
	
	protected void addAnalysis1Node(Object[] selectedCells) {
		
		Vector<Mutant> which = new Vector<Mutant>();
		for(int i = 0; i < selectedCells.length; i++) {
			Object s = selectedCells[i];
			if( s instanceof DefaultEdge ||
				s instanceof DefaultPort) {continue;}
			else{
				which.add((Mutant) ((DefaultGraphCell)s).getUserObject());
	 		}
		}
		
		if(which.size() == 0) {
			//only edges or ports are selected, so nothing to do
			return;
		}
		
		String name=JOptionPane.showInputDialog("Provide a name for the "+jTabRM.getTitleAt(1)+" Analysis node:");
		if(name == null || name.trim().length() == 0) {
			return;
		}
		
		while(simDB.isNameDuplicate(name)) {
			JOptionPane.showMessageDialog(null, "The name already exists.\nProvide a different name.", "Error!", JOptionPane.ERROR_MESSAGE);
			name=JOptionPane.showInputDialog("Provide a name for the new"+jTabRM.getTitleAt(1)+" node:", name);
			if(name==null) return;
		}
		name = simDB.cleanMutantName(name);
		
		Simulation base = new Simulation(name);
		Iterator<Mutant> it = which.iterator();
		while(it.hasNext()) {
			Mutant m = it.next();
			base.addMutantParameter(m);
		}
		
		simDB.addSimulation(base);
		
		DefaultGraphCell cell = m_jgAdapter_analysis1.getVertexCell(base);
		cell.addPort();
		Map nested = new Hashtable();
		Map withRelocation = new Hashtable();
		withRelocation.putAll(basicCell_attributeMap);
		//the bounds values are not used because the graph has setResize, but bounds need to be non null to trigger that
		AttributeMap allAttributes = cell.getAttributes();
		java.awt.geom.Rectangle2D bounds = cell.getAttributes().createRect(10, 10, 20, 20);
		GraphConstants.setBounds(allAttributes, bounds);
		nested.put(cell, basicCell_attributeMap);
		
		jgraph_graph_analysis1.getGraphLayoutCache().edit(nested, null, null, null);
		
		sortJListAnalysis1();
		jTabRM.setSelectedIndex(1);
		openSingleAnalysis1Frame(cell);
		
	}

	public void openSingleMutantFrame(Object cell) {
		if (cell != null && !(cell instanceof DefaultEdge) && !(cell instanceof DefaultPort)) {
			 Mutant m = (Mutant) ((DefaultGraphCell)cell).getUserObject();
			 DefaultGraphCell cell1 = m_jgAdapter.getVertexCell(m);
				AttributeMap savedAttr = new AttributeMap(cell1.getAttributes());
				String nameBefore = m.getName();
			 mutantsDB.accumulateChanges(m);
			 
			 GraphicalProperties.resetFonts(singleMutantFrame);
			 singleMutantFrame.pack();
			  Rectangle screen = getGraphicsConfiguration().getBounds();
			  singleMutantFrame.setLocation(
			        screen.x + (screen.width - singleMutantFrame.getWidth()) / 2,
			        screen.y + (screen.height - singleMutantFrame.getHeight()) / 2 ); 
		
			  singleMutantFrame.setMutantAndShow(m, mutantsDB.collectAncestors(m), mutantsDB.detectConflict(m));
			 mutantsDB.replaceMutant(nameBefore, m.getName());
			 
			 revalidateConflicts(mutantsDB.getChildren(m));
			 cell1 = m_jgAdapter.getVertexCell(m);
				cell1.addPort();
				Map nested = new Hashtable();
				nested.put(cell1, savedAttr);
				jgraph_graph_parameters.getGraphLayoutCache().edit(nested, null, null, null);
				
				sortJListMutants();
				jTabRM.setSelectedIndex(0);
		}
	}
	
	public void openSingleAnalysis1Frame(Object cell) {
		if (cell != null && !(cell instanceof DefaultEdge) && !(cell instanceof DefaultPort)) {
			Simulation s = (Simulation) ((DefaultGraphCell)cell).getUserObject();
			
			DefaultGraphCell cell1 = m_jgAdapter_analysis1.getVertexCell(s);
			AttributeMap savedAttr = new AttributeMap(cell1.getAttributes());
			s.clearCumulativeChanges();
			simDB.accumulateChanges(s);
			 GraphicalProperties.resetFonts(singleAnalysis1Frame);
			 singleAnalysis1Frame.pack();
			  Rectangle screen = getGraphicsConfiguration().getBounds();
			  singleAnalysis1Frame.setLocation(
			        screen.x + (screen.width - singleAnalysis1Frame.getWidth()) / 2,
			        screen.y + (screen.height - singleAnalysis1Frame.getHeight()) / 2 ); 
		
			 Simulation before = new Simulation(s);
			singleAnalysis1Frame.setSimulationAndShow(s, simDB.collectAncestors(s), simDB.detectConflict(s));
			
			simDB.replaceSimulationInExpression(before.getName(), s.getName()); //replace in expression referring to parents
			plotDB.replaceSimulation(before.getName(), s.getName());//replace simulation in plotting nodes
			
			revalidateConflicts(simDB.getChildren_castMutant(s));
			
			cell1 = m_jgAdapter_analysis1.getVertexCell(s);
			cell1.addPort();
			Map nested = new Hashtable();
			nested.put(cell1, savedAttr);
			jgraph_graph_analysis1.getGraphLayoutCache().edit(nested, null, null, null);
			
			sortJListAnalysis1();
			jTabRM.setSelectedIndex(1);
		
			
		}
	}
	
	
	public void openSinglePlotSettings(Object cell) {
		if (cell != null && !(cell instanceof DefaultEdge) && !(cell instanceof DefaultPort)) {
			 RMPlot s = (RMPlot) ((DefaultGraphCell)cell).getUserObject();
			 setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			DefaultGraphCell cell1 = m_jgAdapter_plot.getVertexCell(s);
			AttributeMap savedAttr = new AttributeMap(cell1.getAttributes());
			
			 plotDB.accumulateChanges(s);
			 Set<Simulation> allSims = simDB.getVertexSet();
			 Vector<String> allSPC = multiModel.getAllSpecies_names();
			 Vector<String> allGLQ = multiModel.getAllGlobalQuantities_names();
			 Vector<String> allCMP = multiModel.getAllCompartments_names();
			 
			 GraphicalProperties.resetFonts(changeChartPropertiesFrame);
			 changeChartPropertiesFrame.pack();
			  Rectangle screen = getGraphicsConfiguration().getBounds();
			  changeChartPropertiesFrame.setLocation(
			        screen.x + (screen.width - changeChartPropertiesFrame.getWidth()) / 2,
			        screen.y + (screen.height - changeChartPropertiesFrame.getHeight()) / 2 ); 
			     
			 Vector newSettings = changeChartPropertiesFrame.setVariablesAndShow(s,allSims,allSPC,allGLQ,allCMP);
			 setCursor(null);
				if(newSettings!= null) {
					
					Vector toPlot = (Vector)newSettings.get(0);
					Vector simulations = (Vector)newSettings.get(1);
					Vector chartProperties = (Vector)newSettings.get(2);
					HashMap<PlotChangeType, Object> localValues = (HashMap<PlotChangeType, Object>) newSettings.get(3);
					
					s.clearPlottedVariables();
					s.clearSimulations();
					for(int i = 0; i < toPlot.size(); ++i) {
						s.addPlottedVariable((PlottedVariable) toPlot.get(i));
					}
					
					for(int i = 0; i < simulations.size(); ++i) {
						String simName = (String) simulations.get(i);
						Simulation sim = simDB.getSimulation(simName);
						s.addSimulation(sim.getName());
					}
					
					s.setShowTitle((Boolean)chartProperties.get(0));
					s.setTitleFont((Font)chartProperties.get(1));
					s.setTitleColor((Color)chartProperties.get(2));
					s.setPlotBackground((Color)chartProperties.get(3));
					s.setOrientationVertical((Boolean)chartProperties.get(4));
			
					s.setXaxis((String)chartProperties.get(5));
					s.setLabelXColor((Color)chartProperties.get(6));
					s.setLabelXFont((Font)chartProperties.get(7));
					s.setAutoadjustX((Boolean)chartProperties.get(8));
					s.setMinX((Double)chartProperties.get(9));
					s.setMaxX((Double)chartProperties.get(10));
				
					s.setLabelY((String)chartProperties.get(11));
					s.setLabelYColor((Color)chartProperties.get(12));
					s.setLabelYFont((Font)chartProperties.get(13));
					s.setAutoadjustY((Boolean)chartProperties.get(14));
					s.setMinY((Double)chartProperties.get(15));
					s.setMaxY((Double)chartProperties.get(16));
					
					s.setLogScaleX((Boolean)chartProperties.get(17));
					s.setLogScaleY((Boolean)chartProperties.get(18));
					s.setVariableX((String)chartProperties.get(19));
					
					
					
					s.setAsLocalChanges(localValues);
					
					cell1 = m_jgAdapter_plot.getVertexCell(s);
					cell1.addPort();
					Map nested = new Hashtable();
					nested.put(cell1, savedAttr);
					jgraph_graph_plot.getGraphLayoutCache().edit(nested, null, null, null);
					
					sortJListPlot();
					jTabRM.setSelectedIndex(2);
				}
		}
	}

	// Defines a EdgeHandle that uses the Shift-Button (Instead of the Right
	// Mouse Button, which is Default) to add/remove point to/from an edge.
	class MyEdgeHandle extends EdgeView.EdgeHandle {

		
		public MyEdgeHandle(EdgeView edge, GraphContext ctx) {
			super(edge, ctx);
		}

		// Override Superclass Method
		public boolean isAddPointEvent(MouseEvent event) {
			// Points are Added using shift-Click
			 return event.isShiftDown();
		}

		// Override Superclass Method
		public boolean isRemovePointEvent(MouseEvent event) {
			// Points are Removed using shift-Click
			return event.isShiftDown();
		}

	}
	
	

HashMap<String,
	             HashMap<Object, AttributeMap>> savedView = new HashMap<String,HashMap<Object, AttributeMap>>();
	
public void saveCurrentView(String name, JGraph jgraph_graph) {
		Object[] all = JGraphModelAdapter.getAll(jgraph_graph.getModel());
		HashMap<Object, AttributeMap> currentView =  new HashMap<Object, AttributeMap>();
		for(int i = 0; i < all.length; i++) {
			DefaultGraphCell currentCell = (DefaultGraphCell) all[i];
			if(currentCell!=null && currentCell.getUserObject()!=null) {
				//the userObject is a Mutant for vertex
				AttributeMap savedAttr = new AttributeMap(currentCell.getAttributes());
				currentView.put(currentCell.getUserObject(), savedAttr);
			} else if(currentCell instanceof DefaultEdge){
				currentView.put(currentCell, new AttributeMap(currentCell.getAttributes()));
			}
		}
		savedView.put(name+jgraph_graph.getName(), currentView);
	}

public String getDefaultViewName(int tabIndex) {
	if(tabIndex==0) return defaultViewName+"%Mutants";
	else if(tabIndex==1) return defaultViewName+"%Analysis1";
	else if(tabIndex==2) return defaultViewName+"%Plot";
	else return null;
}
	
public void loadSavedView(String name, JGraph jgraph_graph) {
	if(jgraph_graph==null) return;
	Map nested = new Hashtable();
	Object[] all = JGraphModelAdapter.getAll(jgraph_graph.getModel());	
	HashMap<Object, AttributeMap> view = savedView.get(name+jgraph_graph.getName());
	if(view==null) return;
	for(int i = 0; i < all.length; i++) {
		DefaultGraphCell currentCell = (DefaultGraphCell) all[i];
		if(currentCell!=null && currentCell.getUserObject()!=null) {
			AttributeMap allAttributes = currentCell.getAttributes();
			AttributeMap savedAttr = view.get(currentCell.getUserObject());
			if(savedAttr != null) {
				if(GraphConstants.getBounds(savedAttr)!=null) GraphConstants.setBounds(allAttributes, GraphConstants.getBounds(savedAttr));
				if(GraphConstants.getBackground(savedAttr)!= null) GraphConstants.setBackground(allAttributes, GraphConstants.getBackground(savedAttr));
				if(GraphConstants.getLineColor(savedAttr)!=null) GraphConstants.setLineColor(allAttributes, GraphConstants.getLineColor(savedAttr));
				GraphConstants.setLineStyle(allAttributes, GraphConstants.getLineStyle(savedAttr));
			}
			nested.put(currentCell, allAttributes);
		}
	}
	jgraph_graph.getGraphLayoutCache().edit(nested, null, null, null);
}

public HashMap<String, HashMap<Object, AttributeMap>> getMutantsGraphToSave() {
	HashMap<String, HashMap<Object, AttributeMap>> ret = new HashMap<String, HashMap<Object, AttributeMap>>();
	if(!ExportMultistateFormat.isAutosave) {
		loadSavedView(defaultViewName,jgraph_graph_analysis1);
		loadSavedView(defaultViewName,jgraph_graph_parameters);
		loadSavedView(defaultViewName,jgraph_graph_plot);
	}
	ret.putAll(savedView);
	return savedView;
}

public static String RM_applyRenameAncestorInExpression(MultiModel multiModel, String expression, String from, String replacement) {
	 if(expression == null || expression.length() == 0) return null;
	 String parsedexpression = null;
	 try{
			InputStream is = new ByteArrayInputStream(expression.getBytes("UTF-8"));
			MR_Expression_Parser parser = new MR_Expression_Parser(is,"UTF-8");
			CompleteExpression root = parser.CompleteExpression();
		    EvaluateExpressionVisitor vis = new EvaluateExpressionVisitor(multiModel,false,false);
		    vis.setFromRunManager(true);
		    root.accept(vis);
			
			if(vis.getExceptions().size() == 0) {
				parsedexpression  = vis.getExpression();
				Vector<String> parentsRefs = vis.getParentsReferences();
				for (String parentRef : parentsRefs) {
					if(parentRef.compareTo(from)!= 0) continue;
					String toBeReplaced = MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MUTANT_PARENT_SEPARATOR)+parentRef;
					parsedexpression = parsedexpression.replaceAll(toBeReplaced, MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.MUTANT_PARENT_SEPARATOR)+replacement);
					}
					return parsedexpression;
				}
			 else {
				vis.getExceptions().get(0).printStackTrace();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}



public static enum NotesLabels {
	   FROM_ANCESTOR("@",1), LOCAL("Local change",2),  FROM_BASESET("Base set",3), CONFLICT("!! CONFLICT !!",4);
	   String label = new String();
	   int option = 0;
	   
	   NotesLabels(String s, int opt) {
	              this.label = s;
	              option = opt;
	    }
	   
	   public String getLabel() {
		   return label;
	   }
	   
	   public int getOption(){
		   return option;
	   }
	   
	   public static NotesLabels getTypeFromDescription(String descr){
		   if (descr != null) {
			   if(descr.startsWith(FROM_ANCESTOR.label)) {
		    		  return NotesLabels.FROM_ANCESTOR;
		    	  }
			      for (NotesLabels b : NotesLabels.values()) {
			    	if (descr.compareTo(b.getLabel())==0) {
			          return b;
			      }
			    }
		   } 
		   return null;
	   }
	   
	   
	  }

		
}





