
package net.pi.sws.http.methods;

import java.io.IOException;

import net.pi.sws.http.HttpCode;
import net.pi.sws.http.HttpMethod;

public class HeadMethod
extends HttpMethod
{

	public HeadMethod( String uri, String version )
	{
		super( uri, version );
	}

	@Override
	protected void execute() throws IOException
	{
		setStatus( HttpCode.S_OK );
	}

}
