
package net.pi.sws.dav;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.pi.sws.http.HttpCode;
import net.pi.sws.http.HttpHeader;
import net.pi.sws.http.HttpRequest;
import net.pi.sws.http.HttpResponse;

import com.bradmcevoy.http.AbstractResponse;
import com.bradmcevoy.http.Cookie;

class DavResponse
extends AbstractResponse
{

	private final DavMethod		method;

	private final HttpRequest	request;

	private final HttpResponse	response;

	DavResponse( DavMethod method )
	{
		this.method = method;
		this.request = method.getRequest();
		this.response = method.getResponse();
	}

	@Override
	public Map<String, String> getHeaders()
	{
		final Map<String, String> result = new HashMap<String, String>();

		for( final HttpHeader h : this.request.getHeaders() ) {
			result.put( h.getName(), h.getValue() );
		}

		return result;
	}

	@Override
	public String getNonStandardHeader( String code )
	{
		final HttpHeader h = this.response.getHeader( code );

		return h != null ? h.getValue() : null;
	}

	@Override
	public OutputStream getOutputStream()
	{
		try {
			return this.response.getByteStream();
		}
		catch( final IOException e ) {
			throw new RuntimeException( e );
		}
	}

	@Override
	public Status getStatus()
	{
		// XXX Status.fromCode should be STATIC!!!
		return Status.SC_OK.fromCode( this.response.getStatus().intValue() );
	}

	@Override
	public void setAuthenticateHeader( List<String> challenges )
	{
	}

	@Override
	public Cookie setCookie( Cookie cookie )
	{
		final MiltonCookie c = new MiltonCookie( cookie );

		this.response.addCookie( c.cookie );

		return c;
	}

	@Override
	public Cookie setCookie( String name, String value )
	{
		final MiltonCookie c = new MiltonCookie( name, value );

		this.response.addCookie( c.cookie );

		return c;
	}

	@Override
	public void setNonStandardHeader( String code, String value )
	{
		this.response.setHeader( new HttpHeader( code, value ) );
	}

	@Override
	public void setStatus( Status status )
	{
		this.response.setStatus( new HttpCode( status.code ) );
	}
}
