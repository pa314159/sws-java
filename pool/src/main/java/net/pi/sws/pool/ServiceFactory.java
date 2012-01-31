
package net.pi.sws.pool;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Creates a service for an accepted connection.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public interface ServiceFactory
{

	Service create( SocketChannel chn ) throws IOException;
}
