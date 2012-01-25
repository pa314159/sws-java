
package net.pi.sws.pool;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class ServiceFactoryTest
implements ServiceFactory
{

	static final byte[]			BAD_DATA	= { 0x33, 0x31, 0x34, (byte) 0xc2, (byte) 0x80 };

	private CompletionService	service;

	public Service create( SelectionKey sk ) throws IOException
	{
		return this.service;
	}

	@Test
	public void run() throws Exception
	{
		final InetSocketAddress a = new InetSocketAddress( 31415 );
		final ServerListener sl = new ServerListener( a, this );

		sl.start();

		final Socket sock = new Socket();

		sock.connect( a );

		final OutputStream os = sock.getOutputStream();

		for( int k = 0; k < BAD_DATA.length; k++ ) {
			os.write( BAD_DATA, 0, k + 1 );
			os.write( '\n' );
			os.flush();
		}

		os.close();

		Assert.assertNull( this.service.getResult() );
	}

	@Before
	public void setUp()
	{
		this.service = new CompletionService( new LineService( "UTF-8" ) );
	}
}
