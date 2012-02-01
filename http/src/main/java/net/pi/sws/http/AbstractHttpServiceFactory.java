
package net.pi.sws.http;

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
public abstract class AbstractHttpServiceFactory
implements ServiceFactory
{

	static final ExtLog	L	= ExtLog.get();

	static {
		L.info( "Starting SWS" );

		// preload methods
		MethodFactory.getInstance();
	}

	/**
	 * Creates an new instance of type <code>AbstractHttpServiceFactory</code>.
	 * 
	 * @param root
	 *            the root of the exposed file system.
	 * @throws IOException
	 */
	public AbstractHttpServiceFactory() throws IOException
	{
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see net.pi.sws.pool.ServiceFactory#create(java.nio.channels.SocketChannel)
	 */
	@Override
	public Service create( SocketChannel channel ) throws IOException
	{
		return new HttpService( this, channel );
	}

}
