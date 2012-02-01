
package net.pi.sws.pool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import junit.framework.Assert;

import org.junit.Test;

public class SimpleListenerTest
extends AbstractServerTest
{

	static private final byte[]	BAD_UTF	= { 0x33, 0x31, 0x34, (byte) 0xc2, (byte) 0x80 };

	@Test
	public void run() throws IOException, InterruptedException
	{
		final Socket sock = new Socket();

		sock.connect( this.pool.getAddress() );

		final OutputStream os = sock.getOutputStream();
		final BufferedReader rd = new BufferedReader( new InputStreamReader( sock.getInputStream(), "UTF-8" ) );

		for( int k = 0; k < BAD_UTF.length; k++ ) {
			os.write( BAD_UTF, 0, k + 1 );
			os.write( '\n' );
			os.flush();

			System.out.printf( "BACK: %s\n", rd.readLine() );
		}

		os.close();

		try {
			final Throwable result = this.service.get().getResult();

			if( result != null ) {
				result.printStackTrace();
			}

			Assert.assertNull( result );
		}
		catch( final InterruptedException e ) {
			e.printStackTrace();

			Assert.fail( e.getMessage() );
		}
	}
}
