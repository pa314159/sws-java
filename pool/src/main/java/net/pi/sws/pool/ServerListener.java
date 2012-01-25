
package net.pi.sws.pool;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.pi.sws.util.ExtLog;

import org.apache.commons.io.IOUtils;

public class ServerListener
implements RejectedExecutionHandler, ThreadFactory
{

	class Handler
	implements Runnable
	{

		private final SelectionKey	sk;

		private final SocketChannel	chn;

		public Handler( SelectionKey sk, SocketChannel chn )
		{
			this.sk = sk;
			this.chn = chn;
		}

		public void run()
		{
			try {
				ServerListener.this.fact.create( this.sk ).accept( this.chn );
			}
			catch( final IOException e ) {
				e.printStackTrace();
			}
			finally {
				IOUtils.closeQuietly( this.chn );
			}
		}
	}

	static final ExtLog					L		= ExtLog.get();

	private final ThreadPoolExecutor	exec	= (ThreadPoolExecutor) Executors.newFixedThreadPool( 20, this );

	private final Selector				sel;

	private final ServerSocketChannel	chn;

	private final ServiceFactory		fact;

	private Thread						thread;

	private int							threads;

	public ServerListener( SocketAddress a, ServiceFactory fact ) throws IOException
	{
		this.sel = Selector.open();
		this.chn = ServerSocketChannel.open();

		this.chn.socket().bind( a, 0 );
		this.chn.configureBlocking( false );
		this.chn.register( this.sel, SelectionKey.OP_ACCEPT );

		this.fact = fact;

		this.exec.setRejectedExecutionHandler( this );
	}

	public int getCorePoolSize()
	{
		return this.exec.getCorePoolSize();
	}

	public long getKeepAliveTime( TimeUnit unit )
	{
		return this.exec.getKeepAliveTime( unit );
	}

	public int getMaximumPoolSize()
	{
		return this.exec.getMaximumPoolSize();
	}

	@Override
	public Thread newThread( Runnable r )
	{
		final Thread t = new Thread( r );

		t.setName( "sws-" + ++this.threads );
		t.setDaemon( true );

		return t;
	}

	public void rejectedExecution( Runnable r, ThreadPoolExecutor executor )
	{
		r.run();
	}

	public void setCorePoolSize( int corePoolSize )
	{
		this.exec.setCorePoolSize( corePoolSize );
	}

	public void setKeepAliveTime( long time, TimeUnit unit )
	{
		this.exec.setKeepAliveTime( time, unit );
	}

	public void setMaximumPoolSize( int maximumPoolSize )
	{
		this.exec.setMaximumPoolSize( maximumPoolSize );
	}

	public void start()
	{
		start( false );
	}

	public void start( boolean daemon )
	{
		if( this.thread != null ) {
			throw new IllegalStateException();
		}

		this.thread = new Thread( new Runnable()
		{

			public void run()
			{
				try {
					loop();
				}
				catch( final IOException e ) {
					e.printStackTrace();
				}
			}
		} );

		this.thread.setDaemon( daemon );
		this.thread.start();
	}

	public void stop()
	{
		if( this.thread == null ) {
			throw new IllegalStateException();
		}

		this.thread.interrupt();

		this.thread = null;
	}

	private void loop() throws IOException
	{
		L.info( "Starting loop" );

		while( !Thread.interrupted() ) {
			if( this.sel.select() > 0 ) {
				final Set<SelectionKey> selected = this.sel.selectedKeys();

				for( final SelectionKey k : selected ) {
					this.exec.execute( new Handler( k, this.chn.accept() ) );
				}
			}
		}

		L.info( "Loop exited" );
	}
}
