
package net.pi.sws.http;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import net.pi.sws.pool.LifeCycle;
import net.pi.sws.pool.Service;
import net.pi.sws.pool.ServiceFactory;
import net.pi.sws.util.Configurator;
import net.pi.sws.util.ExtLog;

/**
 * Factory for HTTP service.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class HttpServiceFactory
implements ServiceFactory, LifeCycle
{

	static private final ExtLog						L		= ExtLog.get();

	static private final Class<HttpServiceFactory>	CLASS	= HttpServiceFactory.class;

	public static final String						DEFAULT	= CLASS.getName();

	static public final HttpServiceFactory get( Map<String, Object> configuration )
	{
		String sfName = (String) configuration.get( DEFAULT );

		if( sfName == null ) {
			sfName = System.getProperty( DEFAULT );
		}

		HttpServiceFactory sf;

		if( sfName == null ) {
			L.info( "No HTTP factory configured, using discovery" );

			sf = load();
		}
		else {
			sf = load( sfName );
		}

		if( sf == null ) {
			L.info( "Still no HTTP factory found, using default" );

			sf = new HttpServiceFactory();
		}

		try {
			configuration.remove( DEFAULT );

			final Configurator c = new Configurator( configuration );

			c.configure( sf );
		}
		catch( final Exception e ) {
			L.warn( "Cannot configure factory %s", e, sfName );
		}

		return sf;
	}

	static private HttpServiceFactory load()
	{
		final ServiceLoader<HttpServiceFactory> sld = ServiceLoader.load( CLASS );
		final Iterator<HttpServiceFactory> it = sld.iterator();

		if( it.hasNext() ) {
			final HttpServiceFactory sv = it.next();

			if( it.hasNext() ) {
				L.warn( "Multiple HTTP factories found, using %s. Override with -D%s", sv.getClass().getName(), DEFAULT );
			}

			return sv;
		}

		return null;
	}

	static private HttpServiceFactory load( String sfName )
	{
		final ClassLoader cld = Thread.currentThread().getContextClassLoader();

		try {
			return (HttpServiceFactory) Class.forName( sfName, true, cld ).newInstance();
		}
		catch( final InstantiationException e ) {
			throw new ServiceConfigurationError( "Cannot create service " + sfName, e );
		}
		catch( final IllegalAccessException e ) {
			throw new ServiceConfigurationError( "Cannot create service " + sfName, e );
		}
		catch( final ClassNotFoundException e ) {
			throw new ServiceConfigurationError( "Cannot create service " + sfName, e );
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see net.pi.sws.pool.ServiceFactory#create(java.nio.channels.SocketChannel)
	 */
	@Override
	public final Service create( SocketChannel channel ) throws IOException
	{
		return new HttpService( this, channel );
	}

	public MethodFactory<? extends HttpServiceFactory> getMethodFactory()
	{
		return AnnotatedMethodFactory.getInstance();
	}

	@Override
	public void start() throws IOException
	{
		L.info( "Starting %s", HttpResponse.SIGNATURE );

		// preload methods
		AnnotatedMethodFactory.getInstance();
	}

	@Override
	public void stop( long timeout )
	{
		L.info( "Stopping %s", HttpResponse.SIGNATURE );
	}
}
