
package net.pi.sws.http;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.pi.sws.util.ClassPathScanner;
import net.pi.sws.util.ExtLog;

/**
 * HTTP method factory. The method is chosen based on the request line. The implemented methods are found by scanning
 * the classes and choosing those annotated with &#64;{@link HTTP}.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
final class MethodFactory
implements ClassPathScanner.Visitor
{

	static private final ExtLog			L			= ExtLog.get();

	private static final MethodFactory	INSTANCE	= new MethodFactory();

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

	public HttpMethod get( String head, File root ) throws IOException
	{
		final String[] parts = head.split( "\\s+" );

		if( parts.length != 3 ) {
			throw new IOException( "Protocol error: " + head );
		}

		final Constructor<? extends HttpMethod> ct = INSTANCE.methods.get( parts[0] );

		if( ct == null ) {
			return new BadMethod( root, head );
		}

		try {
			return ct.newInstance( root, parts[1], parts[2] );
		}
		catch( final InstantiationException e ) {
			return new BadMethod( root, head );
		}
		catch( final IllegalAccessException e ) {
			return new BadMethod( root, head );
		}
		catch( final InvocationTargetException e ) {
			return new BadMethod( root, head );
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

			final Constructor<? extends HttpMethod> ct = cls.getConstructor( File.class, String.class, String.class );

			L.info( "Method %s implemented by %s", met, clsName );

			this.methods.put( met, ct );
		}
		catch( final ClassNotFoundException e ) {
			throw new IOException( clsName, e );
		}
		catch( final NoSuchMethodException e ) {
			throw new IOException( clsName, e );
		}
	}

	//	private void load( ClassLoader cld, URL u ) throws IOException
	//	{
	//		try {
	//			final BufferedReader r = new BufferedReader( new InputStreamReader( u.openStream() ) );
	//
	//			try {
	//				String cls = null;
	//
	//				while( (cls = r.readLine()) != null ) {
	//					add( cld, cls );
	//				}
	//			}
	//			finally {
	//				IO.close( r );
	//			}
	//		}
	//		catch( final IOException e ) {
	//			throw new IOException( String.format( "Cannot read %s", u ), e );
	//		}
	//	}

	private void scan() throws IOException
	{
		final ClassPathScanner cps = new ClassPathScanner( this );

		cps.addPackage( "net.pi.sws." );
		cps.addAnnotation( HTTP.class );
		cps.scan();
		//		final ClassLoader cld = Thread.currentThread().getContextClassLoader();
		//		final Enumeration<URL> e = cld.getResources( "META-INF/services/" + HttpService.class.getName() );
		//
		//		while( e.hasMoreElements() ) {
		//			load( cld, e.nextElement() );
		//		}
	}

}
