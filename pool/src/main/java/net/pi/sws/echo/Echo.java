
package net.pi.sws.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import net.pi.sws.pool.ServerPool;
import net.pi.sws.pool.Service;
import net.pi.sws.pool.ServiceFactory;
import net.pi.sws.util.ExtLog;

public class Echo
implements ServiceFactory
{

	static class ShutdownHook
	extends Thread
	{

		private final ServerPool	pool;

		ShutdownHook( ServerPool pool )
		{
			this.pool = pool;
		}

		@Override
		public void run()
		{
			try {
				this.pool.stop( 500 );
			}
			catch( final InterruptedException e ) {
				L.error( "stop hook", e );
			}
			catch( final IOException e ) {
				L.error( "stop hook", e );
			}
		}
	}

	static private final ExtLog	L	= ExtLog.get();

	static public void main( String[] args ) throws IOException
	{
		if( args.length != 2 ) {
		}

		final String host;
		final int port;

		switch( args.length ) {
			case 2:
				host = args[0];
				port = Integer.parseInt( args[1] );
			break;

			case 1:
				host = "0.0.0.0";
				port = Integer.parseInt( args[0] );
			break;

			default:
				System.err.println( "Usage: java -jar sws-pool.jar [host] port" );
				System.exit( 1 );

				// probably unreachable :)
				return;
		}

		new Echo( host, port ).go();
	}

	private final ServerPool	pool;

	private Echo( String host, int port ) throws IOException
	{
		final SocketAddress bind = new InetSocketAddress( host, port );

		this.pool = new ServerPool( bind, this );
	}

	public Service create( SocketChannel chn ) throws IOException
	{
		return new EchoService( "UTF-8", chn );
	}

	private void go() throws IOException
	{
		this.pool.start();
	}
}
