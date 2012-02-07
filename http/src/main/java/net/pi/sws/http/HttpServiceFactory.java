
package net.pi.sws.http;

import java.beans.Introspector;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import net.pi.sws.pool.Service;
import net.pi.sws.pool.ServiceFactory;
import net.pi.sws.util.ExtLog;

/**
 * Factory for HTTP service.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class HttpServiceFactory
implements ServiceFactory
{

	static private final ExtLog						L		= ExtLog.get();

	static private final Class<HttpServiceFactory>	CLASS	= HttpServiceFactory.class;

	public static final String						DEFAULT	= CLASS.getName();

	static public final HttpServiceFactory get( Map<String, Object> configuration )
	{
		String sfName = (String) configuration.get( DEFAULT );

		if( sfName == null ) {
			sfName = System.getProperty( HttpServiceFactory.class.getName() );
		}

		HttpServiceFactory sf;

		if( sfName == null ) {
			sf = load();
		}
		else {
			sf = load( sfName );
		}

		if( sf == null ) {
			sf = new HttpServiceFactory();
		}

		configure( sf, configuration );

		return sf;
	}

	static private void configure( HttpServiceFactory sv, Map<String, Object> configuration )
	{
		for( Class<?> cls = sv.getClass(); cls != CLASS; cls = cls.getSuperclass() ) {
			final HashMap<String, Object> conf = new HashMap<String, Object>( configuration );

			for( final Method method : cls.getMethods() ) {
				if( Modifier.isAbstract( method.getModifiers() ) ) {
					continue;
				}

				if( !method.getName().startsWith( "set" ) ) {
					continue;
				}

				final String propertyName = Introspector.decapitalize( method.getName().substring( 3 ) );

				if( !conf.containsKey( propertyName ) ) {
					continue;
				}

				final Object value = conf.get( propertyName );

				try {
					method.invoke( sv, value );

					conf.remove( propertyName );
				}
				catch( final IllegalArgumentException e ) {
					L.warn( "Cannot set property %s from %s", e, propertyName, value );
				}
				catch( final IllegalAccessException e ) {
					L.warn( "Cannot set property %s from %s", e, propertyName, value );
				}
				catch( final InvocationTargetException e ) {
					L.warn( "Cannot set property %s from %s", e, propertyName, value );
				}
			}

			for( final Field field : cls.getDeclaredFields() ) {
				final String propertyName = field.getName();

				if( !conf.containsKey( propertyName ) ) {
					continue;
				}

				final Object value = conf.get( propertyName );

				field.setAccessible( true );

				try {
					field.set( sv, value );

					conf.remove( propertyName );
				}
				catch( final IllegalArgumentException e ) {
					L.warn( "Cannot assign field %s from %s", e, field, value );
				}
				catch( final IllegalAccessException e ) {
					L.warn( "Cannot assign field %s from %s", e, field, value );
				}
			}
		}
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

	public HttpServiceFactory()
	{
		ExtLog.get( getClass() ).info( "Starting %s", HttpResponse.SIGNATURE );

		// preload methods
		AnnotatedMethodFactory.getInstance();
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

	public MethodFactory<HttpServiceFactory> getMethodFactory()
	{
		return AnnotatedMethodFactory.getInstance();
	}
}
