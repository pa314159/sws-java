
package net.pi.sws.http;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;

import net.pi.sws.pool.Service;
import net.pi.sws.pool.ServiceFactory;
import net.pi.sws.util.ExtLog;

/**
 * Factory for HTTP service.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class HttpServiceFactory
implements ServiceFactory
{

	static final ExtLog	L	= ExtLog.get();

	static {
		L.info( "Starting SWS" );

		// preload methods
		MethodFactory.getInstance();
	}

	private final File	root;

	/**
	 * Creates an new instance of type <code>HttpServiceFactory</code>.
	 * 
	 * @param root
	 *            the root of the exposed file system.
	 * @throws IOException
	 */
	public HttpServiceFactory( File root ) throws IOException
	{
		this.root = root.getCanonicalFile();

		L.info( "Serving files from %s", this.root );
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see net.pi.sws.pool.ServiceFactory#create(java.nio.channels.SocketChannel)
	 */
	@Override
	public Service create( SocketChannel channel ) throws IOException
	{
		return new HttpService( this.root, channel );
	}

}
