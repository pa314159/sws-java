
package net.pi.sws.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.SocketChannel;

import net.pi.sws.pool.Service;
import net.pi.sws.util.IO;

public class HttpService
implements Service
{

	private static final int	TIMEOUT	= 0;

	private final File			root;

	private final ChannelOutput	oc;

	private final ChannelInput	ic;

	HttpService( File root, SocketChannel channel ) throws IOException
	{
		this.root = root;

		channel.configureBlocking( false );

		this.oc = new ChannelOutput( channel, TIMEOUT );
		this.ic = new ChannelInput( channel, TIMEOUT );
	}

	@Override
	public void accept( SocketChannel channel ) throws IOException
	{
		try {
			final ChannelInputStream is = new ChannelInputStream( this.ic );
			final BufferedReader rd = new BufferedReader( new InputStreamReader( is, "ISO-8859-1" ) );
			final HttpMethod method = MethodFactory.getMethod( rd.readLine(), this.root );

			String head = null;

			while( (head = rd.readLine()) != null ) {
				head = head.trim();

				if( head.isEmpty() ) {
					break;
				}

				method.add( new HttpHeader( head ) );
			}

			method.forward( this.ic, this.oc );
		}
		finally {
			IO.close( this.ic );
			IO.close( this.oc );
		}
	}
}
