
package net.pi.sws.http.methods;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import net.pi.sws.util.IO;

public class GetMethod
extends HeadMethod
{

	public GetMethod( File root, String uri, String version ) throws IOException
	{
		super( root, uri, version );
	}

	@Override
	protected void execute( File file ) throws IOException
	{
		if( file.isFile() ) {
			final RandomAccessFile r = new RandomAccessFile( file, "r" );
			final FileChannel fc = r.getChannel();

			fc.transferTo( 0, r.length(), getOutputChannel() );

			r.close();
		}
		else {
			//			final PrintWriter w = new PrintWriter( getOutputStream(), "UTF-8" );
		}
	}
}
