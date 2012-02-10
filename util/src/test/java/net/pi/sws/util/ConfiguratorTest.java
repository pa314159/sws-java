
package net.pi.sws.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class ConfiguratorTest
{

	public static class A
	{

		B			b;

		C			c;

		final int	n;

		public A( int n )
		{
			this.n = n;
		}

		public void setB( B b )
		{
			this.b = b;
		}

		public void setC( C c )
		{
			this.c = c;
		}
	}

	public static class B
	{

		C			c;

		final int	n;

		public B( int n )
		{
			this.n = n;
		}

		public void setC( C c )
		{
			this.c = c;
		}
	}

	public static class BD
	extends B
	{

		public BD()
		{
			super( 0 );
		}
	}

	public static class C
	{

		D			d;

		final int	n;

		public C()
		{
			this( 0 );
		}

		public C( int n )
		{
			this.n = n;
		}

		public void setD( D d )
		{
			this.d = d;
		}
	}

	public static class D
	{

		E	e;

		public void setE( E e )
		{
			this.e = e;
		}
	}

	public static class E
	{

		final int	n;

		F			f;

		public E()
		{
			this.n = 0;
		}

		public E( int n )
		{
			this.n = n;
		}

		public void setF( F f )
		{
			this.f = f;
		}
	}

	public static class F
	{

		G	g;

		public void setG( G g )
		{
			this.g = g;
		}
	}

	public static class G
	{

		final int	n;

		public G( int n )
		{
			this.n = n;
		}
	}

	public static class ROOT
	{

		A	a;

		B	b;

		C	c;

		public void setA( A a )
		{
			this.a = a;
		}

		public void setB( B b )
		{
			this.b = b;
		}

		public void setC( C c )
		{
			this.c = c;
		}
	}

	@Test
	public void run() throws Exception
	{
		final Map<String, Object> m = new HashMap<String, Object>();

		m.put( "a", 1 );
		m.put( "a.b", BD.class.getName() );
		m.put( "a.b.c", 3 );
		m.put( "a.c", 4 );
		m.put( "a.c.d.e.f.g", 5 );
		m.put( "b", 6 );
		m.put( "c.d.e", 7 );

		final Configurator c = new Configurator( m );
		final ROOT o = new ROOT();

		c.configure( o );

		Assert.assertNotNull( o.a );
		Assert.assertNotNull( o.a.b );
		Assert.assertNotNull( o.a.b.c );
		Assert.assertNull( o.a.b.c.d );
		Assert.assertNotNull( o.a.c );
		Assert.assertNotNull( o.a.c.d );
		Assert.assertNotNull( o.a.c.d.e );
		Assert.assertNotNull( o.a.c.d.e.f );
		Assert.assertNotNull( o.a.c.d.e.f.g );
		Assert.assertNotNull( o.b );
		Assert.assertNull( o.b.c );
		Assert.assertNotNull( o.c );
		Assert.assertNotNull( o.c.d );
		Assert.assertNotNull( o.c.d.e );
		Assert.assertNull( o.c.d.e.f );

		Assert.assertEquals( 1, o.a.n );
		Assert.assertEquals( 0, o.a.b.n );
		Assert.assertEquals( 3, o.a.b.c.n );
		Assert.assertEquals( 0, o.a.c.d.e.n );
		Assert.assertEquals( 5, o.a.c.d.e.f.g.n );
		Assert.assertEquals( 4, o.a.c.n );
		Assert.assertEquals( 6, o.b.n );
		Assert.assertEquals( 0, o.c.n );
		Assert.assertEquals( 7, o.c.d.e.n );
	}
}
