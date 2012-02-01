
package net.pi.sws.http.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import net.pi.sws.http.HttpResponse;
import net.pi.sws.io.IO;
import net.pi.sws.util.ExtLog;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

/**
 * Velocity utility.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
class VeloUtil
implements LogChute
{

	static public class VeloRL
	extends ResourceLoader
	{

		@Override
		public long getLastModified( Resource resource )
		{
			return 0;
		}

		@Override
		public InputStream getResourceStream( String source ) throws ResourceNotFoundException
		{
			return VeloUtil.class.getResourceAsStream( source );
		}

		@Override
		public void init( ExtendedProperties configuration )
		{
		}

		@Override
		public boolean isSourceModified( Resource resource )
		{
			return false;
		}
	}

	static private final ExtLog		L	= ExtLog.get();

	private final VelocityEngine	ve	= new VelocityEngine();

	VeloUtil()
	{
		this.ve.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, this );
		this.ve.setProperty( RuntimeConstants.RESOURCE_LOADER, "velo" );
		this.ve.setProperty( "velo." + RuntimeConstants.RESOURCE_LOADER + ".class", VeloRL.class.getName() );

		this.ve.init();
	}

	@Override
	public void init( RuntimeServices rs ) throws Exception
	{
	}

	@Override
	public boolean isLevelEnabled( int level )
	{
		return L.isEnabled( ExtLog.Level.values()[level + 2] );
	}

	@Override
	public void log( int level, String message )
	{
		L.log( ExtLog.Level.values()[level + 2], "%s", message );
	}

	@Override
	public void log( int level, String message, Throwable t )
	{
		L.log( ExtLog.Level.values()[level + 2], "%s", t, message );
	}

	void merge( VelocityContext ctx, String source, HttpResponse response ) throws IOException
	{
		final Template vt = this.ve.getTemplate( source, "UTF-8" );
		final Writer out = response.getCharStream( IO.UTF8 );

		vt.merge( ctx, out );
	}
}
