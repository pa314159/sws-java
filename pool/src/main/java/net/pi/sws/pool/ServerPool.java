
package net.pi.sws.pool;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.pi.sws.io.IO;
import net.pi.sws.util.ExtLog;
import net.pi.sws.util.NamedThreadFactory;

/**
 * A generic channel based TCP server, listening to a port and spawning accept handlers with each incoming request.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class ServerPool
implements LifeCycle
{

	/**
	 * The accept handler. Upon each incoming request, the server "accepts" the request and configure the channel in
	 * non-blocking mode..
	 * 
	 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
	 */
	class Handler
	implements Runnable
	{

		private final SocketChannel	chn;

		public Handler( SelectionKey sk ) throws IOException
		{
			this.chn = ((ServerSocketChannel) sk.channel()).accept();
		}

		public void run()
		{
			L.info( "Incoming connection from %s", this.chn.socket().getInetAddress() );

			try {
				this.chn.configureBlocking( false );
				this.chn.socket().setTcpNoDelay( true );

				ServerPool.this.fact.create( this.chn ).accept( this.chn );
			}
			catch( final ClosedChannelException e ) {
				L.info( "accept closed" );
			}
			catch( final Throwable t ) {
				L.error( "error in accept handler", t );
			}
			finally {
				close();

				L.info( "accept finished" );
			}
		}

		void close()
		{
			IO.close( this.chn );
		}

		void reject()
		{
			close();

			L.info( "REJECTED" );
		}
	}

	/**
	 * This is the listener loop, it uses one thread from the thread pool.
	 * 
	 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
	 */
	class Loop
	implements Runnable
	{

		@Override
		public void run()
		{
			Thread.currentThread().setName( "loop" );

			L.info( "Entering loop" );

			try {
				loop();
			}
			catch( final IOException e ) {
				L.error( "error in loop", e );
			}
			finally {
				L.info( "Loop exited" );
			}
		}
	}

	/**
	 * Rejection policy for thread pool, it simply closes the connection.
	 * 
	 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
	 */
	static class RejectPolicy
	implements RejectedExecutionHandler
	{

		@Override
		public void rejectedExecution( Runnable r, ThreadPoolExecutor executor )
		{
			if( r instanceof Handler ) {
				((Handler) r).reject();
			}
		}
	}

	static final ExtLog					L	= ExtLog.get();

	private final ThreadPoolExecutor	exec;

	private final Selector				sel;

	private final ServiceFactory		fact;

	private Loop						loop;

	private final SocketAddress			address;

	public ServerPool( SocketAddress a, ServiceFactory fact ) throws IOException
	{
		this.address = a;
		this.sel = Selector.open();

		bind( a );

		this.fact = fact;

		final NamedThreadFactory tf = new NamedThreadFactory( "sws-%02d", false );

		// TODO review this
		final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>( 20 );
		final RejectPolicy reject = new RejectPolicy();

		this.exec = new ThreadPoolExecutor( 20, 20, 0L, TimeUnit.MILLISECONDS, queue, tf, reject );
	}

	/**
	 * Binds the primary and additional socket address.
	 * 
	 * @param address
	 * @throws IOException
	 */
	public void bind( SocketAddress address ) throws IOException
	{
		final ServerSocketChannel chn = ServerSocketChannel.open();

		L.info( "Binding to %s", address );

		chn.socket().setReuseAddress( true );
		chn.socket().bind( address );

		chn.configureBlocking( false );
		chn.register( this.sel, SelectionKey.OP_ACCEPT );
	}

	/**
	 * Returns is the primary listening address of the server.
	 */
	public SocketAddress getAddress()
	{
		return this.address;
	}

	public long getKeepAliveTime( TimeUnit unit )
	{
		return this.exec.getKeepAliveTime( unit );
	}

	public int getPoolSize()
	{
		return this.exec.getCorePoolSize();
	}

	public void setKeepAliveTime( long time, TimeUnit unit )
	{
		this.exec.setKeepAliveTime( time, unit );
	}

	public void setPoolSize( int poolSize )
	{
		this.exec.setCorePoolSize( poolSize + 1 );
		this.exec.setMaximumPoolSize( poolSize + 1 );
	}

	/**
	 * Starts the server loop.
	 * 
	 * @see net.pi.sws.pool.LifeCycle#start()
	 */
	public synchronized void start()
	{
		if( this.loop != null ) {
			throw new IllegalStateException();
		}

		L.info( "Starting pool" );

		this.loop = new Loop();

		this.exec.execute( this.loop );
	}

	/**
	 * Stops the server loop by sending a shutdown message to the thread pool.
	 * 
	 * @see net.pi.sws.pool.LifeCycle#stop(long)
	 */
	public synchronized void stop( long timeout ) throws InterruptedException, IOException
	{
		if( this.loop == null ) {
			throw new IllegalStateException();
		}

		L.info( "Stopping pool" );

		this.exec.shutdown();

		for( final SelectionKey sk : this.sel.keys() ) {
			sk.channel().close();
		}

		this.sel.close();

		this.exec.awaitTermination( timeout, TimeUnit.MILLISECONDS );
		this.exec.shutdownNow();

		L.info( "Stopped" );
	}

	void loop() throws IOException
	{
		while( !Thread.interrupted() ) {
			try {
				if( this.sel.select() == 0 ) {
					continue;
				}
			}
			catch( final ClosedSelectorException e ) {
				break;
			}

			final Iterator<SelectionKey> it = this.sel.selectedKeys().iterator();

			while( it.hasNext() ) {
				final SelectionKey sk = it.next();

				it.remove();

				this.exec.execute( new Handler( sk ) );
			}
		}
	}
}
