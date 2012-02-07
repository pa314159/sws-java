
package net.pi.sws.dav;

import java.io.IOException;

import net.pi.sws.http.HttpMethod;
import net.pi.sws.http.HttpRequest;
import net.pi.sws.http.HttpResponse;

class DavMethod
extends HttpMethod<DavServiceFactory>
{

	final String	name;

	DavMethod( String method, DavServiceFactory fact, HttpRequest request, HttpResponse response )
	{
		super( fact, request, response );

		this.name = method;
	}

	@Override
	protected void respond() throws IOException
	{
		final DavRequest davRequest = new DavRequest( this );
		final DavResponse davResponse = new DavResponse( this );

		this.fact.hm.process( davRequest, davResponse );
	}
}
