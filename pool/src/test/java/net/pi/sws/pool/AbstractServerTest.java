
package net.pi.sws.pool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import net.pi.sws.util.ValidReference;

import org.junit.After;
import org.junit.Before;

public abstract class AbstractServerTest
implements ServiceFactory
{

	static final int									PORT	= 31416;

	protected final ValidReference<CompletionService>	service	= new ValidReference<CompletionService>();

	protected ServerPool								pool;

	public Service create( SocketChannel channel ) throws IOException
	{
		final CompletionService serv = new CompletionService( new LineService( "UTF-8", channel ) );

		this.service.set( serv );

		return serv;
	}

	@Before
	public void setUp() throws IOException
	{
		final InetSocketAddress a = new InetSocketAddress( 31416 );

		this.pool = new ServerPool( a, this );

		this.pool.start();
	}

	@After
	public void tearDown() throws InterruptedException, IOException
	{
		this.pool.stop( 500 );
	}
}
