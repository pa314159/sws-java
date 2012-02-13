
package net.pi.sws.http;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Base of a HTTP message.
 * 
 * @see HttpRequest
 * @see HttpResponse
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
abstract class HttpMessage<C extends Channel, S extends Closeable, R extends Closeable>
{

	private final Map<String, HttpHeader>	headers	= new HashMap<String, HttpHeader>();

	private IOType							ioType;

	private final C							channel;

	private C								wrapped;

	private S								byteS;

	private R								charS;

	HttpMessage( C channel )
	{
		this.channel = channel;
	}

	public final S getByteStream() throws IOException
	{
		if( this.ioType == null ) {
			this.ioType = IOType.ByteStream;

			return this.byteS = newByteStream( wrap() );
		}
		if( this.ioType == IOType.ByteStream ) {
			return this.byteS;
		}

		throw new IllegalStateException( String.format( "Already called get%s()", this.ioType ) );
	}

	public final C getChannel() throws IOException
	{
		if( this.ioType == null ) {
			this.ioType = IOType.Channel;

			return wrap();
		}
		if( this.ioType == IOType.Channel ) {
			return wrap();
		}

		throw new IllegalStateException( String.format( "Already called get%s()", this.ioType ) );
	}

	public final R getCharStream( Charset cs ) throws IOException
	{
		if( this.ioType == null ) {
			this.ioType = IOType.CharStream;

			return this.charS = newCharStream( newByteStream( wrap() ), cs );
		}
		if( this.ioType == IOType.CharStream ) {
			return this.charS;
		}

		throw new IllegalStateException( String.format( "Already called get%s()", this.ioType ) );
	}

	public final HttpHeader getHeader( String name )
	{
		return this.headers.get( name.toLowerCase() );
	}

	public final Collection<HttpHeader> getHeaders()
	{
		return this.headers.values();
	}

	public final String getHeaderValue( String name )
	{
		final HttpHeader h = getHeader( name );

		return h != null ? h.getValue() : null;
	}

	public final String[] getHeaderValues( String name )
	{
		final HttpHeader h = getHeader( name );

		return h != null ? h.getValues() : null;
	}

	public final boolean isHeaderPresent( String name )
	{
		final HttpHeader h = getHeader( name );

		if( h == null ) {
			return false;
		}

		assert h.is( name );

		return true;
	}

	public void setHeader( HttpHeader h )
	{
		this.headers.put( h.name.toLowerCase(), h );
	}

	abstract S newByteStream( C channel ) throws IOException;

	abstract R newCharStream( S stream, Charset cs );

	C wrap() throws IOException
	{
		if( this.wrapped == null ) {
			this.wrapped = wrap( this.channel );
		}

		return this.wrapped;
	}

	C wrap( C channel ) throws IOException
	{
		return channel;
	}
}
