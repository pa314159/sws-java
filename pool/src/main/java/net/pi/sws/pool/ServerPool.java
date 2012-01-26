
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

import net.pi.sws.util.ExtLog;
import net.pi.sws.util.NamedThreadFactory;

import org.apache.commons.io.IOUtils;

public class ServerPool
implements LifeCycle
{

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
			IOUtils.closeQuietly( this.chn );
		}

		void reject()
		{
			close();

			L.info( "REJECTED" );
		}
	}

	class Loop
	implements Runnable
	{

		@Override
		public void run()
		{
			L.info( "Starting loop" );

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

		// XXX review this
		this.exec = new ThreadPoolExecutor( 20, 20, 0L, TimeUnit.MILLISECONDS,
			new ArrayBlockingQueue<Runnable>( 20 ), tf, new RejectPolicy() );
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

		chn.socket().setReuseAddress( true );
		chn.socket().bind( address );
		chn.configureBlocking( false );
		chn.register( this.sel, SelectionKey.OP_ACCEPT );
	}

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

	public synchronized void start()
	{
		if( this.loop != null ) {
			throw new IllegalStateException();
		}

		L.info( "Starting SWS" );

		this.loop = new Loop();

		this.exec.execute( this.loop );
	}

	public synchronized void stop( long timeout ) throws InterruptedException, IOException
	{
		if( this.loop == null ) {
			throw new IllegalStateException();
		}

		L.info( "Stopping SWS" );

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
