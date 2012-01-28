
package net.pi.sws.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class SimpleHttpTest
extends AbstractHttpTest
{

	@Test( /* timeout = 30000 */)
	public void get() throws ClientProtocolException, IOException
	{
		final HttpClient client = new DefaultHttpClient();
		final InetSocketAddress sock = (InetSocketAddress) this.pool.getAddress();
		final HttpHost host = new HttpHost( sock.getHostName(), sock.getPort() );
		final HttpGet request = new HttpGet( "/GET.txt" );

		for( int k = 0; k < 5; k++ ) {
			dump( client.execute( host, request ) );
		}

		request.addHeader( HttpHeader.General.CONNECTION, "close" );
		dump( client.execute( host, request ) );

		client.getConnectionManager().shutdown();
	}

	@Test( /* timeout = 30000 */)
	public void head() throws ClientProtocolException, IOException
	{
		final HttpClient client = new DefaultHttpClient();
		final InetSocketAddress sock = (InetSocketAddress) this.pool.getAddress();
		final HttpHost host = new HttpHost( sock.getHostName(), sock.getPort() );
		final HttpHead request = new HttpHead( "/HEAD.txt" );

		for( int k = 0; k < 5; k++ ) {
			final HttpResponse response = client.execute( host, request );

			dump( response );
		}
		request.addHeader( HttpHeader.General.CONNECTION, "close" );
		dump( client.execute( host, request ) );

		client.getConnectionManager().shutdown();
	}

	private void dump( final HttpResponse response ) throws IOException
	{
		System.out.println( response.getStatusLine() );

		for( final Header h : response.getAllHeaders() ) {
			System.out.println( h );
		}

		final HttpEntity ent = response.getEntity();

		if( ent != null ) {
			final InputStream content = ent.getContent();
			final byte[] data = new byte[8192];
			int read = 0;

			while( (read = content.read( data )) > 0 ) {
				System.out.write( data, 0, read );
			}

			EntityUtils.consume( ent );
		}
	}
}
