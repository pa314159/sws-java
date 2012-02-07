
package net.pi.sws.dav;

import java.io.IOException;

import net.pi.sws.http.HttpMethod;
import net.pi.sws.http.HttpRequest;
import net.pi.sws.http.HttpResponse;
import net.pi.sws.http.MethodFactory;

public class DavMethodFactory
implements MethodFactory<DavServiceFactory>
{

	@Override
	public HttpMethod<DavServiceFactory> get( String method, DavServiceFactory fact, HttpRequest request, HttpResponse response )
	throws IOException
	{
		return new DavMethod( method, fact, request, response );
	}

}
