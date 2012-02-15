
package net.pi.sws.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.pi.sws.io.ChannelInputStream;
import net.pi.sws.io.LimitedChannelInput;

/**
 * The HTTP request object.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class HttpRequest
extends HttpMessage<ReadableByteChannel, InputStream, Reader>
{

	private final String					uri;

	final String							requestLine;

	private final InetSocketAddress			local;

	private final InetSocketAddress			remote;

	private final Map<String, HttpCookie>	cookies	= new HashMap<String, HttpCookie>();

	private final URL						url;

	HttpRequest( ReadableByteChannel channel, String uri, String request, InetSocketAddress local, InetSocketAddress remote )
	throws IOException
	{
		super( channel );

		this.uri = uri;
		this.url = new URL( "http", local.getHostName(), local.getPort(), uri );

		this.requestLine = request;

		this.local = local;
		this.remote = remote;

		parseCookies( HttpHeader.Request.COOKIE );
		parseCookies( HttpHeader.Request.COOKIE_2 );
	}

	public HttpCookie getCookie( String name )
	{
		return this.cookies.get( name );
	}

	public Collection<HttpCookie> getCookies()
	{
		return this.cookies.values();
	}

	public InetSocketAddress getLocalAddress()
	{
		return this.local;
	}

	public InetSocketAddress getRemoteAddress()
	{
		return this.remote;
	}

	public String getURI()
	{
		return this.uri;
	}

	public URL getURL()
	{
		return this.url;
	}

	void consumeInput() throws IOException
	{
		final ReadableByteChannel w = wrap();

		if( w instanceof LimitedChannelInput ) {
			((LimitedChannelInput) w).consume();
		}
	}

	@Override
	InputStream newByteStream( ReadableByteChannel channel ) throws IOException
	{
		return new ChannelInputStream( channel );
	}

	@Override
	Reader newCharStream( InputStream stream, Charset cs )
	{
		return new InputStreamReader( stream, cs );
	}

	@Override
	ReadableByteChannel wrap( ReadableByteChannel channel ) throws IOException
	{
		final HttpHeader h = getHeader( HttpHeader.General.CONTENT_LENGTH );

		if( h == null ) {
			return channel;
		}

		final int expected = Integer.parseInt( h.getValue() );

		return new LimitedChannelInput( channel, expected );
	}

	private void parseCookies( String headName )
	{
		final HttpHeader h = getHeader( headName );

		if( h == null ) {
			return;
		}

		for( final String v : h.getValues() ) {
			final List<HttpCookie> parsed = HttpCookie.parse( headName + ": " + v );

			for( final HttpCookie c : parsed ) {
				this.cookies.put( c.getName(), c );
			}
		}
	}

}
