
package net.pi.sws.http;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import net.pi.sws.pool.Service;
import net.pi.sws.pool.ServiceFactory;

public class HttpServiceFactory
implements ServiceFactory
{

	@Override
	public Service create( SocketChannel channel ) throws IOException
	{
		return new HttpService( channel );
	}

}
