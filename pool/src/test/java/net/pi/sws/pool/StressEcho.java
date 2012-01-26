
package net.pi.sws.pool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.pi.sws.util.IO;
import net.pi.sws.util.NamedThreadFactory;

public class StressEcho
extends AbstractServerTest
{

	class EchoTask
	implements Runnable
	{

		private final Socket			sock;

		private final PrintWriter		wr;

		private final BufferedReader	rd;

		EchoTask() throws IOException
		{
			this.sock = new Socket();
			this.sock.connect( StressEcho.this.pool.getAddress() );

			this.wr = new PrintWriter( new OutputStreamWriter( this.sock.getOutputStream(), "UTF-8" ) );
			this.rd = new BufferedReader( new InputStreamReader( this.sock.getInputStream(), "UTF-8" ) );
		}

		@Override
		public void run()
		{
			try {
				final long end = System.currentTimeMillis() + 5000;

				while( end > System.currentTimeMillis() ) {
					this.wr.println( "heloooooooooooooooooooooooooooooooo!" );
					this.wr.flush();
					this.rd.readLine();
				}
			}
			catch( final Exception e ) {
				e.printStackTrace();
			}
			finally {
				IO.close( this.rd );
				IO.close( this.wr );
				IO.close( this.sock );
			}
		}
	}

	static public void main( String[] args ) throws IOException, InterruptedException
	{
		final StressEcho stress = new StressEcho();

		stress.setUp();
		stress.run();
		stress.tearDown();
	}

	private ThreadPoolExecutor	exec;

	@Override
	public void setUp() throws IOException
	{
		super.setUp();

		this.exec = (ThreadPoolExecutor) Executors
			.newFixedThreadPool( 40, new NamedThreadFactory( "stress-%02d", true ) );
	}

	@Override
	public void tearDown() throws InterruptedException, IOException
	{
		this.exec.shutdown();
		this.exec.awaitTermination( 500, TimeUnit.MILLISECONDS );
		this.exec.shutdownNow();

		super.tearDown();
	}

	private void run() throws IOException, InterruptedException
	{
		for( int k = 0; k < (this.exec.getCorePoolSize() * 8); k++ ) {
			this.exec.submit( new EchoTask() );
		}

		Thread.sleep( 15000 );
	}
}
