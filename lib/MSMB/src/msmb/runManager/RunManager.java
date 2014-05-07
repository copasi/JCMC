package msmb.runManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import msmb.model.MultiModel;
import msmb.utility.GraphicalProperties;

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
import com.l2fprod.common.swing.JTaskPane;
import com.l2fprod.common.swing.JTaskPaneGroup;
import javax.swing.JTabbedPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.JList;
import javax.swing.border.BevelBorder;
import javax.swing.JLabel;

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

public class RunManager  extends JDialog {
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
	
	
	
	String MUTANT_BASIC = new String("MutantBasicShape");
	String SIMULATION_BASIC =  new String("SimulationBasicShape");
	
	protected JTaskPane taskPane = new JTaskPane();
	private JCheckBox onSelectedCells;

	private static MutantsDB mutantsDB;
	private SimulationsDB simDB;
	private Hashtable basicCell_attributeMap;
	

	private MultiModel multiModel;
	private SingleSimulationFrame singleAnalysis1Frame;
	private JTabbedPane jTabRM;

	
	public static GraphUndoManager undoManager;
	public static boolean notUndoableAction = false;
	
	
	public RunManager() {
		setTitle("Run Manager");

		UIManager.put("TaskPane.useGradient", Boolean.TRUE);
		UIManager.put("TaskPane.backgroundGradientStart", Color.GRAY);
		UIManager.put("TaskPane.backgroundGradientEnd", Color.gray.darker().darker());
		UIManager.put("TaskPaneGroup.titleBackgroundGradientStart", Color.gray.brighter().brighter());
		UIManager.put("TaskPaneGroup.titleBackgroundGradientEnd", Color.gray.brighter());
		UIManager.put("TaskPaneGroup.titleForeground", Color.BLACK);
		
		basicCell_attributeMap = new Hashtable();
		GraphConstants.setLabelEnabled(basicCell_attributeMap, false);
		GraphConstants.setEditable(basicCell_attributeMap, false);
		GraphConstants.setEndFill(basicCell_attributeMap, false);
		GraphConstants.setLineWidth(basicCell_attributeMap, 2.0f);
		GraphConstants.setRouting(basicCell_attributeMap, GraphConstants.ROUTING_DEFAULT);
		GraphConstants.setBendable(basicCell_attributeMap, true);
		GraphConstants.setResize(basicCell_attributeMap, true);
		GraphConstants.setInset(basicCell_attributeMap, 10);
		GraphConstants.setBackground(basicCell_attributeMap, Color.orange);
		GraphConstants.setForeground(basicCell_attributeMap, Color.white);
		GraphConstants.setLineColor(basicCell_attributeMap, Color.BLUE);
		GraphConstants.setOpaque(basicCell_attributeMap, true);
		GraphConstants.setLineEnd(basicCell_attributeMap, GraphConstants.ARROW_TECHNICAL);
		GraphConstants.setBorder(basicCell_attributeMap, new BevelBorder(BevelBorder.RAISED));
		GraphConstants.setFont(basicCell_attributeMap, new Font("Tahoma", Font.BOLD, 12));
		
		// Configures the taskpane
		JTaskPaneGroup taskGroup = new JTaskPaneGroup();
		taskGroup.setText("Graph layouts");
		taskGroup.add(new AbstractAction("1 - Hierarchical") {
			public void actionPerformed(ActionEvent e) {
				JGraphHierarchicalLayout h = new JGraphHierarchicalLayout();
				h.setOrientation(SwingConstants.SOUTH);
				execute(h);
			}
		});
		taskGroup.add(new AbstractAction("2 - Organic") {
			public void actionPerformed(ActionEvent e) {
				JGraphOrganicLayout layout = new JGraphOrganicLayout();
				layout.setOptimizeBorderLine(false);
				layout.setEdgeLengthCostFactor(0.001); //bigger and edges will be close together
				layout.setEdgeCrossingCostFactor(500); //bigger and if should cross less, but nodes may be spread more resulting in very long edges
				execute(layout);
			}
		});
		taskGroup.add(new AbstractAction("3 - Fast Organic") {
			public void actionPerformed(ActionEvent e) {
				JGraphFastOrganicLayout layout = new JGraphFastOrganicLayout();
				layout.setForceConstant(60);
				execute(layout);
			}
		});
		
		taskGroup.add(new AbstractAction("4 - Circle") {
			public void actionPerformed(ActionEvent e) {
				execute(new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_CIRCLE));
			}
		});
	
