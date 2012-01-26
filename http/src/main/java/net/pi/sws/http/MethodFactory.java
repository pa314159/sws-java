
package net.pi.sws.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import net.pi.sws.util.IO;

final class MethodFactory
{

	private static final MethodFactory	INSTANCE	= new MethodFactory();

	static {
		try {
			INSTANCE.scan();
		}
		catch( final IOException e ) {
			throw new ExceptionInInitializerError( e );
		}
	}

	static HttpMethod getMethod( String head, File root ) throws IOException
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

	private final Map<String, Constructor<? extends HttpMethod>>	methods	= new HashMap<String, Constructor<? extends HttpMethod>>();

	private MethodFactory()
	{
	}

	private void add( ClassLoader cld, String clsName ) throws IOException
	{
		try {
			final Class<? extends HttpMethod> cls = (Class<? extends HttpMethod>) cld.loadClass( clsName );
			final String metName = cls.getSimpleName().replaceAll( "Method$", "" ).toUpperCase();

			final Constructor<? extends HttpMethod> ct = cls.getConstructor( File.class, String.class, String.class );

			this.methods.put( metName, ct );
		}
		catch( final ClassNotFoundException e ) {
			throw new IOException( clsName, e );
		}
		catch( final NoSuchMethodException e ) {
			throw new IOException( clsName, e );
		}
	}

	private void load( ClassLoader cld, URL u ) throws IOException
	{
		try {
			final BufferedReader r = new BufferedReader( new InputStreamReader( u.openStream() ) );

			try {
				String cls = null;

				while( (cls = r.readLine()) != null ) {
					add( cld, cls );
				}
			}
			finally {
				IO.close( r );
			}
		}
		catch( final IOException e ) {
			throw new IOException( String.format( "Cannot read %s", u ), e );
		}
	}

	private void scan() throws IOException
	{
		final ClassLoader cld = Thread.currentThread().getContextClassLoader();
		final Enumeration<URL> e = cld.getResources( "META-INF/services/" + HttpService.class.getName() );

		while( e.hasMoreElements() ) {
			load( cld, e.nextElement() );
		}
	}

}
