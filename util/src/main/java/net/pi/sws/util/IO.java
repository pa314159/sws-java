
package net.pi.sws.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.Selector;

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

	private IO()
	{
	}
}
