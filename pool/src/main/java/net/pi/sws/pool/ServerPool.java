
package net.pi.sws.pool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.pi.sws.util.ExtLog;

import org.apache.commons.io.IOUtils;

public class ServerPool
implements LifeCycle
{

	class Handler
	implements Runnable
	{

		private final SocketChannel	chn;

		public Handler( SocketChannel chn ) throws IOException
		{
			this.chn = chn;
		}

		public void run()
		{

			try {
				this.chn.configureBlocking( false );
				this.chn.socket().setTcpNoDelay( true );

				ServerPool.this.fact.create( this.chn ).accept( this.chn );
			}
			catch( final Throwable t ) {
				L.error( "error in accept handler", t );
			}
			finally {
				IOUtils.closeQuietly( this.chn );

				L.info( "accept finished" );
			}
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

	static final ExtLog					L	= ExtLog.get();

	private final ThreadPoolExecutor	exec;

	private final Selector				sel;

	private final ServerSocketChannel	chn;

	private final ServiceFactory		fact;

	private Loop						loop;

	public ServerPool( SocketAddress a, ServiceFactory fact ) throws IOException
	{
		this.sel = Selector.open();

		//
		this.chn = ServerSocketChannel.open();

		this.chn.socket().setReuseAddress( true );
		this.chn.socket().bind( a );
		this.chn.configureBlocking( false );
		this.chn.register( this.sel, SelectionKey.OP_ACCEPT );

		//
		this.fact = fact;

		//
		final NamedThreadFactory tf = new NamedThreadFactory( "sws-%02d", false );

		this.exec = (ThreadPoolExecutor) Executors.newFixedThreadPool( 20, tf );

		this.exec.setRejectedExecutionHandler( new CloseExecutionHandler() );
	}

	public InetSocketAddress getAddress()
	{
		return (InetSocketAddress) this.chn.socket().getLocalSocketAddress();
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

		this.chn.close();
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
				it.next();
				it.remove();

				this.exec.execute( new Handler( this.chn.accept() ) );
			}
		}
	}
}
