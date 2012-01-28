
package net.pi.sws.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;

/**
 * Use it instead of {@link java.nio.channels.Channels#newOutputStream(WritableByteChannel)}...
 */
public class ChannelOutputStream
extends OutputStream
{

	private final WritableByteChannel	channel;

	public ChannelOutputStream( WritableByteChannel channel ) throws IOException
	{
		this.channel = channel;
	}

	@Override
	public void close() throws IOException
	{
		this.channel.close();
	}

	@Override
	public void write( byte[] b, int offset, int size ) throws IOException
	{
		IO.writeAll( this.channel, b, offset, size );
	}

	@Override
	public void write( int b ) throws IOException
	{
		final byte[] data = new byte[1];

		write( data, 0, 1 );
	}
}
