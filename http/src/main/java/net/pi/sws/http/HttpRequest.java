
package net.pi.sws.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

import net.pi.sws.io.ChannelInputStream;

public class HttpRequest
extends HttpMessage<ReadableByteChannel, InputStream, Reader>
{

	private final String	uri;

	HttpRequest( ReadableByteChannel channel, String uri )
	{
		super( channel );

		this.uri = uri;
	}

	public String getURI()
	{
		return this.uri;
	}

	@Override
	InputStream newByteStream( ReadableByteChannel channel ) throws IOException
	{
		return new ChannelInputStream( channel );
	}

	@Override
	Reader newCharStream( InputStream stream, Charset cs )
	{
		return new InputStreamReader( stream, cs );
	}

}
