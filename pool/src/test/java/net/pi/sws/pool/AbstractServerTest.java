
package net.pi.sws.pool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import net.pi.sws.echo.EchoServiceFactory;
import net.pi.sws.util.ValidReference;

import org.junit.After;
import org.junit.Before;

public abstract class AbstractServerTest
implements ServiceFactory
{

	static protected final InetSocketAddress			address	= new InetSocketAddress( 31416 );

	protected final ValidReference<CompletionService>	service	= new ValidReference<CompletionService>();

	protected ServerPool								pool;

	private ServiceFactory								fact;

	public Service create( SocketChannel channel ) throws IOException
	{
		final CompletionService serv = new CompletionService( this.fact.create( channel ) );

		this.service.set( serv );

		return serv;
	}

	@Before
	public void setUp() throws IOException
	{
		this.fact = factory();
		this.pool = new ServerPool( address, this, provider() );

		this.pool.start();
	}

	@After
	public void tearDown() throws InterruptedException, IOException
	{
		if( this.pool != null ) {
			this.pool.stop( 500 );
		}

		this.pool = null;
		this.fact = null;
	}

	protected ServiceFactory factory() throws IOException
	{
		return new EchoServiceFactory();
	}

	protected SelectorProvider provider() throws IOException
	{
		return SelectorProvider.provider();
	}
}
