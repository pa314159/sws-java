
package net.pi.sws.pool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

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
				e.printStackTrace();
			}
			catch( final IOException e ) {
				e.printStackTrace();
			}
		}
	}

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
				System.err.println( "Usage: java -jar sws-pool.jar host port" );
				System.exit( 1 );

				// probably unreachable :)
				return;
		}

		final DefaultServiceFactory fact = new DefaultServiceFactory();
		final SocketAddress bind = new InetSocketAddress( host, port );
		final ServerPool pool = new ServerPool( bind, fact );

		pool.start();

		Runtime.getRuntime().addShutdownHook( new ShutdownHook( pool ) );
	}
}
