
package net.pi.sws.http;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

abstract class HttpMessage<C extends Channel, S extends Closeable, R extends Closeable>
{

	private final Map<String, HttpHeader>	headers	= new HashMap<String, HttpHeader>();

	private final C							channel;

	private IOType							ioType;

	private S								byteS;

	private R								charS;

	HttpMessage( C channel )
	{
		this.channel = channel;
	}

	public void addHeader( HttpHeader h )
	{
		this.headers.put( h.name.toLowerCase(), h );
	}

	public S getByteStream() throws IOException
	{
		if( this.ioType == null ) {
			this.ioType = IOType.ByteStream;

			return this.byteS = newByteStream( wrap( this.channel ) );
		}
		if( this.ioType == IOType.Channel ) {
			return this.byteS;
		}

		throw new IllegalStateException( String.format( "Already called get%s()", this.ioType ) );
	}

	public C getChannel() throws IOException
	{
		if( this.ioType == null ) {
			this.ioType = IOType.Channel;

			return wrap( this.channel );
		}
		if( this.ioType == IOType.Channel ) {
			return this.channel;
		}

		throw new IllegalStateException( String.format( "Already called get%s()", this.ioType ) );
	}

	public R getCharStream( Charset cs ) throws IOException
	{
		if( this.ioType == null ) {
			this.ioType = IOType.CharStream;

			return this.charS = newCharStream( newByteStream( wrap( this.channel ) ), cs );
		}
		if( this.ioType == IOType.CharStream ) {
			return this.charS;
		}

		throw new IllegalStateException( String.format( "Already called get%s()", this.ioType ) );
	}

	public HttpHeader getHeader( String name )
	{
		return this.headers.get( name.toLowerCase() );
	}

	public Collection<HttpHeader> getHeaders()
	{
		return this.headers.values();
	}

	public final boolean isHeaderPresent( String name, String content )
	{
		final HttpHeader h = getHeader( name );

		if( h == null ) {
			return false;
		}

		if( content != null ) {
			return h.content.equalsIgnoreCase( content );
		}
		else {
			return true;
		}
	}

	abstract S newByteStream( C channel ) throws IOException;

	abstract R newCharStream( S stream, Charset cs );

	C wrap( C channel ) throws IOException
	{
		return channel;
	}
}