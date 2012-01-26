
package net.pi.sws.http;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
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
		final HttpRequest get = new HttpGet( "/" );
		final InetSocketAddress sock = (InetSocketAddress) this.pool.getAddress();
		final HttpHost host = new HttpHost( sock.getHostName(), sock.getPort() );
		final HttpResponse resp = client.execute( host, get );

		EntityUtils.consume( resp.getEntity() );

		client.getConnectionManager().shutdown();
	}

	@Test( /* timeout = 30000 */)
	public void head() throws ClientProtocolException, IOException
	{
		final HttpClient client = new DefaultHttpClient();
		final HttpRequest get = new HttpHead( "/" );
		final InetSocketAddress sock = (InetSocketAddress) this.pool.getAddress();
		final HttpHost host = new HttpHost( sock.getHostName(), sock.getPort() );
		final HttpResponse resp = client.execute( host, get );

		EntityUtils.consume( resp.getEntity() );

		client.getConnectionManager().shutdown();
	}
}
