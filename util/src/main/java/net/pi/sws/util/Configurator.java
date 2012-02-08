
package net.pi.sws.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public final class Configurator
{

	private static final Map<Class, Class>	TYPES	= new HashMap<Class, Class>();

	static {
		TYPES.put( boolean.class, Boolean.class );
		TYPES.put( byte.class, Byte.class );
		TYPES.put( short.class, Short.class );
		TYPES.put( char.class, Character.class );
		TYPES.put( int.class, Integer.class );
		TYPES.put( long.class, Long.class );
		TYPES.put( float.class, Float.class );
		TYPES.put( double.class, Double.class );
	}

	public static void configure( Object object, Map<String, Object> configuration ) throws Exception
	{
		final BeanInfo bi = Introspector.getBeanInfo( object.getClass() );
		final PropertyDescriptor[] pdv = bi.getPropertyDescriptors();

		configuration = split( configuration );

		for( final PropertyDescriptor pd : pdv ) {
			if( pd.getWriteMethod() == null ) {
				continue;
			}

			configure( object, pd, configuration );
		}
	}

	static Map<String, Object> split( Map<String, Object> configuration )
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

	private static Object buildInstance( Class<?> cls ) throws Exception
	{
		return cls.newInstance();
	}

	private static Object buildInstance( Class<?> cls, Object object ) throws Exception
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
				t = TYPES.get( t );

				if( t.isInstance( object ) ) {
					return ct.newInstance( object );
				}
			}
		}

		return null;
	}

	private static void configure( Object object, PropertyDescriptor pd, Map<String, Object> configuration ) throws Exception
	{
		final String name = pd.getName();

		if( !configuration.containsKey( name ) ) {
			return;
		}

		Object set = configuration.get( name );
		Object val;

		if( pd.getReadMethod() != null ) {
			val = pd.getReadMethod().invoke( object );
		}
		else {
			val = null;
		}

		if( !pd.getPropertyType().isInstance( set ) ) {
			if( set instanceof Map ) {
				final Map<String, Object> sub = (Map<String, Object>) set;

				if( val == null ) {
					if( sub.containsKey( "" ) ) {
						set = buildInstance( pd.getPropertyType(), sub.get( "" ) );
					}
					else {
						set = buildInstance( pd.getPropertyType() );
					}
				}

				if( set != null ) {
					configure( set, sub );
				}
			}
			else {
				return;
			}
		}

		pd.getWriteMethod().invoke( object, set );
	}

	private Configurator()
	{
	}

}
