
package net.pi.sws.http;

import java.io.File;
import java.io.IOException;

import net.pi.sws.pool.AbstractServerTest;

import org.apache.http.HttpHost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractHttpTest
extends AbstractServerTest
{

	static DefaultHttpClient	client;

	static HttpHost				host;

	@BeforeClass
	static public void setUpClient()
	{
		client = new DefaultHttpClient();
		host = new HttpHost( address.getHostName(), address.getPort() );
	}

	@AfterClass
	static public void tearDownClient()
	{
		host = null;

		client.getConnectionManager().shutdown();

		client = null;
	}

	@Override
	@Before
	public void setUp() throws IOException
	{
		this.fact = new HttpServiceFactory( root() );

		super.setUp();
	}

	protected File root() throws IOException
	{
		return new File( "." );
	}
}
