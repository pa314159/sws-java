
package net.pi.sws.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.pi.sws.io.IO;

/**
 * Some HTTP status codes.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class HttpCode
{

	static private final Properties	BUNDLE					= loadBundle();

	static public final HttpCode	CONTINUE				= new HttpCode( 100 );

	static public final HttpCode	OK						= new HttpCode( 200 );

	static public final HttpCode	BAD_REQUEST				= new HttpCode( 400 );

	static public final HttpCode	NOT_FOUND				= new HttpCode( 404 );

	static public final HttpCode	METHOD_NOT_ALLOWED		= new HttpCode( 405 );

	static public final HttpCode	INTERNAL_ERROR			= new HttpCode( 500 );

	static public final HttpCode	NOT_IMPLEMENTED			= new HttpCode( 501 );

	static public final HttpCode	VERSION_NOT_SUPPORTED	= new HttpCode( 505 );

	public static final HttpCode	FORBIDDEN				= new HttpCode( 403 );

	static private Properties loadBundle()
	{
		final Class<HttpCode> cls = HttpCode.class;
		final InputStream is = cls.getResourceAsStream( cls.getSimpleName() + ".properties" );

		if( is == null ) {
			throw new ExceptionInInitializerError();
		}

		try {
			final Properties bundle = new Properties();

			bundle.load( is );

			return bundle;
		}
		catch( final IOException e ) {
			throw new ExceptionInInitializerError( e );
		}
		finally {
			IO.close( is );
		}
	}

	private final int		code;

	private final String	text;

	public HttpCode( int code, String text )
	{
		this.code = code;
		this.text = BUNDLE.getProperty( text, text );
	}

	private HttpCode( int code )
	{
		this( code, Integer.toString( code ) );
	}

	public int intValue()
	{
		return this.code;
	}

	@Override
	public String toString()
	{
		return String.format( "%03d %s", this.code, this.text );
	}
}
