
package net.pi.sws.http;

import java.io.File;
import java.io.IOException;

import net.pi.sws.util.ExtLog;

/**
 * Default http implementation sending {@link HttpCode#S_METHOD_NOT_ALLOWED}.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
final class BadMethod
extends HttpMethod
{

	private static final ExtLog	L		= ExtLog.get();

	static final String			VERSION	= "HTTP/1.0";

	BadMethod( File root, String head ) throws IOException
	{
		super( root, head, VERSION );
	}

	@Override
	protected void respond() throws IOException
	{
		L.info( "BAD request" );

		setStatus( HttpCode.S_METHOD_NOT_ALLOWED );

		addResponseHeader( new HttpHeader( "RequestLine", getURI() ) );

		for( final HttpHeader h : this.requestH.values() ) {
			addResponseHeader( h );
		}
	}
}
