package msmb.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import  msmb.parsers.mathExpression.MR_Expression_ParserConstantsNOQUOTES;
 
public class ReversePolishNotation {
	
	public static boolean FULL_BRACKETS = true;
    // Associativity constants for operators
    private static final int LEFT_ASSOC = 0;
    private static final int RIGHT_ASSOC = 1;
    HashMap<String,Integer> functions = new HashMap<String,Integer>();
    
    public ReversePolishNotation(HashMap<String, Integer> functions_nameNumArguments) {
    	functions.clear();
    	functions.putAll(functions_nameNumArguments);
	}


	public void setFunctions( HashMap<String,Integer> fun) {
    	
    }
    
    // Supported operators
    private static final Map<String, int[]> OPERATORS = new HashMap<String, int[]>();
    static {
        // Map<"token", []{precendence, associativity,number of arguments}>
    	OPERATORS.put("=", new int[] { 0, LEFT_ASSOC, 2 });
    	OPERATORS.put("xor", new int[] { 1, LEFT_ASSOC, 2 });
    	OPERATORS.put("||", new int[] { 1, LEFT_ASSOC, 2 });
    	OPERATORS.put("&&", new int[] { 2, LEFT_ASSOC, 2 });
    	OPERATORS.put("==", new int[] { 3, LEFT_ASSOC, 2 });
    	OPERATORS.put("!=", new int[] { 3, LEFT_ASSOC, 2 });
    	OPERATORS.put("<=", new int[] { 3, LEFT_ASSOC, 2 });
    	OPERATORS.put("<", new int[] { 3, LEFT_ASSOC, 2 });
    	OPERATORS.put(">", new int[] { 3, LEFT_ASSOC, 2 });
    	OPERATORS.put(">=", new int[] { 3, LEFT_ASSOC, 2 });
    	OPERATORS.put("+", new int[] { 4, LEFT_ASSOC, 2 });
        OPERATORS.put("-", new int[] { 4, LEFT_ASSOC, 2 });
        OPERATORS.put("*", new int[] { 5, LEFT_ASSOC, 2 });
        OPERATORS.put("/", new int[] { 5, LEFT_ASSOC, 2 });
        OPERATORS.put("%", new int[] { 6, LEFT_ASSOC, 2 });
        OPERATORS.put("^", new int[] { 7, RIGHT_ASSOC, 2 });
        OPERATORS.put("not", new int[] { 8, RIGHT_ASSOC, 1 });
        OPERATORS.put("*unary*minus", new int[] { 8, RIGHT_ASSOC, 1 });
        OPERATORS.put("*unary*plus", new int[] { 8, RIGHT_ASSOC, 1 });
         
      }
    private static final int SINGLE_VAR_NUM_PRECEDENCE = 100;
    
    /**
     * Test if a certain is an operator .
     * @param token The token to be tested .
     * @return True if token is an operator . Otherwise False .
     */
    private static boolean isOperator(String token) {
        return OPERATORS.containsKey(token);
    }
    
    
    private boolean isFunction(String token) { return functions.containsKey(token); }
    private int getFunctionNumArgs(String token) { return functions.get(token); }
	
    /**
     * Test the associativity of a certain operator token .
     * @param token The token to be tested (needs to operator).
     * @param type LEFT_ASSOC or RIGHT_ASSOC
     * @return True if the tokenType equals the input parameter type .
     */
    private static boolean isAssociative(String token, int type) {
        if (!isOperator(token)) {
            throw new IllegalArgumentException("Invalid token: " + token);
        }
        if (OPERATORS.get(token)[1] == type) {
            return true;
        }
        return false;
    }
 
    /**
     * Compare precendence of two operators.
     * @param token1 The first operator .
     * @param token2 The second operator .
     * @return A negative number if token1 has a smaller precedence than token2,
     * 0 if the precendences of the two tokens are equal, a positive number
     * otherwise.
     */
    private static final int cmpPrecedence(String token1, String token2) {
        if (!isOperator(token1) || !isOperator(token2)) {
            throw new IllegalArgumentException("Invalied tokens: " + token1
                    + " " + token2);
        }
        return OPERATORS.get(token1)[0] - OPERATORS.get(token2)[0];
    }
 
