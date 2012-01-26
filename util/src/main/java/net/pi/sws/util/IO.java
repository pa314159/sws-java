
package net.pi.sws.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;

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

	public static void close( Socket sock )
	{
		try {
			sock.close();
		}
		catch( final IOException e ) {
			;
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

	static public void sendFile( File file, WritableByteChannel channel, Selector sel ) throws IOException
	{
		final RandomAccessFile r = new RandomAccessFile( file, "r" );
		final FileChannel fc = r.getChannel();

		long position = 0;
		long count = fc.size();

		while( count > 0 ) {

			if( sel != null ) {
				sel.select();
			}

			final long transfered = fc.transferTo( position, fc.size(), channel );

			if( transfered < 0 ) {
				throw new IOException( "Cannot send file " + file );
			}
			if( transfered == 0 ) {
				continue;
			}

			position += transfered;
			count -= transfered;
		}
	}

	private IO()
	{
	}
}
