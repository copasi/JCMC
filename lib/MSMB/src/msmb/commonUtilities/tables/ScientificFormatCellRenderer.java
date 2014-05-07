package msmb.commonUtilities.tables;

import java.awt.Component;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.JTable;

public class ScientificFormatCellRenderer extends EditableCellRenderer {

	private static final long serialVersionUID = 1L;
	DecimalFormat sci_decimalFormat;
	DecimalFormat basic_decimalFormat;

	public ScientificFormatCellRenderer() {
		Locale currentLocale = new Locale("en", "US");

		DecimalFormatSymbols symbols =   new DecimalFormatSymbols(currentLocale);
		
		sci_decimalFormat = new DecimalFormat("0.###E00",symbols);
		basic_decimalFormat = new DecimalFormat("#0.0###",symbols);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		try{
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			 
					Double parsed = Double.parseDouble(value.toString());
			if ((parsed < 10000.0 && parsed > 0.0001) || parsed == 0.0 ) this.setText(basic_decimalFormat.format(value));
			else this.setText(sci_decimalFormat.format(value));
		} catch(Exception ex) {
			this.setText(value.toString());
		}
		if(customFont!=null)   setFont(customFont);
		return this;

	}
	
	
}