		taskGroup.add(new AbstractAction("5 - Tilt current layout") {
			public void actionPerformed(ActionEvent e) {
				execute(new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_TILT));
			}
		});
		taskGroup.add(new AbstractAction("6 - Random") {
			public void actionPerformed(ActionEvent e) {
				execute(new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_RANDOM, 640, 480));
			}
		});
		
		JTaskPaneGroup taskGroup_subLayout = new JTaskPaneGroup();
		taskGroup_subLayout.setText("More layouts...");
		taskGroup_subLayout.setExpanded(false);
		taskGroup.add(taskGroup_subLayout);
		taskGroup_subLayout.add(new AbstractAction("Self-Organizing") {
			public void actionPerformed(ActionEvent e) {
				JGraphSelfOrganizingOrganicLayout layout = new JGraphSelfOrganizingOrganicLayout();
				layout.setStartRadius(400);
				layout.setMinRadius(10000);
				layout.setMaxIterationsMultiple(40);
				layout.setDensityFactor(10000);
				execute(new JGraphSelfOrganizingOrganicLayout());
			}
		});
		
		taskGroup_subLayout.add(new AbstractAction("Spring") {
			public void actionPerformed(ActionEvent e) {
				execute(new JGraphSpringLayout(300));
			}
		});
	
		taskGroup_subLayout.add(new AbstractAction("Tree") {
			public void actionPerformed(ActionEvent e) {
				execute(new JGraphTreeLayout());
			}
		});
		
		taskGroup_subLayout.add(new AbstractAction("Compact Tree") {
			public void actionPerformed(ActionEvent e) {
				JGraphCompactTreeLayout l = new JGraphCompactTreeLayout();
				l.setNodeDistance(10);
				execute(l);
			}
		});
		
		taskGroup_subLayout.add(new AbstractAction("Radial Tree") {
			public void actionPerformed(ActionEvent e) {
				execute(new JGraphRadialTreeLayout());
			}
		});
	
		taskGroup.add(new JSeparator());
		taskGroup.add(new JSeparator());
			taskGroup.add(new AbstractAction("Undo graphical edit") {
			public void actionPerformed(ActionEvent e) {
				try {
					undoManager.undo();
				} catch(Exception ex) {
					//nothing to do... undoable action
				}
			}
		});
		
		taskGroup.add(new AbstractAction("Redo graphical edit") {
			public void actionPerformed(ActionEvent e) {
			try{
				undoManager.redo();
			} catch(Exception ex) {
				//nothing to do... undoable action
			}
			}
		});
		
		 taskPane.add(taskGroup);

		taskGroup = new JTaskPaneGroup();
		taskGroup.setText("View");
		
		taskGroup.add(new AbstractAction("Zoom in") {
			public void actionPerformed(ActionEvent e) {
				jgraph_graph_parameters.setScale(jgraph_graph_parameters.getScale()+0.5);
			}
		});
		
		
		taskGroup.add(new AbstractAction("Zoom out") {
			public void actionPerformed(ActionEvent e) {
				jgraph_graph_parameters.setScale(jgraph_graph_parameters.getScale()-0.5);
			}
		});
		
	
		
	
		taskGroup.add(new AbstractAction("Actual Size") {
			public void actionPerformed(ActionEvent e) {
				jgraph_graph_parameters.setScale(1);
			}
		});
		taskGroup.add(new AbstractAction("Fit Window") {
			public void actionPerformed(ActionEvent e) {
				JGraphLayoutMorphingManager.fitViewport(jgraph_graph_parameters);
			}
		});
		
		taskPane.add(taskGroup);

		
		taskGroup = new JTaskPaneGroup();
		taskGroup.setText("Save/Load");
		taskGroup.add(new AbstractAction("Save to image") {
			public void actionPerformed(ActionEvent e) {
					Color bg = null; // Use this to make the background transparent
					bg = jgraph_graph_parameters.getBackground(); // Use this to use the graph backgroundcolor
					BufferedImage img = jgraph_graph_parameters.getImage(bg, 10);
					
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
				saveCurrentView(name,jgraph_graph_parameters);
			
			}
		});
		taskPane.add(taskGroup);
		
		taskGroup.add(new AbstractAction("Load view") {
			public void actionPerformed(ActionEvent e) {
				Set<String> allNames = savedView.keySet();
				Vector sorted = new Vector<String>();
				sorted.addAll(allNames);
				sorted.remove(defaultViewName);
				Collections.sort(sorted);
				sorted.add(0, defaultViewName);
				
				String name=(String) JOptionPane.showInputDialog(
						null,
						"Select the view to load:",
						"Load view",
						JOptionPane.QUESTION_MESSAGE,
						null,
						sorted.toArray(),
						defaultViewName);
				if(name == null || name.trim().length() == 0) {
					return;
				}
				
				loadSavedView(name,jgraph_graph_parameters);
			}
		});
		taskPane.add(taskGroup);
		
		taskGroup = new JTaskPaneGroup();
		taskGroup.setText("Edit");
		onSelectedCells = new  JCheckBox("Apply only on selected cells");
		onSelectedCells.setOpaque(false);
		taskGroup.add(onSelectedCells);
		
		
		taskGroup.add(new AbstractAction("Node color (background)") {
			public void actionPerformed(ActionEvent e) {
				applyColor(0);
			}
		});
		
		taskGroup.add(new AbstractAction("Edge color") {
			public void actionPerformed(ActionEvent e) {
				applyColor(2);
			}
		});
		
	
		taskPane.add(taskGroup);
		
		
	/*	taskGroup = new JTaskPaneGroup();
		taskGroup.setText("Options");*/

		
		initializeFrame();

		setSize(700, 539);
	}
	
	protected void applyColor(int which) {
		//0 background
		//1 foreground
		//2 line
		Map nested = new Hashtable();
		Map attributeMap1 = new Hashtable();
	
			Color newColor = JColorChooser.showDialog(rootPane, "Color Chooser", GraphConstants.getBackground(basicCell_attributeMap));
			if(newColor!= null) {
				if(which == 0) GraphConstants.setBackground(attributeMap1, newColor);
				else if (which == 1) GraphConstants.setForeground(attributeMap1, newColor);
				else if(which == 2)	GraphConstants.setLineColor(attributeMap1, newColor);
			}
			
			Object[] all = null;
			if(onSelectedCells.isSelected()) {
				 all = jgraph_graph_parameters.getSelectionCells();
			} else {
				all = JGraphModelAdapter.getAll(jgraph_graph_parameters.getModel());	
			}
			for(int i = 0; i < all.length; i++) {
				nested.put(all[i], attributeMap1);
			}
		jgraph_graph_parameters.getGraphLayoutCache().edit(nested, null, null, null);
		
	}
 
	
	public MutantsDB getMutantsDB() {
		if(mutantsDB == null) {
			mutantsDB = new MutantsDB(multiModel);
		}
		return mutantsDB;
	}
	

	public void initializeMutantsGraph_fromSavedMSMB(HashMap<String,HashMap<Object, AttributeMap>> savedView_toApply) {
		mutantsDB = new MutantsDB(multiModel);
		mutantsDB.initializeJGraph(savedView_toApply.get(defaultViewName));
		initializeMutantsGraph();
		savedView= new HashMap<String, HashMap<Object,AttributeMap>>();
		savedView.putAll(savedView_toApply);
		loadSavedView(defaultViewName,jgraph_graph_parameters);
		simDB = new SimulationsDB(multiModel);
	}
	
	
	public void initializeAndShow(MultiModel multiModel) {
		GraphicalProperties.resetFonts(this);
		
		mutantsDB = getMutantsDB();
		this.multiModel = multiModel;
		mutantsDB.setMultiModel(this.multiModel);
		initializeMutantsGraph();
		
		
		simDB = new SimulationsDB(multiModel);
	
		initializeAnalysis1Graph();
		initializeGraphsScrollPanels();
		
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
	
		
		undoManager = new GraphUndoManager();
		jgraph_graph_parameters.getModel().addUndoableEditListener(undoManager);
		singleMutantFrame = new SingleMutantFrame(multiModel);
		singleAnalysis1Frame = new SingleSimulationFrame(simDB, multiModel);
		pack();
		setLocationRelativeTo(null);
		setModal(true);
		
		
		loadSavedView(defaultViewName,jgraph_graph_parameters);
		loadSavedView(defaultViewName,jgraph_graph_analysis1);

		setVisible(true);
		saveCurrentView(defaultViewName, jgraph_graph_parameters);
		saveCurrentView(defaultViewName, jgraph_graph_analysis1);
	}
	
	
	
	private void initializeGraphsScrollPanels() {
		JScrollPane graphScrollPane = new JScrollPane();
		graphScrollPane.setViewportView(jgraph_graph_parameters);
		splitPaneGraph_parameters.setLeftComponent(graphScrollPane);
		
		graphScrollPane = new JScrollPane();
		graphScrollPane.setViewportView(jgraph_graph_analysis1);
		splitPaneGraph_analysis1.setLeftComponent(graphScrollPane);
	}

	private void initializeMutantsGraph() {
		
		sortJListMutants();
		
	    m_jgAdapter = new JGraphModelAdapter( mutantsDB.getJgraphT() );
     	jgraph_graph_parameters = new JGraph(m_jgAdapter );
	
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
	
		remove_analysis1 = new AbstractAction("", null) {
			public void actionPerformed(ActionEvent e) {
				if (!jgraph_graph_analysis1.isSelectionEmpty()) {
					/*Object[] cells = jgraph_graph_parameters.getSelectionCells();
					
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
					}*/
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

	private void initializeFrame() {
		JMenuBar jMenuBar = new JMenuBar();
		JMenu testMenu = new JMenu("Edit");
		
		JMenuItem menuItem1 = new JMenuItem("Export to single cps");
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
		testMenu.add(menuItem1);
		
		JMenuItem menuItem2 = new JMenuItem("Run simulations");
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
		testMenu.add(menuItem2);
		
		jMenuBar.add(testMenu);		
		this.setJMenuBar(jMenuBar);
	
		jTabRM = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(jTabRM, BorderLayout.CENTER);
		
		jpanelParameterLists = new JPanel();

		jTabRM.addTab("Parameter lists", null, jpanelParameterLists, null);
		populateJPanelParameterLists();
		
		jPanelAnalysis1 = new JPanel();
		jTabRM.addTab("Time course", null, jPanelAnalysis1, null);
		jPanelAnalysis1.setLayout(new BorderLayout(0, 0));
		jTabRM.setEnabledAt(1, true);
		populateJPanelAnalysis(jPanelAnalysis1);
		
		JPanel panel_5 = new JPanel();
		jTabRM.addTab("Plotting", null, panel_5, null);
		jTabRM.setEnabledAt(2, false);
	}

	private void populateJPanelAnalysis(JPanel jPanelAnalysis) {
		JScrollPane taskPaneScrollPane = new JScrollPane(new JTextArea());
		taskPaneScrollPane.setPreferredSize(new Dimension(300,600));
		
		splitPaneGraph_analysis1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPaneGraph_analysis1.setDividerLocation(350);
		splitPaneGraph_analysis1.setResizeWeight(0.5);
		splitPaneGraph_analysis1.setRightComponent(taskPaneScrollPane);
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
			
	    	
	    	JPanel panel_right= new JPanel();
	    	panel_right.setLayout(new BorderLayout(3, 3));
	    	panel_right.add(splitPaneGraph_analysis1, BorderLayout.CENTER);
			JLabel keyForEdges = new JLabel(" Hold SHIFT to add/delete control points on edges");
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
					
				/*	while(mutantsDB.isNameDuplicate(name)) {
						JOptionPane.showMessageDialog(null, "The name already exists.\nProvide a different name.", "Error!", JOptionPane.ERROR_MESSAGE);
						name=JOptionPane.showInputDialog("Provide a name for the new setting:", name);
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
					
					jgraph_graph.getGraphLayoutCache().edit(nested, null, null, null);
					sortJListMutants();*/
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
					
					while(mutantsDB.isNameDuplicate(name)) {
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
		JScrollPane taskPaneScrollPane = new JScrollPane(taskPane);
		taskPaneScrollPane.setPreferredSize(new Dimension(300,600));
	
		
	   splitPaneGraph_parameters = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPaneGraph_parameters.setDividerLocation(350);
		splitPaneGraph_parameters.setResizeWeight(0.5);
		splitPaneGraph_parameters.setRightComponent(taskPaneScrollPane);
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
	public void execute(final JGraphLayout layout) {
		if (jgraph_graph_parameters != null && jgraph_graph_parameters.isEnabled() && jgraph_graph_parameters.isMoveable()
				&& layout != null) {
			final JGraphFacade facade = createFacade(jgraph_graph_parameters);
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
										morpher.morph(jgraph_graph_parameters, map);
										jgraph_graph_parameters.requestFocus();
									}
								}
							});
						} catch (Exception e) {
							e.printStackTrace();
							JOptionPane
									.showMessageDialog(jgraph_graph_parameters, e.getMessage());
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
			panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
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
				} else {
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
						
					} else {
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
			//System.out.println("Testing2 the existence of a connection: before");
			//System.out.println(mutantsDB.toString());
			System.out.println("CHANGE CONNECTION IN SIMULATIONDB FOR ANALYSIS TAB");
			mutantsDB.addConnection((Mutant)((DefaultGraphCell)source).getUserObject(), (Mutant)((DefaultGraphCell)target).getUserObject());
				
			//System.out.println("Testing2 the existence of a connection: after");
			//System.out.println(mutantsDB.toString());
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
					System.out.println("CHANGE CONNECTION IN SIMULATIONDB FOR ANALYSIS TAB");

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

	//
	//
	//

	
	//
	// PopupMenu
	//
	public JPopupMenu createPopupMenu_parameters(final Point pt, final Object cell) {
		JPopupMenu menu = new JPopupMenu();
		if (cell != null && !(cell instanceof DefaultEdge)) {
			// Edit
			menu.add(new AbstractAction("Edit") {
				public void actionPerformed(ActionEvent e) {
					openSingleMutantFrame(cell);
				}
			});
		
		// Remove
		if (!jgraph_graph_parameters.isSelectionEmpty()) {
			menu.add(new AbstractAction("Remove") {
				public void actionPerformed(ActionEvent e) {
					remove.actionPerformed(e);
				}
			});
			menu.addSeparator();
			menu.add(new AbstractAction("Add analysis") {
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
			}
		}
		
	
		
		return menu;
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
		System.out.println("simChanges: \n"+ simDB.printSimulationsChanges());
		
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
			 mutantsDB.accumulateChanges(m);
			 singleMutantFrame.setMutantAndShow(m, mutantsDB.collectAncestors(m), mutantsDB.detectConflict(m));
			 revalidateConflicts(mutantsDB.getChildren(m));
		}
	}
	
	public void openSingleAnalysis1Frame(Object cell) {
		if (cell != null && !(cell instanceof DefaultEdge) && !(cell instanceof DefaultPort)) {
			 Simulation s = (Simulation) ((DefaultGraphCell)cell).getUserObject();
			 simDB.accumulateChanges(s);
			 singleAnalysis1Frame.setSimulationAndShow(s, simDB.collectAncestors(s), simDB.detectConflict(s));
			 System.out.println("changes after: " +s.getChanges());
			 //revalidateConflicts(simDB.getChildren(s));
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
		//System.out.println("graph to save");
		//System.out.println(mutantsDB.toString());
		Object[] all = JGraphModelAdapter.getAll(jgraph_graph.getModel());
		HashMap<Object, AttributeMap> currentView =  new HashMap<Object, AttributeMap>();
		for(int i = 0; i < all.length; i++) {
			DefaultGraphCell currentCell = (DefaultGraphCell) all[i];
			if(currentCell!=null && currentCell.getUserObject()!=null) {
				//the userObject is a Mutant for vertex
				currentView.put(currentCell.getUserObject(), new AttributeMap(currentCell.getAttributes()));
			} else if(currentCell instanceof DefaultEdge){
				currentView.put(currentCell, new AttributeMap(currentCell.getAttributes()));
			}
		}
		savedView.put(name, currentView);
		
	}
	
public void loadSavedView(String name, JGraph jgraph_graph) {
	if(jgraph_graph==null) return;
	Map nested = new Hashtable();
	Object[] all = JGraphModelAdapter.getAll(jgraph_graph.getModel());	
	HashMap<Object, AttributeMap> view = savedView.get(name);
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
	loadSavedView(defaultViewName,jgraph_graph_analysis1);
	loadSavedView(defaultViewName,jgraph_graph_parameters);
	ret.putAll(savedView);
	return savedView;
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





