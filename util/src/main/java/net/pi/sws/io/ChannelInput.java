
package net.pi.sws.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Non blocking readable channel handling timeouts.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class ChannelInput
implements ReadableByteChannel
{

	private final ReadableByteChannel	channel;

	private final int					timeout;

	private final Selector				sel;

	public ChannelInput( SocketChannel channel, int timeout ) throws IOException
	{
		this.sel = Selector.open();

		channel.register( this.sel, SelectionKey.OP_READ );

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

	public int read( ByteBuffer dst ) throws IOException
	{
		IO.select( this.sel, this.timeout );

		return this.channel.read( dst );
	}
}
