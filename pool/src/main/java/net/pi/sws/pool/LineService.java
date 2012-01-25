
package net.pi.sws.pool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import net.pi.sws.util.ExtLog;

public class LineService
implements Service
{

	static private final ExtLog	L	= ExtLog.get();

	private final Charset		cs;

	public LineService( String encoding )
	{
		this.cs = Charset.forName( encoding );
	}

	public void accept( SocketChannel channel ) throws IOException
	{
		final InputStream is = channel.socket().getInputStream();
		final BufferedReader rd = new BufferedReader( new InputStreamReader( is, this.cs ) );
		String ln = null;

		while( (ln = rd.readLine()) != null ) {
			processLine( ln );
		}
	}

	protected void processLine( String ln )
	{
		L.info( "GOT %s", ln );
	}

}
