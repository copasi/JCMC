package  msmb.gui;

import java.awt.Color;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import msmb.utility.Constants;



import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.*;

import msmb.debugTab.DebugConstants;
import msmb.debugTab.DebugMessage;


public class PrintTablesToPDF {

    String modelName = new String("Model name");
    
	
    public String getModelName() {	return modelName;	}
	public void setModelName(String modelName) {this.modelName = modelName;	}



	public void createPdf(File file, Vector<Object> tablesAndLastTabInfo)
        throws IOException, DocumentException {
    	// step 1
        Document document = new Document(PageSize.LETTER.rotate());
        // step 2
        PdfWriter writer  = PdfWriter.getInstance(document, new FileOutputStream(file));
        HeaderFooter event = new HeaderFooter();
        
        
        float side = 20;
        Rectangle area = new Rectangle(side, side, 792-side, 612-side);
        writer.setBoxSize("area", area);
        writer.setPageEvent(event);
        
        // step 3
        document.open();
        // step 4
        Chapter chapter = new Chapter(new Paragraph("Model definition"), 1);
        chapter.add(Chunk.NEWLINE);
     	
        int i = 0;
        for(; i < tablesAndLastTabInfo.size(); i++) {
        	Object element = tablesAndLastTabInfo.get(i);
        	
        	if(element instanceof CustomTableModel_MSMB) {
        		CustomTableModel_MSMB tablemodel = (CustomTableModel_MSMB) element;
        		int col = tablemodel.getColumnCount();
        		
        		Paragraph title = new Paragraph(tablemodel.getTableName());

        		Section section = chapter.addSection(title);
        		section.add(Chunk.NEWLINE);
        	     	
        		section.setBookmarkTitle(title.getContent());
        		section.setIndentation(30);
        		section.setBookmarkOpen(false);
        		section.setNumberStyle(Section.NUMBERSTYLE_DOTTED_WITHOUT_FINAL_DOT);

        		PdfPTable table = new PdfPTable(col);
        		table.setTotalWidth(650);
        	    table.setLockedWidth(true);
        	        
        	    ArrayList<Double> w  = getWidths(tablemodel.getTableName());
        	    
        	   if(w!=null) {
        		   float widths[] = new float[w.size()];
        		   for(int i1 = 0; i1 < w.size(); i1++) { widths[i1] = w.get(i1).floatValue();}
        	       table.setWidths(widths);
        	   }
        	        
        		Font font = new Font(FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        		Phrase ttable = new Phrase(tablemodel.getTableName(), font);

        		PdfPCell cell = new PdfPCell(ttable);
        		cell.setBackgroundColor(BaseColor.BLACK);
        		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        		cell.setColspan(tablemodel.getColumnCount());
        		table.addCell(cell);

        		            	
        		table.getDefaultCell().setBackgroundColor(BaseColor.LIGHT_GRAY);

        		Vector<String> header = null;
        		if(tablemodel.getTableName().compareTo(Constants.TitlesTabs.SPECIES.getDescription())==0) header = new Vector(Constants.species_columns);
        		else if(tablemodel.getTableName().compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription())==0) header = new Vector(Constants.compartments_columns);
        		else if(tablemodel.getTableName().compareTo(Constants.TitlesTabs.EVENTS.getDescription())==0) header = new Vector(Constants.events_columns);
        		else if(tablemodel.getTableName().compareTo(Constants.TitlesTabs.FUNCTIONS.getDescription())==0) header = new Vector(Constants.functions_columns);
        		else if(tablemodel.getTableName().compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())==0) header = new Vector(Constants.globalQ_columns);
        		else if(tablemodel.getTableName().compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0) header = new Vector(Constants.reactions_columns);
        		else if(tablemodel.getTableName().compareTo(Constants.MULTISTATE_TITLE_TABLE_PDF)==0) {
        			header = new Vector<String>();
        			header.add("Single state of Multistate Species");
        			header.add("Initial quantity");
        			
        		}
        		
        		if(header!=null) header.add(0, "#");

        		for (int i1 = 0; i1 < tablemodel.getColumnCount(); i1++) {
        			if(header!=null) 	table.addCell(header.get(i1));
        			else table.addCell("");
        		}
        		table.getDefaultCell().setBackgroundColor(null);
        		table.setHeaderRows(2);
        		            	
        		for(int i1 = 0; i1 < tablemodel.getRowCount()-1; i1++) {
        			for(int j = 0; j < tablemodel.getColumnCount(); j++) {
        				//if(isButtonColumn(tablemodel.getTableName(),j)) continue;
        				
        				
        				
        				String value = tablemodel.getValueAt(i1, j).toString();
        				if(value.length() <=0) value = " ";
        				Chunk ch = new Chunk(value);
        				if(tablemodel.getTableName().compareTo(Constants.TitlesTabs.FUNCTIONS.getDescription())==0 &&
        						j==Constants.FunctionsColumns.NAME.index) {
        					ch.setSplitCharacter(new CommaSplitCharacter() );
        				}
        				Phrase ph = new Phrase(ch);
        				
        				PdfPCell cell1 = new PdfPCell(ph);
        				cell1.setMinimumHeight(22);
        				if(tablemodel.disabledCell.contains(i1+"_"+j)) {cell1.setBackgroundColor(BaseColor.LIGHT_GRAY);	}
        				//cell1.setBackgroundColor(new BaseColor(Color.yellow.getRGB())); 
        				
        				
                				
        				table.addCell(cell1);
        	  			}
        		}
        		
        		            	
        		section.add(table);

        		section.newPage();


        	} else {
        		break;
        	}
        	
        }
        document.add(chapter);
     	
