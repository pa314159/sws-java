
package net.pi.sws.http;

import java.io.IOException;

/**
 * Base of all HTTP methods.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public abstract class HttpMethod
{

	protected final HttpRequest		request;

	protected final HttpResponse	response;

	protected HttpMethod( HttpRequest request, HttpResponse response )
	{
		this.request = request;
		this.response = response;
	}

	void flush() throws IOException
	{
		this.response.flush();
	}

	protected abstract void respond() throws IOException;

}
