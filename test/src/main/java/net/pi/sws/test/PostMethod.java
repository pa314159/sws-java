
package net.pi.sws.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Map;
import java.util.TreeMap;

import net.pi.sws.http.HTTP;
import net.pi.sws.http.HttpMethod;
import net.pi.sws.http.HttpRequest;
import net.pi.sws.http.HttpResponse;
import net.pi.sws.io.IO;
import net.pi.sws.util.ExtLog;

@HTTP( "POST" )
public class PostMethod
extends HttpMethod
{

	static private final ExtLog	L	= ExtLog.get();

	public PostMethod( HttpRequest request, HttpResponse response )
	{
		super( request, response );
	}

	@Override
	protected void respond() throws IOException
	{
		final InputStream is = this.request.getByteStream();
		final ByteArrayOutputStream os = new ByteArrayOutputStream();

		IO.copy( is, os );

		String post = new String( os.toByteArray(), IO.ISO_8859_1 );

		post = URLDecoder.decode( post, "UTF-8" );

		final Map<String, String> map = new TreeMap<String, String>();
		final String[] pairs = post.split( "&" );

		for( final String pair : pairs ) {
			final int ix = pair.indexOf( '=' );

			if( ix > 0 ) {
				map.put( pair.substring( 0, ix ), pair.substring( ix + 1 ) );
			}
			else {
				map.put( pair, "" );
			}
		}

		L.info( "%s", map );
	}

}
