
package net.pi.sws.http;

import java.io.IOException;

import net.pi.sws.util.ExtLog;

class BadMethod
extends HttpMethod
{

	private static final ExtLog	L		= ExtLog.get();

	static final String			VERSION	= "HTTP/1.0";

	BadMethod( String head )
	{
		super( head, VERSION );
	}

	@Override
	protected void execute() throws IOException
	{
		L.info( "BAD request" );

		setStatus( HttpCode.S_BAD_REQUEST );

		addHeader( new HttpHeader( "RequestLine", getURI() ) );

		for( final HttpHeader h : this.requestH.values() ) {
			addHeader( h );
		}
	}
}
