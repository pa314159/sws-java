
package net.pi.sws.io;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Selector;

import javax.mail.internet.MimeUtility;

/**
 * Simple IO operations
 */
public final class IO
{

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
				continue;
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

		final String line = new String( data, 0, ofs, "ISO-8859-1" );

		try {
			return MimeUtility.decodeText( line );
		}
		catch( final UnsupportedEncodingException e ) {
			return line;
		}
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

	private IO()
	{
	}
}
