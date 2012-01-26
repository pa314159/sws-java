
package net.pi.sws.echo;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import net.pi.sws.pool.Service;
import net.pi.sws.pool.ServiceFactory;

public final class EchoServiceFactory
implements ServiceFactory
{

	public Service create( SocketChannel chn ) throws IOException
	{
		return new EchoService( "UTF-8", chn );
	}

}
