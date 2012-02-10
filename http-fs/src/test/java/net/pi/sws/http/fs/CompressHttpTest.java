
package net.pi.sws.http.fs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

import net.pi.sws.http.CompressionType;
import net.pi.sws.http.HttpHeader;
import net.pi.sws.http.HttpHeader.General;
import net.pi.sws.http.HttpHeader.Request;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith( Parameterized.class )
public class CompressHttpTest
extends AbstractHttpTest
{

	@Parameters
	static public List<Object[]> suite()
	{
		final List<Object[]> suite = new ArrayList<Object[]>();

		for( final CompressionType c : CompressionType.values() ) {
			for( int k = 0; k < 5; k++ ) {
				suite.add( new Object[] { c, k } );
			}
		}

		return suite;
	}

	private final CompressionType	type;

	private final int				size;

	private String					path;

	private byte[]					data;

	public CompressHttpTest( CompressionType type, int chunk )
	{
		this.type = type;
		this.size = 10000 * (chunk + 1);
	}

	@Test
	public void run() throws ClientProtocolException, IOException
	{
		final HttpRequest request = new HttpGet( "/" + this.path );

		request.addHeader( HttpHeader.Request.ACCEPT_ENCODING, this.type.name() );

		final HttpResponse response = client.execute( host, request );
		final Header h = response.getFirstHeader( HttpHeader.General.CONTENT_ENCODING );

		Assert.assertNotNull( h );
		Assert.assertEquals( this.type.name(), h.getValue() );

		final HttpEntity ent = response.getEntity();
		InputStream zin = null;

		switch( this.type ) {
			case gzip:
				zin = new GZIPInputStream( ent.getContent() );
			break;

			case deflate:
				zin = new InflaterInputStream( ent.getContent() );
			break;
		}

		final byte[] data = IOUtils.toByteArray( zin );

		Assert.assertArrayEquals( this.data, data );

		EntityUtils.consume( ent );
	}

	@Override
	protected File root() throws IOException
	{
		this.path = String.format( "data-%d-%s", this.size, this.type );

		final File root = new File( "target" );

		this.data = new byte[this.size];

		for( int k = 0; k < this.data.length; k++ ) {
			this.data[k] = (byte) k;
		}

		final File file = new File( root, this.path );

		FileUtils.writeByteArrayToFile( file, this.data );

		OutputStream zout = null;

		switch( this.type ) {
			case gzip:
				zout = new GZIPOutputStream( new FileOutputStream( new File( root, this.path + ".z" ) ) );
			break;
			case deflate:
				zout = new DeflaterOutputStream( new FileOutputStream( new File( root, this.path + ".z" ) ) );
			break;
		}

		zout.write( this.data );
		zout.close();

		return root;
	}
}
