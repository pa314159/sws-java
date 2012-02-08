
package net.pi.sws.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ConfiguratorTest
{

	public static class A
	{

		B	b;

		public A( int n )
		{
		}

		public void setB( B b )
		{
			this.b = b;
		}
	}

	public static class B
	{

		C	c;

		public B( int n )
		{
		}

		public void setC( C c )
		{
			this.c = c;
		}
	}

	public static class B2
	extends B
	{

		public B2()
		{
			super( 0 );
		}
	}

	public static class C
	{

		D	d;

		public C( int n )
		{
		}

		public void setD( D d )
		{
			this.d = d;
		}
	}

	public static class D
	{

		E	e;

		public D( int n )
		{
		}

		public void setE( E e )
		{
			this.e = e;
		}
	}

	public static class E
	{

		F	f;

		public E( int n )
		{
		}

		public void setF( F f )
		{
			this.f = f;
		}
	}

	public static class F
	{

		G	g;

		public F( int n )
		{
		}

		public void setG( G g )
		{
			this.g = g;
		}
	}

	public static class G
	{

		public G( int n )
		{
		}
	}

	public static class ROOT
	{

		A	a;

		public void setA( A a )
		{
			this.a = a;
		}
	}

	@Test
	public void split() throws Exception
	{
		final Map<String, Object> m = new HashMap<String, Object>();

		m.put( "a", 1 );
		m.put( "a.b", B2.class.getName() );
		m.put( "a.b.c", 3 );
		m.put( "a.c.d.e.f.g", 4 );
		m.put( "b", 5 );
		m.put( "c.d.e", 7 );

		Configurator.configure( new ROOT(), m );
	}

}
