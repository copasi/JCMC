/* Generated by JTB 1.4.7 */
package msmb.parsers.chemicalReaction.visitor;

import java.util.ArrayList;
import java.util.Iterator;

import msmb.parsers.chemicalReaction.syntaxtree.*;

/**
 * A skeleton output formatter for your language grammar.<br>
 * Using the add() method along with force(), indent(), and outdent(),<br>
 * you can easily specify how this visitor will format the given syntax tree.<br>
 * See the JTB documentation for more details.
 * <p>
 * Pass your syntax tree to this visitor, and then to the TreeDumper visitor<br>
 * in order to "pretty print" your tree.
 */
public class TreeFormatter extends DepthFirstVoidVisitor {

  /** The list of formatting commands */
  private final ArrayList<FormatCommand> cmdQueue = new ArrayList<FormatCommand>();
  /** True if line to be wrapped, false otherwise */
  private boolean lineWrap;
  /** The wrap width */
  private final int wrapWidth;
  /** The indentation amount */
  private final int indentAmt;
  /** The current line number */
  private int curLine = 1;
  /** The current column number */
  private int curColumn = 1;
  /** The current indentation */
  private int curIndent = 0;
  /** The default indentation */
  private static int INDENT_AMT = 2;

  /**
   * Constructor with a default indentation amount of {@link #INDENT_AMT} and no line-wrap.
   */
  public TreeFormatter() { this(INDENT_AMT, 0); }

  /**
   * Constructor using an indent amount and a line width used to wrap long lines.<br>
   * If a token's beginColumn value is greater than the specified wrapWidth,<br>
   * it will be moved to the next line andindented one extra level.<br>
   * To turn off line-wrapping, specify a wrapWidth of 0.
   *
   * @param aIndentAmt - Amount of spaces per indentation level
   * @param aWrapWidth - Wrap lines longer than wrapWidth. 0 for no wrap
   */
  public TreeFormatter(final int aIndentAmt, final int aWrapWidth) {
    this.indentAmt = aIndentAmt;
    this.wrapWidth = aWrapWidth;

    if (wrapWidth > 0)
       lineWrap = true;
    else
       lineWrap = false;
  }

  /**
   * Accepts a INodeList object.
   *
   * @param n - the node list to process
   */
  protected void processList(final INodeList n) {
    processList(n, null);
  }

  /**
   * Accepts a INodeList object and performs a format command (if non null)<br>
   * between each node in the list (but not after the last node).
   *
   * @param n - the node list to process
   * @param cmd - the format command
   */
  protected void processList(final INodeList n, final FormatCommand cmd) {
    for (final Iterator<INode> e = n.elements(); e.hasNext();) {
       e.next().accept(this);
       if (cmd != null && e.hasNext())
        cmdQueue.add(cmd);
    }
  }

  /**
   * Inserts one line break and indents the next line to the current indentation level.<br>
   * Use "add(force());".
   *
   * @return the corresponding FormatCommand
   */
  protected FormatCommand force() { return force(1); }

  /**
   * Inserts a given number of line breaks and indents the next line to the current indentation level.<br>
   * Use "add(force(i));".
   *
   * @param i - the number of line breaks
   * @return the corresponding FormatCommand
   */
  protected FormatCommand force(final int i) {
    return new FormatCommand(FormatCommand.FORCE, i);
  }

  /**
   * Increases the indentation level by one.<br>
   * Use "add(indent());".
   *
   * @return the corresponding FormatCommand
   */
  protected FormatCommand indent() { return indent(1); }

  /**
   * Increases the indentation level by a given number.<br>
   * Use "add(indent(i));".
   *
   * @param i - the number of indentation levels to add
   * @return the corresponding FormatCommand
   */
  protected FormatCommand indent(final int i) {
    return new FormatCommand(FormatCommand.INDENT, i);
  }

  /**
   * Reduces the indentation level by one.<br>
   * Use "add(outdent());".
   *
   * @return the corresponding FormatCommand
   */
  protected FormatCommand outdent() { return outdent(1); }

  /**
   * Reduces the indentation level by a given number.<br>
   * Use "add(outdent(i));".
   *
   * @param i - the number of indentation levels to substract
   * @return the corresponding FormatCommand
   */
  protected FormatCommand outdent(final int i) {
    return new FormatCommand(FormatCommand.OUTDENT, i);
  }

  /**
   * Adds one space between tokens.<br>
   * Use "add(space());".
   *
   * @return the corresponding FormatCommand
   */
  protected FormatCommand space() { return space(1); }

