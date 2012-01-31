
package net.pi.sws.pool;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Defines a service.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public interface Service
{

	void accept( SocketChannel channel ) throws IOException;
}
