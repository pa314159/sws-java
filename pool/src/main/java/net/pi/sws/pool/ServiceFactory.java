
package net.pi.sws.pool;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface ServiceFactory
{

	Service create( SocketChannel chn ) throws IOException;
}
