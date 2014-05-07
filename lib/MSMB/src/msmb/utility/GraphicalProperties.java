package msmb.utility;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.jdesktop.swingx.JXList;

import msmb.gui.MainGui;

public class GraphicalProperties {
	public static Color color_border_defaults = Color.MAGENTA;
	
	public static Color color_shading_table = Constants.vt_cream_2;  
	public static Color color_selected_row = Constants.vt_gray_4;
	
	public static Color color_cell_with_errors = Constants.vt_orange;
	public static Color color_cell_with_minorIssues = null;

	public static Color color_cell_to_highlight = Color.YELLOW;
	
	public static Font customFont = new Font(UIManager.getLookAndFeelDefaults().getFont("Label.font").getName(), Font.PLAIN, 12);

	public static void resetFonts(Container container) {
		 
		List<JButton> buttons = SwingUtils.getDescendantsOfClass(JButton.class, container);
		 Iterator<JButton> it = buttons.iterator();
		 while(it.hasNext()) {
			 JButton current = it.next();
			 current.setFont(MainGui.customFont);
		 }
		 
		 List<JTextPane> textPanes = SwingUtils.getDescendantsOfClass(JTextPane.class, container);
		 Iterator<JTextPane> it2 = textPanes.iterator();
		 while(it2.hasNext()) {
			 JTextPane current = it2.next();
			 current.setFont(MainGui.customFont);
		 }
		 
		 List<JLabel> labels = SwingUtils.getDescendantsOfClass(JLabel.class, container);
		 Iterator<JLabel> it3 = labels.iterator();
		 while(it3.hasNext()) {
			 JLabel current = it3.next();
			 current.setFont(MainGui.customFont);
		 }
		 
		 List<JTextField> textFields = SwingUtils.getDescendantsOfClass(JTextField.class, container);
		 Iterator<JTextField> it4 = textFields.iterator();
		 while(it4.hasNext()) {
			 JTextField current = it4.next();
			 current.setFont(MainGui.customFont);
		 }
		
		 
		 
		 List<JCheckBox>  checkBoxes = SwingUtils.getDescendantsOfClass(JCheckBox.class, container);
		 Iterator<JCheckBox> it5 = checkBoxes.iterator();
		 while(it5.hasNext()) {
			 JCheckBox current = it5.next();
			 current.setFont(MainGui.customFont);
		 }
		 
		 List<JRadioButton>  radioButtons = SwingUtils.getDescendantsOfClass(JRadioButton.class, container);
		 Iterator<JRadioButton> it6 = radioButtons.iterator();
		 while(it6.hasNext()) {
			 JRadioButton current = it6.next();
			 current.setFont(MainGui.customFont);
		 }
		 
		
		 List<JSlider>  sliders = SwingUtils.getDescendantsOfClass(JSlider.class, container);
		 Iterator<JSlider> it7 = sliders.iterator();
		 while(it7.hasNext()) {
			 JSlider current = it7.next();
			 current.setFont(MainGui.customFont);
		 }
		 
		 
		 List<JSpinner>  spinners = SwingUtils.getDescendantsOfClass(JSpinner.class, container);
		 Iterator<JSpinner> it8 = spinners.iterator();
		 while(it8.hasNext()) {
			 JSpinner current = it8.next();
			 current.setFont(MainGui.customFont);
		 }
		 
		 List<JTextArea>  textArea = SwingUtils.getDescendantsOfClass(JTextArea.class, container);
		 Iterator<JTextArea> it9 = textArea.iterator();
		 while(it9.hasNext()) {
			 JTextArea current = it9.next();
			 current.setFont(MainGui.customFont);
		 }
		 		 
		 List<JList>  lists = SwingUtils.getDescendantsOfClass(JList.class, container);
		 Iterator<JList> it11 = lists.iterator();
		 while(it11.hasNext()) {
			 JList current = it11.next();
			 current.setFont(MainGui.customFont);
		 }
		 
			List<JComboBox> combos = SwingUtils.getDescendantsOfClass(JComboBox.class, container);
			 Iterator<JComboBox> it12 = combos.iterator();
			 while(it12.hasNext()) {
				 JComboBox current  = it12.next();
				 current.setFont(MainGui.customFont);
			 }
		 
		 
		 
			List<JPanel> panels = SwingUtils.getDescendantsOfClass(JPanel.class, container);
			 Iterator<JPanel> it13 = panels.iterator();
			 while(it13.hasNext()) {
				 JPanel current = it13.next();
				 Border border = current.getBorder();
				 if(border != null && border instanceof TitledBorder) {
					  ((TitledBorder)current.getBorder()).setTitleFont(MainGui.customFont);
				 }
				 resetFonts(current);
			 }
			 
		
			 List<JTree> tree = SwingUtils.getDescendantsOfClass(JTree.class, container);
			 Iterator<JTree> it14 = tree.iterator();
			 while(it14.hasNext()) {
				 JTree current = it14.next();
				 current.setFont(MainGui.customFont);
				 current.setRowHeight(MainGui.customFont.getSize()+3);
			 }
		 
			 
			 List<JTabbedPane> tabpanels = SwingUtils.getDescendantsOfClass(JTabbedPane.class, container);
			 Iterator<JTabbedPane> it15 = tabpanels.iterator();
			 while(it15.hasNext()) {
				 JTabbedPane current = it15.next();
				 current.setFont(customFont);
			 }
			 
			 
			 List<JXList> jxlist = SwingUtils.getDescendantsOfClass(JXList.class, container);
			 Iterator<JXList> it16 = jxlist.iterator();
			 while(it16.hasNext()) {
				 JXList current = it16.next();
				 current.setFont(customFont);
			 }

		// container.revalidate();
		 SwingUtilities.updateComponentTreeUI(container);
		
	}
	
	
	public static void setEnabledFlag(Container container, boolean enabled) {
		 
		List<JButton> buttons = SwingUtils.getDescendantsOfClass(JButton.class, container);
		 Iterator<JButton> it = buttons.iterator();
		 while(it.hasNext()) {
			 JButton current = it.next();
			 current.setEnabled(enabled);
		 }
		 
		 List<JTextPane> textPanes = SwingUtils.getDescendantsOfClass(JTextPane.class, container);
		 Iterator<JTextPane> it2 = textPanes.iterator();
		 while(it2.hasNext()) {
			 JTextPane current = it2.next();
			 current.setEnabled(enabled);
		 }
		 
		 List<JLabel> labels = SwingUtils.getDescendantsOfClass(JLabel.class, container);
		 Iterator<JLabel> it3 = labels.iterator();
		 while(it3.hasNext()) {
			 JLabel current = it3.next();
			 current.setEnabled(enabled);
		 }
		 
		 List<JTextField> textFields = SwingUtils.getDescendantsOfClass(JTextField.class, container);
		 Iterator<JTextField> it4 = textFields.iterator();
		 while(it4.hasNext()) {
			 JTextField current = it4.next();
			 current.setEnabled(enabled);
		 }
		
		 
		 
		 List<JCheckBox>  checkBoxes = SwingUtils.getDescendantsOfClass(JCheckBox.class, container);
		 Iterator<JCheckBox> it5 = checkBoxes.iterator();
		 while(it5.hasNext()) {
			 JCheckBox current = it5.next();
			 current.setEnabled(enabled);
		 }
		 
		 List<JRadioButton>  radioButtons = SwingUtils.getDescendantsOfClass(JRadioButton.class, container);
		 Iterator<JRadioButton> it6 = radioButtons.iterator();
		 while(it6.hasNext()) {
			 JRadioButton current = it6.next();
			 current.setEnabled(enabled);
		 }
		 
		
		 List<JSlider>  sliders = SwingUtils.getDescendantsOfClass(JSlider.class, container);
		 Iterator<JSlider> it7 = sliders.iterator();
		 while(it7.hasNext()) {
			 JSlider current = it7.next();
			 current.setEnabled(enabled);
		 }
		 
		 
		 List<JSpinner>  spinners = SwingUtils.getDescendantsOfClass(JSpinner.class, container);
		 Iterator<JSpinner> it8 = spinners.iterator();
		 while(it8.hasNext()) {
			 JSpinner current = it8.next();
			 current.setEnabled(enabled);
		 }
		 
		 List<JTextArea>  textArea = SwingUtils.getDescendantsOfClass(JTextArea.class, container);
		 Iterator<JTextArea> it9 = textArea.iterator();
		 while(it9.hasNext()) {
			 JTextArea current = it9.next();
			 current.setEnabled(enabled);
		 }
		 		 
		 List<JList>  lists = SwingUtils.getDescendantsOfClass(JList.class, container);
		 Iterator<JList> it11 = lists.iterator();
		 while(it11.hasNext()) {
			 JList current = it11.next();
			 current.setEnabled(enabled);
		 }
		 
			List<JComboBox> combos = SwingUtils.getDescendantsOfClass(JComboBox.class, container);
			 Iterator<JComboBox> it12 = combos.iterator();
			 while(it12.hasNext()) {
				 JComboBox current  = it12.next();
				 current.setEnabled(enabled);
			 }
		 
		 
		 
			List<JPanel> panels = SwingUtils.getDescendantsOfClass(JPanel.class, container);
			 Iterator<JPanel> it13 = panels.iterator();
			 while(it13.hasNext()) {
				 JPanel current = it13.next();
				 Border border = current.getBorder();
				 if(border != null && border instanceof TitledBorder) {
					  ((TitledBorder)current.getBorder()).setTitleFont(MainGui.customFont);
				 }
				 resetFonts(current);
			 }
			 
		
			 List<JTree> tree = SwingUtils.getDescendantsOfClass(JTree.class, container);
			 Iterator<JTree> it14 = tree.iterator();
			 while(it14.hasNext()) {
				 JTree current = it14.next();
				 current.setEnabled(enabled);
			 }
		 
			 
			 List<JTabbedPane> tabpanels = SwingUtils.getDescendantsOfClass(JTabbedPane.class, container);
			 Iterator<JTabbedPane> it15 = tabpanels.iterator();
			 while(it15.hasNext()) {
				 JTabbedPane current = it15.next();
				 current.setEnabled(enabled);
			 }
			 
			 
			 List<JXList> jxlist = SwingUtils.getDescendantsOfClass(JXList.class, container);
			 Iterator<JXList> it16 = jxlist.iterator();
			 while(it16.hasNext()) {
				 JXList current = it16.next();
				 current.setEnabled(enabled);
			 }

		// container.revalidate();
		 SwingUtilities.updateComponentTreeUI(container);
		
	}


}
