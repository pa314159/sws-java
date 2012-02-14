
package net.pi.sws.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import net.pi.sws.pool.ServerPool;

/**
 * Main class for {@link EchoService}.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class Echo
{

	static public void main( String[] args ) throws IOException, InterruptedException
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

	private void go() throws IOException, InterruptedException
	{
		this.pool.start();
		this.pool.addShutdownHook();
		this.pool.join();
	}
}
