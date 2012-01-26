
package net.pi.sws.http.methods;

import java.io.File;
import java.io.IOException;

import net.pi.sws.http.HttpCode;
import net.pi.sws.http.HttpHeader;
import net.pi.sws.http.HttpMethod;

public class HeadMethod
extends HttpMethod
{

	public HeadMethod( File root, String uri, String version ) throws IOException
	{
		super( root, uri, version );
	}

	@Override
	protected final void execute() throws IOException
	{
		final File file = new File( this.root, getURI() );

		if( !file.exists() ) {
			setStatus( HttpCode.S_NOT_FOUND );
		}
		else {
			execute( file );
		}
	}

	protected void execute( File file ) throws IOException
	{
		if( file.isDirectory() ) {
			addHeader( new HttpHeader( "Content-Type", "text/html; charset=UTF-8" ) );
		}
		else {
			addHeader( new HttpHeader( "Content-Type", "application/octet-stream" ) );
			addHeader( new HttpHeader( "Content-Length", Long.toString( file.length() ) ) );
		}
	}

}
