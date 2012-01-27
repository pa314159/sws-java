
package net.pi.sws.http;

import java.io.IOException;

/**
 * Simple header structure, holding the name and the content of the header
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class HttpHeader
{

	final String	name;

	final String	content;

	private int		hash;

	private String	text;

	public HttpHeader( String name, String content ) throws IOException
	{
		this.name = name;
		this.content = content;

		init();
	}

	HttpHeader( String line ) throws IOException
	{
		final int colon = line.indexOf( ':' );

		if( colon <= 0 ) {
			throw new IOException( "Error parsing line " + line );
		}

		this.name = line.substring( 0, colon ).trim();
		this.content = line.substring( colon + 1 ).trim();

		init();
	}

	@Override
	public boolean equals( Object obj )
	{
		if( obj == this ) {
			return true;
		}
		if( obj == null ) {
			return false;
		}
		if( getClass() != obj.getClass() ) {
			return false;
		}

		return this.name.equalsIgnoreCase( ((HttpHeader) obj).name );
	}

	@Override
	public int hashCode()
	{
		return this.hash;
	}

	@Override
	public String toString()
	{
		return this.text.toString();
	}

	private void init() throws IOException
	{
		if( this.name.isEmpty() ) {
			throw new IOException( "Header name cannot be empty" );
		}

		this.hash = 31 + this.name.toUpperCase().hashCode();
		this.text = String.format( "%s: %s", this.name, this.content );
	}
}
