
package net.pi.sws.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith( Parameterized.class )
public class GZIPChannelOutputTest
{

	private static final SecureRandom	RANDOM	= new SecureRandom();

	@Parameters
	static public List<Object[]> suite()
	{
		final List<Object[]> suite = new ArrayList<Object[]>();

		for( int k = 0; k < 5; k++ ) {
			suite.add( new Object[] { k + 1 } );
		}

		return suite;
	}

	private final int	chunks;

	public GZIPChannelOutputTest( int chunks )
	{
		this.chunks = chunks;
	}

	@Test
	public void run1() throws IOException
	{
		final ByteArrayOutputStream expected = new ByteArrayOutputStream();
		final ByteArrayOutputStream result = new ByteArrayOutputStream();

		for( int k = 0; k < this.chunks; k++ ) {
			final byte[] data = new byte[8192 * (k + 1)];

			RANDOM.nextBytes( data );

			final GZIPOutputStream dos = new GZIPOutputStream( expected );

			dos.write( data );
			dos.finish();

			final GZIPChannelOutput dco = new GZIPChannelOutput( Channels.newChannel( result ) );

			dco.write( ByteBuffer.wrap( data ) );
			dco.flush();
		}

		Assert.assertArrayEquals( expected.toByteArray(), result.toByteArray() );
	}

	@Test
	public void run2() throws IOException
	{
		final ByteArrayOutputStream expected = new ByteArrayOutputStream();
		final ByteArrayOutputStream result = new ByteArrayOutputStream();

		for( int k = 0; k < this.chunks; k++ ) {
			final byte[] data = new byte[8192 * (k + 1)];

			RANDOM.nextBytes( data );

			final GZIPOutputStream dos = new GZIPOutputStream( expected, 2048 );

			dos.write( data );
			dos.finish();

			final GZIPChannelOutput dco = new GZIPChannelOutput( Channels.newChannel( result ), 2048 );

			dco.write( ByteBuffer.wrap( data ) );
			dco.flush();
		}

		Assert.assertArrayEquals( expected.toByteArray(), result.toByteArray() );
	}
}
