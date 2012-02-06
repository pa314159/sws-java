
package net.pi.sws.http;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

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

		/** <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.30">Section 14.30</a> */
		String	LOCATION		= "Location";

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

	final String				name;

	private final List<String>	values	= new ArrayList<String>();

	public HttpHeader( String name, Date value )
	{
		if( (name == null) || name.isEmpty() ) {
			throw new IllegalArgumentException( "Header name cannot be empty" );
		}

		this.name = name;
		this.values.add( HttpDate.format( value ) );
	}

	public HttpHeader( String name, int value )
	{
		if( (name == null) || name.isEmpty() ) {
			throw new IllegalArgumentException( "Header name cannot be empty" );
		}

		this.name = name;

		addValue( value );
	}

	public HttpHeader( String name, long value )
	{
		if( (name == null) || name.isEmpty() ) {
			throw new IllegalArgumentException( "Header name cannot be empty" );
		}

		this.name = name;

		addValue( value );
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

		final String[] values = content.split( "," );

		for( final String v : values ) {
			addValue( v.trim() );
		}
	}

	public void addValue( int value )
	{
		this.values.add( Integer.toString( value ) );
	}

	public void addValue( long value )
	{
		this.values.add( Long.toString( value ) );
	}

	public void addValue( String value )
	{
		this.values.add( value );
	}

	public String getName()
	{
		return this.name;
	}

	public String getValue()
	{
		return this.values.isEmpty() ? null : this.values.get( 0 );
	}

	public String[] getValues()
	{
		return this.values.toArray( new String[0] );
	}

	public boolean is( String name )
	{
		return this.name.equalsIgnoreCase( name );
	}

	public boolean matches( Pattern pat )
	{
		// XXX the pattern could match an empty string, but that would be a nonsense, simply check getValue() against
		// null
		if( this.values.isEmpty() ) {
			return false;
		}

		for( final String v : this.values ) {
			if( pat.matcher( v ).matches() ) {
				return true;
			}
		}

		return false;
	}

	public void setValue( int value )
	{
		this.values.clear();

		addValue( value );
	}

	public void setValue( long value )
	{
		this.values.clear();

		addValue( value );
	}

	public void setValue( String value )
	{
		this.values.clear();

		addValue( value );
	}

	@Override
	public String toString()
	{
		final StringBuilder b = new StringBuilder();

		b.append( this.name );
		b.append( ": " );

		for( int k = 0; k < this.values.size(); k++ ) {
			if( k > 0 ) {
				b.append( ", " );
			}

			b.append( encode( this.values.get( k ) ) );
		}

		return b.toString();
	}

	private String encode( String text )
	{
		try {
			return MimeUtility.encodeText( text );
		}
		catch( final UnsupportedEncodingException e ) {
			return text;
		}
	}
}
