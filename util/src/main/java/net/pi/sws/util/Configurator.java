
package net.pi.sws.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public final class Configurator
{

	static private final ExtLog				L			= ExtLog.get();

	static private final Map<Class, Class>	PRIMITIVES	= new HashMap<Class, Class>();

	static {
		PRIMITIVES.put( boolean.class, Boolean.class );
		PRIMITIVES.put( byte.class, Byte.class );
		PRIMITIVES.put( short.class, Short.class );
		PRIMITIVES.put( char.class, Character.class );
		PRIMITIVES.put( int.class, Integer.class );
		PRIMITIVES.put( long.class, Long.class );
		PRIMITIVES.put( float.class, Float.class );
		PRIMITIVES.put( double.class, Double.class );
	}

	static private Object buildInstance( Class<?> cls ) throws Exception
	{
		return cls.newInstance();
	}

	static private Object buildInstance( Class<?> cls, Object object ) throws Exception
	{
		if( object instanceof String ) {
			final ClassLoader cld = Thread.currentThread().getContextClassLoader();

			try {
				return Class.forName( (String) object, true, cld ).newInstance();
			}
			catch( final ClassNotFoundException e ) {
				e.printStackTrace();
			}
		}

		for( final Constructor ct : cls.getConstructors() ) {
			final Class[] types = ct.getParameterTypes();

			if( types.length != 1 ) {
				continue;
			}

			Class t = types[0];

			if( t.isInstance( object ) ) {
				return ct.newInstance( object );
			}

			if( t.isPrimitive() ) {
				t = PRIMITIVES.get( t );

				if( t.isInstance( object ) ) {
					return ct.newInstance( object );
				}
			}
		}

		return null;
	}

	static private Map<String, Object> split( Map<String, Object> configuration )
	{
		final Map<String, Object> result = new TreeMap<String, Object>();
		final Map<String, Map<String, Object>> dots = new TreeMap<String, Map<String, Object>>();

		for( final Map.Entry<String, Object> e : configuration.entrySet() ) {
			final String k = e.getKey();
			final Object v = e.getValue();
			final int x = k.indexOf( '.' );

			if( x < 0 ) {
				result.put( k, v );
			}
			else {
				final String prefix = k.substring( 0, x );

				Map<String, Object> m = dots.get( prefix );

				if( m == null ) {
					m = new TreeMap<String, Object>();

					dots.put( prefix, m );
				}

				m.put( k.substring( x + 1 ), v );
			}
		}

		for( final Map.Entry<String, Map<String, Object>> e : dots.entrySet() ) {
			final String k = e.getKey();
			final Map<String, Object> v = split( e.getValue() );

			if( configuration.containsKey( k ) ) {
				v.put( "", configuration.get( k ) );
			}

			result.put( k, v );
		}

		return result;
	}

	private final Map<String, Object>	configuration;

	public Configurator( Map<String, Object> configuration )
	{
		this.configuration = split( configuration );
	}

	public void configure( Object object ) throws Exception
	{
		if( object != null ) {
			configureWith( object, object.getClass().getName(), this.configuration );
		}
	}

	private void configureWith( Object object, String base, Map<String, Object> configuration ) throws IntrospectionException, Exception
	{
		final BeanInfo bi = Introspector.getBeanInfo( object.getClass() );
		final PropertyDescriptor[] pdv = bi.getPropertyDescriptors();

		for( final PropertyDescriptor pd : pdv ) {
			if( pd.getWriteMethod() == null ) {
				continue;
			}

			configureWith( object, base, configuration, pd );
		}
	}

	private void configureWith( Object object, String base, Map<String, Object> configuration, PropertyDescriptor pd ) throws Exception
	{
		final String name = pd.getName();

		if( !configuration.containsKey( name ) ) {
			return;
		}

		Object set = configuration.get( name );
		Object get;

		if( pd.getReadMethod() != null ) {
			get = pd.getReadMethod().invoke( object );
		}
		else {
			get = null;
		}

		final Class<?> pt = pd.getPropertyType();

		if( !pt.isInstance( set ) ) {
			if( set instanceof Map ) {
				L.trace( "Found enclosed object %s.%s of type %s", base, name, pt );

				final Map<String, Object> cf = (Map<String, Object>) set;

				if( get == null ) {
					if( cf.containsKey( "" ) ) {
						set = buildInstance( pt, cf.get( "" ) );
					}
					else {
						set = buildInstance( pt );
					}
				}

				if( set != null ) {
					configureWith( set, base + "." + name, cf );
				}
			}
			else {
				if( set != null ) {
					set = buildInstance( pt, set );
				}
				else {
					set = buildInstance( pt );
				}
			}
		}

		L.trace( "Initialising %s.%s with %s", base, name, set );

		pd.getWriteMethod().invoke( object, set );
	}

}
