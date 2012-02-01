
package net.pi.sws.http.fs;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.TreeSet;

import net.pi.sws.http.HTTP;
import net.pi.sws.http.HttpRequest;
import net.pi.sws.http.HttpResponse;
import net.pi.sws.io.IO;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * Implementation of GET.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
@HTTP( "GET" )
public class GetMethod
extends HeadMethod
implements FileFilter
{

	static public class FileInfo
	implements Comparable<FileInfo>
	{

		private final String	name;

		private final String	path;

		private final long		size;

		FileInfo( File root, File file )
		{
			this.name = file.getName();
			this.path = IO.pathOf( root, file );
			this.size = file.length();
		}

		@Override
		public int compareTo( FileInfo o )
		{
			return this.name.compareTo( o.name );
		}

		public String getName()
		{
			return this.name;
		}

		public String getPath()
		{
			return this.path;
		}

		public long getSize()
		{
			return this.size;
		}
	}

	static private final VeloUtil		VELO	= new VeloUtil();

	private final Collection<FileInfo>	files	= new TreeSet<FileInfo>();

	private final Collection<FileInfo>	folders	= new TreeSet<FileInfo>();

	GetMethod( HttpServiceFactory fact, HttpRequest request, HttpResponse response )
	{
		super( fact, request, response );
	}

	@Override
	public boolean accept( File file )
	{
		final String name = file.getName();

		if( name.startsWith( "." ) ) {
			return false;
		}

		if( file.isFile() ) {
			this.files.add( new FileInfo( this.fact.getRoot(), file ) );
		}
		else {
			this.folders.add( new FileInfo( this.fact.getRoot(), file ) );
		}

		return false;
	}

	@Override
	void send( byte[] data ) throws IOException
	{
		super.send( data );

		this.response.getByteStream().write( data );
	}

	@Override
	void send( File file ) throws IOException
	{
		super.send( file );

		if( file.isFile() ) {
			sendFile( file );
		}
		else {
			listFiles( file );
		}
	}

	private void listFiles( File file ) throws ResourceNotFoundException, ParseErrorException,
	MethodInvocationException, IOException
	{
		final VelocityContext vc = new VelocityContext();

		file.listFiles( this );

		vc.put( "URI", this.request.getURI() );

		vc.put( "files", this.files );
		vc.put( "folders", this.folders );

		final File root = this.fact.getRoot();

		if( !file.equals( root ) ) {
			vc.put( "DOTDOT", IO.pathOf( root, file.getParentFile() ) );
		}

		VELO.merge( vc, "files.vm", this.response );
	}

	private void sendFile( File file ) throws FileNotFoundException, IOException
	{
		final RandomAccessFile r = new RandomAccessFile( file, "r" );
		final FileChannel fc = r.getChannel();

		fc.transferTo( 0, r.length(), this.response.getChannel() );

		r.close();
	}
}
