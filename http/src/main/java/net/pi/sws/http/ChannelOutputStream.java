
package net.pi.sws.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * Use it instead of {@link java.nio.channels.Channels#newOutputStream(WritableByteChannel)}...
 */
class ChannelOutputStream
extends OutputStream
{

	private final WritableByteChannel	channel;

	ChannelOutputStream( WritableByteChannel channel ) throws IOException
	{
		this.channel = channel;
	}

	@Override
	public void close() throws IOException
	{
		this.channel.close();
	}

	@Override
	public void write( byte[] b, int off, int len ) throws IOException
	{
		final ByteBuffer bb = ByteBuffer.wrap( b, off, len );

		while( len > 0 ) {
			final int transferred = this.channel.write( bb );

			if( transferred == 0 ) {
				continue;
			}
			if( transferred < 0 ) {
				throw new IOException();
			}

			len -= transferred;
			off += transferred;
		}
	}

	@Override
	public void write( int b ) throws IOException
	{
		final byte[] data = new byte[1];

		write( data, 0, 1 );
	}
}
