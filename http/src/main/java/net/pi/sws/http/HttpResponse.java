
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

	private Flushable					flushable;

	private boolean						flushed;

	private HttpCode					status;

	private final File					root;

	HttpResponse( WritableByteChannel channel, File root )
	{
		super( channel );

		this.channel = channel;
		this.root = root;

		setStatus( HttpCode.OK );
		addHeader( SIGNATURE );
	}

	@Override
	public void addHeader( HttpHeader h )
	{
		if( this.flushed ) {
			throw new IllegalStateException( "The headers have been already flushed" );
		}

		super.addHeader( h );
	}

	public File getRoot()
	{
		return this.root;
	}

	public void setStatus( HttpCode code )
	{
		if( this.flushed ) {
			throw new IllegalStateException( "The headers have been already flushed" );
		}

		this.status = code;
	}

	private void eol() throws IOException
	{
		this.channel.write( ByteBuffer.wrap( CRLF ) );
	}

	private void flushHead() throws IOException
	{
		if( this.flushed ) {
			throw new IllegalStateException( "The headers have been already flushed" );
		}

		write( this.status );

		for( final HttpHeader h : getHeaders() ) {
			write( h );
		}

		eol();

		this.flushed = true;
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

	void flush() throws IOException
	{
		if( !this.flushed ) {
			flushHead();
		}
		if( this.flushable != null ) {
			this.flushable.flush();
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
		flushHead();

		channel = super.wrap( channel );

		if( channel instanceof Flushable ) {
			this.flushable = (Flushable) channel;
		}

		return channel;
	}
}
