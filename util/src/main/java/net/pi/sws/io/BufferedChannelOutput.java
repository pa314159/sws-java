
package net.pi.sws.io;

import java.io.Flushable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * A buffered version of a channel. The channel keeps the data internally and expand it as needed. To actually write the
 * data to the wrapped channel one must call {@link #flush()}.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class BufferedChannelOutput
implements WritableByteChannel, Flushable
{

	private final int					iniz;

	private byte[]						data	= new byte[8192];

	private int							offset;

	private final WritableByteChannel	out;

	public BufferedChannelOutput( WritableByteChannel out )
	{
		this( out, 8192 );
	}

	public BufferedChannelOutput( WritableByteChannel out, int sz )
	{
		this.out = out;
		this.data = new byte[sz];
		this.iniz = sz;
	}

	@Override
	public void close() throws IOException
	{
		this.out.close();
	}

	@Override
	public void flush() throws IOException
	{
		new ChannelOutputStream( this.out ).write( this.data, 0, this.offset );

		this.data = new byte[this.iniz];
		this.offset = 0;
	}

	@Override
	public boolean isOpen()
	{
		return this.out.isOpen();
	}

	@Override
	public int write( ByteBuffer src ) throws IOException
	{
		final int sz = src.remaining();

		if( (this.offset + sz) > this.data.length ) {
			final byte[] temp = new byte[this.data.length + Math.max( this.iniz, this.data.length )];

			System.arraycopy( this.data, 0, temp, this.offset, this.data.length );

			this.data = temp;
		}

		ByteBuffer.wrap( this.data, this.offset, this.data.length - this.offset ).put( src );

		this.offset += sz;

		return 0;
	}
}
