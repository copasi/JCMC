package msmb.runManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JLabel;
import java.awt.GridLayout;
import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;

import msmb.runManager.RunManager.NotesLabels;
import msmb.utility.GraphicalProperties;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.util.ShapeUtilities;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Vector;

public class ChangeLinePlotFrame extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private ExitOption exitOption = ExitOption.CANCEL;
	Vector<PlottedVariable> variablesWithCurrentValues = new Vector<PlottedVariable>();
	private JButton button;
	private JLabel lbl1;
	private JLabel lbl2;
	private JSpinner spinner;
	private JLabel lbl3;
	private JComboBox comboBox;
	private JButton btnApplyChanges;
	private JButton btnChangeSelected;
	private JList list;
	private SortedListModel listModel;
	
	
	public static void main(String[] args) {
		try {
			ChangeLinePlotFrame dialog = new ChangeLinePlotFrame();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Vector<PlottedVariable> setVariablesAndShow(Vector<PlottedVariable> varsWithCurrentInterval) {
		try {
			listModel.clear();
			
			for(int i = 0; i < varsWithCurrentInterval.size(); ++i) {
				listModel.add(varsWithCurrentInterval.get(i));
			}
			lbl1.setEnabled(false);
			lbl2.setEnabled(false);
			lbl3.setEnabled(false);
			spinner.setEnabled(false);
			comboBox.setEnabled(false);
			button.setEnabled(false);
			btnApplyChanges.setEnabled(false);
			button.setOpaque(false);
			btnChangeSelected.setEnabled(true);
	
			GraphicalProperties.resetFonts(this);
			pack();
			setLocationRelativeTo(null);
			
			setVisible(true);
		
			//once the window is closed
			if(exitOption != ExitOption.CANCEL) {
				variablesWithCurrentValues.clear();
					for(int i = 0; i < listModel.getSize(); ++i) {
						variablesWithCurrentValues.add((PlottedVariable) listModel.getElementAt(i));
				}
				return variablesWithCurrentValues;
			}
			
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	

	public ChangeLinePlotFrame() {
		setTitle("Change line style");
		setModal(true);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setPreferredSize(new Dimension(100, 200));
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			{
				listModel = new SortedListModel(); 
				list = new JList(listModel);
				 
				scrollPane.setViewportView(list);
			}
		}
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.EAST);
			panel.setLayout(new BorderLayout(0, 0));
			{
				 btnChangeSelected = new JButton("Change selected");
				btnChangeSelected.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						PlottedVariable sel = (PlottedVariable) list.getSelectedValue();
						if(sel!= null) {
							lbl1.setEnabled(true);
							lbl2.setEnabled(true);
							lbl3.setEnabled(true);
							spinner.setEnabled(true);
							comboBox.setEnabled(true);
							button.setEnabled(true);
							button.setOpaque(true);
							btnApplyChanges.setEnabled(true);
							btnChangeSelected.setEnabled(false);
							button.setBackground(sel.getColor());
							spinner.setValue(sel.getLineWidth());
							comboBox.setSelectedIndex(sel.getIndexShape());
						}
						
						
					}
				});
				panel.add(btnChangeSelected, BorderLayout.NORTH);
			}
			{
				JPanel panel_1 = new JPanel();
				panel.add(panel_1, BorderLayout.CENTER);
				panel_1.setLayout(new GridLayout(4, 2, 6, 6));
				{
					JPanel panel_2 = new JPanel();
					panel_1.add(panel_2);
					{
						 lbl1 = new JLabel("  Line color");
						panel_2.add(lbl1);
						lbl1.setVerticalAlignment(SwingConstants.TOP);
					}
				}
				{
					JPanel panel_2 = new JPanel();
					panel_1.add(panel_2);
					{
						button = new JButton();
						button.setPreferredSize(new Dimension(100, 30));
						button.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
									Color initial = null;
									if(button!= null) {
										initial = button.getBackground();
									}
									Color newColor = JColorChooser.showDialog(null, "Color chooser", initial);
									if(newColor!= null) {
										button.setBackground(newColor);
									}
									
								
							}
						});
						panel_2.add(button);
					}
				}
				{
					JPanel panel_2 = new JPanel();
					panel_1.add(panel_2);
					{
						 lbl2 = new JLabel("  Line thickness");
						panel_2.add(lbl2);
						lbl2.setVerticalAlignment(SwingConstants.TOP);
					}
				}
				{
					JPanel panel_2 = new JPanel();
					FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
					flowLayout.setAlignOnBaseline(true);
					panel_1.add(panel_2);
					{
						 Float min = new Float(0.0);
						 Float max = new Float(100.0);
						 Float step = new Float(0.2);
					      final SpinnerNumberModel numberModel = 
					         new SpinnerNumberModel(new Float(1.0), min, max, step);
					     spinner = new JSpinner(numberModel);
						spinner.setPreferredSize(new Dimension(100, 30));
						panel_2.add(spinner);
					}
				}
				{
					JPanel panel_2 = new JPanel();
					panel_1.add(panel_2);
					{
						lbl3 = new JLabel("  Symbol");
						panel_2.add(lbl3);
						lbl3.setVerticalAlignment(SwingConstants.TOP);
					}
				}
				{
					JPanel panel_2 = new JPanel();
					panel_1.add(panel_2);
					{
						 comboBox = new JComboBox();
						 MyShapes[] shapes = MyShapes.values();
						 for(int i = 0; i < shapes.length; ++i) {
							 comboBox.addItem(shapes[i].getLabel());
						 }
						 
						comboBox.setPreferredSize(new Dimension(100, 30));
						panel_2.add(comboBox);
					}
				}
				{
					JPanel panel_2 = new JPanel();
					panel_1.add(panel_2);
				}
				{
					JPanel panel_2 = new JPanel();
					panel_1.add(panel_2);
					{
						btnApplyChanges = new JButton("Apply changes");
						btnApplyChanges.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								int[] sel = list.getSelectedIndices();
								for(int i = 0; i < sel.length; ++i) {
									PlottedVariable current = (PlottedVariable) listModel.getElementAt(sel[i]);
									current.setColor(button.getBackground());
									current.setLineWidth(new Float(spinner.getValue().toString()));
									current.setShape(comboBox.getSelectedIndex());
									listModel.add(current);
								}
								lbl1.setEnabled(false);
								lbl2.setEnabled(false);
								lbl3.setEnabled(false);
								spinner.setEnabled(false);
								comboBox.setEnabled(false);
								button.setEnabled(false);
								btnApplyChanges.setEnabled(false);
								button.setOpaque(false);
								btnChangeSelected.setEnabled(true);
							}
						});
						panel_2.add(btnApplyChanges);
					}
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						exitOption = ExitOption.OK;
						dispose();
					}
				});
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						exitOption = ExitOption.CANCEL;
						dispose();
					}
				});
				buttonPane.add(cancelButton);
			}
		}
		lbl1.setEnabled(false);
		lbl2.setEnabled(false);
		lbl3.setEnabled(false);
		spinner.setEnabled(false);
		comboBox.setEnabled(false);
		button.setEnabled(false);
		btnApplyChanges.setEnabled(false);
		btnChangeSelected.setEnabled(true);
		button.setContentAreaFilled(false);
		button.setOpaque(true);
		button.setBorder(BorderFactory.createLineBorder(Color.black));
	}
	
	public static enum MyShapes {
		   NONE("-None-",0), 
		   SQUARE("Square",1),  
		   CIRCLE("Circle",2), 
		   TRIANGLE("Triangle",3),
		   DIAMOND("Diamond",4),
		   DOWNTRIANGLE("Down triangle",5),
		   SMALLCROSS("Small cross",6),
		   BIGCROSS("Big cross",7);
		   
		 
		   String label = new String();
		   int option = 0;
		   
		   MyShapes(String s, int opt) {
		              this.label = s;
		              option = opt;
		    }
		   
		   public String getLabel() {
			   return label;
		   }
		   
		   public int getIndex(){
			   return option;
		   }
		   
		   public static int getIndexFromDescription(String descr){
			   if (descr != null) {
				      for (MyShapes b : MyShapes.values()) {
				    	if (descr.compareTo(b.getLabel())==0) {
				          return b.getIndex();
				      }
				    }
			   } 
			   return -1;
		   }
		   
		   public static String getDescriptionFromIndex(int index){
			      for (MyShapes b : MyShapes.values()) {
				    	if (index == (b.getIndex())) {
				          return b.getLabel();
				      }
				    }
			   return null;
		   }
		   
		   public static Shape getShapeFromIndex(int index) {
			   int standardSize = 2;
			   switch(index) {
			   		case 0: return null;
			   		case 1: return new Rectangle2D.Double(-standardSize/2,-standardSize/2,standardSize,standardSize);
			   		case 2: return new Ellipse2D.Double(-standardSize/2,-standardSize,standardSize,standardSize);
			   		case 3: return ShapeUtilities.createUpTriangle(standardSize);
			   		case 4: return ShapeUtilities.createDiamond(standardSize);
			   		case 5: return ShapeUtilities.createDownTriangle(standardSize);
			   		case 6: return ShapeUtilities.createDiagonalCross(standardSize, 1);
			   		case 7: return ShapeUtilities.createDiagonalCross(standardSize*2, 1.5f);
			   		default: return null;
			   }
		   }
		
		   
		   
		  }

}


