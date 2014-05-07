package msmb.commonUtilities;

public class ChangedElement {
	String name = null;
	MSMB_Element type= null;
	
	public ChangedElement(String name, MSMB_Element type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {		return name;	}
	public MSMB_Element getElement() {		return type;	}
}
