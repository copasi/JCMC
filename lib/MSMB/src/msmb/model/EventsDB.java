package msmb.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

import msmb.parsers.mathExpression.MR_Expression_Parser;
import msmb.parsers.mathExpression.syntaxtree.CompleteExpression;
import msmb.parsers.mathExpression.visitor.Look4UndefinedMisusedVisitor;
import msmb.utility.CellParsers;
import msmb.utility.Constants;
import msmb.utility.MySyntaxException;

public class EventsDB {
	TreeMap<Integer, Event> eventVector = new TreeMap<Integer, Event>();
	HashMap<String, Integer> eventIndexes = new HashMap<String, Integer>();
	private MultiModel multiModel = null;
	
	
	public EventsDB(MultiModel m) {
		eventVector = new TreeMap<Integer, Event>();
		eventIndexes = new HashMap<String, Integer>();
		eventVector.put(0,null);//rows starts at index 1
		multiModel = m;
	}
	
	public boolean addChangeEvent(int row, String name, String trigger, String actions, String delay, boolean delayAfterCalculation, String notes, int expandActionVolume) throws Throwable {	
		
		Event event = new Event(name);
		event.setNotes(notes);
		event.setDelay(delay);
		event.setDelayAfterCalculation(delayAfterCalculation);
		event.setExpandActionVolume(expandActionVolume);
		eventVector.put(row, event); //take the place even if expressions contains error
		eventIndexes.put(event.getName(), eventVector.size());
		event.setTrigger(multiModel , trigger);
		event.setActions(multiModel , actions);
			
		
		eventVector.put(row, event);
		
		
		return true;
	}
	
	public Collection<Event> getAllEvents() {
		return this.eventVector.values();
	}
	
	public boolean removeEvent(int toBeRemoved) {
		int size = eventVector.keySet().size();
		//globalQIndexes.remove(globalQVector.get(toBeRemoved+1).getName());
		for(int i = toBeRemoved+1; i < size; i++) {
			Event succ = eventVector.get(i+1);
			if(succ==null) {
				eventVector.remove(eventVector.size()-1);
				break; 
			}
			eventVector.put(i, succ);
			eventIndexes.put(succ.getName(), i);
			
		}
		return true;
}
	
	public void clear() {
		eventVector.clear();
		eventIndexes.clear();
	}
	
}
