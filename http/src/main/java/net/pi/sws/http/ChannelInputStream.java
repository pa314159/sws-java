
package net.pi.sws.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Use it instead of {@link java.nio.channels.Channels#newInputStream(ReadableByteChannel)}...
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
class ChannelInputStream
extends InputStream
{

	private final ReadableByteChannel	channel;

	ChannelInputStream( ReadableByteChannel channel ) throws IOException
	{
		this.channel = channel;
	}

	@Override
	public void close() throws IOException
	{
		this.channel.close();
	}

	@Override
	public int read() throws IOException
	{
		final byte[] data = new byte[1];
		final int read = read( data, 0, 1 );

		return read == 1 ? data[0] & 0xFF : -1;
	}

	@Override
	public int read( byte[] b, int off, int len ) throws IOException
	{
		final ByteBuffer bb = ByteBuffer.wrap( b, off, len );

		return this.channel.read( bb );
	}
}
