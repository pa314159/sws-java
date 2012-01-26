
package net.pi.sws.pool;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CompletionService
implements Service
{

	private final Service	service;

	private final Lock		lock	= new ReentrantLock();

	private final Condition	cond	= this.lock.newCondition();

	private Throwable		result;

	CompletionService( Service service )
	{
		this.service = service;
	}

	@Override
	public void accept( SocketChannel channel ) throws IOException
	{
		this.lock.lock();

		try {
			this.service.accept( channel );
		}
		catch( final IOException t ) {
			this.result = t;

			throw t;
		}
		catch( final RuntimeException t ) {
			this.result = t;

			throw t;
		}
		catch( final Throwable t ) {
			this.result = t;

			throw new IOException( t );
		}
		finally {
			this.cond.signalAll();

			this.lock.unlock();
		}
	}

	public Throwable getResult() throws InterruptedException
	{
		this.lock.lock();

		try {
			this.cond.await();

			return this.result;
		}
		finally {
			this.lock.unlock();
		}
	}
}