class PlottedVariable implements Serializable{
	String name;
	Color color;
	float lineWidth;
	int indexShape;
	int intervalPlot = 1;
	
	public PlottedVariable(String n, Color c, float lwid, int indxshape) {
		name = new String(n);
		color = c;
		lineWidth = lwid;
		indexShape = indxshape;
	}
	
	public PlottedVariable(String name) {
		this(name, null, 0, 0);
	}

	public void setColor(Color c) { color = c; }
	public void setShape(int selectedIndex) { indexShape = selectedIndex; }
	public void setIntervalPlot(int plotEveryXpoints) { intervalPlot = plotEveryXpoints; }
	public void setLineWidth(Float value) { if(value!=null) lineWidth = value;}
	
	public String getName() {		return name;	}
	public Color getColor() {		return color;	}
	public float getLineWidth() {		return lineWidth;	}
	public int getIndexShape() {		return indexShape;	}
	public int getIntervalPlot() {		return intervalPlot;	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		 if (obj == null)
	            return false;
	        if (obj == this)
	            return true;
	        if (!(obj instanceof PlottedVariable))
	            return false;

	        PlottedVariable  rhs = (PlottedVariable) obj;
	        return new EqualsBuilder().
	            append(name, rhs.name).
	            isEquals();
	}
	
}



		



