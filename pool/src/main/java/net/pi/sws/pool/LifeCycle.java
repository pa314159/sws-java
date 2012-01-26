
package net.pi.sws.pool;

import java.io.IOException;

public interface LifeCycle
{

	void start() throws IOException;

	void stop( long timeout ) throws InterruptedException, IOException;
}
