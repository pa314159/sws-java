
package net.pi.sws.echo;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import net.pi.sws.pool.Service;
import net.pi.sws.pool.ServiceFactory;

/**
 * Factory for {@link EchoService}.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public final class EchoServiceFactory
implements ServiceFactory
{

	public Service create( SocketChannel chn ) throws IOException
	{
		return new EchoService( "UTF-8", chn );
	}

}
