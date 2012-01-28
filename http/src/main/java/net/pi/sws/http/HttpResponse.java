
package net.pi.sws.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

import javax.mail.internet.MimeUtility;

import net.pi.sws.http.HttpHeader.General;
import net.pi.sws.io.BufferedChannelOutput;
import net.pi.sws.io.ChannelOutputStream;
import net.pi.sws.io.IO;
import net.pi.sws.util.ExtLog;

public class HttpResponse
extends HttpMessage<WritableByteChannel, OutputStream, Writer>
{

	private static final HttpHeader		SIGNATURE;

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

	private static final ExtLog			L		= ExtLog.get();

	private static final byte[]			CRLF	= { 0x0d, 0x0a };

	private final WritableByteChannel	channel;

	private boolean						headFlushed;

	private HttpCode					status;

	private final File					root;

	boolean								gzip;

	private boolean						output;

	private Flushable					flushable;

	private BufferedChannelOutput		buffered;

	HttpResponse( WritableByteChannel channel, File root ) throws IOException
	{
		super( channel );

		this.channel = channel;
		this.root = root.getCanonicalFile();

		setStatus( HttpCode.OK );
		setHeader( SIGNATURE );
	}

	public File getRoot()
	{
		return this.root;
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
				setHeader( new HttpHeader( General.CONTENT_LENGTH, this.buffered.size() ) );

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

		if( !this.gzip && isHeaderPresent( General.CONTENT_LENGTH, null ) ) {
			flushHead();
		}
		else {
			return this.buffered = new BufferedChannelOutput( channel, this.gzip );
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
		final String s = String.format( "%s: %s", h.name, MimeUtility.encodeText( h.content ) );

		L.trace( "RESPONSE: %s", s );

		this.channel.write( ByteBuffer.wrap( s.getBytes( IO.ISO_8859_1 ) ) );

		eol();
	}
}
