package acgui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLResolver;
import org.sbml.libsbml.SBMLUri;
import org.sbml.libsbml.libsbml;

/**
 * Resolve URLs to files.
 * @author T.C. Jones
 * @version January 12, 2016
 */
public class SBMLHTTPResolver extends SBMLResolver {

	/**
	 * Creates a new object.
	 */
	public SBMLHTTPResolver()
	{
		super();
	}
	
	/**
	 * Copy constructor. Creates a copy of the given resolver.
	 * @param resolver, the resolver to copy
	 */
	public SBMLHTTPResolver(SBMLHTTPResolver resolver)
	{
		super(resolver);
	}
	
	/**
	 * Creates and return a copy of this resolver.
	 */
	public SBMLResolver cloneObject()
	{
		return new SBMLHTTPResolver();
	}
	/*
	public SBMLDocument resolve(String uri)
	{
		return resolve(uri, "");
	}
	*/
	/**
	 * Resolves the document for the given URI.
	 * @param uri - the URI to the target document
	 * @param baseUri - base URI, in case the URI is a relative one
	 * @return the document, if this resolver can resolve the document or null.
	 */
	public SBMLDocument resolve(String uri, String baseUri)
	{
		System.out.println("enter resolve(String, String)");
		SBMLUri resolved = resolveUri(uri, baseUri);
		if (resolved == null)
		{
			return null;
		}
		
		String urlData = readURL(resolved.getUri());
		if (urlData == null)
		{
			return null;
		}
		return libsbml.readSBMLFromString(urlData);
	}
	/*
	public SBMLUri resolveUri(String uri)
	{
		return resolveUri(uri, "");
	}
	*/
	/**
	 * Resolves the full URI for the given URI without actually reading the document.
	 * @param uri - the URI to the target document
	 * @param baseUri - base URI, in case the URI is a relative one
	 * @return the full URI to the document, if this resolver can resolve the document or null.
	 */
	public SBMLUri resolveUri(String uri, String baseUri)
	{
		System.out.println("enter resolveUri(String, String)");
		SBMLUri uriProspect = new SBMLUri(uri);
		if (!(uriProspect.getScheme().equalsIgnoreCase("http") || uriProspect.getScheme().equalsIgnoreCase("https")))
		{
			// the uri is not a http or https
			return null;
		}
		return uriProspect;
	}
	
	/**
	 * Reads the file located at the given URL address and return as a String.
	 * @param address, the address of the URL
	 * @return the contents of the URL as a String.
	 */
	private String readURL(String address)
	{
		try
		{
			URL url = new URL(address);
			URLConnection connection = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		
			StringBuilder contents = new StringBuilder();
			String inputLine;
			
			while ((inputLine = in.readLine()) != null)
	        {
	        	contents.append(inputLine);
	        }
	        in.close();
	        
	        return contents.toString();
		} catch (Exception e)
		{
			// file download failed
			e.printStackTrace();
			return null;
		}
	}
}
