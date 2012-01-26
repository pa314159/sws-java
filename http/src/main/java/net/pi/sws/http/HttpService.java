
package net.pi.sws.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.SocketChannel;

import net.pi.sws.pool.Service;
import net.pi.sws.util.IO;

public class HttpService
implements Service
{

	private final BufferedInputStream	is;

	private final BufferedOutputStream	os;

	HttpService( SocketChannel channel ) throws IOException
	{
		channel.configureBlocking( false );

		this.is = new BufferedInputStream( new SocketChannelInputStream( channel, 0 ), 8192 );
		this.os = new BufferedOutputStream( new SocketChannelOutputStream( channel, 0 ) );
	}

	@Override
	public void accept( SocketChannel channel ) throws IOException
	{
		final BufferedReader rd = new BufferedReader( new InputStreamReader( this.is, "ISO-8859-1" ) );

		try {
			final HttpMethod method = MethodFactory.getMethod( rd.readLine() );

			String head = null;

			while( (head = rd.readLine()) != null ) {
				head = head.trim();

				if( head.isEmpty() ) {
					break;
				}

				method.add( new HttpHeader( head ) );
			}

			method.forward( this.is, this.os );
		}
		finally {
			IO.close( this.is );
			IO.close( this.os );
		}
	}
}
