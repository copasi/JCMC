/* Generated by JTB 1.4.7 */
package msmb.parsers.multistateSpecies.syntaxtree;

import msmb.parsers.multistateSpecies.visitor.*;

/**
 * JTB node class for the production MultistateSpecies_Operator_SiteTransferSelector:<br>
 * Corresponding grammar:<br>
 * nodeChoice -> . %0 #0 ( &0 <SUCC><br>
 * .......... .. . .. .. | &1 <PREC> )<br>
 * .......... .. . .. #1 <OPEN_R> #2 MultistateSpecies_Name() #3 "." #4 MultistateSpecies_Operator_SiteName() #5 <CLOSED_R><br>
 * .......... .. | %1 #0 MultistateSpecies_Name() #1 "." #2 MultistateSpecies_Operator_SiteName()<br>
 */
public class MultistateSpecies_Operator_SiteTransferSelector implements INode {

  /** Child node 1 */
  public NodeChoice nodeChoice;

  /** The serial version UID */
  private static final long serialVersionUID = 147L;

  /**
   * Constructs the node with its child node.
   *
   * @param n0 - the child node
   */
  public MultistateSpecies_Operator_SiteTransferSelector(final NodeChoice n0) {
    nodeChoice = n0;
  }

  /**
   * Accepts the IRetArguVisitor visitor.
   *
   * @param <R> the user return type
   * @param <A> the user argument type
   * @param vis - the visitor
   * @param argu - a user chosen argument
   * @return a user chosen return information
   */
  public <R, A> R accept(final IRetArguVisitor<R, A> vis, final A argu) {
    return vis.visit(this, argu);
  }

  /**
   * Accepts the IRetVisitor visitor.
   *
   * @param <R> the user return type
   * @param vis - the visitor
   * @return a user chosen return information
   */
  public <R> R accept(final IRetVisitor<R> vis) {
    return vis.visit(this);
  }

  /**
   * Accepts the IVoidArguVisitor visitor.
   *
   * @param <A> the user argument type
   * @param vis - the visitor
   * @param argu - a user chosen argument
   */
  public <A> void accept(final IVoidArguVisitor<A> vis, final A argu) {
    vis.visit(this, argu);
  }

  /**
   * Accepts the IVoidVisitor visitor.
   *
   * @param vis - the visitor
   */
  public void accept(final IVoidVisitor vis) {
    vis.visit(this);
  }

}