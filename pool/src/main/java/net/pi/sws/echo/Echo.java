
package net.pi.sws.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import net.pi.sws.pool.ServerPool;
import net.pi.sws.util.ExtLog;

/**
 * Main class for {@link EchoService}.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class Echo
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
		final EchoServiceFactory fact = new EchoServiceFactory();

		this.pool = new ServerPool( bind, fact );
	}

	private void go() throws IOException
	{
		this.pool.start();
	}
}
