
package net.pi.sws.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * A reading channel implementation whose input is limited to a certain value.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class LimitedChannelInput
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
		consume();

		this.in.close();
	}

	public void consume() throws IOException
	{
		while( this.expected > 0 ) {
			final int z = Math.max( this.expected, 8192 );
			final ByteBuffer b = ByteBuffer.allocate( z );
			final int r = read( b );

			if( r < 0 ) {
				this.expected = 0;
			}
			else {
				this.expected -= r;
			}
		}
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
			return -1;
		}

		final int remaining = dst.remaining();

		if( remaining > this.expected ) {
			dst.limit( dst.position() + this.expected );
		}

		final int read = this.in.read( dst );

		if( read < 0 ) {
			this.expected = 0;
		}
		else {
			this.expected -= read;
		}

		return read;
	}
}