    public String[] infixToRPN(Object[] inputTokens) {
        ArrayList<String> out = new ArrayList<String>();
        Stack<String> stack = new Stack<String>();
        // For all the input tokens [S1] read the next token [S2]
        for (Object t : inputTokens) {
        	String token = t.toString();
        	if(token.trim().length()==0) continue;
        	  if (isFunction(token)) { //If the token is a function token, then push it onto the stack.
        		  stack.push(token);
        	  }
        	  else if(token.compareTo(",")==0) { //If the token is a function argument separator (e.g., a comma):
        		  while (!stack.empty() && !stack.peek().equals("(")) {
                      out.add(stack.pop()); //Until the token at the top of the stack is a left parenthesis, pop operators off the stack onto the output queue
                  }
               }
        	  else if (isOperator(token)) { //If the token is an operator, o1, then
                
                while (!stack.empty() && isOperator(stack.peek())) { //while there is an operator token, o2, at the top of the stack
                    if ((isAssociative(token, LEFT_ASSOC) && cmpPrecedence(token, stack.peek()) <= 0) //either o1 is left-associative and its precedence is less than or equal to that of o2
                            || (isAssociative(token, RIGHT_ASSOC) && cmpPrecedence(token, stack.peek()) < 0)) { //or o1 has precedence less than that of o2,
                        out.add(stack.pop());   //pop o2 off the stack, onto the output queue;
                        continue;
                    }
                    break;
                }
                stack.push(token); //push o1 onto the stack
            } else if (token.equals("(")) {
                stack.push(token);  // If the token is a left parenthesis, then push it onto the stack.
            } else if (token.equals(")")) { //If the token is a right parenthesis
                    while (!stack.empty() && !stack.peek().equals("(")) { //Until the token at the top of the stack is a left parenthesis, pop operators off the stack onto the output queue.
                    out.add(stack.pop()); 
                }
                stack.pop(); // Pop the left parenthesis from the stack, but not onto the output queue.
                if(!stack.empty() && isFunction(stack.peek())) { //If the token at the top of the stack is a function token, pop it onto the output queue.
                	 out.add(stack.pop()); 
                }
            } else {
                out.add(token); //If the token is a number, then add it to the output queue
            }
        }
        while (!stack.empty()) { // When there are no more tokens to read
            out.add(stack.pop()); // While there are still operator tokens in the stack
        }
        String[] output = new String[out.size()];
        return out.toArray(output);
    }
 
