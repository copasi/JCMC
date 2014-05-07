package msmb.commonUtilities;

public class MSMB_InterfaceChange {
	ChangedElement elementBefore = null;
	ChangedElement elementAfter = null;
	 
	public ChangedElement getElementBefore() {	return elementBefore; }
	public ChangedElement getElementAfter() {	return elementAfter;}

	public void setElementBefore(ChangedElement elementBefore) {	this.elementBefore = elementBefore;	}
	public void setElementAfter(ChangedElement elementAfter) {	this.elementAfter = elementAfter;	}
	
	public MSMB_InterfaceChange(MSMB_Element t)     {       type= t;    }
	 private MSMB_Element type;
	 public MSMB_Element getType(){return type;}
	
}

