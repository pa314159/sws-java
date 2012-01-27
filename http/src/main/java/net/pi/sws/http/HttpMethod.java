
package net.pi.sws.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.pi.sws.io.ChannelInputStream;
import net.pi.sws.io.ChannelOutputStream;

/**
 * Base of all HTTP methods.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public abstract class HttpMethod
{

	static class ByteDataOutputStream
	extends ByteArrayOutputStream
	{

		ByteDataOutputStream()
		{
			super( 8192 );
		}

		void transferTo( WritableByteChannel chn ) throws IOException
		{
			chn.write( ByteBuffer.wrap( this.buf, 0, this.count ) );
		}
	}

	enum IOType
	{
		STREAM( "Stream" ),
		BUFFER( "StreamBuffered" ),
		CHANNEL( "Channel" )

		;

		final String	suffix;

		IOType( String suffix )
		{
			this.suffix = suffix;
		}
	}

	static final Charset			ISO_8859_1	= Charset.forName( "ISO-8859-1" );

	private static final String		SIGNATURE;

	static {
		final InputStream is = HttpMethod.class.getResourceAsStream( "signature.txt" );

		if( is == null ) {
			throw new ExceptionInInitializerError( "Cannot find signature.txt" );
		}

		try {
			final BufferedReader rd = new BufferedReader( new InputStreamReader( is, "UTF-8" ) );

			SIGNATURE = rd.readLine();

			rd.close();
		}
		catch( final IOException e ) {
			throw (Error) new ExceptionInInitializerError( "Cannot read signature.txt" ).initCause( e );
		}
	}

	final Map<String, HttpHeader>	requestH	= new HashMap<String, HttpHeader>();

	private final List<HttpHeader>	responseH	= new ArrayList<HttpHeader>();

	private ReadableByteChannel		ic;

	private InputStream				is;

	private IOType					iType;

	private WritableByteChannel		oc;

	private OutputStream			os;

	private IOType					oType;

	private final String			version;

	private HttpCode				status;

	private boolean					flushed;

	private final String			uri;

	protected final File			root;

	protected HttpMethod( File root, String uri, String version ) throws IOException
	{
		this.root = root.getCanonicalFile();
		this.uri = uri;
		this.version = version;
	}

	public String getURI()
	{
		return this.uri;
	}

	private void flushHead() throws IOException
	{
		if( this.flushed ) {
			throw new IllegalStateException( "The header has been already flushed" );
		}

		final ByteDataOutputStream os = new ByteDataOutputStream();
		final PrintStream ps = new PrintStream( os, true, ISO_8859_1.name() );

		ps.printf( "%s %s %s\r\n", this.version, this.status.code, this.status.text );

		for( final HttpHeader h : this.responseH ) {
			ps.printf( "%s\r\n", h );
		}

		ps.printf( "\r\n" );

		os.transferTo( this.oc );

		// don't ps.close();

		this.flushed = true;
	}

	protected final void addResponseHeader( HttpHeader h ) throws IOException
	{
		if( this.flushed ) {
			throw new IllegalStateException( "Header has been already flushed" );
		}

		this.responseH.add( h );
	}

	protected ReadableByteChannel getInputChannel() throws IOException
	{
		if( (this.iType == null) || (this.iType == IOType.CHANNEL) ) {
			return this.ic;
		}

		throw new IllegalStateException( String.format( "Already called getInput%s()", this.oType.suffix ) );
	}

	protected InputStream getInputStream() throws IOException
	{
		if( this.iType == null ) {
			this.iType = IOType.STREAM;

			return this.is = new ChannelInputStream( this.ic );
		}
		if( this.iType == IOType.STREAM ) {
			return this.is;
		}

		throw new IllegalStateException( String.format( "Already called getInput%s()", this.oType.suffix ) );
	}

	protected WritableByteChannel getOutputChannel() throws IOException
	{
		if( this.oType == null ) {
			this.oType = IOType.CHANNEL;

			flushHead();

			return this.oc;
		}
		if( this.oType == IOType.CHANNEL ) {
			return this.oc;
		}

		throw new IllegalStateException( String.format( "Already called getOutput%s()", this.oType.suffix ) );
	}

	protected OutputStream getOutputStream() throws IOException
	{
		if( this.oType == null ) {
			this.oType = IOType.STREAM;

			flushHead();

			return this.os = new ChannelOutputStream( this.oc );
		}
		if( this.oType == IOType.STREAM ) {
			return this.os;
		}

		throw new IllegalStateException( String.format( "Already called getOutput%s()", this.oType.suffix ) );
	}

	protected OutputStream getOutputStreamBuffered() throws IOException
	{
		if( this.oType == null ) {
			this.oType = IOType.BUFFER;

			return this.os = new ByteDataOutputStream();
		}
		if( this.oType == IOType.BUFFER ) {
			return this.os;
		}

		throw new IllegalStateException( String.format( "Already called getOutput%s()", this.oType.suffix ) );
	}

	protected final HttpHeader getRequestHeader( String name )
	{
		return this.requestH.get( name );
	}

	protected abstract void respond() throws IOException;

	protected final void setStatus( HttpCode code ) throws IOException
	{
		if( this.flushed ) {
			throw new IllegalStateException( "Header has been already flushed" );
		}

		this.status = code;
	}

	void add( HttpHeader h )
	{
		this.requestH.put( h.name, h );
	}

	final void forward( ReadableByteChannel ic, WritableByteChannel oc ) throws IOException
	{
		this.ic = ic;
		this.oc = oc;

		final HttpHeader h = getRequestHeader( HttpHeader.Request.EXPECT );

		if( (h != null) && "100-continue".equals( h.content ) ) {

		}
		else
		{
			setStatus( HttpCode.S_OK );
		}

		addResponseHeader( new HttpHeader( "Server", SIGNATURE ) );

		respond();

		if( !this.flushed ) {
			assert (this.oType == null) || (this.oType == IOType.BUFFER);

			flushHead();
		}

		if( this.os != null ) {
			this.os.flush();
		}

		if( this.oType == IOType.BUFFER ) {
			((ByteDataOutputStream) this.os).transferTo( oc );
		}
	}
}
