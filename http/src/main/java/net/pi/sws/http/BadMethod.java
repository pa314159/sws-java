
package net.pi.sws.http;

import java.io.IOException;
import java.io.PrintWriter;

import net.pi.sws.io.IO;

/**
 * Default method implementation sending {@link HttpCode#S_METHOD_NOT_ALLOWED}.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
final class BadMethod
extends HttpMethod<HttpServiceFactory>
{

	BadMethod( HttpRequest request, HttpResponse response, HttpCode status )
	throws IOException
	{
		super( null, request, response );

		response.setStatus( status );
	}

	@Override
	protected void respond() throws IOException
	{
		this.response.setHeader( new HttpHeader( HttpHeader.General.CONTENT_TYPE, "text/plain; charset=ISO-8859-1" ) );
		final PrintWriter cs = new PrintWriter( this.response.getCharStream( IO.ISO_8859_1 ) );

		cs.println( this.response.getStatus() );
		cs.println();
		cs.println( this.request.requestLine );

		for( final HttpHeader h : this.request.getHeaders() ) {
			cs.println( h );
		}
	}

}
