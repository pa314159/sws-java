
package net.pi.sws.pool;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Definition of a service.
 * 
 * <p>
 * The socket channel is passed to both {@link Service#accept(SocketChannel)} and
 * {@link ServiceFactory#create(SocketChannel)} methods.
 * </p>
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public interface Service
{

	void accept( SocketChannel channel ) throws IOException;
}