  /**
   * Adds a given number of spaces between tokens.<br>
   * Use "add(space(i));".
   *
   * @param i - the number of spaces to add
   * @return the corresponding FormatCommand
   */
  protected FormatCommand space(final int i) {
    return new FormatCommand(FormatCommand.SPACE, i);
  }

  /**
   * Use this method to add FormatCommands to the command queue to be executed<br>
   * when the next token in the tree is visited.
   *
   * @param cmd - the FormatCommand to be added
   */
  protected void add(final FormatCommand cmd) {
    cmdQueue.add(cmd);
  }

  /**
   * Executes the commands waiting in the command queue,<br>
   * then inserts the proper location information into the current NodeToken.
   * <p>
   * If there are any special tokens preceding this token,<br>
   * they will be given the current location information.<br>
   * The token will follow on the next line, at the proper indentation level.<br>
   * If this is not the behavior you want from special tokens,<br>
   * feel free to modify this method.
   */
  @Override
  public void visit(final NodeToken n) {
    for (Iterator<FormatCommand> e = cmdQueue.iterator(); e.hasNext();) {
      final FormatCommand cmd = e.next();
      switch (cmd.getCommand()) {
      case FormatCommand.FORCE :
        curLine += cmd.getNumCommands();
        curColumn = curIndent + 1;
        break;
      case FormatCommand.INDENT :
        curIndent += indentAmt * cmd.getNumCommands();
        break;
      case FormatCommand.OUTDENT :
        if (curIndent >= indentAmt)
        curIndent -= indentAmt * cmd.getNumCommands();
        break;
      case FormatCommand.SPACE :
        curColumn += cmd.getNumCommands();
        break;
      default :
        throw new TreeFormatterException("Invalid value in command queue.");
      }
    }

    cmdQueue.removeAll(cmdQueue);

    //
    // Handle all special tokens preceding this NodeToken
    //
    if (n.numSpecials() > 0)
      for (final Iterator<NodeToken> e = n.specialTokens.iterator(); e.hasNext();) {
       NodeToken special = e.next();

       //
       // Place the token
       // Move cursor to next line after the special token
       // Don't update curColumn - want to keep current indent level
       //
       placeToken(special, curLine, curColumn);
       curLine = special.endLine + 1;
      }

    placeToken(n, curLine, curColumn);
    curLine = n.endLine;
    curColumn = n.endColumn;
  }

  /**
   * Inserts token location (beginLine, beginColumn, endLine, endColumn)<br>
   * information into the NodeToken.<br>
   * Takes into account line-wrap. Does not update curLine and curColumn.
   *
   * @param n - the NodeToken to insert
   * @param aLine - the insertion line number
   * @param aColumn - the insertion column number
   */
  private void placeToken(final NodeToken n, final int aLine, final int aColumn) {
    final int length = n.tokenImage.length();
    int line = aLine;
    int column = aColumn;

    //
    // Find beginning of token.  Only line-wrap for single-line tokens
    //
    if (!lineWrap || n.tokenImage.indexOf('\n') != -1 ||
       column + length <= wrapWidth)
       n.beginColumn = column;
    else {
       ++line;
       column = curIndent + indentAmt + 1;
       n.beginColumn = column;
    }

    n.beginLine = line;

    //
    // Find end of token; don't count '\n' if it's the last character
    //
    for (int i = 0; i < length; ++i) {
       if (n.tokenImage.charAt(i) == '\n' && i < length - 1) {
        ++line;
        column = 1;
       }
       else
        ++column;
    }

    n.endLine = line;
    n.endColumn = column;
  }

  //
  // User-generated visitor methods below
  //

  /**
   * reaction -> Reaction()<br>
   * nodeToken -> <EOF><br>
   */
  @Override
  public void visit(final CompleteReaction n) {
    n.reaction.accept(this);
    n.nodeToken.accept(this);
  }

  /**
   * speciesWithCoeff -> SpeciesWithCoeff()<br>
   * nodeToken -> <EOF><br>
   */
  @Override
  public void visit(final CompleteSpeciesWithCoefficient n) {
    n.speciesWithCoeff.accept(this);
    n.nodeToken.accept(this);
  }

