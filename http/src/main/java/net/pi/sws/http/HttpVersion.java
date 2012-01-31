
package net.pi.sws.http;

import java.io.IOException;

/**
 * Supported HTTP versions.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public enum HttpVersion
{
	HTTP1_0( "HTTP/1.0", HttpRFC.RFC_1945 ),
	HTTP1_1( "HTTP/1.1", HttpRFC.RFC_2616 ),

	;

	static public HttpVersion get( String version ) throws IOException
	{
		if( version == null ) {
			return HTTP1_0;
		}

		version = version.trim();

		for( final HttpVersion v : values() ) {
			if( version.equals( v.tok ) ) {
				return v;
			}
		}

		return null;
	}

	private final String	tok;

	public final HttpRFC	rfc;

	HttpVersion( String tok, HttpRFC rfc )
	{
		this.tok = tok;
		this.rfc = rfc;
	}

	@Override
	public String toString()
	{
		return this.tok;
	}
}
