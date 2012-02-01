
package net.pi.sws.http.fs;

import java.io.File;
import java.io.IOException;

import net.pi.sws.http.AbstractHttpServiceFactory;
import net.pi.sws.util.ExtLog;

public class HttpServiceFactory
extends AbstractHttpServiceFactory
{

	static final ExtLog	L	= ExtLog.get();

	private final File	root;

	public HttpServiceFactory( File root ) throws IOException
	{
		this.root = root.getCanonicalFile();

		L.info( "Serving files from %s", this.root );
	}

	public File getRoot()
	{
		return this.root;
	}

}
