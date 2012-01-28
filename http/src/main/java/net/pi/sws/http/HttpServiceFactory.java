
package net.pi.sws.http;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;

import net.pi.sws.pool.Service;
import net.pi.sws.pool.ServiceFactory;
import net.pi.sws.util.ExtLog;

/**
 * Factory of HTTP service.
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

	public HttpServiceFactory( File root )
	{

		this.root = root;
	}

	@Override
	public Service create( SocketChannel channel ) throws IOException
	{
		return new HttpService( this.root, channel );
	}

}
