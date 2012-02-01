
package net.pi.sws.http;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.pi.sws.util.ClassPathScanner;
import net.pi.sws.util.ExtLog;

/**
 * HTTP method factory.
 * <p>
 * The method is chosen based on the request line. The implemented methods are found by scanning the classes and
 * choosing those annotated with &#64;{@link HTTP}.
 * </p>
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
@SuppressWarnings( { "rawtypes", "unchecked" } )
final class MethodFactory
implements ClassPathScanner.Visitor
{

	static private final ExtLog			L			= ExtLog.get();

	private static final MethodFactory	INSTANCE	= new MethodFactory();

	private static final Class[]		ARGUMENTS	= new Class[] { HttpServiceFactory.class, HttpRequest.class,
													HttpResponse.class };

	static {
		try {
			INSTANCE.scan();
		}
		catch( final IOException e ) {
			throw new ExceptionInInitializerError( e );
		}
	}

	static public MethodFactory getInstance()
	{
		return INSTANCE;
	}

	private final Map<String, Constructor<? extends HttpMethod>>	methods	= new HashMap<String, Constructor<? extends HttpMethod>>();

	private MethodFactory()
	{
	}

	public HttpMethod get( String met, HttpServiceFactory fact, HttpRequest request, HttpResponse response )
	throws IOException
	{
		final Constructor<? extends HttpMethod> ct = INSTANCE.methods.get( met );

		if( ct == null ) {
			return new BadMethod( request, response, HttpCode.NOT_IMPLEMENTED );
		}

		try {
			return ct.newInstance( request, response );
		}
		catch( final InstantiationException e ) {
			return new BadMethod( request, response, HttpCode.INTERNAL_ERROR );
		}
		catch( final IllegalAccessException e ) {
			return new BadMethod( request, response, HttpCode.INTERNAL_ERROR );
		}
		catch( final InvocationTargetException e ) {
			return new BadMethod( request, response, HttpCode.INTERNAL_ERROR );
		}
	}

	@Override
	public void visitClass( ClassLoader cld, String clsName ) throws IOException
	{
		try {
			final Class<? extends HttpMethod> cls = (Class<? extends HttpMethod>) cld.loadClass( clsName );
			final HTTP annotation = cls.getAnnotation( HTTP.class );

			if( annotation == null ) {
				throw new AssertionError( String.format( "The %s is not annotated with @HTTP", cls ) );
			}

			final String met = annotation.value().trim();

			if( met.isEmpty() ) {
				throw new IOException( String.format( "Invalid @HTTP value for %s", clsName ) );
			}

			final Constructor<? extends HttpMethod> ct = findCT( cls );

			ct.setAccessible( true );

			L.info( "%s is %s", met, clsName );

			this.methods.put( met, ct );
		}
		catch( final ClassNotFoundException e ) {
			throw new IOException( clsName, e );
		}
		catch( final NoSuchMethodException e ) {
			throw new IOException( clsName, e );
		}
	}

	private Constructor<? extends HttpMethod> findCT( final Class<? extends HttpMethod> cls )
	throws NoSuchMethodException
	{
		for( final Constructor ct : cls.getDeclaredConstructors() ) {
			final Class[] types = ct.getParameterTypes();

			if( types.length != ARGUMENTS.length ) {
				continue;
			}

			boolean matches = true;

			for( int k = 0; k < ARGUMENTS.length; k++ ) {
				final Class t = types[k];
				final Class e = ARGUMENTS[k];

				if( e.isAssignableFrom( t ) ) {
					continue;
				}

				matches = false;

				break;
			}

			if( matches ) {
				return ct;
			}
		}

		final StringBuilder b = new StringBuilder();

		b.append( cls.getName() );
		b.append( "#<init>( " );

		for( int k = 0; k < ARGUMENTS.length; k++ ) {
			if( k > 0 ) {
				b.append( ", " );
			}

			b.append( ARGUMENTS[k].getName() );
		}

		b.append( " )" );

		throw new NoSuchMethodException( b.toString() );
	}

	private void scan() throws IOException
	{
		final ClassPathScanner cps = new ClassPathScanner( this );

		cps.addPackage( "net.pi.sws." );
		cps.addAnnotation( HTTP.class );
		cps.scan();
	}

}
