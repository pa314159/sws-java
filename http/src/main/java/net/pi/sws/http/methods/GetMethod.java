
package net.pi.sws.http.methods;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import net.pi.sws.http.HTTP;
import net.pi.sws.http.HttpRequest;
import net.pi.sws.http.HttpResponse;

/**
 * Implementation of GET.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
@HTTP( "GET" )
public class GetMethod
extends HeadMethod
{

	GetMethod( HttpRequest request, HttpResponse response ) throws IOException
	{
		super( request, response );
	}

	@Override
	protected void send( File file ) throws IOException
	{
		super.send( file );

		if( file.isFile() ) {
			final RandomAccessFile r = new RandomAccessFile( file, "r" );
			final FileChannel fc = r.getChannel();

			fc.transferTo( 0, r.length(), getResponse().getChannel() );

			r.close();
		}
		else {
			//			final PrintWriter w = new PrintWriter( getOutputStream(), "UTF-8" );
		}
	}
}
