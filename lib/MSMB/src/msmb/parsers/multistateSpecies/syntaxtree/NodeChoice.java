/* Generated by JTB 1.4.7 */
package msmb.parsers.multistateSpecies.syntaxtree;

import msmb.parsers.multistateSpecies.visitor.IRetArguVisitor;
import msmb.parsers.multistateSpecies.visitor.IRetVisitor;
import msmb.parsers.multistateSpecies.visitor.IVoidArguVisitor;
import msmb.parsers.multistateSpecies.visitor.IVoidVisitor;

/**
 * Represents a grammar choice (|), e.g. ' ( A | B ) '.<br>
 * The class stores the node and the "which" choice indicator (0, 1, ...).
 */
public class NodeChoice implements INode {

  /** The real node */
  public INode choice;

  /** The "which" choice indicator */
  public int which;

  /** The total number of choices */
  public int total;

  /** The serial version UID */
  private static final long serialVersionUID = 147L;

  /**
   * Constructs the {@link NodeChoice} with a given node and non standard (-1) which choice and total number of choices.
   *
   * @param node - the node
   */
  public NodeChoice(final INode node) {
   this(node, -1, -1);
  }

  /**
   * Constructs the {@link NodeChoice} with a given node, a which choice and a total (not controlled).
   *
   * @param node - the node
   * @param whichChoice - the which choice
   * @param totalChoices - the total number of choices
   */
  public NodeChoice(final INode node, final int whichChoice, final int totalChoices) {
    choice = node;
    which = whichChoice;
    total = totalChoices;
  }

  /**
   * Accepts a {@link IRetArguVisitor} visitor with user Return and Argument data.
   *
   * @param <R> - the user Return type
   * @param <A> - the user Argument type
   * @param vis - the visitor
   * @param argu - the user Argument data
   * @return the user Return data
   */
  public <R, A> R accept(final IRetArguVisitor<R, A> vis, final A argu) {
    return choice.accept(vis, argu);
  }

  /**
   * Accepts a {@link IRetVisitor} visitor with user Return data.
   *
   * @param <R> - the user Return type
   * @param vis - the visitor
   * @return the user Return data
   */
  public <R> R accept(final IRetVisitor<R> vis) {
    return choice.accept(vis);
  }

  /**
   * Accepts a {@link IVoidArguVisitor} visitor with user Argument data.
   *
   * @param <A> - the user Argument type
   * @param vis - the visitor
   * @param argu - the user Argument data
   */
  public <A> void accept(final IVoidArguVisitor<A> vis, final A argu) {
    choice.accept(vis, argu);
  }

  /**
   * Accepts a {@link IVoidVisitor} visitor with no user Return nor Argument data.
   *
   * @param vis - the visitor
   */
  public void accept(final IVoidVisitor vis) {
    choice.accept(vis);
  }

}
