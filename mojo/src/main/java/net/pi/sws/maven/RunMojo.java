
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
 * @requiresDependencyResolution test
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
	 * @parameter expression="${sws.factory}"
	 */
	private String				factory;

	/**
	 * @parameter expression="${sws.port}" default-value="8080"
	 */
	private int					port;

	/**
	 * @parameter expression="${sws.wait}" default-value="false"
	 */
	private boolean				wait;

	/**
	 * @parameter expression="${sws.host}" default-value="127.0.0.1"
	 */
	private String				host;

	/**
	 * @parameter
	 */
	private Map<String, Object>	properties;

	/**
	 * Project classpath.
	 * 
	 * @parameter default-value="${project.compileClasspathElements}"
	 * @required
	 * @readonly
	 */
	private List<String>		classpath;

	/**
	 * Project classpath.
	 * 
	 * @parameter default-value="${project.testClasspathElements}"
	 * @required
	 * @readonly
	 */
	private List<String>		testClasspath;

	public RunMojo()
	{
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException
	{

		try {
			LogManager.getLogManager().reset();

			final Logger logger = Logger.getLogger( "net.pi.sws" );

			logger.addHandler( new MojoHandler( getLog() ) );

			LogManager.getLogManager().addLogger( logger );

			final List<URL> clp = new ArrayList<URL>();

			for( final String element : this.testClasspath ) {
				clp.add( new File( element ).toURI().toURL() );
			}
			for( final String element : this.classpath ) {
				clp.add( new File( element ).toURI().toURL() );
			}

			final ClassLoader cld = new URLClassLoader( clp.toArray( new URL[0] ), getClass().getClassLoader() );

			Thread.currentThread().setContextClassLoader( cld );

			if( (this.properties == null) || this.properties.isEmpty() ) {
				this.properties = new HashMap<String, Object>();
			}

			if( this.factory != null ) {
				this.properties.put( HttpServiceFactory.DEFAULT, this.factory );
			}

			if( !this.properties.containsKey( "root" ) ) {
				this.properties.put( "root", this.root );
			}

			final HttpServiceFactory fact = HttpServiceFactory.get( this.properties );
			final SocketAddress bind = new InetSocketAddress( this.host, this.port );
			final ServerPool pool = new ServerPool( bind, fact );

			pool.start();

			final Thread hook = new Thread()
			{

				@Override
				public void run()
				{
					try {
						pool.stop( 500 );
					}
					catch( final InterruptedException e ) {
					}
					catch( final IOException e ) {
					}
					finally {
						synchronized( pool ) {
							pool.notify();
						}
					}
				}
			};

			Runtime.getRuntime().addShutdownHook( hook );

			if( this.wait ) {
				synchronized( pool ) {
					try {
						pool.wait();
					}
					catch( final InterruptedException e ) {
						;
					}
				}
			}
		}
		catch( final IOException e ) {
			throw new MojoExecutionException( "Cannot start SWS", e );
		}
	}
}
