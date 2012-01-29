
package net.pi.sws.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

import net.pi.sws.io.ChannelInputStream;

public class HttpRequest
extends HttpMessage<ReadableByteChannel, InputStream, Reader>
{

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

	private final String	uri;

	HttpRequest( ReadableByteChannel channel, String uri )
	{
		super( channel );

		this.uri = uri;
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
