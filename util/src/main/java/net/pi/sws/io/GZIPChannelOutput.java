
package net.pi.sws.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

/**
 * GZIP wrapper over a {@link WritableByteChannel}.
 * 
 * <p>
 * <i><b>NOTE</b> The code is an adaptation of {@link java.util.zip.GZIPOutputStream} by David Connelly.</i>
 * </p>
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class GZIPChannelOutput
extends DeflaterChannelOutput
{

	// GZIP header magic number.
	static private final int	GZIP_MAGIC		= 0x8b1f;

	// Trailer size in bytes.
	static private final int	TRAILER_SIZE	= 8;

	// GZIP member header.
	static private final byte[]	HEADER			= {
												//
		(byte) GZIP_MAGIC, // Magic number (short)
		(byte) (GZIP_MAGIC >> 8), // Magic number (short)
		Deflater.DEFLATED, // Compression method (CM)
		0, // Flags (FLG)
		0, // Modification time MTIME (int)
		0, // Modification time MTIME (int)
		0, // Modification time MTIME (int)
		0, // Modification time MTIME (int)
		0, // Extra flags (XFLG)
		0										// Operating system (OS)
												};

	// CRC-32 of uncompressed data.
	private final CRC32			crc				= new CRC32();

	public GZIPChannelOutput( WritableByteChannel out ) throws IOException
	{
		this( out, 512 );
	}

	public GZIPChannelOutput( WritableByteChannel out, int size ) throws IOException
	{
		super( out, new Deflater( Deflater.DEFAULT_COMPRESSION, true ), size );

		this.defInternal = true;

		writeHeader();

		this.crc.reset();
	}

	/**
	 * Finishes writing compressed data to the output stream without closing the underlying stream. Use this method when
	 * applying multiple filters in succession to the same output stream.
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
				int len = this.def.deflate( this.buf, 0, this.buf.length );
				if( this.def.finished() && (len <= (this.buf.length - TRAILER_SIZE)) ) {
					// last deflater buffer. Fit trailer at the end
					writeTrailer( this.buf, len );
					len = len + TRAILER_SIZE;
					IO.writeAll( this.out, this.buf, 0, len );
					return;
				}
				if( len > 0 ) {
					IO.writeAll( this.out, this.buf, 0, len );
				}
			}
			// if we can't fit the trailer at the end of the last
			// deflater buffer, we write it separately
			final byte[] trailer = new byte[TRAILER_SIZE];
			writeTrailer( trailer, 0 );
			IO.writeAll( this.out, trailer, 0, TRAILER_SIZE );
		}
	}

	@Override
	void setInput( ByteBuffer src )
	{
		if( src.hasArray() ) {
			this.def.setInput( src.array(), src.position(), src.remaining() );
			this.crc.update( src.array(), src.position(), src.remaining() );

			src.position( src.limit() );
		}
		else {
			final byte[] data = new byte[src.remaining()];

			src.get( data );

			this.def.setInput( data );
			this.crc.update( data );
		}
	}

	private void writeHeader() throws IOException
	{
		IO.writeAll( this.out, HEADER, 0, HEADER.length );
	}

	/*
	 * Writes integer in Intel byte order to a byte array, starting at a given offset.
	 */
	private void writeInt( int i, byte[] buf, int offset ) throws IOException
	{
		writeShort( i & 0xffff, buf, offset );
		writeShort( (i >> 16) & 0xffff, buf, offset + 2 );
	}

	/*
	 * Writes short integer in Intel byte order to a byte array, starting at a given offset
	 */
	private void writeShort( int s, byte[] buf, int offset ) throws IOException
	{
		buf[offset] = (byte) (s & 0xff);
		buf[offset + 1] = (byte) ((s >> 8) & 0xff);
	}

	/*
	 * Writes GZIP member trailer to a byte array, starting at a given offset.
	 */
	private void writeTrailer( byte[] buf, int offset ) throws IOException
	{
		writeInt( (int) this.crc.getValue(), buf, offset ); // CRC-32 of uncompr. data
		writeInt( this.def.getTotalIn(), buf, offset + 4 ); // Number of uncompr. bytes
	}
}
