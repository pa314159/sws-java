
package net.pi.sws.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

import net.pi.sws.util.IO;

/**
 * Non blocking writable channel handling timeouts.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
class ChannelOutput
implements WritableByteChannel
{

	private final WritableByteChannel	channel;

	private final int					timeout;

	private final Selector				sel;

	ChannelOutput( SocketChannel channel, int timeout ) throws IOException
	{
		this.sel = Selector.open();

		channel.register( this.sel, SelectionKey.OP_WRITE );

		this.channel = channel;
		this.timeout = timeout;
	}

	public void close() throws IOException
	{
		this.sel.close();
		this.channel.close();
	}

	public boolean isOpen()
	{
		return this.channel.isOpen();
	}

	public int write( ByteBuffer src ) throws IOException
	{
		int total = 0;

		while( src.remaining() > 0 ) {
			IO.select( this.sel, this.timeout );

			final int transferred = this.channel.write( src );

			if( transferred < 0 ) {
				throw new IOException();
			}

			total += transferred;
		}

		return total;
	}

}
