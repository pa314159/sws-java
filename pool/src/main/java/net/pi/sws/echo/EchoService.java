
package net.pi.sws.echo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import net.pi.sws.pool.Service;
import net.pi.sws.util.ExtLog;

public class EchoService
implements Service
{

	static private final ExtLog		L	= ExtLog.get();

	private final Charset			cs;

	private final BufferedReader	rd;

	private final PrintWriter		wr;

	public EchoService( String encoding, SocketChannel channel ) throws IOException
	{
		this.cs = Charset.forName( encoding );

		channel.configureBlocking( true );

		final InputStream is = channel.socket().getInputStream();
		final OutputStream os = channel.socket().getOutputStream();

		this.rd = new BufferedReader( new InputStreamReader( is, this.cs ) );
		this.wr = new PrintWriter( new OutputStreamWriter( os, this.cs ) );
	}

	public void accept( SocketChannel channel ) throws IOException
	{
		String ln = null;

		while( (ln = this.rd.readLine()) != null ) {
			if( processLine( ln ) ) {
				break;
			}
		}

		this.rd.close();
		this.wr.close();

		L.info( "DONE" );
	}

	protected boolean processLine( String ln )
	{
		L.trace( "ECHO %s", ln );

		ln = ln.trim();

		this.wr.println( ln );
		this.wr.flush();

		return ln.isEmpty();
	}

}
