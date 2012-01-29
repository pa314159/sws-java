
package net.pi.sws.io;

import java.io.EOFException;
import java.io.Flushable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.zip.Deflater;

/**
 * Deflate wrapper over a {@link WritableByteChannel}.
 * 
 * <b>NOTE</b> The code is adapted from {@link java.util.zip.DeflaterOutputStream} by David Connelly.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class DeflaterChannelOutput
implements WritableByteChannel, Flushable
{

	final WritableByteChannel	out;

	final Deflater				def;

	final byte[]				buf;

	boolean						defInternal;

	public DeflaterChannelOutput( WritableByteChannel out )
	{
		this( out, new Deflater() );

		this.defInternal = true;
	}

	public DeflaterChannelOutput( WritableByteChannel out, Deflater def )
	{
		this( out, def, 512 );
	}

	public DeflaterChannelOutput( WritableByteChannel out, Deflater def, int size )
	{
		this.out = out;
		this.def = def;
		this.buf = new byte[size];
	}

	@Override
	public void close() throws IOException
	{
		flush();

		if( this.defInternal ) {
			this.def.end();
		}

		this.out.close();
	}

	/**
	 * Finishes writing compressed data to the output stream without closing the underlying stream. Use this method when
	 * applying multiple filters in succession to the same output channel.
	 * 
	 * @exception IOException
	 *                if an I/O error has occurred
	 */
	@Override
	public void flush() throws IOException
	{
		if( !this.def.finished() ) {
			this.def.finish();

			while( !this.def.finished() ) {
				deflate();
			}
		}
	}

	@Override
	public boolean isOpen()
	{
		return this.out.isOpen();
	}

	@Override
	public int write( ByteBuffer src ) throws IOException
	{
		if( this.def.finished() ) {
			throw new EOFException( "write beyond end of channel" );
		}

		final int sz = src.remaining();

		if( sz == 0 ) {
			return 0;
		}

		if( !this.def.finished() ) {
			setInput( src );

			while( !this.def.needsInput() ) {
				deflate();
			}
		}

		return sz;
	}

	void setInput( ByteBuffer src )
	{
		if( src.hasArray() ) {
			this.def.setInput( src.array(), src.position(), src.remaining() );

			src.position( src.limit() );
		}
		else {
			final byte[] data = new byte[src.remaining()];

			src.get( data );

			this.def.setInput( data );
		}
	}

	private void deflate() throws IOException
	{
		final int len = this.def.deflate( this.buf, 0, this.buf.length );

		if( len > 0 ) {
			IO.writeAll( this.out, this.buf, 0, len );
		}
	}
}
