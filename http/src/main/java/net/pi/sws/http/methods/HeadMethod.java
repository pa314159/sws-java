
package net.pi.sws.http.methods;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.pi.sws.http.HTTP;
import net.pi.sws.http.HttpCode;
import net.pi.sws.http.HttpHeader;
import net.pi.sws.http.HttpHeader.General;
import net.pi.sws.http.HttpHeader.Request;
import net.pi.sws.http.HttpHeader.Response;
import net.pi.sws.http.HttpMethod;
import net.pi.sws.http.HttpRequest;
import net.pi.sws.http.HttpResponse;
import net.pi.sws.io.IO;
import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.detector.ExtensionMimeDetector;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;

/**
 * Implementation of HEAD
 * 
 * @author PAPPY <a
 *         href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com
 *         &gt;</a>
 */
@HTTP( "HEAD" )
public class HeadMethod
extends HttpMethod
{

	static {
		MimeUtil.registerMimeDetector( ExtensionMimeDetector.class.getName() );
		MimeUtil.registerMimeDetector( MagicMimeMimeDetector.class.getName() );
	}

	static private final Pattern				JS		= Pattern.compile( ".*javascript",
															Pattern.CASE_INSENSITIVE );

	/**
	 * The default page in a directory.
	 * 
	 * TODO make it configurable some day.
	 */
	static final String							DEFAULT	= "default.html";

	static private final Map<String, byte[]>	ICONS	= new HashMap<String, byte[]>();

	static {
		loadIcon( "file.gif" );
		loadIcon( "folder.gif" );
	}

	private static void loadIcon( String icon )
	{
		final InputStream is = HeadMethod.class.getResourceAsStream( icon );

		if( is == null ) {
			throw new ExceptionInInitializerError( icon );
		}

		final ByteArrayOutputStream data = new ByteArrayOutputStream();

		try {
			IO.copy( is, data );
		}
		catch( final IOException e ) {
			throw (Error) new ExceptionInInitializerError( icon ).initCause( e );
		}

		IO.close( is );

		ICONS.put( icon, data.toByteArray() );
	}

	HeadMethod( HttpRequest request, HttpResponse response )
	{
		super( request, response );
	}

	void send( byte[] data ) throws IOException
	{
		this.response.setHeader( new HttpHeader( General.CONTENT_TYPE,
			"image/gif" ) );
		this.response.setHeader( new HttpHeader( General.CONTENT_LENGTH,
			data.length ) );
	}

	void send( File file ) throws IOException
	{
		if( file.isDirectory() ) {
			this.response.setHeader( new HttpHeader( General.CONTENT_TYPE,
				"text/html; charset=UTF-8" ) );
		}
		else {
			final Collection<MimeType> mimeTypes = MimeUtil.getMimeTypes( file );
			final MimeType mimeType = mimeTypes.iterator().next();

			// small hack to make JS work on Firefox
			if( JS.matcher( mimeType.getSubType() ).matches() ) {
				this.response.disableCompression();
			}

			this.response.setHeader( new HttpHeader( General.CONTENT_TYPE,
				mimeType.toString() ) );
			this.response.setHeader( new HttpHeader( General.CONTENT_LENGTH, file
				.length() ) );
			this.response.setHeader( new HttpHeader( Response.LAST_MODIFIED,
				new Date( file.lastModified() ) ) );
		}
	}

	@Override
	protected final void respond() throws IOException
	{
		final String uri = this.request.getURI();

		// simple hack for icons
		if( this.request.isHeaderPresent( Request.REFERER ) ) {
			final byte[] data = ICONS.get( uri.substring( 1 ) );

			if( data != null ) {
				send( data );

				return;
			}
		}

		File file = new File( this.response.getRoot(), uri ).getCanonicalFile();

		if( !file.exists() ) {
			this.response.setStatus( HttpCode.NOT_FOUND );

			return;
		}
		if( !allowed( file ) ) {
			this.response.setStatus( HttpCode.FORBIDDEN );

			return;
		}

		if( file.isDirectory() ) {
			// or should I send a redirect?
			final File def = new File( file, DEFAULT );

			if( def.isFile() ) {
				file = def;
			}
		}

		send( file );
	}

	private boolean allowed( File file )
	{
		final File root = this.response.getRoot();

		while( file != null ) {
			if( file.equals( root ) ) {
				break;
			}

			file = file.getParentFile();
		}

		return file != null;
	}

}
