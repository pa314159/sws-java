
package net.pi.sws.pool;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import junit.framework.Assert;

import org.junit.Test;

public class SimpleListenerTest
extends AbstractServerTest
{

	static private final byte[]	BAD_DATA	= { 0x33, 0x31, 0x34, (byte) 0xc2, (byte) 0x80 };

	@Test
	public void run() throws IOException, InterruptedException
	{
		final Socket sock = new Socket();

		sock.connect( this.pool.getAddress() );

		final OutputStream os = sock.getOutputStream();

		for( int k = 0; k < BAD_DATA.length; k++ ) {
			os.write( BAD_DATA, 0, k + 1 );
			os.write( '\n' );
			os.flush();
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
