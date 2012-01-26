
package net.pi.sws.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.pi.sws.util.IO;

public final class HttpCode
{

	static private final Class<HttpCode>	CLASS				= HttpCode.class;

	static private final Properties			MESSAGES			= new Properties();

	static {
		final InputStream is = CLASS.getResourceAsStream( CLASS.getSimpleName() + ".properties" );

		if( is == null ) {
			throw new ExceptionInInitializerError();
		}

		try {
			MESSAGES.load( is );
		}
		catch( final IOException e ) {
			throw new ExceptionInInitializerError( e );
		}
		finally {
			IO.close( is );
		}
	}

	static public final HttpCode			S_OK				= new HttpCode( "200" );

	static public final HttpCode			S_BAD_REQUEST		= new HttpCode( "400" );

	static public final HttpCode			S_INTERNAL_ERROR	= new HttpCode( "500" );

	final byte[]							code;

	final byte[]							text;

	private HttpCode( String code )
	{
		this.code = code.getBytes( HttpMethod.ISO_8859_1 );
		this.text = MESSAGES.getProperty( code, code ).getBytes( HttpMethod.ISO_8859_1 );
	}
}
