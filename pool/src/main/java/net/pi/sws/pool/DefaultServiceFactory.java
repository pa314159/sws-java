
package net.pi.sws.pool;

import java.io.IOException;
import java.nio.channels.SocketChannel;

class DefaultServiceFactory
implements ServiceFactory
{

	@Override
	public Service create( SocketChannel chn ) throws IOException
	{
		return new LineReaderService( "UTF-8", chn );
	}
}