    	Font font = new Font(FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
		
    	Chapter chapter2 = new Chapter(new Paragraph("Model properties"), 2);
    	chapter2.add(Chunk.NEWLINE);
     	
     	Object element = tablesAndLastTabInfo.get(i);
        i++;
     	Chunk p = new Chunk("Unit Volume: "+element.toString(), font);
     	chapter2.add(p);
     	chapter2.add(Chunk.NEWLINE);

     	element = tablesAndLastTabInfo.get(i);
        i++;
     	p = new Chunk("Unit Time: "+element.toString(), font);
     	chapter2.add(p);
     	chapter2.add(Chunk.NEWLINE);

     	element = tablesAndLastTabInfo.get(i);
        i++;
     	p = new Chunk("Unit Quantity: "+element.toString(), font);
     	chapter2.add(p);
     	chapter2.add(Chunk.NEWLINE);
     	chapter2.add(Chunk.NEWLINE);
     	chapter2.add(Chunk.NEWLINE);
     	chapter2.add(Chunk.NEWLINE);
     	chapter2.add(Chunk.NEWLINE);
     	
     	element = tablesAndLastTabInfo.get(i);
        i++;
        String interpretation0 = new String();
        if(Boolean.parseBoolean(element.toString())) interpretation0 = "Concentration";
        else interpretation0 = "Particle Number";
     	p = new Chunk("If no reference is specified, in expressions the "+interpretation0 +" is used for calculations", font);
     	chapter2.add(p);
     	chapter2.add(Chunk.NEWLINE);
     	chapter2.add(Chunk.NEWLINE);
     	chapter2.add(Chunk.NEWLINE);
     	
     	element = tablesAndLastTabInfo.get(i);
        i++;
        String interpretation = new String();
        if(Boolean.parseBoolean(element.toString())) interpretation = "Concentration";
        else interpretation = "Particle Number";
     	p = new Chunk("Quantity column for initial value is interpreted as "+interpretation, font);
     	chapter2.add(p);
     	
		
     	document.add(chapter2);
     	
     	
     	element = tablesAndLastTabInfo.get(i);
     	i++;
     	
     	Chapter chapter3 = new Chapter(new Paragraph("Debug messages"), 2);
     	chapter3.add(Chunk.NEWLINE);
    	if(element instanceof HashMap<?, ?> ) {
    		HashMap<String, DebugMessage>  debugMessages = (HashMap<String, DebugMessage> ) element;
    		printDebugMessages(chapter3,debugMessages);
    	    document.add(chapter3);
    	}
    	
      
      
        
        
        
        // step 5
        document.close();
    }
 

    
  
