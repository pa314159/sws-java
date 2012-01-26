
package net.pi.sws.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import net.pi.sws.util.IO;

public class SocketChannelInputStream
extends InputStream
{

	private final ReadableByteChannel	channel;

	private final int					timeout;

	private final Selector				sel;

	SocketChannelInputStream( SocketChannel channel, int timeout ) throws IOException
	{
		this.sel = Selector.open();

		channel.register( this.sel, SelectionKey.OP_READ );

		this.channel = channel;
		this.timeout = timeout;
	}

	@Override
	public void close() throws IOException
	{
		this.sel.close();

		super.close();
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
		if( len == 0 ) {
			return 0;
		}

		final ByteBuffer bb = ByteBuffer.wrap( b, off, len );

		IO.select( this.sel, this.timeout );

		return this.channel.read( bb );
	}
}
