
package net.pi.sws.http;

import java.io.BufferedReader;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpCookie;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.pi.sws.http.HttpHeader.General;
import net.pi.sws.io.BufferedChannelOutput;
import net.pi.sws.io.ChannelOutputStream;
import net.pi.sws.io.DeflaterChannelOutput;
import net.pi.sws.io.GZIPChannelOutput;
import net.pi.sws.io.IO;
import net.pi.sws.util.ExtLog;

/**
 * The HTTP response object.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class HttpResponse
extends HttpMessage<WritableByteChannel, OutputStream, Writer>
{

	static final HttpHeader				SIGNATURE;

	static {
		final InputStream is = HttpMethod.class.getResourceAsStream( "signature.txt" );

		if( is == null ) {
			throw new ExceptionInInitializerError( "Cannot find signature.txt" );
		}

		try {
			final BufferedReader rd = new BufferedReader( new InputStreamReader( is, "UTF-8" ) );

			SIGNATURE = new HttpHeader( HttpHeader.Response.SERVER, rd.readLine() );

			rd.close();
		}
		catch( final IOException e ) {
			throw (Error) new ExceptionInInitializerError( "Cannot read signature.txt" ).initCause( e );
		}
	}

	private static final ExtLog			L		= ExtLog.get( HttpService.class );

	private static final byte[]			CRLF	= { 0x0d, 0x0a };

	private final WritableByteChannel	channel;

	private boolean						headFlushed;

	private HttpCode					status;

	private boolean						output;

	private Flushable					flushable;

	private BufferedChannelOutput		buffered;

	private DeflaterChannelOutput		compressed;

	CompressionType						compression;

	private final List<HttpCookie>		cookies	= new ArrayList<HttpCookie>();

	HttpResponse( WritableByteChannel channel ) throws IOException
	{
		super( channel );

		this.channel = channel;

		setStatus( HttpCode.OK );
		setHeader( SIGNATURE );
		setHeader( new HttpHeader( "Date", new Date() ) );
	}

	public void addCookie( HttpCookie cookie )
	{
		this.cookies.add( cookie );
	}

	public void disableCompression()
	{
		L.trace( "Compression has been disabled" );

		if( this.output ) {
			throw new IllegalStateException( "The output has been already called" );
		}

		this.compression = null;
	}

	public HttpCode getStatus()
	{
		return this.status;
	}

	@Override
	public void setHeader( HttpHeader h )
	{
		if( this.headFlushed ) {
			throw new IllegalStateException( "The headers have been already headFlushed" );
		}

		super.setHeader( h );
	}

	public void setStatus( HttpCode code )
	{
		if( this.headFlushed ) {
			throw new IllegalStateException( "The headers have been already headFlushed" );
		}

		this.status = code;
	}

	void flush() throws IOException
	{
		if( this.output ) {
			assert (this.buffered != null) == !this.headFlushed;

			if( this.flushable != null ) {
				this.flushable.flush();
			}
			if( this.buffered != null ) {
				if( this.compressed != null ) {
					this.compressed.flush();
				}

				setHeader( new HttpHeader( General.CONTENT_LENGTH, this.buffered.size() ) );

				if( this.compression != null ) {
					setHeader( new HttpHeader( General.CONTENT_ENCODING, this.compression.name() ) );
				}

				flushHead();

				this.buffered.flush();
			}
		}
		else {
			assert !this.headFlushed;

			setHeader( new HttpHeader( General.CONTENT_LENGTH, 0 ) );

			flushHead();
		}
	}

	@Override
	OutputStream newByteStream( WritableByteChannel channel ) throws IOException
	{
		final ChannelOutputStream s = new ChannelOutputStream( channel );

		this.flushable = s;

		return s;
	}

	@Override
	Writer newCharStream( OutputStream stream, Charset cs )
	{
		final OutputStreamWriter s = new OutputStreamWriter( stream, cs );

		this.flushable = s;

		return s;
	}

	@Override
	WritableByteChannel wrap( WritableByteChannel channel ) throws IOException
	{
		this.output = true;

		channel = super.wrap( channel );

		if( (this.compression == null) && isHeaderPresent( General.CONTENT_LENGTH ) ) {
			flushHead();
		}
		else {
			this.buffered = new BufferedChannelOutput( channel );

			if( this.compression != null ) {
				switch( this.compression ) {
					case gzip:
						this.compressed = new GZIPChannelOutput( this.buffered );
					break;

					case deflate:
						this.compressed = new DeflaterChannelOutput( this.buffered );
					break;

					default:
						throw new AssertionError( "unreachable or not implemented" );
				}

				return this.compressed;
			}
			else {
				return this.buffered;
			}
		}

		return channel;
	}

	private void eol() throws IOException
	{
		this.channel.write( ByteBuffer.wrap( CRLF ) );
	}

	private void flushHead() throws IOException
	{
		if( this.headFlushed ) {
			throw new IllegalStateException( "The headers have been already headFlushed" );
		}

		write( this.status );

		for( final HttpHeader h : getHeaders() ) {
			write( h );
		}

		for( final HttpCookie c : this.cookies ) {
			write( new HttpHeader( c ) );

		}

		eol();

		this.headFlushed = true;
	}

	private void write( HttpCode status ) throws IOException
	{
		final String s = String.format( "%s %s", HttpVersion.HTTP1_1, status );

		L.trace( "RESPONSE: %s", s );

		this.channel.write( ByteBuffer.wrap( s.getBytes( IO.ISO_8859_1 ) ) );

		eol();
	}

	private void write( HttpHeader h ) throws IOException
	{
		final String s = h.toString();

		L.trace( "RESPONSE: %s", s );

		this.channel.write( ByteBuffer.wrap( s.getBytes( IO.ISO_8859_1 ) ) );

		eol();
	}
}
