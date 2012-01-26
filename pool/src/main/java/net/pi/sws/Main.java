
package net.pi.sws;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import net.pi.sws.pool.DefaultServiceFactory;
import net.pi.sws.pool.ServerPool;

public class Main
{

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

		new ServerPool( bind, fact ).start();
	}
}
