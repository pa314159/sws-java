
package net.pi.sws.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.SocketChannel;
import java.util.regex.Pattern;

import net.pi.sws.io.ChannelInput;
import net.pi.sws.io.ChannelOutput;
import net.pi.sws.io.IO;
import net.pi.sws.pool.Service;
import net.pi.sws.util.ExtLog;

/**
 * HTTP service implementation.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
final class HttpService
implements Service
{

	/**
	 * States of the internal state machine.
	 * 
	 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
	 */
	enum State
	{
		REQUEST,
		HEADER,
		RESPONSE,
		CLOSE
	}

	/**
	 * The execution context of the state machine.
	 * 
	 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
	 */
	static class StateContext
	{

		State		state	= State.REQUEST;

		HttpRFC		rfc		= HttpRFC.RFC_1945;

		HttpMethod	method;

		boolean		keepAlive;
	}

	private static final Pattern		PAT_100_CONTINUE	= Pattern.compile( "100-continue", Pattern.CASE_INSENSITIVE );

	private static final Pattern		PAT_KEEP_ALIVE		= Pattern.compile( "Keep-Alive", Pattern.CASE_INSENSITIVE );

	private static final ExtLog			L					= ExtLog.get();

	private static final int			TIMEOUT				= 0;

	private final ChannelOutput			oc;

	private final ChannelInput			ic;

	private StateContext				context				= new StateContext();

	private final HttpServiceFactory	fact;

	private final InetSocketAddress		remote;

	private final InetSocketAddress		local;

	HttpService( HttpServiceFactory fact, SocketChannel channel ) throws IOException
	{
		this.fact = fact;

		channel.configureBlocking( false );

		this.remote = (InetSocketAddress) channel.socket().getRemoteSocketAddress();
		this.local = (InetSocketAddress) channel.socket().getLocalSocketAddress();

		this.oc = new ChannelOutput( channel, TIMEOUT );
		this.ic = new ChannelInput( channel, TIMEOUT );
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see net.pi.sws.pool.Service#accept(java.nio.channels.SocketChannel)
	 */
	@Override
	public void accept( SocketChannel channel ) throws IOException
	{
		try {
			while( this.context.state != State.CLOSE ) {
				process();
			}
		}
		finally {
			IO.close( this.ic );
			IO.close( this.oc );
		}
	}

	/**
	 * Action performed in the {@link State#HEADER} state.
	 * 
	 * @throws IOException
	 */
	private void doHeader() throws IOException
	{
		final String line = IO.readLINE( this.ic );

		L.trace( "%s: %s", this.context.state, line );

		if( line.isEmpty() ) {
			this.context.state = State.RESPONSE;

			return;
		}

		assert this.context.method != null;

		final HttpHeader h = HttpHeader.parse( line );
		final HttpRequest request = this.context.method.request;

		// intercept compression
		if( h.is( HttpHeader.Request.ACCEPT_ENCODING ) ) {
			for( final CompressionType c : CompressionType.values() ) {
				if( h.matches( c.pattern ) ) {
					this.context.method.response.compression = c;

					break;
				}
			}
		}

		// intercept Keep-Alive
		// TODO RFC2068 says something about HTTP/1.0 persistent connections, but RFC1945 doesn't mention them
		if( this.context.rfc != HttpRFC.RFC_1945 ) {
			if( h.is( HttpHeader.Request.EXPECT ) && h.matches( PAT_100_CONTINUE ) ) {
				this.context.keepAlive = true;

				return; // filter it
			}
			if( h.is( HttpHeader.General.CONNECTION ) && h.matches( PAT_KEEP_ALIVE ) ) {
				this.context.rfc = HttpRFC.RFC_2068;

				this.context.keepAlive = true;

				return; // filter it
			}
		}

		request.setHeader( h );
	}

	/**
	 * Action performed in the {@link State#INVOKE} state.
	 * 
	 * @throws IOException
	 */
	private void doInvoke() throws IOException
	{
		this.context.method.respond();
		this.context.method.flush();

		L.trace( "%s:", this.context.state );

		if( this.context.keepAlive ) {
			this.context = new StateContext();
		}
		else {
			this.context.state = State.CLOSE;
		}
	}

	/**
	 * Action performed in the {@link State#REQUEST} state.
	 * 
	 * @throws IOException
	 */
	private void doRequest() throws IOException
	{
		final String line = IO.readLINE( this.ic );

		if( line == null ) {
			this.context.state = State.CLOSE;

			return;
		}

		L.trace( "%s: %s", this.context.state, line );

		final String[] parts = line.split( "\\s+" );

		HttpVersion version = HttpVersion.HTTP1_0;
		String uri = "";
		String method = null;

		switch( parts.length ) {
			case 3:
				version = HttpVersion.get( parts[2] );

				if( version == null ) {
					break;
				}

				this.context.rfc = version.rfc;

			case 2:
				method = parts[0];
				uri = parts[1];
			break;
		}

		final HttpRequest request = new HttpRequest( this.ic, URI.create( uri ).getPath(), line, this.local, this.remote );
		final HttpResponse response = new HttpResponse( this.oc );

		if( method == null ) {
			if( version == null ) {
				this.context.method = new BadMethod( request, response, HttpCode.VERSION_NOT_SUPPORTED );
			}
			else {
				this.context.method = new BadMethod( request, response, HttpCode.BAD_REQUEST );
			}
		}
		else {
			final MethodFactory mf = this.fact.getMethodFactory();

			this.context.method = mf.get( method, this.fact, request, response );
		}

		this.context.state = State.HEADER;
	}

	private void process() throws IOException
	{
		switch( this.context.state ) {
			case REQUEST:
				doRequest();
			break;

			case HEADER:
				doHeader();
			break;

			case RESPONSE:
				doInvoke();
			break;

			case CLOSE:
		}
	}
}
