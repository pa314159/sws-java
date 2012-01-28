
package net.pi.sws.http.methods;

import java.io.File;
import java.io.IOException;

import net.pi.sws.http.HTTP;
import net.pi.sws.http.HttpCode;
import net.pi.sws.http.HttpHeader;
import net.pi.sws.http.HttpMethod;
import net.pi.sws.http.HttpRequest;
import net.pi.sws.http.HttpResponse;

/**
 * Implementation of HEAD
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
@HTTP( "HEAD" )
public class HeadMethod
extends HttpMethod
{

	HeadMethod( HttpRequest request, HttpResponse response ) throws IOException
	{
		super( request, response );
	}

	@Override
	protected final void respond() throws IOException
	{
		final File file = new File( getResponse().getRoot(), getRequest().getURI() );

		if( !file.exists() ) {
			getResponse().setStatus( HttpCode.NOT_FOUND );
		}
		else {
			send( file );
		}
	}

	protected void send( File file ) throws IOException
	{
		if( file.isDirectory() ) {
			getResponse().addHeader( new HttpHeader( "Content-Type", "text/html; charset=UTF-8" ) );
		}
		else {
			getResponse().addHeader( new HttpHeader( "Content-Type", "application/octet-stream" ) );
			getResponse().addHeader( new HttpHeader( "Content-Length", Long.toString( file.length() ) ) );
		}
	}

}