  /**
   * nodeChoice -> . %0 #0 ( AdditiveExpression() )?<br>
   * .......... .. . .. #1 ( Blank() )*<br>
   * .......... .. . .. #2 <ARROW><br>
   * .......... .. . .. #3 ( ( $0 ( " " )<br>
   * .......... .. . .. .. . . $1 ( Blank() )*<br>
   * .......... .. . .. .. . . $2 ( AdditiveExpression() )? ) )?<br>
   * .......... .. . .. #4 ( $0 ( Blank() )*<br>
   * .......... .. . .. .. . $1 ";"<br>
   * .......... .. . .. .. . $2 ( Blank() )*<br>
   * .......... .. . .. .. . $3 ListModifiers() )?<br>
   * .......... .. | %1 #0 <ARROW2><br>
   * .......... .. . .. #1 ( Blank() )*<br>
   * .......... .. . .. #2 ( AdditiveExpression() )?<br>
   * .......... .. . .. #3 ( $0 ( Blank() )*<br>
   * .......... .. . .. .. . $1 ";"<br>
   * .......... .. . .. .. . $2 ( Blank() )*<br>
   * .......... .. . .. .. . $3 ListModifiers() )?<br>
   */
  @Override
  public void visit(final Reaction n) {
    n.nodeChoice.accept(this);
  }

  /**
   * speciesWithCoeff -> SpeciesWithCoeff()<br>
   * nodeListOptional -> ( #0 ( Blank() )*<br>
   * ................ .. . #1 " + "<br>
   * ................ .. . #2 ( Blank() )*<br>
   * ................ .. . #3 SpeciesWithCoeff() )*<br>
   */
  @Override
  public void visit(final AdditiveExpression n) {
    n.speciesWithCoeff.accept(this);
    if (n.nodeListOptional.present()) {
      processList(n.nodeListOptional);
    }
  }

  /**
   * nodeOptional -> ( #0 Stoichiometry()<br>
   * ............ .. . #1 ( Blank() )*<br>
   * ............ .. . #2 " * "<br>
   * ............ .. . #3 ( Blank() )* )?<br>
   * species -> Species()<br>
   */
  @Override
  public void visit(final SpeciesWithCoeff n) {
    if (n.nodeOptional.present()) {
      n.nodeOptional.accept(this);
    }
    n.species.accept(this);
  }

  /**
   * nodeToken -> " "<br>
   */
  @Override
  public void visit(final Blank n) {
    n.nodeToken.accept(this);
  }

  /**
   * species -> Species()<br>
   * nodeListOptional -> ( #0 ( Blank() )+<br>
   * ................ .. . #1 Species() )*<br>
   */
  @Override
  public void visit(final ListModifiers n) {
    n.species.accept(this);
    if (n.nodeListOptional.present()) {
      processList(n.nodeListOptional);
    }
  }

  /**
   * nodeToken -> <IDENTIFIER><br>
   * nodeListOptional -> ( <IDENTIFIER> )*<br>
   */
  @Override
  public void visit(final Species n) {
    n.nodeToken.accept(this);
    if (n.nodeListOptional.present()) {
      processList(n.nodeListOptional);
    }
  }

  /**
   * nodeChoice -> . %0 <INTEGER_LITERAL><br>
   * .......... .. | %1 <FLOATING_POINT_LITERAL><br>
   */
  @Override
  public void visit(final Stoichiometry n) {
    n.nodeChoice.accept(this);
  }

}

/**
 * Stores a format command.
 */
class FormatCommand {

  /** Line break format code */
  public static final int FORCE = 0;
  /** Indentation format code */
  public static final int INDENT = 1;
  /** Unindentation format code */
  public static final int OUTDENT = 2;
  /** Spacing format code */
  public static final int SPACE = 3;

  /** The format command code */
  private int command;
  /** The format command repetition number */
  private int numCommands;

  /**
   * Constructor with class members.
   *
   * @param aCmd - the command code
   * @param aNumCmd - the command repetition number
   */
  FormatCommand(final int aCmd, final int aNumCmd) {
    this.command = aCmd;
    this.numCommands = aNumCmd;
  }

  /**
   * @return the command code
   */
  public int getCommand()  { return command; }

  /**
   * @return the command repetition number
   */
  public int getNumCommands()  { return numCommands; }

  /**
   * Sets the command code.
   *
   * @param i - the command code
   */
  public void setCommand(final int i)  { command = i; }

  /**
   * Sets the command repetition number.
   *
   * @param i - the command repetition number
   */
  public void setNumCommands(final int i)  { numCommands = i; }

}

/**
 * The TreeFormatter exception class.
 */
class TreeFormatterException extends RuntimeException {

  /** The serial version UID */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor with no message.
   */
  TreeFormatterException()  { super(); }

  /**
   * Constructor with a given message.
   *
   * @param s - the exception message
   */
  TreeFormatterException(final String s)  { super(s); }

}