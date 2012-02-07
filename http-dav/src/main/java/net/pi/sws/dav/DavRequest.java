
package net.pi.sws.dav;

import java.io.IOException;
import java.io.InputStream;
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
		return null;
	}

	@Override
	public List<Cookie> getCookies()
	{
		return null;
	}

	@Override
	public String getFromAddress()
	{
		return this.request.getRemoteAddress().getCanonicalHostName();
	}

	@Override
	public Map<String, String> getHeaders()
	{
		return null;
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
		final HttpHeader h = this.request.getHeader( header.name() );

		return h != null ? h.getValue() : null;
	}

	@Override
	public void parseRequestParameters( Map<String, String> params, Map<String, FileItem> files ) throws RequestParseException
	{
	}

	@Override
	public void setAuthorization( Auth auth )
	{
		this.auth = auth;
	}

}
