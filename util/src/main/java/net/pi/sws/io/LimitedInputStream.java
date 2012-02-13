
package net.pi.sws.io;

import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream
extends InputStream
{

	private final InputStream	in;

	private long				expected;

	public LimitedInputStream( InputStream in, long expected )
	{
		this.in = in;
		this.expected = expected;
	}

	@Override
	public int available() throws IOException
	{
		return (int) this.expected;
	}

	@Override
	public void close() throws IOException
	{
		consume();

		this.in.close();
	}

	public void consume() throws IOException
	{
		final byte[] data = new byte[8192];

		while( this.expected > 0 ) {
			final int read = read( data, 0, data.length );

			if( read < 0 ) {
				this.expected = 0;
			}
			else {
				this.expected -= read;
			}
		}
	}

	@Override
	public int read() throws IOException
	{
		if( this.expected == 0 ) {
			return -1;
		}

		final int read = this.in.read();

		if( read < 0 ) {
			this.expected = 0;
		}
		else {
			this.expected--;
		}

		return read;
	}

	@Override
	public int read( byte[] b, int off, int len ) throws IOException
	{
		if( this.expected == 0 ) {
			return -1;
		}

		len = (int) Math.min( len, this.expected );
		final int read = this.in.read( b, off, len );

		if( read < 0 ) {
			this.expected = 0;
		}
		else {
			this.expected -= read;
		}

		return read;
	}

	@Override
	public long skip( long n ) throws IOException
	{
		return this.expected -= this.in.skip( n );
	}

}
