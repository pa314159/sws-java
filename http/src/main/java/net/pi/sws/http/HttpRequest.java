
package net.pi.sws.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

import net.pi.sws.io.ChannelInputStream;

/**
 * The HTTP request object.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class HttpRequest
extends HttpMessage<ReadableByteChannel, InputStream, Reader>
{

	/**
	 * A reading channel implementation whose input is limited to a certain value.
	 * 
	 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
	 */
	static public class LimitedChannelInput
	implements ReadableByteChannel
	{

		private final ReadableByteChannel	in;

		private int							expected;

		public LimitedChannelInput( ReadableByteChannel in, int expected )
		{
			this.in = in;
			this.expected = expected;
		}

		@Override
		public void close() throws IOException
		{
			this.in.close();
		}

		@Override
		public boolean isOpen()
		{
			return this.in.isOpen();
		}

		@Override
		public int read( ByteBuffer dst ) throws IOException
		{
			if( this.expected == 0 ) {
				return 0;
			}

			final int remaining = dst.remaining();

			if( remaining > this.expected ) {
				dst.limit( dst.position() + this.expected );
			}

			final int read = this.in.read( dst );

			if( read > 0 ) {
				this.expected -= read;
			}

			return read;
		}
	}

	private final String		uri;

	final String				requestLine;

	private final InetAddress	remote;

	HttpRequest( ReadableByteChannel channel, String uri, String requestLine, InetAddress remote )
	{
		super( channel );

		this.uri = uri;
		this.requestLine = requestLine;
		this.remote = remote;
	}

	public InetAddress getRemoteAddress()
	{
		return this.remote;
	}

	public String getURI()
	{
		return this.uri;
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

}
