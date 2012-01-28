
package net.pi.sws.io;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

/**
 * Simple IO operations
 */
public final class IO
{

	static public final Charset	ISO_8859_1	= Charset.forName( "ISO-8859-1" );

	static public final Charset	UTF8		= Charset.forName( "UTF-8" );

	static public void close( Closeable closeable )
	{
		try {
			closeable.close();
		}
		catch( final IOException e ) {
			;
		}
	}

	static public void close( Socket sock )
	{
		try {
			sock.close();
		}
		catch( final IOException e ) {
			;
		}
	}

	public static void copy( InputStream is, OutputStream os ) throws IOException
	{
		final byte[] data = new byte[8192];
		int read = 0;

		while( (read = is.read( data )) > 0 ) {
			os.write( data, 0, read );
		}
	}

	public static String pathOf( File root, File file )
	{
		final String path = file.getPath().substring( root.getPath().length() );

		return path.isEmpty() ? "/" : path;
	}

	static public String readLINE( ReadableByteChannel ic ) throws IOException
	{
		// an acceptable maximum to protect against very long lines,
		// is there any spec of this?
		final int MAX = 8192;

		byte[] data = new byte[128];
		int ofs = 0;

		while( ofs < data.length ) {
			final int read = ic.read( ByteBuffer.wrap( data, ofs, 1 ) );

			if( read < 0 ) {
				throw new EOFException( "expecting more data" );
			}
			if( read == 0 ) {
				break;
			}

			if( data[ofs] == '\n' ) {
				break;
			}

			if( ++ofs == data.length ) {
				if( ofs >= MAX ) {
					throw new IOException( "line too long" );
				}

				final byte[] temp = new byte[data.length * 2];

				System.arraycopy( data, 0, temp, 0, data.length );

				data = temp;
			}
		}

		if( ofs == 0 ) {
			return null;
		}

		if( data[ofs - 1] == '\r' ) {
			ofs--;
		}

		return new String( data, 0, ofs, ISO_8859_1 );
	}

	static public void select( Selector sel, long timeout ) throws IOException
	{
		if( timeout == 0 ) {
			sel.select();
		}
		else {
			final long end = System.currentTimeMillis() + timeout;

			while( sel.select( timeout ) == 0 ) {
				final long now = System.currentTimeMillis();

				if( now > end ) {
					throw new IOException( "select timeout" );
				}

				timeout = end - now;
			}
		}
	}

	static public int writeAll( WritableByteChannel out, byte[] data, int offset, int size ) throws IOException
	{
		final ByteBuffer bb = ByteBuffer.wrap( data, offset, size );

		while( size > 0 ) {
			final int transferred = out.write( bb );

			if( transferred == 0 ) {
				continue;
			}
			if( transferred < 0 ) {
				throw new EOFException();
			}

			size -= transferred;
			offset += transferred;
		}

		return size;
	}

	private IO()
	{
	}
}
