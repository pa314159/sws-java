
package net.pi.sws.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.pi.sws.util.IO;

/**
 * Some HTTP status codes.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public final class HttpCode
{

	static private final Class<HttpCode>	CLASS					= HttpCode.class;

	static private final Properties			MESSAGES				= new Properties();

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

	static public final HttpCode			S_OK					= new HttpCode( "200" );

	static public final HttpCode			S_BAD_REQUEST			= new HttpCode( "400" );

	static public final HttpCode			S_NOT_FOUND				= new HttpCode( "404" );

	static public final HttpCode			S_METHOD_NOT_ALLOWED	= new HttpCode( "405" );

	static public final HttpCode			S_INTERNAL_ERROR		= new HttpCode( "500" );

	final String							code;

	final String							text;

	private HttpCode( String code )
	{
		this.code = code;
		this.text = MESSAGES.getProperty( code, code );
	}
}
