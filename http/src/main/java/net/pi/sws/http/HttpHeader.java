
package net.pi.sws.http;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.mail.internet.MimeUtility;

import net.pi.sws.util.HttpDate;

/**
 * Simple header structure, holding the name and the content of the header
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class HttpHeader
{

	/** <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.5">RFC 2616: 4.5 General Header Fields</a> */
	public interface General
	{

		/** <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.10">Section 14.10</a> */
		String	CONNECTION			= "Connection";

		/** <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.11">Section 14.11</a> */
		String	CONTENT_ENCODING	= "Content-Encoding";

		/** <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.13">Section 14.13</a> */
		String	CONTENT_LENGTH		= "Content-Length";

		/** <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.17">Section 14.17</a> */
		String	CONTENT_TYPE		= "Content-Type";
	}

	/**
	 * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5.3">RFC 2616: 5.3 Request Header Fields</a>
	 */
	public interface Request
	{

		/** <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.3">Section 14.3</a> */
		String	ACCEPT_ENCODING	= "Accept-Encoding";

		/** <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.20">Section 14.20</a> */
		String	EXPECT			= "Expect";

		/** <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.36">Section 14.36</a> */
		String	REFERER			= "Referer";
	}

	/**
	 * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html#sec6.2">RFC 2616: 6.2 Response Header Fields</a>
	 */
	public interface Response
	{

		/** <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.29">Section 14.29</a> */
		String	LAST_MODIFIED	= "Last-Modified";

		/** <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.38">Section 14.38</a> */
		String	SERVER			= "Server";
	}

	static HttpHeader parse( String line ) throws UnsupportedEncodingException
	{
		final int colon = line.indexOf( ':' );

		if( colon <= 0 ) {
			throw new IllegalArgumentException( "Error parsing line " + line );
		}

		final String name = line.substring( 0, colon ).trim();
		String content = line.substring( colon + 1 ).trim();

		try {
			content = MimeUtility.decodeText( content );
		}
		catch( final UnsupportedEncodingException e ) {
		}

		return new HttpHeader( name, content );
	}

	final String	name;

	final String	content;

	public HttpHeader( String name, Date value )
	{
		if( (name == null) || name.isEmpty() ) {
			throw new IllegalArgumentException( "Header name cannot be empty" );
		}

		this.name = name;
		this.content = HttpDate.format( value );
	}

	public HttpHeader( String name, int value )
	{
		if( (name == null) || name.isEmpty() ) {
			throw new IllegalArgumentException( "Header name cannot be empty" );
		}

		this.name = name;
		this.content = Integer.toString( value );
	}

	public HttpHeader( String name, long value )
	{
		if( (name == null) || name.isEmpty() ) {
			throw new IllegalArgumentException( "Header name cannot be empty" );
		}

		this.name = name;
		this.content = Long.toString( value );
	}

	public HttpHeader( String name, String content )
	{
		if( (name == null) || name.isEmpty() ) {
			throw new IllegalArgumentException( "Header name cannot be empty" );
		}

		this.name = name;

		if( content != null ) {
			content = content.trim();
		}
		else {
			content = "";
		}

		this.content = content;
	}

	public boolean is( String name, String content )
	{
		if( !this.name.equalsIgnoreCase( name ) ) {
			return false;
		}

		if( content != null ) {
			return this.content.equalsIgnoreCase( content );
		}

		return true;
	}

	@Override
	public String toString()
	{
		try {
			return String.format( "%s: %s", this.name, MimeUtility.encodeText( this.content ) );
		}
		catch( final UnsupportedEncodingException e ) {
			return String.format( "%s: %s", this.name, this.content );
		}
	}
}
