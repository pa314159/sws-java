
package net.pi.sws.http.methods;

import java.io.File;
import java.io.IOException;

import net.pi.sws.http.HTTP;
import net.pi.sws.http.HttpCode;
import net.pi.sws.http.HttpHeader;
import net.pi.sws.http.HttpMethod;

/**
 * Implementation of HEAD
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
@HTTP( "HEAD" )
public class HeadMethod
extends HttpMethod
{

	public HeadMethod( File root, String uri, String version ) throws IOException
	{
		super( root, uri, version );
	}

	protected void send( File file ) throws IOException
	{
		if( file.isDirectory() ) {
			addResponseHeader( new HttpHeader( "Content-Type", "text/html; charset=UTF-8" ) );
		}
		else {
			addResponseHeader( new HttpHeader( "Content-Type", "application/octet-stream" ) );
			addResponseHeader( new HttpHeader( "Content-Length", Long.toString( file.length() ) ) );
		}
	}

	@Override
	protected final void respond() throws IOException
	{
		final File file = new File( this.root, getURI() );

		if( !file.exists() ) {
			setStatus( HttpCode.S_NOT_FOUND );
		}
		else {
			send( file );
		}
	}

}