	private ArrayList getWidths(String tableName) {
		
	    ArrayList<Double> w = null;
	    
	    if(tableName.compareTo(Constants.TitlesTabs.SPECIES.getDescription())==0) {
	    	w = new ArrayList<Double>(Arrays.asList( new Double[Constants.species_columns.size()+1] ));
	    	w.set(0, new Double(0.05F));
	    	w.set(Constants.SpeciesColumns.NAME.index, new Double(0.25F));
	    	w.set(Constants.SpeciesColumns.COMPARTMENT.index, new Double(0.1F));
	    	w.set(Constants.SpeciesColumns.EXPRESSION.index, new Double(0.2F));
	    	w.set(Constants.SpeciesColumns.INITIAL_QUANTITY.index, new Double(0.2F));
	    	w.set(Constants.SpeciesColumns.NOTES.index, new Double(0.1F));
	    	w.set(Constants.SpeciesColumns.TYPE.index, new Double(0.15F));
	    } else if(tableName.compareTo(Constants.TitlesTabs.GLOBALQ.getDescription())==0) {
	    	w = new ArrayList<Double>(Arrays.asList( new Double[Constants.globalQ_columns.size()+1] ));
	    	w.set(0, new Double(0.05F));
	    	w.set(Constants.GlobalQColumns.NAME.index, new Double(0.2F));
	    	w.set(Constants.GlobalQColumns.EXPRESSION.index, new Double(0.3F));
	    	w.set(Constants.GlobalQColumns.VALUE.index, new Double(0.3F));
	    	w.set(Constants.GlobalQColumns.NOTES.index, new Double(0.1F));
	    	w.set(Constants.GlobalQColumns.TYPE.index, new Double(0.15F));
	    } else if(tableName.compareTo(Constants.TitlesTabs.COMPARTMENTS.getDescription())==0) {
	    	w = new ArrayList<Double>(Arrays.asList( new Double[Constants.compartments_columns.size()+1] ));
	    	w.set(0, new Double(0.05F));
	    	w.set(Constants.CompartmentsColumns.NAME.index, new Double(0.2F));
	    	w.set(Constants.CompartmentsColumns.EXPRESSION.index, new Double(0.3F));
	    	w.set(Constants.CompartmentsColumns.INITIAL_SIZE.index, new Double(0.3F));
	    	w.set(Constants.CompartmentsColumns.NOTES.index, new Double(0.1F));
	    	w.set(Constants.CompartmentsColumns.TYPE.index, new Double(0.15F));
	    } else if(tableName.compareTo(Constants.TitlesTabs.REACTIONS.getDescription())==0) {
	    	w = new ArrayList<Double>(Arrays.asList( new Double[Constants.reactions_columns.size()+1] )); 
	    	w.set(0, new Double(0.05F));
	    	w.set(Constants.ReactionsColumns.NAME.index, new Double(0.1F));
	    	w.set(Constants.ReactionsColumns.KINETIC_LAW.index, new Double(0.3F));
	    	w.set(Constants.ReactionsColumns.REACTION.index, new Double(0.3F));
	    	w.set(Constants.ReactionsColumns.EXPANDED.index, new Double(0.0F)); //not printing the columns with the buttons
	    	w.set(Constants.ReactionsColumns.NOTES.index, new Double(0.1F));
	    	w.set(Constants.ReactionsColumns.TYPE.index, new Double(0.15F));
	    } else if(tableName.compareTo(Constants.TitlesTabs.FUNCTIONS.getDescription())==0) {
	    	w = new ArrayList<Double>(Arrays.asList( new Double[Constants.functions_columns.size()+1] )); 
	    	w.set(0, new Double(0.05F));
	    	w.set(Constants.FunctionsColumns.NAME.index, new Double(0.4F));
	    	w.set(Constants.FunctionsColumns.EQUATION.index, new Double(0.4F));
	    	w.set(Constants.FunctionsColumns.PARAMETER_ROLES.index, new Double(0.0F)); //not printing the columns with the buttons
	    	w.set(Constants.FunctionsColumns.NOTES.index, new Double(0.1F));
	    } else if(tableName.compareTo(Constants.TitlesTabs.EVENTS.getDescription())==0) {
	    	w = new ArrayList<Double>(Arrays.asList( new Double[Constants.events_columns.size()+1] )); 
	    	w.set(0, new Double(0.05F));
	    	w.set(Constants.EventsColumns.NAME.index, new Double(0.1F));
	    	w.set(Constants.EventsColumns.ACTIONS.index, new Double(0.35F));
	    	w.set(Constants.EventsColumns.TRIGGER.index, new Double(0.35F)); 
	    	w.set(Constants.EventsColumns.DELAY.index, new Double(0.15F));
	    	w.set(Constants.EventsColumns.DELAYCALC.index, new Double(0.07F));
	    	w.set(Constants.EventsColumns.EXPAND_ACTION_ONVOLUME_TOSPECIES_C.index, new Double(0.07F));
	     	w.set(Constants.EventsColumns.NOTES.index, new Double(0.1F));
	 	    	
	    } else if(tableName.compareTo(Constants.MULTISTATE_TITLE_TABLE_PDF) ==0) {
	    	w = new ArrayList<Double>(Arrays.asList( new Double[3] )); 
	    	w.set(0, new Double(0.05F));
	    	w.set(1, new Double(0.4F));
	    	w.set(2, new Double(0.4F));
	    }
    	
		return w;
	}
	private void printDebugMessages(Chapter chapter, HashMap<String, DebugMessage> debugMessages) {
    		Font font = new Font(FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
		Vector<String> priorities = new Vector<String>();
		priorities.add(DebugConstants.PriorityType.MAJOR.getDescription());
		priorities.add(DebugConstants.PriorityType.PARSING.getDescription());
		priorities.add(DebugConstants.PriorityType.INCONSISTENCIES.getDescription());
		priorities.add(DebugConstants.PriorityType.MISSING.getDescription());
		priorities.add(DebugConstants.PriorityType.EMPTY.getDescription());
		priorities.add(DebugConstants.PriorityType.DEFAULTS.getDescription());
		priorities.add(DebugConstants.PriorityType.MINOR.getDescription());
		priorities.add(DebugConstants.PriorityType.MINOR_IMPORT_ISSUES.getDescription());
		priorities.add(DebugConstants.PriorityType.MINOR_EMPTY.getDescription());
		//priorities.add(DebugConstants.PriorityType.SIMILARITY.getDescription());
			
		for(String priorityDescr : priorities) {
			Paragraph title = new Paragraph("Priority: "+priorityDescr);
	    	Section section = chapter.addSection(title);
	    	section.add(Chunk.NEWLINE);
			Iterator it = MainGui.debugMessages.keySet().iterator();
			Vector<DebugMessage> sorted = new Vector<DebugMessage>();
			while(it.hasNext()) {
				String key = (String) it.next();
				if(key.contains("@"+DebugConstants.PriorityType.getIndex(priorityDescr)+"_")) {
						sorted.add((DebugMessage)MainGui.debugMessages.get(key));
      			} 
			}
			Collections.sort(sorted);
			for (DebugMessage x : sorted) {
				Chunk p = new Chunk(x.getShortDescription(), font);
				section.add(p);
				section.add(Chunk.NEWLINE);
				p = new Chunk(x.getCompleteDescription(), font);
				section.add(p);
				section.add(Chunk.NEWLINE);
			}
    	} 
           
	
	}



	/** Inner class to add a header and a footer. */
    class HeaderFooter extends PdfPageEventHelper {
       
    
        /**
         * Adds the header and the footer.
         * @see com.itextpdf.text.pdf.PdfPageEventHelper#onEndPage(
         *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
         */
        public void onEndPage(PdfWriter writer, Document document) {
           Rectangle rect = writer.getBoxSize("area");
           
           PdfPTable table = new PdfPTable(2);
           try {
             table.setWidths(new int[]{24, 24});
             table.setTotalWidth(rect.getWidth());
             table.setLockedWidth(true);
             table.getDefaultCell().setFixedHeight(20);
             table.getDefaultCell().setBorder(Rectangle.BOTTOM);
             table.addCell(new Phrase(modelName));
             table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
             table.addCell(String.format("pag. %d", writer.getPageNumber()));
             table.writeSelectedRows(0, -1, 20f, 40f,  writer.getDirectContent());
           }
           catch(DocumentException de) {
             throw new ExceptionConverter(de);
           }
           
       /*    ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_CENTER, new Phrase(String.format("pag. %d", writer.getPageNumber())),
                    (rect.getLeft() + rect.getRight()) / 2, rect.getTop(), 0);*/
        
           Date timestamp = new Date();
           
           SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
           
           
          
          ColumnText.showTextAligned(writer.getDirectContent(),
                Element.ALIGN_CENTER,  new Phrase(df.format(timestamp), FontFactory.getFont("Arial", 10)),
               rect.getRight(),  rect.getHeight() / 2, -90);
         
        }
        
    }
 
}


class CommaSplitCharacter implements SplitCharacter {
	 
    /**
     * @see com.itextpdf.text.SplitCharacter#isSplitCharacter(int, int, int, char[],
     *      com.itextpdf.text.pdf.PdfChunk[])
     */
    public boolean isSplitCharacter(int start, int current, int end, char[] cc,
            PdfChunk[] ck) {
        char c;
        if (ck == null)
            c = cc[current];
        else
            c = (char)ck[Math.min(current, ck.length - 1)]
                    .getUnicodeEquivalent(cc[current]);
        return (c == ',');
    }
 
}