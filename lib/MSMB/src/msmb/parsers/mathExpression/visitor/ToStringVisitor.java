package msmb.parsers.mathExpression.visitor;

import msmb.parsers.mathExpression.MR_Expression_ParserConstantsNOQUOTES;
import msmb.parsers.mathExpression.syntaxtree.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;


public class ToStringVisitor extends DepthFirstVoidVisitor {

	OutputStreamWriter out;

	public ToStringVisitor(final OutputStream o)  { 
		try {
		out = new OutputStreamWriter(o, "UTF-8");
	} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	} }
	
	
	public void flushWriter()  { try {
		out.flush();
	} catch (IOException e) {
		e.printStackTrace();
	} }

	@Override
	public void visit(final NodeToken n) {  
		printToken(n.tokenImage); 
	}

	private void printToken(final String s) {	
		try {
			if(s.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.XOR))==0||
					s.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.BANG	))==0) {
				out.write(" ");
			}
			out.write(s);

			if(s.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.XOR))==0||
					s.compareTo(MR_Expression_ParserConstantsNOQUOTES.getTokenImage(MR_Expression_ParserConstantsNOQUOTES.BANG))==0) {
				out.write(" ");
			}
		
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public static String toString(INode element) {
		ByteArrayOutputStream string = new ByteArrayOutputStream();
		ToStringVisitor toString = new ToStringVisitor(string);
		element.accept(toString);
		try {
			return new String(string.toByteArray(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static String toBinary(String s) {
		 byte[] bytes = s.getBytes();
		  StringBuilder binary = new StringBuilder();
		  for (byte b : bytes)
		  {
		     int val = b;
		     for (int i = 0; i < 8; i++)
		     {
		        binary.append((val & 128) == 0 ? 0 : 1);
		        val <<= 1;
		     }
		     binary.append(' ');
		  }
		  return new String(binary);
	}
}
