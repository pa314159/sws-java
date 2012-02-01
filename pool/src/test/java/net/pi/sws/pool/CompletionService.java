
package net.pi.sws.pool;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import net.pi.sws.util.ValidReference;
import net.pi.sws.util.ValidReference.Type;

public class CompletionService
implements Service, ValidReference.Check<Throwable>
{

	private final Service					service;

	private final ValidReference<Throwable>	result	= new ValidReference<Throwable>( Type.NONE, this );

	private boolean							done;

	CompletionService( Service service )
	{
		this.service = service;
	}

	@Override
	public void accept( SocketChannel channel ) throws IOException
	{
		Throwable x = null;

		try {
			this.service.accept( channel );
		}
		catch( final IOException t ) {
			x = t;

			throw t;
		}
		catch( final RuntimeException t ) {
			x = t;

			throw t;
		}
		catch( final Throwable t ) {
			x = t;

			throw new IOException( t );
		}
		finally {
			this.done = true;

			this.result.set( x );
		}
	}

	public Throwable getResult() throws InterruptedException
	{
		return this.result.get();
	}

	@Override
	public boolean isValid( Throwable value )
	{
		return this.done;
	}
}
