
package net.pi.sws.maven;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import net.pi.sws.http.HttpServiceFactory;
import net.pi.sws.pool.ServerPool;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal run
 * @requiresDependencyResolution compile
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class RunMojo
extends AbstractMojo
{

	/**
	 * @parameter expression="${sws.root}"
	 */
	private File				root;

	/**
	 * @parameter expression="${sws.port}" default-value="8080"
	 */
	private int					port;

	/**
	 * @parameter expression="${sws.host}" default-value="127.0.0.1"
	 */
	private String				host;

	/**
	 * @parameter
	 */
	private Map<String, Object>	configuration;

	/**
	 * Project classpath.
	 * 
	 * @parameter default-value="${project.compileClasspathElements}"
	 * @required
	 * @readonly
	 */
	private List<String>		classpath;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		try {
			LogManager.getLogManager().reset();

			final Logger root = Logger.getLogger( "net.pi.sws" );

			root.addHandler( new MojoHandler( getLog() ) );

			LogManager.getLogManager().addLogger( root );

			final List<URL> clp = new ArrayList<URL>();

			for( final String element : this.classpath ) {
				clp.add( new File( element ).toURI().toURL() );
			}

			final ClassLoader cld = new URLClassLoader( clp.toArray( new URL[0] ), getClass().getClassLoader() );

			Thread.currentThread().setContextClassLoader( cld );

			if( (this.configuration == null) || this.configuration.isEmpty() ) {
				this.configuration = new HashMap<String, Object>();

				root.info( "No HTTP factory configured, will use service discovery" );

				// FS uses this
				this.configuration.put( "root", this.root );
			}

			final SocketAddress bind = new InetSocketAddress( this.host, this.port );
			final HttpServiceFactory fact = HttpServiceFactory.get( this.configuration );
			final ServerPool pool = new ServerPool( bind, fact );

			pool.start();

			synchronized( pool ) {
				pool.wait();
			}
		}
		catch( final IOException e ) {
			throw new MojoExecutionException( "Cannot start SWS", e );
		}
		catch( final InterruptedException e ) {
			throw new MojoExecutionException( "Cannot start SWS", e );
		}
	}
}
