
package net.pi.sws.pool;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class DefaultServiceFactory
implements ServiceFactory
{

	@Override
	public Service create( SocketChannel chn ) throws IOException
	{
		return new LineService( "UTF-8", chn );
	}
}
