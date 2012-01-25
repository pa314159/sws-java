
package net.pi.sws.pool;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface Service
{

	void accept( SocketChannel channel ) throws IOException;
}
