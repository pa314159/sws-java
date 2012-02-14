
package net.pi.sws.pool;

import java.io.IOException;

/**
 * Just a simple lifecycle...
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public interface LifeCycle
{

	void start() throws IOException;

	void stop( long timeout );
}
