
package net.pi.sws.http.fs;

import java.io.File;
import java.io.IOException;

import net.pi.sws.http.HttpServiceFactory;
import net.pi.sws.util.ExtLog;

/**
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class FsHttpServiceFactory
extends HttpServiceFactory
{

	static final ExtLog	L	= ExtLog.get();

	private File		root;

	public FsHttpServiceFactory()
	{
	}

	/**
	 * Creates an new instance of type <code>HttpServiceFactory</code>.
	 * 
	 * @param root
	 *            the root of the exposed file system.
	 * @throws IOException
	 */
	public FsHttpServiceFactory( File root ) throws IOException
	{
		setRoot( root );
	}

	public File getRoot()
	{
		if( this.root == null ) {
			throw new IllegalStateException( "No root has been set" );
		}

		return this.root;
	}

	public void setRoot( File root ) throws IOException
	{
		this.root = root.getCanonicalFile();

		L.info( "Serving files from %s", this.root );
	}

}
