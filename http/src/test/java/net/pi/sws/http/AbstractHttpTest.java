
package net.pi.sws.http;

import java.io.File;
import java.io.IOException;

import net.pi.sws.pool.AbstractServerTest;

import org.junit.Before;

public abstract class AbstractHttpTest
extends AbstractServerTest
{

	@Override
	@Before
	public void setUp() throws IOException
	{
		this.fact = new HttpServiceFactory( new File( "target" ) );

		super.setUp();
	}
}
