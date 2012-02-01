
package net.pi.sws.maven;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import net.pi.sws.http.fs.HttpServiceFactory;
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
	 * @required
	 */
	private File			root;

	/**
	 * @parameter expression="${sws.port}" default-value="8080"
	 */
	private int				port;

	/**
	 * @parameter expression="${sws.host}" default-value="127.0.0.1"
	 */
	private String			host;

	/**
	 * Project classpath.
	 * 
	 * @parameter default-value="${project.compileClasspathElements}"
	 * @required
	 * @readonly
	 */
	private List<String>	classpath;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		LogManager.getLogManager().reset();

		final Logger root = Logger.getLogger( "net.pi.sws" );

		root.setLevel( Level.ALL );
		root.addHandler( new MojoHandler( getLog() ) );

		LogManager.getLogManager().addLogger( root );

		final SocketAddress bind = new InetSocketAddress( this.host, this.port );

		try {
			final List<URL> clp = new ArrayList<URL>();

			for( final String element : this.classpath ) {
				clp.add( new File( element ).toURI().toURL() );
			}

			final ClassLoader cld = new URLClassLoader( clp.toArray( new URL[0] ), getClass().getClassLoader() );

			Thread.currentThread().setContextClassLoader( cld );

			final HttpServiceFactory fact = new HttpServiceFactory( this.root );
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
