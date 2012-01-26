
package net.pi.sws.http;

import java.io.IOException;

import org.junit.Before;

import net.pi.sws.pool.AbstractServerTest;

public abstract class AbstractHttpTest
extends AbstractServerTest
{

	@Override
	@Before
	public void setUp() throws IOException
	{
		this.fact = new HttpServiceFactory();

		super.setUp();
	}
}
