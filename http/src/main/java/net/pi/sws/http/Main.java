
package net.pi.sws.http;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import net.pi.sws.pool.ServerPool;
import net.pi.sws.util.ExtLog;

/**
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class Main
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

		File root;
		switch( args.length ) {
			case 3:
				host = args[0];
				port = Integer.parseInt( args[1] );
				root = new File( args[1] );
			break;

			case 2:
				host = "0.0.0.0";
				port = Integer.parseInt( args[0] );
				root = new File( args[1] );
			break;

			default:
				System.err.println( "Usage: java -jar sws-http.jar [host] port root" );
				System.exit( 1 );

				// probably unreachable :)
				return;
		}

		new Main( host, port, root ).go();
	}

	private final ServerPool	pool;

	private Main( String host, int port, File root ) throws IOException
	{
		// preload methods
		MethodFactory.getInstance();

		final SocketAddress bind = new InetSocketAddress( host, port );
		final HttpServiceFactory fact = new HttpServiceFactory( root );

		this.pool = new ServerPool( bind, fact );
	}

	private void go() throws IOException
	{
		this.pool.start();
	}
}
