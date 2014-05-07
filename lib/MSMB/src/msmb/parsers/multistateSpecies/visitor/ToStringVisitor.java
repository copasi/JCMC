package msmb.parsers.multistateSpecies.visitor;

import msmb.parsers.multistateSpecies.syntaxtree.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;


public class ToStringVisitor extends DepthFirstVoidVisitor {

	OutputStreamWriter out;
	public ToStringVisitor(final OutputStream o)  { try {
		out = new OutputStreamWriter(o, "UTF-8");
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}  }
	public void flushWriter()  { try {
		out.flush();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} }

	@Override
	public void visit(final NodeToken n) {   printToken(n.tokenImage); }

	private void printToken(final String s) {	try {
		out.write(s);
		out.flush();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}		}

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
