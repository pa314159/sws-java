
package net.pi.sws.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

import net.pi.sws.util.IO;

public class SocketChannelOutputStream
extends OutputStream
{

	private final WritableByteChannel	channel;

	private final int					timeout;

	private final Selector				sel;

	SocketChannelOutputStream( SocketChannel channel, int timeout ) throws IOException
	{
		this.sel = Selector.open();

		channel.register( this.sel, SelectionKey.OP_WRITE );

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
	public void write( byte[] b, int off, int len ) throws IOException
	{
		final ByteBuffer bb = ByteBuffer.wrap( b, off, len );

		while( len > 0 ) {
			IO.select( this.sel, this.timeout );

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
