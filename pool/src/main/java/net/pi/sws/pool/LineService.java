
package net.pi.sws.pool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import net.pi.sws.util.ExtLog;

class LineService
implements Service
{

	static private final ExtLog		L	= ExtLog.get();

	private final Charset			cs;

	private final BufferedReader	rd;

	public LineService( String encoding, SocketChannel channel ) throws IOException
	{
		this.cs = Charset.forName( encoding );

		channel.configureBlocking( true );

		final InputStream is = channel.socket().getInputStream();

		this.rd = new BufferedReader( new InputStreamReader( is, this.cs ) );
	}

	public void accept( SocketChannel channel ) throws IOException
	{
		String ln = null;

		while( (ln = this.rd.readLine()) != null ) {
			if( processLine( ln ) ) {
				break;
			}
		}

		L.info( "DONE" );
	}

	protected boolean processLine( String ln )
	{
		L.info( "GOT %s", ln );

		ln = ln.trim();

		return ln.isEmpty();
	}

}
