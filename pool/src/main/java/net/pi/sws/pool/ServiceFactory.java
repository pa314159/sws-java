
package net.pi.sws.pool;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface ServiceFactory
{

	Service create( SelectionKey sk ) throws IOException;
}
