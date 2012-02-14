
package net.pi.sws.dav;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import net.pi.sws.pool.ServerPool;
import net.pi.sws.util.ExtLog;

import com.bradmcevoy.http.AuthenticationService;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.http.fs.FileSystemResourceFactory;
import com.ettrema.http.fs.NullSecurityManager;

public class Main
{

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
				System.err.println( "Usage: SWS [host] port root" );
				System.exit( 1 );

				// probably unreachable :)
				return;
		}

		new Main( host, port, root ).go();
	}

	private final ServerPool	pool;

	private Main( String host, int port, File root ) throws IOException
	{
		final SocketAddress bind = new InetSocketAddress( host, port );
		final ResourceFactory rf = new FileSystemResourceFactory( root, new NullSecurityManager() );
		final AuthenticationService auth = new AuthenticationService();

		auth.setDisableBasic( true );
		auth.setDisableDigest( true );

		final HttpManager hm = new HttpManager( rf, auth );

		final DavServiceFactory fact = new DavServiceFactory( hm );

		this.pool = new ServerPool( bind, fact );
	}

	private void go() throws IOException
	{
		this.pool.start();
		this.pool.addShutdownHook();
	}

}
