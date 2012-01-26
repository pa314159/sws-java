
package net.pi.sws.http;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;

import net.pi.sws.pool.Service;
import net.pi.sws.pool.ServiceFactory;

public class HttpServiceFactory
implements ServiceFactory
{

	private final File	root;

	HttpServiceFactory( File root )
	{
		this.root = root;
	}

	@Override
	public Service create( SocketChannel channel ) throws IOException
	{
		return new HttpService( this.root, channel );
	}

}
