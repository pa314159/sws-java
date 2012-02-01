
package net.pi.sws.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.pi.sws.http.HTTP;
import net.pi.sws.http.HttpHeader;
import net.pi.sws.http.HttpHeader.General;
import net.pi.sws.http.HttpMethod;
import net.pi.sws.http.HttpRequest;
import net.pi.sws.http.HttpResponse;
import net.pi.sws.http.AbstractHttpServiceFactory;
import net.pi.sws.io.IO;
import net.pi.sws.util.ExtLog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

@HTTP( "POST" )
public class PostMethod
extends HttpMethod<AbstractHttpServiceFactory>
{

	static private final ExtLog	L	= ExtLog.get();

	private Document			dom;

	public PostMethod( AbstractHttpServiceFactory fact, HttpRequest request, HttpResponse response )
	{
		super( fact, request, response );
	}

	protected Element addElement( String name ) throws IOException
	{
		final Element child = getDocument().createElement( name );

		this.dom.appendChild( child );

		return child;
	}

	protected Element addElement( String name, Element element ) throws IOException
	{
		final Element child = getDocument().createElement( name );

		element.appendChild( child );

		return child;
	}

	protected Element addElement( String name, String value, Element element ) throws IOException
	{
		final Element child = getDocument().createElement( name );

		element.appendChild( child );
		child.appendChild( getDocument().createTextNode( value ) );

		return child;
	}

	protected Document getDocument() throws IOException
	{
		if( this.dom != null ) {
			return this.dom;
		}

		try {
			return this.dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}
		catch( final ParserConfigurationException e ) {
			throw new IOException( "Cannot create XML document", e );
		}
	}

	@Override
	protected void respond() throws IOException
	{
		final InputStream is = this.request.getByteStream();
		final ByteArrayOutputStream os = new ByteArrayOutputStream();

		IO.copy( is, os );

		String post = new String( os.toByteArray(), IO.ISO_8859_1 );

		post = URLDecoder.decode( post, "UTF-8" );

		final Map<String, String> map = new TreeMap<String, String>();
		final String[] pairs = post.split( "&" );

		for( final String pair : pairs ) {
			final int ix = pair.indexOf( '=' );

			if( ix > 0 ) {
				map.put( pair.substring( 0, ix ), pair.substring( ix + 1 ) );
			}
			else {
				map.put( pair, "" );
			}
		}

		L.info( "%s", map );

		final Element top = addElement( "request" );

		// send back the request headers
		final Element headers = addElement( "headers", top );

		for( final HttpHeader h : this.request.getHeaders() ) {
			final Element header = addElement( "header", headers );

			addElement( "name", h.getName(), header );

			final Element values = addElement( "values", header );

			for( final String v : h.getValues() ) {
				addElement( "value", v, values );
			}
		}

		// send back the request parameters
		final Element params = addElement( "parameters", top );

		for( final Map.Entry<String, String> ent : map.entrySet() ) {
			final Element param = addElement( "parameter", params );

			addElement( "name", ent.getKey(), param );
			addElement( "value", ent.getValue(), param );
		}

		sendDocument();
	}

	protected void sendDocument() throws IOException
	{
		if( this.dom == null ) {
			throw new IllegalStateException( "No XML document has been requested" );
		}

		try {
			final TransformerFactory f = TransformerFactory.newInstance();

			f.setAttribute( "indent-number", new Integer( 2 ) );

			final Transformer t = f.newTransformer();

			t.setOutputProperty( OutputKeys.INDENT, "yes" );

			this.response.setHeader( new HttpHeader( General.CONTENT_TYPE, "text/xml" ) );

			t.transform( new DOMSource( this.dom ), new StreamResult( this.response.getCharStream( IO.UTF8 ) ) );
		}
		catch( final TransformerException e ) {
			throw new IOException( "Cannot output XML document", e );
		}
	}
}