    public static void main(String[] args) {
    	HashMap<String, Integer> functions = new HashMap<String, Integer>();
    	functions.put("function",3);
    	functions.put("test",2);
    	functions.put("sin",1);
    	functions.put("exp",1);
        functions.put("if",3);
    	ReversePolishNotation rpn = new ReversePolishNotation(functions);
   
    	String original = "V / ( 2 * TV )";//"a <= b && c < d";//"Day_in_hours - Time <= 12 && Day_in_hours - Time < 0";//"if ( Time < 30 , 0 , 0.075 * ( Time - 30 ) )"; //"1.11 * sin ( 2 * 3.1416 / 800 * ( Time - 200 ) ) + 1.11";//"2 * sin ( 1 ) + 3";//"a && b && c || d && f";//"a + b * c >= 3 + 2 * 1 && ";//" function ( f - b * c + d , ! e , test ( ( b * s ) / 2 , 4 + 2 + ( 3 ) ) ) ";//"function ( A + ( B + ( ! e + 2 * 4 ) ) , 2 )";
        String[] input = original.split(" ");
    	System.out.println("Original: ");
    	System.out.println(original);
    	 String[] output = rpn.infixToRPN(input);
        System.out.println("RPN:");
        for (String token : output) {
            System.out.print(token + " ");
        }
        System.out.println("");
        System.out.println("Infix:");
        
        System.out.println(rpn.RPNtoInfix(output));
        FULL_BRACKETS = false;
        System.out.println(rpn.RPNtoInfix(output));
        
    	String evaluation = " ( 1 + 2 ) * 0.5  + 3";
        String[] input2 = evaluation.split(" ");
    	System.out.println("evaluation: ");
    	System.out.println(evaluation);
    	 String[] output2 = rpn.infixToRPN(input2);
    	  System.out.println("RPN:");
     	  for (String token : output2) {
              System.out.print(token + " ");
          }
          System.out.println("");
          System.out.println("Evaluated:");
         try{
			double r =rpn.evaluateRPN(output2);
			 System.out.println("Result: " + r); // print result
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
    }
    
	public String RPNtoInfix(String[] inputTokens) {
	    Stack<String> stack = new Stack<String>();
	    Stack<Integer> stackPrecedence = new Stack<Integer>();
        
	    for (String token : inputTokens) {
	    	  if (isFunction(token)) {
	    		  int n = getFunctionNumArgs(token);
	    		  String arguments = new String();
	    		  for(int i = 0; i < n; i++) {
	    		 	 String element = stack.pop();
	    			 if(FULL_BRACKETS) element = "("+element+")";
	    		 	 stackPrecedence.pop();
	    		 	arguments = ","+element+arguments;
	              }
	    		  if(n>0) arguments = arguments.substring(1);
	             stack.push(token + "("+arguments+")");
	           stackPrecedence.push(SINGLE_VAR_NUM_PRECEDENCE);
	    	  } 
	    	  else if (isOperator(token) && OPERATORS.get(token)[2]==2) {
	              String right = stack.pop();
	              Integer rightPrec = stackPrecedence.pop();
	              String left = stack.pop();
	              Integer leftPrec = stackPrecedence.pop();
	              int operatorPrec = OPERATORS.get(token)[0];
	             if(isAssociative(token, RIGHT_ASSOC)) {
	            	 if(rightPrec < operatorPrec || FULL_BRACKETS) { right = "(" + right + ")";   	 }
	            	 if(leftPrec <= operatorPrec || FULL_BRACKETS)  { left = "(" + left + ")";     	 }
	            	 stack.push(left + token + right);
	            	 stackPrecedence.push(operatorPrec);
	             } else if(isAssociative(token, LEFT_ASSOC)) {
	            	 if(rightPrec <= operatorPrec || (FULL_BRACKETS 
	            			// && token.compareTo("&&")!=0 && token.compareTo("||")!=0
	            			 ) ) { right = "(" + right + ")";   	 } 
	            	 		// don't add the brackets around terms of a logical series because otherwise I have problems in the parser
	            	 if(leftPrec < operatorPrec || (FULL_BRACKETS
	            			 //&& token.compareTo("&&")!=0&& token.compareTo("||")!=0
	            			 ) )  { left = "(" + left + ")";     	 }
	          		 if(token.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.XOR))==0
	          				 ||
	          				token.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.BANG))==0 ) token = " " + token + " ";
	            	
	          		 stack.push(left + token + right);
		             stackPrecedence.push(operatorPrec);
	             }
            } else  if (isOperator(token) && OPERATORS.get(token)[2]==1) {
	              String right = stack.pop();
	              Integer rightPrec = stackPrecedence.pop();
	              int operatorPrec = OPERATORS.get(token)[0];
	             if(isAssociative(token, RIGHT_ASSOC)) {
	            	 if(rightPrec < operatorPrec || FULL_BRACKETS) { right = "(" + right + ")";   	 }
	            	 if(token.compareTo("*unary*minus")==0) token = "-";
	            	 if(token.compareTo("*unary*plus")==0) token = "+";
	            	 if(token.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.BANG))==0 ) token = " " + token + " ";
	            	
	            	 stack.push(token + right);
	            	 stackPrecedence.push(operatorPrec);
	             } 
          }  else {
            	if(FULL_BRACKETS) stack.push("("+token +")");
            	else stack.push(token);
            	stackPrecedence.push(SINGLE_VAR_NUM_PRECEDENCE);
            } 
        }
	    
	    String ret = new String();
	    ret += stack.pop();
	    
        return ret;
        }
	
	
public double evaluateRPN(Object[] inputTokens ) throws Exception {
		  Stack<Object> tks = new Stack<Object>();
          tks.addAll(Arrays.asList(inputTokens));
          return evaluateRPN_recursive(tks);
	}
	
 private double evaluateRPN_recursive(Stack<Object> tks) throws Exception  {
	    String tk = tks.pop().toString();
	    double x,y;
	    try  {x = Double.parseDouble(tk);}
	    catch (Exception e)  {
	      y = evaluateRPN_recursive(tks);  x = evaluateRPN_recursive(tks);
	      if      (tk.equals("+"))  x += y;
	      else if (tk.equals("-"))  x -= y;
	      else if (tk.equals("*"))  x *= y;
	      else if (tk.equals("/"))  x /= y;
	      else {
	    	  System.out.println("Unrecognized tk: "+tk);
	    	  throw new Exception();
	      }
	    }
	    return x;
	  }
   
	
}
