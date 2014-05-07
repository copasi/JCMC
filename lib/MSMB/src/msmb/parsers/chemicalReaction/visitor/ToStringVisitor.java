package msmb.parsers.chemicalReaction.visitor;

import msmb.parsers.chemicalReaction.syntaxtree.*;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;


public class ToStringVisitor extends DepthFirstVoidVisitor {

	//private PrintWriter out;
	OutputStreamWriter out;
//	public ToStringVisitor(final OutputStream o)  { out = new PrintWriter(o, true); }
	public ToStringVisitor(final OutputStream o)  { try {
		out = new OutputStreamWriter(o, "UTF-8");
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} }
	
	
	public void flushWriter()  { try {
		out.flush();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} }

	@Override
	public void visit(final NodeToken n) {   printToken(n.tokenImage); }

//	private void printToken(final String s) {	out.print(s);	out.flush();	}
	private void printToken(final String s) {	try {
		out.write(s);
		out.flush();
	} catch (IOException e) {
		// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}