
package net.pi.sws.http;

import java.io.IOException;

/**
 * Default method implementation sending {@link HttpCode#S_METHOD_NOT_ALLOWED}.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
final class BadMethod
extends HttpMethod<AbstractHttpServiceFactory>
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
	}

}
