
package net.pi.sws.http;

import java.io.IOException;

/**
 * Base of all HTTP methods.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public abstract class HttpMethod<F extends HttpServiceFactory>
{

	protected final F				fact;

	protected final HttpRequest		request;

	protected final HttpResponse	response;

	protected HttpMethod( F fact, HttpRequest request, HttpResponse response )
	{
		this.fact = fact;
		this.request = request;
		this.response = response;
	}

	public HttpRequest getRequest()
	{
		return this.request;
	}

	public HttpResponse getResponse()
	{
		return this.response;
	}

	protected abstract void respond() throws IOException;

	void flush() throws IOException
	{
		this.request.consumeInput();
		this.response.flush();
	}

}
