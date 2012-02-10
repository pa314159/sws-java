
package net.pi.sws.dav;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.pi.sws.http.HttpHeader;
import net.pi.sws.http.HttpRequest;
import net.pi.sws.http.HttpResponse;

import com.bradmcevoy.http.AbstractRequest;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Cookie;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.RequestParseException;

public class DavRequest
extends AbstractRequest
{

	private final DavMethod		method;

	private final HttpRequest	request;

	private final HttpResponse	response;

	private Auth				auth;

	private Map<String, Cookie>	cookies;

	DavRequest( DavMethod method )
	{
		this.method = method;
		this.request = method.getRequest();
		this.response = method.getResponse();
	}

	@Override
	public String getAbsoluteUrl()
	{
		return this.request.getURI();
	}

	@Override
	public Auth getAuthorization()
	{
		return this.auth;
	}

	@Override
	public Cookie getCookie( String name )
	{
		return cookies().get( name );
	}

	@Override
	public List<Cookie> getCookies()
	{
		return Arrays.asList( cookies().values().toArray( new Cookie[0] ) );
	}

	@Override
	public String getFromAddress()
	{
		return this.request.getRemoteAddress().getCanonicalHostName();
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
	public InputStream getInputStream() throws IOException
	{
		return this.request.getByteStream();
	}

	@Override
	public Method getMethod()
	{
		return Method.valueOf( this.method.name );
	}

	@Override
	public String getRemoteAddr()
	{
		return this.request.getRemoteAddress().getHostAddress();
	}

	@Override
	public String getRequestHeader( Header header )
	{
		return this.request.getHeaderValue( header.name() );
	}

	@Override
	public void parseRequestParameters( Map<String, String> params, Map<String, FileItem> files ) throws RequestParseException
	{
		// throw new UnsupportedOperationException( "NOT YET IMPLEMENTED" );
	}

	@Override
	public void setAuthorization( Auth auth )
	{
		this.auth = auth;
	}

	Map<String, Cookie> cookies()
	{
		if( this.cookies == null ) {
			this.cookies = new HashMap<String, Cookie>();

			for( final HttpCookie c : this.request.getCookies() ) {
				this.cookies.put( c.getName(), new MiltonCookie( c ) );
			}
		}

		return this.cookies;
	}

}
